package com.znaky.feature.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.AnswerOption
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.QuestionImage
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.answerState
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.Question
import com.znaky.core.model.ReviewCard

@Composable
internal fun PracticeScreen(onBack: () -> Unit, viewModel: PracticeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val c = ZnakyTheme.colors
    val total = state.questions.size
    val question = state.current
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        Header(
            title = state.title,
            subtitle = if (total > 0 && !state.isFinished) "питання ${state.index + 1} з $total · без таймера" else null,
            bookmarked = question != null && question.id in bookmarks,
            showBookmark = question != null && !state.isFinished,
            onBack = onBack,
            onBookmark = viewModel::toggleBookmark,
        )
        if (total > 0 && !state.isFinished) SegmentedProgress(state.results, state.index, Modifier.padding(horizontal = 16.dp))
        when {
            state.isLoading -> LoadingBox()
            total == 0 -> Message(emptyText(state.mode))
            state.isFinished -> Finished(state.correctCount, total, state.mode, onBack)
            question != null -> QuestionPage(state, question, question.id in bookmarks, viewModel::answer, viewModel::next, Modifier.weight(1f))
        }
    }
}

private fun emptyText(mode: String) = when (mode) {
    PracticeMode.REVIEW -> "На сьогодні все повторено. Нові питання з’являться завтра."
    PracticeMode.MISTAKES -> "Помилок немає. Так тримати!"
    PracticeMode.BOOKMARKS -> "Закладок поки немає. Збережіть питання значком закладки вгорі."
    else -> "Тут поки немає питань. Поверніться пізніше!"
}

@Composable
private fun Header(
    title: String,
    subtitle: String?,
    bookmarked: Boolean,
    showBookmark: Boolean,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
) {
    val c = ZnakyTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = "Назад", tint = c.link)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = ZnakyType.headline, color = c.textPrimary, maxLines = 1)
            if (subtitle != null) Text(subtitle, style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal), color = c.textTertiary)
        }
        if (showBookmark) {
            IconButton(onClick = onBookmark) {
                Icon(
                    if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = if (bookmarked) "Прибрати із закладок" else "Додати в закладки",
                    tint = if (bookmarked) c.accent else c.textTertiary,
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

/** One segment per question: green right, red wrong, blue current. Long sessions use one bar. */
@Composable
private fun SegmentedProgress(results: List<Boolean?>, index: Int, modifier: Modifier) {
    val c = ZnakyTheme.colors
    if (results.size > 30) {
        LinearBar((index + 1f) / results.size, modifier.fillMaxWidth(), height = 5.dp)
        return
    }
    Row(modifier.fillMaxWidth().height(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        results.forEachIndexed { i, r ->
            val color = when {
                r == true -> c.success
                r == false -> c.error
                i == index -> c.primary
                else -> c.surfaceMuted
            }
            Box(Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun QuestionPage(
    state: PracticeUiState,
    question: Question,
    bookmarked: Boolean,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier,
) {
    val c = ZnakyTheme.colors
    val scroll = rememberScrollState()
    LaunchedEffect(state.index) { scroll.scrollTo(0) }
    LaunchedEffect(state.revealed) { if (state.revealed) scroll.animateScrollTo(scroll.maxValue) }
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val mistakeAt = state.lastMistakes[question.id]
                if (state.mode == PracticeMode.REVIEW && mistakeAt != null) {
                    Pill(mistakeAgo(mistakeAt), background = c.redTint, contentColor = c.red, icon = Icons.Rounded.WarningAmber)
                } else {
                    state.topicLabels[question.topicId]?.let {
                        Pill(it, Modifier.weight(1f, fill = false), background = c.surfaceMuted, contentColor = c.textSecondary)
                    }
                }
                if (bookmarked) Pill("Збережено", background = c.accentTint, contentColor = c.onAccentTint, icon = Icons.Rounded.Bookmark)
            }
            question.imageAsset?.let { QuestionImage(it) }
            Text(question.text, style = ZnakyType.headline.copy(lineHeight = ZnakyType.body.lineHeight), color = c.textPrimary)
            question.answers.forEachIndexed { i, answer ->
                AnswerOption(
                    index = i,
                    text = answer.text,
                    state = answerState(question, answer, state.selectedAnswerId, state.revealed),
                    enabled = !state.revealed,
                    onClick = { onAnswer(answer.id) },
                )
            }
            Reveal(state.revealed) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val correct = state.selectedAnswerId == question.correctAnswerId
                    if (state.mode == PracticeMode.REVIEW) RepetitionResult(correct, state.cardAfter)
                    ExplanationCard(question, state.rules, correct)
                }
            }
        }
        Reveal(state.revealed) {
            ZButton(
                if (state.index + 1 >= state.questions.size) "Завершити" else "Далі",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

/** Plain (non-scope) AnimatedVisibility, usable from any parent. */
@Composable
private fun Reveal(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 3 }) { content() }
}

private fun mistakeAgo(millis: Long): String {
    val days = ((System.currentTimeMillis() - millis) / 86_400_000L).toInt()
    return when {
        days <= 0 -> "Помилка сьогодні"
        days == 1 -> "Помилка вчора"
        else -> "Помилка ${days} ${daysWord(days)} тому"
    }
}

internal fun daysWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "днів"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дні"
        else -> "днів"
    }
}

/** «ПОЯСНЕННЯ» with our explanation and the quoted road-rules clause. */
@Composable
internal fun ExplanationCard(question: Question, rules: Map<String, String>, correct: Boolean) {
    val c = ZnakyTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .padding(16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ПОЯСНЕННЯ", style = ZnakyType.caption, color = if (correct) c.success else c.error, modifier = Modifier.weight(1f))
            Text(if (correct) "Правильно" else "Помилка", style = ZnakyType.caption, color = if (correct) c.success else c.error)
        }
        Text(
            question.explanation ?: "Правильна відповідь: «${question.answers.first { it.id == question.correctAnswerId }.text}».",
            style = ZnakyType.callout,
            color = c.textPrimary,
        )
        question.ruleRefs.forEach { point ->
            val clause = rules[point]
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.background),
            ) {
                Box(Modifier.width(3.dp).fillMaxHeight().background(c.primary))
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = c.link, fontWeight = FontWeight.Bold)) { append(if (point.first().isDigit()) "ПДР $point" else point) }
                        if (clause != null) append(" — $clause")
                    },
                    style = ZnakyType.subhead,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}

