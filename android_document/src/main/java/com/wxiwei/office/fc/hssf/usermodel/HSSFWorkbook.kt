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

import com.wxiwei.office.fc.POIDocument
import com.wxiwei.office.fc.codec.DigestUtils.md5
import com.wxiwei.office.fc.ddf.EscherBSERecord
import com.wxiwei.office.fc.ddf.EscherBitmapBlip
import com.wxiwei.office.fc.ddf.EscherBlipRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hssf.OldExcelFormatException
import com.wxiwei.office.fc.hssf.formula.FormulaShifter.Companion.createForSheetShift
import com.wxiwei.office.fc.hssf.formula.SheetNameFormatter.appendFormat
import com.wxiwei.office.fc.hssf.formula.ptg.Area3DPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MemFuncPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.UnionPtg
import com.wxiwei.office.fc.hssf.formula.udf.AggregatingUDFFinder
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder
import com.wxiwei.office.fc.hssf.model.InternalSheet.Companion.createSheet
import com.wxiwei.office.fc.hssf.model.InternalWorkbook
import com.wxiwei.office.fc.hssf.model.InternalWorkbook.Companion.createWorkbook
import com.wxiwei.office.fc.hssf.model.RecordStream
import com.wxiwei.office.fc.hssf.record.AbstractEscherHolderRecord
import com.wxiwei.office.fc.hssf.record.DrawingGroupRecord
import com.wxiwei.office.fc.hssf.record.EmbeddedObjectRefSubRecord
import com.wxiwei.office.fc.hssf.record.FormatRecord
import com.wxiwei.office.fc.hssf.record.LabelRecord
import com.wxiwei.office.fc.hssf.record.LabelSSTRecord
import com.wxiwei.office.fc.hssf.record.NameRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.RecalcIdRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.RecordBase
import com.wxiwei.office.fc.hssf.record.RecordFactory.createRecords
import com.wxiwei.office.fc.hssf.record.SSTRecord
import com.wxiwei.office.fc.hssf.record.SubRecord
import com.wxiwei.office.fc.hssf.record.UnknownRecord
import com.wxiwei.office.fc.hssf.record.WindowOneRecord
import com.wxiwei.office.fc.hssf.record.aggregates.RecordAggregate.RecordVisitor
import com.wxiwei.office.fc.hssf.record.common.UnicodeString
import com.wxiwei.office.fc.hssf.util.CellReference
import com.wxiwei.office.fc.poifs.filesystem.DirectoryNode
import com.wxiwei.office.fc.poifs.filesystem.POIFSFileSystem
import com.wxiwei.office.fc.ss.usermodel.IRow
import com.wxiwei.office.fc.ss.usermodel.IRow.MissingCellPolicy
import com.wxiwei.office.fc.ss.usermodel.Sheet
import com.wxiwei.office.fc.ss.usermodel.Workbook
import com.wxiwei.office.fc.ss.util.WorkbookUtil
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.util.Hashtable
import java.util.regex.Pattern

/**
 * High level representation of a workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 * 
 * @see InternalWorkbook
 * 
 * @see HSSFSheet
 * 
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Shawn Laubach (slaubach at apache dot org)
 */
class HSSFWorkbook : POIDocument, Workbook {
    /**
     * this is the reference to the low level Workbook object
     */
    @get:JvmName("getWorkbookProperty")
    var workbook: InternalWorkbook
        private set
    fun getWorkbook(): InternalWorkbook = workbook

    /**
     * this holds the HSSFSheet objects attached to this workbook
     */
    protected var _sheets: MutableList<HSSFSheet>

    /**
     * this holds the HSSFName objects attached to this workbook
     */
    private val names: ArrayList<HSSFName?>

    /**
     * this holds the HSSFFont objects attached to this workbook.
     * We only create these from the low level records as required.
     */
    private var fonts: Hashtable<Short?, HSSFFont?>? = null

    /**
     * holds whether or not to preserve other nodes in the POIFS.  Used
     * for macros and embedded objects.
     */
    private var preserveNodes = false

    /**
     * Used to keep track of the data formatter so that all
     * createDataFormatter calls return the same one for a given
     * book.  This ensures that updates from one places is visible
     * someplace else.
     */
    private var formatter: HSSFDataFormat? = null

    /**
     * Retrieves the current policy on what to do when
     * getting missing or blank cells from a row.
     * The default is to return blank and null cells.
     * [MissingCellPolicy]
     */
    /**
     * Sets the policy on what to do when
     * getting missing or blank cells from a row.
     * This will then apply to all calls to
     * [HSSFRow.getCell]}. See
     * [MissingCellPolicy].
     * Note that this has no effect on any
     * iterators, only on when fetching Cells
     * by their column index.
     */
    /**
     * The policy to apply in the event of missing or
     * blank cells when fetching from a row.
     * See [MissingCellPolicy]
     */
    var missingCellPolicy: MissingCellPolicy? = IRow.RETURN_NULL_AND_BLANK

    //private static POILogger log = POILogFactory.getLogger(HSSFWorkbook.class);
    /**
     * 
     * Returns the locator of user-defined functions.
     * The default instance extends the built-in functions with the Analysis Tool Pack
     * 
     * @return the locator of user-defined functions
     */
    /*package*/
    /**
     * The locator of user-defined functions.
     * By default includes functions from the Excel Analysis Toolpack
     */
    val uDFFinder: UDFFinder = UDFFinder.DEFAULT

    // SS中配色方案
    private var palette: HSSFPalette? = null

    /**
     * Creates new HSSFWorkbook from scratch (start here!)
     * 
     */
    constructor() : this(createWorkbook())

    private constructor(book: InternalWorkbook) : super(null as DirectoryNode?) {
        workbook = book
        _sheets = ArrayList<HSSFSheet>(INITIAL_CAPACITY)
        names = ArrayList<HSSFName?>(INITIAL_CAPACITY)
    }

    /**
     * given a POI POIFSFileSystem object, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     * 
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     * macros.  This takes more memory, so only say yes if you
     * need to. If set, will store all of the POIFSFileSystem
     * in memory
     * @see POIFSFileSystem
     * 
     * @exception IOException if the stream cannot be read
     */
    @JvmOverloads
    constructor(fs: POIFSFileSystem, preserveNodes: Boolean = false) : this(
        fs.getRoot(),
        fs,
        preserveNodes
    )

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     * within it, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     * 
     * @param directory the POI filesystem directory to process from
     * @param fs the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     * macros.  This takes more memory, so only say yes if you
     * need to. If set, will store all of the POIFSFileSystem
     * in memory
     * @see POIFSFileSystem
     * 
     * @exception IOException if the stream cannot be read
     */
    constructor(directory: DirectoryNode, fs: POIFSFileSystem?, preserveNodes: Boolean) : this(
        directory,
        preserveNodes
    )

