package com.znaky.feature.car

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** What the 3D car shows. */
enum class CarKind { ENGINE, ELECTRIC }

/** 3D = solid body, X_RAY = see-through body with the parts inside, SCHEME = top-down x-ray. */
enum class CarView { SOLID, X_RAY, SCHEME }

/**
 * One flat polygon of the model. Coordinates are model units: x along the car (front is +x),
 * y up, z across. [normal] is the outward unit normal, used for culling and shading.
 */
@Immutable
internal class Face(
    val xs: FloatArray,
    val ys: FloatArray,
    val zs: FloatArray,
    val nx: Float,
    val ny: Float,
    val nz: Float,
    val color: Color,
    val part: Part,
)

internal enum class Part { BODY, GLASS, WHEEL, LIGHT, INSIDE }

@Immutable
internal class CarMesh(val faces: List<Face>)

private class MeshBuilder {
    val faces = ArrayList<Face>(160)

    fun quad(p: List<FloatArray>, n: FloatArray, color: Color, part: Part) {
        faces += Face(
            FloatArray(p.size) { p[it][0] }, FloatArray(p.size) { p[it][1] }, FloatArray(p.size) { p[it][2] },
            n[0], n[1], n[2], color, part,
        )
    }

    /** Axis-aligned box centred at (cx, cy, cz) with length [l] (x), height [h] (y) and width [w] (z). */
    fun box(cx: Float, cy: Float, cz: Float, l: Float, h: Float, w: Float, color: Color, part: Part, topColor: Color = color) {
        val x0 = cx - l / 2; val x1 = cx + l / 2
        val y0 = cy - h / 2; val y1 = cy + h / 2
        val z0 = cz - w / 2; val z1 = cz + w / 2
        fun v(x: Float, y: Float, z: Float) = floatArrayOf(x, y, z)
        quad(listOf(v(x1, y0, z0), v(x1, y1, z0), v(x1, y1, z1), v(x1, y0, z1)), floatArrayOf(1f, 0f, 0f), color, part)
        quad(listOf(v(x0, y0, z0), v(x0, y0, z1), v(x0, y1, z1), v(x0, y1, z0)), floatArrayOf(-1f, 0f, 0f), color, part)
        quad(listOf(v(x0, y1, z0), v(x0, y1, z1), v(x1, y1, z1), v(x1, y1, z0)), floatArrayOf(0f, 1f, 0f), topColor, part)
        quad(listOf(v(x0, y0, z0), v(x1, y0, z0), v(x1, y0, z1), v(x0, y0, z1)), floatArrayOf(0f, -1f, 0f), color, part)
        quad(listOf(v(x0, y0, z1), v(x1, y0, z1), v(x1, y1, z1), v(x0, y1, z1)), floatArrayOf(0f, 0f, 1f), color, part)
        quad(listOf(v(x0, y0, z0), v(x0, y1, z0), v(x1, y1, z0), v(x1, y0, z0)), floatArrayOf(0f, 0f, -1f), color, part)
    }

    /** A wheel: a short cylinder with its axis along z. */
    fun wheel(cx: Float, cy: Float, cz: Float, r: Float, width: Float, hub: Color) {
        val n = 14
        val zOut = if (cz > 0) cz + width / 2 else cz - width / 2
        val zIn = if (cz > 0) cz - width / 2 else cz + width / 2
        val sign = if (cz > 0) 1f else -1f
        val ring = List(n) { i ->
            val a = (2 * PI * i / n).toFloat()
            floatArrayOf(cx + r * cos(a), cy + r * sin(a))
        }
        for (i in 0 until n) {
            val a = ring[i]; val b = ring[(i + 1) % n]
            val mid = (2 * PI * (i + 0.5) / n).toFloat()
            quad(
                listOf(floatArrayOf(a[0], a[1], zIn), floatArrayOf(b[0], b[1], zIn), floatArrayOf(b[0], b[1], zOut), floatArrayOf(a[0], a[1], zOut)),
                floatArrayOf(cos(mid), sin(mid), 0f), Tyre, Part.WHEEL,
            )
        }
        quad(ring.map { floatArrayOf(it[0], it[1], zOut) }, floatArrayOf(0f, 0f, sign), Tyre, Part.WHEEL)
        // Rim and hub sit a hair outside the tyre wall so they always win the depth sort.
        val rim = List(n) { i -> val a = (2 * PI * i / n).toFloat(); floatArrayOf(cx + r * 0.62f * cos(a), cy + r * 0.62f * sin(a), zOut + sign * 0.6f) }
        quad(rim, floatArrayOf(0f, 0f, sign), Rim, Part.WHEEL)
        val hubRing = List(8) { i -> val a = (2 * PI * i / 8).toFloat(); floatArrayOf(cx + r * 0.3f * cos(a), cy + r * 0.3f * sin(a), zOut + sign * 1.2f) }
        quad(hubRing, floatArrayOf(0f, 0f, sign), hub, Part.WHEEL)
    }
}

private val Tyre = Color(0xFF111827)
private val Rim = Color(0xFFCBD5E1)
private val Glass = Color(0xFF8DB8E8)
private val HeadLight = Color(0xFFFFF3B0)
private val TailLight = Color(0xFFFF5A5F)

