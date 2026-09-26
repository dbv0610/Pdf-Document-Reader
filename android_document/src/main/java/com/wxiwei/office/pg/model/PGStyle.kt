package com.wxiwei.office.pg.model

import java.util.Hashtable
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IAttributeSet

class PGStyle {
    private var anchor: Rectangle? = null
    private var attr: IAttributeSet? = null
    private var lvlStyleIDs: MutableMap<Int, Int>? = HashMap()
    private var defaultFontColor: MutableMap<Int, String>? = null

    fun getAnchor(): Rectangle? = anchor
    fun setAnchor(anchor: Rectangle?) { this.anchor = anchor }
    fun getSectionAttr(): IAttributeSet? = attr
    fun setSectionAttr(attr: IAttributeSet?) { this.attr = attr }
    fun getStyle(lvl: Int): Int = lvlStyleIDs?.get(lvl) ?: -1
    fun addStyle(lvl: Int, style: Int) { lvlStyleIDs?.put(lvl, style) }
    fun addDefaultFontColor(lvl: Int, fontColor: String?) {
        if (lvl > 0 && fontColor != null) {
            if (defaultFontColor == null) defaultFontColor = Hashtable()
            defaultFontColor?.put(lvl, fontColor)
        }
    }
    fun getDefaultFontColor(lvl: Int): String? = defaultFontColor?.get(lvl)
    fun dispose() {
        anchor = null
        attr?.dispose(); attr = null
        lvlStyleIDs?.clear(); lvlStyleIDs = null
        defaultFontColor?.clear(); defaultFontColor = null
    }
}
