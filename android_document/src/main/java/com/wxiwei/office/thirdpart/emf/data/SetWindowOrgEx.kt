// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetWindowOrgEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetWindowOrgEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetWindowOrgEx() : EMFTag(10, 1) {
    private var point: Point? = null

    constructor(point: Point?) : this() {
        this.point = point
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetWindowOrgEx(emf.readPOINTL())
    }

    override fun toString(): String {
        return super.toString() + "\n  point: " + point
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The SetWindowOrgEx function specifies which window point maps to
        // the window origin (0,0).
        renderer.setWindowOrigin(point)
        //renderer.resetTransformation();
    }
}
