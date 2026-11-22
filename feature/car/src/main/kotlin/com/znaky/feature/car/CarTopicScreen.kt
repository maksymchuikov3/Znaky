package com.znaky.feature.car

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.StepBar
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

/** A short step-by-step lesson with an animated scene per step. */
@Composable
internal fun CarTopicScreen(
    topicId: String,
    startStep: Int,
    onBack: () -> Unit,
    onTrain: (examTopicId: String) -> Unit,
) {
    val topic = carTopics[topicId] ?: carTopics.getValue("brakes")
    val c = ZnakyTheme.colors
    var index by rememberSaveable { mutableIntStateOf(startStep.coerceIn(0, topic.steps.lastIndex)) }
    val last = index == topic.steps.lastIndex

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack) {
            StepBar(topic.steps.size, index + 1, Modifier.weight(1f), color = topic.accent)
            Text("${index + 1}/${topic.steps.size}", style = ZnakyType.footnote, color = c.textSecondary)
        }
        AnimatedContent(
            targetState = index,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "step",
        ) { i ->
            val step = topic.steps[i]
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(topic.group, style = ZnakyType.caption, color = topic.accent)
                    Text(step.title, style = ZnakyType.title, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                }
                TopicScene(step.scene, dark = c.isDark)
                ZCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(step.text, style = ZnakyType.body, color = c.textPrimary)
                        if (step.facts.isNotEmpty()) Facts(step.facts)
                        step.warning?.let { Warning(it) }
                        step.note?.let { Text(it, style = ZnakyType.footnote, color = c.textSecondary) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (index > 0) {
                ZButton("", onClick = { index-- }, kind = ButtonKind.Plain, icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Попередній крок", modifier = Modifier.size(56.dp))
            }
            if (last && topic.examTopicId != null) {
                ZButton("5 питань", onClick = { onTrain(topic.examTopicId) }, kind = ButtonKind.Tinted, modifier = Modifier.weight(1f))
            }
            ZButton(
                if (last) "Готово" else "Далі",
                onClick = { if (last) onBack() else index++ },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Facts(facts: List<Pair<String, String>>) {
    val tints = listOf(Color(0xFFFFF8DB) to Color(0xFF7A5600), Color(0xFFDDF7F2) to Color(0xFF0F5E54))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        facts.forEachIndexed { i, (title, value) ->
            val (bg, fg) = tints[i % tints.size]
            Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(title, style = ZnakyType.caption, color = fg)
                Text(value, style = ZnakyType.subhead, color = fg)
            }
        }
    }
}

@Composable
private fun Warning(text: String) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.errorTint).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Rounded.Warning, contentDescription = null, tint = c.error, modifier = Modifier.size(20.dp))
        Text(text, style = ZnakyType.subhead, color = c.textPrimary)
    }
}
