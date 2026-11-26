package com.znaky.feature.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricMoped
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.znaky.core.art.ArtCanvas
import com.znaky.core.art.RoadSigns
import com.znaky.core.art.layer
import com.znaky.core.designsystem.art.drawArt
import com.znaky.core.designsystem.art.rememberArtCanvas
import com.znaky.core.designsystem.component.LinearBar
import com.znaky.core.designsystem.component.ZButton
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType
import com.znaky.core.domain.repository.ExamHorizon
import com.znaky.core.model.LicenseCategory
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.serialization.Serializable

/** First launch: welcome, licence category, exam date, daily reminder. */
@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingScreen(onDone: () -> Unit, onRequestNotifications: () -> Unit) {
    composable<OnboardingRoute> {
        OnboardingScreen(onDone, onRequestNotifications)
    }
}

@Composable
internal fun OnboardingScreen(onDone: () -> Unit, onRequestNotifications: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onDone() }
    BackHandler(enabled = state.step > 0) { viewModel.back() }
    val c = ZnakyTheme.colors
    Box(Modifier.fillMaxSize().background(c.background)) {
        AnimatedContent(state.step, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) }, label = "onboarding") { step ->
            when (step) {
                0 -> Welcome(onStart = viewModel::next)
                1 -> StepPage(1, onBack = viewModel::back, onSkip = { viewModel.finish(remind = false) }, button = "Далі", onButton = viewModel::next) {
                    CategoryStep(state.category, viewModel::selectCategory)
                }
                2 -> StepPage(2, onBack = viewModel::back, onSkip = { viewModel.finish(remind = false) }, button = "Далі", onButton = viewModel::next) {
                    DateStep(state.horizon, viewModel::selectHorizon)
                }
                else -> StepPage(
                    3,
                    onBack = viewModel::back,
                    onSkip = { viewModel.finish(remind = false) },
                    button = "Нагадувати о ${"%02d".format(state.reminderHour)}:00",
                    onButton = {
                        onRequestNotifications()
                        viewModel.finish(remind = true)
                    },
                    secondary = "Не зараз",
                    onSecondary = { viewModel.finish(remind = false) },
                ) {
                    ReminderStep(state.reminderHour, viewModel::selectHour)
                }
            }
        }
    }
}

@Composable
private fun Welcome(onStart: () -> Unit) {
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        WelcomeScene(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)))
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Права —\nз першої спроби",
                style = ZnakyType.largeTitle,
                color = c.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                "Уся офіційна програма теорії, питання ГСЦ МВС, пояснення простими словами і 3D-тренажери",
                style = ZnakyType.callout,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            PageDots(0)
            ZButton("Почати", onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        }
    }
}

@Composable
private fun PageDots(active: Int) {
    val c = ZnakyTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        repeat(4) { i ->
            Box(Modifier.size(width = if (i == active) 22.dp else 7.dp, height = 7.dp).clip(CircleShape).background(if (i == active) c.primary else c.surfaceMuted))
        }
    }
}

/**
 * Sky, sun, city and hills; a car drives to the right past road signs. The camera follows the car:
 * the car stays put with a light bounce while lane dashes, signs and speed streaks slide left,
 * and the wheels turn forward, so the motion always reads nose-first. Loops seamlessly.
 */
