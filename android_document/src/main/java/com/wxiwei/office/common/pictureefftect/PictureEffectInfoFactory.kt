/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          PictureEffectInforFactory.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:08:14
 */
package com.wxiwei.office.common.pictureefftect

import android.graphics.Color
import com.wxiwei.office.fc.ddf.EscherOptRecord
import com.wxiwei.office.fc.ddf.EscherProperties
import com.wxiwei.office.fc.ddf.EscherProperty
import com.wxiwei.office.fc.ddf.EscherSimpleProperty
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.hwpf.usermodel.Picture
import com.wxiwei.office.fc.ppt.reader.ReaderKit.Companion.instance
import kotlin.math.min

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2013-1-16
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
object PictureEffectInfoFactory {
    /**
     * effect information existed in the blipFill node
     * @param blipFill
     * @return
     */
    @JvmStatic
    fun getPictureEffectInfor(blipFill: Element?): PictureEffectInfo? {
        if (blipFill == null) {
            return null
        }

        val effectInfor = PictureEffectInfo()
        var validateInfor = false
        //crop information
        var e = blipFill.element("srcRect")
        var value: String? = null
        if (e != null) {
            //left

            var left = 0f
            value = e.attributeValue("l")
            if (value != null) {
                left = value.toInt() / 100000f
            }


            //top
            var top = 0f
            value = e.attributeValue("t")
            if (value != null) {
                top = value.toInt() / 100000f
            }


            //right
            var right = 0f
            value = e.attributeValue("r")
            if (value != null) {
                right = value.toInt() / 100000f
            }


            //bottom
            var bottom = 0f
            value = e.attributeValue("b")
            if (value != null) {
                bottom = value.toInt() / 100000f
            }

            if (left != 0f || top != 0f || right != 0f || bottom != 0f) {
                val croppedInfor = PictureCroppedInfo(left, top, right, bottom)

                validateInfor = true
                effectInfor.pictureCroppedInfor = croppedInfor
            }
        }

        /**////////////////////////////////////////////////picture effect */
        val blip = blipFill.element("blip")
        //gray scale
        if (blip!!.element("grayscl") != null) {
            validateInfor = true
            effectInfor.isGrayScale = true
        }


        //black&White
        e = blip!!.element("biLevel")
        if (e != null) {
            value = e.attributeValue("thresh")
            if (value != null) {
                validateInfor = true
                effectInfor.blackWhiteThreshold = value.toInt() / 100000f * 255
            }
        }


        //brightness and contrast
        e = blip!!.element("lum")
        if (e != null) {
            //brightness
            value = e.attributeValue("bright")
            if (value != null) {
                validateInfor = true
                val b = value.toInt() / 100000f
                effectInfor.brightness = b * 255
            }


            //contrast
            value = e.attributeValue("contrast")
            if (value != null) {
                validateInfor = true
                val c = value.toInt() / 100000f
                if (c > 0) {
                    effectInfor.setContrast(1 + c * 9)
                } else {
                    effectInfor.setContrast(1 + c)
                }
            }
        }


        //transparent color
        e = blip!!.element("clrChange")
        if (e != null && (e.element("clrFrom").also { e = it }) != null) {
            validateInfor = true
            effectInfor.transparentColor = instance().getColor(null, e)
        }

        if (validateInfor) {
            return effectInfor
        }

        return null
    }

    /**
     * effect information existed in the imagedata node
     * @param imagedata
     * @return
     */
    fun getPictureEffectInfor_ImageData(imagedata: Element?): PictureEffectInfo? {
        if (imagedata != null) {
            val effectInfor = PictureEffectInfo()
            var validateInfor = false


            //cropleft
            var left = 0f
            var value = imagedata.attributeValue("cropleft")
            if (value != null) {
                left = value.toFloat() / 65535
            }


            //croptop
            value = imagedata.attributeValue("croptop")
            var top = 0f
            if (value != null) {
                top = value.toFloat() / 65535
            }


            //cropright
            value = imagedata.attributeValue("cropright")
            var right = 0f
            if (value != null) {
                right = value.toFloat() / 65535
            }


            //cropbottom
            value = imagedata.attributeValue("cropbottom")
            var bottom = 0f
            if (value != null) {
                bottom = value.toFloat() / 65535
            }

            if (left != 0f || top != 0f || right != 0f || bottom != 0f) {
                val croppedInfor = PictureCroppedInfo(left, top, right, bottom)

                validateInfor = true
                effectInfor.pictureCroppedInfor = croppedInfor
            }


            //Image Brightness
            value = imagedata.attributeValue("blacklevel")
            if (value != null) {
                validateInfor = true
                var blacklevel = 0f
                if (value.contains("f")) {
                    blacklevel = value.toFloat() / 65535 * 2
                } else {
                    blacklevel = value.toFloat() * 2
                }
                effectInfor.brightness = blacklevel * 255
            }


            //Image Intensity(contrast)
            value = imagedata.attributeValue("gain")
            if (value != null) {
                validateInfor = true
                var gain = 0f
                if (value.contains("f")) {
                    gain = value.toFloat() / 65535
                } else {
                    gain = value.toFloat()
                }
                effectInfor.setContrast(gain)
            }


            //Image Grayscale Toggle
            val grayscale = imagedata.attributeValue("grayscale")
            if (grayscale != null && (grayscale.equals(
                    "t",
                    ignoreCase = true
                ) || grayscale.equals("true", ignoreCase = true))
            ) {
                validateInfor = true
                //Image Bilevel Toggle
                val bilevel = imagedata.attributeValue("bilevel")
                if (bilevel != null && (bilevel.equals(
                        "t",
                        ignoreCase = true
                    ) || bilevel.equals("true", ignoreCase = true))
                ) {
                    effectInfor.blackWhiteThreshold = 128f
                } else {
                    effectInfor.isGrayScale = true
                }
            }


            //Image Transparency Color
            val chromakey = imagedata.attributeValue("chromakey")
            if (chromakey != null) {
                validateInfor = true
                effectInfor.transparentColor = Color.parseColor(chromakey)
            }

            if (validateInfor) {
                return effectInfor
            }
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
            var iterator: MutableIterator<*> = opt.getEscherProperties().iterator()
            while (iterator.hasNext()) {
                val prop = iterator.next() as EscherProperty
                if (prop.getPropertyNumber().toInt() == propId) return prop
            }
        }
        return null
    }

