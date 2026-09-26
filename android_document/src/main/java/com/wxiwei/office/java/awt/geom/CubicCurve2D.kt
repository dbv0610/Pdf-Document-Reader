/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Shape
import java.io.Serializable
import java.util.Arrays

/**
 * The `CubicCurve2D` class defines a cubic parametric curve
 * segment in (x,y) coordinate space.
 */
abstract class CubicCurve2D protected constructor() : Shape, Cloneable {

    /**
     * A cubic parametric curve segment specified with
     * `float` coordinates.
     */
    open class Float : CubicCurve2D, Serializable {
        @JvmField
        var x1 = 0f

        @JvmField
        var y1 = 0f

        @JvmField
        var ctrlx1 = 0f

        @JvmField
        var ctrly1 = 0f

        @JvmField
        var ctrlx2 = 0f

        @JvmField
        var ctrly2 = 0f

        @JvmField
        var x2 = 0f

        @JvmField
        var y2 = 0f

        /**
         * Constructs and initializes a CubicCurve with coordinates
         * (0, 0, 0, 0, 0, 0, 0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a `CubicCurve2D` from
         * the specified `float` coordinates.
         */
        constructor(x1: kotlin.Float, y1: kotlin.Float, ctrlx1: kotlin.Float, ctrly1: kotlin.Float,
                    ctrlx2: kotlin.Float, ctrly2: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float) {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
        }

        override fun getX1(): kotlin.Double {
            return x1.toDouble()
        }

        override fun getY1(): kotlin.Double {
            return y1.toDouble()
        }

        override fun getP1(): Point2D {
            return Point2D.Float(x1, y1)
        }

        override fun getCtrlX1(): kotlin.Double {
            return ctrlx1.toDouble()
        }

        override fun getCtrlY1(): kotlin.Double {
            return ctrly1.toDouble()
        }

        override fun getCtrlP1(): Point2D {
            return Point2D.Float(ctrlx1, ctrly1)
        }

        override fun getCtrlX2(): kotlin.Double {
            return ctrlx2.toDouble()
        }

        override fun getCtrlY2(): kotlin.Double {
            return ctrly2.toDouble()
        }

        override fun getCtrlP2(): Point2D {
            return Point2D.Float(ctrlx2, ctrly2)
        }

        override fun getX2(): kotlin.Double {
            return x2.toDouble()
        }

        override fun getY2(): kotlin.Double {
            return y2.toDouble()
        }

        override fun getP2(): Point2D {
            return Point2D.Float(x2, y2)
        }

