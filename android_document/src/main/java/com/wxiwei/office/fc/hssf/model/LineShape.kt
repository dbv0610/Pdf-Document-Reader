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
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherShapePathProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape

/**
 * Represents a line shape and creates all the line specific low level records.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class LineShape internal constructor(hssfShape: HSSFSimpleShape, shapeId: Int) : AbstractShape() {
    /**
     * Creates the lowerlevel escher records for this shape.
     */
    private fun createSpContainer(hssfShape: HSSFSimpleShape, shapeId: Int): EscherContainerRecord {
        val shape: HSSFShape = hssfShape

        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        var anchor: EscherRecord? = EscherClientAnchorRecord()
        val clientData = EscherClientDataRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = ((ShapeTypes.Line shl 4) or 0x2).toShort()

        sp.shapeId = shapeId
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
        opt.addEscherProperty(
            EscherShapePathProperty(
                EscherProperties.GEOMETRY__SHAPEPATH,
                EscherShapePathProperty.COMPLEX
            )
        )
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.LINESTYLE__NOLINEDRAWDASH,
                1048592
            )
        )
        addStandardOptions(shape, opt)
        val userAnchor = shape.getAnchor()
        if (userAnchor!!.isHorizontallyFlipped) {
            sp.flags = sp.flags or EscherSpRecord.FLAG_FLIPHORIZ
        }
        if (userAnchor.isVerticallyFlipped) {
            sp.flags = sp.flags or EscherSpRecord.FLAG_FLIPVERT
        }
        anchor = createAnchor(userAnchor)
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



    override val spContainer: EscherContainerRecord?
    override val objRecord: ObjRecord?

    /**
     * Creates the line shape from the highlevel user shape.  All low level
     * records are created at this point.
     * 
     * @param hssfShape     The user model shape.
     * @param shapeId       The identifier to use for this shape.
     */
    init {
        spContainer = createSpContainer(hssfShape, shapeId)
        objRecord = createObjRecord(hssfShape, shapeId)
    }
}
