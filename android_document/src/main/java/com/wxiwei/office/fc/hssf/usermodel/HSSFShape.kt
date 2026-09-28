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

import com.wxiwei.office.common.bg.BackgroundAndFill
import com.wxiwei.office.common.bg.Gradient
import com.wxiwei.office.common.bg.LinearGradientShader
import com.wxiwei.office.common.bg.RadialGradientShader
import com.wxiwei.office.common.bg.TileShader
import com.wxiwei.office.common.borders.Line
import com.wxiwei.office.common.picture.Picture
import com.wxiwei.office.common.shape.Arrow
import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.constant.AutoShapeConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.ss.model.XLSModel.AWorkbook
import com.wxiwei.office.system.IControl

/**
 * An abstract shape.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
abstract class HSSFShape

/**
 * Create a new shape with the specified parent and anchor.
 */ internal constructor(
    protected var escherContainer: EscherContainerRecord?,
    /**
     * Gets the parent shape.
     */
    val parent: HSSFShape?, private var anchor: HSSFAnchor?
) {
    fun checkPatriarch(): Boolean {
        var topParent = parent
        while (_patriarch == null && topParent != null) {
            _patriarch = topParent._patriarch
            topParent = topParent.parent
        }

        return _patriarch != null
    }

    fun processLineWidth() {
        this.lineWidth = ShapeKit.getLineWidth(escherContainer)
    }

    /**
     * @return  the anchor that is used by this shape.
     */
    fun getAnchor(): HSSFAnchor? {
        return anchor
    }

    /**
     * Sets a particular anchor.  A top-level shape must have an anchor of
     * HSSFClientAnchor.  A child anchor must have an anchor of HSSFChildAnchor
     * 
     * @param anchor    the anchor to use.
     * @throws IllegalArgumentException     when the wrong anchor is used for
     * this particular shape.
     * 
     * @see HSSFChildAnchor
     * 
     * @see HSSFClientAnchor
     */
    fun setAnchor(anchor: HSSFAnchor?) {
        if (parent == null) {
            require(anchor !is HSSFChildAnchor) { "Must use client anchors for shapes directly attached to sheet." }
        } else {
            require(anchor !is HSSFClientAnchor) { "Must use child anchors for shapes attached to groups." }
        }

        this.anchor = anchor
    }

    var lineStyleColor: Int
        /**
         * The color applied to the lines of this shape.
         */
        get() = _lineStyleColor
        /**
         * The color applied to the lines of this shape.
         */
        set(lineStyleColor) {
            _lineStyleColor = lineStyleColor
            _lineStyleColor = (0xFFFFFF and _lineStyleColor) or (255 shl 24)
        }

    /**
     * The color applied to the lines of this shape.
     */
    fun setLineStyleColor(red: Int, green: Int, blue: Int) {
        this._lineStyleColor =
            ((255 and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)
    }

    /**
     * The color used to fill this shape.
     */
    fun setFillColor(fillColor: Int, alpha: Int) {
        this.fillColor = fillColor
        this.fillColor = (0xFFFFFF and this.fillColor) or (alpha shl 24)
    }

    /**
     * The color used to fill this shape.
     */
    fun setFillColor(red: Int, green: Int, blue: Int, alpha: Int) {
        this.fillColor =
            ((alpha and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or ((blue and 0xFF) shl 0)
    }

    fun setFilpH(flipH: Boolean) {
        this.flipH = flipH
    }

    fun setStartArrow(type: Byte, width: Int, length: Int) {
        startArrow = Arrow(type, width, length)
    }

    fun setEndArrow(type: Byte, width: Int, length: Int) {
        endArrow = Arrow(type, width, length)
    }

    val startArrowType: Int
        get() = startArrow!!.type.toInt()

    val startArrowWidth: Int
        get() = startArrow!!.width

    val startArrowLength: Int
        get() = startArrow!!.length

    val endArrowType: Int
        get() = endArrow!!.type.toInt()

    val endArrowWidth: Int
        get() = endArrow!!.width

    val endArrowLength: Int
        get() = endArrow!!.length

    /**
     * 
     * Count of all children and their children's children.
     */
    open fun countOfAllChildren(): Int {
        return 1
    }

    fun processSimpleBackground(escherContainer: EscherContainerRecord?, workbook: AWorkbook?) {
        val opt = ShapeKit.getEscherChild(
            escherContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?

        val type = ShapeKit.getFillType(escherContainer)
        if (type == BackgroundAndFill.FILL_PICTURE.toInt()) {
            val p4 = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.FILL__PATTERNTEXTURE.toInt()
            ) as EscherSimpleProperty?
            if (p4 != null) {
                val iwb = workbook!!.getInternalWorkbook()
                val bseRecord = iwb!!.getBSERecord(p4.propertyValue)
                if (bseRecord != null) {
                    val blipRecord = bseRecord.blipRecord
                    if (blipRecord != null) {
                        this.fillType = BackgroundAndFill.FILL_PICTURE.toInt()
                        this.bGPictureData = blipRecord.picturedata
                        return
                    }
                }
            }
        } else if (type == BackgroundAndFill.FILL_PATTERN.toInt()) {
            val color = ShapeKit.getFillbackColor(
                escherContainer,
                workbook,
                MainConstant.APPLICATION_TYPE_SS.toInt()
            )
            if (color != null) {
                this.fillType = BackgroundAndFill.FILL_SOLID.toInt()
                setFillColor(color.getRGB(), 255)
                return
            }
        } else if (this.isGradientTile) {
            this.fillType = type
            return
        } else {
            val color = ShapeKit.getForegroundColor(
                escherContainer,
                workbook,
                MainConstant.APPLICATION_TYPE_SS.toInt()
            )
            if (color != null) {
                this.fillType = BackgroundAndFill.FILL_SOLID.toInt()
                setFillColor(color.getRGB(), 255)
                return
            }
        }
        this.isNoFill = true
    }

    val isGradientTile: Boolean
        get() {
            val type = ShapeKit.getFillType(escherContainer)
            return type == BackgroundAndFill.FILL_SHADE_LINEAR.toInt() || type == BackgroundAndFill.FILL_SHADE_RADIAL.toInt() || type == BackgroundAndFill.FILL_SHADE_RECT.toInt() || type == BackgroundAndFill.FILL_SHADE_SHAPE.toInt() || type == BackgroundAndFill.FILL_SHADE_TILE.toInt()
        }


    fun getGradientTileBackground(workbook: AWorkbook, control: IControl): BackgroundAndFill? {
        var bgFill: BackgroundAndFill? = null

        val type = this.fillType
        if (type == BackgroundAndFill.FILL_SHADE_LINEAR.toInt() || type == BackgroundAndFill.FILL_SHADE_RADIAL.toInt() || type == BackgroundAndFill.FILL_SHADE_RECT.toInt() || type == BackgroundAndFill.FILL_SHADE_SHAPE.toInt()) {
            bgFill = BackgroundAndFill()

            var angle = ShapeKit.getFillAngle(escherContainer)
            when (angle) {
                -90, 0 -> angle += 90
                -45 -> angle = 135
                -135 -> angle = 45
            }

            val focus = ShapeKit.getFillFocus(escherContainer)
            val fillColor =
                ShapeKit.getForegroundColor(
                    escherContainer,
                    workbook,
                    MainConstant.APPLICATION_TYPE_SS.toInt()
                )
            val fillbackColor =
                ShapeKit.getFillbackColor(
                    escherContainer,
                    workbook,
                    MainConstant.APPLICATION_TYPE_SS.toInt()
                )

            var colors: IntArray? = null
            var positions: FloatArray? = null
            if (ShapeKit.isShaderPreset(escherContainer)) {
                colors = ShapeKit.getShaderColors(escherContainer)
                positions = ShapeKit.getShaderPositions(escherContainer)
            }

            if (colors == null) {
                colors = intArrayOf(
                    if (fillColor == null) -0x1 else fillColor.getRGB(),
                    if (fillbackColor == null) -0x1 else fillbackColor.getRGB()
                )
            }
            if (positions == null) {
                positions = floatArrayOf(0f, 1f)
            }

            var gradient: Gradient? = null
            if (type == BackgroundAndFill.FILL_SHADE_LINEAR.toInt()) {
                gradient = LinearGradientShader(angle.toFloat(), colors, positions)
            } else if (type == BackgroundAndFill.FILL_SHADE_RADIAL.toInt() || type == BackgroundAndFill.FILL_SHADE_RECT.toInt() || type == BackgroundAndFill.FILL_SHADE_SHAPE.toInt()) {
                gradient =
                    RadialGradientShader(
                        ShapeKit.getRadialGradientPositionType(escherContainer),
                        colors,
                        positions
                    )
            }

            if (gradient != null) {
                gradient.focus = focus
            }

            bgFill.fillType = type.toByte()
            bgFill.shader = gradient
        } else if (type == BackgroundAndFill.FILL_SHADE_TILE.toInt()) {
            bgFill = BackgroundAndFill()

            val opt = ShapeKit.getEscherChild(
                escherContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            // 背景为图片
            val p4 = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.FILL__PATTERNTEXTURE.toInt()
            ) as EscherSimpleProperty?
            if (p4 != null) {
                val iwb = workbook!!.getInternalWorkbook()
                val bseRecord = iwb!!.getBSERecord(p4.propertyValue)
                if (bseRecord != null) {
                    val blipRecord = bseRecord.blipRecord
                    if (blipRecord != null) {
                        bgFill.fillType = BackgroundAndFill.FILL_SHADE_TILE
                        val pic = Picture()
                        pic.data = blipRecord.picturedata

                        control.getSysKit().getPictureManage().addPicture(pic)

                        bgFill.shader = TileShader(pic, TileShader.Flip_None, 1f, 1.0f)
                    }
                }
            }
        }

        return bgFill
    }

    val line: Line
        /**
         * 
         * @return
         */
        get() {
            val lineFill = BackgroundAndFill()
            lineFill.foregroundColor = _lineStyleColor

            val line =
                Line()
            line.backgroundAndFill = lineFill
            line.lineWidth = this.lineWidth
            line.isDash = this.lineStyle > AutoShapeConstant.LINESTYLE_SOLID
            return line
        }

    var _patriarch: HSSFPatriarch? = null

    var shapeType: Int = ShapeTypes.NotPrimitive

    /**
     * 
     * @return
     */
    /**
     * 
     * @param noBorder
     */
    var isNoBorder: Boolean = false
    private var _lineStyleColor = 0x08000040
    /**
     * @return  returns with width of the line in EMUs.  12700 = 1 pt.
     */
    /**
     * Sets the width of the line.  12700 = 1 pt.
     * 
     * @param lineWidth width in EMU's.  12700EMU's = 1 pt
     * 
     * @see HSSFShape.LINEWIDTH_ONE_PT
     */
    var lineWidth: Int = AutoShapeConstant.LINEWIDTH_DEFAULT // 12700 = 1pt
    /**
     * @return One of the constants in LINESTYLE_*
     */
    /**
     * Sets the line style.
     * 
     * @param lineStyle     One of the constants in LINESTYLE_*
     */
    var lineStyle: Int = AutoShapeConstant.LINESTYLE_SOLID

    /**
     * @return `true` if this shape is not filled with a color.
     */
    /**
     * Sets whether this shape is filled or transparent.
     */
    var isNoFill: Boolean = false
    /**
     * 
     * @return
     */
    /**
     * 
     * @param fillType
     */
    var fillType: Int = 0

    /**
     * The color used to fill this shape.
     */
    var fillColor: Int = 0x08000009
        private set

    var bGPictureData: ByteArray? = null

    var rotation: Int = 0
    var flipH: Boolean = false
        private set
    var flipV: Boolean = false

    // start arror or not
    private var startArrow: Arrow? = null

    // end arror or not
    private var endArrow: Arrow? = null

    companion object {
        fun toClientAnchor(anchorRecord: EscherClientAnchorRecord): HSSFClientAnchor {
            val anchor = HSSFClientAnchor()
            anchor.anchorType = anchorRecord.flag.toInt()
            anchor.setCol1(anchorRecord.col1)
            anchor.setCol2(anchorRecord.col2)
            anchor.dx1 = anchorRecord.dx1.toInt()
            anchor.dx2 = anchorRecord.dx2.toInt()
            anchor.dy1 = anchorRecord.dy1.toInt()
            anchor.dy2 = anchorRecord.dy2.toInt()
            anchor.row1 = anchorRecord.row1.toInt()
            anchor.row2 = anchorRecord.row2.toInt()
            return anchor
        }

        fun toChildAnchor(anchorRecord: EscherChildAnchorRecord): HSSFChildAnchor {
            val anchor = HSSFChildAnchor(
                anchorRecord.dx1,
                anchorRecord.dy1,
                anchorRecord.dx2,
                anchorRecord.dy2
            )

            return anchor
        }
    }
}
