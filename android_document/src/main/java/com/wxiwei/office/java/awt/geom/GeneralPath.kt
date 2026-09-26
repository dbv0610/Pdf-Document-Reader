/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Shape

/**
 * The `GeneralPath` class represents a geometric path
 * constructed from straight lines, and quadratic and cubic
 * (B&eacute;zier) curves.  It can contain multiple subpaths.
 *
 * `GeneralPath` is a legacy final class which exactly
 * implements the behavior of its superclass [Path2D.Float].
 * Together with [Path2D.Double], the [Path2D] classes
 * provide full implementations of a general geometric path that
 * support all of the functionality of the [Shape] and
 * [PathIterator] interfaces with the ability to explicitly
 * select different levels of internal coordinate precision.
 */
class GeneralPath : Path2D.Float {

    /**
     * Constructs a new empty single precision `GeneralPath` object
     * with a default winding rule of [WIND_NON_ZERO].
     */
    constructor() : super(WIND_NON_ZERO, INIT_SIZE)

    /**
     * Constructs a new `GeneralPath` object with the specified
     * winding rule to control operations that require the
     * interior of the path to be defined.
     */
    constructor(rule: Int) : super(rule, INIT_SIZE)

    /**
     * Constructs a new `GeneralPath` object with the specified
     * winding rule and the specified initial capacity to store
     * path coordinates.
     */
    constructor(rule: Int, initialCapacity: Int) : super(rule, initialCapacity)

    /**
     * Constructs a new `GeneralPath` object from an arbitrary
     * [Shape] object.
     */
    constructor(s: Shape) : super(s, null)

    internal constructor(windingRule: Int, pointTypes: ByteArray, numTypes: Int,
                         pointCoords: FloatArray, numCoords: Int) : super() {
        // used to construct from native
        this.windingRule = windingRule
        this.pointTypes = pointTypes
        this.numTypes = numTypes
        this.floatCoords = pointCoords
        this.numCoords = numCoords
    }

    companion object {
        /*
         * JDK 1.6 serialVersionUID
         */
        private const val serialVersionUID = -8327096662768731142L
    }
}
