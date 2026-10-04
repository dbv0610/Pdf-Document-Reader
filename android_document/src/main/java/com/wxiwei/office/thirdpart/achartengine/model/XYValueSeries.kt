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
package com.wxiwei.office.thirdpart.achartengine.model

import com.wxiwei.office.thirdpart.achartengine.util.MathHelper
import kotlin.math.max
import kotlin.math.min

/**
 * An extension of the XY series which adds a third dimension. It is used for XY
 * charts like bubble.
 */
class XYValueSeries
/**
 * Builds a new XY value series.
 * 
 * @param title the series title.
 */
    (title: String?) : XYSeries(title) {
    /** A list to contain the series values.  */
    private val mValue: MutableList<Double> = ArrayList<Double>()
    /**
     * Returns the minimum value.
     * 
     * @return the minimum value
     */
    /** The minimum value.  */
    var minValue: Double = MathHelper.NULL_VALUE
        private set
    /**
     * Returns the maximum value.
     * 
     * @return the maximum value
     */
    /** The maximum value.  */
    var maxValue: Double = -MathHelper.NULL_VALUE
        private set

    /**
     * Adds a new value to the series.
     * 
     * @param x the value for the X axis
     * @param y the value for the Y axis
     * @param value the value
     */
    @Synchronized
    fun add(x: Double, y: Double, value: Double) {
        super.add(x, y)
        mValue.add(value)
        updateRange(value)
    }

    /**
     * Initializes the values range.
     */
    private fun initRange() {
        this.minValue = MathHelper.NULL_VALUE
        this.maxValue = MathHelper.NULL_VALUE
        val length = itemCount
        for (k in 0..<length) {
            updateRange(getValue(k))
        }
    }

    /**
     * Updates the values range.
     * 
     * @param value the new value
     */
    private fun updateRange(value: Double) {
        this.minValue = min(this.minValue, value)
        this.maxValue = max(this.maxValue, value)
    }

    /**
     * Adds a new value to the series.
     * 
     * @param x the value for the X axis
     * @param y the value for the Y axis
     */
    @Synchronized
    override fun add(x: Double, y: Double) {
        add(x, y, 0.0)
    }

    /**
     * Removes an existing value from the series.
     * 
     * @param index the index in the series of the value to remove
     */
    @Synchronized
    override fun remove(index: Int) {
        super.remove(index)
        val removedValue = mValue.removeAt(index)!!
        if (removedValue == this.minValue || removedValue == this.maxValue) {
            initRange()
        }
    }

    /**
     * Removes all the values from the series.
     */
    @Synchronized
    override fun clear() {
        super.clear()
        mValue.clear()
        initRange()
    }

    /**
     * Returns the value at the specified index.
     * 
     * @param index the index
     * @return the value
     */
    @Synchronized
    fun getValue(index: Int): Double {
        return mValue.get(index)!!
    }
}
