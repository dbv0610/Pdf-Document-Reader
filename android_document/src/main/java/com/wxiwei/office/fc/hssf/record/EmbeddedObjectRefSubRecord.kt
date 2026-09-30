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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.util.LittleEndianConsts
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtg
import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.byteToHex
import com.wxiwei.office.fc.util.HexDump.intToHex
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianInputStream
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.readCompressedUnicode
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE
import java.io.ByteArrayInputStream

/**
 * ftPictFmla (0x0009)<br></br>
 * A sub-record within the OBJ record which stores a reference to an object
 * stored in a separate entry within the OLE2 compound file.
 * 
 * @author Daniel Noll
 */
class EmbeddedObjectRefSubRecord : SubRecord {
    private var field_1_unknown_int = 0

    /** either an area or a cell ref  */
    private var field_2_refPtg: Ptg? = null

    /** for when the 'formula' doesn't parse properly  */
    private var field_2_unknownFormulaData: ByteArray?

    /** note- this byte is not present in the encoding if the string length is zero  */
    private var field_3_unicode_flag = false // Flags whether the string is Unicode.
    private var field_4_ole_classname: String? =
        null // Classname of the embedded OLE document (e.g. Word.Document.8)

    /** Formulas often have a single non-zero trailing byte.
     * This is in a similar position to he pre-streamId padding
     * It is unknown if the value is important (it seems to mirror a value a few bytes earlier)
     */
    private var field_4_unknownByte: Byte? = null
    private var field_5_stream_id: Int? = null // ID of the OLE stream containing the actual data.
    private val field_6_unknown: ByteArray


    // currently for testing only - needs review
    internal constructor() {
        field_2_unknownFormulaData = byteArrayOf(
            0x02,
            0x6C,
            0x6A,
            0x16,
            0x01,
        ) // just some sample data.  These values vary a lot
        field_6_unknown = EMPTY_BYTE_ARRAY
        field_4_ole_classname = null
    }

    fun getSid(): Short {
        return sid
    }

    constructor(`in`: LittleEndianInput, size: Int) {
        // Much guess-work going on here due to lack of any documentation.
        // See similar source code in OOO:
        // http://svn.services.openoffice.org/ooo/trunk/sc/source/filter/excel/xiescher.cxx
        // 1223 void XclImpOleObj::ReadPictFmla( XclImpStream& rStrm, sal_uInt16 nRecSize )

        val streamIdOffset = `in`.readShort().toInt() // OOO calls this 'nFmlaLen'
        var remaining: Int = size - LittleEndianConsts.SHORT_SIZE

        val dataLenAfterFormula = remaining - streamIdOffset
        val formulaSize = `in`.readUShort()
        remaining -= LittleEndianConsts.SHORT_SIZE
        field_1_unknown_int = `in`.readInt()
        remaining -= LittleEndianConsts.INT_SIZE
        val formulaRawBytes: ByteArray = readRawData(`in`, formulaSize)
        remaining -= formulaSize
        field_2_refPtg = readRefPtg(formulaRawBytes)
        if (field_2_refPtg == null) {
            // common case
            // field_2_n16 seems to be 5 here
            // The formula almost looks like tTbl but the row/column values seem like garbage.
            field_2_unknownFormulaData = formulaRawBytes
        } else {
            field_2_unknownFormulaData = null
        }

        var stringByteCount: Int
        if (remaining >= dataLenAfterFormula + 3) {
            val tag = `in`.readByte().toInt()
            stringByteCount = LittleEndianConsts.BYTE_SIZE
            if (tag != 0x03) {
                throw RecordFormatException("Expected byte 0x03 here")
            }
            val nChars = `in`.readUShort()
            stringByteCount += LittleEndianConsts.SHORT_SIZE
            if (nChars > 0) {
                // OOO: the 4th way Xcl stores a unicode string: not even a Grbit byte present if length 0
                field_3_unicode_flag = (`in`.readByte().toInt() and 0x01) != 0
                stringByteCount += LittleEndianConsts.BYTE_SIZE
                if (field_3_unicode_flag) {
                    field_4_ole_classname = readUnicodeLE(`in`, nChars)
                    stringByteCount += nChars * 2
                } else {
                    field_4_ole_classname = readCompressedUnicode(`in`, nChars)
                    stringByteCount += nChars
                }
            } else {
                field_4_ole_classname = ""
            }
        } else {
            field_4_ole_classname = null
            stringByteCount = 0
        }
        remaining -= stringByteCount
        // Pad to next 2-byte boundary
        if (((stringByteCount + formulaSize) % 2) != 0) {
            val b = `in`.readByte().toInt()
            remaining -= LittleEndianConsts.BYTE_SIZE
            if (field_2_refPtg != null && field_4_ole_classname == null) {
                field_4_unknownByte = b.toByte()
            }
        }
        val nUnexpectedPadding = remaining - dataLenAfterFormula

        if (nUnexpectedPadding > 0) {
            System.err.println("Discarding " + nUnexpectedPadding + " unexpected padding bytes ")
            readRawData(`in`, nUnexpectedPadding)
            remaining -= nUnexpectedPadding
        }

        // Fetch the stream ID
        if (dataLenAfterFormula >= 4) {
            field_5_stream_id = `in`.readInt()
            remaining -= LittleEndianConsts.INT_SIZE
        } else {
            field_5_stream_id = null
        }
        field_6_unknown = readRawData(`in`, remaining)
    }

