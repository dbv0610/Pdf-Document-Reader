package com.alf06.document.reader.edit

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alf06.document.reader.R
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.reader.ReaderState
import com.wxiwei.office.ss.control.ExcelView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Drives the edit toolbar of ReadDocumentActivity like a user: open an .xlsx, tap the edit button,
 * type into the selected cell, save, reopen. Screenshots go to files/edit-ui on the device.
 */
@RunWith(AndroidJUnit4::class)
class ReadDocumentEditTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val shots = File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }

    private fun sample(name: String): File {
        val file = File(context.filesDir, "edit-test-$name")
        instrumentation.context.assets.open("samples/$name").use { i -> file.outputStream().use { i.copyTo(it) } }
        return file
    }

    private fun launch(file: File, type: DocumentType): ActivityScenario<ReadDocumentActivity> {
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = type))
        return ActivityScenario.launch(intent)
    }

    private fun waitFor(timeoutMs: Long = 60_000, condition: () -> Boolean) {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            var ok = false
            instrumentation.runOnMainSync { ok = condition() }
            if (ok) return
            Thread.sleep(200)
        }
        throw AssertionError("timed out")
    }

    private fun screenshot(name: String) {
        Thread.sleep(600)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        File(shots, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    private fun <T : View> find(root: View, match: (View) -> Boolean): T? {
        if (match(root)) @Suppress("UNCHECKED_CAST") return root as T
        if (root is ViewGroup) for (i in 0 until root.childCount) find<T>(root.getChildAt(i), match)?.let { return it }
        return null
    }

    @Test
    fun excelEditToolbar() {
        val file = sample("sample.xlsx")
        launch(file, DocumentType.Excel).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            waitFor { viewer.state.value.status == ReaderState.Status.Ready }
            waitFor { scenario.let { var v = false; it.onActivity { a -> v = a.findViewById<View>(R.id.icEditApp).visibility == View.VISIBLE }; v } }
            Thread.sleep(2000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            // select "Tổng quan"!B21 (row 20, column 1), an empty cell
            scenario.onActivity {
                val excel = viewer.control!!.getView() as ExcelView
                excel.getSpreadsheet()!!.getSheetView()!!.getCurrentSheet()!!.setActiveCellRowCol(20, 1)
            }
            Thread.sleep(800)
            scenario.onActivity { a ->
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                val cellName = find<TextView>(panel) { it is TextView && it.text.toString() == "B21" }
                assertTrue("toolbar follows the selected cell", cellName != null)
                find<EditText>(panel) { it is EditText }!!.setText("=SUM(1,2,3)")
                find<TextView>(panel) { it is TextView && it.text.toString() == "✓" }!!.performClick()
                find<TextView>(panel) { it is TextView && it.text.toString() == "B" }!!.performClick()
            }
            screenshot("excel_toolbar")
            scenario.onActivity { a ->
                val excel = viewer.control!!.getView() as ExcelView
                val cell = excel.getSpreadsheet()!!.getWorkbook()!!.getSheet(0)!!.getRow(20)!!.getCell(1)!!
                assertEquals(6.0, cell.getNumberValue(), 0.0)
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                find<TextView>(panel) { it is TextView && it.text.toString() == "Lưu" }!!.performClick()
            }
            screenshot("excel_saved")
        }
        // reopen the saved file
        launch(file, DocumentType.Excel).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            waitFor { viewer.state.value.status == ReaderState.Status.Ready }
            Thread.sleep(2000)
            scenario.onActivity {
                val book = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!.getWorkbook()!!
                val cell = book.getSheet(0)!!.getRow(20)!!.getCell(1)!!
                assertEquals("SUM(1,2,3)", cell.formula)
                assertTrue("bold saved", book.getFont(cell.getCellStyle()!!.getFontIndex().toInt())!!.isBold())
            }
        }
    }

    @Test
    fun wordEditToolbarShows() {
        val file = sample("sample.docx")
        launch(file, DocumentType.Doc).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            waitFor { viewer.state.value.status == ReaderState.Status.Ready }
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            scenario.onActivity { a -> assertEquals(View.VISIBLE, a.findViewById<View>(R.id.editPanel).visibility) }
            // select "CHƠI CÙNG FILE MIDI" and make it italic from the toolbar
            var start = -1L
            scenario.onActivity {
                val word = viewer.control!!.getView() as com.wxiwei.office.wp.control.Word
                val map = com.wxiwei.office.editor.docx.DocxSourceMap.get(file.absolutePath)!!
                for (i in 0 until map.size) {
                    val l = map.leaf(i); val k = l.text.indexOf("CHƠI CÙNG")
                    if (k >= 0) { start = l.start + k; break }
                }
                com.wxiwei.office.editor.word.WordSelection(word).setSelection(start, start + "CHƠI CÙNG".length)
            }
            scenario.onActivity { a ->
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                find<TextView>(panel) { it is TextView && it.text.toString() == "I" }!!.performClick()
            }
            Thread.sleep(1500)
            scenario.onActivity {
                val doc = (viewer.control!!.getView() as com.wxiwei.office.wp.control.Word).getDocument()
                val italic = com.wxiwei.office.simpletext.model.AttrManage.instance()
                    .getFontItalic(doc.getParagraph(start + 1)!!.getAttribute(), doc.getLeaf(start + 1)!!.getAttribute())
                assertTrue("italic shows at once", italic)
            }
            screenshot("word_toolbar")
        }
    }
}
