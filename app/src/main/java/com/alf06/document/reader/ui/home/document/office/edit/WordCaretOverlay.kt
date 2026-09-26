package com.alf06.document.reader.ui.home.document.office.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

/**
 * Blinking text caret over the Word view. [caret] gives the caret in the coordinates of [source]
 * (see WordSelection.caretRect); it is asked again before every frame, so the caret follows
 * scrolling, zooming and relayout. Touches pass through.
 */
@SuppressLint("ViewConstructor")
internal class WordCaretOverlay(
    context: Context,
    private val source: () -> View?,
    private val caret: () -> Rect?,
) : View(context) {
    /** Shows or hides the caret. */
    var active = false
        set(value) {
            field = value
            shown = true
            removeCallbacks(blink)
            if (value) postDelayed(blink, BLINK_MS)
            refresh()
            invalidate()
        }

    private val rect = Rect()
    private var visible = false
    private var shown = true
    private val paint = Paint().apply {
        color = 0xFF1A73E8.toInt()
        strokeWidth = 2 * context.resources.displayMetrics.density
    }
    private val blink = object : Runnable {
        override fun run() {
            shown = !shown
            invalidate()
            if (active) postDelayed(this, BLINK_MS)
        }
    }
    private val preDraw = ViewTreeObserver.OnPreDrawListener { refresh(); true }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDraw)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDraw)
        removeCallbacks(blink)
        super.onDetachedFromWindow()
    }

    /** Restarts the blink so the caret stays solid while typing. */
    fun touch() {
        shown = true
        removeCallbacks(blink)
        if (active) postDelayed(blink, BLINK_MS)
        invalidate()
    }

    private fun refresh() {
        val view = source()
        val r = if (active && view != null) caret() else null
        if (r == null) {
            if (visible) { visible = false; invalidate() }
            return
        }
        val a = IntArray(2); val b = IntArray(2)
        view!!.getLocationOnScreen(a); getLocationOnScreen(b)
        r.offset(a[0] - b[0], a[1] - b[1])
        if (!visible || r != rect) {
            rect.set(r)
            visible = true
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!visible || !shown) return
        val x = rect.left.toFloat()
        canvas.drawLine(x, rect.top.toFloat(), x, rect.bottom.toFloat(), paint)
    }

    private companion object {
        const val BLINK_MS = 530L
    }
}
