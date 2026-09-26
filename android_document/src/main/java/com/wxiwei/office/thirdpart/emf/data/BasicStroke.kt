/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.Stroke
import kotlin.Any
import kotlin.Boolean
import kotlin.FloatArray
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.collections.contentEquals
import kotlin.collections.indices
import kotlin.require

/**
 * The `BasicStroke` class defines a basic set of rendering
 * attributes for the outlines of graphics primitives, which are rendered
 * with a [Graphics2D] object that has its Stroke attribute set to
 * this `BasicStroke`.
 * The rendering attributes defined by `BasicStroke` describe
 * the shape of the mark made by a pen drawn along the outline of a
 * [Shape] and the decorations applied at the ends and joins of
 * path segments of the `Shape`.
 * These rendering attributes include:
 * <dl compact>
 * <dt>*width*
</dt> * <dd>The pen width, measured perpendicularly to the pen trajectory.
</dd> * <dt>*end caps*
</dt> * <dd>The decoration applied to the ends of unclosed subpaths and
 * dash segments.  Subpaths that start and end on the same point are
 * still considered unclosed if they do not have a CLOSE segment.
 * See [SEG_CLOSE][java.awt.geom.PathIterator.SEG_CLOSE]
 * for more information on the CLOSE segment.
 * The three different decorations are: [.CAP_BUTT],
 * [.CAP_ROUND], and [.CAP_SQUARE].
</dd> * <dt>*line joins*
</dt> * <dd>The decoration applied at the intersection of two path segments
 * and at the intersection of the endpoints of a subpath that is closed
 * using [SEG_CLOSE][java.awt.geom.PathIterator.SEG_CLOSE].
 * The three different decorations are: [.JOIN_BEVEL],
 * [.JOIN_MITER], and [.JOIN_ROUND].
</dd> * <dt>*miter limit*
</dt> * <dd>The limit to trim a line join that has a JOIN_MITER decoration.
 * A line join is trimmed when the ratio of miter length to stroke
 * width is greater than the miterlimit value.  The miter length is
 * the diagonal length of the miter, which is the distance between
 * the inside corner and the outside corner of the intersection.
 * The smaller the angle formed by two line segments, the longer
 * the miter length and the sharper the angle of intersection.  The
 * default miterlimit value of 10.0f causes all angles less than
 * 11 degrees to be trimmed.  Trimming miters converts
 * the decoration of the line join to bevel.
</dd> * <dt>*dash attributes*
</dt> * <dd>The definition of how to make a dash pattern by alternating
 * between opaque and transparent sections.
</dd></dl> * 
 * All attributes that specify measurements and distances controlling
 * the shape of the returned outline are measured in the same
 * coordinate system as the original unstroked `Shape`
 * argument.  When a `Graphics2D` object uses a
 * `Stroke` object to redefine a path during the execution
 * of one of its `draw` methods, the geometry is supplied
 * in its original form before the `Graphics2D` transform
 * attribute is applied.  Therefore, attributes such as the pen width
 * are interpreted in the user space coordinate system of the
 * `Graphics2D` object and are subject to the scaling and
 * shearing effects of the user-space-to-device-space transform in that
 * particular `Graphics2D`.
 * For example, the width of a rendered shape's outline is determined
 * not only by the width attribute of this `BasicStroke`,
 * but also by the transform attribute of the
 * `Graphics2D` object.  Consider this code:
 * <blockquote><tt>
 * // sets the Graphics2D object's Tranform attribute
 * g2d.scale(10, 10);
 * // sets the Graphics2D object's Stroke attribute
 * g2d.setStroke(new BasicStroke(1.5f));
</tt></blockquote> * 
 * Assuming there are no other scaling transforms added to the
 * `Graphics2D` object, the resulting line
 * will be approximately 15 pixels wide.
 * As the example code demonstrates, a floating-point line
 * offers better precision, especially when large transforms are
 * used with a `Graphics2D` object.
 * When a line is diagonal, the exact width depends on how the
 * rendering pipeline chooses which pixels to fill as it traces the
 * theoretical widened outline.  The choice of which pixels to turn
 * on is affected by the antialiasing attribute because the
 * antialiasing rendering pipeline can choose to color
 * partially-covered pixels.
 * 
 * 
 * For more information on the user space coordinate system and the
 * rendering process, see the `Graphics2D` class comments.
 * @see Graphics2D
 * 
 * @version %I%, %G%
 * @author Jim Graham
 */
