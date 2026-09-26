package com.wxiwei.office.system

import com.wxiwei.office.constant.wp.WPViewConstant

/**
 * Snapshot of a Word/TXT document once its layout completes, see [IMainFrame.completeLayout].
 *
 * @property pageNumber 1-based page on screen.
 * @property pageCount pages laid out.
 * @property pageWidth width of the first page in px at zoom 1.
 * @property pageHeight height of the first page in px at zoom 1.
 * @property zoom current zoom.
 * @property fitZoom zoom that fits a page to the view width.
 * @property viewMode [WPViewConstant.PAGE_ROOT], [WPViewConstant.NORMAL_ROOT] or [WPViewConstant.PRINT_ROOT].
 */
data class LayoutInfo(
    val pageNumber: Int,
    val pageCount: Int,
    val pageWidth: Int,
    val pageHeight: Int,
    val zoom: Float,
    val fitZoom: Float,
    val viewMode: Int,
) {
    val isPageMode: Boolean get() = viewMode == WPViewConstant.PAGE_ROOT.toInt()
}
