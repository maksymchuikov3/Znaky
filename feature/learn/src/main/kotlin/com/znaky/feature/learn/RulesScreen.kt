package com.znaky.feature.learn

import androidx.compose.foundation.background
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.SignColors
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class RulesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    questions: QuestionRepository,
) : ViewModel() {
    private val appContext = context
    private val books = mutableMapOf<String, StateFlow<PdrBook?>>()

    fun book(key: String): StateFlow<PdrBook?> = books.getOrPut(key) {
        flow { emit(PdrText.load(appContext, key)) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    /** Question topics by ПДР section number, for the «питання з розділу» button. */
    val topics: StateFlow<Map<String, Topic>> = questions.observeTopics()
        .map { list -> list.filter { it.block == ExamBlock.TRAFFIC_RULES }.associateBy { it.number } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

@Composable
internal fun RulesScreen(
    bookKey: String,
    moto: Boolean,
    onBack: () -> Unit,
    onOpen: (Int) -> Unit,
    viewModel: RulesViewModel = hiltViewModel(),
) {
    val book by viewModel.book(bookKey).collectAsStateWithLifecycle()
    val pdr = book ?: return LoadingBox()
    val motoPoints = if (moto) pdr.sections.flatMap { s -> s.points.filter { it.moto }.map { s to it } } else emptyList()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "title") {
            if (moto) {
                LargeTitle("ПДР для мотоцикліста", subtitle = "Ті самі правила, що й для авто. Пункти, де окремо сказано про мотоцикли й мопеди, позначені «Мото».")
            } else {
                LargeTitle(pdr.title, subtitle = pdr.subtitle)
            }
        }
        if (motoPoints.isNotEmpty()) item(key = "moto") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Окремо про мотоцикли й мопеди · ${motoPoints.size}") {
                motoPoints.forEachIndexed { i, (s, p) ->
                    ListRow(
                        title = if (s.n == 33) "Знак ${p.label}" else "Пункт ${p.label}",
                        subtitle = p.blocks.firstOrNull()?.text?.take(90)?.let { if (it.length == 90) "$it…" else it },
                        showDivider = i > 0,
                        onClick = { onOpen(s.n) },
                        leading = { MotoBadge() },
                    )
                }
            }
        }
        item(key = "list") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Розділи ${pdr.sectionNumber(1)}–${pdr.sectionNumber(pdr.sections.size)}") {
                pdr.sections.forEachIndexed { i, s ->
                    ListRow(
                        title = s.title,
                        subtitle = buildString {
                            append("${pointsLabel(s)} · ${s.readMinutes} хв читання")
                            if (moto) s.points.count { it.moto }.takeIf { it > 0 }?.let { append(" · $it про мото") }
                        },
                        showDivider = i > 0,
                        onClick = { onOpen(s.n) },
                        leading = { NumberBadge(pdr.sectionNumber(s.n)) },
                    )
                }
            }
        }
        item(key = "source") {
            Text(
                "${pdr.source}, ${pdr.edition}.",
                style = ZnakyType.caption,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
internal fun RuleSectionScreen(
    section: Int,
    bookKey: String,
    moto: Boolean,
    onBack: () -> Unit,
    onTrain: (String) -> Unit,
    onOpenSign: (group: String, code: String) -> Unit,
    onOpenMarkings: () -> Unit,
    viewModel: RulesViewModel = hiltViewModel(),
) {
    val book by viewModel.book(bookKey).collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val s = book?.sections?.firstOrNull { it.n == section } ?: return LoadingBox()
    // ПДР sections map to question topics by number; other books have no question bank of their own here.
    val topic = if (bookKey == PdrText.PDR) topics[s.n.toString()] else null
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Розділ ${book?.sectionNumber(s.n)}") }
        item(key = "title") { LargeTitle(s.title, subtitle = "${pointsLabel(s)} · ${s.readMinutes} хв читання") }
        if (s.n == 12 && bookKey == PdrText.PDR) item(key = "speed") { SpeedLimits() }
        if (s.intro.isNotEmpty()) item(key = "intro") { Blocks(s.intro, Modifier.padding(horizontal = 20.dp)) }
        s.points.forEachIndexed { i, p ->
            if (p.heading != null) {
                item(key = "h$i") {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(p.heading, style = ZnakyType.title2, color = c.textPrimary)
                        if (p.headingIntro.isNotEmpty()) Blocks(p.headingIntro)
                    }
                }
            }
            item(key = "p$i") {
                when (if (bookKey == PdrText.PDR) s.n else 0) {
                    33 -> {
                        val signs = p.linkedSigns()
                        PointCard(p, signs = signs, moto = moto, onClick = signs.firstOrNull()?.let { first -> { onOpenSign(first.group, first.code) } })
                    }
                    34 -> PointCard(p, moto = moto, onClick = if (p.note) null else onOpenMarkings)
                    else -> PointCard(p, moto = moto)
                }
            }
        }
        if (s.footnote.isNotEmpty()) item(key = "foot") { Blocks(s.footnote, Modifier.padding(horizontal = 20.dp)) }
        if (topic != null && topic.questionCount > 0) {
            item(key = "train") {
                ZButton(
                    "${topic.questionCount} питань із розділу",
                    onClick = { onTrain(topic.id) },
                    icon = Icons.Rounded.Quiz,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun pointsLabel(s: PdrSection): String {
    val n = s.pointCount
    if (n == 0) return "Без пунктів, лише пояснення"
    val word = when {
        s.n == 33 -> "знаків і табличок"
        s.n == 34 -> "ліній розмітки"
        n % 10 == 1 && n % 100 != 11 -> "пункт"
        n % 10 in 2..4 && n % 100 !in 12..14 -> "пункти"
        else -> "пунктів"
    }
    return "$n $word"
}

@Composable
private fun MotoBadge() {
    val c = ZnakyTheme.colors
    Text(
        "Мото",
        style = ZnakyType.caption,
        color = c.onAccentTint,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.accentTint).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun NumberBadge(text: String) {
    val c = ZnakyTheme.colors
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(c.tealTint), contentAlignment = Alignment.Center) {
        Text(text, style = ZnakyType.footnote, color = c.teal)
    }
}

@Composable
private fun PointCard(p: PdrPoint, signs: List<RoadSignInfo> = emptyList(), moto: Boolean = false, onClick: (() -> Unit)? = null) {
    val c = ZnakyTheme.colors
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), radius = 18.dp, onClick = onClick) {
        if (signs.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                signs.take(4).forEach { RoadSign(it.code, "${it.code} ${it.name}", Modifier.size(56.dp)) }
                if (signs.size > 4) {
                    Text("+${signs.size - 4}", style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
        if (!p.note) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    p.label,
                    style = ZnakyType.caption,
                    color = if (p.excluded) c.textTertiary else c.teal,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.tealTint).padding(horizontal = 8.dp, vertical = 3.dp),
                )
                if (moto && p.moto) MotoBadge()
            }
            Spacer(Modifier.height(8.dp))
        }
        Blocks(p.blocks, dim = p.excluded)
    }
}

@Composable
private fun Blocks(blocks: List<PdrBlock>, modifier: Modifier = Modifier, dim: Boolean = false) {
    val c = ZnakyTheme.colors
    val text = if (dim) c.textTertiary else c.textPrimary
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { b ->
            when (b.t) {
                "li" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(b.mark.orEmpty(), style = ZnakyType.bodyStrong, color = c.teal, modifier = Modifier.width(28.dp))
                    Text(b.text, style = ZnakyType.body, color = text, modifier = Modifier.weight(1f))
                }
                "note" -> Text(
                    b.text,
                    style = ZnakyType.subhead,
                    color = c.textSecondary,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.sandTint).padding(12.dp),
                )
                "table" -> PdrTable(b.rows)
                else -> Text(b.text, style = ZnakyType.body, color = text)
            }
        }
    }
}

