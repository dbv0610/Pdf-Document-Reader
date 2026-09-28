/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TimeIterateDataAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              下午7:42:31
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

/**
 * TODO: An atom record that specifies how an animation is applied to sub-elements
 * of the target object for a repeated effect. It can be applied to the letters,
 * words, or shapes within a target object
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
class TimeIterateDataAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    /**
     * the interval time of iterations, which can be either absolute time
     * or a percentage as specified in iterateIntervalType. It MUST be ignored
     * if fIterateIntervalPropertyUsed is FALSE and a value of 0x00000000
     * MUST be used instead
     */
    private val iterateInterval: Int

    /**
     * the type of iteration behavior. It MUST be ignored if fIterateTypePropertyUsed
     * is FALSE and a value of 0x00000000 MUST be used instead. It MUST be a value
     * from the following table.
     * 0x00000000 All at once: all sub-elements animate together with no interval time.
     * 0x00000001 By word: sub-elements are words.
     * 0x00000002 By letter: sub-elements are letters.
     */
    private val iterateType: Int

    /**
     * the direction of the iteration behavior. It MUST be ignored if fIterateDirectionPropertyUsed
     * is FALSE and a value of 0x00000001 MUST be used instead. It MUST be a value from the
     * following table
     * 0x00000000 Backwards: from the last sub-element to the first sub-element
     * 0x00000001 Forwards: from the first sub-element to the last sub-element
     */
    private val iterateDirection: Int

    /**
     * the type of interval time as specified in iterateInterval. It MUST be ignored
     * if fIterateIntervalTypePropertyUsed is FALSE and a value of 0x00000000 MUST be
     * used instead. It MUST be a value from the following table.
     * 0x00000000 Seconds: iterateInterval is absolute time in milliseconds.
     * 0x00000001 Percentage: iterateInterval is a percentage of animation duration, in tenths of a percent.
     */
    private val iterateIntervalType: Int

    private val fIterateDirectionPropertyUsed = false
    private val fIterateTypePropertyUsed = false
    private val fIterateIntervalPropertyUsed = false
    private val fIterateIntervalTypePropertyUsed = false

    private val reserved: ByteArray? = null

    /**
     * We are of type 1000
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Set things up, and find our more interesting children
     */
    init {
        var len = len
        if (len < 28) {
            len = 28
        }
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        iterateInterval = getInt(source, start + 8)
        iterateType = getInt(source, start + 12)
        iterateDirection = getInt(source, start + 16)
        iterateIntervalType = getInt(source, start + 20)
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
        private const val _type: Long = 0xF140
    }
}
