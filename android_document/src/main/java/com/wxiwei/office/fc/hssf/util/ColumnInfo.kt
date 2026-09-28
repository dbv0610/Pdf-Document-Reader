/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          ColumnInfo.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:33:07
 */
package com.wxiwei.office.fc.hssf.util

/**
 * TODO: column information(width, hidden)
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-4-20
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class ColumnInfo
    (
    /**
     * @param _firstCol The _firstCol to set.
     */
    var firstCol: Int,
    /**
     * @param _lastCol The _lastCol to set.
     */
    var lastCol: Int,
    /**
     * @param _colWidth The _colWidth to set.
     */
    var colWidth: Int, style: Int, hidden: Boolean
) {
    /**
     * @return Returns the _firstCol.
     */
    /**
     * @return Returns the _lastCol.
     */
    /**
     * @return Returns the _colWidth.
     */
    /**
     * @return Returns the hidden.
     */
    /**
     * @param hidden The hidden to set.
     */
    var isHidden: Boolean
    /**
     * @return Returns the style.
     */
    /**
     * @param style The style to set.
     */
    var style: Int = 0

    init {
        this.style = style
        this.isHidden = hidden
    }
}
