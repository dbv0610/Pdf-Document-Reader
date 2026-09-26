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

/**
 * The `QuadCurve2D` class defines a quadratic parametric curve
 * segment in (x,y) coordinate space.
 */
abstract class QuadCurve2D protected constructor() : Shape, Cloneable {

    /**
     * A quadratic parametric curve segment specified with
     * `float` coordinates.
     */
    open class Float : QuadCurve2D, Serializable {
        @JvmField
        var x1 = 0f

        @JvmField
        var y1 = 0f

        @JvmField
        var ctrlx = 0f

        @JvmField
        var ctrly = 0f

        @JvmField
        var x2 = 0f

        @JvmField
        var y2 = 0f

        /**
         * Constructs and initializes a `QuadCurve2D` with
         * coordinates (0, 0, 0, 0, 0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a `QuadCurve2D` from the
         * specified `float` coordinates.
         */
        constructor(x1: kotlin.Float, y1: kotlin.Float,
                    ctrlx: kotlin.Float, ctrly: kotlin.Float,
                    x2: kotlin.Float, y2: kotlin.Float) {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2)
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

        override fun getCtrlX(): kotlin.Double {
            return ctrlx.toDouble()
        }

        override fun getCtrlY(): kotlin.Double {
            return ctrly.toDouble()
        }

        override fun getCtrlPt(): Point2D {
            return Point2D.Float(ctrlx, ctrly)
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

        override fun setCurve(x1: kotlin.Double, y1: kotlin.Double,
                              ctrlx: kotlin.Double, ctrly: kotlin.Double,
                              x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.ctrlx = ctrlx.toFloat()
            this.ctrly = ctrly.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        /**
         * Sets the location of the end points and control point of this curve
         * to the specified `float` coordinates.
         */
        open fun setCurve(x1: kotlin.Float, y1: kotlin.Float,
                          ctrlx: kotlin.Float, ctrly: kotlin.Float,
                          x2: kotlin.Float, y2: kotlin.Float) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx = ctrlx
            this.ctrly = ctrly
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val left = Math.min(Math.min(x1, x2), ctrlx)
            val top = Math.min(Math.min(y1, y2), ctrly)
            val right = Math.max(Math.max(x1, x2), ctrlx)
            val bottom = Math.max(Math.max(y1, y2), ctrly)
            return Rectangle2D.Float(left, top,
                right - left, bottom - top)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -8511188402130719609L
        }
    }

    /**
     * A quadratic parametric curve segment specified with
     * `double` coordinates.
     */
    open class Double : QuadCurve2D, Serializable {
        @JvmField
        var x1 = 0.0

        @JvmField
        var y1 = 0.0

        @JvmField
        var ctrlx = 0.0

        @JvmField
        var ctrly = 0.0

        @JvmField
        var x2 = 0.0

        @JvmField
        var y2 = 0.0

        /**
         * Constructs and initializes a `QuadCurve2D` with
         * coordinates (0, 0, 0, 0, 0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a `QuadCurve2D` from the
         * specified `double` coordinates.
         */
        constructor(x1: kotlin.Double, y1: kotlin.Double,
                    ctrlx: kotlin.Double, ctrly: kotlin.Double,
                    x2: kotlin.Double, y2: kotlin.Double) {
            setCurve(x1, y1, ctrlx, ctrly, x2, y2)
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

        override fun getCtrlX(): kotlin.Double {
            return ctrlx
        }

        override fun getCtrlY(): kotlin.Double {
            return ctrly
        }

        override fun getCtrlPt(): Point2D {
            return Point2D.Double(ctrlx, ctrly)
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

        override fun setCurve(x1: kotlin.Double, y1: kotlin.Double,
                              ctrlx: kotlin.Double, ctrly: kotlin.Double,
                              x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1
            this.y1 = y1
            this.ctrlx = ctrlx
            this.ctrly = ctrly
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val left = Math.min(Math.min(x1, x2), ctrlx)
            val top = Math.min(Math.min(y1, y2), ctrly)
            val right = Math.max(Math.max(x1, x2), ctrlx)
            val bottom = Math.max(Math.max(y1, y2), ctrly)
            return Rectangle2D.Double(left, top,
                right - left, bottom - top)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 4217149928428559721L
        }
    }

    abstract fun getX1(): kotlin.Double

    abstract fun getY1(): kotlin.Double

    abstract fun getP1(): Point2D

    abstract fun getCtrlX(): kotlin.Double

    abstract fun getCtrlY(): kotlin.Double

    abstract fun getCtrlPt(): Point2D

    abstract fun getX2(): kotlin.Double

    abstract fun getY2(): kotlin.Double

    abstract fun getP2(): Point2D

    /**
     * Sets the location of the end points and control point of this curve
     * to the specified `double` coordinates.
     */
    abstract fun setCurve(x1: kotlin.Double, y1: kotlin.Double,
                          ctrlx: kotlin.Double, ctrly: kotlin.Double,
                          x2: kotlin.Double, y2: kotlin.Double)

    /**
     * Sets the location of the end points and control points of this
     * `QuadCurve2D` to the `double` coordinates at
     * the specified offset in the specified array.
     */
    open fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(coords[offset + 0], coords[offset + 1],
            coords[offset + 2], coords[offset + 3],
            coords[offset + 4], coords[offset + 5])
    }

