/*
 * 文件名称:          AbstractMainFrame.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:08:07
 */
package com.wxiwei.office.macro

import android.app.Activity
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.res.ResKit
import com.wxiwei.office.system.LayoutInfo
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.beans.pagelist.IPageListViewListener
import java.io.File

/**
 *
 */
internal class MacroFrame(application: Application, activity: Activity) : IMainFrame {

    // support touch zoom in / zoom out
    private var isTouchZoom = true

    // is draw page number
    private var isDrawPageNumber = true

    //show message when zooming or nor
    private var showZoomingMsg = true

    //popup error dialog when throw err or not
    private var popupErrorDlg = true

    //show password dialog
    private var showPasswordDlg = true

    //show progress bar when parsing document
    private var showProgressbarDlg = true

    // show find dialog
    private var showFindDlg = true

    //show txt encode chosing dialog
    private var showTXTEncodeDlg = true

    // page size greater then view size do change page
    private var isChangePage = true

    //txt default encode
    private var txtDefalutEncode: String? = null

    // normal view, changed after zoom bend, you need to re-layout
    private var isZoomAfterLayoutForWord = true

    // Word application default view (Normal or Page)，0 = page view, 1 = normal view;
    private var wordDefaultView: Byte = 0

    //
    private var bottomBarHeight = 0

    //
    private var topBarHeight = 0

    // application name
    private var appName: String? = null

    //
    private var app: Application? = application

    //
    private var activity: Activity? = activity

    //
    private var touchEventListener: TouchEventListener? = null

    //
    private var updateStatusListener: UpdateStatusListener? = null

    //
    private var openFileFinishListener: OpenFileFinishListener? = null

    //
    private var errorListener: ErrorListener? = null

    //
    private var resI18N: MutableMap<String, Int>? = null

    //whether write log to temporary file
    private var writeLog = true

    //open file to get thumbnail, or not
    private var isThumbnail = false

    //view background
    private var bg: Any? = Color.GRAY

    //
    private var ignoreOriginalSize = false

    private var pageListViewMovingPosition = IPageListViewListener.Moving_Horizontal

    init {
        val resID = activity.application.applicationInfo.labelRes
        if (resID > 0) {
            this.appName = activity.resources.getString(resID)
        }
    }

    /**
     * get activity instance
     */
    override fun getActivity(): Activity {
        return activity!!
    }

    /**
     * added touch event listener
     */
    fun addTouchEventListener(listener: TouchEventListener?) {
        this.touchEventListener = listener
    }

    /**
     * added update status listener
     */
    fun addUpdateStatusListener(listener: UpdateStatusListener?) {
        this.updateStatusListener = listener
    }

    /**
     * added open file finish listener
     *
     * @param listener OpenFileFinishListener instance
     */
    fun addOpenFileFinishListener(listener: OpenFileFinishListener?) {
        this.openFileFinishListener = listener
    }

    /**
     * added error listener
     *
     * @param listener ErrorListener instance
     */
    fun addErrorListener(listener: ErrorListener?) {
        this.errorListener = listener
    }

    override fun getTemporaryDirectory(): File? {
        val activity = this.activity
        if (activity != null) {
            // Get path for the file on external storage.  If external
            // storage is not currently mounted this will fail.
            val file = activity.getExternalFilesDir(null)
            return file ?: activity.filesDir
        }

        return null
    }

    /**
     * event method, office engine dispatch
     *
     * @param       v             event source
     * @param       e1            MotionEvent instance
     * @param       e2            MotionEvent instance
     * @param       velocityX     x axis velocity
     * @param       velocityY     y axis velocity
     * @param       eventMethodType  event method
     */
    override fun onEventMethod(
        v: View?, e1: MotionEvent?, e2: MotionEvent?,
        velocityX: Float, velocityY: Float, eventMethodType: Byte
    ): Boolean {
        touchEventListener?.onEventMethod(v, e1, e2, velocityX, velocityY, eventMethodType)
        return false
    }

    /**
     * update tool bar status
     */
    override fun updateToolsbarStatus() {
        updateStatusListener?.updateStatus()
    }

    /**
     * callback this method after zoom change
     */
    override fun changeZoom() {
        updateStatusListener?.changeZoom()
    }

    /**
     * callback this method after zoom change
     */
    override fun changePage(pageNumber: Int, pageCount: Int) {
        updateStatusListener?.changePage(pageNumber, pageCount)
    }

    /**
     * callback this method after layout completed
     */
    override fun completeLayout(info: LayoutInfo) {
        updateStatusListener?.completeLayout(info)
    }

