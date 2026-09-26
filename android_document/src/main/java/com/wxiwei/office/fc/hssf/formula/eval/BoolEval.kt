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
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class BoolEval private constructor(val booleanValue: Boolean) : NumericValueEval, StringValueEval {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNumberValueProperty")
    override val numberValue: Double
        get() {
        return (if (this.booleanValue) 1 else 0).toDouble()
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String
        get() {
        return if (this.booleanValue) "TRUE" else "FALSE"
    }

    override fun toString(): String {
        val sb = StringBuilder(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(stringValue)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        val FALSE: BoolEval = BoolEval(false)

        val TRUE: BoolEval = BoolEval(true)

        /**
         * Convenience method for the following:<br></br>
         * `(b ? BoolEval.TRUE : BoolEval.FALSE)`
         * 
         * @return the <tt>BoolEval</tt> instance representing <tt>b</tt>.
         */
        fun valueOf(b: Boolean): BoolEval? {
            return if (b) TRUE else FALSE
        }
    }
}
