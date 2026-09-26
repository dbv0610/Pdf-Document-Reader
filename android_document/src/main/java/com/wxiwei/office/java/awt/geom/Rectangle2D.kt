/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.wxiwei.office.java.awt.geom

import java.io.Serializable

/**
 * The `Rectangle2D` class describes a rectangle
 * defined by a location (x,&nbsp;y) and dimension
 * (w&nbsp;x&nbsp;h).
 */
abstract class Rectangle2D protected constructor() : RectangularShape() {

    companion object {
        /**
         * The bitmask that indicates that a point lies to the left of
         * this `Rectangle2D`.
         */
        const val OUT_LEFT = 1

        /**
         * The bitmask that indicates that a point lies above
         * this `Rectangle2D`.
         */
        const val OUT_TOP = 2

        /**
         * The bitmask that indicates that a point lies to the right of
         * this `Rectangle2D`.
         */
        const val OUT_RIGHT = 4

        /**
         * The bitmask that indicates that a point lies below
         * this `Rectangle2D`.
         */
        const val OUT_BOTTOM = 8

        /**
         * Intersects the pair of specified source `Rectangle2D`
         * objects and puts the result into the specified destination
         * `Rectangle2D` object.
         */
        @JvmStatic
        fun intersect(src1: Rectangle2D,
                      src2: Rectangle2D,
                      dest: Rectangle2D) {
            val x1 = Math.max(src1.getMinX(), src2.getMinX())
            val y1 = Math.max(src1.getMinY(), src2.getMinY())
            val x2 = Math.min(src1.getMaxX(), src2.getMaxX())
            val y2 = Math.min(src1.getMaxY(), src2.getMaxY())
            dest.setFrame(x1, y1, x2 - x1, y2 - y1)
        }

        /**
         * Unions the pair of source `Rectangle2D` objects
         * and puts the result into the specified destination
         * `Rectangle2D` object.
         */
        @JvmStatic
        fun union(src1: Rectangle2D,
                  src2: Rectangle2D,
                  dest: Rectangle2D) {
            val x1 = Math.min(src1.getMinX(), src2.getMinX())
            val y1 = Math.min(src1.getMinY(), src2.getMinY())
            val x2 = Math.max(src1.getMaxX(), src2.getMaxX())
            val y2 = Math.max(src1.getMaxY(), src2.getMaxY())
            dest.setFrameFromDiagonal(x1, y1, x2, y2)
        }
    }

    /**
     * The `Float` class defines a rectangle specified in float
     * coordinates.
     */
    open class Float : Rectangle2D, Serializable {
        /**
         * The X coordinate of this `Rectangle2D`.
         */
        @JvmField
        var x = 0f

        /**
         * The Y coordinate of this `Rectangle2D`.
         */
        @JvmField
        var y = 0f

        /**
         * The width of this `Rectangle2D`.
         */
        @JvmField
        var width = 0f

        /**
         * The height of this `Rectangle2D`.
         */
        @JvmField
        var height = 0f

        /**
         * Constructs a new `Rectangle2D`, initialized to
         * location (0.0,&nbsp;0.0) and size (0.0,&nbsp;0.0).
         */
        constructor()

        /**
         * Constructs and initializes a `Rectangle2D`
         * from the specified `float` coordinates.
         */
        constructor(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float) {
            setRect(x, y, w, h)
        }

        override fun getX(): kotlin.Double {
            return x.toDouble()
        }

        override fun getY(): kotlin.Double {
            return y.toDouble()
        }

        override fun getWidth(): kotlin.Double {
            return width.toDouble()
        }

        override fun getHeight(): kotlin.Double {
            return height.toDouble()
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0f) || (height <= 0.0f)
        }

        /**
         * Sets the location and size of this `Rectangle2D`
         * to the specified `float` values.
         */
        open fun setRect(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
        }

