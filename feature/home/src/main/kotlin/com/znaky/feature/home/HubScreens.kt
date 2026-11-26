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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Traffic
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.art.Officer
import com.znaky.core.art.RoadSigns
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.ProgressRepository
import com.znaky.core.domain.repository.QuestionRepository
import com.znaky.core.domain.repository.SettingsRepository
import com.znaky.core.model.ExamBlock
import com.znaky.core.model.LicenseCategory
import com.znaky.core.model.MOTO_TOPIC_PREFIX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import javax.inject.Inject

/** The "Вчити" tab: the four exam blocks, reference tiles and the rules route. */
@Serializable
data object LearnHubRoute

/** The "Тренування" tab: mock exam, random questions, repetition, mistakes, bookmarks. */
@Serializable
data object TrainingRoute

@Immutable
data class LearnState(val route: List<PlanTopic>, val blocks: Map<ExamBlock, Float>, val category: LicenseCategory)

@Immutable
data class TrainingState(val bestExam: Int?, val due: Int, val mistakes: Int, val bookmarks: Int, val topics: Int, val ruleSections: Int)

@HiltViewModel
class HubViewModel @Inject constructor(
    questions: QuestionRepository,
    progress: ProgressRepository,
    settings: SettingsRepository,
) : ViewModel() {
    val learn: StateFlow<LearnState?> = combine(
        questions.observeTopics(),
        progress.observeTopicProgress(),
        settings.licenseCategory,
    ) { topics, topicProgress, category ->
        val route = studyRoute(topics, topicProgress)
        LearnState(route, blockCompletion(route), category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val training: StateFlow<TrainingState?> = combine(
        progress.observeExamResults(),
        progress.observeDueForReview(),
        progress.observeMistakes(),
        progress.observeBookmarks(),
        questions.observeTopics(),
    ) { exams, due, mistakes, bookmarks, topics ->
        TrainingState(
            bestExam = exams.maxOfOrNull { it.correctAnswers },
            due = due.size,
            mistakes = mistakes.size,
            bookmarks = bookmarks.size,
            topics = topics.size,
            ruleSections = topics.count { it.block == ExamBlock.TRAFFIC_RULES },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

fun NavGraphBuilder.hubScreens(
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenReview: () -> Unit,
) {
    composable<LearnHubRoute> {
        val viewModel: HubViewModel = hiltViewModel()
        val state by viewModel.learn.collectAsStateWithLifecycle()
        LearnHubScreen(state, onOpenSection, onOpenTopic)
    }
    composable<TrainingRoute> {
        val viewModel: HubViewModel = hiltViewModel()
        val state by viewModel.training.collectAsStateWithLifecycle()
        TrainingScreen(state, onOpenSection)
    }
}

private class BlockStyle(val icon: ImageVector, val fg: (com.znaky.core.designsystem.theme.ZnakyColors) -> Color)

private fun ExamBlock.style() = when (this) {
    ExamBlock.TRAFFIC_RULES -> BlockStyle(Icons.Rounded.Traffic) { it.onAccentTint }
    ExamBlock.ROAD_SAFETY -> BlockStyle(Icons.Rounded.Shield) { it.link }
    ExamBlock.VEHICLE -> BlockStyle(Icons.Rounded.Settings) { it.link }
    ExamBlock.FIRST_AID -> BlockStyle(Icons.Rounded.MedicalServices) { it.success }
}

/** Sign art for rule sections that have one; others show the section number only. */
private val SectionSigns = mapOf("12" to "3.29", "14" to "3.25", "15" to "3.34", "16" to "2.3", "18" to "1.22", "33" to "2.1", "8" to "4.1", "2" to "5.1")

@Composable
private fun LearnHubScreen(state: LearnState?, onOpenSection: (HomeSection) -> Unit, onOpenTopic: (String) -> Unit) {
    val s = state ?: return LoadingBox()
    if (s.category.isWater) return BoatLearnHub(s, onOpenSection, onOpenTopic)
    val c = ZnakyTheme.colors
    val rules = s.route.filter { it.block == ExamBlock.TRAFFIC_RULES && !it.id.startsWith(MOTO_TOPIC_PREFIX) }
    val here = rules.indexOfFirst { it.completion < LEARNED }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            LargeTitle("Вчити", Modifier.padding(top = 16.dp), subtitle = "Уся офіційна програма теоретичного іспиту · ${s.category.name}")
        }
        if (s.category.isMoto) item(key = "course") {
            CourseCard(ExtraCourse.MOTO, onOpenSection, Modifier.padding(horizontal = 16.dp))
        }
        item(key = "blocks") {
            val blocks = listOf(ExamBlock.TRAFFIC_RULES, ExamBlock.ROAD_SAFETY, ExamBlock.VEHICLE, ExamBlock.FIRST_AID)
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                blocks.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { block ->
                            BlockCard(block, s.blocks[block] ?: 0f, Modifier.weight(1f)) {
                                when (block) {
                                    ExamBlock.TRAFFIC_RULES -> onOpenSection(HomeSection.RULES)
                                    ExamBlock.VEHICLE -> onOpenSection(HomeSection.ANATOMY)
                                    else -> s.route.firstOrNull { it.block == block }?.let { onOpenTopic(it.id) } ?: onOpenSection(HomeSection.TOPICS)
                                }
                            }
                        }
                    }
                }
            }
        }
        item(key = "tiles") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RefTile("Знаки", Modifier.weight(1f), { onOpenSection(HomeSection.SIGNS) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf("2.1", "2.2", "3.29").forEach { code -> ArtImage(null, Modifier.size(26.dp)) { RoadSigns.draw(this, code) } }
                    }
                }
                RefTile("Регулювальник", Modifier.weight(1f), { onOpenSection(HomeSection.OFFICER) }) {
                    ArtImage(null, Modifier.size(28.dp, 40.dp)) { Officer.draw(this, armsOut = true, t = 0f) }
                }
                RefTile("Словник", Modifier.weight(1f), { onOpenSection(HomeSection.GLOSSARY) }) {
                    Text("Aa", style = ZnakyType.title.copy(fontSize = 26.sp), color = c.link)
                }
            }
        }
        item(key = "rulesTitle") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ПРАВИЛА · ${rules.size} ${plural(rules.size, "РОЗДІЛ", "РОЗДІЛИ", "РОЗДІЛІВ")} ПДР",
                    style = ZnakyType.footnote,
                    color = c.textTertiary,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text("Усі ›", style = ZnakyType.footnote, color = c.link, modifier = Modifier.clickable(role = Role.Button) { onOpenSection(HomeSection.RULES) })
            }
        }
        item(key = "rules") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(c.surface)) {
                rules.forEachIndexed { i, topic ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), thickness = 1.dp, color = c.separator)
                    RuleRow(topic, isHere = i == here, onClick = { onOpenTopic(topic.id) })
                }
            }
        }
    }
}

