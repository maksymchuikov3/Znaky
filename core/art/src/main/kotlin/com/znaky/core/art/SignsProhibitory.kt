package com.znaky.core.art

/** Group 2: priority signs. */
internal val prioritySigns: Map<String, SignArt> = linkedMapOf(
    "2.1" to { triangleBlank(false) },
    "2.2" to {
        fun oct(r: Float): FloatArray {
            val a = r * 0.4142f
            return floatArrayOf(50 - a, 50 - r, 50 + a, 50 - r, 50 + r, 50 - a, 50 + r, 50 + a, 50 + a, 50 + r, 50 - a, 50 + r, 50 - r, 50 + a, 50 - r, 50 - a)
        }
        path(ArtPath.roundedPolygon(4f, *oct(48.5f)), fill(S_WHITE))
        path(ArtPath.roundedPolygon(4f, *oct(48.5f)), stroke(S_EDGE, 0.6f))
        path(ArtPath.roundedPolygon(3.4f, *oct(46.8f)), fill(S_RED))
        path(ArtPath.roundedPolygon(3f, *oct(42.6f)), stroke(S_WHITE, 2.4f))
        text("STOP", 50f, 50f, 19f, S_WHITE, condensed = true)
    },
    "2.3" to { mainRoad(end = false) },
    "2.4" to { mainRoad(end = true) },
    "2.5" to {
        // Oncoming traffic has priority: its arrow (down, on the left) is black, yours is red.
        prohibitory {
            arrow(38f, 26f, 38f, 75f, 6.4f, 16f, 15f, S_BLACK)
            arrow(62f, 75f, 62f, 26f, 6.4f, 16f, 15f, S_RED)
        }
    },
    "2.6" to {
        squareBlank(S_BLUE)
        arrow(36f, 22f, 36f, 78f, 6.4f, 16f, 15f, S_RED)
        arrow(64f, 78f, 64f, 22f, 6.4f, 16f, 15f, S_WHITE)
    },
)

private fun ArtCanvas.mainRoad(end: Boolean) {
    val outer = ArtPath.roundedPolygon(5f, 50f, 1.5f, 98.5f, 50f, 50f, 98.5f, 1.5f, 50f)
    path(outer, fill(S_WHITE))
    path(outer, stroke(S_EDGE, 0.6f))
    path(ArtPath.roundedPolygon(4f, 50f, 4.2f, 95.8f, 50f, 50f, 95.8f, 4.2f, 50f), stroke(S_BLACK, 1.2f))
    path(ArtPath.roundedPolygon(1.5f, 50f, 20f, 80f, 50f, 50f, 80f, 20f, 50f), fill(S_YELLOW))
    if (end) {
        clip(ArtPath.roundedPolygon(1.5f, 50f, 9f, 91f, 50f, 50f, 91f, 9f, 50f))
        for (i in -2..2) {
            val o = i * 4.2f
            line(8f + o, 92f + o, 92f + o, 8f + o, stroke(S_BLACK, 1.8f, cap = Cap.BUTT))
        }
    }
}

