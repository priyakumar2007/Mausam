package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = MausamBlueLight,
  onPrimary = Color(0xFF00224D),
  primaryContainer = MausamIndigoDark,
  onPrimaryContainer = Color(0xFFDCE2FF),
  secondary = MausamCyanLight,
  onSecondary = Color(0xFF00363D),
  secondaryContainer = Color(0xFF004F58),
  onSecondaryContainer = Color(0xFF97F0FF),
  tertiary = MausamIndigoLight,
  onTertiary = Color(0xFF1B0061),
  background = BgDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = CardDark,
  onSurfaceVariant = TextSecondaryDark,
  error = WeatherAlert,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = MausamBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFDBEAFE),
  onPrimaryContainer = Color(0xFF1E3A8A),
  secondary = Color(0xFF0284C7),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE0F2FE),
  onSecondaryContainer = Color(0xFF0369A1),
  tertiary = MausamPurple,
  onTertiary = Color.White,
  background = BgLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = CardLightElevated,
  onSurfaceVariant = TextSecondaryLight,
  error = WeatherAlert,
  onError = Color.White
)

@Composable
fun MausamTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature weather tech theme by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
