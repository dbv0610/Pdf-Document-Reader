/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:          TimeVariant.java
 *  
 * 编译器:            android2.2
 * 时间:              下午3:40:39
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian
import com.wxiwei.office.fc.util.LittleEndian.getFloat
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.StringUtil.getFromUnicodeLE
import java.util.Hashtable

/**
 * TODO: A variable type record that specifies an attribute of a time node
 * and whose type and meaning are specified by the value of the rh.recInstance field
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
 * 日期:            2013-1-6
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
class TimeVariant(source: ByteArray, start: Int, len: Int) : PositionDependentRecordAtom() {
    private var _header: ByteArray?

    /**
     * get type of attributes for a time node.
     * @return
     */
    //the type of attributes for a time node.
    val attributeType: Int

    var value: Any? = null
        private set

    /**
     * We are of type 0xF142
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * Set things up, and find our more interesting children
     */
    init {
        // Grab the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)
        this.attributeType = (LittleEndian.getShort(_header!!, 0).toInt() and 0xFFF0) shr 4

        val t = source[start + 8]
        when (t) {
            TVT_Bool -> value = (source[start + 9].toInt() == 1)
            TVT_Int -> value = getInt(source, start + 9)
            TVT_TVT_Float -> value = getFloat(source, start + 9)
            TVT_String -> {
                val strLen = LittleEndian.getInt(_header!!, 4) - 1
                val textBytes = ByteArray(strLen)
                System.arraycopy(source, start + 9, textBytes, 0, strLen)
                value = getFromUnicodeLE(textBytes)
            }
        }
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
        //Display type in UI.
        const val TPID_Display: Byte = 2

        //Relationship to the master time node
        const val TPID_MasterPos: Byte = 5

        //Type of the subordinate time node.
        const val TPID_SlaveType: Byte = 6

        //Identifier of an animation effect.
        const val TPID__EffectID: Byte = 9

        //Direction of an animation effect.
        const val TPID_EffectDir: Byte = 10

        //Type of an animation effect.
        const val TPID_EffectType: Byte = 0x0B

        //Whether the time node is an after effect.
        const val TPID_AfterEffect: Byte = 0x0D

        //The number of slides that a media will play across.
        const val TPID_SlideCount: Byte = 0x0F

        //Time filtering for the time node.
        const val TPID__TimeFilter: Byte = 0x10

        //Event filtering for the time node..
        const val TPID__EventFilter: Byte = 0x11

        //Whether to display the media when it is stopped..
        const val TPID_HideWhenStopped: Byte = 0x12

        //Build identifier.
        const val TPID__GroupID: Byte = 0x13

        //The role of the time node in the timing structure.
        const val TPID__EffectNodeType: Byte = 0x14

        //Whether the time node is a placeholder
        const val TPID_PlaceholderNode: Byte = 0x15

        //The volume of a media.
        const val TPID__MediaVolume: Byte = 0x16

        //Whether a media object is mute.
        const val TPID_MediaMute: Byte = 0x17

        //Whether to zoom a media object to full screen.
        const val TPID__ZoomToFullScreen: Byte = 0x1A

        /**////////////////////////////////////////////////////////////TimeEffectType */
        const val TimeEffectType__Entrance: Byte = 1
        const val TimeEffectType__Exit: Byte = 2
        const val TimeEffectType__Emphasis: Byte = 3
        const val TimeEffectType__MotionPath: Byte = 4
        const val TimeEffectType__ActionVerb: Byte = 5
        const val TimeEffectType__MediaCommand: Byte = 6


        private const val TVT_Bool: Byte = 0
        private const val TVT_Int: Byte = 1
        private const val TVT_TVT_Float: Byte = 2
        private const val TVT_String: Byte = 3

        private const val _type: Long = 0xF142
    }
}
