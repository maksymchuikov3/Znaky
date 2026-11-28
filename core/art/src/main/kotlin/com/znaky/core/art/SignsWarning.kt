package com.znaky.core.art

/** Group 1: warning signs. Pictograms sit in the white field of the triangle, around (50, 62). */
internal val warningSigns: Map<String, SignArt> = linkedMapOf(
    "1.1" to { warning { bentArrow(41f, 80f, 65f, 52f, 1, 7.4f, 18f, 13f, S_BLACK) } },
    "1.2" to { warning { bentArrow(59f, 80f, 35f, 52f, -1, 7.4f, 18f, 13f, S_BLACK) } },
    "1.3.1" to { warning { zigzag(1) } },
    "1.3.2" to { warning { zigzag(-1) } },
    "1.4.1" to { chevrons(1) },
    "1.4.2" to { chevrons(-1) },
    "1.4.3" to { chevrons(0) },
    "1.5.1" to { warning { narrowing(left = true, right = true) } },
    "1.5.2" to { warning { narrowing(left = false, right = true) } },
    "1.5.3" to { warning { narrowing(left = true, right = false) } },
    "1.6" to { warning { slope(up = true) } },
    "1.7" to { warning { slope(up = false) } },
    "1.8" to {
        warning {
            // A car rolling off the quay into the water.
            shape(S_BLACK, 24f, 66f, 52f, 66f, 52f, 72f, 24f, 72f, round = 0.3f)
            at(48f, 57f, 0.72f) { layer { rotate(30f); carSide(S_BLACK, S_WHITE) } }
            for (i in 0 until 2) path(ArtPath.svg("M${54 + i * 0} ${74 + i * 4.5f} q3 -2.4 6 0 t6 0 t6 0 t6 0"), stroke(S_BLACK, 2f))
        }
    },
    "1.9" to {
        warning {
            path(ArtPath.svg("M25 80 L25 62 Q25 41 50 41 Q75 41 75 62 L75 80 L66 80 L66 63 Q66 50 50 50 Q34 50 34 63 L34 80 Z"), fill(S_BLACK))
            path(ArtPath.svg("M42 80 L47 55 L53 55 L58 80 Z"), fill(S_BLACK))
            rect(49.2f, 60f, 50.8f, 64f, fill(S_WHITE)); rect(49.2f, 69f, 50.8f, 74f, fill(S_WHITE))
        }
    },
    "1.10" to { warning { path(ArtPath.svg("M24 78 L24 70 Q30.5 60 37 70 Q43.5 80 50 70 Q56.5 60 63 70 Q69.5 80 76 70 L76 78 Z"), fill(S_BLACK)) } },
    "1.11" to { warning { path(ArtPath.svg("M24 78 L24 73 L36 73 Q43 58 50 58 Q57 58 64 73 L76 73 L76 78 Z"), fill(S_BLACK)) } },
    "1.12" to { warning { path(ArtPath.svg("M24 66 L38 66 Q44 78 50 78 Q56 78 62 66 L76 66 L76 80 L24 80 Z"), fill(S_BLACK)) } },
    "1.13" to {
        warning {
            at(50f, 50f, 0.62f) { carSide(S_BLACK, S_WHITE) }
            path(ArtPath.svg("M35 80 C31 74 41 70 37 64"), stroke(S_BLACK, 2.6f))
            path(ArtPath.svg("M64 80 C68 74 58 70 62 64"), stroke(S_BLACK, 2.6f))
        }
    },
    "1.14" to {
        warning {
            at(55f, 68f, 0.6f) { carSide(S_BLACK, S_WHITE) }
            listOf(30f to 72f, 35f to 63f, 27f to 58f, 37f to 53f, 31f to 47f).forEachIndexed { i, (x, y) ->
                circle(x, y, 2.4f - i * 0.15f, fill(S_BLACK))
            }
        }
    },
    "1.15" to {
        warning {
            path(ArtPath.svg("M24 80 L24 64 L46 64 L46 70 L76 70 L76 80 Z"), fill(S_BLACK))
            rect(46f, 64f, 76f, 70f, fill(S_WHITE))
            for (i in 0 until 4) rect(48f + i * 7.4f, 66.4f, 52.4f + i * 7.4f, 68f, fill(S_BLACK))
        }
    },
    "1.16" to {
        warning {
            path(ArtPath.svg("M26 80 L26 40 L35 40 L48 80 Z"), fill(S_BLACK))
            shape(S_BLACK, 43f, 46f, 49f, 44f, 51f, 50f, 45f, 52f, round = 1f)
            shape(S_BLACK, 51f, 58f, 58f, 56f, 59f, 63f, 52f, 65f, round = 1f)
            shape(S_BLACK, 56f, 70f, 63f, 69f, 64f, 76f, 57f, 77f, round = 1f)
            rect(50f, 78f, 76f, 80f, fill(S_BLACK))
        }
    },
    "1.17" to { warning { at(51f, 64f, 0.98f) { windsock(S_BLACK) } } },
    "1.18" to { warning { at(51f, 62f, 0.9f) { airplane(S_BLACK) } } },
    "1.19" to {
        warning {
            roundaboutArrows(50f, 63f, 12.5f, 4.2f, 11f, 7.6f, S_BLACK)
        }
    },
    "1.20" to { warning { at(50f, 63f, 0.8f) { tramFront(S_BLACK, S_WHITE) } } },
    "1.21" to {
        warning {
            rect(46f, 38f, 54f, 82f, fill(S_BLACK))
            rect(28f, 60f, 72f, 68f, fill(S_BLACK))
        }
    },
    "1.22" to {
        warning {
            path(ArtPath.svg("M44 82 L44 43 L50 36 L56 43 L56 82 Z"), fill(S_BLACK))
            rect(28f, 61.5f, 72f, 66.5f, fill(S_BLACK))
        }
    },
    "1.23.1" to { warning { sideRoad(listOf(1 to 63f)) } },
    "1.23.2" to { warning { sideRoad(listOf(-1 to 63f)) } },
    "1.23.3" to { warning { sideRoad(listOf(1 to 54f, -1 to 72f)) } },
    "1.23.4" to { warning { sideRoad(listOf(-1 to 54f, 1 to 72f)) } },
    "1.24" to { warning { at(50f, 62f, 0.78f) { trafficLight() } } },
    "1.25" to {
        warning {
            rect(24f, 68f, 34f, 82f, fill(S_BLACK)); rect(66f, 68f, 76f, 82f, fill(S_BLACK))
            layer { rotate(-30f, 34f, 70f); rect(30f, 66f, 50f, 71f, fill(S_BLACK)) }
            layer { rotate(30f, 66f, 70f); rect(50f, 66f, 70f, 71f, fill(S_BLACK)) }
            for (i in 0 until 2) path(ArtPath.svg("M37 ${74 + i * 5} q2.2 -2 4.4 0 t4.4 0 t4.4 0 t4.4 0 t4.4 0 t4.4 0"), stroke(S_BLACK, 1.8f))
        }
    },
    "1.26" to {
        warning {
            arrow(41f, 46f, 41f, 82f, 5.6f, 13f, 11f, S_BLACK)
            arrow(59f, 80f, 59f, 42f, 5.6f, 13f, 11f, S_BLACK)
        }
    },
    "1.27" to {
        warning {
            // Barrier: a gate with posts.
            for (x in listOf(30f, 68f)) rect(x, 54f, x + 4f, 79f, fill(S_BLACK))
            path(ArtPath.svg("M30 58 L72 58 L72 66 L30 66 Z"), fill(S_BLACK))
            for (i in 0 until 3) rect(36f + i * 12f, 59.6f, 42f + i * 12f, 64.4f, fill(S_WHITE))
        }
    },
    "1.28" to { warning { at(52f, 66f, 0.74f) { locomotive(S_BLACK, S_WHITE) } } },
    "1.29" to { crossbuck(double = false) },
    "1.30" to { crossbuck(double = true) },
    "1.31.1" to { approachBoard(3, right = true) },
    "1.31.2" to { approachBoard(2, right = true) },
    "1.31.3" to { approachBoard(1, right = true) },
    "1.31.4" to { approachBoard(3, right = false) },
    "1.31.5" to { approachBoard(2, right = false) },
    "1.31.6" to { approachBoard(1, right = false) },
    "1.32" to {
        warning {
            for (i in 0 until 5) {
                val x = 27f + i * 9.6f
                path(ArtPath.roundedPolygon(0.6f, x + 2f, 76f, x + 7.6f, 76f, x + 5.6f, 81.5f, x, 81.5f), fill(S_BLACK))
            }
            at(49f, 57.5f) { pedestrian(S_BLACK) }
        }
    },
    "1.33" to {
        warning {
            at(41f, 59f, 0.98f) { pedestrian(S_BLACK) }
            at(61f, 66f, 0.72f) { pedestrian(S_BLACK) }
            poly(S_BLACK, 3.2f, 48.8f, 55.6f, 53f, 59.5f, 55.6f, 61f)
            rect(26f, 79.5f, 74f, 81.5f, fill(S_BLACK), radius = 1f)
        }
    },
    "1.34" to { warning { at(50f, 66f, 0.78f) { bicycle(S_BLACK, 3f) } } },
    "1.35" to { warning { at(50f, 66f, 0.86f) { cow(S_BLACK) } } },
    "1.36" to { warning { at(49f, 65f, 0.9f) { deer(S_BLACK) } } },
    "1.37" to { warning { at(50f, 65f, 0.86f) { roadWorker(S_BLACK) } } },
    "1.38" to {
        warning {
            at(50f, 47f, 0.4f) { carRear() }
            at(40f, 61f, 0.46f) { carRear() }
            at(60f, 61f, 0.46f) { carRear() }
            at(30f, 76f, 0.5f) { carRear() }
            at(50f, 76f, 0.5f) { carRear() }
            at(70f, 76f, 0.5f) { carRear() }
        }
    },
    "1.39" to {
        warning {
            rect(46.4f, 41f, 53.6f, 69f, fill(S_BLACK), radius = 3.4f)
            circle(50f, 76.5f, 4.2f, fill(S_BLACK))
        }
    },
    "1.40" to {
        warning {
            path(ArtPath.svg("M36 82 L41.6 62 L58.4 62 L64 82 Z"), fill(S_BLACK))
            for (row in 0 until 4) for (i in 0 until 4) {
                val y = 58f - row * 5f
                val half = 7.6f - row * 1.2f
                circle(50f - half + i * (2 * half / 3), y, 1.3f, fill(S_BLACK))
            }
        }
    },
    "1.4.4" to { chevronBoard(1, 1, 56f) },
    "1.4.5" to { chevronBoard(1, -1, 56f) },
    "1.4.6" to { chevronBoard(2, 1, 76f) },
    "1.4.7" to { chevronBoard(2, -1, 76f) },
    "1.41" to {
        warning {
            at(36f, 70f, 0.62f) { layer { rotate(24f); carSide(S_BLACK, S_WHITE) } }
            mirrored { at(36f, 70f, 0.62f) { layer { rotate(24f); carSide(S_BLACK, S_WHITE) } } }
            for (a in listOf(-60f, -90f, -120f)) {
                val r = Math.toRadians(a.toDouble())
                line(50f + 6f * kotlin.math.cos(r).toFloat(), 56f + 6f * kotlin.math.sin(r).toFloat(), 50f + 13f * kotlin.math.cos(r).toFloat(), 56f + 13f * kotlin.math.sin(r).toFloat(), stroke(S_BLACK, 2.4f))
            }
        }
    },
)

