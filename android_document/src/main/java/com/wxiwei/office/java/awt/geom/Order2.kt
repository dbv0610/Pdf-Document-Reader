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

import java.util.Vector

internal class Order2(x0: Double, y0: Double,
                      cx0: Double, cy0: Double,
                      x1: Double, y1: Double,
                      direction: Int) : Curve(direction) {

    private val x0: Double
    private val y0: Double
    private val cx0: Double
    private val cy0: Double
    private val x1: Double
    private val y1: Double
    private val xmin: Double
    private val xmax: Double

    private val xcoeff0: Double
    private val xcoeff1: Double
    private val xcoeff2: Double
    private val ycoeff0: Double
    private val ycoeff1: Double
    private val ycoeff2: Double

    init {
        var cy0 = cy0
        // REMIND: Better accuracy in the root finding methods would
        //  ensure that cy0 is in range.  As it stands, it is never
        //  more than "1 mantissa bit" out of range...
        if (cy0 < y0) {
            cy0 = y0
        } else if (cy0 > y1) {
            cy0 = y1
        }
        this.x0 = x0
        this.y0 = y0
        this.cx0 = cx0
        this.cy0 = cy0
        this.x1 = x1
        this.y1 = y1
        xmin = Math.min(Math.min(x0, x1), cx0)
        xmax = Math.max(Math.max(x0, x1), cx0)
        xcoeff0 = x0
        xcoeff1 = cx0 + cx0 - x0 - x0
        xcoeff2 = x0 - cx0 - cx0 + x1
        ycoeff0 = y0
        ycoeff1 = cy0 + cy0 - y0 - y0
        ycoeff2 = y0 - cy0 - cy0 + y1
    }

    companion object {
        @JvmStatic
        fun insert(curves: Vector<Any?>, tmp: DoubleArray,
                   x0: Double, y0: Double,
                   cx0: Double, cy0: Double,
                   x1: Double, y1: Double,
                   direction: Int) {
            val numparams = getHorizontalParams(y0, cy0, y1, tmp)
            if (numparams == 0) {
                // We are using addInstance here to avoid inserting horisontal
                // segments
                addInstance(curves, x0, y0, cx0, cy0, x1, y1, direction)
                return
            }
            // assert(numparams == 1);
            val t = tmp[0]
            tmp[0] = x0; tmp[1] = y0
            tmp[2] = cx0; tmp[3] = cy0
            tmp[4] = x1; tmp[5] = y1
            split(tmp, 0, t)
            val i0 = if (direction == INCREASING) 0 else 4
            val i1 = 4 - i0
            addInstance(curves, tmp[i0], tmp[i0 + 1], tmp[i0 + 2], tmp[i0 + 3],
                tmp[i0 + 4], tmp[i0 + 5], direction)
            addInstance(curves, tmp[i1], tmp[i1 + 1], tmp[i1 + 2], tmp[i1 + 3],
                tmp[i1 + 4], tmp[i1 + 5], direction)
        }

        @JvmStatic
        fun addInstance(curves: Vector<Any?>,
                        x0: Double, y0: Double,
                        cx0: Double, cy0: Double,
                        x1: Double, y1: Double,
                        direction: Int) {
            if (y0 > y1) {
                curves.add(Order2(x1, y1, cx0, cy0, x0, y0, -direction))
            } else if (y1 > y0) {
                curves.add(Order2(x0, y0, cx0, cy0, x1, y1, direction))
            }
        }

        /*
         * Return the count of the number of horizontal sections of the
         * specified quadratic Bezier curve.  Put the parameters for the
         * horizontal sections into the specified <code>ret</code> array.
         */
        @JvmStatic
        fun getHorizontalParams(c0: Double, cp: Double, c1: Double,
                                ret: DoubleArray): Int {
            var c0 = c0
            var c1 = c1
            if (c0 <= cp && cp <= c1) {
                return 0
            }
            c0 -= cp
            c1 -= cp
            val denom = c0 + c1
            // If denom == 0 then cp == (c0+c1)/2 and we have a line.
            if (denom == 0.0) {
                return 0
            }
            val t = c0 / denom
            // No splits at t==0 and t==1
            if (t <= 0 || t >= 1) {
                return 0
            }
            ret[0] = t
            return 1
        }

        /*
         * Split the quadratic Bezier stored at coords[pos...pos+5] representing
         * the paramtric range [0..1] into two subcurves representing the
         * parametric subranges [0..t] and [t..1].  Store the results back
         * into the array at coords[pos...pos+5] and coords[pos+4...pos+9].
         */
        @JvmStatic
        fun split(coords: DoubleArray, pos: Int, t: Double) {
            var x0: Double
            var y0: Double
            var cx: Double
            var cy: Double
            var x1: Double
            var y1: Double
            x1 = coords[pos + 4]
            coords[pos + 8] = x1
            y1 = coords[pos + 5]
            coords[pos + 9] = y1
            cx = coords[pos + 2]
            cy = coords[pos + 3]
            x1 = cx + (x1 - cx) * t
            y1 = cy + (y1 - cy) * t
            x0 = coords[pos + 0]
            y0 = coords[pos + 1]
            x0 = x0 + (cx - x0) * t
            y0 = y0 + (cy - y0) * t
            cx = x0 + (x1 - x0) * t
            cy = y0 + (y1 - y0) * t
            coords[pos + 2] = x0
            coords[pos + 3] = y0
            coords[pos + 4] = cx
            coords[pos + 5] = cy
            coords[pos + 6] = x1
            coords[pos + 7] = y1
        }

        @JvmStatic
        fun TforY(y: Double,
                  ycoeff0: Double, ycoeff1: Double, ycoeff2: Double): Double {
            var ycoeff0 = ycoeff0
            // The caller should have already eliminated y values
            // outside of the y0 to y1 range.
            ycoeff0 -= y
            if (ycoeff2 == 0.0) {
                // The quadratic parabola has degenerated to a line.
                val root = -ycoeff0 / ycoeff1
                if (root >= 0 && root <= 1) {
                    return root
                }
            } else {
                // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
                var d = ycoeff1 * ycoeff1 - 4.0 * ycoeff2 * ycoeff0
                // If d < 0.0, then there are no roots
                if (d >= 0.0) {
                    d = Math.sqrt(d)
                    // For accuracy, calculate one root using:
                    //     (-ycoeff1 +/- d) / 2ycoeff2
                    // and the other using:
                    //     2ycoeff0 / (-ycoeff1 +/- d)
                    // Choose the sign of the +/- so that ycoeff1+d
                    // gets larger in magnitude
                    if (ycoeff1 < 0.0) {
                        d = -d
                    }
                    val q = (ycoeff1 + d) / -2.0
                    // We already tested ycoeff2 for being 0 above
                    var root = q / ycoeff2
                    if (root >= 0 && root <= 1) {
                        return root
                    }
                    if (q != 0.0) {
                        root = ycoeff0 / q
                        if (root >= 0 && root <= 1) {
                            return root
                        }
                    }
                }
            }
            /* We failed to find a root in [0,1].  Collapse y onto the
             * nearest endpoint (see original comments for rationale).
             */
            val y0 = ycoeff0
            val y1 = ycoeff0 + ycoeff1 + ycoeff2
            return if (0 < (y0 + y1) / 2) 0.0 else 1.0
        }
    }

    override fun getOrder(): Int {
        return 2
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

    fun getCX0(): Double {
        return cx0
    }

    fun getCY0(): Double {
        return cy0
    }

    override fun getX1(): Double {
        return if (direction == DECREASING) x0 else x1
    }

    override fun getY1(): Double {
        return if (direction == DECREASING) y0 else y1
    }

    override fun XforY(y: Double): Double {
        if (y <= y0) {
            return x0
        }
        if (y >= y1) {
            return x1
        }
        return XforT(TforY(y))
    }

    override fun TforY(y: Double): Double {
        if (y <= y0) {
            return 0.0
        }
        if (y >= y1) {
            return 1.0
        }
        return TforY(y, ycoeff0, ycoeff1, ycoeff2)
    }

    override fun XforT(t: Double): Double {
        return (xcoeff2 * t + xcoeff1) * t + xcoeff0
    }

    override fun YforT(t: Double): Double {
        return (ycoeff2 * t + ycoeff1) * t + ycoeff0
    }

    override fun dXforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> (xcoeff2 * t + xcoeff1) * t + xcoeff0
            1 -> 2 * xcoeff2 * t + xcoeff1
            2 -> 2 * xcoeff2
            else -> 0.0
        }
    }

    override fun dYforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> (ycoeff2 * t + ycoeff1) * t + ycoeff0
            1 -> 2 * ycoeff2 * t + ycoeff1
            2 -> 2 * ycoeff2
            else -> 0.0
        }
    }

    override fun nextVertical(t0: Double, t1: Double): Double {
        val t = -xcoeff1 / (2 * xcoeff2)
        if (t > t0 && t < t1) {
            return t
        }
        return t1
    }

    override fun enlarge(r: Rectangle2D) {
        r.add(x0, y0)
        val t = -xcoeff1 / (2 * xcoeff2)
        if (t > 0 && t < 1) {
            r.add(XforT(t), YforT(t))
        }
        r.add(x1, y1)
    }

    override fun getSubCurve(ystart: Double, yend: Double, dir: Int): Curve {
        val t0: Double
        val t1: Double
        if (ystart <= y0) {
            if (yend >= y1) {
                return getWithDirection(dir)
            }
            t0 = 0.0
        } else {
            t0 = TforY(ystart, ycoeff0, ycoeff1, ycoeff2)
        }
        if (yend >= y1) {
            t1 = 1.0
        } else {
            t1 = TforY(yend, ycoeff0, ycoeff1, ycoeff2)
        }
        val eqn = DoubleArray(10)
        eqn[0] = x0
        eqn[1] = y0
        eqn[2] = cx0
        eqn[3] = cy0
        eqn[4] = x1
        eqn[5] = y1
        if (t1 < 1) {
            split(eqn, 0, t1)
        }
        val i: Int
        if (t0 <= 0) {
            i = 0
        } else {
            split(eqn, 0, t0 / t1)
            i = 4
        }
        return Order2(eqn[i + 0], ystart,
            eqn[i + 2], eqn[i + 3],
            eqn[i + 4], yend,
            dir)
    }

    override fun getReversedCurve(): Curve {
        return Order2(x0, y0, cx0, cy0, x1, y1, -direction)
    }

    override fun getSegment(coords: DoubleArray): Int {
        coords[0] = cx0
        coords[1] = cy0
        if (direction == INCREASING) {
            coords[2] = x1
            coords[3] = y1
        } else {
            coords[2] = x0
            coords[3] = y0
        }
        return PathIterator.SEG_QUADTO
    }

    override fun controlPointString(): String {
        return ("(" + Curve.round(cx0) + ", " + Curve.round(cy0) + "), ")
    }
}
