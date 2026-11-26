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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.model.ExamBlock
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** Places the home, learn and training screens can send the user to. */
enum class HomeSection {
    RULES, SIGNS, MARKINGS, EXPLAIN, CAR, SIGN_GAME,
    /** Starts a mock exam right away. */
    EXAM,
    /** The mock exam rules screen with a start button. */
    EXAM_INTRO,
    TOPICS, PROGRESS, LEARN, TRAINING,
    /** «Повторення» with the spaced-repetition track. */
    REPETITION,
    /** Practice the questions whose latest answer was wrong. */
    MISTAKES,
    BOOKMARKS,
    /** 20 random questions without a timer. */
    RANDOM,
    /** «Будова авто»: 3D model with numbered parts. */
    ANATOMY,
    GLOSSARY,
    OFFICER,
    /** Theory courses outside the car programme and their 3D models. */
    MOTO_COURSE,
    BOAT_COURSE,
    ANATOMY_MOTO,
    ANATOMY_BOAT,
}

fun NavGraphBuilder.homeScreen(
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenReview: () -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LifecycleResumeEffect(Unit) {
            viewModel.refresh()
            onPauseOrDispose { }
        }
        HomeScreen(state, onOpenSection, onOpenTopic)
    }
}

internal fun questionsWord(n: Int): String = plural(n, "питання", "питання", "питань")

internal fun mistakesWord(n: Int): String = plural(n, "помилка", "помилки", "помилок")

internal fun daysWord(n: Int): String = plural(n, "день", "дні", "днів")

/** Ukrainian plural: [one] for 1, 21…; [few] for 2–4, 22…; [many] otherwise. */
internal fun plural(n: Int, one: String, few: String, many: String): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

internal fun chaptersWord(n: Int): String = plural(n, "розділу", "розділів", "розділів")

/**
 * The home screen is the book: progress, the chapter to do now (read, then answer), a short
 * stretch of the contents around it, saved questions and the mock exam. Nothing else.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
) {
    val c = ZnakyTheme.colors
    when (state) {
        HomeUiState.Loading -> LoadingBox()
        HomeUiState.ContentError -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Не вдалося завантажити питання. Перезапустіть застосунок.", style = ZnakyType.body, color = c.textSecondary, textAlign = TextAlign.Center)
        }
        is HomeUiState.Ready -> {
            var showAll by rememberSaveable { mutableStateOf(false) }
            val current = state.chapters.getOrNull(state.current)
            LazyColumn(
                Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") { Header(state, onStreak = { onOpenSection(HomeSection.PROGRESS) }) }
                item(key = "current") {
                    if (current != null) CurrentChapter(current, state.current + 1, state.chapters.size, onOpenTopic)
                    else BookDone(onExam = { onOpenSection(HomeSection.EXAM_INTRO) })
                }
                item(key = "tocTitle") {
                    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 20.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "ЗМІСТ" + (current?.part?.let { " · ${it.uppercase()}" } ?: ""),
                            style = ZnakyType.caption,
                            color = c.textTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).semantics { heading() },
                        )
                        Text(
                            if (showAll) "Згорнути" else "Увесь зміст ›",
                            style = ZnakyType.footnote,
                            color = c.link,
                            modifier = Modifier.clickable(role = Role.Button) { showAll = !showAll }.padding(vertical = 6.dp),
                        )
                    }
                }
                item(key = "toc") { Contents(state, showAll, onOpenTopic) }
                item(key = "saved") {
                    LinkRow(
                        icon = Icons.Rounded.Bookmark,
                        tint = c.red,
                        tintBg = c.redTint,
                        title = "Збережене",
                        subtitle = if (state.saved > 0) "${state.saved} ${questionsWord(state.saved)}: ти їх зберіг або помилився"
                        else "Тут будуть питання, які ти збережеш або в яких помилишся",
                        onClick = { onOpenSection(HomeSection.REPETITION) },
                    )
                }
                item(key = "exam") {
                    LinkRow(
                        icon = Icons.Rounded.Timer,
                        tint = c.onAccentTint,
                        tintBg = c.accentTint,
                        title = "Пробний іспит",
                        subtitle = if (state.category.isWater) "20 питань для судноводія" else "20 питань · 20 хв, як у ТСЦ",
                        onClick = { onOpenSection(HomeSection.EXAM_INTRO) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(state: HomeUiState.Ready, onStreak: () -> Unit) {
    val c = ZnakyTheme.colors
    val total = state.chapters.size
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Мій підручник", style = ZnakyType.largeTitle, color = c.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
            StreakPill(state.streak, onStreak)
        }
        Text("Пройдено ${state.doneCount} з $total ${chaptersWord(total)}", style = ZnakyType.callout, color = c.textSecondary)
        LinearBar(if (total == 0) 0f else state.doneCount / total.toFloat(), Modifier.padding(end = 4.dp, top = 2.dp), color = c.success)
    }
}

private fun BookChapter.kicker(index: Int, total: Int) = "РОЗДІЛ $num · ${part.uppercase()}".takeIf { num.isNotBlank() } ?: "КРОК $index З $total"

@Composable
private fun CurrentChapter(ch: BookChapter, index: Int, total: Int, onOpen: (String) -> Unit) {
    val c = ZnakyTheme.colors
    val readStep = ch.readTarget != null
    val reading = readStep && !ch.read
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(c.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ch.kicker(index, total), style = ZnakyType.caption, color = c.link, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ch.title, style = ZnakyType.title, color = c.textPrimary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var n = 1
            if (readStep) Step(n++, "Прочитати", "текст і схеми", done = ch.read, now = reading, Modifier.weight(1f))
            if (ch.hasQuestions) Step(n, "Питання", "${ch.questions} ${questionsWord(ch.questions)}", done = ch.questionsDone, now = !reading, Modifier.weight(1f))
        }
        when {
            reading -> ZButton("Читати розділ", onClick = { onOpen(ch.readTarget!!) }, icon = Icons.AutoMirrored.Rounded.MenuBook, modifier = Modifier.fillMaxWidth())
            ch.hasQuestions -> ZButton("Відповісти на питання", onClick = { onOpen(ch.topicId!!) }, icon = Icons.Rounded.Quiz, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Step(n: Int, title: String, subtitle: String, done: Boolean, now: Boolean, modifier: Modifier) {
    val c = ZnakyTheme.colors
    val (bg, fg) = when {
        done -> c.successTint to c.success
        now -> c.primaryTint to c.link
        else -> c.surfaceMuted to c.textPrimary
    }
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clearAndSetSemantics { contentDescription = "Крок $n: $title, $subtitle" + if (done) ", зроблено" else "" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp).padding(end = 2.dp))
            Text(if (done) title else "$n · $title", style = ZnakyType.headline, color = fg, maxLines = 1)
        }
        Text(subtitle, style = ZnakyType.subhead, color = c.textSecondary, maxLines = 1)
    }
}

@Composable
private fun BookDone(onExam: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(26.dp)).background(c.surface).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = c.accent, modifier = Modifier.size(36.dp))
        Text("Підручник пройдено!", style = ZnakyType.title, color = c.textPrimary)
        Text("Ти прочитав усі розділи і відповів на питання. Перевір себе на пробному іспиті.", style = ZnakyType.callout, color = c.textSecondary)
        ZButton("Пробний іспит", onClick = onExam, icon = Icons.Rounded.Timer, modifier = Modifier.fillMaxWidth())
    }
}

/** A short stretch of the contents around the current chapter, or all of it grouped by part. */
@Composable
private fun Contents(state: HomeUiState.Ready, showAll: Boolean, onOpen: (String) -> Unit) {
    val c = ZnakyTheme.colors
    val chapters = state.chapters
    val cur = if (state.current < 0) chapters.lastIndex else state.current
    val shown = if (showAll) chapters.indices.toList() else (cur - 1..cur + 1).filter { it in chapters.indices }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(22.dp)).background(c.surface).padding(vertical = 6.dp)) {
        var part: String? = null
        shown.forEachIndexed { k, i ->
            val ch = chapters[i]
            if (showAll && ch.part != part) {
                part = ch.part
                Text(ch.part.uppercase(), style = ZnakyType.caption, color = c.textTertiary, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp))
            }
            val target = when {
                ch.readTarget != null && !ch.read -> ch.readTarget
                ch.hasQuestions -> ch.topicId
                else -> ch.readTarget
            }
            ContentsRow(ch, isCurrent = i == state.current, line = k > 0, onClick = target?.let { t -> { onOpen(t) } })
        }
    }
}

