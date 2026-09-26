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

import com.wxiwei.office.fc.hssf.formula.eval.ConcatEval
import com.wxiwei.office.fc.hssf.formula.eval.FunctionEval.getBasicFunction
import com.wxiwei.office.fc.hssf.formula.eval.IntersectionEval
import com.wxiwei.office.fc.hssf.formula.eval.PercentEval
import com.wxiwei.office.fc.hssf.formula.eval.RangeEval
import com.wxiwei.office.fc.hssf.formula.eval.RelationalOperationEval
import com.wxiwei.office.fc.hssf.formula.eval.TwoOperandNumericOperation
import com.wxiwei.office.fc.hssf.formula.eval.UnaryMinusEval
import com.wxiwei.office.fc.hssf.formula.eval.UnaryPlusEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.Function
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry
import com.wxiwei.office.fc.hssf.formula.function.Indirect
import com.wxiwei.office.fc.hssf.formula.ptg.AbstractFunctionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.AddPtg
import com.wxiwei.office.fc.hssf.formula.ptg.ConcatPtg
import com.wxiwei.office.fc.hssf.formula.ptg.DividePtg
import com.wxiwei.office.fc.hssf.formula.ptg.EqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.GreaterEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.GreaterThanPtg
import com.wxiwei.office.fc.hssf.formula.ptg.IntersectionPtg
import com.wxiwei.office.fc.hssf.formula.ptg.LessEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.LessThanPtg
import com.wxiwei.office.fc.hssf.formula.ptg.MultiplyPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NotEqualPtg
import com.wxiwei.office.fc.hssf.formula.ptg.OperationPtg
import com.wxiwei.office.fc.hssf.formula.ptg.PercentPtg
import com.wxiwei.office.fc.hssf.formula.ptg.PowerPtg
import com.wxiwei.office.fc.hssf.formula.ptg.RangePtg
import com.wxiwei.office.fc.hssf.formula.ptg.SubtractPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnaryMinusPtg
import com.wxiwei.office.fc.hssf.formula.ptg.UnaryPlusPtg
import java.lang.reflect.Modifier

/**
 * This class creates <tt>OperationEval</tt> instances to help evaluate <tt>OperationPtg</tt>
 * formula tokens.
 * 
 * @author Josh Micich
 */
internal object OperationEvaluatorFactory {
    private val _instancesByPtgClass: MutableMap<OperationPtg?, Function?> =
        initialiseInstancesMap()

    private fun initialiseInstancesMap(): MutableMap<OperationPtg?, Function?> {
        val m: MutableMap<OperationPtg?, Function?> = HashMap<OperationPtg?, Function?>(32)

        put(m, EqualPtg.instance, RelationalOperationEval.EqualEval)
        put(m, GreaterEqualPtg.instance, RelationalOperationEval.GreaterEqualEval)
        put(m, GreaterThanPtg.instance, RelationalOperationEval.GreaterThanEval)
        put(m, LessEqualPtg.instance, RelationalOperationEval.LessEqualEval)
        put(m, LessThanPtg.instance, RelationalOperationEval.LessThanEval)
        put(m, NotEqualPtg.instance, RelationalOperationEval.NotEqualEval)

        put(m, ConcatPtg.instance, ConcatEval.instance)
        put(m, AddPtg.instance, TwoOperandNumericOperation.AddEval)
        put(m, DividePtg.instance, TwoOperandNumericOperation.DivideEval)
        put(m, MultiplyPtg.instance, TwoOperandNumericOperation.MultiplyEval)
        put(m, PercentPtg.instance, PercentEval.instance)
        put(m, PowerPtg.instance, TwoOperandNumericOperation.PowerEval)
        put(m, SubtractPtg.instance, TwoOperandNumericOperation.SubtractEval)
        put(m, UnaryMinusPtg.instance, UnaryMinusEval.instance)
        put(m, UnaryPlusPtg.instance, UnaryPlusEval.instance)
        put(m, RangePtg.instance, RangeEval.instance)
        put(m, IntersectionPtg.instance, IntersectionEval.instance)
        return m
    }

    private fun put(
        m: MutableMap<OperationPtg?, Function?>, ptgKey: OperationPtg,
        instance: Function?
    ) {
        // make sure ptg has single private constructor because map lookups assume singleton keys
        val cc = ptgKey.javaClass.getDeclaredConstructors()
        if (cc.size > 1 || !Modifier.isPrivate(cc[0]!!.getModifiers())) {
            throw RuntimeException(
                ("Failed to verify instance ("
                        + ptgKey.javaClass.getName() + ") is a singleton.")
            )
        }
        m.put(ptgKey, instance)
    }

    /**
     * returns the OperationEval concrete impl instance corresponding
     * to the supplied operationPtg
     */
    fun evaluate(
        ptg: OperationPtg, args: Array<ValueEval?>,
        ec: OperationEvaluationContext
    ): ValueEval? {
        requireNotNull(ptg) { "ptg must not be null" }
        val result = _instancesByPtgClass.get(ptg)

        if (result != null) {
            return result.evaluate(args, ec.rowIndex, ec.columnIndex.toShort().toInt())
        }

        if (ptg is AbstractFunctionPtg) {
            val fptg = ptg
            val functionIndex = fptg.functionIndex.toInt()
            when (functionIndex) {
                FunctionMetadataRegistry.FUNCTION_INDEX_INDIRECT.toInt() -> return Indirect.instance.evaluate(
                    args,
                    ec
                )

                FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL.toInt() -> return UserDefinedFunction.Companion.instance.evaluate(
                    args,
                    ec
                )
            }

            return getBasicFunction(functionIndex)!!.evaluate(
                args,
                ec.rowIndex,
                ec.columnIndex.toShort().toInt()
            )
        }
        throw RuntimeException("Unexpected operation ptg class (" + ptg.javaClass.getName() + ")")
    }
}
