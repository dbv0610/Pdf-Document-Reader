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

import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.hslf.record.ExEmbed
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.usermodel.ObjectData

/**
 * A shape representing embedded OLE obejct.
 * 
 * @author Yegor Kozlov
 */
class OLEShape : Picture {
    protected var _exEmbed: ExEmbed? = null

    /**
     * Create a new `OLEShape`
     * 
     * @param idx the index of the picture
     */
    constructor(idx: Int) : super(idx)

    /**
     * Create a new `OLEShape`
     * 
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    constructor(idx: Int, parent: Shape?) : super(idx, parent)

    /**
     * Create a `OLEShape` object
     * 
     * @param escherRecord the `EscherSpContainer` record which holds information about
     * this picture in the `Slide`
     * @param parent the parent shape of this picture
     */
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    val objectID: Int
        /**
         * Returns unique identifier for the OLE object.
         * 
         * @return the unique identifier for the OLE object
         */
        get() = ShapeKit.getEscherProperty(spContainer, EscherProperties.BLIP__PICTUREID)

    val objectData: ObjectData?
        /**
         * Returns unique identifier for the OLE object.
         * 
         * @return the unique identifier for the OLE object
         */
        get() {
            val ppt = sheet!!.slideShow!!
            val ole =
                ppt.embeddedObjects!!

            //persist reference
            val ref = this.exEmbed!!.exOleObjAtom!!.objStgDataRef

            var data: ObjectData? = null

            for (i in ole.indices) {
                if (ole[i]!!.exOleObjStg!!.persistId == ref) {
                    data = ole[i]
                }
            }

            if (data == null) {
            }

            return data
        }

    val exEmbed: ExEmbed?
        /**
         * Return the record container for this embedded object.
         * 
         * 
         * 
         * It contains:
         * 1. ExEmbedAtom.(4045)
         * 2. ExOleObjAtom (4035)
         * 3. CString (4026), Instance MenuName (1) used for menus and the Links dialog box.
         * 4. CString (4026), Instance ProgID (2) that stores the OLE Programmatic Identifier.
         * A ProgID is a string that uniquely identifies a given object.
         * 5. CString (4026), Instance ClipboardName (3) that appears in the paste special dialog.
         * 6. MetaFile( 4033), optional
         * 
         */
        get() {
            if (_exEmbed == null) {
                val ppt = sheet!!.slideShow!!

                val lst = ppt.documentRecord!!.exObjList
                if (lst == null) {
                    return null
                }

                val id = this.objectID
                val ch: Array<Record> =
                    lst.getChildRecords()
                for (i in ch.indices) {
                    if (ch[i] is ExEmbed) {
                        val embd = ch[i] as ExEmbed
                        if (embd.exOleObjAtom!!.objID == id) _exEmbed = embd
                    }
                }
            }
            return _exEmbed
        }

    val instanceName: String?
        /**
         * Returns the instance name of the embedded object, e.g. "Document" or "Workbook".
         * 
         * @return the instance name of the embedded object
         */
        get() = this.exEmbed!!.getMenuName()

    val fullName: String?
        /**
         * Returns the full name of the embedded object,
         * e.g. "Microsoft Word Document" or "Microsoft Office Excel Worksheet".
         * 
         * @return the full name of the embedded object
         */
        get() = this.exEmbed!!.getClipboardName()

    val progID: String?
        /**
         * Returns the ProgID that stores the OLE Programmatic Identifier.
         * A ProgID is a string that uniquely identifies a given object, for example,
         * "Word.Document.8" or "Excel.Sheet.8".
         * 
         * @return the ProgID
         */
        get() = this.exEmbed!!.getProgId()

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        if (_exEmbed != null) {
            _exEmbed!!.dispose()
            _exEmbed = null
        }
    }
}