@Composable
private fun PdrTable(rows: List<List<String>>) {
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.surfaceMuted)) {
        rows.forEachIndexed { i, row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { j, cell ->
                    Text(
                        cell,
                        style = if (i == 0) ZnakyType.footnote else ZnakyType.subhead,
                        color = if (j == 0) c.textPrimary else c.textSecondary,
                        textAlign = if (j == 0) TextAlign.Start else TextAlign.End,
                        modifier = Modifier.weight(if (j == 0) 2.2f else 1f),
                    )
                }
            }
            if (i < rows.lastIndex) HorizontalDivider(color = c.separator)
        }
    }
}

@Composable
private fun SpeedLimits() {
    val c = ZnakyTheme.colors
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Легковий автомобіль, км/год", style = ZnakyType.footnote, color = c.textTertiary)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Limit("50", "населений пункт")
            Limit("90", "поза містом")
            Limit("110", "дорога з розділювальною смугою")
            Limit("130", "автомагістраль")
        }
    }
}

@Composable
private fun Limit(value: String, label: String) {
    val c = ZnakyTheme.colors
    Column(Modifier.size(width = 76.dp, height = 120.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(SignColors.Red)
                .padding(6.dp)
                .clip(CircleShape)
                .background(Color.White)
                .semantics { contentDescription = "$value км/год, $label" },
            contentAlignment = Alignment.Center,
        ) {
            Text(value, style = ZnakyType.headline, color = SignColors.Black)
        }
        Text(label, style = ZnakyType.caption.copy(fontWeight = ZnakyType.subhead.fontWeight), color = c.textSecondary, textAlign = TextAlign.Center)
    }
}
