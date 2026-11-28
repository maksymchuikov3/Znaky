package com.znaky.core.art

/*
 * Pictograms shared by many signs, drawn around the origin about 50 units wide.
 * Place them with `at(cx, cy, k) { carSide(...) }`. [ink] is the symbol colour and
 * [bg] the sign field behind it (used to cut windows and wheel rims).
 */

private fun ArtCanvas.wheel(x: Float, y: Float, r: Float, ink: Long, bg: Long) {
    circle(x, y, r + 1.3f, fill(bg))
    circle(x, y, r, fill(ink))
    circle(x, y, r * 0.34f, fill(bg))
}

/** Passenger car seen from the side, facing right; ~44 × 25. */
internal fun ArtCanvas.carSide(ink: Long, bg: Long) {
    svgFill("M-22 4 L-22 -1.5 Q-22 -5 -18.6 -5.6 L-11.5 -6.6 L-5.4 -12.8 Q-3.8 -14.4 -1.4 -14.4 L8.6 -14.4 Q11 -14.4 12.6 -12.4 L16.8 -6.8 L19.8 -5.9 Q22 -5.2 22 -2.2 L22 4 Q22 6 20 6 L-20 6 Q-22 6 -22 4 Z", ink)
    svgFill("M-8.6 -7.2 L-4.4 -11.6 Q-3.6 -12.3 -2.4 -12.3 L1 -12.3 L1 -7.2 Z", bg)
    svgFill("M3.2 -7.2 L3.2 -12.3 L8.4 -12.3 Q9.6 -12.3 10.4 -11.3 L13.6 -7.2 Z", bg)
    wheel(-12f, 6f, 5f, ink, bg)
    wheel(12.5f, 6f, 5f, ink, bg)
}

/** Lorry from the side, cab on the right; ~50 × 27. */
internal fun ArtCanvas.truckSide(ink: Long, bg: Long, trailer: Boolean = false, plate: Long? = null) {
    val dx = if (trailer) 8f else 0f
    layer {
        translate(dx, 0f)
        rect(-25f, -15f, 6.5f, 3f, fill(ink), radius = 1f)
        svgFill("M8 4.5 L8 -10 Q8 -11.4 9.4 -11.4 L17 -11.4 Q19 -11.4 20 -9.6 L23.6 -3.4 Q24.6 -2.2 24.6 0 L24.6 3.4 Q24.6 4.5 23.5 4.5 Z", ink)
        svgFill("M10.6 -9 L16.6 -9 Q17.6 -9 18 -8.2 L20.6 -3.6 L10.6 -3.6 Z", bg)
        rect(-25f, 1.6f, 24.6f, 5f, fill(ink), radius = 0.8f)
        if (plate != null) {
            rect(-17f, -11f, -3f, -1f, fill(bg))
            rect(-16f, -10f, -4f, -2f, fill(plate))
            rect(-16f, -6.4f, -4f, -5.6f, fill(ink))
        }
        wheel(-16f, 6.5f, 4.6f, ink, bg)
        wheel(-6.5f, 6.5f, 4.6f, ink, bg)
        wheel(15.5f, 6.5f, 4.6f, ink, bg)
    }
    if (trailer) {
        rect(-34f, 0.6f, -16f, 2.2f, fill(ink))
        rect(-44f, -11f, -27f, 3f, fill(ink), radius = 1f)
        wheel(-35.5f, 6.5f, 4.4f, ink, bg)
    }
}

/** Bus from the side, facing right; ~50 × 27. */
internal fun ArtCanvas.busSide(ink: Long, bg: Long) {
    svgFill("M-25 4 L-25 -13 Q-25 -16 -22 -16 L20 -16 Q23 -16 24 -13.4 L25 -9 L25 4 Q25 6 23 6 L-23 6 Q-25 6 -25 4 Z", ink)
    for (i in 0 until 5) rect(-21.5f + i * 8.2f, -12.6f, -15.4f + i * 8.2f, -5.4f, fill(bg), radius = 0.8f)
    svgFill("M20 -12.6 L22 -12.6 Q22.6 -12.6 22.8 -12 L23.4 -5.4 L20 -5.4 Z", bg)
    rect(-25f, -1.6f, 25f, -0.6f, fill(bg))
    wheel(-14f, 6f, 4.8f, ink, bg)
    wheel(14f, 6f, 4.8f, ink, bg)
}

