/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

import java.util.NoSuchElementException

/**
 * The `FlatteningPathIterator` class returns a flattened view of
 * another [PathIterator] object.  Other Shape
 * classes can use this class to provide flattening behavior for their paths
 * without having to perform the interpolation calculations themselves.
 */
class FlatteningPathIterator(src: PathIterator, flatness: Double, limit: Int) : PathIterator {

    companion object {
        internal const val GROW_SIZE = 24 // Multiple of cubic & quad curve size
    }

    internal var src: PathIterator // The source iterator

    internal var squareflat: Double // Square of the flatness parameter
    // for testing against squared lengths

    internal var limit: Int // Maximum number of recursion levels

    internal var hold = DoubleArray(14) // The cache of interpolated coords
    // Note that this must be long enough
    // to store a full cubic segment and
    // a relative cubic segment to avoid
    // aliasing when copying the coords
    // of a curve to the end of the array.
    // This is also serendipitously equal
    // to the size of a full quad segment
    // and 2 relative quad segments.

    internal var curx = 0.0 // The ending x,y of the last segment
    internal var cury = 0.0

    internal var movx = 0.0 // The x,y of the last move segment
    internal var movy = 0.0

    internal var holdType = 0 // The type of the curve being held
    // for interpolation

    internal var holdEnd = 0 // The index of the last curve segment
    // being held for interpolation

    internal var holdIndex = 0 // The index of the curve segment
    // that was last interpolated.  This
    // is the curve segment ready to be
    // returned in the next call to
    // currentSegment().

    internal var levels: IntArray // The recursion level at which
    // each curve being held in storage
    // was generated.

    internal var levelIndex = 0 // The index of the entry in the
    // levels array of the curve segment
    // at the holdIndex

    internal var done = false // True when iteration is done

    /**
     * Constructs a new `FlatteningPathIterator` object that
     * flattens a path as it iterates over it.  The iterator does not
     * subdivide any curve read from the source iterator to more than
     * 10 levels of subdivision which yields a maximum of 1024 line
     * segments per curve.
     * @param src the original unflattened path being iterated over
     * @param flatness the maximum allowable distance between the
     * control points and the flattened curve
     */
    constructor(src: PathIterator, flatness: Double) : this(src, flatness, 10)

    /**
     * Constructs a new `FlatteningPathIterator` object
     * that flattens a path as it iterates over it.
     * The `limit` parameter allows you to control the
     * maximum number of recursive subdivisions that the iterator
     * can make before it assumes that the curve is flat enough
     * without measuring against the `flatness` parameter.
     */
    init {
        if (flatness < 0.0) {
            throw IllegalArgumentException("flatness must be >= 0")
        }
        if (limit < 0) {
            throw IllegalArgumentException("limit must be >= 0")
        }
        this.src = src
        this.squareflat = flatness * flatness
        this.limit = limit
        this.levels = IntArray(limit + 1)
        // prime the first path segment
        next(false)
    }

    /**
     * Returns the flatness of this iterator.
     */
    fun getFlatness(): Double {
        return Math.sqrt(squareflat)
    }

    /**
     * Returns the recursion limit of this iterator.
     */
    fun getRecursionLimit(): Int {
        return limit
    }

    /**
     * Returns the winding rule for determining the interior of the
     * path.
     */
    override fun getWindingRule(): Int {
        return src.getWindingRule()
    }

    /**
     * Tests if the iteration is complete.
     */
    override fun isDone(): Boolean {
        return done
    }