    private fun getStreamIDOffset(formulaSize: Int): Int {
        var result = 2 + 4 // formulaSize + f2unknown_int
        result += formulaSize

        val stringLen: Int
        if (field_4_ole_classname == null) {
            // don't write 0x03, stringLen, flag, text
            stringLen = 0
        } else {
            result += 1 + 2 // 0x03, stringLen
            stringLen = field_4_ole_classname!!.length
            if (stringLen > 0) {
                result += 1 // flag
                if (field_3_unicode_flag) {
                    result += stringLen * 2
                } else {
                    result += stringLen
                }
            }
        }
        // pad to next 2 byte boundary
        if ((result % 2) != 0) {
            result++
        }
        return result
    }

    private fun getDataSize(idOffset: Int): Int {
        var result = 2 + idOffset // 2 for idOffset short field itself
        if (field_5_stream_id != null) {
            result += 4
        }
        return result + field_6_unknown.size
    }

    override fun getDataSize(): Int {
        val formulaSize =
            if (field_2_refPtg == null) field_2_unknownFormulaData!!.size else field_2_refPtg!!.size
        val idOffset = getStreamIDOffset(formulaSize)
        return getDataSize(idOffset)
    }

    override fun serialize(out: LittleEndianOutput) {
        val formulaSize =
            if (field_2_refPtg == null) field_2_unknownFormulaData!!.size else field_2_refPtg!!.size
        val idOffset = getStreamIDOffset(formulaSize)
        val dataSize = getDataSize(idOffset)


        out.writeShort(sid.toInt())
        out.writeShort(dataSize)

        out.writeShort(idOffset)
        out.writeShort(formulaSize)
        out.writeInt(field_1_unknown_int)

        var pos = 12

        if (field_2_refPtg == null) {
            out.write(field_2_unknownFormulaData!!)
        } else {
            field_2_refPtg!!.write(out)
        }
        pos += formulaSize

        val stringLen: Int
        if (field_4_ole_classname == null) {
            // don't write 0x03, stringLen, flag, text
            stringLen = 0
        } else {
            out.writeByte(0x03)
            pos += 1
            stringLen = field_4_ole_classname!!.length
            out.writeShort(stringLen)
            pos += 2
            if (stringLen > 0) {
                out.writeByte(if (field_3_unicode_flag) 0x01 else 0x00)
                pos += 1

                if (field_3_unicode_flag) {
                    StringUtil.putUnicodeLE(field_4_ole_classname!!, out)
                    pos += stringLen * 2
                } else {
                    StringUtil.putCompressedUnicode(field_4_ole_classname!!, out)
                    pos += stringLen
                }
            }
        }

        // pad to next 2-byte boundary (requires 0 or 1 bytes)
        when (idOffset - (pos - 6)) {
            1 -> {
                out.writeByte(if (field_4_unknownByte == null) 0x00 else field_4_unknownByte!!.toInt())
                pos++
            }

            0 -> {}
            else -> throw IllegalStateException("Bad padding calculation (" + idOffset + ", " + pos + ")")
        }

        if (field_5_stream_id != null) {
            out.writeInt(field_5_stream_id!!)
            pos += 4
        }
        out.write(field_6_unknown)
    }