/** Trolleybus: a bus with two poles. */
internal fun ArtCanvas.trolleybusSide(ink: Long, bg: Long) {
    busSide(ink, bg)
    poly(ink, 1.5f, -6f, -16f, -18f, -26f, cap = Cap.BUTT)
    poly(ink, 1.5f, -2f, -16f, -13f, -26f, cap = Cap.BUTT)
}

/** Tram seen from the front with its pantograph; ~30 × 44. */
internal fun ArtCanvas.tramFront(ink: Long, bg: Long) {
    poly(ink, 1.6f, -6f, -16f, 4f, -22f, -4f, -26f, cap = Cap.BUTT)
    rect(-8f, -26.8f, 8f, -25.2f, fill(ink))
    svgFill("M-13 18 L-13 -12 Q-13 -16 -9 -16 L9 -16 Q13 -16 13 -12 L13 18 Z", ink)
    rect(-10f, -12.5f, 10f, -1f, fill(bg), radius = 1.2f)
    circle(-7.6f, 9f, 2f, fill(bg))
    circle(7.6f, 9f, 2f, fill(bg))
    rect(-4f, 3f, 4f, 5f, fill(bg))
    rect(-11f, 18f, -6f, 22f, fill(ink))
    rect(6f, 18f, 11f, 22f, fill(ink))
}

/** Motorcycle from the side, facing right; ~48 × 26. */
internal fun ArtCanvas.motoSide(ink: Long, bg: Long) {
    circle(-14f, 5f, 7.2f, stroke(ink, 2.8f))
    circle(15f, 5f, 7.2f, stroke(ink, 2.8f))
    circle(-14f, 5f, 1.6f, fill(ink)); circle(15f, 5f, 1.6f, fill(ink))
    poly(ink, 2.6f, -14f, 5f, -6f, -2f, 6f, -2f)
    poly(ink, 2.6f, 15f, 5f, 10.5f, -10f, 7f, -14.5f)
    poly(ink, 2.4f, 7f, -14.5f, 13f, -14.5f)
    svgFill("M-9.5 -3 Q-11 -9 -4 -9.6 L5.4 -10.2 Q9.6 -10.4 9.6 -6.6 L8.6 -2.6 Z", ink)
    svgFill("M-17 -6 Q-17 -9.4 -12.4 -9.4 L-4 -9.4 L-5 -6.2 Z", ink)
    svgFill("M-5 -2 L5 -2 L3 4 L-3.6 4 Z", ink)
}

/** Moped / scooter with a step-through frame; ~44 × 26. */
internal fun ArtCanvas.mopedSide(ink: Long, bg: Long) {
    circle(-13f, 6f, 6f, stroke(ink, 2.6f))
    circle(14f, 6f, 6f, stroke(ink, 2.6f))
    circle(-13f, 6f, 1.4f, fill(ink)); circle(14f, 6f, 1.4f, fill(ink))
    svgFill("M-19 0 Q-19 -4 -14 -4.6 L-2 -4.6 L1.8 3 L8 3 Q9 3 9.4 2 L11 -10 L13.4 -10 L11.6 3 Q11 6.4 7.6 6.4 L-4.6 6.4 Q-6 6.4 -6.6 5.2 L-8 2 L-17 2 Q-19 2 -19 0 Z", ink)
    svgFill("M-15 -7.6 Q-15 -9.6 -12.6 -9.6 L-4 -9.6 L-4.6 -6.2 L-15 -6.2 Z", ink)
    poly(ink, 2.4f, 12.2f, -10f, 9.6f, -15f, 15.4f, -15f)
    poly(ink, 2.2f, 14f, 6f, 12f, -9f)
}