internal val EngineColor = Color(0xFFD9822B)
internal val GearboxColor = Color(0xFF6D4FC2)
internal val BatteryColor = Color(0xFF34D058)
internal val MotorColor = Color(0xFF14B8A6)
internal val FuelColor = Color(0xFFE0A400)

/** Builds the car once; the renderer only transforms these faces. Proportions follow the mockup. */
internal fun buildCarMesh(kind: CarKind, bodyColor: Color = Color(0xFFE8EEF5)): CarMesh {
    val m = MeshBuilder()
    m.box(0f, 0f, 0f, 210f, 38f, 96f, bodyColor, Part.BODY)
    m.box(-10f, 34f, 0f, 116f, 30f, 84f, Glass, Part.GLASS, topColor = bodyColor)
    // Lights are thin plates on the bumpers.
    for (z in listOf(-34f, 34f)) {
        m.box(105.8f, 6f, z, 1.2f, 8f, 18f, HeadLight, Part.LIGHT)
        m.box(-105.8f, 6f, z, 1.2f, 8f, 18f, TailLight, Part.LIGHT)
    }
    for (x in listOf(68f, -68f)) for (z in listOf(49f, -49f)) m.wheel(x, -19f, z, 23f, 16f, TailLight)
    when (kind) {
        CarKind.ENGINE -> {
            m.box(70f, 2f, 0f, 52f, 30f, 60f, EngineColor, Part.INSIDE)
            m.box(28f, -6f, 0f, 30f, 18f, 30f, GearboxColor, Part.INSIDE)
            m.box(-62f, -10f, 0f, 40f, 12f, 60f, FuelColor, Part.INSIDE)
        }
        CarKind.ELECTRIC -> {
            m.box(0f, -12f, 0f, 150f, 8f, 80f, BatteryColor, Part.INSIDE)
            m.box(-68f, -8f, 0f, 26f, 26f, 40f, MotorColor, Part.INSIDE)
        }
    }
    return CarMesh(m.faces)
}

/**
 * Draws [mesh] rotated by [yaw] and [pitch] (degrees) with a simple perspective.
 * Faces are back-face culled, sorted far-to-near (painter's algorithm) and shaded by one light.
 * Scratch arrays are reused between frames to keep the draw loop allocation-light.
 */
internal class CarRenderer(private val mesh: CarMesh) {
    private val depth = FloatArray(mesh.faces.size)
    private val shade = FloatArray(mesh.faces.size)
    private val order = Array(mesh.faces.size) { it }
    private val visible = BooleanArray(mesh.faces.size)
    private val path = Path()

    fun DrawScope.draw(yaw: Float, pitch: Float, view: CarView) {
        val cy = (yaw * PI / 180).toFloat()
        val sy = sin(cy); val cosY = cos(cy)
        val cp = (pitch * PI / 180).toFloat()
        val sp = sin(cp); val cosP = cos(cp)
        val k = size.width / 300f
        val focal = 900f
        val ox = size.width / 2
        val oy = size.height * 0.56f
        val xray = view != CarView.SOLID

        mesh.faces.forEachIndexed { i, f ->
            val hidden = !xray && f.part == Part.INSIDE
            // Rotate the normal: yaw around y, then pitch around x.
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
            // Light from the upper left and slightly in front of the viewer.
            val light = (-0.35f * nx1 + 0.8f * ny2 - 0.48f * nz2).coerceAtLeast(0f)
            shade[i] = 0.62f + 0.38f * light
        }
        order.sortWith { a, b -> depth[b].compareTo(depth[a]) }

        for (i in order) {
            if (!visible[i]) continue
            val f = mesh.faces[i]
            path.reset()
            for (v in f.xs.indices) {
                val x1 = f.xs[v] * cosY - f.zs[v] * sy
                val z1 = f.xs[v] * sy + f.zs[v] * cosY
                val y2 = f.ys[v] * cosP - z1 * sp
                val z2 = f.ys[v] * sp + z1 * cosP
                val s = focal / (focal + z2)
                val px = ox + x1 * k * s
                val py = oy - y2 * k * s
                if (v == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            val alpha = when {
                !xray -> 1f
                f.part == Part.BODY || f.part == Part.GLASS -> 0.16f
                f.part == Part.WHEEL -> 0.45f
                else -> 1f
            }
            val base = f.color
            val lit = if (f.part == Part.LIGHT) base else Color(base.red * shade[i], base.green * shade[i], base.blue * shade[i])
            drawPath(path, lit, alpha = alpha * if (f.part == Part.GLASS && !xray) 0.85f else 1f)
        }
    }
}

/** Ground shadow under the car: an ellipse that flattens as the camera looks more from the side. */
internal fun DrawScope.drawCarShadow(pitch: Float) {
    val k = size.width / 300f
    val squash = 0.18f + 0.6f * (kotlin.math.abs(pitch) / 90f).coerceIn(0f, 1f)
    val c = Offset(size.width / 2, size.height * 0.56f + 44f * k)
    drawOval(
        Color.Black.copy(alpha = 0.16f),
        topLeft = Offset(c.x - 130f * k, c.y - 60f * k * squash),
        size = androidx.compose.ui.geometry.Size(260f * k, 120f * k * squash),
    )
}
