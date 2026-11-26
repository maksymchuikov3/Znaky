package com.znaky.feature.car

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

internal data class GlossaryTerm(
    val term: String,
    val category: TermCategory,
    val text: String,
    val source: String,
    val see: String? = null,
    val synonyms: String? = null,
)

private fun String.norm() = lowercase(Locale.ROOT).replace("’", "").replace("'", "").replace("ʼ", "")

private val ukCollator: Collator = Collator.getInstance(Locale.forLanguageTag("uk"))

/** Filters by chapter and query; name matches come first, then matches in the explanation. */
internal fun filterGlossary(terms: List<GlossaryTerm>, category: TermCategory?, query: String): List<GlossaryTerm> {
    val q = query.trim().norm()
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
                    it.synonyms?.norm()?.contains(q) == true -> 1
                    else -> 2
                }
            }.thenComparator { a, b -> ukCollator.compare(a.term, b.term) },
        )
}

/**
 * «Словник водія» styled as a small rulebook: a cover, a table of contents with seven chapters,
 * chapter pages with an alphabet index, and a search across the whole book.
 */
@Composable
internal fun GlossaryScreen(onBack: () -> Unit) {
    val b = book
    var query by rememberSaveable { mutableStateOf("") }
    var chapterName by rememberSaveable { mutableStateOf<String?>(null) }
    val chapter = chapterName?.let { TermCategory.valueOf(it) }

    BackHandler(enabled = chapter != null || query.isNotEmpty()) {
        if (query.isNotEmpty()) query = "" else chapterName = null
    }

    Box(Modifier.fillMaxSize().background(b.paper)) {
        AnimatedContent(
            targetState = chapter,
            transitionSpec = {
                val forward = (targetState?.number ?: 0) > (initialState?.number ?: 0)
                val dir = if (forward) 1 else -1
                (slideInHorizontally(tween(280)) { it / 5 * dir } + fadeIn(tween(280)))
                    .togetherWith(slideOutHorizontally(tween(220)) { -it / 6 * dir } + fadeOut(tween(200)))
            },
            label = "book-page",
        ) { shown ->
            if (shown == null) {
                ContentsPage(
                    query = query,
                    onQuery = { query = it },
                    onBack = onBack,
                    onOpenChapter = { query = ""; chapterName = it.name },
                )
            } else {
                ChapterPage(
                    chapter = shown,
                    onBack = { chapterName = null },
                    onNext = { next -> chapterName = next.name },
                )
            }
        }
    }
}

// ---------------------------------------------------------------- contents

