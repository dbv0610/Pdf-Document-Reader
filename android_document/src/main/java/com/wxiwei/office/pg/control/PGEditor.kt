/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.pg.control

import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.pg.animate.ShapeAnimation
import com.wxiwei.office.simpletext.control.Highlight
import com.wxiwei.office.simpletext.control.IHighlight
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.view.STRoot
import com.wxiwei.office.system.IControl

class PGEditor(private var pgView: Presentation?) : IWord {
    private var editorTextBox: TextBox? = null
    private var highlight: IHighlight? = Highlight(this)
    private var paraAnimation: MutableMap<Int, IAnimation>? = null

    override fun getHighlight(): IHighlight? = highlight

    override fun modelToView(offset: Long, rect: Rectangle, isBack: Boolean): Rectangle {
        editorTextBox?.let { box ->
            val root: STRoot? = box.rootView
            root?.modelToView(offset, rect, isBack)
            rect.x += box.bounds!!.x
            rect.y += box.bounds!!.y
        }
        return rect
    }

    override fun getDocument(): IDocument? = null

    override fun getText(start: Long, end: Long): String? {
        val elem = editorTextBox?.element ?: return null
        if (elem.getEndOffset() - elem.getStartOffset() > 0) {
            val str = elem.getText(null)
            if (str != null) return str.substring(maxOf(start, elem.getStartOffset()).toInt(), minOf(end, elem.getEndOffset()).toInt())
        }
        return null
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        val view = pgView ?: return -1
        val shape = view.getCurrentSlide()?.getShape(x, y)
        if (shape != null && shape.type == AbstractShape.SHAPE_TEXTBOX) {
            val root = (shape as TextBox).rootView
            if (root != null) return root.viewToModel(x - shape.bounds!!.x, y - shape.bounds!!.y, isBack)
        }
        return -1
    }

    fun getEditorTextBox(): TextBox? = editorTextBox
    fun setEditorTextBox(editorBox: TextBox?) { editorTextBox = editorBox }

    override fun getEditType(): Byte = MainConstant.APPLICATION_TYPE_PPT

    fun setShapeAnimation(paraAnimation: MutableMap<Int, IAnimation>?) { this.paraAnimation = paraAnimation }

    override fun getParagraphAnimation(paragraphID: Int): IAnimation? {
        if (pgView != null && paraAnimation != null) {
            return paraAnimation?.get(paragraphID)
                ?: paraAnimation?.get(ShapeAnimation.Para_All)
                ?: paraAnimation?.get(ShapeAnimation.Para_BG)
        }
        return null
    }

    override fun getTextBox(): IShape? = editorTextBox

    fun clearAnimation() { paraAnimation?.clear() }

    override fun getControl(): IControl? = pgView?.getControl()
    fun getPGView(): Presentation? = pgView

    override fun dispose() {
        editorTextBox = null
        highlight?.dispose(); highlight = null
        pgView = null
        paraAnimation?.clear(); paraAnimation = null
    }
}
