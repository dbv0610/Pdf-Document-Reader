/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
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
