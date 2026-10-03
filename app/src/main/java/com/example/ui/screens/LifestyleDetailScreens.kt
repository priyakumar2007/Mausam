package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AirQualityData
import com.example.data.model.CommuterModeInsight
import com.example.data.model.EventsModeInsight
import com.example.data.model.FamilyModeInsight
import com.example.data.model.FitnessInsight
import com.example.data.model.HealthInsight
import com.example.data.model.WeatherData
import com.example.domain.PersonalizationEngine
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple
import com.example.ui.theme.WeatherAlert
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherSun
import com.example.ui.theme.WeatherSunWarm
import com.example.ui.theme.WeatherWarning

// ----------------- 1. HEALTH MODE SCREEN -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthModeScreen(
  weather: WeatherData?,
  airQuality: AirQualityData?,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("❤️ Health & Wellness Mode", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier
  ) { padding ->
    if (weather == null) return@Scaffold

    val health = remember(weather, airQuality) {
      PersonalizationEngine().calculateHealthInsight(weather, airQuality?.aqi ?: 45)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Comfort Summary Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "Outdoor Comfort Index", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Surface(
                color = WeatherSafe,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = health.outdoorComfort,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              HealthMetricItem("Temperature", "${weather.tempC.toInt()}°C", WeatherSunWarm)
              HealthMetricItem("Humidity", "${weather.humidity}%", MausamCyan)
              HealthMetricItem("UV Level", "${weather.uvIndex.toInt()} (${health.uvRisk})", WeatherSun)
              HealthMetricItem("Air Quality", "${airQuality?.aqi ?: 45} AQI", WeatherSafe)
            }
          }
        }
      }

      // Air Quality & Particulate Metrics
      if (airQuality != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Ambient Air Quality Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "PM2.5: ${airQuality.pm25} µg/m³", style = MaterialTheme.typography.bodySmall)
                Text(text = "PM10: ${airQuality.pm10} µg/m³", style = MaterialTheme.typography.bodySmall)
                Text(text = "Status: ${airQuality.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = airQuality.advisory,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Pollen Notice
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = MausamCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = health.pollenInfo,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Health Suggestions
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Wellness & Hydration Guidance",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = WeatherSafe
            )
            Spacer(modifier = Modifier.height(8.dp))
            health.suggestions.forEach { s ->
              Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WeatherSafe, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = s, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }

      // Disclaimer
      item {
        Text(
          text = "Disclaimer: Weather-based wellness information only; not medical advice.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun HealthMetricItem(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
  }
}

// ----------------- 2. FITNESS MODE SCREEN -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessModeScreen(
  weather: WeatherData?,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedActivity by remember { mutableStateOf("Running") }
  val activities = listOf("Running", "Walking", "Cycling", "General Workout")

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("🏃 Fitness Intelligence", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier
  ) { padding ->
    if (weather == null) return@Scaffold

    val fitness = remember(weather, selectedActivity) {
      PersonalizationEngine().calculateFitnessActivityInsight(weather, selectedActivity)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Activity Selection Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          activities.forEach { act ->
            FilterChip(
              selected = selectedActivity == act,
              onClick = { selectedActivity = act },
              label = { Text(act, fontSize = 12.sp) }
            )
          }
        }
      }

      // Fitness Comfort Meter Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "$selectedActivity Comfort Score", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Surface(
                color = if (fitness.comfortScore >= 70) WeatherSafe else WeatherWarning,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${fitness.comfortScore}/100 • ${fitness.status}",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { fitness.comfortScore / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (fitness.comfortScore >= 70) WeatherSafe else WeatherWarning,
              trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = fitness.explanation,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 20.sp
            )
          }
        }
      }

      // Best Windows Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Optimal Workout Windows", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Column {
                Text("Morning Window", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(fitness.bestRunningTime, fontWeight = FontWeight.Bold, color = MausamBlue)
              }
              Column {
                Text("Evening Window", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(fitness.bestWalkingTime, fontWeight = FontWeight.Bold, color = WeatherSunWarm)
              }
            }
          }
        }
      }

      // Current Workout Environment Variables
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Live Training Conditions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              HealthMetricItem("Temperature", "${weather.tempC.toInt()}°C", WeatherSunWarm)
              HealthMetricItem("Feels Like", "${weather.feelsLikeC.toInt()}°C", WeatherSun)
              HealthMetricItem("Rain Chance", "${weather.hourly.firstOrNull()?.rainProb ?: 10}%", WeatherRain)
              HealthMetricItem("Wind Speed", "${weather.windKmh.toInt()} km/h", MausamCyan)
            }
          }
        }
      }
    }
  }
}