    /**
     * given a POI POIFSFileSystem object, and a specific directory
     * within it, read in its Workbook and populate the high and
     * low level models.  If you're reading in a workbook...start here.
     * 
     * @param directory the POI filesystem directory to process from
     * @param preserveNodes whether to preseve other nodes, such as
     * macros.  This takes more memory, so only say yes if you
     * need to. If set, will store all of the POIFSFileSystem
     * in memory
     * @see POIFSFileSystem
     * 
     * @exception IOException if the stream cannot be read
     */
    constructor(directory: DirectoryNode, preserveNodes: Boolean) : super(directory) {
        val workbookName: String? = getWorkbookDirEntryName(directory)

        this.preserveNodes = preserveNodes

        // If we're not preserving nodes, don't track the
        //  POIFS any more
        if (!preserveNodes) {
            this.directory = null
        }

        _sheets = ArrayList<HSSFSheet>(INITIAL_CAPACITY)
        names = ArrayList<HSSFName?>(INITIAL_CAPACITY)

        // Grab the data from the workbook stream, however
        //  it happens to be spelled.
        val stream: InputStream? = directory.createDocumentInputStream(workbookName)

        val records = createRecords(stream)

        workbook = InternalWorkbook.createWorkbook(records as MutableList<Record>)
        setPropertiesFromWorkbook(workbook)
        val recOffset = workbook.numRecords

        // convert all LabelRecord records to LabelSSTRecord
        convertLabelRecords(records, recOffset)
        val rs = RecordStream(records, recOffset)
        while (rs.hasNext()) {
            val sheet = createSheet(rs)
            _sheets.add(HSSFSheet(this, sheet))
        }

        for (i in 0..<workbook.numNames) {
//            NameRecord nameRecord = workbook.getNameRecord(i);
//            HSSFName name = new HSSFName(this, nameRecord,
//                workbook.getNameCommentRecord(nameRecord));
//            names.add(name);
        }
    }

    /**
     * Companion to HSSFWorkbook(POIFSFileSystem), this constructs the POI filesystem around your
     * inputstream.
     * 
     * @param s  the POI filesystem that contains the Workbook stream.
     * @param preserveNodes whether to preseve other nodes, such as
     * macros.  This takes more memory, so only say yes if you
     * need to.
     * @see POIFSFileSystem
     * 
     * @see .HSSFWorkbook
     * @exception IOException if the stream cannot be read
     */
    @JvmOverloads
    constructor(s: InputStream?, preserveNodes: Boolean = true) : this(
        POIFSFileSystem(s),
        preserveNodes
    )

    /**
     * used internally to set the workbook properties.
     */
    private fun setPropertiesFromWorkbook(book: InternalWorkbook) {
        this.workbook = book

        // none currently
    }

    /**
     * This is basically a kludge to deal with the now obsolete Label records.  If
     * you have to read in a sheet that contains Label records, be aware that the rest
     * of the API doesn't deal with them, the low level structure only provides read-only
     * semi-immutable structures (the sets are there for interface conformance with NO
     * impelmentation).  In short, you need to call this function passing it a reference
     * to the Workbook object.  All labels will be converted to LabelSST records and their
     * contained strings will be written to the Shared String tabel (SSTRecord) within
     * the Workbook.
     * 
     * @param records a collection of sheet's records.
     * @param offset the offset to search at
     * @see LabelRecord
     * 
     * @see LabelSSTRecord
     * 
     * @see SSTRecord
     */
    private fun convertLabelRecords(records: MutableList<Record?>, offset: Int) {
        /*if (log.check(POILogger.DEBUG))
            log.log(POILogger.DEBUG, "convertLabelRecords called");*/
        for (k in offset..<records.size) {
            val rec = records.get(k) as Record

            if (rec.getSid() == LabelRecord.sid) {
                val oldrec = rec as LabelRecord

                records.removeAt(k)
                val newrec = LabelSSTRecord()
                val stringid = workbook.addSSTString(UnicodeString(oldrec.getValue() ?: ""))

                newrec.row = oldrec.row
                newrec.column = oldrec.column
                newrec.xFIndex = oldrec.xFIndex
                newrec.setSSTIndex(stringid)
                records.add(k, newrec)
            }
        }
        /*if (log.check(POILogger.DEBUG))
            log.log(POILogger.DEBUG, "convertLabelRecords exit");*/
    }

    /**
     * sets the order of appearance for a given sheet.
     * 
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */
    fun setSheetOrder(sheetname: String?, pos: Int) {
        val oldSheetIndex = getSheetIndex(sheetname)
        _sheets.add(pos, _sheets.removeAt(oldSheetIndex))
        workbook.setSheetOrder(sheetname, pos)

        val shifter = createForSheetShift(oldSheetIndex, pos)
        for (sheet in _sheets) {
            sheet.sheet.updateFormulasAfterCellShift(shifter,  /* not used */-1)
        }

        workbook.updateNamesAfterCellShift(shifter)
    }

    private fun validateSheetIndex(index: Int) {
        val lastSheetIx = _sheets.size - 1
        require(!(index < 0 || index > lastSheetIx)) {
            ("Sheet index (" + index + ") is out of range (0.."
                    + lastSheetIx + ")")
        }
    }

    /**
     * Selects a single sheet. This may be different to
     * the 'active' sheet (which is the sheet with focus).
     */
    fun setSelectedTab(index: Int) {
        validateSheetIndex(index)
        val nSheets = _sheets.size
        for (i in 0..<nSheets) {
            getSheetAt(i).isSelected = (i == index)
        }
        workbook.windowOne!!.setNumSelectedTabs(1.toShort())
    }

    /**
     * deprecated May 2008
     */
    @Deprecated("use setSelectedTab(int)")
    fun setSelectedTab(index: Short) {
        setSelectedTab(index.toInt())
    }

    fun setSelectedTabs(indexes: IntArray) {
        for (i in indexes.indices) {
            validateSheetIndex(indexes[i])
        }
        val nSheets = _sheets.size
        for (i in 0..<nSheets) {
            var bSelect = false
            for (j in indexes.indices) {
                if (indexes[j] == i) {
                    bSelect = true
                    break
                }
            }
            getSheetAt(i).isSelected = bSelect
        }
        workbook.windowOne!!.setNumSelectedTabs(indexes.size.toShort())
    }

    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     */
    fun setActiveSheet(index: Int) {
        validateSheetIndex(index)
        val nSheets = _sheets.size
        for (i in 0..<nSheets) {
            getSheetAt(i).isActive = (i == index)
        }
        workbook.windowOne!!.setActiveSheetIndex(index)
    }

    val activeSheetIndex: Int
        /**
         * gets the tab whose data is actually seen when the sheet is opened.
         * This may be different from the "selected sheet" since excel seems to
         * allow you to show the data of one sheet when another is seen "selected"
         * in the tabs (at the bottom).
         * @see HSSFSheet.setSelected
         */
        get() = workbook.windowOne!!.getActiveSheetIndex()

    @get:Deprecated("- Misleading name - use getActiveSheetIndex()")
    val selectedTab: Short
        /**
         * deprecated May 2008
         */
        get() = this.activeSheetIndex.toShort()

