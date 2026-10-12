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

/**
 * A series for the range category charts like the range bar.
 */
class RangeCategorySeries
/**
 * Builds a new category series.
 * 
 * @param title the series title
 */
    (title: String?) : CategorySeries(title) {
    /** The series values.  */
    private val mMaxValues: MutableList<Double> = ArrayList<Double>()

    /**
     * Adds new values to the series
     * 
     * @param minValue the new minimum value
     * @param maxValue the new maximum value
     */
    @Synchronized
    fun add(minValue: Double, maxValue: Double) {
        super.add(minValue)
        mMaxValues.add(maxValue)
    }

    /**
     * Adds new values to the series.
     * 
     * @param category the category
     * @param minValue the new minimum value
     * @param maxValue the new maximum value
     */
    @Synchronized
    fun add(category: String?, minValue: Double, maxValue: Double) {
        super.add(category, minValue)
        mMaxValues.add(maxValue)
    }

    /**
     * Removes existing values from the series.
     * 
     * @param index the index in the series of the values to remove
     */
    @Synchronized
    override fun remove(index: Int) {
        super.remove(index)
        mMaxValues.removeAt(index)
    }

    /**
     * Removes all the existing values from the series.
     */
    @Synchronized
    override fun clear() {
        super.clear()
        mMaxValues.clear()
    }

    /**
     * Returns the minimum value at the specified index.
     * 
     * @param index the index
     * @return the minimum value at the index
     */
    fun getMinimumValue(index: Int): Double {
        return getValue(index)
    }

    /**
     * Returns the maximum value at the specified index.
     * 
     * @param index the index
     * @return the maximum value at the index
     */
    fun getMaximumValue(index: Int): Double {
        return mMaxValues.get(index)!!
    }

    /**
     * Transforms the range category series to an XY series.
     * 
     * @return the XY series
     */
    override fun toXYSeries(): XYSeries {
        val xySeries = XYSeries(title)
        val length = itemCount
        for (k in 0..<length) {
            xySeries.add((k + 1).toDouble(), getMinimumValue(k))
            xySeries.add((k + 1).toDouble(), getMaximumValue(k))
        }
        return xySeries
    }
}
