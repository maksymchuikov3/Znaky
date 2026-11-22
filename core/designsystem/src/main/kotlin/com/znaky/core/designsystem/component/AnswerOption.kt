package com.znaky.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.theme.FeedbackColors

enum class AnswerState { NEUTRAL, CORRECT, WRONG }

@Composable
fun AnswerOption(
    index: Int,
    text: String,
    state: AnswerState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (container, border) = when (state) {
        AnswerState.NEUTRAL -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.outlineVariant
        AnswerState.CORRECT -> FeedbackColors.CorrectContainer to FeedbackColors.Correct
        AnswerState.WRONG -> FeedbackColors.WrongContainer to FeedbackColors.Wrong
    }
    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .semantics { role = Role.Button },
        colors = CardDefaults.outlinedCardColors(
            containerColor = container,
            disabledContainerColor = container,
            contentColor = if (state == AnswerState.NEUTRAL) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Black,
            disabledContentColor = if (state == AnswerState.NEUTRAL) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Black,
        ),
        border = BorderStroke(if (state == AnswerState.NEUTRAL) 1.dp else 2.dp, border),
    ) {
        Text(
            text = "${index + 1}. $text",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}
