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

import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AreaPtgBase
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.DimensionsRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.FooterRecord
import com.wxiwei.office.fc.hssf.record.HCenterRecord
import com.wxiwei.office.fc.hssf.record.HeaderRecord
import com.wxiwei.office.fc.hssf.record.PrintSetupRecord
import com.wxiwei.office.fc.hssf.record.ProtectRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.SCLRecord
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.hssf.record.VCenterRecord
import com.wxiwei.office.fc.hssf.record.chart.AreaFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisLineFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisOptionsRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisParentRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisRecord
import com.wxiwei.office.fc.hssf.record.chart.AxisUsedRecord
import com.wxiwei.office.fc.hssf.record.chart.BarRecord
import com.wxiwei.office.fc.hssf.record.chart.BeginRecord
import com.wxiwei.office.fc.hssf.record.chart.CategorySeriesAxisRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartFormatRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartRecord
import com.wxiwei.office.fc.hssf.record.chart.ChartTitleFormatRecord
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
import com.wxiwei.office.fc.hssf.record.chart.ObjectLinkRecord
import com.wxiwei.office.fc.hssf.record.chart.PlotAreaRecord
import com.wxiwei.office.fc.hssf.record.chart.PlotGrowthRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesIndexRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesTextRecord
import com.wxiwei.office.fc.hssf.record.chart.SeriesToChartGroupRecord
import com.wxiwei.office.fc.hssf.record.chart.SheetPropertiesRecord
import com.wxiwei.office.fc.hssf.record.chart.TextRecord
import com.wxiwei.office.fc.hssf.record.chart.TickRecord
import com.wxiwei.office.fc.hssf.record.chart.UnitsRecord
import com.wxiwei.office.fc.hssf.record.chart.ValueRangeRecord
import com.wxiwei.office.fc.hssf.util.CellRangeAddress
import com.wxiwei.office.fc.ss.util.CellRangeAddressBase
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * Has methods for construction of a chart object.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class HSSFChart(
    workbook: AWorkbook?,
    escherContainer: EscherContainerRecord?,
    parent: HSSFShape?,
    anchor: HSSFAnchor?
) : HSSFSimpleShape(escherContainer, parent, anchor) {
    private val sheet: HSSFSheet? = null
    private var chartRecord: ChartRecord? = null

    var legendRecord: LegendRecord? = null
        private set
    var chartTitleFormat: ChartTitleFormatRecord? = null
        private set

    /**
     * x label title, y label title, chart title
     * @return
     */
    val seriesText: MutableMap<SeriesTextRecord?, Record?> = HashMap<SeriesTextRecord?, Record?>()

    /**
     * contain the background color of series
     * @return
     */
    var seriesBackgroundColorFormat: AreaFormatRecord? = null
        private set

    /**
     * 
     * @return
     */
    var marginColorFormat: AreaFormatRecord? = null
        private set

    val valueRangeRecord: MutableList<ValueRangeRecord?> = ArrayList<ValueRangeRecord?>()

    var type: HSSFChartType = HSSFChartType.Unknown
        private set

    private val series: MutableList<HSSFSeries?> = ArrayList<HSSFSeries?>()

    enum class HSSFChartType {
        Area {
            override fun getSid(): Short {
                return 0x101A
            }
        },
        Bar {
            override fun getSid(): Short {
                return 0x1017
            }
        },
        Line {
            override fun getSid(): Short {
                return 0x1018
            }
        },
        Pie {
            override fun getSid(): Short {
                return 0x1019
            }
        },
        Scatter {
            override fun getSid(): Short {
                return 0x101B
            }
        },
        Unknown {
            override fun getSid(): Short {
                return 0
            }
        };

        abstract fun getSid(): Short
    }

    /**
     * Construct a new textbox with the given parent and anchor.
     * @param parent
     * @param anchor  One of HSSFClientAnchor or HSSFChildAnchor
     */
    init {
        if (escherContainer != null && workbook != null) {
            processLineWidth()
            processLine(escherContainer, workbook)
            processSimpleBackground(escherContainer, workbook)
            processRotationAndFlip(escherContainer)
        }

        shapeType = OBJECT_TYPE_CHART.toInt()
    }

    /**
     * Creates a bar chart.  API needs some work. :)
     * 
     * 
     * NOTE:  Does not yet work...  checking it in just so others
     * can take a look.
     */
    fun createBarChart(workbook: HSSFWorkbook, sheet: HSSFSheet) {
        val records: MutableList<Record?> = ArrayList<Record?>()
        records.add(createMSDrawingObjectRecord())
        records.add(createOBJRecord())
        records.add(createBOFRecord())
        records.add(HeaderRecord(""))
        records.add(FooterRecord(""))
        records.add(createHCenterRecord())
        records.add(createVCenterRecord())
        records.add(createPrintSetupRecord())
        // unknown 33
        records.add(createFontBasisRecord1())
        records.add(createFontBasisRecord2())
        records.add(ProtectRecord(false))
        records.add(createUnitsRecord())
        records.add(createChartRecord(0, 0, 30434904, 19031616))
        records.add(createBeginRecord())
        records.add(createSCLRecord(1.toShort(), 1.toShort()))
        records.add(createPlotGrowthRecord(65536, 65536))
        records.add(createFrameRecord1())
        records.add(createBeginRecord())
        records.add(createLineFormatRecord(true))
        records.add(createAreaFormatRecord1())
        records.add(createEndRecord())
        records.add(createSeriesRecord())
        records.add(createBeginRecord())
        records.add(createTitleLinkedDataRecord())
        records.add(createValuesLinkedDataRecord())
        records.add(createCategoriesLinkedDataRecord())
        records.add(createDataFormatRecord())
        //		records.add(createBeginRecord());
        // unknown
        //		records.add(createEndRecord());
        records.add(createSeriesToChartGroupRecord())
        records.add(createEndRecord())
        records.add(createSheetPropsRecord())
        records.add(createDefaultTextRecord(DefaultDataLabelTextPropertiesRecord.CATEGORY_DATA_TYPE_ALL_TEXT_CHARACTERISTIC))
        records.add(createAllTextRecord())
        records.add(createBeginRecord())
        // unknown
        records.add(createFontIndexRecord(5))
        records.add(createDirectLinkRecord())
        records.add(createEndRecord())
        records.add(createDefaultTextRecord(3.toShort())) // eek, undocumented text type
        records.add(createUnknownTextRecord())
        records.add(createBeginRecord())
        records.add(createFontIndexRecord(6.toShort().toInt()))
        records.add(createDirectLinkRecord())
        records.add(createEndRecord())

        records.add(createAxisUsedRecord(1.toShort()))
        createAxisRecords(records)

        records.add(createEndRecord())
        records.add(createDimensionsRecord())
        records.add(createSeriesIndexRecord(2))
        records.add(createSeriesIndexRecord(1))
        records.add(createSeriesIndexRecord(3))
        records.add(EOFRecord.instance)



        sheet.insertChartRecords(records)
        workbook.insertChartRecord()
    }

    var chartX: Int
        /** Get the X offset of the chart  */
        get() = chartRecord!!.x
        /** Sets the X offset of the chart  */
        set(x) {
            chartRecord!!.x = x
        }
    var chartY: Int
        /** Get the Y offset of the chart  */
        get() = chartRecord!!.y
        /** Sets the Y offset of the chart  */
        set(y) {
            chartRecord!!.y = y
        }
    var chartWidth: Int
        /** Get the width of the chart. [ChartRecord]  */
        get() = chartRecord!!.width
        /** Sets the width of the chart. [ChartRecord]  */
        set(width) {
            chartRecord!!.width = width
        }
    var chartHeight: Int
        /** Get the height of the chart. [ChartRecord]  */
        get() = chartRecord!!.height
        /** Sets the height of the chart. [ChartRecord]  */
        set(height) {
            chartRecord!!.height = height
        }

    fun setChartRecord(chartRecord: ChartRecord) {
        this.chartRecord = chartRecord
    }

    fun getChartRecord(): ChartRecord {
        return chartRecord!!
    }

    /**
     * Returns the series of the chart
     */
    fun getSeries(): Array<HSSFSeries?> {
        return series.toTypedArray<HSSFSeries?>()
    }

    /**
     * Set value range (basic Axis Options)
     * @param axisIndex 0 - primary axis, 1 - secondary axis
     * @param minimum minimum value; Double.NaN - automatic; null - no change
     * @param maximum maximum value; Double.NaN - automatic; null - no change
     * @param majorUnit major unit value; Double.NaN - automatic; null - no change
     * @param minorUnit minor unit value; Double.NaN - automatic; null - no change
     */
    fun setValueRange(
        axisIndex: Int,
        minimum: Double?,
        maximum: Double?,
        majorUnit: Double?,
        minorUnit: Double?
    ) {
        val valueRange = valueRangeRecord.get(axisIndex)
        if (valueRange == null) return
        if (minimum != null) {
            valueRange.isAutomaticMinimum = minimum.isNaN()
            valueRange.minimumAxisValue = minimum
        }
        if (maximum != null) {
            valueRange.isAutomaticMaximum = maximum.isNaN()
            valueRange.maximumAxisValue = maximum
        }
        if (majorUnit != null) {
            valueRange.isAutomaticMajor = majorUnit.isNaN()
            valueRange.majorIncrement = majorUnit
        }
        if (minorUnit != null) {
            valueRange.isAutomaticMinor = minorUnit.isNaN()
            valueRange.minorIncrement = minorUnit
        }
    }

    private fun createSeriesIndexRecord(index: Int): SeriesIndexRecord {
        val r = SeriesIndexRecord()
        r.index = index.toShort()
        return r
    }

    private fun createDimensionsRecord(): DimensionsRecord {
        val r = DimensionsRecord()
        r.setFirstRow(0)
        r.setLastRow(31)
        r.setFirstCol(0.toShort())
        r.setLastCol(1.toShort())
        return r
    }

    private fun createHCenterRecord(): HCenterRecord {
        val r = HCenterRecord()
        r.setHCenter(false)
        return r
    }

    private fun createVCenterRecord(): VCenterRecord {
        val r = VCenterRecord()
        r.setVCenter(false)
        return r
    }

    private fun createPrintSetupRecord(): PrintSetupRecord {
        val r = PrintSetupRecord()
        r.setPaperSize(0.toShort())
        r.setScale(18.toShort())
        r.setPageStart(1.toShort())
        r.setFitWidth(1.toShort())
        r.setFitHeight(1.toShort())
        r.setLeftToRight(false)
        r.setLandscape(false)
        r.setValidSettings(true)
        r.setNoColor(false)
        r.setDraft(false)
        r.setNotes(false)
        r.setNoOrientation(false)
        r.setUsePage(false)
        r.setHResolution(0.toShort())
        r.setVResolution(0.toShort())
        r.setHeaderMargin(0.5)
        r.setFooterMargin(0.5)
        r.setCopies(15.toShort()) // what the ??
        return r
    }

    private fun createFontBasisRecord1(): FontBasisRecord {
        val r = FontBasisRecord()
        r.xBasis = 9120.toShort()
        r.yBasis = 5640.toShort()
        r.heightBasis = 200.toShort()
        r.scale = 0.toShort()
        r.indexToFontTable = 5.toShort()
        return r
    }

    private fun createFontBasisRecord2(): FontBasisRecord {
        val r = createFontBasisRecord1()
        r.indexToFontTable = 6.toShort()
        return r
    }

    private fun createBOFRecord(): BOFRecord {
        val r = BOFRecord()
        r.version = 600
        r.type = 20
        r.build = 0x1CFE
        r.buildYear = 1997
        r.historyBitMask = 0x40C9
        r.requiredVersion = 106
        return r
    }

    private fun createOBJRecord(): UnknownRecord {
        val data = byteArrayOf(
            0x15.toByte(),
            0x00.toByte(),
            0x12.toByte(),
            0x00.toByte(),
            0x05.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x11.toByte(),
            0x60.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0xB8.toByte(),
            0x03.toByte(),
            0x87.toByte(),
            0x03.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
        )

        return UnknownRecord(0x005D.toShort().toInt(), data)
    }

    private fun createMSDrawingObjectRecord(): UnknownRecord {
        // Since we haven't created this object yet we'll just put in the raw
        // form for the moment.

        val data = byteArrayOf(
            0x0F.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0xF0.toByte(),
            0xC0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x10.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xF0.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0F.toByte(),
            0x00.toByte(),
            0x03.toByte(),
            0xF0.toByte(),
            0xA8.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0F.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0xF0.toByte(),
            0x28.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x09.toByte(),
            0xF0.toByte(),
            0x10.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x0A.toByte(),
            0xF0.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x05.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0F.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0xF0.toByte(),
            0x70.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x92.toByte(),
            0x0C.toByte(),
            0x0A.toByte(),
            0xF0.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x93.toByte(),
            0x00.toByte(),
            0x0B.toByte(),
            0xF0.toByte(),
            0x36.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x7F.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0x01.toByte(),
            0x04.toByte(),
            0x01.toByte(),
            0xBF.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x81.toByte(),
            0x01.toByte(),
            0x4E.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x83.toByte(),
            0x01.toByte(),
            0x4D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xBF.toByte(),
            0x01.toByte(),
            0x10.toByte(),
            0x00.toByte(),
            0x11.toByte(),
            0x00.toByte(),
            0xC0.toByte(),
            0x01.toByte(),
            0x4D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xFF.toByte(),
            0x01.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x3F.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0xBF.toByte(),
            0x03.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x10.toByte(),
            0xF0.toByte(),
            0x12.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0xC0.toByte(),
            0x02.toByte(),
            0x0A.toByte(),
            0x00.toByte(),
            0xF4.toByte(),
            0x00.toByte(),
            0x0E.toByte(),
            0x00.toByte(),
            0x66.toByte(),
            0x01.toByte(),
            0x20.toByte(),
            0x00.toByte(),
            0xE9.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x11.toByte(),
            0xF0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte()
        )

        return UnknownRecord(0x00EC.toShort().toInt(), data)
    }

    private fun createAxisRecords(records: MutableList<Record?>) {
        records.add(createAxisParentRecord())
        records.add(createBeginRecord())
        records.add(createAxisRecord(AxisRecord.AXIS_TYPE_CATEGORY_OR_X_AXIS))
        records.add(createBeginRecord())
        records.add(createCategorySeriesAxisRecord())
        records.add(createAxisOptionsRecord())
        records.add(createTickRecord1())
        records.add(createEndRecord())
        records.add(createAxisRecord(AxisRecord.AXIS_TYPE_VALUE_AXIS))
        records.add(createBeginRecord())
        records.add(createValueRangeRecord())
        records.add(createTickRecord2())
        records.add(createAxisLineFormatRecord(AxisLineFormatRecord.AXIS_TYPE_MAJOR_GRID_LINE))
        records.add(createLineFormatRecord(false))
        records.add(createEndRecord())
        records.add(createPlotAreaRecord())
        records.add(createFrameRecord2())
        records.add(createBeginRecord())
        records.add(createLineFormatRecord2())
        records.add(createAreaFormatRecord2())
        records.add(createEndRecord())
        records.add(createChartFormatRecord())
        records.add(createBeginRecord())
        records.add(createBarRecord())
        // unknown 1022
        records.add(createLegendRecord())
        records.add(createBeginRecord())
        // unknown 104f
        records.add(createTextRecord())
        records.add(createBeginRecord())
        // unknown 104f
        records.add(createLinkedDataRecord())
        records.add(createEndRecord())
        records.add(createEndRecord())
        records.add(createEndRecord())
        records.add(createEndRecord())
    }

    private fun createLinkedDataRecord(): LinkedDataRecord {
        val r = LinkedDataRecord()
        r.linkType = LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT
        r.referenceType = LinkedDataRecord.REFERENCE_TYPE_DIRECT
        r.isCustomNumberFormat = false
        r.indexNumberFmtRecord = 0.toShort()
        r.setFormulaOfLink(null)
        return r
    }

    private fun createTextRecord(): TextRecord {
        val r = TextRecord()
        r.horizontalAlignment = TextRecord.HORIZONTAL_ALIGNMENT_CENTER
        r.verticalAlignment = TextRecord.VERTICAL_ALIGNMENT_CENTER
        r.displayMode = 1.toShort()
        r.rgbColor = 0x00000000
        r.x = -37
        r.y = -60
        r.width = 0
        r.height = 0
        r.isAutoColor = true
        r.isShowKey = false
        r.isShowValue = false
        r.isVertical = false
        r.isAutoGeneratedText = true
        r.isGenerated = true
        r.isAutoLabelDeleted = false
        r.isAutoBackground = true
        r.rotation = 0.toShort()
        r.isShowCategoryLabelAsPercentage = false
        r.isShowValueAsPercentage = false
        r.isShowBubbleSizes = false
        r.isShowLabel = false
        r.indexOfColorValue = 77.toShort()
        r.dataLabelPlacement = 0.toShort()
        r.textRotation = 0.toShort()
        return r
    }

    private fun createLegendRecord(): LegendRecord {
        val r = LegendRecord()
        r.xAxisUpperLeft = 3542
        r.yAxisUpperLeft = 1566
        r.xSize = 437
        r.ySize = 213
        r.type = LegendRecord.TYPE_RIGHT
        r.spacing = LegendRecord.SPACING_MEDIUM
        r.isAutoPosition = true
        r.isAutoSeries = true
        r.isAutoXPositioning = true
        r.isAutoYPositioning = true
        r.isVertical = true
        r.isDataTable = false
        return r
    }

    private fun createBarRecord(): BarRecord {
        val r = BarRecord()
        r.barSpace = 0.toShort()
        r.categorySpace = 150.toShort()
        r.isHorizontal = false
        r.isStacked = false
        r.isDisplayAsPercentage = false
        r.isShadow = false
        return r
    }

    private fun createChartFormatRecord(): ChartFormatRecord {
        val r = ChartFormatRecord()
        r.xPosition = 0
        r.yPosition = 0
        r.width = 0
        r.height = 0
        r.varyDisplayPattern = false
        return r
    }

    private fun createPlotAreaRecord(): PlotAreaRecord {
        val r = PlotAreaRecord()
        return r
    }

    private fun createAxisLineFormatRecord(format: Short): AxisLineFormatRecord {
        val r = AxisLineFormatRecord()
        r.axisType = format
        return r
    }

    private fun createValueRangeRecord(): ValueRangeRecord {
        val r = ValueRangeRecord()
        r.minimumAxisValue = 0.0
        r.maximumAxisValue = 0.0
        r.majorIncrement = 0.0
        r.minorIncrement = 0.0
        r.categoryAxisCross = 0.0
        r.isAutomaticMinimum = true
        r.isAutomaticMaximum = true
        r.isAutomaticMajor = true
        r.isAutomaticMinor = true
        r.isAutomaticCategoryCrossing = true
        r.isLogarithmicScale = false
        r.isValuesInReverse = false
        r.isCrossCategoryAxisAtMaximum = false
        r.isReserved = true // what's this do??
        return r
    }

    private fun createTickRecord1(): TickRecord {
        val r = TickRecord()
        r.majorTickType = 2.toByte()
        r.minorTickType = 0.toByte()
        r.labelPosition = 3.toByte()
        r.background = 1.toByte()
        r.labelColorRgb = 0
        r.zero1 = 0
        r.zero2 = 0
        r.zero3 = 45.toShort()
        r.isAutorotate = true
        r.isAutoTextBackground = true
        r.rotation = 0.toShort()
        r.isAutorotate = true
        r.tickColor = 77.toShort()
        return r
    }

    private fun createTickRecord2(): TickRecord {
        val r = createTickRecord1()
        r.zero3 = 0.toShort()
        return r
    }

    private fun createAxisOptionsRecord(): AxisOptionsRecord {
        val r = AxisOptionsRecord()
        r.minimumCategory = (-28644).toShort()
        r.maximumCategory = (-28715).toShort()
        r.majorUnitValue = 2.toShort()
        r.majorUnit = 0.toShort()
        r.minorUnitValue = 1.toShort()
        r.minorUnit = 0.toShort()
        r.baseUnit = 0.toShort()
        r.crossingPoint = (-28644).toShort()
        r.isDefaultMinimum = true
        r.isDefaultMaximum = true
        r.isDefaultMajor = true
        r.isDefaultMinorUnit = true
        r.setIsDate(true)
        r.isDefaultBase = true
        r.isDefaultCross = true
        r.isDefaultDateSettings = true
        return r
    }

    private fun createCategorySeriesAxisRecord(): CategorySeriesAxisRecord {
        val r = CategorySeriesAxisRecord()
        r.crossingPoint = 1.toShort()
        r.labelFrequency = 1.toShort()
        r.tickMarkFrequency = 1.toShort()
        r.isValueAxisCrossing = true
        r.isCrossesFarRight = false
        r.isReversed = false
        return r
    }

    private fun createAxisRecord(axisType: Short): AxisRecord {
        val r = AxisRecord()
        r.axisType = axisType
        return r
    }

    private fun createAxisParentRecord(): AxisParentRecord {
        val r = AxisParentRecord()
        r.axisType = AxisParentRecord.AXIS_TYPE_MAIN
        r.x = 479
        r.y = 221
        r.width = 2995
        r.height = 2902
        return r
    }

    private fun createAxisUsedRecord(numAxis: Short): AxisUsedRecord {
        val r = AxisUsedRecord()
        r.numAxis = numAxis
        return r
    }

    private fun createDirectLinkRecord(): LinkedDataRecord {
        val r = LinkedDataRecord()
        r.linkType = LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT
        r.referenceType = LinkedDataRecord.REFERENCE_TYPE_DIRECT
        r.isCustomNumberFormat = false
        r.indexNumberFmtRecord = 0.toShort()
        r.setFormulaOfLink(null)
        return r
    }

    private fun createFontIndexRecord(index: Int): FontIndexRecord {
        val r = FontIndexRecord()
        r.fontIndex = index.toShort()
        return r
    }

    private fun createAllTextRecord(): TextRecord {
        val r = TextRecord()
        r.horizontalAlignment = TextRecord.HORIZONTAL_ALIGNMENT_CENTER
        r.verticalAlignment = TextRecord.VERTICAL_ALIGNMENT_CENTER
        r.displayMode = TextRecord.DISPLAY_MODE_TRANSPARENT
        r.rgbColor = 0
        r.x = -37
        r.y = -60
        r.width = 0
        r.height = 0
        r.isAutoColor = true
        r.isShowKey = false
        r.isShowValue = true
        r.isVertical = false
        r.isAutoGeneratedText = true
        r.isGenerated = true
        r.isAutoLabelDeleted = false
        r.isAutoBackground = true
        r.rotation = 0.toShort()
        r.isShowCategoryLabelAsPercentage = false
        r.isShowValueAsPercentage = false
        r.isShowBubbleSizes = false
        r.isShowLabel = false
        r.indexOfColorValue = 77.toShort()
        r.dataLabelPlacement = 0.toShort()
        r.textRotation = 0.toShort()
        return r
    }

    private fun createUnknownTextRecord(): TextRecord {
        val r = TextRecord()
        r.horizontalAlignment = TextRecord.HORIZONTAL_ALIGNMENT_CENTER
        r.verticalAlignment = TextRecord.VERTICAL_ALIGNMENT_CENTER
        r.displayMode = TextRecord.DISPLAY_MODE_TRANSPARENT
        r.rgbColor = 0
        r.x = -37
        r.y = -60
        r.width = 0
        r.height = 0
        r.isAutoColor = true
        r.isShowKey = false
        r.isShowValue = false
        r.isVertical = false
        r.isAutoGeneratedText = true
        r.isGenerated = true
        r.isAutoLabelDeleted = false
        r.isAutoBackground = true
        r.rotation = 0.toShort()
        r.isShowCategoryLabelAsPercentage = false
        r.isShowValueAsPercentage = false
        r.isShowBubbleSizes = false
        r.isShowLabel = false
        r.indexOfColorValue = 77.toShort()
        r.dataLabelPlacement = 11088.toShort()
        r.textRotation = 0.toShort()
        return r
    }

    private fun createDefaultTextRecord(categoryDataType: Short): DefaultDataLabelTextPropertiesRecord {
        val r = DefaultDataLabelTextPropertiesRecord()
        r.categoryDataType = categoryDataType
        return r
    }

    private fun createSheetPropsRecord(): SheetPropertiesRecord {
        val r = SheetPropertiesRecord()
        r.isChartTypeManuallyFormatted = false
        r.isPlotVisibleOnly = true
        r.isDoNotSizeWithWindow = false
        r.isDefaultPlotDimensions = true
        r.isAutoPlotArea = false
        return r
    }

    private fun createSeriesToChartGroupRecord(): SeriesToChartGroupRecord {
        return SeriesToChartGroupRecord()
    }

    private fun createDataFormatRecord(): DataFormatRecord {
        val r = DataFormatRecord()
        r.pointNumber = (-1).toShort()
        r.seriesIndex = 0.toShort()
        r.seriesNumber = 0.toShort()
        r.isUseExcel4Colors = false
        return r
    }

    private fun createCategoriesLinkedDataRecord(): LinkedDataRecord {
        val r = LinkedDataRecord()
        r.linkType = LinkedDataRecord.LINK_TYPE_CATEGORIES
        r.referenceType = LinkedDataRecord.REFERENCE_TYPE_WORKSHEET
        r.isCustomNumberFormat = false
        r.indexNumberFmtRecord = 0.toShort()
        val p = Area3DPtg(
            0, 31, 1, 1,
            false, false, false, false, 0
        )
        r.setFormulaOfLink(arrayOf<Ptg?>(p))
        return r
    }

    private fun createValuesLinkedDataRecord(): LinkedDataRecord {
        val r = LinkedDataRecord()
        r.linkType = LinkedDataRecord.LINK_TYPE_VALUES
        r.referenceType = LinkedDataRecord.REFERENCE_TYPE_WORKSHEET
        r.isCustomNumberFormat = false
        r.indexNumberFmtRecord = 0.toShort()
        val p = Area3DPtg(
            0, 31, 0, 0,
            false, false, false, false, 0
        )
        r.setFormulaOfLink(arrayOf<Ptg?>(p))
        return r
    }

    private fun createTitleLinkedDataRecord(): LinkedDataRecord {
        val r = LinkedDataRecord()
        r.linkType = LinkedDataRecord.LINK_TYPE_TITLE_OR_TEXT
        r.referenceType = LinkedDataRecord.REFERENCE_TYPE_DIRECT
        r.isCustomNumberFormat = false
        r.indexNumberFmtRecord = 0.toShort()
        r.setFormulaOfLink(null)
        return r
    }

    private fun createSeriesRecord(): SeriesRecord {
        val r = SeriesRecord()
        r.categoryDataType = SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC
        r.valuesDataType = SeriesRecord.VALUES_DATA_TYPE_NUMERIC
        r.numCategories = 32.toShort()
        r.numValues = 31.toShort()
        r.bubbleSeriesType = SeriesRecord.BUBBLE_SERIES_TYPE_NUMERIC
        r.numBubbleValues = 0.toShort()
        return r
    }

    private fun createEndRecord(): EndRecord {
        return EndRecord()
    }

    private fun createAreaFormatRecord1(): AreaFormatRecord {
        val r = AreaFormatRecord()
        r.foregroundColor = 16777215 // RGB Color
        r.backgroundColor = 0 // RGB Color
        r.pattern = 1.toShort() // TODO: Add Pattern constants to record
        r.isAutomatic = true
        r.isInvert = false
        r.forecolorIndex = 78.toShort()
        r.backcolorIndex = 77.toShort()
        return r
    }

    private fun createAreaFormatRecord2(): AreaFormatRecord {
        val r = AreaFormatRecord()
        r.foregroundColor = 0x00c0c0c0
        r.backgroundColor = 0x00000000
        r.pattern = 1.toShort()
        r.isAutomatic = false
        r.isInvert = false
        r.forecolorIndex = 22.toShort()
        r.backcolorIndex = 79.toShort()
        return r
    }

    private fun createLineFormatRecord(drawTicks: Boolean): LineFormatRecord {
        val r = LineFormatRecord()
        r.lineColor = 0
        r.linePattern = LineFormatRecord.LINE_PATTERN_SOLID
        r.weight = (-1).toShort()
        r.isAuto = true
        r.isDrawTicks = drawTicks
        r.colourPaletteIndex = 77.toShort() // what colour is this?
        return r
    }

    private fun createLineFormatRecord2(): LineFormatRecord {
        val r = LineFormatRecord()
        r.lineColor = 0x00808080
        r.linePattern = 0.toShort()
        r.weight = 0.toShort()
        r.isAuto = false
        r.isDrawTicks = false
        r.isUnknown = false
        r.colourPaletteIndex = 23.toShort()
        return r
    }

    private fun createFrameRecord1(): FrameRecord {
        val r = FrameRecord()
        r.borderType = FrameRecord.BORDER_TYPE_REGULAR
        r.isAutoSize = false
        r.isAutoPosition = true
        return r
    }

    private fun createFrameRecord2(): FrameRecord {
        val r = FrameRecord()
        r.borderType = FrameRecord.BORDER_TYPE_REGULAR
        r.isAutoSize = true
        r.isAutoPosition = true
        return r
    }

    private fun createPlotGrowthRecord(horizScale: Int, vertScale: Int): PlotGrowthRecord {
        val r = PlotGrowthRecord()
        r.horizontalScale = horizScale
        r.verticalScale = vertScale
        return r
    }

    private fun createSCLRecord(numerator: Short, denominator: Short): SCLRecord {
        val r = SCLRecord()
        r.setDenominator(denominator)
        r.setNumerator(numerator)
        return r
    }

    private fun createBeginRecord(): BeginRecord {
        return BeginRecord()
    }

    private fun createChartRecord(x: Int, y: Int, width: Int, height: Int): ChartRecord {
        val r = ChartRecord()
        r.x = x
        r.y = y
        r.width = width
        r.height = height
        return r
    }

    private fun createUnitsRecord(): UnitsRecord {
        val r = UnitsRecord()
        r.units = 0.toShort()
        return r
    }


    /**
     * A series in a chart
     */
    inner class HSSFSeries /* package */ internal constructor(
        /**
         * @return record with series
         */
        val series: SeriesRecord
    ) {
        /**
         * 
         * @return
         */
        var seriesTextRecord: SeriesTextRecord? = null

        /**
         * @return record with data names
         */
        var dataName: LinkedDataRecord? = null
            private set

        /**
         * @return record with data values
         */
        var dataValues: LinkedDataRecord? = null
            private set

        /**
         * @return record with data category labels
         */
        var dataCategoryLabels: LinkedDataRecord? = null
            private set

        /**
         * @return record with data secondary category labels
         */
        var dataSecondaryCategoryLabels: LinkedDataRecord? = null
            private set
        /**
         * 
         * @return
         */
        /**
         * 
         * @param areaFormatRecord
         */
        var areaFormat: AreaFormatRecord? = null

        //data label property
        var textRecord: TextRecord? = null
        var dataLabelExtensionRecord: DataLabelExtensionRecord? = null

        /* package */
        fun insertData(data: LinkedDataRecord) {
            when (data.linkType.toInt()) {
                0 -> dataName = data
                1 -> dataValues = data
                2 -> dataCategoryLabels = data
                3 -> dataSecondaryCategoryLabels = data
            }
        }

        /* package */
        fun setSeriesTitleText(seriesTitleText: SeriesTextRecord?) {
            this.seriesTextRecord = seriesTitleText
        }

        val numValues: Short
            get() = series.numValues
        val valueType: Short
            /**
             * See [SeriesRecord]
             */
            get() = series.valuesDataType

        var seriesTitle: String?
            /**
             * Returns the series' title, if there is one,
             * or null if not
             */
            get() {
                if (this.seriesTextRecord != null) {
                    return seriesTextRecord!!.text
                }
                return null
            }
            /**
             * Changes the series' title, but only if there
             * was one already.
             * TODO - add in the records if not
             */
            set(title) {
                if (this.seriesTextRecord != null) {
                    seriesTextRecord!!.text = title!!
                } else {
                    throw IllegalStateException("No series title found to change")
                }
            }

        private fun getCellRange(linkedDataRecord: LinkedDataRecord?): CellRangeAddressBase? {
            if (linkedDataRecord == null) {
                return null
            }

            var firstRow = 0
            var lastRow = 0
            var firstCol = 0
            var lastCol = 0

            for (ptg in linkedDataRecord.formulaOfLink) {
                if (ptg is AreaPtgBase) {
                    val areaPtg = ptg

                    firstRow = areaPtg.firstRow
                    lastRow = areaPtg.lastRow

                    firstCol = areaPtg.firstColumn
                    lastCol = areaPtg.lastColumn
                }
            }

            return CellRangeAddress(firstRow, lastRow, firstCol, lastCol)
        }

        var valuesCellRange: CellRangeAddressBase?
            get() = getCellRange(dataValues)
            set(range) {
                val count = setVerticalCellRange(dataValues, range!!)
                if (count == null) {
                    return
                }

                series.numValues = count.toShort()
            }

        var categoryLabelsCellRange: CellRangeAddressBase?
            get() = getCellRange(dataCategoryLabels)
            set(range) {
                val count = setVerticalCellRange(dataCategoryLabels, range!!)
                if (count == null) {
                    return
                }

                series.numCategories = count.toShort()
            }

        private fun setVerticalCellRange(
            linkedDataRecord: LinkedDataRecord?,
            range: CellRangeAddressBase
        ): Int? {
            if (linkedDataRecord == null) {
                return null
            }

            val ptgList: MutableList<Ptg?> = ArrayList<Ptg?>()

            val rowCount = (range.getLastRow() - range.getFirstRow()) + 1
            val colCount = (range.getLastColumn() - range.getFirstColumn()) + 1

            for (ptg in linkedDataRecord.formulaOfLink) {
                if (ptg is AreaPtgBase) {
                    val areaPtg = ptg

                    areaPtg.setFirstRow(range.getFirstRow())
                    areaPtg.setLastRow(range.getLastRow())

                    areaPtg.setFirstColumn(range.getFirstColumn())
                    areaPtg.setLastColumn(range.getLastColumn())
                    ptgList.add(areaPtg)
                }
            }

            linkedDataRecord.setFormulaOfLink(ptgList.toTypedArray<Ptg?>())

            return rowCount * colCount
        }
    }

    @Throws(Exception::class)
    fun createSeries(): HSSFSeries? {
        val seriesTemplate = ArrayList<RecordBase?>()
        var seriesTemplateFilled = false

        var idx = 0
        var deep = 0
        var chartRecordIdx = -1
        var chartDeep = -1
        var lastSeriesDeep = -1
        var endSeriesRecordIdx = -1
        var seriesIdx = 0
        val records: MutableList<RecordBase> = sheet!!.sheet.records


        /* store first series as template and find last series index */
        for (record in records) {
            idx++

            if (record is BeginRecord) {
                deep++
            } else if (record is EndRecord) {
                deep--

                if (lastSeriesDeep == deep) {
                    lastSeriesDeep = -1
                    endSeriesRecordIdx = idx
                    if (!seriesTemplateFilled) {
                        seriesTemplate.add(record)
                        seriesTemplateFilled = true
                    }
                }

                if (chartDeep == deep) {
                    break
                }
            }

            if (record is ChartRecord) {
                if (record === chartRecord) {
                    chartRecordIdx = idx
                    chartDeep = deep
                }
            } else if (record is SeriesRecord) {
                if (chartRecordIdx != -1) {
                    seriesIdx++
                    lastSeriesDeep = deep
                }
            }

            if (lastSeriesDeep != -1 && !seriesTemplateFilled) {
                seriesTemplate.add(record)
            }
        }


        /* check if a series was found */
        if (endSeriesRecordIdx == -1) {
            return null
        }


        /* next index in the records list where the new series can be inserted */
        idx = endSeriesRecordIdx + 1

        var newSeries: HSSFSeries? = null


        /* duplicate record of the template series */
        val clonedRecords = ArrayList<RecordBase?>()
        for (record in seriesTemplate) {
            var newRecord: Record? = null

            if (record is BeginRecord) {
                newRecord = BeginRecord()
            } else if (record is EndRecord) {
                newRecord = EndRecord()
            } else if (record is SeriesRecord) {
                val seriesRecord = record.clone() as SeriesRecord
                newSeries = HSSFSeries(seriesRecord)
                newRecord = seriesRecord
            } else if (record is LinkedDataRecord) {
                val linkedDataRecord = record.clone() as LinkedDataRecord
                if (newSeries != null) {
                    newSeries.insertData(linkedDataRecord)
                }
                newRecord = linkedDataRecord
            } else if (record is DataFormatRecord) {
                val dataFormatRecord = record.clone() as DataFormatRecord

                dataFormatRecord.seriesIndex = seriesIdx.toShort()
                dataFormatRecord.seriesNumber = seriesIdx.toShort()

                newRecord = dataFormatRecord
            } else if (record is SeriesTextRecord) {
                val seriesTextRecord = record.clone() as SeriesTextRecord
                if (newSeries != null) {
                    newSeries.setSeriesTitleText(seriesTextRecord)
                }
                newRecord = seriesTextRecord
            } else if (record is Record) {
                newRecord = record.clone() as Record
            }

            if (newRecord != null) {
                clonedRecords.add(newRecord)
            }
        }


        /* check if a user model series object was created */
        if (newSeries == null) {
            return null
        }


        /* transfer series to record list */
        for (record in clonedRecords) {
            records.add(idx++, record!!)
        }

        return newSeries
    }

    fun removeSeries(series: HSSFSeries?) {
        this.series.remove(series)
        //		int idx = 0;
//		int deep = 0;
//		int chartDeep = -1;
//		int lastSeriesDeep = -1;
//		int seriesIdx = -1;
//		boolean removeSeries = false;
//		boolean chartEntered = false;
//		boolean result = false;
//		final List<RecordBase> records = sheet.getSheet().getRecords();
//		
//		/* store first series as template and find last series index */
//		Iterator<RecordBase> iter = records.iterator();
//		while (iter.hasNext())
//		{		
//			RecordBase record = iter.next();
//			idx++;
//			
//			if (record instanceof BeginRecord)
//			{
//				deep++;
//			} 
//			else if (record instanceof EndRecord)
//			{
//				deep--;
//				
//				if (lastSeriesDeep == deep)
//				{
//					lastSeriesDeep = -1;
//					
//					if (removeSeries)
//					{
//						removeSeries = false;
//						result = true;
//						iter.remove();
//					}
//				}
//				
//				if (chartDeep == deep) 
//				{
//					break;
//				}
//			}
//			
//			if (record instanceof ChartRecord)
//			{
//				if (record == chartRecord)
//				{
//					chartDeep = deep;
//					chartEntered = true;
//				}
//			}
//			else if (record instanceof SeriesRecord) 
//			{
//				if (chartEntered)
//				{
//					if (series.series == record)
//					{
//						lastSeriesDeep = deep;
//						removeSeries = true;
//					}
//					else 
//					{
//						seriesIdx++;
//					}
//				}
//			} 
//			else if (record instanceof DataFormatRecord) 
//			{
//				if (chartEntered && !removeSeries)
//				{
//					DataFormatRecord dataFormatRecord = (DataFormatRecord) record;
//					dataFormatRecord.setSeriesIndex((short) seriesIdx);
//					dataFormatRecord.setSeriesNumber((short) seriesIdx);
//				}
//			}
//			
//			if (removeSeries)
//			{
//				iter.remove();
//			}
//		}
//		
//		return result;
    }

    companion object {
        const val OBJECT_TYPE_CHART: Short = 5

        /**
         * convert records to chart's series
         * @param records
         * @param chart
         * @param seriesStart start index of series records
         * @return end index of series records
         */
        private fun convetRecordsToSeriesByPostion(
            records: MutableList<Record>,
            chart: HSSFChart?,
            seriesStart: Int
        ): Int {
            if (seriesStart >= records.size || records.get(seriesStart)
                    .getSid() != SeriesRecord.sid
            ) {
                return -1
            }
            var series = chart!!.HSSFSeries((records.get(seriesStart) as SeriesRecord?)!!)
            chart.series.add(series)

            var start = seriesStart + 1
            if (records.get(start) is BeginRecord) {
                var beginReocrdsCount = 1
                start++
                while (start <= records.size && beginReocrdsCount > 0) {
                    val r = records.get(start)
                    if (r is LinkedDataRecord) {
                        val linkedDataRecord = r
                        if (chart.series.size > 0) {
                            series = chart.series.get(chart.series.size - 1) as HSSFSeries
                            series.insertData(linkedDataRecord)
                        }
                    } else if (r is SeriesTextRecord) {
                        // Applies to a series                
                        val str = r
                        if (chart.series.size > 0) {
                            series = chart.series.get(chart.series.size - 1) as HSSFSeries
                            series.seriesTextRecord = str
                        }
                    } else if (r.getSid() == AreaFormatRecord.sid) {
                        val areaFormatRecord = r as AreaFormatRecord

                        series = chart.series.get(chart.series.size - 1) as HSSFSeries
                        series.areaFormat = areaFormatRecord
                    } else if (r is BeginRecord) {
                        beginReocrdsCount++
                    } else if (r is EndRecord) {
                        beginReocrdsCount--
                    }
                    start++
                }
            }
            return start - 1
        }

        /**
         * 
         * @param records
         * @param chart
         * @param seriesStart
         * @return
         */
        private fun convetRecordsToText(
            records: MutableList<Record>,
            chart: HSSFChart,
            seriesStart: Int
        ): Int {
            if (seriesStart >= records.size || records.get(seriesStart)
                    .getSid() != TextRecord.sid
            ) {
                return -1
            }

            val txtRecord = records.get(seriesStart) as TextRecord

            var str: SeriesTextRecord? = null
            var objLinkRecord: ObjectLinkRecord? = null
            var start = seriesStart + 1
            if (records.get(start) is BeginRecord) {
                var beginReocrdsCount = 1
                start++
                while (start <= records.size && beginReocrdsCount > 0) {
                    val r: Record? = records.get(start)
                    if (r is SeriesTextRecord) {
                        // Applies to a series                
                        str = records.get(start) as SeriesTextRecord?
                    } else if (r is ObjectLinkRecord) {
                        objLinkRecord = r
                    } else if (r is BeginRecord) {
                        beginReocrdsCount++
                    } else if (r is EndRecord) {
                        beginReocrdsCount--
                    }
                    start++
                }
            }

            if (txtRecord.width > 0 && txtRecord.height > 0 && objLinkRecord != null && chart.series.size > 0) {
                if (str != null) {
                    chart.seriesText.put(str, objLinkRecord)
                } else if (chart.series.size > chart.seriesText.size) {
                    chart.seriesText.put(
                        chart.series.get(chart.seriesText.size)!!.seriesTextRecord,
                        objLinkRecord
                    )
                }
            }

            return start - 1
        }

        /**
         * convert records to chart
         * @param records
         * @param chart
         */
        fun convertRecordsToChart(records: MutableList<Record>?, chart: HSSFChart?) {
            if (chart == null || records == null) {
                return
            }

            val size = records.size
            var index = 0
            var r: Record
            while (index < size) {
                r = records.get(index)
                if (r is ChartRecord) {
                    chart.setChartRecord(r)
                } else if (r is LegendRecord) {
                    chart.legendRecord = r
                } else if (r.getSid() == AreaFormatRecord.sid) {
                    if (chart.getSeries().size == 0) {
                        chart.marginColorFormat = r as AreaFormatRecord
                    } else {
                        chart.seriesBackgroundColorFormat = r as AreaFormatRecord
                    }
                } else if (r is SeriesRecord) {
                    //process one series
                    index = convetRecordsToSeriesByPostion(records, chart, index)
                } else if (r is TextRecord) {
                    //text property
                    index = convetRecordsToText(records, chart, index)
                    //	        	chart.series.get(chart.series.size() - 1).setTextRecord((TextRecord)r);
                } else if (r is DataLabelExtensionRecord) {
                    chart.series.get(chart.series.size - 1)!!.dataLabelExtensionRecord =
                        r
                } else if (r is ChartTitleFormatRecord) {
                    chart.chartTitleFormat =
                        r
                } else if (r is ValueRangeRecord) {
                    chart.valueRangeRecord.add(r)
                } else if (r.getSid() == AxisParentRecord.sid) {
                    //chart.axisParentRecord = new AxisParentRecord((UnknownRecord)r);
                } else if (r is Record) {
                    if (chart != null) {
                        val record = r
                        for (type in HSSFChartType.entries) {
                            if (type === HSSFChartType.Unknown) {
                                continue
                            }
                            if (record.getSid() == type.getSid()) {
                                chart.type = type
                                break
                            }
                        }
                    }
                }
                index++
            }
        }

        /**
         * Returns all the charts for the given sheet.
         * 
         * NOTE: You won't be able to do very much with
         * these charts yet, as this is very limited support
         */
        fun getSheetCharts(sheet: HSSFSheet): Array<HSSFChart?> {
            val charts: MutableList<HSSFChart?> = ArrayList<HSSFChart?>()
            var lastChart: HSSFChart? = null
            var lastSeries: HSSFSeries? = null
            // Find records of interest
            val records: MutableList<RecordBase> = sheet.sheet.records
            for (r in records) {
                if (r is ChartRecord) {
                    lastSeries = null
                    lastChart = HSSFChart(null, null, null, null)
                    lastChart.setChartRecord(r)
                    charts.add(lastChart)
                } else if (r is LegendRecord) {
                    lastChart!!.legendRecord = r
                } else if (r is SeriesRecord) {
                    val series = lastChart!!.HSSFSeries(r)
                    lastChart.series.add(series)
                    lastSeries = series
                } else if (r is ChartTitleFormatRecord) {
                    lastChart!!.chartTitleFormat =
                        r
                } else if (r is SeriesTextRecord) {
                    // Applies to a series, unless we've seen
                    //  a legend already
                    val str = r
                    if (lastChart!!.legendRecord == null &&
                        lastChart.series.size > 0
                    ) {
                        val series =
                            lastChart.series.get(lastChart.series.size - 1) as HSSFSeries
                        series.seriesTextRecord = str
                    }
                } else if (r is LinkedDataRecord) {
                    val linkedDataRecord = r
                    if (lastSeries != null) {
                        lastSeries.insertData(linkedDataRecord)
                    }
                } else if (r is ValueRangeRecord) {
                    lastChart!!.valueRangeRecord.add(r)
                } else if (r is Record) {
                    if (lastChart != null) {
                        val record = r
                        for (type in HSSFChartType.entries) {
                            if (type === HSSFChartType.Unknown) {
                                continue
                            }
                            if (record.getSid() == type.getSid()) {
                                lastChart.type = type
                                break
                            }
                        }
                    }
                }
            }

            return charts.toTypedArray<HSSFChart?>()
        }
    }
}
