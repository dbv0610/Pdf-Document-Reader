/*
 * 文件名称:           HyperlinkReader.java
 *  
 * 编译器:             android2.2
 * 时间:               下午1:23:18
 */
package com.wxiwei.office.fc.ppt.reader

import com.wxiwei.office.common.hyperlink.Hyperlink
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.fc.openxml4j.opc.PackageRelationshipTypes
import com.wxiwei.office.system.IControl
import java.util.Hashtable

/**
 * 解析  hyperlink
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
 * 日期:           2012-3-6
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
class HyperlinkReader {
    /**
     * 获取hyperlink
     */
    @Throws(Exception::class)
    fun getHyperlinkList(control: IControl, packagePart: PackagePart) {
        link = Hashtable<String?, Int?>()
        val hyperlinkRelCollection =
            packagePart.getRelationshipsByType(PackageRelationshipTypes.HYPERLINK_PART)
        for (hyperlinkRel in hyperlinkRelCollection) {
            val id = hyperlinkRel.getId()
            if (getLinkIndex(id) < 0) {
                link!!.put(
                    id,
                    control.getSysKit().getHyperlinkManage()
                        .addHyperlink(hyperlinkRel.getTargetURI().toString(), Hyperlink.LINK_URL)
                )
            }
        }
    }

    /**
     * get hyperlink index
     * @param id
     * @return
     */
    fun getLinkIndex(id: String?): Int {
        if (link != null && link!!.size > 0) {
            val index = link!!.get(id)
            if (index != null) {
                return index
            }
        }
        return -1
    }

    /**
     * 
     */
    fun disposs() {
        if (link != null) {
            link!!.clear()
            link = null
        }
    }

    // hyperlink id and index
    private var link: MutableMap<String?, Int?>? = null

    companion object {
        private val hyperlink = HyperlinkReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): HyperlinkReader {
            return hyperlink
        }
    }
}
