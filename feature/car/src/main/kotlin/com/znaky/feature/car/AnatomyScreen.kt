package com.znaky.feature.car

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.znaky.core.art.Anatomy3D
import com.znaky.core.art.Car3D
import com.znaky.core.art.fill
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.LoadingBox
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.hypot

@Serializable
internal data class AnatomyPart(
    val name: String,
    val color: Long,
    val tag: String = "",
    val kid: String = "",
    val how: List<String> = emptyList(),
    val facts: List<List<String>> = emptyList(),
    val check: List<String> = emptyList(),
    val exam: String = "",
) {
    /** The colour as ARGB (the asset stores 0xRRGGBB). */
    val argb: Long get() = 0xFF000000 or color
}

@Serializable
internal data class AnatomyKind(val title: String, val lead: String, val parts: List<String>)

@Serializable
internal data class AnatomyData(val kinds: Map<String, AnatomyKind>, val parts: Map<String, AnatomyPart>)

private val json = Json { ignoreUnknownKeys = true }

private val Kinds = listOf("ice" to Anatomy3D.Kind.ICE, "ev" to Anatomy3D.Kind.EV, "moto" to Anatomy3D.Kind.MOTO)

@Composable
internal fun AnatomyScreen(startKind: String, onBack: () -> Unit, onOpenLessons: () -> Unit) {
    val context = LocalContext.current
    val data by produceState<AnatomyData?>(null) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("anatomy/parts.json").bufferedReader().use { json.decodeFromString(AnatomyData.serializer(), it.readText()) }
            }.getOrNull()
        }
    }
    val c = ZnakyTheme.colors
    var kindIndex by rememberSaveable { mutableIntStateOf(Kinds.indexOfFirst { it.first == startKind }.coerceAtLeast(0)) }
    var selected by rememberSaveable(kindIndex) { mutableStateOf<String?>(null) }
    val d = data
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        TopBar(onBack = onBack) {
            Text(
                "4 з 20 на іспиті",
                style = ZnakyType.footnote,
                color = c.onPrimaryTint,
                modifier = Modifier.clip(CircleShape).background(c.primaryTint).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        if (d == null) {
            LoadingBox()
            return@Column
        }
        val (kindKey, kind) = Kinds[kindIndex]
        val info = d.kinds.getValue(kindKey)
        val ids = info.parts.filter { it in d.parts }
        val listState = rememberLazyListState()
        LaunchedEffect(selected) {
            val i = ids.indexOf(selected)
            if (i >= 0) listState.animateScrollToItem(HEADER_ITEMS + i)
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "title") {
                Text("Будова авто", style = ZnakyType.largeTitle, color = c.textPrimary, modifier = Modifier.padding(horizontal = 20.dp))
            }
            item(key = "kinds") {
                SegmentedControl(
                    listOf("Звичайне авто", "Електромобіль", "Мотоцикл"),
                    selectedIndex = kindIndex,
                    onSelect = { kindIndex = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item(key = "model") {
                ModelView(kind, ids, d.parts, selected, onSelect = { selected = if (selected == it) null else it })
            }
            item(key = "lead") {
                Text(info.lead, style = ZnakyType.callout, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
            }
            itemsIndexed(ids, key = { _, id -> "$kindKey-$id" }) { i, id ->
                val part = d.parts.getValue(id)
                PartRow(
                    number = i + 1,
                    part = part,
                    expanded = selected == id,
                    first = i == 0,
                    last = i == ids.lastIndex,
                    onClick = { selected = if (selected == id) null else id },
                )
            }
            item(key = "lessons") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(c.primaryTint)
                        .clickable(onClick = onOpenLessons)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.School, contentDescription = null, tint = c.link)
                    Text("3D-уроки: як працюють двигун, гальма і батарея", style = ZnakyType.bodyStrong, color = c.onPrimaryTint, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Items above the part list in the LazyColumn: title, segments, model, lead. */
private const val HEADER_ITEMS = 4

/**
 * The model spins slowly with numbered markers on every part. Dragging turns it; tapping a
 * marker selects that part, which is highlighted while the rest dims.
 */
@Composable
private fun ModelView(kind: Anatomy3D.Kind, ids: List<String>, parts: Map<String, AnatomyPart>, selected: String?, onSelect: (String) -> Unit) {
    val c = ZnakyTheme.colors
    val model = remember(kind) { Anatomy3D.build(kind) { id -> parts[id]?.argb ?: 0xFF8B95A3 } }
    val renderer = remember(model) { Car3D.Renderer(model.mesh) }
    val art = rememberArtCanvas()
    val yaw = remember { Animatable(-30f) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pitch = -16f
    val shellAlpha = if (kind == Anatomy3D.Kind.MOTO) 0.9f else 0.22f
    LaunchedEffect(dragging, selected) {
        if (dragging || selected != null) return@LaunchedEffect
        while (isActive) {
            yaw.animateTo(yaw.value + 360f, tween(24_000, easing = LinearEasing))
        }
    }
    val sky = if (c.isDark) listOf(Color(0xFF1C2733), Color(0xFF101820)) else listOf(Color(0xFFFFFFFF), Color(0xFFE9EEF5))
    val markerColor = c.primary
    val point = FloatArray(3)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(sky))
            .semantics { contentDescription = "Тривимірна модель з пронумерованими частинами. Торкніться номера, щоб прочитати про частину." }
            .pointerInput(kind) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false },
                ) { change, amount ->
                    change.consume()
                    scope.launch { yaw.snapTo(yaw.value - amount.x * 0.45f) }
                }
            }
            .pointerInput(kind, ids) {
                detectTapGestures { pos ->
                    val hit = FloatArray(3)
                    var best: String? = null
                    var bestDist = 28.dp.toPx()
                    ids.forEach { id ->
                        val a = model.anchors[id] ?: return@forEach
                        renderer.project(size.width.toFloat(), size.height.toFloat(), yaw.value, pitch, a[0], a[1], a[2], hit)
                        val dist = hypot(hit[0] - pos.x, hit[1] - pos.y)
                        if (dist < bestDist) {
                            bestDist = dist
                            best = id
                        }
                    }
                    best?.let(onSelect)
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = 9.dp.toPx()
            val textSize = 9.dp.toPx()
            drawArt(art) {
                Car3D.shadow(this, pitch)
                renderer.draw(this, yaw.value, pitch, Car3D.View.X_RAY, highlight = selected, shellAlpha = shellAlpha)
                ids.forEachIndexed { i, id ->
                    val a = model.anchors[id] ?: return@forEachIndexed
                    renderer.project(width, height, yaw.value, pitch, a[0], a[1], a[2], point)
                    val behind = point[2] > 30f
                    val isSelected = id == selected
                    val rr = if (isSelected) r * 1.3f else r
                    val alpha = if (behind && !isSelected) 0.45f else 1f
                    circle(point[0], point[1], rr + 1.5f, fill(0xFFFFFFFF, alpha))
                    circle(point[0], point[1], rr, fill(if (isSelected) 0xFFFFB547 else markerColor.toArgbLong(), alpha))
                    text("${i + 1}", point[0], point[1], textSize, 0xFFFFFFFF)
                }
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(10.dp).clip(CircleShape).background(c.surface.copy(alpha = 0.85f)).padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(14.dp))
            Text("Крутіть і торкайтесь номерів", style = ZnakyType.caption, color = c.textSecondary)
        }
    }
}

private fun Color.toArgbLong(): Long {
    val a = (alpha * 255).toInt() and 0xFF
    val r = (red * 255).toInt() and 0xFF
    val g = (green * 255).toInt() and 0xFF
    val b = (blue * 255).toInt() and 0xFF
    return (a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}

@Composable
private fun PartRow(number: Int, part: AnatomyPart, expanded: Boolean, first: Boolean, last: Boolean, onClick: () -> Unit) {
    val c = ZnakyTheme.colors
    val top = if (first) 20.dp else 0.dp
    val bottom = if (last) 20.dp else 0.dp
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
            .background(if (expanded) c.primaryTint else c.surface)
            .clickable(onClick = onClick),
    ) {
        if (!first) HorizontalDivider(Modifier.padding(start = 52.dp), thickness = 1.dp, color = c.separator)
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(if (expanded) c.accent else c.primary), contentAlignment = Alignment.Center) {
                Text("$number", style = ZnakyType.footnote, color = c.onPrimary)
            }
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.textPrimary)) { append(part.name) }
                    append(" — ${part.kid}")
                },
                style = ZnakyType.subhead,
                color = c.textSecondary,
                modifier = Modifier.weight(1f),
            )
        }
        if (expanded) PartDetails(part)
    }
}

