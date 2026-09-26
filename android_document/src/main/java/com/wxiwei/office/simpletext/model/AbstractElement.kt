/*
 * 文件名称:          AbstrctElement.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:46:46
 */
package com.wxiwei.office.simpletext.model

/**
 * 元素的抽象类
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2011-12-29
 *
 * 负责人:          ljj8494
 */
abstract class AbstractElement : IElement {
    //
    @JvmField
    protected var start: Long = 0

    //
    @JvmField
    protected var end: Long = 0

    // 属性集
    @JvmField
    protected var attr: IAttributeSet? = AttributeSetImpl()

    /**
     *
     */
    override fun getType(): Short {
        return -1
    }

    /**
     *
     */
    override fun setStartOffset(start: Long) {
        this.start = start
    }

    /**
     *
     */
    override fun getStartOffset(): Long {
        return start
    }

    /**
     *
     */
    override fun setEndOffset(end: Long) {
        this.end = end
    }

    /**
     *
     */
    override fun getEndOffset(): Long {
        return this.end
    }

    /**
     *
     */
    override fun setAttribute(attrSet: IAttributeSet?) {
        this.attr = attrSet
    }

    /**
     *
     */
    override fun getAttribute(): IAttributeSet {
        return this.attr ?: AttributeSetImpl()
    }

    /**
     *
     */
    override fun getText(doc: IDocument?): String? {
        return null
    }

    /**
     *
     */
    override fun toString(): String {
        return "[" + start + ", " + end + "]：" + getText(null)
    }

    /**
     *
     */
    override fun dispose() {
        if (attr != null) {
            attr!!.dispose()
            attr = null
        }
    }
}
