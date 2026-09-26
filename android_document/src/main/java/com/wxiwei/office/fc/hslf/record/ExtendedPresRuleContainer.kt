/*
 * 文件名称:           ExtendedPresRuleContainer.java
 *  
 * 编译器:             android2.2
 * 时间:               下午4:10:49
 */
package com.wxiwei.office.fc.hslf.record

import java.io.IOException
import java.io.OutputStream
import java.util.Vector

/**
 * be made up of ExtendedParagraphHeaderAtom and ExtendedParagraphAtom
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
class ExtendedPresRuleContainer protected constructor(source: ByteArray, start: Int, len: Int) :
    PositionDependentRecordContainer() {
    /**
     * 
     * 
     */
    public override fun getRecordType(): Long {
        return _type
    }

    /**
     * 
     * 
     */
    @Throws(IOException::class)
    fun writeOut(o: OutputStream?) {
    }

    /**
     * Inner class to wrap up a matching set of records that hold the
     * text for a given sheet. This includes sets of ExtendedParagraphHeaderAtom
     * and ExtendedParagraphAtom.
     */
    inner class ExtendedParaAtomsSet
        (
        /**
         * 
         * @return
         */
        //
        var extendedParaHeaderAtom: ExtendedParagraphHeaderAtom?,
        /**
         * 
         * @return
         */
        //
        var extendedParaAtom: ExtendedParagraphAtom?
    ) {
        /**
         * 
         */
        fun dispose() {
            if (this.extendedParaHeaderAtom != null) {
                extendedParaHeaderAtom!!.dispose()
                this.extendedParaHeaderAtom = null
            }
            if (this.extendedParaAtom != null) {
                extendedParaAtom!!.dispose()
                this.extendedParaAtom = null
            }
        }

        /**
         * 
         * @param extendedParaHeaderAtom
         * @param extendedParaAtom
         */
        init {
            this.extendedParaAtom = extendedParaAtom
        }
    }

    /**
     * 
     */
    override fun dispose() {
        _header = null
        if (this.extendedParaAtomsSets != null) {
            for (eps in this.extendedParaAtomsSets!!) {
                eps.dispose()
            }
            this.extendedParaAtomsSets = null
        }
    }

    //
    private var _header: ByteArray?

    /**
     * 
     * @return
     */
    //
    var extendedParaAtomsSets: Array<ExtendedParaAtomsSet>?
        private set

    /**
     * Create a new holder for a boring record with children, but with
     * position dependent characteristics
     */
    init {
        // Just grab the header, not the whole contents
        _header = ByteArray(8)
        System.arraycopy(source, start, _header, 0, 8)

        // Find our children
        _children = findChildRecords(source, start + 8, len - 8)
        val sets = Vector<ExtendedParaAtomsSet>()
        for (i in _children.indices) {
            if (_children[i] is ExtendedParagraphAtom) {
                for (j in i - 1 downTo 0) {
                    if (_children[j] is ExtendedParagraphHeaderAtom) {
                        val set = ExtendedParaAtomsSet(
                            _children[j] as ExtendedParagraphHeaderAtom,
                            _children[i] as ExtendedParagraphAtom
                        )
                        sets.add(set)
                        break
                    }
                }
            }
        }
        // Turn the vector into an array
        this.extendedParaAtomsSets = sets.toTypedArray()
    }

    companion object {
        //
        private const val _type: Long = 4014
    }
}
