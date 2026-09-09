package com.iammuksith.batterypulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_logs")
data class BatteryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val level: Int,
    val status: String,
    val plugType: String,
    val temperature: Float,
    val voltage: Int,
    val isCharging: Boolean
)

@Entity(tableName = "charging_sessions")
data class ChargingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val startLevel: Int,
    val endLevel: Int,
    val plugType: String,
    val avgTemp: Float
) {
    val durationMinutes: Long
        get() {
            val end = endTime ?: System.currentTimeMillis()
            return ((end - startTime) / (1000 * 60)).coerceAtLeast(1)
        }

    val percentageGain: Int
        get() = (endLevel - startLevel).coerceAtLeast(0)

    val chargingSpeedPerHour: Float
        get() {
            val mins = durationMinutes.toFloat()
            return if (mins > 0) (percentageGain / mins) * 60f else 0f
        }
}
