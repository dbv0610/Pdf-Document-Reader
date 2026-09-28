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

import com.wxiwei.office.fc.hssf.formula.FormulaShifter
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.CFHeaderRecord
import com.wxiwei.office.fc.hssf.record.CalcCountRecord
import com.wxiwei.office.fc.hssf.record.CalcModeRecord
import com.wxiwei.office.fc.hssf.record.CellValueRecordInterface
import com.wxiwei.office.fc.hssf.record.ColumnInfoRecord
import com.wxiwei.office.fc.hssf.record.ContinueRecord
import com.wxiwei.office.fc.hssf.record.DVALRecord
import com.wxiwei.office.fc.hssf.record.DefaultColWidthRecord
import com.wxiwei.office.fc.hssf.record.DefaultRowHeightRecord
import com.wxiwei.office.fc.hssf.record.DeltaRecord
import com.wxiwei.office.fc.hssf.record.DimensionsRecord
import com.wxiwei.office.fc.hssf.record.DrawingRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.EscherAggregate
import com.wxiwei.office.fc.hssf.record.FeatHdrRecord
import com.wxiwei.office.fc.hssf.record.FeatRecord
import com.wxiwei.office.fc.hssf.record.GridsetRecord
import com.wxiwei.office.fc.hssf.record.GutsRecord
import com.wxiwei.office.fc.hssf.record.IndexRecord
import com.wxiwei.office.fc.hssf.record.IterationRecord
import com.wxiwei.office.fc.hssf.record.MergeCellsRecord
import com.wxiwei.office.fc.hssf.record.NoteRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.PaneRecord
import com.wxiwei.office.fc.hssf.record.PrintGridlinesRecord
import com.wxiwei.office.fc.hssf.record.PrintHeadersRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.RefModeRecord
import com.wxiwei.office.fc.hssf.record.RowRecord
import com.wxiwei.office.fc.hssf.record.SCLRecord
import com.wxiwei.office.fc.hssf.record.SaveRecalcRecord
import com.wxiwei.office.fc.hssf.record.SelectionRecord
import com.wxiwei.office.fc.hssf.record.TextObjectRecord
import com.wxiwei.office.fc.hssf.record.UncalcedRecord
import com.wxiwei.office.fc.hssf.record.WSBoolRecord
import com.wxiwei.office.fc.hssf.record.WindowTwoRecord
import com.wxiwei.office.fc.hssf.record.aggregates.ChartSubstreamRecordAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.ColumnInfoRecordsAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.ConditionalFormattingTable
import com.wxiwei.office.fc.hssf.record.aggregates.CustomViewSettingsRecordAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.DataValidityTable
import com.wxiwei.office.fc.hssf.record.aggregates.MergedCellsTable
import com.wxiwei.office.fc.hssf.record.aggregates.PageSettingsBlock
import com.wxiwei.office.fc.hssf.record.aggregates.RecordAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.RecordAggregate.PositionTrackingVisitor
import com.wxiwei.office.fc.hssf.record.aggregates.RecordAggregate.RecordVisitor
import com.wxiwei.office.fc.hssf.record.aggregates.RowRecordsAggregate
import com.wxiwei.office.fc.hssf.record.aggregates.WorksheetProtectionBlock
import com.wxiwei.office.fc.hssf.record.chart.ChartRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFChart
import com.wxiwei.office.fc.hssf.util.ColumnInfo
import com.wxiwei.office.fc.hssf.util.HSSFPaneInformation
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.util.Internal
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.AbstractReader
import kotlin.math.max
import kotlin.math.min

/**
 * Low level model implementation of a Sheet (one workbook contains many sheets)
 * This file contains the low level binary records starting at the sheets BOF and
 * ending with the sheets EOF.  Use HSSFSheet for a high level representation.
 * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
</P> * <P>
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org) Gridlines, Headers, Footers, PrintSetup, and Setting Default Column Styles
 * @author Jason Height (jheight at chariot dot net dot au) Clone support. DBCell & Index Record writing support
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little)
 * 
 * @see InternalWorkbook
 * 
 * @see com.wxiwei.office.fc.hssf.usermodel.HSSFSheet
</P> */
@Internal
class InternalSheet {
    val records: MutableList<RecordBase>

    /**
     * Returns the PrintGridlinesRecord.
     */
    var printGridlines: PrintGridlinesRecord? = null
    protected var gridset: GridsetRecord? = null

    /**
     * Gets the gridset record for this sheet.
     */
    val gridsetRecord: GridsetRecord?
        get() = gridset
    private var _gutsRecord: GutsRecord? = null
    protected var defaultcolwidth: DefaultColWidthRecord? = DefaultColWidthRecord()
    protected var defaultrowheight: DefaultRowHeightRecord? = DefaultRowHeightRecord()
    private var _psBlock: PageSettingsBlock? = null

    /**
     * 'Worksheet Protection Block'<br></br>
     * Aggregate object is always present, but possibly empty.
     */
    private val _protectionBlock = WorksheetProtectionBlock()

    /**
     * @return the [WorksheetProtectionBlock] for this sheet
     */
    val protectionBlock: WorksheetProtectionBlock
        get() = _protectionBlock

    var windowTwo: WindowTwoRecord? = null
        protected set

    var selection: SelectionRecord? = null

    /** java object always present, but if empty no BIFF records are written  */
    private val _mergedCellsTable: MergedCellsTable

    /** always present in this POI object, not always written to Excel file  */ /*package*/
    var _columnInfos: ColumnInfoRecordsAggregate? = null

    /** the DimensionsRecord is always present  */
    private var _dimensions: DimensionsRecord? = null

    /** always present  */
    val rowsAggregate: RowRecordsAggregate?
    private var _dataValidityTable: DataValidityTable? = null
    private var condFormatting: ConditionalFormattingTable? = null

    private var rowRecIterator: MutableIterator<RowRecord?>? = null

    /**
     * whether an uncalced record must be inserted or not at generation
     */
    var uncalced: Boolean
        get() = _isUncalced
        set(uncalced) {
            this._isUncalced = uncalced
        }

    /** Add an UncalcedRecord if not true indicating formulas have not been calculated  */
    protected var _isUncalced: Boolean = false

    /**
     * worksheet or chartsheet
     */
    private var sheetType = BOFRecord.TYPE_WORKSHEET

