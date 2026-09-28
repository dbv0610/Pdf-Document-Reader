/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reader.pdfviewer.pdfium.PdfFormField
import com.reader.pdfviewer.pdfium.PdfiumCore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Edits through the viewer: shapes moved and resized with undo, notes, form fields, page geometry, then saved. */
@RunWith(AndroidJUnit4::class)
class PdfViewEditTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        instrumentation.runOnMainSync { result = runCatching(block) }
        return result!!.getOrThrow()
    }

    private fun textPdf(name: String, pages: Int): File {
        val out = File(context.cacheDir, name).also { it.delete() }
        val doc = android.graphics.pdf.PdfDocument()
        val paint = Paint().apply { textSize = 24f }
        for (i in 0 until pages) {
            val page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, i + 1).create())
            page.canvas.drawText("Page ${i + 1}", 80f, 120f, paint)
            doc.finishPage(page)
        }
        out.outputStream().use { doc.writeTo(it) }
        doc.close()
        return out
    }

    /** Opens [file] in a PDFView filling an activity, then runs [block] (off the main thread). */
    private fun withView(file: File, block: (PDFView) -> Unit) {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            lateinit var view: PDFView
            val loaded = CountDownLatch(1)
            scenario.onActivity { activity ->
                val container = FrameLayout(activity)
                activity.setContentView(container)
                view = PDFView(activity, null)
                container.addView(view, FrameLayout.LayoutParams(-1, -1))
                view.fromFile(file).enableAnnotationRendering(true).onLoad { loaded.countDown() }.load()
            }
            assertTrue("loaded", loaded.await(30, TimeUnit.SECONDS))
            // shown once laid out after the load
            val until = System.currentTimeMillis() + 10_000
            while (onMain { view.pageViewRect(0) } == null && System.currentTimeMillis() < until) Thread.sleep(50)
            instrumentation.waitForIdleSync()
            block(view)
        }
    }

    private fun annotations(file: File): List<Pair<String?, RectF>> {
        val pdfium = PdfiumCore(context)
        val doc = pdfium.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        try {
            pdfium.openPage(doc, 0)
            return pdfium.getAnnotations(doc, 0).map { it.name to it.rect }
        } finally {
            pdfium.closeDocument(doc)
        }
    }

    @Test
    fun moveResizeUndoAndSave() {
        val file = textPdf("view_edit.pdf", 2)
        val saved = File(context.cacheDir, "view_edit_saved.pdf").also { it.delete() }
        withView(file) { view ->
            // page geometry: the first page is on screen, a point inside it maps to page 0
            val rect = onMain { view.pageViewRect(0) }!!
            assertTrue(rect.width() > 100)
            assertEquals(0, onMain { view.pageAt(rect.centerX(), rect.top + 10) })

            val name = onMain { view.addShape(0, PDFView.SHAPE_RECT, RectF(100f, 700f, 200f, 600f), Color.RED, 2f) }!!
            assertTrue(onMain { view.canMoveAnnotation(name) })
            assertTrue(onMain { view.moveAnnotation(name, RectF(300f, 400f, 450f, 300f)) })
            assertEquals(RectF(300f, 400f, 450f, 300f), onMain { view.movableBounds(name) })
            assertTrue(onMain { view.undoEdit() })
            assertEquals("back where it was", RectF(100f, 700f, 200f, 600f), onMain { view.movableBounds(name) })
            assertTrue(onMain { view.redoEdit() })

            val picture = Bitmap.createBitmap(30, 10, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
            val image = onMain { view.addImage(0, RectF(50f, 200f, 110f, 180f), picture) }!!
            assertTrue(onMain { view.moveAnnotation(image, RectF(60f, 250f, 180f, 210f)) })

            val note = onMain { view.addNote(0, 400f, 800f, "Kiểm tra") }!!
            val info = onMain { view.getAnnotations(0).first { it.name == note } }
            assertEquals("Kiểm tra", onMain { view.getAnnotationText(info) })
            assertTrue(onMain { view.setAnnotationText(info, "Đã xem") })
            assertTrue(onMain { view.undoEdit() })
            assertEquals("Kiểm tra", onMain { view.getAnnotationText(info) })
            assertTrue(onMain { view.redoEdit() })

            // the sentence highlight and the paper colors draw without trouble
            onMain { view.setReadingHighlight(0, listOf(RectF(0.1f, 0.1f, 0.5f, 0.15f))); view.setSepiaMode(true); view.setNightMode(true); view.setSepiaMode(false) }
            instrumentation.waitForIdleSync()

            assertTrue(onMain { view.hasUnsavedChanges })
            assertTrue(view.saveDocument(saved))
        }
        val annots = annotations(saved).toMap()
        assertEquals(3, annots.size)
        val shape = annots.entries.first { it.key!!.startsWith("pdfview-shape-") }.value
        assertTrue("shape moved: $shape", shape.left > 290 && shape.left < 305 && shape.top > 395 && shape.top < 405)
        val image = annots.entries.first { it.key!!.startsWith("pdfview-image-") }.value
        assertEquals(60f, image.left, 0.5f)
        assertEquals(180f, image.right, 0.5f)
    }

    @Test
    fun documentWithoutFormHasNoFields() {
        withView(textPdf("view_noform.pdf", 1)) { view ->
            assertFalse(onMain { view.hasForm })
            assertTrue(onMain { view.getFormFields(0) }.isEmpty())
        }
    }

    @Test
    fun fillFormInTheViewer() {
        // the same form as PdfToolsTest, made with PdfBox
        val file = File(context.cacheDir, "view_form.pdf").also { it.delete() }
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context)
        com.tom_roush.pdfbox.pdmodel.PDDocument().use { pd ->
            val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4)
            pd.addPage(page)
            val form = com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm(pd)
            pd.documentCatalog.acroForm = form
            form.defaultResources = com.tom_roush.pdfbox.pdmodel.PDResources().apply {
                put(com.tom_roush.pdfbox.cos.COSName.getPDFName("Helv"), com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA)
            }
            form.defaultAppearance = "/Helv 12 Tf 0 g"
            val text = com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField(form).apply { partialName = "email"; defaultAppearance = "/Helv 12 Tf 0 g" }
            text.widgets[0].apply { rectangle = com.tom_roush.pdfbox.pdmodel.common.PDRectangle(50f, 700f, 300f, 24f); this.page = page; isPrinted = true }
            page.annotations.add(text.widgets[0])
            form.fields.add(text)
            pd.save(file)
        }
        val saved = File(context.cacheDir, "view_form_saved.pdf").also { it.delete() }
        withView(file) { view ->
            assertTrue(onMain { view.hasForm })
            val field = onMain { view.getFormFields(0) }.single()
            assertEquals(PdfFormField.TYPE_TEXT, field.type)
            assertEquals("email", field.name)
            // the field is found under its middle on screen
            val onScreen = onMain { view.pageRectToView(0, field.rect) }!!
            assertNotNull(onMain { view.findFormFieldAt(onScreen.centerX(), onScreen.centerY()) })
            assertTrue(onMain { view.setFormText(field, "a@b.vn") })
            assertEquals("a@b.vn", onMain { view.getFormFields(0) }.single().value)
            instrumentation.waitForIdleSync()
            assertTrue(view.saveDocument(saved))
        }
        com.tom_roush.pdfbox.pdmodel.PDDocument.load(saved).use { pd ->
            assertEquals("a@b.vn", pd.documentCatalog.acroForm.getField("email").valueAsString)
        }
    }
}
