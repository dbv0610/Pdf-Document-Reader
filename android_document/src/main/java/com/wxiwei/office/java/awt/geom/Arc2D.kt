/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wxiwei.office.java.awt.geom

import java.io.Serializable

/**
 * `Arc2D` is the abstract superclass for all objects that
 * store a 2D arc defined by a framing rectangle,
 * start angle, angular extent (length of the arc), and a closure type
 * (`OPEN`, `CHORD`, or `PIE`).
 */
abstract class Arc2D : RectangularShape {

    /**
     * This class defines an arc specified in `float` precision.
     */
    open class Float : Arc2D, Serializable {
        /**
         * The X coordinate of the upper-left corner of the framing
         * rectangle of the arc.
         */
        @JvmField
        var x = 0f

        /**
         * The Y coordinate of the upper-left corner of the framing
         * rectangle of the arc.
         */
        @JvmField
        var y = 0f

        /**
         * The overall width of the full ellipse of which this arc is
         * a partial section (not considering the
         * angular extents).
         */
        @JvmField
        var width = 0f

        /**
         * The overall height of the full ellipse of which this arc is
         * a partial section (not considering the
         * angular extents).
         */
        @JvmField
        var height = 0f

        /**
         * The starting angle of the arc in degrees.
         */
        @JvmField
        var start = 0f

        /**
         * The angular extent of the arc in degrees.
         */
        @JvmField
        var extent = 0f

        /**
         * Constructs a new OPEN arc, initialized to location (0, 0),
         * size (0, 0), angular extents (start = 0, extent = 0).
         */
        constructor() : super(OPEN)

        /**
         * Constructs a new arc, initialized to location (0, 0),
         * size (0, 0), angular extents (start = 0, extent = 0), and
         * the specified closure type.
         */
        constructor(type: Int) : super(type)

        /**
         * Constructs a new arc, initialized to the specified location,
         * size, angular extents, and closure type.
         */
        constructor(x: kotlin.Float, y: kotlin.Float, w: kotlin.Float, h: kotlin.Float,
                    start: kotlin.Float, extent: kotlin.Float, type: Int) : super(type) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
            this.start = start
            this.extent = extent
        }

        /**
         * Constructs a new arc, initialized to the specified location,
         * size, angular extents, and closure type.
         */
        constructor(ellipseBounds: Rectangle2D, start: kotlin.Float, extent: kotlin.Float, type: Int) : super(type) {
            this.x = ellipseBounds.getX().toFloat()
            this.y = ellipseBounds.getY().toFloat()
            this.width = ellipseBounds.getWidth().toFloat()
            this.height = ellipseBounds.getHeight().toFloat()
            this.start = start
            this.extent = extent
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

        override fun getAngleStart(): kotlin.Double {
            return start.toDouble()
        }

        override fun getAngleExtent(): kotlin.Double {
            return extent.toDouble()
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0 || height <= 0.0)
        }

        override fun setArc(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double,
                            angSt: kotlin.Double, angExt: kotlin.Double, closure: Int) {
            this.setArcType(closure)
            this.x = x.toFloat()
            this.y = y.toFloat()
            this.width = w.toFloat()
            this.height = h.toFloat()
            this.start = angSt.toFloat()
            this.extent = angExt.toFloat()
        }

        override fun setAngleStart(angSt: kotlin.Double) {
            this.start = angSt.toFloat()
        }

        override fun setAngleExtent(angExt: kotlin.Double) {
            this.extent = angExt.toFloat()
        }

