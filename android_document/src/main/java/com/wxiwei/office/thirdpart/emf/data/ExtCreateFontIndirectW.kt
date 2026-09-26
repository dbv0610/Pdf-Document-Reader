// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * ExtCreateFontIndirectW TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: ExtCreateFontIndirectW.java 10367 2007-01-22 19:26:48Z duns $
 */
class ExtCreateFontIndirectW() : EMFTag(82, 1) {
    private var index = 0

    private var font: ExtLogFontW? = null

    constructor(index: Int, font: ExtLogFontW) : this() {
        this.index = index
        this.font = font
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return ExtCreateFontIndirectW(emf.readDWORD(), ExtLogFontW(emf))
    }

    override fun toString(): String {
        return (super.toString() + "\n  index: 0x" + Integer.toHexString(index) + "\n"
                + font.toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.storeGDIObject(index, font)
    }
}
