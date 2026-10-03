package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PersonalizationEngine
import com.example.ui.MausamUiState
import com.example.ui.MausamViewModel
import com.example.ui.navigation.MainNavTab
import com.example.ui.navigation.Screen
import com.example.ui.components.LocationSearchDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BeachScreen
import com.example.ui.screens.CommuterModeScreen
import com.example.ui.screens.CropRiskScreen
import com.example.ui.screens.EventsModeScreen
import com.example.ui.screens.FamilyModeScreen
import com.example.ui.screens.FitnessModeScreen
import com.example.ui.screens.ForecastScreen
import com.example.ui.screens.HealthModeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LifestyleSelectionScreen
import com.example.ui.screens.LocationPermissionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TravelScreen
import com.example.ui.theme.MausamBlue
import com.example.ui.theme.MausamCyan
import com.example.ui.theme.MausamIndigo
import com.example.ui.theme.MausamPurple
import com.example.ui.theme.MausamTheme
import com.example.ui.theme.WeatherSafe
import com.example.ui.theme.WeatherSunWarm

class MainActivity : ComponentActivity() {
  private val viewModel: MausamViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MausamTheme {
        val uiState by viewModel.uiState.collectAsState()
        MausamAppContent(viewModel = viewModel, uiState = uiState)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MausamAppContent(
  viewModel: MausamViewModel,
  uiState: MausamUiState
) {
  // BackHandler to navigate back smoothly using state manager
  BackHandler(enabled = viewModel.navigator.canGoBack) {
    viewModel.navigateBack()
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTabletOrWide = maxWidth >= 600.dp

    Crossfade(targetState = uiState.currentScreen, label = "screen_transition") { screen ->
      when (screen) {
        is Screen.Splash -> {
          SplashScreen(
            onNavigateNext = {
              viewModel.navigateTo(Screen.Login)
            }
          )
        }

        is Screen.Login -> {
          LoginScreen(
            onLoginWithGoogle = { viewModel.loginWithGoogle() },
            onLoginAsGuest = { viewModel.loginAsGuest() },
            onTryDemo = { viewModel.activateDemoMode() }
          )
        }

        is Screen.LocationPermission -> {
          LocationPermissionScreen(
            userName = uiState.userProfile.name,
            onRequestPermission = { viewModel.requestLocationDetection() },
            onSkipToDemo = { viewModel.activateDemoMode() }
          )
        }

        is Screen.LifestyleSelection -> {
          LifestyleSelectionScreen(
            selectedLifestyles = uiState.userProfile.lifestyles,
            onToggleLifestyle = { viewModel.toggleLifestyle(it) },
            onContinue = { viewModel.finishLifestyleSetup() }
          )
        }

        is Screen.Home, is Screen.MainHome, is Screen.Lifestyle, is Screen.Forecast, is Screen.Alerts, is Screen.Profile -> {
          MainAppScaffold(
            uiState = uiState,
            viewModel = viewModel,
            isWideScreen = isTabletOrWide,
            currentScreen = screen
          )
        }

        is Screen.HealthDetail -> {
          HealthModeScreen(
            weather = uiState.weatherData,
            airQuality = uiState.airQualityData,
            onBack = { viewModel.navigateBack() }
          )
        }

        is Screen.FitnessDetail -> {
          FitnessModeScreen(
            weather = uiState.weatherData,
            onBack = { viewModel.navigateBack() }
          )
        }

        is Screen.FamilyDetail -> {
          FamilyModeScreen(
            weather = uiState.weatherData,
            onBack = { viewModel.navigateBack() }
          )
        }

        is Screen.CommuterDetail -> {
          CommuterModeScreen(
            weather = uiState.weatherData,
            onBack = { viewModel.navigateBack() }
          )
        }

        is Screen.EventsDetail -> {
          EventsModeScreen(
            weather = uiState.weatherData,
            onBack = { viewModel.navigateBack() }
          )
        }

        is Screen.CropRiskDetail -> {
          Scaffold(
            topBar = {
              TopAppBar(
                title = { Text("Crop Risk Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                  IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Back")
                  }
                }
              )
            }
          ) { padding ->
            CropRiskScreen(
              cropAssessment = uiState.cropAssessment,
              cropImageUri = uiState.cropImageUri,
              cropFieldLocation = uiState.cropFieldLocation,
              myFields = uiState.myFields,
              myAnalyses = uiState.myAnalyses,
              isAnalyzing = uiState.isCropAnalyzing,
              errorMessage = uiState.cropError,
              onImageSelected = { viewModel.handleCropImageSelected(it) },
              onChangeFieldLocation = { viewModel.openSearch(true) },
              onSaveNewField = { name, crop, loc -> viewModel.saveNewField(name, crop, loc) },
              modifier = Modifier.padding(padding)
            )
          }
        }

        is Screen.TravelDetail -> {
          Scaffold(
            topBar = {
              TopAppBar(
                title = { Text("Travel Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                  IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Back")
                  }
                }
              )
            }
          ) { padding ->
            TravelScreen(
              source = uiState.travelSource,
              destination = uiState.travelDestination,
              travelComparison = uiState.travelComparison,
              isLoading = uiState.isTravelLoading,
              onCalculateRoutes = { src, dst -> viewModel.calculateTravel(src, dst) },
              onUseCurrentAsSource = { viewModel.calculateTravel(uiState.currentLocation, uiState.travelDestination) },
              modifier = Modifier.padding(padding)
            )
          }
        }

        is Screen.BeachDetail -> {
          Scaffold(
            topBar = {
              TopAppBar(
                title = { Text("Beach Marine Finder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                  IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Back")
                  }
                }
              )
            }
          ) { padding ->
            BeachScreen(
              beaches = uiState.nearbyBeaches,
              isLoading = uiState.isBeachLoading,
              onRefresh = { viewModel.loadNearbyBeaches() },
              modifier = Modifier.padding(padding)
            )
          }
        }

        is Screen.AssistantDetail -> {
          Scaffold(
            topBar = {
              TopAppBar(
                title = { Text("Ask MAUSAM", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                  IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Back")
                  }
                }
              )
            }
          ) { padding ->
            AssistantScreen(
              messages = uiState.chatMessages,
              isReplying = uiState.isChatReplying,
              language = uiState.userProfile.language,
              onSendMessage = { viewModel.sendChatMessage(it) },
              modifier = Modifier.padding(padding)
            )
          }
        }

        is Screen.SettingsDetail -> {
          SettingsScreen(
            userProfile = uiState.userProfile,
            onUpdateUnit = { viewModel.updateTemperatureUnit(it) },
            onUpdateLanguage = { viewModel.updateLanguage(it) },
            onClearCache = { viewModel.clearCache() },
            onToggleDemoMode = { viewModel.activateDemoMode() },
            onBack = { viewModel.navigateBack() }
          )
        }
      }
    }

