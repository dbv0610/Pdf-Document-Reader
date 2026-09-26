/*
 * 文件名称:          DateTimeFormat.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:37:06
 */
package com.wxiwei.office.ss.util.format

import java.text.FieldPosition
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

//// BEGIN android-added
//import com.ibm.icu4jni.util.LocaleData;
//import com.ibm.icu4jni.util.Resources;
//// END android-added

/**
 * TODO: refer to DataFormatter
 *
 * Read版本:        Read V1.0
 *
 * 作者:            jqin
 *
 * 日期:            2011-12-7
 *
 * 负责人:           jqin
 *
 * 负责小组:
 */
open class DateTimeFormat private constructor(locale: Locale) {

    protected var calendar: Calendar? = null

    /**
     * The number format used to format a number.
     */
    protected var numberFormat: NumberFormat? = null

    private var pattern: String? = null

    private var dateTimeFormatData: DateTimeFormatSymbols? = null

    private var ampm = false

    // BEGIN android-removed
    // SimpleDateFormat(Locale locale, com.ibm.icu.text.SimpleDateFormat icuFormat){
    // }
    // END android-removed

    init {
        numberFormat = NumberFormat.getInstance(locale)
        numberFormat!!.isParseIntegerOnly = true
        numberFormat!!.isGroupingUsed = false
        calendar = GregorianCalendar(locale)
        calendar!!.add(Calendar.YEAR, -80)
    }

    /**
     * Constructs a new `SimpleDateFormat` using the specified
     * non-localized pattern and the `DateFormatSymbols` and
     * `Calendar` for the default locale.
     *
     * @param pattern
     * the pattern.
     * @throws NullPointerException
     * if the pattern is `null`.
     * @throws IllegalArgumentException
     * if `pattern` is not considered to be usable by this
     * formatter.
     */
    constructor(pattern: String) : this(pattern, Locale.getDefault())

    constructor(template: String, locale: Locale) : this(locale) {
        var template = template
        template = adjust(template)

        validatePattern(template)

        // BEGIN android-removed
        // icuFormat = new com.ibm.icu.text.SimpleDateFormat(template, locale);
        // icuFormat.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone(tzId));
        // END android-removed

        pattern = template
        // BEGIN android-changed
        dateTimeFormatData = DateTimeFormatSymbols(locale)
    }

    private fun adjust(template: String): String {
        var template = template
        if (template.contains("AM/PM") || template.contains("上午/下午")) {
            template = template.replace("AM/PM", "").replace("上午/下午", "")
            ampm = true
        }

        val date = isDate(template)
        val time = isTime(template)

        if (time && date) {
            //date and time

            //change "mmm..." to "MMM..."
            var index = template.indexOf("mmm")
            var chars: CharArray? = null
            while (index > -1) {
                chars = template.toCharArray()
                val first = index
                var last = index + 3
                while (template[last] == 'm') {
                    last++
                }
                index = first
                while (index < last) {
                    chars[index] = 'M'
                    index++
                }
                template = String(chars)
                index = template.indexOf("mmm")
            }

            //
            chars = template.toCharArray()
            val indexList: MutableList<Int> = ArrayList()
            index = template.indexOf('m')
            if (index > -1) {
            }
        } else if (date) {
            //date
            template = template.replace('m', 'M')
        } else {
            //time
            if (!ampm) {
                template = template.replace('h', 'k')
            }
        }

        return template
    }

    /**
     * Validates the format character.
     *
     * @param format
     * the format character
     *
     * @throws IllegalArgumentException
     * when the format character is invalid
     */
    private fun validateFormat(format: Char) {
        val index = patternChars.indexOf(format)
        if (index == -1) {
            // text.03=Unknown pattern character - '{0}'
            throw IllegalArgumentException("invalidate char") //$NON-NLS-1$
        }
    }

