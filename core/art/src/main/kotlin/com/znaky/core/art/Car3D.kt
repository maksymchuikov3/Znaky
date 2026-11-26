package com.znaky.core.art

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * A small real-time 3D car for the «3D-машина» screen. Model units: x along the car
 * (front is +x), y up, z across. The body and cabin are extruded side profiles, so the
 * car has a sloped bonnet, windscreen and boot instead of stacked boxes.
 */
object Car3D {
    enum class Kind { ENGINE, ELECTRIC }
    enum class View { SOLID, X_RAY, SCHEME }
    enum class Part { BODY, GLASS, WHEEL, LIGHT, INSIDE, TRIM }

    const val ENGINE = 0xFFD9822B
    const val GEARBOX = 0xFF6D4FC2
    const val BATTERY = 0xFF34D058
    const val MOTOR = 0xFF14B8A6
    const val FUEL = 0xFFE0A400

    private const val TYRE = 0xFF15191F
    private const val RIM = 0xFFD5DCE4
    private const val GLASS = 0xFF3F5873
    private const val HEAD = 0xFFFFF3B0
    private const val TAIL = 0xFFFF5A5F
    private const val TRIM = 0xFF2B3542

    /** [tag] names the anatomy part (e.g. "engine") a face belongs to, for highlighting. */
    class Face(
        val xs: FloatArray, val ys: FloatArray, val zs: FloatArray,
        val nx: Float, val ny: Float, val nz: Float,
        val color: Long, val part: Part, val tag: String? = null,
    )

    class Mesh(val faces: List<Face>)

    internal class Builder {
        val faces = ArrayList<Face>(400)

        fun face(p: List<FloatArray>, n: FloatArray, color: Long, part: Part, tag: String? = null) {
            faces += Face(FloatArray(p.size) { p[it][0] }, FloatArray(p.size) { p[it][1] }, FloatArray(p.size) { p[it][2] }, n[0], n[1], n[2], color, part, tag)
        }

        fun box(cx: Float, cy: Float, cz: Float, l: Float, h: Float, w: Float, color: Long, part: Part, tag: String? = null) {
            val x0 = cx - l / 2; val x1 = cx + l / 2; val y0 = cy - h / 2; val y1 = cy + h / 2; val z0 = cz - w / 2; val z1 = cz + w / 2
            fun v(x: Float, y: Float, z: Float) = floatArrayOf(x, y, z)
            face(listOf(v(x1, y0, z0), v(x1, y1, z0), v(x1, y1, z1), v(x1, y0, z1)), floatArrayOf(1f, 0f, 0f), color, part, tag)
            face(listOf(v(x0, y0, z0), v(x0, y0, z1), v(x0, y1, z1), v(x0, y1, z0)), floatArrayOf(-1f, 0f, 0f), color, part, tag)
            face(listOf(v(x0, y1, z0), v(x0, y1, z1), v(x1, y1, z1), v(x1, y1, z0)), floatArrayOf(0f, 1f, 0f), color, part, tag)
            face(listOf(v(x0, y0, z0), v(x1, y0, z0), v(x1, y0, z1), v(x0, y0, z1)), floatArrayOf(0f, -1f, 0f), color, part, tag)
            face(listOf(v(x0, y0, z1), v(x1, y0, z1), v(x1, y1, z1), v(x0, y1, z1)), floatArrayOf(0f, 0f, 1f), color, part, tag)
            face(listOf(v(x0, y0, z0), v(x0, y1, z0), v(x1, y1, z0), v(x1, y0, z0)), floatArrayOf(0f, 0f, -1f), color, part, tag)
        }

