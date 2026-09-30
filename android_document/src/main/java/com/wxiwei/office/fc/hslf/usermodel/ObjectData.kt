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
package com.wxiwei.office.fc.hslf.usermodel

import com.wxiwei.office.fc.hslf.record.ExOleObjStg
import java.io.IOException
import java.io.InputStream

/**
 * A class that represents object data embedded in a slide show.
 * 
 * @author Daniel Noll
 */
class ObjectData
    (storage: ExOleObjStg?) {
    /**
     * Return the record that contains the object data.
     * 
     * @return the record that contains the object data.
     */
    /**
     * The record that contains the object data.
     */
    var exOleObjStg: ExOleObjStg?
        private set

    /**
     * Creates the object data wrapping the record that contains the object data.
     * 
     * @param storage the record that contains the object data.
     */
    init {
        this.exOleObjStg = storage
    }

    val data: InputStream?
        /**
         * Gets an input stream which returns the binary of the embedded data.
         * 
         * @return the input stream which will contain the binary of the embedded data.
         */
        get() = exOleObjStg!!.data

    /**
     * Sets the embedded data.
     * 
     * @param data the embedded data.
     */
    @Throws(IOException::class)
    fun setData(data: ByteArray) {
        exOleObjStg!!.setData(data)
    }

    /**
     * 
     */
    fun dispose() {
        if (this.exOleObjStg != null) {
            exOleObjStg!!.dispose()
            this.exOleObjStg = null
        }
    }
}