@Composable
private fun WelcomeScene(modifier: Modifier) {
    val c = ZnakyTheme.colors
    val dark = c.isDark
    val transition = rememberInfiniteTransition(label = "welcome")
    val drive by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "drive")
    val glow by transition.animateFloat(0.85f, 1.1f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "sun")
    val art = rememberArtCanvas()
    Box(modifier.semantics { contentDescription = "Синє авто їде дорогою вперед повз дорожні знаки" }) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(Brush.verticalGradient(if (dark) listOf(Color(0xFF14233D), Color(0xFF263A5C)) else listOf(Color(0xFFCFE0FB), Color(0xFFEAF1FC))))
            // Sun (moon at night) with a breathing glow.
            val sun = Offset(w * 0.78f, h * 0.2f)
            drawCircle((if (dark) Color(0xFFE8E2C8) else Color(0xFFFFC53D)).copy(alpha = 0.25f), 46.dp.toPx() * glow, sun)
            drawCircle(if (dark) Color(0xFFE8E2C8) else Color(0xFFFFC53D), 30.dp.toPx(), sun)
            // City skyline.
            val city = if (dark) Color(0xFF34496E) else Color(0xFFB4C8EC)
            val base = h * 0.6f
            listOf(0.05f to 0.10f, 0.13f to 0.16f, 0.22f to 0.09f, 0.3f to 0.13f, 0.46f to 0.11f, 0.56f to 0.18f, 0.66f to 0.08f, 0.74f to 0.14f, 0.86f to 0.12f, 0.93f to 0.17f)
                .forEach { (x, hh) -> drawRect(city, Offset(w * x, base - h * hh), Size(w * 0.07f, h * hh)) }
            // Hills.
            val hills = Path().apply {
                moveTo(0f, base)
                cubicTo(w * 0.2f, base - h * 0.1f, w * 0.4f, base - h * 0.02f, w * 0.55f, base - h * 0.06f)
                cubicTo(w * 0.75f, base - h * 0.12f, w * 0.9f, base - h * 0.03f, w, base - h * 0.05f)
                lineTo(w, h * 0.72f); lineTo(0f, h * 0.72f); close()
            }
            drawPath(hills, if (dark) Color(0xFF2C4A35) else Color(0xFFBFE0A8))
            listOf(0.04f, 0.27f, 0.68f, 0.96f).forEach { x ->
                drawLine(Color(0xFF6B5A44), Offset(w * x, base - 4.dp.toPx()), Offset(w * x, base + 8.dp.toPx()), 2.dp.toPx())
                drawCircle(if (dark) Color(0xFF3F7346) else Color(0xFF6DB061), 9.dp.toPx(), Offset(w * x, base - 8.dp.toPx()))
            }
            // Road.
            val roadTop = h * 0.72f
            drawRect(if (dark) Color(0xFF2E353F) else Color(0xFF9AA3AF), Offset(0f, roadTop), Size(w, h - roadTop))
            drawRect(if (dark) Color(0xFF3C4450) else Color(0xFFDDE1E6), Offset(0f, roadTop - 6.dp.toPx()), Size(w, 6.dp.toPx()))
            val laneY = roadTop + (h - roadTop) * 0.45f

            // One loop moves the world left by `travel`; the dash period and the wheel turn are fitted
            // to it so the loop restarts without a jump.
            val travel = w + 120.dp.toPx()
            val dashes = maxOf(1, (travel / 30.dp.toPx()).roundToInt())
            val period = travel / dashes
            val shift = travel * drive
            drawLine(
                Color.White.copy(alpha = 0.85f),
                Offset(0f, laneY),
                Offset(w, laneY),
                3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(period * 0.6f, period * 0.4f), shift % period),
            )
            // Two signs, half a loop apart, come in from the right and pass behind the car.
            val signSize = 40.dp.toPx()
            listOf("2.1" to 0f, "3.29" to 0.5f).forEach { (id, offset) ->
                val sx = w + 60.dp.toPx() - travel * ((drive + offset) % 1f)
                drawLine(Color(0xFF8B95A3), Offset(sx, roadTop - 4.dp.toPx()), Offset(sx, roadTop - 64.dp.toPx()), 3.dp.toPx())
                drawArt(art) {
                    val canvas = this
                    canvas.layer {
                        translate(sx - signSize / 2, roadTop - 64.dp.toPx() - signSize * 0.8f)
                        scale(signSize / 100f)
                        RoadSigns.draw(object : ArtCanvas by canvas {
                            override val width = 100f
                            override val height = 100f
                        }, id)
                    }
                }
            }

            // The car keeps its place a little left of centre, nose to the right.
            val carW = minOf(190.dp.toPx(), w * 0.5f)
            val v = carW / 240f
            val carX = w * 0.42f - carW / 2
            val bounce = sin(drive * 2 * PI.toFloat() * 12) * 1.dp.toPx()
            // Wheel bottoms (y = 91 in the car's box) sit just below the lane line.
            val carY = laneY + 12.dp.toPx() - 91 * v + bounce
            // Speed streaks trail behind the rear bumper and fade as they slide away.
            val streak = Color.White.copy(alpha = if (dark) 0.35f else 0.8f)
            listOf(Triple(38f, 0f, 1f), Triple(52f, 0.33f, 0.7f), Triple(64f, 0.66f, 0.85f)).forEach { (dy, lag, len) ->
                val p = (drive * 8 + lag) % 1f
                val alpha = if (p < 0.3f) p / 0.3f else (1f - p) / 0.7f
                val x0 = carX + 8 * v - p * 36.dp.toPx()
                drawLine(
                    streak.copy(alpha = streak.alpha * alpha),
                    Offset(x0, carY + dy * v),
                    Offset(x0 - 26.dp.toPx() * len, carY + dy * v),
                    3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            // A whole number of fifth-turns per loop, so the five spokes line up when it restarts.
            val turns = (travel / (2 * PI.toFloat() * 17 * v) * 5).roundToInt() / 5f
            translate(carX, carY) { sideCar(carW, wheelAngle = drive * turns * 360f, groundOffset = -bounce, beam = dark) }
        }
    }
}

