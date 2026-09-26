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

import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation of Excel T() function
 * 
 * 
 * If the argument is a text or error value it is returned unmodified.  All other argument types
 * cause an empty string result.  If the argument is an area, the first (top-left) cell is used
 * (regardless of the coordinates of the evaluating formula cell).
 */
class T : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval {
        var arg = arg0
        if (arg is RefEval) {
            arg = arg.innerValueEval
        } else if (arg is AreaEval) {
            // when the arg is an area, choose the top left cell
            arg = arg.getRelativeValue(0, 0)
        }

        if (arg is StringEval) {
            // Text values are returned unmodified
            return arg
        }

        if (arg is ErrorEval) {
            // Error values also returned unmodified
            return arg
        }
        // for all other argument types the result is empty string
        return StringEval.EMPTY_INSTANCE
    }
}
