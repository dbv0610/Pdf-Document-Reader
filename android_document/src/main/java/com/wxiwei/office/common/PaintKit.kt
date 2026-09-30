/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          PaintKit.java
 *  
 * 编译器:            android2.2
 * 时间:              下午1:42:18
 */
package com.wxiwei.office.common

import android.graphics.Paint
import android.graphics.Typeface
import com.wxiwei.office.constant.SSConstant

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
 * 日期:            2011-12-7
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
class PaintKit
private constructor() {
    private val paint: Paint = Paint()

    /**
     * 
     */
    init {
        paint.setTextSize(SSConstant.HEADER_TEXT_FONTSZIE.toFloat())
        paint.setTypeface(Typeface.SERIF)
        paint.setFlags(Paint.ANTI_ALIAS_FLAG)
        paint.setStrokeCap(Paint.Cap.ROUND)
    }

    fun getPaint(): Paint {
        paint.reset()
        paint.setAntiAlias(true)

        return paint
    }

    companion object {
        private val pk = PaintKit()

        /**
         * 
         * @return
         */
        fun instance(): PaintKit {
            return pk
        }
    }
}
