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

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.putInt
import java.io.IOException
import java.io.OutputStream


/**
 * OEPlaceholderAtom (3998).
 * <br></br>
 * What MSDN says about  `OutlineTextRefAtom`:
 * 
 * 
 * Appears in a slide to indicate a text that is already contained in the document,
 * in a SlideListWithText containter. Sometimes slide texts are not contained
 * within the slide container to be able to delay loading a slide and still display
 * the title and body text in outline view.
 * 
 * 
 * @author Yegor Kozlov
 */
class OutlineTextRefAtom : RecordAtom {
    /**
     * record header
     */
    private var _header: ByteArray?

    /**
     * Return text's index within the SlideListWithText container
     * (0 for title, 1..n for the nth body).
     * 
     * @return idx text's index
     */
    /**
     * Sets text's index within the SlideListWithText container
     * (0 for title, 1..n for the nth body).
     * 
     * @param idx 0-based text's index
     */
    /**
     * the text's index within the SlideListWithText (0 for title, 1..n for the nth body)
     */
    var textIndex: Int

    /**
     * Build an instance of `OutlineTextRefAtom` from on-disk data
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the record data
        this.textIndex = getInt(source, start + 8)
    }

    /**
     * Create a new instance of `FontEntityAtom`
     */
    protected constructor() {
        this.textIndex = 0

        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 0)
        LittleEndian.putUShort(_header!!, 2, getRecordType().toInt())
        LittleEndian.putInt(_header!!, 4, 4)
    }

    public override fun getRecordType(): Long {
        return RecordTypes.OutlineTextRefAtom.typeID.toLong()
    }

    /**
     * Write the contents of the record back, so it can be written to disk
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        out.write(_header)

        val recdata = ByteArray(4)
        putInt(recdata, 0, this.textIndex)
        out.write(recdata)
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }
}
