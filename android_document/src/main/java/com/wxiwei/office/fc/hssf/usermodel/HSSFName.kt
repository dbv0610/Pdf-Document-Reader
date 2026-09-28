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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.NameCommentRecord
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.ss.usermodel.Name
import com.wxiwei.office.ss.model.XLSModel.AWorkbook


/**
 * High Level Representation of a 'defined name' which could be a 'built-in' name,
 * 'named range' or name of a user defined function.
 * 
 * @author Libin Roman (Vista Portal LDT. Developer)
 */
class HSSFName
/**
 * Creates new HSSFName   - called by HSSFWorkbook to create a name from
 * scratch.
 * 
 * @see HSSFWorkbook.createName
 * @param name the Name Record
 * @param comment the Name Comment Record, optional.
 * @param book workbook object associated with the sheet.
 */(
    private var _book: AWorkbook?,
    private var _definedNameRec: NameRecord?,
    private var _commentRec: NameCommentRecord?
) : Name {
    /**
     * Creates new HSSFName   - called by HSSFWorkbook to create a name from
     * scratch.
     * 
     * @see HSSFWorkbook.createName
     * @param name the Name Record
     * @param book workbook object associated with the sheet.
     */
    /* package */
    internal constructor(book: AWorkbook?, name: NameRecord?) : this(book, name, null)

    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range referred to
     */
    override fun getSheetName(): String? {
        val indexToExternSheet = _definedNameRec!!.getExternSheetNumber()

        return _book!!.getInternalWorkbook()!!.findSheetNameFromExternSheet(indexToExternSheet)
    }

    /**
     * @return text name of this defined name
     */
    override fun getNameName(): String? {
        return _definedNameRec!!.getNameText()
    }

    /**
     * Sets the name of the named range
     * 
     * 
     * The following is a list of syntax rules that you need to be aware of when you create and edit names.
     * 
     *  * **Valid characters**
     * The first character of a name must be a letter, an underscore character (_), or a backslash (\).
     * Remaining characters in the name can be letters, numbers, periods, and underscore characters.
     * 
     *  * **Cell references disallowed**
     * Names cannot be the same as a cell reference, such as Z$100 or R1C1.
     *  * **Spaces are not valid**
     * Spaces are not allowed as part of a name. Use the underscore character (_) and period (.) as word separators, such as, Sales_Tax or First.Quarter.
     * 
     *  * **Name length**
     * A name can contain up to 255 characters.
     * 
     *  * **Case sensitivity**
     * Names can contain uppercase and lowercase letters.
     * 
     * 
     * 
     * 
     * 
     * A name must always be unique within its scope. POI prevents you from defining a name that is not unique
     * within its scope. However you can use the same name in different scopes. Example:
     * <pre><blockquote>
     * //by default names are workbook-global
     * HSSFName name;
     * name = workbook.createName();
     * name.setNameName("sales_08");
     * 
     * name = workbook.createName();
     * name.setNameName("sales_08"); //will throw an exception: "The workbook already contains this name (case-insensitive)"
     * 
     * //create sheet-level name
     * name = workbook.createName();
     * name.setSheetIndex(0); //the scope of the name is the first sheet
     * name.setNameName("sales_08");  //ok
     * 
     * name = workbook.createName();
     * name.setSheetIndex(0);
     * name.setNameName("sales_08");  //will throw an exception: "The sheet already contains this name (case-insensitive)"
     * 
    </blockquote></pre> * 
     * 
     * 
     * @param nameName named range name to set
     * @throws IllegalArgumentException if the name is invalid or the name already exists (case-insensitive)
     */
    override fun setNameName(nameName: String) {
        validateName(nameName)

        val definedNameRec = _definedNameRec!!
        val wb = _book!!.getInternalWorkbook()
        definedNameRec.setNameText(nameName)

        val sheetNumber: Int = definedNameRec.sheetNumber

        //Check to ensure no other names have the same case-insensitive name
        for (i in wb!!.numNames - 1 downTo 0) {
            val rec = wb.getNameRecord(i)
            if (rec != definedNameRec) {
                if (rec.getNameText()
                        .equals(nameName, ignoreCase = true) && sheetNumber == rec.sheetNumber
                ) {
                    val msg =
                        "The " + (if (sheetNumber == 0) "workbook" else "sheet") + " already contains this name: " + nameName
                    definedNameRec.setNameText(nameName + "(2)")
                    throw IllegalArgumentException(msg)
                }
            }
        }


        // Update our comment, if there is one
        val commentRec = _commentRec
        if (commentRec != null) {
            commentRec.nameText = nameName
            _book!!.getInternalWorkbook()!!.updateNameCommentRecordCache(commentRec)
        }
    }

    @get:Deprecated("(Nov 2008) Misleading name. Use {@link #getRefersToFormula()} instead.")
    @set:Deprecated("(Nov 2008) Misleading name. Use {@link #setRefersToFormula(String)} instead.")
    var reference: String?
        /**
         * Returns the formula that the name is defined to refer to.
         * 
         */
        get() = getRefersToFormula()
        /**
         * Sets the formula that the name is defined to refer to.
         * 
         */
        set(ref) {
            setRefersToFormula(ref)
        }

    override fun setRefersToFormula(formulaText: String?) {
//        Ptg[] ptgs = HSSFFormulaParser.parse(formulaText, _book, FormulaType.NAMEDRANGE, getSheetIndex());
//        _definedNameRec.setNameDefinition(ptgs);
    }

    override fun getRefersToFormula(): String? {
//        if (_definedNameRec.isFunctionName()) {
//            throw new IllegalStateException("Only applicable to named ranges");
//        }
//        Ptg[] ptgs = _definedNameRec.getNameDefinition();
//        if (ptgs.length < 1) {
//            // 'refersToFormula' has not been set yet
//            return null;
//        }
//        return HSSFFormulaParser.toFormulaString(_book, ptgs);
        return null
    }

    val refersToFormulaDefinition: Array<Ptg?>
        get() {
            check(!_definedNameRec!!.isFunctionName()) { "Only applicable to named ranges" }
            return _definedNameRec!!.getNameDefinition()
        }

    override fun isDeleted(): Boolean {
        val ptgs: Array<Ptg?> = _definedNameRec!!.getNameDefinition()
        return Ptg.doesFormulaReferToDeletedCell(ptgs)
    }

    /**
     * Checks if this name is a function name
     * 
     * @return true if this name is a function name
     */
    override fun isFunctionName(): Boolean {
        return _definedNameRec!!.isFunctionName()
    }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(_definedNameRec!!.getNameText())
        sb.append("]")
        return sb.toString()
    }

    /**
     * Specifies if the defined name is a local name, and if so, which sheet it is on.
     * 
     * @param index if greater than 0, the defined name is a local name and the value MUST be a 0-based index
     * to the collection of sheets as they appear in the workbook.
     * @throws IllegalArgumentException if the sheet index is invalid.
     */
    override fun setSheetIndex(index: Int) {
        val lastSheetIx = _book!!.getNumberOfSheets() - 1
        require(!(index < -1 || index > lastSheetIx)) {
            "Sheet index (" + index + ") is out of range" +
                    (if (lastSheetIx == -1) "" else (" (0.." + lastSheetIx + ")"))
        }

        _definedNameRec!!.sheetNumber = index + 1
    }

    /**
     * Returns the sheet index this name applies to.
     * 
     * @return the sheet index this name applies to, -1 if this name applies to the entire workbook
     */
    override fun getSheetIndex(): Int {
        return _definedNameRec!!.sheetNumber - 1
    }

    /**
     * Returns the comment the user provided when the name was created.
     * 
     * @return the user comment for this named range
     */
    override fun getComment(): String? {
        val commentRec = _commentRec
        if (commentRec != null) {
            // Prefer the comment record if it has text in it
            if (commentRec.commentText.isNotEmpty()) {
                return commentRec.commentText
            }
        }
        return _definedNameRec!!.descriptionText
    }

    /**
     * Sets the comment the user provided when the name was created.
     * 
     * @param comment the user comment for this named range
     */
    override fun setComment(comment: String?) {
        // Update the main record
        _definedNameRec!!.descriptionText = comment
        // If we have a comment record too, update that as well
        val commentRec = _commentRec
        if (commentRec != null) {
            commentRec.commentText = comment ?: ""
        }
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     * 
     * @param value `true` indicates the name refers to a function.
     */
    override fun setFunction(value: Boolean) {
        _definedNameRec!!.setFunction(value)
    }

    fun dispose() {
        _book = null
        _definedNameRec = null
        _commentRec = null
    }

    companion object {
        private fun validateName(name: String) {
            require(name.length != 0) { "Name cannot be blank" }

            val c = name.get(0)
            require(!(!(c == '_' || Character.isLetter(c)) || name.indexOf(' ') != -1)) { "Invalid name: '" + name + "'; Names must begin with a letter or underscore and not contain spaces" }
        }
    }
}
