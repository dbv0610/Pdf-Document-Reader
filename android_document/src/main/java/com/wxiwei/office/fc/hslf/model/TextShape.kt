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

import com.wxiwei.office.common.shape.TextBox
import com.wxiwei.office.fc.ShapeKit
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.ddf.EscherTextboxRecord
import com.wxiwei.office.fc.hslf.record.EscherTextboxWrapper
import com.wxiwei.office.fc.hslf.record.InteractiveInfo
import com.wxiwei.office.fc.hslf.record.InteractiveInfoAtom
import com.wxiwei.office.fc.hslf.record.OEPlaceholderAtom
import com.wxiwei.office.fc.hslf.record.OutlineTextRefAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.record.RoundTripHFPlaceholder12
import com.wxiwei.office.fc.hslf.record.StyleTextPropAtom
import com.wxiwei.office.fc.hslf.record.TextCharsAtom
import com.wxiwei.office.fc.hslf.record.TextHeaderAtom
import com.wxiwei.office.fc.hslf.record.TextRulerAtom
import com.wxiwei.office.fc.hslf.record.TxInteractiveInfoAtom
import com.wxiwei.office.java.awt.Rectangle
import com.wxiwei.office.java.awt.geom.Rectangle2D

/**
 * A common superclass of all shapes that can hold text.
 * 
 * @author Yegor Kozlov
 */
abstract class TextShape : SimpleShape {
    /**
     * TextRun object which holds actual text and format data
     */
    protected var _txtrun: TextRun? = null

    /**
     * Escher container which holds text attributes such as
     * TextHeaderAtom, TextBytesAtom ot TextCharsAtom, StyleTextPropAtom etc.
     */
    protected var _txtbox: EscherTextboxWrapper? = null

    /**
     * Used to calculate text bounds
     */
    //protected static final FontRenderContext _frc = new FontRenderContext(null, true, true);
    /**
     * Create a TextBox object and initialize it from the supplied Record container.
     * 
     * @param escherRecord       `EscherSpContainer` container which holds information about this shape
     * @param parent    the parent of the shape
     */
    protected constructor(
        escherRecord: EscherContainerRecord?,
        parent: Shape?
    ) : super(escherRecord, parent)

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     * 
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     * 
     */
    @JvmOverloads
    constructor(parent: Shape? = null) : super(null, parent) {
        spContainer = createSpContainer(parent is ShapeGroup)
    }

    fun createTextRun(): TextRun? {
        _txtbox = this.escherTextboxWrapper
        if (_txtbox == null) _txtbox = EscherTextboxWrapper()

        _txtrun = this.textRun
        if (_txtrun == null) {
            val tha = TextHeaderAtom()
            tha.setParentRecord(_txtbox)
            _txtbox!!.appendChildRecord(tha)

            val tca = TextCharsAtom()
            _txtbox!!.appendChildRecord(tca)

            val sta = StyleTextPropAtom(0)
            _txtbox!!.appendChildRecord(sta)

            _txtrun = TextRun(tha, tca, sta)
            _txtrun!!.records = arrayOf<Record?>(tha, tca, sta)
            _txtrun!!.text = ""

            spContainer!!.addChildRecord(_txtbox!!.escherRecord)

            setDefaultTextProperties(_txtrun)
        }

        return _txtrun
    }

    /**
     * Set default properties for the  TextRun.
     * Depending on the text and shape type the defaults are different:
     * TextBox: align=left, valign=top
     * AutoShape: align=center, valign=middle
     * 
     */
    protected open fun setDefaultTextProperties(_txtrun: TextRun?) {
    }

    var text: String?
        /**
         * Returns the text contained in this text frame.
         * 
         * @return the text string for this textbox.
         */
        get() {
            val tx = this.textRun
            return if (tx == null) null else tx.text
        }
        /**
         * Sets the text contained in this text frame.
         * 
         * @param text the text string used by this object.
         */
        set(text) {
            var tx = this.textRun
            if (tx == null) {
                tx = createTextRun()
            }
            tx!!.text = text!!
            this.textId = text.hashCode()
        }

