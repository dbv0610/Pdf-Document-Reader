// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetROP2 TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetROP2.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetROP2() : EMFTag(20, 1), EMFConstants {
    private var mode = 0

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetROP2(emf.readDWORD())
    }


    override fun toString(): String {
        return super.toString() + "\n  mode: " + mode
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The SetROP2 function sets the current foreground mix mode.
        // GDI uses the foreground mix mode to combine pens and interiors
        // of filled objects with the colors already on the screen. The
        // foreground mix mode defines how colors from the brush or pen
        // and the colors in the existing image are to be combined.
        renderer.setRop2(mode)
    }
}
