package com.znaky.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Signpost
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyColors
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.readiness.ReadinessCalculator.Level
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Serializable
data object HomeRoute

/** Places the home, learn and training screens can send the user to. */
enum class HomeSection {
    RULES, SIGNS, MARKINGS, EXPLAIN, CAR, SIGN_GAME,
    /** Starts a mock exam right away. */
    EXAM,
    /** The exam rules screen with the start button. */
    EXAM_INTRO,
    TOPICS, PROGRESS, LEARN, TRAINING,
}

fun NavGraphBuilder.homeScreen(
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenReview: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(state, onOpenSection, onOpenTopic, onOpenReview)
    }
}

@Immutable
internal data class Tile(val section: HomeSection, val label: String, val icon: ImageVector, val fg: (ZnakyColors) -> Color, val bg: (ZnakyColors) -> Color)

/** Reference sections, the same on the home screen and in the Learn tab. */
internal val learnTiles = listOf(
    Tile(HomeSection.RULES, "Правила", Icons.Rounded.Gavel, { it.teal }, { it.tealTint }),
    Tile(HomeSection.SIGNS, "Знаки", Icons.Rounded.Signpost, { it.red }, { it.redTint }),
    Tile(HomeSection.MARKINGS, "Розмітка", Icons.Rounded.AddRoad, { it.slate }, { it.slateTint }),
    Tile(HomeSection.EXPLAIN, "Пояснення", Icons.Rounded.Lightbulb, { it.purple }, { it.purpleTint }),
    Tile(HomeSection.CAR, "3D-машина", Icons.Rounded.DirectionsCar, { it.sand }, { it.sandTint }),
    Tile(HomeSection.SIGN_GAME, "Гра", Icons.Rounded.SportsEsports, { it.onAccentTint }, { it.accentTint }),
)

private val weekdays = listOf("ПОНЕДІЛОК", "ВІВТОРОК", "СЕРЕДА", "ЧЕТВЕР", "П’ЯТНИЦЯ", "СУБОТА", "НЕДІЛЯ")
private val months = listOf("СІЧНЯ", "ЛЮТОГО", "БЕРЕЗНЯ", "КВІТНЯ", "ТРАВНЯ", "ЧЕРВНЯ", "ЛИПНЯ", "СЕРПНЯ", "ВЕРЕСНЯ", "ЖОВТНЯ", "ЛИСТОПАДА", "ГРУДНЯ")

private fun greeting(hour: Int) = when (hour) {
    in 5..11 -> "Доброго ранку"
    in 12..17 -> "Доброго дня"
    in 18..22 -> "Доброго вечора"
    else -> "Доброї ночі"
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    onOpenReview: () -> Unit,
) {
    val c = ZnakyTheme.colors
    when (state) {
        HomeUiState.Loading -> LoadingBox()
        HomeUiState.ContentError -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Не вдалося завантажити питання. Перезапустіть застосунок.", style = ZnakyType.body, color = c.textSecondary, textAlign = TextAlign.Center)
        }
        is HomeUiState.Ready -> LazyColumn(
            Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "header") { Header(state.streak, onStreak = { onOpenSection(HomeSection.PROGRESS) }) }
            item(key = "readiness") {
                ReadinessCard(state, onDetails = { onOpenSection(HomeSection.PROGRESS) }, onExam = { onOpenSection(HomeSection.EXAM) })
            }
            item(key = "today") {
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlanCard(
                        state.nextTopic,
                        Modifier.weight(1f),
                        onClick = { state.nextTopic?.let { onOpenTopic(it.id) } ?: onOpenSection(HomeSection.TOPICS) },
                    )
                    ReviewCard(
                        state.dueForReview,
                        Modifier.weight(1f),
                        onClick = { if (state.dueForReview > 0) onOpenReview() else onOpenSection(HomeSection.TRAINING) },
                    )
                }
            }
            item(key = "goal") { DailyGoal(state.answersToday) }
            item(key = "learnTitle") {
                Text(
                    "Вчити",
                    style = ZnakyType.title2,
                    color = c.textPrimary,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp).semantics { heading() },
                )
            }
            item(key = "learn") { LearnGrid(onOpenSection) }
            item(key = "disclaimer") {
                Text(
                    "Незалежний навчальний застосунок. Не є офіційним продуктом МВС чи ГСЦ МВС.",
                    style = ZnakyType.footnote.copy(fontWeight = ZnakyType.subhead.fontWeight),
                    color = c.textTertiary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Header(streak: Int, onStreak: () -> Unit) {
    val c = ZnakyTheme.colors
    // The date is read once per composition of the screen, not every frame.
    val now = remember { LocalDateTime.now() }
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${weekdays[now.dayOfWeek.value - 1]}, ${now.dayOfMonth} ${months[now.monthValue - 1]}",
                style = ZnakyType.footnote,
                color = c.textSecondary,
            )
            Text(greeting(now.hour), style = ZnakyType.largeTitle, color = c.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Row(
            Modifier
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(50))
                .background(c.accentTint)
                .clickable(role = Role.Button, onClick = onStreak)
                .clearAndSetSemantics { contentDescription = "Серія $streak ${daysWord(streak)}" }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = c.onAccentTint, modifier = Modifier.size(18.dp))
            Text("$streak", style = ZnakyType.bodyStrong, color = c.onAccentTint)
        }
    }
}

