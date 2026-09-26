/*
 * 文件名称:          AccountFormat.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:21:42
 */
package com.wxiwei.office.ss.util.format

import java.text.DecimalFormat
import java.text.Format
import java.util.regex.Pattern

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2011-12-5
 * 负责人:           jqin
 */
class AccountFormat
/**
 *
 */
private constructor() {
    /**
     *
     * @param pattern
     * @param value
     * @return
     */
    fun format(pattern: String, value: Double): String {
        val subFormat = SEMICOLON.split(pattern)
        var contents = ""
        when (subFormat.size) {
            1 -> contents = parse(subFormat[0], value, false)

            2 -> contents = parse(subFormat[0] + ";" + subFormat[1], value, false)

            3, 4 -> if (Math.abs(value) > ZERO) {
                contents = parse(subFormat[0] + ";" + subFormat[1], value, false)
            } else {
                contents = parse(subFormat[2], 0.0, true)
            }
        }

        return contents
    }

    /**
     *
     * @param pattern
     * @param value
     * @param isZero
     * @return
     */
    private fun parse(pattern: String, value: Double, isZero: Boolean): String {
        var pattern = pattern
        var value = value
        var contents = ""
        val subFormat = SEMICOLON.split(pattern)
        var index = pattern.indexOf("*")
        if (Math.abs(value) < ZERO && subFormat.size == 1) {
            val header = pattern.substring(0, index + 1)
            index = pattern.indexOf('-')
            pattern = pattern.replace("#", "")
            pattern = pattern.replace("?", " ")
            contents = header + pattern.substring(index - 1, pattern.length)
        } else {
            pattern = pattern.replace("*", "")
            val format: Format = DecimalFormat(pattern)
            contents = format.format(value)

            //rounding
            if (value > 0) {
                value += 0.000000001
            } else if (value < 0) {
                value -= 0.000000001
            }
            contents = format.format(value)

            contents = contents.substring(0, index) + "*" + contents.substring(index, contents.length)
        }

        return contents
    }

    companion object {
        private const val ZERO = 0.000001

        // Java String.split semantics
        private val SEMICOLON: Pattern = Pattern.compile(";")

        private val af = AccountFormat()

        /**
         *
         * @return
         */
        @JvmStatic
        fun instance(): AccountFormat {
            return af
        }
    }
}
