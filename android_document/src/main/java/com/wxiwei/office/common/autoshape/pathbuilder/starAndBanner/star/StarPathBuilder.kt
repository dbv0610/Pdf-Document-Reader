/*
 * 文件名称:          StarAndFlagPathBuilder.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:02:27
 */
package com.wxiwei.office.common.autoshape.pathbuilder.starAndBanner.star

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.pow
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
 * 日期:            2012-10-11
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
object StarPathBuilder {
    private val sm = Matrix()

    private val path = Path()

    /**
     * get star path
     * @param shape
     * @param rect
     * @return
     */
    @JvmStatic
    fun getStarPath(shape: AutoShape, rect: Rect): Path? {
        path.reset()

        when (shape.getShapeType()) {
            ShapeTypes.IrregularSeal1 -> return getIrregularSeal1Path(shape, rect)

            ShapeTypes.IrregularSeal2 -> return getIrregularSeal2Path(shape, rect)

            ShapeTypes.Star4, ShapeTypes.Star5, ShapeTypes.Star, ShapeTypes.Star6, ShapeTypes.Star7, ShapeTypes.Star8, ShapeTypes.Star10, ShapeTypes.Star12, ShapeTypes.Star16, ShapeTypes.Star24, ShapeTypes.Star32 -> if (shape.isAutoShape07()) {
                return LaterStarPathBuilder.getStarPath(shape, rect)
            } else {
                return EarlyStarPathBuilder.getStarPath(shape, rect)
            }
        }

        return path
    }

    private fun getIrregularSeal1Path(shape: AutoShape?, rect: Rect): Path {
        val len = 380f
        path.moveTo(66f, 206f)
        path.lineTo(0f, 150f)

        path.lineTo(83f, 134f)
        path.lineTo(8f, 41f)

        path.lineTo(128f, 112f)
        path.lineTo(147f, 42f)

        path.lineTo(190f, 103f)
        path.lineTo(255f, 0f)

        path.lineTo(250f, 93f)
        path.lineTo(323f, 78f)

        path.lineTo(294f, 128f)
        path.lineTo(370f, 142f)

        path.lineTo(310f, 185f)
        path.lineTo(380f, 233f)

        path.lineTo(296f, 228f)
        path.lineTo(319f, 318f)

        path.lineTo(247f, 255f)
        path.lineTo(233f, 346f)

        path.lineTo(185f, 263f)
        path.lineTo(149f, 380f)

        path.lineTo(135f, 275f)
        path.lineTo(84f, 309f)

        path.lineTo(99f, 245f)
        path.lineTo(0f, 256f)

        path.close()

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())
        return path
    }

    private fun getIrregularSeal2Path(shape: AutoShape?, rect: Rect): Path {
        val len = 380f

        path.moveTo(70f, 203f)
        path.lineTo(20f, 143f)

        path.lineTo(95f, 137f)
        path.lineTo(79f, 64f)

        path.lineTo(151f, 113f)
        path.lineTo(170f, 32f)

        path.lineTo(202f, 76f)
        path.lineTo(260f, 0f)

        path.lineTo(255f, 101f)
        path.lineTo(316f, 55f)

        path.lineTo(287f, 114f)
        path.lineTo(380f, 115f)

        path.lineTo(298f, 164f)
        path.lineTo(321f, 198f)

        path.lineTo(287f, 215f)
        path.lineTo(331f, 273f)

        path.lineTo(257f, 251f)
        path.lineTo(262f, 304f)

        path.lineTo(215f, 280f)
        path.lineTo(204f, 330f)

        path.lineTo(174f, 304f)
        path.lineTo(153f, 345f)

        path.lineTo(132f, 317f)
        path.lineTo(86f, 380f)

        path.lineTo(85f, 319f)
        path.lineTo(23f, 313f)

        path.lineTo(58f, 269f)
        path.lineTo(0f, 225f)
        path.close()

        sm.reset()
        sm.postScale(rect.width() / len, rect.height() / len)
        path.transform(sm)

        path.offset(rect.left.toFloat(), rect.top.toFloat())
        return path
    }


    /**
     * the rect of the star must be square
     * 
     * @param A half  horizontal width of the outer oval
     * @param B half  vertical width of the outer oval
     * @param a half  horizontal width of the inner oval
     * @param b half  vertical width of the inner oval
     * @param starPoints points count of the star
     * @return
     */
    @JvmStatic
    fun getStarPath(A: Int, B: Int, a: Int, b: Int, starPoints: Int): Path {
        val offDegree = 360f / (starPoints * 2)


        //270 degree
        path.moveTo(0f, -B.toFloat())

        var x = 0f
        var y = 0f
        var degree = 270f
        if (a > 0 && b > 0) {
            var index = 1
            while (index++ < starPoints) {
                degree = (degree + offDegree) % 360
                if (degree == 90f) {
                    x = 0f
                    y = b.toFloat()
                } else {
                    x = (a * b / sqrt(
                        b.toDouble().pow(2.0) + (a * tan(degree * Math.PI / 180)).pow(2.0)
                    )).toFloat()
                    if (degree > 90 && degree < 270) {
                        x = -x
                    }

                    y = (x * tan(degree * Math.PI / 180)).toFloat()
                }
                path.lineTo(x, y)

                degree = (degree + offDegree) % 360
                if (degree == 90f) {
                    x = 0f
                    y = B.toFloat()
                } else {
                    x = (A * B / sqrt(
                        B.toDouble().pow(2.0) + (A * tan(degree * Math.PI / 180)).pow(2.0)
                    )).toFloat()
                    if (degree > 90 && degree < 270) {
                        x = -x
                    }

                    y = (x * tan(degree * Math.PI / 180)).toFloat()
                }
                path.lineTo(x, y)
            }


            //the last point
            degree = 270 - offDegree
            x = -(a * b / sqrt(
                b.toDouble().pow(2.0) + (a * tan(degree * Math.PI / 180)).pow(2.0)
            )).toFloat()
            y = (x * tan(degree * Math.PI / 180)).toFloat()
            path.lineTo(x, y)
        } else {
            var index = 1
            while (index++ < starPoints) {
                degree = (degree + offDegree) % 360
                path.lineTo(0f, 0f)

                degree = (degree + offDegree) % 360
                if (degree == 90f) {
                    x = 0f
                    y = B.toFloat()
                } else {
                    x = (A * B / sqrt(
                        B.toDouble().pow(2.0) + (A * tan(degree * Math.PI / 180)).pow(2.0)
                    )).toFloat()
                    if (degree > 90 && degree < 270) {
                        x = -x
                    }

                    y = (x * tan(degree * Math.PI / 180)).toFloat()
                }
                path.lineTo(x, y)
            }


            //the last point
            path.lineTo(0f, 0f)
        }

        path.close()

        return path
    }
}
