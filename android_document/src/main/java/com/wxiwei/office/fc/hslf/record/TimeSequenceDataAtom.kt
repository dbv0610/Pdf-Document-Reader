/*
 * 文件名称:          TimeSequenceDataAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              下午8:00:26
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

/**
 * TODO: sequencing information for the child nodes of a time node.
 * Each child can only be activated after its prior sibling has been activated.
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
class TimeSequenceDataAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    /**
     * the concurrency behavior of the child nodes of the corresponding time node.
     * It MUST be ignored if fConcurrencyPropertyUsed is FALSE
     * and a value of 0x00000000 MUST be used instead.
     * It MUST be a value from the following table.
     * 0x00000000 No concurrency: the next child is activated only after the current
     * child ends and conditions in the corresponding next time condition
     * list are met.
     * 0x00000001 Concurrency enabled: the next child can be activated after the current
     * child is activated and conditions in the corresponding next time
     * condition list are met.
     */
    private val concurrency: Int

    /**
     * actions when traversing forward in the sequence of child nodes of the corresponding
     * time node. It MUST be ignored if fNextActionPropertyUsed is FALSE and a value
     * of 0x00000000 MUST be used instead. It MUST be a value from the following table.
     * 0x00000000 Take no action.
     * 0x00000001 Traverse forward the current child node along the timeline to a natural
     * end time.The natural end time of a child node is the time when the child
     * node will end without interventions. If the end time is infinite,
     * the child node will never stop. The natural end time of the child node
     * is specified as the latest non-infinite end time of its child nodes.
     */
    private val nextAction: Int

    /**
     * actions when traversing backward in the sequence of child nodes of the corresponding
     * time node. It MUST be ignored if fPreviousActionPropertyUsed is FALSE and a value
     * of 0x0000000 MUST be used instead. It MUST be a value from the following table.
     * 0x00000000 Take no action.
     * 0x00000001 Continue backwards in the sequence until reaching a child that starts
     * only on the next time condition as specified in the corresponding next
     * time condition list.
     */
    private val previousAction: Int

    private val reserved1: Int

    private val fConcurrencyPropertyUsed = false
    private val fNextActionPropertyUsed = false
    private val fPreviousActionPropertyUsed = false

    private val reserved2: ByteArray? = null

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

        concurrency = getInt(source, start + 8)
        nextAction = getInt(source, start + 12)
        previousAction = getInt(source, start + 16)
        reserved1 = getInt(source, start + 20)
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
        private const val _type: Long = 0xF141
    }
}