/** «Вчити» for the boat course: the course card, its topics and the glossary. */
@Composable
private fun BoatLearnHub(s: LearnState, onOpenSection: (HomeSection) -> Unit, onOpenTopic: (String) -> Unit) {
    val c = ZnakyTheme.colors
    val topics = s.route
    val here = topics.indexOfFirst { it.completion < LEARNED }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            LargeTitle("Вчити", Modifier.padding(top = 16.dp), subtitle = "Судноводій малого судна і гідроцикла")
        }
        item(key = "course") { CourseCard(ExtraCourse.BOAT, onOpenSection, Modifier.padding(horizontal = 16.dp)) }
        item(key = "tiles") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RefTile("Словник", Modifier.weight(1f), { onOpenSection(HomeSection.GLOSSARY) }) {
                    Text("Aa", style = ZnakyType.title.copy(fontSize = 26.sp), color = c.link)
                }
                RefTile("20 випадкових", Modifier.weight(1f), { onOpenSection(HomeSection.RANDOM) }) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = c.link, modifier = Modifier.size(28.dp))
                }
                RefTile("Мої помилки", Modifier.weight(1f), { onOpenSection(HomeSection.MISTAKES) }) {
                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = c.red, modifier = Modifier.size(28.dp))
                }
            }
        }
        item(key = "topicsTitle") {
            Text(
                "ТЕМИ · ${topics.size} ${plural(topics.size, "ТЕМА", "ТЕМИ", "ТЕМ")}",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).semantics { heading() },
            )
        }
        item(key = "topics") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(c.surface)) {
                topics.forEachIndexed { i, topic ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), thickness = 1.dp, color = c.separator)
                    RuleRow(topic, isHere = i == here, onClick = { onOpenTopic(topic.id) }, showSign = false)
                }
            }
        }
    }
}

