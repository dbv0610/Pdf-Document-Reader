/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wxiwei.office.thirdpart.achartengine.renderers

import android.graphics.Color
import android.graphics.Paint
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.thirdpart.achartengine.util.MathHelper

/**
 * Multiple XY series renderer.
 */
class XYMultipleSeriesRenderer @JvmOverloads constructor(
    /** The number of scales.  */
    val scalesCount: Int = 1
) : DefaultRenderer() {
    /**
     * Returns the title for the X axis.
     * 
     * @return the X axis title
     */
    /**
     * Sets the title for the X axis.
     * 
     * @param title the X axis title
     */
    /** The X axis title.  */
    var xTitle: String? = ""

    /** The Y axis title.  */
    private var mYTitle: Array<String?> = arrayOfNulls(scalesCount)
    /**
     * Returns the horizontal axis title text size.
     * 
     * @return the axis title text size
     */
    /**
     * Sets the horizontal axis title text size.
     * 
     * @param textSize the chart axis text size
     */
    /** The axis title text size.  */
    var xTitleTextSize: Float = 12f
    /**
     * Returns the vertical axis title text size.
     * 
     * @return the axis title text size
     */
    /**
     * Sets the vertical axis title text size.
     * 
     * @param textSize the chart axis text size
     */
    var yTitleTextSize: Float = 12f

    /** The start value in the X axis range.  */
    private var mMinX: DoubleArray = DoubleArray(scalesCount)

    /** The end value in the X axis range.  */
    private var mMaxX: DoubleArray = DoubleArray(scalesCount)

    /** The start value in the Y axis range.  */
    private var mMinY: DoubleArray = DoubleArray(scalesCount)

    /** The end value in the Y axis range.  */
    private var mMaxY: DoubleArray = DoubleArray(scalesCount)
    /**
     * Returns the approximate number of labels for the X axis.
     * 
     * @return the approximate number of labels for the X axis
     */
    /**
     * Sets the approximate number of labels for the X axis.
     * 
     * @param xLabels the approximate number of labels for the X axis
     */
    /** The approximative number of labels on the x axis.  */
    var xLabels: Int = 5
    /**
     * Returns the approximate number of labels for the Y axis.
     * 
     * @return the approximate number of labels for the Y axis
     */
    /**
     * Sets the approximate number of labels for the Y axis.
     * 
     * @param yLabels the approximate number of labels for the Y axis
     */
    /** The approximative number of labels on the y axis.  */
    var yLabels: Int = 7
    /**
     * Returns the current orientation of the chart X axis.
     * 
     * @return the chart orientation
     */
    /**
     * Sets the current orientation of the chart X axis.
     * 
     * @param orientation the chart orientation
     */
    /** The current orientation of the chart.  */
    var orientation: Orientation = Orientation.HORIZONTAL

    /** The X axis text labels.  */
    private val mXTextLabels: MutableMap<Double?, String?> = HashMap<Double?, String?>()

    /** The Y axis text labels.  */
    private val mYTextLabels: MutableMap<Int?, MutableMap<Double?, String?>?> =
        LinkedHashMap<Int?, MutableMap<Double?, String?>?>()
    /**
     * Returns the enabled state of the pan on X axis.
     * 
     * @return if pan is enabled on X axis
     */
    /** A flag for enabling or not the pan on the X axis.  */
    var isPanXEnabled: Boolean = true
        private set
    /**
     * Returns the enabled state of the pan on Y axis.
     * 
     * @return if pan is enabled on Y axis
     */
    /** A flag for enabling or not the pan on the Y axis.  */
    var isPanYEnabled: Boolean = true
        private set
    /**
     * Returns the enabled state of the zoom on X axis.
     * 
     * @return if zoom is enabled on X axis
     */
    /** A flag for enabling or not the zoom on the X axis.  */
    var isZoomXEnabled: Boolean = true
        private set
    /**
     * Returns the enabled state of the zoom on Y axis.
     * 
     * @return if zoom is enabled on Y axis
     */
    /** A flag for enabling or not the zoom on the Y axis .  */
    var isZoomYEnabled: Boolean = true
        private set
    /**
     * Returns the spacing between bars, in bar charts.
     * 
     * @return the spacing between bars
     */
    /**
     * Sets the spacing between bars, in bar charts. Only available for bar
     * charts. This is a coefficient of the bar width. For instance, if you want
     * the spacing to be a half of the bar width, set this value to 0.5.
     * 
     * @param spacing the spacing between bars coefficient
     */
    /** The spacing between bars, in bar charts.  */
    var barSpacing: Double = 0.0
    /**
     * Returns the margins color.
     * 
     * @return the margins color
     */
    /**
     * Sets the color of the margins.
     * 
     * @param color the margins color
     */
    /** The margins colors.  */
    var marginsColor: Int = DefaultRenderer.Companion.NO_COLOR
    /**
     * Returns the pan limits.
     * 
     * @return the pan limits
     */
    /**
     * Sets the pan limits as an array of 4 values. Setting it to null or a
     * different size array will disable the panning limitation. Values:
     * [panMinimumX, panMaximumX, panMinimumY, panMaximumY]
     * 
     * @param panLimits the pan limits
     */
    /** The pan limits.  */
    var panLimits: DoubleArray? = null
    /**
     * Returns the zoom limits.
     * 
     * @return the zoom limits
     */
    /**
     * Sets the zoom limits as an array of 4 values. Setting it to null or a
     * different size array will disable the zooming limitation. Values:
     * [zoomMinimumX, zoomMaximumX, zoomMinimumY, zoomMaximumY]
     * 
     * @param zoomLimits the zoom limits
     */
    /** The zoom limits.  */
    var zoomLimits: DoubleArray? = null
    /**
     * Returns the rotation angle of labels for the X axis.
     * 
     * @return the rotation angle of labels for the X axis
     */
    /**
     * Sets the rotation angle (in degrees) of labels for the X axis.
     * 
     * @param angle the rotation angle of labels for the X axis
     */
    /** The X axis labels rotation angle.  */
    var xLabelsAngle: Float = 0f
    /**
     * Returns the rotation angle of labels for the Y axis.
     * 
     * @return the approximate number of labels for the Y axis
     */
    /**
     * Sets the rotation angle (in degrees) of labels for the Y axis.
     * 
     * @param angle the rotation angle of labels for the Y axis
     */
    /** The Y axis labels rotation angle.  */
    var yLabelsAngle: Float = 0f

    /** The initial axis range.  */
    private val initialRange: MutableMap<Int?, DoubleArray?> = LinkedHashMap<Int?, DoubleArray?>()
    /**
     * Returns the size of the points, for charts displaying points.
     * 
     * @return the point size
     */
    /**
     * Sets the size of the points, for charts displaying points.
     * 
     * @param size the point size
     */
    /** The point size for charts displaying points.  */
    var pointSize: Float = 5f
    /**
     * 
     * @return
     */
    /**
     * 
     * @param mSeriesBackgroundColor
     */
    /** The series background color.  */
    var seriesBackgroundColor: BackgroundAndFill? = null
    /**
     * 
     * @return
     */
    /**
     * sets the frame
     * @param frame
     */
    /*the series frame*/
    @JvmField
    var seriesFrame: Line? = null
    /**
     * Returns the grid color.
     * 
     * @return the grid color
     */
    /**
     * Sets the color of the grid.
     * 
     * @param color the grid color
     */
    /** The grid color.  */
    var gridColor: Int = Color.BLACK

    /**
     * Returns the X axis labels alignment.
     * 
     * @return X labels alignment
     */
    /**
     * Sets the X axis labels alignment.
     * 
     * @param align the X labels alignment
     */
    /** The X axis labels alignment.  */
    var xLabelsAlign: Paint.Align = Paint.Align.CENTER

    /** The Y axis labels alignment.  */
    private var yLabelsAlign: Array<Paint.Align?> = arrayOfNulls(scalesCount)

    /** The Y axis alignment.  */
    private var yAxisAlign: Array<Paint.Align?> = arrayOfNulls(scalesCount)

    /**
     * An enum for the XY chart orientation of the X axis.
     */
    enum class Orientation(angle: Int) {
        HORIZONTAL(0), VERTICAL(90);
        /**
         * Return the orientation rotate angle.
         * 
         * @return the orientaion rotate angle
         */
        /** The rotate angle.  */
        var angle: Int = 0
            private set

        init {
            this.angle = angle
        }
    }

    init {
        initAxesRange(scalesCount)
    }

    fun initAxesRange(scales: Int) {
        mYTitle = arrayOfNulls<String>(scales)
        yLabelsAlign = arrayOfNulls<Paint.Align>(scales)
        yAxisAlign = arrayOfNulls<Paint.Align>(scales)
        mMinX = DoubleArray(scales)
        mMaxX = DoubleArray(scales)
        mMinY = DoubleArray(scales)
        mMaxY = DoubleArray(scales)
        for (i in 0..<scales) {
            initAxesRangeForScale(i)
        }
    }

    fun initAxesRangeForScale(i: Int) {
        mMinX[i] = MathHelper.NULL_VALUE
        mMaxX[i] = -MathHelper.NULL_VALUE
        mMinY[i] = MathHelper.NULL_VALUE
        mMaxY[i] = -MathHelper.NULL_VALUE
        val range = doubleArrayOf(mMinX[i], mMaxX[i], mMinY[i], mMaxY[i])
        initialRange.put(i, range)
        mYTitle[i] = ""
        mYTextLabels.put(i, HashMap<Double?, String?>())
        yLabelsAlign[i] = Paint.Align.CENTER
        yAxisAlign[i] = Paint.Align.LEFT
    }

    var yTitle: String?
        /**
         * Returns the title for the Y axis.
         * 
         * @return the Y axis title
         */
        get() = getYTitle(0)
        /**
         * Sets the title for the Y axis.
         * 
         * @param title the Y axis title
         */
        set(title) {
            setYTitle(title, 0)
        }

    /**
     * Returns the title for the Y axis.
     * 
     * @param scale the renderer scale
     * @return the Y axis title
     */
    fun getYTitle(scale: Int): String? {
        return mYTitle[scale]
    }

    /**
     * Sets the title for the Y axis.
     * 
     * @param title the Y axis title
     * @param scale the renderer scale
     */
    fun setYTitle(title: String?, scale: Int) {
        mYTitle[scale] = title
    }

    var xAxisMin: Double
        /**
         * Returns the start value of the X axis range.
         * 
         * @return the X axis range start value
         */
        get() = getXAxisMin(0)
        /**
         * Sets the start value of the X axis range.
         * 
         * @param min the X axis range start value
         */
        set(min) {
            setXAxisMin(min, 0)
        }

    val isMinXSet: Boolean
        /**
         * Returns if the minimum X value was set.
         * 
         * @return the minX was set or not
         */
        get() = isMinXSet(0)

    var xAxisMax: Double
        /**
         * Returns the end value of the X axis range.
         * 
         * @return the X axis range end value
         */
        get() = getXAxisMax(0)
        /**
         * Sets the end value of the X axis range.
         * 
         * @param max the X axis range end value
         */
        set(max) {
            setXAxisMax(max, 0)
        }

    val isMaxXSet: Boolean
        /**
         * Returns if the maximum X value was set.
         * 
         * @return the maxX was set or not
         */
        get() = isMaxXSet(0)

    var yAxisMin: Double
        /**
         * Returns the start value of the Y axis range.
         * 
         * @return the Y axis range end value
         */
        get() = getYAxisMin(0)
        /**
         * Sets the start value of the Y axis range.
         * 
         * @param min the Y axis range start value
         */
        set(min) {
            setYAxisMin(min, 0)
        }

    val isMinYSet: Boolean
        /**
         * Returns if the minimum Y value was set.
         * 
         * @return the minY was set or not
         */
        get() = isMinYSet(0)

    var yAxisMax: Double
        /**
         * Returns the end value of the Y axis range.
         * 
         * @return the Y axis range end value
         */
        get() = getYAxisMax(0)
        /**
         * Sets the end value of the Y axis range.
         * 
         * @param max the Y axis range end value
         */
        set(max) {
            setYAxisMax(max, 0)
        }

    val isMaxYSet: Boolean
        /**
         * Returns if the maximum Y value was set.
         * 
         * @return the maxY was set or not
         */
        get() = isMaxYSet(0)

    /**
     * Returns the start value of the X axis range.
     * 
     * @param scale the renderer scale
     * @return the X axis range start value
     */
    fun getXAxisMin(scale: Int): Double {
        return mMinX[scale]
    }

    /**
     * Sets the start value of the X axis range.
     * 
     * @param min the X axis range start value
     * @param scale the renderer scale
     */
    fun setXAxisMin(min: Double, scale: Int) {
        if (!isMinXSet(scale)) {
            initialRange.get(scale)!![0] = min
        }
        mMinX[scale] = min
    }

    /**
     * Returns if the minimum X value was set.
     * 
     * @param scale the renderer scale
     * @return the minX was set or not
     */
    fun isMinXSet(scale: Int): Boolean {
        return mMinX[scale] != MathHelper.NULL_VALUE
    }

    /**
     * Returns the end value of the X axis range.
     * 
     * @param scale the renderer scale
     * @return the X axis range end value
     */
    fun getXAxisMax(scale: Int): Double {
        return mMaxX[scale]
    }

    /**
     * Sets the end value of the X axis range.
     * 
     * @param max the X axis range end value
     * @param scale the renderer scale
     */
    fun setXAxisMax(max: Double, scale: Int) {
        if (!isMaxXSet(scale)) {
            initialRange.get(scale)!![1] = max
        }
        mMaxX[scale] = max
    }

    /**
     * Returns if the maximum X value was set.
     * 
     * @param scale the renderer scale
     * @return the maxX was set or not
     */
    fun isMaxXSet(scale: Int): Boolean {
        return mMaxX[scale] != -MathHelper.NULL_VALUE
    }

    /**
     * Returns the start value of the Y axis range.
     * 
     * @param scale the renderer scale
     * @return the Y axis range end value
     */
    fun getYAxisMin(scale: Int): Double {
        return mMinY[scale]
    }

    /**
     * Sets the start value of the Y axis range.
     * 
     * @param min the Y axis range start value
     * @param scale the renderer scale
     */
    fun setYAxisMin(min: Double, scale: Int) {
        if (!isMinYSet(scale)) {
            initialRange.get(scale)!![2] = min
        }
        mMinY[scale] = min
    }

    /**
     * Returns if the minimum Y value was set.
     * 
     * @param scale the renderer scale
     * @return the minY was set or not
     */
    fun isMinYSet(scale: Int): Boolean {
        return mMinY[scale] != MathHelper.NULL_VALUE
    }

    /**
     * Returns the end value of the Y axis range.
     * 
     * @param scale the renderer scale
     * @return the Y axis range end value
     */
    fun getYAxisMax(scale: Int): Double {
        return mMaxY[scale]
    }

    /**
     * Sets the end value of the Y axis range.
     * 
     * @param max the Y axis range end value
     * @param scale the renderer scale
     */
    fun setYAxisMax(max: Double, scale: Int) {
        if (!isMaxYSet(scale)) {
            initialRange.get(scale)!![3] = max
        }
        mMaxY[scale] = max
    }

    /**
     * Returns if the maximum Y value was set.
     * 
     * @param scale the renderer scale
     * @return the maxY was set or not
     */
    fun isMaxYSet(scale: Int): Boolean {
        return mMaxY[scale] != -MathHelper.NULL_VALUE
    }

    val xTextLabels: Int
        get() = mXTextLabels.size

    /**
     * Adds a new text label for the specified X axis value.
     * 
     * @param x the X axis value
     * @param text the text label
     */
    @Deprecated("use addXTextLabel instead")
    fun addTextLabel(x: Double, text: String?) {
        addXTextLabel(x, text)
    }

    /**
     * Adds a new text label for the specified X axis value.
     * 
     * @param x the X axis value
     * @param text the text label
     */
    fun addXTextLabel(x: Double, text: String?) {
        mXTextLabels.put(x, text)
    }

    /**
     * Returns the X axis text label at the specified X axis value.
     * 
     * @param x the X axis value
     * @return the X axis text label
     */
    fun getXTextLabel(x: Double?): String? {
        return mXTextLabels.get(x)
    }

    val xTextLabelLocations: Array<Double>
        /**
         * Returns the X text label locations.
         * 
         * @return the X text label locations
         */
        get() = mXTextLabels.keys.map { it!! }.toTypedArray()

    /**
     * Clears the existing text labels.
     * 
     */
    @Deprecated("use clearXTextLabels instead")
    fun clearTextLabels() {
        clearXTextLabels()
    }

    /**
     * Clears the existing text labels on the X axis.
     */
    fun clearXTextLabels() {
        mXTextLabels.clear()
    }

    /**
     * Adds a new text label for the specified Y axis value.
     * 
     * @param y the Y axis value
     * @param text the text label
     * @param scale the renderer scale
     */
    /**
     * Adds a new text label for the specified Y axis value.
     * 
     * @param y the Y axis value
     * @param text the text label
     */
    @JvmOverloads
    fun addYTextLabel(y: Double, text: String?, scale: Int = 0) {
        mYTextLabels.get(scale)!!.put(y, text)
    }

    /**
     * Returns the Y axis text label at the specified Y axis value.
     * 
     * @param y the Y axis value
     * @return the Y axis text label
     */
    fun getYTextLabel(y: Double?): String? {
        return getYTextLabel(y, 0)
    }

    /**
     * Returns the Y axis text label at the specified Y axis value.
     * 
     * @param y the Y axis value
     * @param scale the renderer scale
     * @return the Y axis text label
     */
    fun getYTextLabel(y: Double?, scale: Int): String? {
        return mYTextLabels.get(scale)!!.get(y)
    }

    val yTextLabelLocations: Array<Double>
        /**
         * Returns the Y text label locations.
         * 
         * @return the Y text label locations
         */
        get() = getYTextLabelLocations(0)

    /**
     * Returns the Y text label locations.
     * 
     * @param scale the renderer scale
     * @return the Y text label locations
     */
    fun getYTextLabelLocations(scale: Int): Array<Double> {
        return mYTextLabels.get(scale)!!.keys.map { it!! }.toTypedArray()
    }

    /**
     * Clears the existing text labels on the Y axis.
     */
    fun clearYTextLabels() {
        mYTextLabels.clear()
    }

    /**
     * Sets if the chart point values should be displayed as text.
     * 
     * @param display if the chart point values should be displayed as text
     */
    @Deprecated("use SimpleSeriesRenderer.setDisplayChartValues() instead")
    fun setDisplayChartValues(display: Boolean) {
        val renderers = seriesRenderers
        for (renderer in renderers) {
            renderer!!.isDisplayChartValues = display
        }
    }

    /**
     * Sets the chart values text size.
     * 
     * @param textSize the chart values text size
     */
    @Deprecated("use SimpleSeriesRenderer.setChartValuesTextSize() instead")
    fun setChartValuesTextSize(textSize: Float) {
        val renderers = seriesRenderers
        for (renderer in renderers) {
            renderer!!.chartValuesTextSize = textSize
        }
    }

    /**
     * Returns the enabled state of the pan on at least one axis.
     * 
     * @return if pan is enabled
     */
    override val isPanEnabled: Boolean
        get() = this.isPanXEnabled && this.isPanYEnabled

    /**
     * Sets the enabled state of the pan.
     * 
     * @param enabledX pan enabled on X axis
     * @param enabledY pan enabled on Y axis
     */
    fun setPanEnabled(enabledX: Boolean, enabledY: Boolean) {
        this.isPanXEnabled = enabledX
        this.isPanYEnabled = enabledY
    }

    /**
     * Returns the enabled state of the zoom on at least one axis.
     * 
     * @return if zoom is enabled
     */
    override var isZoomEnabled: Boolean
        get() = this.isZoomXEnabled || this.isZoomYEnabled
        set(enabled) {
            super.isZoomEnabled = enabled
        }

    /**
     * Sets the enabled state of the zoom.
     * 
     * @param enabledX zoom enabled on X axis
     * @param enabledY zoom enabled on Y axis
     */
    fun setZoomEnabled(enabledX: Boolean, enabledY: Boolean) {
        this.isZoomXEnabled = enabledX
        this.isZoomYEnabled = enabledY
    }

    @get:Deprecated("use getBarSpacing instead")
    val barsSpacing: Double
        /**
         * Returns the spacing between bars, in bar charts.
         * 
         * @return the spacing between bars
         */
        get() = this.barSpacing

    fun setRange(range: DoubleArray) {
        setRange(range, 0)
    }

    /**
     * Sets the axes range values.
     * 
     * @param range an array having the values in this order: minX, maxX, minY,
     * maxY
     * @param scale the renderer scale
     */
    fun setRange(range: DoubleArray, scale: Int) {
        setXAxisMin(range[0], scale)
        setXAxisMax(range[1], scale)
        setYAxisMin(range[2], scale)
        setYAxisMax(range[3], scale)
    }

    val isInitialRangeSet: Boolean
        get() = isInitialRangeSet(0)

    /**
     * Returns if the initial range is set.
     * 
     * @param scale the renderer scale
     * @return the initial range was set or not
     */
    fun isInitialRangeSet(scale: Int): Boolean {
        return initialRange.get(scale) != null
    }

    /**
     * Returns the initial range.
     * 
     * @return the initial range
     */
    fun getInitialRange(): DoubleArray? {
        return getInitialRange(0)
    }

    /**
     * Returns the initial range.
     * 
     * @param scale the renderer scale
     * @return the initial range
     */
    fun getInitialRange(scale: Int): DoubleArray? {
        return initialRange.get(scale)
    }

    /**
     * Sets the axes initial range values. This will be used in the zoom fit tool.
     * 
     * @param range an array having the values in this order: minX, maxX, minY,
     * maxY
     */
    fun setInitialRange(range: DoubleArray?) {
        setInitialRange(range, 0)
    }

    /**
     * Sets the axes initial range values. This will be used in the zoom fit tool.
     * 
     * @param range an array having the values in this order: minX, maxX, minY,
     * maxY
     * @param scale the renderer scale
     */
    fun setInitialRange(range: DoubleArray?, scale: Int) {
        initialRange.put(scale, range)
    }

    /**
     * Returns the Y axis labels alignment.
     * 
     * @param scale the renderer scale
     * @return Y labels alignment
     */
    fun getYLabelsAlign(scale: Int): Paint.Align {
        return yLabelsAlign[scale]!!
    }

    fun setYLabelsAlign(align: Paint.Align?) {
        setYLabelsAlign(align, 0)
    }

    fun getYAxisAlign(scale: Int): Paint.Align {
        return yAxisAlign[scale]!!
    }

    fun setYAxisAlign(align: Paint.Align?, scale: Int) {
        yAxisAlign[scale] = align
    }

    /**
     * Sets the Y axis labels alignment.
     * 
     * @param align the Y labels alignment
     */
    fun setYLabelsAlign(align: Paint.Align?, scale: Int) {
        yLabelsAlign[scale] = align
    }
}