    /*
     * Ensures that the hold array can hold up to (want) more values.
     * It is currently holding (hold.length - holdIndex) values.
     */
    internal fun ensureHoldCapacity(want: Int) {
        if (holdIndex - want < 0) {
            val have = hold.size - holdIndex
            val newsize = hold.size + GROW_SIZE
            val newhold = DoubleArray(newsize)
            System.arraycopy(hold, holdIndex, newhold, holdIndex + GROW_SIZE, have)
            hold = newhold
            holdIndex += GROW_SIZE
            holdEnd += GROW_SIZE
        }
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    override fun next() {
        next(true)
    }

    private fun next(doNext: Boolean) {
        var level: Int

        if (holdIndex >= holdEnd) {
            if (doNext) {
                src.next()
            }
            if (src.isDone()) {
                done = true
                return
            }
            holdType = src.currentSegment(hold)
            levelIndex = 0
            levels[0] = 0
        }

        when (holdType) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                curx = hold[0]
                cury = hold[1]
                if (holdType == PathIterator.SEG_MOVETO) {
                    movx = curx
                    movy = cury
                }
                holdIndex = 0
                holdEnd = 0
            }
            PathIterator.SEG_CLOSE -> {
                curx = movx
                cury = movy
                holdIndex = 0
                holdEnd = 0
            }
            PathIterator.SEG_QUADTO -> {
                if (holdIndex >= holdEnd) {
                    // Move the coordinates to the end of the array.
                    holdIndex = hold.size - 6
                    holdEnd = hold.size - 2
                    hold[holdIndex + 0] = curx
                    hold[holdIndex + 1] = cury
                    hold[holdIndex + 2] = hold[0]
                    hold[holdIndex + 3] = hold[1]
                    curx = hold[2]
                    hold[holdIndex + 4] = curx
                    cury = hold[3]
                    hold[holdIndex + 5] = cury
                }

                level = levels[levelIndex]
                while (level < limit) {
                    if (QuadCurve2D.getFlatnessSq(hold, holdIndex) < squareflat) {
                        break
                    }

                    ensureHoldCapacity(4)
                    QuadCurve2D.subdivide(hold, holdIndex, hold, holdIndex - 4, hold, holdIndex)
                    holdIndex -= 4

                    // Now that we have subdivided, we have constructed
                    // two curves of one depth lower than the original
                    // curve.  One of those curves is in the place of
                    // the former curve and one of them is in the next
                    // set of held coordinate slots.  We now set both
                    // curves level values to the next higher level.
                    level++
                    levels[levelIndex] = level
                    levelIndex++
                    levels[levelIndex] = level
                }

                // This curve segment is flat enough, or it is too deep
                // in recursion levels to try to flatten any more.  The
                // two coordinates at holdIndex+4 and holdIndex+5 now
                // contain the endpoint of the curve which can be the
                // endpoint of an approximating line segment.
                holdIndex += 4
                levelIndex--
            }
            PathIterator.SEG_CUBICTO -> {
                if (holdIndex >= holdEnd) {
                    // Move the coordinates to the end of the array.
                    holdIndex = hold.size - 8
                    holdEnd = hold.size - 2
                    hold[holdIndex + 0] = curx
                    hold[holdIndex + 1] = cury
                    hold[holdIndex + 2] = hold[0]
                    hold[holdIndex + 3] = hold[1]
                    hold[holdIndex + 4] = hold[2]
                    hold[holdIndex + 5] = hold[3]
                    curx = hold[4]
                    hold[holdIndex + 6] = curx
                    cury = hold[5]
                    hold[holdIndex + 7] = cury
                }

                level = levels[levelIndex]
                while (level < limit) {
                    if (CubicCurve2D.getFlatnessSq(hold, holdIndex) < squareflat) {
                        break
                    }

                    ensureHoldCapacity(6)
                    CubicCurve2D.subdivide(hold, holdIndex, hold, holdIndex - 6, hold, holdIndex)
                    holdIndex -= 6

                    // Now that we have subdivided, we have constructed
                    // two curves of one depth lower than the original
                    // curve.  One of those curves is in the place of
                    // the former curve and one of them is in the next
                    // set of held coordinate slots.  We now set both
                    // curves level values to the next higher level.
                    level++
                    levels[levelIndex] = level
                    levelIndex++
                    levels[levelIndex] = level
                }

                // This curve segment is flat enough, or it is too deep
                // in recursion levels to try to flatten any more.  The
                // two coordinates at holdIndex+6 and holdIndex+7 now
                // contain the endpoint of the curve which can be the
                // endpoint of an approximating line segment.
                holdIndex += 6
                levelIndex--
            }
        }
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
     */
    override fun currentSegment(coords: FloatArray): Int {
        if (isDone()) {
            throw NoSuchElementException("flattening iterator out of bounds")
        }
        var type = holdType
        if (type != PathIterator.SEG_CLOSE) {
            coords[0] = hold[holdIndex + 0].toFloat()
            coords[1] = hold[holdIndex + 1].toFloat()
            if (type != PathIterator.SEG_MOVETO) {
                type = PathIterator.SEG_LINETO
            }
        }
        return type
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
     */
    override fun currentSegment(coords: DoubleArray): Int {
        if (isDone()) {
            throw NoSuchElementException("flattening iterator out of bounds")
        }
        var type = holdType
        if (type != PathIterator.SEG_CLOSE) {
            coords[0] = hold[holdIndex + 0]
            coords[1] = hold[holdIndex + 1]
            if (type != PathIterator.SEG_MOVETO) {
                type = PathIterator.SEG_LINETO
            }
        }
        return type
    }
}
