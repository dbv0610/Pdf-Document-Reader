// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * PolyBezierTo TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyBezierTo.java 10367 2007-01-22 19:26:48Z duns $
 */
open class PolyBezierTo : AbstractPolygon {
    constructor() : super(5, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : super(
        5,
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
        return PolyBezierTo(r, n, emf.readPOINTL(n))
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

        if (points != null && points.size > 0) {
            var p1: Point
            var p2: Point
            var p3: Point
            var point = 0
            while (point < numberOfPoints) {
                // add a point to gp
                p1 = points[point]!!
                p2 = points[point + 1]!!
                p3 = points[point + 2]!!
                currentFigure.curveTo(
                    p1.x.toFloat(), p1.y.toFloat(),
                    p2.x.toFloat(), p2.y.toFloat(),
                    p3.x.toFloat(), p3.y.toFloat()
                )
                point = point + 3
            }
        }
    }
}
