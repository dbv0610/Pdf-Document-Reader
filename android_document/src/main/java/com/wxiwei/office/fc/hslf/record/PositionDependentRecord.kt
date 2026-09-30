/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.hslf.record

import java.util.Hashtable

interface PositionDependentRecord {
    var lastOnDiskOffset: Int
    fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?)
    fun dispose()
}
