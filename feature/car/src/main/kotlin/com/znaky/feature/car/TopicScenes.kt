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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** Scene canvas is designed on a 358 × 250 grid and scaled to the real width. */
private const val GRID_W = 358f
private const val GRID_H = 250f

@Composable
internal fun TopicScene(kind: SceneKind, dark: Boolean, modifier: Modifier = Modifier) {
    val duration = when (kind) {
        SceneKind.BRAKES -> 5000
        SceneKind.ABS -> 2400
        SceneKind.STOPPING -> 4500
        SceneKind.REGEN -> 6000
        SceneKind.CHARGING -> 4000
        SceneKind.HIGH_VOLTAGE -> 0
    }
    val time = if (duration > 0 && !reducedMotion()) loopTime(duration) else remember { mutableFloatStateOf(0.6f) }
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
            val k = size.width / GRID_W
            translate(top = (size.height - GRID_H * k) / 2) {
                scale(k, k, pivot = Offset.Zero) {
                    val t = time.value
                    when (kind) {
                        SceneKind.BRAKES -> drawBrakes(t, dark)
                        SceneKind.ABS -> drawAbs(t, dark)
                        SceneKind.STOPPING -> drawStopping(t, dark)
                        SceneKind.REGEN -> drawRegen(t)
                        SceneKind.CHARGING -> drawCharging(t)
                        SceneKind.HIGH_VOLTAGE -> drawHighVoltage()
                    }
                }
            }
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

/**
 * Piecewise keyframes: [keys] are times 0..1 and [values] the value at each time.
 * Values hold before the first and after the last key, and ease between keys.
 */
private fun kf(t: Float, keys: FloatArray, values: FloatArray): Float {
    if (t <= keys.first()) return values.first()
    if (t >= keys.last()) return values.last()
    var i = 1
    while (t > keys[i]) i++
    val k = ((t - keys[i - 1]) / (keys[i] - keys[i - 1])).coerceIn(0f, 1f)
    val e = k * k * (3 - 2 * k)
    return values[i - 1] + (values[i] - values[i - 1]) * e
}

private fun f(vararg v: Float) = v

private val Ink = Color(0xFF0B1B2B)
private val Steel = Color(0xFF8A97A6)
private val Caliper = Color(0xFFC93A3F)
private val Fluid = Color(0xFFE0A400)
private val Amber = Color(0xFFFFD84D)
private val Mint = Color(0xFF5EEAD4)
private val Battery = Color(0xFF34D058)

private fun DrawScope.drawBrakes(t: Float, dark: Boolean) {
    val pedalAngle = kf(t, f(0f, .3f, .42f, .8f, .9f), f(0f, 0f, 18f, 18f, 0f))
    val pad = kf(t, f(0f, .35f, .45f, .8f, .9f), f(0f, 0f, 7f, 7f, 0f))
    val fluid = kf(t, f(0f, .3f, .45f, .8f, .92f), f(0f, 0f, 1f, 1f, 0f))
    val heat = kf(t, f(0f, .4f, .55f, .78f, .95f), f(0f, 0f, .85f, .85f, 0f))
    val disc = kf(t, f(0f, .4f, .75f), f(0f, 540f, 660f))
    val line = if (dark) Color(0xFF3A4756) else Color(0xFFE3E8EF)

    // Pedal on a hinge.
    drawRoundRect(Steel, Offset(40f, 40f), Size(6f, 30f), CornerRadius(3f))
    rotate(pedalAngle, pivot = Offset(43f, 66f)) {
        drawRoundRect(if (dark) Color(0xFFB9C6D4) else Color(0xFF4A5868), Offset(38f, 66f), Size(10f, 70f), CornerRadius(5f))
        drawRoundRect(if (dark) Color(0xFFE8EEF5) else Ink, Offset(26f, 128f), Size(34f, 14f), CornerRadius(5f))
    }
    // Master cylinder and the brake line, filled up to [fluid].
    drawRoundRect(Color(0xFFC7D3E0), Offset(72f, 84f), Size(46f, 24f), CornerRadius(6f))
    val pipe = Path().apply { moveTo(118f, 96f); lineTo(200f, 96f); quadraticTo(214f, 96f, 214f, 110f); lineTo(214f, 140f) }
    drawPath(pipe, line, style = Stroke(6f, cap = StrokeCap.Round))
    if (fluid > 0.01f) {
        drawPath(pipe, Fluid, style = Stroke(6f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(120f * fluid, 400f))))
    }
    // Disc.
    rotate(disc, pivot = Offset(270f, 160f)) {
        drawCircle(Color(0xFFB8C2CD), 66f, Offset(270f, 160f))
        drawCircle(Color(0xFFCFD6DE), 60f, Offset(270f, 160f))
        drawCircle(Steel, 24f, Offset(270f, 160f))
        listOf(Offset(270f, 112f), Offset(318f, 160f), Offset(270f, 208f), Offset(222f, 160f)).forEach { drawCircle(Steel, 4f, it) }
        drawCircle(Steel, 3f, Offset(304f, 126f)); drawCircle(Steel, 3f, Offset(236f, 194f))
    }
    if (heat > 0.01f) drawCircle(Color(0xFFFF6A3D), 62f, Offset(270f, 160f), alpha = heat, style = Stroke(10f))
    // Caliper with two pads squeezing the disc edge.
    drawRoundRect(Caliper, Offset(196f, 130f), Size(40f, 64f), CornerRadius(10f))
    drawRoundRect(Color(0xFF2B3A4A), Offset(200f + pad, 138f), Size(8f, 48f), CornerRadius(2f))
    drawRoundRect(Color(0xFF2B3A4A), Offset(224f - pad, 138f), Size(8f, 48f), CornerRadius(2f))
}

