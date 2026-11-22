package com.znaky.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Asphalt40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Asphalt90,
    onPrimaryContainer = Asphalt10,
    secondary = Amber40,
    secondaryContainer = Amber90,
)

private val DarkColors = darkColorScheme(
    primary = Asphalt80,
    onPrimary = Asphalt10,
    primaryContainer = Asphalt30,
    onPrimaryContainer = Asphalt90,
    secondary = Amber80,
    secondaryContainer = Amber40,
)

@Composable
fun ZnakyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
