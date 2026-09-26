// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Ellipse2D
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * Ellipse TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: Ellipse.java 10367 2007-01-22 19:26:48Z duns $
 */
class Ellipse() : EMFTag(42, 1) {
    private var bounds: Rectangle? = null

    constructor(bounds: Rectangle) : this() {
        this.bounds = bounds
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return Ellipse(emf.readRECTL())
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
        // The Ellipse function draws an ellipse. The center of the ellipse
        // is the center of the specified bounding rectangle.
        // The ellipse is outlined by using the current pen and is filled by
        // using the current brush.
        // The current position is neither used nor updated by Ellipse.
        renderer.fillAndDrawOrAppend(
            Ellipse2D.Double(
                bounds!!.x.toDouble(), bounds!!.y.toDouble(), bounds!!
                    .getWidth(), bounds!!.getHeight()
            )
        )
    }
}
