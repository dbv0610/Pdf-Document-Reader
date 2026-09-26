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

import com.wxiwei.office.fc.hslf.util.SystemTimeUtils
import com.wxiwei.office.fc.util.LittleEndian
import java.io.IOException
import java.io.OutputStream
import java.util.Date

/**
 * An atomic record containing information about a comment.
 * 
 * @author Daniel Noll
 */
class Comment2000Atom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs a brand new comment atom record.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(28)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)

        // It is fine for the other values to be zero
    }

    /**
     * Constructs the comment atom record from its source data.
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)
    }

    var number: Int
        /**
         * Gets the comment number (note - each user normally has their own count).
         * @return the comment number.
         */
        get() = LittleEndian.getInt(_data!!, 0)
        /**
         * Sets the comment number (note - each user normally has their own count).
         * @param number the comment number.
         */
        set(number) {
            LittleEndian.putInt(_data!!, 0, number)
        }

    var date: Date
        /**
         * Gets the date the comment was made.
         * @return the comment date.
         */
        get() = SystemTimeUtils.getDate(_data!!, 4)
        /**
         * Sets the date the comment was made.
         * @param date the comment date.
         */
        set(date) {
            SystemTimeUtils.storeDate(date, _data!!, 4)
        }

    var xOffset: Int
        /**
         * Gets the X offset of the comment on the page.
         * @return the X offset.
         */
        get() = LittleEndian.getInt(_data!!, 20)
        /**
         * Sets the X offset of the comment on the page.
         * @param xOffset the X offset.
         */
        set(xOffset) {
            LittleEndian.putInt(_data!!, 20, xOffset)
        }

    var yOffset: Int
        /**
         * Gets the Y offset of the comment on the page.
         * @return the Y offset.
         */
        get() = LittleEndian.getInt(_data!!, 24)
        /**
         * Sets the Y offset of the comment on the page.
         * @param yOffset the Y offset.
         */
        set(yOffset) {
            LittleEndian.putInt(_data!!, 24, yOffset)
        }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.Comment2000Atom.typeID.toLong()
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
        out.write(_data)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }
}
