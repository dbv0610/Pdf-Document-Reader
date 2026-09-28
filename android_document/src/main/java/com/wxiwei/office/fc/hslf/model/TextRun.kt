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

import com.wxiwei.office.fc.hslf.model.textproperties.TextPropCollection
import com.wxiwei.office.fc.hslf.record.ExtendedParagraphAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.StyleTextPropAtom
import com.wxiwei.office.fc.hslf.record.TextBytesAtom
import com.wxiwei.office.fc.hslf.record.TextCharsAtom
import com.wxiwei.office.fc.hslf.record.TextHeaderAtom
import com.wxiwei.office.fc.hslf.record.TextRulerAtom
import com.wxiwei.office.fc.hslf.record.TextSpecInfoAtom
import com.wxiwei.office.fc.hslf.usermodel.RichTextRun
import com.wxiwei.office.fc.hslf.usermodel.SlideShow
import com.wxiwei.office.fc.util.StringUtil.hasMultibyte
import com.wxiwei.office.fc.util.StringUtil.putCompressedUnicode
import java.util.LinkedList
import java.util.Vector

/**
 * This class represents a run of text in a powerpoint document. That
 * run could be text on a sheet, or text in a note.
 * It is only a very basic class for now
 * 
 * @author Nick Burch
 */
class TextRun
private constructor(// Note: These fields are protected to help with unit testing
    //   Other classes shouldn't really go playing with them!
    protected var _headerAtom: TextHeaderAtom?,
    tba: TextBytesAtom?,
    tca: TextCharsAtom?,
    protected var _styleAtom: StyleTextPropAtom?
) {
    /**
     * Returns records that make up this text run
     * 
     * @return text run records
     */
    /**
     * all text run records that follow TextHeaderAtom.
     * (there can be misc InteractiveInfo, TxInteractiveInfo and other records)
     */
    @get:JvmName("getRecordsProperty")
    var records: Array<Record?>? = null

    /**
     * Constructs a Text Run from a Unicode text block
     * 
     * @param tha the TextHeaderAtom that defines what's what
     * @param tca the TextCharsAtom containing the text
     * @param sta the StyleTextPropAtom which defines the character stylings
     */
    constructor(tha: TextHeaderAtom?, tca: TextCharsAtom?, sta: StyleTextPropAtom?) : this(
        tha,
        null,
        tca,
        sta
    )

    /**
     * Constructs a Text Run from a Ascii text block
     * 
     * @param tha the TextHeaderAtom that defines what's what
     * @param tba the TextBytesAtom containing the text
     * @param sta the StyleTextPropAtom which defines the character stylings
     */
    constructor(tha: TextHeaderAtom?, tba: TextBytesAtom?, sta: StyleTextPropAtom?) : this(
        tha,
        tba,
        null,
        sta
    )

    fun buildRichTextRuns(pStyles: LinkedList<*>, cStyles: LinkedList<*>, runRawText: String) {
        // Handle case of no current style, with a default

        if (pStyles.size == 0 || cStyles.size == 0) {
            this.richTextRuns = arrayOf(RichTextRun(this, 0, runRawText.length))
        } else {
            // Build up Rich Text Runs, one for each
            //  character/paragraph style pair
            val rtrs: Vector<RichTextRun> = Vector<RichTextRun>()

            var pos = 0

            var curP = 0
            var curC = 0
            var pLenRemain = -1
            var cLenRemain = -1

            // Build one for each run with the same style
            while (pos <= runRawText.length && curP < pStyles.size && curC < cStyles.size) {
                // Get the Props to use
                val pProps = pStyles.get(curP) as TextPropCollection
                val cProps = cStyles.get(curC) as TextPropCollection

                val pLen = pProps.charactersCovered
                val cLen = cProps.charactersCovered

                // Handle new pass
                var freshSet = false
                if (pLenRemain == -1 && cLenRemain == -1) {
                    freshSet = true
                }
                if (pLenRemain == -1) {
                    pLenRemain = pLen
                }
                if (cLenRemain == -1) {
                    cLenRemain = cLen
                }

                // So we know how to build the eventual run
                var runLen = -1
                var pShared = false
                var cShared = false

                // Same size, new styles - neither shared
                if (pLen == cLen && freshSet) {
                    runLen = cLen
                    pShared = false
                    cShared = false
                    curP++
                    curC++
                    pLenRemain = -1
                    cLenRemain = -1
                } else {
                    // Some sharing

                    // See if we are already in a shared block

                    if (pLenRemain < pLen) {
                        // Existing shared p block
                        pShared = true

                        // Do we end with the c block, or either side of it?
                        if (pLenRemain == cLenRemain) {
                            // We end at the same time
                            cShared = false
                            runLen = pLenRemain
                            curP++
                            curC++
                            pLenRemain = -1
                            cLenRemain = -1
                        } else if (pLenRemain < cLenRemain) {
                            // We end before the c block
                            cShared = true
                            runLen = pLenRemain
                            curP++
                            cLenRemain -= pLenRemain
                            pLenRemain = -1
                        } else {
                            // We end after the c block
                            cShared = false
                            runLen = cLenRemain
                            curC++
                            pLenRemain -= cLenRemain
                            cLenRemain = -1
                        }
                    } else if (cLenRemain < cLen) {
                        // Existing shared c block
                        cShared = true

                        // Do we end with the p block, or either side of it?
                        if (pLenRemain == cLenRemain) {
                            // We end at the same time
                            pShared = false
                            runLen = cLenRemain
                            curP++
                            curC++
                            pLenRemain = -1
                            cLenRemain = -1
                        } else if (cLenRemain < pLenRemain) {
                            // We end before the p block
                            pShared = true
                            runLen = cLenRemain
                            curC++
                            pLenRemain -= cLenRemain
                            cLenRemain = -1
                        } else {
                            // We end after the p block
                            pShared = false
                            runLen = pLenRemain
                            curP++
                            cLenRemain -= pLenRemain
                            pLenRemain = -1
                        }
                    } else {
                        // Start of a shared block
                        if (pLenRemain < cLenRemain) {
                            // Shared c block
                            pShared = false
                            cShared = true
                            runLen = pLenRemain
                            curP++
                            cLenRemain -= pLenRemain
                            pLenRemain = -1
                        } else {
                            // Shared p block
                            pShared = true
                            cShared = false
                            runLen = cLenRemain
                            curC++
                            pLenRemain -= cLenRemain
                            cLenRemain = -1
                        }
                    }
                }

                // Wind on
                val prevPos = pos
                pos += runLen
                // Adjust for end-of-run extra 1 length
                if (pos > runRawText.length) {
                    runLen--
                }

                // Save
                val rtr = RichTextRun(
                    this, prevPos, runLen, pProps, cProps, pShared,
                    cShared
                )
                rtrs.add(rtr)
            }

            // Build the array
            this.richTextRuns = rtrs.toTypedArray()
        }
    }

    // Update methods follow
    /**
     * Adds the supplied text onto the end of the TextRun,
     * creating a new RichTextRun (returned) for it to
     * sit in.
     * In many cases, before calling this, you'll want to add
     * a newline onto the end of your last RichTextRun
     */
    fun appendText(s: String): RichTextRun {
        // We will need a StyleTextProp atom
        ensureStyleAtomPresent()

        // First up, append the text to the
        //  underlying text atom
        val oldSize = this.rawText.length
        storeText(this.rawText + s)

        // If either of the previous styles overran
        //  the text by one, we need to shuffle that
        //  extra character onto the new ones
        val pOverRun = _styleAtom!!.paragraphTextLengthCovered - oldSize
        val cOverRun = _styleAtom!!.characterTextLengthCovered - oldSize
        if (pOverRun > 0) {
            val tpc = _styleAtom!!.paragraphStyles!!.getLast()
            tpc.updateTextSize(tpc.charactersCovered - pOverRun)
        }
        if (cOverRun > 0) {
            val tpc = _styleAtom!!.characterStyles.getLast()
            tpc.updateTextSize(tpc.charactersCovered - cOverRun)
        }

        // Next, add the styles for its paragraph and characters
        val newPTP = _styleAtom!!
            .addParagraphTextPropCollection(s.length + pOverRun)
        val newCTP = _styleAtom!!
            .addCharacterTextPropCollection(s.length + cOverRun)

        // Now, create the new RichTextRun
        val nr = RichTextRun(this, oldSize, s.length, newPTP, newCTP, false, false)

        // Add the new RichTextRun onto our list
        val newRuns: Array<RichTextRun> = richTextRuns!! + nr
        this.richTextRuns = newRuns

        // And return the new run to the caller
        return nr
    }

    /**
     * Saves the given string to the records. Doesn't
     * touch the stylings.
     */
    private fun storeText(s: String) {
        // Remove a single trailing \r, as there is an implicit one at the
        //  end of every record
        var s = s
        if (s.endsWith("\r")) {
            s = s.substring(0, s.length - 1)
        }

        // Store in the appropriate record
        if (_isUnicode) {
            // The atom can safely convert to unicode
            _charAtom!!.text = s
        } else {
            // Will it fit in a 8 bit atom?
            val hasMultibyte = hasMultibyte(s)
            if (!hasMultibyte) {
                // Fine to go into 8 bit atom
                val text = ByteArray(s.length)
                putCompressedUnicode(s, text, 0)
                _byteAtom!!.setText(text)
            } else {
                // Need to swap a TextBytesAtom for a TextCharsAtom

                // Build the new TextCharsAtom

                _charAtom = TextCharsAtom()
                _charAtom!!.text = s

                // Use the TextHeaderAtom to do the swap on the parent
                val parent = _headerAtom!!.parentRecord!!
                val cr: Array<Record> = parent.getChildRecords()
                for (i in cr.indices) {
                    // Look for TextBytesAtom
                    if (cr[i] == _byteAtom) {
                        // Found it, so replace, then all done
                        cr[i] = _charAtom!!
                        break
                    }
                }

                // Flag the change
                _byteAtom = null
                _isUnicode = true
            }
        }
        /**
         * If TextSpecInfoAtom is present, we must update the text size in it,
         * otherwise the ppt will be corrupted
         */
        val records = this.records
        if (records != null) for (i in records.indices) {
            if (records[i] is TextSpecInfoAtom) {
                val specAtom = records[i] as TextSpecInfoAtom
                if ((s.length + 1) != specAtom.charactersCovered) {
                    specAtom.reset(s.length + 1)
                }
            }
        }
    }

    /**
     * Handles an update to the text stored in one of the Rich Text Runs
     * @param run
     * @param s
     */
    fun changeTextInRichTextRun(run: RichTextRun, s: String) {
        // Figure out which run it is
        var runID = -1
        for (i in richTextRuns!!.indices) {
            if (run == this.richTextRuns!![i]) {
                runID = i
            }
        }
        require(runID != -1) { "Supplied RichTextRun wasn't from this TextRun" }

        // Ensure a StyleTextPropAtom is present, adding if required
        ensureStyleAtomPresent()

        // Update the text length for its Paragraph and Character stylings
        // If it's shared:
        //   * calculate the new length based on the run's old text
        //   * this should leave in any +1's for the end of block if needed
        // If it isn't shared:
        //   * reset the length, to the new string's length
        //   * add on +1 if the last block
        // The last run needs its stylings to be 1 longer than the raw
        //  text is. This is to define the stylings that any new text
        //  that is added will inherit
        val pCol = run._getRawParagraphStyle()!!
        val cCol = run._getRawCharacterStyle()!!
        var newSize = s.length
        if (runID == richTextRuns!!.size - 1) {
            newSize++
        }

        if (run._isParagraphStyleShared()) {
            pCol.updateTextSize(pCol.charactersCovered - run.length + s.length)
        } else {
            pCol.updateTextSize(newSize)
        }
        if (run._isCharacterStyleShared()) {
            cCol.updateTextSize(cCol.charactersCovered - run.length + s.length)
        } else {
            cCol.updateTextSize(newSize)
        }

        // Build up the new text
        // As we go through, update the start position for all subsequent runs
        // The building relies on the old text still being present
        val newText = StringBuffer()
        for (i in richTextRuns!!.indices) {
            val newStartPos = newText.length

            // Build up the new text
            if (i != runID) {
                // Not the affected run, so keep old text
                newText.append(this.richTextRuns!![i].rawText)
            } else {
                // Affected run, so use new text
                newText.append(s)
            }

            // Do we need to update the start position of this run?
            // (Need to get the text before we update the start pos)
            if (i <= runID) {
                // Change is after this, so don't need to change start position
            } else {
                // Change has occured, so update start position
                this.richTextRuns!![i].updateStartPosition(newStartPos)
            }
        }

        // Now we can save the new text
        storeText(newText.toString())
    }

    /**
     * Ensure a StyleTextPropAtom is present for this run,
     * by adding if required. Normally for internal TextRun use.
     */
    fun ensureStyleAtomPresent() {
        if (_styleAtom != null) {
            // All there
            return
        }

        // Create a new one at the right size
        _styleAtom = StyleTextPropAtom(this.rawText.length + 1)

        // Use the TextHeader atom to get at the parent
        val runAtomsParent = _headerAtom!!.parentRecord!!

        // Add the new StyleTextPropAtom after the TextCharsAtom / TextBytesAtom
        var addAfter: Record? = _byteAtom
        if (_byteAtom == null) {
            addAfter = _charAtom
        }
        runAtomsParent.addChildAfter(_styleAtom!!, addAfter!!)

        // Feed this to our sole rich text run
        check(richTextRuns!!.size == 1) { "Needed to add StyleTextPropAtom when had many rich text runs" }
        // These are the only styles for now
        this.richTextRuns!![0].supplyTextProps(
            _styleAtom!!.paragraphStyles!!.get(0),
            _styleAtom!!.characterStyles.get(0), false, false
        )
    }

    // Accesser methods follow
    fun getText(): String = text
    fun getRichTextRuns(): Array<RichTextRun>? = richTextRuns
    fun getRecords(): Array<Record?>? = records

    @get:JvmName("getTextProperty")
    var text: String
        /**
         * Returns the text content of the run, which has been made safe
         * for printing and other use.
         */
        get() {
            val rawText = this.rawText

            // PowerPoint seems to store files with \r as the line break
            // The messes things up on everything but a Mac, so translate
            //  them to \n
            var text = rawText.replace('\r', '\n')

            /*int type = _headerAtom == null ? 0 : _headerAtom.getTextType();
     if (type == TextHeaderAtom.TITLE_TYPE || type == TextHeaderAtom.CENTER_TITLE_TYPE)
     {
         //0xB acts like cariage return in page titles and like blank in the others
         text = text.replace((char)0x0B, '\n');
     }
     else
     {
         text = text.replace((char)0x0B, ' ');
     }*/
            text = text.replace(0x0B.toChar(), '\u000b')
            return text
        }
        /**
         * Changes the text.
         * Converts '\r' into '\n'
         */
        set(s) {
            val text = normalize(s)
            this.rawText = text
        }

    var rawText: String
        /**
         * Returns the raw text content of the run. This hasn't had any
         * changes applied to it, and so is probably unlikely to print
         * out nicely.
         */
        get() {
            if (_isUnicode) {
                return _charAtom!!.text
            }
            return _byteAtom!!.text
        }
        /**
         * Changes the text, and sets it all to have the same styling
         * as the the first character has.
         * If you care about styling, do setText on a RichTextRun instead
         */
        set(s) {
            // Save the new text to the atoms
            storeText(s)
            val fst = this.richTextRuns!![0]

            // Finally, zap and re-do the RichTextRuns
            this.richTextRuns = arrayOf(fst)

            // Now handle record stylings:
            // If there isn't styling
            //  no change, stays with no styling
            // If there is styling:
            //  everthing gets the same style that the first block has
            if (_styleAtom != null) {
                val pStyles: LinkedList<*> = _styleAtom!!.paragraphStyles!!
                while (pStyles.size > 1) {
                    pStyles.removeLast()
                }

                val cStyles: LinkedList<*> = _styleAtom!!.characterStyles
                while (cStyles.size > 1) {
                    cStyles.removeLast()
                }

                this.richTextRuns!![0].text = s
            } else {
                // Recreate rich text run with no styling
                this.richTextRuns!![0] = RichTextRun(this, 0, s.length)
            }
        }

    var runType: Int
        /**
         * Returns the type of the text, from the TextHeaderAtom.
         * Possible values can be seen from TextHeaderAtom
         * @see TextHeaderAtom
         */
        get() = _headerAtom!!.textType
        /**
         * Changes the type of the text. Values should be taken
         * from TextHeaderAtom. No checking is done to ensure you
         * set this to a valid value!
         * @see TextHeaderAtom
         */
        set(type) {
            _headerAtom!!.textType = type
        }

    /**
     * Supply the SlideShow we belong to.
     * Also passes it on to our child RichTextRuns
     */
    fun supplySlideShow(ss: SlideShow?) {
        slideShow = ss
        val richTextRuns = this.richTextRuns
        if (richTextRuns != null) {
            for (i in richTextRuns.indices) {
                richTextRuns[i].supplySlideShow(slideShow)
            }
        }
    }

    val hyperlinks: Array<Hyperlink?>?
        /**
         * Returns the array of all hyperlinks in this text run
         * 
         * @return the array of all hyperlinks in this text run
         * or `null` if not found.
         */
        get() = Hyperlink.find(this)

    /**
     * Fetch RichTextRun at a given position
     * 
     * @param pos 0-based index in the text
     * @return RichTextRun or null if not found
     */
    fun getRichTextRunAt(pos: Int): RichTextRun? {
        for (i in richTextRuns!!.indices) {
            val start = this.richTextRuns!![i].startIndex
            val end = this.richTextRuns!![i].endIndex
            if (pos >= start && pos < end) return this.richTextRuns!![i]
        }
        return null
    }

    val textRuler: TextRulerAtom?
        get() {
            if (_ruler == null) {
                val records = this.records
                if (records != null) for (i in records.indices) {
                    if (records[i] is TextRulerAtom) {
                        _ruler = records[i] as TextRulerAtom?
                        break
                    }
                }
            }
            return _ruler
        }

    fun createTextRuler(): TextRulerAtom? {
        _ruler = this.textRuler
        if (_ruler == null) {
            _ruler = TextRulerAtom.paragraphInstance
            _headerAtom!!.parentRecord!!.appendChildRecord(_ruler!!)
        }
        return _ruler
    }

    /**
     * Returns a new string with line breaks converted into internal ppt representation
     */
    fun normalize(s: String): String {
        val ns = s.replace("\\r?\\n".toRegex(), "\r")
        return ns
    }

    /**
     * get number type
     * @return
     */
    fun getNumberingType(characterIndex: Int): Int {
        if (this.extendedParagraphAtom != null) {
            val index = getAutoNumberIndex(characterIndex)
            if (index >= 0) {
                val paraPropList = extendedParagraphAtom!!.getExtendedParagraphPropList()
                if (paraPropList != null && paraPropList.size > 0 && index < paraPropList.size) {
                    val paraProp = paraPropList.get(index)
                    if (paraProp != null) {
                        return paraProp.numberingType
                    }
                }
            }
        }
        return -1
    }

    /**
     * get number start
     * @return
     */
    fun getNumberingStart(characterIndex: Int): Int {
        if (this.extendedParagraphAtom != null) {
            val index = getAutoNumberIndex(characterIndex)
            if (index >= 0) {
                val paraPropList = extendedParagraphAtom!!.getExtendedParagraphPropList()
                if (paraPropList != null && paraPropList.size > 0 && index < paraPropList.size) {
                    val paraProp = paraPropList.get(index)
                    if (paraProp != null) {
                        return paraProp.start
                    }
                }
            }
        }
        return 0
    }

    /**
     * charcater startoffset
     * @return
     */
    fun getAutoNumberIndex(characterIndex: Int): Int {
        val records = this.records
        if (records != null) {
            for (i in records.indices) {
                if (records[i] is StyleTextPropAtom) {
                    val stp = records[i] as StyleTextPropAtom?
                    if (stp != null) {
                        return stp.getAutoNumberIndex(characterIndex)
                    }
                }
            }
        }
        return -1
    }

    /**
     * 
     */
    fun dispose() {
        slideShow = null
        this.sheet = null
        if (_headerAtom != null) {
            _headerAtom!!.dispose()
            _headerAtom = null
        }
        if (_byteAtom != null) {
            _byteAtom!!.dispose()
            _byteAtom = null
        }
        if (_charAtom != null) {
            _charAtom!!.dispose()
            _charAtom = null
        }
        if (_styleAtom != null) {
            _styleAtom!!.dispose()
            _styleAtom = null
        }
        if (_ruler != null) {
            _ruler!!.dispose()
            _ruler = null
        }
        if (this.extendedParagraphAtom != null) {
            extendedParagraphAtom!!.dispose()
            this.extendedParagraphAtom = null
        }
        if (this.richTextRuns != null) {
            for (rt in this.richTextRuns!!) {
                rt.dispose()
            }
            this.richTextRuns = null
        }
    }


    protected var _byteAtom: TextBytesAtom? = null
    protected var _charAtom: TextCharsAtom? = null
    var _ruler: TextRulerAtom? = null
    /**
     * 
     * @return
     */
    /**
     * get bullet and number ruler
     * @param extendedParaAtom
     */
    var extendedParagraphAtom: ExtendedParagraphAtom? = null
    protected var _isUnicode: Boolean = false

    /**
     * Fetch the rich text runs (runs of text with the same styling) that
     * are contained within this block of text
     */
    @get:JvmName("getRichTextRunsProperty")
    var richTextRuns: Array<RichTextRun>? = null
        protected set
    private var slideShow: SlideShow? = null
    var sheet: Sheet? = null
    /**
     * @return  Shape ID
     */
    /**
     * @param id Shape ID
     */
    var shapeId: Int = -1
    /**
     * @return  0-based index of the text run in the SLWT container
     */
    /**
     * @param id 0-based index of the text run in the SLWT container
     */
    var index: Int = 0 //position in the owning SlideListWithText

    /**
     * Internal constructor and initializer
     */
    init {
        if (tba != null) {
            _byteAtom = tba
            _isUnicode = false
        } else {
            _charAtom = tca
            _isUnicode = true
        }
        val runRawText = this.text

        // Figure out the rich text runs
        var pStyles: LinkedList<*> = LinkedList<Any?>()
        var cStyles: LinkedList<*> = LinkedList<Any?>()
        if (_styleAtom != null) {
            // Get the style atom to grok itself
            _styleAtom!!.setParentTextSize(runRawText.length)
            pStyles = _styleAtom!!.paragraphStyles!!
            cStyles = _styleAtom!!.characterStyles
        }
        buildRichTextRuns(pStyles, cStyles, runRawText)
    }
}
