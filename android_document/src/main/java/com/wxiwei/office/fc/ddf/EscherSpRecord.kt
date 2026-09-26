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
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * Together the the EscherOptRecord this record defines some of the basic
 * properties of a shape.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherSpRecord

    : EscherRecord() {
    /**
     * @return  A number that identifies this shape
     */
    /**
     * Sets a number that identifies this shape.
     */
    var shapeId: Int = 0
    /**
     * The flags that apply to this shape.
     * 
     * @see .FLAG_GROUP
     * 
     * @see .FLAG_CHILD
     * 
     * @see .FLAG_PATRIARCH
     * 
     * @see .FLAG_DELETED
     * 
     * @see .FLAG_OLESHAPE
     * 
     * @see .FLAG_HAVEMASTER
     * 
     * @see .FLAG_FLIPHORIZ
     * 
     * @see .FLAG_FLIPVERT
     * 
     * @see .FLAG_CONNECTOR
     * 
     * @see .FLAG_HAVEANCHOR
     * 
     * @see .FLAG_BACKGROUND
     * 
     * @see .FLAG_HASSHAPETYPE
     */
    /**
     * The flags that apply to this shape.
     * 
     * @see .FLAG_GROUP
     * 
     * @see .FLAG_CHILD
     * 
     * @see .FLAG_PATRIARCH
     * 
     * @see .FLAG_DELETED
     * 
     * @see .FLAG_OLESHAPE
     * 
     * @see .FLAG_HAVEMASTER
     * 
     * @see .FLAG_FLIPHORIZ
     * 
     * @see .FLAG_FLIPVERT
     * 
     * @see .FLAG_CONNECTOR
     * 
     * @see .FLAG_HAVEANCHOR
     * 
     * @see .FLAG_BACKGROUND
     * 
     * @see .FLAG_HASSHAPETYPE
     */
    var flags: Int = 0

    override fun fillFields(
        data: ByteArray?,
        offset: Int,
        recordFactory: EscherRecordFactory?
    ): Int {
        val data = data!!
        val bytesRemaining = readHeader(data, offset)
        val pos = offset + 8
        var size = 0
        this.shapeId = getInt(data, pos + size)
        size += 4
        this.flags = getInt(data, pos + size)
        size += 4
        //        bytesRemaining -= size;
//        remainingData  =  new byte[bytesRemaining];
//        System.arraycopy( data, pos + size, remainingData, 0, bytesRemaining );
        return recordSize
    }

    /**
     * This method serializes this escher record into a byte array.
     * 
     * @param offset   The offset into `data` to start writing the record data to.
     * @param data     The byte array to serialize to.
     * @param listener A listener to retrieve start and end callbacks.  Use a `NullEscherSerailizationListener` to ignore these events.
     * @return The number of bytes written.
     * 
     * @see NullEscherSerializationListener
     */
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
        val remainingBytes = 8
        putInt(data, offset + 4, remainingBytes)
        putInt(data, offset + 8, this.shapeId)
        putInt(data, offset + 12, this.flags)
        //        System.arraycopy( remainingData, 0, data, offset + 26, remainingData.length );
//        int pos = offset + 8 + 18 + remainingData.length;
        listener.afterRecordSerialize(
            offset + recordSize,
            recordId,
            recordSize,
            this
        )
        return 8 + 8
    }

    override val recordSize: Int
        get() {
        return 8 + 8
    }

    override var recordId: Short
        set(value) {
            super.recordId = value
        }
        get() {
        return RECORD_ID
    }

    override val recordName: String
        get() {
        return "Sp"
    }


    /**
     * @return  the string representing this shape.
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separator")

        return javaClass.getName() + ":" + nl +
                "  RecordId: 0x" + toHex(RECORD_ID) + nl +
                "  Options: 0x" + toHex(options) + nl +
                "  ShapeId: " + this.shapeId + nl +
                "  Flags: " + decodeFlags(this.flags) + " (0x" + toHex(
            this.flags
        ) + ")" + nl
    }

    /**
     * Converts the shape flags into a more descriptive name.
     */
    private fun decodeFlags(flags: Int): String {
        val result = StringBuffer()
        result.append(if ((flags and FLAG_GROUP) != 0) "|GROUP" else "")
        result.append(if ((flags and FLAG_CHILD) != 0) "|CHILD" else "")
        result.append(if ((flags and FLAG_PATRIARCH) != 0) "|PATRIARCH" else "")
        result.append(if ((flags and FLAG_DELETED) != 0) "|DELETED" else "")
        result.append(if ((flags and FLAG_OLESHAPE) != 0) "|OLESHAPE" else "")
        result.append(if ((flags and FLAG_HAVEMASTER) != 0) "|HAVEMASTER" else "")
        result.append(if ((flags and FLAG_FLIPHORIZ) != 0) "|FLIPHORIZ" else "")
        result.append(if ((flags and FLAG_FLIPVERT) != 0) "|FLIPVERT" else "")
        result.append(if ((flags and FLAG_CONNECTOR) != 0) "|CONNECTOR" else "")
        result.append(if ((flags and FLAG_HAVEANCHOR) != 0) "|HAVEANCHOR" else "")
        result.append(if ((flags and FLAG_BACKGROUND) != 0) "|BACKGROUND" else "")
        result.append(if ((flags and FLAG_HASSHAPETYPE) != 0) "|HASSHAPETYPE" else "")

        //need to check, else blows up on some records - bug 34435
        if (result.length > 0) {
            result.deleteCharAt(0)
        }
        return result.toString()
    }

    /**
     * 
     * 
     */
    override fun dispose() {
    }

    companion object {
        @JvmField
        val RECORD_ID: Short = 0xF00A.toShort()
        const val RECORD_DESCRIPTION: String = "MsofbtSp"

        const val FLAG_GROUP: Int = 0x0001
        const val FLAG_CHILD: Int = 0x0002
        const val FLAG_PATRIARCH: Int = 0x0004
        const val FLAG_DELETED: Int = 0x0008
        const val FLAG_OLESHAPE: Int = 0x0010
        const val FLAG_HAVEMASTER: Int = 0x0020
        const val FLAG_FLIPHORIZ: Int = 0x0040
        const val FLAG_FLIPVERT: Int = 0x0080
        const val FLAG_CONNECTOR: Int = 0x0100
        const val FLAG_HAVEANCHOR: Int = 0x0200
        const val FLAG_BACKGROUND: Int = 0x0400
        const val FLAG_HASSHAPETYPE: Int = 0x0800
    }
}
