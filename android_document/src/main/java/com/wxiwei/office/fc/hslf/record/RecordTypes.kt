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
package com.wxiwei.office.fc.hslf.record

/**
 * List of all known record types in a PowerPoint document, and the
 * classes that handle them.
 * There are two categories of records:
 *  *  PowerPoint records: 0 <= info <= 10002 (will carry class info)
 *  *  Escher records: info >= 0xF000 (handled by DDF, so no class info)
 * 
 * @author Yegor Kozlov
 * @author Nick Burch
 */
object RecordTypes {
    @JvmField
    var typeToName: HashMap<Int?, String?> = HashMap()
    @JvmField
    var typeToClass: HashMap<Int?, Class<out Record?>?> = HashMap()

    @JvmField
    val Unknown: Type = Type(0, null)
    @JvmField
    val Document: Type = Type(1000, com.wxiwei.office.fc.hslf.record.Document::class.java)
    @JvmField
    val DocumentAtom: Type = Type(1001, com.wxiwei.office.fc.hslf.record.DocumentAtom::class.java)
    @JvmField
    val EndDocument: Type = Type(1002, null)
    @JvmField
    val Slide: Type = Type(1006, com.wxiwei.office.fc.hslf.record.Slide::class.java)
    @JvmField
    val SlideAtom: Type = Type(1007, com.wxiwei.office.fc.hslf.record.SlideAtom::class.java)
    @JvmField
    val Notes: Type = Type(1008, com.wxiwei.office.fc.hslf.record.Notes::class.java)
    @JvmField
    val NotesAtom: Type = Type(1009, com.wxiwei.office.fc.hslf.record.NotesAtom::class.java)
    @JvmField
    val Environment: Type = Type(1010, com.wxiwei.office.fc.hslf.record.Environment::class.java)
    @JvmField
    val SlidePersistAtom: Type = Type(1011, com.wxiwei.office.fc.hslf.record.SlidePersistAtom::class.java)
    @JvmField
    val SSlideLayoutAtom: Type = Type(1015, null)
    @JvmField
    val MainMaster: Type = Type(1016, com.wxiwei.office.fc.hslf.record.MainMaster::class.java)