    /**
     * When a textbox is added to  a sheet we need to tell upper-level
     * `PPDrawing` about it.
     * 
     * @param sh the sheet we are adding to
     */
    override fun afterInsert(sh: Sheet?) {
        super.afterInsert(sh)

        val _txtbox = this.escherTextboxWrapper
        if (_txtbox != null) {
            val ppdrawing = sh!!.pPDrawing
            ppdrawing!!.addTextboxWrapper(_txtbox)
            // Ensure the escher layer knows about the added records
            /*try
            {
                //_txtbox.writeOut(null);
            }
            catch(IOException e)
            {
                throw new HSLFException(e);
            }*/
            if (anchor.equals(Rectangle()) && "" != this.text) resizeToFitText()
        }
        if (_txtrun != null) {
            _txtrun!!.shapeId = shapeId
            sh!!.onAddTextShape(this)
        }
    }

    protected val escherTextboxWrapper: EscherTextboxWrapper?
        get() {
            if (_txtbox == null) {
                val textRecord = ShapeKit.getEscherChild(
                    spContainer, EscherTextboxRecord.RECORD_ID.toInt()
                ) as EscherTextboxRecord?
                if (textRecord != null) _txtbox = EscherTextboxWrapper(textRecord)
            }
            return _txtbox
        }

    val metaCharactersType: Byte
        /**
         * 
         * @return
         */
        get() {
            val clientTextBox = this.escherTextboxWrapper
            if (clientTextBox != null) {
                val children: Array<out Record?> =
                    clientTextBox.getChildRecords()
                for (i in children.indices) {
                    if (children[i] != null) {
                        val rType = children[i]!!.getRecordType()
                        if (rType == RecordTypes.SlideNumberMCAtom.typeID.toLong()) {
                            return TextBox.MC_SlideNumber
                        } else if (rType == RecordTypes.DateTimeMCAtom.typeID.toLong()) {
                            return TextBox.MC_DateTime
                        } else if (rType == RecordTypes.GenericDateMCAtom.typeID.toLong()) {
                            return TextBox.MC_GenericDate
                        } else if (rType == RecordTypes.RTFDateTimeMCAtom.typeID.toLong()) {
                            return TextBox.MC_RTFDateTime
                        } else if (rType == RecordTypes.FooterMCAtom.typeID.toLong()) {
                            return TextBox.MC_Footer
                        }
                    }
                }
            }
            return -1
        }

    /**
     * Adjust the size of the TextShape so it encompasses the text inside it.
     * 
     * @return a `Rectangle2D` that is the bounds of this `TextShape`.
     */
    fun resizeToFitText(): Rectangle2D? {
        /*String txt = getText();
        if (txt == null || txt.length() == 0)
            return new Rectangle2D.Float();

        RichTextRun rt = getTextRun().getRichTextRuns()[0];
        int size = rt.getFontSize();
        int style = 0;
        if (rt.isBold())
            style |= Font.BOLD;
        if (rt.isItalic())
            style |= Font.ITALIC;
        String fntname = rt.getFontName();
        Font font = new Font(fntname, style, size);

        float width = 0, height = 0, leading = 0;
        String[] lines = txt.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].length() == 0)
                continue;

            TextLayout layout = new TextLayout(lines[i], font, _frc);

            leading = Math.max(leading, layout.getLeading());
            width = Math.max(width, layout.getAdvance());
            height = Math.max(height, (height + (layout.getDescent() + layout.getAscent())));
        }

        // add one character to width
        Rectangle2D charBounds = font.getMaxCharBounds(_frc);
        width += getMarginLeft() + getMarginRight() + charBounds.getWidth();

        // add leading to height
        height += getMarginTop() + getMarginBottom() + leading;

        Rectangle2D anchor = anchor2D;
        anchor.setRect(anchor.getX(), anchor.getY(), width, height);
        setAnchor(anchor);*/
        val anchor = anchor2D
        return anchor
    }

