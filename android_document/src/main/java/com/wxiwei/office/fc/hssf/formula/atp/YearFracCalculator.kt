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
package com.wxiwei.office.fc.hssf.formula.atp

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.ss.util.DateUtil.Companion.setCalendar
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import kotlin.math.floor

/**
 * Internal calculation methods for Excel 'Analysis ToolPak' function YEARFRAC()<br></br>
 * 
 * Algorithm inspired by www.dwheeler.com/yearfrac
 * 
 * @author Josh Micich
 */
internal object YearFracCalculator {
    /** use UTC time-zone to avoid daylight savings issues  */
    private val UTC_TIME_ZONE: TimeZone? = TimeZone.getTimeZone("UTC")
    private val MS_PER_HOUR = 60 * 60 * 1000
    private val MS_PER_DAY = 24 * MS_PER_HOUR
    private const val DAYS_PER_NORMAL_YEAR = 365
    private val DAYS_PER_LEAP_YEAR = DAYS_PER_NORMAL_YEAR + 1

    /** the length of normal long months i.e. 31  */
    private const val LONG_MONTH_LEN = 31

    /** the length of normal short months i.e. 30  */
    private const val SHORT_MONTH_LEN = 30
    private const val SHORT_FEB_LEN = 28
    private val LONG_FEB_LEN = SHORT_FEB_LEN + 1

    @Throws(EvaluationException::class)
    fun calculate(pStartDateVal: Double, pEndDateVal: Double, basis: Int): Double {
        if (basis < 0 || basis >= 5) {
            // if basis is invalid the result is #NUM!
            throw EvaluationException(ErrorEval.NUM_ERROR)
        }

        // common logic for all bases

        // truncate day values
        var startDateVal = floor(pStartDateVal).toInt()
        var endDateVal = floor(pEndDateVal).toInt()
        if (startDateVal == endDateVal) {
            // when dates are equal, result is zero 
            return 0.0
        }
        // swap start and end if out of order
        if (startDateVal > endDateVal) {
            val temp = startDateVal
            startDateVal = endDateVal
            endDateVal = temp
        }

        when (basis) {
            0 -> return basis0(startDateVal, endDateVal)
            1 -> return basis1(startDateVal, endDateVal)
            2 -> return basis2(startDateVal, endDateVal)
            3 -> return basis3(startDateVal.toDouble(), endDateVal.toDouble())
            4 -> return basis4(startDateVal, endDateVal)
        }
        throw IllegalStateException("cannot happen")
    }


    /**
     * @param startDateVal assumed to be less than or equal to endDateVal
     * @param endDateVal assumed to be greater than or equal to startDateVal
     */
    fun basis0(startDateVal: Int, endDateVal: Int): Double {
        val startDate = createDate(startDateVal)
        val endDate = createDate(endDateVal)
        var date1day = startDate.day
        var date2day = endDate.day

        // basis zero has funny adjustments to the day-of-month fields when at end-of-month 
        if (date1day == LONG_MONTH_LEN && date2day == LONG_MONTH_LEN) {
            date1day = SHORT_MONTH_LEN
            date2day = SHORT_MONTH_LEN
        } else if (date1day == LONG_MONTH_LEN) {
            date1day = SHORT_MONTH_LEN
        } else if (date1day == SHORT_MONTH_LEN && date2day == LONG_MONTH_LEN) {
            date2day = SHORT_MONTH_LEN
            // Note: If date2day==31, it STAYS 31 if date1day < 30.
            // Special fixes for February:
        } else if (startDate.month == 2 && isLastDayOfMonth(startDate)) {
            // Note - these assignments deliberately set Feb 30 date.
            date1day = SHORT_MONTH_LEN
            if (endDate.month == 2 && isLastDayOfMonth(endDate)) {
                // only adjusted when first date is last day in Feb
                date2day = SHORT_MONTH_LEN
            }
        }
        return calculateAdjusted(startDate, endDate, date1day, date2day)
    }

    /**
     * @param startDateVal assumed to be less than or equal to endDateVal
     * @param endDateVal assumed to be greater than or equal to startDateVal
     */
    fun basis1(startDateVal: Int, endDateVal: Int): Double {
        val startDate = createDate(startDateVal)
        val endDate = createDate(endDateVal)
        val yearLength: Double
        if (isGreaterThanOneYear(startDate, endDate)) {
            yearLength = averageYearLength(startDate.year, endDate.year)
        } else if (shouldCountFeb29(startDate, endDate)) {
            yearLength = DAYS_PER_LEAP_YEAR.toDouble()
        } else {
            yearLength = DAYS_PER_NORMAL_YEAR.toDouble()
        }
        return dateDiff(startDate.tsMilliseconds, endDate.tsMilliseconds) / yearLength
    }

    /**
     * @param startDateVal assumed to be less than or equal to endDateVal
     * @param endDateVal assumed to be greater than or equal to startDateVal
     */
    fun basis2(startDateVal: Int, endDateVal: Int): Double {
        return (endDateVal - startDateVal) / 360.0
    }

    /**
     * @param startDateVal assumed to be less than or equal to endDateVal
     * @param endDateVal assumed to be greater than or equal to startDateVal
     */
    fun basis3(startDateVal: Double, endDateVal: Double): Double {
        return (endDateVal - startDateVal) / 365.0
    }

