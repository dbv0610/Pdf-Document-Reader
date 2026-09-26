/*
 * 文件名称:           WedgeCallout.java
 *  
 * 编译器:             android2.2
 * 时间:               上午9:25:20
 */
package com.wxiwei.office.common.autoshape.pathbuilder.wedgecallout

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.wxiwei.office.common.autoshape.ExtendPath
import com.wxiwei.office.common.shape.AutoShape
import com.wxiwei.office.common.shape.ShapeTypes
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * draw wedgeCallout
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-9-26
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class WedgeCalloutDrawing {
    /**
     * 
     * @param canvas
     * @param shape
     * @param rect
     */
    fun getWedgeCalloutPath(shape: AutoShape, rect: Rect): Any? {
        paths.clear()
        path.reset()

        when (shape.getShapeType()) {
            ShapeTypes.WedgeRectCallout -> return getWedgeRectCalloutPath(shape, rect)

            ShapeTypes.WedgeRoundRectCallout -> return getWedgeRoundRectCalloutPath(shape, rect)

            ShapeTypes.WedgeEllipseCallout -> return getWedgeEllipseCalloutPath(shape, rect)

            ShapeTypes.CloudCallout -> return getCloudCalloutPath(shape, rect)

            ShapeTypes.BorderCallout1 -> if (shape.isAutoShape07()) {
                return getBorderCallout1Path(shape, rect)
            } else {
                return get03BorderCallout2Path(shape, rect)
            }

            ShapeTypes.BorderCallout2 -> if (shape.isAutoShape07()) {
                return getBorderCallout2Path(shape, rect)
            } else {
                return get03BorderCallout2Path(shape, rect)
            }

            ShapeTypes.BorderCallout3 -> if (shape.isAutoShape07()) {
                return getBorderCallout3Path(shape, rect)
            } else {
                return get03BorderCallout3Path(shape, rect)
            }

            ShapeTypes.BorderCallout4 -> return get03BorderCallout4Path(shape, rect)

            ShapeTypes.AccentCallout1 -> if (shape.isAutoShape07()) {
                return getAccentCallout1Path(shape, rect)
            } else {
                return get03AccentCallout1Path(shape, rect)
            }

            ShapeTypes.AccentCallout2 -> if (shape.isAutoShape07()) {
                return getAccentCallout2Path(shape, rect)
            } else {
                return get03AccentCallout2Path(shape, rect)
            }

            ShapeTypes.AccentCallout3 -> if (shape.isAutoShape07()) {
                return getAccentCallout3Path(shape, rect)
            } else {
                return get03AccentCallout3(shape, rect)
            }

            ShapeTypes.AccentCallout4 -> return get03AccentCallout4(shape, rect)

            ShapeTypes.Callout1 -> if (shape.isAutoShape07()) {
                return getCallout1(shape, rect)
            } else {
                return get03AccentCallout1Path(shape, rect)
            }

            ShapeTypes.Callout2 -> if (shape.isAutoShape07()) {
                return getCallout2(shape, rect)
            } else {
                return get03Callout2(shape, rect)
            }

            ShapeTypes.Callout3 -> if (shape.isAutoShape07()) {
                return getCallout3(shape, rect)
            } else {
                return get03Callout3(shape, rect)
            }

            ShapeTypes.Callout4 -> return get03Callout4(shape, rect)

            ShapeTypes.AccentBorderCallout1 -> if (shape.isAutoShape07()) {
                return getAccentBorderCallout1(shape, rect)
            } else {
                return get03BorderCallout2Path(shape, rect)
            }

            ShapeTypes.AccentBorderCallout2 -> if (shape.isAutoShape07()) {
                return getAccentBorderCallout2(shape, rect)
            } else {
                return get03AccentBorderCallout2(shape, rect)
            }

            ShapeTypes.AccentBorderCallout3 -> if (shape.isAutoShape07()) {
                return getAccentBorderCallout3(shape, rect)
            } else {
                return get03AccentBorderCallout3(shape, rect)
            }

            ShapeTypes.AccentBorderCallout4 -> return get03AccentBorderCallout4(shape, rect)
        }
        return null
    }

    companion object {
        private val rectF = RectF()

        private val path = Path()

        private val paths: MutableList<ExtendPath?> = ArrayList<ExtendPath?>()

        //
        private val kit = WedgeCalloutDrawing()

        /**
         * 
         * @return
         */
        fun instance(): WedgeCalloutDrawing {
            return kit
        }

        /**
         * 矩形标注
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getWedgeRectCalloutPath(shape: AutoShape, rect: Rect): Path {
            var x = -rect.width() * 0.2f
            var y = rect.height() * 0.6f
            var z = rect.width().toFloat() / 12
            val values = shape.getAdjustData()
            if (shape.isAutoShape07()) {
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!!
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!!
                    }
                }
            } else {
                x = -rect.width() * 0.433f
                y = rect.height() * 0.7f
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!! - rect.width() / 2
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!! - rect.height() / 2
                    }
                }
            }

            if (abs(y / x).toFloat() < rect.height().toFloat() / rect.width()) {
                z = rect.height().toFloat() / 12
                // right
                if (x >= 0) {
                    path.moveTo(rect.left.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.top.toFloat())
                    if (y >= 0) {
                        path.lineTo(rect.right.toFloat(), rect.exactCenterY() + z)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right.toFloat(), rect.bottom - z * 2)
                    } else {
                        path.lineTo(rect.right.toFloat(), rect.top + z * 2)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right.toFloat(), rect.exactCenterY() - z)
                    }
                    path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                    path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
                } else {
                    path.moveTo(rect.left.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                    path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
                    if (y >= 0) {
                        path.lineTo(rect.left.toFloat(), rect.bottom - z * 2)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left.toFloat(), rect.exactCenterY() + z)
                    } else {
                        path.lineTo(rect.left.toFloat(), rect.exactCenterY() - z)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left.toFloat(), rect.top + z * 2)
                    }
                }
            }
            else {
                // bottom
                if (y >= 0) {
                    path.moveTo(rect.left.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                    if (x >= 0) {
                        path.lineTo(rect.right - z * 2, rect.bottom.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.exactCenterX() + z, rect.bottom.toFloat())
                    } else {
                        path.lineTo(rect.exactCenterX() - z, rect.bottom.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left + z * 2, rect.bottom.toFloat())
                    }
                    path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
                } else {
                    path.moveTo(rect.left.toFloat(), rect.top.toFloat())
                    if (x >= 0) {
                        path.lineTo(rect.exactCenterX() + z, rect.top.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right - z * 2, rect.top.toFloat())
                    } else {
                        path.lineTo(rect.left + z * 2, rect.top.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.exactCenterX() - z, rect.top.toFloat())
                    }
                    path.lineTo(rect.right.toFloat(), rect.top.toFloat())
                    path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                    path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
                }
            }
            path.close()

            return path
        }

        /**
         * 圆角矩形标注
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getWedgeRoundRectCalloutPath(shape: AutoShape, rect: Rect): Path {
            var x = -rect.width() * 0.2f
            var y = rect.height() * 0.6f
            var z = rect.width().toFloat() / 12
            var r = min(rect.width(), rect.height()) * 0.16667f
            val values = shape.getAdjustData()
            if (shape.isAutoShape07()) {
                if (values != null && values.size >= 3) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!!
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!!
                    }
                    if (values[2] != null) {
                        r = min(rect.width(), rect.height()) * values[2]!!
                    }
                }
            } else {
                x = -rect.width() * 0.433f
                y = rect.height() * 0.7f
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!! - rect.width() / 2
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!! - rect.height() / 2
                    }
                }
            }

            if (abs(y / x).toFloat() < rect.height().toFloat() / rect.width()) {
                z = rect.height().toFloat() / 12
                // right
                if (x >= 0) {
                    rectF.set(
                        rect.left.toFloat(),
                        rect.top.toFloat(),
                        rect.left + r * 2,
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 180f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.top.toFloat(),
                        rect.right.toFloat(),
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 270f, 90f)
                    if (y >= 0) {
                        path.lineTo(rect.right.toFloat(), rect.exactCenterY() + z)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right.toFloat(), rect.bottom - z * 2)
                    } else {
                        path.lineTo(rect.right.toFloat(), rect.top + z * 2)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right.toFloat(), rect.exactCenterY() - z)
                    }
                    rectF.set(
                        rect.right - r * 2,
                        rect.bottom - r * 2,
                        rect.right.toFloat(),
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 0f, 90f)
                    rectF.set(
                        rect.left.toFloat(),
                        rect.bottom - r * 2,
                        rect.left + r * 2,
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 90f, 90f)
                } else {
                    rectF.set(
                        rect.left.toFloat(),
                        rect.top.toFloat(),
                        rect.left + r * 2,
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 180f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.top.toFloat(),
                        rect.right.toFloat(),
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 270f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.bottom - r * 2,
                        rect.right.toFloat(),
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 0f, 90f)
                    rectF.set(
                        rect.left.toFloat(),
                        rect.bottom - r * 2,
                        rect.left + r * 2,
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 90f, 90f)
                    if (y >= 0) {
                        path.lineTo(rect.left.toFloat(), rect.bottom - z * 2)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left.toFloat(), rect.exactCenterY() + z)
                    } else {
                        path.lineTo(rect.left.toFloat(), rect.exactCenterY() - z)
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left.toFloat(), rect.top + z * 2)
                    }
                }
            }
            else {
                // bottom
                if (y >= 0) {
                    rectF.set(
                        rect.left.toFloat(),
                        rect.top.toFloat(),
                        rect.left + r * 2,
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 180f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.top.toFloat(),
                        rect.right.toFloat(),
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 270f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.bottom - r * 2,
                        rect.right.toFloat(),
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 0f, 90f)
                    if (x >= 0) {
                        path.lineTo(rect.right - z * 2, rect.bottom.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.exactCenterX() + z, rect.bottom.toFloat())
                    } else {
                        path.lineTo(rect.exactCenterX() - z, rect.bottom.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.left + z * 2, rect.bottom.toFloat())
                    }
                    rectF.set(
                        rect.left.toFloat(),
                        rect.bottom - r * 2,
                        rect.left + r * 2,
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 90f, 90f)
                } else {
                    rectF.set(
                        rect.left.toFloat(),
                        rect.top.toFloat(),
                        rect.left + r * 2,
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 180f, 90f)
                    if (x >= 0) {
                        path.lineTo(rect.exactCenterX() + z, rect.top.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.right - z * 2, rect.top.toFloat())
                    } else {
                        path.lineTo(rect.left + z * 2, rect.top.toFloat())
                        path.lineTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
                        path.lineTo(rect.exactCenterX() - z, rect.top.toFloat())
                    }
                    rectF.set(
                        rect.right - r * 2,
                        rect.top.toFloat(),
                        rect.right.toFloat(),
                        rect.top + r * 2
                    )
                    path.arcTo(rectF, 270f, 90f)
                    rectF.set(
                        rect.right - r * 2,
                        rect.bottom - r * 2,
                        rect.right.toFloat(),
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 0f, 90f)
                    rectF.set(
                        rect.left.toFloat(),
                        rect.bottom - r * 2,
                        rect.left + r * 2,
                        rect.bottom.toFloat()
                    )
                    path.arcTo(rectF, 90f, 90f)
                }
            }
            path.close()

            return path
        }

        /**
         * 椭圆形标注
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getWedgeEllipseCalloutPath(shape: AutoShape, rect: Rect): Path {
            var x = -rect.width() * 0.2f
            var y = rect.height() * 0.6f
            val values = shape.getAdjustData()
            if (shape.isAutoShape07()) {
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!!
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!!
                    }
                }
            } else {
                x = -rect.width() * 0.433f
                y = rect.height() * 0.7f
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        x = rect.width() * values[0]!! - rect.width() / 2
                    }
                    if (values[1] != null) {
                        y = rect.height() * values[1]!! - rect.height() / 2
                    }
                }
            }
            val angle1 = Math.toDegrees(atan2(rect.width().toDouble(), rect.height().toDouble()))
                .toFloat() / 2
            val angle2 = Math.toDegrees(atan2(abs(y).toDouble(), abs(x).toDouble())).toFloat()
            var start = 0f


            path.moveTo(rect.exactCenterX() + x, rect.exactCenterY() + y)
            rectF.set(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat()
            )
            if (y >= 0) {
                if (x >= 0) {
                    start = angle2 + angle1 / 2
                } else {
                    start = 180 - angle2 + angle1 / 2
                }

                path.arcTo(rectF, start, 360 - angle1)
            } else {
                if (x >= 0) {
                    start = 360 - angle2 - angle1 / 2
                } else {
                    start = 180 + angle2 - angle1 / 2
                }
                path.arcTo(rectF, start, -360 + angle1)
            }
            path.close()

            return path
        }

        /**
         * 云形标注
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getCloudCalloutPath(shape: AutoShape, rect: Rect): Path {
            val len = 468f


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

            val m = Matrix()
            m.postScale(rect.width() / len, rect.height() / len)
            path.transform(m)

            path.offset(rect.left.toFloat(), rect.top.toFloat())

            val values = shape.getAdjustData()
            var adj1 = 0
            var adj2 = 0
            if (shape.isAutoShape07()) {
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        adj1 = Math.round(rect.width() * values[0]!!)
                    }
                    if (values[1] != null) {
                        adj2 = Math.round(rect.height() * values[1]!!)
                    }
                } else {
                    adj1 = Math.round(rect.width() * -0.2f)
                    adj2 = Math.round(rect.height() * 0.6f)
                }
            } else {
                if (values != null && values.size >= 2) {
                    if (values[0] != null) {
                        adj1 = Math.round(rect.width() * values[0]!! - rect.width() / 2)
                    }
                    if (values[1] != null) {
                        adj2 = Math.round(rect.height() * values[1]!! - rect.height() / 2)
                    }
                } else {
                    adj1 = Math.round(rect.width() * -0.433f)
                    adj2 = Math.round(rect.height() * 0.7f)
                }
            }


            //intersection of circle centers line and outer cirlce
            val angle: Double = getAngle(adj1.toDouble(), adj2.toDouble())

            /**
             * y = x * tanθ
             * x = √(a^2* b^2 /(b^2 + a^2 * tanθ^2)
             */
            val a = rect.width() / 2
            val b = rect.height() / 2

            var outx = (a * b / sqrt(
                b.toDouble().pow(2.0) + (a * tan(angle * Math.PI / 180)).pow(2.0)
            )).toFloat()
            if (angle > 90 && angle < 270) {
                outx = -outx
            }

            var outy = (outx * tan(angle * Math.PI / 180)).toFloat()


            //center of small circle
            val sx = (rect.centerX() + adj1).toFloat()
            val sy = (rect.centerY() + adj2).toFloat()

            outx = rect.centerX() + outx
            outy = rect.centerY() + outy

            val r = min(rect.width(), rect.height()) / 468f
            //small circle
            path.addCircle(sx, sy, 16 * r, Path.Direction.CW)


            //middle circle
            var x = outx + 0.7f * (sx - outx)
            var y = outy + 0.7f * (sy - outy)
            path.addCircle(x, y, 24 * r, Path.Direction.CW)


            //larger circle
            x = outx + 0.3f * (sx - outx)
            y = outy + 0.3f * (sy - outy)
            path.addCircle(x, y, 40 * r, Path.Direction.CW)

            return path
        }

        private fun getAngle(x: Double, y: Double): Double {
            var angle = acos(x / sqrt(x * x + y * y)) * 180 / Math.PI

            if (y < 0) {
                angle = 360 - angle
            }

            return angle
        }

        /**
         * 线性标注1
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getBorderCallout1Path(shape: AutoShape, rect: Rect): Path {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 1.125f
            var x2 = rect.left + rect.width() * (-0.38333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 4) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
            }


            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)

            return path
        }

        /**
         * 线性标注2
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getBorderCallout2Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.125f
            var x3 = rect.left + rect.width() * (-0.46667f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 6) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注3
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getBorderCallout3Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.0f
            var x3 = rect.left + rect.width() * (-0.16667f)
            var y4 = rect.top + rect.height() * 1.12963f
            var x4 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 8) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
                if (values[6] != null) {
                    y4 = rect.top + rect.height() * values[6]!!
                }
                if (values[7] != null) {
                    x4 = rect.left + rect.width() * values[7]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注1(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentCallout1Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 1.125f
            var x2 = rect.left + rect.width() * (-0.38333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 4) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注2(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentCallout2Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.125f
            var x3 = rect.left + rect.width() * (-0.46667f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 6) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )

            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注3(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentCallout3Path(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.0f
            var x3 = rect.left + rect.width() * (-0.16667f)
            var y4 = rect.top + rect.height() * 1.12963f
            var x4 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 8) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
                if (values[6] != null) {
                    y4 = rect.top + rect.height() * values[6]!!
                }
                if (values[7] != null) {
                    x4 = rect.left + rect.width() * values[7]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注1(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getCallout1(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 1.125f
            var x2 = rect.left + rect.width() * (-0.38333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 4) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
            }

            val fill = shape.getBackgroundAndFill()
            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注2(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getCallout2(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.125f
            var x3 = rect.left + rect.width() * (-0.46667f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 6) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )

            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注3(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getCallout3(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.0f
            var x3 = rect.left + rect.width() * (-0.16667f)
            var y4 = rect.top + rect.height() * 1.12963f
            var x4 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 8) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
                if (values[6] != null) {
                    y4 = rect.top + rect.height() * values[6]!!
                }
                if (values[7] != null) {
                    x4 = rect.left + rect.width() * values[7]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.path = path
            extendPath.backgroundAndFill = fill
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注1(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentBorderCallout1(shape: AutoShape, rect: Rect): Path {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 1.125f
            var x2 = rect.left + rect.width() * (-0.38333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 4) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
            }


            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )

            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)

            return path
        }

        /**
         * 线性标注2(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentBorderCallout2(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.125f
            var x3 = rect.left + rect.width() * (-0.46667f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 6) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()

            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 线性标注3(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun getAccentBorderCallout3(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 1.0f
            var x3 = rect.left + rect.width() * (-0.16667f)
            var y4 = rect.top + rect.height() * 1.12963f
            var x4 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 8) {
                if (values[0] != null) {
                    y1 = rect.top + rect.height() * values[0]!!
                }
                if (values[1] != null) {
                    x1 = rect.left + rect.width() * values[1]!!
                }
                if (values[2] != null) {
                    y2 = rect.top + rect.height() * values[2]!!
                }
                if (values[3] != null) {
                    x2 = rect.left + rect.width() * values[3]!!
                }
                if (values[4] != null) {
                    y3 = rect.top + rect.height() * values[4]!!
                }
                if (values[5] != null) {
                    x3 = rect.left + rect.width() * values[5]!!
                }
                if (values[6] != null) {
                    y4 = rect.top + rect.height() * values[6]!!
                }
                if (values[7] != null) {
                    x4 = rect.left + rect.width() * values[7]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, rect.top.toFloat())
            path.lineTo(x1, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注2
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03BorderCallout2Path(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注3
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03BorderCallout3Path(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注4
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03BorderCallout4Path(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * 1.08333f
            var y4 = rect.top + rect.height() * 0.1875f
            var x4 = rect.left + rect.width() * 1.08333f
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
                if (values.size >= 7 && values[6] != null) {
                    x4 = rect.left + rect.width() * values[6]!!
                }
                if (values.size >= 8 && values[7] != null) {
                    y4 = rect.top + rect.height() * values[7]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注1(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentCallout1Path(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f

            var x2 = rect.left + rect.width() * (-0.38333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 4) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            extendPath.setLine(shape.getLine())
            extendPath.path = path
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注2(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentCallout2Path(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x2, rect.top.toFloat())
            path.lineTo(x2, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注3(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentCallout3(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path

            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()

            path.moveTo(x3, rect.top.toFloat())
            path.lineTo(x3, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注4(带强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentCallout4(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * 1.08333f
            var y4 = rect.top + rect.height() * 0.1875f
            var x4 = rect.left + rect.width() * 1.08333f
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
                if (values.size >= 7 && values[6] != null) {
                    x4 = rect.left + rect.width() * values[6]!!
                }
                if (values.size >= 8 && values[7] != null) {
                    y4 = rect.top + rect.height() * values[7]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path

            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()

            path.moveTo(x4, rect.top.toFloat())
            path.lineTo(x4, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)

            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注2(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03Callout2(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path

            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注3(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03Callout3(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path

            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注4(无边框)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03Callout4(shape: AutoShape, rect: Rect): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * 1.08333f
            var y4 = rect.top + rect.height() * 0.1875f
            var x4 = rect.left + rect.width() * 1.08333f
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
                if (values.size >= 7 && values[6] != null) {
                    x4 = rect.left + rect.width() * values[6]!!
                }
                if (values.size >= 8 && values[7] != null) {
                    y4 = rect.top + rect.height() * values[7]!!
                }
            }
            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path

            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注2(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentBorderCallout2(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x2, rect.top.toFloat())
            path.lineTo(x2, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注3(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentBorderCallout3(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.08333f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * (-0.08333f)
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()

            path.moveTo(x3, rect.top.toFloat())
            path.lineTo(x3, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }

        /**
         * 03线性标注4(带边框和强调线)
         * @param canvas
         * @param shape
         * @param rect
         */
        private fun get03AccentBorderCallout4(
            shape: AutoShape,
            rect: Rect
        ): MutableList<ExtendPath?> {
            var y1 = rect.top + rect.height() * 0.1875f
            var x1 = rect.left + rect.width() * (-0.08333f)
            var y2 = rect.top + rect.height() * 0.1875f
            var x2 = rect.left + rect.width() * (-0.16667f)
            var y3 = rect.top + rect.height() * 0.1875f
            var x3 = rect.left + rect.width() * 1.08333f
            var y4 = rect.top + rect.height() * 0.1875f
            var x4 = rect.left + rect.width() * 1.08333f
            val values = shape.getAdjustData()
            if (values != null && values.size >= 1) {
                if (values[0] != null) {
                    x1 = rect.left + rect.width() * values[0]!!
                }
                if (values.size >= 2 && values[1] != null) {
                    y1 = rect.top + rect.height() * values[1]!!
                }
                if (values.size >= 3 && values[2] != null) {
                    x2 = rect.left + rect.width() * values[2]!!
                }
                if (values.size >= 4 && values[3] != null) {
                    y2 = rect.top + rect.height() * values[3]!!
                }
                if (values.size >= 5 && values[4] != null) {
                    x3 = rect.left + rect.width() * values[4]!!
                }
                if (values.size >= 6 && values[5] != null) {
                    y3 = rect.top + rect.height() * values[5]!!
                }
                if (values.size >= 7 && values[6] != null) {
                    x4 = rect.left + rect.width() * values[6]!!
                }
                if (values.size >= 8 && values[7] != null) {
                    y4 = rect.top + rect.height() * values[7]!!
                }
            }

            val fill = shape.getBackgroundAndFill()

            var extendPath = ExtendPath()
            var path = Path()
            path.addRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                Path.Direction.CW
            )
            extendPath.backgroundAndFill = fill
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            extendPath = ExtendPath()
            path = Path()
            path.moveTo(x4, rect.top.toFloat())
            path.lineTo(x4, rect.bottom.toFloat())

            path.moveTo(x1, y1)
            path.lineTo(x2, y2)
            path.lineTo(x3, y3)
            path.lineTo(x4, y4)
            extendPath.path = path
            extendPath.setLine(shape.getLine())
            paths.add(extendPath)

            return paths
        }
    }
}
