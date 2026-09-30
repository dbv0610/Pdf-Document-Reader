/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.editor.word

import android.graphics.Rect
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.pagelist.APageListItem
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.wp.view.PageRoot
import com.wxiwei.office.wp.view.PageView
import com.wxiwei.office.wp.view.TitleView
import com.wxiwei.office.wp.view.WPViewKit
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
     * Index of the page whose header or footer is being edited. One header view is laid out and
     * shared by every page, so the caret needs to know which page to show it on; a tap in a
     * header or footer sets it.
     */
    var storyPage = 0

    private fun area(offset: Long) = offset and WPModelConstant.AREA_MASK
    private fun isStory(offset: Long) = area(offset) == WPModelConstant.HEADER || area(offset) == WPModelConstant.FOOTER
    private fun isTextbox(offset: Long) = area(offset) == WPModelConstant.TEXTBOX

    /** The shape of text box [offset] on [page] and its laid-out text. */
    private fun textboxOn(page: PageView, offset: Long): Pair<com.wxiwei.office.wp.view.ShapeView, IView>? {
        val index = ((offset and WPModelConstant.TEXTBOX_MASK) shr 32).toInt()
        for (sv in page.getShapeViews()) {
            val shape = sv as? com.wxiwei.office.wp.view.ShapeView ?: continue
            return shape to (shape.textRoot(index) ?: continue)
        }
        return null
    }

    /** Offset in a text box of [page] under the page-local point, topmost first, or -1. */
    private fun textboxOffsetAt(page: PageView, x: Int, y: Int): Long {
        for (sv in page.getShapeViews().asReversed()) {
            val shape = sv as? com.wxiwei.office.wp.view.ShapeView ?: continue
            val index = shape.getShape()?.let { (it as? com.wxiwei.office.common.shape.WPAutoShape)?.elementIndex } ?: continue
            val root = shape.textRoot(index) ?: continue
            val b = shape.getShape()?.bounds ?: continue
            val lx = x - shape.getX(); val ly = y - shape.getY()
            if (lx < 0 || ly < 0 || lx >= b.width || ly >= b.height) continue
            val offset = root.viewToModel(lx, ly, false)
            if (offset < 0) continue
            storyPage = page.getPageNumber() - 1
            return offset
        }
        return -1
    }
    private fun title(page: PageView, offset: Long): TitleView? = if (area(offset) == WPModelConstant.HEADER) page.getHeader() else page.getFooter()

    /** Offset in the header or footer of [page] under the page-local point ([x], [y]), or -1. */
    private fun storyOffsetAt(page: PageView, x: Int, y: Int): Long {
        for (title in listOf(page.getHeader(), page.getFooter())) {
            title ?: continue
            val ty = y - title.getY()
            if (ty < 0 || ty >= title.getLayoutSpan(WPViewConstant.Y_AXIS)) continue
            val para = WPViewKit.instance().nearestChild(title.getChildView(), ty) { it.getHeight() } ?: continue
            val offset = para.viewToModel(x - title.getX(), ty, false)
            if (offset < 0) continue
            storyPage = page.getPageNumber() - 1
            return offset
        }
        return -1
    }

    /** Where [view] (inside [title]) is in its page: the offsets of its parents up to the title, and the title's. */
    private fun inPage(view: IView?, title: IView): Pair<Int, Int> {
        var x = title.getX(); var y = title.getY()
        var v = view
        while (v != null && v !== title) { x += v.getX(); y += v.getY(); v = v.getParentView() }
        return x to y
    }

    /**
     * The view of [type] holding [offset]. A page hands out a table whole (PageView.getView stops
     * at it): go on down its rows and cells to the line, picture... asked for.
     */
    private fun viewAt(root: IView, offset: Long, type: Short, isBack: Boolean): IView? {
        var v = root.getView(offset, type.toInt(), isBack)
        while (v != null && v.getType() == WPViewConstant.TABLE_VIEW && type != WPViewConstant.TABLE_VIEW) {
            val inner = v.getView(offset, type.toInt(), isBack) ?: return null
            if (inner === v) return null
            v = inner
        }
        return v
    }

    /** A laid-out caret position: its line, the caret x and line top in page-root coordinates, and its page. */
    private class Spot(val line: IView, val x: Int, val top: Int, val left: Int, val page: IView?)

    private fun locate(offset: Long, isBack: Boolean): Spot? {
        val root = root() ?: return null
        if (isTextbox(offset)) {
            val page = (root as? PageRoot)?.getPageView(storyPage) ?: return null
            val (shape, box) = textboxOn(page, offset) ?: return null
            val para = box.getView(offset, WPViewConstant.PARAGRAPH_VIEW.toInt(), isBack) ?: return null
            val line = para.getView(offset, WPViewConstant.LINE_VIEW.toInt(), isBack) ?: return null
            val r = para.modelToView(offset, Rectangle(), isBack) ?: return null
            val (px, py) = inPage(para.getParentView(), box)
            val (lx, ly) = inPage(line, box)
            val ox = page.getX() + shape.getX(); val oy = page.getY() + shape.getY()
            return Spot(line, ox + px + r.x, oy + ly, ox + lx, page)
        }
        if (isStory(offset)) {
            val page = (root as? PageRoot)?.getPageView(storyPage) ?: return null
            val title = title(page, offset) ?: return null
            val para = title.getView(offset, WPViewConstant.PARAGRAPH_VIEW.toInt(), isBack) ?: return null
            val line = para.getView(offset, WPViewConstant.LINE_VIEW.toInt(), isBack) ?: return null
            // ParagraphView.modelToView is relative to the paragraph's parent
            val r = para.modelToView(offset, Rectangle(), isBack) ?: return null
            val (px, py) = inPage(para.getParentView(), title)
            val (lx, ly) = inPage(line, title)
            return Spot(line, page.getX() + px + r.x, page.getY() + ly, page.getX() + lx, page)
        }
        val line = viewAt(root, offset, WPViewConstant.LINE_VIEW, isBack) ?: return null
        val a = word.modelToView(offset, Rectangle(), isBack)
        val lineRect = WPViewKit.instance().getAbsoluteCoordinate(line, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
        var page: IView? = line
        while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
        return Spot(line, a.x, lineRect.y, lineRect.x, page)
    }
    /**
     * Offset of the caret for a touch at ([viewX], [viewY]) in Word view coordinates, or -1. A
     * touch right of a paragraph's last line (or below the text, or in an empty table cell) puts
     * it before that paragraph's mark, never after the document's last mark.
     */
    fun offsetAt(viewX: Float, viewY: Float): Long {
        val raw = rawOffsetAt(viewX, viewY)
        if (raw <= 0 || area(raw) != WPModelConstant.MAIN) return raw
        val doc = word.getDocument()
        var o = minOf(raw, doc.getAreaEnd(0) - 1).coerceAtLeast(0)
        if (o > 0 && doc.getText(o - 1, o) == "\n") {
            val before = caretRect(o - 1)
            val here = caretRect(o)
            // [o] starts the next line below, or the next cell of the same table row, right of the touch
            if (before != null && viewY >= before.top && viewY < before.bottom &&
                (here == null || viewY < here.top || viewY >= here.bottom || viewX < here.left)) o -= 1
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
            val px = ((x - item.left) / z).toInt(); val py = ((y - item.top) / z).toInt()
            textboxOffsetAt(page, px, py).let { if (it >= 0) return it }
            storyOffsetAt(page, px, py).let { if (it >= 0) return it }
            return word.viewToModel(px + page.getX(), py + page.getY(), false)
        }
        val vx = ((viewX + word.scrollX) / z).toInt(); val vy = ((viewY + word.scrollY) / z).toInt()
        // a tap in a page's header or footer box edits it
        (root() as? PageRoot)?.let { pages ->
            for (i in 0 until pages.getPageCount()) {
                val page = pages.getPageView(i) ?: continue
                if (vy < page.getY() || vy >= page.getY() + page.getHeight()) continue
                textboxOffsetAt(page, vx - page.getX(), vy - page.getY()).let { if (it >= 0) return it }
                storyOffsetAt(page, vx - page.getX(), vy - page.getY()).let { if (it >= 0) return it }
                break
            }
        }
        return word.viewToModel(vx, vy, false)
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
            val spot = locate(at, false) ?: break
            val line = spot.line
            val stop = minOf(end, line.getEndOffset(null))
            if (stop <= at) break
            val bx = locate(stop, true)?.takeIf { it.line === line }?.x ?: (spot.left + line.getWidth())
            // Match Highlight.draw's line height and paragraph top/bottom spacing.
            var top = spot.top
            var height = line.getLayoutSpan(WPViewConstant.Y_AXIS)
            line.getParentView()?.let { p ->
                if (line.getPreView() == null) { top -= p.getTopIndent(); height += p.getTopIndent() }
                if (line.getNextView() == null) height += p.getBottomIndent()
            }
            val shift = shift(root, spot.page)
            if (shift != null) {
                val (dx, dy) = shift
                result.add(Rect(floor(spot.x * z + dx).toInt(), floor(top * z + dy).toInt(),
                    ceil(maxOf(spot.x, bx) * z + dx).toInt(), ceil((top + height) * z + dy).toInt()))
            }
            at = stop
        }
        return result
    }
    /** View offset of model coordinates (times zoom) on [page], or null when that page is not shown. */
    private fun shift(root: IView, page: IView?): Pair<Float, Float>? {
        if (!printMode) return -word.scrollX.toFloat() to -word.scrollY.toFloat()
        val list = word.getPrintWord().getListView()
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
        val spot = locate(offset, false) ?: return null
        val (dx, dy) = shift(root, spot.page) ?: return null
        val z = word.getZoom()
        val x = floor(spot.x * z + dx).toInt()
        return Rect(x, floor(spot.top * z + dy).toInt(), x, ceil((spot.top + spot.line.getLayoutSpan(WPViewConstant.Y_AXIS)) * z + dy).toInt())
    }

    /**
     * Bottom of the text area of the page holding [offset] (above its bottom margin and footer),
     * in Word view coordinates; null when that page is not laid out or shown.
     */
    fun bodyBottomAt(offset: Long): Int? {
        if (isStory(offset) || isTextbox(offset)) return null
        val root = root() ?: return null
        val line = viewAt(root, offset, WPViewConstant.LINE_VIEW, false) ?: return null
        var page: IView? = line
        while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
        page ?: return null
        val (_, dy) = shift(root, page) ?: return null
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

    /** A floating picture or shape drawn on a page: its anchor offset and its box in Word view coordinates. */
    class PlacedShape(val offset: Long, val shape: com.wxiwei.office.common.shape.IShape, val rect: Rect)

    private fun placedShapes(): List<PlacedShape> {
        val root = root() as? PageRoot ?: return emptyList()
        val z = word.getZoom()
        val out = ArrayList<PlacedShape>()
        for (i in 0 until root.getPageCount()) {
            val page = root.getPageView(i) ?: continue
            val (dx, dy) = shift(root, page) ?: continue
            for (sv in page.getShapeViews()) {
                val shape = (sv as? com.wxiwei.office.wp.view.ShapeView)?.getShape() ?: (sv as? com.wxiwei.office.wp.view.ObjView)?.getShape() ?: continue
                val b = shape.bounds ?: continue
                val left = (page.getX() + sv.getX()) * z + dx
                val top = (page.getY() + sv.getY()) * z + dy
                out.add(PlacedShape(sv.getStartOffset(null), shape, Rect(floor(left).toInt(), floor(top).toInt(), ceil(left + b.width * z).toInt(), ceil(top + b.height * z).toInt())))
            }
        }
        return out
    }

    /** The floating picture or shape under ([viewX], [viewY]) (Word view coordinates), topmost first, or null. */
    fun floatingShapeAt(viewX: Float, viewY: Float): PlacedShape? =
        placedShapes().lastOrNull { it.rect.contains(viewX.toInt(), viewY.toInt()) }

    /**
     * The box of the in-line picture whose one-char object is at [offset], in Word view
     * coordinates, as it is drawn; null when it is not laid out in a line of the body. (The
     * [rectsFor] box of its char is a caret-wide sliver the height of the line.)
     */
    fun inlineObjectRect(offset: Long): Rect? {
        if (isStory(offset) || isTextbox(offset)) return null
        val root = root() ?: return null
        val obj = viewAt(root, offset, WPViewConstant.OBJ_VIEW, false) as? com.wxiwei.office.wp.view.ObjView ?: return null
        if (!obj.isInline() || obj.getWidth() <= 0 || obj.getHeight() <= 0) return null
        var page: IView? = obj
        while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
        val (dx, dy) = shift(root, page) ?: return null
        val at = WPViewKit.instance().getAbsoluteCoordinate(obj, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
        val z = word.getZoom()
        return Rect(floor(at.x * z + dx).toInt(), floor(at.y * z + dy).toInt(),
            ceil((at.x + obj.getWidth()) * z + dx).toInt(), ceil((at.y + obj.getHeight()) * z + dy).toInt())
    }

    /**
     * The box of the body table holding [start, end) in Word view coordinates: from its part on
     * the page of [start] to its part on the page of [end] - 1; null when it is not laid out.
     */
    fun tableRect(start: Long, end: Long): Rect? {
        if (isStory(start) || isTextbox(start)) return null
        val root = root() ?: return null
        val z = word.getZoom()
        var out: Rect? = null
        for (offset in listOf(start, end - 1)) {
            // the outermost table view holding the offset
            var table: IView? = root.getView(offset, WPViewConstant.TABLE_VIEW.toInt(), false) ?: continue
            var up = table?.getParentView()
            while (up != null) { if (up.getType() == WPViewConstant.TABLE_VIEW) table = up; up = up.getParentView() }
            val t = table ?: continue
            var page: IView? = t
            while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
            val (dx, dy) = shift(root, page) ?: continue
            val at = WPViewKit.instance().getAbsoluteCoordinate(t, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
            val r = Rect(floor(at.x * z + dx).toInt(), floor(at.y * z + dy).toInt(),
                ceil((at.x + t.getWidth()) * z + dx).toInt(), ceil((at.y + t.getHeight()) * z + dy).toInt())
            out = out?.apply { union(r) } ?: r
        }
        return out
    }

    /** A column border of a table: after grid column [index] - 1, at [x]. */
    class ColumnGuide(val index: Int, val x: Int)
    /** A row of a table, from [top] to [bottom]; [start] is the row's first offset. */
    class RowGuide(val start: Long, val top: Int, val bottom: Int)
    /** The borders of a laid-out table that can be dragged, in Word view coordinates. */
    class TableGuides(val left: Int, val top: Int, val right: Int, val bottom: Int, val columns: List<ColumnGuide>, val rows: List<RowGuide>)

    /**
     * The column borders (from its first row part shown) and the rows of the body table holding
     * [start, end), in Word view coordinates; null when it is not laid out. A column border is
     * the grid line after a column: where a merged cell hides it in some rows, the rows showing it
     * give its place.
     */
    fun tableGuides(start: Long, end: Long): TableGuides? {
        if (isStory(start) || isTextbox(start)) return null
        val root = root() ?: return null
        val z = word.getZoom()
        val tableElem = (word.getDocument() as? com.wxiwei.office.wp.model.WPDocument)?.getParagraph0(start) ?: return null
        // every part of the table (one per page it runs over)
        val parts = ArrayList<IView>()
        var offset = start
        while (offset < end) {
            var t: IView? = root.getView(offset, WPViewConstant.TABLE_VIEW.toInt(), false) ?: break
            var up = t?.getParentView()
            while (up != null) { if (up.getType() == WPViewConstant.TABLE_VIEW) t = up; up = up.getParentView() }
            val part = t ?: break
            if (part.getElement() !== tableElem || parts.any { it === part }) break
            parts.add(part)
            val next = part.getEndOffset(null)
            if (next <= offset) break
            offset = next
        }
        if (parts.isEmpty()) return null
        val rows = ArrayList<RowGuide>()
        val edges = HashMap<Int, Int>() // grid index -> smallest right edge seen (view x)
        var left = Int.MAX_VALUE; var top = Int.MAX_VALUE; var right = Int.MIN_VALUE; var bottom = Int.MIN_VALUE
        for (part in parts) {
            var page: IView? = part
            while (page != null && page.getType() != WPViewConstant.PAGE_VIEW) page = page.getParentView()
            val (dx, dy) = shift(root, page) ?: continue
            val at = WPViewKit.instance().getAbsoluteCoordinate(part, WPViewConstant.PAGE_ROOT.toInt(), Rectangle())
            fun vx(x: Int) = floor(x * z + dx).toInt()
            fun vy(y: Int) = floor(y * z + dy).toInt()
            left = minOf(left, vx(at.x)); top = minOf(top, vy(at.y))
            right = maxOf(right, vx(at.x + part.getWidth())); bottom = maxOf(bottom, vy(at.y + part.getHeight()))
            var row = part.getChildView()
            while (row != null) {
                val ry = at.y + row.getY()
                rows.add(RowGuide(row.getElement()?.getStartOffset() ?: row.getStartOffset(null), vy(ry), vy(ry + row.getHeight())))
                var cell = row.getChildView()
                var k = 0
                while (cell != null) {
                    val x = vx(at.x + row.getX() + cell.getX() + cell.getLayoutSpan(WPViewConstant.X_AXIS))
                    edges[k + 1] = minOf(edges[k + 1] ?: Int.MAX_VALUE, x)
                    k++
                    cell = cell.getNextView()
                }
                row = row.getNextView()
            }
        }
        if (rows.isEmpty()) return null
        return TableGuides(left, top, right, bottom, edges.entries.sortedBy { it.key }.map { ColumnGuide(it.key, it.value) }, rows)
    }

    /** The box of the floating shape anchored at [offset], or null. */
    fun floatingShapeRect(offset: Long): Rect? = placedShapes().firstOrNull { it.offset == offset }?.rect

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
