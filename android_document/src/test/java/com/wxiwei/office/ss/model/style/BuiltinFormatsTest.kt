package com.wxiwei.office.ss.model.style

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Built-in number formats, including the East Asian date ids WPS writes (58 in sample.xlsx). */
class BuiltinFormatsTest {
    private fun isDate(f: String?) = f != null && f.contains('d') && f.contains('m') && f.contains('y')

    @Test fun commonFormats() {
        assertEquals("General", BuiltinFormats.getBuiltinFormat(0))
        assertEquals("0.00", BuiltinFormats.getBuiltinFormat(2))
        assertEquals("0%", BuiltinFormats.getBuiltinFormat(9))
    }

    @Test fun localeDatesAreDates() {
        for (id in listOf(14, 27, 30, 36, 50, 54, 58)) assertTrue("id $id: ${BuiltinFormats.getBuiltinFormat(id)}", isDate(BuiltinFormats.getBuiltinFormat(id)))
    }

    @Test fun timesAreTimes() {
        assertEquals("h:mm", BuiltinFormats.getBuiltinFormat(55))
        assertEquals("h:mm:ss", BuiltinFormats.getBuiltinFormat(56))
    }
}
