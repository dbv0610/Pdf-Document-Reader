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

import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.record.FontRecord
import com.wxiwei.office.fc.ss.usermodel.ICell

/**
 * Excel can get cranky if you give it files containing too
 * many (especially duplicate) objects, and this class can
 * help to avoid those.
 * In general, it's much better to make sure you don't
 * duplicate the objects in your code, as this is likely
 * to be much faster than creating lots and lots of
 * excel objects+records, only to optimise them down to
 * many fewer at a later stage.
 * However, sometimes this is too hard / tricky to do, which
 * is where the use of this class comes in.
 */
object HSSFOptimiser {
    /**
     * Goes through the Workbook, optimising the fonts by
     * removing duplicate ones.
     * For now, only works on fonts used in [HSSFCellStyle]
     * and [HSSFRichTextString]. Any other font uses
     * (eg charts, pictures) may well end up broken!
     * This can be a slow operation, especially if you have
     * lots of cells, cell styles or rich text strings
     * @param workbook The workbook in which to optimise the fonts
     */
    fun optimiseFonts(workbook: HSSFWorkbook) {
        // Where each font has ended up, and if we need to
        //  delete the record for it. Start off with no change
        val newPos =
            ShortArray(workbook.getWorkbook().numberOfFontRecords + 1)
        val zapRecords = BooleanArray(newPos.size)
        for (i in newPos.indices) {
            newPos[i] = i.toShort()
            zapRecords[i] = false
        }


        // Get each font record, so we can do deletes
        //  without getting confused
        val frecs = arrayOfNulls<FontRecord>(newPos.size)
        for (i in newPos.indices) {
            // There is no 4!
            if (i == 4) continue

            frecs[i] = workbook.getWorkbook().getFontRecordAt(i)
        }


        // Loop over each font, seeing if it is the same
        //  as an earlier one. If it is, point users of the
        //  later duplicate copy to the earlier one, and 
        //  mark the later one as needing deleting
        // Note - don't change built in fonts (those before 5)
        for (i in 5..<newPos.size) {
            // Check this one for being a duplicate
            //  of an earlier one
            var earlierDuplicate = -1
            var j = 0
            while (j < i && earlierDuplicate == -1) {
                if (j == 4) {
                    j++
                    continue
                }

                val frCheck = workbook.getWorkbook().getFontRecordAt(j)
                if (frCheck.sameProperties(frecs[i]!!)) {
                    earlierDuplicate = j
                }
                j++
            }


            // If we got a duplicate, mark it as such
            if (earlierDuplicate != -1) {
                newPos[i] = earlierDuplicate.toShort()
                zapRecords[i] = true
            }
        }


        // Update the new positions based on
        //  deletes that have occurred between
        //  the start and them
        // Only need to worry about user fonts
        for (i in 5..<newPos.size) {
            // Find the number deleted to that
            //  point, and adjust
            val preDeletePos = newPos[i]
            var newPosition = preDeletePos
            for (j in 0..<preDeletePos) {
                if (zapRecords[j]) newPosition--
            }


            // Update the new position
            newPos[i] = newPosition
        }


        // Zap the un-needed user font records
        for (i in 5..<newPos.size) {
            if (zapRecords[i]) {
                workbook.getWorkbook().removeFontRecord(
                    frecs[i]
                )
            }
        }


        // Tell HSSFWorkbook that it needs to
        //  re-start its HSSFFontCache
        workbook.resetFontCache()


        // Update the cell styles to point at the 
        //  new locations of the fonts
        for (i in 0..<workbook.getWorkbook().numExFormats) {
            val xfr = workbook.getWorkbook().getExFormatAt(i)
            if (xfr != null) {
                xfr.fontIndex = newPos[xfr.fontIndex.toInt()]
            }
        }


        // Update the rich text strings to point at
        //  the new locations of the fonts
        // Remember that one underlying unicode string
        //  may be shared by multiple RichTextStrings!
        val doneUnicodeStrings = HashSet<Any>()
        for (sheetNum in 0..<workbook.getNumberOfSheets()) {
            val s = workbook.getSheetAt(sheetNum)
            val rIt: MutableIterator<*> = s.rowIterator()
            while (rIt.hasNext()) {
                val row = rIt.next() as HSSFRow
                val cIt: MutableIterator<*> = row.cellIterator()
                while (cIt.hasNext()) {
                    val cell = cIt.next() as HSSFCell
                    if (cell.getCellType() == ICell.CELL_TYPE_STRING) {
                        val rtr = cell.getRichStringCellValue()
                        val u = rtr.rawUnicodeString


                        // Have we done this string already?
                        if (u != null && !doneUnicodeStrings.contains(u)) {
                            // Update for each new position
                            for (i in 5..<newPos.size) {
                                if (i.toShort() != newPos[i]) {
                                    u.swapFontUse(i.toShort(), newPos[i])
                                }
                            }


                            // Mark as done
                            doneUnicodeStrings.add(u)
                        }
                    }
                }
            }
        }
    }

