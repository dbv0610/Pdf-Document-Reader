// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * RestoreDC TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: RestoreDC.java 10367 2007-01-22 19:26:48Z duns $
 */
class RestoreDC() : EMFTag(34, 1) {
    private var savedDC = -1

    constructor(savedDC: Int) : this() {
        this.savedDC = savedDC
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return RestoreDC(emf.readDWORD())
    }


    override fun toString(): String {
        return super.toString() + "\n  savedDC: " + savedDC
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.retoreDC()
    }
}
