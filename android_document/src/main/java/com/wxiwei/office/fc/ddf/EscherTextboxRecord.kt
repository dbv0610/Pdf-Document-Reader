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

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort
import com.wxiwei.office.fc.util.RecordFormatException

/**
 * Holds data from the parent application. Most commonly used to store
 * text in the format of the parent application, rather than in
 * Escher format. We don't attempt to understand the contents, since
 * they will be in the parent's format, not Escher format.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nick Burch  (nick at torchbox dot com)
 */
open class EscherTextboxRecord : EscherRecord() {
    /** The data for this record not including the the 8 byte header  */
    private var thedata: ByteArray? = NO_BYTES

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)

        // Save the data, ready for the calling code to do something
        //  useful with it
        thedata = ByteArray(bytesRemaining)
        System.arraycopy(data, offset + 8, thedata, 0, bytesRemaining)
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
        val remainingBytes = thedata!!.size
        putInt(data, offset + 4, remainingBytes)
        System.arraycopy(thedata, 0, data, offset + 8, thedata!!.size)
        val pos = offset + 8 + thedata!!.size

        listener.afterRecordSerialize(pos, recordId, pos - offset, this)
        val size = pos - offset
        if (size != recordSize) throw RecordFormatException(size.toString() + " bytes written but recordSize reports " + recordSize)
        return size
    }

    var data: ByteArray?
        /**
         * Returns any extra data associated with this record.  In practice excel
         * does not seem to put anything here, but with PowerPoint this will
         * contain the bytes that make up a TextHeaderAtom followed by a
         * TextBytesAtom/TextCharsAtom
         */
        get() = thedata
        set(b) {
            setData(b!!, 0, b.size)
        }

    /**
     * Sets the extra data (in the parent application's format) to be
     * contained by the record. Used when the parent application changes
     * the contents.
     */
    fun setData(b: ByteArray, start: Int, length: Int) {
        thedata = ByteArray(length)
        System.arraycopy(b, start, thedata, 0, length)
    }

    override val recordSize: Int
        get() {
        return 8 + thedata!!.size
    }

    override fun clone(): Any {
        // shallow clone
        return super.clone()
    }

    override val recordName: String?
        get() {
        return "ClientTextbox"
    }

    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        var theDumpHex = ""
        try {
            if (thedata!!.size != 0) {
                theDumpHex = "  Extra Data:" + nl
                theDumpHex += HexDump.dump(thedata!!, 0, 0)
            }
        } catch (e: Exception) {
            theDumpHex = "Error!!"
        }

        return javaClass.getName() + ":" + nl +
                "  isContainer: " + isContainerRecord + nl +
                "  options: 0x" + toHex(options) + nl +
                "  recordId: 0x" + toHex(recordId) + nl +
                "  numchildren: " + childRecords.size + nl +
                theDumpHex
    }

    /**
     * 
     */
    override fun dispose() {
        thedata = null
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF00D.toShort()
        const val RECORD_DESCRIPTION: String = "msofbtClientTextbox"

        private val NO_BYTES = ByteArray(0)
    }
}



