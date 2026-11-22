package com.znaky.feature.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.SignColors
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class RulesViewModel @Inject constructor(questions: QuestionRepository) : ViewModel() {
    val sections: StateFlow<List<Topic>?> = questions.observeTopics()
        .map { list -> list.filter { it.block == ExamBlock.TRAFFIC_RULES }.sortedBy { it.number.toIntOrNull() ?: Int.MAX_VALUE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
internal fun RulesScreen(onBack: () -> Unit, onOpen: (String) -> Unit, viewModel: RulesViewModel = hiltViewModel()) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val list = sections ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "title") { LargeTitle("Правила", subtitle = "Розділи ПДР, з яких є питання на іспиті") }
        item(key = "list") {
            GroupedList(Modifier.padding(horizontal = 16.dp)) {
                list.forEachIndexed { i, t ->
                    ListRow(
                        title = t.title,
                        subtitle = "${t.questionCount} питань",
                        showDivider = i > 0,
                        onClick = { onOpen(t.id) },
                        leading = {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(c.tealTint), contentAlignment = Alignment.Center) {
                                Text(t.number, style = ZnakyType.footnote, color = c.teal)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun RuleSectionScreen(topicId: String, onBack: () -> Unit, onTrain: (String) -> Unit, viewModel: RulesViewModel = hiltViewModel()) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val topic = sections?.firstOrNull { it.id == topicId } ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Розділ ${topic.number}") }
        item(key = "title") { LargeTitle(topic.title) }
        if (topic.number == "12") {
            item(key = "speed") { SpeedLimits() }
            item(key = "points") {
                GroupedList(Modifier.padding(horizontal = 16.dp), header = "Головне") {
                    ListRow("У населених пунктах — до 50 км/год", subtitle = "п. 12.4")
                    ListRow("У житловій і пішохідній зонах — до 20 км/год", subtitle = "п. 12.5", showDivider = true)
                    ListRow("Водій зі стажем до 2 років — до 70 км/год", subtitle = "п. 12.7", showDivider = true)
                }
            }
        } else {
            item(key = "soon") {
                Text(
                    "Наочний конспект цього розділу з’явиться в наступних оновленнях. Поки що тренуйтеся на питаннях.",
                    style = ZnakyType.callout,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
        item(key = "train") {
            ZButton("${topic.questionCount} питань із розділу", onClick = { onTrain(topic.id) }, icon = Icons.Rounded.Quiz, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun SpeedLimits() {
    val c = ZnakyTheme.colors
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Легковий автомобіль, км/год", style = ZnakyType.footnote, color = c.textTertiary)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Limit("50", "населений пункт")
            Limit("90", "поза містом")
            Limit("110", "дорога з розділювальною смугою")
            Limit("130", "автомагістраль")
        }
    }
}

@Composable
private fun Limit(value: String, label: String) {
    val c = ZnakyTheme.colors
    Column(Modifier.size(width = 76.dp, height = 120.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(SignColors.Red)
                .padding(6.dp)
                .clip(CircleShape)
                .background(Color.White)
                .semantics { contentDescription = "$value км/год, $label" },
            contentAlignment = Alignment.Center,
        ) {
            Text(value, style = ZnakyType.headline, color = SignColors.Black)
        }
        Text(label, style = ZnakyType.caption.copy(fontWeight = ZnakyType.subhead.fontWeight), color = c.textSecondary, textAlign = TextAlign.Center)
    }
}
