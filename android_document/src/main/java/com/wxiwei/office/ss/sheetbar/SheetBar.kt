/*
 * 文件名称:          SheetBar.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.sheetbar

import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.wxiwei.office.R
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.IControl
import java.util.Vector

/**
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2011-11-15
 * 负责人:          ljj8494
 */
class SheetBar : HorizontalScrollView, View.OnClickListener {
    //
    private var minimumWidth = 0
    //
    private var sheetbarResManager: SheetbarResManager? = null
    //
    private var sheetbarHeight = 0
    //
    private var currentSheet: SheetButton? = null
    //
    private var control: IControl? = null
    //
    private var sheetbarFrame: LinearLayout? = null

    /**
     *
     * @param context
     */
    constructor(context: Context) : super(context)

    constructor(context: Context, control: IControl?, minimumWidth: Int) : super(context) {
        this.control = control
        this.isVerticalFadingEdgeEnabled = false
        this.setFadingEdgeLength(0)
        if (minimumWidth == resources.displayMetrics.widthPixels) {
            this.minimumWidth = -1
        } else {
            this.minimumWidth = minimumWidth
        }
        init()
    }

    /**
     *
     */
    public override fun onConfigurationChanged(newConfig: Configuration) {
        sheetbarFrame!!.minimumWidth = if (minimumWidth == -1) resources.displayMetrics.widthPixels else minimumWidth
    }

    /**
     *
     */
    private fun init() {
        val context = this.context
        val sheetbarFrame = LinearLayout(context)
        this.sheetbarFrame = sheetbarFrame
        sheetbarFrame.gravity = Gravity.BOTTOM
        sheetbarFrame.setPadding(
            context.resources.getDimensionPixelSize(R.dimen.space_10),
            sheetbarFrame.paddingTop,
            sheetbarFrame.paddingRight,
            sheetbarFrame.paddingBottom
        )

        val sheetbarResManager = SheetbarResManager(context)
        this.sheetbarResManager = sheetbarResManager
        val drawable = sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBAR_BG)
        sheetbarFrame.setBackgroundDrawable(drawable)
        sheetbarFrame.orientation = LinearLayout.HORIZONTAL
        sheetbarFrame.minimumWidth = if (minimumWidth == -1) resources.displayMetrics.widthPixels else minimumWidth

        sheetbarHeight = drawable!!.intrinsicHeight

        /*drawable = sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBAR_SHADOW_LEFT);
        LayoutParams parmas = new LayoutParams(LayoutParams.WRAP_CONTENT, drawable.getIntrinsicHeight());
        // 左边shadow
        View left = new View(context);
        left.setBackgroundDrawable(drawable);
        sheetbarFrame.addView(left, parmas);*/

        // sheetButton
        @Suppress("UNCHECKED_CAST")
        val vec = control!!.getActionValue(EventConstant.SS_GET_ALL_SHEET_NAME, null) as Vector<String>
        //drawable = sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT);
        val parmasButton = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, sheetbarHeight)
        val count = vec.size
        for (i in 0 until count) {
            val sb = SheetButton(context, vec[i], i, sheetbarResManager)
            if (currentSheet == null) {
                currentSheet = sb
                currentSheet!!.changeFocus(true)
            }
            sb.setOnClickListener(this)
            sheetbarFrame.addView(sb, parmasButton)

            /*if (i < count - 1)
            {
                View view = new View(context);
                drawable = sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBAR_SEPARATOR_H);
                view.setBackgroundDrawable(drawable);
                sheetbarFrame.addView(view, parmasButton);
            }*/
        }

        // 右边shadow
        /*View right = new View(context);
        drawable = sheetbarResManager.getDrawable(SheetbarResConstant.RESID_SHEETBAR_SHADOW_RIGHT);
        right.setBackgroundDrawable(drawable);
        sheetbarFrame.addView(right, parmas);*/

        //
        addView(sheetbarFrame, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, sheetbarHeight))
    }

    /**
     *
     */
    override fun onClick(v: View) {
        currentSheet!!.changeFocus(false)

        val sb = v as SheetButton
        sb.changeFocus(true)
        currentSheet = sb

        control!!.actionEvent(EventConstant.SS_SHOW_SHEET, currentSheet!!.getSheetIndex())
    }

    /**
     * set focus sheet button(called when clicked document hyperlink)
     * @param index
     */
    fun setFocusSheetButton(index: Int) {
        if (currentSheet!!.getSheetIndex() == index) {
            return
        }

        val sheetbarFrame = sheetbarFrame!!
        val count = sheetbarFrame.childCount
        var view: View? = null
        for (i in 0 until count) {
            view = sheetbarFrame.getChildAt(i)
            if (view is SheetButton && view.getSheetIndex() == index) {
                currentSheet!!.changeFocus(false)

                currentSheet = view
                currentSheet!!.changeFocus(true)
                break
            }
        }

        //sheetbar scrolled
        val screenWidth = control!!.getActivity().windowManager.defaultDisplay.width
        val barWidth = sheetbarFrame.width
        if (barWidth > screenWidth) {
            val left = view!!.left
            val right = view.right
            var off = (screenWidth - (right - left)) / 2
            off = left - off
            if (off < 0) {
                off = 0
            } else if (off + screenWidth > barWidth) {
                off = barWidth - screenWidth
            }

            scrollTo(off, 0)
        }
    }

    /**
     * @return Returns the sheetbarHeight.
     */
    fun getSheetbarHeight(): Int {
        return sheetbarHeight
    }

    /**
     *
     */
    fun dispose() {
        sheetbarResManager!!.dispose()
        sheetbarResManager = null
        currentSheet = null
        if (sheetbarFrame != null) {
            val count = sheetbarFrame!!.childCount
            var v: View
            for (i in 0 until count) {
                v = sheetbarFrame!!.getChildAt(i)
                if (v is SheetButton) {
                    v.dispose()
                }
            }
            sheetbarFrame = null
        }
    }
}