/**
 * A friendly side view of a sedan, [width] wide and about 0.4 × [width] tall, nose to the right:
 * boot and red tail light on the left, raked windscreen, mirror, long bonnet and headlight on the right.
 * [wheelAngle] turns the wheels clockwise (forward when driving right); [groundOffset] keeps the
 * shadow on the road while the body bounces; [beam] lights the road ahead at night.
 * Drawn in a 240 × 100 box, the same car as in the design mockup.
 */
private fun DrawScope.sideCar(width: Float, wheelAngle: Float = 0f, groundOffset: Float = 0f, beam: Boolean = false) {
    val v = width / 240f
    fun o(x: Float, y: Float) = Offset(x * v, y * v)
    if (beam) {
        drawPath(
            Path().apply { moveTo(224 * v, 55 * v); lineTo(340 * v, 34 * v); lineTo(340 * v, 84 * v); close() },
            Brush.horizontalGradient(listOf(Color(0xFFFFE7A0).copy(alpha = 0.6f), Color.Transparent), startX = 224 * v, endX = 340 * v),
        )
    }
    drawOval(Color.Black.copy(alpha = 0.2f), o(14f, 88f + groundOffset / v), Size(216 * v, 12 * v))
    val shell = Path().apply {
        moveTo(16 * v, 72 * v)
        lineTo(14 * v, 58 * v)
        quadraticTo(14 * v, 50 * v, 24 * v, 48 * v)
        lineTo(56 * v, 45 * v)
        lineTo(80 * v, 24 * v)
        quadraticTo(86 * v, 19 * v, 96 * v, 19 * v)
        lineTo(134 * v, 19 * v)
        quadraticTo(144 * v, 19 * v, 152 * v, 26 * v)
        lineTo(172 * v, 44 * v)
        lineTo(208 * v, 48 * v)
        quadraticTo(224 * v, 50 * v, 226 * v, 60 * v)
        lineTo(226 * v, 70 * v)
        quadraticTo(226 * v, 74 * v, 222 * v, 74 * v)
        lineTo(205 * v, 74 * v)
        arcTo(Rect(163 * v, 53 * v, 205 * v, 95 * v), 0f, -180f, false)
        lineTo(83 * v, 74 * v)
        arcTo(Rect(41 * v, 53 * v, 83 * v, 95 * v), 0f, -180f, false)
        lineTo(20 * v, 74 * v)
        close()
    }
    drawPath(shell, Brush.verticalGradient(0f to Color(0xFF6F9BFF), 0.55f to Color(0xFF3A68E0), 1f to Color(0xFF2448B0), startY = 19 * v, endY = 74 * v))
    val trim = Color(0xFF2448B0)
    drawLine(trim.copy(alpha = 0.5f), o(20f, 62f), o(226f, 62f), 1.5f * v)
    drawLine(Color.White.copy(alpha = 0.35f), o(26f, 50f), o(58f, 47f), 2 * v, cap = StrokeCap.Round)
    drawLine(Color.White.copy(alpha = 0.35f), o(174f, 46f), o(208f, 50f), 2 * v, cap = StrokeCap.Round)
    // Windows.
    val glass = Brush.linearGradient(listOf(Color(0xFFE3EEFF), Color(0xFF9DB8E8)), o(66f, 23f), o(164f, 43f))
    drawPath(Path().apply { moveTo(88 * v, 26 * v); quadraticTo(92 * v, 23 * v, 98 * v, 23 * v); lineTo(110 * v, 23 * v); lineTo(110 * v, 43 * v); lineTo(66 * v, 43 * v); close() }, glass)
    drawPath(Path().apply { moveTo(116 * v, 23 * v); lineTo(134 * v, 23 * v); quadraticTo(142 * v, 23 * v, 148 * v, 29 * v); lineTo(164 * v, 43 * v); lineTo(116 * v, 43 * v); close() }, glass)
    drawLine(Color.White.copy(alpha = 0.8f), o(122f, 26f), o(132f, 26f), 2.5f * v, cap = StrokeCap.Round)
    // Door seams, handles and the side mirror.
    drawLine(trim, o(112f, 46f), o(112f, 70f), 1.6f * v)
    drawLine(trim, o(164f, 46f), o(164f, 70f), 1.6f * v)
    drawRoundRect(Color(0xFFDCE6FF), o(148f, 52f), Size(9 * v, 2.5f * v), CornerRadius(1.2f * v))
    drawRoundRect(Color(0xFFDCE6FF), o(96f, 52f), Size(9 * v, 2.5f * v), CornerRadius(1.2f * v))
    drawPath(Path().apply { moveTo(160 * v, 40 * v); lineTo(168 * v, 39 * v); lineTo(169 * v, 44 * v); lineTo(161 * v, 45 * v); close() }, trim)
    // Headlight at the front (right), tail light at the back (left).
    drawPath(Path().apply { moveTo(212 * v, 53 * v); quadraticTo(222 * v, 53 * v, 224 * v, 58 * v); lineTo(214 * v, 59 * v); close() }, Color(0xFFFFF6D0))
    drawRoundRect(Color(0xFFFF5A5F), o(14f, 54f), Size(7 * v, 7 * v), CornerRadius(2 * v))
    drawRoundRect(Color(0xFF1C2A55).copy(alpha = 0.5f), o(20f, 70f), Size(206 * v, 4 * v), CornerRadius(2 * v))
    // Wheels with spokes that turn as the car rolls.
    for (x in floatArrayOf(62f, 184f)) {
        val centre = o(x, 74f)
        drawCircle(Color(0xFF1B1F27), 17 * v, centre)
        drawCircle(Color(0xFFD5DCE4), 10 * v, centre)
        rotate(wheelAngle, centre) {
            repeat(5) { i ->
                val a = i * 72f * PI.toFloat() / 180f
                drawLine(Color(0xFF7B8494), centre, Offset(centre.x + cos(a) * 9 * v, centre.y + sin(a) * 9 * v), 2.6f * v, cap = StrokeCap.Round)
            }
        }
        drawCircle(Color(0xFF7B8494), 3 * v, centre)
    }
}