    var firstVisibleTab: Int
        /**
         * sets the first tab that is displayed in the list of tabs in excel.
         */
        get() = workbook.windowOne!!.getFirstVisibleTab()
        /**
         * sets the first tab that is displayed in the list of tabs
         * in excel.
         * @param index
         */
        set(index) {
            workbook.windowOne!!.setFirstVisibleTab(index)
        }

    @get:Deprecated("- Misleading name - use getFirstVisibleTab()")
    @set:Deprecated("- Misleading name - use setFirstVisibleTab()")
    var displayedTab: Short
        /**
         * deprecated May 2008
         */
        get() = this.firstVisibleTab.toShort()
        /**
         * deprecated May 2008
         */
        set(index) {
            this.firstVisibleTab = index.toInt()
        }

    /**
     * Set the sheet name.
     * 
     * @param sheetIx number (0 based)
     * @throws IllegalArgumentException if the name is null or invalid
     * or workbook already contains a sheet with this name
     * @see {@link .createSheet
     * @see {@link WorkbookUtil.createSafeSheetName
     */
    fun setSheetName(sheetIx: Int, name: String) {
        requireNotNull(name) { "sheetName must not be null" }

        require(
            !workbook.doesContainsSheetName(
                name,
                sheetIx
            )
        ) { "The workbook already contains a sheet with this name" }
        validateSheetIndex(sheetIx)
        workbook.setSheetName(sheetIx, name)
    }

    /**
     * @return Sheet name for the specified index
     */
    fun getSheetName(sheetIndex: Int): String {
        validateSheetIndex(sheetIndex)
        return workbook.getSheetName(sheetIndex)
    }

    var isHidden: Boolean
        get() = workbook.windowOne!!.getHidden()
        set(hiddenFlag) {
            workbook.windowOne!!.setHidden(hiddenFlag)
        }

    fun isSheetHidden(sheetIx: Int): Boolean {
        validateSheetIndex(sheetIx)
        return workbook.isSheetHidden(sheetIx)
    }

    fun isSheetVeryHidden(sheetIx: Int): Boolean {
        validateSheetIndex(sheetIx)
        return workbook.isSheetVeryHidden(sheetIx)
    }

    fun setSheetHidden(sheetIx: Int, hidden: Boolean) {
        validateSheetIndex(sheetIx)
        workbook.setSheetHidden(sheetIx, hidden)
    }

    fun setSheetHidden(sheetIx: Int, hidden: Int) {
        validateSheetIndex(sheetIx)
        WorkbookUtil.validateSheetState(hidden)
        workbook.setSheetHidden(sheetIx, hidden)
    }

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    fun getSheetIndex(name: String?): Int {
        return workbook.getSheetIndex(name)
    }

