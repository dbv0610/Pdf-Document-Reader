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
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.FtCblsSubRecord
import com.wxiwei.office.fc.hssf.record.LbsDataSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFClientAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape

/**
 * Represents a combobox shape.
 * 
 * @author Yegor Kozlov
 */
class ComboboxShape
internal constructor(hssfShape: HSSFSimpleShape, shapeId: Int) : AbstractShape() {
    override val spContainer: EscherContainerRecord?
    override val objRecord: ObjRecord?

    /**
     * Creates the low evel records for a combobox.
     * 
     * @param hssfShape The highlevel shape.
     * @param shapeId   The shape id to use for this shape.
     */
    init {
        spContainer = createSpContainer(hssfShape, shapeId)
        objRecord = createObjRecord(hssfShape, shapeId)
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private fun createObjRecord(shape: HSSFSimpleShape?, shapeId: Int): ObjRecord {
        val obj = ObjRecord()
        val c = CommonObjectDataSubRecord()
        c.objectType = HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX
        c.objectId = getCmoObjectId(shapeId)
        c.isLocked = true
        c.isPrintable = false
        c.isAutofill = true
        c.isAutoline = false
        val f = FtCblsSubRecord()

        val l = LbsDataSubRecord.newAutoFilterInstance()

        val e = EndSubRecord()

        obj.addSubRecord(c)
        obj.addSubRecord(f)
        obj.addSubRecord(l)
        obj.addSubRecord(e)

        return obj
    }

    /**
     * Generates the escher shape records for this shape.
     */
    private fun createSpContainer(shape: HSSFSimpleShape, shapeId: Int): EscherContainerRecord {
        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        val clientData = EscherClientDataRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = ((ShapeTypes.HostControl shl 4) or 0x2).toShort()

        sp.shapeId = shapeId
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.PROTECTION__LOCKAGAINSTGROUPING,
                17039620
            )
        )
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE,
                0x00080008
            )
        )
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.LINESTYLE__NOLINEDRAWDASH,
                0x00080000
            )
        )
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00020000))

        val userAnchor = shape.getAnchor() as HSSFClientAnchor
        userAnchor.anchorType = 1
        val anchor = createAnchor(userAnchor)
        clientData.recordId = EscherClientDataRecord.RECORD_ID
        clientData.options = 0x0000.toShort()

        spContainer.addChildRecord(sp)
        spContainer.addChildRecord(opt)
        spContainer.addChildRecord(anchor)
        spContainer.addChildRecord(clientData)

        return spContainer
    }


}
