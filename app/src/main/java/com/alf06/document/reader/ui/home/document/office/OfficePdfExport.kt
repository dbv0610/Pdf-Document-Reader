package com.alf06.document.reader.ui.home.document.office

import android.graphics.Color
import android.graphics.pdf.PdfDocument
import com.wxiwei.office.ss.control.ExcelView
import com.wxiwei.office.ss.model.baseModel.Sheet
import com.wxiwei.office.wp.control.Word
import java.io.OutputStream

/** Layout pixels (96 per inch) to PDF points (72 per inch). */
private const val PX_TO_PT = 72f / 96f

/**
 * Every page of a Word document as vector PDF pages of the same size. Pages not laid out yet are
 * left out. Draw on the document's drawing thread. Returns the number of pages written.
 */
internal fun writeWordPdf(word: Word, out: OutputStream, progress: (Int) -> Unit = {}): Int {
    val count = word.getPageCount()
    val doc = PdfDocument()
    var written = 0
    try {
        for (n in 1..count) {
            val bounds = word.getPageBounds(n) ?: continue
            if (bounds.width() <= 0 || bounds.height() <= 0) continue
            val page = doc.startPage(PdfDocument.PageInfo.Builder(Math.round(bounds.width() * PX_TO_PT), Math.round(bounds.height() * PX_TO_PT), n).create())
            page.canvas.drawColor(Color.WHITE)
            page.canvas.scale(PX_TO_PT, PX_TO_PT)
            if (word.drawPage(n, page.canvas)) written++
            doc.finishPage(page)
            progress(n)
        }
        doc.writeTo(out)
    } finally {
        doc.close()
    }
    return written
}

/** One page of a sheet: the part from column edge [left] / row edge [top] to [right] / [bottom] (sheet pixels). */
internal class SheetPage(val sheet: Sheet, val left: Float, val top: Float, val right: Float, val bottom: Float)

/**
 * The used part of every loaded sheet cut into A4 landscape pages at column and row edges (a
 * column or row wider than a page is cut where it must), left to right, then top to bottom.
 */
internal fun sheetPages(excel: ExcelView): List<SheetPage> {
    val book = excel.getSpreadsheet()?.getWorkbook() ?: return emptyList()
    val view = excel.getSpreadsheet()?.getSheetView() ?: return emptyList()
    val maxW = (A4_LONG - 2 * MARGIN) / PX_TO_PT
    val maxH = (A4_SHORT - 2 * MARGIN) / PX_TO_PT
    fun breaks(edges: FloatArray, max: Float): List<Float> {
        val result = arrayListOf(0f)
        var start = 0f
        var last = 0f
        for (e in edges) {
            if (e - start > max) {
                if (last > start) { result += last; start = last }
                while (e - start > max) { start += max; result += start }
            }
            last = e
        }
        if (edges.last() > start) result += edges.last()
        return result
    }
    val pages = ArrayList<SheetPage>()
    for (i in 0 until book.getSheetCount()) {
        val sheet = book.getSheet(i) ?: continue
        val (xs, ys) = view.printEdges(sheet)
        if (xs.isEmpty() || ys.isEmpty()) continue
        val bx = breaks(xs, maxW)
        val by = breaks(ys, maxH)
        for (c in 0 until bx.size - 1) for (r in 0 until by.size - 1) pages += SheetPage(sheet, bx[c], by[r], bx[c + 1], by[r + 1])
    }
    return pages
}

/**
 * Writes [pages] as A4 landscape PDF pages, the cells drawn as vectors without row and column
 * headers. Call on the main thread (the sheet is drawn there): it lets the screen update and
 * can be cancelled between pages.
 */
internal suspend fun writeSheetPdf(excel: ExcelView, pages: List<SheetPage>, out: OutputStream, progress: (Int) -> Unit = {}): Int {
    val view = excel.getSpreadsheet()?.getSheetView() ?: return 0
    val doc = PdfDocument()
    var written = 0
    try {
        pages.forEachIndexed { i, p ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(A4_LONG.toInt(), A4_SHORT.toInt(), i + 1).create())
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            canvas.translate(MARGIN, MARGIN)
            canvas.scale(PX_TO_PT, PX_TO_PT)
            canvas.clipRect(0f, 0f, p.right - p.left, p.bottom - p.top)
            view.drawRegion(p.sheet, Math.round(p.left), Math.round(p.top), 1f, canvas, headers = false)
            doc.finishPage(page)
            written++
            progress(i + 1)
            kotlinx.coroutines.yield()
        }
        doc.writeTo(out)
    } finally {
        doc.close()
    }
    return written
}

private const val A4_LONG = 842f
private const val A4_SHORT = 595f
private const val MARGIN = 28f
