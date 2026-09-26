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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherArrayProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.java.awt.geom.Point2D

/**
 * A simple closed polygon shape
 * 
 * @author Yegor Kozlov
 */
class Polygon : AutoShape {
    /**
     * Create a Polygon object and initialize it from the supplied Record container.
     * 
     * @param escherRecord       `EscherSpContainer` container which holds information about this shape
     * @param parent    the parent of the shape
     */
    protected constructor(
        escherRecord: EscherContainerRecord?,
        parent: Shape?
    ) : super(escherRecord, parent)

    /**
     * Create a new Polygon. This constructor is used when a new shape is created.
     * 
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    /**
     * Create a new Polygon. This constructor is used when a new shape is created.
     * 
     */
    @JvmOverloads
    constructor(parent: Shape? = null) : super(null, parent) {
        spContainer = createSpContainer(ShapeTypes.NotPrimitive, parent is ShapeGroup)
    }

    /**
     * Set the polygon vertices
     * 
     * @param xPoints
     * @param yPoints
     */
    fun setPoints(xPoints: FloatArray, yPoints: FloatArray) {
        val right = findBiggest(xPoints)
        val bottom = findBiggest(yPoints)
        val left = findSmallest(xPoints)
        val top = findSmallest(yPoints)

        val opt = ShapeKit.getEscherChild(
            spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        opt!!.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GEOMETRY__RIGHT,
                ((right - left) * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GEOMETRY__BOTTOM,
                ((bottom - top) * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
            )
        )

        for (i in xPoints.indices) {
            xPoints[i] += -left
            yPoints[i] += -top
        }

        val numpoints = xPoints.size

        val verticesProp = EscherArrayProperty(
            EscherProperties.GEOMETRY__VERTICES, false, ByteArray(0)
        )
        verticesProp.numberOfElementsInArray = numpoints + 1
        verticesProp.numberOfElementsInMemory = numpoints + 1
        verticesProp.setSizeOfElements(0xFFF0)
        for (i in 0..<numpoints) {
            val data = ByteArray(4)
            putShort(
                data,
                0,
                (xPoints[i] * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt().toShort()
            )
            putShort(
                data,
                2,
                (yPoints[i] * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt().toShort()
            )
            verticesProp.setElement(i, data)
        }
        val data = ByteArray(4)
        putShort(
            data,
            0,
            (xPoints[0] * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt().toShort()
        )
        putShort(
            data,
            2,
            (yPoints[0] * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt().toShort()
        )
        verticesProp.setElement(numpoints, data)
        opt.addEscherProperty(verticesProp)

        val segmentsProp = EscherArrayProperty(
            EscherProperties.GEOMETRY__SEGMENTINFO, false, null
        )
        segmentsProp.setSizeOfElements(0x0002)
        segmentsProp.numberOfElementsInArray = numpoints * 2 + 4
        segmentsProp.numberOfElementsInMemory = numpoints * 2 + 4
        segmentsProp.setElement(0, byteArrayOf(0x00.toByte(), 0x40.toByte()))
        segmentsProp.setElement(1, byteArrayOf(0x00.toByte(), 0xAC.toByte()))
        for (i in 0..<numpoints) {
            segmentsProp.setElement(2 + i * 2, byteArrayOf(0x01.toByte(), 0x00.toByte()))
            segmentsProp.setElement(3 + i * 2, byteArrayOf(0x00.toByte(), 0xAC.toByte()))
        }
        segmentsProp.setElement(
            segmentsProp.numberOfElementsInArray - 2,
            byteArrayOf(0x01.toByte(), 0x60.toByte())
        )
        segmentsProp.setElement(
            segmentsProp.numberOfElementsInArray - 1,
            byteArrayOf(0x00.toByte(), 0x80.toByte())
        )
        opt.addEscherProperty(segmentsProp)

        opt.sortProperties()
    }

    /**
     * Set the polygon vertices
     * 
     * @param points the polygon vertices
     */
    fun setPoints(points: Array<Point2D?>) {
        val xpoints = FloatArray(points.size)
        val ypoints = FloatArray(points.size)
        for (i in points.indices) {
            xpoints[i] = points[i]!!.getX().toFloat()
            ypoints[i] = points[i]!!.getY().toFloat()
        }

        setPoints(xpoints, ypoints)
    }

    private fun findBiggest(values: FloatArray): Float {
        var result = Float.MIN_VALUE
        for (i in values.indices) {
            if (values[i] > result) result = values[i]
        }
        return result
    }

    private fun findSmallest(values: FloatArray): Float {
        var result = Float.MAX_VALUE
        for (i in values.indices) {
            if (values[i] < result) result = values[i]
        }
        return result
    }
}
