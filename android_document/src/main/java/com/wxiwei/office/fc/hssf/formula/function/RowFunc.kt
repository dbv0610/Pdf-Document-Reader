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
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation for the Excel function ROW
 * 
 * @author Josh Micich
 */
class RowFunc : Function0Arg, Function1Arg {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int): ValueEval {
        return NumberEval((srcRowIndex + 1).toDouble())
    }

    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval {
        val rnum: Int

        if (arg0 is AreaEval) {
            rnum = arg0.firstRow
        } else if (arg0 is RefEval) {
            rnum = arg0.row
        } else {
            // anything else is not valid argument
            return ErrorEval.VALUE_INVALID
        }

        return NumberEval((rnum + 1).toDouble())
    }

    override fun evaluate(
        args: Array<ValueEval?>,
        srcRowIndex: Int,
        srcColumnIndex: Int
    ): ValueEval? {
        when (args.size) {
            1 -> return evaluate(srcRowIndex, srcColumnIndex, args[0])
            0 -> return NumberEval((srcRowIndex + 1).toDouble())
        }
        return ErrorEval.VALUE_INVALID
    }
}