        override fun makeBounds(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Rectangle2D {
            return Rectangle2D.Float(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())
        }

        /**
         * Writes the default serializable fields to the
         * `ObjectOutputStream` followed by a byte
         * indicating the arc type of this `Arc2D`
         * instance.
         */
        @Throws(java.io.IOException::class)
        private fun writeObject(s: java.io.ObjectOutputStream) {
            s.defaultWriteObject()
            s.writeByte(getArcType())
        }

        /**
         * Reads the default serializable fields from the
         * `ObjectInputStream` followed by a byte
         * indicating the arc type of this `Arc2D`
         * instance.
         */
        @Throws(ClassNotFoundException::class, java.io.IOException::class)
        private fun readObject(s: java.io.ObjectInputStream) {
            s.defaultReadObject()
            try {
                setArcType(s.readByte().toInt())
            } catch (iae: IllegalArgumentException) {
                throw java.io.InvalidObjectException(iae.message)
            }
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 9130893014586380278L
        }
    }

    /**
     * This class defines an arc specified in `double` precision.
     */
    open class Double : Arc2D, Serializable {
        /**
         * The X coordinate of the upper-left corner of the framing
         * rectangle of the arc.
         */
        @JvmField
        var x = 0.0

        /**
         * The Y coordinate of the upper-left corner of the framing
         * rectangle of the arc.
         */
        @JvmField
        var y = 0.0

        /**
         * The overall width of the full ellipse of which this arc is
         * a partial section (not considering the angular extents).
         */
        @JvmField
        var width = 0.0

        /**
         * The overall height of the full ellipse of which this arc is
         * a partial section (not considering the angular extents).
         */
        @JvmField
        var height = 0.0

        /**
         * The starting angle of the arc in degrees.
         */
        @JvmField
        var start = 0.0

        /**
         * The angular extent of the arc in degrees.
         */
        @JvmField
        var extent = 0.0

        /**
         * Constructs a new OPEN arc, initialized to location (0, 0),
         * size (0, 0), angular extents (start = 0, extent = 0).
         */
        constructor() : super(OPEN)

        /**
         * Constructs a new arc, initialized to location (0, 0),
         * size (0, 0), angular extents (start = 0, extent = 0), and
         * the specified closure type.
         */
        constructor(type: Int) : super(type)

        /**
         * Constructs a new arc, initialized to the specified location,
         * size, angular extents, and closure type.
         */
        constructor(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double,
                    start: kotlin.Double, extent: kotlin.Double, type: Int) : super(type) {
            this.x = x
            this.y = y
            this.width = w
            this.height = h
            this.start = start
            this.extent = extent
        }

