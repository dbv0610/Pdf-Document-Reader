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

/**
 * An anchor is what specifics the position of a shape within a client object
 * or within another containing shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
abstract class Anchor {
    //
    @JvmField
    protected var dx1: Int = 0
    //
    @JvmField
    protected var dy1: Int = 0
    //
    @JvmField
    protected var dx2: Int = 0
    //
    @JvmField
    protected var dy2: Int = 0

    constructor()

    constructor(dx1: Int, dy1: Int, dx2: Int, dy2: Int) {
        this.dx1 = dx1
        this.dy1 = dy1
        this.dx2 = dx2
        this.dy2 = dy2
    }

    fun getDx1(): Int = dx1

    fun setDx1(dx1: Int) {
        this.dx1 = dx1
    }

    fun getDy1(): Int = dy1

    fun setDy1(dy1: Int) {
        this.dy1 = dy1
    }

    fun getDy2(): Int = dy2

    fun setDy2(dy2: Int) {
        this.dy2 = dy2
    }

    fun getDx2(): Int = dx2

    fun setDx2(dx2: Int) {
        this.dx2 = dx2
    }

    abstract fun isHorizontallyFlipped(): Boolean

    abstract fun isVerticallyFlipped(): Boolean
}
