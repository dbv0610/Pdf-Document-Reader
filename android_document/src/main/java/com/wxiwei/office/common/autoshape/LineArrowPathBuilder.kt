/*
 * 文件名称:          LineArrowPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:09:14
 */
package com.wxiwei.office.common.autoshape

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.wxiwei.office.common.shape.Arrow
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

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
 * 日期:            2012-10-25
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
object LineArrowPathBuilder {
    private const val SMALL = 5
    private const val MEDIUM = 9
    private const val LARGE = 13

    var p: PointF = PointF()

    private fun getArrowWidth(arrow: Arrow?, lineWidth: Int): Int {
        var width = MEDIUM

        if (lineWidth < 3) {
//            switch(arrow.getWidth())
//            {
//                case 0:
//                    width = SMALL;
//                    break;
//                case 1:
//                    width = MEDIUM;
//                    break;
//                case 2:
//                    width = LARGE;
//                    break;
//            }
        } else {
            width = lineWidth * 3
            //            switch(arrow.getWidth())
//            {
//                case 0:
//                    width = lineWidth * 2;
//                    break;
//                case 1:
//                    width = lineWidth * 3;
//                    break;
//                case 2:
//                    width = lineWidth * 5;
//                    break;                 
//            }
        }
        return width
    }

    @JvmStatic
    fun getArrowLength(arrow: Arrow?, lineWidth: Int): Int {
        var length = MEDIUM

        if (lineWidth < 3) {
//            switch(arrow.getLength())
//            {
//                case 0:
//                    length = SMALL;
//                    break;
//                case 1:
//                    length = MEDIUM;
//                    break;
//                case 2:
//                    length = LARGE;
//                    break;                 
//            }
        } else {
            length = lineWidth * 3
            //            switch(arrow.getLength())
//            {
//                case 0:
//                    length = lineWidth * 2;
//                    break;
//                case 1:
//                    length = lineWidth * 3;
//                    break;
//                case 2:
//                    length = lineWidth * 5;
//                    break;                 
//            }
        }

        return length
    }

