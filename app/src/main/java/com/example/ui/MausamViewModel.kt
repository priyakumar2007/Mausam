package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.local.CropAnalysisEntity
import com.example.data.local.CropFieldEntity
import com.example.data.local.MausamDatabase
import com.example.data.local.SavedLocationEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AirQualityData
import com.example.data.model.BeachSafetyDetail
import com.example.data.model.ChatMessage
import com.example.data.model.CropRiskAssessment
import com.example.data.model.LocationInfo
import com.example.data.model.TravelRouteComparison
import com.example.data.model.WeatherData
import com.example.data.repository.BeachRepository
import com.example.data.repository.CropRiskRepository
import com.example.data.repository.LocationHelper
import com.example.data.repository.TravelRepository
import com.example.data.repository.WeatherRepository
import com.example.domain.PersonalizationEngine
import com.example.domain.WeatherAssistantEngine
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.HomeTab
import com.example.ui.navigation.MainNavTab
import com.example.ui.navigation.NavigationStateManager
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MausamUiState(
  val currentScreen: Screen = Screen.Splash,
  val selectedTab: MainNavTab = MainNavTab.HOME,
  val userProfile: UserProfileEntity = UserProfileEntity(),
  val currentLocation: LocationInfo = LocationInfo(
    locality = "Melmaruvathur",
    district = "Chengalpattu District",
    state = "Tamil Nadu",
    country = "India",
    latitude = 12.4334,
    longitude = 79.8297,
    isDetectedGps = false
  ),
  val weatherData: WeatherData? = null,
  val airQualityData: AirQualityData? = null,
  val isWeatherLoading: Boolean = false,
  val weatherError: String? = null,
  val cropAssessment: CropRiskAssessment? = null,
  val cropImageUri: Uri? = null,
  val cropFieldLocation: LocationInfo? = null,
  val myFields: List<CropFieldEntity> = emptyList(),
  val myAnalyses: List<CropAnalysisEntity> = emptyList(),
  val isCropAnalyzing: Boolean = false,
  val cropError: String? = null,
  val travelComparison: TravelRouteComparison? = null,
  val travelSource: LocationInfo = LocationInfo("Melmaruvathur", "Chengalpattu", "Tamil Nadu", "India", 12.4334, 79.8297),
  val travelDestination: LocationInfo = LocationInfo("Chennai", "Chennai", "Tamil Nadu", "India", 13.0827, 80.2707),
  val isTravelLoading: Boolean = false,
  val nearbyBeaches: List<BeachSafetyDetail> = emptyList(),
  val isBeachLoading: Boolean = false,
  val chatMessages: List<ChatMessage> = emptyList(),
  val isChatReplying: Boolean = false,
  val isSearchOpen: Boolean = false,
  val searchResults: List<LocationInfo> = emptyList(),
  val isSearching: Boolean = false,
  val selectedDayIndex: Int = 0,
  val isOffline: Boolean = false
)

class MausamViewModel(application: Application) : AndroidViewModel(application) {
  private val db = MausamDatabase.getInstance(application)
  private val locationHelper = LocationHelper(application)
  private val weatherRepo = WeatherRepository(db)
  private val cropRepo = CropRiskRepository(application, db, weatherRepo)
  private val travelRepo = TravelRepository(weatherRepo)
  private val beachRepo = BeachRepository(weatherRepo)
  private val personalizationEngine = PersonalizationEngine()
  private val assistantEngine = WeatherAssistantEngine()

  private val _uiState = MutableStateFlow(MausamUiState())
  val uiState: StateFlow<MausamUiState> = _uiState.asStateFlow()

  init {
    loadSavedUserProfile()
    observeFieldsAndAnalyses()
    initChatWelcome()
  }

  private fun loadSavedUserProfile() {
    viewModelScope.launch {
      val saved = db.userDao().getUserProfile()
      if (saved != null) {
        _uiState.update { it.copy(userProfile = saved) }
      }
    }
  }

