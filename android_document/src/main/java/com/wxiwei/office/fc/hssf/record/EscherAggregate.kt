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
package com.wxiwei.office.fc.hssf.record

import com.wxiwei.office.fc.ddf.DefaultEscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherDgRecord
import com.wxiwei.office.fc.ddf.EscherDggRecord.FileIdCluster
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherSerializationListener
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherSpgrRecord
import com.wxiwei.office.fc.ddf.EscherTextboxRecord
import com.wxiwei.office.fc.hssf.model.AbstractShape.Companion.createShape
import com.wxiwei.office.fc.hssf.model.CommentShape
import com.wxiwei.office.fc.hssf.model.ConvertAnchor.createAnchor
import com.wxiwei.office.fc.hssf.model.DrawingManager2
import com.wxiwei.office.fc.hssf.model.TextboxShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFChart
import com.wxiwei.office.fc.hssf.usermodel.HSSFClientAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFPatriarch
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFShapeContainer
import com.wxiwei.office.fc.hssf.usermodel.HSSFShapeFactory
import com.wxiwei.office.fc.hssf.usermodel.HSSFShapeGroup
import com.wxiwei.office.fc.hssf.usermodel.HSSFTextbox
import com.wxiwei.office.fc.util.POILogFactory.Companion.getLogger
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * This class is used to aggregate the MSODRAWING and OBJ record
 * combinations.  This is necessary due to the bizare way in which
 * these records are serialized.  What happens is that you get a
 * combination of MSODRAWING -> OBJ -> MSODRAWING -> OBJ records
 * but the escher records are serialized _across_ the MSODRAWING
 * records.
 * 
 * 
 * It gets even worse when you start looking at TXO records.
 * 
 * 
 * So what we do with this class is aggregate lazily.  That is
 * we don't aggregate the MSODRAWING -> OBJ records unless we
 * need to modify them.
 * 
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
class EscherAggregate(private val drawingManager: DrawingManager2) : AbstractEscherHolderRecord() {
    @JvmField
    protected var patriarch: HSSFPatriarch? = null

    /** Maps shape container objects to their [TextObjectRecord] or [ObjRecord]  */
    private var shapeToObj: MutableMap<EscherRecord?, Record?> = HashMap<EscherRecord?, Record?>()

    //chart records
    private val chartToObj: MutableMap<EscherRecord?, MutableList<Record>?> =
        HashMap()

    private var drawingGroupId: Short = 0

    /**
     * list of "tail" records that need to be serialized after all drawing group records
     */
    private val tailRec: MutableList<Record?> = ArrayList<Record?>()

    /**
     * @return  Returns the current sid.
     */
    override fun getSid(): Short {
        return Companion.sid
    }

    /**
     * Calculates the string representation of this record.  This is
     * simply a dump of all the records.
     */
    override fun toString(): String {
        val nl = System.getProperty("line.separtor")

        val result = StringBuffer()
        result.append('[').append(getRecordName()).append(']'.toString() + nl)
        val iterator: MutableIterator<*> = escherRecords.iterator()
        while (iterator.hasNext()) {
            val escherRecord = iterator.next() as EscherRecord
            result.append(escherRecord.toString())
        }
        result.append("[/").append(getRecordName()).append(']'.toString() + nl)

        return result.toString()
    }

