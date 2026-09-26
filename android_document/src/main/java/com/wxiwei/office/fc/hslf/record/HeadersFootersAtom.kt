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
 * An atom record that specifies options for displaying headers and footers
 * on a presentation slide or notes slide.
 * 
 * @author Yegor Kozlov
 */
class HeadersFootersAtom : RecordAtom {
    /**
     * record header
     */
    private var _header: ByteArray?

    /**
     * record data
     */
    private var _recdata: ByteArray?

    /**
     * Build an instance of `HeadersFootersAtom` from on-disk data
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
     * Create a new instance of `HeadersFootersAtom`
     */
    constructor() {
        _recdata = ByteArray(4)

        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())
        LittleEndian.putInt(_header!!, 4, _recdata!!.size)
    }

    public override fun getRecordType(): Long {
        return RecordTypes.HeadersFootersAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)
        out.write(_recdata)
    }

    var formatId: Int
        /**
         * A signed integer that specifies the format ID to be used to style the datetime.
         * 
         * 
         * It MUST be in the range [0, 12]. 
         * This value is converted into a string as specified by the index field of the DateTimeMCAtom record.
         * It MUST be ignored unless fHasTodayDate is TRUE.
         * 
         * 
         * @return  A signed integer that specifies the format ID to be used to style the datetime.
         */
        get() = LittleEndian.getShort(_recdata!!, 0).toInt()
        /**
         * A signed integer that specifies the format ID to be used to style the datetime.
         * 
         * @param formatId  A signed integer that specifies the format ID to be used to style the datetime.
         */
        set(formatId) {
            LittleEndian.putUShort(_recdata!!, 0, formatId)
        }

    var mask: Int
        /**
         * A bit mask specifying options for displaying headers and footers
         * 
         *  *  A - [.fHasDate] (1 bit): A bit that specifies whether the date is displayed in the footer.
         *  *  B - [.fHasTodayDate] (1 bit): A bit that specifies whether the current datetime is used for
         * displaying the datetime.
         *  *  C - [.fHasUserDate] (1 bit): A bit that specifies whether the date specified in UserDateAtom record
         * is used for displaying the datetime.
         *  *  D - [.fHasSlideNumber] (1 bit): A bit that specifies whether the slide number is displayed in the footer.
         *  *  E - [.fHasHeader] (1 bit): A bit that specifies whether the header text specified by HeaderAtom
         * record is displayed.
         *  *  F - [.fHasFooter] (1 bit): A bit that specifies whether the footer text specified by FooterAtom
         * record is displayed.
         *  *  reserved (10 bits): MUST be zero and MUST be ignored.
         * 
         * @return A bit mask specifying options for displaying headers and footers
         */
        get() = LittleEndian.getShort(_recdata!!, 2).toInt()
        /**
         * A bit mask specifying options for displaying headers and footers
         * 
         * @param mask A bit mask specifying options for displaying headers and footers
         */
        set(mask) {
            LittleEndian.putUShort(_recdata!!, 2, mask)
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
        buf.append("HeadersFootersAtom\n")
        buf.append("\tFormatId: " + this.formatId + "\n")
        buf.append("\tMask    : " + this.mask + "\n")
        buf.append("\t  fHasDate        : " + getFlag(fHasDate) + "\n")
        buf.append("\t  fHasTodayDate   : " + getFlag(fHasTodayDate) + "\n")
        buf.append("\t  fHasUserDate    : " + getFlag(fHasUserDate) + "\n")
        buf.append("\t  fHasSlideNumber : " + getFlag(fHasSlideNumber) + "\n")
        buf.append("\t  fHasHeader      : " + getFlag(fHasHeader) + "\n")
        buf.append("\t  fHasFooter      : " + getFlag(fHasFooter) + "\n")
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
         * A bit that specifies whether the date is displayed in the footer.
         * @see .getMask
         * @see .setMask
         */
        const val fHasDate: Int = 1

        /**
         * A bit that specifies whether the current datetime is used for displaying the datetime.
         * @see .getMask
         * @see .setMask
         */
        const val fHasTodayDate: Int = 2

        /**
         * A bit that specifies whether the date specified in UserDateAtom record
         * is used for displaying the datetime.
         * 
         * @see .getMask
         * @see .setMask
         */
        const val fHasUserDate: Int = 4

        /**
         * A bit that specifies whether the slide number is displayed in the footer.
         * 
         * @see .getMask
         * @see .setMask
         */
        const val fHasSlideNumber: Int = 8

        /**
         * bit that specifies whether the header text is displayed.
         * 
         * @see .getMask
         * @see .setMask
         */
        const val fHasHeader: Int = 16

        /**
         * bit that specifies whether the footer text is displayed.
         * 
         * @see .getMask
         * @see .setMask
         */
        const val fHasFooter: Int = 32
    }
}