    /**
     * full screen, not show top tool bar
     */
    override fun fullScreen(fullscreen: Boolean) {
    }

    /**
     * (non-Javadoc)
     * @see IMainFrame#showProgressBar(boolean)
     */
    override fun showProgressBar(visible: Boolean) {
        if (showProgressbarDlg) {
            activity!!.setProgressBarIndeterminateVisibility(visible)
        }
    }

    /**
     * @param viewList
     */
    override fun updateViewImages(viewList: List<Int?>) {
        updateStatusListener?.updateViewImage(viewList.toTypedArray())
    }

    /**
     * do action
     *
     * @param actionID action ID
     *
     * @param obj acValue
     *
     * @return  True if the listener has consumed the event, false otherwise.
     */
    override fun doActionEvent(actionID: Int, obj: Any?): Boolean {
        return false
    }

    /**
     * reader file finish call this method
     */
    override fun openFileFinish() {
        app!!.openFileFinish()
        openFileFinishListener?.openFileFinish()
    }

    /**
     * when engine error occurred callback this method
     *
     * @param errorCode  error code
     */
    override fun error(errorCode: Int) {
        errorListener?.error(errorCode)
    }

    /**
     * when need destroy office engine instance callback this method
     */
    fun destroyEngine() {
        if (errorListener != null) {
            //errorListener.destroyEngine();
        }
    }

    /**
     * set the find back button and find forward button state
     *
     * @param state
     */
    override fun setFindBackForwardState(state: Boolean) {
    }

    /**
     * get bottom  bar height
     */
    override fun getBottomBarHeight(): Int {
        return this.bottomBarHeight
    }

    /**
     * set bottom bar height
     */
    fun setBottomBarHeight(value: Int) {
        this.bottomBarHeight = value
    }

    /**
     * get top bar height
     */
    override fun getTopBarHeight(): Int {
        return this.topBarHeight
    }

    /**
     * set top bar height
     */
    fun setTopBarHeight(value: Int) {
        this.topBarHeight = value
    }

    /**
     * get application name
     */
    override fun getAppName(): String {
        return appName ?: "wxiwei"
    }

    /**
     * set application name
     */
    fun setAppName(name: String?) {
        this.appName = name
    }

    /**
     * is support draw page number
     */
    override fun isDrawPageNumber(): Boolean {
        return this.isDrawPageNumber
    }

    /**
     * set is support draw page number?
     */
    fun setDrawPageNumber(value: Boolean) {
        this.isDrawPageNumber = value
    }

    /**
     * true: show message when zooming
     * false: not show message when zooming
     */
    override fun isShowZoomingMsg(): Boolean {
        return showZoomingMsg
    }

    /**
     * show or hide zooming message
     */
    fun setShowZoomingMsg(showZoomingMsg: Boolean) {
        this.showZoomingMsg = showZoomingMsg
    }

    /**
     * true: pop up dialog when throw err
     * false: not pop up dialog when throw err
     */
    override fun isPopUpErrorDlg(): Boolean {
        return popupErrorDlg
    }

    fun setShowPasswordDlg(showPasswordDlg: Boolean) {
        this.showPasswordDlg = showPasswordDlg
    }

    /**
     * show password dialog when parse file with password
     */
    override fun isShowPasswordDlg(): Boolean {
        return showPasswordDlg
    }

    fun setShowProgressBar(showProgressbarDlg: Boolean) {
        this.showProgressbarDlg = showProgressbarDlg
    }

    /**
     * show progress bar or not when parsing document
     */
    override fun isShowProgressBar(): Boolean {
        return showProgressbarDlg
    }

    fun setShowFindDlg(b: Boolean) {
        this.showFindDlg = b
    }

    override fun isShowFindDlg(): Boolean {
        return showFindDlg
    }

    fun setShowTXTEncodeDlg(showTXTEncodeDlg: Boolean) {
        this.showTXTEncodeDlg = showTXTEncodeDlg
    }

    /**
     * show txt encode dialog when parse txt file
     */
    override fun isShowTXTEncodeDlg(): Boolean {
        return showTXTEncodeDlg
    }

    /**
     * set txt default encode
     */
    fun setTXTDefaultEncode(encode: String?) {
        this.txtDefalutEncode = encode
    }

    /**
     * get txt default encode when not showing txt encode dialog
     * @return null if showing txt encode dialog
     */
    override fun getTXTDefaultEncode(): String? {
        if (!showTXTEncodeDlg) {
            return txtDefalutEncode
        }

        return null
    }

