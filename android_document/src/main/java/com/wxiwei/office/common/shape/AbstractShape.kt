/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          AbstractShape.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:07:59
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.pg.animate.IAnimation

/**
 * shape的抽象类
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
 * 日期:            2012-2-14
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
open class AbstractShape : IShape {
    override val type: Short get() = -1
    override var parent: IShape? = null
    override var groupShapeID: Int = -1
    override var shapeID: Int = 0
    protected var rect: Rectangle? = null
    override var bounds: Rectangle?
        get() = rect
        set(value) { rect = value }
    override var data: Any?
        get() = null
        set(value) {}
    override var flipHorizontal: Boolean = false
    override var flipVertical: Boolean = false
    override var rotation: Float = 0f
    override var isHidden: Boolean = false
    override var animation: IAnimation? = null
    override var placeHolderID: Int = 0
    var backgroundAndFill: BackgroundAndFill? = null
    var line: Line? = null
        set(value) {
            field = value
            if (value != null) hasLine = true
        }
    private var hasLine = false

    fun hasLine(): Boolean = line != null

    fun setLine(hasLine: Boolean) {
        this.hasLine = hasLine
        if (hasLine && line == null) line = Line()
    }

    fun createLine(): Line = Line().also { line = it }

    override fun dispose() {
        rect = null
        parent?.dispose()
        parent = null
        animation?.dispose()
        animation = null
        backgroundAndFill?.dispose()
        backgroundAndFill = null
        line?.dispose()
        line = null
    }

    companion object {
        // picture
        const val SHAPE_PICTURE: Short = 0
        // text box
        const val SHAPE_TEXTBOX: Short = 1

        // auto shape
        const val SHAPE_AUTOSHAPE: Short = 2

        //
        const val SHAPE_BG_FILL: Short = 3

        //
        const val SHAPE_LINE: Short = 4

        // chart
        const val SHAPE_CHART: Short = 5

        // table
        const val SHAPE_TABLE: Short = 6

        //group shape
        const val SHAPE_GROUP: Short = 7

        //smart art
        const val SHAPE_SMARTART: Short = 8
    }
}
