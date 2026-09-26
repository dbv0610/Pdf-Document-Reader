package com.wxiwei.office.wp.control

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.ClipboardManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.bookmark.Bookmark
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.constant.DialogConstant
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.system.AbstractControl
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.IFind
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.SysKit
import com.wxiwei.office.wp.dialog.TXTEncodingDialog
import java.util.Vector

class WPControl(private var mainControl: IControl?, doc: IDocument, filePath: String) : AbstractControl() {
    private var isDispose = false
    private var wpView: Word? = Word(mainControl!!.getMainFrame().getActivity().applicationContext, doc, filePath, this)

    override fun setStopDraw(isStopDraw: Boolean) {
        Log.e("WPControl", "setStopDraw = $isStopDraw")
        wpView?.setStopDraw(isStopDraw)
    }

    override fun layoutView(x: Int, y: Int, w: Int, h: Int) {
    }

    override fun actionEvent(actionID: Int, obj: Any?) {
        val wpView = wpView ?: return
        val mainControl = mainControl ?: return
        when (actionID) {
            EventConstant.WP_SHOW_PAGE -> {
                wpView.showPage(obj as Int, EventConstant.WP_SHOW_PAGE)
                if (wpView.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) return
                updateStatus()
                exportImage()
            }

            EventConstant.SYS_SET_PROGRESS_BAR_ID -> {
                if (wpView.parent != null) {
                    wpView.post {
                        if (!isDispose) {
                            mainControl.getMainFrame().showProgressBar(obj as Boolean)
                        }
                    }
                }
            }

            EventConstant.SYS_VECTORGRAPH_PROGRESS -> {
                if (wpView.parent != null) {
                    wpView.post {
                        if (!isDispose) {
                            @Suppress("UNCHECKED_CAST")
                            mainControl.getMainFrame().updateViewImages(obj as List<Int>)
                        }
                    }
                } else {
                    DocumentCoroutines.launchSuspend(mainControl) {
                        if (!isDispose) {
                            @Suppress("UNCHECKED_CAST")
                            mainControl.getMainFrame().updateViewImages(obj as List<Int>)
                        }
                    }
                }
            }

            EventConstant.SYS_INIT_ID -> {
                android.util.Log.d("OfficeWPControl", "SYS_INIT_ID view=${wpView.javaClass.name}")
                wpView.init()
                android.util.Log.d("OfficeWPControl", "SYS_INIT_ID completed")
            }
            EventConstant.TEST_REPAINT_ID -> wpView.postInvalidate()
            EventConstant.WP_PRINT_MODE -> {
                if (wpView.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) return
                wpView.switchView(WPViewConstant.PRINT_ROOT.toInt())
                updateStatus()
            }

            EventConstant.WP_SWITCH_VIEW -> {
                val rootType = if (obj != null) {
                    obj as Int
                } else {
                    val current = wpView.getCurrentRootType()
                    if (current == WPViewConstant.PAGE_ROOT.toInt()) WPViewConstant.NORMAL_ROOT.toInt() else WPViewConstant.PAGE_ROOT.toInt()
                }
                wpView.switchView(rootType)
                updateStatus()
                if (rootType != WPViewConstant.PRINT_ROOT.toInt()) {
                    exportImage()
                }
            }

            EventConstant.APP_ZOOM_ID -> {
                val params = obj as IntArray
                wpView.setZoom(params[0] / MainConstant.STANDARD_RATE.toFloat(), params[1], params[2])
                wpView.post {
                    if (!isDispose) {
                        getMainFrame().changeZoom()
                    }
                }
            }

            EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS -> updateStatus()
            EventConstant.WP_SELECT_TEXT_ID -> {
                wpView.getStatus().setSelectTextStatus(!wpView.getStatus().isSelectTextStatus())
            }

            EventConstant.APP_INTERNET_SEARCH_ID -> ControlKit.instance().internetSearch(wpView)
            EventConstant.FILE_COPY_ID -> {
                val clip = getActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip.text = wpView.getHighlight().getSelectText()
            }

            EventConstant.SYS_AUTO_TEST_FINISH_ID -> {
                wpView.post {
                    if (!isDispose) {
                        mainControl.getMainFrame().showProgressBar(false)
                    }
                }
                if (isAutoTest()) {
                    getMainFrame().getActivity().onBackPressed()
                }
            }

            EventConstant.APP_GENERATED_PICTURE_ID -> exportImage()
            EventConstant.APP_PAGE_UP_ID -> {
                if (wpView.getCurrentRootType() == WPViewConstant.NORMAL_ROOT.toInt()) {
                    scrollNormalView((-wpView.height + 10).toFloat())
                } else {
                    wpView.showPage(wpView.getCurrentPageNumber() - 2, EventConstant.APP_PAGE_UP_ID)
                }
                if (wpView.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) return
                updateStatus()
                exportImage()
            }

            EventConstant.APP_PAGE_DOWN_ID -> {
                if (wpView.getCurrentRootType() == WPViewConstant.NORMAL_ROOT.toInt()) {
                    scrollNormalView((wpView.height + 10).toFloat())
                } else {
                    wpView.showPage(wpView.getCurrentPageNumber(), EventConstant.APP_PAGE_DOWN_ID)
                }
                if (wpView.getCurrentRootType() == WPViewConstant.PRINT_ROOT.toInt()) return
                updateStatus()
                exportImage()
            }

            EventConstant.APP_HYPERLINK -> {
                val link = obj as Hyperlink?
                if (link != null) {
                    try {
                        if (link.getLinkType() == Hyperlink.LINK_BOOKMARK) {
                            val bm: Bookmark? = getSysKit().getBookmarkManage().getBookmark(link.getAddress())
                            if (bm != null) {
                                ControlKit.instance().gotoOffset(wpView, bm.getStart())
                            }
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.getAddress()))
                            getMainFrame().getActivity().startActivity(intent)
                        }
                    } catch (e: Exception) {
                        OpenTrace.e("Word hyperlink action failed", e)
                    }
                }
            }

