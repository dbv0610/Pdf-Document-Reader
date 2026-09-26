// Copyright 2002, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFInputStream
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import java.io.IOException

/**
 * Polyline TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: Polyline.java 10367 2007-01-22 19:26:48Z duns $
 */
open class Polyline : AbstractPolygon {
    constructor() : super(4, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : super(
        4,
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
        return Polyline(r, n, emf.readPOINTL(n))
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val points = this.points
        val numberOfPoints = this.numberOfPoints

        if (points != null && points.size > 0) {
            val gp = GeneralPath(
                renderer.getWindingRule()
            )
            var p: Point
            for (point in 0..<numberOfPoints) {
                // add a point to gp
                p = points[point]!!
                if (point > 0) {
                    gp.lineTo(p.x.toFloat(), p.y.toFloat())
                } else {
                    gp.moveTo(p.x.toFloat(), p.y.toFloat())
                }
            }
            renderer.drawOrAppend(gp)
        }
    }
}
