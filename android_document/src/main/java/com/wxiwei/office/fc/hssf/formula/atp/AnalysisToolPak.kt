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
package com.wxiwei.office.fc.hssf.formula.atp

import com.wxiwei.office.fc.hssf.formula.OperationEvaluationContext
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.hssf.formula.function.FreeRefFunction
import com.wxiwei.office.fc.hssf.formula.udf.UDFFinder

/**
 * @author Josh Micich
 * @author Petr Udalau - systematized work of add-in libraries and user defined functions.
 */
class AnalysisToolPak private constructor() : UDFFinder {
    private class NotImplemented(private val _functionName: String?) : FreeRefFunction {
        override fun evaluate(
            args: Array<ValueEval?>,
            ec: OperationEvaluationContext
        ): ValueEval? {
            return null
            //throw new NotImplementedException(_functionName);
        }
    }

    private val _functionsByName: MutableMap<String?, FreeRefFunction?> = createFunctionsMap()


    override fun findFunction(name: String?): FreeRefFunction? {
        return _functionsByName.get(name)
    }

    private fun createFunctionsMap(): MutableMap<String?, FreeRefFunction?> {
        val m: MutableMap<String?, FreeRefFunction?> = HashMap<String?, FreeRefFunction?>(108)

        r(m, "ACCRINT", null)
        r(m, "ACCRINTM", null)
        r(m, "AMORDEGRC", null)
        r(m, "AMORLINC", null)
        r(m, "AVERAGEIF", null)
        r(m, "AVERAGEIFS", null)
        r(m, "BAHTTEXT", null)
        r(m, "BESSELI", null)
        r(m, "BESSELJ", null)
        r(m, "BESSELK", null)
        r(m, "BESSELY", null)
        r(m, "BIN2DEC", null)
        r(m, "BIN2HEX", null)
        r(m, "BIN2OCT", null)
        r(m, "COMPLEX", null)
        r(m, "CONVERT", null)
        r(m, "COUNTIFS", null)
        r(m, "COUPDAYBS", null)
        r(m, "COUPDAYS", null)
        r(m, "COUPDAYSNC", null)
        r(m, "COUPNCD", null)
        r(m, "COUPNUM", null)
        r(m, "COUPPCD", null)
        r(m, "CUBEKPIMEMBER", null)
        r(m, "CUBEMEMBER", null)
        r(m, "CUBEMEMBERPROPERTY", null)
        r(m, "CUBERANKEDMEMBER", null)
        r(m, "CUBESET", null)
        r(m, "CUBESETCOUNT", null)
        r(m, "CUBEVALUE", null)
        r(m, "CUMIPMT", null)
        r(m, "CUMPRINC", null)
        r(m, "DEC2BIN", null)
        r(m, "DEC2HEX", null)
        r(m, "DEC2OCT", null)
        r(m, "DELTA", null)
        r(m, "DISC", null)
        r(m, "DOLLARDE", null)
        r(m, "DOLLARFR", null)
        r(m, "DURATION", null)
        r(m, "EDATE", null)
        r(m, "EFFECT", null)
        r(m, "EOMONTH", null)
        r(m, "ERF", null)
        r(m, "ERFC", null)
        r(m, "FACTDOUBLE", null)
        r(m, "FVSCHEDULE", null)
        r(m, "GCD", null)
        r(m, "GESTEP", null)
        r(m, "HEX2BIN", null)
        r(m, "HEX2DEC", null)
        r(m, "HEX2OCT", null)
        r(m, "IFERROR", null)
        r(m, "IMABS", null)
        r(m, "IMAGINARY", null)
        r(m, "IMARGUMENT", null)
        r(m, "IMCONJUGATE", null)
        r(m, "IMCOS", null)
        r(m, "IMDIV", null)
        r(m, "IMEXP", null)
        r(m, "IMLN", null)
        r(m, "IMLOG10", null)
        r(m, "IMLOG2", null)
        r(m, "IMPOWER", null)
        r(m, "IMPRODUCT", null)
        r(m, "IMREAL", null)
        r(m, "IMSIN", null)
        r(m, "IMSQRT", null)
        r(m, "IMSUB", null)
        r(m, "IMSUM", null)
        r(m, "INTRATE", null)
        r(m, "ISEVEN", ParityFunction.Companion.IS_EVEN)
        r(m, "ISODD", ParityFunction.Companion.IS_ODD)
        r(m, "JIS", null)
        r(m, "LCM", null)
        r(m, "MDURATION", null)
        r(m, "MROUND", MRound.Companion.instance)
        r(m, "MULTINOMIAL", null)
        r(m, "NETWORKDAYS", null)
        r(m, "NOMINAL", null)
        r(m, "OCT2BIN", null)
        r(m, "OCT2DEC", null)
        r(m, "OCT2HEX", null)
        r(m, "ODDFPRICE", null)
        r(m, "ODDFYIELD", null)
        r(m, "ODDLPRICE", null)
        r(m, "ODDLYIELD", null)
        r(m, "PRICE", null)
        r(m, "PRICEDISC", null)
        r(m, "PRICEMAT", null)
        r(m, "QUOTIENT", null)
        r(m, "RANDBETWEEN", RandBetween.Companion.instance)
        r(m, "RECEIVED", null)
        r(m, "RTD", null)
        r(m, "SERIESSUM", null)
        r(m, "SQRTPI", null)
        r(m, "SUMIFS", null)
        r(m, "TBILLEQ", null)
        r(m, "TBILLPRICE", null)
        r(m, "TBILLYIELD", null)
        r(m, "WEEKNUM", null)
        r(m, "WORKDAY", null)
        r(m, "XIRR", null)
        r(m, "XNPV", null)
        r(m, "YEARFRAC", YearFrac.Companion.instance)
        r(m, "YIELD", null)
        r(m, "YIELDDISC", null)
        r(m, "YIELDMAT", null)

        return m
    }

    companion object {
        @JvmField
        val instance: UDFFinder = AnalysisToolPak()

        private fun r(
            m: MutableMap<String?, FreeRefFunction?>,
            functionName: String?,
            pFunc: FreeRefFunction?
        ) {
            val func = if (pFunc == null) NotImplemented(functionName) else pFunc
            m.put(functionName, func)
        }
    }
}
