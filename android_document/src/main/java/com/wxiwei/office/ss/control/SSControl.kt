package com.wxiwei.office.ss.control

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.ClipboardManager
import android.view.View
import com.wxiwei.office.common.ICustomDialog
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.util.ReferenceUtil
import com.wxiwei.office.system.AbstractControl
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.DocumentCoroutines
import com.wxiwei.office.system.OpenTrace
import com.wxiwei.office.system.IFind
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.SysKit
import com.wxiwei.office.system.activity
import com.wxiwei.office.system.mainFrame
import com.wxiwei.office.system.reader
import com.wxiwei.office.system.sysKit
import com.wxiwei.office.system.isAutoTest
import com.wxiwei.office.system.officeToPicture
import com.wxiwei.office.system.customDialog
import java.util.Vector

class SSControl(mainControl: IControl?, book: Workbook?, filepath: String?) : AbstractControl() {
    private var mainControl: IControl? = mainControl
    private var isDispose = false
    private var spreadSheet: Spreadsheet?
    private var excelView: ExcelView?

    init {
        excelView = ExcelView(getMainFrame()!!.activity, filepath, book, this)
        spreadSheet = excelView!!.getSpreadsheet()
    }

    override fun canBackLayout(): Boolean = false
    override fun setLayoutThreadDied(isDied: Boolean) {}
    override fun setStopDraw(isStopDraw: Boolean) {}
    override fun layoutView(x: Int, y: Int, w: Int, h: Int) {}

