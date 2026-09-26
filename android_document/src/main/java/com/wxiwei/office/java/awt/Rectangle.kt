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

package com.wxiwei.office.java.awt

import android.graphics.Point
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * A `Rectangle` specifies an area in a coordinate space that is
 * enclosed by the `Rectangle` object's upper-left point
 * `(x,y)`
 * in the coordinate space, its width, and its height.
 */
open class Rectangle(
    /**
     * The X coordinate of the upper-left corner of the `Rectangle`.
     */
    @JvmField var x: Int,
    /**
     * The Y coordinate of the upper-left corner of the `Rectangle`.
     */
    @JvmField var y: Int,
    /**
     * The width of the `Rectangle`.
     */
    @JvmField var width: Int,
    /**
     * The height of the `Rectangle`.
     */
    @JvmField var height: Int
) : Rectangle2D(), Shape, java.io.Serializable {

    companion object {
        /*
         * JDK 1.1 serialVersionUID
         */
        private const val serialVersionUID = -4345857070255674764L

        // Note: the original declared `private static native void initIDs()` and an
        // empty static initializer; neither was ever used.

        // Return best integer representation for v, clipped to integer
        // range and floor-ed or ceiling-ed, depending on the boolean.
        private fun clip(v: kotlin.Double, doceil: Boolean): Int {
            if (v <= Int.MIN_VALUE) {
                return Int.MIN_VALUE
            }
            if (v >= Int.MAX_VALUE) {
                return Int.MAX_VALUE
            }
            return (if (doceil) Math.ceil(v) else Math.floor(v)).toInt()
        }
    }

    /**
     * Constructs a new `Rectangle` whose upper-left corner
     * is at (0,&nbsp;0) in the coordinate space, and whose width and
     * height are both zero.
     */
    constructor() : this(0, 0, 0, 0)

    /**
     * Constructs a new `Rectangle`, initialized to match
     * the values of the specified `Rectangle`.
     */
    constructor(r: Rectangle) : this(r.x, r.y, r.width, r.height)

    /**
     * Constructs a new `Rectangle` whose upper-left corner
     * is at (0,&nbsp;0) in the coordinate space, and whose width and
     * height are specified by the arguments of the same name.
     */
    constructor(width: Int, height: Int) : this(0, 0, width, height)

    /**
     * Constructs a new `Rectangle` whose upper-left corner is
     * specified by the [Point] argument, and
     * whose width and height are specified by the
     * [Dimension] argument.
     */
    constructor(p: Point, d: Dimension) : this(p.x, p.y, d.width, d.height)

    /**
     * Constructs a new `Rectangle` whose upper-left corner is the
     * specified `Point`, and whose width and height are both zero.
     */
    constructor(p: Point) : this(p.x, p.y, 0, 0)

    /**
     * Constructs a new `Rectangle` whose top left corner is
     * (0,&nbsp;0) and whose width and height are specified
     * by the `Dimension` argument.
     */
    constructor(d: Dimension) : this(0, 0, d.width, d.height)

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

    /**
     * Gets the bounding `Rectangle` of this `Rectangle`.
     */
    override fun getBounds(): Rectangle {
        return Rectangle(x, y, width, height)
    }

    override fun getBounds2D(): Rectangle2D {
        return Rectangle(x, y, width, height)
    }

    /**
     * Sets the bounding `Rectangle` of this `Rectangle`
     * to match the specified `Rectangle`.
     */
    open fun setBounds(r: Rectangle) {
        setBounds(r.x, r.y, r.width, r.height)
    }

    /**
     * Sets the bounding `Rectangle` of this
     * `Rectangle` to the specified
     * `x`, `y`, `width`,
     * and `height`.
     */
    open fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        reshape(x, y, width, height)
    }

    /**
     * Sets the bounds of this `Rectangle` to the integer bounds
     * which encompass the specified `x`, `y`, `width`,
     * and `height`.
     */
    override fun setRect(x: kotlin.Double, y: kotlin.Double, width: kotlin.Double, height: kotlin.Double) {
        var width = width
        var height = height
        val newx: Int
        val newy: Int
        val neww: Int
        val newh: Int

        if (x > 2.0 * Int.MAX_VALUE) {
            // Too far in positive X direction to represent...
            // We cannot even reach the left side of the specified
            // rectangle even with both x & width set to MAX_VALUE.
            // The intersection with the "maximal integer rectangle"
            // is non-existant so we should use a width < 0.
            // REMIND: Should we try to determine a more "meaningful"
            // adjusted value for neww than just "-1"?
            newx = Int.MAX_VALUE
            neww = -1
        } else {
            newx = clip(x, false)
            if (width >= 0) width += x - newx
            neww = clip(width, width >= 0)
        }

        if (y > 2.0 * Int.MAX_VALUE) {
            // Too far in positive Y direction to represent...
            newy = Int.MAX_VALUE
            newh = -1
        } else {
            newy = clip(y, false)
            if (height >= 0) height += y - newy
            newh = clip(height, height >= 0)
        }

        reshape(newx, newy, neww, newh)
    }

    /**
     * Sets the bounding `Rectangle` of this
     * `Rectangle` to the specified
     * `x`, `y`, `width`,
     * and `height`.
     */
    @Deprecated("As of JDK version 1.1, replaced by setBounds(int, int, int, int).")
    open fun reshape(x: Int, y: Int, width: Int, height: Int) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    /**
     * Returns the location of this `Rectangle`.
     */
    open fun getLocation(): Point {
        return Point(x, y)
    }

    /**
     * Moves this `Rectangle` to the specified location.
     */
    open fun setLocation(p: Point) {
        setLocation(p.x, p.y)
    }

    /**
     * Moves this `Rectangle` to the specified location.
     */
    open fun setLocation(x: Int, y: Int) {
        move(x, y)
    }

    /**
     * Moves this `Rectangle` to the specified location.
     */
    @Deprecated("As of JDK version 1.1, replaced by setLocation(int, int).")
    open fun move(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    /**
     * Translates this `Rectangle` the indicated distance,
     * to the right along the X coordinate axis, and
     * downward along the Y coordinate axis.
     */
    open fun translate(dx: Int, dy: Int) {
        var oldv = this.x
        var newv = oldv + dx
        if (dx < 0) {
            // moving leftward
            if (newv > oldv) {
                // negative overflow
                // Only adjust width if it was valid (>= 0).
                if (width >= 0) {
                    // The right edge is now conceptually at
                    // newv+width, but we may move newv to prevent
                    // overflow.  But we want the right edge to
                    // remain at its new location in spite of the
                    // clipping.  Think of the following adjustment
                    // conceptually the same as:
                    // width += newv; newv = MIN_VALUE; width -= newv;
                    width += newv - Int.MIN_VALUE
                    // width may go negative if the right edge went past
                    // MIN_VALUE, but it cannot overflow since it cannot
                    // have moved more than MIN_VALUE and any non-negative
                    // number + MIN_VALUE does not overflow.
                }
                newv = Int.MIN_VALUE
            }
        } else {
            // moving rightward (or staying still)
            if (newv < oldv) {
                // positive overflow
                if (width >= 0) {
                    // Conceptually the same as:
                    // width += newv; newv = MAX_VALUE; width -= newv;
                    width += newv - Int.MAX_VALUE
                    // With large widths and large displacements
                    // we may overflow so we need to check it.
                    if (width < 0) width = Int.MAX_VALUE
                }
                newv = Int.MAX_VALUE
            }
        }
        this.x = newv

        oldv = this.y
        newv = oldv + dy
        if (dy < 0) {
            // moving upward
            if (newv > oldv) {
                // negative overflow
                if (height >= 0) {
                    height += newv - Int.MIN_VALUE
                    // See above comment about no overflow in this case
                }
                newv = Int.MIN_VALUE
            }
        } else {
            // moving downward (or staying still)
            if (newv < oldv) {
                // positive overflow
                if (height >= 0) {
                    height += newv - Int.MAX_VALUE
                    if (height < 0) height = Int.MAX_VALUE
                }
                newv = Int.MAX_VALUE
            }
        }
        this.y = newv
    }

    /**
     * Gets the size of this `Rectangle`, represented by
     * the returned `Dimension`.
     */
    open fun getSize(): Dimension {
        return Dimension(width, height)
    }

    /**
     * Sets the size of this `Rectangle` to match the
     * specified `Dimension`.
     */
    open fun setSize(d: Dimension) {
        setSize(d.width, d.height)
    }

    /**
     * Sets the size of this `Rectangle` to the specified
     * width and height.
     */
    open fun setSize(width: Int, height: Int) {
        resize(width, height)
    }

    /**
     * Sets the size of this `Rectangle` to the specified
     * width and height.
     */
    @Deprecated("As of JDK version 1.1, replaced by setSize(int, int).")
    open fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /**
     * Checks whether or not this `Rectangle` contains the
     * specified `Point`.
     */
    open fun contains(p: Point): Boolean {
        return contains(p.x, p.y)
    }

    /**
     * Checks whether or not this `Rectangle` contains the
     * point at the specified location `(x,y)`.
     */
    open fun contains(x: Int, y: Int): Boolean {
        return inside(x, y)
    }

    /**
     * Checks whether or not this `Rectangle` entirely contains
     * the specified `Rectangle`.
     */
    open fun contains(r: Rectangle): Boolean {
        return contains(r.x, r.y, r.width, r.height)
    }

    /**
     * Checks whether this `Rectangle` entirely contains
     * the `Rectangle`
     * at the specified location `(X,Y)` with the
     * specified dimensions `(W,H)`.
     */
    open fun contains(X: Int, Y: Int, W: Int, H: Int): Boolean {
        var W = W
        var H = H
        var w = this.width
        var h = this.height
        if ((w or h or W or H) < 0) {
            // At least one of the dimensions is negative...
            return false
        }
        // Note: if any dimension is zero, tests below must return false...
        val x = this.x
        val y = this.y
        if (X < x || Y < y) {
            return false
        }
        w += x
        W += X
        if (W <= X) {
            // X+W overflowed or W was zero, return false if...
            // either original w or W was zero or
            // x+w did not overflow or
            // the overflowed x+w is smaller than the overflowed X+W
            if (w >= x || W > w) return false
        } else {
            // X+W did not overflow and W was not zero, return false if...
            // original w was zero or
            // x+w did not overflow and x+w is smaller than X+W
            if (w >= x && W > w) return false
        }
        h += y
        H += Y
        if (H <= Y) {
            if (h >= y || H > h) return false
        } else {
            if (h >= y && H > h) return false
        }
        return true
    }

    /**
     * Determines whether or not this `Rectangle` contains
     * the point at the specified location `(X,&nbsp;Y)`.
     */
    @Deprecated("As of JDK version 1.1, replaced by contains(int, int).")
    open fun inside(X: Int, Y: Int): Boolean {
        var w = this.width
        var h = this.height
        if ((w or h) < 0) {
            // At least one of the dimensions is negative...
            return false
        }
        // Note: if either dimension is zero, tests below must return false...
        val x = this.x
        val y = this.y
        if (X < x || Y < y) {
            return false
        }
        w += x
        h += y
        //    overflow || intersect
        return ((w < x || w > X) &&
                (h < y || h > Y))
    }

    /**
     * Determines whether or not this `Rectangle` and the specified
     * `Rectangle` intersect.
     */
    open fun intersects(r: Rectangle): Boolean {
        var tw = this.width
        var th = this.height
        var rw = r.width
        var rh = r.height
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false
        }
        val tx = this.x
        val ty = this.y
        val rx = r.x
        val ry = r.y
        rw += rx
        rh += ry
        tw += tx
        th += ty
        //      overflow || intersect
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry))
    }

    /**
     * Computes the intersection of this `Rectangle` with the
     * specified `Rectangle`.
     */
    open fun intersection(r: Rectangle): Rectangle {
        var tx1 = this.x
        var ty1 = this.y
        val rx1 = r.x
        val ry1 = r.y
        var tx2: Long = tx1.toLong()
        tx2 += this.width
        var ty2: Long = ty1.toLong()
        ty2 += this.height
        var rx2: Long = rx1.toLong()
        rx2 += r.width
        var ry2: Long = ry1.toLong()
        ry2 += r.height
        if (tx1 < rx1) tx1 = rx1
        if (ty1 < ry1) ty1 = ry1
        if (tx2 > rx2) tx2 = rx2
        if (ty2 > ry2) ty2 = ry2
        tx2 -= tx1.toLong()
        ty2 -= ty1.toLong()
        // tx2,ty2 will never overflow (they will never be
        // larger than the smallest of the two source w,h)
        // they might underflow, though...
        if (tx2 < Int.MIN_VALUE) tx2 = Int.MIN_VALUE.toLong()
        if (ty2 < Int.MIN_VALUE) ty2 = Int.MIN_VALUE.toLong()
        return Rectangle(tx1, ty1, tx2.toInt(), ty2.toInt())
    }

    /**
     * Computes the union of this `Rectangle` with the
     * specified `Rectangle`.
     */
    open fun union(r: Rectangle): Rectangle {
        var tx2: Long = this.width.toLong()
        var ty2: Long = this.height.toLong()
        if ((tx2 or ty2) < 0) {
            // This rectangle has negative dimensions...
            // If r has non-negative dimensions then it is the answer.
            // If r is non-existant (has a negative dimension), then both
            // are non-existant and we can return any non-existant rectangle
            // as an answer.  Thus, returning r meets that criterion.
            // Either way, r is our answer.
            return Rectangle(r)
        }
        var rx2: Long = r.width.toLong()
        var ry2: Long = r.height.toLong()
        if ((rx2 or ry2) < 0) {
            return Rectangle(this)
        }
        var tx1 = this.x
        var ty1 = this.y
        tx2 += tx1.toLong()
        ty2 += ty1.toLong()
        val rx1 = r.x
        val ry1 = r.y
        rx2 += rx1.toLong()
        ry2 += ry1.toLong()
        if (tx1 > rx1) tx1 = rx1
        if (ty1 > ry1) ty1 = ry1
        if (tx2 < rx2) tx2 = rx2
        if (ty2 < ry2) ty2 = ry2
        tx2 -= tx1.toLong()
        ty2 -= ty1.toLong()
        // tx2,ty2 will never underflow since both original rectangles
        // were already proven to be non-empty
        // they might overflow, though...
        if (tx2 > Int.MAX_VALUE) tx2 = Int.MAX_VALUE.toLong()
        if (ty2 > Int.MAX_VALUE) ty2 = Int.MAX_VALUE.toLong()
        return Rectangle(tx1, ty1, tx2.toInt(), ty2.toInt())
    }

    /**
     * Adds a point, specified by the integer arguments `newx,newy`
     * to the bounds of this `Rectangle`.
     */
    open fun add(newx: Int, newy: Int) {
        if ((width or height) < 0) {
            this.x = newx
            this.y = newy
            this.height = 0
            this.width = this.height
            return
        }
        var x1 = this.x
        var y1 = this.y
        var x2: Long = this.width.toLong()
        var y2: Long = this.height.toLong()
        x2 += x1.toLong()
        y2 += y1.toLong()
        if (x1 > newx) x1 = newx
        if (y1 > newy) y1 = newy
        if (x2 < newx) x2 = newx.toLong()
        if (y2 < newy) y2 = newy.toLong()
        x2 -= x1.toLong()
        y2 -= y1.toLong()
        if (x2 > Int.MAX_VALUE) x2 = Int.MAX_VALUE.toLong()
        if (y2 > Int.MAX_VALUE) y2 = Int.MAX_VALUE.toLong()
        reshape(x1, y1, x2.toInt(), y2.toInt())
    }

    /**
     * Adds the specified `Point` to the bounds of this
     * `Rectangle`.
     */
    open fun add(pt: Point) {
        add(pt.x, pt.y)
    }

    /**
     * Adds a `Rectangle` to this `Rectangle`.
     */
    open fun add(r: Rectangle) {
        var tx2: Long = this.width.toLong()
        var ty2: Long = this.height.toLong()
        if ((tx2 or ty2) < 0) {
            reshape(r.x, r.y, r.width, r.height)
        }
        var rx2: Long = r.width.toLong()
        var ry2: Long = r.height.toLong()
        if ((rx2 or ry2) < 0) {
            return
        }
        var tx1 = this.x
        var ty1 = this.y
        tx2 += tx1.toLong()
        ty2 += ty1.toLong()
        val rx1 = r.x
        val ry1 = r.y
        rx2 += rx1.toLong()
        ry2 += ry1.toLong()
        if (tx1 > rx1) tx1 = rx1
        if (ty1 > ry1) ty1 = ry1
        if (tx2 < rx2) tx2 = rx2
        if (ty2 < ry2) ty2 = ry2
        tx2 -= tx1.toLong()
        ty2 -= ty1.toLong()
        // tx2,ty2 will never underflow since both original
        // rectangles were non-empty
        // they might overflow, though...
        if (tx2 > Int.MAX_VALUE) tx2 = Int.MAX_VALUE.toLong()
        if (ty2 > Int.MAX_VALUE) ty2 = Int.MAX_VALUE.toLong()
        reshape(tx1, ty1, tx2.toInt(), ty2.toInt())
    }

    /**
     * Resizes the `Rectangle` both horizontally and vertically.
     */
    open fun grow(h: Int, v: Int) {
        var x0: Long = this.x.toLong()
        var y0: Long = this.y.toLong()
        var x1: Long = this.width.toLong()
        var y1: Long = this.height.toLong()
        x1 += x0
        y1 += y0

        x0 -= h.toLong()
        y0 -= v.toLong()
        x1 += h.toLong()
        y1 += v.toLong()

        if (x1 < x0) {
            // Non-existant in X direction
            // Final width must remain negative so subtract x0 before
            // it is clipped so that we avoid the risk that the clipping
            // of x0 will reverse the ordering of x0 and x1.
            x1 -= x0
            if (x1 < Int.MIN_VALUE) x1 = Int.MIN_VALUE.toLong()
            if (x0 < Int.MIN_VALUE) x0 = Int.MIN_VALUE.toLong()
            else if (x0 > Int.MAX_VALUE) x0 = Int.MAX_VALUE.toLong()
        } else { // (x1 >= x0)
            // Clip x0 before we subtract it from x1 in case the clipping
            // affects the representable area of the rectangle.
            if (x0 < Int.MIN_VALUE) x0 = Int.MIN_VALUE.toLong()
            else if (x0 > Int.MAX_VALUE) x0 = Int.MAX_VALUE.toLong()
            x1 -= x0
            // The only way x1 can be negative now is if we clipped
            // x0 against MIN and x1 is less than MIN - in which case
            // we want to leave the width negative since the result
            // did not intersect the representable area.
            if (x1 < Int.MIN_VALUE) x1 = Int.MIN_VALUE.toLong()
            else if (x1 > Int.MAX_VALUE) x1 = Int.MAX_VALUE.toLong()
        }

        if (y1 < y0) {
            // Non-existant in Y direction
            y1 -= y0
            if (y1 < Int.MIN_VALUE) y1 = Int.MIN_VALUE.toLong()
            if (y0 < Int.MIN_VALUE) y0 = Int.MIN_VALUE.toLong()
            else if (y0 > Int.MAX_VALUE) y0 = Int.MAX_VALUE.toLong()
        } else { // (y1 >= y0)
            if (y0 < Int.MIN_VALUE) y0 = Int.MIN_VALUE.toLong()
            else if (y0 > Int.MAX_VALUE) y0 = Int.MAX_VALUE.toLong()
            y1 -= y0
            if (y1 < Int.MIN_VALUE) y1 = Int.MIN_VALUE.toLong()
            else if (y1 > Int.MAX_VALUE) y1 = Int.MAX_VALUE.toLong()
        }

        reshape(x0.toInt(), y0.toInt(), x1.toInt(), y1.toInt())
    }

    override fun isEmpty(): Boolean {
        return (width <= 0) || (height <= 0)
    }

    override fun outcode(x: kotlin.Double, y: kotlin.Double): Int {
        /*
         * Note on casts to double below.  If the arithmetic of
         * x+w or y+h is done in int, then we may get integer
         * overflow. By converting to double before the addition
         * we force the addition to be carried out in double to
         * avoid overflow in the comparison.
         *
         * See bug 4320890 for problems that this can cause.
         */
        var out = 0
        if (this.width <= 0) {
            out = out or (OUT_LEFT or OUT_RIGHT)
        } else if (x < this.x) {
            out = out or OUT_LEFT
        } else if (x > this.x + this.width.toDouble()) {
            out = out or OUT_RIGHT
        }
        if (this.height <= 0) {
            out = out or (OUT_TOP or OUT_BOTTOM)
        } else if (y < this.y) {
            out = out or OUT_TOP
        } else if (y > this.y + this.height.toDouble()) {
            out = out or OUT_BOTTOM
        }
        return out
    }

    override fun createIntersection(r: Rectangle2D): Rectangle2D {
        if (r is Rectangle) {
            return intersection(r)
        }
        val dest: Rectangle2D = Rectangle2D.Double()
        Rectangle2D.intersect(this, r, dest)
        return dest
    }

    override fun createUnion(r: Rectangle2D): Rectangle2D {
        if (r is Rectangle) {
            return union(r)
        }
        val dest: Rectangle2D = Rectangle2D.Double()
        Rectangle2D.union(this, r, dest)
        return dest
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rectangle) {
            val r = other
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height))
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return javaClass.name + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]"
    }
}
