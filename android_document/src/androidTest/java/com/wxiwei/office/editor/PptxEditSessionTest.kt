package com.wxiwei.office.editor

import android.graphics.Bitmap
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.editor.pptx.LivePptxSession
import com.wxiwei.office.editor.pptx.PptxEditor
import com.wxiwei.office.editor.pptx.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** sample.pptx slide 2: change the title text, move a picture, add and delete a text box, save, reopen. */
@RunWith(AndroidJUnit4::class)
class PptxEditSessionTest {
    private val out = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    private suspend fun shot(reader: com.wxiwei.office.reader.OfficeReader, name: String) {
        val bitmap = reader.thumbnails!!.render(2, 1280) ?: return
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
    }

    @Test
    fun editSaveReopen() {
        val source = OpenDocument.copySample("sample.pptx", "pptx_edit_source.pptx")
        val saved = OpenDocument.output("pptx_edit_saved.pptx")
        var titleId = -1
        var pictureId = -1
        var pictureRect: Rect? = null
        var addedId = -1
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            val shapes = onMain { session.listShapes(1) }
            shapes.forEach { Log.i("PptxEditTest", "shape ${it.id} ${it.kind} ${it.name} '${it.text.take(30)}' ${it.rectEmu}") }
            val title = shapes.first { it.text.contains("KHÁM PHÁ") }
            val picture = shapes.first { it.kind.name == "PICTURE" || it.kind.name == "GROUP" }
            titleId = title.id; pictureId = picture.id
            assertTrue(session.lastError?.toString(), onMain { session.setShapeText(1, title.id, "LỘ TRÌNH ĐÃ SỬA") })
            val r = picture.rectEmu
            pictureRect = Rect(r.x + 914400, r.y, r.width, r.height)
            assertTrue(session.lastError?.toString(), onMain { session.moveShape(1, picture.id, pictureRect!!) })
            val added = onMain { session.addTextBox(1, Rect(914400, 914400, 4572000, 914400), "Hộp mới", 32f, "C00000", true) }
            assertTrue(session.lastError?.toString(), added > 0)
            // rotate the new box, undo, redo: live model and file follow
            assertTrue(session.lastError?.toString(), onMain { session.rotateShape(1, added, 30f) })
            val live = { onMain { (reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation).getSlide(1)!!.getShapes().filter { it.shapeID == added }.map { it.rotation } } }
            assertEquals(listOf(30f), live().distinct())
            assertTrue(onMain { session.undo() }); assertEquals(listOf(0f), live().distinct())
            assertTrue(onMain { session.redo() }); assertEquals(listOf(30f), live().distinct())
            addedId = added
            val temp = onMain { session.addTextBox(1, Rect(0, 0, 914400, 914400), "tạm", 20f) }
            assertTrue(onMain { session.undo() }) // removes "tạm"
            // format the title: bold, red, 60pt, right aligned; undo and redo
            val fmt = com.wxiwei.office.editor.pptx.TextFormat(bold = true, rgbHex = "C00000", sizePt = 60f, align = "r")
            assertTrue(session.lastError?.toString(), onMain { session.setTextFormat(1, title.id, fmt) })
            assertTrue(onMain { session.undo() })
            assertTrue(onMain { session.redo() })
            Log.i("PptxEditTest", "needsReopen=${session.needsReopen} temp=$temp added=$added")
            shot(reader, "pptx_edit_live")
            val result = onMain { session.save(saved) }
            assertTrue(result.toString(), result is EditResult.Ok)
        }
        val editor = PptxEditor(saved)
        val after = editor.listShapes(1)
        assertEquals("LỘ TRÌNH ĐÃ SỬA", after.first { it.id == titleId }.text)
        assertEquals(pictureRect, after.first { it.id == pictureId }.rectEmu)
        assertTrue(after.any { it.text == "Hộp mới" })
        assertEquals(30f, after.first { it.id == addedId }.rotationDeg, 0.001f)
        assertTrue("undone box not saved", after.none { it.text == "tạm" })
        // the saved title runs carry the format
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide2.xml")).readBytes().toString(Charsets.UTF_8) }
        val titleXml = xml.substring(xml.indexOf("LỘ TRÌNH ĐÃ SỬA").let { xml.lastIndexOf("<p:sp>", it) }, xml.indexOf("LỘ TRÌNH ĐÃ SỬA"))
        assertTrue(titleXml, titleXml.contains("b=\"1\"") && titleXml.contains("sz=\"6000\"") && titleXml.contains("C00000") && titleXml.contains("algn=\"r\""))
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader -> shot(reader, "pptx_edit_reopened") }
    }
}
