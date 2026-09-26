/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

import java.io.Serializable

/**
 * The `Ellipse2D` class describes an ellipse that is defined
 * by a framing rectangle.
 *
 * This class is only the abstract superclass for all objects which
 * store a 2D ellipse.
 * The actual storage representation of the coordinates is left to
 * the subclass.
 */
abstract class Ellipse2D protected constructor() : RectangularShape() {

    /**
     * The `Float` class defines an ellipse specified
     * in `float` precision.
     */
    open class Float : Ellipse2D, Serializable {
        /**
         * The X coordinate of the upper-left corner of the
         * framing rectangle of this `Ellipse2D`.
         */
        @JvmField
        var x = 0f

        /**
         * The Y coordinate of the upper-left corner of the
         * framing rectangle of this `Ellipse2D`.
         */
        @JvmField
        var y = 0f

        /**
         * The overall width of this `Ellipse2D`.
         */
        @JvmField
        var width = 0f

        /**
         * The overall height of this `Ellipse2D`.
         */
        @JvmField
        var height = 0f

        /**
         * Constructs a new `Ellipse2D`, initialized to
         * location (0,&nbsp;0) and size (0,&nbsp;0).
         */
        constructor()

        /**
         * Constructs and initializes an `Ellipse2D` from the
         * specified coordinates.
         */
        constructor(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float) {
            setFrame(x, y, w, h)
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
            return (width <= 0.0 || height <= 0.0)
        }

        /**
         * Sets the location and size of the framing rectangle of this
         * `Shape` to the specified rectangular values.
         */
        fun setFrame(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
        }

        override fun setFrame(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
            this.width = w.toFloat()
            this.height = h.toFloat()
        }

        override fun getBounds2D(): Rectangle2D {
            return Rectangle2D.Float(x, y, width, height)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -6633761252372475977L
        }
    }

    /**
     * The `Double` class defines an ellipse specified
     * in `double` precision.
     */
    open class Double : Ellipse2D, Serializable {
        /**
         * The X coordinate of the upper-left corner of the
         * framing rectangle of this `Ellipse2D`.
         */
        @JvmField
        var x = 0.0

        /**
         * The Y coordinate of the upper-left corner of the
         * framing rectangle of this `Ellipse2D`.
         */
        @JvmField
        var y = 0.0

        /**
         * The overall width of this `Ellipse2D`.
         */
        @JvmField
        var width = 0.0

        /**
         * The overall height of the `Ellipse2D`.
         */
        @JvmField
        var height = 0.0

        /**
         * Constructs a new `Ellipse2D`, initialized to
         * location (0,&nbsp;0) and size (0,&nbsp;0).
         */
        constructor()

        /**
         * Constructs and initializes an `Ellipse2D` from the
         * specified coordinates.
         */
        constructor(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            setFrame(x, y, w, h)
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
            return (width <= 0.0 || height <= 0.0)
        }

        override fun setFrame(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
        }

        override fun getBounds2D(): Rectangle2D {
            return Rectangle2D.Double(x, y, width, height)
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 5555464816372320683L
        }
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        // Normalize the coordinates compared to the ellipse
        // having a center at 0,0 and a radius of 0.5.
        val ellw = getWidth()
        if (ellw <= 0.0) {
            return false
        }
        val normx = (x - getX()) / ellw - 0.5
        val ellh = getHeight()
        if (ellh <= 0.0) {
            return false
        }
        val normy = (y - getY()) / ellh - 0.5
        return (normx * normx + normy * normy) < 0.25
    }

    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (w <= 0.0 || h <= 0.0) {
            return false
        }
        // Normalize the rectangular coordinates compared to the ellipse
        // having a center at 0,0 and a radius of 0.5.
        val ellw = getWidth()
        if (ellw <= 0.0) {
            return false
        }
        val normx0 = (x - getX()) / ellw - 0.5
        val normx1 = normx0 + w / ellw
        val ellh = getHeight()
        if (ellh <= 0.0) {
            return false
        }
        val normy0 = (y - getY()) / ellh - 0.5
        val normy1 = normy0 + h / ellh
        // find nearest x (left edge, right edge, 0.0)
        // find nearest y (top edge, bottom edge, 0.0)
        // if nearest x,y is inside circle of radius 0.5, then intersects
        val nearx: kotlin.Double
        val neary: kotlin.Double
        if (normx0 > 0.0) {
            // center to left of X extents
            nearx = normx0
        } else if (normx1 < 0.0) {
            // center to right of X extents
            nearx = normx1
        } else {
            nearx = 0.0
        }
        if (normy0 > 0.0) {
            // center above Y extents
            neary = normy0
        } else if (normy1 < 0.0) {
            // center below Y extents
            neary = normy1
        } else {
            neary = 0.0
        }
        return (nearx * nearx + neary * neary) < 0.25
    }

    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        return (contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h))
    }

    /**
     * Returns an iteration object that defines the boundary of this
     * `Ellipse2D`.
     */
    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return EllipseIterator(this, at)
    }

    /**
     * Returns the hashcode for this `Ellipse2D`.
     */
    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(getX())
        bits += java.lang.Double.doubleToLongBits(getY()) * 37
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    /**
     * Determines whether or not the specified `Object` is
     * equal to this `Ellipse2D`.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Ellipse2D) {
            return ((getX() == other.getX()) && (getY() == other.getY())
                    && (getWidth() == other.getWidth()) && (getHeight() == other.getHeight()))
        }
        return false
    }
}
