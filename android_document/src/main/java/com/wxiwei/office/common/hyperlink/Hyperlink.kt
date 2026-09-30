/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Hyperlink.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:12:13
 */
package com.wxiwei.office.common.hyperlink

/**
 * Hyperlink 类
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2012-2-14
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class Hyperlink {
    /**
     * 
     */
    fun dispose() {
        address = null
        title = null
    }

    /**
     * @return Returns the id.
     */
    /**
     * @param id The id to set.
     */
    // ID
    var id: Int = -1
    /**
     * @return Returns the type.
     */
    /**
     * @param type The type to set.
     */
    // hyperlink type
    var linkType: Int = 0
    /**
     * @return Returns the address.
     */
    /**
     * @param address The address to set.
     */
    // hyperlink address;
    var address: String? = null
    /**
     * @return Returns the title.
     */
    /**
     * @param title The title to set.
     */
    // hyperlink title
    var title: String? = null

    companion object {
        // Link to a existing file or web page
        const val LINK_URL: Int = 1
        // Link to a place in this document
        const val LINK_DOCUMENT: Int = 2
        // Link to an E-mail address
        const val LINK_EMAIL: Int = 3
        // Link to a file
        const val LINK_FILE: Int = 4
        // Link to a book mark
        const val LINK_BOOKMARK: Int = 5
    }
}
