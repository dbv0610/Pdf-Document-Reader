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
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherComplexProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.record.ExControl
import com.wxiwei.office.fc.hslf.record.ExObjList
import com.wxiwei.office.fc.hslf.record.OEShapeAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.util.LittleEndian
import java.io.UnsupportedEncodingException

/**
 * Represents an ActiveX control in a PowerPoint document.
 * 
 * TODO: finish
 * @author Yegor Kozlov
 */
class ActiveXShape : Picture {
    /**
     * Create a new `Picture`
     * 
     * @param pictureIdx the index of the picture
     */
    constructor(movieIdx: Int, pictureIdx: Int) : super(pictureIdx, null) {
        setActiveXIndex(movieIdx)
    }

    /**
     * Create a `Picture` object
     * 
     * @param escherRecord the `EscherSpContainer` record which holds information about
     * this picture in the `Slide`
     * @param parent the parent shape of this picture
     */
    protected constructor(
        escherRecord: EscherContainerRecord?,
        parent: Shape?
    ) : super(escherRecord, parent)

    /**
     * Create a new Placeholder and initialize internal structures
     * 
     * @return the created `EscherContainerRecord` which holds shape data
     */
    override fun createSpContainer(idx: Int, isChild: Boolean): EscherContainerRecord? {
        spContainer = super.createSpContainer(idx, isChild)

        /*EscherSpRecord spRecord = spContainer!!.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE
            | EscherSpRecord.FLAG_OLESHAPE);

        setShapeType(ShapeTypes.HostControl);
        setEscherProperty(EscherProperties.BLIP__PICTUREID, idx);
        setEscherProperty(EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80008);
        setEscherProperty(EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);
        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, -1);

        EscherClientDataRecord cldata = new EscherClientDataRecord();
        cldata.setOptions((short)0xF);
        spContainer!!.addChildRecord(cldata); // TODO unit test to prove getChildRecords().add is wrong

        OEShapeAtom oe = new OEShapeAtom();

        //convert hslf into ddf
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            //oe.writeOut(out);
        }
        catch(Exception e)
        {
            throw new HSLFException(e);
        }
        cldata.setRemainingData(out.toByteArray());
*/
        return spContainer
    }

    /**
     * Assign a control to this shape
     * 
     * @see com.wxiwei.office.fc.hslf.usermodel.SlideShow.addMovie
     * @param idx  the index of the movie
     */
    fun setActiveXIndex(idx: Int) {
        val spContainer = spContainer
        val it: MutableIterator<EscherRecord?> = spContainer!!.childIterator
        while (it.hasNext()) {
            val obj = it.next()
            if (obj!!.recordId == EscherClientDataRecord.RECORD_ID) {
                val clientRecord = obj as EscherClientDataRecord
                val recdata = clientRecord.remainingData
                LittleEndian.putInt(recdata!!, 8, idx)
            }
        }
    }

    val controlIndex: Int
        get() {
            var idx = -1
            val oe =
                getClientDataRecord(RecordTypes.OEShapeAtom.typeID) as OEShapeAtom?
            if (oe != null) idx = oe.options
            return idx
        }

    /**
     * Set a property of this ActiveX control
     * @param key
     * @param value
     */
    fun setProperty(key: String?, value: String?) {
    }

    val exControl: ExControl?
        /**
         * Document-level container that specifies information about an ActiveX control
         * 
         * @return container that specifies information about an ActiveX control
         */
        get() {
            val idx = this.controlIndex
            var ctrl: ExControl? = null
            val doc =
                sheet!!.slideShow!!.documentRecord!!
            val lst =
                doc.findFirstOfType(RecordTypes.ExObjList.typeID.toLong()) as ExObjList?
            if (lst != null) {
                val ch: Array<Record> =
                    lst.getChildRecords()
                for (i in ch.indices) {
                    if (ch[i] is ExControl) {
                        val c = ch[i] as ExControl
                        if (c.exOleObjAtom!!.objID == idx) {
                            ctrl = c
                            break
                        }
                    }
                }
            }
            return ctrl
        }

    override fun afterInsert(sheet: Sheet?) {
        val ctrl = this.exControl
        ctrl!!.exControlAtom.slideId = sheet!!._getSheetNumber()

        try {
            val name = ctrl.getProgId() + "-" + this.controlIndex
            val data = (name + '\u0000').toByteArray(charset("UTF-16LE"))
            val prop = EscherComplexProperty(
                EscherProperties.GROUPSHAPE__SHAPENAME, false, data
            )
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            opt!!.addEscherProperty(prop)
        } catch (e: UnsupportedEncodingException) {
            throw HSLFException(e)
        }
    }

    companion object {
        val DEFAULT_ACTIVEX_THUMBNAIL: Int = -1
    }
}
