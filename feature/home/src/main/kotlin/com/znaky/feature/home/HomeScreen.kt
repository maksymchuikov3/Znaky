package com.znaky.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Traffic
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.art.ArtPath
import com.znaky.core.art.Street
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.readiness.ReadinessCalculator.Level
import com.znaky.core.model.ExamBlock
import kotlinx.serialization.Serializable
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

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onOpenSection: (HomeSection) -> Unit,
    onOpenTopic: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenReview: () -> Unit,
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
            item(key = "header") { Header(state.streak, onStreak = { onOpenSection(HomeSection.PROGRESS) }) }
            item(key = "road") { RoadCard(state, onClick = { onOpenSection(HomeSection.PROGRESS) }) }
            item(key = "next") {
                NextStopCard(
                    state.nextTopic,
                    onGo = { topic -> if (topic != null) onOpenTopic(topic.id) else onOpenSection(HomeSection.EXAM_INTRO) },
                )
            }
            item(key = "chips") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Chip(
                        icon = Icons.Rounded.WarningAmber,
                        title = if (state.mistakes > 0) "${state.mistakes} ${mistakesWord(state.mistakes)}" else "Помилок немає",
                        subtitle = if (state.dueForReview > 0) "повторити ${state.dueForReview}" else "повторення",
                        bg = c.redTint,
                        fg = c.red,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSection(HomeSection.REPETITION) },
                    )
                    Chip(
                        icon = Icons.Rounded.Timer,
                        title = "Іспит як у ТСЦ",
                        subtitle = "20 питань · 20 хв",
                        bg = c.primaryTint,
                        fg = c.link,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenSection(HomeSection.EXAM_INTRO) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(streak: Int, onStreak: () -> Unit) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Шлях до ТСЦ", style = ZnakyType.largeTitle, color = c.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
        StreakPill(streak, onStreak)
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

private class Stop(val label: String, val icon: ImageVector, val done: Boolean)

private fun stops(state: HomeUiState.Ready): List<Stop> {
    val b = state.blocks
    fun learned(block: ExamBlock) = (b[block] ?: 0f) >= LEARNED
    return listOf(
        Stop("Допомога", Icons.Rounded.MedicalServices, learned(ExamBlock.FIRST_AID)),
        Stop("Правила", Icons.Rounded.Traffic, learned(ExamBlock.TRAFFIC_RULES)),
        Stop("Безпека", Icons.Rounded.Shield, learned(ExamBlock.ROAD_SAFETY)),
        Stop("Будова", Icons.Rounded.Settings, learned(ExamBlock.VEHICLE)),
        Stop("Пробні", Icons.Rounded.Timer, state.passedRecent >= 3),
        Stop("ТСЦ", Icons.Rounded.Flag, state.readiness.level == Level.READY),
    )
}

@Composable
private fun RoadCard(state: HomeUiState.Ready, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    val score = state.readiness.score
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Speedometer(score / 100f, Modifier.size(width = 112.dp, height = 76.dp))
            Column(Modifier.weight(1f)) {
                Text("$score%", style = ZnakyType.largeTitle.copy(fontSize = 34.sp), color = c.textPrimary)
                Text("готовність до іспиту", style = ZnakyType.subhead, color = c.textSecondary)
                Text(
                    if (state.recentExams == 0) "мета: 3 пробні поспіль без >2 помилок"
                    else "пробні: ${state.passedRecent} з ${state.recentExams} складено · мета 3 поспіль",
                    style = ZnakyType.subhead,
                    color = c.textTertiary,
                )
            }
        }
        StopsRoad(stops(state))
    }
}

