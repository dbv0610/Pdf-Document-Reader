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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hslf.record.ColorSchemeAtom
import com.wxiwei.office.fc.hslf.record.Comment2000
import com.wxiwei.office.fc.hslf.record.ExtendedPresRuleContainer.ExtendedParaAtomsSet
import com.wxiwei.office.fc.hslf.record.RecordContainer
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.record.SlideListWithText.SlideAtomsSet
import com.wxiwei.office.fc.hslf.record.SlideProgTagsContainer
import com.wxiwei.office.fc.hslf.record.SlideShowSlideInfoAtom
import com.wxiwei.office.fc.hslf.record.TextHeaderAtom
import com.wxiwei.office.java.awt.Rectangle
import java.util.Vector

/**
 * This class represents a slide in a PowerPoint Document. It allows
 * access to the text within, and the layout. For now, it only does
 * the text side of things though
 * 
 * @author Nick Burch
 * @author Yegor Kozlov
 */
class Slide : Sheet {
    /**
     * Constructs a Slide from the Slide record, and the SlideAtomsSet
     * containing the text.
     * Initialises TextRuns, to provide easier access to the text
     * 
     * @param slide the Slide record we're based on
     * @param notes the Notes sheet attached to us
     * @param atomSet the SlideAtomsSet to get the text from
     */
    constructor(
        slide: com.wxiwei.office.fc.hslf.record.Slide?, notes: Notes?, atomSet: SlideAtomsSet?,
        extendedAtomsSets: Array<ExtendedParaAtomsSet>?, slideIdentifier: Int, slideNumber: Int
    ) : super(slide, slideIdentifier) {
        this.notesSheet = notes
        this.slideAtomsSet = atomSet
        this.slideNumber = slideNumber
        _extendedAtomsSets = extendedAtomsSets


        // Grab the TextRuns from the PPDrawing
        val _otherRuns: Array<TextRun?> = findTextRuns(pPDrawing!!).map { it as TextRun? }.toTypedArray()

        // For the text coming in from the SlideAtomsSet:
        // Build up TextRuns from pairs of TextHeaderAtom and
        //  one of TextBytesAtom or TextCharsAtom
        val textRuns: Vector<TextRun> = Vector<TextRun>()
        if (this.slideAtomsSet != null) {
            Sheet.findTextRuns(slideAtomsSet!!.slideRecords!!, textRuns)
        } else {
            // No text on the slide, must just be pictures
        }

        // Build an array, more useful than a vector
        val runs = arrayOfNulls<TextRun>(textRuns.size + _otherRuns.size)
        // Grab text from SlideListWithTexts entries
        var i = 0
        i = 0
        while (i < textRuns.size) {
            runs[i] = textRuns.get(i)
            runs[i]!!.sheet = this
            i++
        }
        // Grab text from slide's PPDrawing
        var k = 0
        while (k < _otherRuns.size) {
            runs[i] = _otherRuns[k]!!
            runs[i]!!.sheet = this
            i++
            k++
        }
        _runs = Array(runs.size) { runs[it]!! }

        val extSets = _extendedAtomsSets
        if (extSets != null) {
            i = 0
            while (i < _runs!!.size) {
                if (_runs!![i].extendedParagraphAtom == null) {
                    val type = _runs!![i].runType
                    for (j in extSets.indices) {
                        val paraHeaderAtom = extSets[j].extendedParaHeaderAtom
                        if (paraHeaderAtom != null && paraHeaderAtom.textType == type) {
                            _runs!![i].extendedParagraphAtom = extSets[j].extendedParaAtom
                            break
                        }
                    }
                }
                i++
            }
        }
    }

    /**
     * Create a new Slide instance
     * @param sheetNumber The internal number of the sheet, as used by PersistPtrHolder
     * @param slideNumber The user facing number of the sheet
     */
    constructor(
        sheetNumber: Int,
        sheetRefId: Int,
        slideNumber: Int
    ) : super(com.wxiwei.office.fc.hslf.record.Slide(), sheetNumber) {
        this.slideNumber = slideNumber
        sheetContainer!!.sheetId = sheetRefId
    }

    /**
     * Sets the Notes that are associated with this. Updates the
     * references in the records to point to the new ID
     */
    fun setNotes(notes: Notes?) {
        this.notesSheet = notes

        // Update the Slide Atom's ID of where to point to
        val sa = this.slideRecord!!.slideAtom

        if (notes == null) {
            // Set to 0
            sa!!.notesID = 0
        } else {
            // Set to the value from the notes' sheet id
            sa!!.notesID = notes._getSheetNumber()
        }
    }

