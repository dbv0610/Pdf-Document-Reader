/*
 * 文件名称:           ImageTextButton.java
 * 
 * 编译器:             android2.2
 * 时间:               下午1:34:44
 */
package com.wxiwei.office.officereader.beans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.system.IControl
import kotlin.math.ceil


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
class AImageTextButton(
    context: Context, control: IControl?, text: String?, toolstip: String?,
    iconResID: Int, iconResIdDisable: Int, actionID: Int, textGravity: Int, fontSize: Int
) : AImageButton(context, control, toolstip, iconResID, iconResIdDisable, actionID) {
    /**
     * 
     * 
     */
    override fun onDraw(canvas: Canvas) {
        val bitmap = bitmap ?: return
        val text = text ?: return
        val clip = canvas.getClipBounds()
        // 绘制区域宽度
        val w = clip.right - clip.left
        // 绘制区域高度
        val h = clip.bottom - clip.top
        // icon 宽度
        val bW = bitmap.getWidth()
        // icon 高度
        val bH = bitmap.getHeight()
        var x: Int
        var y: Int
        // 文本在上面
        if (textGravity == TEXT_TOP) {
            x = w - textWidth / 2
            y = (h - bH - GAP * 2 - textHeight) / 2

            canvas.drawText(text, x.toFloat(), y - paint.ascent(), paint)
            y = h - bH - GAP
            x = (w - bW) / 2
            canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)
        } else if (textGravity == TEXT_BOTTOM) {
            topIndent = (h - bH - GAP * 6 - textHeight) / 2
            x = (w - bW) / 2
            y = topIndent
            canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)

            y = bH + topIndent + GAP * 6
            x = (w - textWidth) / 2
            canvas.drawText(text, x.toFloat(), y - paint.ascent(), paint)
        } else if (textGravity == TEXT_LEFT) {
            x = (w - textWidth - bW - GAP * 2) / 2
            y = (h - textHeight) / 2
            canvas.drawText(text, x.toFloat(), y - paint.ascent(), paint)

            y = (h - bH) / 2
            x = w - bW - GAP
            canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)
        } else if (textGravity == TEXT_RIGHT) {
            leftIndent = w / 10
            y = (h - bH) / 2
            x = leftIndent
            canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)

            y = (h - textHeight) / 2
            x = bW + leftIndent + GAP * 6
            canvas.drawText(text, x.toFloat(), y - paint.ascent(), paint)
        }
    }

    /**
     * 
     * 
     */
    override fun dispose() {
        super.dispose()
        text = null
    }

    // 见常量定义
    private var textGravity = -1

    //
    private var textWidth = 0

    //
    private var textHeight = 0
    /**
     * @return Returns the topIndent.
     */
    /**
     * @param topIndent The topIndent to set.
     */
    // 上边距
    var topIndent: Int = 0
    /**
     * @return Returns the bottomIndent.
     */
    /**
     * @param bottomIndent The bottomIndent to set.
     */
    // 下边距
    var bottomIndent: Int = 0
    /**
     * @return Returns the leftIndent.
     */
    /**
     * @param leftIndent The leftIndent to set.
     */
    // 左边距
    var leftIndent: Int = 0
    /**
     * @return Returns the rightIndent.
     */
    /**
     * @param rightIndent The rightIndent to set.
     */
    // 右边距
    var rightIndent: Int = 0

    // 显示文本
    private var text: String?

    private var paint: Paint

    /**
     * 
     * @param context           Activity实例
     * @param control           Control实例
     * @param text              显示的文本
     * @param tooltip           toolstip文本
     * @param iconResID         显示的图标ID
     * @param iconResIdDisable  button不可用时显示图片，如查不需要处理请传入-1
     * @param actionID          button的ActionID
     * @param textGravity       文本相对图标的位置
     * @param fontSize          文本字体大小
     */
    init {
        setEnabled(true)
        this.text = text
        paint = Paint()
        // 文字相对图片位置必须有效
        if (textGravity >= TEXT_TOP && textGravity <= TEXT_RIGHT) {
            this.textGravity = textGravity
            paint.setFlags(Paint.ANTI_ALIAS_FLAG)
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL))
            paint.setTextSize(fontSize.toFloat())
            if (text != null && text.length > 0) {
                textWidth = paint.measureText(text).toInt()
                textHeight = ceil((paint.descent() - paint.ascent()).toDouble()).toInt()
            }
        }
    }

    companion object {
        // 文本在图片上面
        const val TEXT_TOP: Int = 0 // 0

        // 文本在图片下面
        val TEXT_BOTTOM: Int = TEXT_TOP + 1 // 1

        // 文本在图片左面
        val TEXT_LEFT: Int = TEXT_BOTTOM + 1 // 2

        // 文本在图片左面
        val TEXT_RIGHT: Int = TEXT_LEFT + 1 // 3

        //
        val GAP: Int = MainConstant.GAP
    }
}
