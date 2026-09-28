/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          SlaveContainer.java
 *  
 * 编译器:            android2.2
 * 时间:              下午8:13:03
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: A container record that specifies a subordinate time node whose start time
 * depends on the relation to its master time node.At most one of the following fields
 * MUST exist: timeColorBehavior, timeSetBehavior, or timeCommandBehavior.
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
 * 日期:            2013-1-7
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
class SlaveContainer protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    private val _header: ByteArray?

    /**
     * We are of type 0xF144
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Set things up, and find our more interesting children
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
    }

    companion object {
        private const val _type: Long = 0xF145
    }
}

