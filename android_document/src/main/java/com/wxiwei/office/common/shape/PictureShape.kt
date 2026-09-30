/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          Picture.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:01:51
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.common.pictureefftect.PictureEffectInfo
import com.wxiwei.office.system.IControl

/**
 * picture data class
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
class PictureShape : AbstractShape() {
    /**
     * get type of this shape
     */
    override val type: Short
        get() = AbstractShape.Companion.SHAPE_PICTURE

    /**
     * 
     */
    fun getPicture(control: IControl?): Picture? {
        if (control == null) {
            return null
        }
        return control?.getSysKit()?.getPictureManage()?.getPicture(pictureIndex)
    }

    /**
     * 
     */
    fun setZoomX(zoomX: Short) {
        this.zoomX = zoomX
    }

    /**
     * 
     */
    fun setZoomY(zoomY: Short) {
        this.zoomY = zoomY
    }


    //    /**
    //     * 
    //     * @return
    //     */
    //    public int getRealWidth()
    //    {
    //       return  (int)(rect.width * zoomX / 1000.f);//rect.width * 1000.f / zoomX
    //    }
    //    
    //    /**
    //     * 
    //     */
    //    public int getRealHeight()
    //    {
    //       return  (int)(rect.height * zoomY / 1000.f);
    //    } 

    override fun dispose() {
        super.dispose()
    }

    /**
     * 
     * @return
     */
    /**
     * 
     * @param pictureIndex
     */
    @JvmField
    var pictureIndex: Int = 0

    // Horizontal scaling factor supplied by user expressed in .001% units
    private var zoomX: Short = 0

    // Vertical scaling factor supplied by user expressed in .001% units
    private var zoomY: Short = 0

    /**
     * 
     */
    /**
     * 
     * @param effectInfor
     */
    //picture effect property
    var pictureEffectInfor: PictureEffectInfo? = null

    companion object {
        fun getPicture(control: IControl?, pictureIndex: Int): Picture? {
            if (control == null) {
                return null
            }
            return control?.getSysKit()?.getPictureManage()?.getPicture(pictureIndex)
        }
    }
}
