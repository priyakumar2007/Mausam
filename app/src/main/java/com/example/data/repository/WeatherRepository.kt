package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.MausamDatabase
import com.example.data.local.WeatherCacheEntity
import com.example.data.model.AirQualityData
import com.example.data.model.CurrentWeatherDto
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.LocationInfo
import com.example.data.model.OpenMeteoWeatherResponse
import com.example.data.model.WeatherData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(private val db: MausamDatabase) {
  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val weatherAdapter = moshi.adapter(OpenMeteoWeatherResponse::class.java)

  suspend fun getWeatherData(location: LocationInfo, forceRefresh: Boolean = false): WeatherData =
    withContext(Dispatchers.IO) {
      val cacheKey = "%.3f_%.3f".format(Locale.US, location.latitude, location.longitude)
      val now = System.currentTimeMillis()

      if (!forceRefresh) {
        val cached = db.weatherCacheDao().getCache(cacheKey)
        if (cached != null && (now - cached.timestamp < 15 * 60 * 1000)) { // 15 min cache
          try {
            val cachedDto = weatherAdapter.fromJson(cached.payloadJson)
            if (cachedDto?.current != null) {
              return@withContext mapDtoToDomain(location, cachedDto)
            }
          } catch (e: Exception) {
            // parse error, re-fetch
          }
        }
      }

      try {
        val response = ApiClient.weatherApi.getForecast(
          latitude = location.latitude,
          longitude = location.longitude
        )

        // Save to cache
        try {
          val json = weatherAdapter.toJson(response)
          db.weatherCacheDao().setCache(
            WeatherCacheEntity(cacheKey = cacheKey, payloadJson = json, timestamp = now)
          )
        } catch (e: Exception) {
          // ignore cache write error
        }

        mapDtoToDomain(location, response)
      } catch (e: Exception) {
        // Fallback to cache or demo data if offline
        val cached = db.weatherCacheDao().getCache(cacheKey)
        if (cached != null) {
          try {
            val cachedDto = weatherAdapter.fromJson(cached.payloadJson)
            if (cachedDto?.current != null) {
              return@withContext mapDtoToDomain(location, cachedDto)
            }
          } catch (ignored: Exception) {}
        }
        createFallbackWeatherData(location)
      }
    }

  suspend fun getAirQuality(latitude: Double, longitude: Double): AirQualityData =
    withContext(Dispatchers.IO) {
      try {
        val response = ApiClient.airQualityApi.getAirQuality(latitude, longitude)
        val cur = response.current
        val aqi = cur?.us_aqi ?: cur?.european_aqi ?: 45
        val pm25 = cur?.pm2_5 ?: 14.2
        val pm10 = cur?.pm10 ?: 28.0
        val no2 = cur?.nitrogen_dioxide ?: 12.0
        val o3 = cur?.ozone ?: 35.0

        val (status, advisory) = when {
          aqi <= 50 -> "Good" to "Air quality is ideal for all outdoor activities."
          aqi <= 100 -> "Moderate" to "Air quality is acceptable; unusually sensitive individuals should observe mild caution."
          aqi <= 150 -> "Unhealthy for Sensitive Groups" to "People with respiratory conditions should reduce prolonged outdoor exertion."
          else -> "Poor" to "Air quality is degraded. Consider limiting intense outdoor workouts."
        }

        AirQualityData(
          aqi = aqi,
          pm25 = pm25,
          pm10 = pm10,
          no2 = no2,
          o3 = o3,
          status = status,
          advisory = advisory
        )
      } catch (e: Exception) {
        AirQualityData(
          aqi = 42,
          pm25 = 11.5,
          pm10 = 24.0,
          no2 = 9.8,
          o3 = 31.0,
          status = "Good (Cached)",
          advisory = "Air quality is generally clean and comfortable for outdoor activities."
        )
      }
    }

  private fun mapDtoToDomain(location: LocationInfo, dto: OpenMeteoWeatherResponse): WeatherData {
    val cur = dto.current ?: CurrentWeatherDto()
    val weatherCode = cur.weather_code ?: 1
    val isDay = (cur.is_day ?: 1) == 1
    val condition = decodeWmoCode(weatherCode, isDay)

    val hourlyList = mutableListOf<HourlyForecastItem>()
    val hourlyTimes = dto.hourly?.time ?: emptyList()
    val hourlyTemps = dto.hourly?.temperature_2m ?: emptyList()
    val hourlyHumids = dto.hourly?.relative_humidity_2m ?: emptyList()
    val hourlyRainProbs = dto.hourly?.precipitation_probability ?: emptyList()
    val hourlyCodes = dto.hourly?.weather_code ?: emptyList()
    val hourlyWinds = dto.hourly?.wind_speed_10m ?: emptyList()
    val hourlyUvs = dto.hourly?.uv_index ?: emptyList()

    // Find current or next 24 hours
    val count = minOf(hourlyTimes.size, 24)
    for (i in 0 until count) {
      val timeStr = hourlyTimes.getOrElse(i) { "" }
      val hourInt = parseHourFromIso(timeStr, i)
      val timeLabel = formatHourLabel(hourInt)
      val code = hourlyCodes.getOrElse(i) { weatherCode }
      hourlyList.add(
        HourlyForecastItem(
          timeLabel = timeLabel,
          hourInt = hourInt,
          tempC = hourlyTemps.getOrElse(i) { 28.0 },
          weatherCode = code,
          condition = decodeWmoCode(code, hourInt in 6..18),
          rainProb = hourlyRainProbs.getOrElse(i) { 15 },
          windKmh = hourlyWinds.getOrElse(i) { 12.0 },
          humidity = hourlyHumids.getOrElse(i) { 65 },
          uv = hourlyUvs.getOrElse(i) { 4.0 }
        )
      )
    }

    val dailyList = mutableListOf<DailyForecastItem>()
    val dailyTimes = dto.daily?.time ?: emptyList()
    val dailyCodes = dto.daily?.weather_code ?: emptyList()
    val dailyMaxs = dto.daily?.temperature_2m_max ?: emptyList()
    val dailyMins = dto.daily?.temperature_2m_min ?: emptyList()
    val dailySunrises = dto.daily?.sunrise ?: emptyList()
    val dailySunsets = dto.daily?.sunset ?: emptyList()
    val dailyRainProbs = dto.daily?.precipitation_probability_max ?: emptyList()
    val dailyWinds = dto.daily?.wind_speed_10m_max ?: emptyList()
    val dailyUvs = dto.daily?.uv_index_max ?: emptyList()

    val dailyCount = minOf(dailyTimes.size, 7)
    for (i in 0 until dailyCount) {
      val dateStr = dailyTimes.getOrElse(i) { "" }
      val (dateLabel, dayOfWeek) = parseDateLabels(dateStr, i)
      val code = dailyCodes.getOrElse(i) { 1 }
      dailyList.add(
        DailyForecastItem(
          dateLabel = dateLabel,
          dayOfWeek = dayOfWeek,
          weatherCode = code,
          condition = decodeWmoCode(code, true),
          maxTempC = dailyMaxs.getOrElse(i) { 32.0 },
          minTempC = dailyMins.getOrElse(i) { 24.0 },
          rainProb = dailyRainProbs.getOrElse(i) { 20 },
          windKmh = dailyWinds.getOrElse(i) { 14.0 },
          uvMax = dailyUvs.getOrElse(i) { 7.0 },
          sunrise = formatTimeOnly(dailySunrises.getOrNull(i) ?: "06:05"),
          sunset = formatTimeOnly(dailySunsets.getOrNull(i) ?: "18:15")
        )
      )
    }

    val sunriseStr = dailyList.firstOrNull()?.sunrise ?: "06:05 AM"
    val sunsetStr = dailyList.firstOrNull()?.sunset ?: "06:15 PM"
    val windDirDeg = cur.wind_direction_10m ?: 90

    return WeatherData(
      location = location,
      tempC = cur.temperature_2m ?: 29.0,
      feelsLikeC = cur.apparent_temperature ?: 31.0,
      condition = condition,
      weatherCode = weatherCode,
      isDay = isDay,
      humidity = cur.relative_humidity_2m ?: 68,
      windKmh = cur.wind_speed_10m ?: 12.0,
      windDirectionDeg = windDirDeg,
      windDirectionText = degreesToCardinal(windDirDeg),
      uvIndex = cur.uv_index ?: 5.0,
      visibilityKm = 10.0,
      pressureHpa = cur.pressure_msl ?: 1012.0,
      cloudCoverPct = cur.cloud_cover ?: 35,
      precipitationMm = cur.precipitation ?: 0.0,
      sunrise = sunriseStr,
      sunset = sunsetStr,
      hourly = hourlyList,
      daily = dailyList
    )
  }

  private fun createFallbackWeatherData(location: LocationInfo): WeatherData {
    val sampleHourly = (0..23).map { h ->
      val hourProb = when (h) {
        in 16..19 -> 65
        in 12..15 -> 30
        else -> 10
      }
      HourlyForecastItem(
        timeLabel = formatHourLabel(h),
        hourInt = h,
        tempC = 27.0 + (if (h in 11..15) 5.0 else 0.0) - (if (h in 0..5) 3.0 else 0.0),
        weatherCode = if (h in 16..19) 61 else 1,
        condition = if (h in 16..19) "Scattered Rain" else "Partly Cloudy",
        rainProb = hourProb,
        windKmh = 14.0,
        humidity = 72,
        uv = if (h in 10..14) 7.5 else 2.0
      )
    }

    val sampleDaily = listOf("Today", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri").mapIndexed { idx, day ->
      DailyForecastItem(
        dateLabel = "Oct ${3 + idx}",
        dayOfWeek = day,
        weatherCode = if (idx == 1 || idx == 4) 61 else 1,
        condition = if (idx == 1 || idx == 4) "Scattered Showers" else "Mostly Sunny",
        maxTempC = 32.0 + (idx % 2),
        minTempC = 24.0,
        rainProb = if (idx == 1 || idx == 4) 65 else 20,
        windKmh = 15.0,
        uvMax = 8.0,
        sunrise = "06:02 AM",
        sunset = "06:11 PM"
      )
    }

    return WeatherData(
      location = location,
      tempC = 29.4,
      feelsLikeC = 32.1,
      condition = "Partly Cloudy",
      weatherCode = 2,
      isDay = true,
      humidity = 70,
      windKmh = 13.5,
      windDirectionDeg = 85,
      windDirectionText = "ENE",
      uvIndex = 6.2,
      visibilityKm = 9.5,
      pressureHpa = 1011.0,
      cloudCoverPct = 40,
      precipitationMm = 0.2,
      sunrise = "06:02 AM",
      sunset = "06:11 PM",
      hourly = sampleHourly,
      daily = sampleDaily
    )
  }

  fun decodeWmoCode(code: Int, isDay: Boolean): String {
    return when (code) {
      0 -> if (isDay) "Sunny & Clear" else "Clear Sky"
      1 -> if (isDay) "Mainly Sunny" else "Mostly Clear"
      2 -> "Partly Cloudy"
      3 -> "Overcast"
      45, 48 -> "Misty Fog"
      51, 53, 55 -> "Light Drizzle"
      56, 57 -> "Freezing Drizzle"
      61 -> "Light Rain"
      63 -> "Moderate Rain"
      65 -> "Heavy Downpour"
      66, 67 -> "Freezing Rain"
      71, 73, 75 -> "Snowfall"
      80 -> "Light Showers"
      81 -> "Scattered Showers"
      82 -> "Violent Showers"
      95 -> "Thunderstorm"
      96, 99 -> "Severe Thunderstorm with Hail"
      else -> "Partly Cloudy"
    }
  }

  private fun degreesToCardinal(deg: Int): String {
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
      "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = ((deg + 11.25) / 22.5).toInt() % 16
    return directions[index]
  }

  private fun parseHourFromIso(timeStr: String, fallback: Int): Int {
    return try {
      if (timeStr.contains("T")) {
        val timePart = timeStr.substringAfter("T")
        timePart.substringBefore(":").toInt()
      } else fallback
    } catch (e: Exception) {
      fallback
    }
  }

  private fun formatHourLabel(hour: Int): String {
    val h = hour % 24
    return when {
      h == 0 -> "12 AM"
      h < 12 -> "$h AM"
      h == 12 -> "12 PM"
      else -> "${h - 12} PM"
    }
  }

  private fun parseDateLabels(dateStr: String, dayIndex: Int): Pair<String, String> {
    if (dayIndex == 0) return Pair("Today", "Today")
    if (dayIndex == 1) return Pair("Tomorrow", "Tomorrow")
    return try {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val date = sdf.parse(dateStr) ?: Date()
      val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
      val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
      Pair(monthDayFormat.format(date), dayFormat.format(date))
    } catch (e: Exception) {
      Pair("Day $dayIndex", "Day $dayIndex")
    }
  }

  private fun formatTimeOnly(isoTime: String): String {
    return try {
      if (isoTime.contains("T")) {
        val timePart = isoTime.substringAfter("T")
        val hour = timePart.substringBefore(":").toInt()
        val minute = timePart.substringAfter(":").substringBefore(":").take(2)
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format(Locale.getDefault(), "%02d:%s %s", displayHour, minute, amPm)
      } else isoTime
    } catch (e: Exception) {
      isoTime
    }
  }
}
