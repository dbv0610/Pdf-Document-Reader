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

    @Test
    fun pptx() = withReader("sample.pptx") { reader ->
        renderPages(reader, "pptx", 1920) { it.pageCount >= 10 }
    }

    @Test
    fun xlsx() = withReader("sample.xlsx") { reader ->
        val excel = findExcelView(reader.documentView!!)!!
        var sheets = 0
        instrumentation.runOnMainSync { sheets = excel.getSpreadsheet()!!.getSheetCount() }
        for (i in 0 until sheets) {
            instrumentation.runOnMainSync { excel.showSheet(i) }
            delay(2500)
            lateinit var bitmap: Bitmap
            instrumentation.runOnMainSync {
                bitmap = Bitmap.createBitmap(excel.width, excel.height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                excel.draw(Canvas(bitmap))
            }
            save(bitmap, "xlsx_sheet%d.png".format(i + 1))
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
