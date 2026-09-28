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
package com.wxiwei.office.fc.hssf.formula.udf

import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import java.util.Arrays


/**
 * Collects add-in libraries and VB macro functions together into one UDF finder
 * 
 * @author PUdalau
 */
class AggregatingUDFFinder(vararg usedToolPacks: UDFFinder) : UDFFinder {
    private val _usedToolPacks: MutableCollection<UDFFinder>

    init {
        _usedToolPacks = ArrayList<UDFFinder>(usedToolPacks.size)
        _usedToolPacks.addAll(usedToolPacks)
    }

    /**
     * Returns executor by specified name. Returns `null` if
     * function isn't contained by any registered tool pack.
     * 
     * @param name Name of function.
     * @return Function executor. `null` if not found
     */
    override fun findFunction(name: String?): FreeRefFunction? {
        var evaluatorForFunction: FreeRefFunction?
        for (pack in _usedToolPacks) {
            evaluatorForFunction = pack.findFunction(name)
            if (evaluatorForFunction != null) {
                return evaluatorForFunction
            }
        }
        return null
    }

    /**
     * Add a new toolpack
     * 
     * @param toolPack the UDF toolpack to add
     */
    fun add(toolPack: UDFFinder?) {
        _usedToolPacks.add(toolPack!!)
    }
}
