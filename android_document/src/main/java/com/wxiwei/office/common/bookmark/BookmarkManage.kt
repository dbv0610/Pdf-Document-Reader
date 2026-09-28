/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          	BookmarkManage.java
 *  
 * 编译器:            android2.2
 * 时间:             	下午5:05:13
 */
package com.wxiwei.office.common.bookmark

/**
 * book mark manage
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
class BookmarkManage {
    /**
     * 
     */
    fun addBookmark(bm: Bookmark) {
        bms!!.put(bm.name, bm)
    }

    /**
     * 
     */
    fun getBookmark(name: String?): Bookmark? {
        return bms!!.get(name)
    }

    val bookmarkCount: Int
        /**
         * 
         */
        get() = bms!!.size

    /**
     * 
     */
    fun dispose() {
        if (bms != null) {
            bms!!.clear()
            bms = null
        }
    }

    //
    private var bms: MutableMap<String?, Bookmark?>?

    /**
     * 
     */
    init {
        bms = HashMap<String?, Bookmark?>()
    }
}