        /**
         * Extrudes the side profile [pts] (x0, y0, x1, y1 …, counter-clockwise) across z = ±[halfW].
         * [edgeColor] colours the strip after vertex i (windscreen, roof…); [side] the two side caps.
         */
        fun extrude(pts: FloatArray, halfW: Float, side: Long, sidePart: Part, tag: String? = null, zOffset: Float = 0f, edgeColor: (Int) -> Pair<Long, Part>) {
            val n = pts.size / 2
            val zf = zOffset + halfW
            val zb = zOffset - halfW
            face(List(n) { floatArrayOf(pts[it * 2], pts[it * 2 + 1], zf) }, floatArrayOf(0f, 0f, 1f), side, sidePart, tag)
            face(List(n) { floatArrayOf(pts[(n - 1 - it) * 2], pts[(n - 1 - it) * 2 + 1], zb) }, floatArrayOf(0f, 0f, -1f), side, sidePart, tag)
            for (i in 0 until n) {
                val j = (i + 1) % n
                val ax = pts[i * 2]; val ay = pts[i * 2 + 1]; val bx = pts[j * 2]; val by = pts[j * 2 + 1]
                val len = hypot(bx - ax, by - ay)
                if (len < 0.01f) continue
                val (color, part) = edgeColor(i)
                face(
                    listOf(floatArrayOf(ax, ay, zb), floatArrayOf(bx, by, zb), floatArrayOf(bx, by, zf), floatArrayOf(ax, ay, zf)),
                    floatArrayOf((by - ay) / len, -(bx - ax) / len, 0f), color, part, tag,
                )
            }
        }

        fun wheel(cx: Float, cy: Float, cz: Float, r: Float, width: Float, tag: String? = null) {
            val n = 22
            val sign = if (cz > 0) 1f else -1f
            val zOut = cz + sign * width / 2; val zIn = cz - sign * width / 2
            val ring = List(n) { i -> val a = (2 * PI * i / n).toFloat(); floatArrayOf(cx + r * cos(a), cy + r * sin(a)) }
            for (i in 0 until n) {
                val a = ring[i]; val b = ring[(i + 1) % n]
                val mid = (2 * PI * (i + 0.5) / n).toFloat()
                face(listOf(floatArrayOf(a[0], a[1], zIn), floatArrayOf(b[0], b[1], zIn), floatArrayOf(b[0], b[1], zOut), floatArrayOf(a[0], a[1], zOut)),
                    floatArrayOf(cos(mid), sin(mid), 0f), if (i % 2 == 0) TYRE else 0xFF1E242C, Part.WHEEL, tag)
            }
            face(ring.map { floatArrayOf(it[0], it[1], zOut) }, floatArrayOf(0f, 0f, sign), TYRE, Part.WHEEL, tag)
            fun disc(rr: Float, dz: Float, color: Long, segs: Int = n) =
                face(List(segs) { i -> val a = (2 * PI * i / segs).toFloat(); floatArrayOf(cx + rr * cos(a), cy + rr * sin(a), zOut + sign * dz) }, floatArrayOf(0f, 0f, sign), color, Part.WHEEL, tag)
            disc(r * 0.68f, 0.4f, RIM)
            disc(r * 0.56f, 0.6f, 0xFF4D5864)
            // Five spokes over the dark centre.
            for (s in 0 until 5) {
                val a = (2 * PI * s / 5).toFloat()
                val ca = cos(a); val sa = sin(a); val px = -sa; val py = ca
                val w0 = r * 0.1f; val w1 = r * 0.07f; val r0 = r * 0.18f; val r1 = r * 0.6f
                face(listOf(
                    floatArrayOf(cx + ca * r0 + px * w0, cy + sa * r0 + py * w0, zOut + sign * 0.9f),
                    floatArrayOf(cx + ca * r1 + px * w1, cy + sa * r1 + py * w1, zOut + sign * 0.9f),
                    floatArrayOf(cx + ca * r1 - px * w1, cy + sa * r1 - py * w1, zOut + sign * 0.9f),
                    floatArrayOf(cx + ca * r0 - px * w0, cy + sa * r0 - py * w0, zOut + sign * 0.9f),
                ), floatArrayOf(0f, 0f, sign), RIM, Part.WHEEL, tag)
            }
            disc(r * 0.2f, 1.1f, 0xFFB8C2CD, 10)
        }
    }

    /** Builds the car once; [Renderer] only transforms the faces. */
    fun build(kind: Kind, bodyColor: Long = 0xFFE8EEF5): Mesh {
        val m = Builder()
        shell(m, bodyColor, wheelTag = null)
        when (kind) {
            Kind.ENGINE -> {
                m.box(70f, 2f, 0f, 46f, 28f, 58f, ENGINE, Part.INSIDE)
                m.box(32f, -6f, 0f, 28f, 18f, 30f, GEARBOX, Part.INSIDE)
                m.box(-62f, -9f, 0f, 38f, 12f, 60f, FUEL, Part.INSIDE)
            }
            Kind.ELECTRIC -> {
                m.box(0f, -11f, 0f, 150f, 8f, 80f, BATTERY, Part.INSIDE)
                m.box(-66f, -8f, 0f, 26f, 26f, 40f, MOTOR, Part.INSIDE)
            }
        }
        return Mesh(m.faces)
    }

