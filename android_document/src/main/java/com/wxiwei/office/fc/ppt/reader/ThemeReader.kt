/*
 * 文件名称:           ThemeReader.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:29:30
 */
package com.wxiwei.office.fc.ppt.reader

import android.graphics.Color
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart

/**
 * 解析 theme color
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
 * 日期:           2012-3-2
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
class ThemeReader {
    /**
     * 
     */
    @Throws(Exception::class)
    fun getThemeColorMap(themePart: PackagePart): MutableMap<String, Int>? {
        // theme xml
        val saxreader = SAXReader()
        val `in` = themePart.getInputStream()
        val poiTheme = saxreader.read(`in`)
        val root = poiTheme.getRootElement()
        if (root != null) {
            val themeElements = root.element("themeElements")
            if (themeElements != null) {
                val clrScheme = themeElements.element("clrScheme")


                // color map
                val colorMap: MutableMap<String, Int> = HashMap<String, Int>()
                val it = clrScheme.elementIterator()
                while (it.hasNext()) {
                    val clr = it.next() as Element
                    val name = clr.getName()
                    val srgbClr = clr.element("srgbClr")
                    val sysClr = clr.element("sysClr")
                    if (srgbClr != null) {
                        colorMap.put(name, Color.parseColor("#" + srgbClr.attributeValue("val")))
                    } else if (sysClr != null) {
                        colorMap.put(name, Color.parseColor("#" + sysClr.attributeValue("lastClr")))
                    } else {
                        colorMap.put(name, Color.WHITE)
                    }
                }
                return colorMap
            }
        }
        `in`.close()
        return null
    }

    companion object {
        private val themeReader = ThemeReader()

        /**
         * 
         */
        @JvmStatic
        fun instance(): ThemeReader {
            return themeReader
        }
    }
}
