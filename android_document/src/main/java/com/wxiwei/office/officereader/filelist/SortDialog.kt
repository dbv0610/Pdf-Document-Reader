/*
 * 文件名称:          SortDialog.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:44:46
 */
package com.wxiwei.office.officereader.filelist

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.wxiwei.office.R
import com.wxiwei.office.officereader.beans.SingleChoiceList
import com.wxiwei.office.system.IControl
import com.wxiwei.office.system.IDialogAction
import com.wxiwei.office.system.beans.ADialog
import java.util.Vector

/**
 * Sort dialog
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2011-12-20
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class SortDialog(
    control: IControl?,
    context: Context,
    action: IDialogAction?,
    model: Vector<Any>?,
    dialogID: Int,
    titleResID: Int,
    itemsResID: Int
) : ADialog(control, context, action, model, dialogID, titleResID) {
    /**
     * 
     * @param context
     * @param itemsResID
     */
    fun init(context: Context, itemsResID: Int) {
        val mWidth = context.getResources().getDisplayMetrics().widthPixels - MARGIN * 2
        //
        var index = 0
        if (model != null) {
            index = model!!.get(0) as Int
        }
        singleChoiceList = SingleChoiceList(context, itemsResID)
        if (index >= 0) {
            singleChoiceList!!.setItemChecked(index, true)
        }


        // add ListView
        var params = LinearLayout.LayoutParams(mWidth, 100)
        params.leftMargin = GAP
        params.rightMargin = GAP
        dialogFrame!!.addView(singleChoiceList, params)


        // add separated line
        val view = View(context)
        view.setBackgroundColor(Color.GRAY)
        dialogFrame!!.addView(
            view,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        )


        //add ascending, descending       
        sortGroup = RadioGroup(context)
        sortGroup!!.setOrientation(LinearLayout.HORIZONTAL)
        sortGroup!!.setGravity(Gravity.CENTER)
        params = LinearLayout.LayoutParams(mWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)

        val btnAscending = RadioButton(context)
        //        btnAscending.setText(R.string.dialog_ascending);
        sortGroup!!.addView(btnAscending, params)

        val btnDescending = RadioButton(context)
        //        btnDescending.setText(R.string.dialog_descending);
        sortGroup!!.addView(btnDescending, params)

        params = LinearLayout.LayoutParams(mWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = GAP
        params.rightMargin = GAP
        dialogFrame!!.addView(sortGroup, params)


        //设置 ascending, descending
        var pos = 0
        if (model != null) {
            pos = model!!.get(1) as Int
        }
        (sortGroup!!.getChildAt(pos) as RadioButton).setChecked(true)


        //ok, cancel
        val linearLayoutBtn = LinearLayout(context)
        linearLayoutBtn.setGravity(Gravity.CENTER)
        linearLayoutBtn.setOrientation(LinearLayout.HORIZONTAL)
        params = LinearLayout.LayoutParams(mWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)

        ok = Button(context)
        ok!!.setText(R.string.ok)
        ok!!.setOnClickListener(this)
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
    public override fun doLayout() {
        var mWidth = getContext().getResources().getDisplayMetrics().widthPixels
        var mHeight = getContext().getResources().getDisplayMetrics().heightPixels
        // 需要减去标题栏的高度
        mHeight -= getWindow()!!.getDecorView().getHeight() - dialogFrame!!.getHeight()
        if (control!!.getSysKit().isVertical(getContext())) {
            mWidth -= MARGIN * 2
            mHeight -= MARGIN * 11
        } else {
            mWidth -= MARGIN * 8
            mHeight -= MARGIN * 2
        }


        //set singleChoice
        var params = LinearLayout.LayoutParams(
            mWidth - GAP * 2,
            mHeight - sortGroup!!.getHeight() - ok!!.getHeight() - GAP * 4
        )
        params.leftMargin = GAP
        params.rightMargin = GAP
        singleChoiceList!!.setLayoutParams(params)


        //sortGroup        
        params = LinearLayout.LayoutParams(mWidth / 2, LinearLayout.LayoutParams.WRAP_CONTENT)
        (sortGroup!!.getChildAt(0) as RadioButton).setLayoutParams(params)
        (sortGroup!!.getChildAt(1) as RadioButton).setLayoutParams(params)

        params = LinearLayout.LayoutParams(mWidth, sortGroup!!.getHeight())
        params.leftMargin = GAP
        params.rightMargin = GAP
        params.gravity = Gravity.CENTER
        sortGroup!!.setLayoutParams(params)


        // ok, cancel
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
     * 
     * 
     */
    public override fun onClick(v: View?) {
        if (v === ok) {
            val typePos = singleChoiceList!!.getCheckedItemPosition()
            val checkedID = sortGroup!!.getCheckedRadioButtonId()
            val childPos =
                sortGroup!!.indexOfChild(sortGroup!!.findViewById<View?>(checkedID) as RadioButton?)
            val vector = Vector<Any>()
            vector.add(typePos)
            vector.add(childPos)
            action!!.doAction(dialogID, vector)
        }
        dismiss()
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        singleChoiceList = null
        sortGroup = null
    }

    //
    private var singleChoiceList: SingleChoiceList? = null

    //
    private var sortGroup: RadioGroup? = null

    /**
     * 
     * @param context
     * @param action
     * @param model
     * @param dialogID
     * @param titleResID
     * @param itemsResID
     */
    init {
        init(context, itemsResID)
    }
}
