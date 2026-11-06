package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedEarthquakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EarthquakeDao {

  @Query("SELECT * FROM cached_earthquakes ORDER BY timeMillis DESC")
  fun observeAllEarthquakes(): Flow<List<CachedEarthquakeEntity>>

  @Query("SELECT * FROM cached_earthquakes ORDER BY timeMillis DESC")
  suspend fun getAllEarthquakes(): List<CachedEarthquakeEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEarthquakes(earthquakes: List<CachedEarthquakeEntity>)

  @Query("DELETE FROM cached_earthquakes")
  suspend fun clearAllEarthquakes()

  @Query("SELECT COUNT(*) FROM cached_earthquakes")
  suspend fun getCount(): Int
}
