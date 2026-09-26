// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Bitmap
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFImageLoader
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * StretchDIBits TAG. Encoded as plain RGB rather than the not-yet-working PNG
 * format. The BI_code for BI_PNG and BI_JPG seems to be missing from the
 * WINGDI.H file of visual C++.
 * 
 * @author Mark Donszelmann
 * @version $Id: StretchDIBits.java 10510 2007-01-30 23:58:16Z duns $
 */
class StretchDIBits() : EMFTag(81, 1), EMFConstants {
    private var bounds: Rectangle? = null

    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0

    private var xSrc = 0
    private var ySrc = 0
    private var widthSrc = 0
    private var heightSrc = 0

    private var usage = 0
    private var dwROP = 0

    private var bkg: Color? = null

    private var bmi: BitmapInfo? = null

    //private BufferedImage image;
    private var image: Bitmap? = null

    constructor(
        bounds: Rectangle?, x: Int, y: Int, width: Int, height: Int,
        image: Bitmap, bkg: Color?
    ) : this() {
        this.bounds = bounds
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.xSrc = 0
        this.ySrc = 0
        this.widthSrc = image.getWidth()
        this.heightSrc = image.getHeight()
        this.usage = EMFConstants.DIB_RGB_COLORS
        this.dwROP = EMFConstants.SRCCOPY

        this.bkg = bkg
        this.image = image
        this.bmi = null
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val tag = StretchDIBits()
        tag.bounds = emf.readRECTL() // 16
        tag.x = emf.readLONG() // 20
        tag.y = emf.readLONG() // 24
        tag.xSrc = emf.readLONG() // 28
        tag.ySrc = emf.readLONG() // 32
        tag.width = emf.readLONG() // 36
        tag.height = emf.readLONG() // 40
        // ignored
        emf.readDWORD() // 44
        emf.readDWORD() // 48
        emf.readDWORD() // 52
        emf.readDWORD() // 56

        tag.usage = emf.readDWORD() // 60
        tag.dwROP = emf.readDWORD() // 64
        tag.widthSrc = emf.readLONG() // 68
        tag.heightSrc = emf.readLONG() // 72

        // FIXME: this size can differ and can be placed somewhere else
        tag.bmi = BitmapInfo(emf)

        tag.image = EMFImageLoader.readImage(
            tag.bmi!!.getHeader(), tag.width, tag.height, emf, (len
                    - 72 - BitmapInfoHeader.Companion.size), null
        )

        return tag
    }


    override fun toString(): String {
        return (super.toString() + "\n  bounds: " + bounds + "\n  x, y, w, h: " + x + " " + y + " "
                + width + " " + height + "\n  xSrc, ySrc, widthSrc, heightSrc: " + xSrc + " " + ySrc
                + " " + widthSrc + " " + heightSrc + "\n  usage: " + usage + "\n  dwROP: " + dwROP
                + "\n  bkg: " + bkg + "\n" + bmi.toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The StretchDIBits function copies the color data for a rectangle of pixels in a
        // DIB to the specified destination rectangle. If the destination rectangle is larger
        // than the source rectangle, this function stretches the rows and columns of color
        // data to fit the destination rectangle. If the destination rectangle is smaller
        // than the source rectangle, this function compresses the rows and columns by using
        // the specified raster operation.
        if (image != null) {
            renderer.drawImage(image, x, y, widthSrc, heightSrc)
        }
    }

    companion object {
        const val size: Int = 80
    }
}
