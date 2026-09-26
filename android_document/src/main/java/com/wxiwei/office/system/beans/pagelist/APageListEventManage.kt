package com.wxiwei.office.system.beans.pagelist

import android.graphics.Point
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import android.widget.Toast
import com.wxiwei.office.constant.MainConstant
import java.util.NoSuchElementException

/** Page-list touch and gesture manager. */
open class APageListEventManage(protected val listView: APageListView) :
    ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener,
    Runnable,
    View.OnTouchListener,
    GestureDetector.OnDoubleTapListener,
    View.OnClickListener {

    companion object {
        private const val MOVING_DIAGONALLY = 0
        private const val MOVING_LEFT = 1
        private const val MOVING_RIGHT = 2
        private const val MOVING_UP = 3
        private const val MOVING_DOWN = 4
        private const val MAX_ZOOM = 3.0f
    }

    protected val gesture = GestureDetector(listView.context, this)
    protected val mScroller = Scroller(listView.context)
    private val mScaleGestureDetector = ScaleGestureDetector(listView.context, this)
    protected val toast = Toast.makeText(listView.context, "", Toast.LENGTH_SHORT)
    private var isOnFling = false
    private var isOnScroll = false
    private var isDoubleTap = false
    private var isProcessOnScroll = true
    private var isTouchEventIn = false
    private var isScaling = false
    private var mScrollerLastX = 0
    private var mScrollerLastY = 0
    private var mXScroll = 0
    private var mYScroll = 0
    private var eventPointerCount = 0

    protected open fun zoom(event: MotionEvent): Boolean = false

    open fun processOnTouch(event: MotionEvent): Boolean {
        eventPointerCount = event.pointerCount
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            isOnFling = false
            isTouchEventIn = true
        }
        mScaleGestureDetector.onTouchEvent(event)
        if (!isScaling) {
            gesture.onTouchEvent(event)
        }
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            isProcessOnScroll = true
            isTouchEventIn = false
            val pageView = listView.currentPageView
            if (pageView != null) {
                if (mScroller.isFinished && !isDoubleTap) {
                    slideViewOntoScreen(pageView)
                }
                if (mScroller.isFinished && isOnScroll) {
                    listView.pageListViewListener.setDrawPictrue(true)
                    listView.postRepaint(pageView)
                }
            }
            isDoubleTap = false
            isOnScroll = false
            toast.cancel()
        }
        return true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        listView.pageListViewListener.onEventMethod(v, event, null, -1.0f, -1.0f, IPageListViewListener.ON_TOUCH)
        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        listView.removeCallbacks(this)
        mScroller.forceFinished(true)
        listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_DOWN)
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        listView.pageListViewListener.onEventMethod(listView, e1, e2, velocityX, velocityY, IPageListViewListener.ON_FLING)
        if (!isProcessOnScroll || isDoubleTap) return true
        val pageView = listView.currentPageView ?: return true
        val bounds = listView.getScrollBounds(pageView)
        val horizontal = listView.pageListViewListener.getPageListViewMovingPosition() == IPageListViewListener.Moving_Horizontal
        if ((if (horizontal) pageView.width <= listView.width else pageView.height <= listView.height) || listView.pageListViewListener.isChangePage()) {
            when (directionOfTravel(velocityX, velocityY)) {
                MOVING_LEFT -> if (horizontal && bounds.left >= 0) { isOnFling = true; listView.nextPageView(); return true }
                MOVING_RIGHT -> if (horizontal && bounds.right <= 0) { isOnFling = true; listView.previousPageview(); return true }
                MOVING_UP -> if (!horizontal && bounds.top >= 0) { isOnFling = true; listView.nextPageView(); return true }
                MOVING_DOWN -> if (!horizontal && bounds.bottom <= 0) { isOnFling = true; listView.previousPageview(); return true }
            }
        }
        mScrollerLastX = 0
        mScrollerLastY = 0
        val expandedBounds = Rect(bounds)
        expandedBounds.inset(-100, -100)
        if (withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY) && expandedBounds.contains(0, 0)) {
            mScroller.fling(0, 0, velocityX.toInt(), velocityY.toInt(), bounds.left, bounds.right, bounds.top, bounds.bottom)
            listView.postOnAnimation(this)
        }
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        listView.pageListViewListener.onEventMethod(listView, e1, e2, distanceX, distanceY, IPageListViewListener.ON_SCROLL)
        if (isProcessOnScroll && !isDoubleTap) {
            listView.pageListViewListener.setDrawPictrue(false)
            isOnScroll = true
            mXScroll -= distanceX.toInt()
            mYScroll -= distanceY.toInt()
            if (!listView.pageListViewListener.isChangePage()) {
                val item = listView.currentPageView
                if (item != null && item.width > listView.width) {
                    if (distanceX > 0 && listView.width - mXScroll - item.left > item.width && item.pageIndex < listView.pageCount - 1) mXScroll = -(item.width - listView.width + item.left)
                    else if (distanceX < 0 && mXScroll + item.left > 0 && item.pageIndex != 0) mXScroll = 0
                }
            }
            listView.requestLayout()
        }
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (eventPointerCount <= 1 || !listView.pageListViewListener.isTouchZoom()) return true
        isTouchEventIn = true
        val previousScale = listView.zoom
        val zoom = (listView.zoom * detector.scaleFactor).coerceIn(listView.fitZoom, MAX_ZOOM)
        if ((zoom * MainConstant.ZOOM_ROUND).toInt() != (previousScale * MainConstant.ZOOM_ROUND).toInt()) {
            isOnScroll = true
            val factor = zoom / previousScale
            listView.setZoom(zoom, false)
            val view = listView.currentPageView
            if (view != null) {
                val focusX = detector.focusX.toInt() - (view.left + mXScroll)
                val focusY = detector.focusY.toInt() - (view.top + mYScroll)
                mXScroll += (focusX - focusX * factor).toInt()
                mYScroll += (focusY - focusY * factor).toInt()
                listView.requestLayout()
            }
        }
        if (listView.pageListViewListener.isShowZoomingMsg()) {
            toast.setText("${Math.round(zoom * 100)}%")
            toast.show()
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (eventPointerCount <= 1 || !listView.pageListViewListener.isTouchZoom()) return true
        isScaling = true
        mXScroll = 0
        mYScroll = 0
        isProcessOnScroll = false
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) { if (eventPointerCount > 1 && listView.pageListViewListener.isTouchZoom()) isScaling = false }
    override fun onShowPress(e: MotionEvent) { listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_SHOW_PRESS) }
    override fun onSingleTapUp(e: MotionEvent): Boolean { listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_SINGLE_TAP_UP); return false }
    override fun onLongPress(e: MotionEvent) { listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_LONG_PRESS) }
    override fun onClick(v: View) { listView.pageListViewListener.onEventMethod(listView, null, null, -1.0f, -1.0f, IPageListViewListener.ON_CLICK) }
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean { listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_SINGLE_TAP_CONFIRMED); return false }
    override fun onDoubleTap(e: MotionEvent): Boolean { isProcessOnScroll = true; isTouchEventIn = false; isDoubleTap = true; listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_DOUBLE_TAP); return false }
    override fun onDoubleTapEvent(e: MotionEvent): Boolean { isTouchEventIn = false; isDoubleTap = true; listView.pageListViewListener.onEventMethod(listView, e, null, -1.0f, -1.0f, IPageListViewListener.ON_DOUBLE_TAP_EVENT); return false }

    // TODO(coroutine): preserve inertial UI scrolling; use the main dispatcher in a later refactor.
    override fun run() {
        if (!mScroller.isFinished) {
            listView.pageListViewListener.setDrawPictrue(false)
            mScroller.computeScrollOffset()
            mXScroll += mScroller.currX - mScrollerLastX
            mYScroll += mScroller.currY - mScrollerLastY
            mScrollerLastX = mScroller.currX
            mScrollerLastY = mScroller.currY
            listView.requestLayout()
            listView.postOnAnimation(this)
        } else if (!isTouchEventIn) {
            listView.postRepaint(listView.currentPageView)
            listView.pageListViewListener.updateStutus(null)
            listView.pageListViewListener.setDrawPictrue(true)
        }
    }

    fun slideViewOntoScreen(pageItem: APageListItem) {
        val correction = listView.getCorrection(listView.getScrollBounds(pageItem))
        if (correction.x != 0 || correction.y != 0) {
            mScrollerLastX = 0
            mScrollerLastY = 0
            mScroller.startScroll(0, 0, correction.x, correction.y, 400)
            listView.removeCallbacks(this)
            listView.postOnAnimation(this)
        }
        listView.pageListViewListener.resetSearchResult(pageItem)
    }

    fun getScrollX() = mXScroll
    fun getScrollY() = mYScroll
    fun setScrollAxisValue(x: Int, y: Int) { mXScroll = x; mYScroll = y }
    fun isTouchEventIn() = isTouchEventIn
    fun isScrollerFinished() = mScroller.isFinished
    fun isOnFling() = isOnFling

    protected fun directionOfTravel(vx: Float, vy: Float): Int = when {
        kotlin.math.abs(vx) > 2 * kotlin.math.abs(vy) -> if (vx > 0) MOVING_RIGHT else MOVING_LEFT
        kotlin.math.abs(vy) > 2 * kotlin.math.abs(vx) -> if (vy > 0) MOVING_DOWN else MOVING_UP
        else -> MOVING_DIAGONALLY
    }

    protected fun withinBoundsInDirectionOfTravel(bounds: Rect, vx: Float, vy: Float): Boolean = when (directionOfTravel(vx, vy)) {
        MOVING_DIAGONALLY -> bounds.contains(0, 0)
        MOVING_LEFT -> bounds.left <= 0
        MOVING_RIGHT -> bounds.right >= 0
        MOVING_UP -> bounds.top <= 0
        MOVING_DOWN -> bounds.bottom >= 0
        else -> throw NoSuchElementException()
    }

    open fun dispose() { }
}
