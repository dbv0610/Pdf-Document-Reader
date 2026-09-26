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
import com.wxiwei.office.fc.hssf.formula.eval.AreaEval
import com.wxiwei.office.fc.hssf.formula.eval.BlankEval
import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.EvaluationException
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.NumericValueEval
import com.wxiwei.office.fc.hssf.formula.eval.RefEval
import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval


/**
 * Implementation for the Excel function SUMPRODUCT
 *
 *
 * 
 * Syntax : <br></br>
 * SUMPRODUCT ( array1[, array2[, array3[, ...]]])
 * <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 * <tr><th>array1, ... arrayN&nbsp;&nbsp;</th><td>typically area references,
 * possibly cell references or scalar values</td></tr>
</table> * <br></br>
 * 
 * Let A**n**<sub>(**i**,**j**)</sub> represent the element in the **i**th row **j**th column
 * of the **n**th array<br></br>
 * Assuming each array has the same dimensions (W, H), the result is defined as:<br></br>
 * SUMPRODUCT = <sub>**i**: 1..H</sub> &nbsp;
 * (&nbsp; <sub>**j**: 1..W</sub> &nbsp;
 * (&nbsp; <sub>**n**: 1..N</sub>
 * A**n**<sub>(**i**,**j**)</sub>&nbsp;
 * )&nbsp;
 * )
 * 
 * @author Josh Micich
 */
class Sumproduct : Function {
    override fun evaluate(args: Array<ValueEval?>, srcCellRow: Int, srcCellCol: Int): ValueEval? {
        val maxN = args.size

        if (maxN < 1) {
            return ErrorEval.VALUE_INVALID
        }
        val firstArg = args[0]
        try {
            if (firstArg is NumericValueEval) {
                return evaluateSingleProduct(args)
            }
            if (firstArg is RefEval) {
                return evaluateSingleProduct(args)
            }
            if (firstArg is TwoDEval) {
                val ae = firstArg
                if (ae.isRow && ae.isColumn) {
                    return evaluateSingleProduct(args)
                }
                return evaluateAreaSumProduct(args)
            }
        } catch (e: EvaluationException) {
            return e.errorEval
        }
        throw RuntimeException(
            ("Invalid arg type for SUMPRODUCT: ("
                    + firstArg!!.javaClass.getName() + ")")
        )
    }

    companion object {
        @Throws(EvaluationException::class)
        private fun evaluateSingleProduct(evalArgs: Array<ValueEval?>): ValueEval {
            val maxN = evalArgs.size

            var term = 1.0
            for (n in 0..<maxN) {
                val `val`: Double = getScalarValue(evalArgs[n])
                term *= `val`
            }
            return NumberEval(term)
        }

        @Throws(EvaluationException::class)
        private fun getScalarValue(arg: ValueEval?): Double {
            var eval: ValueEval?
            if (arg is RefEval) {
                val re = arg
                eval = re.innerValueEval
            } else {
                eval = arg
            }

            if (eval == null) {
                throw RuntimeException("parameter may not be null")
            }
            if (eval is AreaEval) {
                val ae = eval
                // an area ref can work as a scalar value if it is 1x1
                if (!ae.isColumn || !ae.isRow) {
                    throw EvaluationException(ErrorEval.VALUE_INVALID)
                }
                eval = ae.getRelativeValue(0, 0)
            }

            return getProductTerm(eval, true)
        }

        @Throws(EvaluationException::class)
        private fun evaluateAreaSumProduct(evalArgs: Array<ValueEval?>): ValueEval {
            val maxN = evalArgs.size
            val args: Array<TwoDEval?> = arrayOfNulls<TwoDEval>(maxN)
            try {
                System.arraycopy(evalArgs, 0, args, 0, maxN)
            } catch (e: ArrayStoreException) {
                // one of the other args was not an AreaRef
                return ErrorEval.VALUE_INVALID
            }


            val firstArg = args[0]!!

            val height = firstArg.height
            val width = firstArg.width // TODO - junit

            // first check dimensions
            if (!areasAllSameSize(args, height, width)) {
                // normally this results in #VALUE!,
                // but errors in individual cells take precedence
                for (i in 1..<args.size) {
                    throwFirstError(args[i]!!)
                }
                return ErrorEval.VALUE_INVALID
            }

            var acc = 0.0

            for (rrIx in 0..<height) {
                for (rcIx in 0..<width) {
                    var term = 1.0
                    for (n in 0..<maxN) {
                        val `val`: Double = getProductTerm(args[n]!!.getValue(rrIx, rcIx), false)
                        term *= `val`
                    }
                    acc += term
                }
            }

            return NumberEval(acc)
        }

        @Throws(EvaluationException::class)
        private fun throwFirstError(areaEval: TwoDEval) {
            val height = areaEval.height
            val width = areaEval.width
            for (rrIx in 0..<height) {
                for (rcIx in 0..<width) {
                    val ve = areaEval.getValue(rrIx, rcIx)
                    if (ve is ErrorEval) {
                        throw EvaluationException(ve)
                    }
                }
            }
        }

        private fun areasAllSameSize(args: Array<TwoDEval?>, height: Int, width: Int): Boolean {
            for (i in args.indices) {
                val areaEval = args[i]!!
                // check that height and width match
                if (areaEval.height != height) {
                    return false
                }
                if (areaEval.width != width) {
                    return false
                }
            }
            return true
        }


        /**
         * Determines a `double` value for the specified `ValueEval`.
         * @param isScalarProduct `false` for SUMPRODUCTs over area refs.
         * @throws EvaluationException if `ve` represents an error value.
         * 
         * 
         * Note - string values and empty cells are interpreted differently depending on
         * `isScalarProduct`.  For scalar products, if any term is blank or a string, the
         * error (#VALUE!) is raised.  For area (sum)products, if any term is blank or a string, the
         * result is zero.
         */
        @Throws(EvaluationException::class)
        private fun getProductTerm(ve: ValueEval?, isScalarProduct: Boolean): Double {
            if (ve is BlankEval || ve == null) {
                // TODO - shouldn't BlankEval.INSTANCE be used always instead of null?
                // null seems to occur when the blank cell is part of an area ref (but not reliably)
                if (isScalarProduct) {
                    throw EvaluationException(ErrorEval.VALUE_INVALID)
                }
                return 0.0
            }

            if (ve is ErrorEval) {
                throw EvaluationException(ve)
            }
            if (ve is StringEval) {
                if (isScalarProduct) {
                    throw EvaluationException(ErrorEval.VALUE_INVALID)
                }
                // Note for area SUMPRODUCTs, string values are interpreted as zero
                // even if they would parse as valid numeric values
                return 0.0
            }
            if (ve is NumericValueEval) {
                val nve = ve
                return nve.numberValue
            }
            throw RuntimeException(
                ("Unexpected value eval class ("
                        + ve.javaClass.getName() + ")")
            )
        }
    }
}
