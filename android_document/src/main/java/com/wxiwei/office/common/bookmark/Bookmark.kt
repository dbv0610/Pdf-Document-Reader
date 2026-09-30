/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          	Bookmark.java
 *  
 * 编译器:            android2.2
 * 时间:             	下午4:55:48
 */
package com.wxiwei.office.common.bookmark

/**
 * book mark
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
 * 日期:            	2013-5-9
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
class Bookmark
    (
    /**
     * @param name The name to set.
     */
    //
    var name: String?,
    /**
     * @param start The start to set.
     */
    //
    var start: Long, end: Long
) {
    /**
     * @return Returns the start.
     */
    /**
     * @return Returns the end.
     */
    /**
     * @param end The end to set.
     */
    //
    var end: Long
    /**
     * @return Returns the name.
     */

    /**
     * 
     * @param name
     * @param start
     * @param end
     */
    init {
        this.start = start
        this.end = end
    }
}
