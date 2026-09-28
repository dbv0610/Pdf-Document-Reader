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

/**
 * This class represents a comment on a slide, in the format used by
 * PPT 2000/XP/etc. (PPT 97 uses plain Escher Text Boxes for comments)
 * @author Nick Burch
 */
class Comment2000 : RecordContainer {
    private var _header: ByteArray?

    var author: String?
        /**
         * Get the Author of this comment
         */
        get() = if (authorRecord == null) null else authorRecord!!.text
        /**
         * Set the Author of this comment
         */
        set(author) {
            authorRecord!!.text = author!!
        }

    var authorInitials: String?
        /**
         * Get the Author's Initials of this comment
         */
        get() = if (authorInitialsRecord == null) null else authorInitialsRecord!!.text
        /**
         * Set the Author's Initials of this comment
         */
        set(initials) {
            authorInitialsRecord!!.text = initials!!
        }

    var text: String?
        /**
         * Get the text of this comment
         */
        get() = if (commentRecord == null) null else commentRecord!!.text
        /**
         * Set the text of this comment
         */
        set(text) {
            commentRecord!!.text = text!!
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
        for (r in _children) {
            if (r is CString) {
                val cs = r
                val recInstance = cs.options shr 4
                when (recInstance) {
                    0 -> authorRecord = cs
                    1 -> commentRecord = cs
                    2 -> authorInitialsRecord = cs
                }
            } else if (r is Comment2000Atom) {
                this.comment2000Atom = r
            }
            /*else
            {
                logger.log(POILogger.WARN, "Unexpected record with type=" + r.getRecordType()
                    + " in Comment2000: " + r.getClass().getName());
            }*/
        }
    }

    /**
     * Create a new Comment2000, with blank fields
     */
    constructor() {
        _header = ByteArray(8)
        val newChildren = arrayOfNulls<Record>(4)

        // Setup our header block
        _header!![0] = 0x0f // We are a container record
        LittleEndian.putShort(_header!!, 2, _type.toShort())

        // Setup our child records
        val csa = CString()
        val csb = CString()
        val csc = CString()
        csa.options = 0x00
        csb.options = 0x10
        csc.options = 0x20
        newChildren[0] = csa
        newChildren[1] = csb
        newChildren[2] = csc
        newChildren[3] = Comment2000Atom()
        _children = newChildren.requireNoNulls()
        findInterestingChildren()
    }

    /**
     * We are of type 1200
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (authorRecord != null) {
            authorRecord!!.dispose()
            authorRecord = null
        }
        if (authorInitialsRecord != null) {
            authorInitialsRecord!!.dispose()
            authorInitialsRecord = null
        }
        if (commentRecord != null) {
            commentRecord!!.dispose()
            commentRecord = null
        }
        if (this.comment2000Atom != null) {
            comment2000Atom!!.dispose()
            this.comment2000Atom = null
        }
    }


    // Links to our more interesting children
    /**
     * An optional string that specifies the name of the author of the presentation comment.
     */
    private var authorRecord: CString? = null

    /**
     * An optional string record that specifies the text of the presentation comment
     */
    private var authorInitialsRecord: CString? = null

    /**
     * An optional string record that specifies the initials of the author of the presentation comment
     */
    private var commentRecord: CString? = null

    /**
     * Returns the Comment2000Atom of this Comment
     */
    /**
     * A Comment2000Atom record that specifies the settings for displaying the presentation comment
     */
    var comment2000Atom: Comment2000Atom? = null
        private set

    companion object {
        private const val _type: Long = 12000
    }
}
