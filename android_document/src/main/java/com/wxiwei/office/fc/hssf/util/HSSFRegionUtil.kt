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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.hssf.usermodel.HSSFSheet
import com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook
import com.wxiwei.office.fc.ss.util.HSSFCellRangeAddress
import com.wxiwei.office.fc.ss.util.Region
import com.wxiwei.office.fc.ss.util.RegionUtil


/**
 * Various utility functions that make working with a region of cells easier.
 * 
 * @author Eric Pugh epugh@upstate.com
 */
object HSSFRegionUtil {
    private fun toCRA(region: Region): HSSFCellRangeAddress {
        return Region.convertToCellRangeAddress(region)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setBorderLeft(
        border: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setBorderLeft(border.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the left border for a region of cells by manipulating the cell style
     * of the individual cells on the left
     * 
     * @param border The new border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setBorderLeft(
        border: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setBorderLeft(border, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setLeftBorderColor(
        color: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setLeftBorderColor(color.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the leftBorderColor attribute of the HSSFRegionUtil object
     * 
     * @param color The color of the border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setLeftBorderColor(
        color: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setLeftBorderColor(color, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setBorderRight(
        border: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setBorderRight(border.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the borderRight attribute of the HSSFRegionUtil object
     * 
     * @param border The new border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setBorderRight(
        border: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setBorderRight(border, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setRightBorderColor(
        color: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setRightBorderColor(color.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the rightBorderColor attribute of the HSSFRegionUtil object
     * 
     * @param color The color of the border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setRightBorderColor(
        color: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setRightBorderColor(color, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setBorderBottom(
        border: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setBorderBottom(border.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the borderBottom attribute of the HSSFRegionUtil object
     * 
     * @param border The new border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setBorderBottom(
        border: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setBorderBottom(border, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setBottomBorderColor(
        color: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setBottomBorderColor(color.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the bottomBorderColor attribute of the HSSFRegionUtil object
     * 
     * @param color The color of the border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setBottomBorderColor(
        color: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setBottomBorderColor(color, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setBorderTop(
        border: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setBorderTop(border.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the borderBottom attribute of the HSSFRegionUtil object
     * 
     * @param border The new border
     * @param region The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setBorderTop(
        border: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setBorderTop(border, region, sheet, workbook)
    }

    @Deprecated("(Aug 2008) use {@link HSSFCellRangeAddress} instead of {@link Region}")
    fun setTopBorderColor(
        color: Short, region: Region, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        setTopBorderColor(color.toInt(), toCRA(region), sheet, workbook)
    }

    /**
     * Sets the topBorderColor attribute of the HSSFRegionUtil object
     * 
     * @param color The color of the border
     * @param region  The region that should have the border
     * @param workbook The workbook that the region is on.
     * @param sheet The sheet that the region is on.
     */
    fun setTopBorderColor(
        color: Int, region: HSSFCellRangeAddress, sheet: HSSFSheet?,
        workbook: HSSFWorkbook?
    ) {
        RegionUtil.setTopBorderColor(color, region, sheet, workbook)
    }
}
