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
package com.wxiwei.office.fc.hssf.model

import com.wxiwei.office.constant.AutoShapeConstant
import com.wxiwei.office.fc.ddf.EscherBoolProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRGBProperty
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hssf.record.ObjRecord
import com.wxiwei.office.fc.hssf.usermodel.HSSFAnchor
import com.wxiwei.office.fc.hssf.usermodel.HSSFComment
import com.wxiwei.office.fc.hssf.usermodel.HSSFPolygon
import com.wxiwei.office.fc.hssf.usermodel.HSSFShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFSimpleShape
import com.wxiwei.office.fc.hssf.usermodel.HSSFTextbox

/**
 * An abstract shape is the lowlevel model for a shape.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
abstract class AbstractShape
protected constructor() {
    /**
     * @return  The shape container and it's children that can represent this
     * shape.
     */
    abstract val spContainer: EscherContainerRecord?

    /**
     * @return  The object record that is associated with this shape.
     */
    abstract val objRecord: ObjRecord?

    /**
     * Creates an escher anchor record from a HSSFAnchor.
     * 
     * @param userAnchor    The high level anchor to convert.
     * @return  An escher anchor record.
     */
    protected fun createAnchor(userAnchor: HSSFAnchor?): EscherRecord {
        return ConvertAnchor.createAnchor(userAnchor)
    }

    /**
     * Add standard properties to the opt record.  These properties effect
     * all records.
     * 
     * @param shape     The user model shape.
     * @param opt       The opt record to add the properties to.
     * @return          The number of options added.
     */
    protected open fun addStandardOptions(shape: HSSFShape, opt: EscherOptRecord): Int {
        opt.addEscherProperty(
            EscherBoolProperty(
                EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE,
                0x080000
            )
        )
        //        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x080008 ) );
        if (shape.isNoFill) {
            // Wonderful... none of the spec's give any clue as to what these constants mean.
            opt.addEscherProperty(
                EscherBoolProperty(
                    EscherProperties.FILL__NOFILLHITTEST,
                    0x00110000
                )
            )
        } else {
            opt.addEscherProperty(
                EscherBoolProperty(
                    EscherProperties.FILL__NOFILLHITTEST,
                    0x00010000
                )
            )
        }
        opt.addEscherProperty(
            EscherRGBProperty(
                EscherProperties.FILL__FILLCOLOR,
                shape.fillColor
            )
        )
        opt.addEscherProperty(EscherBoolProperty(EscherProperties.GROUPSHAPE__PRINT, 0x080000))
        opt.addEscherProperty(
            EscherRGBProperty(
                EscherProperties.LINESTYLE__COLOR,
                shape.lineStyleColor
            )
        )
        var options = 5
        if (shape.lineWidth != AutoShapeConstant.LINEWIDTH_DEFAULT) {
            opt.addEscherProperty(
                EscherSimpleProperty(
                    EscherProperties.LINESTYLE__LINEWIDTH,
                    shape.lineWidth
                )
            )
            options++
        }
        if (shape.lineStyle != AutoShapeConstant.LINESTYLE_SOLID) {
            opt.addEscherProperty(
                EscherSimpleProperty(
                    EscherProperties.LINESTYLE__LINEDASHING,
                    shape.lineStyle
                )
            )
            opt.addEscherProperty(
                EscherSimpleProperty(
                    EscherProperties.LINESTYLE__LINEENDCAPSTYLE,
                    0
                )
            )
            if (shape.lineStyle == AutoShapeConstant.LINESTYLE_NONE) opt.addEscherProperty(
                EscherBoolProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080000)
            )
            else opt.addEscherProperty(
                EscherBoolProperty(
                    EscherProperties.LINESTYLE__NOLINEDRAWDASH,
                    0x00080008
                )
            )
            options += 3
        }
        opt.sortProperties()
        return options // # options added
    }

    /**
     * Generate id for the CommonObjectDataSubRecord that stands behind this shape
     * 
     * 
     * 
     * Typically objectId starts with 1, is unique among all Obj record within the worksheet stream
     * and increments by 1 for every new shape.
     * For most shapes there is a straight relationship between shapeId (generated by DDF) and objectId:
     * 
     * 
     * 
     * shapeId  is unique and starts with 1024, hence objectId can be derived as `shapeId-1024`.
     * 
     * 
     * 
     * An exception from this rule is the CellComment shape whose objectId start with 1024.
     * See [CommentShape.getCmoObjectId]
     * 
     * 
     * 
     * 
     * @param  shapeId   shape id as generated by drawing manager
     * @return objectId  object id that will be assigned to the Obj record
     */
    open fun getCmoObjectId(shapeId: Int): Int {
        return shapeId - 1024
    }

    companion object {
        /**
         * Create a new shape object used to create the escher records.
         * 
         * @param hssfShape     The simple shape this is based on.
         */
        @JvmStatic
        fun createShape(hssfShape: HSSFShape?, shapeId: Int): AbstractShape {
            val shape: AbstractShape
            if (hssfShape is HSSFComment) {
                shape = CommentShape(hssfShape, shapeId)
            } else if (hssfShape is HSSFTextbox) {
                shape = TextboxShape(hssfShape, shapeId)
            } else if (hssfShape is HSSFPolygon) {
                shape = PolygonShape(hssfShape, shapeId)
            } else if (hssfShape is HSSFSimpleShape) {
                val simpleShape = hssfShape
                when (simpleShape.shapeType) {
                    HSSFSimpleShape.OBJECT_TYPE_PICTURE.toInt() -> shape =
                        PictureShape(simpleShape, shapeId)

                    HSSFSimpleShape.OBJECT_TYPE_LINE.toInt() -> shape = LineShape(simpleShape, shapeId)
                    HSSFSimpleShape.OBJECT_TYPE_OVAL.toInt(), HSSFSimpleShape.OBJECT_TYPE_RECTANGLE.toInt() -> shape =
                        SimpleFilledShape(simpleShape, shapeId)

                    HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX.toInt() -> shape =
                        ComboboxShape(simpleShape, shapeId)

                    else -> throw IllegalArgumentException("Do not know how to handle this type of shape")
                }
            } else {
                throw IllegalArgumentException("Unknown shape type")
            }
            val sp = shape.spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            if (hssfShape.parent != null) sp!!.flags = sp.flags or EscherSpRecord.FLAG_CHILD
            return shape
        }
    }
}
