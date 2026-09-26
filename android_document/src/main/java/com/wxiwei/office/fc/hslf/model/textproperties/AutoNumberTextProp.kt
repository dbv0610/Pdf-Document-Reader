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
