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
package com.wxiwei.office.thirdpart.achartengine.chart

import kotlin.math.max

/**
 * The chart point style enumerator.
 */
enum class PointStyle
/**
 * The point style enum constructor.
 * 
 * @param name the name
 */(
    /** The point shape name.  */
    private val styleName: String
) {
    X("x"), CIRCLE("circle"), TRIANGLE("triangle"), SQUARE("square"), DIAMOND("diamond"), POINT(
        "point"
    );

    /**
     * Returns the point shape name.
     * 
     * @return the point shape name
     */

    /**
     * Returns the point shape name.
     * 
     * @return the point shape name
     */
    override fun toString(): String {
        return this.styleName
    }

    companion object {
        /**
         * Return the point shape that has the provided symbol.
         * 
         * @param name the point style name
         * @return the point shape
         */
        fun getPointStyleForName(name: String?): PointStyle? {
            var pointStyle: PointStyle? = null
            val styles: Array<PointStyle?> = entries.toTypedArray()
            val length = styles.size
            var i = 0
            while (i < length && pointStyle == null) {
                if (styles[i]!!.styleName == name) {
                    pointStyle = styles[i]
                }
                i++
            }
            return pointStyle
        }

        /**
         * Returns the point shape index based on the given name.
         * 
         * @return the point shape index
         */
        fun getIndexForName(name: String?): Int {
            var index = -1
            val styles: Array<PointStyle?> = entries.toTypedArray()
            val length = styles.size
            var i = 0
            while (i < length && index < 0) {
                if (styles[i]!!.styleName == name) {
                    index = i
                }
                i++
            }
            return max(0, index)
        }
    }
}
