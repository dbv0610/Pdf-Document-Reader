/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */
package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.ptg.IntPtg
import com.wxiwei.office.fc.hssf.formula.ptg.NumberPtg
import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.ss.util.NumberToTextConverter

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class NumberEval : NumericValueEval, StringValueEval {
    private val _value: Double
    private var _stringValue: String? = null

    constructor(ptg: Ptg) {
        _value = when (ptg) {
            is IntPtg -> {
                ptg.value.toDouble()
            }

            is NumberPtg -> {
                ptg.value
            }

            else -> {
                throw IllegalArgumentException(
                    ("bad argument type (" + ptg.javaClass.getName()
                            + ")")
                )
            }
        }
    }

    constructor(value: Double) {
        _value = value
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getNumberValueProperty")
    override val numberValue: Double
        get() {
        return _value
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String
        get() {
        if (_stringValue == null) {
            _stringValue = NumberToTextConverter.toText(_value)
        }
        return _stringValue!!
    }

    override fun toString(): String {
        val sb = StringBuffer(64)
        sb.append(javaClass.getName()).append(" [")
        sb.append(stringValue)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        val ZERO: NumberEval = NumberEval(0.0)
    }
}
