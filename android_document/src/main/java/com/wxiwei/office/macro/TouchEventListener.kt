/*
 * 文件名称:          ITouchListener.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:34:20
 */
package com.wxiwei.office.macro

import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.system.IMainFrame

/**
 * touch event listener
 */
interface TouchEventListener {

    /**
     * event method, office engine dispatch
     *
     * @param       v             event source
     * @param       e1            MotionEvent instance
     * @param       e2            MotionEvent instance
     * @param       velocityX     x axis velocity
     * @param       velocityY     y axis velocity
     * @param       eventMethodType  event method
     *              @see TouchEventListener#EVENT_CLICK
     *              @see TouchEventListener#EVENT_DOUBLE_TAP
     *              @see TouchEventListener#EVENT_DOUBLE_TAP_EVENT
     *              @see TouchEventListener#EVENT_DOWN
     *              @see TouchEventListener#EVENT_FLING
     *              @see TouchEventListener#EVENT_LONG_PRESS
     *              @see TouchEventListener#EVENT_SCROLL
     *              @see TouchEventListener#EVENT_SHOW_PRESS
     *              @see TouchEventListener#EVENT_SINGLE_TAP_CONFIRMED
     *              @see TouchEventListener#EVENT_SINGLE_TAP_UP
     *              @see TouchEventListener#EVENT_TOUCH
     */
    fun onEventMethod(
        v: View?, e1: MotionEvent?, e2: MotionEvent?,
        velocityX: Float, velocityY: Float, eventMethodType: Byte
    ): Boolean

    companion object {
        // onTouch
        const val EVENT_TOUCH = IMainFrame.ON_TOUCH
        // onDown
        const val EVENT_DOWN = IMainFrame.ON_DOWN
        // onShowPress
        const val EVENT_SHOW_PRESS = IMainFrame.ON_SHOW_PRESS
        // onSingleTapUp
        const val EVENT_SINGLE_TAP_UP = IMainFrame.ON_SINGLE_TAP_UP
        // onScroll
        const val EVENT_SCROLL = IMainFrame.ON_SCROLL
        // onLongPress
        const val EVENT_LONG_PRESS = IMainFrame.ON_LONG_PRESS
        // onFling
        const val EVENT_FLING = IMainFrame.ON_FLING
        // onSingleTapConfirmed
        const val EVENT_SINGLE_TAP_CONFIRMED = IMainFrame.ON_SINGLE_TAP_CONFIRMED
        // onDoubleTap
        const val EVENT_DOUBLE_TAP = IMainFrame.ON_DOUBLE_TAP
        // onDoubleTapEvent
        const val EVENT_DOUBLE_TAP_EVENT = IMainFrame.ON_DOUBLE_TAP_EVENT
        // onClick
        const val EVENT_CLICK = IMainFrame.ON_CLICK
    }
}
