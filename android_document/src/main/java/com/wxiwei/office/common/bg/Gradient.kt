/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.common.bg


abstract class Gradient(colors: IntArray?, positions: FloatArray?) : AShader() {
    open val gradientType: Int = 0
    protected var colors: IntArray? = null
    protected var positions: FloatArray? = null

    //percent
    @JvmField
    var focus: Int = 100

    init {
        if (colors != null && colors.size >= 2) {
            this.colors = colors
        }

        this.positions = positions
    }

    companion object {
        const val COORDINATE_LENGTH: Int = 100
    }
}