    //public static final Type SSSlideInfoAtom = new Type(1017,null);
    @JvmField
    val SlideViewInfo: Type = Type(1018, null)
    @JvmField
    val GuideAtom: Type = Type(1019, null)
    @JvmField
    val ViewInfo: Type = Type(1020, null)
    @JvmField
    val ViewInfoAtom: Type = Type(1021, null)
    @JvmField
    val SlideViewInfoAtom: Type = Type(1022, null)
    @JvmField
    val VBAInfo: Type = Type(1023, null)
    @JvmField
    val VBAInfoAtom: Type = Type(1024, null)
    @JvmField
    val SSDocInfoAtom: Type = Type(1025, null)
    @JvmField
    val Summary: Type = Type(1026, null)
    @JvmField
    val DocRoutingSlip: Type = Type(1030, null)
    @JvmField
    val OutlineViewInfo: Type = Type(1031, null)
    @JvmField
    val SorterViewInfo: Type = Type(1032, null)
    @JvmField
    val ExObjList: Type = Type(1033, com.wxiwei.office.fc.hslf.record.ExObjList::class.java)
    @JvmField
    val ExObjListAtom: Type = Type(1034, com.wxiwei.office.fc.hslf.record.ExObjListAtom::class.java)
    @JvmField
    val PPDrawingGroup: Type = Type(1035, com.wxiwei.office.fc.hslf.record.PPDrawingGroup::class.java)
    @JvmField
    val PPDrawing: Type = Type(1036, com.wxiwei.office.fc.hslf.record.PPDrawing::class.java)
    @JvmField
    val NamedShows: Type = Type(1040, null)
    @JvmField
    val NamedShow: Type = Type(1041, null)
    @JvmField
    val NamedShowSlides: Type = Type(1042, null)
    @JvmField
    val SheetProperties: Type = Type(1044, null)
    @JvmField
    val List: Type = Type(2000, com.wxiwei.office.fc.hslf.record.List::class.java)
    @JvmField
    val FontCollection: Type = Type(2005, com.wxiwei.office.fc.hslf.record.FontCollection::class.java)
    @JvmField
    val BookmarkCollection: Type = Type(2019, null)
    @JvmField
    val SoundCollection: Type = Type(2020, com.wxiwei.office.fc.hslf.record.SoundCollection::class.java)
    @JvmField
    val SoundCollAtom: Type = Type(2021, null)
    @JvmField
    val Sound: Type = Type(2022, com.wxiwei.office.fc.hslf.record.Sound::class.java)
    @JvmField
    val SoundData: Type = Type(2023, com.wxiwei.office.fc.hslf.record.SoundData::class.java)
    @JvmField
    val BookmarkSeedAtom: Type = Type(2025, null)
    @JvmField
    val ColorSchemeAtom: Type = Type(2032, com.wxiwei.office.fc.hslf.record.ColorSchemeAtom::class.java)
    @JvmField
    val ExObjRefAtom: Type = Type(3009, null)
    @JvmField
    val OEShapeAtom: Type = Type(3009, com.wxiwei.office.fc.hslf.record.OEShapeAtom::class.java)
    @JvmField
    val OEPlaceholderAtom: Type = Type(3011, com.wxiwei.office.fc.hslf.record.OEPlaceholderAtom::class.java)
    @JvmField
    val GPopublicintAtom: Type = Type(3024, null)
    @JvmField
    val GRatioAtom: Type = Type(3031, null)
    @JvmField
    val OutlineTextRefAtom: Type = Type(3998, com.wxiwei.office.fc.hslf.record.OutlineTextRefAtom::class.java)
    @JvmField
    val TextHeaderAtom: Type = Type(3999, com.wxiwei.office.fc.hslf.record.TextHeaderAtom::class.java)
    @JvmField
    val TextCharsAtom: Type = Type(4000, com.wxiwei.office.fc.hslf.record.TextCharsAtom::class.java)
    @JvmField
    val StyleTextPropAtom: Type = Type(4001, com.wxiwei.office.fc.hslf.record.StyleTextPropAtom::class.java)
    @JvmField
    val BaseTextPropAtom: Type = Type(4002, null)
    @JvmField
    val TxMasterStyleAtom: Type = Type(4003, com.wxiwei.office.fc.hslf.record.TxMasterStyleAtom::class.java)
    @JvmField
    val TxCFStyleAtom: Type = Type(4004, null)
    @JvmField
    val TxPFStyleAtom: Type = Type(4005, null)
    @JvmField
    val TextRulerAtom: Type = Type(4006, com.wxiwei.office.fc.hslf.record.TextRulerAtom::class.java)
    @JvmField
    val TextBookmarkAtom: Type = Type(4007, null)
    @JvmField
    val TextBytesAtom: Type = Type(4008, com.wxiwei.office.fc.hslf.record.TextBytesAtom::class.java)
    @JvmField
    val TxSIStyleAtom: Type = Type(4009, null)
    @JvmField
    val TextSpecInfoAtom: Type = Type(4010, com.wxiwei.office.fc.hslf.record.TextSpecInfoAtom::class.java)
    @JvmField
    val DefaultRulerAtom: Type = Type(4011, null)
    @JvmField
    val ExtendedParagraphAtom: Type = Type(4012, com.wxiwei.office.fc.hslf.record.ExtendedParagraphAtom::class.java)
    @JvmField
    val ExtendedPreRuleContainer: Type = Type(4014, com.wxiwei.office.fc.hslf.record.ExtendedPresRuleContainer::class.java)
    @JvmField
    val ExtendedParagraphHeaderAtom: Type =
        Type(4015, com.wxiwei.office.fc.hslf.record.ExtendedParagraphHeaderAtom::class.java)
    @JvmField
    val FontEntityAtom: Type = Type(4023, com.wxiwei.office.fc.hslf.record.FontEntityAtom::class.java)
    @JvmField
    val FontEmbeddedData: Type = Type(4024, null)
    @JvmField
    val CString: Type = Type(4026, com.wxiwei.office.fc.hslf.record.CString::class.java)
    @JvmField
    val MetaFile: Type = Type(4033, null)
    @JvmField
    val ExOleObjAtom: Type = Type(4035, com.wxiwei.office.fc.hslf.record.ExOleObjAtom::class.java)
    @JvmField
    val SrKinsoku: Type = Type(4040, null)
    @JvmField
    val HandOut: Type = Type(4041, com.wxiwei.office.fc.hslf.record.DummyPositionSensitiveRecordWithChildren::class.java)
    @JvmField
    val ExEmbed: Type = Type(4044, com.wxiwei.office.fc.hslf.record.ExEmbed::class.java)
    @JvmField
    val ExEmbedAtom: Type = Type(4045, com.wxiwei.office.fc.hslf.record.ExEmbedAtom::class.java)
    @JvmField
    val ExLink: Type = Type(4046, null)
    @JvmField
    val BookmarkEntityAtom: Type = Type(4048, null)
    @JvmField
    val ExLinkAtom: Type = Type(4049, null)
    @JvmField
    val SrKinsokuAtom: Type = Type(4050, null)
    @JvmField
    val ExHyperlinkAtom: Type = Type(4051, com.wxiwei.office.fc.hslf.record.ExHyperlinkAtom::class.java)
    @JvmField
    val ExHyperlink: Type = Type(4055, com.wxiwei.office.fc.hslf.record.ExHyperlink::class.java)
    @JvmField
    val SlideNumberMCAtom: Type = Type(4056, null)
    @JvmField
    val HeadersFooters: Type = Type(4057, com.wxiwei.office.fc.hslf.record.HeadersFootersContainer::class.java)
    @JvmField
    val HeadersFootersAtom: Type = Type(4058, com.wxiwei.office.fc.hslf.record.HeadersFootersAtom::class.java)
    @JvmField
    val TxInteractiveInfoAtom: Type = Type(4063, com.wxiwei.office.fc.hslf.record.TxInteractiveInfoAtom::class.java)
    @JvmField
    val CharFormatAtom: Type = Type(4066, null)
    @JvmField
    val ParaFormatAtom: Type = Type(4067, null)
    @JvmField
    val RecolorInfoAtom: Type = Type(4071, null)
    @JvmField
    val ExQuickTimeMovie: Type = Type(4074, null)
    @JvmField
    val ExQuickTimeMovieData: Type = Type(4075, null)
    @JvmField
    val ExControl: Type = Type(4078, com.wxiwei.office.fc.hslf.record.ExControl::class.java)
    @JvmField
    val SlideListWithText: Type = Type(4080, com.wxiwei.office.fc.hslf.record.SlideListWithText::class.java)
    @JvmField
    val InteractiveInfo: Type = Type(4082, com.wxiwei.office.fc.hslf.record.InteractiveInfo::class.java)
    @JvmField
    val InteractiveInfoAtom: Type = Type(4083, com.wxiwei.office.fc.hslf.record.InteractiveInfoAtom::class.java)
    @JvmField
    val UserEditAtom: Type = Type(4085, com.wxiwei.office.fc.hslf.record.UserEditAtom::class.java)
    @JvmField
    val CurrentUserAtom: Type = Type(4086, null)
    @JvmField
    val DateTimeMCAtom: Type = Type(4087, null)
    @JvmField
    val GenericDateMCAtom: Type = Type(4088, null)
    @JvmField
    val FooterMCAtom: Type = Type(4090, null)
    @JvmField
    val ExControlAtom: Type = Type(4091, com.wxiwei.office.fc.hslf.record.ExControlAtom::class.java)
    @JvmField
    val ExMediaAtom: Type = Type(4100, com.wxiwei.office.fc.hslf.record.ExMediaAtom::class.java)
    @JvmField
    val ExVideoContainer: Type = Type(4101, com.wxiwei.office.fc.hslf.record.ExVideoContainer::class.java)
    @JvmField
    val ExAviMovie: Type = Type(4102, com.wxiwei.office.fc.hslf.record.ExAviMovie::class.java)
    @JvmField
    val ExMCIMovie: Type = Type(4103, com.wxiwei.office.fc.hslf.record.ExMCIMovie::class.java)
    @JvmField
    val ExMIDIAudio: Type = Type(4109, null)
    @JvmField
    val ExCDAudio: Type = Type(4110, null)
    @JvmField
    val ExWAVAudioEmbedded: Type = Type(4111, null)
    @JvmField
    val ExWAVAudioLink: Type = Type(4112, null)
    @JvmField
    val ExOleObjStg: Type = Type(4113, com.wxiwei.office.fc.hslf.record.ExOleObjStg::class.java)
    @JvmField
    val ExCDAudioAtom: Type = Type(4114, null)
    @JvmField
    val ExWAVAudioEmbeddedAtom: Type = Type(4115, null)
    @JvmField
    val AnimationInfo: Type = Type(4116, com.wxiwei.office.fc.hslf.record.AnimationInfo::class.java)
    @JvmField
    val AnimationInfoAtom: Type = Type(4081, com.wxiwei.office.fc.hslf.record.AnimationInfoAtom::class.java)
    @JvmField
    val RTFDateTimeMCAtom: Type = Type(4117, null)

