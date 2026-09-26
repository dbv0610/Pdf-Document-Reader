package com.wxiwei.office.editor.pptx

import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.ooxml.A
import com.wxiwei.office.editor.ooxml.P
import com.wxiwei.office.editor.ooxml.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LivePptxSessionTest {
    @get:Rule val temp = TemporaryFolder()

    /** Stands in for the open slide: keeps text/rect per shape id. */
    private class FakeDisplay : LiveSlideDisplay {
        val text = HashMap<Int, String>(); val rect = HashMap<Int, Rect>()
        var liveOk = true
        override fun addTextBox(slideIndex: Int, id: Int, rectEmu: Rect, text: String, sizePt: Float, rgbHex: String, bold: Boolean): Boolean { this.text[id] = text; rect[id] = rectEmu; return liveOk }
        override fun addImage(slideIndex: Int, id: Int, rectEmu: Rect, imageFile: File): Boolean { rect[id] = rectEmu; return liveOk }
        override fun shapeText(slideIndex: Int, id: Int) = text[id]
        override fun setShapeText(slideIndex: Int, id: Int, text: String): Boolean { this.text[id] = text; return liveOk }
        override fun shapeRect(slideIndex: Int, id: Int) = rect[id]
        override fun moveShape(slideIndex: Int, id: Int, rectEmu: Rect): Boolean { rect[id] = rectEmu; return liveOk }
        override fun removeShape(slideIndex: Int, id: Int): Any? = if (rect.containsKey(id)) Triple(id, text.remove(id), rect.remove(id)) else null
        @Suppress("UNCHECKED_CAST")
        override fun restoreShape(slideIndex: Int, token: Any): Boolean {
            val (id, t, r) = token as Triple<Int, String?, Rect?>
            t?.let { text[id] = it }; r?.let { rect[id] = it }; return true
        }
    }

    private fun fixture(): File {
        val slide = """<p:sld xmlns:p="${P.uri}" xmlns:a="${A.uri}" xmlns:r="${R.uri}"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/></p:nvGrpSpPr><p:grpSpPr/>""" +
            """<p:sp><p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x="100" y="200"/><a:ext cx="3000" cy="4000"/></a:xfrm></p:spPr><p:txBody><a:bodyPr/><a:p><a:r><a:rPr sz="2400"/><a:t>Old</a:t></a:r></a:p></p:txBody></p:sp>""" +
            """</p:spTree></p:cSld></p:sld>"""
        val rels = { items: String -> """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">$items</Relationships>""" }
        val entries = linkedMapOf(
            "[Content_Types].xml" to """<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="xml" ContentType="application/xml"/></Types>""",
            "ppt/presentation.xml" to """<p:presentation xmlns:p="${P.uri}" xmlns:r="${R.uri}"><p:sldIdLst><p:sldId id="256" r:id="s1"/></p:sldIdLst><p:sldSz cx="9144000" cy="6858000"/></p:presentation>""",
            "ppt/_rels/presentation.xml.rels" to rels("""<Relationship Id="s1" Type="${R.uri}/slide" Target="slides/slide1.xml"/>"""),
            "ppt/slides/slide1.xml" to slide)
        val f = temp.newFile("live-${System.nanoTime()}.pptx")
        ZipOutputStream(f.outputStream()).use { z -> entries.forEach { (n, x) -> z.putNextEntry(ZipEntry(n)); z.write(x.toByteArray()); z.closeEntry() } }
        return f
    }

    private fun session(display: FakeDisplay = FakeDisplay()) = LivePptxSession(PptxEditor(fixture()), display) to display
    private fun saved(s: LivePptxSession): List<PptxShapeInfo> {
        val out = File(temp.root, "out-${System.nanoTime()}.pptx")
        assertTrue(s.save(out) is EditResult.Ok)
        return PptxEditor(out).listShapes(0)
    }

    @Test fun editsShowLiveAndPersistWithoutReopen() {
        val (s, d) = session()
        d.text[2] = "Old"; d.rect[2] = Rect(100, 200, 3000, 4000)
        val id = s.addTextBox(0, Rect(10, 20, 300, 400), "Xin chào")
        assertEquals(3, id); assertEquals("Xin chào", d.text[id])
        assertTrue(s.setShapeText(0, 2, "New")); assertEquals("New", d.text[2])
        assertTrue(s.moveShape(0, 2, Rect(500, 600, 3000, 4000))); assertEquals(Rect(500, 600, 3000, 4000), d.rect[2])
        assertFalse(s.needsReopen)
        val shapes = saved(s)
        assertEquals("New", shapes.first { it.id == 2 }.text)
        assertEquals(Rect(500, 600, 3000, 4000), shapes.first { it.id == 2 }.rectEmu)
        assertEquals("Xin chào", shapes.first { it.id == 3 }.text)
    }

    @Test fun undoRedoKeepBothLayersInStep() {
        val (s, d) = session()
        d.text[2] = "Old"; d.rect[2] = Rect(100, 200, 3000, 4000)
        s.setShapeText(0, 2, "A"); s.moveShape(0, 2, Rect(1, 2, 3, 4)); s.deleteShape(0, 2)
        assertNull(d.rect[2]); assertTrue(saved(s).none { it.id == 2 })
        assertTrue(s.undo()); assertEquals(Rect(1, 2, 3, 4), d.rect[2]); assertEquals(Rect(1, 2, 3, 4), saved(s).first { it.id == 2 }.rectEmu)
        assertTrue(s.undo()); assertEquals(Rect(100, 200, 3000, 4000), d.rect[2]); assertEquals(Rect(100, 200, 3000, 4000), saved(s).first { it.id == 2 }.rectEmu)
        assertTrue(s.undo()); assertEquals("Old", d.text[2]); assertEquals("Old", saved(s).first { it.id == 2 }.text)
        assertFalse(s.undo())
        assertTrue(s.redo()); assertTrue(s.redo()); assertEquals("A", saved(s).first { it.id == 2 }.text); assertEquals(Rect(1, 2, 3, 4), d.rect[2])
        // A new edit drops the redo branch
        s.addTextBox(0, Rect(0, 0, 10, 10), "x"); assertFalse(s.canRedo())
        // Undo of an addition removes it; redo brings it back with the same id
        assertTrue(s.undo()); assertTrue(saved(s).none { it.id == 3 }); assertNull(d.text[3])
        assertTrue(s.redo()); assertEquals("x", saved(s).first { it.id == 3 }.text); assertEquals("x", d.text[3])
    }

    @Test fun editThatCannotShowLiveIsStillSavedAndFlagged() {
        val (s, d) = session(FakeDisplay().apply { liveOk = false })
        assertTrue(s.setShapeText(0, 2, "Saved anyway"))
        assertTrue(s.needsReopen)
        assertEquals("Saved anyway", saved(s).first { it.id == 2 }.text)
        assertEquals(-1, s.addTextBox(5, Rect(0, 0, 1, 1), "bad slide")); assertTrue(s.lastError != null)
    }
}
