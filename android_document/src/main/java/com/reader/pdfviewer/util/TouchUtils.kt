package com.reader.pdfviewer.util

import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class TouchUtils private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        val DIRECTION_SCROLLING_LEFT: Int = -1
        const val DIRECTION_SCROLLING_RIGHT: Int = 1
        val DIRECTION_SCROLLING_TOP: Int = -1
        const val DIRECTION_SCROLLING_BOTTOM: Int = 1

        fun handleTouchPriority(
            event: MotionEvent,
            view: View,
            pointerCount: Int,
            shouldOverrideTouchPriority: Boolean,
            isZooming: Boolean
        ) {
            val viewToDisableTouch: ViewParent? = getViewToDisableTouch(view)

            if (viewToDisableTouch == null) {
                return
            }

            val canScrollHorizontally =
                view.canScrollHorizontally(DIRECTION_SCROLLING_RIGHT) && view.canScrollHorizontally(DIRECTION_SCROLLING_LEFT)
            val canScrollVertically =
                view.canScrollVertically(DIRECTION_SCROLLING_TOP) && view.canScrollVertically(DIRECTION_SCROLLING_BOTTOM)
            if (shouldOverrideTouchPriority) {
                viewToDisableTouch.requestDisallowInterceptTouchEvent(false)

                val viewPager: ViewParent? = getViewPager(view)
                if (viewPager != null) {
                    viewPager.requestDisallowInterceptTouchEvent(true)
                }
            } else if (event.getPointerCount() >= pointerCount || canScrollHorizontally || canScrollVertically) {
                val action = event.getAction()

                if (action == MotionEvent.ACTION_UP) {
                    viewToDisableTouch.requestDisallowInterceptTouchEvent(false)
                } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || isZooming) {
                    viewToDisableTouch.requestDisallowInterceptTouchEvent(true)
                }
            }
        }

        fun handleSelectionGestureTouchPriority(view: View, disallowIntercept: Boolean) {
            val recyclerViewParent: ViewParent? = getViewToDisableTouch(view)
            if (recyclerViewParent != null) {
                recyclerViewParent.requestDisallowInterceptTouchEvent(disallowIntercept)
            }

            val viewPagerParent: ViewParent? = getViewPager(view)
            if (viewPagerParent != null) {
                viewPagerParent.requestDisallowInterceptTouchEvent(disallowIntercept)
            }
        }

        private fun getViewToDisableTouch(startingView: View): ViewParent? {
            var parentView = startingView.getParent()

            while (parentView != null && parentView !is RecyclerView) {
                parentView = parentView.getParent()
            }

            return parentView
        }

        private fun getViewPager(startingView: View): ViewParent? {
            var parentView = startingView.getParent()

            while (parentView != null && parentView !is ViewPager2) {
                parentView = parentView.getParent()
            }

            return parentView
        }
    }
}
