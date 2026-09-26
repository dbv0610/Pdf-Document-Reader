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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.EncryptedDocumentException
import com.wxiwei.office.fc.hssf.record.crypto.Biff8EncryptionKey
import com.wxiwei.office.fc.hssf.record.crypto.Biff8EncryptionKey.Companion.create
import com.wxiwei.office.fc.hssf.record.crypto.Biff8EncryptionKey.Companion.currentUserPassword
import java.io.InputStream

/**
 * A stream based way to get at complete records, with
 * as low a memory footprint as possible.
 * This handles reading from a RecordInputStream, turning
 * the data into full records, processing continue records
 * etc.
 * Most users should use [HSSFEventFactory] /
 * [HSSFListener] and have new records pushed to
 * them, but this does allow for a "pull" style of coding.
 */
class RecordFactoryInputStream(`in`: InputStream?, shouldIncludeContinueRecords: Boolean) {
    /**
     * Keeps track of the sizes of the initial records up to and including [FilePassRecord]
     * Needed for protected files because each byte is encrypted with respect to its absolute
     * position from the start of the stream.
     */
    private class StreamEncryptionInfo(rs: RecordInputStream, outputRecs: MutableList<Record>) {
        private val _initialRecordsSize: Int
        private val _filePassRec: FilePassRecord?
        private val _lastRecord: Record?
        private val _hasBOFRecord: Boolean

        init {
            var rec: Record?
            rs.nextRecord()
            var recSize = 4 + rs.remaining()
            rec = RecordFactory.createSingleRecord(rs)
            if (rec != null) outputRecs.add(rec)
            var fpr: FilePassRecord? = null
            if (rec is BOFRecord) {
                _hasBOFRecord = true
                if (rs.hasNextRecord()) {
                    rs.nextRecord()
                    rec = RecordFactory.createSingleRecord(rs)
                    recSize += rec?.getRecordSize() ?: 0
                    if (rec != null) outputRecs.add(rec)
                    if (rec is FilePassRecord) {
                        fpr = rec
                        outputRecs.removeAt(outputRecs.size - 1)
                        // TODO - add fpr not added to outputRecs
                        rec = outputRecs.get(0)
                    } else {
                        // workbook not encrypted (typical case)
                        check(rec !is EOFRecord) { "Nothing between BOF and EOF" }
                    }
                }
            } else {
                // Invalid in a normal workbook stream.
                // However, some test cases work on sub-sections of
                // the workbook stream that do not begin with BOF
                _hasBOFRecord = false
            }
            _initialRecordsSize = recSize
            _filePassRec = fpr
            _lastRecord = rec
        }

        fun createDecryptingStream(original: InputStream?): RecordInputStream {
            val fpr = _filePassRec
            val userPassword = currentUserPassword

            val key: Biff8EncryptionKey?
            if (userPassword == null) {
                key = create(fpr!!.getDocId())
            } else {
                key = create(userPassword, fpr!!.getDocId())
            }
            if (!key.validate(fpr.getSaltData(), fpr.getSaltHash())) {
                /*throw new EncryptedDocumentException(
						(userPassword == null ? "Default" : "Supplied")
						+ " password is invalid for docId/saltData/saltHash");*/
                // OpenFileErrors maps these messages to PASSWORD_REQUIRED / PASSWORD_INCORRECT
                throw EncryptedDocumentException(
                    if (userPassword == null)
                        "Cannot process encrypted office files!"
                    else
                        "Password is incorrect"
                )
            }
            return RecordInputStream(original!!, key, _initialRecordsSize)
        }

        fun hasEncryption(): Boolean {
            return _filePassRec != null
        }

        /**
         * @return last record scanned while looking for encryption info.
         * This will typically be the first or second record read. Possibly `null`
         * if stream was empty
         */
        fun getLastRecord(): Record? {
            return _lastRecord
        }

