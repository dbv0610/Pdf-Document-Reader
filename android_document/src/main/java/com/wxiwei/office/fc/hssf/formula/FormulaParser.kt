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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.constant.fc.ErrorConstant
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadata
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry.Companion.getFunctionByName
import com.wxiwei.office.fc.hssf.formula.ptg.AbstractFunctionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AbstractFunctionPtg.Companion.isBuiltInFunctionName
import com.wxiwei.office.fc.hssf.formula.ptg.AddPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ArrayPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AttrPtg.Companion.sumSingle
import com.wxiwei.office.fc.hssf.formula.ptg.BoolPtg.Companion.valueOf
import com.wxiwei.office.fc.hssf.formula.ptg.ConcatPtg
import com.wxiwei.office.fc.hssf.formula.ptg.DividePtg
import com.wxiwei.office.fc.hssf.formula.ptg.EqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ErrPtg
import com.wxiwei.office.fc.hssf.formula.ptg.FuncPtg.Companion.create
import com.wxiwei.office.fc.hssf.formula.ptg.FuncVarPtg.Companion.create
import com.wxiwei.office.fc.hssf.formula.ptg.GreaterEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.GreaterThanPtg
import com.wxiwei.office.fc.hssf.formula.ptg.IntPtg
import com.wxiwei.office.fc.hssf.formula.ptg.IntPtg.Companion.isInRange
import com.wxiwei.office.fc.hssf.formula.ptg.LessEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.LessThanPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemAreaPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MissingArgPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MultiplyPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NamePtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NotEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NumberPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperandPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperationPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ParenthesisPtg
import com.wxiwei.office.fc.hssf.formula.ptg.PercentPtg
import com.wxiwei.office.fc.hssf.formula.ptg.PowerPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.RangePtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RefPtg
import com.wxiwei.office.fc.hssf.formula.ptg.StringPtg
import com.wxiwei.office.fc.hssf.formula.ptg.SubtractPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnaryMinusPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnaryPlusPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ValueOperatorPtg
import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants
import com.wxiwei.office.fc.ss.util.AreaReference
import com.wxiwei.office.fc.ss.util.CellReference
import java.util.Locale
import java.util.regex.Pattern

