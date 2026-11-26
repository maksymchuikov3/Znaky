package com.znaky.core.art

import kotlin.math.sqrt

/**
 * Road signs of annex 1 to the Rules, drawn to the shapes and proportions of ДСТУ 4100:
 * a thin white outer edge, the red or blue field, and pictograms with the standard's
 * stroke weights. Every sign is drawn in a 100 × 100 box; [draw] scales it to the canvas.
 */
object RoadSigns {
    const val RED = 0xFFD42A20
    const val BLUE = 0xFF1C5BB4
    const val YELLOW = 0xFFFFC21A
    const val GREEN = 0xFF0B7A3E
    const val BLACK = 0xFF16181B
    const val WHITE = 0xFFFFFFFF
    /** Hairline around the white edge, so white signs keep their outline on white screens. */
    private const val EDGE = 0xFFB4BCC6

    val codes: List<String> get() = art.keys.toList()

    fun has(code: String) = code in art

    /** Draws sign [code] centred in the canvas, as large as fits. */
    fun draw(c: ArtCanvas, code: String) {
        val block = art[code] ?: return
        val s = minOf(c.width, c.height) / 100f
        c.layer {
            translate((c.width - 100f * s) / 2, (c.height - 100f * s) / 2)
            scale(s)
            block()
        }
    }

    // ---- Sign blanks ----------------------------------------------------------------

    /** Equilateral triangle with side ~94, as points inset by [d] from the outer edge. */
    private fun tri(d: Float, up: Boolean): FloatArray {
        val h = 94f * sqrt(3f) / 2
        val top = (100f - h) / 2 + if (up) 3f else -3f
        val cy = if (up) top + h * 2 / 3 else top + h / 3
        val k = (h / 3 - d) / (h / 3)
        val pts = if (up) floatArrayOf(50f, top, 97f, top + h, 3f, top + h) else floatArrayOf(3f, top, 97f, top, 50f, top + h)
        for (i in pts.indices step 2) { pts[i] = 50f + (pts[i] - 50f) * k; pts[i + 1] = cy + (pts[i + 1] - cy) * k }
        return pts
    }

    private fun ArtCanvas.triangleBlank(up: Boolean) {
        path(ArtPath.roundedPolygon(7f, *tri(0f, up)), fill(WHITE))
        path(ArtPath.roundedPolygon(7f, *tri(0f, up)), stroke(EDGE, 0.6f))
        path(ArtPath.roundedPolygon(5.6f, *tri(1.8f, up)), fill(RED))
        path(ArtPath.roundedPolygon(2.6f, *tri(10.2f, up)), fill(WHITE))
    }

    private fun ArtCanvas.warning(glyph: ArtCanvas.() -> Unit) { triangleBlank(true); glyph() }

    private fun ArtCanvas.roundBlank(field: Long, ring: Long? = RED) {
        circle(50f, 50f, 48.5f, fill(WHITE))
        circle(50f, 50f, 48.5f, stroke(EDGE, 0.6f))
        if (ring != null) {
            circle(50f, 50f, 46.8f, fill(ring))
            circle(50f, 50f, 37f, fill(field))
        } else circle(50f, 50f, 46.8f, fill(field))
    }

    private fun ArtCanvas.prohibitory(glyph: ArtCanvas.() -> Unit = {}) { roundBlank(WHITE); glyph() }
    private fun ArtCanvas.mandatory(glyph: ArtCanvas.() -> Unit) { roundBlank(BLUE, ring = null); glyph() }

    private fun ArtCanvas.squareBlank(field: Long) {
        rect(2f, 2f, 98f, 98f, fill(WHITE), radius = 9f)
        rect(2f, 2f, 98f, 98f, stroke(EDGE, 0.6f), radius = 9f)
        rect(3.8f, 3.8f, 96.2f, 96.2f, fill(field), radius = 7.5f)
    }

    // ---- Pictogram parts --------------------------------------------------------------

    /** Straight arrow from ([x0],[y0]) to the tip ([x1],[y1]): shaft [w] wide, head [hw] wide and [hl] long. */
    private fun ArtCanvas.arrow(x0: Float, y0: Float, x1: Float, y1: Float, w: Float, hw: Float, hl: Float, color: Long) {
        val dx = x1 - x0; val dy = y1 - y0
        val len = sqrt(dx * dx + dy * dy)
        val ux = dx / len; val uy = dy / len
        val nx = -uy; val ny = ux
        val bx = x1 - ux * hl; val by = y1 - uy * hl
        path(
            ArtPath()
                .moveTo(x0 + nx * w / 2, y0 + ny * w / 2).lineTo(bx + nx * w / 2, by + ny * w / 2)
                .lineTo(bx + nx * hw / 2, by + ny * hw / 2).lineTo(x1, y1)
                .lineTo(bx - nx * hw / 2, by - ny * hw / 2).lineTo(bx - nx * w / 2, by - ny * w / 2)
                .lineTo(x0 - nx * w / 2, y0 - ny * w / 2).close(),
            Paint(color, roundJoin = false),
        )
    }

