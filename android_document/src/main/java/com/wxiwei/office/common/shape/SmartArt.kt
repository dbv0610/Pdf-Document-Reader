/*
 * 文件名称:          SmartArt.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:10:05
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
 * 日期:            2013-4-17
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
class SmartArt : AbstractShape() {
    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_SMARTART

    /**
     * append shape of this slide
     */
    fun appendShapes(shape: IShape?) {
        if (shape != null) this.shapes.add(shape)
    }

    /**
     * get all shapes of this slide
     */
    fun getShapes(): Array<IShape> {
        return shapes.toTypedArray<IShape>()
    }

    private val offX = 0
    private val offY = 0

    // shapes of this slide
    private val shapes: MutableList<IShape>

    /**
     * 
     */
    init {
        shapes = ArrayList<IShape>()
    }
}
