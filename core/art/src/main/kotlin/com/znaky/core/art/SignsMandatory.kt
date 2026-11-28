package com.znaky.core.art

/** Group 4: mandatory signs, white pictograms on a blue disc. */
internal val mandatorySigns: Map<String, SignArt> = linkedMapOf(
    "4.1" to { mandatory { arrow(50f, 82f, 50f, 17f, 11f, 30f, 24f, S_WHITE) } },
    "4.2" to { mandatory { bentArrow(38f, 82f, 82f, 44f, 1, 11f, 30f, 22f, S_WHITE) } },
    "4.3" to { mandatory { bentArrow(62f, 82f, 18f, 44f, -1, 11f, 30f, 22f, S_WHITE) } },
    "4.4" to {
        mandatory {
            arrow(42f, 84f, 42f, 16f, 10f, 26f, 20f, S_WHITE)
            bentArrow(42f, 74f, 84f, 52f, 1, 10f, 26f, 19f, S_WHITE, r = 10f)
        }
    },
    "4.5" to {
        mandatory {
            arrow(58f, 84f, 58f, 16f, 10f, 26f, 20f, S_WHITE)
            bentArrow(58f, 74f, 16f, 52f, -1, 10f, 26f, 19f, S_WHITE, r = 10f)
        }
    },
    "4.6" to {
        mandatory {
            bentArrow(50f, 84f, 84f, 44f, 1, 10f, 26f, 19f, S_WHITE, r = 12f)
            bentArrow(50f, 84f, 16f, 44f, -1, 10f, 26f, 19f, S_WHITE, r = 12f)
        }
    },
    "4.7" to { mandatory { arrow(28f, 28f, 74f, 74f, 11f, 30f, 22f, S_WHITE) } },
    "4.8" to { mandatory { arrow(72f, 28f, 26f, 74f, 11f, 30f, 22f, S_WHITE) } },
    "4.9" to {
        mandatory {
            arrow(50f, 40f, 24f, 70f, 9f, 24f, 18f, S_WHITE)
            arrow(50f, 40f, 76f, 70f, 9f, 24f, 18f, S_WHITE)
            rect(45.5f, 18f, 54.5f, 42f, fill(S_WHITE))
            circle(50f, 40f, 4.5f, fill(S_WHITE))
        }
    },
    "4.10" to {
        mandatory {
            roundaboutArrows(50f, 50f, 25f, 8f, 21f, 14f, S_WHITE)
        }
    },
    "4.11" to { mandatory { at(50f, 52f, 1.3f) { carSide(S_WHITE, S_BLUE) } } },
    "4.14" to { mandatory { at(50f, 52f, 1.36f) { bicycle(S_WHITE, 3f) } } },
    "4.16" to { mandatory { at(50f, 50f, 1.4f) { pedestrian(S_WHITE) } } },
    "4.17" to {
        mandatory {
            at(50f, 34f, 0.95f) { bicycle(S_WHITE, 2.6f) }
            rect(18f, 48.8f, 82f, 51.2f, fill(S_WHITE))
            at(50f, 72f, 0.72f) { pedestrian(S_WHITE) }
        }
    },
    "4.19" to { mandatory { at(48f, 56f, 1.25f) { horseRider(S_WHITE) } } },
    "4.12" to { mandatory { text("30", 50f, 50f, 34f, S_WHITE, condensed = true) } },
    "4.13" to {
        mandatory {
            text("30", 50f, 50f, 34f, S_WHITE, condensed = true)
            layer { clip(ArtPath.circle(50f, 50f, 46.8f)); line(84f, 16f, 16f, 84f, stroke(S_RED, 6f, cap = Cap.BUTT)) }
        }
    },
    "4.20.1" to { mandatory { dangerTruck(); arrow(50f, 86f, 50f, 54f, 6f, 16f, 11f, S_WHITE) } },
    "4.20.2" to { mandatory { dangerTruck(); bentArrow(42f, 88f, 76f, 66f, 1, 6f, 16f, 11f, S_WHITE, r = 8f) } },
    "4.20.3" to { mandatory { dangerTruck(); bentArrow(58f, 88f, 24f, 66f, -1, 6f, 16f, 11f, S_WHITE, r = 8f) } },
    "4.15" to {
        mandatory { at(50f, 52f, 1.36f) { bicycle(S_WHITE, 3f) } }
        layer { clip(ArtPath.circle(50f, 50f, 46.8f)); line(84f, 16f, 16f, 84f, stroke(S_RED, 6f, cap = Cap.BUTT)) }
    },
    "4.18" to {
        rectBlank(84f, 96f, S_BLUE)
        rect(48.8f, 10f, 51.2f, 90f, fill(S_WHITE))
        at(29f, 50f, 1.05f) { pedestrian(S_WHITE) }
        at(71f, 52f, 0.86f) { bicycle(S_WHITE, 2.8f) }
    },
    "4.21" to { mandatory { chainWheel() } },
    "4.22" to {
        mandatory { chainWheel() }
        layer { clip(ArtPath.circle(50f, 50f, 46.8f)); line(84f, 16f, 16f, 84f, stroke(S_RED, 6f, cap = Cap.BUTT)) }
    },
    "4.23" to { carAndBike(end = false) },
    "4.24" to { carAndBike(end = true) },
)

private fun ArtCanvas.dangerTruck() {
    at(50f, 33f, 0.92f) { truckSide(S_WHITE, S_BLUE, plate = S_ORANGE) }
}


/** 4.21, 4.22: a tyre with snow chains across the tread. */
private fun ArtCanvas.chainWheel() {
    circle(50f, 50f, 26f, stroke(S_WHITE, 9f))
    circle(50f, 50f, 9f, fill(S_WHITE))
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0)
        val c = kotlin.math.cos(a).toFloat(); val s = kotlin.math.sin(a).toFloat()
        line(50f + 18f * c - 5f * s, 50f + 18f * s + 5f * c, 50f + 34f * c + 5f * s, 50f + 34f * s - 5f * c, stroke(S_WHITE, 2.2f))
    }
}

/** 4.23, 4.24: road shared by cars and cyclists; the end sign is crossed out. */
private fun ArtCanvas.carAndBike(end: Boolean) {
    squareBlank(S_BLUE)
    at(50f, 34f, 1.05f) { carSide(S_WHITE, S_BLUE) }
    at(50f, 68f, 0.95f) { bicycle(S_WHITE, 2.8f) }
    if (end) layer {
        clip(ArtPath.roundRect(3.8f, 3.8f, 96.2f, 96.2f, 7.5f))
        line(2f, 98f, 98f, 2f, stroke(S_RED, 6.4f, cap = Cap.BUTT))
    }
}