        /**
         * `false` in some test cases
         */
        fun hasBOFRecord(): Boolean {
            return _hasBOFRecord
        }
    }


    private /*final*/ var _recStream: RecordInputStream?
    private val _shouldIncludeContinueRecords: Boolean

    /**
     * Temporarily stores a group of [Record]s, for future return by [.nextRecord].
     * This is used at the start of the workbook stream, and also when the most recently read
     * underlying record is a [MulRKRecord]
     */
    private var _unreadRecordBuffer: Array<Record?>? = null

    /**
     * used to help iterating over the unread records
     */
    private var _unreadRecordIndex = -1

    /**
     * The most recent record that we gave to the user
     */
    private var _lastRecord: Record? = null

    /**
     * The most recent DrawingRecord seen
     */
    private var _lastDrawingRecord: DrawingRecord? = DrawingRecord()

    private var _bofDepth: Int = 0

    private var _lastRecordWasEOFLevelZero: Boolean = false


    /**
     * @param shouldIncludeContinueRecords caller can pass `false` if loose
     * [ContinueRecord]s should be skipped (this is sometimes useful in event based
     * processing).
     */
    init {
        var rs = RecordInputStream(`in`!!)
        val records: MutableList<Record> = ArrayList<Record>()
        val sei = StreamEncryptionInfo(rs, records)
        if (sei.hasEncryption()) {
            rs = sei.createDecryptingStream(`in`!!)
        } else {
            // typical case - non-encrypted stream
        }

        if (!records.isEmpty()) {
            _unreadRecordBuffer = records.toTypedArray()
            _unreadRecordIndex = 0
        }
        _recStream = rs
        _shouldIncludeContinueRecords = shouldIncludeContinueRecords
        _lastRecord = sei.getLastRecord()

        /*
		* How to recognise end of stream?
		* In the best case, the underlying input stream (in) ends just after the last EOF record
		* Usually however, the stream is padded with an arbitrary byte count.  Excel and most apps
		* reliably use zeros for padding and if this were always the case, this code could just
		* skip all the (zero sized) records with sid==0.  However, bug 46987 shows a file with
		* non-zero padding that is read OK by Excel (Excel also fixes the padding).
		*
		* So to properly detect the workbook end of stream, this code has to identify the last
		* EOF record.  This is not so easy because the worbook bof+eof pair do not bracket the
		* whole stream.  The worksheets follow the workbook, but it is not easy to tell how many
		* sheet sub-streams should be present.  Hence we are looking for an EOF record that is not
		* immediately followed by a BOF record.  One extra complication is that bof+eof sub-
		* streams can be nested within worksheet streams and it's not clear in these cases what
		* record might follow any EOF record.  So we also need to keep track of the bof/eof
		* nesting level.
		*/
        _bofDepth = if (sei.hasBOFRecord()) 1 else 0
        _lastRecordWasEOFLevelZero = false
    }

    /**
     * Returns the next (complete) record from the
     * stream, or null if there are no more.
     */
    fun nextRecord(): Record? {
        var r: Record?
        r = getNextUnreadRecord()
        if (r != null) {
            // found an unread record
            return r
        }
        while (true) {
            if (!_recStream!!.hasNextRecord()) {
                // recStream is exhausted;
                return null
            }

            if (_lastRecordWasEOFLevelZero) {
                // Potential place for ending the workbook stream
                // Check that the next record is not BOFRecord(0x0809)
                // Normally the input stream contains only zero padding after the last EOFRecord,
                // but bug 46987 and 48068 suggests that the padding may be garbage.
                // This code relies on the padding bytes not starting with BOFRecord.sid
                if (_recStream!!.getNextSid() != BOFRecord.Companion.sid.toInt()) {
                    return null
                }
                // else - another sheet substream starting here
            }

            // step underlying RecordInputStream to the next record
            _recStream!!.nextRecord()

            r = readNextRecord()
            if (r == null) {
                // some record types may get skipped (e.g. DBCellRecord and ContinueRecord)
                continue
            }
            return r
        }
    }

