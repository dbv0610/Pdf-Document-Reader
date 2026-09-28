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

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.POILogger

/**
 * A container record that specifies information about external video data.
 * 
 * @author Yegor Kozlov
 */
class ExVideoContainer : RecordContainer {
    private var _header: ByteArray?

    /**
     * Returns the ExMediaAtom of this link
     */
    // Links to our more interesting children
    var exMediaAtom: ExMediaAtom? = null
        private set

    /**
     * Returns the Path Atom (CString) of this link
     */
    //the UNC or local path to a video file.
    var pathAtom: CString? = null
        private set

    /**
     * Set things up, and find our more interesting children
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
        findInterestingChildren()
    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper
     * methods.
     */
    private fun findInterestingChildren() {
        // First child should be the ExMediaAtom

        if (_children[0] is ExMediaAtom) {
            this.exMediaAtom = _children[0] as ExMediaAtom
        } else {
            logger.log(
                POILogger.ERROR, "First child record wasn't a ExMediaAtom, was of type "
                        + _children[0].getRecordType()
            )
        }
        if (_children[1] is CString) {
            pathAtom = _children[1] as CString
        } else {
            logger.log(
                POILogger.ERROR, "Second child record wasn't a CString, was of type "
                        + _children[1].getRecordType()
            )
        }
    }

    /**
     * Create a new ExVideoContainer, with blank fields
     */
    constructor() {
        // Setup our header block
        _header = ByteArray(8)
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())

        val newChildren = arrayOfNulls<Record>(2)
        this.exMediaAtom = ExMediaAtom()
        newChildren[0] = this.exMediaAtom!!
        pathAtom = CString()
        newChildren[1] = pathAtom!!
        _children = newChildren.requireNoNulls()
    }

    /**
     * We are of type 4103
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExVideoContainer.typeID.toLong()
    }

    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (pathAtom != null) {
            pathAtom!!.dispose()
            pathAtom = null
        }
        if (this.exMediaAtom != null) {
            exMediaAtom!!.dispose()
            this.exMediaAtom = null
        }
    }
}
