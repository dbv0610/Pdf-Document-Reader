/*
 * 文件名称:          LinePathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:03:57
 */
package com.wxiwei.office.common.autoshape.pathbuilder.line

import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.autoshape.pathbuilder.LineArrowPathBuilder
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.LineShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-11-2
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
object LinePathBuilder {
    private val paths: MutableList<ExtendPath?> = ArrayList<ExtendPath?>()

    /**
     * 
     * @param shape
     * @param rect
     * @param zoom
     * @return
     */
    @JvmStatic
    fun getLinePath(shape: LineShape, rect: Rect, zoom: Float): MutableList<ExtendPath?>? {
        paths.clear()

        when (shape.getShapeType()) {
            ShapeTypes.Line, ShapeTypes.StraightConnector1 -> return getStraightConnectorPath(
                shape,
                rect,
                zoom
            )

            ShapeTypes.BentConnector2 -> return getBentConnectorPath2(shape, rect, zoom)

            ShapeTypes.BentConnector3 -> return getBentConnectorPath3(shape, rect, zoom)

            ShapeTypes.CurvedConnector2 -> return getCurvedConnector2Path(shape, rect, zoom)

            ShapeTypes.CurvedConnector3 -> return getCurvedConnector3Path(shape, rect, zoom)

            ShapeTypes.CurvedConnector4 -> return getCurvedConnector4Path(shape, rect, zoom)

            ShapeTypes.CurvedConnector5 -> return getCurvedConnector4Path(shape, rect, zoom)
        }
        return null
    }

    private fun getStraightConnectorPath(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var extendPath = ExtendPath()
        var path: Path? = Path()


        var x0 = rect.left
        var y0 = rect.top
        var x1 = rect.right
        var y1 = rect.bottom
        val lineLength =
            sqrt((rect.width() * rect.width() + rect.height() * rect.height()).toDouble())
        if (shape.getStartArrowhead() &&
            (shape.getStartArrow().getType() == Arrow.Arrow_Triangle || shape.getStartArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val arrowLength = LineArrowPathBuilder.getArrowLength(
                shape.getStartArrow(),
                shape.getLine().getLineWidth()
            )
            if (abs(x1 - x0) >= 1) {
                x0 = (x0 + (arrowLength * zoom) / lineLength * (x1 - x0) * 0.75f).toInt()
            }
            if (abs(y1 - y0) >= 1) {
                y0 = (y0 + (arrowLength * zoom) / lineLength * (y1 - y0) * 0.75f).toInt()
            }
        }

        if (shape.getEndArrowhead() &&
            (shape.getEndArrow().getType() == Arrow.Arrow_Triangle || shape.getEndArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val arrowLength = LineArrowPathBuilder.getArrowLength(
                shape.getEndArrow(),
                shape.getLine().getLineWidth()
            )
            if (abs(x1 - x0) >= 1) {
                x1 = (x1 + (arrowLength * zoom) / lineLength * (x0 - x1) * 0.75f).toInt()
            }
            if (abs(y1 - y0) >= 1) {
                y1 = (y1 + (arrowLength * zoom) / lineLength * (y0 - y1) * 0.75f).toInt()
            }
        }

        path!!.moveTo(x0.toFloat(), y0.toFloat())
        path.lineTo(x1.toFloat(), y1.toFloat())

        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }
        extendPath.backgroundAndFill = bgFill
        extendPath.setLine(shape.getLine())
        extendPath.path = path
        paths.add(extendPath)

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getEndArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getStartArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        return paths
    }

    private fun getBentConnectorPath2(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var x = rect.width() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * values[0]!!
            }
        }

        var extendPath = ExtendPath()
        var path: Path? = Path()

