package com.wxiwei.office.wp.control

import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.wp.view.PageRoot
import com.wxiwei.office.wp.view.PageView

class ControlKit {
    fun internetSearch(word: Word) {
        val doc = word.getDocument()
        val start = word.getHighlight().getSelectStart()
        val end = word.getHighlight().getSelectEnd()
        var str = ""
        if (start != end) {
            str = doc.getText(start, end)
        }
        word.getControl().getSysKit().internetSearch(str, word.getControl().getMainFrame().getActivity())
    }

    fun gotoOffset(word: Word, offset: Long) {
        val rect = Rectangle()
        if (word.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) {
            val root = word.getRoot(WPViewConstant.PAGE_ROOT.toInt())
            var invalidate = true
            if (root != null && root.getType() == WPViewConstant.PAGE_ROOT) {
                var pv = (root as PageRoot).getViewContainer().getParagraph(offset, false)
                while (pv != null && pv.getType() != WPViewConstant.PAGE_VIEW) {
                    pv = pv.getParentView()
                }
                if (pv != null) {
                    val pageIndex = (pv as PageView).getPageNumber() - 1
                    if (pageIndex != word.getCurrentPageNumber() - 1) {
                        word.showPage(pageIndex, -1)
                        invalidate = false
                    } else {
                        rect.setBounds(0, 0, 0, 0)
                        word.modelToView(offset, rect, false)
                        rect.x -= pv.getX()
                        rect.y -= pv.getY()
                        if (!word.getPrintWord().getListView().isPointVisibleOnScreen(rect.x, rect.y)) {
                            word.getPrintWord().getListView().setItemPointVisibleOnScreen(rect.x, rect.y)
                            invalidate = false
                        } else {
                            word.getPrintWord().exportImage(word.getPrintWord().getListView().getCurrentPageView(), null)
                        }
                    }
                }
            }
            if (invalidate) {
                word.postInvalidate()
            }
            return
        }

        rect.setBounds(0, 0, 0, 0)
        word.modelToView(offset, rect, false)
        val vRect = word.getVisibleRect()
        val zoom = word.getZoom()
        var x = (rect.x * zoom).toInt()
        var y = (rect.y * zoom).toInt()
        if (!vRect.contains(x, y)) {
            if (x + vRect.width > word.getWordWidth() * zoom) {
                x = (word.getWordWidth() * zoom).toInt() - vRect.width
            }
            if (y + vRect.height > word.getWordHeight() * zoom) {
                y = (word.getWordHeight() * zoom).toInt() - vRect.height
            }
            word.scrollTo(x, y)
        } else {
            word.postInvalidate()
        }
        word.getControl().actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
        if (word.getCurrentRootType() != WPViewConstant.PRINT_ROOT.toInt()) {
            word.getControl().actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
        }
    }

    companion object {
        private val kit = ControlKit()

        @JvmStatic
        fun instance(): ControlKit = kit
    }
}
