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
