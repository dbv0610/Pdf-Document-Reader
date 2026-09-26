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
import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherProperty
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * 
 * 
 * Represents a Shape which is the elemental object that composes a drawing.
 * This class is a wrapper around EscherSpContainer which holds all information
 * about a shape in PowerPoint document.
 * 
 * 
 * 
 * When you add a shape, you usually specify the dimensions of the shape and the position
 * of the upper'left corner of the bounding box for the shape relative to the upper'left
 * corner of the page, worksheet, or slide. Distances in the drawing layer are measured
 * in points (72 points = 1 inch).
 * 
 * 
 * 
 * 
 * @author Yegor Kozlov
 */
abstract class Shape

/**
 * Create a Shape object. This constructor is used when an existing Shape is read from from a PowerPoint document.
 * 
 * @param escherRecord       `EscherSpContainer` container which holds information about this shape
 * @param parent             the parent of this Shape
 */ protected constructor(
    /**
     * Either EscherSpContainer or EscheSpgrContainer record
     * which holds information about this shape.
     */
    var spContainer: EscherContainerRecord?,
    /**
     * Parent of this shape.
     * `null` for the topmost shapes.
     */
    var parent: Shape?
) {
    open var shapeType: Int
        /**
         * @return type of the shape.
         */
        get() = ShapeKit.getShapeType(this.spContainer)
        /**
         * @param type type of the shape.
         * @see RecordTypes
         */
        set(type) {
            val spRecord =
                spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            spRecord!!.options = (type shl 4 or 0x2).toShort()
        }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getShapeIdProperty")
    open var shapeId: Int
        /**
         * @return id for the shape.
         */
        get() = ShapeKit.getShapeId(this.spContainer)
        /**
         * Sets shape ID
         * 
         * @param id of the shape
         */
        set(id) {
            val spRecord =
                spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            if (spRecord != null) {
                spRecord.shapeId = id
            }
        }

    fun getShapeId(): Int = shapeId

    val isHidden: Boolean
        /**
         * 
         * @return
         */
        get() = ShapeKit.isHidden(this.spContainer)

    val masterShapeID: Int
        /**
         * 
         * @return
         */
        get() = ShapeKit.getMasterShapeID(this.spContainer)

    open val anchor2D: Rectangle2D
        /**
         * Returns the anchor (the bounding box rectangle) of this shape.
         * All coordinates are expressed in points (72 dpi).
         * 
         * @return the anchor of this shape
         */
        get() {
            var anchor: Rectangle2D? = null
            val spRecord =
                spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            val flags = spRecord!!.flags
            if ((flags and EscherSpRecord.FLAG_CHILD) != 0) {
                val rec = ShapeKit.getEscherChild(
                    this.spContainer,
                    EscherChildAnchorRecord.RECORD_ID.toInt()
                ) as EscherChildAnchorRecord?
                if (rec == null) {
                    val clrec = ShapeKit.getEscherChild(
                        this.spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
                    ) as EscherClientAnchorRecord?
                    anchor = Rectangle2D.Float(
                        clrec!!.col1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        clrec.flag.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        (clrec.dx1 - clrec.col1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        (clrec.row1 - clrec.flag).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
                    )
                } else {
                    anchor = Rectangle2D.Float(
                        rec.dx1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        rec.dy1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        (rec.dx2 - rec.dx1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                        (rec.dy2 - rec.dy1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
                    )
                }
            } else {
                val rec = ShapeKit.getEscherChild(
                    this.spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
                ) as EscherClientAnchorRecord?
                anchor = Rectangle2D.Float(
                    rec!!.col1.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    rec.flag.toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    (rec.dx1 - rec.col1).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI,
                    (rec.row1 - rec.flag).toFloat() * MainConstant.POINT_DPI / ShapeKit.MASTER_DPI
                )
            }
            return anchor
        }

    val anchor: Rectangle
        /**
         * Returns the anchor (the bounding box rectangle) of this shape.
         * All coordinates are expressed in points (72 dpi).
         * 
         * @return the anchor of this shape
         */
        get() {
            val anchor2d = this.anchor2D
            return anchor2d.getBounds()
        }

    /**
     * Sets the anchor (the bounding box rectangle) of this shape.
     * All coordinates should be expressed in points (72 dpi).
     * 
     * @param anchor new anchor
     */
    open fun setAnchor(anchor: Rectangle2D) {
        val spRecord =
            spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
        val flags = spRecord!!.flags
        if ((flags and EscherSpRecord.FLAG_CHILD) != 0) {
            val rec = ShapeKit.getEscherChild(
                this.spContainer,
                EscherChildAnchorRecord.RECORD_ID.toInt()
            ) as EscherChildAnchorRecord?
            rec!!.dx1 = (anchor.getX() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            rec.dy1 = (anchor.getY() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            rec.dx2 =
                ((anchor.getWidth() + anchor.getX()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
            rec.dy2 =
                ((anchor.getHeight() + anchor.getY()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
        } else {
            val rec = ShapeKit.getEscherChild(
                this.spContainer, EscherClientAnchorRecord.RECORD_ID.toInt()
            ) as EscherClientAnchorRecord?
            rec!!.flag = (anchor.getY() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                .toShort()
            rec.col1 = (anchor.getX() * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI).toInt()
                .toShort()
            rec.dx1 =
                (((anchor.getWidth() + anchor.getX()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)).toInt()
                    .toShort()
            rec.row1 =
                (((anchor.getHeight() + anchor.getY()) * ShapeKit.MASTER_DPI / MainConstant.POINT_DPI)).toInt()
                    .toShort()
        }
    }

    protected fun getColor(rgb: Int, alpha: Int): Color {
        var rgb = rgb
        if (rgb >= 0x8000000) {
            val idx = rgb - 0x8000000
            val ca = this.sheet!!.colorScheme!!
            if (idx >= 0 && idx <= 7) {
                rgb = ca.getColor(idx)
            }
        }
        val tmp = Color(rgb, true)
        return Color(tmp.getBlue(), tmp.getGreen(), tmp.getRed(), alpha)
    }

    val fill: Fill
        /**
         * Fill properties of this shape
         * 
         * @return fill properties of this shape
         */
        get() {
            if (_fill == null) {
                _fill = Fill(this)
            }
            return _fill!!
        }

    open val hyperlink: Hyperlink?
        /**
         * Returns the hyperlink assigned to this shape
         * 
         * @return the hyperlink assigned to this shape
         * or `null` if not found.
         */
        get() = Hyperlink.Companion.find(this)

    /**
     * Set an simple escher property for this shape.
     * 
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     * @param value     value of the property. If value = -1 then the property is removed.
     */
    fun setEscherProperty(propId: Short, value: Int) {
        val opt = Companion.getEscherChild(
            this.spContainer!!,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        Companion.setEscherProperty(opt!!, propId, value)
    }

    /**
     * Get the value of a simple escher property for this shape.
     * 
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     */
    fun getEscherProperty(propId: Short): Int {
        val opt = Companion.getEscherChild(
            this.spContainer!!,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        val prop = getEscherProperty(opt, propId.toInt()) as EscherSimpleProperty?
        return if (prop == null) 0 else prop.propertyValue
    }

    /**
     * Get the value of a simple escher property for this shape.
     * 
     * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
     */
    fun getEscherProperty(propId: Short, defaultValue: Int): Int {
        val opt = Companion.getEscherChild(
            this.spContainer!!,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        val prop = getEscherProperty(opt, propId.toInt()) as EscherSimpleProperty?
        return if (prop == null) defaultValue else prop.propertyValue
    }


    open val adjustmentValue: Array<Float?>?
        get() = ShapeKit.getAdjustmentValue(this.spContainer)

    val startArrowType: Int
        get() {
            val opt =
                Companion.getEscherChild(
                    this.spContainer!!,
                    EscherOptRecord.RECORD_ID.toInt()
                ) as EscherOptRecord?
            if (opt != null) {
                val prop =
                    getEscherProperty(
                        opt,
                        EscherProperties.LINESTYLE__LINESTARTARROWHEAD.toInt()
                    ) as EscherSimpleProperty?
                if (prop != null && prop.propertyValue > 0) {
                    return prop.propertyValue
                }
            }
            return 0
        }

    val startArrowWidth: Int
        get() = ShapeKit.getStartArrowWidth(this.spContainer)

    val startArrowLength: Int
        get() = ShapeKit.getStartArrowLength(this.spContainer)

    val endArrowType: Int
        get() {
            val opt =
                Companion.getEscherChild(
                    this.spContainer!!,
                    EscherOptRecord.RECORD_ID.toInt()
                ) as EscherOptRecord?
            if (opt != null) {
                val prop =
                    getEscherProperty(
                        opt,
                        EscherProperties.LINESTYLE__LINEENDARROWHEAD.toInt()
                    ) as EscherSimpleProperty?
                if (prop != null && prop.propertyValue > 0) {
                    return prop.propertyValue
                }
            }
            return 0
        }

    val endArrowWidth: Int
        get() = ShapeKit.getEndArrowWidth(this.spContainer)

    val endArrowLength: Int
        get() = ShapeKit.getEndArrowLength(this.spContainer)

    /**
     * shape has line or not
     * @return
     */
    fun hasLine(): Boolean {
        return ShapeKit.hasLine(this.spContainer)
    }

    val lineWidth: Double
        /**
         * Returns width of the line in in points
         */
        get() = ShapeKit.getLineWidth(this.spContainer).toDouble()

    val lineColor: Color?
        get() = ShapeKit.getLineColor(
            this.spContainer,
            this.sheet,
            MainConstant.APPLICATION_TYPE_PPT.toInt()
        )

    val fillColor: Color?
        /**
         * The color used to fill this shape.
         */
        get() = this.fill.foregroundColor

    open val flipHorizontal: Boolean
        /**
         * Whether the shape is horizontally flipped
         * 
         * @return whether the shape is horizontally flipped
         */
        get() = ShapeKit.getFlipHorizontal(this.spContainer)

    open val flipVertical: Boolean
        /**
         * Whether the shape is vertically flipped
         * 
         * @return whether the shape is vertically flipped
         */
        get() = ShapeKit.getFlipVertical(this.spContainer)

    open val rotation: Int
        /**
         * Rotation angle in degrees
         * 
         * @return rotation angle in degrees
         */
        get() = ShapeKit.getRotation(this.spContainer)

    /**
     * 
     */
    open fun dispose() {
        this.parent = null
        this.sheet = null
        if (this.spContainer != null) {
            spContainer!!.dispose()
            this.spContainer = null
        }
        if (_fill != null) {
            _fill!!.dispose()
            _fill = null
        }
    }


    /** */
    /**
     * Creates the lowerlevel escher records for this shape.
     */
    protected abstract fun createSpContainer(isChild: Boolean): EscherContainerRecord?

    val shapeName: String?
        /**
         * @return name of the shape.
         */
        get() = ShapeTypes.typeName(this.shapeType)

    open val logicalAnchor2D: Rectangle2D
        get() = this.anchor2D

    /**
     * Moves the top left corner of the shape to the specified point.
     * 
     * @param x the x coordinate of the top left corner of the shape
     * @param y the y coordinate of the top left corner of the shape
     */
    fun moveTo(x: Float, y: Float) {
        val anchor = this.anchor2D
        anchor.setRect(x.toDouble(), y.toDouble(), anchor.getWidth(), anchor.getHeight())
        setAnchor(anchor)
    }

    /**
     * Event which fires when a shape is inserted in the sheet.
     * In some cases we need to propagate changes to upper level containers.
     * <br></br>
     * Default implementation does nothing.
     * 
     * @param sh - owning shape
     */
    open fun afterInsert(sh: Sheet?) {
    }

    open val outline: com.wxiwei.office.java.awt.Shape
        get() = this.logicalAnchor2D

    /**
     * @return  The shape container and it's children that can represent this
     * shape.
     */

    /**
     * @return the parent of this shape
     */

    /**
     * @return the `SlideShow` this shape belongs to
     */
    /**
     * Assign the `SlideShow` this shape belongs to
     * 
     * @param sheet owner of this shape
     */
    /**
     * The `Sheet` this shape belongs to
     */
    open var sheet: Sheet? = null

    /**
     * Fill
     */
    protected var _fill: Fill? = null

    companion object {
        /**
         * Helper method to return escher child by record ID
         * 
         * @return escher record or `null` if not found.
         */
        fun getEscherChild(owner: EscherContainerRecord, recordId: Int): EscherRecord? {
            val iterator: MutableIterator<EscherRecord?> = owner.childIterator
            while (iterator.hasNext()) {
                val escherRecord = iterator.next()
                if (escherRecord!!.recordId.toInt() == recordId) return escherRecord
            }
            return null
        }

        /**
         * Returns  escher property by id.
         * 
         * @return escher property or `null` if not found.
         */
        fun getEscherProperty(opt: EscherOptRecord?, propId: Int): EscherProperty? {
            if (opt != null) {
                var iterator: MutableIterator<*> = opt.escherProperties.iterator()
                while (iterator.hasNext()) {
                    val prop = iterator.next() as EscherProperty
                    if (prop.propertyNumber.toInt() == propId) return prop
                }
            }
            return null
        }

        /**
         * Set an escher property for this shape.
         * 
         * @param opt       The opt record to set the properties to.
         * @param propId    The id of the property. One of the constants defined in EscherOptRecord.
         * @param value     value of the property. If value = -1 then the property is removed.
         */
        fun setEscherProperty(opt: EscherOptRecord?, propId: Short, value: Int) {
            val props: MutableList<*> = opt!!.escherProperties
            val iterator: MutableIterator<*> = props.iterator()
            while (iterator.hasNext()) {
                val prop = iterator.next() as EscherProperty
                if (prop.id == propId) {
                    iterator.remove()
                }
            }
            if (value != -1) {
                opt.addEscherProperty(EscherSimpleProperty(propId, value))
                opt.sortProperties()
            }
        }
    }
}
