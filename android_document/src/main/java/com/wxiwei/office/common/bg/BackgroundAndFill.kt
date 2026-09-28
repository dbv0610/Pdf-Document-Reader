/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Background.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:34:21
 */
package com.wxiwei.office.common.bg

import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.common.pictureefftect.PictureStretchInfo
import com.wxiwei.office.common.shape.AbstractShape
import com.wxiwei.office.system.IControl

/**
 * 文件注释
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
 * 日期:            2012-2-15
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
class BackgroundAndFill {
    val type: Short
        /**
         * 
         * 
         */
        get() = AbstractShape.Companion.SHAPE_BG_FILL

    /**
     * 
     */
    fun getPicture(control: IControl?): Picture? {
        return control?.getSysKit()?.getPictureManage()?.getPicture(pictureIndex)
    }

    /**
     * 
     * 
     */
    fun dispose() {
        stretch = null
        if (shader != null) {
            shader!!.dispose()
            shader = null
        }
    }

    //is slide background fill
    var isSlideBackgroundFill: Boolean = false

    var stretch: PictureStretchInfo? = null
    /**
     * @return Returns the fillType.
     */
    /**
     * @param fillType The fillType to set.
     */
    //
    @JvmField
    var fillType: Byte = 0
    /**
     * @return Returns the bgColor.
     */
    /**
     * @param bgColor The bgColor to set.
     */
    // BackgroundColor;
    var backgoundColor: Int = 0
    /**
     * @return Returns the fgColor.
     */
    /**
     * @param fgColor The fgColor to set.
     */
    // filled by color
    var foregroundColor: Int = 0
    /**
     * @return Returns the picture index.
     */
    /**
     * @param picture index The picture index to set.
     */
    //filled by picture
    var pictureIndex: Int = 0

    //filled by gradient color, tile
    @JvmField
    var shader: AShader? = null

    companion object {
        // no Fill
        const val FILL_NO: Byte = -1
        // Fill with a solid color
        const val FILL_SOLID: Byte = 0
        // Fill with a pattern (bitmap)
        const val FILL_PATTERN: Byte = 1
        // shade to title
        const val FILL_SHADE_TILE: Byte = 2
        // Center a picture in the shape
        const val FILL_PICTURE: Byte = 3
        // Similar to FILL_SHADE, but the fill angle
        // is additionally scaled by the aspect ratio of
        // the shape. If shape is square, it is the same as FILL_SHADE
        const val FILL_SHADE_RECT: Byte = 5
        // Shade from bounding rectangle to end point
        const val FILL_SHADE_RADIAL: Byte = 4
        // Shade from shape outline to end point
        const val FILL_SHADE_SHAPE: Byte = 6
        // Shade from start to end points
        const val FILL_SHADE_LINEAR: Byte = 7
        // A texture (pattern with its own color map)
        const val FILL_TEXTURE: Byte = 8
        // Use the background fill color/pattern
        const val FILL_BACKGROUND: Byte = 9
    }
}
