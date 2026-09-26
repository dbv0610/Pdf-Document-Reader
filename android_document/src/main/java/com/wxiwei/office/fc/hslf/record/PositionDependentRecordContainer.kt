package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

abstract class PositionDependentRecordContainer : RecordContainer(), PositionDependentRecord {
    var sheetId: Int = 0

    protected var myLastOnDiskOffset: Int = 0

    override var lastOnDiskOffset: Int
        get() = myLastOnDiskOffset
        set(offset) {
            myLastOnDiskOffset = offset
        }

    override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
        return
    }
}