private fun ArtCanvas.zigzag(dir: Int) {
    val d = dir.toFloat()
    path(
        ArtPath.svg("M${50 - 10 * d} 82 L${50 - 10 * d} 70 Q${50 - 10 * d} 64 ${50 - 4 * d} 62 L${50 + 6 * d} 58.6 Q${50 + 11 * d} 57 ${50 + 9 * d} 52 L${50 + 4 * d} 44"),
        stroke(S_BLACK, 6.4f, cap = Cap.BUTT),
    )
    arrowHead(50f + 1.4f * d, 37.6f, if (dir > 0) 245f else 295f, 15f, 11f, S_BLACK)
}

private fun ArtCanvas.narrowing(left: Boolean, right: Boolean) {
    val lx = if (left) "M30 82 L30 70 Q30 64 38 58 Q42 55 42 50 L42 42" else "M33 82 L33 42"
    val rx = if (right) "M70 82 L70 70 Q70 64 62 58 Q58 55 58 50 L58 42" else "M67 82 L67 42"
    path(ArtPath.svg(lx), stroke(S_BLACK, 5f, cap = Cap.BUTT))
    path(ArtPath.svg(rx), stroke(S_BLACK, 5f, cap = Cap.BUTT))
}

private fun ArtCanvas.slope(up: Boolean) {
    val pts = if (up) floatArrayOf(25f, 80f, 75f, 80f, 75f, 54f) else floatArrayOf(25f, 80f, 75f, 80f, 25f, 54f)
    shape(S_BLACK, *pts, round = 0.5f)
    text("10%", if (up) 63f else 37f, 73.5f, 7.4f, S_WHITE, condensed = true)
}