// ----------------- 3. PARENTS & FAMILY MODE SCREEN -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyModeScreen(
  weather: WeatherData?,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("👨‍👩‍👧 Parents & Family Mode", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier
  ) { padding ->
    if (weather == null) return@Scaffold

    val family = remember(weather) {
      PersonalizationEngine().calculateFamilyInsight(weather)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MausamIndigo)
              Spacer(modifier = Modifier.width(8.dp))
              Text("School Commute Forecast", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Morning Bell: ${family.morningSchoolWeather}", style = MaterialTheme.typography.bodyMedium)
            Text("Commute Rain Risk: ${family.rainProbabilityCommute}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (family.rainProbabilityCommute >= 40) WeatherRain else WeatherSafe)
            if (family.thunderstormAlert) {
              Spacer(modifier = Modifier.height(6.dp))
              Surface(color = WeatherAlert.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text("⚠️ Active lightning/storm watch during transit windows.", color = WeatherAlert, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
              }
            }
          }
        }
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Parenting & Commute Checkpoints", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            family.suggestions.forEach { s ->
              Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MausamBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(s, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }
  }
}

// ----------------- 4. COMMUTER MODE SCREEN -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuterModeScreen(
  weather: WeatherData?,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("🚗 Commuter Intelligence", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier
  ) { padding ->
    if (weather == null) return@Scaffold

    val commuter = remember(weather) {
      PersonalizationEngine().calculateCommuterInsight(weather)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text("Road & Transit Atmospheric Conditions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Road Surface Grip: ${commuter.roadSurfaceRisk}", style = MaterialTheme.typography.bodyMedium)
            Text("Visibility: ${commuter.visibilityCondition}", style = MaterialTheme.typography.bodyMedium)
            Text("Transit Pace: ${commuter.rainImpact}", style = MaterialTheme.typography.bodyMedium, color = MausamBlue)
            Text("Wind Cross-Drafts: ${commuter.windAlert}", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Commute Safety Advisories", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            commuter.commuteTips.forEach { tip ->
              Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MausamBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(tip, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }
  }
}

// ----------------- 5. EVENTS MODE SCREEN -----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsModeScreen(
  weather: WeatherData?,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var eventName by remember { mutableStateOf("Family Celebration") }
  var isOutdoor by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("🎉 Events Planner", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = modifier
  ) { padding ->
    if (weather == null) return@Scaffold

    val eventInsight = remember(weather, eventName, isOutdoor) {
      PersonalizationEngine().calculateEventsInsight(weather, eventName, isOutdoor)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
              value = eventName,
              onValueChange = { eventName = it },
              label = { Text("Event Name") },
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Outdoor Venue Setup")
              Switch(checked = isOutdoor, onCheckedChange = { isOutdoor = it })
            }
          }
        }
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Outdoor Suitability Score", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Surface(
                color = if (eventInsight.outdoorSuitabilityScore >= 75) WeatherSafe else WeatherWarning,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${eventInsight.outdoorSuitabilityScore}/100",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(eventInsight.advisory, style = MaterialTheme.typography.bodyMedium, color = MausamBlue, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Expected Ambient: ${eventInsight.temperatureFeel}", style = MaterialTheme.typography.bodySmall)
            Text("Precipitation Risk: ${eventInsight.rainProbability}%", style = MaterialTheme.typography.bodySmall)
            Text("Wind Speed: ${eventInsight.windGust}", style = MaterialTheme.typography.bodySmall)

            if (eventInsight.warnings.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              eventInsight.warnings.forEach { w ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                  Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WeatherWarning, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(w, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }
      }
    }
  }
}
