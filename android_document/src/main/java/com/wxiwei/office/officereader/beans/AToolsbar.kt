/*
 * 文件名称:          Toolbar.java
 *  
 * 编译器:            android2.2
 * 时间:              下午1:48:31
 */
package com.wxiwei.office.officereader.beans

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.wxiwei.office.system.IControl

/**
 * 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            梁金晶
 * 
 * 
 * 日期:            2011-10-27
 * 
 * 
 * 负责人:          梁金晶
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
open class AToolsbar : HorizontalScrollView {
    /**
     * 
     * @param control
     */
    constructor(content: Context?, control: IControl?) : super(content) {
        this.control = control
        this.isAnimation = true
        this.setVerticalFadingEdgeEnabled(false)
        this.setFadingEdgeLength(0)
        //        toolsbarFrame = new LinearLayout(content);
//        // 在同一水平线
//        toolsbarFrame.setOrientation(LinearLayout.HORIZONTAL);
//        toolsbarFrame.setMinimumWidth(this.getResources().getDisplayMetrics().widthPixels);
//        // button size
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(),  R.drawable.sys_toolsbar_button_bg_normal, opts);
//        this.buttonWidth = opts.outWidth;
//        this.buttonHeight = opts.outHeight;
//
//        toolsbarFrame.setBackgroundResource(R.drawable.sys_toolsbar_button_bg_normal);
//        addView(toolsbarFrame, new LayoutParams(LayoutParams.MATCH_PARENT, buttonHeight));
    }


    /**
     * 
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 
     * 
     */
    public override fun onConfigurationChanged(newConfig: Configuration?) {
        toolsbarFrame!!.setMinimumWidth(this.getResources().getDisplayMetrics().widthPixels)
    }

    /**
     * 
     * 
     * @param actionID
     * @return
     */
    protected fun addButton(
        iconResID: Int, iconResIdDisable: Int,
        tooltipResID: Int, actionID: Int, isAddSeparated: Boolean
    ): AImageButton {
        val context = getContext()
        val res = context.getResources()
        val tb = AImageButton(
            context, control,
            res.getString(tooltipResID), iconResID, iconResIdDisable, actionID
        )

//        tb.setNormalBgResID(R.drawable.sys_toolsbar_button_bg_normal);
//        tb.setPushBgResID(R.drawable.sys_toolsbar_button_bg_push);
        tb.setLayoutParams(LayoutParams(buttonWidth, buttonHeight))
        toolsbarFrame!!.addView(tb)

        toolsbarWidth += buttonWidth
        if (actionButtonIndex == null) {
            actionButtonIndex = HashMap<Int?, Int?>()
        }
        actionButtonIndex!!.put(actionID, toolsbarFrame!!.getChildCount() - 1)

        if (isAddSeparated) {
            addSeparated()
        }
        return tb
    }

    /**
     * 
     * @param checkIconResID
     * @param uncheckIconResID
     * @param iconResIdDisable
     * @param checktipResID
     * @param unchecktipResID
     * @param actionID
     * @param isAddSeparated
     * @return
     */
    protected fun addCheckButton(
        checkIconResID: Int, uncheckIconResID: Int, iconResIdDisable: Int,
        checktipResID: Int, unchecktipResID: Int, actionID: Int, isAddSeparated: Boolean
    ): AImageCheckButton {
        val context = getContext()
        val res = context.getResources()
        val tb = AImageCheckButton(
            context,
            control,
            res.getString(checktipResID),
            res.getString(unchecktipResID),
            checkIconResID,
            uncheckIconResID,
            iconResIdDisable,
            actionID
        )

//
//        tb.setNormalBgResID(R.drawable.sys_toolsbar_button_bg_normal);
//        tb.setPushBgResID(R.drawable.sys_toolsbar_button_bg_push);
        tb.setLayoutParams(LayoutParams(buttonWidth, buttonHeight))
        //tb.setOnClickListener(this);
        toolsbarFrame!!.addView(tb)

        toolsbarWidth += buttonWidth
        if (actionButtonIndex == null) {
            actionButtonIndex = HashMap<Int?, Int?>()
        }
        actionButtonIndex!!.put(actionID, toolsbarFrame!!.getChildCount() - 1)

        if (isAddSeparated) {
            addSeparated()
        }
        return tb
    }

    /**
     * 添加分隔符
     */
    protected fun addSeparated() {
//        toolsbarFrame.addView(new AImageButton(getContext(), control, "",
//            R.drawable.sys_toolsbar_separated_horizontal, -1,  -1),
//            new LayoutParams(1, buttonHeight));
//        toolsbarWidth += 1;
    }

    /**
     * 
     * 
     */
    public override fun onDraw(canvas: Canvas) {
        if (this.isAnimation) {
            this.isAnimation = false
            if (toolsbarFrame!!.getWidth() > getResources().getDisplayMetrics().widthPixels) {
                scrollTo(buttonWidth * 3, 0)
            }
            fling(-4000)
        }
        super.onDraw(canvas)
    }

    /**
     * set button enable/disable
     * @param actionID
     * @param enabled
     */
    fun setEnabled(actionID: Int, enabled: Boolean) {
        val index = actionButtonIndex!!.get(actionID)
        if (index != null && index >= 0 && index < toolsbarFrame!!.getChildCount()) {
            toolsbarFrame!!.getChildAt(index).setEnabled(enabled)
        }
    }

    /**
     * set check button state
     * @param actionID
     * @param state
     */
    fun setCheckState(actionID: Int, state: Short) {
        val index: Int = actionButtonIndex!!.get(actionID)!!
        if (index >= 0 && index < toolsbarFrame!!.getChildCount() && toolsbarFrame!!.getChildAt(
                index
            ) is AImageCheckButton
        ) {
            val check = toolsbarFrame!!.getChildAt(index) as AImageCheckButton
            check.setState(state)
        }
    }

    /**
     * 更新button的状态
     */
    open fun updateStatus() {
    }


    /**
     * 
     */
    open fun dispose() {
        control = null
        if (actionButtonIndex != null) {
            actionButtonIndex!!.clear()
            actionButtonIndex = null
        }
        val count = toolsbarFrame!!.getChildCount()
        for (i in 0..<count) {
            val v = toolsbarFrame!!.getChildAt(i)
            if (v is AImageButton) {
                v.dispose()
            }
        }
        toolsbarFrame = null
    }

    /**
     * @return Returns the animation.
     */
    /**
     * @param animation The animation to set.
     */
    //
    var isAnimation: Boolean = false
    /**
     * @return Returns the buttonWidth.
     */
    /**
     * @param buttonWidth The buttonWidth to set.
     */
    // button宽度
    @JvmField
    var buttonWidth: Int = 0
    /**
     * @return Returns the buttonHeight.
     */
    /**
     * @param buttonHeight The buttonHeight to set.
     */
    // button调蓄
    @JvmField
    var buttonHeight: Int = 0
    /**
     * @return Returns the toolsbarWidth.
     */
    /**
     * @param toolsbarWidth The toolsbarWidth to set.
     */
    // 工具条实际宽度
    var toolsbarWidth: Int = 0

    // 用于事件派发
    @JvmField
    protected var control: IControl? = null

    //
    protected var toolsbarFrame: LinearLayout? = null

    // 记录actionID对应button在toolsbar子视图的位置
    protected var actionButtonIndex: MutableMap<Int?, Int?>? = null
}
