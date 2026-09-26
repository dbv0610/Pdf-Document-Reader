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

import com.wxiwei.office.fc.hssf.formula.function.NumericFunction.OneArg


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class Even : OneArg() {
    override fun evaluate(d: Double): Double {
        if (d == 0.0) {
            return 0.0
        }
        val result: Long
        if (d > 0) {
            result = calcEven(d)
        } else {
            result = -calcEven(-d)
        }
        return result.toDouble()
    }

    companion object {
        private const val PARITY_MASK = -0x2L

        private fun calcEven(d: Double): Long {
            val x = (d.toLong()) and PARITY_MASK
            if (x.toDouble() == d) {
                return x
            }
            return x + 2
        }
    }
}
