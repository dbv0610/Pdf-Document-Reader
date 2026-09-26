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

import com.wxiwei.office.fc.hssf.formula.eval.StringEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval

/**
 * Implementation of Excel HYPERLINK function.
 *
 *
 * 
 * In Excel this function has special behaviour - it causes the displayed cell value to behave like
 * a hyperlink in the GUI. From an evaluation perspective however, it is very simple.
 *
 *
 * 
 * **Syntax**:<br></br>
 * **HYPERLINK**(**link_location**, friendly_name)
 *
 *
 * 
 * **link_location** The URL of the hyperlink <br></br>
 * **friendly_name** (optional) the value to display
 *
 *
 * 
 * Returns last argument.  Leaves type unchanged (does not convert to [StringEval]).
 * 
 * @author Wayne Clingingsmith
 */
class Hyperlink : Var1or2ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        return arg0
    }

    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        // note - if last arg is MissingArgEval, result will be NumberEval.ZERO,
        // but WorkbookEvaluator does that translation
        return arg1
    }
}
