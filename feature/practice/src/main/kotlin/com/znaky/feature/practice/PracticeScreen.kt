package com.znaky.feature.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.QuestionContent
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.Question

@Composable
internal fun PracticeScreen(onBack: () -> Unit, viewModel: PracticeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        val total = state.questions.size
        TopBar(onBack = onBack, close = true) {
            if (total > 0 && !state.isFinished) {
                LinearBar((state.index + if (state.revealed) 1 else 0).toFloat() / total, Modifier.weight(1f).padding(end = 12.dp), height = 8.dp)
                Text("${state.index + 1}/$total", style = ZnakyType.footnote, color = c.textSecondary)
            }
        }
        when {
            state.isLoading -> LoadingBox()
            total == 0 -> Message("Тут поки немає питань. Поверніться пізніше!")
            state.isFinished -> Finished(state.correctCount, total, onBack)
            else -> {
                val question = state.current ?: return@Column
                Box(Modifier.weight(1f)) {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.title?.let { Text(it.uppercase(), style = ZnakyType.caption, color = c.textTertiary, modifier = Modifier.padding(horizontal = 4.dp)) }
                        QuestionContent(question, state.selectedAnswerId, state.revealed, onAnswer = viewModel::answer)
                        // Room for the feedback sheet so it never covers the last answer.
                        Spacer(Modifier.height(if (state.revealed) 260.dp else 24.dp))
                    }
                    AnimatedVisibility(
                        visible = state.revealed,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        FeedbackSheet(question, state.selectedAnswerId, isLast = state.index + 1 >= total, onNext = viewModel::next)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackSheet(question: Question, selected: String?, isLast: Boolean, onNext: () -> Unit) {
    val c = ZnakyTheme.colors
    val correct = selected == question.correctAnswerId
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(c.surface)
            .navigationBarsPadding()
            .padding(20.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                contentDescription = null,
                tint = if (correct) c.success else c.error,
                modifier = Modifier.size(28.dp),
            )
            Text(
                if (correct) "Правильно!" else "Неправильно",
                style = ZnakyType.title2,
                color = if (correct) c.success else c.error,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        question.explanation?.let { Text(it, style = ZnakyType.callout, color = c.textSecondary) }
        if (question.ruleRefs.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                question.ruleRefs.forEach { Pill("п. $it ПДР") }
            }
        }
        ZButton(if (isLast) "Завершити" else "Далі", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Finished(correct: Int, total: Int, onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        ProgressRing(correct.toFloat() / total, size = 160.dp, stroke = 14.dp, color = c.success) {
            Text("$correct/$total", style = ZnakyType.largeTitle, color = c.textPrimary)
        }
        Text("Сесію завершено", style = ZnakyType.title, color = c.textPrimary)
        Text("Помилки потраплять у повторення, і застосунок нагадає про них.", style = ZnakyType.callout, color = c.textSecondary, textAlign = TextAlign.Center)
        ZButton("Готово", onClick = onBack, modifier = Modifier.fillMaxWidth(), kind = ButtonKind.Primary)
    }
}

@Composable
private fun Message(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = ZnakyType.body, color = ZnakyTheme.colors.textSecondary, textAlign = TextAlign.Center)
    }
}
