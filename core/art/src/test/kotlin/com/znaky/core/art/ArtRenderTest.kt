package com.znaky.core.art

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every illustration draws without errors and actually paints something. */
class ArtRenderTest {
    private fun painted(w: Float, h: Float, draw: (ArtCanvas) -> Unit): Float {
        val c = Java2DCanvas(w, h, density = 1f)
        draw(c)
        var n = 0
        for (y in 0 until c.image.height step 2) for (x in 0 until c.image.width step 2) if ((c.image.getRGB(x, y) ushr 24) > 0) n++
        return n / ((c.image.width / 2f) * (c.image.height / 2f))
    }

    @Test fun everySignInTheCatalogueIsDrawn() {
        val expected = listOf("1.1", "1.2", "1.22", "1.23", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "3.1", "3.21", "3.25", "3.29", "3.34", "3.35", "4.1", "4.2", "4.3", "5.1")
        expected.forEach { code ->
            assertTrue("no art for $code", RoadSigns.has(code))
            assertTrue("sign $code is empty", painted(120f, 120f) { RoadSigns.draw(it, code) } > 0.3f)
        }
    }

    @Test fun markingsCoverTheirTile() {
        Markings.numbers.forEach { n ->
            listOf(false, true).forEach { dark -> assertTrue(painted(160f, 96f) { Markings.draw(it, n, dark) } > 0.99f) }
        }
    }

    @Test fun streetSceneAndCarsDraw() {
        listOf(false, true).forEach { dark ->
            assertTrue(painted(300f, 300f) { c ->
                Street.crossroads(c, dark, zebra = true)
                Street.car(c, 54.5f, 80f, 0f, 0xFF2F6DB5)
                Street.trafficLight(c, 62f, 63f, 2)
                Street.mainRoadSign(c, 64f, 72f)
            } > 0.99f)
        }
    }

    @Test fun officerDrawsInBothPoses() {
        assertTrue(painted(70f, 110f) { Officer.draw(it, armsOut = true, t = 0f) } > 0.2f)
        assertTrue(painted(70f, 110f) { Officer.draw(it, armsOut = false, t = 0.7f) } > 0.2f)
    }

    @Test fun carScenesDrawAcrossTheLoop() {
        CarScenes.Kind.entries.forEach { kind ->
            for (i in 0..10) assertTrue("$kind at ${i / 10f}", painted(358f, 250f) { CarScenes.draw(it, kind, i / 10f, dark = i % 2 == 0) } > 0.05f)
        }
    }

    @Test fun car3dRendersFromEveryAngle() {
        Car3D.Kind.entries.forEach { kind ->
            val r = Car3D.Renderer(Car3D.build(kind))
            Car3D.View.entries.forEach { view ->
                for (yaw in 0 until 360 step 45) assertTrue(painted(300f, 260f) { r.draw(it, yaw.toFloat(), if (view == Car3D.View.SCHEME) -88f else -22f, view) } > 0.08f)
            }
        }
    }

    @Test fun svgParserHandlesRelativeAndArcCommands() {
        val p = ArtPath.svg("M10 10 h20 v20 l-20 0 z m40 0 a10 10 0 0 1 20 0 q5 5 10 0 c1 1 2 2 3 3")
        val verbs = ArrayList<String>()
        var end = 0f to 0f
        p.replay(object : ArtPath.PathSink {
            override fun moveTo(x: Float, y: Float) { verbs += "M"; end = x to y }
            override fun lineTo(x: Float, y: Float) { verbs += "L"; end = x to y }
            override fun quadTo(x1: Float, y1: Float, x: Float, y: Float) { verbs += "Q"; end = x to y }
            override fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) { verbs += "C"; end = x to y }
            override fun close() { verbs += "Z" }
        })
        assertEquals(listOf("M", "L", "L", "L", "Z", "M"), verbs.take(6))
        assertEquals(83f, end.first, 0.01f)
        assertEquals(13f, end.second, 0.01f)
    }
}
