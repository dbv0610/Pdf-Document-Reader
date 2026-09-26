/*
 * 文件名称:          MathPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:23:07
 */
package com.wxiwei.office.common.autoshape.pathbuilder.math

import android.graphics.Path
import android.graphics.Rect
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.min
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
 * 日期:            2012-11-6
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
object MathPathBuilder {
    private val path = Path()

    /**
     * get math path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getMathPath(shape: AutoShape, rect: Rect): Path? {
        path.reset()
        when (shape.getShapeType()) {
            ShapeTypes.MathPlus -> return getMathPlusPath(shape, rect)

            ShapeTypes.MathMinus -> return getMathMinusPath(shape, rect)

            ShapeTypes.MathMultiply -> return getMathMultiplyPath(shape, rect)

            ShapeTypes.MathDivide -> return getMathDividePath(shape, rect)

            ShapeTypes.MathEqual -> return getMathEqualPath(shape, rect)

            ShapeTypes.MathNotEqual -> return getMathNotEqualPath(shape, rect)
        }

        return null
    }

    private fun getMathPlusPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.24f / 2
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!! / 2
            }
        }
        val left = rect.left + rect.width().toFloat() / 8
        val right = rect.right - rect.width().toFloat() / 8
        val top = rect.top + rect.height().toFloat() / 8
        val bottom = rect.bottom - rect.height().toFloat() / 8


        path.moveTo(left, rect.exactCenterY() - x)
        path.lineTo(rect.exactCenterX() - x, rect.exactCenterY() - x)
        path.lineTo(rect.exactCenterX() - x, top)
        path.lineTo(rect.exactCenterX() + x, top)
        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() - x)
        path.lineTo(right, rect.exactCenterY() - x)
        path.lineTo(right, rect.exactCenterY() + x)
        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + x)
        path.lineTo(rect.exactCenterX() + x, bottom)
        path.lineTo(rect.exactCenterX() - x, bottom)
        path.lineTo(rect.exactCenterX() - x, rect.exactCenterY() + x)
        path.lineTo(left, rect.exactCenterY() + x)
        path.close()
        return path
    }

    private fun getMathMinusPath(shape: AutoShape, rect: Rect): Path {
        var x = rect.height() * 0.24f / 2
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.height() * values[0]!! / 2
            }
        }
        val left = rect.left + rect.width().toFloat() / 8
        val right = rect.right - rect.width().toFloat() / 8
        val top = rect.exactCenterY() - x
        val bottom = rect.exactCenterY() + x

        path.addRect(left, top, right, bottom, Path.Direction.CW)
        return path
    }

    private fun getMathMultiplyPath(shape: AutoShape, rect: Rect): Path {
        var d = rect.height() * 0.24f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                d = rect.height() * values[0]!!
            }
        }
        val k = rect.height().toFloat() / rect.width()
        val b = d * sqrt((k * k + 1).toDouble()).toFloat() / 2
        val c = sqrt(
            (rect.width() * rect.width() +
                    rect.height() * rect.height()).toDouble()
        ).toFloat() * sqrt((1 / (k * k) + 1).toDouble()).toFloat() / 4
        val x0 = (rect.right + rect.left).toFloat() / 2
        val y0 = rect.exactCenterY()
        val x1 = (c - b) / (k + 1 / k)
        val y1 = k * x1 + b
        val x2 = (c + b) / (k + 1 / k)
        val y2 = k * x2 - b

        path.moveTo(x0, y0 - b)
        path.lineTo(x0 + x1, y0 - y1)
        path.lineTo(x0 + x2, y0 - y2)
        path.lineTo(x0 + b / k, y0)
        path.lineTo(x0 + x2, y0 + y2)
        path.lineTo(x0 + x1, y0 + y1)
        path.lineTo(x0, y0 + b)
        path.lineTo(x0 - x1, y0 + y1)
        path.lineTo(x0 - x2, y0 + y2)
        path.lineTo(x0 - b / k, y0)
        path.lineTo(x0 - x2, y0 - y2)
        path.lineTo(x0 - x1, y0 - y1)
        path.close()
        return path
    }

    private fun getMathDividePath(shape: AutoShape, rect: Rect): Path {
        var x1 = rect.height() * 0.2352f / 2
        var x2 = rect.height() * 0.0588f
        var r = rect.height() * 0.1176f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 3) {
            if (values[0] != null) {
                x1 = rect.height() * values[0]!! / 2
            }
            if (values[1] != null) {
                x2 = rect.height() * values[1]!!
            }
            if (values[2] != null) {
                r = rect.height() * values[2]!!
            }
        }

        path.addRect(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() - x1,
            rect.right - rect.width().toFloat() / 8,
            rect.exactCenterY() + x1, Path.Direction.CW
        )

        path.moveTo(
            rect.exactCenterX() + r,
            rect.exactCenterY() - x1 - x2 - r
        )
        path.addCircle(
            rect.exactCenterX(),
            rect.exactCenterY() - x1 - x2 - r, r, Path.Direction.CW
        )

        path.moveTo(
            rect.exactCenterX(),
            rect.exactCenterY() + x1 + x2 + r
        )
        path.addCircle(
            rect.exactCenterX(),
            rect.exactCenterY() + x1 + x2 + r, r, Path.Direction.CW
        )
        return path
    }

    private fun getMathEqualPath(shape: AutoShape, rect: Rect): Path {
        var x1 = rect.height() * 0.2352f
        var x2 = rect.height() * 0.1176f / 2
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x1 = rect.height() * values[0]!!
            }
            if (values[1] != null) {
                x2 = rect.height() * values[1]!! / 2
            }
        }

        path.reset()
        path.addRect(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() - x2 - x1,
            rect.right - rect.width().toFloat() / 8,
            rect.exactCenterY() - x2, Path.Direction.CW
        )

        path.moveTo(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() + x2
        )
        path.addRect(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() + x2,
            rect.right - rect.width().toFloat() / 8,
            rect.exactCenterY() + x2 + x1, Path.Direction.CW
        )
        return path
    }

    private fun getMathNotEqualPath(shape: AutoShape, rect: Rect): Path {
        var d1 = rect.height() * 0.2352f
        var angle = 110f
        var d2 = rect.height() * 0.1176f / 2
        val values = shape.getAdjustData()
        if (values != null && values.size >= 3) {
            if (values[0] != null) {
                d1 = rect.height() * values[0]!!
            }
            if (values[1] != null) {
                angle = values[1]!! * 10 / 6
            }
            if (values[2] != null) {
                d2 = rect.height() * values[2]!! / 2
            }
        }
        val k = -tan(Math.toRadians(angle.toDouble())).toFloat()
        val b = d1 * sqrt((k * k + 1).toDouble()).toFloat() / 2
        val c = rect.height().toFloat() / 2 - (b - rect.height().toFloat() / 2) / (k * k)

        val x0 = rect.exactCenterX()
        val y0 = rect.exactCenterY()
        val x1 = (rect.height().toFloat() / 2 - b) / k
        val y1 = rect.height().toFloat() / 2
        val x2 = (b + c) / (k + 1 / k)
        val y2 = k * x2 - b
        val y3 = d2 + d1
        val x3 = (y3 - b) / k
        val y4 = y3
        val x4 = (y4 + b) / k
        val y5 = d2
        val x5 = (d2 - b) / k
        val y6 = y5
        val x6 = (y6 + b) / k

        path.reset()
        path.moveTo(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() - d2 - d1
        )
        if (k >= 0) {
            path.lineTo(x0 + x3, y0 - y3)
            path.lineTo(x0 + x1, y0 - y1)
            path.lineTo(x0 + x2, y0 - y2)
            path.lineTo(x0 + x4, y0 - y4)
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() - d2 - d1
            )
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() - d2
            )
            path.lineTo(x0 + x6, y0 - y6)
            path.lineTo(x0 - x5, y0 + y5)
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() + d2
            )
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() + d2 + d1
            )
            path.lineTo(x0 - x3, y0 + y3)
            path.lineTo(x0 - x1, y0 + y1)
            path.lineTo(x0 - x2, y0 + y2)
            path.lineTo(x0 - x4, y0 + y4)
            path.lineTo(
                rect.left + rect.width().toFloat() / 8,
                rect.exactCenterY() + d2 + d1
            )
            path.lineTo(
                rect.left + rect.width().toFloat() / 8,
                rect.exactCenterY() + d2
            )
            path.lineTo(x0 - x6, y0 + y6)
            path.lineTo(x0 + x5, y0 - y5)
        } else {
            path.lineTo(x0 + x4, y0 - y4)
            path.lineTo(x0 + x2, y0 - y2)
            path.lineTo(x0 + x1, y0 - y1)
            path.lineTo(x0 + x3, y0 - y3)
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() - d2 - d1
            )
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() - d2
            )
            path.lineTo(x0 + x5, y0 - y5)
            path.lineTo(x0 - x6, y0 + y6)
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() + d2
            )
            path.lineTo(
                rect.right - rect.width().toFloat() / 8,
                rect.exactCenterY() + d2 + d1
            )
            path.lineTo(x0 - x4, y0 + y4)
            path.lineTo(x0 - x2, y0 + y2)
            path.lineTo(x0 - x1, y0 + y1)
            path.lineTo(x0 - x3, y0 + y3)
            path.lineTo(
                rect.left + rect.width().toFloat() / 8,
                rect.exactCenterY() + d2 + d1
            )
            path.lineTo(
                rect.left + rect.width().toFloat() / 8,
                rect.exactCenterY() + d2
            )
            path.lineTo(x0 - x5, y0 + y5)
            path.lineTo(x0 + x6, y0 - y6)
        }
        path.lineTo(
            rect.left + rect.width().toFloat() / 8,
            rect.exactCenterY() - d2
        )
        path.close()
        return path
    }
}
