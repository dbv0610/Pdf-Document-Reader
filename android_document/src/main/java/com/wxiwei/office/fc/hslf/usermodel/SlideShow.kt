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
package com.wxiwei.office.fc.hslf.usermodel

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.hslf.HSLFSlideShow
import com.wxiwei.office.fc.hslf.exceptions.CorruptPowerPointFileException
import com.wxiwei.office.fc.hslf.model.HeadersFooters
import com.wxiwei.office.fc.hslf.model.Hyperlink
import com.wxiwei.office.fc.hslf.model.MovieShape
import com.wxiwei.office.fc.hslf.model.SlideMaster
import com.wxiwei.office.fc.hslf.model.TitleMaster
import com.wxiwei.office.fc.hslf.record.Document
import com.wxiwei.office.fc.hslf.record.ExAviMovie
import com.wxiwei.office.fc.hslf.record.ExControl
import com.wxiwei.office.fc.hslf.record.ExHyperlink
import com.wxiwei.office.fc.hslf.record.ExMCIMovie
import com.wxiwei.office.fc.hslf.record.ExObjList
import com.wxiwei.office.fc.hslf.record.ExOleObjAtom
import com.wxiwei.office.fc.hslf.record.ExtendedPresRuleContainer.ExtendedParaAtomsSet
import com.wxiwei.office.fc.hslf.record.FontCollection
import com.wxiwei.office.fc.hslf.record.HeadersFootersContainer
import com.wxiwei.office.fc.hslf.record.MainMaster
import com.wxiwei.office.fc.hslf.record.Notes
import com.wxiwei.office.fc.hslf.record.PersistPtrHolder
import com.wxiwei.office.fc.hslf.record.PositionDependentRecord
import com.wxiwei.office.fc.hslf.record.PositionDependentRecordContainer
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordContainer
import com.wxiwei.office.fc.hslf.record.RecordContainer.Companion.handleParentAwareRecords
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.record.Slide
import com.wxiwei.office.fc.hslf.record.SlideListWithText.SlideAtomsSet
import com.wxiwei.office.java.awt.Dimension
import java.io.IOException
import java.io.OutputStream
import java.util.Arrays
import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector
import kotlin.math.min

/**
 * This class is a friendly wrapper on top of the more scary HSLFSlideShow.
 * 
 * figure out how to match notes to their correct sheet (will involve
 * understanding DocSlideList and DocNotesList) - handle Slide creation cleaner
 * 
 * @author Nick Burch
 * @author Yegor kozlov
 */
