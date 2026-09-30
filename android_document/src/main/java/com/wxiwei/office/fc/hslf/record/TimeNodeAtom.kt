/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TimeNodeAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              上午11:18:30
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

/**
 * TODO: An atom record that specifies the attributes of a time node.
 * Let the corresponding time node be specified by the TimeNodeContainer
 * record or the SlaveContainer record that contains this TimeNodeAtom record.
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
 * 日期:            2013-1-6
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
class TimeNodeAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?
    private val reserved1: Int

    /**
     * how the corresponding time node restarts when it completes its action.
     * It MUST be ignored if fRestartProperty is FALSE
     * and a value of 0x00000000 MUST be used instead.It MUST be a value from the following table:
     * 0x00000000 Does not restart.
     * 0x00000001 Can restart at any time.
     * 0x00000002 Can restart when the corresponding time node is not active.
     * 0x00000003 Same as 0x00000000.
     */
    private val restart: Int

    /**
     * the type of the corresponding time node.
     * It MUST be ignored if fGroupingTypeProperty is FALSE
     * and a value of TL_TNT_Parallel MUST be used instead.
     */
    private val timeNodeType: Int

    /**
     * the state of the target object's properties when the animation ends.
     * It MUST be ignored if fFillProperty is FALSE
     * and a value of 0x00000000 MUST be used instead.It MUST be a value from the following table:
     * 0x00000000 The properties remain at their ending values while the parent time node is still running or holding. After which, the properties reset to their original values.
     * 0x00000001 The properties reset to their original values after the time node becomes inactive.
     * 0x00000002  The properties remain at their ending values while the parent time node is still running or holding, or until another sibling time node is started under a sequential time node as specified in the type field. After which, the properties reset to their original values.
     * 0x00000003  Same as 0x00000000.
     * 0x00000004  Same as 0x00000001.
     */
    private val fill: Int

    private val reserved2 = 0
    private val reserved3: Byte = 0
    private val unused = 0

    //the duration of the corresponding time node in milliseconds.
    //It MUST be ignored if fDurationProperty is FALSE 
    //and a value of 0x00000000 MUST be used instead.
    private val duration: Int

    //whether fill was explicitly set by a user interface action
    private val fFillProperty: Boolean

    //whether restart was explicitly set by a user interface action
    private val fRestartProperty: Boolean

    //
    private val reserved4 = false

    //whether type was explicitly set by a user interface action
    private val fGroupingTypeProperty: Boolean

    //whether duration was explicitly set by a user interface action
    private val fDurationProperty: Boolean

    private val reserved5: ByteArray? = null

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
        if (len < 40) {
            len = 40
        }
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        reserved1 = getInt(source, start + 8)
        restart = getInt(source, start + 12)

        timeNodeType = getInt(source, start + 16)
        fill = getInt(source, start + 20)

        duration = getInt(source, start + 32)

        val b = source[start + 36]
        fDurationProperty = ((b.toInt() and 0x10)) shr 4 > 0
        fGroupingTypeProperty = ((b.toInt() and 0x8) shr 3) > 0

        fRestartProperty = ((b.toInt() and 0x2) shr 1) > 0
        fFillProperty = ((b.toInt() and 0x1)) > 0
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
        /**
         * time node type
         */
        //Parallel time node whose child nodes can start simultaneously
        const val TNT_Parallel: Int = 0

        /**
         * Sequential time node whose child nodes can only start sequentially
         * and each child can only start after its previous sibling has started
         */
        const val TNT_Sequential: Int = 1

        //Behavior time node that contains a behavior
        const val TNT_Behavior: Int = 2

        //Media time node that contains a media object
        const val TNT__Media: Int = 3


        private const val _type: Long = 0xF127
    }
}