    /** Arrow that goes up from ([x], [y0]) and bends towards [dir] (+1 right, −1 left), tip at ([tipX], [tipY]). */
    private fun ArtCanvas.bentArrow(x: Float, y0: Float, tipX: Float, tipY: Float, dir: Int, w: Float, hw: Float, hl: Float, color: Long) {
        val baseX = tipX - dir * hl
        val r = 11f
        path(
            ArtPath().moveTo(x, y0).lineTo(x, tipY + r).quadTo(x, tipY, x + dir * r, tipY).lineTo(baseX + dir * 0.5f, tipY),
            stroke(color, w, cap = Cap.BUTT, roundJoin = false),
        )
        path(ArtPath().moveTo(tipX, tipY).lineTo(baseX, tipY - hw / 2).lineTo(baseX, tipY + hw / 2).close(), Paint(color, roundJoin = false))
        rect(x - w / 2, y0 - 0.1f, x + w / 2, y0 + 0.1f, fill(color))
    }

    /** Car seen from the front, centred at ([x], [y]). */
    private fun ArtCanvas.carFront(x: Float, y: Float, color: Long) {
        val body = ArtPath()
            .moveTo(x - 7.5f, y - 10f).lineTo(x + 7.5f, y - 10f).quadTo(x + 9f, y - 10f, x + 9.8f, y - 8f)
            .lineTo(x + 11.5f, y - 2.5f).quadTo(x + 13f, y - 2f, x + 13f, y).lineTo(x + 13f, y + 6f)
            .quadTo(x + 13f, y + 7.5f, x + 11.5f, y + 7.5f).lineTo(x - 11.5f, y + 7.5f).quadTo(x - 13f, y + 7.5f, x - 13f, y + 6f)
            .lineTo(x - 13f, y).quadTo(x - 13f, y - 2f, x - 11.5f, y - 2.5f).lineTo(x - 9.8f, y - 8f).quadTo(x - 9f, y - 10f, x - 7.5f, y - 10f).close()
        rect(x - 12f, y + 5f, x - 7f, y + 11.5f, fill(color), radius = 1.2f)
        rect(x + 7f, y + 5f, x + 12f, y + 11.5f, fill(color), radius = 1.2f)
        path(body, fill(color))
        path(ArtPath.roundedPolygon(1f, x - 6.6f, y - 7.6f, x + 6.6f, y - 7.6f, x + 8.6f, y - 2.6f, x - 8.6f, y - 2.6f), fill(WHITE))
        circle(x - 8.6f, y + 2f, 1.9f, fill(WHITE))
        circle(x + 8.6f, y + 2f, 1.9f, fill(WHITE))
    }

    /** A walking person: [x], [y] = hip; [k] = size; [facing] +1 right. */
    private fun ArtCanvas.walker(x: Float, y: Float, k: Float, facing: Int = 1, handTo: Pair<Float, Float>? = null) {
        val f = facing.toFloat()
        val limb = stroke(BLACK, 4.6f * k)
        circle(x + 2.6f * k * f, y - 22.5f * k, 4.6f * k, fill(BLACK))
        path(ArtPath().moveTo(x + 2f * k * f, y - 15f * k).lineTo(x, y), stroke(BLACK, 6.4f * k))
        // Legs mid-stride.
        path(ArtPath().moveTo(x, y).lineTo(x + 5f * k * f, y + 7.5f * k).lineTo(x + 7.5f * k * f, y + 15.5f * k), limb)
        path(ArtPath().moveTo(x, y).lineTo(x - 3.5f * k * f, y + 8f * k).lineTo(x - 9f * k * f, y + 13.5f * k), limb)
        // Arms swing opposite to the legs.
        path(ArtPath().moveTo(x + 1.8f * k * f, y - 13.5f * k).lineTo(x - 3.5f * k * f, y - 7f * k).lineTo(x - 5f * k * f, y - 1f * k), stroke(BLACK, 3.8f * k))
        val hand = handTo ?: ((x + 8f * k * f) to (y - 4f * k))
        path(ArtPath().moveTo(x + 1.8f * k * f, y - 13.5f * k).lineTo(x + 5.5f * k * f, y - 8f * k).lineTo(hand.first, hand.second), stroke(BLACK, 3.8f * k))
    }

