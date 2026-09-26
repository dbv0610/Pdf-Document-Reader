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

import com.wxiwei.office.fc.ss.SpreadsheetVersion
import com.wxiwei.office.fc.ss.util.CellReference
import java.util.regex.Pattern

/**
 * Formats sheet names for use in formula expressions.
 * 
 * @author Josh Micich
 */
object SheetNameFormatter {
    private const val DELIMITER = '\''

    /**
     * Matches a single cell ref with no absolute ('$') markers
     */
    private val CELL_REF_PATTERN: Pattern = Pattern.compile("([A-Za-z]+)([0-9]+)")

    /**
     * Used to format sheet names as they would appear in cell formula expressions.
     * @return the sheet name unchanged if there is no need for delimiting.  Otherwise the sheet
     * name is enclosed in single quotes (').  Any single quotes which were already present in the
     * sheet name will be converted to double single quotes ('').
     */
    @JvmStatic
    fun format(rawSheetName: String): String {
        val sb = StringBuffer(rawSheetName.length + 2)
        appendFormat(sb, rawSheetName)
        return sb.toString()
    }

    /**
     * Convenience method for ([.format]) when a StringBuffer is already available.
     * 
     * @param out - sheet name will be appended here possibly with delimiting quotes
     */
    @JvmStatic
    fun appendFormat(out: StringBuffer, rawSheetName: String) {
        val needsQuotes = needsDelimiting(rawSheetName)
        if (needsQuotes) {
            out.append(DELIMITER)
            appendAndEscape(out, rawSheetName)
            out.append(DELIMITER)
        } else {
            out.append(rawSheetName)
        }
    }

    fun appendFormat(out: StringBuffer, workbookName: String, rawSheetName: String) {
        val needsQuotes = needsDelimiting(workbookName) || needsDelimiting(rawSheetName)
        if (needsQuotes) {
            out.append(DELIMITER)
            out.append('[')
            appendAndEscape(out, workbookName.replace('[', '(').replace(']', ')'))
            out.append(']')
            appendAndEscape(out, rawSheetName)
            out.append(DELIMITER)
        } else {
            out.append('[')
            out.append(workbookName)
            out.append(']')
            out.append(rawSheetName)
        }
    }

    private fun appendAndEscape(sb: StringBuffer, rawSheetName: String) {
        val len = rawSheetName.length
        for (i in 0..<len) {
            val ch = rawSheetName.get(i)
            if (ch == DELIMITER) {
                // single quotes (') are encoded as ('')
                sb.append(DELIMITER)
            }
            sb.append(ch)
        }
    }

    private fun needsDelimiting(rawSheetName: String): Boolean {
        val len = rawSheetName.length
        if (len < 1) {
            throw RuntimeException("Zero length string is an invalid sheet name")
        }
        if (Character.isDigit(rawSheetName.get(0))) {
            // sheet name with digit in the first position always requires delimiting
            return true
        }
        for (i in 0..<len) {
            val ch = rawSheetName.get(i)
            if (isSpecialChar(ch)) {
                return true
            }
        }
        if (Character.isLetter(rawSheetName.get(0))
            && Character.isDigit(rawSheetName.get(len - 1))
        ) {
            // note - values like "A$1:$C$20" don't get this far 
            if (nameLooksLikePlainCellReference(rawSheetName)) {
                return true
            }
        }
        if (nameLooksLikeBooleanLiteral(rawSheetName)) {
            return true
        }
        // Error constant literals all contain '#' and other special characters
        // so they don't get this far
        return false
    }

    private fun nameLooksLikeBooleanLiteral(rawSheetName: String): Boolean {
        when (rawSheetName.get(0)) {
            'T', 't' -> return "TRUE".equals(rawSheetName, ignoreCase = true)
            'F', 'f' -> return "FALSE".equals(rawSheetName, ignoreCase = true)
        }
        return false
    }

    /**
     * @return `true` if the presence of the specified character in a sheet name would
     * require the sheet name to be delimited in formulas.  This includes every non-alphanumeric
     * character besides underscore '_' and dot '.'.
     */
    /* package */
    fun isSpecialChar(ch: Char): Boolean {
        // note - Character.isJavaIdentifierPart() would allow dollars '$'
        if (Character.isLetterOrDigit(ch)) {
            return false
        }
        when (ch) {
            '.', '_' -> return false
            '\n', '\r', '\t' -> throw RuntimeException(
                ("Illegal character (0x"
                        + Integer.toHexString(ch.code) + ") found in sheet name")
            )
        }
        return true
    }


    /**
     * Used to decide whether sheet names like 'AB123' need delimiting due to the fact that they
     * look like cell references.
     * 
     * 
     * This code is currently being used for translating formulas represented with `Ptg`
     * tokens into human readable text form.  In formula expressions, a sheet name always has a
     * trailing '!' so there is little chance for ambiguity.  It doesn't matter too much what this
     * method returns but it is worth noting the likely consumers of these formula text strings:
     * 
     *  1. POI's own formula parser
     *  1. Visual reading by human
     *  1. VBA automation entry into Excel cell contents e.g.  ActiveCell.Formula = "=c64!A1"
     *  1. Manual entry into Excel cell contents
     *  1. Some third party formula parser
     * 
     * 
     * At the time of writing, POI's formula parser tolerates cell-like sheet names in formulas
     * with or without delimiters.  The same goes for Excel(2007), both manual and automated entry.
     * 
     * 
     * For better or worse this implementation attempts to replicate Excel's formula renderer.
     * Excel uses range checking on the apparent 'row' and 'column' components.  Note however that
     * the maximum sheet size varies across versions.
     * @see CellReference
     */
    /* package */
    fun cellReferenceIsWithinRange(lettersPrefix: String?, numbersSuffix: String?): Boolean {
        return CellReference.cellReferenceIsWithinRange(
            lettersPrefix,
            numbersSuffix,
            SpreadsheetVersion.EXCEL97
        )
    }

    /**
     * Note - this method assumes the specified rawSheetName has only letters and digits.  It
     * cannot be used to match absolute or range references (using the dollar or colon char).
     * 
     * 
     * Some notable cases:
     * <blockquote><table border="0" cellpadding="1" cellspacing="0" summary="Notable cases.">
     * <tr><th>Input&nbsp;</th><th>Result&nbsp;</th><th>Comments</th></tr>
     * <tr><td>"A1"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
     * <tr><td>"a111"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
     * <tr><td>"AA"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
     * <tr><td>"aa1"&nbsp;&nbsp;</td><td>true</td><td>&nbsp;</td></tr>
     * <tr><td>"A1A"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
     * <tr><td>"A1A1"&nbsp;&nbsp;</td><td>false</td><td>&nbsp;</td></tr>
     * <tr><td>"A$1:$C$20"&nbsp;&nbsp;</td><td>false</td><td>Not a plain cell reference</td></tr>
     * <tr><td>"SALES20080101"&nbsp;&nbsp;</td><td>true</td>
     * <td>Still needs delimiting even though well out of range</td></tr>
    </table></blockquote> * 
     * 
     * @return `true` if there is any possible ambiguity that the specified rawSheetName
     * could be interpreted as a valid cell name.
     */
    /* package */
    fun nameLooksLikePlainCellReference(rawSheetName: String): Boolean {
        val matcher = CELL_REF_PATTERN.matcher(rawSheetName)
        if (!matcher.matches()) {
            return false
        }


        // rawSheetName == "Sheet1" gets this far.
        val lettersPrefix = matcher.group(1)
        val numbersSuffix = matcher.group(2)
        return cellReferenceIsWithinRange(lettersPrefix, numbersSuffix)
    }
}
