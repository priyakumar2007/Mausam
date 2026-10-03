package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSun
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateNext: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "splash")

  val sunScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sunScale"
  )

  val cloudOffset by infiniteTransition.animateFloat(
    initialValue = -12f,
    targetValue = 12f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "cloudOffset"
  )

  val rainFall by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 40f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rainFall"
  )

  LaunchedEffect(Unit) {
    delay(2400)
    onNavigateNext()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MausamIndigo,
            MausamBlue,
            MausamPurple.copy(alpha = 0.85f)
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Weather Animation Container
      Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
      ) {
        // Glowing Sun Behind
        Icon(
          imageVector = Icons.Default.WbSunny,
          contentDescription = null,
          tint = WeatherSun,
          modifier = Modifier
            .size(85.dp)
            .scale(sunScale)
            .align(Alignment.TopEnd)
            .padding(end = 12.dp, top = 8.dp)
        )

        // Floating Cloud Shape via Canvas
        Canvas(
          modifier = Modifier
            .size(130.dp, 80.dp)
            .align(Alignment.Center)
        ) {
          val ox = cloudOffset
          drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = 32.dp.toPx(),
            center = Offset(size.width * 0.45f + ox, size.height * 0.5f)
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = 24.dp.toPx(),
            center = Offset(size.width * 0.25f + ox, size.height * 0.65f)
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = 28.dp.toPx(),
            center = Offset(size.width * 0.7f + ox, size.height * 0.62f)
          )

          // Falling Rain Drops
          val dropX1 = size.width * 0.35f + ox
          val dropX2 = size.width * 0.55f + ox
          val dropX3 = size.width * 0.70f + ox
          val startY = size.height * 0.85f

          drawLine(
            color = WeatherRain,
            start = Offset(dropX1, startY + (rainFall % 30)),
            end = Offset(dropX1 - 2, startY + (rainFall % 30) + 12),
            strokeWidth = 3f
          )
          drawLine(
            color = WeatherRain,
            start = Offset(dropX2, startY + ((rainFall + 15) % 30)),
            end = Offset(dropX2 - 2, startY + ((rainFall + 15) % 30) + 12),
            strokeWidth = 3f
          )
          drawLine(
            color = WeatherRain,
            start = Offset(dropX3, startY + ((rainFall + 8) % 30)),
            end = Offset(dropX3 - 2, startY + ((rainFall + 8) % 30) + 12),
            strokeWidth = 3f
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "MAUSAM",
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 4.sp,
        color = Color.White
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Weather that fits your life.",
        style = MaterialTheme.typography.titleMedium,
        color = MausamCyan,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
