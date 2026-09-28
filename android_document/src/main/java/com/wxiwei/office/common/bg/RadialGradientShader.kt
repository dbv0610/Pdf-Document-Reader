/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.common.bg

import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Shader.TileMode
import com.wxiwei.office.system.IControl
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

class RadialGradientShader(
    private val positionType: Int,
    colors: IntArray?,
    positions: FloatArray?
) : Gradient(colors, positions) {
    override val gradientType: Int
        get() = BackgroundAndFill.Companion.FILL_SHADE_RADIAL.toInt()

    override fun createShader(control: IControl?, viewIndex: Int, rect: Rect?): Shader? {
        val colors = colors ?: return null
        val coordinate = this.circleCoordinate
        if (positionType == Center_Center && focus == 0) {
            val size = colors.size
            var nTem = 0
            for (i in 0..<size / 2) {
                nTem = colors[i]
                colors[i] = colors[size - 1 - i]
                colors[size - 1 - i] = nTem
            }
        }
        shader = RadialGradient(
            coordinate[0].toFloat(), coordinate[1].toFloat(), coordinate[2].toFloat(),
            colors, positions, TileMode.REPEAT
        )
        return shader
    }

    private val circleCoordinate: IntArray
        /**
         * 0:The x-coordinate of the center of the radius
         * 1:The y-coordinate of the center of the radius
         * 2:Must be positive. The radius of the circle for this gradient
         * @param angle
         * @return
         */
        get() {
            val radius = ceil(
                sqrt(
                    Gradient.Companion.COORDINATE_LENGTH.toDouble()
                        .pow(2.0) * 2
                )
            ).toInt()
            when (positionType) {
                Center_TR -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH,
                    0,
                    radius
                )

                Center_BL -> return intArrayOf(
                    0,
                    Gradient.Companion.COORDINATE_LENGTH,
                    radius
                )

                Center_BR -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH,
                    Gradient.Companion.COORDINATE_LENGTH,
                    radius
                )

                Center_Center -> return intArrayOf(
                    Gradient.Companion.COORDINATE_LENGTH / 2,
                    Gradient.Companion.COORDINATE_LENGTH / 2,
                    radius / 2
                )

                else -> return intArrayOf(0, 0, radius)
            }
        }

    companion object {
        //the center of the radius is in the top and left corner of rect
        const val Center_TL: Int = 0
        //the center of the radius is in the top and right corner of rect
        const val Center_TR: Int = 1
        //the center of the radius is in the bottom and left corner of rect
        const val Center_BL: Int = 2
        //the center of the radius is in the bottom and right corner of rect
        const val Center_BR: Int = 3
        //the center of the radius is in the center of rect
        const val Center_Center: Int = 4
    }
}
