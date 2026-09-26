package com.wxiwei.office.editor.pptx

import com.wxiwei.office.editor.*
import com.wxiwei.office.editor.ooxml.*
import com.wxiwei.office.editor.slide.SlideGeometry
import com.wxiwei.office.fc.dom4j.Element
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PptxEditorTest {
    @get:Rule val temp = TemporaryFolder()
    private fun fixture(): File {
        val entries = linkedMapOf(
            "[Content_Types].xml" to """<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="xml" ContentType="application/xml"/></Types>""",
            "ppt/presentation.xml" to """<p:presentation xmlns:p="${P.uri}" xmlns:r="${R.uri}"><p:sldIdLst><p:sldId id="256" r:id="second"/><p:sldId id="257" r:id="first"/></p:sldIdLst><p:sldSz cx="9144000" cy="6858000"/></p:presentation>""",
            "ppt/_rels/presentation.xml.rels" to rels("second" to ("slide" to "slides/slide2.xml"), "first" to ("slide" to "slides/slide1.xml")),
            "ppt/slides/slide2.xml" to slide("""<p:sp><p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr/><p:nvPr><p:ph type="title" idx="0"/></p:nvPr></p:nvSpPr><p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:pPr algn="ctr"/><a:r><a:rPr b="1" sz="2400"/><a:t>Old</a:t></a:r></a:p></p:txBody></p:sp><p:grpSp><p:nvGrpSpPr><p:cNvPr id="3" name="Group"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="1000" y="2000"/><a:ext cx="4000" cy="6000"/><a:chOff x="100" y="200"/><a:chExt cx="2000" cy="2000"/></a:xfrm></p:grpSpPr><p:sp><p:nvSpPr><p:cNvPr id="9" name="Child"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x="200" y="300"/><a:ext cx="400" cy="500"/></a:xfrm></p:spPr></p:sp></p:grpSp>""", " show=\"0\""),
            "ppt/slides/slide1.xml" to slide(""),
            "ppt/slides/_rels/slide2.xml.rels" to rels("layout" to ("slideLayout" to "../slideLayouts/slideLayout1.xml")),
            "ppt/slideLayouts/slideLayout1.xml" to slide("""<p:sp><p:nvSpPr><p:cNvPr id="2" name="Layout title"/><p:cNvSpPr/><p:nvPr><p:ph type="title"/></p:nvPr></p:nvSpPr><p:spPr><a:xfrm><a:off x="100" y="200"/><a:ext cx="3000" cy="4000"/></a:xfrm></p:spPr></p:sp>""")
        )
        val f = temp.newFile("source-${System.nanoTime()}.pptx")
        ZipOutputStream(f.outputStream()).use { z -> entries.forEach { (name, xml) -> z.putNextEntry(ZipEntry(name)); z.write(xml.toByteArray()); z.closeEntry() } }
        return f
    }
    private fun slide(body: String, attrs: String = "") = """<p:sld xmlns:p="${P.uri}" xmlns:a="${A.uri}" xmlns:r="${R.uri}"$attrs><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/></p:nvGrpSpPr><p:grpSpPr/>$body</p:spTree></p:cSld></p:sld>"""
    private fun rels(vararg items: Pair<String, Pair<String, String>>) = """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">${items.joinToString("") { (id, value) -> """<Relationship Id="$id" Type="${R.uri}/${value.first}" Target="${value.second}"/>""" }}</Relationships>"""
    private fun output() = File(temp.root, "out-${System.nanoTime()}.pptx")
    private fun save(e: PptxEditor): OoxmlPackage { val out = output(); val result = e.save(out); assertTrue(result.toString(), result is EditResult.Ok); return OoxmlPackage.open(out) }
    private fun all(e: Element): List<Element> = listOf(e) + e.elements().filterIsInstance<Element>().flatMap { all(it) }
    private fun root(pkg: OoxmlPackage) = pkg.xml("ppt/slides/slide2.xml").rootElement
    @Test fun orderHiddenSlidesPlaceholderAndGroupBounds() {
        val e = PptxEditor(fixture())
        assertEquals(Size(9144000, 6858000), e.slideSizeEmu())
        val shapes = e.listShapes(0)
        assertEquals(listOf(2, 3, 9), shapes.map { it.id })
        assertEquals(Rect(100, 200, 3000, 4000), shapes[0].rectEmu); assertTrue(shapes[0].isPlaceholder)
        assertEquals(Rect(1200, 2300, 800, 1500), shapes[2].rectEmu)
        assertTrue(e.listShapes(1).isEmpty())
        assertEquals(9, SlideGeometry.hitTest(shapes, Point(1300, 2400))!!.id)
    }
    @Test fun textBoxesReserveIdsAndPreserveLinesStyleAndWhitespace() {
        val e = PptxEditor(fixture())
        assertEquals(10, e.addTextBox(0, Rect(1, 2, 300, 400), " Xin chào\n\nTail ", 24f, "0000FF", true))
        assertEquals(11, e.addTextBox(0, Rect(5, 6, 700, 800), "next"))
        val pkg = save(e); val shapes = root(pkg).firstChild(P, "cSld")!!.firstChild(P, "spTree")!!.childrenNamed(P, "sp")
        val box = shapes[1]; val body = box.firstChild(P, "txBody")!!
        assertEquals(3, body.childrenNamed(A, "p").size)
        assertEquals(listOf(" Xin chào", "", "Tail "), all(body).filter { it.name == "t" }.map { it.text })
        val rp = all(body).first { it.name == "rPr" }; assertEquals("2400", rp.attributeValue("sz")); assertEquals("1", rp.attributeValue("b"))
        assertEquals("0000FF", all(rp).first { it.name == "srgbClr" }.attributeValue("val"))
        assertEquals("TextBox 11", shapes.last().firstChild(P, "nvSpPr")!!.firstChild(P, "cNvPr")!!.attributeValue("name"))
        assertEquals(pkg.bytes("ppt/slides/slide1.xml")!!.toList(), OoxmlPackage.open(fixture()).bytes("ppt/slides/slide1.xml")!!.toList())
    }
    @Test fun imageRelationshipContentTypeAndDeletion() {
        val e = PptxEditor(fixture()); val png = temp.newFile("test.png").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val id = e.addImage(0, Rect(10, 20, 300, 200), png); assertEquals(10, id)
        val pkg = save(e)
        assertArrayEquals(png.readBytes(), pkg.bytes("ppt/media/image1.png"))
        assertTrue(pkg.relationships("ppt/slides/slide2.xml").any { it.type == REL_IMAGE && it.target == "../media/image1.png" })
        assertTrue(all(pkg.xml("[Content_Types].xml").rootElement).any { it.attributeValue("ContentType") == "image/png" })
        assertTrue(e.deleteShape(0, id)); val deleted = save(e)
        assertFalse(deleted.relationships("ppt/slides/slide2.xml").any { it.type == REL_IMAGE }); assertTrue(deleted.has("ppt/media/image1.png"))
    }
    @Test fun textStyleAndMovesSurviveRoundTrip() {
        val e = PptxEditor(fixture()); assertTrue(e.setShapeText(0, 2, "ĐÃ SỬA\nAgain"))
        assertTrue(e.moveShape(0, 2, Rect(50, 60, 700, 800)))
        assertTrue(e.moveShape(0, 9, Rect(1400, 2600, 1000, 1800)))
        assertEquals(Rect(1400, 2600, 1000, 1800), e.listShapes(0).last().rectEmu)
        val pkg = save(e); val body = all(root(pkg)).first { it.name == "txBody" }
        assertEquals(listOf("ctr", "ctr"), all(body).filter { it.name == "pPr" }.map { it.attributeValue("algn") })
        assertEquals(listOf("1", "1"), all(body).filter { it.name == "rPr" }.map { it.attributeValue("b") })
        assertTrue(e.deleteShape(0, 3)); assertEquals(listOf(2), e.listShapes(0).map { it.id })
    }
    @Test fun masterFallbackAndSharedImageRelationship() {
        val source = fixture(); val pkg = OoxmlPackage.open(source)
        val layout = "ppt/slideLayouts/slideLayout1.xml"
        val sp = all(pkg.xml(layout).rootElement).first { it.name == "spPr" }; sp.clearContent()
        pkg.addRelationship(layout, "${R.uri}/slideMaster", "../slideMasters/master.xml")
        pkg.putBytes("ppt/slideMasters/master.xml", slide("""<p:sp><p:nvSpPr><p:cNvPr id="1" name="Master"/><p:nvPr><p:ph type="title"/></p:nvPr></p:nvSpPr><p:spPr><a:xfrm><a:off x="7" y="8"/><a:ext cx="9" cy="10"/></a:xfrm></p:spPr></p:sp>""").toByteArray())
        val intermediate = output(); assertTrue(pkg.saveTo(intermediate) is EditResult.Ok)
        assertEquals(Rect(7, 8, 9, 10), PptxEditor(intermediate).listShapes(0).first().rectEmu)
        val e = PptxEditor(source); val png = temp.newFile("shared.png").apply { writeBytes(byteArrayOf(1)) }; e.addImage(0, Rect(0, 0, 100, 100), png)
        val imagePkg = save(e); val pic = all(root(imagePkg)).first { it.name == "pic" }; val copy = pic.createCopy()
        copy.firstChild(P, "nvPicPr")!!.firstChild(P, "cNvPr")!!.addAttribute("id", "11"); pic.parent.add(copy)
        val file = output(); imagePkg.saveTo(file)
        val remove = PptxEditor(file); remove.deleteShape(0, 10)
        assertTrue(save(remove).relationships("ppt/slides/slide2.xml").any { it.type == REL_IMAGE })
    }
    @Test fun nestedGroupsAndGraphicFrameResizePersist() {
        val pkg = OoxmlPackage.open(fixture())
        val group = all(root(pkg)).first { it.name == "grpSp" }
        val child = group.firstChild(P, "sp")!!
        val nested = group.createCopy()
        nested.firstChild(P, "nvGrpSpPr")!!.firstChild(P, "cNvPr")!!.addAttribute("id", "20")
        nested.firstChild(P, "sp")!!.firstChild(P, "nvSpPr")!!.firstChild(P, "cNvPr")!!.addAttribute("id", "21")
        group.remove(child); group.add(nested)
        val file = output(); pkg.saveTo(file)
        val editor = PptxEditor(file)
        assertEquals(Rect(3200, 8300, 1600, 4500), editor.listShapes(0).last().rectEmu)
        assertTrue(editor.moveShape(0, 21, Rect(3600, 9200, 2000, 5400)))
        assertTrue(editor.moveShape(0, 3, Rect(2000, 3000, 8000, 12000)))
        val moved = save(editor)
        val out = output(); moved.saveTo(out)
        assertEquals(Rect(7200, 17400, 4000, 10800), PptxEditor(out).listShapes(0).last().rectEmu)
        val tree = root(moved).firstChild(P, "cSld")!!.firstChild(P, "spTree")!!
        val frame = newElement(P, "graphicFrame")
        val nv = frame.addElement(com.wxiwei.office.fc.dom4j.QName("nvGraphicFramePr", P))
        nv.addElement(com.wxiwei.office.fc.dom4j.QName("cNvPr", P)).addAttribute("id", "30").addAttribute("name", "Table")
        frame.addElement(com.wxiwei.office.fc.dom4j.QName("graphic", A)).addElement(com.wxiwei.office.fc.dom4j.QName("graphicData", A)).addElement(com.wxiwei.office.fc.dom4j.QName("tbl", A))
        tree.add(frame)
        val tableFile = output(); moved.saveTo(tableFile)
        val tables = PptxEditor(tableFile)
        assertTrue(tables.moveShape(0, 30, Rect(30, 40, 500, 600)))
        val result = save(tables)
        val xf = all(root(result)).first { it.name == "graphicFrame" }.firstChild(P, "xfrm")!!
        assertEquals("30", xf.firstChild(A, "off")!!.attributeValue("x"))
        assertEquals(ShapeKind.TABLE, tables.listShapes(0).last().kind)
    }

    @Test fun invalidOperationsDoNotWriteAndLegacyIsUnsupported() {
        val e = PptxEditor(fixture()); assertFalse(e.moveShape(0, 404, Rect(0, 0, 1, 1)))
        // A rejected op is never queued, so it must not block saving the valid ones (live editing)
        assertTrue(e.lastError != null)
        val out = output(); assertTrue(e.save(out) is EditResult.Ok); assertTrue(out.exists())
        assertEquals(Reason.UNSUPPORTED_FORMAT, (PptxEditor(temp.newFile("old.ppt")).save(out) as EditResult.Error).reason)
        assertEquals(-1, e.addTextBox(0, Rect(0, 0, -1, 1), "bad"))
    }
}
