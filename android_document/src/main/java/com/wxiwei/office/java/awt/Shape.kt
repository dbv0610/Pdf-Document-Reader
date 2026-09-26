/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt

import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.PathIterator
import com.wxiwei.office.java.awt.geom.Point2D
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * The <code>Shape</code> interface provides definitions for objects
 * that represent some form of geometric shape.  The <code>Shape</code>
 * is described by a [PathIterator] object, which can express the
 * outline of the <code>Shape</code> as well as a rule for determining
 * how the outline divides the 2D plane into interior and exterior
 * points.
 *
 * @author Jim Graham
 * @since 1.2
 */
interface Shape {
    /**
     * Returns an integer [Rectangle] that completely encloses the
     * <code>Shape</code>.
     * @return an integer <code>Rectangle</code> that completely encloses
     *                 the <code>Shape</code>.
     * @since 1.2
     */
    fun getBounds(): Rectangle

    /**
     * Returns a high precision and more accurate bounding box of
     * the <code>Shape</code> than the <code>getBounds</code> method.
     * @return an instance of <code>Rectangle2D</code> that is a
     *                 high-precision bounding box of the <code>Shape</code>.
     * @since 1.2
     */
    fun getBounds2D(): Rectangle2D

    /**
     * Tests if the specified coordinates are inside the boundary of the
     * <code>Shape</code>.
     * @since 1.2
     */
    fun contains(x: Double, y: Double): Boolean

    /**
     * Tests if a specified [Point2D] is inside the boundary
     * of the <code>Shape</code>.
     * @since 1.2
     */
    fun contains(p: Point2D): Boolean

    /**
     * Tests if the interior of the <code>Shape</code> intersects the
     * interior of a specified rectangular area.
     * @since 1.2
     */
    fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean

    /**
     * Tests if the interior of the <code>Shape</code> intersects the
     * interior of a specified <code>Rectangle2D</code>.
     * @since 1.2
     */
    fun intersects(r: Rectangle2D): Boolean

    /**
     * Tests if the interior of the <code>Shape</code> entirely contains
     * the specified rectangular area.
     * @since 1.2
     */
    fun contains(x: Double, y: Double, w: Double, h: Double): Boolean

    /**
     * Tests if the interior of the <code>Shape</code> entirely contains the
     * specified <code>Rectangle2D</code>.
     * @since 1.2
     */
    fun contains(r: Rectangle2D): Boolean

    /**
     * Returns an iterator object that iterates along the
     * <code>Shape</code> boundary and provides access to the geometry of the
     * <code>Shape</code> outline.
     * @since 1.2
     */
    fun getPathIterator(at: AffineTransform?): PathIterator

    /**
     * Returns an iterator object that iterates along the <code>Shape</code>
     * boundary and provides access to a flattened view of the
     * <code>Shape</code> outline geometry.
     * @since 1.2
     */
    fun getPathIterator(at: AffineTransform?, flatness: Double): PathIterator
}