    /**
     * Sets the location of the end points and control point of this
     * `QuadCurve2D` to the specified `Point2D`
     * coordinates.
     */
    open fun setCurve(p1: Point2D, cp: Point2D, p2: Point2D) {
        setCurve(p1.getX(), p1.getY(),
            cp.getX(), cp.getY(),
            p2.getX(), p2.getY())
    }

    /**
     * Sets the location of the end points and control points of this
     * `QuadCurve2D` to the coordinates of the
     * `Point2D` objects at the specified offset in
     * the specified array.
     */
    open fun setCurve(pts: Array<Point2D>, offset: Int) {
        setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(),
            pts[offset + 1].getX(), pts[offset + 1].getY(),
            pts[offset + 2].getX(), pts[offset + 2].getY())
    }

    /**
     * Sets the location of the end points and control point of this
     * `QuadCurve2D` to the same as those in the specified
     * `QuadCurve2D`.
     */
    open fun setCurve(c: QuadCurve2D) {
        setCurve(c.getX1(), c.getY1(),
            c.getCtrlX(), c.getCtrlY(),
            c.getX2(), c.getY2())
    }

    /**
     * Returns the square of the flatness, or maximum distance of a
     * control point from the line connecting the end points, of this
     * `QuadCurve2D`.
     */
    open fun getFlatnessSq(): kotlin.Double {
        return Line2D.ptSegDistSq(getX1(), getY1(),
            getX2(), getY2(),
            getCtrlX(), getCtrlY())
    }

    /**
     * Returns the flatness, or maximum distance of a
     * control point from the line connecting the end points, of this
     * `QuadCurve2D`.
     */
    open fun getFlatness(): kotlin.Double {
        return Line2D.ptSegDist(getX1(), getY1(),
            getX2(), getY2(),
            getCtrlX(), getCtrlY())
    }

    /**
     * Subdivides this `QuadCurve2D` and stores the resulting
     * two subdivided curves into the `left` and
     * `right` curve parameters.
     */
    open fun subdivide(left: QuadCurve2D?, right: QuadCurve2D?) {
        QuadCurve2D.subdivide(this, left, right)
    }

    /**
     * Tests if a specified coordinate is inside the boundary of the shape of
     * this `QuadCurve2D`.
     */
    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        val x1 = getX1()
        val y1 = getY1()
        val xc = getCtrlX()
        val yc = getCtrlY()
        val x2 = getX2()
        val y2 = getY2()

