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
    "5.19.1" to {
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
    "5.27.1" to { crossover(left = true) },
    "5.27.2" to { crossover(left = false) },
    "5.28" to {
        squareBlank(S_BLUE)
        path(ArtPath.svg("M30 90 L30 12 L48 12 L48 90 Z"), fill(S_WHITE))
        path(ArtPath.svg("M48 74 Q66 64 72 30 L86 30 Q80 70 48 90 Z"), fill(S_WHITE))
        for (i in 0 until 6) circle(74f + (i % 2) * 6f, 22f + (i / 2) * 3.4f, 1.4f, fill(S_WHITE))
        at(39f, 40f, 0.3f) { layer { rotate(-90f); carSide(S_BLUE, S_WHITE) } }
    },
    "5.29" to { squareBlank(S_BLUE); uTurnArrow(66f, 86f, 74f, 32f, 11f, 28f, 20f, S_WHITE) },
    "5.30" to {
        rectBlank(80f, 96f, S_BLUE)
        uTurnArrow(64f, 70f, 60f, 28f, 9f, 24f, 17f, S_WHITE)
        text("200 м", 50f, 84f, 10f, S_WHITE, condensed = true)
    },
    "5.31.1" to { truckDirection(0) },
    "5.31.2" to { truckDirection(1) },
    "5.31.3" to { truckDirection(-1) },
    "5.32.1" to { squareBlank(S_BLUE); rect(42f, 34f, 58f, 88f, fill(S_WHITE)); rect(26f, 16f, 74f, 30f, fill(S_RED)) },
    "5.32.2" to { deadEndTurn(1) },
    "5.32.3" to { deadEndTurn(-1) },
    "5.33" to { squareBlank(S_BLUE); text("50", 50f, 50f, 36f, S_WHITE, condensed = true) },
    "5.34" to { livingZone() },
    "5.35" to { livingZone(); endSlash(w = 80f) },
    "5.36" to { pedestrianZone() },
    "5.37" to { pedestrianZone(); endSlash() },
    "5.38.1" to { crossingSign() },
    "5.38.2" to { mirrored { crossingSign() } },
    "5.40.1" to { stairsSign(down = true) },
    "5.40.2" to { mirrored { stairsSign(down = true) } },
    "5.41.1" to { stairsSign(down = false) },
    "5.41.2" to { mirrored { stairsSign(down = false) } },
    "5.42.1" to { squareBlank(S_BLUE); text("P", 50f, 50f, 56f, S_WHITE) },
    "5.43" to { parkingZone(end = false) },
    "5.44" to { parkingZone(end = true) },
    "5.45.1" to { stopSign { busSide(S_BLACK, S_WHITE) } },
    "5.45.2" to { stopSign { busSide(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.46.1" to { stopSign(k = 0.86f) { tramFront(S_BLACK, S_WHITE) } },
    "5.46.2" to { stopSign(k = 0.86f) { tramFront(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.47.1" to { stopSign(k = 0.84f, dy = 4f) { trolleybusSide(S_BLACK, S_WHITE) } },
    "5.47.2" to { stopSign(k = 0.84f, dy = 4f) { trolleybusSide(S_BLACK, S_WHITE) }; endSlash(w = 80f) },
    "5.48" to {
        squareBlank(S_BLUE)
        rect(14f, 30f, 86f, 70f, fill(S_YELLOW), radius = 4f)
        for (i in 0 until 6) rect(18f + i * 11f, 32f + (i % 2) * 4f, 22f + i * 11f, 36f + (i % 2) * 4f, fill(S_BLACK))
        text("TAXI", 50f, 56f, 15f, S_BLACK)
    },
    "5.49" to { rectBlank(96f, 46f, S_WHITE, border = S_BLACK); text("КИЇВ", 50f, 50f, 15f, S_BLACK) },
    "5.50" to {
        rectBlank(96f, 46f, S_WHITE, border = S_BLACK); text("КИЇВ", 50f, 50f, 15f, S_BLACK)
        layer { clip(ArtPath.roundRect(4f, 28f, 96f, 72f, 4f)); line(8f, 70f, 92f, 30f, stroke(S_RED, 5f, cap = Cap.BUTT)) }
    },
    "5.51" to { rectBlank(96f, 46f, S_BLUE, border = S_WHITE); text("ОБУХІВ", 50f, 50f, 14f, S_WHITE) },
    "5.52" to {
        rectBlank(96f, 46f, S_BLUE, border = S_WHITE); text("ОБУХІВ", 50f, 50f, 14f, S_WHITE)
        layer { clip(ArtPath.roundRect(4f, 28f, 96f, 72f, 4f)); line(8f, 70f, 92f, 30f, stroke(S_RED, 5f, cap = Cap.BUTT)) }
    },
    "5.55" to { speedTable() },
    "5.56" to {
        rectBlank(84f, 96f, S_BLUE)
        text("ПЕРЕВАЛ", 50f, 15f, 7.6f, S_WHITE, condensed = true)
        text("Яблуницький", 50f, 26f, 7f, S_WHITE, condensed = true)
        rect(14f, 34f, 86f, 50f, fill(S_GREEN), radius = 1.5f); text("ВІДКРИТО", 50f, 42f, 8f, S_WHITE, condensed = true)
        rect(14f, 54f, 86f, 70f, fill(S_WHITE), radius = 1.5f)
        rect(14f, 74f, 86f, 90f, fill(S_WHITE), radius = 1.5f)
    },
    "5.57" to { preDirection() },
    "5.58" to {
        rectBlank(96f, 70f, S_BLUE)
        bentArrow(66f, 80f, 90f, 42f, 1, 7f, 18f, 12f, S_WHITE, r = 8f)
        text("Житомир", 31f, 34f, 8f, S_WHITE, condensed = true)
        text("Вінниця", 31f, 48f, 8f, S_WHITE, condensed = true)
        text("500 м", 31f, 70f, 8f, S_WHITE, condensed = true)
    },
    "5.59" to {
        val p = ArtPath.roundedPolygon(3f, 4f, 30f, 80f, 30f, 97f, 50f, 80f, 70f, 4f, 70f)
        path(p, fill(S_WHITE)); path(p, stroke(S_EDGE, 0.6f))
        path(ArtPath.roundedPolygon(2.4f, 5.6f, 31.6f, 79.3f, 31.6f, 95f, 50f, 79.3f, 68.4f, 5.6f, 68.4f), fill(S_GREEN))
        text("Львів", 36f, 50f, 12f, S_WHITE, condensed = true)
        text("54", 74f, 50f, 10f, S_WHITE, condensed = true)
    },
    "5.60" to {
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
    "5.62" to {
        rectBlank(84f, 96f, S_BLUE)
        rect(44f, 10f, 56f, 90f, fill(S_WHITE))
        rect(10f, 44f, 90f, 56f, fill(S_WHITE))
        path(ArtPath.svg("M47 88 L47 66 Q47 63 50 63 L72 63 Q75 63 75 60 L75 30 Q75 26 71 26 L58 26"), stroke(S_RED, 2.6f))
        arrowHead(26f, 50f, 180f, 8f, 6f, S_BLACK)
        line(44f, 50f, 30f, 50f, stroke(S_BLACK, 2.4f))
        line(36f, 44f, 36f, 56f, stroke(S_RED, 2.6f))
    },
    "5.63" to {
        rectBlank(84f, 96f, S_YELLOW)
        rect(45f, 10f, 55f, 90f, fill(S_BLACK))
        rect(10f, 30f, 90f, 38f, fill(S_BLACK)); rect(10f, 66f, 90f, 74f, fill(S_BLACK)); rect(70f, 30f, 78f, 74f, fill(S_BLACK))
        rect(42f, 44f, 58f, 56f, fill(S_RED)); rect(44f, 46f, 56f, 54f, fill(S_WHITE))
        path(ArtPath.svg("M50 90 L50 70 L74 70 L74 34 L50 34 L50 14"), stroke(S_WHITE, 1.6f, dash = floatArrayOf(3f, 2.4f)))
    },
    "5.64.1" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 40f, 50f, 11f, S_BLACK, condensed = true); arrow(64f, 50f, 90f, 50f, 5f, 15f, 10f, S_BLACK) },
    "5.64.2" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 60f, 50f, 11f, S_BLACK, condensed = true); arrow(36f, 50f, 10f, 50f, 5f, 15f, 10f, S_BLACK) },
    "5.64.3" to { rectBlank(96f, 46f, S_YELLOW); text("ОБ'ЇЗД", 40f, 50f, 11f, S_BLACK, condensed = true); bentArrow(76f, 70f, 92f, 40f, 1, 4.6f, 13f, 9f, S_BLACK, r = 6f) },
    "5.65.1" to { rectBlank(96f, 40f, S_BLUE, border = S_WHITE); text("р. Дніпро", 50f, 50f, 12f, S_WHITE, condensed = true) },
    "5.65.2" to { rectBlank(96f, 40f, S_WHITE, border = S_BLACK); text("вул. Шевченка", 50f, 50f, 8.4f, S_BLACK, condensed = true) },
    "5.61" to {
        rectBlank(96f, 70f, S_BLUE)
        listOf("Ірпінь" to "12", "Бородянка" to "48", "Коростень" to "151").forEachIndexed { i, (n, km) ->
            text(n, 34f, 28f + i * 22f, 9f, S_WHITE, condensed = true)
            text(km, 80f, 28f + i * 22f, 9f, S_WHITE, condensed = true)
        }
    },
    "5.68" to {
        rectBlank(44f, 70f, S_WHITE, border = S_BLACK)
        text("34", 50f, 38f, 14f, S_BLACK, condensed = true)
        rect(32f, 49.4f, 68f, 50.6f, fill(S_BLACK))
        text("35", 50f, 62f, 14f, S_BLACK, condensed = true)
    },
    "5.66" to { routeNumber("М 06", S_RED) },
    "5.67.1" to {
        rectBlank(84f, 96f, S_WHITE)
        routeNumber("Н 02", S_BLUE, cy = 38f)
        arrow(50f, 86f, 50f, 60f, 6f, 16f, 10f, S_BLACK)
    },
    "5.67.2" to {
        rectBlank(84f, 96f, S_WHITE)
        routeNumber("Р 04", S_BLUE, cy = 38f)
        bentArrow(42f, 90f, 72f, 70f, 1, 5f, 14f, 9f, S_BLACK, r = 7f)
    },
    "5.67.3" to {
        rectBlank(84f, 96f, S_WHITE)
        routeNumber("М 05", S_RED, cy = 38f)
        bentArrow(58f, 90f, 28f, 70f, -1, 5f, 14f, 9f, S_BLACK, r = 7f)
    },
    "5.69" to { rectBlank(96f, 52f, S_BLUE); text("STOP", 50f, 50f, 20f, S_WHITE) },
    "5.53" to { denseBuildings() },
    "5.54" to { denseBuildings(); endSlash(w = 96f, h = 60f) },
    "5.70" to {
        rectBlank(84f, 96f, S_YELLOW)
        text("НОВА", 50f, 18f, 7.4f, S_BLACK, condensed = true)
        text("СХЕМА РУХУ", 50f, 30f, 7.4f, S_BLACK, condensed = true)
        rect(46f, 40f, 54f, 90f, fill(S_BLACK)); rect(14f, 56f, 86f, 64f, fill(S_BLACK))
        arrow(50f, 88f, 50f, 58f, 3f, 9f, 6f, S_YELLOW)
    },
    "5.71" to { pictoSquare { plane() } },
    "5.72" to { pictoSquare { at(50f, 54f, 1.1f) { locomotive(S_WHITE, S_BLUE) } } },
    "5.73" to { pictoSquare { at(50f, 52f, 1.3f) { busSide(S_WHITE, S_BLUE) } } },
    "5.76" to {
        squareBlank(S_BLUE)
        rect(22f, 38f, 68f, 64f, fill(S_WHITE), radius = 3f)
        path(ArtPath.svg("M68 46 L82 38 L82 64 L68 56 Z"), fill(S_WHITE))
        circle(36f, 51f, 7f, fill(S_BLUE)); circle(36f, 51f, 4f, fill(S_WHITE))
        rect(40f, 64f, 48f, 80f, fill(S_WHITE))
    },
    "5.10.3" to {
        rectBlank(96f, 60f, S_BLUE)
        arrow(12f, 62f, 88f, 62f, 7f, 20f, 15f, S_WHITE)
        rect(10f, 47.4f, 90f, 48.6f, fill(S_WHITE))
        arrow(46f, 34f, 88f, 34f, 5f, 15f, 11f, S_WHITE)
        at(26f, 34f, 0.32f) { busSide(S_WHITE, S_BLUE) }
    },
    "5.10.4" to { mirrored { rectBlank(96f, 60f, S_BLUE); arrow(12f, 62f, 88f, 62f, 7f, 20f, 15f, S_WHITE); rect(10f, 47.4f, 90f, 48.6f, fill(S_WHITE)); arrow(46f, 34f, 88f, 34f, 5f, 15f, 11f, S_WHITE) }; at(74f, 34f, 0.32f) { busSide(S_WHITE, S_BLUE) } },
    "5.19.2" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(30f, 88f, 30f, 36f, 6.4f, 17f, 13f, S_WHITE)
        arrow(50f, 88f, 50f, 36f, 6.4f, 17f, 13f, S_WHITE)
        bentArrow(70f, 88f, 88f, 50f, 1, 6.4f, 17f, 10f, S_WHITE, r = 8f)
        at(70f, 22f, 0.5f) { bicycle(S_WHITE, 2.6f) }
    },
    "5.19.3" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(30f, 88f, 30f, 36f, 6.4f, 17f, 13f, S_WHITE)
        arrow(50f, 88f, 50f, 36f, 6.4f, 17f, 13f, S_WHITE)
        arrow(70f, 88f, 70f, 36f, 6.4f, 17f, 13f, S_WHITE)
        at(30f, 22f) {
            circle(0f, 0f, 11f, fill(S_WHITE)); circle(0f, 0f, 10f, stroke(S_RED, 2.4f))
            at(0f, 0f, 0.26f) { truckSide(S_BLACK, S_WHITE) }
            line(-7f, -7f, 7f, 7f, stroke(S_RED, 2.2f))
        }
    },
    "5.24" to { laneSpeeds(listOf("80", "60"), min = true) },
    "5.25" to {
        rectBlank(84f, 96f, S_BLUE)
        arrow(34f, 88f, 34f, 50f, 6.4f, 17f, 13f, S_WHITE)
        arrow(66f, 88f, 66f, 50f, 6.4f, 17f, 13f, S_WHITE)
        at(34f, 28f, 0.9f) { circle(0f, 0f, 14f, fill(S_WHITE)); circle(0f, 0f, 12.4f, fill(S_BLUE)); text("40", 0f, 0f, 12f, S_WHITE, condensed = true) }
        at(66f, 28f, 0.5f) { truckSide(S_WHITE, S_BLUE) }
    },
    "5.26" to { laneSpeeds(listOf("110", "90", "70"), min = false) },
    "5.39" to {
        squareBlank(S_BLUE)
        path(ArtPath.roundedPolygon(3f, 50f, 12f, 90f, 84f, 10f, 84f), fill(S_WHITE))
        for (i in 0 until 5) {
            val t = 30f + i * 9f
            line(t - 2f, 82f - (i * 9f) * 0.9f - 2f, t + 4f, 82f - (i * 9f) * 0.9f - 8f, stroke(S_BLACK, 3f, cap = Cap.BUTT))
        }
        at(44f, 56f, 0.7f) { pedestrian(S_BLACK) }
        arrowHead(70f, 44f, 315f, 8f, 6f, S_BLACK)
    },
    "5.42.2" to {
        squareBlank(S_BLUE)
        path(ArtPath.svg("M14 34 L50 14 L86 34"), stroke(S_WHITE, 5f, cap = Cap.BUTT))
        text("P", 50f, 60f, 46f, S_WHITE)
    },
    "5.42.3" to {
        squareBlank(S_BLUE)
        text("P", 34f, 44f, 40f, S_WHITE)
        at(70f, 70f, 0.42f) { truckSide(S_WHITE, S_BLUE) }
        arrow(64f, 30f, 82f, 30f, 3f, 9f, 6f, S_WHITE); arrow(82f, 40f, 64f, 40f, 3f, 9f, 6f, S_WHITE)
    },
    "5.74" to { tunnelSign() },
    "5.75" to { tunnelSign(); endSlash() },
    "5.77" to { pictoSquare { anchor() } },
    "5.78" to {
        rectBlank(84f, 96f, S_BLUE)
        rect(18f, 14f, 82f, 60f, fill(S_WHITE), radius = 3f)
        rect(26f, 28f, 74f, 52f, fill(S_BLUE), radius = 3f)
        circle(38f, 40f, 7f, fill(S_WHITE)); circle(38f, 40f, 3f, fill(S_BLUE))
        rect(50f, 34f, 70f, 36f, fill(S_WHITE)); rect(50f, 39f, 70f, 41f, fill(S_WHITE)); rect(50f, 44f, 64f, 46f, fill(S_WHITE))
        line(34f, 28f, 62f, 16f, stroke(S_BLUE, 1.6f))
        text("FM 103,6", 50f, 76f, 11f, S_WHITE, condensed = true)
    },
    "5.79.1" to { emergencyExit() },
    "5.79.2" to { mirrored { emergencyExit() } },
    "5.80.1" to { exitDistance(left = true) },
    "5.80.2" to { exitDistance(left = false) },
    "5.81" to {
        rectBlank(96f, 56f, S_GREEN)
        at(50f, 46f, 0.62f) { runner(S_WHITE) }
        arrow(30f, 46f, 10f, 46f, 3.6f, 11f, 7f, S_WHITE); arrow(70f, 46f, 90f, 46f, 3.6f, 11f, 7f, S_WHITE)
        text("120 м", 22f, 66f, 7.4f, S_WHITE, condensed = true); text("380 м", 78f, 66f, 7.4f, S_WHITE, condensed = true)
    },
    "5.82.1" to { tollRoad(square = true) },
    "5.83.1" to { tollRoad(square = false) },
    "5.82.2" to { tollRoad(square = true); endSlash() },
    "5.83.2" to { tollRoad(square = false); endSlash(w = 96f, h = 60f) },
    "5.84" to { borderZone("ПРИКОРДОННА", "СМУГА", end = false) },
    "5.85" to { borderZone("ПРИКОРДОННА", "СМУГА", end = true) },
    "5.86" to { borderZone("КОНТРОЛЬОВАНИЙ", "ПРИКОРДОННИЙ", "РАЙОН", end = false) },
    "5.87" to { borderZone("КОНТРОЛЬОВАНИЙ", "ПРИКОРДОННИЙ", "РАЙОН", end = true) },
    "5.88" to { bikeLane() },
    "5.89" to { bikeLane(); endSlash(w = 84f) },
    "5.90" to { pedBikeZone(end = false) },
    "5.91" to { pedBikeZone(end = true) },
    "5.92.1" to { bikeCrossing(pedestrians = false) },
    "5.92.2" to { mirrored { bikeCrossing(pedestrians = false) } },
    "5.93.1" to { bikeCrossing(pedestrians = true) },
    "5.93.2" to { mirrored { bikeCrossing(pedestrians = true) } },
    "5.94.1" to { contraflowBikes() },
    "5.94.2" to { contraflowBikes(); endSlash() },
    "5.95.1" to {
        rectBlank(96f, 60f, S_BLUE)
        arrow(12f, 40f, 88f, 40f, 8f, 22f, 16f, S_WHITE)
        arrow(70f, 64f, 30f, 64f, 3.6f, 11f, 8f, S_WHITE)
        at(82f, 64f, 0.42f) { bicycle(S_WHITE, 3f) }
    },
    "5.95.2" to {
        rectBlank(96f, 60f, S_BLUE)
        arrow(88f, 40f, 12f, 40f, 8f, 22f, 16f, S_WHITE)
        arrow(30f, 64f, 70f, 64f, 3.6f, 11f, 8f, S_WHITE)
        at(18f, 64f, 0.42f) { bicycle(S_WHITE, 3f) }
    },
    "5.96.1" to { weighStation() },
    "5.96.2" to { weighStation(); endSlash(w = 96f, h = 70f) },
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


/** 5.24, 5.26: lanes with a speed sign over each (blue = minimum, red ring = maximum). */
private fun ArtCanvas.laneSpeeds(speeds: List<String>, min: Boolean) {
    rectBlank(84f, 96f, S_BLUE)
    val n = speeds.size
    speeds.forEachIndexed { i, v ->
        val x = 50f + (i - (n - 1) / 2f) * (60f / n)
        arrow(x, 88f, x, 52f, 5.6f, 15f, 11f, S_WHITE)
        val r = if (n == 3) 10f else 13f
        if (min) {
            circle(x, 30f, r, fill(S_WHITE)); circle(x, 30f, r - 1.6f, fill(S_BLUE)); text(v, x, 30f, r * 0.9f, S_WHITE, condensed = true)
        } else {
            circle(x, 30f, r, fill(S_RED)); circle(x, 30f, r * 0.76f, fill(S_WHITE)); text(v, x, 30f, r * 0.8f, S_BLACK, condensed = true)
        }
    }
}

private fun ArtCanvas.tunnelSign() {
    squareBlank(S_BLUE)
    path(ArtPath.svg("M14 84 L14 52 Q14 20 50 20 Q86 20 86 52 L86 84 L74 84 L74 54 Q74 32 50 32 Q26 32 26 54 L26 84 Z"), fill(S_WHITE))
    path(ArtPath.svg("M38 84 L46 46 L54 46 L62 84 Z"), fill(S_WHITE))
    rect(49f, 54f, 51f, 60f, fill(S_BLUE)); rect(49f, 66f, 51f, 74f, fill(S_BLUE))
}

private fun ArtCanvas.anchor() {
    circle(50f, 22f, 6f, stroke(S_WHITE, 3.4f))
    rect(47.6f, 28f, 52.4f, 78f, fill(S_WHITE))
    rect(36f, 36f, 64f, 40.6f, fill(S_WHITE), radius = 2f)
    path(ArtPath.svg("M22 60 Q24 80 50 80 Q76 80 78 60"), stroke(S_WHITE, 4.6f))
    arrowHead(22f, 56f, 270f, 10f, 8f, S_WHITE); arrowHead(78f, 56f, 270f, 10f, 8f, S_WHITE)
}

/** Running man of the emergency-exit signs, centred on the origin. */
private fun ArtCanvas.runner(ink: Long) {
    circle(6f, -24f, 5f, fill(ink))
    path(ArtPath.svg("M-2 -16 L10 -14 L14 -2 L22 2 M10 -14 L-6 -4 M2 -14 L-2 4 L10 12 L8 26 M-2 4 L-12 14 L-22 14"), stroke(ink, 5f))
}

private fun ArtCanvas.emergencyExit() {
    squareBlank(S_GREEN)
    rect(56f, 18f, 84f, 82f, fill(S_WHITE), radius = 1.5f)
    rect(60f, 22f, 80f, 78f, fill(S_GREEN))
    at(40f, 54f, 1f) { runner(S_WHITE) }
}

private fun ArtCanvas.exitDistance(left: Boolean) {
    rectBlank(96f, 50f, S_GREEN)
    val k = if (left) -1f else 1f
    at(50f - 8f * k, 46f, 0.6f) { if (left) layer { scale(-1f, 1f); runner(S_WHITE) } else runner(S_WHITE) }
    arrow(50f + 12f * k, 46f, 50f + 38f * k, 46f, 4f, 12f, 8f, S_WHITE)
    text("150 м", 50f - 28f * k, 62f, 8f, S_WHITE, condensed = true)
}

private fun ArtCanvas.tollRoad(square: Boolean) {
    if (square) {
        squareBlank(S_BLUE)
        at(50f, 34f, 1.1f) { carSide(S_WHITE, S_BLUE) }
        circle(50f, 68f, 15f, fill(S_WHITE))
        text("₴", 50f, 68f, 20f, S_BLUE)
    } else {
        rectBlank(96f, 60f, S_BLUE)
        circle(22f, 50f, 13f, fill(S_WHITE)); text("₴", 22f, 50f, 17f, S_BLUE)
        text("ПЛАТНА", 62f, 42f, 10f, S_WHITE, condensed = true)
        text("ДОРОГА", 62f, 58f, 10f, S_WHITE, condensed = true)
    }
}

private fun ArtCanvas.borderZone(vararg lines: String, end: Boolean) {
    rectBlank(96f, 56f, S_BLUE, border = S_WHITE)
    val size = minOf(9.6f, lines.minOf { 80f / (it.length * 1.05f) })
    val step = size * 1.5f
    lines.forEachIndexed { i, s -> text(s, 50f, 50f + (i - (lines.size - 1) / 2f) * step, size, S_WHITE, condensed = true) }
    if (end) layer { clip(ArtPath.roundRect(4f, 24f, 96f, 76f, 4f)); line(8f, 74f, 92f, 26f, stroke(S_RED, 5f, cap = Cap.BUTT)) }
}

private fun ArtCanvas.bikeLane() {
    rectBlank(84f, 96f, S_BLUE)
    arrow(30f, 88f, 30f, 16f, 6.4f, 17f, 13f, S_WHITE)
    rect(52f, 10f, 53.6f, 90f, fill(S_WHITE))
    arrow(70f, 88f, 70f, 46f, 5f, 15f, 11f, S_WHITE)
    at(70f, 28f, 0.56f) { bicycle(S_WHITE, 2.8f) }
}

private fun ArtCanvas.pedBikeZone(end: Boolean) {
    val ink = if (end) S_GREY else S_BLACK
    rectBlank(80f, 96f, S_WHITE, border = ink)
    circle(50f, 38f, 22f, fill(if (end) S_GREY else S_BLUE))
    at(42f, 40f, 0.62f) { pedestrian(S_WHITE) }
    at(60f, 42f, 0.42f) { bicycle(S_WHITE, 3f) }
    text("ЗОНА", 50f, 76f, 12f, ink, condensed = true)
    if (end) layer {
        clip(ArtPath.roundRect(12f, 4f, 88f, 96f, 4f))
        for (i in -2..2) { val o = i * 3.4f; line(14f + o, 94f + o, 86f + o, 6f + o, stroke(S_BLACK, 1.5f, cap = Cap.BUTT)) }
    }
}

private fun ArtCanvas.bikeCrossing(pedestrians: Boolean) {
    squareBlank(S_BLUE)
    path(ArtPath.roundedPolygon(3f, 50f, 12f, 90f, 84f, 10f, 84f), fill(S_WHITE))
    if (pedestrians) {
        for (i in 0 until 3) shape(S_BLACK, 22f + i * 9f, 76f, 28f + i * 9f, 76f, 26f + i * 9f, 81f, 20f + i * 9f, 81f, round = 0.4f)
        for (i in 0 until 3) { rect(52f + i * 9f, 76f, 56f + i * 9f, 78f, fill(S_BLACK)); rect(52f + i * 9f, 79f, 56f + i * 9f, 81f, fill(S_BLACK)) }
        at(36f, 60f, 0.62f) { pedestrian(S_BLACK) }
        at(62f, 64f, 0.44f) { bicycle(S_BLACK, 3f) }
    } else {
        for (i in 0 until 6) { rect(22f + i * 10f, 76f, 27f + i * 10f, 78f, fill(S_BLACK)); rect(22f + i * 10f, 79f, 27f + i * 10f, 81f, fill(S_BLACK)) }
        at(50f, 60f, 0.6f) { bicycle(S_BLACK, 3f) }
    }
}

private fun ArtCanvas.contraflowBikes() {
    squareBlank(S_BLUE)
    arrow(34f, 88f, 34f, 14f, 10f, 26f, 20f, S_WHITE)
    rect(55f, 10f, 56.6f, 90f, fill(S_WHITE))
    arrow(72f, 40f, 72f, 86f, 5f, 15f, 11f, S_WHITE)
    at(72f, 24f, 0.5f) { bicycle(S_WHITE, 3f) }
}

private fun ArtCanvas.weighStation() {
    rectBlank(96f, 70f, S_BLUE)
    at(50f, 42f, 0.9f) { truckSide(S_WHITE, S_BLUE) }
    rect(18f, 58f, 82f, 62f, fill(S_WHITE))
    path(ArtPath.svg("M30 62 L34 70 L66 70 L70 62"), stroke(S_WHITE, 2.4f))
    text("ГВК", 50f, 78f, 7f, S_WHITE, condensed = true)
}
