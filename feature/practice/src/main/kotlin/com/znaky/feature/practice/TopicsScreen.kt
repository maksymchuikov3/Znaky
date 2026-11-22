package com.znaky.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.SearchField
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.ExamBlock

internal fun ExamBlock.title() = when (this) {
    ExamBlock.TRAFFIC_RULES -> "Правила дорожнього руху"
    ExamBlock.ROAD_SAFETY -> "Безпека руху"
    ExamBlock.VEHICLE -> "Будова автомобіля"
    ExamBlock.FIRST_AID -> "Домедична допомога"
}

@Composable
internal fun TopicsScreen(onTopicClick: (String) -> Unit, viewModel: TopicsViewModel = hiltViewModel()) {
    val items by viewModel.topics.collectAsStateWithLifecycle()
    val list = items ?: return LoadingBox()
    var query by rememberSaveable { mutableStateOf("") }
    val c = ZnakyTheme.colors
    // Grouping is recomputed only when the data or the query changes, not on every frame.
    val groups = remember(list, query) {
        list.filter { query.isBlank() || it.topic.title.contains(query.trim(), ignoreCase = true) || it.topic.number == query.trim() }
            .groupBy { it.topic.block }
            .toSortedMap()
    }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "title") {
            LargeTitle("Тести", Modifier.padding(top = 16.dp), subtitle = "${list.size} тем · ${list.sumOf { it.topic.questionCount }} питань")
        }
        item(key = "search") { SearchField(query, { query = it }, "Тема або номер розділу") }
        groups.forEach { (block, topics) ->
            item(key = block.name) {
                GroupedList(Modifier.padding(horizontal = 16.dp), header = block.title()) {
                    topics.forEachIndexed { i, item ->
                        val p = item.progress
                        val total = p?.totalQuestions ?: item.topic.questionCount
                        val done = p?.correctlyAnsweredQuestions ?: 0
                        ListRow(
                            title = item.topic.title,
                            subtitle = "Розділ ${item.topic.number} · $done з $total",
                            showDivider = i > 0,
                            onClick = { onTopicClick(item.topic.id) },
                            leading = {
                                ProgressRing(p?.completion ?: 0f, size = 36.dp, stroke = 4.dp, color = if ((p?.completion ?: 0f) >= 0.9f) c.success else c.primary)
                            },
                        )
                    }
                }
            }
        }
        if (groups.isEmpty()) {
            item(key = "empty") {
                Text("Нічого не знайдено", style = ZnakyType.callout, color = c.textTertiary, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
        item(key = "bottom") { Spacer(Modifier.height(16.dp)) }
    }
}
