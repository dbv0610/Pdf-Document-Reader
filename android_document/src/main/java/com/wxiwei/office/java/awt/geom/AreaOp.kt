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

import java.util.Arrays
import java.util.Comparator
import java.util.Vector

abstract class AreaOp private constructor() {

    abstract class CAGOp : AreaOp() {
        var inLeft = false
        var inRight = false
        var inResult = false

        override fun newRow() {
            inLeft = false
            inRight = false
            inResult = false
        }

        internal override fun classify(e: Edge): Int {
            if (e.getCurveTag() == CTAG_LEFT) {
                inLeft = !inLeft
            } else {
                inRight = !inRight
            }
            val newClass = newClassification(inLeft, inRight)
            if (inResult == newClass) {
                return ETAG_IGNORE
            }
            inResult = newClass
            return if (newClass) ETAG_ENTER else ETAG_EXIT
        }

        override fun getState(): Int {
            return if (inResult) RSTAG_INSIDE else RSTAG_OUTSIDE
        }

        abstract fun newClassification(inLeft: Boolean, inRight: Boolean): Boolean
    }

    open class AddOp : CAGOp() {
        override fun newClassification(inLeft: Boolean, inRight: Boolean): Boolean {
            return (inLeft || inRight)
        }
    }

    open class SubOp : CAGOp() {
        override fun newClassification(inLeft: Boolean, inRight: Boolean): Boolean {
            return (inLeft && !inRight)
        }
    }

    open class IntOp : CAGOp() {
        override fun newClassification(inLeft: Boolean, inRight: Boolean): Boolean {
            return (inLeft && inRight)
        }
    }

    open class XorOp : CAGOp() {
        override fun newClassification(inLeft: Boolean, inRight: Boolean): Boolean {
            return (inLeft != inRight)
        }
    }

    open class NZWindOp : AreaOp() {
        private var count = 0

        override fun newRow() {
            count = 0
        }

        internal override fun classify(e: Edge): Int {
            // Note: the right curves should be an empty set with this op...
            // assert(e.getCurveTag() == CTAG_LEFT);
            var newCount = count
            val type = if (newCount == 0) ETAG_ENTER else ETAG_IGNORE
            newCount += e.getCurve().getDirection()
            count = newCount
            return if (newCount == 0) ETAG_EXIT else type
        }

        override fun getState(): Int {
            return if (count == 0) RSTAG_OUTSIDE else RSTAG_INSIDE
        }
    }

    open class EOWindOp : AreaOp() {
        private var inside = false

        override fun newRow() {
            inside = false
        }

        internal override fun classify(e: Edge): Int {
            // Note: the right curves should be an empty set with this op...
            // assert(e.getCurveTag() == CTAG_LEFT);
            val newInside = !inside
            inside = newInside
            return if (newInside) ETAG_ENTER else ETAG_EXIT
        }

        override fun getState(): Int {
            return if (inside) RSTAG_INSIDE else RSTAG_OUTSIDE
        }
    }

    abstract fun newRow()

    internal abstract fun classify(e: Edge): Int

    abstract fun getState(): Int

    open fun calculate(left: Vector<*>, right: Vector<*>): Vector<Any?> {
        var edges = Vector<Any?>()
        addEdges(edges, left, CTAG_LEFT)
        addEdges(edges, right, CTAG_RIGHT)
        edges = pruneEdges(edges)
        if (false) {
            println("result: ")
            val numcurves = edges.size
            val curvelist = edges.toArray(arrayOfNulls<Curve>(numcurves))
            for (i in 0 until numcurves) {
                println("curvelist[" + i + "] = " + curvelist[i])
            }
        }
        return edges
    }

