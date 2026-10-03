package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.CropAnalysisEntity
import com.example.data.local.CropFieldEntity
import com.example.data.model.CropRiskAssessment
import com.example.data.model.CropRiskLevel
import com.example.data.model.LocationInfo
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.WeatherAlert
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherWarning
import java.io.File

@Composable
fun CropRiskScreen(
  cropAssessment: CropRiskAssessment?,
  cropImageUri: Uri?,
  cropFieldLocation: LocationInfo?,
  myFields: List<CropFieldEntity>,
  myAnalyses: List<CropAnalysisEntity>,
  isAnalyzing: Boolean,
  errorMessage: String?,
  onImageSelected: (Uri) -> Unit,
  onChangeFieldLocation: () -> Unit,
  onSaveNewField: (String, String, LocationInfo) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeTab by remember { mutableIntStateOf(0) } // 0: Analyze Photo, 1: My Fields & History
  var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

  // Gallery Picker
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { onImageSelected(it) }
  }

  // Camera Launcher
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success ->
    if (success && tempCameraUri != null) {
      onImageSelected(tempCameraUri!!)
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    TabRow(
      selectedTabIndex = activeTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Tab(
        selected = activeTab == 0,
        onClick = { activeTab = 0 },
        text = { Text("Crop Risk Analysis", fontWeight = FontWeight.SemiBold) }
      )
      Tab(
        selected = activeTab == 1,
        onClick = { activeTab = 1 },
        text = { Text("My Fields & History", fontWeight = FontWeight.SemiBold) }
      )
    }

    if (activeTab == 0) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Section Header
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WeatherSafe.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                tint = WeatherSafe,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "🌱 Crop Risk Engine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Visual Foliage Inspection + Image Location Weather",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Upload / Capture Buttons
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Field Photo Input",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "GPS EXIF metadata will be automatically detected to fetch weather for your exact field.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = {
                    val photoFile = File(context.cacheDir, "crop_cam_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(
                      context,
                      "${context.packageName}.fileprovider",
                      photoFile
                    )
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("camera_capture_button"),
                  shape = RoundedCornerShape(14.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MausamBlue)
                ) {
                  Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Live Camera")
                }

                OutlinedButton(
                  onClick = { galleryLauncher.launch("image/*") },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("gallery_upload_button"),
                  shape = RoundedCornerShape(14.dp)
                ) {
                  Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Gallery Upload")
                }
              }
            }
          }
        }

        // Image Preview & Field Location Badge
        if (cropImageUri != null) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
                  contentAlignment = Alignment.Center
                ) {
                  AsyncImage(
                    model = cropImageUri,
                    contentDescription = "Uploaded Crop Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )

                  if (isAnalyzing) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                          text = "Inspecting image & fetching field weather...",
                          color = Color.White,
                          style = MaterialTheme.typography.bodySmall
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Field Location Indicator (CRITICAL: Prioritizes Image Location!)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = null,
                      tint = WeatherSafe,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                      Text(
                        text = "🌱 Field Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = cropFieldLocation?.displayName ?: "Melmaruvathur Field",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                      )
                      cropFieldLocation?.fullAddress?.takeIf { it.isNotBlank() }?.let {
                        Text(
                          text = it,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }

                  OutlinedButton(
                    onClick = onChangeFieldLocation,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Text("Change Field", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }

        // Assessment Result
        if (cropAssessment != null && !isAnalyzing) {
          item {
            CropAssessmentResultCard(assessment = cropAssessment)
          }
        }

        if (errorMessage != null) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = WeatherAlert.copy(alpha = 0.12f))
            ) {
              Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WeatherAlert)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = WeatherAlert)
              }
            }
          }
        }
      }
    } else {
      // Tab 1: My Fields & History
      MyFieldsTabContent(
        myFields = myFields,
        myAnalyses = myAnalyses,
        onAddNewField = onSaveNewField
      )
    }
  }
}

