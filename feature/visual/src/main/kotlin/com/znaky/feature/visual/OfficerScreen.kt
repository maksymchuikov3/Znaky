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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.art.ArtCanvas
import com.znaky.core.art.ArtPath
import com.znaky.core.art.Officer
import com.znaky.core.art.Street
import com.znaky.core.designsystem.art.ArtImage
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.AnswerOption
import com.znaky.core.designsystem.component.AnswerState
import com.znaky.core.designsystem.component.Pill
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

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
    val art = rememberArtCanvas()
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
                    drawArt(art) {
                        Street.crossroads(this, c.isDark)
                        drawAllowed(if (armsOut) listOf(Route.STRAIGHT, Route.RIGHT) else listOf(Route.RIGHT))
                        drawTrackCar(CarTrack("Ви", SceneColors.Me, SceneColors.MeRoof, emptyList(), emptyList()), Offset(Lanes.UP, 0.8f), 0f)
                    }
                }
                ArtImage(null, Modifier.size(70.dp, 110.dp)) { Officer.draw(this, armsOut = time.value < 0.5f, t = time.value) }
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

private fun ArtCanvas.drawAllowed(routes: List<Route>) {
    val up = Lanes.UP * 100f
    val right = Lanes.RIGHTWARD * 100f
    routes.forEach { r ->
        val p = when (r) {
            Route.STRAIGHT -> ArtPath().moveTo(up, 72f).lineTo(up, 12f)
            Route.RIGHT -> ArtPath().moveTo(up, 72f).quadTo(up, right, 70f, right).lineTo(92f, right)
        }
        Street.route(this, p)
    }
}
