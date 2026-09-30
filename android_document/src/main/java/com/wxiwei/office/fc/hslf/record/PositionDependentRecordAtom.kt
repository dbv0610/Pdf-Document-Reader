/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

abstract class PositionDependentRecordAtom : RecordAtom(), PositionDependentRecord {
    protected var myLastOnDiskOffset: Int = 0

    override var lastOnDiskOffset: Int
        get() = myLastOnDiskOffset
        set(offset) {
            myLastOnDiskOffset = offset
        }

    override abstract fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?)

    override fun dispose() {
    }
}
