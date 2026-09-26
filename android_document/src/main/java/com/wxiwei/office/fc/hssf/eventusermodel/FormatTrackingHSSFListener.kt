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
package com.wxiwei.office.fc.hssf.eventusermodel

import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.record.FormatRecord
import com.wxiwei.office.fc.hssf.record.FormulaRecord
import com.wxiwei.office.fc.hssf.record.NumberRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.usermodel.HSSFDataFormat
import com.wxiwei.office.fc.hssf.usermodel.HSSFDataFormatter
import java.text.NumberFormat
import java.util.Hashtable
import java.util.Locale

/**
 * A proxy HSSFListener that keeps track of the document formatting records, and
 * provides an easy way to look up the format strings used by cells from their
 * ids.
 */
class FormatTrackingHSSFListener @JvmOverloads constructor(
    private val _childListener: HSSFListener,
    locale: Locale = Locale.getDefault()
) : HSSFListener {
    private val _formatter: HSSFDataFormatter
    private val _defaultFormat: NumberFormat
    private val _customFormatRecords: MutableMap<Int?, FormatRecord?> =
        Hashtable<Int?, FormatRecord?>()
    private val _xfRecords: MutableList<ExtendedFormatRecord?> = ArrayList<ExtendedFormatRecord?>()

    /**
     * Creates a format tracking wrapper around the given listener, using
     * the given locale for the formats.
     */
    /**
     * Creates a format tracking wrapper around the given listener, using
     * the [default locale][Locale.getDefault] for the formats.
     */
    init {
        _formatter = HSSFDataFormatter(locale)
        _defaultFormat = NumberFormat.getInstance(locale)
    }

    protected val numberOfCustomFormats: Int
        get() = _customFormatRecords.size

    protected val numberOfExtendedFormats: Int
        get() = _xfRecords.size

    /**
     * Process this record ourselves, and then pass it on to our child listener
     */
    override fun processRecord(record: Record?) {
        // Handle it ourselves
        processRecordInternally(record)

        // Now pass on to our child
        _childListener.processRecord(record)
    }

    /**
     * Process the record ourselves, but do not pass it on to the child
     * Listener.
     * 
     * @param record
     */
    fun processRecordInternally(record: Record?) {
        if (record is FormatRecord) {
            val fr = record
            _customFormatRecords.put(fr.getIndexCode(), fr)
        }
        if (record is ExtendedFormatRecord) {
            val xr = record
            _xfRecords.add(xr)
        }
    }

    /**
     * Formats the given numeric of date Cell's contents as a String, in as
     * close as we can to the way that Excel would do so. Uses the various
     * format records to manage this.
     * 
     * TODO - move this to a central class in such a way that hssf.usermodel can
     * make use of it too
     */
    fun formatNumberDateCell(cell: CellValueRecordInterface?): String? {
        val value: Double
        if (cell is NumberRecord) {
            value = cell.getValue()
        } else if (cell is FormulaRecord) {
            value = cell.getValue()
        } else {
            throw IllegalArgumentException("Unsupported CellValue Record passed in " + cell)
        }

        // Get the built in format, if there is one
        val formatIndex = getFormatIndex(cell)
        val formatString = getFormatString(cell)

        if (formatString == null) {
            return _defaultFormat.format(value)
        }
        // Format, using the nice new
        // HSSFDataFormatter to do the work for us
        return _formatter.formatRawCellContents(value, formatIndex, formatString)
    }

    /**
     * Returns the format string, eg $##.##, for the given number format index.
     */
    fun getFormatString(formatIndex: Int): String? {
        var format: String? = null
        if (formatIndex >= HSSFDataFormat.numberOfBuiltinBuiltinFormats) {
            val tfr = _customFormatRecords.get(formatIndex)
            if (tfr == null) {
                System.err.println(
                    ("Requested format at index " + formatIndex
                            + ", but it wasn't found")
                )
            } else {
                format = tfr.getFormatString()
            }
        } else {
            format = HSSFDataFormat.getBuiltinFormat(formatIndex.toShort())
        }
        return format
    }

    /**
     * Returns the format string, eg $##.##, used by your cell
     */
    fun getFormatString(cell: CellValueRecordInterface): String? {
        val formatIndex = getFormatIndex(cell)
        if (formatIndex == -1) {
            // Not found
            return null
        }
        return getFormatString(formatIndex)
    }

    /**
     * Returns the index of the format string, used by your cell, or -1 if none
     * found
     */
    fun getFormatIndex(cell: CellValueRecordInterface): Int {
        val xfr = _xfRecords.get(cell.xFIndex.toInt())
        if (xfr == null) {
            System.err.println(
                ("Cell " + cell.row + "," + cell.column
                        + " uses XF with index " + cell.xFIndex + ", but we don't have that")
            )
            return -1
        }
        return xfr.getFormatIndex().toInt()
    }
}
