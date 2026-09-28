package com.alf06.document.reader.edit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alf06.document.reader.R
import com.alf06.document.reader.model.DocumentType
import com.alf06.document.reader.model.RecentDocument
import com.alf06.document.reader.ui.home.document.office.ReadDocumentActivity
import com.alf06.document.reader.ui.home.document.office.edit.writeSlide
import com.wxiwei.office.reader.OfficeDocumentView
import com.wxiwei.office.reader.ReaderState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** P3: slide 2 of ppt2.pptx exported as a PNG and as a one-page PDF; both show the slide. */
@RunWith(AndroidJUnit4::class)
class SlideExportTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    /** Share of pixels that are not white: a drawn slide has plenty. */
    private fun inked(b: Bitmap): Float {
        var n = 0; var total = 0
        for (y in 0 until b.height step 8) for (x in 0 until b.width step 8) { total++; if (b.getPixel(x, y) != Color.WHITE) n++ }
        return n.toFloat() / total
    }

    @Test
    fun exportSlidePngAndPdf() {
        val file = File(context.filesDir, "export-test.pptx")
        instrumentation.context.assets.open("samples/ppt2.pptx").use { i -> file.outputStream().use { i.copyTo(it) } }
        val intent = Intent(context, ReadDocumentActivity::class.java)
            .putExtra(ReadDocumentActivity.ARG_DOCUMENT, RecentDocument(path = file.absolutePath, size = file.length(), type = DocumentType.Ppt))
        val out = File(context.getExternalFilesDir(null), "edit-ui").apply { mkdirs() }
        val png = File(out, "slide2.png"); val pdf = File(out, "slide2.pdf")
        ActivityScenario.launch<ReadDocumentActivity>(intent).use { scenario ->
            lateinit var viewer: OfficeDocumentView
            scenario.onActivity { viewer = it.findViewById(R.id.officeViewer) }
            val end = System.currentTimeMillis() + 60_000
            while (viewer.state.value.status != ReaderState.Status.Ready && System.currentTimeMillis() < end) Thread.sleep(200)
            Thread.sleep(3000)
            scenario.onActivity { it.findViewById<View>(R.id.icEditApp).performClick() }
            Thread.sleep(800)
            scenario.onActivity {
                val p = viewer.control!!.getView() as com.wxiwei.office.pg.control.Presentation
                assertTrue(png.outputStream().use { o -> writeSlide(p, 1, false, o) })
                assertTrue(pdf.outputStream().use { o -> writeSlide(p, 1, true, o) })
            }
        }
        val image = BitmapFactory.decodeFile(png.absolutePath)
        assertEquals(1920, image.width)
        assertTrue("PNG shows the slide: ${inked(image)}", inked(image) > 0.2f)
        android.graphics.pdf.PdfRenderer(android.os.ParcelFileDescriptor.open(pdf, android.os.ParcelFileDescriptor.MODE_READ_ONLY)).use { r ->
            assertEquals(1, r.pageCount)
            r.openPage(0).use { page ->
                val b = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                b.eraseColor(Color.WHITE)
                page.render(b, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                android.util.Log.i("SlideExport", "pdf page ${page.width}x${page.height} inked ${inked(b)}; png inked ${inked(image)}")
                assertTrue("PDF shows the slide: ${inked(b)}", inked(b) > 0.2f)
                File(out, "slide2_pdf.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 90, it) }
            }
        }
    }
}
