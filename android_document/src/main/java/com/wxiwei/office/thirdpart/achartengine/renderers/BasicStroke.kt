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

import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import java.io.Serializable

/**
 * A descriptor for the stroke style.
 */
class BasicStroke
/**
 * Build a new basic stroke style.
 * 
 * @param cap the stroke cap
 * @param join the stroke join
 * @param miter the stroke miter
 * @param intervals the path effect intervals
 * @param the path effect phase
 */(
    /** The stroke cap.  */
    val cap: Cap?,
    /** The stroke join.  */
    val join: Join?,
    /** The stroke miter.  */
    val miter: Float,
    /** The path effect intervals.  */
    val intervals: FloatArray?, phase: Float
) : Serializable {
    /**
     * Returns the stroke cap.
     * 
     * @return the stroke cap
     */
    /**
     * Returns the stroke join.
     * 
     * @return the stroke join
     */
    /**
     * Returns the stroke miter.
     * 
     * @return the stroke miter
     */
    /**
     * Returns the path effect intervals.
     * 
     * @return the path effect intervals
     */
    /**
     * Returns the path effect phase.
     * 
     * @return the path effect phase
     */
    /** The path effect phase.  */
    val phase: Float = 0f

    companion object {
        /** The solid line style.  */
        val SOLID: BasicStroke = BasicStroke(Cap.BUTT, Join.MITER, 4f, null, 0f)

        /** The dashed line style.  */
        val DASHED: BasicStroke =
            BasicStroke(Cap.ROUND, Join.BEVEL, 10f, floatArrayOf(10f, 10f), 1f)

        /** The dot line style.  */
        val DOTTED: BasicStroke = BasicStroke(Cap.ROUND, Join.BEVEL, 5f, floatArrayOf(2f, 10f), 1f)
    }
}
