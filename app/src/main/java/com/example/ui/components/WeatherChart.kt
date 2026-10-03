package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecastItem
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherRain
import com.example.ui.theme.WeatherSun

@Composable
fun HourlyTemperaturePlot(
  hourly: List<HourlyForecastItem>,
  modifier: Modifier = Modifier,
  isFahrenheit: Boolean = false
) {
  if (hourly.isEmpty()) return

  val items = hourly.take(24)
  val temps = items.map { if (isFahrenheit) it.tempC * 9 / 5 + 32 else it.tempC }
  val minTemp = (temps.minOrNull() ?: 20.0) - 2.0
  val maxTemp = (temps.maxOrNull() ?: 35.0) + 2.0
  val range = (maxTemp - minTemp).coerceAtLeast(1.0)

  val itemWidth = 56.dp
  val totalWidth = itemWidth * items.size
  val scrollState = rememberScrollState()

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "24-Hour Temperature Curve",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = if (isFahrenheit) "Unit: °F" else "Unit: °C",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          RoundedCornerShape(16.dp)
        )
        .padding(vertical = 12.dp)
        .horizontalScroll(scrollState)
    ) {
      Canvas(
        modifier = Modifier
          .width(totalWidth)
          .height(130.dp)
      ) {
        val w = size.width
        val h = size.height
        val pointSpacing = w / items.size

        val points = items.indices.map { i ->
          val temp = temps[i]
          val x = i * pointSpacing + (pointSpacing / 2f)
          val normalizedY = ((maxTemp - temp) / range).toFloat()
          val y = 20.dp.toPx() + normalizedY * (h - 55.dp.toPx())
          Offset(x, y)
        }

        // Draw gradient area below curve
        if (points.isNotEmpty()) {
          val areaPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              val prev = points[i - 1]
              val curr = points[i]
              val cx = (prev.x + curr.x) / 2f
              cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
            }
            lineTo(points.last().x, h - 25.dp.toPx())
            lineTo(points.first().x, h - 25.dp.toPx())
            close()
          }

          drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
              colors = listOf(
                MausamCyan.copy(alpha = 0.35f),
                MausamBlue.copy(alpha = 0.05f)
              ),
              startY = 0f,
              endY = h
            )
          )

          // Draw smooth stroke
          val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              val prev = points[i - 1]
              val curr = points[i]
              val cx = (prev.x + curr.x) / 2f
              cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
            }
          }

          drawPath(
            path = strokePath,
            brush = Brush.horizontalGradient(listOf(WeatherSun, MausamCyan, MausamBlue)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )

          // Draw point dots and text
          val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }
          val timePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
          }

          points.forEachIndexed { idx, pt ->
            // Circle dot
            drawCircle(
              color = Color.White,
              radius = 3.5.dp.toPx(),
              center = pt
            )
            drawCircle(
              color = MausamBlue,
              radius = 2.dp.toPx(),
              center = pt
            )

            // Temp label
            val tempText = "${temps[idx].toInt()}°"
            drawContext.canvas.nativeCanvas.drawText(
              tempText,
              pt.x,
              pt.y - 8.dp.toPx(),
              paint
            )

            // Time label
            drawContext.canvas.nativeCanvas.drawText(
              items[idx].timeLabel,
              pt.x,
              h - 6.dp.toPx(),
              timePaint
            )
          }
        }
      }
    }
  }
}

@Composable
fun HourlyRainProbabilityBarPlot(
  hourly: List<HourlyForecastItem>,
  modifier: Modifier = Modifier
) {
  if (hourly.isEmpty()) return
  val items = hourly.take(24)
  val scrollState = rememberScrollState()
  val itemWidth = 48.dp
  val totalWidth = itemWidth * items.size

  // Calculate natural language insight
  val highestProb = items.maxOfOrNull { it.rainProb } ?: 0
  val rainHours = items.filter { it.rainProb >= 40 }
  val summaryText = if (rainHours.isNotEmpty()) {
    val first = rainHours.first().timeLabel
    val last = rainHours.last().timeLabel
    "Rain probability increases between $first and $last (up to $highestProb%)."
  } else {
    "Low rain probability (below 25%) throughout the next 24 hours."
  }

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Rain Probability (Next 24h)",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Peak: $highestProb%",
        style = MaterialTheme.typography.labelSmall,
        color = WeatherRain
      )
    }

    Text(
      text = summaryText,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          RoundedCornerShape(16.dp)
        )
        .padding(vertical = 10.dp)
        .horizontalScroll(scrollState)
    ) {
      Canvas(
        modifier = Modifier
          .width(totalWidth)
          .height(110.dp)
      ) {
        val h = size.height
        val barWidth = 14.dp.toPx()
        val spacing = size.width / items.size

        val textPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 22f
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        val labelPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.LTGRAY
          textSize = 22f
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }

        items.forEachIndexed { i, item ->
          val cx = i * spacing + (spacing / 2f)
          val prob = item.rainProb
          val maxBarHeight = h - 45.dp.toPx()
          val barHeight = ((prob / 100f) * maxBarHeight).coerceAtLeast(3.dp.toPx())
          val topY = (h - 25.dp.toPx()) - barHeight

          val barColor = when {
            prob >= 60 -> WeatherRain
            prob >= 35 -> MausamCyan
            else -> MausamCyan.copy(alpha = 0.4f)
          }

          // Draw rounded bar
          drawRoundRect(
            color = barColor,
            topLeft = Offset(cx - barWidth / 2, topY),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
          )

          // Probability text above bar
          drawContext.canvas.nativeCanvas.drawText(
            "$prob%",
            cx,
            topY - 4.dp.toPx(),
            textPaint
          )

          // Time label below bar
          drawContext.canvas.nativeCanvas.drawText(
            item.timeLabel,
            cx,
            h - 4.dp.toPx(),
            labelPaint
          )
        }
      }
    }
  }
}

