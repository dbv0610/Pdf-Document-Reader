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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.hssf.record.chart.AreaFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.AreaRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisLineFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisOptionsRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisParentRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisUsedRecord
import com.wxiwei.office.fc.hssf.record.chart.BarRecord
import com.wxiwei.office.fc.hssf.record.chart.BeginRecord
import com.wxiwei.office.fc.hssf.record.chart.CatLabRecord
import com.wxiwei.office.fc.hssf.record.chart.CategorySeriesAxisRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartEndBlockRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartEndObjectRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartFRTInfoRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartStartBlockRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartStartObjectRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartTitleFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.DatRecord
import com.wxiwei.office.fc.hssf.record.chart.DataFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.DataLabelExtensionRecord
import com.wxiwei.office.fc.hssf.record.chart.DefaultDataLabelTextPropertiesRecord
import com.wxiwei.office.fc.hssf.record.chart.EndRecord
import com.wxiwei.office.fc.hssf.record.chart.FontBasisRecord
import com.wxiwei.office.fc.hssf.record.chart.FontIndexRecord
import com.wxiwei.office.fc.hssf.record.chart.FrameRecord
import com.wxiwei.office.fc.hssf.record.chart.LegendRecord
import com.wxiwei.office.fc.hssf.record.chart.LineFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.LinkedDataRecord
import com.wxiwei.office.fc.hssf.record.chart.NumberFormatIndexRecord
import com.wxiwei.office.fc.hssf.record.chart.ObjectLinkRecord
import com.wxiwei.office.fc.hssf.record.chart.PlotAreaRecord
import com.wxiwei.office.fc.hssf.record.chart.PlotGrowthRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesLabelsRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesListRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesTextRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesToChartGroupRecord
import com.wxiwei.office.fc.hssf.record.chart.SheetPropertiesRecord
import com.wxiwei.office.fc.hssf.record.chart.TextRecord
import com.wxiwei.office.fc.hssf.record.chart.TickRecord
import com.wxiwei.office.fc.hssf.record.chart.UnitsRecord
import com.wxiwei.office.fc.hssf.record.chart.ValueRangeRecord
import com.wxiwei.office.fc.hssf.record.pivottable.DataItemRecord
import com.wxiwei.office.fc.hssf.record.pivottable.ExtendedPivotTableViewFieldsRecord
import com.wxiwei.office.fc.hssf.record.pivottable.PageItemRecord
import com.wxiwei.office.fc.hssf.record.pivottable.StreamIDRecord
import com.wxiwei.office.fc.hssf.record.pivottable.ViewDefinitionRecord
import com.wxiwei.office.fc.hssf.record.pivottable.ViewFieldsRecord
import com.wxiwei.office.fc.hssf.record.pivottable.ViewSourceRecord
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.AbstractReader
import java.io.InputStream
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Arrays
import java.util.Locale

/**
 * Title:  Record Factory<P>
 * Description:  Takes a stream and outputs an array of Record objects.</P><P>
 * 
 * @see com.wxiwei.office.fc.hssf.eventmodel.EventRecordFactory
 * 
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
</P> */
object RecordFactory {
    private const val NUM_RECORDS = 512

    private val CONSTRUCTOR_ARGS = arrayOf<Class<*>?>(RecordInputStream::class.java)

