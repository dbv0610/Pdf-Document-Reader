/*
 * 文件名称:          LayoutKit.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:16:55
 */
package com.wxiwei.office.wp.view

import android.util.Log
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.font.FontKit
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.DocAttr
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.simpletext.view.PageAttr
import com.wxiwei.office.simpletext.view.ParaAttr
import com.wxiwei.office.simpletext.view.ViewKit
import com.wxiwei.office.system.IControl
import com.wxiwei.office.wp.control.Word

/**
 * 布局工具类
 */
class LayoutKit private constructor() {

    companion object {
        //
        private val kit = LayoutKit()

        @JvmStatic
        fun instance(): LayoutKit {
            return kit
        }
    }

    /**
     * 布局页面坐标
     * @param root
     * @param zoom
     */
    fun layoutAllPage(root: PageRoot?, zoom: Float) {
        if (root == null || root.getChildView() == null) {
            return
        }
        val word = root.getContainer() as Word
        val space = word.getPageSpacing()
        var dx = space
        var dy = space
        var pv = root.getChildView()
        val width = pv!!.getWidth()
        var visibleWidth = word.getWidth()
        visibleWidth = if (visibleWidth == 0) word.getWordWidth() else visibleWidth
        if (visibleWidth > width * zoom) {
            dx += ((visibleWidth / zoom - width - space * 2) / 2).toInt()
        }
        while (pv != null) {
            pv.setLocation(dx, dy)
            dy += pv.getHeight() + space
            pv = pv.getNextView()
        }
        root.setSize(width + space * 2, dy)
        word.setSize(width + space * 2, dy)
    }

