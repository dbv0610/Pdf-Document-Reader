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
 * This data represents an embedded object in the document.
 * 
 * @author Daniel Noll
 */
open class ExEmbed : RecordContainer {
    /**
     * Set things up, and find our more interesting children
     * 
     * @param source the source data as a byte array.
     * @param start the start offset into the byte array.
     * @param len the length of the slice in the byte array.
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
     * Create a new ExEmbed, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        val newChildren = arrayOfNulls<Record>(5)

        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())

        // Setup our child records
        val cs1 = CString()
        cs1.options = 0x1 shl 4
        val cs2 = CString()
        cs2.options = 0x2 shl 4
        val cs3 = CString()
        cs3.options = 0x3 shl 4
        newChildren[0] = ExEmbedAtom()
        newChildren[1] = ExOleObjAtom()
        newChildren[2] = cs1
        newChildren[3] = cs2
        newChildren[4] = cs3
        _children = newChildren.requireNoNulls()
        findInterestingChildren()
    }

    /**
     * Go through our child records, picking out the ones that are
     * interesting, and saving those for use by the easy helper methods.
     */
    private fun findInterestingChildren() {
        // First child should be the ExHyperlinkAtom

        if (_children[0] is ExEmbedAtom) {
            embedAtom = _children[0] as ExEmbedAtom
        } else {
            logger.log(
                POILogger.ERROR, "First child record wasn't a ExEmbedAtom, was of type "
                        + _children[0].getRecordType()
            )
        }

        // Second child should be the ExOleObjAtom
        if (_children[1] is ExOleObjAtom) {
            this.exOleObjAtom = _children[1] as ExOleObjAtom
        } else {
            logger.log(
                POILogger.ERROR, "Second child record wasn't a ExOleObjAtom, was of type "
                        + _children[1].getRecordType()
            )
        }

        for (i in 2..<_children.size) {
            if (_children[i] is CString) {
                val cs = _children[i] as CString
                val opts = cs.options shr 4
                when (opts) {
                    0x1 -> menuName = cs
                    0x2 -> progId = cs
                    0x3 -> clipboardName = cs
                }
            }
        }
    }

    val exEmbedAtom: ExEmbedAtom?
        /**
         * Gets the [ExEmbedAtom].
         * 
         * @return the [ExEmbedAtom].
         */
        get() = embedAtom as ExEmbedAtom?

    /**
     * Gets the name used for menus and the Links dialog box.
     * 
     * @return the name used for menus and the Links dialog box.
     */
    fun getMenuName(): String? {
        return if (menuName == null) null else menuName!!.text
    }

    fun setMenuName(s: String) {
        if (menuName != null) menuName!!.text = s
    }

    /**
     * Gets the OLE Programmatic Identifier.
     * 
     * @return the OLE Programmatic Identifier.
     */
    fun getProgId(): String? {
        return if (progId == null) null else progId!!.text
    }

    fun setProgId(s: String) {
        if (progId != null) progId!!.text = s
    }

    /**
     * Gets the name that appears in the paste special dialog.
     * 
     * @return the name that appears in the paste special dialog.
     */
    fun getClipboardName(): String? {
        return if (clipboardName == null) null else clipboardName!!.text
    }

    fun setClipboardName(s: String) {
        if (clipboardName != null) clipboardName!!.text = s
    }

    /**
     * Returns the type (held as a little endian in bytes 3 and 4)
     * that this class handles.
     * 
     * @return the record type.
     */
    public override fun getRecordType(): Long {
        return RecordTypes.ExEmbed.typeID.toLong()
    }


    /**
     * 
     */
    public override fun dispose() {
        super.dispose()
        _header = null
        if (embedAtom != null) {
            embedAtom!!.dispose()
            embedAtom = null
        }
        if (this.exOleObjAtom != null) {
            exOleObjAtom!!.dispose()
            this.exOleObjAtom = null
        }
        if (menuName != null) {
            menuName!!.dispose()
            menuName = null
        }
        if (progId != null) {
            progId!!.dispose()
            progId = null
        }
        if (clipboardName != null) {
            clipboardName!!.dispose()
            clipboardName = null
        }
    }


    /**
     * Record header data.
     */
    private var _header: ByteArray?

    // Links to our more interesting children
    protected var embedAtom: RecordAtom? = null

    /**
     * Gets the [ExOleObjAtom].
     * 
     * @return the [ExOleObjAtom].
     */
    var exOleObjAtom: ExOleObjAtom? = null
        private set
    private var menuName: CString? = null
    private var progId: CString? = null
    private var clipboardName: CString? = null
}
