/*
 * 文件名称:          StyleManager.java
 *
 * 编译器:            android2.2
 * 时间:              上午10:47:13
 */
package com.wxiwei.office.simpletext.model

import java.util.Hashtable

/**
 * style manage
 *
 * Read版本:        Read V1.0
 *
 * 作者:            ljj8494
 *
 * 日期:            2012-2-27
 *
 * 负责人:          ljj8494
 */
class StyleManage {
    private var styles: MutableMap<Int, Style>? = Hashtable<Int, Style>()

    /**
     * get style for styleID
     */
    fun getStyle(styleID: Int): Style? {
        return styles!![styleID]
    }

    /**
     * get style for style name
     *  This method as little as possible, because the very collapse of time-consuming
     */
    fun getStyleForName(styleName: String?): Style? {
        val itor = styles!!.values.iterator()
        while (itor.hasNext()) {
            val s = itor.next()
            if (s.getName()!!.equals(styleName)) {
                return s
            }
        }
        return null
    }

    /**
     * add style
     */
    fun addStyle(style: Style) {
        styles!!.put(style.getId(), style)
    }

    fun dispose() {
        val itor = styles!!.values.iterator()
        while (itor.hasNext()) {
            itor.next().dispose()
        }
        styles!!.clear()
        styles = null
    }

    companion object {
        private val kit = StyleManage()

        /**
         *
         */
        @JvmStatic
        fun instance(): StyleManage {
            return kit
        }
    }
}
