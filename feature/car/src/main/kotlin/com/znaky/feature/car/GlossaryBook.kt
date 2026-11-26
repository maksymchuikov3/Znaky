package com.znaky.feature.car

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.znaky.core.designsystem.theme.ZnakyTheme

/**
 * Soft «rulebook» palette for the glossary: warm paper, gentle ink, a calm blue cover and a coral ribbon.
 * The theory pages use the same values (BookColors in core:designsystem once merged).
 */
internal data class BookPalette(
    val paper: Color,
    val page: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val rule: Color,
    val cover: Color,
    val coverTint: Color,
    val ribbon: Color,
    val highlight: Color,
)

private val LightBook = BookPalette(
    paper = Color(0xFFFAF6EF),
    page = Color(0xFFFFFDF9),
    ink = Color(0xFF2B2722),
    inkSecondary = Color(0xFF6B6257),
    inkTertiary = Color(0xFF9A8F80),
    rule = Color(0xFFEDE4D3),
    cover = Color(0xFF5B7BC0),
    coverTint = Color(0xFFE9EEF8),
    ribbon = Color(0xFFE07A6F),
    highlight = Color(0xFFFBE3C8),
)

private val DarkBook = BookPalette(
    paper = Color(0xFF1A1815),
    page = Color(0xFF24211D),
    ink = Color(0xFFEDE6DA),
    inkSecondary = Color(0xFFB5AB9B),
    inkTertiary = Color(0xFF857B6D),
    rule = Color(0xFF3A342C),
    cover = Color(0xFF9DB4F0),
    coverTint = Color(0xFF2A2F3B),
    ribbon = Color(0xFFEE8D82),
    highlight = Color(0xFF4A3B2A),
)

internal val book: BookPalette
    @Composable @ReadOnlyComposable
    get() = if (ZnakyTheme.colors.isDark) DarkBook else LightBook

/** Serif type for titles and headwords, so the screen reads like a printed handbook. */
internal object BookType {
    private val serif = FontFamily.Serif
    val coverTitle = TextStyle(fontFamily = serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp, letterSpacing = (-0.01).em)
    val chapterTitle = TextStyle(fontFamily = serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
    val contentsTitle = TextStyle(fontFamily = serif, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
    val headword = TextStyle(fontFamily = serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
    val dropLetter = TextStyle(fontFamily = serif, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
    val intro = TextStyle(fontFamily = serif, fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp)
    val smallCaps = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.12.em, lineHeight = 14.sp)
}
