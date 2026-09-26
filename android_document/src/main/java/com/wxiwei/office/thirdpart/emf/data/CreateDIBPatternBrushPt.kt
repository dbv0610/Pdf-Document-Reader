/**
 * $Id$
 * 
 * Copyright (c) 1998-2006
 * semture GmbH
 * 
 * Alle Rechte vorbehalten
 */
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Bitmap
import com.wxiwei.office.thirdpart.emf.EMFImageLoader
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * @author Steffen Greiffenberg
 * @version $Revision$
 */
class CreateDIBPatternBrushPt : EMFTag(94, 1) {
    private var usage = 0

    private var bmi: BitmapInfo? = null

    //private BufferedImage image;
    private var image: Bitmap? = null

    private var index = 0

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val tag = CreateDIBPatternBrushPt()

        // read the index for storing the GDIObject
        tag.index = emf.readDWORD()

        // read whatever
        /*byte[] bytes =*/
        emf.readByte(24)

        tag.bmi = BitmapInfo(emf)

        // not used but read:
        // DIB_PAL_COLORS 	A color table is provided and
        // consists of an array of 16-bit indexes into the
        // logical palette of the device context into which
        // the brush is to be selected.
        // DIB_RGB_COLORS 	A color table is provided and
        // contains literal RGB values.
        tag.usage = emf.readDWORD()

        tag.image = EMFImageLoader.readImage(
            tag.bmi!!.getHeader(),
            tag.bmi!!.getHeader().width,
            tag.bmi!!.getHeader().height,
            emf,
            len - 4 - 24 - BitmapInfoHeader.Companion.size - 4, null
        )

        return tag
    }

    override fun toString(): String {
        return super.toString() +
                "\n  usage: " + usage +
                "\n" + bmi.toString()
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
        renderer.storeGDIObject(index, object : GDIObject {
            override fun render(renderer: EMFRenderer) {
                if (image != null) {
//                    renderer.setBrushPaint(new TexturePaint(image, new Rectangle(0, 0, 16, 16)));
                    renderer.setBrushPaint(image)
                }
            }
        })
    }
}