/** Bicycle from the side, facing right; ~44 × 24. */
internal fun ArtCanvas.bicycle(ink: Long, w: Float = 2.6f) {
    circle(-13f, 5f, 8f, stroke(ink, w))
    circle(13.5f, 5f, 8f, stroke(ink, w))
    poly(ink, w, -13f, 5f, -1f, 5f, -6.4f, -7.4f, -13f, 5f)
    poly(ink, w, -1f, 5f, 9.4f, -6.6f, -5f, -6.6f)
    poly(ink, w, 13.5f, 5f, 8.6f, -11f, 5f, -12.6f)
    poly(ink, w * 1.2f, -10f, -9.6f, -3.4f, -9.6f)
}

/** Agricultural tractor, facing right; ~46 × 30. */
internal fun ArtCanvas.tractorSide(ink: Long, bg: Long) {
    svgFill("M-14 -2 L-14 -21 L3 -21 L3 -19 L-1 -19 L0.4 -6 L16 -6 Q20 -6 20 -2 L20 4 L-14 4 Z", ink)
    svgFill("M-11.6 -18.6 L-2.8 -18.6 L-1.8 -8 L-11.6 -8 Z", bg)
    rect(11f, -13f, 13.2f, -6f, fill(ink))
    circle(-10f, 4f, 11.6f, fill(bg))
    circle(-10f, 4f, 10.2f, fill(ink))
    circle(-10f, 4f, 4.2f, fill(bg))
    circle(-10f, 4f, 2f, fill(ink))
    circle(15f, 7.6f, 7.2f, fill(bg))
    circle(15f, 7.6f, 6f, fill(ink))
    circle(15f, 7.6f, 2.2f, fill(bg))
}

/** Horse-drawn cart going right; ~56 × 28. */
internal fun ArtCanvas.horseCart(ink: Long, bg: Long) {
    layer { translate(13f, 0f); horse(ink) }
    rect(-30f, -6f, -6f, 0f, fill(ink), radius = 0.8f)
    poly(ink, 1.8f, -6f, -3f, 6f, -4f)
    circle(-18f, 4f, 7.6f, stroke(ink, 2.2f))
    circle(-18f, 4f, 1.6f, fill(ink))
    for (a in 0 until 4) {
        val r = Math.toRadians(a * 45.0)
        line(-18f + 7f * kotlin.math.cos(r).toFloat(), 4f + 7f * kotlin.math.sin(r).toFloat(), -18f - 7f * kotlin.math.cos(r).toFloat(), 4f - 7f * kotlin.math.sin(r).toFloat(), stroke(ink, 1.1f))
    }
}

/** Horse standing, facing right; ~32 × 28, hooves at y = 12. */
internal fun ArtCanvas.horse(ink: Long) {
    svgFill(
        "M-13 -6 Q-12 -10 -6 -10 L6 -10 Q9 -10 10.5 -12.5 L13 -18 Q14.4 -20.6 16.6 -19.4 L19.4 -16.6 Q20.6 -15 19.6 -13.6 L18 -13 L15.4 -9.4 Q14 -6 13 -3 L12 1 " +
            "L13.6 11.6 L11 12 L9 3.4 L7.6 3.6 L6.6 12 L4 12 L4 2.6 L-6 2.6 L-7.6 11.8 L-10.2 12 L-10 2 L-11.4 3 L-13 11.4 L-15.4 11.2 L-13.8 -1 Q-15.4 2 -17.6 5 L-18.6 3.8 Q-15 -1 -13 -6 Z",
        ink,
    )
    shape(ink, 13f, -19f, 12.4f, -23f, 15.4f, -20f, round = 0.4f)
}

/** Rider on a horse, facing right. */
internal fun ArtCanvas.horseRider(ink: Long) {
    horse(ink)
    circle(1f, -25.5f, 3.2f, fill(ink))
    svgFill("M-2 -21.6 Q1 -22.6 3.4 -21.4 L4.6 -13.6 L2.6 -6.4 L-1 -6.4 L-0.4 -11 Z", ink)
    poly(ink, 2f, 3f, -19f, 7.6f, -14.6f, 10.6f, -14f)
}

