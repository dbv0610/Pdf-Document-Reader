/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           WPAutoshape.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:25:32
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.java.awt.Rectangle

/**
 * TODO: 文件注释
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
 * 日期:           2013-3-22
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
open class WPAutoShape  /*// 绝对值
    public static final short HEIGHT_ABSOLUTE = 0;
    // 相对于页面
    public static final short HEIGHT_RELATIVE = 1;
    // 相对于页边距
    public static final short HEIGHT_RELATIVE_MARGIN = 2;
    // 相对于上边距
    public static final short HEIGHT_RELATIVE_TOP = 3;
    // 相对于下边距
    public static final short HEIGHT_RELATIVE_BOTTOM = 4;
    // 相对于内边距
    public static final short HEIGHT_RELATIVE_INNER = 5;
    // 相对于外边距
    public static final short HEIGHT_RELATIVE_OUTER = 6;
    
    // 绝对值
    public static final short WIDTH_ABSOLUTE = 0;
    // 相对于页面
    public static final short WIDTH_RELATIVE = 1;
    // 相对于页边距
    public static final short WIDTH_RELATIVE_MARGIN = 2;
    // 相对于左边距
    public static final short WIDTH_RELATIVE_LFET = 3;
    // 相对于右边距
    public static final short WIDTH_RELATIVE_RIGHT = 4;
    // 相对于内边距
    public static final short WIDTH_RELATIVE_INNER = 5;
    // 相对于外边距
    public static final short WIDTH_RELATIVE_OUTER = 6;*/
/**
 * 
 */
    : WPAbstractShape() {
    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_AUTOSHAPE

    open val isWatermarkShape: Boolean
        /**
         * 
         */
        get() = false

    /**
     * 
     * 
     */
    override var bounds: Rectangle?
        get() = groupShape?.bounds ?: super.bounds
        set(value) { super.bounds = value }

    /**
     * 
     * @param groupShape
     */
    fun addGroupShape(groupShape: WPGroupShape?) {
        this.groupShape = groupShape
    }

    /**
     * dispose
     */
    override fun dispose() {
        super.dispose()
        if (groupShape != null) {
            groupShape!!.dispose()
            groupShape = null
        }
    }

    /**
     * 
     * @return
     */
    //
    var groupShape: WPGroupShape? = null
        private set
}
