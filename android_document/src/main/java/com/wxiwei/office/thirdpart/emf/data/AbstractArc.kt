// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import android.graphics.Point
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.geom.Arc2D
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import com.wxiwei.office.thirdpart.emf.EMFTag
import kotlin.math.abs
import kotlin.math.atan


/**
 * @author Steffen Greiffenberg
 * @version $Id$
 */
abstract class AbstractArc protected constructor(
    id: Int,
    version: Int,
    private val bounds: Rectangle?,
    private val start: Point?,
    private val end: Point?
) : EMFTag(id, version) {
    override fun toString(): String {
        return (super.toString() + "\n  bounds: " + bounds + "\n  start: " + start + "\n  end: "
                + end)
    }

    protected fun getAngle(point: Point): Double {
        val circleX = bounds!!.getX() + bounds!!.getWidth() / 2
        val circleY = bounds!!.getY() + bounds!!.getHeight() / 2

        val x = point.x.toDouble()
        val y = point.y.toDouble()

        var alpha = 0.0
        if (x > circleX) {
            val nx = abs(y - circleY) / (x - circleX)
            alpha = atan(nx) / Math.PI * 180

            if (y > circleY) {
                alpha = 360 - alpha
            }
        } else if (x == circleX) {
            alpha = (if (y < circleY) 90 else 270).toDouble()
        } else {
            val nx = abs(y - circleY) / (circleX - x)
            alpha = atan(nx) / Math.PI * 180

            if (y < circleY) {
                alpha = 180 - alpha
            } else {
                alpha += 180.0
            }
        }

        return alpha
    }

    /**
     * creates a shape based on bounds, start and end
     * 
     * @param renderer EMFRenderer storing the drawing session data
     * @param arcType type of arc, e.g. [Arc2D.OPEN]
     * @return shape to render
     */
    protected fun getShape(renderer: EMFRenderer, arcType: Int): Shape {
        // normalize start and end point to a circle
        //double nx0 = start!!.x / bounds!!.getWidth();

        // double ny0 = arc.getStart().y / arc.getBounds().height;
        //double nx1 = end!!.x / bounds!!.getWidth();

        // double ny1 = arc.getEnd().y / arc.getBounds().height;
        // calculate angle of start point

        val alpha0: Double
        val alpha1: Double
        if (renderer.getArcDirection() == EMFConstants.AD_CLOCKWISE) {
//            alpha0 = Math.acos(nx0);
//            alpha1 = Math.acos(nx1);
            alpha0 = getAngle(end!!)
            alpha1 = getAngle(start!!)
        } else {
//            alpha0 = Math.acos(nx1);
//            alpha1 = Math.acos(nx0);
            alpha0 = getAngle(start!!)
            alpha1 = getAngle(end!!)
        }

        var extent = 0.0

        if (alpha1 > alpha0) {
            extent = alpha1 - alpha0
        } else {
            extent = 360 - (alpha0 - alpha1)
        }

        return Arc2D.Double(
            bounds!!.getX(), bounds!!.getY(), bounds!!.getWidth(), bounds!!.getHeight(),
            alpha0, extent, arcType
        )
    }
}
