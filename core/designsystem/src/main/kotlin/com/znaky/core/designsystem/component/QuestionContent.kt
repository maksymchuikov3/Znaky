package com.znaky.core.designsystem.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.znaky.core.model.Question

/**
 * Question text, optional picture and answers. [revealed] = true shows the correct answer
 * and, when [showExplanation] is set, the explanation with rule references.
 */
@Composable
fun QuestionContent(
    question: Question,
    selectedAnswerId: String?,
    revealed: Boolean,
    showExplanation: Boolean,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
    rulePointLabel: (String) -> String = { "п. $it ПДР" },
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(question.text, style = MaterialTheme.typography.titleMedium)
        question.imageAsset?.let { AssetImage(it) }
        question.answers.forEachIndexed { i, answer ->
            val state = when {
                !revealed -> AnswerState.NEUTRAL
                answer.id == question.correctAnswerId -> AnswerState.CORRECT
                answer.id == selectedAnswerId -> AnswerState.WRONG
                else -> AnswerState.NEUTRAL
            }
            AnswerOption(
                index = i,
                text = answer.text,
                state = state,
                enabled = !revealed,
                onClick = { onAnswer(answer.id) },
            )
        }
        if (revealed && showExplanation && (question.explanation != null || question.ruleRefs.isNotEmpty())) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.explanation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    if (question.ruleRefs.isNotEmpty()) {
                        Text(
                            question.ruleRefs.joinToString(" · ") { rulePointLabel(it) },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetImage(path: String) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        runCatching { context.assets.open(path).use(BitmapFactory::decodeStream)?.asImageBitmap() }.getOrNull()
    } ?: return
    Image(
        bitmap = bitmap,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth(),
    )
}
