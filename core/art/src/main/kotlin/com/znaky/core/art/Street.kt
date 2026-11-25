package com.znaky.core.art

/**
 * A top-down street kit for the rule scenes: a four-way crossing with curbs, sidewalks,
 * lane markings, houses and trees, plus cars, a traffic light and the main-road sign.
 * Scenes are drawn in a 100 × 100 space scaled to the canvas; roads span 41..59.
 */
object Street {
    class Palette(
        val grass: Long, val grass2: Long, val sidewalk: Long, val curb: Long, val asphalt: Long, val asphalt2: Long,
        val paint: Long, val tree: Long, val treeLight: Long, val shadow: Float, val roofs: LongArray, val walls: LongArray,
    )

    val Light = Palette(
        grass = 0xFFD3E6C1, grass2 = 0xFFC3DBAD, sidewalk = 0xFFE7E9EC, curb = 0xFFFAFBFC, asphalt = 0xFF5D6672, asphalt2 = 0xFF535C68,
        paint = 0xFFF8F8F4, tree = 0xFF5E9E5A, treeLight = 0xFF86BE73, shadow = 0.16f,
        roofs = longArrayOf(0xFFE9B9A0, 0xFFB9C7E6, 0xFFF0D7A4, 0xFFC5DCCF), walls = longArrayOf(0xFFF7EEE6, 0xFFEEF1F8, 0xFFFBF5E9, 0xFFEFF5F1),
    )
    val Dark = Palette(
        grass = 0xFF263A2B, grass2 = 0xFF223427, sidewalk = 0xFF3C4450, curb = 0xFF4C5562, asphalt = 0xFF2E353F, asphalt2 = 0xFF29303A,
        paint = 0xFFD9DEE4, tree = 0xFF2F5A36, treeLight = 0xFF3F7346, shadow = 0.32f,
        roofs = longArrayOf(0xFF7A5446, 0xFF4B5878, 0xFF7A6646, 0xFF46665A), walls = longArrayOf(0xFF3A3F48, 0xFF373D48, 0xFF3D3F45, 0xFF384240),
    )

    fun palette(dark: Boolean) = if (dark) Dark else Light

    /** Runs [block] in the 100 × 100 scene space of canvas [c]. */
    inline fun scene(c: ArtCanvas, block: ArtCanvas.() -> Unit) = c.layer {
        scale(c.width / 100f, c.height / 100f)
        clip(ArtPath.roundRect(0f, 0f, 100f, 100f, 0f))
        block()
    }

    fun crossroads(c: ArtCanvas, dark: Boolean, zebra: Boolean = false) = scene(c) {
        val p = palette(dark)
        rect(0f, 0f, 100f, 100f, fill(linear(0f, 0f, 0f, 100f, p.asphalt, p.asphalt2)))
        // Asphalt grain: a fixed scatter of specks, the same on every frame.
        var seed = 7
        repeat(180) {
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val x = (seed % 1000) / 10f
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val y = (seed % 1000) / 10f
            if (x in 40f..60f || y in 40f..60f) circle(x, y, 0.22f, fill(if (it % 2 == 0) 0xFFFFFFFF else 0xFF000000, 0.07f))
        }
        // Four corner blocks: sidewalk with a rounded curb, then a lawn.
        val corners = listOf(floatArrayOf(-6f, -6f, 41f, 41f), floatArrayOf(59f, -6f, 106f, 41f), floatArrayOf(-6f, 59f, 41f, 106f), floatArrayOf(59f, 59f, 106f, 106f))
        corners.forEach { (l, t, r, b) ->
            rect(l + 0.5f, t + 0.6f, r + 0.5f, b + 0.6f, fill(0xFF000000, p.shadow * 0.8f), radius = 6f)
            rect(l, t, r, b, fill(p.sidewalk), radius = 6f)
            rect(l, t, r, b, stroke(p.curb, 0.5f), radius = 6f)
            rect(l + 3f, t + 3f, r - 3f, b - 3f, fill(linear(l, t, r, b, p.grass, p.grass2)), radius = 3.5f)
        }
        // Centre lines (dashed), stopping short of the crossing.
        val dash = floatArrayOf(3.4f, 2.6f)
        line(50f, 0f, 50f, 37f, stroke(p.paint, 0.8f, Cap.BUTT, dash))
        line(50f, 63f, 50f, 100f, stroke(p.paint, 0.8f, Cap.BUTT, dash, phase = 1.3f))
        line(0f, 50f, 37f, 50f, stroke(p.paint, 0.8f, Cap.BUTT, dash))
        line(63f, 50f, 100f, 50f, stroke(p.paint, 0.8f, Cap.BUTT, dash, phase = 1.3f))
        // Stop lines on each approach (right-hand traffic).
        val stop = stroke(p.paint, 1.1f, Cap.BUTT)
        line(50.6f, if (zebra) 69.5f else 64f, 58.6f, if (zebra) 69.5f else 64f, stop)
        line(41.4f, 36f, 49.4f, 36f, stop)
        line(36f, 50.6f, 36f, 58.6f, stop)
        line(64f, 41.4f, 64f, 49.4f, stop)
        if (zebra) {
            var x = 42f
            while (x < 58.5f) { rect(x, 61.5f, x + 1.6f, 67.5f, fill(p.paint, 0.95f), radius = 0.2f); x += 3.2f }
        }
        // Houses and trees on the lawns.
        house(this, p, 6f, 7f, 26f, 22f, 0)
        house(this, p, 68f, 8f, 26f, 20f, 1)
        house(this, p, 8f, 68f, 24f, 22f, 2)
        house(this, p, 69f, 70f, 24f, 22f, 3)
        listOf(33f to 8f, 33f to 30f, 8f to 33f, 92f to 33f, 67f to 92f, 92f to 66f, 31f to 92f, 64f to 31.5f).forEach { (x, y) -> tree(this, p, x, y, 3.4f) }
    }

