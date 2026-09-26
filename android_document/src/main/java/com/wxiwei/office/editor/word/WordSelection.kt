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
    fun offsetAt(viewX: Float, viewY: Float): Long {
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
            var dx = -word.scrollX.toFloat(); var dy = -word.scrollY.toFloat()
            var visible = true
            if (printMode) {
                val list = word.getPrintWord().getListView()
                var page: IView? = line
                while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
                val item = (0 until list.childCount).map { list.getChildAt(it) }.filterIsInstance<APageListItem>().firstOrNull {
                    (root as PageRoot).getPageView(it.pageIndex) === page
                }
                if (item == null || page == null) visible = false else {
                    val origin = IntArray(2); val location = IntArray(2)
                    word.getLocationOnScreen(origin); item.getLocationOnScreen(location)
                    dx = location[0] - origin[0] - page.getX() * z
                    dy = location[1] - origin[1] - page.getY() * z
                }
            }
            if (visible) result.add(Rect(floor(a.x * z + dx).toInt(), floor(top * z + dy).toInt(),
                ceil(maxOf(a.x, b.x) * z + dx).toInt(), ceil((top + height) * z + dy).toInt()))
            at = stop
        }
        return result
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
        return (para.getStartOffset() + start) until (para.getStartOffset() + end)
    }
}
