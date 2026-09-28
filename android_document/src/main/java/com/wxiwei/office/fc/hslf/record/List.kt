/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
/*
 * 文件名称:           List.java
 *  
 * 编译器:             android2.2
 * 时间:               上午10:51:23
 */
package com.wxiwei.office.fc.hslf.record

import java.io.IOException
import java.io.OutputStream

/**
 * TODO: 文件注释
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
 * 日期:           2012-7-30
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
class List protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    /**
     * Look for ExtendedPreRule
     */
    private fun findExtendedPreRuleRecord(toSearch: Array<Record>) {
        for (i in toSearch.indices) {
            if (toSearch[i] is ExtendedPresRuleContainer) {
                this.extendedPresRuleContainer = toSearch[i] as ExtendedPresRuleContainer
            } else {
                // If it has children, walk them
                if (!toSearch[i].isAnAtom()) {
                    val children = toSearch[i].getChildRecords()
                    if (children != null) {
                        findExtendedPreRuleRecord(children)
                    }
                }
            }
        }
    }

    /**
     * 
     * 
     */
    public override fun getRecordType(): Long {
        return RecordTypes.List.typeID.toLong()
    }

    /**
     * 
     * 
     */
    @Throws(IOException::class)
    fun writeOut(o: OutputStream?) {
    }

    /**
     * 
     */
    override fun dispose() {
        _header = null
        if (this.extendedPresRuleContainer != null) {
            extendedPresRuleContainer!!.dispose()
            this.extendedPresRuleContainer = null
        }
    }

    //
    private var _header: ByteArray?

    /**
     * 
     * @return
     */
    //
    var extendedPresRuleContainer: ExtendedPresRuleContainer? = null
        private set

    /**
     * 
     * @param source
     * @param start
     * @param len
     */
    init {
        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)


        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
        findExtendedPreRuleRecord(_children)
    }
}
