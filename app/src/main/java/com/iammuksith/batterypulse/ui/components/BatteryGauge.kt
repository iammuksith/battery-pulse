package com.iammuksith.batterypulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iammuksith.batterypulse.data.model.BatteryHealthCondition
import com.iammuksith.batterypulse.ui.theme.ChargingBoltColor
import com.iammuksith.batterypulse.ui.theme.HealthAlertColor
import com.iammuksith.batterypulse.ui.theme.HealthGoodColor
import com.iammuksith.batterypulse.ui.theme.HealthWarningColor

@Composable
fun BatteryGauge(
    level: Int,
    isCharging: Boolean,
    health: BatteryHealthCondition,
    statusText: String,
    lowPowerMode: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(
            durationMillis = if (lowPowerMode) 200 else 700,
            easing = FastOutSlowInEasing
        ),
        label = "BatteryLevelAnim"
    )

    val gaugeColor by animateColorAsState(
        targetValue = when {
            isCharging -> HealthGoodColor
            level <= 15 -> HealthAlertColor
            level <= 30 -> HealthWarningColor
            health != BatteryHealthCondition.GOOD && health != BatteryHealthCondition.UNKNOWN -> HealthWarningColor
            else -> HealthGoodColor
        },
        label = "GaugeColorAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "chargingPulse")
    val pulseScale by if (isCharging && !lowPowerMode) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        animateFloatAsState(targetValue = 1f, label = "staticScale")
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("battery_gauge"),
        contentAlignment = Alignment.Center
    ) {
        // Circular Gauge Track & Progress
        val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = (animatedLevel / 100f) * 270f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(gaugeColor, secondaryColor, gaugeColor),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCharging) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = ChargingBoltColor,
                        modifier = Modifier.size((26 * pulseScale).dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CHARGING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Health Status",
                        tint = gaugeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = health.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${animatedLevel.toInt()}%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("battery_status_text")
                )
            }
        }
    }
}
