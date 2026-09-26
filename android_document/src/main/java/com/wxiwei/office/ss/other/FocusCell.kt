/*
 * 文件名称:          HeaderInformation.java
 *
 * 编译器:            android2.2
 * 时间:              下午1:39:22
 */
package com.wxiwei.office.ss.other

import android.graphics.Rect

/**
 * TODO: cell include header cell  and sheet cell, used when changing row or column size
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-13
 * 负责人:           jqin
 */
class FocusCell : Cloneable {
    //
    private var headerType: Short = UNKNOWN
    //row or column index
    private var row = 0
    private var col = 0
    private var area: Rect? = null

    constructor()

    constructor(type: Short, area: Rect?, row: Int, col: Int) {
        this.headerType = type
        this.area = area
        if (type == ROWHEADER) {
            this.row = row
        } else if (type == COLUMNHEADER) {
            this.col = col
        }
    }

    public override fun clone(): FocusCell {
        val rect = Rect(area)
        return FocusCell(headerType, rect, row, col)
    }

    /**
     *
     * @param type
     * ROWHEADER
     * COLUMNHEADER
     */
    fun setType(type: Short) {
        headerType = type
    }

    /**
     *
     * @return
     * * ROWHEADER
     * COLUMNHEADER
     */
    fun getType(): Int = headerType.toInt()

    /**
     * set row index
     * @param row
     */
    fun setRow(row: Int) {
        if (headerType == ROWHEADER || headerType == CELL) {
            this.row = row
        }
    }

    /**
     * get row index
     * @return
     */
    fun getRow(): Int {
        if (headerType == ROWHEADER || headerType == CELL) {
            return row
        }

        return -1
    }

    /**
     * set column index
     * @param col
     */
    fun setColumn(col: Int) {
        if (headerType == COLUMNHEADER || headerType == CELL) {
            this.col = col
        }
    }

    /**
     * get column index
     * @return
     */
    fun getColumn(): Int {
        if (headerType == COLUMNHEADER || headerType == CELL) {
            return col
        }

        return -1
    }

    fun setRect(area: Rect?) {
        this.area = area
    }

    fun getRect(): Rect? = area

    fun dispose() {
    }

    companion object {
        const val UNKNOWN: Short = 0
        const val ROWHEADER: Short = (UNKNOWN + 1).toShort()
        const val COLUMNHEADER: Short = (ROWHEADER + 1).toShort()
        const val CELL: Short = (COLUMNHEADER + 1).toShort()
    }
}
