/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.editor.pptx.LivePptxSession
import com.wxiwei.office.editor.pptx.PptxEditor
import com.wxiwei.office.editor.pptx.Rect
import com.wxiwei.office.editor.xlsx.SheetEditSession
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.wp.control.Word
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.zip.ZipFile

@RunWith(AndroidJUnit4::class)
class DocumentCreatorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun create(format: DocumentCreator.Format) = OpenDocument.output("created.${format.extension}").also {
        val result = DocumentCreator.create(context, format, it)
        assertTrue(result.toString(), result is EditResult.Ok)
    }

    @Test fun blankWordTypeSaveReopen() {
        val source = create(DocumentCreator.Format.WORD)
        val saved = OpenDocument.output("created-saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.insertText(0, "Tài liệu mới\nDòng thứ hai") })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val text = onMain { (reader.control!!.getView() as Word).getDocument().getText(0, 25) }
            assertTrue(text, text.startsWith("Tài liệu mới\nDòng thứ hai"))
        }
    }

    @Test fun blankExcelEnterFormulaSaveReopen() {
        val source = create(DocumentCreator.Format.EXCEL)
        val saved = OpenDocument.output("created-saved.xlsx")
        OpenDocument.open(source) { reader ->
            val session = onMain { SheetEditSession(reader.control!!, source) }
            assertTrue(onMain { session.setCellInput(0, 0, 0, "Bảng tính mới") })
            assertTrue(onMain { session.setCellInput(0, 1, 0, "=SUM(1,2,3)") })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved) { reader ->
            onMain {
                val book = (reader.control!!.getView() as ExcelView).getSpreadsheet()!!.getWorkbook()!!
                val cell = book.getSheet(0)!!.getRow(1)!!.getCell(0)!!
                assertEquals("SUM(1,2,3)", cell.formula)
                assertEquals(6.0, cell.getNumberValue(), 0.0)
            }
        }
    }

    @Test fun blankPowerpointAddTextAndSlideSaveReopen() {
        val source = create(DocumentCreator.Format.POWERPOINT)
        val saved = OpenDocument.output("created-saved.pptx")
        OpenDocument.open(source, { it.pageCount > 0 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain {
                session.addTextBox(0, Rect(914400, 914400, 4572000, 914400), "Bản trình chiếu mới", 32f) > 0
            })
            assertTrue(onMain { session.addBlankSlide(0) })
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.pageCount == 2 }) {
            assertTrue(PptxEditor(saved).listShapes(0).any { it.text == "Bản trình chiếu mới" })
        }
    }

    @Test fun rejectsExistingFilesAndWrongExtensions() {
        for (format in DocumentCreator.Format.entries) {
            val source = create(format)
            val before = source.readBytes()
            assertTrue(DocumentCreator.create(context, format, source) is EditResult.Error)
            assertArrayEquals(before, source.readBytes())
            ZipFile(source).use { assertNotNull(it.getEntry("[Content_Types].xml")) }
        }
        val wrong = OpenDocument.output("created.invalid")
        assertTrue(DocumentCreator.create(context, DocumentCreator.Format.WORD, wrong) is EditResult.Error)
        assertFalse(wrong.exists())
    }
}