private fun DrawScope.drawAbs(t: Float, dark: Boolean) {
    val fg = if (dark) Color(0xFFE8EEF5) else Ink
    val grid = if (dark) Color(0xFF3A4756) else Color(0xFFE3E8EF)
    // Wheel keeps turning, a little jerkily, while pressure pulses.
    val pulses = 6
    val spin = 360f * t * 2.2f + 14f * sin(2 * PI.toFloat() * pulses * t)
    rotate(spin, pivot = Offset(86f, 132f)) {
        drawCircle(Color(0xFF111827), 58f, Offset(86f, 132f))
        drawCircle(Color(0xFFCBD5E1), 34f, Offset(86f, 132f))
        for (i in 0 until 5) rotate(i * 72f, pivot = Offset(86f, 132f)) {
            drawRoundRect(Steel, Offset(83f, 100f), Size(6f, 26f), CornerRadius(3f))
        }
        drawCircle(Caliper, 9f, Offset(86f, 132f))
    }
    // Pressure chart: a square-ish wave scrolling right to left.
    val left = 170f; val right = 340f; val top = 70f; val bottom = 200f
    drawLine(grid, Offset(left, bottom), Offset(right, bottom), 2f)
    drawLine(grid, Offset(left, top), Offset(left, bottom), 2f)
    val path = Path()
    val steps = 80
    for (i in 0..steps) {
        val x = left + (right - left) * i / steps
        val phase = (i.toFloat() / steps * 3f + t * 3f) % 1f
        val p = if (phase < 0.55f) 0.85f else 0.35f
        val y = bottom - (bottom - top) * p
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, Caliper, style = Stroke(4f, cap = StrokeCap.Round))
    // Little ABS lamp blinking in time with the pulses.
    val on = (t * pulses) % 1f < 0.5f
    drawRoundRect(if (on) Amber else grid, Offset(300f, 24f), Size(44f, 26f), CornerRadius(8f))
    drawCircle(if (on) Ink else fg.copy(alpha = 0.4f), 6f, Offset(322f, 37f), style = Stroke(2.5f))
}

