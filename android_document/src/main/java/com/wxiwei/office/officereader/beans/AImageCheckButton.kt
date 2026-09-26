/*
 * 文件名称:          AImageCheckButton.java
 *  
 * 编译器:            android2.2
 * 时间:              下午5:12:10
 */
package com.wxiwei.office.officereader.beans

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.EventConstant
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
 * 日期:            2012-3-8
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
class AImageCheckButton(
    context: Context, control: IControl?,
    checkTips: String?, uncheckTips: String?,
    checkIconResID: Int, uncheckIconResID: Int,
    iconResIdDisable: Int, actionID: Int
) : AImageButton(context, control, checkTips, checkIconResID, iconResIdDisable, actionID) {
    /**
     */
    override fun onDraw(canvas: Canvas) {
        val icon = when (state) {
            DISABLE -> bitmapDisable
            CHECK -> bitmap
            UNCHECK -> uncheckBitmap
            else -> null
        } ?: return
        canvas.drawBitmap(
            icon,
            (width - icon.width) / 2f,
            (height - icon.height) / 2f,
            PaintKit.instance().getPaint()
        )
    }

    /**
     * 
     */
    override fun onLongPress(e: MotionEvent) {
        longPressed = true
        when (state) {
            CHECK -> control?.actionEvent(EventConstant.SYS_SHOW_TOOLTIP, toolstip)
            UNCHECK -> control?.actionEvent(EventConstant.SYS_SHOW_TOOLTIP, uncheckTips)
        }
    }


    /**
     * 单击事件
     */
    override fun onClick(v: View?) {
        if (v !is AImageButton || !isEnabled) return
        if (longPressed) {
            longPressed = false
            return
        }
        when (state) {
            CHECK -> setState(UNCHECK)
            UNCHECK -> setState(CHECK)
        }
        control?.actionEvent(v.actionID, (state == CHECK))
        postInvalidate()
        longPressed = false
    }


    /**
     * 
     * @param state
     */
    fun setState(state: Short) {
        this.state = state
        setEnabled(state != DISABLE)
    }

    /**
     * 
     * @return
     */
    fun getState(): Short {
        return state
    }

    override fun dispose() {
        super.dispose()

        if (uncheckBitmap != null) {
            uncheckBitmap!!.recycle()
            uncheckBitmap = null
        }
    }


    // 显示图片
    protected var uncheckBitmap: Bitmap?

    //state
    private var state: Short = 0

    //uncheck tips
    protected var uncheckTips: String?

    /**
     * 
     * @param context
     * @param control
     * @param toolstip
     * @param checkIconResID
     * @param uncheckIconResID
     * @param iconResIdDisable
     * @param actionID
     */
    init {
        this.uncheckTips = uncheckTips
        val opts = BitmapFactory.Options()
        opts.inDensity = context.getResources().getDisplayMetrics().densityDpi
        opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi
        uncheckBitmap = BitmapFactory.decodeResource(context.getResources(), uncheckIconResID, opts)
    }

    companion object {
        //disable
        const val DISABLE: Short = 0

        //checked
        const val CHECK: Short = 1

        //unchecked
        const val UNCHECK: Short = 2
    }
}
