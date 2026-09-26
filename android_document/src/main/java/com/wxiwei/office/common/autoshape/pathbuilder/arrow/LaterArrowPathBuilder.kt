/*
 * 文件名称:          LaterArrowPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:43:56
 */
package com.wxiwei.office.common.autoshape.pathbuilder.arrow

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.min
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
object LaterArrowPathBuilder {
    private val TODEGREE = 36000000f / 21600000f

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

            ShapeTypes.QuadArrowCallout -> return getQuadArrowCalloutPath(shape, rect)

            ShapeTypes.CircularArrow -> return getCircularArrowPath(shape, rect)
        }

        return Path()
    }

    private fun getRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.height() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo(rect.left.toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.centerY() + adj1).toFloat())
        path.close()

        return path
    }

    private fun getLeftArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.height() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj2).toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), rect.bottom.toFloat())
        path.close()
        return path
    }

    private fun getUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.width() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj2).toFloat())
        path.close()
        return path
    }

    private fun getDownArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.width() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo((rect.centerX() - adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.close()
        return path
    }

    private fun getLeftRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.height() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)

            if (adj2 * 2 > rect.width()) {
                adj2 = len2 * 2
            }
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj2).toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj2).toFloat(), rect.bottom.toFloat())

        path.close()
        return path
    }

    private fun getUpDownArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var adj1 = 0
        var adj2 = 0
        val len1 = rect.width() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)

            if (adj2 * 2 > rect.height()) {
                adj2 = len2 * 2
            }
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.top + adj2).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj2).toFloat())

        path.close()
        return path
    }

    private fun getQuadArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len1 = min(rect.width(), rect.height()) / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
            adj3 = Math.round(len2 * values[2]!!)

            if (adj1 > adj2) {
                adj1 = adj2
            }

            if (adj2 + adj3 > len2 / 2) {
                adj3 = len2 / 2 - adj2
            }
        } else {
            adj1 = Math.round(len1 * 0.225f)
            adj2 = Math.round(len2 * 0.225f)
            adj3 = Math.round(len2 * 0.225f)
        }


        //left arrow
        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj1).toFloat())


        //up
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.top + adj3).toFloat())


        //right
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.centerY() + adj1).toFloat())


        //down
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.centerY() + adj1).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj2).toFloat())

        path.close()
        return path
    }

    private fun getLeftRightUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!! / 2)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)

            if (adj1 > adj2) {
                adj1 = adj2
            }

            if (adj2 + adj3 > rect.width()) {
                adj3 = len / 2 - adj2
            }

            if (adj2 * 2 + adj3 > rect.height()) {
                adj3 = rect.height() - adj2 * 2
            }
        } else {
            adj1 = Math.round(len * 0.225f / 2)
            adj2 = Math.round(len * 0.225f)
            adj3 = Math.round(len * 0.225f)
        }


        //left arrow
        path.moveTo((rect.left + adj3).toFloat(), (rect.bottom - adj2 + adj1).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2 * 2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2 - adj1).toFloat())
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.bottom - adj2 - adj1).toFloat())


        //up arrow
        path.lineTo((rect.centerX() - adj1).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1).toFloat(), (rect.bottom - adj2 - adj1).toFloat())


        //right arrow
        path.lineTo((rect.right - adj3).toFloat(), (rect.bottom - adj2 - adj1).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.bottom - adj2 * 2).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.bottom - adj2 + adj1).toFloat())

        path.close()

        return path
    }

    private fun getBentArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(len * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj3 + adj4 > rect.width()) {
                adj4 = rect.width() - adj3
            }
            if (adj4 > rect.height()) {
                adj4 = rect.height()
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(len * 0.4375f)
        }

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj2 - adj1 / 2 + adj4).toFloat())
        s_rect.set(
            rect.left.toFloat(),
            (rect.top + adj2 - adj1 / 2).toFloat(),
            (rect.left + 2 * adj4).toFloat(),
            (rect.top + adj2 - adj1 / 2 + 2 * adj4).toFloat()
        )
        path.arcTo(s_rect, 180f, 90f)

        path.lineTo((rect.right - adj3).toFloat(), (rect.top + adj2 - adj1 / 2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.top + adj2 * 2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.top + adj2 + adj1 / 2).toFloat())

        if (adj4 >= adj1) {
            path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj2 + adj1 / 2).toFloat())

            s_rect.set(
                (rect.left + adj1).toFloat(),
                (rect.top + adj2 + adj1 / 2).toFloat(),
                (rect.left + 2 * (adj4 - adj1)).toFloat(),
                (rect.top + adj2 + adj1 / 2 + 2 * (adj4 - adj1)).toFloat()
            )
            path.arcTo(s_rect, 270f, -90f)
            path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2 - adj1 / 2 + adj4).toFloat())
        } else {
            path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj2 + adj1 / 2).toFloat())
        }

        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getUturnArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        var adj5 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 5) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(len * values[3]!!)
            adj5 = Math.round(rect.height() * values[4]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj4 + adj3 >= adj5) {
                adj5 = adj3 + adj4
            }

            if (adj5 > rect.height()) {
                adj5 = rect.height()
                adj3 = adj5 - adj4
            }

            if (adj5 - adj3 < adj1) {
                adj3 = adj5 - adj1
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(len * 0.4375f)
            adj5 = Math.round(rect.height() * 0.75f)
        }

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj4).toFloat())

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.left + 2 * adj4).toFloat(),
            (rect.top + 2 * adj4).toFloat()
        )
        path.arcTo(s_rect, 180f, 90f)
        path.lineTo((rect.right - adj2 + adj1 / 2 - adj4).toFloat(), rect.top.toFloat())

        s_rect.set(
            (rect.right - adj2 + adj1 / 2 - 2 * adj4).toFloat(),
            rect.top.toFloat(),
            (rect.right - adj2 + adj1 / 2).toFloat(),
            (rect.top + 2 * adj4).toFloat()
        )
        path.arcTo(s_rect, 270f, 90f)

        path.lineTo((rect.right - adj2 + adj1 / 2).toFloat(), (rect.top + adj5 - adj3).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj5 - adj3).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.top + adj5).toFloat())
        path.lineTo((rect.right - adj2 * 2).toFloat(), (rect.top + adj5 - adj3).toFloat())
        path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.top + adj5 - adj3).toFloat())


        if (adj4 >= adj1) {
            path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.top + adj4).toFloat())
            s_rect.set(
                (rect.right - adj2 - adj1 / 2 - 2 * (adj4 - adj1)).toFloat(),
                (rect.top + adj1).toFloat(),
                (rect.right - adj2 - adj1 / 2).toFloat(),
                (rect.top + adj1 + 2 * (adj4 - adj1)).toFloat()
            )
            path.arcTo(s_rect, 0f, -90f)
            path.lineTo(
                (rect.right - adj2 + adj1 / 2 - adj4).toFloat(),
                (rect.top + adj1).toFloat()
            )

            path.lineTo((rect.left + adj4).toFloat(), (rect.top + adj1).toFloat())

            s_rect.set(
                (rect.left + adj1).toFloat(),
                (rect.top + adj1).toFloat(),
                (rect.left + adj1 + 2 * (adj4 - adj1)).toFloat(),
                (rect.top + adj1 + 2 * (adj4 - adj1)).toFloat()
            )
            path.arcTo(s_rect, 270f, -90f)
            path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj4).toFloat())
        } else {
            path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.top + adj1).toFloat())
            path.lineTo((rect.left + adj1).toFloat(), (rect.top + adj1).toFloat())
        }

        path.lineTo((rect.left + adj1).toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getLeftUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj2 * 2 + adj3 > len) {
                adj3 = len - adj2 * 2
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
        }


        //right arrow
        path.moveTo((rect.left + adj3).toFloat(), (rect.bottom - adj2 + adj1 / 2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - 2 * adj2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.bottom - adj2 - adj1 / 2).toFloat())
        path.lineTo(
            (rect.right - adj2 - adj1 / 2).toFloat(),
            (rect.bottom - adj2 - adj1 / 2).toFloat()
        )


        //up arrow
        path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2 * 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2 + adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(
            (rect.right - adj2 + adj1 / 2).toFloat(),
            (rect.bottom - adj2 + adj1 / 2).toFloat()
        )
        path.close()

        return path
    }

    private fun getBentUpArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
        }

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj1).toFloat())
        path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.bottom - adj1).toFloat())
        path.lineTo((rect.right - adj2 - adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2 * 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2 + adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.right - adj2 + adj1 / 2).toFloat(), rect.bottom.toFloat())

        path.close()


        return path
    }

    private fun getCurvedRightArrowPath(shape: AutoShape, rect: Rect): MutableList<Path?> {
        val pathList: MutableList<Path?> = ArrayList<Path?>(2)

        val values = shape.getAdjustData()
        var path = Path()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.5f)
            adj3 = Math.round(len * 0.25f)
        }

        val ovalWidth = 2 * rect.width()
        val ovalHeight = (rect.bottom - adj2 / 2 - adj1 / 2 - rect.top)

        path.moveTo(rect.right.toFloat(), rect.top.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalWidth).toFloat(),
            (rect.top + ovalHeight).toFloat()
        )
        path.arcTo(s_rect, 270f, -90f)
        path.lineTo(rect.left.toFloat(), (rect.top + ovalHeight / 2 + adj1).toFloat())
        s_rect.set(
            rect.left.toFloat(),
            (rect.top + adj1).toFloat(),
            (rect.left + ovalWidth).toFloat(),
            (rect.top + ovalHeight + adj1).toFloat()
        )
        path.arcTo(s_rect, 180f, 90f)
        path.close()

        pathList.add(path)

        path = Path()
        path.moveTo(rect.left.toFloat(), (rect.top + ovalHeight / 2).toFloat())

        val y = sqrt(
            (ovalHeight / 2).toDouble().pow(2.0) * ((ovalWidth / 2).toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / (ovalWidth / 2).toDouble().pow(2.0)
        )

        val angle = (atan(y / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalWidth).toFloat(),
            (rect.top + ovalHeight).toFloat()
        )
        path.arcTo(s_rect, 180f, -angle.toFloat())
        path.setLastPoint(
            (rect.right - adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt()).toFloat()
        )

        path.lineTo(
            (rect.right - adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 - adj2 / 2).toFloat()
        )
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj2 / 2).toFloat())
        path.lineTo(
            (rect.right - adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 + adj2 / 2).toFloat()
        )
        path.lineTo(
            (rect.right - adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1).toFloat()
        )

        s_rect.set(
            rect.left.toFloat(),
            (rect.top + adj1).toFloat(),
            (rect.left + ovalWidth).toFloat(),
            (rect.top + ovalHeight + adj1).toFloat()
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

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.5f)
            adj3 = Math.round(len * 0.25f)
        }

        val ovalWidth = 2 * rect.width()
        val ovalHeight = (rect.bottom - adj2 / 2 - adj1 / 2 - rect.top)

        path.moveTo(rect.right.toFloat(), (rect.top + ovalHeight / 2).toFloat())

        val y = sqrt(
            (ovalHeight / 2).toDouble().pow(2.0) * ((ovalWidth / 2).toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / (ovalWidth / 2).toDouble().pow(2.0)
        )

        val angle = (atan(y / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            (rect.right - ovalWidth).toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            (rect.top + ovalHeight).toFloat()
        )
        path.arcTo(s_rect, 0f, angle.toFloat())
        path.setLastPoint(
            (rect.left + adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt()).toFloat()
        )
        path.lineTo(
            (rect.left + adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 - adj2 / 2).toFloat()
        )
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj2 / 2).toFloat())
        path.lineTo(
            (rect.left + adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1 / 2 + adj2 / 2).toFloat()
        )
        path.lineTo(
            (rect.left + adj3).toFloat(),
            (rect.top + ovalHeight / 2 + y.toInt() + adj1).toFloat()
        )
        s_rect.set(
            (rect.right - ovalWidth).toFloat(),
            (rect.top + adj1).toFloat(),
            rect.right.toFloat(),
            (rect.top + ovalHeight + adj1).toFloat()
        )
        path.arcTo(s_rect, angle.toFloat(), -angle.toFloat())
        path.close()

        pathList.add(path)


        //        
        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        s_rect.set(
            (rect.right - ovalWidth).toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            (rect.top + ovalHeight).toFloat()
        )
        path.arcTo(s_rect, 270f, 90f)
        path.lineTo(rect.right.toFloat(), (rect.top + ovalHeight / 2 + adj1).toFloat())
        s_rect.set(
            (rect.right - ovalWidth).toFloat(),
            (rect.top + adj1).toFloat(),
            rect.right.toFloat(),
            (rect.top + ovalHeight + adj1).toFloat()
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

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.5f)
            adj3 = Math.round(len * 0.25f)
        }

        val ovalHalfWidth = (rect.width() - adj2 / 2 - adj1 / 2) / 2
        val ovalHalfHeight = rect.height()

        path.moveTo((rect.left + ovalHalfWidth).toFloat(), rect.bottom.toFloat())

        val x = sqrt(
            ovalHalfWidth.toDouble().pow(2.0) * (ovalHalfHeight.toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / ovalHalfHeight.toDouble().pow(2.0)
        )
        val angle = (atan(x / adj3) * 180 / Math.PI).toInt()
        s_rect.set(
            rect.left.toFloat(),
            (rect.top - ovalHalfHeight).toFloat(),
            (rect.left + ovalHalfWidth * 2).toFloat(),
            (rect.top + ovalHalfHeight).toFloat()
        )
        path.arcTo(s_rect, 90f, -angle.toFloat())
        path.setLastPoint(rect.left + ovalHalfWidth + x.toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 - adj2 / 2,
            (rect.top + adj3).toFloat()
        )
        path.lineTo((rect.right - adj2 / 2).toFloat(), rect.top.toFloat())
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 + adj2 / 2,
            (rect.top + adj3).toFloat()
        )
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1, (rect.top + adj3).toFloat())

        s_rect.set(
            (rect.left + adj1).toFloat(),
            (rect.top - ovalHalfHeight).toFloat(),
            (rect.left + ovalHalfWidth * 2 + adj1).toFloat(),
            (rect.top + ovalHalfHeight).toFloat()
        )
        path.arcTo(s_rect, (90 - angle).toFloat(), angle.toFloat())
        path.close()

        pathList.add(path)


        //        
        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            (rect.top - ovalHalfHeight).toFloat(),
            (rect.left + ovalHalfWidth * 2).toFloat(),
            (rect.top + ovalHalfHeight).toFloat()
        )
        path.arcTo(s_rect, 180f, -90f)
        path.lineTo((rect.left + ovalHalfWidth + adj1).toFloat(), rect.bottom.toFloat())
        s_rect.set(
            (rect.left + adj1).toFloat(),
            (rect.top - ovalHalfHeight).toFloat(),
            (rect.left + ovalHalfWidth * 2 + adj1).toFloat(),
            (rect.top + ovalHalfHeight).toFloat()
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

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 3) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.5f)
            adj3 = Math.round(len * 0.25f)
        }

        val ovalHalfWidth = (rect.width() - adj2 / 2 - adj1 / 2) / 2
        val ovalHalfHeight = rect.height()

        path.moveTo(rect.left.toFloat(), rect.bottom.toFloat())
        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalHalfWidth * 2).toFloat(),
            (rect.top + ovalHalfHeight * 2).toFloat()
        )
        path.arcTo(s_rect, 180f, 90f)
        path.lineTo((rect.left + ovalHalfWidth + adj1).toFloat(), rect.top.toFloat())
        s_rect.set(
            (rect.left + adj1).toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalHalfWidth * 2 + adj1).toFloat(),
            (rect.top + ovalHalfHeight * 2).toFloat()
        )
        path.arcTo(s_rect, 270f, -90f)
        path.close()
        pathList.add(path)

        path = Path()
        path.moveTo((rect.left + ovalHalfWidth).toFloat(), rect.top.toFloat())

        val x = sqrt(
            ovalHalfWidth.toDouble().pow(2.0) * (ovalHalfHeight.toDouble()
                .pow(2.0) - adj3.toDouble().pow(2.0)) / ovalHalfHeight.toDouble().pow(2.0)
        )
        val angle = (atan(x / adj3) * 180 / Math.PI).toInt()

        s_rect.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalHalfWidth * 2).toFloat(),
            (rect.top + ovalHalfHeight * 2).toFloat()
        )
        path.arcTo(s_rect, 270f, angle.toFloat())
        path.setLastPoint(rect.left + ovalHalfWidth + x.toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 - adj2 / 2,
            (rect.bottom - adj3).toFloat()
        )
        path.lineTo((rect.right - adj2 / 2).toFloat(), rect.bottom.toFloat())
        path.lineTo(
            rect.left + ovalHalfWidth + x.toFloat() + adj1 / 2 + adj2 / 2,
            (rect.bottom - adj3).toFloat()
        )
        path.lineTo(rect.left + ovalHalfWidth + x.toFloat() + adj1, (rect.bottom - adj3).toFloat())

        s_rect.set(
            (rect.left + adj1).toFloat(),
            rect.top.toFloat(),
            (rect.left + ovalHalfWidth * 2 + adj1).toFloat(),
            (rect.top + ovalHalfHeight * 2).toFloat()
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
        var len1 = rect.height() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }

        len1 = len2 / 32
        path.addRect(
            rect.left.toFloat(),
            (rect.centerY() - adj1).toFloat(),
            (rect.left + len1).toFloat(),
            (rect.centerY() + adj1).toFloat(),
            Path.Direction.CW
        )

        path.addRect(
            (rect.left + len1 * 2).toFloat(),
            (rect.centerY() - adj1).toFloat(),
            (rect.left + len1 * 4).toFloat(),
            (rect.centerY() + adj1).toFloat(),
            Path.Direction.CW
        )

        path.moveTo((rect.left + len1 * 5).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() - adj1).toFloat())


        //right arrow
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())

        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + len1 * 5).toFloat(), (rect.centerY() + adj1).toFloat())
        path.close()

        return path
    }

    private fun getNotchedRightArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        val len1 = rect.height() / 2
        val len2 = min(rect.width(), rect.height())
        if (values != null && values.size == 2) {
            adj1 = Math.round(len1 * values[0]!!)
            adj2 = Math.round(len2 * values[1]!!)
        } else {
            adj1 = Math.round(len1 * 0.5f)
            adj2 = Math.round(len2 * 0.5f)
        }


        //notch is similar with right arrow
        val adj3 = 2 * adj1 * adj2 / rect.height()

        path.moveTo(rect.left.toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() - adj1).toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj2).toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.centerY() + adj1).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), rect.centerY().toFloat())
        path.close()


        return path
    }

    private fun getHomePlatePath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 1) {
            adj1 = Math.round(len * values[0]!!)
        } else {
            adj1 = Math.round(len * 0.5f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getChevronPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 1) {
            adj1 = Math.round(len * values[0]!!)
        } else {
            adj1 = Math.round(len * 0.5f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.left + adj1).toFloat(), rect.centerY().toFloat())
        path.close()

        return path
    }

    private fun getRightArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(rect.width() * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj3 > rect.width()) {
                adj3 = rect.width()
            }

            if (adj4 + adj3 > rect.width()) {
                adj4 = rect.width() - adj3
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(rect.width() * 0.65f)
        }

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + adj4).toFloat(), rect.top.toFloat())

        path.lineTo((rect.left + adj4).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())

        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj2).toFloat())

        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.left + adj4).toFloat(), (rect.centerY() + adj1 / 2).toFloat())

        path.lineTo((rect.left + adj4).toFloat(), rect.bottom.toFloat())
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
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(rect.width() * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj3 > rect.width()) {
                adj3 = rect.width()
            }

            if (adj4 + adj3 > rect.width()) {
                adj4 = rect.width() - adj3
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(rect.width() * 0.65f)
        }

        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj2).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.right - adj4).toFloat(), (rect.centerY() - adj1 / 2).toFloat())

        path.lineTo((rect.right - adj4).toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.right - adj4).toFloat(), rect.bottom.toFloat())

        path.lineTo((rect.right - adj4).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj2).toFloat())

        path.close()

        return path
    }

    private fun getUpArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(rect.height() * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj3 > rect.height()) {
                adj3 = rect.width()
            }

            if (adj4 + adj3 > rect.height()) {
                adj4 = rect.height() - adj3
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(rect.height() * 0.65f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo(rect.right.toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())

        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.bottom - adj4).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.top + adj3).toFloat())

        path.close()

        return path
    }

    private fun getDownArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(rect.height() * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj3 > rect.height()) {
                adj3 = rect.width()
            }

            if (adj4 + adj3 > rect.height()) {
                adj4 = rect.height() - adj3
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(rect.height() * 0.65f)
        }

        path.moveTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo(rect.left.toFloat(), (rect.top + adj4).toFloat())
        path.lineTo(rect.left.toFloat(), rect.top.toFloat())

        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.top + adj4).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.bottom - adj3).toFloat())

        path.close()
        return path
    }

    private fun getLeftRightArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(rect.width() * values[3]!!)

            if (2 * adj3 >= rect.width()) {
                adj3 = rect.width() / 2
            }

            if (2 * adj3 + adj4 >= rect.width()) {
                adj4 = rect.width() - 2 * adj3
            }
        } else {
            adj1 = Math.round(len * 0.25f)
            adj2 = Math.round(len * 0.25f)
            adj3 = Math.round(len * 0.25f)
            adj4 = Math.round(rect.width() * 0.5f)
        }


        //left arrow
        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj2).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), rect.top.toFloat())

        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())


        //right arrow
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj2).toFloat())

        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() + adj1 / 2).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj2).toFloat())
        path.close()

        return path
    }

    private fun getQuadArrowCalloutPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        val len = min(rect.width(), rect.height())
        if (values != null && values.size == 4) {
            for (i in 0..3) {
                if (values[i]!! > 1 && i != 2) {
                    values[i] = 1f
                }
            }
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(len * values[1]!!)
            adj3 = Math.round(len * values[2]!!)
            adj4 = Math.round(len * values[3]!!)

            if (adj1 > adj2 * 2) {
                adj1 = adj2 * 2
            }

            if (adj4 > adj2 * 2) {
                adj4 = adj2 * 2
            }

            if (adj2 * 2 >= len) {
                adj2 = len / 2
                adj3 = 0
            }

            if (adj3 * 2 >= len) {
                adj3 = len / 2
            }

            if (adj2 + adj3 > len / 2) {
                adj3 = len / 2 - adj2
            }
        } else {
            adj1 = Math.round(len * 0.18515f)
            adj2 = Math.round(len * 0.18515f)
            adj3 = Math.round(len * 0.18515f)
            adj4 = Math.round(len * 0.48f)
        }


        //left arrow
        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj2).toFloat())

        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() - adj4 / 2).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.centerY() - adj4 / 2).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.top + adj3).toFloat())


        //up arrow
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.top + adj3).toFloat())

        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.top + adj3).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.centerY() - adj4 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() - adj4 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() - adj1 / 2).toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj1 / 2).toFloat())


        //right arrow
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() - adj2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj2).toFloat())

        path.lineTo((rect.right - adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.centerX() + adj4 / 2).toFloat(), (rect.centerY() + adj4 / 2).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.centerY() + adj4 / 2).toFloat())
        path.lineTo((rect.centerX() + adj1 / 2).toFloat(), (rect.bottom - adj3).toFloat())


        //down arrow
        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.bottom - adj3).toFloat())

        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.bottom - adj3).toFloat())
        path.lineTo((rect.centerX() - adj1 / 2).toFloat(), (rect.centerY() + adj4 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() + adj4 / 2).toFloat())
        path.lineTo((rect.centerX() - adj4 / 2).toFloat(), (rect.centerY() + adj1 / 2).toFloat())
        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj1 / 2).toFloat())


        path.lineTo((rect.left + adj3).toFloat(), (rect.centerY() + adj2).toFloat())
        path.close()
        return path
    }

    private fun getCircularArrowPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        var adj1 = 0
        var adj2 = 0
        var adj3 = 0
        var adj4 = 0
        var adj5 = 0
        val len = 100
        if (values != null && values.size == 5) {
            adj1 = Math.round(len * values[0]!!)
            adj2 = Math.round(values[1]!! * TODEGREE)
            adj3 = Math.round(values[2]!! * TODEGREE)
            adj4 = Math.round(values[3]!! * TODEGREE)
            adj5 = Math.round(len * values[4]!!)
        } else {
            adj1 = Math.round(len * 0.125f)
            adj2 = 20
            adj3 = 340
            adj4 = 180
            adj5 = Math.round(len * 0.125f)
        }


        //radius of circal between outer and inner
        val insideRadius = len / 2 - adj5


        //outer arc line
        //path.moveTo((insideRadius + adj1 / 2) * (float)Math.cos(adj4 *  Math.PI / 180f), (insideRadius + adj1 / 2) * (float)Math.sin(adj4 *  Math.PI / 180f));

        //point of the arrow tail line
        val y = insideRadius * sin(adj3 * Math.PI / 180f)
        val x = insideRadius * cos(adj3 * Math.PI / 180f)


        //arrow tail line  y = kx + b  
        val k = tan((adj3 + adj2) * Math.PI / 180f)
        val b = y - k * x


        //The distance between arrow tail center and tail endpoint
        var offX1 = sqrt(adj5.toDouble().pow(2.0) / (k.pow(2.0) + 1))
        //The distance between arrow tail center and intersetion of arrow tail and circle
        var offX2 = sqrt((adj1 / 2).toDouble().pow(2.0) / (k.pow(2.0) + 1))

        if (adj3 > 90 && adj3 < 270) {
            offX1 = -offX1
            offX2 = -offX2
        }

        val outerDegree = getAngle(x + offX2, k * (x + offX2) + b)
        val innerDegree = getAngle(x - offX2, k * (x - offX2) + b)


        s_rect.set(
            (adj5 - adj1 / 2 - len / 2).toFloat(),
            (adj5 - adj1 / 2 - len / 2).toFloat(),
            (len / 2 - adj5 + adj1 / 2).toFloat(),
            (len / 2 - adj5 + adj1 / 2).toFloat()
        )
        path.arcTo(s_rect, adj4.toFloat(), (outerDegree - adj4 + 360).toFloat() % 360)

        path.lineTo((x + offX1).toFloat(), (k * (x + offX1) + b).toFloat())
        path.lineTo(
            (insideRadius * cos((adj3 + adj2) * Math.PI / 180f)).toFloat(),
            (insideRadius * sin((adj3 + adj2) * Math.PI / 180f)).toFloat()
        )
        path.lineTo((x - offX1).toFloat(), (k * (x - offX1) + b).toFloat())

        s_rect.set(
            (adj5 + adj1 / 2 - len / 2).toFloat(),
            (adj5 + adj1 / 2 - len / 2).toFloat(),
            (len / 2 - adj5 - adj1 / 2).toFloat(),
            (len / 2 - adj5 - adj1 / 2).toFloat()
        )
        path.arcTo(s_rect, innerDegree.toFloat(), (adj4 - innerDegree - 360).toFloat() % 360)

        path.close()

        val m = Matrix()
        m.postScale(rect.width() / 100f, rect.height() / 100f)
        path.transform(m)

        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }

    private fun getAngle(x: Double, y: Double): Double {
        var angle = acos(x / sqrt(x * x + y * y)) * 180 / Math.PI

        if (y < 0) {
            angle = 360 - angle
        }

        return angle
    }
}
