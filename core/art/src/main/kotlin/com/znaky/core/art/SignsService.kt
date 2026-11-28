package com.znaky.core.art

/** Group 6: service signs, a blue plate with a white panel and a black pictogram. */
internal val serviceSigns: Map<String, SignArt> = linkedMapOf(
    "6.1" to { service { redCross(50f, 43f, 20f) } },
    "6.2" to { service { bed(); redCross(66f, 28f, 9f) } },
    "6.3" to { service { handset(46f, 40f, 0.9f); text("SOS", 50f, 63f, 9f, S_BLACK) } },
    "6.4" to {
        service {
            rect(42f, 28f, 58f, 66f, fill(S_RED), radius = 5f)
            rect(46f, 21f, 54f, 28f, fill(S_BLACK))
            path(ArtPath.svg("M54 23 L64 20 L66 24 Q60 26 62 38"), stroke(S_BLACK, 2.6f))
            rect(44f, 40f, 56f, 50f, fill(S_WHITE))
        }
    },
    "6.5" to {
        service {
            layer {
                rotate(-45f, 50f, 43f)
                rect(46f, 26f, 54f, 62f, fill(S_BLACK), radius = 2f)
                path(ArtPath.svg("M41 26 Q41 16 50 15 L50 22 L46 24 L46 28 L54 28 L54 24 L50 22 L50 15 Q59 16 59 26 Q59 32 54 33 L46 33 Q41 32 41 26 Z"), fill(S_BLACK))
            }
        }
    },
    "6.6" to {
        service {
            at(50f, 50f, 0.86f) { carSide(S_BLACK, S_WHITE) }
            for (x in listOf(34f, 46f, 58f, 70f)) path(ArtPath.svg("M$x 20 Q${x + 2.6f} 25 ${x + 2.6f} 27 A2.6 2.6 0 0 1 ${x - 2.6f} 27 Q${x - 2.6f} 25 $x 20 Z"), fill(S_BLACK))
        }
    },
    "6.7.1" to { service { at(50f, 43f, 1.05f) { fuelPump(S_BLACK, S_WHITE) } } },
    "6.7.2" to { service { at(50f, 43f, 1.05f) { fuelPump(S_GAS, S_WHITE) } } },
    "6.7.4" to { service { at(36f, 43f, 0.8f) { fuelPump(S_BLACK, S_WHITE) }; at(66f, 43f, 0.8f) { fuelPump(S_GAS, S_WHITE) } } },
    "6.8" to { service { handset(50f, 43f, 1.1f) } },
    "6.9" to { service { text("i", 50f, 43f, 34f, S_BLACK) } },
    "6.10" to {
        service {
            text("ПОЛІЦІЯ", 50f, 43f, 9f, S_BLACK, condensed = true)
        }
    },
    "6.11" to {
        service {
            at(37f, 46f, 0.68f) { person(S_BLACK) }
            at(63f, 46f, 0.68f) { person(S_BLACK, dress = true) }
            rect(49.2f, 22f, 50.8f, 66f, fill(S_BLACK))
        }
    },
    "6.12" to {
        service {
            path(ArtPath.svg("M28 30 L52 30 Q60 30 60 38 L60 44 L52 44 L52 40 Q52 38 50 38 L28 38 Z"), fill(S_BLACK))
            rect(36f, 24f, 42f, 30f, fill(S_BLACK)); rect(32f, 21f, 46f, 24f, fill(S_BLACK), radius = 1f)
            path(ArtPath.svg("M56 50 Q60 56 60 59 A4 4 0 0 1 52 59 Q52 56 56 50 Z"), fill(S_BLACK))
        }
    },
    "6.13" to {
        service {
            rect(38f, 34f, 41f, 64f, fill(S_BLACK), radius = 1.4f)
            for (x in listOf(33.4f, 37.8f, 42.2f, 46.6f)) rect(x - 0.8f, 20f, x + 0.8f, 32f, fill(S_BLACK))
            path(ArtPath.svg("M32.6 30 L47.4 30 Q47.4 37 39.5 37 Q32.6 37 32.6 30 Z"), fill(S_BLACK))
            path(ArtPath.svg("M60 64 L60 20 Q68 24 68 38 L64 42 L64 64 Z"), fill(S_BLACK))
        }
    },
    "6.14" to {
        service {
            path(ArtPath.svg("M30 32 L62 32 L60 54 Q58 60 52 60 L40 60 Q34 60 32 54 Z"), fill(S_BLACK))
            path(ArtPath.svg("M61 36 Q72 36 70 44 Q68 50 59 50"), stroke(S_BLACK, 3f))
            rect(26f, 62f, 66f, 65f, fill(S_BLACK), radius = 1.4f)
            for (x in listOf(40f, 48f)) path(ArtPath.svg("M$x 28 Q${x - 3} 24 $x 20 Q${x + 3} 16 $x 12"), stroke(S_BLACK, 1.8f))
        }
    },
    "6.15" to {
        service {
            path(ArtPath.svg("M30 66 L30 50 L22 50 L36 20 L50 50 L42 50 L42 66 Z"), fill(S_BLACK))
            rect(50f, 46f, 80f, 50f, fill(S_BLACK)); rect(54f, 50f, 57f, 66f, fill(S_BLACK)); rect(73f, 50f, 76f, 66f, fill(S_BLACK))
            rect(52f, 56f, 78f, 58f, fill(S_BLACK))
        }
    },
    "6.16" to { service { bed() } },
    "6.17" to {
        service {
            path(ArtPath.svg("M24 66 L24 42 L40 28 L56 42 L56 66 Z"), fill(S_BLACK))
            rect(36f, 52f, 44f, 66f, fill(S_WHITE))
            path(ArtPath.svg("M64 66 L64 54 L58 54 L68 30 L78 54 L72 54 L72 66 Z"), fill(S_BLACK))
        }
    },
    "6.18" to { service { tent(50f, 1f) } },
    "6.19" to { service { caravan(50f, 46f, 1f) } },
    "6.20" to { service { tent(34f, 0.7f); caravan(64f, 50f, 0.62f) } },
    "6.21" to {
        service {
            circle(34f, 34f, 4.6f, fill(S_BLACK))
            path(ArtPath.svg("M38 40 L60 32 L62 36 L44 44 L48 48 L42 52 Z"), fill(S_BLACK))
            for (i in 0 until 2) path(ArtPath.svg("M22 ${56 + i * 7} q4.5 -3.4 9 0 t9 0 t9 0 t9 0 t9 0 t9 0"), stroke(S_BLACK, 2.4f))
        }
    },
    "6.22" to {
        service {
            path(ArtPath.svg("M24 66 L24 28 L30 28 L30 34 L36 34 L36 28 L42 28 L42 40 L58 40 L58 28 L64 28 L64 34 L70 34 L70 28 L76 28 L76 66 Z"), fill(S_BLACK))
            path(ArtPath.svg("M44 66 L44 56 Q50 48 56 56 L56 66 Z"), fill(S_WHITE))
        }
    },
    "6.7.3" to { service { at(50f, 43f, 1.05f) { charger(S_EV) } } },
    "6.7.5" to { service { at(36f, 43f, 0.8f) { fuelPump(S_BLACK, S_WHITE) }; at(66f, 43f, 0.8f) { charger(S_EV) } } },
    "6.7.6" to { service { at(36f, 43f, 0.8f) { fuelPump(S_GAS, S_WHITE) }; at(66f, 43f, 0.8f) { charger(S_EV) } } },
    "6.7.7" to {
        service {
            at(30f, 43f, 0.62f) { fuelPump(S_BLACK, S_WHITE) }
            at(51f, 43f, 0.62f) { fuelPump(S_GAS, S_WHITE) }
            at(72f, 43f, 0.62f) { charger(S_EV) }
        }
    },
    "6.23" to {
        service {
            circle(50f, 43f, 22f, fill(S_BLACK))
            circle(50f, 43f, 12f, fill(S_WHITE))
            circle(50f, 43f, 5f, fill(S_BLACK))
            for (i in 0 until 12) {
                val a = Math.toRadians(i * 30.0)
                line(50f + 17f * kotlin.math.cos(a).toFloat(), 43f + 17f * kotlin.math.sin(a).toFloat(), 50f + 21f * kotlin.math.cos(a).toFloat(), 43f + 21f * kotlin.math.sin(a).toFloat(), stroke(S_WHITE, 1.6f))
            }
        }
    },
)

