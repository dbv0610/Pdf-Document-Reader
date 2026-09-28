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

import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherProperty
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.NoteStructureSubRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFComment
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape

/**
 * Represents a cell comment.
 * This class converts highlevel model data from `HSSFComment`
 * to low-level records.
 * 
 * @author Yegor Kozlov
 */
class CommentShape(hssfShape: HSSFComment, shapeId: Int) : TextboxShape(hssfShape, shapeId) {
    /**
     * Return the `NoteRecord` holding the comment attributes
     * 
     * @return `NoteRecord` holding the comment attributes
     */
    val noteRecord: NoteRecord?

    /**
     * Creates the low-level records for a comment.
     * 
     * @param hssfShape  The highlevel shape.
     * @param shapeId    The shape id to use for this shape.
     */
    init {
        this.noteRecord = createNoteRecord(hssfShape, shapeId)

        val obj = objRecord!!
        val records = obj.getSubRecords()!!
        var cmoIdx = 0
        for (i in records.indices) {
            val r: Any? = records.get(i)

            if (r is CommonObjectDataSubRecord) {
                //modify autofill attribute inherited from <code>TextObjectRecord</code>
                val cmo = r
                cmo.isAutofill = false
                cmoIdx = i
            }
        }
        //add NoteStructure sub record
        //we don't know it's format, for now the record data is empty
        val u = NoteStructureSubRecord()
        obj.addSubRecord(cmoIdx + 1, u)
    }

    /**
     * Creates the low level `NoteRecord`
     * which holds the comment attributes.
     */
    private fun createNoteRecord(shape: HSSFComment, shapeId: Int): NoteRecord {
        val note = NoteRecord()
        note.setColumn(shape.getColumn())
        note.setRow(shape.getRow())
        note.setFlags(if (shape.isVisible()) NoteRecord.NOTE_VISIBLE else NoteRecord.NOTE_HIDDEN)
        note.setShapeId(shapeId)
        note.setAuthor(shape.getAuthor() ?: "")
        return note
    }

    /**
     * Sets standard escher options for a comment.
     * This method is responsible for setting default background,
     * shading and other comment properties.
     * 
     * @param shape   The highlevel shape.
     * @param opt     The escher records holding the proerties
     * @return number of escher options added
     */
    override fun addStandardOptions(shape: HSSFShape, opt: EscherOptRecord): Int {
        super.addStandardOptions(shape, opt)

        //remove unnecessary properties inherited from TextboxShape
        val props: MutableList<EscherProperty> = opt.escherProperties
        val iterator: MutableIterator<EscherProperty> = props.iterator()
        while (iterator.hasNext()) {
            val prop = iterator.next()
            when (prop.id) {
                EscherProperties.TEXT__TEXTLEFT, EscherProperties.TEXT__TEXTRIGHT, EscherProperties.TEXT__TEXTTOP, EscherProperties.TEXT__TEXTBOTTOM, EscherProperties.GROUPSHAPE__PRINT, EscherProperties.FILL__FILLBACKCOLOR, EscherProperties.LINESTYLE__COLOR -> iterator.remove()
            }
        }

        val comment = shape as HSSFComment
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.GROUPSHAPE__PRINT,
                if (comment.isVisible()) 0x000A0000 else 0x000A0002
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.SHADOWSTYLE__SHADOWOBSURED,
                0x00030003
            )
        )
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.SHADOWSTYLE__COLOR, 0x00000000))
        opt.sortProperties()
        return opt.escherProperties.size // # options added
    }

    override fun getCmoObjectId(shapeId: Int): Int {
        return shapeId
    }
}
