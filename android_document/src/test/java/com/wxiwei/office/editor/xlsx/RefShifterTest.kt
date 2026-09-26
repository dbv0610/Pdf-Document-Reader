package com.wxiwei.office.editor.xlsx

import com.wxiwei.office.editor.xlsx.RefShifter.Change
import org.junit.Assert.assertEquals
import org.junit.Test

class RefShifterTest {
    private val ins = Change("Data", rows = true, at = 4, count = 2)   // 2 rows before row 5
    private val del = Change("Data", rows = true, at = 4, count = -2)  // rows 5..6 deleted
    private val insCol = Change("Data", rows = false, at = 1, count = 1) // before column B

    @Test fun insertRows() {
        assertEquals("A3+A7+\$B\$9", RefShifter.shift("A3+A5+\$B\$7", "Data", ins))
        assertEquals("SUM(G2:G76)", RefShifter.shift("SUM(G2:G74)", "Data", ins))
        assertEquals("SUM(G7:G9)", RefShifter.shift("SUM(G5:G7)", "Data", ins))
        assertEquals("SUM(2:9)", RefShifter.shift("SUM(2:7)", "Data", ins))
        assertEquals("SUM(A:A)", RefShifter.shift("SUM(A:A)", "Data", ins))
    }

    @Test fun otherSheets() {
        assertEquals("SUM('Data'!G2:G76)+A5", RefShifter.shift("SUM('Data'!G2:G74)+A5", "Summary", ins))
        assertEquals("Data!A7", RefShifter.shift("Data!A5", "Summary", ins))
        assertEquals("Other!A5", RefShifter.shift("Other!A5", "Data", ins))
        assertEquals("SUM('Dữ liệu chi tiết'!G2:G76)", RefShifter.shift("SUM('Dữ liệu chi tiết'!G2:G74)", "Tổng quan", Change("Dữ liệu chi tiết", true, 4, 2)))
    }

    @Test fun deleteRows() {
        assertEquals("A3+#REF!+A5", RefShifter.shift("A3+A5+A7", "Data", del))
        assertEquals("SUM(G2:G72)", RefShifter.shift("SUM(G2:G74)", "Data", del))
        assertEquals("SUM(#REF!)", RefShifter.shift("SUM(G5:G6)", "Data", del))
        assertEquals("SUM(G5:G5)", RefShifter.shift("SUM(G6:G7)", "Data", del))
    }

    @Test fun columnsAndText() {
        assertEquals("C1&\"B1\"&A1", RefShifter.shift("B1&\"B1\"&A1", "Data", insCol))
        assertEquals("SUM(A1:D1)", RefShifter.shift("SUM(A1:C1)", "Data", insCol))
        assertEquals("IF(OR(ISNUMBER(SEARCH(\"sdk\",E2))),\"X\",\"\")", RefShifter.shift("IF(OR(ISNUMBER(SEARCH(\"sdk\",D2))),\"X\",\"\")", "Data", Change("Data", false, 3, 1)))
        assertEquals("TEXT(E7,\"yyyy-mm\")", RefShifter.shift("TEXT(E5,\"yyyy-mm\")", "Data", ins))
        // function names and names are not references
        assertEquals("LOG10(A7)+MyName", RefShifter.shift("LOG10(A5)+MyName", "Data", ins))
    }

    @Test fun ranges() {
        assertEquals("B5:I118", RefShifter.shiftRange("B5:I116", Change("S", true, 10, 2)))
        assertEquals(null, RefShifter.shiftRange("B11:C12", Change("S", true, 10, -2)))
    }
}
