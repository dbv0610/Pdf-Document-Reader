/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.reader.OfficeReader
import com.wxiwei.office.wp.control.Word
import com.wxiwei.office.wp.model.WPDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** A picture inserted live: shown at once, resized and moved without a save, read back as shown. */
@RunWith(AndroidJUnit4::class)
class DocxLivePictureTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun ok(s: LiveDocxSession, done: Boolean) = assertTrue(s.lastError?.toString(), done)

    /** A 200 x 100 PNG, red on the left half, blue on the right. */
    private fun image(): File = File(context.cacheDir, "live-picture.png").also { f ->
        val b = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) for (x in 0 until 200) b.setPixel(x, y, if (x < 100) Color.RED else Color.BLUE)
        f.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun doc(reader: OfficeReader) = (reader.control!!.getView() as Word).getDocument() as WPDocument

    /** The body text and, for each picture, where it is and its size in pixels. */
    private fun shown(reader: OfficeReader, s: LiveDocxSession): Pair<String, List<String>> = onMain {
        val d = doc(reader)
        val text = d.getText(0, d.getAreaEnd(0))
        text to text.indices.mapNotNull { i -> s.shapeAt(i.toLong())?.bounds?.let { "$i:${it.width}x${it.height}" } }
    }

    private suspend fun page(reader: OfficeReader): Bitmap {
        reader.thumbnails!!.invalidateAll()
        kotlinx.coroutines.delay(800)
        return reader.thumbnails!!.render(1, 1000)!!
    }

    /** Runs [edit] live on sample.docx without a reopen, saves; the saved file shows the same text, pictures and first page. */
    private fun liveThenSaved(name: String, edit: suspend (LiveDocxSession, OfficeReader) -> Unit): Pair<String, List<String>> {
        val source = OpenDocument.copySample("sample.docx", "docx_livepic_$name.docx")
        val saved = OpenDocument.output("docx_livepic_${name}_saved.docx")
        var live: Pair<String, List<String>>? = null
        var livePage: Bitmap? = null
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val s = onMain { LiveDocxSession(reader.control!!, source) }
            edit(s, reader)
            assertFalse("shown without reopening", s.needsReopen)
            live = shown(reader, s)
            livePage = page(reader)
            val result = onMain { s.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        var back: Pair<String, List<String>>? = null
        var backPage: Bitmap? = null
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            back = shown(reader, onMain { LiveDocxSession(reader.control!!, saved) })
            backPage = page(reader)
        }
        assertEquals("the saved file reads back as shown", live, back)
        val a = livePage!!; val b = backPage!!
        var diff = 0
        for (y in 0 until minOf(a.height, b.height)) for (x in 0 until minOf(a.width, b.width)) if (a.getPixel(x, y) != b.getPixel(x, y)) diff++
        assertTrue("first page pixels differing: $diff", a.width == b.width && a.height == b.height && diff < a.width * a.height / 1000)
        return back!!
    }

    /** Right after the first word of the first line under the title. */
    private fun place(reader: OfficeReader): Long = onMain {
        val d = doc(reader)
        d.getText(0, d.getAreaEnd(0)).indexOf("PianoLearn").toLong() + "PianoLearn".length
    }

    @Test fun insertAndResize() {
        val (text, pictures) = liveThenSaved("resize") { s, reader ->
            val at = place(reader)
            ok(s, onMain { s.insertImage(at, image(), 200, 100) })
            assertNotNull("drawn at once", onMain { s.shapeAt(at) })
            ok(s, onMain { s.resizeObject(at, 150, 75) })
        }
        val at = text.indexOf("PianoLearn") + "PianoLearn".length
        assertTrue(pictures.toString(), pictures.contains("$at:150x75"))
    }

    @Test fun insertThenMove() {
        val (text, pictures) = liveThenSaved("move") { s, reader ->
            val at = place(reader)
            ok(s, onMain { s.insertImage(at, image(), 120, 60) })
            // to the start of that paragraph
            val start = onMain { doc(reader).getParagraph(at)!!.getStartOffset() }
            ok(s, onMain { s.moveObject(at, start) })
        }
        val start = text.indexOf("PianoLearn")
        assertTrue(pictures.toString(), pictures.contains("${start - 1}:120x60"))
    }

    @Test fun undoRedoAndNotDeletedAsText() {
        liveThenSaved("undo") { s, reader ->
            val at = place(reader)
            ok(s, onMain { s.insertImage(at, image(), 80, 40) })
            ok(s, onMain { s.undo() })
            assertEquals(null, onMain { s.shapeAt(at) })
            ok(s, onMain { s.redo() })
            assertNotNull(onMain { s.shapeAt(at) })
            assertFalse("not deleted as text", onMain { s.deleteText(at, at + 1) })
        }
    }

    /** In text typed in this session the file has no place for it: shown after the file is read again. */
    @Test fun inTypedTextAfterReopen() {
        val source = OpenDocument.copySample("sample.docx", "docx_livepic_typed.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val s = onMain { LiveDocxSession(reader.control!!, source) }
            val at = place(reader)
            ok(s, onMain { s.insertText(at, "abc") })
            ok(s, onMain { s.insertImage(at + 1, image(), 80, 40) })
            assertTrue(s.needsReopen)
        }
    }
}