@Composable
private fun PartDetails(part: AnatomyPart) {
    val c = ZnakyTheme.colors
    Column(Modifier.padding(start = 52.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (part.facts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                part.facts.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { f ->
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(c.surface).padding(8.dp)) {
                                Text(f.getOrElse(0) { "" }, style = ZnakyType.headline, color = c.textPrimary)
                                Text(f.getOrElse(1) { "" }, style = ZnakyType.caption.copy(fontWeight = FontWeight.Normal), color = c.textSecondary)
                            }
                        }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }
        if (part.how.isNotEmpty()) {
            Text("ЯК ПРАЦЮЄ", style = ZnakyType.caption, color = c.textTertiary)
            part.how.forEachIndexed { i, step ->
                Text("${i + 1}. $step", style = ZnakyType.subhead, color = c.textPrimary)
            }
        }
        if (part.check.isNotEmpty()) {
            Text("ЩО ПЕРЕВІРЯТИ", style = ZnakyType.caption, color = c.textTertiary)
            part.check.forEach { Text("• $it", style = ZnakyType.subhead, color = c.textPrimary) }
        }
        if (part.exam.isNotBlank()) {
            Text(
                "На іспиті: ${part.exam}",
                style = ZnakyType.subhead,
                color = c.onAccentTint,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.accentTint).padding(10.dp),
            )
        }
    }
}