    private fun pruneEdges(edges: Vector<Any?>): Vector<Any?> {
        val numedges = edges.size
        if (numedges < 2) {
            return edges
        }
        val edgelist = edges.toArray(arrayOfNulls<Edge>(numedges))
        Arrays.sort(edgelist, YXTopComparator)
        if (false) {
            println("pruning: ")
            for (i in 0 until numedges) {
                println("edgelist[" + i + "] = " + edgelist[i])
            }
        }
        var e: Edge
        var left = 0
        var right = 0
        var cur: Int
        var next = 0
        val yrange = DoubleArray(2)
        val subcurves = Vector<Any?>()
        val chains = Vector<Any?>()
        val links = Vector<Any?>()
        // Active edges are between left (inclusive) and right (exclusive)
        while (left < numedges) {
            var y = yrange[0]
            // Prune active edges that fall off the top of the active y range
            next = right - 1
            cur = next
            while (cur >= left) {
                e = edgelist[cur]!!
                if (e.getCurve().getYBot() > y) {
                    if (next > cur) {
                        edgelist[next] = e
                    }
                    next--
                }
                cur--
            }
            left = next + 1
            // Grab a new "top of Y range" if the active edges are empty
            if (left >= right) {
                if (right >= numedges) {
                    break
                }
                y = edgelist[right]!!.getCurve().getYTop()
                if (y > yrange[0]) {
                    finalizeSubCurves(subcurves, chains)
                }
                yrange[0] = y
            }
            // Incorporate new active edges that enter the active y range
            while (right < numedges) {
                e = edgelist[right]!!
                if (e.getCurve().getYTop() > y) {
                    break
                }
                right++
            }
            // Sort the current active edges by their X values and
            // determine the maximum valid Y range where the X ordering
            // is correct
            yrange[1] = edgelist[left]!!.getCurve().getYBot()
            if (right < numedges) {
                y = edgelist[right]!!.getCurve().getYTop()
                if (yrange[1] > y) {
                    yrange[1] = y
                }
            }
            if (false) {
                println("current line: y = [" +
                        yrange[0] + ", " + yrange[1] + "]")
                cur = left
                while (cur < right) {
                    println("  " + edgelist[cur])
                    cur++
                }
            }
            // Note: We could start at left+1, but we need to make
            // sure that edgelist[left] has its equivalence set to 0.
            var nexteq = 1
            cur = left
            while (cur < right) {
                e = edgelist[cur]!!
                e.setEquivalence(0)
                next = cur
                while (next > left) {
                    val prevedge = edgelist[next - 1]!!
                    val ordering = e.compareTo(prevedge, yrange)
                    if (yrange[1] <= yrange[0]) {
                        throw InternalError("backstepping to " + yrange[1] +
                                " from " + yrange[0])
                    }
                    if (ordering >= 0) {
                        if (ordering == 0) {
                            // If the curves are equal, mark them to be
                            // deleted later if they cancel each other
                            // out so that we avoid having extraneous
                            // curve segments.
                            var eq = prevedge.getEquivalence()
                            if (eq == 0) {
                                eq = nexteq++
                                prevedge.setEquivalence(eq)
                            }
                            e.setEquivalence(eq)
                        }
                        break
                    }
                    edgelist[next] = prevedge
                    next--
                }
                edgelist[next] = e
                cur++
            }
            if (false) {
                println("current sorted line: y = [" +
                        yrange[0] + ", " + yrange[1] + "]")
                cur = left
                while (cur < right) {
                    println("  " + edgelist[cur])
                    cur++
                }
            }
            // Now prune the active edge list.
            // For each edge in the list, determine its classification
            // (entering shape, exiting shape, ignore - no change) and
            // record the current Y range and its classification in the
            // Edge object for use later in constructing the new outline.
            newRow()
            val ystart = yrange[0]
            val yend = yrange[1]
            cur = left
            while (cur < right) {
                e = edgelist[cur]!!
                var etag: Int
                val eq = e.getEquivalence()
                if (eq != 0) {
                    // Find one of the segments in the "equal" range
                    // with the right transition state and prefer an
                    // edge that was either active up until ystart
                    // or the edge that extends the furthest downward
                    // (i.e. has the most potential for continuation)
                    val origstate = getState()
                    etag = if (origstate == RSTAG_INSIDE)
                        ETAG_EXIT
                    else
                        ETAG_ENTER
                    var activematch: Edge? = null
                    var longestmatch = e
                    var furthesty = yend
                    do {
                        // Note: classify() must be called
                        // on every edge we consume here.
                        classify(e)
                        if (activematch == null &&
                            e.isActiveFor(ystart, etag)) {
                            activematch = e
                        }
                        y = e.getCurve().getYBot()
                        if (y > furthesty) {
                            longestmatch = e
                            furthesty = y
                        }
                        var more = false
                        if (++cur < right) {
                            e = edgelist[cur]!!
                            more = e.getEquivalence() == eq
                        }
                    } while (more)
                    --cur
                    if (getState() == origstate) {
                        etag = ETAG_IGNORE
                    } else {
                        e = activematch ?: longestmatch
                    }
                } else {
                    etag = classify(e)
                }
                if (etag != ETAG_IGNORE) {
                    e.record(yend, etag)
                    links.add(CurveLink(e.getCurve(), ystart, yend, etag))
                }
                cur++
            }
            // assert(getState() == AreaOp.RSTAG_OUTSIDE);
            if (getState() != RSTAG_OUTSIDE) {
                println("Still inside at end of active edge list!")
                println("num curves = " + (right - left))
                println("num links = " + links.size)
                println("y top = " + yrange[0])
                if (right < numedges) {
                    println("y top of next curve = " +
                            edgelist[right]!!.getCurve().getYTop())
                } else {
                    println("no more curves")
                }
                cur = left
                while (cur < right) {
                    e = edgelist[cur]!!
                    println(e)
                    val eq = e.getEquivalence()
                    if (eq != 0) {
                        println("  was equal to $eq...")
                    }
                    cur++
                }
            }
            if (false) {
                println("new links:")
                for (i in 0 until links.size) {
                    val link = links.elementAt(i) as CurveLink
                    println("  " + link.getSubCurve())
                }
            }
            resolveLinks(subcurves, chains, links)
            links.clear()
            // Finally capture the bottom of the valid Y range as the top
            // of the next Y range.
            yrange[0] = yend
        }
        finalizeSubCurves(subcurves, chains)
        val ret = Vector<Any?>()
        val enum_ = subcurves.elements()
        while (enum_.hasMoreElements()) {
            var link = enum_.nextElement() as CurveLink
            ret.add(link.getMoveto())
            var nextlink: CurveLink? = link
            while (true) {
                nextlink = nextlink!!.getNext()
                if (nextlink == null) {
                    break
                }
                if (!link.absorb(nextlink)) {
                    ret.add(link.getSubCurve())
                    link = nextlink
                }
            }
            ret.add(link.getSubCurve())
        }
        return ret
    }

