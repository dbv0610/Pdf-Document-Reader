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

import com.wxiwei.office.constant.AutoShapeConstant
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.DefaultEscherRecordFactory
import com.wxiwei.office.fc.ddf.EscherChildAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientAnchorRecord
import com.wxiwei.office.fc.ddf.EscherClientDataRecord
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherRecord
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.record.InteractiveInfo
import com.wxiwei.office.fc.hslf.record.InteractiveInfoAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putUShort
import com.wxiwei.office.java.awt.Color
import com.wxiwei.office.java.awt.geom.AffineTransform
import com.wxiwei.office.java.awt.geom.Rectangle2D
import java.io.ByteArrayOutputStream

/**
 * An abstract simple (non-group) shape.
 * This is the parent class for all primitive shapes like Line, Rectangle, etc.
 * 
 * @author Yegor Kozlov
 */
abstract class SimpleShape
/**
 * Create a SimpleShape object and initialize it from the supplied Record container.
 * 
 * @param escherRecord    `EscherSpContainer` container which holds information about this shape
 * @param parent    the parent of the shape
 */
protected constructor(escherRecord: EscherContainerRecord?, parent: Shape?) :
    Shape(escherRecord, parent) {
    override val logicalAnchor2D: Rectangle2D
        get() {
        var anchor = anchor2D
        val parent = this.parent

        //if it is a groupped shape see if we need to transform the coordinates
        if (parent != null) {
            /*Shape top = _parent;
            while (top.parent != null)
                top = top.parent;*/

            // child anchor in msofbtClientAnchor container

            val clientAnchor = (parent as ShapeGroup).getClientAnchor2D(parent)
            // group anchor in msofbtSpgr container
            val spgrAnchor = parent.coordinates

            //            double scale = Math.max(spgrAnchor.getWidth() / clientAnchor.getWidth(), 
//                                    spgrAnchor.getHeight() / clientAnchor.getHeight());
//
//            double x = clientAnchor.getX() + (anchor.getX() - spgrAnchor.getX()) / scale;
//            double y = clientAnchor.getY() + (anchor.getY() - spgrAnchor.getY()) / scale;
//            double width = anchor.getWidth() / scale;
//            double height = anchor.getHeight() / scale;
            val scalex = spgrAnchor.getWidth() / clientAnchor.getWidth()
            val scaley = spgrAnchor.getHeight() / clientAnchor.getHeight()

            val x = clientAnchor.getX() + (anchor.getX() - spgrAnchor.getX()) / scalex
            val y = clientAnchor.getY() + (anchor.getY() - spgrAnchor.getY()) / scaley
            val width = anchor.getWidth() / scalex
            val height = anchor.getHeight() / scaley


            anchor = Rectangle2D.Double(x, y, width, height)
        }

        val angle = rotation
        if (angle != 0) {
            val centerX = anchor.getX() + anchor.getWidth() / 2
            val centerY = anchor.getY() + anchor.getHeight() / 2

            var trans = AffineTransform()
            trans.translate(centerX, centerY)
            trans.rotate(Math.toRadians(angle.toDouble()))
            trans.translate(-centerX, -centerY)

            val rect = trans.createTransformedShape(anchor)!!.getBounds2D()
            if ((anchor.getWidth() < anchor.getHeight() && rect.getWidth() > rect.getHeight())
                || (anchor.getWidth() > anchor.getHeight() && rect.getWidth() < rect.getHeight())
            ) {
                trans = AffineTransform()
                trans.translate(centerX, centerY)
                trans.rotate(Math.PI / 2)
                trans.translate(-centerX, -centerY)
                anchor = trans.createTransformedShape(anchor)!!.getBounds2D()
            }
        }
        return anchor
        }

    /**
     * Find a record in the underlying EscherClientDataRecord
     * 
     * @param recordType type of the record to search
     */
    fun getClientDataRecord(recordType: Int): com.wxiwei.office.fc.hslf.record.Record? {
        val records = this.clientRecords
        if (records != null) {
            for (i in records.indices) {
                if (records[i].getRecordType() == recordType.toLong()) {
                    return records[i]
                }
            }
        }
        return null
    }

    protected val clientRecords: Array<Record>?
        /**
         * Search for EscherClientDataRecord, if found, convert its contents into an array of HSLF records
         * 
         * @return an array of HSLF records contained in the shape's EscherClientDataRecord or `null`
         */
        get() {
            if (_clientData == null) {
                var r = ShapeKit.getEscherChild(
                    spContainer,
                    EscherClientDataRecord.RECORD_ID.toInt()
                )
                //ddf can return EscherContainerRecord with recordId=EscherClientDataRecord.RECORD_ID
                //convert in to EscherClientDataRecord on the fly
                if (r != null && r !is EscherClientDataRecord) {
                    val data = r.serialize()
                    r = EscherClientDataRecord()
                    r.fillFields(data, 0, DefaultEscherRecordFactory())
                }
                _clientData = r
            }
            if (_clientData != null && _clientRecords == null) {
                val data = _clientData!!.remainingData
                _clientRecords =
                    com.wxiwei.office.fc.hslf.record.Record.findChildRecords(data!!, 0, data.size)
            }
            return _clientRecords
        }


    /** */
    /**
     * Create a new Shape
     * 
     * @param isChild   `true` if the Line is inside a group, `false` otherwise
     * @return the record container which holds this shape
     */
    override fun createSpContainer(isChild: Boolean): EscherContainerRecord? {
        spContainer = EscherContainerRecord()
        spContainer!!.recordId = EscherContainerRecord.SP_CONTAINER
        spContainer!!.options = 15.toShort()

        val sp = EscherSpRecord()
        var flags = EscherSpRecord.FLAG_HAVEANCHOR or EscherSpRecord.FLAG_HASSHAPETYPE
        if (isChild) flags = flags or EscherSpRecord.FLAG_CHILD
        sp.flags = flags
        spContainer!!.addChildRecord(sp)

        val opt = EscherOptRecord()
        opt.recordId = EscherOptRecord.RECORD_ID
        spContainer!!.addChildRecord(opt)

        val anchor: EscherRecord?
        if (isChild) anchor = EscherChildAnchorRecord()
        else {
            anchor = EscherClientAnchorRecord()

            //hack. internal variable EscherClientAnchorRecord.shortRecord can be
            //initialized only in fillFields(). We need to set shortRecord=false;
            val header = ByteArray(16)
            putUShort(header, 0, 0)
            putUShort(header, 2, 0)
            putInt(header, 4, 8)
            anchor.fillFields(header, 0, null)
        }
        spContainer!!.addChildRecord(anchor)

        return spContainer
    }

    /**
     * Sets the width of line in in points
     * @param width  the width of line in in points
     */
    fun setLineWidth(width: Double) {
        val opt = ShapeKit.getEscherChild(
            spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        Shape.Companion.setEscherProperty(
            opt,
            EscherProperties.LINESTYLE__LINEWIDTH,
            (width * ShapeKit.EMU_PER_POINT).toInt()
        )
    }

    /**
     * Sets the color of line
     * 
     * @param color new color of the line
     */
    fun setLineColor(color: Color?) {
        val opt = ShapeKit.getEscherChild(
            spContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        if (color == null) {
            Shape.Companion.setEscherProperty(
                opt,
                EscherProperties.LINESTYLE__NOLINEDRAWDASH,
                0x80000
            )
        } else {
            val rgb = Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB()
            Shape.Companion.setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, rgb)
            Shape.Companion.setEscherProperty(
                opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, if (color == null)
                    0x180010
                else
                    0x180018
            )
        }
    }

    var lineDashing: Int
        /**
         * Gets line dashing. One of the PEN_* constants defined in this class.
         * 
         * @return dashing of the line.
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?

            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.LINESTYLE__LINEDASHING.toInt()
            ) as EscherSimpleProperty?
            return if (prop == null) AutoShapeConstant.LINESTYLE_SOLID else prop.propertyValue
        }
        /**
         * Sets line dashing. One of the PEN_* constants defined in this class.
         * 
         * @param pen new style of the line.
         */
        set(pen) {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?

            Shape.Companion.setEscherProperty(
                opt,
                EscherProperties.LINESTYLE__LINEDASHING,
                pen
            )
        }

    var lineStyle: Int
        /**
         * Returns line style. One of the constants defined in this class.
         * 
         * @return style of the line.
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.LINESTYLE__LINESTYLE.toInt()
            ) as EscherSimpleProperty?
            return if (prop == null) AutoShapeConstant.LINE_SIMPLE else prop.propertyValue
        }
        /**
         * Sets line style. One of the constants defined in this class.
         * 
         * @param style new style of the line.
         */
        set(style) {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            Shape.Companion.setEscherProperty(
                opt,
                EscherProperties.LINESTYLE__LINESTYLE,
                if (style == AutoShapeConstant.LINE_SIMPLE)
                    -1
                else
                    style
            )
        }

    /**
     * The color used to fill this shape.
     * 
     * @param color the background color
     */
    fun setFillColor(color: Color?) {
        fill.foregroundColor = color
    }

    /**
     * Rotate this shape
     * 
     * @param theta the rotation angle in degrees
     */
    fun setRotation(theta: Int) {
        setEscherProperty(EscherProperties.TRANSFORM__ROTATION, (theta shl 16))
    }

    /*public void draw(Graphics2D graphics)
    {
        AffineTransform at = graphics.getTransform();
        ShapePainter.paint(this, graphics);
        graphics.setTransform(at);
    }*/
    protected fun updateClientData() {
        val clientRecords = _clientRecords
        if (_clientData != null && clientRecords != null) {
            val out = ByteArrayOutputStream()
            try {
                for (i in clientRecords.indices) {
                    //_clientRecords[i].writeOut(out);
                }
            } catch (e: Exception) {
                throw HSLFException(e)
            }
            _clientData!!.remainingData = out.toByteArray()
        }
    }

    fun setHyperlink(link: Hyperlink) {
        if (link.id == -1) {
            throw HSLFException("You must call SlideShow.addHyperlink(Hyperlink link) first")
        }

        val cldata = EscherClientDataRecord()
        cldata.options = 0xF.toShort()
        spContainer!!.addChildRecord(cldata) // TODO - junit to prove getChildRecords().add is wrong

        val info = InteractiveInfo()
        val infoAtom = info.interactiveInfoAtom!!

        when (link.getType()) {
            Hyperlink.Companion.LINK_FIRSTSLIDE.toInt() -> {
                infoAtom.action = InteractiveInfoAtom.Companion.ACTION_JUMP
                infoAtom.jump = InteractiveInfoAtom.Companion.JUMP_FIRSTSLIDE
                infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_FirstSlide
            }

            Hyperlink.Companion.LINK_LASTSLIDE.toInt() -> {
                infoAtom.action = InteractiveInfoAtom.Companion.ACTION_JUMP
                infoAtom.jump = InteractiveInfoAtom.Companion.JUMP_LASTSLIDE
                infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_LastSlide
            }

            Hyperlink.Companion.LINK_NEXTSLIDE.toInt() -> {
                infoAtom.action = InteractiveInfoAtom.Companion.ACTION_JUMP
                infoAtom.jump = InteractiveInfoAtom.Companion.JUMP_NEXTSLIDE
                infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_NextSlide
            }

            Hyperlink.Companion.LINK_PREVIOUSSLIDE.toInt() -> {
                infoAtom.action = InteractiveInfoAtom.Companion.ACTION_JUMP
                infoAtom.jump = InteractiveInfoAtom.Companion.JUMP_PREVIOUSSLIDE
                infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_PreviousSlide
            }

            Hyperlink.Companion.LINK_URL.toInt() -> {
                infoAtom.action = InteractiveInfoAtom.Companion.ACTION_HYPERLINK
                infoAtom.jump = InteractiveInfoAtom.Companion.JUMP_NONE
                infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_Url
            }
        }

        infoAtom.hyperlinkID = link.id

        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            info.writeOut(out);
        }
        catch(Exception e)
        {
            throw new HSLFException(e);
        }
        cldata.setRemainingData(out.toByteArray());*/
    }

    /**
     * 
     * 
     */
    override fun dispose() {
        super.dispose()
        if (_clientRecords != null) {
            for (rec in _clientRecords!!) {
                rec.dispose()
            }
            _clientRecords = null
        }
        if (_clientData != null) {
            _clientData!!.dispose()
            _clientData = null
        }
    }

    /**
     * Records stored in EscherClientDataRecord
     */
    protected var _clientRecords: Array<com.wxiwei.office.fc.hslf.record.Record>? = null
    protected var _clientData: EscherClientDataRecord? = null
}
