package com.znaky.feature.car

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsMotorsports
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.znaky.core.art.CourseScenes
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.StepBar
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

// ---- Data (assets/courses/<id>.json, generated by tools/content/courses.py) ----------------

@Serializable
internal data class CourseStep(
    val title: String,
    val text: String = "",
    val bullets: List<String> = emptyList(),
    val note: String? = null,
    val warning: String? = null,
    val facts: List<List<String>> = emptyList(),
    val ref: String? = null,
    val scene: String? = null,
)

@Serializable
internal data class CourseChapter(val id: String, val title: String, val icon: String = "", val topicId: String? = null, val steps: List<CourseStep>)

@Serializable
internal data class CourseSource(val title: String, val url: String)

@Serializable
internal data class CourseData(
    val id: String,
    val title: String,
    val subtitle: String,
    val lead: String,
    val accent: Long,
    val anatomy: String,
    val examNote: String = "",
    val sources: List<CourseSource> = emptyList(),
    val chapters: List<CourseChapter>,
) {
    val accentColor: Color get() = Color(0xFF000000 or accent)
    val minutes: Int get() = chapters.sumOf { it.steps.size }
}

private val courseJson = Json { ignoreUnknownKeys = true }

@Composable
private fun rememberCourse(id: String): CourseData? {
    val context = LocalContext.current
    val course by produceState<CourseData?>(null, id) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("courses/$id.json").bufferedReader().use { courseJson.decodeFromString(CourseData.serializer(), it.readText()) }
            }.getOrNull()
        }
    }
    return course
}

/** Which chapters the learner has read, kept on the device. */
private class CourseProgress(context: Context) {
    private val prefs = context.getSharedPreferences("course_progress", Context.MODE_PRIVATE)
    fun done(course: String): Set<String> = prefs.getStringSet(course, emptySet()).orEmpty()
    fun markDone(course: String, chapter: String) = prefs.edit().putStringSet(course, done(course) + chapter).apply()
}

/** Question counts per topic, to show «N питань» and hide practice for topics without questions. */
@HiltViewModel
internal class CourseViewModel @Inject constructor(questions: QuestionRepository) : ViewModel() {
    val questionCounts: StateFlow<Map<String, Int>> = questions.observeTopics()
        .map { list -> list.associate { it.id to it.questionCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

private fun chapterIcon(key: String): ImageVector = when (key) {
    "badge" -> Icons.Rounded.Badge
    "lifebuoy" -> Icons.Rounded.SupportAgent
    "swap" -> Icons.Rounded.SwapHoriz
    "speed" -> Icons.Rounded.Speed
    "jetski" -> Icons.Rounded.Waves
    "engine" -> Icons.Rounded.Settings
    "sos" -> Icons.Rounded.Sos
    "block" -> Icons.Rounded.Block
    "moto" -> Icons.Rounded.TwoWheeler
    "helmet" -> Icons.Rounded.SportsMotorsports
    "steer" -> Icons.Rounded.Tune
    "lane" -> Icons.Rounded.AltRoute
    "passenger" -> Icons.Rounded.Groups
    "wrench" -> Icons.Rounded.Build
    else -> Icons.AutoMirrored.Rounded.MenuBook
}

// ---- Course hub ---------------------------------------------------------------------------

@Composable
internal fun CourseScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenChapter: (chapterId: String) -> Unit,
    onOpenAnatomy: (kind: String) -> Unit,
    onTrain: (topicId: String) -> Unit,
    onOpenRules: () -> Unit,
    viewModel: CourseViewModel = hiltViewModel(),
) {
    val course = rememberCourse(courseId) ?: return LoadingBox()
    val counts by viewModel.questionCounts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val progress = remember(context) { CourseProgress(context) }
    // Re-read on every return from a lesson.
    val done = remember(course.id) { mutableStateOf(progress.done(course.id)) }
    LifecycleResumeEffect(course.id) {
        done.value = progress.done(course.id)
        onPauseOrDispose { }
    }
    val c = ZnakyTheme.colors
    val accent = course.accentColor
    val next = course.chapters.firstOrNull { it.id !in done.value }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "hero") { CourseHero(course, done.value.size, next, onContinue = { (next ?: course.chapters.first()).let { onOpenChapter(it.id) } }, onOpenAnatomy = { onOpenAnatomy(course.anatomy) }) }
        item(key = "chaptersTitle") {
            Text(
                "РОЗДІЛИ · ${course.chapters.size}",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 22.dp).semantics { heading() },
            )
        }
        item(key = "chapters") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(22.dp)).background(c.surface)) {
                course.chapters.forEachIndexed { i, ch ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 66.dp), thickness = 1.dp, color = c.separator)
                    ChapterRow(
                        index = i + 1,
                        chapter = ch,
                        accent = accent,
                        isDone = ch.id in done.value,
                        isNext = ch == next,
                        questions = ch.topicId?.let { counts[it] } ?: 0,
                        onOpen = { onOpenChapter(ch.id) },
                        onTrain = { ch.topicId?.let(onTrain) },
                    )
                }
            }
        }
        item(key = "rules") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Повний текст правил") {
                ListRow(
                    title = if (course.id == "boat") "Правила плавання малих суден" else "ПДР для мотоцикліста",
                    subtitle = if (course.id == "boat") {
                        "Офіційний текст по розділах і пунктах"
                    } else {
                        "Усі 34 розділи, пункти про мотоцикли й мопеди позначені"
                    },
                    onClick = onOpenRules,
                )
            }
        }
        if (course.examNote.isNotEmpty()) {
            item(key = "exam") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(18.dp)).background(c.primaryTint).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = c.link, modifier = Modifier.size(20.dp))
                    Text(course.examNote, style = ZnakyType.subhead, color = c.textPrimary)
                }
            }
        }
        item(key = "sources") { Sources(course.sources) }
    }
}

