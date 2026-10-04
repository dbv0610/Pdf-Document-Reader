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
import android.graphics.Typeface
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import java.io.Serializable

/**
 * An abstract renderer to be extended by the multiple series classes.
 */
open class DefaultRenderer : Serializable {
    @JvmField
    var defaultFontSize: Float = 12f

    /**
     * Returns the text typeface name.
     * 
     * @return the text typeface name
     */
    /** The typeface name for the texts.  */
    var textTypefaceName: String? = REGULAR_TEXT_FONT.toString()
        private set
    /**
     * Returns the text typeface style.
     * 
     * @return the text typeface style
     */
    /** The typeface style for the texts.  */
    var textTypefaceStyle: Int = Typeface.NORMAL
        private set
    /**
     * Returns the background color.
     * 
     * @return the background color
     */
    /**
     * Sets the background color.
     * 
     * @param color the background color
     */
    /** The chart background color.  */
    var backgroundAndFill: BackgroundAndFill? = null
    /**
     * 
     * @return
     */
    /**
     * sets the frame
     * @param frame
     */
    /*the chart frame*/
    var chartFrame: Line? = null
    /**
     * Returns if the background color should be applied.
     * 
     * @return the apply flag for the background color.
     */
    /**
     * Sets if the background color should be applied.
     * 
     * @param apply the apply flag for the background color
     */
    /** If the background color is applied.  */
    var isApplyBackgroundColor: Boolean = true

    /**
     * if the chart title visible
     */
    var isShowChartTitle: Boolean = true
    /**
     * Returns the chart title text size.
     * 
     * @return the chart title text size
     */
    /**
     * Sets the chart title text size.
     * 
     * @param textSize the chart title text size
     */
    /** The chart title text size.  */
    open var chartTitleTextSize: Float = 15f
    /**
     * Returns the chart title.
     * 
     * @return the chart title
     */
    /**
     * Sets the chart title.
     * 
     * @param title the chart title
     */
    /** The chart title.  */
    open var chartTitle: String? = ""
    /**
     * Returns if the axes should be visible.
     * 
     * @return the visibility flag for the axes
     */
    /**
     * Sets if the axes should be visible.
     * 
     * @param showAxes the visibility flag for the axes
     */
    /** If the axes are visible.  */
    var isShowAxes: Boolean = true
    /**
     * Returns the axes color.
     * 
     * @return the axes color
     */
    /**
     * Sets the axes color.
     * 
     * @param color the axes color
     */
    /** The axes color.  */
    var axesColor: Int = Color.BLACK
    /**
     * Returns if the labels should be visible.
     * 
     * @return the visibility flag for the labels
     */
    /**
     * Sets if the labels should be visible.
     * 
     * @param showLabels the visibility flag for the labels
     */
    /** If the labels are visible.  */
    var isShowLabels: Boolean = true
    /**
     * Returns the labels color.
     * 
     * @return the labels color
     */
    /**
     * Sets the labels color.
     * 
     * @param color the labels color
     */
    /** The labels color.  */
    var labelsColor: Int = TEXT_COLOR
    /**
     * Returns the labels text size.
     * 
     * @return the labels text size
     */
    /**
     * Sets the labels text size.
     * 
     * @param textSize the labels text size
     */
    /** The labels text size.  */
    var labelsTextSize: Float = 10f
    /**
     * Returns if the legend should be visible.
     * 
     * @return the visibility flag for the legend
     */
    /**
     * Sets if the legend should be visible.
     * 
     * @param showLegend the visibility flag for the legend
     */
    /** If the legend is visible.  */
    var isShowLegend: Boolean = true
    /**
     * Returns the legend text size.
     * 
     * @return the legend text size
     */
    /**
     * Sets the legend text size.
     * 
     * @param textSize the legend text size
     */
    /** The legend text size.  */
    var legendTextSize: Float = 12f
    /**
     * Returns if the legend should size to fit.
     * 
     * @return the fit behavior
     */
    /**
     * Sets if the legend should size to fit.
     * 
     * @param fit the fit behavior
     */
    /** If the legend should size to fit.  */
    var isFitLegend: Boolean = false
    /**
     * Returns if the grid should be visible.
     * 
     * @return the visibility flag for the grid
     */
    /**
     * Sets if the grid should be visible.
     * 
     * @param showGrid the visibility flag for the grid
     */
    /** If the grid should be displayed.  */
    var isShowGridH: Boolean = false
    /**
     * Returns if the grid should be visible.
     * 
     * @return the visibility flag for the grid
     */
    /**
     * Sets if the grid should be visible.
     * 
     * @param showGrid the visibility flag for the grid
     */
    var isShowGridV: Boolean = false
    /**
     * Returns if the grid should be visible for custom X or Y labels.
     * 
     * @return the visibility flag for the custom text grid
     */
    /**
     * Sets if the grid for custom X or Y labels should be visible.
     * 
     * @param showGrid the visibility flag for the custom text grid
     */
    /** If the custom text grid should be displayed.  */
    var isShowCustomTextGrid: Boolean = false

