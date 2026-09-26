// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Dimension
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.RoundRectangle2D
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * RoundRect TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: RoundRect.java 10377 2007-01-23 15:44:34Z duns $
 */
class RoundRect() : EMFTag(44, 1) {
    private var bounds: Rectangle? = null

    private var corner: Dimension? = null

    constructor(bounds: Rectangle, corner: Dimension) : this() {
        this.bounds = bounds
        this.corner = corner
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return RoundRect(emf.readRECTL(), emf.readSIZEL())
    }

    override fun toString(): String {
        return super.toString() + "\n  bounds: " + bounds + "\n  corner: " + corner
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        renderer.fillAndDrawOrAppend(
            RoundRectangle2D.Double(
                bounds!!.x.toDouble(), bounds!!.x.toDouble(),
                bounds!!.getWidth(), bounds!!.getHeight(), corner!!.getWidth(), corner!!.getHeight()
            )
        )
    }
}
