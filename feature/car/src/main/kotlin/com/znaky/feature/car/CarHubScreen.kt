package com.znaky.feature.car

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.art.Car3D
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** «3D-машина»: a rotatable car with x-ray and scheme views, and the list of topics. */
@Composable
internal fun CarHubScreen(onBack: () -> Unit, onOpenTopic: (id: String, step: Int) -> Unit, onOpenGlossary: () -> Unit) {
    val c = ZnakyTheme.colors
    var kindIndex by rememberSaveable { mutableIntStateOf(0) }
    var viewIndex by rememberSaveable { mutableIntStateOf(0) }
    val kind = CarKind.entries[kindIndex]
    val view = CarView.entries[viewIndex]
    val groups = remember(kind) { topicGroups(kind) }

    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack, backLabel = "Головна") }
        item(key = "title") {
            LargeTitle("Як влаштована машина", subtitle = "Головне про вузли авто, коротко і з картинками. Для іспиту і для дороги.")
        }
        item(key = "kind") {
            SegmentedControl(listOf("З двигуном", "Електромобіль"), kindIndex, { kindIndex = it }, Modifier.padding(horizontal = 16.dp))
        }
        item(key = "car") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Car3DView(kind, view)
                SegmentedControl(listOf("3D", "Рентген", "Схема"), viewIndex, { viewIndex = it })
            }
        }
        item(key = "continue") {
            val accent = Color(0xFFC93A3F)
            ZCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), onClick = { onOpenTopic("brakes", 0) }) {
                Text("ПРОДОВЖИТИ", style = ZnakyType.caption, color = c.textTertiary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Гальма і ABS", style = ZnakyType.headline, color = c.textPrimary, modifier = Modifier.weight(1f))
                    Text("3 кроки", style = ZnakyType.footnote, color = c.textSecondary)
                }
                LinearBar(0f, Modifier.padding(top = 10.dp), color = accent)
            }
        }
        items(groups, key = { "${kind.name}-${it.name}" }) { group ->
            GroupedList(Modifier.padding(horizontal = 16.dp), header = group.name) {
                group.entries.forEachIndexed { i, entry ->
                    val id = entry.topicId
                    ListRow(
                        entry.title,
                        subtitle = entry.meta,
                        showDivider = i > 0,
                        onClick = id?.let { { onOpenTopic(it, entry.step) } },
                        leading = { Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(group.color)) },
                    )
                }
            }
        }
        item(key = "glossary") {
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Словник") {
                ListRow(
                    "Словник водія",
                    subtitle = "${termsCount(GlossaryTerms.size)}: від кліренсу до ABS",
                    showDivider = false,
                    onClick = onOpenGlossary,
                    leading = { Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(TermCategory.PDR.color)) },
                )
            }
        }
        item(key = "note") {
            Text(
                "Модель спрощена і показує принцип роботи. Це не інструкція з ремонту.",
                style = ZnakyType.footnote,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            )
        }
    }
}

/**
 * The car spins slowly on its own; dragging turns it by hand and pauses the spin for a moment.
 * Rotation lives in an [Animatable] read only inside the Canvas, so frames skip recomposition.
 */
@Composable
private fun Car3DView(kind: CarKind, view: CarView) {
    val c = ZnakyTheme.colors
    val renderer = remember(kind) { Car3D.Renderer(Car3D.build(kind.art)) }
    val art = rememberArtCanvas()
    val yaw = remember { Animatable(-35f) }
    var dragging by remember { mutableStateOf(false) }
    val pitch by animateFloatAsState(if (view == CarView.SCHEME) -88f else -22f, tween(500), label = "pitch")
    val scope = rememberCoroutineScope()

    LaunchedEffect(dragging, view) {
        if (dragging || view == CarView.SCHEME) {
            if (view == CarView.SCHEME) yaw.animateTo(0f, tween(500))
            return@LaunchedEffect
        }
        while (isActive) {
            // 16 s per turn, like the mockup; continue from wherever the user left it.
            yaw.animateTo(yaw.value + 360f, tween(16_000, easing = LinearEasing))
        }
    }

    val sky = if (c.isDark) listOf(Color(0xFF1C2733), Color(0xFF101820)) else listOf(Color(0xFFF7F9FC), Color(0xFFE4EAF2))
    Box(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(sky))
            .semantics {
                contentDescription = if (kind == CarKind.ELECTRIC) "Тривимірна модель електромобіля. Проведіть пальцем, щоб покрутити."
                else "Тривимірна модель автомобіля з двигуном. Проведіть пальцем, щоб покрутити."
            }
            .pointerInput(view) {
                if (view == CarView.SCHEME) return@pointerInput
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false },
                ) { change, amount ->
                    change.consume()
                    scope.launch { yaw.snapTo(yaw.value - amount.x * 0.45f) }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawArt(art) {
                Car3D.shadow(this, pitch)
                renderer.draw(this, yaw.value, pitch, view.art)
            }
        }
        if (view != CarView.SCHEME) {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(12.dp).clip(RoundedCornerShape(999.dp)).background(c.surface.copy(alpha = 0.85f)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
                Text("Крутіть", style = ZnakyType.footnote, color = c.textSecondary)
            }
        } else {
            Legend(kind, Modifier.align(Alignment.BottomStart).padding(12.dp))
        }
    }
}

@Composable
private fun Legend(kind: CarKind, modifier: Modifier = Modifier) {
    val items = when (kind) {
        CarKind.ENGINE -> listOf("Двигун" to EngineColor, "Коробка" to GearboxColor, "Бак" to FuelColor)
        CarKind.ELECTRIC -> listOf("Батарея" to BatteryColor, "Мотор" to MotorColor)
    }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (name, color) ->
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(ZnakyTheme.colors.surface.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Text(name, style = ZnakyType.caption, color = ZnakyTheme.colors.textPrimary)
            }
        }
    }
}
