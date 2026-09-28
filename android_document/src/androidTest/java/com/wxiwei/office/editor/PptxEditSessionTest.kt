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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** sample.pptx slide 2: change the title text, move a picture, add and delete a text box, save, reopen. */
@RunWith(AndroidJUnit4::class)
class PptxEditSessionTest {
    private val out = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "render").apply { mkdirs() }

    private suspend fun shot(reader: com.wxiwei.office.reader.OfficeReader, name: String, page: Int = 2) {
        val bitmap = reader.thumbnails!!.render(page, 1280) ?: return
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

    /** A text box added to a new presentation shows its text centered in the box. */
    @Test
    fun newTextBoxIsCentered() {
        val file = OpenDocument.output("pptx_new_centered.pptx")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(DocumentCreator.create(context, DocumentCreator.Format.POWERPOINT, file) is EditResult.Ok)
        OpenDocument.open(file, { it.pageCount >= 1 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, file) }
            val size = onMain { session.slideSizeEmu() }
            val box = Rect(size.width / 4, size.height * 2 / 5, size.width / 2, size.height / 5)
            assertTrue(onMain { session.addTextBox(0, box, "Text box", 32f) } > 0)
            kotlinx.coroutines.delay(500)
            val bitmap = reader.thumbnails!!.render(1, 1280)!!
            File(out, "pptx_new_centered.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
            // the dark pixels of the text, and their middle against the box's middle
            var minX = Int.MAX_VALUE; var maxX = -1; var minY = Int.MAX_VALUE; var maxY = -1
            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                val c = bitmap.getPixel(x, y)
                if (android.graphics.Color.red(c) < 100 && android.graphics.Color.green(c) < 100 && android.graphics.Color.blue(c) < 100) {
                    minX = minOf(minX, x); maxX = maxOf(maxX, x); minY = minOf(minY, y); maxY = maxOf(maxY, y)
                }
            }
            val scale = bitmap.width / size.width.toDouble()
            val boxCx = (box.x + box.width / 2) * scale; val boxCy = (box.y + box.height / 2) * scale
            Log.i("PptxEditTest", "text ink $minX..$maxX x $minY..$maxY, box centre ${boxCx.toInt()},${boxCy.toInt()}")
            assertTrue("text drawn", maxX > minX)
            assertEquals("horizontally centered", boxCx, (minX + maxX) / 2.0, bitmap.width * 0.02)
            assertEquals("vertically centered", boxCy, (minY + maxY) / 2.0, bitmap.height * 0.04)
        }
    }

    /** Bold + red on chars [4, 8) of a text box: only they change, live, undone, and in the file as their own run. */
    @Test
    fun formatPartOfText() {
        val source = OpenDocument.copySample("ppt2.pptx", "pptx_range_fmt.pptx")
        val saved = OpenDocument.output("pptx_range_fmt_saved.pptx")
        var part = ""
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            val text = onMain { session.listShapes(9) }.first { it.id == 8 }.text
            part = text.substring(4, 8)
            val boldAt = { i: Long ->
                onMain {
                    val box = (reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation).getSlide(9)!!.getShapes()
                        .filterIsInstance<com.wxiwei.office.common.shape.TextBox>().first { it.shapeID == 8 }
                    val para = box.element!!.getParaCollection()!!.getElementForIndex(0) as com.wxiwei.office.simpletext.model.ParagraphElement
                    val leaf = (0 until para.leafCount()).map { para.getElementForIndex(it)!! }.first { i >= it.getStartOffset() && i < it.getEndOffset() }
                    com.wxiwei.office.simpletext.model.AttrManage.instance().getFontBold(para.getAttribute(), leaf.getAttribute())
                }
            }
            assertTrue(session.lastError?.toString(), onMain { session.setTextFormat(9, 8, 4, 8, com.wxiwei.office.editor.pptx.TextFormat(bold = true, rgbHex = "C00000")) })
            assertEquals(listOf(false, true, true, false), listOf(3L, 4L, 7L, 8L).map(boldAt))
            assertTrue(onMain { session.undo() })
            assertEquals(false, boldAt(5))
            assertTrue(onMain { session.redo() })
            assertEquals(true, boldAt(5))
            shot(reader, "pptx_range_fmt", 10)
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide10.xml")).readBytes().toString(Charsets.UTF_8) }
        // the shape's runs that are now bold and red read exactly the formatted chars (Canva splits words over runs)
        val shape = xml.substring(Regex("<p:cNvPr[^>]*\\bid=\"8\"").find(xml)!!.range.first).substringBefore("</p:sp>")
        val formatted = Regex("<a:r><a:rPr([^>]*)>(.*?)</a:rPr><a:t>([^<]*)</a:t></a:r>").findAll(shape)
            .filter { it.groupValues[1].contains("b=\"1\"") && it.groupValues[2].contains("C00000") }.joinToString("") { it.groupValues[3] }
        assertEquals(part, formatted)
        assertEquals(PptxEditor(source).listShapes(9).first { it.id == 8 }.text, PptxEditor(saved).listShapes(9).first { it.id == 8 }.text)
    }

    /** A box that fits its text (spAutoFit) grows with more lines, shrinks back on undo, and is saved grown. */
    @Test
    fun autoFitBoxFollowsText() {
        val source = OpenDocument.copySample("ppt2.pptx", "pptx_autofit.pptx")
        val saved = OpenDocument.output("pptx_autofit_saved.pptx")
        var grown: Rect? = null
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            val before = onMain { session.listShapes(9) }.first { it.id == 8 }
            val lines = before.text + "\nDòng thêm một\nDòng thêm hai"
            assertTrue(session.lastError?.toString(), onMain { session.setShapeText(9, 8, lines) })
            grown = onMain { session.listShapes(9) }.first { it.id == 8 }.rectEmu
            Log.i("PptxEditTest", "autofit ${before.rectEmu} -> $grown")
            // at least three lines of 33.59pt (lnSpc spcPts 3359): the old text and two new lines
            assertTrue("taller: ${before.rectEmu.height} -> ${grown!!.height}", grown!!.height > before.rectEmu.height && grown!!.height >= 12700L * 99)
            assertEquals("top kept", before.rectEmu.y, grown!!.y)
            assertTrue(onMain { session.undo() })
            assertEquals(before.rectEmu, onMain { session.listShapes(9) }.first { it.id == 8 }.rectEmu)
            assertTrue(onMain { session.redo() })
            assertEquals(grown, onMain { session.listShapes(9) }.first { it.id == 8 }.rectEmu)
            assertTrue(!session.needsReopen)
            shot(reader, "pptx_autofit", 10)
            assertTrue(onMain { session.save(saved) } is EditResult.Ok)
        }
        assertEquals(grown, PptxEditor(saved).listShapes(9).first { it.id == 8 }.rectEmu)
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

    /** Offsets of a text box's laid-out text from its middle: lines (x) and the block (y), in pixels at zoom 1. */
    private fun centering(box: com.wxiwei.office.pg.model.PGSlide?, id: Int, reader: com.wxiwei.office.reader.OfficeReader): Pair<List<Int>, Int>? {
        val tb = box?.getShapes()?.filterIsInstance<com.wxiwei.office.common.shape.TextBox>()?.firstOrNull { it.shapeID == id } ?: return null
        val root = tb.rootView ?: return null
        val b = tb.bounds ?: return null
        val xs = ArrayList<Int>()
        var top = Int.MAX_VALUE; var bottom = Int.MIN_VALUE
        var para = root.getChildView()
        while (para != null) {
            var line = para.getChildView()
            while (line != null) {
                // the line's text: from its first leaf to the end of its last one
                var leaf = line.getChildView(); var l = Int.MAX_VALUE; var r = Int.MIN_VALUE
                while (leaf != null) { l = minOf(l, leaf.getX()); r = maxOf(r, leaf.getX() + leaf.getWidth()); leaf = leaf.getNextView() }
                if (r > l) xs.add(para.getX() + line.getX() + (l + r) / 2 - b.width / 2)
                // the root is moved down by the vertical alignment (anchor) of the box
                top = minOf(top, root.getY() + para.getY() + line.getY()); bottom = maxOf(bottom, root.getY() + para.getY() + line.getY() + line.getHeight())
                line = line.getNextView()
            }
            para = para.getNextView()
        }
        return xs to ((top + bottom) / 2 - b.height / 2)
    }

    /** Centered text stays centered when its size changes (horizontally, and vertically when anchored in the middle). */
    @Test
    fun sizeKeepsCentering() {
        val problems = ArrayList<String>()
        for (sample in listOf("sample.pptx", "ppt2.pptx")) {
            val source = OpenDocument.copySample(sample, "center_" + sample)
            OpenDocument.open(source, { it.pageCount >= 1 }) { reader ->
                val session = onMain { LivePptxSession(reader.control!!, source) }
                val p = reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation
                val slides = minOf(4, onMain { session.slideCount() })
                for (slide in 0 until slides) {
                    val shapes = onMain { session.listShapes(slide) }.filter { it.kind.name == "TEXT" && it.text.isNotBlank() }
                    for (shape in shapes) {
                        reader.thumbnails!!.render(slide + 1, 1280)
                        val before = onMain { centering(p.getSlide(slide), shape.id, reader) } ?: continue
                        val size = onMain { session.textStyle(slide, shape.id)?.sizePt } ?: continue
                        assertTrue(onMain { session.setTextFormat(slide, shape.id, com.wxiwei.office.editor.pptx.TextFormat(sizePt = size * 1.5f)) })
                        reader.thumbnails!!.invalidateAll()
                        reader.thumbnails!!.render(slide + 1, 1280)
                        val after = onMain { centering(p.getSlide(slide), shape.id, reader) } ?: continue
                        Log.i("CenterTest", "$sample s$slide #${shape.id} '${shape.text.take(20)}' ${size}pt x ${before.first} -> ${after.first}  y ${before.second} -> ${after.second}")
                        // lines that were centered (within 3 px) must stay centered; so must the block
                        val wasCentered = before.first.isNotEmpty() && before.first.all { Math.abs(it) <= 3 }
                        if (wasCentered && after.first.any { Math.abs(it) > 3 }) problems.add("$sample s$slide #${shape.id} x ${after.first}")
                        if (Math.abs(before.second) <= 3 && Math.abs(after.second) > 3) problems.add("$sample s$slide #${shape.id} y ${before.second} -> ${after.second}")
                        onMain { session.undo() }
                    }
                }
            }
        }
        assertEquals(emptyList<String>(), problems)
    }

    /** P2: a red rectangle, a blue ellipse, a yellow arrow and a line on slide 1: drawn at once, undone/redone, saved, read back. */
    @Test
    fun addShapes() {
        val source = OpenDocument.copySample("sample.pptx", "shapes_source.pptx")
        val saved = OpenDocument.output("shapes_saved.pptx")
        val specs = listOf(Triple("rect", "FF0000", Rect(914400, 914400, 1828800, 1371600)), Triple("ellipse", "0000FF", Rect(3657600, 914400, 1828800, 1371600)),
            Triple("rightArrow", "FFC000", Rect(914400, 3200400, 2743200, 914400)), Triple("line", "00B050", Rect(4572000, 3657600, 2743200, 0)))
        val ids = ArrayList<Int>()
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            for ((prst, color, r) in specs) {
                val id = onMain { session.addShape(0, r, prst, if (prst == "line") null else color, if (prst == "line") color else "404040", 2f) }
                assertTrue(session.lastError?.toString(), id > 0); ids.add(id)
            }
            val p = reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation
            val live = onMain { p.getSlide(0)!!.getShapes().filter { it.shapeID in ids }.map { it.javaClass.simpleName } }
            assertEquals(listOf("AutoShape", "AutoShape", "AutoShape", "LineShape"), live)
            // the fill color in the middle of each filled shape
            reader.thumbnails!!.invalidateAll()
            val bmp = reader.thumbnails!!.render(1, 1280)!!
            val size = onMain { session.slideSizeEmu() }
            for ((prst, color, r) in specs.take(3)) {
                val px = bmp.getPixel(((r.x + r.width / 2).toDouble() / size.width * bmp.width).toInt(), ((r.y + r.height / 2).toDouble() / size.height * bmp.height).toInt()) and 0xFFFFFF
                Log.i("PptxEditTest", "$prst middle %06X".format(px))
                assertEquals("$prst filled", color.toInt(16), px)
            }
            File(out, "shapes.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) }
            // undo the line, redo it
            assertTrue(onMain { session.undo() })
            assertEquals(3, onMain { p.getSlide(0)!!.getShapes().count { it.shapeID in ids } })
            assertTrue(onMain { session.redo() })
            assertTrue(onMain { session.save(saved) } is com.wxiwei.office.editor.EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide1.xml")).readBytes().toString(Charsets.UTF_8) }
        for ((prst, _, _) in specs) assertTrue("$prst saved", xml.contains("prst=\"$prst\""))
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader ->
            val p = reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation
            val types = onMain { p.getSlide(0)!!.getShapes().filter { it.shapeID in ids }.map { it.javaClass.simpleName } }
            assertEquals("read back", listOf("AutoShape", "AutoShape", "AutoShape", "LineShape"), types)
        }
    }

    /** Z-order: red under blue at the same place; red to the front shows red, undo shows blue, saved order, read back. */
    @Test
    fun zOrder() {
        val source = OpenDocument.copySample("sample.pptx", "zorder_source.pptx")
        val saved = OpenDocument.output("zorder_saved.pptx")
        val r = Rect(914400, 914400, 1828800, 1371600)
        var red = 0; var blue = 0
        suspend fun middle(reader: com.wxiwei.office.reader.OfficeReader, session: LivePptxSession): Int {
            reader.thumbnails!!.invalidateAll()
            val bmp = reader.thumbnails!!.render(1, 1280)!!
            val size = onMain { session.slideSizeEmu() }
            return bmp.getPixel(((r.x + r.width / 2).toDouble() / size.width * bmp.width).toInt(), ((r.y + r.height / 2).toDouble() / size.height * bmp.height).toInt()) and 0xFFFFFF
        }
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            red = onMain { session.addShape(0, r, "rect", "FF0000", "FF0000", 1f) }
            blue = onMain { session.addShape(0, r, "rect", "0000FF", "0000FF", 1f) }
            assertEquals("blue on top", 0x0000FF, middle(reader, session))
            assertFalse("blue is already in front", onMain { session.reorderShape(0, blue, "front") })
            assertTrue(session.lastError?.toString(), onMain { session.reorderShape(0, red, "front") })
            assertEquals("red to the front", 0xFF0000, middle(reader, session))
            assertTrue(onMain { session.undo() })
            assertEquals("undone", 0x0000FF, middle(reader, session))
            assertTrue(onMain { session.redo() })
            assertTrue(onMain { session.reorderShape(0, blue, "forward") })
            assertEquals("blue one layer up", 0x0000FF, middle(reader, session))
            assertTrue(onMain { session.reorderShape(0, blue, "back") })
            assertEquals("blue to the back", 0xFF0000, middle(reader, session))
            assertTrue(onMain { session.save(saved) } is com.wxiwei.office.editor.EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide1.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("blue first in the tree", xml.indexOf("<p:cNvPr id=\"$blue\"") in 0 until xml.indexOf("<p:cNvPr id=\"$red\""))
        assertTrue("blue before every other shape", Regex("<p:cNvPr id=\"(\\d+)\"").findAll(xml).map { it.groupValues[1].toInt() }.filter { it != 1 }.first() == blue)
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, saved) }
            assertEquals("read back: red on top", 0xFF0000, middle(reader, session))
        }
    }

    /** Animations and a transition written, undone, redone, saved, read back the same; the engine still opens the file. */
    @Test
    fun animationsAndTransition() {
        val source = OpenDocument.copySample("sample.pptx", "anim_source.pptx")
        val saved = OpenDocument.output("anim_saved.pptx")
        var effects = emptyList<com.wxiwei.office.editor.pptx.SlideEffect>()
        val push = com.wxiwei.office.editor.pptx.SlideTransition("push", "l", 1000, 5000)
        OpenDocument.open(source, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, source) }
            val ids = onMain { session.listShapes(0).map { it.id } }
            assertTrue("two shapes on slide 1: $ids", ids.size >= 2)
            val (a, b) = ids[0] to ids[1]
            effects = listOf(
                com.wxiwei.office.editor.pptx.SlideEffect(a, com.wxiwei.office.editor.pptx.SlideEffect.Kind.ENTRANCE, com.wxiwei.office.editor.pptx.SlideEffect.Effect.FADE, durationMs = 700),
                com.wxiwei.office.editor.pptx.SlideEffect(b, com.wxiwei.office.editor.pptx.SlideEffect.Kind.ENTRANCE, com.wxiwei.office.editor.pptx.SlideEffect.Effect.FLY, com.wxiwei.office.editor.pptx.SlideEffect.Direction.LEFT, com.wxiwei.office.editor.pptx.SlideEffect.Start.WITH, 500),
                com.wxiwei.office.editor.pptx.SlideEffect(a, com.wxiwei.office.editor.pptx.SlideEffect.Kind.EMPHASIS, com.wxiwei.office.editor.pptx.SlideEffect.Effect.PULSE, start = com.wxiwei.office.editor.pptx.SlideEffect.Start.AFTER, durationMs = 600),
                com.wxiwei.office.editor.pptx.SlideEffect(b, com.wxiwei.office.editor.pptx.SlideEffect.Kind.EXIT, com.wxiwei.office.editor.pptx.SlideEffect.Effect.ZOOM, durationMs = 500),
            )
            assertTrue(session.lastError?.toString(), onMain { session.setSlideEffects(0, effects) })
            assertEquals(effects, onMain { session.slideEffects(0) })
            assertTrue(onMain { session.setSlideTransition(listOf(0, 1), push) })
            assertEquals(push, onMain { session.slideTransition(1) })
            assertTrue(onMain { session.undo() })
            assertEquals("transition undone", null, onMain { session.slideTransition(1) })
            assertTrue(onMain { session.undo() })
            assertEquals("effects undone", emptyList<com.wxiwei.office.editor.pptx.SlideEffect>(), onMain { session.slideEffects(0) })
            assertTrue(onMain { session.redo() }); assertTrue(onMain { session.redo() })
            // writing what was read changes nothing
            val xml1 = onMain { session.readPackage("") { it.xml("ppt/slides/slide1.xml").asXML()!! } }
            assertTrue(onMain { session.setSlideEffects(0, session.slideEffects(0)) })
            assertEquals("idempotent", xml1, onMain { session.readPackage("") { it.xml("ppt/slides/slide1.xml").asXML()!! } })
            assertTrue(onMain { session.save(saved) } is com.wxiwei.office.editor.EditResult.Ok)
        }
        val xml = java.util.zip.ZipFile(saved).use { z -> z.getInputStream(z.getEntry("ppt/slides/slide1.xml")).readBytes().toString(Charsets.UTF_8) }
        assertTrue("transition before timing", xml.indexOf("<p:transition") in 0 until xml.indexOf("<p:timing"))
        assertTrue("unique cTn ids", Regex("<p:cTn id=\"(\\d+)\"").findAll(xml).map { it.groupValues[1] }.toList().let { it.size == it.toSet().size && it.size > 8 })
        OpenDocument.open(saved, { it.pageCount >= 10 }) { reader ->
            val session = onMain { LivePptxSession(reader.control!!, saved) }
            assertEquals("read back", effects, onMain { session.slideEffects(0) })
            assertEquals(push, onMain { session.slideTransition(0) })
            val p = reader.control!!.getView() as com.wxiwei.office.pg.control.Presentation
            assertTrue("the engine reads the animations", onMain { p.getSlide(0)!!.getSlideShowAnimation()?.isNotEmpty() == true })
        }
    }
}
