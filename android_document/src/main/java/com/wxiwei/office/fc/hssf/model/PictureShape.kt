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
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFPicture
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape

/**
 * Represents a picture shape and creates all specific low level records.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class PictureShape
internal constructor(hssfShape: HSSFSimpleShape, shapeId: Int) : AbstractShape() {
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

    /**
     * Creates the lowerlevel escher records for this shape.
     */
    private fun createSpContainer(
        hssfShape: HSSFSimpleShape?,
        shapeId: Int
    ): EscherContainerRecord {
        val shape = hssfShape as HSSFPicture

        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        val anchor: EscherRecord?
        val clientData = EscherClientDataRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = ((ShapeTypes.PictureFrame shl 4) or 0x2).toShort()

        sp.shapeId = shapeId
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
        //        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x00800080 ) );
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.BLIP__BLIPTODISPLAY,
                false,
                true,
                shape.pictureIndex
            )
        )
        //        opt.addEscherProperty( new EscherComplexProperty( EscherProperties.BLIP__BLIPFILENAME, true, new byte[] { (byte)0x74, (byte)0x00, (byte)0x65, (byte)0x00, (byte)0x73, (byte)0x00, (byte)0x74, (byte)0x00, (byte)0x00, (byte)0x00 } ) );
//        opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.FILL__FILLTYPE, 0x00000003 ) );
        addStandardOptions(shape, opt)
        val userAnchor = shape.getAnchor()
        if (userAnchor!!.isHorizontallyFlipped) sp.flags = sp.flags or EscherSpRecord.FLAG_FLIPHORIZ
        if (userAnchor.isVerticallyFlipped) sp.flags = sp.flags or EscherSpRecord.FLAG_FLIPVERT
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
        c.reserved2 = 0x0
        val e = EndSubRecord()

        obj.addSubRecord(c)
        obj.addSubRecord(e)

        return obj
    }


}
