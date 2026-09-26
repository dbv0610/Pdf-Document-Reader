/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

import java.io.Serializable

/**
 * The `RoundRectangle2D` class defines a rectangle with
 * rounded corners defined by a location `(x,y)`, a
 * dimension `(w x h)`, and the width and height of an arc
 * with which to round the corners.
 */
abstract class RoundRectangle2D protected constructor() : RectangularShape() {

    /**
     * The `Float` class defines a rectangle with rounded
     * corners all specified in `float` coordinates.
     */
    open class Float : RoundRectangle2D, Serializable {
        /**
         * The X coordinate of this `RoundRectangle2D`.
         */
        @JvmField
        var x = 0f

        /**
         * The Y coordinate of this `RoundRectangle2D`.
         */
        @JvmField
        var y = 0f

        /**
         * The width of this `RoundRectangle2D`.
         */
        @JvmField
        var width = 0f

        /**
         * The height of this `RoundRectangle2D`.
         */
        @JvmField
        var height = 0f

        /**
         * The width of the arc that rounds off the corners.
         */
        @JvmField
        var arcwidth = 0f

        /**
         * The height of the arc that rounds off the corners.
         */
        @JvmField
        var archeight = 0f

        /**
         * Constructs a new `RoundRectangle2D`, initialized to
         * location (0.0,&nbsp;0), size (0.0,&nbsp;0.0), and corner arcs
         * of radius 0.0.
         */
        constructor()

        /**
         * Constructs and initializes a `RoundRectangle2D`
         * from the specified `float` coordinates.
         */
        constructor(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float, arcw: kotlin.Float, arch: kotlin.Float) {
            setRoundRect(x, y, w, h, arcw, arch)
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

        override fun getArcWidth(): kotlin.Double {
            return arcwidth.toDouble()
        }

        override fun getArcHeight(): kotlin.Double {
            return archeight.toDouble()
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0f) || (height <= 0.0f)
        }

        /**
         * Sets the location, size, and corner radii of this
         * `RoundRectangle2D` to the specified
         * `float` values.
         */
        fun setRoundRect(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float, arcw: kotlin.Float, arch: kotlin.Float) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
            this.arcwidth = arcw
            this.archeight = arch
        }

