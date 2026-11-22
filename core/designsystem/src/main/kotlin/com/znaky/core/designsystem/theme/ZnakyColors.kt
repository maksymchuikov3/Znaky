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
    val isDark: Boolean,
)

val LightZnakyColors = ZnakyColors(
    background = Color(0xFFF2F4F7),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFE3E8EF),
    separator = Color(0xFFE6EAF0),
    textPrimary = Color(0xFF0B1B2B),
    textSecondary = Color(0xFF4A5868),
    textTertiary = Color(0xFF5B6878),
    primary = Color(0xFF24507F),
    onPrimary = Color(0xFFFFFFFF),
    link = Color(0xFF24507F),
    primaryTint = Color(0xFFE8EFF8),
    onPrimaryTint = Color(0xFF1A3C60),
    accent = Color(0xFFFFB547),
    accentTint = Color(0xFFFFF1DB),
    onAccentTint = Color(0xFF8A5A00),
    success = Color(0xFF1E7A3A),
    successTint = Color(0xFFE7F5EC),
    error = Color(0xFFB3261E),
    errorTint = Color(0xFFFDECEC),
    purple = Color(0xFF6D4FC2),
    purpleTint = Color(0xFFEEE9FA),
    teal = Color(0xFF0F766E),
    tealTint = Color(0xFFDDF1EE),
    orange = Color(0xFFD9822B),
    red = Color(0xFFC93A3F),
    redTint = Color(0xFFFDECEC),
    isDark = false,
)

val DarkZnakyColors = ZnakyColors(
    background = Color(0xFF0E141B),
    surface = Color(0xFF18212B),
    surfaceMuted = Color(0xFF232E3A),
    separator = Color(0xFF2A3644),
    textPrimary = Color(0xFFE8EEF5),
    textSecondary = Color(0xFFA9B6C4),
    textTertiary = Color(0xFF8E9BAA),
    primary = Color(0xFF3D73AD),
    onPrimary = Color(0xFFFFFFFF),
    link = Color(0xFF8DB8E8),
    primaryTint = Color(0xFF1C3550),
    onPrimaryTint = Color(0xFFB9D3F0),
    accent = Color(0xFFFFB547),
    accentTint = Color(0xFF3A2A10),
    onAccentTint = Color(0xFFFFC970),
    success = Color(0xFF7FD69A),
    successTint = Color(0xFF15301F),
    error = Color(0xFFFF8A80),
    errorTint = Color(0xFF3A1D1F),
    purple = Color(0xFFB8A6EA),
    purpleTint = Color(0xFF2A2147),
    teal = Color(0xFF5EEAD4),
    tealTint = Color(0xFF123330),
    orange = Color(0xFFF0A35A),
    red = Color(0xFFFF8A80),
    redTint = Color(0xFF3A1D1F),
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
