/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           HSSFShapeFactory.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:23:36
 */
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherProperty
import com.wxiwei.office.fc.ddf.EscherPropertyFactory
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherSpgrRecord
import com.wxiwei.office.fc.hssf.record.CommonObjectDataSubRecord
import com.wxiwei.office.fc.hssf.record.EmbeddedObjectRefSubRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.record.Record
import com.wxiwei.office.fc.hssf.record.SubRecord
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2013-4-10
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
object HSSFShapeFactory {
    fun createShape(
        workbook: AWorkbook?, shapeToObj: MutableMap<EscherRecord?, Record?>?,
        spContainer: EscherContainerRecord, parent: HSSFShape?
    ): HSSFShape? {
        if (spContainer.recordId == EscherContainerRecord.SPGR_CONTAINER) {
            return createShapeGroup(workbook, shapeToObj, spContainer, parent)
        }
        return createSimpeShape(workbook, shapeToObj, spContainer, parent)
    }

    fun createShapeGroup(
        workbook: AWorkbook?, shapeToObj: MutableMap<EscherRecord?, Record?>?,
        spContainer: EscherContainerRecord, parent: HSSFShape?
    ): HSSFShapeGroup? {
        var group: HSSFShapeGroup? = null
        val childRecords = spContainer.childRecords
        if (childRecords.size > 0) {
            val groupContainer = childRecords.get(0) as EscherContainerRecord

            var anchor: HSSFAnchor? = null
            if (parent == null) {
                val anchorRecord = ShapeKit.getEscherChild(
                    groupContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
                ) as EscherClientAnchorRecord?
                if (anchorRecord != null && anchorRecord.col2 <= 255 && anchorRecord.row2 <= 65535) {
                    anchor = HSSFShape.toClientAnchor(anchorRecord)
                }
            } else {
                val childRecord = ShapeKit.getEscherChild(
                    groupContainer, EscherChildAnchorRecord.RECORD_ID.toInt()
                ) as EscherChildAnchorRecord?
                if (childRecord != null) {
                    anchor = HSSFShape.toChildAnchor(childRecord)
                }
            }
            if (anchor == null) {
                anchor = HSSFClientAnchor()
            }


            val opt = ShapeKit.getEscherChild(groupContainer, 0xF122.toShort().toInt())
            if (opt != null) {
                val f = EscherPropertyFactory()
                val props = f.createProperties(opt.serialize(), 8, opt.instance)
                val p = props.get(0) as EscherSimpleProperty
                if (p.propertyNumber.toInt() != 0x39F || p.propertyValue != 1) {
                    group = HSSFShapeGroup(groupContainer, parent, anchor)
                }
            } else {
                group = HSSFShapeGroup(groupContainer, parent, anchor)
            }

            val spgrRecord = ShapeKit.getEscherChild(
                groupContainer, EscherSpgrRecord.RECORD_ID.toInt()
            ) as EscherSpgrRecord?
            if (spgrRecord != null) {
                group!!.setCoordinates(
                    spgrRecord.rectX1,
                    spgrRecord.rectY1, spgrRecord.rectX2, spgrRecord.rectY2
                )
            }

            for (i in 1..<childRecords.size) {
                val shape = HSSFShapeFactory.createShape(
                    workbook,
                    shapeToObj,
                    (childRecords.get(i) as EscherContainerRecord?)!!,
                    group
                )
                if (shape != null) {
                    group!!.addChildShape(shape)
                }
            }
        }
        return group
    }

    fun createSimpeShape(
        workbook: AWorkbook?, shapeToObj: MutableMap<EscherRecord?, Record?>?,
        spContainer: EscherContainerRecord, parent: HSSFShape?
    ): HSSFShape? {
        var shape: HSSFShape? = null
        var anchor: HSSFAnchor? = null

        if (parent == null) {
            val anchorRecord = ShapeKit.getEscherChild(
                spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
            ) as EscherClientAnchorRecord?
            if (anchorRecord != null && anchorRecord.col2 <= 255 && anchorRecord.row2 <= 65535) {
                anchor = HSSFShape.toClientAnchor(anchorRecord)
            }
        } else {
            val childRecord = ShapeKit.getEscherChild(
                spContainer, EscherChildAnchorRecord.RECORD_ID.toInt()
            ) as EscherChildAnchorRecord?
            if (childRecord != null) {
                anchor = HSSFShape.toChildAnchor(childRecord)
            }
        }


        val spRecord = spContainer.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
        if (spRecord == null) {
            return null
        }

        val type = spRecord.options.toInt() shr 4
        when (type) {
            ShapeTypes.TextBox -> {
                if (shapeToObj != null && shapeToObj.size > 0) {
                    val escherClientDataRecord =
                        ShapeKit.getEscherChild(
                            spContainer,
                            EscherClientDataRecord.RECORD_ID.toInt()
                        ) as EscherClientDataRecord?
                    val record = shapeToObj.get(escherClientDataRecord)

                    if (record is ObjRecord && record.getSubRecords()!!.get(0) is CommonObjectDataSubRecord) {
                        val commonObjectDataSubRecord =
                            record.getSubRecords()!!.get(0) as CommonObjectDataSubRecord
                        if (commonObjectDataSubRecord.objectType != CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT) {
                            //except comment
                            shape = HSSFAutoShape(workbook, spContainer, parent, anchor, type)
                        }
                    }
                } else {
                    // falls through to PictureFrame handling (as in the original switch)
                    shape = createPicture(workbook, spContainer, parent, anchor)
                }
            }

            ShapeTypes.PictureFrame -> {
                shape = createPicture(workbook, spContainer, parent, anchor)
            }

            ShapeTypes.HostControl ->                 //chart ,参考 HSSFChart.getSheetCharts                
                shape = HSSFChart(workbook, spContainer, parent, anchor)

            ShapeTypes.Line, ShapeTypes.StraightConnector1, ShapeTypes.BentConnector2, ShapeTypes.BentConnector3, ShapeTypes.CurvedConnector3 -> shape =
                HSSFLine(workbook, spContainer, parent, anchor, type)

            ShapeTypes.NotPrimitive, ShapeTypes.NotchedCircularArrow -> shape =
                HSSFFreeform(workbook, spContainer, parent, anchor, type)

            else -> {
                shape = HSSFAutoShape(workbook, spContainer, parent, anchor, type)
                shape.setAdjustmentValue(spContainer)
            }
        }
        return shape
    }


