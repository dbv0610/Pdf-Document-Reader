package com.wxiwei.office.common.bg

import android.graphics.LinearGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Shader.TileMode
import com.wxiwei.office.system.IControl

class LinearGradientShader(private val angle: Float, colors: IntArray?, positions: FloatArray?) :
    Gradient(colors, positions) {
    override val gradientType: Int
        get() = BackgroundAndFill.Companion.FILL_SHADE_LINEAR.toInt()

    fun getAngle(): Int {
        return angle.toInt()
    }

    override fun createShader(control: IControl?, viewIndex: Int, rect: Rect?): Shader? {
        try {
            val colors = colors ?: return null
            val coordinate = this.linearGradientCoordinate
            shader = LinearGradient(
                coordinate[0].toFloat(),
                coordinate[1].toFloat(),
                coordinate[2].toFloat(),
                coordinate[3].toFloat(),
                colors,
                positions,
                TileMode.MIRROR
            )
            return shader
        } catch (e: Exception) {
            return null
        }
    }

    private val linearGradientCoordinate: IntArray
        /**
         * 0:The x-coordinate for the start of the gradient line
         * 1:The y-coordinate for the start of the gradient line
         * 2:x1 The x-coordinate for the end of the gradient line
         * 3:y1 The y-coordinate for the end of the gradient line
         * @param angle
         * @return
         */
        get() {
            when (Math.round((angle + 22) % 360 / 45)) {
                0 -> return intArrayOf(
                    0,
                    0,
                    Gradient.Companion.COORDINATE_LENGTH,
                    0
                )

                1 -> return intArrayOf(
                    0,
                    0,
                    Gradient.Companion.COORDINATE_LENGTH,
                    Gradient.Companion.COORDINATE_LENGTH
                )

                2 -> return intArrayOf(
                    0,
                    0,
                    0,
                    Gradient.Companion.COORDINATE_LENGTH
                )

                3 -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH,
                    0,
                    0,
                    Gradient.Companion.COORDINATE_LENGTH
                )

                4 -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH,
                    0,
                    0,
                    0
                )

                5 -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH,
                    Gradient.Companion.COORDINATE_LENGTH,
                    0,
                    0
                )

                6 -> return intArrayOf(
                    0,
                    Gradient.Companion.COORDINATE_LENGTH,
                    0,
                    0
                )

                else -> return intArrayOf(
                    0,
                    Gradient.Companion.COORDINATE_LENGTH,
                    Gradient.Companion.COORDINATE_LENGTH,
                    0
                )
            }
        }
}
