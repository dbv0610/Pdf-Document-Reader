package com.wxiwei.office.editor.slide

import android.graphics.RectF
import com.wxiwei.office.editor.pptx.Point
import com.wxiwei.office.editor.pptx.Rect
import com.wxiwei.office.editor.pptx.PptxShapeInfo
import com.wxiwei.office.pg.control.Presentation
import kotlin.math.roundToLong

/** Coordinates are local to Presentation, not screen coordinates. Normal page drawing uses
 * 96 dpi (9525 EMU/pixel) times zoom, with an integer page origin. Page sizes in the viewer
 * are truncated to pixels, so dividing by the original EMU slide width would drift.
 * Slideshow mapping is for the settled slide, not an animated transition.
 */
object SlideGeometry {
    private data class Mapping(val left: Float, val top: Float, val width: Float, val height: Float, val scale: Double)
    private fun mapping(p: Presentation): Mapping? {
        if (p.getCurrentIndex() < 0 || p.width <= 0 || p.height <= 0) return null
        val zoom = p.getZoom(); if (!zoom.isFinite() || zoom <= 0f) return null
        val size = p.getPageSize() ?: return null
        val width = (size.width * zoom).toInt(); val height = (size.height * zoom).toInt()
        if (width <= 0 || height <= 0) return null
        val origin = if (p.isSlideShow()) {
            // getSlideDrawingRect spans the entire view height; SlideShowView centers actual content.
            (p.getSlideDrawingRect()?.left ?: ((p.width - width) / 2)).toFloat() to ((p.height - height) / 2).toFloat()
        } else {
            val item = p.getPrintMode().getListView()?.getCurrentPageView() ?: return null
            val a = IntArray(2); val b = IntArray(2)
            p.getLocationOnScreen(a); item.getLocationOnScreen(b)
            (b[0] - a[0]).toFloat() to (b[1] - a[1]).toFloat()
        }
        return Mapping(origin.first, origin.second, width.toFloat(), height.toFloat(), zoom.toDouble() / 9525.0)
    }
    fun viewToEmu(presentation: Presentation, viewX: Float, viewY: Float): Point? {
        val m = mapping(presentation) ?: return null
        if (!viewX.isFinite() || !viewY.isFinite() || viewX < m.left || viewY < m.top || viewX >= m.left + m.width || viewY >= m.top + m.height) return null
        return Point(((viewX - m.left) / m.scale).roundToLong(), ((viewY - m.top) / m.scale).roundToLong())
    }
    fun emuToView(presentation: Presentation, rectEmu: Rect): RectF? {
        val m = mapping(presentation) ?: return null
        return RectF((m.left + rectEmu.x * m.scale).toFloat(), (m.top + rectEmu.y * m.scale).toFloat(),
            (m.left + (rectEmu.x.toDouble() + rectEmu.width) * m.scale).toFloat(), (m.top + (rectEmu.y.toDouble() + rectEmu.height) * m.scale).toFloat())
    }
    fun hitTest(shapes: List<PptxShapeInfo>, emuPoint: Point): PptxShapeInfo? = shapes.lastOrNull {
        val r = it.rectEmu
        r.width > 0 && r.height > 0 && emuPoint.x >= r.x && emuPoint.y >= r.y && emuPoint.x.toDouble() < r.x.toDouble() + r.width && emuPoint.y.toDouble() < r.y.toDouble() + r.height
    }
}
