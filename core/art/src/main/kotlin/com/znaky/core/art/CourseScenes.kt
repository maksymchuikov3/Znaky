package com.znaky.core.art

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated illustrations of the moto and boat courses, drawn on a 358 × 250 grid like [CarScenes].
 * [t] is loop time 0..1. Boats are drawn from above with north up; a heading of 0° points right.
 */
object CourseScenes {
    const val GRID_W = 358f
    const val GRID_H = 250f

    enum class Kind {
        BOAT_SIZES, BOAT_HEADON, BOAT_CROSSING, BOAT_SAIL, BOAT_OVERTAKE, BOAT_WAKE, BOAT_CHANNEL,
        BOAT_JETSKI_ZONE, BOAT_TOW, BOAT_MOB, BOAT_PARTS, BOAT_MOTOR, BOAT_VEST, BOAT_CHECKLIST,
        MOTO_TYPES, MOTO_HELMET, MOTO_VISIBLE, MOTO_SPEED, MOTO_LANE, MOTO_CONTROLS, MOTO_COUNTERSTEER,
        MOTO_BRAKING, MOTO_PASSENGER, MOTO_CHECK,
        ;

        val water: Boolean get() = name.startsWith("BOAT")

        companion object {
            /** «boat_headon» → [BOAT_HEADON]; null for unknown ids. */
            fun of(id: String?): Kind? = id?.let { s -> entries.firstOrNull { it.name.equals(s, ignoreCase = true) } }
        }
    }

    private const val INK = 0xFF0B1B2B
    private const val WHITE = 0xFFFFFFFF
    private const val GOOD = 0xFF1E9E5A
    private const val BAD = 0xFFE0453F
    private const val AMBER = 0xFFF2A516
    private const val BLUE = 0xFF2F62C9
    private const val ORANGE = 0xFFE07A2E
    private const val TYRE = 0xFF15191F

    fun draw(c: ArtCanvas, kind: Kind, t: Float, dark: Boolean) = c.layer {
        val k = c.width / GRID_W
        translate(0f, (c.height - GRID_H * k) / 2)
        scale(k)
        when (kind) {
            Kind.BOAT_SIZES -> boatSizes(dark)
            Kind.BOAT_HEADON -> headOn(t, dark)
            Kind.BOAT_CROSSING -> crossing(t, dark)
            Kind.BOAT_SAIL -> sail(t, dark)
            Kind.BOAT_OVERTAKE -> overtake(t, dark)
            Kind.BOAT_WAKE -> wake(t, dark)
            Kind.BOAT_CHANNEL -> channel(t, dark)
            Kind.BOAT_JETSKI_ZONE -> jetskiZone(t, dark)
            Kind.BOAT_TOW -> tow(t, dark)
            Kind.BOAT_MOB -> manOverboard(t, dark)
            Kind.BOAT_PARTS -> boatParts(dark)
            Kind.BOAT_MOTOR -> outboard(t, dark)
            Kind.BOAT_VEST -> vest(t, dark)
            Kind.BOAT_CHECKLIST -> checklist(t, dark)
            Kind.MOTO_TYPES -> motoTypes(dark)
            Kind.MOTO_HELMET -> helmet(t, dark)
            Kind.MOTO_VISIBLE -> visible(t, dark)
            Kind.MOTO_SPEED -> speed(t, dark)
            Kind.MOTO_LANE -> lane(t, dark)
            Kind.MOTO_CONTROLS -> controls(dark)
            Kind.MOTO_COUNTERSTEER -> countersteer(t, dark)
            Kind.MOTO_BRAKING -> braking(t, dark)
            Kind.MOTO_PASSENGER -> passenger(dark)
            Kind.MOTO_CHECK -> motoCheck(t, dark)
        }
    }

    // ---- Helpers --------------------------------------------------------------------------

    private fun kf(t: Float, keys: FloatArray, values: FloatArray): Float {
        if (t <= keys.first()) return values.first()
        if (t >= keys.last()) return values.last()
        var i = 1
        while (t > keys[i]) i++
        val x = ((t - keys[i - 1]) / (keys[i] - keys[i - 1])).coerceIn(0f, 1f)
        val e = x * x * (3 - 2 * x)
        return values[i - 1] + (values[i] - values[i - 1]) * e
    }

    private fun f(vararg v: Float) = v
    private fun lerp(a: Float, b: Float, x: Float) = a + (b - a) * x
    private fun text(dark: Boolean) = if (dark) 0xFFE8EEF5 else INK
    private fun muted(dark: Boolean) = if (dark) 0xFF8D9BAC else 0xFF5B6878

