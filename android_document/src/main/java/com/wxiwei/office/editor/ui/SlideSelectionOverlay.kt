package com.wxiwei.office.editor.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.ViewTreeObserver
import com.editor.docsdk.EditStyle
import com.wxiwei.office.editor.pptx.Rect
import com.wxiwei.office.editor.slide.SlideGeometry
import com.wxiwei.office.pg.control.Presentation

/**
 * Selection frame with 8 handles drawn over the open slide. It follows scrolling and zooming by
 * recomputing the frame before every frame the window draws. Touches outside the frame pass
 * through to the slide; dragging inside moves the shape and dragging a handle resizes it. The
 * frame previews the drag and [onChange] receives the final rectangle once, on release.
 */
@SuppressLint("ViewConstructor")
class SlideSelectionOverlay(context: Context, private val presentation: () -> Presentation?) : View(context) {
    private val editStyle = EditStyle.of(context)
    /** Slide of [selection]; the frame hides while another slide is shown. */
    var slideIndex = -1

    /** Selected shape in slide EMU, or null for no selection. */
    var selection: Rect? = null
        set(value) {
            field = value
            refresh()
        }

    /** Clockwise rotation of the selected shape in degrees; rotated frames only move and rotate. */
    var shapeRotation = 0f
        set(value) {
            field = value
            invalidate()
        }
    /** Final rotation after dragging the rotate handle, in degrees. */
    var onRotate: ((Float) -> Unit)? = null

    /** False: the frame only shows the selection; it does not move, resize or rotate the shape. */
    var movable = true
        set(value) {
            field = value
            invalidate()
        }

    /** Corner handles keep the width/height ratio (pictures). */
    var keepAspect = false
    /** A tap inside the frame (no drag), in screen coordinates. */
    var onTap: ((rawX: Float, rawY: Float) -> Unit)? = null
    /** The frame on screen (this view's coordinates) whenever it moves, null when hidden. */
    var onFrame: ((RectF?) -> Unit)? = null

    /** Final rectangle of a move or resize, in slide EMU. */
    var onChange: ((Rect) -> Unit)? = null

