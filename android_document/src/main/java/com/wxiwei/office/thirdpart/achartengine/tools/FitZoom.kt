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
import com.wxiwei.office.thirdpart.achartengine.util.MathHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FitZoom
/**
 * Builds an instance of the fit zoom tool.
 * 
 * @param chart the XY chart
 */
    (chart: AbstractChart?) : AbstractTool(chart) {
    /**
     * Apply the tool.
     */
    fun apply() {
        if (mChart is XYChart) {
            if ((mChart as XYChart).dataset == null) {
                return
            }
            val scales = mRenderer!!.scalesCount
            if (mRenderer!!.isInitialRangeSet) {
                for (i in 0..<scales) {
                    if (mRenderer!!.isInitialRangeSet(i)) {
                        mRenderer!!.setRange(mRenderer!!.getInitialRange(i)!!, i)
                    }
                }
            } else {
                val series = (mChart as XYChart).dataset!!.series
                var range: DoubleArray? = null
                val length = series.size
                if (length > 0) {
                    for (i in 0..<scales) {
                        range = doubleArrayOf(
                            MathHelper.NULL_VALUE, -MathHelper.NULL_VALUE,
                            MathHelper.NULL_VALUE, -MathHelper.NULL_VALUE
                        )
                        for (j in 0..<length) {
                            if (i == series[j]!!.scaleNumber) {
                                range[0] = min(range[0], series[j]!!.minX)
                                range[1] = max(range[1], series[j]!!.maxX)
                                range[2] = min(range[2], series[j]!!.minY)
                                range[3] = max(range[3], series[j]!!.maxY)
                            }
                        }
                        val marginX = abs(range[1] - range[0]) / 40
                        val marginY = abs(range[3] - range[2]) / 40
                        mRenderer!!.setRange(
                            doubleArrayOf(
                                range[0] - marginX, range[1] + marginX,
                                range[2] - marginY, range[3] + marginY
                            ), i
                        )
                    }
                }
            }
        } else {
            val renderer = (mChart as RoundChart).renderer
            renderer.scale = renderer.originalScale
        }
    }
}
