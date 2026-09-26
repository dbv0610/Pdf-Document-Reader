/*
 * 閺傚洣娆㈤崥宥囆�:          NumericCellFormatter.java
 * 閻楀牊娼堥幍?婀丂2001-2014 閾忕钂嬮敍鍫熸線瀹哥儑绱氱粔鎴炲Η閺堝妾洪崗顒�寰�
 * 缂傛牞鐦ч崳?            android2.2
 * 閺冨爼妫�:              娑撳﹤宕�10:20:18
 */
package com.wxiwei.office.ss.util.format

import android.graphics.Color
import com.wxiwei.office.ss.model.baseModel.Cell
import com.wxiwei.office.ss.model.sheetProperty.Palette
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.util.Date
import java.util.regex.Pattern

/**
 * TODO: 閺傚洣娆㈠▔銊╁櫞
 *
 * Read閻楀牊婀�:        Read V1.0
 *
 * 娴ｆ粏?:            jqin
 *
 * 閺冦儲婀�:            2011-12-2
 *
 * 鐠愮喕鐭楁禍?           jqin
 *
 * 鐠愮喕鐭楃亸蹇曠矋:
 */
class NumericFormatter private constructor() {
    /**
     * A map to cache formats.
     * Map<String,Format> formats
     */
    private val formats: MutableMap<String, Format>

    /**
     *
     */
    init {
        formats = HashMap()

        // init built-in formats

        val zipFormat = ZipPlusFourFormat.instance
        addFormat("00000\\-0000", zipFormat)
        addFormat("00000-0000", zipFormat)

        val phoneFormat = PhoneFormat.instance
        // allow for format string variations
        addFormat("[<=9999999]###\\-####;\\(###\\)\\ ###\\-####", phoneFormat)
        addFormat("[<=9999999]###-####;(###) ###-####", phoneFormat)
        addFormat("###\\-####;\\(###\\)\\ ###\\-####", phoneFormat)
        addFormat("###-####;(###) ###-####", phoneFormat)

        addFormat("[<=9999999]000\\-0000;\\(000\\)\\ 000\\-0000", phoneFormat)
        addFormat("[<=9999999]000-0000;(000) 000-0000", phoneFormat)
        addFormat("000\\-0000;\\(000\\)\\ 000\\-0000", phoneFormat)
        addFormat("000-0000;(000) 000-0000", phoneFormat)

        val ssnFormat = SSNFormat.instance
        addFormat("000\\-00\\-0000", ssnFormat)
        addFormat("000-00-0000", ssnFormat)
    }

    /**
     * Adds a new format to the available formats.
     *
     * The value that will be passed to the Format's format method (specified
     * by `java.text.Format#format`) will be a double value from a
     * numeric cell. Therefore the code in the format method should expect a
     * `Number` value.
     *
     * @param excelFormatStr The data format string
     * @param format A Format instance
     */
    fun addFormat(excelFormatStr: String, format: Format) {
        formats[excelFormatStr] = format
    }

    /**
     *
     * @param formatString
     * @return
     */
    fun getNumericCellType(formatString: String): Short {
        var formatString: String? = formatString
        var cellType: Short = -1
        val len = formatString!!.length

        if (formatString == null || formatString.length == 0 || formatString.equals("General", ignoreCase = true)) {
            cellType = Cell.CELL_TYPE_NUMERIC_GENERAL
        } else if ("@" == formatString) {
            cellType = Cell.CELL_TYPE_NUMERIC_STRING
        } else if (formatString.replace("?/", "").length < len) {
            cellType = Cell.CELL_TYPE_NUMERIC_FRACTIONAL
        } else if (formatString.indexOf('*') > -1) {
            cellType = Cell.CELL_TYPE_NUMERIC_ACCOUNTING
        } else {
            formatString = validateDatePattern(formatString)

            if (formatString == null || formatString.length == 0 || formatString.equals("General", ignoreCase = true)) {
                cellType = Cell.CELL_TYPE_NUMERIC_GENERAL
            } else if (DateTimeFormat.isDateTimeFormat(formatString)) {
                cellType = Cell.CELL_TYPE_NUMERIC_SIMPLEDATE
            } else {
                cellType = Cell.CELL_TYPE_NUMERIC_DECIMAL //CELL_TYPE_NUMERIC_GENERAL
            }
        }

        return cellType
    }

