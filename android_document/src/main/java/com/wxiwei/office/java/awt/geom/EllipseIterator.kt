/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * A utility class to iterate over the path segments of an ellipse
 * through the PathIterator interface.
 *
 * @author  Jim Graham
 */
internal class EllipseIterator(e: Ellipse2D, var affine: AffineTransform?) : PathIterator {
    var x: Double = e.getX()
    var y: Double = e.getY()
    var w: Double = e.getWidth()
    var h: Double = e.getHeight()
    var index = 0

    init {
        if (w < 0 || h < 0) {
            index = 6
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
        return index > 5
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
            throw NoSuchElementException("ellipse iterator out of bounds")
        }
        if (index == 5) {
            return PathIterator.SEG_CLOSE
        }
        if (index == 0) {
            val ctrls = ctrlpts[3]
            coords[0] = (x + ctrls[4] * w).toFloat()
            coords[1] = (y + ctrls[5] * h).toFloat()
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_MOVETO
        }
        val ctrls = ctrlpts[index - 1]
        coords[0] = (x + ctrls[0] * w).toFloat()
        coords[1] = (y + ctrls[1] * h).toFloat()
        coords[2] = (x + ctrls[2] * w).toFloat()
        coords[3] = (y + ctrls[3] * h).toFloat()
        coords[4] = (x + ctrls[4] * w).toFloat()
        coords[5] = (y + ctrls[5] * h).toFloat()
        affine?.transform(coords, 0, coords, 0, 3)
        return PathIterator.SEG_CUBICTO
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("ellipse iterator out of bounds")
        }
        if (index == 5) {
            return PathIterator.SEG_CLOSE
        }
        if (index == 0) {
            val ctrls = ctrlpts[3]
            coords[0] = x + ctrls[4] * w
            coords[1] = y + ctrls[5] * h
            affine?.transform(coords, 0, coords, 0, 1)
            return PathIterator.SEG_MOVETO
        }
        val ctrls = ctrlpts[index - 1]
        coords[0] = x + ctrls[0] * w
        coords[1] = y + ctrls[1] * h
        coords[2] = x + ctrls[2] * w
        coords[3] = y + ctrls[3] * h
        coords[4] = x + ctrls[4] * w
        coords[5] = y + ctrls[5] * h
        affine?.transform(coords, 0, coords, 0, 3)
        return PathIterator.SEG_CUBICTO
    }

    companion object {
        // ArcIterator.btan(Math.PI/2)
        const val CtrlVal = 0.5522847498307933

        /*
         * ctrlpts contains the control points for a set of 4 cubic
         * bezier curves that approximate a circle of radius 0.5
         * centered at 0.5, 0.5
         */
        private const val pcv = 0.5 + CtrlVal * 0.5
        private const val ncv = 0.5 - CtrlVal * 0.5
        private val ctrlpts = arrayOf(
            doubleArrayOf(1.0, pcv, pcv, 1.0, 0.5, 1.0),
            doubleArrayOf(ncv, 1.0, 0.0, pcv, 0.0, 0.5),
            doubleArrayOf(0.0, ncv, ncv, 0.0, 0.5, 0.0),
            doubleArrayOf(pcv, 0.0, 1.0, ncv, 1.0, 0.5)
        )
    }
}
