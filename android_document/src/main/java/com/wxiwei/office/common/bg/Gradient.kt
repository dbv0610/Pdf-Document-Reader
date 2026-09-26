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
