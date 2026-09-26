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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherBinaryTagRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hslf.record.CString
import com.wxiwei.office.fc.hslf.record.ColorSchemeAtom
import com.wxiwei.office.fc.hslf.record.ExtendedParagraphAtom
import com.wxiwei.office.fc.hslf.record.OEPlaceholderAtom
import com.wxiwei.office.fc.hslf.record.PPDrawing
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordContainer
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.record.RoundTripHFPlaceholder12
import com.wxiwei.office.fc.hslf.record.SheetContainer
import com.wxiwei.office.fc.hslf.record.StyleTextPropAtom
import com.wxiwei.office.fc.hslf.record.TextBytesAtom
import com.wxiwei.office.fc.hslf.record.TextCharsAtom
import com.wxiwei.office.fc.hslf.record.TextHeaderAtom
import com.wxiwei.office.fc.hslf.usermodel.SlideShow
import java.util.Vector

/**
 * This class defines the common format of "Sheets" in a powerpoint
 * document. Such sheets could be Slides, Notes, Master etc
 * 
 * @author Nick Burch
 * @author Yegor Kozlov
 */
abstract class Sheet
    (
    /**
     * Record container that holds sheet data.
     * For slides it is arc.fc.hslf.record.Slide,
     * for notes it is arc.fc.hslf.record.Notes,
     * for slide masters it is arc.fc.hslf.record.SlideMaster, etc.
     */
    var sheetContainer: SheetContainer?, private val _sheetNo: Int
) {
    /**
     * Returns an array of all the TextRuns in the sheet.
     */
    abstract val textRuns: Array<TextRun>?

    /**
     * Returns the (internal, RefID based) sheet number, as used
     * to in PersistPtr stuff.
     */
    fun _getSheetRefId(): Int {
        return sheetContainer!!.sheetId
    }

    /**
     * Returns the (internal, SlideIdentifier based) sheet number, as used
     * to reference this sheet from other records.
     */
    fun _getSheetNumber(): Int {
        return _sheetNo
    }

    val pPDrawing: PPDrawing?
        /**
         * Fetch the PPDrawing from the underlying record
         */
        get() = sheetContainer!!.getPPDrawing()

    open var slideShow: SlideShow?
        /**
         * Fetch the SlideShow we're attached to
         */
        get() = _slideShow
        /**
         * Set the SlideShow we're attached to.
         * Also passes it on to our child RichTextRuns
         */
        set(ss) {
            _slideShow = ss
            val trs = this.textRuns
            if (trs != null) {
                for (i in trs.indices) {
                    trs[i].supplySlideShow(_slideShow)
                }
            }
        }

    val shapes: Array<Shape>
        /**
         * Returns all shapes contained in this Sheet
         * 
         * @return all shapes contained in this Sheet (Slide or Notes)
         */
        get() {
            if (_shapes != null) {
                return _shapes!!
            }
            val ppdrawing: PPDrawing? = this.pPDrawing

            val dg =
                ppdrawing!!.escherRecords!![0] as EscherContainerRecord
            var spgr: EscherContainerRecord? = null

            run {
                val it: MutableIterator<EscherRecord?> = dg.childIterator
                while (it.hasNext()) {
                    val rec = it.next()
                    if (rec!!.recordId == EscherContainerRecord.SPGR_CONTAINER) {
                        spgr = rec as EscherContainerRecord
                        break
                    }
                }
            }
            checkNotNull(spgr) { "spgr not found" }

            val shapes: MutableList<Shape> =
                ArrayList<Shape>()
            val it: MutableIterator<EscherRecord?> = spgr!!.childIterator
            if (it.hasNext()) {
                // skip first item
                it.next()
            }
            while (it.hasNext()) {
                val sp = it.next() as EscherContainerRecord
                val sh = ShapeFactory.createShape(sp, null)
                sh.sheet = this
                shapes.add(sh)
            }
            val result = shapes.toTypedArray()
            _shapes = result
            return result
        }

    /**
     * Add a new Shape to this Slide
     * 
     * @param shape - the Shape to add
     */
    fun addShape(shape: Shape) {
        val ppdrawing: PPDrawing? = this.pPDrawing

        val dgContainer = ppdrawing!!.escherRecords!![0] as EscherContainerRecord
        val spgr = ShapeKit.getEscherChild(
            dgContainer,
            EscherContainerRecord.SPGR_CONTAINER.toInt()
        ) as EscherContainerRecord?
        spgr!!.addChildRecord(shape.spContainer)

        shape.sheet = this
        shape.shapeId = allocateShapeId()
        shape.afterInsert(this)
    }

    /**
     * Allocates new shape id for the new drawing group id.
     * 
     * @return a new shape id.
     */
    fun allocateShapeId(): Int {
        val dgg = _slideShow!!.documentRecord!!.pPDrawingGroup!!
            .escherDggRecord!!
        val dg = sheetContainer!!.getPPDrawing()!!.escherDgRecord!!

        dgg.numShapesSaved = dgg.numShapesSaved + 1

        // Add to existing cluster if space available
        for (i in dgg.fileIdClusters!!.indices) {
            val c = dgg.fileIdClusters!![i]
            if (c!!.drawingGroupId == dg.drawingGroupId.toInt() && c.numShapeIdsUsed != 1024) {
                val result = c.numShapeIdsUsed + (1024 * (i + 1))
                c.incrementShapeId()
                dg.numShapes = dg.numShapes + 1
                dg.lastMSOSPID = result
                if (result >= dgg.shapeIdMax) dgg.shapeIdMax = result + 1
                return result
            }
        }

        // Create new cluster
        dgg.addCluster(dg.drawingGroupId.toInt(), 0, false)
        dgg.fileIdClusters!![dgg.fileIdClusters!!.size - 1]!!.incrementShapeId()
        dg.numShapes = dg.numShapes + 1
        val result = (1024 * dgg.fileIdClusters!!.size)
        dg.lastMSOSPID = result
        if (result >= dgg.shapeIdMax) dgg.shapeIdMax = result + 1
        return result
    }

    /**
     * Removes the specified shape from this sheet.
     * 
     * @param shape shape to be removed from this sheet, if present.
     * @return <tt>true</tt> if the shape was deleted.
     */
    fun removeShape(shape: Shape): Boolean {
        val ppdrawing: PPDrawing? = this.pPDrawing

        val dg = ppdrawing!!.escherRecords!![0] as EscherContainerRecord
        var spgr: EscherContainerRecord? = null

        val it: MutableIterator<EscherRecord?> = dg.childIterator
        while (it.hasNext()) {
            val rec = it.next()
            if (rec!!.recordId == EscherContainerRecord.SPGR_CONTAINER) {
                spgr = rec as EscherContainerRecord
                break
            }
        }
        if (spgr == null) {
            return false
        }

        val lst = spgr.childRecords
        val sc = shape.spContainer
        val result = sc != null && lst.remove(sc)
        spgr.childRecords = lst
        return result
    }

    /**
     * Called by SlideShow ater a new sheet is created
     */
    open fun onCreate() {
    }

    /**
     * Return the master sheet .
     */
    abstract val masterSheet: MasterSheet?

    open val colorScheme: ColorSchemeAtom?
        /**
         * Color scheme for this sheet.
         */
        get() = sheetContainer!!.getColorScheme()

    open val background: Background?
        /**
         * Returns the background shape for this sheet.
         * 
         * @return the background shape for this sheet.
         */
        get() {
            if (_background == null) {
                val ppdrawing: PPDrawing? = this.pPDrawing

                val dg =
                    ppdrawing!!.escherRecords!![0] as EscherContainerRecord
                var spContainer: EscherContainerRecord? = null

                val it: MutableIterator<EscherRecord?> = dg.childIterator
                while (it.hasNext()) {
                    val rec = it.next()
                    if (rec!!.recordId == EscherContainerRecord.SP_CONTAINER) {
                        spContainer = rec as EscherContainerRecord
                        break
                    }
                }
                if (spContainer != null) {
                    _background = Background(spContainer, null)
                    _background!!.sheet = this
                }
            }
            return _background
        }

    /*public void draw(Graphics2D graphics)
    {

    }*/
    /**
     * Subclasses should call this method and update the array of text runs
     * when a text shape is added
     * 
     * @param shape
     */
    open fun onAddTextShape(shape: TextShape?) {
    }

    /**
     * Return placeholder by text type
     * 
     * @param type  type of text, See [TextHeaderAtom]
     * @return  `TextShape` or `null`
     */
    fun getPlaceholderByTextType(type: Int): TextShape? {
        val shape = this.shapes
        for (i in shape.indices) {
            if (shape[i] is TextShape) {
                val tx = shape[i] as TextShape
                val run = tx.textRun
                if (run != null && run.runType == type) {
                    return tx
                }
            }
        }
        return null
    }

    /**
     * Search text placeholer by its type
     * 
     * @param type  type of placeholder to search. See [OEPlaceholderAtom]
     * @return  `TextShape` or `null`
     */
    fun getPlaceholder(type: Int): TextShape? {
        val shape = this.shapes
        for (i in shape.indices) {
            if (shape[i] is TextShape) {
                val tx = shape[i] as TextShape
                var placeholderId = 0
                val oep = tx.placeholderAtom
                if (oep != null) {
                    placeholderId = oep.placeholderId
                } else {
                    //special case for files saved in Office 2007
                    val hldr = tx
                        .getClientDataRecord(RecordTypes.RoundTripHFPlaceholder12.typeID) as RoundTripHFPlaceholder12?
                    if (hldr != null) placeholderId = hldr.placeholderId
                }
                if (placeholderId == type) {
                    return tx
                }
            }
        }
        return null
    }

    val programmableTag: String?
        /**
         * Return programmable tag associated with this sheet, e.g. `___PPT12`.
         * 
         * @return programmable tag associated with this sheet.
         */
        get() {
            var tag: String? = null
            val progTags = this.sheetContainer!!.findFirstOfType(
                RecordTypes.SlideProgTagsContainer.typeID.toLong()
            ) as RecordContainer?
            if (progTags != null) {
                val progBinaryTag = progTags
                    .findFirstOfType(RecordTypes.SlideProgBinaryTagContainer.typeID.toLong()) as RecordContainer?
                if (progBinaryTag != null) {
                    val binaryTag = progBinaryTag
                        .findFirstOfType(RecordTypes.CString.typeID.toLong()) as CString?
                    if (binaryTag != null) tag = binaryTag.text
                }
            }

            return tag
        }


    /**
     * 
     */
    open fun dispose() {
        _slideShow = null
        if (_background != null) {
            _background!!.dispose()
            _background = null
        }
        if (_shapes != null) {
            for (sp in _shapes!!) {
                sp.dispose()
            }
            _shapes = null
        }
        if (this.sheetContainer != null) {
            sheetContainer!!.dispose()
            this.sheetContainer = null
        }
    }

    /**
     * The `SlideShow` we belong to
     */
    private var _slideShow: SlideShow? = null

    /**
     * Sheet background
     */
    private var _background: Background? = null

    /**
     * Shape[]
     */
    private var _shapes: Array<Shape>? = null

    /**
     * Return record container for this sheet
     */

    companion object {
        /**
         * For a given PPDrawing, grab all the TextRuns
         */
        @JvmStatic
        fun findTextRuns(ppdrawing: PPDrawing): Array<TextRun> {
            val runsV: Vector<TextRun> = Vector<TextRun>()
            val wrappers = ppdrawing.textboxWrappers!!
            for (i in wrappers.indices) {
                val s1 = runsV.size

                // propagate parents to parent-aware records
                RecordContainer.handleParentAwareRecords(wrappers[i])
                Companion.findTextRuns(wrappers[i].getChildRecords(), runsV)
                val s2 = runsV.size
                if (s2 != s1) {
                    val t = runsV.get(runsV.size - 1)
                    t.shapeId = wrappers[i].shapeId
                    var find = false
                    for (j in i - 1 downTo 0) {
                        if (wrappers[j].shapeId == EscherBinaryTagRecord.RECORD_ID.toInt()) {
                            val records: Array<Record> = wrappers[j].getChildRecords()
                            for (n in records.indices) {
                                if (records[0] is ExtendedParagraphAtom) {
                                    find = true
                                    t.extendedParagraphAtom = records[n] as ExtendedParagraphAtom
                                    break
                                }
                            }
                        }
                        if (find) {
                            break
                        }
                    }
                }
            }
            return runsV.toTypedArray()
        }

        /**
         * Scans through the supplied record array, looking for
         * a TextHeaderAtom followed by one of a TextBytesAtom or
         * a TextCharsAtom. Builds up TextRuns from these
         * 
         * @param records the records to build from
         * @param found   vector to add any found to
         */
        @JvmStatic
        protected fun findTextRuns(records: Array<out Record?>, found: Vector<TextRun>) {
            // Look for a TextHeaderAtom
            var i = 0
            var slwtIndex = 0
            while (i < (records.size - 1)) {
                if (records[i] is TextHeaderAtom) {
                    var trun: TextRun? = null
                    val tha = records[i] as TextHeaderAtom?
                    var stpa: StyleTextPropAtom? = null

                    // Look for a subsequent StyleTextPropAtom
                    if (i < (records.size - 2)) {
                        if (records[i + 2] is StyleTextPropAtom) {
                            stpa = records[i + 2] as StyleTextPropAtom?
                        }
                    }

                    // See what follows the TextHeaderAtom
                    if (records[i + 1] is TextCharsAtom) {
                        val tca = records[i + 1] as TextCharsAtom?
                        trun = TextRun(tha, tca, stpa)
                    } else if (records[i + 1] is TextBytesAtom) {
                        val tba = records[i + 1] as TextBytesAtom?
                        trun = TextRun(tha, tba, stpa)
                    } else if (records[i + 1]!!.getRecordType() == 4001L) {
                        // StyleTextPropAtom - Safe to ignore
                    } else if (records[i + 1]!!.getRecordType() == 4010L) {
                        // TextSpecInfoAtom - Safe to ignore
                    } else {
                        System.err
                            .println(
                                "Found a TextHeaderAtom not followed by a TextBytesAtom or TextCharsAtom: Followed by "
                                        + records[i + 1]!!.getRecordType()
                            )
                    }

                    if (trun != null) {
                        val lst: ArrayList<Record?> = ArrayList<Record?>()
                        for (j in i..<records.size) {
                            if (j > i && records[j] is TextHeaderAtom) break
                            lst.add(records[j])
                        }
                        val recs = lst.toTypedArray()
                        trun.records = recs
                        trun.index = slwtIndex

                        found.add(trun)
                        i++
                    } else {
                        // Not a valid one, so skip on to next and look again
                    }
                    slwtIndex++
                }
                i++
            }
        }
    }
}