    //    /**
    //     * build shape tree from escher container
    //     * @param container root escher container from which escher records must be taken
    //     * @param agg - EscherAggregate
    //     * @param out - shape container to which shapes must be added
    //     * @param root - node to create HSSFObjectData shapes
    //     */
    //    public static void createShapeTree(EscherContainerRecord container, Map<EscherRecord, Record> shapeToObj, HSSFShapeContainer out, DirectoryNode root)
    //    {
    //        if (container.getRecordId() == EscherContainerRecord.SPGR_CONTAINER) 
    //        {
    //            ObjRecord obj = null;
    //            EscherClientDataRecord clientData = ((EscherContainerRecord) container.getChild(0)).getChildById(EscherClientDataRecord.RECORD_ID);
    //            if (null != clientData)
    //            {
    //                obj = (ObjRecord) shapeToObj.get(clientData);
    //            }
    //            HSSFShapeGroup group = new HSSFShapeGroup(container, obj);
    //            List<EscherContainerRecord> children = container.getChildContainers();
    //            // skip the first child record, it is group descriptor
    //            for (int i = 0; i < children.size(); i++) 
    //            {
    //                EscherContainerRecord spContainer = children.get(i);
    //                if (i != 0)
    //                {
    //                    createShapeTree(spContainer, shapeToObj, group, root);
    //                }
    //            }
    //            out.addShape(group);
    //        } 
    //        else if (container.getRecordId() == EscherContainerRecord.SP_CONTAINER)
    //        {
    //            ObjRecord objRecord = null;
    //            TextObjectRecord txtRecord = null;
    //
    //            for (EscherRecord record : container.getChildRecords()) {
    //                switch (record.getRecordId()) 
    //                {
    //                    case EscherClientDataRecord.RECORD_ID:
    //                        objRecord = (ObjRecord) shapeToObj.get(record);
    //                        break;
    //                    case EscherTextboxRecord.RECORD_ID:
    //                        txtRecord = (TextObjectRecord) shapeToObj.get(record);
    //                        break;
    //                }
    //            }
    //            if (isEmbeddedObject(objRecord))
    //            {
    //                HSSFObjectData objectData = new HSSFObjectData(container, objRecord, root);
    //                out.addShape(objectData);
    //                return;
    //            }
    //            CommonObjectDataSubRecord cmo = (CommonObjectDataSubRecord) objRecord.getSubRecords().get(0);
    //            HSSFShape shape;
    //            switch (cmo.getObjectType()) 
    //            {
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_PICTURE:
    //                    shape = new HSSFPicture(container, objRecord);
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_RECTANGLE:
    //                    shape = new HSSFSimpleShape(container, objRecord, txtRecord);
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_LINE:
    //                    shape = new HSSFSimpleShape(container, objRecord);
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_COMBO_BOX:
    //                    shape = new HSSFCombobox(container, objRecord);
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING:
    //                    EscherOptRecord optRecord = container.getChildById(EscherOptRecord.RECORD_ID);
    //                    EscherProperty property = optRecord.lookup(EscherProperties.GEOMETRY__VERTICES);
    //                    if (null != property)
    //                    {
    //                        shape = new HSSFPolygon(container, objRecord, txtRecord);
    //                    } 
    //                    else 
    //                    {
    //                        shape = new HSSFSimpleShape(container, objRecord, txtRecord);
    //                    }
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_TEXT:
    //                    shape = new HSSFTextbox(container, objRecord, txtRecord);
    //                    break;
    //                case CommonObjectDataSubRecord.OBJECT_TYPE_COMMENT:
    //                    shape = new HSSFComment(container, objRecord, txtRecord, agg.getNoteRecordByObj(objRecord));
    //                    break;
    //                default:
    //                    shape = new HSSFSimpleShape(container, objRecord, txtRecord);
    //            }
    //            out.addShape(shape);
    //        }
    //    }
    private fun createPicture(
        workbook: AWorkbook?, spContainer: EscherContainerRecord,
        parent: HSSFShape?, anchor: HSSFAnchor?
    ): HSSFPicture {
        val opt = ShapeKit.getEscherChild(
            spContainer, EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        val prop = opt!!.lookup<EscherProperty>(
            EscherProperties.BLIP__BLIPTODISPLAY.toInt()
        ) as EscherSimpleProperty?
        val shape = HSSFPicture(workbook, spContainer, parent, anchor, opt)
        if (prop != null) {
            shape.pictureIndex = prop.propertyValue
        }
        return shape
    }

    private fun isEmbeddedObject(obj: ObjRecord): Boolean {
        val subRecordIter: MutableIterator<SubRecord> = obj.getSubRecords()!!.iterator()
        while (subRecordIter.hasNext()) {
            val sub = subRecordIter.next()
            if (sub is EmbeddedObjectRefSubRecord) {
                return true
            }
        }
        return false
    }
}
