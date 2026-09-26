package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

interface PositionDependentRecord {
    var lastOnDiskOffset: Int
    fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?)
    fun dispose()
}
