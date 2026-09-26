// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * CreateBrushIndirect TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: CreateBrushIndirect.java 10367 2007-01-22 19:26:48Z duns $
 */
class CreateBrushIndirect() : EMFTag(39, 1) {
    private var index = 0

    private var brush: LogBrush32? = null

    constructor(index: Int, brush: LogBrush32) : this() {
        this.index = index
        this.brush = brush
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return CreateBrushIndirect(emf.readDWORD(), LogBrush32(emf))
    }

    override fun toString(): String {
        return (super.toString() + "\n  index: 0x" + Integer.toHexString(index) + "\n"
                + brush.toString())
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // CreateBrushIndirect
        //
        // The CreateBrushIndirect function creates a logical brush that has the
        // specified style, color, and pattern.
        //
        // HBRUSH CreateBrushIndirect(
        //   CONST LOGBRUSH *lplb   // brush information
        // );
        renderer.storeGDIObject(index, brush)
    }
}