    /** Returns the index of the given sheet
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based). <tt>-1</tt> if not found
     */
    fun getSheetIndex(sheet: Sheet?): Int {
        for (i in _sheets.indices) {
            if (_sheets.get(i) == sheet) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the external sheet index of the sheet
     * with the given internal index, creating one
     * if needed.
     * Used by some of the more obscure formula and
     * named range things.
     */
    @Deprecated(
        """for POI internal use only (formula parsing).  This method is likely to
      be removed in future versions of POI."""
    )
    fun getExternalSheetIndex(internalSheetIndex: Int): Int {
        return workbook.checkExternSheet(internalSheetIndex).toInt()
    }

    @Deprecated(
        """for POI internal use only (formula rendering).  This method is likely to
      be removed in future versions of POI."""
    )
    fun findSheetNameFromExternSheet(externSheetIndex: Int): String? {
        // TODO - don't expose internal ugliness like externSheet indexes to the user model API
        return workbook.findSheetNameFromExternSheet(externSheetIndex)
    }

    /**
     * @param refIndex Index to REF entry in EXTERNSHEET record in the Link Table
     * @param definedNameIndex zero-based to DEFINEDNAME or EXTERNALNAME record
     * @return the string representation of the defined or external name
     */
    @Deprecated(
        """for POI internal use only (formula rendering).  This method is likely to
      be removed in future versions of POI.
     
      """
    )
    fun resolveNameXText(refIndex: Int, definedNameIndex: Int): String? {
        // TODO - make this less cryptic / move elsewhere
        return workbook.resolveNameXText(refIndex, definedNameIndex)
    }

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     * 
     * @return HSSFSheet representing the new sheet.
     */
    fun createSheet(): HSSFSheet {
        val sheet = HSSFSheet(this)

        _sheets.add(sheet)
        workbook.setSheetName(_sheets.size - 1, "Sheet" + (_sheets.size - 1))
        val isOnlySheet = _sheets.size == 1
        sheet.isSelected = isOnlySheet
        sheet.isActive = isOnlySheet
        return sheet
    }

    /**
     * create an HSSFSheet from an existing sheet in the HSSFWorkbook.
     * 
     * @return HSSFSheet representing the cloned sheet.
     */
    fun cloneSheet(sheetIndex: Int): HSSFSheet {
        validateSheetIndex(sheetIndex)
        val srcSheet = _sheets.get(sheetIndex)
        val srcName = workbook.getSheetName(sheetIndex)
        val clonedSheet = srcSheet.cloneSheet(this)
        clonedSheet.isSelected = false
        clonedSheet.isActive = false

        val name = getUniqueSheetName(srcName)
        val newSheetIndex = _sheets.size
        _sheets.add(clonedSheet)
        workbook.setSheetName(newSheetIndex, name)

        // Check this sheet has an autofilter, (which has a built-in NameRecord at workbook level)
        val filterDbNameIndex = findExistingBuiltinNameRecordIdx(
            sheetIndex,
            NameRecord.BUILTIN_FILTER_DB
        )
        if (filterDbNameIndex != -1) {
//            NameRecord newNameRecord = workbook.cloneFilter(filterDbNameIndex, newSheetIndex);
//            HSSFName newName = new HSSFName(this, newNameRecord);
//            names.add(newName);
        }
        // TODO - maybe same logic required for other/all built-in name records
        workbook.cloneDrawings(clonedSheet.sheet)

        return clonedSheet
    }

    private fun getUniqueSheetName(srcName: String): String {
        var uniqueIndex = 2
        var baseName: String? = srcName
        val bracketPos = srcName.lastIndexOf('(')
        if (bracketPos > 0 && srcName.endsWith(")")) {
            val suffix = srcName.substring(bracketPos + 1, srcName.length - ")".length)
            try {
                uniqueIndex = suffix.trim { it <= ' ' }.toInt()
                uniqueIndex++
                baseName = srcName.substring(0, bracketPos).trim { it <= ' ' }
            } catch (e: NumberFormatException) {
                // contents of brackets not numeric
            }
        }
        while (true) {
            // Try and find the next sheet name that is unique
            val index = (uniqueIndex++).toString()
            val name: String
            if (baseName!!.length + index.length + 2 < 31) {
                name = baseName + " (" + index + ")"
            } else {
                name = baseName.substring(0, 31 - index.length - 2) + "(" + index + ")"
            }

            //If the sheet name is unique, then set it otherwise move on to the next number.
            if (workbook.getSheetIndex(name) == -1) {
                return name
            }
        }
    }

    /**
     * Create a new sheet for this Workbook and return the high level representation.
     * Use this to create new sheets.
     * 
     * 
     * 
     * Note that Excel allows sheet names up to 31 chars in length but other applications
     * (such as OpenOffice) allow more. Some versions of Excel crash with names longer than 31 chars,
     * others - truncate such names to 31 character.
     * 
     * 
     * 
     * POI's SpreadsheetAPI silently truncates the input argument to 31 characters.
     * Example:
     * 
     * <pre>`
     * Sheet sheet = workbook.createSheet("My very long sheet name which is longer than 31 chars"); // will be truncated
     * assert 31 == sheet.getSheetName().length();
     * assert "My very long sheet name which i" == sheet.getSheetName();
    `</pre> * 
     * 
     * 
     * Except the 31-character constraint, Excel applies some other rules:
     * 
     * 
     * Sheet name MUST be unique in the workbook and MUST NOT contain the any of the following characters:
     * 
     *  *  0x0000 
     *  *  0x0003 
     *  *  colon (:) 
     *  *  backslash (\) 
     *  *  asterisk (*) 
     *  *  question mark (?) 
     *  *  forward slash (/) 
     *  *  opening square bracket ([) 
     *  *  closing square bracket (]) 
     * 
     * The string MUST NOT begin or end with the single quote (') character.
     * 
     * 
     * @param sheetname  sheetname to set for the sheet.
     * @return Sheet representing the new sheet.
     * @throws IllegalArgumentException if the name is null or invalid
     * or workbook already contains a sheet with this name
     * @see {@link WorkbookUtil.createSafeSheetName
     */
    fun createSheet(sheetname: String): HSSFSheet {
        requireNotNull(sheetname) { "sheetName must not be null" }

        require(
            !workbook.doesContainsSheetName(
                sheetname,
                _sheets.size
            )
        ) { "The workbook already contains a sheet of this name" }

        val sheet = HSSFSheet(this)

        workbook.setSheetName(_sheets.size, sheetname)
        _sheets.add(sheet)
        val isOnlySheet = _sheets.size == 1
        sheet.isSelected = isOnlySheet
        sheet.isActive = isOnlySheet
        return sheet
    }

    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */
    override fun getNumberOfSheets(): Int {
        return _sheets.size
    }

    fun getSheetIndexFromExternSheetIndex(externSheetNumber: Int): Int {
        return workbook.getSheetIndexFromExternSheetIndex(externSheetNumber)
    }

    private val sheets: Array<HSSFSheet?>
        get() {
            return Array<HSSFSheet?>(_sheets.size) { _sheets[it] }
        }

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical & logical)
     * @return HSSFSheet at the provided index
     */
    override fun getSheetAt(index: Int): HSSFSheet {
        validateSheetIndex(index)
        return _sheets.get(index)
    }

    val isEmpty: Boolean
        /**
         * workbook has sheet(s) or not
         * @return
         */
        get() = _sheets.size == 0

    /**
     * Get sheet with the given name (case insensitive match)
     * @param name of the sheet
     * @return HSSFSheet with the name provided or `null` if it does not exist
     */
    fun getSheet(name: String?): HSSFSheet? {
        var retval: HSSFSheet? = null

        for (k in _sheets.indices) {
            val sheetname = workbook.getSheetName(k)

            if (sheetname.equals(name, ignoreCase = true)) {
                retval = _sheets.get(k) as HSSFSheet?
            }
        }
        return retval
    }

    /**
     * Removes sheet at the given index.
     *
     *
     * 
     * Care must be taken if the removed sheet is the currently active or only selected sheet in
     * the workbook. There are a few situations when Excel must have a selection and/or active
     * sheet. (For example when printing - see Bug 40414).<br></br>
     * 
     * This method makes sure that if the removed sheet was active, another sheet will become
     * active in its place.  Furthermore, if the removed sheet was the only selected sheet, another
     * sheet will become selected.  The newly active/selected sheet will have the same index, or
     * one less if the removed sheet was the last in the workbook.
     * 
     * @param index of the sheet  (0-based)
     */
    fun removeSheetAt(index: Int) {
        validateSheetIndex(index)
        val wasActive = getSheetAt(index).isActive
        val wasSelected = getSheetAt(index).isSelected

        _sheets.removeAt(index)
        workbook.removeSheet(index)

        // set the remaining active/selected sheet
        val nSheets = _sheets.size
        if (nSheets < 1) {
            // nothing more to do if there are no sheets left
            return
        }
        // the index of the closest remaining sheet to the one just deleted
        var newSheetIndex = index
        if (newSheetIndex >= nSheets) {
            newSheetIndex = nSheets - 1
        }
        if (wasActive) {
            setActiveSheet(newSheetIndex)
        }

        if (wasSelected) {
            var someOtherSheetIsStillSelected = false
            for (i in 0..<nSheets) {
                if (getSheetAt(i).isSelected) {
                    someOtherSheetIsStillSelected = true
                    break
                }
            }
            if (!someOtherSheetIsStillSelected) {
                setSelectedTab(newSheetIndex)
            }
        }
    }

    var backupFlag: Boolean
        /**
         * determine whether the Excel GUI will backup the workbook when saving.
         * 
         * @return the current setting for backups.
         */
        get() {
            val backupRecord = workbook.backupRecord

            return if (backupRecord!!.backup.toInt() == 0) false else true
        }
        /**
         * determine whether the Excel GUI will backup the workbook when saving.
         * 
         * @param backupValue   true to indicate a backup will be performed.
         */
        set(backupValue) {
            val backupRecord = workbook.backupRecord

            backupRecord!!.backup = if (backupValue) 1.toShort() else 0.toShort()
        }

    /**
     * Sets the repeating rows and columns for a sheet (as found in
     * 2003:File->PageSetup->Sheet, 2007:Page Layout->Print Titles).
     * This is function is included in the workbook
     * because it creates/modifies name records which are stored at the
     * workbook level.
     * 
     * 
     * To set just repeating columns:
     * <pre>
     * workbook.setRepeatingRowsAndColumns(0,0,1,-1-1);
    </pre> * 
     * To set just repeating rows:
     * <pre>
     * workbook.setRepeatingRowsAndColumns(0,-1,-1,0,4);
    </pre> * 
     * To remove all repeating rows and columns for a sheet.
     * <pre>
     * workbook.setRepeatingRowsAndColumns(0,-1,-1,-1,-1);
    </pre> * 
     * 
     * @param sheetIndex    0 based index to sheet.
     * @param startColumn   0 based start of repeating columns.
     * @param endColumn     0 based end of repeating columns.
     * @param startRow      0 based start of repeating rows.
     * @param endRow        0 based end of repeating rows.
     */
    fun setRepeatingRowsAndColumns(
        sheetIndex: Int, startColumn: Int, endColumn: Int,
        startRow: Int, endRow: Int
    ) {
        // Check arguments
        require(!(startColumn == -1 && endColumn != -1)) { "Invalid column range specification" }
        require(!(startRow == -1 && endRow != -1)) { "Invalid row range specification" }
        require(!(startColumn < -1 || startColumn >= MAX_COLUMN)) { "Invalid column range specification" }
        require(!(endColumn < -1 || endColumn >= MAX_COLUMN)) { "Invalid column range specification" }
        require(!(startRow < -1 || startRow > MAX_ROW)) { "Invalid row range specification" }
        require(!(endRow < -1 || endRow > MAX_ROW)) { "Invalid row range specification" }
        require(startColumn <= endColumn) { "Invalid column range specification" }
        require(startRow <= endRow) { "Invalid row range specification" }

        val sheet = getSheetAt(sheetIndex)
        val externSheetIndex = this.workbook.checkExternSheet(sheetIndex)

        val settingRowAndColumn =
            startColumn != -1 && endColumn != -1 && startRow != -1 && endRow != -1
        val removingRange = startColumn == -1 && endColumn == -1 && startRow == -1 && endRow == -1

        val rowColHeaderNameIndex = findExistingBuiltinNameRecordIdx(
            sheetIndex,
            NameRecord.BUILTIN_PRINT_TITLE
        )
        if (removingRange) {
            if (rowColHeaderNameIndex >= 0) {
                workbook.removeName(rowColHeaderNameIndex)
            }
            return
        }
        val isNewRecord: Boolean
        val nameRecord: NameRecord?
        if (rowColHeaderNameIndex < 0) {
            //does a lot of the house keeping for builtin records, like setting lengths to zero etc
            nameRecord = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_TITLE, sheetIndex + 1)
            isNewRecord = true
        } else {
            nameRecord = workbook.getNameRecord(rowColHeaderNameIndex)
            isNewRecord = false
        }

        val temp: MutableList<Ptg?> = ArrayList<Ptg?>()

        if (settingRowAndColumn) {
            val exprsSize = 2 * 11 + 1 // 2 * Area3DPtg.SIZE + UnionPtg.SIZE
            temp.add(MemFuncPtg(exprsSize))
        }
        if (startColumn >= 0) {
            val colArea = Area3DPtg(
                0, MAX_ROW, startColumn, endColumn, false, false,
                false, false, externSheetIndex.toInt()
            )
            temp.add(colArea)
        }
        if (startRow >= 0) {
            val rowArea = Area3DPtg(
                startRow, endRow, 0, MAX_COLUMN.toInt(), false, false, false,
                false, externSheetIndex.toInt()
            )
            temp.add(rowArea)
        }
        if (settingRowAndColumn) {
            temp.add(UnionPtg.instance)
        }
        val ptgs = temp.toTypedArray()
        nameRecord.setNameDefinition(ptgs)

        if (isNewRecord) {
//            HSSFName newName = new HSSFName(this, nameRecord, nameRecord.isBuiltInName() ? null
//                : workbook.getNameCommentRecord(nameRecord));
//            names.add(newName);
        }

        val printSetup = sheet.printSetup
        printSetup.setValidSettings(false)

        sheet.isActive = true
    }

