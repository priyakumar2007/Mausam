package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MausamAlertItem
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.WeatherAlert
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherWarning

@Composable
fun AlertsScreen(
  alerts: List<MausamAlertItem>,
  modifier: Modifier = Modifier
) {
  val officialAlerts = alerts.filter { it.isOfficial }
  val mausamAdvisories = alerts.filter { !it.isOfficial }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = null,
          tint = WeatherAlert,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "🔔 Weather Alerts & Advisories",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
      Text(
        text = "Clear distinction between Regional Meteorological warnings and MAUSAM rule-based lifestyle advisories.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Section 1: Official Meteorological Warnings
    item {
      Text(
        text = "Official Meteorological Alerts",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    if (officialAlerts.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WeatherSafe)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "No severe official meteorological warnings currently active for this region.",
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    } else {
      items(officialAlerts) { alert ->
        AlertCardItem(alert = alert)
      }
    }

    // Section 2: MAUSAM Lifestyle Advisories
    item {
      Text(
        text = "MAUSAM Algorithmic Advisories",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    if (mausamAdvisories.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MausamBlue)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Atmospheric conditions are within standard baseline thresholds.",
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    } else {
      items(mausamAdvisories) { alert ->
        AlertCardItem(alert = alert)
      }
    }
  }
}

@Composable
private fun AlertCardItem(alert: MausamAlertItem) {
  val badgeColor = if (alert.isOfficial) WeatherAlert else WeatherWarning

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
        Surface(
          color = badgeColor,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (alert.isOfficial) "OFFICIAL ALERT" else "MAUSAM ADVISORY",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
        Text(
          text = alert.category,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = alert.title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = alert.message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "📍 Affected Location: ${alert.affectedLocation}",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "⏱️ Expected Duration: ${alert.expectedPeriod}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "💡 Action: ${alert.recommendedAction}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = if (alert.severity == "HIGH") WeatherAlert else MausamBlue
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Source: ${alert.source}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }
  }
}
