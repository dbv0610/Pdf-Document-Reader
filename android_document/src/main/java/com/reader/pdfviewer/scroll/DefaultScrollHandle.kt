package com.reader.pdfviewer.scroll

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.reader.pdfviewer.PDFView
import com.wxiwei.office.R
import com.reader.pdfviewer.util.TouchUtils
import com.reader.pdfviewer.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DefaultScrollHandle @JvmOverloads constructor(context: Context, private val inverted: Boolean = false) :
    RelativeLayout(
        context
    ), ScrollHandle {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var hideJob: Job? = null
    protected var pageIndicator: TextView? = null
    private var relativeHandlerMiddle = 0f
    private var pdfView: PDFView? = null
    private var currentPos = 0f
    private var handleBackgroundDrawable: Drawable? = null
    private var handleView: View? = null
    private var handleAlign = 0
    private var handleWidth: Int = HANDLE_WIDTH
    private var handleHeight: Int = HANDLE_HEIGHT
    private var handlePaddingLeft = 0
    private var handlePaddingTop = 0
    private var handlePaddingRight = 0
    private var handlePaddingBottom = 0

    private var hideHandleDelayMillis = 1000

    private var hasStartedDragging = false
    private var textColorResId = -1
    private var textSize = -1


    private fun setHandleRelativePosition(pdfView: PDFView?) {
         if (pdfView?.isSwipeVertical == true) {
            handleAlign = if (inverted) ALIGN_PARENT_LEFT else ALIGN_PARENT_RIGHT
        } else {
            val tempWidth = handleWidth
            handleWidth = handleHeight
            handleHeight = tempWidth
            handleAlign = if (inverted) ALIGN_PARENT_TOP else ALIGN_PARENT_BOTTOM
        }
    }

    private fun setHandleView() {
        if (handleView != null) {
            initViewWithCustomView()
        } else {
            initDefaultView(handleBackgroundDrawable)
        }

        visibility = INVISIBLE
        if (pageIndicator != null) {
            if (textColorResId != -1) pageIndicator!!.setTextColor(textColorResId)
            if (textSize != -1) pageIndicator!!.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
        }
    }

    private fun initDefaultView(drawable: Drawable?) {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.default_handle, null)
        pageIndicator = view.findViewById<TextView?>(R.id.pageIndicator)
        pageIndicator!!.background = if (drawable != null) drawable else this.defaultHandleBackgroundDrawable
        addView(view, this.customViewLayoutParams)
        setRootLayoutParams()
    }

    private val customViewLayoutParams: LayoutParams
        get() = getLayoutParams(
            Util.getDP(context, handleWidth),
            Util.getDP(context, handleHeight),
            NO_ALIGN,
            true
        )

    private fun initViewWithCustomView() {
        if (handleView!!.parent != null) {
            removeView(handleView)
        }
        addView(handleView, this.customViewLayoutParams)
        setRootLayoutParams()
    }

    private val defaultHandleBackgroundDrawable: Drawable?
        get() {
            val drawableResId = when (handleAlign) {
                ALIGN_PARENT_LEFT -> R.drawable.default_scroll_handle_left
                ALIGN_PARENT_RIGHT -> R.drawable.default_scroll_handle_right
                ALIGN_PARENT_TOP -> R.drawable.default_scroll_handle_top
                else -> R.drawable.default_scroll_handle_bottom
            }
            return getDrawable(drawableResId)
        }

    private fun getLayoutParams(width: Int, height: Int, align: Int, withPadding: Boolean): LayoutParams {
        val layoutParams = LayoutParams(width, height)
        if (align != NO_ALIGN) layoutParams.addRule(align)

        if (withPadding) {
            layoutParams.setMargins(
                Util.getDP(context, handlePaddingLeft),
                Util.getDP(context, handlePaddingTop),
                Util.getDP(context, handlePaddingRight),
                Util.getDP(context, handlePaddingBottom)
            )
        }

        return layoutParams
    }

    private fun setRootLayoutParams() {
        layoutParams = getLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, handleAlign, false)
    }

    private fun getDrawable(resDrawable: Int): Drawable? {
        return ContextCompat.getDrawable(context, resDrawable)
    }

    override fun setScroll(position: Float) {
        if (!shown()) {
            show()
        } else {
            cancelHideDelayed()
        }
        if (pdfView != null) {
            setPosition((if (pdfView!!.isSwipeVertical) pdfView!!.height else pdfView!!.width) * position)
        }
    }

    override fun setupLayout(pdfView: PDFView?) {
        setHandleRelativePosition(pdfView)
        setHandleView()
        pdfView?.addView(this)
        this.pdfView = pdfView
    }

    override fun destroyLayout() {
        pdfView!!.removeView(this)
    }

    private fun setPosition(pos: Float) {
        var pos = pos
        if (pos.isInfinite() || pos.isNaN()) {
            return
        }
        val pdfViewSize: Float
        val handleSize: Int
        if (pdfView!!.isSwipeVertical) {
            pdfViewSize = pdfView!!.height.toFloat()
            handleSize = handleHeight
        } else {
            pdfViewSize = pdfView!!.width.toFloat()
            handleSize = handleWidth
        }
        pos -= relativeHandlerMiddle

        val maxBound = pdfViewSize - Util.getDP(context, handleSize) - this.paddings
        if (pos < 0) {
            pos = 0f
        } else if (pos > maxBound) {
            pos = maxBound
        }

        if (pdfView!!.isSwipeVertical) {
            y = pos
        } else {
            x = pos
        }

        calculateMiddle()
        invalidate()
    }

    private val paddings: Int
        get() {
            val paddings = when (handleAlign) {
                ALIGN_PARENT_LEFT, ALIGN_PARENT_RIGHT -> handlePaddingTop + handlePaddingBottom
                ALIGN_PARENT_TOP, ALIGN_PARENT_BOTTOM -> handlePaddingLeft + handlePaddingRight
                else -> 0
            }
            return Util.getDP(context, paddings)
        }

    private fun calculateMiddle() {
        val pos: Float
        val viewSize: Float
        val pdfViewSize: Float
        if (pdfView!!.isSwipeVertical) {
            pos = y
            viewSize = height.toFloat()
            pdfViewSize = pdfView!!.height.toFloat()
        } else {
            pos = x
            viewSize = width.toFloat()
            pdfViewSize = pdfView!!.width.toFloat()
        }
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize
    }

    override fun hideDelayed() {
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(hideHandleDelayMillis.toLong())
            hide()
        }
    }

    private fun cancelHideDelayed() {
        hideJob?.cancel()
        hideJob = null
    }

    override fun onDetachedFromWindow() {
        cancelHideDelayed()
        super.onDetachedFromWindow()
    }

    override fun setPageNum(pageNum: Int) {
        val text = pageNum.toString()
        if (pageIndicator != null && pageIndicator!!.text != text) {
            pageIndicator!!.text = text
        }
    }

    override fun shown(): Boolean {
        return visibility == VISIBLE
    }

    override fun show() {
        visibility = VISIBLE
    }

    override fun hide() {
        visibility = INVISIBLE
    }

    /**
     * @param color color of the text handle
     */
    fun setTextColor(color: Int) {
        textColorResId = color
    }

    /**
     * @param size text size in dp
     */
    fun setTextSize(size: Int) {
        textSize = size
    }

    /**
     * @param handleBackgroundDrawable drawable to set as a background
     */
    fun setPageHandleBackground(handleBackgroundDrawable: Drawable?) {
        this.handleBackgroundDrawable = handleBackgroundDrawable
    }

    /**
     * Use a custom view as the handle. if you want to have the page indicator,
     * provide the pageIndicator parameter.
     * 
     * @param handleView    view to set as the handle
     * @param pageIndicator TextView to use as the page indicator
     */
    fun setPageHandleView(handleView: View?, pageIndicator: TextView?) {
        this.handleView = handleView
        this.pageIndicator = pageIndicator
    }

    /**
     * @param handleWidth  width of the handle
     * @param handleHeight width of the handle
     */
    fun setHandleSize(handleWidth: Int, handleHeight: Int) {
        this.handleWidth = handleWidth
        this.handleHeight = handleHeight
    }

    /**
     * @param paddingLeft   left padding of the handle
     * @param paddingTop    top padding of the handle
     * @param paddingRight  right padding of the handle
     * @param paddingBottom bottom padding of the handle
     */
    fun setHandlePaddings(paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
        handlePaddingLeft = paddingLeft
        handlePaddingTop = paddingTop
        handlePaddingRight = paddingRight
        handlePaddingBottom = paddingBottom
    }

    /**
     * @param hideHandleDelayMillis delay in milliseconds to hide the handle after scrolling the PDF
     */
    fun setHideHandleDelay(hideHandleDelayMillis: Int) {
        this.hideHandleDelayMillis = hideHandleDelayMillis
    }

    private val isPDFViewReady: Boolean
        get() = pdfView != null && pdfView!!.pageCount > 0 && !pdfView!!.documentFitsView()

    private fun getTouchedView(event: MotionEvent): View? {
        val x = Math.round(event.x)
        val y = Math.round(event.y)
        for (i in 0..<childCount) {
            val child = getChildAt(i)
            if (x > child.left && x < child.right && y > child.top && y < child.bottom) {
                return child
            }
        }
        return null
    }

    private fun shouldIgnoreTouch(event: MotionEvent): Boolean {
        val touchedView = getTouchedView(event)
        if (hasStartedDragging) {
            return false
        } else if (touchedView != null) {
            val tag = touchedView.tag
            return tag != null && tag.toString() != "rootHandle"
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!this.isPDFViewReady || shouldIgnoreTouch(event)) {
            return super.onTouchEvent(event)
        }

        hasStartedDragging = true

        TouchUtils.handleTouchPriority(event, this, TOUCH_POINTER_COUNT, false, pdfView!!.isZooming)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                pdfView!!.stopFling()
                cancelHideDelayed()
                if (pdfView!!.isSwipeVertical) {
                    currentPos = event.rawY - y
                } else {
                    currentPos = event.rawX - x
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (pdfView!!.isSwipeVertical) {
                    setPosition(event.rawY - currentPos + relativeHandlerMiddle)
                    pdfView!!.setPositionOffset(relativeHandlerMiddle / height, false)
                } else {
                    setPosition(event.rawX - currentPos + relativeHandlerMiddle)
                    pdfView!!.setPositionOffset(relativeHandlerMiddle / width, false)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                hideDelayed()
                pdfView!!.performPageSnap()
                hasStartedDragging = false
                return true
            }
            else -> {
                return super.onTouchEvent(event)
            }
        }
    }

    companion object {
        private const val HANDLE_WIDTH = 65
        private const val HANDLE_HEIGHT = 40
        private val NO_ALIGN = -1
        private const val TOUCH_POINTER_COUNT = 1
    }
}
