// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * MoveToEx TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: MoveToEx.java 10367 2007-01-22 19:26:48Z duns $
 */
class MoveToEx() : EMFTag(27, 1) {
    private var point: Point? = null

    constructor(point: Point) : this() {
        this.point = point
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return MoveToEx(emf.readPOINTL())
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
        // The MoveToEx function updates the current position to the
        // specified point
        // and optionally returns the previous position.
        val currentFigure = GeneralPath(renderer.getWindingRule())
        currentFigure.moveTo(point!!.x.toFloat(), point!!.y.toFloat())
        renderer.setFigure(currentFigure)
    }
}