    private constructor(rs: RecordStream, iAbortListener: AbstractReader?) {
        _mergedCellsTable = MergedCellsTable()
        var rra: RowRecordsAggregate? = null

        val records: MutableList<RecordBase> = ArrayList<RecordBase>(128)
        this.records =
            records // needed here due to calls to findFirstRecordLocBySid before we're done
        var dimsloc = -1

        if (rs.peekNextSid() != BOFRecord.sid.toInt()) {
            throw RuntimeException("BOF record expected")
        }
        val bof = rs.next as BOFRecord
        if (bof.type != BOFRecord.TYPE_WORKSHEET) {
            // TODO - fix junit tests throw new RuntimeException("Bad BOF record type");
            sheetType = bof.type
        }

        records.add(bof)
        while (rs.hasNext()) {
            if (iAbortListener != null && iAbortListener.isAborted()) {
                throw AbortReaderError("abort Reader")
            }

            val recSid = rs.peekNextSid()

            if (recSid == CFHeaderRecord.sid.toInt()) {
                condFormatting = ConditionalFormattingTable(rs)
                records.add(condFormatting!!)
                continue
            }

            if (recSid == ColumnInfoRecord.sid.toInt()) {
                _columnInfos = ColumnInfoRecordsAggregate(rs)
                records.add(_columnInfos!!)
                continue
            }
            if (recSid == DVALRecord.sid.toInt()) {
                _dataValidityTable = DataValidityTable(rs)
                records.add(_dataValidityTable!!)
                continue
            }

            if (RecordOrderer.isRowBlockRecord(recSid)) {
                //only add the aggregate once
                if (rra != null) {
                    throw RuntimeException("row/cell records found in the wrong place")
                }
                val rbr = RowBlocksReader(rs)
                _mergedCellsTable.addRecords(rbr.looseMergedCells)
                rra = RowRecordsAggregate(
                    rbr.plainRecordStream,
                    rbr.sharedFormulaManager
                )
                records.add(rra) //only add the aggregate once
                continue
            }

            if (CustomViewSettingsRecordAggregate.isBeginRecord(recSid)) {
                // This happens three times in test sample file "29982.xls"
                // Also several times in bugzilla samples 46840-23373 and 46840-23374
                records.add(CustomViewSettingsRecordAggregate(rs))
                continue
            }

            if (PageSettingsBlock.isComponentRecord(recSid)) {
                if (this._psBlock == null) {
                    // first PSB record encountered - read all of them:
                    this._psBlock = PageSettingsBlock(rs)
                    records.add(this._psBlock!!)
                } else {
                    // one or more PSB records found after some intervening non-PSB records
                    _psBlock!!.addLateRecords(rs)
                }
                // YK: in some cases records can be moved to the preceding
                // CustomViewSettingsRecordAggregate blocks
                _psBlock!!.positionRecords(records)
                continue
            }

            if (WorksheetProtectionBlock.isComponentRecord(recSid)) {
                _protectionBlock.addRecords(rs)
                continue
            }

            if (recSid == MergeCellsRecord.sid.toInt()) {
                // when the MergedCellsTable is found in the right place, we expect those records to be contiguous
                _mergedCellsTable.read(rs)
                continue
            }

            if (recSid == BOFRecord.sid.toInt()) {
                val chartAgg = ChartSubstreamRecordAggregate(rs)
                if (false) {
                    // TODO - would like to keep the chart aggregate packed, but one unit test needs attention
                    records.add(chartAgg)
                } else {
                    spillAggregate(chartAgg, records)
                }
                continue
            }

            val rec = rs.next
            if (recSid == IndexRecord.sid.toInt()) {
                // ignore INDEX record because it is only needed by Excel,
                // and POI always re-calculates its contents
                continue
            }

            if (recSid == UncalcedRecord.sid.toInt()) {
                // don't add UncalcedRecord to the list
                _isUncalced = true // this flag is enough
                continue
            }

            if (recSid == FeatRecord.sid.toInt() || recSid == FeatHdrRecord.sid.toInt()) {
                records.add(rec!!)
                continue
            }

            if (recSid == EOFRecord.sid.toInt()) {
                records.add(rec!!)
                break
            }

            if (recSid == DimensionsRecord.sid.toInt()) {
                // Make a columns aggregate if one hasn't ready been created.
                if (_columnInfos == null) {
                    _columnInfos = ColumnInfoRecordsAggregate()
                    records.add(_columnInfos!!)
                }

                _dimensions = rec as DimensionsRecord?
                dimsloc = records.size
            } else if (recSid == DefaultColWidthRecord.sid.toInt()) {
                defaultcolwidth = rec as DefaultColWidthRecord?
            } else if (recSid == DefaultRowHeightRecord.sid.toInt()) {
                defaultrowheight = rec as DefaultRowHeightRecord?
            } else if (recSid == PrintGridlinesRecord.sid.toInt()) {
                this.printGridlines = rec as PrintGridlinesRecord?
            } else if (recSid == GridsetRecord.sid.toInt()) {
                gridset = rec as GridsetRecord?
            } else if (recSid == SelectionRecord.sid.toInt()) {
                this.selection = rec as SelectionRecord?
            } else if (recSid == WindowTwoRecord.sid.toInt()) {
                this.windowTwo = rec as WindowTwoRecord?
            } else if (recSid == GutsRecord.sid.toInt()) {
                _gutsRecord = rec as GutsRecord?
            }

            records.add(rec!!)
        }
        if (this.windowTwo == null) {
            throw RuntimeException("WINDOW2 was not found")
        }
        if (_dimensions == null) {
            // Excel seems to always write the DIMENSION record, but tolerates when it is not present
            // in all cases Excel (2007) adds the missing DIMENSION record
            if (rra == null) {
                // bug 46206 alludes to files which skip the DIMENSION record
                // when there are no row/cell records.
                // Not clear which application wrote these files.
                rra = RowRecordsAggregate()
            } else {
                log.log(POILogger.WARN, "DIMENSION record not found even though row/cells present")
                // Not sure if any tools write files like this, but Excel reads them OK
            }
            dimsloc = findFirstRecordLocBySid(WindowTwoRecord.sid)
            _dimensions = rra.createDimensions()
            records.add(dimsloc, _dimensions!!)
        }
        if (rra == null) {
            rra = RowRecordsAggregate()
            records.add(dimsloc + 1, rra)
        }
        this.rowsAggregate = rra
        // put merged cells table in the right place (regardless of where the first MergedCellsRecord was found */
        RecordOrderer.addNewSheetRecord(records, _mergedCellsTable)
        RecordOrderer.addNewSheetRecord(records, _protectionBlock)
        if (log.check(POILogger.DEBUG)) log.log(
            POILogger.DEBUG,
            "sheet createSheet (existing file) exited"
        )
    }

    val isChartSheet: Boolean
        get() = sheetType == BOFRecord.TYPE_CHART

    private class RecordCloner(private val _destList: MutableList<Record?>) : RecordVisitor {
        override fun visitRecord(r: Record) {
            _destList.add(r.clone() as Record)
        }
    }

