/*
 * 文件名称:          EarlyStarPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:38:00
 */
package com.wxiwei.office.common.autoshape.pathbuilder.starAndBanner.star

import android.graphics.Matrix
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
 * 日期:            2012-10-19
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
object EarlyStarPathBuilder {
    private val sm = Matrix()
    var s_rect: RectF = RectF()

    @JvmStatic
    fun getStarPath(shape: AutoShape, rect: Rect): Path? {
        when (shape.getShapeType()) {
            ShapeTypes.Star4 -> return getStar4Path(shape, rect)

            ShapeTypes.Star5, ShapeTypes.Star -> return getStar5Path(shape, rect)

            ShapeTypes.Star8 -> return getStar8Path(shape, rect)

            ShapeTypes.Star16 -> return getStar16Path(shape, rect)

            ShapeTypes.Star24 -> return getStar24Path(shape, rect)

            ShapeTypes.Star32 -> return getStar32Path(shape, rect)
        }

        return null
    }

    private fun getStar4Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        val width = len
        val height = len
        var a = 0f
        var b = 0f
        if (values != null && values.size == 1) {
            if (values[0]!! > 0.5f) {
                values[0] = 0.5f
            }
            a = len * (0.5f - values[0]!!)
            b = len * (0.5f - values[0]!!)
        } else {
            a = len * 0.125f
            b = len * 0.125f
        }

        val outA = width / 2
        val outB = height / 2

        val path = StarPathBuilder.getStarPath(outA.toInt(), outB.toInt(), a.toInt(), b.toInt(), 4)

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)
        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }

    private fun getStar5Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        var width = len
        var height = len
        var a = 0f
        var b = 0f

        width = width * 1.05146f
        height = height * 1.10557f

        a = width * 0.2f
        b = height * 0.2f

        val outA = width / 2
        val outB = height / 2

        val path = StarPathBuilder.getStarPath(outA.toInt(), outB.toInt(), a.toInt(), b.toInt(), 5)

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        height = height * rect.height() / len
        path.offset(rect.centerX().toFloat(), rect.centerY() + (height - rect.height()) / 2)

        return path
    }

    private fun getStar8Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        val width = len
        val height = len
        var a = 0f
        var b = 0f
        if (values != null && values.size == 1) {
            if (values[0]!! > 0.5f) {
                values[0] = 0.5f
            }
            a = width * (0.5f - values[0]!!)
            b = height * (0.5f - values[0]!!)
        } else {
            a = width * 0.375f
            b = height * 0.375f
        }

        val outA = width / 2
        val outB = height / 2

        val path = StarPathBuilder.getStarPath(outA.toInt(), outB.toInt(), a.toInt(), b.toInt(), 8)

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)
        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }

    private fun getStar16Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        val width = len
        val height = len
        var a = 0f
        var b = 0f
        if (values != null && values.size == 1) {
            if (values[0]!! > 0.5f) {
                values[0] = 0.5f
            }
            a = width * (0.5f - values[0]!!)
            b = height * (0.5f - values[0]!!)
        } else {
            a = width * 0.375f
            b = height * 0.375f
        }

        val path = StarPathBuilder.getStarPath(
            width.toInt() / 2,
            height.toInt() / 2,
            a.toInt(),
            b.toInt(),
            16
        )

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)
        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }


    private fun getStar24Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        val width = len
        val height = len
        var a = 0f
        var b = 0f
        if (values != null && values.size == 1) {
            if (values[0]!! > 0.5f) {
                values[0] = 0.5f
            }
            a = width * (0.5f - values[0]!!)
            b = height * (0.5f - values[0]!!)
        } else {
            a = width * 0.375f
            b = height * 0.375f
        }

        val path = StarPathBuilder.getStarPath(
            width.toInt() / 2,
            height.toInt() / 2,
            a.toInt(),
            b.toInt(),
            24
        )

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)
        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())
        return path
    }


    private fun getStar32Path(shape: AutoShape, rect: Rect): Path {
        val values = shape.getAdjustData()

        val len = min(rect.width(), rect.height()).toFloat()
        val width = len
        val height = len
        var a = 0f
        var b = 0f
        if (values != null && values.size == 1) {
            if (values[0]!! > 0.5f) {
                values[0] = 0.5f
            }
            a = width * (0.5f - values[0]!!)
            b = height * (0.5f - values[0]!!)
        } else {
            a = width * 0.375f
            b = height * 0.375f
        }

        val path = StarPathBuilder.getStarPath(
            width.toInt() / 2,
            height.toInt() / 2,
            a.toInt(),
            b.toInt(),
            32
        )

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)
        path.offset(rect.centerX().toFloat(), rect.centerY().toFloat())

        return path
    }
}
