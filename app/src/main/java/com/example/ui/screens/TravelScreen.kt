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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.LocationInfo
import com.example.data.model.RouteCheckpointWeather
import com.example.data.model.TravelRouteComparison
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherSun
import com.example.ui.theme.WeatherWarning
import kotlin.math.roundToInt

@Composable
fun TravelScreen(
  source: LocationInfo,
  destination: LocationInfo,
  travelComparison: TravelRouteComparison?,
  isLoading: Boolean,
  onCalculateRoutes: (LocationInfo, LocationInfo) -> Unit,
  onUseCurrentAsSource: () -> Unit,
  modifier: Modifier = Modifier
) {
  var sourceText by remember { mutableStateOf(source.displayName) }
  var destText by remember { mutableStateOf(destination.displayName) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = MausamBlue,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "✈️ Travel Planner & Weather",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
      Text(
        text = "Compare shortest route vs highway bypass with live checkpoint weather",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Source & Destination Input Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
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
              text = "Trip Waypoints",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MausamBlue
            )
            Text(
              text = "📍 Use Current as Source",
              style = MaterialTheme.typography.labelSmall,
              color = MausamCyan,
              modifier = Modifier.clickable {
                onUseCurrentAsSource()
                sourceText = source.displayName
              }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            label = { Text("From (Origin)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
              Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MausamBlue)
            }
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = destText,
            onValueChange = { destText = it },
            label = { Text("To (Destination)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
              Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = WeatherSafe)
            }
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val src = LocationInfo(sourceText, "", "Tamil Nadu", "India", 12.4334, 79.8297)
              val dst = LocationInfo(destText, "", "Tamil Nadu", "India", 13.0827, 80.2707)
              onCalculateRoutes(src, dst)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("calculate_route_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MausamBlue)
          ) {
            Icon(imageVector = Icons.Default.Route, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compare Routes & Weather")
          }
        }
      }
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
            CircularProgressIndicator(color = MausamBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Routing via OSRM & fetching checkpoint forecasts...", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    } else if (travelComparison != null) {
      // Friendly Advisory Box
      item {
        Surface(
          color = MausamIndigo.copy(alpha = 0.12f),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(14.dp)) {
            Text("🧠 ", fontSize = 18.sp)
            Column {
              Text(
                text = "Travel Advisory",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MausamIndigo
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = travelComparison.advisory,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      // Route Comparison Cards
      item {
        Text(
          text = "Route Comparison",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      // Route 1: Shortest
      item {
        RouteOptionCard(
          title = "🚗 Shortest Route",
          distanceKm = travelComparison.shortestDistanceKm,
          durationMin = travelComparison.shortestDurationMin,
          trafficStatus = travelComparison.shortestTraffic,
          weatherSummary = travelComparison.shortestWeatherCondition,
          isRecommended = travelComparison.shortestDurationMin <= travelComparison.alternativeDurationMin
        )
      }

      // Route 2: Longer Alternative
      item {
        RouteOptionCard(
          title = "🛣️ Longer Alternative Route",
          distanceKm = travelComparison.alternativeDistanceKm,
          durationMin = travelComparison.alternativeDurationMin,
          trafficStatus = travelComparison.alternativeTraffic,
          weatherSummary = travelComparison.alternativeWeatherCondition,
          isRecommended = travelComparison.alternativeDurationMin < travelComparison.shortestDurationMin
        )
      }

      // Route Checkpoints Weather
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Weather Along Route Checkpoints",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            travelComparison.checkpoints.forEachIndexed { idx, cp ->
              CheckpointRow(checkpoint = cp, isLast = idx == travelComparison.checkpoints.size - 1)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RouteOptionCard(
  title: String,
  distanceKm: Double,
  durationMin: Int,
  trafficStatus: String,
  weatherSummary: String,
  isRecommended: Boolean
) {
  val hours = durationMin / 60
  val mins = durationMin % 60
  val durationStr = if (hours > 0) "${hours}h ${mins}m" else "${mins} min"

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        if (isRecommended) {
          Surface(color = WeatherSafe, shape = RoundedCornerShape(8.dp)) {
            Text(
              text = "FASTER",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "Distance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "${distanceKm.roundToInt()} km", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
        }
        Column {
          Text(text = "Travel Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = durationStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MausamBlue)
        }
        Column {
          Text(text = "Traffic Flow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = trafficStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surface)
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Weather along path: $weatherSummary",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun CheckpointRow(checkpoint: RouteCheckpointWeather, isLast: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(MausamBlue)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = checkpoint.name,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = "${checkpoint.tempC.toInt()}°C",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = "${checkpoint.rainProb}% rain",
      style = MaterialTheme.typography.bodySmall,
      color = if (checkpoint.rainProb >= 40) WeatherRain else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
