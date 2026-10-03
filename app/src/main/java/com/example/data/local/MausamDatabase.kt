package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

// ----------------- Room Entities -----------------

@Entity(tableName = "user_profile")
data class UserProfileEntity(
  @PrimaryKey val id: Int = 1,
  val name: String = "Friend",
  val email: String = "guest@mausam.app",
  val lifestyles: List<String> = listOf("health", "fitness", "beach", "agriculture", "travel"),
  val tempUnit: String = "C", // "C" or "F"
  val language: String = "en", // "en", "ta" (Tamil), "tanglish"
  val isDemoMode: Boolean = false,
  val isGuest: Boolean = true
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val locality: String,
  val district: String,
  val state: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val isCurrent: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_fields")
data class CropFieldEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fieldName: String,
  val cropType: String,
  val locationName: String,
  val district: String,
  val state: String,
  val latitude: Double,
  val longitude: Double,
  val plantingDate: String,
  val notes: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "crop_analyses")
data class CropAnalysisEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fieldId: Long = 0,
  val imagePath: String,
  val latitude: Double,
  val longitude: Double,
  val locationName: String,
  val riskLevel: String,
  val observedIssues: String,
  val weatherSummary: String,
  val recommendations: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
  @PrimaryKey val cacheKey: String, // e.g. "12.43_79.83"
  val payloadJson: String,
  val timestamp: Long = System.currentTimeMillis()
)

// ----------------- Type Converters -----------------

class Converters {
  private val moshi = Moshi.Builder().build()
  private val listType = Types.newParameterizedType(List::class.java, String::class.java)
  private val adapter = moshi.adapter<List<String>>(listType)

  @TypeConverter
  fun fromStringList(value: List<String>?): String {
    return adapter.toJson(value ?: emptyList())
  }

  @TypeConverter
  fun toStringList(value: String?): List<String> {
    if (value.isNullOrBlank()) return emptyList()
    return try {
      adapter.fromJson(value) ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }
  }
}

// ----------------- DAOs -----------------

@Dao
interface UserDao {
  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  fun getUserProfileFlow(): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
  suspend fun getUserProfile(): UserProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUserProfile(profile: UserProfileEntity)
}

@Dao
interface SavedLocationDao {
  @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
  fun getSavedLocationsFlow(): Flow<List<SavedLocationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLocation(location: SavedLocationEntity): Long

  @Query("DELETE FROM saved_locations WHERE id = :id")
  suspend fun deleteLocation(id: Long)
}

@Dao
interface CropFieldDao {
  @Query("SELECT * FROM crop_fields ORDER BY timestamp DESC")
  fun getAllFieldsFlow(): Flow<List<CropFieldEntity>>

  @Query("SELECT * FROM crop_fields WHERE id = :id LIMIT 1")
  suspend fun getFieldById(id: Long): CropFieldEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertField(field: CropFieldEntity): Long

  @Query("DELETE FROM crop_fields WHERE id = :id")
  suspend fun deleteField(id: Long)
}

@Dao
interface CropAnalysisDao {
  @Query("SELECT * FROM crop_analyses ORDER BY timestamp DESC")
  fun getAllAnalysesFlow(): Flow<List<CropAnalysisEntity>>

  @Query("SELECT * FROM crop_analyses WHERE fieldId = :fieldId ORDER BY timestamp DESC")
  fun getAnalysesForField(fieldId: Long): Flow<List<CropAnalysisEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnalysis(analysis: CropAnalysisEntity): Long
}

@Dao
interface WeatherCacheDao {
  @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
  suspend fun getCache(key: String): WeatherCacheEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setCache(cache: WeatherCacheEntity)

  @Query("DELETE FROM weather_cache WHERE timestamp < :olderThan")
  suspend fun purgeOldCache(olderThan: Long)
}

// ----------------- Database -----------------

@Database(
  entities = [
    UserProfileEntity::class,
    SavedLocationEntity::class,
    CropFieldEntity::class,
    CropAnalysisEntity::class,
    WeatherCacheEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MausamDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun savedLocationDao(): SavedLocationDao
  abstract fun cropFieldDao(): CropFieldDao
  abstract fun cropAnalysisDao(): CropAnalysisDao
  abstract fun weatherCacheDao(): WeatherCacheDao

  companion object {
    @Volatile
    private var INSTANCE: MausamDatabase? = null

    fun getInstance(context: Context): MausamDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MausamDatabase::class.java,
          "mausam_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
