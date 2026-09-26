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
package com.wxiwei.office.fc.hssf.record.cont

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.util.DelayableLittleEndianOutput
import com.wxiwei.office.fc.util.LittleEndianByteArrayOutputStream
import com.wxiwei.office.fc.util.LittleEndianOutput


/**
 * Allows the writing of BIFF records when the 'ushort size' header field is not known in advance.
 * When the client is finished writing data, it calls [.terminate], at which point this
 * class updates the 'ushort size' with its final value.
 * 
 * @author Josh Micich
 */
internal class UnknownLengthRecordOutput(private val _originalOut: LittleEndianOutput, sid: Int) :
    LittleEndianOutput {
    /** for writing the 'ushort size'  field once its value is known  */
    private val _dataSizeOutput: LittleEndianOutput
    private val _byteBuffer: ByteArray?
    private var _out: LittleEndianOutput? = null
    private var _size = 0

    init {
        _originalOut.writeShort(sid)
        if (_originalOut is DelayableLittleEndianOutput) {
            // optimisation
            val dleo = _originalOut
            _dataSizeOutput = dleo.createDelayedOutput(2)
            _byteBuffer = null
            _out = _originalOut
        } else {
            // otherwise temporarily write all subsequent data to a buffer
            _dataSizeOutput = _originalOut
            _byteBuffer = ByteArray(RecordInputStream.MAX_RECORD_DATA_SIZE.toInt())
            _out = LittleEndianByteArrayOutputStream(_byteBuffer, 0)
        }
    }

    val totalSize: Int
        /**
         * includes 4 byte header
         */
        get() = 4 + _size
    val availableSpace: Int
        get() {
            checkNotNull(_out) { "Record already terminated" }
            return MAX_DATA_SIZE - _size
        }

    /**
     * Finishes writing the current record and updates 'ushort size' field.<br></br>
     * After this method is called, only [.getTotalSize] may be called.
     */
    fun terminate() {
        checkNotNull(_out) { "Record already terminated" }
        _dataSizeOutput.writeShort(_size)
        if (_byteBuffer != null) {
            _originalOut.write(_byteBuffer, 0, _size)
            _out = null
            return
        }
        _out = null
    }

    override fun write(b: ByteArray) {
        _out!!.write(b)
        _size += b.size
    }

    override fun write(b: ByteArray, offset: Int, len: Int) {
        _out!!.write(b, offset, len)
        _size += len
    }

    override fun writeByte(v: Int) {
        _out!!.writeByte(v)
        _size += 1
    }

    override fun writeDouble(v: Double) {
        _out!!.writeDouble(v)
        _size += 8
    }

    override fun writeInt(v: Int) {
        _out!!.writeInt(v)
        _size += 4
    }

    override fun writeLong(v: Long) {
        _out!!.writeLong(v)
        _size += 8
    }

    override fun writeShort(v: Int) {
        _out!!.writeShort(v)
        _size += 2
    }

    companion object {
        private val MAX_DATA_SIZE = RecordInputStream.MAX_RECORD_DATA_SIZE.toInt()
    }
}