        /*
         * We have a convex shape bounded by quad curve Pc(t)
         * and ine Pl(t).
         *
         *     P1 = (x1, y1) - start point of curve
         *     P2 = (x2, y2) - end point of curve
         *     Pc = (xc, yc) - control point
         *
         *     Pq(t) = P1*(1 - t)^2 + 2*Pc*t*(1 - t) + P2*t^2 =
         *           = (P1 - 2*Pc + P2)*t^2 + 2*(Pc - P1)*t + P1
         *     Pl(t) = P1*(1 - t) + P2*t
         *     t = [0:1]
         *
         *     P = (x, y) - point of interest
         *
         * Let's look at second derivative of quad curve equation:
         *
         *     Pq''(t) = 2 * (P1 - 2 * Pc + P2) = Pq''
         *     It's constant vector.
         *
         * Let's draw a line through P to be parallel to this
         * vector and find the intersection of the quad curve
         * and the line.
         *
         * Pq(t) is point of intersection if system of equations
         * below has the solution.
         *
         *     L(s) = P + Pq''*s == Pq(t)
         *     Pq''*s + (P - Pq(t)) == 0
         *
         *     | xq''*s + (x - xq(t)) == 0
         *     | yq''*s + (y - yq(t)) == 0
         *
         * This system has the solution if rank of its matrix equals to 1.
         * That is, determinant of the matrix should be zero.
         *
         *     (y - yq(t))*xq'' == (x - xq(t))*yq''
         *
         * Let's solve this equation with 't' variable.
         * Also let kx = x1 - 2*xc + x2
         *          ky = y1 - 2*yc + y2
         *
         *     t0q = (1/2)*((x - x1)*ky - (y - y1)*kx) /
         *                 ((xc - x1)*ky - (yc - y1)*kx)
         *
         * Let's do the same for our line Pl(t):
         *
         *     t0l = ((x - x1)*ky - (y - y1)*kx) /
         *           ((x2 - x1)*ky - (y2 - y1)*kx)
         *
         * It's easy to check that t0q == t0l. This fact means
         * we can compute t0 only one time.
         *
         * In case t0 < 0 or t0 > 1, we have an intersections outside
         * of shape bounds. So, P is definitely out of shape.
         *
         * In case t0 is inside [0:1], we should calculate Pq(t0)
         * and Pl(t0). We have three points for now, and all of them
         * lie on one line. So, we just need to detect, is our point
         * of interest between points of intersections or not.
         *
         * If the denominator in the t0q and t0l equations is
         * zero, then the points must be collinear and so the
         * curve is degenerate and encloses no area.  Thus the
         * result is false.
         */
        val kx = x1 - 2 * xc + x2
        val ky = y1 - 2 * yc + y2
        val dx = x - x1
        val dy = y - y1
        val dxl = x2 - x1
        val dyl = y2 - y1

        val t0 = (dx * ky - dy * kx) / (dxl * ky - dyl * kx)
        if (t0 < 0 || t0 > 1 || t0 != t0) {
            return false
        }

        val xb = kx * t0 * t0 + 2 * (xc - x1) * t0 + x1
        val yb = ky * t0 * t0 + 2 * (yc - y1) * t0 + y1
        val xl = dxl * t0 + x1
        val yl = dyl * t0 + y1

        return (x >= xb && x < xl) ||
                (x >= xl && x < xb) ||
                (y >= yb && y < yl) ||
                (y >= yl && y < yb)
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
        val ctrlx = getCtrlX()
        val ctrly = getCtrlY()
        val ctrlxtag = getTag(ctrlx, x, x + w)
        val ctrlytag = getTag(ctrly, y, y + h)

        // Trivially reject if all points are entirely to one side of
        // the rectangle.
        if (x1tag < INSIDE && x2tag < INSIDE && ctrlxtag < INSIDE) {
            return false       // All points left
        }
        if (y1tag < INSIDE && y2tag < INSIDE && ctrlytag < INSIDE) {
            return false       // All points above
        }
        if (x1tag > INSIDE && x2tag > INSIDE && ctrlxtag > INSIDE) {
            return false       // All points right
        }
        if (y1tag > INSIDE && y2tag > INSIDE && ctrlytag > INSIDE) {
            return false       // All points below
        }