    private fun findExistingBuiltinNameRecordIdx(sheetIndex: Int, builtinCode: Byte): Int {
        for (defNameIndex in names.indices) {
            val r = workbook.getNameRecord(defNameIndex)
            if (r == null) {
                throw RuntimeException("Unable to find all defined names to iterate over")
            }
            if (!r.isBuiltInName() || r.getBuiltInName() != builtinCode) {
                continue
            }
            if (r.getSheetNumber() - 1 == sheetIndex) {
                return defNameIndex
            }
        }
        return -1
    }

    /**
     * create a new Font and add it to the workbook's font table
     * @return new font object
     */
    fun createFont(): HSSFFont {
        val font = workbook.createNewFont()
        var fontindex = (this.numberOfFonts - 1).toShort()

        if (fontindex > 3) {
            fontindex++ // THERE IS NO FOUR!!
        }
        require(fontindex != Short.MAX_VALUE) { "Maximum number of fonts was exceeded" }

        // Ask getFontAt() to build it for us,
        //  so it gets properly cached
        return getFontAt(fontindex)
    }

    /**
     * Finds a font that matches the one with the supplied attributes
     */
    fun findFont(
        boldWeight: Short, color: Short, fontHeight: Short, name: String?,
        italic: Boolean, strikeout: Boolean, typeOffset: Short, underline: Byte
    ): HSSFFont? {
        for (i in 0..this.numberOfFonts) {
            // Remember - there is no 4!
            if (i.toInt() == 4) continue

            val hssfFont = getFontAt(i.toShort())
            if (hssfFont.getBoldweight() == boldWeight && hssfFont.getColor() == color && hssfFont.getFontHeight() == fontHeight && hssfFont.getFontName() == name
                && hssfFont.getItalic() == italic && hssfFont.getStrikeout() == strikeout && hssfFont.getTypeOffset() == typeOffset && hssfFont.getUnderline() == underline
            ) {
                return hssfFont
            }
        }

        return null
    }

    val numberOfFonts: Short
        /**
         * get the number of fonts in the font table
         * @return number of fonts
         */
        get() = workbook.numberOfFontRecords.toShort()

    /**
     * Get the font at the given index number
     * @param idx  index number
     * @return HSSFFont at the index
     */
    fun getFontAt(idx: Short): HSSFFont {
        if (fonts == null) fonts = Hashtable<Short?, HSSFFont?>()

        // So we don't confuse users, give them back
        //  the same object every time, but create
        //  them lazily
        val sIdx: Short? = idx
        if (fonts!!.containsKey(sIdx)) {
            return fonts!!.get(sIdx) as HSSFFont
        }

        val font = workbook.getFontRecordAt(idx.toInt())
        val retval = HSSFFont(idx, font)
        fonts!!.put(sIdx, retval)

        return retval
    }

    /**
     * Reset the fonts cache, causing all new calls
     * to getFontAt() to create new objects.
     * Should only be called after deleting fonts,
     * and that's not something you should normally do
     */
    fun resetFontCache() {
        fonts = Hashtable<Short?, HSSFFont?>()
    }

    /**
     * Create a new Cell style and add it to the workbook's style table.
     * You can define up to 4000 unique styles in a .xls workbook.
     * 
     * @return the new Cell Style object
     * @throws IllegalStateException if the maximum number of cell styles exceeded the limit
     */
    fun createCellStyle(): HSSFCellStyle {
        check(workbook.numExFormats != MAX_STYLES) {
            ("The maximum number of cell styles was exceeded. "
                    + "You can define up to 4000 styles in a .xls workbook")
        }
        val xfr = workbook.createCellXF()
        val index = (this.numCellStyles - 1).toShort()
        val style = HSSFCellStyle(index, xfr, this)

        return style
    }

    val numCellStyles: Short
        /**
         * get the number of styles the workbook contains
         * @return count of cell styles
         */
        get() = workbook.numExFormats.toShort()

