package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

  @Query("SELECT * FROM cached_alerts ORDER BY timestampMillis DESC")
  fun observeAllAlerts(): Flow<List<CachedAlertEntity>>

  @Query("SELECT * FROM cached_alerts ORDER BY timestampMillis DESC")
  suspend fun getAllAlerts(): List<CachedAlertEntity>

  @Query("SELECT * FROM cached_alerts WHERE region = :region ORDER BY timestampMillis DESC")
  fun observeAlertsByRegion(region: String): Flow<List<CachedAlertEntity>>

  @Query("SELECT * FROM cached_alerts WHERE id = :alertId LIMIT 1")
  suspend fun getAlertById(alertId: String): CachedAlertEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlerts(alerts: List<CachedAlertEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: CachedAlertEntity)

  @Query("DELETE FROM cached_alerts WHERE id = :alertId")
  suspend fun deleteAlertById(alertId: String)

  @Query("DELETE FROM cached_alerts")
  suspend fun clearAllAlerts()

  @Query("SELECT COUNT(*) FROM cached_alerts")
  suspend fun getAlertCount(): Int

  @Query("SELECT MAX(cachedAtMillis) FROM cached_alerts")
  suspend fun getLatestCacheTimestamp(): Long?
}
