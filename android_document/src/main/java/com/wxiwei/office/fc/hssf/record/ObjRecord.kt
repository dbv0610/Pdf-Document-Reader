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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianInputStream
import java.io.ByteArrayInputStream

/**
 * OBJRECORD (0x005D)
 *
 *
 * 
 * The obj record is used to hold various graphic objects and controls.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class ObjRecord : Record {
    private var subrecords: MutableList<SubRecord>?

    /** used when POI has no idea what is going on  */
    private val _uninterpretedData: ByteArray?

    /**
     * Excel seems to tolerate padding to quad or double byte length
     */
    private var _isPaddedToQuadByteMultiple = false


    //00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
    //00000010 26 01 00 00 00 00 00 00 00 00                   &.........
    constructor() {
        subrecords = ArrayList<SubRecord>(2)
        // TODO - ensure 2 sub-records (ftCmo 15h, and ftEnd 00h) are always created
        _uninterpretedData = null
    }

    constructor(`in`: RecordInputStream) {
        // TODO - problems with OBJ sub-records stream
        // MS spec says first sub-record is always CommonObjectDataSubRecord,
        // and last is
        // always EndSubRecord. OOO spec does not mention ObjRecord(0x005D).
        // Existing POI test data seems to violate that rule. Some test data
        // seems to contain
        // garbage, and a crash is only averted by stopping at what looks like
        // the 'EndSubRecord'

        // Check if this can be continued, if so then the
        // following wont work properly

        val subRecordData = `in`.readRemainder()
        if (getUShort(subRecordData, 0) != CommonObjectDataSubRecord.Companion.sid.toInt()) {
            // seems to occur in just one junit on "OddStyleRecord.xls" (file created by CrystalReports)
            // Excel tolerates the funny ObjRecord, and replaces it with a corrected version
            // The exact logic/reasoning is not yet understood
            _uninterpretedData = subRecordData
            subrecords = null
            return
        }

        //YK: files produced by OO violate the condition below
        /*
        if (subRecordData.length % 2 != 0) {
			String msg = "Unexpected length of subRecordData : " + HexDump.toHex(subRecordData);
			throw new RecordFormatException(msg);
		}
        */
        subrecords = ArrayList<SubRecord>()
        val bais = ByteArrayInputStream(subRecordData)
        val subRecStream = LittleEndianInputStream(bais)
        val cmo = SubRecord.Companion.createSubRecord(subRecStream, 0) as CommonObjectDataSubRecord
        subrecords!!.add(cmo)
        while (true) {
            val subRecord: SubRecord =
                SubRecord.Companion.createSubRecord(subRecStream, cmo.objectType.toInt())
            subrecords!!.add(subRecord)
            if (subRecord.isTerminating()) {
                break
            }
        }
        val nRemainingBytes = bais.available()
        if (nRemainingBytes > 0) {
            // At present (Oct-2008), most unit test samples have (subRecordData.length % 2 == 0)
            _isPaddedToQuadByteMultiple = subRecordData.size % MAX_PAD_ALIGNMENT == 0
            if (nRemainingBytes >= (if (_isPaddedToQuadByteMultiple) MAX_PAD_ALIGNMENT else NORMAL_PAD_ALIGNMENT)) {
                if (!canPaddingBeDiscarded(subRecordData, nRemainingBytes)) {
                    val msg = ("Leftover " + nRemainingBytes
                            + " bytes in subrecord data " + toHex(subRecordData))
                    throw RecordFormatException(msg)
                }
                _isPaddedToQuadByteMultiple = false
            }
        } else {
            _isPaddedToQuadByteMultiple = false
        }
        _uninterpretedData = null
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("[OBJ]\n")
        for (i in subrecords!!.indices) {
            val record = subrecords!!.get(i)
            sb.append("SUBRECORD: ").append(record.toString())
        }
        sb.append("[/OBJ]\n")
        return sb.toString()
    }

    override fun getRecordSize(): Int {
        if (_uninterpretedData != null) {
            return _uninterpretedData.size + 4
        }
        var size = 0
        for (i in subrecords!!.indices.reversed()) {
            val record = subrecords!!.get(i)
            size += record.getDataSize() + 4
        }
        if (_isPaddedToQuadByteMultiple) {
            while (size % MAX_PAD_ALIGNMENT != 0) {
                size++
            }
        } else {
            while (size % NORMAL_PAD_ALIGNMENT != 0) {
                size++
            }
        }
        return size + 4
    }

    override fun serialize(offset: Int, data: ByteArray): Int {
        val recSize = getRecordSize()
        val dataSize = recSize - 4
        val out = LittleEndianByteArrayOutputStream(data, offset, recSize)

        out.writeShort(Companion.sid.toInt())
        out.writeShort(dataSize)

        if (_uninterpretedData == null) {
            for (i in subrecords!!.indices) {
                val record = subrecords!!.get(i)
                record.serialize(out)
            }
            val expectedEndIx = offset + dataSize
            // padding
            while (out.getWriteIndex() < expectedEndIx) {
                out.writeByte(0)
            }
        } else {
            out.write(_uninterpretedData)
        }
        return recSize
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    fun getSubRecords(): MutableList<SubRecord>? {
        return subrecords
    }

    fun clearSubRecords() {
        subrecords!!.clear()
    }

    fun addSubRecord(index: Int, element: SubRecord?) {
        subrecords!!.add(index, element!!)
    }

    fun addSubRecord(o: SubRecord?): Boolean {
        return subrecords!!.add(o!!)
    }

    override fun clone(): Any {
        val rec = ObjRecord()

        for (i in subrecords!!.indices) {
            val record = subrecords!!.get(i)
            rec.addSubRecord(record.clone() as SubRecord)
        }
        return rec
    }

    companion object {
        const val sid: Short = 0x005D

        private const val NORMAL_PAD_ALIGNMENT = 2
        private const val MAX_PAD_ALIGNMENT = 4

        /**
         * Some XLS files have ObjRecords with nearly 8Kb of excessive padding. These were probably
         * written by a version of POI (around 3.1) which incorrectly interpreted the second short of
         * the ftLbs subrecord (0x1FEE) as a length, and read that many bytes as padding (other bugs
         * helped allow this to occur).
         * 
         * Excel reads files with this excessive padding OK, truncating the over-sized ObjRecord back
         * to the its proper size.  POI does the same.
         */
        private fun canPaddingBeDiscarded(data: ByteArray, nRemainingBytes: Int): Boolean {
            // make sure none of the padding looks important
            for (i in data.size - nRemainingBytes..<data.size) {
                if (data[i].toInt() != 0x00) {
                    return false
                }
            }
            return true
        }
    }
}