@Composable
private fun ContentsPage(query: String, onQuery: (String) -> Unit, onBack: () -> Unit, onOpenChapter: (TermCategory) -> Unit) {
    val b = book
    val results = remember(query) { if (query.isBlank()) emptyList() else filterGlossary(GlossaryTerms, null, query) }
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding()) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "cover") { Cover() }
        item(key = "search") { BookSearch(query, onQuery, Modifier.padding(top = 16.dp)) }
        if (query.isBlank()) {
            item(key = "contents-h") { SmallCapsHeader("Зміст", Modifier.padding(start = 24.dp, top = 22.dp, bottom = 8.dp)) }
            item(key = "contents") { Contents(onOpenChapter) }
            item(key = "note") { BookFootnote() }
        } else {
            item(key = "count") {
                Text(
                    if (results.isEmpty()) "Нічого не знайдено. Спробуйте інше слово." else termsCount(results.size),
                    style = ZnakyType.subhead,
                    color = b.inkSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
            searchResults(results, query)
            item(key = "end") { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}

@Composable
private fun Cover() {
    val b = book
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(b.page)
            .border(1.dp, b.rule, RoundedCornerShape(22.dp)),
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Spine of the book on the left edge.
            Box(Modifier.width(12.dp).fillMaxHeight().background(b.cover))
            Box(Modifier.width(2.dp).fillMaxHeight().background(b.cover.copy(alpha = 0.25f)))
            Column(Modifier.weight(1f).padding(start = 18.dp, end = 52.dp, top = 22.dp, bottom = 20.dp)) {
            Text("ДОВІДНИК ВОДІЯ · RUSHAI", style = BookType.smallCaps, color = b.cover)
            Text(
                "Словник водія",
                style = BookType.coverTitle,
                color = b.ink,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            Text(
                "Терміни Правил дорожнього руху, будова авто, мотоцикл і мале судно простими словами.",
                style = BookType.intro,
                color = b.inkSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoverStamp("${TermCategory.entries.size} розділів")
                CoverStamp(termsCount(GlossaryTerms.size))
            }
            }
        }
        Ribbon(Modifier.align(Alignment.TopEnd).padding(end = 22.dp))
    }
}

@Composable
private fun CoverStamp(text: String) {
    val b = book
    Text(
        text,
        style = ZnakyType.caption,
        color = b.cover,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(b.coverTint).padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

/** A bookmark ribbon with a notched tail. */
@Composable
private fun Ribbon(modifier: Modifier = Modifier) {
    val color = book.ribbon
    Canvas(modifier.size(width = 20.dp, height = 46.dp)) {
        val p = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width / 2, size.height - size.width * 0.45f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(p, color)
    }
}

@Composable
private fun BookSearch(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val b = book
    val hint = "Знайти термін: обгін, кліренс, транець…"
    Row(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(b.page)
            .border(1.dp, b.rule, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = b.inkTertiary, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, style = ZnakyType.body, color = b.inkTertiary, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = ZnakyType.body.copy(color = b.ink),
                cursorBrush = SolidColor(b.cover),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Пошук у словнику" },
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Очистити пошук",
                tint = b.inkTertiary,
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).clickable { onChange("") },
            )
        }
    }
}

@Composable
private fun SmallCapsHeader(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = BookType.smallCaps, color = book.inkTertiary, modifier = modifier.semantics { heading() })
}

@Composable
private fun Contents(onOpen: (TermCategory) -> Unit) {
    val b = book
    Page {
        TermCategory.entries.forEachIndexed { i, ch ->
            if (i > 0) Rule()
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(ch) }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ch.number.toString(),
                    style = BookType.dropLetter.copy(fontSize = BookType.headword.fontSize * 1.4f),
                    color = b.cover,
                    modifier = Modifier.width(34.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(ch.title, style = BookType.contentsTitle, color = b.ink)
                    Text(ch.intro, style = ZnakyType.subhead, color = b.inkSecondary, maxLines = 2, modifier = Modifier.padding(top = 2.dp))
                }
                DottedLeader(Modifier.width(18.dp).padding(horizontal = 4.dp))
                Text(
                    GlossaryTerms.count { it.category == ch }.toString(),
                    style = BookType.contentsTitle,
                    color = b.inkSecondary,
                )
            }
        }
    }
}

@Composable
private fun DottedLeader(modifier: Modifier = Modifier) {
    val color = book.inkTertiary
    Canvas(modifier.height(2.dp)) {
        drawLine(
            color,
            Offset(0f, size.height / 2),
            Offset(size.width, size.height / 2),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 6f)),
        )
    }
}

@Composable
private fun BookFootnote() {
    Text(
        "Визначення з п. 1.10 Правил дорожнього руху переказані простими словами і звірені з чинною редакцією. " +
            "Офіційне формулювання дивіться в тексті ПДР України.",
        style = ZnakyType.footnote.copy(fontStyle = FontStyle.Italic),
        color = book.inkTertiary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).navigationBarsPadding(),
    )
}

private fun LazyListScope.searchResults(results: List<GlossaryTerm>, query: String) {
    results.forEach { t ->
        item(key = "S-${t.category}-${t.term}") {
            Page(Modifier.padding(vertical = 4.dp)) {
                Text(
                    "Розділ ${t.category.number} · ${t.category.title}".uppercase(),
                    style = BookType.smallCaps,
                    color = book.cover,
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp),
                )
                Entry(t, query)
            }
        }
    }
}

// ---------------------------------------------------------------- chapter