    /**
     * @return the next [Record] from the multiple record group as expanded from
     * a recently read [MulRKRecord]. `null` if not present.
     */
    private fun getNextUnreadRecord(): Record? {
        if (_unreadRecordBuffer != null) {
            val ix = _unreadRecordIndex
            if (ix < _unreadRecordBuffer!!.size) {
                val result = _unreadRecordBuffer!![ix]
                _unreadRecordIndex = ix + 1
                return result
            }
            _unreadRecordIndex = -1
            _unreadRecordBuffer = null
        }
        return null
    }

    /**
     * @return the next available record, or `null` if
     * this pass didn't return a record that's
     * suitable for returning (eg was a continue record).
     */
    private fun readNextRecord(): Record? {
        val record = RecordFactory.createSingleRecord(_recStream!!) ?: return null
        _lastRecordWasEOFLevelZero = false

        //不是连续的DrawingRecord/ContinueRecord和ObjRecord成对出现
        if (_lastDrawingRecord != null && record.getSid() != ContinueRecord.Companion.sid && record.getSid() != ObjRecord.Companion.sid && record.getSid() != TextObjectRecord.Companion.sid) {
            _lastDrawingRecord = null
        }

        if (record is BOFRecord) {
            _bofDepth++
            return record
        }

        if (record is EOFRecord) {
            _bofDepth--
            if (_bofDepth < 1) {
                _lastRecordWasEOFLevelZero = true
            }

            return record
        }

        if (record is DBCellRecord) {
            // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
            return null
        }

        if (record is RKRecord) {
            return RecordFactory.convertToNumberRecord(record)
        }

        if (record is MulRKRecord) {
            val records: Array<Record?> = RecordFactory.convertRKRecords(record) as Array<Record?>

            _unreadRecordBuffer = records
            _unreadRecordIndex = 1
            return records[0]
        }

        if (record.getSid() == DrawingGroupRecord.Companion.sid
            && _lastRecord is DrawingGroupRecord
        ) {
            val lastDGRecord = _lastRecord as DrawingGroupRecord
            lastDGRecord.join(record as AbstractEscherHolderRecord)
            return null
        }
        if (record.getSid() == ContinueRecord.Companion.sid) {
            val contRec = record as ContinueRecord

            if (_lastRecord is ObjRecord || _lastRecord is TextObjectRecord) {
                // Drawing records have a very strange continue behaviour.
                //There can actually be OBJ records mixed between the continues.
                if (_lastDrawingRecord != null) {
                    _lastDrawingRecord!!.processContinueRecord(contRec.data!!)
                    contRec.resetData()
                }


                //we must remember the position of the continue record.
                //in the serialization procedure the original structure of records must be preserved
                if (_shouldIncludeContinueRecords) {
                    return record
                }
                return null
            }
            if (_lastRecord is DrawingGroupRecord) {
                (_lastRecord as DrawingGroupRecord).processContinueRecord(contRec.data!!)
                return null
            }
            if (_lastRecord is DrawingRecord) {
                (_lastRecord as DrawingRecord).processContinueRecord(contRec.data!!)
                return null
            }
            if (_lastRecord is UnknownRecord) {
                //Gracefully handle records that we don't know about,
                //that happen to be continued
                return record
            }
            if (_lastRecord is EOFRecord) {
                // This is really odd, but excel still sometimes
                //  outputs a file like this all the same
                return record
            }
            throw RecordFormatException("Unhandled Continue Record followining " + _lastRecord!!.javaClass)
        }
        _lastRecord = record
        if (record is DrawingRecord) {
            _lastDrawingRecord = record
        }
        return record
    }

    fun dispose() {
        _recStream = null

        _unreadRecordBuffer = null

        _lastRecord = null
        _lastDrawingRecord = null
    }
}
