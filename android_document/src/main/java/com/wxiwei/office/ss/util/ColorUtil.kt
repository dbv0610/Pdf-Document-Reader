/*
 * 文件名称:          ColorUtil.java
 *
 * 编译器:            android2.2
 * 时间:              上午9:36:23
 */
package com.wxiwei.office.ss.util

import android.graphics.Color

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-3-19
 * 负责人:           jqin
 */
class ColorUtil {
    /**
     * Standard Red Green Blue ctColor value (RGB) with applied tint.
     * Alpha values are ignored.
     */
    fun getColorWithTint(color: Int, tint: Double): Int {
        val r = applyTint(Color.red(color) and 0xFF, tint)
        val g = applyTint(Color.green(color) and 0xFF, tint)
        val b = applyTint(Color.blue(color) and 0xFF, tint)

        return Color.rgb(r, g, b)
    }

    companion object {
        private val util = ColorUtil()

        //
        @JvmStatic
        fun instance(): ColorUtil {
            return util
        }

        /**
         * Return a color-int from red, green, blue components.
         * The alpha component is implicity 255 (fully opaque).
         * These component values should be [0..255], but there is no
         * range check performed, so if they are out of range, the
         * returned color is undefined.
         * @param red  Red component [0..255] of the color
         * @param green Green component [0..255] of the color
         * @param blue  Blue component [0..255] of the color
         */
        @JvmStatic
        fun rgb(red: Int, green: Int, blue: Int): Int {
            return (0xFF shl 24) or (red shl 16 and 0xFF0000) or (green shl 8 and 0xFF00) or (blue and 0xFF)
        }

        @JvmStatic
        fun rgb(red: Byte, green: Byte, blue: Byte): Int {
            return (0xFF shl 24) or (red.toInt() shl 16 and 0xFF0000) or (green.toInt() shl 8 and 0xFF00) or (blue.toInt() and 0xFF)
        }

        /**
         * http://poi.apache.org/apidocs/org/apache/poi/xssf/usermodel/XSSFColor.html
         * @param lum
         * @param tint
         * @return
         */
        private fun applyTint(lum: Int, tint: Double): Int {
            var lum = lum
            if (tint > 0) {
                lum = (lum + (255 - lum) * tint).toInt()
            } else if (tint < 0) {
                lum = (lum * (1 + tint)).toInt()
            }

            lum = if (lum > 255) 255 else lum

            return lum
        }
    }
}
