/*
 * WPFind.kt  (chuyển từ WPFind.java + sửa lỗi tìm kiếm / highlight)
 */
package com.wxiwei.office.wp.control

import com.wxiwei.office.system.search.SearchText
import java.util.function.Consumer

import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.system.IFind
import com.wxiwei.office.wp.view.PageRoot
import com.wxiwei.office.wp.view.PageView

/**
 * Tìm kiếm trong văn bản Word và highlight kết quả.
 *
 * Các lỗi đã sửa so với bản Java:
 * 1. Không phân biệt hoa/thường ("hà nội" tìm được "Hà Nội").
 * 2. Chuẩn hóa từ khóa về Unicode NFC (bàn phím tiếng Việt có thể gửi dạng tổ hợp NFD),
 *    và thử thêm dạng NFD cho văn bản cũ (.doc) chưa chuẩn hóa.
 * 3. nbsp / tab / ngắt dòng mềm trong văn bản được coi là dấu cách.
 *    Nháy cong ‘ ’ “ ” được coi như nháy thẳng.
 *    => Việc "gập" ký tự giữ NGUYÊN độ dài chuỗi nên vị trí highlight luôn đúng.
 * 4. Tìm tiếp / tìm lùi dựa trên vị trí kết quả hiện tại (bản cũ dùng
 *    relativeParaIndex - query.length * 2 => nhảy sai, bỏ sót kết quả sát nhau).
 * 5. find() tìm từ trang đang xem tới cuối rồi QUAY VÒNG lên đầu tài liệu
 *    (bản cũ báo "không tìm thấy" nếu kết quả nằm trước trang hiện tại).
 * 6. Tìm tiếp/tìm lùi cũng quay vòng khi tới cuối/đầu tài liệu.
 * 7. Cache nội dung đoạn đã "gập" để không gọi getText() lặp lại.
 */
class WPFind(word: Word?) : IFind {

    private var word: Word? = word
    private var rect: Rectangle? = Rectangle()

    private var isSetPointToVisible = false
    private var pageIndex = 0

    /** Từ khóa gốc người dùng nhập */
    private var query: String? = null

    /** Các dạng từ khóa đã gập (NFC, và NFD nếu khác) */
    private var foldedQueries: List<String> = emptyList()

    /** Đoạn đang chứa kết quả hiện tại */
    private var findElement: IElement? = null

    /** Nội dung đoạn hiện tại đã gập (cùng độ dài với text gốc) */
    private var foldedPara: String? = null

    /** Vị trí (trong đoạn) và độ dài của kết quả hiện tại; -1 = chưa có */
    private var matchIndex = -1
    private var matchLength = 0

    override fun find(value: String?): Boolean {
        val w = word ?: return false
        if (!prepareQuery(value)) return false
        isSetPointToVisible = false

        val zoom = w.getZoom()
        var offset = 0L
        if (w.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) {
            var view: IView? = w.getPrintWord().getCurrentPageView()
            while (view != null && view.getType() != WPViewConstant.PARAGRAPH_VIEW) {
                view = view.getChildView()
            }
            if (view != null) {
                offset = view.getStartOffset(null)
            }
        } else {
            offset = w.viewToModel((w.scrollX / zoom).toInt(), (w.scrollY / zoom).toInt(), false)
        }

        val doc = w.getDocument()
        val startPara = doc.getParagraph(offset)
        // 1) từ đoạn đang xem tới cuối tài liệu
        var para = startPara
        while (para != null) {
            if (searchInParagraph(doc, para, 0, forward = true)) return true
            para = doc.getParagraph(para.getEndOffset())
        }
        // 2) quay vòng: từ đầu tài liệu tới đoạn đang xem
        val stopOffset = startPara?.getStartOffset() ?: Long.MAX_VALUE
        para = doc.getParagraph(0)
        while (para != null && para.getStartOffset() < stopOffset) {
            if (searchInParagraph(doc, para, 0, forward = true)) return true
            para = doc.getParagraph(para.getEndOffset())
        }
        clearMatch()
        return false
    }

    override fun findForward(): Boolean {
        val w = word ?: return false
        if (query == null) return false
        isSetPointToVisible = false
        val doc = w.getDocument()

        // tiếp trong đoạn hiện tại, sau kết quả hiện tại
        val cur = findElement
        if (cur != null && matchIndex >= 0) {
            if (searchInParagraph(doc, cur, matchIndex + maxOf(matchLength, 1), forward = true)) return true
        }
        // các đoạn sau
        val startOffset = cur?.getEndOffset() ?: 0L
        var para = doc.getParagraph(startOffset)
        while (para != null) {
            if (searchInParagraph(doc, para, 0, forward = true)) return true
            para = doc.getParagraph(para.getEndOffset())
        }
        // quay vòng từ đầu tài liệu tới hết đoạn hiện tại
        val stop = cur?.getEndOffset() ?: Long.MAX_VALUE
        para = doc.getParagraph(0)
        while (para != null && para.getStartOffset() < stop) {
            if (searchInParagraph(doc, para, 0, forward = true)) return true
            para = doc.getParagraph(para.getEndOffset())
        }
        return false
    }