@Composable
private fun BlockCard(block: ExamBlock, completion: Float, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    val style = block.style()
    val fg = style.fg(c)
    Column(
        modifier
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(style.icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Text(" ${block.examShare()} з 20 на іспиті", style = ZnakyType.caption, color = fg, maxLines = 1)
        }
        Text(block.label(), style = ZnakyType.bodyStrong, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        LinearBar(completion, Modifier.fillMaxWidth(), height = 6.dp, color = fg)
    }
}

@Composable
private fun RefTile(label: String, modifier: Modifier, onClick: () -> Unit, art: @Composable () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .height(92.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.height(40.dp), contentAlignment = Alignment.Center) { art() }
        Text(label, style = ZnakyType.footnote, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RuleRow(topic: PlanTopic, isHere: Boolean, onClick: () -> Unit, showSign: Boolean = true) {
    val c = ZnakyTheme.colors
    val learned = topic.completion >= LEARNED
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isHere) c.accentTint else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 54.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(topic.number, style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.width(22.dp))
        val sign = if (showSign) SectionSigns[topic.number] else null
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            if (sign != null && RoadSigns.has(sign)) ArtImage(null, Modifier.size(30.dp)) { RoadSigns.draw(this, sign) }
            else Box(Modifier.size(26.dp).clip(CircleShape).background(c.surfaceMuted))
        }
        Column(Modifier.weight(1f)) {
            Text(topic.title, style = ZnakyType.bodyStrong, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (isHere) {
                val done = (topic.completion * topic.questions).toInt()
                Text("ТИ ТУТ · $done з ${topic.questions}", style = ZnakyType.caption, color = c.onAccentTint)
            }
        }
        if (learned) Icon(Icons.Rounded.Check, contentDescription = "Вивчено", tint = c.success, modifier = Modifier.size(20.dp))
        else Text("${(topic.completion * 100).toInt()}%", style = ZnakyType.footnote, color = c.textSecondary)
    }
}

@Composable
private fun TrainingScreen(state: TrainingState?, onOpenSection: (HomeSection) -> Unit) {
    val s = state ?: return LoadingBox()
    val c = ZnakyTheme.colors
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") { LargeTitle("Тренування", Modifier.padding(top = 16.dp), subtitle = "Обери режим — усі питання з офіційної бази") }
        item(key = "exam") { ExamHero(s.bestExam, onOpen = { onOpenSection(HomeSection.EXAM_INTRO) }, onStart = { onOpenSection(HomeSection.EXAM) }) }
        item(key = "modes") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard(
                    icon = Icons.Rounded.Shuffle,
                    iconBg = c.accentTint,
                    iconFg = c.onAccentTint,
                    title = "20 випадкових",
                    subtitle = "без таймера, з поясненнями",
                    accent = null,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenSection(HomeSection.RANDOM) },
                )
                ModeCard(
                    icon = Icons.Rounded.Sync,
                    iconBg = c.successTint,
                    iconFg = c.success,
                    title = "Повторення",
                    subtitle = if (s.due > 0) "на сьогодні" else "на сьогодні все",
                    accent = if (s.due > 0) "${s.due}" else null,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenSection(HomeSection.REPETITION) },
                )
            }
        }
        item(key = "ownTitle") {
            Text("СВОЄ", style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.padding(horizontal = 22.dp, vertical = 2.dp).semantics { heading() })
        }
        item(key = "own") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(20.dp)).background(c.surface)) {
                OwnRow(
                    Icons.Rounded.Layers, c.primaryTint, c.link, "За темами",
                    "${s.ruleSections} ${plural(s.ruleSections, "розділ", "розділи", "розділів")} ПДР + 3 блоки",
                    null, { onOpenSection(HomeSection.TOPICS) },
                )
                HorizontalDivider(Modifier.padding(start = 64.dp), thickness = 1.dp, color = c.separator)
                OwnRow(
                    Icons.Rounded.WarningAmber, c.redTint, c.red, "Мої помилки", "питання, де ти помилився",
                    s.mistakes.takeIf { it > 0 }?.let { Triple(it, c.redTint, c.red) }, { onOpenSection(HomeSection.MISTAKES) },
                )
                HorizontalDivider(Modifier.padding(start = 64.dp), thickness = 1.dp, color = c.separator)
                OwnRow(
                    Icons.Rounded.Bookmark, c.accentTint, c.accent, "Закладки", "збережені, щоб повернутись",
                    s.bookmarks.takeIf { it > 0 }?.let { Triple(it, c.accentTint, c.onAccentTint) }, { onOpenSection(HomeSection.BOOKMARKS) },
                )
            }
        }
    }
}

