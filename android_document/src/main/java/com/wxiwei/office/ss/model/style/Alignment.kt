/*
 * 文件名称:          Alignment.java
 *
 * 编译器:            android2.2
 * 时间:              下午2:26:28
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
class Alignment {
    /*
    general (normal) horizontal alignment
    ALIGN_GENERAL = 0x0;
    left-justified horizontal alignment
    ALIGN_LEFT = 0x1;
    center horizontal alignment
    ALIGN_CENTER = 0x2;
    right-justified horizontal alignment
    ALIGN_RIGHT = 0x3;
    fill? horizontal alignment
    ALIGN_JUSTIFY = 0x4;
    justified horizontal alignment
    ALIGN_FILL = 0x5;
    center-selection? horizontal alignment
    ALIGN_CENTER_SELECTION = 0x6;
    top-aligned vertical alignment
    VERTICAL_TOP = 0x1;
    center-aligned vertical alignment
    VERTICAL_CENTER = 0x2;
    bottom-aligned vertical alignment
    VERTICAL_BOTTOM = 0x3;
    vertically justified vertical alignment
    VERTICAL_JUSTIFY = 0x4;
    vertically Distributed vertical alignment
    VERTICAL_DISTRIBUTED = 0x5;
    */

    private var horizontal: Short = 0
    private var vertival: Short = CellStyle.VERTICAL_BOTTOM
    private var rotation: Short = 0
    private var wrapText = false
    private var indent: Short = 0

    /**
     *
     * @param horizontal
     */
    fun setHorizontalAlign(horizontal: Short) {
        this.horizontal = horizontal
    }

    /**
     *
     * @return
     */
    fun getHorizontalAlign(): Short = horizontal

    /**
     *
     * @param v
     */
    fun setVerticalAlign(v: Short) {
        this.vertival = v
    }

    /**
     *
     * @return
     */
    fun getVerticalAlign(): Short = vertival

    /**
     *
     * @param rotation
     */
    fun setRotation(rotation: Short) {
        this.rotation = rotation
    }

    /**
     *
     * @return
     */
    fun getRotaion(): Short = rotation

    /**
     *
     * @param wrapText
     */
    fun setWrapText(wrapText: Boolean) {
        this.wrapText = wrapText
    }

    /**
     *
     * @return
     */
    fun isWrapText(): Boolean = wrapText

    /**
     *
     * @param indent
     */
    fun setIndent(indent: Short) {
        this.indent = indent
    }

    /**
     *
     * @return
     */
    fun getIndent(): Short = indent

    /**
     *
     */
    fun dispose() {
    }
}
