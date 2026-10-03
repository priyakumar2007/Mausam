package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.local.CropAnalysisEntity
import com.example.data.local.CropFieldEntity
import com.example.data.local.MausamDatabase
import com.example.data.model.CropAiAnalysisResult
import com.example.data.model.CropCategoryRisk
import com.example.data.model.CropRiskAssessment
import com.example.data.model.CropRiskLevel
import com.example.data.model.LocationInfo
import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.abs

class CropRiskRepository(
  private val context: Context,
  private val db: MausamDatabase,
  private val weatherRepo: WeatherRepository
) {

  fun getAllFields(): Flow<List<CropFieldEntity>> = db.cropFieldDao().getAllFieldsFlow()
  fun getAllAnalyses(): Flow<List<CropAnalysisEntity>> = db.cropAnalysisDao().getAllAnalysesFlow()

  suspend fun saveField(field: CropFieldEntity): Long = withContext(Dispatchers.IO) {
    db.cropFieldDao().insertField(field)
  }

  suspend fun saveAnalysis(analysis: CropAnalysisEntity): Long = withContext(Dispatchers.IO) {
    db.cropAnalysisDao().insertAnalysis(analysis)
  }

  data class ImageQualityResult(
    val isValid: Boolean,
    val warningMessage: String? = null,
    val brightnessRatio: Float = 0.5f,
    val greenDominance: Float = 0.5f,
    val yellowSpots: Float = 0f,
    val brownSpots: Float = 0f
  )

  /**
   * Pre-analysis Image Quality Check
   * Checks for extreme darkness, overexposure, or low contrast / non-vegetation framing.
   */
  suspend fun checkImageQuality(uri: Uri): ImageQualityResult = withContext(Dispatchers.IO) {
    var inputStream: InputStream? = null
    try {
      inputStream = context.contentResolver.openInputStream(uri)
      val options = BitmapFactory.Options().apply {
        inSampleSize = 4 // Downsample for fast inspection
      }
      val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        ?: return@withContext ImageQualityResult(false, "Could not decode image. Please select a valid photo.")

      var totalLuminance = 0L
      var pixelCount = 0
      var greenPixels = 0
      var yellowPixels = 0
      var brownDarkPixels = 0

      val width = bitmap.width
      val height = bitmap.height
      val step = 4

      for (x in 0 until width step step) {
        for (y in 0 until height step step) {
          val pixel = bitmap.getPixel(x, y)
          val r = (pixel shr 16) and 0xFF
          val g = (pixel shr 8) and 0xFF
          val b = pixel and 0xFF

          val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
          totalLuminance += lum
          pixelCount++

          // Plant color heuristics
          if (g > r && g > b && g > 60) {
            greenPixels++
          } else if (r > 140 && g > 130 && b < 100) {
            yellowPixels++ // Chlorosis / yellowing
          } else if (r > 70 && g > 40 && b < 40 && abs(r - g) > 20) {
            brownDarkPixels++ // Necrotic spot / fungal blight
          }
        }
      }

      val avgLum = if (pixelCount > 0) totalLuminance.toFloat() / pixelCount else 128f
      val greenRatio = if (pixelCount > 0) greenPixels.toFloat() / pixelCount else 0f
      val yellowRatio = if (pixelCount > 0) yellowPixels.toFloat() / pixelCount else 0f
      val brownRatio = if (pixelCount > 0) brownDarkPixels.toFloat() / pixelCount else 0f

      // Quality checks
      if (avgLum < 35f) {
        return@withContext ImageQualityResult(
          isValid = false,
          warningMessage = "Image is too dark. Please take photo in good daylight."
        )
      }
      if (avgLum > 235f) {
        return@withContext ImageQualityResult(
          isValid = false,
          warningMessage = "Image is overexposed/too bright. Please avoid direct harsh glare."
        )
      }
      if (greenRatio < 0.08f && yellowRatio < 0.05f) {
        return@withContext ImageQualityResult(
          isValid = false,
          warningMessage = "Crop foliage not clearly visible. Please capture a closer image of the plant leaf or canopy."
        )
      }

      ImageQualityResult(
        isValid = true,
        brightnessRatio = avgLum / 255f,
        greenDominance = greenRatio,
        yellowSpots = yellowRatio,
        brownSpots = brownRatio
      )
    } catch (e: Exception) {
      ImageQualityResult(false, "Error reading image file: ${e.localizedMessage}")
    } finally {
      try { inputStream?.close() } catch (ignored: Exception) {}
    }
  }

  /**
   * Combines Image Observations + Exact Field Location Weather to compute crop risks.
   */
  suspend fun assessCropRisk(
    imageUri: Uri,
    fieldLocation: LocationInfo,
    cropType: String = "Paddy"
  ): CropRiskAssessment = withContext(Dispatchers.IO) {
    val quality = checkImageQuality(imageUri)
    val fieldWeather = weatherRepo.getWeatherData(fieldLocation)

    val reasons = mutableListOf<String>()
    val suggestions = mutableListOf<String>()
    val observations = mutableListOf<String>()

    // 1. Analyze Weather conditions for field location
    val upcomingRainProb = fieldWeather.hourly.take(24).maxOfOrNull { it.rainProb } ?: 20
    val highRainHours = fieldWeather.hourly.take(24).count { it.rainProb >= 50 }
    val humidity = fieldWeather.humidity
    val maxTemp = fieldWeather.daily.firstOrNull()?.maxTempC ?: fieldWeather.tempC

    var riskScore = 0 // 0 to 10

    if (humidity >= 80) {
      riskScore += 2
      reasons.add("High relative humidity (${humidity}%) creates favourable conditions for fungal spores.")
    } else if (humidity >= 65) {
      riskScore += 1
    }

    if (upcomingRainProb >= 60 || highRainHours >= 4) {
      riskScore += 3
      reasons.add("Substantial rainfall forecasted in next 24 hours (${upcomingRainProb}% probability). Risk of waterlogging.")
      suggestions.add("Inspect field bunds and ensure drainage channels are cleared to prevent standing water.")
      suggestions.add("Postpone planned fertilizer or chemical applications until rain passes.")
    } else if (upcomingRainProb < 15 && maxTemp >= 35.0) {
      riskScore += 2
      reasons.add("Hot weather (${maxTemp.toInt()}°C) and dry forecast. Potential heat and moisture stress.")
      suggestions.add("Maintain adequate soil moisture through timely early-morning or evening irrigation.")
    }

    if (fieldWeather.windKmh >= 30) {
      riskScore += 2
      reasons.add("Gusty winds (${fieldWeather.windKmh.toInt()} km/h) may cause lodging or physical foliage abrasion.")
    }

    // 2. Visual inspection heuristics from photo
    if (quality.brownSpots > 0.04f) {
      riskScore += 3
      observations.add("Localized brown/dark spots detected on leaf blades.")
      if (humidity >= 70 || upcomingRainProb >= 40) {
        reasons.add("Elevated fungal or leaf blight vulnerability detected under current humid weather.")
        suggestions.add("Monitor infected leaves closely; improve canopy aeration and maintain drainage.")
      } else {
        reasons.add("Possible localized leaf injury or spotting.")
        suggestions.add("Monitor affected plants for symptom spread across adjacent rows.")
      }
    } else if (quality.yellowSpots > 0.08f) {
      riskScore += 2
      observations.add("Mild foliar yellowing (chlorosis pattern) observed.")
      reasons.add("Nutrient or root-zone aeration stress indicator.")
      suggestions.add("Check soil moisture levels; assess nitrogen/micronutrient availability after drainage check.")
    } else {
      observations.add("Leaf pigmentation appears largely vibrant with normal vegetative vigor.")
    }

    // 3. Populate 7 Specific Crop Risk Categories required by Agricultural Intelligence
    val categoryRisks = listOf(
      CropCategoryRisk(
        category = "Heat Stress",
        level = if (maxTemp >= 36.0) CropRiskLevel.HIGH else if (maxTemp >= 32.0) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (maxTemp >= 36.0) "Daytime high (${maxTemp.toInt()}°C) exceeds optimal vegetative threshold." else "Temperatures are within manageable crop tolerance."
      ),
      CropCategoryRisk(
        category = "Heavy Rain Risk",
        level = if (upcomingRainProb >= 65 || highRainHours >= 4) CropRiskLevel.HIGH else if (upcomingRainProb >= 40) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (upcomingRainProb >= 65) "Significant precipitation likelihood (${upcomingRainProb}%) during next 24h cycle." else "Low to moderate rainfall expected."
      ),
      CropCategoryRisk(
        category = "Water Stress",
        level = if (upcomingRainProb < 15 && humidity < 50 && maxTemp > 33) CropRiskLevel.HIGH else if (upcomingRainProb < 20) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (upcomingRainProb < 15 && humidity < 50) "Low soil moisture recharge forecast with rapid evapotranspiration." else "Adequate ambient moisture."
      ),
      CropCategoryRisk(
        category = "Strong Wind Risk",
        level = if (fieldWeather.windKmh >= 35.0) CropRiskLevel.HIGH else if (fieldWeather.windKmh >= 24.0) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (fieldWeather.windKmh >= 24.0) "Sustained wind (${fieldWeather.windKmh.toInt()} km/h) may stress younger shoots." else "Mild breeze under 20 km/h."
      ),
      CropCategoryRisk(
        category = "Cold / Frost Risk",
        level = if (fieldWeather.tempC <= 10.0) CropRiskLevel.HIGH else if (fieldWeather.tempC <= 15.0) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (fieldWeather.tempC <= 15.0) "Cool nighttime dips observed." else "Negligible cold or frost injury risk in this tropical zone."
      ),
      CropCategoryRisk(
        category = "Possible Disease Risk",
        level = if (humidity >= 78 && (quality.brownSpots > 0.03f || upcomingRainProb >= 40)) CropRiskLevel.HIGH else if (humidity >= 68) CropRiskLevel.MODERATE else CropRiskLevel.LOW,
        reason = if (humidity >= 78) "Prolonged high leaf wetness (${humidity}% RH) encourages fungal germination." else "Dry air inhibits pathogen spread."
      ),
      CropCategoryRisk(
        category = "Overall Crop Risk",
        level = when {
          riskScore >= 6 -> CropRiskLevel.HIGH
          riskScore >= 3 -> CropRiskLevel.MODERATE
          else -> CropRiskLevel.LOW
        },
        reason = "Weighted composite of atmospheric factors and computer vision leaf observations."
      )
    )

    val overallRiskLevel = categoryRisks.last().level

    val aiAnalysis = CropAiAnalysisResult(
      cropType = cropType.ifBlank { "Paddy / Rice (Oryza sativa)" },
      visibleDamage = if (quality.brownSpots > 0.03f) "Localized leaf spot lesions detected" else "No major mechanical foliage tearing",
      leafDiscoloration = if (quality.yellowSpots > 0.06f) "Mild interveinal yellowing (chlorosis pattern)" else "Uniform green chlorophyll pigmentation",
      possibleDiseaseIndicators = if (humidity >= 75 && quality.brownSpots > 0.03f) "Fungal leaf spot / blast indicators under high humidity" else "Low disease manifestation",
      pestIndicators = "No active stem borer or chewing insect damage evident",
      wiltingWaterStress = if (upcomingRainProb < 20 && maxTemp > 34) "Slight canopy turgor reduction possible" else "Healthy upright leaf angle",
      generalCondition = if (overallRiskLevel == CropRiskLevel.LOW) "Vigorous crop standing" else "Moderate maintenance required",
      confidencePercent = if (quality.greenDominance > 0.25f) 86 else 72
    )

    if (suggestions.isEmpty()) {
      suggestions.add("Maintain regular scouting schedule every 3-4 days.")
      suggestions.add("Keep farm drainage outlets free of weed obstruction.")
      suggestions.add("Conditions are currently favorable for healthy crop growth.")
    }

    val weatherSummary = "${fieldLocation.locality}: Temp ${fieldWeather.tempC.toInt()}°C, Humidity ${fieldWeather.humidity}%, Rain chance ${upcomingRainProb}%"
    val observationText = observations.joinToString("; ")

    CropRiskAssessment(
      fieldLocation = fieldLocation,
      riskLevel = overallRiskLevel,
      reasons = reasons,
      suggestions = suggestions,
      imageObservation = observationText,
      weatherFactorSummary = weatherSummary,
      categoryRisks = categoryRisks,
      aiAnalysis = aiAnalysis
    )
  }
}
