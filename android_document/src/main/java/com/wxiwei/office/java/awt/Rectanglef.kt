package com.wxiwei.office.java.awt

/*
 * Copyright (c) 2002-2004 LWJGL Project All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. * Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of 'LWJGL' nor the names
 * of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES.
 */

/**
 * A 2D integer Rectangle class which looks remarkably like an lwjgl
 * one.
 */
open class Rectanglef {

    /** Rectangle's bounds */
    var x = 0f
    var y = 0f
    var width = 0f
    var height = 0f

    /**
     * Constructor for Rectangle.
     */
    constructor()

    /**
     * Constructor for Rectangle.
     */
    constructor(x: Float, y: Float, w: Float, h: Float) {
        this.x = x
        this.y = y
        width = w
        height = h
    }

    fun setLocation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun setSize(w: Float, h: Float) {
        width = w
        height = h
    }

    fun setBounds(x: Float, y: Float, w: Float, h: Float) {
        this.x = x
        this.y = y
        width = w
        height = h
    }

    /**
     * Translate the rectangle by an amount.
     *
     * @param newX The translation amount on the x axis
     * @param newY The translation amount on the y axis
     */
    fun translate(newX: Float, newY: Float) {
        x += newX
        y += newY
    }

    /**
     * Checks whether or not this `Rectangle` contains the
     * point at the specified location (*x*,&nbsp;*y*).
     */
    fun contains(X: Float, Y: Float): Boolean {
        var w = width
        var h = height
        if (w < 0 || h < 0) {
            // At least one of the dimensions is negative...
            return false
        }
        // Note: if either dimension is zero, tests below must return
        // false...
        if (X < x || Y < y) {
            return false
        }
        w += x
        h += y
        // overflow || intersect
        return (w < x || w > X) && (h < y || h > Y)
    }

    /**
     * Checks whether this `Rectangle` entirely contains the
     * `Rectangle` at the specified location
     * (*X*,&nbsp;*Y*) with the specified dimensions
     * (*W*,&nbsp;*H*).
     */
    fun contains(X: Float, Y: Float, W: Float, H: Float): Boolean {
        var w = width
        var h = height
        var W = W
        var H = H
        if (w < 0 || W < 0 || h < 0 || H < 0) {
            // At least one of the dimensions is negative...
            return false
        }
        // Note: if any dimension is zero, tests below must return
        // false...
        if (X < x || Y < y) {
            return false
        }
        w += x
        W += X
        if (W <= X) {
            // X+W overflowed or W was zero, return false if...
            // either original w or W was zero or
            // x+w did not overflow or
            // the overflowed x+w is smaller than the overflowed X+W
            if (w >= x || W > w) {
                return false
            }
        } else {
            // X+W did not overflow and W was not zero, return false
            // if...
            // original w was zero or
            // x+w did not overflow and x+w is smaller than X+W
            if (w >= x && W > w) {
                return false
            }
        }
        h += y
        H += Y
        if (H <= Y) {
            if (h >= y || H > h) {
                return false
            }
        } else {
            if (h >= y && H > h) {
                return false
            }
        }
        return true
    }

    /**
     * Adds a point, specified by the integer arguments
     * `newx` and `newy`, to this `Rectangle`.
     */
    fun add(newx: Float, newy: Float) {
        val x1 = Math.min(x, newx)
        val x2 = Math.max(x + width, newx)
        val y1 = Math.min(y, newy)
        val y2 = Math.max(y + height, newy)
        x = x1
        y = y1
        width = x2 - x1
        height = y2 - y1
    }

    /**
     * Resizes the `Rectangle` both horizontally and
     * vertically.
     */
    fun grow(h: Float, v: Float) {
        x -= h
        y -= v
        width += h * 2
        height += v * 2
    }

    /**
     * Determines whether or not this `Rectangle` is empty.
     * A `Rectangle` is empty if its width or its height is
     * less than or equal to zero.
     */
    fun isEmpty(): Boolean {
        return width <= 0 || height <= 0
    }

    /**
     * Checks whether two rectangles are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (other is Rectanglef) {
            return x == other.x && y == other.y && width == other.width && height == other.height
        }
        return super.equals(other)
    }

    /**
     * Debugging
     */
    override fun toString(): String {
        return javaClass.name + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]"
    }
}