@Composable
private fun StepPage(
    step: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    button: String,
    onButton: () -> Unit,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = ZnakyTheme.colors
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = "Назад", tint = c.textSecondary)
            }
            LinearBar(step / 3f, Modifier.weight(1f).padding(horizontal = 8.dp), height = 5.dp)
            Text(
                "Пропустити",
                style = ZnakyType.callout.copy(fontWeight = FontWeight.SemiBold),
                color = c.textSecondary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button, onClick = onSkip).padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
        Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ZButton(button, onClick = onButton, modifier = Modifier.fillMaxWidth())
            if (secondary != null) {
                Text(
                    secondary,
                    style = ZnakyType.bodyStrong,
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(10.dp)).clickable(role = Role.Button, onClick = onSecondary).padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    val c = ZnakyTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = ZnakyType.title.copy(fontSize = 28.sp, lineHeight = 34.sp), color = c.textPrimary, modifier = Modifier.semantics { heading() })
        Text(subtitle, style = ZnakyType.callout, color = c.textSecondary)
    }
}

private class CategoryOption(val category: LicenseCategory?, val code: String, val label: String, val icon: ImageVector, val tint: Color)

@Composable
private fun CategoryStep(selected: LicenseCategory, onSelect: (LicenseCategory) -> Unit) {
    val c = ZnakyTheme.colors
    StepTitle("Що будеш водити?", "Покажемо теорію і питання саме для тебе")
    val options = listOf(
        CategoryOption(LicenseCategory.B, "B", "Легковий автомобіль", Icons.Rounded.DirectionsCar, Color(0xFF2F62C9)),
        CategoryOption(LicenseCategory.A, "A", "Мотоцикл", Icons.Rounded.TwoWheeler, Color(0xFFE07A2E)),
        CategoryOption(LicenseCategory.A1, "A1", "Мопед, скутер", Icons.Rounded.ElectricMoped, Color(0xFF2FA66A)),
        CategoryOption(LicenseCategory.BOAT, "Човен", "Катер, гідроцикл", Icons.Rounded.DirectionsBoat, Color(0xFF1C8DB8)),
    )
    options.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { option ->
                val isSelected = option.category == selected
                Box(
                    Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isSelected) c.primaryTint else c.surface)
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) c.primary else Color.Transparent, RoundedCornerShape(22.dp))
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { option.category?.let(onSelect) })
                        .padding(14.dp),
                ) {
                    Icon(option.icon, contentDescription = null, tint = option.tint, modifier = Modifier.size(64.dp).align(Alignment.TopCenter))
                    if (isSelected) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = c.primary, modifier = Modifier.size(24.dp).align(Alignment.TopEnd))
                    }
                    Column(Modifier.align(Alignment.BottomStart)) {
                        Text(option.code, style = ZnakyType.title, color = c.textPrimary, maxLines = 1)
                        Text(option.label, style = ZnakyType.footnote.copy(fontWeight = FontWeight.Normal), color = c.textTertiary, maxLines = 1)
                    }
                }
            }
        }
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(22.dp))
        Text("C · D · BE…", style = ZnakyType.bodyStrong, color = c.textTertiary, modifier = Modifier.weight(1f))
        Text("згодом", style = ZnakyType.footnote, color = c.textTertiary)
    }
    InfoNote(
        when {
            selected.isWater -> "Іспит на судноводія складають у кваліфікаційній комісії Адміністрації судноплавства, з 18 років"
            else -> "Іспит у ТСЦ для кожної категорії: 20 питань, 20 хвилин, не більше 2 помилок"
        },
    )
}

