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
import com.wxiwei.office.fc.ddf.EscherBSERecord
import com.wxiwei.office.fc.ddf.EscherComplexProperty
import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.ddf.EscherSpRecord
import com.wxiwei.office.fc.hslf.exceptions.HSLFException
import com.wxiwei.office.fc.hslf.usermodel.PictureData
import com.wxiwei.office.java.awt.Rectangle
import java.io.UnsupportedEncodingException

/**
 * Represents a picture in a PowerPoint document.
 * 
 * @author Yegor Kozlov
 */
open class Picture : SimpleShape {
    /**
     * Create a new `Picture`
     * 
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    /**
     * Create a new `Picture`
     * 
     * @param idx the index of the picture
     */
    @JvmOverloads
    constructor(idx: Int, parent: Shape? = null) : super(null, parent) {
        _escherContainer = createSpContainer(idx, parent is ShapeGroup)
    }

    /**
     * Create a `Picture` object
     * 
     * @param escherRecord the `EscherSpContainer` record which holds information about
     * this picture in the `Slide`
     * @param parent the parent shape of this picture
     */
    constructor(escherRecord: EscherContainerRecord?, parent: Shape?) : super(escherRecord, parent)

    val pictureIndex: Int
        /**
         * Returns index associated with this picture.
         * Index starts with 1 and points to a EscherBSE record which
         * holds information about this picture.
         * 
         * @return the index to this picture (1 based).
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                _escherContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.BLIP__BLIPTODISPLAY.toInt()
            ) as EscherSimpleProperty?
            return if (prop == null) 0 else prop.getPropertyValue()
        }

    /**
     * Create a new Picture and populate the inital structure of the `EscherSp` record which holds information about this picture.
     * 
     * @param idx the index of the picture which referes to `EscherBSE` container.
     * @return the create Picture object
     */
    protected open fun createSpContainer(idx: Int, isChild: Boolean): EscherContainerRecord? {
        _escherContainer = super.createSpContainer(isChild)
        _escherContainer.setOptions(15.toShort())

        val spRecord = _escherContainer.getChildById<EscherSpRecord?>(EscherSpRecord.RECORD_ID)
        spRecord!!.setOptions(((ShapeTypes.PictureFrame shl 4) or 0x2).toShort())

        //set default properties for a picture
        val opt = ShapeKit.getEscherChild(
            _escherContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?
        setEscherProperty(opt, EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x800080)

        //another weird feature of powerpoint: for picture id we must add 0x4000.
        setEscherProperty(opt, (EscherProperties.BLIP__BLIPTODISPLAY + 0x4000).toShort(), idx)

        return _escherContainer
    }

    /**
     * Resize this picture to the default size.
     * For PNG and JPEG resizes the image to 100%,
     * for other types sets the default size of 200x200 pixels.
     */
    fun setDefaultSize() {
        /*PictureData pict = getPictureData();
        if (pict instanceof Bitmap)
        {
            BufferedImage img = null;
            try
            {
                img = ImageIO.read(new ByteArrayInputStream(pict.getData()));
            }
            catch(IOException e)
            {
            }
            catch(NegativeArraySizeException ne)
            {
            }

            if (img != null)
            {
                // Valid image, set anchor from it
                setAnchor(new java.awt.Rectangle(0, 0, img.getWidth() * POINT_DPI / PIXEL_DPI,
                    img.getHeight() * POINT_DPI / PIXEL_DPI));
            }
            else
            {
                // Invalid image, go with the default metafile size
                setAnchor(new java.awt.Rectangle(0, 0, 200, 200));
            }
        }
        else
        {
            //default size of a metafile picture is 200x200
            setAnchor(new java.awt.Rectangle(50, 50, 200, 200));
        }*/
    }

    val pictureData: PictureData?
        /**
         * Returns the picture data for this picture.
         * 
         * @return the picture data for this picture.
         */
        get() {
            val ppt = getSheet().getSlideShow()
            val pict =
                ppt.pictureData

            val bse = this.escherBSERecord
            if (bse == null) {
                //logger.log(POILogger.ERROR, "no reference to picture data found ");
            } else {
                for (i in pict!!.indices) {
                    if (pict[i]!!.offset == bse.getOffset()) {
                        return pict[i]
                    }
                }
                //logger.log(POILogger.ERROR, "no picture found for our BSE offset " + bse.getOffset());
            }
            return null
        }

    val escherOptRecord: EscherOptRecord?
        /**
         * 
         * @return
         */
        get() = ShapeKit.getEscherChild(
            _escherContainer,
            EscherOptRecord.RECORD_ID.toInt()
        ) as EscherOptRecord?

    protected val escherBSERecord: EscherBSERecord?
        get() {
            val ppt = getSheet().getSlideShow()
            val doc = ppt.documentRecord
            val dggContainer = doc!!.getPPDrawingGroup().getDggContainer()
            val bstore = ShapeKit.getEscherChild(
                dggContainer,
                EscherContainerRecord.BSTORE_CONTAINER.toInt()
            ) as EscherContainerRecord?
            if (bstore == null) {
                //logger.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
                return null
            }
            val lst: MutableList<*> = bstore.getChildRecords()
            val idx = this.pictureIndex
            if (idx == 0) {
                //logger.log(POILogger.DEBUG, "picture index was not found, returning ");
                return null
            }
            return lst.get(idx - 1) as EscherBSERecord?
        }

    var pictureName: String?
        /**
         * Name of this picture.
         * 
         * @return name of this picture
         */
        get() {
            val opt = ShapeKit.getEscherChild(
                _escherContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            val prop = ShapeKit.getEscherProperty(
                opt,
                EscherProperties.BLIP__BLIPFILENAME.toInt()
            ) as EscherComplexProperty?
            var name: String? = null
            if (prop != null) {
                try {
                    name =
                        String(prop.getComplexData(), charset("UTF-16LE"))
                    val idx = name.indexOf('\u0000')
                    return if (idx == -1) name else name.substring(0, idx)
                } catch (e: UnsupportedEncodingException) {
                    throw HSLFException(e)
                }
            }
            return name
        }
        /**
         * Name of this picture.
         * 
         * @param name of this picture
         */
        set(name) {
            val opt = ShapeKit.getEscherChild(
                _escherContainer,
                EscherOptRecord.RECORD_ID.toInt()
            ) as EscherOptRecord?
            try {
                val data: ByteArray? =
                    (name + '\u0000').toByteArray(charset("UTF-16LE"))
                val prop = EscherComplexProperty(
                    EscherProperties.BLIP__BLIPFILENAME, false, data
                )
                opt!!.addEscherProperty(prop)
            } catch (e: UnsupportedEncodingException) {
                throw HSLFException(e)
            }
        }

    /**
     * By default set the orininal image size
     */
    protected override fun afterInsert(sh: Sheet?) {
        super.afterInsert(sh)

        val bse = this.escherBSERecord
        bse!!.setRef(bse.getRef() + 1)

        val anchor = getAnchor()
        if (anchor.equals(Rectangle())) {
            setDefaultSize()
        }
    } /*public void draw(Graphics2D graphics)
    {
        AffineTransform at = graphics.getTransform();
        ShapePainter.paint(this, graphics);

        PictureData data = getPictureData();
        if (data != null)
            data.draw(graphics, this);

        graphics.setTransform(at);
    }*/

    companion object {
        /**
         * Windows Enhanced Metafile (EMF)
         */
        const val EMF: Int = 2

        /**
         * Windows Metafile (WMF)
         */
        const val WMF: Int = 3

        /**
         * Macintosh PICT
         */
        const val PICT: Int = 4

        /**
         * JPEG
         */
        const val JPEG: Int = 5

        /**
         * PNG
         */
        const val PNG: Int = 6

        /**
         * Windows DIB (BMP)
         */
        const val DIB: Byte = 7
    }
}
