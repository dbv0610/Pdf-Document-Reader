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

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherPropertyFactory
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hslf.record.InteractiveInfo
import com.wxiwei.office.fc.hslf.record.OEShapeAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.Record.Companion.findChildRecords
import com.wxiwei.office.fc.hslf.record.RecordTypes

/**
 * Create a `Shape` object depending on its type
 * 
 * @author Yegor Kozlov
 */
object ShapeFactory {
    // For logging
    //protected static POILogger logger = POILogFactory.getLogger(ShapeFactory.class);
    /**
     * Create a new shape from the data provided.
     */
    fun createShape(spContainer: EscherContainerRecord, parent: Shape?): Shape {
        if (spContainer.recordId == EscherContainerRecord.SPGR_CONTAINER) {
            return createShapeGroup(spContainer, parent)
        }
        return createSimpeShape(spContainer, parent)
    }

    fun createShapeGroup(spContainer: EscherContainerRecord, parent: Shape?): ShapeGroup {
        var group: ShapeGroup? = null
        val opt = ShapeKit.getEscherChild(
            spContainer.getChild(0) as EscherContainerRecord?,
            0xF122.toShort().toInt()
        )
        if (opt != null) {
            try {
                val f = EscherPropertyFactory()
                val props: MutableList<*> = f.createProperties(opt.serialize(), 8, opt.instance)
                val p = props.get(0) as EscherSimpleProperty
                if (p.propertyNumber.toInt() == 0x39F && p.propertyValue == 1) {
                    group = Table(spContainer, parent)
                } else {
                    group = ShapeGroup(spContainer, parent)
                }
            } catch (e: Exception) {
                //logger.log(POILogger.WARN, e.getMessage());
                group = ShapeGroup(spContainer, parent)
            }
        } else {
            group = ShapeGroup(spContainer, parent)
        }

        return group
    }

    fun createSimpeShape(spContainer: EscherContainerRecord, parent: Shape?): Shape {
        var shape: Shape? = null
        val spRecord = spContainer.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)

        val type = spRecord!!.options.toInt() shr 4
        when (type) {
            ShapeTypes.TextBox -> shape = TextBox(spContainer, parent)
            ShapeTypes.HostControl, ShapeTypes.PictureFrame -> {
                val info = getClientDataRecord(
                    spContainer,
                    RecordTypes.InteractiveInfo.typeID
                ) as InteractiveInfo?
                val oes = getClientDataRecord(
                    spContainer,
                    RecordTypes.OEShapeAtom.typeID
                ) as OEShapeAtom?
                if (info != null && info.interactiveInfoAtom != null) {
                    /*switch (info.getInteractiveInfoAtom().getAction())
                    {
                        case InteractiveInfoAtom.ACTION_OLE:
                            shape = new OLEShape(spContainer, parent);
                            break;
                        case InteractiveInfoAtom.ACTION_MEDIA:
                            shape = new MovieShape(spContainer, parent);
                            break;
                        default:
                            break;
                    }*/
                } else if (oes != null) {
                    shape = OLEShape(spContainer, parent)
                }
                if (shape == null) {
                    shape = Picture(spContainer, parent)
                }
            }

            ShapeTypes.Line, ShapeTypes.StraightConnector1, ShapeTypes.BentConnector2, ShapeTypes.BentConnector3, ShapeTypes.CurvedConnector3 -> shape =
                Line(spContainer, parent)

            ShapeTypes.NotPrimitive, ShapeTypes.NotchedCircularArrow -> {
                val opt = ShapeKit.getEscherChild(
                    spContainer,
                    EscherOptRecord.RECORD_ID.toInt()
                ) as EscherOptRecord?
                if (opt != null) {
                    val prop = ShapeKit.getEscherProperty(
                        opt,
                        EscherProperties.GEOMETRY__VERTICES.toInt()
                    )
                    if (prop != null) {
                        shape = Freeform(spContainer, parent)
                    }
                    /*else
                    {
    
                        shape = new AutoShape(spContainer, parent);
                    }*/
                }
            }

            else -> shape = AutoShape(spContainer, parent)
        }
        //shape id
        shape!!.shapeId = spRecord.shapeId

        return shape
    }

    internal fun getClientDataRecord(spContainer: EscherContainerRecord, recordType: Int): Record? {
        val oep: Record? = null
        val it: MutableIterator<EscherRecord?> = spContainer.childIterator
        while (it.hasNext()) {
            val obj = it.next()
            if (obj!!.recordId == EscherClientDataRecord.RECORD_ID) {
                val data = obj.serialize()
                val records: Array<Record> = findChildRecords(data, 8, data.size - 8)
                for (j in records.indices) {
                    if (records[j].getRecordType() == recordType.toLong()) {
                        return records[j]
                    }
                }
            }
        }
        return oep
    }
}
