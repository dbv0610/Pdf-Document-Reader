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

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ddf.DefaultEscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRGBProperty
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherSpgrRecord
import com.wxiwei.office.fc.ddf.EscherTextboxRecord
import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.io.IOException
import java.io.OutputStream
import java.util.Vector

/**
 * These are actually wrappers onto Escher drawings. Make use of
 * the DDF classes to do useful things with them.
 * For now, creates a tree of the Escher records, and then creates any
 * PowerPoint (hslf) records found within the EscherTextboxRecord
 * (msofbtClientTextbox) records.
 * Also provides easy access to the EscherTextboxRecords, so that their
 * text may be extracted and used in Sheets
 * 
 * @author Nick Burch
 */
// For now, pretending to be an atom. Might not always be, but that
//  would require a wrapping class
class PPDrawing : RecordAtom {
    /* ******************** record stuff follows ********************** */
    /**
     * Sets everything up, groks the escher etc
     */
    protected constructor(source: ByteArray, start: Int, len: Int) {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Get the type
        _type = LittleEndian.getUShort(_header!!, 2).toLong()

        // Get the contents for now
        val contents = ByteArray(len)
        System.arraycopy(source, start, contents, 0, len)

        // Build up a tree of Escher records contained within
        val erf = DefaultEscherRecordFactory()
        val escherChildren = Vector<Any?>()
        findEscherChildren(erf, contents, 8, len - 8, escherChildren)

        this.escherRecords = Array<EscherRecord>(escherChildren.size) { i ->
            escherChildren.get(i) as EscherRecord
        }

        // Find and EscherTextboxRecord's, and wrap them up
        val textboxes = Vector<Any?>()
        findEscherTextboxRecord(this.escherRecords!!, textboxes)
        textboxWrappers = Array<EscherTextboxWrapper>(textboxes.size) { i ->
            textboxes.get(i) as EscherTextboxWrapper
        }
    }

    /**
     * Creates a new, empty, PPDrawing (typically for use with a new Slide
     * or Notes)
     */
    constructor() {
        _header = ByteArray(8)
        LittleEndian.putUShort(_header!!, 0, 15)
        LittleEndian.putUShort(_header!!, 2, RecordTypes.PPDrawing.typeID)
        LittleEndian.putInt(_header!!, 4, 0)

        textboxWrappers = arrayOf<EscherTextboxWrapper>()
        create()
    }

    /**
     * Tree walking way of finding Escher Child Records
     */
    private fun findEscherChildren(
        erf: DefaultEscherRecordFactory, source: ByteArray, startPos: Int,
        lenToGo: Int, found: Vector<Any?>
    ) {
        var startPos = startPos
        var lenToGo = lenToGo
        val escherBytes = getInt(source, startPos + 4) + 8

        // Find the record
        val r = erf.createRecord(source, startPos)
        // Fill it in
        r.fillFields(source, startPos, erf)
        // Save it
        found.add(r)

        // Wind on
        var size = r.recordSize

        /*if (size < 8)
        {
            logger.log(POILogger.WARN, "Hit short DDF record at " + startPos + " - " + size);
        }*/
        /**
         * Sanity check. Always advance the cursor by the correct value.
         * 
         * getRecordSize() must return exatcly the same number of bytes that was written in fillFields.
         * Sometimes it is not so, see an example in bug #44770. Most likely reason is that one of ddf records calculates wrong size.
         */
        if (size != escherBytes) {
            /*logger.log(
                POILogger.WARN,
                "Record length=" + escherBytes + " but getRecordSize() returned "
                    + r.getRecordSize() + "; record: " + r.getClass());*/
            size = escherBytes
        }
        startPos += size
        lenToGo -= size
        if (lenToGo >= 8) {
            findEscherChildren(erf, source, startPos, lenToGo, found)
        }
    }

    /**
     * Look for EscherTextboxRecords
     */
    private fun findEscherTextboxRecord(toSearch: Array<EscherRecord>, found: Vector<Any?>) {
        for (i in toSearch.indices) {
            if (toSearch[i] is EscherTextboxRecord) {
                val tbr = toSearch[i] as EscherTextboxRecord?
                val w = EscherTextboxWrapper(tbr)
                found.add(w)
                if ("BinaryTagData" == toSearch[i].recordName) {
                    w.shapeId = toSearch[i].recordId.toInt()
                } else {
                    for (j in i downTo 0) {
                        if (toSearch[j] is EscherSpRecord) {
                            val sp = toSearch[j] as EscherSpRecord
                            w.shapeId = sp.shapeId
                            break
                        }
                    }
                }
            } else {
                // If it has children, walk them
                if (toSearch[i].isContainerRecord) {
                    val childrenL = toSearch[i].childRecords
                    val children: Array<EscherRecord> = childrenL.toTypedArray()
                    findEscherTextboxRecord(children, found)
                }
            }
        }
    }

    /**
     * We are type 1036
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * We're pretending to be an atom, so return null
     */
    override fun getChildRecords(): Array<Record>? {
        return null
    }

