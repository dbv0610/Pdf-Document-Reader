package com.wxiwei.office.editor.word

import android.graphics.Rect
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.view.PageRoot
import com.wxiwei.office.simpletext.view.IView
import java.text.BreakIterator
import kotlin.math.ceil
import kotlin.math.floor

/** UI-thread helpers. Coordinates are local to Word; ranges returned are [start, end). */
class WordSelection(private val word: Word) {
    constructor(control: IControl) : this(control.getView() as? Word ?: error("Open a Word document first"))
    private val printMode get() = word.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()
    private fun root(): IView? = if (printMode) word.getPrintWord().getListView().model as? PageRoot else word.getRoot(word.getCurrentRootType())
    /**
     * Offset of the caret for a touch at ([viewX], [viewY]) in Word view coordinates, or -1. A
     * touch right of a paragraph's last line (or below the text) puts it before that paragraph's
     * mark, never after the document's last mark.
     */
    fun offsetAt(viewX: Float, viewY: Float): Long {
        val raw = rawOffsetAt(viewX, viewY)
        if (raw <= 0 || (raw and com.wxiwei.office.constant.wp.WPModelConstant.AREA_MASK) != com.wxiwei.office.constant.wp.WPModelConstant.MAIN) return raw
        val doc = word.getDocument()
        var o = minOf(raw, doc.getAreaEnd(0) - 1).coerceAtLeast(0)
        if (o > 0 && doc.getText(o - 1, o) == "\n") {
            val before = caretRect(o - 1)
            val here = caretRect(o)
            if (before != null && viewY >= before.top && viewY < before.bottom && (here == null || viewY < here.top || viewY >= here.bottom)) o -= 1
        }
        return o
    }

    private fun rawOffsetAt(viewX: Float, viewY: Float): Long {
        val z = word.getZoom()
        if (printMode) {
            val list = word.getPrintWord().getListView()
            val origin = IntArray(2); val local = IntArray(2)
            word.getLocationOnScreen(origin); list.getLocationOnScreen(local)
            val x = viewX + origin[0] - local[0]; val y = viewY + origin[1] - local[1]
            val item = (0 until list.childCount).map { list.getChildAt(it) }.filterIsInstance<APageListItem>()
                .firstOrNull { x >= it.left && x < it.right && y >= it.top && y < it.bottom } ?: return -1
            val page = (root() as? PageRoot)?.getPageView(item.pageIndex) ?: return -1
            return word.viewToModel(((x - item.left) / z).toInt() + page.getX(), ((y - item.top) / z).toInt() + page.getY(), false)
        }
        return word.viewToModel(((viewX + word.scrollX) / z).toInt(), ((viewY + word.scrollY) / z).toInt(), false)
    }
    fun offsetAt(viewX: Int, viewY: Int) = offsetAt(viewX.toFloat(), viewY.toFloat())

