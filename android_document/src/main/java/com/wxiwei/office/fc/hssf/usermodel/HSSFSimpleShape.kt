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
package com.wxiwei.office.fc.hssf.usermodel

import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.ss.model.XLSModel.AWorkbook

/**
 * Represents a simple shape such as a line, rectangle or oval.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
open class HSSFSimpleShape  //    public final static short       OBJECT_TYPE_MICROSOFT_OFFICE_DRAWING = 30;
    (escherContainer: EscherContainerRecord?, parent: HSSFShape?, anchor: HSSFAnchor?) :
    HSSFShape(escherContainer, parent, anchor) {
    fun processLine(escherContainer: EscherContainerRecord?, workbook: AWorkbook?) {
        if (ShapeKit.hasLine(escherContainer)) {
            val color = ShapeKit.getLineColor(
                escherContainer,
                workbook,
                MainConstant.APPLICATION_TYPE_SS.toInt()
            )
            if (color != null) {
                lineStyleColor = color.getRGB()
            } else {
                isNoBorder = true
            }

            lineStyle = ShapeKit.getLineDashing(escherContainer)
        } else {
            isNoBorder = true
        }
    }

    fun processArrow(escherContainer: EscherContainerRecord?) {
        setStartArrow(
            ShapeKit.getStartArrowType(escherContainer).toByte(),
            ShapeKit.getStartArrowWidth(escherContainer),
            ShapeKit.getStartArrowLength(escherContainer)
        )

        setEndArrow(
            ShapeKit.getEndArrowType(escherContainer).toByte(),
            ShapeKit.getEndArrowWidth(escherContainer),
            ShapeKit.getEndArrowLength(escherContainer)
        )
    }

    fun processRotationAndFlip(escherContainer: EscherContainerRecord?) {
        rotation = ShapeKit.getRotation(escherContainer)
        setFilpH(ShapeKit.getFlipHorizontal(escherContainer))
        flipV = ShapeKit.getFlipVertical(escherContainer)
    }

    companion object {
        // The commented out ones haven't been tested yet or aren't supported
        // by HSSFSimpleShape.
        const val OBJECT_TYPE_LINE: Short = 1
        const val OBJECT_TYPE_RECTANGLE: Short = 2
        const val OBJECT_TYPE_OVAL: Short = 3

        //    public final static short       OBJECT_TYPE_ARC                = 4;
        //    public final static short       OBJECT_TYPE_CHART              = 5;
        //    public final static short       OBJECT_TYPE_TEXT               = 6;
        //    public final static short       OBJECT_TYPE_BUTTON             = 7;
        const val OBJECT_TYPE_PICTURE: Short = 8

        //    public final static short       OBJECT_TYPE_POLYGON            = 9;
        //    public final static short       OBJECT_TYPE_CHECKBOX           = 11;
        //    public final static short       OBJECT_TYPE_OPTION_BUTTON      = 12;
        //    public final static short       OBJECT_TYPE_EDIT_BOX           = 13;
        //    public final static short       OBJECT_TYPE_LABEL              = 14;
        //    public final static short       OBJECT_TYPE_DIALOG_BOX         = 15;
        //    public final static short       OBJECT_TYPE_SPINNER            = 16;
        //    public final static short       OBJECT_TYPE_SCROLL_BAR         = 17;
        //    public final static short       OBJECT_TYPE_LIST_BOX           = 18;
        //    public final static short       OBJECT_TYPE_GROUP_BOX          = 19;
        const val OBJECT_TYPE_COMBO_BOX: Short = 20
        const val OBJECT_TYPE_COMMENT: Short = 25
    }
}
