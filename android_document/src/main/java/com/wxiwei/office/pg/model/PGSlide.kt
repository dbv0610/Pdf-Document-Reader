package com.wxiwei.office.pg.model

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.common.shape.IShape
import com.wxiwei.office.common.shape.SmartArt
import com.wxiwei.office.common.shape.TableCell
import com.wxiwei.office.common.shape.TableShape
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Rectanglef
import com.wxiwei.office.pg.animate.ShapeAnimation

class PGSlide {
    companion object {
        const val Slide_Master: Byte = 0
        const val Slide_Layout: Byte = 1
        const val Slide_Normal: Byte = 2
    }

    private var hasTable = false
    private var slideNo = 0
    private var slideType = 0
    private var shapeCountForFind = -1
    private var shapes: MutableList<IShape>? = ArrayList()
    private var shapesForFind: MutableList<IShape>? = null
    private var notes: PGNotes? = null
    private var bgFill: BackgroundAndFill? = null
    private val masterIndexs = intArrayOf(-1, -1)
    private var hasTransition = false
    private var grpShapeLst: MutableMap<Int, MutableList<Int>>? = null
    private var shapeAnimLst: MutableList<ShapeAnimation>? = null
    private var smartArtList: MutableMap<String, SmartArt>? = null
    private var showMasterHeadersFooters = false
    private var geometryType = 0

    init { showMasterHeadersFooters = true; geometryType = -1 }
    constructor()
    constructor(slideNo: Int, notes: PGNotes?) { this.slideNo = slideNo; this.notes = notes }

    fun setSlideNo(slideNo: Int) { this.slideNo = slideNo }
    fun getSlideNo(): Int = slideNo
    fun getSlideType(): Int = slideType
    fun setSlideType(slideType: Int) { this.slideType = slideType }
    fun appendShapes(shape: IShape?) {
        if (shape == null) return
        if (!hasTable) hasTable = shape.getType() == AbstractShape.SHAPE_TABLE
        shapes?.add(shape)
    }
    /** Live editing: remove a top-level shape; returns its z-order index or -1. */
    fun removeShape(shape: IShape): Int {
        val index = shapes?.indexOf(shape) ?: -1
        if (index >= 0) { shapes?.removeAt(index); shapesForFind = null; shapeCountForFind = -1 }
        return index
    }
    /** Live editing: put a shape back at a z-order index (undo of [removeShape]). */
    fun insertShape(index: Int, shape: IShape) {
        val list = shapes ?: return
        list.add(index.coerceIn(0, list.size), shape)
        if (!hasTable) hasTable = shape.getType() == AbstractShape.SHAPE_TABLE
        shapesForFind = null; shapeCountForFind = -1
    }
    fun getShapes(): Array<IShape> = shapes?.toTypedArray() ?: emptyArray()
    fun getShapeCount(): Int = shapes?.size ?: 0
    fun getShapeCountForFind(): Int {
        if (!hasTable) return getShapeCount()
        if (shapeCountForFind > 0) return shapeCountForFind
        shapesForFind = ArrayList()
        var count = 0
        for (shape in shapes.orEmpty()) {
            if (shape.getType() == AbstractShape.SHAPE_TABLE) {
                val table = shape as TableShape
                for (i in 0 until table.getCellCount()) {
                    val cell: TableCell? = table.getCell(i)
                    if (cell?.getText() != null) { shapesForFind?.add(cell.getText()); count++ }
                }
            } else { shapesForFind?.add(shape); count++ }
        }
        shapeCountForFind = count
        return count
    }
    fun getShape(index: Int): IShape? = if (index in 0 until getShapeCount()) shapes?.get(index) else null
    fun getShapeForFind(index: Int): IShape? = if (!hasTable) getShape(index) else if (index in 0 until (shapesForFind?.size ?: 0)) shapesForFind?.get(index) else null

    fun getShape(x: Float, y: Float): IShape? = findShape(x, y, false)
    fun getShape(x: Int, y: Int): IShape? = findShape(x.toFloat(), y.toFloat(), false)
    private fun findShape(x: Float, y: Float, textboxOnly: Boolean): IShape? {
        val list = if (textboxOnly) shapes.orEmpty().asReversed() else shapes.orEmpty()
        for (shape in list) {
            val rect = shape.getBounds()
            if (shape.getType() == AbstractShape.SHAPE_TABLE) {
                val table = shape as TableShape
                for (i in 0 until table.getCellCount()) {
                    val cell = table.getCell(i)
                    val r: Rectanglef? = cell?.getBounds()
                    if (r?.contains(x, y) == true) return cell.getText()
                }
            } else if (rect.contains(x.toInt(), y.toInt()) && (!textboxOnly || shape.getType() == AbstractShape.SHAPE_TEXTBOX)) return shape
        }
        return null
    }
    fun getTextboxShape(x: Int, y: Int): IShape? = findShape(x.toFloat(), y.toFloat(), true)
    fun setNotes(notes: PGNotes?) { this.notes = notes }
    fun getNotes(): PGNotes? = notes
    fun getBackgroundAndFill(): BackgroundAndFill? = bgFill
    fun setBackgroundAndFill(bgFill: BackgroundAndFill?) { this.bgFill = bgFill }
    fun setMasterSlideIndex(index: Int) { masterIndexs[0] = index }
    fun setLayoutSlideIndex(index: Int) { masterIndexs[1] = index }
    fun getMasterIndexs(): IntArray = masterIndexs
    fun setTransition(transition: Boolean) { hasTransition = transition }
    fun hasTransition(): Boolean = hasTransition
    fun addShapeAnimation(shapeAnim: ShapeAnimation?) { if (shapeAnimLst == null) shapeAnimLst = ArrayList(); if (shapeAnim != null) shapeAnimLst?.add(shapeAnim) }
    fun getSlideShowAnimation(): MutableList<ShapeAnimation>? = shapeAnimLst
    fun addGroupShape(grpShapeID: Int, childShapes: MutableList<Int>) {
        if (grpShapeLst == null) grpShapeLst = HashMap()
        for (id in childShapes.toList()) if (grpShapeLst?.containsKey(id) == true) { val sub = grpShapeLst?.remove(id); childShapes.remove(id); if (sub != null) childShapes.addAll(sub) }
        grpShapeLst?.put(grpShapeID, childShapes)
    }
    fun getGroupShape(): MutableMap<Int, MutableList<Int>>? = grpShapeLst
    fun addSmartArt(id: String?, smartArt: SmartArt?) { if (smartArtList == null) smartArtList = HashMap(); if (id != null && smartArt != null) smartArtList?.put(id, smartArt) }
    fun getSmartArt(id: String?): SmartArt? = if (id != null) smartArtList?.remove(id) else null
    fun getTextboxByPlaceHolderID(placeHolderID: Int): IShape? = shapes.orEmpty().firstOrNull { it.getType() == AbstractShape.SHAPE_TEXTBOX && it.getPlaceHolderID() == placeHolderID }
    fun isShowMasterHeadersFooter(): Boolean = showMasterHeadersFooters
    fun setShowMasterHeadersFooters(value: Boolean) { showMasterHeadersFooters = value }
    fun getGeometryType(): Int = geometryType
    fun setGeometryType(value: Int) { geometryType = value }
    fun dispose() {
        notes?.dispose(); notes = null
        shapesForFind?.clear(); shapesForFind = null
        shapes?.forEach { it.dispose() }; shapes?.clear(); shapes = null
        bgFill?.dispose(); bgFill = null
        shapeAnimLst?.clear(); shapeAnimLst = null
    }
}
