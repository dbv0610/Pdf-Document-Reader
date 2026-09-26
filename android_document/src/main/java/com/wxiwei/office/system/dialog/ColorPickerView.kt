package com.wxiwei.office.system.dialog

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPickerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val mPaint: Paint
    private val mCenterPaint: Paint
    private val mColors: IntArray
    private var alpha = 0xFF
    private var mTrackingCenter = false
    private var mHighlightCenter = false

    init {
        mColors = intArrayOf(0xFFFF0000.toInt(), 0xFFFF00FF.toInt(), 0xFF0000FF.toInt(), 0xFF00FFFF.toInt(), 0xFF00FF00.toInt(), 0xFFFFFF00.toInt(), 0xFFFF0000.toInt())
        val shader: Shader = SweepGradient(0f, 0f, mColors, null)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.shader = shader
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 50f
        mCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterPaint.color = ColorPickerDialog.mInitialColor
        mCenterPaint.strokeWidth = 5f
    }

    fun setAlpha(alpha: Int) {
        this.alpha = alpha
        mCenterPaint.alpha = alpha
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val r = CENTER_X - mPaint.strokeWidth * 0.5f
        canvas.translate(CENTER_X.toFloat(), CENTER_X.toFloat())
        canvas.drawOval(RectF(-r, -r, r, r), mPaint)
        canvas.drawCircle(0f, 0f, CENTER_RADIUS.toFloat(), mCenterPaint)
        if (mTrackingCenter) {
            val color = mCenterPaint.color
            mCenterPaint.style = Paint.Style.STROKE
            if (!mHighlightCenter) {
                mCenterPaint.alpha = 0x80
            }
            canvas.drawCircle(0f, 0f, CENTER_RADIUS.toFloat() + mCenterPaint.strokeWidth, mCenterPaint)
            mCenterPaint.style = Paint.Style.FILL
            mCenterPaint.color = color
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2)
    }

    private fun floatToByte(value: Float): Int = Math.round(value)

    private fun pinToByte(value: Int): Int = value.coerceIn(0, 255)

    private fun ave(source: Int, destination: Int, proportion: Float): Int = source + Math.round(proportion * (destination - source))

    private fun interpColor(colors: IntArray, unit: Float): Int {
        if (unit <= 0) return colors[0]
        if (unit >= 1) return colors[colors.size - 1]
        val scaled = unit * (colors.size - 1)
        val index = scaled.toInt()
        val proportion = scaled - index
        val source = colors[index]
        val destination = colors[index + 1]
        return Color.argb(alpha, ave(Color.red(source), Color.red(destination), proportion), ave(Color.green(source), Color.green(destination), proportion), ave(Color.blue(source), Color.blue(destination), proportion))
    }

    private fun rotateColor(color: Int, radians: Float): Int {
        val degrees = radians * 180 / 3.1415927f
        val matrix = ColorMatrix()
        val temporary = ColorMatrix()
        matrix.setRGB2YUV()
        temporary.setRotate(0, degrees)
        matrix.postConcat(temporary)
        temporary.setYUV2RGB()
        matrix.postConcat(temporary)
        val values = matrix.array
        val red = floatToByte(values[0] * Color.red(color) + values[1] * Color.green(color) + values[2] * Color.blue(color))
        val green = floatToByte(values[5] * Color.red(color) + values[6] * Color.green(color) + values[7] * Color.blue(color))
        val blue = floatToByte(values[10] * Color.red(color) + values[11] * Color.green(color) + values[12] * Color.blue(color))
        return Color.argb(Color.alpha(color), pinToByte(red), pinToByte(green), pinToByte(blue))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - CENTER_X
        val y = event.y - CENTER_Y
        val inCenter = Math.sqrt((x * x + y * y).toDouble()) <= CENTER_RADIUS
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTrackingCenter = inCenter
                if (inCenter) {
                    mHighlightCenter = true
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter
                        invalidate()
                    }
                } else {
                    var unit = Math.atan2(y.toDouble(), x.toDouble()).toFloat() / (2 * PI)
                    if (unit < 0) unit += 1
                    mCenterPaint.color = interpColor(mColors, unit)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mTrackingCenter) {
                    mTrackingCenter = false
                    invalidate()
                }
            }
        }
        return true
    }

    fun getColor(): Int = mCenterPaint.color

    companion object {
        private const val CENTER_X = 140
        private const val CENTER_Y = 140
        private const val CENTER_RADIUS = 32
        private const val PI = 3.1415926f
    }
}
