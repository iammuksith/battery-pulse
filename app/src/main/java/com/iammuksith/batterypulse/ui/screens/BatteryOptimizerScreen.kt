package com.iammuksith.batterypulse.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iammuksith.batterypulse.R
import com.iammuksith.batterypulse.ui.theme.CleanBorderColor
import com.iammuksith.batterypulse.ui.theme.EmeraldPrimary
import com.iammuksith.batterypulse.ui.theme.EmeraldSecondary
import com.iammuksith.batterypulse.ui.theme.HealthAlertColor
import com.iammuksith.batterypulse.ui.theme.HealthWarningColor
import com.iammuksith.batterypulse.ui.theme.PureWhite
import com.iammuksith.batterypulse.util.NotificationHelper

@Composable
fun BatteryOptimizerScreen(
    isLowPowerMode: Boolean,
    onToggleLowPowerMode: (Boolean) -> Unit,
    is80PercentAlert: Boolean,
    onToggle80PercentAlert: (Boolean) -> Unit,
    isTempAlert: Boolean,
    onToggleTempAlert: (Boolean) -> Unit,
    safeTempThreshold: Float,
    onSelectTempThreshold: (Float) -> Unit,
    isLowBatteryAlert: Boolean,
    onToggleLowBatteryAlert: (Boolean) -> Unit,
    lowBatteryThreshold: Int,
    onSelectLowBatteryThreshold: (Int) -> Unit,
    onTestLowBatteryAlert: () -> Unit,
    onTestOverheatAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    var hasNotificationPermission by remember {
        mutableStateOf(notificationHelper.hasNotificationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("optimizer_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = stringResource(R.string.optimizer_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.optimizer_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // In-App Battery-Saving Mode Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("low_power_mode_card"),
                shape = RoundedCornerShape(16.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = "Battery Saver",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.saver_mode_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.saver_mode_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isLowPowerMode,
                        onCheckedChange = onToggleLowPowerMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("low_power_switch")
                    )
                }
            }
        }

        // Smart Battery Pulse Protection Alerts
        item {
            Text(
                text = stringResource(R.string.alerts_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Notification Permission Rationale Card (if needed on Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_rationale_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, HealthWarningColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Permission Info",
                                tint = HealthWarningColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notification Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.permission_rationale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("grant_notification_btn")
                        ) {
                            Text(text = stringResource(R.string.permission_grant), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 1. Low Battery Warning (< 20%) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alert_low_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = "Low Battery Alert",
                                tint = HealthAlertColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.alert_low_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Notify when battery drops below $lowBatteryThreshold%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isLowBatteryAlert,
                            onCheckedChange = { checked ->
                                if (checked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                onToggleLowBatteryAlert(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("alert_low_switch")
                        )
                    }

                    if (isLowBatteryAlert) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Alert Threshold Level:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(15, 20, 25).forEach { lvl ->
                                FilterChip(
                                    selected = lowBatteryThreshold == lvl,
                                    onClick = { onSelectLowBatteryThreshold(lvl) },
                                    label = { Text("$lvl%") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. High Temperature / Overheat Warning Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alert_temp_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Temp Alert",
                                tint = HealthWarningColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.alert_temp_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format("Notify when temperature exceeds %.0f°C", safeTempThreshold),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isTempAlert,
                            onCheckedChange = { checked ->
                                if (checked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                onToggleTempAlert(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("alert_temp_switch")
                        )
                    }

                    if (isTempAlert) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Safe Temperature Limit:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(38.0f, 40.0f, 42.0f, 45.0f).forEach { limit ->
                                FilterChip(
                                    selected = safeTempThreshold == limit,
                                    onClick = { onSelectTempThreshold(limit) },
                                    label = { Text("${limit.toInt()}°C") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 80% Charge Limit Reminder Toggle
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alert_80_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "80% Alert",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.alert_80_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.alert_80_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = is80PercentAlert,
                        onCheckedChange = { checked ->
                            if (checked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onToggle80PercentAlert(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("alert_80_switch")
                    )
                }
            }
        }

        // 4. Test Notification Buttons (Immediate Verification)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_notifications_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Test Notification Delivery",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Instantly trigger notifications to verify sound, vibration, and alert appearance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onTestLowBatteryAlert()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_low_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Test Low (<20%)", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onTestOverheatAlert()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_overheat_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Test Overheat", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Control Background Apps & Drain Control Section
        item {
            Text(
                text = "Control Background Apps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            OutlinedCard(
                onClick = { openSystemBatterySaver(context) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_system_saver_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "System Saver",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.action_system_saver),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Activate system-level CPU and background task throttle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            OutlinedCard(
                onClick = { openBatteryUsageSummary(context) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_battery_usage_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Battery Usage",
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.action_battery_usage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Identify top power draining applications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            OutlinedCard(
                onClick = { openManageApps(context) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CleanBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_manage_apps_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Manage Apps",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.action_manage_apps),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Put unused apps into deep sleep to stop drain",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun openSystemBatterySaver(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallback = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    }
}

private fun openBatteryUsageSummary(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        openManageApps(context)
    }
}

private fun openManageApps(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallback = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    }
}