    /**
     * contains the classes for all the records we want to parse.<br></br>
     * Note - this most but not *every* subclass of Record.
     */
    private val recordClasses: Array<Class<out Record?>?> = arrayOf(
        ArrayRecord::class.java,
        AutoFilterInfoRecord::class.java,
        BackupRecord::class.java,
        BlankRecord::class.java,
        BOFRecord::class.java,
        BookBoolRecord::class.java,
        BoolErrRecord::class.java,
        BottomMarginRecord::class.java,
        BoundSheetRecord::class.java,
        CalcCountRecord::class.java,
        CalcModeRecord::class.java,
        CFHeaderRecord::class.java,
        CFRuleRecord::class.java,
        ChartRecord::class.java,
        ChartTitleFormatRecord::class.java,
        CodepageRecord::class.java,
        ColumnInfoRecord::class.java,
        ContinueRecord::class.java,
        CountryRecord::class.java,
        CRNCountRecord::class.java,
        CRNRecord::class.java,
        DateWindow1904Record::class.java,
        DBCellRecord::class.java,
        DefaultColWidthRecord::class.java,
        DefaultRowHeightRecord::class.java,
        DeltaRecord::class.java,
        DimensionsRecord::class.java,
        DrawingGroupRecord::class.java,
        DrawingRecord::class.java,
        DrawingSelectionRecord::class.java,
        DSFRecord::class.java,
        DVALRecord::class.java,
        DVRecord::class.java,
        EOFRecord::class.java,
        ExtendedFormatRecord::class.java,
        ExternalNameRecord::class.java,
        ExternSheetRecord::class.java,
        ExtSSTRecord::class.java,
        FeatRecord::class.java,
        FeatHdrRecord::class.java,
        FilePassRecord::class.java,
        FileSharingRecord::class.java,
        FnGroupCountRecord::class.java,
        FontRecord::class.java,
        FooterRecord::class.java,
        FormatRecord::class.java,
        FormulaRecord::class.java,
        GridsetRecord::class.java,
        GutsRecord::class.java,
        HCenterRecord::class.java,
        HeaderRecord::class.java,
        HeaderFooterRecord::class.java,
        HideObjRecord::class.java,
        HorizontalPageBreakRecord::class.java,
        HyperlinkRecord::class.java,
        IndexRecord::class.java,
        InterfaceEndRecord::class.java,
        InterfaceHdrRecord::class.java,
        IterationRecord::class.java,
        LabelRecord::class.java,
        LabelSSTRecord::class.java,
        LeftMarginRecord::class.java,
        LegendRecord::class.java,
        MergeCellsRecord::class.java,
        MMSRecord::class.java,
        MulBlankRecord::class.java,
        MulRKRecord::class.java,
        NameRecord::class.java,
        NameCommentRecord::class.java,
        NoteRecord::class.java,
        NumberRecord::class.java,
        ObjectProtectRecord::class.java,
        ObjRecord::class.java,
        PaletteRecord::class.java,
        PaneRecord::class.java,
        PasswordRecord::class.java,
        PasswordRev4Record::class.java,
        PrecisionRecord::class.java,
        PrintGridlinesRecord::class.java,
        PrintHeadersRecord::class.java,
        PrintSetupRecord::class.java,
        ProtectionRev4Record::class.java,
        ProtectRecord::class.java,
        RecalcIdRecord::class.java,
        RefModeRecord::class.java,
        RefreshAllRecord::class.java,
        RightMarginRecord::class.java,
        RKRecord::class.java,
        RowRecord::class.java,
        SaveRecalcRecord::class.java,
        ScenarioProtectRecord::class.java,
        SelectionRecord::class.java,
        SeriesRecord::class.java,
        SeriesTextRecord::class.java,
        SharedFormulaRecord::class.java,
        SSTRecord::class.java,
        StringRecord::class.java,
        StyleRecord::class.java,
        SupBookRecord::class.java,
        TabIdRecord::class.java,
        TableRecord::class.java,
        TableStylesRecord::class.java,
        TextObjectRecord::class.java,
        TopMarginRecord::class.java,
        UncalcedRecord::class.java,
        UseSelFSRecord::class.java,
        UserSViewBegin::class.java,
        UserSViewEnd::class.java,
        ValueRangeRecord::class.java,
        VCenterRecord::class.java,
        VerticalPageBreakRecord::class.java,
        WindowOneRecord::class.java,
        WindowProtectRecord::class.java,
        WindowTwoRecord::class.java,
        WriteAccessRecord::class.java,
        WriteProtectRecord::class.java,
        WSBoolRecord::class.java,  // chart records

        BeginRecord::class.java,
        ChartFRTInfoRecord::class.java,
        ChartStartBlockRecord::class.java,
        ChartEndBlockRecord::class.java,  // TODO ChartFormatRecord.class,
        ChartStartObjectRecord::class.java,
        ChartEndObjectRecord::class.java,
        CatLabRecord::class.java,
        DataFormatRecord::class.java,
        EndRecord::class.java,
        LinkedDataRecord::class.java,
        SeriesToChartGroupRecord::class.java,  //sid is same as SeriesChartGroupIndexRecord

        AreaFormatRecord::class.java,
        AreaRecord::class.java,
        AxisLineFormatRecord::class.java,
        AxisOptionsRecord::class.java,
        AxisParentRecord::class.java,
        AxisRecord::class.java,
        AxisUsedRecord::class.java,
        BarRecord::class.java,
        CategorySeriesAxisRecord::class.java,
        DatRecord::class.java,
        DefaultDataLabelTextPropertiesRecord::class.java,
        FontBasisRecord::class.java,
        FontIndexRecord::class.java,
        FrameRecord::class.java,
        LineFormatRecord::class.java,
        NumberFormatIndexRecord::class.java,
        PlotAreaRecord::class.java,
        PlotGrowthRecord::class.java,  /*SeriesIndexRecord.class,*/
        SeriesLabelsRecord::class.java,
        SeriesListRecord::class.java,
        SheetPropertiesRecord::class.java,
        TickRecord::class.java,
        UnitsRecord::class.java,  // pivot table records

        DataItemRecord::class.java,
        ExtendedPivotTableViewFieldsRecord::class.java,
        PageItemRecord::class.java,
        StreamIDRecord::class.java,
        ViewDefinitionRecord::class.java,
        ViewFieldsRecord::class.java,
        ViewSourceRecord::class.java,
        DataLabelExtensionRecord::class.java,
        TextRecord::class.java,
        ObjectLinkRecord::class.java,
    )

