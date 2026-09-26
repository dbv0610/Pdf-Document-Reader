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
package com.wxiwei.office.fc.hssf.record.aggregates

import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.ObjectProtectRecord
import com.wxiwei.office.fc.hssf.record.PasswordRecord
import com.wxiwei.office.fc.hssf.record.ProtectRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordFormatException
import com.wxiwei.office.fc.hssf.record.ScenarioProtectRecord


/**
 * Groups the sheet protection records for a worksheet.
 * 
 * 
 * 
 * See OOO excelfileformat.pdf sec 4.18.2 'Sheet Protection in a Workbook
 * (BIFF5-BIFF8)'
 * 
 * @author Josh Micich
 */
class WorksheetProtectionBlock
/**
 * Creates an empty WorksheetProtectionBlock
 */
    : RecordAggregate() {
    // Every one of these component records is optional
    // (The whole WorksheetProtectionBlock may not be present)
    private var _protectRecord: ProtectRecord? = null
    private var _objectProtectRecord: ObjectProtectRecord? = null
    var hCenter: ScenarioProtectRecord? = null
        private set
    var passwordRecord: PasswordRecord? = null
        private set

    private fun readARecord(rs: RecordStream): Boolean {
        when (rs.peekNextSid()) {
            ProtectRecord.sid.toInt() -> {
                checkNotPresent(_protectRecord)
                _protectRecord = rs.next as ProtectRecord?
            }

            ObjectProtectRecord.sid.toInt() -> {
                checkNotPresent(_objectProtectRecord)
                _objectProtectRecord = rs.next as ObjectProtectRecord?
            }

            ScenarioProtectRecord.sid.toInt() -> {
                checkNotPresent(this.hCenter)
                this.hCenter = rs.next as ScenarioProtectRecord?
            }

            PasswordRecord.sid.toInt() -> {
                checkNotPresent(this.passwordRecord)
                this.passwordRecord = rs.next as PasswordRecord?
            }

            else ->                // all other record types are not part of the PageSettingsBlock
                return false
        }
        return true
    }

    private fun checkNotPresent(rec: Record?) {
        if (rec != null) {
            throw RecordFormatException(
                ("Duplicate PageSettingsBlock record (sid=0x"
                        + Integer.toHexString(rec.getSid().toInt()) + ")")
            )
        }
    }

    override fun visitContainedRecords(rv: RecordVisitor) {
        // Replicates record order from Excel 2007, though this is not critical

        visitIfPresent(_protectRecord, rv)
        visitIfPresent(_objectProtectRecord, rv)
        visitIfPresent(this.hCenter, rv)
        visitIfPresent(this.passwordRecord, rv)
    }

    /**
     * This method reads [WorksheetProtectionBlock] records from the supplied RecordStream
     * until the first non-WorksheetProtectionBlock record is encountered. As each record is read,
     * it is incorporated into this WorksheetProtectionBlock.
     * 
     * 
     * As per the OOO documentation, the protection block records can be expected to be written
     * together (with no intervening records), but earlier versions of POI (prior to Jun 2009)
     * didn't do this.  Workbooks with sheet protection created by those earlier POI versions
     * seemed to be valid (Excel opens them OK). So PO allows continues to support reading of files
     * with non continuous worksheet protection blocks.
     * 
     * 
     * 
     * **Note** - when POI writes out this WorksheetProtectionBlock, the records will always be
     * written in one consolidated block (in the standard ordering) regardless of how scattered the
     * records were when they were originally read.
     */
    fun addRecords(rs: RecordStream) {
        while (true) {
            if (!readARecord(rs)) {
                break
            }
        }
    }

    private val protect: ProtectRecord
        /**
         * @return the ProtectRecord. If one is not contained in the sheet, then one
         * is created.
         */
        get() {
            if (_protectRecord == null) {
                _protectRecord = ProtectRecord(false)
            }
            return _protectRecord!!
        }

    private val password: PasswordRecord
        /**
         * @return the PasswordRecord. If one is not contained in the sheet, then
         * one is created.
         */
        get() {
            if (this.passwordRecord == null) {
                this.passwordRecord = createPassword()
            }
            return this.passwordRecord!!
        }

    /**
     * protect a spreadsheet with a password (not encrypted, just sets protect
     * flags and the password.
     * 
     * @param password to set. Pass `null` to remove all protection
     * @param shouldProtectObjects are protected
     * @param shouldProtectScenarios are protected
     */
    fun protectSheet(
        password: String?, shouldProtectObjects: Boolean,
        shouldProtectScenarios: Boolean
    ) {
        if (password == null) {
            this.passwordRecord = null
            _protectRecord = null
            _objectProtectRecord = null
            this.hCenter = null
            return
        }

        val prec = this.protect
        val pass = this.password
        prec.setProtect(true)
        pass.setPassword(PasswordRecord.hashPassword(password).toInt())
        if (_objectProtectRecord == null && shouldProtectObjects) {
            val rec: ObjectProtectRecord = createObjectProtect()
            rec.setProtect(true)
            _objectProtectRecord = rec
        }
        if (this.hCenter == null && shouldProtectScenarios) {
            val srec: ScenarioProtectRecord = createScenarioProtect()
            srec.setProtect(true)
            this.hCenter = srec
        }
    }

    val isSheetProtected: Boolean
        get() = _protectRecord != null && _protectRecord!!.getProtect()

    val isObjectProtected: Boolean
        get() = _objectProtectRecord != null && _objectProtectRecord!!.getProtect()

    val isScenarioProtected: Boolean
        get() = this.hCenter != null && hCenter!!.getProtect()

    val passwordHash: Int
        get() {
            if (this.passwordRecord == null) {
                return 0
            }
            return passwordRecord!!.getPassword()
        }

    companion object {
        /**
         * @return `true` if the specified Record sid is one belonging to
         * the 'Page Settings Block'.
         */
        fun isComponentRecord(sid: Int): Boolean {
            when (sid) {
                ProtectRecord.sid.toInt(), ObjectProtectRecord.sid.toInt(), ScenarioProtectRecord.sid.toInt(), PasswordRecord.sid.toInt() -> return true
            }
            return false
        }

        private fun visitIfPresent(r: Record?, rv: RecordVisitor) {
            if (r != null) {
                rv.visitRecord(r)
            }
        }

        /**
         * creates an ObjectProtect record with protect set to false.
         */
        private fun createObjectProtect(): ObjectProtectRecord {
            val retval = ObjectProtectRecord()
            retval.setProtect(false)
            return retval
        }

        /**
         * creates a ScenarioProtect record with protect set to false.
         */
        private fun createScenarioProtect(): ScenarioProtectRecord {
            val retval = ScenarioProtectRecord()
            retval.setProtect(false)
            return retval
        }

        /**
         * creates a Password record with password set to 0x0000.
         */
        private fun createPassword(): PasswordRecord {
            return PasswordRecord(0x0000)
        }
    }
}
