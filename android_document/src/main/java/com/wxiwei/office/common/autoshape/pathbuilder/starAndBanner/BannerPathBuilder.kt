/*
 * 文件名称:          FlagPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午4:57:48
 */
package com.wxiwei.office.common.autoshape.pathbuilder.starAndBanner

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.ss.util.ColorUtil
import kotlin.math.abs
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
 * 日期:            2012-10-12
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
object BannerPathBuilder {
    private val sm = Matrix()

    private val tempRect = RectF()

    private val pathExList: MutableList<ExtendPath?> = ArrayList<ExtendPath?>(2)

    //picture fill color
    private const val PICTURECOLOR = -0x70aaaaab

    private val TINT = -0.3f

    /**
     * get banner path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getFlagExtendPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?>? {
        pathExList.clear()

        when (shape.getShapeType()) {
            ShapeTypes.Ribbon2 -> return getRibbon2Path(shape, rect)

            ShapeTypes.Ribbon -> return getRibbonPath(shape, rect)

            ShapeTypes.EllipseRibbon2 -> return getEllipseRibbon2Path(shape, rect)

            ShapeTypes.EllipseRibbon -> return getEllipseRibbonPath(shape, rect)

            ShapeTypes.VerticalScroll -> return getVerticalScrollPath(shape, rect)

            ShapeTypes.HorizontalScroll -> return getHorizontalScrollPath(shape, rect)

            ShapeTypes.Wave -> return getWavePath(shape, rect)

            ShapeTypes.DoubleWave -> return getDoubleWavePath(shape, rect)

            ShapeTypes.LeftRightRibbon -> return getLeftRightRibbon(shape, rect)
        }

        return null
    }

    private fun getRibbon2Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        val curleS = width / 8

        var adj1 = 0
        var adj2 = 0
        if (shape.isAutoShape07()) {
            if (values != null && values.size == 2) {
                //values[0]:[0, 1/3], values[1]:[0.25, 0.75]
                adj1 = Math.round(height * values[0]!!)
                adj2 = Math.round(width / 2 * values[1]!!)
            } else {
                adj1 = Math.round(height * 0.16667f)
                adj2 = Math.round(width / 2 * 0.5f)
            }
        } else {
            if (values != null && values.size >= 1) {
                //values[1]:[0, 1/3], values[0]:[0.25, 0.75]
                if (values[0] != null) {
                    adj2 = Math.round(width * (0.5f - values[0]!!))
                } else {
                    adj2 = Math.round(width * 0.25f)
                }

                if (values.size >= 2 && values[1] != null) {
                    adj1 = Math.round(height * (1 - values[1]!!))
                } else {
                    adj1 = Math.round(height * 0.125f)
                }
            } else {
                adj1 = Math.round(height * 0.125f)
                adj2 = Math.round(width * 0.25f)
            }
        }


        //small oval
        val a = (curleS / 4).toFloat()
        val b = (adj1 / 4).toFloat()


        //left notched shape
        var pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }

        var path = Path()
        path.moveTo(rect.left.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.left + curleS).toFloat(), (rect.bottom - (height - adj1) / 2).toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())

        path.lineTo(rect.centerX() - adj2 + a * 3, rect.bottom.toFloat())

        tempRect.set(
            rect.centerX() - adj2 + a * 2,
            rect.bottom - b * 2,
            (rect.centerX() - adj2 + curleS).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 90f, -180f)

        path.lineTo(rect.centerX() - adj2 + a, rect.bottom - b * 2)

        tempRect.set(
            (rect.centerX() - adj2).toFloat(),
            rect.bottom - b * 4,
            rect.centerX() - adj2 + a * 2,
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 90f, 90f)

        path.lineTo((rect.centerX() - adj2).toFloat(), (rect.top + adj1).toFloat())
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //right notched shape
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.right.toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - curleS).toFloat(), (rect.bottom - (height - adj1) / 2).toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())

        path.lineTo(rect.centerX() + adj2 - a * 3, rect.bottom.toFloat())

        tempRect.set(
            rect.centerX() + adj2 - a * 4,
            rect.bottom - b * 2,
            rect.centerX() + adj2 - 2 * a,
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 90f, 180f)

        path.lineTo(rect.centerX() + adj2 - a, rect.bottom - b * 2)

        tempRect.set(
            rect.centerX() + adj2 - 2 * a,
            rect.bottom - b * 4,
            (rect.centerX() + adj2).toFloat(),
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 90f, -90f)

        path.lineTo((rect.centerX() + adj2).toFloat(), (rect.top + adj1).toFloat())
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //middle shape        
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.centerX() - adj2).toFloat(), rect.top + b)

        tempRect.set(
            (rect.centerX() - adj2).toFloat(),
            rect.top.toFloat(),
            rect.centerX() - adj2 + 2 * a,
            rect.top + b * 2
        )
        path.arcTo(tempRect, 180f, 90f)

        path.lineTo(rect.centerX() + adj2 - a, rect.top.toFloat())

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.top.toFloat(),
            (rect.centerX() + adj2).toFloat(),
            rect.top + b * 2
        )
        path.arcTo(tempRect, 270f, 90f)

        path.lineTo((rect.centerX() + adj2).toFloat(), rect.bottom - b * 3)

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.bottom - b * 4,
            (rect.centerX() + adj2).toFloat(),
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 0f, -90f)

        path.lineTo(rect.centerX() - adj2 + a, rect.bottom - b * 4)

        tempRect.set(
            (rect.centerX() - adj2).toFloat(),
            rect.bottom - b * 4,
            rect.centerX() - adj2 + 2 * a,
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 270f, -90f)

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //left dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.centerX() - adj2 + curleS).toFloat(), rect.bottom - b * 4)
        path.lineTo(rect.centerX() - adj2 + a, rect.bottom - b * 4)

        tempRect.set(
            (rect.centerX() - adj2).toFloat(),
            rect.bottom - b * 4,
            rect.centerX() - adj2 + a * 2,
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 270f, -180f)

        path.lineTo(rect.centerX() - adj2 + a * 3, rect.bottom - b * 2)

        tempRect.set(
            rect.centerX() - adj2 + a * 2,
            rect.bottom - b * 2,
            rect.centerX() - adj2 + a * 4,
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 270f, 90f)

        path.close()

        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill

        pathExtend.path = path
        pathExList.add(pathExtend)


        //right dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.centerX() + adj2 - curleS).toFloat(), rect.bottom - b * 4)
        path.lineTo(rect.centerX() + adj2 - a, rect.bottom - b * 4)

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.bottom - b * 4,
            (rect.centerX() + adj2).toFloat(),
            rect.bottom - b * 2
        )
        path.arcTo(tempRect, 270f, 180f)

        path.lineTo(rect.centerX() + adj2 - a * 3, rect.bottom - b * 2)

        tempRect.set(
            rect.centerX() + adj2 - a * 4,
            rect.bottom - b * 2,
            rect.centerX() + adj2 - a * 2,
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 270f, -90f)

        path.close()

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill

        pathExtend.path = path
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getRibbonPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        val curleS = width / 8

        var adj1 = 0f
        var adj2 = 0f
        if (shape.isAutoShape07()) {
            if (values != null && values.size == 2) {
                //values[0]:[0, 1/3], values[1]:[0.25, 0.75]
                adj1 = Math.round(height * values[0]!!).toFloat()
                adj2 = Math.round(width / 2 * values[1]!!).toFloat()
            } else {
                adj1 = Math.round(height * 0.16667f).toFloat()
                adj2 = Math.round(width / 2 * 0.5f).toFloat()
            }
        } else {
            if (values != null && values.size >= 1) {
                //values[1]:[0, 1/3], values[0]:[0.25, 0.75]
                if (values[0] != null) {
                    adj2 = Math.round(width * (0.5f - values[0]!!)).toFloat()
                } else {
                    adj2 = Math.round(width * 0.25f).toFloat()
                }

                if (values.size >= 2 && values[1] != null) {
                    adj1 = Math.round(height * values[1]!!).toFloat()
                } else {
                    adj1 = Math.round(height * 0.125f).toFloat()
                }
            } else {
                adj1 = Math.round(height * 0.125f).toFloat()
                adj2 = Math.round(width * 0.25f).toFloat()
            }
        }


        //small oval
        val a = (curleS / 4).toFloat()
        val b = adj1 / 4


        //left notched shape
        var pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }

        var path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo((rect.left + curleS).toFloat(), rect.top + (height - adj1) / 2)
        path.lineTo(rect.left.toFloat(), rect.top + (height - adj1))

        path.lineTo(rect.centerX() - adj2, rect.top + (height - adj1))

        tempRect.set(
            rect.centerX() - adj2,
            rect.top + b * 2,
            rect.centerX() - adj2 + a * 2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 180f, 90f)

        path.lineTo(rect.centerX() - adj2 + a * 3, rect.top + b * 2)

        tempRect.set(
            rect.centerX() - adj2 + a * 2,
            rect.top.toFloat(),
            rect.centerX() - adj2 + a * 4,
            rect.top + b * 2
        )
        path.arcTo(tempRect, 90f, -180f)

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //middle part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.centerX() - adj2 + a, rect.bottom.toFloat())

        tempRect.set(
            rect.centerX() - adj2,
            rect.bottom - b * 2,
            rect.centerX() - adj2 + a * 2,
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 90f, 90f)

        path.lineTo(rect.centerX() - adj2, rect.top + b * 3)

        tempRect.set(
            rect.centerX() - adj2,
            rect.top + b * 2,
            rect.centerX() - adj2 + a * 2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 180f, -90f)

        path.lineTo(rect.centerX() + adj2 - a, rect.top + b * 4)

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.top + b * 2,
            rect.centerX() + adj2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 90f, -90f)

        path.lineTo(rect.centerX() + adj2, rect.bottom - b)

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.bottom - b * 2,
            rect.centerX() + adj2,
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 0f, 90f)

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //right part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo((rect.right - curleS).toFloat(), rect.top + (height - adj1) / 2)
        path.lineTo(rect.right.toFloat(), rect.top + (height - adj1))

        path.lineTo(rect.centerX() + adj2, rect.top + (height - adj1))

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.top + b * 2,
            rect.centerX() + adj2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 0f, -90f)

        path.lineTo(rect.centerX() + adj2 - a * 3, rect.top + b * 2)

        tempRect.set(
            rect.centerX() + adj2 - a * 4,
            rect.top.toFloat(),
            rect.centerX() + adj2 - a * 2,
            rect.top + b * 2
        )
        path.arcTo(tempRect, 90f, 180f)

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //left dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.centerX() - adj2 + a, rect.top + b * 4)

        tempRect.set(
            rect.centerX() - adj2,
            rect.top + b * 2,
            rect.centerX() - adj2 + a * 2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 90f, 180f)

        path.lineTo(rect.centerX() - adj2 + a * 3, rect.top + b * 2)

        tempRect.set(
            rect.centerX() - adj2 + a * 2,
            rect.top.toFloat(),
            rect.centerX() - adj2 + a * 4,
            rect.top + b * 2
        )
        path.arcTo(tempRect, 90f, -90f)

        path.lineTo(rect.centerX() - adj2 + curleS, rect.top + b * 4)
        path.close()

        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill

        pathExtend.path = path
        pathExList.add(pathExtend)


        //right dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.centerX() + adj2 - a, rect.top + b * 4)

        tempRect.set(
            rect.centerX() + adj2 - a * 2,
            rect.top + b * 2,
            rect.centerX() + adj2,
            rect.top + b * 4
        )
        path.arcTo(tempRect, 90f, -180f)

        path.lineTo(rect.centerX() + adj2 - a * 3, rect.top + b * 2)

        tempRect.set(
            rect.centerX() + adj2 - a * 4,
            rect.top.toFloat(),
            rect.centerX() + adj2 - a * 2,
            rect.top + b * 2
        )
        path.arcTo(tempRect, 90f, 90f)

        path.lineTo(rect.centerX() + adj2 - curleS, rect.top + b * 4)
        path.close()

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill

        pathExtend.path = path
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getEllipseRibbon2Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        var adj1 = 0
        var adj2 = 0
        var adj3 = 0

        var fU = 0.5f

        if (shape.isAutoShape07()) {
            if (values != null && values.size == 3) {
                if (values[0]!! - values[2]!! > 0.2f) {
                    values[2] = values[0]!! - 0.2f
                }

                if (values[1]!! > 0.75f) {
                    values[1] = 0.75f
                }

                fU = 0.5f - values[1]!! / 2

                adj1 = Math.round(len * values[0]!!)
                adj2 = Math.round(len / 2 * values[1]!!)
                adj3 = Math.round(len * values[2]!!)
            } else {
                adj1 = Math.round(len * 0.25f)
                adj2 = Math.round(len / 2 * 0.5f)
                adj3 = Math.round(len * 0.125f)

                fU = 0.25f
            }
        } else {
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    fU = values[0]!!
                    adj2 = Math.round(len * (0.5f - values[0]!!))
                } else {
                    fU = 0.25f
                    adj2 = Math.round(len * 0.25f)
                }

                if (values.size >= 2 && values[1] != null) {
                    adj1 = Math.round(len * (1 - values[1]!!))
                } else {
                    adj1 = Math.round(len * 0.25f)
                }

                if (values.size >= 3 && values[2] != null) {
                    adj3 = Math.round(len * values[2]!!)
                } else {
                    adj3 = Math.round(len * 0.125f)
                }
            } else {
                fU = 0.25f

                adj1 = Math.round(len * 0.25f)
                adj2 = Math.round(len / 2 * 0.5f)
                adj3 = Math.round(len * 0.125f)
            }
        }

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)

        if (adj3 >= adj1) {
            val ctrlPoints = computeBezierCtrPoint(
                0f, adj1.toFloat(),
                len, adj1.toFloat(),
                len / 2, 0f, 0.5f
            )

            val pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            val path = Path()
            path.moveTo(0f, adj1.toFloat())

            path.cubicTo(
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, adj1.toFloat()
            )

            path.lineTo(len - len * 0.125f, len / 2)
            path.lineTo(len, len)

            path.cubicTo(
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y + len - adj1,
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y + len - adj1,
                0f, len
            )

            path.lineTo(len * 0.125f, len / 2)
            path.close()

            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)
        } else {
            //left
            var ctrlPoints = computeBezierCtrPoint(
                0f, adj1.toFloat(),
                len, adj1.toFloat(),
                len / 2, (adj1 - adj3).toFloat(), 0.5f
            )

            var p1 = BezierComputePoint(
                0f, adj1.toFloat(),
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, adj1.toFloat(), 0.125f
            )

            val p2 = BezierComputePoint(
                0f, adj1.toFloat(),
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, adj1.toFloat(), fU
            )

            val end = BezierComputePoint(
                0f, adj1.toFloat(),
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, adj1.toFloat(), fU + 0.125f
            )

            ctrlPoints = computeBezierCtrPoint(
                0f, adj1.toFloat(),
                end.x, end.y,
                p1.x, p1.y, 0.125f / (fU + 0.125f)
            )

            var pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            var path = Path()
            path.moveTo(0f, adj1.toFloat())

            path.cubicTo(
                ctrlPoints!!.get(0)!!.x, ctrlPoints.get(0)!!.y,
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y,
                end.x, end.y
            )

            path.lineTo(end.x, end.y + len - adj1)

            path.cubicTo(
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y + len - adj1,
                ctrlPoints.get(0)!!.x, ctrlPoints.get(0)!!.y + len - adj1,
                0f, len
            )

            ctrlPoints = computeBezierCtrPoint(
                0f, (len + adj1) / 2,
                len, (len + adj1) / 2,
                len / 2, (len + adj1) / 2 - adj3, 0.5f
            )

            val notched = BezierComputePoint(
                0f, (len + adj1) / 2,
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, (len + adj1) / 2, 0.125f
            )

            path.lineTo(notched.x, notched.y)
            path.close()


            //right
            ctrlPoints = computeBezierCtrPoint(
                len - end.x, end.y,
                len, adj1.toFloat(),
                len - p1.x, p1.y, 1 - 0.125f / (fU + 0.125f)
            )

            path.moveTo(len - end.x, end.y)
            path.cubicTo(
                ctrlPoints!!.get(0)!!.x, ctrlPoints.get(0)!!.y,
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y,
                len, adj1.toFloat()
            )

            path.lineTo(len - notched.x, notched.y)
            path.lineTo(len, len)

            path.cubicTo(
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y + len - adj1,
                ctrlPoints.get(0)!!.x, ctrlPoints.get(0)!!.y + len - adj1,
                len - end.x, end.y + len - adj1
            )

            path.close()

            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)


            //middle part
            ctrlPoints = computeBezierCtrPoint(
                0f, adj3.toFloat(),
                len, adj3.toFloat(),
                len / 2, 0f, 0.5f
            )


            p1 = BezierComputePoint(
                0f, adj3.toFloat(),
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, adj3.toFloat(), fU
            )

            ctrlPoints = computeBezierCtrPoint(
                p1.x, p1.y,
                len - p1.x, p1.y,
                len / 2, 0f, 0.5f
            )

            pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            path = Path()
            path.moveTo(p1.x, p1.y)
            path.cubicTo(
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len - p1.x, p1.y
            )

            path.lineTo(len - p1.x, p1.y + len - adj1)

            path.cubicTo(
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y + len - adj1,
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y + len - adj1,
                p1.x, p1.y + len - adj1
            )

            path.close()


            //left and right dark part       
            path.moveTo(p1.x, p1.y + len - adj1)
            path.lineTo(end.x, end.y + len - adj1)

            path.moveTo(len - p1.x, p1.y + len - adj1)
            path.lineTo(len - end.x, end.y + len - adj1)

            pathExtend.path = path
            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)
        }

        return pathExList
    }

    private fun getEllipseRibbonPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        var adj1 = 0
        var adj2 = 0
        var adj3 = 0

        var fU = 0.5f

        if (shape.isAutoShape07()) {
            if (values != null && values.size == 3) {
                if (values[0]!! - values[2]!! > 0.2f) {
                    values[2] = values[0]!! - 0.2f
                }

                if (values[1]!! > 0.75f) {
                    values[1] = 0.75f
                }

                fU = 0.5f - values[1]!! / 2

                adj1 = Math.round(len * values[0]!!)
                adj2 = Math.round(len / 2 * values[1]!!)
                adj3 = Math.round(len * values[2]!!)
            } else {
                adj1 = Math.round(len * 0.25f)
                adj2 = Math.round(len / 2 * 0.5f)
                adj3 = Math.round(len * 0.125f)

                fU = 0.25f
            }
        } else {
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    fU = values[0]!!
                    adj2 = Math.round(len * (0.5f - values[0]!!))
                } else {
                    fU = 0.25f
                    adj2 = Math.round(len * 0.25f)
                }

                if (values.size >= 2 && values[1] != null) {
                    adj1 = Math.round(len * values[1]!!)
                } else {
                    adj1 = Math.round(len * 0.25f)
                }

                if (values.size >= 3 && values[2] != null) {
                    adj3 = Math.round(len * (1 - values[2]!!))
                } else {
                    adj3 = Math.round(len * 0.125f)
                }
            } else {
                fU = 0.25f

                adj1 = Math.round(len * 0.25f)
                adj2 = Math.round(len / 2 * 0.5f)
                adj3 = Math.round(len * 0.125f)
            }
        }


        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)

        if (adj3 >= adj1) {
            val ctrlPoints = computeBezierCtrPoint(
                0f, 0f,
                len, 0f,
                len / 2, adj1.toFloat(), 0.5f
            )

            val pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }

            val path = Path()
            path.moveTo(0f, 0f)

            path.cubicTo(
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, 0f
            )

            path.lineTo(len - len * 0.125f, len / 2)
            path.lineTo(len, len - adj1)

            path.cubicTo(
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y + len - adj1,
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y + len - adj1,
                0f, len - adj1
            )

            path.lineTo(len * 0.125f, len / 2)
            path.close()

            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)
        } else {
            //left
            var ctrlPoints = computeBezierCtrPoint(
                0f, 0f,
                len, 0f,
                len / 2, adj3.toFloat(), 0.5f
            )

            var p1 = BezierComputePoint(
                0f, 0f,
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, 0f, 0.125f
            )

            val p2 = BezierComputePoint(
                0f, 0f,
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, 0f, fU
            )

            val end = BezierComputePoint(
                0f, 0f,
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, 0f, fU + 0.125f
            )

            ctrlPoints = computeBezierCtrPoint(
                0f, 0f,
                end.x, end.y,
                p1.x, p1.y, 0.125f / (fU + 0.125f)
            )

            var pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            var path = Path()
            path.moveTo(0f, 0f)

            path.cubicTo(
                ctrlPoints!!.get(0)!!.x, ctrlPoints.get(0)!!.y,
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y,
                end.x, end.y
            )

            path.lineTo(end.x, end.y + len - adj1)

            path.cubicTo(
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y + len - adj1,
                ctrlPoints.get(0)!!.x, ctrlPoints.get(0)!!.y + len - adj1,
                0f, len - adj1
            )

            ctrlPoints = computeBezierCtrPoint(
                0f, (len - adj1) / 2,
                len, (len - adj1) / 2,
                len / 2, (len - adj1) / 2 + adj3, 0.5f
            )

            val notched = BezierComputePoint(
                0f, (len - adj1) / 2,
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, (len - adj1) / 2, 0.125f
            )

            path.lineTo(notched.x, notched.y)
            path.close()


            //right
            ctrlPoints = computeBezierCtrPoint(
                len - end.x, end.y,
                len, 0f,
                len - p1.x, p1.y, 1 - 0.125f / (fU + 0.125f)
            )

            path.moveTo(len - end.x, end.y)
            path.cubicTo(
                ctrlPoints!!.get(0)!!.x, ctrlPoints.get(0)!!.y,
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y,
                len, 0f
            )

            path.lineTo(len - notched.x, notched.y)
            path.lineTo(len, len - adj1)

            path.cubicTo(
                ctrlPoints.get(1)!!.x, ctrlPoints.get(1)!!.y + len - adj1,
                ctrlPoints.get(0)!!.x, ctrlPoints.get(0)!!.y + len - adj1,
                len - end.x, end.y + len - adj1
            )

            path.close()

            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)


            //middle part
            ctrlPoints = computeBezierCtrPoint(
                0f, len - adj3,
                len, len - adj3,
                len / 2, len, 0.5f
            )


            p1 = BezierComputePoint(
                0f, len - adj3,
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len, len - adj3, fU
            )

            ctrlPoints = computeBezierCtrPoint(
                p1.x, p1.y,
                len - p1.x, p1.y,
                len / 2, len, 0.5f
            )

            pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            path = Path()
            path.moveTo(p1.x, p1.y)
            path.cubicTo(
                (ctrlPoints!!.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y,
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y,
                len - p1.x, p1.y
            )

            path.lineTo(len - p1.x, p1.y - (len - adj1))

            path.cubicTo(
                (ctrlPoints.get(1)!!.x + len / 2) / 2, ctrlPoints.get(1)!!.y - (len - adj1),
                (ctrlPoints.get(0)!!.x + len / 2) / 2, ctrlPoints.get(0)!!.y - (len - adj1),
                p1.x, p1.y - (len - adj1)
            )

            path.close()


            //left dark part       
            path.moveTo(p1.x, p1.y - (len - adj1))
            path.lineTo(end.x, end.y)

            path.moveTo(len - p1.x, p1.y - (len - adj1))
            path.lineTo(len - end.x, end.y)

            pathExtend.path = path
            path.transform(sm)
            path.offset(rect.left.toFloat(), rect.top.toFloat())

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)
        }

        return pathExList
    }

    /**
     * 
     * @param startX start point of bezier line
     * @param startY
     * @param p1X   p1, p2 are point in the bezier line(between start and end)
     * @param p1Y
     * @param p2X
     * @param p2Y
     * @param endX  end point of bezier line
     * @param endY
     * @return
     */
    private fun computeBezierCtrPoint(
        startX: Float, startY: Float,
        p1X: Float, p1Y: Float,
        p2X: Float, p2Y: Float,
        endX: Float, endY: Float
    ): MutableList<PointF?> {
        val ctrlPoints: MutableList<PointF?> = ArrayList<PointF?>(2)
        val ctrAdd1 = PointF()
        val ctrAdd2 = PointF()
        ctrlPoints.add(0, ctrAdd1)
        ctrlPoints.add(1, ctrAdd2)


        //P1=(-5*Q0+18*Q1-9*Q2+2*Q3)/6;
        //P2=(2*Q0-9*Q1+18*Q2-5*Q3)/6;
        ctrAdd1.x = (-5 * startX + 18 * p1X - 9 * p2X + 2 * endX) / 6
        ctrAdd1.y = (-5 * startY + 18 * p1Y - 9 * p2Y + 2 * endY) / 6
        ctrAdd2.x = (2 * startX - 9 * p1X + 18 * p2X - 5 * endX) / 6
        ctrAdd2.y = (2 * startY - 9 * p1Y + 18 * p2Y - 5 * endY) / 6

        return ctrlPoints
    }