    override fun actionEvent(actionID: Int, obj: Any?) {
        val sheet = spreadSheet ?: return
        when (actionID) {
            EventConstant.SYS_SET_PROGRESS_BAR_ID -> if (sheet.parent != null) {
                // TODO(coroutine): marshal progress updates onto the view thread; use Dispatchers.Main.
                sheet.post {
                    if (!isDispose) mainControl!!.mainFrame.showProgressBar(obj as Boolean)
                }
            }
            EventConstant.SYS_VECTORGRAPH_PROGRESS -> {
                val update = Runnable { if (!isDispose) mainControl!!.mainFrame.updateViewImages(obj as List<Int>) }
                if (sheet.parent != null) sheet.post(update) else DocumentCoroutines.launch(mainControl, update)
            }
            EventConstant.SYS_INIT_ID -> excelView!!.init()
            EventConstant.SS_SHOW_SHEET -> excelView!!.showSheet(obj as Int)
            EventConstant.TEST_REPAINT_ID -> sheet.postInvalidate()
            EventConstant.SS_SHEET_CHANGE -> Unit
            EventConstant.APP_ZOOM_ID -> {
                val params = obj as IntArray
                sheet.setZoom(params[0] / MainConstant.STANDARD_RATE.toFloat())
                sheet.post {
                    if (!isDispose) {
                        mainFrame!!.changeZoom()
                        updateStatus()
                    }
                }
            }
            EventConstant.APP_CONTENT_SELECTED -> updateStatus()
            EventConstant.FILE_COPY_ID -> {
                val clip = mainFrame!!.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip.text = sheet.getActiveCellContent()
            }
            EventConstant.APP_HYPERLINK -> {
                val hyperlink = sheet.getActiveCellHyperlink()
                if (hyperlink != null) {
                    try {
                        if (hyperlink.getLinkType() == Hyperlink.LINK_DOCUMENT) {
                            val addr = hyperlink.getAddress()
                            val index = addr.indexOf("!")
                            val sheetName = addr.substring(0, index).replace("'", "")
                            val ref = addr.substring(index + 1)
                            var rowIndex = ReferenceUtil.instance().getRowIndex(ref)
                            var columnIndex = ReferenceUtil.instance().getColumnIndex(ref)
                            val target = sheet.getWorkbook()!!.getSheet(sheetName)
                            target!!.setActiveCellRowCol(rowIndex, columnIndex)
                            excelView!!.showSheet(sheetName)
                            rowIndex -= 1
                            columnIndex -= 1
                            sheet.getSheetView()!!.goToCell(if (rowIndex >= 0) rowIndex else 0, if (columnIndex >= 0) columnIndex else 0)
                            mainFrame!!.doActionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null)
                            sheet.postInvalidate()
                        } else if (hyperlink.getLinkType() == Hyperlink.LINK_EMAIL || hyperlink.getLinkType() == Hyperlink.LINK_URL) {
                            mainFrame!!.activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(hyperlink.getAddress())))
                        } else {
                            mainControl!!.actionEvent(EventConstant.SYS_SHOW_TOOLTIP, "not supported hyperlink!")
                        }
                    } catch (e: Exception) {
                        OpenTrace.e("Excel hyperlink action failed", e)
                    }
                }
            }
            EventConstant.APP_INTERNET_SEARCH_ID -> sysKit!!.internetSearch(sheet.getActiveCellContent() ?: "", mainFrame!!.activity)
            EventConstant.SYS_AUTO_TEST_FINISH_ID -> if (mainControl!!.isAutoTest) mainFrame!!.activity.onBackPressed()
            EventConstant.APP_GENERATED_PICTURE_ID -> exportImage()
            EventConstant.APP_ABORTREADING -> mainControl!!.reader?.abortReader()
            EventConstant.APP_PAGE_UP_ID -> if (sheet.getEventManage() != null) {
                sheet.getEventManage()!!.onScroll(null as android.view.MotionEvent?, null as android.view.MotionEvent, 0f, -sheet.height + 10f)
                exportImage()
                sheet.post { if (!isDispose) updateStatus() }
            }
            EventConstant.APP_PAGE_DOWN_ID -> if (sheet.getEventManage() != null) {
                sheet.getEventManage()!!.onScroll(null as android.view.MotionEvent?, null as android.view.MotionEvent, 0f, sheet.height - 10f)
                exportImage()
                sheet.post { if (!isDispose) updateStatus() }
            }
            EventConstant.SS_REMOVE_SHEET_BAR -> excelView!!.removeSheetBar()
            EventConstant.APP_INIT_CALLOUTVIEW_ID -> sheet.initCalloutView()
        }
    }

    override fun getActionValue(actionID: Int, obj: Any?): Any? {
        val sheet = spreadSheet ?: return null
        return when (actionID) {
            EventConstant.APP_ZOOM_ID -> sheet.getZoom()
            EventConstant.APP_FIT_ZOOM_ID -> sheet.getFitZoom()
            EventConstant.APP_COUNT_PAGES_ID -> sheet.getSheetCount()
            EventConstant.APP_CURRENT_PAGE_NUMBER_ID -> sheet.getCurrentSheetNumber()
            EventConstant.SS_GET_ALL_SHEET_NAME -> {
                val vec = Vector<String>()
                val book = sheet.getWorkbook()!!
                for (i in 0 until book.getSheetCount()) vec.add(book.getSheet(i)!!.getSheetName())
                vec
            }
            EventConstant.SS_GET_SHEET_NAME -> {
                val number = obj as Int
                if (number == -1) null else sheet.getWorkbook()!!.getSheet(number - 1)?.getSheetName()
            }
            EventConstant.APP_THUMBNAIL_ID -> if (obj is IntArray && obj.size == 3) {
                sheet.getThumbnail(obj[0], obj[1], obj[2] / MainConstant.STANDARD_RATE.toFloat())
            } else null
            EventConstant.APP_GET_SNAPSHOT_ID -> sheet.getSnapshot(obj as Bitmap)
            else -> null
        }
    }

    private fun updateStatus() {
        spreadSheet?.post { if (!isDispose) mainFrame!!.updateToolsbarStatus() }
    }

    private fun exportImage() {
        spreadSheet?.post { if (!isDispose) spreadSheet!!.createPicture() }
    }

    override fun getCurrentViewIndex(): Int = excelView?.getCurrentViewIndex() ?: -1
    override fun getView(): View = excelView!!
    override fun getDialog(activity: Activity?, id: Int): Dialog? = null
    override fun getMainFrame(): IMainFrame = mainControl!!.getMainFrame()
    override fun getActivity(): Activity = mainControl!!.getMainFrame().getActivity()
    override fun getFind(): IFind = spreadSheet!!
    override fun isAutoTest(): Boolean = mainControl?.isAutoTest() ?: false
    override fun getOfficeToPicture(): IOfficeToPicture? = mainControl?.getOfficeToPicture()
    override fun getCustomDialog(): ICustomDialog? = mainControl?.getCustomDialog()
    override fun getApplicationType(): Byte = MainConstant.APPLICATION_TYPE_SS
    override fun getSysKit(): SysKit = mainControl!!.getSysKit()
    override fun isEndFile(): Boolean = false

    override fun dispose() {
        isDispose = true
        mainControl = null
        spreadSheet = null
        if (excelView != null) {
            excelView!!.dispose()
            excelView = null
        }
    }
}
