package com.iammuksith.batterypulse.data.model

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    NOT_CHARGING,
    FULL,
    UNKNOWN
}

enum class PlugType {
    AC,
    USB,
    WIRELESS,
    DOCK,
    UNPLUGGED
}

enum class BatteryHealthCondition {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    FAILURE,
    COLD,
    UNKNOWN
}

data class BatteryInfo(
    val level: Int = 0,
    val status: BatteryStatus = BatteryStatus.UNKNOWN,
    val plugType: PlugType = PlugType.UNPLUGGED,
    val health: BatteryHealthCondition = BatteryHealthCondition.GOOD,
    val temperatureCelsius: Float = 0f,
    val voltageMv: Int = 0,
    val technology: String = "Li-ion",
    val isPresent: Boolean = true,
    val currentNowMa: Int? = null,
    val chargeCounterMah: Int? = null,
    val chargeTimeRemainingMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isValid: Boolean = false
) {
    val temperatureFahrenheit: Float
        get() = (temperatureCelsius * 9f / 5f) + 32f

    val voltageVolts: Float
        get() = voltageMv / 1000f

    val isCharging: Boolean
        get() = status == BatteryStatus.CHARGING || plugType != PlugType.UNPLUGGED
}