private fun ArtCanvas.service(content: ArtCanvas.() -> Unit) { serviceBlank(); content() }

private fun ArtCanvas.redCross(cx: Float, cy: Float, r: Float) {
    val t = r * 0.36f
    rect(cx - t, cy - r, cx + t, cy + r, fill(S_RED))
    rect(cx - r, cy - t, cx + r, cy + t, fill(S_RED))
}

private fun ArtCanvas.bed() {
    rect(24f, 34f, 28f, 66f, fill(S_BLACK))
    rect(24f, 50f, 76f, 58f, fill(S_BLACK))
    rect(72f, 50f, 76f, 66f, fill(S_BLACK))
    circle(35f, 44f, 5f, fill(S_BLACK))
    rect(42f, 42f, 76f, 50f, fill(S_BLACK), radius = 3f)
}

private fun ArtCanvas.handset(cx: Float, cy: Float, k: Float) = at(cx, cy, k) {
    path(
        ArtPath.svg("M-16 -12 Q-12 -18 -6 -14 L-2 -8 Q0 -5 -3 -3 L-6 -1 Q-3 6 3 9 L6 6 Q8 3 11 5 L16 9 Q20 14 14 18 Q4 22 -8 10 Q-20 -2 -16 -12 Z"),
        fill(S_BLACK),
    )
}

