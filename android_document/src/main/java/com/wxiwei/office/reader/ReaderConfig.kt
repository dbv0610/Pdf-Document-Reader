package com.wxiwei.office.reader

import com.wxiwei.office.constant.wp.WPViewConstant

/** Options for [OfficeReader]; the defaults suit an embedded full-screen reader. */
data class ReaderConfig(
    /** Color behind the pages; null keeps the library default. */
    val backgroundColor: Int? = null,
    /** [WPViewConstant.PAGE_ROOT], [WPViewConstant.NORMAL_ROOT] or [WPViewConstant.PRINT_ROOT]. */
    val wordDefaultView: Byte = WPViewConstant.PAGE_ROOT.toByte(),
    /**
     * Gap between Word/TXT pages and around them in page view, in px at zoom 1 (it scales with
     * the zoom, like the pages); null keeps the library default of [WPViewConstant.PAGE_SPACE].
     */
    val wordPageSpacing: Int? = null,
    val showTxtEncodeDialog: Boolean = true,
    val txtDefaultEncode: String? = "GBK",
    val touchZoom: Boolean = true,
    /** Thumbnail size relative to the page at zoom 1. */
    val thumbnailScale: Float = 0.5f,
    /** Memory budget of the thumbnail cache; least recently used thumbnails are dropped past it. */
    val thumbnailCacheBytes: Int = defaultThumbnailCacheBytes(),
) {
    companion object {
        /** An eighth of the heap, capped at 64 MB. */
        fun defaultThumbnailCacheBytes(): Int =
            minOf(Runtime.getRuntime().maxMemory() / 8, 64L * 1024 * 1024).toInt()
    }
}
