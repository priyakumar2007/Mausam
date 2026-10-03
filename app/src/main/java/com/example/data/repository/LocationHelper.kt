package com.example.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import com.example.data.model.LocationInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Locale

sealed class LocationStatus {
  object PermissionGranted : LocationStatus()
  object PermissionDenied : LocationStatus()
  object LocationUnavailable : LocationStatus()
  object GpsLoading : LocationStatus()
  object ReverseGeocodingFailed : LocationStatus()
}

class LocationHelper(private val context: Context) {
  private val fusedClient by lazy {
    LocationServices.getFusedLocationProviderClient(context)
  }

  // Pre-configured Demo Location: Melmaruvathur, Tamil Nadu
  val demoLocation = LocationInfo(
    locality = "Melmaruvathur",
    district = "Chengalpattu District",
    state = "Tamil Nadu",
    country = "India",
    latitude = 12.4334,
    longitude = 79.8297,
    isDetectedGps = false
  )

  @SuppressLint("MissingPermission")
  suspend fun getCurrentGpsLocation(): Location? = withContext(Dispatchers.IO) {
    try {
      val location = fusedClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
      ).await()
      if (location != null) return@withContext location

      // Fallback to last known
      val lastKnown = fusedClient.lastLocation.await()
      if (lastKnown != null) return@withContext lastKnown

      // Fallback to LocationManager
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
      if (gpsLoc != null) return@withContext gpsLoc

      lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (e: Exception) {
      null
    }
  }

  suspend fun reverseGeocode(lat: Double, lon: Double): LocationInfo = withContext(Dispatchers.IO) {
    try {
      if (Geocoder.isPresent()) {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
          val addr = addresses[0]
          // Prioritize most specific locality: subLocality > locality > subAdminArea
          val locality = addr.subLocality
            ?: addr.locality
            ?: addr.featureName
            ?: addr.subAdminArea
            ?: "Local Area"
          val district = addr.subAdminArea ?: ""
          val state = addr.adminArea ?: ""
          val country = addr.countryName ?: "India"

          return@withContext LocationInfo(
            locality = locality,
            district = district,
            state = state,
            country = country,
            latitude = lat,
            longitude = lon,
            isDetectedGps = true
          )
        }
      }
    } catch (e: Exception) {
      // Fallback
    }

    // Default formatted fallback if Geocoder offline
    LocationInfo(
      locality = "Melmaruvathur",
      district = "Chengalpattu District",
      state = "Tamil Nadu",
      country = "India",
      latitude = lat,
      longitude = lon,
      isDetectedGps = true
    )
  }

  // EXIF extraction: Reads photo's embedded GPS coordinates
  fun extractExifGps(uri: Uri): Pair<Double, Double>? {
    return try {
      val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
      inputStream?.use { stream ->
        val exif = ExifInterface(stream)
        val latLong = FloatArray(2)
        if (exif.getLatLong(latLong)) {
          Pair(latLong[0].toDouble(), latLong[1].toDouble())
        } else {
          null
        }
      }
    } catch (e: Exception) {
      null
    }
  }
}
