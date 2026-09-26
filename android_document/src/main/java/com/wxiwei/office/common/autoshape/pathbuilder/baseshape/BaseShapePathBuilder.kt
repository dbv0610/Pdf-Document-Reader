/*
 * 文件名称:          BaseShapePathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:01:08
 */
package com.wxiwei.office.common.autoshape.pathbuilder.baseshape

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.ss.util.ColorUtil
import kotlin.math.abs
import kotlin.math.acos
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
object BaseShapePathBuilder {
    private val TODEGREE_07 = 18000000f / 10800000f
    private val TODEGREE_03 = 18000000f / 54620000f

    private val rectF = RectF()

    private val path = Path()

    private val paths: MutableList<ExtendPath?> = ArrayList<ExtendPath?>()

    private val m = Matrix()

    /**
     * get base shape path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getBaseShapePath(shape: AutoShape, rect: Rect): Any? {
        path.reset()
        paths.clear()

        when (shape.getShapeType()) {
            ShapeTypes.Ellipse -> return getEllipsePath(shape, rect)

            ShapeTypes.Triangle -> return getTrianglePath(shape, rect)

            ShapeTypes.RtTriangle -> return getRtTrianglePath(shape, rect)

            ShapeTypes.Parallelogram -> return getParallelogramPath(shape, rect)

            ShapeTypes.Trapezoid -> return getTrapezoidPath(shape, rect)

            ShapeTypes.Diamond -> return getDiamondPath(shape, rect)

            ShapeTypes.Pentagon -> return getPentagonPath(shape, rect)

            ShapeTypes.Hexagon -> return getHexagonPath(shape, rect)

            ShapeTypes.Heptagon -> return getHeptagonPath(shape, rect)

            ShapeTypes.Octagon -> return getOctagonPath(shape, rect)

            ShapeTypes.Decagon -> return getDecagonPath(shape, rect)

            ShapeTypes.Dodecagon -> return getDodecagonPath(shape, rect)

            ShapeTypes.Pie -> return getPiePath(shape, rect)

            ShapeTypes.Chord -> return getChordPath(shape, rect)

            ShapeTypes.Teardrop -> return getTeardropPath(shape, rect)

            ShapeTypes.Frame -> return getFramePath(shape, rect)

            ShapeTypes.HalfFrame -> return getHalfFramePath(shape, rect)

            ShapeTypes.Corner -> return getCornerPath(shape, rect)

            ShapeTypes.DiagStripe -> return getDiagStripePath(shape, rect)

            ShapeTypes.Plus -> return getPlusPath(shape, rect)

            ShapeTypes.Plaque -> return getPlaquePath(shape, rect)

            ShapeTypes.Can -> return getCanPath(shape, rect)

            ShapeTypes.Cube -> return getCubePath(shape, rect)

            ShapeTypes.Bevel -> return getBevelPath(shape, rect)

            ShapeTypes.Donut -> return getDonutPath(shape, rect)

            ShapeTypes.NoSmoking -> return getNoSmokingPath(shape, rect)

            ShapeTypes.BlockArc -> return getBlockArcPath(shape, rect)

            ShapeTypes.FoldedCorner -> return getFoldedCornerPath(shape, rect)

            ShapeTypes.SmileyFace -> return getSmileyFacePath(shape, rect)

            ShapeTypes.Sun -> return getSunPath(shape, rect)

            ShapeTypes.Heart -> return getHeartPath(shape, rect)

            ShapeTypes.LightningBolt -> return getLightningBoltPath(shape, rect)

            ShapeTypes.Moon -> return getMoonPath(shape, rect)

            ShapeTypes.Cloud -> return getCloudPath(shape, rect)

            ShapeTypes.Arc -> return getArcPath(shape, rect)

            ShapeTypes.BracketPair -> return getBracketPairPath(shape, rect)

            ShapeTypes.BracePair -> return getBracePairPath(shape, rect)

            ShapeTypes.LeftBracket -> return getLeftBracketPath(shape, rect)

            ShapeTypes.RightBracket -> return getRightBracketPath(shape, rect)

            ShapeTypes.LeftBrace -> return getLeftBracePath(shape, rect)

            ShapeTypes.RightBrace -> return getRightBracePath(shape, rect)
        }

        return null
    }

    private fun getEllipsePath(shape: AutoShape?, rect: Rect): Path {
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)

        return path
    }

    private fun getTrianglePath(shape: AutoShape, rect: Rect): Path {
        var x = rect.width() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * values[0]!!
            }
        }

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getRtTrianglePath(shape: AutoShape?, rect: Rect): Path {
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getParallelogramPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.2f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
            }
        } else {
            x = rect.width() * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = rect.width() * values[0]!!
                }
            }
        }

        path.reset()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getTrapezoidPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.2f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
            }

            path.moveTo(rect.left + x, rect.top.toFloat())
            path.lineTo(rect.right - x, rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
            path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
            path.close()
        } else {
            x = rect.width() * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = rect.width() * values[0]!!
                }
            }

            path.moveTo(rect.left.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right - x, rect.bottom.toFloat())
            path.lineTo(rect.left + x, rect.bottom.toFloat())
            path.close()
        }

        return path
    }

    private fun getDiamondPath(shape: AutoShape?, rect: Rect): Path {
        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.exactCenterX(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.close()

        return path
    }

    private fun getPentagonPath(shape: AutoShape?, rect: Rect): Path {
        val x = rect.width().toFloat() / 2
        val y = x * tan(Math.toRadians(36.0)).toFloat()

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + y)
        path.lineTo(
            rect.right - (rect.height() - y) * tan(Math.toRadians(18.0)).toFloat(),
            rect.bottom.toFloat()
        )
        path.lineTo(
            rect.left + (rect.height() - y) * tan(Math.toRadians(18.0)).toFloat(),
            rect.bottom.toFloat()
        )
        path.lineTo(rect.left.toFloat(), rect.top + y)
        path.close()

        return path
    }

    private fun getHexagonPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
            }
        } else {
            x = rect.width() * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = rect.width() * values[0]!!
                }
            }
        }

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.close()

        return path
    }

    private fun getHeptagonPath(shape: AutoShape?, rect: Rect): Path {
        val x1 = rect.width() * 0.1f
        val x2 = rect.width() * 0.275f
        val y1 = rect.height() * 0.2f
        val y2 = rect.height() * 0.35f

        path.reset()
        path.moveTo(rect.exactCenterX(), rect.top.toFloat())
        path.lineTo(rect.right - x1, rect.top + y1)
        path.lineTo(rect.right.toFloat(), rect.bottom - y2)
        path.lineTo(rect.right - x2, rect.bottom.toFloat())
        path.lineTo(rect.left + x2, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - y2)
        path.lineTo(rect.left + x1, rect.top + y1)
        path.close()

        return path
    }

    private fun getOctagonPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.width(), rect.height()) * 0.25f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
        }

        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom - x)
        path.lineTo(rect.left.toFloat(), rect.top + x)
        path.close()

        return path
    }

    private fun getDecagonPath(shape: AutoShape?, rect: Rect): Path {
        val x1 = rect.width() * 0.1f
        val x2 = rect.width() * 0.35f
        val y = rect.height() * 0.2f

        path.moveTo(rect.left + x2, rect.top.toFloat())
        path.lineTo(rect.right - x2, rect.top.toFloat())
        path.lineTo(rect.right - x1, rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.exactCenterY())
        path.lineTo(rect.right - x1, rect.bottom - y)
        path.lineTo(rect.right - x2, rect.bottom.toFloat())
        path.lineTo(rect.left + x2, rect.bottom.toFloat())
        path.lineTo(rect.left + x1, rect.bottom - y)
        path.lineTo(rect.left.toFloat(), rect.exactCenterY())
        path.lineTo(rect.left + x1, rect.top + y)
        path.close()

        return path
    }

    private fun getDodecagonPath(shape: AutoShape?, rect: Rect): Path {
        val x1 = rect.width() * 0.133f
        val x2 = rect.width() * 0.35f
        val y1 = rect.height() * 0.133f
        val y2 = rect.height() * 0.35f

        path.moveTo(rect.left + x2, rect.top.toFloat())
        path.lineTo(rect.right - x2, rect.top.toFloat())
        path.lineTo(rect.right - x1, rect.top + y1)
        path.lineTo(rect.right.toFloat(), rect.top + y2)
        path.lineTo(rect.right.toFloat(), rect.bottom - y2)
        path.lineTo(rect.right - x1, rect.bottom - y1)
        path.lineTo(rect.right - x2, rect.bottom.toFloat())
        path.lineTo(rect.left + x2, rect.bottom.toFloat())
        path.lineTo(rect.left + x1, rect.bottom - y1)
        path.lineTo(rect.left.toFloat(), rect.bottom - y2)
        path.lineTo(rect.left.toFloat(), rect.top + y2)
        path.lineTo(rect.left + x1, rect.top + y1)
        path.close()

        return path
    }

    private fun getPiePath(shape: AutoShape, rect: Rect): Path {
        var start = 0f
        var end = 270f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                start = values[0]!! * TODEGREE_07
            }
            if (values[1] != null) {
                end = values[1]!! * TODEGREE_07
            }
        }

        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, start, (end - start + 360) % 360)
        path.close()

        return path
    }

    private fun getChordPath(shape: AutoShape, rect: Rect): Path {
        var start = 45f
        var end = 270f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                start = values[0]!! * 10 / 6
            }
            if (values[1] != null) {
                end = values[1]!! * 10 / 6
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, start, end - start)
        path.close()

        return path
    }

    private fun getTeardropPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        var y = 0f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1 && values[0] != null) {
            x = rect.width().toFloat() / 2 * values[0]!!
            y = rect.height().toFloat() / 2 * values[0]!!
        } else {
            x = rect.width().toFloat() / 2
            y = rect.height().toFloat() / 2
        }

        path.moveTo(rect.right.toFloat(), rect.centerY().toFloat())
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 0f, 270f)

        path.quadTo(
            rect.centerX() + x / 2,
            rect.top.toFloat(),
            rect.centerX() + x,
            rect.centerY() - y
        )
        path.quadTo(
            rect.right.toFloat(),
            rect.centerY() - y / 2,
            rect.right.toFloat(),
            rect.centerY().toFloat()
        )

        path.close()
        return path
    }

    private fun getFramePath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.height(), rect.width()) * 0.1f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.height(), rect.width()) * values[0]!!
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addRect(rectF, Path.Direction.CW)
        rectF.set(rect.left + x, rect.top + x, rect.right - x, rect.bottom - x)
        path.addRect(rectF, Path.Direction.CCW)

        return path
    }

    private fun getHalfFramePath(shape: AutoShape, rect: Rect): Path {
        var x1 = min(rect.height(), rect.width()) * 0.33333f
        var y1 = min(rect.height(), rect.width()) * 0.33333f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                y1 = min(rect.height(), rect.width()) * values[0]!!
            }
            if (values[1] != null) {
                x1 = min(rect.height(), rect.width()) * values[1]!!
            }
        }
        val x2 = y1 * rect.width() / rect.height()
        val y2 = x1 * rect.height() / rect.width()

        path.reset()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x2, rect.top + y1)
        path.lineTo(rect.left + x1, rect.top + y1)
        path.lineTo(rect.left + x1, rect.bottom - y2)
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getCornerPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.height(), rect.width()) * 0.5f
        var y = min(rect.height(), rect.width()) * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 2) {
            if (values[0] != null) {
                y = min(rect.height(), rect.width()) * values[0]!!
            }
            if (values[1] != null) {
                x = min(rect.height(), rect.width()) * values[1]!!
            }
        }
        y = rect.height() - y

        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.left + x, rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.top + y)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getDiagStripePath(shape: AutoShape, rect: Rect): Path {
        var y = rect.height() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                y = rect.height() * values[0]!!
            }
        }
        val x = y * rect.width() / rect.height()

        path.moveTo(rect.left.toFloat(), rect.top + y)
        path.lineTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        return path
    }

    private fun getPlusPath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.height(), rect.width()) * 0.25f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.height(), rect.width()) * values[0]!!
            }
        }

        path.moveTo(rect.left.toFloat(), rect.top + x)
        path.lineTo(rect.left + x, rect.top + x)
        path.lineTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom.toFloat())
        path.lineTo(rect.left + x, rect.bottom - x)
        path.lineTo(rect.left.toFloat(), rect.bottom - x)
        path.close()

        return path
    }

    private fun getPlaquePath(shape: AutoShape, rect: Rect): Path {
        var x = min(rect.height(), rect.width()) * 0.16f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.height(), rect.width()) * values[0]!!
            }
        }

        rectF.set(rect.right - x, rect.top - x, rect.right + x, rect.top + x)
        path.arcTo(rectF, 180f, -90f)
        rectF.set(rect.right - x, rect.bottom - x, rect.right + x, rect.bottom + x)
        path.arcTo(rectF, 270f, -90f)
        rectF.set(rect.left - x, rect.bottom - x, rect.left + x, rect.bottom + x)
        path.arcTo(rectF, 0f, -90f)
        rectF.set(rect.left - x, rect.top - x, rect.left + x, rect.top + x)
        path.arcTo(rectF, 90f, -90f)
        path.close()

        return path
    }

    private fun getCanPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.height(), rect.width()) * 0.175f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.height(), rect.width()) * values[0]!!
                }
            }
        } else {
            x = rect.height() * 0.25f
            if (values != null && values.size > 0) {
                if (values[0] != null) {
                    x = rect.height() * values[0]!!
                }
            }
        }

        val fill = shape.getBackgroundAndFill()
        var bgFill = fill
        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), 0.4)
            )
        }

        var extendPath = ExtendPath()
        var path = Path()
        rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.top + x)
        path.addOval(rectF, Path.Direction.CW)

        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        extendPath.setLine(shape.getLine())

        paths.add(extendPath)


        extendPath = ExtendPath()
        path = Path()
        path.arcTo(rectF, 180f, -180f)
        rectF.set(rect.left.toFloat(), rect.bottom - x, rect.right.toFloat(), rect.bottom.toFloat())
        path.arcTo(rectF, 0f, 180f)
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = fill
        extendPath.path = path
        paths.add(extendPath)

        return paths
    }

    private fun getCubePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = min(rect.height(), rect.width()) * 0.25f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.height(), rect.width()) * values[0]!!
            }
        }

        val fill = shape.getBackgroundAndFill()
        var extendPath = ExtendPath()
        var path = Path()
        path.addRect(
            rect.left.toFloat(),
            rect.top + x,
            rect.right - x,
            rect.bottom.toFloat(),
            Path.Direction.CW
        )
        extendPath.backgroundAndFill = fill
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        var bgFill = fill
        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), 0.2)
            )
        }

        extendPath = ExtendPath()
        path = Path()
        path.moveTo(rect.left + x, rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top + x)
        path.lineTo(rect.left.toFloat(), rect.top + x)
        path.close()

        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        paths.add(extendPath)

        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.2)
            )
        }

        extendPath = ExtendPath()
        path = Path()
        path.moveTo(rect.right - x, rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom.toFloat())
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        paths.add(extendPath)

        return paths
    }

    private fun getBevelPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = min(rect.height(), rect.width()) * 0.125f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.height(), rect.width()) * values[0]!!
            }
        }

        val fill = shape.getBackgroundAndFill()
        var bgFill = fill
        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), 0.2)
            )
        }

        var extendPath = ExtendPath()
        var path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right - x, rect.top + x)
        path.lineTo(rect.left + x, rect.top + x)
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        paths.add(extendPath)


        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.4)
            )
        }

        extendPath = ExtendPath()
        path = Path()
        path.moveTo(rect.right - x, rect.top + x)
        path.lineTo(rect.right.toFloat(), rect.top.toFloat())
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.right - x, rect.bottom - x)
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        paths.add(extendPath)

        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.2)
            )
        }

        extendPath = ExtendPath()
        path = Path()
        path.moveTo(rect.left + x, rect.bottom - x)
        path.lineTo(rect.right - x, rect.bottom - x)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        paths.add(extendPath)

        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), 0.4)
            )
        }

        extendPath = ExtendPath()
        path = Path()
        path.moveTo(rect.left.toFloat(), rect.top.toFloat())
        path.lineTo(rect.left + x, rect.top + x)
        path.lineTo(rect.left + x, rect.bottom - x)
        path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
        path.close()

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        extendPath.path = path
        paths.add(extendPath)

        extendPath = ExtendPath()
        path = Path()
        path.addRect(
            rect.left + x,
            rect.top + x,
            rect.right - x,
            rect.bottom - x,
            Path.Direction.CW
        )

        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = fill
        extendPath.path = path
        paths.add(extendPath)

        return paths
    }

    private fun getDonutPath(shape: AutoShape, rect: Rect): Path {
        var x = 0f
        var y = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.height(), rect.width()) * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.height(), rect.width()) * values[0]!!
                }
            }
            y = x
        } else {
            x = rect.width() * 0.25f
            y = rect.height() * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = rect.width() * values[0]!!
                    y = rect.height() * values[0]!!
                }
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)
        rectF.set(rect.left + x, rect.top + y, rect.right - x, rect.bottom - y)
        path.addOval(rectF, Path.Direction.CCW)

        return path
    }

    private fun getNoSmokingPath(shape: AutoShape, rect: Rect): Path {
        val len = min(rect.width(), rect.height()).toFloat()
        var rate = 0.2f
        var x = len * 0.2f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1 && values[0] != null) {
            rate = values[0]!!
        }

        x = len * rate

        rectF.set(0f, 0f, len, len)
        path.addOval(rectF, Path.Direction.CCW)

        var degree = 45.0
        if (rate <= 0.25f) {
            rate = rate * 0.5f / (0.5f - rate)
            degree = acos(rate.toDouble())
        } else {
            degree = Math.PI / 3 * (0.5f - rate) / 0.25f
        }

        rectF.set(x, x, len - x, len - x)

        path.arcTo(
            rectF, ((Math.PI * 1.75f - degree) / Math.PI * 180).toFloat(),
            (degree * 2 / Math.PI * 180).toFloat(), true
        )
        path.close()


        path.arcTo(
            rectF, ((Math.PI * 0.75f - degree) / Math.PI * 180).toFloat(),
            (degree * 2 / Math.PI * 180).toFloat(), true
        )
        path.close()

        m.reset()
        m.postScale(rect.width() / len, rect.height() / len)
        path.transform(m)
        path.offset(rect.left.toFloat(), rect.top.toFloat())


        return path
    }

    private fun getBlockArcPath(shape: AutoShape, rect: Rect): Path {
        var start = 180f
        var end = 0f
        var x = min(rect.width(), rect.height()) * 0.25f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            if (values != null && values.size >= 3) {
                if (values[0] != null) {
                    start = values[0]!! * 10 / 6
                }
                if (values[1] != null) {
                    end = values[1]!! * 10 / 6
                }
                if (values[2] != null) {
                    x = min(rect.width(), rect.height()) * values[2]!!
                }
            }
        } else {
            var angle = 90f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    angle = values[0]!! * TODEGREE_03
                } else {
                    angle = 0f
                }

                if (values.size >= 2 && values[1] != null) {
                    x = rect.width() * values[1]!!
                } else {
                    x = rect.width() * 0.25f
                }
            } else {
                angle = 180f
                x = rect.width() * 0.25f
            }

            if (angle >= 0) {
                start = angle
                end = 90 + (90 - angle)
            } else {
                start = angle + 360
                end = 360 - (start - 180)
            }
        }

        if (end >= start) {
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, start, end - start)
            rectF.set(rect.left + x, rect.top + x, rect.right - x, rect.bottom - x)
            path.arcTo(rectF, end, start - end)
        } else {
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, start, 360 + end - start)
            rectF.set(rect.left + x, rect.top + x, rect.right - x, rect.bottom - x)
            path.arcTo(rectF, end, start - end - 360)
        }
        path.close()

        return path
    }

    private fun getFoldedCornerPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.25f
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.moveTo(rect.left.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.bottom - x)
            path.lineTo(rect.right - x, rect.bottom.toFloat())
            path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
            path.close()

            extendPath.setLine(shape.getLine())
            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)


            var bgFill = fill
            if (fill != null) {
                bgFill = BackgroundAndFill()
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(
                    ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.2)
                )
            }

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(
                rect.right - x * sin(Math.toRadians(75.0)).toFloat() * sqrt(6.0).toFloat() / 3,
                rect.bottom - x * sin(Math.toRadians(75.0)).toFloat() * sqrt(6.0).toFloat() / 3
            )
            path.lineTo(rect.right.toFloat(), rect.bottom - x)
            path.lineTo(rect.right - x, rect.bottom.toFloat())
            path.close()

            extendPath.setLine(shape.getLine())
            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        } else {
            var adjust = 0f
            x = min(rect.width(), rect.height()) * 0.125f
            if (values != null && values.size >= 1) {
                x = min(rect.width(), rect.height()) * (1 - values[0]!!)
            }
            if (rect.height() > rect.width()) {
                x *= 1.4286.toFloat()
                adjust = 0.7f
            } else {
                adjust = 1.4286f
            }

            var extendPath = ExtendPath()
            var path = Path()
            path.moveTo(rect.left.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.bottom - x)
            path.lineTo(rect.right - x * adjust, rect.bottom.toFloat())
            path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
            path.close()
            val fill = shape.getBackgroundAndFill()

            extendPath.setLine(shape.getLine())
            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            var bgFill = fill
            if (fill != null) {
                bgFill = BackgroundAndFill()
                bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
                bgFill.setForegroundColor(
                    ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.2)
                )
            }

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(
                rect.right - x * adjust * sin(Math.toRadians(75.0)).toFloat() * sqrt(6.0).toFloat() / 3,
                rect.bottom - x * sin(Math.toRadians(75.0)).toFloat() * sqrt(6.0).toFloat() / 3
            )
            path.lineTo(rect.right.toFloat(), rect.bottom - x)
            path.lineTo(rect.right - x * adjust, rect.bottom.toFloat())
            path.close()

            extendPath.setLine(shape.getLine())
            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        return paths
    }

    private fun getSmileyFacePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = rect.height() * 0.04653f * 2
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (shape.isAutoShape07()) {
                if (values[0] != null) {
                    x = rect.height() * values[0]!! * 2
                }
            } else {
                if (values[0] != null) {
                    x = rect.height() * (values[0]!! - 0.77f) * 2
                }
            }
        }

        val fill = shape.getBackgroundAndFill()

        var extendPath = ExtendPath()
        var path = Path()
        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            rect.right.toFloat(),
            rect.bottom.toFloat()
        )
        path.addOval(rectF, Path.Direction.CW)
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = fill
        paths.add(extendPath)


        var left = rect.left + rect.width().toFloat() / 4
        var right = rect.right - rect.width().toFloat() / 4
        var top = rect.bottom - rect.height().toFloat() / 4 - abs(x)
        var bottom = rect.bottom - rect.height().toFloat() / 4 + abs(x)
        extendPath = ExtendPath()
        path = Path()
        rectF.set(left, top, right, bottom)
        if (x >= 0) {
            path.arcTo(rectF, 15f, 150f)
        } else {
            path.arcTo(rectF, 195f, 150f)
        }
        extendPath.path = path
        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = fill
        paths.add(extendPath)


        var bgFill = fill
        if (fill != null) {
            bgFill = BackgroundAndFill()
            bgFill.setFillType(BackgroundAndFill.FILL_SOLID)
            bgFill.setForegroundColor(
                ColorUtil.instance().getColorWithTint(fill.getForegroundColor(), -0.2)
            )
        }

        left = rect.exactCenterX() - rect.width().toFloat() / 5
        right = rect.exactCenterX() - rect.width().toFloat() / 10
        top = rect.exactCenterY() - rect.height().toFloat() / 5
        bottom = rect.exactCenterY() - rect.height().toFloat() / 10

        extendPath = ExtendPath()
        path = Path()
        rectF.set(left, top, right, bottom)
        path.addOval(rectF, Path.Direction.CW)

        extendPath.path = path
        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        paths.add(extendPath)

        left = rect.exactCenterX() + rect.width().toFloat() / 10
        right = rect.exactCenterX() + rect.width().toFloat() / 5

        extendPath = ExtendPath()
        path = Path()
        rectF.set(left, top, right, bottom)
        path.addOval(rectF, Path.Direction.CW)

        extendPath.path = path
        extendPath.setLine(shape.getLine())
        extendPath.backgroundAndFill = bgFill
        paths.add(extendPath)

        return paths
    }

    private fun getSunPath(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()
        var offX = 0f
        var offY = 0f
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                offX = rect.width() * values[0]!!
                offY = rect.height() * values[0]!!
            }
        } else {
            offX = rect.width() * 0.25f
            offY = rect.height() * 0.25f
        }


        //inner oval
        rectF.set(rect.left + offX, rect.top + offY, rect.right - offX, rect.bottom - offY)
        path.addOval(rectF, Path.Direction.CW)


        //up and down arrow
        path.moveTo(rect.centerX().toFloat(), rect.top.toFloat())
        path.lineTo((rect.centerX() + rect.width() / 14).toFloat(), rect.top + offY * 0.75f)
        path.lineTo((rect.centerX() - rect.width() / 14).toFloat(), rect.top + offY * 0.75f)
        path.close()

        path.moveTo(rect.centerX().toFloat(), rect.bottom.toFloat())
        path.lineTo((rect.centerX() - rect.width() / 14).toFloat(), rect.bottom - offY * 0.75f)
        path.lineTo((rect.centerX() + rect.width() / 14).toFloat(), rect.bottom - offY * 0.75f)
        path.close()


        //left and right arrow
        path.moveTo(rect.left.toFloat(), rect.centerY().toFloat())
        path.lineTo(rect.left + offX * 0.75f, (rect.centerY() - rect.height() / 14).toFloat())
        path.lineTo(rect.left + offX * 0.75f, (rect.centerY() + rect.height() / 14).toFloat())
        path.close()

        path.moveTo(rect.right.toFloat(), rect.centerY().toFloat())
        path.lineTo(rect.right - offX * 0.75f, (rect.centerY() + rect.height() / 14).toFloat())
        path.lineTo(rect.right - offX * 0.75f, (rect.centerY() - rect.height() / 14).toFloat())
        path.close()


        //right-bottom arrow
        //arrow header
        val hx = (sqrt(0.5) * rect.width()).toFloat() / 2
        val hy = (sqrt(0.5) * rect.height()).toFloat() / 2

        val mx_tail = (sqrt(0.5) * (rect.width() - offX * 0.75f * 2)).toFloat() / 2
        val my_tail = (sqrt(0.5) * (rect.height() - offY * 0.75f * 2)).toFloat() / 2

        val offLen = ((rect.width() + rect.height()) / 28).toFloat()
        //tangent line of oval: x0x/a^2 + y0y/ b^2 = 1    (x0, y0)---(a/2^0.5, b/2^0.5)        
        offX = (offLen * rect.width() / sqrt(
            rect.width().toDouble().pow(2.0) + rect.height().toDouble().pow(2.0)
        )).toFloat()
        offY = (offLen * rect.height() / sqrt(
            rect.width().toDouble().pow(2.0) + rect.height().toDouble().pow(2.0)
        )).toFloat()

        val tailX1 = mx_tail + offX
        val tailY1 = my_tail - offY

        val tailX2 = mx_tail - offX
        val tailY2 = my_tail + offY

        path.moveTo(rect.centerX() + hx, rect.centerY() + hy)
        path.lineTo(rect.centerX() + tailX1, rect.centerY() + tailY1)
        path.lineTo(rect.centerX() + tailX2, rect.centerY() + tailY2)
        path.close()


        //left-top arrow
        path.moveTo(rect.centerX() - hx, rect.centerY() - hy)
        path.lineTo(rect.centerX() - tailX1, rect.centerY() - tailY1)
        path.lineTo(rect.centerX() - tailX2, rect.centerY() - tailY2)
        path.close()


        // right-top
        path.moveTo(rect.centerX() + hx, rect.centerY() - hy)
        path.lineTo(rect.centerX() + tailX1, rect.centerY() - tailY1)
        path.lineTo(rect.centerX() + tailX2, rect.centerY() - tailY2)
        path.close()


        //left-bottom
        path.moveTo(rect.centerX() - hx, rect.centerY() + hy)
        path.lineTo(rect.centerX() - tailX1, rect.centerY() + tailY1)
        path.lineTo(rect.centerX() - tailX2, rect.centerY() + tailY2)
        path.close()

        return path
    }

    private fun getHeartPath(shape: AutoShape?, rect: Rect): Path {
        path.moveTo(0f, 30f)
        path.cubicTo(
            0f, -10f,
            40f, 0f,
            50f, 20f
        )

        path.cubicTo(
            60f, 0f,
            100f, -10f,
            100f, 30f
        )

        path.cubicTo(
            100f, 60f,
            60f, 100f,
            50f, 100f
        )

        path.cubicTo(
            40f, 100f,
            0f, 60f,
            0f, 30f
        )
        path.close()

        m.reset()
        m.postScale(rect.width() / 100f, rect.height() / 100f)
        path.transform(m)

        path.offset(rect.left.toFloat(), rect.top.toFloat())

        return path
    }

    private fun getLightningBoltPath(shape: AutoShape?, rect: Rect): Path {
        val w = rect.width().toFloat()
        val h = rect.height().toFloat()

        path.moveTo(rect.left + w * 0.4f, rect.top.toFloat())
        path.lineTo(rect.left + w * 0.6f, rect.top + h * 0.2857f)
        path.lineTo(rect.left + w * 0.5167f, rect.top + h * 0.3f)
        path.lineTo(rect.right - w * 0.23f, rect.bottom - h * 0.44f)
        path.lineTo(rect.right - w * 0.3448f, rect.bottom - h * 0.4f)
        path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
        path.lineTo(rect.left + w * 0.4615f, rect.bottom - h * 0.3167f)
        path.lineTo(rect.left + w * 0.5455f, rect.bottom - h * 0.35f)
        path.lineTo(rect.left + w * 0.25f, rect.top + h * 0.4545f)
        path.lineTo(rect.left + w * 0.35f, rect.top + h * 0.3921f)
        path.lineTo(rect.left.toFloat(), rect.top + h * 0.19f)
        path.close()

        return path
    }

    private fun getMoonPath(shape: AutoShape, rect: Rect): Path {
        var x = rect.width() * 0.5f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = rect.width() * (1 - values[0]!!)
            }
        }

        rectF.set(
            rect.left.toFloat(),
            rect.top.toFloat(),
            (rect.right * 2 - rect.left).toFloat(),
            rect.bottom.toFloat()
        )
        path.arcTo(rectF, 90f, 180f)
        rectF.set(rect.right - x, rect.top.toFloat(), rect.right + x, rect.bottom.toFloat())
        path.arcTo(rectF, 270f, -180f)

        return path
    }

    private fun getCloudPath(shape: AutoShape?, rect: Rect): Path {
        val len = 468f
        path.reset()

        rectF.set(0f, 160f, 90f, 285f)
        path.arcTo(rectF, 120f, 148f)

        rectF.set(41f, 44f, 188f, 250f)
        path.arcTo(rectF, 172.5f, 127.5f)

        rectF.set(140f, 14f, 264f, 220f)
        path.arcTo(rectF, 218f, 90f)

        rectF.set(230f, 0f, 340f, 210f)
        path.arcTo(rectF, 219f, 92f)

        rectF.set(296f, 0f, 428f, 246f)
        path.arcTo(rectF, 232f, 101f)


        rectF.set(342f, 60f, 454f, 214f)
        path.arcTo(rectF, 293f, 89f)

        rectF.set(324f, 130f, 468f, 327f)
        path.arcTo(rectF, 319f, 119f)


        rectF.set(280f, 240f, 405f, 412f)
        path.arcTo(rectF, 1f, 122f)

        rectF.set(168f, 274f, 312f, 468f)
        path.arcTo(rectF, 16f, 130f)

        rectF.set(57f, 249f, 213f, 441f)
        path.arcTo(rectF, 56f, 74f)

        rectF.set(11f, 259f, 99f, 386f)
        path.arcTo(rectF, 84f, 140f)

        path.close()

        m.reset()
        m.postScale(rect.width() / len, rect.height() / len)
        path.transform(m)

        path.offset(rect.left.toFloat(), rect.top.toFloat())
        return path
    }

    private fun getArcPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var start = 0f
        var end = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            if (values != null && values.size >= 2) {
                start = values[0]!! * TODEGREE_07
                end = values[1]!! * TODEGREE_07
            } else {
                start = 270f
                end = 0f
            }
        } else {
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    start = values[0]!! / 3
                }
                if (values.size >= 2 && values[1] != null) {
                    end = values[1]!! / 3
                }
            } else {
                start = 270f
                end = 0f
            }

            if (start < 0) {
                start += 360f
            }
            if (end < 0) {
                end += 360f
            }
        }

        val fill = shape.getBackgroundAndFill()

        var extendPath = ExtendPath()
        var path = Path()
        if (fill != null) {
            extendPath = ExtendPath()
            path = Path()

            path.moveTo(rect.exactCenterX(), rect.exactCenterY())
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, start, (end - start + 360) % 360)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, start, (end - start + 360) % 360)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }
        return paths
    }

    private fun getBracketPairPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1 && values[0] != null) {
            x = min(rect.width(), rect.height()) * values[0]!!
        } else {
            x = min(rect.width(), rect.height()) * 0.18f
        }
        val bgFill = shape.getBackgroundAndFill()
        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            val radii = floatArrayOf(x, x, x, x, x, x, x, x)
            path.addRoundRect(rectF, radii, Path.Direction.CW)

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()

            rectF.set(
                rect.right - x * 2,
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.top + x * 2
            )
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                rect.right - x * 2,
                rect.bottom - x * 2,
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)
            path.moveTo(rect.left + x, rect.bottom.toFloat())
            rectF.set(
                rect.left.toFloat(),
                rect.bottom - x * 2,
                rect.left + x * 2,
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 90f, 90f)
            rectF.set(rect.left.toFloat(), rect.top.toFloat(), rect.left + x * 2, rect.top + x * 2)
            path.arcTo(rectF, 180f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }

        return paths
    }

    private fun getLeftBracketPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            if (values != null && values.size >= 1) {
                x = min(rect.width(), rect.height()) * values[0]!!
            } else {
                x = min(rect.width(), rect.height()) * 0.08f
            }
        } else {
            if (values != null && values.size >= 1 && values[0] != null) {
                if (values[0] != null) {
                    x = rect.height() * values[0]!!
                }
            } else {
                x = rect.height() * 0.08f
            }
        }

        val bgFill = shape.getBackgroundAndFill()

        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()

            rectF.set(
                rect.left.toFloat(),
                rect.bottom - x * 2,
                (rect.right * 2 - rect.left).toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                (rect.right * 2 - rect.left).toFloat(),
                rect.top + x * 2
            )
            path.arcTo(rectF, 180f, 90f)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.left.toFloat(),
                rect.bottom - x * 2,
                (rect.right * 2 - rect.left).toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                (rect.right * 2 - rect.left).toFloat(),
                rect.top + x * 2
            )
            path.arcTo(rectF, 180f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }

        return paths
    }

    private fun getRightBracketPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        val values = shape.getAdjustData()

        if (shape.isAutoShape07()) {
            if (values != null && values.size >= 1 && values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            } else {
                x = min(rect.width(), rect.height()) * 0.08f
            }
        } else {
            if (values != null && values.size >= 1 && values[0] != null) {
                x = rect.height() * values[0]!!
            } else {
                x = rect.height() * 0.08f
            }
        }

        val bgFill = shape.getBackgroundAndFill()

        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                (rect.left * 2 - rect.right).toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.top + x * 2
            )
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                (rect.left * 2 - rect.right).toFloat(),
                rect.bottom - x * 2,
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                (rect.left * 2 - rect.right).toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.top + x * 2
            )
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                (rect.left * 2 - rect.right).toFloat(),
                rect.bottom - x * 2,
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }


        return paths
    }

    private fun getBracePairPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = min(rect.width(), rect.height()) * 0.08f
        val values = shape.getAdjustData()
        if (values != null && values.size >= 1) {
            if (values[0] != null) {
                x = min(rect.width(), rect.height()) * values[0]!!
            }
        }

        val bgFill = shape.getBackgroundAndFill()

        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()

            rectF.set(rect.right - x * 3, rect.top.toFloat(), rect.right - x, rect.top + x * 2)
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                rect.right - x, rect.exactCenterY() - x * 2, rect.right + x,
                rect.exactCenterY()
            )
            path.arcTo(rectF, 180f, -90f)
            rectF.set(
                rect.right - x, rect.exactCenterY(), rect.right + x,
                rect.exactCenterY() + x * 2
            )
            path.arcTo(rectF, 270f, -90f)
            rectF.set(
                rect.right - x * 3,
                rect.bottom - x * 2,
                rect.right - x,
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)

            rectF.set(rect.left + x, rect.bottom - x * 2, rect.left + x * 3, rect.bottom.toFloat())
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left - x, rect.exactCenterY(), rect.left + x,
                rect.exactCenterY() + x * 2
            )
            path.arcTo(rectF, 0f, -90f)
            rectF.set(
                rect.left - x, rect.exactCenterY() - x * 2, rect.left + x,
                rect.exactCenterY()
            )
            path.arcTo(rectF, 90f, -90f)
            rectF.set(rect.left + x, rect.top.toFloat(), rect.left + x * 3, rect.top + x * 2)
            path.arcTo(rectF, 180f, 90f)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()

            path.moveTo(rect.right - x * 2, rect.top.toFloat())
            rectF.set(rect.right - x * 3, rect.top.toFloat(), rect.right - x, rect.top + x * 2)
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                rect.right - x, rect.exactCenterY() - x * 2, rect.right + x,
                rect.exactCenterY()
            )
            path.arcTo(rectF, 180f, -90f)
            rectF.set(
                rect.right - x, rect.exactCenterY(), rect.right + x,
                rect.exactCenterY() + x * 2
            )
            path.arcTo(rectF, 270f, -90f)
            rectF.set(
                rect.right - x * 3,
                rect.bottom - x * 2,
                rect.right - x,
                rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)

            path.moveTo(rect.left + x * 2, rect.bottom.toFloat())
            rectF.set(rect.left + x, rect.bottom - x * 2, rect.left + x * 3, rect.bottom.toFloat())
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left - x, rect.exactCenterY(), rect.left + x,
                rect.exactCenterY() + x * 2
            )
            path.arcTo(rectF, 0f, -90f)
            rectF.set(
                rect.left - x, rect.exactCenterY() - x * 2, rect.left + x,
                rect.exactCenterY()
            )
            path.arcTo(rectF, 90f, -90f)
            rectF.set(rect.left + x, rect.top.toFloat(), rect.left + x * 3, rect.top + x * 2)
            path.arcTo(rectF, 180f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }

        return paths
    }

    private fun getLeftBracePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        var y = rect.height() * 0.5f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.08333f
            if (values != null && values.size >= 2) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
                if (values[1] != null) {
                    y = rect.height() * values[1]!!
                }
            }
        } else {
            x = rect.height() * 0.08333f
            if (values != null && values.size >= 2) {
                if (values[0] != null) {
                    x = rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    y = rect.height() * values[1]!!
                }
            }
        }

        if (rect.top + y + x * 2 > rect.bottom) {
            x = (rect.height() - y) / 2
        }

        val bgFill = shape.getBackgroundAndFill()

        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.exactCenterX(), rect.bottom - x * 2,
                rect.right + rect.width().toFloat() / 2, rect.bottom.toFloat()
            )
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top + y,
                rect.exactCenterX(), rect.top + y + x * 2
            )
            path.arcTo(rectF, 0f, -90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top + y - x * 2,
                rect.exactCenterX(), rect.top + y
            )
            path.arcTo(rectF, 90f, -90f)
            rectF.set(
                rect.exactCenterX(), rect.top.toFloat(),
                rect.right + rect.width().toFloat() / 2, rect.top + x * 2
            )
            path.arcTo(rectF, 180f, 90f)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.exactCenterX(), rect.bottom - x * 2,
                rect.right + rect.width().toFloat() / 2, rect.bottom.toFloat()
            )
            path.arcTo(rectF, 90f, 90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top + y,
                rect.exactCenterX(), rect.top + y + x * 2
            )
            path.arcTo(rectF, 0f, -90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top + y - x * 2,
                rect.exactCenterX(), rect.top + y
            )
            path.arcTo(rectF, 90f, -90f)
            rectF.set(
                rect.exactCenterX(), rect.top.toFloat(),
                rect.right + rect.width().toFloat() / 2, rect.top + x * 2
            )
            path.arcTo(rectF, 180f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }

        return paths
    }

    private fun getRightBracePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var x = 0f
        var y = rect.height() * 0.5f
        val values = shape.getAdjustData()
        if (shape.isAutoShape07()) {
            x = min(rect.width(), rect.height()) * 0.08333f
            if (values != null && values.size >= 2) {
                if (values[0] != null) {
                    x = min(rect.width(), rect.height()) * values[0]!!
                }
                if (values[1] != null) {
                    y = rect.height() * values[1]!!
                }
            }
        } else {
            x = rect.height() * 0.08333f
            if (values != null && values.size >= 2) {
                if (values[0] != null) {
                    x = rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    y = rect.height() * values[1]!!
                }
            }
        }

        if (rect.top + y + x * 2 > rect.bottom) {
            x = (rect.height() - y) / 2
        }

        val bgFill = shape.getBackgroundAndFill()

        var extendPath: ExtendPath? = null
        var path: Path? = null
        if (bgFill != null) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top.toFloat(),
                (rect.right + rect.left).toFloat() / 2, rect.top + x * 2
            )
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                (rect.right + rect.left).toFloat() / 2, rect.top + y - x * 2,
                rect.right + rect.width().toFloat() / 2, rect.top + y
            )
            path.arcTo(rectF, 180f, -90f)
            rectF.set(
                (rect.right + rect.left).toFloat() / 2, rect.top + y,
                rect.right + rect.width().toFloat() / 2, rect.top + y + x * 2
            )
            path.arcTo(rectF, 270f, -90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.bottom - x * 2,
                (rect.right + rect.left).toFloat() / 2, rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)
            path.close()

            extendPath.path = path
            extendPath.backgroundAndFill = bgFill
            paths.add(extendPath)
        }

        if (shape.hasLine()) {
            extendPath = ExtendPath()
            path = Path()
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.top.toFloat(),
                (rect.right + rect.left).toFloat() / 2, rect.top + x * 2
            )
            path.arcTo(rectF, 270f, 90f)
            rectF.set(
                (rect.right + rect.left).toFloat() / 2, rect.top + y - x * 2,
                rect.right + rect.width().toFloat() / 2, rect.top + y
            )
            path.arcTo(rectF, 180f, -90f)
            rectF.set(
                (rect.right + rect.left).toFloat() / 2, rect.top + y,
                rect.right + rect.width().toFloat() / 2, rect.top + y + x * 2
            )
            path.arcTo(rectF, 270f, -90f)
            rectF.set(
                rect.left - rect.width().toFloat() / 2, rect.bottom - x * 2,
                (rect.right + rect.left).toFloat() / 2, rect.bottom.toFloat()
            )
            path.arcTo(rectF, 0f, 90f)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)
        }
        return paths
    }
}
