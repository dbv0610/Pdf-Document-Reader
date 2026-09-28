/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TimeColorBehaviorAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:10:06
 */
package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

/**
 * TODO: An atom record that specifies the information for an animation that changes the color of an object
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
class TimeColorBehaviorAtom(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    //A TimeColorBehaviorPropertyUsedFlag structure that specifies which fields are valid.
    private val flag = 0

    /**
     * We are of type 0x2AFB
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Set things up, and find our more interesting children
     */
    init {
        var len = len
        if (len < 60) {
            len = 60
        }
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)
    }

    /**
     * At write-out time, update the references to PersistPtrs and
     * other UserEditAtoms to point to their new positions
     */
    public override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    companion object {
        private const val _type: Long = 0xF135
    }
}