    /**
     * Clones the low level records of this sheet and returns the new sheet instance.
     * This method is implemented by adding methods for deep cloning to all records that
     * can be added to a sheet. The **Record** object does not implement cloneable.
     * When adding a new record, implement a public clone method if and only if the record
     * belongs to a sheet.
     */
    fun cloneSheet(): InternalSheet {
        val clonedRecords: MutableList<Record?> = ArrayList<Record?>(
            records.size
        )
        for (i in records.indices) {
            val rb = records.get(i)
            if (rb is RecordAggregate) {
                rb.visitContainedRecords(RecordCloner(clonedRecords))
                continue
            }
            val rec = (rb as Record).clone() as Record
            clonedRecords.add(rec)
        }
        return createSheet(RecordStream(clonedRecords, 0))
    }

    private constructor() {
        _mergedCellsTable = MergedCellsTable()
        val records: MutableList<RecordBase> = ArrayList<RecordBase>(32)

        if (log.check(POILogger.DEBUG)) log.log(
            POILogger.DEBUG,
            "Sheet createsheet from scratch called"
        )

        records.add(createBOF())

        records.add(createCalcMode())
        records.add(createCalcCount())
        records.add(createRefMode())
        records.add(createIteration())
        records.add(createDelta())
        records.add(createSaveRecalc())
        records.add(createPrintHeaders())
        this.printGridlines = createPrintGridlines()
        records.add(this.printGridlines!!)
        gridset = createGridset()
        records.add(gridset!!)
        _gutsRecord = createGuts()
        records.add(_gutsRecord!!)
        defaultrowheight = createDefaultRowHeight()
        records.add(defaultrowheight!!)
        records.add(createWSBool())

        // 'Page Settings Block'
        this._psBlock = PageSettingsBlock()
        records.add(this._psBlock!!)

        // 'Worksheet Protection Block' (after 'Page Settings Block' and before DEFCOLWIDTH)
        records.add(_protectionBlock) // initially empty

        defaultcolwidth = createDefaultColWidth()
        records.add(defaultcolwidth!!)
        val columns = ColumnInfoRecordsAggregate()
        records.add(columns)
        _columnInfos = columns
        _dimensions = createDimensions()
        records.add(_dimensions!!)
        this.rowsAggregate = RowRecordsAggregate()
        records.add(this.rowsAggregate)
        // 'Sheet View Settings'
        records.add(createWindowTwo().also { this.windowTwo = it })
        this.selection = createSelection()
        records.add(this.selection!!)

        records.add(_mergedCellsTable) // MCT comes after 'Sheet View Settings'
        records.add(EOFRecord.instance)

        this.records = records
        if (log.check(POILogger.DEBUG)) log.log(
            POILogger.DEBUG,
            "Sheet createsheet from scratch exit"
        )
    }

    /**
     * Updates formulas in cells and conditional formats due to moving of cells
     * @param externSheetIndex the externSheet index of this sheet
     */
    fun updateFormulasAfterCellShift(shifter: FormulaShifter?, externSheetIndex: Int) {
        this.rowsAggregate!!.updateFormulasAfterRowShift(shifter, externSheetIndex)
        if (condFormatting != null) {
            this.conditionalFormattingTable.updateFormulasAfterCellShift(
                shifter,
                externSheetIndex
            )
        }
        // TODO - adjust data validations
    }

    fun addMergedRegion(rowFrom: Int, colFrom: Int, rowTo: Int, colTo: Int): Int {
        // Validate input
        require(rowTo >= rowFrom) {
            ("The 'to' row (" + rowTo
                    + ") must not be less than the 'from' row (" + rowFrom + ")")
        }
        require(colTo >= colFrom) {
            ("The 'to' col (" + colTo
                    + ") must not be less than the 'from' col (" + colFrom + ")")
        }

        val mrt = this._mergedCellsTable
        mrt.addArea(rowFrom, colFrom, rowTo, colTo)
        return mrt.numberOfMergedRegions - 1
    }

    fun removeMergedRegion(index: Int) {
        //safety checks
        val mrt = this._mergedCellsTable
        if (index >= mrt.numberOfMergedRegions) {
            return
        }
        mrt.remove(index)
    }

    fun getMergedRegionAt(index: Int): HSSFCellRangeAddress? {
        //safety checks
        val mrt = this._mergedCellsTable
        if (index >= mrt.numberOfMergedRegions) {
            return null
        }
        return mrt.get(index)
    }

    val numMergedRegions: Int
        get() = this._mergedCellsTable.numberOfMergedRegions

    val conditionalFormattingTable: ConditionalFormattingTable
        get() {
            var cft = condFormatting
            if (cft == null) {
                cft = ConditionalFormattingTable()
                condFormatting = cft
                RecordOrderer.addNewSheetRecord(this.records, cft)
            }
            return cft
        }

    /**
     * Per an earlier reported bug in working with Andy Khan's excel read library.  This
     * sets the values in the sheet's DimensionsRecord object to be correct.  Excel doesn't
     * really care, but we want to play nice with other libraries.
     * 
     * @see DimensionsRecord
     */
    fun setDimensions(firstrow: Int, firstcol: Short, lastrow: Int, lastcol: Short) {
        if (log.check(POILogger.DEBUG)) {
            log.log(POILogger.DEBUG, "Sheet.setDimensions")
            log.log(
                POILogger.DEBUG,
                (StringBuffer("firstrow")).append(firstrow).append("firstcol")
                    .append(firstcol.toInt())
                    .append("lastrow").append(lastrow).append("lastcol").append(lastcol.toInt())
                    .toString()
            )
        }
        _dimensions!!.setFirstCol(firstcol)
        _dimensions!!.setFirstRow(firstrow)
        _dimensions!!.setLastCol(lastcol)
        _dimensions!!.setLastRow(lastrow)
        if (log.check(POILogger.DEBUG)) log.log(POILogger.DEBUG, "Sheet.setDimensions exiting")
    }

    fun visitContainedRecords(rv: RecordVisitor, offset: Int) {
        val ptv = PositionTrackingVisitor(rv, offset)

        var haveSerializedIndex = false

        for (k in records.indices) {
            val record = records.get(k)

            if (record is RecordAggregate) {
                val agg = record
                agg.visitContainedRecords(ptv)
            } else {
                ptv.visitRecord(record as Record)
            }

            // If the BOF record was just serialized then add the IndexRecord
            if (record is BOFRecord) {
                if (!haveSerializedIndex) {
                    haveSerializedIndex = true
                    // Add an optional UncalcedRecord. However, we should add
                    //  it in only the once, after the sheet's own BOFRecord.
                    // If there are diagrams, they have their own BOFRecords,
                    //  and one shouldn't go in after that!
                    if (_isUncalced) {
                        ptv.visitRecord(UncalcedRecord())
                    }
                    //Can there be more than one BOF for a sheet? If not then we can
                    //remove this guard. So be safe it is left here.
                    if (this.rowsAggregate != null) {
                        // find forward distance to first RowRecord
                        val initRecsSize = getSizeOfInitialSheetRecords(k)
                        val currentPos = ptv.position
                        ptv.visitRecord(rowsAggregate.createIndexRecord(currentPos, initRecsSize))
                    }
                }
            }
        }
    }

