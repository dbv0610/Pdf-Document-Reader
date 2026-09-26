package com.wxiwei.office.editor.docx

import com.wxiwei.office.editor.*
import com.wxiwei.office.editor.ooxml.*
import com.wxiwei.office.fc.dom4j.Element
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocxEditorTest {
    @get:Rule val temp = TemporaryFolder()
    private val basic = """<w:p><w:pPr><w:jc w:val="left"/></w:pPr><w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">Hello </w:t></w:r><w:r><w:t>world</w:t><w:tab/></w:r></w:p><w:tbl><w:tr><w:tc><w:p><w:r><w:t>Cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:sectPr/>"""
    private fun fixture(body: String = basic): File = temp.newFile("source-${System.nanoTime()}.docx").also { file ->
        ZipOutputStream(file.outputStream()).use { zip ->
            linkedMapOf(
                "[Content_Types].xml" to """<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>""",
                "word/document.xml" to """<w:document xmlns:w="${W.uri}" xmlns:r="${R.uri}" xmlns:wp="${WP.uri}" xmlns:a="${A.uri}" xmlns:pic="${PIC.uri}"><w:body>$body</w:body></w:document>""",
                "_rels/.rels" to """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="$REL_OFFICE_DOCUMENT" Target="word/document.xml"/></Relationships>"""
            ).forEach { (name, xml) -> zip.putNextEntry(ZipEntry(name)); zip.write(xml.toByteArray()); zip.closeEntry() }
        }
    }
    private fun map(wrong: Boolean = false) = DocxSourceMap().apply {
        addLeaf(0, 6, intArrayOf(0), if (wrong) "WRONG!" else "Hello ", DocxSourceMap.Kind.TEXT)
        addLeaf(6, 12, intArrayOf(1), "world ", DocxSourceMap.Kind.TEXT)
        addLeaf(12, 13, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END)
        addParagraph(0, 0, 13)
        addLeaf(13, 17, intArrayOf(2), "Cell", DocxSourceMap.Kind.TEXT)
        addLeaf(17, 18, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END)
        addParagraph(1, 13, 18)
    }
    private fun output() = File(temp.root, "out-${System.nanoTime()}.docx")
    private fun save(editor: DocxEditor): OoxmlPackage {
        val out = output(); val result = editor.save(out)
        assertTrue(result.toString(), result is EditResult.Ok)
        return OoxmlPackage.open(out)
    }
    private fun body(pkg: OoxmlPackage) = pkg.xml("word/document.xml").rootElement.firstChild(W, "body")!!
    private fun runs(pkg: OoxmlPackage): List<Element> = ArrayList<Element>().also { rr -> DocxEditor.walk(body(pkg)) { if (it.name == "r" && it.namespaceURI == W.uri) rr.add(it) } }
    private fun text(pkg: OoxmlPackage) = runs(pkg).joinToString("") { DocxEditor.runText(it) }
    private fun pr(run: Element, name: String) = run.firstChild(W, "rPr")?.firstChild(W, name)

    @Test fun highlightAcrossBoundarySplitsAndPreservesBoldAndTab() {
        val pkg = save(DocxEditor(fixture(), map()).apply { highlight(3, 9) })
        val rr = runs(pkg)
        assertEquals(listOf("Hel", "lo ", "wor", "ld ", "Cell"), rr.map { DocxEditor.runText(it) })
        assertNotNull(pr(rr[0], "b")); assertNotNull(pr(rr[1], "b")); assertNull(pr(rr[2], "b"))
        assertNull(pr(rr[0], "highlight")); assertEquals("yellow", pr(rr[1], "highlight")!!.attributeValue("val"))
        assertEquals("yellow", pr(rr[2], "highlight")!!.attributeValue("val")); assertNull(pr(rr[3], "highlight"))
        assertNotNull(rr[3].firstChild(W, "tab"))
        assertEquals("preserve", rr[1].firstChild(W, "t")!!.attributeValue("space"))
    }
    @Test fun insertInsideRunInheritsFormatting() {
        val pkg = save(DocxEditor(fixture(), map()).apply { insertText(2, "[chèn]") })
        assertEquals("He[chèn]llo world Cell", text(pkg))
        assertNotNull(pr(runs(pkg)[1], "b"))
    }
    @Test fun deleteAcrossRuns() {
        val pkg = save(DocxEditor(fixture(), map()).apply { deleteText(3, 9) })
        assertEquals("Helld Cell", text(pkg)); assertNotNull(pr(runs(pkg)[0], "b"))
    }
    @Test fun replaceAcrossRuns() {
        val pkg = save(DocxEditor(fixture(), map()).apply { replaceText(3, 9, "ĐÃ SỬA") })
        assertEquals("HelĐÃ SỬAld Cell", text(pkg))
    }
    @Test fun queueOrderAndOriginalOffsetsSurviveOverlappingFormattingAndInsertion() {
        val pkg = save(DocxEditor(fixture(), map()).apply {
            setBold(0, 12, true); setBold(2, 8, false); insertText(3, "X"); setItalic(4, 10, true); deleteText(8, 10)
        })
        assertEquals("HelXlo wod Cell", text(pkg))
        val rr = runs(pkg)
        assertEquals("0", pr(rr.first { DocxEditor.runText(it) == "l" }, "b")!!.attributeValue("val"))
        assertTrue(rr.any { pr(it, "i") != null })
    }
    @Test fun newlineSplitsParagraphAndCopiesParagraphProperties() {
        val pkg = save(DocxEditor(fixture(), map()).apply { insertText(3, "A\nB\nC") })
        val pp = body(pkg).childrenNamed(W, "p")
        assertEquals(3, pp.size)
        assertTrue(pp.all { it.firstChild(W, "pPr")?.firstChild(W, "jc") != null })
        assertEquals(listOf("HelA", "B", "Clo world "), pp.map { it.childrenNamed(W, "r").joinToString("") { r -> DocxEditor.runText(r) } })
    }
    @Test fun imageAddsMediaRelationshipContentTypeAndUniqueIds() {
        val png = temp.newFile("image.png").apply { writeBytes(java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aO1cAAAAASUVORK5CYII=")) }
        val pkg = save(DocxEditor(fixture(), map()).apply { insertImage(2, png, 200, 120); insertImage(7, png, 200, 120) })
        val rels = pkg.relationships("word/document.xml")
        assertEquals(2, rels.size); assertTrue(rels.all { it.type == REL_IMAGE })
        rels.forEach { assertArrayEquals(png.readBytes(), pkg.bytes(pkg.resolveTarget("word/document.xml", it.target))) }
        val ids = ArrayList<String>()
        DocxEditor.walk(body(pkg)) { if (it.name == "docPr") ids.add(it.attributeValue("id")) }
        assertEquals(listOf("1", "2"), ids)
        val types = pkg.xml("[Content_Types].xml").rootElement.elements().filterIsInstance<Element>()
        assertEquals(1, types.count { it.attributeValue("Extension") == "png" && it.attributeValue("ContentType") == "image/png" })
        assertEquals("Hello world Cell", text(pkg))
    }
    @Test fun mismatchDoesNotCreateOrOverwriteOutput() {
        val editor = DocxEditor(fixture(), map(true)).apply { setBold(0, 4, true) }
        val out = output(); assertEquals(Reason.MAP_MISMATCH, (editor.save(out) as EditResult.Error).reason); assertFalse(out.exists())
        out.writeText("keep me"); editor.save(out); assertEquals("keep me", out.readText())
    }
    @Test fun tableRunCanBeEditedAndAppendPrecedesSectionProperties() {
        val pkg = save(DocxEditor(fixture(), map()).apply { replaceText(14, 16, "XX"); appendParagraph("Tail") })
        assertEquals("Hello world CXXlTail", text(pkg))
        assertEquals("sectPr", body(pkg).elements().filterIsInstance<Element>().last().name)
    }
    @Test fun formattingColorsSizesAndOffValues() {
        val pkg = save(DocxEditor(fixture(), map()).apply {
            highlight(0, 6, "#FF12AB34"); setTextColor(0, 6, "ff0000"); setUnderline(0, 6, false); setFontSize(0, 6, 12.5)
        })
        val first = runs(pkg).first()
        assertEquals("12AB34", pr(first, "shd")!!.attributeValue("fill"))
        assertEquals("FF0000", pr(first, "color")!!.attributeValue("val"))
        assertEquals("none", pr(first, "u")!!.attributeValue("val"))
        assertEquals("25", pr(first, "sz")!!.attributeValue("val"))
    }
    @Test fun fieldsAllowFormattingButRejectInteriorInsertion() {
        val file = fixture("""<w:p><w:r><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText>PAGE</w:instrText></w:r><w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>12</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 2, intArrayOf(0, 1, 2, 3, 4), "12", DocxSourceMap.Kind.FIELD)
            addLeaf(2, 3, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 3)
        }
        assertTrue(runs(save(DocxEditor(file, map).apply { setItalic(0, 1, true) })).all { pr(it, "i") != null })
        assertEquals(Reason.INVALID_ARGUMENT, (DocxEditor(file, map).apply { insertText(1, "x") }.save(output()) as EditResult.Error).reason)
        assertEquals("x12", text(save(DocxEditor(file, map).apply { insertText(0, "x") })))
    }
    @Test fun paragraphDeletionJoinsSiblingsButRefusesCellBoundary() {
        val file = fixture("""<w:p><w:r><w:t>ab</w:t></w:r></w:p><w:p><w:r><w:t>cd</w:t></w:r></w:p>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 2, intArrayOf(0), "ab", DocxSourceMap.Kind.TEXT); addLeaf(2, 3, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 3)
            addLeaf(3, 5, intArrayOf(1), "cd", DocxSourceMap.Kind.TEXT); addLeaf(5, 6, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(1, 3, 6)
        }
        val pkg = save(DocxEditor(file, map).apply { deleteText(1, 4) })
        assertEquals("ad", text(pkg)); assertEquals(1, body(pkg).childrenNamed(W, "p").size)
        val out = output()
        assertEquals(Reason.INVALID_ARGUMENT, (DocxEditor(fixture(), map()).apply { deleteText(11, 14) }.save(out) as EditResult.Error).reason)
        assertFalse(out.exists())
    }
    @Test fun readerTextRulesAndUnsafeNfcSplit() {
        val file = fixture("""<w:p><w:r><w:t>é</w:t><w:tab/><w:br/><w:noBreakHyphen/></w:r></w:p>""")
        val map = DocxSourceMap().apply { addLeaf(0, 4, intArrayOf(0), "é \u000b-", DocxSourceMap.Kind.TEXT); addLeaf(4, 5, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 5) }
        assertEquals("é \u000b-", text(save(DocxEditor(file, map).apply { setBold(0, 4, true) })))
        assertEquals(Reason.MAP_MISMATCH, (DocxEditor(file, map).apply { insertText(1, "a") }.save(output()) as EditResult.Error).reason)
    }
    @Test fun legacyDocAndInvalidOffsetsAreRejected() {
        assertEquals(Reason.UNSUPPORTED_FORMAT, (DocxEditor(temp.newFile("old.doc"), map()).save(output()) as EditResult.Error).reason)
        val editor = DocxEditor(fixture(), map())
        assertFalse(editor.insertText(0x1000000000000000L, "x"))
        assertFalse(editor.setFontSize(0, 1, Double.NaN))
        assertFalse(editor.setTextColor(0, 1, "bad"))
    }
    @Test fun primitiveMapGrowsAndRegistryReplacesOldMap() {
        val map = DocxSourceMap.begin("relative.docx")
        repeat(200) { map.addLeaf(it.toLong(), it + 1L, intArrayOf(it), "x", DocxSourceMap.Kind.TEXT); map.addParagraph(it, it.toLong(), it + 1L) }
        assertEquals(200, map.size); assertEquals(199, map.paragraph(199).paraIndex); assertArrayEquals(intArrayOf(199), map.leaf(199).runIndices)
        assertSame(map, DocxSourceMap.get(File("relative.docx").absolutePath))
        assertNotSame(map, DocxSourceMap.begin("relative.docx")); DocxSourceMap.clear("relative.docx")
        assertNull(DocxSourceMap.get("relative.docx"))
    }
    @Test fun repeatedEndInsertionsKeepQueueOrder() {
        val pkg = save(DocxEditor(fixture(), map()).apply { insertText(12, "A"); insertText(12, "B"); insertText(12, "C") })
        assertEquals("Hello world ABCCell", text(pkg))
    }
    @Test fun splitThenDeleteOriginalSeparatorUsesLastNewParagraph() {
        val file = fixture("""<w:p><w:r><w:t>ab</w:t></w:r></w:p><w:p><w:r><w:t>cd</w:t></w:r></w:p>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 2, intArrayOf(0), "ab", DocxSourceMap.Kind.TEXT); addLeaf(2, 3, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 3)
            addLeaf(3, 5, intArrayOf(1), "cd", DocxSourceMap.Kind.TEXT); addLeaf(5, 6, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(1, 3, 6)
        }
        val pkg = save(DocxEditor(file, map).apply { insertText(1, "X\nY"); deleteText(2, 3); deleteText(2, 3) })
        assertEquals(listOf("aX", "Ybcd"), body(pkg).childrenNamed(W, "p").map { it.childrenNamed(W, "r").joinToString("") { r -> DocxEditor.runText(r) } })
    }
    @Test fun simpleFieldBoundaryInsertionStaysOutsideField() {
        val file = fixture("""<w:p><w:fldSimple w:instr="PAGE"><w:r><w:t>12</w:t></w:r></w:fldSimple></w:p>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 2, intArrayOf(0), "12", DocxSourceMap.Kind.FIELD); addLeaf(2, 3, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 3)
        }
        val pkg = save(DocxEditor(file, map).apply { insertText(0, "A"); insertText(2, "B") })
        val p = body(pkg).firstChild(W, "p")!!
        assertEquals(listOf("r", "fldSimple", "r"), p.elements().filterIsInstance<Element>().map { it.name })
        assertEquals("A12B", text(pkg))
    }
    @Test fun globalIndicesIncludeSkippedFallbackAndTextboxDescendants() {
        val file = fixture("""<w:p><w:r><w:t>one</w:t></w:r><x:AlternateContent xmlns:x="${MC.uri}"><x:Fallback><w:r><w:t>skip</w:t><w:p><w:r><w:t>nested</w:t></w:r></w:p></w:r></x:Fallback></x:AlternateContent></w:p><w:sdt><w:sdtContent><w:tbl><w:tr><w:tc><w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl></w:sdtContent></w:sdt>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 3, intArrayOf(0), "one", DocxSourceMap.Kind.TEXT); addLeaf(3, 4, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 4)
            addLeaf(4, 8, intArrayOf(3), "cell", DocxSourceMap.Kind.TEXT); addLeaf(8, 9, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(2, 4, 9)
        }
        val pkg = save(DocxEditor(file, map).apply { replaceText(4, 8, "TABLE") })
        assertEquals("oneskipnestedTABLE", text(pkg))
    }
    @Test fun objectsAllowFormattingAndBoundaryInsertionButNotTextDeletion() {
        val file = fixture("""<w:p><w:r><w:drawing/></w:r></w:p>""")
        val map = DocxSourceMap().apply { addLeaf(0, 1, intArrayOf(0), "1", DocxSourceMap.Kind.OBJECT); addLeaf(1, 2, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 2) }
        val pkg = save(DocxEditor(file, map).apply { highlight(0, 1); insertText(1, "after") })
        assertNotNull(pr(runs(pkg)[0], "highlight")); assertEquals("after", text(pkg))
        assertEquals(Reason.INVALID_ARGUMENT, (DocxEditor(file, map).apply { deleteText(0, 1) }.save(output()) as EditResult.Error).reason)
    }

    @Test fun actualSaxReaderRecordsFieldsTablesSkippedRunsAndParagraphEnds() {
        val file = fixture("""<w:p><w:r><w:t>Hello </w:t></w:r><w:r><w:t>world</w:t><w:tab/></w:r><x:AlternateContent xmlns:x="${MC.uri}"><x:Fallback><w:p><w:r><w:t>skipped</w:t></w:r></w:p></x:Fallback></x:AlternateContent></w:p><w:tbl><w:tr><w:tc><w:p><w:r><w:t>Cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:sdt><w:sdtContent><w:p><w:r><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText>PAGE</w:instrText></w:r><w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>12</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r></w:p></w:sdtContent></w:sdt>""")
        val reader = com.wxiwei.office.fc.doc.DOCXReader(null, file.absolutePath)
        val model = com.wxiwei.office.wp.model.WPDocument()
        reader.javaClass.getDeclaredField("wpdoc").apply { isAccessible = true }.set(reader, model)
        val sax = com.wxiwei.office.fc.dom4j.io.SAXReader()
        val handler = reader.DOCXSaxHandler()
        listOf("p", "tbl", "sdt").forEach { sax.addHandler("/document/body/$it", handler) }
        sax.read(OoxmlPackage.open(file).bytes("word/document.xml")!!.inputStream())
        val map = DocxSourceMap.get(file.absolutePath)!!
        assertEquals(3, map.paragraphCount)
        assertEquals(listOf(0, 2, 3), (0 until map.paragraphCount).map { map.paragraph(it).paraIndex })
        assertEquals("Hello world \nCell\n12\n", (0 until map.size).joinToString("") { map.leaf(it).text })
        assertArrayEquals(intArrayOf(3), map.leaf(3).runIndices)
        val field = (0 until map.size).map { map.leaf(it) }.single { it.kind == DocxSourceMap.Kind.FIELD }
        assertArrayEquals(intArrayOf(4, 5, 6, 7, 8), field.runIndices)
        val pkg = save(DocxEditor(file, map).apply { highlight(3, 9); replaceText(14, 16, "XX"); setItalic(field.start, field.end, true) })
        assertTrue(text(pkg).contains("CXXl"))
        assertEquals("Hello world \n", model.getParagraph(0)!!.getText(model))
    }

    @Test fun multipleObjectsInOneRunCannotBeSplitAtModelLeafBoundary() {
        val file = fixture("""<w:p><w:r><w:pict/></w:r></w:p>""")
        val map = DocxSourceMap().apply {
            addLeaf(0, 1, intArrayOf(0), "1", DocxSourceMap.Kind.OBJECT)
            addLeaf(1, 2, intArrayOf(0), "1", DocxSourceMap.Kind.OBJECT)
            addLeaf(2, 3, intArrayOf(), "\n", DocxSourceMap.Kind.PARA_END); addParagraph(0, 0, 3)
        }
        val out = output()
        assertEquals(Reason.INVALID_ARGUMENT, (DocxEditor(file, map).apply { insertText(1, "x") }.save(out) as EditResult.Error).reason)
        assertFalse(out.exists())
    }

}
