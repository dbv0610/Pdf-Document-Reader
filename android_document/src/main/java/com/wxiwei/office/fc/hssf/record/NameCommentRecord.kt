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

import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.shortToHex
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode

/**
 * Title: NAMECMT Record (0x0894)
 * 
 * 
 * Description: Defines a comment associated with a specified name.
 * <P>
 * REFERENCE:
</P> * <P>
 * 
 * @author Andrew Shirley (aks at corefiling.co.uk)
</P> */
class NameCommentRecord : StandardRecord {
    private val field_1_record_type: Short
    private val field_2_frt_cell_ref_flag: Short
    private val field_3_reserved: Long

    //private short             field_4_name_length;
    //private short             field_5_comment_length;
    private var field_6_name_text: String
    private var field_7_comment_text: String

    constructor(name: String, comment: String) {
        field_1_record_type = 0
        field_2_frt_cell_ref_flag = 0
        field_3_reserved = 0
        field_6_name_text = name
        field_7_comment_text = comment
    }

    public override fun serialize(out: LittleEndianOutput) {
        val field_4_name_length = field_6_name_text.length
        val field_5_comment_length = field_7_comment_text.length

        out.writeShort(field_1_record_type.toInt())
        out.writeShort(field_2_frt_cell_ref_flag.toInt())
        out.writeLong(field_3_reserved)
        out.writeShort(field_4_name_length)
        out.writeShort(field_5_comment_length)

        out.writeByte(0)
        putCompressedUnicode(field_6_name_text, out)
        out.writeByte(0)
        putCompressedUnicode(field_7_comment_text, out)
    }

    override fun getDataSize(): Int {
        return (18 // 4 shorts + 1 long + 2 spurious 'nul's
                + field_6_name_text.length
                + field_7_comment_text.length)
    }

    /**
     * @param ris the RecordInputstream to read the record from
     */
    constructor(ris: RecordInputStream) {
        val `in`: LittleEndianInput = ris
        field_1_record_type = `in`.readShort()
        field_2_frt_cell_ref_flag = `in`.readShort()
        field_3_reserved = `in`.readLong()
        val field_4_name_length = `in`.readShort().toInt()
        val field_5_comment_length = `in`.readShort().toInt()

        `in`.readByte() //spurious NUL
        field_6_name_text = readCompressedUnicode(`in`, field_4_name_length)
        `in`.readByte() //spurious NUL
        field_7_comment_text = readCompressedUnicode(`in`, field_5_comment_length)
    }

    /**
     * return the non static version of the id for this record.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[NAMECMT]\n")
        sb.append("    .record type            = ").append(shortToHex(field_1_record_type.toInt()))
            .append("\n")
        sb.append("    .frt cell ref flag      = ")
            .append(byteToHex(field_2_frt_cell_ref_flag.toInt())).append("\n")
        sb.append("    .reserved               = ").append(field_3_reserved).append("\n")
        sb.append("    .name length            = ").append(field_6_name_text.length).append("\n")
        sb.append("    .comment length         = ").append(field_7_comment_text.length).append("\n")
        sb.append("    .name                   = ").append(field_6_name_text).append("\n")
        sb.append("    .comment                = ").append(field_7_comment_text).append("\n")
        sb.append("[/NAMECMT]\n")

        return sb.toString()
    }

    @get:JvmName("getNameTextProperty")
    @set:JvmName("setNameTextProperty")
    var nameText: String
        get() = field_6_name_text
        set(newName) {
            field_6_name_text = newName
        }

    fun getNameText(): String {
        return field_6_name_text
    }

    /**
     * Updates the name we're associated with, normally used
     * when renaming that Name
     */
    fun setNameText(newName: String) {
        field_6_name_text = newName
    }

    @get:JvmName("getCommentTextProperty")
    @set:JvmName("setCommentTextProperty")
    var commentText: String
        get() = field_7_comment_text
        set(comment) {
            field_7_comment_text = comment
        }

    /**
     * @return the text of the comment.
     */
    fun getCommentText(): String {
        return field_7_comment_text
    }

    fun setCommentText(comment: String) {
        field_7_comment_text = comment
    }

    fun getRecordType(): Short {
        return field_1_record_type
    }

    companion object {
        const val sid: Short = 0x0894
    }
}
