/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.util.Arrays
import java.io.Serializable
import java.io.StreamCorruptedException

/**
 * The `Path2D` class provides a simple, yet flexible
 * shape which represents an arbitrary geometric path.
 */
abstract class Path2D : Shape, Cloneable {

    @Transient
    internal lateinit var pointTypes: ByteArray

    @Transient
    @JvmField
    internal var numTypes = 0

    @Transient
    @JvmField
    internal var numCoords = 0

    @Transient
    @JvmField
    internal var windingRule = 0

    /**
     * Constructs a new empty `Path2D` object.
     * It is assumed that the package sibling subclass that is
     * defaulting to this constructor will fill in all values.
     */
    internal constructor()

    /**
     * Constructs a new `Path2D` object from the given
     * specified initial values.
     * This method is only intended for internal use and should
     * not be made public if the other constructors for this class
     * are ever exposed.
     */
    internal constructor(rule: Int, initialTypes: Int) {
        setWindingRule(rule)
        this.pointTypes = ByteArray(initialTypes)
    }

    internal abstract fun cloneCoordsFloat(at: AffineTransform?): FloatArray

    internal abstract fun cloneCoordsDouble(at: AffineTransform?): DoubleArray

    internal abstract fun append(x: kotlin.Float, y: kotlin.Float)

    internal abstract fun append(x: kotlin.Double, y: kotlin.Double)

    internal abstract fun getPoint(coordindex: Int): Point2D

    internal abstract fun needRoom(needMove: Boolean, newCoords: Int)

    internal abstract fun pointCrossings(px: kotlin.Double, py: kotlin.Double): Int

    internal abstract fun rectCrossings(rxmin: kotlin.Double, rymin: kotlin.Double,
                                        rxmax: kotlin.Double, rymax: kotlin.Double): Int

    /**
     * The `Float` class defines a geometric path with
     * coordinates stored in single precision floating point.
     */
    open class Float : Path2D, Serializable {

        @Transient
        internal lateinit var floatCoords: FloatArray

        /**
         * Constructs a new empty single precision `Path2D` object
         * with a default winding rule of [WIND_NON_ZERO].
         */
        constructor() : this(WIND_NON_ZERO, INIT_SIZE)

        /**
         * Constructs a new empty single precision `Path2D` object
         * with the specified winding rule to control operations that
         * require the interior of the path to be defined.
         */
        constructor(rule: Int) : this(rule, INIT_SIZE)

        /**
         * Constructs a new empty single precision `Path2D` object
         * with the specified winding rule and the specified initial
         * capacity to store path segments.
         */
        constructor(rule: Int, initialCapacity: Int) : super(rule, initialCapacity) {
            floatCoords = FloatArray(initialCapacity * 2)
        }

        /**
         * Constructs a new single precision `Path2D` object
         * from an arbitrary [Shape] object.
         */
        constructor(s: Shape) : this(s, null)

        /**
         * Constructs a new single precision `Path2D` object
         * from an arbitrary [Shape] object, transformed by an
         * [AffineTransform] object.
         */
        constructor(s: Shape, at: AffineTransform?) : super() {
            if (s is Path2D) {
                val p2d = s
                setWindingRule(p2d.windingRule)
                this.numTypes = p2d.numTypes
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.pointTypes.size)
                this.numCoords = p2d.numCoords
                this.floatCoords = p2d.cloneCoordsFloat(at)
            } else {
                val pi = s.getPathIterator(at)
                setWindingRule(pi.getWindingRule())
                this.pointTypes = ByteArray(INIT_SIZE)
                this.floatCoords = FloatArray(INIT_SIZE * 2)
                append(pi, false)
            }
        }

        override fun cloneCoordsFloat(at: AffineTransform?): FloatArray {
            val ret: FloatArray
            if (at == null) {
                ret = Arrays.copyOf(this.floatCoords, this.floatCoords.size)
            } else {
                ret = FloatArray(floatCoords.size)
                at.transform(floatCoords, 0, ret, 0, numCoords / 2)
            }
            return ret
        }

        override fun cloneCoordsDouble(at: AffineTransform?): DoubleArray {
            val ret = DoubleArray(floatCoords.size)
            if (at == null) {
                for (i in 0 until numCoords) {
                    ret[i] = floatCoords[i].toDouble()
                }
            } else {
                at.transform(floatCoords, 0, ret, 0, numCoords / 2)
            }
            return ret
        }

        override fun append(x: kotlin.Float, y: kotlin.Float) {
            floatCoords[numCoords++] = x
            floatCoords[numCoords++] = y
        }

        override fun append(x: kotlin.Double, y: kotlin.Double) {
            floatCoords[numCoords++] = x.toFloat()
            floatCoords[numCoords++] = y.toFloat()
        }

        override fun getPoint(coordindex: Int): Point2D {
            return Point2D.Float(floatCoords[coordindex], floatCoords[coordindex + 1])
        }

        override fun needRoom(needMove: Boolean, newCoords: Int) {
            if (needMove && numTypes == 0) {
                // throw new IllegalPathStateException("missing initial moveto "+
                // "in path definition");
            }
            var size = pointTypes.size
            if (numTypes >= size) {
                var grow = size
                if (grow > EXPAND_MAX) {
                    grow = EXPAND_MAX
                }
                pointTypes = Arrays.copyOf(pointTypes, size + grow)
            }
            size = floatCoords.size
            if (numCoords + newCoords > size) {
                var grow = size
                if (grow > EXPAND_MAX * 2) {
                    grow = EXPAND_MAX * 2
                }
                if (grow < newCoords) {
                    grow = newCoords
                }
                floatCoords = Arrays.copyOf(floatCoords, size + grow)
            }
        }

