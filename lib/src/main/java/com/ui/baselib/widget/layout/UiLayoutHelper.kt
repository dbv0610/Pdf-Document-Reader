package com.ui.baselib.widget.layout

import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import com.ui.baselib.utils.isValidHexColor
import kotlin.math.min

/**
 * Helper class for UI layout drawing operations.
 * Centralizes common drawing logic for all Ui*Layout classes.
 */
class UiLayoutHelper(private val view: View) {
    companion object {
        // Stroke options
        const val STROKE_NONE = 0
        const val STROKE_TOP = 1
        const val STROKE_LEFT = 2
        const val STROKE_BOTTOM = 4
        const val STROKE_RIGHT = 8
        const val STROKE_HORIZONTAL = 5      // top + bottom
        const val STROKE_VERTICAL = 10       // left + right
        const val STROKE_TOP_LEFT = 3        // top + left
        const val STROKE_TOP_RIGHT = 9       // top + right
        const val STROKE_BOTTOM_LEFT = 6     // bottom + left
        const val STROKE_BOTTOM_RIGHT = 12   // bottom + right
        const val STROKE_ALL = 15

        private val COLOR_SPLIT_REGEX = Regex("\\s+")
    }

    // Corner radius
    var cornerRadius = 0f
    var cornerTopLeft = 0f
    var cornerTopRight = 0f
    var cornerBottomLeft = 0f
    var cornerBottomRight = 0f

    // Background
    var isGradient = false
    var bgGradientOrientation = GradientOrientation.TOP_TO_BOTTOM
    var bgGradientType = GradientType.LINEAR
    var bgGradientCenterX = 0.5f  // 0.0 to 1.0, relative to view width
    var bgGradientCenterY = 0.5f  // 0.0 to 1.0, relative to view height
    var bgGradientRadius = 0f    // 0 means auto-calculate based on view size
    var bgColor = Color.TRANSPARENT
    var bgColors: IntArray? = null

    // Stroke
    var stWidth = 0f
    var stTopWidth = -1f
    var stLeftWidth = -1f
    var stBottomWidth = -1f
    var stRightWidth = -1f
    var stColor = Color.TRANSPARENT
    var stColors: IntArray? = null
    var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    var isDashed = false
    var dashSpace = 10f
    var strokeOption = STROKE_ALL
    var strokeCap = Paint.Cap.ROUND

    // Shadow
    var compatElevationDp = 0f
    var compatShadowColor = Color.argb(90, 0, 0, 0)
    var shadowRadiusPx = dp(12f)
    var shadowDxPx = 0f
    var shadowDyPx = dp(4f)

    // Dimension ratio and percentage sizing
    var dimenRatio: String? = null  // Format: "W,16:9" or "H,16:9" or "16:9" (auto side)
    var dimenRatioSide = DimenRatioSide.AUTO  // Which side is calculated from ratio
    var dimenRatioValue = 0f  // The actual ratio value (width/height)
    var widthPercent = -1f  // 0-100, -1 means not set
    var heightPercent = -1f  // 0-100, -1 means not set
    var maxWidthPercent = -1f
    var maxHeightPercent = -1f
    var minWidthPercent = -1f
    var minHeightPercent = -1f

    enum class DimenRatioSide {
        WIDTH,   // Width is calculated from height * ratio
        HEIGHT,  // Height is calculated from width / ratio
        AUTO     // Like Compose aspectRatio: the 0dp/wrap_content side is derived from the fixed one
    }

    // Internal
    private val strokeRectF = RectF()
    private val clipPath = Path()
    private val tmpRectF = RectF()
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bgPath = Path()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val strokePath = Path()

    /**
     * Caches a corner-radii FloatArray keyed by the geometry that produced it, so repeated
     * onDraw calls with unchanged size/corners reuse the same array instead of allocating a
     * fresh one every frame.
     */
    private class RadiiCache {
        var array: FloatArray? = null
        private var w = Float.NaN
        private var h = Float.NaN
        private var inset = Float.NaN
        private var tl = 0f
        private var tr = 0f
        private var bl = 0f
        private var br = 0f
        private var uniform = 0f

        fun matches(w: Float, h: Float, inset: Float, tl: Float, tr: Float, bl: Float, br: Float, uniform: Float) =
            array != null && this.w == w && this.h == h && this.inset == inset &&
                this.tl == tl && this.tr == tr && this.bl == bl && this.br == br && this.uniform == uniform

        fun store(array: FloatArray, w: Float, h: Float, inset: Float, tl: Float, tr: Float, bl: Float, br: Float, uniform: Float) {
            this.array = array
            this.w = w; this.h = h; this.inset = inset
            this.tl = tl; this.tr = tr; this.bl = bl; this.br = br; this.uniform = uniform
        }
    }

    private val cornerRadiiCache = RadiiCache()
    private val strokeRadiiCache = RadiiCache()

    // Background gradient shader cache - a Shader is otherwise reallocated on every onDraw.
    private var bgShaderCache: Shader? = null
    private var bgShaderCacheW = Float.NaN
    private var bgShaderCacheH = Float.NaN
    private var bgShaderCacheType: GradientType? = null
    private var bgShaderCacheOrientation: GradientOrientation? = null
    private var bgShaderCacheCenterX = Float.NaN
    private var bgShaderCacheCenterY = Float.NaN
    private var bgShaderCacheRadius = Float.NaN
    private var bgShaderCacheColors: IntArray? = null

    // Stroke gradient shader cache (stroke only supports LINEAR gradients).
    private var strokeShaderCache: Shader? = null
    private var strokeShaderCacheW = Float.NaN
    private var strokeShaderCacheH = Float.NaN
    private var strokeShaderCacheOrientation: GradientOrientation? = null
    private var strokeShaderCacheColors: IntArray? = null

    // Background path cache - rebuilding a round-rect Path every frame is wasted work when
    // geometry hasn't changed since the last draw.
    private var bgPathCacheW = Float.NaN
    private var bgPathCacheH = Float.NaN
    private var bgPathCacheRadii: FloatArray? = null