    /**
     * Serializes this aggregate to a byte array.  Since this is an aggregate
     * record it will effectively serialize the aggregated records.
     * 
     * @param offset    The offset into the start of the array.
     * @param data      The byte array to serialize to.
     * @return          The number of bytes serialized.
     */
    override fun serialize(offset: Int, data: ByteArray): Int {
        convertUserModelToRecords()

        // Determine buffer size
        val records: MutableList<*> = escherRecords
        val size = getEscherRecordSize(records)
        val buffer = ByteArray(size)


        // Serialize escher records into one big data structure and keep note of ending offsets.
        val spEndingOffsets: MutableList<Int?> = ArrayList<Int?>()
        val shapes: MutableList<EscherRecord?> = ArrayList<EscherRecord?>()
        var pos = 0
        val iterator: MutableIterator<*> = records.iterator()
        while (iterator.hasNext()) {
            val e = iterator.next() as EscherRecord
            pos += e.serialize(pos, buffer, object : EscherSerializationListener {
                override fun beforeRecordSerialize(
                    offset: Int,
                    recordId: Short,
                    record: EscherRecord?
                ) {
                }

                override fun afterRecordSerialize(
                    offset: Int,
                    recordId: Short,
                    size: Int,
                    record: EscherRecord?
                ) {
                    if (recordId == EscherClientDataRecord.RECORD_ID || recordId == EscherTextboxRecord.RECORD_ID) {
                        spEndingOffsets.add(offset)
                        shapes.add(record)
                    }
                }
            })
        }
        // todo: fix this
        shapes.add(0, null)
        spEndingOffsets.add(0, null)

        // Split escher records into separate MSODRAWING and OBJ, TXO records.  (We don't break on
        // the first one because it's the patriach).
        pos = offset
        for (i in 1..<shapes.size) {
            val endOffset = (spEndingOffsets.get(i) as Int) - 1
            val startOffset: Int
            if (i == 1) startOffset = 0
            else startOffset = (spEndingOffsets.get(i - 1) as Int)

            // Create and write a new MSODRAWING record
            val drawing = DrawingRecord()
            val drawingData = ByteArray(endOffset - startOffset + 1)
            System.arraycopy(buffer, startOffset, drawingData, 0, drawingData.size)
            drawing.setData(drawingData)
            var temp = drawing.serialize(pos, data)
            pos += temp

            // Write the matching OBJ record
            val obj = shapeToObj.get(shapes.get(i))
            temp = obj!!.serialize(pos, data)
            pos += temp
        }

        // write records that need to be serialized after all drawing group records
        for (i in tailRec.indices) {
            val rec = tailRec.get(i) as Record
            pos += rec.serialize(pos, data)
        }

        val bytesWritten = pos - offset
        if (bytesWritten != getRecordSize()) throw RecordFormatException(bytesWritten.toString() + " bytes written but getRecordSize() reports " + getRecordSize())
        return bytesWritten
    }

    /**
     * How many bytes do the raw escher records contain.
     * @param records   List of escher records
     * @return  the number of bytes
     */
    private fun getEscherRecordSize(records: MutableList<*>): Int {
        var size = 0
        val iterator: MutableIterator<*> = records.iterator()
        while (iterator.hasNext()) {
            size += (iterator.next() as EscherRecord).recordSize
        }
        return size
    }

    override fun getRecordSize(): Int {
        // TODO - convert this to RecordAggregate
        convertUserModelToRecords()
        val records: MutableList<*> = escherRecords
        val rawEscherSize = getEscherRecordSize(records)
        val drawingRecordSize = rawEscherSize + (shapeToObj.size) * 4
        var objRecordSize = 0
        run {
            val iterator: MutableIterator<*> = shapeToObj.values.iterator()
            while (iterator.hasNext()) {
                val r = iterator.next() as Record
                objRecordSize += r.getRecordSize()
            }
        }
        var tailRecordSize = 0
        val iterator = tailRec.iterator()
        while (iterator.hasNext()) {
            val r = iterator.next() as Record
            tailRecordSize += r.getRecordSize()
        }
        return drawingRecordSize + objRecordSize + tailRecordSize
    }

    /**
     * Associates an escher record to an OBJ record or a TXO record.
     */
    fun associateShapeToObjRecord(r: EscherRecord?, objRecord: ObjRecord?): Any? {
        return shapeToObj.put(r, objRecord)
    }

    fun getPatriarch(): HSSFPatriarch? {
        return patriarch
    }

    fun setPatriarch(patriarch: HSSFPatriarch?) {
        this.patriarch = patriarch
    }