    var verticalAlignment: Int
        /**
         * Returns the type of vertical alignment for the text.
         * One of the `Anchor*` constants defined in this class.
         * 
         * @return the type of alignment
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__ANCHORTEXT.toInt()
            ) as EscherSimpleProperty?
            var valign: Int = AnchorTop
            if (prop == null) {
                /**
                 * If vertical alignment was not found in the shape properties then try to
                 * fetch the master shape and search for the align property there.
                 */
                val type = this.textRun!!.runType
                val master =
                    sheet!!.masterSheet
                var masterShape: TextShape? = null
                if (master != null && this.placeholderAtom != null) {
                    masterShape = master.getPlaceholderByTextType(type)
                }
                if (masterShape != null) {
                    valign = masterShape.verticalAlignment
                } else {
                    //not found in the master sheet. Use the hardcoded defaults.
                    when (type) {
                        TextHeaderAtom.Companion.TITLE_TYPE, TextHeaderAtom.Companion.CENTER_TITLE_TYPE -> valign =
                            AnchorMiddle

                        else -> valign = AnchorTop
                    }
                }
            } else {
                valign = prop.propertyValue
            }
            return valign
        }
        /**
         * Sets the type of vertical alignment for the text.
         * One of the `Anchor*` constants defined in this class.
         * 
         * @param align - the type of alignment
         */
        set(align) {
            setEscherProperty(EscherProperties.TEXT__ANCHORTEXT, align)
        }

    var horizontalAlignment: Int
        /**
         * Gets the type of horizontal alignment for the text.
         * One of the `Align*` constants defined in this class.
         * 
         * @return align - the type of horizontal alignment
         */
        get() {
            val tx = this.textRun
            return if (tx == null) -1 else tx.richTextRuns!![0].alignment
        }
        /**
         * Sets the type of horizontal alignment for the text.
         * One of the `Align*` constants defined in this class.
         * 
         * @param align - the type of horizontal alignment
         */
        set(align) {
            val tx = this.textRun
            if (tx != null) tx.richTextRuns!![0].alignment = align
        }

    var marginBottom: Float
        /**
         * Returns the distance (in points) between the bottom of the text frame
         * and the bottom of the inscribed rectangle of the shape that contains the text.
         * Default value is 1/20 inch.
         * 
         * @return the botom margin
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__TEXTBOTTOM.toInt()
            ) as EscherSimpleProperty?
            val `val` =
                if (prop == null) ShapeKit.EMU_PER_INCH / 20 else prop.propertyValue
            return `val`.toFloat() / ShapeKit.EMU_PER_POINT
        }
        /**
         * Sets the botom margin.
         * @see .getMarginBottom
         * @param margin    the bottom margin
         */
        set(margin) {
            setEscherProperty(
                EscherProperties.TEXT__TEXTBOTTOM,
                (margin * ShapeKit.EMU_PER_POINT).toInt()
            )
        }

    var marginLeft: Float
        /**
         * Returns the distance (in points) between the left edge of the text frame
         * and the left edge of the inscribed rectangle of the shape that contains
         * the text.
         * Default value is 1/10 inch.
         * 
         * @return the left margin
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__TEXTLEFT.toInt()
            ) as EscherSimpleProperty?
            val `val` =
                if (prop == null) ShapeKit.EMU_PER_INCH / 10 else prop.propertyValue
            return `val`.toFloat() / ShapeKit.EMU_PER_POINT
        }
        /**
         * Sets the left margin.
         * @see .getMarginLeft
         * @param margin    the left margin
         */
        set(margin) {
            setEscherProperty(
                EscherProperties.TEXT__TEXTLEFT,
                (margin * ShapeKit.EMU_PER_POINT).toInt()
            )
        }

    var marginRight: Float
        /**
         * Returns the distance (in points) between the right edge of the
         * text frame and the right edge of the inscribed rectangle of the shape
         * that contains the text.
         * Default value is 1/10 inch.
         * 
         * @return the right margin
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__TEXTRIGHT.toInt()
            ) as EscherSimpleProperty?
            val `val` =
                if (prop == null) ShapeKit.EMU_PER_INCH / 10 else prop.propertyValue
            return `val`.toFloat() / ShapeKit.EMU_PER_POINT
        }
        /**
         * Sets the right margin.
         * @see .getMarginRight
         * @param margin    the right margin
         */
        set(margin) {
            setEscherProperty(
                EscherProperties.TEXT__TEXTRIGHT,
                (margin * ShapeKit.EMU_PER_POINT).toInt()
            )
        }

    var marginTop: Float
        /**
         * Returns the distance (in points) between the top of the text frame
         * and the top of the inscribed rectangle of the shape that contains the text.
         * Default value is 1/20 inch.
         * 
         * @return the top margin
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__TEXTTOP.toInt()
            ) as EscherSimpleProperty?
            val `val` =
                if (prop == null) ShapeKit.EMU_PER_INCH / 20 else prop.propertyValue
            return `val`.toFloat() / ShapeKit.EMU_PER_POINT
        }
        /**
         * Sets the top margin.
         * @see .getMarginTop
         * @param margin    the top margin
         */
        set(margin) {
            setEscherProperty(
                EscherProperties.TEXT__TEXTTOP,
                (margin * ShapeKit.EMU_PER_POINT).toInt()
            )
        }

    var wordWrap: Int
        /**
         * Returns the value indicating word wrap.
         * 
         * @return the value indicating word wrap.
         * Must be one of the `Wrap*` constants defined in this class.
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__WRAPTEXT.toInt()
            ) as EscherSimpleProperty?
            return if (prop == null) WrapSquare else prop.propertyValue
        }
        /**
         * Specifies how the text should be wrapped
         * 
         * @param wrap  the value indicating how the text should be wrapped.
         * Must be one of the `Wrap*` constants defined in this class.
         */
        set(wrap) {
            setEscherProperty(EscherProperties.TEXT__WRAPTEXT, wrap)
        }

    var textId: Int
        /**
         * @return id for the text.
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                spContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.TEXT__TEXTID.toInt()
            ) as EscherSimpleProperty?
            return if (prop == null) 0 else prop.propertyValue
        }
        /**
         * Sets text ID
         * 
         * @param id of the text
         */
        set(id) {
            setEscherProperty(EscherProperties.TEXT__TEXTID, id)
        }

    val textRun: TextRun?
        /**
         * @return the TextRun object for this text box
         */
        get() {
            if (_txtrun == null) initTextRun()
            return _txtrun
        }

    override var sheet: Sheet?
        get() = super.sheet
        set(sheet) {
            super.sheet = sheet

            // Initialize _txtrun object.
            // (We can't do it in the constructor because the sheet
            //  is not assigned then, it's only built once we have
            //  all the records)
            val tx = this.textRun
            if (tx != null) {
                // Supply the sheet to our child RichTextRuns
                tx.sheet = sheet
                val rt = tx.richTextRuns!!
                for (i in rt.indices) {
                    rt[i].supplySlideShow(sheet!!.slideShow)
                }
            }
        }

    protected fun initTextRun() {
        val txtbox = this.escherTextboxWrapper
        val sheet = sheet

        if (sheet == null || txtbox == null) return

        var ota: OutlineTextRefAtom? = null

        val child: Array<Record> = txtbox.getChildRecords()
        for (i in child.indices) {
            if (child[i] is OutlineTextRefAtom) {
                ota = child[i] as OutlineTextRefAtom?
                break
            }
        }

        val runs = sheet.textRuns
        if (ota != null) {
            val idx = ota.textIndex
            for (i in runs!!.indices) {
                if (runs[i].index == idx && runs[i].shapeId < 0) {
                    _txtrun = runs[i]
                    break
                }
            }
            /*if (_txtrun == null)
            {
                logger.log(POILogger.WARN, "text run not found for OutlineTextRefAtom.TextIndex="
                    + idx);
            }*/
        } else {
            val escherSpRecord =
                spContainer!!.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
            val shapeId = escherSpRecord!!.shapeId
            if (runs != null) for (i in runs.indices) {
                if (runs[i].shapeId == shapeId) {
                    _txtrun = runs[i]
                    break
                }
            }
        }
        // ensure the same references child records of TextRun
        if (_txtrun != null) for (i in child.indices) {
            if (_txtrun!!._ruler == null && child[i] is TextRulerAtom) {
                _txtrun!!._ruler = child[i] as TextRulerAtom?
            }
            for (r in _txtrun!!.records!!) {
                if (child[i].getRecordType() == r!!.getRecordType()) {
                    child[i] = r
                }
            }
        }
    }

    /*public void draw(Graphics2D graphics)
    {
        AffineTransform at = graphics.getTransform();
        ShapePainter.paint(this, graphics);
        new TextPainter(this).paint(graphics);
        graphics.setTransform(at);
    }*/
    val placeholderAtom: OEPlaceholderAtom?
        /**
         * Return `OEPlaceholderAtom`, the atom that describes a placeholder.
         * 
         * @return `OEPlaceholderAtom` or `null` if not found
         */
        get() = getClientDataRecord(RecordTypes.OEPlaceholderAtom.typeID) as OEPlaceholderAtom?

    val placeholderId: Int
        /**
         * 
         * @return
         */
        get() {
            var placeholderId = 0
            val oep = this.placeholderAtom
            if (oep != null) {
                placeholderId = oep.placeholderId
            } else {
                //special case for files saved in Office 2007
                val hldr =
                    getClientDataRecord(RecordTypes.RoundTripHFPlaceholder12.typeID) as RoundTripHFPlaceholder12?
                if (hldr != null) placeholderId = hldr.placeholderId
            }

            return placeholderId
        }

    /**
     * 
     * Assigns a hyperlink to this text shape
     * 
     * @param linkId    id of the hyperlink, @see arc.fc.hslf.usermodel.SlideShow#addHyperlink(Hyperlink)
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @see com.wxiwei.office.fc.hslf.usermodel.SlideShow.addHyperlink
     */
    fun setHyperlink(linkId: Int, beginIndex: Int, endIndex: Int) {
        //TODO validate beginIndex and endIndex and throw IllegalArgumentException

        val info = InteractiveInfo()
        val infoAtom = info.interactiveInfoAtom!!
        infoAtom.action = InteractiveInfoAtom.Companion.ACTION_HYPERLINK
        infoAtom.hyperlinkType = InteractiveInfoAtom.Companion.LINK_Url
        infoAtom.hyperlinkID = linkId

        _txtbox!!.appendChildRecord(info)

        val txiatom = TxInteractiveInfoAtom()
        txiatom.startIndex = beginIndex
        txiatom.endIndex = endIndex
        _txtbox!!.appendChildRecord(txiatom)
    }

    val unicodeGeoText: String?
        /**
         * get wordart content
         * @param escherContainer
         * @return
         */
        get() = ShapeKit.getUnicodeGeoText(spContainer)

    /**
     * 
     */
    override fun dispose() {
        super.dispose()
        if (_txtrun != null) {
            _txtrun!!.dispose()
            _txtrun = null
        }
        if (_txtbox != null) {
            _txtbox!!.dispose()
            _txtbox = null
        }
    }

    companion object {
        /**
         * How to anchor the text
         */
        const val AnchorTop: Int = 0
        const val AnchorMiddle: Int = 1
        const val AnchorBottom: Int = 2
        const val AnchorTopCentered: Int = 3
        const val AnchorMiddleCentered: Int = 4
        const val AnchorBottomCentered: Int = 5
        const val AnchorTopBaseline: Int = 6
        const val AnchorBottomBaseline: Int = 7
        const val AnchorTopCenteredBaseline: Int = 8
        const val AnchorBottomCenteredBaseline: Int = 9

        /**
         * How to wrap the text
         */
        const val WrapSquare: Int = 0
        const val WrapByPoints: Int = 1
        const val WrapNone: Int = 2
        const val WrapTopBottom: Int = 3
        const val WrapThrough: Int = 4

        /**
         * How to align the text
         */
        const val AlignLeft: Int = 0
        const val AlignCenter: Int = 1
        const val AlignRight: Int = 2
        const val AlignJustify: Int = 3
    }
}
