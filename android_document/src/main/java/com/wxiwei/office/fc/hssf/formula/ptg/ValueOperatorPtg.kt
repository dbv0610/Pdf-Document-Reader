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
package com.wxiwei.office.fc.hssf.formula.ptg

import com.wxiwei.office.fc.util.LittleEndianOutput

/**
 * Common superclass of all value operators. Subclasses include all unary and
 * binary operators except for the reference operators (IntersectionPtg,
 * RangePtg, UnionPtg)
 * 
 * @author Josh Micich
 */
abstract class ValueOperatorPtg : OperationPtg() {
    /**
     * All Operator <tt>Ptg</tt>s are base tokens (i.e. are not RVA classified)
     */
    override val isBaseToken: Boolean get() {
        return true
    }

    override val defaultOperandClass: Byte get() {
        return Ptg.Companion.CLASS_VALUE
    }

    override fun write(out: LittleEndianOutput) {
        out.writeByte(this.sid.toInt())
    }

    protected abstract val sid: Byte

    override val size: Int get() {
        return 1
    }

    override fun toFormulaString(): String? {
        // TODO - prune this method out of the hierarchy
        throw RuntimeException("toFormulaString(String[] operands) should be used for subclasses of OperationPtgs")
    }
}