private fun ArtCanvas.person(ink: Long, dress: Boolean = false) {
    circle(0f, -22f, 5f, fill(ink))
    if (dress) path(ArtPath.svg("M-5 -15 L5 -15 L10 8 L-10 8 Z"), fill(ink)) else rect(-6f, -15f, 6f, 6f, fill(ink), radius = 2f)
    rect(-5f, 6f, -1.4f, 24f, fill(ink)); rect(1.4f, 6f, 5f, 24f, fill(ink))
}

private fun ArtCanvas.tent(cx: Float, k: Float) = at(cx, 44f, k) {
    path(ArtPath.svg("M-26 22 L0 -20 L26 22 Z"), fill(S_BLACK))
    path(ArtPath.svg("M-7 22 L0 6 L7 22 Z"), fill(S_WHITE))
}

private fun ArtCanvas.caravan(cx: Float, cy: Float, k: Float) = at(cx, cy, k) {
    rect(-24f, -14f, 18f, 10f, fill(S_BLACK), radius = 5f)
    rect(-16f, -8f, -4f, 0f, fill(S_WHITE), radius = 1f)
    rect(4f, -8f, 12f, 6f, fill(S_WHITE), radius = 1f)
    rect(18f, 4f, 28f, 6.4f, fill(S_BLACK))
    circle(-6f, 12f, 5f, fill(S_WHITE)); circle(-6f, 12f, 4f, fill(S_BLACK))
}


/** Light blue of gas symbols and green of charging symbols on 6.7.x (ПДР 33, 6.7). */
private const val S_GAS = 0xFF2E9FE0
private const val S_EV = 0xFF169B4E

/** EV charging post with a plug cable and a lightning bolt, centred on the origin. */
private fun ArtCanvas.charger(ink: Long) {
    rect(-14f, -18f, 4f, 18f, fill(ink), radius = 2f)
    path(ArtPath.svg("M-3 -12 L-9 0 L-5 0 L-7 10 L0 -3 L-4 -3 L-1 -12 Z"), fill(S_WHITE))
    rect(-17f, 15f, 7f, 19f, fill(ink), radius = 1f)
    path(ArtPath.svg("M4 -6 L9 -6 Q11 -6 11 -4 L11 8 Q11 11 13.6 11 Q16 11 16 8 L16 -8"), stroke(ink, 2.4f))
    rect(13f, -14f, 19f, -8f, fill(ink), radius = 1f)
    line(14.4f, -17f, 14.4f, -14f, stroke(ink, 1.4f)); line(17.6f, -17f, 17.6f, -14f, stroke(ink, 1.4f))
}
