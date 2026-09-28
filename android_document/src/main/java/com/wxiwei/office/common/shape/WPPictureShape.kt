/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          WPPictureShape.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:11:57
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
 * 日期:            2013-5-29
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
class WPPictureShape : WPAutoShape() {
    /**
     * 
     * 
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_PICTURE

    fun setPictureShape(pictureShape: PictureShape) {
        this.pictureShape = pictureShape

        if (rect == null) {
            rect = pictureShape.bounds
        }
    }

    fun getPictureShape(): PictureShape? {
        return pictureShape
    }


    /**
     * 
     */
    override val isWatermarkShape: Boolean
        get() = false

    override fun dispose() {
        if (pictureShape != null) {
            pictureShape!!.dispose()
            pictureShape = null
        }
    }

    private var pictureShape: PictureShape? = null
}
