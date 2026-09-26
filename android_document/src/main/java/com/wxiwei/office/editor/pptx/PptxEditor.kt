package com.wxiwei.office.editor.pptx

import com.wxiwei.office.editor.*
import com.wxiwei.office.editor.ooxml.*
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.QName
import java.io.File
import java.io.IOException
import kotlin.math.roundToLong

/** EMU coordinates, with width/height (not right/bottom). */
data class Rect(val x: Long, val y: Long, val width: Long, val height: Long)
data class Size(val width: Long, val height: Long)
data class Point(val x: Long, val y: Long)
enum class ShapeKind { TEXT, PICTURE, TABLE, GROUP, OTHER }
data class PptxShapeInfo(val id: Int, val name: String, val kind: ShapeKind, val rectEmu: Rect,
                         val text: String, val isPlaceholder: Boolean)

/**
 * Zero-based presentation order, including show="0" slides: PPTXReader.processSlide does not
 * filter hidden slides. Mutations queue in order; queries include queued edits. Save replays
 * against a fresh package and atomically writes a new file. Not thread safe.
 * Rectangles are axis aligned; rotation/flip and inherited master artwork are not hit-tested.
 * Failed calls return -1/false/empty and set lastError; a failed queue must be corrected before save.
 */
class PptxEditor(private val source: File) {
    private val ops = mutableListOf<(OoxmlPackage) -> Unit>()
    var lastError: EditResult.Error? = null; private set
    private fun failure(e: Exception): EditResult.Error = EditResult.Error(when (e) {
        is UnsupportedOperationException -> Reason.UNSUPPORTED_FORMAT
        is NoSuchElementException -> Reason.NOT_FOUND
        is IllegalArgumentException -> Reason.INVALID_ARGUMENT
        is IOException -> Reason.IO
        else -> Reason.INTERNAL
    }, e.message ?: "PPTX edit failed", e)
    private fun open(): OoxmlPackage {
        if (!source.extension.equals("pptx", true)) throw UnsupportedOperationException("Only PPTX is editable; legacy PPT is unsupported")
        return OoxmlPackage.open(source)
    }
    // The package with every queued op applied. Kept in memory so each edit costs one op, not a
    // re-read of the whole file; rebuilt by replaying the ops after a failure or undoLast.
    private var current: OoxmlPackage? = null
    private fun snapshot(): OoxmlPackage = current ?: open().also { pkg -> ops.forEach { it(pkg) }; current = pkg }
    private fun <T> read(fallback: T, block: (OoxmlPackage) -> T): T = try { block(snapshot()) } catch (e: Exception) { lastError = failure(e); fallback }
    private fun queue(op: (OoxmlPackage) -> Unit): Boolean = try {
        op(snapshot()); ops.add(op); lastError = null; true
    } catch (e: Exception) { current = null; lastError = failure(e); false } // a failed op may have half applied
    /** Drop the last queued operation (undo). */
    fun undoLast(): Boolean {
        if (ops.isEmpty()) return false
        ops.removeAt(ops.lastIndex); current = null; return true
    }
    val queuedCount: Int get() = ops.size
    fun slideSizeEmu(): Size = read(Size(0, 0)) { pkg ->
        val s = pkg.xml("ppt/presentation.xml").rootElement.firstChild(P, "sldSz") ?: error("Missing slide size")
        Size(s.num("cx"), s.num("cy"))
    }
    private fun part(pkg: OoxmlPackage, index: Int): String {
        val ids = pkg.xml("ppt/presentation.xml").rootElement.firstChild(P, "sldIdLst")?.childrenNamed(P, "sldId").orEmpty()
        require(index in ids.indices) { "Slide index out of range: $index" }
        val rid = ids[index].attributeValue(QName("id", R))
        val rel = pkg.relationships("ppt/presentation.xml").firstOrNull { it.id == rid && it.type == REL_SLIDE && it.targetMode != "External" }
            ?: throw NoSuchElementException("Missing slide relationship: $rid")
        return pkg.resolveTarget("ppt/presentation.xml", rel.target)
    }
    private fun tree(pkg: OoxmlPackage, part: String): Element = pkg.xml(part).rootElement.firstChild(P, "cSld")?.firstChild(P, "spTree") ?: error("Missing shape tree")
    private fun descendants(e: Element): List<Element> = listOf(e) + e.elements().filterIsInstance<Element>().flatMap { descendants(it) }
    private fun nv(e: Element) = e.elements().filterIsInstance<Element>().firstOrNull { it.namespaceURI == P.uri && it.name.startsWith("nv") }
    private fun identity(e: Element) = nv(e)?.firstChild(P, "cNvPr")
    private fun placeholder(e: Element) = nv(e)?.firstChild(P, "nvPr")?.firstChild(P, "ph")
    private fun xfrm(e: Element): Element? = when (e.name) {
        "grpSp" -> e.firstChild(P, "grpSpPr")?.firstChild(A, "xfrm")
        "graphicFrame" -> e.firstChild(P, "xfrm")
        else -> e.firstChild(P, "spPr")?.firstChild(A, "xfrm")
    }
    private fun Element.num(key: String, default: Long = 0) = attributeValue(key)?.toLongOrNull() ?: default
    private fun rect(x: Element?): Rect? {
        val off = x?.firstChild(A, "off") ?: return null
        val ext = x.firstChild(A, "ext") ?: return null
        return Rect(off.num("x"), off.num("y"), ext.num("cx"), ext.num("cy"))
    }
    private fun inherited(pkg: OoxmlPackage, slide: String, shape: Element): Rect? {
        var ph = placeholder(shape) ?: return null
        var current = slide
        for (type in listOf("slideLayout", "slideMaster")) {
            val rel = pkg.relationships(current).firstOrNull { it.type == "${R.uri}/$type" && it.targetMode != "External" } ?: continue
            current = pkg.resolveTarget(current, rel.target)
            val candidates = descendants(tree(pkg, current)).filter { placeholder(it) != null }
            val match = if (type == "slideLayout") {
                candidates.firstOrNull { placeholder(it)!!.num("idx") == ph.num("idx") && (placeholder(it)!!.attributeValue("type") ?: "obj") == (ph.attributeValue("type") ?: "obj") }
                    ?: candidates.firstOrNull { placeholder(it)!!.num("idx") == ph.num("idx") }
            } else candidates.firstOrNull { (placeholder(it)!!.attributeValue("type") ?: "obj") == (ph.attributeValue("type") ?: "obj") }
            if (match != null) { rect(xfrm(match))?.let { return it }; ph = placeholder(match)!! }
        }
        return null
    }
    private data class Transform(val sx: Double = 1.0, val sy: Double = 1.0, val tx: Double = 0.0, val ty: Double = 0.0) {
        fun map(r: Rect) = Rect((r.x * sx + tx).roundToLong(), (r.y * sy + ty).roundToLong(), (r.width * sx).roundToLong(), (r.height * sy).roundToLong())
        fun inverse(r: Rect): Rect { require(sx != 0.0 && sy != 0.0) { "Degenerate group transform" }; return Rect(((r.x - tx) / sx).roundToLong(), ((r.y - ty) / sy).roundToLong(), (r.width / sx).roundToLong(), (r.height / sy).roundToLong()) }
        fun group(x: Element?): Transform {
            val r = rectOf(x) ?: return this
            val off = x?.firstChild(A, "chOff"); val ext = x?.firstChild(A, "chExt")
            val cx = ext?.attributeValue("cx")?.toDoubleOrNull() ?: r.width.toDouble()
            val cy = ext?.attributeValue("cy")?.toDoubleOrNull() ?: r.height.toDouble()
            val gx = if (cx == 0.0) 1.0 else r.width / cx; val gy = if (cy == 0.0) 1.0 else r.height / cy
            return Transform(sx * gx, sy * gy, tx + sx * (r.x - gx * (off?.attributeValue("x")?.toDoubleOrNull() ?: 0.0)), ty + sy * (r.y - gy * (off?.attributeValue("y")?.toDoubleOrNull() ?: 0.0)))
        }
        companion object {
            private fun rectOf(x: Element?): Rect? {
                val o = x?.firstChild(A, "off") ?: return null; val e = x.firstChild(A, "ext") ?: return null
                return Rect(o.attributeValue("x").toLong(), o.attributeValue("y").toLong(), e.attributeValue("cx").toLong(), e.attributeValue("cy").toLong())
            }
        }
    }
    private fun walk(root: Element, t: Transform = Transform()): List<Pair<Element, Transform>> = root.elements().filterIsInstance<Element>().flatMap { e ->
        if (identity(e) == null) emptyList() else listOf(e to t) + if (e.name == "grpSp") walk(e, t.group(xfrm(e))) else emptyList()
    }
    fun listShapes(slideIndex: Int): List<PptxShapeInfo> = read(emptyList()) { pkg ->
        val part = part(pkg, slideIndex)
        walk(tree(pkg, part)).map { (e, t) ->
            val id = identity(e)!!
            PptxShapeInfo(id.num("id").toInt(), id.attributeValue("name") ?: "", when (e.name) {
                "sp" -> ShapeKind.TEXT; "pic" -> ShapeKind.PICTURE; "grpSp" -> ShapeKind.GROUP
                "graphicFrame" -> if (descendants(e).any { it.namespaceURI == A.uri && it.name == "tbl" }) ShapeKind.TABLE else ShapeKind.OTHER
                else -> ShapeKind.OTHER
            }, t.map(rect(xfrm(e)) ?: inherited(pkg, part, e) ?: Rect(0, 0, 0, 0)),
                descendants(e).filter { it.namespaceURI == A.uri && it.name == "p" }.joinToString("\n") { p -> descendants(p).filter { it.namespaceURI == A.uri && it.name in listOf("t", "br") }.joinToString("") { if (it.name == "br") "\n" else it.text } }, placeholder(e) != null)
        }
    }
    private fun nextId(index: Int): Int = read(-1) { pkg ->
        val max = descendants(tree(pkg, part(pkg, index))).filter { it.namespaceURI == P.uri && it.name == "cNvPr" }.maxOfOrNull { it.num("id") } ?: 0L
        require(max < Int.MAX_VALUE); (max + 1).toInt()
    }
    private fun Element.child(ns: Namespace, name: String) = addElement(QName(name, ns))
    private fun geometry(e: Element, r: Rect) {
        require(r.width > 0 && r.height > 0) { "Positive shape dimensions required" }
        e.child(A, "off").addAttribute("x", r.x.toString()).addAttribute("y", r.y.toString())
        e.child(A, "ext").addAttribute("cx", r.width.toString()).addAttribute("cy", r.height.toString())
    }
    private fun shapeProperties(e: Element, r: Rect) = e.child(P, "spPr").apply {
        geometry(child(A, "xfrm"), r); child(A, "prstGeom").addAttribute("prst", "rect").child(A, "avLst")
    }
    private fun paragraphs(body: Element, text: String, rPr: Element?, pPr: Element?) {
        body.childrenNamed(A, "p").forEach { body.remove(it) }
        text.replace("\r\n", "\n").replace('\r', '\n').split('\n').forEach { line ->
            val p = body.child(A, "p"); pPr?.let { p.add(it.createCopy()) }
            val r = p.child(A, "r"); rPr?.let { r.add(it.createCopy()) }; r.child(A, "t").text = line
        }
    }
    fun addTextBox(slideIndex: Int, rectEmu: Rect, text: String, sizePt: Float = 18f, rgbHex: String = "000000", bold: Boolean = false): Int {
        val id = nextId(slideIndex); if (id < 0) return -1
        return if (queue { pkg ->
            require(sizePt.isFinite() && sizePt in 1f..4000f && rgbHex.matches(Regex("[0-9a-fA-F]{6}"))) { "Invalid font size or RGB" }
            val sp = tree(pkg, part(pkg, slideIndex)).child(P, "sp")
            sp.child(P, "nvSpPr").apply { child(P, "cNvPr").addAttribute("id", "$id").addAttribute("name", "TextBox $id"); child(P, "cNvSpPr").addAttribute("txBox", "1"); child(P, "nvPr") }
            shapeProperties(sp, rectEmu).child(A, "noFill")
            val body = sp.child(P, "txBody")
            body.child(A, "bodyPr").addAttribute("wrap", "square").addAttribute("rtlCol", "0").child(A, "spAutoFit"); body.child(A, "lstStyle")
            val rp = newElement(A, "rPr").addAttribute("lang", "vi-VN").addAttribute("sz", (sizePt * 100).roundToLong().toString()).addAttribute("b", if (bold) "1" else "0")
            rp.child(A, "solidFill").child(A, "srgbClr").addAttribute("val", rgbHex.uppercase())
            paragraphs(body, text, rp, null)
        }) id else -1
    }
    fun addImage(slideIndex: Int, rectEmu: Rect, imageFile: File): Int {
        val id = nextId(slideIndex); if (id < 0) return -1
        val bytes = try { imageFile.readBytes() } catch (e: Exception) { lastError = failure(e); return -1 }
        return if (queue { pkg ->
            val part = part(pkg, slideIndex)
            val media = pkg.addMedia("ppt/media", bytes, imageFile.extension)
            val rid = pkg.addRelationship(part, REL_IMAGE, "../media/${media.substringAfterLast('/')}")
            val pic = tree(pkg, part).child(P, "pic")
            pic.child(P, "nvPicPr").apply { child(P, "cNvPr").addAttribute("id", "$id").addAttribute("name", "Picture $id"); child(P, "cNvPicPr").child(A, "picLocks").addAttribute("noChangeAspect", "1"); child(P, "nvPr") }
            pic.child(P, "blipFill").apply { child(A, "blip").addAttribute(QName("embed", R), rid); child(A, "stretch").child(A, "fillRect") }
            shapeProperties(pic, rectEmu)
        }) id else -1
    }
    private fun find(pkg: OoxmlPackage, index: Int, id: Int) = walk(tree(pkg, part(pkg, index))).firstOrNull { identity(it.first)?.num("id") == id.toLong() } ?: throw NoSuchElementException("Shape $id not found")
    fun setShapeText(slideIndex: Int, shapeId: Int, text: String): Boolean = queue { pkg ->
        val e = find(pkg, slideIndex, shapeId).first
        val body = e.firstChild(P, "txBody") ?: throw IllegalArgumentException("Shape has no editable text body")
        val rp = descendants(body).firstOrNull { it.namespaceURI == A.uri && it.name == "r" }?.firstChild(A, "rPr")?.createCopy()
        val pp = body.firstChild(A, "p")?.firstChild(A, "pPr")?.createCopy()
        paragraphs(body, text, rp, pp)
    }
    fun moveShape(slideIndex: Int, shapeId: Int, rectEmu: Rect): Boolean = queue { pkg ->
        val (e, t) = find(pkg, slideIndex, shapeId)
        val r = t.inverse(rectEmu)
        val parent = when (e.name) { "grpSp" -> e.firstChild(P, "grpSpPr") ?: e.child(P, "grpSpPr"); "graphicFrame" -> e; else -> e.firstChild(P, "spPr") ?: e.child(P, "spPr") }
        val old = xfrm(e)
        val replacement = old?.createCopy() ?: newElement(if (e.name == "graphicFrame") P else A, "xfrm")
        listOf("off", "ext").forEach { name -> replacement.childrenNamed(A, name).forEach { replacement.remove(it) } }
        val coords = newElement(A, "xfrm"); geometry(coords, r)
        coords.elements().filterIsInstance<Element>().reversed().forEach { replacement.content().add(0, it.createCopy()) }
        if (old != null) parent.remove(old)
        parent.content().add(if (e.name == "graphicFrame") minOf(1, parent.content().size) else 0, replacement)
    }
    fun deleteShape(slideIndex: Int, shapeId: Int): Boolean = queue { pkg ->
        val part = part(pkg, slideIndex); val e = find(pkg, slideIndex, shapeId).first
        val embeds = descendants(e).mapNotNull { it.attributeValue(QName("embed", R)) }.toSet()
        e.parent.remove(e)
        val used = descendants(pkg.xml(part).rootElement).mapNotNull { it.attributeValue(QName("embed", R)) }.toSet()
        val relPart = pkg.relsPartOf(part)
        if (pkg.has(relPart)) { val rels = pkg.xml(relPart).rootElement; rels.elements().filterIsInstance<Element>().filter { it.attributeValue("Id") in (embeds - used) && it.attributeValue("Type") == REL_IMAGE }.forEach { rels.remove(it) } }
    }
    fun save(target: File): EditResult {
        if (!source.extension.equals("pptx", true)) return EditResult.Error(Reason.UNSUPPORTED_FORMAT, "Legacy PPT is unsupported")
        return try {
            require(source.canonicalFile != target.canonicalFile) { "Save to a new file" }
            snapshot().saveTo(target)
        } catch (e: Exception) { failure(e).also { lastError = it } }
    }
}
