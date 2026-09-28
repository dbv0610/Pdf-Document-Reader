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
package com.wxiwei.office.fc.hssf.formula.eval

import java.util.regex.Pattern
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.NumberFormatException
import kotlin.RuntimeException
import kotlin.String
import kotlin.Throws
import kotlin.math.floor

/**
 * Provides functionality for evaluating arguments to functions and operators.
 * 
 * @author Josh Micich
 * @author Brendan Nolan
 */
object OperandResolver {
    // Based on regular expression defined in JavaDoc at {@link java.lang.Double#valueOf}
    // modified to remove support for NaN, Infinity, Hexadecimal support and floating type suffixes
    private const val Digits = "(\\p{Digit}+)"
    private val Exp = "[eE][+-]?" + Digits
    private val fpRegex = ("[\\x00-\\x20]*" +
            "[+-]?(" +
            "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
            "(\\.(" + Digits + ")(" + Exp + ")?))))" +
            "[\\x00-\\x20]*")


    /**
     * Retrieves a single value from a variety of different argument types according to standard
     * Excel rules.  Does not perform any type conversion.
     * @param arg the evaluated argument as passed to the function or operator.
     * @param srcCellRow used when arg is a single column AreaRef
     * @param srcCellCol used when arg is a single row AreaRef
     * @return a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt> or <tt>BlankEval</tt>.
     * Never `null` or <tt>ErrorEval</tt>.
     * @throws EvaluationException(#VALUE!) if srcCellRow or srcCellCol do not properly index into
     * an AreaEval.  If the actual value retrieved is an ErrorEval, a corresponding
     * EvaluationException is thrown.
     */
    @Throws(EvaluationException::class)
    fun getSingleValue(arg: ValueEval?, srcCellRow: Int, srcCellCol: Int): ValueEval {
        val result: ValueEval?
        if (arg is RefEval) {
            result = arg.innerValueEval
        } else if (arg is AreaEval) {
            result = chooseSingleElementFromArea(arg, srcCellRow, srcCellCol)
        } else {
            result = arg
        }
        if (result is ErrorEval) {
            throw EvaluationException(result)
        }


        if (result is RefEval) {
            return getSingleValue(result, srcCellRow, srcCellCol)
        }

        return result!!
    }

    /**
     * Implements (some perhaps not well known) Excel functionality to select a single cell from an
     * area depending on the coordinates of the calling cell.  Here is an example demonstrating
     * both selection from a single row area and a single column area in the same formula.
     * 
     * <table border="1" cellpadding="1" cellspacing="1" summary="sample spreadsheet">
     * <tr><th>&nbsp;</th><th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th><th>&nbsp;C&nbsp;</th><th>&nbsp;D&nbsp;</th></tr>
     * <tr><th>1</th><td>15</td><td>20</td><td>25</td><td>&nbsp;</td></tr>
     * <tr><th>2</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>200</td></tr>
     * <tr><th>3</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>300</td></tr>
     * <tr><th>3</th><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>400</td></tr>
    </table> * 
     * 
     * If the formula "=1000+A1:B1+D2:D3" is put into the 9 cells from A2 to C4, the spreadsheet
     * will look like this:
     * 
     * <table border="1" cellpadding="1" cellspacing="1" summary="sample spreadsheet">
     * <tr><th>&nbsp;</th><th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th><th>&nbsp;C&nbsp;</th><th>&nbsp;D&nbsp;</th></tr>
     * <tr><th>1</th><td>15</td><td>20</td><td>25</td><td>&nbsp;</td></tr>
     * <tr><th>2</th><td>1215</td><td>1220</td><td>#VALUE!</td><td>200</td></tr>
     * <tr><th>3</th><td>1315</td><td>1320</td><td>#VALUE!</td><td>300</td></tr>
     * <tr><th>4</th><td>#VALUE!</td><td>#VALUE!</td><td>#VALUE!</td><td>400</td></tr>
    </table> * 
     * 
     * Note that the row area (A1:B1) does not include column C and the column area (D2:D3) does
     * not include row 4, so the values in C1(=25) and D4(=400) are not accessible to the formula
     * as written, but in the 4 cells A2:B3, the row and column selection works ok.
     *
     *
     * 
     * The same concept is extended to references across sheets, such that even multi-row,
     * multi-column areas can be useful.
     *
     *
     * 
     * Of course with carefully (or carelessly) chosen parameters, cyclic references can occur and
     * hence this method **can** throw a 'circular reference' EvaluationException.  Note that
     * this method does not attempt to detect cycles.  Every cell in the specified Area <tt>ae</tt>
     * has already been evaluated prior to this method call.  Any cell (or cell**s**) part of
     * <tt>ae</tt> that would incur a cyclic reference error if selected by this method, will
     * already have the value <t>ErrorEval.CIRCULAR_REF_ERROR upon entry to this method.  It
     * is assumed logic exists elsewhere to produce this behaviour.
     * 
     * @return whatever the selected cell's evaluated value is.  Never `null`. Never
     * <tt>ErrorEval</tt>.
     * @throws EvaluationException if there is a problem with indexing into the area, or if the
     * evaluated cell has an error.
    </t> */
    @Throws(EvaluationException::class)
    fun chooseSingleElementFromArea(
        ae: AreaEval,
        srcCellRow: Int, srcCellCol: Int
    ): ValueEval? {
        val result = chooseSingleElementFromAreaInternal(ae, srcCellRow, srcCellCol)
        if (result is ErrorEval) {
            throw EvaluationException(result)
        }
        return result
    }

