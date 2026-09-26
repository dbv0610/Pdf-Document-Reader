/*
 * 文件名称:          MergedCell.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:09:52
 */
package com.wxiwei.office.ss.other

/**
 * TODO: 这个类主要管理合并单元格完全不可见部分的宽度和高度，配合ScrolledCell类决定了当前合并单元格的可见部分
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2011-12-12
 * 负责人:           jqin
 */
class MergeCell {
    private var width = 0f
    private var height = 0f
    private var frozenRow = false
    private var frozenColumn = false
    //frozen cell:   no visible width in the right and no visible height in the bottom
    //free cell:     no vislble width in the left and no visible height in the top
    // 合并单元格不可见单元格宽度
    private var novisibleWidth = 0f
    // 合并单元格不可见单元格高度
    private var noVisibleHeight = 0f

    fun reset() {
        setWidth(0f)
        setHeight(0f)
        setFrozenRow(false)
        setFrozenColumn(false)
        setNovisibleWidth(0f)
        setNoVisibleHeight(0f)
    }

    /**
     * @return Returns the width.
     */
    fun getWidth(): Float = width

    /**
     * @param width The width to set.
     */
    fun setWidth(width: Float) {
        this.width = width
    }

    /**
     * @return Returns the height.
     */
    fun getHeight(): Float = height

    /**
     * @param height The height to set.
     */
    fun setHeight(height: Float) {
        this.height = height
    }

    /**
     *
     */
    fun dispose() {
    }

    /**
     * @return Returns the frozenRow.
     */
    fun isFrozenRow(): Boolean = frozenRow

    /**
     * @param frozenRow The frozenRow to set.
     */
    fun setFrozenRow(frozenRow: Boolean) {
        this.frozenRow = frozenRow
    }

    /**
     * @return Returns the frozenColumn.
     */
    fun isFrozenColumn(): Boolean = frozenColumn

    /**
     * @param frozenColumn The frozenColumn to set.
     */
    fun setFrozenColumn(frozenColumn: Boolean) {
        this.frozenColumn = frozenColumn
    }

    /**
     * @return Returns the novisibleWidth.
     */
    fun getNovisibleWidth(): Float = novisibleWidth

    /**
     * @param novisibleWidth The novisibleWidth to set.
     */
    fun setNovisibleWidth(novisibleWidth: Float) {
        this.novisibleWidth = novisibleWidth
    }

    /**
     * @return Returns the noVisibleHeight.
     */
    fun getNoVisibleHeight(): Float = noVisibleHeight

    /**
     * @param noVisibleHeight The noVisibleHeight to set.
     */
    fun setNoVisibleHeight(noVisibleHeight: Float) {
        this.noVisibleHeight = noVisibleHeight
    }
}
