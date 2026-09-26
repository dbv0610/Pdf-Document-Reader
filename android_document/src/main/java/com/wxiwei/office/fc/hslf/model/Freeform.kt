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

import android.graphics.Path
import android.graphics.PointF
import com.wxiwei.office.common.autoshape.pathbuilder.ArrowPathAndTail
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherArrayProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.java.awt.geom.PathIterator
import com.wxiwei.office.java.awt.geom.Point2D
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * A "Freeform" shape.
 * 
 * 
 * 
 * Shapes drawn with the "Freeform" tool have cubic bezier curve segments in the smooth sections
 * and straight-line segments in the straight sections. This object closely corresponds to `java.awt.geom.GeneralPath`.
 * 
 * @author Yegor Kozlov
 */
class Freeform : AutoShape {
    /**
     * Create a Freeform object and initialize it from the supplied Record container.
     * 
     * @param escherRecord       `EscherSpContainer` container which holds information about this shape
     * @param parent    the parent of the shape
     */
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    /**
     * Gets the freeform path
     * @param rect
     * @param startArrowTailCenter
     * @param endArrowTailCenter
     * @return
     */
    fun getFreeformPath(
        rect: Rectangle?,
        startArrowTailCenter: PointF?,
        startArrowType: Byte,
        endArrowTailCenter: PointF?,
        endArrowType: Byte
    ): Array<Path?>? {
        return ShapeKit.getFreeformPath(
            spContainer,
            rect,
            startArrowTailCenter,
            startArrowType,
            endArrowTailCenter,
            endArrowType
        )
    }


    /** */
    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     * 
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    /**
     * Create a new Freeform. This constructor is used when a new shape is created.
     * 
     */
    @JvmOverloads
    constructor(parent: Shape? = null) : super(null, parent) {
        spContainer = createSpContainer(ShapeTypes.NotPrimitive, parent is ShapeGroup)
    }

