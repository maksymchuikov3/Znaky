package com.znaky.core.art

/**
 * A resolution-independent path: absolute move/line/quad/cubic/close commands.
 * Build it in code or parse SVG path data with [ArtPath.svg]. Adapters may cache their
 * native path in [native], so static art is converted only once.
 */
class ArtPath {
    internal val ops = ArrayList<Float>(64)
    internal val verbs = ArrayList<Byte>(16)
    @JvmField var native: Any? = null

    fun moveTo(x: Float, y: Float) = apply { verbs += MOVE; ops += x; ops += y; lx = x; ly = y; sx = x; sy = y }
    fun lineTo(x: Float, y: Float) = apply { verbs += LINE; ops += x; ops += y; lx = x; ly = y }
    fun quadTo(x1: Float, y1: Float, x: Float, y: Float) = apply { verbs += QUAD; ops += x1; ops += y1; ops += x; ops += y; lx = x; ly = y }
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        apply { verbs += CUBIC; ops += x1; ops += y1; ops += x2; ops += y2; ops += x; ops += y; lx = x; ly = y }
    fun close() = apply { verbs += CLOSE; lx = sx; ly = sy }

    private var lx = 0f; private var ly = 0f; private var sx = 0f; private var sy = 0f

    /** Replays the commands into [sink]. */
    fun replay(sink: PathSink) {
        var i = 0
        for (v in verbs) when (v) {
            MOVE -> { sink.moveTo(ops[i], ops[i + 1]); i += 2 }
            LINE -> { sink.lineTo(ops[i], ops[i + 1]); i += 2 }
            QUAD -> { sink.quadTo(ops[i], ops[i + 1], ops[i + 2], ops[i + 3]); i += 4 }
            CUBIC -> { sink.cubicTo(ops[i], ops[i + 1], ops[i + 2], ops[i + 3], ops[i + 4], ops[i + 5]); i += 6 }
            CLOSE -> sink.close()
        }
    }

    interface PathSink {
        fun moveTo(x: Float, y: Float)
        fun lineTo(x: Float, y: Float)
        fun quadTo(x1: Float, y1: Float, x: Float, y: Float)
        fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float)
        fun close()
    }

    companion object {
        private const val MOVE: Byte = 0
        private const val LINE: Byte = 1
        private const val QUAD: Byte = 2
        private const val CUBIC: Byte = 3
        private const val CLOSE: Byte = 4

        /** Parses SVG path data with commands M L H V Q T C S A Z (absolute and relative). */
        fun svg(d: String): ArtPath = SvgPathParser(d).parse()

        fun roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float): ArtPath {
            val k = 0.5523f * rad
            return ArtPath().moveTo(l + rad, t).lineTo(r - rad, t).cubicTo(r - rad + k, t, r, t + rad - k, r, t + rad)
                .lineTo(r, b - rad).cubicTo(r, b - rad + k, r - rad + k, b, r - rad, b)
                .lineTo(l + rad, b).cubicTo(l + rad - k, b, l, b - rad + k, l, b - rad)
                .lineTo(l, t + rad).cubicTo(l, t + rad - k, l + rad - k, t, l + rad, t).close()
        }

        fun circle(cx: Float, cy: Float, r: Float): ArtPath {
            val k = 0.5523f * r
            return ArtPath().moveTo(cx + r, cy).cubicTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r)
                .cubicTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy).cubicTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r)
                .cubicTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy).close()
        }

        /** A closed polygon through [pts] (x0, y0, x1, y1, …) whose corners are rounded by [rad]. */
        fun roundedPolygon(rad: Float, vararg pts: Float): ArtPath {
            val n = pts.size / 2
            val p = ArtPath()
            for (i in 0..n) {
                val c = i % n
                val px = pts[(c + n - 1) % n * 2]; val py = pts[(c + n - 1) % n * 2 + 1]
                val cx = pts[c * 2]; val cy = pts[c * 2 + 1]
                val nx = pts[(c + 1) % n * 2]; val ny = pts[(c + 1) % n * 2 + 1]
                val d1 = kotlin.math.hypot(px - cx, py - cy); val d2 = kotlin.math.hypot(nx - cx, ny - cy)
                val r1 = minOf(rad, d1 / 2) / d1; val r2 = minOf(rad, d2 / 2) / d2
                val ax = cx + (px - cx) * r1; val ay = cy + (py - cy) * r1
                val bx = cx + (nx - cx) * r2; val by = cy + (ny - cy) * r2
                if (i == 0) p.moveTo(bx, by) else { p.lineTo(ax, ay); p.quadTo(cx, cy, bx, by) }
            }
            return p.close()
        }
    }
}

private class SvgPathParser(private val s: String) {
    private var i = 0
    private val p = ArtPath()
    private var x = 0f; private var y = 0f
    private var sx = 0f; private var sy = 0f
    private var cx = 0f; private var cy = 0f // last control point for S/T
    private var last = ' '

