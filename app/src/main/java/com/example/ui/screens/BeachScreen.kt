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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeachSafetyDetail
import com.example.data.model.BeachSafetyStatus
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherAlert
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherSun
import com.example.ui.theme.WeatherWarning

@Composable
fun BeachScreen(
  beaches: List<BeachSafetyDetail>,
  isLoading: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.BeachAccess,
            contentDescription = null,
            tint = MausamCyan,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "🏖️ Nearest Beach & Marine Safety",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        }
        Button(
          onClick = onRefresh,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MausamBlue)
        ) {
          Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Scan Waves", fontSize = 11.sp)
        }
      }
      Text(
        text = "Real-time wave height, swell period, wind direction & alternative beach safety checks.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    if (isLoading) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MausamCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Fetching Open-Meteo marine swell and wave metrics...", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    } else if (beaches.isNotEmpty()) {
      val nearest = beaches.first()

      // Beach Friend Mode Card
      item {
        Surface(
          color = MausamCyan.copy(alpha = 0.12f),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(14.dp)) {
            Text("🌊 ", fontSize = 20.sp)
            Column {
              Text(
                text = "Beach Friend Insight",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MausamBlue
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Hey! Your nearest beach is ${nearest.beachName} (${nearest.distanceKm} km away). Waves are currently ${nearest.waveHeight}m with wind at ${nearest.windSpeedKmh.toInt()} km/h. " +
                    if (nearest.safetyStatus.isRisky) "Conditions are elevated, so review our calmer nearby alternative below." else "Conditions are enjoyable for coastal walking.",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      // Alternative Beach Recommendation Card (If nearest is Caution/High-Risk)
      if (nearest.alternativeBeachName != null && nearest.alternativeBeachReason != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WeatherSafe.copy(alpha = 0.12f))
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
              Icon(imageVector = Icons.Default.Surfing, contentDescription = null, tint = WeatherSafe, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "💡 MAUSAM Recommends: ${nearest.alternativeBeachName}",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall,
                  color = WeatherSafe
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = nearest.alternativeBeachReason!!,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // Alternative Day Recommendation (If today has high wave swell)
      if (nearest.alternativeDayDate != null && nearest.alternativeDayReason != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MausamBlue.copy(alpha = 0.12f))
          ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MausamBlue, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "📅 Calmer Day Alternative: ${nearest.alternativeDayDate}",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall,
                  color = MausamBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = nearest.alternativeDayReason!!,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // Nearest Beaches List
      item {
        Text(
          text = "Nearby Coastal Beaches",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      items(beaches) { beach ->
        BeachDetailCard(beach = beach)
      }
    }
  }
}

@Composable
private fun BeachDetailCard(beach: BeachSafetyDetail) {
  val statusColor = when (beach.safetyStatus) {
    BeachSafetyStatus.SUITABLE -> WeatherSafe
    BeachSafetyStatus.CAUTION -> WeatherWarning
    BeachSafetyStatus.HIGH_RISK -> WeatherAlert
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = beach.beachName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Text(
            text = "${beach.distanceKm} km away • ${beach.locationDesc}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          color = statusColor,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = beach.safetyStatus.label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Wave & Marine Metrics
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        MetricCol(
          icon = Icons.Default.Water,
          label = "Wave Height",
          value = "${beach.waveHeight} m",
          color = MausamCyan
        )
        MetricCol(
          icon = Icons.Default.Surfing,
          label = "Swell Period",
          value = "${beach.wavePeriod}s (${beach.waveDirection})",
          color = MausamBlue
        )
        MetricCol(
          icon = Icons.Default.Air,
          label = "Wind Speed",
          value = "${beach.windSpeedKmh.toInt()} km/h",
          color = MausamCyan
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        MetricCol(
          icon = Icons.Default.Thermostat,
          label = "Air / Water",
          value = "${beach.airTempC.toInt()}°C / ${beach.waterTempC?.toInt() ?: 28}°C",
          color = WeatherSun
        )
        MetricCol(
          icon = Icons.Default.WbSunny,
          label = "UV Index",
          value = "${beach.uvIndex.toInt()}",
          color = WeatherSun
        )
        MetricCol(
          icon = Icons.Default.WaterDrop,
          label = "Rain Chance",
          value = "${beach.rainProb}%",
          color = WeatherRain
        )
      }

      if (beach.riskReasons.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surface)
        Spacer(modifier = Modifier.height(8.dp))

        beach.riskReasons.forEach { r ->
          Row(modifier = Modifier.padding(vertical = 2.dp)) {
            Text("• ", fontWeight = FontWeight.Bold, color = statusColor)
            Text(r, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}

@Composable
private fun MetricCol(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  color: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
  }
}