@Composable
private fun ExamHero(best: Int?, onOpen: () -> Unit, onStart: () -> Unit) {
    val c = ZnakyTheme.colors
    val top = if (c.isDark) Color(0xFF2D4F99) else Color(0xFF3D73E0)
    val bottom = if (c.isDark) Color(0xFF223D7A) else Color(0xFF2F62C9)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ЯК У ТСЦ", style = ZnakyType.caption, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            if (best != null) {
                Text(
                    "Найкращий: $best/20",
                    style = ZnakyType.caption,
                    color = Color.White,
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Text("Пробний іспит", style = ZnakyType.title, color = Color.White)
        Text("20 питань · 20 хв · до 2 помилок · 10/4/4/2 за блоками", style = ZnakyType.subhead, color = Color.White.copy(alpha = 0.85f))
        Row(
            Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onStart),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF2F62C9), modifier = Modifier.size(20.dp))
            Text(" Почати іспит", style = ZnakyType.button, color = Color(0xFF2F62C9))
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    iconBg: Color,
    iconFg: Color,
    title: String,
    subtitle: String,
    accent: String?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val c = ZnakyTheme.colors
    Column(
        modifier
            .heightIn(min = 128.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(22.dp))
        }
        Text(title, style = ZnakyType.headline, color = c.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (accent != null) Text("$accent ", style = ZnakyType.footnote, color = c.success)
            Text(subtitle, style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal), color = c.textSecondary)
        }
    }
}

@Composable
private fun OwnRow(
    icon: ImageVector,
    iconBg: Color,
    iconFg: Color,
    title: String,
    subtitle: String,
    badge: Triple<Int, Color, Color>?,
    onClick: () -> Unit,
) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = ZnakyType.bodyStrong, color = c.textPrimary)
            Text(subtitle, style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal), color = c.textTertiary)
        }
        if (badge != null) {
            Box(Modifier.clip(CircleShape).background(badge.second).padding(horizontal = 10.dp, vertical = 3.dp)) {
                Text("${badge.first}", style = ZnakyType.footnote, color = badge.third)
            }
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = c.textTertiary)
    }
}
