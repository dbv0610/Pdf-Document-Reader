package com.wxiwei.office.thirdpart.emf.util

import android.graphics.Bitmap
import android.graphics.Canvas
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import java.io.FileInputStream
import java.io.FileOutputStream

object EMFUtil {
    /**
     * convert EMF picture ot PNG picture
     * @param strSrc
     * @param strDst
     * @param width
     * @param height
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun convert(
        strSrc: String?, strDst: String?, width: Int,
        height: Int
    ): Bitmap {
        val `is` = FileInputStream(strSrc)
        val inputStream: EMFInputStream = EMFInputStream(
            `is`,
            EMFInputStream.Companion.DEFAULT_VERSION
        )
        val header = inputStream.readHeader()
        val frameW = header.frame!!.getWidth().toInt()
        val frameH = header.frame!!.getHeight().toInt()

        val deviceW = header.device!!.width
        val deviceH = header.device!!.height

        val millimetersW = header.millimeters!!.getWidth().toInt()
        val millimetersH = header.millimeters!!.getHeight().toInt()

        val fileWidth = frameW * deviceW / millimetersW / 100 + 1
        val fileHeight = frameH * deviceH / millimetersH / 100 + 1

        val frameX = header.frame!!.x
        val frameY = header.frame!!.y

        val x = frameX * deviceW / millimetersW / 100
        val y = frameY * deviceH / millimetersH / 100

        val emfRenderer = EMFRenderer(inputStream)
        val bitmap: Bitmap
        var canvas: Canvas? = null
        if (width * height < fileWidth * fileHeight) {
            bitmap = Bitmap.createBitmap(
                width, height,
                Bitmap.Config.ARGB_8888
            )

            canvas = Canvas(bitmap)
            val sx = width.toFloat() / fileWidth
            val sy = height.toFloat() / fileHeight
            canvas.scale(sx, sy)
        } else {
            bitmap = Bitmap.createBitmap(
                fileWidth, fileHeight,
                Bitmap.Config.ARGB_8888
            )

            canvas = Canvas(bitmap)
        }

        canvas.translate(-x.toFloat(), -y.toFloat())
        emfRenderer.paint(canvas)

        val out = FileOutputStream(strDst)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()
        return bitmap
    }
}