    // Global Location Search Dialog
    LocationSearchDialog(
      isOpen = uiState.isSearchOpen,
      onDismiss = { viewModel.openSearch(false) },
      onUseCurrentLocation = { viewModel.requestLocationDetection() },
      onSearchQuery = { viewModel.searchPlaces(it) },
      searchResults = uiState.searchResults,
      isSearching = uiState.isSearching,
      onSelectLocation = { viewModel.selectLocation(it) }
    )
  }
}

@Composable
private fun MainAppScaffold(
  uiState: MausamUiState,
  viewModel: MausamViewModel,
  isWideScreen: Boolean,
  currentScreen: Screen
) {
  val selectedTab = when (currentScreen) {
    is Screen.Home, is Screen.MainHome -> MainNavTab.HOME
    is Screen.Lifestyle -> MainNavTab.LIFESTYLE
    is Screen.Forecast -> MainNavTab.FORECAST
    is Screen.Alerts -> MainNavTab.ALERTS
    is Screen.Profile -> MainNavTab.PROFILE
    else -> uiState.selectedTab
  }

  if (isWideScreen) {
    // Wide Screen / Tablet Layout: NavigationRail on side
    Row(modifier = Modifier.fillMaxSize()) {
      NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationRailItem(
          selected = selectedTab == MainNavTab.HOME,
          onClick = { viewModel.navigateTo(Screen.Home) },
          icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Home") }
        )
        NavigationRailItem(
          selected = selectedTab == MainNavTab.LIFESTYLE,
          onClick = { viewModel.navigateTo(Screen.Lifestyle) },
          icon = { Icon(Icons.Default.Style, contentDescription = "Lifestyle") },
          label = { Text("Lifestyle") }
        )
        NavigationRailItem(
          selected = selectedTab == MainNavTab.FORECAST,
          onClick = { viewModel.navigateTo(Screen.Forecast) },
          icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Forecast") },
          label = { Text("Forecast") }
        )
        NavigationRailItem(
          selected = selectedTab == MainNavTab.ALERTS,
          onClick = { viewModel.navigateTo(Screen.Alerts) },
          icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
          label = { Text("Alerts") }
        )
        NavigationRailItem(
          selected = selectedTab == MainNavTab.PROFILE,
          onClick = { viewModel.navigateTo(Screen.Profile) },
          icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
          label = { Text("Profile") }
        )
      }

      Box(modifier = Modifier.weight(1f)) {
        HomeTabContent(
          selectedTab = selectedTab,
          uiState = uiState,
          viewModel = viewModel
        )
      }
    }
  } else {
    // Mobile Layout: Bottom Navigation Bar (Home, Lifestyle, Forecast, Alerts, Profile)
    Scaffold(
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          NavigationBarItem(
            selected = selectedTab == MainNavTab.HOME,
            onClick = { viewModel.navigateTo(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_home")
          )
          NavigationBarItem(
            selected = selectedTab == MainNavTab.LIFESTYLE,
            onClick = { viewModel.navigateTo(Screen.Lifestyle) },
            icon = { Icon(Icons.Default.Style, contentDescription = "Lifestyle") },
            label = { Text("Lifestyle", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_lifestyle")
          )
          NavigationBarItem(
            selected = selectedTab == MainNavTab.FORECAST,
            onClick = { viewModel.navigateTo(Screen.Forecast) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Forecast") },
            label = { Text("Forecast", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_forecast")
          )
          NavigationBarItem(
            selected = selectedTab == MainNavTab.ALERTS,
            onClick = { viewModel.navigateTo(Screen.Alerts) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_alerts")
          )
          NavigationBarItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { viewModel.navigateTo(Screen.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_profile")
          )
        }
      }
    ) { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        HomeTabContent(
          selectedTab = selectedTab,
          uiState = uiState,
          viewModel = viewModel
        )
      }
    }
  }
}

@Composable
private fun HomeTabContent(
  selectedTab: MainNavTab,
  uiState: MausamUiState,
  viewModel: MausamViewModel
) {
  when (selectedTab) {
    MainNavTab.HOME -> {
      HomeScreen(
        uiState = uiState,
        onLocationClick = { viewModel.openSearch(true) },
        onNotificationsClick = { viewModel.navigateTo(Screen.Alerts) },
        onProfileClick = { viewModel.navigateTo(Screen.Profile) },
        onCropClick = { viewModel.navigateTo(Screen.CropRiskDetail) },
        onTravelClick = { viewModel.navigateTo(Screen.TravelDetail) },
        onBeachClick = { viewModel.navigateTo(Screen.BeachDetail) },
        onForecastClick = { viewModel.navigateTo(Screen.Forecast) },
        onLifestyleClick = { viewModel.navigateTo(Screen.Lifestyle) }
      )
    }

    MainNavTab.LIFESTYLE -> {
      // Hub for lifestyle actions & details covering all 8 lifestyles
      LifestyleHubScreen(
        uiState = uiState,
        onOpenHealth = { viewModel.navigateTo(Screen.HealthDetail) },
        onOpenFitness = { viewModel.navigateTo(Screen.FitnessDetail) },
        onOpenCrop = { viewModel.navigateTo(Screen.CropRiskDetail) },
        onOpenTravel = { viewModel.navigateTo(Screen.TravelDetail) },
        onOpenBeach = { viewModel.navigateTo(Screen.BeachDetail) },
        onOpenFamily = { viewModel.navigateTo(Screen.FamilyDetail) },
        onOpenCommuter = { viewModel.navigateTo(Screen.CommuterDetail) },
        onOpenEvents = { viewModel.navigateTo(Screen.EventsDetail) },
        onOpenAssistant = { viewModel.navigateTo(Screen.AssistantDetail) }
      )
    }

    MainNavTab.FORECAST -> {
      ForecastScreen(
        weatherData = uiState.weatherData,
        selectedDayIndex = uiState.selectedDayIndex,
        onSelectDay = { viewModel.selectDayIndex(it) },
        isFahrenheit = uiState.userProfile.tempUnit == "F"
      )
    }

    MainNavTab.ALERTS -> {
      val alerts = remember(uiState.weatherData) {
        uiState.weatherData?.let { PersonalizationEngine().generateAlerts(it) } ?: emptyList()
      }
      AlertsScreen(alerts = alerts)
    }

    MainNavTab.PROFILE -> {
      ProfileScreen(
        userProfile = uiState.userProfile,
        onOpenSettings = { viewModel.navigateTo(Screen.SettingsDetail) },
        onManageLifestyles = { viewModel.navigateTo(Screen.LifestyleSelection) },
        onOpenCropFields = { viewModel.navigateTo(Screen.CropRiskDetail) }
      )
    }
  }
}

@Composable
fun LifestyleHubScreen(
  uiState: MausamUiState,
  onOpenHealth: () -> Unit,
  onOpenFitness: () -> Unit,
  onOpenCrop: () -> Unit,
  onOpenTravel: () -> Unit,
  onOpenBeach: () -> Unit,
  onOpenFamily: () -> Unit,
  onOpenCommuter: () -> Unit,
  onOpenEvents: () -> Unit,
  onOpenAssistant: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = "Lifestyles & Intelligence",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Select any of the 8 dedicated companion modules tailored to your day",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // 1. Health
    item {
      LifestyleCardHubItem(
        emoji = "❤️",
        title = "Health & Wellness",
        subtitle = "AQI, PM2.5, heat stress index & hydration advisories",
        color = WeatherSafe,
        icon = Icons.Default.HealthAndSafety,
        onClick = onOpenHealth
      )
    }

    // 2. Fitness
    item {
      LifestyleCardHubItem(
        emoji = "🏃",
        title = "Fitness & Workouts",
        subtitle = "Running, walking, cycling comfort scores & best workout hours",
        color = MausamBlue,
        icon = Icons.Default.FitnessCenter,
        onClick = onOpenFitness
      )
    }

    // 3. Agriculture
    item {
      LifestyleCardHubItem(
        emoji = "🌱",
        title = "Agriculture & Crop Risk",
        subtitle = "Foliage camera/upload, EXIF GPS detection & field weather combinations",
        color = WeatherSafe,
        icon = Icons.Default.Agriculture,
        onClick = onOpenCrop
      )
    }

    // 4. Travel
    item {
      LifestyleCardHubItem(
        emoji = "✈️",
        title = "Travel Planner & Routes",
        subtitle = "Shortest vs bypass alternative routes with weather checkpoints",
        color = MausamBlue,
        icon = Icons.Default.DirectionsCar,
        onClick = onOpenTravel
      )
    }

    // 5. Beach
    item {
      LifestyleCardHubItem(
        emoji = "🏖️",
        title = "Nearest Beach & Marine Swell",
        subtitle = "Wave heights, swell safety status & calmer beach recommendations",
        color = MausamCyan,
        icon = Icons.Default.BeachAccess,
        onClick = onOpenBeach
      )
    }

    // 6. Parents & Family
    item {
      LifestyleCardHubItem(
        emoji = "👨‍👩‍👧",
        title = "Parents & Family Commute",
        subtitle = "Morning school bell weather, rain protection & pickup windows",
        color = MausamIndigo,
        icon = Icons.Default.FamilyRestroom,
        onClick = onOpenFamily
      )
    }

    // 7. Commuter
    item {
      LifestyleCardHubItem(
        emoji = "🚗",
        title = "Daily Commuter Transit",
        subtitle = "Road surface grip, reduced visibility mist alerts & bridge winds",
        color = MausamBlue,
        icon = Icons.Default.DirectionsCar,
        onClick = onOpenCommuter
      )
    }

    // 8. Events
    item {
      LifestyleCardHubItem(
        emoji = "🎉",
        title = "Events & Outdoor Planner",
        subtitle = "Outdoor suitability scores (0-100), heat/wind alerts & canopy tips",
        color = WeatherSunWarm,
        icon = Icons.Default.Event,
        onClick = onOpenEvents
      )
    }

    // Assistant
    item {
      LifestyleCardHubItem(
        emoji = "🤖",
        title = "Ask MAUSAM AI Assistant",
        subtitle = "Context-aware conversational companion in English, Tamil & Tanglish",
        color = MausamPurple,
        icon = Icons.Default.Psychology,
        onClick = onOpenAssistant
      )
    }
  }
}

@Composable
private fun LifestyleCardHubItem(
  emoji: String,
  title: String,
  subtitle: String,
  color: androidx.compose.ui.graphics.Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(emoji, fontSize = 28.sp)
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Text(
          subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(imageVector = icon, contentDescription = null, tint = color)
    }
  }
}
