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

import com.wxiwei.office.fc.util.HexDump.toHex
import com.wxiwei.office.fc.util.LittleEndian.getShort
import com.wxiwei.office.fc.util.LittleEndian.getUShort
import com.wxiwei.office.fc.util.LittleEndian.putInt
import com.wxiwei.office.fc.util.LittleEndian.putShort

/**
 * Escher array properties are the most wierd construction ever invented
 * with all sorts of special cases.  I'm hopeful I've got them all.
 * 
 * @author Glen Stampoultzis (glens at superlinksoftware.com)
 */
class EscherArrayProperty : EscherComplexProperty {
    /**
     * Normally, the size recorded in the simple data (for the complex
     * data) includes the size of the header.
     * There are a few cases when it doesn't though...
     */
    private var sizeIncludesHeaderSize = true

    /**
     * When reading a property from data stream remeber if the complex part is empty and set this flag.
     */
    private var emptyComplexPart = false

    constructor(id: Short, complexData: ByteArray) : super(id, checkComplexData(complexData)) {
        emptyComplexPart = complexData.size == 0
    }

    constructor(propertyNumber: Short, isBlipId: Boolean, complexData: ByteArray?) : super(
        propertyNumber,
        isBlipId,
        checkComplexData(complexData)
    )

    var numberOfElementsInArray: Int
        get() {
            complexData = checkComplexData(complexData)
            return getUShort(complexData, 0)
        }
        set(numberOfElements) {
            val expectedArraySize: Int =
                numberOfElements * getActualSizeOfElements(this.sizeOfElements) + FIXED_SIZE
            if (expectedArraySize != complexData.size) {
                val newArray = ByteArray(expectedArraySize)
                System.arraycopy(complexData, 0, newArray, 0, complexData.size)
                complexData = newArray
            }
            putShort(
                complexData,
                0,
                numberOfElements.toShort()
            )
        }

    var numberOfElementsInMemory: Int
        get() {
            complexData = checkComplexData(complexData)
            return getUShort(complexData, 2)
        }
        set(numberOfElements) {
            val expectedArraySize: Int =
                numberOfElements * getActualSizeOfElements(this.sizeOfElements) + FIXED_SIZE
            if (expectedArraySize != complexData.size) {
                val newArray = ByteArray(expectedArraySize)
                System.arraycopy(complexData, 0, newArray, 0, expectedArraySize)
                complexData = newArray
            }
            putShort(
                complexData,
                2,
                numberOfElements.toShort()
            )
        }

    val sizeOfElements: Short
        get() {
            complexData = checkComplexData(complexData)
            return getShort(complexData, 4)
        }

    fun setSizeOfElements(sizeOfElements: Int) {
        putShort(complexData, 4, sizeOfElements.toShort())

        val expectedArraySize: Int = this.numberOfElementsInArray * getActualSizeOfElements(
            this.sizeOfElements
        ) + FIXED_SIZE
        if (expectedArraySize != complexData.size) {
            // Keep just the first 6 bytes.  The rest is no good to us anyway.
            val newArray = ByteArray(expectedArraySize)
            System.arraycopy(complexData, 0, newArray, 0, 6)
            complexData = newArray
        }
    }

    fun getElement(index: Int): ByteArray {
        val actualSize: Int = getActualSizeOfElements(this.sizeOfElements)
        val result = ByteArray(actualSize)
        val srcPos: Int = FIXED_SIZE + index * actualSize
        if (srcPos + result.size <= complexData.size) {
            System.arraycopy(complexData, srcPos, result, 0, result.size)
        }
        return result
    }

    fun setElement(index: Int, element: ByteArray) {
        val actualSize: Int = getActualSizeOfElements(this.sizeOfElements)
        System.arraycopy(element, 0, complexData, FIXED_SIZE + index * actualSize, actualSize)
    }

    override fun toString(): String {
        val results = StringBuffer()
        results.append("    {EscherArrayProperty:" + '\n')
        results.append("     Num Elements: " + this.numberOfElementsInArray + '\n')
        results.append("     Num Elements In Memory: " + this.numberOfElementsInMemory + '\n')
        results.append("     Size of elements: " + this.sizeOfElements + '\n')
        for (i in 0..<this.numberOfElementsInArray) {
            results.append("     Element " + i + ": " + toHex(getElement(i)) + '\n')
        }
        results.append("}" + '\n')

        return ("propNum: " + propertyNumber
                + ", propName: " + EscherProperties.getPropertyName(propertyNumber)
                + ", complex: " + isComplex
                + ", blipId: " + isBlipId
                + ", data: " + '\n' + results.toString())
    }

    /**
     * We have this method because the way in which arrays in escher works
     * is screwed for seemly arbitary reasons.  While most properties are
     * fairly consistent and have a predictable array size, escher arrays
     * have special cases.
     * 
     * @param data      The data array containing the escher array information
     * @param offset    The offset into the array to start reading from.
     * @return  the number of bytes used by this complex property.
     */
    fun setArrayData(data: ByteArray, offset: Int): Int {
        if (emptyComplexPart) {
            complexData = ByteArray(0)
        } else {
            val numElements = getShort(data, offset)
            getShort(data, offset + 2) // numReserved
            val sizeOfElements = getShort(data, offset + 4)

            val arraySize: Int = getActualSizeOfElements(sizeOfElements) * numElements
            if (arraySize == complexData.size) {
                // The stored data size in the simple block excludes the header size
                complexData = ByteArray(arraySize + 6)
                sizeIncludesHeaderSize = false
            }
            System.arraycopy(data, offset, complexData, 0, complexData.size)
        }
        return complexData.size
    }

    /**
     * Serializes the simple part of this property.  ie the first 6 bytes.
     * 
     * Needs special code to handle the case when the size doesn't
     * include the size of the header block
     */
    override fun serializeSimplePart(data: ByteArray?, pos: Int): Int {
        val data = data!!
        putShort(data, pos, id)
        var recordSize = complexData.size
        if (!sizeIncludesHeaderSize) {
            recordSize -= 6
        }
        putInt(data, pos + 2, recordSize)
        return 6
    }

    companion object {
        /**
         * The size of the header that goes at the
         * start of the array, before the data
         */
        private val FIXED_SIZE = 3 * 2
        private fun checkComplexData(complexData: ByteArray?): ByteArray {
            if (complexData == null || complexData.size == 0) {
                return ByteArray(6)
            }

            return complexData
        }

        /**
         * Sometimes the element size is stored as a negative number.  We
         * negate it and shift it to get the real value.
         */
        fun getActualSizeOfElements(sizeOfElements: Short): Int {
            if (sizeOfElements < 0) {
                return ((-sizeOfElements) shr 2).toShort().toInt()
            }
            return sizeOfElements.toInt()
        }
    }
}
