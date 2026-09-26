package com.wxiwei.office.common.bg

import android.graphics.Rect
import android.graphics.Shader
import com.wxiwei.office.system.IControl

abstract class AShader {
    open fun createShader(control: IControl?, viewIndex: Int, rect: Rect?): Shader? {
        return shader
    }

    /**
     * 
     * 
     */
    fun dispose() {
        shader = null
    }

    var alpha: Int = 255
    var shader: Shader? = null
        protected set
}