        // Test for endpoints on the edge where either the segment
        // or the curve is headed "inwards" from them
        // Note: These tests are a superset of the fast endpoint tests
        //       above and thus repeat those tests, but take more time
        //       and cover more cases
        if (inwards(x1tag, x2tag, ctrlxtag) &&
            inwards(y1tag, y2tag, ctrlytag)) {
            // First endpoint on border with either edge moving inside
            return true
        }
        if (inwards(x2tag, x1tag, ctrlxtag) &&
            inwards(y2tag, y1tag, ctrlytag)) {
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
        // but the 3 points are not all on any one side of the rectangle.
        // Therefore the curve cannot be contained inside the rectangle,
        // but the rectangle might be contained inside the curve, or
        // the curve might intersect the boundary of the rectangle.

        val eqn = DoubleArray(3)
        val res = DoubleArray(3)
        if (!yoverlap) {
            // Both Y coordinates for the closing segment are above or
            // below the rectangle which means that we can only intersect
            // if the curve crosses the top (or bottom) of the rectangle
            // in more than one place and if those crossing locations
            // span the horizontal range of the rectangle.
            fillEqn(eqn, (if (y1tag < INSIDE) y else y + h), y1, ctrly, y2)
            return (solveQuadratic(eqn, res) == 2 &&
                    evalQuadratic(res, 2, true, true, null,
                        x1, ctrlx, x2) == 2 &&
                    getTag(res[0], x, x + w) * getTag(res[1], x, x + w) <= 0)
        }

        // Y ranges overlap.  Now we examine the X ranges
        if (!xoverlap) {
            // Both X coordinates for the closing segment are left of
            // or right of the rectangle which means that we can only
            // intersect if the curve crosses the left (or right) edge
            // of the rectangle in more than one place and if those
            // crossing locations span the vertical range of the rectangle.
            fillEqn(eqn, (if (x1tag < INSIDE) x else x + w), x1, ctrlx, x2)
            return (solveQuadratic(eqn, res) == 2 &&
                    evalQuadratic(res, 2, true, true, null,
                        y1, ctrly, y2) == 2 &&
                    getTag(res[0], y, y + h) * getTag(res[1], y, y + h) <= 0)
        }

        // The X and Y ranges of the endpoints overlap the X and Y
        // ranges of the rectangle, now find out how the endpoint
        // line segment intersects the Y range of the rectangle
        val dx = x2 - x1
        val dy = y2 - y1
        val k = y2 * x1 - x2 * y1
        var c1tag: Int
        var c2tag: Int
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
        // where both endpoints are to one side, except that we will
        // only get one intersection of the curve with the vertical
        // side of the rectangle.  This is because the endpoint segment
        // accounts for the other intersection.
        //
        // (Remember there is overlap in both the X and Y ranges which
        //  means that the segment must cross at least one vertical edge
        //  of the rectangle - in particular, the "near vertical side" -
        //  leaving only one intersection for the curve.)
        //
        // Now we calculate the y tags of the two intersections on the
        // "near vertical side" of the rectangle.  We will have one with
        // the endpoint segment, and one with the curve.  If those two
        // vertical intersections overlap the Y range of the rectangle,
        // we have an intersection.  Otherwise, we don't.

        // c1tag = vertical intersection class of the endpoint segment
        //
        // Choose the y tag of the endpoint that was not on the same
        // side of the rectangle as the subsegment calculated above.
        // Note that we can "steal" the existing Y tag of that endpoint
        // since it will be provably the same as the vertical intersection.
        c1tag = if (c1tag * x1tag <= 0) y1tag else y2tag

        // c2tag = vertical intersection class of the curve
        //
        // We have to calculate this one the straightforward way.
        // Note that the c2tag can still tell us which vertical edge
        // to test against.
        fillEqn(eqn, (if (c2tag < INSIDE) x else x + w), x1, ctrlx, x2)
        val num = solveQuadratic(eqn, res)

        // Note: We should be able to assert(num == 2); since the
        // X range "crosses" (not touches) the vertical boundary,
        // but we pass num to evalQuadratic for completeness.
        evalQuadratic(res, num, true, true, null, y1, ctrly, y2)

        // Note: We can assert(num evals == 1); since one of the
        // 2 crossings will be out of the [0,1] range.
        c2tag = getTag(res[0], y, y + h)

        // Finally, we have an intersection if the two crossings
        // overlap the Y range of the rectangle.
        return (c1tag * c2tag <= 0)
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (w <= 0 || h <= 0) {
            return false
        }
        // Assertion: Quadratic curves closed by connecting their
        // endpoints are always convex.
        return (contains(x, y) &&
                contains(x + w, y) &&
                contains(x + w, y + h) &&
                contains(x, y + h))
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun getBounds(): Rectangle {
        return getBounds2D().getBounds()
    }

    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return QuadIterator(this, at)
    }

