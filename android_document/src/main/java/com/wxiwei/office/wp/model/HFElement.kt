package com.wxiwei.office.wp.model

import com.wxiwei.office.simpletext.model.AbstractElement

class HFElement(private val elemType: Short, private val hfType: Byte) : AbstractElement() {
    override fun getType(): Short = elemType

    fun getHFType(): Byte = hfType
}
