package com.wxiwei.office.ss.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import com.wxiwei.office.ss.model.baseModel.Sheet
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Caches the cell area of a sheet as bitmap tiles so scrolling only blits bitmaps instead of
 * re-laying-out and drawing every visible cell each frame (~650 cells, 30-40 ms per frame).
 *
 * Tiles are aligned to sheet units (zoom 1 pixels), so a tile rendered at scroll origin
 * (i * tileUnits, j * tileUnits) lines up exactly with its neighbours. Tiles are rendered within
 * a per-frame budget (at least one per frame), visible ones first, then rings around the
 * viewport; a tile not rendered yet stays blank for a frame or two during a fast fling. Everything runs on the UI thread under the
 * SheetView monitor, so it shares the renderer without any threading changes.
 */
internal class SheetTileCache(private val view: SheetView) {

    private class Tile(val bitmap: Bitmap, var version: Int)

    private val tiles = LinkedHashMap<Long, Tile>(64, 0.75f, true)
    private var cachedSheet: Sheet? = null
    private var cachedZoom = 0f
    private var tileUnits = 256
    private var tilePx = 512
    private var padUnits = 2
    private var version = 0
    private var contentSignature = Long.MIN_VALUE
    private var bytes = 0L

    /** Drop tile contents whose geometry or data changed (row/column resize, reload). */
    fun invalidate() {
        version++
    }

    fun clear() {
        tiles.values.forEach { it.bitmap.recycle() }
        tiles.clear()
        bytes = 0
    }

    /**
     * Draws the cell area from tiles. The canvas must already be clipped to the cell area.
     * @return true when more tiles should be rendered on a following frame
     */
    fun draw(
        canvas: Canvas, sheet: Sheet, zoom: Float, scrollX: Int, scrollY: Int,
        left: Float, top: Float, right: Int, bottom: Int
    ): Boolean {
        val frameStart = SystemClock.uptimeMillis()
        if (sheet !== cachedSheet || zoom != cachedZoom) {
            clear()
            cachedSheet = sheet
            cachedZoom = zoom
            tileUnits = maxOf(MIN_TILE_UNITS, Math.round(TARGET_TILE_PX / zoom))
            // render a few pixels past each edge and show only the core: cell fills and borders
            // at a tile's own edge are drawn inconsistently (1px seams)
            padUnits = maxOf(1, ceil(PAD_PX / zoom).toInt())
            tilePx = ceil((tileUnits + 2 * padUnits) * zoom).toInt() + 1
        }
        val signature = sheet.getLastRowNum().toLong() * 31 + sheet.getState() +
            (if (sheet.isAccomplished()) 1L shl 40 else 0L)
        if (signature != contentSignature) {
            contentSignature = signature
            version++
        }

        val viewUnitsW = (right - left) / zoom
        val viewUnitsH = (bottom - top) / zoom
        val i0 = floor(scrollX.toFloat() / tileUnits).toInt()
        val i1 = floor((scrollX + viewUnitsW) / tileUnits).toInt()
        val j0 = floor(scrollY.toFloat() / tileUnits).toInt()
        val j1 = floor((scrollY + viewUnitsH) / tileUnits).toInt()

        var pending = false
        var rendered = 0
        for (j in j0..j1) {
            for (i in i0..i1) {
                var tile = tiles[key(i, j)]
                // at least one tile per frame so the screen always fills in; the rest waits for
                // the next frame instead of stalling this one
                val canRender = rendered == 0 || withinBudget(frameStart)
                if (tile == null) {
                    if (canRender) {
                        tile = render(i, j)
                        rendered++
                    } else {
                        pending = true
                        continue
                    }
                } else if (tile.version != version) {
                    if (canRender) {
                        rerender(tile, i, j)
                        rendered++
                    } else {
                        pending = true
                    }
                }
                val x = left + (i * tileUnits - scrollX) * zoom
                val y = top + (j * tileUnits - scrollY) * zoom
                val save = canvas.save()
                canvas.clipRect(x, y, x + tileUnits * zoom, y + tileUnits * zoom)
                canvas.drawBitmap(
                    tile.bitmap,
                    x - (i * tileUnits - originX(i)) * zoom,
                    y - (j * tileUnits - originX(j)) * zoom,
                    null
                )
                canvas.restoreToCount(save)
            }
        }

        // prefetch rings around the viewport
        loop@ for (j in j0 - PREFETCH..j1 + PREFETCH) {
            for (i in i0 - PREFETCH..i1 + PREFETCH) {
                if (i < 0 || j < 0 || (i in i0..i1 && j in j0..j1)) continue
                val tile = tiles[key(i, j)]
                if (tile != null && tile.version == version) continue
                if (!withinBudget(frameStart)) {
                    pending = true
                    break@loop
                }
                if (tile == null) render(i, j) else rerender(tile, i, j)
            }
        }
        trim(i0, i1, j0, j1)
        return pending
    }

    private fun withinBudget(frameStart: Long) =
        SystemClock.uptimeMillis() - frameStart < FRAME_BUDGET_MS

    private fun render(i: Int, j: Int): Tile {
        val bitmap = Bitmap.createBitmap(tilePx, tilePx, Bitmap.Config.ARGB_8888)
        val tile = Tile(bitmap, version)
        rerender(tile, i, j)
        tiles[key(i, j)] = tile
        bytes += bitmap.byteCount
        return tile
    }

    private fun rerender(tile: Tile, i: Int, j: Int) {
        // transparent, not white: the grid lines of empty cells are drawn by the row/column
        // headers underneath the cell area and must show through, as in live drawing
        tile.bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(tile.bitmap)
        view.renderTile(canvas, originX(i), originX(j), tilePx, tilePx)
        tile.version = version
    }

    /** Evict least recently used tiles, never the visible ones. */
    private fun trim(i0: Int, i1: Int, j0: Int, j1: Int) {
        if (bytes <= MAX_BYTES) return
        val iter = tiles.entries.iterator()
        while (bytes > MAX_BYTES && iter.hasNext()) {
            val entry = iter.next()
            val i = (entry.key shr 32).toInt()
            val j = entry.key.toInt()
            if (i in i0..i1 && j in j0..j1) continue
            bytes -= entry.value.bitmap.byteCount
            entry.value.bitmap.recycle()
            iter.remove()
        }
    }

    /** Sheet position where tile [index]'s bitmap starts: the tile start minus the padding. */
    private fun originX(index: Int) = maxOf(0, index * tileUnits - padUnits)

    private fun key(i: Int, j: Int) = (i.toLong() shl 32) or (j.toLong() and 0xffffffffL)

    companion object {
        private const val TARGET_TILE_PX = 256f
        private const val MIN_TILE_UNITS = 16
        private const val PAD_PX = 4f
        private const val PREFETCH = 2
        private const val FRAME_BUDGET_MS = 6L
        private const val MAX_BYTES = 48L * 1024 * 1024
    }
}