    private fun house(c: ArtCanvas, p: Palette, x: Float, y: Float, w: Float, h: Float, i: Int) = with(c) {
        rect(x + 1.6f, y + 2.2f, x + w + 1.6f, y + h + 2.2f, fill(0xFF000000, p.shadow), radius = 1.6f)
        rect(x, y, x + w, y + h, fill(p.walls[i]), radius = 1.6f)
        // A gable roof seen from above: two slopes meeting at the ridge.
        val roof = p.roofs[i]
        rect(x + 0.8f, y + 0.8f, x + w - 0.8f, y + h / 2, fill(Ink.shade(roof, 1.08f)), radius = 1f)
        rect(x + 0.8f, y + h / 2, x + w - 0.8f, y + h - 0.8f, fill(Ink.shade(roof, 0.86f)), radius = 1f)
        line(x + 0.8f, y + h / 2, x + w - 0.8f, y + h / 2, stroke(Ink.shade(roof, 0.7f), 0.5f, Cap.BUTT))
        rect(x + w * 0.7f, y + h * 0.18f, x + w * 0.7f + 2.4f, y + h * 0.18f + 2.4f, fill(Ink.shade(roof, 0.6f)), radius = 0.4f)
    }

    private fun tree(c: ArtCanvas, p: Palette, x: Float, y: Float, r: Float) = with(c) {
        circle(x + r * 0.35f, y + r * 0.5f, r, fill(0xFF000000, p.shadow))
        circle(x, y, r, fill(p.tree))
        circle(x - r * 0.25f, y - r * 0.3f, r * 0.62f, fill(p.treeLight))
        circle(x - r * 0.35f, y - r * 0.45f, r * 0.25f, fill(0xFFFFFFFF, 0.18f))
    }

    /**
     * A car seen from above at ([x], [y]) in scene units, nose pointing up when [angle] is 0.
     * [body] is the paint colour; the roof, glass and lights are derived from it.
     */
    fun car(c: ArtCanvas, x: Float, y: Float, angle: Float, body: Long, scale: Float = 1f) = scene(c) {
        layer {
            translate(x, y)
            rotate(angle)
            scale(scale)
            topCar(this, body)
        }
    }

