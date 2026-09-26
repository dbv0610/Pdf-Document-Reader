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
 * This `Line2D` represents a line segment in (x,y)
 * coordinate space.
 */
abstract class Line2D protected constructor() : Shape, Cloneable {

    /**
     * A line segment specified with float coordinates.
     */
    open class Float : Line2D, Serializable {
        @JvmField
        var x1 = 0f

        @JvmField
        var y1 = 0f

        @JvmField
        var x2 = 0f

        @JvmField
        var y2 = 0f

        /**
         * Constructs and initializes a Line with coordinates (0, 0) -> (0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a Line from the specified coordinates.
         */
        constructor(x1: kotlin.Float, y1: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float) {
            setLine(x1, y1, x2, y2)
        }

        /**
         * Constructs and initializes a `Line2D` from the
         * specified `Point2D` objects.
         */
        constructor(p1: Point2D, p2: Point2D) {
            setLine(p1, p2)
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

        override fun getX2(): kotlin.Double {
            return x2.toDouble()
        }

        override fun getY2(): kotlin.Double {
            return y2.toDouble()
        }

        override fun getP2(): Point2D {
            return Point2D.Float(x2, y2)
        }

        override fun setLine(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1.toFloat()
            this.y1 = y1.toFloat()
            this.x2 = x2.toFloat()
            this.y2 = y2.toFloat()
        }

        /**
         * Sets the location of the end points of this `Line2D`
         * to the specified float coordinates.
         */
        open fun setLine(x1: kotlin.Float, y1: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val x: kotlin.Float
            val y: kotlin.Float
            val w: kotlin.Float
            val h: kotlin.Float
            if (x1 < x2) {
                x = x1
                w = x2 - x1
            } else {
                x = x2
                w = x1 - x2
            }
            if (y1 < y2) {
                y = y1
                h = y2 - y1
            } else {
                y = y2
                h = y1 - y2
            }
            return Rectangle2D.Float(x, y, w, h)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 6161772511649436349L
        }
    }

    /**
     * A line segment specified with double coordinates.
     */
    open class Double : Line2D, Serializable {
        @JvmField
        var x1 = 0.0

        @JvmField
        var y1 = 0.0

        @JvmField
        var x2 = 0.0

        @JvmField
        var y2 = 0.0

        /**
         * Constructs and initializes a Line with coordinates (0, 0) -> (0, 0).
         */
        constructor()

        /**
         * Constructs and initializes a `Line2D` from the
         * specified coordinates.
         */
        constructor(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            setLine(x1, y1, x2, y2)
        }

        /**
         * Constructs and initializes a `Line2D` from the
         * specified `Point2D` objects.
         */
        constructor(p1: Point2D, p2: Point2D) {
            setLine(p1, p2)
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

        override fun getX2(): kotlin.Double {
            return x2
        }

        override fun getY2(): kotlin.Double {
            return y2
        }

        override fun getP2(): Point2D {
            return Point2D.Double(x2, y2)
        }

        override fun setLine(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2
        }

        override fun getBounds2D(): Rectangle2D {
            val x: kotlin.Double
            val y: kotlin.Double
            val w: kotlin.Double
            val h: kotlin.Double
            if (x1 < x2) {
                x = x1
                w = x2 - x1
            } else {
                x = x2
                w = x1 - x2
            }
            if (y1 < y2) {
                y = y1
                h = y2 - y1
            } else {
                y = y2
                h = y1 - y2
            }
            return Rectangle2D.Double(x, y, w, h)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 7979627399746467499L
        }
    }

    /**
     * Returns the X coordinate of the start point in double precision.
     */
    abstract fun getX1(): kotlin.Double

    /**
     * Returns the Y coordinate of the start point in double precision.
     */
    abstract fun getY1(): kotlin.Double

    /**
     * Returns the start `Point2D` of this `Line2D`.
     */
    abstract fun getP1(): Point2D

    /**
     * Returns the X coordinate of the end point in double precision.
     */
    abstract fun getX2(): kotlin.Double

    /**
     * Returns the Y coordinate of the end point in double precision.
     */
    abstract fun getY2(): kotlin.Double

    /**
     * Returns the end `Point2D` of this `Line2D`.
     */
    abstract fun getP2(): Point2D

    /**
     * Sets the location of the end points of this `Line2D` to
     * the specified double coordinates.
     */
    abstract fun setLine(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double)

    /**
     * Sets the location of the end points of this `Line2D` to
     * the specified `Point2D` coordinates.
     */
    open fun setLine(p1: Point2D, p2: Point2D) {
        setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY())
    }

    /**
     * Sets the location of the end points of this `Line2D` to
     * the same as those end points of the specified `Line2D`.
     */
    open fun setLine(l: Line2D) {
        setLine(l.getX1(), l.getY1(), l.getX2(), l.getY2())
    }

