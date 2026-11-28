package com.znaky.core.art

/** Group 7: plates that go under a sign and narrow or explain it. White, black frame. */
internal val plateSigns: Map<String, SignArt> = linkedMapOf(
    "7.1.1" to { plate { label("150 м") } },
    "7.1.2" to { plate { text("STOP", 26f, 50f, 7.4f, S_BLACK); label("200 м", x = 64f, size = 12f) } },
    "7.1.3" to { plate { label("60 м", x = 42f); arrow(64f, 50f, 88f, 50f, 3f, 11f, 8f, S_BLACK) } },
    "7.1.4" to { plate { arrow(36f, 50f, 12f, 50f, 3f, 11f, 8f, S_BLACK); label("60 м", x = 58f) } },
    "7.2.1" to { plate { vArrows(24f, up = true, down = true); label("2 км", x = 58f) } },
    "7.2.2" to { plate { vArrows(24f, up = false, down = true); label("15 м", x = 58f) } },
    "7.2.3" to { plate { vArrows(50f, up = true, down = false) } },
    "7.2.4" to { plate { vArrows(50f, up = true, down = true) } },
    "7.2.5" to {
        plate {
            line(14f, 50f, 86f, 50f, stroke(S_BLACK, 3f, cap = Cap.BUTT))
            arrowHead(10f, 50f, 180f, 11f, 8f, S_BLACK); arrowHead(90f, 50f, 0f, 11f, 8f, S_BLACK)
            text("15 м", 50f, 40f, 8f, S_BLACK, condensed = true)
        }
    },
    "7.2.6" to {
        plate {
            line(12f, 50f, 84f, 50f, stroke(S_BLACK, 3f, cap = Cap.BUTT))
            arrowHead(90f, 50f, 0f, 11f, 8f, S_BLACK)
            text("15 м", 50f, 40f, 8f, S_BLACK, condensed = true)
        }
    },
    "7.3.1" to { plate { tArrows(left = true, straight = false, right = true) } },
    "7.3.2" to { plate { tArrows(left = false, straight = true, right = true) } },
    "7.3.3" to { plate { tArrows(left = true, straight = true, right = false) } },
    "7.4.1" to { plate { label("Сб, Нд, свята", size = 7.6f) } },
    "7.4.2" to { plate { hammers() } },
    "7.4.3" to { plate { label("Пн, Ср, Пт", size = 10f) } },
    "7.4.4" to { plate { label("8.00 – 17.30", size = 10f) } },
    "7.4.5" to { plate(h = 60f) { hammers(cy = 38f, k = 0.7f); text("8.00 – 17.30", 50f, 64f, 8f, S_BLACK, condensed = true) } },
    "7.4.6" to { plate(h = 60f) { text("Сб, Нд", 50f, 38f, 8f, S_BLACK, condensed = true); text("10.00 – 15.00", 50f, 64f, 8f, S_BLACK, condensed = true) } },
    "7.4.7" to { plate(h = 60f) { text("Пн – Пт  8.00 – 18.00", 50f, 40f, 5.6f, S_BLACK, condensed = true); text("Сб  9.00 – 14.00", 50f, 60f, 5.6f, S_BLACK, condensed = true) } },
    "7.5.1" to { plate { at(50f, 50f, 0.86f) { truckSide(S_BLACK, S_WHITE) } } },
    "7.5.2" to { plate { at(56f, 50f, 0.72f) { truckSide(S_BLACK, S_WHITE, trailer = true) } } },
    "7.5.3" to { plate { at(50f, 51f, 0.92f) { carSide(S_BLACK, S_WHITE) } } },
    "7.5.4" to { plate { at(50f, 50f, 0.86f) { busSide(S_BLACK, S_WHITE) } } },
    "7.5.5" to { plate { at(50f, 52f, 0.7f) { tractorSide(S_BLACK, S_WHITE) } } },
    "7.5.6" to { plate { at(50f, 52f, 0.86f) { motoSide(S_BLACK, S_WHITE) } } },
    "7.5.7" to { plate { at(50f, 52f, 0.86f) { bicycle(S_BLACK, 2.6f) } } },
    "7.5.8" to { plate { at(50f, 50f, 0.86f) { truckSide(S_BLACK, S_WHITE, plate = S_ORANGE) } } },
    "7.6.1" to { parkingMethod(0) },
    "7.6.2" to { parkingMethod(1) },
    "7.6.3" to { parkingMethod(2) },
    "7.6.4" to { parkingMethod(3) },
    "7.6.5" to { parkingMethod(4) },
    "7.6.6" to { parkingMethod(5) },
    "7.6.7" to { parkingMethod(6) },
    "7.7" to {
        plate {
            at(40f, 52f, 0.72f) { carSide(S_BLACK, S_WHITE) }
            circle(74f, 50f, 9f, stroke(S_BLACK, 2f))
            path(ArtPath.svg("M70 46 L78 46 L78 54 L70 54 Z"), fill(S_BLACK))
            line(66f, 58f, 82f, 42f, stroke(S_RED, 2.6f))
        }
    },
    "7.8" to {
        plate(h = 70f, w = 60f) {
            rect(47f, 52f, 53f, 80f, fill(S_BLACK)); path(ArtPath.svg("M50 55 L50 38 L70 24"), stroke(S_BLACK, 6f, cap = Cap.BUTT))
            line(30f, 55f, 70f, 55f, stroke(S_BLACK, 2f))
        }
    },
    "7.9" to {
        plate {
            for (x in listOf(36f, 64f)) rect(x - 0.8f, 30f, x + 0.8f, 70f, fill(S_BLACK))
            for (x in listOf(20f, 80f)) arrow(x, 66f, x, 34f, 2.4f, 8f, 6f, S_BLACK)
            arrow(50f, 68f, 50f, 30f, 5f, 15f, 10f, S_BLACK)
        }
    },
    "7.10" to { plate { label("3", size = 22f) } },
    "7.11" to {
        plate {
            path(ArtPath.svg("M18 52 L82 52 L76 62 L24 62 Z"), fill(S_BLACK))
            at(50f, 44f, 0.5f) { carSide(S_BLACK, S_WHITE) }
            path(ArtPath.svg("M14 68 q4 -3 8 0 t8 0 t8 0 t8 0 t8 0 t8 0 t8 0 t8 0 t8 0"), stroke(S_BLACK, 1.8f))
        }
    },
    "7.12" to { plate { snowflake(50f, 50f, 15f) } },
    "7.13" to {
        plate {
            path(ArtPath.svg("M32 46 Q32 34 42 34 Q46 26 56 28 Q66 28 66 38 Q72 38 72 44 Q72 50 66 50 L36 50 Q32 50 32 46 Z"), fill(S_BLACK))
            for ((i, x) in listOf(38f, 48f, 58f, 68f).withIndex()) line(x, 55f + (i % 2) * 2f, x - 3f, 64f + (i % 2) * 2f, stroke(S_BLACK, 2.2f))
        }
    },
    "7.14" to { plate { label("ПЛАТНО ₴", size = 10f) } },
    "7.15" to {
        plate {
            path(ArtPath.svg("M14 64 L30 48 L70 48 L86 64 L80 64 L68 54 L32 54 L20 64 Z"), fill(S_BLACK))
            at(50f, 40f, 0.5f) { carSide(S_BLACK, S_WHITE) }
            rect(40f, 58f, 60f, 66f, fill(S_BLACK))
        }
    },
    "7.16" to {
        plate {
            at(44f, 50f, 0.66f) { pedestrian(S_BLACK) }
            line(50f, 48f, 64f, 66f, stroke(S_BLACK, 1.8f))
            circle(70f, 38f, 5f, fill(S_BLACK)); circle(70f, 38f, 2f, fill(S_WHITE))
        }
    },
    "7.17" to { plate { at(50f, 52f, 0.78f) { wheelchair(S_BLACK, S_WHITE) } } },
    "7.18" to { plate { text("КРІМ", 32f, 50f, 9f, S_BLACK, condensed = true); at(66f, 52f, 0.7f) { wheelchair(S_BLACK, S_WHITE) } } },
    "7.19" to {
        plate {
            circle(32f, 50f, 11f, stroke(S_BLACK, 2.4f))
            poly(S_BLACK, 2.2f, 32f, 43f, 32f, 50f, 37f, 53f)
            label("2 год", x = 66f, size = 11f)
        }
    },
    "7.21.1" to { plate { at(30f, 50f, 0.6f) { carSide(S_BLACK, S_WHITE) }; mirrored { at(30f, 50f, 0.6f) { carSide(S_BLACK, S_WHITE) } }; spark(50f, 42f) } },
    "7.21.2" to { plate { at(30f, 50f, 0.6f) { carSide(S_BLACK, S_WHITE) }; at(64f, 50f, 0.6f) { carSide(S_BLACK, S_WHITE) }; spark(48f, 40f) } },
    "7.21.3" to { plate { at(32f, 52f, 0.6f) { carSide(S_BLACK, S_WHITE) }; at(70f, 50f, 0.66f) { pedestrian(S_BLACK, facing = -1) }; spark(54f, 40f) } },
    "7.21.4" to { plate { at(50f, 44f, 0.66f) { layer { rotate(180f); carSide(S_BLACK, S_WHITE) } }; rect(18f, 58f, 82f, 60f, fill(S_BLACK)) } },
    "7.22" to {
        plate {
            circle(46f, 31f, 4f, fill(S_BLACK))
            path(ArtPath.svg("M42 36 L50 38 L56 48 L50 50 L46 44 L42 54 L48 58 L44 62 L36 54 Z"), fill(S_BLACK))
            line(22f, 60f, 76f, 70f, stroke(S_BLACK, 2.4f))
            line(30f, 40f, 40f, 66f, stroke(S_BLACK, 1.6f))
            line(58f, 40f, 62f, 66f, stroke(S_BLACK, 1.6f))
        }
    },
    "7.2.7" to {
        plate {
            line(14f, 50f, 86f, 50f, stroke(S_BLACK, 3f, cap = Cap.BUTT))
            arrowHead(10f, 50f, 180f, 11f, 8f, S_BLACK); arrowHead(90f, 50f, 0f, 11f, 8f, S_BLACK)
            rect(48.5f, 38f, 51.5f, 62f, fill(S_BLACK))
            text("15 м", 30f, 40f, 7f, S_BLACK, condensed = true); text("15 м", 70f, 40f, 7f, S_BLACK, condensed = true)
        }
    },
    "7.5.9" to { plate { rect(30f, 36f, 70f, 64f, fill(S_BLUE), radius = 2f); text("TIR", 50f, 50f, 14f, S_WHITE) } },
            "7.20" to {
        plate(h = 60f) {
            rect(22f, 30f, 28f, 62f, stroke(S_BLACK, 1.6f), radius = 3f)
            circle(25f, 66f, 6f, fill(S_BLACK)); rect(23.6f, 44f, 26.4f, 64f, fill(S_BLACK))
            text("> 28°C", 60f, 50f, 14f, S_BLACK, condensed = true)
        }
    },
    "7.21.5" to { plate { at(32f, 52f, 0.6f) { carSide(S_BLACK, S_WHITE) }; at(70f, 52f, 0.56f) { bicycle(S_BLACK, 3f) }; spark(52f, 40f) } },
    "7.23" to {
        plate {
            rect(36f, 34f, 52f, 68f, fill(S_BLACK), radius = 2f)
            path(ArtPath.svg("M46 38 L40 50 L44 50 L42 62 L49 47 L45 47 L48 38 Z"), fill(S_WHITE))
            path(ArtPath.svg("M52 44 L58 44 Q60 44 60 46 L60 58 Q60 62 63 62 Q66 62 66 58 L66 40"), stroke(S_BLACK, 2.4f))
        }
    },
    "7.24" to {
        plate {
            rect(16f, 40f, 46f, 60f, fill(S_BLACK), radius = 2f); rect(20f, 43f, 30f, 50f, fill(S_WHITE))
            rect(44f, 54f, 66f, 58f, fill(S_BLACK)); line(46f, 54f, 66f, 36f, stroke(S_BLACK, 2.4f))
            path(ArtPath.svg("M66 36 L66 44"), stroke(S_BLACK, 1.6f))
            at(76f, 46f, 0.42f) { layer { rotate(-20f); carSide(S_BLACK, S_WHITE) } }
            circle(24f, 62f, 4f, fill(S_BLACK)); circle(40f, 62f, 4f, fill(S_BLACK))
        }
    },
    "7.25" to {
        plate {
            path(ArtPath.svg("M50 32 L60 50 L50 68 L40 50 Z"), fill(S_BLACK))
            arrow(36f, 66f, 24f, 34f, 3f, 9f, 6f, S_BLACK); arrow(64f, 66f, 76f, 34f, 3f, 9f, 6f, S_BLACK)
        }
    },
    "7.26" to {
        plate {
            path(ArtPath.svg("M16 42 Q16 34 26 34 L30 34 L30 66 L26 66 Q16 66 16 58 Z"), fill(S_BLACK))
            for (i in 0 until 3) line(34f, 40f + i * 10f, 42f, 40f + i * 10f, stroke(S_BLACK, 2.2f))
            text("01.10 – 30.04", 69f, 50f, 6f, S_BLACK, condensed = true)
        }
    },
    "7.27" to {
        plate {
            rect(30f, 38f, 58f, 58f, fill(S_BLACK), radius = 3f)
            circle(36f, 62f, 4f, fill(S_BLACK)); circle(52f, 62f, 4f, fill(S_BLACK))
            line(58f, 44f, 70f, 44f, stroke(S_BLACK, 2.4f))
            for (i in 0 until 3) rect(16f + i * 0f, 68f, 84f, 70f, fill(S_BLACK))
            rect(64f, 50f, 84f, 54f, fill(S_BLACK))
        }
    },
    "7.28.1" to { plate { circle(50f, 50f, 15f, fill(S_RED)); text("M", 50f, 50f, 18f, S_WHITE) } },
    "7.28.2" to { plate { at(50f, 50f, 0.86f) { busSide(S_BLACK, S_WHITE) } } },
    "7.28.3" to { plate { at(50f, 50f, 0.66f) { tramFront(S_BLACK, S_WHITE) } } },
    "7.28.4" to { plate { at(50f, 52f, 0.8f) { trolleybusSide(S_BLACK, S_WHITE) } } },
    "7.29.1" to { bikeDirection { arrow(72f, 64f, 72f, 32f, 3f, 10f, 7f, S_BLACK) } },
    "7.29.2" to { bikeDirection { arrow(72f, 64f, 72f, 32f, 3f, 10f, 7f, S_BLACK); arrow(84f, 32f, 84f, 64f, 3f, 10f, 7f, S_BLACK) } },
    "7.29.3" to { bikeDirection { arrow(62f, 50f, 88f, 50f, 3f, 10f, 7f, S_BLACK) } },
    "7.29.4" to { bikeDirection { arrow(88f, 50f, 62f, 50f, 3f, 10f, 7f, S_BLACK) } },
    "7.29.5" to { bikeDirection { arrow(62f, 44f, 88f, 44f, 3f, 10f, 7f, S_BLACK); arrow(88f, 58f, 62f, 58f, 3f, 10f, 7f, S_BLACK) } },
    "7.29.6" to { bikeDirection { bentArrow(68f, 66f, 90f, 40f, 1, 3f, 10f, 7f, S_BLACK, r = 6f) } },
    "7.30" to {
        plate {
            rect(30f, 30f, 70f, 70f, fill(S_YELLOW), radius = 3f)
            circle(50f, 44f, 8f, fill(S_BLACK))
            path(ArtPath.svg("M38 66 Q38 54 50 54 Q62 54 62 66 Z"), fill(S_BLACK))
        }
    },
)

