// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * SetViewportExtEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: SetViewportExtEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class SetViewportExtEx() : EMFTag(11, 1) {
    private var size: Dimension? = null

    constructor(size: Dimension?) : this() {
        this.size = size
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return SetViewportExtEx(emf.readSIZEL())
    }

    override fun toString(): String {
        return super.toString() + "\n  size: " + size
    }

    // The SetViewportExtEx function sets the horizontal and vertical
    // extents of the viewport for a device context by using the specified values.
    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The viewport refers to the device coordinate system of the device space.
        // The extent is the maximum value of an axis. This function sets the maximum
        // values for the horizontal and vertical axes of the viewport in device
        // coordinates (or pixels). When mapping between page space and device space,
        // SetWindowExtEx and SetViewportExtEx determine the scaling factor between
        // the window and the viewport.
        renderer.setViewportSize(size)
    }
}