    private val frame = RectF()
    // drag state: handle index (-1 = move), start point, frame at start
    private var dragging = false
    private var moved = false
    private var handle = -1
    private var downX = 0f
    private var downY = 0f
    private val start = RectF()
    private var angle = 0f // rotation shown while dragging the rotate handle
    private val touchSlop = android.view.ViewConfiguration.get(context).scaledTouchSlop
    private var visible = false
    private val density = context.resources.displayMetrics.density
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = editStyle.selection
        pathEffect = DashPathEffect(floatArrayOf(6 * density, 3 * density), 0f)
    }
    private val handleFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = editStyle.handleFill }
    private val handleStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = editStyle.selection
    }
    private val preDraw = ViewTreeObserver.OnPreDrawListener { refresh(); true }

    init {
        isFocusable = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(preDraw)
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDraw)
        super.onDetachedFromWindow()
    }

    /** Recomputes the frame from the slide position; invalidates only when it moved. */
    private fun refresh() {
        if (dragging) return
        val rect = selection
        val p = presentation()
        val view = if (rect != null && p != null && p.getCurrentIndex() == slideIndex) SlideGeometry.emuToView(p, rect) else null
        if (view == null) {
            if (visible) { visible = false; invalidate(); onFrame?.invoke(null) }
            return
        }
        val a = IntArray(2); val b = IntArray(2)
        p!!.getLocationOnScreen(a); getLocationOnScreen(b)
        view.offset((a[0] - b[0]).toFloat(), (a[1] - b[1]).toFloat())
        if (!visible || view != frame) {
            frame.set(view)
            visible = true
            invalidate()
            onFrame?.invoke(RectF(frame))
        }
    }

    /** The current frame (this view's coordinates), or null when hidden. */
    fun frameOnScreen(): RectF? = if (visible) RectF(frame) else null

    /** Handles in drawing order: 0 1 2 top row, 3 4 middle, 5 6 7 bottom row. */
    private fun handles(r: RectF): List<Pair<Float, Float>> {
        val xs = floatArrayOf(r.left, r.centerX(), r.right); val ys = floatArrayOf(r.top, r.centerY(), r.bottom)
        return ys.indices.flatMap { j -> xs.indices.filter { i -> !(i == 1 && j == 1) }.map { i -> xs[i] to ys[j] } }
    }

    private val rotateGap get() = 28 * density

    /** [x], [y] in the frame's unrotated coordinates. */
    private fun local(x: Float, y: Float): Pair<Float, Float> {
        if (shapeRotation == 0f) return x to y
        val a = Math.toRadians(-shapeRotation.toDouble())
        val dx = x - frame.centerX(); val dy = y - frame.centerY()
        return (frame.centerX() + dx * Math.cos(a) - dy * Math.sin(a)).toFloat() to (frame.centerY() + dx * Math.sin(a) + dy * Math.cos(a)).toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                if (!visible || event.pointerCount > 1) return false
                val (lx, ly) = local(event.x, event.y)
                // handle reach shrinks on small frames so their middle still moves the shape
                val grab = (minOf(frame.width(), frame.height()) / 3).coerceIn(6 * density, 16 * density)
                val reach = { hx: Float, hy: Float -> Math.hypot((lx - hx).toDouble(), (ly - hy).toDouble()) }
                handle = if (reach(frame.centerX(), frame.top - rotateGap) <= 16 * density) ROTATE
                    else if (shapeRotation != 0f) -1
                    else handles(frame).withIndex().map { (i, h) -> i to reach(h.first, h.second) }
                        .filter { it.second <= grab }.minByOrNull { it.second }?.first ?: -1
                if (handle < 0 && !RectF(frame).apply { inset(-grab / 2, -grab / 2) }.contains(lx, ly)) return false
                angle = shapeRotation
                dragging = true; moved = false
                downX = event.x; downY = event.y
                start.set(frame)
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                if (!dragging) return false
                if (!movable) return true
                val dx = event.x - downX; val dy = event.y - downY
                if (!moved && Math.hypot(dx.toDouble(), dy.toDouble()) < touchSlop) return true
                moved = true
                if (handle == ROTATE) {
                    // the handle points from the centre to the finger; snap to 15° steps within 4°
                    var a = Math.toDegrees(Math.atan2((event.x - frame.centerX()).toDouble(), (frame.centerY() - event.y).toDouble())).toFloat()
                    a = (a + 360f) % 360f
                    val snap = Math.round(a / 15f) * 15f
                    angle = if (Math.abs(a - snap) < 4f) snap % 360f else a
                } else frame.set(dragged(dx, dy))
                invalidate()
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                if (!dragging) return false
                dragging = false
                val rect = selection
                if (!moved) onTap?.invoke(event.rawX, event.rawY)
                else if (handle == ROTATE) onRotate?.invoke(angle)
                else if (rect != null && start.width() > 0 && start.height() > 0) {
                    // back to EMU with the frame's own scale
                    val sx = rect.width / start.width().toDouble(); val sy = rect.height / start.height().toDouble()
                    // each edge moves by its own delta, so edges not dragged keep their exact EMU
                    val left = rect.x + Math.round((frame.left - start.left) * sx)
                    val top = rect.y + Math.round((frame.top - start.top) * sy)
                    val right = rect.x + rect.width + Math.round((frame.right - start.right) * sx)
                    val bottom = rect.y + rect.height + Math.round((frame.bottom - start.bottom) * sy)
                    val result = if (handle < 0) Rect(left, top, rect.width, rect.height)
                        else Rect(left, top, maxOf(1L, right - left), maxOf(1L, bottom - top))
                    onChange?.invoke(result)
                }
                refresh()
                return true
            }
            android.view.MotionEvent.ACTION_CANCEL -> {
                dragging = false
                visible = false
                refresh()
                return true
            }
        }
        return dragging
    }

    /** Frame after dragging by (dx, dy) from [start] with the grabbed handle. */
    private fun dragged(dx: Float, dy: Float): RectF {
        val r = RectF(start)
        val min = 12 * density
        if (handle < 0) { r.offset(dx, dy); return r }
        val col = intArrayOf(0, 1, 2, 0, 2, 0, 1, 2)[handle]
        val row = intArrayOf(0, 0, 0, 1, 1, 2, 2, 2)[handle]
        if (col == 0) r.left = minOf(start.left + dx, start.right - min)
        if (col == 2) r.right = maxOf(start.right + dx, start.left + min)
        if (row == 0) r.top = minOf(start.top + dy, start.bottom - min)
        if (row == 2) r.bottom = maxOf(start.bottom + dy, start.top + min)
        if (keepAspect && col != 1 && row != 1 && start.height() > 0) {
            // follow the larger change, anchor the opposite corner
            val ratio = start.width() / start.height()
            val scale = maxOf(r.width() / start.width(), r.height() / start.height())
            val w = start.width() * scale; val h = w / ratio
            if (col == 0) r.left = r.right - w else r.right = r.left + w
            if (row == 0) r.top = r.bottom - h else r.bottom = r.top + h
        }
        return r
    }

    override fun onDraw(canvas: Canvas) {
        if (!visible) return
        val shown = if (dragging && handle == ROTATE) angle else shapeRotation
        canvas.save()
        canvas.rotate(shown, frame.centerX(), frame.centerY())
        canvas.drawRect(frame, line)
        if (!movable) return canvas.restore()
        val r = 4.5f * density
        if (shown == 0f && !(dragging && handle == ROTATE)) for ((x, y) in handles(frame)) {
            canvas.drawCircle(x, y, r, handleFill)
            canvas.drawCircle(x, y, r, handleStroke)
        }
        val ry = frame.top - rotateGap
        canvas.drawLine(frame.centerX(), frame.top, frame.centerX(), ry, handleStroke)
        canvas.drawCircle(frame.centerX(), ry, r * 1.4f, handleFill)
        canvas.drawCircle(frame.centerX(), ry, r * 1.4f, handleStroke)
        canvas.drawArc(frame.centerX() - r * 0.8f, ry - r * 0.8f, frame.centerX() + r * 0.8f, ry + r * 0.8f, -60f, 270f, false, handleStroke)
        canvas.restore()
    }

    private companion object {
        const val ROTATE = 8
    }
}
