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
