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
 * A container record that specifies information about the footers on a presentation slide.
 * 
 * 
 * It contains:<br></br>
 *  *  1. [HeadersFootersAtom]
 *  *  2. [CString], Instance UserDate (0), optional: Stores the user's date.
 * This is the date that the user wants in the footers, instead of today's date.
 *  *  3. [CString], Instance Header (1), optional: Stores the Header's contents.
 *  *  4. [CString], Instance Footer (2), optional: Stores the Footer's contents.
 * 
 * 
 * @author Yegor Kozlov
 */
class HeadersFootersContainer : RecordContainer {
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        _children = findChildRecords(source, start + 8, len - 8)
        for (i in _children.indices) {
            if (_children[i] is HeadersFootersAtom) this.headersFootersAtom =
                _children[i] as HeadersFootersAtom
            else if (_children[i] is CString) {
                val cs = _children[i] as CString
                val opts = cs.options shr 4
                when (opts) {
                    USERDATEATOM -> this.userDateAtom = cs
                    HEADERATOM -> this.headerAtom = cs
                    FOOTERATOM -> this.footerAtom = cs
                    else -> logger.log(
                        POILogger.WARN,
                        "Unexpected CString.Options in HeadersFootersContainer: " + opts
                    )
                }
            } else {
                logger.log(
                    POILogger.WARN,
                    "Unexpected record in HeadersFootersContainer: " + _children[i]
                )
            }
        }
    }

    constructor(options: Short) {
        _header = ByteArray(8)
        LittleEndian.putShort(_header!!, 0, options)
        LittleEndian.putShort(_header!!, 2, getRecordType().toShort())

        this.headersFootersAtom = HeadersFootersAtom()
        _children = arrayOf<Record>(
            this.headersFootersAtom!!
        )
        this.footerAtom = null
        this.headerAtom = this.footerAtom
        this.userDateAtom = this.headerAtom
    }

    /**
     * Return the type, which is `[RecordTypes.HeadersFooters]`
     */
    public override fun getRecordType(): Long {
        return RecordTypes.HeadersFooters.typeID.toLong()
    }

    val options: Int
        /**
         * Must be either [.SlideHeadersFootersContainer] or [.NotesHeadersFootersContainer]
         * 
         * @return "instance" field in the record header
         */
        get() = LittleEndian.getShort(_header!!, 0).toInt()

    /**
     * Insert a [CString] record that stores the user's date.
     * 
     * @return  the created [CString] record that stores the user's date.
     */
    fun addUserDateAtom(): CString? {
        if (this.userDateAtom != null) return this.userDateAtom

        this.userDateAtom = CString()
        userDateAtom!!.options = USERDATEATOM shl 4

        addChildAfter(this.userDateAtom!!, this.headersFootersAtom!!)

        return this.userDateAtom
    }

    /**
     * Insert a [CString] record that stores the user's date.
     * 
     * @return  the created [CString] record that stores the user's date.
     */
    fun addHeaderAtom(): CString? {
        if (this.headerAtom != null) return this.headerAtom

        this.headerAtom = CString()
        headerAtom!!.options = HEADERATOM shl 4

        var r: Record? = this.headersFootersAtom
        if (this.userDateAtom != null) r = this.headersFootersAtom
        addChildAfter(this.headerAtom!!, r!!)

        return this.headerAtom
    }

    /**
     * Insert a [CString] record that stores the user's date.
     * 
     * @return  the created [CString] record that stores the user's date.
     */
    fun addFooterAtom(): CString? {
        if (this.footerAtom != null) return this.footerAtom

        this.footerAtom = CString()
        footerAtom!!.options = FOOTERATOM shl 4

        var r: Record? = this.headersFootersAtom
        if (this.headerAtom != null) r = this.headerAtom
        else if (this.userDateAtom != null) r = this.userDateAtom
        addChildAfter(this.footerAtom!!, r!!)

        return this.footerAtom
    }


    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (this.headersFootersAtom != null) {
            headersFootersAtom!!.dispose()
            this.headersFootersAtom = null
        }
        if (this.userDateAtom != null) {
            userDateAtom!!.dispose()
            this.userDateAtom = null
        }
        if (this.headerAtom != null) {
            headerAtom!!.dispose()
            this.headerAtom = null
        }
        if (this.footerAtom != null) {
            footerAtom!!.dispose()
            this.footerAtom = null
        }
    }


    private var _header: ByteArray?

    /**
     * HeadersFootersAtom stores the basic information of the header and footer structure.
     * 
     * @return `HeadersFootersAtom`
     */
    var headersFootersAtom: HeadersFootersAtom? = null
        private set

    /**
     * A [CString] record that stores the user's date.
     * 
     * This is the date that the user wants in the footers, instead of today's date.
     * 
     * @return A [CString] record that stores the user's date or `null`
     */
    var userDateAtom: CString? = null
        private set

    /**
     * A [CString] record that stores the Header's contents.
     * 
     * @return A [CString] record that stores the Header's contents or `null`
     */
    var headerAtom: CString? = null
        private set

    /**
     * A [CString] record that stores the Footers's contents.
     * 
     * @return A [CString] record that stores the Footers's contents or `null`
     */
    var footerAtom: CString? = null
        private set

    companion object {
        /**
         * "instance" field in the record header indicating that this HeadersFootersContaine
         * is applied for slides
         */
        const val SlideHeadersFootersContainer: Short = 0x3F

        /**
         * "instance" field in the record header indicating that this HeadersFootersContaine
         * is applied for notes and handouts
         */
        const val NotesHeadersFootersContainer: Short = 0x4F

        const val USERDATEATOM: Int = 0
        const val HEADERATOM: Int = 1
        const val FOOTERATOM: Int = 2
    }
}
