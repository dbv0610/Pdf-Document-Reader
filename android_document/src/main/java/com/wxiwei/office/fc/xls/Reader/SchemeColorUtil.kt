package com.wxiwei.office.fc.xls.Reader

import com.wxiwei.office.constant.SchemeClrConstant
import com.wxiwei.office.ss.model.baseModel.Workbook

object SchemeColorUtil {
    private var schemeClrName: MutableList<String>? = null
    private var schemeColor: MutableMap<String, Int>? = null

    @JvmStatic
    fun getThemeColor(workbook: Workbook, index: Int): Int {
        init(workbook)
        val names = schemeClrName ?: return -1
        if (index < 0 || index >= names.size) {
            return -1
        }
        return workbook.getSchemeColor(names[index])
    }

    @JvmStatic
    fun getSchemeColor(workbook: Workbook): Map<String, Int> {
        init(workbook)
        return schemeColor ?: emptyMap()
    }

    private fun init(workbook: Workbook) {
        if (schemeColor == null) {
            schemeClrName = mutableListOf(
                SchemeClrConstant.SCHEME_BG1,
                SchemeClrConstant.SCHEME_TX1,
                SchemeClrConstant.SCHEME_BG2,
                SchemeClrConstant.SCHEME_TX2,
                SchemeClrConstant.SCHEME_ACCENT1,
                SchemeClrConstant.SCHEME_ACCENT2,
                SchemeClrConstant.SCHEME_ACCENT3,
                SchemeClrConstant.SCHEME_ACCENT4,
                SchemeClrConstant.SCHEME_ACCENT5,
                SchemeClrConstant.SCHEME_ACCENT6,
                SchemeClrConstant.SCHEME_HLINK,
                SchemeClrConstant.SCHEME_FOLHLINK,
                SchemeClrConstant.SCHEME_DK1,
                SchemeClrConstant.SCHEME_LT1,
                SchemeClrConstant.SCHEME_DK2,
                SchemeClrConstant.SCHEME_LT2
            )
            schemeColor = HashMap()
        }

        val colors = schemeColor ?: return
        colors.clear()
        for (key in schemeClrName ?: emptyList()) {
            colors[key] = workbook.getSchemeColor(key)
        }
    }
}