    private val art: Map<String, ArtCanvas.() -> Unit> = linkedMapOf(
        "1.1" to { warning { bentArrow(41f, 80f, 65f, 52f, 1, 7.4f, 18f, 13f, BLACK) } },
        "1.2" to { warning { bentArrow(59f, 80f, 35f, 52f, -1, 7.4f, 18f, 13f, BLACK) } },
        "1.22" to {
            warning {
                // Zebra under the walker's feet.
                for (i in 0 until 5) {
                    val x = 27f + i * 9.6f
                    path(ArtPath.roundedPolygon(0.6f, x + 2f, 76f, x + 7.6f, 76f, x + 5.6f, 81.5f, x, 81.5f), fill(BLACK))
                }
                walker(49f, 57.5f, 1f)
            }
        },
        "1.23" to {
            warning {
                // A grown-up child leading a smaller one by the hand.
                walker(41f, 59f, 0.98f, handTo = 53f to 59.5f)
                walker(61f, 66f, 0.72f, facing = 1, handTo = 53f to 59.5f)
                rect(26f, 79.5f, 74f, 81.5f, fill(BLACK), radius = 1f)
            }
        },
        "2.1" to { triangleBlank(false) },
        "2.2" to {
            fun oct(r: Float): FloatArray {
                val a = r * 0.4142f
                return floatArrayOf(50 - a, 50 - r, 50 + a, 50 - r, 50 + r, 50 - a, 50 + r, 50 + a, 50 + a, 50 + r, 50 - a, 50 + r, 50 - r, 50 + a, 50 - r, 50 - a)
            }
            path(ArtPath.roundedPolygon(4f, *oct(48.5f)), fill(WHITE))
            path(ArtPath.roundedPolygon(4f, *oct(48.5f)), stroke(EDGE, 0.6f))
            path(ArtPath.roundedPolygon(3.4f, *oct(46.8f)), fill(RED))
            path(ArtPath.roundedPolygon(3f, *oct(42.6f)), stroke(WHITE, 2.4f))
            text("STOP", 50f, 50f, 19f, WHITE, condensed = true)
        },
        "2.3" to { mainRoad(end = false) },
        "2.4" to { mainRoad(end = true) },
        "2.5" to {
            // Oncoming traffic has priority: its arrow (down, on the left) is black, yours is red.
            prohibitory {
                arrow(38f, 26f, 38f, 75f, 6.4f, 16f, 15f, BLACK)
                arrow(62f, 75f, 62f, 26f, 6.4f, 16f, 15f, RED)
            }
        },
        "2.6" to {
            squareBlank(BLUE)
            arrow(36f, 22f, 36f, 78f, 6.4f, 16f, 15f, RED)
            arrow(64f, 78f, 64f, 22f, 6.4f, 16f, 15f, WHITE)
        },
        "3.1" to { prohibitory() },
        "3.21" to {
            circle(50f, 50f, 48.5f, fill(WHITE))
            circle(50f, 50f, 48.5f, stroke(EDGE, 0.6f))
            circle(50f, 50f, 46.8f, fill(RED))
            rect(17f, 42f, 83f, 58f, fill(WHITE), radius = 1.2f)
        },
        "3.25" to { prohibitory { carFront(35f, 50f, RED); carFront(65f, 50f, BLACK) } },
        "3.29" to { prohibitory { text("50", 50f, 50f, 34f, BLACK, condensed = true) } },
        "3.34" to {
            roundBlank(BLUE)
            clip(ArtPath.circle(50f, 50f, 38f))
            line(20f, 20f, 80f, 80f, stroke(RED, 9.4f, cap = Cap.BUTT))
            line(80f, 20f, 20f, 80f, stroke(RED, 9.4f, cap = Cap.BUTT))
        },
        "3.35" to {
            roundBlank(BLUE)
            clip(ArtPath.circle(50f, 50f, 38f))
            line(80f, 20f, 20f, 80f, stroke(RED, 9.4f, cap = Cap.BUTT))
        },
        "4.1" to { mandatory { arrow(50f, 82f, 50f, 17f, 11f, 30f, 24f, WHITE) } },
        "4.2" to { mandatory { bentArrow(38f, 82f, 82f, 44f, 1, 11f, 30f, 22f, WHITE) } },
        "4.3" to { mandatory { bentArrow(62f, 82f, 18f, 44f, -1, 11f, 30f, 22f, WHITE) } },
        "5.1" to {
            squareBlank(GREEN)
            // Two carriageways under an overpass.
            path(ArtPath.svg("M14 86 L40 86 L47.6 47 L45.4 47 Z"), fill(WHITE))
            path(ArtPath.svg("M86 86 L60 86 L52.4 47 L54.6 47 Z"), fill(WHITE))
            rect(10f, 30f, 90f, 39f, fill(WHITE), radius = 1.5f)
            rect(22f, 39f, 27f, 50f, fill(WHITE))
            rect(73f, 39f, 78f, 50f, fill(WHITE))
            rect(10f, 41.5f, 90f, 43f, fill(GREEN))
        },
    )

    private fun ArtCanvas.mainRoad(end: Boolean) {
        val outer = ArtPath.roundedPolygon(5f, 50f, 1.5f, 98.5f, 50f, 50f, 98.5f, 1.5f, 50f)
        path(outer, fill(WHITE))
        path(outer, stroke(EDGE, 0.6f))
        path(ArtPath.roundedPolygon(4f, 50f, 4.2f, 95.8f, 50f, 50f, 95.8f, 4.2f, 50f), stroke(BLACK, 1.2f))
        path(ArtPath.roundedPolygon(1.5f, 50f, 20f, 80f, 50f, 50f, 80f, 20f, 50f), fill(YELLOW))
        if (end) {
            clip(ArtPath.roundedPolygon(1.5f, 50f, 9f, 91f, 50f, 50f, 91f, 9f, 50f))
            for (i in -2..2) {
                val o = i * 4.2f
                line(8f + o, 92f + o, 92f + o, 8f + o, stroke(BLACK, 1.8f, cap = Cap.BUTT))
            }
        }
    }
}
