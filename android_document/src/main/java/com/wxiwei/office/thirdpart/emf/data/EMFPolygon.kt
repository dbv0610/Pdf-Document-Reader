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
 * PolylineTo TAG.
 * 
 * @author Mark Donszelmann
 * @version $Id: EMFPolygon.java 10367 2007-01-22 19:26:48Z duns $
 */
open class EMFPolygon : AbstractPolygon {
    constructor() : super(3, 1, null, 0, null)

    constructor(bounds: Rectangle?, numberOfPoints: Int, points: Array<Point?>?) : super(
        3,
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
        return EMFPolygon(r, n, emf.readPOINTL(n))
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        val points = this.points!!

        // Safety check.
        if (points.size > 1) {
            val path = GeneralPath(
                renderer.getWindingRule()
            )
            path.moveTo(points[0]!!.x.toFloat(), points[0]!!.y.toFloat())
            for (i in 1..<points.size) {
                path.lineTo(points[i]!!.x.toFloat(), points[i]!!.y.toFloat())
            }
            path.closePath()
            renderer.fillAndDrawOrAppend(path)
        }
    }
}
