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

/**
 * Tne atom that holds metadata on a specific Link in the document.
 * (The actual link is held in a sibling CString record)
 * 
 * @author Nick Burch
 */
class ExHyperlinkAtom : RecordAtom {
    /**
     * Record header.
     */
    private var _header: ByteArray?

    /**
     * Record data.
     */
    private var _data: ByteArray?

    /**
     * Constructs a brand new link related atom record.
     */
    constructor() {
        _header = ByteArray(8)
        _data = ByteArray(4)

        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _data!!.size)

        // It is fine for the other values to be zero
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
        // Get the header.
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the record data.
        _data = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _data, 0, len - 8)

        // Must be at least 4 bytes long
        require(_data!!.size >= 4) {
            ("The length of the data for a ExHyperlinkAtom must be at least 4 bytes, but was only "
                    + _data!!.size)
        }
    }

    var number: Int
        /**
         * Gets the link number. This will match the one in the
         * InteractiveInfoAtom which uses the link.
         * @return the link number
         */
        get() = LittleEndian.getInt(_data!!, 0)
        /**
         * Sets the link number
         * @param number the link number.
         */
        set(number) {
            LittleEndian.putInt(_data!!, 0, number)
        }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExHyperlinkAtom.typeID.toLong()
    }


    /**
     * 
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }
}
