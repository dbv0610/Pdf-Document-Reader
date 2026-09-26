package com.wxiwei.office.editor.docx

import com.wxiwei.office.editor.*
import com.wxiwei.office.editor.ooxml.*
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import java.io.File
import java.text.Normalizer
import kotlin.math.roundToInt

/** Queued operations use ORIGINAL UTF-16 model offsets, with exclusive ends.
 * Queue order is preserved, including overlapping formatting. Inserted text has no original offsets.
 * Save always reopens the source, verifies affected source leaves, then atomically writes a copy.
 */
class DocxEditor(private val source: File, private val map: DocxSourceMap) {
    private data class Op(val start: Long, val end: Long, val type: String, val value: String = "",
                          val image: File? = null, val width: Int = 0, val height: Int = 0)
    private val ops = ArrayList<Op>()
    var lastError: EditResult.Error? = null; private set
    private fun queue(op: Op): Boolean {
        lastError = when {
            !source.extension.equals("docx", true) -> EditResult.Error(Reason.UNSUPPORTED_FORMAT, "Only DOCX is editable")
            op.start < 0 || op.end < op.start || op.end >= 0x1000000000000000L ->
                EditResult.Error(Reason.INVALID_ARGUMENT, "Only MAIN offsets are editable")
            else -> null
        }
        if (lastError != null) return false
        ops.add(op); return true
    }
    private fun invalid(message: String): Boolean { lastError = EditResult.Error(Reason.INVALID_ARGUMENT, message); return false }
    fun highlight(start: Long, end: Long, color: String = "yellow"): Boolean {
        val hex = color.removePrefix("#")
        return if (hex.matches(Regex("(?i)[0-9a-f]{6}|[0-9a-f]{8}"))) queue(Op(start, end, "shd", hex.takeLast(6).uppercase()))
        else if (color in setOf("black", "blue", "cyan", "green", "magenta", "red", "yellow", "white", "darkBlue", "darkCyan", "darkGreen", "darkMagenta", "darkRed", "darkYellow", "darkGray", "lightGray", "none")) queue(Op(start, end, "highlight", color))
        else invalid("Invalid highlight color")
    }
    fun setBold(start: Long, end: Long, on: Boolean) = queue(Op(start, end, "b", if (on) "1" else "0"))
    fun setItalic(start: Long, end: Long, on: Boolean) = queue(Op(start, end, "i", if (on) "1" else "0"))
    fun setUnderline(start: Long, end: Long, on: Boolean) = queue(Op(start, end, "u", if (on) "single" else "none"))
    fun setTextColor(start: Long, end: Long, rgbHex: String): Boolean {
        val rgb = rgbHex.removePrefix("#")
        return if (rgb.matches(Regex("(?i)[0-9a-f]{6}"))) queue(Op(start, end, "color", rgb.uppercase())) else invalid("Expected RRGGBB")
    }
    fun setFontSize(start: Long, end: Long, pt: Number): Boolean =
        if (pt.toDouble().isFinite() && pt.toDouble() in 1.0..1638.0) queue(Op(start, end, "sz", (pt.toDouble() * 2).roundToInt().toString())) else invalid("Invalid font size")
    fun insertText(offset: Long, text: String) = queue(Op(offset, offset, "insert", text))
    fun deleteText(start: Long, end: Long) = queue(Op(start, end, "delete"))
    fun replaceText(start: Long, end: Long, text: String) = queue(Op(start, end, "replace", text))
    fun insertImage(offset: Long, imageFile: File, widthPx: Int, heightPx: Int): Boolean =
        if (widthPx <= 0 || heightPx <= 0 || !imageFile.isFile) invalid("Image file and positive dimensions required")
        else queue(Op(offset, offset, "image", image = imageFile, width = widthPx, height = heightPx))
    fun appendParagraph(text: String) = queue(Op(0, 0, "append", text))

    private class Failure(val reason: Reason, message: String) : Exception(message)
    private fun fail(reason: Reason, message: String): Nothing = throw Failure(reason, message)
    private data class Piece(var start: Long, var end: Long, val run: Element, val kind: DocxSourceMap.Kind)
    private data class Boundary(val parent: Element, val before: Element?, val style: Element?)

