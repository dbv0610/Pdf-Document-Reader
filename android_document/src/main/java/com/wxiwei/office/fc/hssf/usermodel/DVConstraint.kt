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
import com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint
import com.wxiwei.office.ss.util.DateUtil
import com.wxiwei.office.ss.util.DateUtil.Companion.getExcelDate
import com.wxiwei.office.ss.util.DateUtil.Companion.parseYYYYMMDDDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 
 * @author Josh Micich
 */
class DVConstraint private constructor(
    private val _validationType: Int,
    private var _operator: Int,
    private var _formula1: String?,
    private var _formula2: String?,
    private var _value1: Double?,
    private var _value2: Double?,
    private var _explicitListValues: Array<String?>?
) : DataValidationConstraint {
    /* package */
    class FormulaPair(val formula1: Array<Ptg?>?, val formula2: Array<Ptg?>?)


    /**
     * Creates a list constraint
     */
    private constructor(listFormula: String?, excplicitListValues: Array<String?>?) : this(
        DataValidationConstraint.ValidationType.LIST, DataValidationConstraint.OperatorType.IGNORED,
        listFormula, null, null, null, excplicitListValues
    )

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#getValidationType()
	 */
    override fun getValidationType(): Int {
        return _validationType
    }

    val isListValidationType: Boolean
        /**
         * Convenience method
         * @return `true` if this constraint is a 'list' validation
         */
        get() = _validationType == DataValidationConstraint.ValidationType.LIST
    val isExplicitList: Boolean
        /**
         * Convenience method
         * @return `true` if this constraint is a 'list' validation with explicit values
         */
        get() = _validationType == DataValidationConstraint.ValidationType.LIST && _explicitListValues != null

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#getOperator()
	 */
    override fun getOperator(): Int {
        return _operator
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#setOperator(int)
	 */
    override fun setOperator(operator: Int) {
        _operator = operator
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#getExplicitListValues()
	 */
    override fun getExplicitListValues(): Array<String?>? {
        return _explicitListValues
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#setExplicitListValues(java.lang.String[])
	 */
    override fun setExplicitListValues(explicitListValues: Array<String?>?) {
        if (_validationType != DataValidationConstraint.ValidationType.LIST) {
            throw RuntimeException("Cannot setExplicitListValues on non-list constraint")
        }
        _formula1 = null
        _explicitListValues = explicitListValues
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#getFormula1()
	 */
    override fun getFormula1(): String? {
        return _formula1
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#setFormula1(java.lang.String)
	 */
    override fun setFormula1(formula1: String?) {
        _value1 = null
        _explicitListValues = null
        _formula1 = formula1
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#getFormula2()
	 */
    override fun getFormula2(): String? {
        return _formula2
    }

    /* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidationConstraint#setFormula2(java.lang.String)
	 */
    override fun setFormula2(formula2: String?) {
        _value2 = null
        _formula2 = formula2
    }

    var value1: Double?
        /**
         * @return the numeric value for expression 1. May be `null`
         */
        get() = _value1
        /**
         * Sets a numeric value for expression 1.
         */
        set(value1) {
            _formula1 = null
            _value1 = value1
        }

    var value2: Double?
        /**
         * @return the numeric value for expression 2. May be `null`
         */
        get() = _value2
        /**
         * Sets a numeric value for expression 2.
         */
        set(value2) {
            _formula2 = null
            _value2 = value2
        }

    /**
     * @return both parsed formulas (for expression 1 and 2).
     */
    /* package */
    fun createFormulas(sheet: HSSFSheet?): FormulaPair {
        val formula1: Array<Ptg?>?
        val formula2: Array<Ptg?>?
        if (this.isListValidationType) {
            formula1 = createListFormula(sheet)
            formula2 = Ptg.EMPTY_PTG_ARRAY
        } else {
            formula1 = convertDoubleFormula(_formula1, _value1, sheet)
            formula2 = convertDoubleFormula(_formula2, _value2, sheet)
        }
        return FormulaPair(formula1, formula2)
    }

    private fun createListFormula(sheet: HSSFSheet?): Array<Ptg?>? {
        //		if (_explicitListValues == null) {
//            HSSFWorkbook wb = sheet.getWorkbook();
//            // formula is parsed with slightly different RVA rules: (root node type must be 'reference')
//			return HSSFFormulaParser.parse(_formula1, wb, FormulaType.DATAVALIDATION_LIST, wb.getSheetIndex(sheet));
//			// To do: Excel places restrictions on the available operations within a list formula.
//			// Some things like union and intersection are not allowed.
//		}
//		// explicit list was provided
//		StringBuffer sb = new StringBuffer(_explicitListValues.length * 16);
//		for (int i = 0; i < _explicitListValues.length; i++) {
//			if (i > 0) {
//				sb.append('\0'); // list delimiter is the nul char
//			}
//			sb.append(_explicitListValues[i]);
//		
//		}
//		return new Ptg[] { new StringPtg(sb.toString()), };

        return null
    }

    companion object {
        // convenient access to ValidationType namespace
        private val VT: DataValidationConstraint.ValidationType? = null


        /**
         * Creates a number based data validation constraint. The text values entered for expr1 and expr2
         * can be either standard Excel formulas or formatted number values. If the expression starts
         * with '=' it is parsed as a formula, otherwise it is parsed as a formatted number.
         * 
         * @param validationType one of [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.ValidationType.ANY],
         * [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.ValidationType.DECIMAL],
         * [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.ValidationType.INTEGER],
         * [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.ValidationType.TEXT_LENGTH]
         * @param comparisonOperator any constant from [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.OperatorType] enum
         * @param expr1 date formula (when first char is '=') or formatted number value
         * @param expr2 date formula (when first char is '=') or formatted number value
         */
        fun createNumericConstraint(
            validationType: Int, comparisonOperator: Int,
            expr1: String?, expr2: String?
        ): DVConstraint {
            when (validationType) {
                DataValidationConstraint.ValidationType.ANY -> require(!(expr1 != null || expr2 != null)) { "expr1 and expr2 must be null for validation type 'any'" }
                DataValidationConstraint.ValidationType.DECIMAL, DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.ValidationType.TEXT_LENGTH -> {
                    requireNotNull(expr1) { "expr1 must be supplied" }
                    DataValidationConstraint.OperatorType.validateSecondArg(
                        comparisonOperator,
                        expr2
                    )
                }

                else -> throw IllegalArgumentException(
                    ("Validation Type ("
                            + validationType + ") not supported with this method")
                )
            }
            // formula1 and value1 are mutually exclusive
            val formula1: String? = getFormulaFromTextExpression(expr1)
            val value1: Double? = if (formula1 == null) convertNumber(expr1) else null
            // formula2 and value2 are mutually exclusive
            val formula2: String? = getFormulaFromTextExpression(expr2)
            val value2: Double? = if (formula2 == null) convertNumber(expr2) else null
            return DVConstraint(
                validationType,
                comparisonOperator,
                formula1,
                formula2,
                value1,
                value2,
                null
            )
        }

        fun createFormulaListConstraint(listFormula: String?): DVConstraint {
            return DVConstraint(listFormula, null)
        }

        fun createExplicitListConstraint(explicitListValues: Array<String?>?): DVConstraint {
            return DVConstraint(null, explicitListValues)
        }


        /**
         * Creates a time based data validation constraint. The text values entered for expr1 and expr2
         * can be either standard Excel formulas or formatted time values. If the expression starts
         * with '=' it is parsed as a formula, otherwise it is parsed as a formatted time.  To parse
         * formatted times, two formats are supported:  "HH:MM" or "HH:MM:SS".  This is contrary to
         * Excel which uses the default time format from the OS.
         * 
         * @param comparisonOperator constant from [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.OperatorType] enum
         * @param expr1 date formula (when first char is '=') or formatted time value
         * @param expr2 date formula (when first char is '=') or formatted time value
         */
        fun createTimeConstraint(
            comparisonOperator: Int,
            expr1: String,
            expr2: String?
        ): DVConstraint {
            requireNotNull(expr1) { "expr1 must be supplied" }
            DataValidationConstraint.OperatorType.validateSecondArg(comparisonOperator, expr1)


            // formula1 and value1 are mutually exclusive
            val formula1: String? = getFormulaFromTextExpression(expr1)
            val value1: Double? = if (formula1 == null) convertTime(expr1) else null
            // formula2 and value2 are mutually exclusive
            val formula2: String? = getFormulaFromTextExpression(expr2)
            val value2: Double? = if (formula2 == null) convertTime(expr2) else null
            return DVConstraint(
                DataValidationConstraint.ValidationType.TIME,
                comparisonOperator,
                formula1,
                formula2,
                value1,
                value2,
                null
            )
        }

        /**
         * Creates a date based data validation constraint. The text values entered for expr1 and expr2
         * can be either standard Excel formulas or formatted date values. If the expression starts
         * with '=' it is parsed as a formula, otherwise it is parsed as a formatted date (Excel uses
         * the same convention).  To parse formatted dates, a date format needs to be specified.  This
         * is contrary to Excel which uses the default short date format from the OS.
         * 
         * @param comparisonOperator constant from [com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.com.wxiwei.office.fc.ss.usermodel.DataValidationConstraint.OperatorType] enum
         * @param expr1 date formula (when first char is '=') or formatted date value
         * @param expr2 date formula (when first char is '=') or formatted date value
         * @param dateFormat ignored if both expr1 and expr2 are formulas.  Default value is "YYYY/MM/DD"
         * otherwise any other valid argument for <tt>SimpleDateFormat</tt> can be used
         * @see [SimpleDateFormat](http://java.sun.com/j2se/1.5.0/docs/api/java/text/DateFormat.html)
         */
        fun createDateConstraint(
            comparisonOperator: Int,
            expr1: String,
            expr2: String?,
            dateFormat: String?
        ): DVConstraint {
            requireNotNull(expr1) { "expr1 must be supplied" }
            DataValidationConstraint.OperatorType.validateSecondArg(comparisonOperator, expr2)
            val df = if (dateFormat == null) null else SimpleDateFormat(dateFormat)


            // formula1 and value1 are mutually exclusive
            val formula1: String? = getFormulaFromTextExpression(expr1)
            val value1: Double? = if (formula1 == null) convertDate(expr1, df) else null
            // formula2 and value2 are mutually exclusive
            val formula2: String? = getFormulaFromTextExpression(expr2)
            val value2: Double? = if (formula2 == null) convertDate(expr2, df) else null
            return DVConstraint(
                DataValidationConstraint.ValidationType.DATE,
                comparisonOperator,
                formula1,
                formula2,
                value1,
                value2,
                null
            )
        }

        /**
         * Distinguishes formula expressions from simple value expressions.  This logic is only
         * required by a few factory methods in this class that create data validation constraints
         * from more or less the same parameters that would have been entered in the Excel UI.  The
         * data validation dialog box uses the convention that formulas begin with '='.  Other methods
         * in this class follow the POI convention (formulas and values are distinct), so the '='
         * convention is not used there.
         * 
         * @param textExpr a formula or value expression
         * @return all text after '=' if textExpr begins with '='. Otherwise `null` if textExpr does not begin with '='
         */
        private fun getFormulaFromTextExpression(textExpr: String?): String? {
            if (textExpr == null) {
                return null
            }
            require(textExpr.length >= 1) { "Empty string is not a valid formula/value expression" }
            if (textExpr.get(0) == '=') {
                return textExpr.substring(1)
            }
            return null
        }


        /**
         * @return `null` if numberStr is `null`
         */
        private fun convertNumber(numberStr: String?): Double? {
            if (numberStr == null) {
                return null
            }
            try {
                return numberStr.toDouble()
            } catch (e: NumberFormatException) {
                throw RuntimeException(
                    ("The supplied text '" + numberStr
                            + "' could not be parsed as a number")
                )
            }
        }

        /**
         * @return `null` if timeStr is `null`
         */
        private fun convertTime(timeStr: String?): Double? {
            if (timeStr == null) {
                return null
            }
            return DateUtil.convertTime(timeStr)
        }

        /**
         * @param dateFormat pass `null` for default YYYYMMDD
         * @return `null` if timeStr is `null`
         */
        private fun convertDate(dateStr: String?, dateFormat: SimpleDateFormat?): Double? {
            if (dateStr == null) {
                return null
            }
            val dateVal: Date?
            if (dateFormat == null) {
                dateVal = parseYYYYMMDDDate(dateStr)
            } else {
                try {
                    dateVal = dateFormat.parse(dateStr)
                } catch (e: ParseException) {
                    throw RuntimeException(
                        ("Failed to parse date '" + dateStr
                                + "' using specified format '" + dateFormat + "'"), e
                    )
                }
            }
            return getExcelDate(dateVal)
        }

        fun createCustomFormulaConstraint(formula: String): DVConstraint {
            requireNotNull(formula) { "formula must be supplied" }
            return DVConstraint(
                DataValidationConstraint.ValidationType.FORMULA,
                DataValidationConstraint.OperatorType.IGNORED,
                formula,
                null,
                null,
                null,
                null
            )
        }

        /**
         * @return The parsed token array representing the formula or value specified.
         * Empty array if both formula and value are `null`
         */
        private fun convertDoubleFormula(
            formula: String?,
            value: Double?,
            sheet: HSSFSheet?
        ): Array<Ptg?>? {
//		if (formula == null) {
//			if (value == null) {
//				return Ptg.EMPTY_PTG_ARRAY;
//			}
//			return new Ptg[] { new NumberPtg(value.doubleValue()), };
//		}
//		if (value != null) {
//			throw new IllegalStateException("Both formula and value cannot be present");
//		}
//        HSSFWorkbook wb = sheet.getWorkbook();
//		return HSSFFormulaParser.parse(formula, wb, FormulaType.CELL, wb.getSheetIndex(sheet));
            return null
        }
    }
}