    companion object {
        /* Constants to tag the left and right curves in the edge list */
        const val CTAG_LEFT = 0
        const val CTAG_RIGHT = 1

        /* Constants to classify edges */
        const val ETAG_IGNORE = 0
        const val ETAG_ENTER = 1
        const val ETAG_EXIT = -1

        /* Constants used to classify result state */
        const val RSTAG_INSIDE = 1
        const val RSTAG_OUTSIDE = -1

        private fun addEdges(edges: Vector<Any?>, curves: Vector<*>, curvetag: Int) {
            val enum_ = curves.elements()
            while (enum_.hasMoreElements()) {
                val c = enum_.nextElement() as Curve
                if (c.getOrder() > 0) {
                    edges.add(Edge(c, curvetag))
                }
            }
        }

        private val YXTopComparator: Comparator<Any?> = object : Comparator<Any?> {
            override fun compare(o1: Any?, o2: Any?): Int {
                val c1 = (o1 as Edge).getCurve()
                val c2 = (o2 as Edge).getCurve()
                var v1: Double
                var v2: Double
                v1 = c1.getYTop()
                v2 = c2.getYTop()
                if (v1 == v2) {
                    v1 = c1.getXTop()
                    v2 = c2.getXTop()
                    if (v1 == v2) {
                        return 0
                    }
                }
                if (v1 < v2) {
                    return -1
                }
                return 1
            }
        }

        @JvmStatic
        fun finalizeSubCurves(subcurves: Vector<Any?>, chains: Vector<Any?>) {
            val numchains = chains.size
            if (numchains == 0) {
                return
            }
            if ((numchains and 1) != 0) {
                throw InternalError("Odd number of chains!")
            }
            val endlist = arrayOfNulls<ChainEnd>(numchains)
            chains.toArray(endlist)
            var i = 1
            while (i < numchains) {
                val open = endlist[i - 1]!!
                val close = endlist[i]!!
                val subcurve = open.linkTo(close)
                if (subcurve != null) {
                    subcurves.add(subcurve)
                }
                i += 2
            }
            chains.clear()
        }

        private val EmptyLinkList = arrayOfNulls<CurveLink>(2)
        private val EmptyChainList = arrayOfNulls<ChainEnd>(2)

        @JvmStatic
        fun resolveLinks(subcurves: Vector<Any?>,
                         chains: Vector<Any?>,
                         links: Vector<Any?>) {
            val numlinks = links.size
            val linklist: Array<CurveLink?>
            if (numlinks == 0) {
                linklist = EmptyLinkList
            } else {
                if ((numlinks and 1) != 0) {
                    throw InternalError("Odd number of new curves!")
                }
                linklist = arrayOfNulls(numlinks + 2)
                links.toArray(linklist)
            }
            val numchains = chains.size
            val endlist: Array<ChainEnd?>
            if (numchains == 0) {
                endlist = EmptyChainList
            } else {
                if ((numchains and 1) != 0) {
                    throw InternalError("Odd number of chains!")
                }
                endlist = arrayOfNulls(numchains + 2)
                chains.toArray(endlist)
            }
            var curchain = 0
            var curlink = 0
            chains.clear()
            var chain = endlist[0]
            var nextchain = endlist[1]
            var link = linklist[0]
            var nextlink = linklist[1]
            while (chain != null || link != null) {
                /*
                 * Strategy 1:
                 * Connect chains or links if they are the only things left...
                 */
                var connectchains = (link == null)
                var connectlinks = (chain == null)

                if (!connectchains && !connectlinks) {
                    // assert(link != null && chain != null);
                    /*
                     * Strategy 2:
                     * Connect chains or links if they close off an open area...
                     */
                    connectchains = ((curchain and 1) == 0 &&
                            chain!!.getX() == nextchain!!.getX())
                    connectlinks = ((curlink and 1) == 0 &&
                            link!!.getX() == nextlink!!.getX())

                    if (!connectchains && !connectlinks) {
                        /*
                         * Strategy 3:
                         * Connect chains or links if their successor is
                         * between them and their potential connectee...
                         */
                        val cx = chain!!.getX()
                        val lx = link!!.getX()
                        connectchains = (nextchain != null && cx < lx &&
                                obstructs(nextchain.getX(), lx, curchain))
                        connectlinks = (nextlink != null && lx < cx &&
                                obstructs(nextlink.getX(), cx, curlink))
                    }
                }
                if (connectchains) {
                    val subcurve = chain!!.linkTo(nextchain!!)
                    if (subcurve != null) {
                        subcurves.add(subcurve)
                    }
                    curchain += 2
                    chain = endlist[curchain]
                    nextchain = endlist[curchain + 1]
                }
                if (connectlinks) {
                    val openend = ChainEnd(link!!, null)
                    val closeend = ChainEnd(nextlink!!, openend)
                    openend.setOtherEnd(closeend)
                    chains.add(openend)
                    chains.add(closeend)
                    curlink += 2
                    link = linklist[curlink]
                    nextlink = linklist[curlink + 1]
                }
                if (!connectchains && !connectlinks) {
                    // assert(link != null);
                    // assert(chain != null);
                    // assert(chain.getEtag() == link.getEtag());
                    chain!!.addLink(link!!)
                    chains.add(chain)
                    curchain++
                    chain = nextchain
                    nextchain = endlist[curchain + 1]
                    curlink++
                    link = nextlink
                    nextlink = linklist[curlink + 1]
                }
            }
            if ((chains.size and 1) != 0) {
                println("Odd number of chains!")
            }
        }

        /*
         * Does the position of the next edge at v1 "obstruct" the
         * connectivity between current edge and the potential
         * partner edge which is positioned at v2?
         *
         * Phase tells us whether we are testing for a transition
         * into or out of the interior part of the resulting area.
         *
         * Require 4-connected continuity if this edge and the partner
         * edge are both "entering into" type edges
         * Allow 8-connected continuity for "exiting from" type edges
         */
        @JvmStatic
        fun obstructs(v1: Double, v2: Double, phase: Int): Boolean {
            return if ((phase and 1) == 0) (v1 <= v2) else (v1 < v2)
        }
    }
}
