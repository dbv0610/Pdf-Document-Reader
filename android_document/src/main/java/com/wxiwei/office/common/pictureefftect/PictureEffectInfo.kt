/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          PictureEffect.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:53:12
 */
package com.wxiwei.office.common.pictureefftect


/**
 * TODO: picture effect, such as trim, contrast, brightness, gray scale
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
class PictureEffectInfo {
    var isGrayScale: Boolean?
        get() = grayscl
        set(grayscl) {
            this.grayscl = grayscl
        }

    var blackWhiteThreshold: Float?
        get() = threshold
        set(threshold) {
            this.threshold = threshold
        }

    var saturation: Float?
        get() = sat
        set(sat) {
            this.sat = sat
        }

    var brightness: Float?
        get() = bright
        set(bright) {
            this.bright = bright
        }

    fun setContrast(contrast: Float) {
        this.contrast = contrast
    }

    fun getContrast(): Float? {
        return contrast
    }

    fun dispose() {
        this.pictureCroppedInfor = null
        grayscl = null
        threshold = null
        sat = null
        bright = null
        contrast = null
        alpha = null
    }

    //the interesting rect after image cropped
    var pictureCroppedInfor: PictureCroppedInfo? = null

    //stretch
    var pictureStretchInfo: PictureStretchInfo? = null

    //whether apply gray scale effct
    private var grayscl: Boolean? = null

    //black&white threshold
    private var threshold: Float? = null

    //saturation([0,>=2])
    private var sat: Float? = null

    //[-255,255]
    private var bright: Float? = null

    //[0, 10]
    private var contrast: Float? = null

    var transparentColor: Int? = null

    var alpha: Int? = null
}