@Composable
private fun ReadinessCard(state: HomeUiState.Ready, onDetails: () -> Unit, onExam: () -> Unit) {
    val c = ZnakyTheme.colors
    val r = state.readiness
    val ready = r.level == Level.READY
    val text = when {
        ready -> "Ви готові: понад $READY_THRESHOLD% і три пробні іспити поспіль складено."
        state.recentExams == 0 -> "Йти на іспит можна з $READY_THRESHOLD%. Складіть пробний іспит, щоб побачити, де ви зараз."
        else -> "Йти на іспит можна з $READY_THRESHOLD%. Зараз ви складаєте ${state.passedRecent} з ${state.recentExams} пробних."
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(c.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.clickable(role = Role.Button, onClickLabel = "Відкрити прогрес", onClick = onDetails),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ThresholdRing(r.score, ready)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Готовність до іспиту", style = ZnakyType.headline, color = c.textPrimary)
                Text(text, style = ZnakyType.subhead, color = c.textSecondary)
            }
        }
        ZButton("Пробний іспит · 20 хв", onClick = onExam, modifier = Modifier.fillMaxWidth())
    }
}

/** Readiness ring with a small green dot where the 85% threshold is. */
@Composable
private fun ThresholdRing(score: Int, ready: Boolean) {
    val c = ZnakyTheme.colors
    val marker = c.success
    ProgressRing(
        score / 100f,
        modifier = Modifier.semantics { contentDescription = "Готовність $score відсотків, поріг $READY_THRESHOLD" },
        size = 72.dp,
        stroke = 9.dp,
        color = if (ready) c.success else c.primary,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = 9.dp.toPx()
            val radius = (size.minDimension - stroke) / 2
            val angle = (-90f + 360f * READY_THRESHOLD / 100f) * PI.toFloat() / 180f
            drawCircle(
                marker,
                radius = 3.5.dp.toPx(),
                center = Offset(center.x + radius * cos(angle), center.y + radius * sin(angle)),
            )
        }
        Text("$score%", style = ZnakyType.headline.copy(fontSize = 18.sp), color = c.textPrimary)
    }
}

@Composable
private fun PlanCard(topic: PlanTopic?, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .fillMaxHeight()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("ДАЛІ ЗА ПЛАНОМ", style = ZnakyType.caption, color = c.textTertiary)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                topic?.title ?: "Усі теми пройдено",
                style = ZnakyType.bodyStrong,
                color = c.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            LinearBar(topic?.completion ?: 1f, height = 5.dp, color = c.teal)
        }
    }
}

@Composable
private fun ReviewCard(due: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .fillMaxHeight()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.redTint)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("ПОВТОРИТИ СЬОГОДНІ", style = ZnakyType.caption, color = c.red)
        Spacer(Modifier.height(10.dp))
        Column {
            Text("$due", style = ZnakyType.largeTitle.copy(fontSize = 28.sp, lineHeight = 30.sp), color = c.red)
            Text(
                if (due > 0) "${questionsWord(due)} з помилками" else "помилок на сьогодні немає",
                style = ZnakyType.subhead,
                color = c.red,
            )
        }
    }
}

@Composable
private fun DailyGoal(answers: Int) {
    val c = ZnakyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ціль дня", style = ZnakyType.bodyStrong, color = c.textPrimary)
        LinearBar(answers / DAILY_GOAL.toFloat(), Modifier.weight(1f), height = 8.dp, color = if (answers >= DAILY_GOAL) c.success else c.primary)
        Text(
            "${answers.coerceAtMost(999)}/$DAILY_GOAL",
            style = ZnakyType.bodyStrong.copy(fontFeatureSettings = "tnum"),
            color = c.textSecondary,
        )
    }
}

@Composable
internal fun LearnGrid(onOpen: (HomeSection) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        learnTiles.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile -> LearnTile(tile, Modifier.weight(1f)) { onOpen(tile.section) } }
            }
        }
    }
}

@Composable
private fun LearnTile(tile: Tile, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .heightIn(min = 84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(tile.bg(c)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tile.icon, contentDescription = null, tint = tile.fg(c), modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(tile.label, style = ZnakyType.footnote.copy(fontSize = 14.sp), color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

internal fun questionsWord(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "питання"
        mod10 in 2..4 && mod100 !in 12..14 -> "питання"
        else -> "питань"
    }
}

internal fun daysWord(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "день"
        mod10 in 2..4 && mod100 !in 12..14 -> "дні"
        else -> "днів"
    }
}
