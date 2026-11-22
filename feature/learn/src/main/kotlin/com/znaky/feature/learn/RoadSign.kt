package com.znaky.feature.learn

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.znaky.core.designsystem.theme.SignColors

/** One drawing instruction of a sign, in a 62×62 box (the same grid as the mockup). */
@Immutable
internal sealed interface Mark {
    data class Shape(val d: String, val fill: Color? = null, val stroke: Color? = null, val width: Float = 0f) : Mark
    data class Circle(val cx: Float, val cy: Float, val r: Float, val fill: Color? = null, val stroke: Color? = null, val width: Float = 0f) : Mark
    data class Label(val text: String, val cx: Float, val cy: Float, val size: Float, val color: Color) : Mark
}

private sealed interface Prepared {
    class P(val path: Path, val mark: Mark.Shape) : Prepared
    class C(val mark: Mark.Circle) : Prepared
    class T(val mark: Mark.Label) : Prepared
}

/**
 * Draws a road sign from [marks]. Paths are parsed once per sign and drawn in the draw
 * phase only, so long lists of signs scroll without re-parsing anything.
 */
@Composable
internal fun RoadSign(marks: List<Mark>, description: String, modifier: Modifier = Modifier) {
    val prepared = remember(marks) {
        marks.map {
            when (it) {
                is Mark.Shape -> Prepared.P(PathParser().parsePathString(it.d).toPath(), it)
                is Mark.Circle -> Prepared.C(it)
                is Mark.Label -> Prepared.T(it)
            }
        }
    }
    val measurer = rememberTextMeasurer()
    Canvas(modifier.semantics { contentDescription = description }) {
        val k = size.minDimension / 62f
        scale(k, pivot = Offset.Zero) {
            prepared.forEach { p ->
                when (p) {
                    is Prepared.P -> {
                        p.mark.fill?.let { drawPath(p.path, it, style = Fill) }
                        p.mark.stroke?.let { drawPath(p.path, it, style = Stroke(p.mark.width, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                    }
                    is Prepared.C -> {
                        val m = p.mark
                        m.fill?.let { drawCircle(it, m.r, Offset(m.cx, m.cy)) }
                        m.stroke?.let { drawCircle(it, m.r, Offset(m.cx, m.cy), style = Stroke(m.width)) }
                    }
                    is Prepared.T -> Unit
                }
            }
        }
        // Text is measured at real size (not scaled) so it stays crisp.
        prepared.forEach { p ->
            if (p is Prepared.T) {
                val m = p.mark
                val layout = measurer.measure(m.text, TextStyle(fontSize = (m.size * k).toSp(), fontWeight = FontWeight.Bold, color = m.color))
                drawText(layout, topLeft = Offset(m.cx * k - layout.size.width / 2f, m.cy * k - layout.size.height / 2f))
            }
        }
    }
}

internal object SignArt {
    private val R = SignColors.Red
    private val B = SignColors.Blue
    private val W = SignColors.White
    private val K = SignColors.Black
    private val Y = SignColors.Yellow
    private val G = Color(0xFF1E7A3A)

    private fun warning(vararg glyph: Mark) = listOf(
        Mark.Shape("M31 4 L59 54 H3 Z", fill = W, stroke = R, width = 6f),
    ) + glyph
    private fun prohibitory(vararg inner: Mark) = listOf(Mark.Circle(31f, 31f, 27f, fill = W, stroke = R, width = 7f)) + inner
    private fun mandatory(vararg inner: Mark) = listOf(Mark.Circle(31f, 31f, 30f, fill = B)) + inner

    val byCode: Map<String, List<Mark>> = mapOf(
        "1.1" to warning(Mark.Shape("M26 47 V35 Q26 29 32 29 H36 M33 24 L38 29 L33 34", stroke = K, width = 3.5f)),
        "1.2" to warning(Mark.Shape("M36 47 V35 Q36 29 30 29 H26 M29 24 L24 29 L29 34", stroke = K, width = 3.5f)),
        "1.22" to warning(
            Mark.Circle(31f, 24f, 3f, fill = K),
            Mark.Shape("M31 28 V38 M31 31 L25 35 M31 31 L37 34 M31 38 L27 46 M31 38 L35 46", stroke = K, width = 2.6f),
            Mark.Shape("M14 49 H48", stroke = K, width = 2f),
        ),
        "1.23" to warning(
            Mark.Circle(26f, 25f, 2.6f, fill = K), Mark.Circle(36f, 29f, 2.2f, fill = K),
            Mark.Shape("M26 28 V37 M26 30 L22 34 M26 37 L23 45 M26 37 L29 45 M36 32 V39 M36 39 L34 45 M36 39 L38 45 M26 30 L36 34", stroke = K, width = 2.4f),
        ),
        "2.1" to listOf(Mark.Shape("M3 6 H59 L31 56 Z", fill = W, stroke = R, width = 6f)),
        "2.2" to listOf(
            Mark.Shape("M19 2 H43 L60 19 V43 L43 60 H19 L2 43 V19 Z", fill = R),
            Mark.Shape("M21 5 H41 L57 21 V41 L41 57 H21 L5 41 V21 Z", stroke = W, width = 1.5f),
            Mark.Label("STOP", 31f, 31f, 15f, W),
        ),
        "2.3" to listOf(Mark.Shape("M31 1 L61 31 L31 61 L1 31 Z", fill = W, stroke = K, width = 1f), Mark.Shape("M31 11 L51 31 L31 51 L11 31 Z", fill = Y)),
        "2.4" to listOf(
            Mark.Shape("M31 1 L61 31 L31 61 L1 31 Z", fill = W, stroke = K, width = 1f),
            Mark.Shape("M31 11 L51 31 L31 51 L11 31 Z", fill = Y),
            Mark.Shape("M17 45 L45 17", stroke = K, width = 3f),
            Mark.Shape("M20 46 L46 20 M16 42 L42 16", stroke = K, width = 1.2f),
        ),
        "2.5" to prohibitory(
            Mark.Shape("M24 46 V24", stroke = K, width = 4f), Mark.Shape("M24 16 L18 25 H30 Z", fill = K),
            Mark.Shape("M38 16 V38", stroke = R, width = 4f), Mark.Shape("M38 47 L32 38 H44 Z", fill = R),
        ),
        "2.6" to listOf(
            Mark.Shape("M7 3 H55 Q59 3 59 7 V55 Q59 59 55 59 H7 Q3 59 3 55 V7 Q3 3 7 3 Z", fill = B),
            Mark.Shape("M24 16 V38", stroke = R, width = 4f), Mark.Shape("M24 47 L18 38 H30 Z", fill = R),
            Mark.Shape("M38 46 V24", stroke = W, width = 4f), Mark.Shape("M38 15 L32 24 H44 Z", fill = W),
        ),
        "3.1" to prohibitory(),
        "3.21" to listOf(Mark.Circle(31f, 31f, 30f, fill = R), Mark.Shape("M11 25 H51 V37 H11 Z", fill = W)),
        "3.25" to prohibitory(
            Mark.Shape("M15 36 V30 L18 24 H25 L28 30 V36 Z", fill = R),
            Mark.Shape("M34 36 V30 L37 24 H44 L47 30 V36 Z", fill = K),
        ),
        "3.29" to prohibitory(Mark.Label("50", 31f, 31f, 22f, K)),
        "3.34" to listOf(Mark.Circle(31f, 31f, 27f, fill = B, stroke = R, width = 7f), Mark.Shape("M14 14 L48 48 M48 14 L14 48", stroke = R, width = 6f)),
        "3.35" to listOf(Mark.Circle(31f, 31f, 27f, fill = B, stroke = R, width = 7f), Mark.Shape("M48 14 L14 48", stroke = R, width = 6f)),
        "4.1" to mandatory(Mark.Shape("M31 48 V22", stroke = W, width = 6f), Mark.Shape("M31 11 L21 25 H41 Z", fill = W)),
        "4.2" to mandatory(Mark.Shape("M22 48 V36 Q22 30 28 30 H36", stroke = W, width = 6f), Mark.Shape("M48 30 L35 20 V40 Z", fill = W)),
        "4.3" to mandatory(Mark.Shape("M40 48 V36 Q40 30 34 30 H26", stroke = W, width = 6f), Mark.Shape("M14 30 L27 20 V40 Z", fill = W)),
        "5.1" to listOf(
            Mark.Shape("M7 3 H55 Q59 3 59 7 V55 Q59 59 55 59 H7 Q3 59 3 55 V7 Q3 3 7 3 Z", fill = G),
            Mark.Shape("M14 50 L26 14 M48 50 L36 14 M31 50 V44 M31 38 V32 M31 26 V20", stroke = W, width = 4f),
            Mark.Shape("M12 52 Q31 40 50 52", stroke = W, width = 3f),
        ),
    )
}