    fun getPictureEffectInfor(pic: Picture?): PictureEffectInfo? {
        if (pic == null) {
            return null
        }

        val effectInfor = PictureEffectInfo()
        var validateInfor = false
        //crop information
        val value: String? = null

        //left
        val left = pic.getDxaCropLeft()
        val top = pic.getDyaCropTop()
        val right = pic.getDxaCropRight()
        val bottom = pic.getDyaCropBottom()

        if (left != 0f || top != 0f || right != 0f || bottom != 0f) {
            val croppedInfor = PictureCroppedInfo(left, top, right, bottom)

            validateInfor = true
            effectInfor.pictureCroppedInfor = croppedInfor
        }

        /**////////////////////////////////////////////////picture effect */
        if (pic.isSetBright()) {
            validateInfor = true
            effectInfor.brightness = pic.getBright()
        }
        if (pic.isSetContrast()) {
            validateInfor = true
            effectInfor.setContrast(pic.getContrast())
        }
        if (pic.isSetGrayScl()) {
            validateInfor = true
            effectInfor.isGrayScale = true
        }
        if (pic.isSetThreshold()) {
            validateInfor = true
            effectInfor.blackWhiteThreshold = pic.getThreshold()
        }


        //transparent color
        if (validateInfor) {
            return effectInfor
        }

        return null
    }

    @JvmStatic
    fun getPictureEffectInfor(opt: EscherOptRecord?): PictureEffectInfo? {
        if (opt == null) {
            return null
        }

        val effectInfor = PictureEffectInfo()
        var validateInfor = false
        //crop information
        val value: String? = null

        //left
        var prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__CROPFROMLEFT.toInt()
        ) as EscherSimpleProperty?
        val left = (if (prop == null) 0f else prop.getPropertyValue() / 65536f)


        //top 
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__CROPFROMTOP.toInt()
        ) as EscherSimpleProperty?
        val top = (if (prop == null) 0f else prop.getPropertyValue() / 65536f)


        //right
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__CROPFROMRIGHT.toInt()
        ) as EscherSimpleProperty?
        val right = (if (prop == null) 0f else prop.getPropertyValue() / 65536f)


        //bottom
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__CROPFROMBOTTOM.toInt()
        ) as EscherSimpleProperty?
        val bottom = (if (prop == null) 0f else prop.getPropertyValue() / 65536f)

        if (left != 0f || top != 0f || right != 0f || bottom != 0f) {
            val croppedInfor = PictureCroppedInfo(left, top, right, bottom)

            validateInfor = true
            effectInfor.pictureCroppedInfor = croppedInfor
        }

        /**////////////////////////////////////////////////picture effect */
        //gray scale, black&White
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__PICTUREACTIVE.toInt()
        ) as EscherSimpleProperty?
        if (prop != null) {
            val propValue = (prop.getPropertyValue() and 0x0F)
            if (propValue == 4) {
                //gray scale
                validateInfor = true
                effectInfor.isGrayScale = true
            } else if (propValue == 6) {
                // black&White
                validateInfor = true
                effectInfor.blackWhiteThreshold = 128f
            }
        }


        //brightness
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__BRIGHTNESSSETTING.toInt()
        ) as EscherSimpleProperty?
        if (prop != null) {
            validateInfor = true
            effectInfor.brightness = prop.getPropertyValue() / 32768f * 255
        }


        //contrast
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__CONTRASTSETTING.toInt()
        ) as EscherSimpleProperty?
        if (prop != null) {
            validateInfor = true
            effectInfor.setContrast(min(prop.getPropertyValue() / 65536f, 10f))
        }


        //transparent color
        prop = getEscherProperty(
            opt,
            EscherProperties.BLIP__TRANSPARENTCOLOR.toInt()
        ) as EscherSimpleProperty?
        if (prop != null) {
            validateInfor = true
            val color = prop.getPropertyValue()
            val r = color and 0xFF
            val g = (color and 0xFF00) shr 8
            val b = (color and 0xFF0000) shr 16
            effectInfor.transparentColor = Color.rgb(r, g, b)
        }

        if (validateInfor) {
            return effectInfor
        }

        return null
    }
}
