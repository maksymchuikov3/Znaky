package com.znaky.core.designsystem.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.znaky.core.art.ArtCanvas
import com.znaky.core.art.ArtPath
import com.znaky.core.art.Cap
import com.znaky.core.art.Paint
import com.znaky.core.art.Shader

/**
 * Draws core:art illustrations with Compose. Static paths are converted to Compose paths
 * once and cached on the [ArtPath]; per-frame shapes reuse one scratch path.
 */
class ComposeArtCanvas(private val measurer: TextMeasurer?) : ArtCanvas {
    private var scope: DrawScope? = null
    private val scratch = Path()

    private val s: DrawScope get() = checkNotNull(scope) { "ComposeArtCanvas used outside draw()" }

    /** Runs [block] against the current draw scope. */
    fun draw(drawScope: DrawScope, block: ArtCanvas.() -> Unit) {
        scope = drawScope
        try { block() } finally { scope = null }
    }

    override val width: Float get() = s.size.width
    override val height: Float get() = s.size.height

    private val canvas get() = s.drawContext.canvas

    override fun save() = canvas.save()
    override fun restore() = canvas.restore()
    override fun translate(dx: Float, dy: Float) = canvas.translate(dx, dy)
    override fun scale(sx: Float, sy: Float) = canvas.scale(sx, sy)
    override fun rotate(degrees: Float, px: Float, py: Float) {
        canvas.translate(px, py)
        canvas.rotate(degrees)
        canvas.translate(-px, -py)
    }
    override fun clip(path: ArtPath) = canvas.clipPath(compose(path), ClipOp.Intersect)

    override fun path(path: ArtPath, paint: Paint) = drawPath(compose(path), paint)

    override fun polygon(xs: FloatArray, ys: FloatArray, count: Int, paint: Paint) {
        scratch.reset()
        for (i in 0 until count) if (i == 0) scratch.moveTo(xs[i], ys[i]) else scratch.lineTo(xs[i], ys[i])
        scratch.close()
        drawPath(scratch, paint)
    }

    override fun circle(cx: Float, cy: Float, r: Float, paint: Paint) {
        val b = brush(paint)
        if (b != null) s.drawCircle(b, r, Offset(cx, cy), paint.alpha, style(paint))
        else s.drawCircle(color(paint.color), r, Offset(cx, cy), paint.alpha, style(paint))
    }

