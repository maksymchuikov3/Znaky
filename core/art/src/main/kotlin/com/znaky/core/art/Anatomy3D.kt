package com.znaky.core.art

import com.znaky.core.art.Car3D.Part
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D models for «Будова авто»: a car with an engine, an electric car and a motorcycle,
 * each part a tagged shape so it can be highlighted and pointed at with a numbered marker.
 * Units and axes are the same as [Car3D]: +x forward, y up (ground at -42), z across.
 */
object Anatomy3D {
    enum class Kind { ICE, EV, MOTO }

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
    }
}
