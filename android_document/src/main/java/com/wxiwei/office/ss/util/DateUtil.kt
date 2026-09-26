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

package com.wxiwei.office.ss.util

import com.wxiwei.office.fc.ss.usermodel.ICell
import com.wxiwei.office.ss.model.baseModel.Cell
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.regex.Pattern

/**
 * Contains methods for dealing with Excel dates.
 *
 * @author  Michael Harhen
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Hack Kampbjorn (hak at 2mba.dk)
 * @author  Alex Jacoby (ajacoby at gmail.com)
 * @author  Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
open class DateUtil protected constructor() {
    // no instances of this class

    private class FormatException(msg: String?) : Exception(msg)

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        private const val MINUTES_PER_HOUR = 60
        private const val HOURS_PER_DAY = 24
        private const val SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE)

        private const val BAD_DATE = -1 // used to specify that date is invalid
        private const val DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L

        private val TIME_SEPARATOR_PATTERN: Pattern = Pattern.compile(":")

        /**
         * The following patterns are used in [isADateFormat]
         */
        private val date_ptrn1: Pattern = Pattern.compile("^\\[\\$\\-.*?\\]")
        private val date_ptrn2: Pattern = Pattern.compile("^\\[[a-zA-Z]+\\]")
        private val date_ptrn3: Pattern = Pattern
            .compile("^[\\[\\]yYmMdDhHsS\\-/,. :\"\\\\]+0*[ampAMP/]*$")

        //  elapsed time patterns: [h],[m] and [s]
        private val date_ptrn4: Pattern = Pattern.compile("^\\[([hH]+|[mM]+|[sS]+)\\]")

        /**
         * Given a Date, converts it into a double representing its internal Excel representation,
         * which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
         *
         * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
         * @param  date the Date
         */
        @JvmStatic
        fun getExcelDate(date: Date?): Double {
            return getExcelDate(date, false)
        }

        /**
         * Given a Date, converts it into a double representing its internal Excel representation,
         * which is the number of days since 1/1/1900. Fractional days represent hours, minutes, and seconds.
         *
         * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
         * @param date the Date
         * @param use1904windowing Should 1900 or 1904 date windowing be used?
         */
        @JvmStatic
        fun getExcelDate(date: Date?, use1904windowing: Boolean): Double {
            val calStart: Calendar = GregorianCalendar()
            calStart.time = date // If date includes hours, minutes, and seconds, set them to 0
            return internalGetExcelDate(calStart, use1904windowing)
        }

        /**
         * Given a Date in the form of a Calendar, converts it into a double
         * representing its internal Excel representation, which is the
         * number of days since 1/1/1900. Fractional days represent hours,
         * minutes, and seconds.
         *
         * @return Excel representation of Date (-1 if error - test for error by checking for less than 0.1)
         * @param date the Calendar holding the date to convert
         * @param use1904windowing Should 1900 or 1904 date windowing be used?
         */
        @JvmStatic
        fun getExcelDate(date: Calendar, use1904windowing: Boolean): Double {
            // Don't alter the supplied Calendar as we do our work
            return internalGetExcelDate(date.clone() as Calendar, use1904windowing)
        }

        private fun internalGetExcelDate(date: Calendar, use1904windowing: Boolean): Double {
            if ((!use1904windowing && date.get(Calendar.YEAR) < 1900)
                || (use1904windowing && date.get(Calendar.YEAR) < 1904)
            ) {
                return BAD_DATE.toDouble()
            }
            // Because of daylight time saving we cannot use
            //     date.getTime() - calStart.getTimeInMillis()
            // as the difference in milliseconds between 00:00 and 04:00
            // can be 3, 4 or 5 hours but Excel expects it to always
            // be 4 hours.
            // E.g. 2004-03-28 04:00 CEST - 2004-03-28 00:00 CET is 3 hours
            // and 2004-10-31 04:00 CET - 2004-10-31 00:00 CEST is 5 hours
            val fraction = ((((date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE)) * 60 + date
                .get(Calendar.SECOND)) * 1000 + date.get(Calendar.MILLISECOND))
                / DAY_MILLISECONDS.toDouble())
            val calStart = dayStart(date)

            var value = fraction + absoluteDay(calStart, use1904windowing)

            if (!use1904windowing && value >= 60) {
                value++
            } else if (use1904windowing) {
                value--
            }

            return value
        }

        /**
         * Given an Excel date with using 1900 date windowing, and
         * converts it to a java.util.Date.
         *
         * NOTE: If the default `TimeZone` in Java uses Daylight
         * Saving Time then the conversion back to an Excel date may not give
         * the same value, that is the comparison
         * `excelDate == getExcelDate(getJavaDate(excelDate,false))`
         * is not always true. For example if default timezone is
         * `Europe/Copenhagen`, on 2004-03-28 the minute after
         * 01:59 CET is 03:00 CEST, if the excel date represents a time between
         * 02:00 and 03:00 then it is converted to past 03:00 summer time
         *
         * @param date  The Excel date.
         * @return Java representation of the date, or null if date is not a valid Excel date
         * @see java.util.TimeZone
         */
        @JvmStatic
        fun getJavaDate(date: Double): Date? {
            return getJavaDate(date, false)
        }

        /**
         * Given an Excel date with either 1900 or 1904 date windowing,
         * converts it to a java.util.Date.
         *
         * NOTE: If the default `TimeZone` in Java uses Daylight
         * Saving Time then the conversion back to an Excel date may not give
         * the same value, that is the comparison
         * `excelDate == getExcelDate(getJavaDate(excelDate,false))`
         * is not always true. For example if default timezone is
         * `Europe/Copenhagen`, on 2004-03-28 the minute after
         * 01:59 CET is 03:00 CEST, if the excel date represents a time between
         * 02:00 and 03:00 then it is converted to past 03:00 summer time
         *
         * @param date  The Excel date.
         * @param use1904windowing  true if date uses 1904 windowing,
         * or false if using 1900 date windowing.
         * @return Java representation of the date, or null if date is not a valid Excel date
         * @see java.util.TimeZone
         */
        @JvmStatic
        fun getJavaDate(date: Double, use1904windowing: Boolean): Date? {
            if (!isValidExcelDate(date)) {
                return null
            }
            val wholeDays = Math.floor(date).toInt()
            val millisecondsInDay = ((date - wholeDays) * DAY_MILLISECONDS + 0.5).toInt()
            val calendar: Calendar = GregorianCalendar() // using default time-zone
            setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing)
            return calendar.time
        }

        @JvmStatic
        fun setCalendar(
            calendar: Calendar, wholeDays: Int, millisecondsInDay: Int,
            use1904windowing: Boolean
        ) {
            var startYear = 1900
            var dayAdjust = -1 // Excel thinks 2/29/1900 is a valid date, which it isn't
            if (use1904windowing) {
                startYear = 1904
                dayAdjust = 1 // 1904 date windowing uses 1/2/1904 as the first day
            } else if (wholeDays < 61) {
                // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
                // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
                dayAdjust = 0
            }
            calendar.set(startYear, 0, wholeDays + dayAdjust, 0, 0, 0)
            calendar.set(GregorianCalendar.MILLISECOND, millisecondsInDay)
        }

        /**
         * Given a format ID and its format String, will check to see if the
         * format represents a date format or not.
         * Firstly, it will check to see if the format ID corresponds to an
         * internal excel date format (eg most US date formats)
         * If not, it will check to see if the format string only contains
         * date formatting characters (ymd-/), which covers most
         * non US date formats.
         *
         * @param formatIndex The index of the format, eg from ExtendedFormatRecord.getFormatIndex
         * @param formatString The format string, eg from FormatRecord.getFormatString
         * @see isInternalDateFormat
         */
        @JvmStatic
        fun isADateFormat(formatIndex: Int, formatString: String?): Boolean {
            // First up, is this an internal date format?
            if (isInternalDateFormat(formatIndex)) {
                return true
            }

            // If we didn't get a real string, it can't be
            if (formatString == null || formatString.length == 0) {
                return false
            }

            var fs: String = formatString
            @Suppress("ConstantConditionIf")
            if (false) {
                // Normalize the format string. The code below is equivalent
                // to the following consecutive regexp replacements:

                // Translate \- into just -, before matching
                fs = fs.replace("\\\\-".toRegex(), "-")
                // And \, into ,
                fs = fs.replace("\\\\,".toRegex(), ",")
                // And \. into .
                fs = fs.replace("\\\\\\.".toRegex(), ".")
                // And '\ ' into ' '
                fs = fs.replace("\\\\ ".toRegex(), " ")

                // If it end in ;@, that's some crazy dd/mm vs mm/dd
                //  switching stuff, which we can ignore
                fs = fs.replace(";@".toRegex(), "")

                // The code above was reworked as suggested in bug 48425:
                // simple loop is more efficient than consecutive regexp replacements.
            }
            val sb = StringBuilder(fs.length)
            var i = 0
            while (i < fs.length) {
                val c = fs[i]
                if (i < fs.length - 1) {
                    val nc = fs[i + 1]
                    if (c == '\\') {
                        when (nc) {
                            '-', ',', '.', ' ', '\\' -> {
                                // skip current '\' and continue to the next char
                                i++
                                continue
                            }
                        }
                    } else if (c == ';' && nc == '@') {
                        i++
                        // skip ";@" duplets
                        i++
                        continue
                    }
                }
                sb.append(c)
                i++
            }
            fs = sb.toString()

            // short-circuit if it indicates elapsed time: [h], [m] or [s]
            if (date_ptrn4.matcher(fs).matches()) {
                return true
            }

            // If it starts with [$-...], then could be a date, but
            //  who knows what that starting bit is all about
            fs = date_ptrn1.matcher(fs).replaceAll("")
            // If it starts with something like [Black] or [Yellow],
            //  then it could be a date
            fs = date_ptrn2.matcher(fs).replaceAll("")
            // You're allowed something like dd/mm/yy;[red]dd/mm/yy
            //  which would place dates before 1900/1904 in red
            // For now, only consider the first one
            if (fs.indexOf(';') > 0 && fs.indexOf(';') < fs.length - 1) {
                fs = fs.substring(0, fs.indexOf(';'))
            }

            // Otherwise, check it's only made up, in any case, of:
            //  y m d h s - \ / , . :
            // optionally followed by AM/PM
            return date_ptrn3.matcher(fs).matches()
        }

        /**
         * Given a format ID this will check whether the format represents
         * an internal excel date format or not.
         * @see isADateFormat
         */
        @JvmStatic
        fun isInternalDateFormat(format: Int): Boolean {
            when (format) {
                // Internal Date Formats as described on page 427 in
                // Microsoft Excel Dev's Kit...
                0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x2d, 0x2e, 0x2f -> return true
            }
            return false
        }

        /**
         * Check if a cell contains a date
         * Since dates are stored internally in Excel as double values
         * we infer it is a date if it is formatted as such.
         * @see isADateFormat
         * @see isInternalDateFormat
         */
        @JvmStatic
        fun isCellDateFormatted(cell: Cell?): Boolean {
            if (cell == null) {
                return false
            }
            var bDate = false

            val d = cell.getNumberValue()
            if (DateUtil.isValidExcelDate(d)) {
                val style = cell.getCellStyle()
                if (style == null) {
                    return false
                }
                val i = style.getNumberFormatID().toInt()
                val f = style.getFormatCode()
                bDate = isADateFormat(i, f)
            }
            return bDate
        }

        /**
         * Check if a cell contains a date, checking only for internal
         * excel date formats.
         * As Excel stores a great many of its dates in "non-internal"
         * date formats, you will not normally want to use this method.
         * @see isADateFormat
         * @see isInternalDateFormat
         */
        @JvmStatic
        fun isCellInternalDateFormatted(cell: ICell?): Boolean {
            if (cell == null) {
                return false
            }
            var bDate = false

            val d = cell.numericCellValue
            if (DateUtil.isValidExcelDate(d)) {
                val style = cell.cellStyle
                val i = style.dataFormat.toInt()
                bDate = isInternalDateFormat(i)
            }
            return bDate
        }

        /**
         * Given a double, checks if it is a valid Excel date.
         *
         * @return true if valid
         * @param  value the double value
         */
        @JvmStatic
        fun isValidExcelDate(value: Double): Boolean {
            return (value > -java.lang.Double.MIN_VALUE)
        }

        /**
         * Given a Calendar, return the number of days since 1900/12/31.
         *
         * @return days number of days since 1900/12/31
         * @param  cal the Calendar
         * @exception IllegalArgumentException if date is invalid
         */
        @JvmStatic
        protected fun absoluteDay(cal: Calendar, use1904windowing: Boolean): Int {
            return (cal.get(Calendar.DAY_OF_YEAR)
                + daysInPriorYears(cal.get(Calendar.YEAR), use1904windowing))
        }

        /**
         * Return the number of days in prior years since 1900
         *
         * @return    days  number of days in years prior to yr.
         * @param     yr    a year (1900 < yr < 4000)
         * @param use1904windowing
         * @exception IllegalArgumentException if year is outside of range.
         */
        private fun daysInPriorYears(yr: Int, use1904windowing: Boolean): Int {
            if ((!use1904windowing && yr < 1900) || (use1904windowing && yr < 1900)) {
                throw IllegalArgumentException("'year' must be 1900 or greater")
            }

            val yr1 = yr - 1
            val leapDays = (yr1 / 4 // plus julian leap days in prior years
                - yr1 / 100 // minus prior century years
                + yr1 / 400 // plus years divisible by 400
                - 460) // leap days in previous 1900 years

            return 365 * (yr - (if (use1904windowing) 1904 else 1900)) + leapDays
        }

        // set HH:MM:SS fields of cal to 00:00:00:000
        private fun dayStart(cal: Calendar): Calendar {
            cal.get(Calendar.HOUR_OF_DAY) // force recalculation of internal fields
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.get(Calendar.HOUR_OF_DAY) // force recalculation of internal fields
            return cal
        }

        /**
         * Converts a string of format "HH:MM" or "HH:MM:SS" to its (Excel) numeric equivalent
         *
         * @return a double between 0 and 1 representing the fraction of the day
         */
        @JvmStatic
        fun convertTime(timeStr: String): Double {
            try {
                return convertTimeInternal(timeStr)
            } catch (e: FormatException) {
                val msg = ("Bad time format '" + timeStr + "' expected 'HH:MM' or 'HH:MM:SS' - "
                    + e.message)
                throw IllegalArgumentException(msg)
            }
        }

        @Throws(FormatException::class)
        private fun convertTimeInternal(timeStr: String): Double {
            val len = timeStr.length
            if (len < 4 || len > 8) {
                throw FormatException("Bad length")
            }
            val parts = TIME_SEPARATOR_PATTERN.split(timeStr)

            val secStr: String
            when (parts.size) {
                2 -> secStr = "00"
                3 -> secStr = parts[2]
                else -> throw FormatException("Expected 2 or 3 fields but got (" + parts.size + ")")
            }
            val hourStr = parts[0]
            val minStr = parts[1]
            val hours = parseInt(hourStr, "hour", HOURS_PER_DAY)
            val minutes = parseInt(minStr, "minute", MINUTES_PER_HOUR)
            val seconds = parseInt(secStr, "second", SECONDS_PER_MINUTE)

            val totalSeconds = (seconds + (minutes + (hours) * 60) * 60).toDouble()
            return totalSeconds / (SECONDS_PER_DAY)
        }

        /**
         * Converts a string of format "YYYY/MM/DD" to its (Excel) numeric equivalent
         *
         * @return a double representing the (integer) number of days since the start of the Excel epoch
         */
        @JvmStatic
        fun parseYYYYMMDDDate(dateStr: String): Date {
            try {
                return parseYYYYMMDDDateInternal(dateStr)
            } catch (e: FormatException) {
                val msg = ("Bad time format " + dateStr + " expected 'YYYY/MM/DD' - "
                    + e.message)
                throw IllegalArgumentException(msg)
            }
        }

        @Throws(FormatException::class)
        private fun parseYYYYMMDDDateInternal(timeStr: String): Date {
            if (timeStr.length != 10) {
                throw FormatException("Bad length")
            }

            val yearStr = timeStr.substring(0, 4)
            val monthStr = timeStr.substring(5, 7)
            val dayStr = timeStr.substring(8, 10)
            val year = parseInt(yearStr, "year", Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            val month = parseInt(monthStr, "month", 1, 12)
            val day = parseInt(dayStr, "day", 1, 31)

            val cal: Calendar = GregorianCalendar(year, month - 1, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        @Throws(FormatException::class)
        private fun parseInt(strVal: String, fieldName: String, rangeMax: Int): Int {
            return parseInt(strVal, fieldName, 0, rangeMax - 1)
        }

        @Throws(FormatException::class)
        private fun parseInt(strVal: String, fieldName: String, lowerLimit: Int, upperLimit: Int): Int {
            val result: Int
            try {
                result = Integer.parseInt(strVal)
            } catch (e: NumberFormatException) {
                throw FormatException("Bad int format '" + strVal + "' for " + fieldName + " field")
            }
            if (result < lowerLimit || result > upperLimit) {
                throw FormatException(
                    fieldName + " value (" + result
                        + ") is outside the allowable range(0.." + upperLimit + ")"
                )
            }
            return result
        }
    }
}