        var x0 = rect.left
        val y0 = rect.top
        val x1 = rect.right
        var y1 = rect.bottom
        if (shape.getStartArrowhead() &&
            (shape.getStartArrow().getType() == Arrow.Arrow_Triangle || shape.getStartArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val length = LineArrowPathBuilder.getArrowLength(
                shape.getStartArrow(),
                shape.getLine().getLineWidth()
            )
            x0 =
                (x0 + ceil(((length * zoom) / abs(x1 - x0) * (x1 - x0) * 0.75f).toDouble())).toInt()
        }

        if (shape.getEndArrowhead() &&
            (shape.getEndArrow().getType() == Arrow.Arrow_Triangle || shape.getEndArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val length = LineArrowPathBuilder.getArrowLength(
                shape.getEndArrow(),
                shape.getLine().getLineWidth()
            )
            y1 =
                (y1 + ceil(((length * zoom) / abs(y1 - y0) * (y0 - y1) * 0.75f).toDouble())).toInt()
        }


        //isBentConnector2
        path!!.moveTo(x0.toFloat(), y0.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(x1.toFloat(), y1.toFloat())

        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                rect.right.toFloat(),
                y1.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getEndArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                x0.toFloat(),
                rect.top.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getStartArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        return paths
    }

    private fun getBentConnectorPath3(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var x = rect.width() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * values[0]!!
            }
        }

        var extendPath = ExtendPath()
        var path: Path? = Path()

        var x0 = rect.left
        val y0 = rect.top
        var x1 = rect.right
        val y1 = rect.bottom

        if (shape.getStartArrowhead() &&
            (shape.getStartArrow().getType() == Arrow.Arrow_Triangle || shape.getStartArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val length = LineArrowPathBuilder.getArrowLength(
                shape.getStartArrow(),
                shape.getLine().getLineWidth()
            )
            x0 =
                (x0 + ceil(((length * zoom) / abs(x1 - x0) * (x1 - x0) * 0.75f).toDouble())).toInt()
        }

        if (shape.getEndArrowhead() &&
            (shape.getEndArrow().getType() == Arrow.Arrow_Triangle || shape.getEndArrow()
                .getType() == Arrow.Arrow_Stealth)
        ) {
            val length = LineArrowPathBuilder.getArrowLength(
                shape.getEndArrow(),
                shape.getLine().getLineWidth()
            )
            x1 =
                (x1 + ceil(((length * zoom) / abs(x1 - x0) * (x0 - x1) * 0.75f).toDouble())).toInt()
        }



        path!!.moveTo(x0.toFloat(), y0.toFloat())
        path.lineTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.lineTo(x1.toFloat(), y1.toFloat())

        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                rect.left + x,
                rect.bottom.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getEndArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getDirectLineArrowPath(
                rect.left + x,
                rect.top.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getStartArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        return paths
    }

    private fun getCurvedConnector2Path(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var extendPath = ExtendPath()
        var path: Path? = Path()
        path!!.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.quadTo(
            rect.right.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )

        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getQuadBezArrowPath(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getEndArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getQuadBezArrowPath(
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                rect.right.toFloat(),
                rect.top.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getStartArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        return paths
    }

    private fun getCurvedConnector3Path(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var x = rect.width() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * values[0]!!
            }
        }

        var extendPath: ExtendPath? = null
        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }

        var startArrowTailCenter: PointF? = null
        var endArrowTailCenter: PointF? = null

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            val arrowPathAndTail = LineArrowPathBuilder.getQuadBezArrowPath(
                rect.left + x,
                rect.exactCenterY(),
                rect.left + x,
                rect.bottom.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            )

            val arrowType = shape.getEndArrow().getType()
            if (arrowType == Arrow.Arrow_Triangle || arrowType == Arrow.Arrow_Stealth) {
                endArrowTailCenter = arrowPathAndTail.arrowTailCenter
            }

            extendPath.path = arrowPathAndTail.arrowPath
            if (arrowType != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            val arrowPathAndTail = LineArrowPathBuilder.getQuadBezArrowPath(
                rect.left + x,
                rect.exactCenterY(),
                rect.left + x,
                rect.top.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            )

            val arrowType = shape.getStartArrow().getType()
            if (arrowType == Arrow.Arrow_Triangle || arrowType == Arrow.Arrow_Stealth) {
                startArrowTailCenter = arrowPathAndTail.arrowTailCenter
            }

            extendPath.path = arrowPathAndTail.arrowPath
            if (arrowType != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        extendPath = ExtendPath()
        val path = Path()
        path.reset()
        if (startArrowTailCenter != null) {
            startArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                rect.left.toFloat(),
                rect.top.toFloat(),
                startArrowTailCenter.x,
                startArrowTailCenter.y,
                shape.getStartArrow().getType()
            )
            path.moveTo(startArrowTailCenter.x, startArrowTailCenter.y)
        } else {
            path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        }

        path.quadTo(rect.left + x, rect.top.toFloat(), rect.left + x, rect.exactCenterY())
        path.moveTo(rect.left + x, rect.exactCenterY())

        if (endArrowTailCenter != null) {
            endArrowTailCenter = LineArrowPathBuilder.getReferencedPosition(
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                endArrowTailCenter.x,
                endArrowTailCenter.y,
                shape.getEndArrow().getType()
            )
            path.quadTo(
                rect.left + x,
                rect.bottom.toFloat(),
                endArrowTailCenter.x,
                endArrowTailCenter.y
            )
        } else {
            path.quadTo(
                rect.left + x,
                rect.bottom.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
        }


        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        return paths
    }

    private fun getCurvedConnector4Path(
        shape: LineShape,
        rect: Rect,
        zoom: Float
    ): MutableList<ExtendPath?> {
        var x = rect.width() * 0.5f
        var y = rect.height() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * values[0]!!
            }

            if (values[1] != null) {
                y = rect.height() * values[1]!!
            }
        }

        val x0 = rect.left + x
        val y0 = rect.top + y / 2

        val x1 = (x0 + rect.right) / 2f
        val y1 = rect.top + y
        var extendPath = ExtendPath()
        var path: Path? = Path()
        path!!.reset()

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.quadTo(x0, rect.top.toFloat(), x0, y0)

        path.moveTo(x0, y0)
        path.quadTo(x0, y1, x1, y1)

        path.moveTo(x1, y1)
        path.quadTo(rect.right.toFloat(), y1, rect.right.toFloat(), rect.bottom.toFloat())

        var bgFill = shape.getBackgroundAndFill()
        if (bgFill == null) {
            bgFill = shape.getLine().getBackgroundAndFill()
        }
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        if (shape.getEndArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getQuadBezArrowPath(
                x1,
                y1,
                rect.right.toFloat(),
                y1,
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                shape.getEndArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getEndArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }
        if (shape.getStartArrowhead()) {
            extendPath = ExtendPath()
            extendPath.setArrowFlag(true)
            path = LineArrowPathBuilder.getQuadBezArrowPath(
                x0,
                y0,
                x0,
                rect.top.toFloat(),
                rect.left.toFloat(),
                rect.top.toFloat(),
                shape.getStartArrow(),
                shape.getLine().getLineWidth(),
                zoom
            ).arrowPath
            extendPath.path = path
            if (shape.getStartArrow().getType() != Arrow.Arrow_Arrow) {
                extendPath.backgroundAndFill = bgFill
            } else {
                extendPath.setLine(shape.getLine())
            }
            paths.add(extendPath)
        }

        return paths
    }
}
