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
import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.io.IOException
import java.io.OutputStream

/**
 * A SlidePersist Atom (type 1011). Holds information on the text of a
 * given slide, which are stored in the same SlideListWithText
 * 
 * @author Nick Burch
 */
class SlidePersistAtom : RecordAtom {
    private var _header: ByteArray?
    // Only set these if you know what you're doing!
    /**
     * Slide reference ID. Should correspond to the PersistPtr
     * "sheet ID" of the matching slide/notes record
     */
    var refID: Int = 0
    var hasShapesOtherThanPlaceholders: Boolean = false
        private set

    /** Number of placeholder texts that will follow in the SlideListWithText  */
    var numPlaceholderTexts: Int = 0
        private set

    /**
     * The internal identifier (256+), which is used to tie slides
     * and notes together
     */
    var slideIdentifier: Int = 0

    /** Reserved fields. Who knows what they do  */
    private var reservedFields: ByteArray?

    /* *************** record code follows ********************** */
    /**
     * For the SlidePersist Atom
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

        // Grab the reference ID
        refID = getInt(source, start + 8)

        // Next up is a set of flags, but only bit 3 is used!
        val flags = getInt(source, start + 12)
        if (flags == 4) {
            hasShapesOtherThanPlaceholders = true
        } else {
            hasShapesOtherThanPlaceholders = false
        }

        // Now the number of Placeholder Texts
        numPlaceholderTexts = getInt(source, start + 16)

        // Last useful one is the unique slide identifier
        slideIdentifier = getInt(source, start + 20)

        // Finally you have typically 4 or 8 bytes of reserved fields,
        //  all zero running from 24 bytes in to the end
        reservedFields = ByteArray(len - 24)
        System.arraycopy(source, start + 24, reservedFields, 0, reservedFields!!.size)
    }

    /**
     * Create a new SlidePersistAtom, for use with a new Slide
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 0)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 20)

        hasShapesOtherThanPlaceholders = true
        reservedFields = ByteArray(4)
    }

    /**
     * We are of type 1011
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
        // Header - size or type unchanged
        out.write(_header)

        // Compute the flags part - only bit 3 is used
        var flags = 0
        if (hasShapesOtherThanPlaceholders) {
            flags = 4
        }

        // Write out our fields
        writeLittleEndian(refID, out)
        writeLittleEndian(flags, out)
        writeLittleEndian(numPlaceholderTexts, out)
        writeLittleEndian(slideIdentifier, out)
        out.write(reservedFields)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        reservedFields = null
    }

    companion object {
        private const val _type = 1011L
    }
}
