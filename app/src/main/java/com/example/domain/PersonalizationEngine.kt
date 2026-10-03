package com.example.domain

import com.example.data.model.CommuterModeInsight
import com.example.data.model.EventsModeInsight
import com.example.data.model.FamilyModeInsight
import com.example.data.model.FitnessInsight
import com.example.data.model.HealthInsight
import com.example.data.model.LifestyleType
import com.example.data.model.MausamAlertItem
import com.example.data.model.WeatherData
import java.util.Calendar

class PersonalizationEngine {

  /**
   * Generates dynamic natural-language insight tailored to real weather variables and time.
   */
  fun generateInsight(weather: WeatherData, lang: String = "en"): String {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when (currentHour) {
      in 5..11 -> if (lang == "ta") "காலை வணக்கம்!" else if (lang == "tanglish") "Good morning pa!" else "Good morning!"
      in 12..16 -> if (lang == "ta") "மதிய வணக்கம்!" else if (lang == "tanglish") "Good afternoon!" else "Good afternoon!"
      in 17..20 -> if (lang == "ta") "மாலை வணக்கம்!" else if (lang == "tanglish") "Good evening!" else "Good evening!"
      else -> if (lang == "ta") "இனிய இரவு வணக்கம்!" else if (lang == "tanglish") "Night time vanakkam!" else "Good night!"
    }

    val temp = weather.tempC.toInt()
    val humidity = weather.humidity
    val rainProbUpcoming = weather.hourly.take(4).maxOfOrNull { it.rainProb } ?: 10
    val rainHour = weather.hourly.take(8).firstOrNull { it.rainProb >= 50 }

    val rainSentence = if (rainHour != null) {
      when (lang) {
        "ta" -> "${rainHour.timeLabel} மணிக்கு பிறகு மழை பெய்ய அதிக வாய்ப்பு உள்ளது, குடை எடுத்துச் செல்வது நல்லது."
        "tanglish" -> "${rainHour.timeLabel}-ku apram rain vara nalla chance irukku, umbrella eduthukonga."
        else -> "Rain probability increases around ${rainHour.timeLabel}, so carrying an umbrella would be useful."
      }
    } else if (rainProbUpcoming < 20) {
      when (lang) {
        "ta" -> "அடுத்த சில மணிநேரங்களில் மழை வாய்ப்பு மிகக் குறைவு. வெளிப்புற பணிகளுக்கு சாதகமான சூழல்."
        "tanglish" -> "Adutha few hours rain chance romba kami. Velila poga safe."
        else -> "The next few hours have very low rain probability, favorable for outdoor movement."
      }
    } else {
      when (lang) {
        "ta" -> "வானம் மேகமூட்டத்துடன் காணப்படும். லேசான தூறல் பெய்யக்கூடும்."
        "tanglish" -> "Sky cloudy-ah irukku, occasional light drizzle vara chance irukku."
        else -> "Skies are partly overcast with occasional localized drizzle possible."
      }
    }

    val tempFeel = if (temp >= 33) {
      if (lang == "ta") "வெப்பம் அதிகமாக உணரப்படுகிறது" else if (lang == "tanglish") "Veyil konjam jaasthi" else "It's warm and humid"
    } else if (temp <= 22) {
      if (lang == "ta") "குளுமையான காற்று நிலவுகிறது" else if (lang == "tanglish") "Cool breezy weather" else "Cool comfortable temperatures"
    } else {
      if (lang == "ta") "மிதமான வெப்பமும் ஈரப்பதமும் உள்ளது" else if (lang == "tanglish") "Moderate weather" else "Pleasantly balanced temperatures"
    }

    return "$timeGreeting $tempFeel ($temp°C, $humidity% humidity). $rainSentence"
  }

