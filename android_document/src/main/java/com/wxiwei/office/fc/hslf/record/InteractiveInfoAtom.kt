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
 * Tne atom that holds metadata on Links in the document.
 * (The actual link is held Document.ExObjList.ExHyperlink)
 * 
 * @author Nick Burch
 * @author Yegor Kozlov
 */
class InteractiveInfoAtom : RecordAtom {
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
        _data = ByteArray(16)

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

        // Must be at least 16 bytes long
        require(_data!!.size >= 16) { "The length of the data for a InteractiveInfoAtom must be at least 16 bytes, but was only " + _data!!.size }

        // First 4 bytes - no idea, normally 0
        // Second 4 bytes - the id of the link (from 1 onwards)
        // Third 4 bytes - no idea, normally 4
        // Fourth 4 bytes - no idea, normally 8
    }

    var hyperlinkID: Int
        /**
         * Gets the link number. You will normally look the
         * ExHyperlink with this number to get the details.
         * @return the link number
         */
        get() = LittleEndian.getInt(_data!!, 4)
        /**
         * Sets the persistent unique identifier of the link
         * 
         * @param number the persistent unique identifier of the link
         */
        set(number) {
            LittleEndian.putInt(_data!!, 4, number)
        }

    var soundRef: Int
        /**
         * a reference to a sound in the sound collection.
         */
        get() = LittleEndian.getInt(_data!!, 0)
        /**
         * a reference to a sound in the sound collection.
         * 
         * @param val a reference to a sound in the sound collection
         */
        set(`val`) {
            LittleEndian.putInt(_data!!, 0, `val`)
        }

    var action: Byte
        /**
         * Hyperlink Action.
         * 
         * 
         * see `ACTION_*` constants for the list of actions
         * 
         * 
         * @return hyperlink action.
         */
        get() = _data!![8]
        /**
         * Hyperlink Action
         * 
         * 
         * see `ACTION_*` constants for the list of actions
         * 
         * 
         * @param val hyperlink action.
         */
        set(`val`) {
            _data!![8] = `val`
        }

    var oleVerb: Byte
        /**
         * Only valid when action == OLEAction. OLE verb to use, 0 = first verb, 1 = second verb, etc.
         */
        get() = _data!![9]
        /**
         * Only valid when action == OLEAction. OLE verb to use, 0 = first verb, 1 = second verb, etc.
         */
        set(`val`) {
            _data!![9] = `val`
        }

    var jump: Byte
        /**
         * Jump
         * 
         * 
         * see `JUMP_*` constants for the list of actions
         * 
         * 
         * @return jump
         */
        get() = _data!![10]
        /**
         * Jump
         * 
         * 
         * see `JUMP_*` constants for the list of actions
         * 
         * 
         * @param val jump
         */
        set(`val`) {
            _data!![10] = `val`
        }

    var flags: Byte
        /**
         * Flags
         * 
         * 
         *  *  Bit 1: Animated. If 1, then button is animated
         *  *  Bit 2: Stop sound. If 1, then stop current sound when button is pressed.
         *  *  Bit 3: CustomShowReturn. If 1, and this is a jump to custom show,
         * then return to this slide after custom show.
         * 
         */
        get() = _data!![11]
        /**
         * Flags
         * 
         * 
         *  *  Bit 1: Animated. If 1, then button is animated
         *  *  Bit 2: Stop sound. If 1, then stop current sound when button is pressed.
         *  *  Bit 3: CustomShowReturn. If 1, and this is a jump to custom show,
         * then return to this slide after custom show.
         * 
         */
        set(`val`) {
            _data!![11] = `val`
        }

    var hyperlinkType: Byte
        /**
         * hyperlink type
         * 
         * @return hyperlink type
         */
        get() = _data!![12]
        /**
         * hyperlink type
         * 
         * @param val hyperlink type
         */
        set(`val`) {
            _data!![12] = `val`
        }

    /**
     * Gets the record type.
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.InteractiveInfoAtom.typeID.toLong()
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        _data = null
    }

    companion object {
        /**
         * Action Table
         */
        const val ACTION_NONE: Byte = 0
        const val ACTION_MACRO: Byte = 1
        const val ACTION_RUNPROGRAM: Byte = 2
        const val ACTION_JUMP: Byte = 3
        const val ACTION_HYPERLINK: Byte = 4
        const val ACTION_OLE: Byte = 5
        const val ACTION_MEDIA: Byte = 6
        const val ACTION_CUSTOMSHOW: Byte = 7

        /**
         * Jump Table
         */
        const val JUMP_NONE: Byte = 0
        const val JUMP_NEXTSLIDE: Byte = 1
        const val JUMP_PREVIOUSSLIDE: Byte = 2
        const val JUMP_FIRSTSLIDE: Byte = 3
        const val JUMP_LASTSLIDE: Byte = 4
        const val JUMP_LASTSLIDEVIEWED: Byte = 5
        const val JUMP_ENDSHOW: Byte = 6

        /**
         * Types of hyperlinks
         */
        const val LINK_NextSlide: Byte = 0x00
        const val LINK_PreviousSlide: Byte = 0x01
        const val LINK_FirstSlide: Byte = 0x02
        const val LINK_LastSlide: Byte = 0x03
        const val LINK_CustomShow: Byte = 0x06
        const val LINK_SlideNumber: Byte = 0x07
        const val LINK_Url: Byte = 0x08
        const val LINK_OtherPresentation: Byte = 0x09
        const val LINK_OtherFile: Byte = 0x0A
        val LINK_NULL: Byte = 0xFF.toByte()
    }
}