    /**
     * Converts the Records into UserModel
     * objects on the bound HSSFPatriarch
     */
    fun convertRecordsToUserModel(workbook: AWorkbook?) {
        checkNotNull(patriarch) { "Must call setPatriarch() first" }

        // The top level container ought to have
        //  the DgRecord and the container of one container
        //  per shape group (patriach overall first)
//		EscherContainerRecord topContainer = getEscherContainer();
//		if(topContainer == null) 
//		{
//			return;
//		}
        val containerList = getgetEscherContainers()
        if (containerList.size == 0) {
            return
        }

        var shape: HSSFShape? = null
        var index = 0
        if (containerList.get(index)!!.childContainers.size > 0) {
            val topContainer = containerList.get(index++)!!.childContainers.get(0)

            val tcc = topContainer!!.childContainers
            check(tcc.size != 0) { "No child escher containers at the point that should hold the patriach data, and one container per top level shape!" }

            // First up, get the patriach position
            // This is in the first EscherSpgrRecord, in
            //  the first container, with a EscherSRecord too
            val patriachContainer = tcc.get(0) as EscherContainerRecord
            var spgr: EscherSpgrRecord? = null
            val it: MutableIterator<EscherRecord?> = patriachContainer.childIterator
            while (it.hasNext()) {
                val r: EscherRecord? = it.next()
                if (r is EscherSpgrRecord) {
                    spgr = r
                    break
                }
            }
            if (spgr != null) {
                patriarch!!.setCoordinates(
                    spgr.rectX1, spgr.rectY1,
                    spgr.rectX2, spgr.rectY2
                )
            }

            // Now process the containers for each group
            //  and objects
            for (i in 1..<tcc.size) {
                val shapeContainer =
                    tcc.get(i) as EscherContainerRecord

                shape = HSSFShapeFactory.createShape(workbook, shapeToObj, shapeContainer, null)
                if (shape != null) {
                    convertRecordsToUserModel(shapeContainer, shape)
                    patriarch!!.addShape(shape)
                }
            }
        }


        while (index < containerList.size) {
            val shapeContainer = containerList.get(index)!!

            shape = HSSFShapeFactory.createShape(workbook, shapeToObj, shapeContainer, null)
            if (shape != null) {
                convertRecordsToUserModel(shapeContainer, shape)
                patriarch!!.addShape(shape)
            }
            index++
        }


        // Now, clear any trace of what records make up
        //  the patriarch
        // Otherwise, everything will go horribly wrong
        //  when we try to write out again....
        // clearEscherRecords();
        drawingManager.dgg.fileIdClusters = arrayOfNulls<FileIdCluster>(0)
    }

    private fun convertRecordsToUserModel(shapeContainer: EscherContainerRecord, model: Any?) {
        val it: MutableIterator<EscherRecord?> = shapeContainer.childIterator
        while (it.hasNext()) {
            val r: EscherRecord? = it.next()
            if (r is EscherSpgrRecord) {
                // This may be overriden by a later EscherClientAnchorRecord
                val spgr = r

                if (model is HSSFShapeGroup) {
                    val group = model
                    group.setCoordinates(spgr.rectX1, spgr.rectY1, spgr.rectX2, spgr.rectY2)
                } else {
                    throw IllegalStateException("Got top level anchor but not processing a group")
                }
            } else if (r is EscherClientAnchorRecord) {
                /*EscherClientAnchorRecord car = (EscherClientAnchorRecord)r;

				if(model instanceof HSSFShape) {
					HSSFShape g = (HSSFShape)model;
					HSSFAnchor anchor = g.getAnchor();
					
					g.getAnchor().setDx1(car.getDx1());
					g.getAnchor().setDx2(car.getDx2());
					g.getAnchor().setDy1(car.getDy1());
					g.getAnchor().setDy2(car.getDy2());
				} else {
					throw new IllegalStateException("Got top level anchor but not processing a group or shape");
				}*/
            } else if (r is EscherTextboxRecord) {
                val tbr = r
                val obj = shapeToObj.get(tbr)
                if (obj is TextObjectRecord && model is HSSFTextbox) {
                    val textObj = obj
                    val textbox = model
                    if (!textbox.isWordArt) {
                        textbox.setString(textObj.getStr())
                    }

                    textbox.horizontalAlignment = textObj.getHorizontalTextAlignment().toShort()
                    textbox.verticalAlignment = textObj.getVerticalTextAlignment().toShort()
                }
            } else if (r is EscherClientDataRecord && model is HSSFChart) {
                val cdr = r
                val recordsList = chartToObj.get(cdr)
                HSSFChart.convertRecordsToChart(recordsList, model)
            } else if (r is EscherSpRecord) {
                // Use flags if needed
            } else if (r is EscherOptRecord) {
                // Use properties if needed
            } else {
                //System.err.println(r);
            }
        }
    }

