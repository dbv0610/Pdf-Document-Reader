package com.alf06.document.reader.ui.home.document.office.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

/**
 * Frame over the selected picture of a Word document, with a handle at each corner. Dragging the
 * picture moves it ([onMove] with the finger's screen point and the shift in view pixels on
 * release); dragging a corner resizes it keeping its proportions ([onResize] with the new size in
 * view pixels). [frame] gives the picture's box in the coordinates of [source] before every frame.
 * Touches outside the frame pass through.
 */
@SuppressLint("ViewConstructor")
internal class WordPictureOverlay(
    context: Context,
    private val source: () -> View?,
    private val frame: () -> Rect?,
    private val onMove: (rawX: Float, rawY: Float, dx: Float, dy: Float) -> Unit,
    private val onResize: (width: Float, height: Float) -> Unit,
) : View(context) {
    /** Shows the frame (a picture is selected) or hides it. */
    var active = false
        set(value) {
            field = value
            refresh()
            invalidate()
        }

    private val density = context.resources.displayMetrics.density
    private val box = RectF()
    private val drag = RectF()
    private var visible = false
    private var mode = 0 // 0 none, 1 move, 2..5 corner (tl, tr, bl, br)
    private var downX = 0f
    private var downY = 0f
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2 * density; color = 0xFF1A73E8.toInt()
        pathEffect = DashPathEffect(floatArrayOf(8 * density, 5 * density), 0f)
    }
    private val handle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1A73E8.toInt() }
    private val handleFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val preDraw = ViewTreeObserver.OnPreDrawListener { if (mode == 0) refresh(); true }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDraw)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDraw)
        super.onDetachedFromWindow()
    }

    private fun refresh() {
        val view = source()
        val r = if (active && view != null) frame() else null
        if (r == null) {
            if (visible) { visible = false; invalidate() }
            return
        }
        val a = IntArray(2); val b = IntArray(2)
        view!!.getLocationOnScreen(a); getLocationOnScreen(b)
        val next = RectF(r).apply { offset((a[0] - b[0]).toFloat(), (a[1] - b[1]).toFloat()) }
        if (!visible || next != box) {
            box.set(next)
            visible = true
            invalidate()
        }
    }

    private fun corners(r: RectF) = listOf(r.left to r.top, r.right to r.top, r.left to r.bottom, r.right to r.bottom)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!visible) return false
        val reach = 24 * density
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val corner = corners(box).indexOfFirst { (x, y) -> Math.hypot((event.x - x).toDouble(), (event.y - y).toDouble()) <= reach }
                mode = when {
                    corner >= 0 -> 2 + corner
                    box.contains(event.x, event.y) -> 1
                    else -> return false
                }
                downX = event.x; downY = event.y
                drag.set(box)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY
                if (mode == 1) drag.set(box.left + dx, box.top + dy, box.right + dx, box.bottom + dy)
                else if (mode >= 2) {
                    // proportional: the larger of the two moves decides
                    val ratio = box.height() / box.width()
                    val sx = if (mode == 2 || mode == 4) -dx else dx
                    val sy = if (mode == 2 || mode == 3) -dy else dy
                    val w = maxOf(16 * density, box.width() + maxOf(sx, sy / ratio))
                    val h = w * ratio
                    val left = if (mode == 2 || mode == 4) box.right - w else box.left
                    val top = if (mode == 2 || mode == 3) box.bottom - h else box.top
                    drag.set(left, top, left + w, top + h)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val m = mode
                mode = 0
                val dx = event.x - downX
                val dy = event.y - downY
                if (m == 1 && Math.hypot(dx.toDouble(), dy.toDouble()) > 8 * density) onMove(event.rawX, event.rawY, dx, dy)
                else if (m >= 2 && (drag.width() != box.width())) onResize(drag.width(), drag.height())
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                mode = 0
                invalidate()
                return true
            }
        }
        return mode != 0
    }

    override fun onDraw(canvas: Canvas) {
        if (!visible) return
        val r = if (mode != 0) drag else box
        canvas.drawRect(r, line)
        for ((x, y) in corners(r)) {
            canvas.drawCircle(x, y, 7 * density, handle)
            canvas.drawCircle(x, y, 4.5f * density, handleFill)
        }
    }
}
