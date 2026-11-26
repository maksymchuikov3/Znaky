package com.znaky.core.art

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Building blocks for road signs: the sign blanks of ДСТУ 4100 and the stroke-weight
 * conventions every pictogram shares. All signs are drawn in a 100 × 100 box.
 */

internal const val S_RED = 0xFFD42A20
internal const val S_BLUE = 0xFF1C5BB4
internal const val S_YELLOW = 0xFFFFC21A
internal const val S_GREEN = 0xFF0B7A3E
internal const val S_BROWN = 0xFF7A4A26
internal const val S_ORANGE = 0xFFF08A1C
internal const val S_BLACK = 0xFF16181B
internal const val S_WHITE = 0xFFFFFFFF
internal const val S_GREY = 0xFF8A9099
/** Hairline around the white edge, so white signs keep their outline on white screens. */
internal const val S_EDGE = 0xFFB4BCC6

internal typealias SignArt = ArtCanvas.() -> Unit

/** Draws [block] with the origin moved to ([cx], [cy]) and scaled by [k]. */
internal inline fun ArtCanvas.at(cx: Float, cy: Float, k: Float = 1f, block: ArtCanvas.() -> Unit) = layer {
    translate(cx, cy)
    if (k != 1f) scale(k)
    block()
}

/** Mirrors [block] left↔right around x = [cx]. */
internal inline fun ArtCanvas.mirrored(cx: Float = 50f, block: ArtCanvas.() -> Unit) = layer {
    translate(2 * cx, 0f)
    scale(-1f, 1f)
    block()
}

internal fun ArtCanvas.svgFill(d: String, color: Long) = path(ArtPath.svg(d), fill(color))

// ---- Blanks ---------------------------------------------------------------------------

/** Equilateral triangle with side ~94, as points inset by [d] from the outer edge. */
internal fun signTri(d: Float, up: Boolean): FloatArray {
    val h = 94f * sqrt(3f) / 2
    val top = (100f - h) / 2 + if (up) 3f else -3f
    val cy = if (up) top + h * 2 / 3 else top + h / 3
    val k = (h / 3 - d) / (h / 3)
    val pts = if (up) floatArrayOf(50f, top, 97f, top + h, 3f, top + h) else floatArrayOf(3f, top, 97f, top, 50f, top + h)
    for (i in pts.indices step 2) { pts[i] = 50f + (pts[i] - 50f) * k; pts[i + 1] = cy + (pts[i + 1] - cy) * k }
    return pts
}

internal fun ArtCanvas.triangleBlank(up: Boolean, field: Long = S_WHITE) {
    path(ArtPath.roundedPolygon(7f, *signTri(0f, up)), fill(S_WHITE))
    path(ArtPath.roundedPolygon(7f, *signTri(0f, up)), stroke(S_EDGE, 0.6f))
    path(ArtPath.roundedPolygon(5.6f, *signTri(1.8f, up)), fill(S_RED))
    path(ArtPath.roundedPolygon(2.6f, *signTri(10.2f, up)), fill(field))
}

/** Warning sign; the pictogram is centred around (50, 62). */
internal fun ArtCanvas.warning(glyph: ArtCanvas.() -> Unit) { triangleBlank(true); glyph() }

internal fun ArtCanvas.roundBlank(field: Long, ring: Long? = S_RED) {
    circle(50f, 50f, 48.5f, fill(S_WHITE))
    circle(50f, 50f, 48.5f, stroke(S_EDGE, 0.6f))
    if (ring != null) {
        circle(50f, 50f, 46.8f, fill(ring))
        circle(50f, 50f, 37f, fill(field))
    } else circle(50f, 50f, 46.8f, fill(field))
}

internal fun ArtCanvas.prohibitory(glyph: ArtCanvas.() -> Unit = {}) { roundBlank(S_WHITE); glyph() }

/** Prohibitory sign with the red diagonal bar over the pictogram (3.2–3.10, 3.19–3.22…). */
internal fun ArtCanvas.prohibitoryBar(glyph: ArtCanvas.() -> Unit) {
    roundBlank(S_WHITE)
    glyph()
    layer {
        clip(ArtPath.circle(50f, 50f, 38f))
        line(24f, 24f, 76f, 76f, stroke(S_RED, 7.6f, cap = Cap.BUTT))
    }
}

/** White sign with the black diagonal band that cancels a restriction (3.26, 3.28, 3.30, 3.31). */
internal fun ArtCanvas.endOfRestriction(band: Int = 5, glyph: ArtCanvas.() -> Unit = {}) {
    circle(50f, 50f, 48.5f, fill(S_WHITE))
    circle(50f, 50f, 48.5f, stroke(S_EDGE, 0.6f))
    circle(50f, 50f, 46.6f, stroke(S_GREY, 1.2f))
    layer { glyph() }
    layer {
        clip(ArtPath.circle(50f, 50f, 46f))
        for (i in 0 until band) {
            val o = (i - (band - 1) / 2f) * 3.6f
            line(16f + o, 84f + o, 84f + o, 16f + o, stroke(S_BLACK, 1.6f, cap = Cap.BUTT))
        }
    }
}

internal fun ArtCanvas.mandatory(glyph: ArtCanvas.() -> Unit) { roundBlank(S_BLUE, ring = null); glyph() }

internal fun ArtCanvas.squareBlank(field: Long) {
    rect(2f, 2f, 98f, 98f, fill(S_WHITE), radius = 9f)
    rect(2f, 2f, 98f, 98f, stroke(S_EDGE, 0.6f), radius = 9f)
    rect(3.8f, 3.8f, 96.2f, 96.2f, fill(field), radius = 7.5f)
}

