package com.znaky.core.art

/** Group 5: information and direction signs. */
internal val infoSigns: Map<String, SignArt> = linkedMapOf(
    "5.1" to { motorway() },
    "5.2" to { motorway(); endSlash() },
    "5.3" to { squareBlank(S_GREEN); at(50f, 52f, 1.4f) { carSide(S_WHITE, S_GREEN) } },
    "5.4" to { squareBlank(S_GREEN); at(50f, 52f, 1.4f) { carSide(S_WHITE, S_GREEN) }; endSlash() },
    "5.5" to { squareBlank(S_BLUE); arrow(50f, 86f, 50f, 14f, 12f, 32f, 26f, S_WHITE) },
    "5.6" to { squareBlank(S_BLUE); arrow(50f, 86f, 50f, 14f, 12f, 32f, 26f, S_WHITE); endSlash() },
    "5.7.1" to { rectBlank(96f, 48f, S_BLUE); arrow(12f, 50f, 88f, 50f, 9f, 26f, 20f, S_WHITE) },
    "5.7.2" to { rectBlank(96f, 48f, S_BLUE); arrow(88f, 50f, 12f, 50f, 9f, 26f, 20f, S_WHITE) },
    "5.8" to { busLaneRoad() },
    "5.9" to { busLaneRoad(); endSlash() },
    "5.10.1" to {
        rectBlank(96f, 60f, S_BLUE)
        arrow(12f, 58f, 88f, 58f, 7f, 20f, 15f, S_WHITE)
        rect(10f, 45.4f, 90f, 46.6f, fill(S_WHITE))
        arrow(84f, 34f, 34f, 34f, 5f, 15f, 11f, S_WHITE)
        at(22f, 34f, 0.32f) { busSide(S_WHITE, S_BLUE) }
    },
    "5.10.2" to {
        rectBlank(96f, 60f, S_BLUE)
        arrow(88f, 58f, 12f, 58f, 7f, 20f, 15f, S_WHITE)
        rect(10f, 45.4f, 90f, 46.6f, fill(S_WHITE))
        arrow(16f, 34f, 66f, 34f, 5f, 15f, 11f, S_WHITE)
        at(78f, 34f, 0.32f) { busSide(S_WHITE, S_BLUE) }
    },
    "5.11" to { busLane() },
    "5.12" to { busLane(); endSlash() },
    "5.13" to { reversible() },
    "5.14" to { reversible(); endSlash() },
    "5.15" to {
        rectBlank(96f, 56f, S_BLUE)
        arrow(12f, 62f, 88f, 62f, 6f, 18f, 13f, S_WHITE)
        arrow(26f, 38f, 12f, 38f, 6f, 18f, 13f, S_WHITE)
        arrow(26f, 38f, 88f, 38f, 6f, 18f, 13f, S_WHITE)
    },
    "5.16" to {
        rectBlank(84f, 96f, S_BLUE)
        bentArrow(28f, 88f, 12f, 30f, -1, 6.4f, 17f, 12f, S_WHITE, r = 8f)
        arrow(50f, 88f, 50f, 14f, 6.4f, 17f, 13f, S_WHITE)
        arrow(72f, 88f, 72f, 14f, 6.4f, 17f, 13f, S_WHITE)
        bentArrow(72f, 70f, 90f, 48f, 1, 6.4f, 17f, 10f, S_WHITE, r = 7f)
    },
    "5.17.1" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(28f, 14f, 28f, 86f, 6.4f, 17f, 13f, S_WHITE)
        arrow(50f, 86f, 50f, 14f, 6.4f, 17f, 13f, S_WHITE)
        arrow(72f, 86f, 72f, 14f, 6.4f, 17f, 13f, S_WHITE)
        rect(38.2f, 10f, 39.8f, 90f, fill(S_WHITE))
    },
    "5.17.2" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(28f, 14f, 28f, 86f, 6.4f, 17f, 13f, S_WHITE)
        arrow(50f, 14f, 50f, 86f, 6.4f, 17f, 13f, S_WHITE)
        arrow(72f, 86f, 72f, 14f, 6.4f, 17f, 13f, S_WHITE)
        rect(60.2f, 10f, 61.8f, 90f, fill(S_WHITE))
    },
    "5.18" to { squareBlank(S_BLUE); bentArrow(58f, 88f, 18f, 42f, -1, 11f, 28f, 20f, S_WHITE) },
    "5.19" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(30f, 88f, 30f, 36f, 6.4f, 17f, 13f, S_WHITE)
        arrow(50f, 88f, 50f, 36f, 6.4f, 17f, 13f, S_WHITE)
        arrow(70f, 88f, 70f, 36f, 6.4f, 17f, 13f, S_WHITE)
        at(70f, 20f, 0.36f) { busSide(S_WHITE, S_BLUE) }
    },
    "5.20.1" to { laneStart(right = true) },
    "5.20.2" to {
        laneStart(right = true)
        circle(34f, 26f, 10f, fill(S_WHITE)); circle(34f, 26f, 8.6f, fill(S_BLUE))
        text("50", 34f, 26f, 8f, S_WHITE, condensed = true)
    },
    "5.20.3" to { laneStart(right = false) },
    "5.21.1" to { laneEnd(right = true) },
    "5.21.2" to { laneEnd(right = false) },
    "5.22" to {
        squareBlank(S_BLUE)
        arrow(40f, 88f, 40f, 14f, 9f, 24f, 18f, S_WHITE)
        path(ArtPath.svg("M78 88 L78 62 Q78 50 56 42"), stroke(S_WHITE, 7f, cap = Cap.BUTT))
    },
    "5.23" to {
        squareBlank(S_BLUE)
        arrow(34f, 88f, 34f, 14f, 9f, 24f, 18f, S_WHITE)
        arrow(66f, 88f, 66f, 14f, 9f, 24f, 18f, S_WHITE)
        rect(49.2f, 10f, 50.8f, 90f, fill(S_WHITE))
    },
    "5.24.1" to { crossover(left = true) },
    "5.24.2" to { crossover(left = false) },
    "5.25" to {
        squareBlank(S_BLUE)
        path(ArtPath.svg("M30 90 L30 12 L48 12 L48 90 Z"), fill(S_WHITE))
        path(ArtPath.svg("M48 74 Q66 64 72 30 L86 30 Q80 70 48 90 Z"), fill(S_WHITE))
        for (i in 0 until 6) circle(74f + (i % 2) * 6f, 22f + (i / 2) * 3.4f, 1.4f, fill(S_WHITE))
        at(39f, 40f, 0.3f) { layer { rotate(-90f); carSide(S_BLUE, S_WHITE) } }
    },
    "5.26" to { squareBlank(S_BLUE); uTurnArrow(66f, 86f, 74f, 32f, 11f, 28f, 20f, S_WHITE) },
    "5.27" to {
        rectBlank(80f, 96f, S_BLUE)
        uTurnArrow(64f, 70f, 60f, 28f, 9f, 24f, 17f, S_WHITE)
        text("200 м", 50f, 84f, 10f, S_WHITE, condensed = true)
    },
    "5.28.1" to { truckDirection(0) },
    "5.28.2" to { truckDirection(1) },
    "5.28.3" to { truckDirection(-1) },
    "5.29.1" to { squareBlank(S_BLUE); rect(42f, 34f, 58f, 88f, fill(S_WHITE)); rect(26f, 16f, 74f, 30f, fill(S_RED)) },
    "5.29.2" to { deadEndTurn(1) },
    "5.29.3" to { deadEndTurn(-1) },
    "5.30" to { squareBlank(S_BLUE); text("50", 50f, 50f, 36f, S_WHITE, condensed = true) },
    "5.31" to { livingZone() },
    "5.32" to { livingZone(); endSlash(w = 80f) },
    "5.33" to { pedestrianZone() },
    "5.34" to { pedestrianZone(); endSlash() },
    "5.35.1" to { crossingSign() },
    "5.35.2" to { mirrored { crossingSign() } },
    "5.36.1" to { stairsSign(down = true) },
    "5.36.2" to { mirrored { stairsSign(down = true) } },
    "5.37.1" to { stairsSign(down = false) },
    "5.37.2" to { mirrored { stairsSign(down = false) } },
    "5.38" to { squareBlank(S_BLUE); text("P", 50f, 50f, 56f, S_WHITE) },
    "5.39" to { parkingZone(end = false) },
    "5.40" to { parkingZone(end = true) },
    "5.41.1" to { stopSign { busSide(S_BLACK, S_WHITE) } },
    "5.41.2" to { stopSign { busSide(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.42.1" to { stopSign(k = 0.86f) { tramFront(S_BLACK, S_WHITE) } },
    "5.42.2" to { stopSign(k = 0.86f) { tramFront(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.43.1" to { stopSign(k = 0.84f, dy = 4f) { trolleybusSide(S_BLACK, S_WHITE) } },
    "5.43.2" to { stopSign(k = 0.84f, dy = 4f) { trolleybusSide(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.44" to {
        squareBlank(S_BLUE)
        rect(14f, 30f, 86f, 70f, fill(S_YELLOW), radius = 4f)
        for (i in 0 until 6) rect(18f + i * 11f, 32f + (i % 2) * 4f, 22f + i * 11f, 36f + (i % 2) * 4f, fill(S_BLACK))
        text("TAXI", 50f, 56f, 15f, S_BLACK)
    },
    "5.45" to { rectBlank(96f, 46f, S_WHITE, border = S_BLACK); text("КИЇВ", 50f, 50f, 15f, S_BLACK) },
    "5.46" to {
        rectBlank(96f, 46f, S_WHITE, border = S_BLACK); text("КИЇВ", 50f, 50f, 15f, S_BLACK)
        layer { clip(ArtPath.roundRect(4f, 28f, 96f, 72f, 4f)); line(8f, 70f, 92f, 30f, stroke(S_RED, 5f, cap = Cap.BUTT)) }
    },
    "5.47" to { rectBlank(96f, 46f, S_BLUE, border = S_WHITE); text("ОБУХІВ", 50f, 50f, 14f, S_WHITE) },
    "5.48" to {
        rectBlank(96f, 46f, S_BLUE, border = S_WHITE); text("ОБУХІВ", 50f, 50f, 14f, S_WHITE)
        layer { clip(ArtPath.roundRect(4f, 28f, 96f, 72f, 4f)); line(8f, 70f, 92f, 30f, stroke(S_RED, 5f, cap = Cap.BUTT)) }
    },
    "5.49" to { speedTable() },
    "5.50" to {
        rectBlank(84f, 96f, S_BLUE)
        text("ПЕРЕВАЛ", 50f, 15f, 7.6f, S_WHITE, condensed = true)
        text("Яблуницький", 50f, 26f, 7f, S_WHITE, condensed = true)
        rect(14f, 34f, 86f, 50f, fill(S_GREEN), radius = 1.5f); text("ВІДКРИТО", 50f, 42f, 8f, S_WHITE, condensed = true)
        rect(14f, 54f, 86f, 70f, fill(S_WHITE), radius = 1.5f)
        rect(14f, 74f, 86f, 90f, fill(S_WHITE), radius = 1.5f)
    },
    "5.51" to { preDirection() },
    "5.52" to {
        rectBlank(96f, 70f, S_BLUE)
        bentArrow(66f, 80f, 90f, 42f, 1, 7f, 18f, 12f, S_WHITE, r = 8f)
        text("Житомир", 31f, 34f, 8f, S_WHITE, condensed = true)
        text("Вінниця", 31f, 48f, 8f, S_WHITE, condensed = true)
        text("500 м", 31f, 70f, 8f, S_WHITE, condensed = true)
    },
    "5.53" to {
        val p = ArtPath.roundedPolygon(3f, 4f, 30f, 80f, 30f, 97f, 50f, 80f, 70f, 4f, 70f)
        path(p, fill(S_WHITE)); path(p, stroke(S_EDGE, 0.6f))
        path(ArtPath.roundedPolygon(2.4f, 5.6f, 31.6f, 79.3f, 31.6f, 95f, 50f, 79.3f, 68.4f, 5.6f, 68.4f), fill(S_GREEN))
        text("Львів", 36f, 50f, 12f, S_WHITE, condensed = true)
        text("54", 74f, 50f, 10f, S_WHITE, condensed = true)
    },
    "5.54" to {
        fun row(y: Float, label: String, km: String, right: Boolean) {
            val pts = if (right) floatArrayOf(6f, y - 13f, 82f, y - 13f, 96f, y, 82f, y + 13f, 6f, y + 13f)
            else floatArrayOf(94f, y - 13f, 18f, y - 13f, 4f, y, 18f, y + 13f, 94f, y + 13f)
            path(ArtPath.roundedPolygon(2.6f, *pts), fill(S_WHITE)); path(ArtPath.roundedPolygon(2.6f, *pts), stroke(S_EDGE, 0.6f))
            layer { translate(50f, y); scale(0.96f, 0.88f); translate(-50f, -y); path(ArtPath.roundedPolygon(2.2f, *pts), fill(S_BLUE)) }
            text(label, if (right) 36f else 64f, y, 10f, S_WHITE, condensed = true)
            text(km, if (right) 76f else 24f, y, 9f, S_WHITE, condensed = true)
        }
        row(34f, "Полтава", "48", true)
        row(66f, "Суми", "112", false)
    },
    "5.55" to {
        rectBlank(84f, 96f, S_BLUE)
        rect(44f, 10f, 56f, 90f, fill(S_WHITE))
        rect(10f, 44f, 90f, 56f, fill(S_WHITE))
        path(ArtPath.svg("M47 88 L47 66 Q47 63 50 63 L72 63 Q75 63 75 60 L75 30 Q75 26 71 26 L58 26"), stroke(S_RED, 2.6f))
        arrowHead(26f, 50f, 180f, 8f, 6f, S_BLACK)
        line(44f, 50f, 30f, 50f, stroke(S_BLACK, 2.4f))
        line(36f, 44f, 36f, 56f, stroke(S_RED, 2.6f))
    },
    "5.56" to {
        rectBlank(84f, 96f, S_YELLOW)
        rect(45f, 10f, 55f, 90f, fill(S_BLACK))
        rect(10f, 30f, 90f, 38f, fill(S_BLACK)); rect(10f, 66f, 90f, 74f, fill(S_BLACK)); rect(70f, 30f, 78f, 74f, fill(S_BLACK))
        rect(42f, 44f, 58f, 56f, fill(S_RED)); rect(44f, 46f, 56f, 54f, fill(S_WHITE))
        path(ArtPath.svg("M50 90 L50 70 L74 70 L74 34 L50 34 L50 14"), stroke(S_WHITE, 1.6f, dash = floatArrayOf(3f, 2.4f)))
    },
    "5.57.1" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 40f, 50f, 11f, S_BLACK, condensed = true); arrow(64f, 50f, 90f, 50f, 5f, 15f, 10f, S_BLACK) },
    "5.57.2" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 60f, 50f, 11f, S_BLACK, condensed = true); arrow(36f, 50f, 10f, 50f, 5f, 15f, 10f, S_BLACK) },
    "5.57.3" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 40f, 50f, 11f, S_BLACK, condensed = true); bentArrow(76f, 70f, 92f, 40f, 1, 4.6f, 13f, 9f, S_BLACK, r = 6f) },
    "5.58.1" to { rectBlank(96f, 40f, S_BLUE, border = S_WHITE); text("р. Дніпро", 50f, 50f, 12f, S_WHITE, condensed = true) },
    "5.58.2" to { rectBlank(96f, 40f, S_WHITE, border = S_BLACK); text("вул. Шевченка", 50f, 50f, 8.4f, S_BLACK, condensed = true) },
    "5.59" to {
        rectBlank(96f, 70f, S_BLUE)
        listOf("Ірпінь" to "12", "Бородянка" to "48", "Коростень" to "151").forEachIndexed { i, (n, km) ->
            text(n, 34f, 28f + i * 22f, 9f, S_WHITE, condensed = true)
            text(km, 80f, 28f + i * 22f, 9f, S_WHITE, condensed = true)
        }
    },
    "5.60" to {
        rectBlank(44f, 70f, S_WHITE, border = S_BLACK)
        text("34", 50f, 38f, 14f, S_BLACK, condensed = true)
        rect(32f, 49.4f, 68f, 50.6f, fill(S_BLACK))
        text("35", 50f, 62f, 14f, S_BLACK, condensed = true)
    },
    "5.61.1" to { routeNumber("М 06", S_RED) },
    "5.61.2" to {
        rectBlank(84f, 96f, S_WHITE)
        routeNumber("Н 02", S_BLUE, cy = 38f)
        arrow(50f, 86f, 50f, 60f, 6f, 16f, 10f, S_BLACK)
    },
    "5.61.3" to {
        rectBlank(84f, 96f, S_WHITE)
        routeNumber("Р 04", S_BLUE, cy = 38f)
        bentArrow(42f, 90f, 72f, 70f, 1, 5f, 14f, 9f, S_BLACK, r = 7f)
    },
    "5.62" to { rectBlank(96f, 52f, S_BLUE); text("STOP", 50f, 50f, 20f, S_WHITE) },
    "5.63.1" to { denseBuildings() },
    "5.63.2" to { denseBuildings(); endSlash(w = 96f, h = 60f) },
    "5.64" to {
        rectBlank(84f, 96f, S_YELLOW)
        text("НОВА", 50f, 18f, 7.4f, S_BLACK, condensed = true)
        text("СХЕМА РУХУ", 50f, 30f, 7.4f, S_BLACK, condensed = true)
        rect(46f, 40f, 54f, 90f, fill(S_BLACK)); rect(14f, 56f, 86f, 64f, fill(S_BLACK))
        arrow(50f, 88f, 50f, 58f, 3f, 9f, 6f, S_YELLOW)
    },
    "5.65" to { pictoSquare { plane() } },
    "5.66" to { pictoSquare { at(50f, 54f, 1.1f) { locomotive(S_WHITE, S_BLUE) } } },
    "5.67" to { pictoSquare { at(50f, 52f, 1.3f) { busSide(S_WHITE, S_BLUE) } } },
    "5.68" to { pictoSquare { church() } },
    "5.69" to { pictoSquare { factory() } },
    "5.70" to {
        squareBlank(S_BLUE)
        rect(22f, 38f, 68f, 64f, fill(S_WHITE), radius = 3f)
        path(ArtPath.svg("M68 46 L82 38 L82 64 L68 56 Z"), fill(S_WHITE))
        circle(36f, 51f, 7f, fill(S_BLUE)); circle(36f, 51f, 4f, fill(S_WHITE))
        rect(40f, 64f, 48f, 80f, fill(S_WHITE))
    },
)

private fun ArtCanvas.motorway() {
    squareBlank(S_GREEN)
    path(ArtPath.svg("M14 86 L40 86 L47.6 47 L45.4 47 Z"), fill(S_WHITE))
    path(ArtPath.svg("M86 86 L60 86 L52.4 47 L54.6 47 Z"), fill(S_WHITE))
    rect(10f, 30f, 90f, 39f, fill(S_WHITE), radius = 1.5f)
    rect(22f, 39f, 27f, 50f, fill(S_WHITE))
    rect(73f, 39f, 78f, 50f, fill(S_WHITE))
    rect(10f, 41.5f, 90f, 43f, fill(S_GREEN))
}

/** Red diagonal that marks the end of a regime on square signs. */
private fun ArtCanvas.endSlash(w: Float = 96f, h: Float = 96f) {
    layer {
        clip(ArtPath.roundRect(50f - w / 2 + 1.8f, 50f - h / 2 + 1.8f, 50f + w / 2 - 1.8f, 50f + h / 2 - 1.8f, 7f))
        line(50f - w / 2, 50f + h / 2, 50f + w / 2, 50f - h / 2, stroke(S_RED, 6.4f, cap = Cap.BUTT))
    }
}

private fun ArtCanvas.busLaneRoad() {
    squareBlank(S_BLUE)
    arrow(64f, 88f, 64f, 14f, 10f, 26f, 20f, S_WHITE)
    arrow(30f, 40f, 30f, 88f, 6f, 17f, 12f, S_WHITE)
    rect(44.4f, 10f, 46f, 90f, fill(S_WHITE))
    at(30f, 24f, 0.36f) { busSide(S_WHITE, S_BLUE) }
}

private fun ArtCanvas.busLane() {
    squareBlank(S_BLUE)
    path(ArtPath.svg("M14 90 L14 10 M62 90 L62 10"), stroke(S_WHITE, 2.4f))
    arrow(38f, 88f, 38f, 52f, 6f, 17f, 12f, S_WHITE)
    at(38f, 30f, 0.44f) { layer { rotate(-90f); busSide(S_WHITE, S_BLUE) } }
    arrow(80f, 88f, 80f, 20f, 6f, 17f, 12f, S_WHITE)
}

private fun ArtCanvas.reversible() {
    squareBlank(S_BLUE)
    arrow(26f, 16f, 26f, 86f, 6.4f, 17f, 13f, S_WHITE)
    arrow(74f, 86f, 74f, 16f, 6.4f, 17f, 13f, S_WHITE)
    arrow(50f, 50f, 50f, 14f, 6.4f, 17f, 13f, S_WHITE)
    arrow(50f, 50f, 50f, 86f, 6.4f, 17f, 13f, S_WHITE)
}

private fun ArtCanvas.laneStart(right: Boolean) {
    rectBlank(84f, 96f, S_BLUE)
    val k = if (right) 1f else -1f
    arrow(50f - 14f * k, 88f, 50f - 14f * k, 14f, 6.4f, 17f, 13f, S_WHITE)
    path(ArtPath.svg("M${50 + 8 * k} 88 L${50 + 8 * k} 64 Q${50 + 8 * k} 56 ${50 + 20 * k} 48 L${50 + 20 * k} 26"), stroke(S_WHITE, 6.4f, cap = Cap.BUTT))
    arrowHead(50f + 20f * k, 14f, 270f, 17f, 13f, S_WHITE)
}

private fun ArtCanvas.laneEnd(right: Boolean) {
    rectBlank(84f, 96f, S_BLUE)
    val k = if (right) 1f else -1f
    arrow(50f - 14f * k, 88f, 50f - 14f * k, 14f, 6.4f, 17f, 13f, S_WHITE)
    path(ArtPath.svg("M${50 + 20 * k} 88 L${50 + 20 * k} 62 Q${50 + 20 * k} 52 ${50 + 4 * k} 40 L${50 - 2 * k} 34"), stroke(S_WHITE, 6.4f, cap = Cap.BUTT))
}

private fun ArtCanvas.crossover(left: Boolean) {
    squareBlank(S_BLUE)
    val k = if (left) 1f else -1f
    rect(48.6f, 10f, 51.4f, 90f, fill(S_WHITE))
    rect(44f, 34f, 56f, 62f, fill(S_BLUE))
    path(ArtPath.svg("M${50 + 18 * k} 88 L${50 + 18 * k} 66 Q${50 + 18 * k} 58 ${50 - 2 * k} 48 Q${50 - 20 * k} 40 ${50 - 20 * k} 30"), stroke(S_WHITE, 7f, cap = Cap.BUTT))
    arrowHead(50f - 20f * k, 14f, 270f, 18f, 14f, S_WHITE)
}

private fun ArtCanvas.truckDirection(dir: Int) {
    rectBlank(96f, 60f, S_BLUE)
    at(36f, 50f, 0.96f) { truckSide(S_WHITE, S_BLUE) }
    when (dir) {
        0 -> arrow(80f, 74f, 80f, 26f, 6f, 17f, 12f, S_WHITE)
        1 -> bentArrow(72f, 74f, 92f, 40f, 1, 6f, 17f, 10f, S_WHITE, r = 6f)
        else -> bentArrow(88f, 74f, 68f, 40f, -1, 6f, 17f, 10f, S_WHITE, r = 6f)
    }
}

private fun ArtCanvas.deadEndTurn(dir: Int) {
    squareBlank(S_BLUE)
    val k = dir.toFloat()
    rect(50f - 20f * k - 7f, 50f, 50f - 20f * k + 7f, 88f, fill(S_WHITE))
    path(ArtPath.svg("M${50 - 20 * k} 56 L${50 - 20 * k} 48 Q${50 - 20 * k} 38 ${50 - 10 * k} 38 L${50 + 18 * k} 38"), stroke(S_WHITE, 14f, cap = Cap.BUTT))
    rect(minOf(50f + 18f * k, 50f + 28f * k), 26f, maxOf(50f + 18f * k, 50f + 28f * k), 50f, fill(S_RED))
}

private fun ArtCanvas.livingZone() {
    rectBlank(80f, 96f, S_BLUE)
    rect(14f, 12f, 86f, 88f, fill(S_WHITE), radius = 2f)
    path(ArtPath.svg("M20 44 L36 30 L52 44 L49 44 L49 58 L23 58 L23 44 Z"), fill(S_BLUE))
    rect(33f, 48f, 39f, 58f, fill(S_WHITE))
    at(63f, 46f, 0.6f) { pedestrian(S_BLACK) }
    at(76f, 52f, 0.42f) { pedestrian(S_BLACK) }
    circle(56f, 60f, 2.6f, fill(S_RED))
    at(42f, 76f, 0.55f) { carSide(S_BLACK, S_WHITE) }
    rect(14f, 84f, 86f, 86f, fill(S_GREY))
}

private fun ArtCanvas.pedestrianZone() {
    squareBlank(S_BLUE)
    at(40f, 50f, 1.1f) { pedestrian(S_WHITE) }
    at(62f, 60f, 0.78f) { pedestrian(S_WHITE) }
    poly(S_WHITE, 3f, 48f, 47f, 54f, 52f, 57f, 54f)
}

private fun ArtCanvas.crossingSign() {
    squareBlank(S_BLUE)
    path(ArtPath.roundedPolygon(3f, 50f, 12f, 90f, 84f, 10f, 84f), fill(S_WHITE))
    for (i in 0 until 4) shape(S_BLACK, 24f + i * 13f, 76f, 32f + i * 13f, 76f, 30f + i * 13f, 81f, 22f + i * 13f, 81f, round = 0.4f)
    at(48f, 58f, 0.82f) { pedestrian(S_BLACK) }
}

private fun ArtCanvas.stairsSign(down: Boolean) {
    squareBlank(S_BLUE)
    // Stairs rising to the right; the walker goes down them (underpass) or up them (overpass).
    path(ArtPath.svg("M14 86 L14 76 L30 76 L30 66 L46 66 L46 56 L62 56 L62 46 L78 46 L78 36 L88 36 L88 86 Z"), fill(S_WHITE))
    if (down) at(52f, 34f, 0.86f) { pedestrian(S_WHITE, facing = -1) } else at(46f, 34f, 0.86f) { pedestrian(S_WHITE) }
    if (down) arrowHead(22f, 60f, 135f, 10f, 8f, S_WHITE) else arrowHead(78f, 18f, 315f, 10f, 8f, S_WHITE)
}

private fun ArtCanvas.parkingZone(end: Boolean) {
    rectBlank(80f, 96f, S_WHITE, border = if (end) S_GREY else S_BLACK)
    rect(28f, 14f, 72f, 58f, fill(S_BLUE), radius = 3f)
    text("P", 50f, 36f, 30f, S_WHITE)
    text("ЗОНА", 50f, 76f, 12f, if (end) S_GREY else S_BLACK, condensed = true)
    if (end) layer {
        clip(ArtPath.roundRect(12f, 4f, 88f, 96f, 4f))
        for (i in -2..2) { val o = i * 3.4f; line(14f + o, 94f + o, 86f + o, 6f + o, stroke(S_BLACK, 1.5f, cap = Cap.BUTT)) }
    }
}

/** Public transport stop: blue plate with a white panel and a black vehicle. */
private fun ArtCanvas.stopSign(k: Float = 1f, dy: Float = 0f, vehicle: ArtCanvas.() -> Unit) {
    rectBlank(80f, 96f, S_BLUE)
    rect(18f, 14f, 82f, 72f, fill(S_WHITE), radius = 2.6f)
    at(50f, 45f + dy, k) { vehicle() }
}

private fun ArtCanvas.speedTable() {
    rectBlank(96f, 80f, S_BLUE)
    text("УКРАЇНА", 50f, 19f, 8f, S_WHITE, condensed = true)
    val rows = listOf("50", "90", "110", "130")
    rows.forEachIndexed { i, v ->
        val x = 20f + i * 20f
        circle(x, 46f, 8.4f, fill(S_WHITE)); circle(x, 46f, 7f, fill(S_RED)); circle(x, 46f, 5.6f, fill(S_WHITE))
        text(v, x, 46f, if (v.length > 2) 4f else 5f, S_BLACK, condensed = true)
    }
    listOf("місто", "дорога", "2 смуги", "магістр.").forEachIndexed { i, s -> text(s, 20f + i * 20f, 66f, 4f, S_WHITE, condensed = true) }
}

private fun ArtCanvas.preDirection() {
    rectBlank(96f, 80f, S_BLUE)
    rect(46f, 52f, 54f, 86f, fill(S_WHITE))
    rect(16f, 46f, 84f, 54f, fill(S_WHITE))
    arrowHead(10f, 50f, 180f, 18f, 10f, S_WHITE); arrowHead(90f, 50f, 0f, 18f, 10f, S_WHITE)
    rect(46f, 26f, 54f, 46f, fill(S_WHITE)); arrowHead(50f, 18f, 270f, 18f, 10f, S_WHITE)
    text("Київ", 26f, 34f, 8f, S_WHITE, condensed = true)
    text("Одеса", 74f, 34f, 8f, S_WHITE, condensed = true)
    text("Умань", 26f, 66f, 7f, S_WHITE, condensed = true)
    text("300 м", 74f, 76f, 7f, S_WHITE, condensed = true)
}

private fun ArtCanvas.routeNumber(label: String, field: Long, cy: Float = 50f) {
    rect(14f, cy - 16f, 86f, cy + 16f, fill(S_WHITE), radius = 4f)
    rect(14f, cy - 16f, 86f, cy + 16f, stroke(S_EDGE, 0.6f), radius = 4f)
    rect(15.6f, cy - 14.4f, 84.4f, cy + 14.4f, fill(field), radius = 3f)
    text(label, 50f, cy, 14f, S_WHITE, condensed = true)
}

private fun ArtCanvas.denseBuildings() {
    rectBlank(96f, 60f, S_BLUE)
    path(ArtPath.svg("M14 72 L14 48 L24 40 L34 48 L34 44 L44 36 L54 44 L54 34 L66 34 L66 52 L74 46 L84 52 L84 72 Z"), fill(S_WHITE))
    for (x in listOf(20f, 40f, 58f, 76f)) rect(x, 54f, x + 4f, 60f, fill(S_BLUE))
}

private fun ArtCanvas.pictoSquare(content: ArtCanvas.() -> Unit) { squareBlank(S_BLUE); content() }

private fun ArtCanvas.plane() {
    // Airliner seen from above, nose up.
    path(ArtPath.svg("M50 12 Q54 12 54 20 L54 40 L86 56 L86 62 L54 54 L54 74 L64 82 L64 86 L50 82 L36 86 L36 82 L46 74 L46 54 L14 62 L14 56 L46 40 L46 20 Q46 12 50 12 Z"), fill(S_WHITE))
}

private fun ArtCanvas.church() {
    path(ArtPath.svg("M26 86 L26 52 L38 42 L38 30 L45 24 L45 14 L48 14 L48 11 L52 11 L52 14 L55 14 L55 24 L62 30 L62 42 L74 52 L74 86 Z"), fill(S_WHITE))
    path(ArtPath.svg("M44 86 L44 72 Q50 64 56 72 L56 86 Z"), fill(S_BLUE))
    rect(47f, 36f, 53f, 46f, fill(S_BLUE), radius = 3f)
}

private fun ArtCanvas.factory() {
    path(ArtPath.svg("M14 86 L14 52 L30 42 L30 52 L46 42 L46 52 L62 42 L62 52 L66 52 L66 18 L76 18 L76 52 L86 52 L86 86 Z"), fill(S_WHITE))
    for (i in 0 until 3) rect(20f + i * 16f, 62f, 28f + i * 16f, 70f, fill(S_BLUE))
}