@Composable
private fun CourseHero(course: CourseData, doneCount: Int, next: CourseChapter?, onContinue: () -> Unit, onOpenAnatomy: () -> Unit) {
    val c = ZnakyTheme.colors
    val accent = course.accentColor
    val total = course.chapters.size
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(c.surface),
    ) {
        // Animated cover: the course's first scene, playing.
        val cover = if (course.id == "boat") CourseScenes.Kind.BOAT_HEADON else CourseScenes.Kind.MOTO_VISIBLE
        CourseSceneView(cover, Modifier.fillMaxWidth().height(170.dp), radius = 0)
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(course.subtitle.uppercase(), style = ZnakyType.caption, color = accent)
            Text(course.title, style = ZnakyType.title.copy(fontSize = 28.sp, lineHeight = 34.sp), color = c.textPrimary, modifier = Modifier.semantics { heading() })
            Text(course.lead, style = ZnakyType.callout, color = c.textSecondary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LinearBar(doneCount / total.toFloat(), Modifier.weight(1f), height = 6.dp, color = accent)
                Text("$doneCount з $total", style = ZnakyType.footnote, color = c.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZButton(
                    when {
                        next == null -> "Повторити"
                        doneCount == 0 -> "Почати курс"
                        else -> "Далі: ${next.title}"
                    },
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                )
                ZButton("3D", onClick = onOpenAnatomy, kind = ButtonKind.Tinted, icon = Icons.Rounded.ViewInAr, contentDescription = "Будова в 3D")
            }
        }
    }
}

@Composable
private fun ChapterRow(index: Int, chapter: CourseChapter, accent: Color, isDone: Boolean, isNext: Boolean, questions: Int, onOpen: () -> Unit, onTrain: () -> Unit) {
    val c = ZnakyTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isNext) accent.copy(alpha = if (c.isDark) 0.16f else 0.08f) else Color.Transparent)
            .clickable(role = Role.Button, onClick = onOpen)
            .heightIn(min = 64.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (isDone) c.successTint else accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            if (isDone) Icon(Icons.Rounded.Check, contentDescription = "Прочитано", tint = c.success, modifier = Modifier.size(22.dp))
            else Icon(chapterIcon(chapter.icon), contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("$index. ${chapter.title}", style = ZnakyType.bodyStrong, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                buildString {
                    append("${chapter.steps.size} ${stepsWord(chapter.steps.size)}")
                    if (questions > 0) append(" · $questions питань")
                    if (isNext) append(" · ТИ ТУТ")
                },
                style = ZnakyType.footnote,
                color = if (isNext) accent else c.textTertiary,
            )
        }
        if (questions > 0) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(c.primaryTint).clickable(role = Role.Button, onClick = onTrain)
                    .semantics { contentDescription = "Питання з теми «${chapter.title}»" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Quiz, contentDescription = null, tint = c.link, modifier = Modifier.size(20.dp)) }
        }
    }
}

private fun stepsWord(n: Int) = when {
    n % 10 == 1 && n % 100 != 11 -> "картка"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "картки"
    else -> "карток"
}

