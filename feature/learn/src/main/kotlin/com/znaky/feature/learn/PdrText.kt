package com.znaky.feature.learn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ---- Full official ПДР text, point by point -------------------------------------------------
// Data: assets/pdr/pdr.json, generated from the official text (zakon.rada.gov.ua, постанова КМУ
// № 1306) by tools/content/pdr_parse.py. Sections 1–34 in order; every numbered point kept,
// including the ones the rules mark as deleted, so the numbering reads exactly like the book.

@Serializable
internal data class PdrBook(
    val source: String,
    val sourceUrl: String,
    val edition: String,
    val sections: List<PdrSection>,
)

@Serializable
internal data class PdrSection(
    val n: Int,
    val title: String,
    val intro: List<PdrBlock> = emptyList(),
    val footnote: List<PdrBlock> = emptyList(),
    val points: List<PdrPoint>,
) {
    /** Real points (not group notes), for counters such as «16 пунктів». */
    val pointCount: Int get() = points.count { !it.note }

    /** Rough reading time at ~900 characters a minute. */
    val readMinutes: Int get() = (points.sumOf { p -> p.blocks.sumOf { it.length } } / 900).coerceAtLeast(1)
}

@Serializable
internal data class PdrPoint(
    /** Point number as printed: «16.11», «31.4.2»; in sections 33/34 the sign or marking code. */
    val n: String,
    /** Sub-heading printed before this point («Регульовані перехрестя», «3. Заборонні знаки»). */
    val heading: String? = null,
    val headingIntro: List<PdrBlock> = emptyList(),
    /** Sections 33/34: all codes the item covers, e.g. «1.4.1-1.4.7». */
    val codes: String? = null,
    /** A general note of a sign group («3.1-3.8, 3.11 не поширюються …»), not a new item. */
    val note: Boolean = false,
    /** The rules mark this point as deleted («виключено»). */
    val excluded: Boolean = false,
    val blocks: List<PdrBlock>,
) {
    val label: String get() = codes ?: n
}

/** t = "p" paragraph, "li" lettered/numbered sub-item, "note" примітка, "table" rows of cells. */
@Serializable
internal data class PdrBlock(
    val t: String,
    val text: String = "",
    val mark: String? = null,
    val rows: List<List<String>> = emptyList(),
) {
    val length: Int get() = text.length + rows.sumOf { r -> r.sumOf { it.length } }
}

internal object PdrText {
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile private var cached: PdrBook? = null

    suspend fun load(context: Context): PdrBook = cached ?: withContext(Dispatchers.IO) {
        context.assets.open("pdr/pdr.json").bufferedReader().use { json.decodeFromString(PdrBook.serializer(), it.readText()) }
            .also { cached = it }
    }
}

// ---- Links from section 33 to the sign catalogue -------------------------------------------

private val CODE = Regex("""\d{1,2}\.\d{1,2}(?:\.\d{1,2})?""")
private val RANGE = Regex("""^(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)\s*[-–]\s*(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?)$""")

/** «1.4.1-1.4.7» → 1.4.1 … 1.4.7; «5.82.1 і 5.83.1» → both; «3.1, 3.2» → both. */
internal fun expandCodes(codes: String): List<String> = codes.split(',', 'і').map { it.trim() }.filter { it.isNotEmpty() }.flatMap { part ->
    val m = RANGE.matchEntire(part)
    if (m == null) {
        CODE.findAll(part).map { it.value }.toList()
    } else {
        val (a, b) = m.destructured
        val pa = a.split('.')
        val pb = b.split('.')
        if (pa.dropLast(1) == pb.dropLast(1)) {
            (pa.last().toInt()..pb.last().toInt()).map { (pa.dropLast(1) + it.toString()).joinToString(".") }
        } else {
            listOf(a, b)
        }
    }
}

/** The sign name the rules print in quotes: «Небезпечний поворот праворуч». */
internal val PdrPoint.officialName: String?
    get() = blocks.firstOrNull()?.text?.let { Regex("«([^»]+)»").find(it)?.groupValues?.get(1) }

private fun String.key() = lowercase().replace(Regex("[^а-яіїєґ]"), "").take(8)

/**
 * Catalogue signs drawn for a section-33 item. A sign is linked only when the catalogue
 * name agrees with the official one, so a stale number never opens the wrong sign.
 */
internal fun PdrPoint.linkedSigns(): List<RoadSignInfo> {
    if (note) return emptyList()
    val name = officialName?.key()
    return expandCodes(codes ?: n).mapNotNull { SignCatalog.byCode(it) }
        .filter { name == null || it.name.key() == name }
}
