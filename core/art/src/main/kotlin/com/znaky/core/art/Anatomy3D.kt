package com.znaky.core.art

import com.znaky.core.art.Car3D.Part
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D models for «Будова авто»: a car with an engine, an electric car, a motorcycle and a small motor boat,
 * each part a tagged shape so it can be highlighted and pointed at with a numbered marker.
 * Units and axes are the same as [Car3D]: +x forward, y up (ground at -42), z across.
 */
object Anatomy3D {
    enum class Kind { ICE, EV, MOTO, BOAT }

    /** [anchors]: where the numbered marker of each part sits, in model units. */
    class Model(val mesh: Car3D.Mesh, val anchors: Map<String, FloatArray>)

    /** Builds the model of [kind]; [colorOf] gives each part's colour (ARGB) by id. */
    fun build(kind: Kind, colorOf: (String) -> Long): Model {
        val m = Car3D.Builder()
        val anchors = LinkedHashMap<String, FloatArray>()
        fun part(id: String, cx: Float, cy: Float, cz: Float, l: Float, h: Float, w: Float, anchor: Boolean = true) {
            m.box(cx, cy, cz, l, h, w, colorOf(id), Part.INSIDE, id)
            if (anchor && id !in anchors) anchors[id] = floatArrayOf(cx, cy + h / 2, cz)
        }
        fun anchor(id: String, x: Float, y: Float, z: Float) {
            anchors[id] = floatArrayOf(x, y, z)
        }
        when (kind) {
            Kind.ICE, Kind.EV -> {
                Car3D.shell(m, 0xFFE8EEF5, wheelTag = "tyres")
                anchor("tyres", 66f, -19f, 47f)
                anchor("lights", 107f, 5f, 33f)
                commonCarParts(::part)
                if (kind == Kind.ICE) {
                    part("engine", 76f, 2f, 0f, 32f, 22f, 38f)
                    part("oil", 76f, -12f, 0f, 24f, 6f, 28f)
                    part("gearbox", 76f, -4f, -27f, 20f, 14f, 12f)
                    part("clutch", 76f, -4f, -20f, 13f, 13f, 3f)
                    part("radiator", 101f, 0f, 0f, 3f, 20f, 60f)
                    part("airfilter", 94f, 12f, 16f, 12f, 7f, 14f)
                    part("tank", -50f, -10f, 0f, 30f, 12f, 56f)
                    part("exhaust", -86f, -14f, 14f, 18f, 7f, 10f)
                    part("exhaust", -12f, -16f, 14f, 130f, 3f, 3f, anchor = false)
                    part("electric", 88f, -16f, 22f, 10f, 8f, 6f)
                    part("electric", 20f, 6f, 36f, 120f, 1.6f, 1.6f, anchor = false)
                    part("cv", 66f, -19f, 24f, 4f, 4f, 30f)
                    part("cv", 66f, -19f, -24f, 4f, 4f, 30f, anchor = false)
                    part("kit", -90f, -2f, 0f, 14f, 8f, 30f)
                } else {
                    part("evbattery", -4f, -13f, 0f, 118f, 6f, 70f)
                    part("motor", -66f, -10f, -6f, 22f, 20f, 22f)
                    part("reducer", -66f, -12f, 13f, 14f, 14f, 10f)
                    part("inverter", -66f, 6f, -4f, 16f, 8f, 22f)
                    part("port", -84f, 12f, 46f, 8f, 8f, 2f)
                    part("hvcables", -36f, -8f, 16f, 40f, 3f, 3f)
                    part("hvcables", -60f, -2f, 8f, 3f, 14f, 3f, anchor = false)
                    part("regen", -66f, -19f, 30f, 4f, 4f, 20f)
                    part("regen", -66f, -19f, -30f, 4f, 4f, 20f, anchor = false)
                    part("cooling", 101f, 0f, 0f, 3f, 20f, 60f)
                    part("cv", 66f, -19f, 24f, 4f, 4f, 30f)
                    part("cv", 66f, -19f, -24f, 4f, 4f, 30f, anchor = false)
                    part("kit", 84f, 2f, 0f, 16f, 8f, 30f)
                }
            }
            Kind.MOTO -> buildMoto(m, ::part, anchors, colorOf)
            Kind.BOAT -> buildBoat(m, ::part, anchors, colorOf)
        }
        return Model(Car3D.Mesh(m.faces), anchors)
    }

