package com.example.skillbridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SkillBridgeColorScheme = lightColorScheme(
    primary = SkillBridgePrimary,
    onPrimary = SkillBridgeWhite,
    primaryContainer = SkillBridgePrimarySoft,
    onPrimaryContainer = SkillBridgePrimaryHover,
    secondary = SkillBridgePrimaryHover,
    onSecondary = SkillBridgeWhite,
    secondaryContainer = SkillBridgeSurfaceMuted,
    onSecondaryContainer = SkillBridgePrimaryHover,
    background = SkillBridgeBg,
    onBackground = SkillBridgeText,
    surface = SkillBridgeBg,
    onSurface = SkillBridgeText,
    surfaceVariant = SkillBridgeSurfaceMuted,
    onSurfaceVariant = SkillBridgeTextMuted,
    outline = SkillBridgeBorder,
    outlineVariant = SkillBridgeBorder,
    error = SkillBridgeDanger,
    onError = SkillBridgeWhite
)

@Composable
fun SkillBridgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SkillBridgeColorScheme,
        typography = Typography,
        content = content
    )
}
