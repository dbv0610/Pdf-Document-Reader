/*
 * 文件名称:          AImageSearchButton.java
 *  
 * 编译器:            android2.2
 * 时间:              下午1:48:27
 */
package com.wxiwei.office.officereader.beans

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl


/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-3-12
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
@SuppressLint("WrongCall")
class AImageFindButton : LinearLayout, GestureDetector.OnGestureListener, View.OnClickListener {
    /**
     * 
     * @param context
     * @param control
     * @param toolstip
     * @param iconResID
     * @param iconResIdDisable
     * @param actionID
     * @param editWidth
     * @param btnWidth
     * @param height
     */
    constructor(
        context: Context, control: IControl?, toolstip: String?,
        iconResID: Int, iconResIdDisable: Int, actionID: Int, editWidth: Int,
        btnWidth: Int, height: Int, textWatcher: TextWatcher?
    ) : super(context) {
        this.control = control
        // 在同一水平线
        setOrientation(HORIZONTAL)
        setVerticalGravity(Gravity.CENTER)


        //edit file name
        editText = EditText(getContext())
        editText!!.setFreezesText(false)
        editText!!.setGravity(Gravity.CENTER)
        editText!!.setSingleLine()
        editText!!.addTextChangedListener(textWatcher)

        val params = LayoutParams(
            editWidth - MainConstant.GAP * 2, LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = MainConstant.GAP
        params.rightMargin = MainConstant.GAP

        addView(editText, params)


        //search button
        btn = AImageButton(
            context, control,
            toolstip, iconResID, iconResIdDisable, actionID
        )

//        btn.setNormalBgResID(R.drawable.sys_toolsbar_button_bg_normal);
//        btn.setPushBgResID(R.drawable.sys_toolsbar_button_bg_push);
        btn!!.setLayoutParams(LayoutParams(btnWidth, height))
        btn!!.setOnClickListener(this)
        btn!!.setEnabled(false)

        addView(btn)
    }

    /**
     * 
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 
     * 
     */
    public override fun onConfigurationChanged(newConfig: Configuration?) {
        //setMinimumWidth(this.getResources().getDisplayMetrics().widthPixels);
    }

    /**
     * 
     * @param width
     */
    fun resetEditTextWidth(width: Int) {
        editText!!.getLayoutParams().width = width
    }

    /**
     * 
     * 
     */
    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 如果相等，基本可以确定button总宽度小于屏幕宽度
        //if (getWidth() == getResources().getDisplayMetrics().widthPixels)
        run {}
    }


    /**
     * 单击事件
     */
    public override fun onDraw(canvas: Canvas) {
        btn!!.onDraw(canvas)
    }

    /**
     * 单击事件
     */
    override fun onClick(v: View?) {
        if (!longPressed && v is AImageButton) {
            val imm =
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.hideSoftInputFromWindow(
                editText!!.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            imm.hideSoftInputFromInputMethod(
                editText!!.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            )

            control!!.actionEvent(v.actionID, editText!!.getText().toString())
        }

        longPressed = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return btn!!.onTouchEvent(event)
    }

    /**
     * 
     */
    override fun onDown(e: MotionEvent): Boolean {
        return btn!!.onDown(e)
    }

    /**
     * 
     */
    override fun onShowPress(e: MotionEvent) {
        btn!!.onShowPress(e)
    }

    /**
     * 
     */
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return btn!!.onSingleTapUp(e)
    }

    /**
     * 
     */
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return btn!!.onScroll(e1, e2, distanceX, distanceY)
    }

    /**
     * 
     */
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return btn!!.onFling(e1, e2, velocityX, velocityY)
    }

    /**
     * 
     */
    override fun onLongPress(e: MotionEvent) {
        longPressed = true
        btn!!.onLongPress(e)
    }

    /**
     * clear textbox content
     * disable search button
     */
    fun reset() {
        editText!!.setText("")
        btn!!.setEnabled(false)
    }

    fun setFindBtnState(state: Boolean) {
        btn!!.setEnabled(state)
    }

    fun dispose() {
        control = null
        editText = null

        btn!!.dispose()
        btn = null
    }

    //
    protected var control: IControl? = null

    //
    private var editText: EditText? = null

    //search button
    private var btn: AImageButton? = null

    //
    //
    private var longPressed = false
}
