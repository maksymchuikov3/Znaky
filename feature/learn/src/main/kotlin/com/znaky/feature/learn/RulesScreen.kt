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
    val book: StateFlow<PdrBook?> = flow { emit(PdrText.load(context)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Question topics by ПДР section number, for the «питання з розділу» button. */
    val topics: StateFlow<Map<String, Topic>> = questions.observeTopics()
        .map { list -> list.filter { it.block == ExamBlock.TRAFFIC_RULES }.associateBy { it.number } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

@Composable
internal fun RulesScreen(onBack: () -> Unit, onOpen: (Int) -> Unit, viewModel: RulesViewModel = hiltViewModel()) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val pdr = book ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "title") { LargeTitle("Правила дорожнього руху", subtitle = "Повний текст ПДР по розділах і пунктах") }
        item(key = "list") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Розділи 1–${pdr.sections.size}") {
                pdr.sections.forEachIndexed { i, s ->
                    ListRow(
                        title = s.title,
                        subtitle = "${pointsLabel(s)} · ${s.readMinutes} хв читання",
                        showDivider = i > 0,
                        onClick = { onOpen(s.n) },
                        leading = { NumberBadge(s.n.toString()) },
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
internal fun RuleSectionScreen(section: Int, onBack: () -> Unit, onTrain: (String) -> Unit, viewModel: RulesViewModel = hiltViewModel()) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val s = book?.sections?.firstOrNull { it.n == section } ?: return LoadingBox()
    val topic = topics[s.n.toString()]
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Розділ ${s.n}") }
        item(key = "title") { LargeTitle(s.title, subtitle = "${pointsLabel(s)} · ${s.readMinutes} хв читання") }
        if (s.n == 12) item(key = "speed") { SpeedLimits() }
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
            item(key = "p$i") { PointCard(p) }
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
private fun NumberBadge(text: String) {
    val c = ZnakyTheme.colors
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(c.tealTint), contentAlignment = Alignment.Center) {
        Text(text, style = ZnakyType.footnote, color = c.teal)
    }
}

@Composable
private fun PointCard(p: PdrPoint) {
    val c = ZnakyTheme.colors
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), radius = 18.dp) {
        if (!p.note) {
            Text(
                p.label,
                style = ZnakyType.caption,
                color = if (p.excluded) c.textTertiary else c.teal,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.tealTint).padding(horizontal = 8.dp, vertical = 3.dp),
            )
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
