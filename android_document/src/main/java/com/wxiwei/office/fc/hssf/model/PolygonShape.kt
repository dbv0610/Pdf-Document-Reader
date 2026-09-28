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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ddf.EscherArrayProperty
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherShapePathProperty
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFPolygon
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.util.LittleEndian.putShort

class PolygonShape internal constructor(hssfShape: HSSFPolygon, shapeId: Int) : AbstractShape() {
    override val spContainer: EscherContainerRecord?
    override val objRecord: ObjRecord?

    /**
     * Creates the low evel records for an polygon.
     * 
     * @param hssfShape  The highlevel shape.
     * @param shapeId    The shape id to use for this shape.
     */
    init {
        spContainer = createSpContainer(hssfShape, shapeId)
        objRecord = createObjRecord(hssfShape, shapeId)
    }

    /**
     * Generates the shape records for this shape.
     * 
     */
    private fun createSpContainer(hssfShape: HSSFPolygon, shapeId: Int): EscherContainerRecord {
        val shape: HSSFShape = hssfShape

        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        val clientData = EscherClientDataRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = ((ShapeTypes.Donut shl 4) or 0x2).toShort()
        sp.shapeId = shapeId
        if (hssfShape.parent == null) sp.flags =
            EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        else sp.flags =
            EscherSpRecord.FLAG_CHILD or EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.TRANSFORM__ROTATION,
                false,
                false,
                0
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GEOMETRY__RIGHT,
                false,
                false,
                hssfShape.drawAreaWidth
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GEOMETRY__BOTTOM,
                false,
                false,
                hssfShape.drawAreaHeight
            )
        )
        opt.addEscherProperty(
            EscherShapePathProperty(
                EscherProperties.GEOMETRY__SHAPEPATH,
                EscherShapePathProperty.COMPLEX
            )
        )
        val verticesProp =
            EscherArrayProperty(EscherProperties.GEOMETRY__VERTICES, false, ByteArray(0))
        verticesProp.numberOfElementsInArray = hssfShape.xPoints!!.size + 1
        verticesProp.numberOfElementsInMemory = hssfShape.xPoints!!.size + 1
        verticesProp.setSizeOfElements(0xFFF0)
        for (i in hssfShape.xPoints!!.indices) {
            val data = ByteArray(4)
            putShort(data, 0, hssfShape.xPoints!![i].toShort())
            putShort(data, 2, hssfShape.yPoints!![i].toShort())
            verticesProp.setElement(i, data)
        }
        val point = hssfShape.xPoints!!.size
        val data = ByteArray(4)
        putShort(data, 0, hssfShape.xPoints!![0].toShort())
        putShort(data, 2, hssfShape.yPoints!![0].toShort())
        verticesProp.setElement(point, data)
        opt.addEscherProperty(verticesProp)
        val segmentsProp = EscherArrayProperty(EscherProperties.GEOMETRY__SEGMENTINFO, false, null)
        segmentsProp.setSizeOfElements(0x0002)
        segmentsProp.numberOfElementsInArray = hssfShape.xPoints!!.size * 2 + 4
        segmentsProp.numberOfElementsInMemory = hssfShape.xPoints!!.size * 2 + 4
        segmentsProp.setElement(0, byteArrayOf(0x00.toByte(), 0x40.toByte()))
        segmentsProp.setElement(1, byteArrayOf(0x00.toByte(), 0xAC.toByte()))
        for (i in hssfShape.xPoints!!.indices) {
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
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GEOMETRY__FILLOK,
                false,
                false,
                0x00010001
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.LINESTYLE__LINESTARTARROWHEAD,
                false,
                false,
                0x0
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.LINESTYLE__LINEENDARROWHEAD,
                false,
                false,
                0x0
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.LINESTYLE__LINEENDCAPSTYLE,
                false,
                false,
                0x0
            )
        )

        addStandardOptions(shape, opt)

        val anchor = createAnchor(shape.getAnchor())
        clientData.recordId = EscherClientDataRecord.RECORD_ID
        clientData.options = 0x0000.toShort()

        spContainer.addChildRecord(sp)
        spContainer.addChildRecord(opt)
        spContainer.addChildRecord(anchor)
        spContainer.addChildRecord(clientData)

        return spContainer
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private fun createObjRecord(hssfShape: HSSFShape?, shapeId: Int): ObjRecord {
        val shape = hssfShape

        val obj = ObjRecord()
        val c = CommonObjectDataSubRecord()
        c.objectType = OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING
        c.objectId = getCmoObjectId(shapeId)
        c.isLocked = true
        c.isPrintable = true
        c.isAutofill = true
        c.isAutoline = true
        val e = EndSubRecord()

        obj.addSubRecord(c)
        obj.addSubRecord(e)

        return obj
    }



    companion object {
        const val OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING: Short = 30
    }
}
