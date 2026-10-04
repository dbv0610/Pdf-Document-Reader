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

import java.util.Date

/**
 * A series for the date / time charts.
 */
class TimeSeries
/**
 * Builds a new date / time series.
 * 
 * @param title the series title
 */
    (title: String?) : XYSeries(title) {
    /**
     * Adds a new value to the series.
     * 
     * @param x the date / time value for the X axis
     * @param y the value for the Y axis
     */
    @Synchronized
    fun add(x: Date, y: Double) {
        super.add(x.getTime().toDouble(), y)
    }
}
