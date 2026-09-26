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
            val root: STRoot? = box.getRootView()
            root?.modelToView(offset, rect, isBack)
            rect.x += box.getBounds().x
            rect.y += box.getBounds().y
        }
        return rect
    }

    override fun getDocument(): IDocument? = null

    override fun getText(start: Long, end: Long): String? {
        val elem = editorTextBox?.getElement() ?: return null
        if (elem.getEndOffset() - elem.getStartOffset() > 0) {
            val str = elem.getText(null)
            if (str != null) return str.substring(maxOf(start, elem.getStartOffset()).toInt(), minOf(end, elem.getEndOffset()).toInt())
        }
        return null
    }

    override fun viewToModel(x: Int, y: Int, isBack: Boolean): Long {
        val view = pgView ?: return -1
        val shape = view.getCurrentSlide()?.getShape(x, y)
        if (shape != null && shape.getType() == AbstractShape.SHAPE_TEXTBOX) {
            val root = (shape as TextBox).getRootView()
            if (root != null) return root.viewToModel(x - shape.getBounds().x, y - shape.getBounds().y, isBack)
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
