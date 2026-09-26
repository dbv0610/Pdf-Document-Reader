package com.reader.pdfviewer.pdfium

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.ArrayMap

class PdfDocument  /*package*/
internal constructor() {
    class Meta {
        var title: String? = null
        var author: String? = null
        var subject: String? = null
        var keywords: String? = null
        var creator: String? = null
        var producer: String? = null
        var creationDate: String? = null
        var modDate: String? = null
    }

    class Bookmark {
        val children: MutableList<Bookmark?> = ArrayList<Bookmark?>()
        var title: String? = null
        var pageIdx: Long = 0
        var mNativePtr: Long = 0

        fun hasChildren(): Boolean {
            return !children.isEmpty()
        }
    }

    class Link(val bounds: RectF?, val destPageIdx: Int?, val uri: String?)

    /*package*/
    var mNativeDocPtr: Long = 0
    /*package*/
    var parcelFileDescriptor: ParcelFileDescriptor? = null

    /*package*/
    val mNativePagesPtr: MutableMap<Int?, Long?> = ArrayMap<Int?, Long?>()

    /*package*/
    val mNativeTextPagesPtr: MutableMap<Int?, Long?> = ArrayMap<Int?, Long?>()

    /**
     * Char boxes of the text pages in [mNativeTextPagesPtr], 4 floats per character. A text page is a
     * snapshot of the page text, so its boxes never change; they are dropped with it. Only the pages
     * used last are kept, as a search can touch every page of a large document.
     */
    internal val mTextCharBoxes: MutableMap<Int, FloatArray> =
        object : LinkedHashMap<Int, FloatArray>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, FloatArray>): Boolean =
                size > MAX_CACHED_CHAR_BOX_PAGES
        }

    fun hasPage(index: Int): Boolean {
        return mNativePagesPtr.containsKey(index)
    }

    private companion object {
        const val MAX_CACHED_CHAR_BOX_PAGES = 8
    }
}