    /**
     * pop up error dialog or hot
     */
    fun setPopUpErrorDlg(popupErrorDlg: Boolean) {
        this.popupErrorDlg = popupErrorDlg
    }

    /**
     * is support zoom in / zoom out
     */
    override fun isTouchZoom(): Boolean {
        return this.isTouchZoom
    }

    /**
     * set is support zoom in / zoom out?
     */
    fun setTouchZoom(value: Boolean) {
        this.isTouchZoom = value
    }

    /**
     * normal view, changed after zoom bend, you need to re-layout
     */
    override fun isZoomAfterLayoutForWord(): Boolean {
        return this.isZoomAfterLayoutForWord
    }

    /**
     * set normal view, changed after zoom bend, you need to re-layout
     */
    fun setZoomAfterLayoutForWord(value: Boolean) {
        this.isZoomAfterLayoutForWord = value
    }

    /**
     * get word application default view (Normal or Page)
     *
     * @return 0, page view
     *          1，normal view;
     */
    override fun getWordDefaultView(): Byte {
        return wordDefaultView
    }

    /**
     * set word application default view (Normal or Page)
     */
    fun setWordDefaultView(value: Byte) {
        this.wordDefaultView = value
    }

    /**
     *  set change page flag, Only when effectively the PageSize greater than ViewSize.
     *  (for PPT, word print mode, PDF)
     */
    fun setChangePage(b: Boolean) {
        this.isChangePage = b
    }

    /**
     *  get change page flag, Only when effectively the PageSize greater than ViewSize.
     *  when page size greater then view size do change page
     */
    override fun isChangePage(): Boolean {
        return this.isChangePage
    }

    /**
     * get Internationalization resource
     *
     * @param resName Internationalization resource name
     *
     * @return  resource value
     */
    override fun getLocalString(resName: String): String? {
        var resI18N = this.resI18N
        if (resI18N == null) {
            resI18N = HashMap()
            this.resI18N = resI18N
            try {
                val className = activity!!.packageName
                // load "R$string"
                val cls = Class.forName("$className.R\$string")
                // get all fields
                val fields = cls.declaredFields
                var fieldName: String
                for (field in fields) {
                    fieldName = field.name.uppercase()
                    if (ResKit.instance().hasResName(fieldName)) {
                        resI18N[fieldName] = field.getInt(null)
                    }
                }
            } catch (e: Exception) {
            }
        }
        var str: String? = null
        val id = resI18N[resName]
        if (id != null) {
            str = activity!!.resources.getString(id)
        }
        if (str == null || str.isEmpty()) {
            str = ResKit.instance().getLocalString(resName)
        }
        return str
    }

    /**
     * added resource ID for internationalization
     *
     * @param   resName   resource name, The value must be the same as field name in the resource file
     * @param   resID     The value in the "R.java"
     */
    fun addI18NResID(resName: String, resID: Int) {
        if (resI18N == null) {
            resI18N = HashMap()
        }
        resI18N!![resName] = resID
    }

    override fun setWriteLog(saveLog: Boolean) {
        this.writeLog = saveLog
    }

    override fun isWriteLog(): Boolean {
        return writeLog
    }

    override fun setThumbnail(isThumbnail: Boolean) {
        this.isThumbnail = isThumbnail
    }

    override fun isThumbnail(): Boolean {
        return isThumbnail
    }

    /**
     * set view background
     */
    fun setViewBackground(bg: Any?) {
        this.bg = bg
    }

    override fun getViewBackground(): Any? {
        return bg
    }

    /**
     * set flag whether fitzoom can be larger than 100%, but be smaller than the max zoom
     */
    override fun setIgnoreOriginalSize(ignoreOriginalSize: Boolean) {
        this.ignoreOriginalSize = ignoreOriginalSize
    }

    /**
     * @return
     * true fitzoom may be larger than 100%, but be smaller than the max zoom
     * false fitzoom can not larger than 100%
     */
    override fun isIgnoreOriginalSize(): Boolean {
        return ignoreOriginalSize
    }

    /**
     * set page list view moving in horizontal or vertical
     */
    fun setPageListViewMovingPosition(position: Byte) {
        this.pageListViewMovingPosition = position
    }

    /**
     * page list view moving position
     */
    override fun getPageListViewMovingPosition(): Byte {
        return pageListViewMovingPosition
    }

    override fun dispose() {
        app = null
        activity = null
        updateStatusListener = null
        touchEventListener = null
        errorListener = null
        openFileFinishListener = null
        txtDefalutEncode = null
    }
}
