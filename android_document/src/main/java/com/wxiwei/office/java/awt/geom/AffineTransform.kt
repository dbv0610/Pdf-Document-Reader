/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Shape

/**
 * The `AffineTransform` class represents a 2D affine transform
 * that performs a linear mapping from 2D coordinates to other 2D
 * coordinates that preserves the "straightness" and
 * "parallelness" of lines.  Affine transformations can be constructed
 * using sequences of translations, scales, flips, rotations, and shears.
 */
open class AffineTransform : Cloneable, java.io.Serializable {

    /**
     * The X coordinate scaling element of the 3x3
     * affine transformation matrix.
     */
    @JvmField
    internal var m00 = 0.0

    /**
     * The Y coordinate shearing element of the 3x3
     * affine transformation matrix.
     */
    @JvmField
    internal var m10 = 0.0

    /**
     * The X coordinate shearing element of the 3x3
     * affine transformation matrix.
     */
    @JvmField
    internal var m01 = 0.0

    /**
     * The Y coordinate scaling element of the 3x3
     * affine transformation matrix.
     */
    @JvmField
    internal var m11 = 0.0

    /**
     * The X coordinate of the translation element of the
     * 3x3 affine transformation matrix.
     */
    @JvmField
    internal var m02 = 0.0

    /**
     * The Y coordinate of the translation element of the
     * 3x3 affine transformation matrix.
     */
    @JvmField
    internal var m12 = 0.0

    /**
     * This field keeps track of which components of the matrix need to
     * be applied when performing a transformation.
     */
    @Transient
    @JvmField
    internal var state = 0

    /**
     * This field caches the current transformation type of the matrix.
     */
    @Transient
    private var type = 0

    private constructor(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double,
                        state: Int) {
        this.m00 = m00
        this.m10 = m10
        this.m01 = m01
        this.m11 = m11
        this.m02 = m02
        this.m12 = m12
        this.state = state
        this.type = TYPE_UNKNOWN
    }

    /**
     * Constructs a new `AffineTransform` representing the
     * Identity transformation.
     */
    constructor() {
        m11 = 1.0
        m00 = m11
        // m01 = m10 = m02 = m12 = 0.0;         /* Not needed. */
        // state = APPLY_IDENTITY;              /* Not needed. */
        // type = TYPE_IDENTITY;                /* Not needed. */
    }

    /**
     * Constructs a new `AffineTransform` that is a copy of
     * the specified `AffineTransform` object.
     */
    constructor(Tx: AffineTransform) {
        this.m00 = Tx.m00
        this.m10 = Tx.m10
        this.m01 = Tx.m01
        this.m11 = Tx.m11
        this.m02 = Tx.m02
        this.m12 = Tx.m12
        this.state = Tx.state
        this.type = Tx.type
    }

    /**
     * Constructs a new `AffineTransform` from 6 floating point
     * values representing the 6 specifiable entries of the 3x3
     * transformation matrix.
     */
    constructor(m00: Float, m10: Float, m01: Float, m11: Float, m02: Float, m12: Float) {
        this.m00 = m00.toDouble()
        this.m10 = m10.toDouble()
        this.m01 = m01.toDouble()
        this.m11 = m11.toDouble()
        this.m02 = m02.toDouble()
        this.m12 = m12.toDouble()
        updateState()
    }

    /**
     * Constructs a new `AffineTransform` from an array of
     * floating point values representing either the 4 non-translation
     * enries or the 6 specifiable entries of the 3x3 transformation
     * matrix.
     */
    constructor(flatmatrix: FloatArray) {
        m00 = flatmatrix[0].toDouble()
        m10 = flatmatrix[1].toDouble()
        m01 = flatmatrix[2].toDouble()
        m11 = flatmatrix[3].toDouble()
        if (flatmatrix.size > 5) {
            m02 = flatmatrix[4].toDouble()
            m12 = flatmatrix[5].toDouble()
        }
        updateState()
    }

    /**
     * Constructs a new `AffineTransform` from 6 double
     * precision values representing the 6 specifiable entries of the 3x3
     * transformation matrix.
     */
    constructor(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double) {
        this.m00 = m00
        this.m10 = m10
        this.m01 = m01
        this.m11 = m11
        this.m02 = m02
        this.m12 = m12
        updateState()
    }

    /**
     * Constructs a new `AffineTransform` from an array of
     * double precision values representing either the 4 non-translation
     * entries or the 6 specifiable entries of the 3x3 transformation
     * matrix.
     */
    constructor(flatmatrix: DoubleArray) {
        m00 = flatmatrix[0]
        m10 = flatmatrix[1]
        m01 = flatmatrix[2]
        m11 = flatmatrix[3]
        if (flatmatrix.size > 5) {
            m02 = flatmatrix[4]
            m12 = flatmatrix[5]
        }
        updateState()
    }

    /**
     * Retrieves the flag bits describing the conversion properties of
     * this transform.
     */
    open fun getType(): Int {
        if (type == TYPE_UNKNOWN) {
            calculateType()
        }
        return type
    }

