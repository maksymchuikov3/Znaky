package com.znaky.core.art

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Animated scenes of the «3D-машина» topics, drawn on a 358 × 250 grid. [t] is loop time 0..1. */
object CarScenes {
    const val GRID_W = 358f
    const val GRID_H = 250f

    enum class Kind { BRAKES, ABS, STOPPING, REGEN, CHARGING, HIGH_VOLTAGE }

    private const val INK = 0xFF0B1B2B
    private const val STEEL = 0xFF8A97A6
    private const val CALIPER = 0xFFD23C40
    private const val FLUID = 0xFFE0A400
    private const val AMBER = 0xFFFFD84D
    private const val MINT = 0xFF5EEAD4
    private const val BATTERY = 0xFF34D058
    private const val HV = 0xFFFF8A1F
    private const val TYRE = 0xFF15191F

    /** Whether the scene sits on its own dark "energy" background regardless of theme. */
    fun darkBackdrop(kind: Kind) = kind == Kind.REGEN || kind == Kind.CHARGING || kind == Kind.HIGH_VOLTAGE

    fun draw(c: ArtCanvas, kind: Kind, t: Float, dark: Boolean) = c.layer {
        val k = c.width / GRID_W
        translate(0f, (c.height - GRID_H * k) / 2)
        scale(k)
        when (kind) {
            Kind.BRAKES -> brakes(t, dark)
            Kind.ABS -> abs(t, dark)
            Kind.STOPPING -> stopping(t, dark)
            Kind.REGEN -> regen(t)
            Kind.CHARGING -> charging(t)
            Kind.HIGH_VOLTAGE -> highVoltage(t)
        }
    }

    /** Keyframes: values hold outside the keys and ease between them. */
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

    // ---- Shared parts -------------------------------------------------------------------

    /** A road wheel: tyre with tread, five-spoke alloy rim, hub. Rotated by [spin] degrees. */
    private fun ArtCanvas.wheel(cx: Float, cy: Float, r: Float, spin: Float, rimColor: Long = 0xFFC9D2DD) = layer {
        rotate(spin, cx, cy)
        circle(cx, cy, r, fill(TYRE))
        // Tread blocks around the edge.
        for (i in 0 until 24) layer {
            rotate(i * 15f, cx, cy)
            rect(cx - r * 0.06f, cy - r, cx + r * 0.06f, cy - r * 0.9f, fill(0xFF2A3038), radius = r * 0.02f)
        }
        circle(cx, cy, r * 0.86f, stroke(0xFF242A32, r * 0.04f))
        circle(cx, cy, r * 0.66f, fill(radial(cx - r * 0.2f, cy - r * 0.2f, r * 0.8f, 0xFFF2F5F8, rimColor, Ink.shade(rimColor, 0.7f))))
        for (i in 0 until 5) layer {
            rotate(i * 72f, cx, cy)
            path(ArtPath.svg("M${cx - r * 0.1f} ${cy - r * 0.16f} L${cx - r * 0.07f} ${cy - r * 0.6f} L${cx + r * 0.07f} ${cy - r * 0.6f} L${cx + r * 0.1f} ${cy - r * 0.16f} Z"), fill(0xFF5B6673))
        }
        circle(cx, cy, r * 0.66f, stroke(Ink.shade(rimColor, 0.6f), r * 0.03f))
        circle(cx, cy, r * 0.17f, fill(0xFF3A4450))
        circle(cx, cy, r * 0.07f, fill(0xFFB8C2CD))
    }

