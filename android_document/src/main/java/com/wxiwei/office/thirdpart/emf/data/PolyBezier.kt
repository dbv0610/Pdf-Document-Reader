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
 * PolyBezier TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: PolyBezier.java 10367 2007-01-22 19:26:48Z duns $
 */
open class PolyBezier : AbstractPolygon {
    constructor() : super(2, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : super(
        2,
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
        return PolyBezier(r, n, emf.readPOINTL(n))
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
            val p = points[0]!!
            gp.moveTo(p.x.toFloat(), p.y.toFloat())

            var point = 1
            while (point < numberOfPoints) {
                // add a point to gp
                val p1 = points[point]!!
                val p2 = points[point + 1]!!
                val p3 = points[point + 2]!!
                if (point > 0) {
                    gp.curveTo(
                        p1.x.toFloat(), p1.y.toFloat(),
                        p2.x.toFloat(), p2.y.toFloat(),
                        p3.x.toFloat(), p3.y.toFloat()
                    )
                }
                point = point + 3
            }
            renderer.fillAndDrawOrAppend(gp)
        }
    }
}