    /**
     * 'initial sheet records' are between INDEX and the 'Row Blocks'
     * @param bofRecordIndex index of record after which INDEX record is to be placed
     * @return count of bytes from end of INDEX record to first ROW record.
     */
    private fun getSizeOfInitialSheetRecords(bofRecordIndex: Int): Int {
        var result = 0
        // start just after BOF record (INDEX is not present in this list)
        for (j in bofRecordIndex + 1..<records.size) {
            val tmpRec = records.get(j)
            if (tmpRec is RowRecordsAggregate) {
                break
            }
            result += tmpRec.getRecordSize()
        }
        if (_isUncalced) {
            result += UncalcedRecord.getStaticRecordSize()
        }
        return result
    }

    /**
     * Adds a value record to the sheet's contained binary records
     * (i.e. LabelSSTRecord or NumberRecord).
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     * 
     * @param row the row to add the cell value to
     * @param col the cell value record itself.
    </P> */
    fun addValueRecord(row: Int, col: CellValueRecordInterface) {
        if (log.check(POILogger.DEBUG)) {
            log.log(POILogger.DEBUG, "add value record  row" + row)
        }
        val d = _dimensions

        if (col.column > d!!.getLastCol()) {
            d.setLastCol((col.column + 1).toShort())
        }
        if (col.column < d.getFirstCol()) {
            d.setFirstCol(col.column)
        }
        rowsAggregate!!.insertCell(col)
    }

    /**
     * remove a value record from the records array.
     * 
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     * 
     * @param row - the row of the value record you wish to remove
     * @param col - a record supporting the CellValueRecordInterface.
     * @see CellValueRecordInterface
     */
    fun removeValueRecord(row: Int, col: CellValueRecordInterface?) {
        log.logFormatted(POILogger.DEBUG, "remove value record row %", intArrayOf(row))
        rowsAggregate!!.removeCell(col)
    }

    /**
     * replace a value record from the records array.
     * 
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     * 
     * @param newval - a record supporting the CellValueRecordInterface.  this will replace
     * the cell value with the same row and column.  If there isn't one, one will
     * be added.
     */
    fun replaceValueRecord(newval: CellValueRecordInterface?) {
        if (log.check(POILogger.DEBUG)) log.log(POILogger.DEBUG, "replaceValueRecord ")
        //The ValueRecordsAggregate use a tree map underneath.
        //The tree Map uses the CellValueRecordInterface as both the
        //key and the value, if we dont do a remove, then
        //the previous instance of the key is retained, effectively using
        //double the memory
        rowsAggregate!!.removeCell(newval)
        rowsAggregate.insertCell(newval!!)
    }

    /**
     * Adds a row record to the sheet
     * 
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.
     * 
     * @param row the row record to be added
    </P> */
    fun addRow(row: RowRecord) {
        if (log.check(POILogger.DEBUG)) log.log(POILogger.DEBUG, "addRow ")
        val d = _dimensions

        if (row.getRowNumber() >= d!!.getLastRow()) {
            d.setLastRow(row.getRowNumber() + 1)
        }
        if (row.getRowNumber() < d.getFirstRow()) {
            d.setFirstRow(row.getRowNumber())
        }

        //If the row exists remove it, so that any cells attached to the row are removed
        val existingRow = rowsAggregate!!.getRow(row.getRowNumber())
        if (existingRow != null) {
            rowsAggregate.removeRow(existingRow)
        }

        rowsAggregate.insertRow(row)

        if (log.check(POILogger.DEBUG)) log.log(POILogger.DEBUG, "exit addRow")
    }

    /**
     * Removes a row record
     * 
     * This method is not loc sensitive, it resets loc to = dimsloc so no worries.
     * 
     * @param row  the row record to remove
     */
    fun removeRow(row: RowRecord) {
        rowsAggregate!!.removeRow(row)
    }

    val cellValueIterator: MutableIterator<CellValueRecordInterface?>
        /**
         * Get all the value records (from LOC). Records will be returned from the first
         * record (starting at LOC) which is a value record.
         * 
         * <P>
         * This method is "loc" sensitive.  Meaning you need to set LOC to where you
         * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
         * When adding several rows you can just start at the last one by leaving loc
         * at what this sets it to.  For this method, set loc to dimsloc to start with,
         * subsequent calls will return values in (physical) sequence or NULL when you get to the end.
         * 
         * @return Iterator of CellValueRecordInterface representing the value records
        </P> */
        get() = rowsAggregate!!.cellValueIterator

    @get:Deprecated("use {@link #getCellValueIterator()} instead")
    val valueRecords: Array<CellValueRecordInterface?>
        /**
         * Get all the value records (from LOC). Records will be returned from the first
         * record (starting at LOC) which is a value record.
         * 
         * <P>
         * This method is "loc" sensitive.  Meaning you need to set LOC to where you
         * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
         * When adding several rows you can just start at the last one by leaving loc
         * at what this sets it to.  For this method, set loc to dimsloc to start with,
         * subsequent calls will return values in (physical) sequence or NULL when you get to the end.
         * 
         * @return Array of CellValueRecordInterface representing the remaining value records
        </P> */
        get() = rowsAggregate!!.valueRecords

    val nextRow: RowRecord?
        /**
         * get the NEXT RowRecord (from LOC).  The first record that is a Row record
         * (starting at LOC) will be returned.
         * <P>
         * This method is "loc" sensitive.  Meaning you need to set LOC to where you
         * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
         * When adding several rows you can just start at the last one by leaving loc
         * at what this sets it to.  For this method, set loc to dimsloc to start with.
         * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
         * 
         * @return RowRecord representing the next row record or NULL if there are no more
        </P> */
        get() {
            if (this.rowRecIterator == null) {
                this.rowRecIterator = rowsAggregate!!.iterator
            }
            if (!rowRecIterator!!.hasNext()) {
                return null
            }
            val rec: RowRecord? = rowRecIterator!!.next()
            rowRecIterator!!.remove()
            return rec
        }

    /**
     * get the NEXT (from LOC) RowRecord where rownumber matches the given rownum.
     * The first record that is a Row record (starting at LOC) that has the
     * same rownum as the given rownum will be returned.
     * <P>
     * This method is "loc" sensitive.  Meaning you need to set LOC to where you
     * want it to start searching.  If you don't know do this: setLoc(getDimsLoc).
     * When adding several rows you can just start at the last one by leaving loc
     * at what this sets it to.  For this method, set loc to dimsloc to start with.
     * subsequent calls will return rows in (physical) sequence or NULL when you get to the end.
     * 
     * @param rownum   which row to return (careful with LOC)
     * @return RowRecord representing the next row record or NULL if there are no more
    </P> */
    fun getRow(rownum: Int): RowRecord? {
        return rowsAggregate!!.getRow(rownum)
    }