    /**
     * @return possibly <tt>ErrorEval</tt>, and `null`
     */
    @Throws(EvaluationException::class)
    private fun chooseSingleElementFromAreaInternal(
        ae: AreaEval,
        srcCellRow: Int, srcCellCol: Int
    ): ValueEval? {
        if (false) {
            // this is too simplistic
            if (ae.containsRow(srcCellRow) && ae.containsColumn(srcCellCol)) {
                throw EvaluationException(ErrorEval.Companion.CIRCULAR_REF_ERROR)
            }
            /*
		Circular references are not dealt with directly here, but it is worth noting some issues.

		ANY one of the return statements in this method could return a cell that is identical
		to the one immediately being evaluated.  The evaluating cell is identified by srcCellRow,
		srcCellRow AND sheet.  The sheet is not available in any nearby calling method, so that's
		one reason why circular references are not easy to detect here. (The sheet of the returned
		cell can be obtained from ae if it is an Area3DEval.)

		Another reason there's little value in attempting to detect circular references here is
		that only direct circular references could be detected.  If the cycle involved two or more
		cells this method could not detect it.

		Logic to detect evaluation cycles of all kinds has been coded in EvaluationCycleDetector
		(and FormulaEvaluator).
		 */
        }

        if (ae.isColumn) {
            if (ae.isRow) {
                return ae.getRelativeValue(0, 0)
            }
            if (!ae.containsRow(srcCellRow)) {
                throw EvaluationException.Companion.invalidValue()
            }
            return ae.getAbsoluteValue(srcCellRow, ae.firstColumn)
        }
        if (!ae.isRow) {
            // multi-column, multi-row area
            if (ae.containsRow(srcCellRow) && ae.containsColumn(srcCellCol)) {
                return ae.getAbsoluteValue(ae.firstRow, ae.firstColumn)
            }
            throw EvaluationException.Companion.invalidValue()
        }
        if (!ae.containsColumn(srcCellCol)) {
            throw EvaluationException.Companion.invalidValue()
        }
        return ae.getAbsoluteValue(ae.firstRow, srcCellCol)
    }

    /**
     * Applies some conversion rules if the supplied value is not already an integer.<br></br>
     * Value is first coerced to a <tt>double</tt> ( See <tt>coerceValueToDouble()</tt> ).
     * Note - <tt>BlankEval</tt> is converted to `0`.
     *
     *
     * 
     * Excel typically converts doubles to integers by truncating toward negative infinity.<br></br>
     * The equivalent java code is:<br></br>
     * &nbsp;&nbsp;`return (int)Math.floor(d);`<br></br>
     * **not**:<br></br>
     * &nbsp;&nbsp;`return (int)d; // wrong - rounds toward zero`
     * 
     */
    @Throws(EvaluationException::class)
    fun coerceValueToInt(ev: ValueEval?): Int {
        if (ev === BlankEval.instance) {
            return 0
        }
        val d = coerceValueToDouble(ev)
        // Note - the standard java type conversion from double to int truncates toward zero.
        // but Math.floor() truncates toward negative infinity
        return floor(d).toInt()
    }

