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
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.HexDump
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.StringUtil

/**
 * A TextCharsAtom (type 4000). Holds text in byte swapped unicode form.
 * The trailing return character is always stripped from this
 * 
 * @author Nick Burch
 */
class TextCharsAtom : RecordAtom {
    private var _header: ByteArray?

    /** The bytes that make up the text  */
    private var _text: ByteArray?

    var text: String
        /** Grabs the text.  */
        get() = StringUtil.getFromUnicodeLE(_text!!)
        /** Updates the text in the Atom.  */
        set(text) {
            // Convert to little endian unicode
            _text = ByteArray(text.length * 2)
            StringUtil.putUnicodeLE(text, _text!!, 0)

            // Update the size (header bytes 5-8)
            LittleEndian.putInt(_header!!, 4, _text!!.size)
        }

    /* *************** record code follows ********************** */
    /**
     * For the TextChars Atom
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
     * Create an empty TextCharsAtom
     */
    constructor() {
        // 0 length header
        _header = byteArrayOf(0, 0, (0xA0 - 256).toByte(), 0x0f, 0, 0, 0, 0)
        // Empty text
        _text = ByteArray(0)
    }

    /**
     * We are of type 4000
     */
    public override fun getRecordType(): Long {
        return _type
    }


    /**
     * dump debug info; use getText() to return a string
     * representation of the atom
     */
    override fun toString(): String {
        val out = StringBuffer()
        out.append("TextCharsAtom:\n")
        out.append(HexDump.dump(_text!!, 0, 0))
        return out.toString()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _text = null
    }

    companion object {
        private const val _type = 4000L
    }
}
