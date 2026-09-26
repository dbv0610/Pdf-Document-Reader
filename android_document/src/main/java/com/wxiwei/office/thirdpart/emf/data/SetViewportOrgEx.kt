// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetViewportOrgEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetViewportOrgEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetViewportOrgEx() : EMFTag(12, 1) {
    private var point: Point? = null

    constructor(point: Point?) : this() {
        this.point = point
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetViewportOrgEx(emf.readPOINTL())
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
        // The SetViewportOrgEx function specifies which device point maps
        // to the viewport origin (0,0).

        // This function (along with SetViewportExtEx and SetWindowExtEx) helps
        // define the mapping from the logical coordinate space (also known as a
        // window) to the device coordinate space (the viewport). SetViewportOrgEx
        // specifies which device point maps to the logical point (0,0). It has the
        // effect of shifting the axes so that the logical point (0,0) no longer
        // refers to the upper-left corner.

        renderer.setViewportOrigin(point)
        //renderer.resetTransformation();
    }
}
