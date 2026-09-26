/*
 * 文件名称:           EscherBinaryTagRecord.java
 *  
 * 编译器:             android2.2
 * 时间:               下午3:55:44
 */
package com.wxiwei.office.fc.ddf

/**
 * holds data of extended pagraph style(bullets and number ruler)
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
 * 日期:           2012-7-18
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
class EscherBinaryTagRecord  //
    : EscherTextboxRecord() {
    /**
     * 
     */
    override val recordName: String
        get() {
        return "BinaryTagData"
    }

    companion object {
        //
        @JvmField
        val RECORD_ID: Short = 0x138B.toShort()
    }
}
