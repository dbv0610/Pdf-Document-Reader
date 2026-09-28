/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.reader.pdfviewer.convert

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.roundToLong

/**
 * Writes pages rebuilt by [PageLayout] as a new DOCX: one paragraph per block, each PDF page
 * starting on a new page, pictures inline. The section takes the size and margins of the first
 * page. The text and the pictures of each page go to files in a folder of [tempDir] as soon as
 * the page is added, so memory does not grow with the number of pages; [close] removes them.
 */
class DocxWriter(tempDir: File) : Closeable {

    /** A picture's stored bytes and their format: "jpeg" or "png". */
    class Image(val bytes: ByteArray, val extension: String)

    private val work = File(tempDir, "docx-${System.nanoTime()}").apply { mkdirs() }
    private val bodyFile = File(work, "body.xml")
    private val bodyOut: Writer = bodyFile.bufferedWriter()
    /** The markup of the page being added, written out at the end of [addPage]. */
    private val body = StringBuilder()
    private val media = ArrayList<String>() // extension of picture n + 1, its bytes in work/image<n + 1>
    private var first: LayoutPage? = null
    private var pageCount = 0

    /** Adds [page]; [images] maps the key of each of its [PictureBlock]s to its picture. */
    fun addPage(page: LayoutPage, images: Map<Int, Image>) {
        if (first == null) first = page
        var breakBefore = pageCount > 0
        for (block in page.blocks) {
            when (block) {
                is ParagraphBlock -> paragraph(block, breakBefore)
                is PictureBlock -> {
                    // a hairline (a rule drawn as a picture) is left out
                    if (block.width < MIN_PICTURE || block.height < MIN_PICTURE) continue
                    val image = images[block.key] ?: continue
                    picture(block, image, breakBefore)
                }
            }
            breakBefore = false
        }
        // a page with nothing on it still starts a page
        if (breakBefore || (pageCount == 0 && page.blocks.isEmpty())) {
            body.append("<w:p>").append(if (breakBefore) "<w:pPr><w:pageBreakBefore/></w:pPr>" else "").append("</w:p>")
        }
        pageCount++
        bodyOut.append(body)
        body.setLength(0)
    }

