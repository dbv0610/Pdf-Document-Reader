/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          VisualShapeAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              上午11:04:36
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getInt
import java.util.Hashtable

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
 * 日期:            2013-1-7
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
class VisualShapeAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?

    /**
     * get the target element type in the shape to which the animation is applied
     * @return
     */
    //the target element type in the shape to which the animation is applied. 
    //It MUST NOT be TVET_Page.
    val targetElementType: Int

    //the target element type of the animation. It MUST be ET_ShapeType
    private val refType: Int

    /**
     * get the target shape on the slide to animate
     * @return
     */
    //the target shape on the slide to animate
    val targetElementID: Int

    /**
     * For VisualShapeGeneralAtom
     * data1:A signed integer that specifies the zero-based character
     * index of the beginning of a text range.
     * It MUST be ignored unless type is TVET_TextRange
     * data2:A signed integer that specifies the zero-based character
     * index of the end of a text range.
     * It MUST be ignored unless type is TVET_TextRange
     */
    /**
     * For VisualShapeChartElementAtom
     * data1 : An unsigned integer that specifies how the chart is built
     * during its animation. It MUST be a value from the following table.
     * Value Meaning
     * 0x00000000 The entire chart.
     * 0x00000001 By series.
     * 0x00000002 By category.
     * 0x00000003 By series element.
     * 0x00000004 By category element.
     * 0x00000005 Custom chart element.
     * data2 (4 bytes): A signed integer that specifies a chart element to animate.
     * It MUST be greater than or equal to 0xFFFFFFFF (-1).
     * The value 0xFFFFFFFF specifies that this record is invalid
     * and SHOULD be ignored. The value 0x00000000 specifies the chart background.
     * Values greater than 0x0000000 specify a one-based index in the list of chart
     * elements specified by data1.
     */
    val data1: Int
    val data2: Int

    /**
     * We are of type 0x2AFB
     */
    public override fun getRecordType(): Long {
        return RECORD_ID
    }

    /**
     * Set things up, and find our more interesting children
     */
    init {
        var len = len
        if (len < 28) {
            len = 28
        }
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        this.targetElementType = getInt(source, start + 8)
        refType = getInt(source, start + 12)
        this.targetElementID = getInt(source, start + 16)

        data1 = getInt(source, start + 20)
        data2 = getInt(source, start + 24)
    }

    /**
     * At write-out time, update the references to PersistPtrs and
     * other UserEditAtoms to point to their new positions
     */
    public override fun updateOtherRecordReferences(oldToNewReferencesLookup: Hashtable<Int?, Int?>?) {
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
    }

    companion object {
        //the part of a slide or shape to which the animation is applied
        const val TVET_Shape: Int = 0 //Applies to the shape and all its text.
        const val TVET_Page: Int = 1 //Applies to the slide.
        const val TVET_TextRange: Int = 2 //Applies to a specified range of text of the shape.
        const val TVET_Audio: Int = 3 //Applies to the audio of the shape.
        const val TVET_Video: Int = 4 //Applies to the video of the shape.
        const val TVET_ChartElement: Int = 5 //Applies to the elements of the chart.
        const val TVET_ShapeOnly: Int = 6 //Applies to the shape but not its text.
        const val TVET_AllTextRange: Int = 8 //Applies to all text of the shape.

        //the element type of an animation target.
        //The animation targets a shape or some part of a shape.
        const val ET_ShapeType: Int = 1

        //The animation targets a sound file that does not correspond to a shape.
        const val ET_SoundType: Int = 2


        var RECORD_ID: Long = 0x2AFB
    }
}
