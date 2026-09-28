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

import com.wxiwei.office.fc.ddf.EscherContainerRecord
import com.wxiwei.office.fc.hslf.record.AnimationInfo
import com.wxiwei.office.fc.hslf.record.AnimationInfoAtom
import com.wxiwei.office.fc.hslf.record.ExMCIMovie
import com.wxiwei.office.fc.hslf.record.ExObjList
import com.wxiwei.office.fc.hslf.record.OEShapeAtom
import com.wxiwei.office.fc.hslf.record.Record
import com.wxiwei.office.fc.hslf.record.RecordTypes
import com.wxiwei.office.fc.hslf.usermodel.SlideShow

/**
 * Represents a movie in a PowerPoint document.
 * 
 * @author Yegor Kozlov
 */
class MovieShape : Picture {
    /**
     * Create a new `Picture`
     * 
     * @param pictureIdx the index of the picture
     */
    constructor(movieIdx: Int, pictureIdx: Int) : super(pictureIdx, null) {
        setMovieIndex(movieIdx)
        this.isAutoPlay = true
    }

    /**
     * Create a new `Picture`
     * 
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    constructor(movieIdx: Int, idx: Int, parent: Shape?) : super(idx, parent) {
        setMovieIndex(movieIdx)
    }

    /**
     * Create a `Picture` object
     * 
     * @param escherRecord the `EscherSpContainer` record which holds information about
     * this picture in the `Slide`
     * @param parent the parent shape of this picture
     */
    protected constructor(
        escherRecord: EscherContainerRecord?,
        parent: Shape?
    ) : super(escherRecord, parent)

    /**
     * Create a new Placeholder and initialize internal structures
     * 
     * @return the created `EscherContainerRecord` which holds shape data
     */
    override fun createSpContainer(idx: Int, isChild: Boolean): EscherContainerRecord? {
        spContainer = super.createSpContainer(idx, isChild)

        /*setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x1000100);
        setEscherProperty(EscherProperties.FILL__NOFILLHITTEST, 0x10001);

        EscherClientDataRecord cldata = new EscherClientDataRecord();
        cldata.setOptions((short)0xF);
        spContainer!!.addChildRecord(cldata);

        OEShapeAtom oe = new OEShapeAtom();
        InteractiveInfo info = new InteractiveInfo();
        InteractiveInfoAtom infoAtom = info.getInteractiveInfoAtom();
        infoAtom.setAction(InteractiveInfoAtom.ACTION_MEDIA);
        infoAtom.setHyperlinkType(InteractiveInfoAtom.LINK_NULL);

        AnimationInfo an = new AnimationInfo();
        AnimationInfoAtom anAtom = an.getAnimationInfoAtom();
        anAtom.setFlag(AnimationInfoAtom.Automatic, true);

        //convert hslf into ddf
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            oe.writeOut(out);
            info.writeOut(out);
        }
        catch(Exception e)
        {
            throw new HSLFException(e);
        }
        cldata.setRemainingData(out.toByteArray())*/


        return spContainer
    }

    /**
     * Assign a movie to this shape
     * 
     * @see SlideShow.addMovie
     * @param idx  the index of the movie
     */
    fun setMovieIndex(idx: Int) {
        val oe = getClientDataRecord(RecordTypes.OEShapeAtom.typeID) as OEShapeAtom
        oe.options = idx

        val an = getClientDataRecord(RecordTypes.AnimationInfo.typeID) as AnimationInfo?
        if (an != null) {
            val ai = an.animationInfoAtom!!
            ai.dimColor = 0x07000000
            ai.setFlag(AnimationInfoAtom.Automatic, true)
            ai.setFlag(AnimationInfoAtom.Play, true)
            ai.setFlag(AnimationInfoAtom.Synchronous, true)
            ai.orderID = idx + 1
        }
    }

    var isAutoPlay: Boolean
        get() {
            val an =
                getClientDataRecord(RecordTypes.AnimationInfo.typeID) as AnimationInfo?
            if (an != null) {
                return an.animationInfoAtom!!.getFlag(AnimationInfoAtom.Automatic)
            }
            return false
        }
        set(flag) {
            val an =
                getClientDataRecord(RecordTypes.AnimationInfo.typeID) as AnimationInfo?
            if (an != null) {
                an.animationInfoAtom!!.setFlag(AnimationInfoAtom.Automatic, flag)
                updateClientData()
            }
        }

    val path: String?
        /**
         * @return UNC or local path to a video file
         */
        get() {
            val oe = getClientDataRecord(RecordTypes.OEShapeAtom.typeID) as OEShapeAtom
            val idx = oe.options

            val ppt = sheet!!.slideShow!!
            val lst = ppt.documentRecord!!.findFirstOfType(
                RecordTypes.ExObjList.typeID.toLong()
            ) as ExObjList?
            if (lst == null) return null

            val r: Array<Record> = lst.getChildRecords()
            for (i in r.indices) {
                if (r[i] is ExMCIMovie) {
                    val mci = r[i] as ExMCIMovie
                    val exVideo = mci.exVideo!!
                    val objectId = exVideo.exMediaAtom!!.objectId
                    if (objectId == idx) {
                        return exVideo.pathAtom!!.text
                    }
                }
            }
            return null
        }

    companion object {
        val DEFAULT_MOVIE_THUMBNAIL: Int = -1

        const val MOVIE_MPEG: Int = 1
        const val MOVIE_AVI: Int = 2
    }
}
