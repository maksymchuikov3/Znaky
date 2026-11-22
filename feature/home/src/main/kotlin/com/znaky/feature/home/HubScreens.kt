package com.znaky.feature.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.SectionTitle
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import javax.inject.Inject

/** The "Вчити" tab: the study route on top, the reference sections below. */
@Serializable
data object LearnHubRoute

/** The "Тренування" tab: mock exam, topic tests, mistakes, the sign game. */
@Serializable
data object TrainingRoute

data class HubState(val route: List<PlanTopic>, val dueForReview: Int)

@HiltViewModel
class HubViewModel @Inject constructor(questions: QuestionRepository, progress: ProgressRepository) : ViewModel() {
    val state: StateFlow<HubState?> = combine(
        questions.observeTopics(),
        progress.observeTopicProgress(),
        progress.observeDueForReview(),
    ) { topics, topicProgress, due -> HubState(studyRoute(topics, topicProgress), due.size) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

fun NavGraphBuilder.hubScreens(
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenReview: () -> Unit,
) {
    composable<LearnHubRoute> {
        val viewModel: HubViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        LearnHubScreen(state, onOpenSection, onOpenTopic)
    }
    composable<TrainingRoute> {
        val viewModel: HubViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        TrainingScreen(state?.dueForReview ?: 0, onOpenSection, onOpenReview)
    }
}

/** How many route steps the Learn tab shows before "Усі теми". */
private const val ROUTE_PREVIEW = 4

@Composable
private fun LearnHubScreen(state: HubState?, onOpenSection: (HomeSection) -> Unit, onOpenTopic: (String) -> Unit) {
    val c = ZnakyTheme.colors
    val route = state?.route.orEmpty()
    // Show the route from the first unfinished topic, so the next step is always on top.
    val start = route.indexOfFirst { it.completion < LEARNED }.let { if (it < 0) 0 else it }
    val preview = route.drop(start).take(ROUTE_PREVIEW)
    val learned = route.count { it.completion >= LEARNED }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "title") {
            LargeTitle(
                "Вчити",
                Modifier.padding(top = 16.dp),
                subtitle = if (route.isEmpty()) "Маршрут тем і довідник" else "Вивчено $learned з ${route.size} тем",
            )
        }
        if (preview.isNotEmpty()) {
            item(key = "routeTitle") { SectionTitle("Маршрут", trailing = "Усі теми", onTrailingClick = { onOpenSection(HomeSection.TOPICS) }) }
            item(key = "route") {
                GroupedList(Modifier.padding(horizontal = 16.dp)) {
                    preview.forEachIndexed { i, topic ->
                        val done = topic.completion >= LEARNED
                        ListRow(
                            title = topic.title,
                            subtitle = if (i == 0 && !done) "Далі · розділ ${topic.number}" else "Розділ ${topic.number} · ${(topic.completion * 100).toInt()}%",
                            showDivider = i > 0,
                            onClick = { onOpenTopic(topic.id) },
                            leading = { RouteStep(start + i + 1, done, current = i == 0 && !done) },
                        )
                    }
                }
            }
        }
        item(key = "refTitle") { SectionTitle("Довідник", Modifier.padding(top = 4.dp)) }
        item(key = "ref") { LearnGrid(onOpenSection) }
    }
}

@Composable
private fun RouteStep(number: Int, done: Boolean, current: Boolean) {
    val c = ZnakyTheme.colors
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> c.successTint
                    current -> c.primary
                    else -> c.surfaceMuted
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Rounded.Check, contentDescription = "Вивчено", tint = c.success, modifier = Modifier.size(18.dp))
        } else {
            Text("$number", style = ZnakyType.footnote, color = if (current) c.onPrimary else c.textSecondary)
        }
    }
}

@Composable
private fun TrainingScreen(due: Int, onOpenSection: (HomeSection) -> Unit, onOpenReview: () -> Unit) {
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "title") { LargeTitle("Тренування", Modifier.padding(top = 16.dp), subtitle = "Пробний іспит, теми і помилки") }
        item(key = "exam") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(c.primaryTint)
                    .clickable(role = Role.Button, onClick = { onOpenSection(HomeSection.EXAM_INTRO) })
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HubIcon(Icons.Rounded.Timer, c.surface, c.link)
                    Column(Modifier.weight(1f)) {
                        Text("Пробний іспит", style = ZnakyType.headline, color = c.onPrimaryTint)
                        Text("20 питань · 20 хвилин · до 2 помилок", style = ZnakyType.subhead, color = c.onPrimaryTint)
                    }
                }
                ZButton("Почати", onClick = { onOpenSection(HomeSection.EXAM) }, modifier = Modifier.fillMaxWidth())
            }
        }
        item(key = "modes") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Режими") {
                ListRow(
                    title = "Тести за темами",
                    subtitle = "Розділи правил по черзі",
                    onClick = { onOpenSection(HomeSection.TOPICS) },
                    leading = { HubIcon(Icons.AutoMirrored.Rounded.FormatListBulleted, c.tealTint, c.teal) },
                )
                ListRow(
                    title = "Мої помилки",
                    subtitle = if (due > 0) "$due ${questionsWord(due)} на сьогодні" else "На сьогодні все повторено",
                    showDivider = true,
                    onClick = if (due > 0) onOpenReview else null,
                    leading = { HubIcon(Icons.Rounded.Replay, c.redTint, c.red) },
                )
                ListRow(
                    title = "Знак на швидкість",
                    subtitle = "Гра: впізнайте знак за 4 секунди",
                    showDivider = true,
                    onClick = { onOpenSection(HomeSection.SIGN_GAME) },
                    leading = { HubIcon(Icons.Rounded.SportsEsports, c.accentTint, c.onAccentTint) },
                )
            }
        }
        item(key = "progress") {
            ProgressHint(onClick = { onOpenSection(HomeSection.PROGRESS) })
        }
    }
}

@Composable
private fun ProgressHint(onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Text(
        "Готовність до іспиту і слабкі теми дивіться в «Прогресі».",
        style = ZnakyType.footnote,
        color = c.textTertiary,
        modifier = Modifier.padding(horizontal = 24.dp).clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun HubIcon(icon: ImageVector, bg: Color, fg: Color) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
    }
}
