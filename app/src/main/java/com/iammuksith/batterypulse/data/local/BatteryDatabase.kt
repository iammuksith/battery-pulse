package com.iammuksith.batterypulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BatteryLogEntity::class, ChargingSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao

    companion object {
        @Volatile
        private var INSTANCE: BatteryDatabase? = null

        fun getDatabase(context: Context): BatteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BatteryDatabase::class.java,
                    "battery_health.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
