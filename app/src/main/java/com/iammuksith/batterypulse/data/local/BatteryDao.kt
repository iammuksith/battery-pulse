package com.iammuksith.batterypulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryLog(log: BatteryLogEntity): Long

    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBatteryLogs(limit: Int = 100): Flow<List<BatteryLogEntity>>

    @Query("SELECT * FROM battery_logs ORDER BY timestamp ASC")
    fun getAllBatteryLogsAsc(): Flow<List<BatteryLogEntity>>

    @Query("DELETE FROM battery_logs WHERE timestamp < :cutoff")
    suspend fun deleteOldLogs(cutoff: Long)

    @Query("DELETE FROM battery_logs")
    suspend fun clearAllBatteryLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargingSession(session: ChargingSessionEntity): Long

    @Update
    suspend fun updateChargingSession(session: ChargingSessionEntity)

    @Query("SELECT * FROM charging_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveChargingSession(): ChargingSessionEntity?

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllChargingSessions(): Flow<List<ChargingSessionEntity>>

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAllSessions()
}
