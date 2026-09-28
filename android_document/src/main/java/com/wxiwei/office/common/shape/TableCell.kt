/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           TableCell.java
 *  
 * 编译器:             android2.2
 * 时间:               下午3:02:39
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.java.awt.Rectanglef

/**
 * Represents a table cell
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-4-11
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class TableCell

/**
 * 
 */
{
    /**
     * 
     */
    fun dispose() {
        if (this.text != null) {
            text!!.dispose()
            this.text = null
        }
        this.bounds = null
        if (this.backgroundAndFill != null) {
            backgroundAndFill!!.dispose()
            this.backgroundAndFill = null
        }
    }

    /**
     * 
     * @return
     */
    /**
     * 
     * @param borderLColor
     */
    // border left color
    var leftLine: Line? = null
    /**
     * 
     * @return
     */
    /**
     * 
     * @param borderRColor
     */
    // border right color
    var rightLine: Line? = null
    /**
     * 
     * @return
     */
    /**
     * 
     * @param borderTColor
     */
    // border top color
    var topLine: Line? = null
    /**
     * 
     * @return
     */
    /**
     * 
     * @param borderBColor
     */
    // border bottom color
    var bottomLine: Line? = null
    /**
     * 
     * @return
     */
    /**
     * 
     * @param textBox
     */
    // text
    var text: TextBox? = null
    /**
     * 
     * 
     */
    /**
     * 
     * 
     */
    // size of this cell
    var bounds: Rectanglef? = null
    /**
     * @return Returns the bgFill.
     */
    /**
     * @param bgFill The bgFill to set.
     */
    // background
    var backgroundAndFill: BackgroundAndFill? = null
}