    /** The simple renderers that are included in this multiple series renderer.  */
    private val mRenderers: MutableList<SimpleSeriesRenderer?> = ArrayList<SimpleSeriesRenderer?>()
    /**
     * Returns the antialiasing flag value.
     * 
     * @return the antialiasing value
     */
    /**
     * Sets the antialiasing value.
     * 
     * @param antialiasing the antialiasing
     */
    /** The antialiasing flag.  */
    var isAntialiasing: Boolean = true
    /**
     * Returns the legend height.
     * 
     * @return the legend height
     */
    /**
     * Sets the legend height, in pixels.
     * 
     * @param height the legend height
     */
    /** The legend height.  */
    var legendHeight: Int = 0
    /**
     * Returns the margin sizes. An array containing the margins in this order:
     * top, left, bottom, right
     * 
     * @return the margin sizes
     */
    /**
     * Sets the margins, in pixels.
     * 
     * @param margins an array containing the margin size values, in this order:
     * top, left, bottom, right
     */
    /** The margins size.  */
    var margins: DoubleArray = doubleArrayOf(0.1, 0.05, 0.1, 0.05)

    /** A value to be used for scaling the chart.  */
    private var mScale = 1f
    /**
     * Returns the enabled state of the zoom.
     * 
     * @return if zoom is enabled
     */
    /**
     * Sets the enabled state of the zoom.
     * 
     * @param enabled zoom enabled
     */
    /** A flag for enabling the zoom.  */
    open var isZoomEnabled: Boolean = true
    /**
     * Returns the visible state of the zoom buttons.
     * 
     * @return if zoom buttons are visible
     */
    /**
     * Sets the visible state of the zoom buttons.
     * 
     * @param visible if the zoom buttons are visible
     */
    /** A flag for enabling the visibility of the zoom buttons.  */
    var isZoomButtonsVisible: Boolean = false
    /**
     * Returns the zoom rate.
     * 
     * @return the zoom rate
     */
    /**
     * Sets the zoom rate.
     * 
     * @param rate the zoom rate
     */
    /** The zoom rate.  */
    var zoomRate: Float = 1.0f
    /**
     * Returns the original value to be used for scaling the chart.
     * 
     * @return the original scale value
     */
    /** The original chart scale.  */
    var originalScale: Float = mScale
        private set

    /**
     * Adds a simple renderer to the multiple renderer.
     * 
     * @param renderer the renderer to be added
     */
    fun addSeriesRenderer(renderer: SimpleSeriesRenderer?) {
        mRenderers.add(renderer)
    }

    /**
     * Adds a simple renderer to the multiple renderer.
     * 
     * @param index the index in the renderers list
     * @param renderer the renderer to be added
     */
    fun addSeriesRenderer(index: Int, renderer: SimpleSeriesRenderer?) {
        mRenderers.add(index, renderer)
    }

    /**
     * Removes a simple renderer from the multiple renderer.
     * 
     * @param renderer the renderer to be removed
     */
    fun removeSeriesRenderer(renderer: SimpleSeriesRenderer?) {
        mRenderers.remove(renderer)
    }

    /**
     * Returns the simple renderer from the multiple renderer list.
     * 
     * @param index the index in the simple renderers list
     * @return the simple renderer at the specified index
     */
    fun getSeriesRendererAt(index: Int): SimpleSeriesRenderer {
        return mRenderers.get(index)!!
    }

    val seriesRendererCount: Int
        /**
         * Returns the simple renderers count in the multiple renderer list.
         * 
         * @return the simple renderers count
         */
        get() = mRenderers.size

    val seriesRenderers: Array<SimpleSeriesRenderer?>
        /**
         * Returns an array of the simple renderers in the multiple renderer list.
         * 
         * @return the simple renderers array
         */
        get() = mRenderers.toTypedArray<SimpleSeriesRenderer?>()

    var backgroundColor: Int
        get() = Color.BLACK
        set(color) {
        }

    /**
     * Sets the text typeface name and style.
     * 
     * @param typefaceName the text typeface name
     * @param style the text typeface style
     */
    fun setTextTypeface(typefaceName: String?, style: Int) {
        textTypefaceName = typefaceName
        textTypefaceStyle = style
    }

    var scale: Float
        /**
         * Returns the value to be used for scaling the chart.
         * 
         * @return the scale value
         */
        get() = mScale
        /**
         * Sets the value to be used for scaling the chart. It works on some charts
         * like pie, doughnut, dial.
         * 
         * @param scale the scale value
         */
        set(scale) {
            if (this.originalScale == 1f) {
                this.originalScale = scale
            }
            mScale = scale
        }

    open val isPanEnabled: Boolean
        /**
         * Returns the enabled state of the pan.
         * 
         * @return if pan is enabled
         */
        get() = false

    companion object {
        /** A no color constant.  */
        const val NO_COLOR: Int = 0

        /** The default background color.  */
        val BACKGROUND_COLOR: Int = Color.BLACK

        /** The default color for text.  */
        val TEXT_COLOR: Int = Color.BLACK

        /** A text font for regular text, like the chart labels.  */
        private val REGULAR_TEXT_FONT: Typeface = Typeface
            .create(Typeface.SERIF, Typeface.NORMAL)
    }
}
