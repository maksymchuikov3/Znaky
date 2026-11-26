package com.znaky.feature.car

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.art.CarScenes
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas

@Composable
internal fun TopicScene(kind: SceneKind, dark: Boolean, modifier: Modifier = Modifier) {
    val duration = when (kind) {
        SceneKind.BRAKES -> 5000
        SceneKind.ABS -> 2400
        SceneKind.STOPPING -> 4500
        SceneKind.REGEN -> 6000
        SceneKind.CHARGING -> 4000
        SceneKind.HIGH_VOLTAGE -> 3000
    }
    val time = if (duration > 0 && !reducedMotion()) loopTime(duration) else remember { mutableFloatStateOf(0.6f) }
    val art = rememberArtCanvas()
    val background = when (kind) {
        SceneKind.REGEN, SceneKind.CHARGING, SceneKind.HIGH_VOLTAGE -> Brush.verticalGradient(listOf(Color(0xFF0F2A24), Color(0xFF12382F)))
        else -> if (dark) Brush.verticalGradient(listOf(Color(0xFF1C2733), Color(0xFF141D27)))
        else Brush.verticalGradient(listOf(Color.White, Color(0xFFEEF2F7)))
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(background)
            .semantics { contentDescription = describe(kind) },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawArt(art) { CarScenes.draw(this, CarScenes.Kind.valueOf(kind.name), time.value, dark) }
        }
    }
}

@Composable
private fun loopTime(durationMillis: Int): State<Float> =
    rememberInfiniteTransition(label = "scene").animateFloat(
        0f, 1f, infiniteRepeatable(tween(durationMillis, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )

/** Honour the system "remove animations" setting: scenes then show one still frame. */
@Composable
private fun reducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

private fun describe(kind: SceneKind) = when (kind) {
    SceneKind.BRAKES -> "Анімована схема: педаль натискається, рідина йде трубкою до супорта, колодки стискають диск, і колесо сповільнюється."
    SceneKind.ABS -> "Анімація: під час гальмування з ABS тиск у гальмах швидко пульсує, і колесо не блокується."
    SceneKind.STOPPING -> "Три автомобілі гальмують на сухому асфальті, мокрому асфальті і льоду. На льоду гальмівний шлях найдовший."
    SceneKind.REGEN -> "Анімація електромобіля збоку. Під час розгону енергія йде з батареї до мотора. Коли водій відпускає педаль, мотор гальмує колеса і повертає енергію в батарею."
    SceneKind.CHARGING -> "Анімація: електромобіль заряджається від станції, батарея наповнюється з 20 до 80 відсотків."
    SceneKind.HIGH_VOLTAGE -> "Схема електромобіля: батарея в підлозі та помаранчеві високовольтні кабелі до мотора."
}