@Composable
fun HourlyWindSpeedPlot(
  hourly: List<HourlyForecastItem>,
  modifier: Modifier = Modifier
) {
  if (hourly.isEmpty()) return
  val items = hourly.take(24)
  val scrollState = rememberScrollState()
  val itemWidth = 52.dp
  val totalWidth = itemWidth * items.size
  val maxWind = (items.maxOfOrNull { it.windKmh } ?: 20.0).coerceAtLeast(10.0)

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Wind Speed (Next 24h)",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Max: ${maxWind.toInt()} km/h",
        style = MaterialTheme.typography.labelSmall,
        color = MausamCyan
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          RoundedCornerShape(16.dp)
        )
        .padding(vertical = 10.dp)
        .horizontalScroll(scrollState)
    ) {
      Canvas(
        modifier = Modifier
          .width(totalWidth)
          .height(95.dp)
      ) {
        val h = size.height
        val barWidth = 10.dp.toPx()
        val spacing = size.width / items.size

        val textPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 20f
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }
        val labelPaint = android.graphics.Paint().apply {
          color = android.graphics.Color.LTGRAY
          textSize = 20f
          textAlign = android.graphics.Paint.Align.CENTER
          isAntiAlias = true
        }

        items.forEachIndexed { i, item ->
          val cx = i * spacing + (spacing / 2f)
          val speed = item.windKmh
          val maxBarHeight = h - 38.dp.toPx()
          val barHeight = ((speed / maxWind) * maxBarHeight).toFloat().coerceAtLeast(4.dp.toPx())
          val topY = (h - 22.dp.toPx()) - barHeight

          drawRoundRect(
            color = MausamCyan.copy(alpha = 0.8f),
            topLeft = Offset(cx - barWidth / 2, topY),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
          )

          drawContext.canvas.nativeCanvas.drawText(
            "${speed.toInt()}",
            cx,
            topY - 4.dp.toPx(),
            textPaint
          )

          drawContext.canvas.nativeCanvas.drawText(
            item.timeLabel,
            cx,
            h - 4.dp.toPx(),
            labelPaint
          )
        }
      }
    }
  }
}

@Composable
fun SevenDayTemperatureRangePlot(
  daily: List<com.example.data.model.DailyForecastItem>,
  modifier: Modifier = Modifier,
  isFahrenheit: Boolean = false
) {
  if (daily.isEmpty()) return

  val overallMin = (daily.minOfOrNull { if (isFahrenheit) it.minTempC * 9 / 5 + 32 else it.minTempC } ?: 18.0) - 2.0
  val overallMax = (daily.maxOfOrNull { if (isFahrenheit) it.maxTempC * 9 / 5 + 32 else it.maxTempC } ?: 36.0) + 2.0
  val range = (overallMax - overallMin).coerceAtLeast(1.0)

  Column(modifier = modifier) {
    Text(
      text = "7-Day Temperature Range",
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        daily.take(7).forEach { day ->
          val min = if (isFahrenheit) day.minTempC * 9 / 5 + 32 else day.minTempC
          val max = if (isFahrenheit) day.maxTempC * 9 / 5 + 32 else day.maxTempC

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = day.dayOfWeek,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.width(48.dp)
            )

            Text(
              text = "${min.toInt()}°",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.width(32.dp)
            )

            Box(
              modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surface)
            ) {
              val startFrac = ((min - overallMin) / range).toFloat().coerceIn(0f, 1f)
              val endFrac = ((max - overallMin) / range).toFloat().coerceIn(0f, 1f)
              val widthFrac = (endFrac - startFrac).coerceAtLeast(0.08f)

              Canvas(modifier = Modifier.fillMaxSize()) {
                val barLeft = size.width * startFrac
                val barW = size.width * widthFrac
                drawRoundRect(
                  brush = Brush.horizontalGradient(listOf(MausamCyan, WeatherSun)),
                  topLeft = Offset(barLeft, 0f),
                  size = androidx.compose.ui.geometry.Size(barW, size.height),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
              text = "${max.toInt()}°",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.width(32.dp)
            )
          }
        }
      }
    }
  }
}