    /**
     * 布局段落
     * @param docAttr       文档属性
     * @param pageAttr      页面属性
     * @param paraAttr      段落属性
     * @param para          布局段落视图
     * @param startOffset   布局开始Offset
     * @param x             布局开始x值
     * @param y             布局开始y值
     * @param w             布局的宽度
     * @param h             布局的高度
     * @param flag          布局标记
     */
    fun layoutPara(control: IControl, doc: IDocument, docAttr: DocAttr, pageAttr: PageAttr, paraAttr: ParaAttr,
                   para: ParagraphView, startOffset: Long, x: Int, y: Int, w: Int, h: Int, flag: Int): Int {
        var flag = flag
        var breakType = WPViewConstant.BREAK_NO.toInt()
        var dx = paraAttr.leftIndent
        var dy = 0
        var spanW = w - paraAttr.leftIndent - paraAttr.rightIndent
        spanW = if (spanW < 0) w else spanW
        var spanH = h
        var paraHeight = 0
        var maxWidth = if (ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_NOT_WRAP_LINE.toInt())) 0 else w
        var firstLine = true
        val elem = para.getElement()
        var lineStart = startOffset
        val elemEnd = elem!!.getEndOffset()
        // 处理段前段后间距
        val prePara = para.getPreView()
        if (prePara == null) { // 页面第一个段落
            spanH -= paraAttr.beforeSpace
            para.setTopIndent(paraAttr.beforeSpace)
            para.setBottomIndent(paraAttr.afterSpace)
            para.setY(para.getY() + paraAttr.beforeSpace)
        } else {
            if (paraAttr.beforeSpace > 0) {
                var beforeSpace = paraAttr.beforeSpace - prePara.getBottomIndent()
                beforeSpace = Math.max(0, beforeSpace)
                spanH -= beforeSpace
                para.setTopIndent(beforeSpace)
                para.setY(para.getY() + beforeSpace)
            }
            spanH -= paraAttr.afterSpace
            para.setBottomIndent(paraAttr.afterSpace)
        }
        var keepOne = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt())
        if (spanH < 0 && !keepOne) {
            return WPViewConstant.BREAK_LIMIT.toInt()
        }
        var line = ViewFactory.createView(control, elem, elem, WPViewConstant.LINE_VIEW.toInt()) as LineView
        Log.e("LayoutKit.layoutPara", "line.setStartOffset = " + lineStart)
        line.setStartOffset(lineStart)
        para.appendChlidView(line)
        flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), true)
        val ss = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_DELLELINEVIEW.toInt())
        var bnView: BNView? = null
        var bnViewWidth = -1

        while (spanH > 0 && lineStart < elemEnd && breakType != WPViewConstant.BREAK_PAGE.toInt()) {
            // layout bullet and number
            if (firstLine && startOffset == elem!!.getStartOffset()) {
                bnView = createBNView(control, doc, docAttr, pageAttr, paraAttr, para, dx, dy, spanW, spanH, flag)
                if (bnView != null) {
                    bnViewWidth = bnView.getWidth()
                }
            }
            var lineIndent = getLineIndent(control, bnViewWidth, paraAttr, firstLine)
            if (bnView != null && lineIndent + paraAttr.leftIndent == paraAttr.tabClearPosition) {
                if ((AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_SPECIALINDENT_ID)
                            && AttrManage.instance().getParaSpecialIndent(elem!!.getAttribute()) < 0)
                    || AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.PARA_INDENT_LEFT_ID)
                ) {
                    bnView.setX(0)
                    lineIndent = bnViewWidth
                    dx = 0
                }
            }
            line.setLeftIndent(lineIndent)
            line.setLocation(dx + lineIndent, dy)
            breakType = layoutLine(control, doc, docAttr, pageAttr, paraAttr, line, bnView, dx, dy, spanW - lineIndent, spanH, elemEnd, flag)
            val lineHeight = line.getLayoutSpan(WPViewConstant.Y_AXIS)
            if (!ss && !keepOne
                && ((spanH - lineHeight < 0 || line.getChildView() == null)
                        || spanW - lineIndent <= 0)
            ) {
                breakType = WPViewConstant.BREAK_LIMIT.toInt()
                para.deleteView(line, true)
                break
            }
            paraHeight += lineHeight
            dy += lineHeight
            spanH -= lineHeight
            lineStart = line.getEndOffset(null)
            maxWidth = Math.max(maxWidth, line.getLayoutSpan(WPViewConstant.X_AXIS))
            if (lineStart < elemEnd && spanH > 0) {
                line = ViewFactory.createView(control, elem, elem, WPViewConstant.LINE_VIEW.toInt()) as LineView
                Log.e("LayoutKit.204", "line.setStartOffset = " + lineStart)
                line.setStartOffset(lineStart)
                para.appendChlidView(line)
            }
            keepOne = false
            firstLine = false
            bnView = null
        }
        para.setSize(maxWidth, paraHeight)
        Log.e("LayoutKit.214", "para.setEndOffset = " + lineStart)
        para.setEndOffset(lineStart)
        return breakType
    }

    fun buildLine(doc: IDocument, para: ParagraphView): Int {
        val breakType = WPViewConstant.BREAK_NO.toInt()
        return breakType
    }

    /**
     * 布局行
     * @param docAttr       文档属性
     * @param pageAttr      页面属性
     * @param paraAttr      段落属性
     * @param line          布局的行
     * @param x             布局开始x值
     * @param y             布局开始y值
     * @param w             布局的宽度
     * @param h             布局的高度
     * @param maxEnd        布局的最大结束位置
     * @param flag          布局标记
     */
    fun layoutLine(control: IControl, doc: IDocument, docAttr: DocAttr, pageAttr: PageAttr, paraAttr: ParaAttr, line: LineView,
                   bnView: BNView?, x: Int, y: Int, w: Int, h: Int, maxEnd: Long, flag: Int): Int {
        var flag = flag
        var breakType = WPViewConstant.BREAK_NO.toInt()
        var dx = 0
        val dy = 0
        var spanW = w
        val start = line.getStartOffset(null)
        var pos = start
        val elem = line.getElement()
        var leaf: LeafView?
        var run: IElement?
        var lineWidth = 0
        var lineHeigth = 0
        var lineHeigthExceptShape = 0
        var keepOne = ViewKit.instance().getBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt())
        while (spanW > 0 && pos < maxEnd || keepOne) {
            run = doc.getLeaf(pos)
            if (run == null) {
                break
            }
            leaf = ViewFactory.createView(control, run, elem, WPViewConstant.LEAF_VIEW.toInt()) as LeafView
            line.appendChlidView(leaf)
            Log.e("LayoutKit.layoutLine", "leaf.setStartOffset = " + pos)
            leaf.setStartOffset(pos)
            leaf.setLocation(dx, dy)

            breakType = leaf.doLayout(docAttr, pageAttr, paraAttr, dx, dy, spanW, h, maxEnd, flag)
            if ((leaf.getType() == WPViewConstant.OBJ_VIEW || leaf.getType() == WPViewConstant.SHAPE_VIEW)
                && breakType == WPViewConstant.BREAK_LIMIT.toInt()
            ) {
                line.deleteView(leaf, true)
                breakType = WPViewConstant.BREAK_NO.toInt()
                break
            }
            pos = leaf.getEndOffset(null)
            Log.e("LayoutKit.360", "line.setEndOffset = " + pos)
            line.setEndOffset(pos)
            val leafWidth = leaf.getLayoutSpan(WPViewConstant.X_AXIS)
            lineWidth += leafWidth
            dx += leafWidth
            lineHeigth = Math.max(lineHeigth, leaf.getLayoutSpan(WPViewConstant.Y_AXIS))
            if (leaf.getType() != WPViewConstant.OBJ_VIEW && leaf.getType() != WPViewConstant.SHAPE_VIEW) {
                lineHeigthExceptShape = Math.max(lineHeigthExceptShape, leaf.getLayoutSpan(WPViewConstant.Y_AXIS))
            }
            spanW -= leafWidth
            if (breakType == WPViewConstant.BREAK_LIMIT.toInt()
                || breakType == WPViewConstant.BREAK_ENTER.toInt()
                || breakType == WPViewConstant.BREAK_PAGE.toInt()
            ) {
                break
            }
            flag = ViewKit.instance().setBitValue(flag, WPViewConstant.LAYOUT_FLAG_KEEPONE.toInt(), false)
            keepOne = false
        }
        line.setSize(lineWidth, lineHeigth)
        line.setHeightExceptShape(lineHeigthExceptShape)
        // 布局宽度受限，需要进行
        if (breakType == WPViewConstant.BREAK_LIMIT.toInt()) {
            var str = elem!!.getText(doc)
            val paraStart = elem!!.getStartOffset()
            Log.e("LayoutKit.381 str", "" + str + " ; paraStart = " + paraStart + " ; start = " + start)
            if (start >= paraStart) {
                str = str!!.substring((start - paraStart).toInt())
                val newPos = FontKit.instance().findBreakOffset(str, (pos - start).toInt()) + start
                adjustLine(line, newPos)
            }
        }
        line.layoutAlignment(docAttr, pageAttr, paraAttr, bnView, w, flag)
        return breakType
    }

    private fun createBNView(control: IControl, doc: IDocument, docAttr: DocAttr, pageAttr: PageAttr, paraAttr: ParaAttr,
                             para: ParagraphView, x: Int, y: Int, w: Int, h: Int, flag: Int): BNView? {
        if (paraAttr.listID >= 0 && paraAttr.listLevel >= 0
            || paraAttr.pgBulletID >= 0
        ) {
            val bnView = ViewFactory.createView(control, null, null, WPViewConstant.BN_VIEW.toInt()) as BNView

            bnView.doLayout(doc, docAttr, pageAttr, paraAttr, para, x, y, w, h, flag)
            para.setBNView(bnView)

            return bnView
        }
        return null
    }

    /**
     * 根据新的断行位置，调整视图
     *
     * @param line
     * @param newPos
     */
    private fun adjustLine(line: LineView, newPos: Long) {
        var view: IView? = line.getLastView()
        var temp: IView?
        var lineWidth = line.getWidth()
        while (view != null && view.getStartOffset(null) >= newPos) {
            temp = view.getPreView()
            lineWidth -= view.getWidth()
            line.deleteView(view, true)
            view = temp
        }
        // 同一leaf，需要折分
        var leafWidth: Int
        if (view != null && view.getEndOffset(null) > newPos) {
            Log.e("LayoutKit.456", "view.setEndOffset = " + newPos)
            view.setEndOffset(newPos)
            lineWidth -= view.getWidth()
            leafWidth = (view as LeafView).getTextWidth().toInt()
            // 重置Leaf的宽度
            view.setWidth(leafWidth)
            lineWidth += leafWidth
        }
        Log.e("LayoutKit.464", "view.setEndOffset = " + newPos)
        line.setEndOffset(newPos)
        line.setWidth(lineWidth)
    }

    private fun getLineIndent(control: IControl, bnViewWidth: Int, paraAttr: ParaAttr, firstLine: Boolean): Int {
        // 首先缩进
        if (firstLine) {
            val bnWidth = if (bnViewWidth <= 0) 0 else bnViewWidth
            return if (paraAttr.specialIndentValue > 0) {
                paraAttr.specialIndentValue + bnWidth
            } else {
                bnWidth
            }
        }
        // 悬挂缩进
        else if (!firstLine && paraAttr.specialIndentValue < 0) {
            if (bnViewWidth > 0 && control.getApplicationType() == MainConstant.APPLICATION_TYPE_PPT) {
                return bnViewWidth
            }
            // 悬挂缩进 值也设置到左缩进，左缩进需要减去悬挂缩进
            return -paraAttr.specialIndentValue
        }
        return 0
    }
}
