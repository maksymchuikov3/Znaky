package com.znaky.feature.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.QuestionContent
import com.znaky.core.designsystem.theme.FeedbackColors
import com.znaky.core.domain.exam.ExamSession
import com.znaky.core.domain.exam.ExamSession.FinishReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamScreen(onExit: () -> Unit, onRetry: () -> Unit, viewModel: ExamViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val running = state as? ExamUiState.Running
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(running?.takeUnless { it.session.isFinished }?.let { formatTime(it.remainingSeconds) } ?: stringResource(R.string.exam_title))
                },
                actions = {
                    if (running != null && !running.session.isFinished) {
                        Text(
                            stringResource(R.string.exam_mistakes, running.session.mistakes, running.session.rules.maxMistakes),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        IconButton(onClick = viewModel::abandon) {
                            Icon(Icons.Default.Close, stringResource(R.string.exam_abandon))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            ExamUiState.Loading -> LoadingBox(Modifier.padding(padding))
            ExamUiState.NotEnoughQuestions -> Text(
                stringResource(R.string.exam_not_enough_questions),
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            is ExamUiState.Running ->
                if (s.session.isFinished) {
                    ExamResultContent(s.session, padding, onExit, onRetry)
                } else {
                    ExamQuestionContent(s.session, padding, viewModel::answer, viewModel::goTo)
                }
        }
    }
}

@Composable
private fun ExamQuestionContent(
    session: ExamSession,
    padding: PaddingValues,
    onAnswer: (String) -> Unit,
    onGoTo: (Int) -> Unit,
) {
    val question = session.currentQuestion ?: return
    Column(Modifier.fillMaxSize().padding(padding)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(session.questions, key = { _, q -> q.id }) { i, q ->
                val answer = session.answers[q.id]
                val color = when {
                    answer == null -> MaterialTheme.colorScheme.surfaceVariant
                    q.isCorrect(answer) -> FeedbackColors.Correct
                    else -> FeedbackColors.Wrong
                }
                val label = stringResource(R.string.exam_question_number, i + 1)
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onGoTo(i) }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${i + 1}",
                        color = if (answer == null) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        style = if (i == session.currentIndex) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        HorizontalDivider()
        QuestionContent(
            question = question,
            selectedAnswerId = session.answers[question.id],
            revealed = question.id in session.answers,
            showExplanation = false,
            onAnswer = onAnswer,
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        )
    }
}

@Composable
private fun ExamResultContent(session: ExamSession, padding: PaddingValues, onExit: () -> Unit, onRetry: () -> Unit) {
    val mistakes = session.questions.filter { q -> session.answers[q.id]?.let { !q.isCorrect(it) } == true }
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(if (session.isPassed) R.string.exam_passed else R.string.exam_failed),
            style = MaterialTheme.typography.headlineMedium,
            color = if (session.isPassed) FeedbackColors.Correct else FeedbackColors.Wrong,
        )
        Text(stringResource(R.string.exam_score, session.correctAnswers, session.questions.size))
        when (session.finishReason) {
            FinishReason.TOO_MANY_MISTAKES -> Text(stringResource(R.string.exam_reason_mistakes, session.rules.maxMistakes))
            FinishReason.TIME_OUT -> Text(stringResource(R.string.exam_reason_timeout))
            FinishReason.ABANDONED -> Text(stringResource(R.string.exam_reason_abandoned))
            else -> Unit
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) { Text(stringResource(R.string.exam_retry)) }
            OutlinedButton(onClick = onExit) { Text(stringResource(R.string.exam_done)) }
        }
        if (mistakes.isNotEmpty()) {
            Text(stringResource(R.string.exam_review_mistakes), style = MaterialTheme.typography.titleLarge)
            mistakes.forEach { q ->
                QuestionContent(
                    question = q,
                    selectedAnswerId = session.answers[q.id],
                    revealed = true,
                    showExplanation = true,
                    onAnswer = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider()
            }
        }
    }
}

private fun formatTime(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