    var path: GeneralPath?
        /**
         * Gets the freeform path
         * 
         * @return the freeform path
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            opt!!.addEscherProperty(EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 0x4))

            var verticesProp = ShapeKit.getEscherProperty(
                opt,
                (EscherProperties.GEOMETRY__VERTICES + 0x4000).toShort().toInt()
            ) as EscherArrayProperty?
            if (verticesProp == null) verticesProp = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.GEOMETRY__VERTICES.toInt()
            ) as EscherArrayProperty?

            var segmentsProp = ShapeKit.getEscherProperty(
                opt,
                (EscherProperties.GEOMETRY__SEGMENTINFO + 0x4000).toShort().toInt()
            ) as EscherArrayProperty?
            if (segmentsProp == null) segmentsProp = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.GEOMETRY__SEGMENTINFO.toInt()
            ) as EscherArrayProperty?

            //sanity check
            if (verticesProp == null) {
                return null
            }
            if (segmentsProp == null) {
                return null
            }

            val path =
                GeneralPath()
            val numPoints = verticesProp.numberOfElementsInArray
            val numSegments = segmentsProp.numberOfElementsInArray
            var i = 0
            var j = 0
            while (i < numSegments && j < numPoints) {
                val elem = segmentsProp.getElement(i)
                if (elem.contentEquals(ShapeKit.SEGMENTINFO_MOVETO)) {
                    val p = verticesProp.getElement(j++)
                    val x = getShort(p, 0)
                    val y = getShort(p, 2)
                    path.moveTo(
                        (x.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (y.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI)
                    )
                } else if (elem.contentEquals(ShapeKit.SEGMENTINFO_CUBICTO) || elem.contentEquals(
                        ShapeKit.SEGMENTINFO_CUBICTO2
                    )
                ) {
                    i++
                    val p1 = verticesProp.getElement(j++)
                    val x1 = getShort(p1, 0)
                    val y1 = getShort(p1, 2)
                    val p2 = verticesProp.getElement(j++)
                    val x2 = getShort(p2, 0)
                    val y2 = getShort(p2, 2)
                    val p3 = verticesProp.getElement(j++)
                    val x3 = getShort(p3, 0)
                    val y3 = getShort(p3, 2)
                    path.curveTo(
                        (x1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (y1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (x2.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (y2.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (x3.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                        (y3.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI)
                    )
                } else if (elem.contentEquals(ShapeKit.SEGMENTINFO_LINETO)) {
                    i++
                    val pnext = segmentsProp.getElement(i)
                    if (pnext.contentEquals(ShapeKit.SEGMENTINFO_ESCAPE)) {
                        if (j + 1 < numPoints) {
                            val p = verticesProp.getElement(j++)
                            val x =
                                getShort(p, 0)
                            val y =
                                getShort(p, 2)
                            path.lineTo(
                                (x.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI),
                                (y.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI)
                            )
                        }
                    } else if (pnext.contentEquals(ShapeKit.SEGMENTINFO_CLOSE)) {
                        path.closePath()
                    }
                }
                i++
            }
            return path
        }
        /**
         * Set the shape path
         * 
         * @param path
         */
        set(path) {
            val bounds: Rectangle2D = path!!.getBounds2D()
            val it: PathIterator =
                path.getPathIterator(AffineTransform())

            val segInfo: MutableList<ByteArray> =
                ArrayList<ByteArray>()
            val pntInfo: MutableList<Point2D.Double> =
                ArrayList<Point2D.Double>()
            var isClosed = false
            while (!it.isDone()) {
                val vals = DoubleArray(6)
                val type = it.currentSegment(vals)
                when (type) {
                    PathIterator.SEG_MOVETO -> {
                        pntInfo.add(
                            Point2D.Double(
                                vals[0],
                                vals[1]
                            )
                        )
                        segInfo.add(ShapeKit.SEGMENTINFO_MOVETO)
                    }

                    PathIterator.SEG_LINETO -> {
                        pntInfo.add(
                            Point2D.Double(
                                vals[0],
                                vals[1]
                            )
                        )
                        segInfo.add(ShapeKit.SEGMENTINFO_LINETO)
                        segInfo.add(ShapeKit.SEGMENTINFO_ESCAPE)
                    }

                    PathIterator.SEG_CUBICTO -> {
                        pntInfo.add(
                            Point2D.Double(
                                vals[0],
                                vals[1]
                            )
                        )
                        pntInfo.add(
                            Point2D.Double(
                                vals[2],
                                vals[3]
                            )
                        )
                        pntInfo.add(
                            Point2D.Double(
                                vals[4],
                                vals[5]
                            )
                        )
                        segInfo.add(ShapeKit.SEGMENTINFO_CUBICTO)
                        segInfo.add(ShapeKit.SEGMENTINFO_ESCAPE2)
                    }

                    PathIterator.SEG_QUADTO -> {}
                    PathIterator.SEG_CLOSE -> {
                        pntInfo.add(pntInfo.get(0))
                        segInfo.add(ShapeKit.SEGMENTINFO_LINETO)
                        segInfo.add(ShapeKit.SEGMENTINFO_ESCAPE)
                        segInfo.add(ShapeKit.SEGMENTINFO_LINETO)
                        segInfo.add(ShapeKit.SEGMENTINFO_CLOSE)
                        isClosed = true
                    }
                }

                it.next()
            }
            if (!isClosed) segInfo.add(ShapeKit.SEGMENTINFO_LINETO)
            segInfo.add(byteArrayOf(0x00, 0x80.toByte()))

            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            opt!!.addEscherProperty(EscherSimpleProperty(EscherProperties.GEOMETRY__SHAPEPATH, 0x4))

            val verticesProp = EscherArrayProperty(
                (EscherProperties.GEOMETRY__VERTICES + 0x4000).toShort(), false, null
            )
            verticesProp.numberOfElementsInArray = pntInfo.size
            verticesProp.numberOfElementsInMemory = pntInfo.size
            verticesProp.setSizeOfElements(0xFFF0)
            for (i in pntInfo.indices) {
                val pnt = pntInfo.get(i)
                val data = ByteArray(4)
                putShort(
                    data, 0,
                    ((pnt.getX() - bounds.getX()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                        .toShort()
                )
                putShort(
                    data, 2,
                    ((pnt.getY() - bounds.getY()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                        .toShort()
                )
                verticesProp.setElement(i, data)
            }
            opt.addEscherProperty(verticesProp)

            val segmentsProp = EscherArrayProperty(
                (EscherProperties.GEOMETRY__SEGMENTINFO + 0x4000).toShort(), false, null
            )
            segmentsProp.numberOfElementsInArray = segInfo.size
            segmentsProp.numberOfElementsInMemory = segInfo.size
            segmentsProp.setSizeOfElements(0x2)
            for (i in segInfo.indices) {
                val seg = segInfo.get(i)
                segmentsProp.setElement(i, seg)
            }
            opt.addEscherProperty(segmentsProp)

            opt.addEscherProperty(
                EscherSimpleProperty(
                    EscherProperties.GEOMETRY__RIGHT,
                    (bounds.getWidth() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                )
            )
            opt.addEscherProperty(
                EscherSimpleProperty(
                    EscherProperties.GEOMETRY__BOTTOM,
                    (bounds.getHeight() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                )
            )

            opt.sortProperties()

            setAnchor(bounds)
        }

    override val outline: com.wxiwei.office.java.awt.Shape
        get() {
            val path = this.path
            val anchor = anchor2D
            val bounds = path!!.getBounds2D()
            val at = AffineTransform()
            at.translate(anchor.getX(), anchor.getY())
            at.scale(anchor.getWidth() / bounds.getWidth(), anchor.getHeight() / bounds.getHeight())
            return at.createTransformedShape(path)!!
        }

    fun getStartArrowPathAndTail(rect: Rectangle?): ArrowPathAndTail? {
        return ShapeKit.getStartArrowPathAndTail(spContainer, rect)
    }

    fun getEndArrowPathAndTail(rect: Rectangle?): ArrowPathAndTail? {
        return ShapeKit.getEndArrowPathAndTail(spContainer, rect)
    }

    override fun dispose() {
        super.dispose()
    }

    companion object {
        val SEGMENTINFO_MOVETO: ByteArray = byteArrayOf(0x00, 0x40)
        val SEGMENTINFO_LINETO: ByteArray = byteArrayOf(0x00, 0xAC.toByte())
        val SEGMENTINFO_LINETO2: ByteArray = byteArrayOf(0x00, 0xB0.toByte())
        val SEGMENTINFO_ESCAPE: ByteArray = byteArrayOf(0x01, 0x00)
        val SEGMENTINFO_ESCAPE1: ByteArray = byteArrayOf(0x03, 0x00)
        val SEGMENTINFO_ESCAPE2: ByteArray = byteArrayOf(0x01, 0x20)
        val SEGMENTINFO_CUBICTO: ByteArray = byteArrayOf(0x00, 0xAD.toByte())
        val SEGMENTINFO_CUBICTO1: ByteArray = byteArrayOf(0x00, 0xAF.toByte())
        val SEGMENTINFO_CUBICTO2: ByteArray =
            byteArrayOf(0x00, 0xB3.toByte()) //OpenOffice inserts 0xB3 instead of 0xAD.
        val SEGMENTINFO_CLOSE: ByteArray = byteArrayOf(0x01, 0x60.toByte())
        val SEGMENTINFO_END: ByteArray = byteArrayOf(0x00, 0x80.toByte())
    }
}
