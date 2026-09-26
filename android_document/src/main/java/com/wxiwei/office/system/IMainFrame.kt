package com.wxiwei.office.system

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.res.ResKit
import com.wxiwei.office.system.beans.pagelist.IPageListViewListener
import java.io.File

interface IMainFrame {
    companion object {
        const val ON_TOUCH: Byte = 0
        const val ON_DOWN: Byte = 1
        const val ON_SHOW_PRESS: Byte = 2
        const val ON_SINGLE_TAP_UP: Byte = 3
        const val ON_SCROLL: Byte = 4
        const val ON_LONG_PRESS: Byte = 5
        const val ON_FLING: Byte = 6
        const val ON_SINGLE_TAP_CONFIRMED: Byte = 7
        const val ON_DOUBLE_TAP: Byte = 8
        const val ON_DOUBLE_TAP_EVENT: Byte = 9
        const val ON_CLICK: Byte = 10
    }

    fun getActivity(): Activity

    // Everything below has a default matching a plain embedded reader, so a host only
    // overrides what it needs (or uses com.wxiwei.office.reader.OfficeReader and none at all).

    /** Return true when the host handled [actionID]; the control then skips its own handling. */
    fun doActionEvent(actionID: Int, obj: Any?): Boolean = false

    /** The document view exists (`MainControl.getView()`); the host adds it to its layout here. */
    fun openFileFinish() = Unit
    fun updateToolsbarStatus() = Unit
    fun setFindBackForwardState(state: Boolean) = Unit
    fun getBottomBarHeight(): Int = 0
    fun getTopBarHeight(): Int = 0
    fun getAppName(): String = getActivity().applicationInfo.loadLabel(getActivity().packageManager).toString()
    fun getTemporaryDirectory(): File? = getActivity().getExternalFilesDir(null) ?: getActivity().filesDir
    fun onEventMethod(v: View?, e1: MotionEvent?, e2: MotionEvent?, xValue: Float, yValue: Float, eventMethodType: Byte): Boolean = false
    fun isDrawPageNumber(): Boolean = false
    fun isShowZoomingMsg(): Boolean = false
    fun isPopUpErrorDlg(): Boolean = true
    fun isShowPasswordDlg(): Boolean = true
    fun isShowProgressBar(): Boolean = false
    fun isShowFindDlg(): Boolean = true
    fun isShowTXTEncodeDlg(): Boolean = true
    fun getTXTDefaultEncode(): String? = "GBK"
    fun isTouchZoom(): Boolean = true
    fun isZoomAfterLayoutForWord(): Boolean = true
    fun getWordDefaultView(): Byte = WPViewConstant.PAGE_ROOT.toByte()

    /**
     * Gap between Word pages and around them in page view, in px at zoom 1 (it scales with the
     * zoom, like the pages). Read when the pages are laid out.
     */
    fun getWordPageSpacing(): Int = WPViewConstant.PAGE_SPACE.toInt()
    fun getLocalString(resName: String): String? = ResKit.instance().getLocalString(resName)
    fun changeZoom() = Unit

    /**
     * The visible page changed. [pageNumber] is 1-based: the Word page on screen, the focused
     * slide or the selected sheet; [pageCount] is how many are loaded so far.
     */
    fun changePage(pageNumber: Int, pageCount: Int) = Unit

    /** Word/TXT finished laying out; [info] is read when this is called, on the main thread. */
    fun completeLayout(info: LayoutInfo) = Unit
    fun error(errorCode: Int) = Unit
    fun fullScreen(fullscreen: Boolean) = Unit
    fun showProgressBar(visible: Boolean) = Unit
    fun updateViewImages(viewList: List<Int?>) = Unit
    fun isChangePage(): Boolean = true
    fun setWriteLog(saveLog: Boolean) = Unit
    fun isWriteLog(): Boolean = false
    fun setThumbnail(isThumbnail: Boolean) = Unit

    /** True lays Word out in one pass on the main thread instead of in the background. */
    fun isThumbnail(): Boolean = false

    /** An Int color or a Drawable for the document view; null keeps the view's own. */
    fun getViewBackground(): Any? = null
    fun setIgnoreOriginalSize(ignoreOriginalSize: Boolean) = Unit
    fun isIgnoreOriginalSize(): Boolean = false
    fun getPageListViewMovingPosition(): Byte = IPageListViewListener.Moving_Vertical
    fun dispose() = Unit
}

val IMainFrame.activity: Activity
    get() = getActivity()
val IMainFrame.isDrawPageNumber: Boolean
    get() = isDrawPageNumber()
val IMainFrame.isShowZoomingMsg: Boolean
    get() = isShowZoomingMsg()
val IMainFrame.isPopUpErrorDlg: Boolean
    get() = isPopUpErrorDlg()
val IMainFrame.isShowPasswordDlg: Boolean
    get() = isShowPasswordDlg()
val IMainFrame.isShowProgressBar: Boolean
    get() = isShowProgressBar()
val IMainFrame.isShowFindDlg: Boolean
    get() = isShowFindDlg()
val IMainFrame.isShowTXTEncodeDlg: Boolean
    get() = isShowTXTEncodeDlg()
val IMainFrame.isTouchZoom: Boolean
    get() = isTouchZoom()
val IMainFrame.isZoomAfterLayoutForWord: Boolean
    get() = isZoomAfterLayoutForWord()
val IMainFrame.isChangePage: Boolean
    get() = isChangePage()
val IMainFrame.isThumbnail: Boolean
    get() = isThumbnail()
val IMainFrame.viewBackground: Any?
    get() = getViewBackground()
val IMainFrame.isIgnoreOriginalSize: Boolean
    get() = isIgnoreOriginalSize()
val IMainFrame.pageListViewMovingPosition: Byte
    get() = getPageListViewMovingPosition()
