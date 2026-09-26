/*
 * 文件名称:          SheetButton.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.sheetbar

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.wxiwei.office.R

class SheetButton(
    context: Context,
    sheetName: String?,
    //
    private var sheetIndex: Int,
    private var sheetbarResManager: SheetbarResManager?
) : LinearLayout(context) {
    // 左边图标
    private var left: View? = null

    // 中间文本
    private var textView: TextView? = null

    // 右边图标
    private var right: View? = null

    private var active = false

    init {
        orientation = HORIZONTAL
        init(context, sheetName)
    }

    /**
     *
     */
    private fun init(context: Context, sheetName: String?) {
        //左边图标
        /*left = new View(context);
        left.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT));
        addView(left);*/

        //
        val textView = TextView(context)
        this.textView = textView
        textView.setBackgroundDrawable(sheetbarResManager!!.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE))
        textView.text = sheetName
        textView.textSize = FONT_SIZE.toFloat()
        textView.gravity = Gravity.CENTER
        textView.setTextColor(getTextColorTab(false))
        val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.space_10)
        val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.space_12)
        textView.setPadding(
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding
        )
        /*int w = (int)textView.getPaint().measureText(sheetName);
        w = Math.max(w, SHEET_BUTTON_MIN_WIDTH);*/
        textView.minWidth = SHEET_BUTTON_MIN_WIDTH
        addView(textView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT))

        // 右边图标
        /*right = new View(context);
        right.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT));
        addView(right);*/
    }

    /**
     *
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            /*case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(!active)
                {
                    left.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_PUSH_LEFT));
                    textView.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_PUSH_MIDDLE));
                    right.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_PUSH_RIGHT));
                }
                break;*/

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (!active) {
                //left.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT));
                textView!!.setBackgroundDrawable(sheetbarResManager!!.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE))
                textView!!.setTextColor(getTextColorTab(false))
                //right.setBackgroundDrawable(sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT));
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 选中或取消选中用到
     */
    fun changeFocus(gainFocus: Boolean) {
        active = gainFocus
        /*left.setBackgroundDrawable(gainFocus ? sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_LEFT) :
            sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT));*/
        textView!!.setBackgroundDrawable(
            if (gainFocus) sheetbarResManager!!.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_MIDDLE)
            else sheetbarResManager!!.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE)
        )
        textView!!.setTextColor(getTextColorTab(gainFocus))
        /*right.setBackgroundDrawable(gainFocus ? sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_RIGHT) :
            sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT));*/
    }

    private fun getTextColorTab(gainFocus: Boolean): Int {
        var colorValue: Int?
        if (gainFocus) {
            colorValue = sheetbarResManager!!.getColor(SheetbarResConstant.RESID_SHEET_BUTTON_SELECTED_TEXT_COLOR)
        } else {
            colorValue = sheetbarResManager!!.getColor(SheetbarResConstant.RESID_SHEET_BUTTON_UNSELECTED_TEXT_COLOR)
        }
        if (colorValue == null) {
            colorValue = Color.WHITE
        }
        return colorValue
    }

    /**
     *
     */
    fun getSheetIndex(): Int {
        return this.sheetIndex
    }

    /**
     *
     */
    fun dispose() {
        sheetbarResManager = null
        left = null
        textView = null
        right = null
    }

    companion object {
        private const val FONT_SIZE = 14

        //
        private const val SHEET_BUTTON_MIN_WIDTH = 100
    }
}
