/*
 * 文件名称:          ArbitraryPolygonShape.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:46:04
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.common.autoshape.ExtendPath

/**
 * TODO: 文件注释
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
 * 日期:            2012-10-9
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
open class ArbitraryPolygonShape : LineShape() {
    fun appendPath(path: ExtendPath?) {
        this.paths.add(path)
    }

    //
    val paths: MutableList<ExtendPath?>

    init {
        paths = ArrayList<ExtendPath?>()
    }
}