    /**
     * Goes through the Wokrbook, optimising the cell styles
     * by removing duplicate ones.
     * For best results, optimise the fonts via a call to
     * [.optimiseFonts] first.
     * @param workbook The workbook in which to optimise the cell styles
     */
    fun optimiseCellStyles(workbook: HSSFWorkbook) {
        // Where each style has ended up, and if we need to
        //  delete the record for it. Start off with no change
        val newPos =
            ShortArray(workbook.getWorkbook().numExFormats)
        val zapRecords = BooleanArray(newPos.size)
        for (i in newPos.indices) {
            newPos[i] = i.toShort()
            zapRecords[i] = false
        }


        // Get each style record, so we can do deletes
        //  without getting confused
        val xfrs = arrayOfNulls<ExtendedFormatRecord>(newPos.size)
        for (i in newPos.indices) {
            xfrs[i] = workbook.getWorkbook().getExFormatAt(i)
        }


        // Loop over each style, seeing if it is the same
        //  as an earlier one. If it is, point users of the
        //  later duplicate copy to the earlier one, and 
        //  mark the later one as needing deleting
        // Only work on user added ones, which come after 20
        for (i in 21..<newPos.size) {
            // Check this one for being a duplicate
            //  of an earlier one
            var earlierDuplicate = -1
            var j = 0
            while (j < i && earlierDuplicate == -1) {
                val xfCheck = workbook.getWorkbook().getExFormatAt(j)
                if (xfCheck!!.equals(xfrs[i])) {
                    earlierDuplicate = j
                }
                j++
            }


            // If we got a duplicate, mark it as such
            if (earlierDuplicate != -1) {
                newPos[i] = earlierDuplicate.toShort()
                zapRecords[i] = true
            }
        }


        // Update the new positions based on
        //  deletes that have occurred between
        //  the start and them
        // Only work on user added ones, which come after 20
        for (i in 21..<newPos.size) {
            // Find the number deleted to that
            //  point, and adjust
            val preDeletePos = newPos[i]
            var newPosition = preDeletePos
            for (j in 0..<preDeletePos) {
                if (zapRecords[j]) newPosition--
            }


            // Update the new position
            newPos[i] = newPosition
        }


        // Zap the un-needed user style records
        for (i in 21..<newPos.size) {
            if (zapRecords[i]) {
                workbook.getWorkbook().removeExFormatRecord(
                    xfrs[i]
                )
            }
        }


        // Finally, update the cells to point at
        //  their new extended format records
        for (sheetNum in 0..<workbook.getNumberOfSheets()) {
            val s = workbook.getSheetAt(sheetNum)
            val rIt: MutableIterator<*> = s.rowIterator()
            while (rIt.hasNext()) {
                val row = rIt.next() as HSSFRow
                val cIt: MutableIterator<*> = row.cellIterator()
                while (cIt.hasNext()) {
                    val cell = cIt.next() as HSSFCell
                    val oldXf = cell.cellValueRecord.xFIndex
                    val newStyle = workbook.getCellStyleAt(
                        newPos[oldXf.toInt()]
                    )
                    cell.setCellStyle(newStyle)
                }
            }
        }
    }
}
