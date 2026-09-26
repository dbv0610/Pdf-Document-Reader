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

import com.wxiwei.office.fc.hssf.formula.function.Address
import com.wxiwei.office.fc.hssf.formula.function.AggregateFunction
import com.wxiwei.office.fc.hssf.formula.function.Averagea
import com.wxiwei.office.fc.hssf.formula.function.BooleanFunction
import com.wxiwei.office.fc.hssf.formula.function.CalendarFieldFunction
import com.wxiwei.office.fc.hssf.formula.function.Choose
import com.wxiwei.office.fc.hssf.formula.function.Column
import com.wxiwei.office.fc.hssf.formula.function.Columns
import com.wxiwei.office.fc.hssf.formula.function.Count
import com.wxiwei.office.fc.hssf.formula.function.Counta
import com.wxiwei.office.fc.hssf.formula.function.Countblank
import com.wxiwei.office.fc.hssf.formula.function.Countif
import com.wxiwei.office.fc.hssf.formula.function.DateFunc
import com.wxiwei.office.fc.hssf.formula.function.Days360
import com.wxiwei.office.fc.hssf.formula.function.Errortype
import com.wxiwei.office.fc.hssf.formula.function.Even
import com.wxiwei.office.fc.hssf.formula.function.FinanceFunction
import com.wxiwei.office.fc.hssf.formula.function.Function
import com.wxiwei.office.fc.hssf.formula.function.FunctionMetadataRegistry
import com.wxiwei.office.fc.hssf.formula.function.Hlookup
import com.wxiwei.office.fc.hssf.formula.function.Hyperlink
import com.wxiwei.office.fc.hssf.formula.function.IfFunc
import com.wxiwei.office.fc.hssf.formula.function.Index
import com.wxiwei.office.fc.hssf.formula.function.Irr
import com.wxiwei.office.fc.hssf.formula.function.LogicalFunction
import com.wxiwei.office.fc.hssf.formula.function.Lookup
import com.wxiwei.office.fc.hssf.formula.function.Match
import com.wxiwei.office.fc.hssf.formula.function.MinaMaxa
import com.wxiwei.office.fc.hssf.formula.function.Mode
import com.wxiwei.office.fc.hssf.formula.function.Na
import com.wxiwei.office.fc.hssf.formula.function.NotImplementedFunction
import com.wxiwei.office.fc.hssf.formula.function.Now
import com.wxiwei.office.fc.hssf.formula.function.Npv
import com.wxiwei.office.fc.hssf.formula.function.NumericFunction
import com.wxiwei.office.fc.hssf.formula.function.Odd
import com.wxiwei.office.fc.hssf.formula.function.Offset
import com.wxiwei.office.fc.hssf.formula.function.Replace
import com.wxiwei.office.fc.hssf.formula.function.RowFunc
import com.wxiwei.office.fc.hssf.formula.function.Rows
import com.wxiwei.office.fc.hssf.formula.function.Substitute
import com.wxiwei.office.fc.hssf.formula.function.Subtotal
import com.wxiwei.office.fc.hssf.formula.function.Sumif
import com.wxiwei.office.fc.hssf.formula.function.Sumproduct
import com.wxiwei.office.fc.hssf.formula.function.Sumx2my2
import com.wxiwei.office.fc.hssf.formula.function.Sumx2py2
import com.wxiwei.office.fc.hssf.formula.function.Sumxmy2
import com.wxiwei.office.fc.hssf.formula.function.T
import com.wxiwei.office.fc.hssf.formula.function.TextFunction
import com.wxiwei.office.fc.hssf.formula.function.TimeFunc
import com.wxiwei.office.fc.hssf.formula.function.Today
import com.wxiwei.office.fc.hssf.formula.function.Value
import com.wxiwei.office.fc.hssf.formula.function.Vlookup
import com.wxiwei.office.fc.hssf.formula.function.Weekday

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
object FunctionEval {
    // convenient access to namespace
    private val ID: FunctionID? = null

    /**
     * Array elements corresponding to unimplemented functions are `null`
     */
    internal val functions: Array<Function?> = produceFunctions()

