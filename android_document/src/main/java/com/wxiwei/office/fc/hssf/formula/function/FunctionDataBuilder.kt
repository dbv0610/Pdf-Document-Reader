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
package com.wxiwei.office.fc.hssf.formula.function

/**
 * Temporarily collects <tt>FunctionMetadata</tt> instances for creation of a
 * <tt>FunctionMetadataRegistry</tt>.
 * 
 * @author Josh Micich
 */
internal class FunctionDataBuilder(sizeEstimate: Int) {
    private var _maxFunctionIndex: Int
    private val _functionDataByName: MutableMap<String?, FunctionMetadata?>
    private val _functionDataByIndex: MutableMap<Int, FunctionMetadata?>

    /** stores indexes of all functions with footnotes (i.e. whose definitions might change)  */
    private val _mutatingFunctionIndexes: MutableSet<Int>

    init {
        _maxFunctionIndex = -1
        _functionDataByName = HashMap<String?, FunctionMetadata?>(sizeEstimate * 3 / 2)
        _functionDataByIndex = HashMap<Int, FunctionMetadata?>(sizeEstimate * 3 / 2)
        _mutatingFunctionIndexes = HashSet<Int>()
    }

    fun add(
        functionIndex: Int, functionName: String?, minParams: Int, maxParams: Int,
        returnClassCode: Byte, parameterClassCodes: ByteArray, hasFootnote: Boolean
    ) {
        val fm = FunctionMetadata(
            functionIndex, functionName, minParams, maxParams,
            returnClassCode, parameterClassCodes
        )

        val indexKey = functionIndex


        if (functionIndex > _maxFunctionIndex) {
            _maxFunctionIndex = functionIndex
        }
        // allow function definitions to change only if both previous and the new items have footnotes
        var prevFM: FunctionMetadata?
        prevFM = _functionDataByName.get(functionName)
        if (prevFM != null) {
            if (!hasFootnote || !_mutatingFunctionIndexes.contains(indexKey)) {
                throw RuntimeException("Multiple entries for function name '" + functionName + "'")
            }
            _functionDataByIndex.remove(prevFM.index)
        }
        prevFM = _functionDataByIndex.get(indexKey)
        if (prevFM != null) {
            if (!hasFootnote || !_mutatingFunctionIndexes.contains(indexKey)) {
                throw RuntimeException("Multiple entries for function index (" + functionIndex + ")")
            }
            _functionDataByName.remove(prevFM.name)
        }
        if (hasFootnote) {
            _mutatingFunctionIndexes.add(indexKey)
        }
        _functionDataByIndex.put(indexKey, fm)
        _functionDataByName.put(functionName, fm)
    }

    fun build(): FunctionMetadataRegistry {
        val jumbledArray: Array<FunctionMetadata?> = _functionDataByName.values.toTypedArray()
        val fdIndexArray = arrayOfNulls<FunctionMetadata>(_maxFunctionIndex + 1)
        for (i in jumbledArray.indices) {
            val fd = jumbledArray[i]!!
            fdIndexArray[fd.index] = fd
        }

        return FunctionMetadataRegistry(fdIndexArray, _functionDataByName)
    }
}
