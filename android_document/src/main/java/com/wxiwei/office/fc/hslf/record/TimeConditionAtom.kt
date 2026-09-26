/*
 * 文件名称:          TimeConditionAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:49:07
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

/**
 * TODO: An atom record that specifies the information used to evaluate when a condition will be true.
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
class TimeConditionAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    //the type of target that participates in the evaluation of the condition
    private val triggerObject: Int

    /**
     * event that causes the condition to be TRUE.
     * It MUST be a value from the following table.
     * 0x00000000 None.
     * 0x00000001 OnBegin event that occurs on the specified target.
     * 0x00000003 Start of the time node that is specified by id.
     * 0x00000004 End of the time node that is specified by id.
     * 0x00000005 Mouse click.
     * 0x00000007 Mouse over.
     * 0x00000009 OnNext event that occurs on the specified target.
     * 0x0000000A OnPrev event that occurs on the specified target.
     * 0x0000000B Stop audio event that occurs when an "onstopaudio" command is fired.
     */
    private val triggerEvent: Int

    /**
     * the target that participates in the evaluation of the condition
     * When triggerObject is TOT_TimeNode, this field specifies the time node identifier.
     * When triggerObject is TOT_RuntimeNodeRef, this field MUST be 0x00000002
     * that specifies that all child time node of the ExtTimeNodeContainer record
     * or SlaveContainer record that contains this record are the target.
     */
    private val id: Int

    //the offset time, in milliseconds, that sets when the condition will become TRUE.
    private val delay: Int

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

        triggerObject = getInt(source, start + 8)
        triggerEvent = getInt(source, start + 12)
        id = getInt(source, start + 16)
        delay = getInt(source, start + 20)
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
        //the type of a target that participates in the evaluation of a time condition
        const val TOT_None: Int = 0 //None.
        const val TOT_VisualElement: Int = 1 //An animatable object.
        const val TOT_TimeNode: Int = 2 //A time node.
        const val TOT_RuntimeNodeRef: Int = 3 //Runtime child time nodes.

        private const val _type: Long = 0xF128
    }
}