            EventConstant.WP_LAYOUT_NORMAL_VIEW -> {
                if (wpView.getCurrentRootType() == WPViewConstant.NORMAL_ROOT.toInt()) {
                    wpView.setExportImageAfterZoom(true)
                    wpView.layoutNormal()
                }
            }

            EventConstant.WP_LAYOUT_COMPLETED -> {
                wpView.updateFieldText()
                if (wpView.parent == null) {
                    getMainFrame().completeLayout(wpView.layoutInfo())
                } else {
                    wpView.post { if (!isDispose) getMainFrame().completeLayout(wpView.layoutInfo()) }
                }
            }

            EventConstant.APP_SET_FIT_SIZE_ID -> wpView.setFitSize(obj as Int)
            EventConstant.APP_INIT_CALLOUTVIEW_ID -> {
                wpView.getPrintWord().getListView().getCurrentPageView().initCalloutView()
            }

            else -> {}
        }
    }

    private fun scrollNormalView(distanceY: Float) {
        val event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_MOVE, 0f, 0f, 0)
        try {
            wpView?.getEventManage()?.onScroll(null, event, 0f, distanceY)
        } finally {
            event.recycle()
        }
    }

    override fun getActionValue(actionID: Int, obj: Any?): Any? {
        val wpView = wpView ?: return null
        return when (actionID) {
            EventConstant.APP_ZOOM_ID -> wpView.getZoom()
            EventConstant.WP_SELECT_TEXT_ID -> wpView.getStatus().isSelectTextStatus()
            EventConstant.APP_FIT_ZOOM_ID -> wpView.getFitZoom()
            EventConstant.APP_COUNT_PAGES_ID -> wpView.getPageCount()
            EventConstant.APP_CURRENT_PAGE_NUMBER_ID -> wpView.getCurrentPageNumber()
            EventConstant.WP_PAGE_TO_IMAGE -> wpView.pageToImage(obj as Int)
            EventConstant.APP_PAGEAREA_TO_IMAGE -> {
                if (obj is IntArray && obj.size == 7) {
                    wpView.pageAreaToImage(obj[0], obj[1], obj[2], obj[3], obj[4], obj[5], obj[6])
                } else {
                    null
                }
            }

            EventConstant.APP_THUMBNAIL_ID -> {
                if (obj is Int) wpView.getThumbnail(obj / MainConstant.STANDARD_RATE.toFloat()) else null
            }

            EventConstant.WP_GET_PAGE_SIZE -> wpView.getPageSize(obj as Int - 1)
            EventConstant.WP_GET_VIEW_MODE -> wpView.getCurrentRootType()
            EventConstant.APP_GET_FIT_SIZE_STATE_ID -> wpView.getFitSizeState()
            EventConstant.APP_GET_SNAPSHOT_ID -> wpView.getSnapshot(obj as Bitmap)
            else -> null
        }
    }

    private fun exportImage() {
        val wpView = wpView ?: return
        wpView.post {
            if (!isDispose) {
                wpView.createPicture()
            }
        }
    }

    private fun updateStatus() {
        val wpView = wpView ?: return
        wpView.post {
            if (!isDispose) {
                getMainFrame().updateToolsbarStatus()
            }
        }
    }

    override fun getCurrentViewIndex(): Int = wpView?.getCurrentPageNumber() ?: -1

    override fun getView(): View = wpView!!

    override fun getDialog(activity: Activity?, id: Int): Dialog? {
        val wpView = wpView ?: return null
        when (id) {
            DialogConstant.ENCODING_DIALOG_ID -> {
                val vector = Vector<Any>()
                vector.add(wpView.getFilePath())
                TXTEncodingDialog(this, activity!!, wpView.getDialogAction(), vector, id).show()
            }

            else -> {}
        }
        return null
    }

    override fun getMainFrame(): IMainFrame = mainControl!!.getMainFrame()

    override fun getActivity(): Activity = getMainFrame().getActivity()

    override fun getFind(): IFind = wpView!!.getFind()

    override fun isAutoTest(): Boolean = mainControl!!.isAutoTest()

    override fun getOfficeToPicture(): IOfficeToPicture? = mainControl!!.getOfficeToPicture()

    override fun getCustomDialog(): ICustomDialog? = mainControl!!.getCustomDialog()

    override fun getApplicationType(): Byte = MainConstant.APPLICATION_TYPE_WP

    override fun getSysKit(): SysKit = mainControl!!.getSysKit()

    override fun canBackLayout(): Boolean = wpView?.canBackLayout() ?: false

    override fun setLayoutThreadDied(isDied: Boolean) {
        wpView?.setLayoutThreadDied(isDied)
    }

    override fun isEndFile(): Boolean = wpView?.isEndFile() ?: true

    fun jumpToPage(page: Int): Boolean = wpView?.jumpToPage(page) ?: false

    fun getPageCount(): Int = wpView?.getPageCount() ?: 0

    override fun dispose() {
        isDispose = true
        wpView?.dispose()
        wpView = null
        mainControl = null
    }
}
