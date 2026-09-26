package com.wxiwei.office.pg.model

import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.pg.model.tableStyle.TableStyle
import com.wxiwei.office.simpletext.model.IDocument
import com.wxiwei.office.simpletext.model.STDocument

class PGModel {
    private var doc: IDocument? = STDocument()
    private var slides: MutableList<PGSlide>? = ArrayList()
    private var pageSize: Dimension? = null
    private var slideMasters: MutableList<PGSlide>? = ArrayList()
    private var total = 0
    private var tableStyleMap: MutableMap<String, TableStyle>? = null
    private var slideNumberOffset = 0
    private var omitTitleSlide = false
    @Synchronized fun appendSlide(slide: PGSlide?) { if (slides != null && slide != null) slides?.add(slide) }
    fun getSlide(index: Int): PGSlide? = if (index in 0 until (slides?.size ?: 0)) slides?.get(index) else null
    fun getSlideForSlideNo(slideNo: Int): PGSlide? = slides?.firstOrNull { it.getSlideNo() == slideNo }
    fun getRealSlideCount(): Int = slides?.size ?: 0
    fun getSlideCount(): Int = total
    fun setSlideCount(total: Int) { this.total = total }
    fun getRenderersDoc(): IDocument? = doc
    fun getPageSize(): Dimension? = pageSize
    fun setPageSize(pageSize: Dimension?) { this.pageSize = pageSize }
    fun appendSlideMaster(master: PGSlide): Int { val size = slideMasters?.size ?: 0; slideMasters?.add(master); return size }
    fun getSlideMaster(index: Int): PGSlide? = if (index in 0 until (slideMasters?.size ?: 0)) slideMasters?.get(index) else null
    fun getSlideMasterCount(): Int = slideMasters?.size ?: 0
    fun putTableStyle(styleID: String?, tableStyle: TableStyle?) { if (tableStyleMap == null) tableStyleMap = HashMap(); if (styleID != null && tableStyle != null) tableStyleMap?.put(styleID, tableStyle) }
    fun getTableStyle(styleID: String?): TableStyle? = if (styleID != null) tableStyleMap?.get(styleID) else null
    fun getSlideNumberOffset(): Int = slideNumberOffset
    fun setSlideNumberOffset(value: Int) { slideNumberOffset = value }
    fun isOmitTitleSlide(): Boolean = omitTitleSlide
    fun setOmitTitleSlide(value: Boolean) { omitTitleSlide = value }
    @Synchronized fun dispose() { doc?.dispose(); doc = null; slides?.forEach { it.dispose() }; slides?.clear(); slides = null; slideMasters?.forEach { it.dispose() }; slideMasters?.clear(); slideMasters = null; tableStyleMap?.clear(); tableStyleMap = null }
}