private fun ArtCanvas.sideRoad(roads: List<Pair<Int, Float>>) {
    path(ArtPath.svg("M44 82 L44 43 L50 36 L56 43 L56 82 Z"), fill(S_BLACK))
    roads.forEach { (side, y) -> if (side > 0) rect(56f, y - 3.6f, 72f + (y - 63f) * -0.4f, y + 3.6f, fill(S_BLACK)) else rect(28f + (y - 63f) * 0.4f, y - 3.6f, 44f, y + 3.6f, fill(S_BLACK)) }
}

/** Car seen from behind, for the queue on 1.38. Centred on the origin, ~30 wide. */
private fun ArtCanvas.carRear() {
    svgFill("M-13 6 L-13 -2 Q-13 -4 -11 -5 L-8.6 -12 Q-8 -14 -6 -14 L6 -14 Q8 -14 8.6 -12 L11 -5 Q13 -4 13 -2 L13 6 Z", S_BLACK)
    svgFill("M-6.6 -11.6 L6.6 -11.6 L8.4 -6 L-8.4 -6 Z", S_WHITE)
    rect(-13f, 5f, -8f, 10f, fill(S_BLACK), radius = 1f)
    rect(8f, 5f, 13f, 10f, fill(S_BLACK), radius = 1f)
}