    override fun findBackward(): Boolean {
        val w = word ?: return false
        if (query == null) return false
        isSetPointToVisible = false
        val doc = w.getDocument()

        // lùi trong đoạn hiện tại, trước kết quả hiện tại
        val cur = findElement
        if (cur != null && matchIndex > 0) {
            if (searchInParagraph(doc, cur, matchIndex - 1, forward = false)) return true
        }
        // các đoạn trước
        val docEnd = doc.getLength(0) - 1
        var para = if (cur == null) doc.getParagraph(docEnd) else doc.getParagraph(cur.getStartOffset() - 1)
        while (para != null) {
            if (searchInParagraph(doc, para, Int.MAX_VALUE, forward = false)) return true
            if (para.getStartOffset() <= 0) break
            para = doc.getParagraph(para.getStartOffset() - 1)
        }
        // quay vòng từ cuối tài liệu về tới đoạn hiện tại
        val stop = cur?.getStartOffset() ?: -1L
        para = doc.getParagraph(docEnd)
        while (para != null && para.getStartOffset() >= stop) {
            if (searchInParagraph(doc, para, Int.MAX_VALUE, forward = false)) return true
            if (para.getStartOffset() <= 0) break
            para = doc.getParagraph(para.getStartOffset() - 1)
        }
        return false
    }

    override fun resetSearchResult() {
        clearMatch()
    }

    override fun getPageIndex(): Int = pageIndex

    fun isSetPointToVisible(): Boolean = isSetPointToVisible

    fun setSetPointToVisible(isSetPointToVisible: Boolean) {
        this.isSetPointToVisible = isSetPointToVisible
    }

    /** Re-apply the current match and report its occurrence index to Java/Kotlin callers. */
    fun focusByCurrent(result: Consumer<Int>) {
        if (query != null && findElement != null && matchIndex >= 0) {
            addHighlight(findElement!!, matchIndex, matchLength)
            result.accept(occurrenceIndex())
        }
    }

    /** Collect all match offsets, preserving the new fold/search behavior. */
    fun findAll(value: String?, result: Consumer<Int>): MutableList<Long> {
        val w = word ?: return mutableListOf()
        if (!prepareQuery(value)) return mutableListOf()
        val hits = mutableListOf<Long>()
        val doc = w.getDocument()
        var para = doc.getParagraph(0)
        var index = 0
        while (para != null) {
            for (at in matchesIn(para.getText(doc) ?: "")) {
                hits.add(para.getStartOffset() + at)
                result.accept(index++)
            }
            para = doc.getParagraph(para.getEndOffset())
        }
        return hits
    }

    fun focusBy(occurrenceIndex: Int): Boolean {
        if (occurrenceIndex < 0 || query == null) return false
        val w = word ?: return false
        val doc = w.getDocument()
        var para = doc.getParagraph(0)
        var index = 0
        while (para != null) {
            for (at in matchesIn(para.getText(doc) ?: "")) {
                if (index++ == occurrenceIndex) return searchInParagraph(doc, para, at, true)
            }
            para = doc.getParagraph(para.getEndOffset())
        }
        return false
    }

    private fun occurrenceIndex(): Int {
        val w = word ?: return -1
        val current = findElement ?: return -1
        val doc = w.getDocument()
        var para = doc.getParagraph(0)
        var index = 0
        while (para != null) {
            for (at in matchesIn(para.getText(doc) ?: "")) {
                if (para === current && at == matchIndex) return index
                index++
            }
            para = doc.getParagraph(para.getEndOffset())
        }
        return -1
    }

    /**
     * Start positions of the matches in a raw paragraph, in reading order.
     * The NFC and NFD forms are merged so the occurrence index follows the text like findForward does.
     */
    private fun matchesIn(text: String): List<Int> = SearchText.matchesIn(text, foldedQueries)

    override fun dispose() {
        findElement = null
        foldedPara = null
        word = null
        rect = null
    }

    // ===================== nội bộ =====================

    fun prepareQuery(value: String?): Boolean {
        foldedQueries = SearchText.queries(value)
        query = value?.takeIf { foldedQueries.isNotEmpty() }
        clearMatch()
        return foldedQueries.isNotEmpty()
    }

