package com.znaky.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.ExamResult
import kotlinx.serialization.Serializable

@Serializable
data object StatsRoute

fun NavController.navigateToStats(navOptions: NavOptions? = null) = navigate(StatsRoute, navOptions)

fun NavGraphBuilder.statsScreen(onTrainTopic: (String) -> Unit) {
    composable<StatsRoute> { StatsScreen(onTrainTopic) }
}

@Composable
internal fun StatsScreen(onTrainTopic: (String) -> Unit, viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "title") {
            LargeTitle("Прогрес", Modifier.padding(top = 16.dp), subtitle = "Наскільки ти готовий до іспиту")
        }
        item(key = "readiness") { ReadinessCard(s) }
        item(key = "week") { WeekCard(s) }
        // What to do next lives on the home screen (the book); here only the topics that need work.
        item(key = "topicsTitle") {
            Text(
                "ВАРТО ПІДТЯГНУТИ",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 22.dp).semantics { heading() },
            )
        }
        item(key = "topics") {
            if (s.weakTopics.isEmpty()) {
                Text(
                    "Слабких тем поки немає. Відповідай на питання розділів, і тут з'являться теми, де найбільше помилок.",
                    style = ZnakyType.subhead,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            } else {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(c.surface)) {
                    s.weakTopics.forEachIndexed { i, t ->
                        if (i > 0) HorizontalDivider(Modifier.padding(start = 48.dp), thickness = 1.dp, color = c.separator)
                        TopicLevelRow(TopicRow(t.id, s.topics.firstOrNull { it.id == t.id }?.number.orEmpty(), t.title, t.completion), onClick = { onTrainTopic(t.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun levelOf(completion: Float): Pair<String, Color> {
    val c = ZnakyTheme.colors
    return when {
        completion < 0.7f -> "СЛАБКО" to c.error
        completion < 0.85f -> "СЕРЕДНЬО" to c.orange
        completion < 0.95f -> "ДОБРЕ" to c.success
        else -> "ВИВЧЕНО" to c.success
    }
}

@Composable
private fun TopicLevelRow(t: TopicRow, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    val (label, color) = levelOf(t.completion)
    val bar = when (label) {
        "СЛАБКО" -> c.error
        "СЕРЕДНЬО" -> c.accent
        else -> c.success
    }
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(t.number, style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.width(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(t.title, style = ZnakyType.bodyStrong, color = c.textPrimary, maxLines = 1)
            LinearBar(t.completion, Modifier.fillMaxWidth(), height = 5.dp, color = bar)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${(t.completion * 100).toInt()}%", style = ZnakyType.bodyStrong, color = c.textPrimary)
            Text(label, style = ZnakyType.caption.copy(fontSize = 10.sp), color = color)
        }
    }
}

@Composable
private fun ReadinessCard(s: StatsUiState) {
    val c = ZnakyTheme.colors
    val left = (3 - s.passedInARow).coerceAtLeast(0)
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(s.readiness.score / 100f, size = 84.dp, stroke = 10.dp, color = c.success) {
                Text("${s.readiness.score}%", style = ZnakyType.title2, color = c.textPrimary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Готовність до ТСЦ", style = ZnakyType.headline, color = c.textPrimary)
                Text(
                    when {
                        s.lastExamsCount == 0 -> "Пройдіть пробний іспит: 3 поспіль без >2 помилок — і ти готовий."
                        left == 0 -> "Пробні: ${s.passedOfLast5} з останніх ${s.lastExamsCount} складено. Ти готовий до ТСЦ!"
                        else -> "Пробні: ${s.passedOfLast5} з останніх ${s.lastExamsCount} складено. Ще $left поспіль без >2 помилок — і ти готовий."
                    },
                    style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal),
                    color = c.textSecondary,
                )
            }
        }
        ExamsChart(s.recentExams)
    }
}

@Composable
private fun WeekCard(s: StatsUiState) {
    val c = ZnakyTheme.colors
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Цей тиждень", style = ZnakyType.headline, color = c.textPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = c.accent, modifier = Modifier.size(18.dp))
            Text("${s.streak} дн. поспіль", style = ZnakyType.footnote, color = c.textSecondary, modifier = Modifier.padding(start = 4.dp))
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            s.week.forEach { d ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (d.active) c.success else c.surfaceMuted)
                            .then(if (d.isToday) Modifier.border(2.dp, c.link, CircleShape) else Modifier)
                            .semantics { contentDescription = d.label + if (d.active) ", займалися" else "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (d.active) Icon(Icons.Rounded.Check, contentDescription = null, tint = c.surface, modifier = Modifier.size(18.dp))
                    }
                    Text(d.label, style = ZnakyType.caption, color = c.textTertiary)
                }
            }
        }
    }
}

@Composable
private fun ExamsChart(exams: List<ExamResult>) {
    val c = ZnakyTheme.colors
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("ПРОБНІ ІСПИТИ", style = ZnakyType.caption, color = c.textTertiary, modifier = Modifier.weight(1f))
        Text("прохідний 18/20", style = ZnakyType.caption, color = c.textTertiary)
    }
    if (exams.isEmpty()) {
        Text("Ви ще не проходили пробний іспит.", style = ZnakyType.subhead, color = c.textSecondary, modifier = Modifier.padding(top = 8.dp))
        return
    }
    val success = c.success
    val idle = c.surfaceMuted
    val line = c.textTertiary
    val labelColor = c.textSecondary
    val measurer = rememberTextMeasurer()
    val labelStyle = ZnakyType.caption
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(top = 8.dp)
            .semantics { contentDescription = "Результати пробних іспитів: " + exams.joinToString { "${it.correctAnswers} з ${it.totalQuestions}" } },
    ) {
        val slots = 8
        val gap = 10.dp.toPx()
        val top = 16.dp.toPx()
        val chartH = size.height - top
        val barW = (size.width - gap * (slots - 1)) / slots
        // Bars show 10..20 correct answers so the differences near the threshold are visible.
        fun yOf(score: Float) = top + chartH * (1f - ((score - 10f) / 10f).coerceIn(0.04f, 1f))
        exams.forEachIndexed { i, e ->
            val score = e.correctAnswers * 20f / e.totalQuestions.coerceAtLeast(1)
            val y = yOf(score)
            val x = i * (barW + gap)
            drawRoundRect(
                color = if (e.passed) success else idle,
                topLeft = Offset(x, y),
                size = Size(barW, size.height - y),
                cornerRadius = CornerRadius(6.dp.toPx()),
            )
            val text = measurer.measure("${e.correctAnswers}", labelStyle)
            drawText(text, color = labelColor, topLeft = Offset(x + (barW - text.size.width) / 2, y - text.size.height - 1.dp.toPx()))
        }
        val y = yOf(18f)
        drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
    }
}
