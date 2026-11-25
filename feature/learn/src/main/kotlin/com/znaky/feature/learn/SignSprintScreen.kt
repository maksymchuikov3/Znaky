package com.znaky.feature.learn

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyType
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ROUND_MILLIS = 4000
private val GameBg = Brush.radialGradient(listOf(Color(0xFF1E3A5C), Color(0xFF0E141B)), radius = 1400f)
private val Amber = Color(0xFFFFB547)
private val Muted = Color(0xFF8DA2B8)
private val Good = Color(0xFF34D058)
private val Bad = Color(0xFFFF6B6B)

private data class Round(val id: Int, val sign: RoadSignInfo, val options: List<String>)

private fun newRound(id: Int, previous: String?): Round {
    val pool = SignCatalog.signs.filter { it.code != previous }
    val sign = pool[Random.nextInt(pool.size)]
    val wrong = SignCatalog.signs.filter { it.code != sign.code }.shuffled().take(3).map { it.name }
    return Round(id, sign, (wrong + sign.name).shuffled())
}

/** "Знак на швидкість": a sign flashes, the timer runs, name it before it disappears. */
@Composable
internal fun SignSprintScreen(onBack: () -> Unit) {
    var round by remember { mutableStateOf(newRound(0, null)) }
    var picked by remember { mutableStateOf<String?>(null) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var streak by rememberSaveable { mutableIntStateOf(0) }
    var best by rememberSaveable { mutableIntStateOf(0) }
    val timer = remember { Animatable(1f) }
    val appear = remember { Animatable(0f) }

    fun finish(answer: String?) {
        if (picked != null) return
        picked = answer ?: ""
        if (answer == round.sign.name) {
            streak += 1
            score += 100 * minOf(streak, 5)
            best = maxOf(best, score)
        } else {
            streak = 0
        }
    }

    LaunchedEffect(round.id) {
        picked = null
        appear.snapTo(0f)
        appear.animateTo(1f, spring(dampingRatio = 0.6f))
        timer.snapTo(1f)
        timer.animateTo(0f, tween(ROUND_MILLIS, easing = LinearEasing))
        finish(null) // time is up
    }
    LaunchedEffect(picked) {
        if (picked != null) {
            timer.stop()
            delay(800)
            round = newRound(round.id + 1, round.sign.code)
        }
    }

    Column(Modifier.fillMaxSize().background(GameBg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack = onBack, close = true) {
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
                Text("Серія $streak", style = ZnakyType.footnote, color = Color.White, modifier = Modifier.padding(start = 4.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Column(Modifier.weight(1f)) {
                Text("РАХУНОК", style = ZnakyType.caption, color = Muted)
                Text("$score", style = ZnakyType.title.copy(fontFeatureSettings = "tnum"), color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("РЕКОРД", style = ZnakyType.caption, color = Muted)
                Text("$best", style = ZnakyType.title.copy(fontFeatureSettings = "tnum"), color = Amber)
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(240.dp)) {
                    val w = 8.dp.toPx()
                    val s = Size(size.width - w, size.height - w)
                    drawArc(Color.White.copy(alpha = 0.1f), 0f, 360f, false, Offset(w / 2, w / 2), s, style = Stroke(w))
                    // Read in the draw phase: the timer animates without recomposing the screen.
                    drawArc(Amber, -90f, 360f * timer.value, false, Offset(w / 2, w / 2), s, style = Stroke(w, cap = StrokeCap.Round))
                }
                RoadSign(
                    round.sign.code,
                    "Знак, який треба назвати",
                    Modifier.size(150.dp).graphicsLayer {
                        val a = appear.value
                        alpha = a.coerceIn(0f, 1f) * (0.35f + 0.65f * timer.value.coerceAtLeast(0.2f))
                        scaleX = 0.6f + 0.4f * a
                        scaleY = 0.6f + 0.4f * a
                    },
                )
            }
        }
        Text(
            when {
                picked == null -> "Встигніть назвати, поки знак не зник"
                picked == round.sign.name -> "Так! ${round.sign.code}"
                else -> "Це ${round.sign.code} «${round.sign.name}»"
            },
            style = ZnakyType.subhead,
            color = if (picked == null) Muted else if (picked == round.sign.name) Good else Bad,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).semantics { liveRegion = LiveRegionMode.Polite },
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            round.options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { option ->
                        val border = when {
                            picked == null -> Color.White.copy(alpha = 0.14f)
                            option == round.sign.name -> Good
                            option == picked -> Bad
                            else -> Color.White.copy(alpha = 0.14f)
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 76.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .border(if (border == Good || border == Bad) 2.dp else 1.dp, border, RoundedCornerShape(20.dp))
                                .clickable(enabled = picked == null, role = Role.Button) { finish(option) }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(option, style = ZnakyType.bodyStrong, color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