    /**
     * Same as [offsetAt] for a touch in screen coordinates (MotionEvent.rawX/rawY). Prefer this with
     * events from IMainFrame.onEventMethod: the viewer shifts the shared MotionEvent between listeners,
     * so its x/y are not reliably local to Word.
     */
    fun offsetAtScreen(rawX: Float, rawY: Float): Long {
        val origin = IntArray(2)
        word.getLocationOnScreen(origin)
        return offsetAt(rawX - origin[0], rawY - origin[1])
    }
    fun rectsFor(start: Long, end: Long): List<Rect> {
        if (end <= start) return emptyList()
        val root = root() ?: return emptyList()
        val result = ArrayList<Rect>(); val z = word.getZoom()
        var at = start
        while (at < end) {
            val line = root.getView(at, WPViewConstant.LINE_VIEW.toInt(), false) ?: break
            val stop = minOf(end, line.getEndOffset(null))
            if (stop <= at) break
            val a = word.modelToView(at, Rectangle(), false)
            val b = word.modelToView(stop, Rectangle(), true)
            // Match Highlight.draw's line height and paragraph top/bottom spacing.
            val lineRect = com.wxiwei.office.wp.view.WPViewKit.instance().getAbsoluteCoordinate(line, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
            var top = lineRect.y
            var height = line.getLayoutSpan(WPViewConstant.Y_AXIS)
            line.getParentView()?.let { p ->
                if (line.getPreView() == null) { top -= p.getTopIndent(); height += p.getTopIndent() }
                if (line.getNextView() == null) height += p.getBottomIndent()
            }
            val shift = shift(root, line)
            if (shift != null) {
                val (dx, dy) = shift
                result.add(Rect(floor(a.x * z + dx).toInt(), floor(top * z + dy).toInt(),
                    ceil(maxOf(a.x, b.x) * z + dx).toInt(), ceil((top + height) * z + dy).toInt()))
            }
            at = stop
        }
        return result
    }
    /** View offset of model coordinates (times zoom) on [line]'s page, or null when that page is not shown. */
    private fun shift(root: IView, line: IView): Pair<Float, Float>? {
        if (!printMode) return -word.scrollX.toFloat() to -word.scrollY.toFloat()
        val list = word.getPrintWord().getListView()
        var page: IView? = line
        while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
        page ?: return null
        val item = (0 until list.childCount).map { list.getChildAt(it) }.filterIsInstance<APageListItem>().firstOrNull {
            (root as PageRoot).getPageView(it.pageIndex) === page
        } ?: return null
        val origin = IntArray(2); val location = IntArray(2)
        word.getLocationOnScreen(origin); item.getLocationOnScreen(location)
        val z = word.getZoom()
        return location[0] - origin[0] - page.getX() * z to location[1] - origin[1] - page.getY() * z
    }

    /**
     * Caret before [offset] in Word view coordinates: a zero-width rectangle as tall as the text
     * line, or null when the line is not laid out or its page is not shown.
     */
    fun caretRect(offset: Long): Rect? {
        val root = root() ?: return null
        val line = root.getView(offset, WPViewConstant.LINE_VIEW.toInt(), false) ?: return null
        val a = word.modelToView(offset, Rectangle(), false)
        val lineRect = com.wxiwei.office.wp.view.WPViewKit.instance().getAbsoluteCoordinate(line, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
        val (dx, dy) = shift(root, line) ?: return null
        val z = word.getZoom()
        val x = floor(a.x * z + dx).toInt()
        return Rect(x, floor(lineRect.y * z + dy).toInt(), x, ceil((lineRect.y + line.getLayoutSpan(WPViewConstant.Y_AXIS)) * z + dy).toInt())
    }

    /**
     * Bottom of the text area of the page holding [offset] (above its bottom margin and footer),
     * in Word view coordinates; null when that page is not laid out or shown.
     */
    fun bodyBottomAt(offset: Long): Int? {
        val root = root() ?: return null
        val line = root.getView(offset, WPViewConstant.LINE_VIEW.toInt(), false) ?: return null
        var page: IView? = line
        while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
        page ?: return null
        val (_, dy) = shift(root, line) ?: return null
        val pageRect = com.wxiwei.office.wp.view.WPViewKit.instance().getAbsoluteCoordinate(page, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
        return floor((pageRect.y + page.getHeight() - page.getBottomIndent()) * word.getZoom() + dy).toInt()
    }

    /**
     * Scrolls the page view so the caret before [offset] is at least [margin] px inside the
     * visible area, which ends [visibleBottom] px below the top of the Word view (above a keyboard
     * or toolbar covering it). Only the page view scrolls; returns true when it moved.
     */
    fun revealCaret(offset: Long, margin: Int, visibleBottom: Int = word.height): Boolean {
        if (printMode || word.getCurrentRootType() != WPViewConstant.PAGE_ROOT.toInt()) return false
        val r = caretRect(offset) ?: return false
        val dy = when {
            r.bottom > visibleBottom - margin -> r.bottom - (visibleBottom - margin)
            r.top < margin -> r.top - margin
            else -> 0
        }
        val dx = when {
            r.left > word.width - margin -> r.left - (word.width - margin)
            r.left < margin -> r.left - margin
            else -> 0
        }
        if (dx == 0 && dy == 0) return false
        word.scrollTo(maxOf(0, word.scrollX + dx), maxOf(0, word.scrollY + dy))
        return true
    }

    fun setSelection(start: Long, end: Long) {
        require(start >= 0 && end >= start)
        word.getHighlight().addHighlight(start, end); repaint()
    }
    fun clearSelection() { word.getHighlight().removeHighlight(); repaint() }
    private fun repaint() {
        word.invalidate()
        if (printMode) { val list = word.getPrintWord().getListView(); for (i in 0 until list.childCount) list.getChildAt(i).invalidate() }
    }
    fun selectedText(): String = word.getHighlight().getSelectText().orEmpty()
    fun selection(): LongRange? = word.getHighlight().let { if (it.isSelectText()) it.getSelectStart() until it.getSelectEnd() else null }
    fun wordAt(offset: Long): LongRange {
        val para = word.getDocument().getParagraph(offset) ?: return offset until offset
        val text = para.getText(word.getDocument()).orEmpty()
        if (text.isEmpty()) return offset until offset
        val local = (offset - para.getStartOffset()).toInt().coerceIn(0, text.lastIndex)
        val boundaries = BreakIterator.getWordInstance().apply { setText(text) }
        val start = if (boundaries.isBoundary(local)) local else boundaries.preceding(local)
        val end = boundaries.following(local).let { if (it == BreakIterator.DONE) text.length else it }
        // a paragraph mark or spaces are not a word to select
        if (text.substring(start, end).isBlank()) return offset until offset
        return (para.getStartOffset() + start) until (para.getStartOffset() + end)
    }
}
