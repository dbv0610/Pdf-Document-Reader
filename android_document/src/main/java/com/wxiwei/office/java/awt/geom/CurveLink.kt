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

internal class CurveLink(
    @JvmField var curve: Curve,
    @JvmField var ytop: Double,
    @JvmField var ybot: Double,
    @JvmField var etag: Int
) {

    @JvmField
    var next: CurveLink? = null

    init {
        if (ytop < curve.getYTop() || ybot > curve.getYBot()) {
            throw InternalError("bad curvelink [$ytop=>$ybot] for $curve")
        }
    }

    fun absorb(link: CurveLink): Boolean {
        return absorb(link.curve, link.ytop, link.ybot, link.etag)
    }

    fun absorb(curve: Curve, ystart: Double, yend: Double, etag: Int): Boolean {
        if (this.curve !== curve || this.etag != etag || ybot < ystart || ytop > yend) {
            return false
        }
        if (ystart < curve.getYTop() || yend > curve.getYBot()) {
            throw InternalError("bad curvelink [$ystart=>$yend] for $curve")
        }
        this.ytop = Math.min(ytop, ystart)
        this.ybot = Math.max(ybot, yend)
        return true
    }

    fun isEmpty(): Boolean {
        return (ytop == ybot)
    }

    fun getCurve(): Curve {
        return curve
    }

    fun getSubCurve(): Curve {
        if (ytop == curve.getYTop() && ybot == curve.getYBot()) {
            return curve.getWithDirection(etag)
        }
        return curve.getSubCurve(ytop, ybot, etag)
    }

    fun getMoveto(): Curve {
        return Order0(getXTop(), getYTop())
    }

    fun getXTop(): Double {
        return curve.XforY(ytop)
    }

    fun getYTop(): Double {
        return ytop
    }

    fun getXBot(): Double {
        return curve.XforY(ybot)
    }

    fun getYBot(): Double {
        return ybot
    }

    fun getX(): Double {
        return curve.XforY(ytop)
    }

    fun getEdgeTag(): Int {
        return etag
    }

    fun setNext(link: CurveLink?) {
        this.next = link
    }

    fun getNext(): CurveLink? {
        return next
    }
}
