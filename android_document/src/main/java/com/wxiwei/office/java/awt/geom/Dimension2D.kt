/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

/**
 * The <code>Dimension2D</code> class is to encapsulate a width
 * and a height dimension.
 * <p>
 * This class is only the abstract superclass for all objects that
 * store a 2D dimension.
 * The actual storage representation of the sizes is left to
 * the subclass.
 *
 * @author  Jim Graham
 * @since 1.2
 */
abstract class Dimension2D
/**
 * This is an abstract class that cannot be instantiated directly.
 * Type-specific implementation subclasses are available for
 * instantiation and provide a number of formats for storing
 * the information necessary to satisfy the various accessor
 * methods below.
 *
 * @see com.wxiwei.office.java.awt.Dimension
 * @since 1.2
 */
protected constructor() : Cloneable {

    /**
     * Returns the width of this <code>Dimension</code> in double
     * precision.
     * @return the width of this <code>Dimension</code>.
     * @since 1.2
     */
    abstract fun getWidth(): Double

    /**
     * Returns the height of this <code>Dimension</code> in double
     * precision.
     * @return the height of this <code>Dimension</code>.
     * @since 1.2
     */
    abstract fun getHeight(): Double

    /**
     * Sets the size of this <code>Dimension</code> object to the
     * specified width and height.
     *
     * @param width  the new width for the <code>Dimension</code> object
     * @param height  the new height for the <code>Dimension</code> object
     * @since 1.2
     */
    abstract fun setSize(width: Double, height: Double)

    /**
     * Sets the size of this <code>Dimension2D</code> object to
     * match the specified size.
     *
     * @param d  the new size for the <code>Dimension2D</code> object
     * @since 1.2
     */
    fun setSize(d: Dimension2D) {
        setSize(d.getWidth(), d.getHeight())
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        Cloneable
     * @since      1.2
     */
    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }
}
