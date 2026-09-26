/*
 * 文件名称:          IOfficeToPictureListener.java
 *
 * 编译器:            android2.2
 * 时间:              下午4:19:51
 */
package com.wxiwei.office.macro

import android.graphics.Bitmap

/**
 * office to picture listener
 */
interface OfficeToPictureListener {

    /**
     * Get converter to of picture Bitmap instance, if the return is empty, is not generated picture
     *
     * @param componentWidth  engine component width
     * @param componentHeight engine component height
     *
     * @return Bitmap instance
     */
    fun getBitmap(componentWidth: Int, componentHeight: Int): Bitmap?

    /**
     * picture generated, the callback method
     *
     * @param bitmap  generated picture bitmap
     */
    fun callBack(bitmap: Bitmap?)
}
