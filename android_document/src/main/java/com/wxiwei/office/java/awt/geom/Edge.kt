/*
 * Copyright 1998 Sun Microsystems, Inc.  All Rights Reserved.
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

internal class Edge(c: Curve, ctag: Int, etag: Int) {

    @JvmField
    var curve: Curve = c

    @JvmField
    var ctag: Int = ctag

    @JvmField
    var etag: Int = etag

    @JvmField
    var activey = 0.0

    @JvmField
    var equivalence = 0

    constructor(c: Curve, ctag: Int) : this(c, ctag, AreaOp.ETAG_IGNORE)

    fun getCurve(): Curve {
        return curve
    }

    fun getCurveTag(): Int {
        return ctag
    }

    fun getEdgeTag(): Int {
        return etag
    }

    fun setEdgeTag(etag: Int) {
        this.etag = etag
    }

    fun getEquivalence(): Int {
        return equivalence
    }

    fun setEquivalence(eq: Int) {
        equivalence = eq
    }

    private var lastEdge: Edge? = null
    private var lastResult = 0
    private var lastLimit = 0.0

    fun compareTo(other: Edge, yrange: DoubleArray): Int {
        if (other === lastEdge && yrange[0] < lastLimit) {
            if (yrange[1] > lastLimit) {
                yrange[1] = lastLimit
            }
            return lastResult
        }
        if (this === other.lastEdge && yrange[0] < other.lastLimit) {
            if (yrange[1] > other.lastLimit) {
                yrange[1] = other.lastLimit
            }
            return 0 - other.lastResult
        }
        val ret = curve.compareTo(other.curve, yrange)
        lastEdge = other
        lastLimit = yrange[1]
        lastResult = ret
        return ret
    }

    fun record(yend: Double, etag: Int) {
        this.activey = yend
        this.etag = etag
    }

    fun isActiveFor(y: Double, etag: Int): Boolean {
        return (this.etag == etag && this.activey >= y)
    }

    override fun toString(): String {
        return ("Edge[" + curve +
                ", " +
                (if (ctag == AreaOp.CTAG_LEFT) "L" else "R") +
                ", " +
                (if (etag == AreaOp.ETAG_ENTER) "I" else (if (etag == AreaOp.ETAG_EXIT) "O" else "N")) +
                "]")
    }

    companion object {
        const val INIT_PARTS = 4
        const val GROW_PARTS = 10
    }
}