    /** The car body, cabin, lights and wheels without anything inside. */
    internal fun shell(m: Builder, bodyColor: Long, wheelTag: String?) {
        val bonnet = Ink.shade(bodyColor, 0.97f)
        // Lower body: bumpers, bonnet, waist and boot.
        val body = floatArrayOf(
            -104f, -16f, 100f, -16f, 106f, -9f, 107f, 4f, 101f, 13f, 64f, 20f, -62f, 21f, -96f, 19f, -105f, 10f, -106f, -6f,
        )
        m.extrude(body, 47f, bodyColor, Part.BODY) { i ->
            when (i) {
                0 -> TRIM to Part.TRIM
                4 -> bonnet to Part.BODY
                else -> bodyColor to Part.BODY
            }
        }
        // Cabin: windscreen, roof, rear window. Side caps are glass with the roof rail above.
        val cabin = floatArrayOf(-64f, 21f, 62f, 21f, 22f, 46f, -40f, 47f)
        m.extrude(cabin, 40f, GLASS, Part.GLASS) { i ->
            when (i) {
                2 -> bodyColor to Part.BODY
                0 -> bodyColor to Part.BODY
                else -> GLASS to Part.GLASS
            }
        }
        // Pillars and roof rails drawn as thin plates over the glass.
        for (z in floatArrayOf(-40.4f, 40.4f)) {
            val n = if (z > 0) 1f else -1f
            fun plate(x0: Float, y0: Float, x1: Float, y1: Float, w: Float) {
                val dx = x1 - x0; val dy = y1 - y0; val len = hypot(dx, dy); val ox = -dy / len * w / 2; val oy = dx / len * w / 2
                val pts = listOf(floatArrayOf(x0 + ox, y0 + oy, z), floatArrayOf(x1 + ox, y1 + oy, z), floatArrayOf(x1 - ox, y1 - oy, z), floatArrayOf(x0 - ox, y0 - oy, z))
                m.face(if (n > 0) pts else pts.reversed(), floatArrayOf(0f, 0f, n), bodyColor, Part.BODY)
            }
            plate(-2f, 21f, 0f, 46.5f, 5f) // B-pillar
            plate(22f, 45.5f, -40f, 46.5f, 3f) // roof rail
            plate(-64f, 21.5f, -40f, 46.5f, 5f) // C-pillar
            plate(62f, 21.5f, 22f, 45.5f, 3.4f) // A-pillar
        }
        // Lights as plates on the bumpers.
        for (z in floatArrayOf(-33f, 33f)) {
            m.box(106.2f, 5f, z, 1.6f, 6f, 20f, HEAD, Part.LIGHT, "lights")
            m.box(-106.2f, 8f, z, 1.6f, 6f, 20f, TAIL, Part.LIGHT, "lights")
        }
        m.box(107.4f, -3f, 0f, 1.2f, 6f, 36f, TRIM, Part.TRIM)
        for (x in floatArrayOf(66f, -66f)) for (z in floatArrayOf(46f, -46f)) m.wheel(x, -19f, z, 23f, 18f, wheelTag)
    }

    /**
     * Draws [mesh] rotated by yaw and pitch (degrees) with a light perspective: back faces
     * culled, faces sorted far-to-near, one directional light. Scratch arrays are reused.
     */
    class Renderer(private val mesh: Mesh) {
        private val depth = FloatArray(mesh.faces.size)
        private val shade = FloatArray(mesh.faces.size)
        private val order = Array(mesh.faces.size) { it }
        private val visible = BooleanArray(mesh.faces.size)
        private val px = FloatArray(32)
        private val py = FloatArray(32)

