/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          IShape.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:23:12
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.pg.animate.IAnimation
import com.wxiwei.office.java.awt.Rectangle

/**
 * PowerPoint的shape的接口
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            ljj8494
 * 
 * 
 * 日期:            2012-2-13
 * 
 * 
 * 负责人:          ljj8494
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
interface IShape {
    /**
     * 
     * @return
     */
    /**
     * 
     * @param id
     */
    var groupShapeID: Int

    /**
     * 
     * @return
     */
    /**
     * 
     * @param id
     */
    var shapeID: Int

    /**
     * 
     * @return
     */
    val type: Short
    /**
     * @return the parent of this shape
     */
    /**
     * set parent of this shape;
     */
    var parent: IShape?

    /**
     * get size of this shape
     */
    /**
     * set size of this shape
     */
    var bounds: Rectangle?
    /**
     * get data of this shape
     */
    /**
     * set data of this shape
     */
    var data: Any?
    /**
     * get horizontal flip of this shape
     */
    /**
     * set horizontal flip of this shape
     */
    var flipHorizontal: Boolean
    /**
     * get vertical flip of this shape
     */
    /**
     * set vertical flip of this shape
     */
    var flipVertical: Boolean
    /**
     * get rotation of this shape
     */
    /**
     * set rotation of this shape
     */
    var rotation: Float

    /**
     * 
     * @return
     */
    /**
     * 
     * @param hidden
     */
    var isHidden: Boolean

    /**
     * 
     * @return
     */
    /**
     * set shape animation
     * @param animation
     */
    var animation: IAnimation?

    /**
     * 
     * @return
     */
    /**
     * 
     * @param placeHolderID
     */
    var placeHolderID: Int

    /**
     * dispose
     */
    fun dispose()
}
