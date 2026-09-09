package com.iammuksith.batterypulse.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iammuksith.batterypulse.R
import com.iammuksith.batterypulse.data.model.BatteryHealthCondition
import com.iammuksith.batterypulse.data.model.BatteryInfo
import com.iammuksith.batterypulse.data.model.BatteryStatus
import com.iammuksith.batterypulse.data.model.PlugType
import com.iammuksith.batterypulse.ui.components.BatteryGauge
import com.iammuksith.batterypulse.ui.components.MetricCard
import com.iammuksith.batterypulse.ui.theme.CleanBorderColor
import com.iammuksith.batterypulse.ui.theme.EmeraldPrimary
import com.iammuksith.batterypulse.ui.theme.EmeraldSecondary
import com.iammuksith.batterypulse.ui.theme.HealthAlertColor
import com.iammuksith.batterypulse.ui.theme.HealthGoodColor
import com.iammuksith.batterypulse.ui.theme.HealthWarningColor
import com.iammuksith.batterypulse.ui.theme.PureWhite
import com.iammuksith.batterypulse.ui.viewmodel.DischargeAnalysis

@Composable
fun BatteryOverviewScreen(
    batteryInfo: BatteryInfo,
    dischargeAnalysis: DischargeAnalysis,
    lowPowerMode: Boolean,
    safeTempThreshold: Float = 40.0f,
    lowBatteryThreshold: Int = 20,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("overview_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Critical Warning Banner: Safe Temperature Exceeded
        if (batteryInfo.temperatureCelsius >= safeTempThreshold) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overheat_warning_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorContainerAlert),
                    border = BorderStroke(1.dp, HealthAlertColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HealthAlertColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Warning",
                                    tint = HealthAlertColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = String.format("Safe Temperature Exceeded (%.1f°C)", batteryInfo.temperatureCelsius),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthAlertColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("Current temp exceeds your safe threshold of %.1f°C. Please let device cool down to prevent lithium-ion degradation.", safeTempThreshold),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Critical Warning Banner: Battery Low (< 20%)
        if (batteryInfo.level <= lowBatteryThreshold && !batteryInfo.isCharging) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_battery_warning_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorContainerWarning),
                    border = BorderStroke(1.dp, HealthWarningColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HealthWarningColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BatteryAlert,
                                    contentDescription = "Low Battery",
                                    tint = HealthWarningColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.banner_low_battery_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthWarningColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("Battery is at %d%% (below %d%%). Connect charger or enable Battery Saver to extend battery life.", batteryInfo.level, lowBatteryThreshold),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Battery Gauge Centerpiece
        item {
            val statusString = when (batteryInfo.status) {
                BatteryStatus.CHARGING -> stringResource(R.string.status_charging)
                BatteryStatus.DISCHARGING -> stringResource(R.string.status_discharging)
                BatteryStatus.NOT_CHARGING -> stringResource(R.string.status_not_charging)
                BatteryStatus.FULL -> stringResource(R.string.status_full)
                BatteryStatus.UNKNOWN -> stringResource(R.string.status_unknown)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BatteryGauge(
                    level = batteryInfo.level,
                    isCharging = batteryInfo.isCharging,
                    health = batteryInfo.health,
                    statusText = statusString,
                    lowPowerMode = lowPowerMode
                )
            }
        }

        // Time Remaining & Discharge Estimation Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("time_remaining_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (batteryInfo.isCharging) Icons.Default.Bolt else Icons.Default.AccessTime,
                                    contentDescription = "Time Estimate",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (batteryInfo.isCharging) {
                                    stringResource(R.string.time_until_full)
                                } else {
                                    stringResource(R.string.time_until_empty)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            val estimatedMins = dischargeAnalysis.estimatedMinutesRemaining ?: 0
                            val hours = estimatedMins / 60
                            val mins = estimatedMins % 60
                            val formattedTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                            Text(
                                text = if (batteryInfo.status == BatteryStatus.FULL) "Fully Charged" else formattedTime,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Drain / Charge rate badge
                    dischargeAnalysis.drainRatePerHour?.let { rate ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Rate",
                                    tint = EmeraldSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f%%/h", rate),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Technical Metrics Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.card_metrics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Metrics Grid (Cards)
        item {
            val voltFormatted = String.format("%.2f V", batteryInfo.voltageVolts)
            MetricCard(
                title = stringResource(R.string.metric_voltage),
                value = voltFormatted,
                subValue = "${batteryInfo.voltageMv} mV",
                icon = Icons.Default.ElectricMeter,
                iconTint = EmeraldPrimary,
                testTag = "metric_voltage"
            )
        }

        item {
            val tempC = batteryInfo.temperatureCelsius
            val tempF = batteryInfo.temperatureFahrenheit
            val tempTint = when {
                tempC >= safeTempThreshold -> HealthAlertColor
                tempC >= safeTempThreshold - 2f -> HealthWarningColor
                else -> EmeraldPrimary
            }
            MetricCard(
                title = stringResource(R.string.metric_temperature),
                value = String.format("%.1f°C", tempC),
                subValue = String.format("%.1f°F", tempF),
                icon = Icons.Default.Thermostat,
                iconTint = tempTint,
                testTag = "metric_temperature"
            )
        }

        item {
            val healthText = when (batteryInfo.health) {
                BatteryHealthCondition.GOOD -> stringResource(R.string.health_good)
                BatteryHealthCondition.OVERHEAT -> stringResource(R.string.health_overheat)
                BatteryHealthCondition.DEAD -> stringResource(R.string.health_dead)
                BatteryHealthCondition.OVER_VOLTAGE -> stringResource(R.string.health_over_voltage)
                BatteryHealthCondition.FAILURE -> stringResource(R.string.health_unspecified_failure)
                BatteryHealthCondition.COLD -> stringResource(R.string.health_cold)
                BatteryHealthCondition.UNKNOWN -> stringResource(R.string.status_unknown)
            }
            val healthTint = if (batteryInfo.health == BatteryHealthCondition.GOOD) HealthGoodColor else HealthWarningColor

            MetricCard(
                title = stringResource(R.string.metric_health),
                value = healthText,
                subValue = if (batteryInfo.health == BatteryHealthCondition.GOOD) "Healthy Cell" else "Inspect Battery",
                icon = Icons.Default.HealthAndSafety,
                iconTint = healthTint,
                testTag = "metric_health"
            )
        }

        item {
            val plugString = when (batteryInfo.plugType) {
                PlugType.AC -> stringResource(R.string.plug_ac)
                PlugType.USB -> stringResource(R.string.plug_usb)
                PlugType.WIRELESS -> stringResource(R.string.plug_wireless)
                PlugType.DOCK -> stringResource(R.string.plug_dock)
                PlugType.UNPLUGGED -> stringResource(R.string.plug_unplugged)
            }
            MetricCard(
                title = stringResource(R.string.metric_power_source),
                value = plugString,
                subValue = if (batteryInfo.isCharging) "Charging" else "On Battery",
                icon = Icons.Default.Power,
                iconTint = EmeraldSecondary,
                testTag = "metric_power_source"
            )
        }

        item {
            MetricCard(
                title = stringResource(R.string.metric_technology),
                value = batteryInfo.technology,
                subValue = if (batteryInfo.isPresent) "Internal Pack" else "Not Detected",
                icon = Icons.Default.Memory,
                iconTint = EmeraldPrimary,
                testTag = "metric_technology"
            )
        }

        batteryInfo.currentNowMa?.let { currentMa ->
            item {
                MetricCard(
                    title = stringResource(R.string.metric_current),
                    value = "$currentMa mA",
                    subValue = if (currentMa > 0) "Charging Current" else "Discharge Rate",
                    icon = Icons.Default.Speed,
                    iconTint = EmeraldSecondary,
                    testTag = "metric_current"
                )
            }
        }

        // Privacy Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("privacy_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacy Shield",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.card_privacy_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.card_privacy_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private val ColorContainerAlert = androidx.compose.ui.graphics.Color(0xFFFFF1F2)
private val ColorContainerWarning = androidx.compose.ui.graphics.Color(0xFFFFFBEB)