@Composable
private fun ContentsRow(ch: BookChapter, isCurrent: Boolean, line: Boolean, onClick: (() -> Unit)?) {
    val c = ZnakyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = 52.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
            if (line) Box(Modifier.align(Alignment.TopCenter).width(2.dp).height(6.dp).background(c.separator))
            val (bg, fg) = when {
                ch.done -> c.successTint to c.success
                isCurrent -> c.primary to c.onPrimary
                else -> c.surfaceMuted to c.textTertiary
            }
            Box(Modifier.size(30.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                if (ch.done) Icon(Icons.Rounded.Check, contentDescription = "Пройдено", tint = fg, modifier = Modifier.size(16.dp))
                else Text(ch.num, style = ZnakyType.footnote, color = fg, maxLines = 1)
            }
        }
        Text(
            ch.title,
            style = if (isCurrent) ZnakyType.bodyStrong else ZnakyType.callout,
            color = when {
                isCurrent -> c.textPrimary
                ch.done -> c.textSecondary
                else -> c.textTertiary
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isCurrent) {
            Text(
                "ти тут",
                style = ZnakyType.caption,
                color = c.onPrimaryTint,
                modifier = Modifier.clip(CircleShape).background(c.primaryTint).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, tint: Color, tintBg: Color, title: String, subtitle: String, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(tintBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = ZnakyType.headline, color = c.textPrimary)
            Text(subtitle, style = ZnakyType.subhead, color = c.textSecondary)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = c.textTertiary)
    }
}

@Composable
internal fun StreakPill(streak: Int, onClick: (() -> Unit)? = null) {
    val c = ZnakyTheme.colors
    Row(
        Modifier
            .clip(CircleShape)
            .background(c.accentTint)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = "Серія: $streak ${daysWord(streak)} поспіль" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = c.accent, modifier = Modifier.size(16.dp))
        Text(" $streak ${daysWord(streak)}", style = ZnakyType.footnote, color = c.onAccentTint)
    }
}

internal fun ExamBlock.label() = when (this) {
    ExamBlock.TRAFFIC_RULES -> "Правила дорожнього руху"
    ExamBlock.ROAD_SAFETY -> "Основи безпеки руху"
    ExamBlock.VEHICLE -> "Будова й експлуатація авто"
    ExamBlock.FIRST_AID -> "Домедична допомога"
}
