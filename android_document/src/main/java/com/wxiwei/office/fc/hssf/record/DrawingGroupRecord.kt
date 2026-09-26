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

import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.NullEscherSerializationListener
import com.wxiwei.office.fc.util.ArrayUtil.Companion.arraycopy
import com.wxiwei.office.fc.util.LittleEndian.putShort
import kotlin.math.min


class DrawingGroupRecord : AbstractEscherHolderRecord {
    constructor()

    constructor(`in`: RecordInputStream) : super(`in`)

    override fun getRecordName(): String {
        return "MSODRAWINGGROUP"
    }

    override fun getSid(): Short {
        return Companion.sid
    }

    override fun serialize(offset: Int, data: ByteArray): Int {
        val rawData = rawData
        if (escherRecords.size == 0 && rawData != null) {
            return writeData(offset, data, rawData)
        }
        val buffer = ByteArray(getRawDataSize())
        var pos = 0
        val iterator: MutableIterator<*> = escherRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next() as EscherRecord
            pos += r.serialize(pos, buffer, NullEscherSerializationListener())
        }

        return writeData(offset, data, buffer)
    }

    /**
     * Process the bytes into escher records.
     * (Not done by default in case we break things,
     * unless you set the "poi.deserialize.escher"
     * system property)
     */
    fun processChildRecords() {
        convertRawBytesToEscherRecords()
    }

    override fun getRecordSize(): Int {
        // TODO - convert this to a RecordAggregate
        return grossSizeFromDataSize(getRawDataSize())
    }

    private fun getRawDataSize(): Int {
        val escherRecords: MutableList<EscherRecord> = escherRecords
        val rawData = rawData
        if (escherRecords.size == 0 && rawData != null) {
            return rawData.size
        }
        var size = 0
        val iterator: MutableIterator<*> = escherRecords.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next() as EscherRecord
            size += r.recordSize
        }
        return size
    }

    private fun writeData(offset: Int, data: ByteArray, rawData: ByteArray): Int {
        var offset = offset
        var writtenActualData = 0
        var writtenRawData = 0
        while (writtenRawData < rawData.size) {
            val segmentLength = min(rawData.size - writtenRawData, MAX_DATA_SIZE)
            if (writtenRawData / MAX_DATA_SIZE >= 2) writeContinueHeader(
                data,
                offset,
                segmentLength
            )
            else writeHeader(data, offset, segmentLength)
            writtenActualData += 4
            offset += 4
            arraycopy(rawData, writtenRawData, data, offset, segmentLength)
            offset += segmentLength
            writtenRawData += segmentLength
            writtenActualData += segmentLength
        }
        return writtenActualData
    }

    private fun writeHeader(data: ByteArray, offset: Int, sizeExcludingHeader: Int) {
        putShort(data, 0 + offset, getSid())
        putShort(data, 2 + offset, sizeExcludingHeader.toShort())
    }

    private fun writeContinueHeader(data: ByteArray, offset: Int, sizeExcludingHeader: Int) {
        putShort(data, 0 + offset, ContinueRecord.Companion.sid)
        putShort(data, 2 + offset, sizeExcludingHeader.toShort())
    }

    companion object {
        const val sid: Short = 0xEB

        const val MAX_RECORD_SIZE: Int = 8228
        private val MAX_DATA_SIZE: Int = MAX_RECORD_SIZE - 4

        fun grossSizeFromDataSize(dataSize: Int): Int {
            return dataSize + ((dataSize - 1) / MAX_DATA_SIZE + 1) * 4
        }
    }
}
