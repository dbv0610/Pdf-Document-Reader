/*
 * 文件名称:          BinaryTagDataBlob.java
 *  
 * 编译器:            android2.2
 * 时间:              上午11:26:07
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: An atom record that contains the value of the name-value pair
 * in a programmable tag.
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
 * 日期:            2013-1-8
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
class BinaryTagDataBlob protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    private val _header: ByteArray?

    /**
     * We are of type 0xF144
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
        var RECORD_ID: Long = 0x138B
    }
}