    /**
     * Called by SlideShow ater a new slide is created.
     * 
     * 
     * For Slide we need to do the following:
     *  *  set id of the drawing group.
     *  *  set shapeId for the container descriptor and background
     * 
     */
    override fun onCreate() {
        //initialize drawing group id
        val dgg = slideShow!!.documentRecord!!.pPDrawingGroup!!
            .escherDggRecord!!
        val dgContainer = sheetContainer!!
            .getPPDrawing()!!.escherRecords!![0] as EscherContainerRecord
        val dg = ShapeKit.getEscherChild(
            dgContainer,
            EscherDgRecord.RECORD_ID.toInt()
        ) as EscherDgRecord?
        val dgId = dgg.maxDrawingGroupId + 1
        dg!!.options = (dgId shl 4).toShort()
        dgg.drawingsSaved = dgg.drawingsSaved + 1
        dgg.maxDrawingGroupId = dgId

        for (c in dgContainer.childContainers) {
            var spr: EscherSpRecord? = null
            when (c!!.recordId) {
                EscherContainerRecord.SPGR_CONTAINER -> {
                    val dc = c.getChild(0) as EscherContainerRecord?
                    spr = dc!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
                }

                EscherContainerRecord.SP_CONTAINER -> spr =
                    c.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            }
            if (spr != null) spr.shapeId = allocateShapeId()
        }

        //PPT doen't increment the number of saved shapes for group descriptor and background
        dg.numShapes = 1
    }

    /**
     * Create a `TextBox` object that represents the slide's title.
     * 
     * @return `TextBox` object that represents the slide's title.
     */
    fun addTitle(): TextBox {
        val pl = Placeholder()
        pl.shapeType = ShapeTypes.Rectangle
        pl.textRun!!.runType = TextHeaderAtom.Companion.TITLE_TYPE
        pl.text = "Click to edit title"
        pl.setAnchor(Rectangle(54, 48, 612, 90))
        addShape(pl)
        return pl
    }

    // Complex Accesser methods follow
    val title: String?
        /**
         * Return title of this slide or `null` if the slide does not have title.
         * 
         * 
         * The title is a run of text of type `TextHeaderAtom.CENTER_TITLE_TYPE` or
         * `TextHeaderAtom.TITLE_TYPE`
         * 
         * 
         * @see TextHeaderAtom
         * 
         * 
         * @return title of this slide
         */
        get() {
            val txt = textRuns!!
            for (i in txt.indices) {
                val type = txt[i].runType
                if (type == TextHeaderAtom.Companion.CENTER_TITLE_TYPE || type == TextHeaderAtom.Companion.TITLE_TYPE) {
                    val title = txt[i].text
                    return title
                }
            }
            return null
        }

    // Simple Accesser methods follow
    /**
     * Returns an array of all the TextRuns found
     */
    override val textRuns: Array<TextRun>?
        get() = _runs

    val slideRecord: com.wxiwei.office.fc.hslf.record.Slide?
        /**
         * Returns the underlying slide record
         */
        get() = sheetContainer as com.wxiwei.office.fc.hslf.record.Slide?

    /**
     * Returns master sheet associated with this slide.
     * It can be either SlideMaster or TitleMaster objects.
     * 
     * @return the master sheet associated with this slide.
     */
    override val masterSheet: MasterSheet?
        get() {
        val master = slideShow!!.slidesMasters!!
        val sa = this.slideRecord!!.slideAtom
        val masterId = sa!!.masterID
        var sheet: MasterSheet? = null
        for (i in master.indices) {
            if (masterId == master[i]?._getSheetNumber()) {
                sheet = master[i]
                break
            }
        }
        if (sheet == null) {
            val titleMaster = slideShow!!.titleMasters
            if (titleMaster != null) for (i in titleMaster.indices) {
                if (masterId == titleMaster[i]?._getSheetNumber()) {
                    sheet = titleMaster[i]
                    break
                }
            }
        }
        return sheet
        }

    /**
     * Change Master of this slide.
     */
    fun setMasterSheet(master: MasterSheet) {
        val sa = this.slideRecord!!.slideAtom
        val sheetNo = master._getSheetNumber()
        sa!!.masterID = sheetNo
    }

    val slideHeadersFooters: HeadersFooters?
        get() {
            val container = this.slideRecord!!.headersFootersContainer
            if (container != null) {
                return HeadersFooters(container, this, false, false)
            }
            return null
        }

    var followMasterBackground: Boolean
        /**
         * Whether this slide follows master sheet background
         * 
         * @return `true` if the slide follows master background,
         * `false` otherwise
         */
        get() {
            val sa = this.slideRecord!!.slideAtom
            return sa!!.followMasterBackground
        }
        /**
         * Sets whether this slide follows master background
         * 
         * @param flag  `true` if the slide follows master,
         * `false` otherwise
         */
        set(flag) {
            val sa = this.slideRecord!!.slideAtom
            sa!!.followMasterBackground = flag
        }

    var followMasterScheme: Boolean
        /**
         * Whether this slide follows master color scheme
         * 
         * @return `true` if the slide follows master color scheme,
         * `false` otherwise
         */
        get() {
            val sa = this.slideRecord!!.slideAtom
            return sa!!.followMasterScheme
        }
        /**
         * Sets whether this slide draws master color scheme
         * 
         * @param flag  `true` if the slide draws master color scheme,
         * `false` otherwise
         */
        set(flag) {
            val sa = this.slideRecord!!.slideAtom
            sa!!.followMasterScheme = flag
        }

