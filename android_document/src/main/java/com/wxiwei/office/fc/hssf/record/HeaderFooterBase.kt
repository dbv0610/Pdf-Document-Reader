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

/**
 * Common header/footer base class
 * 
 * @author Josh Micich
 */
abstract class HeaderFooterBase : StandardRecord {
    private var field_2_hasMultibyte = false
    private var field_3_text: String? = null

    protected constructor(text: String?) {
        setText(text ?: "")
    }

    protected constructor(`in`: RecordInputStream) {
        if (`in`.remaining() > 0) {
            val field_1_footer_len = `in`.readShort().toInt()
            field_2_hasMultibyte = `in`.readByte().toInt() != 0x00

            if (field_2_hasMultibyte) {
                field_3_text = `in`.readUnicodeLEString(field_1_footer_len)
            } else {
                field_3_text = `in`.readCompressedUnicode(field_1_footer_len)
            }
        } else {
            // Note - this is unusual for BIFF records in general, but normal for header / footer records:
            // when the text is empty string, the whole record is empty (just the 4 byte BIFF header)
            field_3_text = ""
        }
    }

    /**
     * set the footer string
     * 
     * @param text string to display
     */
    fun setText(text: String) {
        requireNotNull(text) { "text must not be null" }
        field_2_hasMultibyte = hasMultibyte(text)
        field_3_text = text

        // Check it'll fit into the space in the record
        require(getDataSize() <= RecordInputStream.Companion.MAX_RECORD_DATA_SIZE) {
            ("Header/Footer string too long (limit is "
                    + RecordInputStream.Companion.MAX_RECORD_DATA_SIZE + " bytes)")
        }
    }

    /**
     * get the length of the footer string
     * 
     * @return length of the footer string
     */
    private fun getTextLength(): Int {
        return field_3_text!!.length
    }

    fun getText(): String {
        return field_3_text!!
    }

    public override fun serialize(out: LittleEndianOutput) {
        if (getTextLength() > 0) {
            out.writeShort(getTextLength())
            out.writeByte(if (field_2_hasMultibyte) 0x01 else 0x00)
            if (field_2_hasMultibyte) {
                StringUtil.putUnicodeLE(field_3_text!!, out)
            } else {
                StringUtil.putCompressedUnicode(field_3_text!!, out)
            }
        }
    }

    override fun getDataSize(): Int {
        if (getTextLength() < 1) {
            return 0
        }
        return 3 + getTextLength() * (if (field_2_hasMultibyte) 2 else 1)
    }
}
