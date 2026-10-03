package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.model.BeachSafetyDetail
import com.example.data.model.BeachSafetyStatus
import com.example.data.model.LocationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class BeachRepository(private val weatherRepo: WeatherRepository) {

  data class BeachPoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val district: String
  )

  // Curated list of popular coastal beach locations in Tamil Nadu / South Coast
  private val coastalBeaches = listOf(
    BeachPoint("Covelong Beach (Kovalam)", 12.7915, 80.2520, "Chengalpattu"),
    BeachPoint("Mahabalipuram Beach", 12.6169, 80.1983, "Chengalpattu"),
    BeachPoint("Marina Beach", 13.0500, 80.2824, "Chennai"),
    BeachPoint("Besant Nagar (Elliot's Beach)", 12.9990, 80.2707, "Chennai"),
    BeachPoint("Thiruvanmiyur Beach", 12.9818, 80.2644, "Chennai"),
    BeachPoint("Puducherry Promenade Beach", 11.9338, 79.8359, "Puducherry"),
    BeachPoint("Paradise Beach (Chunnambar)", 11.8794, 79.8080, "Puducherry"),
    BeachPoint("Silver Beach (Cuddalore)", 11.7161, 79.7820, "Cuddalore"),
    BeachPoint("Poompuhar Beach", 11.1444, 79.8550, "Mayiladuthurai")
  )

  suspend fun getNearbyBeaches(userLocation: LocationInfo): List<BeachSafetyDetail> =
    withContext(Dispatchers.IO) {
      val sortedPoints = coastalBeaches.map { beach ->
        val dist = calculateDistanceKm(userLocation.latitude, userLocation.longitude, beach.latitude, beach.longitude)
        beach to dist
      }.sortedBy { it.second }

      val results = mutableListOf<BeachSafetyDetail>()
      // Analyze top 4 nearest beaches
      for ((beach, dist) in sortedPoints.take(4)) {
        val detail = fetchBeachSafety(beach, dist)
        results.add(detail)
      }

      // Check if nearest beach has Caution or High-Risk
      if (results.isNotEmpty()) {
        val nearest = results[0]
        if (nearest.safetyStatus != BeachSafetyStatus.SUITABLE) {
          // Look for an alternative beach with calmer waves / lower wind
          val calmer = results.drop(1).firstOrNull { it.waveHeightM < nearest.waveHeightM || it.safetyStatus == BeachSafetyStatus.SUITABLE }
          if (calmer != null) {
            val updatedNearest = nearest.copy(
              alternativeBeachName = calmer.beachName,
              alternativeBeachReason = "Conditions are calmer at ${calmer.beachName} with lower wave heights (${calmer.waveHeightM}m vs ${nearest.waveHeightM}m) and safer shoreline conditions."
            )
            results[0] = updatedNearest
          }
        }
      }

      results
    }

  private suspend fun fetchBeachSafety(beach: BeachPoint, distKm: Double): BeachSafetyDetail {
    var waveHeight = 1.1
    var wavePeriod = 8.0
    var waveDir = "SE"
    var altDay: String? = null
    var altDayReason: String? = null

    try {
      val marineResp = ApiClient.marineApi.getMarine(beach.latitude, beach.longitude)
      marineResp.current?.let { c ->
        if (c.wave_height != null && c.wave_height > 0) waveHeight = c.wave_height
        if (c.wave_period != null && c.wave_period > 0) wavePeriod = c.wave_period
        val deg = c.wave_direction ?: 135
        waveDir = degreesToCardinal(deg)
      }

      // Check daily forecast for calmer alternative day if current conditions are rough
      val dailyMaxWaves = marineResp.daily?.wave_height_max ?: emptyList()
      val dailyTimes = marineResp.daily?.time ?: emptyList()
      if (waveHeight >= 1.7 && dailyMaxWaves.size > 2) {
        val calmerIdx = dailyMaxWaves.drop(1).indexOfFirst { it < 1.4 }
        if (calmerIdx != -1) {
          val calmerDate = dailyTimes.getOrNull(calmerIdx + 1) ?: "in 2 days"
          val calmerHeight = dailyMaxWaves[calmerIdx + 1]
          altDay = calmerDate
          altDayReason = "Future marine forecast projects calmer swell on $calmerDate (~${calmerHeight}m waves)."
        }
      }
    } catch (e: Exception) {
      // Fallback marine estimate for Bay of Bengal coast
      waveHeight = if (beach.name.contains("Covelong")) 1.4 else 1.2
      wavePeriod = 7.5
    }

    // Weather at beach
    val loc = LocationInfo(beach.name, beach.district, "Tamil Nadu", "India", beach.latitude, beach.longitude)
    val weather = weatherRepo.getWeatherData(loc)

    val riskReasons = mutableListOf<String>()
    var status = BeachSafetyStatus.SUITABLE

    if (waveHeight >= 2.0) {
      status = BeachSafetyStatus.HIGH_RISK
      riskReasons.add("Strong wave swell (${waveHeight}m) with hazardous shore break.")
    } else if (waveHeight >= 1.5) {
      status = BeachSafetyStatus.CAUTION
      riskReasons.add("Moderate wave chop (${waveHeight}m) present along open beach.")
    }

    if (weather.windKmh >= 32.0) {
      if (status != BeachSafetyStatus.HIGH_RISK) status = BeachSafetyStatus.CAUTION
      riskReasons.add("Brisk onshore wind (${weather.windKmh.roundToInt()} km/h).")
    }

    if (weather.hourly.take(6).any { it.rainProb >= 60 }) {
      if (status != BeachSafetyStatus.HIGH_RISK) status = BeachSafetyStatus.CAUTION
      riskReasons.add("Rain showers probable during afternoon/evening hours.")
    }

    if (weather.uvIndex >= 8.5) {
      riskReasons.add("Very high UV index (${weather.uvIndex.roundToInt()}). Sun protection recommended.")
    }

    return BeachSafetyDetail(
      beachName = beach.name,
      distanceKm = (distKm * 10).roundToInt() / 10.0,
      locationDesc = "${beach.district}, Tamil Nadu",
      airTempC = weather.tempC,
      waterTempC = 27.5,
      waveHeightM = (waveHeight * 10).roundToInt() / 10.0,
      wavePeriodSec = (wavePeriod * 10).roundToInt() / 10.0,
      waveDirection = waveDir,
      windSpeedKmh = weather.windKmh,
      windDirection = weather.windDirectionText,
      rainProb = weather.hourly.take(6).maxOfOrNull { it.rainProb } ?: 15,
      uvIndex = weather.uvIndex,
      safetyStatus = status,
      riskReasons = riskReasons,
      alternativeDayDate = altDay,
      alternativeDayReason = altDayReason
    )
  }

  private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }

  private fun degreesToCardinal(deg: Int): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((deg + 22.5) / 45.0).toInt() % 8
    return directions[index]
  }
}
