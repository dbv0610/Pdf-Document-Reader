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
package com.wxiwei.office.fc.hssf.record.common

import com.wxiwei.office.fc.hssf.record.RecordInputStream
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordInput
import com.wxiwei.office.fc.hssf.record.cont.ContinuableRecordOutput
import com.wxiwei.office.fc.util.BitFieldFactory.Companion.getInstance
import com.wxiwei.office.fc.util.LittleEndianInput
import com.wxiwei.office.fc.util.LittleEndianOutput
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.fc.util.StringUtil
import com.wxiwei.office.fc.util.StringUtil.readUnicodeLE
import java.util.Collections

/**
 * Title: Unicode String
 *
 *
 * Description:  Unicode String - just standard fields that are in several records.
 * It is considered more desirable then repeating it in all of them.
 *
 *
 * This is often called a XLUnicodeRichExtendedString in MS documentation.
 *
 *
 * REFERENCE:  PG 264 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 *
 *
 * REFERENCE:  PG 951 Excel Binary File Format (.xls) Structure Specification v20091214
 */
open class UnicodeString : Comparable<UnicodeString>, Cloneable {
    /**
     * get the number of characters in the string,
     * wrapped as needed to fit within a short
     * 
     * @return number of characters
     */
    var charCountShort: Short = 0
        private set
    /**
     * get the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     * 
     * @return optionflags bitmask
     */
    /**
     * set the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     * 
     * @param of  optionflags bitmask
     */
    var optionFlags: Byte = 0
    private var field_3_string: String? = null
    private var field_4_format_runs: MutableList<FormatRun>? = null
    private var field_5_ext_rst: ExtRst? = null

    class FormatRun(
        @get:JvmName("getCharacterPosProperty") val characterPos: Short,
        @get:JvmName("getFontIndexProperty") var fontIndex: Short
    ) : Comparable<FormatRun> {
        fun getCharacterPos(): Short = characterPos
        fun getFontIndex(): Short = fontIndex
        constructor(`in`: LittleEndianInput) : this(`in`.readShort(), `in`.readShort())

        override fun equals(o: Any?): Boolean {
            if (o !is FormatRun) {
                return false
            }
            val other = o

            return this.characterPos == other.characterPos && this.fontIndex == other.fontIndex
        }

        override fun compareTo(r: FormatRun): Int {
            if (this.characterPos == r.characterPos && this.fontIndex == r.fontIndex) {
                return 0
            }
            if (this.characterPos == r.characterPos) {
                return this.fontIndex - r.fontIndex
            }
            return this.characterPos - r.characterPos
        }

        override fun toString(): String {
            return "character=" + this.characterPos + ",fontIndex=" + this.fontIndex
        }

        fun serialize(out: LittleEndianOutput) {
            out.writeShort(characterPos.toInt())
            out.writeShort(fontIndex.toInt())
        }
    }

    // See page 681
    class ExtRst : Comparable<ExtRst> {
        private var reserved: Short = 0

        // This is a Phs (see page 881)
        var formattingFontIndex: Short = 0
            private set
        var formattingOptions: Short = 0
            private set

        // This is a RPHSSub (see page 894)
        var numberOfRuns: Int = 0
            private set
        private var phoneticText: String? = null

        // This is an array of PhRuns (see page 881)
        var phRuns: Array<PhRun?> = arrayOfNulls<PhRun>(0)
            private set

        // Sometimes there's some cruft at the end
        private var extraData: ByteArray = byteArrayOf()

        private fun populateEmpty() {
            reserved = 1
            phoneticText = ""
            phRuns = arrayOfNulls<PhRun>(0)
            extraData = ByteArray(0)
        }

        protected constructor() {
            populateEmpty()
        }

