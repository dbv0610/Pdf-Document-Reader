/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * A utility class to iterate over the path segments of a quadratic curve
 * segment through the PathIterator interface.
 *
 * @author  Jim Graham
 */
internal class QuadIterator(var quad: QuadCurve2D, var affine: AffineTransform?) : PathIterator {
    var index = 0

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
        return (index > 1)
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
            throw NoSuchElementException("quad iterator iterator out of bounds")
        }
        val type: Int
        if (index == 0) {
            coords[0] = quad.getX1().toFloat()
            coords[1] = quad.getY1().toFloat()
            type = PathIterator.SEG_MOVETO
        } else {
            coords[0] = quad.getCtrlX().toFloat()
            coords[1] = quad.getCtrlY().toFloat()
            coords[2] = quad.getX2().toFloat()
            coords[3] = quad.getY2().toFloat()
            type = PathIterator.SEG_QUADTO
        }
        affine?.transform(coords, 0, coords, 0, if (index == 0) 1 else 2)
        return type
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("quad iterator iterator out of bounds")
        }
        val type: Int
        if (index == 0) {
            coords[0] = quad.getX1()
            coords[1] = quad.getY1()
            type = PathIterator.SEG_MOVETO
        } else {
            coords[0] = quad.getCtrlX()
            coords[1] = quad.getCtrlY()
            coords[2] = quad.getX2()
            coords[3] = quad.getY2()
            type = PathIterator.SEG_QUADTO
        }
        affine?.transform(coords, 0, coords, 0, if (index == 0) 1 else 2)
        return type
    }
}
