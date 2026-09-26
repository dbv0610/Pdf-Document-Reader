// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtCreatePen TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtCreatePen.java 10367 2007-01-22 19:26:48Z duns $
 */
class ExtCreatePen() : EMFTag(95, 1) {
    private var index = 0

    private var pen: ExtLogPen? = null

    constructor(index: Int, pen: ExtLogPen) : this() {
        this.index = index
        this.pen = pen
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        val index = emf.readDWORD()
        /* int bmiOffset = */
        emf.readDWORD()
        /* int bmiSize = */
        emf.readDWORD()
        /* int brushOffset = */
        emf.readDWORD()
        /* int brushSize = */
        emf.readDWORD()
        return ExtCreatePen(index, ExtLogPen(emf, len))
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
        renderer.storeGDIObject(index, pen)
    }
}
