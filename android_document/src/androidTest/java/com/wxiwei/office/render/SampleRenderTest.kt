package com.wxiwei.office.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.reader.OfficeReader
import com.wxiwei.office.reader.ReaderConfig
import com.wxiwei.office.reader.ReaderState
import com.wxiwei.office.ss.control.ExcelView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Renders the samples in assets/samples to PNG, for comparing with the reference images before
 * and after a rendering change (see ROADMAP_RENDER_EDIT.md, G0.4). Output:
 * `adb pull /sdcard/Android/data/com.wxiwei.office.test/files/render`.
 */
@RunWith(AndroidJUnit4::class)
class SampleRenderTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val outDir = File(instrumentation.targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    private fun copySample(name: String): File {
        val file = File(instrumentation.targetContext.cacheDir, name)
        instrumentation.context.assets.open("samples/$name").use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        return file
    }

    private fun save(bitmap: Bitmap, name: String) {
        File(outDir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.i(TAG, "saved ${File(outDir, name)} ${bitmap.width}x${bitmap.height}")
        bitmap.recycle()
    }

    private fun withReader(sample: String, block: suspend (OfficeReader) -> Unit) {
        val path = copySample(sample).absolutePath
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var reader: OfficeReader
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)
                reader = OfficeReader(activity, container, ReaderConfig(showTxtEncodeDialog = false))
                reader.onOpenFailure = { true }
                reader.open(path)
            }
            runBlocking {
                val state = withTimeout(OPEN_TIMEOUT) {
                    reader.state.first { it.status == ReaderState.Status.Ready || it.status == ReaderState.Status.Failed }
                }
                assertEquals(state.error?.toString(), ReaderState.Status.Ready, state.status)
                block(reader)
            }
        }
    }

    /** Waits until the page count stops growing (Word lays out, PowerPoint loads in the background). */
    private suspend fun stablePageCount(reader: OfficeReader, done: (ReaderState) -> Boolean): Int {
        withTimeout(OPEN_TIMEOUT) { reader.state.first(done) }
        var last = -1
        while (true) {
            val count = reader.state.value.pageCount
            if (count == last && count > 0) return count
            last = count
            delay(1500)
        }
    }

    private suspend fun renderPages(reader: OfficeReader, prefix: String, width: Int, done: (ReaderState) -> Boolean) {
        val count = stablePageCount(reader, done)
        for (page in 1..count) {
            val bitmap = reader.thumbnails!!.render(page, width) ?: continue
            save(bitmap, "%s_%02d.png".format(prefix, page))
        }
    }

    @Test
    fun docx() = withReader("sample.docx") { reader ->
        renderPages(reader, "docx", 1240) { it.layout != null }
    }

    /** sample.docx page 1 with the layout boxes outlined (DebugBounds): paragraphs, lines, cells. */
    @Test
    fun docxBounds() = withReader("sample.docx") { reader ->
        com.wxiwei.office.wp.view.DebugBounds.enabled = true
        try {
            stablePageCount(reader) { it.layout != null }
            reader.thumbnails!!.render(1, 1240)?.let { save(it, "docx_bounds_01.png") }
        } finally {
            com.wxiwei.office.wp.view.DebugBounds.enabled = false
        }
    }

    /** Thesis template: nearly every page item is a floating text box or picture (wps/wpg). */
    @Test
    fun docTest() = withReader("doc_test.docx") { reader ->
        renderPages(reader, "doctest", 1240) { it.layout != null }
        // diagnosis: every floating shape the reader produced
        val manage = reader.control!!.getSysKit().getWPShapeManage()
        var i = 0
        while (i < 500) {
            val shape = manage.getShape(i) ?: break
            val wp = shape as? com.wxiwei.office.common.shape.WPAbstractShape
            Log.i(TAG, "shape#$i ${shape.javaClass.simpleName} bounds=${shape.bounds} wrap=${wp?.wrap} " +
                "h=${wp?.horizontalRelativeTo}/${wp?.horRelativeValue}/${wp?.horizontalAlignment} " +
                "v=${wp?.verticalRelativeTo}/${wp?.verRelativeValue}/${wp?.verticalAlignment} elem=${wp?.elementIndex}")
            i++
        }
        Log.i(TAG, "shapes=$i")
    }

    /** A floating text box anchored in the bottom-right table cell must sit at that cell. */
    @Test
    fun shapeInTable() = withReader("shape_in_table.docx") { reader ->
        renderPages(reader, "shapeintable", 1240) { it.layout != null }
    }

    /** Canva deck: titles with strong negative character spacing (spc) must stay on one line. */
    @Test
    fun ppt2() = withReader("ppt2.pptx") { reader ->
        renderPages(reader, "ppt2", 1920) { it.pageCount >= 10 }
    }

    @Test
    fun pptx() = withReader("sample.pptx") { reader ->
        renderPages(reader, "pptx", 1920) { it.pageCount >= 10 }
    }

    @Test
    fun xlsx() = renderSheets("sample.xlsx", "xlsx")

    /** sample.xlsx whose "Ghi chú dữ liệu" table uses the table style defined in styles.xml. */
    @Test
    fun customTableStyle() = renderSheets("custom_table_style.xlsx", "customtable")

    /** "Trùng ngày song song" at 30%: the picture anchored on rows 119-138, below hidden rows 111-116. */
    @Test
    fun xlsxPictureBelowHiddenRows() = withReader("sample.xlsx") { reader ->
        val excel = findExcelView(reader.documentView!!)!!
        instrumentation.runOnMainSync { excel.showSheet(2) }
        delay(2500)
        lateinit var bitmap: Bitmap
        instrumentation.runOnMainSync {
            val sheet = excel.getSpreadsheet()!!.getWorkbook()!!.getSheet(2)!!
            bitmap = Bitmap.createBitmap(1080, 1800, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            val canvas = Canvas(bitmap)
            canvas.clipRect(0, 0, 1080, 1800)
            excel.getSheetView()!!.drawRegion(sheet, 0, 0, 0.3f, canvas)
        }
        save(bitmap, "xlsx_picture_rows.png")
    }

    private fun renderSheets(sample: String, prefix: String) = withReader(sample) { reader ->
        val excel = findExcelView(reader.documentView!!)!!
        var sheets = 0
        instrumentation.runOnMainSync { sheets = excel.getSpreadsheet()!!.getSheetCount() }
        for (i in 0 until sheets) {
            instrumentation.runOnMainSync { excel.showSheet(i) }
            // wait until the sheet finished loading, then draw it from A1 at 100%
            delay(2500)
            lateinit var bitmap: Bitmap
            instrumentation.runOnMainSync {
                val sheet = excel.getSpreadsheet()!!.getWorkbook()!!.getSheet(i)!!
                bitmap = Bitmap.createBitmap(1080, 1400, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                val canvas = Canvas(bitmap)
                canvas.clipRect(0, 0, 1080, 1400)
                excel.getSheetView()!!.drawRegion(sheet, 0, 0, 1f, canvas)
            }
            save(bitmap, "%s_sheet%d.png".format(prefix, i + 1))
        }
    }

    private fun findExcelView(view: View): ExcelView? = when (view) {
        is ExcelView -> view
        is ViewGroup -> (0 until view.childCount).firstNotNullOfOrNull { findExcelView(view.getChildAt(it)) }
        else -> null
    }

    private companion object {
        const val TAG = "SampleRenderTest"
        const val OPEN_TIMEOUT = 180_000L
    }
}