@Composable
private fun CropAssessmentResultCard(assessment: CropRiskAssessment) {
  val levelColor = when (assessment.riskLevel) {
    CropRiskLevel.LOW -> WeatherSafe
    CropRiskLevel.MODERATE -> WeatherWarning
    CropRiskLevel.HIGH, CropRiskLevel.VERY_HIGH -> WeatherAlert
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
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
            text = "Crop Risk Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "AI-assisted visual observation",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          color = levelColor,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = assessment.riskLevel.label,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Field Weather Factors Summary
      Surface(
        color = MausamBlue.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = MausamBlue,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = assessment.weatherFactorSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Image Observation
      Text(
        text = "🔍 Visual Leaf Observation:",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = assessment.imageObservation,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
      )

      // 7 Specific Crop Risk Categories Breakdown
      if (assessment.categoryRisks.isNotEmpty()) {
        Text(
          text = "📊 Specific Risk Breakdown (7 Categories):",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        assessment.categoryRisks.forEach { cat ->
          val catColor = when (cat.level) {
            CropRiskLevel.LOW -> WeatherSafe
            CropRiskLevel.MODERATE -> WeatherWarning
            CropRiskLevel.HIGH, CropRiskLevel.VERY_HIGH -> WeatherAlert
          }
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = cat.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(text = cat.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Surface(color = catColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = cat.level.label,
                  color = catColor,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Prototype AI Image Analysis Inspection Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("🤖 ", fontSize = 16.sp)
              Text("Foliage AI Observation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Surface(color = MausamBlue.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
              Text(
                text = "${assessment.aiAnalysis.confidencePercent}% Confidence",
                color = MausamBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Identified Crop: ${assessment.aiAnalysis.cropType}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
          Text(text = "• Foliage Damage: ${assessment.aiAnalysis.visibleDamage}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "• Pigmentation: ${assessment.aiAnalysis.leafDiscoloration}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "• Disease Indicators: ${assessment.aiAnalysis.possibleDiseaseIndicators}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "• Pest Damage: ${assessment.aiAnalysis.pestIndicators}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "• Water Stress / Wilting: ${assessment.aiAnalysis.wiltingWaterStress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "• General Plant Status: ${assessment.aiAnalysis.generalCondition}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(8.dp))
          Surface(color = WeatherWarning.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
            Text(
              text = "⚠️ ${assessment.aiAnalysis.observationBadge} — ${assessment.aiAnalysis.expertVerificationNote}",
              color = WeatherWarning,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Reasons
      if (assessment.reasons.isNotEmpty()) {
        Text(
          text = "Contributing Factors:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        assessment.reasons.forEach { r ->
          Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Text("• ", fontWeight = FontWeight.Bold, color = levelColor)
            Text(r, style = MaterialTheme.typography.bodySmall)
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      // Suggestions
      if (assessment.suggestions.isNotEmpty()) {
        Text(
          text = "Recommended Agronomic Actions:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = WeatherSafe
        )
        assessment.suggestions.forEach { s ->
          Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = WeatherSafe,
              modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(s, style = MaterialTheme.typography.bodySmall)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.surface)
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = assessment.isLabDiagnosisWarning,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
private fun MyFieldsTabContent(
  myFields: List<CropFieldEntity>,
  myAnalyses: List<CropAnalysisEntity>,
  onAddNewField: (String, String, LocationInfo) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var fieldName by remember { mutableStateOf("") }
  var cropType by remember { mutableStateOf("Paddy") }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Saved Field Profiles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Monitor multiple plots & track historical risk scans",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Button(
          onClick = { showAddDialog = true },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = WeatherSafe)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Field")
        }
      }
    }

    if (myFields.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("🌾 Native Field (Melmaruvathur)", fontWeight = FontWeight.Bold)
            Text(
              "Crop: Paddy • Chengalpattu District",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      items(myFields) { field ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "🌾 ${field.fieldName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Surface(color = WeatherSafe.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(
                  text = field.cropType,
                  color = WeatherSafe,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "📍 ${field.locationName}, ${field.district}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Historical scans
    item {
      Text(
        text = "Crop Risk History",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    if (myAnalyses.isEmpty()) {
      item {
        Text(
          text = "No previous risk assessments recorded yet. Upload a field photo above to initiate the first scan.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      items(myAnalyses) { analysis ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = analysis.locationName, fontWeight = FontWeight.Bold)
              Text(
                text = analysis.observedIssues,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              color = if (analysis.riskLevel == "LOW") WeatherSafe else WeatherWarning,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = analysis.riskLevel,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Add Field Profile") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          TextField(
            value = fieldName,
            onValueChange = { fieldName = it },
            label = { Text("Field Name (e.g. Native Field)") },
            modifier = Modifier.fillMaxWidth()
          )
          TextField(
            value = cropType,
            onValueChange = { cropType = it },
            label = { Text("Crop Type (e.g. Paddy, Sugarcane)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (fieldName.isNotBlank()) {
              onAddNewField(
                fieldName,
                cropType,
                LocationInfo("Melmaruvathur", "Chengalpattu", "Tamil Nadu", "India", 12.4334, 79.8297)
              )
              showAddDialog = false
            }
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