/** Person walking right; hip at origin, ~28 tall at k = 1. Reuses the pedestrian used on 1.32. */
internal fun ArtCanvas.pedestrian(ink: Long, facing: Int = 1) {
    val f = facing.toFloat()
    circle(2.6f * f, -22.5f, 4.6f, fill(ink))
    path(ArtPath().moveTo(2f * f, -15f).lineTo(0f, 0f), stroke(ink, 6.4f))
    path(ArtPath().moveTo(0f, 0f).lineTo(5f * f, 7.5f).lineTo(7.5f * f, 15.5f), stroke(ink, 4.6f))
    path(ArtPath().moveTo(0f, 0f).lineTo(-3.5f * f, 8f).lineTo(-9f * f, 13.5f), stroke(ink, 4.6f))
    path(ArtPath().moveTo(1.8f * f, -13.5f).lineTo(-3.5f * f, -7f).lineTo(-5f * f, -1f), stroke(ink, 3.8f))
    path(ArtPath().moveTo(1.8f * f, -13.5f).lineTo(5.5f * f, -8f).lineTo(8f * f, -4f), stroke(ink, 3.8f))
}

/** Pedestrian pushing a hand cart (3.10). */
internal fun ArtCanvas.handCart(ink: Long, bg: Long) {
    layer { translate(-9f, -2f); scale(0.92f); pedestrian(ink) }
    poly(ink, 2.4f, -2f, -8f, 6f, -6f)
    rect(6f, -12f, 22f, 0f, fill(ink), radius = 1f)
    poly(ink, 2f, 6f, 0f, 6f, 4f)
    circle(16f, 6f, 4.6f, fill(ink))
    circle(16f, 6f, 1.6f, fill(bg))
}

/** Person in a wheelchair, facing right (7.17, 7.18). */
internal fun ArtCanvas.wheelchair(ink: Long, bg: Long) {
    circle(-3f, -20f, 4.2f, fill(ink))
    poly(ink, 4.6f, -4f, -13f, -5f, 1f, 7f, 1f, 10f, 12f, cap = Cap.ROUND)
    poly(ink, 3.4f, -4.4f, -9f, 5f, -9f)
    arc(-17f, -6f, 9f, 20f, 200f, 260f, stroke(ink, 2.8f, cap = Cap.BUTT))
}

/** Steam locomotive, facing left (1.28). */
internal fun ArtCanvas.locomotive(ink: Long, bg: Long) {
    svgFill("M-22 4 L-22 -6 Q-22 -9 -19 -9 L4 -9 L4 -18 L-2 -18 L-2 -21 L20 -21 L20 -18 L16 -18 L16 4 Z", ink)
    rect(-17f, -17f, -11.6f, -9f, fill(ink), radius = 0.6f)
    rect(-19f, -19f, -9.6f, -16.6f, fill(ink), radius = 0.6f)
    rect(7f, -15f, 13f, -9.4f, fill(bg), radius = 0.6f)
    shape(ink, -22f, 0f, -27f, 8f, -21f, 8f, round = 0.4f)
    rect(-24f, 4f, 20f, 6f, fill(ink))
    listOf(-15f, -6f, 3f).forEach { circle(it, 8f, 4.2f, fill(bg)); circle(it, 8f, 3.4f, fill(ink)) }
    circle(12.6f, 7f, 5.4f, fill(bg)); circle(12.6f, 7f, 4.6f, fill(ink))
}

/** Cow, facing left (1.35). */
internal fun ArtCanvas.cow(ink: Long) {
    svgFill(
        "M-14 -8 L-19 -11 Q-21.4 -12 -23 -9 L-25 -4 Q-25.6 -2 -23.6 -1.4 L-20 -0.4 L-16.6 -2 L-16 4 L-17 13 L-14.2 13 L-12.6 5 L8 5 L9.2 13 L12 13 L12.4 4 Q15 1 15.4 -4 L17 4 L18.4 4 L17.6 -6 Q16.6 -10 12 -10 L-10 -10 Q-12.6 -10 -14 -8 Z",
        ink,
    )
    shape(ink, -18f, -11f, -17f, -15f, -15.4f, -11.4f, round = 0.3f)
    shape(ink, -22f, -10.6f, -24f, -14.6f, -20f, -11.4f, round = 0.3f)
    rect(-4f, 4f, 2f, 7f, fill(ink), radius = 1.2f)
}

