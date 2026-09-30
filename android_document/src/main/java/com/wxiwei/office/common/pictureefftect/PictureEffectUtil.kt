/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          PictureEffectMgr.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:16:27
 */
package com.wxiwei.office.common.pictureefftect

/**
 * TODO: ColorMatrix
 * ColorMatrixFilter default value
 * 1,0,0,0,0
 * 0,1,0,0,0
 * 0,0,1,0,0
 * 0,0,0,1,0
 * 
 * brightness
 * brightness([-255,255])
 * 1,0,0,0,N
 * 0,1,0,0,N
 * 0,0,1,0,N
 * 0,0,0,1,0
 * 
 * Color reverse
 * -1,0,0,0,255
 * 0,-1,0,0,255
 * 0,0,-1,0,255
 * 0,0,0,1,0
 * 
 * gray scale
 * 0.3086, 0.6094, 0.0820, 0, 0
 * 0.3086, 0.6094, 0.0820, 0, 0
 * 0.3086, 0.6094, 0.0820, 0, 0
 * 0    , 0    , 0    , 1, 0
 * 
 * saturation([0,>=2])
 * 0.3086*(1-N) + N, 0.6094*(1-N)    , 0.0820*(1-N)    , 0, 0,
 * 0.3086*(1-N)   ,  0.6094*(1-N) + N, 0.0820*(1-N)    , 0, 0,
 * 0.3086*(1-N)   ,  0.6094*(1-N)    , 0.0820*(1-N) + N, 0, 0,
 * 0              , 0                , 0               , 1, 0
 * 
 * contrast([0,10])
 * N,0,0,0,128*(1-N)
 * 0,N,0,0,128*(1-N)
 * 0,0,N,0,128*(1-N)
 * 0,0,0,1,0
 * 
 * Black&White
 * Threshold([0,256])
 * 0.3086*256,0.6094*256,0.0820*256,0,-256*N
 * 0.3086*256,0.6094*256,0.0820*256,0,-256*N
 * 0.3086*256,0.6094*256,0.0820*256,0,-256*N
 * 0, 0, 0, 1, 0
 * 
 * Color rotation, such as:
 * 0,1,0,0,0
 * 0,0,1,0,0
 * 1,0,0,0,0
 * 0,0,0,1,0
 * //---------------
 * 0,0,1,0,0
 * 1,0,0,0,0
 * 0,1,0,0,0
 * 0,0,0,1,0
 * Only display a channel, such as:
 * 1,0,0,0,0
 * 0,0,0,0,0
 * 0,0,0,0,0
 * 0,0,0,1,0
 * just display Red channel
 * 
 * 
 * 
 * http://blog.163.com/mdzhg@126/blog/static/1633215682010423113048711/
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
 * 日期:            2013-1-14
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
object PictureEffectUtil {
    /**
     * no effect applied when brightness is 0
     * @param brightness range:[-255,255]
     * @return
     */
    fun getBrightnessArray(brightness: Int): FloatArray {
        return floatArrayOf(
            1f, 0f, 0f, 0f, brightness.toFloat(),
            0f, 1f, 0f, 0f, brightness.toFloat(),
            0f, 0f, 1f, 0f, brightness.toFloat(),
            0f, 0f, 0f, 1f, 0f
        )
    }

    val reverseColorArray: FloatArray
        get() = floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )

    val grayScaleArray: FloatArray
        /**
         * gray scale
         * @param brightness
         * @return
         */
        get() = floatArrayOf(
            0.3086f, 0.6094f, 0.0820f, 0f, 0f,
            0.3086f, 0.6094f, 0.0820f, 0f, 0f,
            0.3086f, 0.6094f, 0.0820f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

    /**
     * 
     * @param sat ([0,>=2])
     * @return
     */
    fun getSaturationArray(sat: Float): FloatArray {
        return floatArrayOf(
            0.3086f * (1 - sat) + sat, 0.6094f * (1 - sat), 0.0820f * (1 - sat), 0f, 0f,
            0.3086f * (1 - sat), 0.6094f * (1 - sat) + sat, 0.0820f * (1 - sat), 0f, 0f,
            0.3086f * (1 - sat), 0.6094f * (1 - sat), 0.0820f * (1 - sat) + sat, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    }

    /**
     * 
     * @param contrast ([0,10]) no effect applied when contrast is 1
     * @return
     */
    fun getContrastArray(contrast: Float): FloatArray {
        return floatArrayOf(
            contrast, 0f, 0f, 0f, 128 * (1 - contrast),
            0f, contrast, 0f, 0f, 128 * (1 - contrast),
            0f, 0f, contrast, 0f, 128 * (1 - contrast),
            0f, 0f, 0f, 1f, 0f
        )
    }

    fun getBrightAndContrastArray(bright: Float, contrast: Float): FloatArray {
        return floatArrayOf(
            contrast, 0f, 0f, 0f, bright,
            0f, contrast, 0f, 0f, bright,
            0f, 0f, contrast, 0f, bright,
            0f, 0f, 0f, 1f, 0f
        )
    }

    /**
     * 
     * @param threshode [0,255]
     * @return
     */
    fun getBlackWhiteArray(threshode: Float): FloatArray {
        return floatArrayOf(
            0.3086f * 256, 0.6094f * 256, 0.0820f * 256, 0f, -256 * threshode,
            0.3086f * 256, 0.6094f * 256, 0.0820f * 256, 0f, -256 * threshode,
            0.3086f * 256, 0.6094f * 256, 0.0820f * 256, 0f, -256 * threshode,
            0f, 0f, 0f, 1f, 0f
        )
    }
}