    /**
     * Returns an indicator of where the specified point
     * `(px,py)` lies with respect to this line segment.
     */
    open fun relativeCCW(px: kotlin.Double, py: kotlin.Double): Int {
        return Line2D.relativeCCW(getX1(), getY1(), getX2(), getY2(), px, py)
    }

    /**
     * Returns an indicator of where the specified `Point2D`
     * lies with respect to this line segment.
     */
    open fun relativeCCW(p: Point2D): Int {
        return Line2D.relativeCCW(getX1(), getY1(), getX2(), getY2(),
            p.getX(), p.getY())
    }

    /**
     * Tests if the line segment from `(x1,y1)` to
     * `(x2,y2)` intersects this line segment.
     */
    open fun intersectsLine(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): Boolean {
        return linesIntersect(x1, y1, x2, y2,
            getX1(), getY1(), getX2(), getY2())
    }

    /**
     * Tests if the specified line segment intersects this line segment.
     */
    open fun intersectsLine(l: Line2D): Boolean {
        return linesIntersect(l.getX1(), l.getY1(), l.getX2(), l.getY2(),
            getX1(), getY1(), getX2(), getY2())
    }

    /**
     * Returns the square of the distance from a point to this line segment.
     */
    open fun ptSegDistSq(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        return Line2D.ptSegDistSq(getX1(), getY1(), getX2(), getY2(), px, py)
    }

    /**
     * Returns the square of the distance from a `Point2D` to
     * this line segment.
     */
    open fun ptSegDistSq(pt: Point2D): kotlin.Double {
        return Line2D.ptSegDistSq(getX1(), getY1(), getX2(), getY2(),
            pt.getX(), pt.getY())
    }

    /**
     * Returns the distance from a point to this line segment.
     */
    open fun ptSegDist(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        return Line2D.ptSegDist(getX1(), getY1(), getX2(), getY2(), px, py)
    }

    /**
     * Returns the distance from a `Point2D` to this line
     * segment.
     */
    open fun ptSegDist(pt: Point2D): kotlin.Double {
        return Line2D.ptSegDist(getX1(), getY1(), getX2(), getY2(),
            pt.getX(), pt.getY())
    }

    /**
     * Returns the square of the distance from a point to this line.
     */
    open fun ptLineDistSq(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        return Line2D.ptLineDistSq(getX1(), getY1(), getX2(), getY2(), px, py)
    }

    /**
     * Returns the square of the distance from a specified
     * `Point2D` to this line.
     */
    open fun ptLineDistSq(pt: Point2D): kotlin.Double {
        return Line2D.ptLineDistSq(getX1(), getY1(), getX2(), getY2(),
            pt.getX(), pt.getY())
    }

    /**
     * Returns the distance from a point to this line.
     */
    open fun ptLineDist(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        return Line2D.ptLineDist(getX1(), getY1(), getX2(), getY2(), px, py)
    }

    /**
     * Returns the distance from a `Point2D` to this line.
     */
    open fun ptLineDist(pt: Point2D): kotlin.Double {
        return Line2D.ptLineDist(getX1(), getY1(), getX2(), getY2(),
            pt.getX(), pt.getY())
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this
     * `Line2D`.  This method is required to implement the
     * [Shape] interface, but in the case of `Line2D`
     * objects it always returns `false` since a line contains
     * no area.
     */
    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        return false
    }

    override fun contains(p: Point2D): Boolean {
        return false
    }

    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        return intersects(Rectangle2D.Double(x, y, w, h))
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return r.intersectsLine(getX1(), getY1(), getX2(), getY2())
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        return false
    }

    override fun contains(r: Rectangle2D): Boolean {
        return false
    }

