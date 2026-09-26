/*
 * 文件名称:          RectPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:36:05
 */
package com.wxiwei.office.common.autoshape.pathbuilder.rect

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.min

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
object RectPathBuilder {
    private val rectF = RectF()

    private val path = Path()

    /**
     * get rectangle path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getRectPath(shape: AutoShape, rect: Rect): Path? {
        path.reset()

        when (shape.getShapeType()) {
            ShapeTypes.Rectangle, ShapeTypes.TextBox, ShapeTypes.TextPlainText -> return getRectanglePath(
                shape,
                rect
            )

            ShapeTypes.RoundRectangle -> return getRoundRectanglePath(shape, rect)

            ShapeTypes.Round1Rect -> return getRound1Path(shape, rect)

            ShapeTypes.Round2SameRect -> return getRound2Path(shape, rect)

            ShapeTypes.Round2DiagRect -> return getRound2DiagRectPath(shape, rect)

            ShapeTypes.Snip1Rect -> return getSnip1RectPath(shape, rect)

            ShapeTypes.Snip2SameRect -> return getSnip2SameRectPath(shape, rect)

            ShapeTypes.Snip2DiagRect -> return getSnip2DiagPath(shape, rect)

            ShapeTypes.SnipRoundRect -> return getSnipRoundPath(shape, rect)
        }

        return null
    }

    private fun getRectanglePath(shape: AutoShape?, rect: Rect): Path {
        path.addRect(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat(),
            Path.Direction.CW
        )
        return path
    }

    private fun getRoundRectanglePath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(x, x, x, x, x, x, x, x), Path.Direction.CW)
        return path
    }

    private fun getRound1Path(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(0f, 0f, x, x, 0f, 0f, 0f, 0f), Path.Direction.CW)
        return path
    }

    private fun getRound2Path(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        var y = 0f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(x, x, x, x, y, y, y, y), Path.Direction.CW)
        return path
    }

    private fun getRound2DiagRectPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        var y = 0f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRoundRect(rectF, floatArrayOf(x, x, y, y, x, x, y, y), Path.Direction.CW)
        return path
    }

    private fun getSnip1RectPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getSnip2SameRectPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        var y = 0f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.bottom - y)
        path.lineTo(rect.right - y, rect.bottom.toFloat())
        path.lineTo(rect.left + y, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - y)
        path.lineTo(rect.left.toFloat(), rect.top + x)
        path.close()
        return path
    }

    private fun getSnip2DiagPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        var y = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - y, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + y, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - y)
        path.lineTo(rect.left.toFloat(), rect.top + x)
        path.close()
        return path
    }

    private fun getdrawSnipRoundRectPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        var y = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - y, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.top + x)
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.left + x * 2, rect.top + x * 2)
        path.arcTo(rectF, 180f, 90f)
        path.close()
        return path
    }

    private fun getSnipRoundPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.18f
        var y = min(rect.width(), rect.height()) * 0.18f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
            if (values[1] != null) {
                y = min(rect.width(), rect.height()) * values[1]!!
            }
        }

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - y, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.top + x)
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.left + x * 2, rect.top + x * 2)
        path.arcTo(rectF, 180f, 90f)
        path.close()

        return path
    }
}
