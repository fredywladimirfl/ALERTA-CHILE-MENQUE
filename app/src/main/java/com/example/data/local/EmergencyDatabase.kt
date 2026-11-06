package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AlertDao
import com.example.data.local.dao.EarthquakeDao
import com.example.data.local.entity.CachedAlertEntity
import com.example.data.local.entity.CachedEarthquakeEntity

@Database(
  entities = [CachedAlertEntity::class, CachedEarthquakeEntity::class],
  version = 1,
  exportSchema = false
)
abstract class EmergencyDatabase : RoomDatabase() {

  abstract fun alertDao(): AlertDao
  abstract fun earthquakeDao(): EarthquakeDao

  companion object {
    @Volatile
    private var INSTANCE: EmergencyDatabase? = null

    fun getInstance(context: Context): EmergencyDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          EmergencyDatabase::class.java,
          "emergency_alerta_chile.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