    /**
     * @param startDateVal assumed to be less than or equal to endDateVal
     * @param endDateVal assumed to be greater than or equal to startDateVal
     */
    fun basis4(startDateVal: Int, endDateVal: Int): Double {
        val startDate = createDate(startDateVal)
        val endDate = createDate(endDateVal)
        var date1day = startDate.day
        var date2day = endDate.day


        // basis four has funny adjustments to the day-of-month fields when at end-of-month 
        if (date1day == LONG_MONTH_LEN) {
            date1day = SHORT_MONTH_LEN
        }
        if (date2day == LONG_MONTH_LEN) {
            date2day = SHORT_MONTH_LEN
        }
        // Note - no adjustments for end of Feb
        return calculateAdjusted(startDate, endDate, date1day, date2day)
    }


    private fun calculateAdjusted(
        startDate: SimpleDate, endDate: SimpleDate, date1day: Int,
        date2day: Int
    ): Double {
        val dayCount =
            ((endDate.year - startDate.year) * 360 + (endDate.month - startDate.month) * SHORT_MONTH_LEN + (date2day - date1day) * 1).toDouble()
        return dayCount / 360
    }

    private fun isLastDayOfMonth(date: SimpleDate): Boolean {
        if (date.day < SHORT_FEB_LEN) {
            return false
        }
        return date.day == getLastDayOfMonth(date)
    }

    private fun getLastDayOfMonth(date: SimpleDate): Int {
        when (date.month) {
            1, 3, 5, 7, 8, 10, 12 -> return LONG_MONTH_LEN
            4, 6, 9, 11 -> return SHORT_MONTH_LEN
        }
        if (isLeapYear(date.year)) {
            return LONG_FEB_LEN
        }
        return SHORT_FEB_LEN
    }

    /**
     * Assumes dates are no more than 1 year apart.
     * @return `true` if dates both within a leap year, or span a period including Feb 29
     */
    private fun shouldCountFeb29(start: SimpleDate, end: SimpleDate): Boolean {
        val startIsLeapYear = isLeapYear(start.year)
        if (startIsLeapYear && start.year == end.year) {
            // note - dates may not actually span Feb-29, but it gets counted anyway in this case
            return true
        }

        val endIsLeapYear = isLeapYear(end.year)
        if (!startIsLeapYear && !endIsLeapYear) {
            return false
        }
        if (startIsLeapYear) {
            when (start.month) {
                SimpleDate.JANUARY, SimpleDate.FEBRUARY -> return true
            }
            return false
        }
        if (endIsLeapYear) {
            when (end.month) {
                SimpleDate.JANUARY -> return false
                SimpleDate.FEBRUARY -> {}
                else -> return true
            }
            return end.day == LONG_FEB_LEN
        }
        return false
    }

    /**
     * @return the whole number of days between the two time-stamps.  Both time-stamps are
     * assumed to represent 12:00 midnight on the respective day.
     */
    private fun dateDiff(startDateMS: Long, endDateMS: Long): Int {
        val msDiff = endDateMS - startDateMS

        // some extra checks to make sure we don't hide some other bug with the rounding 
        val remainderHours = ((msDiff % MS_PER_DAY) / MS_PER_HOUR).toInt()
        when (remainderHours) {
            0 -> {}
            1, 23 -> throw RuntimeException("Unexpected date diff between " + startDateMS + " and " + endDateMS)

            else -> throw RuntimeException("Unexpected date diff between " + startDateMS + " and " + endDateMS)

        }
        return (0.5 + (msDiff.toDouble() / MS_PER_DAY)).toInt()
    }

    private fun averageYearLength(startYear: Int, endYear: Int): Double {
        var dayCount = 0
        for (i in startYear..endYear) {
            dayCount += DAYS_PER_NORMAL_YEAR
            if (isLeapYear(i)) {
                dayCount++
            }
        }
        val numberOfYears = (endYear - startYear + 1).toDouble()
        return dayCount / numberOfYears
    }

    private fun isLeapYear(i: Int): Boolean {
        // leap years are always divisible by 4
        if (i % 4 != 0) {
            return false
        }
        // each 4th century is a leap year
        if (i % 400 == 0) {
            return true
        }
        // all other centuries are *not* leap years
        if (i % 100 == 0) {
            return false
        }
        return true
    }

    private fun isGreaterThanOneYear(start: SimpleDate, end: SimpleDate): Boolean {
        if (start.year == end.year) {
            return false
        }
        if (start.year + 1 != end.year) {
            return true
        }

        if (start.month > end.month) {
            return false
        }
        if (start.month < end.month) {
            return true
        }

        return start.day < end.day
    }

    private fun createDate(dayCount: Int): SimpleDate {
        val calendar = GregorianCalendar(UTC_TIME_ZONE)
        setCalendar(calendar, dayCount, 0, false)
        return SimpleDate(calendar)
    }

    private class SimpleDate(cal: Calendar) {
        val year: Int

        /** 1-based month  */
        val month: Int

        /** day of month  */
        val day: Int

        /** milliseconds since 1970  */
        var tsMilliseconds: Long

        init {
            year = cal.get(Calendar.YEAR)
            month = cal.get(Calendar.MONTH) + 1
            day = cal.get(Calendar.DAY_OF_MONTH)
            tsMilliseconds = cal.getTimeInMillis()
        }

        companion object {
            const val JANUARY: Int = 1
            const val FEBRUARY: Int = 2
        }
    }
}
