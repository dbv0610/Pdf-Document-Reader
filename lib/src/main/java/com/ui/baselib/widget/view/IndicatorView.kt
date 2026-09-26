package com.ui.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.ui.baselib.R

class IndicatorView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var indicatorCount = 5
    private var indicatorSpacing = 20f
    private var indicatorRadius = 20f
    private var indicatorWidth = 0f
    private var indicatorActive = 0
    private var indicatorScale = 1
    private var indicatorActiveWidthRatio = 2.5f
    private var indicatorRects = mutableListOf<RectF>()
    private var paintDefault = Paint(Paint.ANTI_ALIAS_FLAG)
    private var paintActive = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isGradient = false
    private var gradientStartColor = "#0061FF".toColorInt()
    private var gradientEndColor = "#60EFFF".toColorInt()
    private var activeColor = "#FF24D7".toColorInt()
    private var gradientOrientation = ORIENTATION_LEFT_RIGHT

    companion object {
        const val ORIENTATION_LEFT_RIGHT = 0
        const val ORIENTATION_TOP_BOTTOM = 1
        const val ORIENTATION_TL_BR = 2
        const val ORIENTATION_TR_BL = 3
    }

    init {
        val typedArray =
            getContext().theme.obtainStyledAttributes(attrs, R.styleable.IndicatorView, 0, 0)
        try {
            indicatorCount =
                typedArray.getInteger(R.styleable.IndicatorView_max_count, indicatorCount)
            indicatorActive =
                typedArray.getInteger(R.styleable.IndicatorView_active_count, indicatorActive)
            indicatorSpacing =
                typedArray.getDimension(R.styleable.IndicatorView_spacing, indicatorSpacing)
            indicatorRadius =
                typedArray.getDimension(R.styleable.IndicatorView_radius, indicatorRadius)
            indicatorScale =
                typedArray.getInteger(R.styleable.IndicatorView_scale_value, indicatorScale)
            paintDefault.color = typedArray.getColor(
                R.styleable.IndicatorView_default_color,
                ContextCompat.getColor(getContext(), R.color.color_indicator_default)
            )
            activeColor = typedArray.getColor(
                R.styleable.IndicatorView_active_color,
                activeColor
            )
            paintActive.color = activeColor
            isGradient = typedArray.getBoolean(R.styleable.IndicatorView_indicator_gradient, false)
            gradientStartColor = typedArray.getColor(
                R.styleable.IndicatorView_indicator_gradient_start,
                gradientStartColor
            )
            gradientEndColor = typedArray.getColor(
                R.styleable.IndicatorView_indicator_gradient_end,
                gradientEndColor
            )
            gradientOrientation = typedArray.getInt(
                R.styleable.IndicatorView_indicator_gradient_orientation,
                ORIENTATION_LEFT_RIGHT
            )
            indicatorActiveWidthRatio = typedArray.getFloat(
                R.styleable.IndicatorView_active_width_ratio,
                indicatorActiveWidthRatio
            )
        } finally {
            typedArray.recycle()
        }
        val viewTreeObserver = viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (height > 0) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this)
                        indicatorWidth = height.toFloat()
                        requestLayout()
                    }
                }
            })
        }
    }

    private fun calculateContentWidth(): Float {
        if (indicatorWidth <= 0f || indicatorCount <= 0) return 0f
        val normalDots = (indicatorCount - 1).coerceAtLeast(0)
        val activeDotWidth = indicatorWidth * indicatorActiveWidthRatio
        val normalDotsWidth = normalDots * indicatorWidth
        val totalSpacing = normalDots * indicatorSpacing
        return activeDotWidth + normalDotsWidth + totalSpacing
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightSize > 0) {
            indicatorWidth = heightSize.toFloat()
        }
        val desiredWidth = calculateContentWidth().toInt() + paddingLeft + paddingRight
        val resolvedWidth = resolveSize(desiredWidth, widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, heightSize)
        calculateIndicatorRects()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > 0 && h != oldh) {
            indicatorWidth = h.toFloat()
        }
        calculateIndicatorRects()
    }

    fun attachViewPager(viewPager: ViewPager) {
        indicatorCount = viewPager.adapter?.count ?: 0
        requestLayout()
        invalidate()

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                setIndicatorActive(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    fun attachViewPager(viewPager2: ViewPager2) {
        indicatorCount = viewPager2.adapter?.itemCount ?: 0
        requestLayout()
        invalidate()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setIndicatorActive(position)
            }
        })
    }

    private fun calculateIndicatorRects() {
        if (indicatorWidth <= 0f || indicatorCount <= 0) return

        indicatorRects.clear()
        val activeIndex = indicatorActive * indicatorScale
        val top = 0f
        val bottom = indicatorWidth
        var cursor = 0f

        for (i in 0 until indicatorCount) {
            val dotWidth = if (i == activeIndex) {
                indicatorWidth * indicatorActiveWidthRatio
            } else {
                indicatorWidth
            }
            indicatorRects.add(RectF(cursor, top, cursor + dotWidth, bottom))
            cursor += dotWidth + indicatorSpacing
        }

        if (resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
            val totalWidth = cursor - indicatorSpacing
            val mirrored = indicatorRects.map { r ->
                val w = r.right - r.left
                val newLeft = totalWidth - r.right
                RectF(newLeft, r.top, newLeft + w, r.bottom)
            }
            indicatorRects.clear()
            indicatorRects.addAll(mirrored.reversed())
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (indicatorCount > 0 && indicatorRects.size == indicatorCount) {
            val activeIndex = indicatorActive * indicatorScale
            val cornerRadius = indicatorWidth / 2

            for (i in 0 until indicatorCount) {
                val rect = indicatorRects[i]
                val isActive = i == activeIndex
                val paint = if (isActive) {
                    if (isGradient) {
                        val centerX = rect.centerX()
                        val centerY = rect.centerY()
                        val radius = indicatorWidth / 2
                        val (x0, y0, x1, y1) = getGradientCoordinates(centerX, centerY, radius)
                        paintActive.apply {
                            shader = LinearGradient(
                                x0, y0, x1, y1,
                                gradientStartColor,
                                gradientEndColor,
                                Shader.TileMode.CLAMP
                            )
                        }
                    } else {
                        paintActive.apply {
                            shader = null
                            color = activeColor
                        }
                    }
                } else {
                    paintDefault
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }
        }
    }

    fun setIndicatorMaxCount(count: Int) {
        this.indicatorCount = count
        requestLayout()
        invalidate()
    }

    fun setIndicatorActive(number: Int) {
        this.indicatorActive = number
        calculateIndicatorRects()
        invalidate()
    }

    fun setIndicatorDefaultColor(color: Int) {
        this.paintDefault.color = color
        invalidate()
    }

    fun setIndicatorActiveColor(color: Int) {
        this.activeColor = color
        this.paintActive.color = color
        invalidate()
    }

    fun setIndicatorRadius(radius: Float) {
        this.indicatorRadius = radius
        invalidate()
    }

    fun setIndicatorScale(scale: Int) {
        this.indicatorScale = scale
        calculateIndicatorRects()
        invalidate()
    }

    fun setIndicatorSpacing(spacing: Float) {
        this.indicatorSpacing = spacing
        requestLayout()
        invalidate()
    }

    fun setGradient(enabled: Boolean) {
        this.isGradient = enabled
        invalidate()
    }

    fun setGradientColors(startColor: Int, endColor: Int) {
        this.gradientStartColor = startColor
        this.gradientEndColor = endColor
        invalidate()
    }

    fun setGradientOrientation(orientation: Int) {
        this.gradientOrientation = orientation
        invalidate()
    }

    fun setActiveWidthRatio(ratio: Float) {
        this.indicatorActiveWidthRatio = ratio
        requestLayout()
        invalidate()
    }

    private fun getGradientCoordinates(centerX: Float, centerY: Float, radius: Float): FloatArray {
        return when (gradientOrientation) {
            ORIENTATION_LEFT_RIGHT -> floatArrayOf(
                centerX - radius, centerY,
                centerX + radius, centerY
            )
            ORIENTATION_TOP_BOTTOM -> floatArrayOf(
                centerX, centerY - radius,
                centerX, centerY + radius
            )
            ORIENTATION_TL_BR -> floatArrayOf(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius
            )
            ORIENTATION_TR_BL -> floatArrayOf(
                centerX + radius, centerY - radius,
                centerX - radius, centerY + radius
            )
            else -> floatArrayOf(
                centerX - radius, centerY,
                centerX + radius, centerY
            )
        }
    }
}
