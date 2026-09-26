/*
 * 文件名称:          ViewFactory.java
 *
 * 编译器:            android2.2
 * 时间:              下午3:22:30
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.system.*

import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.WPAutoShape
import com.wxiwei.office.constant.wp.AttrIDConstant
import com.wxiwei.office.constant.wp.WPModelConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.model.AttrManage
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.system.IControl

/**
 * 视图工厂
 */
object ViewFactory {

    /**
     * @param elem          model元素
     * @param paraElem      model中段落元素，只有创建leafView才需要，其它view时，传null就可以了
     * @param viewType      视图类型
     */
    @JvmStatic
    fun createView(control: IControl, elem: IElement?, paraElem: IElement?, viewType: Int): IView? {
        var view: IView? = null
        when (viewType) {
            WPViewConstant.PAGE_VIEW.toInt() -> view = PageView(elem!!)

            WPViewConstant.PARAGRAPH_VIEW.toInt() -> view = ParagraphView(elem!!)

            WPViewConstant.LINE_VIEW.toInt() -> view = LineView(elem!!)

            WPViewConstant.LEAF_VIEW.toInt() -> {
                if (AttrManage.instance().hasAttribute(elem!!.getAttribute(), AttrIDConstant.FONT_SHAPE_ID)) {
                    val shape = control.getSysKit().getWPShapeManage().getShape(AttrManage.instance().getShapeID(elem.getAttribute()))
                    if (shape != null) {
                        if (shape.getType().toInt() == AbstractShape.SHAPE_AUTOSHAPE.toInt()
                            || shape.getType().toInt() == AbstractShape.SHAPE_CHART.toInt()
                        ) {
                            view = ShapeView(paraElem!!, elem, shape as AutoShape)
                        } else if (shape.getType().toInt() == AbstractShape.SHAPE_PICTURE.toInt()) {
                            view = ObjView(paraElem!!, elem, shape as WPAutoShape)
                        }
                    } else {
                        view = ObjView(paraElem!!, elem, null)
                    }
                } else if (AttrManage.instance().hasAttribute(elem.getAttribute(), AttrIDConstant.FONT_ENCLOSE_CHARACTER_TYPE_ID)) {
                    view = EncloseCharacterView(paraElem!!, elem)
                } else {
                    view = LeafView(paraElem!!, elem)
                }
            }

            WPViewConstant.TABLE_VIEW.toInt() -> {
                view = if (elem!!.getType().toInt() == WPModelConstant.TABLE_ELEMENT.toInt()) {
                    TableView(elem)
                } else {
                    ParagraphView(elem)
                }
            }

            WPViewConstant.TABLE_ROW_VIEW.toInt() -> view = RowView(elem!!)

            WPViewConstant.TABLE_CELL_VIEW.toInt() -> view = CellView(elem!!)

            WPViewConstant.TITLE_VIEW.toInt() -> view = TitleView(elem!!)

            WPViewConstant.BN_VIEW.toInt() -> view = BNView()

            else -> {
            }
        }
        return view
    }

    @JvmStatic
    fun dispose() {
    }
}
