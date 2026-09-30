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
package com.wxiwei.office.fc.hslf.record

/**
 * Master container for Document. There is one of these for every
 * slideshow, and it holds lots of definitions, and some summaries.
 * 
 * @author Nick Burch
 */
class Document protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    val masterSlideListWithText: SlideListWithText?
        /**
         * Returns the SlideListWithText that deals with the
         * Master Slides
         */
        get() {
            for (i in slideListWithTexts!!.indices) {
                if (this.slideListWithTexts!![i].instance == SlideListWithText.Companion.MASTER) {
                    return this.slideListWithTexts!![i]
                }
            }
            return null
        }

    val slideSlideListWithText: SlideListWithText?
        /**
         * Returns the SlideListWithText that deals with the
         * Slides, or null if there isn't one
         */
        get() {
            for (i in slideListWithTexts!!.indices) {
                if (this.slideListWithTexts!![i].instance == SlideListWithText.Companion.SLIDES) {
                    return this.slideListWithTexts!![i]
                }
            }
            return null
        }

    val notesSlideListWithText: SlideListWithText?
        /**
         * Returns the SlideListWithText that deals with the
         * notes, or null if there isn't one
         */
        get() {
            for (i in slideListWithTexts!!.indices) {
                if (this.slideListWithTexts!![i].instance == SlideListWithText.Companion.NOTES) {
                    return this.slideListWithTexts!![i]
                }
            }
            return null
        }

    /**
     * Adds a new SlideListWithText record, at the appropriate
     * point in the child records.
     */
    fun addSlideListWithText(slwt: SlideListWithText) {
        // The new SlideListWithText should go in
        //  just before the EndDocumentRecord
        val endDoc = _children[_children.size - 1]
        check(endDoc.getRecordType() == RecordTypes.EndDocument.typeID.toLong()) { "The last child record of a Document should be EndDocument, but it was " + endDoc }

        // Add in the record
        addChildBefore(slwt, endDoc)

        // Updated our cached list of SlideListWithText records
        this.slideListWithTexts = slideListWithTexts!! + slwt
    }

    fun removeSlideListWithText(slwt: SlideListWithText) {
        val lst = ArrayList<SlideListWithText>()
        for (s in this.slideListWithTexts!!) {
            if (s != slwt) lst.add(s)
            else {
                removeChild(slwt)
            }
        }
        this.slideListWithTexts = lst.toTypedArray()
    }

    /**
     * We are of type 1000
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    override fun dispose() {
        _header = null
        if (documentAtom != null) {
            documentAtom!!.dispose()
            documentAtom = null
        }
        if (environment != null) {
            environment!!.dispose()
            environment = null
        }
        if (this.pPDrawingGroup != null) {
            pPDrawingGroup!!.dispose()
            this.pPDrawingGroup = null
        }
        val slwts = this.slideListWithTexts
        if (slwts != null) {
            for (swt in slwts!!) {
                swt.dispose()
            }
            this.slideListWithTexts = null
        }
        if (exObjList != null) {
            exObjList!!.dispose()
            exObjList = null
        }
        if (list != null) {
            list!!.dispose()
            list = null
        }
    }


    private var _header: ByteArray?

    /**
     * Returns the DocumentAtom of this Document
     */
    // Links to our more interesting children
    var documentAtom: DocumentAtom?
        private set

    /**
     * Returns the Environment of this Notes, which lots of
     * settings for the document in it
     */
    var environment: Environment? = null
        private set

    /**
     * Returns the PPDrawingGroup, which holds an Escher Structure
     * that contains information on pictures in the slides.
     */
    var pPDrawingGroup: PPDrawingGroup? = null
        private set

    /**
     * Returns all the SlideListWithTexts that are defined for
     * this Document. They hold the text, and some of the text
     * properties, which are referred to by the slides.
     * This will normally return an array of size 2 or 3
     */
    var slideListWithTexts: Array<SlideListWithText>?
        private set

    /**
     * Returns the ExObjList, which holds the references to
     * external objects used in the slides. This may be null, if
     * there are no external references.
     */
    var exObjList: ExObjList? = null // Can be null
        private set

    /**
     * extend paragraph attribute
     * @return
     */
    var list: List? = null
        private set

    /**
     * Set things up, and find our more interesting children
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)

        // Our first one should be a document atom
        check(_children[0] is DocumentAtom) { "The first child of a Document must be a DocumentAtom" }
        documentAtom = _children[0] as DocumentAtom

        // Find how many SlideListWithTexts we have
        // Also, grab the Environment and PPDrawing records
        //  on our way past
        var slwtcount = 0
        for (i in 1..<_children.size) {
            if (_children[i] is SlideListWithText) {
                slwtcount++
            } else if (_children[i] is Environment) {
                environment = _children[i] as Environment
            } else if (_children[i] is PPDrawingGroup) {
                this.pPDrawingGroup = _children[i] as PPDrawingGroup
            } else if (_children[i] is ExObjList) {
                exObjList = _children[i] as ExObjList
            } else if (_children[i] is List) {
                list = _children[i] as List
            }
        }

        // You should only every have 1, 2 or 3 SLWTs
        //  (normally it's 2, or 3 if you have notes)
        // Complain if it's not
        /*if(slwtcount == 0) {
        	logger.log(POILogger.WARN, "No SlideListWithText's found - there should normally be at least one!");
        }
        if(slwtcount > 3) {
        	logger.log(POILogger.WARN, "Found " + slwtcount + " SlideListWithTexts - normally there should only be three!");
        }*/

        // Now grab all the SLWTs
        val slwtList = ArrayList<SlideListWithText>(slwtcount)
        for (i in 1..<_children.size) {
            if (_children[i] is SlideListWithText) {
                slwtList.add(_children[i] as SlideListWithText)
            }
        }
        this.slideListWithTexts = slwtList.toTypedArray()
    }

    companion object {
        private const val _type: Long = 1000
    }
}
