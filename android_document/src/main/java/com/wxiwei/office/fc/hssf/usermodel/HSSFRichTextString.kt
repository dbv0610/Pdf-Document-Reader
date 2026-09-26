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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.record.LabelSSTRecord
import com.wxiwei.office.fc.hssf.record.SSTRecord
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.record.common.UnicodeString.FormatRun
import com.wxiwei.office.fc.ss.usermodel.IFont
import com.wxiwei.office.fc.ss.usermodel.RichTextString

/**
 * Rich text unicode string.  These strings can have fonts applied to
 * arbitary parts of the string.
 * 
 * 
 * 
 * Note, that in certain cases creating too many HSSFRichTextString cells may cause Excel 2003 and lower to crash
 * when changing the color of the cells and then saving the Excel file. Compare two snippets that produce equivalent output:
 * 
 * 
 * <blockquote><pre>
 * HSSFCell hssfCell = row.createCell(idx);
 * //rich text consists of two runs
 * HSSFRichTextString richString = new HSSFRichTextString( "Hello, World!" );
 * richString.applyFont( 0, 6, font1 );
 * richString.applyFont( 6, 13, font2 );
 * hssfCell.setCellValue( richString );
</pre></blockquote> * 
 * 
 * and
 * 
 * 
 * <blockquote><pre>
 * //create a cell style and assign the first font to it
 * HSSFCellStyle style = workbook.createCellStyle();
 * style.setFont(font1);
 * 
 * HSSFCell hssfCell = row.createCell(idx);
 * hssfCell.setCellStyle(style);
 * 
 * //rich text consists of one run overriding the cell style
 * HSSFRichTextString richString = new HSSFRichTextString( "Hello, World!" );
 * richString.applyFont( 6, 13, font2 );
 * hssfCell.setCellValue( richString );
</pre></blockquote> * 
 * 
 * 
 * Excel always uses the latter approach: for a reach text containing N runs Excel saves the font of the first run in the cell's
 * style and subsequent N-1 runs override this font.
 * 
 * 
 *  For more information regarding this behavior please consult Bugzilla 47543:
 * 
 * [
 * https://issues.apache.org/bugzilla/show_bug.cgi?id=47543](https://issues.apache.org/bugzilla/show_bug.cgi?id=47543)
 * 
 * 
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Jason Height (jheight at apache.org)
 */
class HSSFRichTextString : Comparable<HSSFRichTextString>, RichTextString {
    /**
     * Returns the raw, probably shared Unicode String.
     * Used when tweaking the styles, eg updating font
     * positions.
     * Changes to this string may well effect
     * other RichTextStrings too!
     */
    var rawUnicodeString: UnicodeString? = null
        private set
    private var _book: InternalWorkbook? = null
    private var _record: LabelSSTRecord? = null

    @JvmOverloads
    constructor(string: String? = "") {
        if (string == null) {
            this.rawUnicodeString = UnicodeString("")
        } else {
            this.rawUnicodeString = UnicodeString(string)
        }
    }

    internal constructor(book: InternalWorkbook, record: LabelSSTRecord) {
        setWorkbookReferences(book, record)

        this.rawUnicodeString = book.getSSTString(record.sstIndex)
    }

    /** This must be called to setup the internal work book references whenever
     * a RichTextString is added to a cell
     */
    fun setWorkbookReferences(book: InternalWorkbook?, record: LabelSSTRecord) {
        _book = book
        _record = record
    }

    /** Called whenever the unicode string is modified. When it is modified
     * we need to create a new SST index, so that other LabelSSTRecords will not
     * be affected by changes that we make to this string.
     */
    private fun cloneStringIfRequired(): UnicodeString? {
        if (_book == null) return this.rawUnicodeString
        val s = rawUnicodeString!!.clone() as UnicodeString
        return s
    }

    private fun addToSSTIfRequired() {
        if (_book != null && _record != null) {
            val index = _book!!.addSSTString(this.rawUnicodeString)
            _record!!.sstIndex = index
            //The act of adding the string to the SST record may have meant that
            //an existing string was returned for the index, so update our local version
            this.rawUnicodeString = _book!!.getSSTString(index)
        }
    }


