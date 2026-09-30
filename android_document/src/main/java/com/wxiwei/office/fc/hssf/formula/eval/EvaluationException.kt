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
package com.wxiwei.office.fc.hssf.formula.eval

/**
 * This class is used to simplify error handling logic *within* operator and function
 * implementations.   Note - <tt>OperationEval.evaluate()</tt> and <tt>Function.evaluate()</tt>
 * method signatures do not throw this exception so it cannot propagate outside.
 *
 *
 * 
 * Here is an example coded without <tt>EvaluationException</tt>, to show how it can help:
 * <pre>
 * public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
 * // ...
 * Eval arg0 = args[0];
 * if(arg0 instanceof ErrorEval) {
 * return arg0;
 * }
 * if(!(arg0 instanceof AreaEval)) {
 * return ErrorEval.VALUE_INVALID;
 * }
 * double temp = 0;
 * AreaEval area = (AreaEval)arg0;
 * ValueEval[] values = area.getValues();
 * for (int i = 0; i < values.length; i++) {
 * ValueEval ve = values[i];
 * if(ve instanceof ErrorEval) {
 * return ve;
 * }
 * if(!(ve instanceof NumericValueEval)) {
 * return ErrorEval.VALUE_INVALID;
 * }
 * temp += ((NumericValueEval)ve).getNumberValue();
 * }
 * // ...
 * }
</pre> * 
 * In this example, if any error is encountered while processing the arguments, an error is
 * returned immediately. This code is difficult to refactor due to all the points where errors
 * are returned.<br></br>
 * Using <tt>EvaluationException</tt> allows the error returning code to be consolidated to one
 * place.
 *
 *
 * <pre>
 * public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
 * try {
 * // ...
 * AreaEval area = getAreaArg(args[0]);
 * double temp = sumValues(area.getValues());
 * // ...
 * } catch (EvaluationException e) {
 * return e.getErrorEval();
 * }
 * }
 * 
 * private static AreaEval getAreaArg(Eval arg0) throws EvaluationException {
 * if (arg0 instanceof ErrorEval) {
 * throw new EvaluationException((ErrorEval) arg0);
 * }
 * if (arg0 instanceof AreaEval) {
 * return (AreaEval) arg0;
 * }
 * throw EvaluationException.invalidValue();
 * }
 * 
 * private double sumValues(ValueEval[] values) throws EvaluationException {
 * double temp = 0;
 * for (int i = 0; i < values.length; i++) {
 * ValueEval ve = values[i];
 * if (ve instanceof ErrorEval) {
 * throw new EvaluationException((ErrorEval) ve);
 * }
 * if (!(ve instanceof NumericValueEval)) {
 * throw EvaluationException.invalidValue();
 * }
 * temp += ((NumericValueEval) ve).getNumberValue();
 * }
 * return temp;
 * }
</pre> * 
 * It is not mandatory to use EvaluationException, doing so might give the following advantages:<br></br>
 * - Methods can more easily be extracted, allowing for re-use.<br></br>
 * - Type management (typecasting etc) is simpler because error conditions have been separated from
 * intermediate calculation values.<br></br>
 * - Fewer local variables are required. Local variables can have stronger types.<br></br>
 * - It is easier to mimic common Excel error handling behaviour (exit upon encountering first
 * error), because exceptions conveniently propagate up the call stack regardless of execution
 * points or the number of levels of nested calls.
 *
 *
 * 
 * **Note** - Only standard evaluation errors are represented by <tt>EvaluationException</tt> (
 * i.e. conditions expected to be encountered when evaluating arbitrary Excel formulas). Conditions
 * that could never occur in an Excel spreadsheet should result in runtime exceptions. Care should
 * be taken to not translate any POI internal error into an Excel evaluation error code.
 * 
 * @author Josh Micich
 */
class EvaluationException(val errorEval: ErrorEval) : Exception() {
    companion object {
        // some convenience factory methods
        /** **#VALUE!** - Wrong type of operand  */
        fun invalidValue(): EvaluationException {
            return EvaluationException(ErrorEval.Companion.VALUE_INVALID)
        }

        /** **#REF!** - Illegal or deleted cell reference  */
        fun invalidRef(): EvaluationException {
            return EvaluationException(ErrorEval.Companion.REF_INVALID)
        }

        /** **#NUM!** - Value range overflow  */
        fun numberError(): EvaluationException {
            return EvaluationException(ErrorEval.Companion.NUM_ERROR)
        }
    }
}
