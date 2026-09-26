
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
