package com.znaky.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens of the Znaky design (light and dark). Screens use these
 * instead of raw colours, so both themes stay consistent with the mockup.
 */
@Immutable
data class ZnakyColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val separator: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    /** Filled buttons and selected states. */
    val primary: Color,
    val onPrimary: Color,
    /** Links and tinted icons. */
    val link: Color,
    val primaryTint: Color,
    val onPrimaryTint: Color,
    val accent: Color,
    val accentTint: Color,
    val onAccentTint: Color,
    val success: Color,
    val successTint: Color,
    val error: Color,
    val errorTint: Color,
    val purple: Color,
    val purpleTint: Color,
    val teal: Color,
    val tealTint: Color,
    val orange: Color,
    val red: Color,
    val redTint: Color,
    /** Soft tints for section tiles (3D-машина, Розмітка). */
    val sand: Color,
    val sandTint: Color,
    val slate: Color,
    val slateTint: Color,
    val isDark: Boolean,
)

val LightZnakyColors = ZnakyColors(
    background = Color(0xFFF3F4F7),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFE7EAF0),
    separator = Color(0xFFE7EAF0),
    textPrimary = Color(0xFF14202E),
    textSecondary = Color(0xFF56616F),
    textTertiary = Color(0xFF6B7583),
    primary = Color(0xFF2F62C9),
    onPrimary = Color(0xFFFFFFFF),
    link = Color(0xFF2F62C9),
    primaryTint = Color(0xFFE6EEFC),
    onPrimaryTint = Color(0xFF234C9E),
    accent = Color(0xFFFFB547),
    accentTint = Color(0xFFFDEBDD),
    onAccentTint = Color(0xFF9A4512),
    success = Color(0xFF1B6B3D),
    successTint = Color(0xFFE1F4EA),
    error = Color(0xFFB3261E),
    errorTint = Color(0xFFFDECEC),
    purple = Color(0xFF5B3FB0),
    purpleTint = Color(0xFFEEE8FB),
    teal = Color(0xFF0F655E),
    tealTint = Color(0xFFDFF3F2),
    orange = Color(0xFF9A4512),
    red = Color(0xFFB3303E),
    redTint = Color(0xFFFCE6E8),
    sand = Color(0xFF7A5810),
    sandTint = Color(0xFFF6F0E1),
    slate = Color(0xFF3F4E62),
    slateTint = Color(0xFFE9EDF2),
    isDark = false,
)

val DarkZnakyColors = ZnakyColors(
    background = Color(0xFF0F1318),
    surface = Color(0xFF1A2029),
    surfaceMuted = Color(0xFF2A323D),
    separator = Color(0xFF262E39),
    textPrimary = Color(0xFFEEF2F7),
    textSecondary = Color(0xFFAEB8C5),
    textTertiary = Color(0xFF939EAC),
    primary = Color(0xFF7FA6F5),
    onPrimary = Color(0xFF0E1A33),
    link = Color(0xFFA9C2F8),
    primaryTint = Color(0xFF1F2D47),
    onPrimaryTint = Color(0xFFA9C2F8),
    accent = Color(0xFFFFB547),
    accentTint = Color(0xFF3A2A1F),
    onAccentTint = Color(0xFFF2B48A),
    success = Color(0xFF93D9B1),
    successTint = Color(0xFF1C3328),
    error = Color(0xFFFF8A80),
    errorTint = Color(0xFF3A1D1F),
    purple = Color(0xFFC6B5F6),
    purpleTint = Color(0xFF2C2544),
    teal = Color(0xFF8ADBD4),
    tealTint = Color(0xFF193634),
    orange = Color(0xFFF2B48A),
    red = Color(0xFFF2A3AC),
    redTint = Color(0xFF3D2328),
    sand = Color(0xFFE2C981),
    sandTint = Color(0xFF35301F),
    slate = Color(0xFFB9C4D3),
    slateTint = Color(0xFF252D38),
    isDark = true,
)

val LocalZnakyColors = staticCompositionLocalOf { LightZnakyColors }

/** Road-sign colours do not change with the theme: signs look the same on any screen. */
object SignColors {
    val Red = Color(0xFFD7261E)
    val Blue = Color(0xFF1F5FBF)
    val Yellow = Color(0xFFFFD200)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF111111)
}