        override fun setRoundRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double, arcWidth: kotlin.Double, arcHeight: kotlin.Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
            this.width = w.toFloat()
            this.height = h.toFloat()
            this.arcwidth = arcWidth.toFloat()
            this.archeight = arcHeight.toFloat()
        }

        override fun setRoundRect(rr: RoundRectangle2D) {
            this.x = rr.getX().toFloat()
            this.y = rr.getY().toFloat()
            this.width = rr.getWidth().toFloat()
            this.height = rr.getHeight().toFloat()
            this.arcwidth = rr.getArcWidth().toFloat()
            this.archeight = rr.getArcHeight().toFloat()
        }

        override fun getBounds2D(): Rectangle2D {
            return Rectangle2D.Float(x, y, width, height)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -3423150618393866922L
        }
    }

    /**
     * The `Double` class defines a rectangle with rounded
     * corners all specified in `double` coordinates.
     */
    open class Double : RoundRectangle2D, Serializable {
        /**
         * The X coordinate of this `RoundRectangle2D`.
         */
        @JvmField
        var x = 0.0

        /**
         * The Y coordinate of this `RoundRectangle2D`.
         */
        @JvmField
        var y = 0.0

        /**
         * The width of this `RoundRectangle2D`.
         */
        @JvmField
        var width = 0.0

        /**
         * The height of this `RoundRectangle2D`.
         */
        @JvmField
        var height = 0.0

        /**
         * The width of the arc that rounds off the corners.
         */
        @JvmField
        var arcwidth = 0.0

        /**
         * The height of the arc that rounds off the corners.
         */
        @JvmField
        var archeight = 0.0

        /**
         * Constructs a new `RoundRectangle2D`, initialized to
         * location (0.0,&nbsp;0), size (0.0,&nbsp;0.0), and corner arcs
         * of radius 0.0.
         */
        constructor()

        /**
         * Constructs and initializes a `RoundRectangle2D`
         * from the specified `double` coordinates.
         */
        constructor(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double, arcw: kotlin.Double, arch: kotlin.Double) {
            setRoundRect(x, y, w, h, arcw, arch)
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

        override fun getArcWidth(): kotlin.Double {
            return arcwidth
        }

        override fun getArcHeight(): kotlin.Double {
            return archeight
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0f) || (height <= 0.0f)
        }

        override fun setRoundRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double, arcWidth: kotlin.Double, arcHeight: kotlin.Double) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
            this.arcwidth = arcWidth
            this.archeight = arcHeight
        }

        override fun setRoundRect(rr: RoundRectangle2D) {
            this.x = rr.getX()
            this.y = rr.getY()
            this.width = rr.getWidth()
            this.height = rr.getHeight()
            this.arcwidth = rr.getArcWidth()
            this.archeight = rr.getArcHeight()
        }

        override fun getBounds2D(): Rectangle2D {
            return Rectangle2D.Double(x, y, width, height)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 1048939333485206117L
        }
    }

    /**
     * Gets the width of the arc that rounds off the corners.
     */
    abstract fun getArcWidth(): kotlin.Double

    /**
     * Gets the height of the arc that rounds off the corners.
     */
    abstract fun getArcHeight(): kotlin.Double

    /**
     * Sets the location, size, and corner radii of this
     * `RoundRectangle2D` to the specified
     * `double` values.
     */
    abstract fun setRoundRect(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double, arcWidth: kotlin.Double, arcHeight: kotlin.Double)

    /**
     * Sets this `RoundRectangle2D` to be the same as the
     * specified `RoundRectangle2D`.
     */
    open fun setRoundRect(rr: RoundRectangle2D) {
        setRoundRect(rr.getX(), rr.getY(), rr.getWidth(), rr.getHeight(), rr.getArcWidth(), rr.getArcHeight())
    }

    override fun setFrame(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
        setRoundRect(x, y, w, h, getArcWidth(), getArcHeight())
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        var x = x
        var y = y
        if (isEmpty()) {
            return false
        }
        var rrx0 = getX()
        var rry0 = getY()
        val rrx1 = rrx0 + getWidth()
        val rry1 = rry0 + getHeight()
        // Check for trivial rejection - point is outside bounding rectangle
        if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
            return false
        }
        val aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0
        val ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0
        // Check which corner point is in and do circular containment
        // test - otherwise simple acceptance
        rrx0 += aw
        if (x >= rrx0) {
            rrx0 = rrx1 - aw
            if (x < rrx0) {
                return true
            }
        }
        rry0 += ah
        if (y >= rry0) {
            rry0 = rry1 - ah
            if (y < rry0) {
                return true
            }
        }
        x = (x - rrx0) / aw
        y = (y - rry0) / ah
        return (x * x + y * y <= 1.0)
    }

    private fun classify(coord: kotlin.Double, left: kotlin.Double, right: kotlin.Double, arcsize: kotlin.Double): Int {
        return if (coord < left) {
            0
        } else if (coord < left + arcsize) {
            1
        } else if (coord < right - arcsize) {
            2
        } else if (coord < right) {
            3
        } else {
            4
        }
    }

    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        var x = x
        var y = y
        if (isEmpty() || w <= 0 || h <= 0) {
            return false
        }
        val rrx0 = getX()
        val rry0 = getY()
        val rrx1 = rrx0 + getWidth()
        val rry1 = rry0 + getHeight()
        // Check for trivial rejection - bounding rectangles do not intersect
        if (x + w <= rrx0 || x >= rrx1 || y + h <= rry0 || y >= rry1) {
            return false
        }
        val aw = Math.min(getWidth(), Math.abs(getArcWidth())) / 2.0
        val ah = Math.min(getHeight(), Math.abs(getArcHeight())) / 2.0
        val x0class = classify(x, rrx0, rrx1, aw)
        val x1class = classify(x + w, rrx0, rrx1, aw)
        val y0class = classify(y, rry0, rry1, ah)
        val y1class = classify(y + h, rry0, rry1, ah)
        // Trivially accept if any point is inside inner rectangle
        if (x0class == 2 || x1class == 2 || y0class == 2 || y1class == 2) {
            return true
        }
        // Trivially accept if either edge spans inner rectangle
        if ((x0class < 2 && x1class > 2) || (y0class < 2 && y1class > 2)) {
            return true
        }
        // Since neither edge spans the center, then one of the corners
        // must be in one of the rounded edges.  We detect this case if
        // a [xy]0class is 3 or a [xy]1class is 1.  One of those two cases
        // must be true for each direction.
        // We now find a "nearest point" to test for being inside a rounded
        // corner.
        x = if (x1class == 1) (x + w - (rrx0 + aw)) else (x - (rrx1 - aw))
        y = if (y1class == 1) (y + h - (rry0 + ah)) else (y - (rry1 - ah))
        x = x / aw
        y = y / ah
        return (x * x + y * y <= 1.0)
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false
        }
        return (contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h))
    }

    /**
     * Returns an iteration object that defines the boundary of this
     * `RoundRectangle2D`.
     */
    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return RoundRectIterator(this, at)
    }

    /**
     * Returns the hashcode for this `RoundRectangle2D`.
     */
    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(getX())
        bits += java.lang.Double.doubleToLongBits(getY()) * 37
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47
        bits += java.lang.Double.doubleToLongBits(getArcWidth()) * 53
        bits += java.lang.Double.doubleToLongBits(getArcHeight()) * 59
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    /**
     * Determines whether or not the specified `Object` is
     * equal to this `RoundRectangle2D`.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is RoundRectangle2D) {
            return ((getX() == other.getX()) && (getY() == other.getY())
                    && (getWidth() == other.getWidth()) && (getHeight() == other.getHeight())
                    && (getArcWidth() == other.getArcWidth()) && (getArcHeight() == other.getArcHeight()))
        }
        return false
    }
}
