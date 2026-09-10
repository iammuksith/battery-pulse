package com.iammuksith.batterypulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iammuksith.batterypulse.ui.screens.BatteryHistoryScreen
import com.iammuksith.batterypulse.ui.screens.BatteryOptimizerScreen
import com.iammuksith.batterypulse.ui.screens.BatteryOverviewScreen
import com.iammuksith.batterypulse.ui.screens.ChargingSessionsScreen
import com.iammuksith.batterypulse.ui.theme.BatteryPulseTheme
import com.iammuksith.batterypulse.ui.theme.ChargingBoltColor
import com.iammuksith.batterypulse.ui.theme.CleanBorderColor
import com.iammuksith.batterypulse.ui.theme.EmeraldPrimary
import com.iammuksith.batterypulse.ui.theme.PureWhite
import com.iammuksith.batterypulse.ui.viewmodel.BatteryViewModel

sealed class ScreenTab(val titleRes: Int, val icon: ImageVector, val tag: String) {
    object Overview : ScreenTab(R.string.tab_overview, Icons.Default.ElectricMeter, "tab_overview")
    object History : ScreenTab(R.string.tab_history, Icons.Default.Timeline, "tab_history")
    object Sessions : ScreenTab(R.string.tab_sessions, Icons.Default.FlashOn, "tab_sessions")
    object Optimizer : ScreenTab(R.string.tab_optimizer, Icons.Default.Tune, "tab_optimizer")
}

class MainActivity : ComponentActivity() {
    private val viewModel: BatteryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BatteryPulseTheme {
                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                val tabs = listOf(
                    ScreenTab.Overview,
                    ScreenTab.History,
                    ScreenTab.Sessions,
                    ScreenTab.Optimizer
                )

                val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
                val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
                val chargingSessions by viewModel.chargingSessions.collectAsStateWithLifecycle()
                val isLowPowerMode by viewModel.isLowPowerModeEnabled.collectAsStateWithLifecycle()
                val is80PercentAlert by viewModel.is80PercentAlertEnabled.collectAsStateWithLifecycle()
                val isTempAlert by viewModel.isTempAlertEnabled.collectAsStateWithLifecycle()
                val safeTempThreshold by viewModel.safeTempThreshold.collectAsStateWithLifecycle()
                val dischargeAnalysis = remember(batteryInfo, recentLogs) {
                    viewModel.calculateDischargeAnalysis(batteryInfo, recentLogs)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PureWhite,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (batteryInfo.isCharging) ChargingBoltColor else EmeraldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isLowPowerMode) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BatterySaver,
                                                    contentDescription = "Eco",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "ECO",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = PureWhite
                            )
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = PureWhite,
                            shadowElevation = 8.dp
                        ) {
                            androidx.compose.foundation.layout.Column {
                                HorizontalDivider(color = CleanBorderColor, thickness = 1.dp)
                                NavigationBar(
                                    containerColor = PureWhite,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    tabs.forEachIndexed { index, tab ->
                                        NavigationBarItem(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = stringResource(tab.titleRes)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(tab.titleRes),
                                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = selectedTabIndex,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> BatteryOverviewScreen(
                                    batteryInfo = batteryInfo,
                                    dischargeAnalysis = dischargeAnalysis,
                                    lowPowerMode = isLowPowerMode,
                                    safeTempThreshold = safeTempThreshold,
                                )
                                1 -> BatteryHistoryScreen(
                                    logs = recentLogs,
                                    onClearHistory = { viewModel.clearHistory() }
                                )
                                2 -> ChargingSessionsScreen(
                                    batteryInfo = batteryInfo,
                                    sessions = chargingSessions,
                                    onClearSessions = { viewModel.clearSessions() }
                                )
                                3 -> BatteryOptimizerScreen(
                                    isLowPowerMode = isLowPowerMode,
                                    onToggleLowPowerMode = { viewModel.toggleLowPowerMode(it) },
                                    is80PercentAlert = is80PercentAlert,
                                    onToggle80PercentAlert = { viewModel.toggle80PercentAlert(it) },
                                    isTempAlert = isTempAlert,
                                    onToggleTempAlert = { viewModel.toggleTempAlert(it) },
                                    safeTempThreshold = safeTempThreshold,
                                    onSelectTempThreshold = { viewModel.setSafeTempThreshold(it) },
                                    onTestOverheatAlert = { viewModel.testOverheatAlert() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
