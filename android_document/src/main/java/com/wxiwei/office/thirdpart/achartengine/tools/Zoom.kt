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
import com.wxiwei.office.thirdpart.achartengine.chart.RoundChart
import com.wxiwei.office.thirdpart.achartengine.chart.XYChart

/**
 * The zoom tool.
 */
class Zoom(
    chart: AbstractChart?,
    /** A flag to be used to know if this is a zoom in or out.  */
    private val mZoomIn: Boolean, rate: Float
) : AbstractTool(chart) {
    /** The zoom rate.  */
    private var mZoomRate = 0f

    /**
     * Builds the zoom tool.
     * 
     * @param chart the chart
     * @param in zoom in or out
     * @param rate the zoom rate
     */
    init {
        setZoomRate(rate)
    }

    /**
     * Sets the zoom rate.
     * 
     * @param rate
     */
    fun setZoomRate(rate: Float) {
        mZoomRate = rate
    }

    /**
     * Apply the zoom.
     */
    fun apply() {
        if (mChart is XYChart) {
            val scales = mRenderer!!.scalesCount
            for (i in 0..<scales) {
                val range = getRange(i)
                checkRange(range!!, i)
                val limits = mRenderer!!.zoomLimits
                val limited = limits != null && limits.size == 4

                val centerX = (range[0] + range[1]) / 2
                val centerY = (range[2] + range[3]) / 2
                var newWidth = range[1] - range[0]
                var newHeight = range[3] - range[2]
                if (mZoomIn) {
                    if (mRenderer!!.isZoomXEnabled) {
                        newWidth /= mZoomRate.toDouble()
                    }
                    if (mRenderer!!.isZoomYEnabled) {
                        newHeight /= mZoomRate.toDouble()
                    }
                } else {
                    if (mRenderer!!.isZoomXEnabled) {
                        newWidth *= mZoomRate.toDouble()
                    }
                    if (mRenderer!!.isZoomYEnabled) {
                        newHeight *= mZoomRate.toDouble()
                    }
                }

                if (mRenderer!!.isZoomXEnabled) {
                    val newXMin = centerX - newWidth / 2
                    val newXMax = centerX + newWidth / 2
                    if (!limited || limits[0] <= newXMin && limits[1] >= newXMax) {
                        setXRange(newXMin, newXMax, i)
                    }
                }
                if (mRenderer!!.isZoomYEnabled) {
                    val newYMin = centerY - newHeight / 2
                    val newYMax = centerY + newHeight / 2
                    if (!limited || limits[2] <= newYMin && limits[3] >= newYMax) {
                        setYRange(newYMin, newYMax, i)
                    }
                }
            }
        } else {
            val renderer = (mChart as RoundChart).renderer
            if (mZoomIn) {
                renderer.scale = renderer.scale * mZoomRate
            } else {
                renderer.scale = renderer.scale / mZoomRate
            }
        }
    }
}
