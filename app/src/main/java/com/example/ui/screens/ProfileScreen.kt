package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple

@Composable
fun ProfileScreen(
  userProfile: UserProfileEntity,
  onOpenSettings: () -> Unit,
  onManageLifestyles: () -> Unit,
  onOpenCropFields: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "User Profile",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Surface(
          modifier = Modifier.clickable { onOpenSettings() },
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.padding(8.dp)
          )
        }
      }
    }

    // Avatar Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(MausamBlue, MausamPurple))),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = userProfile.name.take(1).uppercase(),
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 26.sp
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            Text(
              text = userProfile.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = userProfile.email,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = if (userProfile.isDemoMode) MausamPurple.copy(alpha = 0.15f) else MausamBlue.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = if (userProfile.isDemoMode) "DEMO MODE ACTIVE" else if (userProfile.isGuest) "GUEST PROFILE" else "GOOGLE ACCOUNT",
                color = if (userProfile.isDemoMode) MausamPurple else MausamBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    // Lifestyle Management
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onManageLifestyles() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Style,
            contentDescription = null,
            tint = MausamBlue
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "Manage Lifestyles", fontWeight = FontWeight.Bold)
            Text(
              text = "${userProfile.lifestyles.size} active (${userProfile.lifestyles.joinToString(", ")})",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
      }
    }

    // Agriculture Fields Management
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenCropFields() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Agriculture,
            contentDescription = null,
            tint = MausamIndigo
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "My Agricultural Fields", fontWeight = FontWeight.Bold)
            Text(
              text = "Track native field locations & historical crop risk scans",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
      }
    }

    // Privacy & Data Usage Section
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = MausamBlue
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Privacy & Data Protection", fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "• Location Data: Device GPS is strictly used to retrieve real-time weather and reverse geocoding for your immediate locality. Your physical location is never sold to third-party ad networks.\n" +
                "• Field Photos: Crop leaf photos are parsed locally to extract image EXIF GPS coordinates and assess foliage risk. Images are not stored on remote servers without permission.\n" +
                "• Offline Cache: All saved fields, routes, and preferences remain securely stored in your local on-device Room database.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    }

    // App Info Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "About MAUSAM", fontWeight = FontWeight.Bold)
          Text(
            text = "Version 1.0 (Production) • Personal weather & lifestyle intelligence companion designed for Tamil Nadu and global regions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}
