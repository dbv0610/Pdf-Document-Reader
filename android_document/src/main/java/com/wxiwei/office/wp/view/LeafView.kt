/*
 * 文件名称:          LeafView.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:36:21
 */
package com.wxiwei.office.wp.view

import android.graphics.Canvas
import android.graphics.Paint
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.macro.UpdateStatusListener
import com.wxiwei.office.simpletext.font.FontTypefaceManage
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.CharAttr
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewKit

/**
 * word Leaf 视图
 */
open class LeafView : AbstractView {

    private class DrawWidths(
        val text: String,
        val start: Int,
        val end: Int,
        val textSize: Float,
        val widths: FloatArray
    ) {
        fun matches(text: String, start: Int, end: Int, textSize: Float): Boolean {
            return this.text == text && this.start == start && this.end == end && this.textSize == textSize
        }
    }

    private var drawWidths: DrawWidths? = null
    private var alternateDrawWidths: DrawWidths? = null

    // 字符属性
    @JvmField
    protected var charAttr: CharAttr? = null

    //
    protected var paint: Paint? = null

    //
    @JvmField
    protected var numPages = -1

    constructor()

    constructor(paraElem: IElement, elem: IElement) {
        this.elem = elem
        initProperty(elem, paraElem)
    }

    override fun getType(): Short {
        return WPViewConstant.LEAF_VIEW
    }

    /**
     * 初始化leaf属性
     */
    @Synchronized
    open fun initProperty(elem: IElement, paraElem: IElement) {
        this.elem = elem
        drawWidths = null
        alternateDrawWidths = null
        if (paint == null) {
            paint = Paint()
        } else {
            paint!!.reset()
        }
        val paint = paint!!
        paint.isAntiAlias = true
        if (charAttr == null) {
            charAttr = CharAttr()
        }
        val charAttr = charAttr!!

        AttrManage.instance().fillCharAttr(charAttr, paraElem.getAttribute(), elem.getAttribute())
        // 粗斜体
        if (charAttr.isBold && charAttr.isItalic) {
            paint.textSkewX = -0.2f
            paint.isFakeBoldText = true
        }
        // 粗体
        else if (charAttr.isBold) {
            paint.isFakeBoldText = true
        }
        // 斜体
        else if (charAttr.isItalic) {
            paint.textSkewX = -0.25f
        }
        // 字体没有什么好改变的，用统一的吧
        paint.typeface = FontTypefaceManage.instance().getFontTypeface(charAttr.fontIndex)
        // 字号
        if (charAttr.subSuperScriptType > 0) {
            paint.textSize = charAttr.fontSize * (charAttr.fontScale / 100f) * MainConstant.POINT_TO_PIXEL / 2
        } else {
            paint.textSize = charAttr.fontSize * (charAttr.fontScale / 100f) * MainConstant.POINT_TO_PIXEL
        }

        // 颜色
        paint.color = charAttr.fontColor
    }