@Composable
private fun Sources(sources: List<CourseSource>) {
    if (sources.isEmpty()) return
    val c = ZnakyTheme.colors
    val uri = LocalUriHandler.current
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("ДЖЕРЕЛА", style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.padding(horizontal = 6.dp).semantics { heading() })
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface)) {
            sources.forEachIndexed { i, s ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 1.dp, color = c.separator)
                Text(
                    s.title,
                    style = ZnakyType.subhead,
                    color = c.link,
                    modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { runCatching { uri.openUri(s.url) } }.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

// ---- Lesson (one chapter, step by step) ---------------------------------------------------

@Composable
internal fun CourseLessonScreen(
    courseId: String,
    chapterId: String,
    onBack: () -> Unit,
    onTrain: (topicId: String) -> Unit,
    onOpenAnatomy: (kind: String) -> Unit,
    viewModel: CourseViewModel = hiltViewModel(),
) {
    val course = rememberCourse(courseId) ?: return LoadingBox()
    val chapter = course.chapters.firstOrNull { it.id == chapterId } ?: course.chapters.first()
    val counts by viewModel.questionCounts.collectAsStateWithLifecycle()
    val questions = chapter.topicId?.let { counts[it] } ?: 0
    val context = LocalContext.current
    val c = ZnakyTheme.colors
    val accent = course.accentColor
    var index by rememberSaveable(chapterId) { mutableIntStateOf(0) }
    val last = index == chapter.steps.lastIndex
    val finish = {
        CourseProgress(context).markDone(course.id, chapter.id)
    }
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack) {
            StepBar(chapter.steps.size, index + 1, Modifier.weight(1f), color = accent)
            Text("${index + 1}/${chapter.steps.size}", style = ZnakyType.footnote, color = c.textSecondary)
        }
        AnimatedContent(
            targetState = index,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "step",
        ) { i ->
            val step = chapter.steps[i]
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(chapter.title.uppercase(), style = ZnakyType.caption, color = accent)
                    Text(step.title, style = ZnakyType.title, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                }
                val scene = CourseScenes.Kind.of(step.scene)
                if (scene != null) CourseSceneView(scene, Modifier.fillMaxWidth().height(240.dp))
                ZCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (step.text.isNotEmpty()) Text(step.text, style = ZnakyType.body, color = c.textPrimary)
                        if (step.bullets.isNotEmpty()) Bullets(step.bullets, accent)
                        if (step.facts.isNotEmpty()) FactTiles(step.facts)
                        step.warning?.let { Callout(it, Icons.Rounded.Warning, c.error, c.errorTint) }
                        step.note?.let { Callout(it, Icons.Rounded.Lightbulb, c.onAccentTint, c.accentTint) }
                        step.ref?.let { RefChip(it) }
                    }
                }
                if (step.scene == "boat_parts" || step.scene == "moto_check") {
                    ZButton("Відкрити 3D-модель", onClick = { onOpenAnatomy(course.anatomy) }, kind = ButtonKind.Tinted, icon = Icons.Rounded.ViewInAr, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (index > 0) {
                ZButton("", onClick = { index-- }, kind = ButtonKind.Plain, icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Попередня картка", modifier = Modifier.size(56.dp))
            }
            if (last && questions > 0 && chapter.topicId != null) {
                ZButton("$questions питань", onClick = { finish(); onTrain(chapter.topicId) }, kind = ButtonKind.Tinted, modifier = Modifier.weight(1f))
            }
            ZButton(
                if (last) "Готово" else "Далі",
                onClick = { if (last) { finish(); onBack() } else index++ },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Bullets(items: List<String>, accent: Color) {
    val c = ZnakyTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { s ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.padding(top = 8.dp).size(7.dp).clip(CircleShape).background(accent))
                Text(s, style = ZnakyType.callout, color = c.textPrimary)
            }
        }
    }
}

@Composable
private fun FactTiles(facts: List<List<String>>) {
    val c = ZnakyTheme.colors
    val tints = if (c.isDark) listOf(Color(0xFF3A3016) to Color(0xFFFFD98A), Color(0xFF123832) to Color(0xFF8FE6D6))
    else listOf(Color(0xFFFFF8DB) to Color(0xFF7A5600), Color(0xFFDDF7F2) to Color(0xFF0F5E54))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        facts.forEachIndexed { i, f ->
            val (bg, fg) = tints[i % tints.size]
            Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(f.getOrElse(0) { "" }, style = ZnakyType.caption, color = fg)
                Text(f.getOrElse(1) { "" }, style = ZnakyType.subhead.copy(fontSize = 15.sp), color = fg)
            }
        }
    }
}

@Composable
private fun Callout(text: String, icon: ImageVector, tint: Color, bg: Color) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(text, style = ZnakyType.subhead, color = c.textPrimary)
    }
}

@Composable
private fun RefChip(ref: String) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(c.background).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.width(3.dp).height(14.dp).background(c.primary))
        Text(ref, style = ZnakyType.footnote, color = c.link)
    }
}

// ---- Scenes -------------------------------------------------------------------------------

@Composable
internal fun CourseSceneView(kind: CourseScenes.Kind, modifier: Modifier = Modifier, radius: Int = 26) {
    val c = ZnakyTheme.colors
    val time = if (reducedMotionOn()) remember { mutableFloatStateOf(0.45f) } else courseLoop(if (kind.water) 7000 else 5000)
    val art = rememberArtCanvas()
    Box(
        modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(if (c.isDark) Color(0xFF141D27) else Color(0xFFEEF2F7))
            .semantics { contentDescription = "Анімована ілюстрація до уроку" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawArt(art) { CourseScenes.draw(this, kind, time.value, c.isDark) }
        }
    }
}

@Composable
private fun courseLoop(durationMillis: Int): State<Float> =
    rememberInfiniteTransition(label = "course").animateFloat(
        0f, 1f, infiniteRepeatable(tween(durationMillis, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )

@Composable
private fun reducedMotionOn(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