    override fun getPathIterator(at: AffineTransform?, flatness: kotlin.Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    /**
     * Creates a new object of the same class and with the same contents
     * as this object.
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
         * Returns the square of the flatness, or maximum distance of a
         * control point from the line connecting the end points, of the
         * quadratic curve specified by the indicated control points.
         */
        @JvmStatic
        fun getFlatnessSq(x1: kotlin.Double, y1: kotlin.Double,
                          ctrlx: kotlin.Double, ctrly: kotlin.Double,
                          x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            return Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx, ctrly)
        }

        /**
         * Returns the flatness, or maximum distance of a
         * control point from the line connecting the end points, of the
         * quadratic curve specified by the indicated control points.
         */
        @JvmStatic
        fun getFlatness(x1: kotlin.Double, y1: kotlin.Double,
                        ctrlx: kotlin.Double, ctrly: kotlin.Double,
                        x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            return Line2D.ptSegDist(x1, y1, x2, y2, ctrlx, ctrly)
        }

        /**
         * Returns the square of the flatness, or maximum distance of a
         * control point from the line connecting the end points, of the
         * quadratic curve specified by the control points stored in the
         * indicated array at the indicated index.
         */
        @JvmStatic
        fun getFlatnessSq(coords: DoubleArray, offset: Int): kotlin.Double {
            return Line2D.ptSegDistSq(coords[offset + 0], coords[offset + 1],
                coords[offset + 4], coords[offset + 5],
                coords[offset + 2], coords[offset + 3])
        }

        /**
         * Returns the flatness, or maximum distance of a
         * control point from the line connecting the end points, of the
         * quadratic curve specified by the control points stored in the
         * indicated array at the indicated index.
         */
        @JvmStatic
        fun getFlatness(coords: DoubleArray, offset: Int): kotlin.Double {
            return Line2D.ptSegDist(coords[offset + 0], coords[offset + 1],
                coords[offset + 4], coords[offset + 5],
                coords[offset + 2], coords[offset + 3])
        }

        /**
         * Subdivides the quadratic curve specified by the `src`
         * parameter and stores the resulting two subdivided curves into the
         * `left` and `right` curve parameters.
         */
        @JvmStatic
        fun subdivide(src: QuadCurve2D,
                      left: QuadCurve2D?,
                      right: QuadCurve2D?) {
            val x1 = src.getX1()
            val y1 = src.getY1()
            var ctrlx = src.getCtrlX()
            var ctrly = src.getCtrlY()
            val x2 = src.getX2()
            val y2 = src.getY2()
            val ctrlx1 = (x1 + ctrlx) / 2.0
            val ctrly1 = (y1 + ctrly) / 2.0
            val ctrlx2 = (x2 + ctrlx) / 2.0
            val ctrly2 = (y2 + ctrly) / 2.0
            ctrlx = (ctrlx1 + ctrlx2) / 2.0
            ctrly = (ctrly1 + ctrly2) / 2.0
            left?.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx, ctrly)
            right?.setCurve(ctrlx, ctrly, ctrlx2, ctrly2, x2, y2)
        }

        /**
         * Subdivides the quadratic curve specified by the coordinates
         * stored in the `src` array at indices
         * `srcoff` through `srcoff`&nbsp;+&nbsp;5
         * and stores the resulting two subdivided curves into the two
         * result arrays at the corresponding indices.
         */
        @JvmStatic
        fun subdivide(src: DoubleArray, srcoff: Int,
                      left: DoubleArray?, leftoff: Int,
                      right: DoubleArray?, rightoff: Int) {
            var x1 = src[srcoff + 0]
            var y1 = src[srcoff + 1]
            var ctrlx = src[srcoff + 2]
            var ctrly = src[srcoff + 3]
            var x2 = src[srcoff + 4]
            var y2 = src[srcoff + 5]
            if (left != null) {
                left[leftoff + 0] = x1
                left[leftoff + 1] = y1
            }
            if (right != null) {
                right[rightoff + 4] = x2
                right[rightoff + 5] = y2
            }
            x1 = (x1 + ctrlx) / 2.0
            y1 = (y1 + ctrly) / 2.0
            x2 = (x2 + ctrlx) / 2.0
            y2 = (y2 + ctrly) / 2.0
            ctrlx = (x1 + x2) / 2.0
            ctrly = (y1 + y2) / 2.0
            if (left != null) {
                left[leftoff + 2] = x1
                left[leftoff + 3] = y1
                left[leftoff + 4] = ctrlx
                left[leftoff + 5] = ctrly
            }
            if (right != null) {
                right[rightoff + 0] = ctrlx
                right[rightoff + 1] = ctrly
                right[rightoff + 2] = x2
                right[rightoff + 3] = y2
            }
        }

