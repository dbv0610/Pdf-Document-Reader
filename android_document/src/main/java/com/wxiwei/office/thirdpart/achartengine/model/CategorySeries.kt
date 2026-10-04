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

import java.io.Serializable

/**
 * A series for the category charts like the pie ones.
 */
open class CategorySeries
/**
 * Builds a new category series.
 * 
 * @param title the series title
 */(
    /** The series title.  */
    val title: String?
) : Serializable {
    /**
     * Returns the series title.
     * 
     * @return the series title
     */

    /** The series categories.  */
    private val mCategories: MutableList<String?> = ArrayList<String?>()

    /** The series values.  */
    private val mValues: MutableList<Double> = ArrayList<Double>()

    /**
     * Adds a new value to the series
     * 
     * @param value the new value
     */
    @Synchronized
    fun add(value: Double) {
        add((mCategories.size + 1).toString() + "", value)
    }

    /**
     * Adds a new value to the series.
     * 
     * @param category the category
     * @param value the new value
     */
    @Synchronized
    fun add(category: String?, value: Double) {
        mCategories.add(category)
        mValues.add(value)
    }

    /**
     * Replaces the value at the specific index in the series.
     * 
     * @param index the index in the series
     * @param category the category
     * @param value the new value
     */
    @Synchronized
    fun set(index: Int, category: String?, value: Double) {
        mCategories.set(index, category)
        mValues.set(index, value)
    }

    /**
     * Removes an existing value from the series.
     * 
     * @param index the index in the series of the value to remove
     */
    @Synchronized
    open fun remove(index: Int) {
        mCategories.removeAt(index)
        mValues.removeAt(index)
    }

    /**
     * Removes all the existing values from the series.
     */
    @Synchronized
    open fun clear() {
        mCategories.clear()
        mValues.clear()
    }

    /**
     * Returns the value at the specified index.
     * 
     * @param index the index
     * @return the value at the index
     */
    @Synchronized
    fun getValue(index: Int): Double {
        return mValues.get(index)!!
    }

    /**
     * Returns the category name at the specified index.
     * 
     * @param index the index
     * @return the category name at the index
     */
    @Synchronized
    fun getCategory(index: Int): String? {
        return mCategories.get(index)
    }

    @get:Synchronized
    val itemCount: Int
        /**
         * Returns the series item count.
         * 
         * @return the series item count
         */
        get() = mCategories.size

    /**
     * Transforms the category series to an XY series.
     * 
     * @return the XY series
     */
    open fun toXYSeries(): XYSeries {
        val xySeries = XYSeries(this.title)
        var k = 0
        for (value in mValues) {
            xySeries.add((++k).toDouble(), value!!)
        }
        return xySeries
    }
}
