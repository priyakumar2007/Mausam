package com.example.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Sealed class representing all destinations within MAUSAM without relying on
 * external libraries such as Navigation Compose.
 */
sealed class Screen(val route: String, val title: String) {
  // Main Primary Navigation Screens
  object Home : Screen("home", "Home")
  object Lifestyle : Screen("lifestyle", "Lifestyle")
  object Forecast : Screen("forecast", "Forecast")
  object Alerts : Screen("alerts", "Alerts")
  object Profile : Screen("profile", "Profile")
  object MainHome : Screen("main_home", "Home")

  // Authentication & Onboarding
  object Splash : Screen("splash", "Splash")
  object Login : Screen("login", "Login")
  object LocationPermission : Screen("location_permission", "Location Permission")
  object LifestyleSelection : Screen("lifestyle_selection", "Select Lifestyles")

  // Dedicated Lifestyle & Intelligence Detail Screens
  object HealthDetail : Screen("health_detail", "Health & Wellness")
  object FitnessDetail : Screen("fitness_detail", "Fitness & Workouts")
  object CropRiskDetail : Screen("crop_risk_detail", "Crop Risk Analysis")
  object TravelDetail : Screen("travel_detail", "Travel Planner & Routes")
  object BeachDetail : Screen("beach_detail", "Beach Marine Safety")
  object FamilyDetail : Screen("family_detail", "Parents & Family Commute")
  object CommuterDetail : Screen("commuter_detail", "Daily Commuter Transit")
  object EventsDetail : Screen("events_detail", "Events & Outdoor Planner")
  object AssistantDetail : Screen("assistant_detail", "Ask MAUSAM Assistant")
  object SettingsDetail : Screen("settings_detail", "Settings")

  val isMainTabScreen: Boolean
    get() = this is Home || this is Lifestyle || this is Forecast || this is Alerts || this is Profile

  val showBottomBar: Boolean
    get() = isMainTabScreen
}

/**
 * Enum representing the main bottom-navigation tabs for rapid indexation and selection.
 */
enum class MainNavTab(val screen: Screen, val label: String) {
  HOME(Screen.Home, "Home"),
  LIFESTYLE(Screen.Lifestyle, "Lifestyle"),
  FORECAST(Screen.Forecast, "Forecast"),
  ALERTS(Screen.Alerts, "Alerts"),
  PROFILE(Screen.Profile, "Profile")
}

/**
 * State Manager to coordinate sealed-class & enum screen navigation and backstack management.
 * Provides complete push, pop, switch-tab, and replace mechanisms.
 */
class NavigationStateManager(initialScreen: Screen = Screen.Splash) {
  private val _backStack = MutableStateFlow<List<Screen>>(listOf(initialScreen))
  val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()

  val currentScreen: Screen
    get() = _backStack.value.lastOrNull() ?: Screen.Home

  private val _currentScreenState = MutableStateFlow(initialScreen)
  val currentScreenState: StateFlow<Screen> = _currentScreenState.asStateFlow()

  val currentTab: MainNavTab?
    get() = when (currentScreen) {
      is Screen.Home -> MainNavTab.HOME
      is Screen.Lifestyle -> MainNavTab.LIFESTYLE
      is Screen.Forecast -> MainNavTab.FORECAST
      is Screen.Alerts -> MainNavTab.ALERTS
      is Screen.Profile -> MainNavTab.PROFILE
      else -> null
    }

  val canGoBack: Boolean
    get() {
      val stack = _backStack.value
      return stack.size > 1 || (stack.size == 1 && stack.first() != Screen.Home && stack.first().isMainTabScreen)
    }

  fun navigateTo(screen: Screen) {
    if (screen == currentScreen) return

    _backStack.update { stack ->
      if (screen.isMainTabScreen) {
        // Tab switching: keep Home as base root if navigating to a secondary tab
        if (screen == Screen.Home) {
          listOf(Screen.Home)
        } else {
          listOf(Screen.Home, screen)
        }
      } else {
        // Push sub-screen onto stack
        stack + screen
      }
    }
    _currentScreenState.value = screen
  }

  fun navigateToTab(tab: MainNavTab) {
    navigateTo(tab.screen)
  }

  fun navigateBack(): Boolean {
    val stack = _backStack.value
    if (stack.size > 1) {
      val newStack = stack.dropLast(1)
      _backStack.value = newStack
      _currentScreenState.value = newStack.last()
      return true
    } else if (stack.size == 1 && stack.first() != Screen.Home && stack.first().isMainTabScreen) {
      // Pop secondary tab back to primary Home tab
      _backStack.value = listOf(Screen.Home)
      _currentScreenState.value = Screen.Home
      return true
    }
    return false
  }

  fun popToRoot() {
    _backStack.value = listOf(Screen.Home)
    _currentScreenState.value = Screen.Home
  }

  fun replace(screen: Screen) {
    _backStack.update { stack ->
      if (stack.isNotEmpty()) stack.dropLast(1) + screen else listOf(screen)
    }
    _currentScreenState.value = screen
  }
}

typealias AppScreen = Screen
typealias HomeTab = MainNavTab
