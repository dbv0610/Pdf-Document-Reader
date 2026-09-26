// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetBkColor TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetBkColor.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetBkColor() : EMFTag(25, 1) {
    private var color: Color? = null

    constructor(color: Color?) : this() {
        this.color = color
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetBkColor(emf.readCOLORREF())
    }

    override fun toString(): String {
        return super.toString() + "\n  color: " + color
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // This function fills the gaps between styled lines drawn using a
        // pen created by the CreatePen function; it does not fill the gaps
        // between styled lines drawn using a pen created by the ExtCreatePen
        // function. The SetBKColor function also sets the background colors
        // for TextOut and ExtTextOut.

        // If the background mode is OPAQUE, the background color is used to
        // fill gaps between styled lines, gaps between hatched lines in brushes,
        // and character cells. The background color is also used when converting
        // bitmaps from color to monochrome and vice versa.

        // TODO: affects TextOut and ExtTextOut, CreatePen
    }
}