        /**
         * Constructs a new arc, initialized to the specified location,
         * size, angular extents, and closure type.
         */
        constructor(ellipseBounds: Rectangle2D, start: kotlin.Double, extent: kotlin.Double, type: Int) : super(type) {
            this.x = ellipseBounds.getX()
            this.y = ellipseBounds.getY()
            this.width = ellipseBounds.getWidth()
            this.height = ellipseBounds.getHeight()
            this.start = start
            this.extent = extent
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

        override fun getAngleStart(): kotlin.Double {
            return start
        }

        override fun getAngleExtent(): kotlin.Double {
            return extent
        }

        override fun isEmpty(): Boolean {
            return (width <= 0.0 || height <= 0.0)
        }

        override fun setArc(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double,
                            angSt: kotlin.Double, angExt: kotlin.Double, closure: Int) {
            this.setArcType(closure)
            this.x = x
            this.y = y
            this.width = w
            this.height = h
            this.start = angSt
            this.extent = angExt
        }

        override fun setAngleStart(angSt: kotlin.Double) {
            this.start = angSt
        }

        override fun setAngleExtent(angExt: kotlin.Double) {
            this.extent = angExt
        }

        override fun makeBounds(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Rectangle2D {
            return Rectangle2D.Double(x, y, w, h)
        }

        /**
         * Writes the default serializable fields to the
         * `ObjectOutputStream` followed by a byte
         * indicating the arc type of this `Arc2D`
         * instance.
         */
        @Throws(java.io.IOException::class)
        private fun writeObject(s: java.io.ObjectOutputStream) {
            s.defaultWriteObject()
            s.writeByte(getArcType())
        }

        /**
         * Reads the default serializable fields from the
         * `ObjectInputStream` followed by a byte
         * indicating the arc type of this `Arc2D`
         * instance.
         */
        @Throws(ClassNotFoundException::class, java.io.IOException::class)
        private fun readObject(s: java.io.ObjectInputStream) {
            s.defaultReadObject()
            try {
                setArcType(s.readByte().toInt())
            } catch (iae: IllegalArgumentException) {
                throw java.io.InvalidObjectException(iae.message)
            }
        }

        companion object {
            /*
             * JDK 1.6 serialVersionUID
             */
            private const val serialVersionUID = 728264085846882001L
        }
    }

    private var type = 0

    /**
     * This is an abstract class that cannot be instantiated directly.
     * Type-specific implementation subclasses are available for
     * instantiation and provide a number of formats for storing
     * the information necessary to satisfy the various accessor
     * methods below.
     *
     * This constructor creates an object with a default closure
     * type of [OPEN].  It is provided only to enable
     * serialization of subclasses.
     */
    internal constructor() : this(OPEN)

    /**
     * This is an abstract class that cannot be instantiated directly.
     * Type-specific implementation subclasses are available for
     * instantiation and provide a number of formats for storing
     * the information necessary to satisfy the various accessor
     * methods below.
     */
    protected constructor(type: Int) : super() {
        setArcType(type)
    }

    /**
     * Returns the starting angle of the arc.
     */
    abstract fun getAngleStart(): kotlin.Double

    /**
     * Returns the angular extent of the arc.
     */
    abstract fun getAngleExtent(): kotlin.Double

    /**
     * Returns the arc closure type of the arc: [OPEN],
     * [CHORD], or [PIE].
     */
    open fun getArcType(): Int {
        return type
    }

    /**
     * Returns the starting point of the arc.  This point is the
     * intersection of the ray from the center defined by the
     * starting angle and the elliptical boundary of the arc.
     */
    open fun getStartPoint(): Point2D {
        val angle = Math.toRadians(-getAngleStart())
        val x = getX() + (Math.cos(angle) * 0.5 + 0.5) * getWidth()
        val y = getY() + (Math.sin(angle) * 0.5 + 0.5) * getHeight()
        return Point2D.Double(x, y)
    }

    /**
     * Returns the ending point of the arc.  This point is the
     * intersection of the ray from the center defined by the
     * starting angle plus the angular extent of the arc and the
     * elliptical boundary of the arc.
     */
    open fun getEndPoint(): Point2D {
        val angle = Math.toRadians(-getAngleStart() - getAngleExtent())
        val x = getX() + (Math.cos(angle) * 0.5 + 0.5) * getWidth()
        val y = getY() + (Math.sin(angle) * 0.5 + 0.5) * getHeight()
        return Point2D.Double(x, y)
    }

    /**
     * Sets the location, size, angular extents, and closure type of
     * this arc to the specified double values.
     */
    abstract fun setArc(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double,
                        angSt: kotlin.Double, angExt: kotlin.Double, closure: Int)

    /**
     * Sets the location, size, angular extents, and closure type of
     * this arc to the specified values.
     */
    open fun setArc(loc: Point2D, size: Dimension2D, angSt: kotlin.Double, angExt: kotlin.Double, closure: Int) {
        setArc(loc.getX(), loc.getY(), size.getWidth(), size.getHeight(), angSt, angExt, closure)
    }

    /**
     * Sets the location, size, angular extents, and closure type of
     * this arc to the specified values.
     */
    open fun setArc(rect: Rectangle2D, angSt: kotlin.Double, angExt: kotlin.Double, closure: Int) {
        setArc(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), angSt, angExt, closure)
    }

    /**
     * Sets this arc to be the same as the specified arc.
     */
    open fun setArc(a: Arc2D) {
        setArc(a.getX(), a.getY(), a.getWidth(), a.getHeight(), a.getAngleStart(),
            a.getAngleExtent(), a.type)
    }

