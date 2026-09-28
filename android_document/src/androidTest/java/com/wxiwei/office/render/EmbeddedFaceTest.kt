package com.wxiwei.office.render

import android.graphics.Paint
import android.graphics.Typeface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wxiwei.office.editor.DocumentCreator
import com.wxiwei.office.editor.EditResult
import com.wxiwei.office.editor.OpenDocument
import com.wxiwei.office.editor.OpenDocument.onMain
import com.wxiwei.office.simpletext.font.FontTypefaceManage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * C5: a presentation embedding a family as regular (Tinos) and bold (Tinos Bold): its bold text is
 * drawn with the embedded bold face, not one synthesized from the regular.
 */
@RunWith(AndroidJUnit4::class)
class EmbeddedFaceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun asset(name: String) = context.assets.open("fonts/$name").use { it.readBytes() }

    /** [source] with the fonts embedded as "EmbTest" (regular + bold). */
    private fun withEmbeddedFonts(source: File, target: File) {
        ZipFile(source).use { zip ->
            ZipOutputStream(target.outputStream()).use { out ->
                for (e in zip.entries()) {
                    var bytes = zip.getInputStream(e).readBytes()
                    when (e.name) {
                        "ppt/presentation.xml" -> {
                            val x = bytes.toString(Charsets.UTF_8)
                            val list = "<p:embeddedFontLst><p:embeddedFont><p:font typeface=\"EmbTest\"/><p:regular r:id=\"rIdEmbR\"/><p:bold r:id=\"rIdEmbB\"/></p:embeddedFont></p:embeddedFontLst>"
                            // CT_Presentation: ... sldSz, notesSz, smartTags, embeddedFontLst, ...
                            val at = Regex("<p:notesSz[^>]*/>").find(x)!!.range.last + 1
                            bytes = (x.substring(0, at) + list + x.substring(at)).toByteArray()
                        }
                        "ppt/_rels/presentation.xml.rels" -> {
                            val x = bytes.toString(Charsets.UTF_8)
                            val rels = "<Relationship Id=\"rIdEmbR\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/font\" Target=\"fonts/font1.fntdata\"/>" +
                                "<Relationship Id=\"rIdEmbB\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/font\" Target=\"fonts/font2.fntdata\"/>"
                            bytes = x.replace("</Relationships>", "$rels</Relationships>").toByteArray()
                        }
                        "[Content_Types].xml" -> {
                            bytes = bytes.toString(Charsets.UTF_8).replace("<Default ", "<Default Extension=\"fntdata\" ContentType=\"application/x-fontdata\"/><Default ").toByteArray()
                        }
                    }
                    out.putNextEntry(ZipEntry(e.name)); out.write(bytes); out.closeEntry()
                }
                out.putNextEntry(ZipEntry("ppt/fonts/font1.fntdata")); out.write(asset("Tinos-Regular.ttf")); out.closeEntry()
                out.putNextEntry(ZipEntry("ppt/fonts/font2.fntdata")); out.write(asset("Tinos-Bold.ttf")); out.closeEntry()
            }
        }
    }

    @Test
    fun boldFaceOfAnEmbeddedFamily() {
        val blank = File(context.cacheDir, "emb_blank.pptx").apply { delete() }
        assertTrue(DocumentCreator.create(context, DocumentCreator.Format.POWERPOINT, blank) is EditResult.Ok)
        val file = File(context.cacheDir, "emb_faces.pptx").apply { delete() }
        withEmbeddedFonts(blank, file)
        OpenDocument.open(file, { it.pageCount >= 1 }) { _ ->
            val kit = FontTypefaceManage.instance()
            assertTrue("family registered", kit.hasEmbeddedFont("EmbTest"))
            val index = onMain { kit.addFontName("EmbTest") }
            val bold = onMain { kit.getFontTypeface(index, true, false) }
            // the real bold face measures like Tinos Bold; a synthesized one like the regular, slightly widened
            val tinosBold = Typeface.createFromAsset(context.assets, "fonts/Tinos-Bold.ttf")
            fun width(t: Typeface) = Paint().apply { typeface = t; textSize = 100f }.measureText("Hamburgefonstiv")
            android.util.Log.i("EmbFace", "embedded bold ${width(bold)}, Tinos Bold ${width(tinosBold)}, regular ${width(kit.getFontTypeface(index))}")
            assertEquals("drawn with the embedded bold face", width(tinosBold), width(bold), 0.5f)
        }
    }
}