class SlideShow
@JvmOverloads constructor(hslfSlideShow: HSLFSlideShow?, isGetThumbnail: Boolean = false) {
    private var _hslfSlideShow: HSLFSlideShow? = null
    private var _records: Array<Record>? = null
    @get:JvmName("getMostRecentCoreRecordsProperty")
    var mostRecentCoreRecords: Array<Record?>? = null
        private set
    fun getMostRecentCoreRecords(): Array<Record?>? = mostRecentCoreRecords
    private var _sheetIdToCoreRecordsLookup: Hashtable<Int?, Int?>? = null
    @get:JvmName("getDocumentRecordProperty")
    var documentRecord: Document? = null
        private set
    fun getDocumentRecord(): Document? = documentRecord
    @get:JvmName("getSlidesMastersProperty")
    var slidesMasters: Array<SlideMaster?>? = null
        private set
    fun getSlidesMasters(): Array<SlideMaster?>? = slidesMasters
    @get:JvmName("getTitleMastersProperty")
    var titleMasters: Array<TitleMaster?>? = null
        private set
    fun getTitleMasters(): Array<TitleMaster?>? = titleMasters
    @get:JvmName("getSlidesProperty")
    var slides: Array<com.wxiwei.office.fc.hslf.model.Slide?>? = null
        private set
    fun getSlides(): Array<com.wxiwei.office.fc.hslf.model.Slide?>? = slides
    @get:JvmName("getNotesProperty")
    var notes: Array<com.wxiwei.office.fc.hslf.model.Notes?>? = null
        private set
    fun getNotes(): Array<com.wxiwei.office.fc.hslf.model.Notes?>? = notes
    @get:JvmName("getFontCollectionProperty")
    var fontCollection: FontCollection? = null
        private set
    fun getFontCollection(): FontCollection? = fontCollection
    private var isGetThumbnail: Boolean = false
    /**
     * Use the PersistPtrHolder entries to figure out what is the "most recent"
     * version of all the core records (Document, Notes, Slide etc), and save a
     * record of them. Do this by walking from the oldest PersistPtr to the
     * newest, overwriting any references found along the way with newer ones
     */
    private fun findMostRecentCoreRecords() {
        // To start with, find the most recent in the byte offset domain
        val mostRecentByBytes = Hashtable<Int?, Int?>()
        for (i in _records!!.indices) {
            if (_records!![i] is PersistPtrHolder) {
                val pph = _records!![i] as PersistPtrHolder

                // If we've already seen any of the "slide" IDs for this
                // PersistPtr, remove their old positions
                val ids = pph.knownSlideIDs
                for (j in ids.indices) {
                    val id = ids[j]
                    if (mostRecentByBytes.containsKey(id)) {
                        mostRecentByBytes.remove(id)
                    }
                }

                // Now, update the byte level locations with their latest values
                val thisSetOfLocations = pph.slideLocationsLookup
                for (j in ids.indices) {
                    val id = ids[j]
                    mostRecentByBytes.put(id, thisSetOfLocations.get(id))
                }
            }
        }

        // We now know how many unique special records we have, so init
        // the array
        this.mostRecentCoreRecords = arrayOfNulls<Record>(mostRecentByBytes.size)

        // We'll also want to be able to turn the slide IDs into a position
        // in this array
        _sheetIdToCoreRecordsLookup = Hashtable<Int?, Int?>()
        val allIDs = IntArray(mostRecentCoreRecords!!.size)
        val ids: Enumeration<Int?> = mostRecentByBytes.keys()
        for (i in allIDs.indices) {
            val id = ids.nextElement()
            allIDs[i] = id!!
        }
        Arrays.sort(allIDs)
        for (i in allIDs.indices) {
            _sheetIdToCoreRecordsLookup!!.put(allIDs[i], i)
        }

        // Now convert the byte offsets back into record offsets
        for (i in _records!!.indices) {
            if (_records!![i] is PositionDependentRecord) {
                val pdr = _records!![i] as PositionDependentRecord
                val recordAt = pdr.lastOnDiskOffset

                // Is it one we care about?
                for (j in allIDs.indices) {
                    val thisID = allIDs[j]
                    val thatRecordAt = mostRecentByBytes.get(thisID)

                    if (thatRecordAt == recordAt) {
                        // Bingo. Now, where do we store it?
                        val storeAtI = _sheetIdToCoreRecordsLookup!![thisID]
                        val storeAt: Int = storeAtI!!

                        // Tell it its Sheet ID, if it cares
                        if (pdr is PositionDependentRecordContainer) {
                            val pdrc = _records!![i] as PositionDependentRecordContainer
                            pdrc.sheetId = thisID
                        }

                        // Finally, save the record
                        this.mostRecentCoreRecords!![storeAt] = _records!![i]
                    }
                }
            }
        }

        // Now look for the interesting records in there
        for (i in mostRecentCoreRecords!!.indices) {
            // Check there really is a record at this number
            if (this.mostRecentCoreRecords!![i] != null) {
                // Find the Document, and interesting things in it
                if (this.mostRecentCoreRecords!![i]!!.getRecordType() == RecordTypes.Document.typeID.toLong()) {
                    this.documentRecord = this.mostRecentCoreRecords!![i] as Document?
                    this.fontCollection = documentRecord!!.environment!!.fontCollection
                }
            } else {
                // No record at this number
                // Odd, but not normally a problem
            }
        }
    }

    /**
     * For a given SlideAtomsSet, return the core record, based on the refID
     * from the SlidePersistAtom
     */
    private fun getCoreRecordForSAS(sas: SlideAtomsSet): Record? {
        val spa = sas.slidePersistAtom
        val refID = spa!!.refID
        return getCoreRecordForRefID(refID)
    }

    /**
     * For a given refID (the internal, 0 based numbering scheme), return the
     * core record
     * 
     * @param refID
     * the refID
     */
    private fun getCoreRecordForRefID(refID: Int): Record? {
        val coreRecordId = _sheetIdToCoreRecordsLookup!!.get(refID)
        if (coreRecordId != null) {
            val r = this.mostRecentCoreRecords!![coreRecordId]
            return r
        }
        /*logger.log(POILogger.ERROR,
            "We tried to look up a reference to a core record, but there was no core ID for reference ID "
                + refID);*/
        return null
    }

    /**
     * Build up model level Slide and Notes objects, from the underlying
     * records.
     */
    private fun buildSlidesAndNotes() {
        // Ensure we really found a Document record earlier
        // If we didn't, then the file is probably corrupt
        if (this.documentRecord == null) {
            throw CorruptPowerPointFileException(
                "The PowerPoint file didn't contain a Document Record in its PersistPtr blocks. It is probably corrupt."
            )
        }

        // Fetch the SlideListWithTexts in the most up-to-date Document Record
        //
        // As far as we understand it:
        // * The first SlideListWithText will contain a SlideAtomsSet
        // for each of the master slides
        // * The second SlideListWithText will contain a SlideAtomsSet
        // for each of the slides, in their current order
        // These SlideAtomsSets will normally contain text
        // * The third SlideListWithText (if present), will contain a
        // SlideAtomsSet for each Notes
        // These SlideAtomsSets will not normally contain text
        //
        // Having indentified the masters, slides and notes + their orders,
        // we have to go and find their matching records
        // We always use the latest versions of these records, and use the
        // SlideAtom/NotesAtom to match them with the StyleAtomSet
        val masterSLWT = documentRecord!!.masterSlideListWithText
        val slidesSLWT = documentRecord!!.slideSlideListWithText
        val notesSLWT = documentRecord!!.notesSlideListWithText

        // Find master slides
        // These can be MainMaster records, but oddly they can also be
        // Slides or Notes, and possibly even other odd stuff....
        // About the only thing you can say is that the master details are in
        // the first SLWT.
        var masterSets: Array<SlideAtomsSet> = emptyArray()
        if (masterSLWT != null) {
            masterSets = masterSLWT.slideAtomsSets!!

            val mmr = ArrayList<SlideMaster>()
            val tmr = ArrayList<TitleMaster>()

            for (i in masterSets.indices) {
                val r = getCoreRecordForSAS(masterSets[i])
                val sas = masterSets[i]
                val sheetNo = sas.slidePersistAtom!!.slideIdentifier
                if (r is Slide) {
                    val master = TitleMaster(r, sheetNo)
                    master.slideShow = this
                    tmr.add(master)
                } else if (r is MainMaster) {
                    val master = SlideMaster(r, sheetNo)
                    master.slideShow = this
                    mmr.add(master)
                }
            }

            this.slidesMasters = mmr.toTypedArray()

            this.titleMasters = tmr.toTypedArray()
        }

        // Having sorted out the masters, that leaves the notes and slides

        // Start by finding the notes records to go with the entries in
        // notesSLWT
        val notesRecords: Array<Notes>
        var notesSets: Array<SlideAtomsSet> = emptyArray()
        val slideIdToNotes = Hashtable<Int?, Int?>()
        if (notesSLWT == null) {
            // None
            notesRecords = emptyArray()
        } else {
            // Match up the records and the SlideAtomSets
            notesSets = notesSLWT.slideAtomsSets!!
            val notesRecordsL = ArrayList<Notes>()
            for (i in notesSets.indices) {
                // Get the right core record
                val r = getCoreRecordForSAS(notesSets[i])

                // Ensure it really is a notes record
                if (r is Notes) {
                    val notesRecord = r
                    notesRecordsL.add(notesRecord)

                    // Record the match between slide id and these notes
                    val spa = notesSets[i].slidePersistAtom
                    val slideId = spa!!.slideIdentifier
                    slideIdToNotes.put(slideId, i)
                }
                /*else
                {
                    logger.log(POILogger.ERROR, "A Notes SlideAtomSet at " + i
                        + " said its record was at refID "
                        + notesSets[i].getSlidePersistAtom().getRefID()
                        + ", but that was actually a " + r);
                }*/
            }
            notesRecords = notesRecordsL.toTypedArray()
        }

        // Now, do the same thing for our slides
        val slidesRecords: Array<Slide?>?
        var slidesSets: Array<SlideAtomsSet> = emptyArray()
        if (slidesSLWT == null) {
            // None
            slidesRecords = arrayOfNulls<Slide>(0)
        } else {
            // Match up the records and the SlideAtomSets
            slidesSets = slidesSLWT.slideAtomsSets!!
            slidesRecords = arrayOfNulls<Slide>(slidesSets.size)
            for (i in slidesSets.indices) {
                // Get the right core record
                val r = getCoreRecordForSAS(slidesSets[i])

                // Ensure it really is a slide record
                if (r is Slide) {
                    slidesRecords[i] = r
                }
                /*else
                {
                    logger.log(POILogger.ERROR, "A Slide SlideAtomSet at " + i
                        + " said its record was at refID "
                        + slidesSets[i].getSlidePersistAtom().getRefID()
                        + ", but that was actually a " + r);
                }*/
            }
        }

        // Finally, generate model objects for everything
        // Notes first
        val notesArr = arrayOfNulls<com.wxiwei.office.fc.hslf.model.Notes>(
            if (isGetThumbnail) min(notesRecords.size, 1) else notesRecords.size
        )
        for (i in notesArr.indices) {
            notesArr[i] = com.wxiwei.office.fc.hslf.model.Notes(notesRecords[i])
            notesArr[i]!!.slideShow = this
        }
        this.notes = notesArr
        // Then slides
        var extendedParaAtomsSets: Array<ExtendedParaAtomsSet>? = null
        if (documentRecord!!.list != null) {
            val extendedPresRule = documentRecord!!.list!!.extendedPresRuleContainer
            if (extendedPresRule != null) {
                extendedParaAtomsSets = extendedPresRule.extendedParaAtomsSets
            }
        }
        this.slides =
            arrayOfNulls<com.wxiwei.office.fc.hslf.model.Slide>(if (isGetThumbnail) 1 else slidesRecords.size)
        for (i in slides!!.indices) {
            val sas = slidesSets[i]
            val slideIdentifier = sas.slidePersistAtom!!.slideIdentifier


            //
            val extendedSets = Vector<ExtendedParaAtomsSet?>()
            if (extendedParaAtomsSets != null) {
                for (j in extendedParaAtomsSets.indices) {
                    val paraHeaderAtom = extendedParaAtomsSets[j]!!.extendedParaHeaderAtom
                    if (paraHeaderAtom != null && paraHeaderAtom.refSlideID == slideIdentifier) {
                        extendedSets.add(extendedParaAtomsSets[j])
                    }
                }
            }
            var extendedAtoms: Array<ExtendedParaAtomsSet>? = null
            if (extendedSets.size > 0) {
                extendedAtoms = extendedSets.filterNotNull().toTypedArray()
            }

            // Do we have a notes for this?
            var notes: com.wxiwei.office.fc.hslf.model.Notes? = null
            // Slide.SlideAtom.notesId references the corresponding notes slide.
            // 0 if slide has no notes.
            val noteId = slidesRecords[i]!!.slideAtom!!.notesID
            if (noteId != 0) {
                val notesPos = slideIdToNotes.get(noteId)
                if (notesPos != null && this.notes != null && notesPos < this.notes!!.size) notes = this.notes!![notesPos]
                /*else
                    logger.log(POILogger.ERROR, "Notes not found for noteId=" + noteId);*/
            }

            // Now, build our slide
            this.slides!![i] = com.wxiwei.office.fc.hslf.model.Slide(
                slidesRecords[i],
                notes,
                sas,
                extendedAtoms,
                slideIdentifier,
                (i + 1)
            )
            this.slides!![i]?.slideShow = this


            //slide transition  and animation
            this.slides!![i]?.slideShowSlideInfoAtom = slidesRecords[i]!!.slideShowSlideInfoAtom
            this.slides!![i]?.slideProgTagsContainer = slidesRecords[i]!!.slideProgTagsContainer
        }
    }

    /**
     * Writes out the slideshow file the is represented by an instance of this
     * class
     * 
     * @param out
     * The OutputStream to write to.
     * @throws IOException
     * If there is an unexpected IOException from the passed in
     * OutputStream
     */
    @Throws(IOException::class)
    fun write(out: OutputStream?) {
        //_hslfSlideShow.write(out);
    }

    /*
     * ===============================================================
     *                         Accessor Code
     * ===============================================================
     */

    val pictureData: Array<PictureData?>?
        /**
         * Returns the data of all the pictures attached to the SlideShow
         */
        get() = _hslfSlideShow!!.pictures

    val embeddedObjects: Array<ObjectData?>?
        /**
         * Returns the data of all the embedded OLE object in the SlideShow
         */
        get() = _hslfSlideShow!!.embeddedObjects.map { it as ObjectData? }.toTypedArray()

    val soundData: Array<SoundData?>
        /**
         * Returns the data of all the embedded sounds in the SlideShow
         */
        get() = SoundData.find(this.documentRecord!!)

    var pageSize: Dimension
        /**
         * Return the current page size
         */
        get() {
            val docatom = documentRecord!!.documentAtom!!
            val pgx =
                (docatom.slideSizeX * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
            val pgy =
                (docatom.slideSizeY * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI).toInt()
            return Dimension(pgx, pgy)
        }
        /**
         * Change the current page size
         * 
         * @param pgsize
         * page size (in points)
         */
        set(pgsize) {
            val docatom = documentRecord!!.documentAtom!!
            docatom.slideSizeX = (pgsize.width * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toLong()
            docatom.slideSizeY = (pgsize.height * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toLong()
        }

    /*
     * ===============================================================
     * Re-ordering Code
     * ===============================================================
     */
    /**
     * Re-orders a slide, to a new position.
     * 
     * @param oldSlideNumber
     * The old slide number (1 based)
     * @param newSlideNumber
     * The new slide number (1 based)
     */
    fun reorderSlide(oldSlideNumber: Int, newSlideNumber: Int) {
        // Ensure these numbers are valid
        require(!(oldSlideNumber < 1 || newSlideNumber < 1)) { "Old and new slide numbers must be greater than 0" }
        require(!(oldSlideNumber > slides!!.size || newSlideNumber > slides!!.size)) {
            ("Old and new slide numbers must not exceed the number of slides (" + slides!!.size
                    + ")")
        }

        // The order of slides is defined by the order of slide atom sets in the
        // SlideListWithText container.
        val slwt = documentRecord!!.slideSlideListWithText
        val sas = slwt!!.slideAtomsSets

        val tmp: SlideAtomsSet = sas!![oldSlideNumber - 1]
        sas[oldSlideNumber - 1] = sas[newSlideNumber - 1]
        sas[newSlideNumber - 1] = tmp

        val lst = ArrayList<Record>()
        for (i in sas.indices) {
            if (sas[i].slidePersistAtom != null) {
                lst.add(sas[i].slidePersistAtom!!)
            }
            val r = sas[i].slideRecords
            if (r != null) {
                for (j in r.indices) {
                    lst.add(r[j])
                }
            }
            this.slides!![i]?.slideNumber = i + 1
        }
        val r = lst.toTypedArray()
        slwt.setChildRecord(r)
    }

    /**
     * Removes the slide at the given index (0-based).
     * 
     * 
     * Shifts any subsequent slides to the left (subtracts one from their slide
     * numbers).
     * 
     * 
     * @param index
     * the index of the slide to remove (0-based)
     * @return the slide that was removed from the slide show.
     */
    fun removeSlide(index: Int): com.wxiwei.office.fc.hslf.model.Slide? {
        val lastSlideIdx = slides!!.size - 1
        require(!(index < 0 || index > lastSlideIdx)) {
            ("Slide index (" + index + ") is out of range (0.."
                    + lastSlideIdx + ")")
        }

        val slwt = documentRecord!!.slideSlideListWithText
        val sas = slwt!!.slideAtomsSets

        var removedSlide: com.wxiwei.office.fc.hslf.model.Slide? = null
        val records = ArrayList<Record>()
        val sa = ArrayList<SlideAtomsSet>()
        val sl = ArrayList<com.wxiwei.office.fc.hslf.model.Slide?>()

        val nt = ArrayList<com.wxiwei.office.fc.hslf.model.Notes?>()
        if (this.notes != null) {
            for (notes in this.notes!!) nt.add(notes)
        }

        var i = 0
        var num = 0
        while (i < slides!!.size) {
            if (i != index) {
                sl.add(this.slides!![i])
                if (sas != null) sa.add(sas[i])
                this.slides!![i]?.slideNumber = num++
                if (sas != null && sas[i].slidePersistAtom != null) records.add(sas[i].slidePersistAtom!!)
                if (sas != null && sas[i].slideRecords != null) {
                    records.addAll(sas[i].slideRecords!!.toList())
                }
            } else {
                removedSlide = this.slides!![i]
                nt.remove(this.slides!![i]?.notesSheet)
            }
            i++
        }
        if (sa.size == 0) {
            documentRecord!!.removeSlideListWithText(slwt)
        } else {
            slwt.slideAtomsSets = sa.toTypedArray()
            slwt.setChildRecord(records.toTypedArray())
        }
        this.slides = sl.toTypedArray()

        // if the removed slide had notes - remove references to them too
        if (removedSlide != null) {
            val notesId = removedSlide.slideRecord!!.slideAtom!!.notesID
            if (notesId != 0) {
                val nslwt = documentRecord!!.notesSlideListWithText
                val noteRecords = ArrayList<Record>()
                val na = ArrayList<SlideAtomsSet>()
                if (nslwt?.slideAtomsSets != null) {
                    for (ns in nslwt.slideAtomsSets!!) {
                        if (ns.slidePersistAtom!!.slideIdentifier != notesId) {
                            na.add(ns)
                            if (ns.slidePersistAtom != null) noteRecords.add(ns.slidePersistAtom!!)
                            if (ns.slideRecords != null) noteRecords.addAll(ns.slideRecords!!.toList())
                        }
                    }
                }
                if (na.size == 0) {
                    if (nslwt != null) documentRecord!!.removeSlideListWithText(nslwt)
                } else {
                    nslwt?.slideAtomsSets = na.toTypedArray()
                    nslwt?.setChildRecord(noteRecords.toTypedArray())
                }
            }
        }
        this.notes = nt.toTypedArray()

        return removedSlide
    }

    /*
     * ===============================================================
     *  Addition Code
     * ===============================================================
     */
    val numberOfFonts: Int
        /**
         * get the number of fonts in the presentation
         * 
         * @return number of fonts
         */
        get() = this.documentRecord!!.environment!!.fontCollection!!.numberOfFonts

    val slideHeadersFooters: HeadersFooters
        /**
         * Return Header / Footer settings for slides
         * 
         * @return Header / Footer settings for slides
         */
        get() {
            // detect if this ppt was saved in Office2007
            val tag = this.slidesMasters!![0]!!.programmableTag
            val ppt2007 = "___PPT12" == tag

            var hdd: HeadersFootersContainer? = null
            val ch: Array<out Record?> =
                documentRecord!!.getChildRecords()
            for (i in ch.indices) {
                val rec = ch[i]
                if (rec is HeadersFootersContainer
                    && rec.options == HeadersFootersContainer.SlideHeadersFootersContainer.toInt()
                ) {
                    hdd = rec
                    break
                }
            }
            var newRecord = false
            if (hdd == null) {
                hdd =
                    HeadersFootersContainer(HeadersFootersContainer.SlideHeadersFootersContainer)
                newRecord = true
            }
            return HeadersFooters(hdd, this, newRecord, ppt2007)
        }

    val notesHeadersFooters: HeadersFooters
        /**
         * Return Header / Footer settings for notes
         * 
         * @return Header / Footer settings for notes
         */
        get() {
            // detect if this ppt was saved in Office2007
            val tag = this.slidesMasters!![0]!!.programmableTag
            val ppt2007 = "___PPT12" == tag

            var hdd: HeadersFootersContainer? = null
            val ch: Array<out Record?> =
                documentRecord!!.getChildRecords()
            for (i in ch.indices) {
                val rec = ch[i]
                if (rec is HeadersFootersContainer
                    && rec.options == HeadersFootersContainer.NotesHeadersFootersContainer.toInt()
                ) {
                    hdd = rec
                    break
                }
            }
            var newRecord = false
            if (hdd == null) {
                hdd =
                    HeadersFootersContainer(HeadersFootersContainer.NotesHeadersFootersContainer)
                newRecord = true
            }
            if (ppt2007 && notes != null && notes!!.size > 0) {
                return HeadersFooters(hdd, this.notes!![0], newRecord, ppt2007)
            }
            return HeadersFooters(hdd, this, newRecord, ppt2007)
        }

    /**
     * Add a movie in this presentation
     * 
     * @param path
     * the path or url to the movie
     * @return 0-based index of the movie
     */
    fun addMovie(path: String, type: Int): Int {
        var lst =
            documentRecord!!.findFirstOfType(RecordTypes.ExObjList.typeID.toLong()) as ExObjList?
        if (lst == null) {
            lst = ExObjList()
            documentRecord!!.addChildAfter(lst, documentRecord!!.documentAtom!!)
        }

        val objAtom = lst.exObjListAtom!!
        // increment the object ID seed
        val objectId = objAtom.objectIDSeed.toInt() + 1
        objAtom.setObjectIDSeed(objectId)
        val mci: ExMCIMovie?
        when (type) {
            MovieShape.MOVIE_MPEG -> mci = ExMCIMovie()
            MovieShape.MOVIE_AVI -> mci = ExAviMovie()
            else -> throw IllegalArgumentException("Unsupported Movie: " + type)
        }

        lst.appendChildRecord(mci)
        val exVideo = mci.exVideo!!
        exVideo.exMediaAtom!!.objectId = objectId
        exVideo.exMediaAtom!!.mask = 0xE80000
        exVideo.pathAtom!!.text = path
        return objectId
    }

    /**
     * Add a control in this presentation
     * 
     * @param name
     * name of the control, e.g. "Shockwave Flash Object"
     * @param progId
     * OLE Programmatic Identifier, e.g.
     * "ShockwaveFlash.ShockwaveFlash.9"
     * @return 0-based index of the control
     */
    fun addControl(name: String?, progId: String?): Int {
        var lst =
            documentRecord!!.findFirstOfType(RecordTypes.ExObjList.typeID.toLong()) as ExObjList?
        if (lst == null) {
            lst = ExObjList()
            documentRecord!!.addChildAfter(lst, documentRecord!!.documentAtom!!)
        }
        val objAtom = lst.exObjListAtom!!
        // increment the object ID seed
        val objectId = objAtom.objectIDSeed.toInt() + 1
        objAtom.setObjectIDSeed(objectId)
        val ctrl = ExControl()
        val oleObj = ctrl.exOleObjAtom!!
        oleObj.objID = objectId
        oleObj.drawAspect = ExOleObjAtom.DRAW_ASPECT_VISIBLE
        oleObj.type = ExOleObjAtom.TYPE_CONTROL
        oleObj.subType = ExOleObjAtom.SUBTYPE_DEFAULT

        ctrl.setProgId(progId.orEmpty())
        ctrl.setMenuName(name.orEmpty())
        ctrl.setClipboardName(name.orEmpty())
        lst.addChildAfter(ctrl, objAtom)

        return objectId
    }

    /**
     * Add a hyperlink to this presentation
     * 
     * @return 0-based index of the hyperlink
     */
    fun addHyperlink(link: Hyperlink): Int {
        var lst =
            documentRecord!!.findFirstOfType(RecordTypes.ExObjList.typeID.toLong()) as ExObjList?
        if (lst == null) {
            lst = ExObjList()
            documentRecord!!.addChildAfter(lst, documentRecord!!.documentAtom!!)
        }
        val objAtom = lst.exObjListAtom!!
        // increment the object ID seed
        val objectId = objAtom.objectIDSeed.toInt() + 1
        objAtom.setObjectIDSeed(objectId)

        val ctrl = ExHyperlink()
        val obj = ctrl.exHyperlinkAtom!!
        obj.number = objectId
        ctrl.linkURL = link.getAddress() ?: ""
        ctrl.linkTitle = link.getTitle() ?: ""
        lst.addChildAfter(ctrl, objAtom)
        link.id = objectId

        return objectId
    }

    val slideCount: Int
        /**
         * 得到slide张数
         */
        get() = slides!!.size

    /**
     * 得到指定的slide
     */
    fun getSlide(index: Int): com.wxiwei.office.fc.hslf.model.Slide? {
        if (index < 0 || index >= this.slideCount) {
            return null
        }
        return this.slides!![index]
    }

    /**
     * 
     */
    fun dispose() {
        if (_hslfSlideShow != null) {
            _hslfSlideShow!!.dispose()
            _hslfSlideShow = null
        }
        if (_records != null) {
            for (rd in _records!!) {
                rd.dispose()
            }
            _records = null
        }
        if (this.mostRecentCoreRecords != null) {
            for (rd in this.mostRecentCoreRecords!!) {
                rd?.dispose()
            }
            this.mostRecentCoreRecords = null
        }
        if (_sheetIdToCoreRecordsLookup != null) {
            _sheetIdToCoreRecordsLookup!!.clear()
            _sheetIdToCoreRecordsLookup = null
        }
        if (this.documentRecord != null) {
            documentRecord!!.dispose()
            this.documentRecord = null
        }

        if (this.slidesMasters != null) {
            for (sm in this.slidesMasters!!) {
                sm?.dispose()
            }
            this.slidesMasters = null
        }

        if (this.titleMasters != null) {
            for (tm in this.titleMasters!!) {
                tm?.dispose()
            }
            this.titleMasters = null
        }

        if (this.slides != null) {
            for (slide in this.slides!!) {
                slide?.dispose()
            }
            this.slides = null
        }

        if (this.notes != null) {
            for (note in this.notes!!) {
                note?.dispose()
            }
            this.notes = null
        }

        if (this.fontCollection != null) {
            fontCollection!!.dispose()
            this.fontCollection = null
        }
    }

    init {
        // Get useful things from our base slideshow
        _hslfSlideShow = hslfSlideShow
        _records = _hslfSlideShow!!.records?.filterNotNull()?.toTypedArray()
        this.isGetThumbnail = isGetThumbnail

        // Handle Parent-aware Records
        for (record in _records!!) {
            if (record is RecordContainer) {
                handleParentAwareRecords(record)
            }
        }

        // Find the versions of the core records we'll want to use
        findMostRecentCoreRecords()

        // Build up the model level Slides and Notes
        buildSlidesAndNotes()
    }
}