    //    public static final Type ProgTags = new Type(5000,DummyPositionSensitiveRecordWithChildren.class);
    @JvmField
    val ProgStringTag: Type = Type(5001, null)

    //    public static final Type ProgBinaryTag = new Type(5002,DummyPositionSensitiveRecordWithChildren.class);
    //    public static final Type BinaryTagData = new Type(5003,DummyPositionSensitiveRecordWithChildren.class);
    @JvmField
    val PrpublicintOptions: Type = Type(6000, null)
    @JvmField
    val PersistPtrFullBlock: Type = Type(6001, com.wxiwei.office.fc.hslf.record.PersistPtrHolder::class.java)
    @JvmField
    val PersistPtrIncrementalBlock: Type = Type(6002, com.wxiwei.office.fc.hslf.record.PersistPtrHolder::class.java)
    @JvmField
    val GScalingAtom: Type = Type(10001, null)
    @JvmField
    val GRColorAtom: Type = Type(10002, null)

    // Records ~12000 seem to be related to the Comments used in PPT 2000/XP
    // (Comments in PPT97 are normal Escher text boxes)
    @JvmField
    val Comment2000: Type = Type(12000, com.wxiwei.office.fc.hslf.record.Comment2000::class.java)
    @JvmField
    val Comment2000Atom: Type = Type(12001, com.wxiwei.office.fc.hslf.record.Comment2000Atom::class.java)
    @JvmField
    val Comment2000Summary: Type = Type(12004, null)
    @JvmField
    val Comment2000SummaryAtom: Type = Type(12005, null)