    /**
     * Sets the position, bounds, angular extents, and closure type of
     * this arc to the specified values. The arc is defined by a center
     * point and a radius rather than a framing rectangle for the full ellipse.
     */
    open fun setArcByCenter(x: kotlin.Double, y: kotlin.Double, radius: kotlin.Double,
                            angSt: kotlin.Double, angExt: kotlin.Double, closure: Int) {
        setArc(x - radius, y - radius, radius * 2.0, radius * 2.0, angSt, angExt, closure)
    }

    /**
     * Sets the position, bounds, and angular extents of this arc to the
     * specified value. The starting angle of the arc is tangent to the
     * line specified by points (p1, p2), the ending angle is tangent to
     * the line specified by points (p2, p3), and the arc has the
     * specified radius.
     */
    open fun setArcByTangent(p1: Point2D, p2: Point2D, p3: Point2D, radius: kotlin.Double) {
        var ang1 = Math.atan2(p1.getY() - p2.getY(), p1.getX() - p2.getX())
        var ang2 = Math.atan2(p3.getY() - p2.getY(), p3.getX() - p2.getX())
        var diff = ang2 - ang1
        if (diff > Math.PI) {
            ang2 -= Math.PI * 2.0
        } else if (diff < -Math.PI) {
            ang2 += Math.PI * 2.0
        }
        val bisect = (ang1 + ang2) / 2.0
        val theta = Math.abs(ang2 - bisect)
        val dist = radius / Math.sin(theta)
        val x = p2.getX() + dist * Math.cos(bisect)
        val y = p2.getY() + dist * Math.sin(bisect)
        // REMIND: This needs some work...
        if (ang1 < ang2) {
            ang1 -= Math.PI / 2.0
            ang2 += Math.PI / 2.0
        } else {
            ang1 += Math.PI / 2.0
            ang2 -= Math.PI / 2.0
        }
        ang1 = Math.toDegrees(-ang1)
        ang2 = Math.toDegrees(-ang2)
        diff = ang2 - ang1
        if (diff < 0) {
            diff += 360
        } else {
            diff -= 360
        }
        setArcByCenter(x, y, radius, ang1, diff, type)
    }

    /**
     * Sets the starting angle of this arc to the specified double
     * value.
     */
    abstract fun setAngleStart(angSt: kotlin.Double)

    /**
     * Sets the angular extent of this arc to the specified double
     * value.
     */
    abstract fun setAngleExtent(angExt: kotlin.Double)

    /**
     * Sets the starting angle of this arc to the angle that the
     * specified point defines relative to the center of this arc.
     * The angular extent of the arc will remain the same.
     */
    open fun setAngleStart(p: Point2D) {
        // Bias the dx and dy by the height and width of the oval.
        val dx = getHeight() * (p.getX() - getCenterX())
        val dy = getWidth() * (p.getY() - getCenterY())
        setAngleStart(-Math.toDegrees(Math.atan2(dy, dx)))
    }

    /**
     * Sets the starting angle and angular extent of this arc using two
     * sets of coordinates.
     */
    open fun setAngles(x1: kotlin.Double, y1: kotlin.Double, x2: kotlin.Double, y2: kotlin.Double) {
        val x = getCenterX()
        val y = getCenterY()
        val w = getWidth()
        val h = getHeight()
        // Note: reversing the Y equations negates the angle to adjust
        // for the upside down coordinate system.
        // Also we should bias atans by the height and width of the oval.
        val ang1 = Math.atan2(w * (y - y1), h * (x1 - x))
        var ang2 = Math.atan2(w * (y - y2), h * (x2 - x))
        ang2 -= ang1
        if (ang2 <= 0.0) {
            ang2 += Math.PI * 2.0
        }
        setAngleStart(Math.toDegrees(ang1))
        setAngleExtent(Math.toDegrees(ang2))
    }

