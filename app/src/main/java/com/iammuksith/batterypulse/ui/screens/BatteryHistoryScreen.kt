package com.iammuksith.batterypulse.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iammuksith.batterypulse.R
import com.iammuksith.batterypulse.data.local.BatteryLogEntity
import com.iammuksith.batterypulse.ui.theme.CleanBorderColor
import com.iammuksith.batterypulse.ui.theme.EmeraldPrimary
import com.iammuksith.batterypulse.ui.theme.EmeraldSecondary
import com.iammuksith.batterypulse.ui.theme.PureWhite
import java.util.Date

@Composable
fun BatteryHistoryScreen(
    logs: List<BatteryLogEntity>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${logs.size} recorded events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (logs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearHistory,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.history_clear_btn), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("history_empty_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CleanBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = "History Empty",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No History Logged Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Interactive Timeline Chart Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_chart_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CleanBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = "Graph",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Discharge & Charge Curve",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val currentLevel = logs.firstOrNull()?.level ?: 0
                            Text(
                                text = "Current: $currentLevel%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Canvas Chart
                        BatteryTimelineChart(
                            logs = logs.take(30),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            // Recent Logs Section Title
            item {
                Text(
                    text = "Event Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(logs, key = { it.id }) { log ->
                val timeStr = DateFormat.format("h:mm a, MMM dd", Date(log.timestamp)).toString()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_item_${log.id}"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (log.isCharging) {
                                    EmeraldPrimary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (log.isCharging) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = "Charging",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${log.level}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (log.isCharging) "Charging (${log.level}%)" else "Battery ${log.level}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("%.1f°C", log.temperature),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${log.voltage} mV",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun BatteryTimelineChart(
    logs: List<BatteryLogEntity>,
    modifier: Modifier = Modifier
) {
    val primaryColor = EmeraldPrimary
    val secondaryColor = EmeraldSecondary
    val gridColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        if (logs.size < 2) {
            // Draw baseline if only 1 item
            val y = size.height * (1f - (logs.firstOrNull()?.level ?: 50) / 100f)
            drawLine(
                color = primaryColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.dp.toPx()
            )
            return@Canvas
        }

        val reversedLogs = logs.reversed() // Oldest to newest
        val widthStep = size.width / (reversedLogs.size - 1)

        // Draw horizontal grid lines (25%, 50%, 75%, 100%)
        for (gridPct in listOf(0.25f, 0.50f, 0.75f, 1.0f)) {
            val gridY = size.height * (1f - gridPct)
            drawLine(
                color = gridColor,
                start = Offset(0f, gridY),
                end = Offset(size.width, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Build path for battery curve
        val path = Path()
        val fillPath = Path()

        reversedLogs.forEachIndexed { index, item ->
            val x = index * widthStep
            val y = size.height * (1f - (item.level.coerceIn(0, 100) / 100f))

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * widthStep
                val prevY = size.height * (1f - (reversedLogs[index - 1].level.coerceIn(0, 100) / 100f))
                val midX = (prevX + x) / 2f
                path.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        // Draw gradient fill under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.25f),
                    primaryColor.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        // Draw smooth main line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(secondaryColor, primaryColor)
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points on vertices
        reversedLogs.forEachIndexed { index, item ->
            val x = index * widthStep
            val y = size.height * (1f - (item.level.coerceIn(0, 100) / 100f))
            drawCircle(
                color = if (item.isCharging) secondaryColor else primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