    //animation record
    @JvmField
    val SlideProgTagsContainer: Type = Type(5000, com.wxiwei.office.fc.hslf.record.SlideProgTagsContainer::class.java)
    @JvmField
    val SlideProgBinaryTagContainer: Type =
        Type(5002, com.wxiwei.office.fc.hslf.record.SlideProgBinaryTagContainer::class.java)
    @JvmField
    val BinaryTagDataBlob: Type = Type(5003, com.wxiwei.office.fc.hslf.record.BinaryTagDataBlob::class.java)

    @JvmField
    val SlideShowSlideInfoAtom: Type = Type(0x03F9, com.wxiwei.office.fc.hslf.record.SlideShowSlideInfoAtom::class.java)

    @JvmField
    val SlideTimeAtom: Type = Type(12011, com.wxiwei.office.fc.hslf.record.SlideTimeAtom::class.java)
    @JvmField
    val TimeNodeContainer: Type = Type(61764, com.wxiwei.office.fc.hslf.record.TimeNodeContainer::class.java)
    @JvmField
    val TimeNodeAtom: Type = Type(61735, com.wxiwei.office.fc.hslf.record.TimeNodeAtom::class.java)
    @JvmField
    val TimeNodeAttributeContainer: Type =
        Type(61757, com.wxiwei.office.fc.hslf.record.TimeNodeAttributeContainer::class.java)
    @JvmField
    val TimeConditionContainer: Type = Type(61733, com.wxiwei.office.fc.hslf.record.TimeConditionContainer::class.java)
    @JvmField
    val TimeConditionAtom: Type = Type(61736, com.wxiwei.office.fc.hslf.record.TimeConditionAtom::class.java)
    @JvmField
    val TimeVariant: Type = Type(61762, com.wxiwei.office.fc.hslf.record.TimeVariant::class.java)