    /**
     * Sets the starting angle and angular extent of this arc using
     * two points.
     */
    open fun setAngles(p1: Point2D, p2: Point2D) {
        setAngles(p1.getX(), p1.getY(), p2.getX(), p2.getY())
    }

    /**
     * Sets the closure type of this arc to the specified value:
     * `OPEN`, `CHORD`, or `PIE`.
     */
    open fun setArcType(type: Int) {
        if (type < OPEN || type > PIE) {
            throw IllegalArgumentException("invalid type for Arc: $type")
        }
        this.type = type
    }

    /**
     * Sets the location and size of the outer bounds of this arc
     * to the specified values.
     */
    override fun setFrame(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double) {
        setArc(x, y, w, h, getAngleStart(), getAngleExtent(), type)
    }

    /**
     * Returns the high-precision framing rectangle of the arc.
     */
    override fun getBounds2D(): Rectangle2D {
        if (isEmpty()) {
            return makeBounds(getX(), getY(), getWidth(), getHeight())
        }
        var x1: kotlin.Double
        var y1: kotlin.Double
        var x2: kotlin.Double
        var y2: kotlin.Double
        if (getArcType() == PIE) {
            y2 = 0.0
            x2 = y2
            y1 = x2
            x1 = y1
        } else {
            y1 = 1.0
            x1 = y1
            y2 = -1.0
            x2 = y2
        }
        var angle = 0.0
        for (i in 0..5) {
            if (i < 4) {
                // 0-3 are the four quadrants
                angle += 90.0
                if (!containsAngle(angle)) {
                    continue
                }
            } else if (i == 4) {
                // 4 is start angle
                angle = getAngleStart()
            } else {
                // 5 is end angle
                angle += getAngleExtent()
            }
            val rads = Math.toRadians(-angle)
            val xe = Math.cos(rads)
            val ye = Math.sin(rads)
            x1 = Math.min(x1, xe)
            y1 = Math.min(y1, ye)
            x2 = Math.max(x2, xe)
            y2 = Math.max(y2, ye)
        }
        val w = getWidth()
        val h = getHeight()
        x2 = (x2 - x1) * 0.5 * w
        y2 = (y2 - y1) * 0.5 * h
        x1 = getX() + (x1 * 0.5 + 0.5) * w
        y1 = getY() + (y1 * 0.5 + 0.5) * h
        return makeBounds(x1, y1, x2, y2)
    }

    /**
     * Constructs a `Rectangle2D` of the appropriate precision
     * to hold the parameters calculated to be the framing rectangle
     * of this arc.
     */
    protected abstract fun makeBounds(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Rectangle2D

    /**
     * Determines whether or not the specified angle is within the
     * angular extents of the arc.
     */
    open fun containsAngle(angle: kotlin.Double): Boolean {
        var angle = angle
        var angExt = getAngleExtent()
        val backwards = (angExt < 0.0)
        if (backwards) {
            angExt = -angExt
        }
        if (angExt >= 360.0) {
            return true
        }
        angle = normalizeDegrees(angle) - normalizeDegrees(getAngleStart())
        if (backwards) {
            angle = -angle
        }
        if (angle < 0.0) {
            angle += 360.0
        }

        return (angle >= 0.0) && (angle < angExt)
    }

    /**
     * Determines whether or not the specified point is inside the boundary
     * of the arc.
     */
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
        val distSq = (normx * normx + normy * normy)
        if (distSq >= 0.25) {
            return false
        }
        val angExt = Math.abs(getAngleExtent())
        if (angExt >= 360.0) {
            return true
        }
        val inarc = containsAngle(-Math.toDegrees(Math.atan2(normy, normx)))
        if (type == PIE) {
            return inarc
        }
        // CHORD and OPEN behave the same way
        if (inarc) {
            if (angExt >= 180.0) {
                return true
            }
            // point must be outside the "pie triangle"
        } else {
            if (angExt <= 180.0) {
                return false
            }
            // point must be inside the "pie triangle"
        }
        // The point is inside the pie triangle iff it is on the same
        // side of the line connecting the ends of the arc as the center.
        var angle = Math.toRadians(-getAngleStart())
        val x1 = Math.cos(angle)
        val y1 = Math.sin(angle)
        angle += Math.toRadians(-getAngleExtent())
        val x2 = Math.cos(angle)
        val y2 = Math.sin(angle)
        val inside = (Line2D.relativeCCW(x1, y1, x2, y2, 2 * normx, 2 * normy)
                * Line2D.relativeCCW(x1, y1, x2, y2, 0.0, 0.0) >= 0)
        return if (inarc) !inside else inside
    }