    var followMasterObjects: Boolean
        /**
         * Whether this slide draws master sheet objects
         * 
         * @return `true` if the slide draws master sheet objects,
         * `false` otherwise
         */
        get() {
            val sa = this.slideRecord!!.slideAtom
            return sa!!.followMasterObjects
        }
        /**
         * Sets whether this slide draws master sheet objects
         * 
         * @param flag  `true` if the slide draws master sheet objects,
         * `false` otherwise
         */
        set(flag) {
            val sa = this.slideRecord!!.slideAtom
            sa!!.followMasterObjects = flag
        }

    /**
     * Background for this slide.
     */
    override val background: Background?
        get() {
            if (this.followMasterBackground) {
                return masterSheet!!.background
            }
            return super.background
        }

    /**
     * Color scheme for this slide.
     */
    override val colorScheme: ColorSchemeAtom?
        get() {
            if (this.followMasterScheme) {
                return masterSheet!!.colorScheme
            }
            return super.colorScheme
        }

    val comments: Array<Comment?>
        /**
         * Get the comment(s) for this slide.
         * Note - for now, only works on PPT 2000 and
         * PPT 2003 files. Doesn't work for PPT 97
         * ones, as they do their comments oddly.
         */
        get() {
            // If there are any, they're in
            //  ProgTags -> ProgBinaryTag -> BinaryTagData
            val progTags = sheetContainer!!.findFirstOfType(
                RecordTypes.SlideProgTagsContainer.typeID.toLong()
            ) as RecordContainer?
            if (progTags != null) {
                val progBinaryTag = progTags
                    .findFirstOfType(RecordTypes.SlideProgBinaryTagContainer.typeID.toLong()) as RecordContainer?
                if (progBinaryTag != null) {
                    val binaryTags = progBinaryTag
                        .findFirstOfType(RecordTypes.BinaryTagDataBlob.typeID.toLong()) as RecordContainer?
                    if (binaryTags != null) {
                        // This is where they'll be
                        var count = 0
                        for (i in binaryTags.getChildRecords().indices) {
                            if (binaryTags.getChildRecords()[i] is Comment2000) {
                                count++
                            }
                        }

                        // Now build
                        val comments =
                            arrayOfNulls<Comment>(count)
                        count = 0
                        for (i in binaryTags.getChildRecords().indices) {
                            if (binaryTags.getChildRecords()[i] is Comment2000) {
                                comments[i] =
                                    Comment(binaryTags.getChildRecords()[i] as Comment2000)
                                count++
                            }
                        }

                        return comments
                    }
                }
            }

            // None found
            return arrayOfNulls<Comment>(0)
        }
    
    override fun onAddTextShape(shape: TextShape?) {
        val run = shape!!.textRun

        if (_runs == null) _runs = arrayOf<TextRun>(run!!)
        else {
            _runs = _runs!! + run!!
        }
    }

    fun setExtendedAtom(extendAtomsSets: Array<ExtendedParaAtomsSet>?) {
        this._extendedAtomsSets = extendAtomsSets
    }

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        if (this.slideAtomsSet != null) {
            slideAtomsSet!!.dispose()
            this.slideAtomsSet = null
        }
        if (_runs != null) {
            for (tr in _runs!!) {
                tr.dispose()
            }
            _runs = null
        }
        if (this.notesSheet != null) {
            notesSheet!!.dispose()
            this.notesSheet = null
        }

        if (_extendedAtomsSets != null) {
            for (eps in _extendedAtomsSets!!) {
                eps.dispose()
            }
            _extendedAtomsSets = null
        }

        if (this.slideShowSlideInfoAtom != null) {
            slideShowSlideInfoAtom!!.dispose()
            this.slideShowSlideInfoAtom = null
        }

        if (this.slideProgTagsContainer != null) {
            slideProgTagsContainer!!.dispose()
            this.slideProgTagsContainer = null
        }
    }


    /**
     * Returns the (public facing) page number of this slide
     */
    /**
     * Changes the Slide's (external facing) page number.
     * @see com.wxiwei.office.fc.hslf.usermodel.SlideShow.reorderSlide
     */
    var slideNumber: Int

    /**
     * @return set of records inside `SlideListWithtext` container
     * which hold text data for this slide (typically for placeholders).
     */
    protected var slideAtomsSet: SlideAtomsSet? = null
        private set
    private var _runs: Array<TextRun>? = null

    /**
     * Returns the Notes Sheet for this slide, or null if there isn't one
     */
    var notesSheet: Notes? = null // usermodel needs to set this
        private set
    private var _extendedAtomsSets: Array<ExtendedParaAtomsSet>? = null

    /**
     * get slide transition atom
     * @return
     */
    /**
     * set slide transition atom
     * @param ssSlideInfoAtom
     */
    //slide transtion
    var slideShowSlideInfoAtom: SlideShowSlideInfoAtom? = null
    /**
     * get slide animation container
     * @return
     */
    /**
     * set slide animation container
     * @param propTagsContainer
     */
    //slide animation
    var slideProgTagsContainer: SlideProgTagsContainer? = null
}
