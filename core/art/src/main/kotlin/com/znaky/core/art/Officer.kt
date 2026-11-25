package com.znaky.core.art

import kotlin.math.sin

/** The traffic officer, front view, drawn in a 70 × 110 box scaled to the canvas. */
object Officer {
    private const val UNIFORM = 0xFF243246
    private const val UNIFORM_DARK = 0xFF19222F
    private const val VEST = 0xFFD4F03C
    private const val VEST_DARK = 0xFFB5D12A
    private const val REFLECT = 0xFFE4E8EE
    private const val SKIN = 0xFFF0C6A0
    private const val WHITE = 0xFFFFFFFF

    /** [armsOut]: both arms to the sides; otherwise the right arm points forward. [t] animates the baton. */
    fun draw(c: ArtCanvas, armsOut: Boolean, t: Float) = c.layer {
        val s = minOf(c.width / 70f, c.height / 110f)
        translate((c.width - 70f * s) / 2, (c.height - 110f * s) / 2)
        scale(s)
        val cx = 35f
        oval(17f, 101f, 53f, 108f, fill(0xFF000000, 0.25f))
        // Legs and shoes.
        rect(26.5f, 62f, 34.2f, 103f, fill(linear(0f, 62f, 0f, 103f, UNIFORM, UNIFORM_DARK)), radius = 3f)
        rect(35.8f, 62f, 43.5f, 103f, fill(linear(0f, 62f, 0f, 103f, UNIFORM, UNIFORM_DARK)), radius = 3f)
        rect(25f, 100f, 34.6f, 105f, fill(0xFF0E1218), radius = 2.4f)
        rect(35.4f, 100f, 45f, 105f, fill(0xFF0E1218), radius = 2.4f)
        // Arms behind the torso when lowered.
        if (!armsOut) {
            arm(this, 22.5f, 31f, 18.5f, 64f, 6.2f)
        }
        // Torso: uniform under a high-visibility vest with reflective stripes.
        path(ArtPath.roundedPolygon(5f, 22f, 27f, 48f, 27f, 47f, 66f, 23f, 66f), fill(linear(22f, 0f, 48f, 0f, VEST_DARK, VEST, VEST_DARK)))
        rect(23f, 46f, 47f, 49.5f, fill(REFLECT))
        rect(23.2f, 55f, 46.8f, 58.5f, fill(REFLECT))
        line(cx, 28f, cx, 66f, stroke(VEST_DARK, 0.8f))
        path(ArtPath.svg("M30 27 L35 35 L40 27 Z"), fill(UNIFORM))
        rect(29f, 25f, 41f, 29f, fill(UNIFORM), radius = 2f)
        // Head and cap with a white top and black peak.
        rect(32f, 20f, 38f, 26f, fill(Ink.shade(SKIN, 0.9f)))
        circle(cx, 16f, 7.2f, fill(SKIN))
        circle(32.4f, 16.5f, 0.8f, fill(0xFF2B2B2B)); circle(37.6f, 16.5f, 0.8f, fill(0xFF2B2B2B))
        path(ArtPath.svg("M32.6 20 Q35 21.6 37.4 20"), stroke(0xFF9C5B4A, 0.8f))
        path(ArtPath.svg("M26.5 11.5 Q27 3.6 35 3.4 Q43 3.6 43.5 11.5 Z"), fill(WHITE))
        rect(26.6f, 9.6f, 43.4f, 12.4f, fill(UNIFORM), radius = 0.8f)
        path(ArtPath.svg("M28 12.2 Q35 15.4 42 12.2 L42 13 Q35 16.2 28 13 Z"), fill(0xFF0E1218))
        circle(cx, 10.8f, 1.1f, fill(0xFFF2C14E))
        if (armsOut) {
            arm(this, 23f, 31f, 1.5f, 31f, 6.2f)
            arm(this, 47f, 31f, 68.5f, 31f, 6.2f)
            baton(this, 68.5f, 31f, 0f)
        } else {
            // Right arm (viewer's left) comes towards us, foreshortened; the baton nods a little.
            val nod = 5f * sin(t * 12f)
            arm(this, 23f, 31f, 15f, 40f, 7.6f)
            layer { rotate(nod, 15f, 40f); baton(this, 15f, 40f, 115f) }
        }
        if (!armsOut) arm(this, 47f, 31f, 51.5f, 64f, 6.2f, glove = true)
    }

    private fun arm(c: ArtCanvas, x0: Float, y0: Float, x1: Float, y1: Float, w: Float, glove: Boolean = true) = with(c) {
        line(x0, y0, x1, y1, stroke(UNIFORM, w))
        // Reflective cuff and white glove.
        val dx = x1 - x0; val dy = y1 - y0
        val len = kotlin.math.hypot(dx, dy)
        val k = (len - 4.5f) / len
        line(x0 + dx * k, y0 + dy * k, x0 + dx * (k + 0.06f), y0 + dy * (k + 0.06f), stroke(REFLECT, w, Cap.BUTT))
        if (glove) circle(x1, y1, w * 0.55f, fill(WHITE))
    }

    /** A black-and-white striped baton from the hand at ([x], [y]) at [deg] (0 = to the right, clockwise). */
    private fun baton(c: ArtCanvas, x: Float, y: Float, deg: Float) = c.layer {
        rotate(deg, x, y)
        rect(x - 0.5f, y - 1.3f, x + 11f, y + 1.3f, fill(WHITE), radius = 1.3f)
        for (i in 0..2) rect(x + 1.5f + i * 3.4f, y - 1.3f, x + 3.1f + i * 3.4f, y + 1.3f, fill(0xFF14171C))
        rect(x - 0.5f, y - 1.3f, x + 11f, y + 1.3f, stroke(0xFF000000, 0.3f, alpha = 0.4f), radius = 1.3f)
    }
}
