/*
 * Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * Proprietary and confidential. Unauthorized copying, modification or distribution of this
 * file, via any medium, is strictly prohibited without the written permission of dongb2002.
 */
package com.wxiwei.office.editor

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reader.pdfviewer.tools.PdfSource
import com.reader.pdfviewer.tools.PdfTools
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Manual check of PDF → Word on real files, not part of the normal run: converts every PDF of the
 * folder given as `-e pdfDir <folder>` and saves, for the first pages, the PDF page and the same
 * page of the Word document as the reader shows it, under files/to-word/ of the test app.
 */
@RunWith(AndroidJUnit4::class)
class PdfToWordRealFilesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val tools = PdfTools(context)

    /** What pdfium tells of the characters of page 1: count, then some sizes, weights and fonts. */
    private fun styles(pdf: File): String {
        val pdfium = com.reader.pdfviewer.pdfium.PdfiumCore(context)
        val doc = pdfium.newDocument(android.os.ParcelFileDescriptor.open(pdf, android.os.ParcelFileDescriptor.MODE_READ_ONLY), null)
        try {
            val page = pdfium.getPageForWord(doc, 0) ?: return "no page\n"
            val layout = page.text
            val st = page.styles
            val sb = StringBuilder("chars=${layout.charCount} styles=${st.sizes.size}\n")
            val step = maxOf(1, layout.charCount / 40)
            for (i in 0 until layout.charCount step step) {
                val box = layout.charBox(i)
                sb.append("'${String(Character.toChars(layout.charCodePoint(i).coerceAtLeast(32)))}' size=${st.sizes.getOrNull(i)} box=${box?.height()} w=${st.weight(i)} it=${st.italic(i)} fb=${st.forceBold(i)} font=${st.font(i)} color=${Integer.toHexString(st.colors.getOrElse(i) { 0 })}\n")
            }
            return sb.toString()
        } finally {
            pdfium.closeDocument(doc)
        }
    }

    @Test
    fun convertFolder() {
        val dir = InstrumentationRegistry.getArguments().getString("pdfDir")
        assumeTrue("no pdfDir given", dir != null)
        val out = File(context.getExternalFilesDir(null), "to-word").apply { deleteRecursively(); mkdirs() }
        val pages = InstrumentationRegistry.getArguments().getString("pages")?.toInt() ?: 3
        for (pdf in File(dir!!).listFiles { f -> f.extension.equals("pdf", true) }.orEmpty().sortedBy { it.name }) {
            val name = pdf.nameWithoutExtension.replace(Regex("[^A-Za-z0-9_-]"), "_")
            val word = File(out, "$name.docx")
            val started = System.currentTimeMillis()
            val count = try {
                runBlocking { tools.toWord(PdfSource(pdf), word) }
            } catch (e: Exception) {
                File(out, "$name.error.txt").writeText(e.stackTraceToString())
                continue
            }
            File(out, "$name.txt").writeText("pages=$count ms=${System.currentTimeMillis() - started} bytes=${word.length()}\n" + styles(pdf))
            runBlocking {
                tools.renderPages(PdfSource(pdf), (0 until minOf(pages, count)).toList(), 800) { i, bitmap ->
                    File(out, "${name}_pdf_${i + 1}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
                }
            }
            OpenDocument.open(word, { it.layout != null }) { reader ->
                delay(1500)
                for (page in 1..pages) {
                    val bitmap = reader.thumbnails?.render(page, 800) ?: break
                    File(out, "${name}_word_$page.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
                }
            }
        }
    }
}
