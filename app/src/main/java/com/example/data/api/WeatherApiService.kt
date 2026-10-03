package com.example.data.api

import com.example.data.model.GeocodingSearchResponse
import com.example.data.model.OpenMeteoAirQualityResponse
import com.example.data.model.OpenMeteoMarineResponse
import com.example.data.model.OpenMeteoWeatherResponse
import com.example.data.model.OsrmRouteResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {
  @GET("v1/forecast")
  suspend fun getForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m,uv_index",
    @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m,uv_index",
    @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,uv_index_max",
    @Query("timezone") timezone: String = "auto"
  ): OpenMeteoWeatherResponse
}

interface AirQualityApiService {
  @GET("v1/air-quality")
  suspend fun getAirQuality(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "european_aqi,us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone"
  ): OpenMeteoAirQualityResponse
}

interface MarineApiService {
  @GET("v1/marine")
  suspend fun getMarine(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "wave_height,wave_direction,wave_period,wind_wave_height,wind_wave_direction,wind_wave_period",
    @Query("daily") daily: String = "wave_height_max",
    @Query("timezone") timezone: String = "auto"
  ): OpenMeteoMarineResponse
}

interface GeocodingApiService {
  @GET("v1/search")
  suspend fun searchLocation(
    @Query("name") query: String,
    @Query("count") count: Int = 10,
    @Query("language") language: String = "en",
    @Query("format") format: String = "json"
  ): GeocodingSearchResponse
}

interface RoutingApiService {
  @GET("route/v1/driving/{coords}")
  suspend fun getRoute(
    @Path("coords", encoded = true) coordinates: String,
    @Query("overview") overview: String = "simplified",
    @Query("alternatives") alternatives: Boolean = true,
    @Query("steps") steps: Boolean = false
  ): OsrmRouteResponse
}

object ApiClient {
  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.NONE
    })
    .build()

  val weatherApi: WeatherApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(WeatherApiService::class.java)
  }

  val airQualityApi: AirQualityApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://air-quality-api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(AirQualityApiService::class.java)
  }

  val marineApi: MarineApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://marine-api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(MarineApiService::class.java)
  }

  val geocodingApi: GeocodingApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://geocoding-api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(GeocodingApiService::class.java)
  }

  val routingApi: RoutingApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://router.project-osrm.org/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(RoutingApiService::class.java)
  }
}
