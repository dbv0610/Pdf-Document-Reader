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

internal class Order0(private val x: Double, private val y: Double) : Curve(Curve.INCREASING) {

    override fun getOrder(): Int {
        return 0
    }

    override fun getXTop(): Double {
        return x
    }

    override fun getYTop(): Double {
        return y
    }

    override fun getXBot(): Double {
        return x
    }

    override fun getYBot(): Double {
        return y
    }

    override fun getXMin(): Double {
        return x
    }

    override fun getXMax(): Double {
        return x
    }

    override fun getX0(): Double {
        return x
    }

    override fun getY0(): Double {
        return y
    }

    override fun getX1(): Double {
        return x
    }

    override fun getY1(): Double {
        return y
    }

    override fun XforY(y: Double): Double {
        return y
    }

    override fun TforY(y: Double): Double {
        return 0.0
    }

    override fun XforT(t: Double): Double {
        return x
    }

    override fun YforT(t: Double): Double {
        return y
    }

    override fun dXforT(t: Double, deriv: Int): Double {
        return 0.0
    }

    override fun dYforT(t: Double, deriv: Int): Double {
        return 0.0
    }

    override fun nextVertical(t0: Double, t1: Double): Double {
        return t1
    }

    override fun crossingsFor(x: Double, y: Double): Int {
        return 0
    }

    override fun accumulateCrossings(c: Crossings): Boolean {
        return (x > c.getXLo() && x < c.getXHi() && y > c.getYLo() && y < c.getYHi())
    }

    override fun enlarge(r: Rectangle2D) {
        r.add(x, y)
    }

    override fun getSubCurve(ystart: Double, yend: Double, dir: Int): Curve {
        return this
    }

    override fun getReversedCurve(): Curve {
        return this
    }

    override fun getSegment(coords: DoubleArray): Int {
        coords[0] = x
        coords[1] = y
        return PathIterator.SEG_MOVETO
    }
}
