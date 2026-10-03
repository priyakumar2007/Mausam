package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LocationInfo
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.WeatherSafe

@Composable
fun LocationSearchDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onUseCurrentLocation: () -> Unit,
  onSearchQuery: (String) -> Unit,
  searchResults: List<LocationInfo>,
  isSearching: Boolean,
  onSelectLocation: (LocationInfo) -> Unit
) {
  if (!isOpen) return

  var query by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 520.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Select Locality",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // "Use Current Location" Quick Action
        Button(
          onClick = {
            onUseCurrentLocation()
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("use_gps_location_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MausamBlue)
        ) {
          Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Use Current GPS Location")
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = query,
          onValueChange = {
            query = it
            onSearchQuery(it)
          },
          placeholder = { Text("Village, Town, City, District...") },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("location_search_field"),
          shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isSearching) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = MausamBlue)
          }
        } else if (searchResults.isNotEmpty()) {
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(searchResults) { loc ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectLocation(loc) }
                  .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.LocationCity,
                  contentDescription = null,
                  tint = MausamBlue,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = loc.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  if (loc.fullAddress.isNotBlank()) {
                    Text(
                      text = loc.fullAddress,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
              HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
          }
        } else if (query.isNotBlank() && query.length >= 2) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No matching localities found. Try another search term.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          // Curated Popular Locations in Tamil Nadu
          Text(
            text = "Suggested Places:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
          )

          val suggestions = listOf(
            LocationInfo("Melmaruvathur", "Chengalpattu", "Tamil Nadu", "India", 12.4334, 79.8297),
            LocationInfo("Acharapakkam", "Chengalpattu", "Tamil Nadu", "India", 12.4167, 79.8167),
            LocationInfo("Chennai", "Chennai", "Tamil Nadu", "India", 13.0827, 80.2707),
            LocationInfo("Mahabalipuram", "Chengalpattu", "Tamil Nadu", "India", 12.6169, 80.1983),
            LocationInfo("Puducherry", "Puducherry", "Puducherry", "India", 11.9416, 79.8083)
          )

          LazyColumn {
            items(suggestions) { loc ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectLocation(loc) }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = WeatherSafe,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${loc.displayName} (${loc.district})",
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          }
        }
      }
    }
  }
}
