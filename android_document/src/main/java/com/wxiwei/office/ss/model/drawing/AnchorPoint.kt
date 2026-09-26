/*
 * 文件名称:          CellAnchor.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:32:00
 */
package com.wxiwei.office.ss.model.drawing

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-29
 * 负责人:           jqin
 */
open class AnchorPoint {
    //
    @JvmField
    protected var row: Int = 0
    //
    @JvmField
    protected var col: Short = 0
    //
    @JvmField
    protected var dx: Int = 0
    //
    @JvmField
    protected var dy: Int = 0

    constructor()

    constructor(col: Short, row: Int, dx: Int, dy: Int) {
        this.row = row
        this.col = col
        this.dx = dx
        this.dy = dy
    }

    /**
     *
     * @param row
     */
    fun setRow(row: Int) {
        this.row = row
    }

    /**
     *
     * @return
     */
    fun getRow(): Int = row

    /**
     *
     * @param col
     */
    fun setColumn(col: Short) {
        this.col = col
    }

    /**
     *
     * @return
     */
    fun getColumn(): Short = col

    /**
     *
     * @param dx
     */
    fun setDX(dx: Int) {
        this.dx = dx
    }

    /**
     *
     * @return
     */
    fun getDX(): Int = dx

    fun setDY(dy: Int) {
        this.dy = dy
    }

    /**
     *
     * @return
     */
    fun getDY(): Int = dy

    fun dispose() {
    }
}
