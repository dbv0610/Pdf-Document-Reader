/*
 * 文件名称:          DrawingCell.java
 *
 * 编译器:            android2.2
 */
package com.wxiwei.office.ss.other

/**
 * TODO: 这个类主要是管理单个Cell（或者合并单元格中的其中一个单元格）的信息，滚动后可见部分的宽和高
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2011-12-28
 * 负责人:           jqin
 */
class DrawingCell {
    //
    private var rowIndex = 0
    private var columnIndex = 0

    //left top postion
    private var left = 0f
    private var top = 0f

    private var width = 0f
    private var height = 0f

    //
    private var visibleWidth = 0f
    private var visibleHeight = 0f

    fun reset() {
        setRowIndex(0)
        setColumnIndex(0)
        setLeft(0f)
        setTop(0f)
        setWidth(0f)
        setHeight(0f)
        setVisibleWidth(0f)
        setVisibleHeight(0f)
    }

    /**
     * @return Returns the rowIndex.
     */
    fun getRowIndex(): Int = rowIndex

    /**
     * @param rowIndex The rowIndex to set.
     */
    fun setRowIndex(rowIndex: Int) {
        this.rowIndex = rowIndex
    }

    /**
     *
     */
    fun increaseRow() {
        rowIndex = rowIndex + 1
    }

    /**
     *
     */
    fun increaseColumn() {
        columnIndex = columnIndex + 1
    }

    /**
     *
     */
    fun getColumnIndex(): Int = columnIndex

    /**
     * @param columnIndex The columnIndex to set.
     */
    fun setColumnIndex(columnIndex: Int) {
        this.columnIndex = columnIndex
    }

    /**
     * @return Returns the left.
     */
    fun getLeft(): Float = left

    /**
     * @param left The left to set.
     */
    fun setLeft(left: Float) {
        this.left = left
    }

    fun increaseLeftWithVisibleWidth() {
        left = left + visibleWidth
    }

    /**
     * @return Returns the top.
     */
    fun getTop(): Float = top

    /**
     * @param top The top to set.
     */
    fun setTop(top: Float) {
        this.top = top
    }

    fun increaseTopWithVisibleHeight() {
        top = top + visibleHeight
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
     * @return Returns the visibleWidth.
     */
    fun getVisibleWidth(): Float = visibleWidth

    /**
     * @param visibleWidth The visibleWidth to set.
     */
    fun setVisibleWidth(visibleWidth: Float) {
        this.visibleWidth = visibleWidth
    }

    /**
     * @return Returns the visibleHeight.
     */
    fun getVisibleHeight(): Float = visibleHeight

    /**
     * @param visibleHeight The visibleHeight to set.
     */
    fun setVisibleHeight(visibleHeight: Float) {
        this.visibleHeight = visibleHeight
    }

    fun dispose() {
    }
}