    fun clear() {
        clearEscherRecords()
        shapeToObj.clear()
        chartToObj.clear()
        //		lastShapeId = 1024;
    }

    override fun getRecordName(): String {
        return "ESCHERAGGREGATE"
    }

    private fun convertUserModelToRecords() {
        if (patriarch != null) {
            shapeToObj.clear()
            tailRec.clear()
            chartToObj.clear()

            clearEscherRecords()
            if (patriarch!!.getChildren().size != 0) {
                convertPatriarch(patriarch!!)
                val dgContainer = getEscherRecord(0) as EscherContainerRecord
                var spgrContainer: EscherContainerRecord? = null
                val iter: MutableIterator<EscherRecord?> = dgContainer.childIterator
                while (iter.hasNext()) {
                    val child = iter.next()!!
                    if (child.recordId == EscherContainerRecord.SPGR_CONTAINER) {
                        spgrContainer = child as EscherContainerRecord
                    }
                }
                convertShapes(patriarch!!, spgrContainer!!, shapeToObj)

                patriarch = null
            }
        }
    }

    private fun convertShapes(
        parent: HSSFShapeContainer,
        escherParent: EscherContainerRecord,
        shapeToObj: MutableMap<EscherRecord?, Record?>
    ) {
        requireNotNull(escherParent) { "Parent record required" }

        val shapes = parent.getChildren()
        val iterator: MutableIterator<*> = shapes.iterator()
        while (iterator.hasNext()) {
            val shape = iterator.next() as HSSFShape?
            if (shape is HSSFShapeGroup) {
                convertGroup(shape, escherParent, shapeToObj)
            } else {
                val shapeModel = createShape(
                    shape,
                    drawingManager.allocateShapeId(drawingGroupId)
                )
                shapeToObj.put(findClientData(shapeModel.spContainer!!), shapeModel.objRecord)
                if (shapeModel is TextboxShape) {
                    val escherTextbox = shapeModel.getEscherTextbox()
                    shapeToObj.put(escherTextbox, shapeModel.textObjectRecord)

                    //					escherParent.addChildRecord(escherTextbox);
                    if (shapeModel is CommentShape) {
                        val comment = shapeModel
                        tailRec.add(comment.noteRecord)
                    }
                }
                escherParent.addChildRecord(shapeModel.spContainer)
            }
        }

        //		drawingManager.newCluster( (short)1 );
//		drawingManager.newCluster( (short)2 );
    }

    private fun convertGroup(
        shape: HSSFShapeGroup,
        escherParent: EscherContainerRecord,
        shapeToObj: MutableMap<EscherRecord?, Record?>
    ) {
        val spgrContainer = EscherContainerRecord()
        val spContainer = EscherContainerRecord()
        val spgr = EscherSpgrRecord()
        val sp = EscherSpRecord()
        val opt = EscherOptRecord()
        val anchor: EscherRecord?
        val clientData = EscherClientDataRecord()

        spgrContainer.recordId = EscherContainerRecord.SPGR_CONTAINER
        spgrContainer.options = 0x000F.toShort()
        spContainer.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer.options = 0x000F.toShort()
        spgr.recordId = EscherSpgrRecord.RECORD_ID
        spgr.options = 0x0001.toShort()
        spgr.rectX1 = shape.x1
        spgr.rectY1 = shape.y1
        spgr.rectX2 = shape.x2
        spgr.rectY2 = shape.y2
        sp.recordId = EscherSpRecord.RECORD_ID
        sp.options = 0x0002.toShort()
        val shapeId = drawingManager.allocateShapeId(drawingGroupId)
        sp.shapeId = shapeId
        if (shape.getAnchor() is HSSFClientAnchor) sp.flags =
            EscherSpRecord.FLAG_GROUP or EscherSpRecord.FLAG_HAVEANCHOR
        else sp.flags =
            EscherSpRecord.FLAG_GROUP or EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_CHILD
        opt.recordId = EscherOptRecord.RECORD_ID
        opt.options = 0x0023.toShort()
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.PROTECTION__LOCKAGAINSTGROUPING,
                0x00040004
            )
        )
        opt.addEscherProperty(EscherBoolProperty(EscherProperties.GROUPSHAPE__PRINT, 0x00080000))

        anchor = createAnchor(shape.getAnchor())
        //		clientAnchor.setCol1( ( (HSSFClientAnchor) shape.getAnchor() ).getCol1() );
