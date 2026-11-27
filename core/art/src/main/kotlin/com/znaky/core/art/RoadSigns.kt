package com.znaky.core.art

/**
 * Road signs of section 33 of the Rules (all seven groups, with every sub-variant and plate),
 * drawn to the shapes and colours of ДСТУ 4100: a thin white outer edge, the red or blue
 * field, and pictograms with the standard's stroke weights. Every sign is drawn in a
 * 100 × 100 box; [draw] scales it to the canvas. Rectangular signs and plates sit centred
 * in that box.
 */
object RoadSigns {
    const val RED = S_RED
    const val BLUE = S_BLUE
    const val YELLOW = S_YELLOW
    const val GREEN = S_GREEN
    const val BLACK = S_BLACK
    const val WHITE = S_WHITE

    private val art: Map<String, SignArt> by lazy {
        LinkedHashMap<String, SignArt>().apply {
            putAll(warningSigns)
            putAll(prioritySigns)
            putAll(prohibitorySigns)
            putAll(mandatorySigns)
            putAll(infoSigns)
            putAll(serviceSigns)
            putAll(plateSigns)
        }
    }

    /** Codes of every sign that can be drawn, in the order of the Rules. */
    val codes: List<String> get() = art.keys.toList()

    fun has(code: String) = code in art

    /** Draws sign [code] centred in the canvas, as large as fits. */
    fun draw(c: ArtCanvas, code: String) {
        val block = art[code] ?: return
        val s = minOf(c.width, c.height) / 100f
        c.layer {
            translate((c.width - 100f * s) / 2, (c.height - 100f * s) / 2)
            scale(s)
            block()
        }
    }
}