    /**
     * Determines whether or not the interior of the arc intersects
     * the interior of the specified rectangle.
     */
    override fun intersects(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {

        val aw = getWidth()
        val ah = getHeight()

        if (w <= 0 || h <= 0 || aw <= 0 || ah <= 0) {
            return false
        }
        val ext = getAngleExtent()
        if (ext == 0.0) {
            return false
        }

        val ax = getX()
        val ay = getY()
        val axw = ax + aw
        val ayh = ay + ah
        val xw = x + w
        val yh = y + h

        // check bbox
        if (x >= axw || y >= ayh || xw <= ax || yh <= ay) {
            return false
        }

        // extract necessary data
        val axc = getCenterX()
        val ayc = getCenterY()
        val sp = getStartPoint()
        val ep = getEndPoint()
        val sx = sp.getX()
        val sy = sp.getY()
        val ex = ep.getX()
        val ey = ep.getY()

        /*
         * Try to catch rectangles that intersect arc in areas
         * outside of rectagle with left top corner coordinates
         * (min(center x, start point x, end point x),
         *  min(center y, start point y, end point y))
         * and rigth bottom corner coordinates
         * (max(center x, start point x, end point x),
         *  max(center y, start point y, end point y)).
         * So we'll check axis segments outside of rectangle above.
         */
        if (ayc >= y && ayc <= yh) { // 0 and 180
            if ((sx < xw && ex < xw && axc < xw && axw > x && containsAngle(0.0))
                || (sx > x && ex > x && axc > x && ax < xw && containsAngle(180.0))) {
                return true
            }
        }
        if (axc >= x && axc <= xw) { // 90 and 270
            if ((sy > y && ey > y && ayc > y && ay < yh && containsAngle(90.0))
                || (sy < yh && ey < yh && ayc < yh && ayh > y && containsAngle(270.0))) {
                return true
            }
        }

        /*
         * For PIE we should check intersection with pie slices;
         * also we should do the same for arcs with extent is greater
         * than 180, because we should cover case of rectangle, which
         * situated between center of arc and chord, but does not
         * intersect the chord.
         */
        val rect: Rectangle2D = Rectangle2D.Double(x, y, w, h)
        if (type == PIE || Math.abs(ext) > 180) {
            // for PIE: try to find intersections with pie slices
            if (rect.intersectsLine(axc, ayc, sx, sy) || rect.intersectsLine(axc, ayc, ex, ey)) {
                return true
            }
        } else {
            // for CHORD and OPEN: try to find intersections with chord
            if (rect.intersectsLine(sx, sy, ex, ey)) {
                return true
            }
        }

        // finally check the rectangle corners inside the arc
        if (contains(x, y) || contains(x + w, y) || contains(x, y + h) || contains(x + w, y + h)) {
            return true
        }

        return false
    }

    /**
     * Determines whether or not the interior of the arc entirely contains
     * the specified rectangle.
     */
    override fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Boolean {
        return contains(x, y, w, h, null)
    }

    /**
     * Determines whether or not the interior of the arc entirely contains
     * the specified rectangle.
     */
    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r)
    }