    /**
     * Validates the pattern.
     *
     * @param template
     * the pattern to validate.
     *
     * @throws NullPointerException
     * if the pattern is null
     * @throws IllegalArgumentException
     * if the pattern is invalid
     */
    private fun validatePattern(template: String) {
        var quote = false
        var next: Int
        var last = -1
        var count = 0

        val patternLength = template.length
        for (i in 0 until patternLength) {
            next = template[i].code
            if (next == '\''.code) {
                if (count > 0) {
                    validateFormat(last.toChar())
                    count = 0
                }
                if (last == next) {
                    last = -1
                } else {
                    last = next
                }
                quote = !quote
                continue
            }
            if (!quote
                && (last == next || (next >= 'a'.code && next <= 'z'.code) || (next >= 'A'.code && next <= 'Z'.code))
            ) {
                if (last == next) {
                    count++
                } else {
                    if (count > 0) {
                        validateFormat(last.toChar())
                    }
                    last = next
                    count = 1
                }
            } else {
                if (count > 0) {
                    validateFormat(last.toChar())
                    count = 0
                }
                last = -1
            }
        }
        if (count > 0) {
            validateFormat(last.toChar())
        }

        if (quote) {
            // text.04=Unterminated quote {0}
            throw IllegalArgumentException("invalidate pattern") //$NON-NLS-1$
        }
    }

    fun format(date: Date?): String {
        return formatImpl(date, StringBuffer()).toString()
    }

    /**
     * Formats the date.
     *
     * If the FieldPosition `field` is not null, and the field
     * specified by this FieldPosition is formatted, set the begin and end index
     * of the formatted field in the FieldPosition.
     *
     * If the Vector `fields` is not null, find fields of this
     * date, set FieldPositions with these fields, and add them to the fields
     * vector.
     *
     * @param date
     * Date to Format
     * @param buffer
     * StringBuffer to store the resulting formatted String
     * @param field
     * FieldPosition to set begin and end index of the field
     * specified, if it is part of the format for this date
     * @param fields
     * Vector used to store the FieldPositions for each field in this
     * date
     * @return the formatted Date
     * @throws IllegalArgumentException
     * if the object cannot be formatted by this Format.
     */
    private fun formatImpl(date: Date?, buffer: StringBuffer): StringBuffer {
        var quote = false
        var next: Int
        var last = -1
        var count = 0
        calendar!!.time = date

        val pattern = this.pattern!!
        val patternLength = pattern.length
        for (i in 0 until patternLength) {
            next = pattern[i].code
            if (next == '\''.code) {
                if (count > 0) {
                    append(buffer, last.toChar(), count)
                    count = 0
                }
                if (last == next) {
                    buffer.append('\'')
                    last = -1
                } else {
                    last = next
                }
                quote = !quote
                continue
            }
            if (!quote
                && (last == next || (next >= 'a'.code && next <= 'z'.code) || (next >= 'A'.code && next <= 'Z'.code))
            ) {
                if (last == next) {
                    count++
                } else {
                    if (count > 0) {
                        append(buffer, last.toChar(), count)
                    }
                    last = next
                    count = 1
                }
            } else {
                if (count > 0) {
                    append(buffer, last.toChar(), count)
                    count = 0
                }
                last = -1
                buffer.append(next.toChar())
            }
        }
        if (count > 0) {
            append(buffer, last.toChar(), count)
        }

        if (ampm) {
            val strAMPM = dateTimeFormatData!!.formatData!!.amPmStrings
            buffer.append(strAMPM[calendar!!.get(Calendar.AM_PM)])
        }

        return buffer
    }

