// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * LineTo TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: LineTo.java 10367 2007-01-22 19:26:48Z duns $
 */
class LineTo() : EMFTag(54, 1) {
    private var point: Point? = null

    constructor(point: Point) : this() {
        this.point = point
    }

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return LineTo(emf.readPOINTL())
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
        // The LineTo function draws a line from the current position up to,
        // but not including, the specified point.
        // The line is drawn by using the current pen and, if the pen is a
        // geometric pen, the current brush.
        var currentFigure = renderer.getFigure()
        if (currentFigure != null) {
            currentFigure.lineTo(point!!.x.toFloat(), point!!.y.toFloat())
            renderer.drawShape(currentFigure)
        } else {
            currentFigure = GeneralPath(renderer.getWindingRule())
            currentFigure.moveTo(point!!.x.toFloat(), point!!.y.toFloat())
            renderer.setFigure(currentFigure)
        }
    }
}
