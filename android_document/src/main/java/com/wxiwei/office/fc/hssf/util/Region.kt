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
package com.wxiwei.office.fc.hssf.util

import com.wxiwei.office.fc.ss.util.Region

/**
 * Represents a from/to row/col square.  This is a object primitive
 * that can be used to represent row,col - row,col just as one would use String
 * to represent a string of characters.  Its really only useful for HSSF though.
 * 
 * @author  Andrew C. Oliver acoliver at apache dot org
 */
class Region : Region {
    /**
     * Creates a new instance of Region (0,0 - 0,0)
     */
    constructor() : super()

    constructor(rowFrom: Int, colFrom: Short, rowTo: Int, colTo: Short) : super(
        rowFrom,
        colFrom,
        rowTo,
        colTo
    )

    constructor(ref: String) : super(ref)
}
