package com.wxiwei.office.pg.model

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IAttributeSet

class PGMaster {
    private var bgFill: BackgroundAndFill? = null
    private var schemeColor: MutableMap<String, Int>? = HashMap()
    private var styleByType: MutableMap<String, PGStyle>? = HashMap()
    private var styleByIdx: MutableMap<Int, PGStyle>? = HashMap()
    private var titlebodyID: MutableMap<Int, Int>? = null
    private var titleStyle: PGStyle? = null
    private var bodyStyle: PGStyle? = null
    private var otherStyle: PGStyle? = null
    private var index = -1
    fun getColor(schemeClr: String): Int = schemeColor?.get(schemeClr) ?: 0
    fun addColor(schemeClr: String, color: Int) { schemeColor?.put(schemeClr, color) }
    fun getBackgroundAndFill(): BackgroundAndFill? = bgFill
    fun setBackgroundAndFill(bgFill: BackgroundAndFill?) { this.bgFill = bgFill }
    fun addStyleByType(type: String, style: PGStyle) { styleByType?.put(type, style) }
    fun addStyleByIdx(idx: Int, style: PGStyle) { styleByIdx?.put(idx, style) }
    fun setTitleStyle(style: PGStyle?) { titleStyle = style }
    fun setBodyStyle(style: PGStyle?) { bodyStyle = style }
    fun setDefaultStyle(style: PGStyle?) { otherStyle = style }
    private fun style(type: String?, idx: Int): PGStyle? {
        val checked = PGPlaceholderUtil.instance().checkTypeName(type)
        if (!PGPlaceholderUtil.instance().isBody(checked)) return styleByType?.get(checked)
        if (idx > 0) return styleByIdx?.get(idx) ?: styleByIdx?.values?.firstOrNull()
        return null
    }
    fun getAnchor(type: String?, idx: Int): Rectangle? = style(type, idx)?.getAnchor()
    fun getSectionAttr(type: String?, idx: Int): IAttributeSet? = style(type, idx)?.getSectionAttr()
    fun getTextStyle(type: String?, idx: Int, lvl: Int): Int {
        val checked = PGPlaceholderUtil.instance().checkTypeName(type)
        if (!PGPlaceholderUtil.instance().isBody(checked)) {
            val styleID = styleByType?.get(checked)?.getStyle(lvl) ?: -1
            if (styleID >= 0) return styleID
            if (PGPlaceholderUtil.TITLE == checked) {
                titleStyle?.let { return it.getStyle(lvl) }
            } else {
                otherStyle?.let { return it.getStyle(lvl) }
            }
        } else if (idx > 0) {
            val styleID = (styleByIdx?.get(idx) ?: styleByIdx?.values?.firstOrNull())?.getStyle(lvl) ?: -1
            if (styleID >= 0) return styleID
            bodyStyle?.let { return it.getStyle(lvl) }
        }
        return -1
    }
    fun getSchemeColor(): MutableMap<String, Int>? = schemeColor
    fun getSlideMasterIndex(): Int = index
    fun setSlideMasterIndex(index: Int) { this.index = index }
    fun addTitleBodyID(idx: Int, id: Int) { if (titlebodyID == null) titlebodyID = HashMap(); titlebodyID?.put(idx, id) }
    fun getTitleBodyID(idx: Int): Int? = titlebodyID?.get(idx)
    fun dispose() {
        bgFill?.dispose(); bgFill = null; schemeColor?.clear(); schemeColor = null
        styleByType?.values?.forEach { it.dispose() }; styleByType?.clear(); styleByType = null
        styleByIdx?.values?.forEach { it.dispose() }; styleByIdx?.clear(); styleByIdx = null
        titleStyle?.dispose(); titleStyle = null; bodyStyle?.dispose(); bodyStyle = null; otherStyle?.dispose(); otherStyle = null
        titlebodyID?.clear(); titlebodyID = null
    }
}
