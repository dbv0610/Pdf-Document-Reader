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
            assertEquals(true, onMain { session.textFormatOf(1, title.id)?.bold })
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
        // the kept runs split the new title over several a:t, so find the shape by its id
        val idAt = Regex("<p:cNvPr[^>]*\\bid=\"$titleId\"").find(xml)!!.range.first
        val titleXml = xml.substring(xml.lastIndexOf("<p:sp>", idAt), xml.indexOf("</p:sp>", idAt))
        assertTrue(titleXml, titleXml.contains("b=\"1\"") && titleXml.contains("sz=\"6000\"") && titleXml.contains("C00000") && titleXml.contains("algn=\"r\""))
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader -> shot(reader, "pptx_edit_reopened") }
    }

    /** Copy of [sample] whose slide [slide] has the run with text [runText] made bold. */
    private fun withBoldRun(sample: String, slide: String, runText: String, name: String): File {
        val source = OpenDocument.copySample(sample, "$name.src.pptx")
        val out = OpenDocument.output("$name.pptx")
        java.util.zip.ZipFile(source).use { zip ->
            java.util.zip.ZipOutputStream(out.outputStream()).use { zos ->
                for (entry in zip.entries()) {
                    var bytes = zip.getInputStream(entry).readBytes()
                    if (entry.name == slide) {
                        val xml = bytes.toString(Charsets.UTF_8)
                        val at = xml.lastIndexOf("<a:rPr ", xml.indexOf("<a:t>$runText</a:t>")) + "<a:rPr ".length
                        bytes = (xml.substring(0, at) + "b=\"1\" " + xml.substring(at)).toByteArray()
                    }
                    zos.putNextEntry(java.util.zip.ZipEntry(entry.name))
                    zos.write(bytes)
                    zos.closeEntry()
                }
            }
        }
        return out
    }

    /** Editing part of a shape's text keeps the format of the other runs, live, after undo and in the file. */
    @Test
    fun setTextKeepsRunFormats() {
        val source = withBoldRun("ppt2.pptx", "ppt/slides/slide10.xml", "in ch", "pptx_runs")
        val saved = OpenDocument.output("pptx_runs_saved.pptx")
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            val before = onMain { session.listShapes(9) }.first { it.id == 8 }.text
            assertTrue(before, before.startsWith("Xin ch"))
            // the leaves as shown: text and bold
            val leaves = {
                onMain {
                    val box = (reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation).getSlide(9)!!.getShapes()
                        .filterIsInstance<com.wxiwei.office.common.shape.TextBox>().first { it.shapeID == 8 }
                    val para = box.element!!.getParaCollection()!!.getElementForIndex(0) as com.wxiwei.office.simpletext.model.ParagraphElement
                    (0 until para.leafCount()).map { j ->
                        val leaf = para.getElementForIndex(j)!!
                        leaf.getText(null)!!.trimEnd('\n') to com.wxiwei.office.simpletext.model.AttrManage.instance().getFontBold(para.getAttribute(), leaf.getAttribute())
                    }
                }
            }
            val original = leaves()
            assertTrue(original.toString(), original.contains("in ch" to true))
            assertTrue(onMain { session.setShapeText(9, 8, "Y" + before.substring(1)) })
            val edited = leaves()
            assertEquals(edited.toString(), "Y" to false, edited[0])
            assertTrue(edited.toString(), edited.contains("in ch" to true))
            assertEquals(original.size, edited.size)
            assertTrue(onMain { session.undo() })
            assertEquals(original, leaves())
            assertTrue(onMain { session.redo() })
            assertEquals(edited, leaves())
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide10.xml")).readBytes().toString(Charsets.UTF_8) }
        val bold = xml.lastIndexOf("<a:rPr ", xml.indexOf("<a:t>in ch</a:t>"))
        assertTrue("bold run kept", bold >= 0 && xml.substring(bold, xml.indexOf("<a:t>in ch</a:t>")).contains("b=\"1\""))
        assertTrue("first run changed", xml.contains("<a:t>Y</a:t>"))
    }

    /** id, kind, bounds, rotation and text of every shape of a slide as the view has it. */
    private fun snapshot(reader: com.wxiwei.office.reader.OfficeReader, slide: Int): List<String> = onMain {
        val p = reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation
        val out = ArrayList<String>()
        fun visit(shapes: Array<com.wxiwei.office.common.shape.IShape>) {
            for (sh in shapes) {
                val b = sh.bounds
                val text = (sh as? com.wxiwei.office.common.shape.TextBox)?.element?.getText(null)?.trim() ?: ""
                if (sh.shapeID > 0) out.add("${sh.shapeID}:${sh.javaClass.simpleName}:${b?.x},${b?.y},${b?.width},${b?.height}:r${sh.rotation}:$text")
                if (sh is com.wxiwei.office.common.shape.GroupShape) visit(sh.getShapes())
            }
        }
        visit(p.getSlide(slide)!!.getShapes())
        out.sorted()
    }

    /** Random text, moves, resizes, rotations, formats, new and deleted boxes, undo/redo; the saved deck reopens as shown. */
    @Test
    fun randomEditsSaveAsShown() {
        for (seed in listOf(1L, 2L, 3L)) {
            val source = OpenDocument.copySample("ppt2.pptx", "pptx_fuzz_$seed.pptx")
            val saved = OpenDocument.output("pptx_fuzz_saved_$seed.pptx")
            var shown = emptyList<String>()
            val log = StringBuilder()
            val slide = seed.toInt() % 3
            OpenDocument.open(source, { it.pageCount >= 3 }) { reader ->
                val session = onMain { LivePptxSession(reader.control!!, source) }
                val rnd = java.util.Random(seed)
                repeat(50) { step ->
                    val shapes = onMain { session.listShapes(slide) }.filter { it.kind.name != "GROUP" }
                    val target = if (shapes.isEmpty()) null else shapes[rnd.nextInt(shapes.size)]
                    val kind = rnd.nextInt(9)
                    val wasReopen = session.needsReopen
                    val ok = onMain {
                        when {
                            kind == 0 && target != null && target.kind.name == "TEXT" -> session.setShapeText(slide, target.id, "chữ $step")
                            kind == 1 && target != null -> target.rectEmu.let { r -> session.moveShape(slide, target.id, Rect(r.x + 91440L * (rnd.nextInt(5) - 2), r.y + 91440L * (rnd.nextInt(5) - 2), r.width, r.height)) }
                            kind == 2 && target != null -> target.rectEmu.let { r -> session.moveShape(slide, target.id, Rect(r.x, r.y, r.width * 3 / 4 + 1, r.height + 45720)) }
                            kind == 3 && target != null -> session.rotateShape(slide, target.id, 15f * rnd.nextInt(24))
                            kind == 4 && target != null && target.kind.name == "TEXT" -> session.setTextFormat(slide, target.id, com.wxiwei.office.editor.pptx.TextFormat(bold = rnd.nextBoolean(), sizePt = 12f + rnd.nextInt(30)))
                            kind == 5 -> session.addTextBox(slide, Rect(914400L * rnd.nextInt(8), 914400L * rnd.nextInt(4), 1828800, 457200), "hộp $step", 18f) > 0
                            kind == 6 && target != null && rnd.nextInt(3) == 0 -> session.deleteShape(slide, target.id)
                            kind == 7 -> session.undo()
                            kind == 8 -> session.redo()
                            else -> false
                        }
                    }
                    log.append("$step:$kind:${target?.id}(${target?.kind}):$ok${if (!wasReopen && session.needsReopen) " REOPEN" else ""} ")
                }
                assertTrue("all edits shown live: $log", !session.needsReopen)
                shown = snapshot(reader, slide)
                val result = onMain { session.save(saved) }
                assertTrue(result.toString(), result is EditResult.Ok)
            }
            OpenDocument.open(saved, { it.pageCount >= 3 }) { reader ->
                val reread = snapshot(reader, slide)
                // the view rounds EMU to pixels; allow one pixel
                fun norm(s: String) = s.split(":").let { f -> f[0] + ":" + f[1] + ":" + f.drop(3).joinToString(":") }
                fun near(a: String, b: String): Boolean {
                    val x = a.split(":")[2].split(",").mapNotNull { it.toIntOrNull() }
                    val y = b.split(":")[2].split(",").mapNotNull { it.toIntOrNull() }
                    return x.size == y.size && x.zip(y).all { (u, v) -> Math.abs(u - v) <= 1 }
                }
                val ok = shown.size == reread.size && shown.zip(reread).all { (a, b) -> norm(a) == norm(b) && near(a, b) }
                if (!ok) {
                    val diff = (shown - reread.toSet()).take(5) to (reread - shown.toSet()).take(5)
                    throw AssertionError("seed $seed slide $slide: shown-only=${diff.first} saved-only=${diff.second} ops=$log")
                }
            }
        }
    }
}

