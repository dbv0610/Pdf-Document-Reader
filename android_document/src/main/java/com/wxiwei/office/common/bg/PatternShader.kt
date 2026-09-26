package com.wxiwei.office.common.bg

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Shader.TileMode
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.system.IControl

class PatternShader(
    private val picture: Picture?,
    private val backgroundColor: Int,
    private val foregroundColor: Int
) : AShader() {
    override fun createShader(control: IControl?, viewIndex: Int, rect: Rect?): Shader? {
        try {
            var bmp: Bitmap? =
                TileShader.Companion.getBitmap(control, viewIndex, picture, rect, null)
            if (bmp != null) {
                val width = bmp.getWidth()
                val height = bmp.getHeight()
                val pixels = IntArray(width * height)
                bmp.getPixels(pixels, 0, width, 0, 0, width, height)
                for (i in 0..<width * height) {
                    if ((pixels[i] and 0xFFFFFF) == 0) {
                        pixels[i] = backgroundColor
                    } else {
                        pixels[i] = foregroundColor
                    }
                }

                bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
                val tileX = TileMode.REPEAT
                val tileY = TileMode.REPEAT

                shader = BitmapShader(bmp, tileX, tileY)
            }

            return shader
        } catch (e: Exception) {
            return null
        }
    }
}
