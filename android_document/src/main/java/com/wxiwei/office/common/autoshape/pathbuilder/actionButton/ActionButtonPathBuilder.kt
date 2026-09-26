/*
 * 文件名称:          actionButtonUtil.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:22:59
 */
package com.wxiwei.office.common.autoshape.pathbuilder.actionButton

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.ss.util.ColorUtil
import kotlin.math.min
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
 * 日期:            2012-9-27
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
object ActionButtonPathBuilder {
    private val tempRect = RectF()

    private val pathExList: MutableList<ExtendPath?> = ArrayList<ExtendPath?>(2)

    //picture fill color
    private const val PICTURECOLOR = -0x70bbbbbc

    private const val PICTURECOLOR_LIGHT = -0x70aaaaab

    private const val PICTURECOLOR_DARK = -0x70323233


    private val TINT_LIGHT1 = -0.3f

    private const val TINT_LIGHT2 = 0.6f

    private val TINT_DARK = -0.5f

    /**
     * get action button path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getActionButtonExtendPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?>? {
        pathExList.clear()

        when (shape.getShapeType()) {
            ShapeTypes.ActionButtonBackPrevious -> return getBackPreviousPath(shape, rect)

            ShapeTypes.ActionButtonForwardNext -> return getForwardNextPath(shape, rect)

            ShapeTypes.ActionButtonBeginning -> return getBeginningPath(shape, rect)

            ShapeTypes.ActionButtonEnd -> return getEndPath(shape, rect)

            ShapeTypes.ActionButtonHome -> return getHomePath(shape, rect)

            ShapeTypes.ActionButtonInformation -> return getInformationPath(shape, rect)

            ShapeTypes.ActionButtonReturn -> return getReturnPath(shape, rect)

            ShapeTypes.ActionButtonMovie -> return getMoviePath(shape, rect)

            ShapeTypes.ActionButtonDocument -> return getDocumentPath(shape, rect)

            ShapeTypes.ActionButtonSound -> return getSoundPath(shape, rect)

            ShapeTypes.ActionButtonHelp -> return getHelpPath(shape, rect)

            ShapeTypes.ActionButtonBlank -> return getBlankPath(shape, rect)

        }

        return null
    }

    private fun getBackPreviousPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        pathExtend = ExtendPath()
        path = Path()
        val arrowLen = Math.round(min(rect.width(), rect.height()) * 0.75f)
        val off = Math.round(sqrt(3.0) / 4 * arrowLen).toFloat()
        path.moveTo(rect.centerX() - off, rect.centerY().toFloat())
        path.lineTo(rect.centerX() + off, (rect.centerY() - arrowLen / 2).toFloat())
        path.lineTo(rect.centerX() + off, (rect.centerY() + arrowLen / 2).toFloat())
        path.close()

        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }
        pathExtend.backgroundAndFill = fill

        pathExtend.path = path
        pathExList.add(pathExtend)

        return pathExList
    }


    private fun getForwardNextPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        pathExtend = ExtendPath()
        path = Path()
        val arrowLen = Math.round(min(rect.width(), rect.height()) * 0.75f)
        val off = Math.round(sqrt(3.0) / 4 * arrowLen).toFloat()
        path.moveTo(rect.centerX() + off, rect.centerY().toFloat())
        path.lineTo(rect.centerX() - off, (rect.centerY() + arrowLen / 2).toFloat())
        path.lineTo(rect.centerX() - off, (rect.centerY() - arrowLen / 2).toFloat())
        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }


    private fun getBeginningPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        pathExtend = ExtendPath()
        path = Path()
        val len = min(rect.width(), rect.height())

        path.addRect(
            rect.centerX() - len * 0.375f,
            rect.centerY() - len * 0.375f,
            rect.centerX() - len * (0.375f - 0.1f),
            rect.centerY() + len * 0.375f,
            Path.Direction.CW
        )

        path.moveTo(rect.centerX() - len * 3 / 16f, rect.centerY().toFloat())
        path.lineTo(rect.centerX() + len * 0.375f, rect.centerY() - len * 0.375f)
        path.lineTo(rect.centerX() + len * 0.375f, rect.centerY() + len * 0.375f)
        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getEndPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)


        pathExtend = ExtendPath()
        path = Path()
        val len = min(rect.width(), rect.height())

        path.addRect(
            rect.centerX() + len * (0.375f - 0.1f),
            rect.centerY() - len * 0.375f,
            rect.centerX() + len * 0.375f,
            rect.centerY() + len * 0.375f,
            Path.Direction.CW
        )

        path.moveTo(rect.centerX() + len * 3 / 16f, rect.centerY().toFloat())
        path.lineTo(rect.centerX() - len * 0.375f, rect.centerY() + len * 0.375f)
        path.lineTo(rect.centerX() - len * 0.375f, rect.centerY() - len * 0.375f)
        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getHomePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        /**/////////// */
        pathExtend = ExtendPath()
        path = Path()
        val len = min(rect.width(), rect.height())

