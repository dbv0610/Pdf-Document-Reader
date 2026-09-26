package com.wxiwei.office.editor

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.docx.DocxSourceMap
import com.wxiwei.office.editor.docx.LiveDocxSession
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.wp.control.Word
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Word formatting shows at once (model + relayout), undoes, and is saved. */
@RunWith(AndroidJUnit4::class)
class LiveDocxSessionTest {
    private val out = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    private fun offsetOf(path: String, needle: String): Long {
        val map = DocxSourceMap.get(path)!!
        for (i in 0 until map.size) {
            val l = map.leaf(i)
            val k = l.text.indexOf(needle)
            if (k >= 0) return l.start + k
        }
        return -1
    }

    private fun bold(reader: com.wxiwei.office.reader.OfficeReader, offset: Long): Boolean = onMain {
        val doc = (reader.control!!.getView() as Word).getDocument()
        val para = doc.getParagraph(offset)!!
        val leaf = doc.getLeaf(offset)!!
        AttrManage.instance().getFontBold(para.getAttribute(), leaf.getAttribute())
    }

    private suspend fun shot(reader: com.wxiwei.office.reader.OfficeReader, name: String) {
        reader.thumbnails?.invalidateAll()
        delay(500)
        val bitmap = reader.thumbnails!!.render(2, 1240) ?: return
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    @Test
    fun formatLiveUndoSave() {
        val source = OpenDocument.copySample("sample.docx", "live_docx.docx")
        val saved = OpenDocument.output("live_docx_saved.docx")
        OpenDocument.open(source, { it.layout != null }) { reader ->
            val start = offsetOf(source.absolutePath, "MidiConverter parse")
            assertTrue(start >= 0)
            val end = start + "MidiConverter parse".length
            assertFalse(bold(reader, start + 2))
            val session = onMain { LiveDocxSession(reader.control!!, source) }
            assertTrue(session.lastError?.toString(), onMain { session.setBold(start, end, true) })
            assertTrue(onMain { session.setTextColor(start, end, "C00000") })
            assertTrue(onMain { session.setFontSize(start, end, 16f) })
            delay(1500)
            assertTrue("bold in the model at once", bold(reader, start + 2))
            assertFalse("text after the range untouched", bold(reader, end + 3))
            assertTrue("pages laid out again", reader.state.value.pageCount > 0 || onMain { (reader.control!!.getView() as Word).getPageCount() } > 0)
            shot(reader, "docx_live_format")
            assertTrue(onMain { session.undo() }) // size
            assertTrue(onMain { session.undo() }) // color
            assertTrue(onMain { session.undo() }) // bold
            assertFalse("undo", bold(reader, start + 2))
            assertTrue(onMain { session.redo() })
            assertTrue("redo", bold(reader, start + 2))
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        OpenDocument.open(saved, { it.layout != null }) { reader ->
            val start = offsetOf(saved.absolutePath, "MidiConverter parse")
            assertTrue("saved bold", bold(reader, start + 2))
            assertEquals(false, bold(reader, start + "MidiConverter parse".length + 3))
        }
    }
}
