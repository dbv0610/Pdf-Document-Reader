package com.alf06.document.reader.ui.home.document.office.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

/**
 * Two drag handles under the ends of the Word selection. Dragging one moves that end to the text
 * under the finger. Touches away from the handles pass through to the document.
 *
 * [range] is the selection (end exclusive), [caret] the caret rectangle of an offset in the
 * coordinates of [source], [offsetAt] the offset at a screen point, [onChange] the new selection.
 */
@SuppressLint("ViewConstructor")
internal class WordSelectionHandles(
    context: Context,
    private val source: () -> View?,
    private val range: () -> LongRange?,
    private val caret: (Long) -> Rect?,
    private val offsetAt: (rawX: Float, rawY: Float) -> Long,
    private val onChange: (start: Long, end: Long) -> Unit,
) : View(context) {
    private val density = context.resources.displayMetrics.density
    private val radius = 9 * density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1A73E8.toInt() }
    // handle tips (the text line bottom) in this view's coordinates, or null when not shown
    private var startTip: Pair<Float, Float>? = null
    private var endTip: Pair<Float, Float>? = null
    private var lineHeight = 0f
    private var dragging = 0 // 0 none, 1 start, 2 end
    private val preDraw = ViewTreeObserver.OnPreDrawListener { refresh(); true }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDraw)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDraw)
        super.onDetachedFromWindow()
    }

    /** Recomputes the handle positions; call after the selection changed. */
    fun refresh() {
        val view = source()
        val r = range()
        var s: Pair<Float, Float>? = null
        var e: Pair<Float, Float>? = null
        if (view != null && r != null && !r.isEmpty()) {
            val a = IntArray(2); val b = IntArray(2)
            view.getLocationOnScreen(a); getLocationOnScreen(b)
            val dx = (a[0] - b[0]).toFloat(); val dy = (a[1] - b[1]).toFloat()
            caret(r.first)?.let { s = it.left + dx to it.bottom + dy; lineHeight = it.height().toFloat() }
            caret(r.last + 1)?.let { e = it.left + dx to it.bottom + dy }
        }
        if (s != startTip || e != endTip) {
            startTip = s; endTip = e
            invalidate()
        }
    }

    private fun near(tip: Pair<Float, Float>?, x: Float, y: Float): Boolean {
        tip ?: return false
        // the grab area is the knob below the tip, generously sized for a finger
        val cy = tip.second + radius
        return Math.hypot((x - tip.first).toDouble(), (y - cy).toDouble()) <= radius * 3
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragging = when {
                    near(endTip, event.x, event.y) -> 2
                    near(startTip, event.x, event.y) -> 1
                    else -> 0
                }
                if (dragging != 0) parent?.requestDisallowInterceptTouchEvent(true)
                return dragging != 0
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging == 0) return false
                val r = range() ?: return true
                // aim at the text line above the knob, not under the finger
                val offset = offsetAt(event.rawX, event.rawY - radius - lineHeight / 2)
                if (offset < 0) return true
                if (dragging == 1) {
                    if (offset < r.last + 1) onChange(offset, r.last + 1)
                } else if (offset > r.first) onChange(r.first, offset)
                refresh()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val was = dragging != 0
                dragging = 0
                return was
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        for (tip in listOf(startTip, endTip)) {
            tip ?: continue
            canvas.drawRect(tip.first - density, tip.second - lineHeight, tip.first + density, tip.second + radius, paint)
            canvas.drawCircle(tip.first, tip.second + radius, radius, paint)
        }
    }
}
