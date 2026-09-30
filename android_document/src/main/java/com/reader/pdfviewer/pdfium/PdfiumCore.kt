/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.reader.pdfviewer.pdfium

import android.graphics.Color
import androidx.annotation.ColorInt
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import com.reader.pdfviewer.pdfium.util.Size
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

class PdfiumCore(ctx: Context) {
    private external fun nativeOpenDocument(fd: Int, password: String?): Long

    private external fun nativeOpenMemDocument(data: ByteArray?, password: String?): Long

    private external fun nativeCloseDocument(docPtr: Long)

    private external fun nativeGetPageCount(docPtr: Long): Int

    private external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long

    private external fun nativeLoadPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray

    private external fun nativeClosePage(pagePtr: Long)

    private external fun nativeClosePages(pagesPtr: LongArray?)

    private external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int

    private external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int

    private external fun nativeGetPageWidthPoint(pagePtr: Long): Int

    private external fun nativeGetPageHeightPoint(pagePtr: Long): Int

    //private native long nativeGetNativeWindow(Surface surface);
    //private native void nativeRenderPage(long pagePtr, long nativeWindowPtr);
    private external fun nativeRenderPage(
        pagePtr: Long, surface: Surface?, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeRenderPageBitmap(
        pagePtr: Long, bitmap: Bitmap?, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeGetDocumentMetaText(docPtr: Long, tag: String?): String?

    private external fun nativeGetFirstChildBookmark(docPtr: Long, bookmarkPtr: Long?): Long?

    private external fun nativeGetSiblingBookmark(docPtr: Long, bookmarkPtr: Long): Long?

    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String?

    private external fun nativeGetBookmarkDestIndex(docPtr: Long, bookmarkPtr: Long): Long

    private external fun nativeGetPageSizeByIndex(docPtr: Long, pageIndex: Int, dpi: Int): Size?

    private external fun nativeGetPageLinks(pagePtr: Long): LongArray

    private external fun nativeGetDestPageIndex(docPtr: Long, linkPtr: Long): Int?

    private external fun nativeGetLinkURI(docPtr: Long, linkPtr: Long): String?

    private external fun nativeGetLinkRect(linkPtr: Long): RectF?

    private external fun nativePageCoordsToDevice(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point

    private external fun nativeLoadTextPage(pagePtr: Long): Long

    private external fun nativeCloseTextPage(textPagePtr: Long)

    private external fun nativeTextCountChars(textPagePtr: Long): Int

    private external fun nativeTextGetText(textPagePtr: Long, startIndex: Int, count: Int): String?

    private external fun nativeTextGetCharIndexAtPos(
        textPagePtr: Long, x: Double, y: Double, toleranceX: Double, toleranceY: Double
    ): Int

    private external fun nativeTextGetCharBox(textPagePtr: Long, index: Int): RectF?
    private external fun nativeTextGetCharBoxes(textPagePtr: Long): FloatArray?
    private external fun nativePageCoordsToDeviceBatch(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int, sizeY: Int, rotate: Int, coords: FloatArray
    ): IntArray?

    private external fun nativeAddTextMarkupAnnot(
        pagePtr: Long, subtype: Int, quads: FloatArray, r: Int, g: Int, b: Int, a: Int, name: String
    ): Boolean

    private external fun nativeAddInkAnnot(pagePtr: Long, points: FloatArray, width: Float,
        r: Int, g: Int, b: Int, a: Int, name: String): Boolean
    private external fun nativeRemoveAnnotByName(pagePtr: Long, name: String): Boolean
    private external fun nativeDeviceToPageCoords(pagePtr: Long, startX: Int, startY: Int,
        sizeX: Int, sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int): android.graphics.PointF?

    private external fun nativeAddFreeTextAnnot(docPtr: Long, pagePtr: Long, text: String, fontPath: String?,
        fontSize: Float, x: Float, y: Float, r: Int, g: Int, b: Int, a: Int, name: String): FloatArray?
    private external fun nativeAddImageAnnot(docPtr: Long, pagePtr: Long, bitmap: Bitmap,
        left: Float, top: Float, right: Float, bottom: Float, name: String): Boolean
    private external fun nativeGetAnnots(pagePtr: Long): Array<String?>
    private external fun nativeRemoveAnnotAt(pagePtr: Long, index: Int): Boolean

    private external fun nativeEditSnapshot(docPtr: Long): ByteArray?
    private external fun nativeOpenEditSnapshot(snapshot: ByteArray): Long

    private external fun nativeSaveAsCopy(docPtr: Long, path: String): Boolean
    private external fun nativeCreateDocument(): Long
    private external fun nativeImportPages(destPtr: Long, srcPtr: Long, pageIndices: IntArray?, insertIndex: Int): Boolean
    private external fun nativeAddJpegPage(docPtr: Long, jpeg: ByteArray, width: Float, height: Float): Boolean
    private external fun nativeGetPageTextLayout(docPtr: Long, pageIndex: Int): Array<Any?>?

    private external fun nativeSetPageRotation(docPtr: Long, pageIndex: Int, rotation: Int): Boolean
    private external fun nativeGetPageRotation(docPtr: Long, pageIndex: Int): Int
    private external fun nativeInsertBlankPage(docPtr: Long, index: Int, width: Float, height: Float): Boolean
    private external fun nativeDeletePage(docPtr: Long, index: Int): Boolean
    private external fun nativeFlattenPage(docPtr: Long, pageIndex: Int): Boolean
    private external fun nativeSaveWithoutSecurity(docPtr: Long, path: String): Boolean
    private external fun nativeAddPageText(docPtr: Long, pageIndex: Int, fontPath: String?, text: String, size: Float,
        r: Int, g: Int, b: Int, a: Int, x: Float, y: Float, angle: Float, anchor: Int): FloatArray?
    private external fun nativeAddInvisibleWords(docPtr: Long, pageIndex: Int, fontPath: String?, words: Array<String>, boxes: FloatArray, turns: Int): Boolean
    private external fun nativeGetPageImages(docPtr: Long, pageIndex: Int): FloatArray?
    private external fun nativeGetRenderedImage(docPtr: Long, pageIndex: Int, objIndex: Int, maxPixels: Int): IntArray?
    private external fun nativeGetPageForWord(docPtr: Long, pageIndex: Int): Array<Any?>?
    private external fun nativeGetImagePixels(docPtr: Long, pageIndex: Int, objIndex: Int, bitmap: Bitmap): Boolean
    private external fun nativeReplaceImageJpeg(docPtr: Long, pageIndex: Int, objIndex: Int, jpeg: ByteArray): Boolean
    private external fun nativeAddNoteAnnot(pagePtr: Long, x: Float, y: Float, r: Int, g: Int, b: Int, a: Int, contents: String?, name: String): Boolean
    private external fun nativeGetAnnotContents(pagePtr: Long, index: Int): String?
    private external fun nativeSetAnnotContents(pagePtr: Long, index: Int, contents: String): Boolean
    private external fun nativeAddShapeAnnot(docPtr: Long, pagePtr: Long, kind: Int, coords: FloatArray,
        r: Int, g: Int, b: Int, a: Int, width: Float, fr: Int, fg: Int, fb: Int, fa: Int, name: String): Boolean
    private external fun nativeInitForms(docPtr: Long): Boolean
    private external fun nativeGetFormFields(pagePtr: Long): Array<String>?
    private external fun nativeSetFormText(pagePtr: Long, annotIndex: Int, text: String): Boolean
    private external fun nativeClickFormField(pagePtr: Long, annotIndex: Int): Boolean
    private external fun nativeSetFormChoice(pagePtr: Long, annotIndex: Int, option: Int): Boolean


    private val mCurrentDpi: Int

    /** Context needed to get screen density  */
    init {
        mCurrentDpi = ctx.resources.displayMetrics.densityDpi
    }

    /** Create new document from file with password  */
    /** Create new document from file  */
    @JvmOverloads
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor, password: String? = null): PdfDocument {
        val document = PdfDocument()
        document.parcelFileDescriptor = fd
        synchronized(lock) {
            document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password)
        }

        return document
    }

    /** Create new document from bytearray with password  */
    /** Create new document from bytearray  */
    @JvmOverloads
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?, password: String? = null): PdfDocument {
        val document = PdfDocument()
        synchronized(lock) {
            document.mNativeDocPtr = nativeOpenMemDocument(data, password)
        }
        return document
    }

