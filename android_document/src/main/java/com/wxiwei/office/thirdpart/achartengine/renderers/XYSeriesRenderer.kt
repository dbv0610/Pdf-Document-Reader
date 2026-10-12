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
package com.wxiwei.office.thirdpart.achartengine.renderers

import android.graphics.Color
import com.wxiwei.office.thirdpart.achartengine.chart.PointStyle

/**
 * A renderer for the XY type series.
 */
class XYSeriesRenderer : SimpleSeriesRenderer() {
    /**
     * Returns if the chart points should be filled.
     * 
     * @return the points fill status
     */
    /**
     * Sets if the chart points should be filled.
     * 
     * @param fill the points fill flag value
     */
    /** If the chart points should be filled.  */
    var isFillPoints: Boolean = true
    /**
     * Returns if the chart should be filled below the line.
     * 
     * @return the fill below line status
     */
    /**
     * Sets if the line chart should be filled below its line. Filling below the
     * line transforms a line chart into an area chart.
     * 
     * @param fill the fill below line flag value
     */
    /** If the chart should be filled below its line.  */
    var isFillBelowLine: Boolean = false
    /**
     * Returns the fill below line color.
     * 
     * @return the fill below line color
     */
    /**
     * Sets the fill below the line color.
     * 
     * @param color the fill below line color
     */
    /** The fill below the chart line color.  */
    var fillBelowLineColor: Int = Color.argb(125, 0, 0, 200)
    /**
     * Returns the point style.
     * 
     * @return the point style
     */
    /**
     * Sets the point style.
     * 
     * @param style the point style
     */
    /** The point style.  */
    var pointStyle: PointStyle? = PointStyle.POINT
    /**
     * Returns the chart line width.
     * 
     * @return the line width
     */
    /**
     * Sets the chart line width.
     * 
     * @param lineWidth the line width
     */
    /** The chart line width.  */
    var lineWidth: Float = 3f
}