        constructor(`in`: LittleEndianInput, expectedLength: Int) {
            reserved = `in`.readShort()


            // Old style detection (Reserved = 0xFF)
            if (reserved.toInt() == -1) {
                populateEmpty()
                return
            }


            // Spot corrupt records
            if (reserved.toInt() != 1) {
                _logger.log(
                    POILogger.WARN,
                    "Warning - ExtRst has wrong magic marker, expecting 1 but found " + reserved + " - ignoring"
                )
                // Grab all the remaining data, and ignore it
                for (i in 0..<expectedLength - 2) {
                    `in`.readByte()
                }
                // And make us be empty
                populateEmpty()
                return
            }

            // Carry on reading in as normal
            val stringDataSize = `in`.readShort()

            formattingFontIndex = `in`.readShort()
            formattingOptions = `in`.readShort()


            // RPHSSub
            numberOfRuns = `in`.readUShort()
            val length1 = `in`.readShort()
            // No really. Someone clearly forgot to read
            //  the docs on their datastructure...
            var length2 = `in`.readShort()
            // And sometimes they write out garbage :(
            if (length1.toInt() == 0 && length2 > 0) {
                length2 = 0
            }
            check(length1 == length2) {
                "The two length fields of the Phonetic Text don't agree! " +
                        length1 + " vs " + length2
            }
            phoneticText = readUnicodeLE(`in`, length1.toInt())

            val runData = stringDataSize - 4 - 6 - (2 * phoneticText!!.length)
            val numRuns = (runData / 6)
            phRuns = arrayOfNulls<PhRun>(numRuns)
            for (i in phRuns.indices) {
                phRuns[i] = PhRun(`in`)
            }

            var extraDataLength = runData - (numRuns * 6)
            if (extraDataLength < 0) {
                System.err.println("Warning - ExtRst overran by " + (0 - extraDataLength) + " bytes")
                extraDataLength = 0
            }
            extraData = ByteArray(extraDataLength)
            for (i in extraData.indices) {
                extraData[i] = `in`.readByte()
            }
        }

        val dataSize: Int
            /**
             * Returns our size, excluding our
             * 4 byte header
             */
            get() = 4 + 6 + (2 * phoneticText!!.length) +
                    (6 * phRuns.size) + extraData.size

        fun serialize(out: ContinuableRecordOutput) {
            val dataSize = this.dataSize

            out.writeContinueIfRequired(8)
            out.writeShort(reserved.toInt())
            out.writeShort(dataSize)
            out.writeShort(formattingFontIndex.toInt())
            out.writeShort(formattingOptions.toInt())

            out.writeContinueIfRequired(6)
            out.writeShort(numberOfRuns)
            out.writeShort(phoneticText!!.length)
            out.writeShort(phoneticText!!.length)

            out.writeContinueIfRequired(phoneticText!!.length * 2)
            StringUtil.putUnicodeLE(phoneticText!!, out)

            for (i in phRuns.indices) {
                phRuns[i]?.serialize(out)
            }

            out.write(extraData)
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is ExtRst) {
                return false
            }
            val other = obj
            return (compareTo(other) == 0)
        }

        override fun compareTo(o: ExtRst): Int {
            var result: Int

            result = reserved - o.reserved
            if (result != 0) return result
            result = formattingFontIndex - o.formattingFontIndex
            if (result != 0) return result
            result = formattingOptions - o.formattingOptions
            if (result != 0) return result
            result = numberOfRuns - o.numberOfRuns
            if (result != 0) return result

            result = phoneticText!!.compareTo(o.phoneticText!!)
            if (result != 0) return result

            result = phRuns.size - o.phRuns.size
            if (result != 0) return result
            for (i in phRuns.indices) {
                val run = phRuns[i] ?: continue
                val oRun = o.phRuns[i] ?: continue
                result =
                    run.phoneticTextFirstCharacterOffset - oRun.phoneticTextFirstCharacterOffset
                if (result != 0) return result
                result =
                    run.realTextFirstCharacterOffset - oRun.realTextFirstCharacterOffset
                if (result != 0) return result
                result = run.realTextFirstCharacterOffset - oRun.realTextLength
                if (result != 0) return result
            }

            result = extraData.size - o.extraData.size
            if (result != 0) return result


            // If we get here, it's the same
            return 0
        }

        fun clone(): ExtRst {
            val ext = ExtRst()
            ext.reserved = reserved
            ext.formattingFontIndex = formattingFontIndex
            ext.formattingOptions = formattingOptions
            ext.numberOfRuns = numberOfRuns
            ext.phoneticText = phoneticText
            ext.phRuns = arrayOfNulls<PhRun>(phRuns.size)
            for (i in ext.phRuns.indices) {
                val run = phRuns[i]
                if (run != null) {
                    ext.phRuns[i] = PhRun(
                        run.phoneticTextFirstCharacterOffset,
                        run.realTextFirstCharacterOffset,
                        run.realTextLength
                    )
                }
            }
            return ext
        }

