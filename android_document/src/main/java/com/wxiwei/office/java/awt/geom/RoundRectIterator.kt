/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * A utility class to iterate over the path segments of a rounded rectangle
 * through the PathIterator interface.
 *
 * @author  Jim Graham
 */
internal class RoundRectIterator(rr: RoundRectangle2D, var affine: AffineTransform?) : PathIterator {
    var x: Double = rr.getX()
    var y: Double = rr.getY()
    var w: Double = rr.getWidth()
    var h: Double = rr.getHeight()
    var aw: Double = Math.min(w, Math.abs(rr.getArcWidth()))
    var ah: Double = Math.min(h, Math.abs(rr.getArcHeight()))
    var index = 0

    init {
        if (aw < 0 || ah < 0) {
            // Don't draw anything...
            index = ctrlpts.size
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
        return index >= ctrlpts.size
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
            throw NoSuchElementException("roundrect iterator out of bounds")
        }
        val ctrls = ctrlpts[index]
        var nc = 0
        var i = 0
        while (i < ctrls.size) {
            coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw).toFloat()
            coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah).toFloat()
            i += 4
        }
        affine?.transform(coords, 0, coords, 0, nc / 2)
        return types[index]
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("roundrect iterator out of bounds")
        }
        val ctrls = ctrlpts[index]
        var nc = 0
        var i = 0
        while (i < ctrls.size) {
            coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw)
            coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah)
            i += 4
        }
        affine?.transform(coords, 0, coords, 0, nc / 2)
        return types[index]
    }

    companion object {
        private val angle = Math.PI / 4.0
        private val a = 1.0 - Math.cos(angle)
        private val b = Math.tan(angle)
        private val c = Math.sqrt(1.0 + b * b) - 1 + a
        private val cv = 4.0 / 3.0 * a * b / c
        private val acv = (1.0 - cv) / 2.0

        // For each array:
        //     4 values for each point {v0, v1, v2, v3}:
        //         point = (x + v0 * w + v1 * arcWidth,
        //                  y + v2 * h + v3 * arcHeight);
        private val ctrlpts = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0, 0.5),
            doubleArrayOf(0.0, 0.0, 1.0, -0.5),
            doubleArrayOf(
                0.0, 0.0, 1.0, -acv,
                0.0, acv, 1.0, 0.0,
                0.0, 0.5, 1.0, 0.0
            ),
            doubleArrayOf(1.0, -0.5, 1.0, 0.0),
            doubleArrayOf(
                1.0, -acv, 1.0, 0.0,
                1.0, 0.0, 1.0, -acv,
                1.0, 0.0, 1.0, -0.5
            ),
            doubleArrayOf(1.0, 0.0, 0.0, 0.5),
            doubleArrayOf(
                1.0, 0.0, 0.0, acv,
                1.0, -acv, 0.0, 0.0,
                1.0, -0.5, 0.0, 0.0
            ),
            doubleArrayOf(0.0, 0.5, 0.0, 0.0),
            doubleArrayOf(
                0.0, acv, 0.0, 0.0,
                0.0, 0.0, 0.0, acv,
                0.0, 0.0, 0.0, 0.5
            ),
            doubleArrayOf(),
        )
        private val types = intArrayOf(
            PathIterator.SEG_MOVETO,
            PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
            PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
            PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
            PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO,
            PathIterator.SEG_CLOSE,
        )
    }
}
