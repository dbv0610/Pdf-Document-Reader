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
import com.wxiwei.office.fc.util.StringUtil

/**
 * A CString (type 4026). Holds a unicode string, and the first two bytes
 * of the record header normally encode the count. Typically attached to
 * some complex sequence of records, eg Commetns.
 * 
 * @author Nick Burch
 */
class CString : RecordAtom {
    private var _header: ByteArray?

    /** The bytes that make up the text  */
    private var _text: ByteArray?

    var text: String
        /** Grabs the text. Never `null`  */
        get() = StringUtil.getFromUnicodeLE(_text!!)
        /** Updates the text in the Atom.  */
        set(text) {
            // Convert to little endian unicode
            _text = ByteArray(text.length * 2)
            StringUtil.putUnicodeLE(text, _text!!, 0)

            // Update the size (header bytes 5-8)
            LittleEndian.putInt(_header!!, 4, _text!!.size)
        }

    var options: Int
        /**
         * Grabs the count, from the first two bytes of the header.
         * The meaning of the count is specific to the type of the parent record
         */
        get() = LittleEndian.getShort(_header!!).toInt()
        /**
         * Sets the count
         * The meaning of the count is specific to the type of the parent record
         */
        set(count) {
            LittleEndian.putShort(_header!!, count.toShort())
        }

    /* *************** record code follows ********************** */
    /**
     * For the CStrubg Atom
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking
        var len = len
        if (len < 8) {
            len = 8
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the text
        _text = ByteArray(len - 8)
        System.arraycopy(source, start + 8, _text, 0, len - 8)
    }

    /**
     * Create an empty CString
     */
    constructor() {
        // 0 length header
        _header = byteArrayOf(0, 0, (0xBA - 256).toByte(), 0x0f, 0, 0, 0, 0)
        // Empty text
        _text = ByteArray(0)
    }

    /**
     * We are of type 4026
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Gets a string representation of this object, primarily for debugging.
     * @return a string representation of this object.
     */
    override fun toString(): String {
        return this.text
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _text = null
    }

    companion object {
        private const val _type = 4026L
    }
}