    /** Side view of an electric hatchback, front to the right, wheels at x 100 and 284. */
    private fun ArtCanvas.evSide(spin: Float, xray: Boolean = false) {
        val body = ArtPath.svg(
            "M48 190 L46 172 Q46 160 58 156 L94 149 Q114 130 132 120 Q146 112 164 111 L222 111 Q246 112 266 128 L290 147 " +
                "L322 154 Q338 158 338 174 L338 188 Q338 194 332 194 L311 194 A27 27 0 0 0 257 194 L127 194 A27 27 0 0 0 73 194 L54 194 Q48 194 48 190 Z",
        )
        if (xray) {
            path(body, fill(0xFFFFFFFF, 0.07f))
            path(body, stroke(0xFFFFFFFF, 1.6f, alpha = 0.55f))
            return
        }
        oval(56f, 208f, 330f, 222f, fill(0xFF000000, 0.35f))
        path(body, fill(linear(0f, 111f, 0f, 194f, 0xFFF6F8FB, 0xFFE3E9F0, 0xFFC4CFDB)))
        // Glass with a B-pillar.
        val glass = ArtPath.svg("M136 125 Q148 117 164 117 L218 117 Q238 118 256 131 L272 146 L128 146 Z")
        path(glass, fill(linear(0f, 117f, 0f, 146f, 0xFF3E5670, 0xFF1D2B3B)))
        path(ArtPath.svg("M150 121 L176 121 L160 144 L140 144 Z"), fill(0xFFFFFFFF, 0.12f))
        rect(196f, 115f, 202f, 147f, fill(0xFFE3E9F0))
        // Door lines, handle, side skirt, lights, charge port.
        path(ArtPath.svg("M199 148 L199 190 M126 150 Q122 170 128 190"), stroke(0xFF9AA8B8, 1.2f))
        rect(168f, 158f, 184f, 162f, fill(0xFF9AA8B8), radius = 2f)
        rect(226f, 158f, 242f, 162f, fill(0xFF9AA8B8), radius = 2f)
        path(ArtPath.svg("M127 188 L257 188 L257 194 L127 194 Z"), fill(0xFF2B3542))
        path(ArtPath.svg("M318 158 Q332 160 336 168 L322 168 Q316 164 318 158 Z"), fill(0xFFFFF4C2))
        rect(46f, 160f, 54f, 172f, fill(0xFFE5484D), radius = 2f)
        rect(70f, 152f, 82f, 160f, fill(0xFFB9C6D4), radius = 2f)
        wheel(100f, 194f, 23f, spin)
        wheel(284f, 194f, 23f, spin)
    }

    private fun ArtCanvas.battery(x: Float, y: Float, w: Float, h: Float, level: Float, color: Long = BATTERY) {
        rect(x, y, x + w, y + h, fill(0xFF173229), radius = h * 0.28f)
        rect(x, y, x + w, y + h, stroke(color, 2f), radius = h * 0.28f)
        val inner = (w - 8f) * level.coerceIn(0f, 1f)
        if (inner > 1f) rect(x + 4f, y + 4f, x + 4f + inner, y + h - 4f, fill(linear(0f, y, 0f, y + h, Ink.shade(color, 1.25f), color)), radius = (h - 8f) * 0.3f)
        // Cell divisions.
        val cells = 6
        for (i in 1 until cells) {
            val cx = x + w * i / cells
            line(cx, y + 3f, cx, y + h - 3f, stroke(0xFF000000, 1f, alpha = 0.25f))
        }
    }

    private fun ArtCanvas.bolt(x: Float, y: Float, s: Float, color: Long) {
        path(
            ArtPath().moveTo(x + 6 * s, y).lineTo(x - 6 * s, y + 17 * s).lineTo(x + 1 * s, y + 17 * s).lineTo(x - 4 * s, y + 32 * s)
                .lineTo(x + 9 * s, y + 12 * s).lineTo(x + 2 * s, y + 12 * s).close(),
            fill(color),
        )
    }

    // ---- Scenes ---------------------------------------------------------------------------

