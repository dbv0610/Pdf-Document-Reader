// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolylineTo TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolylineTo.java 10367 2007-01-22 19:26:48Z duns $
 */
open class PolylineTo : AbstractPolygon {
    constructor() : super(6, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : this(
        6,
        1,
        bounds,
        numberOfPoints,
        points
    )

    protected constructor(
        id: Int,
        version: Int,
        bounds: Rectangle?,
        numberOfPoints: Int,
        points: Array<Point?>?
    ) : super(id, version, bounds, numberOfPoints, points)

    @Throws(IOException::class)
    override fun read(tagID: Int, emf: EMFInputStream, len: Int): EMFTag? {
        val r = emf.readRECTL()
        val n = emf.readDWORD()
        return PolylineTo(r, n, emf.readPOINTL(n))
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val points = this.points
        val numberOfPoints = this.numberOfPoints
        val currentFigure = renderer.getFigure()

        if (points != null) {
            for (point in 0..<numberOfPoints) {
                // add a point to gp
                currentFigure.lineTo(
                    points[point]!!.x.toFloat(),
                    points[point]!!.y.toFloat()
                )
            }
        }
    }
}
