/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * A utility class to iterate over the path segments of a rectangle
 * through the PathIterator interface.
 *
 * @author  Jim Graham
 */
internal class RectIterator(r: Rectangle2D, var affine: AffineTransform?) : PathIterator {
    var x: Double = r.getX()
    var y: Double = r.getY()
    var w: Double = r.getWidth()
    var h: Double = r.getHeight()
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
            throw NoSuchElementException("rect iterator out of bounds")
        }
        if (index == 5) {
            return PathIterator.SEG_CLOSE
        }
        coords[0] = x.toFloat()
        coords[1] = y.toFloat()
        if (index == 1 || index == 2) {
            coords[0] += w.toFloat()
        }
        if (index == 2 || index == 3) {
            coords[1] += h.toFloat()
        }
        affine?.transform(coords, 0, coords, 0, 1)
        return (if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO)
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("rect iterator out of bounds")
        }
        if (index == 5) {
            return PathIterator.SEG_CLOSE
        }
        coords[0] = x
        coords[1] = y
        if (index == 1 || index == 2) {
            coords[0] += w
        }
        if (index == 2 || index == 3) {
            coords[1] += h
        }
        affine?.transform(coords, 0, coords, 0, 1)
        return (if (index == 0) PathIterator.SEG_MOVETO else PathIterator.SEG_LINETO)
    }
}
