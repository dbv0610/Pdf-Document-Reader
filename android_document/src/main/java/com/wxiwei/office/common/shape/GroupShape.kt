/*
 * 文件名称:          GroupShape.java
 *  
 * 编译器:            android2.2
 * 时间:              下午5:11:59
 */
package com.wxiwei.office.common.shape

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2013-4-7
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
open class GroupShape : AbstractShape() {
    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_GROUP

    fun setOffPostion(offX: Int, offY: Int) {
        this.offX = offX
        this.offY = offY
    }

    /**
     * append shape of this slide
     */
    fun appendShapes(shape: IShape?) {
        if (shape != null) this.shapes.add(shape)
    }

    /** Removes [shape]; returns its index, or -1 when it is not a direct child. */
    fun removeShape(shape: IShape): Int {
        val index = shapes.indexOfFirst { it === shape }
        if (index >= 0) shapes.removeAt(index)
        return index
    }

    /** Puts [shape] back at [index] (see [removeShape]). */
    fun insertShape(index: Int, shape: IShape) {
        shapes.add(index.coerceIn(0, shapes.size), shape)
    }

    /**
     * get all shapes of this slide
     */
    fun getShapes(): Array<IShape> {
        return shapes.toTypedArray<IShape>()
    }

    @JvmField
    var offX: Int = 0
    @JvmField
    var offY: Int = 0

    // shapes of this slide
    private val shapes: MutableList<IShape>

    /**
     * 
     */
    init {
        shapes = ArrayList<IShape>()
    }
}
