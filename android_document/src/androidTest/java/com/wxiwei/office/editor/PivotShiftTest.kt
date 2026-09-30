/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.xlsx.StructureWrite
import com.wxiwei.office.editor.xlsx.XlsxWriter
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** Rows inserted into a sheet: the pivot cache reading it and the pivot table sitting on it follow. */
@RunWith(AndroidJUnit4::class)
class PivotShiftTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun entry(file: File, name: String) = ZipFile(file).use { z -> z.getInputStream(z.getEntry(name)).readBytes().toString(Charsets.UTF_8) }

    @Test
    fun pivotSourceAndLocationFollowInsertedRows() {
        val blank = File(context.cacheDir, "pivot_blank.xlsx").apply { delete() }
        assertTrue(DocumentCreator.create(context, DocumentCreator.Format.EXCEL, blank) is EditResult.Ok)
        val sheetName = Regex("<sheet [^>]*name=\"([^\"]+)\"").find(entry(blank, "xl/workbook.xml"))!!.groupValues[1]
        val source = File(context.cacheDir, "pivot_source.xlsx").apply { delete() }
        ZipFile(blank).use { zip ->
            ZipOutputStream(source.outputStream()).use { out ->
                for (e in zip.entries()) { out.putNextEntry(ZipEntry(e.name)); out.write(zip.getInputStream(e).readBytes()); out.closeEntry() }
                fun put(name: String, text: String) { out.putNextEntry(ZipEntry(name)); out.write(text.toByteArray()); out.closeEntry() }
                val ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                put("xl/pivotCache/pivotCacheDefinition1.xml", "<pivotCacheDefinition xmlns=\"$ns\"><cacheSource type=\"worksheet\"><worksheetSource ref=\"A1:C10\" sheet=\"$sheetName\"/></cacheSource></pivotCacheDefinition>")
                put("xl/pivotTables/pivotTable1.xml", "<pivotTableDefinition xmlns=\"$ns\" name=\"P1\" cacheId=\"1\"><location ref=\"E3:G8\" firstHeaderRow=\"1\" firstDataRow=\"1\" firstDataCol=\"1\"/></pivotTableDefinition>")
                put("xl/worksheets/_rels/sheet1.xml.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rIdP1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotTable\" Target=\"../pivotTables/pivotTable1.xml\"/></Relationships>")
            }
        }
        val saved = File(context.cacheDir, "pivot_saved.xlsx").apply { delete() }
        // two rows before row 2
        val result = XlsxWriter(source).save(saved, emptyList(), structure = listOf(StructureWrite(0, true, 1, 2)))
        assertTrue(result.toString(), result is EditResult.Ok)
        assertTrue("source followed: " + entry(saved, "xl/pivotCache/pivotCacheDefinition1.xml"), entry(saved, "xl/pivotCache/pivotCacheDefinition1.xml").contains("ref=\"A1:C12\""))
        assertTrue("location followed: " + entry(saved, "xl/pivotTables/pivotTable1.xml"), entry(saved, "xl/pivotTables/pivotTable1.xml").contains("ref=\"E5:G10\""))
    }
}