/** Group 3: prohibitory signs. */
internal val prohibitorySigns: Map<String, SignArt> = linkedMapOf(
    "3.1" to { prohibitory() },
    "3.2" to {
        prohibitory {
            at(50f, 38f, 0.74f) { carSide(S_BLACK, S_WHITE) }
            at(50f, 63f, 0.7f) { motoSide(S_BLACK, S_WHITE) }
        }
    },
    "3.3" to { prohibitory { at(50f, 50f, 1.06f) { truckSide(S_BLACK, S_WHITE) } } },
    "3.4" to { prohibitory { at(53f, 50f, 0.84f) { truckSide(S_BLACK, S_WHITE, trailer = true) } } },
    "3.5" to { prohibitory { at(48f, 50f, 1.04f) { tractorSide(S_BLACK, S_WHITE) } } },
    "3.6" to { prohibitory { at(50f, 51f, 1.08f) { motoSide(S_BLACK, S_WHITE) } } },
    "3.7" to { prohibitory { at(50f, 51f, 1.1f) { mopedSide(S_BLACK, S_WHITE) } } },
    "3.8" to { prohibitory { at(50f, 51f, 1.12f) { bicycle(S_BLACK, 2.8f) } } },
    "3.9" to { prohibitory { at(50f, 50f, 1.12f) { pedestrian(S_BLACK) } } },
    "3.10" to { prohibitory { at(50f, 52f, 1.06f) { handCart(S_BLACK, S_WHITE) } } },
    "3.11" to { prohibitory { at(51f, 52f, 0.98f) { horseCart(S_BLACK, S_WHITE) } } },
    "3.12" to { prohibitory { at(50f, 50f, 1.06f) { truckSide(S_BLACK, S_WHITE, plate = S_ORANGE) } } },
    "3.13" to {
        prohibitory {
            at(50f, 56f, 0.96f) { truckSide(S_BLACK, S_WHITE) }
            burst(40f, 30f, 9f)
        }
    },
    "3.14" to {
        prohibitory {
            at(50f, 56f, 0.96f) { truckSide(S_BLACK, S_WHITE) }
            for (x in listOf(30f, 42f, 54f)) drop(x, 28f)
        }
    },
    "3.15" to { prohibitory { text("7т", 50f, 50f, 30f, S_BLACK, condensed = true) } },
    "3.16" to {
        prohibitory {
            text("6т", 50f, 37f, 21f, S_BLACK, condensed = true)
            rect(28f, 57f, 72f, 61f, fill(S_BLACK))
            for (x in listOf(34f, 66f)) { circle(x, 66f, 6f, fill(S_BLACK)); circle(x, 66f, 2f, fill(S_WHITE)) }
        }
    },
    "3.17" to {
        prohibitory {
            text("2,5", 50f, 50f, 22f, S_BLACK, condensed = true)
            arrowHead(28f, 50f, 0f, 14f, 10f, S_BLACK)
            arrowHead(72f, 50f, 180f, 14f, 10f, S_BLACK)
        }
    },
    "3.18" to {
        prohibitory {
            text("3,5", 50f, 50f, 22f, S_BLACK, condensed = true)
            arrowHead(50f, 28f, 90f, 14f, 10f, S_BLACK)
            arrowHead(50f, 72f, 270f, 14f, 10f, S_BLACK)
        }
    },
    "3.19" to {
        prohibitory {
            at(50f, 40f, 0.86f) { truckSide(S_BLACK, S_WHITE) }
            text("10м", 50f, 66f, 13f, S_BLACK, condensed = true)
            arrowHead(26f, 57f, 180f, 7f, 5f, S_BLACK); arrowHead(74f, 57f, 0f, 7f, 5f, S_BLACK)
            line(28f, 57f, 72f, 57f, stroke(S_BLACK, 1.4f))
        }
    },
    "3.20" to {
        prohibitory {
            at(35f, 40f, 0.5f) { truckSide(S_BLACK, S_WHITE) }
            at(67f, 40f, 0.5f) { truckSide(S_BLACK, S_WHITE) }
            text("70 м", 50f, 64f, 13f, S_BLACK, condensed = true)
            line(25f, 53f, 75f, 53f, stroke(S_BLACK, 1.4f))
            arrowHead(24f, 53f, 180f, 6f, 4.6f, S_BLACK); arrowHead(76f, 53f, 0f, 6f, 4.6f, S_BLACK)
        }
    },
    "3.21" to {
        circle(50f, 50f, 48.5f, fill(S_WHITE))
        circle(50f, 50f, 48.5f, stroke(S_EDGE, 0.6f))
        circle(50f, 50f, 46.8f, fill(S_RED))
        rect(17f, 42f, 83f, 58f, fill(S_WHITE), radius = 1.2f)
    },
    "3.22" to { prohibitoryBar { bentArrow(40f, 78f, 76f, 42f, 1, 8.6f, 22f, 16f, S_BLACK) } },
    "3.23" to { prohibitoryBar { bentArrow(60f, 78f, 24f, 42f, -1, 8.6f, 22f, 16f, S_BLACK) } },
    "3.24" to { prohibitoryBar { uTurnArrow(62f, 78f, 72f, 24f, 8.4f, 21f, 15f, S_BLACK) } },
    "3.25" to { prohibitory { at(35f, 50f) { carFront(S_RED) }; at(65f, 50f) { carFront(S_BLACK) } } },
    "3.26" to { endOfRestriction { at(35f, 50f) { carFront(S_GREY) }; at(65f, 50f) { carFront(S_GREY) } } },
    "3.27" to { prohibitory { at(33f, 52f, 0.56f) { truckFront(S_RED) }; at(66f, 50f) { carFront(S_BLACK) } } },
    "3.28" to { endOfRestriction { at(33f, 52f, 0.56f) { truckFront(S_GREY) }; at(66f, 50f) { carFront(S_GREY) } } },
    "3.29" to { prohibitory { text("50", 50f, 50f, 34f, S_BLACK, condensed = true) } },
    "3.30" to { endOfRestriction { text("50", 50f, 50f, 34f, S_GREY, condensed = true) } },
    "3.31" to { zoneSign("ЗОНА") { miniRound { text("30", 0f, 0f, 16f, S_BLACK, condensed = true) } } },
    "3.32" to {
        zoneSign("ЗОНА", end = true) { miniRound(grey = true) { text("30", 0f, 0f, 16f, S_GREY, condensed = true) } }
    },
    "3.33" to {
        prohibitoryBar {
            path(ArtPath.svg("M28 44 L36 44 L60 32 Q64 30 64 34 L64 66 Q64 70 60 68 L36 56 L28 56 Q26 56 26 54 L26 46 Q26 44 28 44 Z"), fill(S_BLACK))
            rect(34f, 56f, 41f, 70f, fill(S_BLACK), radius = 1.5f)
            rect(64f, 42f, 72f, 58f, fill(S_BLACK), radius = 2f)
        }
    },
    "3.34" to { noStopping(cross = true) },
    "3.35" to { noStopping(cross = false) },
    "3.36" to { noStopping(cross = false); rect(48.2f, 32f, 51.8f, 68f, fill(S_WHITE)) },
    "3.37" to { noStopping(cross = false); rect(42.4f, 32f, 46f, 68f, fill(S_WHITE)); rect(54f, 32f, 57.6f, 68f, fill(S_WHITE)) },
    "3.38" to { zoneSign("ЗОНА") { miniParking() } },
    "3.39" to { zoneSign("ЗОНА", end = true) { miniParking(grey = true) } },
    "3.40" to { checkpoint("МИТНИЦЯ") },
    "3.41" to { checkpoint("КОНТРОЛЬ") },
    "3.42" to { endOfRestriction() },
    "3.43" to {
        prohibitory {
            rect(46.6f, 25f, 53.4f, 60f, fill(S_BLACK), radius = 3.2f)
            circle(50f, 70f, 4.4f, fill(S_BLACK))
        }
    },
)

