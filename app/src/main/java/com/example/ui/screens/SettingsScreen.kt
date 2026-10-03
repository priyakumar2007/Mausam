package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.WeatherAlert

@Composable
fun SettingsScreen(
  userProfile: UserProfileEntity,
  onUpdateUnit: (String) -> Unit,
  onUpdateLanguage: (String) -> Unit,
  onClearCache: () -> Unit,
  onToggleDemoMode: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var severeAlertsEnabled by remember { mutableStateOf(true) }
  var agriAlertsEnabled by remember { mutableStateOf(true) }
  var travelAlertsEnabled by remember { mutableStateOf(true) }
  var beachAlertsEnabled by remember { mutableStateOf(true) }
  var cacheClearedMessage by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Settings",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Temperature Unit Selection
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = MausamBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Temperature Unit", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onUpdateUnit("C") },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = userProfile.tempUnit == "C", onClick = { onUpdateUnit("C") })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Celsius (°C)")
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onUpdateUnit("F") },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = userProfile.tempUnit == "F", onClick = { onUpdateUnit("F") })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fahrenheit (°F)")
          }
        }
      }
    }

    // Language Selection (English, தமிழ், Tanglish)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MausamBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Language / மொழி", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onUpdateLanguage("en") },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = userProfile.language == "en", onClick = { onUpdateLanguage("en") })
            Spacer(modifier = Modifier.width(8.dp))
            Text("English (Default)")
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onUpdateLanguage("ta") },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = userProfile.language == "ta", onClick = { onUpdateLanguage("ta") })
            Spacer(modifier = Modifier.width(8.dp))
            Text("தமிழ் (Tamil)")
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onUpdateLanguage("tanglish") },
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = userProfile.language == "tanglish", onClick = { onUpdateLanguage("tanglish") })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tanglish (Tamil in English text)")
          }
        }
      }
    }

    // Notifications Toggles
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MausamBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Weather Notifications", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          NotificationSwitchRow("Severe Weather Alerts", severeAlertsEnabled) { severeAlertsEnabled = it }
          NotificationSwitchRow("Crop & Agriculture Alerts", agriAlertsEnabled) { agriAlertsEnabled = it }
          NotificationSwitchRow("Travel & Route Alerts", travelAlertsEnabled) { travelAlertsEnabled = it }
          NotificationSwitchRow("Beach & Marine Swell Alerts", beachAlertsEnabled) { beachAlertsEnabled = it }
        }
      }
    }

    // Demo Mode & Clear Cache
    item {
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
            Column {
              Text(text = "Melmaruvathur Demo Mode", fontWeight = FontWeight.Bold)
              Text(
                text = "Pre-loads paddy field and coastal routes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = userProfile.isDemoMode,
              onCheckedChange = { onToggleDemoMode() }
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.surface)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Local Cache", fontWeight = FontWeight.Bold)
              Text(
                text = if (cacheClearedMessage) "Cache cleared successfully!" else "Clear offline stored weather data",
                style = MaterialTheme.typography.labelSmall,
                color = if (cacheClearedMessage) MausamBlue else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Button(
              onClick = {
                onClearCache()
                cacheClearedMessage = true
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = WeatherAlert.copy(alpha = 0.15f), contentColor = WeatherAlert)
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationSwitchRow(
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title, style = MaterialTheme.typography.bodyMedium)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
