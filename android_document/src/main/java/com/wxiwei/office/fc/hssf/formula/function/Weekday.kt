/*
 * 文件名称:          Weekday.java
 *  
 * 编译器:            android2.2
 * 时间:              上午10:04:51
 */
package com.wxiwei.office.fc.hssf.formula.function

import com.wxiwei.office.fc.hssf.formula.eval.ErrorEval
import com.wxiwei.office.fc.hssf.formula.eval.NumberEval
import com.wxiwei.office.fc.hssf.formula.eval.ValueEval
import com.wxiwei.office.fc.ss.usermodel.ErrorConstants

/**
 * TODO: 文件注释
 * 
 * 
 * 
 * 
 * Read版本:        Read V1.0
 * 
 * 
 * 作者:            jqin
 * 
 * 
 * 日期:            2012-6-7
 * 
 * 
 * 负责人:           jqin
 * 
 * 
 * 负责小组:
 * 
 * 
 * 
 * 
 */
class Weekday : Var1or2ArgFunction() {
    override fun evaluate(srcRowIndex: Int, srcColumnIndex: Int, arg0: ValueEval?): ValueEval? {
        return evaluate(srcRowIndex, srcColumnIndex, arg0, DEFAULT_ARG1)
    }

    override fun evaluate(
        srcRowIndex: Int,
        srcColumnIndex: Int,
        arg0: ValueEval?,
        arg1: ValueEval?
    ): ValueEval? {
        var eval = (CalendarFieldFunction.Companion.WEEKDAY as CalendarFieldFunction).evaluate(
            srcRowIndex,
            srcColumnIndex,
            arg0
        )
        if (arg1 is NumberEval) {
            val numEval = arg1
            var w = Math.round((eval as NumberEval).numberValue).toInt()

            when (Math.round(numEval.numberValue).toInt()) {
                1 -> {}
                2 -> {
                    w = (w - 1)
                    w = (if (w == 0) 7 else w)
                    eval = NumberEval(w.toDouble())
                }

                3 -> {
                    w = (w - 2)
                    w = (if (w >= 0) w else 6)
                    eval = NumberEval(w.toDouble())
                }

                else -> eval = ErrorEval.valueOf(ErrorConstants.ERROR_NUM)
            }
        }
        return eval
    }

    companion object {
        private val DEFAULT_ARG1: ValueEval = NumberEval(1.0)
    }
}
