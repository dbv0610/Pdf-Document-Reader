package com.reader.pdfviewer.util

object Constants {

    const val DEBUG_MODE = false

    /**
     * Between 0 and 1, the thumbnails quality (default 0.5). Increasing this value may cause performances decrease.
     * The thumbnail is what shows while the parts render, a sharper one makes the wait less visible.
     */
    const val THUMBNAIL_RATIO = 0.5f

    /**
     * Minimum quality for printing.
     */
    const val THUMBNAIL_RATIO_PRINTING = 0.75f

    /**
     * The size of the rendered parts (default 512).
     * Tinier : a little bit slower to have the whole page rendered but more reactive.
     * Bigger : user will have to wait longer to have the first visual results.
     * pdfium processes the whole page content for every part, so fewer bigger parts sharpen the page sooner.
     */
    const val PART_SIZE = 512.0f

    /**
     * Part of document above and below screen that should be preloaded, in dp.
     * Parts are rendered nearest to the screen center first, so preloading does not delay what is visible.
     */
    const val PRELOAD_OFFSET = 200

    object Cache {
        /**
         * The size of the cache (number of bitmaps kept).
         * Must stay above the parts needed to cover the screen and the preloaded area
         * (about 30 parts of [PART_SIZE] on a 1080x2000 screen with [PRELOAD_OFFSET]),
         * otherwise visible parts evict each other and are rendered again endlessly.
         */
        const val CACHE_SIZE = 56
        const val THUMBNAILS_CACHE_SIZE = 8
    }

    object Pinch {
        const val MAXIMUM_ZOOM = 100.0f
        const val MINIMUM_ZOOM = 0.3f
    }
}
