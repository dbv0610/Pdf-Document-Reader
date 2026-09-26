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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.io.OutputStream

/**
 * An atom record that specifies information about external audio or video data.
 * 
 * @author Yegor Kozlov
 */
class ExMediaAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * record data
     */
    private var _recdata: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _recdata = ByteArray(8)

        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _recdata!!.size)
    }

    /**
     * Constructs the link related atom record from its
     * source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the record data
        _recdata = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _recdata, 0, len - 8)
    }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExMediaAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     * 
     * @param out the output stream to write to.
     * @throws IOException if an error occurs.
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)
        out.write(_recdata)
    }

    var objectId: Int
        /**
         * A 4-byte unsigned integer that specifies an ID for an external object.
         * 
         * @return  A 4-byte unsigned integer that specifies an ID for an external object.
         */
        get() = LittleEndian.getInt(_recdata!!, 0)
        /**
         * A 4-byte unsigned integer that specifies an ID for an external object.
         * 
         * @param id  A 4-byte unsigned integer that specifies an ID for an external object.
         */
        set(id) {
            LittleEndian.putInt(_recdata!!, 0, id)
        }

    var mask: Int
        /**
         * A bit mask specifying options for displaying headers and footers
         * 
         * @return A bit mask specifying options for displaying headers and footers
         */
        get() = LittleEndian.getInt(_recdata!!, 4)
        /**
         * A bit mask specifying options for displaying video
         * 
         * @param mask A bit mask specifying options for displaying video
         */
        set(mask) {
            LittleEndian.putInt(_recdata!!, 4, mask)
        }

    /**
     * @param bit the bit to check
     * @return whether the specified flag is set
     */
    fun getFlag(bit: Int): Boolean {
        return (this.mask and bit) != 0
    }

    /**
     * @param  bit the bit to set
     * @param  value whether the specified bit is set
     */
    fun setFlag(bit: Int, value: Boolean) {
        var mask = this.mask
        if (value) mask = mask or bit
        else mask = mask and bit.inv()
        this.mask = mask
    }

    override fun toString(): String {
        val buf = StringBuffer()
        buf.append("ExMediaAtom\n")
        buf.append("\tObjectId: " + this.objectId + "\n")
        buf.append("\tMask    : " + this.mask + "\n")
        buf.append("\t  fLoop        : " + getFlag(fLoop) + "\n")
        buf.append("\t  fRewind   : " + getFlag(fRewind) + "\n")
        buf.append("\t  fNarration    : " + getFlag(fNarration) + "\n")
        return buf.toString()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _recdata = null
    }

    companion object {
        /**
         * A bit that specifies whether the audio or video data is repeated continuously during playback.
         */
        const val fLoop: Int = 1

        /**
         * A bit that specifies whether the audio or video data is rewound after playing.
         */
        const val fRewind: Int = 2

        /**
         * A bit that specifies whether the audio data is recorded narration for the slide show. It MUST be FALSE if this ExMediaAtom record is contained by an ExVideoContainer record.
         */
        const val fNarration: Int = 4
    }
}
