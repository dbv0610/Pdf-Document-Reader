/*
 * 文件名称:          NumberFormat.java
 *
 * 编译器:            android2.2
 * 时间:              上午11:26:49
 */
package com.wxiwei.office.ss.model.style

/**
 * TODO: 文件注释
 *
 * Read版本:        Read V1.0
 * 作者:            jqin
 * 日期:            2012-2-22
 * 负责人:           jqin
 */
class NumberFormat {
    //Number Format Id
    private var numFmtId: Short
    //Number Format Code
    private var formatCode: String?

    constructor() {
        numFmtId = 0
        formatCode = "General"
    }

    constructor(numFmtId: Short, formatCode: String?) {
        this.numFmtId = numFmtId
        this.formatCode = formatCode
    }

    fun setNumberFormatID(id: Short) {
        numFmtId = id
    }

    /**
     * get Number Format Id
     * @return
     */
    fun getNumberFormatID(): Short = numFmtId

    /**
     *
     * @param formatCode
     */
    fun setFormatCode(formatCode: String?) {
        this.formatCode = formatCode
    }

    /**
     * get Number Format Code
     * @return
     */
    fun getFormatCode(): String? = formatCode

    /**
     *
     */
    fun dispose() {
        formatCode = null
    }
}
