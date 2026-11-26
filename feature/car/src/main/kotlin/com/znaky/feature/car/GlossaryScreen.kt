package com.znaky.feature.car

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.SearchField
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import java.text.Collator
import java.util.Locale

internal enum class TermCategory(val label: String, val color: Color) {
    PDR("ПДР", Color(0xFF2F6BFF)),
    TECH("Будова", Color(0xFFE8780C)),
    SAFETY("Безпека", Color(0xFFD93A4C)),
    ELECTRIC("Електро", Color(0xFF14A06B)),
    MOTO("Мото", Color(0xFF7A4BE0)),
}

internal data class GlossaryTerm(
    val term: String,
    val category: TermCategory,
    val text: String,
    val source: String,
    val synonyms: String? = null,
)

private fun String.norm() = lowercase(Locale.ROOT).replace("’", "").replace("'", "").replace("ʼ", "")

/** Filters by category and query; name matches come first, then matches in the explanation. */
internal fun filterGlossary(terms: List<GlossaryTerm>, category: TermCategory?, query: String): List<GlossaryTerm> {
    val q = query.trim().norm()
    val collator = Collator.getInstance(Locale.forLanguageTag("uk"))
    return terms
        .filter { category == null || it.category == category }
        .filter { q.isEmpty() || it.term.norm().contains(q) || it.text.norm().contains(q) || it.synonyms?.norm()?.contains(q) == true }
        .sortedWith(
            compareBy<GlossaryTerm> {
                val n = it.term.norm()
                when {
                    q.isEmpty() -> 0
                    n.startsWith(q) -> 0
                    n.contains(q) -> 1
                    else -> 2
                }
            }.thenComparator { a, b -> collator.compare(a.term, b.term) },
        )
}

/** «Словник водія»: ПДР definitions and car, safety, EV and motorcycle terms in plain words. */
@Composable
internal fun GlossaryScreen(onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val category = categoryName?.let { TermCategory.valueOf(it) }
    val results = remember(query, category) { filterGlossary(GlossaryTerms, category, query) }
    val grouped = remember(results, query) {
        if (query.isBlank()) results.groupBy { it.term.first().uppercaseChar().toString() } else mapOf("" to results)
    }

    LazyColumn(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Будова авто") }
        item(key = "title") {
            LargeTitle("Словник водія", subtitle = "Терміни з ПДР і будови авто простими словами")
        }
        item(key = "search") {
            SearchField(query, { query = it }, "Пошук: кліренс, ABS, обгін…", Modifier.padding(top = 14.dp))
        }
        item(key = "chips") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val all = category == null
                Pill(
                    "Усі ${GlossaryTerms.size}",
                    background = if (all) c.textPrimary else c.surface,
                    contentColor = if (all) c.background else c.textPrimary,
                    onClick = { categoryName = null },
                )
                TermCategory.entries.forEach { cat ->
                    val on = cat == category
                    Pill(
                        "${cat.label} ${GlossaryTerms.count { it.category == cat }}",
                        background = if (on) c.textPrimary else c.surface,
                        contentColor = if (on) c.background else c.textPrimary,
                        onClick = { categoryName = if (on) null else cat.name },
                    )
                }
            }
        }
        item(key = "count") {
            Text(
                termsCount(results.size),
                style = ZnakyType.subhead,
                color = c.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        if (results.isEmpty()) {
            item(key = "empty") {
                Text(
                    "Нічого не знайдено. Спробуйте інше слово.",
                    style = ZnakyType.callout,
                    color = c.textSecondary,
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                )
            }
        }
        grouped.forEach { (letter, terms) ->
            if (letter.isNotEmpty()) {
                item(key = "L-$letter") {
                    Text(
                        letter,
                        style = ZnakyType.caption,
                        color = c.textTertiary,
                        modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 6.dp).semantics { heading() },
                    )
                }
            }
            items(terms, key = { "T-${it.term}-${it.category}" }) { TermCard(it) }
        }
        item(key = "note") {
            Text(
                "Визначення з ПДР (п. 1.10) переказані своїми словами. Офіційне формулювання дивіться в тексті Правил дорожнього руху України.",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(20.dp).navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun TermCard(t: GlossaryTerm) {
    val c = ZnakyTheme.colors
    ZCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(), radius = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(t.term, style = ZnakyType.headline, color = c.textPrimary, modifier = Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(t.category.color.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(t.category.color))
                Text(t.category.label, style = ZnakyType.caption, color = t.category.color)
            }
        }
        Text(t.text, style = ZnakyType.callout, color = c.textPrimary, modifier = Modifier.padding(top = 6.dp))
        Column(Modifier.padding(top = 8.dp)) {
            Text("Джерело: ${t.source}", style = ZnakyType.subhead, color = c.textTertiary)
            if (t.synonyms != null) Text("Також: ${t.synonyms}", style = ZnakyType.subhead, color = c.textTertiary)
        }
    }
}

internal fun termsCount(n: Int): String {
    val f = n % 10
    val h = n % 100
    val word = when {
        f == 1 && h != 11 -> "термін"
        f in 2..4 && h !in 12..14 -> "терміни"
        else -> "термінів"
    }
    return "$n $word"
}
