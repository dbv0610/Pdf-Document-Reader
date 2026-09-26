/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Shape

/**
 * `RectangularShape` is the base class for a number of
 * [Shape] objects whose geometry is defined by a rectangular frame.
 * This class does not directly specify any specific geometry by
 * itself, but merely provides manipulation methods inherited by
 * a whole category of `Shape` objects.
 * The manipulation methods provided by this class can be used to
 * query and modify the rectangular frame, which provides a reference
 * for the subclasses to define their geometry.
 */
abstract class RectangularShape protected constructor() : Shape, Cloneable {

    /**
     * Returns the X coordinate of the upper-left corner of
     * the framing rectangle in `double` precision.
     */
    abstract fun getX(): Double

    /**
     * Returns the Y coordinate of the upper-left corner of
     * the framing rectangle in `double` precision.
     */
    abstract fun getY(): Double

    /**
     * Returns the width of the framing rectangle in
     * `double` precision.
     */
    abstract fun getWidth(): Double

    /**
     * Returns the height of the framing rectangle
     * in `double` precision.
     */
    abstract fun getHeight(): Double

    /**
     * Returns the smallest X coordinate of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getMinX(): Double {
        return getX()
    }

    /**
     * Returns the smallest Y coordinate of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getMinY(): Double {
        return getY()
    }

    /**
     * Returns the largest X coordinate of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getMaxX(): Double {
        return getX() + getWidth()
    }

    /**
     * Returns the largest Y coordinate of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getMaxY(): Double {
        return getY() + getHeight()
    }

    /**
     * Returns the X coordinate of the center of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getCenterX(): Double {
        return getX() + getWidth() / 2.0
    }

    /**
     * Returns the Y coordinate of the center of the framing
     * rectangle of the `Shape` in `double` precision.
     */
    open fun getCenterY(): Double {
        return getY() + getHeight() / 2.0
    }

    /**
     * Returns the framing [Rectangle2D]
     * that defines the overall shape of this object.
     */
    open fun getFrame(): Rectangle2D {
        return Rectangle2D.Double(getX(), getY(), getWidth(), getHeight())
    }

    /**
     * Determines whether the `RectangularShape` is empty.
     * When the `RectangularShape` is empty, it encloses no
     * area.
     */
    abstract fun isEmpty(): Boolean

    /**
     * Sets the location and size of the framing rectangle of this
     * `Shape` to the specified rectangular values.
     */
    abstract fun setFrame(x: Double, y: Double, w: Double, h: Double)

    /**
     * Sets the location and size of the framing rectangle of this
     * `Shape` to the specified [Point2D] and
     * [Dimension2D], respectively.
     */
    open fun setFrame(loc: Point2D, size: Dimension2D) {
        setFrame(loc.getX(), loc.getY(), size.getWidth(), size.getHeight())
    }

    /**
     * Sets the framing rectangle of this `Shape` to
     * be the specified `Rectangle2D`.
     */
    open fun setFrame(r: Rectangle2D) {
        setFrame(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    /**
     * Sets the diagonal of the framing rectangle of this `Shape`
     * based on the two specified coordinates.
     */
    open fun setFrameFromDiagonal(x1: Double, y1: Double, x2: Double, y2: Double) {
        var x1 = x1
        var y1 = y1
        var x2 = x2
        var y2 = y2
        if (x2 < x1) {
            val t = x1
            x1 = x2
            x2 = t
        }
        if (y2 < y1) {
            val t = y1
            y1 = y2
            y2 = t
        }
        setFrame(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * Sets the diagonal of the framing rectangle of this `Shape`
     * based on two specified `Point2D` objects.
     */
    open fun setFrameFromDiagonal(p1: Point2D, p2: Point2D) {
        setFrameFromDiagonal(p1.getX(), p1.getY(), p2.getX(), p2.getY())
    }

    /**
     * Sets the framing rectangle of this `Shape`
     * based on the specified center point coordinates and corner point
     * coordinates.
     */
    open fun setFrameFromCenter(centerX: Double, centerY: Double, cornerX: Double, cornerY: Double) {
        val halfW = Math.abs(cornerX - centerX)
        val halfH = Math.abs(cornerY - centerY)
        setFrame(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0)
    }

    /**
     * Sets the framing rectangle of this `Shape` based on a
     * specified center `Point2D` and corner `Point2D`.
     */
    open fun setFrameFromCenter(center: Point2D, corner: Point2D) {
        setFrameFromCenter(center.getX(), center.getY(), corner.getX(), corner.getY())
    }

    override fun contains(p: Point2D): Boolean {
        return contains(p.getX(), p.getY())
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun getBounds(): Rectangle {
        val width = getWidth()
        val height = getHeight()
        if (width < 0 || height < 0) {
            return Rectangle()
        }
        val x = getX()
        val y = getY()
        val x1 = Math.floor(x)
        val y1 = Math.floor(y)
        val x2 = Math.ceil(x + width)
        val y2 = Math.ceil(y + height)
        return Rectangle(x1.toInt(), y1.toInt(), (x2 - x1).toInt(), (y2 - y1).toInt())
    }

    /**
     * Returns an iterator object that iterates along the
     * `Shape` object's boundary and provides access to a
     * flattened view of the outline of the `Shape`
     * object's geometry.
     */
    override fun getPathIterator(at: AffineTransform?, flatness: Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    /**
     * Creates a new object of the same class and with the same
     * contents as this object.
     */
    public override fun clone(): Any {
        return try {
            super.clone()
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }
}