/** Labels of the repetition track: box 1 is «Сьогодні», box 6 is learned. */
internal val TrackLabels = listOf("Сьогодні", "1 д", "3 д", "7 д", "14 д", "✓")

@Composable
private fun RepetitionResult(correct: Boolean, card: ReviewCard?) {
    val c = ZnakyTheme.colors
    val box = card?.box ?: 1
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (correct) "Правильно! Питання їде далі" else "Помилка — питання повертається на старт",
                style = ZnakyType.headline,
                color = if (correct) c.success else c.error,
                modifier = Modifier.weight(1f),
            )
            val days = card?.let { (it.dueEpochDay - java.time.LocalDate.now().toEpochDay()).toInt() }
            val label = when {
                box >= ReviewCard.MAX_BOX -> "Вивчено"
                days == null || days <= 0 -> "сьогодні ще раз"
                else -> "через $days ${daysWord(days)}"
            }
            Pill(label, background = c.primaryTint, contentColor = c.onPrimaryTint)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TrackLabels.forEachIndexed { i, text ->
                val color = when {
                    !correct && i == 0 -> c.error
                    i == box - 1 -> c.primary
                    i < box - 1 -> c.success
                    else -> c.surfaceMuted
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(color))
                    if (i == TrackLabels.lastIndex) {
                        Icon(Icons.Rounded.Check, contentDescription = "Вивчено", tint = c.textTertiary, modifier = Modifier.size(14.dp))
                    } else {
                        Text(text, style = ZnakyType.caption, color = c.textTertiary, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun Finished(correct: Int, total: Int, mode: String, onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    val note = remember(mode) {
        when (mode) {
            PracticeMode.REVIEW -> "Правильні відповіді посунули питання далі по треку. Помилки повернуться завтра."
            else -> "Помилки потраплять у повторення, і застосунок нагадає про них."
        }
    }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        ProgressRing(correct.toFloat() / total, size = 160.dp, stroke = 14.dp, color = c.success) {
            Text("$correct/$total", style = ZnakyType.largeTitle, color = c.textPrimary)
        }
        Text("Сесію завершено", style = ZnakyType.title, color = c.textPrimary)
        Text(note, style = ZnakyType.callout, color = c.textSecondary, textAlign = TextAlign.Center)
        ZButton("Готово", onClick = onBack, modifier = Modifier.fillMaxWidth(), kind = ButtonKind.Primary)
    }
}

@Composable
private fun Message(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = ZnakyType.body, color = ZnakyTheme.colors.textSecondary, textAlign = TextAlign.Center)
    }
}

