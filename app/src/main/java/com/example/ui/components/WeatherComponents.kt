package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItem
import com.example.data.model.FitnessInsight
import com.example.data.model.HealthInsight
import com.example.data.model.LocationInfo
import com.example.data.model.MausamAlertItem
import com.example.data.model.WeatherData
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

@Composable
fun AppHeader(
  location: LocationInfo,
  onLocationClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .clickable { onLocationClick() }
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = "Current Locality",
          tint = MausamBlue,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = location.displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      if (location.fullAddress.isNotBlank()) {
        Text(
          text = location.fullAddress,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          modifier = Modifier.padding(start = 22.dp)
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onNotificationsClick) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Alerts",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(MausamBlue, MausamPurple)))
          .clickable { onProfileClick() },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "M",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
    }
  }
}

@Composable
fun HeroWeatherCard(
  weather: WeatherData,
  isFahrenheit: Boolean = false,
  modifier: Modifier = Modifier
) {
  val displayTemp = if (isFahrenheit) (weather.tempC * 9 / 5 + 32).toInt() else weather.tempC.toInt()
  val displayFeelsLike = if (isFahrenheit) (weather.feelsLikeC * 9 / 5 + 32).toInt() else weather.feelsLikeC.toInt()
  val unitSymbol = if (isFahrenheit) "°F" else "°C"

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              MausamIndigo,
              MausamBlue,
              MausamCyan.copy(alpha = 0.85f)
            )
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = "$displayTemp$unitSymbol",
              fontSize = 58.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White,
              lineHeight = 60.sp
            )
            Text(
              text = "Feels like $displayFeelsLike$unitSymbol",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = Color.White.copy(alpha = 0.2f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = weather.condition,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          // Weather Icon Graphic
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            val icon = getWeatherIconForCode(weather.weatherCode, weather.isDay)
            Icon(
              imageVector = icon,
              contentDescription = weather.condition,
              tint = WeatherSun,
              modifier = Modifier.size(46.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of 6 primary weather metrics
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MetricItem(
            icon = Icons.Default.WaterDrop,
            label = "Humidity",
            value = "${weather.humidity}%"
          )
          MetricItem(
            icon = Icons.Default.Air,
            label = "Wind",
            value = "${weather.windKmh.toInt()} km/h ${weather.windDirectionText}"
          )
          MetricItem(
            icon = Icons.Default.WbSunny,
            label = "UV Index",
            value = "${weather.uvIndex.toInt()}"
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MetricItem(
            icon = Icons.Default.Compress,
            label = "Pressure",
            value = "${weather.pressureHpa.toInt()} hPa"
          )
          MetricItem(
            icon = Icons.Default.WbTwilight,
            label = "Sunrise",
            value = weather.sunrise
          )
          MetricItem(
            icon = Icons.Default.WbTwilight,
            label = "Sunset",
            value = weather.sunset
          )
        }
      }
    }
  }
}

@Composable
private fun MetricItem(icon: ImageVector, label: String, value: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = Color.White.copy(alpha = 0.9f),
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 11.sp
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }
  }
}

@Composable
fun MausamInsightCard(
  insightText: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MausamPurple.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = "Insight",
          tint = MausamPurple,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "🧠 MAUSAM Insight",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MausamPurple
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = insightText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 20.sp
        )
      }
    }
  }
}

@Composable
fun ImportantAlertsBanner(
  alerts: List<MausamAlertItem>,
  onViewAllClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (alerts.isEmpty()) return

  val first = alerts.first()
  val isSevere = first.severity == "SEVERE"
  val bannerColor = if (isSevere) WeatherAlert else WeatherWarning

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onViewAllClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.12f))
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isSevere) Icons.Default.Thunderstorm else Icons.Default.Warning,
        contentDescription = "Alert",
        tint = bannerColor,
        modifier = Modifier.size(26.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = bannerColor,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = if (first.isOfficial) "OFFICIAL ALERT" else "MAUSAM ADVISORY",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = first.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = bannerColor
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = first.message,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2
        )
      }
    }
  }
}

@Composable
fun QuickActionsRow(
  onCropClick: () -> Unit,
  onTravelClick: () -> Unit,
  onBeachClick: () -> Unit,
  onChangeLocationClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "Quick Actions",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionButton(
        icon = Icons.Default.Agriculture,
        label = "Crop Risk",
        badge = "🌱",
        color = WeatherSafe,
        onClick = onCropClick,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.TravelExplore,
        label = "Travel",
        badge = "✈️",
        color = MausamBlue,
        onClick = onTravelClick,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.BeachAccess,
        label = "Find Beach",
        badge = "🏖️",
        color = MausamCyan,
        onClick = onBeachClick,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.LocationOn,
        label = "Location",
        badge = "📍",
        color = MausamPurple,
        onClick = onChangeLocationClick,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  badge: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = color,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun LifestyleHighlightsCard(
  fitness: FitnessInsight?,
  health: HealthInsight?,
  selectedLifestyles: List<String>,
  onOpenLifestyle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Personalized Lifestyle Status",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "View All →",
          style = MaterialTheme.typography.labelSmall,
          color = MausamBlue,
          modifier = Modifier.clickable { onOpenLifestyle() }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Fitness Comfort Meter
      if (fitness != null && selectedLifestyles.contains("fitness")) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = "Fitness",
              tint = MausamBlue,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Fitness Comfort Score",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
          Text(
            text = "${fitness.comfortScore}/100 (${fitness.status})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (fitness.comfortScore >= 70) WeatherSafe else WeatherWarning
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { fitness.comfortScore / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (fitness.comfortScore >= 70) WeatherSafe else WeatherWarning,
          trackColor = MaterialTheme.colorScheme.surface
        )
        Text(
          text = fitness.explanation,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
        )
      }

      // Health AQI & Comfort
      if (health != null && selectedLifestyles.contains("health")) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.HealthAndSafety,
              contentDescription = "Health",
              tint = WeatherSafe,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Air & Outdoor Comfort",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
          Text(
            text = "${health.aqiLevel} • ${health.outdoorComfort}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Composable
fun SmartSuggestionsList(
  suggestions: List<String>,
  modifier: Modifier = Modifier
) {
  if (suggestions.isEmpty()) return

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = "🧠 Smart Suggestions",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    suggestions.take(3).forEach { suggestion ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = WeatherSunWarm,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = suggestion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
fun SevenDayForecastPreview(
  days: List<DailyForecastItem>,
  onViewMore: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "📅 7-Day Forecast",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Detailed View →",
        style = MaterialTheme.typography.labelSmall,
        color = MausamBlue,
        modifier = Modifier.clickable { onViewMore() }
      )
    }

    days.take(5).forEach { day ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = day.dayOfWeek,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.width(80.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = getWeatherIconForCode(day.weatherCode, true),
            contentDescription = day.condition,
            tint = WeatherSun,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${day.rainProb}% rain",
            style = MaterialTheme.typography.labelSmall,
            color = if (day.rainProb >= 40) WeatherRain else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${day.maxTempC.toInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${day.minTempC.toInt()}°",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

fun getWeatherIconForCode(code: Int, isDay: Boolean): ImageVector {
  return when (code) {
    0, 1 -> Icons.Default.WbSunny
    2, 3 -> Icons.Default.Air
    45, 48 -> Icons.Default.Air
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop
    95, 96, 99 -> Icons.Default.Thunderstorm
    else -> Icons.Default.WbSunny
  }
}
