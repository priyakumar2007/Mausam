package com.example.domain

import com.example.data.model.BeachSafetyDetail
import com.example.data.model.CropRiskAssessment
import com.example.data.model.TravelRouteComparison
import com.example.data.model.WeatherData
import java.util.Locale

class WeatherAssistantEngine {

  fun answerQuestion(
    query: String,
    currentWeather: WeatherData?,
    cropAssessment: CropRiskAssessment?,
    travelComparison: TravelRouteComparison?,
    beachDetails: List<BeachSafetyDetail>?,
    lang: String = "en"
  ): String {
    val q = query.lowercase(Locale.ROOT).trim()

    // 1. Running / Fitness query
    if (q.contains("run") || q.contains("walk") || q.contains("jog") || q.contains("exercise") || q.contains("workout")) {
      if (currentWeather == null) return getGenericError(lang)
      val rainProb = currentWeather.hourly.take(4).maxOfOrNull { it.rainProb } ?: 10
      val temp = currentWeather.tempC.toInt()

      return when (lang) {
        "ta" -> if (rainProb < 30 && temp < 32) {
          "ஆம், நீங்கள் ஓட்டப்பயிற்சி செல்லலாம்! தற்போதைய வெப்பநிலை $temp°C, அடுத்த 3 மணிநேரத்தில் மழை வாய்ப்பு மிகக் குறைவு ($rainProb%). காலை 6:00 - 7:30 அல்லது மாலை 5:45 மணிக்கு மேல் சிறந்தது."
        } else {
          "தற்போது வெப்பம் $temp°C மற்றும் ஈரப்பதம் ${currentWeather.humidity}%. மழை வாய்ப்பு $rainProb%. அதிகாலை அல்லது மாலை வேளையில் ஓடுவது சிறந்தது."
        }
        "tanglish" -> if (rainProb < 30 && temp < 32) {
          "Kandipa run poga nalla time! Temp $temp°C, rain chance $rainProb% dhaan. Morning 6:00 - 7:30 AM or evening 5:45 PM super comfortable-ah irukkum."
        } else {
          "Ipo temp $temp°C, humidity ${currentWeather.humidity}%. Rain chance $rainProb%. Konjam neram kazhichu evening workout panna nallathu."
        }
        else -> if (rainProb < 30 && temp < 32) {
          "Yes, it's a good window for a run! Current temperature is $temp°C with low rain probability ($rainProb% in the next 3 hours). The most comfortable window is 06:00 AM – 07:30 AM or after 05:45 PM."
        } else {
          "Conditions are currently muggy ($temp°C, ${currentWeather.humidity}% humidity, $rainProb% rain chance). Early morning or late evening will be significantly more comfortable."
        }
      }
    }

    // 2. Rain query
    if (q.contains("rain") || q.contains("umbrella") || q.contains("mazhai") || q.contains("shower")) {
      if (currentWeather == null) return getGenericError(lang)
      val rainHour = currentWeather.hourly.take(12).firstOrNull { it.rainProb >= 50 }
      val maxRain = currentWeather.hourly.take(12).maxOfOrNull { it.rainProb } ?: 10

      return when (lang) {
        "ta" -> if (rainHour != null) {
          "இன்று ${rainHour.timeLabel} மணிக்கு மழை பெய்ய அதிக வாய்ப்புள்ளது (${rainHour.rainProb}% வாய்ப்பு). குடை அல்லது ரெயின்கோட் எடுத்துச் செல்வது நல்லது."
        } else {
          "இன்று மழை வாய்ப்பு குறைவு (அதிகபட்சம் $maxRain%). வானம் பெரும்பாலும் தெளிவான சூழலில் இருக்கும்."
        }
        "tanglish" -> if (rainHour != null) {
          "Innaiku ${rainHour.timeLabel}-ku apram rain vara nalla chance irukku (${rainHour.rainProb}% probability). Umbrella kandipa eduthukonga."
        } else {
          "Innaiku rain chance romba kami (max $maxRain%). Dry weather-ah irukum."
        }
        else -> if (rainHour != null) {
          "Rain is likely around ${rainHour.timeLabel} with a ${rainHour.rainProb}% probability. Keeping an umbrella handy is recommended."
        } else {
          "Rain probability remains low today (peak $maxRain%). Expect predominantly dry conditions."
        }
      }
    }

    // 3. Crop / Agriculture query
    if (q.contains("crop") || q.contains("farm") || q.contains("field") || q.contains("payir") || q.contains("vayal") || q.contains("paddy")) {
      if (cropAssessment != null) {
        val field = cropAssessment.fieldLocation.locality
        val level = cropAssessment.riskLevel.label
        val reason = cropAssessment.reasons.firstOrNull() ?: "Normal moisture conditions"
        val sug = cropAssessment.suggestions.firstOrNull() ?: "Maintain normal irrigation"

        return when (lang) {
          "ta" -> "உங்கள் $field நிலத்திற்கான பயிர் அபாய நிலை: $level. காரணம்: $reason. ஆலோசனை: $sug."
          "tanglish" -> "Unga $field vayaloda crop risk: $level. Reason: $reason. Suggestion: $sug."
          else -> "For your field at $field, crop risk is currently $level. Primary factor: $reason. Recommended action: $sug."
        }
      } else {
        val loc = currentWeather?.location?.locality ?: "your area"
        return when (lang) {
          "ta" -> "$loc பகுதியில் தற்போதைய வானிலை: வெப்பநிலை ${currentWeather?.tempC?.toInt()}°C, ஈரப்பதம் ${currentWeather?.humidity}%. நேரடி பயிர் புகைப்படத்தை பதிவேற்றி துல்லியமான அபாயத்தை அறியலாம்."
          "tanglish" -> "$loc area-la temp ${currentWeather?.tempC?.toInt()}°C, humidity ${currentWeather?.humidity}%. Vayal photo upload panni accurate risk paakalam."
          else -> "In $loc, humidity is ${currentWeather?.humidity}% and temp is ${currentWeather?.tempC?.toInt()}°C. Upload or capture a crop leaf photo in the Crop Risk section for tailored field analysis."
        }
      }
    }

    // 4. Beach query
    if (q.contains("beach") || q.contains("wave") || q.contains("sea") || q.contains("ocean") || q.contains("kadal")) {
      if (!beachDetails.isNullOrEmpty()) {
        val nearest = beachDetails[0]
        val alt = beachDetails.getOrNull(1)

        return when (lang) {
          "ta" -> if (nearest.safetyStatus.isRisky && alt != null) {
            "அருகிலுள்ள ${nearest.beachName} கடற்கரையில் அலைகள் சற்று சீற்றமாக உள்ளன (${nearest.waveHeight}m அலை உயரம்). ஆனால் ${alt.beachName} கடற்கரை அமைதியான அலைகளுடன் பாதுகாப்பானதாக உள்ளது."
          } else {
            "${nearest.beachName} கடற்கரை தற்போது ${nearest.safetyStatus.label} சூழலில் உள்ளது. அலை உயரம் ${nearest.waveHeight}m, காற்று வேகம் ${nearest.windSpeedKmh.toInt()} km/h."
          }
          "tanglish" -> if (nearest.safetyStatus.isRisky && alt != null) {
            "Pakkatula irukura ${nearest.beachName}-la alai konjam jaasthi (${nearest.waveHeight}m). But ${alt.beachName}-la waves calm-ah irukku, anga poga recommend panrom."
          } else {
            "${nearest.beachName} condition: ${nearest.safetyStatus.label}. Wave height ${nearest.waveHeight}m, wind ${nearest.windSpeedKmh.toInt()} km/h."
          }
          else -> if (nearest.safetyStatus.isRisky && alt != null) {
            "Conditions at nearest ${nearest.beachName} have elevated waves (${nearest.waveHeight}m). However, nearby ${alt.beachName} has calmer water (${alt.waveHeight}m) and is more suitable today."
          } else {
            "${nearest.beachName} currently has ${nearest.safetyStatus.label}. Waves are ${nearest.waveHeight}m with wind at ${nearest.windSpeedKmh.toInt()} km/h."
          }
        }
      } else {
        return "Nearest beach conditions are currently moderate. Mahabalipuram and Covelong are within short driving distance."
      }
    }

    // 5. Travel / Route query
    if (q.contains("trip") || q.contains("travel") || q.contains("route") || q.contains("traffic") || q.contains("drive") || q.contains("chennai")) {
      if (travelComparison != null) {
        return travelComparison.advisory
      } else {
        return "For travel planning, enter your source and destination in the Travel Planner tab to compare shortest vs alternative bypass routes with live weather checkpoints."
      }
    }

    // Default friendly weather briefing
    return if (currentWeather != null) {
      PersonalizationEngine().generateInsight(currentWeather, lang)
    } else {
      "MAUSAM is ready to help you plan around live weather, crop conditions, travel routes, and beach safety!"
    }
  }

  private fun getGenericError(lang: String): String {
    return when (lang) {
      "ta" -> "வானிலை தகவல் பெற முடியவில்லை. சற்று நேரம் கழித்து முயற்சிக்கவும்."
      "tanglish" -> "Weather data load aagala. Konjam kazhichu try pannunga."
      else -> "Weather data is currently loading. Please check in a moment."
    }
  }
}
