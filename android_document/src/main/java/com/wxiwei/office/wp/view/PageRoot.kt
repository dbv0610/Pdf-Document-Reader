/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.util.Log
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IRoot
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.ViewContainer
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.control.Word

class PageRoot(private var word: Word?) : AbstractView(), IRoot {
    private var paraCount = 0
    private var canBackLayoutFlag = true
    private var layoutThread = LayoutThread(this)
    private var wpLayouter = WPLayouter(this)
    private var viewContainer = ViewContainer()
    private var pages = ArrayList<PageView>()
    @Volatile private var layoutStarted = false

    init { }

    fun isFinishLayout(): Boolean = wpLayouter.isLayoutFinish()
    override fun getType(): Short = WPViewConstant.PAGE_ROOT
    override fun getDocument(): IDocument? = word?.getDocument()
    override fun getContainer(): IWord? = word
    override fun getControl(): IControl? = word?.getControl()

    fun doLayout(x: Int, y: Int, w: Int, h: Int, maxEnd: Int, flag: Int): Int {
        return try {
            if (layoutStarted) {
                Log.w("OfficePageLayout", "doLayout skipped: layout already started pages=${pages.size}")
                return WPViewConstant.BREAK_NO.toInt()
            }
            layoutStarted = true
            Log.e("PageRoot.doLayout", "maxEnd $maxEnd")
            val doc = getDocument() ?: return WPViewConstant.BREAK_NO.toInt()
            setParaCount(doc.getParaCount(WPModelConstant.MAIN))
            wpLayouter.doLayout()
            if (!wpLayouter.isLayoutFinish() && word?.getControl()?.getMainFrame()?.isThumbnail() == false) {
                layoutThread.start()
                word?.getControl()?.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true)
            } else {
                word?.getControl()?.actionEvent(EventConstant.WP_LAYOUT_COMPLETED, true)
                word?.getControl()?.actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true)
            }
            WPViewConstant.BREAK_NO.toInt()
        } catch (e: Exception) {
            word?.getControl()?.getSysKit()?.getErrorKit()?.writerLog(e)
            WPViewConstant.BREAK_NO.toInt()
        }
    }

    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        super.draw(canvas, originX, originY, zoom)
    }

    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        val view = viewContainer.getParagraph(offset, isBack)
        if (view != null) {
            view.modelToView(offset, rect, isBack)
            var p = view.getParentView()
            while (p != null && p.getType() != WPViewConstant.PAGE_ROOT) {
                rect.x += p.getX()
                rect.y += p.getY()
                p = p.getParentView()
            }
        }
        rect.x += getX()
        rect.y += getY()
        return rect
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        var xx = x - getX()
        var yy = y - getY()
        // A point in the gap below a page belongs to that page.
        val gap = maxOf(MainConstant.GAP / 2, word?.getPageSpacing() ?: 0)
        var view = getChildView()
        if (view != null && yy > view.getY()) {
            while (view != null) {
                if (yy >= view.getY() && yy <= view.getY() + view.getHeight() + gap) break
                view = view.getNextView()
            }
        }
        view = view ?: getChildView()
        return view?.viewToModel(xx, yy, isBack) ?: -1
    }

    override fun canBackLayout(): Boolean = canBackLayoutFlag && wpLayouter.isLayoutFinish().not()
    override fun backLayout() {
        wpLayouter.backLayout()
        word?.postInvalidate()
        if (wpLayouter.isLayoutFinish()) {
            word?.getControl()?.actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true)
            word?.getControl()?.actionEvent(EventConstant.WP_LAYOUT_COMPLETED, true)
        }
        word?.getControl()?.actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
        LayoutKit.instance().layoutAllPage(this, word?.getZoom() ?: 1f)
        word?.layoutPrintMode()
    }

    fun getParaCount(): Int = paraCount
    fun setParaCount(paraCount: Int) { this.paraCount = paraCount }
    @Synchronized
    fun getPageCount(): Int = pages.size
    @Synchronized
    override fun getChildCount(): Int = pages.size
    override fun getViewContainer(): ViewContainer = viewContainer
    @Synchronized
    fun addPageView(pv: PageView) {
        val start = pv.getStartOffset(null)
        val end = pv.getEndOffset(null)
        if (end <= start) {
            Log.e(
                "OfficePageLayout",
                "reject invalid page start=$start end=$end pageNumber=${pv.getPageNumber()} " +
                    "thread=${Thread.currentThread().name}"
            )
            return
        }
        val before = pages.size
        pages.add(pv)
        Log.d(
            "OfficePageLayout",
            "addPageView before=$before after=${pages.size} " +
                "pageNumber=${pv.getPageNumber()} start=$start end=$end " +
                "thread=${Thread.currentThread().name}"
        )
    }
    /**
     * Lays the pages out again from the page before the one holding the paragraph of [offset]
     * (a live edit there): earlier pages keep their layout. Pages are laid out at once down to
     * [visibleBottom] (page root coordinates), the background layout does the rest. Returns false
     * when only a full layout will do (the edit is on the first pages, or a table breaks there).
     * Call with the document lock held, like any model change.
     */
    fun relayoutFrom(offset: Long, visibleBottom: Int, zoom: Float): Boolean {
        val doc = getDocument() ?: return false
        if (!layoutStarted) return false
        val paraStart = minOf(offset, doc.getParagraph(offset)?.getStartOffset() ?: offset)
        // from the page where the edited paragraph starts: its lines on every page may change
        val restart: Int
        val start: Long
        synchronized(this) {
            // not laid out yet: the background layout will get there with the new text
            if (pages.isNotEmpty() && paraStart >= pages.last().getEndOffset(null) && !wpLayouter.isLayoutFinish()) {
                layoutThread.start()
                return true
            }
            var k = pages.indexOfLast { it.getStartOffset(null) <= paraStart }
            // a page continuing a table needs the table's break state: start before it
            while (k > 0 && pages[k - 1].endsWithBrokenTable) k--
            // an edit on the first page lays out again from it: the root (and the scroll) stays
            if (k < 0) return false
            restart = k
            start = pages[k].getStartOffset(null)
            // the layouter shares one header and footer between the pages and lays out the new pages with them: not disposed with the old ones
            for (i in pages.lastIndex downTo k) deleteView(pages.removeAt(i).also { it.setHeader(null); it.setFooter(null) }, true)
        }
        viewContainer.removeFrom(start)
        wpLayouter.restartAt(start, restart + 1, if (restart > 0) pages[restart - 1].getEndOffset(null) else start)
        // no page left: the first one is laid out now
        if (pages.isEmpty()) wpLayouter.backLayout()
        while (!wpLayouter.isLayoutFinish()) {
            val last = pages.lastOrNull() ?: break
            // the next page would start below the screen
            if (last.getY() + last.getHeight() > visibleBottom) break
            val before = pages.size
            wpLayouter.backLayout()
            if (pages.size == before) break // no progress: leave it to the background layout
        }
        LayoutKit.instance().layoutAllPage(this, zoom)
        if (!wpLayouter.isLayoutFinish()) layoutThread.start()
        return true
    }

    /**
     * Lays out pages now, until they reach [bottom] (page-root coordinates) or the document ends,
     * so a view scrolled there after a full relayout stays there; the rest goes on in the background.
     */
    fun layoutDownTo(bottom: Int, zoom: Float) {
        if (!layoutStarted) return
        // the background layout takes the document's lock for every page
        synchronized(getDocument() ?: this) { layoutPagesDownTo(bottom) }
        LayoutKit.instance().layoutAllPage(this, zoom)
    }

    private fun layoutPagesDownTo(bottom: Int) {
        while (!wpLayouter.isLayoutFinish()) {
            val last = synchronized(this) { pages.lastOrNull() } ?: break
            if (last.getY() + last.getHeight() > bottom) break
            val before = pages.size
            wpLayouter.backLayout()
            if (pages.size == before) break
        }
    }

    fun getPageView(pageIndex: Int): PageView? = if (pageIndex < 0 || pageIndex >= pages.size) null else pages[pageIndex]
    fun checkUpdateHeaderFooterFieldText(): Boolean { var has = false; for (page in pages) has = has || page.checkUpdateHeaderFooterFieldText(pages.size); return has }
    fun setLayoutThreadDied(isDied: Boolean) { layoutThread.setDied(isDied) }

    override fun dispose() {
        super.dispose()
        canBackLayoutFlag = false
        layoutStarted = false
        layoutThread.dispose()
        wpLayouter.dispose()
        viewContainer.dispose()
        pages.clear()
        word = null
    }
}