    /**
     * 视图布局
     */
    open fun doLayout(docAttr: DocAttr?, pageAttr: PageAttr?, paraAttr: ParaAttr?, x: Int, y: Int, w: Int, h: Int, maxEnd: Long, flag: Int): Int {
        val start = getStartOffset(null)
        val startElem = elem!!.getStartOffset()
        var text = elem!!.getText(null)
        if (start > startElem) {
            text = text!!.substring((start - startElem).toInt(), (elem!!.getEndOffset() - startElem).toInt())
        }
        val widths = FloatArray(text!!.length)
        paint!!.getTextWidths(text, widths)
        var tW = 0f
        var i = 0
        val layoutInTable = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_PARA_IN_TABLE.toInt())
        var breakType = WPViewConstant.BREAK_NO.toInt()
        val keepOne = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt())
        var ch: Char
        while (i < text!!.length) {
            ch = text[i]
            tW += widths[i]
            if (ch == '\u0007' || ch == '\n' || ch == '\r') {
                tW -= widths[i]
                i++
                breakType = WPViewConstant.BREAK_ENTER.toInt()
                break
            } else if (!layoutInTable && ch == '\u000C') {
                i++
                breakType = WPViewConstant.BREAK_PAGE.toInt()
                break
            } else if (ch == '\u000B') {
                i++
                breakType = WPViewConstant.BREAK_ENTER.toInt()
                break
            } else if (tW > w) {
                tW -= widths[i]
                breakType = WPViewConstant.BREAK_LIMIT.toInt()
                if (keepOne && i == 0) {
                    tW += widths[i]
                    i++
                    breakType = WPViewConstant.BREAK_NO.toInt()
                }
                break
            }
            i++
        }
        setEndOffset(i + start)
        setSize(tW.toInt(), Math.ceil((paint!!.descent() - paint!!.ascent()).toDouble()).toInt())
        return breakType
    }

    /**
     * 得到指定结束位置字符宽度
     */
    open fun getTextWidth(): Float {
        var text = elem!!.getText(null)
        val s = (start - elem!!.getStartOffset()).toInt()
        val e = (end - elem!!.getStartOffset()).toInt()
        text = text!!.substring(s, e)
        val widths = FloatArray(text.length)
        paint!!.getTextWidths(text, widths)
        var tW = 0f
        for (i in 0 until text.length) {
            tW += widths[i]
        }
        return tW
    }

    private fun getFieldTextReplacedByPage(text: String?, page: Int): String? {
        if (text != null) {
            val chars = text.toCharArray()
            val title = StringBuilder()

            for (i in chars.indices) {
                if (Character.isDigit(chars[i])) {
                    title.append(chars[i])
                }
            }

            if (title.length > 0) {
                return text.replace(title.toString(), page.toString())
            }
        }

        return text
    }

    open fun getPageNumber(): Int {
        try {
            var view: IView? = getParentView()!!.getParentView()!!.getParentView()
            if (view is CellView) {
                view = view.getParentView()!!.getParentView()!!.getParentView()
            }

            if (view is PageView) {
                return view.getPageNumber()
            } else if (view is TitleView) {
                return UpdateStatusListener.ALLPages.toInt()
            }
        } catch (e: Exception) {
        }

        return 0
    }

    private fun getDrawWidths(text: String, start: Int, end: Int, paint: Paint): FloatArray {
        val cached = drawWidths
        if (cached != null && cached.matches(text, start, end, paint.textSize)) {
            return cached.widths
        }
        val alternate = alternateDrawWidths
        if (alternate != null && alternate.matches(text, start, end, paint.textSize)) {
            alternateDrawWidths = cached
            drawWidths = alternate
            return alternate.widths
        }
        // Measure the same shaping context, but retain only this leaf's range.
        val widths = FloatArray(text.length)
        paint.getTextWidths(text, widths)
        val measured = DrawWidths(text, start, end, paint.textSize, widths.copyOfRange(start, end))
        // Keep thumbnail and screen zooms from evicting each other; draw holds the leaf monitor.
        alternateDrawWidths = cached
        drawWidths = measured
        return measured.widths
    }

    @Synchronized
    override fun draw(canvas: Canvas, originX: Int, originY: Int, zoom: Float) {
        val paint = paint!!
        val charAttr = charAttr!!
        val dX = (x * zoom) + originX
        var dY = (y * zoom) + originY
        val oldColor = paint.color
        // 高亮
        if (charAttr.highlightedColor != -1) {
            val line = getParentView()
            if (line != null) {
                paint.color = charAttr.highlightedColor
                canvas.drawRect(dX, originY.toFloat(), dX + getWidth() * zoom, originY + line.getHeight() * zoom, paint)
                paint.color = oldColor
            }
        }

        val oldFontSize = paint.textSize
        paint.textSize = oldFontSize * zoom
        // 下标
        if (charAttr.subSuperScriptType.toInt() == 1) {
            dY -= Math.ceil((paint.descent() - paint.ascent()).toDouble()).toInt()
        }
        // 绘制文本
        var text: String? = elem!!.getText(null)
        var s = (start - elem!!.getStartOffset()).toInt()
        var e = (end - elem!!.getStartOffset()).toInt()

        var adjustFieldText = false
        //total pages
        if (charAttr.pageNumberType == WPModelConstant.PN_PAGE_NUMBER) {
            try {
                var view: IView? = getParentView()!!.getParentView()!!.getParentView()
                if (view != null) {
                    var pageView: PageView? = null
                    while (view != null) {
                        if (view is TitleView && view.getParentView() != null) {
                            pageView = view.getParentView() as PageView
                            break
                        } else if (view is PageView) {
                            pageView = view
                            break
                        } else if (view is WPSTRoot) {
                            view = view.getParentView()!!.getParentView()!!.getParentView()!!.getParentView()
                        } else {
                            view = view.getParentView()
                        }
                    }

                    if (pageView != null) {
                        adjustFieldText = true
                        text = getFieldTextReplacedByPage(text, pageView.getPageNumber())
                        s = 0
                        e = text!!.length
                    }
                }
            } catch (exc: Exception) {
            }
        } else if (charAttr.pageNumberType == WPModelConstant.PN_TOTAL_PAGES && numPages > 0) {
            adjustFieldText = true
            text = getFieldTextReplacedByPage(text, numPages)
            s = 0
            e = text!!.length
        }

        val widths = getDrawWidths(text!!, s, e, paint)
        var extX = 0f
        if (!adjustFieldText && zoom != 1.0f && e > s) {
            var cw = 0f
            for (i in s until e) {
                cw += widths[i - s]
            }
            val ch = text[e - 1]
            if (ch == '\u0007' || ch == '\n' || ch == '\r') {
                cw -= widths[e - s - 1]
            }
            var extW = 0f
            val nextView = getNextView()
            if (nextView != null
                && (nextView.getType() == WPViewConstant.LEAF_VIEW
                        || (nextView.getType() == WPViewConstant.SHAPE_VIEW && (nextView as ShapeView).isInline()))
            ) {
                val nextX = nextView.getX() * zoom
                extW = cw - ((nextX + originX) - dX)
            } else {
                extW = cw - getLayoutSpan(WPViewConstant.X_AXIS) * zoom
            }
            if (extW != 0f) {
                extX = extW / (e - s)
            }
        }
        var drawX = dX
        val drawY = dY - paint.ascent()
        var i = s
        while (i < e) {
            val c = text[i]
            if (c == '\n' || c == '\r' || c == '\u0007' || c == '\u000B'
                || c == '\u000C' || c == '\t' || c == ' ' || c == '\u0002'
            ) {
                drawX += widths[i - s] - extX
                i++
                continue
            }
            // 处理连字符问题
            var skip = 0
            for (j in i + 1 until e) {
                if (widths[j - s] != 0f) {
                    break
                }
                skip++
            }
            canvas.drawText(text, i, i + 1 + skip, drawX, drawY, paint)
            drawX += widths[i - s] - extX
            i += skip
            i++
        }

        // 绘制删除线
        dY += Math.ceil((paint.descent() - paint.ascent()).toDouble()).toInt() / 2
        // 单删除线
        if (charAttr.isStrikeThrough) {
            canvas.drawRect(dX, dY - 1, dX + getWidth() * zoom, dY + 1, paint)
        }
        // 双删除线
        else if (charAttr.isDoubleStrikeThrough) {
            canvas.drawRect(dX, dY - 3, dX + getWidth() * zoom, dY - 1, paint)
            canvas.drawRect(dX, dY, dX + getWidth() * zoom, dY + 2, paint)
        }
        paint.textSize = oldFontSize
    }

    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        var text = elem!!.getText(null)
        val s = (start - elem!!.getStartOffset()).toInt()
        val e = (offset - elem!!.getStartOffset()).toInt()
        text = text!!.substring(s, e)
        rect.x = paint!!.measureText(text).toInt()
        rect.x += getX()
        rect.y += getY()
        rect.height = getLayoutSpan(WPViewConstant.Y_AXIS)
        return rect
    }

    /**
     * @param x
     * @param y
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        var vX = x
        vX -= this.x
        var text = elem!!.getText(null)
        val s = (start - elem!!.getStartOffset()).toInt()
        val e = (end - elem!!.getStartOffset()).toInt()
        text = text!!.substring(s, e)
        val widths = FloatArray(text.length)
        paint!!.getTextWidths(text, widths)
        var count = 0
        for (i in 0 until text.length) {
            vX = (vX - widths[i]).toInt()
            if (vX <= 0) {
                // 如果x值小于最后一个字符宽度的一半，则取前一个offset
                if (vX + widths[i] >= widths[i] / 2) {
                    count++
                }
                break
            }
            count++
        }
        return start + count
    }

    /**
     * 得到基线
     */
    open fun getBaseline(): Int {
        if ("\n" != elem!!.getText(null)) {
            return (-paint!!.ascent()).toInt()
        }
        return 0
    }

    open fun getUnderlinePosition(): Int {
        return (getY() + getHeight() - (getHeight() - paint!!.textSize)).toInt()
    }

    open fun getCharAttr(): CharAttr? {
        return this.charAttr
    }

    /**
     * 放回对象池
     */
    override fun free() {
    }

    /**
     * current leafview has total page number field code or not
     */
    open fun hasUpdatedFieldText(): Boolean {
        if (charAttr != null && charAttr!!.pageNumberType == WPModelConstant.PN_TOTAL_PAGES) {
            return true
        }
        return false
    }

    open fun setNumPages(numPages: Int) {
        if (hasUpdatedFieldText()) {
            this.numPages = numPages
        }
    }

    @Synchronized
    override fun dispose() {
        super.dispose()
        paint = null
        charAttr = null
        drawWidths = null
        alternateDrawWidths = null
    }
}