        /**
         * Projects model point ([x], [y], [z]) like [draw] does into [out]: screen x, screen y and depth
         * (bigger = farther from the viewer).
         */
        fun project(width: Float, height: Float, yaw: Float, pitch: Float, x: Float, y: Float, z: Float, out: FloatArray) {
            val cyw = (yaw * PI / 180).toFloat(); val sy = sin(cyw); val cosY = cos(cyw)
            val cp = (pitch * PI / 180).toFloat(); val sp = sin(cp); val cosP = cos(cp)
            val k = width / 300f
            val x1 = x * cosY - z * sy
            val z1 = x * sy + z * cosY
            val y2 = y * cosP - z1 * sp
            val z2 = y * sp + z1 * cosP
            val s = 900f / (900f + z2)
            out[0] = width / 2 + x1 * k * s
            out[1] = height * 0.56f - y2 * k * s
            out[2] = z2
        }

        /**
         * [highlight] (a face tag) is drawn at full strength and the other inside parts are dimmed;
         * [shellAlpha] is how strongly the body shows in x-ray views.
         */
        fun draw(c: ArtCanvas, yaw: Float, pitch: Float, view: View, highlight: String? = null, shellAlpha: Float = 0.16f) {
            val cyw = (yaw * PI / 180).toFloat(); val sy = sin(cyw); val cosY = cos(cyw)
            val cp = (pitch * PI / 180).toFloat(); val sp = sin(cp); val cosP = cos(cp)
            val k = c.width / 300f
            val focal = 900f
            val ox = c.width / 2; val oy = c.height * 0.56f
            val xray = view != View.SOLID
            mesh.faces.forEachIndexed { i, f ->
                val hidden = !xray && f.part == Part.INSIDE
                val nx1 = f.nx * cosY - f.nz * sy
                val nz1 = f.nx * sy + f.nz * cosY
                val nz2 = f.ny * sp + nz1 * cosP
                val ny2 = f.ny * cosP - nz1 * sp
                visible[i] = !hidden && nz2 < 0.02f
                var zSum = 0f
                for (v in f.xs.indices) {
                    val z1 = f.xs[v] * sy + f.zs[v] * cosY
                    zSum += f.ys[v] * sp + z1 * cosP
                }
                depth[i] = zSum / f.xs.size
                // Key light from the upper left, a little in front of the viewer.
                val light = (-0.35f * nx1 + 0.8f * ny2 - 0.48f * nz2).coerceAtLeast(0f)
                shade[i] = 0.74f + 0.3f * light
            }
            order.sortWith { a, b -> depth[b].compareTo(depth[a]) }
            for (i in order) {
                if (!visible[i]) continue
                val f = mesh.faces[i]
                for (v in f.xs.indices) {
                    val x1 = f.xs[v] * cosY - f.zs[v] * sy
                    val z1 = f.xs[v] * sy + f.zs[v] * cosY
                    val y2 = f.ys[v] * cosP - z1 * sp
                    val z2 = f.ys[v] * sp + z1 * cosP
                    val s = focal / (focal + z2)
                    px[v] = ox + x1 * k * s
                    py[v] = oy - y2 * k * s
                }
                val dim = highlight != null && f.tag != highlight
                val alpha = when {
                    !xray -> 1f
                    f.part == Part.BODY || f.part == Part.GLASS || f.part == Part.TRIM -> shellAlpha
                    f.part == Part.WHEEL -> if (highlight != null && f.tag == highlight) 1f else maxOf(0.45f, shellAlpha)
                    dim -> 0.35f
                    else -> 1f
                }
                val lit = if (highlight != null && f.tag == highlight) 1.15f else 1f
                val color = if (f.part == Part.LIGHT) f.color else Ink.shade(f.color, (shade[i] * lit).coerceAtMost(1.2f))
                c.polygon(px, py, f.xs.size, fill(color, alpha))
                // Glass gets a soft reflection streak.
                if (f.part == Part.GLASS && !xray && f.xs.size == 4) {
                    c.polygon(px, py, 4, fill(0xFFFFFFFF, 0.05f + 0.12f * (shade[i] - 0.74f)))
                }
            }
        }
    }

    /** Ground shadow: an ellipse that flattens as the camera looks more from the side. */
    fun shadow(c: ArtCanvas, pitch: Float) {
        val k = c.width / 300f
        val squash = 0.18f + 0.6f * (abs(pitch) / 90f).coerceIn(0f, 1f)
        val cx = c.width / 2; val cy = c.height * 0.56f + 44f * k
        c.oval(cx - 136f * k, cy - 62f * k * squash, cx + 136f * k, cy + 62f * k * squash, fill(radial(cx, cy, 136f * k, 0x38000000, 0x00000000)))
    }
}