@Composable
private fun ChapterPage(chapter: TermCategory, onBack: () -> Unit, onNext: (TermCategory) -> Unit) {
    val b = book
    val terms = remember(chapter) { filterGlossary(GlossaryTerms, chapter, "") }
    val groups = remember(terms) { terms.groupBy { it.term.first().uppercaseChar() } }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // Index of each letter header in the list: top bar, header, index strip, then per letter a header + its entries.
    val letterIndex = remember(groups) {
        var i = 3
        groups.mapValues { (_, list) -> i.also { i += 1 + list.size } }
    }
    val next = TermCategory.entries.getOrNull(chapter.ordinal + 1)

    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), state = listState) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Зміст") }
        item(key = "head") { ChapterHeader(chapter, terms.size) }
        item(key = "index") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                groups.keys.forEach { letter ->
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(b.page)
                            .border(1.dp, b.rule, RoundedCornerShape(10.dp))
                            .clickable { scope.launch { listState.animateScrollToItem(letterIndex.getValue(letter)) } }
                            .semantics { contentDescription = "Літера $letter" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(letter.toString(), style = BookType.contentsTitle, color = b.cover)
                    }
                }
            }
        }
        groups.forEach { (letter, list) ->
            item(key = "L-$letter") {
                Row(
                    Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(letter.toString(), style = BookType.dropLetter, color = b.cover, modifier = Modifier.semantics { heading() })
                    Box(Modifier.padding(start = 12.dp).weight(1f).height(1.dp).background(b.rule))
                }
            }
            list.forEachIndexed { i, t ->
                item(key = "T-${t.term}") {
                    val shape = when {
                        list.size == 1 -> RoundedCornerShape(18.dp)
                        i == 0 -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                        i == list.lastIndex -> RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                        else -> RoundedCornerShape(0.dp)
                    }
                    Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(shape).background(b.page)) {
                        if (i > 0) Rule()
                        Entry(t, "")
                    }
                }
            }
        }
        item(key = "next") {
            if (next != null) {
                Page(Modifier.padding(top = 24.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clickable { onNext(next) }.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("НАСТУПНИЙ РОЗДІЛ ${next.number}", style = BookType.smallCaps, color = b.inkTertiary)
                            Text(next.title, style = BookType.contentsTitle, color = b.ink, modifier = Modifier.padding(top = 4.dp))
                        }
                        Text("→", style = BookType.chapterTitle, color = b.cover)
                    }
                }
            }
        }
        item(key = "note") { BookFootnote() }
    }
}

@Composable
private fun ChapterHeader(chapter: TermCategory, count: Int) {
    val b = book
    Column(Modifier.padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("РОЗДІЛ ${chapter.number}", style = BookType.smallCaps, color = b.cover, modifier = Modifier.weight(1f))
            Text(termsCount(count), style = ZnakyType.caption, color = b.inkTertiary)
        }
        Text(
            chapter.title,
            style = BookType.chapterTitle,
            color = b.ink,
            modifier = Modifier.padding(top = 6.dp).semantics { heading() },
        )
        Text(chapter.intro, style = BookType.intro.copy(fontStyle = FontStyle.Italic), color = b.inkSecondary, modifier = Modifier.padding(top = 6.dp))
        // Double rule under the title, like a printed chapter opening.
        Box(Modifier.padding(top = 14.dp).fillMaxWidth().height(2.dp).background(b.cover.copy(alpha = 0.55f)))
        Box(Modifier.padding(top = 3.dp).fillMaxWidth().height(1.dp).background(b.cover.copy(alpha = 0.25f)))
    }
}

// ---------------------------------------------------------------- shared pieces

@Composable
private fun Page(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val b = book
    Column(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(b.page)
            .border(1.dp, b.rule, RoundedCornerShape(18.dp)),
    ) { content() }
}

@Composable
private fun Rule() {
    Box(Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(1.dp).background(book.rule))
}

@Composable
private fun Entry(t: GlossaryTerm, query: String) {
    val b = book
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
        Text(highlight(t.term, query, b.highlight), style = BookType.headword, color = b.ink)
        if (t.synonyms != null) {
            Text("також: ${t.synonyms}", style = ZnakyType.subhead.copy(fontStyle = FontStyle.Italic), color = b.inkSecondary)
        }
        Text(highlight(t.text, query, b.highlight), style = ZnakyType.callout, color = b.ink, modifier = Modifier.padding(top = 6.dp))
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "§ ${t.source}",
                style = ZnakyType.caption,
                color = b.cover,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(b.coverTint).padding(horizontal = 7.dp, vertical = 3.dp),
            )
            if (t.see != null) {
                Text("див. ${t.see}", style = ZnakyType.subhead, color = b.inkTertiary, maxLines = 2)
            }
        }
    }
}

private fun highlight(text: String, query: String, bg: androidx.compose.ui.graphics.Color): AnnotatedString {
    val q = query.trim()
    if (q.isEmpty()) return AnnotatedString(text)
    val start = text.lowercase(Locale.ROOT).indexOf(q.lowercase(Locale.ROOT))
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(background = bg)) { append(text.substring(start, start + q.length)) }
        append(text.substring(start + q.length))
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
