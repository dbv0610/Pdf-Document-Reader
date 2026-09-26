package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

abstract class PositionDependentRecordAtom : RecordAtom(), PositionDependentRecord {
    protected var myLastOnDiskOffset: Int = 0

    override fun getLastOnDiskOffset(): Int {
        return myLastOnDiskOffset
    }

    override fun setLastOnDiskOffset(offset: Int) {
        myLastOnDiskOffset = offset
    }

    override abstract fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int, Int>?)

    override fun dispose() {
    }
}
