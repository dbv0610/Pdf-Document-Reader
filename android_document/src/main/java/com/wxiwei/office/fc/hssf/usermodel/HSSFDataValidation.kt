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

import com.wxiwei.office.fc.hssf.record.DVRecord
import com.wxiwei.office.fc.ss.usermodel.DataValidation
import com.wxiwei.office.fc.ss.usermodel.DataValidation.ErrorStyle
import com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint
import com.wxiwei.office.fc.ss.util.CellRangeAddressList


/**
 * Utility class for creating data validation cells
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
class HSSFDataValidation(
    private val _regions: CellRangeAddressList,
    constraint: DataValidationConstraint?
) : DataValidation {
    private var _prompt_title: String? = null
    private var _prompt_text: String? = null
    private var _error_title: String? = null
    private var _error_text: String? = null

    private var _errorStyle = ErrorStyle.STOP
    private var _emptyCellAllowed = true
    private var _suppress_dropdown_arrow = false
    private var _showPromptBox = true
    private var _showErrorBox = true
    val constraint: DVConstraint

    /**
     * Constructor which initializes the cell range on which this object will be
     * applied
     * @param constraint
     */
    init {
        //FIXME: This cast can be avoided.
        this.constraint = constraint as DVConstraint
    }


    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getConstraint()
	 */
    override fun getValidationConstraint(): DataValidationConstraint {
        return this.constraint
    }

    override fun getRegions(): CellRangeAddressList {
        return _regions
    }


    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setErrorStyle(int)
	 */
    override fun setErrorStyle(error_style: Int) {
        _errorStyle = error_style
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorStyle()
	 */
    override fun getErrorStyle(): Int {
        return _errorStyle
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setEmptyCellAllowed(boolean)
	 */
    override fun setEmptyCellAllowed(allowed: Boolean) {
        _emptyCellAllowed = allowed
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getEmptyCellAllowed()
	 */
    override fun getEmptyCellAllowed(): Boolean {
        return _emptyCellAllowed
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setSuppressDropDownArrow(boolean)
	 */
    override fun setSuppressDropDownArrow(suppress: Boolean) {
        _suppress_dropdown_arrow = suppress
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getSuppressDropDownArrow()
	 */
    override fun getSuppressDropDownArrow(): Boolean {
        if (constraint.getValidationType() == DataValidationConstraint.ValidationType.LIST) {
            return _suppress_dropdown_arrow
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setShowPromptBox(boolean)
	 */
    override fun setShowPromptBox(show: Boolean) {
        _showPromptBox = show
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getShowPromptBox()
	 */
    override fun getShowPromptBox(): Boolean {
        return _showPromptBox
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setShowErrorBox(boolean)
	 */
    override fun setShowErrorBox(show: Boolean) {
        _showErrorBox = show
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getShowErrorBox()
	 */
    override fun getShowErrorBox(): Boolean {
        return _showErrorBox
    }


    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#createPromptBox(java.lang.String, java.lang.String)
	 */
    override fun createPromptBox(title: String?, text: String?) {
        _prompt_title = title
        _prompt_text = text
        this.setShowPromptBox(true)
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxTitle()
	 */
    override fun getPromptBoxTitle(): String? {
        return _prompt_title
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxText()
	 */
    override fun getPromptBoxText(): String? {
        return _prompt_text
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#createErrorBox(java.lang.String, java.lang.String)
	 */
    override fun createErrorBox(title: String?, text: String?) {
        _error_title = title
        _error_text = text
        this.setShowErrorBox(true)
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxTitle()
	 */
    override fun getErrorBoxTitle(): String? {
        return _error_title
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxText()
	 */
    override fun getErrorBoxText(): String? {
        return _error_text
    }

    fun createDVRecord(sheet: HSSFSheet?): DVRecord {
        val fp = constraint.createFormulas(sheet)

        return DVRecord(
            constraint.getValidationType(),
            constraint.getOperator(),
            _errorStyle, _emptyCellAllowed, getSuppressDropDownArrow(),
            constraint.getValidationType() == DataValidationConstraint.ValidationType.LIST && constraint.getExplicitListValues() != null,
            _showPromptBox, _prompt_title, _prompt_text,
            _showErrorBox, _error_title, _error_text,
            fp.formula1, fp.formula2,
            _regions
        )
    }
}