    /**
     *
     * @param formatValue
     * @return
     */
    private fun validatePattern(formatValue: String): String {
        var formatValue = formatValue
        var format = formatValue.replace(";@", "")

        formatValue = ""
        var str = ""
        var s = format.indexOf('\"')
        while (s >= 0) {
            str = format.substring(0, s)
            format = format.substring(s + 1, format.length)
            s = format.indexOf('\"')

            //Double quotes
            if (s >= 0) {
                //process format brfore '\"'
                str = deleteInvalidateChars(str)!!
            }
            //concat with text between ""
            formatValue += str + format.substring(0, s)

            //process rest text
            format = format.substring(s + 1, format.length)

            s = format.indexOf('\"')
        }

        return formatValue + deleteInvalidateChars(format)
    }

    private fun deleteInvalidateChars(str: String?): String? {
        var str = str
        if (str != null) {
            str = str.replace("\\\\-".toRegex(), "-")
            str = str.replace("\\\\,".toRegex(), ",")
            str = str.replace("\\\\\\.".toRegex(), ".") // . is a special regexp char
            str = str.replace("\\\\ ".toRegex(), " ")
            str = str.replace("\\\\/".toRegex(), "/") // weird: m\\/d\\/yyyy
            str = str.replace("\"/\"".toRegex(), "/") // "/" is escaped for no reason in: mm"/"dd"/"yyyy

            str = str.replace("_-", " ")
            str = str.replace("_(", " ")
            str = str.replace("_)", "")
            str = str.replace("\\(", "(")
            str = str.replace("\\)", ")")
            str = str.replace("\\", "")
            str = str.replace("_", "")
        }

        return str
    }

    /**
     *
     * @param formatValue
     * @return
     */
    private fun validateDatePattern(formatStr: String): String {
        var format = validatePattern(formatStr)

        //refer to DataFormatter
        // Convert excel date format to SimpleDateFormat.
        // Excel uses lower and upper case 'm' for both minutes and months.
        // From Excel help:
        /*
            The "m" or "mm" code must appear immediately after the "h" or"hh"
            code or immediately before the "ss" code; otherwise, Microsoft
            Excel displays the month instead of minutes."
          */

        var hasAmPm = false
        val amPmMatcher = amPmPattern.matcher(format)
        while (amPmMatcher.find()) {
            hasAmPm = true
        }

        val sb = StringBuffer()
        val chars = format.toCharArray()
        var mIsMonth = true
        val ms: MutableList<Int> = ArrayList()
        var isElapsed = false
        for (j in chars.indices) {
            val c = chars[j]
            if (c == '[' && !isElapsed) {
                isElapsed = true
                mIsMonth = false
                sb.append(c)
            } else if (c == ']' && isElapsed) {
                isElapsed = false
                sb.append(c)
            } else if (isElapsed) {
                if (c == 'h' || c == 'H') {
                    sb.append('H')
                } else if (c == 'm' || c == 'M') {
                    sb.append('m')
                } else if (c == 's' || c == 'S') {
                    sb.append('s')
                } else {
                    sb.append(c)
                }
            } else if (c == 'h' || c == 'H') {
                mIsMonth = false
                if (hasAmPm) {
                    sb.append('h')
                } else {
                    sb.append('H')
                }
            } else if (c == 'm' || c == 'M') {
                if (mIsMonth) {
                    sb.append('M')
                    ms.add(Integer.valueOf(sb.length - 1))
                } else {
                    sb.append('m')
                }
            } else if (c == 's' || c == 'S') {
                sb.append('s')
                // if 'M' precedes 's' it should be minutes ('m')
                for (i in ms.indices) {
                    val index = ms[i].toInt()
                    if (sb[index] == 'M') {
                        sb.replace(index, index + 1, "m")
                    }
                }
                mIsMonth = true
                ms.clear()
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')/*Character.isLetter(c)*/) {
                mIsMonth = true
                ms.clear()
                if (c == 'y' || c == 'Y') {
                    sb.append('y')
                } else if (c == 'd' || c == 'D') {
                    sb.append('d')
                } else {
                    sb.append(c)
                }
            } else {
                sb.append(c)
            }
        }

        format = sb.toString()
        var index = format.indexOf('[')
        while (index > -1) {
            val next = format.indexOf(']')
            format = format.substring(0, index) + format.substring(next + 1, format.length)
            index = format.indexOf('[')
        }

        return format
    }

