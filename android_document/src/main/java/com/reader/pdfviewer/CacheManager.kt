package com.reader.pdfviewer

import android.graphics.RectF
import com.reader.pdfviewer.model.PagePart
import com.reader.pdfviewer.util.Constants.Cache.CACHE_SIZE
import com.reader.pdfviewer.util.Constants.Cache.THUMBNAILS_CACHE_SIZE
import java.util.PriorityQueue

class CacheManager {
    private val passiveCache: PriorityQueue<PagePart>

    private val activeCache: PriorityQueue<PagePart>

    private val thumbnails: MutableList<PagePart>

    private val passiveActiveLock = Any()

    private val orderComparator = PagePartComparator()

    init {
        activeCache = PriorityQueue<PagePart>(CACHE_SIZE, orderComparator)
        passiveCache = PriorityQueue<PagePart>(CACHE_SIZE, orderComparator)
        thumbnails = ArrayList<PagePart>()
    }

    fun cachePart(part: PagePart) {
        synchronized(passiveActiveLock) {
            removePartsAtSameLocation(passiveCache, part)
            removePartsAtSameLocation(activeCache, part)

            // If cache too big, remove and recycle
            makeAFreeSpace()

            // Then add part
            activeCache.offer(part)
        }
    }

    fun makeANewSet() {
        synchronized(passiveActiveLock) {
            passiveCache.addAll(activeCache)
            activeCache.clear()
        }
    }

    private fun makeAFreeSpace() {
        synchronized(passiveActiveLock) {
            while ((activeCache.size + passiveCache.size) >= CACHE_SIZE && !passiveCache.isEmpty()) {
                recycleBitmapsFromPart(passiveCache)
            }
            while ((activeCache.size + passiveCache.size) >= CACHE_SIZE && !activeCache.isEmpty()) {
                recycleBitmapsFromPart(activeCache)
            }
        }
    }

    fun cacheThumbnail(part: PagePart, isForPrinting: Boolean) {
        synchronized(thumbnails) {
            val iterator = thumbnails.iterator()
            while (iterator.hasNext()) {
                val old = iterator.next()
                if (old == part) {
                    iterator.remove()
                    if (old !== part) old.renderedBitmap?.recycle()
                }
            }
            // If cache too big, remove and recycle. But if we're printing, we don't want any limit.
            while (!isForPrinting && thumbnails.size >= THUMBNAILS_CACHE_SIZE) {
                thumbnails.removeAt(0).renderedBitmap?.recycle()
            }

            // Then add thumbnail
            addWithoutDuplicates(thumbnails, part)
        }
    }

    fun upPartIfContained(page: Int, pageRelativeBounds: RectF?, toOrder: Int, renderingZoom: Float): Boolean {
        val fakePart = PagePart(page, null, pageRelativeBounds, false, 0)

        synchronized(passiveActiveLock) {
            val found = find(passiveCache, fakePart, renderingZoom)
            if (found != null) {
                passiveCache.remove(found)
                found.cacheOrder = toOrder
                activeCache.offer(found)
                return true
            }
            return find(activeCache, fakePart, renderingZoom) != null
        }
    }

    /**
     * Return true if already contains the described PagePart
     */
    fun containsThumbnail(page: Int, pageRelativeBounds: RectF?): Boolean {
        val fakePart = PagePart(page, null, pageRelativeBounds, true, 0)
        synchronized(thumbnails) {
            for (part in thumbnails) {
                if (!part.stale && part == fakePart) {
                    return true
                }
            }
            return false
        }
    }

    private fun recycleBitmapsFromPart(cache: PriorityQueue<PagePart>) {
        cache.poll()?.renderedBitmap?.recycle()
    }

    /**
     * Keep the bitmaps of [page] rendered at [zoom] visible until freshly rendered parts replace them.
     * Parts of other zoom levels would never be replaced, they are dropped.
     */
    fun markPageStale(page: Int, zoom: Float) {
        synchronized(passiveActiveLock) {
            markStale(passiveCache, page, zoom)
            markStale(activeCache, page, zoom)
        }
        synchronized(thumbnails) {
            for (part in thumbnails) if (part.page == page) part.stale = true
        }
    }

    private fun markStale(cache: PriorityQueue<PagePart>, page: Int, zoom: Float) {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val part = iterator.next()
            if (part.page != page) {
                continue
            }
            if (part.renderingZoom.compareTo(zoom) == 0) {
                part.stale = true
            } else {
                iterator.remove()
                part.renderedBitmap?.recycle()
            }
        }
    }

    /**
     * Drop every rendered bitmap of [page] so it is rendered again
     */
    fun removePage(page: Int) {
        synchronized(passiveActiveLock) {
            removePageParts(passiveCache, page)
            removePageParts(activeCache, page)
        }
        synchronized(thumbnails) {
            val iterator = thumbnails.iterator()
            while (iterator.hasNext()) {
                val part = iterator.next()
                if (part.page == page) {
                    iterator.remove()
                    part.renderedBitmap?.recycle()
                }
            }
        }
    }

    private fun removePageParts(cache: PriorityQueue<PagePart>, page: Int) {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val part = iterator.next()
            if (part.page == page) {
                iterator.remove()
                part.renderedBitmap?.recycle()
            }
        }
    }

    private fun removePartsAtSameLocation(cache: PriorityQueue<PagePart>, newPart: PagePart) {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val existingPart = iterator.next()
            if (existingPart !== newPart && existingPart == newPart) {
                iterator.remove()
                existingPart.renderedBitmap?.recycle()
            }
        }
    }

    /**
     * Add part if it doesn't exist, recycle bitmap otherwise
     */
    private fun addWithoutDuplicates(collection: MutableCollection<PagePart>, newPart: PagePart) {
        for (part in collection) {
            if (part == newPart) {
                newPart.renderedBitmap?.recycle()
                return
            }
        }
        collection.add(newPart)
    }

    val pageParts: MutableList<PagePart>
        get() {
            synchronized(passiveActiveLock) {
                val parts: MutableList<PagePart> = ArrayList<PagePart>(passiveCache)
                parts.addAll(activeCache)
                return parts
            }
        }

    fun getThumbnails(): MutableList<PagePart> {
        synchronized(thumbnails) {
            return thumbnails
        }
    }

    fun recycle() {
        synchronized(passiveActiveLock) {
            for (part in passiveCache) {
                part.renderedBitmap?.recycle()
            }
            passiveCache.clear()
            for (part in activeCache) {
                part.renderedBitmap?.recycle()
            }
            activeCache.clear()
        }
        synchronized(thumbnails) {
            for (part in thumbnails) {
                part.renderedBitmap?.recycle()
            }
            thumbnails.clear()
        }
    }

    internal class PagePartComparator : Comparator<PagePart> {
        override fun compare(part1: PagePart, part2: PagePart): Int {
            return part1.cacheOrder.compareTo(part2.cacheOrder)
        }
    }

    companion object {
        private fun find(vector: PriorityQueue<PagePart>, fakePart: PagePart?): PagePart? {
            for (part in vector) {
                if (!part.stale && part == fakePart) {
                    return part
                }
            }
            return null
        }

        private fun find(vector: PriorityQueue<PagePart>, fakePart: PagePart?, renderingZoom: Float): PagePart? {
            for (part in vector) {
                if (!part.stale && part == fakePart && part.renderingZoom.compareTo(renderingZoom) == 0) {
                    return part
                }
            }
            return null
        }
    }
}