/** Leaping deer, facing right (1.36). */
internal fun ArtCanvas.deer(ink: Long) {
    svgFill(
        "M-16 -2 Q-18 -6 -15 -8 L-6 -9 L8 -10 Q11 -12 12.4 -16 L13.6 -19 Q15.4 -21 17.4 -19.6 L20.6 -17 Q21.8 -15.6 20.6 -14.6 L17 -14 Q15.6 -9 14 -6 L16 2 L22 10 L19.6 11.4 L12.6 4 L10 -1 L-2 0 L-10 2 L-17 9 L-19.4 7.4 L-14 0 Z",
        ink,
    )
    poly(ink, 1.5f, 14f, -19f, 12f, -26f, 9f, -28f)
    poly(ink, 1.5f, 12.8f, -23.4f, 16f, -27.4f)
    poly(ink, 1.5f, 15.8f, -19.6f, 17f, -26f, 20f, -28f)
    poly(ink, 1.5f, 16.6f, -23.8f, 13.6f, -27.6f)
}

/** Airplane in profile climbing to the right (1.18). */
internal fun ArtCanvas.airplane(ink: Long) {
    svgFill("M-22 6 L-18 4 L12 -8 Q19 -11 22 -9.4 Q23 -7.4 17 -4 L-12 9 Z", ink)
    svgFill("M-4 1 L-12 -14 L-7.6 -15 L6 -3 Z", ink)
    svgFill("M-3 3 L4 13 L8.4 12 L4.6 0 Z", ink)
    svgFill("M-19 3.6 L-24 -6 L-20.6 -6.6 L-14 1.4 Z", ink)
}

/** Wind sock on a pole (1.17). */
internal fun ArtCanvas.windsock(ink: Long) {
    rect(-20f, -16f, -17.6f, 16f, fill(ink))
    svgFill("M-17.6 -16 L18 -11 L18 -3 L-17.6 2 Z", ink)
    rect(-4f, -14.4f, -0.6f, 0.6f, fill(S_WHITE))
    rect(8f, -12.4f, 11f, -1.6f, fill(S_WHITE))
}

/** Traffic light with lit red, amber and green lenses (1.24). */
internal fun ArtCanvas.trafficLight() {
    rect(-8f, -23f, 8f, 23f, fill(S_BLACK), radius = 4f)
    circle(0f, -14f, 5.2f, fill(S_RED))
    circle(0f, 0f, 5.2f, fill(S_YELLOW))
    circle(0f, 14f, 5.2f, fill(S_GREEN))
}

/** Worker with a shovel next to a heap (1.37). */
internal fun ArtCanvas.roadWorker(ink: Long) {
    circle(-1f, -21f, 4.2f, fill(ink))
    svgFill("M-5 -14 Q-1 -16 3 -14 L5 -1 L9 12 L5.6 13 L1.6 3 L-2 13 L-5.6 12 L-3.4 0 Z", ink)
    poly(ink, 3.2f, -3f, -11f, -10f, -3f, -13f, 2f)
    poly(ink, 3.2f, 2.6f, -11f, -6f, -6f)
    poly(ink, 1.8f, -12f, -10f, -19f, 4f)
    shape(ink, -21f, 2f, -16.4f, 4.6f, -19f, 9f, -23f, 7f, round = 0.4f)
    svgFill("M8 13 Q14 2 24 13 Z", ink)
}

/** Fuel pump (6.7.x). */
internal fun ArtCanvas.fuelPump(ink: Long, bg: Long) {
    rect(-14f, -18f, 4f, 18f, fill(ink), radius = 2f)
    rect(-10.6f, -14.6f, 0.6f, -5f, fill(bg), radius = 1f)
    rect(-17f, 15f, 7f, 19f, fill(ink), radius = 1f)
    path(ArtPath.svg("M4 -6 L9 -6 Q11 -6 11 -4 L11 8 Q11 11 13.6 11 Q16 11 16 8 L16 -10 L12 -15"), stroke(ink, 2.4f))
}