    /**
     * Applies some conversion rules if the supplied value is not already a number.
     * Note - <tt>BlankEval</tt> is converted to [NumberEval.ZERO].
     * @param ev must be a [NumberEval], [StringEval], [BoolEval] or
     * [BlankEval]
     * @return actual, parsed or interpreted double value (respectively).
     * @throws EvaluationException(#VALUE!) only if a StringEval is supplied and cannot be parsed
     * as a double (See <tt>parseDouble()</tt> for allowable formats).
     * @throws RuntimeException if the supplied parameter is not [NumberEval],
     * [StringEval], [BoolEval] or [BlankEval]
     */
    @Throws(EvaluationException::class)
    fun coerceValueToDouble(ev: ValueEval?): Double {
        if (ev === BlankEval.instance) {
            return 0.0
        }
        if (ev is NumericValueEval) {
            // this also handles booleans
            return ev.numberValue
        }
        if (ev is StringEval) {
            val dd = parseDouble(ev.stringValue)
            if (dd == null) {
                throw EvaluationException.Companion.invalidValue()
            }
            return dd
        }
        throw RuntimeException("Unexpected arg eval type (" + ev!!.javaClass.getName() + ")")
    }

    /**
     * Converts a string to a double using standard rules that Excel would use.<br></br>
     * Tolerates leading and trailing spaces, 
     *
     *
     * 
     * Doesn't support currency prefixes, commas, percentage signs or arithmetic operations strings.
     * 
     * Some examples:<br></br>
     * " 123 " -&gt; 123.0<br></br>
     * ".123" -&gt; 0.123<br></br>
     * "1E4" -&gt; 1000<br></br>
     * "-123" -&gt; -123.0<br></br>
     * These not supported yet:<br></br>
     * " $ 1,000.00 " -&gt; 1000.0<br></br>
     * "$1.25E4" -&gt; 12500.0<br></br>
     * "5**2" -&gt; 500<br></br>
     * "250%" -&gt; 2.5<br></br>
     * 
     * @return `null` if the specified text cannot be parsed as a number
     */
    fun parseDouble(pText: String): Double? {
        if (Pattern.matches(fpRegex, pText)) try {
            return pText.toDouble()
        } catch (e: NumberFormatException) {
            return null
        }
        else {
            return null
        }
    }

    /**
     * @param ve must be a <tt>NumberEval</tt>, <tt>StringEval</tt>, <tt>BoolEval</tt>, or <tt>BlankEval</tt>
     * @return the converted string value. never `null`
     */
    fun coerceValueToString(ve: ValueEval?): String {
        if (ve is StringValueEval) {
            val sve = ve
            return sve.stringValue
        }
        if (ve === BlankEval.instance) {
            return ""
        }
        throw IllegalArgumentException("Unexpected eval class (" + ve!!.javaClass.getName() + ")")
    }

    /**
     * @return `null` to represent blank values
     * @throws EvaluationException if ve is an ErrorEval, or if a string value cannot be converted
     */
    @Throws(EvaluationException::class)
    fun coerceValueToBoolean(ve: ValueEval?, stringsAreBlanks: Boolean): Boolean? {
        if (ve == null || ve === BlankEval.instance) {
            // TODO - remove 've == null' condition once AreaEval is fixed
            return null
        }
        if (ve is BoolEval) {
            return ve.booleanValue
        }

        if (ve === BlankEval.instance) {
            return null
        }

        if (ve is StringEval) {
            if (stringsAreBlanks) {
                return null
            }
            val str = ve.stringValue
            if (str.equals("true", ignoreCase = true)) {
                return true
            }
            if (str.equals("false", ignoreCase = true)) {
                return false
            }
            // else - string cannot be converted to boolean
            throw EvaluationException(ErrorEval.Companion.VALUE_INVALID)
        }

        if (ve is NumericValueEval) {
            val ne = ve
            val d = ne.numberValue
            if (d.isNaN()) {
                throw EvaluationException(ErrorEval.Companion.VALUE_INVALID)
            }
            return d != 0.0
        }
        if (ve is ErrorEval) {
            throw EvaluationException(ve)
        }
        throw RuntimeException("Unexpected eval (" + ve.javaClass.getName() + ")")
    }
}
