package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ----------------- API Response Models -----------------

@JsonClass(generateAdapter = true)
data class OpenMeteoWeatherResponse(
  val latitude: Double? = null,
  val longitude: Double? = null,
  val timezone: String? = null,
  val current: CurrentWeatherDto? = null,
  val hourly: HourlyWeatherDto? = null,
  val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
  val time: String? = null,
  val temperature_2m: Double? = null,
  val relative_humidity_2m: Int? = null,
  val apparent_temperature: Double? = null,
  val is_day: Int? = null,
  val precipitation: Double? = null,
  val weather_code: Int? = null,
  val cloud_cover: Int? = null,
  val pressure_msl: Double? = null,
  val wind_speed_10m: Double? = null,
  val wind_direction_10m: Int? = null,
  val uv_index: Double? = null
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
  val time: List<String>? = null,
  val temperature_2m: List<Double>? = null,
  val relative_humidity_2m: List<Int>? = null,
  val precipitation_probability: List<Int>? = null,
  val weather_code: List<Int>? = null,
  val wind_speed_10m: List<Double>? = null,
  val uv_index: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
  val time: List<String>? = null,
  val weather_code: List<Int>? = null,
  val temperature_2m_max: List<Double>? = null,
  val temperature_2m_min: List<Double>? = null,
  val sunrise: List<String>? = null,
  val sunset: List<String>? = null,
  val precipitation_sum: List<Double>? = null,
  val precipitation_probability_max: List<Int>? = null,
  val wind_speed_10m_max: List<Double>? = null,
  val uv_index_max: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
  val latitude: Double? = null,
  val longitude: Double? = null,
  val current: CurrentAirQualityDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityDto(
  val time: String? = null,
  val european_aqi: Int? = null,
  val us_aqi: Int? = null,
  val pm10: Double? = null,
  val pm2_5: Double? = null,
  val carbon_monoxide: Double? = null,
  val nitrogen_dioxide: Double? = null,
  val sulphur_dioxide: Double? = null,
  val ozone: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoMarineResponse(
  val latitude: Double? = null,
  val longitude: Double? = null,
  val current: CurrentMarineDto? = null,
  val daily: DailyMarineDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentMarineDto(
  val time: String? = null,
  val wave_height: Double? = null,
  val wave_direction: Int? = null,
  val wave_period: Double? = null,
  val wind_wave_height: Double? = null,
  val wind_wave_direction: Int? = null,
  val wind_wave_period: Double? = null
)

@JsonClass(generateAdapter = true)
data class DailyMarineDto(
  val time: List<String>? = null,
  val wave_height_max: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
  val results: List<GeocodingResultDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
  val id: Long? = null,
  val name: String? = null,
  val latitude: Double? = null,
  val longitude: Double? = null,
  val elevation: Double? = null,
  @Json(name = "admin1") val state: String? = null,
  @Json(name = "admin2") val district: String? = null,
  val country: String? = null
)

@JsonClass(generateAdapter = true)
data class OsrmRouteResponse(
  val code: String? = null,
  val routes: List<OsrmRouteDto>? = null
)

@JsonClass(generateAdapter = true)
data class OsrmRouteDto(
  val distance: Double? = null, // in meters
  val duration: Double? = null, // in seconds
  val geometry: String? = null
)

// ----------------- Domain UI Models -----------------

data class LocationInfo(
  val locality: String,
  val district: String = "",
  val state: String = "",
  val country: String = "India",
  val latitude: Double,
  val longitude: Double,
  val isDetectedGps: Boolean = false
) {
  val displayName: String
    get() = if (locality.isNotBlank()) locality else "Unknown Locality"

  val fullAddress: String
    get() {
      val parts = listOfNotNull(
        district.takeIf { it.isNotBlank() },
        state.takeIf { it.isNotBlank() },
        country.takeIf { it.isNotBlank() }
      )
      return if (parts.isNotEmpty()) parts.joinToString(", ") else ""
    }
}

data class HourlyForecastItem(
  val timeLabel: String,
  val hourInt: Int,
  val tempC: Double,
  val weatherCode: Int,
  val condition: String,
  val rainProb: Int,
  val windKmh: Double,
  val humidity: Int,
  val uv: Double
)

data class DailyForecastItem(
  val dateLabel: String,
  val dayOfWeek: String,
  val weatherCode: Int,
  val condition: String,
  val maxTempC: Double,
  val minTempC: Double,
  val rainProb: Int,
  val windKmh: Double,
  val uvMax: Double,
  val sunrise: String,
  val sunset: String
)

data class WeatherData(
  val location: LocationInfo,
  val tempC: Double,
  val feelsLikeC: Double,
  val condition: String,
  val weatherCode: Int,
  val isDay: Boolean,
  val humidity: Int,
  val windKmh: Double,
  val windDirectionDeg: Int,
  val windDirectionText: String,
  val uvIndex: Double,
  val visibilityKm: Double,
  val pressureHpa: Double,
  val cloudCoverPct: Int,
  val precipitationMm: Double,
  val sunrise: String,
  val sunset: String,
  val hourly: List<HourlyForecastItem>,
  val daily: List<DailyForecastItem>,
  val timestamp: Long = System.currentTimeMillis()
)

data class AirQualityData(
  val aqi: Int,
  val pm25: Double,
  val pm10: Double,
  val no2: Double,
  val o3: Double,
  val status: String,
  val advisory: String
)

enum class LifestyleType(val id: String, val title: String, val emoji: String) {
  HEALTH("health", "Health", "❤️"),
  FITNESS("fitness", "Fitness", "🏃"),
  BEACH("beach", "Beach", "🏖️"),
  TRAVEL("travel", "Travel", "✈️"),
  FAMILY("family", "Family", "👨‍👩‍👧"),
  AGRICULTURE("agriculture", "Agriculture", "🌱"),
  COMMUTER("commuter", "Commuter", "🚗"),
  EVENTS("events", "Events", "🎉")
}

data class FitnessInsight(
  val comfortScore: Int, // 0 - 100
  val bestRunningTime: String,
  val bestWalkingTime: String,
  val explanation: String,
  val status: String
)

data class HealthInsight(
  val outdoorComfort: String,
  val aqiLevel: String,
  val uvRisk: String,
  val pollenInfo: String = "Pollen information is unavailable for this location.",
  val suggestions: List<String>
)

enum class CropRiskLevel(val label: String, val badgeColorHex: Long) {
  LOW("LOW", 0xFF10B981),
  MODERATE("MODERATE", 0xFFF59E0B),
  HIGH("HIGH", 0xFFF97316),
  VERY_HIGH("VERY HIGH", 0xFFEF4444)
}

data class CropCategoryRisk(
  val category: String, // Heat Stress, Heavy Rain Risk, Water Stress, Strong Wind Risk, Cold/Frost Risk, Possible Disease Risk
  val level: CropRiskLevel,
  val reason: String
)

data class CropAiAnalysisResult(
  val cropType: String = "Paddy / Rice (Oryza sativa)",
  val visibleDamage: String = "Minor localized leaf blade scarring",
  val leafDiscoloration: String = "Slight marginal chlorosis observed",
  val possibleDiseaseIndicators: String = "Possible early fungal spore vulnerability under humid canopy",
  val pestIndicators: String = "No critical pest chew patterns detected",
  val wiltingWaterStress: String = "Turgor pressure steady; low wilting indication",
  val generalCondition: String = "Good vegetative development",
  val confidencePercent: Int = 84,
  val observationBadge: String = "AI-assisted observation",
  val expertVerificationNote: String = "Needs expert field verification",
  val disclaimer: String = "AI-assisted crop risk assessment. This result is not a substitute for professional agricultural advice."
)

data class CropRiskAssessment(
  val fieldLocation: LocationInfo,
  val riskLevel: CropRiskLevel,
  val reasons: List<String>,
  val suggestions: List<String>,
  val imageObservation: String,
  val weatherFactorSummary: String,
  val isLabDiagnosisWarning: String = "Possible risk — AI-assisted visual observation, not a laboratory diagnosis.",
  val categoryRisks: List<CropCategoryRisk> = emptyList(),
  val aiAnalysis: CropAiAnalysisResult = CropAiAnalysisResult()
)

data class FamilyModeInsight(
  val morningSchoolWeather: String,
  val commuteWeather: String,
  val rainProbabilityCommute: Int,
  val thunderstormAlert: Boolean,
  val suggestions: List<String>
)

data class CommuterModeInsight(
  val visibilityCondition: String,
  val roadSurfaceRisk: String,
  val rainImpact: String,
  val windAlert: String,
  val commuteTips: List<String>
)

data class EventsModeInsight(
  val eventName: String = "Outdoor Gathering",
  val isOutdoor: Boolean = true,
  val outdoorSuitabilityScore: Int = 82, // 0-100
  val temperatureFeel: String = "Pleasant",
  val rainProbability: Int = 15,
  val windGust: String = "Moderate breeze",
  val advisory: String = "Great conditions for outdoor events during morning and evening windows.",
  val warnings: List<String> = emptyList()
)

data class TravelRouteComparison(
  val sourceName: String,
  val destinationName: String,
  val shortestDistanceKm: Double,
  val shortestDurationMin: Int,
  val shortestTraffic: String,
  val shortestWeatherCondition: String,
  val alternativeDistanceKm: Double,
  val alternativeDurationMin: Int,
  val alternativeTraffic: String,
  val alternativeWeatherCondition: String,
  val checkpoints: List<RouteCheckpointWeather>,
  val advisory: String,
  val travelDate: String = "Today",
  val travelTime: String = "Now"
)

data class RouteCheckpointWeather(
  val name: String,
  val tempC: Double,
  val rainProb: Int,
  val condition: String
)

enum class BeachSafetyStatus(val label: String, val isRisky: Boolean) {
  SUITABLE("Suitable Conditions", false),
  CAUTION("Caution", true),
  HIGH_RISK("High-Risk Conditions", true)
}

data class BeachSafetyDetail(
  val beachName: String,
  val distanceKm: Double,
  val locationDesc: String,
  val airTempC: Double,
  val waterTempC: Double?,
  val waveHeightM: Double,
  val wavePeriodSec: Double,
  val waveDirection: String,
  val windSpeedKmh: Double,
  val windDirection: String,
  val rainProb: Int,
  val uvIndex: Double,
  val safetyStatus: BeachSafetyStatus,
  val riskReasons: List<String>,
  val alternativeBeachName: String? = null,
  val alternativeBeachReason: String? = null,
  val alternativeDayDate: String? = null,
  val alternativeDayReason: String? = null,
  val tideStatus: String = "Mid Tide (Rising)",
  val beachComfort: String = "Comfortable coastal breeze"
) {
  val waveHeight: Double get() = waveHeightM
  val wavePeriod: Double get() = wavePeriodSec
}

data class MausamAlertItem(
  val id: String,
  val title: String,
  val message: String,
  val severity: String, // HIGH, MODERATE, LOW
  val category: String, // Heavy Rain, Thunderstorm, Extreme Heat, High UV, Strong Wind, Poor Visibility, Agriculture Risk, Travel Risk, Beach Risk
  val isOfficial: Boolean,
  val source: String,
  val affectedLocation: String = "Local Area",
  val expectedPeriod: String = "Next 6 hours",
  val recommendedAction: String = "Monitor local conditions",
  val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
  val id: String,
  val isUser: Boolean,
  val text: String,
  val contextTag: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)
