/*
 * 文件名称:          ExcelView.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:21:00
 */
package com.wxiwei.office.ss.control

import android.content.Context
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.ss.model.baseModel.Workbook
import com.wxiwei.office.ss.sheetbar.SheetBar
import com.wxiwei.office.ss.view.SheetView
import com.wxiwei.office.system.IControl

/**
 * excel engine component
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2013-2-18
 *
 * 负责人:          ljj8494
 *
 * 负责小组:
 */
class ExcelView(context: Context, filepath: String?, book: Workbook?, control: IControl?) : RelativeLayout(context) {
    //
    private var isDefaultSheetBar = true

    //
    private var ss: Spreadsheet? = null

    //
    private var bar: SheetBar? = null

    //
    private var control: IControl? = null

    /**
     *
     * @param context
     */
    init {
        this.control = control
        ss = Spreadsheet(context, filepath, book, control, this)

        addView(ss, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    /**
     *
     */
    fun init() {
        ss!!.init()
        initSheetbar()
    }

    /**
     *
     */
    private fun initSheetbar() {
        if (!isDefaultSheetBar) {
            return
        }
        bar = SheetBar(context, control, resources.displayMetrics.widthPixels)
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        addView(bar, params)
    }

    /**
     *
     */
    fun getSpreadsheet(): Spreadsheet? {
        return ss
    }

    /**
     * 显示指定的sheet
     *
     * @param sheetIndex 要显示的sheet名称
     */
    fun showSheet(sheetIndex: Int) {
        ss!!.showSheet(sheetIndex)
        //change focused button
        if (isDefaultSheetBar) {
            bar!!.setFocusSheetButton(sheetIndex)
        } else {
            control!!.getMainFrame().doActionEvent(EventConstant.SS_CHANGE_SHEET, sheetIndex)
        }
    }

    /**
     * 显示指定的sheet
     *
     * @param sheetName 要显示的sheet名称
     */
    fun showSheet(sheetName: String?) {
        ss!!.showSheet(sheetName)

        val sheet = ss!!.getWorkbook()!!.getSheet(sheetName)
        if (sheet == null) {
            return
        }
        val sheetIndex = ss!!.getWorkbook()!!.getSheetIndex(sheet)
        if (isDefaultSheetBar) {
            bar!!.setFocusSheetButton(sheetIndex)
        } else {
            control!!.getMainFrame().doActionEvent(EventConstant.SS_CHANGE_SHEET, sheetIndex)
        }
    }

    /**
     * 得到sheetView视图
     */
    fun getSheetView(): SheetView? {
        return ss!!.getSheetView()
    }

    /**
     *
     */
    fun removeSheetBar() {
        isDefaultSheetBar = false
        removeView(bar)
    }

    /**
     * get sheet bar height
     * @return
     */
    fun getBottomBarHeight(): Int {
        if (isDefaultSheetBar) {
            return bar!!.height
        } else {
            return control!!.getMainFrame().getBottomBarHeight()
        }
    }

    /**
     * current view index
     * @return
     */
    fun getCurrentViewIndex(): Int {
        return ss!!.getCurrentSheetNumber()
    }

    /**
     *
     */
    fun dispose() {
        control = null
        if (ss != null) {
            ss!!.dispose()
        }
        bar = null
    }
}
