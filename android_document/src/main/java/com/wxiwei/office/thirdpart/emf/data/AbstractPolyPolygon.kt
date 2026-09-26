// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.GeneralPath
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag

/**
 * abstract parent for PolyPolygon drawing
 * 
 * @author Steffen Greiffenberg
 * @version $Id$
 */
abstract class AbstractPolyPolygon
/**
 * Constructs a EMFTag.
 * 
 * @param id      id of the element
 * @param version emf version in which this element was first supported
 * @param bounds bounds of figure
 * @param numberOfPoints number of points
 * @param points points
 */ protected constructor(
    id: Int, version: Int,
    protected val bounds: Rectangle?,
    protected val numberOfPoints: IntArray?,
    protected val points: Array<Array<Point?>?>?
) : EMFTag(id, version) {
    override fun toString(): String {
        return super.toString() +
                "\n  bounds: " + bounds +
                "\n  #polys: " + numberOfPoints!!.size
    }

    /**
     * displays the tag using the renderer. The default behavior
     * is to close and fill the polgygons for rendering.
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        render(renderer, true)
    }

    /**
     * displays the tag using the renderer
     * 
     * @param renderer EMFRenderer storing the drawing session data
     * @param closePath if true the path is closed and filled
     */
    protected fun render(renderer: EMFRenderer, closePath: Boolean) {
        // create a GeneralPath containing GeneralPathes
        val path = GeneralPath(
            renderer.getWindingRule()
        )

        // iterate the polgons
        var p: Point
        for (polygon in numberOfPoints!!.indices) {
            // create a new member of path

            val gp = GeneralPath(
                renderer.getWindingRule()
            )
            for (point in 0..<numberOfPoints!![polygon]) {
                // add a point to gp
                p = points!![polygon]!![point]!!
                if (point > 0) {
                    gp.lineTo(p.x.toFloat(), p.y.toFloat())
                } else {
                    gp.moveTo(p.x.toFloat(), p.y.toFloat())
                }
            }

            // close the member, add it to path
            if (closePath) {
                gp.closePath()
            }

            path.append(gp, false)
        }

        // draw the complete path
        if (closePath) {
            renderer.fillAndDrawOrAppend(path)
        } else {
            renderer.drawOrAppend(path)
        }
    }
}
