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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.hslf.model.textproperties.AlignmentTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.CharFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.ParagraphFlagsTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextPropCollection
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianConsts
import java.io.IOException
import java.io.OutputStream

/**
 * TxMasterStyleAtom atom (4003).
 * 
 * 
 * Stores default character and paragraph styles.
 * The atom instance value is the text type and is encoded like the txstyle field in
 * TextHeaderAtom. The text styles are located in the MainMaster container,
 * except for the "other" style, which is in the Document.Environment container.
 * 
 * 
 * 
 * This atom can store up to 5 pairs of paragraph+character styles,
 * each pair describes an indent level. The first pair describes
 * first-level paragraph with no indentation.
 * 
 * 
 * @author Yegor Kozlov
 */
class TxMasterStyleAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    RecordAtom() {
    /**
     * We are of type 4003
     * 
     * @return type of this record
     * @see RecordTypes.TxMasterStyleAtom
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // Write out the (new) header
        out.write(_header)

        // Write out the record data
        out.write(_data)
    }

    val textType: Int
        /**
         * Return type of the text.
         * Must be a constant defined in `TextHeaderAtom`
         * 
         * @return type of the text
         * @see TextHeaderAtom
         */
        get() =//The atom instance value is the text type
            LittleEndian.getShort(_header!!, 0).toInt() shr 4

    /**
     * parse the record data and initialize styles
     */
    protected fun init() {
        //type of the text
        val type = this.textType

        var mask: Int
        var pos = 0

        //number of indentation levels
        val levels = LittleEndian.getShort(_data!!, 0)
        pos += LittleEndianConsts.SHORT_SIZE

        val paraStyles = arrayOfNulls<TextPropCollection>(levels.toInt())
        val charStyles = arrayOfNulls<TextPropCollection>(levels.toInt())
        this.paragraphStyles = paraStyles
        this.characterStyles = charStyles

        for (j in 0..<levels) {
            if (type >= TextHeaderAtom.Companion.CENTRE_BODY_TYPE) {
                // Fetch the 2 byte value, that is safe to ignore for some types of text
                val `val` = LittleEndian.getShort(_data!!, pos)
                pos += LittleEndianConsts.SHORT_SIZE
            }

            mask = LittleEndian.getInt(_data!!, pos)
            pos += LittleEndianConsts.INT_SIZE
            val prprops = TextPropCollection(0)
            pos += prprops.buildTextPropList(mask, getParagraphProps(type, j.toInt()), _data!!, pos)
            paraStyles[j.toInt()] = prprops

            mask = LittleEndian.getInt(_data!!, pos)
            pos += LittleEndianConsts.INT_SIZE
            val chprops = TextPropCollection(0)
            pos += chprops.buildTextPropList(mask, getCharacterProps(type, j.toInt()), _data!!, pos)
            charStyles[j.toInt()] = chprops
        }
    }

    /**
     * Paragraph properties for the specified text type and
     * indent level
     * Depending on the level and type, it may be our special
     * ones, or the standard StyleTextPropAtom ones
     */
    protected fun getParagraphProps(type: Int, level: Int): Array<TextProp?> {
        if (level != 0 || type >= MAX_INDENT) {
            //return StyleTextPropAtom.paragraphTextPropTypes;
            return arrayOf<TextProp?>(
                TextProp(0, 0x1, "hasBullet"), TextProp(0, 0x2, "hasBulletFont"),
                TextProp(0, 0x4, "hasBulletColor"), TextProp(0, 0x8, "hasBulletSize"),
                ParagraphFlagsTextProp(), TextProp(2, 0x80, "bullet.char"),
                TextProp(2, 0x10, "bullet.font"), TextProp(2, 0x40, "bullet.size"),
                TextProp(4, 0x20, "bullet.color"), AlignmentTextProp(),
                TextProp(2, 0x1000, "linespacing"), TextProp(2, 0x2000, "spacebefore"),
                TextProp(2, 0x100, "text.offset"), TextProp(2, 0x400, "bullet.offset"),
                TextProp(2, 0x4000, "spaceafter"), TextProp(2, 0x8000, "defaultTabSize"),
                TextProp(2, 0x100000, "tabStops"), TextProp(2, 0x10000, "fontAlign"),
                TextProp(2, 0xE0000, "wrapFlags"), TextProp(2, 0x200000, "textDirection"),
                TextProp(2, 0x1000000, "buletScheme"), TextProp(2, 0x2000000, "bulletHasScheme")
            )
        }
        return arrayOf<TextProp?>(
            ParagraphFlagsTextProp(), TextProp(2, 0x80, "bullet.char"),
            TextProp(2, 0x10, "bullet.font"), TextProp(2, 0x40, "bullet.size"),
            TextProp(4, 0x20, "bullet.color"), TextProp(2, 0xD00, "alignment"),
            TextProp(2, 0x1000, "linespacing"), TextProp(2, 0x2000, "spacebefore"),
            TextProp(2, 0x4000, "spaceafter"), TextProp(2, 0x8000, "text.offset"),
            TextProp(2, 0x10000, "bullet.offset"), TextProp(2, 0x20000, "defaulttab"),
            TextProp(2, 0x40000, "tabStops"), TextProp(2, 0x80000, "fontAlign"),
            TextProp(2, 0x100000, "para_unknown_1"), TextProp(2, 0x200000, "para_unknown_2"),
        )
    }

    /**
     * Character properties for the specified text type and
     * indent level.
     * Depending on the level and type, it may be our special
     * ones, or the standard StyleTextPropAtom ones
     */
    protected fun getCharacterProps(type: Int, level: Int): Array<TextProp?> {
        if (level != 0 || type >= MAX_INDENT) {
            return StyleTextPropAtom.Companion.characterTextPropTypes
        }
        return arrayOf<TextProp?>(
            CharFlagsTextProp(), TextProp(2, 0x10000, "font.index"),
            TextProp(2, 0x20000, "char_unknown_1"), TextProp(4, 0x40000, "char_unknown_2"),
            TextProp(2, 0x80000, "font.size"), TextProp(2, 0x100000, "char_unknown_3"),
            TextProp(4, 0x200000, "font.color"), TextProp(2, 0x800000, "char_unknown_4")
        )
    }


    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
        if (this.paragraphStyles != null) {
            for (ptc in this.paragraphStyles!!) {
                ptc?.dispose()
            }
            this.paragraphStyles = null
        }
        if (this.characterStyles != null) {
            for (ptc in this.characterStyles!!) {
                ptc?.dispose()
            }
            this.characterStyles = null
        }
    }

    private var _header: ByteArray?
    private var _data: ByteArray?

    /**
     * Returns array of paragraph styles defined in this record.
     * 
     * @return paragraph styles defined in this record
     */
    var paragraphStyles: Array<TextPropCollection?>? = null
        private set

    /**
     * Returns array of character styles defined in this record.
     * 
     * @return character styles defined in this record
     */
    var characterStyles: Array<TextPropCollection?>? = null
        private set

    init {
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, _data!!.size)

        //read available styles
        try {
            init()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Maximum number of indentatio levels allowed in PowerPoint documents
         */
        private const val MAX_INDENT = 5

        private const val _type: Long = 4003
    }
}
