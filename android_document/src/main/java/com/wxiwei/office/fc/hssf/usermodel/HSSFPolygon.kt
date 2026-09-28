/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.ddf.EscherContainerRecord

/**
 * @author Glen Stampoultzis  (glens at superlinksoftware.com)
 */
class HSSFPolygon
internal constructor(
    escherContainer: EscherContainerRecord?,
    parent: HSSFShape?,
    anchor: HSSFAnchor?
) : HSSFShape(escherContainer, parent, anchor) {
    var xPoints: IntArray? = null
    var yPoints: IntArray? = null
    var drawAreaWidth: Int = 100
    var drawAreaHeight: Int = 100

    fun setPoints(xPoints: IntArray, yPoints: IntArray) {
        this.xPoints = cloneArray(xPoints)
        this.yPoints = cloneArray(yPoints)
    }

    private fun cloneArray(a: IntArray): IntArray {
        val result = IntArray(a.size)
        for (i in a.indices) result[i] = a[i]

        return result
    }

    /**
     * Defines the width and height of the points in the polygon
     * @param width
     * @param height
     */
    fun setPolygonDrawArea(width: Int, height: Int) {
        this.drawAreaWidth = width
        this.drawAreaHeight = height
    }
}