    /** The car from above in the current coordinates: 6.4 wide, 12.4 long, centred, nose up. */
    fun topCar(c: ArtCanvas, body: Long) = with(c) {
        val w = 6.4f; val l = 12.4f
        val hw = w / 2; val hl = l / 2
        // Soft ground shadow.
        rect(-hw + 0.5f, -hl + 1f, hw + 0.9f, hl + 1.4f, fill(0xFF000000, 0.28f), radius = 2.6f)
        // Wheels peek out at the sides.
        for (wy in floatArrayOf(-hl + 2.6f, hl - 2.9f)) {
            rect(-hw - 0.35f, wy - 1.3f, -hw + 0.6f, wy + 1.3f, fill(0xFF14171C), radius = 0.4f)
            rect(hw - 0.6f, wy - 1.3f, hw + 0.35f, wy + 1.3f, fill(0xFF14171C), radius = 0.4f)
        }
        val shell = ArtPath()
            .moveTo(-hw + 1.2f, -hl).quadTo(0f, -hl - 0.5f, hw - 1.2f, -hl).quadTo(hw, -hl + 0.3f, hw, -hl + 2f)
            .lineTo(hw, hl - 1.6f).quadTo(hw, hl, hw - 1.4f, hl).lineTo(-hw + 1.4f, hl).quadTo(-hw, hl, -hw, hl - 1.6f)
            .lineTo(-hw, -hl + 2f).quadTo(-hw, -hl + 0.3f, -hw + 1.2f, -hl).close()
        path(shell, fill(linear(-hw, 0f, hw, 0f, Ink.shade(body, 0.8f), Ink.shade(body, 1.06f), Ink.shade(body, 0.8f))))
        // Mirrors.
        rect(-hw - 0.9f, -hl + 3.6f, -hw + 0.2f, -hl + 4.5f, fill(Ink.shade(body, 0.75f)), radius = 0.3f)
        rect(hw - 0.2f, -hl + 3.6f, hw + 0.9f, -hl + 4.5f, fill(Ink.shade(body, 0.75f)), radius = 0.3f)
        val glass = 0xFF1E2B3A
        // Windscreen, roof, rear window.
        path(ArtPath.roundedPolygon(0.6f, -hw + 0.7f, -hl + 3.2f, hw - 0.7f, -hl + 3.2f, hw - 1.1f, -hl + 5.6f, -hw + 1.1f, -hl + 5.6f),
            fill(linear(0f, -hl + 3.2f, 0f, -hl + 5.6f, 0xFF41566C, glass)))
        rect(-hw + 1.1f, -hl + 5.6f, hw - 1.1f, hl - 3.6f, fill(linear(-hw, 0f, hw, 0f, Ink.shade(body, 0.92f), Ink.shade(body, 1.14f), Ink.shade(body, 0.92f))), radius = 0.8f)
        path(ArtPath.roundedPolygon(0.5f, -hw + 1.1f, hl - 3.6f, hw - 1.1f, hl - 3.6f, hw - 0.8f, hl - 1.9f, -hw + 0.8f, hl - 1.9f), fill(glass))
        // Side windows as thin dark strips.
        rect(-hw + 0.35f, -hl + 5.4f, -hw + 0.95f, hl - 3.8f, fill(glass), radius = 0.3f)
        rect(hw - 0.95f, -hl + 5.4f, hw - 0.35f, hl - 3.8f, fill(glass), radius = 0.3f)
        // Sheen on the roof.
        rect(-hw + 1.6f, -hl + 6.2f, -hw + 2.4f, hl - 4.6f, fill(0xFFFFFFFF, 0.22f), radius = 0.4f)
        // Head and tail lights.
        rect(-hw + 0.6f, -hl + 0.1f, -hw + 2f, -hl + 0.9f, fill(0xFFFFF4C2), radius = 0.35f)
        rect(hw - 2f, -hl + 0.1f, hw - 0.6f, -hl + 0.9f, fill(0xFFFFF4C2), radius = 0.35f)
        rect(-hw + 0.5f, hl - 0.8f, -hw + 1.9f, hl - 0.1f, fill(0xFFE5484D), radius = 0.3f)
        rect(hw - 1.9f, hl - 0.8f, hw - 0.5f, hl - 0.1f, fill(0xFFE5484D), radius = 0.3f)
    }

    /** A traffic light on the corner at ([x], [y]) showing [lit] (0 red, 1 amber, 2 green). */
    fun trafficLight(c: ArtCanvas, x: Float, y: Float, lit: Int) = scene(c) {
        rect(x + 0.6f, y + 0.8f, x + 5.6f, y + 12.8f, fill(0xFF000000, 0.25f), radius = 1.4f)
        rect(x, y, x + 5f, y + 12f, fill(linear(x, 0f, x + 5f, 0f, 0xFF2A323C, 0xFF1A2027)), radius = 1.4f)
        val colors = longArrayOf(0xFFFF4D4D, 0xFFFFC233, 0xFF2EDB6E)
        for (i in 0..2) {
            val cy = y + 2.2f + i * 3.8f
            if (i == lit) {
                circle(x + 2.5f, cy, 2.6f, fill(radial(x + 2.5f, cy, 2.6f, Ink.alpha(colors[i], 0.55f), Ink.alpha(colors[i], 0f))))
                circle(x + 2.5f, cy, 1.35f, fill(colors[i]))
                circle(x + 2.1f, cy - 0.45f, 0.45f, fill(0xFFFFFFFF, 0.7f))
            } else circle(x + 2.5f, cy, 1.35f, fill(Ink.shade(colors[i], 0.32f)))
        }
    }

    /** The «Головна дорога» sign on a post, seen from above, at ([x], [y]). */
    fun mainRoadSign(c: ArtCanvas, x: Float, y: Float) = scene(c) {
        circle(x + 0.6f, y + 0.9f, 3.6f, fill(0xFF000000, 0.22f))
        layer {
            translate(x - 3.6f, y - 3.6f)
            scale(0.072f)
            RoadSigns.draw(object : ArtCanvas by this@scene { override val width = 100f; override val height = 100f }, "2.3")
        }
    }

    /** A green "you may go here" route. */
    fun route(c: ArtCanvas, path: ArtPath, color: Long = 0xFF22C55E) = scene(c) {
        path(path, stroke(0xFFFFFFFF, 3.6f, alpha = 0.5f))
        path(path, stroke(color, 2.4f))
    }
}
