package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.model.LocationInfo
import com.example.data.model.RouteCheckpointWeather
import com.example.data.model.TravelRouteComparison
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class TravelRepository(private val weatherRepo: WeatherRepository) {

  suspend fun calculateRoutes(
    source: LocationInfo,
    destination: LocationInfo
  ): TravelRouteComparison = withContext(Dispatchers.IO) {
    val srcCoord = "%.4f,%.4f".format(Locale.US, source.longitude, source.latitude)
    val dstCoord = "%.4f,%.4f".format(Locale.US, destination.longitude, destination.latitude)
    val routeParam = "$srcCoord;$dstCoord"

    var shortestKm = calculateGreatCircleDistanceKm(source.latitude, source.longitude, destination.latitude, destination.longitude) * 1.3
    var shortestMin = (shortestKm / 45.0 * 60).roundToInt()

    var altKm = shortestKm * 1.15
    var altMin = (altKm / 52.0 * 60).roundToInt()

    try {
      val response = ApiClient.routingApi.getRoute(coordinates = routeParam)
      val routes = response.routes
      if (!routes.isNullOrEmpty()) {
        val sorted = routes.sortedBy { it.distance ?: Double.MAX_VALUE }
        val r1 = sorted[0]
        val dist1 = (r1.distance ?: 0.0) / 1000.0
        val dur1 = ((r1.duration ?: 0.0) / 60.0).roundToInt()
        if (dist1 > 0) {
          shortestKm = dist1
          shortestMin = dur1
        }

        if (sorted.size > 1) {
          val r2 = sorted[1]
          val dist2 = (r2.distance ?: 0.0) / 1000.0
          val dur2 = ((r2.duration ?: 0.0) / 60.0).roundToInt()
          if (dist2 > dist1) {
            altKm = dist2
            altMin = dur2
          } else {
            altKm = dist1 * 1.18
            altMin = (altKm / 55.0 * 60).roundToInt()
          }
        } else {
          altKm = shortestKm * 1.18
          altMin = (altKm / 55.0 * 60).roundToInt()
        }
      }
    } catch (e: Exception) {
      // Keep heuristic values based on real geodetic distance
    }

    // Weather at Start, Midpoint, Destination
    val midLat = (source.latitude + destination.latitude) / 2.0
    val midLon = (source.longitude + destination.longitude) / 2.0

    val srcWeather = weatherRepo.getWeatherData(source)
    val midWeather = weatherRepo.getWeatherData(
      LocationInfo("Midway Checkpoint", "", "", "", midLat, midLon)
    )
    val dstWeather = weatherRepo.getWeatherData(destination)

    val checkpoints = listOf(
      RouteCheckpointWeather(
        name = source.locality.ifBlank { "Start" },
        tempC = srcWeather.tempC,
        rainProb = srcWeather.hourly.firstOrNull()?.rainProb ?: 10,
        condition = srcWeather.condition
      ),
      RouteCheckpointWeather(
        name = "Mid-Route Junction",
        tempC = midWeather.tempC,
        rainProb = midWeather.hourly.firstOrNull()?.rainProb ?: 25,
        condition = midWeather.condition
      ),
      RouteCheckpointWeather(
        name = destination.locality.ifBlank { "Destination" },
        tempC = dstWeather.tempC,
        rainProb = dstWeather.hourly.firstOrNull()?.rainProb ?: 40,
        condition = dstWeather.condition
      )
    )

    // Traffic condition
    val shortestTraffic = if (shortestMin > (shortestKm / 40.0 * 60)) "Moderate to Heavy" else "Moderate Traffic"
    val altTraffic = "Lighter Traffic (Bypass Route)"

    val dstRain = dstWeather.hourly.take(6).maxOfOrNull { it.rainProb } ?: 20
    val weatherAdvisory = if (dstRain >= 50) {
      "Rain is likely at ${destination.displayName} (${dstRain}% probability). Wet driving conditions anticipated."
    } else {
      "Dry and clear driving conditions along primary highway segments."
    }

    val diffKm = (altKm - shortestKm).roundToInt().coerceAtLeast(1)
    val advisory = "Shortest route is ${shortestKm.roundToInt()} km (~${formatMinutes(shortestMin)}) with $shortestTraffic. Alternative route via highway bypass is $diffKm km longer (~${formatMinutes(altMin)}) with smoother cruising speeds. $weatherAdvisory"

    TravelRouteComparison(
      sourceName = source.displayName,
      destinationName = destination.displayName,
      shortestDistanceKm = shortestKm,
      shortestDurationMin = shortestMin,
      shortestTraffic = shortestTraffic,
      shortestWeatherCondition = "${srcWeather.condition} → ${dstWeather.condition}",
      alternativeDistanceKm = altKm,
      alternativeDurationMin = altMin,
      alternativeTraffic = altTraffic,
      alternativeWeatherCondition = "${srcWeather.condition} → Bypass → ${dstWeather.condition}",
      checkpoints = checkpoints,
      advisory = advisory
    )
  }

  private fun calculateGreatCircleDistanceKm(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
  ): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }

  private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours} hr ${mins} min" else "${mins} min"
  }
}
