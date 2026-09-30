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

/**
 * Represents an AutoShape.
 * 
 * 
 * AutoShapes are drawing objects with a particular shape that may be customized through smart resizing and adjustments.
 * See [ShapeTypes]
 * 
 * 
 * @author Yegor Kozlov
 */
open class AutoShape : TextShape {
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    override fun setDefaultTextProperties(_txtrun: TextRun?) {
        verticalAlignment = TextShape.AnchorMiddle
        horizontalAlignment = TextShape.AlignCenter
        wordWrap = TextShape.WrapNone
    }


    /** */
    @JvmOverloads
    constructor(type: Int, parent: Shape? = null) : super(null, parent) {
        spContainer = createSpContainer(type, parent is ShapeGroup)
    }

    protected fun createSpContainer(shapeType: Int, isChild: Boolean): EscherContainerRecord? {
        spContainer = super.createSpContainer(isChild)

        this.shapeType = shapeType

        //set default properties for an autoshape
        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x40000)
        setEscherProperty(EscherProperties.FILL__FILLCOLOR, 0x8000004)
        setEscherProperty(EscherProperties.FILL__FILLCOLOR, 0x8000004)
        setEscherProperty(EscherProperties.FILL__FILLBACKCOLOR, 0x8000000)
        setEscherProperty(EscherProperties.FILL__NOFILLHITTEST, 0x100010)
        setEscherProperty(EscherProperties.LINESTYLE__COLOR, 0x8000001)
        setEscherProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80008)
        setEscherProperty(EscherProperties.SHADOWSTYLE__COLOR, 0x8000002)

        return spContainer
    }

    /**
     * Gets adjust value which controls smart resizing of the auto-shape.
     * 
     * 
     * 
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     * 
     * 
     * @param idx the adjust index in the [0, 9] range
     * @return the adjustment value
     */
    fun getAdjustmentValue(idx: Int): Int {
        require(!(idx < 0 || idx > 9)) { "The index of an adjustment value must be in the [0, 9] range" }
        return ShapeKit.getEscherProperty(
            spContainer,
            (EscherProperties.GEOMETRY__ADJUSTVALUE + idx).toShort()
        )
    }

    /**
     * Sets adjust value which controls smart resizing of the auto-shape.
     * 
     * 
     * 
     * The adjustment values are given in shape coordinates:
     * the origin is at the top-left, positive-x is to the right, positive-y is down.
     * The region from (0,0) to (S,S) maps to the geometry box of the shape (S=21600 is a constant).
     * 
     * 
     * @param idx the adjust index in the [0, 9] range
     * @param val the adjustment value
     */
    fun setAdjustmentValue(idx: Int, `val`: Int) {
        require(!(idx < 0 || idx > 9)) { "The index of an adjustment value must be in the [0, 9] range" }
        setEscherProperty((EscherProperties.GEOMETRY__ADJUSTVALUE + idx).toShort(), `val`)
    }

    override val outline: com.wxiwei.office.java.awt.Shape
        get() {
            val outline = AutoShapes.getShapeOutline(shapeType)
            val anchor = logicalAnchor2D
            if (outline == null) {
                return anchor
            }
            val shape = outline.getOutline(this)
            return AutoShapes.transform(shape, anchor)!!
        }

    override fun dispose() {
        super.dispose()
    }
}
