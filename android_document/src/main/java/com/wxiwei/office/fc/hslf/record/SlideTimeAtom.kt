/*
 * 文件名称:          SlideTimeAtom.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:54:11
 */
package com.wxiwei.office.fc.hslf.record

import com.wxiwei.office.fc.util.LittleEndian.getLong
import java.util.Hashtable

/**
 * TODO: An atom record that specifies the slide creation time stamp
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
class SlideTimeAtom protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordAtom() {
    private var _header: ByteArray?
    /**
     * 
     * @return
     */
    /**
     * the time of slide creation.
     */
    val slideCreateTime: Long

    /**
     * For the UserEdit Atom
     */
    init {
        // Sanity Checking
        var len = len
        if (len < 16) {
            len = 16
        }


        // Get the header
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        this.slideCreateTime = getLong(source, start + 8)
    }

    /**
     * We are of type 12011
     */
    public override fun getRecordType(): Long {
        return _type
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
        private const val _type: Long = 12011
    }
}
