/*
 * 文件名称:          DateTimeFormatSymbols.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:46:57
 */
package com.wxiwei.office.ss.util.format

import java.text.DateFormatSymbols
import java.util.Locale

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2011-12-8
 * 负责人:           jqin
 */
class DateTimeFormatSymbols(locale: Locale) {
    @JvmField
    val stdWeekdays = arrayOf(
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    @JvmField
    val stdShortWeekdays = arrayOf(
        "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    )

    @JvmField
    val stdMonths = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December", ""
    )

    @JvmField
    val stdShortMonths = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "July", "Aug", "Sep", "Oct", "Nov", "Dec", ""
    )

    @JvmField
    val stdShortestMonths = arrayOf(
        "J", "F", "M", "A", "M", "J",
        "J", "A", "S", "O", "N", "D"
    )

    @JvmField
    var formatData: DateFormatSymbols? = DateFormatSymbols(locale)

    /**
     *
     */
    fun dispose() {
        formatData = null
    }
}
