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
package com.wxiwei.office.fc.hslf.model

import com.wxiwei.office.common.shape.ShapeTypes
import com.wxiwei.office.fc.hslf.exceptions.HSLFException

/**
 * Contains all known shape types in PowerPoint
 * 
 * @author Yegor Kozlov
 */
object ShapeTypes : ShapeTypes {
    /**
     * Return name of the shape by id
     * @param type  - the id of the shape, one of the static constants defined in this class
     * @return  the name of the shape
     */
    fun typeName(type: Int): String? {
        val name = types.get(type)
        return name
    }

    var types: HashMap<Int, String>

    init {
        types = HashMap<Int, String>()
        try {
            val f = ShapeTypes::class.java.getFields()
            for (i in f.indices) {
                val `val` = f[i]!!.get(null)
                if (`val` is Int) {
                    types.put(`val`, f[i]!!.getName())
                }
            }
        } catch (e: IllegalAccessException) {
            throw HSLFException("Failed to initialize shape types")
        }
    }
}