    /**
     * cache of the recordsToMap();
     */
    private val _recordCreatorsById: MutableMap<Int?, I_RecordCreator?> =
        recordsToMap(recordClasses)

    private var _allKnownRecordSIDs: ShortArray? = null

    /**
     * Debug / diagnosis method<br></br>
     * Gets the POI implementation class for a given <tt>sid</tt>.  Only a subset of the any BIFF
     * records are actually interpreted by POI.  A few others are known but not interpreted
     * (see [UnknownRecord.getBiffName]).
     * @return the POI implementation class for the specified record <tt>sid</tt>.
     * `null` if the specified record is not interpreted by POI.
     */
    fun getRecordClass(sid: Int): Class<out Record?>? {
        val rc = _recordCreatorsById.get(sid)
        if (rc == null) {
            return null
        }
        return rc.getRecordClass()
    }

    /**
     * create a record, if there are MUL records than multiple records
     * are returned digested into the non-mul form.
     */
    fun createRecord(`in`: RecordInputStream): Array<Record?> {
        val record = createSingleRecord(`in`)
        if (record is DBCellRecord) {
            // Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
            return arrayOf<Record?>(null)
        }
        if (record is RKRecord) {
            return arrayOf<Record?>(convertToNumberRecord(record))
        }
        if (record is MulRKRecord) {
            return convertRKRecords(record) as Array<Record?>
        }
        return arrayOf<Record?>(record)
    }

    fun createSingleRecord(`in`: RecordInputStream): Record? {
        val constructor = _recordCreatorsById[`in`.getSid().toInt()]

        if (constructor == null) {
            return UnknownRecord(`in`)
        }

        return constructor.create(`in`)
    }

    /**
     * RK record is a slightly smaller alternative to NumberRecord
     * POI likes NumberRecord better
     */
    fun convertToNumberRecord(rk: RKRecord): NumberRecord {
        val num = NumberRecord()

        num.column = rk.column
        num.row = rk.row
        num.xFIndex = rk.xFIndex
        num.setValue(rk.getRKNumber())
        return num
    }

    /**
     * Converts a [MulRKRecord] into an equivalent array of [NumberRecord]s
     */
    fun convertRKRecords(mrk: MulRKRecord): Array<NumberRecord?> {
        val mulRecs = arrayOfNulls<NumberRecord>(mrk.getNumColumns())
        for (k in 0..<mrk.getNumColumns()) {
            val nr = NumberRecord()

            nr.column = (k + mrk.getFirstColumn()).toShort()
            nr.row = mrk.getRow()
            nr.xFIndex = mrk.getXFAt(k)
            nr.setValue(mrk.getRKNumberAt(k))
            mulRecs[k] = nr
        }
        return mulRecs
    }

    /**
     * Converts a [MulBlankRecord] into an equivalent array of [BlankRecord]s
     */
    fun convertBlankRecords(mbk: MulBlankRecord): Array<BlankRecord?> {
        val mulRecs = arrayOfNulls<BlankRecord>(mbk.getNumColumns())
        for (k in 0..<mbk.getNumColumns()) {
            val br = BlankRecord()

            br.column = (k + mbk.getFirstColumn()).toShort()
            br.row = mbk.getRow()
            br.xFIndex = mbk.getXFAt(k)
            mulRecs[k] = br
        }
        return mulRecs
    }

    /**
     * @return an array of all the SIDS for all known records
     */
    fun getAllKnownRecordSIDs(): ShortArray? {
        if (_allKnownRecordSIDs == null) {
            val results = ShortArray(_recordCreatorsById.size)
            var i = 0

            val iterator = _recordCreatorsById.keys.iterator()
            while (iterator
                    .hasNext()
            ) {
                val sid = iterator.next()

                results[i++] = sid!!.toShort()
            }
            Arrays.sort(results)
            _allKnownRecordSIDs = results
        }

        return _allKnownRecordSIDs!!.clone()
    }

