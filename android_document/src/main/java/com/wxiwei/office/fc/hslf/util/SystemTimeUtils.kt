package com.wxiwei.office.fc.hslf.util

import com.wxiwei.office.fc.util.LittleEndian
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

object SystemTimeUtils {
    @JvmStatic
    fun getDate(data: ByteArray): Date {
        return getDate(data, 0)
    }

    @JvmStatic
    fun getDate(data: ByteArray, offset: Int): Date {
        val cal: Calendar = GregorianCalendar()
        cal.set(Calendar.YEAR, LittleEndian.getShort(data, offset).toInt())
        cal.set(Calendar.MONTH, LittleEndian.getShort(data, offset + 2).toInt() - 1)
        cal.set(Calendar.DAY_OF_MONTH, LittleEndian.getShort(data, offset + 6).toInt())
        cal.set(Calendar.HOUR_OF_DAY, LittleEndian.getShort(data, offset + 8).toInt())
        cal.set(Calendar.MINUTE, LittleEndian.getShort(data, offset + 10).toInt())
        cal.set(Calendar.SECOND, LittleEndian.getShort(data, offset + 12).toInt())
        cal.set(Calendar.MILLISECOND, LittleEndian.getShort(data, offset + 14).toInt())
        return cal.time
    }

    @JvmStatic
    fun storeDate(date: Date, dest: ByteArray) {
        storeDate(date, dest, 0)
    }

    @JvmStatic
    fun storeDate(date: Date, dest: ByteArray, offset: Int) {
        val cal: Calendar = GregorianCalendar()
        cal.time = date
        LittleEndian.putShort(dest, offset, cal.get(Calendar.YEAR).toShort())
        LittleEndian.putShort(dest, offset + 2, (cal.get(Calendar.MONTH) + 1).toShort())
        LittleEndian.putShort(dest, offset + 4, (cal.get(Calendar.DAY_OF_WEEK) - 1).toShort())
        LittleEndian.putShort(dest, offset + 6, cal.get(Calendar.DAY_OF_MONTH).toShort())
        LittleEndian.putShort(dest, offset + 8, cal.get(Calendar.HOUR_OF_DAY).toShort())
        LittleEndian.putShort(dest, offset + 10, cal.get(Calendar.MINUTE).toShort())
        LittleEndian.putShort(dest, offset + 12, cal.get(Calendar.SECOND).toShort())
        LittleEndian.putShort(dest, offset + 14, cal.get(Calendar.MILLISECOND).toShort())
    }
}