    /**
     * 
     * @param startX start of the bezier
     * @param startY
     * @param endX  end of the bezier
     * @param endY
     * @param pointX point in the bezier line
     * @param pointY
     * @param fU
     * @return
     */
    private fun computeBezierCtrPoint(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        pointX: Float, pointY: Float, fU: Float
    ): MutableList<PointF?>? {
        val EPSINON = 0.00001f
        if (fU < EPSINON && fU - 1.0 > EPSINON) {
            return null
        }
        val a: Float
        val b: Float
        val c: Float
        val d: Float
        val s: Float
        val a1: Float
        var c1: Float

        val ctrlPoints: MutableList<PointF?> = ArrayList<PointF?>(2)
        val ctrAdd1 = PointF()
        val ctrAdd2 = PointF()
        ctrlPoints.add(0, ctrAdd1)
        ctrlPoints.add(1, ctrAdd2)

        val fBlend = fU
        val f1subu = 1.0f - fBlend
        a = 3 * fBlend * f1subu * f1subu
        b = 3 * fBlend * fBlend * f1subu
        c = f1subu * f1subu * f1subu
        d = fBlend * fBlend * fBlend

        s = fU / f1subu //note fu = 0 1
        a1 = a + 3 * d
        c1 = (pointX - c * startX - a * startX - b * endX - d * endX)
        if (a1 < EPSINON) return null
        ctrAdd1.x = c1 / a1 + startX
        ctrAdd2.x = s * c1 / a1 + endX

        c1 = (pointY - c * startY - a * startY - b * endY - d * endY)
        if (a1 < EPSINON) return null
        ctrAdd1.y = c1 / a1 + startY
        ctrAdd2.y = s * c1 / a1 + endY

        return ctrlPoints
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
    private fun BezierComputePoint(
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

    private fun getVerticalScrollPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        val len = min(width, height)
        var adj1 = 0
        if (values != null && values.size == 1) {
            //values[0]:[0, 0.25]
            adj1 = Math.round(len * values[0]!!)
        } else {
            adj1 = Math.round(len * 0.125f)
        }

        val radius = adj1 / 2f


        //
        var pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }

        var path = Path()
        path.moveTo(rect.left + radius, rect.bottom.toFloat())

        tempRect.set(
            rect.left.toFloat(),
            (rect.bottom - adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 90f, -90f)

        path.lineTo((rect.left + adj1).toFloat(), rect.top + radius)

        tempRect.set(
            (rect.left + adj1).toFloat(),
            rect.top.toFloat(),
            (rect.left + adj1 * 2).toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 180f, 270f)

        path.lineTo((rect.right - adj1).toFloat(), (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), rect.bottom - radius)

        tempRect.set(
            (rect.right - adj1 * 2).toFloat(),
            (rect.bottom - adj1).toFloat(),
            (rect.right - adj1).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 0f, 90f)
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.left + radius * 3, rect.top.toFloat())

        tempRect.set(
            (rect.left + adj1).toFloat(),
            rect.top.toFloat(),
            (rect.left + adj1 * 2).toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 270f, 180f)

        path.lineTo(rect.right - radius, (rect.top + adj1).toFloat())

        tempRect.set(
            (rect.right - adj1).toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 90f, -180f)
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.left + adj1).toFloat(), (rect.bottom - adj1).toFloat())

        path.lineTo((rect.left + adj1).toFloat(), rect.bottom - radius)
        path.lineTo(rect.left + radius, rect.bottom - radius)

        tempRect.set(
            rect.left + radius * 0.5f,
            (rect.bottom - adj1).toFloat(),
            rect.left + radius * 1.5f,
            rect.bottom - radius
        )
        path.arcTo(tempRect, 90f, -180f)

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.left + adj1).toFloat(), rect.bottom - radius)

        tempRect.set(
            rect.left.toFloat(),
            (rect.bottom - adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 0f, 270f)

        tempRect.set(
            rect.left + radius * 0.5f,
            (rect.bottom - adj1).toFloat(),
            rect.left + radius * 1.5f,
            rect.bottom - radius
        )
        path.arcTo(tempRect, 270f, 180f)

        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.left + adj1 * 2).toFloat(), rect.top + radius)

        tempRect.set(
            (rect.left + adj1).toFloat(),
            rect.top.toFloat(),
            (rect.left + adj1 * 2).toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 0f, 90f)

        tempRect.set(
            rect.left + adj1 + radius * 0.5f,
            rect.top + radius,
            rect.left + adj1 + radius * 1.5f,
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 90f, 180f)

        path.close()

        pathExtend.path = path

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getHorizontalScrollPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        val len = min(width, height)
        var adj1 = 0
        if (values != null && values.size == 1) {
            //values[0]:[0, 0.25]
            adj1 = Math.round(len * values[0]!!)
        } else {
            adj1 = Math.round(len * 0.125f)
        }

        val radius = adj1 / 2f


        //
        var pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }

        var path = Path()
        path.moveTo(rect.left.toFloat(), rect.top + radius * 3)

        tempRect.set(
            rect.left.toFloat(),
            (rect.top + adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            (rect.top + adj1 * 2).toFloat()
        )
        path.arcTo(tempRect, 180f, -180f)

        path.lineTo((rect.left + adj1).toFloat(), rect.bottom - radius)

        tempRect.set(
            rect.left.toFloat(),
            (rect.bottom - adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(tempRect, 0f, 180f)
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.left + adj1).toFloat(), rect.top + radius * 3)

        tempRect.set(
            rect.left.toFloat(),
            (rect.top + adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            (rect.top + adj1 * 2).toFloat()
        )
        path.arcTo(tempRect, 0f, 270f)

        path.lineTo(rect.right - radius, (rect.top + adj1).toFloat())

        tempRect.set(
            (rect.right - adj1).toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 90f, -90f)

        path.lineTo(rect.right.toFloat(), rect.bottom - adj1 - radius)

        tempRect.set(
            (rect.right - adj1).toFloat(),
            (rect.bottom - adj1 * 2).toFloat(),
            rect.right.toFloat(),
            (rect.bottom - adj1).toFloat()
        )
        path.arcTo(tempRect, 0f, 90f)

        path.lineTo((rect.left + adj1).toFloat(), (rect.bottom - adj1).toFloat())

        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo((rect.right - adj1).toFloat(), rect.top + radius)

        tempRect.set(
            (rect.right - adj1).toFloat(),
            rect.top + radius * 0.5f,
            rect.right - radius,
            rect.top + radius * 1.5f
        )
        path.arcTo(tempRect, 180f, -180f)

        path.lineTo(rect.right - radius, (rect.top + adj1).toFloat())
        path.lineTo((rect.right - adj1).toFloat(), (rect.top + adj1).toFloat())
        path.close()

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        //dark part
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.left + radius, (rect.top + adj1 * 2).toFloat())

        tempRect.set(
            rect.left.toFloat(),
            (rect.top + adj1).toFloat(),
            (rect.left + adj1).toFloat(),
            (rect.top + adj1 * 2).toFloat()
        )
        path.arcTo(tempRect, 90f, -90f)

        tempRect.set(
            rect.left + radius,
            rect.top + adj1 + radius * 0.5f,
            (rect.left + adj1).toFloat(),
            rect.top + adj1 + radius * 1.5f
        )
        path.arcTo(tempRect, 0f, -180f)

        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)


        //
        pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        path = Path()
        path.moveTo(rect.right - radius, rect.top + radius)

        tempRect.set(
            (rect.right - adj1).toFloat(),
            rect.top + radius * 0.5f,
            rect.right - radius,
            rect.top + radius * 1.5f
        )
        path.arcTo(tempRect, 0f, 180f)

        tempRect.set(
            (rect.right - adj1).toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            (rect.top + adj1).toFloat()
        )
        path.arcTo(tempRect, 180f, 270f)

        path.close()

        pathExtend.path = path
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)
        return pathExList
    }

    private fun getWavePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        var adj1 = 0
        var adj2 = 0
        if (shape.isAutoShape07()) {
            if (values != null && values.size == 2) {
                //values[0]:[0, 0.25]   values[1];[-0.1,0.1]
                adj1 = Math.round(height * values[0]!!)
                adj2 = Math.round(width * values[1]!!)
            } else {
                adj1 = Math.round(height * 0.125f)
                adj2 = 0
            }
        } else {
            if (values != null && values.size >= 1) {
                //values[0]:[0, 0.25]   values[1];[-0.1,0.1]
                if (values[0] != null) {
                    adj1 = Math.round(height * values[0]!!)
                } else {
                    adj1 = Math.round(height * 0.125f)
                }

                if (values.size >= 2 && values[1] != null) {
                    adj2 = Math.round(width * (values[1]!! - 0.5f))
                } else {
                    adj2 = 0
                }
            } else {
                adj1 = Math.round(height * 0.125f)
                adj2 = 0
            }
        }

        val waveW = width - abs(adj2 * 2)
        val waveH = height - adj1

        val pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }
        val path = Path()
        if (adj2 > 0) {
            path.moveTo(rect.left.toFloat(), (rect.top + adj1).toFloat())

            path.cubicTo(
                rect.left + waveW * 0.3333f, rect.top + adj1 - adj1 * 3.3333f,
                rect.left + waveW * 0.6667f, rect.top + adj1 + adj1 * 3.3333f,
                (rect.left + waveW).toFloat(), (rect.top + adj1).toFloat()
            )

            path.lineTo(rect.right.toFloat(), (rect.bottom - adj1).toFloat())

            path.cubicTo(
                rect.right - waveW * 0.333f, rect.bottom - adj1 + adj1 * 3.3333f,
                rect.right - waveW * 0.6667f, rect.bottom - adj1 - adj1 * 3.333f,
                (rect.right - waveW).toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.close()
        } else {
            path.moveTo((rect.right - waveW).toFloat(), (rect.top + adj1).toFloat())

            path.cubicTo(
                rect.right - waveW * 0.6667f, rect.top + adj1 - adj1 * 3.333f,
                rect.right - waveW * 0.3333f, rect.top + adj1 + adj1 * 3.333f,
                rect.right.toFloat(), (rect.top + adj1).toFloat()
            )

            path.lineTo((rect.left + waveW).toFloat(), (rect.bottom - adj1).toFloat())

            path.cubicTo(
                rect.left + waveW * 0.6667f, rect.bottom - adj1 + adj1 * 3.333f,
                rect.left + waveW * 0.333f, rect.bottom - adj1 - adj1 * 3.333f,
                rect.left.toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.close()
        }


        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getDoubleWavePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val values = shape.getAdjustData()

        val width = rect.width()
        val height = rect.height()
        var adj1 = 0
        var adj2 = 0
        if (shape.isAutoShape07()) {
            if (values != null && values.size == 2) {
                //values[0]:[0, 0.25]   values[1];[-0.1,0.1]
                adj1 = Math.round(height * values[0]!!)
                adj2 = Math.round(width * values[1]!!)
            } else {
                adj1 = Math.round(height * 0.125f)
                adj2 = 0
            }
        } else {
            if (values != null && values.size >= 1) {
                //values[0]:[0, 0.25]   values[1];[-0.1,0.1]
                if (values[0] != null) {
                    adj1 = Math.round(height * values[0]!!)
                } else {
                    adj1 = Math.round(height * 0.125f)
                }

                if (values.size >= 2 && values[1] != null) {
                    adj2 = Math.round(width * (values[1]!! - 0.5f))
                } else {
                    adj2 = 0
                }
            } else {
                adj1 = Math.round(height * 0.125f)
                adj2 = 0
            }
        }

        val waveW = (width - abs(adj2 * 2)) / 2

        val pathExtend = ExtendPath()
        if (shape.hasLine()) {
            pathExtend.setLine(shape.getLine())
            pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
        }

        val path = Path()
        if (adj2 > 0) {
            path.moveTo(rect.left.toFloat(), (rect.top + adj1).toFloat())

            path.cubicTo(
                rect.left + waveW * 0.3333f, rect.top + adj1 - adj1 * 3.333f,
                rect.left + waveW * 0.6667f, rect.top + adj1 + adj1 * 3.333f,
                (rect.left + waveW).toFloat(), (rect.top + adj1).toFloat()
            )

            path.cubicTo(
                rect.left + waveW * 1.3333f, rect.top + adj1 - adj1 * 3.333f,
                rect.left + waveW * 1.6667f, rect.top + adj1 + adj1 * 3.333f,
                (rect.left + waveW * 2).toFloat(), (rect.top + adj1).toFloat()
            )

            path.lineTo(rect.right.toFloat(), (rect.bottom - adj1).toFloat())

            path.cubicTo(
                rect.right - waveW * 0.3333f, rect.bottom - adj1 + adj1 * 3.333f,
                rect.right - waveW * 0.6667f, rect.bottom - adj1 - adj1 * 3.333f,
                (rect.right - waveW).toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.cubicTo(
                rect.right - waveW * 1.3333f, rect.bottom - adj1 + adj1 * 3.333f,
                rect.right - waveW * 1.6667f, rect.bottom - adj1 - adj1 * 3.333f,
                (rect.right - waveW * 2).toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.close()
        } else {
            path.moveTo((rect.right - waveW * 2).toFloat(), (rect.top + adj1).toFloat())

            path.cubicTo(
                rect.right - waveW * 1.6667f, rect.top + adj1 - adj1 * 3.333f,
                rect.right - waveW * 1.3333f, rect.top + adj1 + adj1 * 3.333f,
                (rect.right - waveW).toFloat(), (rect.top + adj1).toFloat()
            )

            path.cubicTo(
                rect.right - waveW * 0.6667f, rect.top + adj1 - adj1 * 3.333f,
                rect.right - waveW * 0.3333f, rect.top + adj1 + adj1 * 3.333f,
                rect.right.toFloat(), (rect.top + adj1).toFloat()
            )

            path.lineTo((rect.left + waveW * 2).toFloat(), (rect.bottom - adj1).toFloat())

            path.cubicTo(
                rect.left + waveW * 1.6667f, rect.bottom - adj1 + adj1 * 3.333f,
                rect.left + waveW * 1.3333f, rect.bottom - adj1 - adj1 * 3.333f,
                (rect.left + waveW).toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.cubicTo(
                rect.left + waveW * 0.6667f, rect.bottom - adj1 + adj1 * 3.333f,
                rect.left + waveW * 0.3333f, rect.bottom - adj1 - adj1 * 3.333f,
                rect.left.toFloat(), (rect.bottom - adj1).toFloat()
            )

            path.close()
        }

        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getLeftRightRibbon(shape: AutoShape, rect: Rect): MutableList<ExtendPath?>? {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height())
        val height = rect.height()
        var adj1 = 0
        var adj2 = 0
        var adj3V = 0
        var adj3H = 0
        if (shape.isAutoShape07()) {
            if (values != null && values.size == 3) {
                //
                adj1 = Math.round(height * values[0]!!)
                adj2 = Math.round(len * values[1]!!)
                adj3H = Math.round(rect.width() * values[2]!!)
                adj3V = Math.round(height * values[2]!!)
            } else {
                adj1 = Math.round(height * 0.5f)
                adj2 = Math.round(len * 0.5f)
                adj3H = Math.round(rect.width() * 0.16667f)
                adj3V = Math.round(height * 0.16667f)
            }

            val arrowHeight = height - adj3V

            var pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }

            var path = Path()
            path.moveTo(rect.left.toFloat(), (rect.top + arrowHeight / 2).toFloat())
            path.lineTo((rect.left + adj2).toFloat(), rect.top.toFloat())
            path.lineTo(
                (rect.left + adj2).toFloat(),
                (rect.top + (arrowHeight - adj1) / 2).toFloat()
            )
            path.lineTo(rect.centerX().toFloat(), (rect.top + (arrowHeight - adj1) / 2).toFloat())

            path.arcTo(
                RectF(
                    (rect.centerX() - adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2).toFloat(),
                    (rect.centerX() + adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2 + adj3V / 2).toFloat()
                ),
                270f, 180f
            )

            path.arcTo(
                RectF(
                    (rect.centerX() - adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2 + adj3V / 2).toFloat(),
                    (rect.centerX() + adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2 + adj3V).toFloat()
                ),
                270f, -180f
            )

            path.lineTo(
                (rect.right - adj2).toFloat(),
                (rect.bottom - (arrowHeight - adj1) / 2 - adj1).toFloat()
            )
            path.lineTo((rect.right - adj2).toFloat(), (rect.bottom - arrowHeight).toFloat())
            path.lineTo(rect.right.toFloat(), (rect.bottom - arrowHeight / 2).toFloat())
            path.lineTo((rect.right - adj2).toFloat(), rect.bottom.toFloat())
            path.lineTo(
                (rect.right - adj2).toFloat(),
                (rect.bottom - (arrowHeight - adj1) / 2).toFloat()
            )

            path.arcTo(
                RectF(
                    (rect.centerX() - adj3H / 4).toFloat(),
                    (rect.bottom - (arrowHeight - adj1) / 2 - adj3V / 2).toFloat(),
                    (rect.centerX() + adj3H / 4).toFloat(),
                    (rect.bottom - (arrowHeight - adj1) / 2).toFloat()
                ),
                90f, 90f
            )

            path.lineTo(
                (rect.centerX() - adj3H / 4).toFloat(),
                (rect.top + (arrowHeight - adj1) / 2 + adj1).toFloat()
            )
            path.lineTo(
                (rect.left + adj2).toFloat(),
                (rect.top + (arrowHeight - adj1) / 2 + adj1).toFloat()
            )
            path.lineTo((rect.left + adj2).toFloat(), (rect.top + arrowHeight).toFloat())
            path.close()

            pathExtend.path = path
            pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
            pathExList.add(pathExtend)


            //dark part
            pathExtend = ExtendPath()
            if (shape.hasLine()) {
                pathExtend.setLine(shape.getLine())
                pathExtend.backgroundAndFill = shape.getLine().getBackgroundAndFill()
            }
            path = Path()
            path.arcTo(
                RectF(
                    (rect.centerX() - adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2 + adj3V / 2).toFloat(),
                    (rect.centerX() + adj3H / 4).toFloat(),
                    (rect.top + (arrowHeight - adj1) / 2 + adj3V).toFloat()
                ),
                270f, -180f
            )
            path.close()

            val fill = BackgroundAndFill()
            fill.setFillType(BackgroundAndFill.FILL_SOLID)

            val shapeFill = shape.getBackgroundAndFill()
            if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
                fill.setForegroundColor(
                    ColorUtil.instance()
                        .getColorWithTint(shapeFill.getForegroundColor(), TINT.toDouble())
                )
            } else {
                fill.setForegroundColor(PICTURECOLOR)
            }
            pathExtend.backgroundAndFill = fill

            pathExtend.path = path
            pathExList.add(pathExtend)

            return pathExList
        }

        return null
    }
}
