// Copyright 2002-2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Arc2D
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * Arc TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: Arc.java 10377 2007-01-23 15:44:34Z duns $
 */
class Arc : AbstractArc {
    constructor() : super(45, 1, null, null, null)

    constructor(bounds: Rectangle?, start: Point?, end: Point?) : super(45, 1, bounds, start, end)

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag {
        return Arc(
            emf.readRECTL(),
            emf.readPOINTL(),
            emf.readPOINTL()
        )
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        // The Arc function draws an elliptical arc.
        //
        // BOOL Arc(
        // HDC hdc, // handle to device context
        // int nLeftRect, // x-coord of rectangle's upper-left corner
        // int nTopRect, // y-coord of rectangle's upper-left corner
        // int nRightRect, // x-coord of rectangle's lower-right corner
        // int nBottomRect, // y-coord of rectangle's lower-right corner
        // int nXStartArc, // x-coord of first radial ending point
        // int nYStartArc, // y-coord of first radial ending point
        // int nXEndArc, // x-coord of second radial ending point
        // int nYEndArc // y-coord of second radial ending point
        // );
        // The points (nLeftRect, nTopRect) and (nRightRect, nBottomRect)
        // specify the bounding rectangle.
        // An ellipse formed by the specified bounding rectangle defines the
        // curve of the arc.
        // The arc extends in the current drawing direction from the point
        // where it intersects the
        // radial from the center of the bounding rectangle to the
        // (nXStartArc, nYStartArc) point.
        // The arc ends where it intersects the radial from the center of
        // the bounding rectangle to
        // the (nXEndArc, nYEndArc) point. If the starting point and ending
        // point are the same,
        // a complete ellipse is drawn.

        renderer.fillAndDrawOrAppend(
            getShape(renderer, Arc2D.OPEN)
        )
    }
}
