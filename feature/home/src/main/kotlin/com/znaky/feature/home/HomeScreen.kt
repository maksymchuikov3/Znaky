package com.znaky.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddRoad
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Signpost
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ProgressRing
import com.znaky.core.designsystem.component.SectionTitle
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyColors
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.readiness.ReadinessCalculator.Level
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** The eight sections of the app, shown as tiles on the home screen. */
enum class HomeSection { TESTS, EXAM, RULES, SIGNS, MARKINGS, EXPLAIN, CAR, PROGRESS }

fun NavGraphBuilder.homeScreen(
    onOpenSection: (HomeSection) -> Unit,
    onOpenReview: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HomeScreen(state, onOpenSection, onOpenReview)
    }
}

@Immutable
private data class Tile(val section: HomeSection, val label: String, val icon: ImageVector, val color: (ZnakyColors) -> Color, val tint: (ZnakyColors) -> Color)

private val tiles = listOf(
    Tile(HomeSection.TESTS, "Тести", Icons.Rounded.Quiz, { it.primary }, { it.primaryTint }),
    Tile(HomeSection.EXAM, "Іспит", Icons.Rounded.Timer, { it.onAccentTint }, { it.accentTint }),
    Tile(HomeSection.RULES, "Правила", Icons.Rounded.Gavel, { it.teal }, { it.tealTint }),
    Tile(HomeSection.SIGNS, "Знаки", Icons.Rounded.Signpost, { it.red }, { it.redTint }),
    Tile(HomeSection.MARKINGS, "Розмітка", Icons.Rounded.AddRoad, { it.textSecondary }, { it.surfaceMuted }),
    Tile(HomeSection.EXPLAIN, "Пояснення", Icons.Rounded.Lightbulb, { it.purple }, { it.purpleTint }),
    Tile(HomeSection.CAR, "3D-машина", Icons.Rounded.DirectionsCar, { it.orange }, { it.accentTint }),
    Tile(HomeSection.PROGRESS, "Прогрес", Icons.Rounded.BarChart, { it.success }, { it.successTint }),
)

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onOpenSection: (HomeSection) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "header") {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ПІДГОТОВКА ДО ІСПИТУ", style = ZnakyType.caption, color = c.textTertiary)
                    Text("Привіт!", style = ZnakyType.largeTitle, color = c.textPrimary)
                }
            }
            item(key = "readiness") { ReadinessCard(state, onClick = { onOpenSection(HomeSection.PROGRESS) }) }
            item(key = "continue") { ContinueCard(state.dueForReview, onOpenReview, onTopics = { onOpenSection(HomeSection.TESTS) }) }
            item(key = "sectionsTitle") { SectionTitle("Розділи", Modifier.padding(top = 6.dp)) }
            item(key = "sections") { SectionsGrid(onOpenSection) }
            item(key = "cta") {
                ZButton(
                    "Пробний іспит · 20 хв",
                    onClick = { onOpenSection(HomeSection.EXAM) },
                    icon = Icons.Rounded.Timer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            item(key = "disclaimer") {
                Text(
                    "Незалежний навчальний застосунок. Не є офіційним продуктом МВС чи ГСЦ МВС.",
                    style = ZnakyType.footnote.copy(fontWeight = ZnakyType.subhead.fontWeight),
                    color = c.textTertiary,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun ReadinessCard(state: HomeUiState.Ready, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    val r = state.readiness
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(r.score / 100f, size = 84.dp, stroke = 10.dp, color = if (r.level == Level.READY) c.success else c.primary) {
                Text("${r.score}%", style = ZnakyType.title2, color = c.textPrimary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Готовність до іспиту", style = ZnakyType.headline, color = c.textPrimary)
                Text(levelText(r.level), style = ZnakyType.subhead, color = c.textSecondary)
                Text("Пробних іспитів: ${r.examsTaken} · вивчено ${(r.topicCoverage * 100).toInt()}%", style = ZnakyType.footnote, color = c.textTertiary)
            }
        }
    }
}

private fun levelText(level: Level) = when (level) {
    Level.TOO_EARLY -> "Пройдіть щонайменше 3 пробні іспити, щоб побачити оцінку"
    Level.GETTING_THERE -> "Рухаєтеся в правильному напрямку"
    Level.ALMOST -> "Майже готові, підтягніть слабкі теми"
    Level.READY -> "Готові до іспиту в сервісному центрі"
}

@Composable
private fun ContinueCard(due: Int, onReview: () -> Unit, onTopics: () -> Unit) {
    val c = ZnakyTheme.colors
    val hasReview = due > 0
    ZCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), onClick = if (hasReview) onReview else onTopics, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(if (hasReview) c.accentTint else c.primaryTint), contentAlignment = Alignment.Center) {
                Icon(if (hasReview) Icons.Rounded.Replay else Icons.Rounded.Quiz, contentDescription = null, tint = if (hasReview) c.onAccentTint else c.link)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("ПРОДОВЖИТИ", style = ZnakyType.caption, color = c.textTertiary)
                Text(if (hasReview) "Повторити помилки" else "Тести за темами", style = ZnakyType.headline, color = c.textPrimary)
                Text(if (hasReview) "$due ${questionsWord(due)} на сьогодні" else "Оберіть тему і тренуйтеся", style = ZnakyType.footnote, color = c.textTertiary)
            }
        }
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

@Composable
private fun SectionsGrid(onOpen: (HomeSection) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { tile -> SectionTile(tile, Modifier.weight(1f)) { onOpen(tile.section) } }
            }
        }
    }
}

@Composable
private fun SectionTile(tile: Tile, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.size(64.dp).aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(tile.tint(c)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(tile.icon, contentDescription = null, tint = tile.color(c), modifier = Modifier.size(30.dp))
        }
        Text(tile.label, style = ZnakyType.footnote, color = c.textPrimary, maxLines = 1, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
    }
}
