package com.iammuksith.batterypulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iammuksith.batterypulse.data.local.BatteryDatabase
import com.iammuksith.batterypulse.data.local.BatteryLogEntity
import com.iammuksith.batterypulse.data.local.ChargingSessionEntity
import com.iammuksith.batterypulse.data.model.BatteryInfo
import com.iammuksith.batterypulse.data.repository.BatteryRepository
import com.iammuksith.batterypulse.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DischargeAnalysis(
    val drainRatePerHour: Float? = null,
    val estimatedMinutesRemaining: Long? = null,
    val isCharging: Boolean = false,
    val minRecordedTemp: Float? = null,
    val maxRecordedTemp: Float? = null,
    val avgRecordedVoltage: Int? = null
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BatteryDatabase.getDatabase(application)
    private val repository = BatteryRepository(application, database.batteryDao(), viewModelScope)
    private val notificationHelper = NotificationHelper(application)

    val batteryInfo: StateFlow<BatteryInfo> = repository.batteryInfoFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatteryInfo()
        )

    val recentLogs: StateFlow<List<BatteryLogEntity>> = repository.recentLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chargingSessions: StateFlow<List<ChargingSessionEntity>> = repository.chargingSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Optimizer & Alert Settings
    private val _isLowPowerModeEnabled = MutableStateFlow(false)
    val isLowPowerModeEnabled: StateFlow<Boolean> = _isLowPowerModeEnabled.asStateFlow()

    private val _is80PercentAlertEnabled = MutableStateFlow(true)
    val is80PercentAlertEnabled: StateFlow<Boolean> = _is80PercentAlertEnabled.asStateFlow()

    private val _isTempAlertEnabled = MutableStateFlow(true)
    val isTempAlertEnabled: StateFlow<Boolean> = _isTempAlertEnabled.asStateFlow()

    private val _safeTempThreshold = MutableStateFlow(40.0f)
    val safeTempThreshold: StateFlow<Float> = _safeTempThreshold.asStateFlow()

    private val _isLowBatteryAlertEnabled = MutableStateFlow(true)
    val isLowBatteryAlertEnabled: StateFlow<Boolean> = _isLowBatteryAlertEnabled.asStateFlow()

    private val _lowBatteryThreshold = MutableStateFlow(20)
    val lowBatteryThreshold: StateFlow<Int> = _lowBatteryThreshold.asStateFlow()

    private var hasAlerted80Percent = false
    private var hasAlertedTemp = false
    private var hasAlertedLow = false

    init {
        // Monitor battery changes for alerts
        viewModelScope.launch {
            batteryInfo.collect { info ->
                checkSmartAlerts(info)
            }
        }
    }

    private fun checkSmartAlerts(info: BatteryInfo) {
        if (!info.isValid) return
        // 80% charge protection alert
        if (_is80PercentAlertEnabled.value && info.isCharging) {
            if (info.level >= 80 && !hasAlerted80Percent) {
                notificationHelper.showChargeLimitAlert(info.level)
                hasAlerted80Percent = true
            } else if (info.level < 75) {
                hasAlerted80Percent = false
            }
        } else if (!info.isCharging) {
            hasAlerted80Percent = false
        }

        // Temperature alert (exceeding safe threshold)
        if (_isTempAlertEnabled.value) {
            val limit = _safeTempThreshold.value
            if (info.temperatureCelsius >= limit && !hasAlertedTemp) {
                notificationHelper.showHighTemperatureAlert(info.temperatureCelsius, limit)
                hasAlertedTemp = true
            } else if (info.temperatureCelsius < limit - 2.0f) {
                hasAlertedTemp = false
            }
        }

        // Low battery alert (< 20% or threshold)
        if (_isLowBatteryAlertEnabled.value && !info.isCharging) {
            val threshold = _lowBatteryThreshold.value
            if (info.level <= threshold && !hasAlertedLow) {
                notificationHelper.showLowBatteryAlert(info.level, threshold)
                hasAlertedLow = true
            } else if (info.level > threshold + 5) {
                hasAlertedLow = false
            }
        } else if (info.isCharging) {
            hasAlertedLow = false
        }
    }

    fun calculateDischargeAnalysis(
        currentInfo: BatteryInfo,
        logs: List<BatteryLogEntity>
    ): DischargeAnalysis {
        if (logs.isEmpty()) {
            return DischargeAnalysis(
                isCharging = currentInfo.isCharging,
                estimatedMinutesRemaining = if (currentInfo.isCharging) {
                    currentInfo.chargeTimeRemainingMs?.let { it / (1000 * 60) }
                } else {
                    // Nominal estimate (approx 6 hours on 100%)
                    (currentInfo.level * 4.5f).toLong()
                }
            )
        }

        val minTemp = logs.minOfOrNull { it.temperature }
        val maxTemp = logs.maxOfOrNull { it.temperature }
        val avgVolt = logs.map { it.voltage }.average().toInt()

        if (currentInfo.isCharging) {
            val remainingMins = currentInfo.chargeTimeRemainingMs?.let { it / (1000 * 60) }
                ?: run {
                    val neededPct = (100 - currentInfo.level).coerceAtLeast(0)
                    (neededPct * 1.5f).toLong() // Nominal ~1.5 min per 1%
                }

            return DischargeAnalysis(
                isCharging = true,
                drainRatePerHour = null,
                estimatedMinutesRemaining = remainingMins,
                minRecordedTemp = minTemp,
                maxRecordedTemp = maxTemp,
                avgRecordedVoltage = avgVolt
            )
        } else {
            // Calculate drain rate from recent discharging entries
            val dischargingLogs = logs.filter { !it.isCharging }.take(30)
            var calculatedRate: Float? = null
            var estimatedMins: Long? = null

            if (dischargingLogs.size >= 2) {
                val oldest = dischargingLogs.last()
                val newest = dischargingLogs.first()
                val timeDiffHours = (newest.timestamp - oldest.timestamp).toFloat() / (1000f * 60f * 60f)
                val levelDiff = (oldest.level - newest.level).toFloat()

                if (timeDiffHours > 0.05f && levelDiff >= 1) {
                    calculatedRate = (levelDiff / timeDiffHours).coerceIn(1f, 50f)
                }
            }

            val effectiveRate = calculatedRate ?: 12.0f // Nominal standard ~12% per hour
            estimatedMins = ((currentInfo.level / effectiveRate) * 60f).toLong().coerceAtLeast(5)

            return DischargeAnalysis(
                drainRatePerHour = calculatedRate,
                estimatedMinutesRemaining = estimatedMins,
                isCharging = false,
                minRecordedTemp = minTemp,
                maxRecordedTemp = maxTemp,
                avgRecordedVoltage = avgVolt
            )
        }
    }

    fun toggleLowPowerMode(enabled: Boolean) {
        _isLowPowerModeEnabled.value = enabled
    }

    fun toggle80PercentAlert(enabled: Boolean) {
        _is80PercentAlertEnabled.value = enabled
    }

    fun toggleTempAlert(enabled: Boolean) {
        _isTempAlertEnabled.value = enabled
    }

    fun toggleLowBatteryAlert(enabled: Boolean) {
        _isLowBatteryAlertEnabled.value = enabled
    }

    fun setSafeTempThreshold(temp: Float) {
        _safeTempThreshold.value = temp
        hasAlertedTemp = false // allow re-trigger on new threshold
    }

    fun setLowBatteryThreshold(level: Int) {
        _lowBatteryThreshold.value = level
        hasAlertedLow = false
    }

    fun testLowBatteryAlert() {
        notificationHelper.showTestAlert(isTempTest = false)
    }

    fun testOverheatAlert() {
        notificationHelper.showTestAlert(isTempTest = true)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearSessions() {
        viewModelScope.launch {
            repository.clearSessions()
        }
    }
}
