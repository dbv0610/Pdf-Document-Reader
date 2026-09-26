/*
 * 文件名称:          CellFormatter.java
 *
 * 编译器:            android2.2
 * 时间:              下午8:38:58
 */
package com.wxiwei.office.ss.util.format

import java.text.FieldPosition
import java.text.Format
import java.text.ParseException
import java.text.ParsePosition

/**
 * 分数格式化
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2011-11-9
 * 负责人:          ljj8494
 */
@Suppress("serial")
open class FractionalFormat
/**
 * Constructs a new FractionalFormatter
 *
 *  # ?/? Up to one digit
 *  # ??/?? Up to two digits
 *  # ???/??? Up to three digits
 *  # ?/2 In halves
 *  # ?/4 In quarters
 *  # ?/8 In eighths
 *  # ?/16 In sixteenths
 *  # ?/10 In tenths
 *  # ?/100 In hundredths
 */
(formatStr: String?) : Format() {
    private val ONE_DIGIT: Short = 1
    private val TWO_DIGIT: Short = 2
    private val THREE_DIGIT: Short = 3
    private val UNITS: Short = 4
    private var units = 1
    private var mode: Short = -1

    init {
        if ("# ?/?" == formatStr) {
            mode = ONE_DIGIT
        } else if ("# ??/??" == formatStr) {
            mode = TWO_DIGIT
        } else if ("# ???/???" == formatStr) {
            mode = THREE_DIGIT
        } else if ("# ?/2" == formatStr) {
            mode = UNITS
            units = 2
        } else if ("# ?/4" == formatStr) {
            mode = UNITS
            units = 4
        } else if ("# ?/8" == formatStr) {
            mode = UNITS
            units = 8
        } else if ("# ??/16" == formatStr) {
            mode = UNITS
            units = 16
        } else if ("# ?/10" == formatStr) {
            mode = UNITS
            units = 10
        } else if ("# ??/100" == formatStr) {
            mode = UNITS
            units = 100
        }
    }

    /**
     *
     * @param  f       Description of the Parameter
     * @param  maxDen  Description of the Parameter
     * @return         Description of the Return Value
     */
    private fun format(f: Double, maxDen: Int): String {
        var whole = f.toLong()
        var sign = 1
        if (f < 0) {
            sign = -1
        }
        var precision = 0.00001
        val allowedError = precision
        var d = Math.abs(f)
        d -= whole.toDouble()
        val frac = d
        var diff = frac
        var num: Long = 1
        var den: Long = 0
        var a: Long = 0
        var b: Long = 0
        var i: Long = 0
        if (frac > precision) {
            while (true) {
                d = 1.0 / d
                i = (d + precision).toLong()
                d -= i.toDouble()
                if (a > 0) {
                    num = i * num + b
                }
                den = (num / frac + 0.5).toLong()
                diff = Math.abs(num.toDouble() / den - frac)
                if (den > maxDen) {
                    if (a > 0) {
                        num = a
                        den = (num / frac + 0.5).toLong()
                        diff = Math.abs(num.toDouble() / den - frac)
                    } else {
                        den = maxDen.toLong()
                        num = 1
                        diff = Math.abs(num.toDouble() / den - frac)
                        if (diff > frac) {
                            num = 0
                            den = 1
                            // Keeps final check below from adding 1 and keeps Den from being 0
                            diff = frac
                        }
                    }
                    break
                }
                if ((diff <= allowedError) || (d < precision)) {
                    break
                }
                precision = allowedError / diff
                // This calcualtion of Precision does not always provide results within
                // Allowed Error. It compensates for loss of significant digits that occurs.
                // It helps to round the inprecise reciprocal values to i.
                b = a
                a = num
            }
        }
        if (num == den) {
            whole++
            num = 0
            den = 0
        } else if (den == 0L) {
            num = 0
        }
        if (sign < 0) {
            if (whole == 0L) {
                num = -num
            } else {
                whole = -whole
            }
        }

        var value = ""
        if (whole != 0L) {
            value = value + whole.toString()
        }

        if (num != 0L && den != 0L) {
            value = value + (" " + num + "/" + den)
        }

        return value
    }

    /** This method formats the double in the units specified.
     *  The usints could be any number but in this current implementation it is
     *  halves (2), quaters (4), eigths (8) etc
     */
    private fun formatUnit(f: Double, units: Int): String {
        var f = f
        val whole = f.toLong()
        f -= whole.toDouble()
        val num = Math.round(f * units)

        var value = ""
        if (whole != 0L) {
            value = value + whole.toString()
        }

        if (num != 0L) {
            value = value + (" " + num + "/" + units)
        }

        return value
    }

    /**
     *
     * @param value
     * @return
     */
    fun format(value: Double): String {
        if (mode == ONE_DIGIT) {
            return format(value, 9)
        } else if (mode == TWO_DIGIT) {
            return format(value, 99)
        } else if (mode == THREE_DIGIT) {
            return format(value, 999)
        } else if (mode == UNITS) {
            return formatUnit(value, units)
        }
        throw RuntimeException("Unexpected Case")
    }

    /**
     *
     */
    override fun format(obj: Any?, toAppendTo: StringBuffer, pos: FieldPosition?): StringBuffer {
        if (obj is Number) {
            toAppendTo.append(format(obj.toDouble()))
            return toAppendTo
        }
        throw IllegalArgumentException("Can only handle Numbers")
    }

    /**
     *
     */
    override fun parseObject(source: String?, status: ParsePosition?): Any? {
        return null
    }

    /**
     *
     */
    @Throws(ParseException::class)
    override fun parseObject(source: String?): Any? {
        return null
    }

    /**
     *
     */
    override fun clone(): Any {
        // original Java returned null; Kotlin requires a non-null return type here,
        // so an unchecked generic cast is used to keep returning null.
        return nullValue()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> nullValue(): T = null as T
}
