/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.wxiwei.office.java.awt.geom

import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.Shape
import java.util.NoSuchElementException
import java.util.Vector

open class Area : Shape, Cloneable {

    private var curves: Vector<Any?>

    /**
     * Default constructor which creates an empty area.
     */
    constructor() {
        curves = EmptyCurves
    }

    /**
     * The `Area` class creates an area geometry from the
     * specified [Shape] object.
     */
    constructor(s: Shape) {
        if (s is Area) {
            curves = s.curves
        } else {
            curves = pathToCurves(s.getPathIterator(null))
        }
    }

    open fun add(rhs: Area) {
        curves = AreaOp.AddOp().calculate(this.curves, rhs.curves)
        invalidateBounds()
    }

    open fun subtract(rhs: Area) {
        curves = AreaOp.SubOp().calculate(this.curves, rhs.curves)
        invalidateBounds()
    }

    open fun intersect(rhs: Area) {
        curves = AreaOp.IntOp().calculate(this.curves, rhs.curves)
        invalidateBounds()
    }

    open fun exclusiveOr(rhs: Area) {
        curves = AreaOp.XorOp().calculate(this.curves, rhs.curves)
        invalidateBounds()
    }

    open fun reset() {
        curves = Vector()
        invalidateBounds()
    }

    open fun isEmpty(): Boolean {
        return (curves.size == 0)
    }

    open fun isPolygonal(): Boolean {
        val enum_ = curves.elements()
        while (enum_.hasMoreElements()) {
            if ((enum_.nextElement() as Curve).getOrder() > 1) {
                return false
            }
        }
        return true
    }

    open fun isRectangular(): Boolean {
        val size = curves.size
        if (size == 0) {
            return true
        }
        if (size > 3) {
            return false
        }
        val c1 = curves[1] as Curve
        val c2 = curves[2] as Curve
        if (c1.getOrder() != 1 || c2.getOrder() != 1) {
            return false
        }
        if (c1.getXTop() != c1.getXBot() || c2.getXTop() != c2.getXBot()) {
            return false
        }
        if (c1.getYTop() != c2.getYTop() || c1.getYBot() != c2.getYBot()) {
            // One might be able to prove that this is impossible...
            return false
        }
        return true
    }

    open fun isSingular(): Boolean {
        if (curves.size < 3) {
            return true
        }
        val enum_ = curves.elements()
        enum_.nextElement() // First Order0 "moveto"
        while (enum_.hasMoreElements()) {
            if ((enum_.nextElement() as Curve).getOrder() == 0) {
                return false
            }
        }
        return true
    }

    private var cachedBounds: Rectangle2D? = null

    private fun invalidateBounds() {
        cachedBounds = null
    }

    private fun getCachedBounds(): Rectangle2D {
        if (cachedBounds != null) {
            return cachedBounds!!
        }
        val r: Rectangle2D = Rectangle2D.Double()
        if (curves.size > 0) {
            val c = curves[0] as Curve
            // First point is always an order 0 curve (moveto)
            r.setRect(c.getX0(), c.getY0(), 0.0, 0.0)
            for (i in 1 until curves.size) {
                (curves[i] as Curve).enlarge(r)
            }
        }
        cachedBounds = r
        return r
    }

    override fun getBounds2D(): Rectangle2D {
        return getCachedBounds().getBounds2D()
    }

    override fun getBounds(): Rectangle {
        return getCachedBounds().getBounds()
    }

    public override fun clone(): Any {
        return Area(this)
    }

    open fun equals(other: Area?): Boolean {
        // REMIND: A *much* simpler operation should be possible...
        // Should be able to do a curve-wise comparison since all Areas
        // should evaluate their curves in the same top-down order.
        if (other === this) {
            return true
        }
        if (other == null) {
            return false
        }
        val c = AreaOp.XorOp().calculate(this.curves, other.curves)
        return c.isEmpty()
    }

    open fun transform(t: AffineTransform?) {
        if (t == null) {
            throw NullPointerException("transform must not be null")
        }
        // REMIND: A simpler operation can be performed for some types
        // of transform.
        curves = pathToCurves(getPathIterator(t))
        invalidateBounds()
    }

    open fun createTransformedArea(t: AffineTransform?): Area {
        val a = Area(this)
        a.transform(t)
        return a
    }

    override fun contains(x: Double, y: Double): Boolean {
        if (!getCachedBounds().contains(x, y)) {
            return false
        }
        val enum_ = curves.elements()
        var crossings = 0
        while (enum_.hasMoreElements()) {
            val c = enum_.nextElement() as Curve
            crossings += c.crossingsFor(x, y)
        }
        return ((crossings and 1) == 1)
    }

    override fun contains(p: Point2D): Boolean {
        return contains(p.getX(), p.getY())
    }

    override fun contains(x: Double, y: Double, w: Double, h: Double): Boolean {
        if (w < 0 || h < 0) {
            return false
        }
        if (!getCachedBounds().contains(x, y, w, h)) {
            return false
        }
        val c = Crossings.findCrossings(curves, x, y, x + w, y + h)
        return (c != null && c.covers(y, y + h))
    }

    override fun contains(r: Rectangle2D): Boolean {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun intersects(x: Double, y: Double, w: Double, h: Double): Boolean {
        if (w < 0 || h < 0) {
            return false
        }
        if (!getCachedBounds().intersects(x, y, w, h)) {
            return false
        }
        val c = Crossings.findCrossings(curves, x, y, x + w, y + h)
        return (c == null || !c.isEmpty())
    }

    override fun intersects(r: Rectangle2D): Boolean {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight())
    }

