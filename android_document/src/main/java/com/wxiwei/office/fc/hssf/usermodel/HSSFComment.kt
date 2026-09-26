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

import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.TextObjectRecord
import com.wxiwei.office.fc.ss.usermodel.Comment
import com.wxiwei.office.fc.ss.usermodel.RichTextString


/**
 * Represents a cell comment - a sticky note associated with a cell.
 * 
 * @author Yegor Kozlov
 */
class HSSFComment(
    escherContainer: EscherContainerRecord?,
    parent: HSSFShape?,
    anchor: HSSFAnchor?
) : HSSFTextbox(escherContainer, parent, anchor), Comment {
    /*
         * TODO - make HSSFComment more consistent when created vs read from file.
         * Currently HSSFComment has two main forms (corresponding to the two constructors).   There
         * are certain operations that only work on comment objects in one of the forms (e.g. deleting
         * comments).
         * POI is also deficient in its management of RowRecord fields firstCol and lastCol.  Those 
         * fields are supposed to take comments into account, but POI does not do this yet (feb 2009).
         * It seems like HSSFRow should manage a collection of local HSSFComments 
         */
    private var _visible: Boolean = false
    private var _row = 0
    private var _col = 0
    private var _author: String? = null

    /**
     * Returns the underlying Note record
     */
    var noteRecord: NoteRecord? = null
        private set

    /**
     * Returns the underlying Text record
     */
    var textObjectRecord: TextObjectRecord? = null
        private set

    /**
     * Construct a new comment with the given parent and anchor.
     * 
     * @param parent
     * @param anchor  defines position of this anchor in the sheet
     */
    init {
        shapeType = OBJECT_TYPE_COMMENT.toInt()

        //default color for comments
        setFillColor(0x08000050, 255)

        //by default comments are hidden
        _visible = false

        _author = ""
    }

    constructor(note: NoteRecord?, txo: TextObjectRecord?) : this(
        null,
        null as HSSFShape?,
        null as HSSFAnchor?
    ) {
        this.textObjectRecord = txo
        this.noteRecord = note
    }

    /**
     * Returns whether this comment is visible.
     * 
     * @param visible `true` if the comment is visible, `false` otherwise
     */
    override fun setVisible(visible: Boolean) {
        if (this.noteRecord != null) {
            noteRecord!!.setFlags(if (visible) NoteRecord.NOTE_VISIBLE else NoteRecord.NOTE_HIDDEN)
        }
        _visible = visible
    }

    /**
     * Sets whether this comment is visible.
     * 
     * @return `true` if the comment is visible, `false` otherwise
     */
    override fun isVisible(): Boolean {
        return _visible
    }

    /**
     * Return the row of the cell that contains the comment
     * 
     * @return the 0-based row of the cell that contains the comment
     */
    override fun getRow(): Int {
        return _row
    }

    /**
     * Set the row of the cell that contains the comment
     * 
     * @param row the 0-based row of the cell that contains the comment
     */
    override fun setRow(row: Int) {
        if (this.noteRecord != null) {
            noteRecord!!.setRow(row)
        }
        _row = row
    }

    /**
     * Return the column of the cell that contains the comment
     * 
     * @return the 0-based column of the cell that contains the comment
     */
    override fun getColumn(): Int {
        return _col
    }

    /**
     * Set the column of the cell that contains the comment
     * 
     * @param col the 0-based column of the cell that contains the comment
     */
    override fun setColumn(col: Int) {
        if (this.noteRecord != null) {
            noteRecord!!.setColumn(col)
        }
        _col = col
    }

    @Deprecated("(Nov 2009) use {@link HSSFComment#setColumn(int)} }")
    fun setColumn(col: Short) {
        setColumn(col.toInt())
    }

    /**
     * Name of the original comment author
     * 
     * @return the name of the original author of the comment
     */
    override fun getAuthor(): String? {
        return _author
    }

    /**
     * Name of the original comment author
     * 
     * @param author the name of the original author of the comment
     */
    override fun setAuthor(author: String) {
        if (this.noteRecord != null) noteRecord!!.setAuthor(author)
        this._author = author
    }

    /**
     * Sets the rich text string used by this comment.
     * 
     * @param string    Sets the rich text string used by this object.
     */
    override fun getString(): HSSFRichTextString {
        return super.getString()
    }

    override fun setString(string: RichTextString?) {
        val hstring = string as HSSFRichTextString
        //if font is not set we must set the default one
        if (hstring.numFormattingRuns() == 0) hstring.applyFont(0.toShort())

        if (this.textObjectRecord != null) {
            textObjectRecord!!.setStr(hstring)
        }
        super.setString(string)
    }
}
