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

internal class Order3(x0: Double, y0: Double,
                      cx0: Double, cy0: Double,
                      cx1: Double, cy1: Double,
                      x1: Double, y1: Double,
                      direction: Int) : Curve(direction) {

    private val x0: Double
    private val y0: Double
    private val cx0: Double
    private val cy0: Double
    private val cx1: Double
    private val cy1: Double
    private val x1: Double
    private val y1: Double

    private val xmin: Double
    private val xmax: Double

    private val xcoeff0: Double
    private val xcoeff1: Double
    private val xcoeff2: Double
    private val xcoeff3: Double

    private val ycoeff0: Double
    private val ycoeff1: Double
    private val ycoeff2: Double
    private val ycoeff3: Double

    private var TforY1 = 0.0
    private var YforT1 = 0.0
    private var TforY2 = 0.0
    private var YforT2 = 0.0
    private var TforY3 = 0.0
    private var YforT3 = 0.0

    init {
        var cy0 = cy0
        var cy1 = cy1
        // REMIND: Better accuracy in the root finding methods would
        //  ensure that cys are in range.  As it stands, they are never
        //  more than "1 mantissa bit" out of range...
        if (cy0 < y0) cy0 = y0
        if (cy1 > y1) cy1 = y1
        this.x0 = x0
        this.y0 = y0
        this.cx0 = cx0
        this.cy0 = cy0
        this.cx1 = cx1
        this.cy1 = cy1
        this.x1 = x1
        this.y1 = y1
        xmin = Math.min(Math.min(x0, x1), Math.min(cx0, cx1))
        xmax = Math.max(Math.max(x0, x1), Math.max(cx0, cx1))
        xcoeff0 = x0
        xcoeff1 = (cx0 - x0) * 3.0
        xcoeff2 = (cx1 - cx0 - cx0 + x0) * 3.0
        xcoeff3 = x1 - (cx1 - cx0) * 3.0 - x0
        ycoeff0 = y0
        ycoeff1 = (cy0 - y0) * 3.0
        ycoeff2 = (cy1 - cy0 - cy0 + y0) * 3.0
        ycoeff3 = y1 - (cy1 - cy0) * 3.0 - y0
        YforT3 = y0
        YforT2 = y0
        YforT1 = y0
    }

    companion object {
        @JvmStatic
        fun insert(curves: Vector<Any?>, tmp: DoubleArray,
                   x0: Double, y0: Double,
                   cx0: Double, cy0: Double,
                   cx1: Double, cy1: Double,
                   x1: Double, y1: Double,
                   direction: Int) {
            var numparams = getHorizontalParams(y0, cy0, cy1, y1, tmp)
            if (numparams == 0) {
                // We are using addInstance here to avoid inserting horisontal
                // segments
                addInstance(curves, x0, y0, cx0, cy0, cx1, cy1, x1, y1, direction)
                return
            }
            // Store coordinates for splitting at tmp[3..10]
            tmp[3] = x0; tmp[4] = y0
            tmp[5] = cx0; tmp[6] = cy0
            tmp[7] = cx1; tmp[8] = cy1
            tmp[9] = x1; tmp[10] = y1
            var t = tmp[0]
            if (numparams > 1 && t > tmp[1]) {
                // Perform a "2 element sort"...
                tmp[0] = tmp[1]
                tmp[1] = t
                t = tmp[0]
            }
            split(tmp, 3, t)
            if (numparams > 1) {
                // Recalculate tmp[1] relative to the range [tmp[0]...1]
                t = (tmp[1] - t) / (1 - t)
                split(tmp, 9, t)
            }
            var index = 3
            if (direction == DECREASING) {
                index += numparams * 6
            }
            while (numparams >= 0) {
                addInstance(curves,
                    tmp[index + 0], tmp[index + 1],
                    tmp[index + 2], tmp[index + 3],
                    tmp[index + 4], tmp[index + 5],
                    tmp[index + 6], tmp[index + 7],
                    direction)
                numparams--
                if (direction == INCREASING) {
                    index += 6
                } else {
                    index -= 6
                }
            }
        }

        @JvmStatic
        fun addInstance(curves: Vector<Any?>,
                        x0: Double, y0: Double,
                        cx0: Double, cy0: Double,
                        cx1: Double, cy1: Double,
                        x1: Double, y1: Double,
                        direction: Int) {
            if (y0 > y1) {
                curves.add(Order3(x1, y1, cx1, cy1, cx0, cy0, x0, y0, -direction))
            } else if (y1 > y0) {
                curves.add(Order3(x0, y0, cx0, cy0, cx1, cy1, x1, y1, direction))
            }
        }

        /*
         * Return the count of the number of horizontal sections of the
         * specified cubic Bezier curve.  Put the parameters for the
         * horizontal sections into the specified <code>ret</code> array.
         */
        @JvmStatic
        fun getHorizontalParams(c0: Double, cp0: Double,
                                cp1: Double, c1: Double,
                                ret: DoubleArray): Int {
            var c1 = c1
            var cp1 = cp1
            var cp0 = cp0
            if (c0 <= cp0 && cp0 <= cp1 && cp1 <= c1) {
                return 0
            }
            c1 -= cp1
            cp1 -= cp0
            cp0 -= c0
            ret[0] = cp0
            ret[1] = (cp1 - cp0) * 2
            ret[2] = (c1 - cp1 - cp1 + cp0)
            val numroots = QuadCurve2D.solveQuadratic(ret, ret)
            var j = 0
            for (i in 0 until numroots) {
                val t = ret[i]
                // No splits at t==0 and t==1
                if (t > 0 && t < 1) {
                    if (j < i) {
                        ret[j] = t
                    }
                    j++
                }
            }
            return j
        }

        /*
         * Split the cubic Bezier stored at coords[pos...pos+7] representing
         * the parametric range [0..1] into two subcurves representing the
         * parametric subranges [0..t] and [t..1].  Store the results back
         * into the array at coords[pos...pos+7] and coords[pos+6...pos+13].
         */
        @JvmStatic
        fun split(coords: DoubleArray, pos: Int, t: Double) {
            var x0: Double
            var y0: Double
            var cx0: Double
            var cy0: Double
            var cx1: Double
            var cy1: Double
            var x1: Double
            var y1: Double
            x1 = coords[pos + 6]
            coords[pos + 12] = x1
            y1 = coords[pos + 7]
            coords[pos + 13] = y1
            cx1 = coords[pos + 4]
            cy1 = coords[pos + 5]
            x1 = cx1 + (x1 - cx1) * t
            y1 = cy1 + (y1 - cy1) * t
            x0 = coords[pos + 0]
            y0 = coords[pos + 1]
            cx0 = coords[pos + 2]
            cy0 = coords[pos + 3]
            x0 = x0 + (cx0 - x0) * t
            y0 = y0 + (cy0 - y0) * t
            cx0 = cx0 + (cx1 - cx0) * t
            cy0 = cy0 + (cy1 - cy0) * t
            cx1 = cx0 + (x1 - cx0) * t
            cy1 = cy0 + (y1 - cy0) * t
            cx0 = x0 + (cx0 - x0) * t
            cy0 = y0 + (cy0 - y0) * t
            coords[pos + 2] = x0
            coords[pos + 3] = y0
            coords[pos + 4] = cx0
            coords[pos + 5] = cy0
            coords[pos + 6] = cx0 + (cx1 - cx0) * t
            coords[pos + 7] = cy0 + (cy1 - cy0) * t
            coords[pos + 8] = cx1
            coords[pos + 9] = cy1
            coords[pos + 10] = x1
            coords[pos + 11] = y1
        }
    }

    override fun getOrder(): Int {
        return 3
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
        return if (direction == INCREASING) cx0 else cx1
    }

    fun getCY0(): Double {
        return if (direction == INCREASING) cy0 else cy1
    }

    fun getCX1(): Double {
        return if (direction == DECREASING) cx0 else cx1
    }

    fun getCY1(): Double {
        return if (direction == DECREASING) cy0 else cy1
    }

    override fun getX1(): Double {
        return if (direction == DECREASING) x0 else x1
    }

    override fun getY1(): Double {
        return if (direction == DECREASING) y0 else y1
    }

    /*
     * Solve the cubic whose coefficients are in the a,b,c,d fields and
     * return the first root in the range [0, 1].
     * The cubic solved is represented by the equation:
     *     x^3 + (ycoeff2)x^2 + (ycoeff1)x + (ycoeff0) = y
     * @return the first valid root (in the range [0, 1])
     */
    override fun TforY(y: Double): Double {
        if (y <= y0) return 0.0
        if (y >= y1) return 1.0
        if (y == YforT1) return TforY1
        if (y == YforT2) return TforY2
        if (y == YforT3) return TforY3
        // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
        if (ycoeff3 == 0.0) {
            // The cubic degenerated to quadratic (or line or ...).
            return Order2.TforY(y, ycoeff0, ycoeff1, ycoeff2)
        }
        val a = ycoeff2 / ycoeff3
        val b = ycoeff1 / ycoeff3
        val c = (ycoeff0 - y) / ycoeff3
        var Q = (a * a - 3.0 * b) / 9.0
        var R = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0
        val R2 = R * R
        val Q3 = Q * Q * Q
        val a_3 = a / 3.0
        var t: Double
        if (R2 < Q3) {
            val theta = Math.acos(R / Math.sqrt(Q3))
            Q = -2.0 * Math.sqrt(Q)
            t = refine(a, b, c, y, Q * Math.cos(theta / 3.0) - a_3)
            if (t < 0) {
                t = refine(a, b, c, y,
                    Q * Math.cos((theta + Math.PI * 2.0) / 3.0) - a_3)
            }
            if (t < 0) {
                t = refine(a, b, c, y,
                    Q * Math.cos((theta - Math.PI * 2.0) / 3.0) - a_3)
            }
        } else {
            val neg = (R < 0.0)
            val S = Math.sqrt(R2 - Q3)
            if (neg) {
                R = -R
            }
            var A = Math.pow(R + S, 1.0 / 3.0)
            if (!neg) {
                A = -A
            }
            val B = if (A == 0.0) 0.0 else (Q / A)
            t = refine(a, b, c, y, (A + B) - a_3)
        }
        if (t < 0) {
            //throw new InternalError("bad t");
            var t0 = 0.0
            var t1 = 1.0
            while (true) {
                t = (t0 + t1) / 2
                if (t == t0 || t == t1) {
                    break
                }
                val yt = YforT(t)
                if (yt < y) {
                    t0 = t
                } else if (yt > y) {
                    t1 = t
                } else {
                    break
                }
            }
        }
        if (t >= 0) {
            TforY3 = TforY2
            YforT3 = YforT2
            TforY2 = TforY1
            YforT2 = YforT1
            TforY1 = t
            YforT1 = y
        }
        return t
    }

    fun refine(a: Double, b: Double, c: Double,
               target: Double, t: Double): Double {
        var t = t
        if (t < -0.1 || t > 1.1) {
            return -1.0
        }
        var y = YforT(t)
        var t0: Double
        var t1: Double
        if (y < target) {
            t0 = t
            t1 = 1.0
        } else {
            t0 = 0.0
            t1 = t
        }
        var useslope = true
        while (y != target) {
            if (!useslope) {
                val t2 = (t0 + t1) / 2
                if (t2 == t0 || t2 == t1) {
                    break
                }
                t = t2
            } else {
                val slope = dYforT(t, 1)
                if (slope == 0.0) {
                    useslope = false
                    continue
                }
                val t2 = t + ((target - y) / slope)
                if (t2 == t || t2 <= t0 || t2 >= t1) {
                    useslope = false
                    continue
                }
                t = t2
            }
            y = YforT(t)
            if (y < target) {
                t0 = t
            } else if (y > target) {
                t1 = t
            } else {
                break
            }
        }
        return if (t > 1) -1.0 else t
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

    override fun XforT(t: Double): Double {
        return (((xcoeff3 * t) + xcoeff2) * t + xcoeff1) * t + xcoeff0
    }

    override fun YforT(t: Double): Double {
        return (((ycoeff3 * t) + ycoeff2) * t + ycoeff1) * t + ycoeff0
    }

    override fun dXforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> (((xcoeff3 * t) + xcoeff2) * t + xcoeff1) * t + xcoeff0
            1 -> ((3 * xcoeff3 * t) + 2 * xcoeff2) * t + xcoeff1
            2 -> (6 * xcoeff3 * t) + 2 * xcoeff2
            3 -> 6 * xcoeff3
            else -> 0.0
        }
    }

    override fun dYforT(t: Double, deriv: Int): Double {
        return when (deriv) {
            0 -> (((ycoeff3 * t) + ycoeff2) * t + ycoeff1) * t + ycoeff0
            1 -> ((3 * ycoeff3 * t) + 2 * ycoeff2) * t + ycoeff1
            2 -> (6 * ycoeff3 * t) + 2 * ycoeff2
            3 -> 6 * ycoeff3
            else -> 0.0
        }
    }

    override fun nextVertical(t0: Double, t1: Double): Double {
        var t1 = t1
        val eqn = doubleArrayOf(xcoeff1, 2 * xcoeff2, 3 * xcoeff3)
        val numroots = QuadCurve2D.solveQuadratic(eqn, eqn)
        for (i in 0 until numroots) {
            if (eqn[i] > t0 && eqn[i] < t1) {
                t1 = eqn[i]
            }
        }
        return t1
    }

    override fun enlarge(r: Rectangle2D) {
        r.add(x0, y0)
        val eqn = doubleArrayOf(xcoeff1, 2 * xcoeff2, 3 * xcoeff3)
        val numroots = QuadCurve2D.solveQuadratic(eqn, eqn)
        for (i in 0 until numroots) {
            val t = eqn[i]
            if (t > 0 && t < 1) {
                r.add(XforT(t), YforT(t))
            }
        }
        r.add(x1, y1)
    }

    override fun getSubCurve(ystart: Double, yend: Double, dir: Int): Curve {
        if (ystart <= y0 && yend >= y1) {
            return getWithDirection(dir)
        }
        val eqn = DoubleArray(14)
        var t0: Double
        var t1: Double
        t0 = TforY(ystart)
        t1 = TforY(yend)
        eqn[0] = x0
        eqn[1] = y0
        eqn[2] = cx0
        eqn[3] = cy0
        eqn[4] = cx1
        eqn[5] = cy1
        eqn[6] = x1
        eqn[7] = y1
        if (t0 > t1) {
            /* This happens in only rare cases where ystart is
             * very near yend and solving for the yend root ends
             * up stepping slightly lower in t than solving for
             * the ystart root.
             * The simplest solution for now is to just reorder the t
             * values and chop out a miniscule curve piece.
             */
            val t = t0
            t0 = t1
            t1 = t
        }
        if (t1 < 1) {
            split(eqn, 0, t1)
        }
        val i: Int
        if (t0 <= 0) {
            i = 0
        } else {
            split(eqn, 0, t0 / t1)
            i = 6
        }
        return Order3(eqn[i + 0], ystart,
            eqn[i + 2], eqn[i + 3],
            eqn[i + 4], eqn[i + 5],
            eqn[i + 6], yend,
            dir)
    }

    override fun getReversedCurve(): Curve {
        return Order3(x0, y0, cx0, cy0, cx1, cy1, x1, y1, -direction)
    }

    override fun getSegment(coords: DoubleArray): Int {
        if (direction == INCREASING) {
            coords[0] = cx0
            coords[1] = cy0
            coords[2] = cx1
            coords[3] = cy1
            coords[4] = x1
            coords[5] = y1
        } else {
            coords[0] = cx1
            coords[1] = cy1
            coords[2] = cx0
            coords[3] = cy0
            coords[4] = x0
            coords[5] = y0
        }
        return PathIterator.SEG_CUBICTO
    }

    override fun controlPointString(): String {
        return (("(" + Curve.round(getCX0()) + ", " + Curve.round(getCY0()) + "), ") +
                ("(" + Curve.round(getCX1()) + ", " + Curve.round(getCY1()) + "), "))
    }
}
