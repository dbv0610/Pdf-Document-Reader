// Copyright 2002-2003, FreeHEP.
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
 * BitBlt TAG. Encoded as plain RGB rather than the not-yet-working PNG format.
 * The BI_code for BI_PNG and BI_JPG seems to be missing from the WINGDI.H file
 * of visual C++.
 * 
 * @author Mark Donszelmann
 * @version $Id: BitBlt.java 10367 2007-01-22 19:26:48Z duns $
 */
class BitBlt() : EMFTag(76, 1), EMFConstants {
    private var bounds: Rectangle? = null

    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0

    private var dwROP = 0

    private var xSrc = 0
    private var ySrc = 0

    private var transform: AffineTransform? = null

    private var bkg: Color? = null

    private var usage = 0

    private var bmi: BitmapInfo? = null

    //private BufferedImage image;
    private var image: Bitmap? = null

    constructor(
        bounds: Rectangle, x: Int, y: Int, width: Int, height: Int, transform: AffineTransform?,
        image: Bitmap?, bkg: Color?
    ) : this() {
        this.bounds = bounds
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.dwROP = EMFConstants.SRCCOPY
        this.xSrc = 0
        this.ySrc = 0
        this.transform = transform
        this.bkg = bkg
        this.usage = EMFConstants.DIB_RGB_COLORS
        this.image = image
        this.bmi = null
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val tag = BitBlt()

        tag.bounds = emf.readRECTL() // 16
        tag.x = emf.readLONG() // 20
        tag.y = emf.readLONG() // 24
        tag.width = emf.readLONG() // 28
        tag.height = emf.readLONG() // 32
        tag.dwROP = emf.readDWORD() // 36
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

        // read bmi
        if (bmiSize > 0) {
            tag.bmi = BitmapInfo(emf)
        } else {
            tag.bmi = null
        }

        if (bitmapSize > 0 && tag.bmi != null) {
            tag.image = EMFImageLoader.readImage(
                tag.bmi!!.getHeader(), tag.width, tag.height, emf,
                bitmapSize, null
            )
        } else {
            tag.image = null
        }

        return tag
    }

    override fun toString(): String {
        return (super.toString() + "\n  bounds: " + bounds + "\n  x, y, w, h: " + x + " " + y + " "
                + width + " " + height + "\n  dwROP: 0x" + Integer.toHexString(dwROP)
                + "\n  xSrc, ySrc: " + xSrc + " " + ySrc + "\n  transform: " + transform + "\n  bkg: "
                + bkg + "\n  usage: " + usage + "\n"
                + (if (bmi != null) bmi.toString() else "  bitmap: null"))
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        if (image != null) {
            renderer.drawImage(image, transform)
        } else if (!bounds!!.isEmpty() && dwROP == 0x00F00021) {
            bounds!!.x = x
            bounds!!.y = y
            //renderer.setTextBkColor();
            renderer.fillShape(bounds)
        }
        val currentFigure = renderer.getFigure()
        // fills the current path
        if (currentFigure != null) {
            renderer.fillAndDrawShape(currentFigure)
        }
    }

    companion object {
        private const val size = 100
    }
}