        /**
         * Solves the quadratic whose coefficients are in the `eqn`
         * array and places the non-complex roots back into the same array,
         * returning the number of roots.
         */
        @JvmStatic
        fun solveQuadratic(eqn: DoubleArray): Int {
            return solveQuadratic(eqn, eqn)
        }

        /**
         * Solves the quadratic whose coefficients are in the `eqn`
         * array and places the non-complex roots into the `res`
         * array, returning the number of roots.
         */
        @JvmStatic
        fun solveQuadratic(eqn: DoubleArray, res: DoubleArray): Int {
            val a = eqn[2]
            val b = eqn[1]
            val c = eqn[0]
            var roots = 0
            if (a == 0.0) {
                // The quadratic parabola has degenerated to a line.
                if (b == 0.0) {
                    // The line has degenerated to a constant.
                    return -1
                }
                res[roots++] = -c / b
            } else {
                // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
                var d = b * b - 4.0 * a * c
                if (d < 0.0) {
                    // If d < 0.0, then there are no roots
                    return 0
                }
                d = Math.sqrt(d)
                // For accuracy, calculate one root using:
                //     (-b +/- d) / 2a
                // and the other using:
                //     2c / (-b +/- d)
                // Choose the sign of the +/- so that b+d gets larger in magnitude
                if (b < 0.0) {
                    d = -d
                }
                val q = (b + d) / -2.0
                // We already tested a for being 0 above
                res[roots++] = q / a
                if (q != 0.0) {
                    res[roots++] = c / q
                }
            }
            return roots
        }

        /*
         * Fill an array with the coefficients of the parametric equation
         * in t, ready for solving against val with solveQuadratic.
         * We currently have:
         *     val = Py(t) = C1*(1-t)^2 + 2*CP*t*(1-t) + C2*t^2
         *                 = C1 - 2*C1*t + C1*t^2 + 2*CP*t - 2*CP*t^2 + C2*t^2
         *                 = C1 + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2
         *               0 = (C1 - val) + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2
         *               0 = C + Bt + At^2
         *     C = C1 - val
         *     B = 2*CP - 2*C1
         *     A = C1 - 2*CP + C2
         */
        private fun fillEqn(eqn: DoubleArray, `val`: kotlin.Double,
                            c1: kotlin.Double, cp: kotlin.Double, c2: kotlin.Double) {
            eqn[0] = c1 - `val`
            eqn[1] = cp + cp - c1 - c1
            eqn[2] = c1 - cp - cp + c2
            return
        }

        /*
         * Evaluate the t values in the first num slots of the vals[] array
         * and place the evaluated values back into the same array.  Only
         * evaluate t values that are within the range <0, 1>, including
         * the 0 and 1 ends of the range iff the include0 or include1
         * booleans are true.  If an "inflection" equation is handed in,
         * then any points which represent a point of inflection for that
         * quadratic equation are also ignored.
         */
        private fun evalQuadratic(vals: DoubleArray, num: Int,
                                  include0: Boolean,
                                  include1: Boolean,
                                  inflect: DoubleArray?,
                                  c1: kotlin.Double, ctrl: kotlin.Double, c2: kotlin.Double): Int {
            var j = 0
            for (i in 0 until num) {
                val t = vals[i]
                if ((if (include0) t >= 0 else t > 0) &&
                    (if (include1) t <= 1 else t < 1) &&
                    (inflect == null ||
                            inflect[1] + 2 * inflect[2] * t != 0.0)) {
                    val u = 1 - t
                    vals[j++] = c1 * u * u + 2 * ctrl * t * u + c2 * t * t
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
