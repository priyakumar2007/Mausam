package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherSafe

@Composable
fun LocationPermissionScreen(
  userName: String,
  onRequestPermission: () -> Unit,
  onSkipToDemo: () -> Unit,
  modifier: Modifier = Modifier
) {
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) {
    onRequestPermission()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "Hi, $userName 👋",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "To deliver weather that fits your life, MAUSAM identifies your exact village or locality.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "📍 Exact Locality System",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MausamBlue
          )

          Spacer(modifier = Modifier.height(12.dp))

          LocalityStepRow(
            icon = Icons.Default.Place,
            title = "Village / Specific Locality",
            desc = "e.g., Melmaruvathur or Acharapakkam"
          )
          LocalityStepRow(
            icon = Icons.Default.LocationCity,
            title = "District & Taluk",
            desc = "e.g., Chengalpattu District"
          )
          LocalityStepRow(
            icon = Icons.Default.LocationOn,
            title = "State & Region",
            desc = "Tamil Nadu, India"
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = {
          permissionLauncher.launch(
            arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
            )
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("enable_location_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MausamBlue,
          contentColor = Color.White
        )
      ) {
        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Enable Location Detection",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onSkipToDemo,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("use_demo_location_button"),
        shape = RoundedCornerShape(16.dp)
      ) {
        Text("Continue with Melmaruvathur (Demo)")
      }
    }
  }
}

@Composable
private fun LocalityStepRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(MausamCyan.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MausamBlue,
        modifier = Modifier.size(16.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
