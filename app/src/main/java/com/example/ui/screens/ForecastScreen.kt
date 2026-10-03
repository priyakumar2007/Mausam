package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.WeatherData
import com.example.ui.components.HourlyRainProbabilityBarPlot
import com.example.ui.components.HourlyTemperaturePlot
import com.example.ui.components.HourlyWindSpeedPlot
import com.example.ui.components.SevenDayTemperatureRangePlot
import com.example.ui.components.getWeatherIconForCode
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSun

@Composable
fun ForecastScreen(
  weatherData: WeatherData?,
  selectedDayIndex: Int,
  onSelectDay: (Int) -> Unit,
  isFahrenheit: Boolean = false,
  modifier: Modifier = Modifier
) {
  if (weatherData == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Forecast data is loading...")
    }
    return
  }

  val selectedDay = weatherData.daily.getOrNull(selectedDayIndex) ?: weatherData.daily.first()

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.CalendarMonth,
          contentDescription = null,
          tint = MausamBlue,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "7-Day & Hourly Forecast",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = "Tap any day to inspect full hourly predictions and conditions.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Horizontal Day Selector
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        itemsIndexed(weatherData.daily) { index, item ->
          DaySelectorChip(
            item = item,
            isSelected = index == selectedDayIndex,
            onClick = { onSelectDay(index) }
          )
        }
      }
    }

    // Selected Day Summary Card
    item {
      SelectedDayDetailCard(day = selectedDay, isFahrenheit = isFahrenheit)
    }

    // 24-Hour Temperature Curve for this location
    item {
      HourlyTemperaturePlot(
        hourly = weatherData.hourly,
        isFahrenheit = isFahrenheit
      )
    }

    // Rain probability chart
    item {
      HourlyRainProbabilityBarPlot(hourly = weatherData.hourly)
    }

    // 24-Hour Wind Speed Chart
    item {
      HourlyWindSpeedPlot(hourly = weatherData.hourly)
    }

    // 7-Day High / Low Range Chart
    item {
      SevenDayTemperatureRangePlot(
        daily = weatherData.daily,
        isFahrenheit = isFahrenheit
      )
    }

    // Hourly Timeline List
    item {
      Text(
        text = "Detailed Hourly Timeline",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
      )
    }

    item {
      val scrollState = rememberScrollState()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        weatherData.hourly.take(24).forEach { item ->
          HourlyMiniCard(item = item, isFahrenheit = isFahrenheit)
        }
      }
    }
  }
}

@Composable
private fun DaySelectorChip(
  item: DailyForecastItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier.clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) MausamBlue else MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = if (isSelected) 4.dp else 0.dp
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = item.dayOfWeek,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Icon(
        imageVector = getWeatherIconForCode(item.weatherCode, true),
        contentDescription = item.condition,
        tint = if (isSelected) WeatherSun else MausamCyan,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${item.maxTempC.toInt()}°",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun SelectedDayDetailCard(
  day: DailyForecastItem,
  isFahrenheit: Boolean
) {
  val max = if (isFahrenheit) (day.maxTempC * 9 / 5 + 32).toInt() else day.maxTempC.toInt()
  val min = if (isFahrenheit) (day.minTempC * 9 / 5 + 32).toInt() else day.minTempC.toInt()
  val unit = if (isFahrenheit) "°F" else "°C"

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "${day.dayOfWeek}, ${day.dateLabel}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = day.condition,
            style = MaterialTheme.typography.bodyMedium,
            color = MausamBlue,
            fontWeight = FontWeight.SemiBold
          )
        }
        Text(
          text = "$max$unit / $min$unit",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        DetailStatItem(
          icon = Icons.Default.WaterDrop,
          label = "Rain Prob",
          value = "${day.rainProb}%",
          color = WeatherRain
        )
        DetailStatItem(
          icon = Icons.Default.Air,
          label = "Wind Max",
          value = "${day.windKmh.toInt()} km/h",
          color = MausamCyan
        )
        DetailStatItem(
          icon = Icons.Default.WbSunny,
          label = "UV Index",
          value = "${day.uvMax.toInt()}",
          color = WeatherSun
        )
        DetailStatItem(
          icon = Icons.Default.WbTwilight,
          label = "Sunrise",
          value = day.sunrise,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun DetailStatItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  color: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = color,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 10.sp
    )
  }
}

@Composable
private fun HourlyMiniCard(
  item: HourlyForecastItem,
  isFahrenheit: Boolean
) {
  val temp = if (isFahrenheit) (item.tempC * 9 / 5 + 32).toInt() else item.tempC.toInt()

  Card(
    modifier = Modifier.width(76.dp),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = item.timeLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Icon(
        imageVector = getWeatherIconForCode(item.weatherCode, item.hourInt in 6..18),
        contentDescription = item.condition,
        tint = WeatherSun,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "$temp°",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${item.rainProb}%",
        style = MaterialTheme.typography.labelSmall,
        color = if (item.rainProb >= 40) WeatherRain else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }
  }
}
