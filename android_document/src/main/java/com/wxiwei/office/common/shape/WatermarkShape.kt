/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          	WatermarkShape.java
 *  
 * 编译器:            android2.2
 * 时间:             	下午5:06:47
 */
package com.wxiwei.office.common.shape

import com.wxiwei.office.common.pictureefftect.PictureEffectInfo

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        	Office engine V1.0
 * 
 * 
 * 作者:            	ljj8494
 * 
 * 
 * 日期:            	2013-4-24
 * 
 * 
 * 负责人:          	ljj8494
 * 
 * 
 * 负责小组:        	TMC
 * 
 * 
 * 
 * 
 */
class WatermarkShape : WPAutoShape() {
    private val OPACITY = 0.2f

    /**
     * 
     * 
     */
    override val type: Short
        get() {
        if (watermarkType == Watermark_Text) {
            return AbstractShape.Companion.SHAPE_AUTOSHAPE
        } else {
            return AbstractShape.Companion.SHAPE_PICTURE
        }
    }

    /**
     * 
     */
    override val isWatermarkShape: Boolean
        get() = true

    fun getOpacity(): Float {
        return opacity
    }

    fun setOpacity(opacity: Float) {
        this.opacity = OPACITY * opacity
    }

    val effectInfor: PictureEffectInfo?
        get() {
            if (watermarkType == Watermark_Picture) {
                if (effect == null) {
                    effect = PictureEffectInfo()
                    effect!!.alpha = Math.round(255 * opacity)
                    effect!!.brightness = Math.round(255 * blacklevel).toFloat()
                }

                return effect
            }

            return null
        }

    /**
     * 
     */
    override fun dispose() {
        watermartString = null
        if (effect != null) {
            effect!!.dispose()
            effect = null
        }
    }

    var watermarkType: Byte = 0

    /**
     * 
     * @return
     */
    /**
     * 
     * @param watermartString
     */
    //for text watermark
    var watermartString: String? = null
    var isAutoFontSize: Boolean = false
    var fontSize: Int = 36
    var fontColor: Int = -0x1000000

    //for picture watermark
    var pictureIndex: Int = -1
    var blacklevel: Float = 0f
    var gain: Float = 0f
    private var effect: PictureEffectInfo? = null

    private var opacity = OPACITY

    companion object {
        const val Watermark_Text: Byte = 0
        const val Watermark_Picture: Byte = 1
    }
}
