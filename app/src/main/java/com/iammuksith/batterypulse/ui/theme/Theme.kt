package com.iammuksith.batterypulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pure White & Crisp Light Color Scheme
private val WhiteLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerWhite,
    onPrimaryContainer = OnPrimaryContainerWhite,
    secondary = EmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = EmeraldTertiary,
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = SurfaceMutedLight,
    onBackground = OnSurfaceDarkSlate,
    onSurface = OnSurfaceDarkSlate,
    onSurfaceVariant = OnSurfaceMutedSlate,
    outline = CleanBorderColor,
    outlineVariant = Color(0xFFF1F5F9)
)

@Composable
fun BatteryPulseTheme(
    darkTheme: Boolean = false, // Defaults to pure white light theme per user request
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WhiteLightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun BatteryHealthTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = BatteryPulseTheme(darkTheme, content)