    private val gutsRecord: GutsRecord
        get() {
            if (_gutsRecord == null) {
                val result: GutsRecord = createGuts()
                RecordOrderer.addNewSheetRecord(this.records, result)
                _gutsRecord = result
            }

            return _gutsRecord!!
        }

    var defaultColumnWidth: Int
        /**
         * get the default column width for the sheet (if the columns do not define their own width)
         * @return default column width
         */
        get() = defaultcolwidth!!.getColWidth()
        /**
         * set the default column width for the sheet (if the columns do not define their own width)
         * @param dcw  default column width
         */
        set(dcw) {
            defaultcolwidth!!.setColWidth(dcw)
        }

    var isGridsPrinted: Boolean
        /**
         * @return `true` if gridlines are printed
         */
        get() {
            if (gridset == null) {
                gridset = createGridset()
                //Insert the newlycreated Gridset record at the end of the record (just before the EOF)
                val loc = findFirstRecordLocBySid(EOFRecord.sid)
                records.add(loc, gridset!!)
            }
            return !gridset!!.getGridset()
        }
        /**
         * set whether gridlines printed or not.
         * @param value     True if gridlines printed.
         */
        set(value) {
            gridset!!.setGridset(!value)
        }

    var defaultRowHeight: Short
        /**
         * get the default row height for the sheet (if the rows do not define their own height)
         * @return  default row height
         */
        get() = defaultrowheight!!.getRowHeight()
        /**
         * set the default row height for the sheet (if the rows do not define their own height)
         */
        set(dch) {
            defaultrowheight!!.setRowHeight(dch)
        }

    /**
     * get the width of a given column in units of 1/256th of a character width
     * @param columnIndex index
     * @see DefaultColWidthRecord
     * 
     * @see ColumnInfoRecord
     * 
     * @see .setColumnWidth
     * @return column width in units of 1/256th of a character width
     */
    fun getColumnWidth(columnIndex: Int): Int {
        val ci = _columnInfos!!.findColumnInfo(columnIndex)
        if (ci != null) {
            return ci.columnWidth
        }
        //default column width is measured in characters
        //multiply
        return (256 * defaultcolwidth!!.getColWidth())
    }

    /**
     * 得到像素单位的列宽
     */
    fun getColumnPixelWidth(columnIndex: Int): Int {
        val ci = _columnInfos!!.findColumnInfo(columnIndex)
        if (ci != null) {
            return ci.colPixelWidth
        }
        // 默认80像素
        return 80
    }

    /**
     * 设置像素单位的列宽
     */
    fun setColumnPixelWidth(columnIndex: Int, width: Int) {
        val ci = _columnInfos!!.findColumnInfo(columnIndex)
        if (ci != null) {
            ci.colPixelWidth = width
        }
    }

    /**
     * get the index to the ExtendedFormatRecord "associated" with
     * the column at specified 0-based index. (In this case, an
     * ExtendedFormatRecord index is actually associated with a
     * ColumnInfoRecord which spans 1 or more columns)
     * <br></br>
     * Returns the index to the default ExtendedFormatRecord (0xF)
     * if no ColumnInfoRecord exists that includes the column
     * index specified.
     * @param columnIndex
     * @return index of ExtendedFormatRecord associated with
     * ColumnInfoRecord that includes the column index or the
     * index of the default ExtendedFormatRecord (0xF)
     */
    fun getXFIndexForColAt(columnIndex: Short): Short {
        val ci = _columnInfos!!.findColumnInfo(columnIndex.toInt())
        if (ci != null) {
            return ci.xFIndex.toShort()
        }
        return 0xF
    }

    /**
     * set the width for a given column in 1/256th of a character width units
     * 
     * @param column -
     * the column number
     * @param width
     * (in units of 1/256th of a character width)
     */
    fun setColumnWidth(column: Int, width: Int) {
        require(width <= 255 * 256) { "The maximum column width for an individual cell is 255 characters." }

        setColumn(column, null, width, null, null, null)
    }

    /**
     * Get the hidden property for a given column.
     * @param columnIndex column index
     * @see DefaultColWidthRecord
     * 
     * @see ColumnInfoRecord
     * 
     * @see .setColumnHidden
     * @return whether the column is hidden or not.
     */
    fun isColumnHidden(columnIndex: Int): Boolean {
        val cir = _columnInfos!!.findColumnInfo(columnIndex)
        if (cir == null) {
            return false
        }
        return cir.hidden
    }

    val columnInfo: MutableList<ColumnInfo?>?
        /**
         * get the column information
         * @return
         */
        get() {
            if (_columnInfos == null) {
                return null
            }

            val size = _columnInfos!!.numColumns
            val colInfoList: MutableList<ColumnInfo?> =
                ArrayList<ColumnInfo?>(5)
            var columnInfo: ColumnInfoRecord
            for (i in 0..<size) {
                columnInfo = _columnInfos!!.getColInfo(i)
                colInfoList.add(
                    ColumnInfo(
                        columnInfo.firstColumn,
                        columnInfo.lastColumn,
                        columnInfo.columnWidth,
                        columnInfo.xFIndex,
                        columnInfo.hidden
                    )
                )
            }

            return colInfoList
        }

    /**
     * Get the hidden property for a given column.
     * @param column - the column number
     * @param hidden - whether the column is hidden or not
     */
    fun setColumnHidden(column: Int, hidden: Boolean) {
        setColumn(column, null, null, null, hidden, null)
    }

    fun setDefaultColumnStyle(column: Int, styleIndex: Int) {
        setColumn(column, styleIndex.toShort(), null, null, null, null)
    }

    private fun setColumn(
        column: Int, xfStyle: Short?, width: Int?, level: Int?, hidden: Boolean?,
        collapsed: Boolean?
    ) {
        _columnInfos!!.setColumn(column, xfStyle, width, level, hidden, collapsed)
    }

    /**
     * Creates an outline group for the specified columns.
     * @param fromColumn    group from this column (inclusive)
     * @param toColumn      group to this column (inclusive)
     * @param indent        if true the group will be indented by one level,
     * if false indenting will be removed by one level.
     */
    fun groupColumnRange(fromColumn: Int, toColumn: Int, indent: Boolean) {
        // Set the level for each column

        _columnInfos!!.groupColumnRange(fromColumn, toColumn, indent)

        // Determine the maximum overall level
        val maxLevel = _columnInfos!!.maxOutlineLevel

        val guts = this.gutsRecord
        guts.setColLevelMax((maxLevel + 1).toShort())
        if (maxLevel == 0) {
            guts.setTopColGutter(0.toShort())
        } else {
            guts.setTopColGutter((29 + (12 * (maxLevel - 1))).toShort())
        }
    }

    var topRow: Short
        get() = if (this.windowTwo == null) 0.toShort() else windowTwo!!.getTopRow()
        set(topRow) {
            if (this.windowTwo != null) {
                windowTwo!!.setTopRow(topRow)
            }
        }

