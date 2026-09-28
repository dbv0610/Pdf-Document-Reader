/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 锟侥硷拷锟斤拷锟斤拷:          ListData.java
 * 锟斤拷权锟斤拷锟斤拷@2001-2014 锟斤拷锟斤拷锟斤拷锟捷ｏ拷锟狡硷拷锟斤拷锟睫癸拷司
 * 锟斤拷锟斤拷锟斤拷:            android2.2
 * 时锟斤拷:              锟斤拷锟斤拷10:49:31
 */
package com.wxiwei.office.common.bulletnumber

/**
 * bullet and number object
 * 
 * 
 * 
 * 
 * Read锟芥本:        Read V1.0
 * 
 * 
 * 锟斤拷锟斤拷:            ljj8494
 * 
 * 
 * 锟斤拷锟斤拷:            2012-6-18
 * 
 * 
 * 锟斤拷锟斤拷锟斤拷:          ljj8494
 * 
 * 
 * 锟斤拷锟斤拷小锟斤拷:
 * 
 * 
 * 
 * 
 */
class ListData {
    /**
     * 
     * @param level
     * @return
     */
    fun getLevel(level: Int): ListLevel? {
        return levels?.getOrNull(level)
    }

    /**
     * 
     */
    fun resetForNormalView() {
        if (levels != null) {
            for (level in levels.orEmpty()) {
                level?.normalParaCount = 0
            }
        }
    }

    /**
     * 
     */
    fun dispose() {
        if (levels != null) {
            for (level in levels.orEmpty()) {
                level?.dispose()
            }
            levels = null
        }
    }

    /**
     * @return Returns the listID.
     */
    /**
     * @param listID The listID to set.
     */
    // ID
    var listID: Int = 0
    /**
     * @return Returns the simpleList.
     */
    /**
     * @param simpleList The simpleList to set.
     */
    // = 0, nine level, = 1, one level
    var simpleList: Byte = 0
    /**
     * @return Returns the linkStyle.
     */
    /**
     * @param linkStyle The linkStyle to set.
     */
    // is this level link style
    var linkStyle: ShortArray? = null
    /**
     * @return Returns the linkStyleID.
     */
    /**
     * @param linkStyleID The linkStyleID to set.
     */
    //
    var linkStyleID: Short = -1
    /**
     * @return Returns the isNumber.
     */
    /**
     * @param isNumber The isNumber to set.
     */
    // is this number
    var isNumber: Boolean = false
    /**
     * @return Returns the levels.
     */
    /**
     * @param levels The levels to set.
     */
    // level 
    var levels: Array<out ListLevel?>? = null
    /**
     * @return Returns the preParaLevel.
     */
    /**
     * @param preParaLevel The preParaLevel to set.
     */
    // previous paragraph level of same listID
    var preParaLevel: Byte = 0
    /**
     * @return Returns the normalPreParaLevel.
     */
    /**
     * @param normalPreParaLevel The normalPreParaLevel to set.
     */
    //
    var normalPreParaLevel: Byte = 0
}