        @Synchronized
        final override fun moveTo(x: kotlin.Double, y: kotlin.Double) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                floatCoords[numCoords - 2] = x.toFloat()
                floatCoords[numCoords - 1] = y.toFloat()
            } else {
                needRoom(false, 2)
                pointTypes[numTypes++] = SEG_MOVETO
                floatCoords[numCoords++] = x.toFloat()
                floatCoords[numCoords++] = y.toFloat()
            }
        }

        /**
         * Adds a point to the path by moving to the specified
         * coordinates specified in float precision.
         */
        @Synchronized
        fun moveTo(x: kotlin.Float, y: kotlin.Float) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                floatCoords[numCoords - 2] = x
                floatCoords[numCoords - 1] = y
            } else {
                needRoom(false, 2)
                pointTypes[numTypes++] = SEG_MOVETO
                floatCoords[numCoords++] = x
                floatCoords[numCoords++] = y
            }
        }

        @Synchronized
        final override fun lineTo(x: kotlin.Double, y: kotlin.Double) {
            needRoom(true, 2)
            pointTypes[numTypes++] = SEG_LINETO
            floatCoords[numCoords++] = x.toFloat()
            floatCoords[numCoords++] = y.toFloat()
        }

        /**
         * Adds a point to the path by drawing a straight line from the
         * current coordinates to the new specified coordinates
         * specified in float precision.
         */
        @Synchronized
        fun lineTo(x: kotlin.Float, y: kotlin.Float) {
            needRoom(true, 2)
            pointTypes[numTypes++] = SEG_LINETO
            floatCoords[numCoords++] = x
            floatCoords[numCoords++] = y
        }

        @Synchronized
        final override fun quadTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            needRoom(true, 4)
            pointTypes[numTypes++] = SEG_QUADTO
            floatCoords[numCoords++] = x1.toFloat()
            floatCoords[numCoords++] = y1.toFloat()
            floatCoords[numCoords++] = x2.toFloat()
            floatCoords[numCoords++] = y2.toFloat()
        }

        /**
         * Adds a curved segment, defined by two new points, to the path by
         * drawing a Quadratic curve that intersects both the current
         * coordinates and the specified coordinates `(x2,y2)`,
         * using the specified point `(x1,y1)` as a quadratic
         * parametric control point.
         */
        @Synchronized
        fun quadTo(x1: kotlin.Float, y1: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float) {
            needRoom(true, 4)
            pointTypes[numTypes++] = SEG_QUADTO
            floatCoords[numCoords++] = x1
            floatCoords[numCoords++] = y1
            floatCoords[numCoords++] = x2
            floatCoords[numCoords++] = y2
        }

        @Synchronized
        final override fun curveTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double,
                                   x3: kotlin.Double, y3: kotlin.Double) {
            needRoom(true, 6)
            pointTypes[numTypes++] = SEG_CUBICTO
            floatCoords[numCoords++] = x1.toFloat()
            floatCoords[numCoords++] = y1.toFloat()
            floatCoords[numCoords++] = x2.toFloat()
            floatCoords[numCoords++] = y2.toFloat()
            floatCoords[numCoords++] = x3.toFloat()
            floatCoords[numCoords++] = y3.toFloat()
        }

        /**
         * Adds a curved segment, defined by three new points, to the path by
         * drawing a B&eacute;zier curve that intersects both the current
         * coordinates and the specified coordinates `(x3,y3)`,
         * using the specified points `(x1,y1)` and `(x2,y2)` as
         * B&eacute;zier control points.
         */
        @Synchronized
        fun curveTo(x1: kotlin.Float, y1: kotlin.Float, x2: kotlin.Float, y2: kotlin.Float,
                    x3: kotlin.Float, y3: kotlin.Float) {
            needRoom(true, 6)
            pointTypes[numTypes++] = SEG_CUBICTO
            floatCoords[numCoords++] = x1
            floatCoords[numCoords++] = y1
            floatCoords[numCoords++] = x2
            floatCoords[numCoords++] = y2
            floatCoords[numCoords++] = x3
            floatCoords[numCoords++] = y3
        }

        override fun pointCrossings(px: kotlin.Double, py: kotlin.Double): Int {
            val movx: kotlin.Double
            val movy: kotlin.Double
            val curx: kotlin.Double
            val cury: kotlin.Double
            val coords = floatCoords
            movx = coords[0].toDouble()
            curx = movx
            movy = coords[1].toDouble()
            cury = movy
            val crossings = 0
            return crossings
        }

        override fun rectCrossings(rxmin: kotlin.Double, rymin: kotlin.Double,
                                   rxmax: kotlin.Double, rymax: kotlin.Double): Int {
            val coords = floatCoords
            val curx: kotlin.Double
            val cury: kotlin.Double
            val movx: kotlin.Double
            val movy: kotlin.Double
            movx = coords[0].toDouble()
            curx = movx
            movy = coords[1].toDouble()
            cury = movy
            val crossings = 0
            return crossings
        }

        final override fun append(pi: PathIterator, connect: Boolean) {
            var connect = connect
            val coords = FloatArray(6)
            while (!pi.isDone()) {
                when (pi.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO -> {
                        if (!connect || numTypes < 1 || numCoords < 1) {
                            moveTo(coords[0], coords[1])
                        } else if (pointTypes[numTypes - 1] != SEG_CLOSE
                            && floatCoords[numCoords - 2] == coords[0]
                            && floatCoords[numCoords - 1] == coords[1]) {
                            // Collapse out initial moveto/lineto
                        } else {
                            // NO BREAK in the original - fall through to lineto
                            lineTo(coords[0], coords[1])
                        }
                    }
                    PathIterator.SEG_LINETO -> lineTo(coords[0], coords[1])
                    PathIterator.SEG_QUADTO -> quadTo(coords[0], coords[1], coords[2], coords[3])
                    PathIterator.SEG_CUBICTO -> curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
                    PathIterator.SEG_CLOSE -> closePath()
                }
                pi.next()
                connect = false
            }
        }

        final override fun transform(at: AffineTransform) {
            at.transform(floatCoords, 0, floatCoords, 0, numCoords / 2)
        }

        @Synchronized
        final override fun getBounds2D(): Rectangle2D {
            var x1: kotlin.Float
            var y1: kotlin.Float
            var x2: kotlin.Float
            var y2: kotlin.Float
            var i = numCoords
            if (i > 0) {
                y2 = floatCoords[--i]
                y1 = y2
                x2 = floatCoords[--i]
                x1 = x2
                while (i > 0) {
                    val y = floatCoords[--i]
                    val x = floatCoords[--i]
                    if (x < x1) x1 = x
                    if (y < y1) y1 = y
                    if (x > x2) x2 = x
                    if (y > y2) y2 = y
                }
            } else {
                y2 = 0.0f
                x2 = y2
                y1 = x2
                x1 = y1
            }
            return Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1)
        }

        /**
         * The iterator for this class is not multi-threaded safe,
         * which means that the `Path2D` class does not
         * guarantee that modifications to the geometry of this
         * `Path2D` object do not affect any iterations of
         * that geometry that are already in process.
         */
        override fun getPathIterator(at: AffineTransform?): PathIterator {
            return if (at == null) {
                CopyIterator(this)
            } else {
                TxIterator(this, at)
            }
        }

        /**
         * Creates a new object of the same class as this object.
         */
        final override fun clone(): Any {
            // Note: It would be nice to have this return Path2D
            // but one of our subclasses (GeneralPath) needs to
            // offer "public Object clone()" for backwards
            // compatibility so we cannot restrict it further.
            // REMIND: Can we do both somehow?
            run {
                return Float(this)
            }
        }

        /**
         * Writes the default serializable fields to the
         * `ObjectOutputStream` followed by an explicit
         * serialization of the path segments stored in this
         * path.
         */
        @Throws(java.io.IOException::class)
        private fun writeObject(s: java.io.ObjectOutputStream) {
            writeObject(s, false)
        }

        /**
         * Reads the default serializable fields from the
         * `ObjectInputStream` followed by an explicit
         * serialization of the path segments stored in this
         * path.
         */
        @Throws(ClassNotFoundException::class, java.io.IOException::class)
        private fun readObject(s: java.io.ObjectInputStream) {
            readObject(s, false)
        }

        internal class CopyIterator(p2df: Float) : Path2D.Iterator(p2df) {
            var floatCoords: FloatArray = p2df.floatCoords

            override fun currentSegment(coords: FloatArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    System.arraycopy(floatCoords, pointIdx, coords, 0, numCoords)
                }
                return type
            }

            override fun currentSegment(coords: DoubleArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    for (i in 0 until numCoords) {
                        coords[i] = floatCoords[pointIdx + i].toDouble()
                    }
                }
                return type
            }
        }

        internal class TxIterator(p2df: Float, at: AffineTransform) : Path2D.Iterator(p2df) {
            var floatCoords: FloatArray = p2df.floatCoords
            var affine: AffineTransform = at

            override fun currentSegment(coords: FloatArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    affine.transform(floatCoords, pointIdx, coords, 0, numCoords / 2)
                }
                return type
            }

            override fun currentSegment(coords: DoubleArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    affine.transform(floatCoords, pointIdx, coords, 0, numCoords / 2)
                }
                return type
            }
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 6990832515060788886L
        }
    }

    /**
     * The `Double` class defines a geometric path with
     * coordinates stored in double precision floating point.
     */
    open class Double : Path2D, Serializable {

        @Transient
        internal lateinit var doubleCoords: DoubleArray

        /**
         * Constructs a new empty double precision `Path2D` object
         * with a default winding rule of [WIND_NON_ZERO].
         */
        constructor() : this(WIND_NON_ZERO, INIT_SIZE)

        /**
         * Constructs a new empty double precision `Path2D` object
         * with the specified winding rule to control operations that
         * require the interior of the path to be defined.
         */
        constructor(rule: Int) : this(rule, INIT_SIZE)

        /**
         * Constructs a new empty double precision `Path2D` object
         * with the specified winding rule and the specified initial
         * capacity to store path segments.
         */
        constructor(rule: Int, initialCapacity: Int) : super(rule, initialCapacity) {
            doubleCoords = DoubleArray(initialCapacity * 2)
        }

        /**
         * Constructs a new double precision `Path2D` object
         * from an arbitrary [Shape] object.
         */
        constructor(s: Shape) : this(s, null)

        /**
         * Constructs a new double precision `Path2D` object
         * from an arbitrary [Shape] object, transformed by an
         * [AffineTransform] object.
         */
        constructor(s: Shape, at: AffineTransform?) : super() {
            if (s is Path2D) {
                val p2d = s
                setWindingRule(p2d.windingRule)
                this.numTypes = p2d.numTypes
                this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.pointTypes.size)
                this.numCoords = p2d.numCoords
                this.doubleCoords = p2d.cloneCoordsDouble(at)
            } else {
                val pi = s.getPathIterator(at)
                setWindingRule(pi.getWindingRule())
                this.pointTypes = ByteArray(INIT_SIZE)
                this.doubleCoords = DoubleArray(INIT_SIZE * 2)
                append(pi, false)
            }
        }

        override fun cloneCoordsFloat(at: AffineTransform?): FloatArray {
            val ret = FloatArray(doubleCoords.size)
            if (at == null) {
                for (i in 0 until numCoords) {
                    ret[i] = doubleCoords[i].toFloat()
                }
            } else {
                at.transform(doubleCoords, 0, ret, 0, numCoords / 2)
            }
            return ret
        }

        override fun cloneCoordsDouble(at: AffineTransform?): DoubleArray {
            val ret: DoubleArray
            if (at == null) {
                ret = Arrays.copyOf(this.doubleCoords, this.doubleCoords.size)
            } else {
                ret = DoubleArray(doubleCoords.size)
                at.transform(doubleCoords, 0, ret, 0, numCoords / 2)
            }
            return ret
        }

        override fun append(x: kotlin.Float, y: kotlin.Float) {
            doubleCoords[numCoords++] = x.toDouble()
            doubleCoords[numCoords++] = y.toDouble()
        }

        override fun append(x: kotlin.Double, y: kotlin.Double) {
            doubleCoords[numCoords++] = x
            doubleCoords[numCoords++] = y
        }

        override fun getPoint(coordindex: Int): Point2D {
            return Point2D.Double(doubleCoords[coordindex], doubleCoords[coordindex + 1])
        }

        override fun needRoom(needMove: Boolean, newCoords: Int) {
            if (needMove && numTypes == 0) {
                // throw new IllegalPathStateException("missing initial moveto "+
                // "in path definition");
            }
            var size = pointTypes.size
            if (numTypes >= size) {
                var grow = size
                if (grow > EXPAND_MAX) {
                    grow = EXPAND_MAX
                }
                pointTypes = Arrays.copyOf(pointTypes, size + grow)
            }
            size = doubleCoords.size
            if (numCoords + newCoords > size) {
                var grow = size
                if (grow > EXPAND_MAX * 2) {
                    grow = EXPAND_MAX * 2
                }
                if (grow < newCoords) {
                    grow = newCoords
                }
                doubleCoords = Arrays.copyOf(doubleCoords, size + grow)
            }
        }

        @Synchronized
        final override fun moveTo(x: kotlin.Double, y: kotlin.Double) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                doubleCoords[numCoords - 2] = x
                doubleCoords[numCoords - 1] = y
            } else {
                needRoom(false, 2)
                pointTypes[numTypes++] = SEG_MOVETO
                doubleCoords[numCoords++] = x
                doubleCoords[numCoords++] = y
            }
        }

        @Synchronized
        final override fun lineTo(x: kotlin.Double, y: kotlin.Double) {
            needRoom(true, 2)
            pointTypes[numTypes++] = SEG_LINETO
            doubleCoords[numCoords++] = x
            doubleCoords[numCoords++] = y
        }

        @Synchronized
        final override fun quadTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
            needRoom(true, 4)
            pointTypes[numTypes++] = SEG_QUADTO
            doubleCoords[numCoords++] = x1
            doubleCoords[numCoords++] = y1
            doubleCoords[numCoords++] = x2
            doubleCoords[numCoords++] = y2
        }

        @Synchronized
        final override fun curveTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double,
                                   x3: kotlin.Double, y3: kotlin.Double) {
            needRoom(true, 6)
            pointTypes[numTypes++] = SEG_CUBICTO
            doubleCoords[numCoords++] = x1
            doubleCoords[numCoords++] = y1
            doubleCoords[numCoords++] = x2
            doubleCoords[numCoords++] = y2
            doubleCoords[numCoords++] = x3
            doubleCoords[numCoords++] = y3
        }

        override fun pointCrossings(px: kotlin.Double, py: kotlin.Double): Int {
            val movx: kotlin.Double
            val movy: kotlin.Double
            val curx: kotlin.Double
            val cury: kotlin.Double
            val coords = doubleCoords
            movx = coords[0]
            curx = movx
            movy = coords[1]
            cury = movy
            val crossings = 0
            return crossings
        }

        override fun rectCrossings(rxmin: kotlin.Double, rymin: kotlin.Double,
                                   rxmax: kotlin.Double, rymax: kotlin.Double): Int {
            val coords = doubleCoords
            val curx: kotlin.Double
            val cury: kotlin.Double
            val movx: kotlin.Double
            val movy: kotlin.Double
            movx = coords[0]
            curx = movx
            movy = coords[1]
            cury = movy
            val crossings = 0
            return crossings
        }

        final override fun append(pi: PathIterator, connect: Boolean) {
            var connect = connect
            val coords = DoubleArray(6)
            while (!pi.isDone()) {
                when (pi.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO -> {
                        if (!connect || numTypes < 1 || numCoords < 1) {
                            moveTo(coords[0], coords[1])
                        } else if (pointTypes[numTypes - 1] != SEG_CLOSE
                            && doubleCoords[numCoords - 2] == coords[0]
                            && doubleCoords[numCoords - 1] == coords[1]) {
                            // Collapse out initial moveto/lineto
                        } else {
                            // NO BREAK in the original - fall through to lineto
                            lineTo(coords[0], coords[1])
                        }
                    }
                    PathIterator.SEG_LINETO -> lineTo(coords[0], coords[1])
                    PathIterator.SEG_QUADTO -> quadTo(coords[0], coords[1], coords[2], coords[3])
                    PathIterator.SEG_CUBICTO -> curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
                    PathIterator.SEG_CLOSE -> closePath()
                }
                pi.next()
                connect = false
            }
        }

        final override fun transform(at: AffineTransform) {
            at.transform(doubleCoords, 0, doubleCoords, 0, numCoords / 2)
        }

        @Synchronized
        final override fun getBounds2D(): Rectangle2D {
            var x1: kotlin.Double
            var y1: kotlin.Double
            var x2: kotlin.Double
            var y2: kotlin.Double
            var i = numCoords
            if (i > 0) {
                y2 = doubleCoords[--i]
                y1 = y2
                x2 = doubleCoords[--i]
                x1 = x2
                while (i > 0) {
                    val y = doubleCoords[--i]
                    val x = doubleCoords[--i]
                    if (x < x1) x1 = x
                    if (y < y1) y1 = y
                    if (x > x2) x2 = x
                    if (y > y2) y2 = y
                }
            } else {
                y2 = 0.0
                x2 = y2
                y1 = x2
                x1 = y1
            }
            return Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1)
        }

        /**
         * The iterator for this class is not multi-threaded safe,
         * which means that the `Path2D` class does not
         * guarantee that modifications to the geometry of this
         * `Path2D` object do not affect any iterations of
         * that geometry that are already in process.
         */
        override fun getPathIterator(at: AffineTransform?): PathIterator {
            return if (at == null) {
                CopyIterator(this)
            } else {
                TxIterator(this, at)
            }
        }

        /**
         * Creates a new object of the same class as this object.
         */
        final override fun clone(): Any {
            // Note: It would be nice to have this return Path2D
            // but one of our subclasses (GeneralPath) needs to
            // offer "public Object clone()" for backwards
            // compatibility so we cannot restrict it further.
            // REMIND: Can we do both somehow?
            return Double(this)
        }

        /**
         * Writes the default serializable fields to the
         * `ObjectOutputStream` followed by an explicit
         * serialization of the path segments stored in this
         * path.
         */
        @Throws(java.io.IOException::class)
        private fun writeObject(s: java.io.ObjectOutputStream) {
            writeObject(s, true)
        }

        /**
         * Reads the default serializable fields from the
         * `ObjectInputStream` followed by an explicit
         * serialization of the path segments stored in this
         * path.
         */
        @Throws(ClassNotFoundException::class, java.io.IOException::class)
        private fun readObject(s: java.io.ObjectInputStream) {
            readObject(s, true)
        }

        internal class CopyIterator(p2dd: Double) : Path2D.Iterator(p2dd) {
            var doubleCoords: DoubleArray = p2dd.doubleCoords

            override fun currentSegment(coords: FloatArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    for (i in 0 until numCoords) {
                        coords[i] = doubleCoords[pointIdx + i].toFloat()
                    }
                }
                return type
            }

            override fun currentSegment(coords: DoubleArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    System.arraycopy(doubleCoords, pointIdx, coords, 0, numCoords)
                }
                return type
            }
        }

        internal class TxIterator(p2dd: Double, at: AffineTransform) : Path2D.Iterator(p2dd) {
            var doubleCoords: DoubleArray = p2dd.doubleCoords
            var affine: AffineTransform = at

            override fun currentSegment(coords: FloatArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    affine.transform(doubleCoords, pointIdx, coords, 0, numCoords / 2)
                }
                return type
            }

            override fun currentSegment(coords: DoubleArray): Int {
                val type = path.pointTypes[typeIdx].toInt()
                val numCoords = curvecoords[type]
                if (numCoords > 0) {
                    affine.transform(doubleCoords, pointIdx, coords, 0, numCoords / 2)
                }
                return type
            }
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 1826762518450014216L
        }
    }

    /**
     * Adds a point to the path by moving to the specified
     * coordinates specified in double precision.
     */
    abstract fun moveTo(x: kotlin.Double, y: kotlin.Double)

    /**
     * Adds a point to the path by drawing a straight line from the
     * current coordinates to the new specified coordinates
     * specified in double precision.
     */
    abstract fun lineTo(x: kotlin.Double, y: kotlin.Double)

    /**
     * Adds a curved segment, defined by two new points, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the specified coordinates `(x2,y2)`,
     * using the specified point `(x1,y1)` as a quadratic
     * parametric control point.
     */
    abstract fun quadTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double)

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a B&eacute;zier curve that intersects both the current
     * coordinates and the specified coordinates `(x3,y3)`,
     * using the specified points `(x1,y1)` and `(x2,y2)` as
     * B&eacute;zier control points.
     */
    abstract fun curveTo(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double,
                         x3: kotlin.Double, y3: kotlin.Double)

    /**
     * Closes the current subpath by drawing a straight line back to
     * the coordinates of the last `moveTo`.  If the path is already
     * closed then this method has no effect.
     */
    @Synchronized
    fun closePath() {
        if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
            needRoom(true, 0)
            pointTypes[numTypes++] = SEG_CLOSE
        }
    }

    /**
     * Appends the geometry of the specified `Shape` object to the
     * path, possibly connecting the new geometry to the existing path
     * segments with a line segment.
     */
    fun append(s: Shape, connect: Boolean) {
        append(s.getPathIterator(null), connect)
    }

    /**
     * Appends the geometry of the specified
     * [PathIterator] object
     * to the path, possibly connecting the new geometry to the existing
     * path segments with a line segment.
     */
    abstract fun append(pi: PathIterator, connect: Boolean)

    /**
     * Returns the fill style winding rule.
     */
    @Synchronized
    fun getWindingRule(): Int {
        return windingRule
    }

    /**
     * Sets the winding rule of this path to the specified value.
     */
    fun setWindingRule(rule: Int) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw IllegalArgumentException("winding rule must be " + "WIND_EVEN_ODD or "
                    + "WIND_NON_ZERO")
        }
        windingRule = rule
    }

    /**
     * Returns the coordinates most recently added to the end of the path
     * as a [Point2D] object.
     */
    @Synchronized
    fun getCurrentPoint(): Point2D? {
        var index = numCoords
        if (numTypes < 1 || index < 1) {
            return null
        }
        if (pointTypes[numTypes - 1] == SEG_CLOSE) {
            var i = numTypes - 2
            loop@ while (i > 0) {
                when (pointTypes[i]) {
                    SEG_MOVETO -> break@loop
                    SEG_LINETO -> index -= 2
                    SEG_QUADTO -> index -= 4
                    SEG_CUBICTO -> index -= 6
                    SEG_CLOSE -> {
                    }
                }
                i--
            }
        }
        return getPoint(index - 2)
    }

    /**
     * Resets the path to empty.  The append position is set back to the
     * beginning of the path and all coordinates and point types are
     * forgotten.
     */
    @Synchronized
    fun reset() {
        numCoords = 0
        numTypes = numCoords
    }

    /**
     * Transforms the geometry of this path using the specified
     * [AffineTransform].
     */
    abstract fun transform(at: AffineTransform)

    /**
     * Returns a new `Shape` representing a transformed version
     * of this `Path2D`.
     */
    @Synchronized
    fun createTransformedShape(at: AffineTransform?): Shape {
        val p2d = clone() as Path2D
        if (at != null) {
            p2d.transform(at)
        }
        return p2d
    }

    final override fun getBounds(): Rectangle {
        return getBounds2D().getBounds()
    }

    final override fun contains(x: kotlin.Double, y: kotlin.Double): Boolean {
        if (x * 0.0 + y * 0.0 == 0.0) {
            /* N * 0.0 is 0.0 only if N is finite.
             * Here we know that both x and y are finite.
             */
            if (numTypes < 2) {
                return false
            }
            val mask = if (windingRule == WIND_NON_ZERO) -1 else 1
            return ((pointCrossings(x, y) and mask) != 0)
        } else {
            /* Either x or y was infinite or NaN.
             * A NaN always produces a negative response to any test
             * and Infinity values cannot be "inside" any path so
             * they should return false as well.
             */
            return false
        }
    }

    final override fun contains(p: Point2D): Boolean {
        return contains(p.getX(), p.getY())
    }

    final override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false
        }
        if (w <= 0 || h <= 0) {
            return false
        }
        val mask = if (windingRule == WIND_NON_ZERO) -1 else 2
        val crossings = rectCrossings(x, y, x + w, y + h)
        return (crossings != 0 && (crossings and mask) != 0)
    }

    final override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    final override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinte values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false
        }
        if (w <= 0 || h <= 0) {
            return false
        }
        val mask = if (windingRule == WIND_NON_ZERO) -1 else 2
        val crossings = rectCrossings(x, y, x + w, y + h)
        return (crossings == 0 || (crossings and mask) != 0)
    }

    final override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    /**
     * The iterator for this class is not multi-threaded safe,
     * which means that this `Path2D` class does not
     * guarantee that modifications to the geometry of this
     * `Path2D` object do not affect any iterations of
     * that geometry that are already in process.
     */
    override fun getPathIterator(at: AffineTransform?, flatness: kotlin.Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    /**
     * Creates a new object of the same class as this object.
     */
    abstract override fun clone(): Any

    @Throws(java.io.IOException::class)
    internal fun writeObject(s: java.io.ObjectOutputStream, isdbl: Boolean) {
        s.defaultWriteObject()

        val fCoords: FloatArray?
        val dCoords: DoubleArray?

        if (isdbl) {
            dCoords = (this as Double).doubleCoords
            fCoords = null
        } else {
            fCoords = (this as Float).floatCoords
            dCoords = null
        }

        val numTypes = this.numTypes

        s.writeByte((if (isdbl) SERIAL_STORAGE_DBL_ARRAY else SERIAL_STORAGE_FLT_ARRAY).toInt())
        s.writeInt(numTypes)
        s.writeInt(numCoords)
        s.writeByte(windingRule.toByte().toInt())

        var cindex = 0
        for (i in 0 until numTypes) {
            var npoints: Int
            val serialtype: Byte
            when (pointTypes[i]) {
                SEG_MOVETO -> {
                    npoints = 1
                    serialtype = if (isdbl) SERIAL_SEG_DBL_MOVETO else SERIAL_SEG_FLT_MOVETO
                }
                SEG_LINETO -> {
                    npoints = 1
                    serialtype = if (isdbl) SERIAL_SEG_DBL_LINETO else SERIAL_SEG_FLT_LINETO
                }
                SEG_QUADTO -> {
                    npoints = 2
                    serialtype = if (isdbl) SERIAL_SEG_DBL_QUADTO else SERIAL_SEG_FLT_QUADTO
                }
                SEG_CUBICTO -> {
                    npoints = 3
                    serialtype = if (isdbl) SERIAL_SEG_DBL_CUBICTO else SERIAL_SEG_FLT_CUBICTO
                }
                SEG_CLOSE -> {
                    npoints = 0
                    serialtype = SERIAL_SEG_CLOSE
                }
                else -> {
                    // Should never happen
                    throw InternalError("unrecognized path type")
                }
            }
            s.writeByte(serialtype.toInt())
            while (--npoints >= 0) {
                if (isdbl) {
                    s.writeDouble(dCoords!![cindex++])
                    s.writeDouble(dCoords[cindex++])
                } else {
                    s.writeFloat(fCoords!![cindex++])
                    s.writeFloat(fCoords[cindex++])
                }
            }
        }
        s.writeByte(SERIAL_PATH_END.toInt())
    }

    @Throws(ClassNotFoundException::class, java.io.IOException::class)
    internal fun readObject(s: java.io.ObjectInputStream, storedbl: Boolean) {
        s.defaultReadObject()

        // The subclass calls this method with the storage type that
        // they want us to use (storedbl) so we ignore the storage
        // method hint from the stream.
        s.readByte()
        val nT = s.readInt()
        var nC = s.readInt()
        try {
            setWindingRule(s.readByte().toInt())
        } catch (iae: IllegalArgumentException) {
            throw java.io.InvalidObjectException(iae.message)
        }

        pointTypes = ByteArray(if (nT < 0) INIT_SIZE else nT)
        if (nC < 0) {
            nC = INIT_SIZE * 2
        }
        if (storedbl) {
            (this as Double).doubleCoords = DoubleArray(nC)
        } else {
            (this as Float).floatCoords = FloatArray(nC)
        }

        var i = 0
        PATHDONE@ while (nT < 0 || i < nT) {
            val isdbl: Boolean
            var npoints: Int
            val segtype: Byte

            val serialtype = s.readByte()
            when (serialtype) {
                SERIAL_SEG_FLT_MOVETO -> {
                    isdbl = false
                    npoints = 1
                    segtype = SEG_MOVETO
                }
                SERIAL_SEG_FLT_LINETO -> {
                    isdbl = false
                    npoints = 1
                    segtype = SEG_LINETO
                }
                SERIAL_SEG_FLT_QUADTO -> {
                    isdbl = false
                    npoints = 2
                    segtype = SEG_QUADTO
                }
                SERIAL_SEG_FLT_CUBICTO -> {
                    isdbl = false
                    npoints = 3
                    segtype = SEG_CUBICTO
                }
                SERIAL_SEG_DBL_MOVETO -> {
                    isdbl = true
                    npoints = 1
                    segtype = SEG_MOVETO
                }
                SERIAL_SEG_DBL_LINETO -> {
                    isdbl = true
                    npoints = 1
                    segtype = SEG_LINETO
                }
                SERIAL_SEG_DBL_QUADTO -> {
                    isdbl = true
                    npoints = 2
                    segtype = SEG_QUADTO
                }
                SERIAL_SEG_DBL_CUBICTO -> {
                    isdbl = true
                    npoints = 3
                    segtype = SEG_CUBICTO
                }
                SERIAL_SEG_CLOSE -> {
                    isdbl = false
                    npoints = 0
                    segtype = SEG_CLOSE
                }
                SERIAL_PATH_END -> {
                    if (nT < 0) {
                        break@PATHDONE
                    }
                    throw StreamCorruptedException("unexpected PATH_END")
                }
                else -> throw StreamCorruptedException("unrecognized path type")
            }
            needRoom(segtype != SEG_MOVETO, npoints * 2)
            if (isdbl) {
                while (--npoints >= 0) {
                    append(s.readDouble(), s.readDouble())
                }
            } else {
                while (--npoints >= 0) {
                    append(s.readFloat(), s.readFloat())
                }
            }
            pointTypes[numTypes++] = segtype
            i++
        }
        if (nT >= 0 && s.readByte() != SERIAL_PATH_END) {
            throw StreamCorruptedException("missing PATH_END")
        }
    }

    internal abstract class Iterator(@JvmField var path: Path2D) : PathIterator {
        @JvmField
        var typeIdx = 0

        @JvmField
        var pointIdx = 0

        override fun getWindingRule(): Int {
            return path.getWindingRule()
        }

        override fun isDone(): Boolean {
            return (typeIdx >= path.numTypes)
        }

        override fun next() {
            val type = path.pointTypes[typeIdx++].toInt()
            pointIdx += curvecoords[type]
        }

        companion object {
            @JvmField
            val curvecoords = intArrayOf(2, 2, 4, 6, 0)
        }
    }

    companion object {
        /**
         * An even-odd winding rule for determining the interior of
         * a path.
         */
        const val WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD

        /**
         * A non-zero winding rule for determining the interior of a
         * path.
         */
        const val WIND_NON_ZERO = PathIterator.WIND_NON_ZERO

        // For code simplicity, copy these constants to our namespace
        // and cast them to byte constants for easy storage.
        private const val SEG_MOVETO = PathIterator.SEG_MOVETO.toByte()
        private const val SEG_LINETO = PathIterator.SEG_LINETO.toByte()
        private const val SEG_QUADTO = PathIterator.SEG_QUADTO.toByte()
        private const val SEG_CUBICTO = PathIterator.SEG_CUBICTO.toByte()
        private const val SEG_CLOSE = PathIterator.SEG_CLOSE.toByte()

        internal const val INIT_SIZE = 20
        internal const val EXPAND_MAX = 500

        /**
         * Tests if the specified coordinates are inside the closed
         * boundary of the specified [PathIterator].
         */
        @JvmStatic
        fun contains(pi: PathIterator, x: kotlin.Double, y: kotlin.Double): Boolean {
            if (x * 0.0 + y * 0.0 == 0.0) {
                /* N * 0.0 is 0.0 only if N is finite.
                 * Here we know that both x and y are finite.
                 */
                val mask = if (pi.getWindingRule() == WIND_NON_ZERO) -1 else 1
                val cross = 0
                return ((cross and mask) != 0)
            } else {
                /* Either x or y was infinite or NaN.
                 * A NaN always produces a negative response to any test
                 * and Infinity values cannot be "inside" any path so
                 * they should return false as well.
                 */
                return false
            }
        }

        /**
         * Tests if the specified [Point2D] is inside the closed
         * boundary of the specified [PathIterator].
         */
        @JvmStatic
        fun contains(pi: PathIterator, p: Point2D): Boolean {
            return contains(pi, p.getX(), p.getY())
        }

        /**
         * Tests if the specified rectangular area is entirely inside the
         * closed boundary of the specified [PathIterator].
         */
        @JvmStatic
        fun contains(pi: PathIterator, x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
            if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
                /* [xy]+[wh] is NaN if any of those values are NaN,
                 * or if adding the two together would produce NaN
                 * by virtue of adding opposing Infinte values.
                 * Since we need to add them below, their sum must
                 * not be NaN.
                 * We return false because NaN always produces a
                 * negative response to tests
                 */
                return false
            }
            if (w <= 0 || h <= 0) {
                return false
            }
            val mask = if (pi.getWindingRule() == WIND_NON_ZERO) -1 else 2
            val crossings = 0
            return (crossings != 0 && (crossings and mask) != 0)
        }

        /**
         * Tests if the specified [Rectangle2D] is entirely inside the
         * closed boundary of the specified [PathIterator].
         */
        @JvmStatic
        fun contains(pi: PathIterator, r: Rectangle2D): Boolean {
            return contains(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight())
        }

        /**
         * Tests if the interior of the specified [PathIterator]
         * intersects the interior of a specified set of rectangular
         * coordinates.
         */
        @JvmStatic
        fun intersects(pi: PathIterator, x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
            if (java.lang.Double.isNaN(x + w) || java.lang.Double.isNaN(y + h)) {
                /* [xy]+[wh] is NaN if any of those values are NaN,
                 * or if adding the two together would produce NaN
                 * by virtue of adding opposing Infinte values.
                 * Since we need to add them below, their sum must
                 * not be NaN.
                 * We return false because NaN always produces a
                 * negative response to tests
                 */
                return false
            }
            if (w <= 0 || h <= 0) {
                return false
            }
            val mask = if (pi.getWindingRule() == WIND_NON_ZERO) -1 else 2
            val crossings = 0
            return (crossings == 0 || (crossings and mask) != 0)
        }

        /**
         * Tests if the interior of the specified [PathIterator]
         * intersects the interior of a specified [Rectangle2D].
         */
        @JvmStatic
        fun intersects(pi: PathIterator, r: Rectangle2D): Boolean {
            return intersects(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight())
        }

        /*
         * Support fields and methods for serializing the subclasses.
         */
        private const val SERIAL_STORAGE_FLT_ARRAY: Byte = 0x30
        private const val SERIAL_STORAGE_DBL_ARRAY: Byte = 0x31

        private const val SERIAL_SEG_FLT_MOVETO: Byte = 0x40
        private const val SERIAL_SEG_FLT_LINETO: Byte = 0x41
        private const val SERIAL_SEG_FLT_QUADTO: Byte = 0x42
        private const val SERIAL_SEG_FLT_CUBICTO: Byte = 0x43

        private const val SERIAL_SEG_DBL_MOVETO: Byte = 0x50
        private const val SERIAL_SEG_DBL_LINETO: Byte = 0x51
        private const val SERIAL_SEG_DBL_QUADTO: Byte = 0x52
        private const val SERIAL_SEG_DBL_CUBICTO: Byte = 0x53

        private const val SERIAL_SEG_CLOSE: Byte = 0x60
        private const val SERIAL_PATH_END: Byte = 0x61
    }
}
