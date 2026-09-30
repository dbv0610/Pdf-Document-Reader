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

import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import java.io.IOException
import java.io.OutputStream

/**
 * A Notes Atom (type 1009). Holds information on the parent Notes, such
 * as what slide it is tied to
 * 
 * @author Nick Burch
 */
class NotesAtom protected constructor(source: ByteArray, start: Int, len: Int) : RecordAtom() {
    private var _header: ByteArray?
    var slideID: Int
    var followMasterObjects: Boolean = false
    var followMasterScheme: Boolean = false
    var followMasterBackground: Boolean = false
    private var reserved: ByteArray?


    /* *************** record code follows ********************** */ /**
     * For the Notes Atom
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 8) {
            len = 8
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the slide ID
        slideID = getInt(source, start + 8)

        // Grok the flags, stored as bits
        val flags = getUShort(source, start + 12)
        if ((flags and 4) == 4) {
            followMasterBackground = true
        } else {
            followMasterBackground = false
        }
        if ((flags and 2) == 2) {
            followMasterScheme = true
        } else {
            followMasterScheme = false
        }
        if ((flags and 1) == 1) {
            followMasterObjects = true
        } else {
            followMasterObjects = false
        }

        // There might be 2 more bytes, which are a reserved field
        reserved = ByteArray(len - 14)
        System.arraycopy(source, start + 14, reserved, 0, reserved!!.size)
    }

    /**
     * We are of type 1009
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // Header
        out.write(_header)

        // Slide ID
        writeLittleEndian(slideID, out)

        // Flags
        var flags: Short = 0
        if (followMasterObjects) {
            flags = (flags + 1).toShort()
        }
        if (followMasterScheme) {
            flags = (flags + 2).toShort()
        }
        if (followMasterBackground) {
            flags = (flags + 4).toShort()
        }
        writeLittleEndian(flags, out)

        // Reserved fields
        out.write(reserved)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        reserved = null
    }

    companion object {
        private const val _type = 1009L
    }
}
