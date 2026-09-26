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

import com.wxiwei.office.fc.ddf.EscherBSERecord
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord.FileIdCluster
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherProperty
import com.wxiwei.office.fc.ddf.EscherRGBProperty
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherSplitMenuColorsRecord
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalName
import com.wxiwei.office.fc.hssf.formula.EvaluationWorkbook.ExternalSheet
import com.wxiwei.office.fc.hssf.formula.FormulaShifter
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NameXPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperandPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.Ref3DPtg
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.record.BOFRecord
import com.wxiwei.office.fc.hssf.record.BackupRecord
import com.wxiwei.office.fc.hssf.record.BookBoolRecord
import com.wxiwei.office.fc.hssf.record.BoundSheetRecord
import com.wxiwei.office.fc.hssf.record.CodepageRecord
import com.wxiwei.office.fc.hssf.record.CountryRecord
import com.wxiwei.office.fc.hssf.record.DSFRecord
import com.wxiwei.office.fc.hssf.record.DateWindow1904Record
import com.wxiwei.office.fc.hssf.record.DrawingGroupRecord
import com.wxiwei.office.fc.hssf.record.EOFRecord
import com.wxiwei.office.fc.hssf.record.EscherAggregate
import com.wxiwei.office.fc.hssf.record.ExtSSTRecord
import com.wxiwei.office.fc.hssf.record.ExtendedFormatRecord
import com.wxiwei.office.fc.hssf.record.ExternSheetRecord
import com.wxiwei.office.fc.hssf.record.FileSharingRecord
import com.wxiwei.office.fc.hssf.record.FnGroupCountRecord
import com.wxiwei.office.fc.hssf.record.FontRecord
import com.wxiwei.office.fc.hssf.record.FormatRecord
import com.wxiwei.office.fc.hssf.record.HideObjRecord
import com.wxiwei.office.fc.hssf.record.HyperlinkRecord
import com.wxiwei.office.fc.hssf.record.InterfaceEndRecord
import com.wxiwei.office.fc.hssf.record.InterfaceHdrRecord
import com.wxiwei.office.fc.hssf.record.MMSRecord
import com.wxiwei.office.fc.hssf.record.NameCommentRecord
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.PaletteRecord
import com.wxiwei.office.fc.hssf.record.PasswordRecord
import com.wxiwei.office.fc.hssf.record.PasswordRev4Record
import com.wxiwei.office.fc.hssf.record.PrecisionRecord
import com.wxiwei.office.fc.hssf.record.ProtectRecord
import com.wxiwei.office.fc.hssf.record.ProtectionRev4Record
import com.wxiwei.office.fc.hssf.record.RecalcIdRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RefreshAllRecord
import com.wxiwei.office.fc.hssf.record.SSTRecord
import com.wxiwei.office.fc.hssf.record.StyleRecord
import com.wxiwei.office.fc.hssf.record.SupBookRecord
import com.wxiwei.office.fc.hssf.record.TabIdRecord
import com.wxiwei.office.fc.hssf.record.UseSelFSRecord
import com.wxiwei.office.fc.hssf.record.WindowOneRecord
import com.wxiwei.office.fc.hssf.record.WindowProtectRecord
import com.wxiwei.office.fc.hssf.record.WriteAccessRecord
import com.wxiwei.office.fc.hssf.record.WriteProtectRecord
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.util.HSSFColor
import com.wxiwei.office.fc.ss.usermodel.BuiltinFormats
import com.wxiwei.office.fc.util.Internal
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.fc.util.POILogger
import com.wxiwei.office.system.AbortReaderError
import com.wxiwei.office.system.AbstractReader
import java.security.AccessControlException
import java.util.Locale

