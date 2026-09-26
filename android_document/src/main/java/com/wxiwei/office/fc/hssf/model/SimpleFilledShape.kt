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
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape

class SimpleFilledShape
internal constructor(hssfShape: HSSFSimpleShape, shapeId: Int) : AbstractShape() {
    override val spContainer: EscherContainerRecord?
    override val objRecord: ObjRecord?

    /**
     * Creates the low evel records for an oval.
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
     * @param hssfShape
     * @param shapeId
     */
    private fun createSpContainer(hssfShape: HSSFSimpleShape, shapeId: Int): EscherContainerRecord {
        val shape: HSSFShape = hssfShape

        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        val clientData = EscherClientDataRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        val shapeType = objTypeToShapeType(hssfShape.shapeType)
        sp.options = ((shapeType.toInt() shl 4) or 0x2).toShort()
        sp.shapeId = shapeId
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
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

    private fun objTypeToShapeType(objType: Int): Short {
        val shapeType: Short
        if (objType == HSSFSimpleShape.OBJECT_TYPE_OVAL.toInt()) shapeType =
            ShapeTypes.Ellipse.toShort()
        else if (objType == HSSFSimpleShape.OBJECT_TYPE_RECTANGLE.toInt()) shapeType =
            ShapeTypes.Rectangle.toShort()
        else throw IllegalArgumentException("Unable to handle an object of this type")
        return shapeType
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private fun createObjRecord(hssfShape: HSSFShape, shapeId: Int): ObjRecord {
        val shape = hssfShape

        val obj = ObjRecord()
        val c = CommonObjectDataSubRecord()
        c.objectType = (shape as HSSFSimpleShape).shapeType.toShort()
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


}
