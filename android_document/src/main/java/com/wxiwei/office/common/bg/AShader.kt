/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
