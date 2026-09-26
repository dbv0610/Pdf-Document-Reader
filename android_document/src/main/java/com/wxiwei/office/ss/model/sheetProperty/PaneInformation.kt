/*
 * 文件名称:          PanInformation.java
 *
 * 编译器:            android2.2
 * 时间:              下午7:49:30
 */
package com.wxiwei.office.ss.model.sheetProperty

/**
 * 窗口冻结
 *
 * Read版本:        Read V1.0
 * 作者:            ljj8494
 * 日期:            2012-2-21
 * 负责人:          ljj8494
 */
class PaneInformation {
    private var topRow: Short = 0
    private var leftColumn: Short = 0
    private var frozen = true

    constructor()

    /**
     * @param top
     * @param left
     * @param frozen
     */
    constructor(top: Short, left: Short, frozen: Boolean) {
        this.topRow = top
        this.leftColumn = left
        this.frozen = frozen
    }

    fun setHorizontalSplitTopRow(topRow: Short) {
        this.topRow = topRow
    }

    /**
     * For a horizontal split returns the top row in the BOTTOM pane.
     * @return 0 if there is no horizontal split, or the top row of the bottom pane.
     */
    fun getHorizontalSplitTopRow(): Short = topRow

    fun setVerticalSplitLeftColumn(leftColumn: Short) {
        this.leftColumn = leftColumn
    }

    /**
     * For a vertical split returns the left column in the RIGHT pane.
     * @return 0 if there is no vertical split, or the left column in the RIGHT pane.
     */
    fun getVerticalSplitLeftColumn(): Short = leftColumn

    fun setFreePane(frozen: Boolean) {
        this.frozen = frozen
    }

    /** Returns true if this is a Freeze pane, false if it is a split pane.
     */
    fun isFreezePane(): Boolean = frozen

    companion object {
        /** Constant for active pane being the lower right*/
        const val PANE_LOWER_RIGHT: Byte = 0
        /** Constant for active pane being the upper right*/
        const val PANE_UPPER_RIGHT: Byte = 1
        /** Constant for active pane being the lower left*/
        const val PANE_LOWER_LEFT: Byte = 2
        /** Constant for active pane being the upper left*/
        const val PANE_UPPER_LEFT: Byte = 3
    }
}
