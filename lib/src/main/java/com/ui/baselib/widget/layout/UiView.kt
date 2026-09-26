package com.ui.baselib.widget.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.ui.baselib.R

class UiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiView).apply {
            try {
                helper.readCornerAttrs(this,
                    R.styleable.UiView_cornerRadius,
                    R.styleable.UiView_cornerTopLeft,
                    R.styleable.UiView_cornerTopRight,
                    R.styleable.UiView_cornerBottomLeft,
                    R.styleable.UiView_cornerBottomRight
                )
                helper.readBackgroundAttrs(this,
                    R.styleable.UiView_bgIsGradient,
                    R.styleable.UiView_bgGradientStart,
                    R.styleable.UiView_bgGradientCenter,
                    R.styleable.UiView_bgGradientEnd,
                    R.styleable.UiView_bgColor,
                    R.styleable.UiView_bgGdOrientation,
                    R.styleable.UiView_bgGradientType,
                    R.styleable.UiView_bgGradientCenterX,
                    R.styleable.UiView_bgGradientCenterY,
                    R.styleable.UiView_bgGradientRadius,
                    R.styleable.UiView_bgGradientColors,
                    R.styleable.UiView_bgColors
                )
                helper.readStrokeAttrs(this,
                    R.styleable.UiView_strokeWidth,
                    R.styleable.UiView_stColor,
                    R.styleable.UiView_strokeDistance,
                    R.styleable.UiView_distanceSpace,
                    R.styleable.UiView_strokeGradient,
                    R.styleable.UiView_strokeGdOrientation,
                    R.styleable.UiView_strokeOption,
                    R.styleable.UiView_strokeCap,
                    R.styleable.UiView_stColors,
                    R.styleable.UiView_strokeWidths,
                    R.styleable.UiView_strokeGradientStart,
                    R.styleable.UiView_strokeGradientCenter,
                    R.styleable.UiView_strokeGradientEnd
                )
                helper.readShadowAttrs(this,
                    R.styleable.UiView_shadowColor,
                    R.styleable.UiView_shadowRadius,
                    R.styleable.UiView_shadowDx,
                    R.styleable.UiView_shadowDy,
                    R.styleable.UiView_shadowElevation
                )
                helper.readDimensionAttrs(this,
                    R.styleable.UiView_uiDimenRatio,
                    R.styleable.UiView_uiWidthPercent,
                    R.styleable.UiView_uiHeightPercent,
                    R.styleable.UiView_uiMaxWidthPercent,
                    R.styleable.UiView_uiMaxHeightPercent,
                    R.styleable.UiView_uiMinWidthPercent,
                    R.styleable.UiView_uiMinHeightPercent
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        
        helper.drawBackground(canvas, w, h)
        val save = canvas.save()
        canvas.clipPath(helper.getClipPath())
        super.onDraw(canvas)
        canvas.restoreToCount(save)
        helper.drawStroke(canvas, w, h)
    }

    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() = invalidateOutline()
    override fun updateViewClipPath() = helper.updateClipPath(width, height)
}
