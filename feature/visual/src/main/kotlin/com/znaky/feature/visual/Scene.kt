package com.znaky.feature.visual

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.atan2

internal object SceneColors {
    val Grass = Color(0xFFC9DDBA)
    val GrassDark = Color(0xFF2F4630)
    val Road = Color(0xFF59636F)
    val Line = Color.White
    val Me = Color(0xFF2F6DB5)
    val MeRoof = Color(0xFF8DB8E8)
    val Yellow = Color(0xFFE7A12E)
    val YellowRoof = Color(0xFFFFD27A)
    val Silver = Color(0xFFE8EEF5)
    val SilverRoof = Color(0xFFB9C6D4)
    val Go = Color(0xFF22C55E)
    val Stop = Color(0xFFEF4444)
}

/**
 * A car that drives through [points] (unit coordinates of the scene, 0..1) reaching
 * point i at time [times]`[i]` (0..1 of the loop). Before the first time and after the
 * last one the car is hidden, so every loop starts clean.
 */
@Immutable
internal data class CarTrack(
    val label: String,
    val body: Color,
    val roof: Color,
    val points: List<Offset>,
    val times: List<Float>,
) {
    /** Position and heading (degrees, 0 = moving up) at loop time [t], or null if hidden. */
    fun at(t: Float): Pair<Offset, Float>? {
        if (t < times.first() || t > times.last()) return null
        val i = (1 until times.size).first { t <= times[it] }
        val a = points[i - 1]
        val b = points[i]
        val span = (times[i] - times[i - 1]).coerceAtLeast(1e-4f)
        val k = ((t - times[i - 1]) / span).coerceIn(0f, 1f)
        val eased = k * k * (3 - 2 * k)
        val pos = a + (b - a) * eased
        // Heading follows the current segment; while parked, keep the next moving segment's heading.
        var j = i
        while (j < points.size && points[j] == points[j - 1]) j++
        val from = if (j < points.size) points[j - 1] else a
        val to = if (j < points.size) points[j] else b
        val angle = if (from == to) 0f else Math.toDegrees(atan2((to.x - from.x).toDouble(), (from.y - to.y).toDouble())).toFloat()
        return pos to angle
    }
}

/** Lanes for right-hand traffic on a 4-way crossing whose roads span 0.41..0.59. */
internal object Lanes {
    const val UP = 0.545f
    const val DOWN = 0.455f
    const val LEFTWARD = 0.455f
    const val RIGHTWARD = 0.545f
}

internal fun DrawScope.drawCrossroads(dark: Boolean, zebra: Boolean = false) {
    val w = size.width
    val h = size.height
    drawRect(if (dark) SceneColors.GrassDark else SceneColors.Grass)
    drawRect(SceneColors.Road, Offset(0f, h * 0.41f), Size(w, h * 0.18f))
    drawRect(SceneColors.Road, Offset(w * 0.41f, 0f), Size(w * 0.18f, h))
    val dash = PathEffect.dashPathEffect(floatArrayOf(w * 0.035f, w * 0.03f))
    val stroke = w * 0.008f
    drawLine(SceneColors.Line, Offset(0f, h * 0.5f), Offset(w * 0.40f, h * 0.5f), stroke, pathEffect = dash)
    drawLine(SceneColors.Line, Offset(w * 0.60f, h * 0.5f), Offset(w, h * 0.5f), stroke, pathEffect = dash)
    drawLine(SceneColors.Line, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h * 0.40f), stroke, pathEffect = dash)
    drawLine(SceneColors.Line, Offset(w * 0.5f, h * 0.60f), Offset(w * 0.5f, h), stroke, pathEffect = dash)
    if (zebra) {
        var x = w * 0.42f
        while (x < w * 0.58f) {
            drawRect(SceneColors.Line, Offset(x, h * 0.62f), Size(w * 0.015f, h * 0.05f))
            x += w * 0.03f
        }
    }
    // Low blocks at the corners read as buildings once the plane is tilted.
    val blocks = listOf(Offset(0.06f, 0.07f), Offset(0.66f, 0.08f), Offset(0.08f, 0.67f), Offset(0.67f, 0.68f))
    val tints = listOf(Color(0xFFF4EDE4), Color(0xFFE9E2F3), Color(0xFFF7F1E8), Color(0xFFE6EEF6))
    blocks.forEachIndexed { i, b ->
        val tl = Offset(b.x * w, b.y * h)
        val s = Size(w * 0.26f, h * 0.24f)
        drawRoundRect(Color.Black.copy(alpha = 0.12f), tl + Offset(w * 0.012f, h * 0.02f), s, CornerRadius(w * 0.02f))
        drawRoundRect(if (dark) tints[i].copy(alpha = 0.25f) else tints[i], tl, s, CornerRadius(w * 0.02f))
    }
}

/** A car seen from above, centred at [center] (unit coords), rotated by [angle] degrees. */
internal fun DrawScope.drawCar(track: CarTrack, center: Offset, angle: Float) {
    val w = size.width
    val c = Offset(center.x * w, center.y * size.height)
    val carW = w * 0.062f
    val carL = w * 0.12f
    rotate(angle, pivot = c) {
        drawRoundRect(Color.Black.copy(alpha = 0.25f), Offset(c.x - carW / 2 + 3f, c.y - carL / 2 + 6f), Size(carW, carL), CornerRadius(carW * 0.3f))
        drawRoundRect(track.body, Offset(c.x - carW / 2, c.y - carL / 2), Size(carW, carL), CornerRadius(carW * 0.3f))
        drawRoundRect(track.roof, Offset(c.x - carW * 0.36f, c.y - carL * 0.2f), Size(carW * 0.72f, carL * 0.46f), CornerRadius(carW * 0.18f))
        // Headlights show which way the car is facing.
        drawCircle(Color(0xFFFFF3B0), carW * 0.12f, Offset(c.x - carW * 0.28f, c.y - carL * 0.44f))
        drawCircle(Color(0xFFFFF3B0), carW * 0.12f, Offset(c.x + carW * 0.28f, c.y - carL * 0.44f))
    }
}
