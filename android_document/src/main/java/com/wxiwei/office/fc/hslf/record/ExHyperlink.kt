/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.POILogger

/**
 * This class represents the data of a link in the document.
 * @author Nick Burch
 */
class ExHyperlink : RecordContainer {
    private var _header: ByteArray?

    var linkURL: String?
        /**
         * Returns the URL of the link.
         * 
         * @return the URL of the link
         */
        get() = if (linkDetailsB == null) null else linkDetailsB!!.text
        /**
         * Sets the URL of the link
         * TODO: Figure out if we should always set both
         */
        set(url) {
            if (linkDetailsB != null) {
                linkDetailsB!!.text = url!!
            }
        }

    var linkTitle: String?
        /**
         * Returns the hyperlink's user-readable name
         * 
         * @return the hyperlink's user-readable name
         */
        get() = if (linkDetailsA == null) null else linkDetailsA!!.text
        set(title) {
            if (linkDetailsA != null) {
                linkDetailsA!!.text = title!!
            }
        }

    /**
     * Get the link details (field A)
     */
    fun _getDetailsA(): String? {
        return if (linkDetailsA == null) null else linkDetailsA!!.text
    }

    /**
     * Get the link details (field B)
     */
    fun _getDetailsB(): String? {
        return if (linkDetailsB == null) null else linkDetailsB!!.text
    }

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
        // First child should be the ExHyperlinkAtom

        if (_children[0] is ExHyperlinkAtom) {
            this.exHyperlinkAtom = _children[0] as ExHyperlinkAtom
        } else {
            logger.log(
                POILogger.ERROR,
                "First child record wasn't a ExHyperlinkAtom, was of type " + _children[0].getRecordType()
            )
        }

        for (i in 1..<_children.size) {
            if (_children[i] is CString) {
                if (linkDetailsA == null) linkDetailsA = _children[i] as CString
                else linkDetailsB = _children[i] as CString
            } else {
                logger.log(
                    POILogger.ERROR,
                    "Record after ExHyperlinkAtom wasn't a CString, was of type " + _children[1].getRecordType()
                )
            }
        }
    }

    /**
     * Create a new ExHyperlink, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        val newChildren = arrayOfNulls<Record>(3)


        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, _type.toShort())


        // Setup our child records
        val csa = CString()
        val csb = CString()
        csa.options = 0x00
        csb.options = 0x10
        newChildren[0] = ExHyperlinkAtom()
        newChildren[1] = csa
        newChildren[2] = csb
        _children = newChildren.requireNoNulls()
        findInterestingChildren()
    }

    /**
     * We are of type 4055
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (linkDetailsA != null) {
            linkDetailsA!!.dispose()
            linkDetailsA = null
        }
        if (linkDetailsB != null) {
            linkDetailsB!!.dispose()
            linkDetailsB = null
        }
        if (this.exHyperlinkAtom != null) {
            exHyperlinkAtom!!.dispose()
            this.exHyperlinkAtom = null
        }
    }

    /**
     * Returns the ExHyperlinkAtom of this link
     */
    // Links to our more interesting children
    var exHyperlinkAtom: ExHyperlinkAtom? = null
        private set
    private var linkDetailsA: CString? = null
    private var linkDetailsB: CString? = null

    companion object {
        private const val _type: Long = 4055
    }
}
