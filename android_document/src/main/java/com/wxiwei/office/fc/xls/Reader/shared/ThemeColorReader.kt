package com.wxiwei.office.fc.xls.Reader.shared

import android.graphics.Color
import com.wxiwei.office.constant.SchemeClrConstant
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.io.SAXReader
import com.wxiwei.office.fc.openxml4j.opc.PackagePart
import com.wxiwei.office.ss.model.baseModel.Workbook

class ThemeColorReader private constructor() {
    companion object {
        private val reader = ThemeColorReader()

        @JvmStatic
        fun instance(): ThemeColorReader = reader
    }

    fun getThemeColor(themeParts: PackagePart, book: Workbook) {
        val saxreader = SAXReader()
        val input = themeParts.inputStream
        val poiTheme: Document = saxreader.read(input)
        input.close()
        val root = poiTheme.rootElement
        val themeElements = root.element("themeElements")
        val themeColorElement = themeElements.element("clrScheme")

        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_LT1, SchemeClrConstant.SCHEME_BG1, 0, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_DK1, SchemeClrConstant.SCHEME_TX1, 1, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_LT2, SchemeClrConstant.SCHEME_BG2, 2, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_DK2, SchemeClrConstant.SCHEME_TX2, 3, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT1, null, 4, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT2, null, 5, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT3, null, 6, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT4, null, 7, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT5, null, 8, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_ACCENT6, null, 9, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_HLINK, null, 10, book)
        addThemeColor(themeColorElement, SchemeClrConstant.SCHEME_FOLHLINK, null, 11, book)
    }

    private fun addThemeColor(
        themeColorElement: Element,
        name: String,
        alias: String?,
        index: Int,
        book: Workbook
    ) {
        val color = getColorIndex(themeColorElement.element(name), book)
        book.addSchemeColorIndex(name, color)
        if (alias != null) {
            book.addSchemeColorIndex(alias, color)
        }
        book.addThemeColorIndex(index, color)
    }

    private fun getColorIndex(colorEle: Element, book: Workbook): Int {
        var color = Color.BLACK
        val rgbElement = colorEle.element("srgbClr")
        val sysElement = colorEle.element("sysClr")
        if (rgbElement != null) {
            color = rgbElement.attributeValue("val").toInt(16)
        } else if (sysElement != null) {
            color = sysElement.attributeValue("lastClr").toInt(16)
        }
        color = color or (0xFF shl 24)
        return book.addColor(color)
    }
}
