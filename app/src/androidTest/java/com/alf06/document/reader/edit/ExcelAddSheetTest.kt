package com.alf06.document.reader.edit

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alf06.document.reader.R
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.reader.ReaderState
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.sheetbar.SheetButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** "+ Sheet" in the Excel edit bar: a new tab, shown, saved with the file. */
@RunWith(AndroidJUnit4::class)
class ExcelAddSheetTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun <T : View> find(root: View, match: (View) -> Boolean): T? {
        if (match(root)) @Suppress("UNCHECKED_CAST") return root as T
        if (root is ViewGroup) for (i in 0 until root.childCount) find<T>(root.getChildAt(i), match)?.let { return it }
        return null
    }

    private fun count(root: View, match: (View) -> Boolean): Int =
        (if (match(root)) 1 else 0) + if (root is ViewGroup) (0 until root.childCount).sumOf { count(root.getChildAt(it), match) } else 0

    @Test
    fun addSheetFromTheEditBar() {
        val file = File(context.filesDir, "edit-test-add-sheet.xlsx")
        instrumentation.context.assets.open("samples/sample.xlsx").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = DocumentType.Excel))
        var before = 0
        ActivityScenario.launch<ReadDocumentActivity>(intent).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            val end = System.currentTimeMillis() + 60_000
            while (viewer.state.value.status != ReaderState.Status.Ready && System.currentTimeMillis() < end) Thread.sleep(200)
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(800)
            scenario.onActivity { before = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!.getSheetCount() }
            scenario.onActivity { a -> find<View>(a.findViewById<ViewGroup>(R.id.editPanel)) { it.tag == "ADD_SHEET" }!!.performClick() }
            Thread.sleep(500)
            onView(withText(android.R.string.ok)).inRoot(isDialog()).perform(click())
            Thread.sleep(1500)
            scenario.onActivity {
                val excel = viewer.control!!.getView() as ExcelView
                val ss = excel.getSpreadsheet()!!
                assertEquals("one more sheet", before + 1, ss.getSheetCount())
                assertEquals("the new sheet is shown", before, ss.getWorkbook()!!.getSheetIndex(ss.getSheetView()!!.getCurrentSheet()))
                assertEquals("one more tab", before + 1, count(excel) { it is SheetButton })
            }
            instrumentation.uiAutomation.takeScreenshot()?.let { b ->
                File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }.resolve("excel_sheet_added.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
            scenario.onActivity { a -> find<View>(a.findViewById<ViewGroup>(R.id.editPanel)) { it.tag == "SAVE" }!!.performClick() }
            Thread.sleep(2500)
        }
        val workbook = java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry("xl/workbook.xml")).readBytes().toString(Charsets.UTF_8) }
        assertEquals("saved", before + 1, Regex("<sheet ").findAll(workbook).count())
    }

    private fun a11yTap(text: String) {
        var root = instrumentation.uiAutomation.rootInActiveWindow
        val end = System.currentTimeMillis() + 3000
        while (root == null && System.currentTimeMillis() < end) { Thread.sleep(100); root = instrumentation.uiAutomation.rootInActiveWindow }
        fun find(n: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
            n ?: return null; if (n.text?.toString() == text) return n
            for (i in 0 until n.childCount) find(n.getChild(i))?.let { return it }
            return null
        }
        val r = android.graphics.Rect()
        find(root)!!.getBoundsInScreen(r)
        val t = android.os.SystemClock.uptimeMillis()
        for (a in listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP)) {
            val e = android.view.MotionEvent.obtain(t, android.os.SystemClock.uptimeMillis(), a, r.exactCenterX(), r.exactCenterY(), 0)
            e.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
            instrumentation.uiAutomation.injectInputEvent(e, true); e.recycle()
        }
        Thread.sleep(800)
    }

    /** X4: "Xoay chữ" → 90° up on a text cell: turned in the model, its row grown, saved, read back. */
    @Test
    fun rotateCellText() {
        val file = File(context.filesDir, "edit-test-rotate.xlsx")
        instrumentation.context.assets.open("samples/sample.xlsx").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = DocumentType.Excel))
        var at = -1 to -1
        ActivityScenario.launch<ReadDocumentActivity>(intent).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            val end = System.currentTimeMillis() + 60_000
            while (viewer.state.value.status != ReaderState.Status.Ready && System.currentTimeMillis() < end) Thread.sleep(200)
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(800)
            var heightBefore = 0.0
            scenario.onActivity {
                val ss = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!
                val sheet = ss.getWorkbook()!!.getSheet(0)!!
                // a short text cell of the first rows
                loop@ for (r in 2..30) { val row = sheet.getRow(r) ?: continue; for (c in 0..6) { val cell = row.getCell(c) ?: continue
                    if (cell.getCellType() == com.wxiwei.office.ss.model.baseModel.Cell.CELL_TYPE_STRING && cell.getExpandedRangeAddressIndex() < 0) { at = r to c; break@loop } } }
                ss.getSheetView()!!.getCurrentSheet()!!.setActiveCellRowCol(at.first, at.second)
                heightBefore = sheet.getRow(at.first)!!.getRowPixelHeight().toDouble()
            }
            assertTrue("a text cell", at.first >= 0)
            Thread.sleep(800)
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "TEXT_ROTATION" }!!.performClick() }
            Thread.sleep(700)
            a11yTap(ui(com.wxiwei.office.R.string.docsdk_edit_rotate_vertical_up))
            Thread.sleep(800)
            scenario.onActivity {
                val sheet = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!.getWorkbook()!!.getSheet(0)!!
                assertEquals(90, sheet.getRow(at.first)!!.getCell(at.second)!!.getCellStyle()!!.getRotation().toInt())
                val h = sheet.getRow(at.first)!!.getRowPixelHeight().toDouble()
                android.util.Log.i("RotateTest", "cell $at row height $heightBefore -> $h")
                assertTrue("row grown for the turned text: $heightBefore -> $h", h > heightBefore)
            }
            instrumentation.uiAutomation.takeScreenshot()?.let { b ->
                File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }.resolve("excel_rotated.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "SAVE" }!!.performClick() }
            Thread.sleep(2500)
        }
        val styles = java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry("xl/styles.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("textRotation saved", styles.contains("textRotation=\"90\""))
    }

    /** Clicks the dialog view with this text or description through accessibility (works for rows scrolled out of sight). */
    private fun a11yClick(label: String) {
        var root = instrumentation.uiAutomation.rootInActiveWindow
        val end = System.currentTimeMillis() + 3000
        while (root == null && System.currentTimeMillis() < end) { Thread.sleep(100); root = instrumentation.uiAutomation.rootInActiveWindow }
        fun find(n: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
            n ?: return null
            if (n.text?.toString() == label || n.contentDescription?.toString() == label) return n
            for (i in 0 until n.childCount) find(n.getChild(i))?.let { return it }
            return null
        }
        assertTrue("clicked $label", find(root)!!.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
        Thread.sleep(300)
    }

    /** "Căn lề…": right, top, indent 2 in one step: shown on the cell, saved, undo brings the old alignment back. */
    @Test
    fun alignmentDialog() {
        val file = File(context.filesDir, "edit-test-align.xlsx")
        instrumentation.context.assets.open("samples/sample.xlsx").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = DocumentType.Excel))
        var at = -1 to -1
        ActivityScenario.launch<ReadDocumentActivity>(intent).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            val end = System.currentTimeMillis() + 60_000
            while (viewer.state.value.status != ReaderState.Status.Ready && System.currentTimeMillis() < end) Thread.sleep(200)
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(800)
            fun cellStyle() = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!.getWorkbook()!!.getSheet(0)!!.getRow(at.first)!!.getCell(at.second)!!.getCellStyle()!!
            var before = Triple<Short, Short, Short>(0, 0, 0)
            scenario.onActivity {
                val ss = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!
                val sheet = ss.getWorkbook()!!.getSheet(0)!!
                loop@ for (r in 2..30) { val row = sheet.getRow(r) ?: continue; for (c in 0..6) { val cell = row.getCell(c) ?: continue
                    if (cell.getCellType() == com.wxiwei.office.ss.model.baseModel.Cell.CELL_TYPE_STRING && cell.getExpandedRangeAddressIndex() < 0) { at = r to c; break@loop } } }
                ss.getSheetView()!!.getCurrentSheet()!!.setActiveCellRowCol(at.first, at.second)
                cellStyle().let { st -> before = Triple(st.getHorizontalAlign(), st.getVerticalAlign(), st.getIndent()) }
            }
            assertTrue("a text cell", at.first >= 0)
            Thread.sleep(800)
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "CELL_ALIGNMENT" }!!.performClick() }
            Thread.sleep(700)
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_right))
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_top))
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_indent_increase))
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_indent_increase))
            instrumentation.uiAutomation.takeScreenshot()?.let { b ->
                File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }.resolve("excel_align_dialog.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_apply))
            Thread.sleep(800)
            scenario.onActivity {
                val st = cellStyle()
                assertEquals(com.wxiwei.office.ss.model.style.CellStyle.ALIGN_RIGHT, st.getHorizontalAlign())
                assertEquals(com.wxiwei.office.ss.model.style.CellStyle.VERTICAL_TOP, st.getVerticalAlign())
                assertEquals(2, st.getIndent().toInt())
            }
            // one undo step for the whole dialog
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "UNDO" }!!.performClick() }
            Thread.sleep(500)
            scenario.onActivity { cellStyle().let { st -> assertEquals(before, Triple(st.getHorizontalAlign(), st.getVerticalAlign(), st.getIndent())) } }
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "REDO" }!!.performClick() }
            Thread.sleep(500)
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "SAVE" }!!.performClick() }
            Thread.sleep(2500)
        }
        val styles = java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry("xl/styles.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("alignment saved", Regex("<alignment[^>]*horizontal=\"right\"[^>]*").findAll(styles).any { it.value.contains("vertical=\"top\"") && it.value.contains("indent=\"2\"") })
    }

    private fun inject(action: Int, x: Float, y: Float, downTime: Long) {
        val e = android.view.MotionEvent.obtain(downTime, android.os.SystemClock.uptimeMillis(), action, x, y, 0)
        e.source = android.view.InputDevice.SOURCE_TOUCHSCREEN
        instrumentation.uiAutomation.injectInputEvent(e, true); e.recycle()
    }

    /** Long press A17 and drag to B19: the range is selected, filled red at once, merged (after the prompt), saved. */
    @Test
    fun rangeSelectFillAndMerge() {
        val file = File(context.filesDir, "edit-test-range.xlsx")
        instrumentation.context.assets.open("samples/sample.xlsx").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = DocumentType.Excel))
        ActivityScenario.launch<ReadDocumentActivity>(intent).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            val end = System.currentTimeMillis() + 60_000
            while (viewer.state.value.status != ReaderState.Status.Ready && System.currentTimeMillis() < end) Thread.sleep(200)
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(1000)
            fun ss() = (viewer.control!!.getView() as ExcelView).getSpreadsheet()!!
            /** Screen center of a cell. */
            fun center(row: Int, col: Int): Pair<Float, Float> {
                var p = 0f to 0f
                scenario.onActivity {
                    val r = com.wxiwei.office.ss.util.ModelUtil.instance().getCellAnchor(ss().getSheetView()!!, row, col)
                    val loc = IntArray(2); ss().getLocationOnScreen(loc)
                    p = (loc[0] + r.centerX()) to (loc[1] + r.centerY())
                }
                return p
            }
            val (x0, y0) = center(16, 0)
            val (x1, y1) = center(18, 1)
            val t = android.os.SystemClock.uptimeMillis()
            inject(android.view.MotionEvent.ACTION_DOWN, x0, y0, t)
            Thread.sleep(1000)
            for (k in 1..8) { inject(android.view.MotionEvent.ACTION_MOVE, x0 + (x1 - x0) * k / 8, y0 + (y1 - y0) * k / 8, t); Thread.sleep(40) }
            inject(android.view.MotionEvent.ACTION_UP, x1, y1, t)
            Thread.sleep(800)
            scenario.onActivity {
                val range = ss().getSheetView()!!.getCurrentSheet()!!.getSelectionRange()
                assertTrue("a range selected: $range", range != null)
                assertEquals(listOf(16, 0, 18, 1), listOf(range!!.getFirstRow(), range.getFirstColumn(), range.getLastRow(), range.getLastColumn()))
            }
            instrumentation.uiAutomation.takeScreenshot()?.let { b ->
                File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }.resolve("excel_range.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
            scenario.onActivity { a -> assertEquals("A17:B19", find<TextView>(a.findViewById(R.id.editPanel)) { it is TextView && it.contentDescription == a.getString(com.wxiwei.office.R.string.docsdk_edit_cell_or_range) }!!.text.toString()) }
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "FILL_COLOR" }!!.performClick() }
            Thread.sleep(700)
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_color_description, "FF0000"))
            Thread.sleep(700)
            scenario.onActivity {
                val sheet = ss().getWorkbook()!!.getSheet(0)!!
                for (r in 16..18) for (c in 0..1) {
                    val fill = sheet.getRow(r)!!.getCell(c)!!.getCellStyle()!!.getFillPattern()
                    assertTrue("filled $r,$c", fill != null)
                }
            }
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "MERGE_CELLS" }!!.performClick() }
            Thread.sleep(700)
            a11yClick(ui(com.wxiwei.office.R.string.docsdk_edit_merge))
            Thread.sleep(700)
            scenario.onActivity {
                val sheet = ss().getWorkbook()!!.getSheet(0)!!
                assertTrue("merged", sheet.mergeIndexAt(18, 1) >= 0 && sheet.mergeIndexAt(16, 0) == sheet.mergeIndexAt(18, 1))
                assertEquals("one cell selected again", null, sheet.getSelectionRange())
            }
            instrumentation.uiAutomation.takeScreenshot()?.let { b ->
                File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }.resolve("excel_merged.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
            scenario.onActivity { a -> find<View>(a.findViewById(R.id.editPanel)) { it.tag == "SAVE" }!!.performClick() }
            Thread.sleep(2500)
        }
        val xml = java.util.zip.ZipFile(file).use { z -> z.getInputStream(z.getEntry("xl/worksheets/sheet1.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("merge saved", xml.contains("<mergeCell ref=\"A17:B19\"/>"))
    }

    /** An SDK text in the language of the activity on screen. */
    private fun ui(id: Int, vararg args: Any): String {
        var text = ""
        instrumentation.runOnMainSync {
            val a = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).first()
            text = a.getString(id, *args)
        }
        return text
    }
}
