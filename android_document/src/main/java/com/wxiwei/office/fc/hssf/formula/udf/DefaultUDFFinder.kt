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
import java.util.Locale

/**
 * Default UDF finder - for adding your own user defined functions.
 * 
 * @author PUdalau
 */
class DefaultUDFFinder(functionNames: Array<String?>, functionImpls: Array<FreeRefFunction?>) :
    UDFFinder {
    private val _functionsByName: MutableMap<String?, FreeRefFunction?>

    init {
        val nFuncs = functionNames.size
        require(functionImpls.size == nFuncs) { "Mismatch in number of function names and implementations" }
        val m = HashMap<String?, FreeRefFunction?>(nFuncs * 3 / 2)
        for (i in functionImpls.indices) {
            m.put(functionNames[i]!!.uppercase(Locale.getDefault()), functionImpls[i])
        }
        _functionsByName = m
    }

    override fun findFunction(name: String?): FreeRefFunction? {
        return _functionsByName.get(name!!.uppercase(Locale.getDefault()))
    }
}