    private fun append(buffer: StringBuffer, format: Char, count: Int) {
        val calendar = this.calendar!!
        val dateTimeFormatData = this.dateTimeFormatData!!
        var field = -1
        val index = patternChars.indexOf(format)
        if (index == -1) {
            // text.03=Unknown pattern character - '{0}'
            throw IllegalArgumentException("invalidate char") //$NON-NLS-1$
        }

        var hour: Int
        when (index) {
            ERA_FIELD -> {
                //G
                val strERAS = dateTimeFormatData.formatData!!.eras
                buffer.append(strERAS[calendar.get(Calendar.ERA)])
            }

            YEAR_FIELD -> {
                //y
                var year = calendar.get(Calendar.YEAR)
                // BEGIN android-changed
                // According to Unicode CLDR TR35(http://unicode.org/reports/tr35/) :
                // If date pattern is "yy", display the last 2 digits of year.
                // Otherwise, display the actual year with minimum digit count.
                // Therefore, if the pattern is "y", the display value for year 1234  is '1234' not '34'.
                if (count == 2) {
                    year %= 100
                    appendNumber(buffer, 2, year)
                } else {
                    appendNumber(buffer, count, year)
                }
                // END android-changed
            }

            MONTH_FIELD -> {
                //M
                val month = calendar.get(Calendar.MONTH)
                if (count <= 2) {
                    appendNumber(buffer, count, month + 1)
                } else if (count == 3) {
                    val strMonths = dateTimeFormatData.formatData!!.shortMonths
                    buffer.append(strMonths[month])
                } else {
                    val strMonths = dateTimeFormatData.formatData!!.months
                    buffer.append(strMonths[month])
                }
            }

            DATE_FIELD -> {
                //d
                val weekday = calendar.get(Calendar.DAY_OF_WEEK)
                if (weekday < dateTimeFormatData.stdShortWeekdays.size) {
                    if (count == 3) {
                        buffer.append(dateTimeFormatData.stdShortWeekdays[weekday])
                    } else if (count > 3) {
                        buffer.append(dateTimeFormatData.stdWeekdays[weekday])
                    } else {
                        field = Calendar.DATE
                    }
                }
            }

            HOUR_OF_DAY1_FIELD -> {
                // k
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                appendNumber(buffer, count, if (hour == 0) 24 else hour)
            }

            HOUR_OF_DAY0_FIELD -> {
                // H
                if (ampm) {
                    hour = calendar.get(Calendar.HOUR)
                    appendNumber(buffer, count, if (hour == 0) 12 else hour)
                } else {
                    hour = calendar.get(Calendar.HOUR_OF_DAY)
                    appendNumber(buffer, count, hour)
                }
            }

            MINUTE_FIELD -> {
                //m
                if (count == 3 || count > 5) {
                    buffer.append(dateTimeFormatData.stdShortMonths[calendar.get(Calendar.MONTH)])
                } else if (count == 4) {
                    buffer.append(dateTimeFormatData.stdMonths[calendar.get(Calendar.MONTH)])
                } else if (count == 5) {
                    buffer.append(dateTimeFormatData.stdShortestMonths[calendar.get(Calendar.MONTH)])
                } else {
                    field = Calendar.MINUTE
                }
            }

            SECOND_FIELD -> {
                //s
                field = Calendar.SECOND
            }

            MILLISECOND_FIELD -> {
                //S
                val value = calendar.get(Calendar.MILLISECOND)
                appendNumber(buffer, count, value)
            }

            DAY_OF_YEAR_FIELD -> {
                //D
                field = Calendar.DAY_OF_YEAR
            }

            DAY_OF_WEEK_IN_MONTH_FIELD -> {
                //F
                field = Calendar.DAY_OF_WEEK_IN_MONTH
            }

            WEEK_OF_YEAR_FIELD -> {
                //w
                field = Calendar.WEEK_OF_YEAR
            }

            WEEK_OF_MONTH_FIELD -> {
                //W
                field = Calendar.WEEK_OF_MONTH
            }

            DAY_OF_WEEK_FIELD -> {
                //a
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                if (count == 3) {
                    val strWeekdays = dateTimeFormatData.formatData!!.shortWeekdays
                    buffer.append(strWeekdays[day])
                } else if (count > 3) {
                    val strWeekdays = dateTimeFormatData.formatData!!.weekdays
                    buffer.append(strWeekdays[day])
                }
            }

            HOUR1_FIELD -> {
                // h
                if (ampm) {
                    hour = calendar.get(Calendar.HOUR)
                    appendNumber(buffer, count, if (hour == 0) 12 else hour)
                } else {
                    hour = calendar.get(Calendar.HOUR_OF_DAY)
                    appendNumber(buffer, count, hour)
                }
            }

            HOUR0_FIELD -> {
                // K
                field = Calendar.HOUR
            }

            TIMEZONE_FIELD -> {
                // z
                appendTimeZone(buffer, count, true)
            }

            // BEGIN android-changed
            (TIMEZONE_FIELD + 1) -> {
                // Z
                appendNumericTimeZone(buffer, false)
            }
            // END android-changed
        }
        if (field != -1) {
            appendNumber(buffer, count, calendar.get(field))
        }
    }