    /** Writes the document to [target], replacing it only once it is complete. */
    fun save(target: File) {
        val parent = target.absoluteFile.parentFile ?: throw IOException("No folder for ${target.name}")
        val temp = File.createTempFile(".docx-", ".tmp", parent)
        try {
            ZipOutputStream(temp.outputStream().buffered()).use { zip ->
                fun put(name: String, data: ByteArray) {
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(data)
                    zip.closeEntry()
                }
                put("[Content_Types].xml", contentTypes().toByteArray())
                put("_rels/.rels", ROOT_RELS.toByteArray())
                bodyOut.flush()
                zip.putNextEntry(ZipEntry("word/document.xml"))
                zip.write(documentHead().toByteArray())
                if (pageCount == 0) zip.write("<w:p/>".toByteArray()) else bodyFile.inputStream().use { it.copyTo(zip) }
                zip.write(documentTail().toByteArray())
                zip.closeEntry()
                put("word/styles.xml", STYLES.toByteArray())
                put("word/_rels/document.xml.rels", documentRels().toByteArray())
                media.forEachIndexed { i, extension ->
                    zip.putNextEntry(ZipEntry("word/media/image${i + 1}.$extension"))
                    File(work, "image${i + 1}").inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            temp.delete()
        }
    }

    override fun close() {
        runCatching { bodyOut.close() }
        work.deleteRecursively()
    }

    private fun paragraph(p: ParagraphBlock, breakBefore: Boolean) {
        val size = (p.fontSize * 2).roundToLong()
        body.append("<w:p><w:pPr>")
        if (breakBefore) body.append("<w:pageBreakBefore/>")
        body.append("<w:spacing w:before=\"").append(twips(p.spaceBefore)).append("\" w:after=\"0\"/>")
        if (p.indent != 0f || p.firstLine != 0f) {
            body.append("<w:ind w:left=\"").append(twips(p.indent)).append('"')
            if (p.firstLine > 0f) body.append(" w:firstLine=\"").append(twips(p.firstLine)).append('"')
            if (p.firstLine < 0f) body.append(" w:hanging=\"").append(twips(-p.firstLine)).append('"')
            body.append("/>")
        }
        jc(p.align)
        body.append("<w:rPr><w:sz w:val=\"").append(size).append("\"/><w:szCs w:val=\"").append(size).append("\"/></w:rPr>")
        body.append("</w:pPr>")
        for (run in p.runs) run(run, p.fontSize)
        body.append("</w:p>")
    }

    private fun run(run: Run, paragraphSize: Float) {
        val style = run.style
        val size = ((style?.size?.takeIf { it > 0f } ?: paragraphSize) * 2).roundToLong()
        body.append("<w:r><w:rPr>")
        style?.font?.let { f ->
            val name = escape(f).replace("\"", "&quot;")
            body.append("<w:rFonts w:ascii=\"").append(name).append("\" w:hAnsi=\"").append(name)
                .append("\" w:eastAsia=\"").append(name).append("\" w:cs=\"").append(name).append("\"/>")
        }
        if (style?.bold == true) body.append("<w:b/><w:bCs/>")
        if (style?.italic == true) body.append("<w:i/><w:iCs/>")
        color(style?.color ?: -1)?.let { body.append("<w:color w:val=\"").append(it).append("\"/>") }
        body.append("<w:sz w:val=\"").append(size).append("\"/><w:szCs w:val=\"").append(size).append("\"/></w:rPr>")
            .append("<w:t xml:space=\"preserve\">").append(escape(run.text)).append("</w:t></w:r>")
    }

    private fun picture(p: PictureBlock, image: Image, breakBefore: Boolean) {
        media += image.extension
        val n = media.size
        File(work, "image$n").writeBytes(image.bytes)
        // at most the text area of the section: a picture that fits on no page stops the layout
        val section = first!!
        val fit = minOf(1f, (section.width - section.marginLeft - section.marginRight) / p.width,
            (section.height - 2 * section.marginTop - PAGE_SLACK) / p.height).coerceAtLeast(0.01f)
        val cx = emu(p.width * fit)
        val cy = emu(p.height * fit)
        body.append("<w:p><w:pPr>")
        if (breakBefore) body.append("<w:pageBreakBefore/>")
        body.append("<w:spacing w:before=\"").append(twips(p.spaceBefore)).append("\" w:after=\"0\"/>")
        jc(p.align)
        body.append("</w:pPr><w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">")
            .append("<wp:extent cx=\"").append(cx).append("\" cy=\"").append(cy).append("\"/>")
            .append("<wp:docPr id=\"").append(n).append("\" name=\"Picture ").append(n).append("\"/>")
            .append("<a:graphic xmlns:a=\"$NS_A\"><a:graphicData uri=\"$NS_PIC\"><pic:pic xmlns:pic=\"$NS_PIC\">")
            .append("<pic:nvPicPr><pic:cNvPr id=\"").append(n).append("\" name=\"image").append(n).append('.').append(image.extension)
            .append("\"/><pic:cNvPicPr/></pic:nvPicPr>")
            .append("<pic:blipFill><a:blip r:embed=\"").append(imageRelId(n)).append("\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>")
            .append("<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"").append(cx).append("\" cy=\"").append(cy)
            .append("\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>")
            .append("</pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>")
    }

    private fun jc(align: Align) {
        when (align) {
            Align.CENTER -> body.append("<w:jc w:val=\"center\"/>")
            Align.RIGHT -> body.append("<w:jc w:val=\"right\"/>")
            Align.LEFT -> Unit
        }
    }

    private fun documentHead() = "$XML_HEAD<w:document xmlns:w=\"$NS_W\" xmlns:r=\"$NS_R\" xmlns:wp=\"$NS_WP\"><w:body>"

    private fun documentTail(): String {
        val page = first
        val width = page?.width ?: A4_WIDTH
        val height = page?.height ?: A4_HEIGHT
        val left = page?.marginLeft ?: 72f
        val right = page?.marginRight ?: 72f
        val top = page?.marginTop ?: 72f
        return "<w:sectPr><w:pgSz w:w=\"${twips(width)}\" w:h=\"${twips(height)}\"" +
            (if (width > height) " w:orient=\"landscape\"" else "") + "/>" +
            "<w:pgMar w:top=\"${twips(top)}\" w:right=\"${twips(right)}\" w:bottom=\"${twips(top)}\" w:left=\"${twips(left)}\"" +
            " w:header=\"360\" w:footer=\"360\" w:gutter=\"0\"/></w:sectPr></w:body></w:document>"
    }

    private fun contentTypes(): String {
        val types = media.distinct().joinToString("") {
            "<Default Extension=\"$it\" ContentType=\"image/$it\"/>"
        }
        return "$XML_HEAD<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>$types" +
            "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>" +
            "<Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/></Types>"
    }

    private fun documentRels(): String = buildString {
        append(XML_HEAD).append("<Relationships xmlns=\"$NS_RELS\">")
        append("<Relationship Id=\"rId1\" Type=\"$NS_R/styles\" Target=\"styles.xml\"/>")
        media.forEachIndexed { i, extension ->
            append("<Relationship Id=\"").append(imageRelId(i + 1)).append("\" Type=\"$NS_R/image\" Target=\"media/image")
                .append(i + 1).append('.').append(extension).append("\"/>")
        }
        append("</Relationships>")
    }

    private fun imageRelId(n: Int) = "rIdImg$n"

    companion object {
        private const val A4_WIDTH = 595f
        private const val A4_HEIGHT = 842f
        private const val XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
        private const val NS_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
        private const val NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
        private const val NS_WP = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
        private const val NS_A = "http://schemas.openxmlformats.org/drawingml/2006/main"
        private const val NS_PIC = "http://schemas.openxmlformats.org/drawingml/2006/picture"
        private const val NS_RELS = "http://schemas.openxmlformats.org/package/2006/relationships"
        private const val ROOT_RELS = XML_HEAD + "<Relationships xmlns=\"$NS_RELS\"><Relationship Id=\"rId1\" " +
            "Type=\"$NS_R/officeDocument\" Target=\"word/document.xml\"/></Relationships>"
        // same defaults as the blank document template: Arial, single spacing, no space after
        private const val STYLES = XML_HEAD + "<w:styles xmlns:w=\"$NS_W\"><w:docDefaults><w:rPrDefault><w:rPr>" +
            "<w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:eastAsia=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/>" +
            "</w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>" +
            "</w:pPrDefault></w:docDefaults><w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">" +
            "<w:name w:val=\"Normal\"/><w:qFormat/></w:style></w:styles>"

        /**
         * The color to write, or null for the default black: black itself, and very light text,
         * which sat on a background the document no longer has.
         */
        internal fun color(rgb: Int): String? {
            if (rgb < 0) return null
            val r = rgb shr 16 and 0xFF; val g = rgb shr 8 and 0xFF; val b = rgb and 0xFF
            val light = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            if (light > LIGHT_TEXT || (r < 16 && g < 16 && b < 16)) return null
            return "%02X%02X%02X".format(r, g, b)
        }

        private const val LIGHT_TEXT = 0.8
        private const val MIN_PICTURE = 2f
        private const val PAGE_SLACK = 24f

        private fun twips(points: Float): Long = (points * 20).roundToLong()
        private fun emu(points: Float): Long = (points * 12700).roundToLong().coerceAtLeast(1)

        /** Text for XML: markup escaped, characters XML 1.0 cannot hold dropped. */
        internal fun escape(text: String): String = buildString(text.length) {
            var i = 0
            while (i < text.length) {
                val c = text[i]
                when {
                    c == '&' -> append("&amp;")
                    c == '<' -> append("&lt;")
                    c == '>' -> append("&gt;")
                    c == '\t' || c == '\n' || c == '\r' -> append(' ')
                    c < ' ' || c == '￾' || c == '￿' -> Unit
                    c.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate() -> {
                        append(c).append(text[i + 1]); i++
                    }
                    c.isSurrogate() -> Unit
                    else -> append(c)
                }
                i++
            }
        }
    }
}
