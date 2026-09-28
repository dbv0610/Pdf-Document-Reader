/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           AutoNumberTextProp.java
 *  
 * 编译器:             android2.2
 * 时间:               下午1:33:12
 */
package com.wxiwei.office.fc.hslf.model.textproperties

/**
 * a kind of auto number data
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-7-19
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class AutoNumberTextProp {
    /**
     * 
     */
    constructor()

    /**
     * 
     * @param numberingType
     * @param start
     */
    constructor(numberingType: Int, start: Int) {
        this.numberingType = numberingType
        this.start = start
    }

    /**
     * 
     */
    fun dispose() {
    }

    /**
     * 
     * @return
     */
    /**
     * 
     * @param numberingType
     */
    //
    var numberingType: Int = -1
    /**
     * 
     * @return
     */
    /**
     * 
     * @param start
     */
    //
    var start: Int = 0
}
