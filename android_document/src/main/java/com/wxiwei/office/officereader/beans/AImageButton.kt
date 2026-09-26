/*
 * 文件名称:          AImageButton.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:52:27
 */
package com.wxiwei.office.officereader.beans

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.wxiwei.office.common.PaintKit
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.system.IControl


/**
 * 自定义ImageButton
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
 * 日期:            2011-12-7
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
open class AImageButton(
    context: Context, //
    protected var control: IControl?,
    /**
     * @return Returns the tooltip.
     */
    // 提示信息
    var toolstip: String?,
    iconResID: Int, iconResIdDisable: Int, actionID: Int
) : View(context), GestureDetector.OnGestureListener, View.OnClickListener {
    /**
     * 
     */
    override fun onFocusChanged(
        gainFocus: Boolean,
        direction: Int, previouslyFocusedRect: Rect?
    ) {
        val id = if (gainFocus) focusBgResID else normalBgResID
        if (id != -1) {
            setBackgroundResource(id)
        }
    }

    /**
     * 单击事件
     */
    override fun onClick(v: View?) {
        if (!longPressed && v is AImageButton) {
            control!!.actionEvent(v.actionID, null)
        }
        longPressed = false
    }


    /**
     * 
     * 
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture!!.onTouchEvent(event)
        val action = event.getAction()
        if (!isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                control!!.actionEvent(EventConstant.SYS_CLOSE_TOOLTIP, null)
            }
            return true
        }
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> if (pushBgResID != -1) {
                setBackgroundResource(pushBgResID)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (normalBgResID == -1) {
                    setBackgroundDrawable(null)
                } else {
                    setBackgroundResource(normalBgResID)
                }
                control!!.actionEvent(EventConstant.SYS_CLOSE_TOOLTIP, null)
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 
     * 
     */
    public override fun onDraw(canvas: Canvas) {
        if (bitmap == null) {
            return
        }
        if (isEnabled()) {
            canvas.drawBitmap(
                bitmap!!,
                ((getWidth() - bitmap!!.getWidth()) / 2).toFloat(),
                ((getHeight() - bitmap!!.getHeight()) / 2).toFloat(),
                PaintKit.instance().getPaint()
            )
        } else if (bitmapDisable != null) {
            canvas.drawBitmap(
                bitmapDisable!!,
                ((getWidth() - bitmapDisable!!.getWidth()) / 2).toFloat(),
                ((getHeight() - bitmapDisable!!.getHeight()) / 2).toFloat(),
                PaintKit.instance().getPaint()
            )
        }
    }

    /**
     * 
     */
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    /**
     * 
     */
    override fun onShowPress(e: MotionEvent) {
    }

    /**
     * 
     */
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
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
        return false
    }

    /**
     * 
     */
    override fun onLongPress(e: MotionEvent) {
        longPressed = true

        if (toolstip != null && toolstip!!.length > 0) {
            control!!.actionEvent(EventConstant.SYS_SHOW_TOOLTIP, toolstip)
        }
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
        return false
    }


    /**
     * 设置获得焦点时背景ID
     */
    fun setFocusBgResID(focusBgResID: Int) {
        this.focusBgResID = focusBgResID
    }

    /**
     * 设置按下时背景ID
     * 
     * @param pushBgResID The pushBgResID to set.
     */
    fun setPushBgResID(pushBgResID: Int) {
        this.pushBgResID = pushBgResID
    }

    /**
     * 设置正常模式下的背景
     * 
     * @param normalBgResID The normalBgResID to set.
     */
    fun setNormalBgResID(normalBgResID: Int) {
        setBackgroundResource(normalBgResID)
        this.normalBgResID = normalBgResID
    }

    val iconWidth: Int
        /**
         * 
         */
        get() = if (bitmap == null) 0 else bitmap!!.getWidth()

    val iconHeight: Int
        /**
         * 
         */
        get() = if (bitmap == null) 0 else bitmap!!.getHeight()

    /**
     * 
     */
    open fun dispose() {
        toolstip = null
        control = null
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
        if (bitmapDisable != null) {
            bitmapDisable!!.recycle()
            bitmapDisable = null
        }
        gesture = null
    }

    /**
     * @return Returns the actionID.
     */
    // 动作ID
    var actionID: Int
        protected set

    // 普通状态下背景图片ID
    @JvmField
    protected var normalBgResID: Int = -1

    // 按下的背景图片ID
    @JvmField
    protected var pushBgResID: Int = -1

    // 获得焦点的背景图片ID
    @JvmField
    protected var focusBgResID: Int = -1

    // 显示图片
    protected var bitmap: Bitmap?

    // 不可用的图片
    protected var bitmapDisable: Bitmap? = null

    //
    protected var gesture: GestureDetector?

    // 绘制器
    //
    protected var longPressed: Boolean = false

    /**
     * 
     * @param context           Activity实例
     * @param toolstip          tool tip文本
     * @param iconResID         显示的图标ID
     * @param iconResIdDisable  button不可用时显示图片，如查不需要处理请传入-1
     * @param actionID          button的ActionID
     */
    init {
        this.toolstip = toolstip
        val opts = BitmapFactory.Options()
        opts.inDensity = context.getResources().getDisplayMetrics().densityDpi
        opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), iconResID, opts)
        if (iconResIdDisable != -1) {
            this.bitmapDisable =
                BitmapFactory.decodeResource(getContext().getResources(), iconResIdDisable, opts)
        }
        this.actionID = actionID

        gesture = GestureDetector(context, this)
        setFocusable(true)
        setClickable(true)
        setLongClickable(true)
        this.setOnClickListener(this)
    }
}