private fun DrawScope.drawStopping(t: Float, dark: Boolean) {
    val road = if (dark) Color(0xFF2B3542) else Color(0xFFD9DFE6)
    val lanes = listOf(
        Triple("dry", Color(0xFF1E7A3A), 0.42f),
        Triple("wet", Color(0xFF2F6DB5), 0.62f),
        Triple("ice", Color(0xFF6FC6E8), 0.96f),
    )
    val brakeAt = 0.18f
    lanes.forEachIndexed { i, (_, color, stop) ->
        val y = 50f + i * 64f
        drawRoundRect(road, Offset(16f, y - 18f), Size(326f, 36f), CornerRadius(12f))
        if (i == 2) drawRoundRect(Color.White.copy(alpha = 0.35f), Offset(120f, y - 18f), Size(222f, 36f), CornerRadius(12f))
        // Car decelerates uniformly from the brake point to its stopping point.
        val travel = 300f * stop
        val tt = ((t - brakeAt) / (0.8f - brakeAt)).coerceIn(0f, 1f)
        val k = 1f - (1f - tt) * (1f - tt)
        val start = 26f + kf(t, f(0f, brakeAt), f(-40f, 0f))
        val x = if (t < brakeAt) start else 26f + (travel - 30f) * k
        drawLine(Caliper, Offset(56f, y - 22f), Offset(56f, y + 22f), 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
        drawRoundRect(color, Offset(x, y - 11f), Size(36f, 22f), CornerRadius(7f))
        drawRoundRect(Color.White.copy(alpha = 0.6f), Offset(x + 16f, y - 8f), Size(12f, 16f), CornerRadius(4f))
        if (t > brakeAt) drawLine(Ink.copy(alpha = 0.25f), Offset(56f, y + 8f), Offset(x, y + 8f), 3f)
    }
}

private fun DrawScope.drawRegen(t: Float) {
    val charging = t in 0.5f..0.95f
    val outAlpha = kf(t, f(0f, .45f, .5f, .95f, 1f), f(1f, 1f, 0f, 0f, 1f))
    // Road dashes move fast while accelerating and slow down while regenerating.
    val roadShift = kf(t, f(0f, .5f, 1f), f(0f, 260f, 380f))
    var x = -roadShift % 52f
    while (x < GRID_W) {
        drawRect(Color.White.copy(alpha = 0.5f), Offset(x, 212f), Size(26f, 4f))
        x += 52f
    }
    drawRect(Color(0xFF0A1E1A), Offset(0f, 214f), Size(GRID_W, 36f))
    // Car body, glass, battery floor and rear motor.
    val body = Path().apply {
        moveTo(48f, 190f); quadraticTo(48f, 166f, 74f, 160f); lineTo(120f, 152f); quadraticTo(150f, 118f, 196f, 116f)
        lineTo(236f, 116f); quadraticTo(270f, 118f, 292f, 150f); lineTo(318f, 158f); quadraticTo(334f, 164f, 334f, 184f)
        lineTo(334f, 196f); lineTo(48f, 196f); close()
    }
    drawPath(body, Color(0xFFE8EEF5))
    val glass = Path().apply { moveTo(134f, 152f); quadraticTo(156f, 126f, 192f, 124f); lineTo(232f, 124f); quadraticTo(258f, 126f, 276f, 152f); close() }
    drawPath(glass, Color(0xFF8DB8E8), alpha = 0.85f)
    drawRoundRect(Color(0xFF1C3B33), Offset(104f, 176f), Size(150f, 18f), CornerRadius(5f))
    drawRoundRect(Battery, Offset(104f, 176f), Size(150f, 18f), CornerRadius(5f), style = Stroke(2f))
    val level = kf(t, f(0f, .5f, .95f, 1f), f(108f, 96f, 110f, 108f))
    drawRoundRect(Battery, Offset(108f, 180f), Size(level, 10f), CornerRadius(3f))
    drawCircle(Color(0xFF2B3A4A), 14f, Offset(292f, 186f))
    drawCircle(Color(0xFF7FD69A), 14f, Offset(292f, 186f), style = Stroke(2f))
    // Energy flow between battery and motor: yellow outwards, mint back in.
    val dash = (t * 7.5f % 1f) * 12f
    val flowColor = if (charging) Mint else Amber
    val phase = if (charging) dash else 12f - dash
    drawLine(flowColor, Offset(254f, 185f), Offset(278f, 185f), 5f, StrokeCap.Round, PathEffect.dashPathEffect(floatArrayOf(6f, 6f), phase))
    // Wheels spin quickly, then slower as regen brakes the car.
    val spin = kf(t, f(0f, .5f, 1f), f(0f, 1000f, 1440f))
    for (cx in listOf(100f, 290f)) rotate(spin, pivot = Offset(cx, 200f)) {
        drawCircle(Color(0xFF111827), 24f, Offset(cx, 200f))
        drawCircle(Color(0xFF9CA3AF), 12f, Offset(cx, 200f))
        drawLine(Color(0xFF4B5563), Offset(cx, 188f), Offset(cx, 212f), 3f)
        drawLine(Color(0xFF4B5563), Offset(cx - 12f, 200f), Offset(cx + 12f, 200f), 3f)
    }
    // Gauge: needle swings from "power" (right, yellow) to "charge" (left, mint).
    val gx = 14f; val gy = 14f
    drawRoundRect(Color.White.copy(alpha = 0.08f), Offset(gx, gy), Size(118f, 84f), CornerRadius(16f))
    val center = Offset(gx + 59f, gy + 58f)
    drawArc(Mint, 180f, 90f, false, Offset(center.x - 40f, center.y - 40f), Size(80f, 80f), style = Stroke(7f, cap = StrokeCap.Round))
    drawArc(Amber, 270f, 90f, false, Offset(center.x - 40f, center.y - 40f), Size(80f, 80f), style = Stroke(7f, cap = StrokeCap.Round))
    val needle = kf(t, f(0f, .4f, .5f, .92f, 1f), f(55f, 55f, -50f, -50f, 55f))
    rotate(needle, pivot = center) {
        drawRoundRect(Color.White, Offset(center.x - 1.5f, center.y - 36f), Size(3f, 36f), CornerRadius(1.5f))
    }
    drawCircle(Color.White, 4f, center)
    drawCircle(if (outAlpha > 0.5f) Amber else Mint, 5f, Offset(gx + 59f, gy + 76f))
}

private fun DrawScope.drawCharging(t: Float) {
    // Charging post on the left, cable with flowing current, battery gauge on the right.
    drawRoundRect(Color(0xFF2B3A4A), Offset(30f, 60f), Size(56f, 150f), CornerRadius(12f))
    drawRoundRect(Battery, Offset(42f, 76f), Size(32f, 22f), CornerRadius(5f))
    val cable = Path().apply { moveTo(86f, 150f); cubicTo(140f, 150f, 130f, 200f, 190f, 196f) }
    drawPath(cable, Color(0xFF111827), style = Stroke(8f, cap = StrokeCap.Round))
    drawPath(cable, Mint, style = Stroke(3f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), -t * 72f)))
    val bx = 190f; val by = 120f
    drawRoundRect(Color(0xFF1C3B33), Offset(bx, by), Size(140f, 72f), CornerRadius(14f))
    drawRoundRect(Battery, Offset(bx, by), Size(140f, 72f), CornerRadius(14f), style = Stroke(3f))
    drawRoundRect(Battery, Offset(bx + 140f, by + 24f), Size(8f, 24f), CornerRadius(3f))
    val level = kf(t, f(0f, .1f, .85f, 1f), f(.2f, .2f, .8f, .8f))
    drawRoundRect(Battery, Offset(bx + 8f, by + 8f), Size(124f * level, 56f), CornerRadius(8f))
    // Bolt.
    val bolt = Path().apply { moveTo(bx + 76f, by + 14f); lineTo(bx + 58f, by + 40f); lineTo(bx + 70f, by + 40f); lineTo(bx + 64f, by + 60f); lineTo(bx + 84f, by + 32f); lineTo(bx + 72f, by + 32f); close() }
    drawPath(bolt, Amber)
}

