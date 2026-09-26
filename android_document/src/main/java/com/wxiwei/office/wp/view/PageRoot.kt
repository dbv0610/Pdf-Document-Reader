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
