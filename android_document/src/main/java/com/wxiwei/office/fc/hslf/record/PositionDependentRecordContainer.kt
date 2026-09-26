package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

abstract class PositionDependentRecordContainer : RecordContainer(), PositionDependentRecord {
    var sheetId: Int = 0

    protected var myLastOnDiskOffset: Int = 0

    override fun getLastOnDiskOffset(): Int {
        return myLastOnDiskOffset
    }

    override fun setLastOnDiskOffset(offset: Int) {
        myLastOnDiskOffset = offset
    }

    override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int, Int>?) {
        return
    }
}