        override fun setRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
            this.width = w.toFloat()
            this.height = h.toFloat()
        }

        override fun setRect(r: Rectangle2D) {
            this.x = r.getX().toFloat()
            this.y = r.getY().toFloat()
            this.width = r.getWidth().toFloat()
            this.height = r.getHeight().toFloat()
        }

        override fun outcode(x: kotlin.Double, y: kotlin.Double): Int {
            /*
             * Note on casts to double below.  If the arithmetic of
             * x+w or y+h is done in float, then some bits may be
             * lost if the binary exponents of x/y and w/h are not
             * similar.  By converting to double before the addition
             * we force the addition to be carried out in double to
             * avoid rounding error in the comparison.
             *
             * See bug 4320890 for problems that this inaccuracy causes.
             */
            var out = 0
            if (this.width <= 0) {
                out = out or (OUT_LEFT or OUT_RIGHT)
            } else if (x < this.x) {
                out = out or OUT_LEFT
            } else if (x > this.x + this.width.toDouble()) {
                out = out or OUT_RIGHT
            }
            if (this.height <= 0) {
                out = out or (OUT_TOP or OUT_BOTTOM)
            } else if (y < this.y) {
                out = out or OUT_TOP
            } else if (y > this.y + this.height.toDouble()) {
                out = out or OUT_BOTTOM
            }
            return out
        }

        override fun getBounds2D(): Rectangle2D {
            return Float(x, y, width, height)
        }

        override fun createIntersection(r: Rectangle2D): Rectangle2D {
            val dest: Rectangle2D
            if (r is Float) {
                dest = Float()
            } else {
                dest = Double()
            }
            Rectangle2D.intersect(this, r, dest)
            return dest
        }

        override fun createUnion(r: Rectangle2D): Rectangle2D {
            val dest: Rectangle2D
            if (r is Float) {
                dest = Float()
            } else {
                dest = Double()
            }
            Rectangle2D.union(this, r, dest)
            return dest
        }

        override fun toString(): String {
            return (javaClass.name
                    + "[x=" + x +
                    ",y=" + y +
                    ",w=" + width +
                    ",h=" + height + "]")
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 3798716824173675777L
        }
    }

    /**
     * The `Double` class defines a rectangle specified in
     * double coordinates.
     */
    open class Double : Rectangle2D, Serializable {
        /**
         * The X coordinate of this `Rectangle2D`.
         */
        @JvmField
        var x = 0.0

        /**
         * The Y coordinate of this `Rectangle2D`.
         */
        @JvmField
        var y = 0.0

        /**
         * The width of this `Rectangle2D`.
         */
        @JvmField
        var width = 0.0

        /**
         * The height of this `Rectangle2D`.
         */
        @JvmField
        var height = 0.0

        /**
         * Constructs a new `Rectangle2D`, initialized to
         * location (0,&nbsp;0) and size (0,&nbsp;0).
         */
        constructor()

        /**
         * Constructs and initializes a `Rectangle2D`
         * from the specified `double` coordinates.
         */
        constructor(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            setRect(x, y, w, h)
        }

        override fun getX(): kotlin.Double {
            return x
        }

        override fun getY(): kotlin.Double {
            return y
        }

        override fun getWidth(): kotlin.Double {
            return width
        }

        override fun getHeight(): kotlin.Double {
            return height
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0) || (height <= 0.0)
        }

        override fun setRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
        }

        override fun setRect(r: Rectangle2D) {
            this.x = r.getX()
            this.y = r.getY()
            this.width = r.getWidth()
            this.height = r.getHeight()
        }

        override fun outcode(x: kotlin.Double, y: kotlin.Double): Int {
            var out = 0
            if (this.width <= 0) {
                out = out or (OUT_LEFT or OUT_RIGHT)
            } else if (x < this.x) {
                out = out or OUT_LEFT
            } else if (x > this.x + this.width) {
                out = out or OUT_RIGHT
            }
            if (this.height <= 0) {
                out = out or (OUT_TOP or OUT_BOTTOM)
            } else if (y < this.y) {
                out = out or OUT_TOP
            } else if (y > this.y + this.height) {
                out = out or OUT_BOTTOM
            }
            return out
        }

        override fun getBounds2D(): Rectangle2D {
            return Double(x, y, width, height)
        }

        override fun createIntersection(r: Rectangle2D): Rectangle2D {
            val dest: Rectangle2D = Double()
            Rectangle2D.intersect(this, r, dest)
            return dest
        }

        override fun createUnion(r: Rectangle2D): Rectangle2D {
            val dest: Rectangle2D = Double()
            Rectangle2D.union(this, r, dest)
            return dest
        }

        override fun toString(): String {
            return (javaClass.name
                    + "[x=" + x +
                    ",y=" + y +
                    ",w=" + width +
                    ",h=" + height + "]")
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 7771313791441850493L
        }
    }

    /**
     * Sets the location and size of this `Rectangle2D`
     * to the specified `double` values.
     */
    abstract fun setRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double)

    /**
     * Sets this `Rectangle2D` to be the same as the specified
     * `Rectangle2D`.
     */
    open fun setRect(r: Rectangle2D) {
        setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    /**
     * Tests if the specified line segment intersects the interior of this
     * `Rectangle2D`.
     */
    open fun intersectsLine(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): Boolean {
        var x1 = x1
        var y1 = y1
        var out1: Int
        val out2: Int = outcode(x2, y2)
        if (out2 == 0) {
            return true
        }
        while (true) {
            out1 = outcode(x1, y1)
            if (out1 == 0) {
                break
            }
            if ((out1 and out2) != 0) {
                return false
            }
            if ((out1 and (OUT_LEFT or OUT_RIGHT)) != 0) {
                var x = getX()
                if ((out1 and OUT_RIGHT) != 0) {
                    x += getWidth()
                }
                y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1)
                x1 = x
            } else {
                var y = getY()
                if ((out1 and OUT_BOTTOM) != 0) {
                    y += getHeight()
                }
                x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
                y1 = y
            }
        }
        return true
    }

    /**
     * Tests if the specified line segment intersects the interior of this
     * `Rectangle2D`.
     */
    open fun intersectsLine(l: Line2D): Boolean {
        return intersectsLine(l.getX1(), l.getY1(), l.getX2(), l.getY2())
    }

    /**
     * Determines where the specified coordinates lie with respect
     * to this `Rectangle2D`.
     */
    abstract fun outcode(x: kotlin.Double, y: kotlin.Double): Int

    /**
     * Determines where the specified [Point2D] lies with
     * respect to this `Rectangle2D`.
     */
    open fun outcode(p: Point2D): Int {
        return outcode(p.getX(), p.getY())
    }

    /**
     * Sets the location and size of the outer bounds of this
     * `Rectangle2D` to the specified rectangular values.
     */
    override fun setFrame(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
        setRect(x, y, w, h)
    }

    override fun getBounds2D(): Rectangle2D {
        return clone() as Rectangle2D
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        val x0 = getX()
        val y0 = getY()
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight())
    }

    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false
        }
        val x0 = getX()
        val y0 = getY()
        return (x + w > x0 &&
                y + h > y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight())
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false
        }
        val x0 = getX()
        val y0 = getY()
        return (x >= x0 &&
                y >= y0 &&
                (x + w) <= x0 + getWidth() &&
                (y + h) <= y0 + getHeight())
    }

    /**
     * Returns a new `Rectangle2D` object representing the
     * intersection of this `Rectangle2D` with the specified
     * `Rectangle2D`.
     */
    abstract fun createIntersection(r: Rectangle2D): Rectangle2D

    /**
     * Returns a new `Rectangle2D` object representing the
     * union of this `Rectangle2D` with the specified
     * `Rectangle2D`.
     */
    abstract fun createUnion(r: Rectangle2D): Rectangle2D

    /**
     * Adds a point, specified by the double precision arguments
     * `newx` and `newy`, to this
     * `Rectangle2D`.
     */
    open fun add(newx: kotlin.Double, newy: kotlin.Double) {
        val x1 = Math.min(getMinX(), newx)
        val x2 = Math.max(getMaxX(), newx)
        val y1 = Math.min(getMinY(), newy)
        val y2 = Math.max(getMaxY(), newy)
        setRect(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * Adds the `Point2D` object `pt` to this
     * `Rectangle2D`.
     */
    open fun add(pt: Point2D) {
        add(pt.getX(), pt.getY())
    }

    /**
     * Adds a `Rectangle2D` object to this
     * `Rectangle2D`.
     */
    open fun add(r: Rectangle2D) {
        val x1 = Math.min(getMinX(), r.getMinX())
        val x2 = Math.max(getMaxX(), r.getMaxX())
        val y1 = Math.min(getMinY(), r.getMinY())
        val y2 = Math.max(getMaxY(), r.getMaxY())
        setRect(x1, y1, x2 - x1, y2 - y1)
    }

    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return RectIterator(this, at)
    }

    override fun getPathIterator(at: AffineTransform?, flatness: kotlin.Double): PathIterator {
        return RectIterator(this, at)
    }

    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(getX())
        bits += java.lang.Double.doubleToLongBits(getY()) * 37
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Rectangle2D) {
            val r2d = other
            return ((getX() == r2d.getX()) &&
                    (getY() == r2d.getY()) &&
                    (getWidth() == r2d.getWidth()) &&
                    (getHeight() == r2d.getHeight()))
        }
        return false
    }
}
