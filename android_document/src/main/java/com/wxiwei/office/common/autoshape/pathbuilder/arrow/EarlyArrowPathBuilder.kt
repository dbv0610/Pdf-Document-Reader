/*
 * 文件名称:          EarlyArrowPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:42:46
 */
package com.wxiwei.office.common.autoshape.pathbuilder.arrow

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
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
 * 日期:            2012-10-17
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
object EarlyArrowPathBuilder : ArrowPathBuilder() {
    private val TODEGREE = 18000000f / 54620000f

    private val s_rect = RectF()

    private val path = Path()

    /**
     * get autoshape path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getArrowPath(shape: AutoShape, rect: Rect): Any? {
        path.reset()

        when (shape.getShapeType()) {
            ShapeTypes.RightArrow -> return getRightArrowPath(shape, rect)

            ShapeTypes.LeftArrow -> return getLeftArrowPath(shape, rect)

            ShapeTypes.UpArrow -> return getUpArrowPath(shape, rect)

            ShapeTypes.DownArrow -> return getDownArrowPath(shape, rect)

            ShapeTypes.LeftRightArrow -> return getLeftRightArrowPath(shape, rect)

            ShapeTypes.UpDownArrow -> return getUpDownArrowPath(shape, rect)

            ShapeTypes.QuadArrow -> return getQuadArrowPath(shape, rect)

            ShapeTypes.LeftRightUpArrow -> return getLeftRightUpArrowPath(shape, rect)

            ShapeTypes.BentArrow -> return getBentArrowPath(shape, rect)

            ShapeTypes.UturnArrow -> return getUturnArrowPath(shape, rect)

            ShapeTypes.LeftUpArrow -> return getLeftUpArrowPath(shape, rect)

            ShapeTypes.BentUpArrow -> return getBentUpArrowPath(shape, rect)

            ShapeTypes.CurvedRightArrow -> return getCurvedRightArrowPath(shape, rect)

            ShapeTypes.CurvedLeftArrow -> return getCurvedLeftArrowPath(shape, rect)

            ShapeTypes.CurvedUpArrow -> return getCurvedUpArrowPath(shape, rect)

            ShapeTypes.CurvedDownArrow -> return getCurvedDownArrowPath(shape, rect)

            ShapeTypes.StripedRightArrow -> return getStripedRightArrowPath(shape, rect)

            ShapeTypes.NotchedRightArrow -> return getNotchedRightArrowPath(shape, rect)

            ShapeTypes.HomePlate -> return getHomePlatePath(shape, rect)

            ShapeTypes.Chevron -> return getChevronPath(shape, rect)

            ShapeTypes.RightArrowCallout -> return getRightArrowCalloutPath(shape, rect)

            ShapeTypes.LeftArrowCallout -> return getLeftArrowCalloutPath(shape, rect)

            ShapeTypes.UpArrowCallout -> return getUpArrowCalloutPath(shape, rect)

            ShapeTypes.DownArrowCallout -> return getDownArrowCalloutPath(shape, rect)

            ShapeTypes.LeftRightArrowCallout -> return getLeftRightArrowCalloutPath(shape, rect)

            ShapeTypes.UpDownArrowCallout -> return getUpDownArrowCalloutPath(shape, rect)

            ShapeTypes.QuadArrowCallout -> return getQuadArrowCalloutPath(shape, rect)

            ShapeTypes.CircularArrow -> return getCircularArrowPath(shape, rect)
        }

        return Path()
    }

    private fun getRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.75f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.75f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        path.moveTo(rect.left.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.close()
        return path
    }

    private fun getLeftArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.25f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.25f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.height() * values[0]!!)
            } else {
                adj1 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.width() * values[1]!!)
            } else {
                adj2 = Math.round(rect.width() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.height() * 0.25f)
            adj2 = Math.round(rect.width() * 0.25f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.close()

        return path
    }

    private fun getDownArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.height() * values[0]!!)
            } else {
                adj1 = Math.round(rect.height() * 0.75f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.width() * values[1]!!)
            } else {
                adj2 = Math.round(rect.width() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.height() * 0.75f)
            adj2 = Math.round(rect.width() * 0.25f)
        }

        path.moveTo((rect.left + adj2).toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.top + adj1).toFloat())
        path.close()

        return path
    }

    private fun getLeftRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.2f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.2f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.top.toFloat())

        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())

        path.lineTo((rect.right - adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())

        path.close()
        return path
    }

    private fun getUpDownArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.25f)
            }


            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.25f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2).toFloat())

        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())

        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj2).toFloat())
        path.close()

        return path
    }

    private fun getQuadArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adjLR1 = 0
        var adjLR2 = 0
        var adjLR3 = 0

        var adjUD1 = 0
        var adjUD2 = 0
        var adjUD3 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adjLR1 = Math.round(rect.height() * values[0]!!)
                adjUD1 = Math.round(rect.width() * values[0]!!)
            } else {
                adjLR1 = Math.round(rect.height() * 0.3f)
                adjUD1 = Math.round(rect.width() * 0.3f)
            }

            if (values.size >= 2 && values[1] != null) {
                adjLR2 = Math.round(rect.height() * values[1]!!)
                adjUD2 = Math.round(rect.width() * values[1]!!)
            } else {
                adjLR2 = Math.round(rect.height() * 0.4f)
                adjUD2 = Math.round(rect.width() * 0.4f)
            }

            if (values.size >= 3 && values[2] != null) {
                adjLR3 = Math.round(rect.width() * values[2]!!)
                adjUD3 = Math.round(rect.height() * values[2]!!)
            } else {
                adjLR3 = Math.round(rect.width() * 0.2f)
                adjUD3 = Math.round(rect.height() * 0.2f)
            }
        } else {
            adjLR1 = Math.round(rect.height() * 0.3f)
            adjLR2 = Math.round(rect.height() * 0.4f)
            adjLR3 = Math.round(rect.width() * 0.2f)

            adjUD1 = Math.round(rect.width() * 0.3f)
            adjUD2 = Math.round(rect.width() * 0.4f)
            adjUD3 = Math.round(rect.height() * 0.2f)
        }


        //left
        path.moveTo((rect.left + adjUD2).toFloat(), (rect.bottom - adjLR2).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.bottom - adjLR2).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.bottom - adjLR1).toFloat())
        path.lineTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.top + adjLR1).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.top + adjLR2).toFloat())


        //up
        path.lineTo((rect.left + adjUD2).toFloat(), (rect.top + adjLR2).toFloat())
        path.lineTo((rect.left + adjUD2).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo((rect.left + adjUD1).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adjUD1).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.top + adjUD3).toFloat())


        //right
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.top + adjLR2).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.top + adjLR2).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.top + adjLR1).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.bottom - adjLR1).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.bottom - adjLR2).toFloat())


        //down
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.bottom - adjLR2).toFloat())
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.bottom - adjUD3).toFloat())
        path.lineTo((rect.right - adjUD1).toFloat(), (rect.bottom - adjUD3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adjUD1).toFloat(), (rect.bottom - adjUD3).toFloat())
        path.lineTo((rect.left + adjUD2).toFloat(), (rect.bottom - adjUD3).toFloat())

        path.close()

        return path
    }

    private fun getLeftRightUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adjLR1 = 0
        var adjLR2 = 0
        var adjLR3 = 0

        var adjUD1 = 0
        var adjUD2 = 0
        var adjUD3 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adjLR1 = Math.round(rect.height() * (0.5f - values[0]!!) * 10 / 7)
                adjUD1 = Math.round(rect.width() * values[0]!!)
            } else {
                adjLR1 = Math.round(rect.height() * 0.2f * 10 / 7)
                adjUD1 = Math.round(rect.width() * 0.3f)
            }

            if (values.size >= 2 && values[1] != null) {
                adjLR2 = Math.round(rect.height() * (0.5f - values[1]!!) * 10 / 7)
                adjUD2 = Math.round(rect.width() * values[1]!!)
            } else {
                adjLR2 = Math.round(rect.height() * 0.1f * 10 / 7)
                adjUD2 = Math.round(rect.width() * 0.4f)
            }

            if (values.size >= 3 && values[2] != null) {
                adjLR3 = Math.round(rect.width() * values[2]!! * 0.7f)
                adjUD3 = Math.round(rect.height() * values[2]!!)
            } else {
                adjLR3 = Math.round(rect.width() * 0.2f * 0.7f)
                adjUD3 = Math.round(rect.height() * 0.2f)
            }
        } else {
            adjLR1 = Math.round(rect.height() * 0.2f * 10 / 7)
            adjLR2 = Math.round(rect.height() * 0.1f * 10 / 7)
            adjLR3 = Math.round(rect.width() * 0.3f * 0.7f)

            adjUD1 = Math.round(rect.width() * 0.3f)
            adjUD2 = Math.round(rect.width() * 0.4f)
            adjUD3 = Math.round(rect.height() * 0.2f)
        }


        //left
        path.moveTo((rect.left + adjLR3).toFloat(), (rect.bottom - adjLR1 + adjLR2).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adjLR1).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.bottom - adjLR1 * 2).toFloat())
        path.lineTo((rect.left + adjLR3).toFloat(), (rect.bottom - adjLR1 - adjLR2).toFloat())


        //up
        path.lineTo((rect.left + adjUD2).toFloat(), (rect.bottom - adjLR1 - adjLR2).toFloat())
        path.lineTo((rect.left + adjUD2).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo((rect.left + adjUD1).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adjUD1).toFloat(), (rect.top + adjUD3).toFloat())
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.top + adjUD3).toFloat())


        //right
        path.lineTo((rect.right - adjUD2).toFloat(), (rect.bottom - adjLR1 - adjLR2).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.bottom - adjLR1 - adjLR2).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.bottom - adjLR1 * 2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adjLR1).toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adjLR3).toFloat(), (rect.bottom - adjLR1 + adjLR2).toFloat())

        path.close()

        return path
    }

    private fun getBentArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.7f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.125f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.7f)
            adj2 = Math.round(rect.height() * 0.125f)
        }

        val arrowHeight = rect.height() * 0.57f

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.top + arrowHeight)

        s_rect.set(
            rect.left.toFloat(),
            (rect.top + adj2).toFloat(),
            rect.left + rect.width() * 1.04f,
            rect.top + arrowHeight + (arrowHeight - adj2)
        )
        path.arcTo(s_rect, 180f, 90f)

        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + arrowHeight / 2)
        path.lineTo((rect.left + adj1).toFloat(), rect.top + arrowHeight)
        path.lineTo(
            (rect.left + adj1).toFloat(),
            rect.top + arrowHeight / 2 + (arrowHeight - adj2 * 2) / 2
        )

        val smallOvalAdj = (arrowHeight - adj2 * 2) / rect.height().toFloat()
        s_rect.set(
            rect.left + rect.width() * smallOvalAdj,
            rect.top + arrowHeight / 2 + (arrowHeight - adj2 * 2) / 2,
            rect.left + rect.width() * (0.57f + 0.57f - smallOvalAdj),
            rect.top + arrowHeight + arrowHeight - (arrowHeight / 2 + (arrowHeight - adj2 * 2) / 2)
        )
        path.arcTo(s_rect, 270f, -90f)

        path.lineTo(rect.left + rect.width() * smallOvalAdj, rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getUturnArrowPath(shape: AutoShape?, rect: Rect): Path {
        val width = rect.width()
        val height = rect.height()

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.top + height * 0.38f)

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right - width * (0.28f - 0.14f),
            rect.top + height * 0.76f
        )
        path.arcTo(s_rect, 180f, 180f)

        path.lineTo(rect.right.toFloat(), rect.top + height * 0.38f)
        path.lineTo(rect.right - width * 0.28f, rect.top + height * 0.66f)
        path.lineTo(rect.right - width * 0.56f, rect.top + height * 0.38f)
        path.lineTo(rect.right - width * (0.28f + 0.14f), rect.top + height * 0.38f)

        s_rect.set(
            rect.left + width * 0.28f,
            rect.top + height * 0.28f,
            rect.right - width * (0.28f + 0.14f),
            rect.top + height * (0.38f + 0.38f - 0.28f)
        )
        path.arcTo(s_rect, 0f, -180f)

        path.lineTo(rect.left + width * 0.28f, rect.bottom.toFloat())

        path.close()

        return path
    }

    private fun getLeftUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var upAdj1 = 0f
        var upAdj2 = 0f
        var upAdj3 = 0f

        var downAdj1 = 0f
        var downAdj2 = 0f
        var downAdj3 = 0f

        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                upAdj1 = rect.width() * values[0]!!
                downAdj1 = rect.height() * values[0]!!
            } else {
                upAdj1 = Math.round(rect.width() * 0.43f).toFloat()
                downAdj1 = rect.height() * 0.43f
            }

            if (values.size >= 2 && values[1] != null) {
                upAdj2 = rect.width() * values[1]!!
                downAdj2 = rect.height() * values[1]!!
            } else {
                upAdj2 = Math.round(rect.width() * 0.86f).toFloat()
                downAdj2 = rect.height() * 0.86f
            }

            if (values.size >= 3 && values[2] != null) {
                upAdj3 = rect.height() * values[2]!!
                downAdj3 = rect.width() * values[2]!!
            } else {
                upAdj3 = Math.round(rect.height() * 0.28f).toFloat()
                downAdj3 = rect.width() * 0.28f
            }
        } else {
            upAdj1 = Math.round(rect.width() * 0.43f).toFloat()
            upAdj2 = Math.round(rect.width() * 0.86f).toFloat()
            upAdj3 = Math.round(rect.height() * 0.28f).toFloat()

            downAdj1 = rect.height() * 0.43f
            downAdj2 = rect.height() * 0.86f
            downAdj3 = rect.width() * 0.28f
        }


        //left
        var arrowHeaderWidth = (rect.height() - downAdj1)
        var arrowTailWidth = arrowHeaderWidth - (rect.height() - downAdj2) * 2
        path.moveTo(rect.left + downAdj3, rect.top + downAdj2)
        path.lineTo(rect.left + downAdj3, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - arrowHeaderWidth / 2)
        path.lineTo(rect.left + downAdj3, rect.top + downAdj1)
        path.lineTo(rect.left + downAdj3, rect.top + downAdj2 - arrowTailWidth)


        //up
        arrowHeaderWidth = (rect.width() - upAdj1)
        path.lineTo(
            rect.left + upAdj2 - (arrowHeaderWidth - (rect.width() - upAdj2) * 2),
            rect.top + downAdj2 - arrowTailWidth
        )
        arrowTailWidth = arrowHeaderWidth - (rect.width() - upAdj2) * 2

        path.lineTo(rect.left + upAdj2 - arrowTailWidth, rect.top + upAdj3)
        path.lineTo(rect.left + upAdj1, rect.top + upAdj3)
        path.lineTo(rect.right - arrowHeaderWidth / 2, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + upAdj3)
        path.lineTo(rect.left + upAdj2, rect.top + upAdj3)
        path.lineTo(rect.left + upAdj2, rect.top + downAdj2)
        path.close()

        return path
    }

    private fun getBentUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f

        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = rect.width() * values[0]!!
            } else {
                adj1 = Math.round(rect.width() * 0.43f).toFloat()
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = rect.width() * values[1]!!
            } else {
                adj2 = Math.round(rect.width() * 0.86f).toFloat()
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = rect.height() * values[2]!!
            } else {
                adj3 = Math.round(rect.height() * 0.28f).toFloat()
            }
        } else {
            adj1 = Math.round(rect.width() * 0.43f).toFloat()
            adj2 = Math.round(rect.width() * 0.86f).toFloat()
            adj3 = Math.round(rect.height() * 0.28f).toFloat()
        }


        //up
        val arrowHeaderWidth = (rect.width() - adj1)
        var arrowTailWidth = arrowHeaderWidth - (rect.width() - adj2) * 2
        path.moveTo(rect.left + adj2 - arrowTailWidth, rect.top + adj3)
        path.lineTo(rect.left + adj1, rect.top + adj3)
        path.lineTo(rect.right - arrowHeaderWidth / 2, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + adj3)
        path.lineTo(rect.left + adj2, rect.top + adj3)

        path.lineTo(rect.left + adj2, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())

        arrowTailWidth = rect.height() * arrowTailWidth / rect.width()
        path.lineTo(rect.left.toFloat(), rect.bottom - arrowTailWidth)
        path.lineTo(
            rect.left + adj2 - (arrowHeaderWidth - (rect.width() - adj2) * 2),
            rect.bottom - arrowTailWidth
        )

        path.close()

        return path
    }

    private fun getCurvedRightArrowPath(shape: AutoShape, rect: Rect): MutableList<Path?> {
        val pathList: MutableList<Path?> = ArrayList<Path?>(2)

        val values = shape.getAdjustData()
        var path = Path()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = rect.height() * values[0]!!
            } else {
                adj1 = Math.round(rect.height() * 0.6f).toFloat()
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = rect.height() * values[1]!!
            } else {
                adj2 = Math.round(rect.height() * 0.9f).toFloat()
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = rect.width() * values[2]!!
            } else {
                adj3 = Math.round(rect.width() * 0.66667f).toFloat()
            }
        } else {
            adj1 = Math.round(rect.height() * 0.6f).toFloat()
            adj2 = Math.round(rect.height() * 0.9f).toFloat()
            adj3 = Math.round(rect.width() * 0.66667f).toFloat()
        }

        val arrowHeight = rect.height() * 0.4f
        val d2 = rect.height() - adj1
        var d1 = arrowHeight - (arrowHeight - (adj2 - adj1)) * 2
        if (d1 < 0) {
            d1 = 0f
        }

        adj1 = d1
        adj2 = d2
        adj3 = rect.width() - adj3

        val ovalWidth = (2 * rect.width()).toFloat()
        val ovalHeight = (rect.bottom - adj2 / 2 - adj1 / 2 - rect.top)

        path.moveTo(rect.right.toFloat(), rect.top.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.left + ovalWidth,
            rect.top + ovalHeight
        )
        path.arcTo(s_rect, 270f, -90f)
        path.lineTo(rect.left.toFloat(), rect.top + ovalHeight / 2 + adj1)
        s_rect.set(
            rect.left.toFloat(),
            rect.top + adj1,
            rect.left + ovalWidth,
            rect.top + ovalHeight + adj1
        )
        path.arcTo(s_rect, 180f, 90f)
        path.close()

        pathList.add(path)

        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top + ovalHeight / 2)

        val y = sqrt(
            (ovalHeight / 2).toDouble().pow(2.0) * ((ovalWidth / 2).toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / (ovalWidth / 2).toDouble().pow(2.0)
        )

        val angle = (atan(y / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.left + ovalWidth,
            rect.top + ovalHeight
        )
        path.arcTo(s_rect, 180f, -angle.toFloat())
        path.setLastPoint(rect.right - adj3, rect.top + ovalHeight / 2 + y.toInt())

        path.lineTo(rect.right - adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 - adj2 / 2)
        path.lineTo(rect.right.toFloat(), rect.bottom - adj2 / 2)
        path.lineTo(rect.right - adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 + adj2 / 2)
        path.lineTo(rect.right - adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1)

        s_rect.set(
            rect.left.toFloat(),
            rect.top + adj1,
            rect.left + ovalWidth,
            rect.top + ovalHeight + adj1
        )
        path.arcTo(s_rect, (180 - angle).toFloat(), angle.toFloat())
        path.close()

        pathList.add(path)

        return pathList
    }

    private fun getCurvedLeftArrowPath(shape: AutoShape, rect: Rect): MutableList<Path?> {
        val pathList: MutableList<Path?> = ArrayList<Path?>(2)

        val values = shape.getAdjustData()
        var path = Path()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = rect.height() * values[0]!!
            } else {
                adj1 = Math.round(rect.height() * 0.6f).toFloat()
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = rect.height() * values[1]!!
            } else {
                adj2 = Math.round(rect.height() * 0.9f).toFloat()
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = rect.width() * values[2]!!
            } else {
                adj3 = Math.round(rect.width() * 0.66667f).toFloat()
            }
        } else {
            adj1 = Math.round(rect.height() * 0.6f).toFloat()
            adj2 = Math.round(rect.height() * 0.9f).toFloat()
            adj3 = Math.round(rect.width() * 0.66667f).toFloat()
        }

        val arrowHeight = rect.height() * 0.4f
        val d2 = rect.height() - adj1
        var d1 = arrowHeight - (arrowHeight - (adj2 - adj1)) * 2
        if (d1 < 0) {
            d1 = 0f
        }

        adj1 = d1
        adj2 = d2

        val ovalWidth = (2 * rect.width()).toFloat()
        val ovalHeight = (rect.bottom - adj2 / 2 - adj1 / 2 - rect.top)

        path.moveTo(rect.right.toFloat(), rect.top + ovalHeight / 2)

        val y = sqrt(
            (ovalHeight / 2).toDouble().pow(2.0) * ((ovalWidth / 2).toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / (ovalWidth / 2).toDouble().pow(2.0)
        )

        val angle = (atan(y / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            rect.right - ovalWidth,
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.top + ovalHeight
        )
        path.arcTo(s_rect, 0f, angle.toFloat())
        path.setLastPoint(rect.left + adj3, rect.top + ovalHeight / 2 + y.toInt())
        path.lineTo(rect.left + adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 - adj2 / 2)
        path.lineTo(rect.left.toFloat(), rect.bottom - adj2 / 2)
        path.lineTo(rect.left + adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 + adj2 / 2)
        path.lineTo(rect.left + adj3, rect.top + ovalHeight / 2 + y.toInt() + adj1)
        s_rect.set(
            rect.right - ovalWidth,
            rect.top + adj1,
            rect.right.toFloat(),
            rect.top + ovalHeight + adj1
        )
        path.arcTo(s_rect, angle.toFloat(), -angle.toFloat())
        path.close()
        pathList.add(path)

        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        s_rect.set(
            rect.right - ovalWidth,
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.top + ovalHeight
        )
        path.arcTo(s_rect, 270f, 90f)
        path.lineTo(rect.right.toFloat(), rect.top + ovalHeight / 2 + adj1)
        s_rect.set(
            rect.right - ovalWidth,
            rect.top + adj1,
            rect.right.toFloat(),
            rect.top + ovalHeight + adj1
        )
        path.arcTo(s_rect, 0f, -90f)
        path.close()

        pathList.add(path)

        return pathList
    }

    private fun getCurvedUpArrowPath(shape: AutoShape, rect: Rect): MutableList<Path?> {
        val pathList: MutableList<Path?> = ArrayList<Path?>(2)

        val values = shape.getAdjustData()
        var path = Path()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = rect.width() * values[0]!!
            } else {
                adj1 = Math.round(rect.width() * 0.6f).toFloat()
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = rect.width() * values[1]!!
            } else {
                adj2 = Math.round(rect.width() * 0.9f).toFloat()
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = rect.height() * values[2]!!
            } else {
                adj3 = Math.round(rect.height() * 0.66667f).toFloat()
            }
        } else {
            adj1 = Math.round(rect.width() * 0.6f).toFloat()
            adj2 = Math.round(rect.width() * 0.9f).toFloat()
            adj3 = Math.round(rect.height() * 0.33333f).toFloat()
        }

        val arrowWidth = rect.width() * 0.4f
        val d2 = rect.width() - adj1
        var d1 = arrowWidth - (arrowWidth - (adj2 - adj1)) * 2
        if (d1 < 0) {
            d1 = 0f
        }

        adj1 = d1
        adj2 = d2

        val ovalHalfWidth = (rect.width() - adj2 / 2 - adj1 / 2) / 2
        val ovalHalfHeight = rect.height().toFloat()

        path.moveTo(rect.left + ovalHalfWidth, rect.bottom.toFloat())

        val x = sqrt(
            ovalHalfWidth.toDouble().pow(2.0) * (ovalHalfHeight.toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / ovalHalfHeight.toDouble().pow(2.0)
        )
        val angle = (atan(x / adj3) * 180 / Math.PI).toInt()
        s_rect.set(
            rect.left.toFloat(),
            rect.top - ovalHalfHeight,
            rect.left + ovalHalfWidth * 2,
            rect.top + ovalHalfHeight
        )
        path.arcTo(s_rect, 90f, -angle.toFloat())
        path.setLastPoint(rect.left + ovalHalfWidth + x.toFloat(), rect.top + adj3)
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 - adj2 / 2, rect.top + adj3)
        path.lineTo(rect.right - adj2 / 2, rect.top.toFloat())
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 + adj2 / 2, rect.top + adj3)
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1, rect.top + adj3)

        s_rect.set(
            rect.left + adj1,
            rect.top - ovalHalfHeight,
            rect.left + ovalHalfWidth * 2 + adj1,
            rect.top + ovalHalfHeight
        )
        path.arcTo(s_rect, (90 - angle).toFloat(), angle.toFloat())
        path.close()
        pathList.add(path)


        //
        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            rect.top - ovalHalfHeight,
            rect.left + ovalHalfWidth * 2,
            rect.top + ovalHalfHeight
        )
        path.arcTo(s_rect, 180f, -90f)
        path.lineTo(rect.left + ovalHalfWidth + adj1, rect.bottom.toFloat())
        s_rect.set(
            rect.left + adj1,
            rect.top - ovalHalfHeight,
            rect.left + ovalHalfWidth * 2 + adj1,
            rect.top + ovalHalfHeight
        )
        path.arcTo(s_rect, 90f, 90f)
        path.close()
        pathList.add(path)

        return pathList
    }

    private fun getCurvedDownArrowPath(shape: AutoShape, rect: Rect): MutableList<Path?> {
        val pathList: MutableList<Path?> = ArrayList<Path?>(2)

        val values = shape.getAdjustData()
        var path = Path()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = rect.width() * values[0]!!
            } else {
                adj1 = Math.round(rect.width() * 0.6f).toFloat()
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = rect.width() * values[1]!!
            } else {
                adj2 = Math.round(rect.width() * 0.9f).toFloat()
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = rect.height() * values[2]!!
            } else {
                adj3 = Math.round(rect.height() * 0.66667f).toFloat()
            }
        } else {
            adj1 = Math.round(rect.width() * 0.6f).toFloat()
            adj2 = Math.round(rect.width() * 0.9f).toFloat()
            adj3 = Math.round(rect.height() * 0.666667f).toFloat()
        }

        val arrowWidth = rect.width() * 0.4f
        val d2 = rect.width() - adj1
        var d1 = arrowWidth - (arrowWidth - (adj2 - adj1)) * 2
        if (d1 < 0) {
            d1 = 0f
        }

        adj1 = d1
        adj2 = d2
        adj3 = rect.height() - adj3

        val ovalHalfWidth = (rect.width() - adj2 / 2 - adj1 / 2) / 2
        val ovalHalfHeight = rect.height().toFloat()
        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.left + ovalHalfWidth * 2,
            rect.top + ovalHalfHeight * 2
        )
        path.arcTo(s_rect, 180f, 90f)
        path.lineTo(rect.left + ovalHalfWidth + adj1, rect.top.toFloat())
        s_rect.set(
            rect.left + adj1,
            rect.top.toFloat(),
            rect.left + ovalHalfWidth * 2 + adj1,
            rect.top + ovalHalfHeight * 2
        )
        path.arcTo(s_rect, 270f, -90f)
        path.close()
        pathList.add(path)


        //
        path = Path()
        path.moveTo(rect.left + ovalHalfWidth, rect.top.toFloat())

        val x = sqrt(
            ovalHalfWidth.toDouble().pow(2.0) * (ovalHalfHeight.toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / ovalHalfHeight.toDouble().pow(2.0)
        )
        val angle = (atan(x / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.left + ovalHalfWidth * 2,
            rect.top + ovalHalfHeight * 2
        )
        path.arcTo(s_rect, 270f, angle.toFloat())
        path.setLastPoint(rect.left + ovalHalfWidth + x.toFloat(), rect.bottom - adj3)
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 - adj2 / 2,
            rect.bottom - adj3
        )
        path.lineTo(rect.right - adj2 / 2, rect.bottom.toFloat())
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 + adj2 / 2,
            rect.bottom - adj3
        )
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1, rect.bottom - adj3)

        s_rect.set(
            rect.left + adj1,
            rect.top.toFloat(),
            rect.left + ovalHalfWidth * 2 + adj1,
            rect.top + ovalHalfHeight * 2
        )
        path.arcTo(s_rect, (270 + angle).toFloat(), -angle.toFloat())
        path.close()

        pathList.add(path)

        return pathList
    }

    private fun getStripedRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.75f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.75f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        val len = rect.width() * 0.03f

        path.addRect(
            rect.left.toFloat(),
            (rect.top + adj2).toFloat(),
            rect.left + len,
            (rect.bottom - adj2).toFloat(),
            Path.Direction.CW
        )

        path.addRect(
            rect.left + len * 2,
            (rect.top + adj2).toFloat(),
            rect.left + len * 4,
            (rect.bottom - adj2).toFloat(),
            Path.Direction.CW
        )

        path.moveTo(rect.left + len * 5, (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())


        //right arrow
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())

        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.left + len * 5, (rect.bottom - adj2).toFloat())
        path.close()

        return path
    }

    private fun getNotchedRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.75f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.75f)
            adj2 = Math.round(rect.height() * 0.25f)
        }

        path.moveTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(
            rect.left + (rect.height() - adj2 * 2) * (rect.width() - adj1) / rect.height()
                .toFloat(), rect.centerY().toFloat()
        )
        path.lineTo(rect.left.toFloat(), (rect.top + adj2).toFloat())

        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.close()

        return path
    }

    private fun getHomePlatePath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        if (values != null && values.size == 1 && values[0] != null) {
            adj1 = Math.round(rect.width() * values[0]!!)
        } else {
            adj1 = Math.round(rect.width() * 0.75f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getChevronPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        if (values != null && values.size == 1 && values[0] != null) {
            adj1 = Math.round(rect.width() * values[0]!!)
        } else {
            adj1 = Math.round(rect.width() * 0.75f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + rect.width() - adj1).toFloat(), rect.centerY().toFloat())
        path.close()

        return path
    }

    private fun getRightArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.67f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.width() * values[2]!!)
            } else {
                adj3 = Math.round(rect.width() * 0.83f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.height() * values[3]!!)
            } else {
                adj4 = Math.round(rect.height() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.67f)
            adj2 = Math.round(rect.height() * 0.25f)
            adj3 = Math.round(rect.width() * 0.83f)
            adj4 = Math.round(rect.height() * 0.375f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getLeftArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.33f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.width() * values[2]!!)
            } else {
                adj3 = Math.round(rect.width() * 0.17f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.height() * values[3]!!)
            } else {
                adj4 = Math.round(rect.height() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.33f)
            adj2 = Math.round(rect.height() * 0.25f)
            adj3 = Math.round(rect.width() * 0.17f)
            adj4 = Math.round(rect.height() * 0.375f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2).toFloat())
        path.close()

        return path
    }

    private fun getUpArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.height() * values[0]!!)
            } else {
                adj1 = Math.round(rect.height() * 0.33f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.width() * values[1]!!)
            } else {
                adj2 = Math.round(rect.width() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.height() * values[2]!!)
            } else {
                adj3 = Math.round(rect.height() * 0.17f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.width() * values[3]!!)
            } else {
                adj4 = Math.round(rect.width() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.height() * 0.33f)
            adj2 = Math.round(rect.width() * 0.25f)
            adj3 = Math.round(rect.height() * 0.17f)
            adj4 = Math.round(rect.width() * 0.375f)
        }

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())

        path.close()

        return path
    }

    private fun getDownArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.height() * values[0]!!)
            } else {
                adj1 = Math.round(rect.height() * 0.67f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.width() * values[1]!!)
            } else {
                adj2 = Math.round(rect.width() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.height() * values[2]!!)
            } else {
                adj3 = Math.round(rect.height() * 0.83f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.width() * values[3]!!)
            } else {
                adj4 = Math.round(rect.width() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.height() * 0.67f)
            adj2 = Math.round(rect.width() * 0.25f)
            adj3 = Math.round(rect.height() * 0.83f)
            adj4 = Math.round(rect.width() * 0.375f)
        }

        path.moveTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj1).toFloat())

        path.close()
        return path
    }

    private fun getLeftRightArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.width() * values[0]!!)
            } else {
                adj1 = Math.round(rect.width() * 0.35f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.height() * values[1]!!)
            } else {
                adj2 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.width() * values[2]!!)
            } else {
                adj3 = Math.round(rect.width() * 0.13f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.height() * values[3]!!)
            } else {
                adj4 = Math.round(rect.height() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.width() * 0.35f)
            adj2 = Math.round(rect.height() * 0.25f)
            adj3 = Math.round(rect.width() * 0.13f)
            adj4 = Math.round(rect.height() * 0.375f)
        }


        //left arrow
        path.moveTo((rect.left + adj1).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj4).toFloat())

        path.lineTo((rect.left + adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.top.toFloat())


        //right arrow
        path.lineTo((rect.right - adj1).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.bottom - adj4).toFloat())

        path.lineTo((rect.right - adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getUpDownArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = Math.round(rect.height() * values[0]!!)
            } else {
                adj1 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = Math.round(rect.width() * values[1]!!)
            } else {
                adj2 = Math.round(rect.width() * 0.25f)
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = Math.round(rect.height() * values[2]!!)
            } else {
                adj3 = Math.round(rect.height() * 0.125f)
            }

            if (values.size >= 4 && values[3] != null) {
                adj4 = Math.round(rect.width() * values[3]!!)
            } else {
                adj4 = Math.round(rect.width() * 0.375f)
            }
        } else {
            adj1 = Math.round(rect.height() * 0.25f)
            adj2 = Math.round(rect.width() * 0.25f)
            adj3 = Math.round(rect.height() * 0.125f)
            adj4 = Math.round(rect.width() * 0.375f)
        }


        //up part
        path.moveTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj1).toFloat())

        path.lineTo(rect.right.toFloat(), (rect.bottom - adj1).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.bottom - adj1).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.bottom - adj1).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj1).toFloat())

        path.close()

        return path
    }

    private fun getQuadArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var lrAdj1 = 0
        var lrAdj2 = 0
        var lrAdj3 = 0
        var lrAdj4 = 0

        var udAdj1 = 0
        var udAdj2 = 0
        var udAdj3 = 0
        var udAdj4 = 0

        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                lrAdj1 = Math.round(rect.width() * values[0]!!)
                udAdj1 = Math.round(rect.height() * values[0]!!)
            } else {
                lrAdj1 = Math.round(rect.width() * 0.25f)
                udAdj1 = Math.round(rect.height() * 0.25f)
            }

            if (values.size >= 2 && values[1] != null) {
                lrAdj2 = Math.round(rect.height() * values[1]!!)
                udAdj2 = Math.round(rect.width() * values[1]!!)
            } else {
                lrAdj2 = Math.round(rect.height() * 0.375f)
                udAdj2 = Math.round(rect.width() * 0.375f)
            }

            if (values.size >= 3 && values[2] != null) {
                lrAdj3 = Math.round(rect.width() * values[2]!!)
                udAdj3 = Math.round(rect.height() * values[2]!!)
            } else {
                lrAdj3 = Math.round(rect.width() * 0.125f)
                udAdj3 = Math.round(rect.height() * 0.125f)
            }

            if (values.size >= 4 && values[3] != null) {
                lrAdj4 = Math.round(rect.height() * values[3]!!)
                udAdj4 = Math.round(rect.width() * values[3]!!)
            } else {
                lrAdj4 = Math.round(rect.height() * 0.45f)
                udAdj4 = Math.round(rect.width() * 0.45f)
            }
        } else {
            lrAdj1 = Math.round(rect.width() * 0.25f)
            lrAdj2 = Math.round(rect.height() * 0.375f)
            lrAdj3 = Math.round(rect.width() * 0.125f)
            lrAdj4 = Math.round(rect.height() * 0.45f)

            udAdj1 = Math.round(rect.height() * 0.25f)
            udAdj2 = Math.round(rect.width() * 0.375f)
            udAdj3 = Math.round(rect.height() * 0.125f)
            udAdj4 = Math.round(rect.width() * 0.45f)
        }


        //left
        path.moveTo((rect.left + lrAdj1).toFloat(), (rect.bottom - lrAdj4).toFloat())
        path.lineTo((rect.left + lrAdj3).toFloat(), (rect.bottom - lrAdj4).toFloat())
        path.lineTo((rect.left + lrAdj3).toFloat(), (rect.bottom - lrAdj2).toFloat())
        path.lineTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + lrAdj3).toFloat(), (rect.top + lrAdj2).toFloat())
        path.lineTo((rect.left + lrAdj3).toFloat(), (rect.top + lrAdj4).toFloat())
        path.lineTo((rect.left + lrAdj1).toFloat(), (rect.top + lrAdj4).toFloat())
        path.lineTo((rect.left + lrAdj1).toFloat(), (rect.top + udAdj1).toFloat())


        //up
        path.lineTo((rect.left + udAdj4).toFloat(), (rect.top + udAdj1).toFloat())
        path.lineTo((rect.left + udAdj4).toFloat(), (rect.top + udAdj3).toFloat())
        path.lineTo((rect.left + udAdj2).toFloat(), (rect.top + udAdj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - udAdj2).toFloat(), (rect.top + udAdj3).toFloat())
        path.lineTo((rect.right - udAdj4).toFloat(), (rect.top + udAdj3).toFloat())
        path.lineTo((rect.right - udAdj4).toFloat(), (rect.top + udAdj1).toFloat())
        path.lineTo((rect.right - lrAdj1).toFloat(), (rect.top + udAdj1).toFloat())


        //right
        path.lineTo((rect.right - lrAdj1).toFloat(), (rect.top + lrAdj4).toFloat())
        path.lineTo((rect.right - lrAdj3).toFloat(), (rect.top + lrAdj4).toFloat())
        path.lineTo((rect.right - lrAdj3).toFloat(), (rect.top + lrAdj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - lrAdj3).toFloat(), (rect.bottom - lrAdj2).toFloat())
        path.lineTo((rect.right - lrAdj3).toFloat(), (rect.bottom - lrAdj4).toFloat())
        path.lineTo((rect.right - lrAdj1).toFloat(), (rect.bottom - lrAdj4).toFloat())
        path.lineTo((rect.right - lrAdj1).toFloat(), (rect.bottom - udAdj1).toFloat())


        //down
        path.lineTo((rect.right - udAdj4).toFloat(), (rect.bottom - udAdj1).toFloat())
        path.lineTo((rect.right - udAdj4).toFloat(), (rect.bottom - udAdj3).toFloat())
        path.lineTo((rect.right - udAdj2).toFloat(), (rect.bottom - udAdj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + udAdj2).toFloat(), (rect.bottom - udAdj3).toFloat())
        path.lineTo((rect.left + udAdj4).toFloat(), (rect.bottom - udAdj3).toFloat())
        path.lineTo((rect.left + udAdj4).toFloat(), (rect.bottom - udAdj1).toFloat())
        path.lineTo((rect.left + lrAdj1).toFloat(), (rect.bottom - udAdj1).toFloat())

        path.close()
        return path
    }

    private fun getCircularArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0f
        var adj2 = 0f
        var adj3 = 0f
        val len = 100
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                adj1 = values[0]!! * TODEGREE
                if (adj1 < 0) {
                    adj1 += 360f
                }
            } else {
                adj1 = 180f
            }

            if (values.size >= 2 && values[1] != null) {
                adj2 = values[1]!! * TODEGREE
                if (adj2 < 0) {
                    adj2 += 360f
                }
            } else {
                adj2 = 0f
            }

            if (values.size >= 3 && values[2] != null) {
                adj3 = values[2]!! * len
            } else {
                adj3 = len * 0.25f
            }
        } else {
            adj1 = 180f
            adj2 = 0f
            adj3 = len * 0.25f
        }

        val outerRadius = (len / 2).toFloat()
        path.moveTo(
            (outerRadius * cos(adj1 * Math.PI / 180)).toFloat(),
            (outerRadius * sin(adj1 * Math.PI / 180)).toFloat()
        )

        s_rect.set(-outerRadius, -outerRadius, outerRadius, outerRadius)
        path.arcTo(s_rect, adj1, (adj2 - adj1 + 360) % 360)


        //arrow
        path.lineTo(
            ((outerRadius + len * 0.125f) * cos(adj2 * Math.PI / 180)).toFloat(),
            ((outerRadius + len * 0.125f) * sin(adj2 * Math.PI / 180)).toFloat()
        )

        path.lineTo(
            ((outerRadius + adj3) * 0.5f * cos((adj2 + 30) * Math.PI / 180)).toFloat(),
            ((outerRadius + adj3) * 0.5f * sin((adj2 + 30) * Math.PI / 180)).toFloat()
        )

        path.lineTo(
            ((adj3 - len * 0.125f) * cos(adj2 * Math.PI / 180)).toFloat(),
            ((adj3 - len * 0.125f) * sin(adj2 * Math.PI / 180)).toFloat()
        )


        s_rect.set(-adj3, -adj3, adj3, adj3)
        path.arcTo(s_rect, adj2, -(adj2 - adj1 + 360) % 360)

        path.close()

        val m = Matrix()
        m.postScale(rect.width() / 100f, rect.height() / 100f)
        path.transform(m)

        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }
}
