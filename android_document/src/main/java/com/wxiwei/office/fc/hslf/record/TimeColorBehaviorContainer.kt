/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TimeColorBehaviorContainer.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:05:12
 */
package com.wxiwei.office.fc.hslf.record

/**
 * TODO: A container record that specifies a behavior that changes the color of an object.
 * This animation behavior is applied to the object specified
 * by the behavior.clientVisualElement field and used to animate one property specified
 * by the behavior.stringList field. The property MUST be one from the following list
 * that is a subset of the properties specified in the TimeStringListContainer
 * record: "ppt_c", "style.color", "imageData.chromakey", "fill.color", "fill.color2",
 * "stroke.color", "stroke.color2", "shadow.color", "shadow.color2", "extrusion.color",
 * and "fillcolor".
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
class TimeColorBehaviorContainer protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    private val _header: ByteArray?

    //how to change the color of the object and which attributes within this field are valid
    private val colorBehaviorAtom: TimeColorBehaviorAtom?

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

        colorBehaviorAtom = TimeColorBehaviorAtom(source, start + 8, 60)
        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
    }

    companion object {
        var RECORD_ID: Long = 0xF12C
    }
}
