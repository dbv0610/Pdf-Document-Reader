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
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

/**
 * An XY series encapsulates values for XY charts like line, time, area,
 * scatter... charts.
 */
open class XYSeries @JvmOverloads constructor(
    /** The series title.  */
    var title: String?,
    /** The scale number for this series.  */
    val scaleNumber: Int = 0
) : Serializable {
    /**
     * Returns the series title.
     * 
     * @return the series title
     */
    /**
     * Sets the series title.
     * 
     * @param title the series title
     */

    /** A list to contain the values for the X axis.  */
    private val mX: MutableList<Double> = ArrayList<Double>()

    /** A list to contain the values for the Y axis.  */
    private val mY: MutableList<Double> = ArrayList<Double>()
    /**
     * Returns the minimum value on the X axis.
     * 
     * @return the X axis minimum value
     */
    /** The minimum value for the X axis.  */
    var minX: Double = MathHelper.NULL_VALUE
        private set
    /**
     * Returns the maximum value on the X axis.
     * 
     * @return the X axis maximum value
     */
    /** The maximum value for the X axis.  */
    var maxX: Double = -MathHelper.NULL_VALUE
        private set
    /**
     * Returns the minimum value on the Y axis.
     * 
     * @return the Y axis minimum value
     */
    /** The minimum value for the Y axis.  */
    var minY: Double = MathHelper.NULL_VALUE
        private set
    /**
     * Returns the maximum value on the Y axis.
     * 
     * @return the Y axis maximum value
     */
    /** The maximum value for the Y axis.  */
    var maxY: Double = -MathHelper.NULL_VALUE
        private set

    /**
     * Builds a new XY series.
     * 
     * @param title the series title.
     * @param scaleNumber the series scale number
     */
    /**
     * Builds a new XY series.
     * 
     * @param title the series title.
     */
    init {
        initRange()
    }

    /**
     * Initializes the range for both axes.
     */
    private fun initRange() {
        this.minX = MathHelper.NULL_VALUE
        this.maxX = -MathHelper.NULL_VALUE
        this.minY = MathHelper.NULL_VALUE
        this.maxY = -MathHelper.NULL_VALUE
        val length = this.itemCount
        for (k in 0..<length) {
            val x = getX(k)
            val y = getY(k)
            updateRange(x, y)
        }
    }

    /**
     * Updates the range on both axes.
     * 
     * @param x the new x value
     * @param y the new y value
     */
    private fun updateRange(x: Double, y: Double) {
        this.minX = min(this.minX, x)
        this.maxX = max(this.maxX, x)
        this.minY = min(this.minY, y)
        this.maxY = max(this.maxY, y)
    }

    /**
     * Adds a new value to the series.
     * 
     * @param x the value for the X axis
     * @param y the value for the Y axis
     */
    @Synchronized
    open fun add(x: Double, y: Double) {
        mX.add(x)
        mY.add(y)
        updateRange(x, y)
    }

    /**
     * Removes an existing value from the series.
     * 
     * @param index the index in the series of the value to remove
     */
    @Synchronized
    open fun remove(index: Int) {
        val removedX = mX.removeAt(index)!!
        val removedY = mY.removeAt(index)!!
        if (removedX == this.minX || removedX == this.maxX || removedY == this.minY || removedY == this.maxY) {
            initRange()
        }
    }

    /**
     * Removes all the existing values from the series.
     */
    @Synchronized
    open fun clear() {
        mX.clear()
        mY.clear()
        initRange()
    }

    /**
     * Returns the X axis value at the specified index.
     * 
     * @param index the index
     * @return the X value
     */
    @Synchronized
    fun getX(index: Int): Double {
        return mX.get(index)!!
    }

    /**
     * Returns the Y axis value at the specified index.
     * 
     * @param index the index
     * @return the Y value
     */
    @Synchronized
    fun getY(index: Int): Double {
        return mY.get(index)!!
    }

    @get:Synchronized
    val itemCount: Int
        /**
         * Returns the series item count.
         * 
         * @return the series item count
         */
        get() = mX.size
}
