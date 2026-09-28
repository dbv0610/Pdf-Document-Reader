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

import com.wxiwei.office.fc.hssf.formula.TwoDEval
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.CountUtils.I_MatchPredicate


/**
 * Implementation for the function COUNTBLANK
 * 
 * 
 * Syntax: COUNTBLANK ( range )
 * <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><th>range&nbsp;&nbsp;&nbsp;</th><td>is the range of cells to count blanks</td></tr>
</table> * 
 * 
 * 
 * @author Mads Mohr Christensen
 */
class Countblank : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval {
        val result: Double
        if (arg0 is RefEval) {
            result = CountUtils.countMatchingCell(arg0, predicate).toDouble()
        } else if (arg0 is TwoDEval) {
            result = CountUtils.countMatchingCellsInArea(arg0, predicate).toDouble()
        } else {
            throw IllegalArgumentException("Bad range arg type (" + arg0!!.javaClass.getName() + ")")
        }
        return NumberEval(result)
    }

    companion object {
        private val predicate: I_MatchPredicate = object : I_MatchPredicate {
            override fun matches(valueEval: ValueEval?): Boolean {
                // Note - only BlankEval counts
                return valueEval === BlankEval.instance
            }
        }
    }
}
