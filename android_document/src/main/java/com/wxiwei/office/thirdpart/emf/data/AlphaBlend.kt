// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Bitmap
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFImageLoader
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PNG and JPG seem not to work.
 * 
 * @author Mark Donszelmann
 * @version $Id: AlphaBlend.java 10367 2007-01-22 19:26:48Z duns $
 */
class AlphaBlend() : EMFTag(114, 1), EMFConstants {
    private var bounds: Rectangle? = null

    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0

    private var dwROP: BlendFunction? = null

    private var xSrc = 0
    private var ySrc = 0

    private var transform: AffineTransform? = null

    private var bkg: Color? = null

    private var usage = 0

    private var bmi: BitmapInfo? = null

    //private BufferedImage image;
    private var image: Bitmap? = null

    constructor(
        bounds: Rectangle?, x: Int, y: Int, width: Int, height: Int,
        transform: AffineTransform?, image: Bitmap?, bkg: Color?
    ) : this() {
        this.bounds = bounds
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.dwROP = BlendFunction(EMFConstants.AC_SRC_OVER, 0, 0xFF, EMFConstants.AC_SRC_ALPHA)
        this.xSrc = 0
        this.ySrc = 0
        this.transform = transform
        this.bkg = if (bkg == null) Color(0, 0, 0, 0) else bkg
        this.usage = EMFConstants.DIB_RGB_COLORS
        this.image = image
        this.bmi = null
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val tag = AlphaBlend()
        tag.bounds = emf.readRECTL() // 16
        tag.x = emf.readLONG() // 20
        tag.y = emf.readLONG() // 24
        tag.width = emf.readLONG() // 28
        tag.height = emf.readLONG() // 32
        tag.dwROP = BlendFunction(emf) // 36
        tag.xSrc = emf.readLONG() // 40
        tag.ySrc = emf.readLONG() // 44
        tag.transform = emf.readXFORM() // 68
        tag.bkg = emf.readCOLORREF() // 72
        tag.usage = emf.readDWORD() // 76

        // ignored
        /* int bmiOffset = */
        emf.readDWORD() // 80
        val bmiSize = emf.readDWORD() // 84
        /* int bitmapOffset = */
        emf.readDWORD() // 88
        val bitmapSize = emf.readDWORD() // 92

        /* int width = */
        emf.readLONG() // 96
        /* int height = */
        emf.readLONG() // 100

        // FIXME: this size can differ and can be placed somewhere else
        tag.bmi = if (bmiSize > 0) BitmapInfo(emf) else null

        tag.image = EMFImageLoader.readImage(
            tag.bmi!!.getHeader(), tag.width, tag.height, emf,  /*bitmapSize*/
            len - 100 - BitmapInfoHeader.Companion.size, tag.dwROP
        )

        return tag
    }

    override fun toString(): String {
        return (super.toString() + "\n  bounds: " + bounds + "\n  x, y, w, h: " + x + " " + y + " "
                + width + " " + height + "\n  dwROP: " + dwROP + "\n  xSrc, ySrc: " + xSrc + " " + ySrc
                + "\n  transform: " + transform + "\n  bkg: " + bkg + "\n  usage: " + usage + "\n"
                + (if (bmi != null) bmi.toString() else "  bitmap: null"))
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // This function displays bitmaps that have transparent or semitransparent pixels.
        if (image != null) {
            renderer.drawImage(image, x, y, width, height)
        }
    }

    companion object {
        private const val size = 108
    }
}
