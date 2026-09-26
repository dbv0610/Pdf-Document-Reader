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

import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecord
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Title:        Extended Static String Table (0x00FF)
 *
 *
 * Description: This record is used for a quick lookup into the SST record. This
 * record breaks the SST table into a set of buckets. The offsets
 * to these buckets within the SST record are kept as well as the
 * position relative to the start of the SST record.
 * REFERENCE:  PG 313 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at apache dot org)
</P> */
class ExtSSTRecord : ContinuableRecord {
    class InfoSubRecord {
        private val field_1_stream_pos: Int // stream pointer to the SST record
        private val field_2_bucket_sst_offset: Int // don't really understand this yet.

        /** unused - supposed to be zero  */
        private var field_3_zero: Short = 0

        /** Creates new ExtSSTInfoSubRecord  */
        constructor(streamPos: Int, bucketSstOffset: Int) {
            field_1_stream_pos = streamPos
            field_2_bucket_sst_offset = bucketSstOffset
        }

        constructor(`in`: RecordInputStream) {
            field_1_stream_pos = `in`.readInt()
            field_2_bucket_sst_offset = `in`.readShort().toInt()
            field_3_zero = `in`.readShort()
        }

        fun getStreamPos(): Int {
            return field_1_stream_pos
        }

        fun getBucketSSTOffset(): Int {
            return field_2_bucket_sst_offset
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeInt(field_1_stream_pos)
            out.writeShort(field_2_bucket_sst_offset)
            out.writeShort(field_3_zero.toInt())
        }

        companion object {
            const val ENCODED_SIZE: Int = 8
        }
    }


    private var _stringsPerBucket: Short
    private var _sstInfos: Array<InfoSubRecord?>


    constructor() {
        _stringsPerBucket = DEFAULT_BUCKET_SIZE.toShort()
        _sstInfos = arrayOfNulls<InfoSubRecord>(0)
    }

    constructor(`in`: RecordInputStream) {
        _stringsPerBucket = `in`.readShort()

        val nInfos = `in`.remaining() / InfoSubRecord.ENCODED_SIZE
        val lst = ArrayList<InfoSubRecord?>(nInfos)

        while (`in`.available() > 0) {
            val info = InfoSubRecord(`in`)
            lst.add(info)

            if (`in`.available() == 0 && `in`.hasNextRecord() && `in`.getNextSid() == ContinueRecord.Companion.sid.toInt()) {
                `in`.nextRecord()
            }
        }
        _sstInfos = lst.toTypedArray<InfoSubRecord?>()
    }

    fun setNumStringsPerBucket(numStrings: Short) {
        _stringsPerBucket = numStrings
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        buffer.append("[EXTSST]\n")
        buffer.append("    .dsst           = ")
            .append(Integer.toHexString(_stringsPerBucket.toInt()))
            .append("\n")
        buffer.append("    .numInfoRecords = ").append(_sstInfos.size)
            .append("\n")
        for (k in _sstInfos.indices) {
            buffer.append("    .inforecord     = ").append(k).append("\n")
            buffer.append("    .streampos      = ")
                .append(
                    Integer
                        .toHexString(_sstInfos[k]!!.getStreamPos())
                ).append("\n")
            buffer.append("    .sstoffset      = ")
                .append(
                    Integer
                        .toHexString(_sstInfos[k]!!.getBucketSSTOffset())
                )
                .append("\n")
        }
        buffer.append("[/EXTSST]\n")
        return buffer.toString()
    }

    public override fun serialize(out: ContinuableRecordOutput) {
        out.writeShort(_stringsPerBucket.toInt())
        for (k in _sstInfos.indices) {
            _sstInfos[k]!!.serialize(out)
        }
    }

    protected fun getDataSize(): Int {
        return 2 + InfoSubRecord.ENCODED_SIZE * _sstInfos.size
    }

    protected fun getInfoSubRecords(): Array<InfoSubRecord?> {
        return _sstInfos
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    fun setBucketOffsets(bucketAbsoluteOffsets: IntArray, bucketRelativeOffsets: IntArray) {
        // TODO - replace no-arg constructor with this logic
        _sstInfos = arrayOfNulls<InfoSubRecord>(bucketAbsoluteOffsets.size)
        for (i in bucketAbsoluteOffsets.indices) {
            _sstInfos[i] = InfoSubRecord(bucketAbsoluteOffsets[i], bucketRelativeOffsets[i])
        }
    }

    companion object {
        const val sid: Short = 0x00FF
        const val DEFAULT_BUCKET_SIZE: Int = 8

        //Can't seem to find this documented but from the biffviewer it is clear that
        //Excel only records the indexes for the first 128 buckets.
        const val MAX_BUCKETS: Int = 128


        fun getNumberOfInfoRecsForStrings(numStrings: Int): Int {
            var infoRecs: Int = (numStrings / DEFAULT_BUCKET_SIZE)
            if ((numStrings % DEFAULT_BUCKET_SIZE) != 0) infoRecs++
            //Excel seems to max out after 128 info records.
            //This isn't really documented anywhere...
            if (infoRecs > MAX_BUCKETS) infoRecs = MAX_BUCKETS
            return infoRecs
        }

        /** Given a number of strings (in the sst), returns the size of the extsst record */
        fun getRecordSizeForStrings(numStrings: Int): Int {
            return 4 + 2 + getNumberOfInfoRecsForStrings(numStrings) * 8
        }
    }
}
