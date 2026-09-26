/*
 * Copyright 1998-2000 Sun Microsystems, Inc.  All Rights Reserved.
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

internal class Order1(
    private val x0: Double,
    private val y0: Double,
    private val x1: Double,
    private val y1: Double,
    direction: Int
) : Curve(direction) {

    private val xmin: Double
    private val xmax: Double

    init {
        if (x0 < x1) {
            this.xmin = x0
            this.xmax = x1
        } else {
            this.xmin = x1
            this.xmax = x0
        }
    }

    override fun getOrder(): Int {
        return 1
    }

    override fun getXTop(): Double {
        return x0
    }

    override fun getYTop(): Double {
        return y0
    }

    override fun getXBot(): Double {
        return x1
    }

    override fun getYBot(): Double {
        return y1
    }

    override fun getXMin(): Double {
        return xmin
    }

    override fun getXMax(): Double {
        return xmax
    }

    override fun getX0(): Double {
        return if (direction == INCREASING) x0 else x1
    }

    override fun getY0(): Double {
        return if (direction == INCREASING) y0 else y1
    }

    override fun getX1(): Double {
        return if (direction == DECREASING) x0 else x1
    }

    override fun getY1(): Double {
        return if (direction == DECREASING) y0 else y1
    }

    override fun XforY(y: Double): Double {
        if (x0 == x1 || y <= y0) {
            return x0
        }
        if (y >= y1) {
            return x1
        }
        // assert(y0 != y1); /* No horizontal lines... */
        return (x0 + (y - y0) * (x1 - x0) / (y1 - y0))
    }

    override fun TforY(y: Double): Double {
        if (y <= y0) {
            return 0.0
        }
        if (y >= y1) {
            return 1.0
        }
        return (y - y0) / (y1 - y0)
    }

    override fun XforT(t: Double): Double {
        return x0 + t * (x1 - x0)
    }

    override fun YforT(t: Double): Double {
        return y0 + t * (y1 - y0)
    }

    override fun dXforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> x0 + t * (x1 - x0)
            1 -> (x1 - x0)
            else -> 0.0
        }
    }

    override fun dYforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> y0 + t * (y1 - y0)
            1 -> (y1 - y0)
            else -> 0.0
        }
    }

    override fun nextVertical(t0: Double, t1: Double): Double {
        return t1
    }

    override fun accumulateCrossings(c: Crossings): Boolean {
        val xlo = c.getXLo()
        val ylo = c.getYLo()
        val xhi = c.getXHi()
        val yhi = c.getYHi()
        if (xmin >= xhi) {
            return false
        }
        val xstart: Double
        val ystart: Double
        val xend: Double
        val yend: Double
        if (y0 < ylo) {
            if (y1 <= ylo) {
                return false
            }
            ystart = ylo
            xstart = XforY(ylo)
        } else {
            if (y0 >= yhi) {
                return false
            }
            ystart = y0
            xstart = x0
        }
        if (y1 > yhi) {
            yend = yhi
            xend = XforY(yhi)
        } else {
            yend = y1
            xend = x1
        }
        if (xstart >= xhi && xend >= xhi) {
            return false
        }
        if (xstart > xlo || xend > xlo) {
            return true
        }
        c.record(ystart, yend, direction)
        return false
    }

    override fun enlarge(r: Rectangle2D) {
        r.add(x0, y0)
        r.add(x1, y1)
    }

    override fun getSubCurve(ystart: Double, yend: Double, dir: Int): Curve {
        if (ystart == y0 && yend == y1) {
            return getWithDirection(dir)
        }
        if (x0 == x1) {
            return Order1(x0, ystart, x1, yend, dir)
        }
        val num = x0 - x1
        val denom = y0 - y1
        val xstart = (x0 + (ystart - y0) * num / denom)
        val xend = (x0 + (yend - y0) * num / denom)
        return Order1(xstart, ystart, xend, yend, dir)
    }

    override fun getReversedCurve(): Curve {
        return Order1(x0, y0, x1, y1, -direction)
    }

    override fun compareTo(other: Curve, yrange: DoubleArray): Int {
        if (other !is Order1) {
            return super.compareTo(other, yrange)
        }
        if (yrange[1] <= yrange[0]) {
            throw InternalError("yrange already screwed up...")
        }
        yrange[1] = Math.min(Math.min(yrange[1], y1), other.y1)
        if (yrange[1] <= yrange[0]) {
            throw InternalError("backstepping from " + yrange[0] + " to " + yrange[1])
        }
        if (xmax <= other.xmin) {
            return if (xmin == other.xmax) 0 else -1
        }
        if (xmin >= other.xmax) {
            return 1
        }
        /*
         * If "this" is curve A and "other" is curve B, then...
         * y == ((x0A - x0B) * dyA * dyB
         *       - y0A * dxA * dyB + y0B * dxB * dyA)
         *    / (dxB * dyA - dxA * dyB)
         */
        val dxa = x1 - x0
        val dya = y1 - y0
        val dxb = other.x1 - other.x0
        val dyb = other.y1 - other.y0
        val denom = dxb * dya - dxa * dyb
        var y: Double
        if (denom != 0.0) {
            val num = ((x0 - other.x0) * dya * dyb
                    - y0 * dxa * dyb
                    + other.y0 * dxb * dya)
            y = num / denom
            if (y <= yrange[0]) {
                // intersection is above us
                // Use bottom-most common y for comparison
                y = Math.min(y1, other.y1)
            } else {
                // intersection is below the top of our range
                if (y < yrange[1]) {
                    // If intersection is in our range, adjust valid range
                    yrange[1] = y
                }
                // Use top-most common y for comparison
                y = Math.max(y0, other.y0)
            }
        } else {
            // lines are parallel, choose any common y for comparison
            // Note - prefer an endpoint for speed of calculating the X
            // (see shortcuts in Order1.XforY())
            y = Math.max(y0, other.y0)
        }
        return orderof(XforY(y), other.XforY(y))
    }

    override fun getSegment(coords: DoubleArray): Int {
        if (direction == INCREASING) {
            coords[0] = x1
            coords[1] = y1
        } else {
            coords[0] = x0
            coords[1] = y0
        }
        return PathIterator.SEG_LINETO
    }
}