private fun ArtCanvas.label(s: String, x: Float = 50f, size: Float = 14f) = text(s, x, 50f, size, S_BLACK, condensed = true)

private fun ArtCanvas.vArrows(x: Float, up: Boolean, down: Boolean) {
    line(x, if (up) 34f else 30f, x, if (down) 66f else 70f, stroke(S_BLACK, 3f, cap = Cap.BUTT))
    if (up) arrowHead(x, 29f, 270f, 11f, 8f, S_BLACK)
    if (down) arrowHead(x, 71f, 90f, 11f, 8f, S_BLACK)
}

private fun ArtCanvas.tArrows(left: Boolean, straight: Boolean, right: Boolean) {
    rect(48f, 50f, 52f, 70f, fill(S_BLACK))
    if (left) { rect(26f, 48f, 52f, 52f, fill(S_BLACK)); arrowHead(18f, 50f, 180f, 11f, 9f, S_BLACK) }
    if (right) { rect(48f, 48f, 74f, 52f, fill(S_BLACK)); arrowHead(82f, 50f, 0f, 11f, 9f, S_BLACK) }
    if (straight) { rect(48f, 38f, 52f, 52f, fill(S_BLACK)); arrowHead(50f, 30f, 270f, 11f, 9f, S_BLACK) }
}