    override fun oval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        val b = brush(paint)
        if (b != null) s.drawOval(b, Offset(left, top), Size(right - left, bottom - top), paint.alpha, style(paint))
        else s.drawOval(color(paint.color), Offset(left, top), Size(right - left, bottom - top), paint.alpha, style(paint))
    }

    override fun rect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint, radius: Float) {
        val b = brush(paint)
        val tl = Offset(left, top)
        val sz = Size(right - left, bottom - top)
        val cr = CornerRadius(radius)
        if (b != null) s.drawRoundRect(b, tl, sz, cr, paint.alpha, style(paint))
        else s.drawRoundRect(color(paint.color), tl, sz, cr, style(paint), paint.alpha)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint) {
        val b = brush(paint)
        val width = paint.stroke.coerceAtLeast(0.01f)
        if (b != null) s.drawLine(b, Offset(x1, y1), Offset(x2, y2), width, cap(paint.cap), dash(paint), paint.alpha)
        else s.drawLine(color(paint.color), Offset(x1, y1), Offset(x2, y2), width, cap(paint.cap), dash(paint), paint.alpha)
    }

    override fun arc(left: Float, top: Float, right: Float, bottom: Float, startDeg: Float, sweepDeg: Float, paint: Paint) {
        val useCenter = paint.stroke <= 0f
        val b = brush(paint)
        if (b != null) s.drawArc(b, startDeg, sweepDeg, useCenter, Offset(left, top), Size(right - left, bottom - top), paint.alpha, style(paint))
        else s.drawArc(color(paint.color), startDeg, sweepDeg, useCenter, Offset(left, top), Size(right - left, bottom - top), paint.alpha, style(paint))
    }

    override fun text(text: String, cx: Float, cy: Float, size: Float, color: Long, condensed: Boolean) {
        val m = measurer ?: return
        val scope = s
        val layout = m.measure(
            text,
            TextStyle(fontSize = with(scope) { (size / 0.72f).toSp() }, fontWeight = FontWeight.Bold, color = color(color)),
        )
        canvas.save()
        if (condensed) {
            canvas.translate(cx, cy)
            canvas.scale(0.84f, 1f)
            canvas.translate(-cx, -cy)
        }
        scope.drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, cy + size / 2f - layout.firstBaseline))
        canvas.restore()
    }

    private fun drawPath(path: Path, paint: Paint) {
        val b = brush(paint)
        if (b != null) s.drawPath(path, b, paint.alpha, style(paint))
        else s.drawPath(path, color(paint.color), paint.alpha, style(paint))
    }

    private fun compose(path: ArtPath): Path {
        (path.native as? Path)?.let { return it }
        val p = Path()
        path.replay(object : ArtPath.PathSink {
            override fun moveTo(x: Float, y: Float) = p.moveTo(x, y)
            override fun lineTo(x: Float, y: Float) = p.lineTo(x, y)
            override fun quadTo(x1: Float, y1: Float, x: Float, y: Float) = p.quadraticTo(x1, y1, x, y)
            override fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) = p.cubicTo(x1, y1, x2, y2, x, y)
            override fun close() = p.close()
        })
        path.native = p
        return p
    }

    private fun style(p: Paint): DrawStyle =
        if (p.stroke <= 0f) Fill
        else Stroke(p.stroke, cap = cap(p.cap), join = if (p.roundJoin) StrokeJoin.Round else StrokeJoin.Miter, pathEffect = dash(p))

    private fun dash(p: Paint): PathEffect? = p.dash?.let { d ->
        val total = d.sum()
        PathEffect.dashPathEffect(d, ((p.dashPhase % total) + total) % total)
    }

    private fun cap(c: Cap) = when (c) {
        Cap.BUTT -> StrokeCap.Butt
        Cap.ROUND -> StrokeCap.Round
        Cap.SQUARE -> StrokeCap.Square
    }

    private fun brush(p: Paint): Brush? = when (val sh = p.shader) {
        null -> null
        is Shader.Linear -> Brush.linearGradient(*stops(sh.colors, sh.stops), start = Offset(sh.x0, sh.y0), end = Offset(sh.x1, sh.y1))
        is Shader.Radial -> Brush.radialGradient(*stops(sh.colors, sh.stops), center = Offset(sh.cx, sh.cy), radius = sh.r)
    }

    private fun stops(colors: LongArray, stops: FloatArray?): Array<Pair<Float, Color>> =
        Array(colors.size) { (stops?.get(it) ?: (it / (colors.size - 1f))) to color(colors[it]) }

    private fun color(c: Long) = Color(c)
}

/** A remembered adapter for drawing art inside an existing Canvas lambda. */
@Composable
fun rememberArtCanvas(): ComposeArtCanvas {
    val measurer = rememberTextMeasurer()
    return remember(measurer) { ComposeArtCanvas(measurer) }
}

/** Draws [block] into the current draw scope through [canvas]. */
fun DrawScope.drawArt(canvas: ComposeArtCanvas, block: ArtCanvas.() -> Unit) = canvas.draw(this, block)

/** An illustration from core:art filling [modifier]'s bounds. */
@Composable
fun ArtImage(description: String?, modifier: Modifier = Modifier, block: ArtCanvas.() -> Unit) {
    val art = rememberArtCanvas()
    val semantics = if (description != null) Modifier.semantics { contentDescription = description } else Modifier
    Canvas(modifier.then(semantics)) { drawArt(art, block) }
}
