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
package com.wxiwei.office.fc.ddf

/**
 * Provides a list of all known escher properties including the description and
 * type.
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
object EscherProperties {
    // Property constants
    const val TRANSFORM__ROTATION: Short = 4
    const val PROTECTION__LOCKROTATION: Short = 119
    const val PROTECTION__LOCKASPECTRATIO: Short = 120
    const val PROTECTION__LOCKPOSITION: Short = 121
    const val PROTECTION__LOCKAGAINSTSELECT: Short = 122
    const val PROTECTION__LOCKCROPPING: Short = 123
    const val PROTECTION__LOCKVERTICES: Short = 124
    const val PROTECTION__LOCKTEXT: Short = 125
    const val PROTECTION__LOCKADJUSTHANDLES: Short = 126
    const val PROTECTION__LOCKAGAINSTGROUPING: Short = 127
    const val TEXT__TEXTID: Short = 128
    const val TEXT__TEXTLEFT: Short = 129
    const val TEXT__TEXTTOP: Short = 130
    const val TEXT__TEXTRIGHT: Short = 131
    const val TEXT__TEXTBOTTOM: Short = 132
    const val TEXT__WRAPTEXT: Short = 133
    const val TEXT__SCALETEXT: Short = 134
    const val TEXT__ANCHORTEXT: Short = 135
    const val TEXT__TEXTFLOW: Short = 136
    const val TEXT__FONTROTATION: Short = 137
    const val TEXT__IDOFNEXTSHAPE: Short = 138
    const val TEXT__BIDIR: Short = 139
    const val TEXT__SINGLECLICKSELECTS: Short = 187
    const val TEXT__USEHOSTMARGINS: Short = 188
    const val TEXT__ROTATETEXTWITHSHAPE: Short = 189
    const val TEXT__SIZESHAPETOFITTEXT: Short = 190
    const val TEXT__SIZE_TEXT_TO_FIT_SHAPE: Short = 191
    const val GEOTEXT__UNICODE: Short = 192
    const val GEOTEXT__RTFTEXT: Short = 193
    const val GEOTEXT__ALIGNMENTONCURVE: Short = 194
    const val GEOTEXT__DEFAULTPOINTSIZE: Short = 195
    const val GEOTEXT__TEXTSPACING: Short = 196
    const val GEOTEXT__FONTFAMILYNAME: Short = 197
    const val GEOTEXT__REVERSEROWORDER: Short = 240
    const val GEOTEXT__HASTEXTEFFECT: Short = 241
    const val GEOTEXT__ROTATECHARACTERS: Short = 242
    const val GEOTEXT__KERNCHARACTERS: Short = 243
    const val GEOTEXT__TIGHTORTRACK: Short = 244
    const val GEOTEXT__STRETCHTOFITSHAPE: Short = 245
    const val GEOTEXT__CHARBOUNDINGBOX: Short = 246
    const val GEOTEXT__SCALETEXTONPATH: Short = 247
    const val GEOTEXT__STRETCHCHARHEIGHT: Short = 248
    const val GEOTEXT__NOMEASUREALONGPATH: Short = 249
    const val GEOTEXT__BOLDFONT: Short = 250
    const val GEOTEXT__ITALICFONT: Short = 251
    const val GEOTEXT__UNDERLINEFONT: Short = 252
    const val GEOTEXT__SHADOWFONT: Short = 253
    const val GEOTEXT__SMALLCAPSFONT: Short = 254
    const val GEOTEXT__STRIKETHROUGHFONT: Short = 255
    const val BLIP__CROPFROMTOP: Short = 256
    const val BLIP__CROPFROMBOTTOM: Short = 257
    const val BLIP__CROPFROMLEFT: Short = 258
    const val BLIP__CROPFROMRIGHT: Short = 259
    const val BLIP__BLIPTODISPLAY: Short = 260
    const val BLIP__BLIPFILENAME: Short = 261
    const val BLIP__BLIPFLAGS: Short = 262
    const val BLIP__TRANSPARENTCOLOR: Short = 263
    const val BLIP__CONTRASTSETTING: Short = 264
    const val BLIP__BRIGHTNESSSETTING: Short = 265
    const val BLIP__GAMMA: Short = 266
    const val BLIP__PICTUREID: Short = 267
    const val BLIP__DOUBLEMOD: Short = 268
    const val BLIP__PICTUREFILLMOD: Short = 269
    const val BLIP__PICTURELINE: Short = 270
    const val BLIP__PRINTBLIP: Short = 271
    const val BLIP__PRINTBLIPFILENAME: Short = 272
    const val BLIP__PRINTFLAGS: Short = 273
    const val BLIP__NOHITTESTPICTURE: Short = 316
    const val BLIP__PICTUREGRAY: Short = 317
    const val BLIP__PICTUREBILEVEL: Short = 318
    const val BLIP__PICTUREACTIVE: Short = 319
    const val GEOMETRY__LEFT: Short = 320
    const val GEOMETRY__TOP: Short = 321
    const val GEOMETRY__RIGHT: Short = 322
    const val GEOMETRY__BOTTOM: Short = 323
    const val GEOMETRY__SHAPEPATH: Short = 324
    const val GEOMETRY__VERTICES: Short = 325
    const val GEOMETRY__SEGMENTINFO: Short = 326
    const val GEOMETRY__ADJUSTVALUE: Short = 327
    const val GEOMETRY__ADJUST2VALUE: Short = 328
    const val GEOMETRY__ADJUST3VALUE: Short = 329
    const val GEOMETRY__ADJUST4VALUE: Short = 330
    const val GEOMETRY__ADJUST5VALUE: Short = 331
    const val GEOMETRY__ADJUST6VALUE: Short = 332
    const val GEOMETRY__ADJUST7VALUE: Short = 333
    const val GEOMETRY__ADJUST8VALUE: Short = 334
    const val GEOMETRY__ADJUST9VALUE: Short = 335
    const val GEOMETRY__ADJUST10VALUE: Short = 336
    const val GEOMETRY__SHADOWok: Short = 378
    const val GEOMETRY__3DOK: Short = 379
    const val GEOMETRY__LINEOK: Short = 380
    const val GEOMETRY__GEOTEXTOK: Short = 381
    const val GEOMETRY__FILLSHADESHAPEOK: Short = 382
    const val GEOMETRY__FILLOK: Short = 383
    const val FILL__FILLTYPE: Short = 384
    const val FILL__FILLCOLOR: Short = 385
    const val FILL__FILLOPACITY: Short = 386
    const val FILL__FILLBACKCOLOR: Short = 387
    const val FILL__BACKOPACITY: Short = 388
    const val FILL__CRMOD: Short = 389
    const val FILL__PATTERNTEXTURE: Short = 390
    const val FILL__BLIPFILENAME: Short = 391
    const val FILL__BLIPFLAGS: Short = 392
    const val FILL__WIDTH: Short = 393
    const val FILL__HEIGHT: Short = 394
    const val FILL__ANGLE: Short = 395
    const val FILL__FOCUS: Short = 396
    const val FILL__TOLEFT: Short = 397
    const val FILL__TOTOP: Short = 398
    const val FILL__TORIGHT: Short = 399
    const val FILL__TOBOTTOM: Short = 400
    const val FILL__RECTLEFT: Short = 401
    const val FILL__RECTTOP: Short = 402
    const val FILL__RECTRIGHT: Short = 403
    const val FILL__RECTBOTTOM: Short = 404
    const val FILL__DZTYPE: Short = 405
    const val FILL__SHADEPRESET: Short = 406
    const val FILL__SHADECOLORS: Short = 407
    const val FILL__ORIGINX: Short = 408
    const val FILL__ORIGINY: Short = 409
    const val FILL__SHAPEORIGINX: Short = 410
    const val FILL__SHAPEORIGINY: Short = 411
    const val FILL__SHADETYPE: Short = 412
    const val FILL__FILLED: Short = 443
    const val FILL__HITTESTFILL: Short = 444
    const val FILL__SHAPE: Short = 445
    const val FILL__USERECT: Short = 446
    const val FILL__NOFILLHITTEST: Short = 447
    const val LINESTYLE__COLOR: Short = 448
    const val LINESTYLE__OPACITY: Short = 449
    const val LINESTYLE__BACKCOLOR: Short = 450
    const val LINESTYLE__CRMOD: Short = 451
    const val LINESTYLE__LINETYPE: Short = 452
    const val LINESTYLE__FILLBLIP: Short = 453
    const val LINESTYLE__FILLBLIPNAME: Short = 454
    const val LINESTYLE__FILLBLIPFLAGS: Short = 455
    const val LINESTYLE__FILLWIDTH: Short = 456
    const val LINESTYLE__FILLHEIGHT: Short = 457
    const val LINESTYLE__FILLDZTYPE: Short = 458
    const val LINESTYLE__LINEWIDTH: Short = 459
    const val LINESTYLE__LINEMITERLIMIT: Short = 460
    const val LINESTYLE__LINESTYLE: Short = 461
    const val LINESTYLE__LINEDASHING: Short = 462
    const val LINESTYLE__LINEDASHSTYLE: Short = 463
    const val LINESTYLE__LINESTARTARROWHEAD: Short = 464
    const val LINESTYLE__LINEENDARROWHEAD: Short = 465
    const val LINESTYLE__LINESTARTARROWWIDTH: Short = 466
    const val LINESTYLE__LINEESTARTARROWLENGTH: Short = 467
    const val LINESTYLE__LINEENDARROWWIDTH: Short = 468
    const val LINESTYLE__LINEENDARROWLENGTH: Short = 469
    const val LINESTYLE__LINEJOINSTYLE: Short = 470
    const val LINESTYLE__LINEENDCAPSTYLE: Short = 471
    const val LINESTYLE__ARROWHEADSOK: Short = 507
    const val LINESTYLE__ANYLINE: Short = 508
    const val LINESTYLE__HITLINETEST: Short = 509
    const val LINESTYLE__LINEFILLSHAPE: Short = 510
    const val LINESTYLE__NOLINEDRAWDASH: Short = 511
    const val SHADOWSTYLE__TYPE: Short = 512
    const val SHADOWSTYLE__COLOR: Short = 513
    const val SHADOWSTYLE__HIGHLIGHT: Short = 514
    const val SHADOWSTYLE__CRMOD: Short = 515
    const val SHADOWSTYLE__OPACITY: Short = 516
    const val SHADOWSTYLE__OFFSETX: Short = 517
    const val SHADOWSTYLE__OFFSETY: Short = 518
    const val SHADOWSTYLE__SECONDOFFSETX: Short = 519
    const val SHADOWSTYLE__SECONDOFFSETY: Short = 520
    const val SHADOWSTYLE__SCALEXTOX: Short = 521
    const val SHADOWSTYLE__SCALEYTOX: Short = 522
    const val SHADOWSTYLE__SCALEXTOY: Short = 523
    const val SHADOWSTYLE__SCALEYTOY: Short = 524
    const val SHADOWSTYLE__PERSPECTIVEX: Short = 525
    const val SHADOWSTYLE__PERSPECTIVEY: Short = 526
    const val SHADOWSTYLE__WEIGHT: Short = 527
    const val SHADOWSTYLE__ORIGINX: Short = 528
    const val SHADOWSTYLE__ORIGINY: Short = 529
    const val SHADOWSTYLE__SHADOW: Short = 574
    const val SHADOWSTYLE__SHADOWOBSURED: Short = 575
    const val PERSPECTIVE__TYPE: Short = 576
    const val PERSPECTIVE__OFFSETX: Short = 577
    const val PERSPECTIVE__OFFSETY: Short = 578
    const val PERSPECTIVE__SCALEXTOX: Short = 579
    const val PERSPECTIVE__SCALEYTOX: Short = 580
    const val PERSPECTIVE__SCALEXTOY: Short = 581
    const val PERSPECTIVE__SCALEYTOY: Short = 582
    const val PERSPECTIVE__PERSPECTIVEX: Short = 583
    const val PERSPECTIVE__PERSPECTIVEY: Short = 584
    const val PERSPECTIVE__WEIGHT: Short = 585
    const val PERSPECTIVE__ORIGINX: Short = 586
    const val PERSPECTIVE__ORIGINY: Short = 587
    const val PERSPECTIVE__PERSPECTIVEON: Short = 639
    const val THREED__SPECULARAMOUNT: Short = 640
    const val THREED__DIFFUSEAMOUNT: Short = 661
    const val THREED__SHININESS: Short = 662
    const val THREED__EDGETHICKNESS: Short = 663
    const val THREED__EXTRUDEFORWARD: Short = 664
    const val THREED__EXTRUDEBACKWARD: Short = 665
    const val THREED__EXTRUDEPLANE: Short = 666
    const val THREED__EXTRUSIONCOLOR: Short = 667
    const val THREED__CRMOD: Short = 648
    const val THREED__3DEFFECT: Short = 700
    const val THREED__METALLIC: Short = 701
    const val THREED__USEEXTRUSIONCOLOR: Short = 702
    const val THREED__LIGHTFACE: Short = 703
    const val THREEDSTYLE__YROTATIONANGLE: Short = 704
    const val THREEDSTYLE__XROTATIONANGLE: Short = 705
    const val THREEDSTYLE__ROTATIONAXISX: Short = 706
    const val THREEDSTYLE__ROTATIONAXISY: Short = 707
    const val THREEDSTYLE__ROTATIONAXISZ: Short = 708
    const val THREEDSTYLE__ROTATIONANGLE: Short = 709
    const val THREEDSTYLE__ROTATIONCENTERX: Short = 710
    const val THREEDSTYLE__ROTATIONCENTERY: Short = 711
    const val THREEDSTYLE__ROTATIONCENTERZ: Short = 712
    const val THREEDSTYLE__RENDERMODE: Short = 713
    const val THREEDSTYLE__TOLERANCE: Short = 714
    const val THREEDSTYLE__XVIEWPOINT: Short = 715
    const val THREEDSTYLE__YVIEWPOINT: Short = 716
    const val THREEDSTYLE__ZVIEWPOINT: Short = 717
    const val THREEDSTYLE__ORIGINX: Short = 718
    const val THREEDSTYLE__ORIGINY: Short = 719
    const val THREEDSTYLE__SKEWANGLE: Short = 720
    const val THREEDSTYLE__SKEWAMOUNT: Short = 721
    const val THREEDSTYLE__AMBIENTINTENSITY: Short = 722
    const val THREEDSTYLE__KEYX: Short = 723
    const val THREEDSTYLE__KEYY: Short = 724
    const val THREEDSTYLE__KEYZ: Short = 725
    const val THREEDSTYLE__KEYINTENSITY: Short = 726
    const val THREEDSTYLE__FILLX: Short = 727
    const val THREEDSTYLE__FILLY: Short = 728
    const val THREEDSTYLE__FILLZ: Short = 729
    const val THREEDSTYLE__FILLINTENSITY: Short = 730
    const val THREEDSTYLE__CONSTRAINROTATION: Short = 763
    const val THREEDSTYLE__ROTATIONCENTERAUTO: Short = 764
    const val THREEDSTYLE__PARALLEL: Short = 765
    const val THREEDSTYLE__KEYHARSH: Short = 766
    const val THREEDSTYLE__FILLHARSH: Short = 767
    const val SHAPE__MASTER: Short = 769
    const val SHAPE__CONNECTORSTYLE: Short = 771
    const val SHAPE__BLACKANDWHITESETTINGS: Short = 772
    const val SHAPE__WMODEPUREBW: Short = 773
    const val SHAPE__WMODEBW: Short = 774
    const val SHAPE__OLEICON: Short = 826
    const val SHAPE__PREFERRELATIVERESIZE: Short = 827
    const val SHAPE__LOCKSHAPETYPE: Short = 828
    const val SHAPE__DELETEATTACHEDOBJECT: Short = 830
    const val SHAPE__BACKGROUNDSHAPE: Short = 831
    const val CALLOUT__CALLOUTTYPE: Short = 832
    const val CALLOUT__XYCALLOUTGAP: Short = 833
    const val CALLOUT__CALLOUTANGLE: Short = 834
    const val CALLOUT__CALLOUTDROPTYPE: Short = 835
    const val CALLOUT__CALLOUTDROPSPECIFIED: Short = 836
    const val CALLOUT__CALLOUTLENGTHSPECIFIED: Short = 837
    const val CALLOUT__ISCALLOUT: Short = 889
    const val CALLOUT__CALLOUTACCENTBAR: Short = 890
    const val CALLOUT__CALLOUTTEXTBORDER: Short = 891
    const val CALLOUT__CALLOUTMINUSX: Short = 892
    const val CALLOUT__CALLOUTMINUSY: Short = 893
    const val CALLOUT__DROPAUTO: Short = 894
    const val CALLOUT__LENGTHSPECIFIED: Short = 895
    const val GROUPSHAPE__SHAPENAME: Short = 0x0380
    const val GROUPSHAPE__DESCRIPTION: Short = 0x0381
    const val GROUPSHAPE__HYPERLINK: Short = 0x0382
    const val GROUPSHAPE__WRAPPOLYGONVERTICES: Short = 0x0383
    const val GROUPSHAPE__WRAPDISTLEFT: Short = 0x0384
    const val GROUPSHAPE__WRAPDISTTOP: Short = 0x0385
    const val GROUPSHAPE__WRAPDISTRIGHT: Short = 0x0386
    const val GROUPSHAPE__WRAPDISTBOTTOM: Short = 0x0387
    const val GROUPSHAPE__REGROUPID: Short = 0x0388
    const val GROUPSHAPE__UNUSED906: Short = 0x038A
    const val GROUPSHAPE__TOOLTIP: Short = 0x038D
    const val GROUPSHAPE__SCRIPT: Short = 0x038E
    const val GROUPSHAPE__POSH: Short = 0x038F
    const val GROUPSHAPE__POSRELH: Short = 0x0390
    const val GROUPSHAPE__POSV: Short = 0x0391
    const val GROUPSHAPE__POSRELV: Short = 0x0392
    const val GROUPSHAPE__HR_PCT: Short = 0x0393
    const val GROUPSHAPE__HR_ALIGN: Short = 0x0394
    const val GROUPSHAPE__HR_HEIGHT: Short = 0x0395
    const val GROUPSHAPE__HR_WIDTH: Short = 0x0396
    const val GROUPSHAPE__SCRIPTEXT: Short = 0x0397
    const val GROUPSHAPE__SCRIPTLANG: Short = 0x0398
    const val GROUPSHAPE__BORDERTOPCOLOR: Short = 0x039B
    const val GROUPSHAPE__BORDERLEFTCOLOR: Short = 0x039C
    const val GROUPSHAPE__BORDERBOTTOMCOLOR: Short = 0x039D
    const val GROUPSHAPE__BORDERRIGHTCOLOR: Short = 0x039E
    const val GROUPSHAPE__TABLEPROPERTIES: Short = 0x039F
    const val GROUPSHAPE__TABLEROWPROPERTIES: Short = 0x03A0
    const val GROUPSHAPE__WEBBOT: Short = 0x03A5
    const val GROUPSHAPE__METROBLOB: Short = 0x03A9
    const val GROUPSHAPE__ZORDER: Short = 0x03AA
    const val GROUPSHAPE__FLAGS: Short = 0x03BF
    const val GROUPSHAPE__EDITEDWRAP: Short = 953
    const val GROUPSHAPE__BEHINDDOCUMENT: Short = 954
    const val GROUPSHAPE__ONDBLCLICKNOTIFY: Short = 955
    const val GROUPSHAPE__ISBUTTON: Short = 956
    const val GROUPSHAPE__1DADJUSTMENT: Short = 957
    const val GROUPSHAPE__HIDDEN: Short = 958
    const val GROUPSHAPE__PRINT: Short = 959

    private val properties: MutableMap<Short?, EscherPropertyMetaData?> = initProps()

    private fun initProps(): MutableMap<Short?, EscherPropertyMetaData?> {
        val m: MutableMap<Short?, EscherPropertyMetaData?> =
            HashMap<Short?, EscherPropertyMetaData?>()
        addProp(m, TRANSFORM__ROTATION.toInt(), "transform.rotation")
        addProp(m, PROTECTION__LOCKROTATION.toInt(), "protection.lockrotation")
        addProp(m, PROTECTION__LOCKASPECTRATIO.toInt(), "protection.lockaspectratio")
        addProp(m, PROTECTION__LOCKPOSITION.toInt(), "protection.lockposition")
        addProp(m, PROTECTION__LOCKAGAINSTSELECT.toInt(), "protection.lockagainstselect")
        addProp(m, PROTECTION__LOCKCROPPING.toInt(), "protection.lockcropping")
        addProp(m, PROTECTION__LOCKVERTICES.toInt(), "protection.lockvertices")
        addProp(m, PROTECTION__LOCKTEXT.toInt(), "protection.locktext")
        addProp(m, PROTECTION__LOCKADJUSTHANDLES.toInt(), "protection.lockadjusthandles")
        addProp(
            m,
            PROTECTION__LOCKAGAINSTGROUPING.toInt(),
            "protection.lockagainstgrouping",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        addProp(m, TEXT__TEXTID.toInt(), "text.textid")
        addProp(m, TEXT__TEXTLEFT.toInt(), "text.textleft")
        addProp(m, TEXT__TEXTTOP.toInt(), "text.texttop")
        addProp(m, TEXT__TEXTRIGHT.toInt(), "text.textright")
        addProp(m, TEXT__TEXTBOTTOM.toInt(), "text.textbottom")
        addProp(m, TEXT__WRAPTEXT.toInt(), "text.wraptext")
        addProp(m, TEXT__SCALETEXT.toInt(), "text.scaletext")
        addProp(m, TEXT__ANCHORTEXT.toInt(), "text.anchortext")
        addProp(m, TEXT__TEXTFLOW.toInt(), "text.textflow")
        addProp(m, TEXT__FONTROTATION.toInt(), "text.fontrotation")
        addProp(m, TEXT__IDOFNEXTSHAPE.toInt(), "text.idofnextshape")
        addProp(m, TEXT__BIDIR.toInt(), "text.bidir")
        addProp(m, TEXT__SINGLECLICKSELECTS.toInt(), "text.singleclickselects")
        addProp(m, TEXT__USEHOSTMARGINS.toInt(), "text.usehostmargins")
        addProp(m, TEXT__ROTATETEXTWITHSHAPE.toInt(), "text.rotatetextwithshape")
        addProp(m, TEXT__SIZESHAPETOFITTEXT.toInt(), "text.sizeshapetofittext")
        addProp(
            m,
            TEXT__SIZE_TEXT_TO_FIT_SHAPE.toInt(),
            "text.sizetexttofitshape",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        addProp(m, GEOTEXT__UNICODE.toInt(), "geotext.unicode")
        addProp(m, GEOTEXT__RTFTEXT.toInt(), "geotext.rtftext")
        addProp(m, GEOTEXT__ALIGNMENTONCURVE.toInt(), "geotext.alignmentoncurve")
        addProp(m, GEOTEXT__DEFAULTPOINTSIZE.toInt(), "geotext.defaultpointsize")
        addProp(m, GEOTEXT__TEXTSPACING.toInt(), "geotext.textspacing")
        addProp(m, GEOTEXT__FONTFAMILYNAME.toInt(), "geotext.fontfamilyname")
        addProp(m, GEOTEXT__REVERSEROWORDER.toInt(), "geotext.reverseroworder")
        addProp(m, GEOTEXT__HASTEXTEFFECT.toInt(), "geotext.hastexteffect")
        addProp(m, GEOTEXT__ROTATECHARACTERS.toInt(), "geotext.rotatecharacters")
        addProp(m, GEOTEXT__KERNCHARACTERS.toInt(), "geotext.kerncharacters")
        addProp(m, GEOTEXT__TIGHTORTRACK.toInt(), "geotext.tightortrack")
        addProp(m, GEOTEXT__STRETCHTOFITSHAPE.toInt(), "geotext.stretchtofitshape")
        addProp(m, GEOTEXT__CHARBOUNDINGBOX.toInt(), "geotext.charboundingbox")
        addProp(m, GEOTEXT__SCALETEXTONPATH.toInt(), "geotext.scaletextonpath")
        addProp(m, GEOTEXT__STRETCHCHARHEIGHT.toInt(), "geotext.stretchcharheight")
        addProp(m, GEOTEXT__NOMEASUREALONGPATH.toInt(), "geotext.nomeasurealongpath")
        addProp(m, GEOTEXT__BOLDFONT.toInt(), "geotext.boldfont")
        addProp(m, GEOTEXT__ITALICFONT.toInt(), "geotext.italicfont")
        addProp(m, GEOTEXT__UNDERLINEFONT.toInt(), "geotext.underlinefont")
        addProp(m, GEOTEXT__SHADOWFONT.toInt(), "geotext.shadowfont")
        addProp(m, GEOTEXT__SMALLCAPSFONT.toInt(), "geotext.smallcapsfont")
        addProp(m, GEOTEXT__STRIKETHROUGHFONT.toInt(), "geotext.strikethroughfont")
        addProp(m, BLIP__CROPFROMTOP.toInt(), "blip.cropfromtop")
        addProp(m, BLIP__CROPFROMBOTTOM.toInt(), "blip.cropfrombottom")
        addProp(m, BLIP__CROPFROMLEFT.toInt(), "blip.cropfromleft")
        addProp(m, BLIP__CROPFROMRIGHT.toInt(), "blip.cropfromright")
        addProp(m, BLIP__BLIPTODISPLAY.toInt(), "blip.bliptodisplay")
        addProp(m, BLIP__BLIPFILENAME.toInt(), "blip.blipfilename")
        addProp(m, BLIP__BLIPFLAGS.toInt(), "blip.blipflags")
        addProp(m, BLIP__TRANSPARENTCOLOR.toInt(), "blip.transparentcolor")
        addProp(m, BLIP__CONTRASTSETTING.toInt(), "blip.contrastsetting")
        addProp(m, BLIP__BRIGHTNESSSETTING.toInt(), "blip.brightnesssetting")
        addProp(m, BLIP__GAMMA.toInt(), "blip.gamma")
        addProp(m, BLIP__PICTUREID.toInt(), "blip.pictureid")
        addProp(m, BLIP__DOUBLEMOD.toInt(), "blip.doublemod")
        addProp(m, BLIP__PICTUREFILLMOD.toInt(), "blip.picturefillmod")
        addProp(m, BLIP__PICTURELINE.toInt(), "blip.pictureline")
        addProp(m, BLIP__PRINTBLIP.toInt(), "blip.printblip")
        addProp(m, BLIP__PRINTBLIPFILENAME.toInt(), "blip.printblipfilename")
        addProp(m, BLIP__PRINTFLAGS.toInt(), "blip.printflags")
        addProp(m, BLIP__NOHITTESTPICTURE.toInt(), "blip.nohittestpicture")
        addProp(m, BLIP__PICTUREGRAY.toInt(), "blip.picturegray")
        addProp(m, BLIP__PICTUREBILEVEL.toInt(), "blip.picturebilevel")
        addProp(m, BLIP__PICTUREACTIVE.toInt(), "blip.pictureactive")
        addProp(m, GEOMETRY__LEFT.toInt(), "geometry.left")
        addProp(m, GEOMETRY__TOP.toInt(), "geometry.top")
        addProp(m, GEOMETRY__RIGHT.toInt(), "geometry.right")
        addProp(m, GEOMETRY__BOTTOM.toInt(), "geometry.bottom")
        addProp(
            m,
            GEOMETRY__SHAPEPATH.toInt(),
            "geometry.shapepath",
            EscherPropertyMetaData.Companion.TYPE_SHAPEPATH
        )
        addProp(
            m,
            GEOMETRY__VERTICES.toInt(),
            "geometry.vertices",
            EscherPropertyMetaData.Companion.TYPE_ARRAY
        )
        addProp(
            m,
            GEOMETRY__SEGMENTINFO.toInt(),
            "geometry.segmentinfo",
            EscherPropertyMetaData.Companion.TYPE_ARRAY
        )
        addProp(m, GEOMETRY__ADJUSTVALUE.toInt(), "geometry.adjustvalue")
        addProp(m, GEOMETRY__ADJUST2VALUE.toInt(), "geometry.adjust2value")
        addProp(m, GEOMETRY__ADJUST3VALUE.toInt(), "geometry.adjust3value")
        addProp(m, GEOMETRY__ADJUST4VALUE.toInt(), "geometry.adjust4value")
        addProp(m, GEOMETRY__ADJUST5VALUE.toInt(), "geometry.adjust5value")
        addProp(m, GEOMETRY__ADJUST6VALUE.toInt(), "geometry.adjust6value")
        addProp(m, GEOMETRY__ADJUST7VALUE.toInt(), "geometry.adjust7value")
        addProp(m, GEOMETRY__ADJUST8VALUE.toInt(), "geometry.adjust8value")
        addProp(m, GEOMETRY__ADJUST9VALUE.toInt(), "geometry.adjust9value")
        addProp(m, GEOMETRY__ADJUST10VALUE.toInt(), "geometry.adjust10value")
        addProp(m, GEOMETRY__SHADOWok.toInt(), "geometry.shadowOK")
        addProp(m, GEOMETRY__3DOK.toInt(), "geometry.3dok")
        addProp(m, GEOMETRY__LINEOK.toInt(), "geometry.lineok")
        addProp(m, GEOMETRY__GEOTEXTOK.toInt(), "geometry.geotextok")
        addProp(m, GEOMETRY__FILLSHADESHAPEOK.toInt(), "geometry.fillshadeshapeok")
        addProp(
            m,
            GEOMETRY__FILLOK.toInt(),
            "geometry.fillok",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        addProp(m, FILL__FILLTYPE.toInt(), "fill.filltype")
        addProp(
            m,
            FILL__FILLCOLOR.toInt(),
            "fill.fillcolor",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, FILL__FILLOPACITY.toInt(), "fill.fillopacity")
        addProp(
            m,
            FILL__FILLBACKCOLOR.toInt(),
            "fill.fillbackcolor",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, FILL__BACKOPACITY.toInt(), "fill.backopacity")
        addProp(m, FILL__CRMOD.toInt(), "fill.crmod")
        addProp(m, FILL__PATTERNTEXTURE.toInt(), "fill.patterntexture")
        addProp(m, FILL__BLIPFILENAME.toInt(), "fill.blipfilename")
        addProp(m, FILL__BLIPFLAGS.toInt(), "fill.blipflags")
        addProp(m, FILL__WIDTH.toInt(), "fill.width")
        addProp(m, FILL__HEIGHT.toInt(), "fill.height")
        addProp(m, FILL__ANGLE.toInt(), "fill.angle")
        addProp(m, FILL__FOCUS.toInt(), "fill.focus")
        addProp(m, FILL__TOLEFT.toInt(), "fill.toleft")
        addProp(m, FILL__TOTOP.toInt(), "fill.totop")
        addProp(m, FILL__TORIGHT.toInt(), "fill.toright")
        addProp(m, FILL__TOBOTTOM.toInt(), "fill.tobottom")
        addProp(m, FILL__RECTLEFT.toInt(), "fill.rectleft")
        addProp(m, FILL__RECTTOP.toInt(), "fill.recttop")
        addProp(m, FILL__RECTRIGHT.toInt(), "fill.rectright")
        addProp(m, FILL__RECTBOTTOM.toInt(), "fill.rectbottom")
        addProp(m, FILL__DZTYPE.toInt(), "fill.dztype")
        addProp(m, FILL__SHADEPRESET.toInt(), "fill.shadepreset")
        addProp(
            m,
            FILL__SHADECOLORS.toInt(),
            "fill.shadecolors",
            EscherPropertyMetaData.Companion.TYPE_ARRAY
        )
        addProp(m, FILL__ORIGINX.toInt(), "fill.originx")
        addProp(m, FILL__ORIGINY.toInt(), "fill.originy")
        addProp(m, FILL__SHAPEORIGINX.toInt(), "fill.shapeoriginx")
        addProp(m, FILL__SHAPEORIGINY.toInt(), "fill.shapeoriginy")
        addProp(m, FILL__SHADETYPE.toInt(), "fill.shadetype")
        addProp(m, FILL__FILLED.toInt(), "fill.filled")
        addProp(m, FILL__HITTESTFILL.toInt(), "fill.hittestfill")
        addProp(m, FILL__SHAPE.toInt(), "fill.shape")
        addProp(m, FILL__USERECT.toInt(), "fill.userect")
        addProp(
            m,
            FILL__NOFILLHITTEST.toInt(),
            "fill.nofillhittest",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        addProp(
            m,
            LINESTYLE__COLOR.toInt(),
            "linestyle.color",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, LINESTYLE__OPACITY.toInt(), "linestyle.opacity")
        addProp(
            m,
            LINESTYLE__BACKCOLOR.toInt(),
            "linestyle.backcolor",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, LINESTYLE__CRMOD.toInt(), "linestyle.crmod")
        addProp(m, LINESTYLE__LINETYPE.toInt(), "linestyle.linetype")
        addProp(m, LINESTYLE__FILLBLIP.toInt(), "linestyle.fillblip")
        addProp(m, LINESTYLE__FILLBLIPNAME.toInt(), "linestyle.fillblipname")
        addProp(m, LINESTYLE__FILLBLIPFLAGS.toInt(), "linestyle.fillblipflags")
        addProp(m, LINESTYLE__FILLWIDTH.toInt(), "linestyle.fillwidth")
        addProp(m, LINESTYLE__FILLHEIGHT.toInt(), "linestyle.fillheight")
        addProp(m, LINESTYLE__FILLDZTYPE.toInt(), "linestyle.filldztype")
        addProp(m, LINESTYLE__LINEWIDTH.toInt(), "linestyle.linewidth")
        addProp(m, LINESTYLE__LINEMITERLIMIT.toInt(), "linestyle.linemiterlimit")
        addProp(m, LINESTYLE__LINESTYLE.toInt(), "linestyle.linestyle")
        addProp(m, LINESTYLE__LINEDASHING.toInt(), "linestyle.linedashing")
        addProp(
            m,
            LINESTYLE__LINEDASHSTYLE.toInt(),
            "linestyle.linedashstyle",
            EscherPropertyMetaData.Companion.TYPE_ARRAY
        )
        addProp(m, LINESTYLE__LINESTARTARROWHEAD.toInt(), "linestyle.linestartarrowhead")
        addProp(m, LINESTYLE__LINEENDARROWHEAD.toInt(), "linestyle.lineendarrowhead")
        addProp(m, LINESTYLE__LINESTARTARROWWIDTH.toInt(), "linestyle.linestartarrowwidth")
        addProp(m, LINESTYLE__LINEESTARTARROWLENGTH.toInt(), "linestyle.lineestartarrowlength")
        addProp(m, LINESTYLE__LINEENDARROWWIDTH.toInt(), "linestyle.lineendarrowwidth")
        addProp(m, LINESTYLE__LINEENDARROWLENGTH.toInt(), "linestyle.lineendarrowlength")
        addProp(m, LINESTYLE__LINEJOINSTYLE.toInt(), "linestyle.linejoinstyle")
        addProp(m, LINESTYLE__LINEENDCAPSTYLE.toInt(), "linestyle.lineendcapstyle")
        addProp(m, LINESTYLE__ARROWHEADSOK.toInt(), "linestyle.arrowheadsok")
        addProp(m, LINESTYLE__ANYLINE.toInt(), "linestyle.anyline")
        addProp(m, LINESTYLE__HITLINETEST.toInt(), "linestyle.hitlinetest")
        addProp(m, LINESTYLE__LINEFILLSHAPE.toInt(), "linestyle.linefillshape")
        addProp(
            m,
            LINESTYLE__NOLINEDRAWDASH.toInt(),
            "linestyle.nolinedrawdash",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        addProp(m, SHADOWSTYLE__TYPE.toInt(), "shadowstyle.type")
        addProp(
            m,
            SHADOWSTYLE__COLOR.toInt(),
            "shadowstyle.color",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, SHADOWSTYLE__HIGHLIGHT.toInt(), "shadowstyle.highlight")
        addProp(m, SHADOWSTYLE__CRMOD.toInt(), "shadowstyle.crmod")
        addProp(m, SHADOWSTYLE__OPACITY.toInt(), "shadowstyle.opacity")
        addProp(m, SHADOWSTYLE__OFFSETX.toInt(), "shadowstyle.offsetx")
        addProp(m, SHADOWSTYLE__OFFSETY.toInt(), "shadowstyle.offsety")
        addProp(m, SHADOWSTYLE__SECONDOFFSETX.toInt(), "shadowstyle.secondoffsetx")
        addProp(m, SHADOWSTYLE__SECONDOFFSETY.toInt(), "shadowstyle.secondoffsety")
        addProp(m, SHADOWSTYLE__SCALEXTOX.toInt(), "shadowstyle.scalextox")
        addProp(m, SHADOWSTYLE__SCALEYTOX.toInt(), "shadowstyle.scaleytox")
        addProp(m, SHADOWSTYLE__SCALEXTOY.toInt(), "shadowstyle.scalextoy")
        addProp(m, SHADOWSTYLE__SCALEYTOY.toInt(), "shadowstyle.scaleytoy")
        addProp(m, SHADOWSTYLE__PERSPECTIVEX.toInt(), "shadowstyle.perspectivex")
        addProp(m, SHADOWSTYLE__PERSPECTIVEY.toInt(), "shadowstyle.perspectivey")
        addProp(m, SHADOWSTYLE__WEIGHT.toInt(), "shadowstyle.weight")
        addProp(m, SHADOWSTYLE__ORIGINX.toInt(), "shadowstyle.originx")
        addProp(m, SHADOWSTYLE__ORIGINY.toInt(), "shadowstyle.originy")
        addProp(m, SHADOWSTYLE__SHADOW.toInt(), "shadowstyle.shadow")
        addProp(m, SHADOWSTYLE__SHADOWOBSURED.toInt(), "shadowstyle.shadowobsured")
        addProp(m, PERSPECTIVE__TYPE.toInt(), "perspective.type")
        addProp(m, PERSPECTIVE__OFFSETX.toInt(), "perspective.offsetx")
        addProp(m, PERSPECTIVE__OFFSETY.toInt(), "perspective.offsety")
        addProp(m, PERSPECTIVE__SCALEXTOX.toInt(), "perspective.scalextox")
        addProp(m, PERSPECTIVE__SCALEYTOX.toInt(), "perspective.scaleytox")
        addProp(m, PERSPECTIVE__SCALEXTOY.toInt(), "perspective.scalextoy")
        addProp(m, PERSPECTIVE__SCALEYTOY.toInt(), "perspective.scaleytoy")
        addProp(m, PERSPECTIVE__PERSPECTIVEX.toInt(), "perspective.perspectivex")
        addProp(m, PERSPECTIVE__PERSPECTIVEY.toInt(), "perspective.perspectivey")
        addProp(m, PERSPECTIVE__WEIGHT.toInt(), "perspective.weight")
        addProp(m, PERSPECTIVE__ORIGINX.toInt(), "perspective.originx")
        addProp(m, PERSPECTIVE__ORIGINY.toInt(), "perspective.originy")
        addProp(m, PERSPECTIVE__PERSPECTIVEON.toInt(), "perspective.perspectiveon")
        addProp(m, THREED__SPECULARAMOUNT.toInt(), "3d.specularamount")
        addProp(m, THREED__DIFFUSEAMOUNT.toInt(), "3d.diffuseamount")
        addProp(m, THREED__SHININESS.toInt(), "3d.shininess")
        addProp(m, THREED__EDGETHICKNESS.toInt(), "3d.edgethickness")
        addProp(m, THREED__EXTRUDEFORWARD.toInt(), "3d.extrudeforward")
        addProp(m, THREED__EXTRUDEBACKWARD.toInt(), "3d.extrudebackward")
        addProp(m, THREED__EXTRUDEPLANE.toInt(), "3d.extrudeplane")
        addProp(
            m,
            THREED__EXTRUSIONCOLOR.toInt(),
            "3d.extrusioncolor",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, THREED__CRMOD.toInt(), "3d.crmod")
        addProp(m, THREED__3DEFFECT.toInt(), "3d.3deffect")
        addProp(m, THREED__METALLIC.toInt(), "3d.metallic")
        addProp(
            m,
            THREED__USEEXTRUSIONCOLOR.toInt(),
            "3d.useextrusioncolor",
            EscherPropertyMetaData.Companion.TYPE_RGB
        )
        addProp(m, THREED__LIGHTFACE.toInt(), "3d.lightface")
        addProp(m, THREEDSTYLE__YROTATIONANGLE.toInt(), "3dstyle.yrotationangle")
        addProp(m, THREEDSTYLE__XROTATIONANGLE.toInt(), "3dstyle.xrotationangle")
        addProp(m, THREEDSTYLE__ROTATIONAXISX.toInt(), "3dstyle.rotationaxisx")
        addProp(m, THREEDSTYLE__ROTATIONAXISY.toInt(), "3dstyle.rotationaxisy")
        addProp(m, THREEDSTYLE__ROTATIONAXISZ.toInt(), "3dstyle.rotationaxisz")
        addProp(m, THREEDSTYLE__ROTATIONANGLE.toInt(), "3dstyle.rotationangle")
        addProp(m, THREEDSTYLE__ROTATIONCENTERX.toInt(), "3dstyle.rotationcenterx")
        addProp(m, THREEDSTYLE__ROTATIONCENTERY.toInt(), "3dstyle.rotationcentery")
        addProp(m, THREEDSTYLE__ROTATIONCENTERZ.toInt(), "3dstyle.rotationcenterz")
        addProp(m, THREEDSTYLE__RENDERMODE.toInt(), "3dstyle.rendermode")
        addProp(m, THREEDSTYLE__TOLERANCE.toInt(), "3dstyle.tolerance")
        addProp(m, THREEDSTYLE__XVIEWPOINT.toInt(), "3dstyle.xviewpoint")
        addProp(m, THREEDSTYLE__YVIEWPOINT.toInt(), "3dstyle.yviewpoint")
        addProp(m, THREEDSTYLE__ZVIEWPOINT.toInt(), "3dstyle.zviewpoint")
        addProp(m, THREEDSTYLE__ORIGINX.toInt(), "3dstyle.originx")
        addProp(m, THREEDSTYLE__ORIGINY.toInt(), "3dstyle.originy")
        addProp(m, THREEDSTYLE__SKEWANGLE.toInt(), "3dstyle.skewangle")
        addProp(m, THREEDSTYLE__SKEWAMOUNT.toInt(), "3dstyle.skewamount")
        addProp(m, THREEDSTYLE__AMBIENTINTENSITY.toInt(), "3dstyle.ambientintensity")
        addProp(m, THREEDSTYLE__KEYX.toInt(), "3dstyle.keyx")
        addProp(m, THREEDSTYLE__KEYY.toInt(), "3dstyle.keyy")
        addProp(m, THREEDSTYLE__KEYZ.toInt(), "3dstyle.keyz")
        addProp(m, THREEDSTYLE__KEYINTENSITY.toInt(), "3dstyle.keyintensity")
        addProp(m, THREEDSTYLE__FILLX.toInt(), "3dstyle.fillx")
        addProp(m, THREEDSTYLE__FILLY.toInt(), "3dstyle.filly")
        addProp(m, THREEDSTYLE__FILLZ.toInt(), "3dstyle.fillz")
        addProp(m, THREEDSTYLE__FILLINTENSITY.toInt(), "3dstyle.fillintensity")
        addProp(m, THREEDSTYLE__CONSTRAINROTATION.toInt(), "3dstyle.constrainrotation")
        addProp(m, THREEDSTYLE__ROTATIONCENTERAUTO.toInt(), "3dstyle.rotationcenterauto")
        addProp(m, THREEDSTYLE__PARALLEL.toInt(), "3dstyle.parallel")
        addProp(m, THREEDSTYLE__KEYHARSH.toInt(), "3dstyle.keyharsh")
        addProp(m, THREEDSTYLE__FILLHARSH.toInt(), "3dstyle.fillharsh")
        addProp(m, SHAPE__MASTER.toInt(), "shape.master")
        addProp(m, SHAPE__CONNECTORSTYLE.toInt(), "shape.connectorstyle")
        addProp(m, SHAPE__BLACKANDWHITESETTINGS.toInt(), "shape.blackandwhitesettings")
        addProp(m, SHAPE__WMODEPUREBW.toInt(), "shape.wmodepurebw")
        addProp(m, SHAPE__WMODEBW.toInt(), "shape.wmodebw")
        addProp(m, SHAPE__OLEICON.toInt(), "shape.oleicon")
        addProp(m, SHAPE__PREFERRELATIVERESIZE.toInt(), "shape.preferrelativeresize")
        addProp(m, SHAPE__LOCKSHAPETYPE.toInt(), "shape.lockshapetype")
        addProp(m, SHAPE__DELETEATTACHEDOBJECT.toInt(), "shape.deleteattachedobject")
        addProp(m, SHAPE__BACKGROUNDSHAPE.toInt(), "shape.backgroundshape")
        addProp(m, CALLOUT__CALLOUTTYPE.toInt(), "callout.callouttype")
        addProp(m, CALLOUT__XYCALLOUTGAP.toInt(), "callout.xycalloutgap")
        addProp(m, CALLOUT__CALLOUTANGLE.toInt(), "callout.calloutangle")
        addProp(m, CALLOUT__CALLOUTDROPTYPE.toInt(), "callout.calloutdroptype")
        addProp(m, CALLOUT__CALLOUTDROPSPECIFIED.toInt(), "callout.calloutdropspecified")
        addProp(m, CALLOUT__CALLOUTLENGTHSPECIFIED.toInt(), "callout.calloutlengthspecified")
        addProp(m, CALLOUT__ISCALLOUT.toInt(), "callout.iscallout")
        addProp(m, CALLOUT__CALLOUTACCENTBAR.toInt(), "callout.calloutaccentbar")
        addProp(m, CALLOUT__CALLOUTTEXTBORDER.toInt(), "callout.callouttextborder")
        addProp(m, CALLOUT__CALLOUTMINUSX.toInt(), "callout.calloutminusx")
        addProp(m, CALLOUT__CALLOUTMINUSY.toInt(), "callout.calloutminusy")
        addProp(m, CALLOUT__DROPAUTO.toInt(), "callout.dropauto")
        addProp(m, CALLOUT__LENGTHSPECIFIED.toInt(), "callout.lengthspecified")
        addProp(m, GROUPSHAPE__SHAPENAME.toInt(), "groupshape.shapename")
        addProp(m, GROUPSHAPE__DESCRIPTION.toInt(), "groupshape.description")
        addProp(m, GROUPSHAPE__HYPERLINK.toInt(), "groupshape.hyperlink")
        addProp(
            m,
            GROUPSHAPE__WRAPPOLYGONVERTICES.toInt(),
            "groupshape.wrappolygonvertices",
            EscherPropertyMetaData.Companion.TYPE_ARRAY
        )
        addProp(m, GROUPSHAPE__WRAPDISTLEFT.toInt(), "groupshape.wrapdistleft")
        addProp(m, GROUPSHAPE__WRAPDISTTOP.toInt(), "groupshape.wrapdisttop")
        addProp(m, GROUPSHAPE__WRAPDISTRIGHT.toInt(), "groupshape.wrapdistright")
        addProp(m, GROUPSHAPE__WRAPDISTBOTTOM.toInt(), "groupshape.wrapdistbottom")
        addProp(m, GROUPSHAPE__REGROUPID.toInt(), "groupshape.regroupid")
        addProp(m, GROUPSHAPE__UNUSED906.toInt(), "unused906") // 0x038A;
        addProp(m, GROUPSHAPE__TOOLTIP.toInt(), "groupshape.wzTooltip") // 0x038D;
        addProp(m, GROUPSHAPE__SCRIPT.toInt(), "groupshape.wzScript") // 0x038E;
        addProp(m, GROUPSHAPE__POSH.toInt(), "groupshape.posh") // 0x038F;
        addProp(m, GROUPSHAPE__POSRELH.toInt(), "groupshape.posrelh") // 0x0390;
        addProp(m, GROUPSHAPE__POSV.toInt(), "groupshape.posv") // 0x0391;
        addProp(m, GROUPSHAPE__POSRELV.toInt(), "groupshape.posrelv") // 0x0392;
        addProp(m, GROUPSHAPE__HR_PCT.toInt(), "groupshape.pctHR") // 0x0393;
        addProp(m, GROUPSHAPE__HR_ALIGN.toInt(), "groupshape.alignHR") // 0x0394;
        addProp(m, GROUPSHAPE__HR_HEIGHT.toInt(), "groupshape.dxHeightHR") // 0x0395;
        addProp(m, GROUPSHAPE__HR_WIDTH.toInt(), "groupshape.dxWidthHR") // 0x0396;
        addProp(m, GROUPSHAPE__SCRIPTEXT.toInt(), "groupshape.wzScriptExtAttr") // 0x0397;
        addProp(m, GROUPSHAPE__SCRIPTLANG.toInt(), "groupshape.scriptLang") // 0x0398;
        addProp(m, GROUPSHAPE__BORDERTOPCOLOR.toInt(), "groupshape.borderTopColor") // 0x039B;
        addProp(m, GROUPSHAPE__BORDERLEFTCOLOR.toInt(), "groupshape.borderLeftColor") // 0x039C;
        addProp(m, GROUPSHAPE__BORDERBOTTOMCOLOR.toInt(), "groupshape.borderBottomColor") // 0x039D;
        addProp(m, GROUPSHAPE__BORDERRIGHTCOLOR.toInt(), "groupshape.borderRightColor") // 0x039E;
        addProp(m, GROUPSHAPE__TABLEPROPERTIES.toInt(), "groupshape.tableProperties") // 0x039F;
        addProp(
            m,
            GROUPSHAPE__TABLEROWPROPERTIES.toInt(),
            "groupshape.tableRowProperties"
        ) // 0x03A0;
        addProp(m, GROUPSHAPE__WEBBOT.toInt(), "groupshape.wzWebBot") // 0x03A5;
        addProp(m, GROUPSHAPE__METROBLOB.toInt(), "groupshape.metroBlob") // 0x03A9;
        addProp(m, GROUPSHAPE__ZORDER.toInt(), "groupshape.dhgt") // 0x03AA;
        addProp(m, GROUPSHAPE__FLAGS.toInt(), "groupshape.GroupShapeBooleanProperties") // 0x03BF;

        addProp(m, GROUPSHAPE__EDITEDWRAP.toInt(), "groupshape.editedwrap")
        addProp(m, GROUPSHAPE__BEHINDDOCUMENT.toInt(), "groupshape.behinddocument")
        addProp(m, GROUPSHAPE__ONDBLCLICKNOTIFY.toInt(), "groupshape.ondblclicknotify")
        addProp(m, GROUPSHAPE__ISBUTTON.toInt(), "groupshape.isbutton")
        addProp(m, GROUPSHAPE__1DADJUSTMENT.toInt(), "groupshape.1dadjustment")
        addProp(m, GROUPSHAPE__HIDDEN.toInt(), "groupshape.hidden")
        addProp(
            m,
            GROUPSHAPE__PRINT.toInt(),
            "groupshape.print",
            EscherPropertyMetaData.Companion.TYPE_BOOLEAN
        )
        return m
    }

    private fun addProp(m: MutableMap<Short?, EscherPropertyMetaData?>, s: Int, propName: String?) {
        m.put(s.toShort(), EscherPropertyMetaData(propName))
    }

    private fun addProp(
        m: MutableMap<Short?, EscherPropertyMetaData?>,
        s: Int,
        propName: String?,
        type: Byte
    ) {
        m.put(s.toShort(), EscherPropertyMetaData(propName, type))
    }

    fun getPropertyName(propertyId: Short): String? {
        val o = properties.get(propertyId)
        return if (o == null) "unknown" else o.description
    }

    fun getPropertyType(propertyId: Short): Byte {
        val escherPropertyMetaData = properties.get(propertyId)
        return if (escherPropertyMetaData == null) 0 else escherPropertyMetaData.type
    }
}
