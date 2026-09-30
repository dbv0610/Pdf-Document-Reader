/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           ExtendedParagraphAtom.java
 *  
 * 编译器:             android2.2
 * 时间:               下午5:33:25
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.hslf.model.textproperties.AutoNumberTextProp
import com.wxiwei.office.fc.hslf.model.textproperties.TextProp
import com.wxiwei.office.fc.util.LittleEndian.getInt
import com.wxiwei.office.fc.util.LittleEndian.getShort
import java.util.LinkedList

/**
 * get extended paragraph property(auto number)
 * 
 * 
 * 
 * 
 * Read版本:       Read V1.0
 * 
 * 
 * 作者:           jhy1790
 * 
 * 
 * 日期:           2012-7-18
 * 
 * 
 * 负责人:         jhy1790
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class ExtendedParagraphAtom : RecordAtom {
    /**
     * mask == 0x03000000是需特殊处理为0x01800000
     * @param source
     * @param start
     * @param len
     */
    constructor(source: ByteArray, start: Int, len: Int) {
        // Sanity Checking
        var len = len
        if (len < 8) {
            len = 8
        }
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Grab the text
        var pos = start + 8
        while (pos < (start + len) && len >= 28) {
            if ((len - pos) < 4) {
                break
            }
            val paraProp = AutoNumberTextProp()
            var mask = getInt(source, pos)
            if (mask == 0x03000000) {
                mask = mask shr 1
            }
            pos += 4

            if (mask != 0) {
                // for BuInstance
                if (mask == 0x01800000) {
                    pos += 2
                } else {
                    pos += 4
                }
                for (i in extendedParagraphPropTypes.indices) {
                    var `val` = 0
                    if ((mask and extendedParagraphPropTypes[i]!!.mask) != 0) {
                        `val` = getShort(source, pos).toInt()
                        if ("NumberingType" == extendedParagraphPropTypes[i]!!.name) {
                            paraProp.numberingType = `val`
                        } else if ("Start" == extendedParagraphPropTypes[i]!!.name) {
                            paraProp.start = `val`
                        }
                        pos += extendedParagraphPropTypes[i]!!.size
                    } else {
                        break
                    }
                }
                if (mask == 0x01800000) {
                    pos += 2
                }
            }
            autoNumberList!!.add(paraProp)
            pos += 8
        }
    }

    /**
     * 
     */
    protected constructor()

    /**
     * 
     * @return
     */
    override fun getExtendedParagraphPropList(): LinkedList<AutoNumberTextProp>? {
        return autoNumberList
    }

    /**
     * We are of type 4012
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     */
    public override fun dispose() {
        _header = null
        val list = autoNumberList
        if (list != null) {
            for (an in list!!) {
                an.dispose()
            }
            list.clear()
            autoNumberList = null
        }
    }

    //
    private var _header: ByteArray? = null

    //
    private var autoNumberList: LinkedList<AutoNumberTextProp>? = LinkedList<AutoNumberTextProp>()

    companion object {
        //
        private const val _type: Long = 4012

        // All the different kinds of extended paragraph properties we might handle
        var extendedParagraphPropTypes: Array<TextProp?> =
            arrayOf<TextProp?>( //new TextProp(2, 0x02000000, "BuInstance"), 
                TextProp(2, 0x01000000, "NumberingType"),
                TextProp(2, 0x00800000, "Start")
            )
    }
}