    /**
     * gets the record constructors and sticks them in the map by SID
     * @return map of SIDs to short,short,byte[] constructors for Record classes
     * most of org.apache.poi.hssf.record.*
     */
    private fun recordsToMap(records: Array<Class<out Record?>?>): MutableMap<Int?, I_RecordCreator?> {
        val result: MutableMap<Int?, I_RecordCreator?> = HashMap<Int?, I_RecordCreator?>()
        val uniqueRecClasses: MutableSet<Class<*>?> = HashSet<Class<*>?>(records.size * 3 / 2)

        for (i in records.indices) {
            val recClass: Class<out Record> = records[i]!!
            if (!Record::class.java.isAssignableFrom(recClass)) {
                throw RuntimeException("Invalid record sub-class (" + recClass.getName() + ")")
            }
            if (Modifier.isAbstract(recClass.getModifiers())) {
                throw RuntimeException(
                    ("Invalid record class (" + recClass.getName()
                            + ") - must not be abstract")
                )
            }
            if (!uniqueRecClasses.add(recClass)) {
                throw RuntimeException("duplicate record class (" + recClass.getName() + ")")
            }

            val sid: Int
            try {
                sid = recClass.getField("sid").getShort(null).toInt()
            } catch (illegalArgumentException: Exception) {
                throw RecordFormatException("Unable to determine record types")
            }
            val key = sid
            if (result.containsKey(key)) {
                val prevClass: Class<*> = result.get(key)!!.getRecordClass()
                throw RuntimeException(
                    ("duplicate record sid 0x"
                            + Integer.toHexString(sid)
                        .uppercase(Locale.getDefault()) + " for classes ("
                            + recClass.getName() + ") and (" + prevClass.getName() + ")")
                )
            }
            result.put(key, getRecordCreator(recClass))
        }
        //		result.put(Integer.valueOf(0x0406), result.get(Integer.valueOf(0x06)));
        return result
    }

    private fun getRecordCreator(recClass: Class<out Record>): I_RecordCreator {
        try {
            val constructor: Constructor<out Record>?
            constructor = recClass.getConstructor(*CONSTRUCTOR_ARGS)
            return ReflectionConstructorRecordCreator(constructor)
        } catch (e: NoSuchMethodException) {
            // fall through and look for other construction methods
        }
        try {
            val m = recClass.getDeclaredMethod("create", *CONSTRUCTOR_ARGS)
            return ReflectionMethodRecordCreator(m)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(
                ("Failed to find constructor or create method for ("
                        + recClass.getName() + ").")
            )
        }
    }

    /**
     * Create an array of records from an input stream
     * 
     * @param in the InputStream from which the records will be obtained
     * 
     * @return an array of Records created from the InputStream
     * 
     * @exception RecordFormatException on error processing the InputStream
     */
    /**
     * Create an array of records from an input stream
     * 
     * @param in the InputStream from which the records will be obtained
     * 
     * @return an array of Records created from the InputStream
     * 
     * @exception RecordFormatException on error processing the InputStream
     */
    @JvmOverloads
    @Throws(RecordFormatException::class)
    fun createRecords(
        `in`: InputStream?,
        iAbortListener: AbstractReader? = null
    ): MutableList<Record?> {
        val records: MutableList<Record?> = ArrayList<Record?>(NUM_RECORDS)

        var recStream: RecordFactoryInputStream? = RecordFactoryInputStream(`in`, true)

        var record: Record?
        while ((recStream!!.nextRecord().also { record = it }) != null) {
            if (iAbortListener != null && iAbortListener.isAborted()) {
                throw AbortReaderError("abort Reader")
            }
            records.add(record)
        }

        recStream.dispose()
        recStream = null

        return records
    }

    private interface I_RecordCreator {
        fun create(`in`: RecordInputStream?): Record?

        fun getRecordClass(): Class<out Record?>
    }

    private class ReflectionConstructorRecordCreator(c: Constructor<out Record>) : I_RecordCreator {
        private val _c: Constructor<out Record>

        init {
            _c = c
        }

        override fun create(`in`: RecordInputStream?): Record {
            val args = arrayOf<Any?>(`in`)
            try {
                return _c.newInstance(*args)
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: InstantiationException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RecordFormatException(
                    "Unable to construct record instance",
                    e.getTargetException()
                )
            }
        }

        override fun getRecordClass(): Class<out Record?> {
            return _c.getDeclaringClass()
        }
    }

    /**
     * A "create" method is used instead of the usual constructor if the created record might
     * be of a different class to the declaring class.
     */
    private class ReflectionMethodRecordCreator(m: Method) : I_RecordCreator {
        private val _m: Method

        init {
            _m = m
        }

        override fun create(`in`: RecordInputStream?): Record? {
            val args = arrayOf<Any?>(`in`)
            try {
                return _m.invoke(null, *args) as Record?
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RecordFormatException(
                    "Unable to construct record instance",
                    e.getTargetException()
                )
            }
        }

        override fun getRecordClass(): Class<out Record?> {
            return _m.getDeclaringClass() as Class<out Record?>
        }
    }
}
