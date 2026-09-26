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

import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.hslf.record.MainMaster
import com.wxiwei.office.fc.hslf.record.TextHeaderAtom
import com.wxiwei.office.fc.hslf.record.TxMasterStyleAtom
import com.wxiwei.office.fc.hslf.usermodel.SlideShow

/**
 * SlideMaster determines the graphics, layout, and formatting for all the slides in a given presentation.
 * It stores information about default font styles, placeholder sizes and positions,
 * background design, and color schemes.
 * 
 * @author Yegor Kozlov
 */
class SlideMaster(record: MainMaster?, sheetNo: Int) : MasterSheet(record, sheetNo) {
    /**
     * Returns an array of all the TextRuns found
     */
    override val textRuns: Array<TextRun>?
        get() = _runs

    /**
     * Returns `null` since SlideMasters doen't have master sheet.
     */
    override val masterSheet: MasterSheet?
        get() = null

    /**
     * Pickup a style attribute from the master.
     * This is the "workhorse" which returns the default style attrubutes.
     */
    override fun getStyleAttribute(
        txtype: Int,
        level: Int,
        name: String?,
        isCharacter: Boolean
    ): TextProp? {
        var txtype = txtype
        var prop: TextProp? = null
        for (i in level downTo 0) {
            val styles = if (isCharacter)
                this.txMasterStyleAtoms!![txtype]!!.characterStyles!!
            else
                this.txMasterStyleAtoms!![txtype]!!.paragraphStyles!!
            if (i < styles.size) prop = styles[i]?.findByName(name)
            if (prop != null) break
        }
        if (prop == null) {
            if (isCharacter) {
                when (txtype) {
                    TextHeaderAtom.CENTRE_BODY_TYPE, TextHeaderAtom.HALF_BODY_TYPE, TextHeaderAtom.QUARTER_BODY_TYPE -> txtype =
                        TextHeaderAtom.BODY_TYPE

                    TextHeaderAtom.CENTER_TITLE_TYPE -> txtype =
                        TextHeaderAtom.TITLE_TYPE

                    else -> return null
                }
            } else {
                when (txtype) {
                    TextHeaderAtom.CENTRE_BODY_TYPE, TextHeaderAtom.HALF_BODY_TYPE, TextHeaderAtom.QUARTER_BODY_TYPE -> txtype =
                        TextHeaderAtom.BODY_TYPE

                    TextHeaderAtom.CENTER_TITLE_TYPE -> txtype =
                        TextHeaderAtom.TITLE_TYPE

                    else -> return null
                }
            }
            prop = getStyleAttribute(txtype, level, name, isCharacter)
        }
        return prop
    }

    /**
     * Assign SlideShow for this slide master.
     * (Used interanlly)
     */
    override var slideShow: SlideShow?
        get() = super.slideShow
        set(ss) {
            super.slideShow = ss

            //after the slide show is assigned collect all available style records
            if (this.txMasterStyleAtoms == null) {
                this.txMasterStyleAtoms = arrayOfNulls<TxMasterStyleAtom>(9)

                /*TxMasterStyleAtom txdoc = getSlideShow().getDocumentRecord().getEnvironment()
                    .getTxMasterStyleAtom();
                _txmaster[txdoc.getTextType()] = txdoc;*/
                val txrec = (sheetContainer as MainMaster).txMasterStyleAtoms!!
                for (i in txrec.indices) {
                    this.txMasterStyleAtoms!![txrec[i].textType] = txrec[i]
                }
                val txdoc = slideShow!!.documentRecord!!.environment!!
                    .txMasterStyleAtom!!
                this.txMasterStyleAtoms!![txdoc.textType] = txdoc
            }
        }

    override fun onAddTextShape(shape: TextShape?) {
        val run = shape!!.textRun

        if (_runs == null) _runs = arrayOf<TextRun>(run!!)
        else {
            _runs = _runs!! + run!!
        }
    }

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        if (_runs != null) {
            for (tr in _runs!!) {
                tr.dispose()
            }
            _runs = null
        }
        if (this.txMasterStyleAtoms != null) {
            for (tsa in this.txMasterStyleAtoms!!) {
                if (tsa != null) {
                    tsa.dispose()
                }
            }
            this.txMasterStyleAtoms = null
        }
    }

    private var _runs: Array<TextRun>?

    /**
     * all TxMasterStyleAtoms available in this master
     */
    var txMasterStyleAtoms: Array<TxMasterStyleAtom?>? = null
        private set

    /**
     * Constructs a SlideMaster from the MainMaster record,
     * 
     */
    init {
        _runs = Sheet.findTextRuns(pPDrawing!!)
        for (i in _runs!!.indices) _runs!![i].sheet = this
    }
}
