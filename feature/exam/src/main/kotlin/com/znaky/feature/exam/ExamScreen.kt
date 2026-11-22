package com.znaky.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.AnswerOption
import com.znaky.core.designsystem.component.AnswerState
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.QuestionContent
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.exam.ExamSession

@Composable
internal fun ExamScreen(onExit: () -> Unit, onRetry: () -> Unit, viewModel: ExamViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = ZnakyTheme.colors
    Box(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        when (val s = state) {
            ExamUiState.Loading -> LoadingBox()
            ExamUiState.NotEnoughQuestions -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                Text("Недостатньо питань для іспиту в обраній категорії.", style = ZnakyType.body, color = c.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                ZButton("Назад", onClick = onExit, modifier = Modifier.fillMaxWidth())
            }
            is ExamUiState.Running ->
                if (s.session.isFinished) {
                    ExamResultContent(s.session, onExit, onRetry)
                } else {
                    RunningExam(s.session, s.remainingSeconds, viewModel::answer, viewModel::goTo, viewModel::abandon)
                }
        }
    }
}

@Composable
private fun RunningExam(
    session: ExamSession,
    remainingSeconds: Int,
    onAnswer: (String) -> Unit,
    onGoTo: (Int) -> Unit,
    onAbandon: () -> Unit,
) {
    val c = ZnakyTheme.colors
    val question = session.currentQuestion ?: return
    // The choice is only committed with "Відповісти", like in the service-centre software.
    var selected by rememberSaveable(question.id) { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onAbandon, close = true) {
            Text("Питання ${session.currentIndex + 1} з ${session.questions.size}", style = ZnakyType.bodyStrong, color = c.textPrimary, modifier = Modifier.weight(1f))
            MistakeDots(session.mistakes, session.rules.maxMistakes)
            TimerPill(remainingSeconds)
        }
        QuestionStrip(session, onGoTo)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuestionContent(question, selectedAnswerId = selected, revealed = false, onAnswer = { selected = it }, enabled = true)
        }
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ZButton("Пропустити", onClick = { onGoTo(nextIndex(session)) }, kind = ButtonKind.Tinted, modifier = Modifier.weight(1f))
            ZButton("Відповісти", onClick = { selected?.let(onAnswer) }, enabled = selected != null, modifier = Modifier.weight(1f))
        }
    }
}

private fun nextIndex(session: ExamSession): Int {
    val n = session.questions.size
    for (step in 1 until n) {
        val i = (session.currentIndex + step) % n
        if (session.questions[i].id !in session.answers) return i
    }
    return session.currentIndex
}

@Composable
private fun MistakeDots(mistakes: Int, max: Int) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.semantics { contentDescription = "Помилок $mistakes з $max дозволених" },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(max) { i ->
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (i < mistakes) c.error else c.surfaceMuted))
        }
    }
}

@Composable
private fun TimerPill(seconds: Int) {
    val c = ZnakyTheme.colors
    val low = seconds < 5 * 60
    Row(
        Modifier.clip(CircleShape).background(if (low) c.errorTint else c.surface).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Timer, contentDescription = null, tint = if (low) c.error else c.link, modifier = Modifier.size(16.dp))
        Text(
            "%d:%02d".format(seconds / 60, seconds % 60),
            style = ZnakyType.footnote.copy(fontFeatureSettings = "tnum"),
            color = if (low) c.error else c.textPrimary,
            modifier = Modifier.padding(start = 4.dp).semantics { contentDescription = "Залишилось ${seconds / 60} хв" },
        )
    }
}

