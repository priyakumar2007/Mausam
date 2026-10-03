package com.example

import com.example.domain.LanguageManager
import com.example.ui.navigation.MainNavTab
import com.example.ui.navigation.NavigationStateManager
import com.example.ui.navigation.Screen
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLanguageTranslations() {
    val enInsight = LanguageManager.getString("mausam_insight", "en")
    val taInsight = LanguageManager.getString("mausam_insight", "ta")
    val tanInsight = LanguageManager.getString("mausam_insight", "tanglish")

    assertEquals("MAUSAM Insight", enInsight)
    assertEquals("வானிலை பார்வை", taInsight)
    assertEquals("MAUSAM Insight", tanInsight)
  }

  @Test
  fun testNavigationStateManager() {
    val nav = NavigationStateManager(initialScreen = Screen.Splash)
    assertEquals(Screen.Splash, nav.currentScreen)
    assertFalse(nav.canGoBack)

    // Navigate to Login
    nav.navigateTo(Screen.Login)
    assertEquals(Screen.Login, nav.currentScreen)
    assertTrue(nav.canGoBack)

    // Navigate to Main tabs
    nav.navigateTo(Screen.Home)
    assertEquals(Screen.Home, nav.currentScreen)
    assertEquals(MainNavTab.HOME, nav.currentTab)
    assertFalse(nav.canGoBack) // At root Home tab

    // Switch to Forecast tab
    nav.navigateTo(Screen.Forecast)
    assertEquals(Screen.Forecast, nav.currentScreen)
    assertEquals(MainNavTab.FORECAST, nav.currentTab)
    assertTrue(nav.canGoBack)

    // Navigate back from Forecast returns to Home
    val handledBack = nav.navigateBack()
    assertTrue(handledBack)
    assertEquals(Screen.Home, nav.currentScreen)

    // Navigate to sub-screen like CropRiskDetail
    nav.navigateTo(Screen.CropRiskDetail)
    assertEquals(Screen.CropRiskDetail, nav.currentScreen)
    assertNull(nav.currentTab)
    assertTrue(nav.canGoBack)

    // Navigate back returns to Home
    val popped = nav.navigateBack()
    assertTrue(popped)
    assertEquals(Screen.Home, nav.currentScreen)
  }
}