    @JvmField
    val TimeSetBehaviorContainer: Type =
        Type(61745, com.wxiwei.office.fc.hslf.record.TimeSetBehaviorContainer::class.java)
    @JvmField
    val TimeAnimateBehaviorContainer: Type =
        Type(61739, com.wxiwei.office.fc.hslf.record.TimeAnimateBehaviorContainer::class.java)
    @JvmField
    val TimeBehaviorContainer: Type = Type(61738, com.wxiwei.office.fc.hslf.record.TimeBehaviorContainer::class.java)
    @JvmField
    val ClientVisualElementContainer: Type =
        Type(61756, com.wxiwei.office.fc.hslf.record.ClientVisualElementContainer::class.java)
    @JvmField
    val VisualShapeAtom: Type = Type(11003, com.wxiwei.office.fc.hslf.record.VisualShapeAtom::class.java)

    @JvmField
    val SlaveContainer: Type = Type(0xF145, com.wxiwei.office.fc.hslf.record.SlaveContainer::class.java)
    @JvmField
    val TimeColorBehaviorAtom: Type = Type(0xF135, com.wxiwei.office.fc.hslf.record.TimeColorBehaviorAtom::class.java)
    @JvmField
    val TimeColorBehaviorContainer: Type =
        Type(0xF12C, com.wxiwei.office.fc.hslf.record.TimeColorBehaviorContainer::class.java)
    @JvmField
    val TimeCommandBehaviorContainer: Type =
        Type(0xF132, com.wxiwei.office.fc.hslf.record.TimeCommandBehaviorContainer::class.java)
    @JvmField
    val TimeEffectBehaviorContainer: Type =
        Type(0xF12D, com.wxiwei.office.fc.hslf.record.TimeEffectBehaviorContainer::class.java)
    @JvmField
    val TimeIterateDataAtom: Type = Type(0xF140, com.wxiwei.office.fc.hslf.record.TimeIterateDataAtom::class.java)
    @JvmField
    val TimeMotionBehaviorContainer: Type =
        Type(0xF12E, com.wxiwei.office.fc.hslf.record.TimeMotionBehaviorContainer::class.java)
    @JvmField
    val TimeRotationBehaviorContainer: Type =
        Type(0xF12F, com.wxiwei.office.fc.hslf.record.TimeRotationBehaviorContainer::class.java)
    @JvmField
    val TimeScaleBehaviorContainer: Type =
        Type(0xF130, com.wxiwei.office.fc.hslf.record.TimeScaleBehaviorContainer::class.java)
    @JvmField
    val TimeSequenceDataAtom: Type = Type(0xF141, com.wxiwei.office.fc.hslf.record.TimeSequenceDataAtom::class.java)

    // Records ~12050 seem to be related to Document Encryption
    @JvmField
    val DocumentEncryptionAtom: Type = Type(12052, com.wxiwei.office.fc.hslf.record.DocumentEncryptionAtom::class.java)