@Composable
private fun QuestionStrip(session: ExamSession, onGoTo: (Int) -> Unit) {
    val c = ZnakyTheme.colors
    val listState = rememberLazyListState()
    LaunchedEffect(session.currentIndex) { listState.animateScrollToItem((session.currentIndex - 3).coerceAtLeast(0)) }
    LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(session.questions, key = { _, q -> q.id }) { i, q ->
            val answered = q.id in session.answers
            val current = i == session.currentIndex
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (answered) c.primary else c.surface)
                    .then(if (current) Modifier.border(2.dp, c.link, CircleShape) else Modifier)
                    .clickable(enabled = !answered, role = Role.Button) { onGoTo(i) }
                    .semantics { contentDescription = "Питання ${i + 1}" + if (answered) ", є відповідь" else "" },
                contentAlignment = Alignment.Center,
            ) {
                Text("${i + 1}", style = ZnakyType.footnote, color = if (answered) c.onPrimary else c.textSecondary)
            }
        }
    }
}

@Composable
private fun ExamResultContent(session: ExamSession, onExit: () -> Unit, onRetry: () -> Unit) {
    val c = ZnakyTheme.colors
    val passed = session.isPassed
    val total = session.questions.size
    val wrong = session.questions.filter { q -> session.answers[q.id]?.let { !q.isCorrect(it) } == true }
    val minutes = (((session.finishedAtMillis ?: session.startedAtMillis) - session.startedAtMillis) / 60_000).toInt()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "ring") {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressRing(session.correctAnswers.toFloat() / total, size = 168.dp, stroke = 14.dp, color = if (passed) c.success else c.error) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${session.correctAnswers}/$total", style = ZnakyType.largeTitle, color = c.textPrimary)
                        Text("правильно", style = ZnakyType.footnote, color = c.textTertiary)
                    }
                }
                Text(
                    if (passed) "Складено" else "Не складено",
                    style = ZnakyType.headline,
                    color = if (passed) c.success else c.error,
                    modifier = Modifier.clip(CircleShape).background(if (passed) c.successTint else c.errorTint).padding(horizontal = 16.dp, vertical = 6.dp),
                )
                Text(reasonText(session), style = ZnakyType.callout, color = c.textSecondary, textAlign = TextAlign.Center)
            }
        }
        item(key = "stats") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("Правильно", "${session.correctAnswers}", Modifier.weight(1f))
                Stat("Помилки", "${session.mistakes}", Modifier.weight(1f))
                Stat("Час", "$minutes хв", Modifier.weight(1f))
            }
        }
        if (wrong.isNotEmpty()) {
            item(key = "mistakesTitle") { Text("Помилки", style = ZnakyType.title2, color = c.textPrimary, modifier = Modifier.padding(top = 6.dp)) }
            wrong.forEach { q ->
                item(key = q.id) {
                    ZCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(q.text, style = ZnakyType.bodyStrong, color = c.textPrimary)
                            q.answers.forEachIndexed { i, a ->
                                val st = when (a.id) {
                                    q.correctAnswerId -> AnswerState.CORRECT
                                    session.answers[q.id] -> AnswerState.WRONG
                                    else -> null
                                }
                                if (st != null) AnswerOption(i, a.text, st, enabled = false, onClick = {})
                            }
                            q.explanation?.let { Text(it, style = ZnakyType.subhead, color = c.textSecondary) }
                        }
                    }
                }
            }
        }
        item(key = "buttons") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(Modifier.height(4.dp))
                ZButton("Новий іспит", onClick = onRetry, modifier = Modifier.fillMaxWidth())
                ZButton("На головну", onClick = onExit, kind = ButtonKind.Tinted, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun reasonText(session: ExamSession) = when (session.finishReason) {
    ExamSession.FinishReason.ALL_ANSWERED ->
        if (session.isPassed) "Ви відповіли на всі питання в межах дозволених помилок." else "Забагато помилок."
    ExamSession.FinishReason.TOO_MANY_MISTAKES -> "Допущено більше ${session.rules.maxMistakes} помилок."
    ExamSession.FinishReason.TIME_OUT -> "Час вичерпано."
    ExamSession.FinishReason.ABANDONED, null -> "Іспит завершено достроково."
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    val c = ZnakyTheme.colors
    ZCard(modifier, radius = 18.dp, contentPadding = PaddingValues(12.dp)) {
        Text(value, style = ZnakyType.title2, color = c.textPrimary)
        Text(label, style = ZnakyType.footnote, color = c.textTertiary)
    }
}
