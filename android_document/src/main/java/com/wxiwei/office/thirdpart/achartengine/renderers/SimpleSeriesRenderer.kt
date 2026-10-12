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
import java.io.Serializable

/**
 * A simple series renderer.
 */
open class SimpleSeriesRenderer : Serializable {
    /**
     * Returns the series color.
     * 
     * @return the series color
     */
    /**
     * Sets the series color.
     * 
     * @param color the series color
     */
    /** The series color.  */
    var color: Int = Color.BLUE
    /**
     * Returns if the chart point values should be displayed as text.
     * 
     * @return if the chart point values should be displayed as text
     */
    /**
     * Sets if the chart point values should be displayed as text.
     * 
     * @param display if the chart point values should be displayed as text
     */
    /** If the values should be displayed above the chart points.  */
    var isDisplayChartValues: Boolean = false
    /**
     * Returns the chart values text size.
     * 
     * @return the chart values text size
     */
    /**
     * Sets the chart values text size.
     * 
     * @param textSize the chart values text size
     */
    /** The chart values text size.  */
    var chartValuesTextSize: Float = 10f
    /**
     * Returns the stroke style.
     * 
     * @return the stroke style
     */
    /**
     * Sets the stroke style.
     * 
     * @param stroke the stroke style
     */
    /** The stroke style.  */
    var stroke: BasicStroke? = null
    /**
     * Returns the gradient is enabled value.
     * @return the gradient enabled
     */
    /**
     * Sets the gradient enabled value.
     * @param enabled the gradient enabled
     */
    /** If gradient is enabled.  */
    var isGradientEnabled: Boolean = false
    /**
     * Returns the gradient start value.
     * @return the gradient start value
     */
    /** The gradient start value.  */
    var gradientStartValue: Double = 0.0
        private set
    /**
     * Returns the gradient start color.
     * @return the gradient start color
     */
    /** The gradient start color.  */
    var gradientStartColor: Int = 0
        private set
    /**
     * Returns the gradient stop value.
     * @return the gradient stop value
     */
    /** The gradient stop value.  */
    var gradientStopValue: Double = 0.0
        private set
    /**
     * Returns the gradient stop color.
     * @return the gradient stop color
     */
    /** The gradient stop color.  */
    var gradientStopColor: Int = 0
        private set

    /**
     * Sets the gradient start value and color.
     * @param start the gradient start value
     * @param color the gradient start color
     */
    fun setGradientStart(start: Double, color: Int) {
        this.gradientStartValue = start
        this.gradientStartColor = color
    }

    /**
     * Sets the gradient stop value and color.
     * @param start the gradient stop value
     * @param color the gradient stop color
     */
    fun setGradientStop(start: Double, color: Int) {
        this.gradientStopValue = start
        this.gradientStopColor = color
    }
}
