package com.wxiwei.office.pg.model

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.simpletext.model.IAttributeSet

class PGLayout {
    private var bgFill: BackgroundAndFill? = null
    private var styleByType: MutableMap<String, PGStyle>? = HashMap()
    private var styleByIdx: MutableMap<Int, PGStyle>? = HashMap()
    private var shapeType: MutableMap<Int, String?>? = null
    private var titlebodyID: MutableMap<Int, Int>? = null
    private var index = -1
    private var addShapes = true
    fun getAnchor(type: String?, idx: Int): Rectangle? = choose(type, idx)?.getAnchor()
    fun getSectionAttr(type: String?, idx: Int): IAttributeSet? = choose(type, idx)?.getSectionAttr()
    fun getStyleID(type: String?, idx: Int, lvl: Int): Int = choose(type, idx)?.getStyle(lvl) ?: -1
    private fun choose(type: String?, idx: Int): PGStyle? = if (!PGPlaceholderUtil.instance().isBody(type)) styleByType?.get(type) else if (idx > 0) styleByIdx?.get(idx) else null
    fun setStyleByType(type: String, style: PGStyle) { styleByType?.put(type, style) }
    fun setStyleByIdx(idx: Int, style: PGStyle) { styleByIdx?.put(idx, style) }
    fun getBackgroundAndFill(): BackgroundAndFill? = bgFill
    fun setBackgroundAndFill(bgFill: BackgroundAndFill?) { this.bgFill = bgFill }
    fun getSlideMasterIndex(): Int = index
    fun setSlideMasterIndex(index: Int) { this.index = index }
    fun isAddShapes(): Boolean = addShapes
    fun setAddShapes(addShapes: Boolean) { this.addShapes = addShapes }
    // PPTX layouts may contain user-drawn shapes without a placeholder type.
    // Java parser code legitimately passes null here.
    fun addShapeType(idx: Int, type: String?) { if (shapeType == null) shapeType = HashMap(); shapeType?.put(idx, type) }
    fun getShapeType(idx: Int): String? = shapeType?.get(idx)
    fun addTitleBodyID(idx: Int, id: Int) { if (titlebodyID == null) titlebodyID = HashMap(); titlebodyID?.put(idx, id) }
    fun getTitleBodyID(idx: Int): Int? = titlebodyID?.get(idx)
    fun disposs() {
        bgFill?.dispose(); bgFill = null
        styleByType?.values?.forEach { it.dispose() }; styleByType?.clear(); styleByType = null
        styleByIdx?.values?.forEach { it.dispose() }; styleByIdx?.clear(); styleByIdx = null
        shapeType?.clear(); shapeType = null; titlebodyID?.clear(); titlebodyID = null
    }
}