    /**
     * Append a representation of the time zone of 'calendar' to 'buffer'.
     *
     * @param count the number of z or Z characters in the format string; "zzz" would be 3,
     * for example.
     * @param generalTimeZone true if we should use a display name ("PDT") if available;
     * false implies that we should use RFC 822 format ("-0800") instead. This corresponds to 'z'
     * versus 'Z' in the format string.
     */
    private fun appendTimeZone(buffer: StringBuffer, count: Int, generalTimeZone: Boolean) {
        // BEGIN android-changed: optimized.
        if (generalTimeZone) {
            val tz: TimeZone = calendar!!.timeZone
            val daylight = (calendar!!.get(Calendar.DST_OFFSET) != 0)
            val style = if (count < 4) TimeZone.SHORT else TimeZone.LONG
            //if (!formatData.customZoneStrings)
            run {
                buffer.append(tz.getDisplayName(daylight, style, Locale.getDefault()))
                return
            }
            // We can't call TimeZone.getDisplayName() because it would not use
            // the custom DateFormatSymbols of this SimpleDateFormat.
///////////// String custom = Resources.lookupDisplayTimeZone(formatData.zoneStrings, tz.getID(), daylight, style);
/*            if (custom != null)
            {
                buffer.append(custom);
                return;
            }*/////////////////////////////////////////////////////////////////////////////////////
        }
        // We didn't find what we were looking for, so default to a numeric time zone.
        appendNumericTimeZone(buffer, generalTimeZone)
        // END android-changed
    }

    // BEGIN android-added: factored out duplication.
    /**
     * @param generalTimeZone "GMT-08:00" rather than "-0800".
     */
    private fun appendNumericTimeZone(buffer: StringBuffer, generalTimeZone: Boolean) {
        var offset = calendar!!.get(Calendar.ZONE_OFFSET) + calendar!!.get(Calendar.DST_OFFSET)
        var sign = '+'
        if (offset < 0) {
            sign = '-'
            offset = -offset
        }
        if (generalTimeZone) {
            buffer.append("GMT")
        }
        buffer.append(sign)
        appendNumber(buffer, 2, offset / 3600000)
        if (generalTimeZone) {
            buffer.append(':')
        }
        appendNumber(buffer, 2, (offset % 3600000) / 60000)
    }
    // END android-added

    private fun appendNumber(buffer: StringBuffer, count: Int, value: Int) {
        val numberFormat = this.numberFormat!!
        val minimumIntegerDigits = numberFormat.minimumIntegerDigits
        numberFormat.minimumIntegerDigits = count
        numberFormat.format(Integer.valueOf(value), buffer, FieldPosition(0))
        numberFormat.minimumIntegerDigits = minimumIntegerDigits
    }

    /**
     *
     * @param formatString
     * @return
     */
    private fun isDate(formatString: String): Boolean {
        var subString = formatString.replace("AM", "")
        subString = formatString.replace("PM", "")

        val len = datePatternChars.length
        var value = false
        var index = 0
        while (index < len) {
            if (subString.indexOf(datePatternChars[index]) > -1) {
                value = true
                break
            }
            index++
        }
        return value
    }

    /**
     *
     * @param formatString
     * @return
     */
    private fun isTime(formatString: String): Boolean {
        val len = timePatternChars.length
        var value = false
        var index = 0
        while (index < len) {
            if (formatString.indexOf(timePatternChars[index]) > -1) {
                value = true
                break
            }
            index++
        }
        return value
    }

    /**
     *
     */
    fun dispose() {
        calendar = null
        numberFormat = null
        dateTimeFormatData!!.dispose()
        dateTimeFormatData = null
    }

