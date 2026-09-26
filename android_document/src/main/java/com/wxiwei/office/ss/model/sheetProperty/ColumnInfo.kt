/*
 * 文件名称:          ColumnInfo.java
 *
 * 编译器:            android2.2
 * 时间:              下午5:17:41
 */
package com.wxiwei.office.ss.model.sheetProperty

/**
 * TODO: columns information
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-5-15
 * 负责人:           jqin
 */
class ColumnInfo(
    private var _firstCol: Int,
    private var _lastCol: Int,
    private var _colWidth: Float,
    private var style: Int,
    private var hidden: Boolean
) {
    /**
     * @return Returns the _firstCol.
     */
    fun getFirstCol(): Int = _firstCol

    /**
     * @param _firstCol The _firstCol to set.
     */
    fun setFirstCol(_firstCol: Int) {
        this._firstCol = _firstCol
    }

    /**
     * @return Returns the _lastCol.
     */
    fun getLastCol(): Int = _lastCol

    /**
     * @param _lastCol The _lastCol to set.
     */
    fun setLastCol(_lastCol: Int) {
        this._lastCol = _lastCol
    }

    /**
     * @return Returns the _colWidth.
     */
    fun getColWidth(): Float = _colWidth

    /**
     * @param _colWidth The _colWidth to set.
     */
    fun setColWidth(_colWidth: Float) {
        this._colWidth = _colWidth
    }

    /**
     * @return Returns the hidden.
     */
    fun isHidden(): Boolean = hidden

    /**
     * @param hidden The hidden to set.
     */
    fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    /**
     * @return Returns the style.
     */
    fun getStyle(): Int = style

    /**
     * @param style The style to set.
     */
    fun setStyle(style: Int) {
        this.style = style
    }
}