    /**
     * get the cell style object at the given index
     * @param idx  index within the set of styles
     * @return HSSFCellStyle object at the index
     */
    fun getCellStyleAt(idx: Short): HSSFCellStyle? {
        val xfr = workbook.getExFormatAt(idx.toInt())
        if (xfr != null) {
            return HSSFCellStyle(idx, xfr, this)
        } else {
            return null
        }
    }

    /**
     * Method write - write out this workbook to an Outputstream.  Constructs
     * a new POI POIFSFileSystem, passes in the workbook binary representation  and
     * writes it out.
     * 
     * @param stream - the java OutputStream you wish to write the XLS to
     * 
     * @exception IOException if anything can't be written.
     * @see POIFSFileSystem
     */
    @Throws(IOException::class)
    override fun write(stream: OutputStream?) {
        val bytes = this.bytes
        val fs = POIFSFileSystem()

        // For tracking what we've written out, used if we're
        //  going to be preserving nodes
        val excepts: MutableList<String?> = ArrayList<String?>(1)

        // Write out the Workbook stream
        fs.createDocument(ByteArrayInputStream(bytes), "Workbook")

        // Write out our HPFS properties, if we have them
        writeProperties(fs, excepts)

        if (preserveNodes) {
            // Don't write out the old Workbook, we'll be doing our new one
            excepts.add("Workbook")
            // If the file had WORKBOOK instead of Workbook, we'll write it
            //  out correctly shortly, so don't include the old one
            excepts.add("WORKBOOK")

            // Copy over all the other nodes to our new poifs
            copyNodes(this.directory, fs.getRoot(), excepts)

            // YK: preserve StorageClsid, it is important for embedded workbooks,
            // see Bugzilla 47920
            fs.getRoot().setStorageClsid(this.directory.getStorageClsid())
        }
        fs.writeFilesystem(stream)
    }

    /**
     * Totals the sizes of all sheet records and eventually serializes them
     */
    private class SheetRecordCollector : RecordVisitor {
        private val _list: MutableList<Record>
        var totalSize: Int = 0
            private set

        init {
            _list = ArrayList<Record>(128)
        }

        override fun visitRecord(r: Record) {
            _list.add(r)
            this.totalSize += r.getRecordSize()
        }

        fun serialize(offset: Int, data: ByteArray): Int {
            var result = 0
            val nRecs = _list.size
            for (i in 0..<nRecs) {
                val rec = _list.get(i)
                result += rec.serialize(offset + result, data)
            }
            return result
        }
    }

    val bytes: ByteArray
        /**
         * Method getBytes - get the bytes of just the HSSF portions of the XLS file.
         * Use this to construct a POI POIFSFileSystem yourself.
         * 
         * 
         * @return byte[] array containing the binary representation of this workbook and all contained
         * sheets, rows, cells, etc.
         */
        get() {
            /*if (log.check(POILogger.DEBUG))
            {
                log.log(DEBUG, "HSSFWorkbook.getBytes()");
            }*/

            val sheets = this.sheets
            val nSheets = sheets.size

            // before getting the workbook size we must tell the sheets that
            // serialization is about to occur.
            for (i in 0..<nSheets) {
                sheets[i]!!.sheet.preSerialize()
            }

            var totalsize = workbook.size

            // pre-calculate all the sheet sizes and set BOF indexes
            val srCollectors: Array<SheetRecordCollector?> =
                arrayOfNulls<SheetRecordCollector>(nSheets)
            for (k in 0..<nSheets) {
                workbook.setSheetBof(k, totalsize)
                val src = SheetRecordCollector()
                sheets[k]!!.sheet.visitContainedRecords(src, totalsize)
                totalsize += src.totalSize
                srCollectors[k] = src
            }

            val retval = ByteArray(totalsize)
            var pos = workbook.serialize(0, retval)

            for (k in 0..<nSheets) {
                val src = srCollectors[k]!!
                val serializedSize = src.serialize(pos, retval)
                check(serializedSize == src.totalSize) {
                    ("Actual serialized sheet size (" + serializedSize
                            + ") differs from pre-calculated size (" + src.totalSize + ") for sheet ("
                            + k + ")")
                }
                pos += serializedSize
            }
            return retval
        }

    @Deprecated(
        """Do not call this method from your applications. Use the methods
       available in the HSSFRow to add string HSSFCells"""
    )
    fun addSSTString(string: String): Int {
        return workbook.addSSTString(UnicodeString(string))
    }

    val sSTUniqueStringSize: Int
        /**
         * 
         * @return
         */
        get() = workbook.sSTUniqueStringSize

    @Deprecated(
        """Do not call this method from your applications. Use the methods
       available in the HSSFRow to get string HSSFCells"""
    )
    fun getSSTString(index: Int): String {
        return workbook.getSSTString(index)!!.string
    }

    val numberOfNames: Int
        get() {
            val result = names.size
            return result
        }

    fun getName(name: String?): HSSFName? {
        val nameIndex = getNameIndex(name)
        if (nameIndex < 0) {
            return null
        }
        return names.get(nameIndex)
    }

    fun getNameAt(nameIndex: Int): HSSFName? {
        val nNames = names.size
        check(nNames >= 1) { "There are no defined names in this workbook" }
        require(!(nameIndex < 0 || nameIndex > nNames)) {
            ("Specified name index " + nameIndex
                    + " is outside the allowable range (0.." + (nNames - 1) + ").")
        }
        return names.get(nameIndex)
    }

    fun getNameRecord(nameIndex: Int): NameRecord {
        return this.workbook.getNameRecord(nameIndex)
    }

    /** gets the named range name
     * @param index the named range index (0 based)
     * @return named range name
     */
    fun getNameName(index: Int): String? {
        val result = getNameAt(index)!!.getNameName()

        return result
    }

    /**
     * Sets the printarea for the sheet provided
     * 
     * 
     * i.e. Reference = $A$1:$B$2
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @param reference Valid name Reference for the Print Area
     */
    fun setPrintArea(sheetIndex: Int, reference: String) {
        var name = workbook.getSpecificBuiltinRecord(
            NameRecord.BUILTIN_PRINT_AREA,
            sheetIndex + 1
        )

        if (name == null) {
            name = workbook.createBuiltInName(NameRecord.BUILTIN_PRINT_AREA, sheetIndex + 1)
            // adding one here because 0 indicates a global named region; doesn't make sense for print areas
        }
        val parts: Array<String?> = COMMA_PATTERN.split(reference)
        val sb = StringBuffer(32)
        for (i in parts.indices) {
            if (i > 0) {
                sb.append(",")
            }
            appendFormat(sb, getSheetName(sheetIndex))
            sb.append("!")
            sb.append(parts[i])
        }
        //        name.setNameDefinition(HSSFFormulaParser.parse(sb.toString(), this, FormulaType.NAMEDRANGE,
//            sheetIndex));
    }

