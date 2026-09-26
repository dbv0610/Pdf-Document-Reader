/*
 * 文件名称:          TimeConditionContainer.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:44:29
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: A container record that specifies a time condition of a time node.
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
class TimeConditionContainer protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    private val _header: ByteArray?

    /**
     * We are of type 0xF13D
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
        private const val _type: Long = 0xF125
    }
}