    var leftCol: Short
        get() = if (this.windowTwo == null) 0.toShort() else windowTwo!!.getLeftCol()
        /**
         * Sets the left column to show in desktop window pane.
         * @param leftCol the left column to show in desktop window pane
         */
        set(leftCol) {
            if (this.windowTwo != null) {
                windowTwo!!.setLeftCol(leftCol)
            }
        }

    var activeCellRow: Int
        /**
         * Returns the active row
         * 
         * @see SelectionRecord
         * 
         * @return row the active row index
         */
        get() {
            if (this.selection == null) {
                return 0
            }
            return selection!!.getActiveCellRow()
        }
        /**
         * Sets the active row
         * 
         * @param row the row index
         * @see SelectionRecord
         */
        set(row) {
            //shouldn't have a sheet w/o a SelectionRecord, but best to guard anyway
            if (this.selection != null) {
                selection!!.setActiveCellRow(row)
            }
        }

    var activeCellCol: Short
        /**
         * @see SelectionRecord
         * 
         * @return column of the active cell
         */
        get() {
            if (this.selection == null) {
                return 0
            }
            return selection!!.getActiveCellCol().toShort()
        }
        /**
         * Sets the active column
         * 
         * @param col the column index
         * @see SelectionRecord
         */
        set(col) {
            //shouldn't have a sheet w/o a SelectionRecord, but best to guard anyway
            if (this.selection != null) {
                selection!!.setActiveCellCol(col)
            }
        }

    /**
     * Returns the first occurrence of a record matching a particular sid.
     */
    fun findFirstRecordBySid(sid: Short): Record? {
        val ix = findFirstRecordLocBySid(sid)
        if (ix < 0) {
            return null
        }
        return records.get(ix) as Record?
    }

    /**
     * Sets the SCL record or creates it in the correct place if it does not
     * already exist.
     * 
     * @param sclRecord     The record to set.
     */
    fun setSCLRecord(sclRecord: SCLRecord?) {
        val oldRecordLoc = findFirstRecordLocBySid(SCLRecord.sid)
        if (oldRecordLoc == -1) {
            // Insert it after the window record
            val windowRecordLoc = findFirstRecordLocBySid(WindowTwoRecord.sid)
            records.add(windowRecordLoc + 1, sclRecord!!)
        } else {
            records.set(oldRecordLoc, sclRecord!!)
        }
    }

    /**
     * Finds the first occurrence of a record matching a particular sid and
     * returns it's position.
     * @param sid   the sid to search for
     * @return  the record position of the matching record or -1 if no match
     * is made.
     */
    fun findFirstRecordLocBySid(sid: Short): Int { // TODO - remove this method
        val max = records.size
        for (i in 0..<max) {
            val rb: Any? = records.get(i)
            if (rb !is Record) {
                continue
            }
            val record = rb
            if (record.getSid() == sid) {
                return i
            }
        }
        return -1
    }

