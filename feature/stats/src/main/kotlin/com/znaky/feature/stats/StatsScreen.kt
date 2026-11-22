package com.znaky.feature.stats

import androidx.compose.foundation.Canvas
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
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.Pill
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
        item(key = "title") { LargeTitle("Прогрес", Modifier.padding(top = 16.dp)) }
        item(key = "readiness") {
            ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProgressRing(s.readiness.score / 100f, size = 84.dp, stroke = 10.dp) {
                        Text("${s.readiness.score}%", style = ZnakyType.title2, color = c.textPrimary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Готовність", style = ZnakyType.headline, color = c.textPrimary)
                        Text("Вивчено питань", style = ZnakyType.footnote, color = c.textTertiary)
                        LinearBar(s.readiness.topicCoverage, color = c.success)
                        Text("Пробних іспитів: ${s.readiness.examsTaken}", style = ZnakyType.footnote, color = c.textTertiary)
                    }
                }
            }
        }
        item(key = "week") { WeekCard(s) }
        item(key = "exams") { ExamsChart(s.recentExams) }
        if (s.weakTopics.isNotEmpty()) {
            item(key = "weak") {
                GroupedList(Modifier.padding(horizontal = 16.dp), header = "Варто підтягнути") {
                    s.weakTopics.forEachIndexed { i, t ->
                        ListRow(
                            title = t.title,
                            subtitle = "Вивчено ${(t.completion * 100).toInt()}%",
                            showDivider = i > 0,
                            showChevron = false,
                            onClick = { onTrainTopic(t.id) },
                            trailing = { Pill("Тренувати") },
                        )
                    }
                }
            }
        }
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
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Пробні іспити", style = ZnakyType.headline, color = c.textPrimary)
        if (exams.isEmpty()) {
            Text("Ви ще не проходили пробний іспит.", style = ZnakyType.subhead, color = c.textSecondary, modifier = Modifier.padding(top = 8.dp))
            return@ZCard
        }
        Text("Прохідний поріг: 18 з 20", style = ZnakyType.footnote, color = c.textTertiary)
        val success = c.success
        val error = c.error
        val line = c.textTertiary
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 12.dp)
                .semantics { contentDescription = "Результати: " + exams.joinToString { "${it.correctAnswers} з ${it.totalQuestions}" } },
        ) {
            val slots = 8
            val gap = 10.dp.toPx()
            val barW = (size.width - gap * (slots - 1)) / slots
            exams.forEachIndexed { i, e ->
                val ratio = e.correctAnswers.toFloat() / e.totalQuestions.coerceAtLeast(1)
                val h = size.height * ratio.coerceAtLeast(0.04f)
                drawRoundRect(
                    color = if (e.passed) success else error,
                    topLeft = Offset(i * (barW + gap), size.height - h),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                )
            }
            val y = size.height * (1 - 18f / 20f)
            drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        }
    }
}
