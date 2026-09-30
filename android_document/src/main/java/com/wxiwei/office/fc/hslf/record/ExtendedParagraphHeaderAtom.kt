/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           ExtendedParagraphHeaderAtom.java
 *  
 * 编译器:             android2.2
 * 时间:               下午5:33:25
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt

/**
 * get extended paragraph property
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
class ExtendedParagraphHeaderAtom(source: ByteArray, start: Int, len: Int) : RecordAtom() {
    /**
     * We are of type 4015
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    //
    private var _header: ByteArray?

    /**
     * 
     * @return
     */
    //
    var refSlideID: Int = 0
        private set

    /**
     * 
     * @return
     */
    //
    var textType: Int = 0
        private set

    /**
     * @param source
     * @param start
     * @param len
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 8) {
            len = 8
        }
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        if (len >= 16) {
            refSlideID = getInt(source, start + 8)
            textType = getInt(source, start + 12)
        }
    }

    companion object {
        //
        private const val _type: Long = 4015
    }
}
