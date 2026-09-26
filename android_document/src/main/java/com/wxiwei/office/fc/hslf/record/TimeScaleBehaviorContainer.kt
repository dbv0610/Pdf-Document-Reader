/*
 * 文件名称:          TimeScaleBehaviorContainer.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:34:27
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: a scale-animation behavior that changes the size of an object
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
class TimeScaleBehaviorContainer protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    private val _header: ByteArray?

    /**
     * We are of type 0xF13D
     */
    public override fun getRecordType(): Long {
        return RECORD_ID
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
        var RECORD_ID: Long = 0xF130
    }
}
