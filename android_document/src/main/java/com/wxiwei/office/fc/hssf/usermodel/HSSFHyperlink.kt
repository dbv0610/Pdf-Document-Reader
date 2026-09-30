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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.fc.hssf.record.HyperlinkRecord
import com.wxiwei.office.fc.ss.usermodel.IHyperlink


/**
 * Represents an Excel hyperlink.
 * 
 * @author Yegor Kozlov (yegor at apache dot org)
 */
class HSSFHyperlink : IHyperlink {
    /**
     * Low-level record object that stores the actual hyperlink data
     */
    var record: HyperlinkRecord? = null

    /**
     * If we create a new hypelrink remember its type
     */
    protected var link_type: Int = 0

    /**
     * Construct a new hyperlink
     * 
     * @param type the type of hyperlink to create
     */
    constructor(type: Int) {
        this.link_type = type
        record = HyperlinkRecord()
        when (type) {
            LINK_URL, LINK_EMAIL -> record!!.newUrlLink()
            LINK_FILE -> record!!.newFileLink()
            LINK_DOCUMENT -> record!!.newDocumentLink()
        }
    }

    /**
     * Initialize the hyperlink by a `HyperlinkRecord` record
     * 
     * @param record
     */
    constructor(record: HyperlinkRecord) {
        this.record = record


        // Figure out the type
        if (record.isFileLink()) {
            link_type = LINK_FILE
        } else if (record.isDocumentLink()) {
            link_type = LINK_DOCUMENT
        } else {
            if (record.getAddress() != null &&
                record.getAddress()!!.startsWith("mailto:")
            ) {
                link_type = LINK_EMAIL
            } else {
                link_type = LINK_URL
            }
        }
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     * 
     * @return the 0-based row of the cell that contains the hyperlink
     */
    override fun getFirstRow(): Int {
        return record!!.getFirstRow()
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     * 
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    override fun setFirstRow(row: Int) {
        record!!.setFirstRow(row)
    }

    /**
     * Return the row of the last cell that contains the hyperlink
     * 
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    override fun getLastRow(): Int {
        return record!!.getLastRow()
    }

    /**
     * Set the row of the last cell that contains the hyperlink
     * 
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    override fun setLastRow(row: Int) {
        record!!.setLastRow(row)
    }

    /**
     * Return the column of the first cell that contains the hyperlink
     * 
     * @return the 0-based column of the first cell that contains the hyperlink
     */
    override fun getFirstColumn(): Int {
        return record!!.getFirstColumn()
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     * 
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    override fun setFirstColumn(col: Int) {
        record!!.setFirstColumn(col.toShort().toInt())
    }

    /**
     * Return the column of the last cell that contains the hyperlink
     * 
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    override fun getLastColumn(): Int {
        return record!!.getLastColumn()
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     * 
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    override fun setLastColumn(col: Int) {
        record!!.setLastColumn(col.toShort().toInt())
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     * 
     * @return  the address of this hyperlink
     */
    override fun getAddress(): String? {
        return record!!.getAddress()
    }

    var textMark: String?
        get() = record!!.getTextMark()
        /**
         * Convenience method equivalent to [.setAddress]
         * 
         * @param textMark the place in worksheet this hypelrink referes to, e.g. 'Target Sheet'!A1'
         */
        set(textMark) {
            record!!.setTextMark(textMark)
        }

    var shortFilename: String?
        get() = record!!.getShortFilename()
        /**
         * Convenience method equivalent to [.setAddress]
         * 
         * @param shortFilename the path to a file this hypelrink points to, e.g. 'readme.txt'
         */
        set(shortFilename) {
            record!!.setShortFilename(shortFilename)
        }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
     * 
     * @param address  the address of this hyperlink
     */
    override fun setAddress(address: String?) {
        record!!.setAddress(address)
    }

    /**
     * Return text label for this hyperlink
     * 
     * @return  text to display
     */
    override fun getLabel(): String? {
        return record!!.getLabel()
    }

    /**
     * Sets text label for this hyperlink
     * 
     * @param label text label for this hyperlink
     */
    override fun setLabel(label: String?) {
        record!!.setLabel(label)
    }

    /**
     * Return the type of this hyperlink
     * 
     * @return the type of this hyperlink
     */
    override fun getType(): Int {
        return link_type
    }

    companion object {
        /**
         * Link to a existing file or web page
         */
        const val LINK_URL: Int = 1

        /**
         * Link to a place in this document
         */
        const val LINK_DOCUMENT: Int = 2

        /**
         * Link to an E-mail address
         */
        const val LINK_EMAIL: Int = 3

        /**
         * Link to a file
         */
        const val LINK_FILE: Int = 4
    }
}
