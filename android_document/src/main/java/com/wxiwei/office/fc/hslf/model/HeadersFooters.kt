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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.fc.hslf.record.CString
import com.wxiwei.office.fc.hslf.record.HeadersFootersAtom
import com.wxiwei.office.fc.hslf.record.HeadersFootersContainer
import com.wxiwei.office.fc.hslf.record.OEPlaceholderAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.usermodel.SlideShow

/**
 * Header / Footer settings.
 * 
 * You can get these on slides, or across all notes
 * 
 * @author Yegor Kozlov
 */
class HeadersFooters {
    private val _container: HeadersFootersContainer?
    private var _newRecord: Boolean
    private var _ppt: SlideShow? = null
    private var _sheet: Sheet? = null
    private val _ppt2007: Boolean

    constructor(
        rec: HeadersFootersContainer?, ppt: SlideShow, newRecord: Boolean,
        isPpt2007: Boolean
    ) {
        _container = rec
        _newRecord = newRecord
        _ppt = ppt
        _ppt2007 = false
    }

    constructor(
        rec: HeadersFootersContainer?, sheet: Sheet?, newRecord: Boolean,
        isPpt2007: Boolean
    ) {
        _container = rec
        _newRecord = newRecord
        _sheet = sheet
        _ppt2007 = false
    }

    var headerText: String?
        /**
         * Headers's text
         * 
         * @return Headers's text
         */
        get() {
            val cs = if (_container == null) null else _container.headerAtom
            return getPlaceholderText(OEPlaceholderAtom.MasterHeader.toInt(), cs)
        }
        /**
         * Sets headers's text
         * 
         * @param text headers's text
         */
        set(text) {
            if (_newRecord) attach()

            this.isHeaderVisible = true
            var cs = _container!!.headerAtom
            if (cs == null) cs = _container.addHeaderAtom()

            cs!!.text = text!!
        }

    val footerText: String?
        /**
         * Footer's text
         * 
         * @return Footer's text
         */
        get() {
            val cs = if (_container == null) null else _container.footerAtom
            return getPlaceholderText(OEPlaceholderAtom.MasterFooter.toInt(), cs)
        }

    /**
     * Sets footers's text
     * 
     * @param text footers's text
     */
    fun setFootersText(text: String) {
        if (_newRecord) attach()

        this.isFooterVisible = true
        var cs = _container!!.footerAtom
        if (cs == null) cs = _container.addFooterAtom()

        cs!!.text = text!!
    }

    var dateTimeText: String?
        /**
         * This is the date that the user wants in the footers, instead of today's date.
         * 
         * @return custom user date
         */
        get() {
            val cs = if (_container == null) null else _container.userDateAtom
            return getPlaceholderText(OEPlaceholderAtom.MasterDate.toInt(), cs)
        }
        /**
         * Sets custom user date to be displayed instead of today's date.
         * 
         * @param text custom user date
         */
        set(text) {
            if (_newRecord) attach()

            this.isUserDateVisible = true
            this.isDateTimeVisible = true
            var cs = _container!!.userDateAtom
            if (cs == null) cs = _container.addUserDateAtom()

            cs!!.text = text!!
        }

    var isFooterVisible: Boolean
        /**
         * whether the footer text is displayed.
         */
        get() = isVisible(
            HeadersFootersAtom.fHasFooter,
            OEPlaceholderAtom.MasterFooter.toInt()
        )
        /**
         * whether the footer text is displayed.
         */
        set(flag) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!
                .setFlag(HeadersFootersAtom.fHasFooter, flag)
        }

    var isHeaderVisible: Boolean
        /**
         * whether the header text is displayed.
         */
        get() = isVisible(
            HeadersFootersAtom.fHasHeader,
            OEPlaceholderAtom.MasterHeader.toInt()
        )
        /**
         * whether the header text is displayed.
         */
        set(flag) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!
                .setFlag(HeadersFootersAtom.fHasHeader, flag)
        }

    var isDateTimeVisible: Boolean
        /**
         * whether the date is displayed in the footer.
         */
        get() = isVisible(
            HeadersFootersAtom.fHasDate,
            OEPlaceholderAtom.MasterDate.toInt()
        )
        /**
         * whether the date is displayed in the footer.
         */
        set(flag) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!
                .setFlag(HeadersFootersAtom.fHasDate, flag)
        }

    var isUserDateVisible: Boolean
        /**
         * whether the custom user date is used instead of today's date.
         */
        get() = isVisible(
            HeadersFootersAtom.fHasUserDate,
            OEPlaceholderAtom.MasterDate.toInt()
        )
        /**
         * whether the date is displayed in the footer.
         */
        set(flag) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!
                .setFlag(HeadersFootersAtom.fHasUserDate, flag)
        }

    var isSlideNumberVisible: Boolean
        /**
         * whether the slide number is displayed in the footer.
         */
        get() = isVisible(
            HeadersFootersAtom.fHasSlideNumber,
            OEPlaceholderAtom.MasterSlideNumber.toInt()
        )
        /**
         * whether the slide number is displayed in the footer.
         */
        set(flag) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!
                .setFlag(HeadersFootersAtom.fHasSlideNumber, flag)
        }

    var dateTimeFormat: Int
        /**
         * An integer that specifies the format ID to be used to style the datetime.
         * 
         * @return an integer that specifies the format ID to be used to style the datetime.
         */
        get() = _container!!.headersFootersAtom!!.formatId
        /**
         * An integer that specifies the format ID to be used to style the datetime.
         * 
         * @param formatId an integer that specifies the format ID to be used to style the datetime.
         */
        set(formatId) {
            if (_newRecord) attach()
            _container!!.headersFootersAtom!!.formatId = formatId
        }

    /**
     * Attach this HeadersFootersContainer to the parent Document record
     */
    private fun attach() {
        val doc = _ppt!!.documentRecord!!
        val ch: Array<Record> = doc.getChildRecords()
        var lst: Record? = null
        for (i in ch.indices) {
            if (ch[i].getRecordType() == RecordTypes.List.typeID.toLong()) {
                lst = ch[i]
                break
            }
        }
        doc.addChildAfter(_container!!, lst!!)
        _newRecord = false
    }

    private fun isVisible(flag: Int, placeholderId: Int): Boolean {
        val visible: Boolean
        if (_ppt2007) {
            val master: Sheet? = _sheet ?: _ppt!!.slidesMasters!![0]
            val placeholder = master?.getPlaceholder(placeholderId)
            visible = placeholder != null && placeholder.text != null
        } else {
            visible = _container!!.headersFootersAtom!!.getFlag(flag)
        }
        return visible
    }

    private fun getPlaceholderText(placeholderId: Int, cs: CString?): String? {
        var text: String? = null
        if (_ppt2007) {
            val master: Sheet? = _sheet ?: _ppt!!.slidesMasters!![0]
            val placeholder = master?.getPlaceholder(placeholderId)
            if (placeholder != null) text = placeholder.text

            //default text in master placeholders is not visible
            if ("*" == text) text = null
        } else {
            text = if (cs == null) null else cs.text
        }
        return text
    }
}