    /**
     * Applies a font to the specified characters of a string.
     * 
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply the font to (exclusive)
     * @param fontIndex     The font to use.
     */
    override fun applyFont(startIndex: Int, endIndex: Int, fontIndex: Short) {
        require(startIndex <= endIndex) { "Start index must be less than end index." }
        require(!(startIndex < 0 || endIndex > length())) { "Start and end index not in range." }
        if (startIndex == endIndex) return

        //Need to check what the font is currently, so we can reapply it after
        //the range is completed
        var currentFont: Short = NO_FONT
        if (endIndex != length()) {
            currentFont = this.getFontAtIndex(endIndex)
        }

        //Need to clear the current formatting between the startIndex and endIndex
        this.rawUnicodeString = cloneStringIfRequired()
        val formatting: MutableIterator<*>? = rawUnicodeString!!.formatIterator()
        if (formatting != null) {
            while (formatting.hasNext()) {
                val r = formatting.next() as FormatRun
                if ((r.characterPos >= startIndex) && (r.characterPos < endIndex)) formatting.remove()
            }
        }


        rawUnicodeString!!.addFormatRun(FormatRun(startIndex.toShort(), fontIndex))
        if (endIndex != length()) rawUnicodeString!!.addFormatRun(
            FormatRun(
                endIndex.toShort(),
                currentFont
            )
        )

        addToSSTIfRequired()
    }

    /**
     * Applies a font to the specified characters of a string.
     * 
     * @param startIndex    The start index to apply the font to (inclusive)
     * @param endIndex      The end index to apply to font to (exclusive)
     * @param font          The index of the font to use.
     */
    override fun applyFont(startIndex: Int, endIndex: Int, font: IFont) {
        applyFont(startIndex, endIndex, (font as HSSFFont).getIndex())
    }

    /**
     * Sets the font of the entire string.
     * @param font          The font to use.
     */
    override fun applyFont(font: IFont) {
        applyFont(0, rawUnicodeString!!.charCount, font)
    }

    /**
     * Removes any formatting that may have been applied to the string.
     */
    override fun clearFormatting() {
        this.rawUnicodeString = cloneStringIfRequired()
        rawUnicodeString!!.clearFormatting()
        addToSSTIfRequired()
    }

    @get:JvmName("getSSTIndexProperty")
    val sSTIndex: Int
        get() = _record?.sstIndex ?: -1
    fun getSSTIndex(): Int = sSTIndex

    /**
     * Returns the plain string representation.
     */
    override fun getString(): String {
        return rawUnicodeString!!.string
    }

    @get:JvmName("getUnicodeStringProperty")
    var unicodeString: UnicodeString?
        /**
         * Used internally by the HSSFCell to get the internal
         * string value.
         * Will ensure the string is not shared
         */
        get() = cloneStringIfRequired()
        /** Used internally by the HSSFCell to set the internal string value */
        set(str) {
            this.rawUnicodeString = str
        }

    fun getUnicodeString(): UnicodeString? = unicodeString


    /**
     * @return  the number of characters in the text.
     */
    override fun length(): Int {
        return rawUnicodeString!!.charCount
    }

    /**
     * Returns the font in use at a particular index.
     * 
     * @param index         The index.
     * @return              The font that's currently being applied at that
     * index or null if no font is being applied or the
     * index is out of range.
     */
    fun getFontAtIndex(index: Int): Short {
        val size = rawUnicodeString!!.formatRunCount
        var currentRun: FormatRun? = null
        for (i in 0..<size) {
            val r = rawUnicodeString!!.getFormatRun(i)
            if (r!!.characterPos > index) {
                break
            }
            currentRun = r
        }
        if (currentRun == null) {
            return NO_FONT
        }
        return currentRun.fontIndex
    }

    /**
     * @return  The number of formatting runs used. There will always be at
     * least one of font NO_FONT.
     * 
     * @see .NO_FONT
     */
    override fun numFormattingRuns(): Int {
        return rawUnicodeString!!.formatRunCount
    }

    /**
     * The index within the string to which the specified formatting run applies.
     * @param index     the index of the formatting run
     * @return  the index within the string.
     */
    override fun getIndexOfFormattingRun(index: Int): Int {
        val r = rawUnicodeString!!.getFormatRun(index)
        return r!!.characterPos.toInt()
    }

    /**
     * Gets the font used in a particular formatting run.
     * 
     * @param index     the index of the formatting run
     * @return  the font number used.
     */
    fun getFontOfFormattingRun(index: Int): Short {
        val r = rawUnicodeString!!.getFormatRun(index)
        return r!!.fontIndex
    }

    /**
     * Compares one rich text string to another.
     */
    override fun compareTo(r: HSSFRichTextString): Int {
        return rawUnicodeString!!.compareTo(r.rawUnicodeString!!)
    }

    override fun equals(o: Any?): Boolean {
        if (o is HSSFRichTextString) {
            return rawUnicodeString!!.equals(o.rawUnicodeString)
        }
        return false
    }

    /**
     * @return  the plain text representation of this string.
     */
    override fun toString(): String {
        return rawUnicodeString.toString()
    }

    /**
     * Applies the specified font to the entire string.
     * 
     * @param fontIndex  the font to apply.
     */
    override fun applyFont(fontIndex: Short) {
        applyFont(0, rawUnicodeString!!.charCount, fontIndex)
    }

    companion object {
        /** Place holder for indicating that NO_FONT has been applied here  */
        const val NO_FONT: Short = 0
    }
}
