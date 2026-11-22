package com.znaky.core.designsystem.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.Answer
import com.znaky.core.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AnswerState { NEUTRAL, SELECTED, CORRECT, WRONG }

@Composable
fun AnswerOption(
    index: Int,
    text: String,
    state: AnswerState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZnakyTheme.colors
    val (frame, fill) = when (state) {
        AnswerState.NEUTRAL -> null to c.surface
        AnswerState.SELECTED -> c.primary to c.primaryTint
        AnswerState.CORRECT -> c.success to c.successTint
        AnswerState.WRONG -> c.error to c.errorTint
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .optionFrame(frame, c.separator, fill)
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                selected = state != AnswerState.NEUTRAL
                stateDescription = when (state) {
                    AnswerState.CORRECT -> "Правильна відповідь"
                    AnswerState.WRONG -> "Неправильна відповідь"
                    AnswerState.SELECTED -> "Вибрано"
                    AnswerState.NEUTRAL -> ""
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            AnswerState.CORRECT -> StatusBadge(Icons.Rounded.Check, c.success)
            AnswerState.WRONG -> StatusBadge(Icons.Rounded.Close, c.error)
            AnswerState.SELECTED -> NumberBadge("${index + 1}", c.primary, c.onPrimary)
            AnswerState.NEUTRAL -> NumberBadge("${index + 1}", c.surfaceMuted, c.textSecondary)
        }
        Text(text, style = ZnakyType.callout.copy(fontWeight = ZnakyType.bodyStrong.fontWeight), color = c.textPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusBadge(icon: ImageVector, color: Color) {
    Box(Modifier.size(26.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = ZnakyTheme.colors.surface, modifier = Modifier.size(16.dp))
    }
}

/** Which state an answer is in, given what the user picked and whether the result is shown. */
fun answerState(question: Question, answer: Answer, selectedAnswerId: String?, revealed: Boolean): AnswerState = when {
    revealed && answer.id == question.correctAnswerId -> AnswerState.CORRECT
    revealed && answer.id == selectedAnswerId -> AnswerState.WRONG
    !revealed && answer.id == selectedAnswerId -> AnswerState.SELECTED
    else -> AnswerState.NEUTRAL
}

/** Question text, optional picture and the answers. */
@Composable
fun QuestionContent(
    question: Question,
    selectedAnswerId: String?,
    revealed: Boolean,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = !revealed,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(question.text, style = ZnakyType.headline.copy(lineHeight = ZnakyType.body.lineHeight), color = ZnakyTheme.colors.textPrimary)
        question.imageAsset?.let { AssetImage(it) }
        question.answers.forEachIndexed { i, answer ->
            AnswerOption(
                index = i,
                text = answer.text,
                state = answerState(question, answer, selectedAnswerId, revealed),
                enabled = enabled,
                onClick = { onAnswer(answer.id) },
            )
        }
    }
}

/** Decodes the bundled picture off the main thread so scrolling stays smooth. */
@Composable
private fun AssetImage(path: String) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)),
        )
    }
}