  fun calculateFitnessInsight(weather: WeatherData, lang: String = "en"): FitnessInsight {
    var score = 90

    // Temp penalty (ideal: 18 - 25 C)
    if (weather.tempC > 32) score -= 25
    else if (weather.tempC > 28) score -= 12
    else if (weather.tempC < 15) score -= 10

    // Humidity penalty (ideal: 40 - 65%)
    if (weather.humidity > 80) score -= 20
    else if (weather.humidity > 70) score -= 10

    // Rain penalty
    val currentRainProb = weather.hourly.firstOrNull()?.rainProb ?: 10
    if (currentRainProb > 60) score -= 30
    else if (currentRainProb > 30) score -= 15

    // Wind penalty
    if (weather.windKmh > 28) score -= 10

    score = score.coerceIn(15, 100)

    val bestRun = "06:00 AM – 07:30 AM"
    val bestWalk = "05:45 PM – 07:15 PM"

    val explanation = when {
      score >= 80 -> "Conditions are comfortable because temperature, wind and UV are moderate."
      weather.humidity >= 75 -> "Comfort is reduced because humidity is high (${weather.humidity}%)."
      weather.tempC >= 32 -> "Comfort is reduced due to elevated daytime heat (${weather.tempC.toInt()}°C)."
      currentRainProb >= 50 -> "Slippery wet surfaces anticipated due to rain probability."
      else -> "Moderate outdoor workout suitability."
    }

    val status = when {
      score >= 80 -> "Optimal"
      score >= 60 -> "Good"
      score >= 40 -> "Fair"
      else -> "Challenging"
    }

    return FitnessInsight(
      comfortScore = score,
      bestRunningTime = bestRun,
      bestWalkingTime = bestWalk,
      explanation = explanation,
      status = status
    )
  }

  fun calculateHealthInsight(weather: WeatherData, aqiValue: Int): HealthInsight {
    val suggestions = mutableListOf<String>()

    val comfort = when {
      weather.tempC in 20.0..29.0 && weather.humidity in 45..70 && aqiValue <= 60 -> "High Outdoor Comfort"
      weather.tempC > 34.0 || weather.humidity > 82 -> "Muggy & Warm"
      aqiValue > 120 -> "Air Sensitivity Caution"
      else -> "Moderate Outdoor Comfort"
    }

    val uvRisk = when {
      weather.uvIndex >= 9.0 -> "Very High (Peak 11:30 AM – 2:30 PM)"
      weather.uvIndex >= 6.0 -> "Moderate to High"
      weather.uvIndex >= 3.0 -> "Moderate"
      else -> "Low UV"
    }

    if (weather.uvIndex >= 6.0) {
      suggestions.add("UV is elevated around midday. Consider reducing prolonged direct sun exposure.")
    }
    if (weather.humidity >= 75) {
      suggestions.add("High ambient moisture; stay well hydrated during prolonged outdoor walking.")
    }
    if (aqiValue > 100) {
      suggestions.add("Sensitive individuals should minimize heavy cardiovascular workouts along busy roads.")
    } else {
      suggestions.add("Ambient outdoor air is clean for recreational activities.")
    }

    return HealthInsight(
      outdoorComfort = comfort,
      aqiLevel = "AQI $aqiValue",
      uvRisk = uvRisk,
      pollenInfo = "Pollen information is unavailable for this location.",
      suggestions = suggestions
    )
  }

  fun calculateFitnessActivityInsight(
    weather: WeatherData,
    activity: String = "Running",
    lang: String = "en"
  ): FitnessInsight {
    val base = calculateFitnessInsight(weather, lang)
    val specificTip = when (activity) {
      "Running" -> if (weather.tempC > 30) "Hydrate every 2 km and choose shaded routes to prevent thermal exhaustion." else "Optimal pavement traction and breathable air."
      "Walking" -> if (weather.humidity > 75) "Carry water; moderate pace recommended to stay cool." else "Brisk walking is highly suitable under current breeze."
      "Cycling" -> if (weather.windKmh > 20) "Headwinds of ${weather.windKmh.toInt()} km/h will increase resistance; maintain steady cadence." else "Clear road visibility with smooth ride conditions."
      else -> "Outdoor bodyweight or resistance training is feasible in open shaded parks."
    }

    return base.copy(
      explanation = "${base.explanation} $specificTip"
    )
  }