@Composable
private fun InfoNote(text: String) {
    val c = ZnakyTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.successTint).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = c.success, modifier = Modifier.size(18.dp))
        Text(text, style = ZnakyType.footnote, color = c.textPrimary)
    }
}

private val MonthNames = listOf("СІЧЕНЬ", "ЛЮТИЙ", "БЕРЕЗЕНЬ", "КВІТЕНЬ", "ТРАВЕНЬ", "ЧЕРВЕНЬ", "ЛИПЕНЬ", "СЕРПЕНЬ", "ВЕРЕСЕНЬ", "ЖОВТЕНЬ", "ЛИСТОПАД", "ГРУДЕНЬ")

@Composable
private fun DateStep(horizon: ExamHorizon, onSelect: (ExamHorizon) -> Unit) {
    val c = ZnakyTheme.colors
    DateScene(horizon)
    StepTitle("Коли іспит?", "Складемо план так, щоб ти встиг усе")
    ExamHorizon.entries.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { h ->
                val selected = h == horizon
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                        .clip(CircleShape)
                        .background(if (selected) c.primary else c.surface)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(h) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(h.label, style = ZnakyType.bodyStrong, color = if (selected) c.onPrimary else c.textPrimary)
                }
            }
        }
    }
    val (minutes, days) = horizon.plan()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("ТВІЙ ПЛАН", style = ZnakyType.caption, color = c.textTertiary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("$minutes хв", style = ZnakyType.title, color = c.textPrimary)
            Text(
                "на день, $days днів на тиждень. Останній тиждень — пробні іспити.",
                style = ZnakyType.subhead,
                color = c.textSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд").forEachIndexed { i, d ->
                val on = i < days
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(9.dp)).background(if (on) c.primaryTint else c.surfaceMuted),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) Box(Modifier.size(8.dp).clip(CircleShape).background(c.primary))
                    }
                    Text(d, style = ZnakyType.caption, color = c.textTertiary)
                }
            }
        }
    }
}

