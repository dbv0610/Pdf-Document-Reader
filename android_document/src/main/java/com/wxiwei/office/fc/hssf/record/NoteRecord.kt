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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE

/**
 * NOTE: Comment Associated with a Cell (0x001C)
 *
 *
 * 
 * @author Yegor Kozlov
 */
class NoteRecord : StandardRecord {
    private var field_1_row = 0
    private var field_2_col = 0
    private var field_3_flags: Short
    private var field_4_shapeid = 0
    private var field_5_hasMultibyte = false
    private var field_6_author: String? = null

    /**
     * Saves padding byte value to reduce delta during round-trip serialization.<br></br>
     * 
     * The documentation is not clear about how padding should work.  In any case
     * Excel(2007) does something different.
     */
    private var field_7_padding: Byte? = null

    /**
     * Construct a new `NoteRecord` and
     * fill its data with the default values
     */
    constructor() {
        field_6_author = ""
        field_3_flags = 0
        field_7_padding = DEFAULT_PADDING // seems to be always present regardless of author text
    }

    /**
     * @return id of this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Read the record data from the supplied `RecordInputStream`
     */
    constructor(`in`: RecordInputStream) {
        field_1_row = `in`.readUShort()
        field_2_col = `in`.readShort().toInt()
        field_3_flags = `in`.readShort()
        field_4_shapeid = `in`.readUShort()
        val length = `in`.readShort().toInt()
        field_5_hasMultibyte = `in`.readByte().toInt() != 0x00
        if (field_5_hasMultibyte) {
            field_6_author = readUnicodeLE(`in`, length)
        } else {
            field_6_author = readCompressedUnicode(`in`, length)
        }
        if (`in`.available() == 1) {
            field_7_padding = `in`.readByte()
        }
    }

    public override fun serialize(out: LittleEndianOutput) {
        out.writeShort(field_1_row)
        out.writeShort(field_2_col)
        out.writeShort(field_3_flags.toInt())
        out.writeShort(field_4_shapeid)
        out.writeShort(field_6_author!!.length)
        out.writeByte(if (field_5_hasMultibyte) 0x01 else 0x00)
        if (field_5_hasMultibyte) {
            StringUtil.putUnicodeLE(field_6_author!!, out)
        } else {
            StringUtil.putCompressedUnicode(field_6_author!!, out)
        }
        if (field_7_padding != null) {
            out.writeByte(field_7_padding!!.toInt())
        }
    }

    override fun getDataSize(): Int {
        return (11 // 5 shorts + 1 byte
                + field_6_author!!.length * (if (field_5_hasMultibyte) 2 else 1) + (if (field_7_padding == null) 0 else 1))
    }

    /**
     * Convert this record to string.
     * Used by BiffViewer and other utilities.
     */
    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[NOTE]\n")
        buffer.append("    .row    = ").append(field_1_row).append("\n")
        buffer.append("    .col    = ").append(field_2_col).append("\n")
        buffer.append("    .flags  = ").append(field_3_flags.toInt()).append("\n")
        buffer.append("    .shapeid= ").append(field_4_shapeid).append("\n")
        buffer.append("    .author = ").append(field_6_author).append("\n")
        buffer.append("[/NOTE]\n")
        return buffer.toString()
    }

    /**
     * Return the row that contains the comment
     * 
     * @return the row that contains the comment
     */
    fun getRow(): Int {
        return field_1_row
    }

    /**
     * Specify the row that contains the comment
     * 
     * @param row the row that contains the comment
     */
    fun setRow(row: Int) {
        field_1_row = row
    }

    /**
     * Return the column that contains the comment
     * 
     * @return the column that contains the comment
     */
    fun getColumn(): Int {
        return field_2_col
    }

    /**
     * Specify the column that contains the comment
     * 
     * @param col the column that contains the comment
     */
    fun setColumn(col: Int) {
        field_2_col = col
    }

    /**
     * Options flags.
     * 
     * @return the options flag
     * @see .NOTE_VISIBLE
     * 
     * @see .NOTE_HIDDEN
     */
    fun getFlags(): Short {
        return field_3_flags
    }

    /**
     * Options flag
     * 
     * @param flags the options flag
     * @see .NOTE_VISIBLE
     * 
     * @see .NOTE_HIDDEN
     */
    fun setFlags(flags: Short) {
        field_3_flags = flags
    }

    /**
     * For unit testing only!
     */
    protected fun authorIsMultibyte(): Boolean {
        return field_5_hasMultibyte
    }

    /**
     * Object id for OBJ record that contains the comment
     */
    fun getShapeId(): Int {
        return field_4_shapeid
    }

    /**
     * Object id for OBJ record that contains the comment
     */
    fun setShapeId(id: Int) {
        field_4_shapeid = id
    }

    /**
     * Name of the original comment author
     * 
     * @return the name of the original author of the comment
     */
    fun getAuthor(): String {
        return field_6_author!!
    }

    /**
     * Name of the original comment author
     * 
     * @param author the name of the original author of the comment
     */
    fun setAuthor(author: String) {
        field_6_author = author
        field_5_hasMultibyte = hasMultibyte(author)
    }

    override fun clone(): Any {
        val rec = NoteRecord()
        rec.field_1_row = field_1_row
        rec.field_2_col = field_2_col
        rec.field_3_flags = field_3_flags
        rec.field_4_shapeid = field_4_shapeid
        rec.field_6_author = field_6_author
        return rec
    }

    companion object {
        const val sid: Short = 0x001C

        @JvmField
        val EMPTY_ARRAY: Array<NoteRecord?> = arrayOf<NoteRecord?>()

        /**
         * Flag indicating that the comment is hidden (default)
         */
        const val NOTE_HIDDEN: Short = 0x0

        /**
         * Flag indicating that the comment is visible
         */
        const val NOTE_VISIBLE: Short = 0x2

        private val DEFAULT_PADDING = 0.toByte()
    }
}
