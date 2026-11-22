package com.znaky.feature.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.znaky.core.designsystem.component.LargeTitle
import com.znaky.core.designsystem.component.SegmentedControl
import com.znaky.core.designsystem.component.TopBar
import com.znaky.core.designsystem.theme.ZnakyTheme
import com.znaky.core.designsystem.theme.ZnakyType

private val Asphalt = Color(0xFF4B5563)
private val Paint = Color.White

@Immutable
private data class Marking(val number: String, val name: String, val rule: String, val draw: DrawScope.() -> Unit)

private fun DrawScope.vLine(xFrac: Float, dash: FloatArray? = null) {
    val x = size.width * xFrac
    drawLine(
        Paint, Offset(x, 0f), Offset(x, size.height), strokeWidth = 5.dp.toPx(),
        pathEffect = dash?.let { PathEffect.dashPathEffect(it.map { v -> v * density }.toFloatArray()) },
    )
}

private val markings = listOf(
    Marking("1.1", "Суцільна лінія", "Розділяє зустрічні потоки й позначає межі смуг у небезпечних місцях. Перетинати заборонено.") { vLine(0.5f) },
    Marking("1.3", "Подвійна суцільна", "Розділяє зустрічні потоки на дорогах із чотирма й більше смугами. Перетинати заборонено.") { vLine(0.46f); vLine(0.54f) },
    Marking("1.5", "Переривчаста лінія", "Розділяє потоки й позначає межі смуг. Перетинати дозволено.") { vLine(0.5f, floatArrayOf(18f, 16f)) },
    Marking("1.6", "Лінія наближення", "Попереджає, що попереду суцільна лінія. Перетинати можна, але готуйтеся до заборони.") { vLine(0.5f, floatArrayOf(26f, 8f)) },
    Marking("1.11", "Суцільна поруч із переривчастою", "Перетинати можна лише з боку переривчастої лінії.") { vLine(0.46f); vLine(0.54f, floatArrayOf(18f, 16f)) },
    Marking("1.14.1", "Пішохідний перехід «зебра»", "Позначає нерегульований пішохідний перехід. Пішоходи на ньому мають перевагу.") {
        val stripe = 12.dp.toPx()
        var x = 16.dp.toPx()
        while (x + stripe < size.width - 16.dp.toPx()) {
            drawRect(Paint, Offset(x, 18.dp.toPx()), Size(stripe, size.height - 36.dp.toPx()))
            x += stripe * 2
        }
    },
)

@Composable
internal fun MarkingsScreen(onBack: () -> Unit) {
    val c = ZnakyTheme.colors
    var tab by rememberSaveable { mutableStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(
        Modifier.fillMaxSize().background(c.background).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "top") { TopBar(onBack = onBack) }
        item(key = "title") { LargeTitle("Розмітка") }
        item(key = "tabs") { SegmentedControl(listOf("Горизонтальна", "Вертикальна"), tab, { tab = it }, Modifier.padding(horizontal = 16.dp)) }
        if (tab == 1) {
            item(key = "vertical") {
                Text("Вертикальна розмітка з’явиться в наступному оновленні.", style = ZnakyType.callout, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
            }
        } else {
            markings.chunked(2).forEachIndexed { row, pair ->
                item(key = "row$row") {
                    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { m ->
                            val open = expanded == m.number
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(c.surface)
                                    .clickable(role = Role.Button) { expanded = if (open) null else m.number },
                            ) {
                                Canvas(
                                    Modifier.fillMaxWidth().height(96.dp).background(Asphalt).semantics { contentDescription = m.name },
                                    onDraw = m.draw,
                                )
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(m.number, style = ZnakyType.footnote, color = c.textPrimary)
                                    Text(m.name, style = ZnakyType.subhead, color = c.textSecondary)
                                    if (open) Text(m.rule, style = ZnakyType.subhead, color = c.textPrimary, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
            item(key = "hint") {
                Text("Торкніться розмітки, щоб побачити, що вона дозволяє і забороняє.", style = ZnakyType.footnote, color = c.textTertiary, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}
