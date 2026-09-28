package com.wxiwei.office.fc.ppt.reader

import android.graphics.Typeface
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.ZipPackage
import com.wxiwei.office.simpletext.font.FontTypefaceManage
import com.wxiwei.office.system.IControl
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Loads fonts embedded in presentation.xml (p:embeddedFontLst). PowerPoint and Canva store them
 * as EOT (.fntdata); uncompressed EOT carries the raw TTF/OTF at the end of the file.
 * Without these, text falls back to a wider system font, wraps and overflows its box.
 */
internal object EmbeddedFontReader {

    private const val EOT_HEADER_MIN = 16
    private const val FLAG_MTX_COMPRESSED = 0x4
    private const val FLAG_XOR_ENCRYPTED = 0x10000000
    private const val XOR_KEY = 0x50

    fun read(control: IControl?, zipPackage: ZipPackage?, presentationPart: PackagePart?, fontLst: Element?) {
        if (control == null || zipPackage == null || presentationPart == null || fontLst == null) {
            return
        }
        val dir = File(control.getSysKit().getPictureManage().getPicTempPath())
        for (font in fontLst.elements("embeddedFont")!!.filterIsInstance<Element>()) {
            try {
                val name = font.element("font")?.attributeValue("typeface") ?: continue
                if (FontTypefaceManage.instance().hasEmbeddedFont(name)) {
                    continue
                }
                fun load(variant: String): Pair<Typeface, ByteArray>? {
                    val rId = font.element(variant)?.attributeValue("id") ?: return null
                    val part = zipPackage.getPart(presentationPart.getRelationship(rId).getTargetURI()) ?: return null
                    val fontData = extractFontData(part.getInputStream().use { it.readBytes() }) ?: return null
                    return createTypeface(dir, fontData)?.let { it to fontData }
                }
                val faces = listOf("regular" to Typeface.NORMAL, "bold" to Typeface.BOLD, "italic" to Typeface.ITALIC, "boldItalic" to Typeface.BOLD_ITALIC)
                    .mapNotNull { (v, style) -> load(v)?.let { style to it } }
                // regular first; a family embedded only as bold/italic still beats the fallback
                val (baseStyle, base) = faces.firstOrNull() ?: continue
                FontTypefaceManage.instance().addEmbeddedFont(name, base.first)
                if (baseStyle == Typeface.NORMAL && weightClass(base.second) >= 600) FontTypefaceManage.instance().markBoldFace(name)
                // its real bold / italic faces, instead of ones synthesized from the regular
                for ((style, face) in faces) if (style != Typeface.NORMAL) FontTypefaceManage.instance().addEmbeddedFace(name, style, face.first)
            } catch (e: Exception) {
                control.getSysKit().getErrorKit().writerLog(e)
            }
        }
    }

    /** Returns the raw sfnt bytes, or null for MTX-compressed or malformed EOT. */
    internal fun extractFontData(data: ByteArray): ByteArray? {
        if (isSfnt(data, 0)) {
            return data
        }
        if (data.size < EOT_HEADER_MIN) {
            return null
        }
        val header = ByteBuffer.wrap(data, 0, EOT_HEADER_MIN).order(ByteOrder.LITTLE_ENDIAN)
        val eotSize = header.getInt(0)
        val fontDataSize = header.getInt(4)
        val flags = header.getInt(12)
        if (flags and FLAG_MTX_COMPRESSED != 0) {
            return null
        }
        if (eotSize > data.size || fontDataSize <= 0 || fontDataSize > eotSize) {
            return null
        }
        val start = eotSize - fontDataSize
        val fontData = data.copyOfRange(start, eotSize)
        if (flags and FLAG_XOR_ENCRYPTED != 0) {
            for (i in fontData.indices) {
                fontData[i] = (fontData[i].toInt() xor XOR_KEY).toByte()
            }
        }
        return if (isSfnt(fontData, 0)) fontData else null
    }

    /** usWeightClass of the OS/2 table (400 regular, 700 bold), or 0 when absent. */
    internal fun weightClass(font: ByteArray): Int {
        if (font.size < 12) return 0
        val buf = ByteBuffer.wrap(font).order(ByteOrder.BIG_ENDIAN)
        val numTables = buf.getShort(4).toInt() and 0xFFFF
        for (i in 0 until numTables) {
            val record = 12 + i * 16
            if (record + 16 > font.size) return 0
            if (buf.getInt(record) == 0x4F532F32 /* OS/2 */) {
                val offset = buf.getInt(record + 8)
                return if (offset >= 0 && offset + 6 <= font.size) buf.getShort(offset + 4).toInt() and 0xFFFF else 0
            }
        }
        return 0
    }

    private fun isSfnt(data: ByteArray, offset: Int): Boolean {
        if (data.size < offset + 4) {
            return false
        }
        val tag = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt(offset)
        return tag == 0x00010000 || tag == 0x4F54544F /* OTTO */ || tag == 0x74727565 /* true */
    }

    private fun createTypeface(dir: File, fontData: ByteArray): Typeface? {
        if (!dir.exists() && !dir.mkdirs()) {
            return null
        }
        val file = File.createTempFile("font", ".ttf", dir)
        try {
            FileOutputStream(file).use { it.write(fontData) }
            return Typeface.createFromFile(file)
        } catch (e: RuntimeException) {
            return null
        } finally {
            file.delete()
        }
    }
}
