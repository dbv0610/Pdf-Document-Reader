package com.wxiwei.office.reader

import com.wxiwei.office.system.LayoutInfo
import com.wxiwei.office.system.OfficeFileType
import com.wxiwei.office.system.OpenFileException

/** Everything a screen needs to render an [OfficeReader]; collect [OfficeReader.state]. */
data class ReaderState(
    val status: Status = Status.Idle,
    val fileType: OfficeFileType? = null,
    /** The library asked for its loading indicator (opening, or re-opening a TXT with another encoding). */
    val isLoading: Boolean = false,
    /** 1-based page on screen, focused slide or selected sheet; 0 before the document shows. */
    val pageNumber: Int = 0,
    /** Pages/slides loaded so far; grows while Word lays out or PowerPoint loads in the background. */
    val pageCount: Int = 0,
    /**
     * Pages [OfficeReader.thumbnails] can draw: [pageCount] for paged Word/TXT and PowerPoint,
     * 0 for Excel and Word's continuous (NORMAL_ROOT) view, which has no pages.
     */
    val thumbnailCount: Int = 0,
    /** Set once Word/TXT finished laying out. */
    val layout: LayoutInfo? = null,
    val error: OpenFileException? = null,
) {
    enum class Status { Idle, Opening, Ready, Failed }
}