    private fun produceFunctions(): Array<Function?> {
        val retval: Array<Function?> = arrayOfNulls<Function>(368)

        retval[0] = Count()
        retval[FunctionID.IF] = IfFunc()
        retval[2] = LogicalFunction.ISNA
        retval[3] = LogicalFunction.ISERROR
        retval[FunctionID.SUM] = AggregateFunction.SUM
        retval[5] = AggregateFunction.AVERAGE
        retval[6] = AggregateFunction.MIN
        retval[7] = AggregateFunction.MAX
        retval[8] = RowFunc() // ROW
        retval[9] = Column()
        retval[10] = Na()
        retval[11] = Npv()
        retval[12] = AggregateFunction.STDEV
        retval[13] = NumericFunction.DOLLAR

        retval[15] = NumericFunction.SIN
        retval[16] = NumericFunction.COS
        retval[17] = NumericFunction.TAN
        retval[18] = NumericFunction.ATAN
        retval[19] = NumericFunction.PI
        retval[20] = NumericFunction.SQRT
        retval[21] = NumericFunction.EXP
        retval[22] = NumericFunction.LN
        retval[23] = NumericFunction.LOG10
        retval[24] = NumericFunction.ABS
        retval[25] = NumericFunction.INT
        retval[26] = NumericFunction.SIGN
        retval[27] = NumericFunction.ROUND
        retval[28] = Lookup()
        retval[29] = Index()

        retval[31] = TextFunction.MID
        retval[32] = TextFunction.LEN
        retval[33] = Value()
        retval[34] = BooleanFunction.TRUE
        retval[35] = BooleanFunction.FALSE
        retval[36] = BooleanFunction.AND
        retval[37] = BooleanFunction.OR
        retval[38] = BooleanFunction.NOT
        retval[39] = NumericFunction.MOD

        retval[46] = AggregateFunction.VAR
        retval[48] = TextFunction.TEXT

        retval[56] = FinanceFunction.PV
        retval[57] = FinanceFunction.FV
        retval[58] = FinanceFunction.NPER
        retval[59] = FinanceFunction.PMT

        retval[62] = Irr()
        retval[63] = NumericFunction.RAND
        retval[64] = Match()
        retval[65] = DateFunc.instance
        retval[66] = TimeFunc()
        retval[67] = CalendarFieldFunction.DAY
        retval[68] = CalendarFieldFunction.MONTH
        retval[69] = CalendarFieldFunction.YEAR
        retval[70] = Weekday()

        retval[71] = CalendarFieldFunction.HOUR
        retval[72] = CalendarFieldFunction.MINUTE
        retval[73] = CalendarFieldFunction.SECOND
        retval[74] = Now()

        retval[76] = Rows()
        retval[77] = Columns()
        retval[82] = TextFunction.SEARCH
        retval[FunctionID.OFFSET] = Offset()
        retval[82] = TextFunction.SEARCH

        retval[97] = NumericFunction.ATAN2
        retval[98] = NumericFunction.ASIN
        retval[99] = NumericFunction.ACOS
        retval[FunctionID.CHOOSE] = Choose()
        retval[101] = Hlookup()
        retval[102] = Vlookup()

        retval[105] = LogicalFunction.ISREF

        retval[109] = NumericFunction.LOG

        retval[111] = TextFunction.CHAR
        retval[112] = TextFunction.LOWER
        retval[113] = TextFunction.UPPER

        retval[115] = TextFunction.LEFT
        retval[116] = TextFunction.RIGHT
        retval[117] = TextFunction.EXACT
        retval[118] = TextFunction.TRIM
        retval[119] = Replace()
        retval[120] = Substitute()
        retval[121] = TextFunction.CODE

        retval[124] = TextFunction.FIND

        retval[127] = LogicalFunction.ISTEXT
        retval[128] = LogicalFunction.ISNUMBER
        retval[129] = LogicalFunction.ISBLANK
        retval[130] = T()

        retval[144] = AggregateFunction.DDB

        retval[FunctionID.INDIRECT] = null // Indirect.evaluate has different signature
        retval[162] = TextFunction.CLEAN //Aniket Banerjee    
        retval[169] = Counta()

        retval[183] = AggregateFunction.PRODUCT
        retval[184] = NumericFunction.FACT

        retval[190] = LogicalFunction.ISNONTEXT
        retval[194] = AggregateFunction.VARP
        retval[197] = NumericFunction.TRUNC
        retval[198] = LogicalFunction.ISLOGICAL

        retval[212] = NumericFunction.ROUNDUP
        retval[213] = NumericFunction.ROUNDDOWN
        retval[219] = Address() //Aniket Banerjee
        retval[220] = Days360()
        retval[221] = Today()

        retval[227] = AggregateFunction.MEDIAN
        retval[228] = Sumproduct()
        retval[229] = NumericFunction.SINH
        retval[230] = NumericFunction.COSH
        retval[231] = NumericFunction.TANH
        retval[232] = NumericFunction.ASINH
        retval[233] = NumericFunction.ACOSH
        retval[234] = NumericFunction.ATANH

        retval[247] = AggregateFunction.DB

        retval[FunctionID.EXTERNAL_FUNC] = null // ExternalFunction is a FreeREfFunction

        retval[261] = Errortype()

        retval[269] = AggregateFunction.AVEDEV

        retval[276] = NumericFunction.COMBIN

        retval[279] = Even()

        retval[285] = NumericFunction.FLOOR

        retval[288] = NumericFunction.CEILING

        retval[298] = Odd()

        retval[300] = NumericFunction.POISSON

        retval[303] = Sumxmy2()
        retval[304] = Sumx2my2()
        retval[305] = Sumx2py2()

        retval[318] = AggregateFunction.DEVSQ

        retval[321] = AggregateFunction.SUMSQ

        retval[325] = AggregateFunction.LARGE
        retval[326] = AggregateFunction.SMALL

        retval[330] = Mode()

        retval[336] = TextFunction.CONCATENATE
        retval[337] = NumericFunction.POWER

        retval[342] = NumericFunction.RADIANS
        retval[343] = NumericFunction.DEGREES

        retval[344] = Subtotal()
        retval[345] = Sumif()
        retval[346] = Countif()
        retval[347] = Countblank()

        retval[359] = Hyperlink()

        retval[361] = Averagea()

        retval[362] = MinaMaxa.MAXA
        retval[363] = MinaMaxa.MINA

        for (i in retval.indices) {
            val f: Function? = retval[i]
            if (f == null) {
                val fm = FunctionMetadataRegistry.getFunctionByIndex(i)
                if (fm == null) {
                    continue
                }
                retval[i] = NotImplementedFunction(fm.name)
            }
        }
        return retval
    }

    /**
     * @return `null` if the specified functionIndex is for INDIRECT() or any external (add-in) function.
     */
    fun getBasicFunction(functionIndex: Int): Function? {
        // check for 'free ref' functions first
        when (functionIndex) {
            FunctionID.INDIRECT, FunctionID.EXTERNAL_FUNC -> return null
        }
        // else - must be plain function
        val result = functions[functionIndex]
        if (result == null) {
            throw NotImplementedException("FuncIx=" + functionIndex)
        }
        return result
    }

    /**
     * Some function IDs that require special treatment
     */
    private object FunctionID {
        /** 1  */
        val IF: Int = FunctionMetadataRegistry.FUNCTION_INDEX_IF

        /** 4  */
        val SUM: Int = FunctionMetadataRegistry.FUNCTION_INDEX_SUM.toInt()

        /** 78  */
        const val OFFSET: Int = 78

        /** 100  */
        val CHOOSE: Int = FunctionMetadataRegistry.FUNCTION_INDEX_CHOOSE

        /** 148  */
        val INDIRECT: Int = FunctionMetadataRegistry.FUNCTION_INDEX_INDIRECT.toInt()

        /** 255  */
        val EXTERNAL_FUNC: Int = FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL.toInt()
    }
}