/** 1.4.x: red board with white chevrons; [dir] +1 right, −1 left, 0 both ways. */
private fun ArtCanvas.chevrons(dir: Int) {
    rectBlank(96f, 52f, S_RED)
    fun chev(x: Float, d: Int) {
        val k = d.toFloat()
        shape(S_WHITE, x - 7f * k, 32f, x + 4f * k, 50f, x - 7f * k, 68f, x - 1f * k, 68f, x + 10f * k, 50f, x - 1f * k, 32f, round = 0.3f)
    }
    when (dir) {
        0 -> { chev(28f, -1); chev(42f, -1); chev(58f, 1); chev(72f, 1) }
        else -> listOf(24f, 40f, 56f, 72f).forEach { chev(if (dir > 0) it else 100f - it, dir) }
    }
}

/** 1.29 / 1.30: the white X-shaped crossbuck with red edges. */
private fun ArtCanvas.crossbuck(double: Boolean) {
    fun bar(angle: Float, cy: Float) = layer {
        rotate(angle, 50f, cy)
        rect(4f, cy - 6.6f, 96f, cy + 6.6f, fill(S_EDGE), radius = 1.4f)
        rect(4.3f, cy - 6.3f, 95.7f, cy + 6.3f, fill(S_RED), radius = 1.2f)
        rect(6.6f, cy - 4.2f, 93.4f, cy + 4.2f, fill(S_WHITE), radius = 0.6f)
    }
    if (double) {
        layer { translate(50f, 38f); scale(0.8f); translate(-50f, -50f); bar(36f, 50f); bar(-36f, 50f) }
        layer {
            clip(ArtPath().moveTo(0f, 72f).lineTo(100f, 72f).lineTo(100f, 100f).lineTo(0f, 100f).close())
            layer { translate(50f, 38f); scale(0.8f); translate(-50f, -50f); translate(0f, 38f); bar(36f, 50f); bar(-36f, 50f) }
        }
    } else {
        bar(36f, 50f)
        bar(-36f, 50f)
    }
}

/** 1.31.x: tall white board with [n] red diagonal stripes; [right] for the right-hand side boards. */
private fun ArtCanvas.approachBoard(n: Int, right: Boolean) {
    rect(36f, 2f, 64f, 98f, fill(S_WHITE), radius = 2f)
    rect(36f, 2f, 64f, 98f, stroke(S_EDGE, 0.6f), radius = 2f)
    layer {
        clip(ArtPath.roundRect(37f, 3f, 63f, 97f, 1.6f))
        val k = if (right) 1f else -1f
        for (i in 0 until n) {
            val y = 50f - (n - 1) * 13f + i * 26f - 5f
            path(ArtPath().moveTo(50f - 18f * k, y + 12f * 1f).lineTo(50f + 18f * k, y - 10f).lineTo(50f + 18f * k, y - 1f).lineTo(50f - 18f * k, y + 21f).close(), fill(S_RED))
        }
    }
}


/** 1.4.4–1.4.7: smaller red boards with one or two white chevrons. */
private fun ArtCanvas.chevronBoard(n: Int, dir: Int, h: Float) {
    rectBlank(if (n == 1) 56f else 70f, h, S_RED)
    val k = dir.toFloat()
    val xs = if (n == 1) listOf(50f) else listOf(42f, 58f)
    val top = 50f - h * 0.3f
    val bot = 50f + h * 0.3f
    xs.forEach { x ->
        shape(S_WHITE, x - 7f * k, top, x + 5f * k, 50f, x - 7f * k, bot, x - 1f * k, bot, x + 11f * k, 50f, x - 1f * k, top, round = 0.3f)
    }
}
