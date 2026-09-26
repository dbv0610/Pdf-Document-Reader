package com.reader.pdfviewer

import android.graphics.RectF
import com.reader.pdfviewer.PageRenderer.RenderingSize
import com.reader.pdfviewer.util.Constants
import com.reader.pdfviewer.util.Constants.Cache.CACHE_SIZE
import com.reader.pdfviewer.util.Constants.PRELOAD_OFFSET
import com.reader.pdfviewer.util.MathUtils
import com.reader.pdfviewer.util.Util
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal class PagesLoader(private val pdfView: PDFView) {
    private var cacheOrder = 0
    private var xOffset = 0f
    private var yOffset = 0f
    private var pageRelativePartWidth = 0f
    private var pageRelativePartHeight = 0f
    private var partRenderWidth = 0f
    private var partRenderHeight = 0f
    private val thumbnailRect = RectF(0f, 0f, 1f, 1f)
    private val preloadOffset: Int

    private class Holder {
        var row: Int = 0
        var col: Int = 0

        override fun toString(): String {
            return "Holder{row=" + row + ", col=" + col + '}'
        }
    }

    private inner class RenderRange {
        var page: Int = 0
        var gridSize: GridSize = GridSize()
        var leftTop: Holder = PagesLoader.Holder()
        var rightBottom: Holder

        init {
            this.rightBottom = PagesLoader.Holder()
        }

        override fun toString(): String {
            return "RenderRange{" +
                    "page=" + page +
                    ", gridSize=" + gridSize +
                    ", leftTop=" + leftTop +
                    ", rightBottom=" + rightBottom +
                    '}'
        }
    }

    private inner class GridSize {
        var rows: Int = 0
        var cols: Int = 0

        override fun toString(): String {
            return "GridSize{rows=" + rows + ", cols=" + cols + '}'
        }
    }

    init {
        this.preloadOffset = Util.getDP(pdfView.getContext(), PRELOAD_OFFSET)
    }

    private val pdfFile: PdfFile
        get() = checkNotNull(pdfView.pdfFile) { "pdfFile == null" }

    fun loadPagesForPrinting(pagesCount: Int) {
        loadAllForPrinting(pagesCount)
    }

    private fun getPageColsRows(grid: GridSize, pageIndex: Int) {
        val size = pdfFile.getPageSize(pageIndex)
        val ratioX = 1f / size.width
        val ratioY = 1f / size.height
        val partHeight = (Constants.PART_SIZE * ratioY) / pdfView.zoom
        val partWidth = (Constants.PART_SIZE * ratioX) / pdfView.zoom
        grid.rows = MathUtils.ceil(1f / partHeight)
        grid.cols = MathUtils.ceil(1f / partWidth)
    }

    private fun calculatePartSize(grid: GridSize) {
        pageRelativePartWidth = 1f / grid.cols.toFloat()
        pageRelativePartHeight = 1f / grid.rows.toFloat()
        partRenderWidth = Constants.PART_SIZE / pageRelativePartWidth
        partRenderHeight = Constants.PART_SIZE / pageRelativePartHeight
    }

    /**
     * calculate the render range of each page
     */
    private fun getRenderRangeList(
        firstXOffset: Float,
        firstYOffset: Float,
        lastXOffset: Float,
        lastYOffset: Float
    ): MutableList<RenderRange> {
        val fixedFirstXOffset = -MathUtils.max(firstXOffset, 0f)
        val fixedFirstYOffset = -MathUtils.max(firstYOffset, 0f)

        val fixedLastXOffset = -MathUtils.max(lastXOffset, 0f)
        val fixedLastYOffset = -MathUtils.max(lastYOffset, 0f)

        val offsetFirst = if (pdfView.isSwipeVertical) fixedFirstYOffset else fixedFirstXOffset
        val offsetLast = if (pdfView.isSwipeVertical) fixedLastYOffset else fixedLastXOffset

        val firstPage = pdfFile.getPageAtOffset(offsetFirst, pdfView.zoom)
        val lastPage = pdfFile.getPageAtOffset(offsetLast, pdfView.zoom)
        val pageCount = lastPage - firstPage + 1

        val renderRanges: MutableList<RenderRange> = LinkedList<RenderRange>()

        for (page in firstPage..lastPage) {
            val range = RenderRange()
            range.page = page

            val pageFirstXOffset: Float
            val pageFirstYOffset: Float
            val pageLastXOffset: Float
            val pageLastYOffset: Float
            if (page == firstPage) {
                pageFirstXOffset = fixedFirstXOffset
                pageFirstYOffset = fixedFirstYOffset
                if (pageCount == 1) {
                    pageLastXOffset = fixedLastXOffset
                    pageLastYOffset = fixedLastYOffset
                } else {
                    val pageOffset = pdfFile.getPageOffset(page, pdfView.zoom)
                    val pageSize = pdfFile.getScaledPageSize(page, pdfView.zoom)
                    if (pdfView.isSwipeVertical) {
                        pageLastXOffset = fixedLastXOffset
                        pageLastYOffset = pageOffset + pageSize.height
                    } else {
                        pageLastYOffset = fixedLastYOffset
                        pageLastXOffset = pageOffset + pageSize.width
                    }
                }
            } else if (page == lastPage) {
                val pageOffset = pdfFile.getPageOffset(page, pdfView.zoom)

                if (pdfView.isSwipeVertical) {
                    pageFirstXOffset = fixedFirstXOffset
                    pageFirstYOffset = pageOffset
                } else {
                    pageFirstYOffset = fixedFirstYOffset
                    pageFirstXOffset = pageOffset
                }

                pageLastXOffset = fixedLastXOffset
                pageLastYOffset = fixedLastYOffset
            } else {
                val pageOffset = pdfFile.getPageOffset(page, pdfView.zoom)
                val pageSize = pdfFile.getScaledPageSize(page, pdfView.zoom)
                if (pdfView.isSwipeVertical) {
                    pageFirstXOffset = fixedFirstXOffset
                    pageFirstYOffset = pageOffset

                    pageLastXOffset = fixedLastXOffset
                    pageLastYOffset = pageOffset + pageSize.height
                } else {
                    pageFirstXOffset = pageOffset
                    pageFirstYOffset = fixedFirstYOffset

                    pageLastXOffset = pageOffset + pageSize.width
                    pageLastYOffset = fixedLastYOffset
                }
            }

            getPageColsRows(range.gridSize, range.page) // get the page's grid size that rows and cols
            val scaledPageSize = pdfFile.getScaledPageSize(range.page, pdfView.zoom)
            val rowHeight = scaledPageSize.height / range.gridSize.rows
            val colWidth = scaledPageSize.width / range.gridSize.cols

            // Get the page offset int the whole file
            // ---------------------------------------
            // |            |           |            |
            // |<--offset-->|   (page)  |<--offset-->|
            // |            |           |            |
            // |            |           |            |
            // ---------------------------------------
            val secondaryOffset = pdfFile.getSecondaryPageOffset(page, pdfView.zoom)

            // calculate the row,col of the point in the leftTop and rightBottom
            val primaryPageOffset = pdfFile.getPageOffset(range.page, pdfView.zoom)
            if (pdfView.isSwipeVertical) {
                range.leftTop.row = MathUtils.floor(
                    max(0f, pageFirstYOffset - primaryPageOffset) / rowHeight
                )
                range.leftTop.col = MathUtils.floor(MathUtils.min(pageFirstXOffset - secondaryOffset, 0f) / colWidth)

                range.rightBottom.row = MathUtils.ceil(
                    abs(pageLastYOffset - primaryPageOffset) / rowHeight
                )
                range.rightBottom.col = MathUtils.floor(
                    MathUtils.min(pageLastXOffset - secondaryOffset, 0f) / colWidth
                )
            } else {
                // Same fix for horizontal swipe mode
                range.leftTop.col = MathUtils.floor(
                    max(0f, pageFirstXOffset - primaryPageOffset) / colWidth
                )
                range.leftTop.row = MathUtils.floor(
                    MathUtils.min(pageFirstYOffset - secondaryOffset, 0f) / rowHeight
                )

                range.rightBottom.col = MathUtils.floor(
                    abs(pageLastXOffset - primaryPageOffset) / colWidth
                )
                range.rightBottom.row = MathUtils.floor(
                    MathUtils.min(pageLastYOffset - secondaryOffset, 0f) / rowHeight
                )
            }

            renderRanges.add(range)
        }

        return renderRanges
    }

    private fun loadAllForPrinting(pagesCount: Int) {
        for (i in 0..<pagesCount) {
            loadThumbnail(i, true)
        }
    }

    /**
     * A part of a page to render, [distance] being its distance to the center of the screen
     */
    private class Cell(
        val page: Int,
        val row: Int,
        val col: Int,
        val relativeWidth: Float,
        val relativeHeight: Float,
        val renderWidth: Float,
        val renderHeight: Float,
        val distance: Float,
    )

    private fun loadVisible() {
        val scaledPreloadOffset = preloadOffset.toFloat()
        val firstXOffset = -xOffset + scaledPreloadOffset
        val lastXOffset = -xOffset - pdfView.width - scaledPreloadOffset
        val firstYOffset = -yOffset + scaledPreloadOffset
        val lastYOffset = -yOffset - pdfView.height - scaledPreloadOffset

        val rangeList = getRenderRangeList(firstXOffset, firstYOffset, lastXOffset, lastYOffset)

        for (range in rangeList) {
            loadThumbnail(range.page, false)
        }

        // Center of the screen in document coordinates
        val centerX = xOffset + pdfView.width / 2f
        val centerY = yOffset + pdfView.height / 2f
        val cells = ArrayList<Cell>()
        for (range in rangeList) {
            calculatePartSize(range.gridSize)
            val pageSize = pdfFile.getScaledPageSize(range.page, pdfView.zoom)
            val primaryOffset = pdfFile.getPageOffset(range.page, pdfView.zoom)
            val secondaryOffset = pdfFile.getSecondaryPageOffset(range.page, pdfView.zoom)
            val pageLeft = if (pdfView.isSwipeVertical) secondaryOffset else primaryOffset
            val pageTop = if (pdfView.isSwipeVertical) primaryOffset else secondaryOffset
            for (row in range.leftTop.row..range.rightBottom.row) {
                for (col in range.leftTop.col..range.rightBottom.col) {
                    val relX = pageRelativePartWidth * col
                    val relY = pageRelativePartHeight * row
                    // Past the last row or column of the page
                    if (relX >= 1f || relY >= 1f) {
                        continue
                    }
                    val cellCenterX = pageLeft + (relX + pageRelativePartWidth / 2f) * pageSize.width
                    val cellCenterY = pageTop + (relY + pageRelativePartHeight / 2f) * pageSize.height
                    cells.add(
                        Cell(
                            range.page, row, col,
                            pageRelativePartWidth, pageRelativePartHeight,
                            partRenderWidth, partRenderHeight,
                            hypot(cellCenterX - centerX, cellCenterY - centerY)
                        )
                    )
                }
            }
        }

        // The renderer handles tasks in order: render what is on screen before the preloaded parts,
        // and give the nearest parts the highest cache order so they are evicted last
        cells.sortBy { it.distance }
        val count = min(cells.size, CACHE_SIZE)
        for (index in 0..<count) {
            val cell = cells[index]
            loadCell(cell, cacheOrder + count - index)
        }
        cacheOrder += count + 1
    }

    private fun loadCell(cell: Cell, order: Int) {
        val relX = cell.relativeWidth * cell.col
        val relY = cell.relativeHeight * cell.row
        var relWidth = cell.relativeWidth
        var relHeight = cell.relativeHeight

        var renderWidth = cell.renderWidth
        var renderHeight = cell.renderHeight
        if (relX + relWidth > 1) {
            relWidth = 1 - relX
        }
        if (relY + relHeight > 1) {
            relHeight = 1 - relY
        }
        renderWidth *= relWidth
        renderHeight *= relHeight
        val pageRelativeBounds = RectF(relX, relY, relX + relWidth, relY + relHeight)

        if (renderWidth > 0 && renderHeight > 0) {
            if (!pdfView.cacheManager.upPartIfContained(cell.page, pageRelativeBounds, order, pdfView.zoom)) {
                pdfView.pageRenderer?.addRenderingTask(
                    cell.page,
                    RenderingSize(renderWidth, renderHeight, pageRelativeBounds),
                    false,
                    order,
                    pdfView.zoom,
                    pdfView.isBestQuality,
                    pdfView.isAnnotationRendering,
                    false
                )
            }
        }
    }

    private fun loadThumbnail(page: Int, isForPrinting: Boolean) {
        val pageSize = pdfFile.getPageSize(page)
        val thumbnailRatio = if (isForPrinting) Constants.THUMBNAIL_RATIO_PRINTING else pdfView.thumbnailRatio
        val thumbnailWidth = pageSize.width * thumbnailRatio
        val thumbnailHeight = pageSize.height * thumbnailRatio
        if (!pdfView.cacheManager.containsThumbnail(page, thumbnailRect)) {
            pdfView.pageRenderer?.addRenderingTask(
                page,
                RenderingSize(thumbnailWidth, thumbnailHeight, thumbnailRect),
                true,
                0,
                pdfView.zoom,
                pdfView.isBestQuality,
                pdfView.isAnnotationRendering,
                isForPrinting
            )
        } else if (page == pdfView.pageCount - 1 && isForPrinting) {
            pdfView.callbacks.callsOnReadyForPrinting(pdfView.pagesAsBitmaps)
        }
    }

    fun loadPages() {
        cacheOrder = 1
        xOffset = -MathUtils.max(pdfView.currentXOffset, 0f)
        yOffset = -MathUtils.max(pdfView.currentYOffset, 0f)

        loadVisible()
    }
}
