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

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import java.io.IOException
import java.io.OutputStream

/**
 * A Slide Atom (type 1007). Holds information on the parent Slide, what
 * Master Slide it uses, what Notes is attached to it, that sort of thing.
 * It also has a SSlideLayoutAtom embeded in it, but without the Atom header
 * 
 * @author Nick Burch
 */
class SlideAtom : RecordAtom {
    private var _header: ByteArray?
    /** Get the ID of the master slide used. 0 if this is a master slide, otherwise -2147483648  */
    /** Change slide master.   */
    var masterID: Int
    /** Get the ID of the notes for this slide. 0 if doesn't have one  */
    /** Change the ID of the notes for this slide. 0 if it no longer has one  */
    var notesID: Int

    var followMasterObjects: Boolean = false
    var followMasterScheme: Boolean = false
    var followMasterBackground: Boolean = false

    /** Get the embeded SSlideLayoutAtom  */
    var sSlideLayoutAtom: SSlideLayoutAtom?
        private set
    private var reserved: ByteArray?


    /* *************** record code follows ********************** */
    /**
     * For the Slide Atom
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking
        var len = len
        if (len < 30) {
            len = 30
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the 12 bytes that is "SSlideLayoutAtom"
        val SSlideLayoutAtomData = ByteArray(12)
        System.arraycopy(source, start + 8, SSlideLayoutAtomData, 0, 12)
        // Use them to build up the SSlideLayoutAtom
        this.sSlideLayoutAtom = SSlideLayoutAtom(SSlideLayoutAtomData)

        // Get the IDs of the master and notes
        masterID = getInt(source, start + 12 + 8)
        notesID = getInt(source, start + 16 + 8)

        // Grok the flags, stored as bits
        val flags = getUShort(source, start + 20 + 8)
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

        // If there's any other bits of data, keep them about
        // 8 bytes header + 20 bytes to flags + 2 bytes flags = 30 bytes
        reserved = ByteArray(len - 30)
        System.arraycopy(source, start + 30, reserved, 0, reserved!!.size)
    }

    /**
     * Create a new SlideAtom, to go with a new Slide
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 2)
        LittleEndian.putUShort(_header!!, 2, _type.toInt())
        LittleEndian.putInt(_header!!, 4, 24)

        val ssdate = ByteArray(12)
        this.sSlideLayoutAtom = SSlideLayoutAtom(ssdate)
        sSlideLayoutAtom!!.geometryType = SSlideLayoutAtom.BLANK_SLIDE

        followMasterObjects = true
        followMasterScheme = true
        followMasterBackground = true
        masterID = -2147483648
        notesID = 0
        reserved = ByteArray(2)
    }

    /**
     * We are of type 1007
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

        // SSSlideLayoutAtom stuff
        sSlideLayoutAtom!!.writeOut(out)

        // IDs
        writeLittleEndian(masterID, out)
        writeLittleEndian(notesID, out)

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

        // Reserved data
        out.write(reserved)
    }


    /**
     * Holds the geometry of the Slide, and the ID of the placeholders
     * on the slide.
     * (Embeded inside SlideAtom is a SSlideLayoutAtom, without the
     * usual record header. Since it's a fixed size and tied to
     * the SlideAtom, we'll hold it here.)
     */
    class SSlideLayoutAtom(data: ByteArray) {
        /** Retrieve the geometry type  */
        /** Set the geometry type  */
        /** What geometry type we are  */
        var geometryType: Int

        /** What placeholder IDs we have  */
        private var placeholderIDs: ByteArray?

        /**
         * Create a new Embeded SSlideLayoutAtom, from 12 bytes of data
         */
        init {
            if (data.size != 12) {
                throw RuntimeException("SSlideLayoutAtom created with byte array not 12 bytes long - was " + data.size + " bytes in size")
            }

            // Grab out our data
            this.geometryType = getInt(data, 0)
            placeholderIDs = ByteArray(8)
            System.arraycopy(data, 4, placeholderIDs, 0, 8)
        }

        /**
         * Write the contents of the record back, so it can be written
         * to disk. Skips the record header
         */
        @Throws(IOException::class)
        fun writeOut(out: OutputStream) {
            // Write the geometry
            Record.writeLittleEndian(this.geometryType, out)
            // Write the placeholder IDs
            out.write(placeholderIDs)
        }

        /**
         * 
         */
        fun dispose() {
            placeholderIDs = null
        }

        companion object {
            // The different kinds of geometry
            const val TITLE_SLIDE: Int = 0
            const val TITLE_BODY_SLIDE: Int = 1
            const val TITLE_MASTER_SLIDE: Int = 2
            const val MASTER_SLIDE: Int = 3
            const val MASTER_NOTES: Int = 4
            const val NOTES_TITLE_BODY: Int = 5
            const val HANDOUT: Int = 6 // Only header, footer and date placeholders
            const val TITLE_ONLY: Int = 7
            const val TITLE_2_COLUMN_BODY: Int = 8
            const val TITLE_2_ROW_BODY: Int = 9
            const val TITLE_2_COLUNM_RIGHT_2_ROW_BODY: Int = 10
            const val TITLE_2_COLUNM_LEFT_2_ROW_BODY: Int = 11
            const val TITLE_2_ROW_BOTTOM_2_COLUMN_BODY: Int = 12
            const val TITLE_2_ROW_TOP_2_COLUMN_BODY: Int = 13
            const val FOUR_OBJECTS: Int = 14
            const val BIG_OBJECT: Int = 15
            const val BLANK_SLIDE: Int = 16
            const val VERTICAL_TITLE_BODY_LEFT: Int = 17
            const val VERTICAL_TITLE_2_ROW_BODY_LEFT: Int = 17
        }
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (this.sSlideLayoutAtom != null) {
            sSlideLayoutAtom!!.dispose()
            this.sSlideLayoutAtom = null
        }
        reserved = null
    }

    companion object {
        private const val _type = 1007L
        const val MASTER_SLIDE_ID: Int = 0
        val USES_MASTER_SLIDE_ID: Int = -2147483648
    }
}
