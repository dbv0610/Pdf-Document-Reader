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

import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.hslf.record.Slide

/**
 * Title masters define the design template for slides with a Title Slide layout.
 * 
 * @author Yegor Kozlov
 */
class TitleMaster(record: Slide?, sheetNo: Int) : MasterSheet(record, sheetNo) {
    private var _runs: Array<TextRun>?

    /**
     * Constructs a TitleMaster
     * 
     */
    init {
        _runs = Sheet.findTextRuns(pPDrawing!!)
        for (i in _runs!!.indices) _runs!![i].sheet = this
    }

    /**
     * Returns an array of all the TextRuns found
     */
    override val textRuns: Array<TextRun>?
        get() = _runs

    /**
     * Delegate the call to the underlying slide master.
     */
    override fun getStyleAttribute(
        txtype: Int,
        level: Int,
        name: String?,
        isCharacter: Boolean
    ): TextProp? {
        val master = masterSheet
        return if (master == null) null else master.getStyleAttribute(
            txtype,
            level,
            name,
            isCharacter
        )
    }

    /**
     * Returns the slide master for this title master.
     */
    override val masterSheet: MasterSheet?
        get() {
            val master = slideShow!!.slidesMasters!!
            val sa = (sheetContainer as Slide).slideAtom
            val masterId = sa!!.masterID
            for (i in master.indices) {
                if (masterId == master[i]?._getSheetNumber()) return master[i]
            }
            return null
        }

    /**
     * 
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
    }
}
