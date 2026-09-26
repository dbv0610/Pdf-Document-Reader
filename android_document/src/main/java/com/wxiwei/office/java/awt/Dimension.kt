/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt

import com.wxiwei.office.java.awt.geom.Dimension2D

/**
 * The <code>Dimension</code> class encapsulates the width and
 * height of a component (in integer precision) in a single object.
 * <p>
 * Normally the values of <code>width</code>
 * and <code>height</code> are non-negative integers.
 * The constructors that allow you to create a dimension do
 * not prevent you from setting a negative value for these properties.
 * If the value of <code>width</code> or <code>height</code> is
 * negative, the behavior of some methods defined by other objects is
 * undefined.
 *
 * @author  Sami Shaio
 * @author  Arthur van Hoff
 * @since   1.0
 */
class Dimension(width: Int, height: Int) : Dimension2D(), java.io.Serializable {

    /**
     * The width dimension; negative values can be used.
     */
    @JvmField
    var width: Int = width

    /**
     * The height dimension; negative values can be used.
     */
    @JvmField
    var height: Int = height

    /**
     * Creates an instance of <code>Dimension</code> with a width
     * of zero and a height of zero.
     */
    constructor() : this(0, 0)

    /**
     * Creates an instance of <code>Dimension</code> whose width
     * and height are the same as for the specified dimension.
     *
     * @param    d   the specified dimension for the
     *               <code>width</code> and
     *               <code>height</code> values
     */
    constructor(d: Dimension) : this(d.width, d.height)

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    override fun getWidth(): Double {
        return width.toDouble()
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    override fun getHeight(): Double {
        return height.toDouble()
    }

    /**
     * Sets the size of this <code>Dimension</code> object to
     * the specified width and height in double precision.
     * Note that if <code>width</code> or <code>height</code>
     * are larger than <code>Integer.MAX_VALUE</code>, they will
     * be reset to <code>Integer.MAX_VALUE</code>.
     *
     * @param width  the new width for the <code>Dimension</code> object
     * @param height the new height for the <code>Dimension</code> object
     * @since 1.2
     */
    override fun setSize(width: Double, height: Double) {
        this.width = Math.ceil(width).toInt()
        this.height = Math.ceil(height).toInt()
    }

    /**
     * Gets the size of this <code>Dimension</code> object.
     * This method is included for completeness, to parallel the
     * <code>getSize</code> method defined by <code>Component</code>.
     *
     * @return   the size of this dimension, a new instance of
     *           <code>Dimension</code> with the same width and height
     * @since    1.1
     */
    fun getSize(): Dimension {
        return Dimension(width, height)
    }

    /**
     * Sets the size of this <code>Dimension</code> object to the specified size.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     * @param    d  the new size for this <code>Dimension</code> object
     * @since    1.1
     */
    fun setSize(d: Dimension) {
        setSize(d.width, d.height)
    }

    /**
     * Sets the size of this <code>Dimension</code> object
     * to the specified width and height.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     *
     * @param    width   the new width for this <code>Dimension</code> object
     * @param    height  the new height for this <code>Dimension</code> object
     * @since    1.1
     */
    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /**
     * Checks whether two dimension objects have equal values.
     */
    override fun equals(other: Any?): Boolean {
        if (other is Dimension) {
            return (width == other.width) && (height == other.height)
        }
        return false
    }

    /**
     * Returns the hash code for this <code>Dimension</code>.
     *
     * @return    a hash code for this <code>Dimension</code>
     */
    override fun hashCode(): Int {
        val sum = width + height
        return sum * (sum + 1) / 2 + width
    }

    /**
     * Returns a string representation of the values of this
     * <code>Dimension</code> object's <code>height</code> and
     * <code>width</code> fields.
     *
     * @return  a string representation of this <code>Dimension</code> object
     */
    override fun toString(): String {
        return javaClass.name + "[width=" + width + ",height=" + height + "]"
    }

    companion object {
        /*
         * JDK 1.1 serialVersionUID
         */
        private const val serialVersionUID = 4723952579491349524L
    }
}
