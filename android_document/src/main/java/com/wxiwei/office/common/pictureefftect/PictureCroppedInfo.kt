/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          PictureCroppedInfor.java
 *  
 * 编译器:            android2.2
 * 时间:              下午5:36:40
 */
package com.wxiwei.office.common.pictureefftect

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
 * 日期:            2013-1-15
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

/**
 * picture cropped information
 * @author jqin
 */
class PictureCroppedInfo
    (leftOff: Float, topOff: Float, rightOff: Float, bottomOff: Float) {
    var leftOff: Float
    var topOff: Float
    var rightOff: Float
    var bottomOff: Float

    init {
        this.leftOff = leftOff
        this.topOff = topOff
        this.rightOff = rightOff
        this.bottomOff = bottomOff
    }
}
