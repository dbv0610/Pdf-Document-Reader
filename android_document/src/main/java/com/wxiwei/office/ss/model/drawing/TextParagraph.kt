/*
 * 文件名称:          TextParagraph.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:10:21
 */
package com.wxiwei.office.ss.model.drawing

import com.wxiwei.office.simpletext.font.Font
import com.wxiwei.office.ss.model.style.Alignment

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-3-1
 * 负责人:           jqin
 */
class TextParagraph {
    private var textRun: String? = null
    private var font: Font? = null
    private var align: Alignment? = Alignment()

    /**
     *
     * @param textRun
     */
    fun setTextRun(textRun: String?) {
        this.textRun = textRun
    }

    /**
     *
     * @return
     */
    fun getTextRun(): String? = textRun

    /**
     *
     * @param font
     */
    fun setFont(font: Font?) {
        this.font = font
    }

    /**
     *
     * @return
     */
    fun getFont(): Font? = font

    /**
     *
     * @param horizon
     */
    fun setHorizontalAlign(horizon: Short) {
        align!!.setHorizontalAlign(horizon)
    }

    /**
     *
     * @return
     */
    fun getHorizontalAlign(): Short = align!!.getHorizontalAlign()

    /**
     *
     * @param vertical
     */
    fun setVerticalAlign(vertical: Short) {
        align!!.setVerticalAlign(vertical)
    }

    /**
     *
     * @return
     */
    fun getVerticalAlign(): Short = align!!.getVerticalAlign()

    fun dispose() {
        textRun = null
        font = null
        if (align != null) {
            align!!.dispose()
            align = null
        }
    }
}
