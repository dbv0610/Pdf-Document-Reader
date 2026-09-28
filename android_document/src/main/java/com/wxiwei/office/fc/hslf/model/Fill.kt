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

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherBSERecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.hslf.usermodel.PictureData
import com.wxiwei.office.java.awt.Color

/**
 * Represents functionality provided by the 'Fill Effects' dialog in PowerPoint.
 * 
 * @author Yegor Kozlov
 */
class Fill

/**
 * Construct a `Fill` object for a shape.
 * Fill information will be read from shape's escher properties.
 * 
 * @param shape the shape this background applies to
 */(
    /**
     * The shape this background applies to
     */
    protected var shape: Shape?
) {
    var fillType: Int
        /**
         * Returns fill type.
         * Must be one of the `FILL_*` constants defined in this class.
         * 
         * @return type of fill
         */
        get() = ShapeKit.getFillType(shape!!.spContainer)
        /**
         * Sets fill type.
         * Must be one of the `FILL_*` constants defined in this class.
         * 
         * @param type type of the fill
         */
        set(type) {
            val opt = ShapeKit.getEscherChild(
                shape!!.spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            Shape.setEscherProperty(
                opt,
                EscherProperties.FILL__FILLTYPE,
                type
            )
        }

    val fillAngle: Int
        /**
         * 
         * @return
         */
        get() = ShapeKit.getFillAngle(shape!!.spContainer)

    val fillFocus: Int
        /**
         * 
         * @return
         */
        get() = ShapeKit.getFillFocus(shape!!.spContainer)

    val isShaderPreset: Boolean
        get() = ShapeKit.isShaderPreset(shape!!.spContainer)

    val shaderColors: IntArray?
        get() = ShapeKit.getShaderColors(shape!!.spContainer)

    val shaderPositions: FloatArray?
        get() = ShapeKit.getShaderPositions(shape!!.spContainer)

    val radialGradientPositionType: Int
        /**
         * 
         * @return
         */
        get() = ShapeKit.getRadialGradientPositionType(shape!!.spContainer)

    var foregroundColor: Color?
        /**
         * Foreground color
         */
        get() = ShapeKit.getForegroundColor(
            shape!!.spContainer,
            shape!!.sheet,
            MainConstant.APPLICATION_TYPE_PPT.toInt()
        )
        /**
         * Foreground color
         */
        set(color) {
            val opt = ShapeKit.getEscherChild(
                shape!!.spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            if (color == null) {
                Shape.setEscherProperty(
                    opt,
                    EscherProperties.FILL__NOFILLHITTEST,
                    0x150000
                )
            } else {
                val rgb = Color(
                    color.getBlue(),
                    color.getGreen(),
                    color.getRed(),
                    0
                ).getRGB()
                Shape.setEscherProperty(
                    opt,
                    EscherProperties.FILL__FILLCOLOR,
                    rgb
                )
                Shape.setEscherProperty(
                    opt,
                    EscherProperties.FILL__NOFILLHITTEST,
                    0x150011
                )
            }
        }

    val fillbackColor: Color?
        /**
         * Background color
         */
        get() = ShapeKit.getFillbackColor(
            shape!!.spContainer,
            shape!!.sheet,
            MainConstant.APPLICATION_TYPE_PPT.toInt()
        )

    val pictureData: PictureData?
        /**
         * `PictureData` object used in a texture, pattern of picture fill.
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                shape!!.spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?

            val ep = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.FILL__PATTERNTEXTURE.toInt()
            )
            if (ep == null || ep !is EscherSimpleProperty) {
                return null
            }

            val p = ep

            val ppt =
                shape!!.sheet!!.slideShow!!
            val pict =
                ppt.pictureData!!
            val doc = ppt.documentRecord!!

            val dggContainer = doc.pPDrawingGroup!!.dggContainer
            val bstore = ShapeKit.getEscherChild(
                dggContainer,
                EscherContainerRecord.BSTORE_CONTAINER.toInt()
            ) as EscherContainerRecord?

            if (bstore != null) {
                val lst = bstore.childRecords
                val idx = (p.propertyValue and 0xFFFF)
                if (idx == 0) {
                    //logger.log(POILogger.WARN, "no reference to picture data found ");
                } else {
                    val bse = lst.get(idx - 1) as EscherBSERecord
                    for (i in pict.indices) {
                        if (pict[i]!!.offset == bse.offset) {
                            return pict[i]
                        }
                    }
                }
            }
            return null
        }

    /** */

    /**
     * Background color
     */
    fun setBackgroundColor(color: Color?) {
        val opt = ShapeKit.getEscherChild(
            shape!!.spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        if (color == null) {
            Shape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, -1)
        } else {
            val rgb = Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB()
            Shape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, rgb)
        }
    }

    /**
     * Assign picture used to fill the underlying shape.
     * 
     * @param idx 0-based index of the picture added to this ppt by `SlideShow.addPicture` method.
     */
    fun setPictureData(idx: Int) {
        val opt = ShapeKit.getEscherChild(
            shape!!.spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        Shape.setEscherProperty(
            opt,
            (EscherProperties.FILL__PATTERNTEXTURE + 0x4000).toShort(),
            idx
        )
    }

    /**
     * 
     */
    fun dispose() {
        shape = null
    }
}
