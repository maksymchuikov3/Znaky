package com.znaky.feature.visual

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Traffic
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.GroupedList
import com.znaky.core.designsystem.component.IconTile
import com.znaky.core.designsystem.component.ListRow
import com.znaky.core.designsystem.component.NumberBadge
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

private const val LOOP_MS = 8000

/** Our car: stops in the middle of the crossing, waits, then finishes the left turn. */
private val turnLeftCars = listOf(
    CarTrack(
        "Ви", SceneColors.Me, SceneColors.MeRoof,
        listOf(
            Offset(Lanes.UP, 1.1f), Offset(Lanes.UP, 0.63f), Offset(0.52f, 0.53f), Offset(0.52f, 0.53f),
            Offset(0.40f, Lanes.LEFTWARD), Offset(-0.1f, Lanes.LEFTWARD),
        ),
        listOf(0f, 0.2f, 0.3f, 0.6f, 0.7f, 0.92f),
    ),
    CarTrack(
        "Зустрічний", SceneColors.Silver, SceneColors.SilverRoof,
        listOf(Offset(Lanes.DOWN, -0.1f), Offset(Lanes.DOWN, 1.1f)),
        listOf(0.24f, 0.6f),
    ),
)

private data class ExplainStep(val title: String, val text: String, val until: Float)

private val turnLeftSteps = listOf(
    ExplainStep("Заїжджаєте на перехрестя", "На зелений сигнал виїжджаєте до центру перехрестя і зупиняєтесь, не заважаючи зустрічним.", 0.3f),
    ExplainStep("Пропускаєте зустрічних", "Транспорт, що їде назустріч прямо або праворуч, має перевагу. Чекаєте, поки він проїде.", 0.6f),
    ExplainStep("Завершуєте поворот", "Коли смуга вільна, завершуєте поворот ліворуч, навіть якщо сигнал уже змінився.", 1f),
)

/** «Пояснення»: animated schemes for hard rules, plus links to the interactive scenes. */
@Composable
internal fun ExplainScreen(onBack: () -> Unit, onOpenPriority: () -> Unit, onOpenOfficer: () -> Unit) {
    val c = ZnakyTheme.colors
    var mode by rememberSaveable { mutableStateOf(0) }
    val time = rememberLoopTime(LOOP_MS)
    // Recompose only when the highlighted step changes, not every frame.
    val step by remember { derivedStateOf { turnLeftSteps.indexOfFirst { time.value <= it.until }.coerceAtLeast(0) } }

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack, backLabel = "Пояснення")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("СХЕМА · ПЕРЕХРЕСТЯ", style = ZnakyType.caption, color = c.purple)
                Text("Поворот ліворуч на зелений", style = ZnakyType.title, color = c.textPrimary)
                TrafficScene(
                    turnLeftCars,
                    "Анімація: ви заїжджаєте на перехрестя, пропускаєте зустрічний автомобіль, що їде прямо, і завершуєте поворот ліворуч.",
                    tilt = mode == 0,
                    dark = c.isDark,
                    time = time,
                    overlay = { drawTrafficLight() },
                )
                SegmentedControl(listOf("3D", "Згори"), mode, { mode = it })
                turnLeftSteps.forEachIndexed { i, s -> StepRow(i, s, active = i == step) }
                Row { Pill("п. 16.6 ПДР") }
            }
            GroupedList(Modifier.padding(horizontal = 16.dp), header = "Інші схеми") {
                ListRow(
                    "Хто першим",
                    subtitle = "Розставте черговість проїзду",
                    onClick = onOpenPriority,
                    leading = { IconTile(Icons.Rounded.Traffic, c.purple) },
                )
                ListRow(
                    "Регулювальник",
                    subtitle = "Що означають сигнали руками",
                    showDivider = true,
                    onClick = onOpenOfficer,
                    leading = { IconTile(Icons.Rounded.Groups, c.teal) },
                )
            }
        }
    }
}

@Composable
private fun StepRow(index: Int, step: ExplainStep, active: Boolean) {
    val c = ZnakyTheme.colors
    val bg by animateColorAsState(if (active) c.primaryTint else c.surface, label = "step")
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(bg).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        NumberBadge("${index + 1}", if (active) c.primary else c.surfaceMuted, if (active) c.onPrimary else c.textSecondary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(step.title, style = ZnakyType.bodyStrong, color = c.textPrimary)
            Text(step.text, style = ZnakyType.subhead, color = c.textSecondary)
        }
    }
}

/** A traffic light at the near-right corner showing green for our direction. */
private fun DrawScope.drawTrafficLight() {
    val w = size.width
    val h = size.height
    val tl = Offset(w * 0.62f, h * 0.63f)
    val box = Size(w * 0.05f, h * 0.12f)
    drawRoundRect(Color(0xFF1F2933), tl, box, CornerRadius(w * 0.012f))
    val r = w * 0.014f
    val cx = tl.x + box.width / 2
    drawCircle(Color(0xFF5A1D1D), r, Offset(cx, tl.y + box.height * 0.2f))
    drawCircle(Color(0xFF5A4A1D), r, Offset(cx, tl.y + box.height * 0.5f))
    drawCircle(SceneColors.Go, r, Offset(cx, tl.y + box.height * 0.8f))
}
