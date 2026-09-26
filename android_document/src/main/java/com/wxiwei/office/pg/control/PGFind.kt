package com.wxiwei.office.pg.control

import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.model.PGSlide
import com.wxiwei.office.simpletext.model.SectionElement
import com.wxiwei.office.system.IFind
import com.wxiwei.office.system.search.SearchText

class PGFind(private var presentation: Presentation?) : IFind {
    private var isSetPointToVisible = false
    private var query: String? = null
    private var shapeIndex = -1
    private var slideIndex = -1
    private var startOffset = -1
    private var matchLength = 0
    protected var rect: Rectangle = Rectangle()

    override fun find(query: String?): Boolean {
        if (query == null) return false
        this.query = query; matchLength = query.length; startOffset = -1; shapeIndex = -1
        val p = presentation ?: return false
        var index = p.getCurrentIndex()
        do {
            if (findSlideForward(index)) return true
            if (++index == p.getRealSlideCount()) index = 0
        } while (index != p.getCurrentIndex())
        return false
    }

    override fun findBackward(): Boolean {
        val p = presentation ?: return false
        if (query == null) return false
        var index = p.getCurrentIndex()
        do {
            if (findSlideBackward(index)) return true
            startOffset = -1; shapeIndex = -1
        } while (--index >= 0)
        return false
    }

    override fun findForward(): Boolean {
        val p = presentation ?: return false
        if (query == null) return false
        var index = p.getCurrentIndex()
        do {
            if (findSlideForward(index)) return true
            startOffset = -1; shapeIndex = -1
        } while (++index != p.getRealSlideCount())
        return false
    }

    private fun findSlideBackward(slideIndex: Int): Boolean {
        val p = presentation ?: return false
        val slide = p.getSlide(slideIndex) ?: return false
        var i = if (shapeIndex >= 0) shapeIndex else slide.getShapeCountForFind() - 1
        while (i >= 0) {
            val shape = slide.getShapeForFind(i)
            if (shape != null && shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
                var offset = if (shapeIndex == i && p.getCurrentIndex() == slideIndex) startOffset else -1
                val elem: SectionElement? = (shape as TextBox).getElement()
                val q = query ?: return false
                if (elem == null || (offset >= 0 && offset < q.length) || elem.getEndOffset() - elem.getStartOffset() == 0L) { i--; continue }
                offset = if (offset >= 0) elem.getText(p.getRenderersDoc())!!.lastIndexOf(q, maxOf(startOffset - q.length, 0)) else elem.getText(p.getRenderersDoc())!!.lastIndexOf(q)
                if (offset >= 0) { startOffset = offset; shapeIndex = i; addHighlight(slideIndex, shape as TextBox); return true }
            }
            i--
        }
        return false
    }

    private fun findSlideForward(slideIndex: Int): Boolean {
        val p = presentation ?: return false
        val slide = p.getSlide(slideIndex) ?: return false
        val q = query ?: return false
        for (i in maxOf(0, shapeIndex) until slide.getShapeCountForFind()) {
            val shape = slide.getShapeForFind(i)
            if (shape != null && shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
                val elem: SectionElement? = (shape as TextBox).getElement()
                if (elem == null || elem.getEndOffset() - elem.getStartOffset() == 0L) continue
                val offset = if (shapeIndex == i && p.getCurrentIndex() == slideIndex) elem.getText(p.getRenderersDoc())!!.indexOf(q, startOffset + q.length) else elem.getText(p.getRenderersDoc())!!.indexOf(q)
                if (offset >= 0) { startOffset = offset; shapeIndex = i; addHighlight(slideIndex, shape as TextBox); return true }
            }
        }
        return false
    }

    fun removeAllHighlights() {}

    fun addHighlight(slideIndex: Int, textBox: TextBox) {
        val p = presentation ?: return
        var invalidate = true
        if (slideIndex != p.getCurrentIndex()) { p.showSlide(slideIndex, true); isSetPointToVisible = true; invalidate = false }
        else {
            rect.setBounds(0, 0, 0, 0); p.getEditor().modelToView(startOffset.toLong(), rect, false)
            if (!p.getPrintMode().getListView()!!.isPointVisibleOnScreen(rect.x, rect.y)) { p.getPrintMode().getListView()!!.setItemPointVisibleOnScreen(rect.x, rect.y); invalidate = false }
            else p.getPrintMode().exportImage(p.getPrintMode().getListView()!!.getCurrentPageView(), null)
        }
        if (invalidate) p.postInvalidate()
        this.slideIndex = slideIndex; p.getEditor().setEditorTextBox(textBox)
        val q = query ?: return
        p.getEditor().getHighlight()?.addHighlight(startOffset.toLong(), (startOffset + matchLength).toLong())
        p.getControl()!!.actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
    }

    fun onConfigurationChanged() {
        val p = presentation ?: return
        val slide = p.getCurrentSlide() ?: return
        if (shapeIndex >= 0 && shapeIndex < slide.getShapeCountForFind()) { val q = query ?: return; p.getEditor().getHighlight()?.addHighlight(startOffset.toLong(), (startOffset + matchLength).toLong()); p.postInvalidate() }
    }
    fun isSetPointToVisible(): Boolean = isSetPointToVisible
    fun setSetPointToVisible(value: Boolean) { isSetPointToVisible = value }

    class SearchResult(val slideIndex: Int, val shapeIndex: Int, val startOffset: Int, val matchLength: Int, val query: String)

    @JvmOverloads
    fun findAll(query: String?, isActive: () -> Boolean = { true }): List<SearchResult> {
        val all = ArrayList<SearchResult>()
        val p = presentation ?: return all
        if (query.isNullOrEmpty()) return all
        val queries = SearchText.queries(query)
        for (si in 0 until p.getRealSlideCount()) {
            if (!isActive()) return all
            val slide = p.getSlide(si) ?: continue
            for (sh in 0 until slide.getShapeCountForFind()) {
                if (!isActive()) return all
                val shape = slide.getShapeForFind(sh)
                if (shape != null && shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
                    val elem = (shape as TextBox).getElement() ?: continue
                    // Folding keeps the length, so positions stay valid in the original text
                    val text = elem.getText(p.getRenderersDoc()) ?: continue
                    val folded = SearchText.fold(text)
                    for (at in SearchText.matchesIn(text, queries)) {
                        if (!isActive()) return all
                        val length = queries.first { folded.startsWith(it, at) }.length
                        all.add(SearchResult(si, sh, at, length, query))
                    }
                }
            }
        }
        return all
    }

    /** Show and highlight a result returned by [findAll]. */
    fun focus(result: SearchResult): Boolean {
        val p = presentation ?: return false
        val slide = p.getSlide(result.slideIndex) ?: return false
        val shape = slide.getShapeForFind(result.shapeIndex) as? TextBox ?: return false
        shapeIndex = result.shapeIndex
        startOffset = result.startOffset
        this.query = result.query
        matchLength = result.matchLength
        addHighlight(result.slideIndex, shape)
        return true
    }

    override fun getPageIndex(): Int = slideIndex
    override fun resetSearchResult() {}
    override fun dispose() { presentation = null; query = null }
}
