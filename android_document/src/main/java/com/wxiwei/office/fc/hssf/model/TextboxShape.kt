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
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherTextboxRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EndSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.TextObjectRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFTextbox

/**
 * Represents an textbox shape and converts between the highlevel records
 * and lowlevel records for an oval.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
open class TextboxShape
internal constructor(hssfShape: HSSFTextbox, shapeId: Int) : AbstractShape() {
    final override val spContainer: EscherContainerRecord?
    @JvmField
    val textObjectRecord: TextObjectRecord?
    final override val objRecord: ObjRecord?
    private var escherTextbox: EscherTextboxRecord? = null

    /**
     * Creates the low evel records for an textbox.
     * 
     * @param hssfShape  The highlevel shape.
     * @param shapeId    The shape id to use for this shape.
     */
    init {
        spContainer = createSpContainer(hssfShape, shapeId)
        objRecord = createObjRecord(hssfShape, shapeId)
        textObjectRecord = createTextObjectRecord(hssfShape, shapeId)
    }

    /**
     * Creates the low level OBJ record for this shape.
     */
    private fun createObjRecord(hssfShape: HSSFTextbox, shapeId: Int): ObjRecord {
        val shape: HSSFShape = hssfShape

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

    /**
     * Generates the escher shape records for this shape.
     * 
     * @param hssfShape
     * @param shapeId
     */
    private fun createSpContainer(hssfShape: HSSFTextbox, shapeId: Int): EscherContainerRecord {
        val shape = hssfShape

        val spContainer = EscherContainerRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        var anchor: EscherRecord? = EscherClientAnchorRecord()
        val clientData = EscherClientDataRecord()
        escherTextbox = EscherTextboxRecord()

        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = ((ShapeTypes.TextBox shl 4) or 0x2).toShort()

        sp.shapeId = shapeId
        sp.flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        opt.recordId = EscherOptRecord.RECORD_ID
        //        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 262144 ) );
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.TEXT__TEXTID, 0))
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.TEXT__TEXTLEFT,
                shape.marginLeft
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.TEXT__TEXTRIGHT,
                shape.marginRight
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.TEXT__TEXTBOTTOM,
                shape.marginBottom
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.TEXT__TEXTTOP,
                shape.marginTop
            )
        )

        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.TEXT__WRAPTEXT, 0))
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.TEXT__ANCHORTEXT, 0))
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00080000))

        addStandardOptions(shape, opt)
        val userAnchor = shape.getAnchor()
        //        if (userAnchor.isHorizontallyFlipped())
        //            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPHORIZ);
        //        if (userAnchor.isVerticallyFlipped())
        //            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_FLIPVERT);
        anchor = createAnchor(userAnchor)
        clientData.recordId = EscherClientDataRecord.RECORD_ID
        clientData.options = 0x0000.toShort()
        escherTextbox!!.recordId = EscherTextboxRecord.RECORD_ID
        escherTextbox!!.options = 0x0000.toShort()

        spContainer.addChildRecord(sp)
        spContainer.addChildRecord(opt)
        spContainer.addChildRecord(anchor)
        spContainer.addChildRecord(clientData)
        spContainer.addChildRecord(escherTextbox)

        return spContainer
    }

    /**
     * Textboxes also have an extra TXO record associated with them that most
     * other shapes dont have.
     */
    private fun createTextObjectRecord(hssfShape: HSSFTextbox, shapeId: Int): TextObjectRecord {
        val shape: HSSFTextbox? = hssfShape

        val obj = TextObjectRecord()
        obj.setHorizontalTextAlignment(hssfShape.horizontalAlignment.toInt())
        obj.setVerticalTextAlignment(hssfShape.verticalAlignment.toInt())
        obj.setTextLocked(true)
        obj.setTextOrientation(TextObjectRecord.TEXT_ORIENTATION_NONE.toInt())
        obj.setStr(shape!!.getString())

        return obj
    }



    fun getEscherTextbox(): EscherRecord {
        return escherTextbox!!
    }
}
