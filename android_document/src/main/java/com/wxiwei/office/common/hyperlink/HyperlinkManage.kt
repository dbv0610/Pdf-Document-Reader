/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           HyperlinkManage.java
 *  
 * 编译器:             android2.2
 * 时间:               上午10:01:57
 */
package com.wxiwei.office.common.hyperlink

/**
 * hyperlink manage
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
 * 日期:           2012-3-7
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
class HyperlinkManage {
    /**
     * get hyperlink for hlinkID
     */
    fun getHyperlink(hlinkID: Int): Hyperlink? {
        if (hlinkID >= 0 && hlinkID < hlinks!!.size) {
            return hlinks.get(hlinkID)
        }
        return null
    }

    /**
     * 
     */
    fun getHyperlinkIndex(key: String?): Int? {
        return hlinkIndexs!!.get(key)
    }

    /**
     * add hyperlink
     */
    fun addHyperlink(address: String?, linkType: Int): Int {
        val index = hlinkIndexs!!.get(address)
        if (index == null) {
            val hlink = Hyperlink()
            hlink.linkType = linkType
            hlink.address = address

            val size = hlinks!!.size
            hlinks.add(hlink)
            hlinkIndexs.put(address, size)
            return size
        }
        return index
    }

    /**
     * 
     */
    fun dispose() {
        if (hlinks != null) {
            for (hyperlink in hlinks) {
                hyperlink.dispose()
            }
            hlinks.clear()
        }
        if (hlinkIndexs != null) {
            hlinkIndexs.clear()
        }
    }

    //
    private val hlinks: MutableList<Hyperlink>?

    //
    private val hlinkIndexs: MutableMap<String?, Int?>?

    /**
     * 
     */
    init {
        hlinks = ArrayList<Hyperlink>()
        hlinkIndexs = HashMap<String?, Int?>()
    }
}
