package com.wxiwei.office.wp.control

import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.common.picture.PictureKit
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.AEventManage
import kotlin.math.abs

class WPEventManage(protected var word: Word?, control: IControl) : AEventManage(word!!.context, control) {
    private var oldX = 0
    private var oldY = 0

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val touchView = v ?: return false
        try {
            super.onTouch(v, event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    PictureKit.instance().setDrawPictrue(true)
                    processDown(touchView, event)
                }

                MotionEvent.ACTION_MOVE -> {}
                MotionEvent.ACTION_UP -> {
                    val currentWord = word ?: return false
                    if (zoomChange) {
                        zoomChange = false
                        if (currentWord.getCurrentRootType() == WPViewConstant.PAGE_ROOT.toInt()) {
                            control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null)
                        }
                        if (control.getMainFrame().isZoomAfterLayoutForWord()) {
                            control.actionEvent(EventConstant.WP_LAYOUT_NORMAL_VIEW, null)
                        }
                    }
                    currentWord.getControl().actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
                }

                else -> {}
            }
        } catch (e: Exception) {
            control.getSysKit().getErrorKit().writerLog(e)
        }
        return false
    }

    protected fun processDown(v: View, event: MotionEvent) {
        val currentWord = word ?: return
        val x = convertCoorForX(event.x)
        val y = convertCoorForY(event.y)
        val offset = currentWord.viewToModel(x, y, false)
        if (currentWord.getHighlight().isSelectText()) {
            currentWord.getHighlight().removeHighlight()
            currentWord.getStatus().setPressOffset(offset)
            currentWord.postInvalidate()
        }
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return onScrollImpl(e1, e2, distanceX, distanceY)
    }

    fun onScrollImpl(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val currentWord = word ?: return true
        if (currentWord.getStatus().isSelectTextStatus()) {
            return true
        }
        // tương đương super.onScroll(e1, e2, distanceX, distanceY) của AEventManage
        isScroll = true
        control.getMainFrame().onEventMethod(control.getView(), e1, e2, distanceX, distanceY, com.wxiwei.office.system.IMainFrame.ON_SCROLL)
        var change = false
        val isScrollX = abs(distanceX) > abs(distanceY)
        val r: Rectangle = currentWord.getVisibleRect()
        var sX = r.x
        var sY = r.y
        val zoom = currentWord.getZoom()
        val wW: Int = if (currentWord.getCurrentRootType() == WPViewConstant.NORMAL_ROOT.toInt() &&
            control.getMainFrame().isZoomAfterLayoutForWord()
        ) {
            if (currentWord.getWidth() == currentWord.getWordWidth()) {
                currentWord.getWidth()
            } else {
                (currentWord.getWordWidth() * zoom).toInt()
            }
        } else {
            (currentWord.getWordWidth() * zoom).toInt()
        }
        val wH = (currentWord.getWordHeight() * zoom).toInt()
        if (isScrollX) {
            if (distanceX > 0 && sX + r.width < wW) {
                sX += distanceX.toInt()
                if (sX + r.width > wW) {
                    sX = wW - r.width
                }
                change = true
            } else if (distanceX < 0 && sX > 0) {
                sX += distanceX.toInt()
                if (sX < 0) {
                    sX = 0
                }
                change = true
            }
        } else {
            if (distanceY > 0 && sY + r.height < wH) {
                sY += distanceY.toInt()
                if (sY + r.height > wH) {
                    sY = wH - r.height
                }
                change = true
            } else if (distanceY < 0 && sY > 0) {
                sY += distanceY.toInt()
                if (sY < 0) {
                    sY = 0
                }
                change = true
            }
        }
        if (change) {
            isScroll = true
            currentWord.scrollTo(sX, sY)
        }
        return true
    }

    override fun fling(velocityX: Int, velocityY: Int) {
        super.fling(velocityX, velocityY)
        val currentWord = word ?: return
        val r = currentWord.getVisibleRect()
        val zoom = currentWord.getZoom()
        oldY = 0
        oldX = 0
        val wW: Int = if (currentWord.getCurrentRootType() == WPViewConstant.NORMAL_ROOT.toInt() &&
            control.getMainFrame().isZoomAfterLayoutForWord()
        ) {
            if (currentWord.getWidth() == currentWord.getWordWidth()) {
                currentWord.getWidth()
            } else {
                (currentWord.getWordWidth() * zoom).toInt() + 5
            }
        } else {
            (currentWord.getWordWidth() * zoom).toInt()
        }
        if (abs(velocityY) > abs(velocityX)) {
            oldY = r.y
            mScroller.fling(r.x, r.y, 0, velocityY, 0, r.x, 0, (currentWord.getWordHeight() * zoom).toInt() - r.height)
        } else {
            oldX = r.x
            mScroller.fling(r.x, r.y, velocityX, 0, 0, wW - r.width, r.y, 0)
        }
        currentWord.postInvalidate()
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        super.onDoubleTapEvent(e)
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        super.onSingleTapUp(e)
        val currentWord = word ?: return true
        if (e.action == MotionEvent.ACTION_UP) {
            val x = convertCoorForX(e.x)
            val y = convertCoorForY(e.y)
            val offset = currentWord.viewToModel(x, y, false)
            if (offset >= 0) {
                val leaf = currentWord.getDocument().getLeaf(offset)
                if (leaf != null) {
                    val hyID = AttrManage.instance().getHperlinkID(leaf.getAttribute())
                    if (hyID >= 0) {
                        val hylink = control.getSysKit().getHyperlinkManage().getHyperlink(hyID)
                        if (hylink != null) {
                            control.actionEvent(EventConstant.APP_HYPERLINK, hylink)
                        }
                    }
                }
            }
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        val currentWord = word ?: return
        if (mScroller.computeScrollOffset()) {
            isFling = true
            PictureKit.instance().setDrawPictrue(false)
            val sX = mScroller.currX
            val sY = mScroller.currY
            if (oldX == sX && oldY == sY || sX == currentWord.getScrollX() && sY == currentWord.getScrollY()) {
                PictureKit.instance().setDrawPictrue(true)
            }
            oldX = sX
            oldY = sY
            currentWord.scrollTo(sX, sY)
            return
        }
        if (!PictureKit.instance().isDrawPictrue()) {
            PictureKit.instance().setDrawPictrue(true)
            currentWord.postInvalidate()
        }
    }

    protected fun convertCoorForX(x: Float): Int {
        val currentWord = word ?: return 0
        return ((x + currentWord.getScrollX()) / currentWord.getZoom()).toInt()
    }

    protected fun convertCoorForY(y: Float): Int {
        val currentWord = word ?: return 0
        return ((y + currentWord.getScrollY()) / currentWord.getZoom()).toInt()
    }

    override fun dispose() {
        super.dispose()
        word = null
    }
}
