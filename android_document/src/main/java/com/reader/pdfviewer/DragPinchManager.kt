package com.reader.pdfviewer

import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import com.reader.pdfviewer.model.LinkTapEvent
import com.reader.pdfviewer.util.Constants.Pinch.MAXIMUM_ZOOM
import com.reader.pdfviewer.util.Constants.Pinch.MINIMUM_ZOOM
import com.reader.pdfviewer.util.MathUtils
import com.reader.pdfviewer.util.TouchUtils
import kotlin.collections.orEmpty
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * This Manager takes care of moving the PDFView,
 * set its zoom track user actions.
 */
internal class DragPinchManager(private val pdfView: PDFView, private val animationManager: AnimationManager) :
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, OnScaleGestureListener, OnTouchListener {
    private val gestureDetector: GestureDetector = GestureDetector(pdfView.context, this)
    private val scaleGestureDetector: ScaleGestureDetector =
        ScaleGestureDetector(pdfView.getContext(), this)

    private var inkNavigation = false
    private var inkDown: MotionEvent? = null
    private var scrolling = false
    private var scaling = false
    private var selectingText = false
    private var magnifying = false
    private var draggingSelectionHandle = false
    private var draggingStartHandle = false
    private var enabled = false
    private var hasTouchPriority = false
    private var startingScrollingXPosition: Float = STARTING_TOUCH_POSITION_NOT_INITIALIZED
    private var startingTouchXPosition: Float = STARTING_TOUCH_POSITION_NOT_INITIALIZED
    private var startingTouchYPosition: Float = STARTING_TOUCH_POSITION_NOT_INITIALIZED

    init {
        pdfView.setOnTouchListener(this)
    }

    fun enable() {
        enabled = true
    }

    fun disable() {
        inkDown?.recycle()
        inkDown = null
        inkNavigation = false
        enabled = false
    }

    fun hasTouchPriority(): Boolean {
        return hasTouchPriority
    }

    fun setHasTouchPriority(hasTouchPriority: Boolean) {
        this.hasTouchPriority = hasTouchPriority
    }

    fun disableLongPress() {
        gestureDetector.setIsLongpressEnabled(false)
    }

    private fun setSelectionTouchPriority(disallowIntercept: Boolean) {
        if (!hasTouchPriority) {
            return
        }
        TouchUtils.Companion.handleSelectionGestureTouchPriority(pdfView, disallowIntercept)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (pdfView.isDrawingMode) return true
        if (pdfView.hasTextSelection()) {
            pdfView.clearTextSelection()
            return true
        }
        val onTapHandled = pdfView.callbacks.callOnTap(e)
        val linkTapped = checkLinkTapped(e.getX(), e.getY())
        if (!onTapHandled && !linkTapped) {
            val ps = pdfView.scrollHandle
            if (ps != null && !pdfView.documentFitsView()) {
                if (!ps.shown()) {
                    ps.show()
                } else {
                    ps.hide()
                }
            }
        }
        pdfView.performClick()
        return true
    }

    private fun checkLinkTapped(x: Float, y: Float): Boolean {
        val pdfFile = pdfView.pdfFile
        if (pdfFile == null) {
            return false
        }
        val mappedX = -pdfView.currentXOffset + x
        val mappedY = -pdfView.currentYOffset + y
        val page = pdfFile.getPageAtOffset(if (pdfView.isSwipeVertical) mappedY else mappedX, pdfView.zoom)
        val pageSize = pdfFile.getScaledPageSize(page, pdfView.zoom)
        val pageX: Int
        val pageY: Int
        if (pdfView.isSwipeVertical) {
            pageX = pdfFile.getSecondaryPageOffset(page, pdfView.zoom).toInt()
            pageY = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        } else {
            pageY = pdfFile.getSecondaryPageOffset(page, pdfView.zoom).toInt()
            pageX = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        }
        for (link in pdfFile.getPageLinks(page).orEmpty()) {
            if (link == null) continue
            val mapped = pdfFile.mapRectToDevice(
                page, pageX, pageY, pageSize.width.toInt(), pageSize.height.toInt(),
                link.bounds
            ) ?: continue
            mapped.sort()
            if (mapped.contains(mappedX, mappedY)) {
                pdfView.callbacks.callLinkHandler(LinkTapEvent(x, y, mappedX, mappedY, mapped, link))
                return true
            }
        }
        return false
    }

    private fun startPageFling(
        downEvent: MotionEvent, ev: MotionEvent, velocityX: Float,
        velocityY: Float
    ) {
        if (!checkDoPageFling(velocityX, velocityY)) {
            return
        }

        val direction: Int
        if (pdfView.isSwipeVertical) {
            direction = if (velocityY > 0) -1 else 1
        } else {
            direction = if (velocityX > 0) -1 else 1
        }
        // Continue to the next page in the fling direction from where the finger was lifted,
        // after a long drag the page where it started may already be behind and the fling must not go back
        val pdfFile = pdfView.pdfFile ?: return
        val velocity = if (pdfView.isSwipeVertical) velocityY else velocityX
        var maxDuration = 0L
        val targetPage: Int
        if (pdfView.isPageSnap) {
            // Snapping aligns the page in the center of the screen, start from that page
            val startingPage = pdfView.findFocusPage(pdfView.currentXOffset, pdfView.currentYOffset)
            targetPage = startingPage + direction
        } else {
            // Without snapping the target page is aligned to the start of the screen, start from the page there
            val offset = if (pdfView.isSwipeVertical) pdfView.currentYOffset else pdfView.currentXOffset
            val startingPage = pdfFile.getPageAtOffset(-offset, pdfView.zoom)
            val startingPageHidden = offset < -pdfFile.getPageOffset(startingPage, pdfView.zoom) - 1
            // Going back first shows the start of the page that is partly scrolled out
            val nextPage = if (direction < 0 && startingPageHidden) startingPage else startingPage + direction
            if (pdfView.isPageFlingByVelocity) {
                // A strong fling goes on to the page starting nearest to where a free fling would stop;
                // a light one still moves one page.
                val (projected, projectedDuration) = animationManager.projectFling(velocity)
                val landingPage = nearestPageStart(-projected)
                targetPage = if (direction > 0) max(nextPage, landingPage) else min(nextPage, landingPage)
                maxDuration = projectedDuration.toLong()
            } else {
                targetPage = nextPage
            }
        }
        val page = max(0, min(pdfView.pageCount - 1, targetPage))

        val edge = pdfView.findSnapEdge(page)
        val offset = pdfView.snapOffsetForPage(page, edge)
        animationManager.startPageFlingAnimation(-offset, velocity, maxDuration)
    }

    /** The page whose start is closest to [docOffset], a position in the document at the current zoom. */
    private fun nearestPageStart(docOffset: Float): Int {
        val pdfFile = pdfView.pdfFile ?: return 0
        val zoom = pdfView.zoom
        val page = pdfFile.getPageAtOffset(docOffset, zoom)
        if (page + 1 >= pdfFile.pagesCount) return page
        val toStart = docOffset - pdfFile.getPageOffset(page, zoom)
        val toNext = pdfFile.getPageOffset(page + 1, zoom) - docOffset
        return if (toNext < toStart) page + 1 else page
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (pdfView.isDrawingMode || !pdfView.isDoubleTapEnabled) {
            return false
        }

        if (pdfView.zoom < pdfView.midZoom) {
            pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.midZoom)
        } else if (pdfView.zoom < pdfView.maxZoom) {
            pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.maxZoom)
        } else {
            pdfView.resetZoomWithAnimation()
        }
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        // Ignore if touching a selection handle
        if (pdfView.hasTextSelection()
            && (pdfView.isStartHandleTouched(e.getX(), e.getY()) || pdfView.isEndHandleTouched(e.getX(), e.getY()))
        ) {
            return false // Let onTouch handle the selection handles
        }
        // A touch during an animated jump or page fling takes over from it; left running, the animation
        // and the new drag would both move the page every frame and make it shake.
        if (pdfView.isJumping || animationManager.isFlinging()) animationManager.stopAll() else animationManager.stopFling()
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        // Nothing to do here since we don't want to show anything when the user press the view
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (draggingSelectionHandle) {
            return true
        }
        scrolling = true
        if ((pdfView.isDrawingMode && inkNavigation) || pdfView.isZooming || pdfView.isSwipeEnabled) {
            pdfView.moveRelativeTo(-distanceX, -distanceY)
        }
        if (!scaling || pdfView.doRenderDuringScale()) {
            pdfView.loadPageByOffset()
        }
        return true
    }

    private fun onScrollEnd() {
        pdfView.loadPages()
        hideHandle()
        if (!animationManager.isFlinging()) {
            pdfView.performPageSnap()
        }
    }

    override fun onLongPress(e: MotionEvent) {
        if (pdfView.isDrawingMode) return
        pdfView.performLongClick()
        // Check if touching a selection handle
        if (pdfView.hasTextSelection()) {
            if (pdfView.isStartHandleTouched(e.getX(), e.getY())) {
                pdfView.startSelectionHandleDrag(true, e.getX(), e.getY())
                draggingSelectionHandle = true
                draggingStartHandle = true
                setSelectionTouchPriority(true)
                return
            }
            if (pdfView.isEndHandleTouched(e.getX(), e.getY())) {
                pdfView.startSelectionHandleDrag(false, e.getX(), e.getY())
                draggingSelectionHandle = true
                draggingStartHandle = false
                setSelectionTouchPriority(true)
                return
            }
        }
        if (pdfView.isTextSelectionEnabled && pdfView.startTextSelection(e.getX(), e.getY())) {
            selectingText = true
            setSelectionTouchPriority(true)
            return
        }
        // No text under the finger, zoom on it instead
        if (pdfView.showMagnifier(e.getX(), e.getY())) {
            magnifying = true
            setSelectionTouchPriority(true)
        }
        pdfView.callbacks.callOnLongPress(e)
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (draggingSelectionHandle) {
            return true
        }
        if (!pdfView.isSwipeEnabled) {
            return false
        }
        if (pdfView.isPageFlingEnabled) {
            if (pdfView.pageFillsScreen()) {
                onBoundedFling(velocityX, velocityY)
            } else if (e1 != null) {
                startPageFling(e1, e2, velocityX, velocityY)
            }
            return true
        }

        val xOffset = pdfView.currentXOffset.toInt()
        val yOffset = pdfView.currentYOffset.toInt()

        val minX: Float
        val minY: Float
        val pdfFile = pdfView.pdfFile ?: return false
        if (pdfView.isSwipeVertical) {
            minX = -(pdfView.toCurrentScale(pdfFile.maxPageWidth) - pdfView.width)
            minY = -(pdfFile.getDocLen(pdfView.zoom) - pdfView.height)
        } else {
            minX = -(pdfFile.getDocLen(pdfView.zoom) - pdfView.width)
            minY = -(pdfView.toCurrentScale(pdfFile.maxPageHeight) - pdfView.height)
        }

        animationManager.startFlingAnimation(
            xOffset, yOffset, (velocityX).toInt(), (velocityY).toInt(), minX.toInt(), 0, minY.toInt(),
            0
        )
        return true
    }

    private fun onBoundedFling(velocityX: Float, velocityY: Float) {
        val xOffset = pdfView.currentXOffset.toInt()
        val yOffset = pdfView.currentYOffset.toInt()

        val pdfFile = pdfView.pdfFile ?: return

        val pageStart = -pdfFile.getPageOffset(pdfView.currentPage, pdfView.zoom)
        val pageEnd = pageStart - pdfFile.getPageLength(pdfView.currentPage, pdfView.zoom)
        val minX: Float
        val minY: Float
        val maxX: Float
        val maxY: Float
        if (pdfView.isSwipeVertical) {
            minX = -(pdfView.toCurrentScale(pdfFile.maxPageWidth) - pdfView.width)
            minY = pageEnd + pdfView.height
            maxX = 0f
            maxY = pageStart
        } else {
            minX = pageEnd + pdfView.width
            minY = -(pdfView.toCurrentScale(pdfFile.maxPageHeight) - pdfView.height)
            maxX = pageStart
            maxY = 0f
        }

        animationManager.startFlingAnimation(
            xOffset, yOffset, (velocityX).toInt(), (velocityY).toInt(), minX.toInt(), maxX.toInt(),
            minY.toInt(), maxY.toInt()
        )
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var dr = detector.getScaleFactor()
        val wantedZoom = pdfView.zoom * dr
        val minZoom = MathUtils.limit(pdfView.minZoom, MINIMUM_ZOOM, MAXIMUM_ZOOM)
        val maxZoom = MathUtils.limit(pdfView.maxZoom, minZoom, MAXIMUM_ZOOM)
        if (wantedZoom < minZoom) {
            dr = minZoom / pdfView.zoom
        } else if (wantedZoom > maxZoom) {
            dr = maxZoom / pdfView.zoom
        }
        pdfView.zoomCenteredRelativeTo(dr, PointF(detector.getFocusX(), detector.getFocusY()))
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        scaling = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        pdfView.loadPages()
        hideHandle()
        scaling = false
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!enabled) {
            return false
        }

        if (pdfView.isDrawingMode) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    inkNavigation = false
                    selectingText = false
                    magnifying = false
                    draggingSelectionHandle = false
                    inkDown?.recycle()
                    inkDown = MotionEvent.obtain(event)
                    pdfView.parent?.requestDisallowInterceptTouchEvent(true)
                    pdfView.startInkStroke(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (!inkNavigation) {
                        pdfView.cancelInkStroke()
                        inkNavigation = true
                        // Seed both detectors with DOWN before handing over POINTER_DOWN.
                        inkDown?.let {
                            scaleGestureDetector.onTouchEvent(it)
                            gestureDetector.onTouchEvent(it)
                            it.recycle()
                        }
                        inkDown = null
                    }
                }
                MotionEvent.ACTION_MOVE -> if (!inkNavigation) {
                    for (index in 0 until event.historySize) {
                        pdfView.addInkPoint(event.getHistoricalX(index), event.getHistoricalY(index))
                    }
                    pdfView.addInkPoint(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (!inkNavigation) {
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        pdfView.addInkPoint(event.x, event.y)
                        pdfView.finishInkStroke()
                    } else pdfView.cancelInkStroke()
                    inkDown?.recycle()
                    inkDown = null
                    pdfView.parent?.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
            // After the second finger, consume the entire gesture through its final UP/CANCEL.
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                pdfView.cancelInkStroke()
                inkNavigation = false
                scrolling = false
                onScrollEnd()
                pdfView.parent?.requestDisallowInterceptTouchEvent(false)
            }
            return true
        }

        // Check for handle dragging on ACTION_DOWN
        if (event.action == MotionEvent.ACTION_DOWN && pdfView.hasTextSelection()) {
            if (pdfView.isStartHandleTouched(event.getX(), event.getY())) {
                pdfView.startSelectionHandleDrag(true, event.getX(), event.getY())
                draggingSelectionHandle = true
                draggingStartHandle = true
                setSelectionTouchPriority(true)
                return true
            }
            if (pdfView.isEndHandleTouched(event.getX(), event.getY())) {
                pdfView.startSelectionHandleDrag(false, event.getX(), event.getY())
                draggingSelectionHandle = true
                draggingStartHandle = false
                setSelectionTouchPriority(true)
                return true
            }
        }

        if (draggingSelectionHandle) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                setSelectionTouchPriority(true)
                if (draggingStartHandle) {
                    pdfView.extendSelectionFromStart(event.getX(), event.getY())
                } else {
                    pdfView.extendSelectionFromEnd(event.getX(), event.getY())
                }
                return true
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                draggingSelectionHandle = false
                draggingStartHandle = false
                setSelectionTouchPriority(false)
                pdfView.finishTextSelection()
                return true
            }
        }

        if (magnifying) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                pdfView.showMagnifier(event.getX(), event.getY())
                return true
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                magnifying = false
                setSelectionTouchPriority(false)
                pdfView.hideMagnifier()
                return true
            }
        }

        if (selectingText) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                setSelectionTouchPriority(true)
                pdfView.updateTextSelection(event.getX(), event.getY())
                return true
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                selectingText = false
                setSelectionTouchPriority(false)
                pdfView.finishTextSelection()
                return true
            }
        }

        var retVal = scaleGestureDetector.onTouchEvent(event)
        retVal = gestureDetector.onTouchEvent(event) || retVal

        if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() >= TOUCH_POINTER_COUNT) {
            startingScrollingXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
            startingTouchXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
            startingTouchYPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
        }

        if (event.getAction() == MotionEvent.ACTION_UP && scrolling) {
            startingScrollingXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
            startingTouchXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
            startingTouchYPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
            scrolling = false
            onScrollEnd()
        }

        if (hasTouchPriority) {
            TouchUtils.Companion.handleTouchPriority(
                event,
                v,
                TOUCH_POINTER_COUNT,
                shouldOverrideTouchPriority(v, event),
                pdfView.isZooming
            )
        }

        return retVal
    }

    private fun shouldOverrideTouchPriority(v: View, event: MotionEvent): Boolean {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleActionDownEvent(v, event)
        }

        val canScrollLeft = v.canScrollHorizontally(TouchUtils.Companion.DIRECTION_SCROLLING_LEFT)
        val canScrollRight = v.canScrollHorizontally(TouchUtils.Companion.DIRECTION_SCROLLING_RIGHT)
        val canScrollHorizontally = canScrollLeft && canScrollRight

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            handleActionMoveEvent(event, canScrollHorizontally)
        }

        val scrollDirection = getScrollingDirection(event.getX())
        val isScrollingBlocked =
            (!canScrollRight && scrollDirection == TouchUtils.Companion.DIRECTION_SCROLLING_LEFT)
                    || (!canScrollLeft && scrollDirection == TouchUtils.Companion.DIRECTION_SCROLLING_RIGHT)

        if (!isScrollingBlocked || startingTouchXPosition == STARTING_TOUCH_POSITION_NOT_INITIALIZED) {
            return false
        } else {
            val deltaX = abs(event.getX() - startingTouchXPosition)
            val deltaY = abs(event.getY() - startingTouchYPosition)
            return deltaX >= MIN_TRIGGER_DELTA_X_TOUCH_PRIORITY && deltaY < MIN_TRIGGER_DELTA_Y_TOUCH_PRIORITY
        }
    }

    private fun handleActionMoveEvent(event: MotionEvent, canScrollHorizontally: Boolean) {
        if (canScrollHorizontally) {
            startingTouchXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
        } else if (startingTouchXPosition == STARTING_TOUCH_POSITION_NOT_INITIALIZED) {
            startingTouchXPosition = event.getX()
        }
    }

    private fun handleActionDownEvent(v: View, event: MotionEvent) {
        startingScrollingXPosition = event.getX()
        if (!v.canScrollHorizontally(TouchUtils.Companion.DIRECTION_SCROLLING_LEFT) || !v.canScrollHorizontally(TouchUtils.Companion.DIRECTION_SCROLLING_RIGHT)) {
            startingTouchXPosition = event.getX()
        } else {
            startingTouchXPosition = STARTING_TOUCH_POSITION_NOT_INITIALIZED
        }
        startingTouchYPosition = event.getY()
    }

    private fun getScrollingDirection(x: Float): Int {
        if (x > startingScrollingXPosition) {
            return TouchUtils.Companion.DIRECTION_SCROLLING_RIGHT
        } else {
            return TouchUtils.Companion.DIRECTION_SCROLLING_LEFT
        }
    }

    private fun hideHandle() {
        val scrollHandle = pdfView.scrollHandle
        if (scrollHandle != null && scrollHandle.shown()) {
            scrollHandle.hideDelayed()
        }
    }

    private fun checkDoPageFling(velocityX: Float, velocityY: Float): Boolean {
        val absX = abs(velocityX)
        val absY = abs(velocityY)
        return if (pdfView.isSwipeVertical) absY > absX else absX > absY
    }

    companion object {
        private const val MIN_TRIGGER_DELTA_X_TOUCH_PRIORITY = 150f
        private const val MIN_TRIGGER_DELTA_Y_TOUCH_PRIORITY = 100f
        private val STARTING_TOUCH_POSITION_NOT_INITIALIZED = -1f
        private const val TOUCH_POINTER_COUNT = 2
    }
}
