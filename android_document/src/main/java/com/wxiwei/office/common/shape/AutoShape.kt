/*
 * 文件名称:           AutoShape.java
 *  
 * 编译器:             android2.2
 * 时间:               下午2:13:13
 */
package com.wxiwei.office.common.shape


/**
 * Represents an AutoShape.
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
 * 日期:           2012-8-24
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
open class AutoShape : AbstractShape {
    /**
     * 
     */
    constructor()

    /**
     * 
     * @param type
     */
    constructor(type: Int) {
        this.shapeType = type
    }

    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_AUTOSHAPE


    /**
     * 
     * @param shape07
     */
    fun setAuotShape07(shape07: Boolean) {
        this.isAutoShape07 = shape07
    }

    /**
     * dispose
     */
    override fun dispose() {
        super.dispose()
    }

    /**
     * get autoShape type
     * @return
     */
    /**
     * set autoShape type
     * @return
     */
    //
    var shapeType: Int = 0

    /**
     * get values of adjust points
     * @return
     */
    /**
     * set values of adjust points
     * @param values
     */
    // adjust values by clockwise(percent)
    var adjustData: Array<Float?>? = null

    /**
     * 
     * @return
     */
    // autoShape is 07 or 03
    var isAutoShape07: Boolean = true
        private set
}