        path.addRect(
            rect.centerX() - len * 0.28f,
            rect.centerY().toFloat(),
            rect.centerX() + len * 0.28f,
            rect.centerY() + len * 0.375f,
            Path.Direction.CW
        )

        path.moveTo(rect.centerX() + len * 0.14f, rect.centerY() - len * 0.33f)
        path.lineTo(rect.centerX() + len * 0.24f, rect.centerY() - len * 0.33f)
        path.lineTo(rect.centerX() + len * 0.24f, rect.centerY() - len * (0.375f - 0.24f))
        path.lineTo(rect.centerX() + len * 0.14f, rect.centerY() - len * (0.375f - 0.14f))
        path.close()

        pathExtend.path = path
        var fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_LIGHT1.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR_LIGHT)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        /**/////////////////// */
        pathExtend = ExtendPath()
        path = Path()

        path.moveTo(rect.centerX() - len * 0.375f, rect.centerY().toFloat())
        path.lineTo(rect.centerX().toFloat(), rect.centerY() - len * 0.375f)
        path.lineTo(rect.centerX() + len * 0.375f, rect.centerY().toFloat())
        path.close()

        path.addRect(
            rect.centerX() - len * 0.05f,
            rect.centerY() + len * 0.18f,
            rect.centerX() + len * 0.05f,
            rect.centerY() + len * 0.375f,
            Path.Direction.CW
        )

        pathExtend.path = path
        fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getInformationPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        /**/////////// */
        pathExtend = ExtendPath()
        path = Path()
        val len = min(rect.width(), rect.height())

        path.addCircle(
            rect.centerX().toFloat(),
            rect.centerY().toFloat(),
            len * 0.375f,
            Path.Direction.CW
        )

        pathExtend.path = path
        var fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        /**/////////////////// */
        pathExtend = ExtendPath()
        path = Path()

        path.addCircle(
            rect.centerX().toFloat(),
            rect.centerY() - len * 0.22f,
            len * 0.06f,
            Path.Direction.CW
        )

        path.moveTo(rect.centerX() - len * 0.12f, rect.centerY() - len * 0.11f)
        path.lineTo(rect.centerX() + len * 0.06f, rect.centerY() - len * 0.11f)
        path.lineTo(rect.centerX() + len * 0.06f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() + len * 0.12f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() + len * 0.12f, rect.centerY() + len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.12f, rect.centerY() + len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.12f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() - len * 0.06f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() - len * 0.06f, rect.centerY() - len * 0.08f)
        path.lineTo(rect.centerX() - len * 0.12f, rect.centerY() - len * 0.08f)
        path.close()

        pathExtend.path = path
        fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_LIGHT2.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR_DARK)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getReturnPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        ////
        val len = min(rect.width(), rect.height())
        pathExtend = ExtendPath()
        path = Path()
        path.moveTo(rect.centerX() - len * 0.4f, rect.centerY() - len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.2f, rect.centerY() - len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.2f, rect.centerY() + len * 0.1f)

        tempRect.set(
            rect.centerX() - len * 0.2f,
            rect.centerY().toFloat(),
            rect.centerX().toFloat(),
            rect.centerY() + len * 0.2f
        )
        path.arcTo(tempRect, 180f, -90f)
        path.lineTo(rect.centerX().toFloat(), rect.centerY() + len * 0.2f)

        tempRect.set(
            rect.centerX() - len * 0.1f,
            rect.centerY().toFloat(),
            rect.centerX() + len * 0.1f,
            rect.centerY() + len * 0.2f
        )
        path.arcTo(tempRect, 90f, -90f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.2f)

        path.lineTo(rect.centerX().toFloat(), rect.centerY() - len * 0.2f)
        path.lineTo(rect.centerX() + len * 0.2f, rect.centerY() - len * 0.4f)
        path.lineTo(rect.centerX() + len * 0.4f, rect.centerY() - len * 0.2f)

        path.lineTo(rect.centerX() + len * 0.3f, rect.centerY() - len * 0.2f)
        tempRect.set(
            rect.centerX() - len * 0.3f,
            rect.centerY() - len * 0.2f,
            rect.centerX() + len * 0.3f,
            rect.centerY() + len * 0.4f
        )
        path.arcTo(tempRect, 0f, 90f)
        tempRect.set(
            rect.centerX() - len * 0.4f,
            rect.centerY() - len * 0.2f,
            rect.centerX() + len * 0.2f,
            rect.centerY() + len * 0.4f
        )
        path.arcTo(tempRect, 90f, 90f)

        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getMoviePath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        ////
        val len = min(rect.width(), rect.height())
        pathExtend = ExtendPath()
        path = Path()
        path.moveTo(rect.centerX() - len * 0.38f, rect.centerY() - len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.31f, rect.centerY() - len * 0.2f)
        path.lineTo(rect.centerX() - len * 0.3f, rect.centerY() - len * 0.18f)

        path.lineTo(rect.centerX() + len * 0.18f, rect.centerY() - len * 0.18f)
        path.lineTo(rect.centerX() + len * 0.22f, rect.centerY() - len * 0.15f)
        path.lineTo(rect.centerX() + len * 0.22f, rect.centerY() - len * 0.12f)
        path.lineTo(rect.centerX() + len * 0.31f, rect.centerY() - len * 0.12f)

        path.lineTo(rect.centerX() + len * 0.34f, rect.centerY() - len * 0.15f)
        path.lineTo(rect.centerX() + len * 0.37f, rect.centerY() - len * 0.15f)
        path.lineTo(rect.centerX() + len * 0.37f, rect.centerY() + len * 0.15f)
        path.lineTo(rect.centerX() + len * 0.34f, rect.centerY() + len * 0.15f)

        path.lineTo(rect.centerX() + len * 0.29f, rect.centerY() + len * 0.12f)
        path.lineTo(rect.centerX() + len * 0.22f, rect.centerY() + len * 0.12f)

        path.lineTo(rect.centerX() + len * 0.22f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() - len * 0.29f, rect.centerY() + len * 0.16f)
        path.lineTo(rect.centerX() - len * 0.29f, rect.centerY() - len * 0.06f)
        path.lineTo(rect.centerX() - len * 0.31f, rect.centerY() - len * 0.06f)

        path.lineTo(rect.centerX() - len * 0.32f, rect.centerY() - len * 0.04f)
        path.lineTo(rect.centerX() - len * 0.38f, rect.centerY() - len * 0.04f)

        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getDocumentPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        ////
        val len = min(rect.width(), rect.height())
        pathExtend = ExtendPath()
        path = Path()
        path.moveTo(rect.centerX() - len * 0.28f, rect.centerY() - len * 0.38f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.38f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.18f)
        path.lineTo(rect.centerX() + len * 0.28f, rect.centerY() - len * 0.18f)
        path.lineTo(rect.centerX() + len * 0.28f, rect.centerY() + len * 0.38f)
        path.lineTo(rect.centerX() - len * 0.28f, rect.centerY() + len * 0.38f)
        path.close()

        pathExtend.path = path
        var fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_LIGHT1.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR_LIGHT)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)


        ////
        pathExtend = ExtendPath()
        path = Path()

        path.moveTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.38f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.18f)
        path.lineTo(rect.centerX() + len * 0.28f, rect.centerY() - len * 0.18f)
        path.close()

        pathExtend.path = path
        fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getSoundPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        ////
        val len = min(rect.width(), rect.height())
        pathExtend = ExtendPath()
        path = Path()
        path.moveTo(rect.centerX() - len * 0.38f, rect.centerY() - len * 0.14f)
        path.lineTo(rect.centerX() - len * 0.14f, rect.centerY() - len * 0.14f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() - len * 0.38f)
        path.lineTo(rect.centerX() + len * 0.1f, rect.centerY() + len * 0.38f)
        path.lineTo(rect.centerX() - len * 0.14f, rect.centerY() + len * 0.14f)
        path.lineTo(rect.centerX() - len * 0.38f, rect.centerY() + len * 0.14f)
        path.close()

        val off = min(5f, len * 0.01f)
        path.moveTo(rect.centerX() + len * 0.18f, rect.centerY() - len * 0.14f)
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY() - len * 0.28f)
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY() - len * 0.28f + off)
        path.lineTo(rect.centerX() + len * 0.18f, rect.centerY() - len * 0.14f + off)
        path.close()

        path.moveTo(rect.centerX() + len * 0.18f, rect.centerY().toFloat())
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY().toFloat())
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY() + off)
        path.lineTo(rect.centerX() + len * 0.18f, rect.centerY() + off)
        path.close()

        path.moveTo(rect.centerX() + len * 0.18f, rect.centerY() + len * 0.14f)
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY() + len * 0.28f)
        path.lineTo(rect.centerX() + len * 0.38f, rect.centerY() + len * 0.28f + off)
        path.lineTo(rect.centerX() + len * 0.18f, rect.centerY() + len * 0.14f + off)
        path.close()

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR_LIGHT)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }


    private fun getHelpPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        var pathExtend = ExtendPath()
        var path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        ////
        val len = min(rect.width(), rect.height())
        pathExtend = ExtendPath()
        path = Path()

        path.moveTo(rect.centerX() - len * 0.2f, rect.centerY() - len * 0.16f)

        tempRect.set(
            rect.centerX() - len * 0.2f,
            rect.centerY() - len * 0.36f,
            rect.centerX() + len * 0.2f,
            rect.centerY() + len * 0.04f
        )
        path.arcTo(tempRect, 180f, 240f)

        tempRect.set(
            rect.centerX() + len * 0.05f,
            rect.centerY() + len * 0.012f,
            rect.centerX() + len * 0.15f,
            rect.centerY() + len * 0.112f
        )
        path.arcTo(tempRect, 270f, -90f)

        path.lineTo(rect.centerX() + len * 0.05f, rect.centerY() + len * 0.18f)
        path.lineTo(rect.centerX() - len * 0.05f, rect.centerY() + len * 0.18f)
        path.lineTo(rect.centerX() - len * 0.05f, rect.centerY() + len * 0.1f)

        tempRect.set(
            rect.centerX() - len * 0.05f,
            rect.centerY() - len * 0.073f,
            rect.centerX() + len * 0.15f,
            rect.centerY() + len * 0.273f
        )
        path.arcTo(tempRect, 180f, 90f)

        tempRect.set(
            rect.centerX() - len * 0.1f,
            rect.centerY() - len * 0.26f,
            rect.centerX() + len * 0.1f,
            rect.centerY() - len * 0.06f
        )
        path.arcTo(tempRect, 60f, -240f)

        path.close()

        path.addCircle(
            rect.centerX().toFloat(),
            rect.centerY() + len * 0.28f,
            len * 0.08f,
            Path.Direction.CW
        )

        pathExtend.path = path
        val fill = BackgroundAndFill()
        fill.setFillType(BackgroundAndFill.FILL_SOLID)

        val shapeFill = shape.getBackgroundAndFill()
        if (shapeFill != null && shapeFill.getFillType() == BackgroundAndFill.FILL_SOLID) {
            fill.setForegroundColor(
                ColorUtil.instance()
                    .getColorWithTint(shapeFill.getForegroundColor(), TINT_DARK.toDouble())
            )
        } else {
            fill.setForegroundColor(PICTURECOLOR_LIGHT)
        }

        pathExtend.backgroundAndFill = fill
        pathExList.add(pathExtend)

        return pathExList
    }

    private fun getBlankPath(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
        val pathExtend = ExtendPath()
        val path = Path()
        tempRect.set(rect)
        path.addRect(tempRect, Path.Direction.CW)
        pathExtend.path = path
        pathExtend.backgroundAndFill = shape.getBackgroundAndFill()
        pathExList.add(pathExtend)

        return pathExList
    }
}
