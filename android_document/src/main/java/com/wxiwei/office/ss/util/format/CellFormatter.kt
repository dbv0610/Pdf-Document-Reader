/*
 * 文件名称:          CellFormatter.java
 *
 * 编译器:            android2.2
 * 时间:              下午8:38:58
 */
package com.wxiwei.office.ss.util.format

import java.text.DecimalFormat
import java.text.Format
import java.text.SimpleDateFormat

/**
 * 单元格式数字格式
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2011-11-9
 * 负责人:          ljj8494
 */
class CellFormatter {
    // 文本格式
    private var textFormatter: Array<Format?>? = null

    // 数值格式
    private var generalNumberFormat: DecimalFormat? = DecimalFormat("0")

    /**
     *
     */
    init {
        val textFormatter = arrayOfNulls<Format>(0x31)
        this.textFormatter = textFormatter

        textFormatter[0x01] = DecimalFormat("0")
        textFormatter[0x02] = DecimalFormat("0.00")
        textFormatter[0x03] = DecimalFormat("#,##0")
        textFormatter[0x04] = DecimalFormat("#,##0.00")
        textFormatter[0x05] = DecimalFormat("$#,##0;$#,##0")
        textFormatter[0x06] = DecimalFormat("$#,##0;$#,##0")
        textFormatter[0x07] = DecimalFormat("$#,##0.00;$#,##0.00")
        textFormatter[0x08] = DecimalFormat("$#,##0.00;$#,##0.00")
        textFormatter[0x09] = DecimalFormat("0%")

        textFormatter[0x0A] = DecimalFormat("0.00%")
        textFormatter[0x0B] = DecimalFormat("0.00E0")
        textFormatter[0x0C] = FractionalFormat("# ?/?")
        textFormatter[0x0D] = FractionalFormat("# ??/??")
        textFormatter[0x0E] = SimpleDateFormat("M/d/yy")
        textFormatter[0x0F] = SimpleDateFormat("d-MMM-yy")
        textFormatter[0x10] = SimpleDateFormat("d-MMM")
        textFormatter[0x11] = SimpleDateFormat("MMM-yy")
        textFormatter[0x12] = SimpleDateFormat("h:mm a")
        textFormatter[0x13] = SimpleDateFormat("h:mm:ss a")
        textFormatter[0x14] = SimpleDateFormat("h:mm")
        textFormatter[0x15] = SimpleDateFormat("h:mm:ss")
        textFormatter[0x16] = SimpleDateFormat("M/d/yy h:mm")

        // 0x17 - 0x24 reserved for international and undocumented 0x25, "(#,##0_);(#,##0)"
        //start at 0x26
        //jmh need to do colour
        //"(#,##0_);[Red](#,##0)"
        textFormatter[0x26] = DecimalFormat("#,##0;#,##0")
        //jmh need to do colour
        //(#,##0.00_);(#,##0.00)
        textFormatter[0x27] = DecimalFormat("#,##0.00;#,##0.00")
        textFormatter[0x28] = DecimalFormat("#,##0.00;#,##0.00")
        //??        textFormatter[0x29] = new DecimalFormat("_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)");
        //??        textFormatter[0x2A] = new DecimalFormat("_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)");
        //??        textFormatter[0x2B] = new DecimalFormat("_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)");
        //??        textFormatter[0x2C] = new DecimalFormat("_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)");
        textFormatter[0x2D] = SimpleDateFormat("mm:ss")
        //??        textFormatter[0x2E] = new SimpleDateFormat("[h]:mm:ss");
        textFormatter[0x2F] = SimpleDateFormat("mm:ss.0")
        textFormatter[0x30] = DecimalFormat("##0.0E0")
    }

    /**
     *
     * @param index
     * @param value
     * @return
     */
    fun format(index: Short, value: Any?): String {
        if (index.toInt() == 0) {
            return value.toString()
        }
        if (textFormatter!![index.toInt()] == null) {
            throw RuntimeException(
                "Sorry. I cant handle the format code :"
                        + Integer.toHexString(index.toInt())
            )
        }
        return textFormatter!![index.toInt()]!!.format(value)
    }

    /**
     *
     * @param index
     * @param value
     * @return
     */
    fun format(index: Short, value: Double): String {
        if (index <= 0 || index >= textFormatter!!.size) {
            return generalNumberFormat!!.format(value)
        }
        val formatter = textFormatter!![index.toInt()]
        if (formatter == null) {
            return generalNumberFormat!!.format(value)
        }
        if (formatter is DecimalFormat) {
            return formatter.format(value)
        }
        if (formatter is FractionalFormat) {
            return formatter.format(value)
        }
        return value.toString()
    }

    /**
     *
     * @param index
     * @param value
     * @return
     */
    fun useRedColor(index: Short, value: Double): Boolean {
        val i = index.toInt()
        return (((i == 0x06) || (i == 0x08) || (i == 0x26) || (i == 0x27)) && (value < 0))
    }

    fun dispose() {
        textFormatter = null
        generalNumberFormat = null
    }

    companion object {
        private val cf = CellFormatter()

        /**
         *
         * @return
         */
        @JvmStatic
        fun instance(): CellFormatter {
            return cf
        }
    }
}