    override fun getPathIterator(at: AffineTransform?): PathIterator {
        return AreaIterator(curves, at)
    }

    override fun getPathIterator(at: AffineTransform?, flatness: Double): PathIterator {
        return FlatteningPathIterator(getPathIterator(at), flatness)
    }

    companion object {
        private val EmptyCurves = Vector<Any?>()

        private fun pathToCurves(pi: PathIterator): Vector<Any?> {
            val curves = Vector<Any?>()
            val windingRule = pi.getWindingRule()
            // coords array is big enough for holding:
            //     coordinates returned from currentSegment (6)
            //     OR
            //         two subdivided quadratic curves (2+4+4=10)
            //         AND
            //             0-1 horizontal splitting parameters
            //             OR
            //             2 parametric equation derivative coefficients
            //     OR
            //         three subdivided cubic curves (2+6+6+6=20)
            //         AND
            //             0-2 horizontal splitting parameters
            //             OR
            //             3 parametric equation derivative coefficients
            val coords = DoubleArray(23)
            var movx = 0.0
            var movy = 0.0
            var curx = 0.0
            var cury = 0.0
            var newx: Double
            var newy: Double
            while (!pi.isDone()) {
                when (pi.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO -> {
                        Curve.insertLine(curves, curx, cury, movx, movy)
                        movx = coords[0]
                        curx = movx
                        movy = coords[1]
                        cury = movy
                        Curve.insertMove(curves, movx, movy)
                    }
                    PathIterator.SEG_LINETO -> {
                        newx = coords[0]
                        newy = coords[1]
                        Curve.insertLine(curves, curx, cury, newx, newy)
                        curx = newx
                        cury = newy
                    }
                    PathIterator.SEG_QUADTO -> {
                        newx = coords[2]
                        newy = coords[3]
                        Curve.insertQuad(curves, curx, cury, coords)
                        curx = newx
                        cury = newy
                    }
                    PathIterator.SEG_CUBICTO -> {
                        newx = coords[4]
                        newy = coords[5]
                        Curve.insertCubic(curves, curx, cury, coords)
                        curx = newx
                        cury = newy
                    }
                    PathIterator.SEG_CLOSE -> {
                        Curve.insertLine(curves, curx, cury, movx, movy)
                        curx = movx
                        cury = movy
                    }
                }
                pi.next()
            }
            Curve.insertLine(curves, curx, cury, movx, movy)
            val operator: AreaOp
            if (windingRule == PathIterator.WIND_EVEN_ODD) {
                operator = AreaOp.EOWindOp()
            } else {
                operator = AreaOp.NZWindOp()
            }
            return operator.calculate(curves, EmptyCurves)
        }
    }
}

internal class AreaIterator(private val curves: Vector<*>, private val transform: AffineTransform?) : PathIterator {
    private var index = 0
    private var prevcurve: Curve? = null
    private var thiscurve: Curve? = null

    init {
        if (curves.size >= 1) {
            thiscurve = curves[0] as Curve
        }
    }

    override fun getWindingRule(): Int {
        // REMIND: Which is better, EVEN_ODD or NON_ZERO?
        //         The paths calculated could be classified either way.
        //return WIND_EVEN_ODD;
        return PathIterator.WIND_NON_ZERO
    }

    override fun isDone(): Boolean {
        return (prevcurve == null && thiscurve == null)
    }

    override fun next() {
        if (prevcurve != null) {
            prevcurve = null
        } else {
            prevcurve = thiscurve
            index++
            if (index < curves.size) {
                thiscurve = curves[index] as Curve
                if (thiscurve!!.getOrder() != 0 &&
                    prevcurve!!.getX1() == thiscurve!!.getX0() &&
                    prevcurve!!.getY1() == thiscurve!!.getY0()) {
                    prevcurve = null
                }
            } else {
                thiscurve = null
            }
        }
    }

    override fun currentSegment(coords: FloatArray): Int {
        val dcoords = DoubleArray(6)
        val segtype = currentSegment(dcoords)
        val numpoints = if (segtype == PathIterator.SEG_CLOSE) 0
        else (if (segtype == PathIterator.SEG_QUADTO) 2
        else (if (segtype == PathIterator.SEG_CUBICTO) 3
        else 1))
        for (i in 0 until numpoints * 2) {
            coords[i] = dcoords[i].toFloat()
        }
        return segtype
    }

    override fun currentSegment(coords: DoubleArray): Int {
        val segtype: Int
        var numpoints: Int
        if (prevcurve != null) {
            // Need to finish off junction between curves
            if (thiscurve == null || thiscurve!!.getOrder() == 0) {
                return PathIterator.SEG_CLOSE
            }
            coords[0] = thiscurve!!.getX0()
            coords[1] = thiscurve!!.getY0()
            segtype = PathIterator.SEG_LINETO
            numpoints = 1
        } else if (thiscurve == null) {
            throw NoSuchElementException("area iterator out of bounds")
        } else {
            segtype = thiscurve!!.getSegment(coords)
            numpoints = thiscurve!!.getOrder()
            if (numpoints == 0) {
                numpoints = 1
            }
        }
        if (transform != null) {
            transform.transform(coords, 0, coords, 0, numpoints)
        }
        return segtype
    }
}
