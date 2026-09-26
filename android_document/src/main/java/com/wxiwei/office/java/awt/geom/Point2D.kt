/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

import java.io.Serializable

/**
 * The `Point2D` class defines a point representing a location
 * in `(x,y)` coordinate space.
 *
 * This class is only the abstract superclass for all objects that
 * store a 2D coordinate.
 * The actual storage representation of the coordinates is left to
 * the subclass.
 */
abstract class Point2D protected constructor() : Cloneable {

    companion object {
        /**
         * Returns the square of the distance between two points.
         */
        @JvmStatic
        fun distanceSq(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            var x1 = x1
            var y1 = y1
            x1 -= x2
            y1 -= y2
            return (x1 * x1 + y1 * y1)
        }

        /**
         * Returns the distance between two points.
         */
        @JvmStatic
        fun distance(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double): kotlin.Double {
            var x1 = x1
            var y1 = y1
            x1 -= x2
            y1 -= y2
            return Math.sqrt(x1 * x1 + y1 * y1)
        }
    }

    /**
     * The `Float` class defines a point specified in float
     * precision.
     */
    open class Float : Point2D, Serializable {
        /**
         * The X coordinate of this `Point2D`.
         */
        @JvmField
        var x = 0f

        /**
         * The Y coordinate of this `Point2D`.
         */
        @JvmField
        var y = 0f

        /**
         * Constructs and initializes a `Point2D` with
         * coordinates (0,&nbsp;0).
         */
        constructor()

        /**
         * Constructs and initializes a `Point2D` with
         * the specified coordinates.
         */
        constructor(x: kotlin.Float, y: kotlin.Float) {
            this.x = x
            this.y = y
        }

        override fun getX(): kotlin.Double {
            return x.toDouble()
        }

        override fun getY(): kotlin.Double {
            return y.toDouble()
        }

        override fun setLocation(x: kotlin.Double, y: kotlin.Double) {
            this.x = x.toFloat()
            this.y = y.toFloat()
        }

        /**
         * Sets the location of this `Point2D` to the
         * specified `float` coordinates.
         */
        fun setLocation(x: kotlin.Float, y: kotlin.Float) {
            this.x = x
            this.y = y
        }

        /**
         * Returns a `String` that represents the value
         * of this `Point2D`.
         */
        override fun toString(): String {
            return "Point2D.Float[$x, $y]"
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = -2870572449815403710L
        }
    }

    /**
     * The `Double` class defines a point specified in
     * `double` precision.
     */
    open class Double : Point2D, Serializable {
        /**
         * The X coordinate of this `Point2D`.
         */
        @JvmField
        var x = 0.0

        /**
         * The Y coordinate of this `Point2D`.
         */
        @JvmField
        var y = 0.0

        /**
         * Constructs and initializes a `Point2D` with
         * coordinates (0,&nbsp;0).
         */
        constructor()

        /**
         * Constructs and initializes a `Point2D` with the
         * specified coordinates.
         */
        constructor(x: kotlin.Double, y: kotlin.Double) {
            this.x = x
            this.y = y
        }

        override fun getX(): kotlin.Double {
            return x
        }

        override fun getY(): kotlin.Double {
            return y
        }

        override fun setLocation(x: kotlin.Double, y: kotlin.Double) {
            this.x = x
            this.y = y
        }

        /**
         * Returns a `String` that represents the value
         * of this `Point2D`.
         */
        override fun toString(): String {
            return "Point2D.Double[$x, $y]"
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 6150783262733311327L
        }
    }

    /**
     * Returns the X coordinate of this `Point2D` in
     * `double` precision.
     */
    abstract fun getX(): kotlin.Double

    /**
     * Returns the Y coordinate of this `Point2D` in
     * `double` precision.
     */
    abstract fun getY(): kotlin.Double

    /**
     * Sets the location of this `Point2D` to the
     * specified `double` coordinates.
     */
    abstract fun setLocation(x: kotlin.Double, y: kotlin.Double)

    /**
     * Sets the location of this `Point2D` to the same
     * coordinates as the specified `Point2D` object.
     */
    open fun setLocation(p: Point2D) {
        setLocation(p.getX(), p.getY())
    }

    /**
     * Returns the square of the distance from this
     * `Point2D` to a specified point.
     */
    open fun distanceSq(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        var px = px
        var py = py
        px -= getX()
        py -= getY()
        return (px * px + py * py)
    }

    /**
     * Returns the square of the distance from this
     * `Point2D` to a specified `Point2D`.
     */
    open fun distanceSq(pt: Point2D): kotlin.Double {
        val px = pt.getX() - this.getX()
        val py = pt.getY() - this.getY()
        return (px * px + py * py)
    }

    /**
     * Returns the distance from this `Point2D` to
     * a specified point.
     */
    open fun distance(px: kotlin.Double, py: kotlin.Double): kotlin.Double {
        var px = px
        var py = py
        px -= getX()
        py -= getY()
        return Math.sqrt(px * px + py * py)
    }

    /**
     * Returns the distance from this `Point2D` to a
     * specified `Point2D`.
     */
    open fun distance(pt: Point2D): kotlin.Double {
        val px = pt.getX() - this.getX()
        val py = pt.getY() - this.getY()
        return Math.sqrt(px * px + py * py)
    }

    /**
     * Creates a new object of the same class and with the
     * same contents as this object.
     */
    public override fun clone(): Any {
        return try {
            super.clone()
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }

    /**
     * Returns the hashcode for this `Point2D`.
     */
    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(getX())
        bits = bits xor (java.lang.Double.doubleToLongBits(getY()) * 31)
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    /**
     * Determines whether or not two points are equal. Two instances of
     * `Point2D` are equal if the values of their
     * `x` and `y` member fields, representing
     * their position in the coordinate space, are the same.
     */
    override fun equals(other: Any?): Boolean {
        if (other is Point2D) {
            return (getX() == other.getX()) && (getY() == other.getY())
        }
        return super.equals(other)
    }
}
