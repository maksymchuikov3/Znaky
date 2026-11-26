package com.znaky.feature.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.znaky.core.art.RoadSigns
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.ReviewCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
data class RepetitionState(
    val dueToday: Int,
    /** Of [dueToday], how many are fresh mistakes (box 1). */
    val dueMistakes: Int,
    /** Questions per box 1..6. */
    val boxes: List<Int>,
    val mistakes: Int,
    val bookmarks: Int,
    /** «16 Перехрестя 54% · 14 Обгін 68%». */
    val weakTopics: String?,
)

@HiltViewModel
class RepetitionViewModel @Inject constructor(progress: ProgressRepository, questions: QuestionRepository) : ViewModel() {
    private val weak = combine(progress.observeTopicProgress(), questions.observeTopics()) { tp, topics ->
        val byId = topics.associateBy { it.id }
        tp.filter { it.answeredQuestions > 0 && it.completion < 0.9f }
            .sortedBy { it.completion }
            .take(2)
            .mapNotNull { p -> byId[p.topicId]?.let { "${it.number} ${it.title} ${(p.completion * 100).toInt()}%" } }
            .joinToString(" · ")
            .ifEmpty { null }
    }

    val state: StateFlow<RepetitionState?> = combine(
        progress.observeReviewCards(),
        progress.observeDueForReview(),
        progress.observeMistakes(),
        progress.observeBookmarks(),
        weak,
    ) { cards, due, mistakes, bookmarks, weakText ->
        val dueSet = due.toSet()
        RepetitionState(
            dueToday = due.size,
            dueMistakes = cards.count { it.questionId in dueSet && it.box == ReviewCard.MIN_BOX },
            boxes = (ReviewCard.MIN_BOX..ReviewCard.MAX_BOX).map { b -> cards.count { it.box == b } },
            mistakes = mistakes.size,
            bookmarks = bookmarks.size,
            weakTopics = weakText,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

internal fun questionsWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "питань"
        mod10 == 1 -> "питання"
        mod10 in 2..4 -> "питання"
        else -> "питань"
    }
}

@Composable
internal fun RepetitionScreen(
    onBack: () -> Unit,
    onStart: () -> Unit,
    onMistakes: () -> Unit,
    onBookmarks: () -> Unit,
    onWeakTopics: () -> Unit,
    viewModel: RepetitionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "back") { TopBar(onBack = onBack, backLabel = "Тренування") }
        item(key = "title") { LargeTitle("Повторення", subtitle = "Помилки повертаються, доки не запам’ятаєш") }
        item(key = "today") { TodayCard(s, onStart) }
        item(key = "trackTitle") {
            Text(
                "ЯК ПИТАННЯ «ДОЇЖДЖАЄ» ДО ВИВЧЕНОГО",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 22.dp),
            )
        }
        item(key = "track") { TrackCard(s.boxes) }
        item(key = "lists") {
            GroupedList(Modifier.padding(horizontal = 16.dp)) {
                ListRow(
                    title = "Мої помилки",
                    onClick = if (s.mistakes > 0) onMistakes else null,
                    leading = { Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = c.red) },
                    trailing = { CountBadge(s.mistakes, c.redTint, c.red) },
                )
                ListRow(
                    title = "Збережені питання",
                    showDivider = true,
                    onClick = onBookmarks,
                    leading = { Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = c.accent) },
                    trailing = { CountBadge(s.bookmarks, c.accentTint, c.onAccentTint) },
                )
                ListRow(
                    title = "Слабкі розділи",
                    subtitle = s.weakTopics ?: "Поки немає: пройдіть кілька тем",
                    showDivider = true,
                    onClick = onWeakTopics,
                    leading = { Icon(Icons.AutoMirrored.Rounded.TrendingDown, contentDescription = null, tint = c.link) },
                )
            }
        }
    }
}

@Composable
internal fun CountBadge(count: Int, bg: Color, fg: Color) {
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text("$count", style = ZnakyType.footnote, color = fg)
    }
}

@Composable
private fun TodayCard(s: RepetitionState, onStart: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardStack()
            Column(Modifier.weight(1f)) {
                Text("СЬОГОДНІ", style = ZnakyType.caption, color = c.red)
                Text("${s.dueToday} ${questionsWord(s.dueToday)}", style = ZnakyType.title, color = c.textPrimary)
                val minutes = (s.dueToday + 1) / 2
                Text(
                    if (s.dueToday == 0) "Усе повторено. Нові з’являться завтра"
                    else "${s.dueMistakes} помилок + ${s.dueToday - s.dueMistakes} на повтор · ~$minutes хв",
                    style = ZnakyType.subhead,
                    color = c.textSecondary,
                )
            }
        }
        ZButton("Повторити", onClick = onStart, icon = Icons.Rounded.PlayArrow, enabled = s.dueToday > 0, modifier = Modifier.fillMaxWidth())
    }
}

/** Three tilted cards with a sign, like a deck of flashcards. */
@Composable
private fun CardStack() {
    val c = ZnakyTheme.colors
    Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
        listOf(-10f, -4f).forEach { angle ->
            Box(Modifier.size(64.dp, 72.dp).rotate(angle).shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(c.surface))
        }
        Box(
            Modifier.size(64.dp, 72.dp).offset(x = 4.dp).shadow(4.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(c.surface),
            contentAlignment = Alignment.Center,
        ) {
            ArtImage(null, Modifier.size(46.dp)) { RoadSigns.draw(this, "2.1") }
        }
    }
}

@Composable
private fun TrackCard(boxes: List<Int>) {
    val c = ZnakyTheme.colors
    val dots = listOf(c.red, c.accent, c.accent, c.primary, c.primary, c.success)
    val labels = listOf("Сьогодні", "1 день", "3 дні", "7 днів", "14 днів", "Вивчено")
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(22.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val line = c.surfaceMuted
        Box(Modifier.fillMaxWidth().height(18.dp)) {
            Canvas(Modifier.fillMaxWidth().height(18.dp)) {
                val step = size.width / 6f
                drawLine(line, Offset(step / 2, size.height / 2), Offset(size.width - step / 2, size.height / 2), strokeWidth = 6.dp.toPx())
                drawLine(
                    Color.White.copy(alpha = 0.7f),
                    Offset(step / 2, size.height / 2),
                    Offset(size.width - step / 2, size.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
                dots.forEachIndexed { i, color ->
                    drawCircle(color, radius = 8.dp.toPx(), center = Offset(step * i + step / 2, size.height / 2))
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${boxes.getOrElse(i) { 0 }}", style = ZnakyType.headline, color = c.textPrimary)
                    Text(label, style = ZnakyType.caption, color = c.textTertiary, maxLines = 1, textAlign = TextAlign.Center)
                }
            }
        }
        Text(
            "Правильна відповідь пересуває питання далі: через 1, 3, 7 і 14 днів. Помилка повертає його на старт.",
            style = ZnakyType.footnote.copy(fontWeight = ZnakyType.subhead.fontWeight),
            color = c.textSecondary,
        )
    }
}