/**
 * This class parses a formula string into a List of tokens in RPN order.
 * Inspired by
 * Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor> ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> | <function>
 * <function> ::= <functionName> ([expression [, expression]*])
 * 
 * 
 * For POI internal use only
 * 
 * 
 * 
 * 
 * @author Avik Sengupta <avik at apache dot org>
 * @author Andrew C. oliver (acoliver at apache dot org)
 * @author Eric Ladner (eladner at goldinc dot com)
 * @author Cameron Riley (criley at ekmail.com)
 * @author Peter M. Murray (pete at quantrix dot com)
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 * @author Josh Micich
 * @author David Lewis (DLewis400 at gmail dot com)
</avik></functionName></function></function></cellRef></expression></number></factor></factor></mulop></factor></term></term></addop></term></expression> */
class FormulaParser private constructor(
    private val _formulaString: String,
    private val _book: FormulaParsingWorkbook?,
    private val _sheetIndex: Int
) {
    private class Identifier(val name: String?, val isQuoted: Boolean) {
        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName())
            sb.append(" [")
            if (this.isQuoted) {
                sb.append("'").append(this.name).append("'")
            } else {
                sb.append(this.name)
            }
            sb.append("]")
            return sb.toString()
        }
    }

    private class SheetIdentifier(val bookName: String?, val sheetIdentifier: Identifier) {
        override fun toString(): String {
            val sb = StringBuffer(64)
            sb.append(javaClass.getName())
            sb.append(" [")
            if (this.bookName != null) {
                sb.append(" [").append(sheetIdentifier.name).append("]")
            }
            if (sheetIdentifier.isQuoted) {
                sb.append("'").append(sheetIdentifier.name).append("'")
            } else {
                sb.append(sheetIdentifier.name)
            }
            sb.append("]")
            return sb.toString()
        }
    }

    private val _formulaLength: Int

    /** points at the next character to be read (after the [.look] char)  */
    private var _pointer = 0

    private var _rootNode: ParseNode? = null

    /**
     * Lookahead Character.
     * gets value '\0' when the input string is exhausted
     */
    private var look = 0.toChar()

    private val _ssVersion: SpreadsheetVersion


    /** Read New Character From Input Stream  */
    private fun GetChar() {
        // Check to see if we've walked off the end of the string.
        if (_pointer > _formulaLength) {
            throw RuntimeException("too far")
        }
        if (_pointer < _formulaLength) {
            look = _formulaString.get(_pointer)
        } else {
            // Just return if so and reset 'look' to something to keep
            // SkipWhitespace from spinning
            look = 0.toChar()
        }
        _pointer++
        //System.out.println("Got char: "+ look);
    }

    private fun resetPointer(ptr: Int) {
        _pointer = ptr
        if (_pointer <= _formulaLength) {
            look = _formulaString.get(_pointer - 1)
        } else {
            // Just return if so and reset 'look' to something to keep
            // SkipWhitespace from spinning
            look = 0.toChar()
        }
    }

    /** Report What Was Expected  */
    private fun expected(s: String?): RuntimeException {
        val msg: String?

        if (look == '=' && _formulaString.substring(0, _pointer - 1)
                .trim { it <= ' ' }.length < 1
        ) {
            msg = ("The specified formula '" + _formulaString
                    + "' starts with an equals sign which is not allowed.")
        } else {
            msg = ("Parse error near char " + (_pointer - 1) + " '" + look + "'"
                    + " in specified formula '" + _formulaString + "'. Expected "
                    + s)
        }
        return FormulaParseException(msg)
    }

    /** Skip Over Leading White Space  */
    private fun SkipWhite() {
        while (IsWhite(look)) {
            GetChar()
        }
    }

    /**
     * Consumes the next input character if it is equal to the one specified otherwise throws an
     * unchecked exception. This method does **not** consume whitespace (before or after the
     * matched character).
     */
    private fun Match(x: Char) {
        if (look != x) {
            throw expected("'" + x + "'")
        }
        GetChar()
    }

    /** Get a Number  */
    private fun GetNum(): String? {
        val value = StringBuffer()

        while (IsDigit(this.look)) {
            value.append(this.look)
            GetChar()
        }
        return if (value.length == 0) null else value.toString()
    }

    private fun parseRangeExpression(): ParseNode {
        var result = parseRangeable()
        var hasRange = false
        while (look == ':') {
            val pos = _pointer
            GetChar()
            val nextPart = parseRangeable()

            // Note - no range simplification here. An expr like "A1:B2:C3:D4:E5" should be
            // grouped into area ref pairs like: "(A1:B2):(C3:D4):E5"
            // Furthermore, Excel doesn't seem to simplify
            // expressions like "Sheet1!A1:Sheet1:B2" into "Sheet1!A1:B2"
            checkValidRangeOperand("LHS", pos, result)
            checkValidRangeOperand("RHS", pos, nextPart)

            val children = arrayOf<ParseNode>(result, nextPart)
            result = ParseNode(RangePtg.instance, children)
            hasRange = true
        }
        if (hasRange) {
            return augmentWithMemPtg(result)
        }
        return result
    }

    /**
     * Parses area refs (things which could be the operand of ':') and simple factors
     * Examples
     * <pre>
     * A$1
     * $A$1 :  $B1
     * A1 .......	C2
     * Sheet1 !$A1
     * a..b!A1
     * 'my sheet'!A1
     * .my.sheet!A1
     * my.named..range.
     * foo.bar(123.456, "abc")
     * 123.456
     * "abc"
     * true
    </pre> * 
     * 
     */
    private fun parseRangeable(): ParseNode {
        SkipWhite()
        var savePointer = _pointer
        val sheetIden = parseSheetName()
        if (sheetIden == null) {
            resetPointer(savePointer)
        } else {
            SkipWhite()
            savePointer = _pointer
        }

        val part1 = parseSimpleRangePart()
        if (part1 == null) {
            if (sheetIden != null) {
                if (look == '#') {  // error ref like MySheet!#REF!
                    return ParseNode(ErrPtg.valueOf(parseErrorLiteral()))
                } else {
                    throw FormulaParseException(
                        ("Cell reference expected after sheet name at index "
                                + _pointer + ".")
                    )
                }
            }
            return parseNonRange(savePointer)
        }
        val whiteAfterPart1: Boolean = IsWhite(look)
        if (whiteAfterPart1) {
            SkipWhite()
        }

        if (look == ':') {
            val colonPos = _pointer
            GetChar()
            SkipWhite()
            var part2 = parseSimpleRangePart()
            if (part2 != null && !part1.isCompatibleForArea(part2)) {
                // second part is not compatible with an area ref e.g. S!A1:S!B2
                // where S might be a sheet name (that looks like a column name)

                part2 = null
            }
            if (part2 == null) {
                // second part is not compatible with an area ref e.g. A1:OFFSET(B2, 1, 2)
                // reset and let caller use explicit range operator
                resetPointer(colonPos)
                if (!part1.isCell) {
                    val prefix: String?
                    if (sheetIden == null) {
                        prefix = ""
                    } else {
                        prefix = "'" + sheetIden.sheetIdentifier.name + '!'
                    }
                    throw FormulaParseException(prefix + part1.rep + "' is not a proper reference.")
                }
                return createAreaRefParseNode(sheetIden, part1, part2)
            }
            return createAreaRefParseNode(sheetIden, part1, part2)
        }

        if (look == '.') {
            GetChar()
            var dotCount = 1
            while (look == '.') {
                dotCount++
                GetChar()
            }
            val whiteBeforePart2: Boolean = IsWhite(look)

            SkipWhite()
            val part2 = parseSimpleRangePart()
            val part1And2 = _formulaString.substring(savePointer - 1, _pointer - 1)
            if (part2 == null) {
                if (sheetIden != null) {
                    throw FormulaParseException(
                        ("Complete area reference expected after sheet name at index "
                                + _pointer + ".")
                    )
                }
                return parseNonRange(savePointer)
            }


            if (whiteAfterPart1 || whiteBeforePart2) {
                if (part1.isRowOrColumn || part2.isRowOrColumn) {
                    // "A .. B" not valid syntax for "A:B"
                    // and there's no other valid expression that fits this grammar
                    throw FormulaParseException(
                        ("Dotted range (full row or column) expression '"
                                + part1And2 + "' must not contain whitespace.")
                    )
                }
                return createAreaRefParseNode(sheetIden, part1, part2)
            }

            if (dotCount == 1 && part1.isRow && part2.isRow) {
                // actually, this is looking more like a number
                return parseNonRange(savePointer)
            }

            if (part1.isRowOrColumn || part2.isRowOrColumn) {
                if (dotCount != 2) {
                    throw FormulaParseException(
                        ("Dotted range (full row or column) expression '" + part1And2
                                + "' must have exactly 2 dots.")
                    )
                }
            }
            return createAreaRefParseNode(sheetIden, part1, part2)
        }
        if (part1.isCell && isValidCellReference(part1.rep)) {
            return createAreaRefParseNode(sheetIden, part1, null)
        }
        if (sheetIden != null) {
            throw FormulaParseException(
                ("Second part of cell reference expected after sheet name at index "
                        + _pointer + ".")
            )
        }

        return parseNonRange(savePointer)
    }


    /**
     * Parses simple factors that are not primitive ranges or range components
     * i.e. '!', ':'(and equiv '...') do not appear
     * Examples
     * <pre>
     * my.named...range.
     * foo.bar(123.456, "abc")
     * 123.456
     * "abc"
     * true
    </pre> * 
     */
    private fun parseNonRange(savePointer: Int): ParseNode {
        resetPointer(savePointer)

        if (Character.isDigit(look)) {
            return ParseNode(parseNumber())
        }
        if (look == '"') {
            return ParseNode(StringPtg(parseStringLiteral()))
        }
        // from now on we can only be dealing with non-quoted identifiers
        // which will either be named ranges or functions
        val sb = StringBuilder()

        // defined names may begin with a letter or underscore
        if (!Character.isLetter(look) && look != '_') {
            throw expected("number, string, or defined name")
        }
        while (isValidDefinedNameChar(look)) {
            sb.append(look)
            GetChar()
        }
        SkipWhite()
        val name = sb.toString()
        if (look == '(') {
            return function(name)
        }
        if (name.equals("TRUE", ignoreCase = true) || name.equals("FALSE", ignoreCase = true)) {
            return ParseNode(valueOf(name.equals("TRUE", ignoreCase = true)))
        }
        checkNotNull(_book) { "Need book to evaluate name '" + name + "'" }
        val evalName = _book.getName(name, _sheetIndex)
        if (evalName == null) {
            throw FormulaParseException(
                ("Specified named range '"
                        + name + "' does not exist in the current workbook.")
            )
        }
        if (evalName.isRange) {
            return ParseNode(evalName.createPtg())
        }
        // TODO - what about NameX ?
        throw FormulaParseException(
            ("Specified name '"
                    + name + "' is not a range as expected.")
        )
    }

    /**
     * 
     * @param sheetIden may be `null`
     * @param part1
     * @param part2 may be `null`
     */
    @Throws(FormulaParseException::class)
    private fun createAreaRefParseNode(
        sheetIden: SheetIdentifier?, part1: SimpleRangePart,
        part2: SimpleRangePart?
    ): ParseNode {
        val extIx: Int
        if (sheetIden == null) {
            extIx = Int.MIN_VALUE
        } else {
            val sName = sheetIden.sheetIdentifier.name
            if (sheetIden.bookName == null) {
                extIx = _book!!.getExternalSheetIndex(sName)
            } else {
                extIx = _book!!.getExternalSheetIndex(sheetIden.bookName, sName)
            }
        }
        val ptg: Ptg?
        if (part2 == null) {
            val cr = part1.cellReference
            if (sheetIden == null) {
                ptg = RefPtg(cr)
            } else {
                ptg = Ref3DPtg(cr, extIx)
            }
        } else {
            val areaRef: AreaReference = createAreaRef(part1, part2)

            if (sheetIden == null) {
                ptg = AreaPtg(areaRef)
            } else {
                ptg = Area3DPtg(areaRef, extIx)
            }
        }
        return ParseNode(ptg)
    }

    /**
     * Create the formula parser, with the string that is to be
     * parsed against the supplied workbook.
     * A later call the parse() method to return ptg list in
     * rpn order, then call the getRPNPtg() to retrieve the
     * parse results.
     * This class is recommended only for single threaded use.
     * 
     * If you only have a usermodel.HSSFWorkbook, and not a
     * model.Workbook, then use the convenience method on
     * usermodel.HSSFFormulaEvaluator
     */
    init {
        _ssVersion =
            if (_book == null) SpreadsheetVersion.EXCEL97 else _book.spreadsheetVersion
        _formulaLength = _formulaString.length
    }

    /**
     * Parses out a potential LHS or RHS of a ':' intended to produce a plain AreaRef.  Normally these are
     * proper cell references but they could also be row or column refs like "$AC" or "10"
     * @return `null` (and leaves [._pointer] unchanged if a proper range part does not parse out
     */
    private fun parseSimpleRangePart(): SimpleRangePart? {
        var ptr = _pointer - 1 // TODO avoid StringIndexOutOfBounds
        var hasDigits = false
        var hasLetters = false
        while (ptr < _formulaLength) {
            val ch = _formulaString.get(ptr)
            if (Character.isDigit(ch)) {
                hasDigits = true
            } else if (Character.isLetter(ch)) {
                hasLetters = true
            } else if (ch == '$' || ch == '_') {
                //
            } else {
                break
            }
            ptr++
        }
        if (ptr <= _pointer - 1) {
            return null
        }
        val rep = _formulaString.substring(_pointer - 1, ptr)
        if (!CELL_REF_PATTERN.matcher(rep).matches()) {
            return null
        }
        // Check range bounds against grid max
        if (hasLetters && hasDigits) {
            if (!isValidCellReference(rep)) {
                return null
            }
        } else if (hasLetters) {
            if (!CellReference.isColumnWithnRange(rep.replace("$", ""), _ssVersion)) {
                return null
            }
        } else if (hasDigits) {
            val i: Int
            try {
                i = rep.replace("$", "").toInt()
            } catch (e: NumberFormatException) {
                return null
            }
            if (i < 1 || i > 65536) {
                return null
            }
        } else {
            // just dollars ? can this happen?
            return null
        }


        resetPointer(ptr + 1) // stepping forward
        return SimpleRangePart(rep, hasLetters, hasDigits)
    }


    /**
     * A1, $A1, A$1, $A$1, A, 1
     */
    private class SimpleRangePart(val rep: String, hasLetters: Boolean, hasNumbers: Boolean) {
        private enum class Type {
            CELL, ROW, COLUMN;

            companion object {
                fun get(hasLetters: Boolean, hasDigits: Boolean): Type {
                    if (hasLetters) {
                        return if (hasDigits) Type.CELL else Type.COLUMN
                    }
                    require(hasDigits) { "must have either letters or numbers" }
                    return Type.ROW
                }
            }
        }

        private val _type: Type

        init {
            _type = Type.Companion.get(hasLetters, hasNumbers)
        }

        val isCell: Boolean
            get() = _type == Type.CELL

        val isRowOrColumn: Boolean
            get() = _type != Type.CELL

        val cellReference: CellReference
            get() {
                check(_type == Type.CELL) { "Not applicable to this type" }
                return CellReference(this.rep)
            }

        val isColumn: Boolean
            get() = _type == Type.COLUMN

        val isRow: Boolean
            get() = _type == Type.ROW

        /**
         * @return `true` if the two range parts can be combined in an
         * [AreaPtg] ( Note - the explicit range operator (:) may still be valid
         * when this method returns `false` )
         */
        fun isCompatibleForArea(part2: SimpleRangePart): Boolean {
            return _type == part2._type
        }

        override fun toString(): String {
            val sb = StringBuilder(64)
            sb.append(javaClass.getName()).append(" [")
            sb.append(this.rep)
            sb.append("]")
            return sb.toString()
        }
    }

    /**
     * Note - caller should reset [._pointer] upon `null` result
     * @return The sheet name as an identifier `null` if '!' is not found in the right place
     */
    private fun parseSheetName(): SheetIdentifier? {
        val bookName: String?
        if (look == '[') {
            val sb = StringBuilder()
            GetChar()
            while (look != ']') {
                sb.append(look)
                GetChar()
            }
            GetChar()
            bookName = sb.toString()
        } else {
            bookName = null
        }

        if (look == '\'') {
            val sb = StringBuffer()

            Match('\'')
            var done = look == '\''
            while (!done) {
                sb.append(look)
                GetChar()
                if (look == '\'') {
                    Match('\'')
                    done = look != '\''
                }
            }

            val iden = Identifier(sb.toString(), true)
            // quoted identifier - can't concatenate anything more
            SkipWhite()
            if (look == '!') {
                GetChar()
                return SheetIdentifier(bookName, iden)
            }
            return null
        }

        // unquoted sheet names must start with underscore or a letter
        if (look == '_' || Character.isLetter(look)) {
            val sb = StringBuilder()
            // can concatenate idens with dots
            while (isUnquotedSheetNameChar(look)) {
                sb.append(look)
                GetChar()
            }
            SkipWhite()
            if (look == '!') {
                GetChar()
                return SheetIdentifier(bookName, Identifier(sb.toString(), false))
            }
            return null
        }
        return null
    }

    /**
     * @return `true` if the specified name is a valid cell reference
     */
    private fun isValidCellReference(str: String): Boolean {
        //check range bounds against grid max
        var result =
            CellReference.classifyCellReference(str, _ssVersion) == CellReference.NameType.CELL

        if (result) {
            /**
             * Check if the argument is a function. Certain names can be either a cell reference or a function name
             * depending on the contenxt. Compare the following examples in Excel 2007:
             * (a) LOG10(100) + 1
             * (b) LOG10 + 1
             * In (a) LOG10 is a name of a built-in function. In (b) LOG10 is a cell reference
             */
            val isFunc = getFunctionByName(str.uppercase(Locale.getDefault())) != null
            if (isFunc) {
                val savePointer = _pointer
                resetPointer(_pointer + str.length)
                SkipWhite()
                // open bracket indicates that the argument is a function,
                // the returning value should be false, i.e. "not a valid cell reference"
                result = look != '('
                resetPointer(savePointer)
            }
        }
        return result
    }


    /**
     * Note - Excel function names are 'case aware but not case sensitive'.  This method may end
     * up creating a defined name record in the workbook if the specified name is not an internal
     * Excel function, and has not been encountered before.
     * 
     * @param name case preserved function name (as it was entered/appeared in the formula).
     */
    private fun function(name: String): ParseNode {
        var nameToken: Ptg? = null
        if (!isBuiltInFunctionName(name)) {
            // user defined function
            // in the token tree, the name is more or less the first argument

            checkNotNull(_book) { "Need book to evaluate name '" + name + "'" }
            val hName = _book.getName(name, _sheetIndex)
            if (hName == null) {
                nameToken = _book.getNameXPtg(name)
                if (nameToken == null) {
                    throw FormulaParseException(
                        ("Name '" + name
                                + "' is completely unknown in the current workbook")
                    )
                }
            } else {
                if (!hName.isFunctionName) {
                    throw FormulaParseException(
                        ("Attempt to use name '" + name
                                + "' as a function, but defined name in workbook does not refer to a function")
                    )
                }

                // calls to user-defined functions within the workbook
                // get a Name token which points to a defined name record
                nameToken = hName.createPtg()
            }
        }

        Match('(')
        val args = Arguments()
        Match(')')

        return getFunction(name, nameToken, args)
    }

    /**
     * Generates the variable function ptg for the formula.
     * 
     * 
     * For IF Formulas, additional PTGs are added to the tokens
     * @param name a [NamePtg] or [NameXPtg] or `null`
     * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
     */
    private fun getFunction(name: String, namePtg: Ptg?, args: Array<ParseNode>): ParseNode {
        val fm = getFunctionByName(name.uppercase(Locale.getDefault()))
        val numArgs = args.size
        if (fm == null) {
            checkNotNull(namePtg) { "NamePtg must be supplied for external functions" }
            // must be external function
            val allArgs = arrayOf<ParseNode>(ParseNode(namePtg)) + args
            return ParseNode(create(name, numArgs + 1), allArgs)
        }

        check(namePtg == null) { "NamePtg no applicable to internal functions" }
        val isVarArgs = !fm.hasFixedArgsLength()
        val funcIx = fm.index
        if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_SUM.toInt() && args.size == 1) {
            // Excel encodes the sum of a single argument as tAttrSum
            // POI does the same for consistency, but this is not critical
            return ParseNode(sumSingle, args)
            // The code below would encode tFuncVar(SUM) which seems to do no harm
        }
        validateNumArgs(args.size, fm)

        val retval: AbstractFunctionPtg?
        if (isVarArgs) {
            retval = create(name, numArgs)
        } else {
            retval = create(funcIx)
        }
        return ParseNode(retval, args)
    }

    private fun validateNumArgs(numArgs: Int, fm: FunctionMetadata) {
        if (numArgs < fm.minParams) {
            var msg = "Too few arguments to function '" + fm.name + "'. "
            if (fm.hasFixedArgsLength()) {
                msg += "Expected " + fm.minParams
            } else {
                msg += "At least " + fm.minParams + " were expected"
            }
            msg += " but got " + numArgs + "."
            throw FormulaParseException(msg)
        }
        //the maximum number of arguments depends on the Excel version
        val maxArgs: Int
        if (fm.hasUnlimitedVarags()) {
            if (_book != null) {
                maxArgs = _book.spreadsheetVersion.getMaxFunctionArgs()
            } else {
                //_book can be omitted by test cases
                maxArgs = fm.maxParams // just use BIFF8
            }
        } else {
            maxArgs = fm.maxParams
        }

        if (numArgs > maxArgs) {
            var msg = "Too many arguments to function '" + fm.name + "'. "
            if (fm.hasFixedArgsLength()) {
                msg += "Expected " + maxArgs
            } else {
                msg += "At most " + maxArgs + " were expected"
            }
            msg += " but got " + numArgs + "."
            throw FormulaParseException(msg)
        }
    }

    /** get arguments to a function  */
    private fun Arguments(): Array<ParseNode> {
        //average 2 args per function
        val temp: MutableList<ParseNode> = ArrayList<ParseNode>(2)
        SkipWhite()
        if (look == ')') {
            return ParseNode.Companion.EMPTY_ARRAY
        }

        var missedPrevArg = true
        var numArgs = 0
        while (true) {
            SkipWhite()
            if (isArgumentDelimiter(look)) {
                if (missedPrevArg) {
                    temp.add(ParseNode(MissingArgPtg.instance))
                    numArgs++
                }
                if (look == ')') {
                    break
                }
                Match(',')
                missedPrevArg = true
                continue
            }
            temp.add(comparisonExpression())
            numArgs++
            missedPrevArg = false
            SkipWhite()
            if (!isArgumentDelimiter(look)) {
                throw expected("',' or ')'")
            }
        }
        val result = temp.toTypedArray()
        return result
    }

    /** Parse and Translate a Math Factor   */
    private fun powerFactor(): ParseNode {
        var result = percentFactor()
        while (true) {
            SkipWhite()
            if (look != '^') {
                return result
            }
            Match('^')
            val other = percentFactor()
            result = ParseNode(PowerPtg.instance, result, other)
        }
    }

    private fun percentFactor(): ParseNode {
        var result = parseSimpleFactor()
        while (true) {
            SkipWhite()
            if (look != '%') {
                return result
            }
            Match('%')
            result = ParseNode(PercentPtg.instance, result)
        }
    }


    /**
     * factors (without ^ or % )
     */
    private fun parseSimpleFactor(): ParseNode {
        SkipWhite()
        when (look) {
            '#' -> return ParseNode(ErrPtg.valueOf(parseErrorLiteral()))
            '-' -> {
                Match('-')
                return parseUnary(false)
            }

            '+' -> {
                Match('+')
                return parseUnary(true)
            }

            '(' -> {
                Match('(')
                val inside = comparisonExpression()
                Match(')')
                return ParseNode(ParenthesisPtg.instance, inside)
            }

            '"' -> return ParseNode(StringPtg(parseStringLiteral()))
            '{' -> {
                Match('{')
                val arrayNode = parseArray()
                Match('}')
                return arrayNode
            }
        }
        if (IsAlpha(look) || Character.isDigit(look) || look == '\'' || look == '[') {
            return parseRangeExpression()
        }
        if (look == '.') {
            return ParseNode(parseNumber())
        }
        throw expected("cell ref or constant literal")
    }


    private fun parseUnary(isPlus: Boolean): ParseNode {
        val numberFollows = IsDigit(look) || look == '.'
        val factor = powerFactor()

        if (numberFollows) {
            // + or - directly next to a number is parsed with the number

            var token = factor.token
            if (token is NumberPtg) {
                if (isPlus) {
                    return factor
                }
                token = NumberPtg(-token.value)
                return ParseNode(token)
            }
            if (token is IntPtg) {
                if (isPlus) {
                    return factor
                }
                val intVal = token.value
                // note - cannot use IntPtg for negatives
                token = NumberPtg(-intVal.toDouble())
                return ParseNode(token)
            }
        }
        return ParseNode(if (isPlus) UnaryPlusPtg.instance else UnaryMinusPtg.instance, factor)
    }

    private fun parseArray(): ParseNode {
        val rowsData: MutableList<Array<Any?>?> = ArrayList<Array<Any?>?>()
        while (true) {
            val singleRowData = parseArrayRow()
            rowsData.add(singleRowData)
            if (look == '}') {
                break
            }
            if (look != ';') {
                throw expected("'}' or ';'")
            }
            Match(';')
        }
        val nRows = rowsData.size
        val values2d = rowsData.toTypedArray()
        val nColumns = values2d[0]!!.size
        checkRowLengths(values2d, nColumns)

        return ParseNode(ArrayPtg(values2d))
    }

    private fun checkRowLengths(values2d: Array<Array<Any?>?>, nColumns: Int) {
        for (i in values2d.indices) {
            val rowLen = values2d[i]!!.size
            if (rowLen != nColumns) {
                throw FormulaParseException(
                    ("Array row " + i + " has length " + rowLen
                            + " but row 0 has length " + nColumns)
                )
            }
        }
    }

    private fun parseArrayRow(): Array<Any?> {
        val temp: MutableList<Any?> = ArrayList<Any?>()
        while (true) {
            temp.add(parseArrayItem())
            SkipWhite()
            when (look) {
                '}', ';' -> {}
                ',' -> {
                    Match(',')
                    continue
                }

                else -> throw expected("'}' or ','")

            }
            break
        }

        val result = temp.toTypedArray()
        return result
    }

    private fun parseArrayItem(): Any? {
        SkipWhite()
        when (look) {
            '"' -> return parseStringLiteral()
            '#' -> return ErrorConstant.valueOf(parseErrorLiteral())
            'F', 'f', 'T', 't' -> return parseBooleanLiteral()
            '-' -> {
                Match('-')
                SkipWhite()
                return convertArrayNumber(parseNumber(), false)
            }
        }
        // else assume number
        return convertArrayNumber(parseNumber(), true)
    }

    private fun parseBooleanLiteral(): Boolean {
        val iden = parseUnquotedIdentifier()
        if ("TRUE".equals(iden, ignoreCase = true)) {
            return java.lang.Boolean.TRUE
        }
        if ("FALSE".equals(iden, ignoreCase = true)) {
            return java.lang.Boolean.FALSE
        }
        throw expected("'TRUE' or 'FALSE'")
    }

    private fun parseNumber(): Ptg {
        var number2: String? = null
        var exponent: String? = null
        val number1 = GetNum()

        if (look == '.') {
            GetChar()
            number2 = GetNum()
        }

        if (look == 'E') {
            GetChar()

            var sign = ""
            if (look == '+') {
                GetChar()
            } else if (look == '-') {
                GetChar()
                sign = "-"
            }

            val number = GetNum()
            if (number == null) {
                throw expected("Integer")
            }
            exponent = sign + number
        }

        if (number1 == null && number2 == null) {
            throw expected("Integer")
        }

        return getNumberPtgFromString(number1, number2, exponent)
    }


    private fun parseErrorLiteral(): Int {
        Match('#')
        val part1 = parseUnquotedIdentifier()!!.uppercase(Locale.getDefault())
        if (part1 == null) {
            throw expected("remainder of error constant literal")
        }

        when (part1.get(0)) {
            'V' -> {
                if (part1 == "VALUE") {
                    Match('!')
                    return ErrorConstants.ERROR_VALUE
                }
                throw expected("#VALUE!")
            }

            'R' -> {
                if (part1 == "REF") {
                    Match('!')
                    return ErrorConstants.ERROR_REF
                }
                throw expected("#REF!")
            }

            'D' -> {
                if (part1 == "DIV") {
                    Match('/')
                    Match('0')
                    Match('!')
                    return ErrorConstants.ERROR_DIV_0
                }
                throw expected("#DIV/0!")
            }

            'N' -> {
                if (part1 == "NAME") {
                    Match('?') // only one that ends in '?'
                    return ErrorConstants.ERROR_NAME
                }
                if (part1 == "NUM") {
                    Match('!')
                    return ErrorConstants.ERROR_NUM
                }
                if (part1 == "NULL") {
                    Match('!')
                    return ErrorConstants.ERROR_NULL
                }
                if (part1 == "N") {
                    Match('/')
                    if (look != 'A' && look != 'a') {
                        throw expected("#N/A")
                    }
                    Match(look)
                    // Note - no '!' or '?' suffix
                    return ErrorConstants.ERROR_NA
                }
                throw expected("#NAME?, #NUM!, #NULL! or #N/A")
            }
        }
        throw expected("#VALUE!, #REF!, #DIV/0!, #NAME?, #NUM!, #NULL! or #N/A")
    }

    private fun parseUnquotedIdentifier(): String? {
        if (look == '\'') {
            throw expected("unquoted identifier")
        }
        val sb = StringBuilder()
        while (Character.isLetterOrDigit(look) || look == '.') {
            sb.append(look)
            GetChar()
        }
        if (sb.length < 1) {
            return null
        }

        return sb.toString()
    }

    private fun parseStringLiteral(): String {
        Match('"')

        val token = StringBuffer()
        while (true) {
            if (look == '"') {
                GetChar()
                if (look != '"') {
                    break
                }
            }
            token.append(look)
            GetChar()
        }
        return token.toString()
    }

    /** Parse and Translate a Math Term  */
    private fun Term(): ParseNode {
        var result = powerFactor()
        while (true) {
            SkipWhite()
            val operator: Ptg?
            when (look) {
                '*' -> {
                    Match('*')
                    operator = MultiplyPtg.instance
                }

                '/' -> {
                    Match('/')
                    operator = DividePtg.instance
                }

                else -> return result // finished with Term
            }
            val other = powerFactor()
            result = ParseNode(operator, result, other)
        }
    }

    private fun unionExpression(): ParseNode {
        var result = comparisonExpression()
        var hasUnions = false
        while (true) {
            SkipWhite()
            when (look) {
                ',' -> {
                    GetChar()
                    hasUnions = true
                    val other = comparisonExpression()
                    result = ParseNode(UnionPtg.instance, result, other)
                    continue
                }
            }
            if (hasUnions) {
                return augmentWithMemPtg(result)
            }
            return result
        }
    }

    private fun comparisonExpression(): ParseNode {
        var result = concatExpression()
        while (true) {
            SkipWhite()
            when (look) {
                '=', '>', '<' -> {
                    val comparisonToken = this.comparisonToken
                    val other = concatExpression()
                    result = ParseNode(comparisonToken, result, other)
                    continue
                }
            }
            return result // finished with predicate expression
        }
    }

    private val comparisonToken: Ptg
        get() {
            if (look == '=') {
                Match(look)
                return EqualPtg.instance
            }
            val isGreater = look == '>'
            Match(look)
            if (isGreater) {
                if (look == '=') {
                    Match('=')
                    return GreaterEqualPtg.instance
                }
                return GreaterThanPtg.instance
            }
            when (look) {
                '=' -> {
                    Match('=')
                    return LessEqualPtg.instance
                }

                '>' -> {
                    Match('>')
                    return NotEqualPtg.instance
                }
            }
            return LessThanPtg.instance
        }


    private fun concatExpression(): ParseNode {
        var result = additiveExpression()
        while (true) {
            SkipWhite()
            if (look != '&') {
                break // finished with concat expression
            }
            Match('&')
            val other = additiveExpression()
            result = ParseNode(ConcatPtg.instance, result, other)
        }
        return result
    }


    /** Parse and Translate an Expression  */
    private fun additiveExpression(): ParseNode {
        var result = Term()
        while (true) {
            SkipWhite()
            val operator: Ptg?
            when (look) {
                '+' -> {
                    Match('+')
                    operator = AddPtg.instance
                }

                '-' -> {
                    Match('-')
                    operator = SubtractPtg.instance
                }

                else -> return result // finished with additive expression
            }
            val other = Term()
            result = ParseNode(operator, result, other)
        }
    }


    //{--------------------------------------------------------------}
    //{ Parse and Translate an Assignment Statement }
    /**
     * procedure Assignment;
     * var Name: string[8];
     * begin
     * Name := GetName;
     * Match('=');
     * Expression;
     * 
     * end;
     */
    /**
     * API call to execute the parsing of the formula
     * 
     */
    private fun parse() {
        _pointer = 0
        GetChar()
        _rootNode = unionExpression()

        if (_pointer <= _formulaLength) {
            val msg = ("Unused input [" + _formulaString.substring(_pointer - 1)
                    + "] after attempting to parse the formula [" + _formulaString + "]")
            throw FormulaParseException(msg)
        }
    }

    private fun getRPNPtg(formulaType: Int): Array<Ptg?>? {
        val oct = OperandClassTransformer(formulaType)
        // RVA is for 'operand class': 'reference', 'value', 'array'
        val rootNode = _rootNode!!
        oct.transformFormula(rootNode)
        return ParseNode.Companion.toTokenArray(rootNode)
    }

    companion object {
        private const val TAB = '\t'

        /**
         * Parse a formula into a array of tokens
         * 
         * @param formula     the formula to parse
         * @param workbook    the parent workbook
         * @param formulaType the type of the formula, see [FormulaType]
         * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
         * The sheet index is required to resolve sheet-level names. `-1` means that
         * the scope of the name will be ignored and  the parser will match names only by name
         * 
         * @return array of parsed tokens
         * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
         */
        @JvmStatic
        fun parse(
            formula: String,
            workbook: FormulaParsingWorkbook?,
            formulaType: Int,
            sheetIndex: Int
        ): Array<Ptg?>? {
            val fp = FormulaParser(formula, workbook, sheetIndex)
            fp.parse()
            return fp.getRPNPtg(formulaType)
        }

        /** Recognize an Alpha Character  */
        private fun IsAlpha(c: Char): Boolean {
            return Character.isLetter(c) || c == '$' || c == '_'
        }

        /** Recognize a Decimal Digit  */
        private fun IsDigit(c: Char): Boolean {
            return Character.isDigit(c)
        }

        /** Recognize White Space  */
        private fun IsWhite(c: Char): Boolean {
            return c == ' ' || c == TAB
        }

        private fun augmentWithMemPtg(root: ParseNode): ParseNode {
            val memPtg: Ptg
            if (needsMemFunc(root)) {
                memPtg = MemFuncPtg(root.encodedSize)
            } else {
                memPtg = MemAreaPtg(root.encodedSize)
            }
            return ParseNode(memPtg, root)
        }

        /**
         * From OOO doc: "Whenever one operand of the reference subexpression is a function,
         * a defined name, a 3D reference, or an external reference (and no error occurs),
         * a tMemFunc token is used"
         * 
         */
        private fun needsMemFunc(root: ParseNode): Boolean {
            val token = root.token
            if (token is AbstractFunctionPtg) {
                return true
            }
            if (token is ExternSheetReferenceToken) { // 3D refs
                return true
            }
            if (token is NamePtg || token is NameXPtg) { // 3D refs
                return true
            }

            if (token is OperationPtg || token is ParenthesisPtg) {
                // expect RangePtg, but perhaps also UnionPtg, IntersectionPtg etc
                for (child in root.children) {
                    if (needsMemFunc(child)) {
                        return true
                    }
                }
                return false
            }
            if (token is OperandPtg) {
                return false
            }
            if (token is OperationPtg) {
                return true
            }

            return false
        }

        /**
         * @param currentParsePosition used to format a potential error message
         */
        private fun checkValidRangeOperand(
            sideName: String?,
            currentParsePosition: Int,
            pn: ParseNode
        ) {
            if (!isValidRangeOperand(pn)) {
                throw FormulaParseException(
                    ("The " + sideName
                            + " of the range operator ':' at position "
                            + currentParsePosition + " is not a proper reference.")
                )
            }
        }

        /**
         * @return `false` if sub-expression represented the specified ParseNode definitely
         * cannot appear on either side of the range (':') operator
         */
        private fun isValidRangeOperand(a: ParseNode): Boolean {
            val tkn = a.token
            // Note - order is important for these instance-of checks
            if (tkn is OperandPtg) {
                // notably cell refs and area refs
                return true
            }

            // next 2 are special cases of OperationPtg
            if (tkn is AbstractFunctionPtg) {
                val afp = tkn
                val returnClass = afp.defaultOperandClass
                return Ptg.CLASS_REF == returnClass
            }
            if (tkn is ValueOperatorPtg) {
                return false
            }
            if (tkn is OperationPtg) {
                return true
            }

            // one special case of ControlPtg
            if (tkn is ParenthesisPtg) {
                // parenthesis Ptg should have only one child
                return isValidRangeOperand(a.children[0])
            }

            // one special case of ScalarConstantPtg
            if (tkn === ErrPtg.REF_INVALID) {
                return true
            }

            // All other ControlPtgs and ScalarConstantPtgs cannot be used with ':'
            return false
        }

        /**
         * 
         * @return `true` if the specified character may be used in a defined name
         */
        private fun isValidDefinedNameChar(ch: Char): Boolean {
            if (Character.isLetterOrDigit(ch)) {
                return true
            }
            when (ch) {
                '.', '_', '?', '\\' -> return true
            }
            return false
        }

        private fun createAreaRef(part1: SimpleRangePart, part2: SimpleRangePart): AreaReference {
            if (!part1.isCompatibleForArea(part2)) {
                throw FormulaParseException(
                    ("has incompatible parts: '"
                            + part1.rep + "' and '" + part2.rep + "'.")
                )
            }
            if (part1.isRow) {
                return AreaReference.getWholeRow(part1.rep, part2.rep)
            }
            if (part1.isColumn) {
                return AreaReference.getWholeColumn(
                    part1.rep,
                    part2.rep
                )
            }
            return AreaReference(
                part1.cellReference,
                part2.cellReference
            )
        }

        /**
         * Matches a zero or one letter-runs followed by zero or one digit-runs.
         * Either or both runs man optionally be prefixed with a single '$'.
         * (copied+modified from [CellReference.CELL_REF_PATTERN])
         */
        private val CELL_REF_PATTERN: Pattern = Pattern.compile("(\\$?[A-Za-z]+)?(\\$?[0-9]+)?")

        /**
         * very similar to [SheetNameFormatter.isSpecialChar]
         */
        private fun isUnquotedSheetNameChar(ch: Char): Boolean {
            if (Character.isLetterOrDigit(ch)) {
                return true
            }
            when (ch) {
                '.', '_' -> return true
            }
            return false
        }

        private fun isArgumentDelimiter(ch: Char): Boolean {
            return ch == ',' || ch == ')'
        }

        private fun convertArrayNumber(ptg: Ptg, isPositive: Boolean): Double {
            var value: Double
            if (ptg is IntPtg) {
                value = ptg.value.toDouble()
            } else if (ptg is NumberPtg) {
                value = ptg.value
            } else {
                throw RuntimeException("Unexpected ptg (" + ptg.javaClass.getName() + ")")
            }
            if (!isPositive) {
                value = -value
            }
            return value
        }

        /**
         * Get a PTG for an integer from its string representation.
         * return Int or Number Ptg based on size of input
         */
        private fun getNumberPtgFromString(
            number1: String?,
            number2: String?,
            exponent: String?
        ): Ptg {
            val number = StringBuffer()

            if (number2 == null) {
                number.append(number1)

                if (exponent != null) {
                    number.append('E')
                    number.append(exponent)
                }

                val numberStr = number.toString()
                val intVal: Int
                try {
                    intVal = numberStr.toInt()
                } catch (e: NumberFormatException) {
                    return NumberPtg(numberStr)
                }
                if (isInRange(intVal)) {
                    return IntPtg(intVal)
                }
                return NumberPtg(numberStr)
            }

            if (number1 != null) {
                number.append(number1)
            }

            number.append('.')
            number.append(number2)

            if (exponent != null) {
                number.append('E')
                number.append(exponent)
            }

            return NumberPtg(number.toString())
        }
    }
}