    /**
     * Write the contents of the record back, so it can be written
     * to disk
     * Walks the escher layer to get the contents
     */
    @Throws(IOException::class)
    fun writeOut(out: OutputStream) {
        // Ensure the escher layer reflects the text changes
        for (i in textboxWrappers!!.indices) {
            //textboxWrappers[i].writeOut(null);
        }

        // Find the new size of the escher children;
        var newSize = 0
        for (i in escherRecords!!.indices) {
            newSize += this.escherRecords!![i].recordSize
        }

        // Update the size (header bytes 5-8)
        LittleEndian.putInt(_header!!, 4, newSize)

        // Write out our header
        out.write(_header)

        // Now grab the children's data
        val b = ByteArray(newSize)
        var done = 0
        for (i in escherRecords!!.indices) {
            val written = this.escherRecords!![i].serialize(done, b)
            done += written
        }

        // Finally, write out the children
        out.write(b)
    }

    /**
     * Create the Escher records associated with a new PPDrawing
     */
    private fun create() {
        val dgContainer = EscherContainerRecord()
        dgContainer.recordId = EscherContainerRecord.DG_CONTAINER
        dgContainer.options = 15.toShort()

        val dg = EscherDgRecord()
        dg.options = 16.toShort()
        dg.numShapes = 1
        dgContainer.addChildRecord(dg)

        val spgrContainer = EscherContainerRecord()
        spgrContainer.options = 15.toShort()
        spgrContainer.recordId = EscherContainerRecord.SPGR_CONTAINER

        var spContainer = EscherContainerRecord()
        spContainer.options = 15.toShort()
        spContainer.recordId = EscherContainerRecord.SP_CONTAINER

        val spgr = EscherSpgrRecord()
        spgr.options = 1.toShort()
        spContainer.addChildRecord(spgr)

        var sp = EscherSpRecord()
        sp.options = ((ShapeTypes.NotPrimitive shl 4) + 2).toShort()
        sp.flags = EscherSpRecord.FLAG_PATRIARCH or EscherSpRecord.FLAG_GROUP
        spContainer.addChildRecord(sp)
        spgrContainer.addChildRecord(spContainer)
        dgContainer.addChildRecord(spgrContainer)

        spContainer = EscherContainerRecord()
        spContainer.options = 15.toShort()
        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        sp = EscherSpRecord()
        sp.options = ((ShapeTypes.Rectangle shl 4) + 2).toShort()
        sp.flags = EscherSpRecord.FLAG_BACKGROUND or EscherSpRecord.FLAG_HASSHAPETYPE
        spContainer.addChildRecord(sp)

        val opt = EscherOptRecord()
        opt.recordId = EscherOptRecord.RECORD_ID
        opt.addEscherProperty(EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, 134217728))
        opt.addEscherProperty(EscherRGBProperty(EscherProperties.FILL__FILLBACKCOLOR, 134217733))
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.FILL__RECTRIGHT, 10064750))
        opt.addEscherProperty(EscherSimpleProperty(EscherProperties.FILL__RECTBOTTOM, 7778750))
        opt.addEscherProperty(EscherBoolProperty(EscherProperties.FILL__NOFILLHITTEST, 1179666))
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.LINESTYLE__NOLINEDRAWDASH,
                524288
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.SHAPE__BLACKANDWHITESETTINGS, 9
            )
        )
        opt.addEscherProperty(
            EscherSimpleProperty(
                EscherProperties.SHAPE__BACKGROUNDSHAPE,
                65537
            )
        )
        spContainer.addChildRecord(opt)

        dgContainer.addChildRecord(spContainer)

        this.escherRecords = arrayOf<EscherRecord>(dgContainer)
    }

    /**
     * Add a new EscherTextboxWrapper to this `PPDrawing`.
     */
    fun addTextboxWrapper(txtbox: EscherTextboxWrapper?) {
        textboxWrappers = textboxWrappers!! + txtbox!!
    }

    val escherDgRecord: EscherDgRecord?
        /**
         * Return EscherDgRecord which keeps track of the number of shapes and shapeId in this drawing group
         * 
         * @return EscherDgRecord
         */
        get() {
            if (dg == null) {
                val dgContainer =
                    this.escherRecords!![0] as EscherContainerRecord
                val it =
                    dgContainer.childIterator
                while (it.hasNext()) {
                    val r = it.next()
                    if (r is EscherDgRecord) {
                        dg = r
                        break
                    }
                }
            }
            return dg
        }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        if (this.escherRecords != null) {
            for (er in this.escherRecords!!) {
                er.dispose()
            }
            this.escherRecords = null
        }
        if (textboxWrappers != null) {
            for (etw in textboxWrappers!!) {
                etw.dispose()
            }
            textboxWrappers = null
        }
        if (dg != null) {
            dg!!.dispose()
            dg = null
        }
    }

    private var _header: ByteArray? = null
    private var _type: Long = 0

    /**
     * Get access to the underlying Escher Records
     */
    var escherRecords: Array<EscherRecord>? = null
        private set

    /**
     * Get access to the atoms inside Textboxes
     */
    var textboxWrappers: Array<EscherTextboxWrapper>? = null
        private set

    //cached EscherDgRecord
    private var dg: EscherDgRecord? = null
}
