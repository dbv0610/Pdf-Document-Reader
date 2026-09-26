/*
 * 文件名称:          CellBorder.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:55:45
 */
package com.wxiwei.office.ss.model.style

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-22
 * 负责人:           jqin
 */
class CellBorder {
    private var left: BorderStyle? = BorderStyle()
    private var top: BorderStyle? = BorderStyle()
    private var right: BorderStyle? = BorderStyle()
    private var bottom: BorderStyle? = BorderStyle()

    fun setLeftBorder(left: BorderStyle?) {
        this.left = left
    }

    fun getLeftBorder(): BorderStyle? = left

    fun setTopBorder(top: BorderStyle?) {
        this.top = top
    }

    fun getTopBorder(): BorderStyle? = top

    fun setRightBorder(right: BorderStyle?) {
        this.right = right
    }

    fun getRightBorder(): BorderStyle? = right

    fun setBottomBorder(bottom: BorderStyle?) {
        this.bottom = bottom
    }

    fun getBottomBorder(): BorderStyle? = bottom

    /**
     *
     */
    fun dispose() {
        if (left != null) {
            left!!.dispose()
            left = null
        }

        if (top != null) {
            top!!.dispose()
            top = null
        }

        if (right != null) {
            right!!.dispose()
            right = null
        }

        if (bottom != null) {
            bottom!!.dispose()
            bottom = null
        }
    }
}