  fun calculateFamilyInsight(weather: WeatherData): FamilyModeInsight {
    val morningRain = weather.hourly.filter { it.hourInt in 7..9 }.maxOfOrNull { it.rainProb } ?: 10
    val afternoonRain = weather.hourly.filter { it.hourInt in 14..16 }.maxOfOrNull { it.rainProb } ?: 15
    val hasStorm = weather.weatherCode in listOf(95, 96, 99)

    val suggestions = mutableListOf<String>()
    if (morningRain >= 40) {
      suggestions.add("Pack umbrellas and rain covers for morning school backpacks.")
    } else {
      suggestions.add("Clear morning commute conditions for school buses and walking.")
    }

    if (afternoonRain >= 40) {
      suggestions.add("Afternoon pickup timing may coincide with light showers; keep umbrellas handy.")
    }

    if (weather.uvIndex >= 7.0) {
      suggestions.add("Ensure children wear caps or apply sunscreen during midday recess.")
    }

    return FamilyModeInsight(
      morningSchoolWeather = "${weather.hourly.find { it.hourInt in 7..8 }?.tempC?.toInt() ?: weather.tempC.toInt()}°C • ${if (morningRain >= 40) "Damp/Rainy" else "Pleasant"}",
      commuteWeather = "Commute rain risk: ${maxOf(morningRain, afternoonRain)}%",
      rainProbabilityCommute = maxOf(morningRain, afternoonRain),
      thunderstormAlert = hasStorm,
      suggestions = suggestions
    )
  }

  fun calculateCommuterInsight(weather: WeatherData): CommuterModeInsight {
    val maxRain = weather.hourly.take(6).maxOfOrNull { it.rainProb } ?: 15
    val visibility = if (weather.weatherCode in listOf(45, 48)) "Reduced (< 3 km due to mist/fog)" else "Clear (> 8 km)"
    val roadRisk = if (maxRain >= 50) "High - Slippery asphalt & standing puddles" else "Low - Dry road grip"
    val rainImpact = if (maxRain >= 50) "Expect +15-20 min transit delays during peak downpours" else "Normal transit transit flow"
    val windAlert = if (weather.windKmh >= 28) "Gusty crosswinds on open elevated flyovers" else "Calm wind flow"

    val tips = listOf(
      if (maxRain >= 40) "Maintain extra braking distance on expressways." else "Optimal two-wheeler and car driving conditions.",
      if (weather.uvIndex > 7) "Use sunglasses for direct highway glare." else "Standard daytime ambient light."
    )

    return CommuterModeInsight(
      visibilityCondition = visibility,
      roadSurfaceRisk = roadRisk,
      rainImpact = rainImpact,
      windAlert = windAlert,
      commuteTips = tips
    )
  }

  fun calculateEventsInsight(
    weather: WeatherData,
    eventName: String = "Outdoor Gathering",
    isOutdoor: Boolean = true
  ): EventsModeInsight {
    var score = 88
    val maxRain = weather.hourly.take(10).maxOfOrNull { it.rainProb } ?: 10
    if (maxRain >= 50) score -= 30
    if (weather.tempC > 34) score -= 20
    if (weather.windKmh > 26) score -= 15
    score = score.coerceIn(20, 100)

    val warnings = mutableListOf<String>()
    if (maxRain >= 45) warnings.add("Rain canopy or backup indoor shelter recommended.")
    if (weather.tempC >= 34) warnings.add("Provide adequate misting/fans and hydration for guests.")
    if (weather.windKmh >= 28) warnings.add("Secure decorative banners, tents, and temporary light stands.")

    val advisory = when {
      score >= 80 -> "Excellent weather window for $eventName."
      score >= 60 -> "Good conditions with mild weather considerations for $eventName."
      else -> "Challenging outdoor weather; consider canopy cover or indoor venues for $eventName."
    }

    return EventsModeInsight(
      eventName = eventName,
      isOutdoor = isOutdoor,
      outdoorSuitabilityScore = score,
      temperatureFeel = "${weather.tempC.toInt()}°C (Feels like ${weather.feelsLikeC.toInt()}°C)",
      rainProbability = maxRain,
      windGust = "${weather.windKmh.toInt()} km/h",
      advisory = advisory,
      warnings = warnings
    )
  }

