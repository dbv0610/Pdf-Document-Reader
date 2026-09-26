/*
 * 文件名称:          LeafElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午9:00:44
 */
package com.wxiwei.office.simpletext.model

import com.wxiwei.office.constant.wp.WPModelConstant

/**
 * Leaf Element
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-28
 *
 * 负责人:          ljj8494
 */
open class LeafElement(text: String?) : AbstractElement() {
    //
    private var text: String? = text

    /**
     *
     */
    override fun getType(): Short {
        return WPModelConstant.LEAF_ELEMENT
    }

    /**
     *
     */
    override fun getText(doc: IDocument?): String? {
        return text
    }

    /**
     *
     */
    fun setText(text: String) {
        this.text = text
        this.setEndOffset(getStartOffset() + text.length)
    }

    /**
     *
     */
    override fun dispose() {
        super.dispose()
        text = null
    }
}
