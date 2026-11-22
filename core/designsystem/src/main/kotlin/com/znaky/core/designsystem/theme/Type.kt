package com.znaky.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Text styles from the mockup, named by role rather than by size. */
@Immutable
object ZnakyType {
    val largeTitle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.025).em, lineHeight = 36.sp)
    val title = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.015).em, lineHeight = 30.sp)
    val title2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.01).em, lineHeight = 26.sp)
    val headline = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp)
    val bodyStrong = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
    val callout = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp)
    val subhead = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
    val footnote = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.02.em, lineHeight = 16.sp)
    val button = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
}

internal val ZnakyTypography = Typography(
    headlineLarge = ZnakyType.largeTitle,
    headlineMedium = ZnakyType.title,
    titleLarge = ZnakyType.title2,
    titleMedium = ZnakyType.headline,
    bodyLarge = ZnakyType.body,
    bodyMedium = ZnakyType.subhead,
    labelLarge = ZnakyType.footnote,
    labelMedium = ZnakyType.caption,
)
