package com.ui.baselib.widget.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.withClip
import com.ui.baselib.R

@Suppress("DEPRECATION")
open class UiConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiConstraintLayout).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiConstraintLayout_cornerRadius,
                    R.styleable.UiConstraintLayout_cornerTopLeft,
                    R.styleable.UiConstraintLayout_cornerTopRight,
                    R.styleable.UiConstraintLayout_cornerBottomLeft,
                    R.styleable.UiConstraintLayout_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiConstraintLayout_bgIsGradient,
                    R.styleable.UiConstraintLayout_bgGradientStart,
                    R.styleable.UiConstraintLayout_bgGradientCenter,
                    R.styleable.UiConstraintLayout_bgGradientEnd,
                    R.styleable.UiConstraintLayout_bgColor,
                    R.styleable.UiConstraintLayout_bgGdOrientation,
                    R.styleable.UiConstraintLayout_bgGradientType,
                    R.styleable.UiConstraintLayout_bgGradientCenterX,
                    R.styleable.UiConstraintLayout_bgGradientCenterY,
                    R.styleable.UiConstraintLayout_bgGradientRadius,
                    R.styleable.UiConstraintLayout_bgGradientColors,
                    R.styleable.UiConstraintLayout_bgColors
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiConstraintLayout_strokeWidth,
                    R.styleable.UiConstraintLayout_stColor,
                    R.styleable.UiConstraintLayout_strokeDistance,
                    R.styleable.UiConstraintLayout_distanceSpace,
                    R.styleable.UiConstraintLayout_strokeGradient,
                    R.styleable.UiConstraintLayout_strokeGdOrientation,
                    R.styleable.UiConstraintLayout_strokeOption,
                    R.styleable.UiConstraintLayout_strokeCap,
                    R.styleable.UiConstraintLayout_stColors,
                    R.styleable.UiConstraintLayout_strokeWidths,
                    R.styleable.UiConstraintLayout_strokeGradientStart,
                    R.styleable.UiConstraintLayout_strokeGradientCenter,
                    R.styleable.UiConstraintLayout_strokeGradientEnd
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiConstraintLayout_shadowColor,
                    R.styleable.UiConstraintLayout_shadowRadius,
                    R.styleable.UiConstraintLayout_shadowDx,
                    R.styleable.UiConstraintLayout_shadowDy,
                    R.styleable.UiConstraintLayout_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiConstraintLayout_uiDimenRatio,
                    R.styleable.UiConstraintLayout_uiWidthPercent,
                    R.styleable.UiConstraintLayout_uiHeightPercent,
                    R.styleable.UiConstraintLayout_uiMaxWidthPercent,
                    R.styleable.UiConstraintLayout_uiMaxHeightPercent,
                    R.styleable.UiConstraintLayout_uiMinWidthPercent,
                    R.styleable.UiConstraintLayout_uiMinHeightPercent
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
            super.dispatchDraw(canvas)
        }
        helper.drawStroke(canvas, w, h)
    }

    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() = invalidateOutline()
    override fun updateViewClipPath() = helper.updateClipPath(width, height)

    fun applyStyle(block: UiConstraintLayout.() -> Unit) = apply(block)
}