    /** Focus a cached paragraph position without scanning preceding paragraphs. */
    fun focusAt(paraStart: Long, at: Int): Boolean {
        if (query == null || at < 0) return false
        val doc = word?.getDocument() ?: return false
        val para = doc.getParagraph(paraStart) ?: return false
        isSetPointToVisible = false
        return searchInParagraph(doc, para, at, true)
    }

    private fun clearMatch() {
        findElement = null
        foldedPara = null
        matchIndex = -1
        matchLength = 0
    }

    /**
     * Tìm trong một đoạn.
     * @param from forward: vị trí bắt đầu tìm; backward: vị trí bắt đầu tối đa của kết quả.
     */
    private fun searchInParagraph(doc: IDocument, para: IElement, from: Int, forward: Boolean): Boolean {
        val text = if (para === findElement && foldedPara != null) {
            foldedPara!!
        } else {
            fold(para.getText(doc) ?: return false)
        }
        var bestIndex = -1
        var bestLen = 0
        for (q in foldedQueries) {
            if (q.isEmpty() || q.length > text.length) continue
            val idx = if (forward) text.indexOf(q, from) else text.lastIndexOf(q, from)
            if (idx < 0) continue
            // chọn kết quả gần nhất theo hướng tìm
            if (bestIndex < 0 || (forward && idx < bestIndex) || (!forward && idx > bestIndex)) {
                bestIndex = idx
                bestLen = q.length
            }
        }
        if (bestIndex < 0) return false
        findElement = para
        foldedPara = text
        matchIndex = bestIndex
        matchLength = bestLen
        addHighlight(para, bestIndex, bestLen)
        return true
    }

    private fun addHighlight(para: IElement, index: Int, queryLen: Int) {
        val w = word ?: return
        val r = rect ?: Rectangle().also { rect = it }
        val findCurrentOffset = para.getStartOffset() + index
        w.getHighlight().addHighlight(findCurrentOffset, findCurrentOffset + queryLen)

        if (w.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) {
            val root = w.getRoot(WPViewConstant.PAGE_ROOT.toInt())
            var invalidate = true
            if (root != null && root.getType() == WPViewConstant.PAGE_ROOT) {
                var pv = (root as PageRoot).getViewContainer()!!.getParagraph(findCurrentOffset, false)
                while (pv != null && pv.getType() != WPViewConstant.PAGE_VIEW) {
                    pv = pv.getParentView()
                }
                if (pv != null) {
                    pageIndex = (pv as PageView).getPageNumber() - 1
                    if (pageIndex != w.getCurrentPageNumber() - 1) {
                        w.showPage(pageIndex, -1)
                        isSetPointToVisible = true
                        invalidate = false
                    } else {
                        r.setBounds(0, 0, 0, 0)
                        w.modelToView(findCurrentOffset, r, false)
                        r.x -= pv.getX()
                        r.y -= pv.getY()
                        val listView = w.getPrintWord().getListView()
                        if (!listView.isPointVisibleOnScreen(r.x, r.y)) {
                            listView.setItemPointVisibleOnScreen(r.x, r.y)
                            invalidate = false
                        } else {
                            w.getPrintWord().exportImage(listView.getCurrentPageView(), null)
                        }
                    }
                }
            }
            if (invalidate) {
                w.postInvalidate()
            }
            return
        }
        //
        r.setBounds(0, 0, 0, 0)
        w.modelToView(findCurrentOffset, r, false)
        val vRect = w.getVisibleRect()
        val zoom = w.getZoom()
        var x = (r.x * zoom).toInt()
        var y = (r.y * zoom).toInt()
        if (!vRect.contains(x, y)) {
            if (x + vRect.width > w.getWordWidth() * zoom) {
                x = (w.getWordWidth() * zoom).toInt() - vRect.width
            }
            if (y + vRect.height > w.getWordHeight() * zoom) {
                y = (w.getWordHeight() * zoom).toInt() - vRect.height
            }
            w.scrollTo(maxOf(x, 0), maxOf(y, 0))
        } else {
            w.postInvalidate()
        }
        //
        w.getControl().actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
        //
        if (w.getCurrentRootType() != WPViewConstant.PRINT_ROOT.toInt()) {
            w.getControl().actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
        }
    }

    companion object {
        /**
         * "Gập" chuỗi để so khớp, GIỮ NGUYÊN ĐỘ DÀI (mỗi ký tự -> đúng 1 ký tự)
         * để vị trí tìm được trùng với offset trong tài liệu.
         */
        @JvmStatic
        fun fold(s: String): String = SearchText.fold(s)
    }
}
