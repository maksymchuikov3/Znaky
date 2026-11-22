package com.znaky.feature.visual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.AnswerOption
import com.znaky.core.designsystem.component.AnswerState
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

private val Vest = Color(0xFFCDEB3A)
private val Uniform = Color(0xFF1F2937)
private val Skin = Color(0xFFF1C7A1)

private val answers = listOf("Прямо і праворуч", "У будь-якому напрямку", "Рух заборонено")
private const val CORRECT = 0

/**
 * Traffic officer scene. The signal alternates: arms out to the sides (you may go straight
 * or right from the side), then right arm forward with the chest towards you (right only).
 */
@Composable
internal fun OfficerScreen(onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    var picked by rememberSaveable { mutableStateOf<Int?>(null) }
    val time = rememberLoopTime(6000)
    // Only the signal phase drives composition; the per-frame value is read in draw lambdas.
    val armsOut by remember { derivedStateOf { time.value < 0.5f } }
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack, close = true, backLabel = "Регулювальник")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("СИГНАЛИ РЕГУЛЮВАЛЬНИКА", style = ZnakyType.caption, color = c.purple)
            Text("Руки витягнуті в сторони. Ви під’їжджаєте збоку. Куди можна їхати?", style = ZnakyType.title2, color = c.textPrimary)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (c.isDark) Color(0xFF14263A) else Color(0xFFD6E6F5))
                    .semantics { contentDescription = "Перехрестя з регулювальником. Сигнал змінюється: руки в сторони, потім права рука вперед." },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(300.dp).graphicsLayer { rotationX = 54f; cameraDistance = 9f * density; scaleX = 1.25f; scaleY = 1.25f }) {
                    drawCrossroads(c.isDark)
                    drawAllowed(if (armsOut) listOf(Route.STRAIGHT, Route.RIGHT) else listOf(Route.RIGHT))
                    drawCar(CarTrack("Ви", SceneColors.Me, SceneColors.MeRoof, emptyList(), emptyList()), Offset(Lanes.UP, 0.8f), 0f)
                }
                Canvas(Modifier.size(70.dp, 110.dp)) { drawOfficer(armsOut = time.value < 0.5f, wave = time.value) }
                Text(
                    if (armsOut) "Руки в сторони" else "Права рука вперед",
                    style = ZnakyType.footnote,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            answers.forEachIndexed { i, a ->
                val state = when {
                    picked == null -> AnswerState.NEUTRAL
                    i == CORRECT -> AnswerState.CORRECT
                    i == picked -> AnswerState.WRONG
                    else -> AnswerState.NEUTRAL
                }
                AnswerOption(i, a, state, enabled = picked == null, onClick = { picked = i })
            }
            if (picked != null) {
                Text(
                    "З боку руки регулювальника дозволено рух прямо і праворуч. Коли права рука витягнута вперед, а ви з боку грудей, можна лише праворуч.",
                    style = ZnakyType.callout,
                    color = c.textSecondary,
                )
                Row { Pill("п. 8.8 ПДР") }
            }
        }
        ZButton(if (picked == null) "Оберіть відповідь" else "Готово", onClick = onBack, enabled = picked != null, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}

private enum class Route { STRAIGHT, RIGHT }

private fun DrawScope.drawAllowed(routes: List<Route>) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(w * 0.028f, cap = StrokeCap.Round)
    routes.forEach { r ->
        val p = Path()
        when (r) {
            Route.STRAIGHT -> { p.moveTo(Lanes.UP * w, 0.72f * h); p.lineTo(Lanes.UP * w, 0.12f * h) }
            Route.RIGHT -> { p.moveTo(Lanes.UP * w, 0.72f * h); p.quadraticTo(Lanes.UP * w, Lanes.RIGHTWARD * h, 0.7f * w, Lanes.RIGHTWARD * h); p.lineTo(0.92f * w, Lanes.RIGHTWARD * h) }
        }
        drawPath(p, SceneColors.Go.copy(alpha = 0.9f), style = stroke)
    }
}

private fun DrawScope.drawOfficer(armsOut: Boolean, wave: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2
    drawOval(Color.Black.copy(alpha = 0.25f), Offset(cx - w * 0.3f, h * 0.92f), androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.07f))
    drawLine(Uniform, Offset(cx - w * 0.08f, h * 0.6f), Offset(cx - w * 0.08f, h * 0.94f), w * 0.1f, StrokeCap.Round)
    drawLine(Uniform, Offset(cx + w * 0.08f, h * 0.6f), Offset(cx + w * 0.08f, h * 0.94f), w * 0.1f, StrokeCap.Round)
    drawRoundRect(Vest, Offset(cx - w * 0.17f, h * 0.24f), androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.4f), androidx.compose.ui.geometry.CornerRadius(w * 0.1f))
    drawCircle(Skin, w * 0.11f, Offset(cx, h * 0.15f))
    drawRect(Uniform, Offset(cx - w * 0.12f, h * 0.04f), androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.07f))
    val shoulderY = h * 0.3f
    val arm = w * 0.38f
    val sw = w * 0.08f
    if (armsOut) {
        drawLine(Uniform, Offset(cx - w * 0.15f, shoulderY), Offset(cx - w * 0.15f - arm, shoulderY), sw, StrokeCap.Round)
        drawLine(Uniform, Offset(cx + w * 0.15f, shoulderY), Offset(cx + w * 0.15f + arm, shoulderY), sw, StrokeCap.Round)
    } else {
        // Right arm points forward (towards the viewer, so it looks short), left arm down.
        val lift = 6f * kotlin.math.sin(wave * 12f)
        rotate(lift, pivot = Offset(cx + w * 0.15f, shoulderY)) {
            drawLine(Uniform, Offset(cx + w * 0.15f, shoulderY), Offset(cx + w * 0.3f, shoulderY + h * 0.1f), sw * 1.3f, StrokeCap.Round)
        }
        drawLine(Uniform, Offset(cx - w * 0.15f, shoulderY), Offset(cx - w * 0.2f, h * 0.6f), sw, StrokeCap.Round)
    }
}
