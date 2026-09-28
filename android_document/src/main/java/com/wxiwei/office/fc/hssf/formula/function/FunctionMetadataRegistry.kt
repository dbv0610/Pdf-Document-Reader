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
package com.wxiwei.office.fc.hssf.formula.function

/**
 * Allows clients to get [FunctionMetadata] instances for any built-in function of Excel.
 * 
 * @author Josh Micich
 */
class FunctionMetadataRegistry /* package */ internal constructor(
    private val _functionDataByIndex: Array<FunctionMetadata?>,
    private val _functionDataByName: MutableMap<String?, FunctionMetadata?>
) {
    val allFunctionNames: MutableSet<String?>
        /* package */
        get() = _functionDataByName.keys


    private fun getFunctionByIndexInternal(index: Int): FunctionMetadata? {
        return _functionDataByIndex[index]
    }

    private fun getFunctionByNameInternal(name: String?): FunctionMetadata? {
        return _functionDataByName.get(name)
    }


    companion object {
        /**
         * The name of the IF function (i.e. "IF").  Extracted as a constant for clarity.
         */
        const val FUNCTION_NAME_IF: String = "IF"

        const val FUNCTION_INDEX_IF: Int = 1
        const val FUNCTION_INDEX_SUM: Short = 4
        const val FUNCTION_INDEX_CHOOSE: Int = 100
        const val FUNCTION_INDEX_INDIRECT: Short = 148
        const val FUNCTION_INDEX_EXTERNAL: Short = 255

        private var _instance: FunctionMetadataRegistry? = null

        private val instance: FunctionMetadataRegistry
            get() {
                if (_instance == null) {
                    _instance =
                        FunctionMetadataReader.createRegistry()
                }
                return _instance!!
            }

        @JvmStatic
        fun getFunctionByIndex(index: Int): FunctionMetadata? {
            return instance.getFunctionByIndexInternal(index)
        }

        /**
         * Resolves a built-in function index.
         * @param name uppercase function name
         * @return a negative value if the function name is not found.
         * This typically occurs for external functions.
         */
        @JvmStatic
        fun lookupIndexByName(name: String?): Short {
            val fd: FunctionMetadata? = instance.getFunctionByNameInternal(name)
            if (fd == null) {
                return -1
            }
            return fd.index.toShort()
        }

        @JvmStatic
        fun getFunctionByName(name: String?): FunctionMetadata? {
            return instance.getFunctionByNameInternal(name)
        }
    }
}