    @JvmField
    val OriginalMainMasterId: Type = Type(1052, null)
    @JvmField
    val CompositeMasterId: Type = Type(1052, null)
    @JvmField
    val RoundTripContentMasterInfo12: Type = Type(1054, null)
    @JvmField
    val RoundTripShapeId12: Type = Type(1055, null)
    @JvmField
    val RoundTripHFPlaceholder12: Type =
        Type(1056, com.wxiwei.office.fc.hslf.record.RoundTripHFPlaceholder12::class.java)
    @JvmField
    val RoundTripContentMasterId: Type = Type(1058, null)
    @JvmField
    val RoundTripOArtTextStyles12: Type = Type(1059, null)
    @JvmField
    val RoundTripShapeCheckSumForCustomLayouts12: Type = Type(1062, null)
    @JvmField
    val RoundTripNotesMasterTextStyles12: Type = Type(1063, null)
    @JvmField
    val RoundTripCustomTableStyles12: Type = Type(1064, null)

    //records greater then 0xF000 belong to with Microsoft Office Drawing format also known as Escher
    const val EscherDggContainer: Int = 0xf000
    const val EscherDgg: Int = 0xf006
    const val EscherCLSID: Int = 0xf016
    const val EscherOPT: Int = 0xf00b
    const val EscherBStoreContainer: Int = 0xf001
    const val EscherBSE: Int = 0xf007
    const val EscherBlip_START: Int = 0xf018
    const val EscherBlip_END: Int = 0xf117
    const val EscherDgContainer: Int = 0xf002
    const val EscherDg: Int = 0xf008
    const val EscherRegroupItems: Int = 0xf118
    const val EscherColorScheme: Int = 0xf120
    const val EscherSpgrContainer: Int = 0xf003
    const val EscherSpContainer: Int = 0xf004
    const val EscherSpgr: Int = 0xf009
    const val EscherSp: Int = 0xf00a
    const val EscherTextbox: Int = 0xf00c
    const val EscherClientTextbox: Int = 0xf00d
    const val EscherAnchor: Int = 0xf00e
    const val EscherChildAnchor: Int = 0xf00f
    const val EscherClientAnchor: Int = 0xf010
    const val EscherClientData: Int = 0xf011
    const val EscherSolverContainer: Int = 0xf005
    const val EscherConnectorRule: Int = 0xf012
    const val EscherAlignRule: Int = 0xf013
    const val EscherArcRule: Int = 0xf014
    const val EscherClientRule: Int = 0xf015
    const val EscherCalloutRule: Int = 0xf017
    const val EscherSelection: Int = 0xf119
    const val EscherColorMRU: Int = 0xf11a
    const val EscherDeletedPspl: Int = 0xf11d
    const val EscherSplitMenuColors: Int = 0xf11e
    const val EscherOleObject: Int = 0xf11f
    const val EscherUserDefined: Int = 0xf122

    /**
     * Returns name of the record by its type
     * 
     * @param type section of the record header
     * @return name of the record
     */
    fun recordName(type: Int): String {
        var name = typeToName.get(type)
        if (name == null) name = "Unknown" + type
        return name
    }

    /**
     * Returns the class handling a record by its type.
     * If given an un-handled PowerPoint record, will return a dummy
     * placeholder class. If given an unknown PowerPoint record, or
     * and Escher record, will return null.
     * 
     * @param type section of the record header
     * @return class to handle the record, or null if an unknown (eg Escher) record
     */
    fun recordHandlingClass(type: Int): Class<out Record?>? {
        val c = typeToClass.get(type)
        return c
    }

    init {
        try {
            val f = RecordTypes::class.java.getFields()
            for (i in f.indices) {
                val `val` = f[i]!!.get(null)

                // Escher record, only store ID -> Name
                if (`val` is Int) {
                    typeToName.put(`val`, f[i]!!.getName())
                }
                // PowerPoint record, store ID -> Name and ID -> Class
                if (`val` is Type) {
                    val t = `val`
                    var c = t.handlingClass
                    val id = t.typeID
                    if (c == null) {
                        c = UnknownRecordPlaceholder::class.java
                    }

                    typeToName.put(id, f[i]!!.getName())
                    typeToClass.put(id, c)
                }
            }
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Failed to initialize records types")
        }
    }


    /**
     * Wrapper for the details of a PowerPoint or Escher record type.
     * Contains both the type, and the handling class (if any), and
     * offers methods to get either back out.
     */
    class Type(var typeID: Int, var handlingClass: Class<out Record?>?)
}
