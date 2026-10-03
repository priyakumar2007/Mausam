package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple
import com.example.ui.theme.WeatherSun

@Composable
fun LoginScreen(
  onLoginWithGoogle: () -> Unit,
  onLoginAsGuest: () -> Unit,
  onTryDemo: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            MaterialTheme.colorScheme.background,
            MausamIndigo.copy(alpha = 0.08f)
          )
        )
      )
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // App Logo
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(MausamIndigo, MausamBlue, MausamCyan))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = "Mausam Logo",
            tint = WeatherSun,
            modifier = Modifier.size(40.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "MAUSAM",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 2.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "“Weather that fits your life.”",
          style = MaterialTheme.typography.bodyMedium,
          color = MausamBlue,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Continue with Google Button
        Button(
          onClick = onLoginWithGoogle,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_login_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MausamIndigo,
            contentColor = Color.White
          )
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "G",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Continue with Google",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Continue as Guest Button
        OutlinedButton(
          onClick = onLoginAsGuest,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("guest_login_button"),
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Continue as Guest",
            style = MaterialTheme.typography.titleSmall
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Try Demo Mode Button
        Button(
          onClick = onTryDemo,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("demo_mode_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MausamCyan.copy(alpha = 0.15f),
            contentColor = MausamBlue
          )
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Explore Melmaruvathur (Demo Mode)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Local device intelligence • No mandatory paid subscriptions",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