    fun save(target: File): EditResult {
        val result = try {
            if (!source.extension.equals("docx", true)) fail(Reason.UNSUPPORTED_FORMAT, "Only DOCX is editable")
            val pkg = OoxmlPackage.open(source)
            Session(pkg).applyAll()
            pkg.saveTo(target)
        } catch (e: Failure) { EditResult.Error(e.reason, e.message ?: "Edit failed")
        } catch (e: IllegalArgumentException) { EditResult.Error(Reason.INVALID_ARGUMENT, e.message ?: "Invalid argument", e)
        } catch (e: java.io.IOException) { EditResult.Error(Reason.IO, e.message ?: "I/O error", e)
        } catch (e: Exception) { EditResult.Error(Reason.INTERNAL, e.message ?: "Edit failed", e) }
        lastError = result as? EditResult.Error
        return result
    }

    private inner class Session(val pkg: OoxmlPackage) {
        val root = pkg.xml("word/document.xml").rootElement
        val body = root.firstChild(W, "body") ?: fail(Reason.MAP_MISMATCH, "No document body")
        val runs = ArrayList<Element>()
        val paragraphs = ArrayList<Element>()
        val leaves = (0 until map.size).map { map.leaf(it) }.sortedBy { it.start }
        val paras = (0 until map.paragraphCount).map { map.paragraph(it) }
        val pieces = ArrayList<Piece>()
        val insertionEnds = HashMap<Long, Element>()
        val deletedSeparators = HashSet<Long>()
        val opaqueSpans = HashMap<Int, LongRange>()
        init {
            body.elements().filterIsInstance<Element>().filter { it.namespaceURI == W.uri && it.name in setOf("p", "tbl", "sdt") }.forEach { child ->
                walk(child) { if (it.namespaceURI == W.uri) when (it.name) { "r" -> runs.add(it); "p" -> paragraphs.add(it) } }
            }
        }
        fun affected(leaf: DocxSourceMap.Leaf) = ops.any { op -> op.type != "append" &&
            if (op.start == op.end) op.start in leaf.start..leaf.end else leaf.start < op.end && leaf.end > op.start }
        fun applyAll() {
            // Verify against untouched DOM, before any operation can change its text.
            for (leaf in leaves) {
                if (leaf.kind == DocxSourceMap.Kind.PARA_END) continue
                val rr = leaf.runIndices.map { runs.getOrNull(it) ?: fail(Reason.MAP_MISMATCH, "Missing run $it") }
                if (affected(leaf)) {
                    val actual = if (leaf.kind == DocxSourceMap.Kind.OBJECT) {
                        if (rr.size == 1 && rr[0].elements().filterIsInstance<Element>().any { it.name in setOf("drawing", "pict", "object", "AlternateContent") }) "1" else ""
                    } else rr.joinToString("") { runText(it) }
                    if (actual != leaf.text || rr.isEmpty()) fail(Reason.MAP_MISMATCH, "Source text differs at ${leaf.start}")
                }
                if (leaf.kind == DocxSourceMap.Kind.OBJECT || leaf.kind == DocxSourceMap.Kind.FIELD) leaf.runIndices.forEach { id ->
                    val old = opaqueSpans[id]
                    opaqueSpans[id] = minOf(old?.first ?: leaf.start, leaf.start)..maxOf(old?.last ?: leaf.end, leaf.end)
                }
                if (leaf.kind == DocxSourceMap.Kind.TEXT && rr.size == 1) pieces.add(Piece(leaf.start, leaf.end, rr[0], leaf.kind))
                else rr.forEach { pieces.add(Piece(leaf.start, leaf.end, it, leaf.kind)) }
            }
            for (op in ops) {
                if (op.type == "append") { append(op.value); continue }
                checkRange(op)
                when (op.type) {
                    "insert" -> insert(boundary(op.start), op.value)?.let { insertionEnds[op.start] = it }
                    "image" -> { val b = boundary(op.start); val run = image(op); addBefore(b.parent, b.before, run); insertionEnds[op.start] = run }
                    "delete", "replace" -> {
                        // Capture insertion anchor before removing original characters.
                        val b = if (op.type == "replace") boundary(op.start) else null
                        if (b != null) insert(b, op.value)?.let { insertionEnds[op.start] = it }
                        delete(op.start, op.end)
                    }
                    else -> format(op)
                }
            }
        }
        fun checkRange(op: Op) {
            if (op.start == op.end) {
                if (paras.none { op.start >= it.start && op.start < it.end }) fail(Reason.INVALID_ARGUMENT, "Offset outside mapped body")
            } else {
                var covered = op.start
                for (leaf in leaves.filter { it.start < op.end && it.end > op.start }) {
                    if (leaf.start > covered) fail(Reason.MAP_MISMATCH, "Unmapped selection")
                    covered = maxOf(covered, leaf.end)
                }
                if (covered < op.end) fail(Reason.MAP_MISMATCH, "Unmapped selection")
            }
        }
        fun split(at: Long) {
            val piece = pieces.firstOrNull { it.kind == DocxSourceMap.Kind.TEXT && at > it.start && at < it.end } ?: return
            val parent = piece.run.parent ?: fail(Reason.MAP_MISMATCH, "Run was removed")
            val cut = (at - piece.start).toInt()
            val originalText = runText(piece.run)
            val raw = rawRunText(piece.run)
            if (raw != originalText) fail(Reason.MAP_MISMATCH, "Cannot split normalized text safely")
            val left = sliceRun(piece.run, 0, cut)
            val right = sliceRun(piece.run, cut, originalText.length)
            addBefore(parent, piece.run, left); addBefore(parent, piece.run, right); piece.run.detach()
            val index = pieces.indexOf(piece)
            pieces.removeAt(index)
            pieces.add(index, Piece(at, piece.end, right, piece.kind))
            pieces.add(index, Piece(piece.start, at, left, piece.kind))
        }
        fun format(op: Op) {
            split(op.end); split(op.start)
            pieces.filter { it.start < op.end && it.end > op.start }.forEach { piece ->
                val pr = piece.run.firstChild(W, "rPr") ?: newElement(W, "rPr").also { addBefore(piece.run, piece.run.elements().filterIsInstance<Element>().firstOrNull(), it) }
                pr.childrenNamed(W, op.type).forEach { it.detach() }
                if (op.type == "highlight") pr.childrenNamed(W, "shd").forEach { it.detach() }
                if (op.type == "shd") pr.childrenNamed(W, "highlight").forEach { it.detach() }
                pr.add(newElement(W, op.type).apply {
                    addAttribute(QName(if (op.type == "shd") "fill" else "val", W), op.value)
                    if (op.type == "shd") addAttribute(QName("val", W), "clear")
                })
                if (op.type == "sz") { pr.childrenNamed(W, "szCs").forEach { it.detach() }; pr.add(newElement(W, "szCs").apply { addAttribute(QName("val", W), op.value) }) }
            }
        }
        fun boundary(at: Long): Boundary {
            if (opaqueSpans.values.any { at > it.first && at < it.last })
                fail(Reason.INVALID_ARGUMENT, "Insert before or after the whole field/object run")
            leaves.firstOrNull { at > it.start && at < it.end && it.kind in setOf(DocxSourceMap.Kind.FIELD, DocxSourceMap.Kind.OBJECT) }?.let {
                fail(Reason.INVALID_ARGUMENT, "Insert before or after fields/objects")
            }
            insertionEnds[at]?.takeIf { it.parent != null }?.let { inserted ->
                val parent = inserted.parent
                val siblings = parent.elements().filterIsInstance<Element>()
                return Boundary(parent, siblings.getOrNull(siblings.indexOf(inserted) + 1), inserted.firstChild(W, "rPr")?.createCopy())
            }
            split(at)
            val next = pieces.firstOrNull { it.start == at && it.run.parent != null }
            if (next != null) {
                val parent = next.run.parent
                val style = next.run.firstChild(W, "rPr")?.createCopy()
                return if (parent.name == "fldSimple") Boundary(parent.parent, parent, style) else Boundary(parent, next.run, style)
            }
            val previous = pieces.lastOrNull { it.end == at && it.run.parent != null }
            if (previous != null) {
                val anchor = if (previous.run.parent.name == "fldSimple") previous.run.parent else previous.run
                val parent = anchor.parent
                val siblings = parent.elements().filterIsInstance<Element>()
                return Boundary(parent, siblings.getOrNull(siblings.indexOf(anchor) + 1), previous.run.firstChild(W, "rPr")?.createCopy())
            }
            val para = paras.firstOrNull { at >= it.start && at < it.end } ?: fail(Reason.INVALID_ARGUMENT, "No paragraph at offset")
            val element = paragraphs.getOrNull(para.paraIndex) ?: fail(Reason.MAP_MISMATCH, "Missing paragraph")
            if (at != para.start && leaves.none { it.kind == DocxSourceMap.Kind.PARA_END && it.start == at }) fail(Reason.INVALID_ARGUMENT, "Offset was deleted by an earlier operation")
            return Boundary(element, null, null)
        }
        fun insert(b: Boundary, text: String): Element? {
            val parts = text.split('\n')
            if (parts.size == 1) {
                if (text.isEmpty()) return null
                return textRun(text, b.style).also { addBefore(b.parent, b.before, it) }
            }
            if (b.parent.name != "p" || b.parent.namespaceURI != W.uri) fail(Reason.INVALID_ARGUMENT, "Paragraph split inside a hyperlink/field is unsupported")
            val para = b.parent
            val parent = para.parent ?: fail(Reason.INVALID_ARGUMENT, "Detached paragraph")
            val after = parent.elements().filterIsInstance<Element>().let { it.getOrNull(it.indexOf(para) + 1) }
            val tail = if (b.before == null) emptyList() else para.elements().filterIsInstance<Element>().let { it.drop(it.indexOf(b.before)) }
            if (parts[0].isNotEmpty()) addBefore(para, b.before, textRun(parts[0], b.style))
            var last = para
            var lastRun: Element? = null
            parts.drop(1).forEach { part ->
                last = newElement(W, "p")
                para.firstChild(W, "pPr")?.let { last.add(it.createCopy()) }
                lastRun = textRun(part, b.style); last.add(lastRun)
                addBefore(parent, after, last)
            }
            tail.forEach { it.detach(); last.add(it) }
            // The original paragraph separator now belongs to the last new paragraph.
            for (i in paragraphs.indices) if (paragraphs[i] === para) paragraphs[i] = last
            return lastRun
        }
        fun delete(start: Long, end: Long) {
            val affected = leaves.filter { it.start < end && it.end > start }
            if (affected.any { it.kind in setOf(DocxSourceMap.Kind.FIELD, DocxSourceMap.Kind.OBJECT) }) fail(Reason.INVALID_ARGUMENT, "Text deletion of fields/objects is unsupported")
            split(end); split(start)
            pieces.filter { it.start >= start && it.end <= end }.toList().forEach { it.run.detach(); pieces.remove(it) }
            // Removing a paragraph separator joins adjacent paragraphs, never crosses cell boundaries.
            affected.filter { it.kind == DocxSourceMap.Kind.PARA_END && it.start !in deletedSeparators }.sortedByDescending { it.start }.forEach { leaf ->
                val p = paras.firstOrNull { leaf.start >= it.start && leaf.start < it.end } ?: fail(Reason.MAP_MISMATCH, "Missing paragraph")
                val first = paragraphs[p.paraIndex]
                val parent = first.parent ?: fail(Reason.INVALID_ARGUMENT, "Paragraph already deleted")
                val siblings = parent.elements().filterIsInstance<Element>()
                val next = siblings.getOrNull(siblings.indexOf(first) + 1)
                if (next == null || next.name != "p" || next.namespaceURI != W.uri) fail(Reason.INVALID_ARGUMENT, "Cannot delete final paragraph or cross table/cell boundaries")
                next.elements().filterIsInstance<Element>().filter { it.name != "pPr" }.forEach { it.detach(); first.add(it) }
                for (i in paragraphs.indices) if (paragraphs[i] === next) paragraphs[i] = first
                next.detach()
                deletedSeparators.add(leaf.start)
            }
        }
        fun append(text: String) { text.split('\n').forEach { part ->
            val p = newElement(W, "p"); p.add(textRun(part, null)); addBefore(body, body.firstChild(W, "sectPr"), p)
        } }
        fun image(op: Op): Element {
            val media = pkg.addMedia("word/media", op.image!!.readBytes(), op.image.extension)
            val rel = pkg.addRelationship("word/document.xml", REL_IMAGE, media.removePrefix("word/"))
            var maxId = 0L
            walk(root) { if (it.name == "docPr" && it.namespaceURI == WP.uri) maxId = maxOf(maxId, it.attributeValue("id")?.toLongOrNull() ?: 0) }
            val cx = pxToEmu(op.width).toString(); val cy = pxToEmu(op.height).toString()
            val run = newElement(W, "r")
            val inline = run.addElement(QName("drawing", W)).addElement(QName("inline", WP))
            inline.addElement(QName("extent", WP)).addAttribute("cx", cx).addAttribute("cy", cy)
            inline.addElement(QName("docPr", WP)).addAttribute("id", (maxId + 1).toString()).addAttribute("name", "Edit image ${maxId + 1}")
            val pic = inline.addElement(QName("graphic", A)).addElement(QName("graphicData", A)).addAttribute("uri", PIC.uri).addElement(QName("pic", PIC))
            val nv = pic.addElement(QName("nvPicPr", PIC))
            nv.addElement(QName("cNvPr", PIC)).addAttribute("id", "0").addAttribute("name", op.image.name)
            nv.addElement(QName("cNvPicPr", PIC))
            val fill = pic.addElement(QName("blipFill", PIC))
            fill.addElement(QName("blip", A)).addAttribute(QName("embed", R), rel)
            fill.addElement(QName("stretch", A)).addElement(QName("fillRect", A))
            val sp = pic.addElement(QName("spPr", PIC))
            val xfrm = sp.addElement(QName("xfrm", A))
            xfrm.addElement(QName("off", A)).addAttribute("x", "0").addAttribute("y", "0")
            xfrm.addElement(QName("ext", A)).addAttribute("cx", cx).addAttribute("cy", cy)
            sp.addElement(QName("prstGeom", A)).addAttribute("prst", "rect").addElement(QName("avLst", A))
            return run
        }
    }
    companion object {
        internal fun walk(root: Element, visit: (Element) -> Unit) { visit(root); root.elements().filterIsInstance<Element>().forEach { walk(it, visit) } }
        private fun childText(child: Element): String = when (child.name) {
            "t" -> child.text ?: ""
            "tab", "ptab" -> " "
            "br" -> if (child.attributeValue("type") == "page") "\u000c" else "\u000b"
            "cr" -> "\u000b"
            "noBreakHyphen" -> "-"
            else -> ""
        }
        private fun rawRunText(run: Element) = run.elements().filterIsInstance<Element>().joinToString("") { childText(it) }
        internal fun runText(run: Element): String {
            var text = rawRunText(run)
            if (text.length > 1) text = text.replace('\u000c', '\u000b')
            return Normalizer.normalize(text, Normalizer.Form.NFC)
        }
        private fun addBefore(parent: Element, before: Element?, child: Element) {
            if (before == null) parent.add(child)
            else { val content = parent.content(); content.add(content.indexOf(before), child) }
        }
        private fun textRun(text: String, style: Element?): Element = newElement(W, "r").apply {
            style?.let { add(it.createCopy()) }
            val buffer = StringBuilder()
            fun flush() { if (buffer.isNotEmpty()) { addElement(QName("t", W)).addAttribute(QName("space", Namespace.XML_NAMESPACE), "preserve").text = buffer.toString(); buffer.setLength(0) } }
            text.forEach { c -> when (c) {
                '\t', '\u000b', '\u000c' -> { flush(); addElement(QName(if (c == '\t') "tab" else "br", W)).also { if (c == '\u000c') it.addAttribute(QName("type", W), "page") } }
                else -> buffer.append(c)
            } }; flush()
        }
        private fun sliceRun(run: Element, start: Int, end: Int): Element {
            val copy = run.createCopy()
            copy.elements().filterIsInstance<Element>().filter { it.name != "rPr" }.forEach { it.detach() }
            var pos = 0
            run.elements().filterIsInstance<Element>().filter { it.name != "rPr" }.forEach { child ->
                val text = childText(child); val a = maxOf(start, pos); val b = minOf(end, pos + text.length)
                if (b > a) copy.add(child.createCopy().apply { if (name == "t") {
                    this.text = text.substring(a - pos, b - pos); addAttribute(QName("space", Namespace.XML_NAMESPACE), "preserve")
                } })
                else if (text.isEmpty()) failUnknownChild(child)
                pos += text.length
            }
            return copy
        }
        private fun failUnknownChild(child: Element) { throw Failure(Reason.MAP_MISMATCH, "Cannot safely split run containing ${child.name}") }
    }
}
