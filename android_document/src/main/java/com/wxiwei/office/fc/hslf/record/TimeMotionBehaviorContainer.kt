/*
 * 文件名称:          TimeMotionBehaviorContainer.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:27:32
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: A container record that specifies a motion behavior that moves an object
 * along a path. This animation behavior is applied to the object specified
 * by the timeBehavior.clientVisualElement field and used to animate two
 * properties specified by the timeBehavior.stringList field. The properties MUST
 * be ones from the list that is specified in the TimeStringListContainer record.
 * If no properties are specified, "ppt_x" and "ppt_y" will be used. If only one
 * property is specified, "ppt_y" will be used as the second property.
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
class TimeMotionBehaviorContainer protected constructor(source: ByteArray, start: Int, len: Int) :
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
        var RECORD_ID: Long = 0xF12E
    }
}
