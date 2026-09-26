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

    private fun inject(action: Int, x: Float, y: Float, downTime: Long) {
        val event = android.view.MotionEvent.obtain(downTime, android.os.SystemClock.uptimeMillis(), action, x, y, 0)
        event.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
        instrumentation.uiAutomation.injectInputEvent(event, true)
        event.recycle()
    }

    @Test
    fun slideTapSelectAndDrag() {
        val file = sample("ppt2.pptx")
        val shapes = com.wxiwei.office.editor.pptx.PptxEditor(file).listShapes(0)
        // the top shape under the centre of the last text shape of slide 1
        val target = shapes.last { it.kind == com.wxiwei.office.editor.pptx.ShapeKind.TEXT && it.rectEmu.width > 0 }
        val r = target.rectEmu
        val center = com.wxiwei.office.editor.pptx.Point(r.x + r.width / 2, r.y + r.height / 2)
        val expected = com.wxiwei.office.editor.slide.SlideGeometry.hitTest(shapes, center)!!
        launch(file, DocumentType.Ppt).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            waitFor { viewer.state.value.status == ReaderState.Status.Ready }
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(500)
            var screen = floatArrayOf(0f, 0f)
            scenario.onActivity {
                val p = viewer.control!!.getView() as com.wxiwei.office.pg.control.Presentation
                val v = com.wxiwei.office.editor.slide.SlideGeometry.emuToView(p, com.wxiwei.office.editor.pptx.Rect(center.x, center.y, 1, 1))!!
                val o = IntArray(2); p.getLocationOnScreen(o)
                screen = floatArrayOf(v.left + o[0], v.top + o[1])
            }
            val down = android.os.SystemClock.uptimeMillis()
            inject(android.view.MotionEvent.ACTION_DOWN, screen[0], screen[1], down)
            inject(android.view.MotionEvent.ACTION_UP, screen[0], screen[1], down)
            Thread.sleep(1000)
            scenario.onActivity { a ->
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                assertTrue("tap selects #${expected.id}", find<TextView>(panel) { it is TextView && it.text.startsWith("#${expected.id} ") } != null)
            }
            screenshot("slide_selected")
            // drag the selection 120 px to the right
            val start = android.os.SystemClock.uptimeMillis()
            inject(android.view.MotionEvent.ACTION_DOWN, screen[0], screen[1], start)
            for (i in 1..10) { Thread.sleep(16); inject(android.view.MotionEvent.ACTION_MOVE, screen[0] + i * 12, screen[1], start) }
            inject(android.view.MotionEvent.ACTION_UP, screen[0] + 120, screen[1], start)
            Thread.sleep(1000)
            screenshot("slide_dragged")
            // rotate a quarter turn with the toolbar
            scenario.onActivity { a ->
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                find<TextView>(panel) { it is TextView && it.text.toString() == "⟳ 90°" }!!.performClick()
            }
            screenshot("slide_rotated")
            scenario.onActivity { a ->
                val panel = a.findViewById<ViewGroup>(R.id.editPanel)
                find<TextView>(panel) { it is TextView && it.text.toString() == "Lưu" }!!.performClick()
            }
            Thread.sleep(1500)
        }
        assertEquals(90f, com.wxiwei.office.editor.pptx.PptxEditor(file).listShapes(0).first { it.id == expected.id }.rotationDeg, 0.01f)
        val after = com.wxiwei.office.editor.pptx.PptxEditor(file).listShapes(0).first { it.id == expected.id }.rectEmu
        assertTrue("moved right: ${expected.rectEmu} -> $after", after.x > expected.rectEmu.x)
        assertEquals(expected.rectEmu.y, after.y)
        assertEquals(expected.rectEmu.width, after.width)
    }

    @Test
    fun wordTapAndType() {
        val file = sample("sample.docx")
        var start = -1L
        launch(file, DocumentType.Doc).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            waitFor { viewer.state.value.status == ReaderState.Status.Ready }
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(500)
            // tap the left half of the first letter of "CHƠI CÙNG"
            var screen = floatArrayOf(0f, 0f)
            scenario.onActivity {
                val word = viewer.control!!.getView() as com.wxiwei.office.wp.control.Word
                val map = com.wxiwei.office.editor.docx.DocxSourceMap.get(file.absolutePath)!!
                for (i in 0 until map.size) {
                    val l = map.leaf(i); val k = l.text.indexOf("CHƠI CÙNG")
                    if (k >= 0) { start = l.start + k; break }
                }
                val r = com.wxiwei.office.editor.word.WordSelection(word).rectsFor(start, start + 1).first()
                val o = IntArray(2); word.getLocationOnScreen(o)
                screen = floatArrayOf(o[0] + r.left + r.width() * 0.25f, o[1] + r.exactCenterY())
            }
            val down = android.os.SystemClock.uptimeMillis()
            inject(android.view.MotionEvent.ACTION_DOWN, screen[0], screen[1], down)
            inject(android.view.MotionEvent.ACTION_UP, screen[0], screen[1], down)
            Thread.sleep(1200)
            // type like a Telex keyboard: committed text, then a composing syllable that changes
            instrumentation.runOnMainSync {
                lateinit var typing: EditText
                scenario.onActivity { a -> typing = find(a.findViewById<ViewGroup>(R.id.editPanel)) { it is EditText && it.alpha == 0f }!! }
                assertTrue("typing field focused", typing.hasFocus())
                val ic = typing.onCreateInputConnection(android.view.inputmethod.EditorInfo())!!
                ic.commitText("Xin ", 1)
                ic.setComposingText("cha", 1)
                ic.setComposingText("chaa", 1)
                ic.setComposingText("châ", 1)
                ic.setComposingText("chào", 1)
                ic.finishComposingText()
                ic.commitText("! ", 1)
                // one Backspace inside the typed text
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL))
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DEL))
            }
            Thread.sleep(800)
            screenshot("word_typed")
            scenario.onActivity {
                val doc = (viewer.control!!.getView() as com.wxiwei.office.wp.control.Word).getDocument()
                val text = doc.getText(start, start + 30)
                assertTrue(text, text.startsWith("Xin chào!CHƠI CÙNG"))
            }
            scenario.onActivity { a ->
                find<TextView>(a.findViewById<ViewGroup>(R.id.editPanel)) { it is TextView && it.text.toString() == "Lưu" }!!.performClick()
            }
            Thread.sleep(2000)
        }
        val xml = java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry("word/document.xml")).readBytes().toString(Charsets.UTF_8) }
        val plain = Regex("<w:t(?: [^>]*)?>([^<]*)</w:t>").findAll(xml).joinToString("") { it.groupValues[1] }
        assertTrue("saved", plain.contains("Xin chào!CHƠI CÙNG"))
    }
}

