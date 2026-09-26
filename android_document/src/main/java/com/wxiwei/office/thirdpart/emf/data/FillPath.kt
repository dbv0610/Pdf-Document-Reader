// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * FillPath TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: FillPath.java 10367 2007-01-22 19:26:48Z duns $
 */
class FillPath() : EMFTag(62, 1) {
    private var bounds: Rectangle? = null

    constructor(bounds: Rectangle?) : this() {
        this.bounds = bounds
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return FillPath(emf.readRECTL())
    }

    override fun toString(): String {
        return super.toString() + "\n  bounds: " + bounds
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val currentPath = renderer.getPath()
        // fills the current path
        if (currentPath != null) {
            renderer.fillShape(currentPath)
            renderer.setPath(null)
        }
    }
}
