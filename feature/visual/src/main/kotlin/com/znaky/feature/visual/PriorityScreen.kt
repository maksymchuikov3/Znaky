package com.znaky.feature.visual

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.art.ArtCanvas
import com.znaky.core.art.Street
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.ButtonKind
import com.znaky.core.designsystem.component.NumberBadge
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.StepBar
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.component.ZCard
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

@Immutable
internal data class PriorityLevel(
    val title: String,
    val situation: String,
    val cars: List<CarTrack>,
    /** Labels of [cars] in the correct order. */
    val order: List<String>,
    val hint: String,
    val mainRoadSign: Boolean = false,
)

private val me = { pts: List<Offset>, ts: List<Float> -> CarTrack("Ви", SceneColors.Me, SceneColors.MeRoof, pts, ts) }

internal val priorityLevels = listOf(
    PriorityLevel(
        title = "Рівнозначне перехрестя",
        situation = "Знаків немає. Ви їдете прямо, жовтий автомобіль наближається справа.",
        cars = listOf(
            me(listOf(Offset(Lanes.UP, 1.1f), Offset(Lanes.UP, 0.68f), Offset(Lanes.UP, 0.68f), Offset(Lanes.UP, -0.1f)), listOf(0f, 0.18f, 0.52f, 0.9f)),
            CarTrack("Жовтий", SceneColors.Yellow, SceneColors.YellowRoof, listOf(Offset(1.1f, Lanes.LEFTWARD), Offset(-0.1f, Lanes.LEFTWARD)), listOf(0.06f, 0.5f)),
        ),
        order = listOf("Жовтий", "Ви"),
        hint = "Перешкода справа: поступаємось",
    ),
    PriorityLevel(
        title = "Ви на головній дорозі",
        situation = "Перед перехрестям знак «Головна дорога». Жовтий автомобіль виїжджає справа з другорядної.",
        cars = listOf(
            me(listOf(Offset(Lanes.UP, 1.1f), Offset(Lanes.UP, -0.1f)), listOf(0f, 0.45f)),
            CarTrack("Жовтий", SceneColors.Yellow, SceneColors.YellowRoof, listOf(Offset(1.1f, Lanes.LEFTWARD), Offset(0.68f, Lanes.LEFTWARD), Offset(0.68f, Lanes.LEFTWARD), Offset(-0.1f, Lanes.LEFTWARD)), listOf(0f, 0.15f, 0.5f, 0.9f)),
        ),
        order = listOf("Ви", "Жовтий"),
        hint = "Головна дорога: ви проїжджаєте першим",
        mainRoadSign = true,
    ),
)

/** "Хто першим": an animated 3D crossroads and an order builder. */
@Composable
internal fun PriorityScreen(onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    var levelIndex by rememberSaveable { mutableStateOf(0) }
    val level = priorityLevels[levelIndex]
    var mode3d by rememberSaveable { mutableStateOf(0) }
    val picked = remember(levelIndex) { mutableStateListOf<String>() }
    var checked by remember(levelIndex) { mutableStateOf(false) }
    val correct = picked.toList() == level.order

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack, close = true) {
            StepBar(priorityLevels.size, levelIndex + 1, Modifier.weight(1f))
            Text("${levelIndex + 1}/${priorityLevels.size}", style = ZnakyType.footnote, color = c.textSecondary)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ХТО ПЕРШИМ · ${level.title.uppercase()}", style = ZnakyType.caption, color = c.purple)
            Text(level.situation, style = ZnakyType.callout, color = c.textSecondary)
            CrossroadsScene(level, tilt = mode3d == 0, dark = c.isDark)
            SegmentedControl(listOf("3D", "Згори"), mode3d, { mode3d = it })
            ZCard(Modifier.fillMaxWidth()) {
                Text("Розставте черговість", style = ZnakyType.headline, color = c.textPrimary)
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(level.cars.size) { slot ->
                        val label = picked.getOrNull(slot)
                        val car = level.cars.firstOrNull { it.label == label }
                        Row(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (car != null) c.primaryTint else c.background)
                                .border(2.dp, car?.body ?: c.separator, RoundedCornerShape(14.dp))
                                .clickable(enabled = car != null && !checked, role = Role.Button) { picked.remove(label) }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NumberBadge("${slot + 1}", car?.body ?: c.surfaceMuted, Color.White)
                            Text(label ?: "порожньо", style = ZnakyType.bodyStrong, color = if (car != null) c.textPrimary else c.textTertiary)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    level.cars.filter { it.label !in picked }.forEach { car ->
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(c.surfaceMuted)
                                .clickable(enabled = !checked, role = Role.Button) { picked.add(car.label) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(car.body))
                            Text(car.label, style = ZnakyType.bodyStrong, color = c.textPrimary)
                        }
                    }
                }
                if (checked) {
                    Text(
                        if (correct) "Правильно! ${level.hint}." else "Ні. ${level.hint}.",
                        style = ZnakyType.bodyStrong,
                        color = if (correct) c.success else c.error,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (checked) {
                ZButton("Ще раз", onClick = { picked.clear(); checked = false }, kind = ButtonKind.Tinted, modifier = Modifier.weight(1f))
                ZButton(
                    if (levelIndex + 1 < priorityLevels.size) "Далі" else "Готово",
                    onClick = { if (levelIndex + 1 < priorityLevels.size) levelIndex++ else onBack() },
                    modifier = Modifier.weight(1f),
                )
            } else {
                ZButton("Перевірити", onClick = { checked = true }, enabled = picked.size == level.cars.size, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** A looping 0..1 clock. Read it only inside draw lambdas so frames skip recomposition. */
@Composable
internal fun rememberLoopTime(durationMillis: Int): State<Float> =
    rememberInfiniteTransition(label = "loop").animateFloat(
        0f, 1f, infiniteRepeatable(tween(durationMillis, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )

@Composable
internal fun CrossroadsScene(level: PriorityLevel, tilt: Boolean, dark: Boolean, modifier: Modifier = Modifier) {
    val time = rememberLoopTime(7000)
    TrafficScene(level.cars, "Анімація перехрестя. ${level.hint}.", tilt, dark, time, modifier) {
        if (level.mainRoadSign) Street.mainRoadSign(this, 64f, 72f)
    }
}

/** A tilted crossroads with [cars] driven by [time]; [overlay] draws extra props (signs, lights). */
@Composable
internal fun TrafficScene(
    cars: List<CarTrack>,
    description: String,
    tilt: Boolean,
    dark: Boolean,
    time: State<Float>,
    modifier: Modifier = Modifier,
    overlay: ArtCanvas.() -> Unit = {},
) {
    val angle by animateFloatAsState(if (tilt) 52f else 0f, tween(600), label = "tilt")
    val sky = if (dark) Color(0xFF14263A) else Color(0xFFCFE3F5)
    val art = rememberArtCanvas()
    Box(
        modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(sky)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .size(300.dp)
                .graphicsLayer {
                    // A real 3D tilt of the flat map: cheap, and the 3D/2D switch animates smoothly.
                    rotationX = angle
                    cameraDistance = 9f * density
                    scaleX = 1f + angle / 180f
                    scaleY = 1f + angle / 180f
                },
        ) {
            drawArt(art) {
                Street.crossroads(this, dark)
                overlay()
                val t = time.value
                cars.forEach { car -> car.at(t)?.let { (pos, heading) -> drawTrackCar(car, pos, heading) } }
            }
        }
    }
}
