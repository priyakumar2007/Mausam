package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LifestyleType
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherSafe

@Composable
fun LifestyleSelectionScreen(
  selectedLifestyles: List<String>,
  onToggleLifestyle: (String) -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allLifestyles = listOf(
    LifestyleType.HEALTH,
    LifestyleType.FITNESS,
    LifestyleType.BEACH,
    LifestyleType.TRAVEL,
    LifestyleType.FAMILY,
    LifestyleType.AGRICULTURE,
    LifestyleType.COMMUTER,
    LifestyleType.EVENTS
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 24.dp)
  ) {
    Text(
      text = "Tailor Your MAUSAM",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Text(
      text = "Select what matters most to your day. You can choose multiple lifestyles.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(allLifestyles) { item ->
        val isSelected = selectedLifestyles.contains(item.id)
        LifestyleCard(
          item = item,
          isSelected = isSelected,
          onClick = { onToggleLifestyle(item.id) }
        )
      }
    }

    Button(
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("finish_lifestyle_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MausamBlue,
        contentColor = Color.White
      )
    ) {
      Text(
        text = "Enter MAUSAM (${selectedLifestyles.size} Selected)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
private fun LifestyleCard(
  item: LifestyleType,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(96.dp)
      .clickable { onClick() }
      .testTag("lifestyle_${item.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) MausamBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    ),
    border = if (isSelected) BorderStroke(2.dp, MausamBlue) else null
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp)
    ) {
      Column(
        modifier = Modifier.align(Alignment.CenterStart)
      ) {
        Text(
          text = item.emoji,
          fontSize = 26.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (isSelected) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Selected",
          tint = MausamBlue,
          modifier = Modifier
            .size(20.dp)
            .align(Alignment.TopEnd)
        )
      }
    }
  }
}
