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

package com.wxiwei.office.ss.model

class ChildAnchor : Anchor {
    constructor()

    constructor(dx1: Int, dy1: Int, dx2: Int, dy2: Int) : super(dx1, dy1, dx2, dy2)

    fun setAnchor(dx1: Int, dy1: Int, dx2: Int, dy2: Int) {
        this.dx1 = dx1
        this.dy1 = dy1
        this.dx2 = dx2
        this.dy2 = dy2
    }

    override fun isHorizontallyFlipped(): Boolean {
        return dx1 > dx2
    }

    override fun isVerticallyFlipped(): Boolean {
        return dy1 > dy2
    }
}