    private fun ArtCanvas.water(dark: Boolean, t: Float = 0f) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) linear(0f, 0f, 0f, GRID_H, 0xFF113246, 0xFF0A2130) else linear(0f, 0f, 0f, GRID_H, 0xFFDDF1FA, 0xFFB9DFF1)))
        // Soft ripples drifting to the left.
        for (row in 0 until 6) for (i in 0 until 6) {
            val y = 22f + row * 42f
            val x = ((i * 70f + row * 31f - t * 70f) % 420f + 420f) % 420f - 40f
            line(x, y, x + 18f, y, stroke(if (dark) 0xFF2A5670 else 0xFFFFFFFF, 2f, alpha = 0.55f))
        }
    }

    private fun ArtCanvas.label(s: String, cx: Float, cy: Float, bg: Long, fg: Long = WHITE, size: Float = 9f) {
        val w = s.length * size * 0.7f + 18f
        rect(cx - w / 2, cy - size, cx + w / 2, cy + size, fill(bg), radius = size)
        text(s, cx, cy, size, fg)
    }

    private fun ArtCanvas.arrow(x1: Float, y1: Float, x2: Float, y2: Float, color: Long, w: Float = 3f, alpha: Float = 1f, dash: FloatArray? = null) {
        line(x1, y1, x2, y2, stroke(color, w, dash = dash, alpha = alpha))
        val a = kotlin.math.atan2(y2 - y1, x2 - x1)
        val s = 4f + w * 2f
        val xs = floatArrayOf(x2, x2 - s * cos(a - 0.5f), x2 - s * cos(a + 0.5f))
        val ys = floatArrayOf(y2, y2 - s * sin(a - 0.5f), y2 - s * sin(a + 0.5f))
        polygon(xs, ys, 3, fill(color, alpha))
    }

    /** A motor boat from above: hull, deck, console, outboard. [heading] in degrees, 0 = right. */
    private fun ArtCanvas.boatTop(cx: Float, cy: Float, len: Float, heading: Float, hull: Long, wakeAlpha: Float = 0.7f) = layer {
        rotate(heading, cx, cy)
        val l = len
        val w = len * 0.4f
        if (wakeAlpha > 0f) {
            val sx = cx - l / 2
            line(sx, cy - w * 0.3f, sx - l * 0.9f, cy - w * 1.1f, stroke(WHITE, 2.4f, alpha = wakeAlpha))
            line(sx, cy + w * 0.3f, sx - l * 0.9f, cy + w * 1.1f, stroke(WHITE, 2.4f, alpha = wakeAlpha))
            line(sx, cy, sx - l * 0.6f, cy, stroke(WHITE, 4f, alpha = wakeAlpha * 0.6f))
        }
        val p = ArtPath.svg(
            "M${cx - l / 2} ${cy - w / 2} L${cx + l * 0.12f} ${cy - w / 2} Q${cx + l * 0.42f} ${cy - w * 0.42f} ${cx + l / 2} ${cy} " +
                "Q${cx + l * 0.42f} ${cy + w * 0.42f} ${cx + l * 0.12f} ${cy + w / 2} L${cx - l / 2} ${cy + w / 2} Z",
        )
        path(p, fill(hull))
        path(p, stroke(Ink.shade(hull, 0.6f), 1.4f))
        rect(cx - l * 0.4f, cy - w * 0.36f, cx + l * 0.12f, cy + w * 0.36f, fill(0xFFF4F1EA), radius = w * 0.2f)
        rect(cx - l * 0.06f, cy - w * 0.22f, cx + l * 0.06f, cy + w * 0.22f, fill(0xFF33414F), radius = 2f)
        rect(cx - l / 2 - l * 0.08f, cy - w * 0.14f, cx - l / 2 + 1f, cy + w * 0.14f, fill(0xFF22272E), radius = 2f)
    }

    /** A sailboat from above: narrow hull and a boom showing the sail side. */
    private fun ArtCanvas.sailTop(cx: Float, cy: Float, len: Float, heading: Float, boom: Float) = layer {
        rotate(heading, cx, cy)
        val l = len
        val w = len * 0.32f
        val p = ArtPath.svg("M${cx - l / 2} ${cy - w / 2} Q${cx + l * 0.2f} ${cy - w * 0.6f} ${cx + l / 2} ${cy} Q${cx + l * 0.2f} ${cy + w * 0.6f} ${cx - l / 2} ${cy + w / 2} Z")
        path(p, fill(0xFFFFFFFF))
        path(p, stroke(0xFF6B7A8C, 1.4f))
        circle(cx + l * 0.1f, cy, 2.6f, fill(INK))
        val bx = cx + l * 0.1f - l * 0.6f * cos(boom * PI.toFloat() / 180f)
        val by = cy - l * 0.6f * sin(boom * PI.toFloat() / 180f)
        path(ArtPath.svg("M${cx + l * 0.1f} $cy Q${(cx + bx) / 2 + 6f} ${(cy + by) / 2 + 10f} $bx $by Z"), fill(0xFFFFFFFF, 0.9f))
        line(cx + l * 0.1f, cy, bx, by, stroke(0xFF465566, 2f))
    }

    private fun ArtCanvas.person(cx: Float, cy: Float, r: Float, vest: Long = ORANGE) {
        circle(cx, cy, r * 1.25f, fill(vest))
        circle(cx, cy - r * 0.15f, r * 0.62f, fill(0xFFF0C9A0))
    }

    private fun ArtCanvas.check(cx: Float, cy: Float, ok: Boolean) {
        circle(cx, cy, 9f, fill(if (ok) GOOD else BAD))
        if (ok) path(ArtPath.svg("M${cx - 4.5f} ${cy} L${cx - 1f} ${cy + 3.5f} L${cx + 5f} ${cy - 3.5f}"), stroke(WHITE, 2.2f))
        else {
            line(cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, stroke(WHITE, 2.2f))
            line(cx + 3.5f, cy - 3.5f, cx - 3.5f, cy + 3.5f, stroke(WHITE, 2.2f))
        }
    }

    // ---- Boat scenes ------------------------------------------------------------------------

    private fun ArtCanvas.boatSizes(dark: Boolean) {
        water(dark)
        // A motor yacht from the side, then a jet ski.
        val hull = 0xFFF7F8FA
        path(ArtPath.svg("M30 120 L212 120 L232 92 L60 92 Q40 92 30 104 Z"), fill(hull))
        path(ArtPath.svg("M30 120 L212 120 L232 92 L60 92 Q40 92 30 104 Z"), stroke(0xFF6B7A8C, 1.5f))
        rect(36f, 112f, 218f, 120f, fill(BLUE))
        path(ArtPath.svg("M80 92 L96 70 L170 70 L188 92 Z"), fill(0xFFDDE5EE))
        rect(104f, 74f, 166f, 86f, fill(0xFF33414F), radius = 3f)
        arrow(130f, 140f, 30f, 140f, text(dark), 1.6f)
        arrow(130f, 140f, 232f, 140f, text(dark), 1.6f)
        label("мале судно — до 20 м", 131f, 158f, BLUE)
        // Jet ski.
        path(ArtPath.svg("M262 118 L338 118 Q346 112 340 104 L300 100 L292 90 L276 90 L268 104 Q256 108 262 118 Z"), fill(0xFFEFF3F7))
        path(ArtPath.svg("M262 118 L338 118 Q346 112 340 104 L300 100 L292 90 L276 90 L268 104 Q256 108 262 118 Z"), stroke(0xFF6B7A8C, 1.5f))
        rect(272f, 104f, 300f, 110f, fill(0xFF1C8DB8), radius = 3f)
        rect(288f, 84f, 296f, 92f, fill(0xFF22272E), radius = 2f)
        arrow(300f, 140f, 260f, 140f, text(dark), 1.6f)
        arrow(300f, 140f, 344f, 140f, text(dark), 1.6f)
        label("гідроцикл ≤ 4 м", 302f, 158f, 0xFF1C8DB8)
        text("водомет, керують на корпусі", 302f, 182f, 7.5f, muted(dark))
        text("прогулянкове судно — до 24 м", 131f, 200f, 8f, muted(dark))
    }

    private fun ArtCanvas.headOn(t: Float, dark: Boolean) {
        water(dark, t)
        val x = lerp(-20f, 378f, t)
        // Each boat sheers to its own starboard as they meet.
        val off = kf(t, f(0f, 0.3f, 0.5f, 0.7f, 1f), f(0f, 22f, 26f, 22f, 10f))
        val ang = kf(t, f(0f, 0.22f, 0.32f, 0.62f, 0.8f), f(0f, 0f, 14f, 0f, -6f))
        boatTop(x, 125f + off, 56f, ang, BLUE)
        boatTop(GRID_W - x, 125f - off, 56f, 180f + ang, ORANGE)
        line(0f, 125f, GRID_W, 125f, stroke(if (dark) 0xFF4D7A94 else 0xFF7FB3CC, 1.4f, dash = floatArrayOf(6f, 6f)))
        label("ЛІВИМИ БОРТАМИ", 179f, 26f, GOOD)
        text("кожне відвертає праворуч", 179f, 228f, 9f, text(dark))
    }

    private fun ArtCanvas.crossing(t: Float, dark: Boolean) {
        water(dark, t)
        // Orange comes from below heading north: it is on blue's starboard and keeps its course.
        val oy = lerp(260f, -30f, t)
        boatTop(210f, oy, 54f, -90f, ORANGE)
        // Blue slows, lets orange pass, then crosses behind its stern.
        val bx = kf(t, f(0f, 0.45f, 1f), f(-20f, 120f, 330f))
        val by = kf(t, f(0f, 0.45f, 0.75f, 1f), f(110f, 110f, 124f, 110f))
        val hd = kf(t, f(0f, 0.45f, 0.6f, 0.85f), f(0f, 0f, 18f, -8f))
        boatTop(bx, by, 54f, hd, BLUE, wakeAlpha = kf(t, f(0f, 0.3f, 0.45f, 0.6f), f(0.7f, 0.15f, 0.15f, 0.7f)))
        label("ПРАВОРУЧ — ПЕРЕВАГА", 258f, 26f, ORANGE)
        label("поступаюсь", 80f, 72f, BLUE)
        text("бачиш судно праворуч — дай йому дорогу", 179f, 230f, 8.5f, text(dark))
    }

    private fun ArtCanvas.sail(t: Float, dark: Boolean) {
        water(dark, t)
        val sx = lerp(380f, -30f, t)
        sailTop(sx, 100f, 60f, 180f, -35f)
        val mx = lerp(-30f, 380f, t)
        val dy = kf(t, f(0f, 0.25f, 0.45f, 0.7f, 1f), f(0f, 30f, 42f, 30f, 10f))
        boatTop(mx, 112f + dy, 50f, kf(t, f(0f, 0.2f, 0.35f, 0.6f), f(0f, 16f, 0f, -8f)), BLUE)
        label("вітрило — головне", 100f, 26f, GOOD)
        label("мотор поступається", 250f, 226f, BLUE)
        arrow(330f, 40f, 300f, 60f, muted(dark), 2f)
        text("вітер", 336f, 32f, 8f, muted(dark))
    }

    private fun ArtCanvas.overtake(t: Float, dark: Boolean) {
        water(dark, t)
        val slow = lerp(60f, 190f, t)
        boatTop(slow, 140f, 46f, 0f, 0xFF8A97A6, wakeAlpha = 0.35f)
        val fx = lerp(-40f, 330f, t)
        val dy = kf(t, f(0f, 0.25f, 0.4f, 0.7f, 0.85f), f(0f, 0f, -48f, -48f, 0f))
        boatTop(fx, 140f + dy, 52f, kf(t, f(0.18f, 0.3f, 0.42f, 0.65f, 0.78f, 0.9f), f(0f, -18f, 0f, 0f, 16f, 0f)), BLUE)
        val gap = slow - fx
        if (gap > 30f && t < 0.35f) {
            line(fx + 30f, 176f, slow - 26f, 176f, stroke(text(dark), 1.4f, dash = floatArrayOf(4f, 3f)))
            label("≥ 100 м", (fx + slow) / 2, 192f, AMBER, INK)
        }
        label("під мостом і ближче 500 м — не можна", 179f, 26f, BAD, size = 8f)
    }

    private fun ArtCanvas.wake(t: Float, dark: Boolean) {
        water(dark, t)
        // Beach strip at the top with swimmers and a buoy line.
        rect(0f, 0f, GRID_W, 46f, fill(if (dark) 0xFF4A3F2E else 0xFFF3E3BF))
        for (i in 0 until 9) circle(20f + i * 40f, 66f, 3.2f, fill(0xFFE53935))
        line(0f, 66f, GRID_W, 66f, stroke(0xFFE53935, 1f, dash = floatArrayOf(3f, 4f), alpha = 0.6f))
        person(90f, 56f, 5f, 0xFF1C8DB8)
        person(240f, 54f, 5f, 0xFF1C8DB8)
        // A kayak near the boat's path.
        val kx = 250f
        layer {
            rotate(-8f + 6f * sin(t * 2 * PI.toFloat() * 2), kx, 160f)
            oval(kx - 30f, 155f, kx + 30f, 165f, fill(0xFFF2A516))
            circle(kx, 160f, 5f, fill(0xFF33414F))
            line(kx - 14f, 152f, kx + 14f, 168f, stroke(0xFF5B6673, 2f))
        }
        val bx = lerp(-40f, 400f, t)
        val near = bx in 150f..330f
        boatTop(bx, 128f, 50f, 0f, BLUE, wakeAlpha = if (near) 0.2f else 0.8f)
        label(if (near) "15 км/год" else "≤ 15 км/год біля людей", 90f, 226f, if (near) GOOD else AMBER, if (near) WHITE else INK)
    }

    private fun ArtCanvas.channel(t: Float, dark: Boolean) {
        water(dark, t)
        // Fairway between buoy rows.
        rect(0f, 86f, GRID_W, 166f, fill(if (dark) 0xFF0C2738 else 0xFFA9D4EA, 0.6f))
        for (i in 0 until 8) {
            val x = 20f + i * 46f
            circle(x, 84f, 4.5f, fill(0xFFE53935)); circle(x, 168f, 4.5f, fill(if (dark) 0xFFDDE3EA else 0xFF1F2A36))
        }
        // A barge moving left along the fairway.
        val sx = lerp(420f, -160f, t)
        rect(sx - 70f, 108f, sx + 70f, 144f, fill(0xFF5C6B7A), radius = 6f)
        rect(sx + 30f, 112f, sx + 60f, 140f, fill(0xFFEFF3F7), radius = 3f)
        rect(sx - 64f, 114f, sx + 20f, 138f, fill(0xFF8C5A3C), radius = 3f)
        // Small boat waits outside, then crosses at 90° once the barge has passed.
        val cross = kf(t, f(0f, 0.62f, 0.9f), f(0f, 0f, 1f))
        boatTop(250f, lerp(212f, 40f, cross), 40f, -90f, ORANGE, wakeAlpha = if (cross in 0.01f..0.99f) 0.7f else 0f)
        label("СУДНОВИЙ ХІД", 70f, 126f, 0x66000000)
        label("перетин — під кутом ~90°", 120f, 226f, ORANGE)
    }

    private fun ArtCanvas.jetskiZone(t: Float, dark: Boolean) {
        water(dark, t)
        rect(0f, 0f, GRID_W, 40f, fill(if (dark) 0xFF4A3F2E else 0xFFF3E3BF))
        for (i in 0 until 9) circle(20f + i * 40f, 60f, 3f, fill(0xFFE53935))
        // 200 m dimension.
        arrow(40f, 64f, 40f, 132f, text(dark), 1.4f)
        arrow(40f, 132f, 40f, 64f, text(dark), 1.4f)
        label("≥ 200 м", 74f, 98f, AMBER, INK)
        // Zone for water fun, fenced by marker poles.
        rect(170f, 150f, 340f, 232f, stroke(0xFFF2A516, 2f, dash = floatArrayOf(6f, 5f)))
        text("зона розваг", 255f, 222f, 8f, text(dark))
        val a = t * 2 * PI.toFloat()
        val jx = if (t < 0.4f) lerp(-20f, 210f, t / 0.4f) else 255f + 50f * cos(a * 1.6f)
        val jy = if (t < 0.4f) lerp(140f, 180f, t / 0.4f) else 190f + 22f * sin(a * 1.6f)
        boatTop(jx, jy, 30f, if (t < 0.4f) 10f else (a * 1.6f * 180f / PI.toFloat() + 90f), 0xFF1C8DB8)
        if (t < 0.4f) label("≤ 15 км/год", 110f, 172f, GOOD)
    }

    private fun ArtCanvas.tow(t: Float, dark: Boolean) {
        water(dark, t)
        val bob = 3f * sin(t * 2 * PI.toFloat() * 3)
        val bx = 250f
        boatTop(bx, 120f, 64f, 0f, BLUE)
        person(bx - 10f, 112f, 4f)
        person(bx - 22f, 128f, 4f, AMBER)
        label("спостерігач", bx - 30f, 160f, AMBER, INK)
        // Rope and banana.
        line(bx - 38f, 120f, 110f, 120f + bob, stroke(text(dark), 1.4f))
        val y = 120f + bob
        rect(30f, y - 9f, 110f, y + 9f, fill(0xFFFFD84D), radius = 9f)
        for (i in 0 until 3) person(48f + i * 24f, y, 4.5f)
        label("усі в жилетах", 70f, 160f, GOOD)
        text("хвиля ≤ 50 см · вітер ≤ 10 м/с · лише вдень", 179f, 222f, 8f, text(dark))
    }

    private fun ArtCanvas.manOverboard(t: Float, dark: Boolean) {
        water(dark, t)
        val px = 250f
        val py = 120f + 3f * sin(t * 2 * PI.toFloat() * 2)
        circle(px, py, 14f + 8f * ((t * 3) % 1f), stroke(WHITE, 1.4f, alpha = 1f - (t * 3) % 1f))
        person(px, py, 6f)
        // Ring thrown.
        val ring = kf(t, f(0f, 0.15f, 0.3f), f(0f, 0f, 1f))
        if (ring > 0f) {
            val rx = lerp(140f, px - 18f, ring)
            val ry = lerp(140f, py + 6f, ring) - 30f * sin(ring * PI.toFloat())
            circle(rx, ry, 8f, stroke(0xFFE53935, 4f))
        }
        val bx = kf(t, f(0f, 0.35f, 0.8f, 1f), f(120f, 140f, 205f, 205f))
        val neutral = t > 0.7f
        boatTop(bx, 150f, 54f, -12f, BLUE, wakeAlpha = if (neutral) 0f else 0.35f)
        label(if (neutral) "мотор — N" else "повільно", bx - 10f, 196f, if (neutral) GOOD else AMBER, if (neutral) WHITE else INK)
        label("гвинт небезпечний!", 270f, 30f, BAD)
    }

    private fun ArtCanvas.boatParts(dark: Boolean) {
        water(dark)
        rect(0f, 150f, GRID_W, GRID_H, fill(if (dark) 0xFF0B2433 else 0xFF9FD0E8, 0.75f))
        val hull = ArtPath.svg("M60 120 L250 120 Q300 120 320 92 L70 92 L60 100 Z")
        path(ArtPath.svg("M60 120 L250 120 Q290 120 310 104 L312 160 Q260 172 66 168 Z"), fill(0xFFE9EEF3, 0.35f))
        path(hull, fill(0xFFF7F8FA))
        path(ArtPath.svg("M62 150 L262 150 Q300 148 316 104 L320 92 L70 92 L60 100 Z"), fill(0xFFF7F8FA))
        path(ArtPath.svg("M62 150 L262 150 Q300 148 316 104 L320 92 L70 92 L60 100 Z"), stroke(0xFF6B7A8C, 1.5f))
        rect(62f, 140f, 280f, 150f, fill(BLUE))
        rect(150f, 70f, 196f, 92f, fill(0xFFDDE5EE), radius = 4f)
        // Outboard on the transom.
        rect(40f, 76f, 62f, 112f, fill(0xFF22272E), radius = 4f)
        rect(46f, 112f, 56f, 168f, fill(0xFF3A4450))
        path(ArtPath.svg("M38 166 L64 166 L60 176 L42 176 Z"), fill(0xFF8A97A6))
        line(30f, 150f, 334f, 150f, stroke(0xFF1C8DB8, 2f, dash = floatArrayOf(5f, 4f)))
        label("ніс", 320f, 72f, INK)
        label("корма", 70f, 58f, INK)
        label("транець", 92f, 192f, AMBER, INK)
        line(66f, 120f, 86f, 184f, stroke(AMBER, 1.4f))
        label("ватерлінія", 260f, 192f, 0xFF1C8DB8)
        label("лівий — бакборт · правий — штирборт", 179f, 230f, 0x88000000, size = 8f)
    }

    private fun ArtCanvas.outboard(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) linear(0f, 0f, 0f, GRID_H, 0xFF1C2733, 0xFF141D27) else linear(0f, 0f, 0f, GRID_H, 0xFFFFFFFF, 0xFFEEF2F7)))
        rect(0f, 160f, GRID_W, GRID_H, fill(if (dark) 0xFF113246 else 0xFFB9DFF1))
        // Transom.
        rect(214f, 60f, 240f, 190f, fill(0xFFDDE5EE), radius = 4f)
        // Motor: cowling, shaft, gearcase, propeller.
        rect(120f, 40f, 206f, 100f, fill(0xFF22272E), radius = 16f)
        rect(130f, 48f, 196f, 58f, fill(0xFF3A4450), radius = 4f)
        rect(150f, 100f, 176f, 196f, fill(0xFF3A4450))
        path(ArtPath.svg("M136 196 L196 196 Q204 206 190 214 L142 214 Q130 206 136 196 Z"), fill(0xFF5B6673))
        rect(200f, 60f, 216f, 80f, fill(0xFF8A97A6), radius = 3f)
        val spin = t * 360f * 6
        layer {
            rotate(spin, 128f, 205f)
            oval(122f, 182f, 134f, 228f, fill(0xFF9AA6B4))
        }
        // Tell-tale water jet.
        val j = (t * 4) % 1f
        for (i in 0 until 4) {
            val x = 150f - (i + j) * 9f
            circle(x, 110f + (i + j) * (i + j) * 2.4f, 2.4f, fill(0xFF4FC3F7))
        }
        label("контрольний струмінь", 96f, 136f, 0xFF1C8DB8)
        label("гвинт", 96f, 214f, BAD)
        label("транець", 270f, 120f, AMBER, INK)
        label("запуск — лише на нейтралі", 179f, 22f, GOOD)
    }

    private fun ArtCanvas.vest(t: Float, dark: Boolean) {
        water(dark, t)
        val bob = 3f * sin(t * 2 * PI.toFloat())
        val cx = 120f
        val cy = 120f + bob
        path(ArtPath.svg("M${cx - 46} ${cy - 50} L${cx - 14} ${cy - 62} L${cx} ${cy - 30} L${cx + 14} ${cy - 62} L${cx + 46} ${cy - 50} L${cx + 42} ${cy + 56} L${cx - 42} ${cy + 56} Z"), fill(ORANGE))
        line(cx, cy - 30f, cx, cy + 56f, stroke(0xFFB85A16, 2f))
        for (y in floatArrayOf(0f, 22f)) rect(cx - 42f, cy + y, cx + 42f, cy + y + 6f, fill(0xFF22272E))
        rect(cx - 36f, cy - 20f, cx - 20f, cy - 12f, fill(0xFFE8EEF5), radius = 2f)
        rect(cx + 20f, cy - 20f, cx + 36f, cy - 12f, fill(0xFFE8EEF5), radius = 2f)
        val rows = listOf("на кожного — свій жилет", "діти до 12 — завжди в жилеті", "вода < +5 °C — усі в жилетах", "гідроцикл — усі в жилетах")
        rows.forEachIndexed { i, s ->
            check(206f, 64f + i * 34f, true)
            text(s, 286f, 64f + i * 34f, 7.6f, text(dark))
        }
    }

    private fun ArtCanvas.checklist(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF141D27 else 0xFFF4F7FB))
        val rows = listOf("погода сприятлива", "судно і мотор справні", "документи і посвідчення", "жилет на кожного", "людей не більше норми")
        val shown = (t * (rows.size + 2)).toInt()
        rows.forEachIndexed { i, s ->
            val y = 40f + i * 38f
            rect(40f, y - 14f, 318f, y + 14f, fill(if (dark) 0xFF1E2A36 else WHITE), radius = 10f)
            if (i < shown) check(60f, y, true) else circle(60f, y, 9f, stroke(muted(dark), 1.6f))
            text(s, 180f, y, 9f, text(dark))
        }
    }

    // ---- Moto scenes ------------------------------------------------------------------------

    private fun ArtCanvas.road(dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) linear(0f, 0f, 0f, GRID_H, 0xFF1C2733, 0xFF141D27) else linear(0f, 0f, 0f, GRID_H, 0xFFFFFFFF, 0xFFEEF2F7)))
        rect(0f, 186f, GRID_W, GRID_H, fill(if (dark) 0xFF2B333C else 0xFF5C6670))
        for (i in 0 until 8) rect(i * 52f, 214f, i * 52f + 26f, 218f, fill(0xFFF4F5F7))
    }

    private fun ArtCanvas.wheel(cx: Float, cy: Float, r: Float, spin: Float) = layer {
        rotate(spin, cx, cy)
        circle(cx, cy, r, fill(TYRE))
        circle(cx, cy, r * 0.74f, fill(0xFF3A4450))
        circle(cx, cy, r * 0.62f, stroke(0xFFB9C2CE, r * 0.08f))
        for (i in 0 until 5) layer {
            rotate(i * 72f, cx, cy)
            line(cx, cy, cx, cy - r * 0.62f, stroke(0xFFB9C2CE, r * 0.08f))
        }
        circle(cx, cy, r * 0.16f, fill(0xFFB9C2CE))
    }

    /** Side view of a road motorcycle, front to the right. Wheel centres at x−52 and x+52, ground at [gy]. */
    private fun ArtCanvas.motoSide(x: Float, gy: Float, color: Long, spin: Float, rider: Boolean = true, pillion: Boolean = false, light: Boolean = false, dip: Float = 0f) {
        val r = 24f
        val fy = gy - r + dip * 0.4f
        if (light) path(ArtPath.svg("M${x + 70} ${fy - 30} L${x + 190} ${gy - 30} L${x + 190} ${gy + 4} Z"), fill(0xFFFFF3B0, 0.45f))
        wheel(x - 52f, gy - r, r, spin)
        wheel(x + 52f, gy - r, r, spin)
        layer {
            rotate(dip * 0.25f, x + 52f, gy - r)
            line(x + 52f, gy - r, x + 38f, gy - r - 54f, stroke(0xFFB9C2CE, 5f))
            line(x + 38f, gy - r - 54f, x + 30f, gy - r - 62f, stroke(0xFF22272E, 4f))
            rect(x + 46f, gy - r - 56f, x + 58f, gy - r - 44f, fill(0xFFFFF3B0), radius = 5f)
        }
        line(x - 52f, gy - r, x - 6f, gy - r - 8f, stroke(0xFF39424E, 5f))
        path(ArtPath.svg("M${x - 20} ${fy - 30} L${x + 24} ${fy - 30} L${x + 14} ${fy - 6} L${x - 16} ${fy - 2} Z"), fill(0xFF3A4450))
        path(ArtPath.svg("M${x - 4} ${fy - 34} Q${x + 10} ${fy - 52} ${x + 36} ${fy - 44} L${x + 34} ${fy - 30} L${x - 4} ${fy - 30} Z"), fill(color))
        path(ArtPath.svg("M${x - 62} ${fy - 34} L${x - 4} ${fy - 34} L${x - 4} ${fy - 26} L${x - 54} ${fy - 26} Z"), fill(0xFF20252C))
        rect(x - 66f, fy - 32f, x - 56f, fy - 26f, fill(0xFFE53935), radius = 2f)
        line(x - 30f, fy - 6f, x - 76f, fy - 18f, stroke(0xFF9AA6B4, 5f))
        if (rider) {
            line(x - 16f, fy - 34f, x + 4f, fy - 8f, stroke(0xFF2D3A4A, 7f))
            line(x + 4f, fy - 8f, x + 6f, fy + 4f, stroke(0xFF2D3A4A, 6f))
            line(x - 16f, fy - 34f, x + 6f, fy - 76f, stroke(0xFF2D3A4A, 10f))
            line(x + 6f, fy - 76f, x + 30f, fy - 60f, stroke(0xFF2D3A4A, 6f))
            circle(x + 10f, fy - 90f, 11f, fill(0xFF1F5FBF))
            rect(x + 12f, fy - 94f, x + 22f, fy - 86f, fill(0xFF0B1B2B), radius = 3f)
        }
        if (pillion) {
            line(x - 40f, fy - 34f, x - 28f, fy - 6f, stroke(0xFF6A4FA8, 7f))
            line(x - 40f, fy - 34f, x - 22f, fy - 70f, stroke(0xFF6A4FA8, 10f))
            circle(x - 20f, fy - 84f, 10f, fill(ORANGE))
        }
    }

    private fun ArtCanvas.motoTypes(dark: Boolean) {
        road(dark)
        // Scooter.
        val sx = 86f
        wheel(sx - 34f, 170f, 16f, 0f)
        wheel(sx + 34f, 170f, 16f, 0f)
        path(ArtPath.svg("M${sx - 48} 160 Q${sx - 46} 132 ${sx - 20} 132 L${sx + 6} 132 L${sx + 10} 160 Z"), fill(0xFF2FA66A))
        path(ArtPath.svg("M${sx + 6} 160 L${sx + 26} 106 L${sx + 34} 106 L${sx + 40} 160 Z"), fill(0xFF2FA66A))
        rect(sx - 40f, 124f, sx - 6f, 132f, fill(0xFF20252C), radius = 4f)
        line(sx + 30f, 106f, sx + 20f, 96f, stroke(0xFF22272E, 4f))
        label("A1 · мопед", sx, 40f, 0xFF2FA66A)
        text("до 50 см³ або 4 кВт", sx, 62f, 8.5f, text(dark))
        // Motorcycle.
        motoSide(262f, 186f, ORANGE, 0f, rider = false)
        label("A · мотоцикл", 262f, 40f, ORANGE)
        text("від 50 см³ або 4 кВт", 262f, 62f, 8.5f, text(dark))
    }

    private fun ArtCanvas.helmet(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF141D27 else 0xFFF4F7FB))
        val cx = 140f
        val cy = 118f
        path(ArtPath.svg("M${cx - 64} ${cy + 30} Q${cx - 70} ${cy - 66} ${cx + 4} ${cy - 70} Q${cx + 72} ${cy - 66} ${cx + 66} ${cy + 10} L${cx + 40} ${cy + 36} L${cx - 20} ${cy + 40} Z"), fill(BLUE))
        path(ArtPath.svg("M${cx + 6} ${cy - 30} Q${cx + 62} ${cy - 28} ${cx + 64} ${cy + 4} L${cx + 14} ${cy + 4} Z"), fill(0xFF0B1B2B))
        path(ArtPath.svg("M${cx + 18} ${cy - 24} Q${cx + 46} ${cy - 22} ${cx + 52} ${cy - 8}"), stroke(0xFF5C7A99, 3f))
        // Chin strap closing.
        val close = kf(t, f(0f, 0.3f, 0.5f, 1f), f(0f, 0f, 1f, 1f))
        val ex = lerp(cx + 30f, cx - 6f, close)
        line(cx - 30f, cy + 34f, cx - 10f, cy + 60f, stroke(0xFF22272E, 4f))
        line(cx + 30f, cy + 34f, ex, cy + 60f, stroke(0xFF22272E, 4f))
        if (close > 0.95f) {
            circle(cx - 8f, cy + 60f, 5f, fill(0xFFB9C2CE))
            label("ЗАСТЕБНУТИЙ", 270f, 110f, GOOD)
        } else label("розстебнутий — злетить", 270f, 110f, BAD)
        text("водій і пасажир", 270f, 140f, 9f, text(dark))
        text("ПДР 2.3 «г», 5.2 «б»", 270f, 160f, 8f, muted(dark))
    }

    private fun ArtCanvas.visible(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(linear(0f, 0f, 0f, GRID_H, 0xFFCFE6FA, 0xFFEFF6FB)))
        rect(0f, 120f, GRID_W, 186f, fill(0xFF9CCB7A))
        rect(0f, 186f, GRID_W, GRID_H, fill(0xFF5C6670))
        for (i in 0 until 8) rect(i * 52f, 214f, i * 52f + 26f, 218f, fill(0xFFF4F5F7))
        // Settlement-end sign 5.50-like plate at left.
        rect(30f, 120f, 34f, 186f, fill(0xFF8A97A6))
        rect(8f, 96f, 58f, 126f, fill(WHITE), radius = 3f)
        line(12f, 122f, 54f, 100f, stroke(BAD, 3f))
        text("кінець міста", 33f, 136f, 6f, INK)
        val x = lerp(60f, 380f, t)
        motoSide(x, 214f, ORANGE, t * 1800f, light = true)
        label("за містом: ДХВ або ближнє світло", 200f, 26f, GOOD)
        text("ПДР п. 9.8", 200f, 48f, 8f, 0xFF3A4757)
    }

    private fun ArtCanvas.speed(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF141D27 else 0xFFF4F7FB))
        val items = listOf(Triple("у місті", 50f, BLUE), Triple("мотоцикл за містом", 80f, ORANGE), Triple("мопед за містом", 60f, 0xFF2FA66A))
        items.forEachIndexed { i, (name, v, col) ->
            val cx = 62f + i * 117f
            val cy = 122f
            arc(cx - 46f, cy - 46f, cx + 46f, cy + 46f, 150f, 240f, stroke(if (dark) 0xFF2A3644 else 0xFFDDE3EA, 9f, cap = Cap.ROUND))
            val frac = (v / 140f) * kf(t, f(0f, 0.15f + i * 0.1f, 0.5f + i * 0.1f, 1f), f(0f, 0f, 1f, 1f))
            arc(cx - 46f, cy - 46f, cx + 46f, cy + 46f, 150f, 240f * frac, stroke(col, 9f, cap = Cap.ROUND))
            val a = (150f + 240f * frac) * PI.toFloat() / 180f
            line(cx, cy, cx + 34f * cos(a), cy + 34f * sin(a), stroke(text(dark), 3f))
            circle(cx, cy, 5f, fill(text(dark)))
            text("${v.toInt()}", cx, cy + 30f, 16f, text(dark))
            text("км/год", cx, cy + 46f, 7f, muted(dark))
            text(name, cx, 210f, 8.5f, text(dark))
        }
        label("стаж до 2 років — не більше 70 за містом", 179f, 30f, AMBER, INK, size = 8f)
    }

    private fun ArtCanvas.lane(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF2B333C else 0xFF6A747E))
        for (i in 0 until 8) {
            val x = ((i * 52f - t * 104f) % 416f + 416f) % 416f - 30f
            rect(x, 123f, x + 26f, 127f, fill(0xFFF4F5F7))
        }
        rect(0f, 0f, GRID_W, 8f, fill(0xFFF4F5F7)); rect(0f, 242f, GRID_W, GRID_H, fill(0xFFF4F5F7))
        // Car with its blind spot.
        val cx = 200f
        path(ArtPath.svg("M${cx - 70} 150 L${cx - 150} 132 L${cx - 150} 236 L${cx - 70} 222 Z"), fill(BAD, 0.25f))
        rect(cx - 70f, 152f, cx + 40f, 220f, fill(0xFF2F6DB5), radius = 16f)
        rect(cx - 10f, 158f, cx + 22f, 214f, fill(0xFF1D3B5E), radius = 8f)
        text("сліпа зона", cx - 112f, 186f, 8f, WHITE)
        // Motorcycle in the next lane moves from blind spot to visible.
        val mx = kf(t, f(0f, 0.4f, 0.8f, 1f), f(90f, 90f, 290f, 290f))
        layer {
            rect(mx - 22f, 70f, mx + 22f, 80f, fill(ORANGE), radius = 5f)
            circle(mx + 4f, 75f, 6f, fill(BLUE))
        }
        val visibleNow = mx > 250f
        label(if (visibleNow) "видно в дзеркалі" else "не тримайся тут", mx, 46f, if (visibleNow) GOOD else BAD)
    }

    private fun ArtCanvas.controls(dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF141D27 else 0xFFF4F7FB))
        // Handlebar from the rider's seat.
        path(ArtPath.svg("M40 120 Q100 100 179 104 Q258 100 318 120"), stroke(0xFF39424E, 9f))
        rect(20f, 108f, 62f, 132f, fill(0xFF22272E), radius = 10f)
        rect(296f, 108f, 338f, 132f, fill(0xFF22272E), radius = 10f)
        path(ArtPath.svg("M70 112 Q40 90 18 96"), stroke(0xFFB9C2CE, 5f))
        path(ArtPath.svg("M288 112 Q318 90 340 96"), stroke(0xFFB9C2CE, 5f))
        rect(150f, 84f, 208f, 114f, fill(0xFF0B1B2B), radius = 8f)
        text("0 км/год", 179f, 99f, 8f, 0xFF5EEAD4)
        label("зчеплення", 46f, 70f, BLUE)
        label("газ", 316f, 150f, ORANGE)
        label("переднє гальмо", 300f, 70f, BAD)
        label("ліва нога: передачі", 90f, 190f, BLUE)
        label("права нога: заднє гальмо", 270f, 190f, BAD)
        text("1 ↓ · N · 2–6 ↑", 90f, 214f, 8.5f, text(dark))
    }

    private fun ArtCanvas.countersteer(t: Float, dark: Boolean) {
        rect(0f, 0f, GRID_W, GRID_H, fill(if (dark) 0xFF2B333C else 0xFF6A747E))
        // Road curving right, seen from behind.
        path(ArtPath.svg("M120 250 Q170 120 330 60"), stroke(0xFFF4F5F7, 3f, dash = floatArrayOf(14f, 10f)))
        val lean = kf(t, f(0f, 0.2f, 0.35f, 0.75f, 0.95f), f(0f, 0f, 24f, 24f, 0f))
        val push = t in 0.18f..0.36f
        layer {
            rotate(lean, 179f, 214f)
            rect(170f, 150f, 188f, 214f, fill(TYRE), radius = 8f)
            rect(150f, 120f, 208f, 150f, fill(ORANGE), radius = 10f)
            rect(136f, 112f, 222f, 118f, fill(0xFF39424E), radius = 3f)
            circle(179f, 84f, 18f, fill(BLUE))
            rect(160f, 96f, 198f, 124f, fill(0xFF2D3A4A), radius = 8f)
            if (push) arrow(222f, 108f, 222f, 84f, AMBER, 3f)
        }
        label(if (push) "права ручка — вперед" else if (lean > 10f) "нахил → поворот праворуч" else "швидкість > 20–30 км/год", 179f, 26f, if (push) AMBER else GOOD, if (push) INK else WHITE)
        text("дивись у поворот", 179f, 236f, 8.5f, WHITE)
    }

    private fun ArtCanvas.braking(t: Float, dark: Boolean) {
        road(dark)
        val brake = kf(t, f(0f, 0.25f, 0.4f, 0.75f, 0.9f), f(0f, 0f, 1f, 1f, 0f))
        val x = 120f + kf(t, f(0f, 0.25f, 0.8f), f(0f, 60f, 110f))
        motoSide(x, 186f, ORANGE, t * 900f * (1f - brake * 0.7f), dip = brake * 10f)
        if (brake > 0.2f) {
            arrow(x + 52f, 120f, x + 52f, 150f, BAD, 4f * brake + 1f)
            arrow(x - 52f, 136f, x - 52f, 150f, BAD, 1.6f)
            label("більше — переднє гальмо", 179f, 30f, BAD)
            label("мотоцикл прямо, плавно", 179f, 54f, GOOD)
        } else label("гальмуй на прямому мотоциклі", 179f, 30f, GOOD)
    }

    private fun ArtCanvas.passenger(dark: Boolean) {
        road(dark)
        motoSide(150f, 186f, ORANGE, 0f, pillion = true)
        // Height ruler.
        line(290f, 186f, 290f, 70f, stroke(text(dark), 1.6f))
        for (i in 0..5) line(284f, 186f - i * 23.2f, 296f, 186f - i * 23.2f, stroke(text(dark), 1.4f))
        label("зріст ≥ 150 см", 300f, 54f, GOOD)
        label("пасажир — у шоломі", 120f, 30f, ORANGE)
        text("ПДР 21.11 «б», 31.4.7 «і»", 120f, 50f, 8f, muted(dark))
    }

    private fun ArtCanvas.motoCheck(t: Float, dark: Boolean) {
        road(dark)
        motoSide(179f, 186f, ORANGE, 0f, rider = false)
        val spots = listOf(Triple(127f, 162f, "шини ≥ 0,8 мм"), Triple(231f, 110f, "світло"), Triple(160f, 150f, "ланцюг"), Triple(231f, 162f, "гальма"), Triple(196f, 92f, "дзеркала, сигнал"))
        val n = (t * (spots.size + 1)).toInt()
        spots.forEachIndexed { i, (x, y, s) ->
            if (i < n) {
                check(x, y, true)
                label(s, if (x < 179f) 60f else 300f, 40f + i * 24f, GOOD, size = 8f)
            }
        }
    }
}
