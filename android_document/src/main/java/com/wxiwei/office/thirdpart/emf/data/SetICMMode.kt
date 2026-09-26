// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetICMMode TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetICMMode.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetICMMode() : EMFTag(98, 1), EMFConstants {
    private var mode = 0

    constructor(mode: Int) : this() {
        this.mode = mode
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetICMMode(emf.readDWORD())
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
        // do nothing
    }
}
