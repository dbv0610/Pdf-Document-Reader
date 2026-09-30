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

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.java.awt.geom.Line2D

/**
 * Represents a line in a PowerPoint drawing
 * 
 * @author Yegor Kozlov
 */
class Line : SimpleShape {
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    override val adjustmentValue: Array<Float?>?
        get() = ShapeKit.getAdjustmentValue(spContainer)


    /** */
    @JvmOverloads
    constructor(parent: Shape? = null) : super(null, parent) {
        spContainer = createSpContainer(parent is ShapeGroup)
    }

    override fun createSpContainer(isChild: Boolean): EscherContainerRecord? {
        spContainer = super.createSpContainer(isChild)

        val spRecord = spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
        val type = ((ShapeTypes.Line shl 4) or 0x2).toShort()
        spRecord!!.options = type

        //set default properties for a line
        val opt = ShapeKit.getEscherChild(
            spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?

        //default line properties
        Shape.setEscherProperty(opt, EscherProperties.GEOMETRY__SHAPEPATH, 4)
        Shape.setEscherProperty(opt, EscherProperties.GEOMETRY__FILLOK, 0x10000)
        Shape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x100000)
        Shape.setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, 0x8000001)
        Shape.setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0xA0008)
        Shape.setEscherProperty(opt, EscherProperties.SHADOWSTYLE__COLOR, 0x8000002)

        return spContainer
    }

    override val outline: com.wxiwei.office.java.awt.Shape
        get() {
            val anchor = logicalAnchor2D
            return Line2D.Double(
                anchor.getX(), anchor.getY(), anchor.getX() + anchor.getWidth(),
                anchor.getY() + anchor.getHeight()
            )
        }

    override fun dispose() {
        super.dispose()
    }
}
