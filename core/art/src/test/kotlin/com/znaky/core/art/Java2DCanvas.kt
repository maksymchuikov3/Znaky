package com.znaky.core.art

import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import java.awt.RadialGradientPaint
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.Shape as AwtShape

/** Renders art into a [BufferedImage]; used for previews and for tests that the art draws. */
class Java2DCanvas(override val width: Float, override val height: Float, private val density: Float = 2f) : ArtCanvas {
    val image = BufferedImage((width * density).toInt(), (height * density).toInt(), BufferedImage.TYPE_INT_ARGB)
    private val g: Graphics2D = image.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        scale(density.toDouble(), density.toDouble())
    }
    private val stack = ArrayDeque<Pair<AffineTransform, AwtShape?>>()

    override fun save() { stack.addLast(g.transform to g.clip) }
    override fun restore() { val (t, c) = stack.removeLast(); g.transform = t; g.clip = c }
    override fun translate(dx: Float, dy: Float) = g.translate(dx.toDouble(), dy.toDouble())
    override fun scale(sx: Float, sy: Float) = g.scale(sx.toDouble(), sy.toDouble())
    override fun rotate(degrees: Float, px: Float, py: Float) = g.rotate(Math.toRadians(degrees.toDouble()), px.toDouble(), py.toDouble())
    override fun clip(path: ArtPath) = g.clip(awt(path))

    override fun path(path: ArtPath, paint: Paint) = draw(awt(path), paint)
    override fun polygon(xs: FloatArray, ys: FloatArray, count: Int, paint: Paint) {
        val p = Path2D.Float()
        for (i in 0 until count) if (i == 0) p.moveTo(xs[i], ys[i]) else p.lineTo(xs[i], ys[i])
        p.closePath(); draw(p, paint)
    }
    override fun circle(cx: Float, cy: Float, r: Float, paint: Paint) = draw(Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r), paint)
    override fun oval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) = draw(Ellipse2D.Float(left, top, right - left, bottom - top), paint)
    override fun rect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint, radius: Float) =
        draw(RoundRectangle2D.Float(left, top, right - left, bottom - top, radius * 2, radius * 2), paint)
    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint) {
        val p = Path2D.Float(); p.moveTo(x1, y1); p.lineTo(x2, y2); draw(p, paint, forceStroke = true)
    }
    override fun arc(left: Float, top: Float, right: Float, bottom: Float, startDeg: Float, sweepDeg: Float, paint: Paint) =
        draw(Arc2D.Float(left, top, right - left, bottom - top, -startDeg, -sweepDeg, if (paint.stroke > 0) Arc2D.OPEN else Arc2D.PIE), paint)

    override fun text(text: String, cx: Float, cy: Float, size: Float, color: Long, condensed: Boolean) {
        val font = Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(size / 0.72f).let {
            if (condensed) it.deriveFont(AffineTransform.getScaleInstance(0.84, 1.0)) else it
        }
        g.font = font
        val fm = g.fontMetrics
        val w = fm.stringWidth(text) * if (condensed) 0.84f else 1f
        g.color = awtColor(color, 1f)
        g.composite = AlphaComposite.SrcOver
        g.drawString(text, cx - w / 2, cy + size / 2)
    }

    private fun awt(path: ArtPath): Path2D.Float {
        (path.native as? Path2D.Float)?.let { return it }
        val p = Path2D.Float()
        path.replay(object : ArtPath.PathSink {
            override fun moveTo(x: Float, y: Float) = p.moveTo(x, y)
            override fun lineTo(x: Float, y: Float) = p.lineTo(x, y)
            override fun quadTo(x1: Float, y1: Float, x: Float, y: Float) = p.quadTo(x1, y1, x, y)
            override fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) = p.curveTo(x1, y1, x2, y2, x, y)
            override fun close() = p.closePath()
        })
        path.native = p
        return p
    }

    private fun draw(shape: AwtShape, paint: Paint, forceStroke: Boolean = false) {
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, paint.alpha.coerceIn(0f, 1f))
        val sh = paint.shader
        g.paint = when (sh) {
            is Shader.Linear -> LinearGradientPaint(sh.x0, sh.y0, sh.x1 + if (sh.x0 == sh.x1 && sh.y0 == sh.y1) 0.01f else 0f, sh.y1, stops(sh.colors, sh.stops), sh.colors.map { awtColor(it, 1f) }.toTypedArray())
            is Shader.Radial -> RadialGradientPaint(sh.cx, sh.cy, sh.r, stops(sh.colors, sh.stops), sh.colors.map { awtColor(it, 1f) }.toTypedArray())
            null -> awtColor(paint.color, 1f)
        }
        if (paint.stroke > 0f || forceStroke) {
            val cap = when (paint.cap) { Cap.BUTT -> BasicStroke.CAP_BUTT; Cap.ROUND -> BasicStroke.CAP_ROUND; Cap.SQUARE -> BasicStroke.CAP_SQUARE }
            val join = if (paint.roundJoin) BasicStroke.JOIN_ROUND else BasicStroke.JOIN_MITER
            val total = paint.dash?.sum() ?: 1f
            val phase = ((paint.dashPhase % total) + total) % total
            g.stroke = BasicStroke(paint.stroke.coerceAtLeast(0.01f), cap, join, 10f, paint.dash, phase)
            g.draw(shape)
        } else g.fill(shape)
    }

    private fun stops(colors: LongArray, s: FloatArray?) = s ?: FloatArray(colors.size) { it / (colors.size - 1f) }
    private fun awtColor(c: Long, a: Float) = Color(((c shr 16) and 0xFF).toInt(), ((c shr 8) and 0xFF).toInt(), (c and 0xFF).toInt(), (((c shr 24) and 0xFF) * a).toInt())
}
