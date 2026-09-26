package com.reader.pdfviewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.PointF
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import android.widget.Scroller
import kotlin.math.abs

/**
 * This manager is used by the PDFView to launch animations.
 * It uses the ValueAnimator appeared in API 11 to start
 * an animation, and call moveTo() on the PDFView as a result
 * of each animation update.
 */
internal class AnimationManager(private val pdfView: PDFView) {
    private var animation: ValueAnimator? = null

    private val scroller: OverScroller = OverScroller(pdfView.context)

    /** Only computes where a free fling would stop; never drives the view. */
    private val projector: Scroller = Scroller(pdfView.context)

    private var flinging = false

    private var pageFlinging = false

    fun startXAnimation(xFrom: Float, xTo: Float, duration: Long = DURATION) {
        stopAll()
        animation = ValueAnimator.ofFloat(xFrom, xTo)
        val xAnimation = XAnimation()
        animation!!.interpolator = DecelerateInterpolator()
        animation!!.addUpdateListener(xAnimation)
        animation!!.addListener(xAnimation)
        animation!!.duration = duration
        animation!!.start()
    }

    fun startYAnimation(yFrom: Float, yTo: Float, duration: Long = DURATION) {
        stopAll()
        animation = ValueAnimator.ofFloat(yFrom, yTo)
        val yAnimation = YAnimation()
        animation!!.interpolator = DecelerateInterpolator()
        animation!!.addUpdateListener(yAnimation)
        animation!!.addListener(yAnimation)
        animation!!.duration = duration
        animation!!.start()
    }

    fun startZoomAnimation(centerX: Float, centerY: Float, zoomFrom: Float, zoomTo: Float) {
        stopAll()
        animation = ValueAnimator.ofFloat(zoomFrom, zoomTo)
        animation!!.interpolator = DecelerateInterpolator()
        val zoomAnim = ZoomAnimation(centerX, centerY)
        animation!!.addUpdateListener(zoomAnim)
        animation!!.addListener(zoomAnim)
        animation!!.duration = DURATION
        animation!!.start()
    }

    fun startFlingAnimation(
        startX: Int,
        startY: Int,
        velocityX: Int,
        velocityY: Int,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ) {
        stopAll()
        flinging = true
        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
    }

    /**
     * Where a free fling at [velocity] (px/s) along the swipe axis would stop, as a view offset, and
     * how long it would take in ms.
     */
    fun projectFling(velocity: Float): Pair<Float, Int> {
        val start = (if (pdfView.isSwipeVertical) pdfView.currentYOffset else pdfView.currentXOffset).toInt()
        projector.fling(0, start, 0, velocity.toInt(), 0, 0, Int.MIN_VALUE / 2, Int.MAX_VALUE / 2)
        val end = projector.finalY.toFloat() to projector.duration
        projector.forceFinished(true)
        return end
    }

    /**
     * Animates to [targetOffset], starting at the fling's [velocity] (px/s) so the page does not jolt
     * on release; [maxDuration] lets a fling over several pages take as long as a free fling would.
     */
    fun startPageFlingAnimation(targetOffset: Float, velocity: Float, maxDuration: Long = DURATION) {
        val from = if (pdfView.isSwipeVertical) pdfView.currentYOffset else pdfView.currentXOffset
        val longest = maxDuration.coerceIn(DURATION, MAX_PAGE_FLING_DURATION)
        // DecelerateInterpolator starts at twice the average speed: 2 * distance / duration.
        val duration = if (velocity == 0f) DURATION else {
            (2000 * abs(targetOffset - from) / abs(velocity)).toLong().coerceIn(MIN_PAGE_FLING_DURATION, longest)
        }
        if (pdfView.isSwipeVertical) {
            startYAnimation(from, targetOffset, duration)
        } else {
            startXAnimation(from, targetOffset, duration)
        }
        pageFlinging = true
    }

    fun computeFling() {
        if (scroller.computeScrollOffset()) {
            if (shouldHandleScrollerValue()) {
                pdfView.moveTo(scroller.currX.toFloat(), scroller.currY.toFloat())
                pdfView.loadPageByOffset()
            }
        } else if (flinging) { // fling finished
            flinging = false
            pdfView.loadPages()
            hideHandle()
            pdfView.performPageSnap()
        }
    }

    private fun shouldHandleScrollerValue(): Boolean {
        return if (pdfView.isSwipeVertical) {
            scroller.currY != 0 && scroller.currY != pdfView.documentLength
        } else {
            scroller.currX != 0 && scroller.currX != pdfView.documentLength
        }
    }

    fun stopAll() {
        if (animation != null) {
            animation!!.cancel()
            animation = null
        }
        stopFling()
    }

    fun stopFling() {
        flinging = false
        scroller.forceFinished(true)
    }

    fun isFlinging(): Boolean {
        return flinging || pageFlinging
    }

    private fun hideHandle() {
        if (pdfView.scrollHandle != null) {
            pdfView.scrollHandle?.hideDelayed()
        }
    }

    internal inner class XAnimation : AnimatorListenerAdapter(), AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            pdfView.moveTo(offset, pdfView.currentYOffset)
            pdfView.loadPageByOffset()
        }

        override fun onAnimationCancel(animation: Animator) {
            pdfView.onScrollAnimationFinished()
            pdfView.loadPages()
            pageFlinging = false
            hideHandle()
        }

        override fun onAnimationEnd(animation: Animator) {
            pdfView.onScrollAnimationFinished()
            pdfView.loadPages()
            pageFlinging = false
            hideHandle()
        }
    }

    internal inner class YAnimation : AnimatorListenerAdapter(), AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val offset = animation.animatedValue as Float
            pdfView.moveTo(pdfView.currentXOffset, offset)
            pdfView.loadPageByOffset()
        }

        override fun onAnimationCancel(animation: Animator) {
            pdfView.onScrollAnimationFinished()
            pdfView.loadPages()
            pageFlinging = false
            hideHandle()
        }

        override fun onAnimationEnd(animation: Animator) {
            pdfView.onScrollAnimationFinished()
            pdfView.loadPages()
            pageFlinging = false
            hideHandle()
        }
    }

    internal inner class ZoomAnimation(private val centerX: Float, private val centerY: Float) : AnimatorUpdateListener,
        Animator.AnimatorListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val zoom = animation.animatedValue as Float
            pdfView.zoomCenteredTo(zoom, PointF(centerX, centerY))
        }

        override fun onAnimationCancel(animation: Animator) {
            pdfView.loadPages()
            hideHandle()
        }

        override fun onAnimationEnd(animation: Animator) {
            pdfView.loadPages()
            pdfView.performPageSnap()
            hideHandle()
        }

        override fun onAnimationRepeat(animation: Animator) {
        }

        override fun onAnimationStart(animation: Animator) {
        }
    }

    private companion object {
        const val DURATION = 400L
        const val MIN_PAGE_FLING_DURATION = 150L
        const val MAX_PAGE_FLING_DURATION = 1200L
    }
}
