// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * CreatePen TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: CreatePen.java 10367 2007-01-22 19:26:48Z duns $
 */
class CreatePen() : EMFTag(38, 1) {
    private var index = 0

    private var pen: LogPen? = null

    constructor(index: Int, pen: LogPen) : this() {
        this.index = index
        this.pen = pen
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return CreatePen(emf.readDWORD(), LogPen(emf))
    }

    override fun toString(): String {
        return (super.toString() + "\n  index: 0x" + Integer.toHexString(index) + "\n"
                + pen.toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // ExtCreatePen
        //
        // The ExtCreatePen function creates a logical cosmetic or
        // geometric pen that has the specified style, width,
        // and brush attributes.
        //
        // HPEN ExtCreatePen(
        //  DWORD dwPenStyle,      // pen style
        //  DWORD dwWidth,         // pen width
        //  CONST LOGBRUSH *lplb,  // brush attributes
        //  DWORD dwStyleCount,    // length of custom style array
        //  CONST DWORD *lpStyle   // custom style array
        //);
        renderer.storeGDIObject(index, pen)
    }
}
