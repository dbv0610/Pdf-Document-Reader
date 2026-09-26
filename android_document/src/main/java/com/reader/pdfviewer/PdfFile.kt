package com.reader.pdfviewer

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.SparseBooleanArray
import com.reader.pdfviewer.exception.PageRenderingException
import com.reader.pdfviewer.util.PageSizeCalculator
import com.reader.pdfviewer.pdfium.PdfDocument
import com.reader.pdfviewer.pdfium.PdfiumCore
import com.reader.pdfviewer.pdfium.util.Size
import com.reader.pdfviewer.pdfium.util.SizeF
import kotlin.math.max

class PdfFile(
    private val pdfiumCore: PdfiumCore?,
    private val pdfDocument: PdfDocument,
    /**
     * The pages the user want to display in order
     * (ex: 0, 2, 2, 8, 8, 1, 1, 1)
     */
    private var originalUserPages: IntArray?,
    private val displayOptions: DisplayOptions
) {
    var pagesCount: Int = 0
        private set
    /**
     * Original page sizes
     */
    private val originalPageSizes: MutableList<Size> = ArrayList<Size>()
    /**
     * Scaled page sizes
     */
    private val pageSizes: MutableList<SizeF> = ArrayList<SizeF>()
    /**
     * Opened pages with indicator whether opening was successful
     */
    private val openedPages = SparseBooleanArray()
    /**
     * Page with maximum width
     */
    private var originalMaxWidthPageSize = Size(0, 0)
    /**
     * Page with maximum height
     */
    private var originalMaxHeightPageSize = Size(0, 0)
    /**
     * Scaled page with maximum height
     */
    private var maxHeightPageSize: SizeF? = SizeF(0f, 0f)
    /**
     * Scaled page with maximum width
     */
    private var maxWidthPageSize: SizeF? = SizeF(0f, 0f)
    /**
     * Calculated offsets for pages
     */
    private val pageOffsets: MutableList<Float?> = ArrayList<Float?>()
    /**
     * Calculated auto spacing for pages
     */
    private val pageSpacing: MutableList<Float?> = ArrayList<Float?>()
    /**
     * Calculated document length (width or height, depending on swipe mode)
     */
    private var documentLength = 0f

    init {
        setup(this.displayOptions.viewSize)
    }

    private fun setup(viewSize: Size) {
        if (originalUserPages != null) {
            pagesCount = originalUserPages!!.size
        } else {
            pagesCount = pdfiumCore!!.getPageCount(pdfDocument)
        }

        for (i in 0..<pagesCount) {
            val pageSize = pdfiumCore!!.getPageSize(pdfDocument, documentPage(i)) ?: Size(0, 0)
            if (pageSize.width > originalMaxWidthPageSize.width) {
                originalMaxWidthPageSize = pageSize
            }
            if (pageSize.height > originalMaxHeightPageSize.height) {
                originalMaxHeightPageSize = pageSize
            }
            originalPageSizes.add(pageSize)
        }

        recalculatePageSizes(viewSize)
    }

    /**
     * Call after view size change to recalculate page sizes, offsets and document length
     * 
     * @param viewSize new size of changed view
     */
    fun recalculatePageSizes(viewSize: Size) {
        pageSizes.clear()
        val calculator = PageSizeCalculator(
            displayOptions.pageFitPolicy,
            originalMaxWidthPageSize,
            originalMaxHeightPageSize,
            viewSize,
            displayOptions.fitEachPage
        )
        maxWidthPageSize = calculator.optimalMaxWidthPageSize
        maxHeightPageSize = calculator.optimalMaxHeightPageSize

        for (size in originalPageSizes) {
            pageSizes.add(calculator.calculate(size))
        }
        if (displayOptions.pdfSpacing.autoSpacing) {
            prepareAutoSpacing(viewSize)
        }
        prepareDocLen()
        preparePagesOffset()
    }

    fun getPageSize(pageIndex: Int): SizeF {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return SizeF(0f, 0f)
        }
        return pageSizes[pageIndex]
    }

    fun getScaledPageSize(pageIndex: Int, zoom: Float): SizeF {
        val size = getPageSize(pageIndex)
        return SizeF(size.width * zoom, size.height * zoom)
    }

    val maxPageSize: SizeF?
        /**
         * get page size with biggest dimension (width in vertical mode and height in horizontal mode)
         * 
         * @return size of page
         */
        get() = if (displayOptions.isVertical) maxWidthPageSize else maxHeightPageSize

    val maxPageWidth: Float
        get() = this.maxPageSize?.width ?: 0f

    val maxPageHeight: Float
        get() = this.maxPageSize?.height ?: 0f

    private fun prepareAutoSpacing(viewSize: Size) {
        pageSpacing.clear()
        for (i in 0..< this.pagesCount) {
            val pageSize = pageSizes.get(i)
            var spacing = max(
                0f,
                if (displayOptions.isVertical) viewSize.height - pageSize.height else viewSize.width - pageSize.width
            )
            if (i < this.pagesCount - 1) {
                spacing += displayOptions.pdfSpacing.pageSeparatorSpacing.toFloat()
            }
            pageSpacing.add(spacing)
        }
    }

    private fun prepareDocLen() {
        var length = 0f
        for (i in 0..<this.pagesCount) {
            val pageSize = pageSizes.get(i)
            length += if (displayOptions.isVertical) pageSize.height else pageSize.width
            if (displayOptions.pdfSpacing.autoSpacing) {
                length += pageSpacing.get(i)!!
            } else if (i < this.pagesCount - 1) {
                length += displayOptions.pdfSpacing.pageSeparatorSpacing.toFloat()
            }
        }
        documentLength =
            length + displayOptions.pdfSpacing.startSpacing + displayOptions.pdfSpacing.endSpacing
    }

    private fun preparePagesOffset() {
        pageOffsets.clear()
        var offset = 0f
        for (i in 0..<this.pagesCount) {
            val pageSize = pageSizes.get(i)
            val size = if (displayOptions.isVertical) pageSize.height else pageSize.width
            if (displayOptions.pdfSpacing.autoSpacing) {
                offset += pageSpacing.get(i)!! / 2f
                if (i == 0) {
                    offset -= displayOptions.pdfSpacing.pageSeparatorSpacing / 2f
                } else if (i == this.pagesCount - 1) {
                    offset += displayOptions.pdfSpacing.pageSeparatorSpacing / 2f
                }
                pageOffsets.add(offset)
                offset += size + pageSpacing.get(i)!! / 2f
            } else {
                // Adding a space at the beginning to be able to zoom out with a space between the top of the screen
                // and the first page of the PDF
                if (i == 0) {
                    offset += displayOptions.pdfSpacing.startSpacing.toFloat()
                }
                pageOffsets.add(offset)
                offset += size + displayOptions.pdfSpacing.pageSeparatorSpacing
            }
        }
    }

    fun getDocLen(zoom: Float): Float {
        return documentLength * zoom
    }

    /**
     * Get the page's height if swiping vertical, or width if swiping horizontal.
     */
    fun getPageLength(pageIndex: Int, zoom: Float): Float {
        val size = getPageSize(pageIndex)
        return (if (displayOptions.isVertical) size.height else size.width) * zoom
    }

    fun getPageSpacing(pageIndex: Int, zoom: Float): Float {
        val spacing: Float
        if (displayOptions.pdfSpacing.autoSpacing) {
            spacing = pageSpacing.get(pageIndex)!!
        } else {
            spacing = displayOptions.pdfSpacing.pageSeparatorSpacing.toFloat()
        }
        return spacing * zoom
    }

    /**
     * Get primary page offset, that is Y for vertical scroll and X for horizontal scroll
     */
    fun getPageOffset(pageIndex: Int, zoom: Float): Float {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return 0f
        }
        return pageOffsets.get(pageIndex)!! * zoom
    }

    /**
     * Get secondary page offset, that is X for vertical scroll and Y for horizontal scroll
     */
    fun getSecondaryPageOffset(pageIndex: Int, zoom: Float): Float {
        val pageSize = getPageSize(pageIndex)
        if (displayOptions.isVertical) {
            val maxWidth = this.maxPageWidth
            return zoom * (maxWidth - pageSize.width) / 2 //x
        } else {
            val maxHeight = this.maxPageHeight
            return zoom * (maxHeight - pageSize.height) / 2 //y
        }
    }

    fun getPageAtOffset(offset: Float, zoom: Float): Int {
        var currentPage = 0
        for (i in 0..<this.pagesCount) {
            val off = pageOffsets.get(i)!! * zoom - getPageSpacing(i, zoom) / 2f
            if (off >= offset) {
                break
            }
            currentPage++
        }
        return if (--currentPage >= 0) currentPage else 0
    }

    @Throws(PageRenderingException::class)
    fun openPage(pageIndex: Int): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return false
        }

        synchronized(lock) {
            if (openedPages.indexOfKey(docPage) < 0) {
                try {
                    pdfiumCore!!.openPage(pdfDocument, docPage)
                    openedPages.put(docPage, true)
                    return true
                } catch (e: Exception) {
                    openedPages.put(docPage, false)
                    throw PageRenderingException(pageIndex, e)
                }
            }
            return false
        }
    }

    fun pageHasError(pageIndex: Int): Boolean {
        val docPage = documentPage(pageIndex)
        return !openedPages.get(docPage, false)
    }

    fun renderPageBitmap(bitmap: Bitmap?, pageIndex: Int, bounds: Rect, annotationRendering: Boolean) {
        val docPage = documentPage(pageIndex)
        pdfiumCore!!.renderPageBitmap(
            pdfDocument, bitmap, docPage,
            bounds.left, bounds.top, bounds.width(), bounds.height(), annotationRendering
        )
    }

    val metaData: PdfDocument.Meta?
        get() = pdfiumCore!!.getDocumentMeta(pdfDocument)

    val bookmarks: MutableList<PdfDocument.Bookmark?>?
        get() = pdfiumCore!!.getTableOfContents(pdfDocument)

    fun getPageLinks(pageIndex: Int): MutableList<PdfDocument.Link?>? {
        val docPage = documentPage(pageIndex)
        return pdfiumCore!!.getPageLinks(pdfDocument, docPage)
    }

    fun mapRectToDevice(
        pageIndex: Int, startX: Int, startY: Int, sizeX: Int, sizeY: Int,
        rect: RectF?
    ): RectF? {
        if (rect == null) {
            return null
        }
        val docPage = documentPage(pageIndex)
        return pdfiumCore!!.mapRectToDevice(pdfDocument, docPage, startX, startY, sizeX, sizeY, 0, rect)
    }

    /** [mapRectToDevice] for rects [fromRect] until [toRect] of [rects] (4 floats each), in one native call */
    fun mapRectsToDevice(
        pageIndex: Int, startX: Int, startY: Int, sizeX: Int, sizeY: Int,
        rects: FloatArray, fromRect: Int = 0, toRect: Int = rects.size / 4
    ): FloatArray? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return null
        }
        return pdfiumCore!!.mapRectsToDevice(pdfDocument, docPage, startX, startY, sizeX, sizeY, 0, rects, fromRect, toRect)
    }

    fun getOriginalPageSize(pageIndex: Int): Size? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return Size(0, 0)
        }
        return originalPageSizes.get(pageIndex)
    }

    fun getPageTextCount(pageIndex: Int): Int {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return 0
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return 0
        }
        return pdfiumCore!!.getPageTextCount(pdfDocument, docPage)
    }

    fun getPageText(pageIndex: Int): String? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return ""
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return ""
        }
        return pdfiumCore!!.getPageText(pdfDocument, docPage)
    }

    /** Text of page for a one-off read such as a document search, without keeping the page open */
    fun readPageText(pageIndex: Int): String? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return ""
        }
        return pdfiumCore!!.readPageText(pdfDocument, docPage)
    }

    /** Render the whole page into [bitmap] for OCR, without keeping the page open */
    fun renderPageForOcr(pageIndex: Int, bitmap: Bitmap): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return false
        }
        return pdfiumCore!!.renderPageBitmapOnce(pdfDocument, bitmap, docPage)
    }

    fun getPageText(pageIndex: Int, start: Int, count: Int): String? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0 || count <= 0) {
            return ""
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return ""
        }
        return pdfiumCore!!.getPageText(pdfDocument, docPage, start, count)
    }

    fun getCharIndexAtCoord(pageIndex: Int, pageX: Double, pageY: Double, toleranceX: Double, toleranceY: Double): Int {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return -1
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return -1
        }
        return pdfiumCore!!.getCharIndexAtCoord(pdfDocument, docPage, pageX, pageY, toleranceX, toleranceY)
    }

    fun getCharBox(pageIndex: Int, charIndex: Int): RectF? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0 || charIndex < 0) {
            return null
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return null
        }
        return pdfiumCore!!.getCharBox(pdfDocument, docPage, charIndex)
    }

    /**
     * Boxes of every character of the page, 4 floats per character as [getCharBox] returns them,
     * cached; the size / 4 is [getPageTextCount]. Do not modify the returned array.
     */
    fun getCharBoxes(pageIndex: Int): FloatArray? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return null
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return null
        }
        return pdfiumCore!!.getCharBoxes(pdfDocument, docPage)
    }

    fun addTextMarkup(pageIndex: Int, subtype: Int, quads: FloatArray, color: Int, name: String): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) {
            return false
        }
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return false
        }
        return pdfiumCore!!.addTextMarkupAnnot(pdfDocument, docPage, subtype, quads, color, name)
    }

    /** Add an ink stroke, [points] being x,y pairs in page coordinates and [width] in points. */
    fun addInk(pageIndex: Int, points: FloatArray, width: Float, color: Int, name: String): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return false
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return false
        }
        return pdfiumCore!!.addInkAnnot(pdfDocument, docPage, points, width, color, name)
    }

    /** Remove the annotation whose NM entry is [name]. */
    fun removeAnnotByName(pageIndex: Int, name: String): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return false
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return false
        }
        return pdfiumCore!!.removeAnnotByName(pdfDocument, docPage, name)
    }

    /** Map a point of the page drawn at [sizeX]x[sizeY] pixels to page coordinates, handling the page rotation. */
    fun deviceToPageCoords(pageIndex: Int, sizeX: Int, sizeY: Int, x: Int, y: Int): android.graphics.PointF? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return null
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return null
        }
        return pdfiumCore!!.deviceToPageCoords(pdfDocument, docPage, 0, 0, sizeX, sizeY, 0, x, y)
    }

    /** addFreeText: map the viewer page to its document page before accessing annotations. */
    fun addFreeText(pageIndex: Int, text: String, fontPath: String?, size: Float, x: Float, y: Float, color: Int, name: String): android.graphics.RectF? {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return null
        try { openPage(pageIndex) } catch (ignored: PageRenderingException) { return null }
        return pdfiumCore!!.addFreeText(pdfDocument, docPage, text, fontPath, size, x, y, color, name)
    }

    /** addImage: map the viewer page to its document page before accessing annotations. */
    fun addImage(pageIndex: Int, rect: android.graphics.RectF, bitmap: android.graphics.Bitmap, name: String): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return false
        try { openPage(pageIndex) } catch (ignored: PageRenderingException) { return false }
        return pdfiumCore!!.addImage(pdfDocument, docPage, rect, bitmap, name)
    }

    /** getAnnotations: map the viewer page to its document page before accessing annotations. */
    fun getAnnotations(pageIndex: Int): List<com.reader.pdfviewer.model.PdfAnnotationInfo> {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return emptyList()
        try { openPage(pageIndex) } catch (ignored: PageRenderingException) { return emptyList() }
        return pdfiumCore!!.getAnnotations(pdfDocument, docPage).map { it.copy(page = pageIndex) }
    }

    /** removeAnnotAt: map the viewer page to its document page before accessing annotations. */
    fun removeAnnotAt(pageIndex: Int, index: Int): Boolean {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return false
        try { openPage(pageIndex) } catch (ignored: PageRenderingException) { return false }
        return pdfiumCore!!.removeAnnotAt(pdfDocument, docPage, index)
    }

    /** Snapshot only for undoing deletion of an imported named annotation. */
    internal fun editSnapshot(): ByteArray? = pdfiumCore?.editSnapshot(pdfDocument)

    /** Restore a snapshot without changing page geometry or viewer page mappings. */
    internal fun restoreEditSnapshot(snapshot: ByteArray): Boolean =
        pdfiumCore?.restoreEditSnapshot(pdfDocument, snapshot) == true

    /** Page width in PDF points. */
    fun getPageWidthPoint(pageIndex: Int): Int {
        val docPage = documentPage(pageIndex)
        if (docPage < 0) return 0
        try {
            openPage(pageIndex)
        } catch (ignored: PageRenderingException) {
            return 0
        }
        return pdfiumCore!!.getPageWidthPoint(pdfDocument, docPage)
    }

    fun saveAsCopy(path: String): Boolean {
        return pdfiumCore?.saveAsCopy(pdfDocument, path) ?: false
    }

    fun dispose() {
        pdfiumCore?.closeDocument(pdfDocument)
        originalUserPages = null
    }

    /**
     * Given the UserPage number, this method restrict it
     * to be sure it's an existing page. It takes care of
     * using the user defined pages if any.
     * 
     * @param userPage A page number.
     * @return A restricted valid page number (example : -2 => 0)
     */
    fun determineValidPageNumberFrom(userPage: Int): Int {
        if (userPage <= 0) {
            return 0
        }
        if (originalUserPages != null) {
            if (userPage >= originalUserPages!!.size) {
                return originalUserPages!!.size - 1
            }
        } else {
            if (userPage >= this.pagesCount) {
                return this.pagesCount - 1
            }
        }
        return userPage
    }

    fun documentPage(userPage: Int): Int {
        var documentPage = userPage
        if (originalUserPages != null) {
            if (userPage < 0 || userPage >= originalUserPages!!.size) {
                return -1
            } else {
                documentPage = originalUserPages!![userPage]
            }
        }

        if (documentPage < 0 || userPage >= this.pagesCount) {
            return -1
        }

        return documentPage
    }

    companion object {
        private val lock = Any()
    }
}
