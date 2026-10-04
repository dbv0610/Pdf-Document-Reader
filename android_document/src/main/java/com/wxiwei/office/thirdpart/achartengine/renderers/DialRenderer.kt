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

import com.wxiwei.office.thirdpart.achartengine.util.MathHelper
import java.util.Arrays

/**
 * Dial chart renderer.
 */
class DialRenderer : DefaultRenderer() {
    /** The chart title.  */
    private var mChartTitle: String? = ""

    /** The chart title text size.  */
    private var mChartTitleTextSize = 15f
    /**
     * Returns the start angle value of the dial.
     * 
     * @return the angle start value
     */
    /**
     * Sets the start angle value of the dial.
     * 
     * @param min the dial angle start value
     */
    /** The start angle in the dial range.  */
    var angleMin: Double = 330.0
    /**
     * Returns the end angle value of the dial.
     * 
     * @return the angle end value
     */
    /**
     * Sets the end angle value of the dial.
     * 
     * @param max the dial angle end value
     */
    /** The end angle in the dial range.  */
    var angleMax: Double = 30.0
    /**
     * Returns the start value to be rendered on the dial.
     * 
     * @return the start value on dial
     */
    /**
     * Sets the start value to be rendered on the dial.
     * 
     * @param min the start value on the dial
     */
    /** The start value in dial range.  */
    var minValue: Double = MathHelper.NULL_VALUE
    /**
     * Returns the end value to be rendered on the dial.
     * 
     * @return the end value on the dial
     */
    /**
     * Sets the end value to be rendered on the dial.
     * 
     * @param max the end value on the dial
     */
    /** The end value in dial range.  */
    var maxValue: Double = -MathHelper.NULL_VALUE
    /**
     * Returns the minor ticks spacing.
     * 
     * @return the minor ticks spacing
     */
    /**
     * Sets the minor ticks spacing.
     * 
     * @param spacing the minor ticks spacing
     */
    /** The spacing for the minor ticks.  */
    var minorTicksSpacing: Double = MathHelper.NULL_VALUE
    /**
     * Returns the major ticks spacing.
     * 
     * @return the major ticks spacing
     */
    /**
     * Sets the major ticks spacing.
     * 
     * @param spacing the major ticks spacing
     */
    /** The spacing for the major ticks.  */
    var majorTicksSpacing: Double = MathHelper.NULL_VALUE

    /** An array of the renderers types (default is NEEDLE).  */
    private val visualTypes: MutableList<Type?> = ArrayList<Type?>()

    enum class Type {
        NEEDLE, ARROW
    }

    /**
     * Returns the chart title.
     * 
     * @return the chart title
     */
    override var chartTitle: String?
        get() = mChartTitle
        set(title) {
            mChartTitle = title
        }

    /**
     * Returns the chart title text size.
     * 
     * @return the chart title text size
     */
    override var chartTitleTextSize: Float
        get() = mChartTitleTextSize
        set(textSize) {
            mChartTitleTextSize = textSize
        }

    val isMinValueSet: Boolean
        /**
         * Returns if the minimum dial value was set.
         * 
         * @return the minimum dial value was set or not
         */
        get() = this.minValue != MathHelper.NULL_VALUE

    val isMaxValueSet: Boolean
        /**
         * Returns if the maximum dial value was set.
         * 
         * @return the maximum dial was set or not
         */
        get() = this.maxValue != -MathHelper.NULL_VALUE

    /**
     * Returns the visual type at the specified index.
     * 
     * @param index the index
     * @return the visual type
     */
    fun getVisualTypeForIndex(index: Int): Type? {
        if (index < visualTypes.size) {
            return visualTypes.get(index)
        }
        return Type.NEEDLE
    }

    /**
     * Sets the visual types.
     * 
     * @param types the visual types
     */
    fun setVisualTypes(types: Array<Type?>) {
        visualTypes.clear()
        visualTypes.addAll(Arrays.asList<Type?>(*types))
    }
}