/**
 * Low level model implementation of a Workbook.  Provides creational methods
 * for settings and objects contained in the workbook object.
 * <P>
 * This file contains the low level binary records starting at the workbook's BOF and
 * ending with the workbook's EOF.  Use HSSFWorkbook for a high level representation.
</P> * <P>
 * The structures of the highlevel API use references to this to perform most of their
 * operations.  Its probably unwise to use these low level structures directly unless you
 * really know what you're doing.  I recommend you read the Microsoft Excel 97 Developer's
 * Kit (Microsoft Press) and the documentation at http://sc.openoffice.org/excelfileformat.pdf
 * before even attempting to use this.
 * 
 * 
 * @author  Luc Girardin (luc dot girardin at macrofocus dot com)
 * @author  Sergei Kozello (sergeikozello at mail.ru)
 * @author  Shawn Laubach (slaubach at apache dot org) (Data Formats)
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Brian Sanders (bsanders at risklabs dot com) - custom palette
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @see com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook
</P> */
@Internal
class InternalWorkbook
private constructor() {
    /**
     * this contains the Worksheet record objects
     */
    private val records: WorkbookRecordList

    /**
     * this contains a reference to the SSTRecord so that new stings can be added
     * to it.
     */
    protected var sst: SSTRecord? = null

    private var linkTable: LinkTable? =
        null // optionally occurs if there are  references in the document. (4.10.3)

    /**
     * holds the "boundsheet" records (aka bundlesheet) so that they can have their
     * reference to their "BOF" marker
     */
    private val boundsheets: MutableList<BoundSheetRecord>

    /**
     * Returns the list of FormatRecords in the workbook.
     * @return ArrayList of FormatRecords in the notebook
     */
    @JvmField
    val formats: MutableList<FormatRecord>
    val hyperlinks: MutableList<HyperlinkRecord?>

    /** the number of extended format records  */
    private var numxfs: Int
    /**
     * gets the number of font records
     * 
     * @return   number of font records in the "font table"
     */
    /** the number of font records  */
    var numberOfFontRecords: Int
        private set

    /** holds the max format id  */
    private var maxformatid: Int
    /**
     * Whether date windowing is based on 1/2/1904 or 1/1/1900.
     * Some versions of Excel (Mac) can save workbooks using 1904 date windowing.
     * 
     * @return true if using 1904 date windowing
     */
    /** whether 1904 date windowing is being used  */
    var isUsing1904DateWindowing: Boolean
        private set
    var drawingManager: DrawingManager2? = null
        private set
    private val escherBSERecords: MutableList<EscherBSERecord?>
    var windowOne: WindowOneRecord? = null
        private set
    private var fileShare: FileSharingRecord? = null
    private var writeAccess: WriteAccessRecord? = null
    private var writeProtect: WriteProtectRecord? = null

    /**
     * Hold the [NameCommentRecord]s indexed by the name of the [NameRecord] to which they apply.
     */
    private val commentRecords: MutableMap<String?, NameCommentRecord?>

    init {
        records = WorkbookRecordList()

        boundsheets = ArrayList<BoundSheetRecord>()
        formats = ArrayList<FormatRecord>()
        hyperlinks = ArrayList<HyperlinkRecord?>()
        numxfs = 0
        this.numberOfFontRecords = 0
        maxformatid = -1
        this.isUsing1904DateWindowing = false
        escherBSERecords = ArrayList<EscherBSERecord?>()
        commentRecords = LinkedHashMap<String?, NameCommentRecord?>()
    }

    /**Retrieves the Builtin NameRecord that matches the name and index
     * There shouldn't be too many names to make the sequential search too slow
     * @param name byte representation of the builtin name to match
     * @param sheetNumber 1-based sheet number
     * @return null if no builtin NameRecord matches
     */
    fun getSpecificBuiltinRecord(name: Byte, sheetNumber: Int): NameRecord? {
        return this.orCreateLinkTable.getSpecificBuiltinRecord(name, sheetNumber)
    }

    /**
     * Removes the specified Builtin NameRecord that matches the name and index
     * @param name byte representation of the builtin to match
     * @param sheetIndex zero-based sheet reference
     */
    fun removeBuiltinRecord(name: Byte, sheetIndex: Int) {
        linkTable!!.removeBuiltinRecord(name, sheetIndex)
        // TODO - do we need "this.records.remove(...);" similar to that in this.removeName(int namenum) {}?
    }

    val numRecords: Int
        get() = records.size()

    /**
     * gets the font record at the given index in the font table.  Remember
     * "There is No Four" (someone at M$ must have gone to Rocky Horror one too
     * many times)
     * 
     * @param idx the index to look at (0 or greater but NOT 4)
     * @return FontRecord located at the given index
     */
    fun getFontRecordAt(idx: Int): FontRecord {
        var index = idx

        if (index > 4) {
            index -= 1 // adjust for "There is no 4"
        }
        if (index > (this.numberOfFontRecords - 1)) {
            throw ArrayIndexOutOfBoundsException(
                ("There are only " + this.numberOfFontRecords
                        + " font records, you asked for " + idx)
            )
        }
        val retval = records
            .get((records.fontpos - (this.numberOfFontRecords - 1)) + index) as FontRecord

        return retval
    }

    /**
     * Retrieves the index of the given font
     */
    fun getFontIndex(font: FontRecord?): Int {
        for (i in 0..this.numberOfFontRecords) {
            val thisFont = records.get(
                (records.fontpos - (this.numberOfFontRecords - 1))
                        + i
            ) as FontRecord
            if (thisFont == font) {
                // There is no 4!
                if (i > 3) {
                    return (i + 1)
                }
                return i
            }
        }
        throw IllegalArgumentException("Could not find that font!")
    }

    /**
     * creates a new font record and adds it to the "font table".  This causes the
     * boundsheets to move down one, extended formats to move down (so this function moves
     * those pointers as well)
     * 
     * @return FontRecord that was just created
     */
    fun createNewFont(): FontRecord {
        val rec: FontRecord = createFont()

        records.add(records.fontpos + 1, rec)
        records.fontpos = records.fontpos + 1
        this.numberOfFontRecords++
        return rec
    }

    /**
     * Removes the given font record from the
     * file's list. This will make all
     * subsequent font indicies drop by one,
     * so you'll need to update those yourself!
     */
    fun removeFontRecord(rec: FontRecord?) {
        records.remove(rec) // this updates FontPos for us
        this.numberOfFontRecords--
    }

    /**
     * Sets the BOF for a given sheet
     * 
     * @param sheetIndex the number of the sheet to set the positing of the bof for
     * @param pos the actual bof position
     */
    fun setSheetBof(sheetIndex: Int, pos: Int) {
        if (log.check(POILogger.DEBUG)) log.log(
            DEBUG, "setting bof for sheetnum =", sheetIndex, " at pos=",
            pos
        )
        checkSheets(sheetIndex)
        getBoundSheetRec(sheetIndex).positionOfBof = pos
    }

    private fun getBoundSheetRec(sheetIndex: Int): BoundSheetRecord {
        return boundsheets.get(sheetIndex)
    }

    val backupRecord: BackupRecord?
        /**
         * Returns the position of the backup record.
         */
        get() = records.get(records.backuppos) as BackupRecord?

    /**
     * sets the name for a given sheet.  If the boundsheet record doesn't exist and
     * its only one more than we have, go ahead and create it.  If it's > 1 more than
     * we have, except
     * 
     * @param sheetnum the sheet number (0 based)
     * @param sheetname the name for the sheet
     */
    fun setSheetName(sheetnum: Int, sheetname: String) {
        var sheetname = sheetname
        checkSheets(sheetnum)

        // YK: Mimic Excel and silently truncate sheet names longer than 31 characters
        if (sheetname.length > 31) sheetname = sheetname.substring(0, 31)

        val sheet = boundsheets.get(sheetnum)
        sheet.sheetname = sheetname
    }

    /**
     * Determines whether a workbook contains the provided sheet name.  For the purpose of
     * comparison, long names are truncated to 31 chars.
     * 
     * @param name the name to test (case insensitive match)
     * @param excludeSheetIdx the sheet to exclude from the check or -1 to include all sheets in the check.
     * @return true if the sheet contains the name, false otherwise.
     */
    fun doesContainsSheetName(name: String, excludeSheetIdx: Int): Boolean {
        var aName = name
        if (aName.length > MAX_SENSITIVE_SHEET_NAME_LEN) {
            aName = aName.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN)
        }
        for (i in boundsheets.indices) {
            val boundSheetRecord = getBoundSheetRec(i)
            if (excludeSheetIdx == i) {
                continue
            }
            var bName = boundSheetRecord.sheetname
            if (bName.length > MAX_SENSITIVE_SHEET_NAME_LEN) {
                bName = bName.substring(0, MAX_SENSITIVE_SHEET_NAME_LEN)
            }
            if (aName.equals(bName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * sets the order of appearance for a given sheet.
     * 
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */
    fun setSheetOrder(sheetname: String?, pos: Int) {
        val sheetNumber = getSheetIndex(sheetname)
        //remove the sheet that needs to be reordered and place it in the spot we want
        boundsheets.add(pos, boundsheets.removeAt(sheetNumber))
    }

    /**
     * gets the name for a given sheet.
     * 
     * @param sheetIndex the sheet number (0 based)
     * @return sheetname the name for the sheet
     */
    fun getSheetName(sheetIndex: Int): String {
        return getBoundSheetRec(sheetIndex).sheetname
    }

    /**
     * Gets the hidden flag for a given sheet.
     * Note that a sheet could instead be
     * set to be very hidden, which is different
     * ([.isSheetVeryHidden])
     * 
     * @param sheetnum the sheet number (0 based)
     * @return True if sheet is hidden
     */
    fun isSheetHidden(sheetnum: Int): Boolean {
        return getBoundSheetRec(sheetnum).isHidden
    }

    /**
     * Gets the very hidden flag for a given sheet.
     * This is different from the normal
     * hidden flag
     * ([.isSheetHidden])
     * 
     * @param sheetnum the sheet number (0 based)
     * @return True if sheet is very hidden
     */
    fun isSheetVeryHidden(sheetnum: Int): Boolean {
        return getBoundSheetRec(sheetnum).isVeryHidden
    }

    /**
     * Hide or unhide a sheet
     * 
     * @param sheetnum The sheet number
     * @param hidden True to mark the sheet as hidden, false otherwise
     */
    fun setSheetHidden(sheetnum: Int, hidden: Boolean) {
        getBoundSheetRec(sheetnum).isHidden = hidden
    }

    /**
     * Hide or unhide a sheet.
     * 0 = not hidden
     * 1 = hidden
     * 2 = very hidden.
     * 
     * @param sheetnum The sheet number
     * @param hidden 0 for not hidden, 1 for hidden, 2 for very hidden
     */
    fun setSheetHidden(sheetnum: Int, hidden: Int) {
        val bsr = getBoundSheetRec(sheetnum)
        var h = false
        var vh = false
        if (hidden == 0) {
        } else if (hidden == 1) {
            h = true
        } else if (hidden == 2) {
            vh = true
        } else {
            throw IllegalArgumentException(
                ("Invalid hidden flag " + hidden
                        + " given, must be 0, 1 or 2")
            )
        }
        bsr.isHidden = h
        bsr.isVeryHidden = vh
    }

    /**
     * get the sheet's index
     * @param name  sheet name
     * @return sheet index or -1 if it was not found.
     */
    fun getSheetIndex(name: String?): Int {
        var retval = -1

        for (k in boundsheets.indices) {
            val sheet = getSheetName(k)

            if (sheet.equals(name, ignoreCase = true)) {
                retval = k
                break
            }
        }
        return retval
    }

    /**
     * if we're trying to address one more sheet than we have, go ahead and add it!  if we're
     * trying to address >1 more than we have throw an exception!
     */
    private fun checkSheets(sheetnum: Int) {
        if ((boundsheets.size) <= sheetnum) { // if we're short one add another..
            if ((boundsheets.size + 1) <= sheetnum) {
                throw RuntimeException("Sheet number out of bounds!")
            }
            val bsr: BoundSheetRecord = createBoundSheet(sheetnum)

            records.add(records.bspos + 1, bsr)
            records.bspos = records.bspos + 1
            boundsheets.add(bsr)
            this.orCreateLinkTable.checkExternSheet(sheetnum)
            fixTabIdRecord()
        } else {
            // Ensure we have enough tab IDs
            // Can be a few short if new sheets were added
            if (records.tabpos > 0) {
                val tir = records.get(records.tabpos) as TabIdRecord
                if (tir._tabids.size < boundsheets.size) {
                    fixTabIdRecord()
                }
            }
        }
    }

    /**
     * @param sheetIndex zero based sheet index
     */
    fun removeSheet(sheetIndex: Int) {
        if (boundsheets.size > sheetIndex) {
            records.remove(records.bspos - (boundsheets.size - 1) + sheetIndex)
            boundsheets.removeAt(sheetIndex)
            fixTabIdRecord()
        }

        // Within NameRecords, it's ok to have the formula
        //  part point at deleted sheets. It's also ok to
        //  have the ExternSheetNumber point at deleted
        //  sheets.
        // However, the sheet index must be adjusted, or
        //  excel will break. (Sheet index is either 0 for
        //  global, or 1 based index to sheet)
        val sheetNum1Based = sheetIndex + 1
        for (i in 0..<this.numNames) {
            val nr = getNameRecord(i)

            if (nr.getSheetNumber() == sheetNum1Based) {
                // Excel re-writes these to point to no sheet
                nr.setSheetNumber(0)
            } else if (nr.getSheetNumber() > sheetNum1Based) {
                // Bump down by one, so still points
                //  at the same sheet
                nr.setSheetNumber(nr.getSheetNumber() - 1)
            }
        }
    }

    /**
     * make the tabid record look like the current situation.
     * 
     */
    private fun fixTabIdRecord() {
        val tir = records.get(records.tabpos) as TabIdRecord
        val tia = ShortArray(boundsheets.size)

        for (k in tia.indices) {
            tia[k] = k.toShort()
        }
        tir.setTabIdArray(tia)
    }

    val numSheets: Int
        /**
         * returns the number of boundsheet objects contained in this workbook.
         * 
         * @return number of BoundSheet records
         */
        get() {
            if (log.check(POILogger.DEBUG)) log.log(
                DEBUG,
                "getNumSheets=",
                boundsheets.size
            )
            return boundsheets.size
        }

    val numExFormats: Int
        /**
         * get the number of ExtendedFormat records contained in this workbook.
         * 
         * @return int count of ExtendedFormat records
         */
        get() {
            if (log.check(POILogger.DEBUG)) log.log(
                DEBUG,
                "getXF=",
                numxfs
            )
            return numxfs
        }

    /**
     * gets the ExtendedFormatRecord at the given 0-based index
     * 
     * @param index of the Extended format record (0-based)
     * @return ExtendedFormatRecord at the given index
     */
    fun getExFormatAt(index: Int): ExtendedFormatRecord? {
        var xfptr: Int = records.xfpos - (numxfs - 1)

        xfptr += index
        val roc = records.get(xfptr)
        if (roc is ExtendedFormatRecord) {
            return roc
        } else {
            return null
        }
    }

    /**
     * Removes the given ExtendedFormatRecord record from the
     * file's list. This will make all
     * subsequent font indicies drop by one,
     * so you'll need to update those yourself!
     */
    fun removeExFormatRecord(rec: ExtendedFormatRecord?) {
        records.remove(rec) // this updates XfPos for us
        numxfs--
    }

    /**
     * creates a new Cell-type Extneded Format Record and adds it to the end of
     * ExtendedFormatRecords collection
     * 
     * @return ExtendedFormatRecord that was created
     */
    fun createCellXF(): ExtendedFormatRecord {
        val xf: ExtendedFormatRecord = createExtendedFormat()

        records.add(records.xfpos + 1, xf)
        records.xfpos = records.xfpos + 1
        numxfs++
        return xf
    }

    /**
     * Returns the StyleRecord for the given
     * xfIndex, or null if that ExtendedFormat doesn't
     * have a Style set.
     */
    fun getStyleRecord(xfIndex: Int): StyleRecord? {
        // Style records always follow after
        //  the ExtendedFormat records
        for (i in records.xfpos..<records.size()) {
            val r = records.get(i)
            if (r is ExtendedFormatRecord) {
                continue
            }
            if (r !is StyleRecord) {
                continue
            }
            val sr = r
            if (sr.getXFIndex() == xfIndex) {
                return sr
            }
        }
        return null
    }

    /**
     * Creates a new StyleRecord, for the given Extended
     * Format index, and adds it onto the end of the
     * records collection
     */
    fun createStyleRecord(xfIndex: Int): StyleRecord {
        // Style records always follow after
        //  the ExtendedFormat records
        val newSR = StyleRecord()
        newSR.setXFIndex(xfIndex)

        // Find the spot
        var addAt = -1
        var i: Int = records.xfpos
        while (i < records.size() && addAt == -1) {
            val r = records.get(i)
            if (r is ExtendedFormatRecord || r is StyleRecord) {
                // Keep going
            } else {
                addAt = i
            }
            i++
        }
        check(addAt != -1) { "No XF Records found!" }
        records.add(addAt, newSR)

        return newSR
    }

    /**
     * Adds a string to the SST table and returns its index (if its a duplicate
     * just returns its index and update the counts) ASSUMES compressed unicode
     * (meaning 8bit)
     * 
     * @param string the string to be added to the SSTRecord
     * 
     * @return index of the string within the SSTRecord
     */
    fun addSSTString(string: UnicodeString?): Int {
        if (log.check(POILogger.DEBUG)) log.log(DEBUG, "insert to sst string='", string)
        if (sst == null) {
            insertSST()
        }
        return sst!!.addString(string)
    }

    @get:JvmName("getSSTUniqueStringSizeProperty")
    val sSTUniqueStringSize: Int
        /**
         * 
         * @return
         */
        get() = sst!!.getNumUniqueStrings()

    fun getSSTUniqueStringSize(): Int = sSTUniqueStringSize

    /**
     * given an index into the SST table, this function returns the corresponding String value
     * @return String containing the SST String
     */
    fun getSSTString(str: Int): UnicodeString? {
        if (sst == null) {
            insertSST()
        }
        val retval = sst!!.getString(str)

        if (log.check(POILogger.DEBUG)) log.log(
            DEBUG,
            "Returning SST for index=",
            str,
            " String= ",
            retval
        )
        return retval
    }

    /**
     * use this function to add a Shared String Table to an existing sheet (say
     * generated by a different java api) without an sst....
     * @see .createExtendedSST
     * @see SSTRecord
     */
    fun insertSST() {
        if (log.check(POILogger.DEBUG)) log.log(DEBUG, "creating new SST via insertSST!")
        val newSst = SSTRecord()
        sst = newSst
        records.add(records.size() - 1, createExtendedSST())
        records.add(records.size() - 2, newSst)
    }

    /**
     * Serializes all records int the worksheet section into a big byte array. Use
     * this to write the Workbook out.
     * 
     * @return byte array containing the HSSF-only portions of the POIFS file.
     */
    // GJS: Not used so why keep it.
    //    public byte [] serialize() {
    //        log.log(DEBUG, "Serializing Workbook!");
    //        byte[] retval    = null;
    //
    //         ArrayList bytes     = new ArrayList(records.size()); */ //        int    arraysize = getSize();
    //        int    pos       = 0;
    //
    //        retval = new byte[ arraysize ];
    //        for (int k = 0; k < records.size(); k++) {
    //
    //            Record record = records.get(k);
    //             Let's skip RECALCID records, as they are only use for optimization */ //        if(record.getSid() != RecalcIdRecord.sid || ((RecalcIdRecord)record).isNeeded()) {
    //                pos += record.serialize(pos, retval);   // rec.length;
    //        }
    //        }
    //        log.log(DEBUG, "Exiting serialize workbook");
    //        return retval;
    //    }
    /**
     * Serializes all records int the worksheet section into a big byte array. Use
     * this to write the Workbook out.
     * @param offset of the data to be written
     * @param data array of bytes to write this to
     */
    fun serialize(offset: Int, data: ByteArray): Int {
        if (log.check(POILogger.DEBUG)) log.log(DEBUG, "Serializing Workbook with offsets")

        var pos = 0

        var sst: SSTRecord? = null
        var sstPos = 0
        var wroteBoundSheets = false
        for (k in 0..<records.size()) {
            var record = records.get(k)
            var len = 0
            if (record is SSTRecord) {
                sst = record
                sstPos = pos
            }
            if (record.getSid() == ExtSSTRecord.sid && sst != null) {
                record = sst.createExtSSTRecord(sstPos + offset)
            }
            if (record is BoundSheetRecord) {
                if (!wroteBoundSheets) {
                    for (i in boundsheets.indices) {
                        len += getBoundSheetRec(i).serialize(pos + offset + len, data)
                    }
                    wroteBoundSheets = true
                }
            } else {
                len = record.serialize(pos + offset, data)
            }
            ///  DEBUG BEGIN ///// */
            //                if (len != record.getRecordSize())
            //                    throw new IllegalStateException("Record size does not match serialized bytes.  Serialized size = " + len + " but getRecordSize() returns " + record.getRecordSize());
            ///  DEBUG END ///// */
                    pos += len // rec.length;
        }
        if (log.check(POILogger.DEBUG)) log.log(DEBUG, "Exiting serialize workbook")
        return pos
    }

    val size: Int
        get() {
            var retval = 0

            var sst: SSTRecord? = null
            for (k in 0..<records.size()) {
                val record = records.get(k)
                if (record is SSTRecord) sst = record
                if (record.getSid() == ExtSSTRecord.sid && sst != null) retval += sst.calcExtSSTRecordSize()
                else retval += record.getRecordSize()
            }
            return retval
        }

    private val orCreateLinkTable: LinkTable
        /**
         * lazy initialization
         * Note - creating the link table causes creation of 1 EXTERNALBOOK and 1 EXTERNALSHEET record
         */
        get() {
            if (linkTable == null) {
                linkTable = LinkTable(this.numSheets.toShort().toInt(), records)
            }
            return linkTable!!
        }

    /** finds the sheet name by his extern sheet index
     * @param externSheetIndex extern sheet index
     * @return sheet name.
     */
    fun findSheetNameFromExternSheet(externSheetIndex: Int): String? {
        val indexToSheet = linkTable!!.getIndexToInternalSheet(externSheetIndex)
        if (indexToSheet < 0) {
            // TODO - what does '-1' mean here?
            //error check, bail out gracefully!
            return ""
        }
        if (indexToSheet >= boundsheets.size) {
            // Not sure if this can ever happen (See bug 45798)
            return "" // Seems to be what excel would do in this case
        }
        return getSheetName(indexToSheet)
    }

    fun getExternalSheet(externSheetIndex: Int): ExternalSheet? {
        val extNames = linkTable!!.getExternalBookAndSheetName(externSheetIndex)
        if (extNames == null) {
            return null
        }
        return ExternalSheet(extNames[0], extNames[1])
    }

    fun getExternalName(externSheetIndex: Int, externNameIndex: Int): ExternalName? {
        val nameName = linkTable!!.resolveNameXText(externSheetIndex, externNameIndex)
        if (nameName == null) {
            return null
        }
        val ix = linkTable!!.resolveNameXIx(externSheetIndex, externNameIndex)
        return ExternalName(nameName, externNameIndex, ix)
    }

    /**
     * Finds the sheet index for a particular external sheet number.
     * @param externSheetNumber     The external sheet number to convert
     * @return  The index to the sheet found.
     */
    fun getSheetIndexFromExternSheetIndex(externSheetNumber: Int): Int {
        return linkTable!!.getSheetIndexFromExternSheetIndex(externSheetNumber)
    }

    /** returns the extern sheet number for specific sheet number ,
     * if this sheet doesn't exist in extern sheet , add it
     * @param sheetNumber sheet number
     * @return index to extern sheet
     */
    fun checkExternSheet(sheetNumber: Int): Short {
        return this.orCreateLinkTable.checkExternSheet(sheetNumber).toShort()
    }

    fun getExternalSheetIndex(workbookName: String?, sheetName: String?): Int {
        return this.orCreateLinkTable.getExternalSheetIndex(workbookName, sheetName)
    }

    val numNames: Int
        /** gets the total number of names
         * @return number of names
         */
        get() {
            if (linkTable == null) {
                return 0
            }
            return linkTable!!.numNames
        }

    /** gets the name record
     * @param index name index
     * @return name record
     */
    fun getNameRecord(index: Int): NameRecord {
        return linkTable!!.getNameRecord(index)
    }

    /** gets the name comment record
     * @param nameRecord name record who's comment is required.
     * @return name comment record or `null` if there isn't one for the given name.
     */
    fun getNameCommentRecord(nameRecord: NameRecord): NameCommentRecord? {
        return commentRecords.get(nameRecord.getNameText())
    }

    /** creates new name
     * @return new name record
     */
    fun createName(): NameRecord {
        return addName(NameRecord())
    }

    /** creates new name
     * @return new name record
     */
    fun addName(name: NameRecord): NameRecord {
        val linkTable = this.orCreateLinkTable
        linkTable.addName(name)

        return name
    }

    /**
     * Generates a NameRecord to represent a built-in region
     * @return a new NameRecord
     */
    fun createBuiltInName(builtInName: Byte, sheetNumber: Int): NameRecord {
        require(!(sheetNumber < 0 || sheetNumber + 1 > Short.MAX_VALUE)) { "Sheet number [" + sheetNumber + "]is not valid " }

        val name = NameRecord(builtInName, sheetNumber)

        if (linkTable!!.nameAlreadyExists(name)) {
            throw RuntimeException(
                ("Builtin (" + builtInName + ") already exists for sheet ("
                        + sheetNumber + ")")
            )
        }
        addName(name)
        return name
    }

    /** removes the name
     * @param nameIndex name index
     */
    fun removeName(nameIndex: Int) {
        if (linkTable!!.numNames > nameIndex) {
            val idx = findFirstRecordLocBySid(NameRecord.sid)
            records.remove(idx + nameIndex)
            linkTable!!.removeName(nameIndex)
        }
    }

    /**
     * If a [NameCommentRecord] is added or the name it references
     * is renamed, then this will update the lookup cache for it.
     */
    fun updateNameCommentRecordCache(commentRecord: NameCommentRecord) {
        if (commentRecords.containsValue(commentRecord)) {
            for (entry in commentRecords.entries) {
                if (entry.value == commentRecord) {
                    commentRecords.remove(entry.key)
                    break
                }
            }
        }
        commentRecords.put(commentRecord.getNameText(), commentRecord)
    }

    /**
     * Returns a format index that matches the passed in format.  It does not tie into HSSFDataFormat.
     * @param format the format string
     * @param createIfNotFound creates a new format if format not found
     * @return the format id of a format that matches or -1 if none found and createIfNotFound
     */
    fun getFormat(format: String?, createIfNotFound: Boolean): Short {
        for (r in formats) {
            if (r.getFormatString() == format) {
                return r.getIndexCode().toShort()
            }
        }

        if (createIfNotFound) {
            return createFormat(format).toShort()
        }

        return -1
    }

    /**
     * Creates a FormatRecord, inserts it, and returns the index code.
     * @param formatString the format string
     * @return the index code of the format record.
     * @see FormatRecord
     * 
     * @see Record
     */
    fun createFormat(formatString: String?): Int {
        maxformatid =
            if (maxformatid >= 0xa4) maxformatid + 1 else 0xa4 //Starting value from M$ empircal study.
        val rec = FormatRecord(maxformatid, formatString!!)

        var pos = 0
        while (pos < records.size() && records.get(pos).getSid() != FormatRecord.sid) pos++
        pos += formats.size
        formats.add(rec)
        records.add(pos, rec)
        return maxformatid
    }

    /**
     * Returns the first occurance of a record matching a particular sid.
     */
    fun findFirstRecordBySid(sid: Short): Record? {
        for (record in records) {
            if (record.getSid() == sid) {
                return record
            }
        }
        return null
    }

    /**
     * Returns the index of a record matching a particular sid.
     * @param sid   The sid of the record to match
     * @return      The index of -1 if no match made.
     */
    fun findFirstRecordLocBySid(sid: Short): Int {
        var index = 0
        for (record in records) {
            if (record.getSid() == sid) {
                return index
            }
            index++
        }
        return -1
    }

    /**
     * Returns the next occurance of a record matching a particular sid.
     */
    fun findNextRecordBySid(sid: Short, pos: Int): Record? {
        var matches = 0
        for (record in records) {
            if (record.getSid() == sid) {
                if (matches++ == pos) return record
            }
        }
        return null
    }

    fun getRecords(): MutableList<Record> {
        return records.records
    }

    val customPalette: PaletteRecord
        /**
         * Returns the custom palette in use for this workbook; if a custom palette record
         * does not exist, then it is created.
         */
        get() {
            val palette: PaletteRecord
            val palettePos: Int = records.palettepos
            if (palettePos != -1) {
                val rec = records.get(palettePos)
                if (rec is PaletteRecord) {
                    palette = rec
                } else throw RuntimeException(
                    ("InternalError: Expected PaletteRecord but got a '"
                            + rec + "'")
                )
            } else {
                palette = createPalette()
                //Add the palette record after the bof which is always the first record
                records.add(1, palette)
                records.palettepos = 1
            }
            return palette
        }

    /**
     * Finds the primary drawing group, if one already exists
     */
    fun findDrawingGroup(): DrawingManager2? {
        if (drawingManager != null) {
            // We already have it!
            return drawingManager
        }

        // Need to find a DrawingGroupRecord that
        //  contains a EscherDggRecord
        for (r in records) {
            if (r is DrawingGroupRecord) {
                val dg = r
                dg.processChildRecords()

                val cr = dg.escherContainer
                if (cr == null) {
                    continue
                }

                var dgg: EscherDggRecord? = null
                var bStore: EscherContainerRecord? = null
                val it: MutableIterator<EscherRecord?> = cr.childIterator
                while (it.hasNext()) {
                    val er = it.next()!!
                    if (er is EscherDggRecord) {
                        dgg = er
                    } else if (er.recordId == EscherContainerRecord.BSTORE_CONTAINER) {
                        bStore = er as EscherContainerRecord
                    }
                }

                if (dgg != null) {
                    drawingManager = DrawingManager2(dgg)
                    if (bStore != null) {
                        for (bs in bStore.childRecords) {
                            if (bs is EscherBSERecord) escherBSERecords.add(bs)
                        }
                    }
                    return drawingManager
                }
            }
        }

        // Look for the DrawingGroup record
        val dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid)

        // If there is one, does it have a EscherDggRecord?
        if (dgLoc != -1) {
            val dg = records.get(dgLoc) as DrawingGroupRecord
            var dgg: EscherDggRecord? = null
            var bStore: EscherContainerRecord? = null
            for (er in dg.escherRecords) {
                if (er is EscherDggRecord) {
                    dgg = er
                } else if (er.recordId == EscherContainerRecord.BSTORE_CONTAINER) {
                    bStore = er as EscherContainerRecord
                }
            }

            if (dgg != null) {
                drawingManager = DrawingManager2(dgg)
                if (bStore != null) {
                    for (bs in bStore.childRecords) {
                        if (bs is EscherBSERecord) escherBSERecords.add(bs)
                    }
                }
            }
        }
        return drawingManager
    }

    /**
     * Creates a primary drawing group record.  If it already
     * exists then it's modified.
     */
    fun createDrawingGroup() {
        if (drawingManager == null) {
            val dggContainer = EscherContainerRecord()
            val dgg = EscherDggRecord()
            val opt = EscherOptRecord()
            val splitMenuColors = EscherSplitMenuColorsRecord()

            dggContainer.recordId = 0xF000.toShort()
            dggContainer.options = 0x000F.toShort()
            dgg.recordId = EscherDggRecord.RECORD_ID
            dgg.options = 0x0000.toShort()
            dgg.shapeIdMax = 1024
            dgg.numShapesSaved = 0
            dgg.drawingsSaved = 0
            dgg.fileIdClusters = arrayOf<FileIdCluster?>()
            drawingManager = DrawingManager2(dgg)
            var bstoreContainer: EscherContainerRecord? = null
            if (escherBSERecords.size > 0) {
                bstoreContainer = EscherContainerRecord()
                bstoreContainer.recordId = EscherContainerRecord.BSTORE_CONTAINER
                bstoreContainer.options = ((escherBSERecords.size shl 4) or 0xF).toShort()
                for (escherRecord in escherBSERecords) {
                    bstoreContainer.addChildRecord(escherRecord)
                }
            }
            opt.recordId = 0xF00B.toShort()
            opt.options = 0x0033.toShort()
            opt.addEscherProperty(
                EscherBoolProperty(
                    EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 524296
                )
            )
            opt.addEscherProperty(
                EscherRGBProperty(
                    EscherProperties.FILL__FILLCOLOR,
                    0x08000041
                )
            )
            opt.addEscherProperty(
                EscherRGBProperty(
                    EscherProperties.LINESTYLE__COLOR,
                    134217792
                )
            )
            splitMenuColors.recordId = 0xF11E.toShort()
            splitMenuColors.options = 0x0040.toShort()
            splitMenuColors.color1 = 0x0800000D
            splitMenuColors.color2 = 0x0800000C
            splitMenuColors.color3 = 0x08000017
            splitMenuColors.color4 = 0x100000F7

            dggContainer.addChildRecord(dgg)
            if (bstoreContainer != null) dggContainer.addChildRecord(bstoreContainer)
            dggContainer.addChildRecord(opt)
            dggContainer.addChildRecord(splitMenuColors)

            val dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid)
            if (dgLoc == -1) {
                val drawingGroup = DrawingGroupRecord()
                drawingGroup.addEscherRecord(dggContainer)
                val loc = findFirstRecordLocBySid(CountryRecord.sid)

                getRecords()!!.add(loc + 1, drawingGroup)
            } else {
                val drawingGroup = DrawingGroupRecord()
                drawingGroup.addEscherRecord(dggContainer)
                getRecords()!!.set(dgLoc, drawingGroup)
            }
        }
    }

    fun getBSERecord(pictureIndex: Int): EscherBSERecord? {
        val index = pictureIndex - 1
        if (index >= 0 && index < escherBSERecords.size) {
            return escherBSERecords.get(index)
        }
        return null
    }

    fun addBSERecord(e: EscherBSERecord?): Int {
        createDrawingGroup()

        // maybe we don't need that as an instance variable anymore
        escherBSERecords.add(e)

        val dgLoc = findFirstRecordLocBySid(DrawingGroupRecord.sid)
        val drawingGroup = getRecords()!!.get(dgLoc) as DrawingGroupRecord

        val dggContainer = drawingGroup.getEscherRecord(0) as EscherContainerRecord
        val bstoreContainer: EscherContainerRecord?
        if (dggContainer.getChild(1)!!.recordId == EscherContainerRecord.BSTORE_CONTAINER) {
            bstoreContainer = dggContainer.getChild(1) as EscherContainerRecord?
        } else {
            bstoreContainer = EscherContainerRecord()
            bstoreContainer.recordId = EscherContainerRecord.BSTORE_CONTAINER
            val childRecords = dggContainer.childRecords
            childRecords.add(1, bstoreContainer)
            dggContainer.childRecords = childRecords
        }
        bstoreContainer!!.options = ((escherBSERecords.size shl 4) or 0xF).toShort()

        bstoreContainer.addChildRecord(e)

        return escherBSERecords.size
    }

    fun getWriteProtect(): WriteProtectRecord? {
        if (writeProtect == null) {
            writeProtect = WriteProtectRecord()
            var i = 0
            i = 0
            while (i < records.size() && records.get(i) !is BOFRecord) {
                i++
            }
            records.add(i + 1, writeProtect!!)
        }
        return this.writeProtect
    }

    fun getWriteAccess(): WriteAccessRecord {
        if (writeAccess == null) {
            writeAccess = createWriteAccess()
            var i = 0
            i = 0
            while (i < records.size() && records.get(i) !is InterfaceEndRecord) {
                i++
            }
            records.add(i + 1, writeAccess!!)
        }
        return writeAccess!!
    }

    val fileSharing: FileSharingRecord
        get() {
            if (fileShare == null) {
                fileShare = FileSharingRecord()
                var i = 0
                i = 0
                while (i < records.size() && records.get(i) !is WriteAccessRecord) {
                    i++
                }
                records.add(i + 1, fileShare!!)
            }
            return fileShare!!
        }

    val isWriteProtected: Boolean
        /**
         * is the workbook protected with a password (not encrypted)?
         */
        get() {
            if (fileShare == null) {
                return false
            }
            val frec = this.fileSharing
            return frec.getReadOnly().toInt() == 1
        }

    /**
     * protect a workbook with a password (not encypted, just sets writeprotect
     * flags and the password.
     * @param password to set
     */
    fun writeProtectWorkbook(password: String, username: String?) {
        val protIdx = -1
        val frec = this.fileSharing
        val waccess = getWriteAccess()
        val wprotect = getWriteProtect()
        frec.setReadOnly(1.toShort())
        frec.setPassword(FileSharingRecord.hashPassword(password))
        frec.setUsername(username!!)
        waccess.setUsername(username)
    }

    /**
     * removes the write protect flag
     */
    fun unwriteProtectWorkbook() {
        records.remove(fileShare)
        records.remove(writeProtect)
        fileShare = null
        writeProtect = null
    }

    /**
     * @param refIndex Index to REF entry in EXTERNSHEET record in the Link Table
     * @param definedNameIndex zero-based to DEFINEDNAME or EXTERNALNAME record
     * @return the string representation of the defined or external name
     */
    fun resolveNameXText(refIndex: Int, definedNameIndex: Int): String? {
        return linkTable!!.resolveNameXText(refIndex, definedNameIndex)
    }

    /**
     * 
     * @param name the  name of an external function, typically a name of a UDF
     * @param udf  locator of user-defiend functions to resolve names of VBA and Add-In functions
     * @return the external name or null
     */
    fun getNameXPtg(name: String?, udf: UDFFinder): NameXPtg? {
        val lnk = this.orCreateLinkTable
        var xptg = lnk.getNameXPtg(name)

        if (xptg == null && udf.findFunction(name) != null) {
            // the name was not found in the list of external names
            // check if the Workbook's UDFFinder is aware about it and register the name if it is
            xptg = lnk.addNameXPtg(name)
        }
        return xptg
    }

    /**
     * Check if the cloned sheet has drawings. If yes, then allocate a new drawing group ID and
     * re-generate shape IDs
     * 
     * @param sheet the cloned sheet
     */
    fun cloneDrawings(sheet: InternalSheet) {
        findDrawingGroup()

        if (drawingManager == null) {
            //this workbook does not have drawings
            return
        }

        //check if the cloned sheet has drawings
        val aggLoc = sheet.aggregateDrawingRecords(drawingManager!!, false)
        if (aggLoc != -1) {
            val agg = sheet.findFirstRecordBySid(EscherAggregate.sid) as EscherAggregate?
            val escherContainer = agg!!.escherContainer
            if (escherContainer == null) {
                return
            }

            val dgg = drawingManager!!.dgg

            //register a new drawing group for the cloned sheet
            val dgId = drawingManager!!.findNewDrawingGroupId().toInt()
            dgg.addCluster(dgId, 0)
            dgg.drawingsSaved = dgg.drawingsSaved + 1

            var dg: EscherDgRecord? = null
            val it: MutableIterator<EscherRecord?> = escherContainer.childIterator
            while (it.hasNext()) {
                val er: EscherRecord? = it.next()
                if (er is EscherDgRecord) {
                    dg = er
                    //update id of the drawing in the cloned sheet
                    dg.options = (dgId shl 4).toShort()
                } else if (er is EscherContainerRecord) {
                    // iterate over shapes and re-generate shapeId
                    val cp = er
                    val spIt = cp.childRecords.iterator()
                    while (spIt
                            .hasNext()
                    ) {
                        val shapeContainer = spIt.next() as EscherContainerRecord
                        for (shapeChildRecord in shapeContainer.childRecords) {
                            val recordId = shapeChildRecord!!.recordId.toInt()
                            if (recordId == EscherSpRecord.RECORD_ID.toInt()) {
                                val sp = shapeChildRecord as EscherSpRecord
                                val shapeId = drawingManager!!.allocateShapeId(dgId.toShort(), dg!!)
                                //allocateShapeId increments the number of shapes. roll back to the previous value
                                dg.numShapes = dg.numShapes - 1
                                sp.shapeId = shapeId
                            } else if (recordId == EscherOptRecord.RECORD_ID.toInt()) {
                                val opt = shapeChildRecord as EscherOptRecord
                                val prop = opt
                                    .lookup<EscherProperty?>(EscherProperties.BLIP__BLIPTODISPLAY.toInt()) as EscherSimpleProperty?
                                if (prop != null) {
                                    val pictureIndex = prop.propertyValue
                                    // increment reference count for pictures
                                    val bse = getBSERecord(pictureIndex)
                                    bse!!.ref = bse.ref + 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun cloneFilter(filterDbNameIndex: Int, newSheetIndex: Int): NameRecord {
        val origNameRecord = getNameRecord(filterDbNameIndex)
        // copy original formula but adjust 3D refs to the new external sheet index
        val newExtSheetIx = checkExternSheet(newSheetIndex).toInt()
        val ptgs: Array<Ptg?> = arrayOf(*origNameRecord.getNameDefinition())
        for (i in ptgs.indices) {
            val ptg = ptgs[i]

            if (ptg is Area3DPtg) {
                val a3p = (ptg as OperandPtg).copy() as Area3DPtg
                a3p.setExternSheetIndex(newExtSheetIx)
                ptgs[i] = a3p
            } else if (ptg is Ref3DPtg) {
                val r3p = (ptg as OperandPtg).copy() as Ref3DPtg
                r3p.setExternSheetIndex(newExtSheetIx)
                ptgs[i] = r3p
            }
        }
        val newNameRecord = createBuiltInName(
            NameRecord.BUILTIN_FILTER_DB,
            newSheetIndex + 1
        )
        newNameRecord.setNameDefinition(ptgs)
        newNameRecord.setHidden(true)
        return newNameRecord
    }

    /**
     * Updates named ranges due to moving of cells
     */
    fun updateNamesAfterCellShift(shifter: FormulaShifter) {
        for (i in 0..<this.numNames) {
            val nr = getNameRecord(i)
            val ptgs: Array<Ptg?> = arrayOf(*nr.getNameDefinition())
            if (shifter.adjustFormula(ptgs, nr.getSheetNumber())) {
                nr.setNameDefinition(ptgs)
            }
        }
    }

    val recalcId: RecalcIdRecord
        /**
         * Get or create RecalcIdRecord
         * 
         * @see com.wxiwei.office.fc.hssf.usermodel.HSSFWorkbook.setForceFormulaRecalculation
         */
        get() {
            var record =
                findFirstRecordBySid(RecalcIdRecord.sid) as RecalcIdRecord?
            if (record == null) {
                record = RecalcIdRecord()
                // typically goes after the Country record
                val pos = findFirstRecordLocBySid(CountryRecord.sid)
                records.add(pos + 1, record)
            }
            return record
        }

    companion object {
        /**
         * Excel silently truncates long sheet names to 31 chars.
         * This constant is used to ensure uniqueness in the first 31 chars
         */
        private const val MAX_SENSITIVE_SHEET_NAME_LEN = 31

        private val log = getLogger(InternalWorkbook::class.java)
        private val DEBUG = POILogger.DEBUG

        /**
         * constant used to set the "codepage" wherever "codepage" is set in records
         * (which is duplicated in more than one record)
         */
        private const val CODEPAGE: Short = 0x04B0

        /**
         * read support  for low level
         * API.  Pass in an array of Record objects, A Workbook
         * object is constructed and passed back with all of its initialization set
         * to the passed in records and references to those records held. Unlike Sheet
         * workbook does not use an offset (its assumed to be 0) since its first in a file.
         * If you need an offset then construct a new array with a 0 offset or write your
         * own ;-p.
         * 
         * @param recs an array of Record objects
         * @return Workbook object
         */
        /**
         * 
         * @param recs
         * @return
         */
        @JvmOverloads
        fun createWorkbook(
            recs: MutableList<Record>,
            iAbortListener: AbstractReader? = null
        ): InternalWorkbook {
            val retval = InternalWorkbook()
            val records: MutableList<Record> = ArrayList<Record>(recs.size / 3)
            retval.records.records = records

            var k: Int
            k = 0
            while (k < recs.size) {
                if (iAbortListener != null && iAbortListener.isAborted()) {
                    throw AbortReaderError("abort Reader")
                }

                val rec = recs.get(k)

                if (rec.getSid() == EOFRecord.sid) {
                    records.add(rec)
                    break
                }
                when (rec.getSid()) {
                    BoundSheetRecord.sid -> {
                        retval.boundsheets.add(rec as BoundSheetRecord)
                        retval.records.bspos = k
                    }

                    SSTRecord.sid -> retval.sst = rec as SSTRecord
                    FontRecord.sid -> {
                        retval.records.fontpos = k
                        retval.numberOfFontRecords++
                    }

                    ExtendedFormatRecord.sid -> {
                        retval.records.xfpos = k
                        retval.numxfs++
                    }

                    TabIdRecord.sid -> retval.records.tabpos = k
                    ProtectRecord.sid -> retval.records.protpos = k
                    BackupRecord.sid -> retval.records.backuppos = k
                    ExternSheetRecord.sid -> throw RuntimeException("Extern sheet is part of LinkTable")
                    NameRecord.sid, SupBookRecord.sid -> {
                        // LinkTable can start with either of these
                        retval.linkTable = LinkTable(recs, k, retval.records, retval.commentRecords)
                        k += retval.linkTable!!.recordCount - 1
                        k++
                        continue
                    }

                    FormatRecord.sid -> {
                        retval.formats.add(rec as FormatRecord)
                        retval.maxformatid = if (retval.maxformatid >= rec.getIndexCode())
                            retval.maxformatid
                        else
                            rec.getIndexCode()
                    }

                    DateWindow1904Record.sid -> retval.isUsing1904DateWindowing =
                        (rec as DateWindow1904Record).getWindowing().toInt() == 1

                    PaletteRecord.sid -> retval.records.palettepos = k
                    WindowOneRecord.sid -> retval.windowOne = rec as WindowOneRecord
                    WriteAccessRecord.sid -> retval.writeAccess = rec as WriteAccessRecord
                    WriteProtectRecord.sid -> retval.writeProtect = rec as WriteProtectRecord
                    FileSharingRecord.sid -> retval.fileShare = rec as FileSharingRecord
                    NameCommentRecord.sid -> {
                        val ncr = rec as NameCommentRecord
                        retval.commentRecords.put(ncr.getNameText(), ncr)
                    }

                    else -> {}
                }
                records.add(rec)
                k++
            }

            //What if we dont have any ranges and supbooks
            //        if (retval.records.supbookpos == 0) {
            //            retval.records.supbookpos = retval.records.bspos + 1;
            //            retval.records.namepos    = retval.records.supbookpos + 1;
            //        }

            // Look for other interesting values that
            //  follow the EOFRecord
            while (k < recs.size) {
                if (iAbortListener != null && iAbortListener.isAborted()) {
                    throw AbortReaderError("abort Reader")
                }
                val rec = recs.get(k)
                when (rec.getSid()) {
                    HyperlinkRecord.sid -> retval.hyperlinks.add(rec as HyperlinkRecord)
                }
                k++
            }

            if (retval.windowOne == null) {
                retval.windowOne = createWindowOne()
            }
            return retval
        }

        /**
         * Creates an empty workbook object with three blank sheets and all the empty
         * fields.  Use this to create a workbook from scratch.
         */
        @JvmStatic
        fun createWorkbook(): InternalWorkbook {
            if (log.check(POILogger.DEBUG)) log.log(DEBUG, "creating new workbook from scratch")
            val retval = InternalWorkbook()
            val records: MutableList<Record> = ArrayList<Record>(30)
            retval.records.records = records
            val formats = retval.formats

            records.add(createBOF())
            records.add(InterfaceHdrRecord(CODEPAGE.toInt()))
            records.add(createMMS())
            records.add(InterfaceEndRecord.instance)
            records.add(createWriteAccess())
            records.add(createCodepage())
            records.add(createDSF())
            records.add(createTabId())
            retval.records.tabpos = records.size - 1
            records.add(createFnGroupCount())
            records.add(createWindowProtect())
            records.add(createProtect())
            retval.records.protpos = records.size - 1
            records.add(createPassword())
            records.add(createProtectionRev4())
            records.add(createPasswordRev4())
            retval.windowOne = createWindowOne()
            records.add(retval.windowOne!!)
            records.add(createBackup())
            retval.records.backuppos = records.size - 1
            records.add(createHideObj())
            records.add(createDateWindow1904())
            records.add(createPrecision())
            records.add(createRefreshAll())
            records.add(createBookBool())
            records.add(createFont())
            records.add(createFont())
            records.add(createFont())
            records.add(createFont())
            retval.records.fontpos = records.size - 1 // last font record position
            retval.numberOfFontRecords = 4

            // set up format records
            for (i in 0..7) {
                val rec: FormatRecord = createFormat(i)
                retval.maxformatid = if (retval.maxformatid >= rec.getIndexCode())
                    retval.maxformatid
                else
                    rec.getIndexCode()
                formats.add(rec)
                records.add(rec)
            }

            for (k in 0..20) {
                records.add(createExtendedFormat(k))
                retval.numxfs++
            }
            retval.records.xfpos = records.size - 1
            for (k in 0..5) {
                records.add(createStyle(k))
            }
            records.add(createUseSelFS())

            val nBoundSheets = 1 // now just do 1
            for (k in 0..<nBoundSheets) {
                val bsr: BoundSheetRecord = createBoundSheet(k)

                records.add(bsr)
                retval.boundsheets.add(bsr)
                retval.records.bspos = records.size - 1
            }
            records.add(createCountry())
            for (k in 0..<nBoundSheets) {
                retval.orCreateLinkTable.checkExternSheet(k)
            }
            retval.sst = SSTRecord()
            records.add(retval.sst!!)
            records.add(createExtendedSST())

            records.add(EOFRecord.instance)
            if (log.check(POILogger.DEBUG)) log.log(DEBUG, "exit create new workbook from scratch")
            return retval
        }

        private fun createBOF(): BOFRecord {
            val retval = BOFRecord()

            retval.version = 0x600.toShort().toInt()
            retval.type = BOFRecord.TYPE_WORKBOOK
            retval.build = 0x10d3.toShort().toInt()
            retval.buildYear = 1996.toShort().toInt()
            retval.historyBitMask = 0x41 // was c1 before verify
            retval.requiredVersion = 0x6
            return retval
        }

        private fun createMMS(): MMSRecord {
            val retval = MMSRecord()

            retval.setAddMenuCount(0.toByte())
            retval.setDelMenuCount(0.toByte())
            return retval
        }

        /**
         * creates the WriteAccess record containing the logged in user's name
         */
        private fun createWriteAccess(): WriteAccessRecord {
            val retval = WriteAccessRecord()

            try {
                retval.setUsername(System.getProperty("user.name"))
            } catch (e: AccessControlException) {
                // AccessControlException can occur in a restricted context
                // (client applet/jws application or restricted security server)
                retval.setUsername("POI")
            }
            return retval
        }

        private fun createCodepage(): CodepageRecord {
            val retval = CodepageRecord()

            retval.codepage = CODEPAGE
            return retval
        }

        private fun createDSF(): DSFRecord {
            return DSFRecord(false) // we don't even support double stream files
        }

        /**
         * creates the TabId record containing an array
         */
        private fun createTabId(): TabIdRecord {
            return TabIdRecord()
        }

        /**
         * creates the FnGroupCount record containing the Magic number constant of 14.
         */
        private fun createFnGroupCount(): FnGroupCountRecord {
            val retval = FnGroupCountRecord()

            retval.setCount(14.toShort())
            return retval
        }

        /**
         * @return a new WindowProtect record with protect set to false.
         */
        private fun createWindowProtect(): WindowProtectRecord {
            // by default even when we support it we won't
            // want it to be protected
            return WindowProtectRecord(false)
        }

        /**
         * @return a new Protect record with protect set to false.
         */
        private fun createProtect(): ProtectRecord {
            // by default even when we support it we won't
            // want it to be protected
            return ProtectRecord(false)
        }

        /**
         * @return a new Password record with password set to 0x0000 (no password).
         */
        private fun createPassword(): PasswordRecord {
            return PasswordRecord(0x0000) // no password by default!
        }

        /**
         * @return a new ProtectionRev4 record with protect set to false.
         */
        private fun createProtectionRev4(): ProtectionRev4Record {
            return ProtectionRev4Record(false)
        }

        /**
         * @return a new PasswordRev4 record with password set to 0.
         */
        private fun createPasswordRev4(): PasswordRev4Record {
            return PasswordRev4Record(0x0000)
        }

        /**
         * creates the WindowOne record with the following magic values: <P>
         * horizontal hold - 0x168 </P><P>
         * vertical hold   - 0x10e </P><P>
         * width           - 0x3a5c </P><P>
         * height          - 0x23be </P><P>
         * options         - 0x38 </P><P>
         * selected tab    - 0 </P><P>
         * displayed tab   - 0 </P><P>
         * num selected tab- 0 </P><P>
         * tab width ratio - 0x258 </P><P>
        </P> */
        private fun createWindowOne(): WindowOneRecord {
            val retval = WindowOneRecord()

            retval.setHorizontalHold(0x168.toShort())
            retval.setVerticalHold(0x10e.toShort())
            retval.setWidth(0x3a5c.toShort())
            retval.setHeight(0x23be.toShort())
            retval.setOptions(0x38.toShort())
            retval.setActiveSheetIndex(0x0)
            retval.setFirstVisibleTab(0x0)
            retval.setNumSelectedTabs(1.toShort())
            retval.setTabWidthRatio(0x258.toShort())
            return retval
        }

        /**
         * creates the Backup record with backup set to 0. (loose the data, who cares)
         */
        private fun createBackup(): BackupRecord {
            val retval = BackupRecord()

            retval.backup = 0.toShort() // by default DONT save backups of files...just loose data
            return retval
        }

        /**
         * creates the HideObj record with hide object set to 0. (don't hide)
         */
        private fun createHideObj(): HideObjRecord {
            val retval = HideObjRecord()
            retval.setHideObj(0.toShort()) // by default set hide object off
            return retval
        }

        /**
         * creates the DateWindow1904 record with windowing set to 0. (don't window)
         */
        private fun createDateWindow1904(): DateWindow1904Record {
            val retval = DateWindow1904Record()

            retval.setWindowing(0.toShort()) // don't EVER use 1904 date windowing...tick tock..
            return retval
        }

        /**
         * creates the Precision record with precision set to true. (full precision)
         */
        private fun createPrecision(): PrecisionRecord {
            val retval = PrecisionRecord()
            retval.setFullPrecision(true) // always use real numbers in calculations!
            return retval
        }

        /**
         * @return a new RefreshAll record with refreshAll set to false. (do not refresh all calcs)
         */
        private fun createRefreshAll(): RefreshAllRecord {
            return RefreshAllRecord(false)
        }

        /**
         * creates the BookBool record with saveLinkValues set to 0. (don't save link values)
         */
        private fun createBookBool(): BookBoolRecord {
            val retval = BookBoolRecord()
            retval.saveLinkValues = 0.toShort()
            return retval
        }

        /**
         * creates a Font record with the following magic values: <P>
         * fontheight           = 0xc8</P><P>
         * attributes           = 0x0</P><P>
         * color palette index  = 0x7fff</P><P>
         * bold weight          = 0x190</P><P>
         * Font Name Length     = 5 </P><P>
         * Font Name            = Arial </P><P>
        </P> */
        private fun createFont(): FontRecord {
            val retval = FontRecord()

            retval.setFontHeight(0xc8.toShort())
            retval.setAttributes(0x0.toShort())
            retval.setColorPaletteIndex(0x7fff.toShort())
            retval.setBoldWeight(0x190.toShort())
            retval.setFontName("Arial")
            return retval
        }

        /**
         * Creates a FormatRecord object
         * @param id    the number of the format record to create (meaning its position in
         * a file as M$ Excel would create it.)
         */
        private fun createFormat(id: Int): FormatRecord {
            // we'll need multiple editions for
            // the different formats

            when (id) {
                0 -> return FormatRecord(5, BuiltinFormats.getBuiltinFormat(5))
                1 -> return FormatRecord(6, BuiltinFormats.getBuiltinFormat(6))
                2 -> return FormatRecord(7, BuiltinFormats.getBuiltinFormat(7))
                3 -> return FormatRecord(8, BuiltinFormats.getBuiltinFormat(8))
                4 -> return FormatRecord(0x2a, BuiltinFormats.getBuiltinFormat(0x2a))
                5 -> return FormatRecord(0x29, BuiltinFormats.getBuiltinFormat(0x29))
                6 -> return FormatRecord(0x2c, BuiltinFormats.getBuiltinFormat(0x2c))
                7 -> return FormatRecord(0x2b, BuiltinFormats.getBuiltinFormat(0x2b))
            }
            throw IllegalArgumentException("Unexpected id " + id)
        }

        /**
         * Creates an ExtendedFormatRecord object
         * @param id    the number of the extended format record to create (meaning its position in
         * a file as MS Excel would create it.)
         */
        private fun createExtendedFormat(id: Int): ExtendedFormatRecord { // we'll need multiple editions
            val retval = ExtendedFormatRecord()

            when (id) {
                0 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                1 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                2 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                3 -> {
                    retval.setFontIndex(2.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                4 -> {
                    retval.setFontIndex(2.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                5 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                6 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                7 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                8 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                9 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                10 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                11 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                12 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                13 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                14 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0xc00).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                15 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x0.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                16 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0x2b.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0x800).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                17 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0x29.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0x800).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                18 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0x2c.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0x800).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                19 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0x2a.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0x800).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                20 -> {
                    retval.setFontIndex(1.toShort())
                    retval.setFormatIndex(0x9.toShort())
                    retval.setCellOptions((-0xb).toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions((-0x800).toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                21 -> {
                    retval.setFontIndex(5.toShort())
                    retval.setFormatIndex(0x0.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x800.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                22 -> {
                    retval.setFontIndex(6.toShort())
                    retval.setFormatIndex(0x0.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x5c00.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                23 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0x31.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x5c00.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                24 -> {
                    retval.setFontIndex(0.toShort())
                    retval.setFormatIndex(0x8.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x5c00.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }

                25 -> {
                    retval.setFontIndex(6.toShort())
                    retval.setFormatIndex(0x8.toShort())
                    retval.setCellOptions(0x1.toShort())
                    retval.setAlignmentOptions(0x20.toShort())
                    retval.setIndentionOptions(0x5c00.toShort())
                    retval.setBorderOptions(0.toShort())
                    retval.setPaletteOptions(0.toShort())
                    retval.setAdtlPaletteOptions(0.toShort())
                    retval.setFillPaletteOptions(0x20c0.toShort())
                }
            }
            return retval
        }

        /**
         * creates an default cell type ExtendedFormatRecord object.
         * @return ExtendedFormatRecord with intial defaults (cell-type)
         */
        private fun createExtendedFormat(): ExtendedFormatRecord {
            val retval = ExtendedFormatRecord()

            retval.setFontIndex(0.toShort())
            retval.setFormatIndex(0x0.toShort())
            retval.setCellOptions(0x1.toShort())
            retval.setAlignmentOptions(0x20.toShort())
            retval.setIndentionOptions(0.toShort())
            retval.setBorderOptions(0.toShort())
            retval.setPaletteOptions(0.toShort())
            retval.setAdtlPaletteOptions(0.toShort())
            retval.setFillPaletteOptions(0x20c0.toShort())
            retval.setTopBorderPaletteIdx(HSSFColor.BLACK.index)
            retval.setBottomBorderPaletteIdx(HSSFColor.BLACK.index)
            retval.setLeftBorderPaletteIdx(HSSFColor.BLACK.index)
            retval.setRightBorderPaletteIdx(HSSFColor.BLACK.index)
            return retval
        }

        /**
         * Creates a StyleRecord object
         * @param id        the number of the style record to create (meaning its position in
         * a file as MS Excel would create it.
         */
        private fun createStyle(id: Int): StyleRecord { // we'll need multiple editions
            val retval = StyleRecord()

            when (id) {
                0 -> {
                    retval.setXFIndex(0x010)
                    retval.setBuiltinStyle(3)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }

                1 -> {
                    retval.setXFIndex(0x011)
                    retval.setBuiltinStyle(6)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }

                2 -> {
                    retval.setXFIndex(0x012)
                    retval.setBuiltinStyle(4)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }

                3 -> {
                    retval.setXFIndex(0x013)
                    retval.setBuiltinStyle(7)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }

                4 -> {
                    retval.setXFIndex(0x000)
                    retval.setBuiltinStyle(0)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }

                5 -> {
                    retval.setXFIndex(0x014)
                    retval.setBuiltinStyle(5)
                    retval.setOutlineStyleLevel((-0x1).toByte().toInt())
                }
            }
            return retval
        }

        /**
         * Creates a palette record initialized to the default palette
         */
        private fun createPalette(): PaletteRecord {
            return PaletteRecord()
        }

        /**
         * @return a new UseSelFS object with the use natural language flag set to 0 (false)
         */
        private fun createUseSelFS(): UseSelFSRecord {
            return UseSelFSRecord(false)
        }

        /**
         * create a "bound sheet" or "bundlesheet" (depending who you ask) record
         * Always sets the sheet's bof to 0.  You'll need to set that yourself.
         * @param id either sheet 0,1 or 2.
         * @return record containing a BoundSheetRecord
         * @see BoundSheetRecord
         * 
         * @see Record
         */
        private fun createBoundSheet(id: Int): BoundSheetRecord {
            return BoundSheetRecord("Sheet" + (id + 1))
        }

        /**
         * Creates the Country record with the default country set to 1
         * and current country set to 7 in case of russian locale ("ru_RU") and 1 otherwise
         */
        private fun createCountry(): CountryRecord {
            val retval = CountryRecord()

            retval.defaultCountry = 1.toShort()

            // from Russia with love ;)
            if (Locale.getDefault().toString() == "ru_RU") {
                retval.currentCountry = 7.toShort()
            } else {
                retval.currentCountry = 1.toShort()
            }

            return retval
        }

        /**
         * Creates the ExtendedSST record with numstrings per bucket set to 0x8.  HSSF
         * doesn't yet know what to do with this thing, but we create it with nothing in
         * it hardly just to make Excel happy and our sheets look like Excel's
         */
        private fun createExtendedSST(): ExtSSTRecord {
            val retval = ExtSSTRecord()
            retval.setNumStringsPerBucket(0x8.toShort())
            return retval
        }
    }
}