    /**
     * Sets whether the sheet is selected
     * @param sel True to select the sheet, false otherwise.
     */
    fun setSelected(sel: Boolean) {
        windowTwo!!.setSelected(sel)
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * 
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * 
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    fun createFreezePane(colSplit: Int, rowSplit: Int, topRow: Int, leftmostColumn: Int) {
        val paneLoc = findFirstRecordLocBySid(PaneRecord.sid)
        if (paneLoc != -1) records.removeAt(paneLoc)

        // If both colSplit and rowSplit are zero then the existing freeze pane is removed
        if (colSplit == 0 && rowSplit == 0) {
            windowTwo!!.setFreezePanes(false)
            windowTwo!!.setFreezePanesNoSplit(false)
            val sel = findFirstRecordBySid(SelectionRecord.sid) as SelectionRecord?
            sel!!.setPane(HSSFPaneInformation.PANE_UPPER_LEFT)
            return
        }

        val loc = findFirstRecordLocBySid(WindowTwoRecord.sid)
        val pane = PaneRecord()
        pane.setX(colSplit.toShort())
        pane.setY(rowSplit.toShort())
        pane.setTopRow(topRow.toShort())
        pane.setLeftColumn(leftmostColumn.toShort())
        if (rowSplit == 0) {
            pane.setTopRow(0.toShort())
            pane.setActivePane(1.toShort())
        } else if (colSplit == 0) {
            pane.setLeftColumn(0.toShort())
            pane.setActivePane(2.toShort())
        } else {
            pane.setActivePane(0.toShort())
        }
        records.add(loc + 1, pane)

        windowTwo!!.setFreezePanes(true)
        windowTwo!!.setFreezePanesNoSplit(true)

        val sel = findFirstRecordBySid(SelectionRecord.sid) as SelectionRecord?
        sel!!.setPane(pane.getActivePane().toByte())
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     * PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see .PANE_LOWER_LEFT
     * 
     * @see .PANE_LOWER_RIGHT
     * 
     * @see .PANE_UPPER_LEFT
     * 
     * @see .PANE_UPPER_RIGHT
     */
    fun createSplitPane(
        xSplitPos: Int, ySplitPos: Int, topRow: Int, leftmostColumn: Int,
        activePane: Int
    ) {
        val paneLoc = findFirstRecordLocBySid(PaneRecord.sid)
        if (paneLoc != -1) records.removeAt(paneLoc)

        val loc = findFirstRecordLocBySid(WindowTwoRecord.sid)
        val r = PaneRecord()
        r.setX(xSplitPos.toShort())
        r.setY(ySplitPos.toShort())
        r.setTopRow(topRow.toShort())
        r.setLeftColumn(leftmostColumn.toShort())
        r.setActivePane(activePane.toShort())
        records.add(loc + 1, r)

        windowTwo!!.setFreezePanes(false)
        windowTwo!!.setFreezePanesNoSplit(false)

        val sel = findFirstRecordBySid(SelectionRecord.sid) as SelectionRecord?
        sel!!.setPane(PANE_LOWER_RIGHT)
    }

    val paneInformation: HSSFPaneInformation?
        /**
         * Returns the information regarding the currently configured pane (split or freeze).
         * @return `null` if no pane configured, or the pane information.
         */
        get() {
            val rec = findFirstRecordBySid(PaneRecord.sid) as PaneRecord?
            if (rec == null) return null

            return HSSFPaneInformation(
                rec.getX(), rec.getY(), rec.getTopRow(), rec.getLeftColumn(),
                rec.getActivePane().toByte(), windowTwo!!.getFreezePanes()
            )
        }

    var isDisplayGridlines: Boolean
        /**
         * @return `true` if gridlines are displayed
         */
        get() = windowTwo!!.getDisplayGridlines()
        /**
         * Sets whether the gridlines are shown in a viewer.
         * @param show whether to show gridlines or not
         */
        set(show) {
            windowTwo!!.setDisplayGridlines(show)
        }

    var isDisplayFormulas: Boolean
        /**
         * Returns if formulas are displayed.
         * @return whether formulas are displayed
         */
        get() = windowTwo!!.getDisplayFormulas()
        /**
         * Sets whether the formulas are shown in a viewer.
         * @param show whether to show formulas or not
         */
        set(show) {
            windowTwo!!.setDisplayFormulas(show)
        }

    var isDisplayRowColHeadings: Boolean
        /**
         * Returns if RowColHeadings are displayed.
         * @return whether RowColHeadings are displayed
         */
        get() = windowTwo!!.getDisplayRowColHeadings()
        /**
         * Sets whether the RowColHeadings are shown in a viewer.
         * @param show whether to show RowColHeadings or not
         */
        set(show) {
            windowTwo!!.setDisplayRowColHeadings(show)
        }

    val chart: HSSFChart?
        /**
         * 
         * @return
         */
        get() {
            if (sheetType == BOFRecord.TYPE_CHART) {
                var loc = findFirstRecordLocBySid(ChartRecord.sid)
                if (loc >= 0) {
                    val chartRecordsList: MutableList<Record> =
                        ArrayList<Record>()
                    var record = records.get(loc)
                    while (record !is WorksheetProtectionBlock) {
                        chartRecordsList.add(record as Record)

                        loc++
                        record = records.get(loc)
                    }

                    val chart = HSSFChart(null, null, null, null)
                    HSSFChart.convertRecordsToChart(chartRecordsList, chart)
                    return chart
                }
            }

            return null
        }

    /**
     * Finds the DrawingRecord for our sheet, and
     * attaches it to the DrawingManager (which knows about
     * the overall DrawingGroup for our workbook).
     * If requested, will create a new DrawRecord
     * if none currently exist
     * @param drawingManager The DrawingManager2 for our workbook
     * @param createIfMissing Should one be created if missing?
     */
    fun aggregateDrawingRecords(drawingManager: DrawingManager2, createIfMissing: Boolean): Int {
        var loc = findFirstRecordLocBySid(DrawingRecord.sid)
        val noDrawingRecordsFound = (loc == -1)
        if (noDrawingRecordsFound) {
            if (!createIfMissing) {
                // None found, and not allowed to add in
                return -1
            }

            val aggregate = EscherAggregate(drawingManager)
            loc = findFirstRecordLocBySid(EscherAggregate.sid)
            if (loc == -1) {
                loc = findFirstRecordLocBySid(WindowTwoRecord.sid)
            } else {
                this.records.removeAt(loc)
            }
            this.records.add(loc, aggregate)
            return loc
        }
        val records =
            this.records
        val r = EscherAggregate.createAggregate(records, loc, drawingManager)

        val startloc = loc
        while (loc + 1 < records.size && (records.get(loc) is DrawingRecord || records.get(loc) is ContinueRecord)
            && (records.get(loc + 1) is ObjRecord || records.get(loc + 1) is TextObjectRecord)
        ) {
//            loc += 2;
//            if (records.get(loc) instanceof NoteRecord)
//              
            loc += EscherAggregate.shapeContainRecords(records, loc)
        }

        val endloc = loc - 1
        for (i in 0..<(endloc - startloc + 1)) records.removeAt(startloc)
        records.add(startloc, r)

        return startloc
    }

    /**
     * Perform any work necessary before the sheet is about to be serialized.
     * For instance the escher aggregates size needs to be calculated before
     * serialization so that the dgg record (which occurs first) can be written.
     */
    fun preSerialize() {
        for (r in this.records) {
            if (r is EscherAggregate) {
                // Trigger flattening of user model and corresponding update of dgg record.
                r.getRecordSize()
            }
        }
    }

    val pageSettings: PageSettingsBlock
        get() {
            var psb = this._psBlock
            if (psb == null) {
                psb = PageSettingsBlock()
                this._psBlock = psb
                RecordOrderer.addNewSheetRecord(this.records, psb)
            }
            return psb
        }

    fun setColumnGroupCollapsed(columnNumber: Int, collapsed: Boolean) {
        if (collapsed) {
            _columnInfos!!.collapseColumn(columnNumber)
        } else {
            _columnInfos!!.expandColumn(columnNumber)
        }
    }

    fun groupRowRange(fromRow: Int, toRow: Int, indent: Boolean) {
        for (rowNum in fromRow..toRow) {
            var row = getRow(rowNum)
            if (row == null) {
                row = RowRecordsAggregate.createRow(rowNum)
                addRow(row)
            }
            var level = row.getOutlineLevel().toInt()
            if (indent) level++
            else level--
            level = max(0, level)
            level = min(7, level)
            row.setOutlineLevel((level).toShort())
        }

        recalcRowGutter()
    }

    private fun recalcRowGutter() {
        var maxLevel = 0
        val iterator: MutableIterator<*> = rowsAggregate!!.iterator
        while (iterator.hasNext()) {
            val rowRecord = iterator.next() as RowRecord
            maxLevel = max(rowRecord.getOutlineLevel().toInt(), maxLevel)
        }

        // Grab the guts record, adding if needed
        val guts = this.gutsRecord
        // Set the levels onto it
        guts.setRowLevelMax((maxLevel + 1).toShort())
        guts.setLeftRowGutter((29 + (12 * (maxLevel))).toShort())
    }

    val orCreateDataValidityTable: DataValidityTable
        get() {
            if (_dataValidityTable == null) {
                val result = DataValidityTable()
                RecordOrderer.addNewSheetRecord(this.records, result)
                _dataValidityTable = result
            }
            return _dataValidityTable!!
        }

    val noteRecords: Array<NoteRecord?>
        /**
         * Get the [NoteRecord]s (related to cell comments) for this sheet
         * @return never `null`, typically empty array
         */
        get() {
            val temp: MutableList<NoteRecord?> =
                ArrayList<NoteRecord?>()
            for (i in records.indices.reversed()) {
                val rec = records.get(i)
                if (rec is NoteRecord) {
                    temp.add(rec)
                }
            }
            if (temp.size < 1) {
                return NoteRecord.EMPTY_ARRAY
            }
            return temp.toTypedArray()
        }

    fun dispose() {
        records.clear()

        this.printGridlines = null
        gridset = null
        _gutsRecord = null
        defaultcolwidth = null
        defaultrowheight = null
        this._psBlock = null


        this.windowTwo = null
        this.selection = null

        _dimensions = null

        _dataValidityTable = null
        condFormatting = null

        this.rowRecIterator = null

        rowsAggregate!!.dispose()
    }

    companion object {
        const val LeftMargin: Short = 0
        const val RightMargin: Short = 1
        const val TopMargin: Short = 2
        const val BottomMargin: Short = 3

        private val log = getLogger(InternalSheet::class.java)

        val PANE_LOWER_RIGHT: Byte = 0.toByte()
        val PANE_UPPER_RIGHT: Byte = 1.toByte()
        val PANE_LOWER_LEFT: Byte = 2.toByte()
        val PANE_UPPER_LEFT: Byte = 3.toByte()

        /**
         * read support  (offset used as starting point for search) for low level
         * API.  Pass in an array of Record objects, the sheet number (0 based) and
         * a record offset (should be the location of the sheets BOF record).  A Sheet
         * object is constructed and passed back with all of its initialization set
         * to the passed in records and references to those records held. This function
         * is normally called via Workbook.
         * 
         * @param rs the stream to read records from
         * 
         * @return Sheet object with all values set to those read from the file
         * 
         * @see InternalWorkbook
         * 
         * @see Record
         */
        @JvmStatic
        fun createSheet(rs: RecordStream): InternalSheet {
            return InternalSheet(rs, null)
        }

        fun createSheet(rs: RecordStream, iAbortListener: AbstractReader?): InternalSheet {
            return InternalSheet(rs, iAbortListener)
        }

        private fun spillAggregate(ra: RecordAggregate, recs: MutableList<RecordBase>) {
            ra.visitContainedRecords(object : RecordVisitor {
                override fun visitRecord(r: Record) {
                    recs.add(r)
                }
            })
        }

        /**
         * Creates a sheet with all the usual records minus values and the "index"
         * record (not required).  Sets the location pointer to where the first value
         * records should go.  Use this to create a sheet from "scratch".
         * 
         * @return Sheet object with all values set to defaults
         */
        @JvmStatic
        fun createSheet(): InternalSheet {
            return InternalSheet()
        }

        /**
         * creates the BOF record
         */
        /* package */
        fun createBOF(): BOFRecord {
            val retval = BOFRecord()

            retval.version = 0x600.toShort().toInt()
            retval.type = 0x010.toShort().toInt()

            retval.build = 0x0dbb.toShort().toInt()
            retval.buildYear = 1996.toShort().toInt()
            retval.historyBitMask = 0xc1
            retval.requiredVersion = 0x6
            return retval
        }

        /**
         * creates the CalcMode record and sets it to 1 (automatic formula caculation)
         */
        private fun createCalcMode(): CalcModeRecord {
            val retval = CalcModeRecord()

            retval.calcMode = 1.toShort()
            return retval
        }

        /**
         * creates the CalcCount record and sets it to 100 (default number of iterations)
         */
        private fun createCalcCount(): CalcCountRecord {
            val retval = CalcCountRecord()

            retval.iterations = 100.toShort() // default 100 iterations
            return retval
        }

        /**
         * creates the RefMode record and sets it to A1 Mode (default reference mode)
         */
        private fun createRefMode(): RefModeRecord {
            val retval = RefModeRecord()

            retval.setMode(RefModeRecord.USE_A1_MODE)
            return retval
        }

        /**
         * creates the Iteration record and sets it to false (don't iteratively calculate formulas)
         */
        private fun createIteration(): IterationRecord {
            return IterationRecord(false)
        }

        /**
         * creates the Delta record and sets it to 0.0010 (default accuracy)
         */
        private fun createDelta(): DeltaRecord {
            return DeltaRecord(DeltaRecord.DEFAULT_VALUE)
        }

        /**
         * creates the SaveRecalc record and sets it to true (recalculate before saving)
         */
        private fun createSaveRecalc(): SaveRecalcRecord {
            val retval = SaveRecalcRecord()

            retval.setRecalc(true)
            return retval
        }

        /**
         * creates the PrintHeaders record and sets it to false (we don't create headers yet so why print them)
         */
        private fun createPrintHeaders(): PrintHeadersRecord {
            val retval = PrintHeadersRecord()

            retval.setPrintHeaders(false)
            return retval
        }

        /**
         * creates the PrintGridlines record and sets it to false (that makes for ugly sheets).  As far as I can
         * tell this does the same thing as the GridsetRecord
         */
        private fun createPrintGridlines(): PrintGridlinesRecord {
            val retval = PrintGridlinesRecord()

            retval.setPrintGridlines(false)
            return retval
        }

        /**
         * creates the Gridset record and sets it to true (user has mucked with the gridlines)
         */
        private fun createGridset(): GridsetRecord {
            val retval = GridsetRecord()

            retval.setGridset(true)
            return retval
        }

        /**
         * creates the Guts record and sets leftrow/topcol guttter and rowlevelmax/collevelmax to 0
         */
        private fun createGuts(): GutsRecord {
            val retval = GutsRecord()

            retval.setLeftRowGutter(0.toShort())
            retval.setTopColGutter(0.toShort())
            retval.setRowLevelMax(0.toShort())
            retval.setColLevelMax(0.toShort())
            return retval
        }

        /**
         * creates the DefaultRowHeight Record and sets its options to 0 and rowheight to 0xff
         */
        private fun createDefaultRowHeight(): DefaultRowHeightRecord {
            val retval = DefaultRowHeightRecord()

            retval.setOptionFlags(0.toShort())
            retval.setRowHeight(DefaultRowHeightRecord.DEFAULT_ROW_HEIGHT)
            return retval
        }

        /**
         * creates the WSBoolRecord and sets its values to defaults
         */
        private fun createWSBool(): WSBoolRecord {
            val retval = WSBoolRecord()

            retval.setWSBool1(0x4.toByte())
            retval.setWSBool2((-0x3f).toByte())
            return retval
        }

        /**
         * creates the DefaultColWidth Record and sets it to 8
         */
        private fun createDefaultColWidth(): DefaultColWidthRecord {
            val retval = DefaultColWidthRecord()
            retval.setColWidth(DefaultColWidthRecord.DEFAULT_COLUMN_WIDTH)
            return retval
        }

        /**
         * creates the Dimensions Record and sets it to bogus values (you should set this yourself
         * or let the high level API do it for you)
         */
        private fun createDimensions(): DimensionsRecord {
            val retval = DimensionsRecord()

            retval.setFirstCol(0.toShort())
            retval.setLastRow(1) // one more than it is
            retval.setFirstRow(0)
            retval.setLastCol(1.toShort()) // one more than it is
            return retval
        }

        /**
         * creates the WindowTwo Record and sets it to:  <P>
         * options        = 0x6b6 </P><P>
         * toprow         = 0 </P><P>
         * leftcol        = 0 </P><P>
         * headercolor    = 0x40 </P><P>
         * pagebreakzoom  = 0x0 </P><P>
         * normalzoom     = 0x0 </P>
         *
         *
         */
        private fun createWindowTwo(): WindowTwoRecord {
            val retval = WindowTwoRecord()

            retval.setOptions(0x6b6.toShort())
            retval.setTopRow(0.toShort())
            retval.setLeftCol(0.toShort())
            retval.setHeaderColor(0x40)
            retval.setPageBreakZoom(0.toShort())
            retval.setNormalZoom(0.toShort())
            return retval
        }

        /**
         * Creates the Selection record and sets it to nothing selected
         */
        private fun createSelection(): SelectionRecord {
            return SelectionRecord(0, 0)
        }
    }
}
