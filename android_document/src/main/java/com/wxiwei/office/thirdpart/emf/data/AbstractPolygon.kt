// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.thirdpart.emf.EMFTag

/**
 * @author Steffen Greiffenberg
 * @version $Id$
 */
abstract class AbstractPolygon : EMFTag {
    protected var bounds: Rectangle? = null
        private set

    protected var numberOfPoints: Int = 0
        private set

    protected var points: Array<Point?>? = null
        private set

    protected constructor(id: Int, version: Int) : super(id, version)

    protected constructor(
        id: Int, version: Int, bounds: Rectangle?, numberOfPoints: Int,
        points: Array<Point?>?
    ) : super(id, version) {
        this.bounds = bounds
        this.numberOfPoints = numberOfPoints
        this.points = points
    }

    override fun toString(): String {
        var result = (super.toString() + "\n  bounds: " + bounds + "\n  #points: "
                + numberOfPoints)
        if (points != null) {
            result += "\n  points: "
            for (i in points!!.indices) {
                result += "[" + points!![i]!!.x + "," + points!![i]!!.y + "]"
                if (i < points!!.size - 1) {
                    result += ", "
                }
            }
        }
        return result
    }
}