    private fun ArtCanvas.brakes(t: Float, dark: Boolean) {
        val pedal = kf(t, f(0f, .3f, .42f, .8f, .9f), f(0f, 0f, 18f, 18f, 0f))
        val pad = kf(t, f(0f, .35f, .45f, .8f, .9f), f(0f, 0f, 6f, 6f, 0f))
        val fluid = kf(t, f(0f, .3f, .45f, .8f, .92f), f(0f, 0f, 1f, 1f, 0f))
        val heat = kf(t, f(0f, .4f, .55f, .78f, .95f), f(0f, 0f, .85f, .85f, 0f))
        val disc = kf(t, f(0f, .4f, .75f), f(0f, 540f, 660f))
        val empty = if (dark) 0xFF3A4756 else 0xFFDCE2EA
        val fg = if (dark) 0xFFE8EEF5 else INK

        // Pedal box: hinge bracket, arm and a ribbed rubber pad.
        rect(34f, 34f, 54f, 48f, fill(if (dark) 0xFF4A5868 else 0xFFB9C4D0), radius = 4f)
        circle(43f, 44f, 4f, fill(STEEL))
        layer {
            rotate(pedal, 43f, 44f)
            path(ArtPath.svg("M39 44 L47 44 L49 122 L37 122 Z"), fill(linear(37f, 0f, 49f, 0f, 0xFF5A6878, 0xFF3C4856)))
            rect(24f, 118f, 62f, 136f, fill(if (dark) 0xFFCBD5E1 else 0xFF1F2933), radius = 6f)
            for (i in 0..3) rect(28f, 121f + i * 3.6f, 58f, 122.6f + i * 3.6f, fill(if (dark) 0xFF9AA8B8 else 0xFF3C4856), radius = 0.8f)
            // Push rod to the master cylinder.
            line(46f, 70f, 76f, 86f, stroke(STEEL, 4f))
        }
        // Master cylinder with reservoir.
        rect(70f, 80f, 122f, 106f, fill(linear(0f, 80f, 0f, 106f, 0xFFE3E9F0, 0xFFAEB9C6)), radius = 7f)
        rect(84f, 62f, 110f, 82f, fill(0xFFF3EBD0, alpha = 0.95f), radius = 4f)
        rect(86f, 70f, 108f, 80f, fill(FLUID, 0.85f), radius = 2f)
        rect(90f, 58f, 104f, 63f, fill(0xFF3C4856), radius = 2f)
        // Brake line, filling with pressure.
        val pipe = ArtPath().moveTo(122f, 93f).lineTo(196f, 93f).quadTo(212f, 93f, 212f, 109f).lineTo(212f, 128f)
        path(pipe, stroke(empty, 7f))
        if (fluid > 0.01f) path(pipe, stroke(FLUID, 5f, dash = floatArrayOf(115f * fluid, 400f)))
        // Arrows showing pressure travelling down the line.
        if (fluid in 0.05f..0.99f) for (i in 0..2) {
            val x = 130f + ((t * 900f + i * 24f) % 70f)
            path(ArtPath.svg("M$x 87 L${x + 6} 93 L$x 99"), stroke(0xFFFFFFFF, 2f, alpha = 0.9f))
        }

        // Ventilated disc with drilled holes.
        layer {
            rotate(disc, 270f, 160f)
            circle(270f, 160f, 68f, fill(radial(250f, 140f, 90f, 0xFFE4E9EE, 0xFFB7C1CC, 0xFF8E9AA8)))
            circle(270f, 160f, 68f, stroke(0xFF7D8997, 2f))
            circle(270f, 160f, 30f, stroke(0xFF9DA9B6, 2f))
            for (ring in 0..1) for (i in 0 until 12) {
                val a = (i * 30f + ring * 15f) * PI.toFloat() / 180f
                val rr = if (ring == 0) 44f else 56f
                circle(270f + rr * cos(a), 160f + rr * sin(a), 3f, fill(0xFF6F7B88))
            }
            circle(270f, 160f, 26f, fill(radial(262f, 152f, 30f, 0xFFD0D7DF, STEEL)))
            for (i in 0 until 5) {
                val a = (i * 72f) * PI.toFloat() / 180f
                circle(270f + 15f * cos(a), 160f + 15f * sin(a), 3.6f, fill(0xFF4D5864))
            }
            circle(270f, 160f, 7f, fill(0xFF4D5864))
        }
        if (heat > 0.01f) {
            circle(270f, 160f, 62f, stroke(0xFFFF6A3D, 11f, alpha = heat * 0.8f))
            circle(270f, 160f, 62f, stroke(0xFFFFC46B, 3f, alpha = heat))
        }
        // Caliper hugging the disc edge, pads squeezing.
        rect(196f, 126f, 238f, 194f, fill(linear(196f, 0f, 238f, 0f, Ink.shade(CALIPER, 1.15f), CALIPER, Ink.shade(CALIPER, 0.75f))), radius = 12f)
        rect(203f, 140f, 231f, 146f, fill(0xFFFFFFFF, 0.25f), radius = 3f)
        rect(201f + pad, 134f, 209f + pad, 186f, fill(0xFF2B3A4A), radius = 2f)
        rect(225f - pad, 134f, 233f - pad, 186f, fill(0xFF2B3A4A), radius = 2f)
        text("Педаль", 43f, 152f, 10f, fg)
        text("Супорт", 217f, 212f, 10f, fg)
    }

