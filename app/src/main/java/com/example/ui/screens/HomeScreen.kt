package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationInfo
import com.example.data.model.WeatherData
import com.example.domain.PersonalizationEngine
import com.example.ui.MausamUiState
import com.example.ui.components.AppHeader
import com.example.ui.components.HeroWeatherCard
import com.example.ui.components.HourlyRainProbabilityBarPlot
import com.example.ui.components.HourlyTemperaturePlot
import com.example.ui.components.ImportantAlertsBanner
import com.example.ui.components.LifestyleHighlightsCard
import com.example.ui.components.MausamInsightCard
import com.example.ui.components.QuickActionsRow
import com.example.ui.components.SevenDayForecastPreview
import com.example.ui.components.SmartSuggestionsList
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamPurple
import com.example.ui.theme.WeatherWarning

@Composable
fun HomeScreen(
  uiState: MausamUiState,
  onLocationClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onCropClick: () -> Unit,
  onTravelClick: () -> Unit,
  onBeachClick: () -> Unit,
  onForecastClick: () -> Unit,
  onLifestyleClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val personalizationEngine = remember { PersonalizationEngine() }
  val weather = uiState.weatherData
  val isFahrenheit = uiState.userProfile.tempUnit == "F"

  Column(modifier = modifier.fillMaxSize()) {
    // Top Bar with Exact Locality
    AppHeader(
      location = uiState.currentLocation,
      onLocationClick = onLocationClick,
      onNotificationsClick = onNotificationsClick,
      onProfileClick = onProfileClick
    )

    if (uiState.isWeatherLoading && weather == null) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = MausamBlue)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Analyzing weather for ${uiState.currentLocation.displayName}...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else if (weather != null) {
      val insight = remember(weather, uiState.userProfile.language) {
        personalizationEngine.generateInsight(weather, uiState.userProfile.language)
      }
      val alerts = remember(weather) {
        personalizationEngine.generateAlerts(weather)
      }
      val fitness = remember(weather, uiState.userProfile.language) {
        personalizationEngine.calculateFitnessInsight(weather, uiState.userProfile.language)
      }
      val health = remember(weather, uiState.airQualityData) {
        personalizationEngine.calculateHealthInsight(weather, uiState.airQualityData?.aqi ?: 45)
      }
      val suggestions = remember(weather) {
        personalizationEngine.generateSmartSuggestions(weather)
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Demo Mode / Offline Banner
        if (uiState.userProfile.isDemoMode) {
          item {
            Surface(
              color = MausamPurple.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("🧪 ", fontSize = 16.sp)
                Column {
                  Text(
                    text = "DEMO DATA MODE ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MausamPurple
                  )
                  Text(
                    text = "Using Melmaruvathur & South Coast preset datasets for presentation.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }
        } else if (uiState.isOffline) {
          item {
            Surface(
              color = WeatherWarning.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("📡 ", fontSize = 16.sp)
                Column {
                  Text(
                    text = "Offline — showing last available weather data.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = WeatherWarning
                  )
                  Text(
                    text = "Cached atmospheric observations preserved on-device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }
        }

        // 1. Hero Weather Card
        item {
          HeroWeatherCard(weather = weather, isFahrenheit = isFahrenheit)
        }

        // 2. Severe Alerts (Must appear before lifestyle according to prompt specs)
        if (alerts.isNotEmpty()) {
          item {
            ImportantAlertsBanner(
              alerts = alerts,
              onViewAllClick = onNotificationsClick
            )
          }
        }

        // 3. MAUSAM Insight
        item {
          MausamInsightCard(insightText = insight)
        }

        // 4. Quick Actions (Crop Risk, Travel, Beach, Change Location)
        item {
          QuickActionsRow(
            onCropClick = onCropClick,
            onTravelClick = onTravelClick,
            onBeachClick = onBeachClick,
            onChangeLocationClick = onLocationClick
          )
        }

        // 5. Lifestyle Highlights (Fitness score, Health AQI)
        item {
          LifestyleHighlightsCard(
            fitness = fitness,
            health = health,
            selectedLifestyles = uiState.userProfile.lifestyles,
            onOpenLifestyle = onLifestyleClick
          )
        }

        // 6. Smart Suggestions
        item {
          SmartSuggestionsList(suggestions = suggestions)
        }

        // 7. Live Weather Plots (Temperature curve)
        item {
          HourlyTemperaturePlot(
            hourly = weather.hourly,
            isFahrenheit = isFahrenheit
          )
        }

        // 8. Rain Probability Bar Chart
        item {
          HourlyRainProbabilityBarPlot(hourly = weather.hourly)
        }

        // 9. 7-Day Forecast preview
        item {
          SevenDayForecastPreview(
            days = weather.daily,
            onViewMore = onForecastClick
          )
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}
