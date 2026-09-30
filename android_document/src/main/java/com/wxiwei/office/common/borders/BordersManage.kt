/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          	BordersManage.java
 *  
 * 编译器:            android2.2
 * 时间:             	上午9:38:51
 */
package com.wxiwei.office.common.borders

/**
 * border manage
 * 
 * 
 * 
 * 
 * Read版本:        	Office engine V1.0
 * 
 * 
 * 作者:            	ljj8494
 * 
 * 
 * 日期:            	2013-3-18
 * 
 * 
 * 负责人:          	ljj8494
 * 
 * 
 * 负责小组:        	TMC
 * 
 * 
 * 
 * 
 */
class BordersManage {
    /**
     * 
     */
    fun addBorders(bs: Borders?): Int {
        val size = borders!!.size
        borders!!.add(bs)
        return size
    }

    /**
     * 
     * @param index
     * @return
     */
    fun getBorders(index: Int): Borders? {
        return borders!!.get(index)
    }

    /**
     * 
     */
    fun dispose() {
        if (borders != null) {
            borders!!.clear()
            borders = null
        }
    }

    private var borders: MutableList<Borders?>? = ArrayList<Borders?>()
}