private fun DrawScope.drawHighVoltage() {
    val car = Path().apply {
        moveTo(40f, 180f); quadraticTo(40f, 150f, 70f, 144f); lineTo(118f, 136f); quadraticTo(150f, 96f, 196f, 94f)
        lineTo(236f, 94f); quadraticTo(272f, 96f, 296f, 134f); lineTo(320f, 142f); quadraticTo(336f, 150f, 336f, 172f)
        lineTo(336f, 186f); lineTo(40f, 186f); close()
    }
    drawPath(car, Color.White.copy(alpha = 0.12f))
    drawPath(car, Color.White.copy(alpha = 0.5f), style = Stroke(2f))
    drawRoundRect(Battery, Offset(96f, 164f), Size(170f, 18f), CornerRadius(5f))
    val orange = Color(0xFFFF8A1F)
    val hv = Path().apply { moveTo(266f, 172f); cubicTo(290f, 172f, 280f, 150f, 300f, 150f) }
    drawPath(hv, orange, style = Stroke(6f, cap = StrokeCap.Round))
    drawCircle(Color(0xFF2B3A4A), 14f, Offset(306f, 160f))
    drawCircle(orange, 14f, Offset(306f, 160f), style = Stroke(2f))
    // Warning triangle.
    val tri = Path().apply { moveTo(179f, 20f); lineTo(214f, 80f); lineTo(144f, 80f); close() }
    drawPath(tri, Amber)
    val bolt = Path().apply { moveTo(183f, 38f); lineTo(172f, 58f); lineTo(180f, 58f); lineTo(175f, 74f); lineTo(188f, 52f); lineTo(180f, 52f); close() }
    drawPath(bolt, Ink)
}