    companion object {
        const val patternChars = "GyMdkHmsSEDFwWahKzZ" //$NON-NLS-1$

        private const val datePatternChars = "GyMdEDFwWazZ"

        private const val timePatternChars = "HhsSkK" //'m'

        /**
         * The format style constant defining the full style.
         */
        const val FULL = 0

        /**
         * The format style constant defining the long style.
         */
        const val LONG = 1

        /**
         * The format style constant defining the medium style.
         */
        const val MEDIUM = 2

        /**
         * The format style constant defining the short style.
         */
        const val SHORT = 3

        /**
         * The `FieldPosition` selector for 'G' field alignment, corresponds
         * to the [Calendar.ERA] field.
         */
        const val ERA_FIELD = 0

        /**
         * The `FieldPosition` selector for 'y' field alignment, corresponds
         * to the [Calendar.YEAR] field.
         */
        const val YEAR_FIELD = 1

        /**
         * The `FieldPosition` selector for 'M' field alignment, corresponds
         * to the [Calendar.MONTH] field.
         */
        const val MONTH_FIELD = 2

        /**
         * The `FieldPosition` selector for 'd' field alignment, corresponds
         * to the [Calendar.DATE] field.
         */
        const val DATE_FIELD = 3

        /**
         * The `FieldPosition` selector for 'k' field alignment, corresponds
         * to the [Calendar.HOUR_OF_DAY] field. `HOUR_OF_DAY1_FIELD` is
         * used for the one-based 24-hour clock. For example, 23:59 + 01:00 results
         * in 24:59.
         */
        const val HOUR_OF_DAY1_FIELD = 4

        /**
         * The `FieldPosition` selector for 'H' field alignment, corresponds
         * to the [Calendar.HOUR_OF_DAY] field. `HOUR_OF_DAY0_FIELD` is
         * used for the zero-based 24-hour clock. For example, 23:59 + 01:00 results
         * in 00:59.
         */
        const val HOUR_OF_DAY0_FIELD = 5

        /**
         * FieldPosition selector for 'm' field alignment, corresponds to the
         * [Calendar.MINUTE] field.
         */
        const val MINUTE_FIELD = 6

        /**
         * FieldPosition selector for 's' field alignment, corresponds to the
         * [Calendar.SECOND] field.
         */
        const val SECOND_FIELD = 7

        /**
         * FieldPosition selector for 'S' field alignment, corresponds to the
         * [Calendar.MILLISECOND] field.
         */
        const val MILLISECOND_FIELD = 8

        /**
         * FieldPosition selector for 'E' field alignment, corresponds to the
         * [Calendar.DAY_OF_WEEK] field.
         */
        //public final static int DAY_OF_WEEK_FIELD = 9;

        /**
         * FieldPosition selector for 'D' field alignment, corresponds to the
         * [Calendar.DAY_OF_YEAR] field.
         */
        const val DAY_OF_YEAR_FIELD = 10

        /**
         * FieldPosition selector for 'F' field alignment, corresponds to the
         * [Calendar.DAY_OF_WEEK_IN_MONTH] field.
         */
        const val DAY_OF_WEEK_IN_MONTH_FIELD = 11

        /**
         * FieldPosition selector for 'w' field alignment, corresponds to the
         * [Calendar.WEEK_OF_YEAR] field.
         */
        const val WEEK_OF_YEAR_FIELD = 12

        /**
         * FieldPosition selector for 'W' field alignment, corresponds to the
         * [Calendar.WEEK_OF_MONTH] field.
         */
        const val WEEK_OF_MONTH_FIELD = 13

        /**
         * FieldPosition selector for 'a' field alignment, corresponds to the
         * [Calendar.AM_PM] field.
         */
        const val DAY_OF_WEEK_FIELD = 14

        /**
         * FieldPosition selector for 'h' field alignment, corresponding to the
         * [Calendar.HOUR] field. `HOUR1_FIELD` is used for the
         * one-based 12-hour clock. For example, 11:30 PM + 1 hour results in 12:30
         * AM.
         */
        const val HOUR1_FIELD = 15

        /**
         * The `FieldPosition` selector for 'z' field alignment, corresponds
         * to the [Calendar.ZONE_OFFSET] and [Calendar.DST_OFFSET]
         * fields.
         */
        const val HOUR0_FIELD = 16

        /**
         * The `FieldPosition` selector for 'z' field alignment, corresponds
         * to the [Calendar.ZONE_OFFSET] and [Calendar.DST_OFFSET]
         * fields.
         */
        const val TIMEZONE_FIELD = 17

//    /**
//     *
//     * @param cell
//     * @return
//     */
//    public static boolean isADateFormat(double value, int formatIndex, String formatString)
//    {
//        // Is it a date?
//        if (DateUtil.isADateFormat(formatIndex, formatString))
//        {
//            if (DateUtil.isValidExcelDate(value))
//            {
//                return true;
//            }
//        }
//
//        return false;
//    }

        @JvmStatic
        fun isDateTimeFormat(format: String): Boolean {
            var format = format
            //remove "scientific symbol E+"
            format = format.replace("E+", "")

            val len = patternChars.length
            var value = false
            var index = 0
            while (index < len) {
                if (format.indexOf(patternChars[index]) > -1) {
                    value = true
                    break
                }
                index++
            }
            return value
        }
    }
}