  fun generateSmartSuggestions(weather: WeatherData): List<String> {
    val list = mutableListOf<String>()

    val rainPeak = weather.hourly.take(12).firstOrNull { it.rainProb >= 50 }
    if (rainPeak != null) {
      list.add("Carry an umbrella around ${rainPeak.timeLabel} as rain probability reaches ${rainPeak.rainProb}%.")
    } else {
      list.add("Dry conditions expected for the next 6 hours; great window for outdoor plans.")
    }

    if (weather.uvIndex >= 7.0) {
      list.add("UV is high (${weather.uvIndex.toInt()}). Wear sunglasses or apply sunscreen if outside between 11 AM and 3 PM.")
    }

    if (weather.windKmh >= 24) {
      list.add("Breezy conditions (${weather.windKmh.toInt()} km/h). Secure loose outdoor garden items or tarps.")
    }

    if (weather.humidity >= 80) {
      list.add("High humidity (${weather.humidity}%) makes it feel like ${weather.feelsLikeC.toInt()}°C. Stay hydrated.")
    }

    list.add("Morning and early evening hours offer the most comfortable temperatures for walking.")
    return list
  }

  fun generateAlerts(weather: WeatherData): List<MausamAlertItem> {
    val alerts = mutableListOf<MausamAlertItem>()
    val loc = weather.location.displayName

    // 1. Thunderstorm Alert
    if (weather.weatherCode in listOf(95, 96, 99)) {
      alerts.add(
        MausamAlertItem(
          id = "storm_active",
          title = "Active Thunderstorm Warning",
          message = "Intense convection and localized lightning detected in the area.",
          severity = "HIGH",
          category = "Thunderstorm",
          isOfficial = true,
          source = "Regional Meteorological Division",
          affectedLocation = loc,
          expectedPeriod = "Next 2–4 hours",
          recommendedAction = "Stay indoors; avoid tall trees and open water bodies."
        )
      )
    }

    // 2. Heavy Rain Alert
    val maxRainProb = weather.hourly.take(12).maxOfOrNull { it.rainProb } ?: 15
    if (maxRainProb >= 65) {
      alerts.add(
        MausamAlertItem(
          id = "heavy_rain",
          title = "Heavy Rain & Waterlogging Risk",
          message = "High precipitation probability (${maxRainProb}%) forecasted. Low-lying zones may face temporary stagnation.",
          severity = if (maxRainProb >= 80) "HIGH" else "MODERATE",
          category = "Heavy Rain",
          isOfficial = false,
          source = "MAUSAM Algorithmic Advisory",
          affectedLocation = loc,
          expectedPeriod = "Next 6 hours",
          recommendedAction = "Clear drainage outlets; carry water-resistant gear during transit."
        )
      )
    }

    // 3. Extreme Heat
    if (weather.tempC >= 36.0) {
      alerts.add(
        MausamAlertItem(
          id = "extreme_heat",
          title = "Elevated Heat Stress",
          message = "Ambient temperature is ${weather.tempC.toInt()}°C (Feels like ${weather.feelsLikeC.toInt()}°C).",
          severity = if (weather.tempC >= 39.0) "HIGH" else "MODERATE",
          category = "Extreme Heat",
          isOfficial = false,
          source = "MAUSAM Algorithmic Advisory",
          affectedLocation = loc,
          expectedPeriod = "11:30 AM – 3:30 PM",
          recommendedAction = "Drink electrolyte-rich fluids and avoid strenuous open-sun labor."
        )
      )
    }

    // 4. High UV
    if (weather.uvIndex >= 8.0) {
      alerts.add(
        MausamAlertItem(
          id = "high_uv",
          title = "Intense Solar UV Index (${weather.uvIndex.toInt()})",
          message = "Solar radiation exceeds safe bare-skin exposure limits.",
          severity = "MODERATE",
          category = "High UV",
          isOfficial = false,
          source = "MAUSAM Algorithmic Advisory",
          affectedLocation = loc,
          expectedPeriod = "Midday hours",
          recommendedAction = "Apply SPF 30+ sunscreen and wear UV-filtering eyewear."
        )
      )
    }

    // 5. Strong Wind
    if (weather.windKmh >= 30.0) {
      alerts.add(
        MausamAlertItem(
          id = "strong_wind",
          title = "Brisk Wind Gusts (${weather.windKmh.toInt()} km/h)",
          message = "Strong wind currents present along open coastal corridors and highways.",
          severity = if (weather.windKmh >= 45.0) "HIGH" else "MODERATE",
          category = "Strong Wind",
          isOfficial = false,
          source = "MAUSAM Algorithmic Advisory",
          affectedLocation = loc,
          expectedPeriod = "Current & Next 4 hours",
          recommendedAction = "Secure lightweight rooftop items and exercise extra control when riding two-wheelers."
        )
      )
    }

    // 6. Poor Visibility
    if (weather.weatherCode in listOf(45, 48)) {
      alerts.add(
        MausamAlertItem(
          id = "poor_visibility",
          title = "Reduced Horizontal Visibility",
          message = "Localized fog or heavy mist reducing sight distance to under 3 km.",
          severity = "MODERATE",
          category = "Poor Visibility",
          isOfficial = false,
          source = "MAUSAM Algorithmic Advisory",
          affectedLocation = loc,
          expectedPeriod = "Early morning window",
          recommendedAction = "Use low-beam headlights and reduce driving speeds on highways."
        )
      )
    }

    // 7. Agriculture Risk
    if (weather.humidity >= 80 && maxRainProb >= 50) {
      alerts.add(
        MausamAlertItem(
          id = "agri_risk",
          title = "Fungal Foliage & Moisture Warning",
          message = "High ambient humidity (${weather.humidity}%) combined with rain increases crop disease vulnerability.",
          severity = "MODERATE",
          category = "Agriculture Risk",
          isOfficial = false,
          source = "MAUSAM Agronomic Advisory",
          affectedLocation = loc,
          expectedPeriod = "Next 24 hours",
          recommendedAction = "Inspect field bund drainage and postpone chemical sprays."
        )
      )
    }

    // 8. Travel Risk
    if (maxRainProb >= 60 || weather.weatherCode in listOf(95, 96, 99)) {
      alerts.add(
        MausamAlertItem(
          id = "travel_risk",
          title = "Highway Commute Advisory",
          message = "Rain showers will cause standing water and slower vehicular transit speeds.",
          severity = "MODERATE",
          category = "Travel Risk",
          isOfficial = false,
          source = "MAUSAM Advisory",
          affectedLocation = loc,
          expectedPeriod = "Next 6 hours",
          recommendedAction = "Allow 15–20 minutes extra travel buffer."
        )
      )
    }

    // 9. Beach Risk
    if (weather.windKmh >= 32.0 || maxRainProb >= 60) {
      alerts.add(
        MausamAlertItem(
          id = "beach_risk",
          title = "Elevated Shoreline Swell",
          message = "Choppy shoreline currents generated by onshore winds.",
          severity = "MODERATE",
          category = "Beach Risk",
          isOfficial = false,
          source = "MAUSAM Coastal Advisory",
          affectedLocation = "Nearby coastal shores",
          expectedPeriod = "Afternoon swell cycle",
          recommendedAction = "Avoid swimming past designated lifeguard shore markers."
        )
      )
    }

    return alerts
  }
}