    private fun getMoneySymbol(formatString: String): String? {
        var formatString = formatString
        var index1 = formatString.indexOf("[")
        var index2 = formatString.indexOf("]")
        var moneysymbol: String? = null
        while (index1 >= 0 && index2 >= 0) {
            val removedStr = formatString.substring(index1, index2 + 1)
            index1 = removedStr.indexOf("$")
            if (index1 >= 0) {
                //eg."[[$鎷�-809]]", [$鈧�-2], not "[$-809]"
                index2 = removedStr.indexOf('-')
                if (index2 < 0) {
                    index2 = removedStr.indexOf("]")
                }
                moneysymbol = removedStr.substring(index1 + 1, index2)
                if (moneysymbol != null) {
                    return moneysymbol
                }
            }

            formatString = formatString.replace(removedStr, "")

            index1 = formatString.indexOf("[")
            index2 = formatString.indexOf("]")
        }

        return null
    }

    /**
     * money symbol is first or negative symbol is first
     * @param formatString
     * @return
     */
    private fun isNegativeFirst(formatString: String): Boolean {
        var formatString = formatString
        var index1 = formatString.indexOf("[")
        val index2 = formatString.indexOf("]")
        var moneySymbol: String? = null
        if (index1 >= 0 && index2 >= 0) {
            val str = formatString.substring(index1, index2 + 1)
            index1 = str.indexOf("$")
            if (index1 >= 0 && str.length == 8) {
                //eg."[[$鎷�-809]]", not "[$-809]"
                moneySymbol = str
            }
        }

        if (moneySymbol != null) {
            index1 = formatString.indexOf(';')
            if (index1 >= 0) {
                formatString = formatString.substring(index1)

                index1 = formatString.indexOf(moneySymbol)
                if (index1 > 0 && formatString[index1 - 1] == '-') {
                    return true
                }
            }
        }

        return false
    }

    /**
     *
     * @param formatString  eg [$楼-804]#,##0.00_);[Red]([$楼-804]#,##0.00)
     * @return
     */
    private fun processMoneyAndNegative(formatString: String): String {
        var formatString = formatString
        var index1 = formatString.indexOf("[")
        var index2 = formatString.indexOf("]")
        val moneysymbol: String? = null
        while (index1 >= 0 && index2 >= 0) {
            val removedStr = formatString.substring(index1, index2 + 1)
            formatString = formatString.replace(removedStr, "")

            index1 = formatString.indexOf("[")
            index2 = formatString.indexOf("]")
        }

        return formatString
    }

    /**
     *
     * @param formatString
     * @param date
     * @return
     */
    fun getFormatContents(formatString: String, date: Date?): String {
        try {
            val format = DateTimeFormat(validateDatePattern(formatString))
            return format.format(date)
        } catch (ex: Exception) {
            val format = DateTimeFormat("m/d/yy")
            return format.format(date)
        }
    }

