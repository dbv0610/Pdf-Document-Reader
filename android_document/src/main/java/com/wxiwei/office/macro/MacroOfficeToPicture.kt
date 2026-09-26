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

    private var modeType = IOfficeToPicture.VIEW_CHANGE_END

    /**
     * set mode type
     */
    override fun setModeType(modeType: Byte) {
        this.modeType = modeType
    }

    override fun getModeType(): Byte {
        return modeType
    }

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

    override fun isZoom(): Boolean {
        return true
    }

    override fun dispose() {
        officeToPictureListener = null
    }
}