  private fun observeFieldsAndAnalyses() {
    viewModelScope.launch {
      cropRepo.getAllFields().collect { fields ->
        _uiState.update { it.copy(myFields = fields) }
      }
    }
    viewModelScope.launch {
      cropRepo.getAllAnalyses().collect { analyses ->
        _uiState.update { it.copy(myAnalyses = analyses) }
      }
    }
  }

  private fun initChatWelcome() {
    val welcomeText = when (_uiState.value.userProfile.language) {
      "ta" -> "வணக்கம்! நான் மௌசம் வானிலை நண்பன். மழை, விவசாய பயிர் பாதுகாப்பு, கடற்கரை அலைகள் அல்லது பயண வழிகள் குறித்து என்னிடம் கேளுங்கள்!"
      "tanglish" -> "Vanakkam! Naan unga MAUSAM Weather Friend. Mazhai varuma, running pogalama, vayal risk, beach trip pathi edha vena kelunga!"
      else -> "Hello! I'm your MAUSAM weather friend. Ask me about rain forecasts, running conditions, crop risks, travel routes, or beach safety!"
    }
    _uiState.update {
      it.copy(
        chatMessages = listOf(
          ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = welcomeText)
        )
      )
    }
  }

  val navigator = NavigationStateManager(initialScreen = Screen.Splash)

  fun navigateTo(screen: Screen) {
    navigator.navigateTo(screen)
    _uiState.update {
      it.copy(
        currentScreen = screen,
        selectedTab = navigator.currentTab ?: it.selectedTab
      )
    }
  }

  fun setHomeTab(tab: MainNavTab) {
    navigator.navigateToTab(tab)
    _uiState.update {
      it.copy(
        currentScreen = tab.screen,
        selectedTab = tab
      )
    }
  }

  fun navigateBack(): Boolean {
    val handled = navigator.navigateBack()
    if (handled) {
      _uiState.update {
        it.copy(
          currentScreen = navigator.currentScreen,
          selectedTab = navigator.currentTab ?: it.selectedTab
        )
      }
    }
    return handled
  }

  fun popToRoot() {
    navigator.popToRoot()
    _uiState.update {
      it.copy(
        currentScreen = Screen.Home,
        selectedTab = MainNavTab.HOME
      )
    }
  }

  fun loginAsGuest() {
    viewModelScope.launch {
      val profile = _uiState.value.userProfile.copy(
        name = "Guest User",
        email = "guest@mausam.app",
        isGuest = true
      )
      db.userDao().saveUserProfile(profile)
      _uiState.update { it.copy(userProfile = profile) }
      navigateTo(Screen.LocationPermission)
    }
  }

  fun loginWithGoogle() {
    viewModelScope.launch {
      val profile = _uiState.value.userProfile.copy(
        name = "Priya Kumar",
        email = "priyakumar130207@gmail.com",
        isGuest = false
      )
      db.userDao().saveUserProfile(profile)
      _uiState.update { it.copy(userProfile = profile) }
      navigateTo(Screen.LocationPermission)
    }
  }

  fun activateDemoMode() {
    viewModelScope.launch {
      val demoLoc = locationHelper.demoLocation
      val profile = _uiState.value.userProfile.copy(isDemoMode = true, name = "Demo Explorer")
      db.userDao().saveUserProfile(profile)
      _uiState.update {
        it.copy(
          userProfile = profile,
          currentLocation = demoLoc
        )
      }
      navigateTo(Screen.Home)
      refreshWeather(demoLoc)
      loadNearbyBeaches(demoLoc)
      calculateTravel(demoLoc, LocationInfo("Chennai", "Chennai", "Tamil Nadu", "India", 13.0827, 80.2707))
    }
  }

  fun requestLocationDetection() {
    viewModelScope.launch {
      _uiState.update { it.copy(isWeatherLoading = true) }
      val gpsLocation = locationHelper.getCurrentGpsLocation()
      val location = if (gpsLocation != null) {
        locationHelper.reverseGeocode(gpsLocation.latitude, gpsLocation.longitude)
      } else {
        locationHelper.demoLocation
      }
      _uiState.update {
        it.copy(
          currentLocation = location
        )
      }
      navigateTo(Screen.LifestyleSelection)
      refreshWeather(location)
      loadNearbyBeaches(location)
    }
  }

  fun toggleLifestyle(lifestyleId: String) {
    val current = _uiState.value.userProfile.lifestyles.toMutableSet()
    if (current.contains(lifestyleId)) {
      if (current.size > 1) current.remove(lifestyleId)
    } else {
      current.add(lifestyleId)
    }
    val updated = _uiState.value.userProfile.copy(lifestyles = current.toList())
    _uiState.update { it.copy(userProfile = updated) }
    viewModelScope.launch {
      db.userDao().saveUserProfile(updated)
    }
  }

  fun finishLifestyleSetup() {
    navigateTo(Screen.Home)
  }

  fun refreshWeather(location: LocationInfo = _uiState.value.currentLocation) {
    viewModelScope.launch {
      _uiState.update { it.copy(isWeatherLoading = true, weatherError = null) }
      try {
        val weather = weatherRepo.getWeatherData(location, forceRefresh = true)
        val airQuality = weatherRepo.getAirQuality(location.latitude, location.longitude)
        _uiState.update {
          it.copy(
            weatherData = weather,
            airQualityData = airQuality,
            isWeatherLoading = false
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isWeatherLoading = false,
            weatherError = "Could not update weather: ${e.localizedMessage}"
          )
        }
      }
    }
  }

  fun selectDayIndex(index: Int) {
    _uiState.update { it.copy(selectedDayIndex = index) }
  }

  // --- Location Search ---
  fun openSearch(open: Boolean) {
    _uiState.update { it.copy(isSearchOpen = open, searchResults = emptyList()) }
  }

  fun searchPlaces(query: String) {
    if (query.length < 2) return
    viewModelScope.launch {
      _uiState.update { it.copy(isSearching = true) }
      try {
        val resp = ApiClient.geocodingApi.searchLocation(query)
        val list = resp.results?.map { r ->
          LocationInfo(
            locality = r.name ?: "Unknown Place",
            district = r.district ?: "",
            state = r.state ?: "",
            country = r.country ?: "India",
            latitude = r.latitude ?: 0.0,
            longitude = r.longitude ?: 0.0,
            isDetectedGps = false
          )
        } ?: emptyList()
        _uiState.update { it.copy(searchResults = list, isSearching = false) }
      } catch (e: Exception) {
        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
      }
    }
  }

  fun selectLocation(loc: LocationInfo) {
    _uiState.update {
      it.copy(
        currentLocation = loc,
        isSearchOpen = false
      )
    }
    viewModelScope.launch {
      db.savedLocationDao().insertLocation(
        SavedLocationEntity(
          locality = loc.locality,
          district = loc.district,
          state = loc.state,
          country = loc.country,
          latitude = loc.latitude,
          longitude = loc.longitude
        )
      )
    }
    refreshWeather(loc)
    loadNearbyBeaches(loc)
  }

  // --- Crop Risk System ---
  fun handleCropImageSelected(uri: Uri, manualLocation: LocationInfo? = null) {
    viewModelScope.launch {
      _uiState.update { it.copy(isCropAnalyzing = true, cropImageUri = uri, cropError = null) }

      // 1. Prioritize Embedded EXIF GPS!
      val exifGps = locationHelper.extractExifGps(uri)
      val fieldLoc = if (exifGps != null) {
        locationHelper.reverseGeocode(exifGps.first, exifGps.second)
      } else if (manualLocation != null) {
        manualLocation
      } else {
        // Use demo field location or prompt
        LocationInfo(
          locality = "Melmaruvathur Field",
          district = "Chengalpattu District",
          state = "Tamil Nadu",
          country = "India",
          latitude = 12.4334,
          longitude = 79.8297
        )
      }

      _uiState.update { it.copy(cropFieldLocation = fieldLoc) }

      // 2. Perform Visual Check & Combine with Field Weather
      try {
        val assessment = cropRepo.assessCropRisk(uri, fieldLoc, "Paddy")
        _uiState.update {
          it.copy(
            cropAssessment = assessment,
            isCropAnalyzing = false
          )
        }

        // Save analysis to Room history
        cropRepo.saveAnalysis(
          CropAnalysisEntity(
            fieldId = 1,
            imagePath = uri.toString(),
            latitude = fieldLoc.latitude,
            longitude = fieldLoc.longitude,
            locationName = fieldLoc.displayName,
            riskLevel = assessment.riskLevel.label,
            observedIssues = assessment.imageObservation,
            weatherSummary = assessment.weatherFactorSummary,
            recommendations = assessment.suggestions.joinToString(" • ")
          )
        )
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isCropAnalyzing = false,
            cropError = "Error analyzing crop image: ${e.localizedMessage}"
          )
        }
      }
    }
  }

  fun setCropFieldLocation(loc: LocationInfo) {
    _uiState.update { it.copy(cropFieldLocation = loc) }
    _uiState.value.cropImageUri?.let { uri ->
      handleCropImageSelected(uri, loc)
    }
  }

  fun saveNewField(name: String, crop: String, location: LocationInfo) {
    viewModelScope.launch {
      cropRepo.saveField(
        CropFieldEntity(
          fieldName = name,
          cropType = crop,
          locationName = location.displayName,
          district = location.district,
          state = location.state,
          latitude = location.latitude,
          longitude = location.longitude,
          plantingDate = "October 2026",
          notes = "Saved field for active weather monitoring"
        )
      )
    }
  }

  // --- Travel System ---
  fun calculateTravel(
    source: LocationInfo = _uiState.value.travelSource,
    destination: LocationInfo = _uiState.value.travelDestination
  ) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          isTravelLoading = true,
          travelSource = source,
          travelDestination = destination
        )
      }
      try {
        val comparison = travelRepo.calculateRoutes(source, destination)
        _uiState.update {
          it.copy(
            travelComparison = comparison,
            isTravelLoading = false
          )
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isTravelLoading = false) }
      }
    }
  }

  // --- Beach Finder ---
  fun loadNearbyBeaches(location: LocationInfo = _uiState.value.currentLocation) {
    viewModelScope.launch {
      _uiState.update { it.copy(isBeachLoading = true) }
      try {
        val beaches = beachRepo.getNearbyBeaches(location)
        _uiState.update {
          it.copy(
            nearbyBeaches = beaches,
            isBeachLoading = false
          )
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isBeachLoading = false) }
      }
    }
  }

  // --- Chat Assistant ---
  fun sendChatMessage(query: String) {
    if (query.isBlank()) return
    val userMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = query)
    _uiState.update {
      it.copy(
        chatMessages = it.chatMessages + userMsg,
        isChatReplying = true
      )
    }

    viewModelScope.launch {
      val state = _uiState.value
      val reply = assistantEngine.answerQuestion(
        query = query,
        currentWeather = state.weatherData,
        cropAssessment = state.cropAssessment,
        travelComparison = state.travelComparison,
        beachDetails = state.nearbyBeaches,
        lang = state.userProfile.language
      )
      val botMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = reply)
      _uiState.update {
        it.copy(
          chatMessages = it.chatMessages + botMsg,
          isChatReplying = false
        )
      }
    }
  }

  // --- Settings ---
  fun updateTemperatureUnit(unit: String) {
    val updated = _uiState.value.userProfile.copy(tempUnit = unit)
    _uiState.update { it.copy(userProfile = updated) }
    viewModelScope.launch { db.userDao().saveUserProfile(updated) }
  }

  fun updateLanguage(lang: String) {
    val updated = _uiState.value.userProfile.copy(language = lang)
    _uiState.update { it.copy(userProfile = updated) }
    viewModelScope.launch { db.userDao().saveUserProfile(updated) }
  }

  fun clearCache() {
    viewModelScope.launch {
      db.weatherCacheDao().purgeOldCache(System.currentTimeMillis() + 100000)
      refreshWeather()
    }
  }
}
