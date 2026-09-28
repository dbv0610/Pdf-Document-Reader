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
package com.wxiwei.office.fc.ddf

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * This record is used whenever a escher record is encountered that
 * we do not explicitly support.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Zhang Zhang (zhangzzh at gmail.com)
 */
class UnknownEscherRecord : EscherRecord() {
    /** The data for this record not including the the 8 byte header  */
    var data: ByteArray = NO_BYTES
        private set
    private var _childRecords: MutableList<EscherRecord>?

    init {
        _childRecords = ArrayList<EscherRecord>()
    }

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        var offset = offset
        var bytesRemaining = readHeader(data, offset)
        /*
		 * Modified by Zhang Zhang
		 * Have a check between avaliable bytes and bytesRemaining, 
		 * take the avaliable length if the bytesRemaining out of range.
		 * July 09, 2010
		 */
        val avaliable = data.size - (offset + 8)
        if (bytesRemaining > avaliable) {
            bytesRemaining = avaliable
        }

        if (isContainerRecord) {
            var bytesWritten = 0
            this.data = ByteArray(0)
            offset += 8
            bytesWritten += 8
            while (bytesRemaining > 0) {
                val child = recordFactory!!.createRecord(data, offset)
                val childBytesWritten = child.fillFields(data, offset, recordFactory)
                bytesWritten += childBytesWritten
                offset += childBytesWritten
                bytesRemaining -= childBytesWritten
                childRecords.add(child)
            }
            return bytesWritten
        }

        this.data = ByteArray(bytesRemaining)
        System.arraycopy(data, offset + 8, this.data, 0, bytesRemaining)
        return bytesRemaining + 8
    }

    override fun serialize(
        offset: Int,
        data: ByteArray?,
        listener: EscherSerializationListener?
    ): Int {
        val data = data!!
        val listener = listener!!
        listener.beforeRecordSerialize(offset, recordId, this)

        putShort(data, offset, options)
        putShort(data, offset + 2, recordId)
        var remainingBytes = this.data.size
        for (r in _childRecords!!) {
            remainingBytes += r.recordSize
        }
        putInt(data, offset + 4, remainingBytes)
        System.arraycopy(this.data, 0, data, offset + 8, this.data.size)
        var pos = offset + 8 + this.data.size
        for (r in _childRecords!!) {
            pos += r.serialize(pos, data, listener)
        }

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        return pos - offset
    }

    override val recordSize: Int
        get() {
        return 8 + data.size
    }

    override var childRecords: MutableList<EscherRecord>
        get() = _childRecords!!
        set(childRecords) {
            _childRecords = childRecords
        }

    override fun clone(): Any {
        // shallow clone
        return super.clone()
    }

    override val recordName: String
        get() {
        return "Unknown 0x" + toHex(recordId)
    }

    override fun toString(): String {
        val children = StringBuffer()
        if (childRecords.size > 0) {
            children.append("  children: " + '\n')
            for (record in _childRecords!!) {
                children.append(record.toString())
                children.append('\n')
            }
        }

        val theDumpHex = toHex(this.data, 32)

        return javaClass.getName() + ":" + '\n' +
                "  isContainer: " + isContainerRecord + '\n' +
                "  options: 0x" + toHex(options) + '\n' +
                "  recordId: 0x" + toHex(recordId) + '\n' +
                "  numchildren: " + childRecords.size + '\n' +
                theDumpHex +
                children.toString()
    }

    fun addChildRecord(childRecord: EscherRecord?) {
        childRecords.add(childRecord!!)
    }

    /**
     * 
     * 
     */
    override fun dispose() {
        val children = _childRecords
        if (children != null) {
            for (er in children) {
                er.dispose()
            }
            children.clear()
            _childRecords = null
        }
    }

    companion object {
        private val NO_BYTES = ByteArray(0)
    }
}
