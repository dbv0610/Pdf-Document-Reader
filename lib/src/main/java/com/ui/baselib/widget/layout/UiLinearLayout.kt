package com.ui.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.graphics.withClip
import com.ui.baselib.R

@Suppress("DEPRECATION")
open class UiLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiLinearLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiLinearLayout_cornerRadius,
                    R.styleable.UiLinearLayout_cornerTopLeft,
                    R.styleable.UiLinearLayout_cornerTopRight,
                    R.styleable.UiLinearLayout_cornerBottomLeft,
                    R.styleable.UiLinearLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiLinearLayout_bgIsGradient,
                    R.styleable.UiLinearLayout_bgGradientStart,
                    R.styleable.UiLinearLayout_bgGradientCenter,
                    R.styleable.UiLinearLayout_bgGradientEnd,
                    R.styleable.UiLinearLayout_bgColor,
                    R.styleable.UiLinearLayout_bgGdOrientation,
                    R.styleable.UiLinearLayout_bgGradientType,
                    R.styleable.UiLinearLayout_bgGradientCenterX,
                    R.styleable.UiLinearLayout_bgGradientCenterY,
                    R.styleable.UiLinearLayout_bgGradientRadius,
                    R.styleable.UiLinearLayout_bgGradientColors,
                    R.styleable.UiLinearLayout_bgColors
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiLinearLayout_strokeWidth,
                    R.styleable.UiLinearLayout_stColor,
                    R.styleable.UiLinearLayout_strokeDistance,
                    R.styleable.UiLinearLayout_distanceSpace,
                    R.styleable.UiLinearLayout_strokeGradient,
                    R.styleable.UiLinearLayout_strokeGdOrientation,
                    R.styleable.UiLinearLayout_strokeOption,
                    R.styleable.UiLinearLayout_strokeCap,
                    R.styleable.UiLinearLayout_stColors,
                    R.styleable.UiLinearLayout_strokeWidths,
                    R.styleable.UiLinearLayout_strokeGradientStart,
                    R.styleable.UiLinearLayout_strokeGradientCenter,
                    R.styleable.UiLinearLayout_strokeGradientEnd
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiLinearLayout_shadowColor,
                    R.styleable.UiLinearLayout_shadowRadius,
                    R.styleable.UiLinearLayout_shadowDx,
                    R.styleable.UiLinearLayout_shadowDy,
                    R.styleable.UiLinearLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiLinearLayout_uiDimenRatio,
                    R.styleable.UiLinearLayout_uiWidthPercent,
                    R.styleable.UiLinearLayout_uiHeightPercent,
                    R.styleable.UiLinearLayout_uiMaxWidthPercent,
                    R.styleable.UiLinearLayout_uiMaxHeightPercent,
                    R.styleable.UiLinearLayout_uiMinWidthPercent,
                    R.styleable.UiLinearLayout_uiMinHeightPercent
                )
            } finally {
                recycle()
            }
        }
        helper.setupShadow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (wSpec, hSpec) = helper.resolveMeasureSpecs(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(wSpec, hSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        helper.drawBackground(canvas, w, h)
        canvas.withClip(helper.getClipPath()) {
            onDrawUnderChildren(this)
            super.dispatchDraw(canvas)
            onDrawOverChildren(this)
        }
        helper.drawStroke(canvas, w, h)
    }

    /** Drawn after the layout background but BEHIND the child views (clipped to the corners). */
    protected open fun onDrawUnderChildren(canvas: Canvas) {}

    /** Drawn ON TOP of the child views but below the stroke (clipped to the corners). */
    protected open fun onDrawOverChildren(canvas: Canvas) {}

    // IUiLayout implementation
    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() = invalidateOutline()
    override fun updateViewClipPath() = helper.updateClipPath(width, height)

    fun applyStyle(block: UiLinearLayout.() -> Unit) = apply(block)
}
