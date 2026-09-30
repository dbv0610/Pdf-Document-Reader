/*
 * Modifications Copyright (c) 2026 dongb2002. All rights reserved.
 *
 * This file is based on third-party open-source code and has been modified by dongb2002.
 * The modifications are proprietary to dongb2002. The original copyright and license notice
 * of this file, where present below, remains in effect for the original portions.
 */

package com.wxiwei.office.fc.hssf.formula.eval

import com.wxiwei.office.fc.hssf.formula.ptg.Ptg
import com.wxiwei.office.fc.hssf.formula.ptg.StringPtg

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
class StringEval(value: String) : StringValueEval {
    private val _value: String = value

    constructor(ptg: Ptg) : this((ptg as StringPtg).value)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getStringValueProperty")
    override val stringValue: String
        get() {
        return _value
    }

    override fun toString(): String {
        val sb = StringBuilder(64)
        sb.append(javaClass.name).append(" [")
        sb.append(_value)
        sb.append("]")
        return sb.toString()
    }

    companion object {
        val EMPTY_INSTANCE: StringEval = StringEval("")
    }
}
