/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

/**
 * The `PathIterator` interface provides the mechanism
 * for objects that implement the [Shape][com.wxiwei.office.java.awt.Shape]
 * interface to return the geometry of their boundary by allowing
 * a caller to retrieve the path of that boundary a segment at a
 * time.
 */
interface PathIterator {

    companion object {
        /**
         * The winding rule constant for specifying an even-odd rule
         * for determining the interior of a path.
         */
        const val WIND_EVEN_ODD = 0

        /**
         * The winding rule constant for specifying a non-zero rule
         * for determining the interior of a path.
         */
        const val WIND_NON_ZERO = 1

        /**
         * The segment type constant for a point that specifies the
         * starting location for a new subpath.
         */
        const val SEG_MOVETO = 0

        /**
         * The segment type constant for a point that specifies the
         * end point of a line to be drawn from the most recently
         * specified point.
         */
        const val SEG_LINETO = 1

        /**
         * The segment type constant for the pair of points that specify
         * a quadratic parametric curve to be drawn from the most recently
         * specified point.
         */
        const val SEG_QUADTO = 2

        /**
         * The segment type constant for the set of 3 points that specify
         * a cubic parametric curve to be drawn from the most recently
         * specified point. This form of curve is commonly known as a
         * B&eacute;zier curve.
         */
        const val SEG_CUBICTO = 3

        /**
         * The segment type constant that specifies that
         * the preceding subpath should be closed by appending a line segment
         * back to the point corresponding to the most recent SEG_MOVETO.
         */
        const val SEG_CLOSE = 4
    }

    /**
     * Returns the winding rule for determining the interior of the
     * path.
     */
    fun getWindingRule(): Int

    /**
     * Tests if the iteration is complete.
     */
    fun isDone(): Boolean

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    fun next()

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    fun currentSegment(coords: FloatArray): Int

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    fun currentSegment(coords: DoubleArray): Int
}