/** A rectangular sign [w] × [h] (in sign units, ≤ 96) centred in the box. */
internal fun ArtCanvas.rectBlank(w: Float, h: Float, field: Long, border: Long? = null, cy: Float = 50f) {
    val l = 50f - w / 2; val t = cy - h / 2; val r = 50f + w / 2; val b = cy + h / 2
    rect(l, t, r, b, fill(S_WHITE), radius = 6f)
    rect(l, t, r, b, stroke(S_EDGE, 0.6f), radius = 6f)
    rect(l + 1.6f, t + 1.6f, r - 1.6f, b - 1.6f, fill(field), radius = 4.8f)
    if (border != null) rect(l + 3.2f, t + 3.2f, r - 3.2f, b - 3.2f, stroke(border, 1.2f), radius = 3.6f)
}

/** Blue square with a white inner square (signs of group 6 and many of group 5). */
internal fun ArtCanvas.serviceBlank(field: Long = S_BLUE) {
    rectBlank(80f, 96f, field)
    rect(18f, 14f, 82f, 72f, fill(S_WHITE), radius = 2.6f)
}

/** Table plate (group 7): white with a thin black frame, [h] tall. */
internal fun ArtCanvas.plate(h: Float = 50f, w: Float = 92f, content: ArtCanvas.() -> Unit) {
    rectBlank(w, h, S_WHITE, border = S_BLACK)
    content()
}

// ---- Pictogram parts ------------------------------------------------------------------

/** Straight arrow from ([x0],[y0]) to the tip ([x1],[y1]): shaft [w] wide, head [hw] wide and [hl] long. */
internal fun ArtCanvas.arrow(x0: Float, y0: Float, x1: Float, y1: Float, w: Float, hw: Float, hl: Float, color: Long) {
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

/** Arrow head only, tip at ([x],[y]) pointing at [deg] (0 = right, 90 = down). */
internal fun ArtCanvas.arrowHead(x: Float, y: Float, deg: Float, hw: Float, hl: Float, color: Long) {
    val a = Math.toRadians(deg.toDouble())
    val ux = cos(a).toFloat(); val uy = sin(a).toFloat()
    val bx = x - ux * hl; val by = y - uy * hl
    path(ArtPath().moveTo(x, y).lineTo(bx - uy * hw / 2, by + ux * hw / 2).lineTo(bx + uy * hw / 2, by - ux * hw / 2).close(), Paint(color, roundJoin = false))
}

/** Arrow that goes up from ([x], [y0]) and bends towards [dir] (+1 right, −1 left), tip at ([tipX], [tipY]). */
internal fun ArtCanvas.bentArrow(x: Float, y0: Float, tipX: Float, tipY: Float, dir: Int, w: Float, hw: Float, hl: Float, color: Long, r: Float = 11f) {
    val baseX = tipX - dir * hl
    path(
        ArtPath().moveTo(x, y0).lineTo(x, tipY + r).quadTo(x, tipY, x + dir * r, tipY).lineTo(baseX + dir * 0.5f, tipY),
        stroke(color, w, cap = Cap.BUTT, roundJoin = false),
    )
    path(ArtPath().moveTo(tipX, tipY).lineTo(baseX, tipY - hw / 2).lineTo(baseX, tipY + hw / 2).close(), Paint(color, roundJoin = false))
}

/** U-turn arrow: up from ([x], [y0]), round over to the left, down to [y1]. */
internal fun ArtCanvas.uTurnArrow(x: Float, y0: Float, y1: Float, span: Float, w: Float, hw: Float, hl: Float, color: Long) {
    val r = span / 2
    val top = 30f
    path(
        ArtPath().moveTo(x, y0).lineTo(x, top + r).cubicTo(x, top - r * 0.33f, x - span, top - r * 0.33f, x - span, top + r).lineTo(x - span, y1 - hl + 0.5f),
        stroke(color, w, cap = Cap.BUTT, roundJoin = false),
    )
    arrowHead(x - span, y1, 90f, hw, hl, color)
}

/** Polyline stroke helper. */
internal fun ArtCanvas.poly(color: Long, w: Float, vararg pts: Float, cap: Cap = Cap.ROUND) {
    val p = ArtPath().moveTo(pts[0], pts[1])
    for (i in 2 until pts.size step 2) p.lineTo(pts[i], pts[i + 1])
    path(p, stroke(color, w, cap = cap))
}

/** Filled polygon helper. */
internal fun ArtCanvas.shape(color: Long, vararg pts: Float, round: Float = 0.6f) = path(ArtPath.roundedPolygon(round, *pts), fill(color))

/** Three arrows chasing each other counter-clockwise around ([cx], [cy]), as on 1.19 and 4.10. */
internal fun ArtCanvas.roundaboutArrows(cx: Float, cy: Float, r: Float, w: Float, hw: Float, hl: Float, color: Long) {
    val headDeg = Math.toDegrees((hl / r).toDouble()).toFloat()
    for (i in 0 until 3) {
        val start = -24f + i * 120f
        val tipDeg = start - 92f
        val baseDeg = tipDeg + headDeg
        arc(cx - r, cy - r, cx + r, cy + r, start, baseDeg - start - 2f, stroke(color, w, cap = Cap.BUTT))
        val t = Math.toRadians(tipDeg.toDouble()); val b = Math.toRadians(baseDeg.toDouble())
        val tx = cx + r * cos(t).toFloat(); val ty = cy + r * sin(t).toFloat()
        val bx = cx + r * cos(b).toFloat(); val by = cy + r * sin(b).toFloat()
        val dir = Math.toDegrees(kotlin.math.atan2((ty - by).toDouble(), (tx - bx).toDouble())).toFloat()
        arrowHead(tx, ty, dir, hw, hl, color)
    }
}
