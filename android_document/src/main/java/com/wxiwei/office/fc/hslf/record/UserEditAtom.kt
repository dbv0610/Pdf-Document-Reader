/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import java.util.Hashtable

/**
 * A UserEdit Atom (type 4085). Holds information which bits of the file
 * were last used by powerpoint, the version of powerpoint last used etc.
 * 
 * ** WARNING ** stores byte offsets from the start of the PPT stream to
 * other records! If you change the size of any elements before one of
 * these, you'll need to update the offsets!
 * 
 * @author Nick Burch
 */
class UserEditAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?
    private var reserved: ByteArray?

    // Somewhat user facing getters
    val lastViewedSlideID: Int
    private val pptVersion: Int

    // More scary internal setters
    // Scary internal getters
    var lastUserEditAtomOffset: Int
    var persistPointersOffset: Int
    val docPersistRef: Int
    var maxPersistWritten: Int
    var lastViewType: Short

    /* *************** record code follows ********************** */ /**
     * For the UserEdit Atom
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 34) {
            len = 34
        }

        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the last viewed slide ID
        lastViewedSlideID = getInt(source, start + 0 + 8)

        // Get the PPT version
        pptVersion = getInt(source, start + 4 + 8)

        // Get the offset to the previous incremental save's UserEditAtom
        // This will be the byte offset on disk where the previous one
        //  starts, or 0 if this is the first one
        lastUserEditAtomOffset = getInt(source, start + 8 + 8)

        // Get the offset to the persist pointers
        // This will be the byte offset on disk where the preceding
        //  PersistPtrFullBlock or PersistPtrIncrementalBlock starts
        persistPointersOffset = getInt(source, start + 12 + 8)

        // Get the persist reference for the document persist object
        // Normally seems to be 1
        docPersistRef = getInt(source, start + 16 + 8)

        // Maximum number of persist objects written
        maxPersistWritten = getInt(source, start + 20 + 8)

        // Last view type
        lastViewType = getShort(source, start + 24 + 8)

        // There might be a few more bytes, which are a reserved field
        reserved = ByteArray(len - 26 - 8)
        System.arraycopy(source, start + 26 + 8, reserved, 0, reserved!!.size)
    }

    /**
     * We are of type 4085
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * At write-out time, update the references to PersistPtrs and
     * other UserEditAtoms to point to their new positions
     */
    public override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
        // Look up the new positions of our preceding UserEditAtomOffset
        if (lastUserEditAtomOffset != 0) {
            val newLocation = oldToNewReferencesLookup!!.get(lastUserEditAtomOffset)
            if (newLocation == null) {
                throw RuntimeException("Couldn't find the new location of the UserEditAtom that used to be at " + lastUserEditAtomOffset)
            }
            lastUserEditAtomOffset = newLocation
        }

        // Ditto for our PersistPtr
        val newLocation = oldToNewReferencesLookup!!.get(persistPointersOffset)
        if (newLocation == null) {
            throw RuntimeException("Couldn't find the new location of the PersistPtr that used to be at " + persistPointersOffset)
        }
        persistPointersOffset = newLocation
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        reserved = null
    }

    companion object {
        const val LAST_VIEW_NONE: Int = 0
        const val LAST_VIEW_SLIDE_VIEW: Int = 1
        const val LAST_VIEW_OUTLINE_VIEW: Int = 2
        const val LAST_VIEW_NOTES: Int = 3

        private const val _type = 4085L
    }
}
