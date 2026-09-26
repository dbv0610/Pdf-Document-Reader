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

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * @author Glen Stampoultzis
 */
open class EscherBlipRecord : EscherRecord() {
    var picturedata: ByteArray? = null
        protected set
    /**
     * @return Returns the tempFilePath.
     */
    /**
     * @param tempFilePath The tempFilePath to set.
     */
    //
    @JvmField
    var tempFilePath: String? = null

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesAfterHeader = readHeader(data, offset)
        val pos: Int = offset + HEADER_SIZE

        this.picturedata = ByteArray(bytesAfterHeader)
        System.arraycopy(data, pos, this.picturedata, 0, bytesAfterHeader)

        return bytesAfterHeader + 8
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

        System.arraycopy(this.picturedata, 0, data, offset + 4, picturedata!!.size)

        listener.afterRecordSerialize(
            offset + 4 + picturedata!!.size,
            recordId,
            picturedata!!.size + 4,
            this
        )
        return picturedata!!.size + 4
    }

    override val recordSize: Int
        get() {
        return picturedata!!.size + HEADER_SIZE
    }

    override val recordName: String?
        get() {
        return "Blip"
    }

    fun setPictureData(pictureData: ByteArray?) {
        this.picturedata = pictureData
    }

    override fun toString(): String {
        val extraData = HexDump.toHex(this.picturedata!!, 32)
        return javaClass.getName() + ":" + '\n' +
                "  RecordId: 0x" + toHex(recordId) + '\n' +
                "  Options: 0x" + toHex(options) + '\n' +
                "  Extra Data:" + '\n' + extraData
    }

    /**
     * 
     */
    override fun dispose() {
        this.picturedata = null
    }

    companion object {
        // TODO - instantiable superclass
        @JvmField
        val RECORD_ID_START: Short = 0xF018.toShort()
        val RECORD_ID_END: Short = 0xF117.toShort()
        const val RECORD_DESCRIPTION: String = "msofbtBlip"

        private const val HEADER_SIZE = 8
    }
}
