package com.znaky.core.art

/**
 * Horizontal road markings (ДСТУ 2587) on a patch of asphalt, seen from above.
 * Dash-to-gap ratios follow the standard: 1.5 is 1:3, 1.6 is 3:1.
 */
object Markings {
    private const val PAINT = 0xFFF6F6F1
    val numbers = listOf("1.1", "1.3", "1.5", "1.6", "1.11", "1.14.1")

    fun draw(c: ArtCanvas, number: String, dark: Boolean) = c.layer {
        val w = c.width; val h = c.height
        val top = if (dark) 0xFF3A414B else 0xFF5A6370
        val bottom = if (dark) 0xFF323841 else 0xFF4F5864
        rect(0f, 0f, w, h, fill(linear(0f, 0f, 0f, h, top, bottom)))
        var seed = number.hashCode() and 0xFFFF
        repeat((w * h / 60).toInt()) {
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val x = (seed % 10000) / 10000f * w
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val y = (seed % 10000) / 10000f * h
            circle(x, y, 0.6f + (seed % 3) * 0.25f, fill(if (it % 3 == 0) 0xFFFFFFFF else 0xFF000000, 0.08f))
        }
        val lw = w * 0.032f
        val unit = h / 4.2f
        fun solid(x: Float) {
            rect(x - lw / 2, 0f, x + lw / 2, h, fill(PAINT))
        }
        fun dashed(x: Float, dash: Float, gap: Float, offset: Float = 0f) {
            var y = -offset
            while (y < h) { rect(x - lw / 2, y, x + lw / 2, y + dash, fill(PAINT), radius = lw * 0.15f); y += dash + gap }
        }
        // A faint lane edge on both sides gives the patch a sense of scale.
        rect(w * 0.06f, 0f, w * 0.06f + lw * 0.6f, h, fill(PAINT, 0.35f))
        rect(w * 0.94f - lw * 0.6f, 0f, w * 0.94f, h, fill(PAINT, 0.35f))
        when (number) {
            "1.1" -> solid(w / 2)
            "1.3" -> { solid(w / 2 - lw * 1.5f); solid(w / 2 + lw * 1.5f) }
            "1.5" -> dashed(w / 2, unit * 0.5f, unit * 1.5f, unit * 0.2f)
            "1.6" -> dashed(w / 2, unit * 1.2f, unit * 0.4f, unit * 0.2f)
            "1.11" -> { solid(w / 2 - lw * 1.5f); dashed(w / 2 + lw * 1.5f, unit * 0.5f, unit * 1.5f, unit * 0.2f) }
            "1.14.1" -> {
                val stripe = w * 0.07f
                var x = w * 0.12f
                while (x + stripe <= w * 0.9f) { rect(x, h * 0.14f, x + stripe, h * 0.86f, fill(PAINT), radius = 1f); x += stripe * 2 }
            }
        }
        // Gentle wear on the paint.
        repeat(20) {
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            circle((seed % 1000) / 1000f * w, ((seed / 1000) % 1000) / 1000f * h, 0.9f, fill(bottom, 0.5f))
        }
    }
}
