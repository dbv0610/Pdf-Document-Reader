/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          MacroOfficeToPicture.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:18:23
 */
package com.wxiwei.office.macro

import android.graphics.Bitmap
import com.wxiwei.office.common.IOfficeToPicture

/**
 * generated picture
 */
internal class MacroOfficeToPicture internal constructor(
    private var officeToPictureListener: OfficeToPictureListener?
) : IOfficeToPicture {

    override var modeType = IOfficeToPicture.VIEW_CHANGE_END



    /**
     * Get converter to of picture Bitmap instance, if the return is empty, is not generated picture
     *
     * @param componentWidth  engine component width
     * @param componentHeight engine component height
     *
     * @return Bitmap instance
     */
    override fun getBitmap(componentWidth: Int, componentHeight: Int): Bitmap? {
        if (officeToPictureListener != null) {
            return officeToPictureListener!!.getBitmap(componentWidth, componentHeight)
            //return officeToPictureListener.getBitmap(845, 480);
        }
        return null
    }

    /**
     * picture generated, the callback method
     *
     * @param bitmap  generated picture bitmap
     */
    override fun callBack(bitmap: Bitmap?) {
        officeToPictureListener?.callBack(bitmap)
    }

    override val isZoom: Boolean get() = true

    override fun dispose() {
        officeToPictureListener = null
    }
}