    /**
     *
     * @param formatString
     * @param cellType
     * @return
     */
    fun getFormatContents(formatString: String, value: Double, cellType: Short): String {
        var formatString = formatString
        var value = value
        var format: Format? = formats[formatString]
        if (format != null) {
            return format.format(value)
        }

        formatString = validatePattern(formatString)
        var contents = ""
        try {
            val moneysymbol: String?
            val isNegativeFirst: Boolean
            when (cellType) {
                Cell.CELL_TYPE_NUMERIC_GENERAL, Cell.CELL_TYPE_NUMERIC_STRING -> {
                    contents = value.toString()
                    if (!contents.contains("E")) {
                        val index = contents.indexOf('.')
                        if (index > 0 && contents.length - index > 10) {
                            contents = contents.substring(0, index + 10)
                        }
                    }
                    contents = delLastZero(contents)!!
                }

                Cell.CELL_TYPE_NUMERIC_FRACTIONAL -> {
                    format = FractionalFormat(formatString)
                    contents = format.format(value)
                    if (contents.length == 0) {
                        contents = 0.toString()
                    }
                }

                Cell.CELL_TYPE_NUMERIC_DECIMAL -> {
                    moneysymbol = getMoneySymbol(formatString)
                    isNegativeFirst = isNegativeFirst(formatString)
                    formatString = processMoneyAndNegative(formatString)

                    if (value < 0) {
                        val formats = formatString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (formats.size == 2 && formats[0] == formats[1]) {
                            value = -value
                        }
                    }

                    format = DecimalFormat(formatString)
                    //rounding
                    if (value > 0) {
                        value += 0.000000001
                    } else if (value < 0) {
                        value -= 0.000000001
                    }
                    contents = format.format(value)
                    if (moneysymbol != null) {
                        if (contents[0] == '(') {
                            //contains "()"
                            contents = "(" + moneysymbol + contents.substring(1)
                        } else {
                            if (value < 0 && isNegativeFirst) {
                                //money char is after "-"
                                contents = "-" + moneysymbol + contents.replace("-", "")
                            } else {
                                contents = moneysymbol + contents
                            }
                        }
                    }
                }

                Cell.CELL_TYPE_NUMERIC_ACCOUNTING -> {
                    moneysymbol = getMoneySymbol(formatString)
                    isNegativeFirst = isNegativeFirst(formatString)
                    formatString = processMoneyAndNegative(formatString)

                    if (value > 0) {
                        value += 0.000000001
                    } else if (value < 0) {
                        value -= 0.000000001
                    }

                    contents = AccountFormat.instance().format(formatString, value)
                    if (moneysymbol != null) {
                        if (value < 0 && isNegativeFirst) {
                            //money char is after "-"
                            contents = "-" + moneysymbol + contents.replace("-", "")
                        } else {
                            contents = moneysymbol + contents
                        }
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            //format like "000-0000"
            if (formatString.replace("0", "").replace("-", "").length == 0) {
                format = DecimalFormat(formatString.replace("-", ""))
                contents = format.format(value)
                val strBuilder = StringBuilder(contents)
                val ps = formatString.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var cnt = 0
                for (i in ps.size - 1 downTo 1) {
                    cnt += ps[i].length
                    strBuilder.insert(strBuilder.length - cnt, "-")
                    cnt += 1
                }
                contents = strBuilder.toString()
            } else {
                contents = value.toString()
            }
        }

        return contents
    }

    private fun delLastZero(contents: String?): String? {
        if (contents != null && contents.length > 1 && !contents.contains("E") && contents[contents.length - 1] == '0') {
            val index = contents.indexOf('.')
            if (index > 0) {
                val chars = contents.toCharArray()

                var i = chars.size - 1
                while (i > index && chars[i] == '0') {
                    i--
                }
                if (chars[i] == '.') {
                    i--
                }

                return String(chars, 0, i + 1)
            }
        }

        return contents
    }

    /**
     * Format class for Excel's SSN format. This class mimics Excel's built-in
     * SSN formatting.
     *
     * @author James May
     */
    @Suppress("serial")
    private class SSNFormat private constructor() : Format() {
        // enforce singleton

        override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition?): StringBuffer {
            return toAppendTo.append(SSNFormat.format(obj as Number))
        }

        override fun parseObject(source: String?, pos: ParsePosition): Any? {
            return df.parseObject(source, pos)
        }

        companion object {
            @JvmField
            val instance: Format = SSNFormat()
            private val df = createIntegerOnlyFormat("000000000")

            /** Format a number as an SSN  */
            @JvmStatic
            fun format(num: Number): String {
                val result = df.format(num)
                val sb = StringBuffer()
                sb.append(result.substring(0, 3)).append('-')
                sb.append(result.substring(3, 5)).append('-')
                sb.append(result.substring(5, 9))
                return sb.toString()
            }
        }
    }

    /**
     * Format class for Excel Zip + 4 format. This class mimics Excel's
     * built-in formatting for Zip + 4.
     * @author James May
     */
    @Suppress("serial")
    private class ZipPlusFourFormat private constructor() : Format() {
        // enforce singleton

        override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition?): StringBuffer {
            return toAppendTo.append(ZipPlusFourFormat.format(obj as Number))
        }

        override fun parseObject(source: String?, pos: ParsePosition): Any? {
            return df.parseObject(source, pos)
        }

        companion object {
            @JvmField
            val instance: Format = ZipPlusFourFormat()
            private val df = createIntegerOnlyFormat("000000000")

            /** Format a number as Zip + 4  */
            @JvmStatic
            fun format(num: Number): String {
                val result = df.format(num)
                val sb = StringBuffer()
                sb.append(result.substring(0, 5)).append('-')
                sb.append(result.substring(5, 9))
                return sb.toString()
            }
        }
    }

    /**
     * Format class for Excel phone number format. This class mimics Excel's
     * built-in phone number formatting.
     * @author James May
     */
    @Suppress("serial")
    private class PhoneFormat private constructor() : Format() {
        // enforce singleton

        override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition?): StringBuffer {
            return toAppendTo.append(PhoneFormat.format(obj as Number))
        }

        override fun parseObject(source: String?, pos: ParsePosition): Any? {
            return df.parseObject(source, pos)
        }

        companion object {
            @JvmField
            val instance: Format = PhoneFormat()
            private val df = createIntegerOnlyFormat("##########")

            /** Format a number as a phone number  */
            @JvmStatic
            fun format(num: Number): String {
                val result = df.format(num)
                val sb = StringBuffer()
                val seg1: String?
                val seg2: String?
                val seg3: String
                val len = result.length
                if (len <= 4) {
                    return result
                }

                seg3 = result.substring(len - 4, len)
                seg2 = result.substring(Math.max(0, len - 7), len - 4)
                seg1 = result.substring(Math.max(0, len - 10), Math.max(0, len - 7))

                if (seg1 != null && seg1.trim { it <= ' ' }.length > 0) {
                    sb.append('(').append(seg1).append(") ")
                }
                if (seg2 != null && seg2.trim { it <= ' ' }.length > 0) {
                    sb.append(seg2).append('-')
                }
                sb.append(seg3)
                return sb.toString()
            }
        }
    }