    /** Parts every car has, in the same places for both models. */
    private fun commonCarParts(part: (String, Float, Float, Float, Float, Float, Float, Boolean) -> Unit) {
        for (x in floatArrayOf(66f, -66f)) for (z in floatArrayOf(36f, -36f)) part("brakes", x, -19f, z, 16f, 16f, 2f, x > 0 && z > 0)
        part("brakefluid", 54f, 12f, -24f, 8f, 8f, 8f, true)
        part("steering", 62f, -14f, 0f, 6f, 5f, 60f, true)
        part("steering", 26f, 20f, -16f, 3f, 16f, 16f, false)
        for (x in floatArrayOf(66f, -66f)) for (z in floatArrayOf(30f, -30f)) part("suspension", x, 0f, z, 6f, 22f, 6f, x > 0 && z < 0)
        part("battery12", 92f, 4f, 28f, 14f, 12f, 12f, true)
        part("wipers", 50f, 22f, -10f, 2f, 1.5f, 34f, true)
        part("wipers", 50f, 22f, 16f, 2f, 1.5f, 26f, false)
        part("mirrors", 48f, 24f, 46f, 6f, 6f, 6f, true)
        part("mirrors", 48f, 24f, -46f, 6f, 6f, 6f, false)
        part("safety", -6f, 10f, -22f, 4f, 22f, 3f, true)
        part("safety", -6f, 10f, 22f, 4f, 22f, 3f, false)
        part("pedals", 36f, -10f, -16f, 6f, 8f, 12f, true)
        part("dash", 34f, 18f, 0f, 10f, 8f, 70f, true)
        part("horn", 104f, -8f, -24f, 4f, 6f, 6f, true)
    }

    private fun buildMoto(
        m: Car3D.Builder,
        part: (String, Float, Float, Float, Float, Float, Float, Boolean) -> Unit,
        anchors: MutableMap<String, FloatArray>,
        colorOf: (String) -> Long,
    ) {
        val frame = 0xFF39424E
        val chrome = 0xFFB9C2CE
        // Wheels: both sides get a rim.
        for (x in floatArrayOf(68f, -66f)) {
            m.wheel(x, -12f, 0.01f, 30f, 10f, "motoTyres")
            m.wheel(x, -12f, -0.01f, 30f, 10f, "motoTyres")
        }
        anchors["motoTyres"] = floatArrayOf(-66f, 18f, 0f)
        // Frame, swingarm, seat and exhaust are the body.
        m.box(16f, 12f, 0f, 76f, 5f, 8f, frame, Part.BODY)
        m.box(-34f, -11f, 7f, 62f, 4f, 3f, frame, Part.BODY)
        m.box(-34f, -11f, -7f, 62f, 4f, 3f, frame, Part.BODY)
        m.extrude(floatArrayOf(-52f, 20f, 4f, 20f, 4f, 26f, -46f, 28f, -54f, 25f), 11f, 0xFF20252C, Part.BODY) { 0xFF20252C to Part.BODY }
        m.box(-30f, -22f, 12f, 54f, 4f, 4f, chrome, Part.BODY)
        m.box(62f, 34f, 0f, 4f, 8f, 10f, 0xFFFFF3B0, Part.LIGHT)
        m.box(-58f, 24f, 0f, 3f, 4f, 10f, 0xFFFF5A5F, Part.LIGHT)
        // Fork: two slanted legs from the steering head to the front axle.
        for (z in floatArrayOf(7f, -7f)) {
            m.extrude(floatArrayOf(66f, -12f, 70f, -12f, 62f, 32f, 58f, 32f), 1.8f, colorOf("fork"), Part.INSIDE, "fork", zOffset = z) { colorOf("fork") to Part.INSIDE }
        }
        anchors["fork"] = floatArrayOf(66f, 8f, 8f)
        part("controls", 56f, 40f, 0f, 5f, 4f, 62f, true)
        // Tank: a rounded side profile.
        m.extrude(floatArrayOf(4f, 18f, 42f, 18f, 40f, 32f, 30f, 36f, 10f, 34f), 13f, colorOf("motoTank"), Part.INSIDE, "motoTank") { colorOf("motoTank") to Part.INSIDE }
        anchors["motoTank"] = floatArrayOf(24f, 36f, 0f)
        part("motoEngine", 6f, -8f, 0f, 30f, 26f, 22f, true)
        part("wetClutch", 12f, -8f, 13f, 14f, 14f, 4f, true)
        part("seqBox", -16f, -10f, 0f, 18f, 18f, 18f, true)
        part("chain", -30f, -12f, 9f, 62f, 3f, 2f, true)
        part("motoBrakes", 68f, -12f, -6f, 20f, 20f, 1.5f, true)
        part("motoBrakes", -66f, -12f, -6f, 15f, 15f, 1.5f, false)
        // Helmet: a half dome resting on the seat.
        val dome = FloatArray(2 * 13)
        for (i in 0..12) {
            val a = (PI * i / 12).toFloat()
            dome[i * 2] = -26f + 10f * cos(a)
            dome[i * 2 + 1] = 28f + 11f * sin(a)
        }
        m.extrude(dome, 8f, colorOf("helmet"), Part.INSIDE, "helmet") { colorOf("helmet") to Part.INSIDE }
        anchors["helmet"] = floatArrayOf(-26f, 39f, 0f)
        // Rear shock: a slanted strut from the swingarm to the frame under the seat.
        m.extrude(floatArrayOf(-28f, -10f, -24f, -10f, -14f, 16f, -18f, 16f), 2.2f, colorOf("rearShock"), Part.INSIDE, "rearShock") { colorOf("rearShock") to Part.INSIDE }
        anchors["rearShock"] = floatArrayOf(-21f, 4f, 3f)
        part("motoLights", 64f, 34f, 0f, 3f, 9f, 11f, true)
        part("motoLights", -59f, 24f, 0f, 3f, 5f, 11f, false)
        part("motoMirrors", 52f, 48f, 22f, 3f, 6f, 8f, true)
        part("motoMirrors", 52f, 48f, -22f, 3f, 6f, 8f, false)
        part("motoMirrors", 54f, 44f, 22f, 1.5f, 8f, 1.5f, false)
        part("motoMirrors", 54f, 44f, -22f, 1.5f, 8f, 1.5f, false)
        part("motoRadiator", 34f, -2f, 0f, 3f, 22f, 26f, true)
        part("motoExhaust", -50f, -16f, 12f, 18f, 7f, 7f, true)
        part("motoBattery", -24f, 12f, 0f, 10f, 8f, 10f, true)
        part("stand", -6f, -30f, -12f, 3f, 22f, 3f, true)
        part("pillion", -40f, 31f, 0f, 22f, 3f, 20f, true)
        part("pillion", -36f, -6f, 14f, 6f, 2f, 6f, false)
        part("pillion", -36f, -6f, -14f, 6f, 2f, 6f, false)
    }