    private fun ArtCanvas.abs(t: Float, dark: Boolean) {
        val fg = if (dark) 0xFFE8EEF5 else INK
        val grid = if (dark) 0xFF3A4756 else 0xFFE1E6ED
        val pulses = 6
        val spin = 360f * t * 2.2f + 14f * sin(2 * PI.toFloat() * pulses * t)
        wheel(86f, 132f, 60f, spin)
        // Pressure chart.
        val l = 168f; val r = 342f; val top = 70f; val bottom = 200f
        rect(l - 6f, top - 14f, r + 6f, bottom + 8f, fill(if (dark) 0xFF1B2430 else 0xFFFFFFFF, 0.8f), radius = 14f)
        for (i in 1..3) line(l, bottom - (bottom - top) * i / 4f, r, bottom - (bottom - top) * i / 4f, stroke(grid, 1f, dash = floatArrayOf(4f, 4f)))
        line(l, bottom, r, bottom, stroke(grid, 2f))
        val p = ArtPath()
        val steps = 120
        for (i in 0..steps) {
            val x = l + (r - l) * i / steps
            val ph = (i.toFloat() / steps * 3f + t * 3f) % 1f
            // Smoothed square wave: pressure builds, releases, builds again.
            val v = 0.6f + 0.25f * (if (ph < 0.5f) 1f else -1f) * minOf(1f, minOf(ph, 1f - ph, kotlin.math.abs(ph - 0.5f)) * 14f)
            val y = bottom - (bottom - top) * v
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        path(p, stroke(CALIPER, 3.5f))
        text("тиск у гальмах", (l + r) / 2, bottom + 18f, 10f, fg)
        // ABS lamp blinking in time with the pulses.
        val on = (t * pulses) % 1f < 0.5f
        rect(292f, 18f, 344f, 46f, fill(if (on) AMBER else grid), radius = 9f)
        text("ABS", 318f, 32f, 12f, if (on) INK else Ink.alpha(fg, 0.5f))
    }

    private fun ArtCanvas.stopping(t: Float, dark: Boolean) {
        val fg = if (dark) 0xFFE8EEF5 else INK
        val lanes = listOf(
            Triple("Сухо", 0xFF2E7D4F, 0.42f),
            Triple("Мокро", 0xFF2F6DB5, 0.62f),
            Triple("Лід", 0xFF7A5CCB, 0.96f),
        )
        val brakeAt = 0.18f
        lanes.forEachIndexed { i, (label, color, stop) ->
            val y = 52f + i * 66f
            val surface = when (i) {
                0 -> linear(0f, y - 22f, 0f, y + 22f, 0xFF5F6874, 0xFF535C68)
                1 -> linear(0f, y - 22f, 0f, y + 22f, 0xFF4B5868, 0xFF3F4B5B)
                else -> linear(16f, 0f, 342f, 0f, 0xFFCFEAF6, 0xFFE9F6FC, 0xFFBFE1F1)
            }
            rect(16f, y - 22f, 342f, y + 22f, fill(surface), radius = 12f)
            when (i) {
                1 -> for (j in 0..5) oval(70f + j * 46f, y - 8f + (j % 2) * 9f, 100f + j * 46f, y - 3f + (j % 2) * 9f, fill(0xFF9CC3EA, 0.35f))
                2 -> for (j in 0..6) line(60f + j * 42f, y - 14f + (j % 3) * 8f, 84f + j * 42f, y - 10f + (j % 3) * 8f, stroke(0xFFFFFFFF, 2f, alpha = 0.8f))
            }
            text(label, 316f, y - 30f, 9f, fg)
            val travel = 300f * stop
            val tt = ((t - brakeAt) / (0.8f - brakeAt)).coerceIn(0f, 1f)
            val k = 1f - (1f - tt) * (1f - tt)
            val x = if (t < brakeAt) 26f + kf(t, f(0f, brakeAt), f(-40f, 0f)) else 26f + (travel - 30f) * k
            // Brake point and skid marks.
            line(56f, y - 22f, 56f, y + 22f, stroke(CALIPER, 2.5f, Cap.BUTT, dash = floatArrayOf(5f, 4f)))
            if (t > brakeAt && x > 56f) {
                val skid = if (i == 2) 0xFFFFFFFF else 0xFF000000
                line(56f, y - 7f, x + 6f, y - 7f, stroke(skid, 3f, alpha = 0.3f))
                line(56f, y + 7f, x + 6f, y + 7f, stroke(skid, 3f, alpha = 0.3f))
            }
            layer {
                translate(x + 22f, y)
                rotate(90f)
                scale(3.4f)
                Street.topCar(this, color)
            }
        }
    }

    private fun ArtCanvas.regen(t: Float) {
        val charging = t in 0.5f..0.95f
        val roadShift = kf(t, f(0f, .5f, 1f), f(0f, 260f, 380f))
        rect(0f, 216f, GRID_W, 250f, fill(0xFF0A1E1A))
        var x = -roadShift % 52f
        while (x < GRID_W) { rect(x, 230f, x + 26f, 233f, fill(0xFFFFFFFF, 0.4f), radius = 1.5f); x += 52f }
        val spin = kf(t, f(0f, .5f, 1f), f(0f, 1000f, 1440f))
        evSide(spin)
        // X-ray cut-out: battery in the floor and the rear motor.
        rect(108f, 172f, 254f, 190f, fill(0xFF0E2A22, 0.9f), radius = 5f)
        val level = kf(t, f(0f, .5f, .95f, 1f), f(.78f, .7f, .8f, .78f))
        battery(108f, 172f, 146f, 18f, level)
        circle(284f, 194f, 11f, fill(0xFF14B8A6))
        circle(284f, 194f, 11f, stroke(0xFFB2F5EA, 1.5f))
        // Energy flow: amber out to the motor, mint back to the battery.
        val dash = (t * 7.5f % 1f) * 12f
        val flow = ArtPath().moveTo(254f, 181f).quadTo(270f, 181f, 274f, 190f)
        path(flow, stroke(if (charging) MINT else AMBER, 4.5f, dash = floatArrayOf(6f, 6f), phase = if (charging) dash else 12f - dash))
        // Gauge: power (right, amber) vs. charge (left, mint).
        rect(14f, 14f, 132f, 98f, fill(0xFFFFFFFF, 0.08f), radius = 16f)
        val gx = 73f; val gy = 72f
        arc(gx - 40f, gy - 40f, gx + 40f, gy + 40f, 180f, 90f, stroke(MINT, 7f))
        arc(gx - 40f, gy - 40f, gx + 40f, gy + 40f, 270f, 90f, stroke(AMBER, 7f))
        val needle = kf(t, f(0f, .4f, .5f, .92f, 1f), f(55f, 55f, -50f, -50f, 55f))
        layer {
            rotate(needle, gx, gy)
            path(ArtPath.svg("M${gx - 2.2f} $gy L$gx ${gy - 37f} L${gx + 2.2f} $gy Z"), fill(0xFFFFFFFF))
        }
        circle(gx, gy, 4.5f, fill(0xFFFFFFFF))
        text(if (charging) "ЗАРЯД" else "ТЯГА", gx, gy + 16f, 9f, if (charging) MINT else AMBER)
    }

    private fun ArtCanvas.charging(t: Float) {
        // Charging post with a screen and a glowing ring.
        oval(20f, 206f, 100f, 216f, fill(0xFF000000, 0.35f))
        rect(30f, 50f, 88f, 210f, fill(linear(30f, 0f, 88f, 0f, 0xFF3A4A5C, 0xFF243140)), radius = 14f)
        rect(40f, 64f, 78f, 98f, fill(0xFF0E1A16), radius = 6f)
        val level = kf(t, f(0f, .1f, .85f, 1f), f(.2f, .2f, .8f, .8f))
        text("${(level * 100).toInt()}%", 59f, 81f, 12f, BATTERY)
        circle(59f, 128f, 13f, stroke(Ink.alpha(MINT, 0.4f + 0.6f * ((sin(t * 2 * PI.toFloat() * 4) + 1) / 2)), 3f))
        bolt(59f, 119f, 0.55f, MINT)
        // Cable with current flowing, into the car's port.
        val cable = ArtPath().moveTo(88f, 160f).cubicTo(150f, 160f, 130f, 214f, 196f, 204f)
        path(cable, stroke(0xFF0E1218, 9f))
        path(cable, stroke(MINT, 3f, dash = floatArrayOf(8f, 10f), phase = -t * 72f))
        rect(190f, 196f, 204f, 212f, fill(0xFF3A4A5C), radius = 3f)
        // Big battery gauge.
        val bx = 172f; val by = 92f
        rect(bx, by, bx + 150f, by + 76f, fill(0xFF173229), radius = 16f)
        rect(bx, by, bx + 150f, by + 76f, stroke(BATTERY, 3f), radius = 16f)
        rect(bx + 150f, by + 26f, bx + 159f, by + 50f, fill(BATTERY), radius = 3f)
        rect(bx + 8f, by + 8f, bx + 8f + 134f * level, by + 68f, fill(linear(0f, by, 0f, by + 76f, 0xFF6BE58A, BATTERY)), radius = 9f)
        for (i in 1..4) line(bx + 150f * i / 5, by + 6f, bx + 150f * i / 5, by + 70f, stroke(0xFF0E1A16, 1.2f, alpha = 0.35f))
        bolt(bx + 75f, by + 16f, 1.4f, AMBER)
        text("20%", bx + 30f, by - 14f, 10f, 0xFF9FB3AC)
        text("80%", bx + 120f, by - 14f, 10f, 0xFF9FB3AC)
    }

    private fun ArtCanvas.highVoltage(t: Float) {
        layer {
            translate(0f, -8f)
            evSide(0f, xray = true)
            battery(104f, 170f, 162f, 18f, 0.85f)
            // Orange high-voltage cables to the motor, with a slow pulse.
            val hv = ArtPath().moveTo(266f, 179f).cubicTo(292f, 179f, 268f, 194f, 284f, 194f)
            path(hv, stroke(HV, 6f))
            path(hv, stroke(0xFFFFE0B8, 2f, dash = floatArrayOf(4f, 14f), phase = -t * 90f))
            circle(284f, 194f, 14f, fill(0xFF243140))
            circle(284f, 194f, 14f, stroke(HV, 2f))
            circle(100f, 194f, 22f, stroke(0xFFFFFFFF, 1.4f, alpha = 0.4f))
            circle(284f, 194f, 22f, stroke(0xFFFFFFFF, 1.4f, alpha = 0.4f))
        }
        // Warning triangle.
        path(ArtPath.roundedPolygon(6f, 179f, 14f, 218f, 82f, 140f, 82f), fill(AMBER))
        path(ArtPath.roundedPolygon(4f, 179f, 24f, 209f, 77f, 149f, 77f), stroke(INK, 3f))
        bolt(179f, 38f, 1.1f, INK)
    }
}