    /**
     * Gets the stream ID containing the actual data.  The data itself
     * can be found under a top-level directory entry in the OLE2 filesystem
     * under the name "MBD<var>xxxxxxxx</var>" where <var>xxxxxxxx</var> is
     * this ID converted into hex (in big endian order, funnily enough.)
     * 
     * @return the data stream ID. Possibly `null`
     */
    fun getStreamId(): Int? {
        return field_5_stream_id
    }

    fun getOLEClassName(): String? {
        return field_4_ole_classname
    }

    fun getObjectData(): ByteArray {
        return field_6_unknown
    }

    override fun clone(): Any {
        return this // TODO proper clone
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("[ftPictFmla]\n")
        sb.append("    .f2unknown     = ").append(intToHex(field_1_unknown_int)).append("\n")
        if (field_2_refPtg == null) {
            sb.append("    .f3unknown     = ").append(HexDump.toHex(field_2_unknownFormulaData!!))
                .append("\n")
        } else {
            sb.append("    .formula       = ").append(field_2_refPtg.toString()).append("\n")
        }
        if (field_4_ole_classname != null) {
            sb.append("    .unicodeFlag   = ").append(field_3_unicode_flag).append("\n")
            sb.append("    .oleClassname  = ").append(field_4_ole_classname).append("\n")
        }
        if (field_4_unknownByte != null) {
            sb.append("    .f4unknown   = ").append(byteToHex(field_4_unknownByte!!.toInt()))
                .append("\n")
        }
        if (field_5_stream_id != null) {
            sb.append("    .streamId      = ").append(HexDump.intToHex(field_5_stream_id!!))
                .append("\n")
        }
        if (field_6_unknown.size > 0) {
            sb.append("    .f7unknown     = ").append(toHex(field_6_unknown)).append("\n")
        }
        sb.append("[/ftPictFmla]")
        return sb.toString()
    }

    companion object {
        const val sid: Short = 0x0009

        private val EMPTY_BYTE_ARRAY = byteArrayOf()

        private fun readRefPtg(formulaRawBytes: ByteArray?): Ptg? {
            val `in`: LittleEndianInput =
                LittleEndianInputStream(ByteArrayInputStream(formulaRawBytes))
            val ptgSid = `in`.readByte()
            when (ptgSid.toInt()) {
                AreaPtg.sid.toInt() -> return AreaPtg(`in`)
                Area3DPtg.sid.toInt() -> return Area3DPtg(`in`)
                RefPtg.sid.toInt() -> return RefPtg(`in`)
                Ref3DPtg.sid.toInt() -> return Ref3DPtg(`in`)
            }
            return null
        }

        private fun readRawData(`in`: LittleEndianInput, size: Int): ByteArray {
            require(size >= 0) { "Negative size (" + size + ")" }
            if (size == 0) {
                return EMPTY_BYTE_ARRAY
            }
            val result = ByteArray(size)
            `in`.readFully(result)
            return result
        }
    }
}