    private fun contains(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double,
                         origrect: Rectangle2D?): Boolean {
        var origrect = origrect
        if (!(contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(x + w, y + h))) {
            return false
        }
        // If the shape is convex then we have done all the testing
        // we need.  Only PIE arcs can be concave and then only if
        // the angular extents are greater than 180 degrees.
        if (type != PIE || Math.abs(getAngleExtent()) <= 180.0) {
            return true
        }
        // For a PIE shape we have an additional test for the case where
        // the angular extents are greater than 180 degrees and all four
        // rectangular corners are inside the shape but one of the
        // rectangle edges spans across the "missing wedge" of the arc.
        // We can test for this case by checking if the rectangle intersects
        // either of the pie angle segments.
        if (origrect == null) {
            origrect = Rectangle2D.Double(x, y, w, h)
        }
        val halfW = getWidth() / 2.0
        val halfH = getHeight() / 2.0
        val xc = getX() + halfW
        val yc = getY() + halfH
        var angle = Math.toRadians(-getAngleStart())
        var xe = xc + halfW * Math.cos(angle)
        var ye = yc + halfH * Math.sin(angle)
        if (origrect.intersectsLine(xc, yc, xe, ye)) {
            return false
        }
        angle += Math.toRadians(-getAngleExtent())
        xe = xc + halfW * Math.cos(angle)
        ye = yc + halfH * Math.sin(angle)
        return !origrect.intersectsLine(xc, yc, xe, ye)
    }

    /**
     * Returns an iteration object that defines the boundary of the
     * arc.
     */
    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return ArcIterator(this, at)
    }

    /**
     * Returns the hashcode for this `Arc2D`.
     */
    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(getX())
        bits += java.lang.Double.doubleToLongBits(getY()) * 37
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47
        bits += java.lang.Double.doubleToLongBits(getAngleStart()) * 53
        bits += java.lang.Double.doubleToLongBits(getAngleExtent()) * 59
        bits += (getArcType() * 61).toLong()
        return ((bits.toInt()) xor ((bits shr 32).toInt()))
    }

    /**
     * Determines whether or not the specified `Object` is
     * equal to this `Arc2D`.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Arc2D) {
            val a2d = other
            return ((getX() == a2d.getX()) && (getY() == a2d.getY())
                    && (getWidth() == a2d.getWidth()) && (getHeight() == a2d.getHeight())
                    && (getAngleStart() == a2d.getAngleStart())
                    && (getAngleExtent() == a2d.getAngleExtent()) && (getArcType() == a2d.getArcType()))
        }
        return false
    }

    companion object {
        /**
         * The closure type for an open arc with no path segments
         * connecting the two ends of the arc segment.
         */
        const val OPEN = 0

        /**
         * The closure type for an arc closed by drawing a straight
         * line segment from the start of the arc segment to the end of the
         * arc segment.
         */
        const val CHORD = 1

        /**
         * The closure type for an arc closed by drawing straight line
         * segments from the start of the arc segment to the center
         * of the full ellipse and from that point to the end of the arc segment.
         */
        const val PIE = 2

        /*
         * Normalizes the specified angle into the range -180 to 180.
         */
        @JvmStatic
        internal fun normalizeDegrees(angle: kotlin.Double): kotlin.Double {
            var angle = angle
            if (angle > 180.0) {
                if (angle <= (180.0 + 360.0)) {
                    angle = angle - 360.0
                } else {
                    angle = Math.IEEEremainder(angle, 360.0)
                    // IEEEremainder can return -180 here for some input values...
                    if (angle == -180.0) {
                        angle = 180.0
                    }
                }
            } else if (angle <= -180.0) {
                if (angle > (-180.0 - 360.0)) {
                    angle = angle + 360.0
                } else {
                    angle = Math.IEEEremainder(angle, 360.0)
                    // IEEEremainder can return -180 here for some input values...
                    if (angle == -180.0) {
                        angle = 180.0
                    }
                }
            }
            return angle
        }
    }
}