//		clientAnchor.setRow1( (short) ( (HSSFClientAnchor) shape.getAnchor() ).getRow1() );
//		clientAnchor.setDx1( (short) shape.getAnchor().getDx1() );
//		clientAnchor.setDy1( (short) shape.getAnchor().getDy1() );
//		clientAnchor.setCol2( ( (HSSFClientAnchor) shape.getAnchor() ).getCol2() );
//		clientAnchor.setRow2( (short) ( (HSSFClientAnchor) shape.getAnchor() ).getRow2() );
//		clientAnchor.setDx2( (short) shape.getAnchor().getDx2() );
//		clientAnchor.setDy2( (short) shape.getAnchor().getDy2() );
        clientData.recordId = EscherClientDataRecord.RECORD_ID
        clientData.options = 0x0000.toShort()

        spgrContainer.addChildRecord(spContainer)
        spContainer.addChildRecord(spgr)
        spContainer.addChildRecord(sp)
        spContainer.addChildRecord(opt)
        spContainer.addChildRecord(anchor)
        spContainer.addChildRecord(clientData)

        val obj = ObjRecord()
        val cmo = CommonObjectDataSubRecord()
        cmo.objectType = CommonObjectDataSubRecord.Companion.OBJECT_TYPE_GROUP
        cmo.objectId = shapeId
        cmo.isLocked = true
        cmo.isPrintable = true
        cmo.isAutofill = true
        cmo.isAutoline = true
        val gmo = GroupMarkerSubRecord()
        val end = EndSubRecord()
        obj.addSubRecord(cmo)
        obj.addSubRecord(gmo)
        obj.addSubRecord(end)
        shapeToObj.put(clientData, obj)

        escherParent.addChildRecord(spgrContainer)

        convertShapes(shape, spgrContainer, shapeToObj)
    }

    private fun findClientData(spContainer: EscherContainerRecord): EscherRecord {
        val iterator: MutableIterator<EscherRecord?> = spContainer.childIterator
        while (iterator.hasNext()) {
            val r = iterator.next()!!
            if (r.recordId == EscherClientDataRecord.RECORD_ID) {
                return r
            }
        }
        throw IllegalArgumentException("Can not find client data record")
    }

    private fun convertPatriarch(patriarch: HSSFPatriarch) {
        val dgContainer = EscherContainerRecord()
        val dg: EscherDgRecord?
        val spgrContainer = EscherContainerRecord()
        val spContainer1 = EscherContainerRecord()
        val spgr = EscherSpgrRecord()
        val sp1 = EscherSpRecord()

        dgContainer.recordId = EscherContainerRecord.DG_CONTAINER
        dgContainer.options = 0x000F.toShort()
        dg = drawingManager.createDgRecord()
        drawingGroupId = dg.drawingGroupId
        //		dg.setOptions( (short) ( drawingId << 4 ) );
//		dg.setNumShapes( getNumberOfShapes( patriarch ) );
//		dg.setLastMSOSPID( 0 );  // populated after all shape id's are assigned.
        spgrContainer.recordId = EscherContainerRecord.SPGR_CONTAINER
        spgrContainer.options = 0x000F.toShort()
        spContainer1.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer1.options = 0x000F.toShort()
        spgr.recordId = EscherSpgrRecord.RECORD_ID
        spgr.options = 0x0001.toShort() // version
        spgr.rectX1 = patriarch.x1
        spgr.rectY1 = patriarch.y1
        spgr.rectX2 = patriarch.x2
        spgr.rectY2 = patriarch.y2
        sp1.recordId = EscherSpRecord.RECORD_ID
        sp1.options = 0x0002.toShort()
        sp1.shapeId = drawingManager.allocateShapeId(dg.drawingGroupId)
        sp1.flags = EscherSpRecord.FLAG_GROUP or EscherSpRecord.FLAG_PATRIARCH

        dgContainer.addChildRecord(dg)
        dgContainer.addChildRecord(spgrContainer)
        spgrContainer.addChildRecord(spContainer1)
        spContainer1.addChildRecord(spgr)
        spContainer1.addChildRecord(sp1)

        addEscherRecord(dgContainer)
    }


    companion object {
        const val sid: Short = 9876 // not a real sid - dummy value
        private val log = getLogger(EscherAggregate::class.java)

        /**
         * 
         * @param records
         * @param currentDrawingRecord
         * @return
         */
        fun shapeContainRecords(records: MutableList<*>, currentDrawingRecord: Int): Int {
            var currentDrawingRecord = currentDrawingRecord
            var count = 0
            if ((sid(records, currentDrawingRecord) == DrawingRecord.Companion.sid || sid(
                    records,
                    currentDrawingRecord
                ) == ContinueRecord.Companion.sid)
                && isObjectRecord(records, currentDrawingRecord + 1)
            ) {
                var record = records.get(currentDrawingRecord + 1) as Record
                count = 2
                //chart records
                if (record is ObjRecord && record.getSubRecords()!!
                        .get(0) is CommonObjectDataSubRecord
                ) {
                    val commonObjectDataSubRecord =
                        record.getSubRecords()!!.get(0) as CommonObjectDataSubRecord
                    if (commonObjectDataSubRecord.objectType == CommonObjectDataSubRecord.Companion.OBJECT_TYPE_CHART) {
                        //chart 
                        val chartRecordsList: MutableList<Record?> = ArrayList<Record?>()
                        currentDrawingRecord += 2
                        record = records.get(currentDrawingRecord) as Record
                        while (record.getSid() != EOFRecord.Companion.sid) {
                            chartRecordsList.add(record)

                            currentDrawingRecord++
                            count++
                            record = records.get(currentDrawingRecord) as Record
                        }
                        return count + 1
                    }
                }


                //not chart
                //note record
                if (records.get(currentDrawingRecord + 2) is NoteRecord) count++
            }
            return count
        }

        /**
         * 
         * @param records
         * @param currentIndex
         * @return
         */
        fun nextDrawingRecord(
            records: MutableList<*>,
            currentIndex: Int
        ): Int { // TODO - remove this method
            val max = records.size
            for (i in currentIndex + 1..<max) {
                val rb: Any? = records.get(i)
                if (rb !is Record) {
                    continue
                }
                val record = rb
                if (record.getSid() == DrawingRecord.Companion.sid || record.getSid() == ContinueRecord.Companion.sid) {
                    return i
                }
            }
            return -1
        }

        /**
         * Collapses the drawing records into an aggregate.
         */
        fun createAggregate(
            records: MutableList<*>,
            locFirstDrawingRecord: Int,
            drawingManager: DrawingManager2
        ): EscherAggregate {
            // Keep track of any shape records created so we can match them back to the object id's.
            // Textbox objects are also treated as shape objects.
            val shapeRecords: MutableList<EscherRecord?> = ArrayList<EscherRecord?>()
            val recordFactory: EscherRecordFactory = object : DefaultEscherRecordFactory() {
                public override fun createRecord(data: ByteArray?, offset: Int): EscherRecord {
                    val r = super.createRecord(data, offset)
                    if (r.recordId == EscherClientDataRecord.RECORD_ID || r.recordId == EscherTextboxRecord.RECORD_ID) {
                        shapeRecords.add(r)
                    }
                    return r
                }
            }

            // Calculate the size of the buffer
            val agg = EscherAggregate(drawingManager)
            var loc = locFirstDrawingRecord
            var dataSize = 0
            while (loc > -1 && loc + 1 < records.size && (sid(
                    records,
                    loc
                ) == DrawingRecord.Companion.sid || sid(
                    records,
                    loc
                ) == ContinueRecord.Companion.sid)
            ) {
                if (isObjectRecord(records, loc + 1)) {
                    if (sid(records, loc) == ContinueRecord.Companion.sid) {
                        dataSize += (records.get(loc) as ContinueRecord).getDataSize()
                    } else {
                        dataSize += (records.get(loc) as DrawingRecord).getData().size
                    }
                }
                loc = nextDrawingRecord(records, loc)
            }

            // Create one big buffer
            val buffer: ByteArray? = ByteArray(dataSize)
            var offset = 0
            var data: ByteArray?
            loc = locFirstDrawingRecord
            while (loc > -1 && loc + 1 < records.size && (sid(
                    records,
                    loc
                ) == DrawingRecord.Companion.sid || sid(
                    records,
                    loc
                ) == ContinueRecord.Companion.sid)
            ) {
                if (isObjectRecord(records, loc + 1)) {
                    if (sid(records, loc) == ContinueRecord.Companion.sid) {
                        val contd = records.get(loc) as ContinueRecord
                        data = contd.data
                    } else {
                        val drawingRecord = records.get(loc) as DrawingRecord
                        data = drawingRecord.getData()
                    }

                    if (data != null) {
                        System.arraycopy(data, 0, buffer, offset, data.size)
                        offset += data.size
                    }
                }

                loc = nextDrawingRecord(records, loc)
            }

            // Decode the shapes
            //		agg.escherRecords = new ArrayList();
            var pos = 0
            while (pos < dataSize) {
                try {
                    val r: EscherRecord = recordFactory.createRecord(buffer, pos)!!
                    val bytesRead = r.fillFields(buffer, pos, recordFactory)
                    agg.addEscherRecord(r)
                    pos += bytesRead
                } catch (e: Exception) {
                    break
                }
            }


            // Associate the object records with the shapes
            loc = locFirstDrawingRecord
            var shapeIndex = 0
            agg.shapeToObj = HashMap<EscherRecord?, Record?>()
            while (loc > -1 && loc + 1 < records.size && (sid(
                    records,
                    loc
                ) == DrawingRecord.Companion.sid || sid(
                    records,
                    loc
                ) == ContinueRecord.Companion.sid)
            ) {
                if (!isObjectRecord(records, loc + 1)) {
                    loc = nextDrawingRecord(records, loc)
                    continue
                }

                var record = records.get(loc + 1) as Record
                try {
                    //chart records
                    if (record is ObjRecord && record.getSubRecords()!!
                            .get(0) is CommonObjectDataSubRecord
                    ) {
                        val commonObjectDataSubRecord =
                            record.getSubRecords()!!.get(0) as CommonObjectDataSubRecord
                        if (commonObjectDataSubRecord.objectType == CommonObjectDataSubRecord.Companion.OBJECT_TYPE_CHART) {
                            //chart 
                            val chartRecordsList: MutableList<Record> = ArrayList<Record>()
                            loc += 2
                            record = records.get(loc) as Record
                            while (record.getSid() != EOFRecord.Companion.sid) {
                                chartRecordsList.add(record)

                                loc++
                                record = records.get(loc) as Record
                            }
                            agg.chartToObj.put(shapeRecords.get(shapeIndex++), chartRecordsList)

                            loc++
                        } else {
                            agg.shapeToObj.put(shapeRecords.get(shapeIndex++), record)
                            loc += 2
                        }
                    } else {
                        agg.shapeToObj.put(shapeRecords.get(shapeIndex++), record)
                        loc += 2
                    }
                } catch (e: Exception) {
                    break
                }
            }

            return agg
        }

        // =============== Private methods ========================
        private fun isObjectRecord(records: MutableList<*>, loc: Int): Boolean {
            return sid(records, loc) == ObjRecord.Companion.sid || sid(
                records,
                loc
            ) == TextObjectRecord.Companion.sid
        }

        private fun sid(records: MutableList<*>, loc: Int): Short {
            return (records.get(loc) as Record).getSid()
        }
    }
}
