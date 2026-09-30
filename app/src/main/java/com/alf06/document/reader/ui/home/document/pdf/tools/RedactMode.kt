package com.alf06.document.reader.ui.home.document.pdf.tools

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * Marking what to black out: drag over a part of a page to cover it, tap a box to take it off.
 * Boxes are kept relative to their page (0..1), see [com.reader.pdfviewer.tools.PdfTools.redact].
 */
internal class RedactMode(private val overlay: PdfOverlayView, private val onChange: (count: Int) -> Unit) : PdfOverlayView.Mode {

    val boxes = HashMap<Int, MutableList<RectF>>()
    val count: Int get() = boxes.values.sumOf { it.size }

    private var page = -1
    private val start = PointF()
    private val now = PointF()

    override fun onDown(x: Float, y: Float): Boolean {
        page = overlay.pdfView.pageAt(x, y) ?: return false
        start.set(x, y); now.set(x, y)
        return true
    }

    override fun onMove(x: Float, y: Float) {
        val r = overlay.pdfView.pageViewRect(page) ?: return
        now.set(x.coerceIn(r.left, r.right), y.coerceIn(r.top, r.bottom))
    }

    override fun onUp(x: Float, y: Float, tap: Boolean) {
        val r = overlay.pdfView.pageViewRect(page) ?: return
        val list = boxes.getOrPut(page) { ArrayList() }
        if (tap) {
            // a tap on a box takes it off
            val u = (x - r.left) / r.width(); val v = (y - r.top) / r.height()
            list.indexOfLast { it.contains(u, v) }.takeIf { it >= 0 }?.let { list.removeAt(it); onChange(count) }
        } else {
            val box = RectF((min(start.x, now.x) - r.left) / r.width(), (min(start.y, now.y) - r.top) / r.height(),
                (max(start.x, now.x) - r.left) / r.width(), (max(start.y, now.y) - r.top) / r.height())
            if (box.width() > 0.005f && box.height() > 0.003f) { list += box; onChange(count) }
        }
        page = -1
    }

    override fun onCancel() { page = -1 }

    fun clear() { boxes.clear(); onChange(0); overlay.invalidate() }

    private val black = Paint().apply { color = 0xCC000000.toInt() }
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFFE53935.toInt(); strokeWidth = overlay.dp(1.5f) }

    override fun draw(canvas: Canvas) {
        for ((p, list) in boxes) {
            val r = overlay.pdfView.pageViewRect(p) ?: continue
            for (b in list) {
                val v = RectF(r.left + b.left * r.width(), r.top + b.top * r.height(), r.left + b.right * r.width(), r.top + b.bottom * r.height())
                canvas.drawRect(v, black)
                canvas.drawRect(v, edge)
            }
        }
        if (page >= 0) {
            val v = RectF(min(start.x, now.x), min(start.y, now.y), max(start.x, now.x), max(start.y, now.y))
            canvas.drawRect(v, black)
            canvas.drawRect(v, edge)
        }
    }
}