    /** Hull half-beam, chine and sheer at station [x] (stern -96 … bow 108). */
    private class Station(val x: Float, val beam: Float, val chineZ: Float, val chineY: Float, val keelY: Float, val sheer: Float)

    private fun station(x: Float): Station {
        // 0 at the transom, 1 at the stem.
        val t = ((x + 96f) / 204f).coerceIn(0f, 1f)
        val bow = ((t - 0.55f) / 0.45f).coerceIn(0f, 1f)
        val beam = 34f * (1f - bow * bow * 0.97f)
        val keel = -30f + 22f * bow * bow
        val chineY = -22f + 16f * bow * bow
        val sheer = 4f + 8f * bow * bow
        return Station(x, beam, beam * 0.78f, chineY, keel, sheer)
    }

    private fun buildBoat(
        m: Car3D.Builder,
        part: (String, Float, Float, Float, Float, Float, Float, Boolean) -> Unit,
        anchors: MutableMap<String, FloatArray>,
        colorOf: (String) -> Long,
    ) {
        val hullColor = 0xFFF1F4F8
        val inner = 0xFFCAD3DE
        val stripe = 0xFF1C8DB8
        val xs = floatArrayOf(-96f, -70f, -40f, -10f, 20f, 46f, 66f, 82f, 94f, 102f, 108f)
        val st = xs.map { station(it) }
        // Section points of one side from keel to gunwale; z mirrored by [side].
        fun section(s: Station, side: Float) = listOf(
            floatArrayOf(s.x, s.keelY, 0f),
            floatArrayOf(s.x, s.chineY, side * s.chineZ),
            floatArrayOf(s.x, s.sheer - 6f, side * s.beam),
            floatArrayOf(s.x, s.sheer, side * s.beam * 0.98f),
        )
        fun quad(a: FloatArray, b: FloatArray, c: FloatArray, d: FloatArray, color: Long, outward: FloatArray, part: Part, tag: String?) {
            val ux = b[0] - a[0]; val uy = b[1] - a[1]; val uz = b[2] - a[2]
            val vx = d[0] - a[0]; val vy = d[1] - a[1]; val vz = d[2] - a[2]
            var nx = uy * vz - uz * vy; var ny = uz * vx - ux * vz; var nz = ux * vy - uy * vx
            val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-4f)
            nx /= len; ny /= len; nz /= len
            val flip = nx * outward[0] + ny * outward[1] + nz * outward[2] < 0
            val pts = if (flip) listOf(a, d, c, b) else listOf(a, b, c, d)
            val n = if (flip) floatArrayOf(-nx, -ny, -nz) else floatArrayOf(nx, ny, nz)
            m.face(pts, n, color, part, tag)
        }
        for (side in floatArrayOf(1f, -1f)) {
            for (i in 0 until st.size - 1) {
                val a = section(st[i], side); val b = section(st[i + 1], side)
                for (k in 0 until 3) {
                    val color = if (k == 2) stripe else hullColor
                    // Outward: away from the hull's centre line, a little downward below the chine.
                    val out = floatArrayOf(0f, if (k == 0) -1f else -0.2f, side)
                    quad(a[k], b[k], b[k + 1], a[k + 1], color, out, Part.BODY, "hull")
                    // The inside of the topsides, seen from above through the open cockpit.
                    if (k > 0) quad(a[k], b[k], b[k + 1], a[k + 1], inner, floatArrayOf(-out[0], -out[1], -out[2]).also { it[1] = 0.3f; it[2] = -side }, Part.TRIM, null)
                }
            }
        }
        anchors["hull"] = floatArrayOf(40f, -20f, 30f)
        // Cockpit floor and foredeck.
        m.box(-28f, -15f, 0f, 136f, 1.5f, 54f, 0xFFB7C4D2, Part.TRIM)
        val fore = st.filter { it.x >= 66f }
        m.face(fore.map { floatArrayOf(it.x, it.sheer, it.beam * 0.98f) } + fore.reversed().map { floatArrayOf(it.x, it.sheer, -it.beam * 0.98f) },
            floatArrayOf(0f, 1f, 0f), 0xFFE3E9F0, Part.BODY)
        // Transom: the flat stern plate carrying the motor.
        part("transom", -95f, -12f, 0f, 3f, 32f, 62f, true)
        anchors["transom"] = floatArrayOf(-95f, -2f, -24f)
        // Outboard motor: cowling, leg and anti-cavitation plate; the propeller under it.
        part("outboard", -104f, 16f, 0f, 18f, 20f, 15f, true)
        part("outboard", -100f, 4f, 0f, 8f, 6f, 10f, false)
        part("outboard", -103f, -14f, 0f, 6f, 36f, 5f, false)
        part("outboard", -104f, -30f, 0f, 14f, 1.5f, 10f, false)
        part("propeller", -104f, -36f, 0f, 9f, 5f, 4f, true)
        part("propeller", -108f, -36f, 0f, 1.5f, 16f, 3f, false)
        part("propeller", -108f, -36f, 0f, 1.5f, 3f, 16f, false)
        // Centre console with a small windscreen.
        part("console", 6f, -1f, -10f, 20f, 26f, 22f, true)
        m.extrude(floatArrayOf(14f, 12f, 18f, 12f, 12f, 22f, 9f, 22f), 11f, 0xFF9FC7E0, Part.GLASS, zOffset = -10f) { 0xFF9FC7E0 to Part.GLASS }
        part("killcord", 0f, 14f, -18f, 3f, 3f, 3f, true)
        part("killcord", -2f, 13f, -18f, 3f, 1f, 1f, false)
        part("throttle", 2f, 10f, 3f, 4f, 10f, 3f, true)
        part("boatHorn", 14f, 13f, -2f, 4f, 3f, 5f, true)
        part("navlights", 104f, 12f, 0f, 4f, 3f, 6f, true)
        part("navlights", -82f, 18f, -26f, 1.5f, 36f, 1.5f, false)
        part("navlights", -82f, 37f, -26f, 4f, 4f, 4f, false)
        part("fueltank", -46f, -21f, 0f, 30f, 8f, 30f, true)
        part("boatBattery", -78f, -9f, 20f, 12f, 10f, 10f, true)
        part("bilge", -84f, -25f, 0f, 8f, 4f, 8f, true)
        // A bench seat with the life jackets stowed under it.
        m.box(-58f, -6f, 0f, 16f, 4f, 58f, 0xFFDDE3EA, Part.TRIM)
        part("vests", -58f, -11f, 12f, 12f, 6f, 16f, true)
        part("vests", -58f, -11f, -10f, 12f, 6f, 14f, false)
        // Life ring hung on the side: eight short segments around a circle.
        for (i in 0 until 8) {
            val a = (2 * PI * i / 8).toFloat()
            part("lifebuoy", -34f + 8f * cos(a), 0f + 8f * sin(a), -31f, 4f, 4f, 3f, false)
        }
        anchors["lifebuoy"] = floatArrayOf(-34f, 9f, -31f)
        part("anchor", 84f, 0f, 0f, 10f, 4f, 12f, true)
        part("anchor", 84f, 0f, 0f, 2f, 4f, 2f, false)
        part("paddles", -14f, -10f, 26f, 64f, 2f, 3f, true)
        part("paddles", 22f, -10f, 26f, 10f, 2f, 6f, false)
        part("fireext", 20f, -7f, -20f, 5f, 14f, 5f, true)
        // Registration number on the bow, standing just off the topside.
        val sn = station(66f)
        part("idnumber", 66f, sn.sheer - 9f, sn.beam * 0.95f + 1f, 28f, 5f, 1f, true)
        part("idnumber", 66f, sn.sheer - 9f, -sn.beam * 0.95f - 1f, 28f, 5f, 1f, false)
    }
}