class BasicStroke @JvmOverloads constructor(
    width: Float = 1.0f, cap: Int = CAP_SQUARE, join: Int = JOIN_MITER, miterlimit: Float = 10.0f,
    dash: FloatArray? = null, dash_phase: Float = 0.0f
) : Stroke {
    /**
     * Returns the line width.  Line width is represented in user space,
     * which is the default-coordinate space used by Java 2D.  See the
     * `Graphics2D` class comments for more information on
     * the user space coordinate system.
     * @return the line width of this `BasicStroke`.
     * @see Graphics2D
     */
    var lineWidth: Float

    /**
     * Returns the line join style.
     * @return the line join style of the `BasicStroke` as one
     * of the static `int` values that define possible line
     * join styles.
     */
    var lineJoin: Int

    /**
     * Returns the end cap style.
     * @return the end cap style of this `BasicStroke` as one
     * of the static `int` values that define possible end cap
     * styles.
     */
    var endCap: Int

    /**
     * Returns the limit of miter joins.
     * @return the limit of miter joins of the `BasicStroke`.
     */
    var miterLimit: Float

    var dash: FloatArray? = null

    /**
     * Returns the current dash phase.
     * The dash phase is a distance specified in user coordinates that
     * represents an offset into the dashing pattern. In other words, the dash
     * phase defines the point in the dashing pattern that will correspond to
     * the beginning of the stroke.
     * @return the dash phase as a `float` value.
     */
    var dashPhase: Float

    /**
     * Constructs a new `BasicStroke` with the specified
     * attributes.
     * @param width the width of this `BasicStroke`.  The
     * width must be greater than or equal to 0.0f.  If width is
     * set to 0.0f, the stroke is rendered as the thinnest
     * possible line for the target device and the antialias
     * hint setting.
     * @param cap the decoration of the ends of a `BasicStroke`
     * @param join the decoration applied where path segments meet
     * @param miterlimit the limit to trim the miter join.  The miterlimit
     * must be greater than or equal to 1.0f.
     * @param dash the array representing the dashing pattern
     * @param dash_phase the offset to start the dashing pattern
     * @throws IllegalArgumentException if `width` is negative
     * @throws IllegalArgumentException if `cap` is not either
     * CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if `miterlimit` is less
     * than 1 and `join` is JOIN_MITER
     * @throws IllegalArgumentException if `join` is not
     * either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     * @throws IllegalArgumentException if `dash_phase`
     * is negative and `dash` is not `null`
     * @throws IllegalArgumentException if the length of
     * `dash` is zero
     * @throws IllegalArgumentException if dash lengths are all zero.
     */
    /**
     * Constructs a new `BasicStroke` with defaults for all
     * attributes.
     * The default attributes are a solid line of width 1.0, CAP_SQUARE,
     * JOIN_MITER, a miter limit of 10.0.
     */
    /**
     * Constructs a solid `BasicStroke` with the specified
     * line width and with default values for the cap and join
     * styles.
     * @param width the width of the `BasicStroke`
     * @throws IllegalArgumentException if `width` is negative
     */
    /**
     * Constructs a solid `BasicStroke` with the specified
     * attributes.  The `miterlimit` parameter is
     * unnecessary in cases where the default is allowable or the
     * line joins are not specified as JOIN_MITER.
     * @param width the width of the `BasicStroke`
     * @param cap the decoration of the ends of a `BasicStroke`
     * @param join the decoration applied where path segments meet
     * @throws IllegalArgumentException if `width` is negative
     * @throws IllegalArgumentException if `cap` is not either
     * CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if `join` is not
     * either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     */
    /**
     * Constructs a solid `BasicStroke` with the specified
     * attributes.
     * @param width the width of the `BasicStroke`
     * @param cap the decoration of the ends of a `BasicStroke`
     * @param join the decoration applied where path segments meet
     * @param miterlimit the limit to trim the miter join
     * @throws IllegalArgumentException if `width` is negative
     * @throws IllegalArgumentException if `cap` is not either
     * CAP_BUTT, CAP_ROUND or CAP_SQUARE
     * @throws IllegalArgumentException if `miterlimit` is less
     * than 1 and `join` is JOIN_MITER
     * @throws IllegalArgumentException if `join` is not
     * either JOIN_ROUND, JOIN_BEVEL, or JOIN_MITER
     */
    init {
        require(!(width < 0.0f)) { "negative width" }
        require(!(cap != CAP_BUTT && cap != CAP_ROUND && cap != CAP_SQUARE)) { "illegal end cap value" }
        if (join == JOIN_MITER) {
            require(!(miterlimit < 1.0f)) { "miter limit < 1" }
        } else require(!(join != JOIN_ROUND && join != JOIN_BEVEL)) { "illegal line join value" }
        if (dash != null) {
            require(!(dash_phase < 0.0f)) { "negative dash phase" }
            var allzero = true
            for (i in dash.indices) {
                val d = dash[i]
                if (d > 0.0) {
                    allzero = false
                } else require(!(d < 0.0)) { "negative dash length" }
            }
            require(!allzero) { "dash lengths all zero" }
        }
        this.lineWidth = width
        this.endCap = cap
        this.lineJoin = join
        this.miterLimit = miterlimit
        if (dash != null) {
            this.dash = dash.clone()
        }
        this.dashPhase = dash_phase
    }

    /**
     * Returns a `Shape` whose interior defines the
     * stroked outline of a specified `Shape`.
     * @param s the `Shape` boundary be stroked
     * @return the `Shape` of the stroked outline.
     */
    override fun createStrokedShape(s: Shape?): Shape? {
//	FillAdapter filler = new FillAdapter();
//	PathStroker stroker = new PathStroker(filler);
//	PathDasher dasher = null;
//
//	try {
//	    PathConsumer consumer;
//
//	    stroker.setPenDiameter(width);
//	    stroker.setPenT4(null);
//	    stroker.setCaps(RasterizerCaps[cap]);
//	    stroker.setCorners(RasterizerCorners[join], miterlimit);
//	    if (dash != null) {
//		dasher = new PathDasher(stroker);
//		dasher.setDash(dash, dash_phase);
//		dasher.setDashT4(null);
//		consumer = dasher;
//	    } else {
//		consumer = stroker;
//	    }
//
//	    feedConsumer(consumer, s.getPathIterator(null));
//	} finally {
//	    stroker.dispose();
//	    if (dasher != null) {
//		dasher.dispose();
//	    }
//	}
//
//	return filler.getShape();
        return null
    }

    //    private void feedConsumer(PathConsumer consumer, PathIterator pi) {
    //	try {
    //	    consumer.beginPath();
    //	    boolean pathClosed = false;
    //	    float mx = 0.0f;
    //	    float my = 0.0f;
    //	    float point[]  = new float[6];
    //
    //	    while (!pi.isDone()) {
    //		int type = pi.currentSegment(point);
    //		if (pathClosed == true) {
    //		    pathClosed = false;
    //		    if (type != PathIterator.SEG_MOVETO) {
    //			// Force current point back to last moveto point
    //			consumer.beginSubpath(mx, my);
    //		    }
    //		}
    //		switch (type) {
    //		case PathIterator.SEG_MOVETO:
    //		    mx = point[0];
    //		    my = point[1];
    //		    consumer.beginSubpath(point[0], point[1]);
    //		    break;
    //		case PathIterator.SEG_LINETO:
    //		    consumer.appendLine(point[0], point[1]);
    //		    break;
    //		case PathIterator.SEG_QUADTO:
    //		    // Quadratic curves take two points
    //		    consumer.appendQuadratic(point[0], point[1],
    //					     point[2], point[3]);
    //		    break;
    //		case PathIterator.SEG_CUBICTO:
    //		    // Cubic curves take three points
    //		    consumer.appendCubic(point[0], point[1],
    //					 point[2], point[3],
    //					 point[4], point[5]);
    //		    break;
    //		case PathIterator.SEG_CLOSE:
    //		    consumer.closedSubpath();
    //		    pathClosed = true;
    //		    break;
    //		}
    //		pi.next();
    //	    }
    //
    //	    consumer.endPath();
    //	} catch (PathException e) {
    //	    throw new InternalError("Unable to Stroke shape ("+
    //				    e.getMessage()+")");
    //	}
    //    }

    val dashArray: FloatArray?
        /**
         * Returns the array representing the lengths of the dash segments.
         * Alternate entries in the array represent the user space lengths
         * of the opaque and transparent segments of the dashes.
         * As the pen moves along the outline of the `Shape`
         * to be stroked, the user space
         * distance that the pen travels is accumulated.  The distance
         * value is used to index into the dash array.
         * The pen is opaque when its current cumulative distance maps
         * to an even element of the dash array and transparent otherwise.
         * @return the dash array.
         */
        get() {
            if (dash == null) {
                return null
            }

            return dash!!.clone()
        }

    /**
     * Returns the hashcode for this stroke.
     * @return      a hash code for this stroke.
     */
    override fun hashCode(): Int {
        var hash = (this.lineWidth).toBits()
        hash = hash * 31 + this.lineJoin
        hash = hash * 31 + this.endCap
        hash = hash * 31 + (this.miterLimit).toBits()
        if (dash != null) {
            hash = hash * 31 + (this.dashPhase).toBits()
            for (i in dash!!.indices) {
                hash = hash * 31 + (dash!![i]).toBits()
            }
        }
        return hash
    }

    /**
     * Returns true if this BasicStroke represents the same
     * stroking operation as the given argument.
     */
    /**
     * Tests if a specified object is equal to this `BasicStroke`
     * by first testing if it is a `BasicStroke` and then comparing
     * its width, join, cap, miter limit, dash, and dash phase attributes with
     * those of this `BasicStroke`.
     * @param  obj the specified object to compare to this
     * `BasicStroke`
     * @return `true` if the width, join, cap, miter limit, dash, and
     * dash phase are the same for both objects;
     * `false` otherwise.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj !is BasicStroke) {
            return false
        }

        val bs = obj
        if (this.lineWidth != bs.lineWidth) {
            return false
        }

        if (this.lineJoin != bs.lineJoin) {
            return false
        }

        if (this.endCap != bs.endCap) {
            return false
        }

        if (this.miterLimit != bs.miterLimit) {
            return false
        }

        if (dash != null) {
            if (this.dashPhase != bs.dashPhase) {
                return false
            }

            if (!dash.contentEquals(bs.dash)) {
                return false
            }
        } else if (bs.dash != null) {
            return false
        }

        return true
    } //    private static final int RasterizerCaps[] = {
    //	Rasterizer.BUTT, Rasterizer.ROUND, Rasterizer.SQUARE
    //    };
    //
    //    private static final int RasterizerCorners[] = {
    //	Rasterizer.MITER, Rasterizer.ROUND, Rasterizer.BEVEL
    //    };
    //    private class FillAdapter implements PathConsumer {
    //	boolean closed;
    //	Path2D.Float path;
    //
    //	public FillAdapter() {
    //            // Ductus only supplies float coordinates so
    //            // Path2D.Double is not necessary here.
    //	    path = new Path2D.Float(Path2D.WIND_NON_ZERO);
    //	}
    //
    //	public Shape getShape() {
    //	    return path;
    //	}
    //
    //	public void dispose() {
    //	}
    //
    //	public PathConsumer getConsumer() {
    //	    return null;
    //	}
    //
    //	public void beginPath() {}
    //
    //	public void beginSubpath(float x0, float y0) {
    //	    if (closed) {
    //		path.closePath();
    //		closed = false;
    //	    }
    //	    path.moveTo(x0, y0);
    //	}
    //
    //	public void appendLine(float x1, float y1) {
    //	    path.lineTo(x1, y1);
    //	}
    //
    //	public void appendQuadratic(float xm, float ym, float x1, float y1) {
    //	    path.quadTo(xm, ym, x1, y1);
    //	}
    //
    //	public void appendCubic(float xm, float ym,
    //				float xn, float yn,
    //				float x1, float y1) {
    //	    path.curveTo(xm, ym, xn, yn, x1, y1);
    //	}
    //
    //	public void closedSubpath() {
    //	    closed = true;
    //	}
    //
    //	public void endPath() {
    //	    if (closed) {
    //		path.closePath();
    //		closed = false;
    //	    }
    //	}
    //
    //	public void useProxy(FastPathProducer proxy)
    //	    throws PathException
    //	{
    //	    proxy.sendTo(this);
    //	}
    //
    //	public long getCPathConsumer() {
    //	    return 0;
    //	}
    //    }

    companion object {
        /**
         * Joins path segments by extending their outside edges until
         * they meet.
         */
        const val JOIN_MITER: Int = 0

        /**
         * Joins path segments by rounding off the corner at a radius
         * of half the line width.
         */
        const val JOIN_ROUND: Int = 1

        /**
         * Joins path segments by connecting the outer corners of their
         * wide outlines with a straight segment.
         */
        const val JOIN_BEVEL: Int = 2

        /**
         * Ends unclosed subpaths and dash segments with no added
         * decoration.
         */
        const val CAP_BUTT: Int = 0

        /**
         * Ends unclosed subpaths and dash segments with a round
         * decoration that has a radius equal to half of the width
         * of the pen.
         */
        const val CAP_ROUND: Int = 1

        /**
         * Ends unclosed subpaths and dash segments with a square
         * projection that extends beyond the end of the segment
         * to a distance equal to half of the line width.
         */
        const val CAP_SQUARE: Int = 2
    }
}