    /**
     * 
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param arrowWidth
     * @param arrowLength
     * @return
     */
    @JvmStatic
    fun getDirectLineArrowPath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        arrow: Arrow,
        lineWidth: Int
    ): Path {
        return getDirectLineArrowPath(startX, startY, endX, endY, arrow, lineWidth, 1.0f)
    }

    @JvmStatic
    fun getDirectLineArrowPath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        arrow: Arrow,
        lineWidth: Int,
        zoom: Float
    ): Path {
        var startX = startX
        var startY = startY
        val width = getArrowWidth(arrow, lineWidth)
        val length = getArrowLength(arrow, lineWidth)

        val ratio = (length * zoom / sqrt(
            (endX - startX).toDouble().pow(2.0) + (endY - startY).toDouble().pow(2.0)
        )).toFloat()
        startY = endY - (endY - startY) * ratio
        startX = endX - (endX - startX) * ratio
        return buildArrowPath(
            startX,
            startY,
            endX,
            endY,
            width * zoom,
            length * zoom,
            arrow.getType()
        )
    }

    @JvmStatic
    fun getQuadBezArrowPath(
        startX: Float, startY: Float,
        ctrlX: Float, ctrlY: Float,
        endX: Float, endY: Float, arrow: Arrow, lineWidth: Int
    ): Path {
        return getQuadBezArrowPath(
            startX, startY,
            ctrlX, ctrlY,
            endX, endY,
            arrow, lineWidth, 1f
        )
    }

    @JvmStatic
    fun getQuadBezArrowPath(
        startX: Float, startY: Float,
        ctrlX: Float, ctrlY: Float,
        endX: Float, endY: Float,
        arrow: Arrow, lineWidth: Int, zoom: Float
    ): Path {
        val width = getArrowWidth(arrow, lineWidth) * zoom
        val length = getArrowLength(arrow, lineWidth) * zoom

        var r = 0.9f
        var offRatio = 0.01f
        var end = quadBezComputePoint(startX, startY, ctrlX, ctrlY, endX, endY, r)
        var dist = Math.round(
            sqrt(
                (end.x - endX).toDouble().pow(2.0) + (end.y - endY).toDouble().pow(2.0)
            )
        ).toInt()
        var inc: Boolean? = null
        while (abs(dist - length) > 1 && r < 1.0f && r > 0) {
            if (dist - length > 1) {
                r += offRatio
                if (inc != null && !inc) {
                    offRatio *= 0.1.toFloat()
                    r -= offRatio
                }
                inc = true
            } else {
                r -= offRatio
                if (inc != null && inc) {
                    offRatio *= 0.1.toFloat()
                    r += offRatio
                }

                inc = false
            }

            end = quadBezComputePoint(startX, startY, ctrlX, ctrlY, endX, endY, r)
            dist =
                Math.round(sqrt(((end.x - endX) * (end.x - endX) + (end.y - endY) * (end.y - endY)).toDouble()))
                    .toInt()
        }


        return buildArrowPath(end.x, end.y, endX, endY, width, length, arrow.getType())
    }

    @JvmStatic
    fun getCubicBezArrowPath(
        startX: Float, startY: Float,
        ctrl1X: Float, ctrl1Y: Float,
        ctrl2X: Float, ctrl2Y: Float,
        endX: Float, endY: Float,
        arrow: Arrow, lineWidth: Int
    ): Path {
        return getCubicBezArrowPath(
            startX, startY,
            ctrl1X, ctrl1Y,
            ctrl2X, ctrl2Y,
            endX, endY,
            arrow, lineWidth, 1f
        )
    }

    @JvmStatic
    fun getCubicBezArrowPath(
        startX: Float, startY: Float,
        ctrl1X: Float, ctrl1Y: Float,
        ctrl2X: Float, ctrl2Y: Float,
        endX: Float, endY: Float,
        arrow: Arrow, lineWidth: Int, zoom: Float
    ): Path {
        val width = getArrowWidth(arrow, lineWidth)
        val length = getArrowLength(arrow, lineWidth)

        var r = 0.9f
        var offRatio = 0.01f
        var end =
            cubicBezComputePoint(startX, startY, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY, r)
        var dist = Math.round(
            sqrt(
                (end.x - endX).toDouble().pow(2.0) + (end.y - endY).toDouble().pow(2.0)
            )
        ).toInt()
        var inc: Boolean? = null
        while (abs(dist - length) > 1 && r < 1.0f && r > 0) {
            if (dist - length > 1) {
                r += offRatio
                if (inc != null && !inc) {
                    offRatio *= 0.1.toFloat()
                    r -= offRatio
                }
                inc = true
            } else {
                r -= offRatio
                if (inc != null && inc) {
                    offRatio *= 0.1.toFloat()
                    r += offRatio
                }

                inc = false
            }

            end =
                cubicBezComputePoint(startX, startY, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY, r)
            dist =
                Math.round(sqrt(((end.x - endX) * (end.x - endX) + (end.y - endY) * (end.y - endY)).toDouble()))
                    .toInt()
        }

        return buildArrowPath(
            end.x,
            end.y,
            endX,
            endY,
            width.toFloat(),
            length.toFloat(),
            arrow.getType()
        )
    }

    private fun quadBezComputePoint(
        startX: Float, startY: Float,
        ctrlX: Float, ctrlY: Float,
        endX: Float, endY: Float, fU: Float
    ): PointF {
        //  
        //  Add up all the blending functions multiplied with the control points
        //
        var fBlend: Float
        val f1subu = 1.0f - fU


        //  First blending function (1-u)^2
        fBlend = f1subu * f1subu
        p.x = fBlend * startX
        p.y = fBlend * startY


        //  Second blending function 2u(1-u)
        fBlend = 2 * fU * f1subu
        p.x += fBlend * ctrlX
        p.y += fBlend * ctrlY


        //  Fourth blending function u^2
        fBlend = fU * fU
        p.x += fBlend * endX
        p.y += fBlend * endY

        return p
    }

    /**
     * 
     * @param startX
     * @param startY
     * @param ctrl1X
     * @param ctrl1Y
     * @param ctrl2X
     * @param ctrl2Y
     * @param endX
     * @param endY
     * @param fU
     * @return
     */
    private fun cubicBezComputePoint(
        startX: Float, startY: Float,
        ctrl1X: Float, ctrl1Y: Float,
        ctrl2X: Float, ctrl2Y: Float,
        endX: Float, endY: Float, fU: Float
    ): PointF {
        val p = PointF()
        //  
        //  Add up all the blending functions multiplied with the control points
        //
        var fBlend: Float
        val f1subu = 1.0f - fU

        //  
        //  First blending function (1-u)^3
        //  
        fBlend = f1subu * f1subu * f1subu
        p.x = fBlend * startX
        p.y = fBlend * startY

        //  
        //  Second blending function 3u(1-u)^2
        //
        fBlend = 3 * fU * f1subu * f1subu
        p.x += fBlend * ctrl1X
        p.y += fBlend * ctrl1Y

        //  
        //  Third blending function 3u^2 * (1-u)
        //
        fBlend = 3 * fU * fU * f1subu
        p.x += fBlend * ctrl2X
        p.y += fBlend * ctrl2Y

        //  
        //  Fourth blending function u^3
        //  
        fBlend = fU * fU * fU
        p.x += fBlend * endX
        p.y += fBlend * endY

        return p
    }

    private fun buildArrowPath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        zoom: Float
    ): Path {
        val path = Path()
        path.moveTo(endX, endY)

        val pBaseX: Float
        val pBaseY: Float
        val vecLineX: Float
        val vecLineY: Float
        val vecLeftX: Float
        val vecLeftY: Float
        val fLength: Float
        val th: Float
        val ta: Float

        val nWidth = (15 * zoom).toInt()
        val fTheta = 1.0f


        // build the line vector
        vecLineX = endX - startX
        vecLineY = endY - startY


        // build the arrow base vector - normal to the line
        vecLeftX = -vecLineY
        vecLeftY = vecLineX


        // setup length parameters
        fLength = sqrt((vecLineX * vecLineX + vecLineY * vecLineY).toDouble()).toFloat()
        th = nWidth / (2.0f * fLength)
        ta = (nWidth / (2.0f * (tan(fTheta.toDouble()) / 2.0f) * fLength)).toFloat()


        // find the base of the arrow
        pBaseX = (endX + -ta * vecLineX).toInt().toFloat()
        pBaseY = (endY + -ta * vecLineY).toInt().toFloat()


        // build the points on the sides of the arrow
        path.lineTo(pBaseX + th * vecLeftX, pBaseY + th * vecLeftY / 2)
        path.lineTo(pBaseX + -th * vecLeftX, pBaseY + -th * vecLeftY / 2)

        path.close()

        return path
    }

    private fun buildArrowPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        arrowWidth: Float, arrowLength: Float, arrowType: Byte
    ): Path {
        when (arrowType) {
            Arrow.Arrow_Triangle -> return buildTriangleArrowPath(
                startX, startY, endX, endY,
                arrowWidth
            )

            Arrow.Arrow_Arrow -> return buildArrowArrowPath(
                startX, startY, endX, endY,
                arrowWidth
            )

            Arrow.Arrow_Diamond -> return buildDiamondArrowPath(
                startX, startY, endX, endY,
                arrowWidth, arrowLength
            )

            Arrow.Arrow_Stealth -> return buildStealthArrowPath(
                startX, startY, endX, endY,
                arrowWidth, arrowLength
            )

            Arrow.Arrow_Oval -> return buildOvalArrowPath(endX, endY, arrowWidth, arrowLength)
        }

        return Path()
    }

    private fun buildTriangleArrowPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        arrowWidth: Float
    ): Path {
        val path = Path()
        path.moveTo(endX, endY)


        //line inclination
        if (endY == startY) {
            path.lineTo(startX, startY - arrowWidth / 2.0f)
            path.lineTo(startX, startY + arrowWidth / 2.0f)
        } else if (endX == startX) {
            path.lineTo(startX - arrowWidth / 2.0f, startY)
            path.lineTo(startX + arrowWidth / 2.0f, startY)
        } else {
            var k = (endY - startY) / (endX - startX)
            k = -1 / k
            val angle = atan(k.toDouble())
            val offx = (arrowWidth / 2.0f * cos(angle)).toFloat()
            val offy = (arrowWidth / 2.0f * sin(angle)).toFloat()


            // build the points on the sides of the arrow
            path.lineTo(startX + offx, startY + offy)
            path.lineTo(startX - offx, startY - offy)
        }


        path.close()

        return path
    }

    private fun buildArrowArrowPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        arrowWidth: Float
    ): Path {
        val path = Path()


        //line inclination
        if (endY == startY) {
            path.moveTo(startX, startY - arrowWidth / 2.0f)
            path.lineTo(endX, endY)
            path.lineTo(startX, startY + arrowWidth / 2.0f)
        } else if (endX == startX) {
            path.moveTo(startX - arrowWidth / 2.0f, startY)
            path.lineTo(endX, endY)
            path.lineTo(startX + arrowWidth / 2.0f, startY)
        } else {
            var k = (endY - startY) / (endX - startX)
            k = -1 / k
            val angle = atan(k.toDouble())
            val offx = (arrowWidth / 2.0f * cos(angle)).toFloat()
            val offy = (arrowWidth / 2.0f * sin(angle)).toFloat()


            // build the points on the sides of the arrow
            path.moveTo(startX + offx, startY + offy)
            path.lineTo(endX, endY)
            path.lineTo(startX - offx, startY - offy)
        }

        return path
    }

    private fun buildDiamondArrowPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        arrowWidth: Float, arrowLength: Float
    ): Path {
        val path = Path()
        //line inclination
        if (endY == startY || endX == startX) {
            path.moveTo(endX - arrowLength / 2.0f, endY)
            path.lineTo(endX, endY - arrowWidth / 2.0f)
            path.lineTo(endX + arrowLength / 2.0f, endY)
            path.lineTo(endX, endY + arrowWidth / 2.0f)
        } else {
            var k = (endY - startY) / (endX - startX)
            k = -1 / k
            val angle = atan(k.toDouble())
            val offx = (arrowLength / 2.0f * cos(angle)).toFloat()
            val offy = (arrowWidth / 2.0f * sin(angle)).toFloat()


            // build the points on the sides of the arrow
            path.moveTo(startX, startY)
            path.lineTo(endX + offx, endY + offy)
            path.lineTo(endX + (endX - startX), endY + (endY - startY))
            path.lineTo(endX - offx, endY - offy)
        }

        path.close()
        return path
    }

    private fun buildStealthArrowPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        arrowWidth: Float, arrowLength: Float
    ): Path {
        val path = Path()
        path.moveTo(endX, endY)


        //line inclination
        if (endY == startY) {
            path.lineTo(startX, startY - arrowWidth / 2.0f)
            path.lineTo(startX + (endX - startX) / 4f, endY)
            path.lineTo(startX, startY + arrowWidth / 2.0f)
        } else if (endX == startX) {
            path.lineTo(startX - arrowWidth / 2.0f, startY)
            path.lineTo(startX, startY + (endY - startY) / 4f)
            path.lineTo(startX + arrowWidth / 2.0f, startY)
        } else {
            var k = (endY - startY) / (endX - startX)
            k = -1 / k
            val angle = atan(k.toDouble())
            val offx = (arrowLength / 2.0f * cos(angle)).toFloat()
            val offy = (arrowWidth / 2.0f * sin(angle)).toFloat()


            // build the points on the sides of the arrow
            path.lineTo(startX + offx, startY + offy)
            path.lineTo(startX + (endX - startX) / 4f, startY + (endY - startY) / 4f)
            path.lineTo(startX - offx, startY - offy)
        }

        path.close()

        return path
    }

    private fun buildOvalArrowPath(
        endX: Float,
        endY: Float,
        arrowWidth: Float,
        arrowLength: Float
    ): Path {
        val path = Path()
        path.addOval(
            RectF(
                endX - arrowLength / 2.0f,
                endY - arrowWidth / 2.0f,
                endX + arrowLength / 2.0f,
                endY + arrowWidth / 2.0f
            ),
            Path.Direction.CCW
        )
        return path
    }
}
