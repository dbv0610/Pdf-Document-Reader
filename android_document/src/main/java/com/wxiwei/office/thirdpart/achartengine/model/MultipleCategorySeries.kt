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
 * A series for the multiple category charts like the doughnut.
 */
class MultipleCategorySeries
/**
 * Builds a new category series.
 * 
 * @param title the series title
 */(
    /** The series title.  */
    private val mTitle: String?
) : Serializable {
    /** The series local keys.  */
    private val mCategories: MutableList<String?> = ArrayList<String?>()

    /** The series name.  */
    private val mTitles: MutableList<Array<String?>?> = ArrayList<Array<String?>?>()

    /** The series values.  */
    private val mValues: MutableList<DoubleArray?> = ArrayList<DoubleArray?>()

    /**
     * Adds a new value to the series
     * 
     * @param titles the titles to be used as labels
     * @param values the new value
     */
    fun add(titles: Array<String?>?, values: DoubleArray?) {
        add(mCategories.size.toString() + "", titles, values)
    }

    /**
     * Adds a new value to the series.
     * 
     * @param category the category name
     * @param titles the titles to be used as labels
     * @param values the new value
     */
    fun add(category: String?, titles: Array<String?>?, values: DoubleArray?) {
        mCategories.add(category)
        mTitles.add(titles)
        mValues.add(values)
    }

    /**
     * Removes an existing value from the series.
     * 
     * @param index the index in the series of the value to remove
     */
    fun remove(index: Int) {
        mCategories.removeAt(index)
        mTitles.removeAt(index)
        mValues.removeAt(index)
    }

    /**
     * Removes all the existing values from the series.
     */
    fun clear() {
        mCategories.clear()
        mTitles.clear()
        mValues.clear()
    }

    /**
     * Returns the values at the specified index.
     * 
     * @param index the index
     * @return the value at the index
     */
    fun getValues(index: Int): DoubleArray {
        return mValues.get(index)!!
    }

    /**
     * Returns the category name at the specified index.
     * 
     * @param index the index
     * @return the category name at the index
     */
    fun getCategory(index: Int): String? {
        return mCategories.get(index)
    }

    val categoriesCount: Int
        /**
         * Returns the categories count.
         * 
         * @return the categories count
         */
        get() = mCategories.size

    /**
     * Returns the series item count.
     * 
     * @param index the index
     * @return the series item count
     */
    fun getItemCount(index: Int): Int {
        return mValues.get(index)!!.size
    }

    /**
     * Returns the series titles.
     * 
     * @param index the index
     * @return the series titles
     */
    fun getTitles(index: Int): Array<String?> {
        return mTitles.get(index)!!
    }

    /**
     * Transforms the category series to an XY series.
     * 
     * @return the XY series
     */
    fun toXYSeries(): XYSeries {
        val xySeries = XYSeries(mTitle)
        return xySeries
    }
}