    fun parse(): ArtPath {
        var cmd = ' '
        while (true) {
            skip()
            if (i >= s.length) break
            val ch = s[i]
            if (ch.isLetter()) { cmd = ch; i++ } else if (cmd == 'M') cmd = 'L' else if (cmd == 'm') cmd = 'l'
            val rel = cmd.isLowerCase()
            val ox = if (rel) x else 0f; val oy = if (rel) y else 0f
            when (cmd.uppercaseChar()) {
                'M' -> { x = ox + num(); y = oy + num(); sx = x; sy = y; p.moveTo(x, y) }
                'L' -> { x = ox + num(); y = oy + num(); p.lineTo(x, y) }
                'H' -> { x = ox + num(); p.lineTo(x, y) }
                'V' -> { y = oy + num(); p.lineTo(x, y) }
                'Q' -> { val x1 = ox + num(); val y1 = oy + num(); x = ox + num(); y = oy + num(); p.quadTo(x1, y1, x, y); cx = x1; cy = y1 }
                'T' -> {
                    val x1 = if (last.uppercaseChar() in "QT") 2 * x - cx else x
                    val y1 = if (last.uppercaseChar() in "QT") 2 * y - cy else y
                    x = ox + num(); y = oy + num(); p.quadTo(x1, y1, x, y); cx = x1; cy = y1
                }
                'C' -> {
                    val x1 = ox + num(); val y1 = oy + num(); val x2 = ox + num(); val y2 = oy + num()
                    x = ox + num(); y = oy + num(); p.cubicTo(x1, y1, x2, y2, x, y); cx = x2; cy = y2
                }
                'S' -> {
                    val x1 = if (last.uppercaseChar() in "CS") 2 * x - cx else x
                    val y1 = if (last.uppercaseChar() in "CS") 2 * y - cy else y
                    val x2 = ox + num(); val y2 = oy + num()
                    x = ox + num(); y = oy + num(); p.cubicTo(x1, y1, x2, y2, x, y); cx = x2; cy = y2
                }
                'A' -> {
                    val rx = num(); val ry = num(); val rot = num(); val large = flag(); val sweep = flag()
                    val ex = ox + num(); val ey = oy + num()
                    arc(rx, ry, rot, large, sweep, ex, ey); x = ex; y = ey
                }
                'Z' -> { p.close(); x = sx; y = sy }
                else -> error("Unsupported path command '$cmd' in: $s")
            }
            last = cmd
        }
        return p
    }

    private fun skip() { while (i < s.length && (s[i].isWhitespace() || s[i] == ',')) i++ }
    private fun flag(): Boolean { skip(); return s[i++] == '1' }
    private fun num(): Float {
        skip()
        val start = i
        if (i < s.length && (s[i] == '-' || s[i] == '+')) i++
        var dot = false
        while (i < s.length && (s[i].isDigit() || (s[i] == '.' && !dot))) { if (s[i] == '.') dot = true; i++ }
        if (i < s.length && (s[i] == 'e' || s[i] == 'E')) { i++; if (s[i] == '-' || s[i] == '+') i++; while (i < s.length && s[i].isDigit()) i++ }
        return s.substring(start, i).toFloat()
    }

    /** Endpoint arc → cubic Béziers (SVG spec F.6). */
    private fun arc(rxIn: Float, ryIn: Float, rotDeg: Float, large: Boolean, sweep: Boolean, ex: Float, ey: Float) {
        var rx = kotlin.math.abs(rxIn); var ry = kotlin.math.abs(ryIn)
        if (rx == 0f || ry == 0f) { p.lineTo(ex, ey); return }
        val phi = Math.toRadians(rotDeg.toDouble())
        val cosP = kotlin.math.cos(phi); val sinP = kotlin.math.sin(phi)
        val dx = (x - ex) / 2.0; val dy = (y - ey) / 2.0
        val x1p = cosP * dx + sinP * dy; val y1p = -sinP * dx + cosP * dy
        var lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
        if (lambda > 1) { val s = kotlin.math.sqrt(lambda); rx *= s.toFloat(); ry *= s.toFloat() }
        val num = rx.toDouble() * rx * ry * ry - rx.toDouble() * rx * y1p * y1p - ry.toDouble() * ry * x1p * x1p
        val den = rx.toDouble() * rx * y1p * y1p + ry.toDouble() * ry * x1p * x1p
        var coef = kotlin.math.sqrt(kotlin.math.max(0.0, num / den))
        if (large == sweep) coef = -coef
        val cxp = coef * rx * y1p / ry; val cyp = -coef * ry * x1p / rx
        val ccx = cosP * cxp - sinP * cyp + (x + ex) / 2.0
        val ccy = sinP * cxp + cosP * cyp + (y + ey) / 2.0
        fun ang(ux: Double, uy: Double, vx: Double, vy: Double): Double {
            val a = kotlin.math.atan2(ux * vy - uy * vx, ux * vx + uy * vy); return a
        }
        val th1 = ang(1.0, 0.0, (x1p - cxp) / rx, (y1p - cyp) / ry)
        var dth = ang((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
        if (!sweep && dth > 0) dth -= 2 * Math.PI else if (sweep && dth < 0) dth += 2 * Math.PI
        val segs = kotlin.math.ceil(kotlin.math.abs(dth) / (Math.PI / 2)).toInt().coerceAtLeast(1)
        val delta = dth / segs
        val t = 4.0 / 3.0 * kotlin.math.tan(delta / 4)
        var th = th1
        repeat(segs) {
            val c1 = kotlin.math.cos(th); val s1 = kotlin.math.sin(th)
            val c2 = kotlin.math.cos(th + delta); val s2 = kotlin.math.sin(th + delta)
            fun px(cx: Double, sx: Double) = (ccx + rx * cx * cosP - ry * sx * sinP).toFloat()
            fun py(cx: Double, sx: Double) = (ccy + rx * cx * sinP + ry * sx * cosP).toFloat()
            p.cubicTo(px(c1 - t * s1, s1 + t * c1), py(c1 - t * s1, s1 + t * c1), px(c2 + t * s2, s2 - t * c2), py(c2 + t * s2, s2 - t * c2), px(c2, s2), py(c2, s2))
            th += delta
        }
    }
}
