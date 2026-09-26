// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFRenderer

/**
 * Parent class for a group of PolyLines. Childs are
 * rendered as not closed polygons.
 * 
 * @author Steffen Greiffenberg
 * @version $Id$?
 */
abstract class AbstractPolyPolyline protected constructor(
    id: Int,
    version: Int,
    bounds: Rectangle?,
    numberOfPoints: IntArray?,
    points: Array<Array<Point?>?>?
) : AbstractPolyPolygon(id, version, bounds, numberOfPoints, points) {
    /**
     * displays the tag using the renderer. The default behavior
     * is not to close the polygons and not to fill them.
     * 
     * @param renderer EMFRenderer storing the drawing session data
     */
    override fun render(renderer: EMFRenderer) {
        render(renderer, false)
    }
}