    /**
     * For the Convenience of Java Programmers maintaining pointers.
     * @see .setPrintArea
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     * @param startColumn Column to begin printarea
     * @param endColumn Column to end the printarea
     * @param startRow Row to begin the printarea
     * @param endRow Row to end the printarea
     */
    fun setPrintArea(
        sheetIndex: Int, startColumn: Int, endColumn: Int, startRow: Int,
        endRow: Int
    ) {
        //using absolute references because they don't get copied and pasted anyway

        var cell = CellReference(startRow, startColumn, true, true)
        var reference = cell.formatAsString()

        cell = CellReference(endRow, endColumn, true, true)
        reference = reference + ":" + cell.formatAsString()

        setPrintArea(sheetIndex, reference)
    }

    /**
     * Retrieves the reference for the printarea of the specified sheet, the sheet name is appended to the reference even if it was not specified.
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    fun getPrintArea(sheetIndex: Int): String? {
        val name = workbook.getSpecificBuiltinRecord(
            NameRecord.BUILTIN_PRINT_AREA,
            sheetIndex + 1
        )
        //adding one here because 0 indicates a global named region; doesn't make sense for print areas
        if (name == null) {
            return null
        }

        //return HSSFFormulaParser.toFormulaString(this, name.getNameDefinition());
        return null
    }

    /**
     * Delete the printarea for the sheet specified
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    fun removePrintArea(sheetIndex: Int) {
        this.workbook.removeBuiltinRecord(NameRecord.BUILTIN_PRINT_AREA, sheetIndex + 1)
    }

    /** creates a new named range and add it to the model
     * @return named range high level
     */
    fun createName(): HSSFName? {
//        NameRecord nameRecord = workbook.createName();
//
//        HSSFName newName = new HSSFName(this, nameRecord);
//
//        names.add(newName);
//
//        return newName;
        return null
    }

    fun getNameIndex(name: String?): Int {
        for (k in names.indices) {
            val nameName = getNameName(k)

            if (nameName.equals(name, ignoreCase = true)) {
                return k
            }
        }
        return -1
    }

    fun removeName(index: Int) {
        names.removeAt(index)
        workbook.removeName(index)
    }

    /**
     * Returns the instance of HSSFDataFormat for this workbook.
     * @return the HSSFDataFormat object
     * @see FormatRecord
     * 
     * @see Record
     */
    fun createDataFormat(): HSSFDataFormat {
        if (formatter == null) formatter = HSSFDataFormat(workbook)
        return formatter!!
    }

    fun removeName(name: String?) {
        val index = getNameIndex(name)

        removeName(index)
    }

    val customPalette: HSSFPalette
        get() {
            if (palette == null) {
                palette = HSSFPalette(workbook.customPalette)
            }
            return palette!!
        }

    /** Test only. Do not use  */
    fun insertChartRecord() {
        val loc = workbook.findFirstRecordLocBySid(SSTRecord.sid)
        val data = byteArrayOf(
            0x0F.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0xF0.toByte(),
            0x52.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x06.toByte(),
            0xF0.toByte(),
            0x18.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x03.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x33.toByte(),
            0x00.toByte(),
            0x0B.toByte(),
            0xF0.toByte(),
            0x12.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0xBF.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x00.toByte(),
            0x81.toByte(),
            0x01.toByte(),
            0x09.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xC0.toByte(),
            0x01.toByte(),
            0x40.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x40.toByte(),
            0x00.toByte(),
            0x1E.toByte(),
            0xF1.toByte(),
            0x10.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0D.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x0C.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0x17.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xF7.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x10.toByte(),
        )
        val r = UnknownRecord(0x00EB.toShort().toInt(), data)
        workbook.getRecords()!!.add(loc, r)
    }

    /**
     * Spits out a list of all the drawing records in the workbook.
     */
    fun dumpDrawingGroupRecords(fat: Boolean) {
        val r = workbook
            .findFirstRecordBySid(DrawingGroupRecord.sid) as DrawingGroupRecord?
        r!!.decode()
        val escherRecords: MutableList<*> = r.escherRecords
        val w = PrintWriter(System.out)
        val iterator: MutableIterator<*> = escherRecords.iterator()
        while (iterator.hasNext()) {
            val escherRecord = iterator.next() as EscherRecord
            if (fat) println(escherRecord.toString())
            else escherRecord.display(w, 0)
        }
        w.flush()
    }

    fun initDrawings() {
        val mgr = workbook.findDrawingGroup()
        if (mgr != null) {
            for (i in 0..<getNumberOfSheets()) {
                getSheetAt(i).drawingPatriarch
            }
        } else {
            workbook.createDrawingGroup()
        }
    }

    /**
     * Adds a picture to the workbook.
     * 
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.  One of `PICTURE_TYPE_*`
     * 
     * @return the index to this picture (1 based).
     */
    fun addPicture(pictureData: ByteArray, format: Int): Int {
        initDrawings()

        val uid = md5(pictureData)
        val blipRecord = EscherBitmapBlip()
        blipRecord.recordId = (EscherBlipRecord.RECORD_ID_START + format).toShort()
        when (format) {
            Workbook.PICTURE_TYPE_EMF -> blipRecord.options = HSSFPictureData.Companion.MSOBI_EMF
            Workbook.PICTURE_TYPE_WMF -> blipRecord.options = HSSFPictureData.Companion.MSOBI_WMF
            Workbook.PICTURE_TYPE_PICT -> blipRecord.options = HSSFPictureData.Companion.MSOBI_PICT
            Workbook.PICTURE_TYPE_PNG -> blipRecord.options = HSSFPictureData.Companion.MSOBI_PNG
            Workbook.PICTURE_TYPE_JPEG -> blipRecord.options = HSSFPictureData.Companion.MSOBI_JPEG
            Workbook.PICTURE_TYPE_DIB -> blipRecord.options = HSSFPictureData.Companion.MSOBI_DIB
        }

        blipRecord.uID = uid
        blipRecord.marker = 0xFF.toByte()
        blipRecord.setPictureData(pictureData)

        val r = EscherBSERecord()
        r.recordId = EscherBSERecord.RECORD_ID
        r.options = (0x0002 or (format shl 4)).toShort()
        r.blipTypeMacOS = format.toByte()
        r.blipTypeWin32 = format.toByte()
        r.uid = uid
        r.tag = 0xFF.toShort()
        r.size = pictureData.size + 25
        r.ref = 1
        r.offset = 0
        r.blipRecord = blipRecord

        return workbook.addBSERecord(r)
    }

    val allPictures: MutableList<HSSFPictureData?>
        /**
         * Gets all pictures from the Workbook.
         * 
         * @return the list of pictures (a list of [HSSFPictureData] objects.)
         */
        get() {
            // The drawing group record always exists at the top level, so we won't need to do this recursively.
            val pictures: MutableList<HSSFPictureData?> =
                ArrayList<HSSFPictureData?>()
            val recordIter =
                workbook.getRecords()!!.iterator()
            while (recordIter.hasNext()) {
                val r = recordIter.next()
                if (r is AbstractEscherHolderRecord) {
                    r.decode()
                    val escherRecords: MutableList<EscherRecord> =
                        r.escherRecords
                    searchForPictures(escherRecords, pictures)
                }
            }
            return pictures
        }