    /**
     * Format class that does nothing and always returns a constant string.
     *
     * This format is used to simulate Excel's handling of a format string
     * of all # when the value is 0. Excel will output "", Java will output "0".
     *
     * @see DataFormatter.createFormat
     */
    @Suppress("serial")
    private class ConstantStringFormat(s: String) : Format() {
        private val str: String = s

        override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition?): StringBuffer {
            return toAppendTo.append(str)
        }

        override fun parseObject(source: String?, pos: ParsePosition): Any? {
            return df.parseObject(source, pos)
        }

        companion object {
            private val df = createIntegerOnlyFormat("##########")
        }
    }

    companion object {
        private val cf = NumericFormatter()

        /** Pattern to find "AM/PM" marker  */
        private val amPmPattern: Pattern = Pattern.compile(
            "((A|P)[M/P]*)",
            Pattern.CASE_INSENSITIVE
        )

        /**
         * A regex to match the colour formattings rules.
         * Allowed colours are: Black, Blue, Cyan, Green,
         * Magenta, Red, White, Yellow, "Color n" (1<=n<=56)
         */
        private val colorPattern: Pattern = Pattern.compile(
            "(\\[BLACK\\])|(\\[BLUE\\])|(\\[CYAN\\])|(\\[GREEN\\])|"
                + "(\\[MAGENTA\\])|(\\[RED\\])|(\\[WHITE\\])|(\\[YELLOW\\])|"
                + "(\\[COLOR\\s*\\d\\])|(\\[COLOR\\s*[0-5]\\d\\])", Pattern.CASE_INSENSITIVE
        )

        /**
         *
         * @return
         */
        @JvmStatic
        fun instance(): NumericFormatter {
            return cf
        }

        /**
         * A regex to match the colour formattings rules.
         * Allowed colours are: Black, Blue, Cyan, Green,
         * Magenta, Red, White, Yellow, "Color n" (1<=n<=56)
         * @param book
         * @param formatStr
         * @return
         */
        @JvmStatic
        fun getNegativeColor(cell: Cell): Int {
            val formatCode = cell.getCellStyle()!!.getFormatCode()
            val book = cell.getSheet()!!.getWorkbook()!!

            // Remove color formatting if present
            val colorM = colorPattern.matcher(formatCode)
            if (colorM.find()) {
                var color = colorM.group()

                // Paranoid replacement...
                val at = formatCode!!.indexOf(color)
                if (at == -1) {
                    return Color.BLACK
                }

                if (color == "[Red]") {
                    return Color.RED
                } else if (color == "[Blue]") {
                    return Color.BLUE
                } else if (color == "[Cyan]") {
                    return Color.CYAN
                } else if (color == "[Green]") {
                    return Color.GREEN
                } else if (color == "[Magenta]") {
                    return Color.MAGENTA
                } else if (color == "[Black]") {
                    return Color.BLACK
                } else if (color == "[White]") {
                    return Color.WHITE
                } else if (color == "[Yellow]") {
                    return Color.YELLOW
                } else if (color == "[Color n]") {
                    // n:[1:56]
                    color = color.replace("[Color ", "").replace("]", "")
                    val index = Integer.parseInt(color)
                    return book.getColor(index + Palette.FIRST_COLOR_INDEX - 1)
                }
            }

            val font = book.getFont(cell.getCellStyle()!!.getFontIndex().toInt())
            return book.getColor(font!!.getColorIndex())
        }

        /**
         * @return a <tt>DecimalFormat</tt> with parseIntegerOnly set `true`
         */
        /* package */
        @JvmStatic
        fun createIntegerOnlyFormat(fmt: String): DecimalFormat {
            val result = DecimalFormat(fmt)
            result.isParseIntegerOnly = true
            return result
        }
    }
}