/** Crossed hammers: working days. */
private fun ArtCanvas.hammers(cy: Float = 50f, k: Float = 1f) = at(50f, cy, k) {
    for (s in listOf(-1f, 1f)) layer {
        rotate(45f * s)
        rect(-1.6f, -14f, 1.6f, 14f, fill(S_BLACK))
        rect(-6f, -16f, 6f, -10f, fill(S_BLACK), radius = 1f)
    }
}

/** 7.6.x: how to park relative to the kerb (drawn as the grey line at the top). */
private fun ArtCanvas.parkingMethod(kind: Int) {
    plate(h = 60f, w = 60f) {
        rect(26f, 30f, 74f, 33f, fill(S_BLACK))
        when (kind) {
            0 -> at(50f, 48f, 0.7f) { carSide(S_BLACK, S_WHITE) }
            1 -> { rect(26f, 36f, 74f, 37f, fill(S_GREY)); at(50f, 46f, 0.7f) { carSide(S_BLACK, S_WHITE) } }
            2 -> { at(50f, 44f, 0.7f) { carSide(S_BLACK, S_WHITE) }; rect(26f, 51f, 74f, 52f, fill(S_GREY)) }
            // 3: across, on the roadway; 4: across, half on the sidewalk; 5: across, on the sidewalk
            3 -> for (x in listOf(36f, 50f, 64f)) at(x, 50f, 0.42f) { layer { rotate(-90f); carSide(S_BLACK, S_WHITE) } }
            4 -> {
                rect(26f, 49.5f, 74f, 50.5f, fill(S_GREY))
                for (x in listOf(36f, 50f, 64f)) at(x, 50f, 0.42f) { layer { rotate(-90f); carSide(S_BLACK, S_WHITE) } }
            }
            5 -> {
                for (x in listOf(36f, 50f, 64f)) at(x, 48f, 0.38f) { layer { rotate(-90f); carSide(S_BLACK, S_WHITE) } }
                rect(26f, 66f, 74f, 67f, fill(S_GREY))
            }
            // 6: at an angle of 30°, 45° or 60°
            else -> for (x in listOf(36f, 52f, 68f)) at(x, 50f, 0.42f) { layer { rotate(-60f); carSide(S_BLACK, S_WHITE) } }
        }
    }
}

private fun ArtCanvas.snowflake(cx: Float, cy: Float, r: Float) {
    for (i in 0 until 3) layer {
        rotate(i * 60f, cx, cy)
        line(cx, cy - r, cx, cy + r, stroke(S_BLACK, 2.4f))
        for (s in listOf(-1f, 1f)) {
            line(cx, cy + s * r * 0.6f, cx - 4f, cy + s * (r * 0.6f + 4f), stroke(S_BLACK, 1.8f))
            line(cx, cy + s * r * 0.6f, cx + 4f, cy + s * (r * 0.6f + 4f), stroke(S_BLACK, 1.8f))
        }
    }
}

private fun ArtCanvas.spark(x: Float, y: Float) {
    for (i in 0 until 4) layer { rotate(i * 45f, x, y); line(x - 5f, y, x + 5f, y, stroke(S_RED, 2f)) }
}


/** 7.29.x: plate with a bicycle on the left and the direction on the right. */
private fun ArtCanvas.bikeDirection(arrows: ArtCanvas.() -> Unit) = plate {
    at(32f, 50f, 0.66f) { bicycle(S_BLACK, 3f) }
    arrows()
}
