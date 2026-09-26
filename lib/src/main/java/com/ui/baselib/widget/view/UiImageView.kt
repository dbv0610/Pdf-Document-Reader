package com.ui.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.ui.baselib.R
import com.ui.baselib.widget.layout.IUiLayout
import com.ui.baselib.widget.layout.UiLayoutHelper

@SuppressLint("CustomViewStyleable")
class UiImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), IUiLayout {

    override val helper = UiLayoutHelper(this)

    // Image-specific properties
    private var gradientIconColors: IntArray? = null
    private var gradientIconOrientation = UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT

    private val gradientIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
    private val rectF = RectF()
    private val backgroundPath = Path()

    init {
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.UiImageView).apply {
            try {
                // Common layout attrs via helper
                helper.readCornerAttrs(
                    this,
                    R.styleable.UiImageView_cornerRadius,
                    R.styleable.UiImageView_cornerTopLeft,
                    R.styleable.UiImageView_cornerTopRight,
                    R.styleable.UiImageView_cornerBottomLeft,
                    R.styleable.UiImageView_cornerBottomRight
                )
                helper.readBackgroundAttrs(
                    this,
                    R.styleable.UiImageView_bgIsGradient,
                    R.styleable.UiImageView_bgGradientStart,
                    R.styleable.UiImageView_bgGradientCenter,
                    R.styleable.UiImageView_bgGradientEnd,
                    R.styleable.UiImageView_bgColor,
                    R.styleable.UiImageView_bgGdOrientation,
                    R.styleable.UiImageView_bgGradientType,
                    R.styleable.UiImageView_bgGradientCenterX,
                    R.styleable.UiImageView_bgGradientCenterY,
                    R.styleable.UiImageView_bgGradientRadius,
                    R.styleable.UiImageView_bgGradientColors,
                    R.styleable.UiImageView_bgColors
                )
                helper.readStrokeAttrs(
                    this,
                    R.styleable.UiImageView_strokeWidth,
                    R.styleable.UiImageView_stColor,
                    R.styleable.UiImageView_strokeDistance,
                    R.styleable.UiImageView_distanceSpace,
                    R.styleable.UiImageView_strokeGradient,
                    R.styleable.UiImageView_strokeGdOrientation,
                    R.styleable.UiImageView_strokeOption,
                    -1,
                    R.styleable.UiImageView_stColors,
                    R.styleable.UiImageView_strokeWidths,
                    R.styleable.UiImageView_strokeGradientStart,
                    R.styleable.UiImageView_strokeGradientCenter,
                    R.styleable.UiImageView_strokeGradientEnd
                )
                helper.readShadowAttrs(
                    this,
                    R.styleable.UiImageView_shadowColor,
                    R.styleable.UiImageView_shadowRadius,
                    R.styleable.UiImageView_shadowDx,
                    R.styleable.UiImageView_shadowDy,
                    R.styleable.UiImageView_shadowElevation
                )
                helper.readDimensionAttrs(
                    this,
                    R.styleable.UiImageView_uiDimenRatio,
                    R.styleable.UiImageView_uiWidthPercent,
                    R.styleable.UiImageView_uiHeightPercent,
                    R.styleable.UiImageView_uiMaxWidthPercent,
                    R.styleable.UiImageView_uiMaxHeightPercent,
                    R.styleable.UiImageView_uiMinWidthPercent,
                    R.styleable.UiImageView_uiMinHeightPercent
                )

                // Image-specific attrs
                val gradientIconsStr = getString(R.styleable.UiImageView_gradientIcons)
                gradientIconColors = gradientIconsStr?.parseHexColors()
                gradientIconOrientation = getInt(R.styleable.UiImageView_imageGdOrientation, 6)
                    .toGradientOrientation()
            } finally {
                recycle()
            }
        }

