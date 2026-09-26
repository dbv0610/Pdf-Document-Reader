/*
 * 文件名称:          TitleView.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.wp.view

import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.simpletext.control.IWord
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.IElement
import com.wxiwei.office.simpletext.view.AbstractView
import com.wxiwei.office.simpletext.view.IView
import com.wxiwei.office.system.IControl

/**
 * title view (for header and footer)
 */
class TitleView(elem: IElement) : AbstractView() {

    /**
     *
     */
    private var pageRoot: IView? = null

    init {
        this.elem = elem
    }

    override fun getType(): Short {
        return WPViewConstant.TITLE_VIEW
    }

    /**
     * 得到组件
     */
    override fun getContainer(): IWord? {
        return pageRoot?.getContainer()
    }

    /**
     * 得到组件
     */
    override fun getControl(): IControl? {
        return pageRoot?.getControl()
    }

    /**
     * 得到model
     */
    override fun getDocument(): IDocument? {
        return pageRoot?.getDocument()
    }

    fun setPageRoot(root: IView?) {
        pageRoot = root
    }

    override fun dispose() {
        super.dispose()
        pageRoot = null
    }
}
