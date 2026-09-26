package com.wxiwei.office.pg.control

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.common.ISlideShow
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.beans.AEventManage

class PGEventManage(private var presentation: Presentation?, control: IControl) : AEventManage(presentation!!.context, control) {
    init { presentation?.setOnTouchListener(this); presentation?.isLongClickable = true }
    override fun onTouch(v: View?, event: MotionEvent): Boolean { super.onTouch(v, event); return false }
    override fun onDoubleTap(e: MotionEvent): Boolean { super.onDoubleTap(e); return true }
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean { super.onScroll(e1, e2, distanceX, distanceY); return true }
    override fun fling(velocityX: Int, velocityY: Int) {
        val p = presentation ?: return
        if (p.isSlideShow()) {
            if (kotlin.math.abs(velocityY) < 400 && kotlin.math.abs(velocityX) < 400) { p.slideShow(ISlideShow.SlideShow_NextStep); return }
            super.fling(velocityX, velocityY); val i = p.getCurrentIndex()
            if (kotlin.math.abs(velocityY) > kotlin.math.abs(velocityX)) { if (velocityY < 0 && i >= 0) p.slideShow(ISlideShow.SlideShow_NextStep) else if (velocityY > 0 && i <= p.getRealSlideCount() - 1) p.slideShow(ISlideShow.SlideShow_PreviousStep) }
            else { if (velocityX < 0 && i >= 0) p.slideShow(ISlideShow.SlideShow_PreviousSlide) else if (velocityX > 0 && i < p.getRealSlideCount() - 1) p.slideShow(ISlideShow.SlideShow_NextSlide) }
        }
    }
    override fun onSingleTapUp(e: MotionEvent): Boolean { super.onSingleTapUp(e); if (e.action == MotionEvent.ACTION_UP) { val p = presentation; val r: Rect? = p?.getSlideDrawingRect(); if (p?.isSlideShow() == true && r?.contains(e.x.toInt(), e.y.toInt()) == true) p.slideShow(ISlideShow.SlideShow_NextStep) }; return true }
    override fun dispose() { super.dispose(); presentation = null }
}
