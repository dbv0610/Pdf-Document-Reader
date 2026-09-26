// Copyright 2007, FreeHEP.
package com.wxiwei.office.thirdpart.emf.data

import com.wxiwei.office.java.awt.Shape
import com.wxiwei.office.java.awt.Stroke
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.AffineTransform.Companion.getTranslateInstance
import com.wxiwei.office.thirdpart.emf.EMFConstants
import com.wxiwei.office.thirdpart.emf.EMFRenderer
import java.util.logging.Logger


/**
 * @author Steffen Greiffenberg
 * @version $Id$
 */
abstract class AbstractPen : EMFConstants, GDIObject {
    /**
     * Represents a stroke that draws inside the given shape.
     * Used if EMFConstants.PS_INSIDEFRAME is used
     */
    private inner class InsideFrameStroke(
        width: Float,
        cap: Int,
        join: Int,
        miterlimit: Float,
        dash: FloatArray?,
        dash_phase: Float
    ) : Stroke {
        private val stroke: BasicStroke

        init {
            stroke = BasicStroke(width, cap, join, miterlimit, dash, dash_phase)
        }

        override fun createStrokedShape(shape: Shape?): Shape? {
            var shape = shape
            if (shape == null) {
                return null
            }

            val oldBounds = shape.getBounds2D()
            val witdh = stroke.lineWidth

            // calcute a transformation for inside drawing
            // based on the stroke width
            val at = AffineTransform()
            if (oldBounds.getWidth() > 0) {
                at.scale(
                    (oldBounds.getWidth() - witdh) /
                            oldBounds.getWidth(), 1.0
                )
            }

            if (oldBounds.getHeight() > 0) {
                at.scale(
                    1.0,
                    (oldBounds.getHeight() - witdh)
                            / oldBounds.getHeight()
                )
            }

            // recalculate shape and its oldBounds
            shape = at.createTransformedShape(shape)
            val newBounds = shape!!.getBounds2D()

            // move the shape to the old origin + the line width offset
            val moveBackTransform = getTranslateInstance(
                oldBounds.getX() - newBounds.getX() + witdh / 2,
                oldBounds.getY() - newBounds.getY() + witdh / 2
            )
            shape = moveBackTransform.createTransformedShape(shape)

            // outline the shape using the simple basic stroke
            return stroke.createStrokedShape(shape)
        }
    }

    /**
     * returns a BasicStroke JOIN for an EMF pen style
     * @param penStyle penstyle
     * @return e.g. [java.awt.BasicStroke.JOIN_MITER]
     */
    protected fun getJoin(penStyle: Int): Int {
        when (penStyle and 0xF000) {
            EMFConstants.PS_JOIN_ROUND -> return BasicStroke.Companion.JOIN_ROUND
            EMFConstants.PS_JOIN_BEVEL -> return BasicStroke.Companion.JOIN_BEVEL
            EMFConstants.PS_JOIN_MITER -> return BasicStroke.Companion.JOIN_MITER
            else -> {
                logger.warning("got unsupported pen style " + penStyle)
                return BasicStroke.Companion.JOIN_ROUND
            }
        }
    }

    /**
     * returns a BasicStroke JOIN for an EMF pen style
     * @param penStyle Style to convert
     * @return asicStroke.CAP_ROUND, BasicStroke.CAP_SQUARE, BasicStroke.CAP_BUTT
     */
    protected fun getCap(penStyle: Int): Int {
        when (penStyle and 0xF00) {
            EMFConstants.PS_ENDCAP_ROUND -> return BasicStroke.Companion.CAP_ROUND
            EMFConstants.PS_ENDCAP_SQUARE -> return BasicStroke.Companion.CAP_SQUARE
            EMFConstants.PS_ENDCAP_FLAT -> return BasicStroke.Companion.CAP_BUTT
            else -> {
                logger.warning("got unsupported pen style " + penStyle)
                return BasicStroke.Companion.CAP_ROUND
            }
        }
    }

    /**
     * returns a Dash for an EMF pen style
     * @param penStyle Style to convert
     * @param style used if EMFConstants#PS_USERSTYLE is set
     * @return float[] representing a dash
     */
    protected fun getDash(penStyle: Int, style: IntArray?): FloatArray? {
        when (penStyle and 0xFF) {
            EMFConstants.PS_SOLID ->                 // do not use float[] { 1 }
                // it's _slow_
                return null

            EMFConstants.PS_DASH -> return floatArrayOf(5f, 5f)
            EMFConstants.PS_DOT -> return floatArrayOf(1f, 2f)
            EMFConstants.PS_DASHDOT -> return floatArrayOf(5f, 2f, 1f, 2f)
            EMFConstants.PS_DASHDOTDOT -> return floatArrayOf(5f, 2f, 1f, 2f, 1f, 2f)
            EMFConstants.PS_INSIDEFRAME ->                 // Represents a pen style that consists of a solid
                // pen that is drawn from within any given bounding rectangle
                return null

            EMFConstants.PS_NULL ->                 // do not use float[] { 1 }
                // it's _slow_
                return null

            EMFConstants.PS_USERSTYLE -> if (style != null && style.size > 0) {
                val result = FloatArray(style.size)
                var i = 0
                while (i < style.size) {
                    result[i] = style[i].toFloat()
                    i++
                }
                return result
            } else {
                return null
            }

            else -> {
                logger.warning("got unsupported pen style " + penStyle)
                // do not use float[] { 1 }
                // it's _slow_
                return null
            }
        }
    }

    /**
     * @param penStyle stored pen style
     * @return true if PS_INSIDEFRAME is set
     */
    private fun isInsideFrameStroke(penStyle: Int): Boolean {
        return (penStyle and 0xFF) == EMFConstants.PS_INSIDEFRAME
    }

    protected fun createStroke(
        renderer: EMFRenderer,
        penStyle: Int,
        style: IntArray?,
        width: Float
    ): Stroke {
        if (isInsideFrameStroke(penStyle)) {
            return InsideFrameStroke(
                width,
                getCap(penStyle),
                getJoin(penStyle),
                renderer.getMeterLimit(),
                getDash(penStyle, style),
                0f
            )
        } else {
            return BasicStroke(
                width,
                getCap(penStyle),
                getJoin(penStyle),
                renderer.getMeterLimit(),
                getDash(penStyle, style),
                0f
            )
        }
    }

    companion object {
        /**
         * logger for all instances
         */
        private val logger: Logger = Logger.getLogger("org.freehep.graphicsio.emf")
    }
}