/** A dashboard gauge: grey dial, green arc up to [value], needle and hub. */
@Composable
private fun Speedometer(value: Float, modifier: Modifier) {
    val c = ZnakyTheme.colors
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), tween(900), label = "gauge")
    val track = c.surfaceMuted
    val arc = if (value >= READY_THRESHOLD / 100f) c.success else Color(0xFF2FA66A)
    val needle = c.textPrimary
    Canvas(modifier.clearAndSetSemantics { contentDescription = "Готовність ${(value * 100).toInt()} відсотків" }) {
        val stroke = 11.dp.toPx()
        val r = minOf(size.width / 2, size.height - stroke / 2) - stroke / 2
        val center = Offset(size.width / 2, size.height - 6.dp.toPx())
        val topLeft = Offset(center.x - r, center.y - r)
        val arcSize = Size(r * 2, r * 2)
        drawArc(track, 180f, 180f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        drawArc(arc, 180f, 180f * animated, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        val angle = PI + PI * animated
        val len = r * 0.78f
        val tip = Offset(center.x + (cos(angle) * len).toFloat(), center.y + (sin(angle) * len).toFloat())
        drawLine(needle, center, tip, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(needle, 6.dp.toPx(), center)
    }
}

@Composable
private fun StopsRoad(stops: List<Stop>) {
    val c = ZnakyTheme.colors
    val current = stops.indexOfFirst { !it.done }.let { if (it < 0) stops.lastIndex else it }
    val road = c.surfaceMuted
    val doneFill = c.success.copy(alpha = 0.35f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxWidth().height(26.dp)) {
                val h = size.height
                val step = size.width / stops.size
                drawRoundRect(road, cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2))
                val doneWidth = step * current + step / 2
                drawRoundRect(doneFill, size = Size(doneWidth, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2))
                drawLine(
                    Color.White.copy(alpha = 0.8f),
                    Offset(step / 2, h / 2),
                    Offset(size.width - step / 2, h / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                )
            }
            Row(Modifier.fillMaxWidth()) {
                stops.forEachIndexed { i, stop ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StopDot(stop, current = i == current)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            stops.forEachIndexed { i, stop ->
                Text(
                    stop.label,
                    style = ZnakyType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = if (i == current) c.onAccentTint else c.textTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StopDot(stop: Stop, current: Boolean) {
    val c = ZnakyTheme.colors
    when {
        stop.done -> Box(Modifier.size(30.dp).clip(CircleShape).background(c.success), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Check, contentDescription = "${stop.label}: пройдено", tint = c.surface, modifier = Modifier.size(18.dp))
        }
        current -> Box(
            Modifier.size(36.dp).clip(CircleShape).background(c.accent).padding(3.dp).clip(CircleShape).background(c.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.DirectionsCar, contentDescription = "${stop.label}: ви тут", tint = c.onPrimary, modifier = Modifier.size(18.dp))
        }
        else -> Box(
            Modifier.size(30.dp).clip(CircleShape).background(c.surface).border(1.5.dp, c.separator, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(stop.icon, contentDescription = stop.label, tint = c.textTertiary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun NextStopCard(topic: PlanTopic?, onGo: (PlanTopic?) -> Unit) {
    val c = ZnakyTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(26.dp)).background(c.surface),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))) {
            val side = maxWidth
            val dark = c.isDark
            ArtImage("Перехрестя: маршрут праворуч", Modifier.requiredSize(side).align(Alignment.Center)) {
                Street.crossroads(this, dark, zebra = true)
                Street.trafficLight(this, 60f, 68f, lit = 2)
                Street.route(this, ArtPath().moveTo(54.5f, 92f).lineTo(54.5f, 66f).quadTo(54.5f, 54.5f, 68f, 54.5f).lineTo(98f, 54.5f))
                Street.car(this, 45.5f, 30f, 180f, 0xFFF2B13A)
                Street.car(this, 54.5f, 84f, 0f, 0xFF2F62C9)
            }
            val label = when {
                topic == null -> "Фініш близько"
                topic.number.toIntOrNull() != null -> "ПДР · розділ ${topic.number}"
                else -> topic.block.label()
            }
            Text(
                label,
                style = ZnakyType.footnote,
                color = Color(0xFF14202E),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("НАСТУПНА ЗУПИНКА", style = ZnakyType.caption, color = c.onAccentTint)
            Text(topic?.title ?: "Пробні іспити", style = ZnakyType.title, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                if (topic == null) "Усі теми пройдено. Складіть 3 пробні поспіль — і ви готові"
                else "${topic.questions} ${questionsWord(topic.questions)} з офіційної бази · ~${minutesFor(topic.questions)} хв",
                style = ZnakyType.subhead,
                color = c.textSecondary,
            )
            ZButton(
                if (topic == null) "Пробний іспит" else "Поїхали",
                onClick = { onGo(topic) },
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
        }
    }
}

/** About 25 seconds per question, rounded up to whole minutes. */
internal fun minutesFor(questions: Int) = ((questions * 25 + 59) / 60).coerceAtLeast(1)

internal fun ExamBlock.label() = when (this) {
    ExamBlock.TRAFFIC_RULES -> "Правила дорожнього руху"
    ExamBlock.ROAD_SAFETY -> "Основи безпеки руху"
    ExamBlock.VEHICLE -> "Будова й експлуатація авто"
    ExamBlock.FIRST_AID -> "Домедична допомога"
}

@Composable
private fun Chip(icon: ImageVector, title: String, subtitle: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
        Column {
            Text(title, style = ZnakyType.bodyStrong, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal), color = c.textSecondary, maxLines = 1)
        }
    }
}