    /** Get total numer of pages in document  */
    fun getPageCount(doc: PdfDocument): Int {
        synchronized(lock) {
            return nativeGetPageCount(doc.mNativeDocPtr)
        }
    }

    /** Open page and store native pointer in [PdfDocument]  */
    fun openPage(doc: PdfDocument, pageIndex: Int): Long {
        val pagePtr: Long
        synchronized(lock) {
            pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex)
            doc.mNativePagesPtr[pageIndex] = pagePtr
            return pagePtr
        }
    }

    /** Closes a page opened with [openPage]. */
    fun closePage(doc: PdfDocument, pageIndex: Int) {
        synchronized(lock) {
            doc.mNativeTextPagesPtr.remove(pageIndex)?.let { nativeCloseTextPage(it) }
            doc.mTextCharBoxes.remove(pageIndex)
            doc.mNativePagesPtr.remove(pageIndex)?.let { nativeClosePage(it) }
        }
    }

    /** Open range of pages and store native pointers in [PdfDocument]  */
    fun openPage(doc: PdfDocument, fromIndex: Int, toIndex: Int): LongArray {
        val pagesPtr: LongArray
        synchronized(lock) {
            pagesPtr = nativeLoadPages(doc.mNativeDocPtr, fromIndex, toIndex)
            var pageIndex = fromIndex
            for (page in pagesPtr) {
                if (pageIndex > toIndex) break
                doc.mNativePagesPtr[pageIndex] = page
                pageIndex++
            }
            return pagesPtr
        }
    }

    /**
     * Get page width in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageWidth(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr: Long?
            if ((doc.mNativePagesPtr.get(index).also { pagePtr = it }) != null) {
                return nativeGetPageWidthPixel(pagePtr!!, mCurrentDpi)
            }
            return 0
        }
    }

    /**
     * Get page height in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageHeight(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr: Long?
            if ((doc.mNativePagesPtr.get(index).also { pagePtr = it }) != null) {
                return nativeGetPageHeightPixel(pagePtr!!, mCurrentDpi)
            }
            return 0
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageWidthPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr: Long?
            if ((doc.mNativePagesPtr.get(index).also { pagePtr = it }) != null) {
                return nativeGetPageWidthPoint(pagePtr!!)
            }
            return 0
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageHeightPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            val pagePtr: Long?
            if ((doc.mNativePagesPtr.get(index).also { pagePtr = it }) != null) {
                return nativeGetPageHeightPoint(pagePtr!!)
            }
            return 0
        }
    }

    /**
     * Get size of page in pixels.<br></br>
     * This method does not require given page to be opened.
     */
    /** Size of a page as shown (its rotation applied), in points. */
    fun getPagePointSize(doc: PdfDocument, index: Int): Size? {
        synchronized(lock) {
            return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, 72)
        }
    }

    fun getPageSize(doc: PdfDocument, index: Int): Size? {
        synchronized(lock) {
            return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, mCurrentDpi)
        }
    }

    /**
     * Render page fragment on [Surface]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     */
    /**
     * Render page fragment on [Surface].<br></br>
     * Page must be opened before rendering.
     */
    @JvmOverloads
    fun renderPage(
        doc: PdfDocument, surface: Surface?, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        synchronized(lock) {
            try {
                //nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(
                    doc.mNativePagesPtr.get(pageIndex)!!, surface, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
    }

    /**
     * Render page fragment on [Bitmap]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     * 
     * 
     * For more info see [renderPageBitmap]
     */
    /**
     * Render page fragment on [Bitmap].<br></br>
     * Page must be opened before rendering.
     * 
     * 
     * Supported bitmap configurations:
     * 
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, twice less memory usage
     * 
     */
    @JvmOverloads
    fun renderPageBitmap(
        doc: PdfDocument, bitmap: Bitmap?, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        synchronized(lock) {
            try {
                nativeRenderPageBitmap(
                    doc.mNativePagesPtr.get(pageIndex)!!, bitmap, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
    }

    /**
     * Add a text markup annotation (underline, strikeout...) to an opened page.
     * [quads] holds 8 values per quad in page coordinates: top left, top right, bottom left, bottom right.
     */
    fun addTextMarkupAnnot(doc: PdfDocument, pageIndex: Int, subtype: Int, quads: FloatArray, @ColorInt color: Int, name: String): Boolean {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[pageIndex] ?: return false
            return nativeAddTextMarkupAnnot(
                pagePtr, subtype, quads,
                Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), name
            )
        }
    }

    /** Add a named ink stroke in PDF page coordinates to the document in memory. */
    fun addInkAnnot(doc: PdfDocument, pageIndex: Int, points: FloatArray, width: Float, @ColorInt color: Int, name: String): Boolean {
        synchronized(lock) {
            val page = doc.mNativePagesPtr[pageIndex] ?: return false
            return nativeAddInkAnnot(page, points, width, Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), name)
        }
    }

    /** Remove the annotation whose NM entry equals [name]. */
    fun removeAnnotByName(doc: PdfDocument, pageIndex: Int, name: String): Boolean {
        synchronized(lock) {
            val page = doc.mNativePagesPtr[pageIndex] ?: return false
            return nativeRemoveAnnotByName(page, name)
        }
    }

    /** Map device coordinates through PDFium, respecting page rotation. */
    fun deviceToPageCoords(doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int, sizeY: Int,
        rotate: Int, deviceX: Int, deviceY: Int): android.graphics.PointF? {
        synchronized(lock) {
            val page = doc.mNativePagesPtr[pageIndex] ?: return null
            return nativeDeviceToPageCoords(page, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY)
        }
    }

    /** Add text objects with an embedded font and return PDF bounds. */
    fun addFreeText(doc: PdfDocument, page: Int, text: String, fontPath: String?, size: Float,
        x: Float, y: Float, color: Int, name: String): RectF? = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized null
        nativeAddFreeTextAnnot(doc.mNativeDocPtr, ptr, text, fontPath, size, x, y,
            Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), name)
            ?.let { RectF(it[0], it[1], it[2], it[3]) }
    }

    /** Add an image stamp in PDF coordinates. */
    fun addImage(doc: PdfDocument, page: Int, rect: RectF, bitmap: Bitmap, name: String): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeAddImageAnnot(doc.mNativeDocPtr, ptr, bitmap, rect.left, rect.top, rect.right, rect.bottom, name)
    }

    /** Read annotation metadata from an opened document page. */
    fun getAnnotations(doc: PdfDocument, page: Int): List<com.reader.pdfviewer.model.PdfAnnotationInfo> = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized emptyList()
        nativeGetAnnots(ptr).toList().chunked(2).mapNotNull { row ->
            val v = row[0]?.split(' ') ?: return@mapNotNull null
            com.reader.pdfviewer.model.PdfAnnotationInfo(page, v[0].toInt(), v[1].toInt(),
                RectF(v[2].toFloat(), v[3].toFloat(), v[4].toFloat(), v[5].toFloat()), row[1]?.takeIf { it.isNotEmpty() })
        }
    }

    /** Remove an annotation by its current index. */
    fun removeAnnotAt(doc: PdfDocument, page: Int, index: Int): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeRemoveAnnotAt(ptr, index)
    }

    /** Preserve arbitrary imported annotation dictionaries for deletion undo. */
    internal fun editSnapshot(doc: PdfDocument): ByteArray? = synchronized(lock) {
        if (doc.mNativeDocPtr == 0L) null else nativeEditSnapshot(doc.mNativeDocPtr)
    }

    /** Replace the in-memory document atomically, preserving the set of opened pages. */
    internal fun restoreEditSnapshot(doc: PdfDocument, snapshot: ByteArray): Boolean = synchronized(lock) {
        val replacement = nativeOpenEditSnapshot(snapshot)
        if (replacement == 0L) return@synchronized false
        if (doc.hasForms) nativeInitForms(replacement)
        val pages = HashMap<Int, Long>()
        try {
            for (page in doc.mNativePagesPtr.keys.filterNotNull()) {
                pages[page] = nativeLoadPage(replacement, page)
            }
        } catch (e: Exception) {
            pages.values.forEach { nativeClosePage(it) }
            nativeCloseDocument(replacement)
            return@synchronized false
        }
        doc.mNativeTextPagesPtr.values.forEach { nativeCloseTextPage(it!!) }
        doc.mNativeTextPagesPtr.clear()
        doc.mTextCharBoxes.clear()
        doc.mNativePagesPtr.values.forEach { nativeClosePage(it!!) }
        doc.mNativePagesPtr.clear()
        nativeCloseDocument(doc.mNativeDocPtr)
        doc.mNativeDocPtr = replacement
        doc.mNativePagesPtr.putAll(pages)
        true
    }

    /** Write the document with its in memory changes to [path]  */
    fun saveAsCopy(doc: PdfDocument, path: String): Boolean {
        synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) {
                return false
            }
            return nativeSaveAsCopy(doc.mNativeDocPtr, path)
        }
    }

    /** Create an empty document, for [importPages] or [addJpegPage]. Close it with [closeDocument].  */
    @Throws(IOException::class)
    fun newEmptyDocument(): PdfDocument {
        synchronized(lock) {
            val ptr = nativeCreateDocument()
            if (ptr == 0L) throw IOException("Cannot create document")
            return PdfDocument().also { it.mNativeDocPtr = ptr }
        }
    }

    /**
     * Copy pages of [src] into [dest], keeping text, links and vector content.
     * @param pageIndices zero based, in output order; null copies every page
     * @param insertAt page index in [dest] for the first copy; negative appends
     */
    fun importPages(dest: PdfDocument, src: PdfDocument, pageIndices: IntArray? = null, insertAt: Int = -1): Boolean {
        synchronized(lock) {
            if (dest.mNativeDocPtr == 0L || src.mNativeDocPtr == 0L || pageIndices?.isEmpty() == true) {
                return false
            }
            return nativeImportPages(dest.mNativeDocPtr, src.mNativeDocPtr, pageIndices, insertAt)
        }
    }

    /** Append a [width] x [height] point page filled by the [jpeg] image, stored without re-encoding.  */
    fun addJpegPage(doc: PdfDocument, jpeg: ByteArray, width: Float, height: Float): Boolean {
        synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) {
                return false
            }
            return nativeAddJpegPage(doc.mNativeDocPtr, jpeg, width, height)
        }
    }

    /**
     * Text of a page with the box of every character, read in one native call.
     * Works whether or not the page is opened; null when the page cannot be loaded.
     */
    fun getPageTextLayout(doc: PdfDocument, pageIndex: Int): PdfPageText? {
        synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) {
                return null
            }
            val raw = nativeGetPageTextLayout(doc.mNativeDocPtr, pageIndex) ?: return null
            val size = raw[2] as FloatArray
            return PdfPageText(pageIndex, size[0], size[1], raw[0] as IntArray, raw[1] as FloatArray)
        }
    }

    // ---- page edits (documents opened for a tool, pages not loaded) ----

    /** Sets the rotation of a page, in quarter turns clockwise (0..3). */
    fun setPageRotation(doc: PdfDocument, pageIndex: Int, quarterTurns: Int): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeSetPageRotation(doc.mNativeDocPtr, pageIndex, quarterTurns)
    }

    /** The rotation of a page, in quarter turns clockwise (0..3). */
    fun getPageRotation(doc: PdfDocument, pageIndex: Int): Int = synchronized(lock) {
        if (doc.mNativeDocPtr == 0L) 0 else nativeGetPageRotation(doc.mNativeDocPtr, pageIndex)
    }

    /** Inserts an empty page of [width] x [height] points at [index]; a negative index appends. */
    fun insertBlankPage(doc: PdfDocument, index: Int, width: Float, height: Float): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeInsertBlankPage(doc.mNativeDocPtr, index, width, height)
    }

    fun deletePage(doc: PdfDocument, index: Int): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeDeletePage(doc.mNativeDocPtr, index)
    }

    /** Makes the annotations and form fields of a page part of its content. */
    fun flattenPage(doc: PdfDocument, pageIndex: Int): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeFlattenPage(doc.mNativeDocPtr, pageIndex)
    }

    /** Saves [doc], opened with its password, without encryption. */
    fun saveWithoutSecurity(doc: PdfDocument, path: String): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeSaveWithoutSecurity(doc.mNativeDocPtr, path)
    }

    /**
     * One line of [text] in the content of a page, its [anchor] (0 left, 1 center, 2 right) at
     * ([x], [y]) in points from the bottom left, turned [angle] degrees counter-clockwise.
     * Returns its bounds (top > bottom) or null.
     */
    fun addPageText(doc: PdfDocument, pageIndex: Int, text: String, size: Float, @ColorInt color: Int,
                    x: Float, y: Float, angle: Float = 0f, anchor: Int = 0, fontPath: String? = null): RectF? = synchronized(lock) {
        if (doc.mNativeDocPtr == 0L) return@synchronized null
        nativeAddPageText(doc.mNativeDocPtr, pageIndex, fontPath, text, size,
            Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), x, y, angle, anchor)
            ?.let { RectF(it[0], it[1], it[2], it[3]) }
    }

    /**
     * Invisible [words], each stretched over its box ([boxes]: left, bottom, right, top in points),
     * running the way they read on the page shown turned [turns] quarters clockwise.
     */
    fun addInvisibleWords(doc: PdfDocument, pageIndex: Int, words: Array<String>, boxes: FloatArray, fontPath: String?, turns: Int = 0): Boolean =
        synchronized(lock) { doc.mNativeDocPtr != 0L && nativeAddInvisibleWords(doc.mNativeDocPtr, pageIndex, fontPath, words, boxes, turns) }

    /**
     * A picture placed on a page: [index] of its object, its pixels, its shown size in points and
     * stored bytes; [bounds] is where it is shown, in points from the top left of the page as it is
     * rendered (like [PdfPageText]); [transparent] when it may have a mask or alpha.
     */
    class PageImage(val index: Int, val pixelWidth: Int, val pixelHeight: Int, val shownWidth: Float, val shownHeight: Float,
                    val storedBytes: Long, val bounds: RectF = RectF(), val transparent: Boolean = false)

    /** The opaque pictures of a page. */
    fun getPageImages(doc: PdfDocument, pageIndex: Int): List<PageImage> = synchronized(lock) {
        if (doc.mNativeDocPtr == 0L) return@synchronized emptyList()
        pageImages(nativeGetPageImages(doc.mNativeDocPtr, pageIndex) ?: return@synchronized emptyList())
    }

    private fun pageImages(v: FloatArray): List<PageImage> = (0 until v.size / 11).map { i ->
        val o = i * 11
        PageImage(v[o].toInt(), v[o + 1].toInt(), v[o + 2].toInt(), v[o + 3], v[o + 4], v[o + 5].toLong(),
            RectF(v[o + 6], v[o + 7], v[o + 8], v[o + 9]), v[o + 10] != 0f)
    }

    /** [image] as shown on the page, its transparency kept; null when it cannot be drawn or is over [maxPixels]. */
    fun getRenderedImage(doc: PdfDocument, pageIndex: Int, image: PageImage, maxPixels: Int): Bitmap? {
        val v = synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) null else nativeGetRenderedImage(doc.mNativeDocPtr, pageIndex, image.index, maxPixels)
        } ?: return null
        return try { Bitmap.createBitmap(v, 2, v[0], v[0], v[1], Bitmap.Config.ARGB_8888) } catch (e: Exception) { null }
    }

    /**
     * Font size (points, 0 unknown), style and fill color (ARGB, 0 unknown) of every character of a
     * page, indexed like [getPageTextLayout]; [fonts] holds the font names a style points to.
     */
    class TextStyles(val sizes: FloatArray, private val styles: IntArray, val colors: IntArray, private val fonts: Array<String?>) {
        fun weight(index: Int) = styles[index] and 0xFFFF
        fun italic(index: Int) = styles[index] and (1 shl 16) != 0
        fun forceBold(index: Int) = styles[index] and (1 shl 17) != 0
        fun font(index: Int): String? = (styles[index] ushr 18).let { if (it in 1..fonts.size) fonts[it - 1] else null }
    }

    /** A page for PDF → Word: its text, the style of each character and all its pictures (any of them may be transparent). */
    class WordPage(val text: PdfPageText, val styles: TextStyles, val images: List<PageImage>)

    /** Everything PDF → Word needs of a page, read with one load of it; null when it cannot be loaded. */
    fun getPageForWord(doc: PdfDocument, pageIndex: Int): WordPage? = synchronized(lock) {
        if (doc.mNativeDocPtr == 0L) return@synchronized null
        val raw = nativeGetPageForWord(doc.mNativeDocPtr, pageIndex) ?: return@synchronized null
        val size = raw[2] as FloatArray
        @Suppress("UNCHECKED_CAST")
        WordPage(PdfPageText(pageIndex, size[0], size[1], raw[0] as IntArray, raw[1] as FloatArray),
            TextStyles(raw[3] as FloatArray, raw[4] as IntArray, raw[5] as IntArray, raw[6] as Array<String?>),
            pageImages(raw[7] as FloatArray))
    }

    /** The pixels of [image] into [bitmap] (ARGB_8888, the picture's pixel size). */
    fun getImagePixels(doc: PdfDocument, pageIndex: Int, image: PageImage, bitmap: Bitmap): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeGetImagePixels(doc.mNativeDocPtr, pageIndex, image.index, bitmap)
    }

    /** Stores [jpeg] as the data of [image], which keeps its place on the page. */
    fun replaceImage(doc: PdfDocument, pageIndex: Int, image: PageImage, jpeg: ByteArray): Boolean = synchronized(lock) {
        doc.mNativeDocPtr != 0L && nativeReplaceImageJpeg(doc.mNativeDocPtr, pageIndex, image.index, jpeg)
    }

    // ---- notes, shapes and forms of opened pages ----

    /** A sticky note whose icon's top left is at ([x], [y]). */
    fun addNote(doc: PdfDocument, page: Int, x: Float, y: Float, @ColorInt color: Int, contents: String?, name: String): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeAddNoteAnnot(ptr, x, y, Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color), contents, name)
    }

    fun getAnnotContents(doc: PdfDocument, page: Int, index: Int): String? = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized null
        nativeGetAnnotContents(ptr, index)
    }

    fun setAnnotContents(doc: PdfDocument, page: Int, index: Int, contents: String): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeSetAnnotContents(ptr, index, contents)
    }

    /** A shape: [kind] 0 rectangle, 1 ellipse, 2 line, 3 arrow; [coords] left, top, right, bottom or x1, y1, x2, y2. */
    fun addShape(doc: PdfDocument, page: Int, kind: Int, coords: FloatArray, @ColorInt stroke: Int, width: Float,
                 @ColorInt fill: Int, name: String): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeAddShapeAnnot(doc.mNativeDocPtr, ptr, kind, coords,
            Color.red(stroke), Color.green(stroke), Color.blue(stroke), Color.alpha(stroke), width,
            Color.red(fill), Color.green(fill), Color.blue(fill), Color.alpha(fill), name)
    }

    /** Sets up the interactive form of [doc]; false when it has none. Call before pages are opened. */
    fun initForms(doc: PdfDocument): Boolean = synchronized(lock) {
        (doc.mNativeDocPtr != 0L && nativeInitForms(doc.mNativeDocPtr)).also { doc.hasForms = it }
    }

    fun getFormFields(doc: PdfDocument, page: Int): List<PdfFormField> = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized emptyList()
        nativeGetFormFields(ptr)?.let { PdfFormField.parse(page, it) } ?: emptyList()
    }

    fun setFormText(doc: PdfDocument, page: Int, field: PdfFormField, text: String): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeSetFormText(ptr, field.index, text)
    }

    fun clickFormField(doc: PdfDocument, page: Int, field: PdfFormField): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeClickFormField(ptr, field.index)
    }

    fun setFormChoice(doc: PdfDocument, page: Int, field: PdfFormField, option: Int): Boolean = synchronized(lock) {
        val ptr = doc.mNativePagesPtr[page] ?: return@synchronized false
        nativeSetFormChoice(ptr, field.index, option)
    }

    /** Release native resources and opened file  */
    fun closeDocument(doc: PdfDocument) {
        synchronized(lock) {
            for (index in doc.mNativePagesPtr.keys) {
                nativeClosePage(doc.mNativePagesPtr.get(index)!!)
            }
            doc.mNativePagesPtr.clear()
            for (textPagePtr in doc.mNativeTextPagesPtr.values) {
                nativeCloseTextPage(textPagePtr!!)
            }
            doc.mNativeTextPagesPtr.clear()
            doc.mTextCharBoxes.clear()

            nativeCloseDocument(doc.mNativeDocPtr)
            doc.mNativeDocPtr = 0
            if (doc.parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    doc.parcelFileDescriptor!!.close()
                } catch (e: IOException) {
                    /* ignore */
                }
                doc.parcelFileDescriptor = null
            }
        }
    }

    /** Get metadata for given document  */
    fun getDocumentMeta(doc: PdfDocument): PdfDocument.Meta {
        synchronized(lock) {
            val meta = PdfDocument.Meta()
            meta.title = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Title")
            meta.author = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Author")
            meta.subject = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Subject")
            meta.keywords = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Keywords")
            meta.creator = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Creator")
            meta.producer = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Producer")
            meta.creationDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "CreationDate")
            meta.modDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "ModDate")
            return meta
        }
    }

    /** Get table of contents (bookmarks) for given document  */
    fun getTableOfContents(doc: PdfDocument): MutableList<PdfDocument.Bookmark?> {
        synchronized(lock) {
            val topLevel: MutableList<PdfDocument.Bookmark?> = ArrayList<PdfDocument.Bookmark?>()
            val first = nativeGetFirstChildBookmark(doc.mNativeDocPtr, null)
            if (first != null) {
                recursiveGetBookmark(topLevel, doc, first)
            }
            return topLevel
        }
    }

    private fun recursiveGetBookmark(tree: MutableList<PdfDocument.Bookmark?>, doc: PdfDocument, bookmarkPtr: Long) {
        val bookmark = PdfDocument.Bookmark()
        bookmark.mNativePtr = bookmarkPtr
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeGetBookmarkDestIndex(doc.mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)

        val child = nativeGetFirstChildBookmark(doc.mNativeDocPtr, bookmarkPtr)
        if (child != null) {
            recursiveGetBookmark(bookmark.children, doc, child)
        }

        val sibling = nativeGetSiblingBookmark(doc.mNativeDocPtr, bookmarkPtr)
        if (sibling != null) {
            recursiveGetBookmark(tree, doc, sibling)
        }
    }

    /** Get all links from given page  */
    fun getPageLinks(doc: PdfDocument, pageIndex: Int): MutableList<PdfDocument.Link?> {
        synchronized(lock) {
            val links: MutableList<PdfDocument.Link?> = ArrayList<PdfDocument.Link?>()
            val nativePagePtr = doc.mNativePagesPtr.get(pageIndex)
            if (nativePagePtr == null) {
                return links
            }
            val linkPtrs = nativeGetPageLinks(nativePagePtr)
            for (linkPtr in linkPtrs) {
                val index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr)
                val uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr)

                val rect = nativeGetLinkRect(linkPtr)
                if (rect != null && (index != null || uri != null)) {
                    links.add(PdfDocument.Link(rect, index, uri))
                }
            }
            return links
        }
    }

    /**
     * Map page coordinates to device screen coordinates
     * 
     * @param doc       pdf document
     * @param pageIndex index of page
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    fun mapPageCoordsToDevice(
        doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point {
        val pagePtr: Long = doc.mNativePagesPtr.get(pageIndex)!!
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY)
    }

    /**
     * @return mapped coordinates
     * @see mapPageCoordsToDevice
     */
    fun mapRectToDevice(
        doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, coords: RectF
    ): RectF {
        val leftTop = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.left.toDouble(), coords.top.toDouble()
        )
        val rightBottom = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.right.toDouble(), coords.bottom.toDouble()
        )
        return RectF(leftTop.x.toFloat(), leftTop.y.toFloat(), rightBottom.x.toFloat(), rightBottom.y.toFloat())
    }

    /** Get text page pointer, loading it on first use. Page must be opened before.  */
    private fun textPagePtr(doc: PdfDocument, pageIndex: Int): Long? {
        doc.mNativeTextPagesPtr.get(pageIndex)?.let { return it }
        val pagePtr = doc.mNativePagesPtr.get(pageIndex) ?: return null
        val textPagePtr = nativeLoadTextPage(pagePtr)
        if (textPagePtr == 0L) {
            return null
        }
        doc.mNativeTextPagesPtr.put(pageIndex, textPagePtr)
        return textPagePtr
    }

    /** Get number of characters on page. Page must be opened before.  */
    fun getPageTextCount(doc: PdfDocument, pageIndex: Int): Int {
        synchronized(lock) {
            val textPagePtr = textPagePtr(doc, pageIndex) ?: return 0
            return nativeTextCountChars(textPagePtr)
        }
    }

    /** Get whole text of page. Page must be opened before.  */
    fun getPageText(doc: PdfDocument, pageIndex: Int): String? {
        synchronized(lock) {
            val textPagePtr = textPagePtr(doc, pageIndex) ?: return null
            return nativeTextGetText(textPagePtr, 0, nativeTextCountChars(textPagePtr))
        }
    }

    /**
     * Render the whole page into [bitmap] without keeping the page open, for one-off uses such as OCR.
     * With [renderAnnot] annotations (ink, highlights...) are drawn too.
     * @return false when the page cannot be loaded
     */
    fun renderPageBitmapOnce(doc: PdfDocument, bitmap: Bitmap, pageIndex: Int, renderAnnot: Boolean = false): Boolean {
        synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) {
                return false
            }
            doc.mNativePagesPtr[pageIndex]?.let { pagePtr ->
                nativeRenderPageBitmap(pagePtr, bitmap, mCurrentDpi, 0, 0, bitmap.width, bitmap.height, renderAnnot)
                return true
            }
            val pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex)
            if (pagePtr == 0L) {
                return false
            }
            try {
                nativeRenderPageBitmap(pagePtr, bitmap, mCurrentDpi, 0, 0, bitmap.width, bitmap.height, renderAnnot)
                return true
            } finally {
                nativeClosePage(pagePtr)
            }
        }
    }

    /**
     * Get whole text of page without keeping it open, for one-off reads such as a document search.
     * An opened page is read as usual, any other page is loaded, read and closed right away.
     */
    fun readPageText(doc: PdfDocument, pageIndex: Int): String? {
        synchronized(lock) {
            if (doc.mNativeDocPtr == 0L) {
                return null
            }
            if (doc.mNativePagesPtr.containsKey(pageIndex)) {
                return getPageText(doc, pageIndex)
            }
            val pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex)
            if (pagePtr == 0L) {
                return null
            }
            try {
                val textPagePtr = nativeLoadTextPage(pagePtr)
                if (textPagePtr == 0L) {
                    return null
                }
                try {
                    return nativeTextGetText(textPagePtr, 0, nativeTextCountChars(textPagePtr))
                } finally {
                    nativeCloseTextPage(textPagePtr)
                }
            } finally {
                nativeClosePage(pagePtr)
            }
        }
    }

    /** Get [count] characters of page text starting at [startIndex]. Page must be opened before.  */
    fun getPageText(doc: PdfDocument, pageIndex: Int, startIndex: Int, count: Int): String? {
        synchronized(lock) {
            val textPagePtr = textPagePtr(doc, pageIndex) ?: return null
            return nativeTextGetText(textPagePtr, startIndex, count)
        }
    }

    /**
     * Get index of character at or near given position in page coordinates.
     * @return character index or -1 if none was found
     */
    fun getCharIndexAtCoord(
        doc: PdfDocument, pageIndex: Int, x: Double, y: Double, toleranceX: Double, toleranceY: Double
    ): Int {
        synchronized(lock) {
            val textPagePtr = textPagePtr(doc, pageIndex) ?: return -1
            return nativeTextGetCharIndexAtPos(textPagePtr, x, y, toleranceX, toleranceY)
        }
    }

    /**
     * Get bounding box of character in page coordinates. Page must be opened before.
     * @return null when the index is out of the page text
     */
    fun getCharBox(doc: PdfDocument, pageIndex: Int, charIndex: Int): RectF? {
        synchronized(lock) {
            val boxes = charBoxes(doc, pageIndex) ?: return null
            if (charIndex < 0 || charIndex * 4 + 3 >= boxes.size) return null
            val offset = charIndex * 4
            return RectF(boxes[offset], boxes[offset + 1], boxes[offset + 2], boxes[offset + 3])
        }
    }

    /**
     * Boxes of every character of the page, 4 floats per character (left, top, right, bottom as
     * [getCharBox] returns them), read in one native call and cached with the text page.
     * Page must be opened before. Do not modify the returned array.
     */
    fun getCharBoxes(doc: PdfDocument, pageIndex: Int): FloatArray? {
        synchronized(lock) {
            return charBoxes(doc, pageIndex)
        }
    }

    private fun charBoxes(doc: PdfDocument, pageIndex: Int): FloatArray? {
        doc.mTextCharBoxes[pageIndex]?.let { return it }
        val textPagePtr = textPagePtr(doc, pageIndex) ?: return null
        return nativeTextGetCharBoxes(textPagePtr)?.also { doc.mTextCharBoxes[pageIndex] = it }
    }

    /** One by one read of a char box, only kept to check [getCharBoxes] against in tests.  */
    internal fun getCharBoxDirect(doc: PdfDocument, pageIndex: Int, charIndex: Int): RectF? {
        synchronized(lock) {
            val textPagePtr = textPagePtr(doc, pageIndex) ?: return null
            return nativeTextGetCharBox(textPagePtr, charIndex)
        }
    }

    /**
     * [mapRectToDevice] for many rects at once: [rects] holds 4 floats per rect (left, top, right,
     * bottom) and the result the mapped left, top, right, bottom of each, the same values
     * [mapRectToDevice] returns. Only rects [fromRect] until [toRect] are mapped; the page must be opened.
     */
    fun mapRectsToDevice(
        doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int, sizeY: Int, rotate: Int,
        rects: FloatArray, fromRect: Int = 0, toRect: Int = rects.size / 4
    ): FloatArray? {
        synchronized(lock) {
            val pagePtr = doc.mNativePagesPtr[pageIndex] ?: return null
            if (fromRect < 0 || toRect > rects.size / 4 || fromRect > toRect) return null
            val coords = rects.copyOfRange(fromRect * 4, toRect * 4)
            val device = nativePageCoordsToDeviceBatch(pagePtr, startX, startY, sizeX, sizeY, rotate, coords)
                ?: return null
            return FloatArray(device.size) { device[it].toFloat() }
        }
    }

    companion object {
        private val TAG: String = PdfiumCore::class.java.getName()
        private val FD_CLASS: Class<*> = FileDescriptor::class.java
        private const val FD_FIELD_NAME = "descriptor"

        init {
            try {
                System.loadLibrary("c++_shared")
                System.loadLibrary("pdfium")
                System.loadLibrary("jniPdfium")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native libraries failed to load - " + e)
            }
        }

        /* synchronize native methods: pdfium is not thread safe, even across documents */
        private val lock = Any()
        private var mFdField: Field? = null
        fun getNumFd(fdObj: ParcelFileDescriptor): Int {
            try {
                if (mFdField == null) {
                    mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                    mFdField!!.setAccessible(true)
                }

                return mFdField!!.getInt(fdObj.getFileDescriptor())
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
                return -1
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return -1
            }
        }
    }
}
