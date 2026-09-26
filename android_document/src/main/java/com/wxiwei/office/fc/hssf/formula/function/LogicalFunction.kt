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
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.BoolEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.OperandResolver.getSingleValue
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 */
abstract class LogicalFunction : Fixed1ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        var ve: ValueEval?
        try {
            ve = getSingleValue(arg0, srcRowIndex, srcColumnIndex)
        } catch (e: EvaluationException) {
            if (false) {
                // Note - it is more usual to propagate error codes straight to the result like this:
                return e.errorEval
                // but logical functions behave a little differently
            }
            // this will usually cause a 'FALSE' result except for ISNONTEXT()
            ve = e.errorEval
        }
        return BoolEval.valueOf(evaluate(ve))
    }

    /**
     * @param arg any [ValueEval], potentially [BlankEval] or [ErrorEval].
     */
    protected abstract fun evaluate(arg: ValueEval?): Boolean

    companion object {
        val ISLOGICAL: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg is BoolEval
            }
        }
        val ISNONTEXT: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg !is StringEval
            }
        }
        val ISNUMBER: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg is NumberEval
            }
        }
        val ISTEXT: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg is StringEval
            }
        }

        val ISBLANK: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg is BlankEval
            }
        }

        val ISERROR: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg is ErrorEval
            }
        }

        /**
         * Implementation for Excel ISNA() function.
         *
         *
         * 
         * **Syntax**:<br></br>
         * **ISNA**(**value**)
         *
         *
         * 
         * **value**  The value to be tested<br></br>
         * <br></br>
         * Returns <tt>TRUE</tt> if the specified value is '#N/A', <tt>FALSE</tt> otherwise.
         */
        val ISNA: Function = object : LogicalFunction() {
            override fun evaluate(arg: ValueEval?): Boolean {
                return arg === ErrorEval.NA
            }
        }

        val ISREF: Function = object : Fixed1ArgFunction() {
            override fun evaluate(
                srcRowIndex: Int,
                srcColumnIndex: Int,
                arg0: ValueEval?
            ): ValueEval {
                if (arg0 is RefEval || arg0 is AreaEval) {
                    return BoolEval.TRUE
                }
                return BoolEval.FALSE
            }
        }
    }
}
