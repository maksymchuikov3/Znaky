package com.znaky.core.art

/**
 * The drawing surface every illustration in Znaky is written against.
 *
 * Art is plain Kotlin with no Android types, so the same code draws on screen (through the
 * Compose adapter in core:designsystem) and into PNG previews in unit tests. Colours are
 * ARGB longs, e.g. `0xFFD7261E`.
 */
interface ArtCanvas {
    val width: Float
    val height: Float

    fun save()
    fun restore()
    fun translate(dx: Float, dy: Float)
    fun scale(sx: Float, sy: Float = sx)
    /** Rotates by [degrees] clockwise around ([px], [py]). */
    fun rotate(degrees: Float, px: Float = 0f, py: Float = 0f)
    fun clip(path: ArtPath)

    fun path(path: ArtPath, paint: Paint)
    fun polygon(xs: FloatArray, ys: FloatArray, count: Int, paint: Paint)
    fun circle(cx: Float, cy: Float, r: Float, paint: Paint)
    fun oval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint)
    fun rect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint, radius: Float = 0f)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint)
    /** Arc of the oval in the box; angles in degrees, 0 = 3 o'clock, clockwise. */
    fun arc(left: Float, top: Float, right: Float, bottom: Float, startDeg: Float, sweepDeg: Float, paint: Paint)
    /** Bold text centred on ([cx], [cy]); [size] is the cap-to-baseline em size in canvas units. */
    fun text(text: String, cx: Float, cy: Float, size: Float, color: Long, condensed: Boolean = false)
}

inline fun ArtCanvas.layer(block: ArtCanvas.() -> Unit) {
    save()
    try { block() } finally { restore() }
}

enum class Cap { BUTT, ROUND, SQUARE }

/** How a shape is painted: filled when [stroke] is 0, outlined otherwise. */
class Paint(
    val color: Long = 0xFF000000,
    val stroke: Float = 0f,
    val cap: Cap = Cap.ROUND,
    val roundJoin: Boolean = true,
    val dash: FloatArray? = null,
    val dashPhase: Float = 0f,
    val shader: Shader? = null,
    val alpha: Float = 1f,
)

fun fill(color: Long, alpha: Float = 1f) = Paint(color, alpha = alpha)
fun fill(shader: Shader, alpha: Float = 1f) = Paint(shader = shader, alpha = alpha)
fun stroke(color: Long, width: Float, cap: Cap = Cap.ROUND, dash: FloatArray? = null, phase: Float = 0f, alpha: Float = 1f, roundJoin: Boolean = true) =
    Paint(color, width, cap, roundJoin, dash, phase, alpha = alpha)

sealed interface Shader {
    class Linear(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val colors: LongArray, val stops: FloatArray? = null) : Shader
    class Radial(val cx: Float, val cy: Float, val r: Float, val colors: LongArray, val stops: FloatArray? = null) : Shader
}

fun linear(x0: Float, y0: Float, x1: Float, y1: Float, vararg colors: Long) = Shader.Linear(x0, y0, x1, y1, colors)
fun radial(cx: Float, cy: Float, r: Float, vararg colors: Long) = Shader.Radial(cx, cy, r, colors)

/** Colour helpers. */
object Ink {
    fun alpha(color: Long, a: Float): Long = ((a.coerceIn(0f, 1f) * 255).toLong() shl 24) or (color and 0xFFFFFF)
    /** Mixes [a] towards [b] by [t]. */
    fun mix(a: Long, b: Long, t: Float): Long {
        fun ch(c: Long, s: Int) = ((c shr s) and 0xFF).toFloat()
        fun m(s: Int) = (ch(a, s) + (ch(b, s) - ch(a, s)) * t).toLong().coerceIn(0, 255) shl s
        return m(24) or m(16) or m(8) or m(0)
    }
    fun shade(color: Long, k: Float): Long = if (k >= 1f) mix(color, 0xFFFFFFFF, (k - 1f).coerceAtMost(1f)) else mix(color, 0xFF000000, 1f - k)
}
