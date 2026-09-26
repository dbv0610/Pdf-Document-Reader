/*
 * 文件名称:          RowProperty.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:10:46
 */
package com.wxiwei.office.ss.model.baseModel

import com.wxiwei.office.ss.other.ExpandedCellRangeAddress

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-9-3
 * 负责人:           jqin
 */
class RowProperty {
    /**
     *
     */
    // Rows are numerous in large worksheets. A HashMap per row was a major
    // source of heap pressure, even though this property object has only
    // three boolean values and one optional list.
    private var zeroHeight = false
    private var completed = false
    private var initExpandedRangeAddr = false
    private var expandedRangeAddr: MutableList<ExpandedCellRangeAddress>? = null

    /**
     *
     * @param rowPropID
     * @param value
     */
    fun setRowProperty(rowPropID: Short, value: Any?) {
        when (rowPropID) {
            ROWPROPID_ZEROHEIGHT -> zeroHeight = value as? Boolean ?: false
            ROWPROPID_COMPLETED -> completed = value as? Boolean ?: false
            ROWPROPID_INITEXPANDEDRANGEADDR -> initExpandedRangeAddr = value as? Boolean ?: false
            ROWPROPID_EXPANDEDRANGEADDRLIST -> {
                if (expandedRangeAddr == null) expandedRangeAddr = ArrayList()
                expandedRangeAddr!!.add(value as ExpandedCellRangeAddress)
            }
        }
    }

    /**
     *
     * @return
     */
    fun isZeroHeight(): Boolean {
        return zeroHeight
    }

    /**
     *
     * @return
     */
    fun isCompleted(): Boolean {
        return completed
    }

    /**
     *
     * @return
     */
    fun isInitExpandedRangeAddr(): Boolean {
        return initExpandedRangeAddr
    }

    /**
     *
     * @return
     */
    fun getExpandedCellCount(): Int {
        return expandedRangeAddr?.size ?: 0
    }

    /**
     *
     * @param index
     * @return
     */
    fun getExpandedCellRangeAddr(index: Int): ExpandedCellRangeAddress? {
        return expandedRangeAddr?.getOrNull(index)
    }

    /**
     *
     */
    fun dispose() {
        expandedRangeAddr?.forEach { it.dispose() }
    }

    companion object {
        const val ROWPROPID_ZEROHEIGHT: Short = 0
        const val ROWPROPID_COMPLETED: Short = 1
        const val ROWPROPID_INITEXPANDEDRANGEADDR: Short = 2
        const val ROWPROPID_EXPANDEDRANGEADDRLIST: Short = 3
    }
}
