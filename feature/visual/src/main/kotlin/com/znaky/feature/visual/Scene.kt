package com.znaky.feature.visual

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.znaky.core.art.ArtCanvas
import com.znaky.core.art.Street
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

/** ARGB of a Compose colour, for the art module. */
internal fun Color.argb(): Long = toArgb().toLong() and 0xFFFFFFFFL

/** Draws [track]'s car at [center] (unit coordinates) heading [angle] degrees. */
internal fun ArtCanvas.drawTrackCar(track: CarTrack, center: Offset, angle: Float) =
    Street.car(this, center.x * 100f, center.y * 100f, angle, track.body.argb())