/** Car seen from the front, centred on the origin. */
internal fun ArtCanvas.carFront(color: Long, bg: Long = S_WHITE) {
    val body = ArtPath.svg("M-7.5 -10 L7.5 -10 Q9 -10 9.8 -8 L11.5 -2.5 Q13 -2 13 0 L13 6 Q13 7.5 11.5 7.5 L-11.5 7.5 Q-13 7.5 -13 6 L-13 0 Q-13 -2 -11.5 -2.5 L-9.8 -8 Q-9 -10 -7.5 -10 Z")
    rect(-12f, 5f, -7f, 11.5f, fill(color), radius = 1.2f)
    rect(7f, 5f, 12f, 11.5f, fill(color), radius = 1.2f)
    path(body, fill(color))
    path(ArtPath.roundedPolygon(1f, -6.6f, -7.6f, 6.6f, -7.6f, 8.6f, -2.6f, -8.6f, -2.6f), fill(bg))
    circle(-8.6f, 2f, 1.9f, fill(bg))
    circle(8.6f, 2f, 1.9f, fill(bg))
}

/** Lorry seen from the front, centred on the origin, ~34 wide at k = 1. */
internal fun ArtCanvas.truckFront(color: Long, bg: Long = S_WHITE) {
    rect(-17f, -24f, 17f, 14f, fill(color), radius = 3f)
    rect(-13f, -19f, 13f, -6f, fill(bg), radius = 1.5f)
    circle(-11f, 6f, 2.6f, fill(bg)); circle(11f, 6f, 2.6f, fill(bg))
    rect(-6f, 3f, 6f, 9f, fill(bg))
    rect(-16f, 14f, -9f, 22f, fill(color), radius = 1.5f)
    rect(9f, 14f, 16f, 22f, fill(color), radius = 1.5f)
}