    // Stroke path cache for the common case only: a single uniform-width border on all 4 sides
    // (the STROKE_ALL branch below). strokePath is a shared mutable field also rebuilt from
    // scratch by drawVariableStroke()/drawPartialStroke() for the other stroke shapes, so those
    // functions null out strokePathCacheRadii to force a rebuild next time this fast path runs -
    // otherwise this cache could return a stale Path left over from a different stroke shape.
    private var strokePathCacheW = Float.NaN
    private var strokePathCacheH = Float.NaN
    private var strokePathCacheInset = Float.NaN
    private var strokePathCacheRadii: FloatArray? = null
    val roundOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }

    /**
     * Updates the background mode and schedules a new display-list recording.
     *
     * The same Paint instance is reused between frames, so changing modes also clears
     * incompatible paint state and invalidates the display list.
     */
    fun setBackgroundGradientEnabled(enabled: Boolean) {
        isGradient = enabled
        if (!enabled) {
            bgPaint.shader = null
        }
        view.invalidate()
    }

    fun dp(value: Float): Float = value * view.resources.displayMetrics.density

    // Read common attributes from TypedArray
    fun readCornerAttrs(
          ta: TypedArray,
          cornerRadiusAttr: Int,
          topLeftAttr: Int,
          topRightAttr: Int,
          bottomLeftAttr: Int,
          bottomRightAttr: Int
    ) {
        cornerRadius = ta.getDimension(cornerRadiusAttr, 0f)
        cornerTopLeft = ta.getDimension(topLeftAttr, 0f)
        cornerTopRight = ta.getDimension(topRightAttr, 0f)
        cornerBottomLeft = ta.getDimension(bottomLeftAttr, 0f)
        cornerBottomRight = ta.getDimension(bottomRightAttr, 0f)
    }

    fun readBackgroundAttrs(
          ta: TypedArray,
          isGradientAttr: Int,
          gradientStartAttr: Int,
          gradientCenterAttr: Int,
          gradientEndAttr: Int,
          colorAttr: Int,
          orientationAttr: Int,
          gradientTypeAttr: Int = -1,
          gradientCenterXAttr: Int = -1,
          gradientCenterYAttr: Int = -1,
          gradientRadiusAttr: Int = -1,
          gradientColorsAttr: Int = -1,
          bgColorsAttr: Int = -1
    ) {
        val isGradientExplicit = ta.hasValue(isGradientAttr)
        isGradient = ta.getBoolean(isGradientAttr, false)
        bgColor = ta.getColor(colorAttr, Color.TRANSPARENT)
        bgGradientOrientation = ta.getInt(orientationAttr, 0).toGradientOrientation()

        if (gradientTypeAttr != -1) {
            bgGradientType = ta.getInt(gradientTypeAttr, 0).toGradientType()
        }
        if (gradientCenterXAttr != -1) {
            bgGradientCenterX = ta.getFloat(gradientCenterXAttr, 0.5f)
        }
        if (gradientCenterYAttr != -1) {
            bgGradientCenterY = ta.getFloat(gradientCenterYAttr, 0.5f)
        }
        if (gradientRadiusAttr != -1) {
            bgGradientRadius = ta.getDimension(gradientRadiusAttr, 0f)
        }
        val gradientStart = ta.getColor(gradientStartAttr, Color.TRANSPARENT)
        val gradientCenter = ta.getColor(gradientCenterAttr, Color.TRANSPARENT)
        val gradientEnd = ta.getColor(gradientEndAttr, Color.TRANSPARENT)
        if (gradientStart != Color.TRANSPARENT || gradientEnd != Color.TRANSPARENT) {
            bgColors = if (gradientCenter != Color.TRANSPARENT) {
                intArrayOf(gradientStart, gradientCenter, gradientEnd)
            } else {
                intArrayOf(gradientStart, gradientEnd)
            }
            if (!isGradientExplicit) isGradient = true
        }
        // Priority 2: Read from bgGradientColors string (e.g., "#FF0000 #00FF00 #0000FF")
        if (gradientColorsAttr != -1) {
            ta.getString(gradientColorsAttr)?.trim()?.split(COLOR_SPLIT_REGEX)
                ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
                ?.toIntArray()
                ?.takeIf { it.size >= 2 }
                ?.let {
                    bgColors = it
                    if (!isGradientExplicit) isGradient = true
                }
        }
        // Priority 3 (highest): Read from bgColors integer-array reference
        if (bgColorsAttr != -1) {
            val resId = ta.getResourceId(bgColorsAttr, 0)
            if (resId != 0) {
                view.resources.getIntArray(resId)
                    .takeIf { it.size >= 2 }
                    ?.let {
                        bgColors = it
                        if (!isGradientExplicit) isGradient = true
                    }
            }
        }
    }

    fun readStrokeAttrs(
          ta: TypedArray,
          widthAttr: Int,
          colorAttr: Int,
          dashedAttr: Int,
          dashSpaceAttr: Int,
          gradientAttr: Int,
          orientationAttr: Int,
          optionAttr: Int,
          capAttr: Int = -1,
          stColorsAttr: Int = -1,
          widthsAttr: Int = -1,
          gradientStartAttr: Int = -1,
          gradientCenterAttr: Int = -1,
          gradientEndAttr: Int = -1
    ) {
        stWidth = ta.getDimension(widthAttr, 0f)
        if (widthsAttr != -1) {
            ta.getString(widthsAttr)?.let { applyStrokeWidthsString(it) }
        }
        stColor = ta.getColor(colorAttr, Color.TRANSPARENT)
        isDashed = ta.getBoolean(dashedAttr, false)
        dashSpace = ta.getDimension(dashSpaceAttr, 10f)
        strokeGradientOrientation = ta.getInt(orientationAttr, 6).toStrokeOrientation()
        strokeOption = ta.getInt(optionAttr, STROKE_ALL)
        if (capAttr != -1) {
            strokeCap = ta.getInt(capAttr, 1).toStrokeCap()
        }

        // Priority 1: Read from strokeGradientStart/Center/End color attrs
        if (gradientStartAttr != -1 && gradientEndAttr != -1) {
            val gradientStart = ta.getColor(gradientStartAttr, Color.TRANSPARENT)
            val gradientCenter = if (gradientCenterAttr != -1) ta.getColor(gradientCenterAttr, Color.TRANSPARENT) else Color.TRANSPARENT
            val gradientEnd = ta.getColor(gradientEndAttr, Color.TRANSPARENT)
            if (gradientStart != Color.TRANSPARENT || gradientEnd != Color.TRANSPARENT) {
                stColors = if (gradientCenter != Color.TRANSPARENT) {
                    intArrayOf(gradientStart, gradientCenter, gradientEnd)
                } else {
                    intArrayOf(gradientStart, gradientEnd)
                }
            }
        }
        // Priority 2: Read from strokeGradient string (e.g., "#FF0000 #00FF00 #0000FF")
        ta.getString(gradientAttr)?.trim()?.split(COLOR_SPLIT_REGEX)
            ?.mapNotNull { if (it.isValidHexColor()) it.toColorInt() else null }
            ?.toIntArray()
            ?.takeIf { it.size >= 2 }
            ?.let { stColors = it }
        // Priority 3 (highest): Read from stColors integer-array reference
        if (stColorsAttr != -1) {
            val resId = ta.getResourceId(stColorsAttr, 0)
            if (resId != 0) {
                stColors = view.resources.getIntArray(resId)
            }
        }
    }

    private fun Int.toStrokeCap(): Paint.Cap = when (this) {
        0 -> Paint.Cap.BUTT
        1 -> Paint.Cap.ROUND
        2 -> Paint.Cap.SQUARE
        else -> Paint.Cap.ROUND
    }

    /**
     * Applies a compact "top left bottom right" per-side stroke width string (matching the
     * order of the [strokeWidths] fluent setter). One token sets all four sides uniformly.
     * Each token is a raw dimension: "2dp"/"2dip" (density-scaled), "2sp" (font-scaled),
     * "2px" (as-is), or a bare number (treated as dp) — e.g. `app:strokeWidths="2dp 0 0 4dp"`.
     * Unlike `@dimen/_Nsdp` attrs, these are plain literals with no resource-reference support.
     */
    private fun applyStrokeWidthsString(raw: String) {
        val tokens = raw.trim().split(COLOR_SPLIT_REGEX).filter { it.isNotEmpty() }
        val dm = view.resources.displayMetrics
        val values = tokens.map { parseDimensionToken(it, dm.density, dm.scaledDensity) }
        when (values.size) {
            1 -> {
                stTopWidth = values[0]; stLeftWidth = values[0]
                stBottomWidth = values[0]; stRightWidth = values[0]
            }
            4 -> {
                stTopWidth = values[0]; stLeftWidth = values[1]
                stBottomWidth = values[2]; stRightWidth = values[3]
            }
        }
    }

    private fun parseDimensionToken(token: String, density: Float, scaledDensity: Float): Float {
        val t = token.trim()
        return when {
            t.endsWith("dip", true) -> t.dropLast(3).trim().toFloatOrNull()?.times(density)
            t.endsWith("dp", true) -> t.dropLast(2).trim().toFloatOrNull()?.times(density)
            t.endsWith("sp", true) -> t.dropLast(2).trim().toFloatOrNull()?.times(scaledDensity)
            t.endsWith("px", true) -> t.dropLast(2).trim().toFloatOrNull()
            else -> t.toFloatOrNull()?.times(density)
        } ?: -1f
    }

    fun readShadowAttrs(
          ta: TypedArray,
          colorAttr: Int,
          radiusAttr: Int,
          dxAttr: Int,
          dyAttr: Int,
          elevationAttr: Int
    ) {
        compatShadowColor = ta.getColor(colorAttr, compatShadowColor)
        shadowRadiusPx = ta.getDimension(radiusAttr, shadowRadiusPx)
        shadowDxPx = ta.getDimension(dxAttr, shadowDxPx)
        shadowDyPx = ta.getDimension(dyAttr, shadowDyPx)
        val elevPx = ta.getDimension(elevationAttr, dp(compatElevationDp))
        compatElevationDp = elevPx / view.resources.displayMetrics.density
    }

    fun readDimensionAttrs(
          ta: TypedArray,
          dimenRatioAttr: Int = -1,
          widthPercentAttr: Int = -1,
          heightPercentAttr: Int = -1,
          maxWidthPercentAttr: Int = -1,
          maxHeightPercentAttr: Int = -1,
          minWidthPercentAttr: Int = -1,
          minHeightPercentAttr: Int = -1
    ) {
        if (dimenRatioAttr != -1) {
            ta.getString(dimenRatioAttr)?.let { parseDimenRatio(it) }
        }
        if (widthPercentAttr != -1) {
            val fraction = ta.getFraction(widthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) widthPercent = fraction * 100f
        }
        if (heightPercentAttr != -1) {
            val fraction = ta.getFraction(heightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) heightPercent = fraction * 100f
        }
        if (maxWidthPercentAttr != -1) {
            val fraction = ta.getFraction(maxWidthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) maxWidthPercent = fraction * 100f
        }
        if (maxHeightPercentAttr != -1) {
            val fraction = ta.getFraction(maxHeightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) maxHeightPercent = fraction * 100f
        }
        if (minWidthPercentAttr != -1) {
            val fraction = ta.getFraction(minWidthPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) minWidthPercent = fraction * 100f
        }
        if (minHeightPercentAttr != -1) {
            val fraction = ta.getFraction(minHeightPercentAttr, 1, 1, -1f)
            if (fraction >= 0f) minHeightPercent = fraction * 100f
        }
    }

    fun parseDimenRatio(ratio: String) {
        dimenRatio = ratio
        val trimmed = ratio.trim()
        if (trimmed.isEmpty()) {
            dimenRatioValue = 0f
            return
        }

        var ratioStr = trimmed
        // Check for side prefix: "W,16:9" or "H,16:9"
        when {
            trimmed.startsWith("W,", ignoreCase = true) -> {
                dimenRatioSide = DimenRatioSide.WIDTH
                ratioStr = trimmed.substring(2)
            }

            trimmed.startsWith("H,", ignoreCase = true) -> {
                dimenRatioSide = DimenRatioSide.HEIGHT
                ratioStr = trimmed.substring(2)
            }

            else -> {
                dimenRatioSide = DimenRatioSide.AUTO
            }
        }

        // Parse ratio value: "16:9" or "1.78"
        dimenRatioValue = if (ratioStr.contains(":")) {
            val parts = ratioStr.split(":")
            if (parts.size == 2) {
                val w = parts[0].trim().toFloatOrNull() ?: 0f
                val h = parts[1].trim().toFloatOrNull() ?: 0f
                if (h > 0f) w / h else 0f
            } else 0f
        } else {
            ratioStr.toFloatOrNull() ?: 0f
        }
    }

    data class MeasureResult(
        val width: Int,
        val height: Int,
        val widthCustomized: Boolean,
        val heightCustomized: Boolean,
    )

    fun measureWithConstraints(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        parentWidth: Int,
        parentHeight: Int
    ): MeasureResult {
        val specW = View.MeasureSpec.getSize(widthMeasureSpec)
        val specH = View.MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        var measuredWidth = specW
        var measuredHeight = specH
        var widthCustomized = false
        var heightCustomized = false

        // Apply percentage width — skip if parent forced EXACTLY (layout_width="Xdp")
        if (widthPercent >= 0f && parentWidth > 0 && widthMode != View.MeasureSpec.EXACTLY) {
            val computed = (parentWidth * widthPercent / 100f).toInt().coerceAtLeast(0)
            measuredWidth = if (widthMode == View.MeasureSpec.AT_MOST && specW > 0)
                minOf(computed, specW) else computed
            widthCustomized = true
        }

        // Apply percentage height — skip if parent forced EXACTLY
        if (heightPercent >= 0f && parentHeight > 0 && heightMode != View.MeasureSpec.EXACTLY) {
            val computed = (parentHeight * heightPercent / 100f).toInt().coerceAtLeast(0)
            measuredHeight = if (heightMode == View.MeasureSpec.AT_MOST && specH > 0)
                minOf(computed, specH) else computed
            heightCustomized = true
        }

        // Apply min/max percentage constraints
        if (minWidthPercent >= 0f && parentWidth > 0) {
            val minW = (parentWidth * minWidthPercent / 100f).toInt().coerceAtLeast(0)
            if (measuredWidth < minW) {
                measuredWidth = minW; widthCustomized = true
            }
        }
        if (maxWidthPercent >= 0f && parentWidth > 0) {
            val maxW = (parentWidth * maxWidthPercent / 100f).toInt().coerceAtLeast(0)
            if (measuredWidth > maxW) {
                measuredWidth = maxW; widthCustomized = true
            }
        }
        if (minHeightPercent >= 0f && parentHeight > 0) {
            val minH = (parentHeight * minHeightPercent / 100f).toInt().coerceAtLeast(0)
            if (measuredHeight < minH) {
                measuredHeight = minH; heightCustomized = true
            }
        }
        if (maxHeightPercent >= 0f && parentHeight > 0) {
            val maxH = (parentHeight * maxHeightPercent / 100f).toInt().coerceAtLeast(0)
            if (measuredHeight > maxH) {
                measuredHeight = maxH; heightCustomized = true
            }
        }

        // Apply dimension ratio — use parentWidth/Height as base when spec is 0
        if (dimenRatioValue > 0f) {
            when (resolveRatioSide(widthMode, specW, heightMode, specH)) {
                DimenRatioSide.HEIGHT, DimenRatioSide.AUTO -> {
                    val baseW = if (measuredWidth > 0) measuredWidth else parentWidth
                    if (baseW > 0) {
                        val computed = (baseW / dimenRatioValue).toInt().coerceAtLeast(0)
                        measuredHeight = if (heightMode == View.MeasureSpec.AT_MOST && specH > 0)
                            minOf(computed, specH) else computed
                        heightCustomized = true
                    }
                }

                DimenRatioSide.WIDTH -> {
                    val baseH = if (measuredHeight > 0) measuredHeight else parentHeight
                    if (baseH > 0) {
                        val computed = (baseH * dimenRatioValue).toInt().coerceAtLeast(0)
                        measuredWidth = if (widthMode == View.MeasureSpec.AT_MOST && specW > 0)
                            minOf(computed, specW) else computed
                        widthCustomized = true
                    }
                }
            }
        }

        return MeasureResult(
            width = measuredWidth.coerceAtLeast(0),
            height = measuredHeight.coerceAtLeast(0),
            widthCustomized = widthCustomized,
            heightCustomized = heightCustomized,
        )
    }

    /**
     * AUTO: the side declared as 0dp / wrap_content is computed from the fixed side
     * (match_parent or exact dp). Falls back to whichever spec has a real size, then HEIGHT.
     */
    private fun resolveRatioSide(widthMode: Int, specW: Int, heightMode: Int, specH: Int): DimenRatioSide {
        if (dimenRatioSide != DimenRatioSide.AUTO) return dimenRatioSide
        val lp = view.layoutParams
        val widthFree = lp != null && (lp.width == 0 || lp.width == ViewGroup.LayoutParams.WRAP_CONTENT)
        val heightFree = lp != null && (lp.height == 0 || lp.height == ViewGroup.LayoutParams.WRAP_CONTENT)
        return when {
            widthFree && !heightFree -> DimenRatioSide.WIDTH
            heightFree && !widthFree -> DimenRatioSide.HEIGHT
            specW > 0 && widthMode != View.MeasureSpec.UNSPECIFIED -> DimenRatioSide.HEIGHT
            specH > 0 && heightMode != View.MeasureSpec.UNSPECIFIED -> DimenRatioSide.WIDTH
            else -> DimenRatioSide.HEIGHT
        }
    }

    /**
     * Resolves width/height MeasureSpecs against the percent/ratio constraints configured on
     * this helper. Returns the original specs unchanged when no custom measurement is set.
     * Shared by every Ui*Layout's onMeasure() to avoid duplicating this logic per class.
     */
    fun resolveMeasureSpecs(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        if (!shouldApplyCustomMeasure()) return widthMeasureSpec to heightMeasureSpec

        val dm = view.resources.displayMetrics
        val specW = View.MeasureSpec.getSize(widthMeasureSpec)
        val specH = View.MeasureSpec.getSize(heightMeasureSpec)
        val parentWidth = if (specW > 0) specW else
            (view.parent as? View)?.width?.takeIf { it > 0 } ?: dm.widthPixels
        val parentHeight = if (specH > 0) specH else
            (view.parent as? View)?.height?.takeIf { it > 0 } ?: dm.heightPixels

        val result = measureWithConstraints(widthMeasureSpec, heightMeasureSpec, parentWidth, parentHeight)
        val wSpec = if (result.widthCustomized)
            View.MeasureSpec.makeMeasureSpec(result.width, View.MeasureSpec.EXACTLY)
            else widthMeasureSpec
        val hSpec = if (result.heightCustomized)
            View.MeasureSpec.makeMeasureSpec(result.height, View.MeasureSpec.EXACTLY)
            else heightMeasureSpec
        return wSpec to hSpec
    }

    fun shouldApplyCustomMeasure(): Boolean {
        return dimenRatioValue > 0f ||
                widthPercent >= 0f ||
                heightPercent >= 0f ||
                maxWidthPercent >= 0f ||
                maxHeightPercent >= 0f ||
                minWidthPercent >= 0f ||
                minHeightPercent >= 0f
    }

    // minSdk is 24 (lib) / 26 (app), both >= LOLLIPOP (21), so elevation-based
    // shadows are always available — no pre-Lollipop fallback needed.
    fun setupShadow() {
        view.outlineProvider = roundOutlineProvider
        view.clipToOutline = false
        ViewCompat.setElevation(view, dp(compatElevationDp))
        applyPlatformShadowColor()
    }

    fun applyPlatformShadowColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.outlineSpotShadowColor = compatShadowColor
            view.outlineAmbientShadowColor = compatShadowColor
        }
    }

    fun onSizeChanged(w: Int, h: Int) {
        updateClipPath(w, h)
        view.invalidateOutline()
    }

    fun updateClipPath(w: Int, h: Int) {
        clipPath.reset()
        if (w <= 0 || h <= 0) return
        tmpRectF.set(0f, 0f, w.toFloat(), h.toFloat())
        val radii = getCornerRadii(w.toFloat(), h.toFloat())
        clipPath.addRoundRect(tmpRectF, radii, Path.Direction.CW)
        clipPath.close()
    }

    fun getClipPath(): Path = clipPath

    fun hasIndividualCorners(): Boolean =
        cornerTopLeft > 0f || cornerTopRight > 0f || cornerBottomLeft > 0f || cornerBottomRight > 0f

    fun getCornerRadii(w: Float, h: Float): FloatArray {
        cornerRadiiCache.array?.let {
            if (cornerRadiiCache.matches(w, h, 0f, cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight, cornerRadius)) {
                return it
            }
        }
        val maxRadius = min(w / 2f, h / 2f)
        val radii = if (hasIndividualCorners()) {
            floatArrayOf(
                min(cornerTopLeft, maxRadius), min(cornerTopLeft, maxRadius),
                min(cornerTopRight, maxRadius), min(cornerTopRight, maxRadius),
                min(cornerBottomRight, maxRadius), min(cornerBottomRight, maxRadius),
                min(cornerBottomLeft, maxRadius), min(cornerBottomLeft, maxRadius)
            )
        } else {
            val r = min(cornerRadius, maxRadius)
            floatArrayOf(r, r, r, r, r, r, r, r)
        }
        cornerRadiiCache.store(radii, w, h, 0f, cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight, cornerRadius)
        return radii
    }

    fun getStrokeCornerRadii(w: Float, h: Float, inset: Float): FloatArray {
        strokeRadiiCache.array?.let {
            if (strokeRadiiCache.matches(w, h, inset, cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight, cornerRadius)) {
                return it
            }
        }
        val maxRadius = min(w / 2f, h / 2f)
        val radii = if (hasIndividualCorners()) {
            floatArrayOf(
                (min(cornerTopLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerTopRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomRight, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomLeft, maxRadius) - inset).coerceAtLeast(0f),
                (min(cornerBottomLeft, maxRadius) - inset).coerceAtLeast(0f)
            )
        } else {
            val r = (min(cornerRadius, maxRadius) - inset).coerceAtLeast(0f)
            floatArrayOf(r, r, r, r, r, r, r, r)
        }
        strokeRadiiCache.store(radii, w, h, inset, cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight, cornerRadius)
        return radii
    }

    fun drawBackground(canvas: Canvas, w: Float, h: Float) {
        val radii = getCornerRadii(w, h)
        val gradientColors = bgColors
        if (isGradient && gradientColors != null && gradientColors.size >= 2) {
            // Paint.color also changes Paint.alpha. A translucent solid background used by the
            // previous frame would otherwise make the restored gradient translucent as well.
            bgPaint.alpha = 255
            bgPaint.shader = obtainBgShader(w, h, gradientColors)
        } else {
            bgPaint.shader = null
            bgPaint.color = bgColor
        }
        if (bgPathCacheRadii !== radii || bgPathCacheW != w || bgPathCacheH != h) {
            bgPath.reset()
            bgPath.addRoundRect(tmpRectF.apply { set(0f, 0f, w, h) }, radii, Path.Direction.CW)
            bgPathCacheRadii = radii; bgPathCacheW = w; bgPathCacheH = h
        }
        canvas.drawPath(bgPath, bgPaint)
    }

    /** Reuses the previously built gradient Shader when size/colors/orientation are unchanged. */
    private fun obtainBgShader(w: Float, h: Float, colors: IntArray): Shader {
        bgShaderCache?.let {
            if (bgShaderCacheW == w && bgShaderCacheH == h &&
                bgShaderCacheType == bgGradientType && bgShaderCacheOrientation == bgGradientOrientation &&
                bgShaderCacheCenterX == bgGradientCenterX && bgShaderCacheCenterY == bgGradientCenterY &&
                bgShaderCacheRadius == bgGradientRadius &&
                bgShaderCacheColors?.contentEquals(colors) == true
            ) {
                return it
            }
        }
        val shader = when (bgGradientType) {
            GradientType.LINEAR -> {
                val (x0, y0, x1, y1) = bgGradientOrientation.toCoordinates(w, h)
                LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
            }
            GradientType.RADIAL -> {
                val centerX = w * bgGradientCenterX
                val centerY = h * bgGradientCenterY
                val radius = if (bgGradientRadius > 0f) bgGradientRadius else {
                    kotlin.math.max(w, h) / 2f
                }
                RadialGradient(centerX, centerY, radius, colors, null, Shader.TileMode.CLAMP)
            }
            GradientType.SWEEP -> {
                val centerX = w * bgGradientCenterX
                val centerY = h * bgGradientCenterY
                SweepGradient(centerX, centerY, colors, null)
            }
        }
        bgShaderCache = shader
        bgShaderCacheW = w; bgShaderCacheH = h
        bgShaderCacheType = bgGradientType; bgShaderCacheOrientation = bgGradientOrientation
        bgShaderCacheCenterX = bgGradientCenterX; bgShaderCacheCenterY = bgGradientCenterY
        bgShaderCacheRadius = bgGradientRadius; bgShaderCacheColors = colors.copyOf()
        return shader
    }

    fun drawStroke(canvas: Canvas, w: Float, h: Float) {
        if (strokeOption == STROKE_NONE || w <= 0f || h <= 0f) return

        val topWidth = selectedStrokeWidth(STROKE_TOP, stTopWidth)
        val leftWidth = selectedStrokeWidth(STROKE_LEFT, stLeftWidth)
        val bottomWidth = selectedStrokeWidth(STROKE_BOTTOM, stBottomWidth)
        val rightWidth = selectedStrokeWidth(STROKE_RIGHT, stRightWidth)
        val uniformWidth = when {
            topWidth > 0f -> topWidth
            leftWidth > 0f -> leftWidth
            bottomWidth > 0f -> bottomWidth
            rightWidth > 0f -> rightWidth
            else -> return
        }.takeIf { width ->
            (topWidth <= 0f || topWidth == width) &&
                (leftWidth <= 0f || leftWidth == width) &&
                (bottomWidth <= 0f || bottomWidth == width) &&
                (rightWidth <= 0f || rightWidth == width)
        }

        val activeOption =
            (if (topWidth > 0f) STROKE_TOP else 0) or
                (if (leftWidth > 0f) STROKE_LEFT else 0) or
                (if (bottomWidth > 0f) STROKE_BOTTOM else 0) or
                (if (rightWidth > 0f) STROKE_RIGHT else 0)

        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = this.strokeCap
        val hasValidDashEffect = isDashed && dashSpace > 0f
        strokePaint.pathEffect = if (hasValidDashEffect) {
            DashPathEffect(floatArrayOf(dashSpace, dashSpace), 0f)
        } else {
            null
        }
        val gradientColors = stColors
        if (gradientColors != null && gradientColors.size >= 2) {
            // Paint.color also changes Paint.alpha. Preserve the gradient colors' own alpha
            // instead of modulating the shader with a translucent solid stroke from a prior draw.
            strokePaint.alpha = 255
            strokePaint.shader = obtainStrokeShader(w, h, gradientColors)
        } else {
            strokePaint.shader = null
            strokePaint.color = stColor
        }

        if (uniformWidth != null && uniformWidth < min(w, h)) {
            strokePaint.strokeWidth = uniformWidth
            val inset = uniformWidth / 2f
            if (activeOption == STROKE_ALL) {
                val strokeRadii = getStrokeCornerRadii(w, h, inset)
                if (strokePathCacheRadii !== strokeRadii || strokePathCacheInset != inset ||
                    strokePathCacheW != w || strokePathCacheH != h
                ) {
                    strokeRectF.set(inset, inset, w - inset, h - inset)
                    strokePath.reset()
                    strokePath.fillType = Path.FillType.WINDING
                    strokePath.addRoundRect(strokeRectF, strokeRadii, Path.Direction.CW)
                    strokePathCacheRadii = strokeRadii
                    strokePathCacheInset = inset; strokePathCacheW = w; strokePathCacheH = h
                }
                canvas.drawPath(strokePath, strokePaint)
            } else {
                drawPartialStroke(canvas, w, h, inset, strokePaint, activeOption)
            }
        } else if (hasValidDashEffect) {
            drawDashedVariableStroke(
                canvas, w, h, topWidth, leftWidth, bottomWidth, rightWidth
            )
        } else {
            drawVariableStroke(
                canvas, w, h, topWidth, leftWidth, bottomWidth, rightWidth
            )
        }
    }

    /** Reuses the previously built gradient Shader when size/colors/orientation are unchanged. */
    private fun obtainStrokeShader(w: Float, h: Float, colors: IntArray): Shader {
        strokeShaderCache?.let {
            if (strokeShaderCacheW == w && strokeShaderCacheH == h &&
                strokeShaderCacheOrientation == strokeGradientOrientation &&
                strokeShaderCacheColors?.contentEquals(colors) == true
            ) {
                return it
            }
        }
        val (x0, y0, x1, y1) = strokeGradientOrientation.toCoordinates(w, h)
        val shader = LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
        strokeShaderCache = shader
        strokeShaderCacheW = w; strokeShaderCacheH = h
        strokeShaderCacheOrientation = strokeGradientOrientation
        strokeShaderCacheColors = colors.copyOf()
        return shader
    }

    private fun selectedStrokeWidth(side: Int, overrideWidth: Float): Float {
        if (strokeOption and side == 0) return 0f
        return (if (overrideWidth >= 0f) overrideWidth else stWidth).coerceAtLeast(0f)
    }

    /**
     * Renders an asymmetric border as the area between the view's outer shape and an inner
     * rounded shape inset independently on each side.
     */
    private fun drawVariableStroke(
        canvas: Canvas,
        w: Float,
        h: Float,
        topWidth: Float,
        leftWidth: Float,
        bottomWidth: Float,
        rightWidth: Float
    ) {
        val top = topWidth.coerceAtMost(h)
        val left = leftWidth.coerceAtMost(w)
        val bottom = bottomWidth.coerceAtMost(h)
        val right = rightWidth.coerceAtMost(w)
        val innerLeft = left
        val innerTop = top
        val innerRight = (w - right).coerceAtLeast(innerLeft)
        val innerBottom = (h - bottom).coerceAtLeast(innerTop)

        // strokePath is shared with the STROKE_ALL fast path in drawStroke(); invalidate its
        // cache since we're about to overwrite the Path with a different shape here.
        strokePathCacheRadii = null
        strokePath.reset()
        strokePath.fillType = Path.FillType.EVEN_ODD
        strokeRectF.set(0f, 0f, w, h)
        val outerRadii = getCornerRadii(w, h)
        strokePath.addRoundRect(strokeRectF, outerRadii, Path.Direction.CW)

        if (innerRight > innerLeft && innerBottom > innerTop) {
            val innerMaxRadiusX = (innerRight - innerLeft) / 2f
            val innerMaxRadiusY = (innerBottom - innerTop) / 2f
            val innerRadii = floatArrayOf(
                (outerRadii[0] - left).coerceIn(0f, innerMaxRadiusX),
                (outerRadii[1] - top).coerceIn(0f, innerMaxRadiusY),
                (outerRadii[2] - right).coerceIn(0f, innerMaxRadiusX),
                (outerRadii[3] - top).coerceIn(0f, innerMaxRadiusY),
                (outerRadii[4] - right).coerceIn(0f, innerMaxRadiusX),
                (outerRadii[5] - bottom).coerceIn(0f, innerMaxRadiusY),
                (outerRadii[6] - left).coerceIn(0f, innerMaxRadiusX),
                (outerRadii[7] - bottom).coerceIn(0f, innerMaxRadiusY)
            )
            strokeRectF.set(innerLeft, innerTop, innerRight, innerBottom)
            strokePath.addRoundRect(strokeRectF, innerRadii, Path.Direction.CW)
        }

        strokePaint.style = Paint.Style.FILL
        strokePaint.pathEffect = null
        canvas.drawPath(strokePath, strokePaint)
        strokePaint.style = Paint.Style.STROKE
    }

    private fun drawDashedVariableStroke(
        canvas: Canvas,
        w: Float,
        h: Float,
        topWidth: Float,
        leftWidth: Float,
        bottomWidth: Float,
        rightWidth: Float
    ) {
        drawDashedSide(canvas, w, h, STROKE_TOP, topWidth)
        drawDashedSide(canvas, w, h, STROKE_RIGHT, rightWidth)
        drawDashedSide(canvas, w, h, STROKE_BOTTOM, bottomWidth)
        drawDashedSide(canvas, w, h, STROKE_LEFT, leftWidth)
    }

    private fun drawDashedSide(canvas: Canvas, w: Float, h: Float, option: Int, width: Float) {
        if (width <= 0f) return
        val maxWidth = if (option == STROKE_TOP || option == STROKE_BOTTOM) h else w
        val safeWidth = width.coerceAtMost(maxWidth)
        strokePaint.strokeWidth = safeWidth
        drawPartialStroke(canvas, w, h, safeWidth / 2f, strokePaint, option)
    }

    /**
     * Draws a subset of the four sides. A corner arc is included whenever EITHER of its two
     * adjacent sides is active — e.g. `strokeOption = STROKE_TOP` alone still curves into both
     * top corners — instead of requiring both adjacent sides, which used to leave the line
     * floating as a flat, inset segment disconnected from the shape's rounded corners.
     *
     * The 8 clockwise features (corner arc, side line) x4 are walked starting right after a
     * genuine gap so that a run wrapping across the array boundary (e.g. LEFT+TOP) is still
     * drawn as one continuous subpath rather than being split with a spurious round cap.
     */
    private fun drawPartialStroke(
        canvas: Canvas,
        w: Float,
        h: Float,
        inset: Float,
        paint: Paint,
        option: Int
    ) {
        // strokePath is shared with the STROKE_ALL fast path in drawStroke(); invalidate its
        // cache since we're about to overwrite the Path with a different shape here.
        strokePathCacheRadii = null
        strokePath.reset()
        strokePath.fillType = Path.FillType.WINDING
        val radii = getStrokeCornerRadii(w, h, inset)
        val topLeftR = radii[0]
        val topRightR = radii[2]
        val bottomRightR = radii[4]
        val bottomLeftR = radii[6]

        val hasTop = option and STROKE_TOP != 0
        val hasRight = option and STROKE_RIGHT != 0
        val hasBottom = option and STROKE_BOTTOM != 0
        val hasLeft = option and STROKE_LEFT != 0

        val features = booleanArrayOf(
            (hasTop || hasLeft) && topLeftR > 0,          // 0: top-left arc
            hasTop,                                        // 1: top line
            (hasTop || hasRight) && topRightR > 0,         // 2: top-right arc
            hasRight,                                      // 3: right line
            (hasRight || hasBottom) && bottomRightR > 0,   // 4: bottom-right arc
            hasBottom,                                     // 5: bottom line
            (hasBottom || hasLeft) && bottomLeftR > 0,     // 6: bottom-left arc
            hasLeft                                        // 7: left line
        )
        if (features.none { it }) return

        val gapIndex = features.indices.firstOrNull { !features[it] }
        val start = if (gapIndex != null) (gapIndex + 1) % 8 else 0

        for (k in 0 until 8) {
            val i = (start + k) % 8
            if (!features[i]) continue
            val isNewRun = k == 0 || !features[(start + k - 1) % 8]
            when (i) {
                0 -> {
                    if (isNewRun) strokePath.moveTo(inset, inset + topLeftR)
                    strokePath.arcTo(inset, inset, inset + topLeftR * 2, inset + topLeftR * 2, 180f, 90f, false)
                }
                1 -> {
                    if (isNewRun) strokePath.moveTo(inset + topLeftR, inset)
                    strokePath.lineTo(w - inset - topRightR, inset)
                }
                2 -> {
                    if (isNewRun) strokePath.moveTo(w - inset - topRightR, inset)
                    strokePath.arcTo(w - inset - topRightR * 2, inset, w - inset, inset + topRightR * 2, -90f, 90f, false)
                }
                3 -> {
                    if (isNewRun) strokePath.moveTo(w - inset, inset + topRightR)
                    strokePath.lineTo(w - inset, h - inset - bottomRightR)
                }
                4 -> {
                    if (isNewRun) strokePath.moveTo(w - inset, h - inset - bottomRightR)
                    strokePath.arcTo(w - inset - bottomRightR * 2, h - inset - bottomRightR * 2, w - inset, h - inset, 0f, 90f, false)
                }
                5 -> {
                    if (isNewRun) strokePath.moveTo(w - inset - bottomRightR, h - inset)
                    strokePath.lineTo(inset + bottomLeftR, h - inset)
                }
                6 -> {
                    if (isNewRun) strokePath.moveTo(inset + bottomLeftR, h - inset)
                    strokePath.arcTo(inset, h - inset - bottomLeftR * 2, inset + bottomLeftR * 2, h - inset, 90f, 90f, false)
                }
                7 -> {
                    if (isNewRun) strokePath.moveTo(inset, h - inset - bottomLeftR)
                    strokePath.lineTo(inset, inset + topLeftR)
                }
            }
        }
        canvas.drawPath(strokePath, paint)
    }

    // Gradient type
    enum class GradientType {
        LINEAR, RADIAL, SWEEP
    }

    // Gradient orientation
    enum class GradientOrientation {
        TOP_TO_BOTTOM, TR_BL, RIGHT_TO_LEFT, BR_TL,
        BOTTOM_TO_TOP, BL_TR, LEFT_TO_RIGHT, TL_BR;

        fun toCoordinates(w: Float, h: Float) = when (this) {
            TOP_TO_BOTTOM -> Quad(0f, 0f, 0f, h)
            BOTTOM_TO_TOP -> Quad(0f, h, 0f, 0f)
            LEFT_TO_RIGHT -> Quad(0f, 0f, w, 0f)
            RIGHT_TO_LEFT -> Quad(w, 0f, 0f, 0f)
            TL_BR -> Quad(0f, 0f, w, h)
            TR_BL -> Quad(w, 0f, 0f, h)
            BL_TR -> Quad(0f, h, w, 0f)
            BR_TL -> Quad(w, h, 0f, 0f)
        }
    }

    data class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

    private fun Int.toGradientOrientation() =
        GradientOrientation.entries.getOrElse(this) { GradientOrientation.TOP_TO_BOTTOM }

    private fun Int.toStrokeOrientation() =
        GradientOrientation.entries.getOrElse(this) { GradientOrientation.LEFT_TO_RIGHT }

    private fun Int.toGradientType() = GradientType.entries.getOrElse(this) { GradientType.LINEAR }
    // ==================== Programmatic Setters ====================
    /** Set background gradient colors */
    fun setBgColors(vararg colors: Int): UiLayoutHelper {
        bgColors = colors.takeIf { it.size >= 2 }?.copyOf()
        isGradient = bgColors != null
        view.invalidate()
        return this
    }

    /** Set background gradient colors with orientation */
    fun setBgColors(orientation: GradientOrientation, vararg colors: Int): UiLayoutHelper {
        bgColors = colors.takeIf { it.size >= 2 }?.copyOf()
        isGradient = bgColors != null
        bgGradientOrientation = orientation
        view.invalidate()
        return this
    }

    /** Set stroke gradient colors */
    fun setStColors(vararg colors: Int): UiLayoutHelper {
        when (colors.size) {
            0 -> stColors = null
            1 -> {
                stColors = null
                stColor = colors[0]
            }
            else -> stColors = colors.copyOf()
        }
        view.invalidate()
        return this
    }

    /** Set stroke gradient colors with orientation */
    fun setStColors(orientation: GradientOrientation, vararg colors: Int): UiLayoutHelper {
        strokeGradientOrientation = orientation
        when (colors.size) {
            0 -> stColors = null
            1 -> {
                stColors = null
                stColor = colors[0]
            }
            else -> stColors = colors.copyOf()
        }
        view.invalidate()
        return this
    }

    /** Set solid background color */
    fun setBgColor(color: Int): UiLayoutHelper {
        bgColor = color
        bgColors = null
        view.invalidate()
        return this
    }

    /** Set solid stroke color */
    fun setStColor(color: Int): UiLayoutHelper {
        stColor = color
        stColors = null
        view.invalidate()
        return this
    }

    /** Set corner radius for all corners */
    fun setCorner(radius: Float): UiLayoutHelper {
        cornerRadius = radius
        cornerTopLeft = 0f
        cornerTopRight = 0f
        cornerBottomLeft = 0f
        cornerBottomRight = 0f
        view.invalidateOutline()
        view.invalidate()
        return this
    }

    /** Set individual corner radii (tl, tr, br, bl) */
    fun setCorners(tl: Float, tr: Float, br: Float, bl: Float): UiLayoutHelper {
        cornerRadius = 0f
        cornerTopLeft = tl
        cornerTopRight = tr
        cornerBottomRight = br
        cornerBottomLeft = bl
        view.invalidateOutline()
        view.invalidate()
        return this
    }

    /** Set stroke width */
    fun setStWidth(width: Float): UiLayoutHelper {
        stWidth = width
        view.invalidate()
        return this
    }

    /** Set dimension ratio (e.g., "16:9", "W,16:9", "H,4:3") */
    fun setDimenRatio(ratio: String?): UiLayoutHelper {
        if (ratio == null) {
            dimenRatio = null
            dimenRatioValue = 0f
        } else {
            parseDimenRatio(ratio)
        }
        view.requestLayout()
        return this
    }

    /** Set dimension ratio with explicit side */
    fun setDimenRatio(ratio: Float, side: DimenRatioSide = DimenRatioSide.AUTO): UiLayoutHelper {
        dimenRatioValue = ratio
        dimenRatioSide = side
        dimenRatio = when (side) {
            DimenRatioSide.WIDTH -> "W,$ratio"
            DimenRatioSide.HEIGHT -> "H,$ratio"
            DimenRatioSide.AUTO -> "$ratio"
        }
        view.requestLayout()
        return this
    }

    /** Set width as percentage of parent (0-100) */
    fun setWidthPercent(percent: Float): UiLayoutHelper {
        widthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set height as percentage of parent (0-100) */
    fun setHeightPercent(percent: Float): UiLayoutHelper {
        heightPercent = percent
        view.requestLayout()
        return this
    }

    /** Set both width and height as percentage of parent */
    fun setSizePercent(wPercent: Float, hPercent: Float): UiLayoutHelper {
        widthPercent = wPercent
        heightPercent = hPercent
        view.requestLayout()
        return this
    }

    /** Set max width as percentage of parent (0-100) */
    fun setMaxWidthPercent(percent: Float): UiLayoutHelper {
        maxWidthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set max height as percentage of parent (0-100) */
    fun setMaxHeightPercent(percent: Float): UiLayoutHelper {
        maxHeightPercent = percent
        view.requestLayout()
        return this
    }

    /** Set min width as percentage of parent (0-100) */
    fun setMinWidthPercent(percent: Float): UiLayoutHelper {
        minWidthPercent = percent
        view.requestLayout()
        return this
    }

    /** Set min height as percentage of parent (0-100) */
    fun setMinHeightPercent(percent: Float): UiLayoutHelper {
        minHeightPercent = percent
        view.requestLayout()
        return this
    }
}