    override fun getBounds(): Rectangle {
        return getBounds2D().getBounds()
    }

    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return LineIterator(this, at)
    }

    override fun getPathIterator(at: AffineTransform?, flatness: kotlin.Double): PathIterator {
        return LineIterator(this, at)
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
         * Returns an indicator of where the specified point
         * `(px,py)` lies with respect to the line segment from
         * `(x1,y1)` to `(x2,y2)`.
         */
        @JvmStatic
        fun relativeCCW(x1: kotlin.Double, y1: kotlin.Double,
                        x2: kotlin.Double, y2: kotlin.Double,
                        px: kotlin.Double, py: kotlin.Double): Int {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            x2 -= x1
            y2 -= y1
            px -= x1
            py -= y1
            var ccw = px * y2 - py * x2
            if (ccw == 0.0) {
                // The point is colinear, classify based on which side of
                // the segment the point falls on.  We can calculate a
                // relative value using the projection of px,py onto the
                // segment - a negative value indicates the point projects
                // outside of the segment in the direction of the particular
                // endpoint used as the origin for the projection.
                ccw = px * x2 + py * y2
                if (ccw > 0.0) {
                    // Reverse the projection to be relative to the original x2,y2
                    // x2 and y2 are simply negated.
                    // px and py need to have (x2 - x1) or (y2 - y1) subtracted
                    //    from them (based on the original values)
                    // Since we really want to get a positive answer when the
                    //    point is "beyond (x2,y2)", then we want to calculate
                    //    the inverse anyway - thus we leave x2 & y2 negated.
                    px -= x2
                    py -= y2
                    ccw = px * x2 + py * y2
                    if (ccw < 0.0) {
                        ccw = 0.0
                    }
                }
            }
            return if (ccw < 0.0) -1 else (if (ccw > 0.0) 1 else 0)
        }

        /**
         * Tests if the line segment from `(x1,y1)` to
         * `(x2,y2)` intersects the line segment from `(x3,y3)`
         * to `(x4,y4)`.
         */
        @JvmStatic
        fun linesIntersect(x1: kotlin.Double, y1: kotlin.Double,
                           x2: kotlin.Double, y2: kotlin.Double,
                           x3: kotlin.Double, y3: kotlin.Double,
                           x4: kotlin.Double, y4: kotlin.Double): Boolean {
            return ((relativeCCW(x1, y1, x2, y2, x3, y3) *
                    relativeCCW(x1, y1, x2, y2, x4, y4) <= 0)
                    && (relativeCCW(x3, y3, x4, y4, x1, y1) *
                    relativeCCW(x3, y3, x4, y4, x2, y2) <= 0))
        }

        /**
         * Returns the square of the distance from a point to a line segment.
         */
        @JvmStatic
        fun ptSegDistSq(x1: kotlin.Double, y1: kotlin.Double,
                        x2: kotlin.Double, y2: kotlin.Double,
                        px: kotlin.Double, py: kotlin.Double): kotlin.Double {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            // Adjust vectors relative to x1,y1
            // x2,y2 becomes relative vector from x1,y1 to end of segment
            x2 -= x1
            y2 -= y1
            // px,py becomes relative vector from x1,y1 to test point
            px -= x1
            py -= y1
            var dotprod = px * x2 + py * y2
            val projlenSq: kotlin.Double
            if (dotprod <= 0.0) {
                // px,py is on the side of x1,y1 away from x2,y2
                // distance to segment is length of px,py vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0
            } else {
                // switch to backwards vectors relative to x2,y2
                // x2,y2 are already the negative of x1,y1=>x2,y2
                // to get px,py to be the negative of px,py=>x2,y2
                // the dot product of two negated vectors is the same
                // as the dot product of the two normal vectors
                px = x2 - px
                py = y2 - py
                dotprod = px * x2 + py * y2
                if (dotprod <= 0.0) {
                    // px,py is on the side of x2,y2 away from x1,y1
                    // distance to segment is length of (backwards) px,py vector
                    // "length of its (clipped) projection" is now 0.0
                    projlenSq = 0.0
                } else {
                    // px,py is between x1,y1 and x2,y2
                    // dotprod is the length of the px,py vector
                    // projected on the x2,y2=>x1,y1 vector times the
                    // length of the x2,y2=>x1,y1 vector
                    projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2)
                }
            }
            // Distance to line is now the length of the relative point
            // vector minus the length of its projection onto the line
            // (which is zero if the projection falls outside the range
            //  of the line segment).
            var lenSq = px * px + py * py - projlenSq
            if (lenSq < 0) {
                lenSq = 0.0
            }
            return lenSq
        }

        /**
         * Returns the distance from a point to a line segment.
         */
        @JvmStatic
        fun ptSegDist(x1: kotlin.Double, y1: kotlin.Double,
                      x2: kotlin.Double, y2: kotlin.Double,
                      px: kotlin.Double, py: kotlin.Double): kotlin.Double {
            return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py))
        }

        /**
         * Returns the square of the distance from a point to a line.
         */
        @JvmStatic
        fun ptLineDistSq(x1: kotlin.Double, y1: kotlin.Double,
                         x2: kotlin.Double, y2: kotlin.Double,
                         px: kotlin.Double, py: kotlin.Double): kotlin.Double {
            var x2 = x2
            var y2 = y2
            var px = px
            var py = py
            // Adjust vectors relative to x1,y1
            // x2,y2 becomes relative vector from x1,y1 to end of segment
            x2 -= x1
            y2 -= y1
            // px,py becomes relative vector from x1,y1 to test point
            px -= x1
            py -= y1
            val dotprod = px * x2 + py * y2
            // dotprod is the length of the px,py vector
            // projected on the x1,y1=>x2,y2 vector times the
            // length of the x1,y1=>x2,y2 vector
            val projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2)
            // Distance to line is now the length of the relative point
            // vector minus the length of its projection onto the line
            var lenSq = px * px + py * py - projlenSq
            if (lenSq < 0) {
                lenSq = 0.0
            }
            return lenSq
        }

        /**
         * Returns the distance from a point to a line.
         */
        @JvmStatic
        fun ptLineDist(x1: kotlin.Double, y1: kotlin.Double,
                       x2: kotlin.Double, y2: kotlin.Double,
                       px: kotlin.Double, py: kotlin.Double): kotlin.Double {
            return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py))
        }
    }
}