    /**
     * Performs a recursive search for pictures in the given list of escher records.
     * 
     * @param escherRecords the escher records.
     * @param pictures the list to populate with the pictures.
     */
    private fun searchForPictures(
        escherRecords: MutableList<EscherRecord>,
        pictures: MutableList<HSSFPictureData?>
    ) {
        for (escherRecord in escherRecords) {
            if (escherRecord is EscherBSERecord) {
                val blip = escherRecord.blipRecord
                if (blip != null) {
                    // TODO: Some kind of structure.
                    val picture = HSSFPictureData(blip)
                    pictures.add(picture)
                }
            }

            // Recursive call.
            searchForPictures(escherRecord.childRecords, pictures)
        }
    }

    val isWriteProtected: Boolean
        /**
         * Is the workbook protected with a password (not encrypted)?
         */
        get() = this.workbook.isWriteProtected

    /**
     * protect a workbook with a password (not encypted, just sets writeprotect
     * flags and the password.
     * @param password to set
     */
    fun writeProtectWorkbook(password: String, username: String?) {
        this.workbook.writeProtectWorkbook(password, username)
    }

    /**
     * removes the write protect flag
     */
    fun unwriteProtectWorkbook() {
        this.workbook.unwriteProtectWorkbook()
    }

    val allEmbeddedObjects: MutableList<HSSFObjectData?>
        /**
         * Gets all embedded OLE2 objects from the Workbook.
         * 
         * @return the list of embedded objects (a list of [HSSFObjectData] objects.)
         */
        get() {
            val objects: MutableList<HSSFObjectData?> =
                ArrayList<HSSFObjectData?>()
            for (i in 0..<getNumberOfSheets()) {
                getAllEmbeddedObjects(getSheetAt(i).sheet.records, objects)
            }
            return objects
        }

    /**
     * Gets all embedded OLE2 objects from the Workbook.
     * 
     * @param records the list of records to search.
     * @param objects the list of embedded objects to populate.
     */
    private fun getAllEmbeddedObjects(
        records: MutableList<RecordBase>,
        objects: MutableList<HSSFObjectData?>
    ) {
        for (obj in records) {
            if (obj is ObjRecord) {
                // TODO: More convenient way of determining if there is stored binary.
                // TODO: Link to the data stored in the other stream.
                val subRecordIter: MutableIterator<SubRecord?> = obj.getSubRecords()!!.iterator()
                while (subRecordIter.hasNext()) {
                    val sub = subRecordIter.next()
                    if (sub is EmbeddedObjectRefSubRecord) {
                        objects.add(HSSFObjectData(obj, directory))
                    }
                }
            }
        }
    }

    val creationHelper: HSSFCreationHelper
        get() = HSSFCreationHelper(this)

    /**
     * Register a new toolpack in this workbook.
     * 
     * @param toopack the toolpack to register
     */
    fun addToolPack(toopack: UDFFinder?) {
        val udfs = this.uDFFinder as AggregatingUDFFinder
        udfs.add(toopack)
    }

    var forceFormulaRecalculation: Boolean
        /**
         * Whether Excel will be asked to recalculate all formulas when the  workbook is opened.
         * 
         * @since 3.8
         */
        get() {
            val iwb = this.workbook
            val recalc =
                iwb.findFirstRecordBySid(RecalcIdRecord.sid) as RecalcIdRecord?
            return recalc != null && recalc.getEngineId() != 0
        }
        /**
         * Whether the application shall perform a full recalculation when the workbook is opened.
         * 
         * 
         * Typically you want to force formula recalculation when you modify cell formulas or values
         * of a workbook previously created by Excel. When set to true, this flag will tell Excel
         * that it needs to recalculate all formulas in the workbook the next time the file is opened.
         * 
         * 
         * 
         * Note, that recalculation updates cached formula results and, thus, modifies the workbook.
         * Depending on the version, Excel may prompt you with "Do you want to save the changes in *filename*?"
         * on close.
         * 
         * 
         * @param value true if the application will perform a full recalculation of
         * workbook values when the workbook is opened
         * @since 3.8
         */
        set(value) {
            val iwb = this.workbook
            val recalc = iwb.recalcId
            recalc.setEngineId(0)
        }

    val isUsing1904DateWindowing: Boolean
        /**
         * Whether date windowing is based on 1/2/1904 or 1/1/1900.
         * Some versions of Excel (Mac) can save workbooks using 1904 date windowing.
         * 
         * @return true if using 1904 date windowing
         */
        get() = workbook.isUsing1904DateWindowing

    companion object {
        private val COMMA_PATTERN: Pattern = Pattern.compile(",")
        private const val MAX_ROW = 0xFFFF
        private val MAX_COLUMN = 0x00FF.toShort()

        /**
         * The maximum number of cell styles in a .xls workbook.
         * The 'official' limit is 4,000, but POI allows a slightly larger number.
         * This extra delta takes into account built-in styles that are automatically
         * created for new workbooks
         * 
         * See http://office.microsoft.com/en-us/excel-help/excel-specifications-and-limits-HP005199291.aspx
         */
        private const val MAX_STYLES = 4030

        //private static final int DEBUG = POILogger.DEBUG;
        /**
         * used for compile-time performance/memory optimization.  This determines the
         * initial capacity for the sheet collection.  Its currently set to 3.
         * Changing it in this release will decrease performance
         * since you're never allowed to have more or less than three sheets!
         */
        const val INITIAL_CAPACITY: Int = 3

        fun create(book: InternalWorkbook): HSSFWorkbook {
            return HSSFWorkbook(book)
        }

        /**
         * Normally, the Workbook will be in a POIFS Stream
         * called "Workbook". However, some weird XLS generators use "WORKBOOK"
         */
        private val WORKBOOK_DIR_ENTRY_NAMES = arrayOf<String?>(
            "Workbook",  // as per BIFF8 spec
            "WORKBOOK",
        )

        private fun getWorkbookDirEntryName(directory: DirectoryNode): String? {
            val potentialNames: Array<String?> = WORKBOOK_DIR_ENTRY_NAMES
            for (i in potentialNames.indices) {
                val wbName = potentialNames[i]
                try {
                    directory.getEntry(wbName)
                    return wbName
                } catch (e: FileNotFoundException) {
                    // continue - to try other options
                }
            }

            // check for previous version of file format
            try {
                directory.getEntry("Book")
                throw OldExcelFormatException(
                    "The supplied spreadsheet seems to be Excel 5.0/7.0 (BIFF5) format. "
                            + "POI only supports BIFF8 format (from Excel versions 97/2000/XP/2003)"
                )
            } catch (e: FileNotFoundException) {
                // fall through
            }

            throw IllegalArgumentException(
                "The supplied POIFSFileSystem does not contain a BIFF8 'Workbook' entry. "
                        + "Is it really an excel file?"
            )
        }
    }
}