    /**
     * This is the utility function to calculate the flag bits when
     * they have not been cached.
     */
    private fun calculateType() {
        var ret = TYPE_IDENTITY
        val sgn0: Boolean
        val sgn1: Boolean
        val M0: Double
        val M1: Double
        val M2: Double
        val M3: Double
        updateState()
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                if (state == (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)) {
                    ret = TYPE_TRANSLATION
                    /* NOBREAK */
                }
                M0 = m00
                M2 = m01
                M3 = m10
                M1 = m11
                if (M0 * M2 + M3 * M1 != 0.0) {
                    // Transformed unit vectors are not perpendicular...
                    this.type = TYPE_GENERAL_TRANSFORM
                    return
                }
                sgn0 = (M0 >= 0.0)
                sgn1 = (M1 >= 0.0)
                if (sgn0 == sgn1) {
                    // sgn(M0) == sgn(M1) therefore sgn(M2) == -sgn(M3)
                    // This is the "unflipped" (right-handed) state
                    if (M0 != M1 || M2 != -M3) {
                        ret = ret or (TYPE_GENERAL_ROTATION or TYPE_GENERAL_SCALE)
                    } else if (M0 * M1 - M2 * M3 != 1.0) {
                        ret = ret or (TYPE_GENERAL_ROTATION or TYPE_UNIFORM_SCALE)
                    } else {
                        ret = ret or TYPE_GENERAL_ROTATION
                    }
                } else {
                    // sgn(M0) == -sgn(M1) therefore sgn(M2) == sgn(M3)
                    // This is the "flipped" (left-handed) state
                    if (M0 != -M1 || M2 != M3) {
                        ret = ret or (TYPE_GENERAL_ROTATION or TYPE_FLIP or TYPE_GENERAL_SCALE)
                    } else if (M0 * M1 - M2 * M3 != 1.0) {
                        ret = ret or (TYPE_GENERAL_ROTATION or TYPE_FLIP or TYPE_UNIFORM_SCALE)
                    } else {
                        ret = ret or (TYPE_GENERAL_ROTATION or TYPE_FLIP)
                    }
                }
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                if (state == (APPLY_SHEAR or APPLY_TRANSLATE)) {
                    ret = TYPE_TRANSLATION
                    /* NOBREAK */
                }
                M0 = m01
                M1 = m10
                sgn0 = (M0 >= 0.0)
                sgn1 = (M1 >= 0.0)
                if (sgn0 != sgn1) {
                    // Different signs - simple 90 degree rotation
                    if (M0 != -M1) {
                        ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_GENERAL_SCALE)
                    } else if (M0 != 1.0 && M0 != -1.0) {
                        ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_UNIFORM_SCALE)
                    } else {
                        ret = ret or TYPE_QUADRANT_ROTATION
                    }
                } else {
                    // Same signs - 90 degree rotation plus an axis flip too
                    if (M0 == M1) {
                        ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_FLIP or TYPE_UNIFORM_SCALE)
                    } else {
                        ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_FLIP or TYPE_GENERAL_SCALE)
                    }
                }
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                if (state == (APPLY_SCALE or APPLY_TRANSLATE)) {
                    ret = TYPE_TRANSLATION
                    /* NOBREAK */
                }
                M0 = m00
                M1 = m11
                sgn0 = (M0 >= 0.0)
                sgn1 = (M1 >= 0.0)
                if (sgn0 == sgn1) {
                    if (sgn0) {
                        // Both scaling factors non-negative - simple scale
                        // Note: APPLY_SCALE implies M0, M1 are not both 1
                        if (M0 == M1) {
                            ret = ret or TYPE_UNIFORM_SCALE
                        } else {
                            ret = ret or TYPE_GENERAL_SCALE
                        }
                    } else {
                        // Both scaling factors negative - 180 degree rotation
                        if (M0 != M1) {
                            ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_GENERAL_SCALE)
                        } else if (M0 != -1.0) {
                            ret = ret or (TYPE_QUADRANT_ROTATION or TYPE_UNIFORM_SCALE)
                        } else {
                            ret = ret or TYPE_QUADRANT_ROTATION
                        }
                    }
                } else {
                    // Scaling factor signs different - flip about some axis
                    if (M0 == -M1) {
                        if (M0 == 1.0 || M0 == -1.0) {
                            ret = ret or TYPE_FLIP
                        } else {
                            ret = ret or (TYPE_FLIP or TYPE_UNIFORM_SCALE)
                        }
                    } else {
                        ret = ret or (TYPE_FLIP or TYPE_GENERAL_SCALE)
                    }
                }
            }
            (APPLY_TRANSLATE) -> ret = TYPE_TRANSLATION
            (APPLY_IDENTITY) -> {
            }
            else -> stateError()
        }
        this.type = ret
    }

    /**
     * Returns the determinant of the matrix representation of the transform.
     */
    open fun getDeterminant(): Double {
        return when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> m00 * m11 - m01 * m10
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> -(m01 * m10)
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> m00 * m11
            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> 1.0
            else -> stateError()
        }
    }

    /**
     * Manually recalculates the state of the transform when the matrix
     * changes too much to predict the effects on the state.
     */
    internal fun updateState() {
        if (m01 == 0.0 && m10 == 0.0) {
            if (m00 == 1.0 && m11 == 1.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_IDENTITY
                    type = TYPE_IDENTITY
                } else {
                    state = APPLY_TRANSLATE
                    type = TYPE_TRANSLATION
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE
                    type = TYPE_UNKNOWN
                } else {
                    state = (APPLY_SCALE or APPLY_TRANSLATE)
                    type = TYPE_UNKNOWN
                }
            }
        } else {
            if (m00 == 0.0 && m11 == 0.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    type = TYPE_UNKNOWN
                } else {
                    state = (APPLY_SHEAR or APPLY_TRANSLATE)
                    type = TYPE_UNKNOWN
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = (APPLY_SHEAR or APPLY_SCALE)
                    type = TYPE_UNKNOWN
                } else {
                    state = (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)
                    type = TYPE_UNKNOWN
                }
            }
        }
    }

    /*
     * Convenience method used internally to throw exceptions when
     * a case was forgotten in a switch statement.
     */
    private fun stateError(): Nothing {
        throw InternalError("missing case in transform state switch")
    }

    /**
     * Retrieves the 6 specifiable values in the 3x3 affine transformation
     * matrix and places them into an array of double precisions values.
     */
    open fun getMatrix(flatmatrix: DoubleArray) {
        flatmatrix[0] = m00
        flatmatrix[1] = m10
        flatmatrix[2] = m01
        flatmatrix[3] = m11
        if (flatmatrix.size > 5) {
            flatmatrix[4] = m02
            flatmatrix[5] = m12
        }
    }

    /**
     * Returns the X coordinate scaling element (m00) of the 3x3
     * affine transformation matrix.
     */
    open fun getScaleX(): Double {
        return m00
    }

    /**
     * Returns the Y coordinate scaling element (m11) of the 3x3
     * affine transformation matrix.
     */
    open fun getScaleY(): Double {
        return m11
    }

    /**
     * Returns the X coordinate shearing element (m01) of the 3x3
     * affine transformation matrix.
     */
    open fun getShearX(): Double {
        return m01
    }

    /**
     * Returns the Y coordinate shearing element (m10) of the 3x3
     * affine transformation matrix.
     */
    open fun getShearY(): Double {
        return m10
    }

    /**
     * Returns the X coordinate of the translation element (m02) of the
     * 3x3 affine transformation matrix.
     */
    open fun getTranslateX(): Double {
        return m02
    }

    /**
     * Returns the Y coordinate of the translation element (m12) of the
     * 3x3 affine transformation matrix.
     */
    open fun getTranslateY(): Double {
        return m12
    }

    /**
     * Concatenates this transform with a translation transformation.
     */
    open fun translate(tx: Double, ty: Double) {
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                m02 = tx * m00 + ty * m01 + m02
                m12 = tx * m10 + ty * m11 + m12
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR or APPLY_SCALE
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                m02 = tx * m00 + ty * m01
                m12 = tx * m10 + ty * m11
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                m02 = ty * m01 + m02
                m12 = tx * m10 + m12
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            (APPLY_SHEAR) -> {
                m02 = ty * m01
                m12 = tx * m10
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SHEAR or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                m02 = tx * m00 + m02
                m12 = ty * m11 + m12
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            (APPLY_SCALE) -> {
                m02 = tx * m00
                m12 = ty * m11
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SCALE or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                m02 = tx + m02
                m12 = ty + m12
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_IDENTITY
                    type = TYPE_IDENTITY
                }
                return
            }
            (APPLY_IDENTITY) -> {
                m02 = tx
                m12 = ty
                if (tx != 0.0 || ty != 0.0) {
                    state = APPLY_TRANSLATE
                    type = TYPE_TRANSLATION
                }
                return
            }
            else -> stateError()
        }
    }

    private fun rotate90() {
        var M0 = m00
        m00 = m01
        m01 = -M0
        M0 = m10
        m10 = m11
        m11 = -M0
        var state = rot90conversion[this.state]
        if ((state and (APPLY_SHEAR or APPLY_SCALE)) == APPLY_SCALE && m00 == 1.0 && m11 == 1.0) {
            state -= APPLY_SCALE
        }
        this.state = state
        type = TYPE_UNKNOWN
    }

    private fun rotate180() {
        m00 = -m00
        m11 = -m11
        val state = this.state
        if ((state and (APPLY_SHEAR)) != 0) {
            // If there was a shear, then this rotation has no
            // effect on the state.
            m01 = -m01
            m10 = -m10
        } else {
            // No shear means the SCALE state may toggle when
            // m00 and m11 are negated.
            if (m00 == 1.0 && m11 == 1.0) {
                this.state = state and APPLY_SCALE.inv()
            } else {
                this.state = state or APPLY_SCALE
            }
        }
        type = TYPE_UNKNOWN
    }

    private fun rotate270() {
        var M0 = m00
        m00 = -m01
        m01 = M0
        M0 = m10
        m10 = -m11
        m11 = M0
        var state = rot90conversion[this.state]
        if ((state and (APPLY_SHEAR or APPLY_SCALE)) == APPLY_SCALE && m00 == 1.0 && m11 == 1.0) {
            state -= APPLY_SCALE
        }
        this.state = state
        type = TYPE_UNKNOWN
    }

    /**
     * Concatenates this transform with a rotation transformation.
     */
    open fun rotate(theta: Double) {
        val sin = Math.sin(theta)
        if (sin == 1.0) {
            rotate90()
        } else if (sin == -1.0) {
            rotate270()
        } else {
            val cos = Math.cos(theta)
            if (cos == -1.0) {
                rotate180()
            } else if (cos != 1.0) {
                var M0: Double
                var M1: Double
                M0 = m00
                M1 = m01
                m00 = cos * M0 + sin * M1
                m01 = -sin * M0 + cos * M1
                M0 = m10
                M1 = m11
                m10 = cos * M0 + sin * M1
                m11 = -sin * M0 + cos * M1
                updateState()
            }
        }
    }

    /**
     * Concatenates this transform with a transform that rotates
     * coordinates around an anchor point.
     */
    open fun rotate(theta: Double, anchorx: Double, anchory: Double) {
        // REMIND: Simple for now - optimize later
        translate(anchorx, anchory)
        rotate(theta)
        translate(-anchorx, -anchory)
    }

    /**
     * Concatenates this transform with a transform that rotates
     * coordinates according to a rotation vector.
     */
    open fun rotate(vecx: Double, vecy: Double) {
        if (vecy == 0.0) {
            if (vecx < 0.0) {
                rotate180()
            }
            // If vecx > 0.0 - no rotation
            // If vecx == 0.0 - undefined rotation - treat as no rotation
        } else if (vecx == 0.0) {
            if (vecy > 0.0) {
                rotate90()
            } else { // vecy must be < 0.0
                rotate270()
            }
        } else {
            val len = Math.sqrt(vecx * vecx + vecy * vecy)
            val sin = vecy / len
            val cos = vecx / len
            var M0: Double
            var M1: Double
            M0 = m00
            M1 = m01
            m00 = cos * M0 + sin * M1
            m01 = -sin * M0 + cos * M1
            M0 = m10
            M1 = m11
            m10 = cos * M0 + sin * M1
            m11 = -sin * M0 + cos * M1
            updateState()
        }
    }

    /**
     * Concatenates this transform with a transform that rotates
     * coordinates around an anchor point according to a rotation
     * vector.
     */
    open fun rotate(vecx: Double, vecy: Double, anchorx: Double, anchory: Double) {
        // REMIND: Simple for now - optimize later
        translate(anchorx, anchory)
        rotate(vecx, vecy)
        translate(-anchorx, -anchory)
    }

    /**
     * Concatenates this transform with a transform that rotates
     * coordinates by the specified number of quadrants.
     */
    open fun quadrantRotate(numquadrants: Int) {
        when (numquadrants and 3) {
            0 -> {
            }
            1 -> rotate90()
            2 -> rotate180()
            3 -> rotate270()
        }
    }

    /**
     * Concatenates this transform with a transform that rotates
     * coordinates by the specified number of quadrants around
     * the specified anchor point.
     */
    open fun quadrantRotate(numquadrants: Int, anchorx: Double, anchory: Double) {
        when (numquadrants and 3) {
            0 -> return
            1 -> {
                m02 += anchorx * (m00 - m01) + anchory * (m01 + m00)
                m12 += anchorx * (m10 - m11) + anchory * (m11 + m10)
                rotate90()
            }
            2 -> {
                m02 += anchorx * (m00 + m00) + anchory * (m01 + m01)
                m12 += anchorx * (m10 + m10) + anchory * (m11 + m11)
                rotate180()
            }
            3 -> {
                m02 += anchorx * (m00 + m01) + anchory * (m01 - m00)
                m12 += anchorx * (m10 + m11) + anchory * (m11 - m10)
                rotate270()
            }
        }
        if (m02 == 0.0 && m12 == 0.0) {
            state = state and APPLY_TRANSLATE.inv()
        } else {
            state = state or APPLY_TRANSLATE
        }
    }

    /**
     * Concatenates this transform with a scaling transformation.
     */
    open fun scale(sx: Double, sy: Double) {
        var state = this.state
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE),
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                if (state == (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)
                    || state == (APPLY_SHEAR or APPLY_SCALE)) {
                    m00 *= sx
                    m11 *= sy
                    /* NOBREAK */
                }
                m01 *= sy
                m10 *= sx
                if (m01 == 0.0 && m10 == 0.0) {
                    state = state and APPLY_TRANSLATE
                    if (m00 == 1.0 && m11 == 1.0) {
                        this.type = if (state == APPLY_IDENTITY) TYPE_IDENTITY else TYPE_TRANSLATION
                    } else {
                        state = state or APPLY_SCALE
                        this.type = TYPE_UNKNOWN
                    }
                    this.state = state
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                m00 *= sx
                m11 *= sy
                if (m00 == 1.0 && m11 == 1.0) {
                    state = state and APPLY_TRANSLATE
                    this.state = state
                    this.type = if (state == APPLY_IDENTITY) TYPE_IDENTITY else TYPE_TRANSLATION
                } else {
                    this.type = TYPE_UNKNOWN
                }
                return
            }
            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> {
                m00 = sx
                m11 = sy
                if (sx != 1.0 || sy != 1.0) {
                    this.state = state or APPLY_SCALE
                    this.type = TYPE_UNKNOWN
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Concatenates this transform with a shearing transformation.
     */
    open fun shear(shx: Double, shy: Double) {
        val state = this.state
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                var M0: Double
                var M1: Double
                M0 = m00
                M1 = m01
                m00 = M0 + M1 * shy
                m01 = M0 * shx + M1
                M0 = m10
                M1 = m11
                m10 = M0 + M1 * shy
                m11 = M0 * shx + M1
                updateState()
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                m00 = m01 * shy
                m11 = m10 * shx
                if (m00 != 0.0 || m11 != 0.0) {
                    this.state = state or APPLY_SCALE
                }
                this.type = TYPE_UNKNOWN
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                m01 = m00 * shx
                m10 = m11 * shy
                if (m01 != 0.0 || m10 != 0.0) {
                    this.state = state or APPLY_SHEAR
                }
                this.type = TYPE_UNKNOWN
                return
            }
            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> {
                m01 = shx
                m10 = shy
                if (m01 != 0.0 || m10 != 0.0) {
                    this.state = state or APPLY_SCALE or APPLY_SHEAR
                    this.type = TYPE_UNKNOWN
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Resets this transform to the Identity transform.
     */
    open fun setToIdentity() {
        m11 = 1.0
        m00 = m11
        m12 = 0.0
        m02 = m12
        m01 = m02
        m10 = m01
        state = APPLY_IDENTITY
        type = TYPE_IDENTITY
    }

    /**
     * Sets this transform to a translation transformation.
     */
    open fun setToTranslation(tx: Double, ty: Double) {
        m00 = 1.0
        m10 = 0.0
        m01 = 0.0
        m11 = 1.0
        m02 = tx
        m12 = ty
        if (tx != 0.0 || ty != 0.0) {
            state = APPLY_TRANSLATE
            type = TYPE_TRANSLATION
        } else {
            state = APPLY_IDENTITY
            type = TYPE_IDENTITY
        }
    }

    /**
     * Sets this transform to a rotation transformation.
     */
    open fun setToRotation(theta: Double) {
        var sin = Math.sin(theta)
        val cos: Double
        if (sin == 1.0 || sin == -1.0) {
            cos = 0.0
            state = APPLY_SHEAR
            type = TYPE_QUADRANT_ROTATION
        } else {
            cos = Math.cos(theta)
            if (cos == -1.0) {
                sin = 0.0
                state = APPLY_SCALE
                type = TYPE_QUADRANT_ROTATION
            } else if (cos == 1.0) {
                sin = 0.0
                state = APPLY_IDENTITY
                type = TYPE_IDENTITY
            } else {
                state = APPLY_SHEAR or APPLY_SCALE
                type = TYPE_GENERAL_ROTATION
            }
        }
        m00 = cos
        m10 = sin
        m01 = -sin
        m11 = cos
        m02 = 0.0
        m12 = 0.0
    }

    /**
     * Sets this transform to a translated rotation transformation.
     */
    open fun setToRotation(theta: Double, anchorx: Double, anchory: Double) {
        setToRotation(theta)
        val sin = m10
        val oneMinusCos = 1.0 - m00
        m02 = anchorx * oneMinusCos + anchory * sin
        m12 = anchory * oneMinusCos - anchorx * sin
        if (m02 != 0.0 || m12 != 0.0) {
            state = state or APPLY_TRANSLATE
            type = type or TYPE_TRANSLATION
        }
    }

    /**
     * Sets this transform to a rotation transformation that rotates
     * coordinates according to a rotation vector.
     */
    open fun setToRotation(vecx: Double, vecy: Double) {
        val sin: Double
        val cos: Double
        if (vecy == 0.0) {
            sin = 0.0
            if (vecx < 0.0) {
                cos = -1.0
                state = APPLY_SCALE
                type = TYPE_QUADRANT_ROTATION
            } else {
                cos = 1.0
                state = APPLY_IDENTITY
                type = TYPE_IDENTITY
            }
        } else if (vecx == 0.0) {
            cos = 0.0
            sin = if (vecy > 0.0) 1.0 else -1.0
            state = APPLY_SHEAR
            type = TYPE_QUADRANT_ROTATION
        } else {
            val len = Math.sqrt(vecx * vecx + vecy * vecy)
            cos = vecx / len
            sin = vecy / len
            state = APPLY_SHEAR or APPLY_SCALE
            type = TYPE_GENERAL_ROTATION
        }
        m00 = cos
        m10 = sin
        m01 = -sin
        m11 = cos
        m02 = 0.0
        m12 = 0.0
    }

    /**
     * Sets this transform to a rotation transformation that rotates
     * coordinates around an anchor point according to a rotation
     * vector.
     */
    open fun setToRotation(vecx: Double, vecy: Double, anchorx: Double, anchory: Double) {
        setToRotation(vecx, vecy)
        val sin = m10
        val oneMinusCos = 1.0 - m00
        m02 = anchorx * oneMinusCos + anchory * sin
        m12 = anchory * oneMinusCos - anchorx * sin
        if (m02 != 0.0 || m12 != 0.0) {
            state = state or APPLY_TRANSLATE
            type = type or TYPE_TRANSLATION
        }
    }

    /**
     * Sets this transform to a rotation transformation that rotates
     * coordinates by the specified number of quadrants.
     */
    open fun setToQuadrantRotation(numquadrants: Int) {
        when (numquadrants and 3) {
            0 -> {
                m00 = 1.0
                m10 = 0.0
                m01 = 0.0
                m11 = 1.0
                m02 = 0.0
                m12 = 0.0
                state = APPLY_IDENTITY
                type = TYPE_IDENTITY
            }
            1 -> {
                m00 = 0.0
                m10 = 1.0
                m01 = -1.0
                m11 = 0.0
                m02 = 0.0
                m12 = 0.0
                state = APPLY_SHEAR
                type = TYPE_QUADRANT_ROTATION
            }
            2 -> {
                m00 = -1.0
                m10 = 0.0
                m01 = 0.0
                m11 = -1.0
                m02 = 0.0
                m12 = 0.0
                state = APPLY_SCALE
                type = TYPE_QUADRANT_ROTATION
            }
            3 -> {
                m00 = 0.0
                m10 = -1.0
                m01 = 1.0
                m11 = 0.0
                m02 = 0.0
                m12 = 0.0
                state = APPLY_SHEAR
                type = TYPE_QUADRANT_ROTATION
            }
        }
    }

    /**
     * Sets this transform to a translated rotation transformation
     * that rotates coordinates by the specified number of quadrants
     * around the specified anchor point.
     */
    open fun setToQuadrantRotation(numquadrants: Int, anchorx: Double, anchory: Double) {
        when (numquadrants and 3) {
            0 -> {
                m00 = 1.0
                m10 = 0.0
                m01 = 0.0
                m11 = 1.0
                m02 = 0.0
                m12 = 0.0
                state = APPLY_IDENTITY
                type = TYPE_IDENTITY
            }
            1 -> {
                m00 = 0.0
                m10 = 1.0
                m01 = -1.0
                m11 = 0.0
                m02 = anchorx + anchory
                m12 = anchory - anchorx
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    type = TYPE_QUADRANT_ROTATION
                } else {
                    state = APPLY_SHEAR or APPLY_TRANSLATE
                    type = TYPE_QUADRANT_ROTATION or TYPE_TRANSLATION
                }
            }
            2 -> {
                m00 = -1.0
                m10 = 0.0
                m01 = 0.0
                m11 = -1.0
                m02 = anchorx + anchorx
                m12 = anchory + anchory
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE
                    type = TYPE_QUADRANT_ROTATION
                } else {
                    state = APPLY_SCALE or APPLY_TRANSLATE
                    type = TYPE_QUADRANT_ROTATION or TYPE_TRANSLATION
                }
            }
            3 -> {
                m00 = 0.0
                m10 = -1.0
                m01 = 1.0
                m11 = 0.0
                m02 = anchorx - anchory
                m12 = anchory + anchorx
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    type = TYPE_QUADRANT_ROTATION
                } else {
                    state = APPLY_SHEAR or APPLY_TRANSLATE
                    type = TYPE_QUADRANT_ROTATION or TYPE_TRANSLATION
                }
            }
        }
    }

    /**
     * Sets this transform to a scaling transformation.
     */
    open fun setToScale(sx: Double, sy: Double) {
        m00 = sx
        m10 = 0.0
        m01 = 0.0
        m11 = sy
        m02 = 0.0
        m12 = 0.0
        if (sx != 1.0 || sy != 1.0) {
            state = APPLY_SCALE
            type = TYPE_UNKNOWN
        } else {
            state = APPLY_IDENTITY
            type = TYPE_IDENTITY
        }
    }

    /**
     * Sets this transform to a shearing transformation.
     */
    open fun setToShear(shx: Double, shy: Double) {
        m00 = 1.0
        m01 = shx
        m10 = shy
        m11 = 1.0
        m02 = 0.0
        m12 = 0.0
        if (shx != 0.0 || shy != 0.0) {
            state = (APPLY_SHEAR or APPLY_SCALE)
            type = TYPE_UNKNOWN
        } else {
            state = APPLY_IDENTITY
            type = TYPE_IDENTITY
        }
    }

    /**
     * Sets this transform to a copy of the transform in the specified
     * `AffineTransform` object.
     */
    open fun setTransform(Tx: AffineTransform) {
        this.m00 = Tx.m00
        this.m10 = Tx.m10
        this.m01 = Tx.m01
        this.m11 = Tx.m11
        this.m02 = Tx.m02
        this.m12 = Tx.m12
        this.state = Tx.state
        this.type = Tx.type
    }

    /**
     * Sets this transform to the matrix specified by the 6
     * double precision values.
     */
    open fun setTransform(m00: Double, m10: Double, m01: Double, m11: Double, m02: Double, m12: Double) {
        this.m00 = m00
        this.m10 = m10
        this.m01 = m01
        this.m11 = m11
        this.m02 = m02
        this.m12 = m12
        updateState()
    }

    /**
     * Concatenates an `AffineTransform` `Tx` to
     * this `AffineTransform` Cx in the most commonly useful
     * way to provide a new user space
     * that is mapped to the former user space by `Tx`.
     */
    open fun concatenate(Tx: AffineTransform) {
        var M0: Double
        var M1: Double
        val T00: Double
        var T01: Double
        var T10: Double
        val T11: Double
        val T02: Double
        val T12: Double
        val mystate = state
        val txstate = Tx.state
        val sw = (txstate shl HI_SHIFT) or mystate
        when (sw) {
            (HI_IDENTITY or APPLY_IDENTITY),
            (HI_IDENTITY or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SCALE),
            (HI_IDENTITY or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SHEAR),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> return

            (HI_SHEAR or HI_SCALE or HI_TRANSLATE or APPLY_IDENTITY),
            (HI_SCALE or HI_TRANSLATE or APPLY_IDENTITY),
            (HI_TRANSLATE or APPLY_IDENTITY) -> {
                if (sw == (HI_SHEAR or HI_SCALE or HI_TRANSLATE or APPLY_IDENTITY)) {
                    m01 = Tx.m01
                    m10 = Tx.m10
                    /* NOBREAK */
                }
                if (sw != (HI_TRANSLATE or APPLY_IDENTITY)) {
                    m00 = Tx.m00
                    m11 = Tx.m11
                    /* NOBREAK */
                }
                m02 = Tx.m02
                m12 = Tx.m12
                state = txstate
                type = Tx.type
                return
            }

            (HI_SHEAR or HI_SCALE or APPLY_IDENTITY),
            (HI_SCALE or APPLY_IDENTITY) -> {
                if (sw == (HI_SHEAR or HI_SCALE or APPLY_IDENTITY)) {
                    m01 = Tx.m01
                    m10 = Tx.m10
                    /* NOBREAK */
                }
                m00 = Tx.m00
                m11 = Tx.m11
                state = txstate
                type = Tx.type
                return
            }

            (HI_SHEAR or HI_TRANSLATE or APPLY_IDENTITY),
            (HI_SHEAR or APPLY_IDENTITY) -> {
                if (sw == (HI_SHEAR or HI_TRANSLATE or APPLY_IDENTITY)) {
                    m02 = Tx.m02
                    m12 = Tx.m12
                    /* NOBREAK */
                }
                m01 = Tx.m01
                m10 = Tx.m10
                m11 = 0.0
                m00 = m11
                state = txstate
                type = Tx.type
                return
            }

            (HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE),
            (HI_TRANSLATE or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SHEAR),
            (HI_TRANSLATE or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SCALE),
            (HI_TRANSLATE or APPLY_TRANSLATE) -> {
                translate(Tx.m02, Tx.m12)
                return
            }

            (HI_SCALE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SHEAR or APPLY_SCALE),
            (HI_SCALE or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SHEAR),
            (HI_SCALE or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SCALE),
            (HI_SCALE or APPLY_TRANSLATE) -> {
                scale(Tx.m00, Tx.m11)
                return
            }

            (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE) -> {
                T01 = Tx.m01
                T10 = Tx.m10
                M0 = m00
                m00 = m01 * T10
                m01 = M0 * T01
                M0 = m10
                m10 = m11 * T10
                m11 = M0 * T01
                type = TYPE_UNKNOWN
                return
            }
            (HI_SHEAR or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SHEAR) -> {
                m00 = m01 * Tx.m10
                m01 = 0.0
                m11 = m10 * Tx.m01
                m10 = 0.0
                state = mystate xor (APPLY_SHEAR or APPLY_SCALE)
                type = TYPE_UNKNOWN
                return
            }
            (HI_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SCALE) -> {
                m01 = m00 * Tx.m01
                m00 = 0.0
                m10 = m11 * Tx.m10
                m11 = 0.0
                state = mystate xor (APPLY_SHEAR or APPLY_SCALE)
                type = TYPE_UNKNOWN
                return
            }
            (HI_SHEAR or APPLY_TRANSLATE) -> {
                m00 = 0.0
                m01 = Tx.m01
                m10 = Tx.m10
                m11 = 0.0
                state = APPLY_TRANSLATE or APPLY_SHEAR
                type = TYPE_UNKNOWN
                return
            }
            else -> {
            }
        }
        // If Tx has more than one attribute, it is not worth optimizing
        // all of those cases...
        T00 = Tx.m00
        T01 = Tx.m01
        T02 = Tx.m02
        T10 = Tx.m10
        T11 = Tx.m11
        T12 = Tx.m12
        when (mystate) {
            (APPLY_SHEAR or APPLY_SCALE),
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                if (mystate == (APPLY_SHEAR or APPLY_SCALE)) {
                    state = mystate or txstate
                    /* NOBREAK */
                }
                M0 = m00
                M1 = m01
                m00 = T00 * M0 + T10 * M1
                m01 = T01 * M0 + T11 * M1
                m02 += T02 * M0 + T12 * M1

                M0 = m10
                M1 = m11
                m10 = T00 * M0 + T10 * M1
                m11 = T01 * M0 + T11 * M1
                m12 += T02 * M0 + T12 * M1
                type = TYPE_UNKNOWN
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                M0 = m01
                m00 = T10 * M0
                m01 = T11 * M0
                m02 += T12 * M0

                M0 = m10
                m10 = T00 * M0
                m11 = T01 * M0
                m12 += T02 * M0
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                M0 = m00
                m00 = T00 * M0
                m01 = T01 * M0
                m02 += T02 * M0

                M0 = m11
                m10 = T10 * M0
                m11 = T11 * M0
                m12 += T12 * M0
            }
            (APPLY_TRANSLATE) -> {
                m00 = T00
                m01 = T01
                m02 += T02

                m10 = T10
                m11 = T11
                m12 += T12
                state = txstate or APPLY_TRANSLATE
                type = TYPE_UNKNOWN
                return
            }
            else -> stateError()
        }
        updateState()
    }

    /**
     * Concatenates an `AffineTransform` `Tx` to
     * this `AffineTransform` Cx
     * in a less commonly used way such that `Tx` modifies the
     * coordinate transformation relative to the absolute pixel
     * space rather than relative to the existing user space.
     */
    open fun preConcatenate(Tx: AffineTransform) {
        var M0: Double
        var M1: Double
        var T00: Double
        var T01: Double
        var T10: Double
        var T11: Double
        var T02: Double
        var T12: Double
        var mystate = state
        val txstate = Tx.state
        val sw = (txstate shl HI_SHIFT) or mystate
        when (sw) {
            (HI_IDENTITY or APPLY_IDENTITY),
            (HI_IDENTITY or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SCALE),
            (HI_IDENTITY or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SHEAR),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE),
            (HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                // Tx is IDENTITY...
                return
            }

            (HI_TRANSLATE or APPLY_IDENTITY),
            (HI_TRANSLATE or APPLY_SCALE),
            (HI_TRANSLATE or APPLY_SHEAR),
            (HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE) -> {
                // Tx is TRANSLATE, this has no TRANSLATE
                m02 = Tx.m02
                m12 = Tx.m12
                state = mystate or APPLY_TRANSLATE
                type = type or TYPE_TRANSLATION
                return
            }

            (HI_TRANSLATE or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                // Tx is TRANSLATE, this has one too
                m02 = m02 + Tx.m02
                m12 = m12 + Tx.m12
                return
            }

            (HI_SCALE or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_IDENTITY),
            (HI_SCALE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SHEAR or APPLY_SCALE),
            (HI_SCALE or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SHEAR),
            (HI_SCALE or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SCALE or APPLY_SCALE) -> {
                if (sw == (HI_SCALE or APPLY_TRANSLATE) || sw == (HI_SCALE or APPLY_IDENTITY)) {
                    // Only these two existing states need a new state
                    state = mystate or APPLY_SCALE
                    /* NOBREAK */
                }
                // Tx is SCALE, this is anything
                T00 = Tx.m00
                T11 = Tx.m11
                if ((mystate and APPLY_SHEAR) != 0) {
                    m01 = m01 * T00
                    m10 = m10 * T11
                    if ((mystate and APPLY_SCALE) != 0) {
                        m00 = m00 * T00
                        m11 = m11 * T11
                    }
                } else {
                    m00 = m00 * T00
                    m11 = m11 * T11
                }
                if ((mystate and APPLY_TRANSLATE) != 0) {
                    m02 = m02 * T00
                    m12 = m12 * T11
                }
                type = TYPE_UNKNOWN
                return
            }

            (HI_SHEAR or APPLY_SHEAR or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SHEAR),
            (HI_SHEAR or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_IDENTITY),
            (HI_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SCALE),
            (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE) -> {
                if (sw == (HI_SHEAR or APPLY_SHEAR or APPLY_TRANSLATE) || sw == (HI_SHEAR or APPLY_SHEAR)) {
                    mystate = mystate or APPLY_SCALE
                    /* NOBREAK */
                }
                if (sw != (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)
                    && sw != (HI_SHEAR or APPLY_SHEAR or APPLY_SCALE)) {
                    state = mystate xor APPLY_SHEAR
                    /* NOBREAK */
                }
                // Tx is SHEAR, this is anything
                T01 = Tx.m01
                T10 = Tx.m10

                M0 = m00
                m00 = m10 * T01
                m10 = M0 * T10

                M0 = m01
                m01 = m11 * T01
                m11 = M0 * T10

                M0 = m02
                m02 = m12 * T01
                m12 = M0 * T10
                type = TYPE_UNKNOWN
                return
            }
            else -> {
            }
        }
        // If Tx has more than one attribute, it is not worth optimizing
        // all of those cases...
        T00 = Tx.m00
        T01 = Tx.m01
        T02 = Tx.m02
        T10 = Tx.m10
        T11 = Tx.m11
        T12 = Tx.m12
        when (mystate) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                if (mystate == (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)) {
                    M0 = m02
                    M1 = m12
                    T02 += M0 * T00 + M1 * T01
                    T12 += M0 * T10 + M1 * T11
                    /* NOBREAK */
                }
                m02 = T02
                m12 = T12

                M0 = m00
                M1 = m10
                m00 = M0 * T00 + M1 * T01
                m10 = M0 * T10 + M1 * T11

                M0 = m01
                M1 = m11
                m01 = M0 * T00 + M1 * T01
                m11 = M0 * T10 + M1 * T11
            }

            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                if (mystate == (APPLY_SHEAR or APPLY_TRANSLATE)) {
                    M0 = m02
                    M1 = m12
                    T02 += M0 * T00 + M1 * T01
                    T12 += M0 * T10 + M1 * T11
                    /* NOBREAK */
                }
                m02 = T02
                m12 = T12

                M0 = m10
                m00 = M0 * T01
                m10 = M0 * T11

                M0 = m01
                m01 = M0 * T00
                m11 = M0 * T10
            }

            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                if (mystate == (APPLY_SCALE or APPLY_TRANSLATE)) {
                    M0 = m02
                    M1 = m12
                    T02 += M0 * T00 + M1 * T01
                    T12 += M0 * T10 + M1 * T11
                    /* NOBREAK */
                }
                m02 = T02
                m12 = T12

                M0 = m00
                m00 = M0 * T00
                m10 = M0 * T10

                M0 = m11
                m01 = M0 * T01
                m11 = M0 * T11
            }

            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> {
                if (mystate == APPLY_TRANSLATE) {
                    M0 = m02
                    M1 = m12
                    T02 += M0 * T00 + M1 * T01
                    T12 += M0 * T10 + M1 * T11
                    /* NOBREAK */
                }
                m02 = T02
                m12 = T12

                m00 = T00
                m10 = T10

                m01 = T01
                m11 = T11

                state = mystate or txstate
                type = TYPE_UNKNOWN
                return
            }
            else -> stateError()
        }
        updateState()
    }

    /**
     * Returns an `AffineTransform` object representing the
     * inverse transformation.
     */
    @Throws(NoninvertibleTransformException::class)
    open fun createInverse(): AffineTransform {
        val det: Double
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                det = m00 * m11 - m01 * m10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                return AffineTransform(m11 / det, -m10 / det, -m01 / det, m00 / det,
                    (m01 * m12 - m11 * m02) / det, (m10 * m02 - m00 * m12) / det, (APPLY_SHEAR
                            or APPLY_SCALE or APPLY_TRANSLATE))
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                det = m00 * m11 - m01 * m10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                return AffineTransform(m11 / det, -m10 / det, -m01 / det, m00 / det, 0.0, 0.0,
                    (APPLY_SHEAR or APPLY_SCALE))
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                if (m01 == 0.0 || m10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                return AffineTransform(0.0, 1.0 / m01, 1.0 / m10, 0.0, -m12 / m10, -m02 / m01,
                    (APPLY_SHEAR or APPLY_TRANSLATE))
            }
            (APPLY_SHEAR) -> {
                if (m01 == 0.0 || m10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                return AffineTransform(0.0, 1.0 / m01, 1.0 / m10, 0.0, 0.0, 0.0, (APPLY_SHEAR))
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                if (m00 == 0.0 || m11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                return AffineTransform(1.0 / m00, 0.0, 0.0, 1.0 / m11, -m02 / m00, -m12 / m11,
                    (APPLY_SCALE or APPLY_TRANSLATE))
            }
            (APPLY_SCALE) -> {
                if (m00 == 0.0 || m11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                return AffineTransform(1.0 / m00, 0.0, 0.0, 1.0 / m11, 0.0, 0.0, (APPLY_SCALE))
            }
            (APPLY_TRANSLATE) -> return AffineTransform(1.0, 0.0, 0.0, 1.0, -m02, -m12, (APPLY_TRANSLATE))
            (APPLY_IDENTITY) -> return AffineTransform()
            else -> stateError()
        }
    }

    /**
     * Sets this transform to the inverse of itself.
     */
    @Throws(NoninvertibleTransformException::class)
    open fun invert() {
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        val det: Double
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                det = M00 * M11 - M01 * M10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                m00 = M11 / det
                m10 = -M10 / det
                m01 = -M01 / det
                m11 = M00 / det
                m02 = (M01 * M12 - M11 * M02) / det
                m12 = (M10 * M02 - M00 * M12) / det
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                det = M00 * M11 - M01 * M10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                m00 = M11 / det
                m10 = -M10 / det
                m01 = -M01 / det
                m11 = M00 / det
                // m02 = 0.0;
                // m12 = 0.0;
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                if (M01 == 0.0 || M10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                // m00 = 0.0;
                m10 = 1.0 / M01
                m01 = 1.0 / M10
                // m11 = 0.0;
                m02 = -M12 / M10
                m12 = -M02 / M01
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                if (M01 == 0.0 || M10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                // m00 = 0.0;
                m10 = 1.0 / M01
                m01 = 1.0 / M10
                // m11 = 0.0;
                // m02 = 0.0;
                // m12 = 0.0;
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                if (M00 == 0.0 || M11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                m00 = 1.0 / M00
                // m10 = 0.0;
                // m01 = 0.0;
                m11 = 1.0 / M11
                m02 = -M02 / M00
                m12 = -M12 / M11
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                if (M00 == 0.0 || M11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                m00 = 1.0 / M00
                // m10 = 0.0;
                // m01 = 0.0;
                m11 = 1.0 / M11
                // m02 = 0.0;
                // m12 = 0.0;
            }
            (APPLY_TRANSLATE) -> {
                // m00 = 1.0;
                // m10 = 0.0;
                // m01 = 0.0;
                // m11 = 1.0;
                m02 = -m02
                m12 = -m12
            }
            (APPLY_IDENTITY) -> {
            }
            else -> stateError()
        }
    }

    /**
     * Transforms the specified `ptSrc` and stores the result
     * in `ptDst`.
     */
    open fun transform(ptSrc: Point2D, ptDst: Point2D?): Point2D {
        var ptDst = ptDst
        if (ptDst == null) {
            if (ptSrc is Point2D.Double) {
                ptDst = Point2D.Double()
            } else {
                ptDst = Point2D.Float()
            }
        }
        // Copy source coords into local variables in case src == dst
        val x = ptSrc.getX()
        val y = ptSrc.getY()
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                ptDst.setLocation(x * m00 + y * m01 + m02, x * m10 + y * m11 + m12)
                return ptDst
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11)
                return ptDst
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                ptDst.setLocation(y * m01 + m02, x * m10 + m12)
                return ptDst
            }
            (APPLY_SHEAR) -> {
                ptDst.setLocation(y * m01, x * m10)
                return ptDst
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                ptDst.setLocation(x * m00 + m02, y * m11 + m12)
                return ptDst
            }
            (APPLY_SCALE) -> {
                ptDst.setLocation(x * m00, y * m11)
                return ptDst
            }
            (APPLY_TRANSLATE) -> {
                ptDst.setLocation(x + m02, y + m12)
                return ptDst
            }
            (APPLY_IDENTITY) -> {
                ptDst.setLocation(x, y)
                return ptDst
            }
            else -> stateError()
        }
    }

    /**
     * Transforms an array of point objects by this transform.
     */
    open fun transform(ptSrc: Array<Point2D>, srcOff: Int, ptDst: Array<Point2D?>, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val state = this.state
        while (--numPts >= 0) {
            // Copy source coords into local variables in case src == dst
            val src = ptSrc[srcOff++]
            val x = src.getX()
            val y = src.getY()
            var dst = ptDst[dstOff++]
            if (dst == null) {
                if (src is Point2D.Double) {
                    dst = Point2D.Double()
                } else {
                    dst = Point2D.Float()
                }
                ptDst[dstOff - 1] = dst
            }
            when (state) {
                (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) ->
                    dst.setLocation(x * m00 + y * m01 + m02, x * m10 + y * m11 + m12)
                (APPLY_SHEAR or APPLY_SCALE) ->
                    dst.setLocation(x * m00 + y * m01, x * m10 + y * m11)
                (APPLY_SHEAR or APPLY_TRANSLATE) ->
                    dst.setLocation(y * m01 + m02, x * m10 + m12)
                (APPLY_SHEAR) ->
                    dst.setLocation(y * m01, x * m10)
                (APPLY_SCALE or APPLY_TRANSLATE) ->
                    dst.setLocation(x * m00 + m02, y * m11 + m12)
                (APPLY_SCALE) ->
                    dst.setLocation(x * m00, y * m11)
                (APPLY_TRANSLATE) ->
                    dst.setLocation(x + m02, y + m12)
                (APPLY_IDENTITY) ->
                    dst.setLocation(x, y)
                else -> stateError()
            }
        }
    }

    /**
     * Transforms an array of coordinates by this transform.
     */
    open fun transform(srcPts: FloatArray, srcOff: Int, dstPts: FloatArray, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        if (dstPts === srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            // If the arrays overlap partially with the destination higher
            // than the source and we transform the coordinates normally
            // we would overwrite some of the later source coordinates
            // with results of previous transformations.
            // To get around this we use arraycopy to copy the points
            // to their final destination with correct overwrite
            // handling and then transform them in place in the new
            // safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
            // srcPts = dstPts;         // They are known to be equal.
            srcOff = dstOff
        }
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    val y = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = (M00 * x + M01 * y + M02).toFloat()
                    dstPts[dstOff++] = (M10 * x + M11 * y + M12).toFloat()
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    val y = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = (M00 * x + M01 * y).toFloat()
                    dstPts[dstOff++] = (M10 * x + M11 * y).toFloat()
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = (M01 * srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (M10 * x + M12).toFloat()
                }
                return
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = (M01 * srcPts[srcOff++]).toFloat()
                    dstPts[dstOff++] = (M10 * x).toFloat()
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (M00 * srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (M11 * srcPts[srcOff++] + M12).toFloat()
                }
                return
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (M00 * srcPts[srcOff++]).toFloat()
                    dstPts[dstOff++] = (M11 * srcPts[srcOff++]).toFloat()
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                M02 = m02
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (srcPts[srcOff++] + M12).toFloat()
                }
                return
            }
            (APPLY_IDENTITY) -> {
                if (srcPts !== dstPts || srcOff != dstOff) {
                    System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Transforms an array of coordinates by this transform.
     */
    open fun transform(srcPts: DoubleArray, srcOff: Int, dstPts: DoubleArray, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        if (dstPts === srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            // If the arrays overlap partially with the destination higher
            // than the source and we transform the coordinates normally
            // we would overwrite some of the later source coordinates
            // with results of previous transformations.
            // To get around this we use arraycopy to copy the points
            // to their final destination with correct overwrite
            // handling and then transform them in place in the new
            // safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
            // srcPts = dstPts;         // They are known to be equal.
            srcOff = dstOff
        }
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = M00 * x + M01 * y + M02
                    dstPts[dstOff++] = M10 * x + M11 * y + M12
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = M00 * x + M01 * y
                    dstPts[dstOff++] = M10 * x + M11 * y
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02
                    dstPts[dstOff++] = M10 * x + M12
                }
                return
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = M01 * srcPts[srcOff++]
                    dstPts[dstOff++] = M10 * x
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02
                    dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12
                }
                return
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++]
                    dstPts[dstOff++] = M11 * srcPts[srcOff++]
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                M02 = m02
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] + M02
                    dstPts[dstOff++] = srcPts[srcOff++] + M12
                }
                return
            }
            (APPLY_IDENTITY) -> {
                if (srcPts !== dstPts || srcOff != dstOff) {
                    System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Transforms an array of coordinates by this transform.
     */
    open fun transform(srcPts: FloatArray, srcOff: Int, dstPts: DoubleArray, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    val y = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = M00 * x + M01 * y + M02
                    dstPts[dstOff++] = M10 * x + M11 * y + M12
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    val y = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = M00 * x + M01 * y
                    dstPts[dstOff++] = M10 * x + M11 * y
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02
                    dstPts[dstOff++] = M10 * x + M12
                }
                return
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = M01 * srcPts[srcOff++]
                    dstPts[dstOff++] = M10 * x
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02
                    dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12
                }
                return
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                while (--numPts >= 0) {
                    dstPts[dstOff++] = M00 * srcPts[srcOff++]
                    dstPts[dstOff++] = M11 * srcPts[srcOff++]
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                M02 = m02
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] + M02
                    dstPts[dstOff++] = srcPts[srcOff++] + M12
                }
                return
            }
            (APPLY_IDENTITY) -> {
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++].toDouble()
                    dstPts[dstOff++] = srcPts[srcOff++].toDouble()
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Transforms an array of coordinates by this transform.
     */
    open fun transform(srcPts: DoubleArray, srcOff: Int, dstPts: FloatArray, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = (M00 * x + M01 * y + M02).toFloat()
                    dstPts[dstOff++] = (M10 * x + M11 * y + M12).toFloat()
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = (M00 * x + M01 * y).toFloat()
                    dstPts[dstOff++] = (M10 * x + M11 * y).toFloat()
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = (M01 * srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (M10 * x + M12).toFloat()
                }
                return
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = (M01 * srcPts[srcOff++]).toFloat()
                    dstPts[dstOff++] = (M10 * x).toFloat()
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (M00 * srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (M11 * srcPts[srcOff++] + M12).toFloat()
                }
                return
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (M00 * srcPts[srcOff++]).toFloat()
                    dstPts[dstOff++] = (M11 * srcPts[srcOff++]).toFloat()
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                M02 = m02
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (srcPts[srcOff++] + M02).toFloat()
                    dstPts[dstOff++] = (srcPts[srcOff++] + M12).toFloat()
                }
                return
            }
            (APPLY_IDENTITY) -> {
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++].toFloat()
                    dstPts[dstOff++] = srcPts[srcOff++].toFloat()
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Inverse transforms the specified `ptSrc` and stores the
     * result in `ptDst`.
     */
    @Throws(NoninvertibleTransformException::class)
    open fun inverseTransform(ptSrc: Point2D, ptDst: Point2D?): Point2D {
        var ptDst = ptDst
        if (ptDst == null) {
            if (ptSrc is Point2D.Double) {
                ptDst = Point2D.Double()
            } else {
                ptDst = Point2D.Float()
            }
        }
        // Copy source coords into local variables in case src == dst
        var x = ptSrc.getX()
        var y = ptSrc.getY()
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                if (state == (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)) {
                    x -= m02
                    y -= m12
                    /* NOBREAK */
                }
                val det = m00 * m11 - m01 * m10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                ptDst.setLocation((x * m11 - y * m01) / det, (y * m00 - x * m10) / det)
                return ptDst
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                if (state == (APPLY_SHEAR or APPLY_TRANSLATE)) {
                    x -= m02
                    y -= m12
                    /* NOBREAK */
                }
                if (m01 == 0.0 || m10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(y / m10, x / m01)
                return ptDst
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                if (state == (APPLY_SCALE or APPLY_TRANSLATE)) {
                    x -= m02
                    y -= m12
                    /* NOBREAK */
                }
                if (m00 == 0.0 || m11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(x / m00, y / m11)
                return ptDst
            }
            (APPLY_TRANSLATE) -> {
                ptDst.setLocation(x - m02, y - m12)
                return ptDst
            }
            (APPLY_IDENTITY) -> {
                ptDst.setLocation(x, y)
                return ptDst
            }
            else -> stateError()
        }
    }

    /**
     * Inverse transforms an array of double precision coordinates by
     * this transform.
     */
    @Throws(NoninvertibleTransformException::class)
    open fun inverseTransform(srcPts: DoubleArray, srcOff: Int, dstPts: DoubleArray, dstOff: Int,
                              numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M02: Double
        val M10: Double
        val M11: Double
        val M12: Double
        val det: Double
        if (dstPts === srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            // If the arrays overlap partially with the destination higher
            // than the source and we transform the coordinates normally
            // we would overwrite some of the later source coordinates
            // with results of previous transformations.
            // To get around this we use arraycopy to copy the points
            // to their final destination with correct overwrite
            // handling and then transform them in place in the new
            // safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
            // srcPts = dstPts;         // They are known to be equal.
            srcOff = dstOff
        }
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M01 = m01
                M02 = m02
                M10 = m10
                M11 = m11
                M12 = m12
                det = M00 * M11 - M01 * M10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++] - M02
                    val y = srcPts[srcOff++] - M12
                    dstPts[dstOff++] = (x * M11 - y * M01) / det
                    dstPts[dstOff++] = (y * M00 - x * M10) / det
                }
                return
            }
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                det = M00 * M11 - M01 * M10
                if (Math.abs(det) <= java.lang.Double.MIN_VALUE) {
                    throw NoninvertibleTransformException("Determinant is $det")
                }
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = (x * M11 - y * M01) / det
                    dstPts[dstOff++] = (y * M00 - x * M10) / det
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE) -> {
                M01 = m01
                M02 = m02
                M10 = m10
                M12 = m12
                if (M01 == 0.0 || M10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++] - M02
                    dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M10
                    dstPts[dstOff++] = x / M01
                }
                return
            }
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                if (M01 == 0.0 || M10 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = srcPts[srcOff++] / M10
                    dstPts[dstOff++] = x / M01
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE) -> {
                M00 = m00
                M02 = m02
                M11 = m11
                M12 = m12
                if (M00 == 0.0 || M11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                while (--numPts >= 0) {
                    dstPts[dstOff++] = (srcPts[srcOff++] - M02) / M00
                    dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M11
                }
                return
            }
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                if (M00 == 0.0 || M11 == 0.0) {
                    throw NoninvertibleTransformException("Determinant is 0")
                }
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] / M00
                    dstPts[dstOff++] = srcPts[srcOff++] / M11
                }
                return
            }
            (APPLY_TRANSLATE) -> {
                M02 = m02
                M12 = m12
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] - M02
                    dstPts[dstOff++] = srcPts[srcOff++] - M12
                }
                return
            }
            (APPLY_IDENTITY) -> {
                if (srcPts !== dstPts || srcOff != dstOff) {
                    System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Transforms the relative distance vector specified by
     * `ptSrc` and stores the result in `ptDst`.
     */
    open fun deltaTransform(ptSrc: Point2D, ptDst: Point2D?): Point2D {
        var ptDst = ptDst
        if (ptDst == null) {
            if (ptSrc is Point2D.Double) {
                ptDst = Point2D.Double()
            } else {
                ptDst = Point2D.Float()
            }
        }
        // Copy source coords into local variables in case src == dst
        val x = ptSrc.getX()
        val y = ptSrc.getY()
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11)
                return ptDst
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                ptDst.setLocation(y * m01, x * m10)
                return ptDst
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                ptDst.setLocation(x * m00, y * m11)
                return ptDst
            }
            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> {
                ptDst.setLocation(x, y)
                return ptDst
            }
            else -> stateError()
        }
    }

    /**
     * Transforms an array of relative distance vectors by this
     * transform.
     */
    open fun deltaTransform(srcPts: DoubleArray, srcOff: Int, dstPts: DoubleArray, dstOff: Int, numPts: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        var numPts = numPts
        val M00: Double
        val M01: Double
        val M10: Double
        val M11: Double
        if (dstPts === srcPts && dstOff > srcOff && dstOff < srcOff + numPts * 2) {
            // If the arrays overlap partially with the destination higher
            // than the source and we transform the coordinates normally
            // we would overwrite some of the later source coordinates
            // with results of previous transformations.
            // To get around this we use arraycopy to copy the points
            // to their final destination with correct overwrite
            // handling and then transform them in place in the new
            // safer location.
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
            // srcPts = dstPts;         // They are known to be equal.
            srcOff = dstOff
        }
        when (state) {
            (APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SHEAR or APPLY_SCALE) -> {
                M00 = m00
                M01 = m01
                M10 = m10
                M11 = m11
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    val y = srcPts[srcOff++]
                    dstPts[dstOff++] = x * M00 + y * M01
                    dstPts[dstOff++] = x * M10 + y * M11
                }
                return
            }
            (APPLY_SHEAR or APPLY_TRANSLATE),
            (APPLY_SHEAR) -> {
                M01 = m01
                M10 = m10
                while (--numPts >= 0) {
                    val x = srcPts[srcOff++]
                    dstPts[dstOff++] = srcPts[srcOff++] * M01
                    dstPts[dstOff++] = x * M10
                }
                return
            }
            (APPLY_SCALE or APPLY_TRANSLATE),
            (APPLY_SCALE) -> {
                M00 = m00
                M11 = m11
                while (--numPts >= 0) {
                    dstPts[dstOff++] = srcPts[srcOff++] * M00
                    dstPts[dstOff++] = srcPts[srcOff++] * M11
                }
                return
            }
            (APPLY_TRANSLATE),
            (APPLY_IDENTITY) -> {
                if (srcPts !== dstPts || srcOff != dstOff) {
                    System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2)
                }
                return
            }
            else -> stateError()
        }
    }

    /**
     * Returns a new [Shape] object defined by the geometry of the
     * specified `Shape` after it has been transformed by
     * this transform.
     */
    open fun createTransformedShape(pSrc: Shape?): Shape? {
        if (pSrc == null) {
            return null
        }
        return Path2D.Double(pSrc, this)
    }

    /**
     * Returns a `String` that represents the value of this
     * [Object].
     */
    override fun toString(): String {
        return ("AffineTransform[[" + _matround(m00) + ", " + _matround(m01) + ", "
                + _matround(m02) + "], [" + _matround(m10) + ", " + _matround(m11) + ", "
                + _matround(m12) + "]]")
    }

    /**
     * Returns `true` if this `AffineTransform` is
     * an identity transform.
     */
    open fun isIdentity(): Boolean {
        return (state == APPLY_IDENTITY || (getType() == TYPE_IDENTITY))
    }

    /**
     * Returns a copy of this `AffineTransform` object.
     */
    public override fun clone(): Any {
        try {
            return super.clone()
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }

    /**
     * Returns the hashcode for this transform.
     */
    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(m00)
        bits = bits * 31 + java.lang.Double.doubleToLongBits(m01)
        bits = bits * 31 + java.lang.Double.doubleToLongBits(m02)
        bits = bits * 31 + java.lang.Double.doubleToLongBits(m10)
        bits = bits * 31 + java.lang.Double.doubleToLongBits(m11)
        bits = bits * 31 + java.lang.Double.doubleToLongBits(m12)
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    /**
     * Returns `true` if this `AffineTransform`
     * represents the same affine coordinate transform as the specified
     * argument.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is AffineTransform) {
            return false
        }
        val a = other
        return ((m00 == a.m00) && (m01 == a.m01) && (m02 == a.m02) && (m10 == a.m10)
                && (m11 == a.m11) && (m12 == a.m12))
    }

    /* Serialization support.  A readObject method is neccessary because
     * the state field is part of the implementation of this particular
     * AffineTransform and not part of the public specification.  The
     * state variable's value needs to be recalculated on the fly by the
     * readObject method as it is in the 6-argument matrix constructor.
     */

    @Throws(ClassNotFoundException::class, java.io.IOException::class)
    private fun writeObject(s: java.io.ObjectOutputStream) {
        s.defaultWriteObject()
    }

    @Throws(ClassNotFoundException::class, java.io.IOException::class)
    private fun readObject(s: java.io.ObjectInputStream) {
        s.defaultReadObject()
        updateState()
    }

    companion object {
        /*
         * This constant is only useful for the cached type field.
         * It indicates that the type has been decached and must be recalculated.
         */
        private const val TYPE_UNKNOWN = -1

        /**
         * This constant indicates that the transform defined by this object
         * is an identity transform.
         */
        const val TYPE_IDENTITY = 0

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a translation in addition to the conversions indicated
         * by other flag bits.
         */
        const val TYPE_TRANSLATION = 1

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a uniform scale in addition to the conversions indicated
         * by other flag bits.
         */
        const val TYPE_UNIFORM_SCALE = 2

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a general scale in addition to the conversions indicated
         * by other flag bits.
         */
        const val TYPE_GENERAL_SCALE = 4

        /**
         * This constant is a bit mask for any of the scale flag bits.
         */
        const val TYPE_MASK_SCALE = (TYPE_UNIFORM_SCALE or TYPE_GENERAL_SCALE)

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a mirror image flip about some axis which changes the
         * normally right handed coordinate system into a left handed
         * system in addition to the conversions indicated by other flag bits.
         */
        const val TYPE_FLIP = 64

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a quadrant rotation by some multiple of 90 degrees in
         * addition to the conversions indicated by other flag bits.
         */
        const val TYPE_QUADRANT_ROTATION = 8

        /**
         * This flag bit indicates that the transform defined by this object
         * performs a rotation by an arbitrary angle in addition to the
         * conversions indicated by other flag bits.
         */
        const val TYPE_GENERAL_ROTATION = 16

        /**
         * This constant is a bit mask for any of the rotation flag bits.
         */
        const val TYPE_MASK_ROTATION = (TYPE_QUADRANT_ROTATION or TYPE_GENERAL_ROTATION)

        /**
         * This constant indicates that the transform defined by this object
         * performs an arbitrary conversion of the input coordinates.
         */
        const val TYPE_GENERAL_TRANSFORM = 32

        /**
         * This constant is used for the internal state variable to indicate
         * that no calculations need to be performed and that the source
         * coordinates only need to be copied to their destinations to
         * complete the transformation equation of this transform.
         */
        internal const val APPLY_IDENTITY = 0

        /**
         * This constant is used for the internal state variable to indicate
         * that the translation components of the matrix (m02 and m12) need
         * to be added to complete the transformation equation of this transform.
         */
        internal const val APPLY_TRANSLATE = 1

        /**
         * This constant is used for the internal state variable to indicate
         * that the scaling components of the matrix (m00 and m11) need
         * to be factored in to complete the transformation equation of
         * this transform.
         */
        internal const val APPLY_SCALE = 2

        /**
         * This constant is used for the internal state variable to indicate
         * that the shearing components of the matrix (m01 and m10) need
         * to be factored in to complete the transformation equation of this
         * transform.
         */
        internal const val APPLY_SHEAR = 4

        /*
         * For methods which combine together the state of two separate
         * transforms and dispatch based upon the combination, these constants
         * specify how far to shift one of the states so that the two states
         * are mutually non-interfering and provide constants for testing the
         * bits of the shifted (HI) state.
         */
        private const val HI_SHIFT = 3
        private const val HI_IDENTITY = APPLY_IDENTITY shl HI_SHIFT
        private const val HI_TRANSLATE = APPLY_TRANSLATE shl HI_SHIFT
        private const val HI_SCALE = APPLY_SCALE shl HI_SHIFT
        private const val HI_SHEAR = APPLY_SHEAR shl HI_SHIFT

        private val rot90conversion = intArrayOf(
            /* IDENTITY => */ APPLY_SHEAR,
            /* TRANSLATE (TR) => */ APPLY_SHEAR or APPLY_TRANSLATE,
            /* SCALE (SC) => */ APPLY_SHEAR,
            /* SC | TR => */ APPLY_SHEAR or APPLY_TRANSLATE,
            /* SHEAR (SH) => */ APPLY_SCALE,
            /* SH | TR => */ APPLY_SCALE or APPLY_TRANSLATE,
            /* SH | SC => */ APPLY_SHEAR or APPLY_SCALE,
            /* SH | SC | TR => */ APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE)

        /**
         * Returns a transform representing a translation transformation.
         */
        @JvmStatic
        fun getTranslateInstance(tx: Double, ty: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToTranslation(tx, ty)
            return Tx
        }

        /**
         * Returns a transform representing a rotation transformation.
         */
        @JvmStatic
        fun getRotateInstance(theta: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToRotation(theta)
            return Tx
        }

        /**
         * Returns a transform that rotates coordinates around an anchor point.
         */
        @JvmStatic
        fun getRotateInstance(theta: Double, anchorx: Double, anchory: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToRotation(theta, anchorx, anchory)
            return Tx
        }

        /**
         * Returns a transform that rotates coordinates according to
         * a rotation vector.
         */
        @JvmStatic
        fun getRotateInstance(vecx: Double, vecy: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToRotation(vecx, vecy)
            return Tx
        }

        /**
         * Returns a transform that rotates coordinates around an anchor
         * point accordinate to a rotation vector.
         */
        @JvmStatic
        fun getRotateInstance(vecx: Double, vecy: Double, anchorx: Double,
                              anchory: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToRotation(vecx, vecy, anchorx, anchory)
            return Tx
        }

        /**
         * Returns a transform that rotates coordinates by the specified
         * number of quadrants.
         */
        @JvmStatic
        fun getQuadrantRotateInstance(numquadrants: Int): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToQuadrantRotation(numquadrants)
            return Tx
        }

        /**
         * Returns a transform that rotates coordinates by the specified
         * number of quadrants around the specified anchor point.
         */
        @JvmStatic
        fun getQuadrantRotateInstance(numquadrants: Int, anchorx: Double,
                                      anchory: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToQuadrantRotation(numquadrants, anchorx, anchory)
            return Tx
        }

        /**
         * Returns a transform representing a scaling transformation.
         */
        @JvmStatic
        fun getScaleInstance(sx: Double, sy: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToScale(sx, sy)
            return Tx
        }

        /**
         * Returns a transform representing a shearing transformation.
         */
        @JvmStatic
        fun getShearInstance(shx: Double, shy: Double): AffineTransform {
            val Tx = AffineTransform()
            Tx.setToShear(shx, shy)
            return Tx
        }

        /* Round values to sane precision for printing
         * Note that Math.sin(Math.PI) has an error of about 10^-16
         */
        private fun _matround(matval: Double): Double {
            return Math.rint(matval * 1E15) / 1E15
        }

        /*
         * JDK 1.2 serialVersionUID
         */
        private const val serialVersionUID = 1330973210523860834L
    }
}