/** A winding road to a flag with a calendar page showing the exam day. */
@Composable
private fun DateScene(horizon: ExamHorizon) {
    val c = ZnakyTheme.colors
    val day = LocalDate.now().plusDays((horizon.days ?: 45).toLong())
    val transition = rememberInfiniteTransition(label = "date")
    val t by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "car")
    Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(24.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(Brush.linearGradient(if (c.isDark) listOf(Color(0xFF1F2D47), Color(0xFF3A2A1F)) else listOf(Color(0xFFDCE7FA), Color(0xFFFCEBDC))))
            val road = Path().apply {
                moveTo(-10f, h * 0.95f)
                cubicTo(w * 0.25f, h * 0.9f, w * 0.2f, h * 0.55f, w * 0.5f, h * 0.55f)
                cubicTo(w * 0.75f, h * 0.55f, w * 0.7f, h * 0.35f, w * 0.88f, h * 0.38f)
            }
            drawPath(road, if (c.isDark) Color(0xFF55606E) else Color(0xFFB7C0CC), style = Stroke(26.dp.toPx(), cap = StrokeCap.Round))
            drawPath(road, Color.White.copy(alpha = 0.9f), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))))
            // Flag at the end of the road.
            val fx = w * 0.9f
            val fy = h * 0.36f
            drawLine(Color(0xFF14202E), Offset(fx, fy), Offset(fx, fy - 26.dp.toPx()), 2.dp.toPx())
            drawRect(Color(0xFF14202E), Offset(fx, fy - 26.dp.toPx()), Size(12.dp.toPx(), 8.dp.toPx()))
            // A little car moving along the middle part of the road.
            val cx = w * (0.3f + 0.35f * t)
            val cy = h * 0.55f + (if (cx < w * 0.5f) (w * 0.5f - cx) / w * h * 0.2f else -(cx - w * 0.5f) / w * h * 0.1f)
            drawRoundRect(Color(0xFF2F62C9), Offset(cx - 14.dp.toPx(), cy - 7.dp.toPx()), Size(28.dp.toPx(), 14.dp.toPx()), CornerRadius(5.dp.toPx()))
            drawRoundRect(Color(0xFFCFE3FF), Offset(cx - 6.dp.toPx(), cy - 5.dp.toPx()), Size(12.dp.toPx(), 10.dp.toPx()), CornerRadius(3.dp.toPx()))
        }
        // Calendar page.
        Column(
            Modifier.align(Alignment.TopEnd).padding(14.dp).width(92.dp).clip(RoundedCornerShape(14.dp)).background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().background(Color(0xFFE5484D)).padding(vertical = 3.dp), contentAlignment = Alignment.Center) {
                Text(MonthNames[day.monthValue - 1], style = ZnakyType.caption, color = Color.White, maxLines = 1)
            }
            Text(if (horizon.days == null) "?" else "${day.dayOfMonth}", style = ZnakyType.title.copy(fontSize = 30.sp), color = Color(0xFF14202E))
            Text("іспит у ТСЦ", style = ZnakyType.caption.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF56616F), modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
private fun ReminderStep(hour: Int, onSelect: (Int) -> Unit) {
    val c = ZnakyTheme.colors
    ReminderScene(hour)
    StepTitle("Нагадувати щодня?", "Одне коротке нагадування. Без спаму.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(8, 13, 19, 21).forEach { h ->
            val selected = h == hour
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 46.dp)
                    .clip(CircleShape)
                    .background(if (selected) c.primary else c.surface)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(h) }),
                contentAlignment = Alignment.Center,
            ) {
                Text("%02d:00".format(h), style = ZnakyType.bodyStrong, color = if (selected) c.onPrimary else c.textPrimary)
            }
        }
    }
}

/** A traffic light turning green next to a phone showing the reminder. */
@Composable
private fun ReminderScene(hour: Int) {
    val c = ZnakyTheme.colors
    val transition = rememberInfiniteTransition(label = "reminder")
    val phase by transition.animateFloat(0f, 3f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "light")
    Row(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(if (c.isDark) listOf(Color(0xFF1F2D47), Color(0xFF3A2A1F)) else listOf(Color(0xFFDCE7FA), Color(0xFFFCEBDC)))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Canvas(Modifier.size(width = 40.dp, height = 104.dp)) {
            drawRoundRect(Color(0xFF242B35), cornerRadius = CornerRadius(12.dp.toPx()))
            val lit = phase.toInt().coerceIn(0, 2)
            val colors = listOf(Color(0xFFFF4D4D), Color(0xFFFFC233), Color(0xFF2EDB6E))
            colors.forEachIndexed { i, col ->
                val center = Offset(size.width / 2, size.height * (0.2f + 0.3f * i))
                drawCircle(if (i == lit) col else col.copy(alpha = 0.18f), 12.dp.toPx(), center)
            }
        }
        Column(
            Modifier.width(150.dp).height(176.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFF1B2433)).padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("%02d:00".format(hour), style = ZnakyType.title.copy(fontSize = 28.sp, fontWeight = FontWeight.Normal), color = Color.White)
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.92f)).padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color(0xFF2F62C9), modifier = Modifier.size(12.dp))
                    Text(" Знаки · зараз", style = ZnakyType.caption.copy(fontSize = 10.sp), color = Color(0xFF56616F))
                }
                Text("Зелене світло для 10 хвилин", style = ZnakyType.caption, color = Color(0xFF14202E))
                Text("Помилки чекають на повтор", style = ZnakyType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Normal), color = Color(0xFF56616F))
            }
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1F5FBF)), contentAlignment = Alignment.Center) {
            Text("↑", style = ZnakyType.title, color = Color.White)
        }
    }
}

