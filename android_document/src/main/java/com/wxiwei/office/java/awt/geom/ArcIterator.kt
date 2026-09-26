/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * A utility class to iterate over the path segments of an arc
 * through the PathIterator interface.
 *
 * @author  Jim Graham
 */
internal class ArcIterator(a: Arc2D, var affine: AffineTransform?) : PathIterator {
    var x: Double
    var y: Double
    var w: Double = a.getWidth() / 2
    var h: Double = a.getHeight() / 2
    var angStRad: Double
    var increment = 0.0
    var cv = 0.0
    var index = 0
    var arcSegs: Int
    var lineSegs = 0

    init {
        this.x = a.getX() + w
        this.y = a.getY() + h
        this.angStRad = -Math.toRadians(a.getAngleStart())
        val ext = -a.getAngleExtent()
        if (ext >= 360.0 || ext <= -360) {
            arcSegs = 4
            this.increment = Math.PI / 2
            // btan(Math.PI / 2);
            this.cv = 0.5522847498307933
            if (ext < 0) {
                increment = -increment
                cv = -cv
            }
        } else {
            arcSegs = Math.ceil(Math.abs(ext) / 90.0).toInt()
            this.increment = Math.toRadians(ext / arcSegs)
            this.cv = btan(increment)
            if (cv == 0.0) {
                arcSegs = 0
            }
        }
        when (a.getArcType()) {
            Arc2D.OPEN -> lineSegs = 0
            Arc2D.CHORD -> lineSegs = 1
            Arc2D.PIE -> lineSegs = 2
        }
        if (w < 0 || h < 0) {
            lineSegs = -1
            arcSegs = -1
        }
    }

    /**
     * Return the winding rule for determining the insideness of the
     * path.
     * @see PathIterator.WIND_EVEN_ODD
     * @see PathIterator.WIND_NON_ZERO
     */
    override fun getWindingRule(): Int {
        return PathIterator.WIND_NON_ZERO
    }

    /**
     * Tests if there are more points to read.
     * @return true if there are more points to read
     */
    override fun isDone(): Boolean {
        return index > arcSegs + lineSegs
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    override fun next() {
        index++
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: FloatArray): Int {
        if (isDone()) {
            throw NoSuchElementException("arc iterator out of bounds")
        }
        var angle = angStRad
        if (index == 0) {
            coords[0] = (x + Math.cos(angle) * w).toFloat()
            coords[1] = (y + Math.sin(angle) * h).toFloat()
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_MOVETO
        }
        if (index > arcSegs) {
            if (index == arcSegs + lineSegs) {
                return PathIterator.SEG_CLOSE
            }
            coords[0] = x.toFloat()
            coords[1] = y.toFloat()
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_LINETO
        }
        angle += increment * (index - 1)
        var relx = Math.cos(angle)
        var rely = Math.sin(angle)
        coords[0] = (x + (relx - cv * rely) * w).toFloat()
        coords[1] = (y + (rely + cv * relx) * h).toFloat()
        angle += increment
        relx = Math.cos(angle)
        rely = Math.sin(angle)
        coords[2] = (x + (relx + cv * rely) * w).toFloat()
        coords[3] = (y + (rely - cv * relx) * h).toFloat()
        coords[4] = (x + relx * w).toFloat()
        coords[5] = (y + rely * h).toFloat()
        affine?.transform(coords, 0, coords, 0, 3)
        return PathIterator.SEG_CUBICTO
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("arc iterator out of bounds")
        }
        var angle = angStRad
        if (index == 0) {
            coords[0] = x + Math.cos(angle) * w
            coords[1] = y + Math.sin(angle) * h
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_MOVETO
        }
        if (index > arcSegs) {
            if (index == arcSegs + lineSegs) {
                return PathIterator.SEG_CLOSE
            }
            coords[0] = x
            coords[1] = y
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_LINETO
        }
        angle += increment * (index - 1)
        var relx = Math.cos(angle)
        var rely = Math.sin(angle)
        coords[0] = x + (relx - cv * rely) * w
        coords[1] = y + (rely + cv * relx) * h
        angle += increment
        relx = Math.cos(angle)
        rely = Math.sin(angle)
        coords[2] = x + (relx + cv * rely) * w
        coords[3] = y + (rely - cv * relx) * h
        coords[4] = x + relx * w
        coords[5] = y + rely * h
        affine?.transform(coords, 0, coords, 0, 3)
        return PathIterator.SEG_CUBICTO
    }

    companion object {
        /*
         * btan computes the length (k) of the control segments at
         * the beginning and end of a cubic bezier that approximates
         * a segment of an arc with extent less than or equal to
         * 90 degrees.  This length (k) will be used to generate the
         * 2 bezier control points for such a segment.
         *
         * k = 4 / 3 * (1 - cos(angb)) / sin(angb)
         *   = 4 / 3 * sin(angb) / (1 + cos(angb))
         */
        private fun btan(increment: Double): Double {
            val half = increment / 2.0
            return 4.0 / 3.0 * Math.sin(half) / (1.0 + Math.cos(half))
        }
    }
}
