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

import com.wxiwei.office.thirdpart.achartengine.chart.AbstractChart
import com.wxiwei.office.thirdpart.achartengine.chart.XYChart
import com.wxiwei.office.thirdpart.achartengine.renderers.XYMultipleSeriesRenderer

/**
 * Abstract class for being extended by graphical view tools.
 */
abstract class AbstractTool(
    /** The chart.  */
    protected var mChart: AbstractChart?
) {
    /** The renderer.  */
    protected var mRenderer: XYMultipleSeriesRenderer? = null

    /**
     * Abstract tool constructor.
     * 
     * @param chart the chart
     */
    init {
        if (mChart is XYChart) {
            mRenderer = (mChart as XYChart).renderer
        }
    }

    fun getRange(scale: Int): DoubleArray {
        val minX = mRenderer!!.getXAxisMin(scale)
        val maxX = mRenderer!!.getXAxisMax(scale)
        val minY = mRenderer!!.getYAxisMin(scale)
        val maxY = mRenderer!!.getYAxisMax(scale)
        return doubleArrayOf(minX, maxX, minY, maxY)
    }

    fun checkRange(range: DoubleArray, scale: Int) {
        if (mChart is XYChart) {
            val calcRange = (mChart as XYChart).getCalcRange(scale)
            if (calcRange != null) {
                if (!mRenderer!!.isMinXSet(scale)) {
                    range[0] = calcRange[0]
                    mRenderer!!.setXAxisMin(range[0], scale)
                }
                if (!mRenderer!!.isMaxXSet(scale)) {
                    range[1] = calcRange[1]
                    mRenderer!!.setXAxisMax(range[1], scale)
                }
                if (!mRenderer!!.isMinYSet(scale)) {
                    range[2] = calcRange[2]
                    mRenderer!!.setYAxisMin(range[2], scale)
                }
                if (!mRenderer!!.isMaxYSet(scale)) {
                    range[3] = calcRange[3]
                    mRenderer!!.setYAxisMax(range[3], scale)
                }
            }
        }
    }

    protected fun setXRange(min: Double, max: Double, scale: Int) {
        mRenderer!!.setXAxisMin(min, scale)
        mRenderer!!.setXAxisMax(max, scale)
    }

    protected fun setYRange(min: Double, max: Double, scale: Int) {
        mRenderer!!.setYAxisMin(min, scale)
        mRenderer!!.setYAxisMax(max, scale)
    }
}
