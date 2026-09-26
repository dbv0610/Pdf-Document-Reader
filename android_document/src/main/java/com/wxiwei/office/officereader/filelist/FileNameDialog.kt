package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.wxiwei.office.R
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.beans.ADialog
import java.util.Vector

/**
 * get file name
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2011-12-13
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
open class FileNameDialog(
    control: IControl?,
    context: Context,
    action: IDialogAction?,
    model: Vector<Any>?,
    dialogID: Int,
    titleResID: Int
) : ADialog(control, context, action, model, dialogID, titleResID) {
    /**
     * 
     */
    fun init(context: Context) {
        val mWidth = getContext().getResources().getDisplayMetrics().widthPixels - MARGIN * 2
        //text viewt
        textView = TextView(context)
        textView!!.setGravity(Gravity.TOP)
        var params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.topMargin = GAP
        params.bottomMargin = GAP
        params.gravity = Gravity.CENTER
        dialogFrame!!.addView(textView, params)


        //edit file name
        editText = EditText(context)
        editText!!.setGravity(Gravity.TOP)
        editText!!.setSingleLine()
        params = LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = GAP
        params.rightMargin = GAP
        dialogFrame!!.addView(editText, params)


        //ok, cancel
        val linearLayoutBtn = LinearLayout(context)
        linearLayoutBtn.setGravity(Gravity.CENTER)
        linearLayoutBtn.setOrientation(LinearLayout.HORIZONTAL)
        params = LinearLayout.LayoutParams(mWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)

        ok = Button(context)
        ok!!.setText(R.string.ok)
        ok!!.setOnClickListener(this)
        ok!!.setEnabled(false)
        linearLayoutBtn.addView(ok, params)

        cancel = Button(context)
        cancel!!.setText(R.string.cancel)
        cancel!!.setOnClickListener(this)
        linearLayoutBtn.addView(cancel, params)

        dialogFrame!!.addView(linearLayoutBtn)
    }

    /**
     * 
     * 
     */
    public override fun onClick(v: View?) {
    }

    /**
     * 
     * 
     */
    public override fun doLayout() {
        var mWidth = getContext().getResources().getDisplayMetrics().widthPixels
        if (control!!.getSysKit().isVertical(getContext())) {
            mWidth -= MARGIN * 2
        } else {
            mWidth -= MARGIN * 8
        }
        //text view
        var params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.topMargin = GAP
        params.bottomMargin = GAP
        textView!!.setLayoutParams(params)


        // editText
        params = LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = GAP
        params.rightMargin = GAP
        editText!!.setLayoutParams(params)


        // ok、cancel
        params = LinearLayout.LayoutParams(mWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)
        ok!!.setLayoutParams(params)
        cancel!!.setLayoutParams(params)
    }

    /**
     * 
     * 
     */
    public override fun onConfigurationChanged(newConfig: Configuration) {
        doLayout()
    }

    /**
     * 判断文件名是否合法
     * 
     * @param fileName
     * @return
     */
    fun isFileNameOK(fileName: String?): Boolean {
        if (fileName == null || fileName.length < 1 || fileName.length > 255) {
            return false
        }

        val invalidateChars = "\\/:*?\"<>|"
        val len = invalidateChars.length
        var index = 0
        while (index < len) {
            if (fileName.indexOf(invalidateChars.get(index)) > -1) {
                return false
            }
            index++
        }
        return true
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        editText = null
        textView = null
    }

    //
    protected var textView: TextView? = null

    //
    protected var editText: EditText? = null

    /**
     * 
     * @param context
     * @param action
     * @param model
     * @param dialogID
     * @param titleResID
     */
    init {
        init(context)
    }
}
