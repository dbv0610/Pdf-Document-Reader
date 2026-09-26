package com.reader.pdfviewer.model

import android.graphics.Bitmap
import android.graphics.RectF

class PagePart @JvmOverloads constructor(
    val page: Int,
    val renderedBitmap: Bitmap?,
    val pageRelativeBounds: RectF?,
    val isThumbnail: Boolean,
    var cacheOrder: Int,
    val renderingZoom: Float = Float.NaN
) {
    /** Retained for display while a replacement is rendered. */
    var stale: Boolean = false

    internal var inkRevision: Long = 0

    override fun equals(obj: Any?): Boolean {
        if (obj !is PagePart) {
            return false
        }

        val part = obj
        return part.page == page && part.pageRelativeBounds?.left == pageRelativeBounds?.left
                && part.pageRelativeBounds?.right == pageRelativeBounds?.right && part.pageRelativeBounds?.top == pageRelativeBounds?.top && part.pageRelativeBounds?.bottom == pageRelativeBounds?.bottom
    }
}