        override fun setCurve(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                              ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.ctrlx1 = ctrlx1.toFloat()
            this.ctrly1 = ctrly1.toFloat()
            this.ctrlx2 = ctrlx2.toFloat()
            this.ctrly2 = ctrly2.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        /**
         * Sets the location of the end points and control points
         * of this curve to the specified `float` coordinates.
         */
        open fun setCurve(x1: kotlin.Float, y1: kotlin.Float, ctrlx1: kotlin.Float, ctrly1: kotlin.Float,
                          ctrlx2: kotlin.Float, ctrly2: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx1 = ctrlx1
            this.ctrly1 = ctrly1
            this.ctrlx2 = ctrlx2
            this.ctrly2 = ctrly2
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val left = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2))
            val top = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2))
            val right = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2))
            val bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2))
            return Rectangle2D.Float(left, top, right - left, bottom - top)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -1272015596714244385L
        }
    }

    /**
     * A cubic parametric curve segment specified with
     * `double` coordinates.
     */
    open class Double : CubicCurve2D, Serializable {
        @JvmField
        var x1 = 0.0

        @JvmField
        var y1 = 0.0

        @JvmField
        var ctrlx1 = 0.0

        @JvmField
        var ctrly1 = 0.0

        @JvmField
        var ctrlx2 = 0.0

        @JvmField
        var ctrly2 = 0.0

        @JvmField
        var x2 = 0.0

        @JvmField
        var y2 = 0.0

        /**
         * Constructs and initializes a CubicCurve with coordinates
         * (0, 0, 0, 0, 0, 0, 0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a `CubicCurve2D` from
         * the specified `double` coordinates.
         */
        constructor(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                    ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
        }

        override fun getX1(): kotlin.Double {
            return x1
        }

        override fun getY1(): kotlin.Double {
            return y1
        }

        override fun getP1(): Point2D {
            return Point2D.Double(x1, y1)
        }

        override fun getCtrlX1(): kotlin.Double {
            return ctrlx1
        }

        override fun getCtrlY1(): kotlin.Double {
            return ctrly1
        }

        override fun getCtrlP1(): Point2D {
            return Point2D.Double(ctrlx1, ctrly1)
        }

        override fun getCtrlX2(): kotlin.Double {
            return ctrlx2
        }

        override fun getCtrlY2(): kotlin.Double {
            return ctrly2
        }

        override fun getCtrlP2(): Point2D {
            return Point2D.Double(ctrlx2, ctrly2)
        }

        override fun getX2(): kotlin.Double {
            return x2
        }

        override fun getY2(): kotlin.Double {
            return y2
        }

        override fun getP2(): Point2D {
            return Point2D.Double(x2, y2)
        }

        override fun setCurve(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                              ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx1 = ctrlx1
            this.ctrly1 = ctrly1
            this.ctrlx2 = ctrlx2
            this.ctrly2 = ctrly2
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val left = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2))
            val top = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2))
            val right = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2))
            val bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2))
            return Rectangle2D.Double(left, top, right - left, bottom - top)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -4202960122839707295L
        }
    }

    abstract fun getX1(): kotlin.Double

    abstract fun getY1(): kotlin.Double

    abstract fun getP1(): Point2D

    abstract fun getCtrlX1(): kotlin.Double

    abstract fun getCtrlY1(): kotlin.Double

    abstract fun getCtrlP1(): Point2D

    abstract fun getCtrlX2(): kotlin.Double

    abstract fun getCtrlY2(): kotlin.Double

    abstract fun getCtrlP2(): Point2D

    abstract fun getX2(): kotlin.Double

    abstract fun getY2(): kotlin.Double

    abstract fun getP2(): Point2D

    /**
     * Sets the location of the end points and control points of this curve
     * to the specified double coordinates.
     */
    abstract fun setCurve(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                          ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double)

    /**
     * Sets the location of the end points and control points of this curve
     * to the double coordinates at the specified offset in the specified
     * array.
     */
    open fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3],
            coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7])
    }

    /**
     * Sets the location of the end points and control points of this curve
     * to the specified `Point2D` coordinates.
     */
    open fun setCurve(p1: Point2D, cp1: Point2D, cp2: Point2D, p2: Point2D) {
        setCurve(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), p2.getX(),
            p2.getY())
    }

    /**
     * Sets the location of the end points and control points of this curve
     * to the coordinates of the `Point2D` objects at the specified
     * offset in the specified array.
     */
    open fun setCurve(pts: Array<Point2D>, offset: Int) {
        setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(), pts[offset + 1].getX(),
            pts[offset + 1].getY(), pts[offset + 2].getX(), pts[offset + 2].getY(),
            pts[offset + 3].getX(), pts[offset + 3].getY())
    }

    /**
     * Sets the location of the end points and control points of this curve
     * to the same as those in the specified `CubicCurve2D`.
     */
    open fun setCurve(c: CubicCurve2D) {
        setCurve(c.getX1(), c.getY1(), c.getCtrlX1(), c.getCtrlY1(), c.getCtrlX2(), c.getCtrlY2(),
            c.getX2(), c.getY2())
    }

    /**
     * Returns the square of the flatness of this curve.
     */
    open fun getFlatnessSq(): kotlin.Double {
        return CubicCurve2D.getFlatnessSq(getX1(), getY1(), getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(),
            getX2(), getY2())
    }

    /**
     * Returns the flatness of this curve.
     */
    open fun getFlatness(): kotlin.Double {
        return CubicCurve2D.getFlatness(getX1(), getY1(), getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(),
            getX2(), getY2())
    }

    /**
     * Subdivides this cubic curve and stores the resulting two
     * subdivided curves into the left and right curve parameters.
     */
    open fun subdivide(left: CubicCurve2D?, right: CubicCurve2D?) {
        CubicCurve2D.subdivide(this, left, right)
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        if (!(x * 0.0 + y * 0.0 == 0.0)) {
            /* Either x or y was infinite or NaN.
             * A NaN always produces a negative response to any test
             * and Infinity values cannot be "inside" any path so
             * they should return false as well.
             */
            return false
        }
        // We count the "Y" crossings to determine if the point is
        // inside the curve bounded by its closing line.
        val crossings = 0
        return ((crossings and 1) == 1)
    }

    override fun contains(p: Point2D): Boolean {
        return contains(p.getX(), p.getY())
    }

    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        // Trivially reject non-existant rectangles
        if (w <= 0 || h <= 0) {
            return false
        }

        // Trivially accept if either endpoint is inside the rectangle
        // (not on its border since it may end there and not go inside)
        // Record where they lie with respect to the rectangle.
        //     -1 => left, 0 => inside, 1 => right
        val x1 = getX1()
        val y1 = getY1()
        val x1tag = getTag(x1, x, x + w)
        val y1tag = getTag(y1, y, y + h)
        if (x1tag == INSIDE && y1tag == INSIDE) {
            return true
        }
        val x2 = getX2()
        val y2 = getY2()
        val x2tag = getTag(x2, x, x + w)
        val y2tag = getTag(y2, y, y + h)
        if (x2tag == INSIDE && y2tag == INSIDE) {
            return true
        }

        val ctrlx1 = getCtrlX1()
        val ctrly1 = getCtrlY1()
        val ctrlx2 = getCtrlX2()
        val ctrly2 = getCtrlY2()
        val ctrlx1tag = getTag(ctrlx1, x, x + w)
        val ctrly1tag = getTag(ctrly1, y, y + h)
        val ctrlx2tag = getTag(ctrlx2, x, x + w)
        val ctrly2tag = getTag(ctrly2, y, y + h)

        // Trivially reject if all points are entirely to one side of
        // the rectangle.
        if (x1tag < INSIDE && x2tag < INSIDE && ctrlx1tag < INSIDE && ctrlx2tag < INSIDE) {
            return false // All points left
        }
        if (y1tag < INSIDE && y2tag < INSIDE && ctrly1tag < INSIDE && ctrly2tag < INSIDE) {
            return false // All points above
        }
        if (x1tag > INSIDE && x2tag > INSIDE && ctrlx1tag > INSIDE && ctrlx2tag > INSIDE) {
            return false // All points right
        }
        if (y1tag > INSIDE && y2tag > INSIDE && ctrly1tag > INSIDE && ctrly2tag > INSIDE) {
            return false // All points below
        }

        // Test for endpoints on the edge where either the segment
        // or the curve is headed "inwards" from them
        // Note: These tests are a superset of the fast endpoint tests
        //       above and thus repeat those tests, but take more time
        //       and cover more cases
        if (inwards(x1tag, x2tag, ctrlx1tag) && inwards(y1tag, y2tag, ctrly1tag)) {
            // First endpoint on border with either edge moving inside
            return true
        }
        if (inwards(x2tag, x1tag, ctrlx2tag) && inwards(y2tag, y1tag, ctrly2tag)) {
            // Second endpoint on border with either edge moving inside
            return true
        }

        // Trivially accept if endpoints span directly across the rectangle
        val xoverlap = (x1tag * x2tag <= 0)
        val yoverlap = (y1tag * y2tag <= 0)
        if (x1tag == INSIDE && x2tag == INSIDE && yoverlap) {
            return true
        }
        if (y1tag == INSIDE && y2tag == INSIDE && xoverlap) {
            return true
        }

        // We now know that both endpoints are outside the rectangle
        // but the 4 points are not all on any one side of the rectangle.
        // Therefore the curve cannot be contained inside the rectangle,
        // but the rectangle might be contained inside the curve, or
        // the curve might intersect the boundary of the rectangle.

        val eqn = DoubleArray(4)
        val res = DoubleArray(4)
        if (!yoverlap) {
            // Both y coordinates for the closing segment are above or
            // below the rectangle which means that we can only intersect
            // if the curve crosses the top (or bottom) of the rectangle
            // in more than one place and if those crossing locations
            // span the horizontal range of the rectangle.
            fillEqn(eqn, (if (y1tag < INSIDE) y else y + h), y1, ctrly1, ctrly2, y2)
            var num = solveCubic(eqn, res)
            num = evalCubic(res, num, true, true, null, x1, ctrlx1, ctrlx2, x2)
            // odd counts imply the crossing was out of [0,1] bounds
            // otherwise there is no way for that part of the curve to
            // "return" to meet its endpoint
            return (num == 2 && getTag(res[0], x, x + w) * getTag(res[1], x, x + w) <= 0)
        }

        // Y ranges overlap.  Now we examine the X ranges
        if (!xoverlap) {
            // Both x coordinates for the closing segment are left of
            // or right of the rectangle which means that we can only
            // intersect if the curve crosses the left (or right) edge
            // of the rectangle in more than one place and if those
            // crossing locations span the vertical range of the rectangle.
            fillEqn(eqn, (if (x1tag < INSIDE) x else x + w), x1, ctrlx1, ctrlx2, x2)
            var num = solveCubic(eqn, res)
            num = evalCubic(res, num, true, true, null, y1, ctrly1, ctrly2, y2)
            // odd counts imply the crossing was out of [0,1] bounds
            // otherwise there is no way for that part of the curve to
            // "return" to meet its endpoint
            return (num == 2 && getTag(res[0], y, y + h) * getTag(res[1], y, y + h) <= 0)
        }

        // The X and Y ranges of the endpoints overlap the X and Y
        // ranges of the rectangle, now find out how the endpoint
        // line segment intersects the Y range of the rectangle
        val dx = x2 - x1
        val dy = y2 - y1
        val k = y2 * x1 - x2 * y1
        var c1tag: Int
        val c2tag: Int
        if (y1tag == INSIDE) {
            c1tag = x1tag
        } else {
            c1tag = getTag((k + dx * (if (y1tag < INSIDE) y else y + h)) / dy, x, x + w)
        }
        if (y2tag == INSIDE) {
            c2tag = x2tag
        } else {
            c2tag = getTag((k + dx * (if (y2tag < INSIDE) y else y + h)) / dy, x, x + w)
        }
        // If the part of the line segment that intersects the Y range
        // of the rectangle crosses it horizontally - trivially accept
        if (c1tag * c2tag <= 0) {
            return true
        }

        // Now we know that both the X and Y ranges intersect and that
        // the endpoint line segment does not directly cross the rectangle.
        //
        // We can almost treat this case like one of the cases above
        // where both endpoints are to one side, except that we may
        // get one or three intersections of the curve with the vertical
        // side of the rectangle.  This is because the endpoint segment
        // accounts for the other intersection in an even pairing.  Thus,
        // with the endpoint crossing we end up with 2 or 4 total crossings.
        //
        // (Remember there is overlap in both the X and Y ranges which
        //  means that the segment itself must cross at least one vertical
        //  edge of the rectangle - in particular, the "near vertical side"
        //  - leaving an odd number of intersections for the curve.)
        //
        // Now we calculate the y tags of all the intersections on the
        // "near vertical side" of the rectangle.  We will have one with
        // the endpoint segment, and one or three with the curve.  If
        // any pair of those vertical intersections overlap the Y range
        // of the rectangle, we have an intersection.  Otherwise, we don't.

        // c1tag = vertical intersection class of the endpoint segment
        //
        // Choose the y tag of the endpoint that was not on the same
        // side of the rectangle as the subsegment calculated above.
        // Note that we can "steal" the existing Y tag of that endpoint
        // since it will be provably the same as the vertical intersection.
        c1tag = if (c1tag * x1tag <= 0) y1tag else y2tag

        // Now we have to calculate an array of solutions of the curve
        // with the "near vertical side" of the rectangle.  Then we
        // need to sort the tags and do a pairwise range test to see
        // if either of the pairs of crossings spans the Y range of
        // the rectangle.
        //
        // Note that the c2tag can still tell us which vertical edge
        // to test against.
        fillEqn(eqn, (if (c2tag < INSIDE) x else x + w), x1, ctrlx1, ctrlx2, x2)
        var num = solveCubic(eqn, res)
        num = evalCubic(res, num, true, true, null, y1, ctrly1, ctrly2, y2)

        // Now put all of the tags into a bucket and sort them.  There
        // is an intersection iff one of the pairs of tags "spans" the
        // Y range of the rectangle.
        val tags = IntArray(num + 1)
        for (i in 0 until num) {
            tags[i] = getTag(res[i], y, y + h)
        }
        tags[num] = c1tag
        Arrays.sort(tags)
        return ((num >= 1 && tags[0] * tags[1] <= 0) || (num >= 3 && tags[2] * tags[3] <= 0))
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (w <= 0 || h <= 0) {
            return false
        }
        // Assertion: Cubic curves closed by connecting their
        // endpoints form either one or two convex halves with
        // the closing line segment as an edge of both sides.
        if (!(contains(x, y) && contains(x + w, y) && contains(x + w, y + h) && contains(x, y + h))) {
            return false
        }
        // Either the rectangle is entirely inside one of the convex
        // halves or it crosses from one to the other, in which case
        // it must intersect the closing line segment.
        val rect: Rectangle2D = Rectangle2D.Double(x, y, w, h)
        return !rect.intersectsLine(getX1(), getY1(), getX2(), getY2())
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun getBounds(): Rectangle {
        return getBounds2D().getBounds()
    }

    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return CubicIterator(this, at)
    }

    override fun getPathIterator(at: AffineTransform?, flatness: kotlin.Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    /**
     * Creates a new object of the same class as this object.
     */
    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }

    companion object {
        /**
         * Returns the square of the flatness of the cubic curve specified
         * by the indicated control points.
         */
        @JvmStatic
        fun getFlatnessSq(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                          ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            return Math.max(Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1),
                Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2))
        }

        /**
         * Returns the flatness of the cubic curve specified
         * by the indicated control points.
         */
        @JvmStatic
        fun getFlatness(x1: kotlin.Double, y1: kotlin.Double, ctrlx1: kotlin.Double, ctrly1: kotlin.Double,
                        ctrlx2: kotlin.Double, ctrly2: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            return Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2))
        }

        /**
         * Returns the square of the flatness of the cubic curve specified
         * by the control points stored in the indicated array at the
         * indicated index.
         */
        @JvmStatic
        fun getFlatnessSq(coords: DoubleArray, offset: Int): kotlin.Double {
            return getFlatnessSq(coords[offset + 0], coords[offset + 1], coords[offset + 2],
                coords[offset + 3], coords[offset + 4], coords[offset + 5], coords[offset + 6],
                coords[offset + 7])
        }

        /**
         * Returns the flatness of the cubic curve specified
         * by the control points stored in the indicated array at the
         * indicated index.
         */
        @JvmStatic
        fun getFlatness(coords: DoubleArray, offset: Int): kotlin.Double {
            return getFlatness(coords[offset + 0], coords[offset + 1], coords[offset + 2],
                coords[offset + 3], coords[offset + 4], coords[offset + 5], coords[offset + 6],
                coords[offset + 7])
        }

        /**
         * Subdivides the cubic curve specified by the `src` parameter
         * and stores the resulting two subdivided curves into the
         * `left` and `right` curve parameters.
         */
        @JvmStatic
        fun subdivide(src: CubicCurve2D, left: CubicCurve2D?, right: CubicCurve2D?) {
            val x1 = src.getX1()
            val y1 = src.getY1()
            var ctrlx1 = src.getCtrlX1()
            var ctrly1 = src.getCtrlY1()
            var ctrlx2 = src.getCtrlX2()
            var ctrly2 = src.getCtrlY2()
            val x2 = src.getX2()
            val y2 = src.getY2()
            var centerx = (ctrlx1 + ctrlx2) / 2.0
            var centery = (ctrly1 + ctrly2) / 2.0
            ctrlx1 = (x1 + ctrlx1) / 2.0
            ctrly1 = (y1 + ctrly1) / 2.0
            ctrlx2 = (x2 + ctrlx2) / 2.0
            ctrly2 = (y2 + ctrly2) / 2.0
            val ctrlx12 = (ctrlx1 + centerx) / 2.0
            val ctrly12 = (ctrly1 + centery) / 2.0
            val ctrlx21 = (ctrlx2 + centerx) / 2.0
            val ctrly21 = (ctrly2 + centery) / 2.0
            centerx = (ctrlx12 + ctrlx21) / 2.0
            centery = (ctrly12 + ctrly21) / 2.0
            left?.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx12, ctrly12, centerx, centery)
            right?.setCurve(centerx, centery, ctrlx21, ctrly21, ctrlx2, ctrly2, x2, y2)
        }

        /**
         * Subdivides the cubic curve specified by the coordinates
         * stored in the `src` array at indices `srcoff`
         * through (`srcoff`&nbsp;+&nbsp;7) and stores the
         * resulting two subdivided curves into the two result arrays at the
         * corresponding indices.
         */
        @JvmStatic
        fun subdivide(src: DoubleArray, srcoff: Int, left: DoubleArray?, leftoff: Int,
                      right: DoubleArray?, rightoff: Int) {
            var x1 = src[srcoff + 0]
            var y1 = src[srcoff + 1]
            var ctrlx1 = src[srcoff + 2]
            var ctrly1 = src[srcoff + 3]
            var ctrlx2 = src[srcoff + 4]
            var ctrly2 = src[srcoff + 5]
            var x2 = src[srcoff + 6]
            var y2 = src[srcoff + 7]
            if (left != null) {
                left[leftoff + 0] = x1
                left[leftoff + 1] = y1
            }
            if (right != null) {
                right[rightoff + 6] = x2
                right[rightoff + 7] = y2
            }
            x1 = (x1 + ctrlx1) / 2.0
            y1 = (y1 + ctrly1) / 2.0
            x2 = (x2 + ctrlx2) / 2.0
            y2 = (y2 + ctrly2) / 2.0
            var centerx = (ctrlx1 + ctrlx2) / 2.0
            var centery = (ctrly1 + ctrly2) / 2.0
            ctrlx1 = (x1 + centerx) / 2.0
            ctrly1 = (y1 + centery) / 2.0
            ctrlx2 = (x2 + centerx) / 2.0
            ctrly2 = (y2 + centery) / 2.0
            centerx = (ctrlx1 + ctrlx2) / 2.0
            centery = (ctrly1 + ctrly2) / 2.0
            if (left != null) {
                left[leftoff + 2] = x1
                left[leftoff + 3] = y1
                left[leftoff + 4] = ctrlx1
                left[leftoff + 5] = ctrly1
                left[leftoff + 6] = centerx
                left[leftoff + 7] = centery
            }
            if (right != null) {
                right[rightoff + 0] = centerx
                right[rightoff + 1] = centery
                right[rightoff + 2] = ctrlx2
                right[rightoff + 3] = ctrly2
                right[rightoff + 4] = x2
                right[rightoff + 5] = y2
            }
        }

        /**
         * Solves the cubic whose coefficients are in the `eqn`
         * array and places the non-complex roots back into the same array,
         * returning the number of roots.
         */
        @JvmStatic
        fun solveCubic(eqn: DoubleArray): Int {
            return solveCubic(eqn, eqn)
        }

        /**
         * Solve the cubic whose coefficients are in the `eqn`
         * array and place the non-complex roots into the `res`
         * array, returning the number of roots.
         */
        @JvmStatic
        fun solveCubic(eqn: DoubleArray, res: DoubleArray): Int {
            var eqn = eqn
            // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
            val d = eqn[3]
            if (d == 0.0) {
                // The cubic has degenerated to quadratic (or line or ...).
                return QuadCurve2D.solveQuadratic(eqn, res)
            }
            var a = eqn[2] / d
            val b = eqn[1] / d
            val c = eqn[0] / d
            var roots = 0
            var Q = (a * a - 3.0 * b) / 9.0
            var R = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0
            val R2 = R * R
            val Q3 = Q * Q * Q
            a = a / 3.0
            if (R2 < Q3) {
                val theta = Math.acos(R / Math.sqrt(Q3))
                Q = -2.0 * Math.sqrt(Q)
                if (res === eqn) {
                    // Copy the eqn so that we don't clobber it with the
                    // roots.  This is needed so that fixRoots can do its
                    // work with the original equation.
                    eqn = DoubleArray(4)
                    System.arraycopy(res, 0, eqn, 0, 4)
                }
                res[roots++] = Q * Math.cos(theta / 3.0) - a
                res[roots++] = Q * Math.cos((theta + Math.PI * 2.0) / 3.0) - a
                res[roots++] = Q * Math.cos((theta - Math.PI * 2.0) / 3.0) - a
                fixRoots(res, eqn)
            } else {
                val neg = (R < 0.0)
                val S = Math.sqrt(R2 - Q3)
                if (neg) {
                    R = -R
                }
                var A = Math.pow(R + S, 1.0 / 3.0)
                if (!neg) {
                    A = -A
                }
                val B = if (A == 0.0) 0.0 else (Q / A)
                res[roots++] = (A + B) - a
            }
            return roots
        }

        /*
         * This pruning step is necessary since solveCubic uses the
         * cosine function to calculate the roots when there are 3
         * of them.  Since the cosine method can have an error of
         * +/- 1E-14 we need to make sure that we don't make any
         * bad decisions due to an error.
         *
         * If the root is not near one of the endpoints, then we will
         * only have a slight inaccuracy in calculating the x intercept
         * which will only cause a slightly wrong answer for some
         * points very close to the curve.  While the results in that
         * case are not as accurate as they could be, they are not
         * disastrously inaccurate either.
         *
         * On the other hand, if the error happens near one end of
         * the curve, then our processing to reject values outside
         * of the t=[0,1] range will fail and the results of that
         * failure will be disastrous since for an entire horizontal
         * range of test points, we will either overcount or undercount
         * the crossings and get a wrong answer for all of them, even
         * when they are clearly and obviously inside or outside the
         * curve.
         *
         * To work around this problem, we try a couple of Newton-Raphson
         * iterations to see if the true root is closer to the endpoint
         * or further away.  If it is further away, then we can stop
         * since we know we are on the right side of the endpoint.  If
         * we change direction, then either we are now being dragged away
         * from the endpoint in which case the first condition will cause
         * us to stop, or we have passed the endpoint and are headed back.
         * In the second case, we simply evaluate the slope at the
         * endpoint itself and place ourselves on the appropriate side
         * of it or on it depending on that result.
         */
        private fun fixRoots(res: DoubleArray, eqn: DoubleArray) {
            val EPSILON = 1E-5
            for (i in 0..2) {
                val t = res[i]
                if (Math.abs(t) < EPSILON) {
                    res[i] = findZero(t, 0.0, eqn)
                } else if (Math.abs(t - 1) < EPSILON) {
                    res[i] = findZero(t, 1.0, eqn)
                }
            }
        }

        private fun solveEqn(eqn: DoubleArray, order: Int, t: kotlin.Double): kotlin.Double {
            var order = order
            var v = eqn[order]
            while (--order >= 0) {
                v = v * t + eqn[order]
            }
            return v
        }

        private fun findZero(t: kotlin.Double, target: kotlin.Double, eqn: DoubleArray): kotlin.Double {
            var t = t
            val slopeqn = doubleArrayOf(eqn[1], 2 * eqn[2], 3 * eqn[3])
            var slope: kotlin.Double
            var origdelta = 0.0
            val origt = t
            while (true) {
                slope = solveEqn(slopeqn, 2, t)
                if (slope == 0.0) {
                    // At a local minima - must return
                    return t
                }
                val y = solveEqn(eqn, 3, t)
                if (y == 0.0) {
                    // Found it! - return it
                    return t
                }
                // assert(slope != 0 && y != 0);
                val delta = -(y / slope)
                // assert(delta != 0);
                if (origdelta == 0.0) {
                    origdelta = delta
                }
                if (t < target) {
                    if (delta < 0) return t
                } else if (t > target) {
                    if (delta > 0) return t
                } else { /* t == target */
                    return if (delta > 0) (target + java.lang.Double.MIN_VALUE)
                    else (target - java.lang.Double.MIN_VALUE)
                }
                val newt = t + delta
                if (t == newt) {
                    // The deltas are so small that we aren't moving...
                    return t
                }
                if (delta * origdelta < 0) {
                    // We have reversed our path.
                    val tag = if (origt < t) getTag(target, origt, t) else getTag(target, t, origt)
                    if (tag != INSIDE) {
                        // Local minima found away from target - return the middle
                        return (origt + t) / 2
                    }
                    // Local minima somewhere near target - move to target
                    // and let the slope determine the resulting t.
                    t = target
                } else {
                    t = newt
                }
            }
        }

        /*
         * Fill an array with the coefficients of the parametric equation
         * in t, ready for solving against val with solveCubic.
         * We currently have:
         * <pre>
         *   val = P(t) = C1(1-t)^3 + 3CP1 t(1-t)^2 + 3CP2 t^2(1-t) + C2 t^3
         *              = C1 - 3C1t + 3C1t^2 - C1t^3 +
         *                3CP1t - 6CP1t^2 + 3CP1t^3 +
         *                3CP2t^2 - 3CP2t^3 +
         *                C2t^3
         *            0 = (C1 - val) +
         *                (3CP1 - 3C1) t +
         *                (3C1 - 6CP1 + 3CP2) t^2 +
         *                (C2 - 3CP2 + 3CP1 - C1) t^3
         *            0 = C + Bt + At^2 + Dt^3
         *     C = C1 - val
         *     B = 3*CP1 - 3*C1
         *     A = 3*CP2 - 6*CP1 + 3*C1
         *     D = C2 - 3*CP2 + 3*CP1 - C1
         * </pre>
         */
        private fun fillEqn(eqn: DoubleArray, `val`: kotlin.Double, c1: kotlin.Double, cp1: kotlin.Double, cp2: kotlin.Double,
                            c2: kotlin.Double) {
            eqn[0] = c1 - `val`
            eqn[1] = (cp1 - c1) * 3.0
            eqn[2] = (cp2 - cp1 - cp1 + c1) * 3.0
            eqn[3] = c2 + (cp1 - cp2) * 3.0 - c1
            return
        }

        /*
         * Evaluate the t values in the first num slots of the vals[] array
         * and place the evaluated values back into the same array.  Only
         * evaluate t values that are within the range <0, 1>, including
         * the 0 and 1 ends of the range iff the include0 or include1
         * booleans are true.  If an "inflection" equation is handed in,
         * then any points which represent a point of inflection for that
         * cubic equation are also ignored.
         */
        private fun evalCubic(vals: DoubleArray, num: Int, include0: Boolean, include1: Boolean,
                              inflect: DoubleArray?, c1: kotlin.Double, cp1: kotlin.Double, cp2: kotlin.Double, c2: kotlin.Double): Int {
            var j = 0
            for (i in 0 until num) {
                val t = vals[i]
                if ((if (include0) t >= 0 else t > 0) && (if (include1) t <= 1 else t < 1)
                    && (inflect == null || inflect[1] + (2 * inflect[2] + 3 * inflect[3] * t) * t != 0.0)) {
                    val u = 1 - t
                    vals[j++] = (c1 * u * u * u + 3 * cp1 * t * u * u + 3 * cp2 * t * t * u + c2 * t * t
                            * t)
                }
            }
            return j
        }

        private const val BELOW = -2
        private const val LOWEDGE = -1
        private const val INSIDE = 0
        private const val HIGHEDGE = 1
        private const val ABOVE = 2

        /*
         * Determine where coord lies with respect to the range from
         * low to high.  It is assumed that low <= high.  The return
         * value is one of the 5 values BELOW, LOWEDGE, INSIDE, HIGHEDGE,
         * or ABOVE.
         */
        private fun getTag(coord: kotlin.Double, low: kotlin.Double, high: kotlin.Double): Int {
            if (coord <= low) {
                return if (coord < low) BELOW else LOWEDGE
            }
            if (coord >= high) {
                return if (coord > high) ABOVE else HIGHEDGE
            }
            return INSIDE
        }

        /*
         * Determine if the pttag represents a coordinate that is already
         * in its test range, or is on the border with either of the two
         * opttags representing another coordinate that is "towards the
         * inside" of that test range.  In other words, are either of the
         * two "opt" points "drawing the pt inward"?
         */
        private fun inwards(pttag: Int, opt1tag: Int, opt2tag: Int): Boolean {
            return when (pttag) {
                BELOW, ABOVE -> false
                LOWEDGE -> (opt1tag >= INSIDE || opt2tag >= INSIDE)
                INSIDE -> true
                HIGHEDGE -> (opt1tag <= INSIDE || opt2tag <= INSIDE)
                else -> false
            }
        }
    }
}
