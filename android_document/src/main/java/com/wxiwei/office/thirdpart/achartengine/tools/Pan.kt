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
package com.wxiwei.office.thirdpart.achartengine.tools

import com.wxiwei.office.thirdpart.achartengine.chart.XYChart

/**
 * The pan tool.
 */
class Pan
/**
 * Builds and instance of the pan tool.
 * 
 * @param chart the XY chart
 */
    (chart: XYChart?) : AbstractTool(chart) {
    /**
     * Apply the tool.
     * 
     * @param oldX the previous location on X axis
     * @param oldY the previous location on Y axis
     * @param newX the current location on X axis
     * @param newY the current location on the Y axis
     */
    fun apply(oldX: Float, oldY: Float, newX: Float, newY: Float) {
        val scales = mRenderer!!.scalesCount
        val limits = mRenderer!!.panLimits
        val limited = limits != null && limits.size == 4
        val chart = mChart as XYChart
        for (i in 0..<scales) {
            val range = getRange(i)
            val calcRange = chart.getCalcRange(i)
            if (range[0] == range[1] && calcRange!![0] == calcRange[1] || range[2] == range[3]
                && calcRange!![2] == calcRange[3]
            ) {
                return
            }
            checkRange(range!!, i)

            val realPoint = chart.toRealPoint(oldX, oldY)
            val realPoint2 = chart.toRealPoint(newX, newY)
            val deltaX = realPoint[0] - realPoint2[0]
            val deltaY = realPoint[1] - realPoint2[1]
            if (mRenderer!!.isPanXEnabled) {
                if (limited) {
                    if (limits!![0] > range[0] + deltaX) {
                        setXRange(limits!![0], limits!![0] + (range[1] - range[0]), i)
                    } else if (limits!![1] < range[1] + deltaX) {
                        setXRange(limits!![1] - (range[1] - range[0]), limits!![1], i)
                    } else {
                        setXRange(range[0] + deltaX, range[1] + deltaX, i)
                    }
                } else {
                    setXRange(range[0] + deltaX, range[1] + deltaX, i)
                }
            }
            if (mRenderer!!.isPanYEnabled) {
                if (limited) {
                    if (limits!![2] > range[2] + deltaY) {
                        setYRange(limits!![2], limits!![2] + (range[3] - range[2]), i)
                    } else if (limits!![3] < range[3] + deltaY) {
                        setYRange(limits!![3] - (range[3] - range[2]), limits!![3], i)
                    } else {
                        setYRange(range[2] + deltaY, range[3] + deltaY, i)
                    }
                } else {
                    setYRange(range[2] + deltaY, range[3] + deltaY, i)
                }
            }
        }
    }
}