        fun getPhoneticText(): String {
            return phoneticText!!
        }
    }

    class PhRun {
        val phoneticTextFirstCharacterOffset: Int
        val realTextFirstCharacterOffset: Int
        val realTextLength: Int

        constructor(
            phoneticTextFirstCharacterOffset: Int,
            realTextFirstCharacterOffset: Int, realTextLength: Int
        ) {
            this.phoneticTextFirstCharacterOffset = phoneticTextFirstCharacterOffset
            this.realTextFirstCharacterOffset = realTextFirstCharacterOffset
            this.realTextLength = realTextLength
        }

        constructor(`in`: LittleEndianInput) {
            phoneticTextFirstCharacterOffset = `in`.readUShort()
            realTextFirstCharacterOffset = `in`.readUShort()
            realTextLength = `in`.readUShort()
        }

        fun serialize(out: ContinuableRecordOutput) {
            out.writeContinueIfRequired(6)
            out.writeShort(phoneticTextFirstCharacterOffset)
            out.writeShort(realTextFirstCharacterOffset)
            out.writeShort(realTextLength)
        }
    }

    private constructor()

    constructor(str: String) {
        this.string = str
    }


    override fun hashCode(): Int {
        var stringHash = 0
        if (field_3_string != null) stringHash = field_3_string.hashCode()
        return this.charCountShort + stringHash
    }

    /**
     * Our handling of equals is inconsistent with compareTo.  The trouble is because we don't truely understand
     * rich text fields yet it's difficult to make a sound comparison.
     * 
     * @param o     The object to compare.
     * @return      true if the object is actually equal.
     */
    override fun equals(o: Any?): Boolean {
        if (o !is UnicodeString) {
            return false
        }
        val other = o

        //OK lets do this in stages to return a quickly, first check the actual string
        val eq = ((this.charCountShort == other.charCountShort)
                && (this.optionFlags == other.optionFlags)
                && field_3_string == other.field_3_string)
        if (!eq) return false

        //OK string appears to be equal but now lets compare formatting runs
        if ((field_4_format_runs == null) && (other.field_4_format_runs == null))  //Strings are equal, and there are not formatting runs.
            return true
        if (((field_4_format_runs == null) && (other.field_4_format_runs != null)) ||
            (field_4_format_runs != null) && (other.field_4_format_runs == null)
        )  //Strings are equal, but one or the other has formatting runs
            return false

        //Strings are equal, so now compare formatting runs.
        val size = field_4_format_runs!!.size
        if (size != other.field_4_format_runs!!.size) return false

        for (i in 0..<size) {
            val run1 = field_4_format_runs!!.get(i)
            val run2 = other.field_4_format_runs!!.get(i)

            if (run1 != run2) return false
        }

        // Well the format runs are equal as well!, better check the ExtRst data
        if (field_5_ext_rst == null && other.field_5_ext_rst == null) {
            // Good
        } else if (field_5_ext_rst != null && other.field_5_ext_rst != null) {
            val extCmp = field_5_ext_rst!!.compareTo(other.field_5_ext_rst!!)
            if (extCmp == 0) {
                // Good
            } else {
                return false
            }
        } else {
            return false
        }

        //Phew!! After all of that we have finally worked out that the strings
        //are identical.
        return true
    }

    /**
     * construct a unicode string record and fill its fields, ID is ignored
     * @param in the RecordInputstream to read the record from
     */
    constructor(`in`: RecordInputStream) {
        this.charCountShort = `in`.readShort()
        this.optionFlags = `in`.readByte()

        var runCount = 0
        var extensionLength = 0
        //Read the number of rich runs if rich text.
        if (this.isRichText) {
            runCount = `in`.readShort().toInt()
        }
        //Read the size of extended data if present.
        if (this.isExtendedText) {
            extensionLength = `in`.readInt()
        }

        val isCompressed = ((optionFlags.toInt() and 1) == 0)
        if (isCompressed) {
            field_3_string = `in`.readCompressedUnicode(this.charCount)
        } else {
            field_3_string = `in`.readUnicodeLEString(this.charCount)
        }


        if (this.isRichText && (runCount > 0)) {
            field_4_format_runs = ArrayList<FormatRun>(runCount)
            for (i in 0..<runCount) {
                field_4_format_runs!!.add(FormatRun(`in`))
            }
        }

        if (this.isExtendedText && (extensionLength > 0)) {
            field_5_ext_rst = ExtRst(ContinuableRecordInput(`in`), extensionLength)
            if (field_5_ext_rst!!.dataSize + 4 != extensionLength) {
                _logger.log(
                    POILogger.WARN,
                    "ExtRst was supposed to be " + extensionLength + " bytes long, but seems to actually be " + (field_5_ext_rst!!.dataSize + 4)
                )
            }
        }
    }


    val charCount: Int
        /**
         * get the number of characters in the string,
         * as an un-wrapped int
         * 
         * @return number of characters
         */
        get() {
            if (this.charCountShort < 0) {
                return this.charCountShort + 65536
            }
            return charCountShort.toInt()
        }

    /**
     * set the number of characters in the string
     * @param cc - number of characters
     */
    fun setCharCount(cc: Short) {
        this.charCountShort = cc
    }

    @get:JvmName("getStringProperty")
    var string: String
        /**
         * @return the actual string this contains as a java String object
         */
        get() = field_3_string!!
        /**
         * set the actual string this contains
         * @param string  the text
         */
        set(string) {
            field_3_string = string
            setCharCount(field_3_string!!.length.toShort())
            // scan for characters greater than 255 ... if any are
            // present, we have to use 16-bit encoding. Otherwise, we
            // can use 8-bit encoding
            var useUTF16 = false
            val strlen = string.length

            for (j in 0..<strlen) {
                if (string.get(j).code > 255) {
                    useUTF16 = true
                    break
                }
            }
            if (useUTF16)  //Set the uncompressed bit
                this.optionFlags =
                    highByte.setByte(
                        this.optionFlags
                    )
            else this.optionFlags =
                highByte.clearByte(
                    this.optionFlags
                )
        }

    fun getString(): String = string

    @get:JvmName("getFormatRunCountProperty")
    val formatRunCount: Int
        get() {
            if (field_4_format_runs == null) return 0
            return field_4_format_runs!!.size
        }

    fun getFormatRunCount(): Int = formatRunCount

    fun getFormatRun(index: Int): FormatRun? {
        if (field_4_format_runs == null) {
            return null
        }
        if (index < 0 || index >= field_4_format_runs!!.size) {
            return null
        }
        return field_4_format_runs!!.get(index)
    }

    private fun findFormatRunAt(characterPos: Int): Int {
        val size = field_4_format_runs!!.size
        for (i in 0..<size) {
            val r = field_4_format_runs!!.get(i)
            if (r.characterPos.toInt() == characterPos) return i
            else if (r.characterPos > characterPos) return -1
        }
        return -1
    }

    /** Adds a font run to the formatted string.
     * 
     * If a font run exists at the current charcter location, then it is
     * replaced with the font run to be added.
     */
    fun addFormatRun(r: FormatRun) {
        if (field_4_format_runs == null) {
            field_4_format_runs = ArrayList<FormatRun>()
        }

        val index = findFormatRunAt(r.characterPos.toInt())
        if (index != -1) field_4_format_runs!!.removeAt(index)

        field_4_format_runs!!.add(r)
        //Need to sort the font runs to ensure that the font runs appear in
        //character order
        field_4_format_runs?.sortWith { a, b ->
            if (a == null && b == null) 0
            else if (a == null) -1
            else if (b == null) 1
            else a.compareTo(b)
        }

        //Make sure that we now say that we are a rich string
        this.optionFlags = richText.setByte(
            this.optionFlags
        )
    }

    fun formatIterator(): MutableIterator<FormatRun>? {
        if (field_4_format_runs != null) {
            return field_4_format_runs!!.iterator()
        }
        return null
    }

    fun removeFormatRun(r: FormatRun?) {
        field_4_format_runs!!.remove(r!!)
        if (field_4_format_runs!!.size == 0) {
            field_4_format_runs = null
            this.optionFlags = richText.clearByte(
                this.optionFlags
            )
        }
    }

    fun clearFormatting() {
        field_4_format_runs = null
        this.optionFlags = richText.clearByte(
            this.optionFlags
        )
    }


    var extendedRst: ExtRst?
        get() = this.field_5_ext_rst
        set(ext_rst) {
            if (ext_rst != null) {
                this.optionFlags =
                    extBit.setByte(
                        this.optionFlags
                    )
            } else {
                this.optionFlags =
                    extBit.clearByte(
                        this.optionFlags
                    )
            }
            this.field_5_ext_rst = ext_rst
        }


    /**
     * Swaps all use in the string of one font index
     * for use of a different font index.
     * Normally only called when fonts have been
     * removed / re-ordered
     */
    fun swapFontUse(oldFontIndex: Short, newFontIndex: Short) {
        for (run in field_4_format_runs!!) {
            if (run.fontIndex == oldFontIndex) {
                run.fontIndex = newFontIndex
            }
        }
    }

    /**
     * unlike the real records we return the same as "getString()" rather than debug info
     * @see .getDebugInfo
     * @return String value of the record
     */
    override fun toString(): String {
        return this.string
    }

    val debugInfo: String
        /**
         * return a character representation of the fields of this record
         * 
         * 
         * @return String of output for biffviewer etc.
         */
        get() {
            val buffer = StringBuffer()

            buffer.append("[UNICODESTRING]\n")
            buffer.append("    .charcount       = ")
                .append(Integer.toHexString(this.charCount)).append("\n")
            buffer.append("    .optionflags     = ")
                .append(Integer.toHexString(this.optionFlags.toInt())).append("\n")
            buffer.append("    .string          = ").append(this.string).append("\n")
            if (field_4_format_runs != null) {
                for (i in field_4_format_runs!!.indices) {
                    val r = field_4_format_runs!!.get(i)
                    buffer.append("      .format_run" + i + "          = ").append(r.toString())
                        .append("\n")
                }
            }
            if (field_5_ext_rst != null) {
                buffer.append("    .field_5_ext_rst          = ").append("\n")
                buffer.append(field_5_ext_rst.toString()).append("\n")
            }
            buffer.append("[/UNICODESTRING]\n")
            return buffer.toString()
        }

    /**
     * Serialises out the String. There are special rules
     * about where we can and can't split onto
     * Continue records.
     */
    fun serialize(out: ContinuableRecordOutput) {
        var numberOfRichTextRuns = 0
        var extendedDataSize = 0
        if (this.isRichText && field_4_format_runs != null) {
            numberOfRichTextRuns = field_4_format_runs!!.size
        }
        if (this.isExtendedText && field_5_ext_rst != null) {
            extendedDataSize = 4 + field_5_ext_rst!!.dataSize
        }


        // Serialise the bulk of the String
        // The writeString handles tricky continue stuff for us
        out.writeString(field_3_string ?: "", numberOfRichTextRuns, extendedDataSize)

        if (numberOfRichTextRuns > 0) {
            //This will ensure that a run does not split a continue

            for (i in 0..<numberOfRichTextRuns) {
                if (out.availableSpace < 4) {
                    out.writeContinue()
                }
                val r = field_4_format_runs!!.get(i)
                r.serialize(out)
            }
        }

        if (extendedDataSize > 0) {
            field_5_ext_rst!!.serialize(out)
        }
    }

    override fun compareTo(str: UnicodeString): Int {
        var result = this.string.compareTo(str.string)

        //As per the equals method lets do this in stages
        if (result != 0) return result

        //OK string appears to be equal but now lets compare formatting runs
        if ((field_4_format_runs == null) && (str.field_4_format_runs == null))  //Strings are equal, and there are no formatting runs.
            return 0

        if ((field_4_format_runs == null) && (str.field_4_format_runs != null))  //Strings are equal, but one or the other has formatting runs
            return 1
        if ((field_4_format_runs != null) && (str.field_4_format_runs == null))  //Strings are equal, but one or the other has formatting runs
            return -1

        //Strings are equal, so now compare formatting runs.
        val size = field_4_format_runs!!.size
        if (size != str.field_4_format_runs!!.size) return size - str.field_4_format_runs!!.size

        for (i in 0..<size) {
            val run1 = field_4_format_runs!!.get(i)
            val run2 = str.field_4_format_runs!!.get(i)

            result = run1.compareTo(run2)
            if (result != 0) return result
        }

        //Well the format runs are equal as well!, better check the ExtRst data
        if ((field_5_ext_rst == null) && (str.field_5_ext_rst == null)) return 0
        if ((field_5_ext_rst == null) && (str.field_5_ext_rst != null)) return 1
        if ((field_5_ext_rst != null) && (str.field_5_ext_rst == null)) return -1

        result = field_5_ext_rst!!.compareTo(str.field_5_ext_rst!!)
        if (result != 0) return result

        //Phew!! After all of that we have finally worked out that the strings
        //are identical.
        return 0
    }

    private val isRichText: Boolean
        get() = richText.isSet(this.optionFlags.toInt())

    private val isExtendedText: Boolean
        get() = extBit.isSet(this.optionFlags.toInt())

    public override fun clone(): Any {
        val str = UnicodeString()
        str.charCountShort = this.charCountShort
        str.optionFlags = this.optionFlags
        str.field_3_string = field_3_string
        if (field_4_format_runs != null) {
            str.field_4_format_runs = ArrayList<FormatRun>()
            for (r in field_4_format_runs) {
                str.field_4_format_runs!!.add(FormatRun(r.characterPos, r.fontIndex))
            }
        }
        if (field_5_ext_rst != null) {
            str.field_5_ext_rst = field_5_ext_rst!!.clone()
        }

        return str
    }

    companion object {
        // TODO - make this final when the compatibility version is removed
        private val _logger = getLogger(UnicodeString::class.java)

        private val highByte = getInstance(0x1)

        // 0x2 is reserved
        private val extBit = getInstance(0x4)
        private val richText = getInstance(0x8)
    }
}
