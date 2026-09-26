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
package com.wxiwei.office.fc.hssf.formula

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NameEval
import com.wxiwei.office.fc.hssf.formula.eval.NameXEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants


/**
 * 
 * Common entry point for all user-defined (non-built-in) functions (where
 * <tt>AbstractFunctionPtg.field_2_fnc_index</tt> == 255)
 * 
 * @author Josh Micich
 * @author Petr Udalau - Improved resolving of UDFs through the ToolPacks.
 */
internal class UserDefinedFunction private constructor() : FreeRefFunction {
    override fun evaluate(args: Array<ValueEval?>, ec: OperationEvaluationContext): ValueEval? {
        val nIncomingArgs = args.size
        if (nIncomingArgs < 1) {
            throw RuntimeException("function name argument missing")
        }

        val nameArg = args[0]
        val functionName: String?
        if (nameArg is NameEval) {
            functionName = nameArg.functionName
        } else if (nameArg is NameXEval) {
            functionName = ec.workbook.resolveNameXText(nameArg.ptg)
        } else {
            throw RuntimeException(
                ("First argument should be a NameEval, but got ("
                        + nameArg!!.javaClass.getName() + ")")
            )
        }
        val targetFunc = ec.findUserDefinedFunction(functionName)
        if (targetFunc == null) {
            return ErrorEval.valueOf(ErrorConstants.ERROR_NAME)
            //throw new NotImplementedException(functionName);
        }
        val nOutGoingArgs = nIncomingArgs - 1
        val outGoingArgs = arrayOfNulls<ValueEval>(nOutGoingArgs)
        System.arraycopy(args, 1, outGoingArgs, 0, nOutGoingArgs)
        return targetFunc.evaluate(outGoingArgs, ec)
    }

    companion object {
        val instance: FreeRefFunction = UserDefinedFunction()
    }
}
