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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.fc.hssf.record.Record

class WorkbookRecordList : Iterable<Record> {
    var records: MutableList<Record> = ArrayList<Record>()

    var protpos: Int = 0 // holds the position of the protect record.
    var bspos: Int = 0 // holds the position of the last bound sheet.
    var tabpos: Int = 0 // holds the position of the tabid record
    var fontpos: Int = 0 // hold the position of the last font record
    var xfpos: Int = 0 // hold the position of the last extended font record
    var backuppos: Int = 0 // holds the position of the backup record.
    /**
     * Returns the namepos.
     * @return int
     */
    /**
     * Sets the namepos.
     * @param namepos The namepos to set
     */
    var namepos: Int = 0 // holds the position of last name record
    /**
     * Returns the supbookpos.
     * @return int
     */
    /**
     * Sets the supbookpos.
     * @param supbookpos The supbookpos to set
     */
    var supbookpos: Int = 0 // holds the position of sup book
    /**
     * Returns the externsheetPos.
     * @return int
     */
    /**
     * Sets the externsheetPos.
     * @param externsheetPos The externsheetPos to set
     */
    var externsheetPos: Int = 0 // holds the position of the extern sheet
    var palettepos: Int = -1 // hold the position of the palette, if applicable


    fun size(): Int {
        return records.size
    }

    fun get(i: Int): Record {
        return records.get(i)
    }

    fun add(pos: Int, r: Record) {
        records.add(pos, r)
        if (this.protpos >= pos) this.protpos = protpos + 1
        if (this.bspos >= pos) this.bspos = bspos + 1
        if (this.tabpos >= pos) this.tabpos = tabpos + 1
        if (this.fontpos >= pos) this.fontpos = fontpos + 1
        if (this.xfpos >= pos) this.xfpos = xfpos + 1
        if (this.backuppos >= pos) this.backuppos = backuppos + 1
        if (this.namepos >= pos) this.namepos = namepos + 1
        if (this.supbookpos >= pos) this.supbookpos = supbookpos + 1
        if ((this.palettepos != -1) && (this.palettepos >= pos)) this.palettepos = palettepos + 1
        if (this.externsheetPos >= pos) this.externsheetPos = this.externsheetPos + 1
    }

    override fun iterator(): MutableIterator<Record> {
        return records.iterator()
    }

    fun remove(record: Any?) {
        val i = records.indexOf(record)
        this.remove(i)
    }

    fun remove(pos: Int) {
        records.removeAt(pos)
        if (this.protpos >= pos) this.protpos = protpos - 1
        if (this.bspos >= pos) this.bspos = bspos - 1
        if (this.tabpos >= pos) this.tabpos = tabpos - 1
        if (this.fontpos >= pos) this.fontpos = fontpos - 1
        if (this.xfpos >= pos) this.xfpos = xfpos - 1
        if (this.backuppos >= pos) this.backuppos = backuppos - 1
        if (this.namepos >= pos) this.namepos = this.namepos - 1
        if (this.supbookpos >= pos) this.supbookpos = this.supbookpos - 1
        if ((this.palettepos != -1) && (this.palettepos >= pos)) this.palettepos = palettepos - 1
        if (this.externsheetPos >= pos) this.externsheetPos = this.externsheetPos - 1
    }
}