        helper.setupShadow()
        applyClipStrategy()
    }

    /**
     * Canvas.clipPath is not anti-aliased when hardware-accelerated, which shows up as
     * shimmering/flickering rounded corners while the view is translated (e.g. ViewPager2
     * paging). Prefer clipToOutline (hardware-accelerated + anti-aliased) whenever the
     * corners are uniform; only fall back to the manual clipPath draw for per-corner radii,
     * which Outline.setRoundRect can't express, forcing a software layer there for AA.
     */
    private fun applyClipStrategy() {
        if (helper.hasIndividualCorners()) {
            clipToOutline = false
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        } else {
            outlineProvider = helper.roundOutlineProvider
            clipToOutline = true
            setLayerType(LAYER_TYPE_NONE, null)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (helper.shouldApplyCustomMeasure()) {
            val dm = context.resources.displayMetrics
            val specW = MeasureSpec.getSize(widthMeasureSpec)
            val specH = MeasureSpec.getSize(heightMeasureSpec)
            val parentWidth = if (specW > 0) specW else
                (parent as? android.view.View)?.width?.takeIf { it > 0 } ?: dm.widthPixels
            val parentHeight = if (specH > 0) specH else
                (parent as? android.view.View)?.height?.takeIf { it > 0 } ?: dm.heightPixels
            val result = helper.measureWithConstraints(
                widthMeasureSpec, heightMeasureSpec, parentWidth, parentHeight
            )
            val wSpec = if (result.widthCustomized)
                MeasureSpec.makeMeasureSpec(result.width, MeasureSpec.EXACTLY)
                else widthMeasureSpec
            val hSpec = if (result.heightCustomized)
                MeasureSpec.makeMeasureSpec(result.height, MeasureSpec.EXACTLY)
                else heightMeasureSpec
            super.onMeasure(wSpec, hSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val radii = helper.getCornerRadii(w, h)
        // rectF is reused below as the saveLayer bounds for the gradient-icon overlay,
        // so it must reflect the current size regardless of the clip-to-outline branch.
        rectF.set(0f, 0f, w, h)

        // Outline-based clipping (set in applyClipStrategy) already clips uniform-radius
        // corners with hardware AA; only fall back to manual clipPath for per-corner radii.
        if (!clipToOutline) {
            backgroundPath.reset()
            backgroundPath.addRoundRect(rectF, radii, Path.Direction.CW)
        }

        canvas.withSave {
            if (!clipToOutline) {
                runCatching { canvas.clipPath(backgroundPath) }
            }

            // 3) Draw background
            helper.drawBackground(canvas, w, h)

            // 4) Draw image with optional gradient overlay
            val iconColors = gradientIconColors
            if (iconColors != null && iconColors.size > 1) {
                val saveCount = canvas.saveLayer(rectF, null)
                super.onDraw(canvas)
                drawGradientOverlay(canvas, w, h, iconColors)
                canvas.restoreToCount(saveCount)
            } else {
                super.onDraw(canvas)
            }

            // 5) Draw stroke
            helper.drawStroke(canvas, w, h)
        }
    }

    private fun drawGradientOverlay(canvas: Canvas, width: Float, height: Float, colors: IntArray) {
        gradientIconPaint.shader = createIconGradientShader(width, height, colors)
        canvas.drawRect(0f, 0f, width, height, gradientIconPaint)
    }

    private fun createIconGradientShader(width: Float, height: Float, colors: IntArray): Shader {
        val (x0, y0, x1, y1) = gradientIconOrientation.toCoordinates(width, height)
        return LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
    }

    private fun String.parseHexColors(): IntArray? {
        return split(" ")
            .mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
            .takeIf { it.isNotEmpty() }
            ?.toIntArray()
    }

    private fun String.isValidHexColor(): Boolean =
        matches(HEX_COLOR_REGEX)

    private fun Int.toGradientOrientation() =
        UiLayoutHelper.GradientOrientation.entries.getOrElse(this) {
            UiLayoutHelper.GradientOrientation.LEFT_TO_RIGHT
        }

    // IUiLayout implementation
    override fun invalidateView() = invalidate()
    override fun invalidateViewOutline() {
        applyClipStrategy()
        invalidateOutline()
    }
    override fun updateViewClipPath() = helper.updateClipPath(width, height)

    // Image-specific fluent API
    fun setGradientIconColors(colors: IntArray?) = apply {
        gradientIconColors = colors
        invalidate()
    }

    fun setGradientIconColors(startColor: Int, endColor: Int) = apply {
        gradientIconColors = intArrayOf(startColor, endColor)
        invalidate()
    }

    fun setGradientIconColors(startColor: Int, centerColor: Int, endColor: Int) = apply {
        gradientIconColors = intArrayOf(startColor, centerColor, endColor)
        invalidate()
    }

    fun gradientIconOrientation(orientation: UiLayoutHelper.GradientOrientation) = apply {
        gradientIconOrientation = orientation
        invalidate()
    }

    fun clearGradientIcon() = apply {
        gradientIconColors = null
        invalidate()
    }

    fun applyStyle(block: UiImageView.() -> Unit) = apply(block)

    companion object {
        private val HEX_COLOR_REGEX = Regex("^#?[0-9a-fA-F]{6,8}$")
    }
}
