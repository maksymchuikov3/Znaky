package com.znaky.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private fun ZnakyColors.toMaterial() = if (isDark) {
    darkColorScheme(
        primary = link, onPrimary = background, primaryContainer = primaryTint, onPrimaryContainer = onPrimaryTint,
        secondary = accent, background = background, onBackground = textPrimary, surface = surface,
        onSurface = textPrimary, onSurfaceVariant = textSecondary, surfaceVariant = surfaceMuted,
        outline = separator, outlineVariant = separator, error = error, errorContainer = errorTint,
    )
} else {
    lightColorScheme(
        primary = primary, onPrimary = onPrimary, primaryContainer = primaryTint, onPrimaryContainer = onPrimaryTint,
        secondary = accent, background = background, onBackground = textPrimary, surface = surface,
        onSurface = textPrimary, onSurfaceVariant = textSecondary, surfaceVariant = surfaceMuted,
        outline = separator, outlineVariant = separator, error = error, errorContainer = errorTint,
    )
}

@Composable
fun ZnakyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkZnakyColors else LightZnakyColors
    CompositionLocalProvider(LocalZnakyColors provides colors) {
        MaterialTheme(colorScheme = colors.toMaterial(), typography = ZnakyTypography, content = content)
    }
}

/** Short access to the design tokens: `ZnakyTheme.colors.surface`. */
object ZnakyTheme {
    val colors: ZnakyColors
        @Composable @ReadOnlyComposable get() = LocalZnakyColors.current
}