private fun ArtCanvas.burst(cx: Float, cy: Float, r: Float) {
    val pts = FloatArray(32)
    for (i in 0 until 16) {
        val a = Math.toRadians(i * 22.5 - 90)
        val rr = if (i % 2 == 0) r else r * 0.48f
        pts[i * 2] = cx + (kotlin.math.cos(a) * rr).toFloat(); pts[i * 2 + 1] = cy + (kotlin.math.sin(a) * rr).toFloat()
    }
    shape(S_BLACK, *pts, round = 0.3f)
}

private fun ArtCanvas.drop(x: Float, y: Float) =
    path(ArtPath.svg("M$x ${y - 6} Q${x + 4.6f} ${y + 0.5f} ${x + 4.6f} ${y + 2.6f} A4.6 4.6 0 0 1 ${x - 4.6f} ${y + 2.6f} Q${x - 4.6f} ${y + 0.5f} $x ${y - 6} Z"), fill(S_BLACK))

private fun ArtCanvas.noStopping(cross: Boolean) {
    roundBlank(S_BLUE)
    layer {
        clip(ArtPath.circle(50f, 50f, 38f))
        line(80f, 20f, 20f, 80f, stroke(S_RED, 9.4f, cap = Cap.BUTT))
        if (cross) line(20f, 20f, 80f, 80f, stroke(S_RED, 9.4f, cap = Cap.BUTT))
    }
}

/** A round sign drawn small, centred on the origin (used inside zone signs). */
private fun ArtCanvas.miniRound(grey: Boolean = false, glyph: ArtCanvas.() -> Unit) {
    if (grey) {
        circle(0f, 0f, 18f, fill(S_WHITE)); circle(0f, 0f, 18f, stroke(S_GREY, 1f))
    } else {
        circle(0f, 0f, 18f, fill(S_RED)); circle(0f, 0f, 14f, fill(S_WHITE))
    }
    glyph()
}

private fun ArtCanvas.miniParking(grey: Boolean = false) {
    if (grey) {
        circle(0f, 0f, 18f, fill(S_WHITE)); circle(0f, 0f, 18f, stroke(S_GREY, 1f))
        layer { clip(ArtPath.circle(0f, 0f, 14f)); line(11f, -11f, -11f, 11f, stroke(S_GREY, 4f, cap = Cap.BUTT)) }
    } else {
        circle(0f, 0f, 18f, fill(S_RED)); circle(0f, 0f, 14f, fill(S_BLUE))
        layer { clip(ArtPath.circle(0f, 0f, 14f)); line(11f, -11f, -11f, 11f, stroke(S_RED, 4f, cap = Cap.BUTT)) }
    }
}

/** 3.31, 3.32, 3.38, 3.39: white square with a small sign and the word ЗОНА. */
private fun ArtCanvas.zoneSign(word: String, end: Boolean = false, sign: ArtCanvas.() -> Unit) {
    rectBlank(80f, 96f, S_WHITE, border = if (end) S_GREY else S_BLACK)
    at(50f, 38f) { sign() }
    text(word, 50f, 74f, 12f, if (end) S_GREY else S_BLACK, condensed = true)
    if (end) layer {
        clip(ArtPath.roundRect(12f, 4f, 88f, 96f, 4f))
        for (i in -2..2) { val o = i * 3.4f; line(14f + o, 94f + o, 86f + o, 6f + o, stroke(S_BLACK, 1.5f, cap = Cap.BUTT)) }
    }
}

/** 3.40, 3.41: prohibitory sign with a horizontal bar and a word. */
private fun ArtCanvas.checkpoint(word: String) {
    prohibitory {
        rect(14f, 48.6f, 86f, 51.4f, fill(S_RED))
        text(word, 50f, 39.5f, 7.6f, S_BLACK, condensed = true)
    }
}
