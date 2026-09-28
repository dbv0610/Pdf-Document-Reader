/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.common.bg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Shader.TileMode
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.system.IControl
import java.io.FileInputStream
import java.io.InputStream

class TileShader(
    private val picture: Picture?,
    private val flip: Int,
    private val horiRatio: Float,
    private val vertRatio: Float
) : AShader() {
    constructor(
        picture: Picture?,
        flip: Int,
        horiRatio: Float,
        vertRatio: Float,
        offsetX: Int,
        offsetY: Int
    ) : this(picture, flip, horiRatio, vertRatio) {
        this.offsetX = offsetX
        this.offsetY = offsetY
    }

    override fun createShader(control: IControl?, viewIndex: Int, rect: Rect?): Shader? {
        try {
            var bmp: Bitmap? = getBitmap(control, viewIndex, picture, rect, null)
            val width = bmp!!.getWidth()
            val height = bmp.getHeight()
            bmp = Bitmap.createScaledBitmap(
                bmp,
                Math.round(width * horiRatio),
                Math.round(height * vertRatio),
                true
            )
            var tileX = TileMode.REPEAT
            var tileY = TileMode.REPEAT
            when (flip) {
                Flip_Horizontal -> {
                    tileX = TileMode.MIRROR
                    tileY = TileMode.MIRROR
                    tileX = TileMode.MIRROR
                    tileY = TileMode.MIRROR
                }

                Flip_Vertical -> {
                    tileY = TileMode.MIRROR
                    tileX = TileMode.MIRROR
                    tileY = TileMode.MIRROR
                }

                Flip_Both -> {
                    tileX = TileMode.MIRROR
                    tileY = TileMode.MIRROR
                }
            }
            shader = BitmapShader(bmp, tileX, tileY)
            return shader
        } catch (e: Exception) {
            return null
        }
    }

    var offsetX: Int = 0
    var offsetY: Int = 0

    companion object {
        //no mirror
        const val Flip_None: Int = 0
        //mirror in horizontal
        const val Flip_Horizontal: Int = 1
        //mirror in vertical
        const val Flip_Vertical: Int = 2
        //mirror in horizontal and vertical
        const val Flip_Both: Int = 3
        fun getBitmap(
            control: IControl?,
            viewIndex: Int,
            picture: Picture?,
            rect: Rect?,
            options: BitmapFactory.Options?
        ): Bitmap? {
            if (control == null || picture == null || rect == null) return null
            var options = options
            try {
                val path = picture.tempFilePath ?: return null
                var sBitmap = control.getSysKit().getPictureManage().getBitmap(path)
                if (sBitmap == null) {
                    val imageType = picture.pictureType
                    if (imageType == Picture.Companion.WMF || imageType == Picture.Companion.EMF) {
                        val dst = control.getSysKit().getPictureManage().convertVectorgraphToPng(
                            viewIndex,
                            imageType,
                            path,
                            rect.width(),
                            rect.height(),
                            true
                        )
                        val `in`: InputStream = FileInputStream(dst)
                        sBitmap = BitmapFactory.decodeStream(`in`, null, options)
                    } else {
                        val `in`: InputStream = FileInputStream(path)
                        sBitmap = BitmapFactory.decodeStream(`in`, null, options)
                    }
                    if (sBitmap == null) {
                        return null
                    }
                    control.getSysKit().getPictureManage().addBitmap(path, sBitmap)
                }

                return sBitmap
            } catch (e: OutOfMemoryError) {
                if (control.getSysKit().getPictureManage().hasBitmap()) {
                    control.getSysKit().getPictureManage().clearBitmap()
                    return getBitmap(control, viewIndex, picture, rect, options)
                } else {
                    if (options == null) {
                        options = BitmapFactory.Options()
                        options.inSampleSize = 2
                    } else {
                        options.inSampleSize *= 2
                    }
                    return getBitmap(control, viewIndex, picture, rect, options)
                }
            } catch (e: Exception) {
                return null
            }
        }
    }
}
