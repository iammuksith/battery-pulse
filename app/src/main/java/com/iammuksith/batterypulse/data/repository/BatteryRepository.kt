package com.iammuksith.batterypulse.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.iammuksith.batterypulse.data.local.BatteryDao
import com.iammuksith.batterypulse.data.local.BatteryLogEntity
import com.iammuksith.batterypulse.data.local.ChargingSessionEntity
import com.iammuksith.batterypulse.data.model.BatteryHealthCondition
import com.iammuksith.batterypulse.data.model.BatteryInfo
import com.iammuksith.batterypulse.data.model.BatteryStatus
import com.iammuksith.batterypulse.data.model.PlugType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class BatteryRepository(
    private val context: Context,
    private val batteryDao: BatteryDao,
    private val scope: CoroutineScope
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private var lastLoggedLevel: Int = -1
    private var lastLoggedCharging: Boolean? = null
    private var lastLoggedTime: Long = 0L

    val recentLogs: Flow<List<BatteryLogEntity>> = batteryDao.getRecentBatteryLogs(120)
    val chargingSessions: Flow<List<ChargingSessionEntity>> = batteryDao.getAllChargingSessions()

    val batteryInfoFlow: Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val info = parseBatteryIntent(intent)
                    trySend(info)
                    handleBatteryPersistence(info)
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // Sticky broadcast provides initial state immediately
        val initialIntent = context.registerReceiver(receiver, filter)
        if (initialIntent != null) {
            val initialInfo = parseBatteryIntent(initialIntent)
            trySend(initialInfo)
            handleBatteryPersistence(initialInfo)
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun parseBatteryIntent(intent: Intent): BatteryInfo {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val level = if (scale > 0) ((rawLevel.toFloat() / scale.toFloat()) * 100).toInt() else rawLevel

        val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val status = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            else -> BatteryStatus.UNKNOWN
        }

        val pluggedInt = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val plugType = when (pluggedInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> PlugType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PlugType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.WIRELESS
            4 -> PlugType.DOCK // BATTERY_PLUGGED_DOCK
            else -> PlugType.UNPLUGGED
        }

        val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealthCondition.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealthCondition.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealthCondition.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealthCondition.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealthCondition.FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealthCondition.COLD
            else -> BatteryHealthCondition.UNKNOWN
        }

        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = rawTemp / 10.0f

        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

        var currentNow: Int? = null
        var chargeRemaining: Long? = null
        batteryManager?.let { bm ->
            val currentUa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (currentUa != Int.MIN_VALUE && currentUa != 0) {
                currentNow = currentUa / 1000 // mA
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val remainingMs = bm.computeChargeTimeRemaining()
                if (remainingMs > 0) {
                    chargeRemaining = remainingMs
                }
            }
        }

        return BatteryInfo(
            level = level,
            status = status,
            plugType = plugType,
            health = health,
            temperatureCelsius = temperature,
            voltageMv = voltageMv,
            technology = technology,
            isPresent = present,
            currentNowMa = currentNow,
            chargeTimeRemainingMs = chargeRemaining,
            timestamp = System.currentTimeMillis(),
            isValid = true
        )
    }

    private fun handleBatteryPersistence(info: BatteryInfo) {
        val now = System.currentTimeMillis()
        val isCharging = info.isCharging

        // Extremely lightweight footprint: only write to DB when level changes or state flips,
        // or every 10 minutes minimum
        val levelChanged = info.level != lastLoggedLevel
        val stateChanged = lastLoggedCharging != null && isCharging != lastLoggedCharging
        val timeElapsed = (now - lastLoggedTime) > (10 * 60 * 1000)

        if (levelChanged || stateChanged || timeElapsed || lastLoggedLevel == -1) {
            lastLoggedLevel = info.level
            lastLoggedCharging = isCharging
            lastLoggedTime = now

            scope.launch(Dispatchers.IO) {
                batteryDao.insertBatteryLog(
                    BatteryLogEntity(
                        timestamp = now,
                        level = info.level,
                        status = info.status.name,
                        plugType = info.plugType.name,
                        temperature = info.temperatureCelsius,
                        voltage = info.voltageMv,
                        isCharging = isCharging
                    )
                )

                // Manage charging session
                if (isCharging) {
                    val activeSession = batteryDao.getActiveChargingSession()
                    if (activeSession == null) {
                        batteryDao.insertChargingSession(
                            ChargingSessionEntity(
                                startTime = now,
                                endTime = null,
                                startLevel = info.level,
                                endLevel = info.level,
                                plugType = info.plugType.name,
                                avgTemp = info.temperatureCelsius
                            )
                        )
                    } else {
                        // Update active session end level
                        batteryDao.updateChargingSession(
                            activeSession.copy(
                                endLevel = info.level,
                                avgTemp = ((activeSession.avgTemp + info.temperatureCelsius) / 2f)
                            )
                        )
                    }
                } else {
                    // Closed any active session
                    val activeSession = batteryDao.getActiveChargingSession()
                    if (activeSession != null) {
                        batteryDao.updateChargingSession(
                            activeSession.copy(
                                endTime = now,
                                endLevel = info.level
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun clearHistory() {
        batteryDao.clearAllBatteryLogs()
    }

    suspend fun clearSessions() {
        batteryDao.clearAllSessions()
    }
}
